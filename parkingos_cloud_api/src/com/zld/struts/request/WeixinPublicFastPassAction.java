package com.zld.struts.request;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.wxpublic.util.CommonUtil;
import com.zld.wxpublic.util.PayCommonUtil;

public class WeixinPublicFastPassAction extends Action {
	@Autowired
	private DataBaseService daService;
	
	@Autowired
	private PgOnlyReadService pService;
	
	@Autowired
	private PublicMethods publicMethods;
	
	@Autowired
	private MemcacheUtils memcacheUtils;
	
	@Autowired
	private CommonMethods commonMethods;
	
	@Autowired
	private LogService logService;
	
	private Logger logger = Logger.getLogger(WeixinPublicFastPassAction.class);
	
	/**
	 * weixin
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Long curtime = System.currentTimeMillis()/1000;
		String action = RequestUtil.processParams(request, "action");
		if(action.equals("prepay")){//ɨNFC����ѯ��ǰ����������Ԥ֧��
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			String code = RequestUtil.processParams(request, "code");
			String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXPUBLIC_APPID+"&secret="+Constants.WXPUBLIC_SECRET+"&code="+code+"&grant_type=authorization_code";
			String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
			JSONObject map = JSONObject.fromObject(result);
			if(map == null || map.get("errcode") != null){
				logger.error(">>>>>>>>>>>>��ȡopenidʧ��....,���»�ȡ>>>>>>>>>>>");
				String redirect_url = "http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpfast.do%3faction%3dprepay%26orderid%3d"+orderid;
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
						+ Constants.WXPUBLIC_APPID
						+ "&redirect_uri="
						+ redirect_url
						+ "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				response.sendRedirect(url);
				return null;
			}
			String openid = (String)map.get("openid");
//			String openid = "oRoekt7uy9abm5hrUBCWYHHDF5sY";
			//**************************����openid��ȡ�û���Ϣ*******************************//
			Map<String, Object> userinfoMap = commonMethods.getUserinfoByOpenid(openid);
			if(userinfoMap == null || (Long)userinfoMap.get("uin") < 0){
				return mapping.findForward("error");
			}
			Long uin = (Long)userinfoMap.get("uin");
			Integer bindflag = (Integer)userinfoMap.get("bindflag");
			//**************************��ȡ������Ϣ***************************************//
			Long end_time = curtime + 15*60;
			if(orderid > 0){
				Map<String, Object> ordermap = commonMethods.getOrderInfo(orderid, -1L, end_time);
				if(ordermap != null){
					String descp = "";
					Long shopTicketId = (Long)ordermap.get("shopticketid");
					Integer ticketstate =  (Integer)ordermap.get("ticketstate");
					Integer tickettype = (Integer)ordermap.get("tickettype");
					Integer tickettime = (Integer)ordermap.get("tickettime");
					Double beforetotal = Double.valueOf(ordermap.get("beforetotal") + "");
					Double aftertotal = Double.valueOf(ordermap.get("aftertotal") + "");
					if(ticketstate == 1){
						if(tickettype == 3){
							descp = tickettime + "";
						}else if(tickettype == 4){
							descp = "���";
						}
					}else if(ticketstate == 0){
						descp = "��ȯ��ʹ��";
					}
					request.setAttribute("createtime", ordermap.get("createtime"));
					request.setAttribute("starttime", ordermap.get("starttime"));
					request.setAttribute("parktime", ordermap.get("parktime"));
					request.setAttribute("beforetotal", ordermap.get("beforetotal"));
					request.setAttribute("aftertotal", ordermap.get("aftertotal"));
					request.setAttribute("distotal", StringUtils.formatDouble(beforetotal - aftertotal));
					request.setAttribute("prestate", ordermap.get("prestate"));
					request.setAttribute("pretotal", ordermap.get("pretotal"));
					request.setAttribute("descp", descp);
					request.setAttribute("carnumber", ordermap.get("carnumber"));
					request.setAttribute("shopticketid", shopTicketId);
				}
			}
			request.setAttribute("openid", openid);
			request.setAttribute("orderid", orderid);
			return mapping.findForward("prepay");
		}else if(action.equals("beginprepay")){
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			String openid = RequestUtil.processParams(request, "openid");
			Integer delaytime = RequestUtil.getInteger(request, "delaytime", 0);
			Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
			Integer paytype = RequestUtil.getInteger(request, "paytype", 0);
			if(orderid == -1 || openid.equals("")){
				return mapping.findForward("error");
			}
			//**************************����openid��ȡ�û���Ϣ*******************************//
			Map<String, Object> userinfoMap = commonMethods.getUserinfoByOpenid(openid);
			if(userinfoMap == null || (Long)userinfoMap.get("uin") < 0){
				return mapping.findForward("error");
			}
			Long uin = (Long)userinfoMap.get("uin");
			Integer bindflag = (Integer)userinfoMap.get("bindflag");
			String mobile = (String)userinfoMap.get("mobile");
			Double balance = Double.valueOf(userinfoMap.get("balance") + "");
			logger.error("orderid:"+orderid+"map:"+userinfoMap);
			//**************************��ȡ������Ϣ*****************************************//
			Integer showorder = 0;
			Integer noticetype = 0;
			Map<String, Object> orderMap = commonMethods.getOrderInfo(orderid, -1L, curtime + delaytime * 60);
			logger.error("orderMap:"+orderMap);
			if(orderMap != null){
				Long uid = (Long)orderMap.get("uid");
				Double aftertotal = Double.valueOf(orderMap.get("aftertotal") + "");//ʹ�ü���ȯ֮��Ľ��
				Double beforetotal = Double.valueOf(orderMap.get("beforetotal") + "");
				Double distotal = Double.valueOf(orderMap.get("distotal") + "");
				Long comid = (Long)orderMap.get("comid");
				Long end_time = (Long)orderMap.get("end_time");
				Long disTicketId = (Long)orderMap.get("shopTicketId");
				//*******************************�жϳ����Ƿ�֧�ֵ���֧��*****************************//
				Long count = pService.getLong("select count(id) from com_info_tb where id=? " +
						" and epay=? ", new Object[]{comid, 1});
				if(count == 0){//��֧�ֵ���֧��
					showorder = 2;
					request.setAttribute("showorder", showorder);
					return mapping.findForward("toprepaypage");
				}
				//*******************************ѡͣ����ȯ**************************************//
				Double tcbTMoney = 0d;
				Double tcbTLimit = 0d;
				if(ticketId == -1){
					logger.error("choose ticket auto>>>ticketid:"+ticketId+",orderid:"+orderid);
					Map<String, Object> ticketparam = new HashMap<String, Object>();
					ticketparam.put("total", aftertotal + "");
					ticketparam.put("uin", uin);
					ticketparam.put("uid", uid);
					ticketparam.put("orderid", orderid);
					ticketparam.put("bindflag", bindflag);
					ticketparam.put("openid", openid);
					ticketparam.put("mobile", mobile);
					Map<String, Object> tMap = getTicket(ticketparam);
					ticketId = (Long)tMap.get("ticketId");
					tcbTLimit = Double.valueOf(tMap.get("limit") + "");
					tcbTMoney = Double.valueOf(tMap.get("ticket_money") + "");
				}else if(ticketId > 0){
					logger.error("choose ticket unauto>>>ticketid:"+ticketId+",orderid:"+orderid);
					tcbTLimit = publicMethods.getTicketMoney(ticketId, 2, uid, aftertotal, 2, comid, orderid);
				}if(ticketId == -2){
					logger.error("��������ʹ��ȯ>>>ticketid:"+ticketId+",uin:"+uin);
					ticketId = -1L;
				}
				logger.error("orderid:"+orderid+",ticketId:"+ticketId+"tcbTLimit:"+tcbTLimit);
				//****************************�����û���Ҫ����Ľ��*************************//
				Double balance_pay = StringUtils.formatDouble(aftertotal - tcbTLimit);//���֧���Ľ��
				if(balance_pay > balance){//����
					balance_pay = balance;
				}
				Double wx_pay = StringUtils.formatDouble(aftertotal - tcbTLimit - balance_pay);
				logger.error("orderid:"+orderid+"wx_pay:"+wx_pay+",balance_pay:"+balance_pay);
				if(wx_pay > 0){
					try {
						Map<String, Object> attachMap = new HashMap<String, Object>();
						attachMap.put("uid", uid);//�շ�ԱID
						attachMap.put("money", aftertotal);//
						if(paytype == 0){
							attachMap.put("type", 4);//
						}else{
							attachMap.put("type", 5);//
						}
						attachMap.put("ticketId", ticketId);//ͣ��ȯ
						attachMap.put("orderid", orderid);
						//��������
						String attach = StringUtils.createJson(attachMap);
						//����֧������
						SortedMap<Object, Object> signParams = new TreeMap<Object, Object>();
						//��ȡJSAPI��ҳ֧������
						signParams = PayCommonUtil.getPayParams(request.getRemoteAddr(), wx_pay, "ͣ����֧��", attach, openid);
						request.setAttribute("appid", signParams.get("appId"));
						request.setAttribute("nonceStr", signParams.get("nonceStr"));
						request.setAttribute("package", signParams.get("package"));
						request.setAttribute("packagevalue", signParams.get("package"));
						request.setAttribute("timestamp", signParams.get("timeStamp"));
						request.setAttribute("paySign", signParams.get("paySign"));
						request.setAttribute("signType", signParams.get("signType"));
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
				/*============ͣ��ȯ�����߼�begin==============*/
				Integer tickettype = 0;//��ͨȯ
				String ticketdescp = "�޿���ͣ��ȯ";
				if(ticketId > 0){
					Map<String, Object> ticketMap = daService.getMap(
							"select type,resources,money from ticket_tb where id=? ",
							new Object[] { ticketId });
					if(ticketMap != null){
						tickettype = (Integer)ticketMap.get("type");
						Integer resources = (Integer)ticketMap.get("resources");
						tcbTMoney = Double.valueOf(ticketMap.get("money") + "");
						if(tickettype == 0){
							if(resources == 0){
								ticketdescp = "��ѡ��"+tcbTMoney+"Ԫ��ͨȯ";
							}else if(resources == 1){
								ticketdescp = "��ѡ��"+tcbTMoney+"Ԫ����ȯ";
							}
						}else if(tickettype == 1){
							ticketdescp = "��ѡ��"+tcbTMoney+"Ԫר��ȯ";
						}
					}
				}else if(ticketId == -100){
					ticketdescp = "�׵�8��ȯ";
					tickettype = 2;
				}else if(ticketId == -2){
					ticketdescp = "��ʹ��ͣ��ȯ";
				}else{
					if(!memcacheUtils.readUseTicketCache(uin)){
						ticketdescp = "����ÿ��ʹ�ô�������";
					}
				}
				request.setAttribute("notice_type", noticetype);
				request.setAttribute("uid", uid);
				request.setAttribute("mobile", mobile);
				request.setAttribute("ticketid", ticketId);
				request.setAttribute("money", aftertotal);//����֧���Ľ��
				request.setAttribute("tcbtmoney", tcbTMoney);//�Żݲ��ֵĽ��
				request.setAttribute("wx_pay", wx_pay);//΢��֧���Ľ��
				request.setAttribute("delaytime", delaytime);
				request.setAttribute("ticketdescp", ticketdescp);
				request.setAttribute("tickettype", tickettype);
				request.setAttribute("tcbtlimit", tcbTLimit);
				request.setAttribute("balancepay", balance_pay);
				request.setAttribute("otherpay", StringUtils.formatDouble(aftertotal - tcbTLimit));
				request.setAttribute("openid", openid);
			}else{
				showorder = 1;
			}
			
			//��������
			request.setAttribute("orderid", orderid);
			request.setAttribute("paytype", paytype);
			request.setAttribute("showorder", showorder);
			return mapping.findForward("toprepaypage");
		}else if(action.equals("getprice")){//��ȡԤ֧�����
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			Integer delaytime = RequestUtil.getInteger(request, "delaytime", -1);
			logger.error("getprice>>>orderid��"+orderid+",delaytime:"+delaytime);
			if(orderid == -1 || delaytime == -1 ){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map<String, Object> infoMap = new HashMap<String, Object>();
			Long end_time = curtime + delaytime * 60;
			Map<String, Object> map = commonMethods.getOrderInfo(orderid, -1L, end_time);
			infoMap.put("aftertotal", map.get("aftertotal"));
			infoMap.put("parktime", map.get("parktime"));
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			return null;
		}else if(action.equals("prepayorder")){
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			String openid = RequestUtil.processParams(request, "openid");
			Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
			Double total = RequestUtil.getDouble(request, "total", 0d);
			if(orderid == -1 || openid.equals("") || total == 0){
				return mapping.findForward("error");
			}
			//**************************����openid��ȡ�û���Ϣ*******************************//
			Map<String, Object> userinfoMap = commonMethods.getUserinfoByOpenid(openid);
			if(userinfoMap == null || (Long)userinfoMap.get("uin") < 0){
				return mapping.findForward("error");
			}
			Long uin = (Long)userinfoMap.get("uin");
			Integer bindflag = (Integer)userinfoMap.get("bindflag");
			Map orderMap = daService.getMap("select * from order_tb where id=? ", new Object[]{orderid});
			int re = publicMethods.prepay(orderMap, total, uin, ticketId, 0, bindflag, null);
			AjaxUtil.ajaxOutput(response, re + "");
			return null;
		}else if(action.equals("epay")){//ֱ��
			Long uid = RequestUtil.getLong(request, "uid", -1L);// �շ�Աid
			Integer major = RequestUtil.getInteger(request, "major", -1);
			Integer minor = RequestUtil.getInteger(request, "minor", -1);
			Double total = RequestUtil.getDouble(request, "total", 0d);
			logger.error("΢�Ź��ںŽ���ֱ����uid:"+uid);
			if(uid == -1 && major != -1 && minor != -1){//����ҡһҡ
				logger.error("ҡһҡֱ����major:"+major+",minor:"+minor);
				if(major == -1 || minor == -1){
					return mapping.findForward("error");
				}
				Map cominfo = daService.getPojo("select * from area_ibeacon_tb where major=? and minor=? ",new Object[] { major,minor });
				if(cominfo == null){
					return mapping.findForward("error");
				}
				Long pass = (Long) cominfo.get("pass");// ���볡��־
				Map passMap = daService.getMap("select passtype,worksite_id from com_pass_tb where id=?", new Object[]{pass});
				if(passMap == null){
					return mapping.findForward("error");
				}
				Map useWorkSiteMap = daService.getMap("select uin from user_worksite_tb where worksite_id=? ", new Object[]{passMap.get("worksite_id")});
				if(useWorkSiteMap == null){
					request.setAttribute("type", "1");
					return mapping.findForward("error");
				}
				uid = (Long)useWorkSiteMap.get("uin");//�շ�Աid
			
			}
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("")){
				String code = RequestUtil.processParams(request, "code");
				String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
						+ Constants.WXPUBLIC_APPID
						+ "&secret="
						+ Constants.WXPUBLIC_SECRET
						+ "&code="
						+ code
						+ "&grant_type=authorization_code";
				String result = CommonUtil.httpsRequest(access_token_url, "GET",null);
				if(result!=null){
					JSONObject map = JSONObject.fromObject(result);
					if (map!=null&&map.get("errcode") != null) {
						return mapping.findForward("error");
					}
					openid = (String) map.get("openid");
				}
			}
			logger.error("ֱ����openid:"+openid+",uid:"+uid);
			if(uid == -1){
				return mapping.findForward("error");
			}
			try {
				String version = request.getHeader("user-agent");//΢�Ű汾��
				char agent = version.charAt(version.indexOf("MicroMessenger")+15);
				String vsign = "oldversion";
				if(agent >= 5){
					vsign = "newversion";
				}
				request.setAttribute("version", vsign);//΢�Ű汾��
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			request.setAttribute("openid", openid);
			Map<String, Object> uidMap = daService.getMap("select * from user_info_tb where id=? ",
					new Object[] { uid });
			request.setAttribute("nickname", uidMap.get("nickname"));//�շ�Ա����
			if(uidMap == null || uidMap.get("comid") == null){
				return mapping.findForward("error");
			}
			Map<String, Object> comMap = daService.getMap(
					"select * from com_info_tb where id=?",
					new Object[] { uidMap.get("comid") });
			if(comMap == null){
				return mapping.findForward("error");
			}
			Integer city_flag = 0;//0������������ĳ��� 1��������
			if(comMap.get("city") != null){
				Integer city = (Integer)comMap.get("city");
				if(city >= 370100 && city < 370200){//���ϳ���
					logger.error("���ϳ���comid:"+uidMap.get("comid"));
					city_flag = 1;
				}
			}
			request.setAttribute("city_flag", city_flag);//���б�־
			Integer bind_flag = 0;//0��δ���˻� 1:�Ѱ��˻�
			Long uin = -1L;//��ǰ΢�ź��û���uin
			String car_number = null;//���ƺ�
			Map<String, Object> bindMap = daService.getMap(
					"select * from user_info_tb where wxp_openid=? ",
					new Object[] { openid });
			if(bindMap == null){
				Map<String, Object> nobindMap = daService.getMap(
						"select * from wxp_user_tb where openid=? ",
						new Object[] { openid });
				if(nobindMap == null){
					uin = daService.getLong("SELECT nextval('seq_user_info_tb'::REGCLASS) AS newid",null);
					int r = daService.update("insert into wxp_user_tb(openid,create_time,uin) values(?,?,?) ",
									new Object[] { openid, System.currentTimeMillis() / 1000, uin});
				}else{
					if(nobindMap.get("car_number") != null){
						car_number = (String)nobindMap.get("car_number");
					}
					uin = (Long)nobindMap.get("uin");
				}
				logger.error("΢�Ź��ں�δ���˻�,openid:"+openid+",uin:"+uin+",uid:"+uid+",carnumber:"+car_number);
			}else{
				bind_flag = 1;
				uin = (Long)bindMap.get("id");
				Map<String, Object> carMap = daService.getMap("select * from car_info_tb where uin=? and state=? limit ?", new Object[]{uin,1,1});
				if(carMap != null){
					car_number = (String)carMap.get("car_number");
				}
				request.setAttribute("mobile", bindMap.get("mobile"));
				logger.error("΢�Ź��ں��Ѱ��˻�openid:"+openid+",uin:"+uin+",uid:"+uid+",carnumber:"+car_number);
			}
			request.setAttribute("bind_flag", bind_flag);
			request.setAttribute("uin", uin);
			request.setAttribute("uid", uid);
			
			Map<String, Object> orderMap = daService
					.getMap("select * from order_tb where state=? and pay_type=? and end_time>? and uin=? and comid=? and c_type=? order by end_time desc limit ?",
							new Object[] {1,1,System.currentTimeMillis() / 1000 - 15 * 60,uin,comMap.get("id"), 0, 1 });
			if(orderMap != null){
				logger.error("������15����֮���ֽ�������NFC����orderid:"+orderMap.get("id")+",uin:"+uin+",openid:"+openid+",uid:"+uid);
			}else if(car_number != null){
				orderMap = daService.getMap(
						"select * from order_tb where nfc_uuid is null and total is null and car_number=? and state=? and comid=? and (c_type=? or c_type=?) ",
						new Object[] { car_number, 0, uidMap.get("comid"), 2, 3 });
				if(orderMap != null){
					logger.error("����δ���㶩��orderid:"+orderMap.get("id")+",uin:"+uin+",carnumber:"+car_number+",openid:"+openid);
				}else{
					orderMap = daService
							.getMap("select * from order_tb where state=? and pay_type=? and end_time>? and comid=? and (c_type=? or c_type=?) and car_number=? order by end_time desc limit ?",
									new Object[] { 1, 1, System.currentTimeMillis() / 1000 - 15 * 60, comMap.get("id"), 2, 3, car_number, 1 });
					if(orderMap != null){
						logger.error("������15����֮���ֽ����������䳵�ƻ���ͨ��ɨ�ƶ���orderid:"+orderMap.get("id")+",uin:"+uin+",c_type:"+orderMap.get("c_type")+",openid:"+openid);
					}
				}
			}
			if(orderMap != null){
				Long orderId = (Long)orderMap.get("id");
				Map<String, Object> infoMap = commonMethods.getOrderInfo(orderId, -1L, curtime);
				request.setAttribute("orderid", orderId);
				request.setAttribute("uid", uid);
				request.setAttribute("total", infoMap.get("beforetotal"));
				request.setAttribute("start_time", infoMap.get("starttime"));
				request.setAttribute("parktime", infoMap.get("parktime"));
				request.setAttribute("car_number", infoMap.get("car_number"));
				return mapping.findForward("payorder");
			}else if(comMap.get("etc") != null){
				Integer etc = (Integer)comMap.get("etc");
				logger.error("�������ͣ�uin:"+uin+",uid:"+uid+",openid:"+openid+",etc:"+etc);
				if(etc == 2){//ͨ��ɨ��
					request.setAttribute("orderid", -1);
					return mapping.findForward("payorder");
				}
			}
			
			if(total > 0){
				String url = "http://" + Constants.WXPUBLIC_REDIRECTURL
						+ "/zld/wxpfast.do?action=toepaypage&openid=" + openid
						+ "&uid=" + uid + "&fee=" + total;
				response.sendRedirect(url);
			}else{
				return mapping.findForward("epayimport");
			}
		}else if(action.equals("toepaypage")){
			String openid = RequestUtil.processParams(request, "openid");
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			Double money = RequestUtil.getDouble(request, "fee", 0d);
			Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);//ȯid
			Double limit = RequestUtil.getDouble(request, "limit", 0d);//ȯ�ֿ۵�����
			if(openid.equals("") || uid == -1 || money == 0){
				return mapping.findForward("error");
			}
			Long uin = -1L;
			Integer bind_flag = 0;
			String mobile = "";
			Double ticket_money = 0d;
			Double balance = 0d;
			Double discount = 0d;
			/*============��ȡ�û���Ϣ�߼�begin==============*/
			Map<String, Object> userMap = daService.getMap("select * from user_info_tb where wxp_openid=? limit ?", new Object[]{openid,1});
			if(userMap != null){
				bind_flag = 1;
				uin = (Long)userMap.get("id");
				mobile = (String)userMap.get("mobile");
				balance = Double.valueOf(userMap.get("balance") + "");
			}else{
				userMap = daService.getMap("select * from wxp_user_tb where openid=? limit ? ", new Object[]{openid, 1});
				if(userMap != null){
					uin = (Long)userMap.get("uin");
					logger.error("δ�󶨣�uid:"+uid+",uin:"+uin+",openid:"+openid);
				}else{
					logger.error("δ�ҵ��û�:uid:"+uid+",uin:"+uin+",openid:"+openid);
					return mapping.findForward("error");
				}
			}
			/*============��ȡ�û���Ϣ�߼�end==============*/
			/*============ѡ��ͣ��ȯ�߼�begin==============*/
			logger.error("uin:"+uin+",limit:"+limit+",openid:"+openid+",ticketid:"+ticketId);
			if(ticketId == -1){
				logger.error("choose ticket auto>>>ticketid:"+ticketId+",uid:"+uid+",fee:"+money+",openid:"+openid+",uin:"+uin);
				
				Map<String, Object> ticketparam = new HashMap<String, Object>();
				ticketparam.put("mobile", mobile);
				ticketparam.put("total", money + "");
				ticketparam.put("uin", uin);
				ticketparam.put("uid", uid);
				ticketparam.put("bindflag", bind_flag);
				ticketparam.put("openid", openid);
				Map<String, Object> tMap = getTicket(ticketparam);
				ticketId = (Long)tMap.get("ticketId");
				ticket_money = Double.valueOf(tMap.get("ticket_money") + "");
				limit = Double.valueOf(tMap.get("limit") + "");
			}else if(ticketId > 0){
				logger.error("choose ticket unauto>>>ticketid:"+ticketId+",uid:"+uid+",fee:"+money+",openid:"+openid+",uin:"+uin);
				
				Map<String, Object> ticketMap = pService.getMap(
						"select money from ticket_tb where id=? ",
						new Object[] { ticketId });
				if(ticketMap != null){
					ticket_money = Double.valueOf(ticketMap.get("money") + "");
					logger.error("ticketid:"+ticketId+",uin:"+uin+",limit:"+limit+",ticket_money:"+ticket_money);
				}else{//û���ҵ�ȯ����Ϊû����ȯ
					logger.error("û���ҵ�ȯ>>>ticketid:"+ticketId+",uin:"+uin+",limit:"+limit);
					ticketId = -1L;
				}
			}
			discount = ticket_money;
			if(ticketId > 0){
				if(discount > limit){
					discount = limit;
				}
			}
			/*============ѡ��ͣ��ȯ�߼�end==============*/
			/*============ͣ��ȯ�����߼�begin==============*/
			Integer tickettype = 0;//��ͨȯ
			String ticketdescp = "�޿���ͣ��ȯ";
			if(ticketId > 0){
				Map<String, Object> ticketMap = daService.getMap(
						"select type,resources from ticket_tb where id=? ",
						new Object[] { ticketId });
				if(ticketMap != null){
					tickettype = (Integer)ticketMap.get("type");
					Integer resources = (Integer)ticketMap.get("resources");
					if(tickettype == 0){
						if(resources == 0){
							ticketdescp = "��ѡ��"+ticket_money+"Ԫ��ͨȯ";
						}else if(resources == 1){
							ticketdescp = "��ѡ��"+ticket_money+"Ԫ����ȯ";
						}
					}else if(tickettype == 1){
						ticketdescp = "��ѡ��"+ticket_money+"Ԫר��ȯ";
					}
				}
			}else if(ticketId == -100){
				ticketdescp = "�׵�8��ȯ";
				tickettype = 2;
			}else if(ticketId == -2){
				ticketdescp = "��ʹ��ͣ��ȯ";
			}else{
				if(!memcacheUtils.readUseTicketCache(uin)){
					ticketdescp = "����ÿ��ʹ�ô�������";
				}
			}
			/*============ͣ��ȯ�����߼�end==============*/
			String chooseurl = "#";
			if(bind_flag == 1 && (ticketId > 0 || ticketId == -2)){
				chooseurl = "wxpfast.do?action=tochooseticket&openid="+openid+"&ticketid="+ticketId+"&mobile="+mobile+"&total="+money+"&uid="+uid;
			}
			/*============����΢��֧���߼�begin==============*/
			money = StringUtils.formatDouble(money);//ȡǰ��λС��
			Double balance_pay = 0d;//���֧���Ľ��
			if(tickettype != 2){//���������
				logger.error("�Ǵ���ȯ���������ticketid:"+ticketId+",openid:"+openid+",ticket_money:"+ticket_money+",uid:"+uid+",balance:"+balance+",discount:"+discount);
				if(money >= discount + balance){
					balance_pay = balance;//���ȫ������֧��
				}else if(discount < money){//ͣ��ȯ������֧�����
					balance_pay = StringUtils.formatDouble(money - discount);
				}
			}
			Double wx_pay = StringUtils.formatDouble(money - discount - balance_pay);
			logger.error("wx_pay:"+wx_pay+",openid:"+openid+",uid:"+uid+",uin:"+uin+",discount:"+discount+",balance_pay:"+balance_pay+",ticket_money:"+ticket_money+",balance:"+balance);
			if(wx_pay > 0){
				try {
					if(ticketId == -2){
						logger.error("��������ʹ��ȯ>>>ticketid:"+ticketId+",uin:"+uin);
						ticketId = -1L;
					}
					Map<String, Object> attachMap = new HashMap<String, Object>();
					attachMap.put("uid", uid);//�շ�ԱID
					attachMap.put("money", money);//ֱ�����
					attachMap.put("type", 0);//ֱ��ͣ����
					attachMap.put("ticketId", ticketId);//ͣ��ȯ
					//��������
					String attach = StringUtils.createJson(attachMap);
					//����֧������
					SortedMap<Object, Object> signParams = new TreeMap<Object, Object>();
					//��ȡJSAPI��ҳ֧������
					signParams = PayCommonUtil.getPayParams(request.getRemoteAddr(), wx_pay, "ͣ����֧��", attach, openid);
					request.setAttribute("appid", signParams.get("appId"));
					request.setAttribute("nonceStr", signParams.get("nonceStr"));
					request.setAttribute("package", signParams.get("package"));
					request.setAttribute("packagevalue", signParams.get("package"));
					request.setAttribute("timestamp", signParams.get("timeStamp"));
					request.setAttribute("paySign", signParams.get("paySign"));
					request.setAttribute("signType", signParams.get("signType"));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
			}
			/*============����΢��֧���߼�end==============*/
			
			request.setAttribute("chooseurl", chooseurl);
			request.setAttribute("bind_flag", bind_flag);
			request.setAttribute("wx_pay", wx_pay);
			request.setAttribute("balance_pay", balance_pay);
			request.setAttribute("ticket_money", ticket_money);
			request.setAttribute("discount", discount);
			request.setAttribute("ticketid", ticketId);
			request.setAttribute("total", money);
			request.setAttribute("uid", uid);
			request.setAttribute("openid", openid);
			request.setAttribute("needpay", StringUtils.formatDouble(wx_pay + balance_pay));
			request.setAttribute("tickettype", tickettype);
			request.setAttribute("ticketdescp", ticketdescp);
			request.setAttribute("mobile", mobile);
			return mapping.findForward("toepay");
		}else if(action.equals("checkorder")){
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			logger.error("���ö����Ƿ��Ѿ����㣬orderid:"+orderid);
			if(orderid == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				logger.error("�ö��������ڣ�orderid��"+orderid);
				return null;
			}
			Long count = daService.getLong(
					"select count(*) from order_tb where state=? and id=? ",
					new Object[] { 1, orderid });
			if(count > 0){
				logger.error("�ö����ѽ��㣬orderid��"+orderid);
				Map orderMap = daService.getMap("select * from order_tb where end_time>? and pay_type=? and (c_type=? or c_type=? or c_type=?) and id=? ",
								new Object[] {curtime - 15*60, 1, 2, 3, 0, orderid });
				if(orderMap != null){
					AjaxUtil.ajaxOutput(response, "-3");
				}else{
					AjaxUtil.ajaxOutput(response, "-2");
				}
			}else{
				logger.error("�ö���δ���㣬orderid��"+orderid);
				AjaxUtil.ajaxOutput(response, "1");
			}
			return null;
		}else if(action.equals("sweepcom")){
			Long codeid = RequestUtil.getLong(request, "codeid", -1L);
			String openid = RequestUtil.processParams(request, "openid");
			String carnumber = RequestUtil.processParams(request, "carnumber");
			
//			openid = "oRoektybTsv33_vSKKUwLAsJAquc";
//			openid = "oRoekt8iW7wwwv2CVq3Bj6rB38EM";
//			openid = "oRoekt7uy9abm5hrUBCWYHHDF5sY";
			
			if(openid.equals("")){
				String code = RequestUtil.processParams(request, "code");
				String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXPUBLIC_APPID+"&secret="+Constants.WXPUBLIC_SECRET+"&code="+code+"&grant_type=authorization_code";
				String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
				JSONObject map = JSONObject.fromObject(result);
				if(map == null || map.get("errcode") != null){
					logger.error(">>>>>>>>>>>>��ȡopenidʧ��....,���»�ȡ>>>>>>>>>>>");
					String redirect_url = "http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpfast.do%3faction%3dsweepcom%26codeid%3d"+codeid;
					String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
							+ Constants.WXPUBLIC_APPID
							+ "&redirect_uri="
							+ redirect_url
							+ "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
					response.sendRedirect(url);
					return null;
				}
				openid = (String)map.get("openid");
			}
			
			logger.error("ɨ������ά����߼���ȯ��openid��"+openid+",codeid:"+codeid);
			if(openid.equals("") || codeid == -1){
				return mapping.findForward("error");
			}
			request.setAttribute("codeid", codeid);
			request.setAttribute("openid", openid);
			//**************************���ݶ�ά���ȡ������Ϣ*******************************//
			Map<String, Object> codeMap = daService.getMap("select * from qr_code_tb where id=? and comid is not null ",
					new Object[] { codeid });
			if(codeMap == null){
				return mapping.findForward("error");
			}
			Long comid = (Long)codeMap.get("comid");
			//**************************����openid��ȡ�û���Ϣ*******************************//
			Map<String, Object> userinfoMap = commonMethods.getUserinfoByOpenid(openid);
			if(userinfoMap == null || (Long)userinfoMap.get("uin") < 0){
				return mapping.findForward("error");
			}
			Long uin = (Long)userinfoMap.get("uin");
			Integer bindflag = (Integer)userinfoMap.get("bindflag");
			//**************************������ƺ�Ϊ�գ����Ұ󶨵ĳ��ƺ�*******************************//
			if(carnumber.equals("")){//���ƺ�Ϊ��
				if(bindflag == 0){//��ʱ�˻�
					Map<String, Object> carMap = daService.getMap("select car_number from wxp_user_tb where uin= ? limit ? ", 
							new Object[]{uin, 1});
					if(carMap != null && carMap.get("car_number") != null){
						carnumber = (String)carMap.get("car_number");
					}
				}else if(bindflag == 1){
					Map<String, Object> carMap = daService.getMap("select car_number from car_info_tb where uin=? and state=? limit ?",
							new Object[] { uin, 1, 1 });
					if(carMap != null && carMap.get("car_number") != null){
						carnumber = (String)carMap.get("car_number");
					}
				}
			}
			if(carnumber.equals("")){
				request.setAttribute("redircturl", "wxpfast.do?action=sweepcom");
				return mapping.findForward("addcarnum");
			}
			//***********************�ܹ�ȡ�����ƺţ����ݳ��ƺŲ�Ѱ��������ȡ��Ϣ****************************//
			Long orderId = -1L;
			Long shopTicketId = -1L;
			Integer ownerflag = 0;
			if((Integer)codeMap.get("type") == 5 && codeMap.get("ticketid") != null){//ɨ�����ȯ��ά��
				shopTicketId = (Long)codeMap.get("ticketid");
			}
			Map<String, Object> orderMap = pService.getMap("select id,total from order_tb where comid=? and car_number=? and state=? order by create_time desc limit ? ", 
					new Object[]{comid, carnumber, 0, 1});
			if(orderMap != null){
				if(orderMap.get("uin") != null && (Long)orderMap.get("uin") > 0){
					Long orderowner = (Long)orderMap.get("uin");
					if(orderowner.intValue() != uin.intValue()){
						ownerflag = 1;
					}
				}
				if(ownerflag == 0){
					orderId = (Long)orderMap.get("id");
					Long end_time = curtime + 15*60;
					if(orderMap.get("total") != null){
						Double pretotal = Double.valueOf(orderMap.get("total") + "");
						if(pretotal > 0){
							end_time = curtime;
						}
					}
					
					Map<String, Object> map = commonMethods.getOrderInfo(orderId, shopTicketId, end_time);
					logger.error("orderid:"+orderId+",map:"+map);
					String descp = "";
					Double pretotal = Double.valueOf(map.get("pretotal") + "");
					Integer prestate = 0;//Ԥ֧��״̬ 0��û��Ԥ֧�� 1���Ѿ�Ԥ֧��
					if(pretotal > 0){
						prestate = 1;
					}
					if(map.get("shopticketid") != null){
						shopTicketId = (Long)map.get("shopticketid");
					}
					Integer ticketstate =  (Integer)map.get("ticketstate");
					Integer tickettype = (Integer)map.get("tickettype");
					Integer tickettime = (Integer)map.get("tickettime");
					Double beforetotal = Double.valueOf(map.get("beforetotal") + "");
					Double aftertotal = Double.valueOf(map.get("aftertotal") + "");
					if(ticketstate == 1){
						if(tickettype == 3){
							descp = tickettime + "Сʱ";
						}else if(tickettype == 4){
							descp = "���";
						}
					}else if(ticketstate == 0){
						descp = "��ȯ��ʹ��";
					}
					request.setAttribute("starttime", map.get("starttime"));
					request.setAttribute("parktime", map.get("parktime"));
					request.setAttribute("beforetotal", map.get("beforetotal"));
					request.setAttribute("aftertotal", map.get("aftertotal"));
					request.setAttribute("distotal", StringUtils.formatDouble(beforetotal - aftertotal));
					request.setAttribute("prestate", prestate);
					request.setAttribute("pretotal", pretotal);
					request.setAttribute("descp", descp);
				}
			}
			request.setAttribute("swpcomflag", 1);
			request.setAttribute("carnumber", carnumber);
			request.setAttribute("orderid", orderId);
			request.setAttribute("shopticketid", shopTicketId);
			return mapping.findForward("prepay");
		}else if(action.equals("toaddcnum")){
			Long codeid = RequestUtil.getLong(request, "codeid", -1L);
			String openid = RequestUtil.processParams(request, "openid");
			if(openid.equals("") || codeid == -1){
				return mapping.findForward("error");
			}
			request.setAttribute("codeid", codeid);
			request.setAttribute("openid", openid);
			request.setAttribute("redircturl", "wxpfast.do?action=sweepcom");
			return mapping.findForward("addcarnum");
		}else if(action.equals("addcnum")){
			String openid = RequestUtil.processParams(request, "openid");
			String carnumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "carnumber"));
			if(openid.equals("") || carnumber.equals("")){
				return mapping.findForward("error");
			}
			Map<String, Object> userinfoMap = commonMethods.getUserinfoByOpenid(openid);
			if(userinfoMap == null || (Long)userinfoMap.get("uin") < 0){
				return mapping.findForward("error");
			}
			Long uin = (Long)userinfoMap.get("uin");
			Integer result = commonMethods.addCarnumber(uin, carnumber);
			AjaxUtil.ajaxOutput(response, result + "");
			return null;
		}else if(action.equals("toreward")){
			String openid = RequestUtil.processParams(request, "openid");
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			
			if(openid.equals("") || uid == -1 || orderid == -1){
				return mapping.findForward("error");
			}
			Map<String, Object> uidMap = pService.getMap(
					"select nickname from user_info_tb where id=? ",
					new Object[] { uid });
			if(uidMap != null){
				Long uin = -1L;
				Map<String, Object> userMap = daService.getMap(
						"select * from user_info_tb where wxp_openid=? ",
						new Object[] { openid });
				if(userMap != null){
					uin = (Long)userMap.get("id");
				}else{
					userMap = daService.getMap("select uin from wxp_user_tb where openid=? ", new Object[]{openid});
					if(userMap != null){
						uin = (Long)userMap.get("uin");
					}
				}
				
				Integer reward_flag = 0;//0:û�д����� 1���ö����Ѵ���
				Long count = daService.getLong(
						"select count(id) from parkuser_reward_tb where uin=? and order_id=? ",
						new Object[] { uin, orderid });
				if(count > 0){
					reward_flag = 1;
				}
				if(reward_flag == 0){
					Long btime = TimeTools.getToDayBeginTime();
					count = daService.getLong("select count(*) from parkuser_reward_tb where uin=? and ctime>=? and uid=? ",
									new Object[] { uin, btime, uid });
					if(count > 0){
						reward_flag = 2;
					}
				}
				request.setAttribute("openid", openid);
				request.setAttribute("uid", uid);
				request.setAttribute("orderid", orderid);
				request.setAttribute("nickname", uidMap.get("nickname"));
				request.setAttribute("reward_flag", reward_flag);
				return mapping.findForward("toreward");
			}else{
				return mapping.findForward("error");
			}
			//http://127.0.0.1/zld/wxpfast.do?action=toreward&uid=10700&orderid=786567&openid=oRoektybTsv33_vSKKUwLAsJAquc
		}else if(action.equals("reward")){
			String openid = RequestUtil.processParams(request, "openid");
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			Double total = RequestUtil.getDouble(request, "total", 2d);
			if(openid.equals("") || orderid == -1 || uid == -1){
				return mapping.findForward("error");
			}
			
			Map<String, Object> userMap = daService.getMap(
					"select * from user_info_tb where wxp_openid=? ",
					new Object[] { openid });
			Map<String, Object> attachMap = new HashMap<String, Object>();
			Double balance_pay = 0d;//���֧���Ľ��
			Double wx_pay = 0d;//΢��֧���Ľ��
			Long ticketid = -1L;
			Double ticketmoney = 0d;
			Double distotal = 0d;
			Long uin = -1L;
			Integer reward_flag = 0;//0:û�д����� 1���ö����Ѵ���
			String descp = "�޿���ͣ��ȯ";
			if(userMap != null){
				uin = (Long)userMap.get("id");
				Double balance = Double.valueOf(userMap.get("balance") + "");//�û����
				Map<String, String> params = new HashMap<String, String>();
				params.put("mobile", userMap.get("mobile") + "");
				params.put("action", "getaccount");
				params.put("total", total + "");
				params.put("uid", uid + "");
				params.put("ptype", 4 + "");
				params.put("utype", "2");
				params.put("source", "1");//����΢�Ź��ں�ѡȯ
				params.put("orderid", orderid + "");
				String result = new HttpProxy().doPost("http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/carowner.do", params);
				logger.error("reward>>>>>ѡȯ���:tickets:"+result+",orderid:"+orderid+",uin:"+userMap.get("id")+",uid:"+uid);
				if(result.equals("")){
					return mapping.findForward("error");
				}
				JSONObject jsonObject = JSONObject.fromObject(result);
				JSONArray jsonArray = JSONArray.fromObject(jsonObject.getString("tickets"));
				if(jsonArray.size() != 0){
					JSONObject jsonObject2 = (JSONObject)jsonArray.get(0);
					ticketid = jsonObject2.getLong("id");
					ticketmoney = jsonObject2.getDouble("money");
					distotal = jsonObject2.getDouble("limit");
					if(distotal > ticketmoney){
						distotal = ticketmoney;
					}
					if(distotal > total){//���ֿ�2Ԫ
						distotal = total;
					}
					Integer resources = jsonObject2.getInt("resources");
					if(resources == 0){
						descp = "��ѡ��"+ticketmoney+"Ԫ��ͨȯ";
					}else if(resources == 1){
						descp = "��ѡ��"+ticketmoney+"Ԫ����ȯ";
					}
				}
				if(total > distotal + balance){
					balance_pay = balance;//���ȫ������֧��
					wx_pay = StringUtils.formatDouble(total - distotal - balance);
				}else if(total > distotal){
					balance_pay = StringUtils.formatDouble(total - distotal);
				}
				
				request.setAttribute("mobile", userMap.get("mobile"));
			}else{
				wx_pay = StringUtils.formatDouble(total);
				
				userMap = daService.getMap("select uin from wxp_user_tb where openid=? ", new Object[]{openid});
				if(userMap != null){
					uin = (Long)userMap.get("uin");
				}
			}
			if(wx_pay > 0){
				attachMap.put("uid", uid);//�շ�ԱID
				attachMap.put("money", total);//���ͽ��
				attachMap.put("type", 6);//����
				attachMap.put("ticketId", ticketid);
				attachMap.put("orderid", orderid);
				
				//��������
				String attach = StringUtils.createJson(attachMap);
				//����֧������
				SortedMap<Object, Object> signParams = new TreeMap<Object, Object>();
				//��ȡJSAPI��ҳ֧������
				signParams = PayCommonUtil.getPayParams(request.getRemoteAddr(), wx_pay, "�����շ�Ա", attach, openid);
				request.setAttribute("appid", signParams.get("appId"));
				request.setAttribute("nonceStr", signParams.get("nonceStr"));
				request.setAttribute("package", signParams.get("package"));
				request.setAttribute("packagevalue", signParams.get("package"));
				request.setAttribute("timestamp", signParams.get("timeStamp"));
				request.setAttribute("paySign", signParams.get("paySign"));
				request.setAttribute("signType", signParams.get("signType"));
			}
			
			Long count = daService.getLong(
					"select count(id) from parkuser_reward_tb where uin=? and order_id=? ",
					new Object[] { uin, orderid });
			if(count > 0){
				reward_flag = 1;
			}
			if(reward_flag == 0){
				Long btime = TimeTools.getToDayBeginTime();
				count = daService.getLong("select count(*) from parkuser_reward_tb where uin=? and ctime>=? and uid=? ",
								new Object[] { uin, btime, uid });
				if(count > 0){
					reward_flag = 2;
				}
			}
			
			request.setAttribute("wx_pay", wx_pay);
			request.setAttribute("balance_pay", balance_pay);
			request.setAttribute("ticketid", ticketid);
			request.setAttribute("total", total);
			request.setAttribute("uid", uid);
			request.setAttribute("openid", openid);
			request.setAttribute("orderid", orderid);
			request.setAttribute("reward_flag", reward_flag);
			request.setAttribute("distotal", distotal);
			request.setAttribute("ticket_money", ticketmoney);
			request.setAttribute("needpay", StringUtils.formatDouble(balance_pay + wx_pay));
			request.setAttribute("descp", descp);
			return mapping.findForward("reward");
			//http://127.0.0.1/zld/wxpfast.do?action=reward
		}else if(action.equals("sweepticket")){
			Long codeid = RequestUtil.getLong(request, "codeid", -1L);
			String code = RequestUtil.processParams(request, "code");
			Integer type = RequestUtil.getInteger(request, "type", 0);//0��ȡ��ʱ��۵����� 1����ȡ��ʱ�򲻿ۻ��֣���֮ǰ��
			String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXPUBLIC_APPID+"&secret="+Constants.WXPUBLIC_SECRET+"&code="+code+"&grant_type=authorization_code";
			String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
			JSONObject map = JSONObject.fromObject(result);
			if(map == null || map.get("errcode") != null){
				logger.error(">>>>>>>>>>>>��ȡopenidʧ��....,���»�ȡ>>>>>>>>>>>");
				String redirect_url = "http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpfast.do%3faction%3dsweepticket%26codeid%3d"+codeid;
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
						+ Constants.WXPUBLIC_APPID
						+ "&redirect_uri="
						+ redirect_url
						+ "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				response.sendRedirect(url);
				return null;
			}
			String openid = (String)map.get("openid");
//			String openid = "oRoekt7uy9abm5hrUBCWYHHDF5sY";
			
			logger.error("ɨ����ר��ȯ>>>openid:"+openid+",codeid:"+codeid+",type:"+type);
			Long uin = -1L;
			Integer codeflag = 1;//0��Ч 1��ʧЧ 2:�ѱ���ȡ
			if(!openid.equals("") && codeid != -1){
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpaccount.do&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				Map<String, Object> userMap = daService.getMap("select id from user_info_tb where wxp_openid=? limit ? ", new Object[]{openid, 1});
				if(userMap != null){
					uin = (Long)userMap.get("id");
					url = "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/wxpaccount.do?action=toticketpage&openid="+openid;
				}else{
					userMap = daService.getMap("select uin from wxp_user_tb where openid=? limit ? ", new Object[]{openid, 1});
					if(userMap != null){
						uin = (Long)userMap.get("uin");
					}else{
						uin = daService.getLong("SELECT nextval('seq_user_info_tb'::REGCLASS) AS newid",null);
						int r = daService.update("insert into wxp_user_tb(openid,create_time,uin) values(?,?,?) ",
										new Object[] { openid, System.currentTimeMillis() / 1000, uin});
						logger.error("ɨ����ר��ȯ��û�а󶨣�û�������˻�������һ��uin:"+uin+",openid:"+openid);
					}
				}
				request.setAttribute("url", url);
				logger.error("ɨ����ר��ȯ>>>openid:"+openid+",codeid:"+codeid+",uin:"+uin+",type:"+type);
				if(uin != -1){
					Map<String, Object> codeMap = daService
							.getMap("select * from qr_code_tb where id=? and type=? and state=? and ticketid is not null and comid is not null and uid is not null ",
									new Object[] { codeid, 6, 0 });
					String carnumber = publicMethods.getCarNumber(uin);
					if(codeMap != null){
						Long ticketid = (Long)codeMap.get("ticketid");
						Map<String, Object> ticketMap = daService.getMap("select * from ticket_tb where id=? and limit_day>? and state=? and type=? and uin is null",
										new Object[] { ticketid, System.currentTimeMillis() / 1000, 0, 1 });
						logger.error("ɨ����ר��ȯ>>>openid:"+openid+",codeid:"+codeid+",uin:"+uin+",ticketid:"+ticketid);
						if(ticketMap != null){
							Double score = Double.valueOf(codeMap.get("score") + "");
							Long uid = (Long)codeMap.get("uid");
							Long comid = (Long)codeMap.get("comid");
							Map<String, Object> uidMap = daService.getMap("select nickname,reward_score from user_info_tb where id=? ", new Object[]{uid});
							Map<String, Object> comMap = daService.getMap("select company_name from com_info_tb where id=? ", new Object[]{comid});
							request.setAttribute("nickname", uidMap.get("nickname"));
							request.setAttribute("cname", comMap.get("company_name"));
							request.setAttribute("uid", uid);
							Double ticket_money = Double.valueOf(ticketMap.get("money") + "");
							request.setAttribute("ticket_money", ticket_money);
							
							Double reward_score = Double.valueOf(uidMap.get("reward_score") +"");
							logger.error("ɨ����ר��ȯ>>>openid:"+openid+",codeid:"+codeid+",uin:"+uin+",ticketid:"+ticketid+",ʣ��score:"+reward_score+"�������Ļ���score:"+score);
							if(reward_score >= score || type == 1){//type=0�����������
								List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
								//��ά��
								Map<String, Object> codeSqlMap = new HashMap<String, Object>();
								
								Map<String, Object> ticketSqlMap = new HashMap<String, Object>();
								
								codeSqlMap.put("sql", "update qr_code_tb set state=? where id=? ");
								codeSqlMap.put("values", new Object[] { 1, codeid });
								bathSql.add(codeSqlMap);
								
								ticketSqlMap.put("sql", "update ticket_tb set uin=? where id=? ");
								ticketSqlMap.put("values", new Object[] { uin, ticketid});
								bathSql.add(ticketSqlMap);
								if(type == 0){
									Map<String, Object> scoreSqlMap = new HashMap<String, Object>();
									//������ϸ
									Map<String, Object> scoreAccountSqlMap = new HashMap<String, Object>();
									
									scoreAccountSqlMap.put("sql", "insert into reward_account_tb (uin,score,type,create_time,remark,target,ticket_id) values(?,?,?,?,?,?,?)");
									scoreAccountSqlMap.put("values", new Object[]{uid,score,1,curtime,"ͣ��ȯ ɨ��",2,ticketid});
									bathSql.add(scoreAccountSqlMap);
									
									scoreSqlMap.put("sql", "update user_info_tb set reward_score=reward_score-? where id=? ");
									scoreSqlMap.put("values", new Object[]{score, uid});
									bathSql.add(scoreSqlMap);
								}
								boolean b = daService.bathUpdate(bathSql);
								logger.error("ɨ����ר��ȯ>>>openid:"+openid+",codeid:"+codeid+",uin:"+uin+",uid:"+uid+",comid:"+comid+",b:"+b);
								if(b){
									codeflag = 0;
									Map<String, Object> infoMap = new HashMap<String, Object>();
									infoMap.put("uin", uid);
									infoMap.put("score", score);
									infoMap.put("tmoney", ticket_money);
									infoMap.put("carnumber", carnumber);
									if(type == 0){//type =0��ʾ�۳����֣�����Ϣ
										logService.insertParkUserMesg(7, infoMap);
									}
								}
							}else{
								logger.error("ɨ����ר��ȯ>>>�շ�Ա���ֲ���openid:"+openid+",codeid:"+codeid+",uin:"+uin+",ticketid:"+ticketid+",ʣ��score:"+reward_score+"�������Ļ���score:"+score);
							}
						}
					}else{
						logger.error("ɨ����ר��ȯ>>>openid:"+openid+",codeid:"+codeid+",uin:"+uin+",��ά��ʧЧ");
					}
				}
			}
			request.setAttribute("codeflag", codeflag);
			return mapping.findForward("getticket");
		}else if(action.equals("tochooseticket")){
			String openid = RequestUtil.processParams(request, "openid");
			Long ticketid = RequestUtil.getLong(request, "ticketid", -1L);
			String mobile = RequestUtil.processParams(request, "mobile");
			Double total = RequestUtil.getDouble(request, "total", 0d);
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			if(openid.equals("") || mobile.equals("") ||ticketid == -1 || total ==0 ){
				return mapping.findForward("error");
			}
			request.setAttribute("openid", openid);
			request.setAttribute("ticketid", ticketid);
			request.setAttribute("mobile", mobile);
			request.setAttribute("total", total);
			request.setAttribute("uid", uid);
			return mapping.findForward("chooseticket");
		}else if(action.equals("sweepspace")){
			Long codeid = RequestUtil.getLong(request, "codeid", -1L);
			String code = RequestUtil.processParams(request, "code");
			String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXPUBLIC_APPID+"&secret="+Constants.WXPUBLIC_SECRET+"&code="+code+"&grant_type=authorization_code";
			String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
			JSONObject map = JSONObject.fromObject(result);
			if(map == null || map.get("errcode") != null){
				logger.error(">>>>>>>>>>>>��ȡopenidʧ��....,���»�ȡ>>>>>>>>>>>");
				String redirect_url = "http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpfast.do%3faction%3dsweepspace%26codeid%3d"+codeid;
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
						+ Constants.WXPUBLIC_APPID
						+ "&redirect_uri="
						+ redirect_url
						+ "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				response.sendRedirect(url);
				return null;
			}
			String openid = (String)map.get("openid");
			
			/*String openid = "oRoektybTsv33_vSKKUwLAsJAquc";
			codeid = 100664L;*/
			
			request.setAttribute("openid", openid);
			request.setAttribute("codeid", codeid);
			Map<String, Object> userMap = commonMethods.getUserinfoByOpenid(openid);
			Long uin = (Long)userMap.get("uin");
			logger.error("sweep parking spot>>>uin:"+uin+",codeid:"+codeid+",openid:"+openid);
			request.setAttribute("uin", uin);
			
			Map<String, Object> parkspaceMap = daService.getMap(
					"select * from com_park_tb where qid=? limit ? ",
					new Object[] { codeid, 1 });
			
			Integer state = 0;//0���� 1��ռ�� 2����Ч
			if(parkspaceMap == null){
				state = 2;
				request.setAttribute("state", state);
				logger.error("û�и��ݸö�ά���ҵ���λ��Ϣ>>>uin:"+uin+",codeid:"+codeid+",openid:"+openid);
			}else{
				state = (Integer)parkspaceMap.get("state");
				Long comid = (Long)parkspaceMap.get("comid");
				request.setAttribute("spaceid", parkspaceMap.get("cid"));
				
				Map<String, Object> comMap = daService.getMap(
						"select company_name from com_info_tb where id=? ",
						new Object[] { comid });
				if(comMap != null){
					request.setAttribute("cname", comMap.get("company_name"));
				}
				state = checkLot((Long)parkspaceMap.get("id"));
				Long orderId = -1L;
				if(state == 1 && parkspaceMap.get("order_id") != null){//��ռ��
					orderId = (Long)parkspaceMap.get("order_id");
				}
				if(orderId == null || orderId <= 0){
					Map<String, Object> orderMap = daService.getMap("select * from order_tb where state=? and uin=? and comid=? ",
							new Object[] { 0, uin, comid });//û�и��ݲ�λ��ȡ��δ����Ķ��������ݳ�������ټ��һ����û��δ���㶩��
					if(orderMap != null){
						orderId = (Long)orderMap.get("id");
					}
				}
				if(orderId != null && orderId > 0){
					Map<String, Object> orderMap = daService.getMap("select * from order_tb where state=? and id=? ",
							new Object[] { 0, orderId });
					if(orderMap.get("uin") != null && (Long)orderMap.get("uin") != -1){
						Long onuin = (Long)orderMap.get("uin");
						logger.error("uin:"+uin+",codeid:"+codeid+",openid:"+openid+",orderid:"+orderId+",onuin:"+onuin);
						if(onuin.intValue() == uin.intValue()){
							Map<String, Object> infoMap = commonMethods.getOrderInfo(orderId, -1L, curtime);
							request.setAttribute("orderid", orderId);
							request.setAttribute("uid", infoMap.get("uid"));
							request.setAttribute("total", infoMap.get("beforetotal"));
							request.setAttribute("start_time", infoMap.get("starttime"));
							request.setAttribute("parktime", infoMap.get("parktime"));
							request.setAttribute("car_number", infoMap.get("car_number"));
							return mapping.findForward("payorder");
						}
					}
				}
			}
			request.setAttribute("state", state);
			return mapping.findForward("createorder");
			//http://192.168.199.208/zld/wxpfast.do?action=sweepspace
		}else if(action.equals("createorder")){
			String openid = RequestUtil.processParams(request, "openid");
			Long codeid = RequestUtil.getLong(request, "codeid", -1L);
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			String carnumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "carnumber")).toUpperCase().trim();
			if(openid.equals("") || codeid == -1 || uid == -1){
				return mapping.findForward("error");
			}
			request.setAttribute("openid", openid);
			request.setAttribute("codeid", codeid);
			request.setAttribute("uid", uid);
			
			Map<String, Object> userMap = commonMethods.getUserinfoByOpenid(openid);
			Long uin = (Long)userMap.get("uin");
			Integer bindflag = (Integer)userMap.get("bindflag");
			logger.error("sweepspace createorder>>>openid:"+openid+",uin:"+uin+",codeid:"+codeid+",uid:"+uid+",carnumber:"+carnumber);
			if(carnumber.equals("")){
				carnumber = commonMethods.getCarnumber(uin, bindflag);
			}
			if(carnumber.equals("")){
				request.setAttribute("redircturl", "wxpfast.do?action=createorder");
				return mapping.findForward("addcarnum");
			}
			
			logger.error("create order by sweep parking space>>>uin:"+uin+",codeid:"+codeid);
			Map<String, Object> parkspaceMap = pService.getMap(
					"select * from com_park_tb where qid=? limit ? ",
					new Object[] { codeid, 1 });
			if(parkspaceMap == null){
				return mapping.findForward("error");
			}
			Integer state = (Integer)parkspaceMap.get("state");
			if(state == 1){//�ó�λ�Ѿ�������ռ��
				logger.error("�ó�λ��ά���ѱ�ռ��>>>uin:"+uin+",codeid:"+codeid);
				request.setAttribute("state", state);
				return mapping.findForward("createorder");
			}
			Long berthId = (Long)parkspaceMap.get("id");
			Long berthSegId = (Long)parkspaceMap.get("berthsec_id");
			//Long uid = commonMethods.getWorkingCollector(berthId);
			logger.error("berthId:"+berthId+",berthSegId:"+berthSegId+",openid:"+openid+",uid:"+uid);
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���¶���״̬���շѳɹ�
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			//���³�λ��ά��
			Map<String, Object> spaceSqlMap = new HashMap<String, Object>();
			
			Long orderId = daService.getkey("seq_order_tb");
			spaceSqlMap.put("sql", "update com_park_tb set state =?,order_id=? where qid=?");
			spaceSqlMap.put("values", new Object[]{1, orderId, codeid});
			bathSql.add(spaceSqlMap);
			//��ȡcar_type���ڼƼۣ������ֳ���
			Long comid = (Long)parkspaceMap.get("comid");
			if(!carnumber.equals("")){//������ĳ��ƺ�
				Map carMap = daService.getMap("select * from car_info_tb where car_number=? and state=? ", new Object[]{carnumber,1});
				if(carMap!=null&&carMap.get("uin")!=null)
					uin = (Long)carMap.get("uin");
			}else {
				carnumber=null;
			}
			Integer car_type = commonMethods.getCarType(carnumber, comid);
			orderSqlMap.put("sql", "insert into order_tb(id,create_time,uin,comid,c_type,car_type,uid,state,car_number,berthnumber,berthsec_id) values(?,?,?,?,?,?,?,?,?,?,?)");
			orderSqlMap.put("values", new Object[]{orderId,curtime,uin,comid,6,car_type,uid,0,carnumber, berthId, berthSegId});
			bathSql.add(orderSqlMap);
			
			boolean b = daService.bathUpdate(bathSql);
			logger.error("baohe redirect:orderId"+orderId+",uin:"+uin+",b:"+b);
			if(b){
				//�����Ͷ���֪ͨ
				logger.error("baohe redirect comin:orderId"+orderId+",uin:"+uin+",b:"+b);
				try {
					logger.error("baohe redirect comin try catch:orderId"+orderId+",uin:"+uin+",b:"+b);
					/*String date = TimeTools.getTime_yyyyMMdd_HHmmss(curtime*1000);
					date = AjaxUtil.encodeUTF8(date);
					date = AjaxUtil.encodeUTF8(date);*/
					carnumber = AjaxUtil.encodeUTF8(carnumber);
					String url = "http://www.bouwa.org/api/services/p4/Business/AppTingCheBaoInsertCarIn?EmployeeNumber="+uid+"&PlateNumber="+carnumber+"&ElectronicOrderid="+orderId+"&BerthNumber="+parkspaceMap.get("cid")+"&CarType=1&CarInTime=2016";
					logger.error("baohe redirect comin url:orderId"+orderId+",uin:"+uin+",url:"+url);
					logger.error("baohe redirect:orderId"+orderId+",uid:"+uid+",carnumber:"+carnumber+",orderId:"+orderId+",cid:"+parkspaceMap.get("cid"));
					String result = new HttpProxy().doGet(url);
					logger.error("baohe redirect:orderId"+orderId+",result:"+result+",uid:"+uid+",carnumber:"+carnumber+",orderId:"+orderId+",cid:"+parkspaceMap.get("cid"));
				} catch (Exception e) {
					// TODO: handle exception
				}
				logger.error("sweepspace createorder>>>openid:"+openid+",uin:"+uin+",codeid:"+codeid+",b:"+b);
			}
			logger.equals("create order by sweep parking space>>>uin:"+uin+",uid:"+uid+",codeid:"+codeid+",orderid:"+orderId+",b:"+b);
			
			return mapping.findForward("neworder");
		}else if(action.equals("modifyorder")){
			Long orderid = RequestUtil.getLong(request, "id", -1L);
			Integer time = RequestUtil.getInteger(request, "time", 0);
			logger.error("orderid:"+orderid+",time:"+time);
			Map<String, Object> orderMap = daService.getMap("select create_time from order_tb where id=? ", new Object[]{orderid});
			Long create_time = (Long)orderMap.get("create_time");
			
			daService.update("update order_tb set create_time=? where id=? ", new Object[]{create_time - time * 60, orderid});
			Map<String, Object> oMap = daService.getMap("select create_time from order_tb where id=? ", new Object[]{orderid});
			logger.error("orderid:"+orderid+",time:"+time+",beftime:"+create_time+",afttime:"+oMap.get("create_time"));
		}else if(action.equals("callredir")){
			String mobile = RequestUtil.processParams(request, "callerid");
			String lot = RequestUtil.processParams(request, "cheweihaoma");
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			uid = 386572L;
			/*mobile = "18201517240";
			lot = "555555";*/
			logger.error("callredir>>>mobile:"+mobile+",lot:"+lot);
			if(mobile.equals("") || lot.equals("")){
				AjaxUtil.ajaxOutput(response, "res=1");
				return null;
			}
			Map<String, Object> userMap = daService.getMap("select id from user_info_tb where mobile=? and auth_flag=? ", 
					new Object[]{mobile, 4});
			if(userMap == null){
				AjaxUtil.ajaxOutput(response, "res=1");
				logger.error("callredir,�ֻ���δע��>>>mobile:"+mobile+",lot:"+lot);
				return null;
			}
			Long uin = (Long)userMap.get("id");
			Map<String, Object> lotMap = daService.getMap("select * from com_park_tb where cid=? limit ? ", 
					new Object[]{lot, 1});
			logger.error("lotMap:"+lotMap);
			if(lotMap == null || lotMap.get("comid") == null){
				AjaxUtil.ajaxOutput(response, "res=1");
				logger.error("callredir,��λ��δע��>>>mobile:"+mobile+",lot:"+lot);
				return null;
			}
			Long comid = -1L;
			if(lotMap.get("comid") != null){
				comid = (Long)lotMap.get("comid");
				Map<String, Object> comMap = daService.getMap("select company_name from com_info_tb where id=? ",
						new Object[] { comid });
				if(comMap == null){
					AjaxUtil.ajaxOutput(response, "res=1");
					logger.error("callredir,����������>>>mobile:"+mobile+",lot:"+lot);
					return null;
				}
			}
			String carnumber = commonMethods.getCarnumber(uin, 1);
			if(carnumber.equals("")){
				AjaxUtil.ajaxOutput(response, "res=1");
				logger.error("callredir,������ע�ᳵ�ƺ�>>>mobile:"+mobile+",lot:"+lot);
				return null;
			}
			Integer state = (Integer)lotMap.get("state");
			Long orderId = -1L;
			if(state == 1 && lotMap.get("order_id") != null){
				orderId = (Long)lotMap.get("order_id");
			}
			if(orderId == null && orderId <= 0){
				Map<String, Object> orderMap = daService.getMap("select * from order_tb where state=? and uin=? and comid=? ",
						new Object[] { 0, uin, comid });
				if(orderMap != null){
					orderId = (Long)orderMap.get("id");
				}
			}
			if(orderId != null && orderId > 0){
				Map<String, Object> orderMap = commonMethods.getOrderInfo(orderId, -1L, curtime);
				if(orderMap.get("uin") != null && (Long)orderMap.get("uin") != -1){
					Long onuin = (Long)orderMap.get("uin");
					logger.error("uin:"+uin+",orderid:"+orderId+",onuin:"+onuin);
					if(onuin.intValue() == uin.intValue()){
						Double total = Double.valueOf(orderMap.get("beforetotal") + "");
						Long end_time = (Long)orderMap.get("end_time");
						int result = publicMethods.payOrder(orderMap, total, uin, 2, 0, -1L, null, -1L, uid);
						
						if(result == 5){
							AjaxUtil.ajaxOutput(response, "res=2");
						}else{
							AjaxUtil.ajaxOutput(response, "res=3");
						}
						logger.error("uin:"+uin+",orderid:"+orderId+",result:"+result);
						return null;
					}
				}
				AjaxUtil.ajaxOutput(response, "res=1");
				return null;
			}
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���¶���״̬���շѳɹ�
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			//���³�λ��ά��
			Map<String, Object> spaceSqlMap = new HashMap<String, Object>();
			
			orderId = daService.getkey("seq_order_tb");
			spaceSqlMap.put("sql", "update com_park_tb set state =?,order_id=? where id=?");
			spaceSqlMap.put("values", new Object[]{1, orderId, lotMap.get("id")});
			bathSql.add(spaceSqlMap);
			
			orderSqlMap.put("sql", "insert into order_tb(id,create_time,uin,comid,c_type,state,car_number,uid) values(?,?,?,?,?,?,?,?)");
			orderSqlMap.put("values", new Object[]{orderId,curtime,uin,comid,6,0,carnumber,uid});
			bathSql.add(orderSqlMap);
			
			boolean b = daService.bathUpdate(bathSql);
			logger.error("baohe redirect:orderId"+orderId+",uin:"+uin+",b:"+b);
			if(b){
				AjaxUtil.ajaxOutput(response, "res=0");
				logger.error("callredir,���ɶ����ɹ�>>>mobile:"+mobile+",lot:"+lot);
				try {
					/*String date = TimeTools.getTime_yyyyMMdd_HHmmss(curtime*1000);
					date = AjaxUtil.encodeUTF8(date);
					date = AjaxUtil.encodeUTF8(date);*/
					carnumber = AjaxUtil.encodeUTF8(carnumber);
					String url = "http://www.bouwa.org/api/services/p4/Business/AppTingCheBaoInsertCarIn?EmployeeNumber="+uid+"&PlateNumber="+carnumber+"&ElectronicOrderid="+orderId+"&BerthNumber="+lot+"&CarType=1&CarInTime=2016";
					logger.error("baohe redirect:orderId"+orderId+",uid:"+uid+",carnumber:"+carnumber+",orderId:"+orderId+",cid:"+lot);
					String result = new HttpProxy().doGet(url);
					logger.error("baohe redirect:orderId"+orderId+",result:"+result+",uid:"+uid+",carnumber:"+carnumber+",orderId:"+orderId+",cid:"+lot);
				} catch (Exception e) {
					// TODO: handle exception
				}
				return null;
			}else{
				AjaxUtil.ajaxOutput(response, "res=1");
				logger.error("callredir,���ɶ���ʧ��>>>mobile:"+mobile+",lot:"+lot);
			}
			//http://192.168.199.239/zldi/wxpfast.do?action=callredir
		}else if(action.equals("sweeporder")){
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			Long endtime = RequestUtil.getLong(request, "endtime", -1L);
			String redirectUrl = "http%3A%2F%2F"+Constants.WXPUBLIC_REDIRECTURL+"%2Fzld%2Fwxpfast.do%3Faction%3Dposorder%26orderid%3D"+orderid+"%26endtime%3D"+endtime;
			String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri="+redirectUrl+
					"&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
			response.sendRedirect(url);
			return null;
			//http://s.tingchebao.com/zld/wxpfast.do?action=sweeporder&orderid=
		}else if(action.equals("posorder")){//����ɨ��pos����ӡ�Ķ�����
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			Long endtime = RequestUtil.getLong(request, "endtime", -1L);
			String code = RequestUtil.processParams(request, "code");
			String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXPUBLIC_APPID+"&secret="+Constants.WXPUBLIC_SECRET+"&code="+code+"&grant_type=authorization_code";
			String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
			JSONObject map = JSONObject.fromObject(result);
			if(map == null || map.get("errcode") != null){
				logger.error(">>>>>>>>>>>>��ȡopenidʧ��....,���»�ȡ>>>>>>>>>>>");
				String redirectUrl = "http%3A%2F%2F"+Constants.WXPUBLIC_REDIRECTURL+"%2Fzld%2Fwxpfast.do%3Faction%3Dposorder%26orderid%3D"+orderid+"%26endtime%3D"+endtime;
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri="+redirectUrl+
						"&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				response.sendRedirect(url);
				return null;
			}
			String openid = (String)map.get("openid");
/*			String openid = "oRoekt_EggHzjwINLJnUf8_w7gBg";
			openid = "oRoektybTsv33_vSKKUwLAsJAquc";*/
			request.setAttribute("openid", openid);
			//**************************����openid��ȡ�û���Ϣ*******************************//
			Map<String, Object> userinfoMap = commonMethods.getUserinfoByOpenid(openid);
			if(userinfoMap == null || (Long)userinfoMap.get("uin") < 0){
				return mapping.findForward("error");
			}
			Map<String, Object> infoMap = commonMethods.getOrderInfo(orderid, -1L, endtime);
			if(infoMap != null){
				request.setAttribute("orderid", orderid);
				request.setAttribute("uid", infoMap.get("uid"));
				request.setAttribute("total", infoMap.get("beforetotal"));
				request.setAttribute("start_time", infoMap.get("starttime"));
				request.setAttribute("parktime", infoMap.get("parktime"));
				request.setAttribute("car_number", infoMap.get("car_number"));
			}else{
				request.setAttribute("orderid", -1L);
			}
			return mapping.findForward("payorder");
			//http://wang151068941.oicp.net/zld/wxpfast.do?action=posorder&orderid=
		}else if(action.equals("payorder")){
			String openid = RequestUtil.processParams(request, "openid");
			Long orderId = RequestUtil.getLong(request, "orderid", -1L);
			Long ticketId  =RequestUtil.getLong(request, "ticketid", -1L);
			Double ticketMoney = RequestUtil.getDouble(request, "ticketMoney", 0d);
			Long disTicketId  =RequestUtil.getLong(request, "disTicketId", -1L);
			Double disTicketMoney = RequestUtil.getDouble(request, "disTicketMoney", 0d);
			Double total = RequestUtil.getDouble(request, "total", 0d);
			Long end_time = RequestUtil.getLong(request, "end_time", -1L);//��������ʱ��,2016-07-07���
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			logger.error("�����㶩����openid��"+openid);
			Map<String, Object> infoMap = new HashMap<String, Object>();
			if(openid.equals("")){
				infoMap.put("resultCode", "FAIL");
				infoMap.put("errorCode", -1);
				infoMap.put("errorInfo", "ϵͳ����");
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
				return null;
			}
			request.setAttribute("openid", openid);
			//**************************����openid��ȡ�û���Ϣ*******************************//
			Map<String, Object> userinfoMap = commonMethods.getUserinfoByOpenid(openid);
			if(userinfoMap == null || (Long)userinfoMap.get("uin") < 0){
				infoMap.put("resultCode", "FAIL");
				infoMap.put("errorCode", -1);
				infoMap.put("errorInfo", "ϵͳ����");
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
				return null;
			}
			Long uin = (Long)userinfoMap.get("uin");
			Map<String, Object> orderMap = pService.getMap("select * from order_tb where id=? ", 
					new Object[]{orderId});
			int result = publicMethods.payOrder(orderMap, total, uin, 2, 0, ticketId, null, -1L, uid);
			
			if(result == 5){
				infoMap.put("resultCode", "SUCCESS");
				if(uid != null && uid > 0){
					orderMap = commonMethods.getOrderInfo(orderId, -1L, end_time);
					Long comId = (Long)orderMap.get("comid");
					Long btime = (Long)orderMap.get("create_time");
					Long etime  = (Long)orderMap.get("end_time");
					String duration = StringUtils.getTimeString(btime, etime);
					String carNumber = (String)orderMap.get("car_number");
					if(carNumber==null||"".equals(carNumber)||"���ƺ�δ֪".equals(carNumber)){
						carNumber = publicMethods.getCarNumber(uin);
					}
					//��֧���ɹ���Ϣ���շ�Ա
					logService.insertParkUserMessage(comId, 2, uid, carNumber, orderId, total, duration, 0, btime, etime,0);
				}
				
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
		}
		return null;
	}
	
	/** 
     * ������ת��Unicode�� 
     * @param str 
     * @return 
     */  
    public String chinaToUnicode(String str){  
        String result="";  
        for (int i = 0; i < str.length(); i++){  
            int chr1 = (char) str.charAt(i);  
            if(chr1>=19968&&chr1<=171941){//���ַ�Χ \u4e00-\u9fa5 (����)  
                result+="\\u" + Integer.toHexString(chr1);  
            }else{  
                result+=str.charAt(i);  
            }  
        }  
        return result;  
    }
	
	private Integer checkLot(Long lotid){
		Map<String, Object> lotMap = daService.getMap("select * from com_park_tb where id=? ", 
				new Object[]{lotid});
		
		Integer state = (Integer)lotMap.get("state");
		if(state == 1){
			boolean reset = false;
			if(lotMap.get("order_id") == null || (Long)lotMap.get("order_id") == -1){
				reset = true;
			}else{
				Long count = daService.getLong("select count(id) from order_tb where state=? and id=? ", 
						new Object[]{0, lotMap.get("order_id")});
				if(count == 0){
					reset = true;
				}
			}
			if(reset){
				int r = daService.update("update com_park_tb set state=?,order_id=null where id=? ",
						new Object[] { 0, lotid });
				logger.error("checkLot>>>��λ״̬��ռ�ã����Ƕ����쳣��������,lotid:"+lotid+",r:"+r);
				return 0;
			}
			return 1;
		}
		return 0;
	}
	
	private Map<String, Object> getTicket(Map<String, Object> infoMap){
		Map<String, Object> rMap = new HashMap<String, Object>();
		Long ticketId = -1L;
		Double ticket_money = 0d;
		Double limit = 0d;
		/*============ѡ��ͣ��ȯ�߼�==============*/
		Long orderid = -1L;
		String mobile = "";
		Long uid = -1L;
		Long uin = -1L;
		Integer bindflag = 0;
		String openid = "";
		Double total = 0d;
		if(infoMap.get("orderid") != null){
			orderid = (Long)infoMap.get("orderid");
		}
		if(infoMap.get("mobile") != null){
			mobile = (String)infoMap.get("mobile");
		}
		if(infoMap.get("uid") != null){
			uid = (Long)infoMap.get("uid");
		}
		if(infoMap.get("uin") != null){
			uin = (Long)infoMap.get("uin");
		}
		if(infoMap.get("bindflag") != null){
			bindflag = (Integer)infoMap.get("bindflag");
		}
		if(infoMap.get("openid") != null){
			openid = (String)infoMap.get("openid");
		}
		if(infoMap.get("total") != null){
			total = Double.valueOf(infoMap.get("total") + "");
		}
		logger.error("orderid:"+orderid+",mobile:"+mobile+",uid:"+uid+",uin:"+uin+",bindflag:"+bindflag+",openid:"+openid+",total:"+total);
		
		//ѡȯ
		Map<String, String> params = new HashMap<String, String>();
		params.put("mobile", mobile);
		params.put("action", "wxaccount");
		params.put("total", total + "");
		params.put("wxp_uin", uin + "");
		params.put("uid", uid + "");
		logger.error("ѡ����ȯ�Ĳ���orderid:"+orderid+",uin:"+uin+",openid:"+openid+",uid:"+uid+",params:"+params.toString());
		String result = new HttpProxy().doPost("http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/carinter.do", params);
		logger.error("ѡ����ȯ���:tickets:"+result+",orderid:"+orderid+",uin:"+uin+",openid:"+openid+",uid:"+uid);
		if(!result.equals("")){
			JSONObject jsonObject = JSONObject.fromObject(result);
			JSONArray jsonArray = JSONArray.fromObject(jsonObject.getString("tickets"));
			if(jsonArray.size() != 0){
				JSONObject jsonObject2 = (JSONObject)jsonArray.get(0);
				ticketId = jsonObject2.getLong("id");
				ticket_money = jsonObject2.getDouble("money");
				limit = ticket_money;
			}else{
				if(bindflag == 1){
					params.clear();
					params.put("mobile", mobile);
					params.put("action", "getaccount");
					params.put("total", total + "");
					params.put("uin", uin + "");
					params.put("orderid", orderid + "");
					params.put("uid", uid + "");
					params.put("utype", "2");
					params.put("source", "1");//����΢�Ź��ں�ѡȯ
					logger.error("ѡ����ȯ�Ĳ���orderid:"+orderid+",uin:"+uin+",openid:"+openid+",uid:"+uid+",params:"+params.toString());
					result = new HttpProxy().doPost("http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/carowner.do", params);
					logger.error("ѡ����ȯ���:tickets:"+result+",orderid:"+orderid+",uin:"+uin+",openid:"+openid);
					if(!result.equals("")){
						jsonObject = JSONObject.fromObject(result);
						jsonArray = JSONArray.fromObject(jsonObject.getString("tickets"));
						if(jsonArray.size() != 0){
							JSONObject jsonObject2 = (JSONObject)jsonArray.get(0);
							ticketId = jsonObject2.getLong("id");
							ticket_money = jsonObject2.getDouble("money");
							limit = jsonObject2.getDouble("limit");
						}
					}
				}
			}
		}
		rMap.put("ticketId", ticketId);
		rMap.put("ticket_money", ticket_money);
		rMap.put("limit", limit);
		logger.error("����ѡȯ�����orderid:"+orderid+",uin:"+uin+",openid:"+openid+",ticketid:"+ticketId+",ticket_money:"+ticket_money);
		return rMap;
	}
}
