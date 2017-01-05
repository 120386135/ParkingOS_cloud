package com.zld.struts.request;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.weixinpay.utils.util.JsonUtil;
import com.zld.wxpublic.util.CommonUtil;

public class WeixinPublicAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private PublicMethods publicMethods;

	private Logger logger = Logger.getLogger(WeixinPublicAction.class);
	/**
	 * weixin
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		if(action.equals("")){
			String code = RequestUtil.processParams(request, "code");
			String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXPUBLIC_APPID+"&secret="+Constants.WXPUBLIC_SECRET+"&code="+code+"&grant_type=authorization_code";
			String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
			JSONObject map = JSONObject.fromObject(result);
			if(map == null || map.get("errcode") != null){
				logger.error(">>>>>>>>>>>>��ȡopenidʧ��....,���»�ȡ>>>>>>>>>>>");
				String redirect_url = "http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpublic.do";
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
						+ Constants.WXPUBLIC_APPID
						+ "&redirect_uri="
						+ redirect_url
						+ "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				response.sendRedirect(url);
				return null;
			}
			String openid = (String)map.get("openid");
//			String openid = "oRoektybTsv33_vSKKUwLAsJAquc";
			//��������
			request.setAttribute("openid", openid);
			Map<String, Object> userMap = daService
					.getMap("select * from user_info_tb where auth_flag=? and wxp_openid=? and state=? ",
							new Object[] {4, openid, 0 });
			if(userMap == null){//δ���û��ʻ��������û���ҳ��
				request.setAttribute("action", "wxpublic.do?action=bind");
				return mapping.findForward("adduser");
			}
			Long count = daService.getLong(
					"select count(id) from car_info_tb where uin=? and state=? ",
					new Object[] { userMap.get("id"), 1 });
			if(count == 0){//����󶨳���ҳ��
				request.setAttribute("mobile", userMap.get("mobile"));
				request.setAttribute("action", "wxpublic.do?action=toparklistpage");
				return mapping.findForward("addcarnumber");
			}
			/*Map<String, String> ret = new HashMap<String, String>();
			ret = getJssdkApiSign(request);
			//jssdkȨ����֤����
			request.setAttribute("appid", Constants.WXPUBLIC_APPID);
			request.setAttribute("nonceStr", ret.get("nonceStr"));
			request.setAttribute("timestamp", ret.get("timestamp"));
			request.setAttribute("signature", ret.get("signature"));*/
			
			return mapping.findForward("parklist");
		}else if(action.equals("bind")){
			
			String mobile = RequestUtil.processParams(request, "mobile").trim();
			String openid = RequestUtil.processParams(request, "openid");
			String topage = RequestUtil.processParams(request, "topage");
			request.setAttribute("openid", openid);
			request.setAttribute("mobile", mobile);
			if(mobile.equals("") || openid.equals("")){
				return mapping.findForward("error");
			}
			Long bind_count = daService.getLong(
					"select count(*) from user_info_tb where wxp_openid is not null and mobile=? ",
					new Object[] { mobile });
			int result = daService
					.update("update user_info_tb set wxp_openid=? where auth_flag=? and state=? and mobile=? ",
							new Object[] { openid, 4, 0, mobile });//΢�Ź��ںŰ󶨳����˻�
			if(result == 1){
				Map<String, Object> userMap = daService
						.getMap("select id from user_info_tb where mobile=? and auth_flag=? ",
								new Object[] { mobile, 4 });
				publicMethods.sharkbinduser(openid, (Long)userMap.get("id"), bind_count);
				
				Long count = daService
						.getLong("select count(c.id) from car_info_tb c,user_info_tb u where c.uin=u.id and u.auth_flag=? and c.state=? and u.mobile=? ",
								new Object[] {4, 1, mobile });
				if(count == 0){//����󶨳����˺�
					request.setAttribute("topage", topage);
					request.setAttribute("action", "wxpublic.do?action=toparklistpage");
					return mapping.findForward("addcarnumber");
				} else {//��ת�������б�ҳ��
					if(topage.equals("wantstop")){
						response.sendRedirect("attendant.do?action=wantstop&openid="+openid);
						return null;
					}
					/*Map<String, String> ret = new HashMap<String, String>();
					ret = getJssdkApiSign(request);
					//jssdkȨ����֤����
					request.setAttribute("appid", Constants.WXPUBLIC_APPID);
					request.setAttribute("nonceStr", ret.get("nonceStr"));
					request.setAttribute("timestamp", ret.get("timestamp"));
					request.setAttribute("signature", ret.get("signature"));*/
					return mapping.findForward("parklist");
				}
			}
			return mapping.findForward("error");
		}else if(action.equals("toparklistpage")){
			String openid = RequestUtil.processParams(request, "openid");
			request.setAttribute("openid", openid);
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			
			Map<String, String> ret = new HashMap<String, String>();
			/*ret = getJssdkApiSign(request);
			//jssdkȨ����֤����
			request.setAttribute("appid", Constants.WXPUBLIC_APPID);
			request.setAttribute("nonceStr", ret.get("nonceStr"));
			request.setAttribute("timestamp", ret.get("timestamp"));
			request.setAttribute("signature", ret.get("signature"));*/
			return mapping.findForward("parklist");
		}else if(action.equals("getparklist")){
			Double latitude =RequestUtil.getDouble(request, "latitude",0d);
			Double longitude =RequestUtil.getDouble(request, "longitude",0d);
			Integer payable = RequestUtil.getInteger(request, "payable", 1);//0�����Ƿ��֧����1���ؿ�֧���ĳ���
			String openid = RequestUtil.processParams(request, "openid");
			if(latitude==0||longitude==0)
				return null;
			List<Map<String, Object>> list = publicMethods.getPark2kmList(latitude,longitude,payable);
			List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Map<String, Object> infoMap = new HashMap<String, Object>();
					double lon2 = Double.valueOf(map.get("lng")+"");
					double lat2 = Double.valueOf(map.get("lat")+"");
					Integer epay = (Integer)map.get("epay");
					Integer isfixed = (Integer)map.get("isfixed");
					double distance = StringUtils.distanceByLnglat(longitude,latitude,lon2,lat2);
					distance = StringUtils.formatDouble(distance);
					Map<String, Object> firstMap = getfirst((Long)map.get("id"));
					int first = 0;
					if(firstMap != null){
						first = 1;
						infoMap.put("first_times", firstMap.get("first_times"));
						infoMap.put("fprice", firstMap.get("fprice"));
						infoMap.put("unit", firstMap.get("unit"));
					}
					infoMap.put("first", first);
					
					//��ͼƬ
					Map<String,Object> picMap = pgOnlyReadService.getMap("select picurl from com_picturs_tb where comid=? order by id desc limit ?",
							new Object[]{map.get("id"),1});
					String picUrls = "";
					if(picMap!=null&&!picMap.isEmpty()){
						picUrls="http://"+Constants.WXPUBLIC_S_DOMAIN+"/tcbcloud/"+(String)picMap.get("picurl");
					}
					infoMap.put("picurl", picUrls);
					int b = parkbackTicket((Long)map.get("id"));
					infoMap.put("id", map.get("id"));
					infoMap.put("company_name", map.get("name"));
					infoMap.put("distance", distance);
					infoMap.put("backticket", b);
					infoMap.put("epay", epay);
					infoMap.put("isfixed", isfixed);
					resultList.add(infoMap);
				}
			}
			Collections.sort(resultList, new ListSort());
			String ret = "{\"openid\":\""+openid+"\",\"parklist\":[]}";
			String parklist = StringUtils.createJson(resultList);
			ret = ret.replace("[]", parklist);
			AjaxUtil.ajaxOutput(response, parklist);
		}else if(action.equals("getparkpic")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String picUrls = "";
			if(comid != -1){
				//��ͼƬ
				Map<String,Object> picMap = pgOnlyReadService.getMap("select picurl from com_picturs_tb where comid=? order by id desc limit ?",
						new Object[]{comid,1});
				if(picMap!=null&&!picMap.isEmpty()){
					picUrls="http://"+Constants.WXPUBLIC_S_DOMAIN+"/tcbcloud/"+(String)picMap.get("picurl");
				}
			}
			AjaxUtil.ajaxOutput(response, picUrls);
			return null;
		}else if(action.equals("toparkerpage")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String openid = RequestUtil.processParams(request, "openid");
//			Long comid = 3480L;
//			String openid = "oRoektybTsv33_vSKKUwLAsJAquc";
			request.setAttribute("openid", openid);
			request.setAttribute("comid", comid);
			
			//��ѯ��û�ж���
			Map<String, Object> comMap = daService.getMap("select etc from com_info_tb where id=? ", new Object[]{comid});
			if(comMap != null && comMap.get("etc") != null){
				Integer etc = (Integer)comMap.get("etc");
				if(etc == 2 || etc == 1){
					Map<String, Object> carMap = daService.getMap("select c.* from car_info_tb c,user_info_tb u where c.uin=u.id and wxp_openid=? ",
									new Object[] { openid });
					if(carMap != null){
						Map<String, Object> orderMap = daService
								.getMap("select * from order_tb where total is null and car_number=? and state=? and comid=? and c_type=? order by create_time desc limit ? ",
										new Object[] { carMap.get("car_number"), 0, comid, 3,1 });
						if(orderMap != null){
							response.sendRedirect("wxpfast.do?action=sweepcom&comid="+comid+"&openid="+openid);
							return null;
						}
					}
				}
			}
			
			return mapping.findForward("parkerlist");
		}else if(action.equals("getparkerlist")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String openid = RequestUtil.processParams(request, "openid");
			if(comid == -1){
				return mapping.findForward("error");
			}else{
				Map<String, Object> comMap = daService.getMap("select * from com_info_tb where id=?", new Object[]{comid});
				List<Map<String, Object>> parkerlist = new ArrayList<Map<String, Object>>();
				parkerlist = daService.getAll("select * from user_info_tb where (auth_flag=? or auth_flag=?) and state=? and isview=? and comid=? order by id ",
								new Object[] { 1, 2, 0, 1, comid });
				setReward(parkerlist);
				String ret = "{\"openid\":\""+openid+"\",\"company_name\":\""+comMap.get("company_name")+"\",\"parkerlist\":[]}";
				String parkers = StringUtils.createJson(parkerlist);
				ret = ret.replace("[]", parkers);
				AjaxUtil.ajaxOutput(response, parkers);
			}
		}else if(action.equals("balancepayinfo")){
			Double money = RequestUtil.getDouble(request, "money", 0d);
			String openid = RequestUtil.processParams(request, "openid");
			Long orderid = RequestUtil.getLong(request, "orderid", -1L);
			Integer paytype = RequestUtil.getInteger(request, "paytype", 0);//0:Ԥ֧����1�����㶩��
			Integer notice_type = RequestUtil.getInteger(request, "notice_type", 0);//֪ͨ���ͣ�0��ֱ���ɹ�֪ͨ��1��Ԥ֧���ɹ�֪ͨ 2�����ͳɹ�֪ͨ 3:��ֵ�ɹ� 4:��֤�ɹ�֪ͨ5������ͣ��ȯ�ɹ�
			Integer leaving_time = RequestUtil.getInteger(request, "leaving_time", 0);
			Long bonusid = RequestUtil.getLong(request, "bonusid", -1L);
			Integer bonus_type = RequestUtil.getInteger(request, "bonus_type", 0);//0:ͣ��ȯ�����1΢�Ŵ��ۺ��
			Integer first_flag = RequestUtil.getInteger(request, "first_flag", 0);//0:���ױ�֧�� 1���ױ�֧��
			
			//-----------����ͣ��ȯ����---------------//
			Integer ticketmoney = RequestUtil.getInteger(request, "ticketmoney", 0);//����ͣ��ȯ���
			Integer ticketnum = RequestUtil.getInteger(request, "ticketnum", 0);//���������
			request.setAttribute("ticketmoney", ticketmoney);
			request.setAttribute("ticketnum", ticketnum);
			//------------������²���---------------//
			String starttime = RequestUtil.processParams(request, "starttime");//���¿�ʼʱ��
			String endtime = RequestUtil.processParams(request, "endtime");//���½���ʱ��
			request.setAttribute("starttime", starttime);
			request.setAttribute("endtime", endtime);
			
			request.setAttribute("leaving_time", leaving_time);
			request.setAttribute("notice_type", notice_type);
			request.setAttribute("paytype", paytype);
			request.setAttribute("openid", openid);
			logger.error("����΢��֧���ɹ�֪ͨ���ܽ��="+money+",orderid"+orderid+",openid="+openid+",notice_type="+notice_type+"����"+leaving_time+"����֮���뿪"+",bonusid="+bonusid+",first_flag="+first_flag);
			
			if(openid.equals("")){
				return mapping.findForward("error");
			}
			
			request.setAttribute("money", money);
			//��ѯ�������
			if(notice_type == 0 || paytype == 1 || notice_type == 3 || notice_type == 4){//ֱ���ɹ���  ���߽��㶩����  �����˻���ֵ
				if(bonusid == -1){
					Long uin = -1L;//�û�id
					Map<String, Object> userMap = daService.getMap(
							"select id from user_info_tb where wxp_openid=? ",
							new Object[] { openid });
					if(userMap != null){//�󶨹��˻�
						uin = (Long)userMap.get("id");
					}else{//δ�󶨹��˻���ҡһҡֱ�����
						Map<String, Object> userNobind = daService.getMap(
								"select uin from wxp_user_tb where openid=? ",
								new Object[] { openid });
						if(userNobind != null){
							uin = (Long)userNobind.get("uin");
						}
					}
					if(uin != -1){
						Map bMap  =daService.getMap("select * from order_ticket_tb where etime is null and uin=? and ctime>? order by ctime desc limit ?",
								new Object[]{uin, System.currentTimeMillis()/1000 -5*60,1});//�����ǰ�ĺ��
						
						if(bMap!=null&&bMap.get("id")!=null){
							if(bMap.get("type")!= null && (Integer)bMap.get("type") == 1){
								bonus_type = 1;//΢�Ŵ��ۺ��
							}
							bonusid = (Long)bMap.get("id");
							request.setAttribute("bonus_money", bMap.get("money"));
							request.setAttribute("bonus_bnum", bMap.get("bnum"));
							
							Long count = daService.getLong("select count(*) from user_account_tb where uin=? and type=? ", new Object[]{uin, 1});
							if(count == 1){
								first_flag = 1;
								logger.error("�ױ�֧�������������bonus_id:"+bonusid+",uin:"+uin+",orderid:"+bMap.get("order_id"));
							}else{
								logger.error("���ױ�֧����bonus_id:"+bonusid+",uin:"+uin+",orderid:"+bMap.get("order_id"));
							}
						}
					}
				}else{
					Map bMap = daService.getMap("select * from order_ticket_tb where id=? ", new Object[]{bonusid});
					if(bMap != null){
						request.setAttribute("bonus_money", bMap.get("money"));
						request.setAttribute("bonus_bnum", bMap.get("bnum"));
					}
				}
			}
			
			request.setAttribute("first_flag", first_flag);
			request.setAttribute("domain", Constants.WXPUBLIC_REDIRECTURL);
			logger.error(">>>>>>>>>>bonusid:"+bonusid+",openid:"+openid+",orderid:"+orderid+",bonus_type:"+bonus_type);
			request.setAttribute("bonus_type", bonus_type);
			request.setAttribute("bonusid", bonusid);
			try {
				//΢�Ź��ں�JSSDK��Ȩ��֤
				Map<String, String> ret = new HashMap<String, String>();
				ret = publicMethods.getJssdkApiSign(request);
				//jssdkȨ����֤����
				request.setAttribute("appid", Constants.WXPUBLIC_APPID);
				request.setAttribute("nonceStr", ret.get("nonceStr"));
				request.setAttribute("timestamp", ret.get("timestamp"));
				request.setAttribute("signature", ret.get("signature"));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			//����֧���ɹ�ҳ��
			request.setAttribute("title", CustomDefind.getValue("TITLE"));
			request.setAttribute("desc", CustomDefind.getValue("DESCRIPTION"));
			return mapping.findForward("paysuccess");
			//http://192.168.199.239/zld/wxpublic.do?action=balancepayinfo
		}else if(action.equals("getmobile")){
			String openid = RequestUtil.processParams(request, "openid");
			if(!openid.equals("")){
				Map<String, Object> userMap = pgOnlyReadService.getMap("select mobile from user_info_tb where wxp_openid=? ", new Object[]{openid});
				if(userMap != null){
					AjaxUtil.ajaxOutput(response, userMap.get("mobile") + "");
					return null;
				}
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("getuserinfo")){
			String mobile = RequestUtil.processParams(request, "mobile");
			if(!mobile.equals("")){
				Map<String, Object> userMap = pgOnlyReadService
						.getMap("select * from user_info_tb where mobile=? and auth_flag=? ",
								new Object[] { mobile, 4 });
				if(userMap != null){
					Long uin = (Long)userMap.get("id");
					List<Map<String, Object>> carlist = new ArrayList<Map<String,Object>>();
					carlist = pgOnlyReadService.getAll("select * from car_info_tb where uin=? ",
							new Object[] { uin });
					boolean useticket = memcacheUtils.readUseTicketCache(uin);
					boolean isblack = publicMethods.isBlackUser(uin);
					int bindflag = 0;
					if(userMap.get("wxp_openid") != null){
						bindflag = 1;
					}
					String ret = "{\"is_auth\":\"" + userMap.get("is_auth")
							+ "\",\"useticket\":\"" + useticket
							+ "\",\"isblack\":\"" + isblack
							+ "\",\"openid\":\"" + userMap.get("wxp_openid")
							+ "\",\"uin\":\"" + userMap.get("id")
							+ "\",\"bindflag\":\"" + bindflag
							+ "\",\"carnumber\":[]}";
					String carnumber = StringUtils.createJson(carlist);
					ret = ret.replace("[]", carnumber);
					AjaxUtil.ajaxOutput(response, ret);
					return null;
				}
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("getcarinfo")){
			String carnumber = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "carnumber"));
			if(!carnumber.equals("")){
				Map<String, Object> carMap = pgOnlyReadService.getMap(
						"select * from car_info_tb where car_number=? ",
						new Object[] { carnumber });
				if(carMap != null){
					Long uin = (Long)carMap.get("uin");
					Map<String, Object> userMap = pgOnlyReadService.getMap(
							"select * from user_info_tb where id=? ",
							new Object[] { uin });
					if(userMap != null){
						List<Map<String, Object>> carlist = new ArrayList<Map<String,Object>>();
						carlist = pgOnlyReadService.getAll("select * from car_info_tb where uin=? ",
								new Object[] { uin });
						boolean useticket = memcacheUtils.readUseTicketCache(uin);
						boolean isblack = publicMethods.isBlackUser(uin);
						int bindflag = 0;
						if(userMap.get("wxp_openid") != null){
							bindflag = 1;
						}
						String ret = "{\"is_auth\":\"" + userMap.get("is_auth")
								+ "\",\"useticket\":\"" + useticket
								+ "\",\"isblack\":\"" + isblack
								+ "\",\"openid\":\"" + userMap.get("wxp_openid")
								+ "\",\"uin\":\"" + userMap.get("id")
								+ "\",\"mobile\":\"" + userMap.get("mobile")
								+ "\",\"bindflag\":\"" + bindflag
								+ "\",\"carnumber\":[]}";
						String cars = StringUtils.createJson(carlist);
						ret = ret.replace("[]", cars);
						AjaxUtil.ajaxOutput(response, ret);
						return null;
					}
				}
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("getconsuminfo")){
			String wxorderid = RequestUtil.processParams(request, "wxorderid");
			String openid = RequestUtil.processParams(request, "openid");
			String btime = RequestUtil.processParams(request, "begintime");
			String etime = RequestUtil.processParams(request, "endtime");
			Integer type = RequestUtil.getInteger(request, "type", 0);//0:��ѯ������ˮ 1����ѯ�˻���ˮ
			Long b = TimeTools.getToDayBeginTime();
			Long e = System.currentTimeMillis()/1000;
			if(!btime.equals("")){
				b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
			}
			if(!etime.equals("")){
				e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime);
			}
			if(!openid.equals("")){
				Long uin = null;
				if(!wxorderid.equals("")){
					Map<String, Object> logMap = pgOnlyReadService.getMap(
							"select * from alipay_log where wxp_orderid=? ",
							new Object[] { wxorderid });
					
					if(logMap != null){
						uin = (Long)logMap.get("uin");
					}
				}else{
					Map<String, Object> userMap = pgOnlyReadService
							.getMap("select * from user_info_tb where wxp_openid=? limit ? ",
									new Object[] { openid, 1 });
					if(userMap != null){
						uin = (Long)userMap.get("id");
					}else{
						userMap = pgOnlyReadService.getMap(
								"select * from wxp_user_tb where openid=? ",
								new Object[] { openid });
						if(userMap != null){
							uin = (Long)userMap.get("uin");
						}
					}
				}
				
				if(uin != null){
					if(type == 0){
						List<Object> params = new ArrayList<Object>();
						String sql = "select * from order_tb where (state=? or state=? or (state=? and end_time between ? and ?)) ";
						params.add(0);
						params.add(2);
						params.add(1);
						params.add(b);
						params.add(e);
						params.add(uin);
						List<Map<String, Object>> carlist = pgOnlyReadService
								.getAll("select car_number from car_info_tb where uin=? ",
										new Object[] { uin });
						String preParams  ="";
						if(carlist != null){
							for(Map<String, Object> map : carlist){
								params.add(map.get("car_number"));
								if(preParams.equals(""))
									preParams ="?";
								else
									preParams += ",?";
							}
						}else{
							Map<String, Object> carMap = pgOnlyReadService
									.getMap("select car_number from wxp_user_tb where uin=? ",
											new Object[] { uin });
							if(carMap != null && carMap.get("car_number") != null){
								params.add(carMap.get("car_number"));
								preParams = "?";
							}
						}
						
						if(!preParams.equals("")){
							sql += " and (uin=? or car_number in (" + preParams + "))";
						}else{
							sql += " and uin=? ";
						}
						
						sql += " order by end_time desc";
						List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
						list = pgOnlyReadService.getAllMap(sql, params);
						for(Map<String, Object> map : list){
							Long create_time = (Long)map.get("create_time");
							String ctime = TimeTools.getTime_MMdd_HHmm(create_time * 1000);
							map.put("ctime", ctime);
							if(map.get("end_time") != null){
								Long end_time = (Long)map.get("end_time");
								String endtime = TimeTools.getTime_MMdd_HHmm(end_time * 1000);
								map.put("etime", endtime);
							}
						}
						AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
						return null;
					}else if(type == 1){
						List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
						list = pgOnlyReadService
								.getAll("select * from user_account_tb where uin=? and create_time between ? and ? order by create_time desc ",
										new Object[] { uin, b, e });
						for(Map<String, Object> map : list){
							Long create_time = (Long)map.get("create_time");
							String ctime = TimeTools.getTime_MMdd_HHmm(create_time * 1000);
							map.put("ctime", ctime);
						}
						AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
						return null;
					}
				}
				
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("getcode")){
			String mobile = RequestUtil.processParams(request, "mobile");
			if(!mobile.equals("")){
				Map<String, Object> userMap = pgOnlyReadService
						.getMap("select id from user_info_tb where mobile=? and auth_flag=? ",
								new Object[] { mobile, 4 });
				if(userMap != null){
					Long uin = (Long)userMap.get("id");
					Map<String, Object> codeMap = pgOnlyReadService
							.getMap("select verification_code from verification_code_tb where uin=? and state=? order by create_time desc limit ?",
									new Object[] { uin, 0, 1 });
					if(codeMap != null){
						AjaxUtil.ajaxOutput(response, codeMap.get("verification_code") + "");
						return null;
					}
				}
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("resetcache")){
			//=========����access_token����begin===========//
			String url = Constants.WXPUBLIC_GETTOKEN_URL;
			//��weixin�ӿ�ȡaccess_token
			String result = new HttpProxy().doGet(url);
			logger.error("resetcache>>>wxpublic_access_token json:"+result);
			String access_token = JsonUtil.getJsonValue(result, "access_token");//result.substring(17,result.indexOf(",")-1);
			logger.error("resetcache>>>wxpublic_access_token:"+access_token);
			//���浽���� 
			memcacheUtils.setWXPublicToken(access_token);
			//=========����access_token����end===========//
			//=========����jsapi_ticket����begin===========//
			String jsapi_ticket_url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+access_token+"&type=jsapi";
			result = CommonUtil.httpsRequest(jsapi_ticket_url, "GET", null);
			net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(result);
			String jsapi_ticket = jsonObject.getString("ticket");
			logger.error("resetcache>>>wxpublic jsapi_ticket:"+jsapi_ticket);
			//���浽����
			memcacheUtils.setJsapi_ticket(jsapi_ticket);
			//=========����jsapi_ticket����end===========//
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("error")){
			Integer errorCode = RequestUtil.getInteger(request, "errorcode", 0);
			request.setAttribute("type", errorCode);
			return mapping.findForward("error");
		}
		return null;
	}
	
	private void setReward(List<Map<String, Object>> list){
		List<Object> uins = new ArrayList<Object>();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				uins.add(map.get("id"));
				
				map.put("rewardcount", 0);
				map.put("servercount", 0);
			}
			if(!uins.isEmpty()){
				String preParams  ="";
				for(Object uin : uins){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				Long btime = TimeTools.getToDayBeginTime() - 6 * 24 * 60 * 60;
				uins.add(btime);
				List<Map<String, Object>> resultList = pgOnlyReadService.getAllMap("select uid,count(id) reward from parkuser_reward_tb where uid in ("+preParams+") and ctime> ? group by uid ", uins);
				if(resultList != null){
					for(Map<String, Object> map : resultList){
						Long uid = (Long)map.get("uid");
						Long reward = (Long)map.get("reward");
						for(Map<String, Object> map2 : list){
							Long id = (Long)map2.get("id");
							if(id.intValue() == uid.intValue()){
								map2.put("rewardcount", reward);
								break;
							}
						}
					}
				}
				uins.add(1);
				resultList = pgOnlyReadService.getAllMap("select count(id) servercount,uid from order_tb where uid in (" + preParams
								+ ") and end_time >? and state=? group by uid ", uins);
				if(resultList != null){
					for(Map<String, Object> map : resultList){
						Long uid = (Long)map.get("uid");
						Long server = (Long)map.get("servercount");
						for(Map<String, Object> map2 : list){
							Long id = (Long)map2.get("id");
							if(id.intValue() == uid.intValue()){
								map2.put("servercount", server);
								break;
							}
						}
					}
				}
			}
		}
		
	}
	
	private int parkbackTicket(Long comid){
		Long count = daService.getLong(
						"select count(id) from park_ticket_tb where tnumber>haveget and comid=? ",
						new Object[] { comid });
		if(count > 0){
			return 1;
		}
		return 0;
	}
	
	private Map<String, Object> getfirst(Long comid){
		Map<String, Object> priceMap = pgOnlyReadService
				.getMap("select * from price_tb where comid=? and pay_type=? and first_times> ? and state=? order by first_times desc limit ? ",
						new Object[] { comid, 0, 0, 0, 1 });
		return priceMap;
	}
	
	class ListSort implements Comparator<Map<String, Object>>{

		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			// TODO Auto-generated method stub
			BigDecimal b1 = new BigDecimal(0);
			BigDecimal b2 = new BigDecimal(0);
			if(o1.get("distance") != null){
				if(o1.get("distance") instanceof Double){
					Double distance = (Double)o1.get("distance");
					b1 = b1.valueOf(distance);
				}else{
					b1 = (BigDecimal)o1.get("distance");
				}
			}
			if(o2.get("distance") != null){
				if(o2.get("distance") instanceof Double){
					Double distance = (Double)o2.get("distance");
					b2 = b2.valueOf(distance);
				}else{
					b2 = (BigDecimal)o2.get("distance");
				}
			}
			return b1.compareTo(b2);
		}
		
	}
}
