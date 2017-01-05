package com.zld.struts.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.utils.Check;
import com.zld.utils.StringUtils;
import com.zld.utils.ZldUploadOperate;
import com.zld.utils.ZldUploadUtils;


/**
 * ��Ӫ���� ���нӿ�
 * @author laoyao
 *
 */
@Path("business")
public class ZldBusinessApi {
	
	
	Logger logger = Logger.getLogger(ZldBaseInfoApi.class);
	
	
	/**
	 * ���㶩��
	 * http://127.0.0.1/zld/api/business/payorder
	 */
	@POST
	@Path("/payorder")//���㶩��
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void payOrder(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		logger.error("payorder:origin params:"+params);
		logger.error("payorder:anlysis params:"+paramMap);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		Map<String, Object> returnMap =new HashMap<String, Object>();
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			if(comid==null||comid.equals("-1")){
				returnMap.put("status", "2");
				returnMap.put("resultCode", "100");
				returnMap.put("message", "������Ϣ���Ϸ����Ҳ�������������ParkingNo���Ϸ���");
				returnMap.put("data", "{}");
				logger.error("payorder:error,������Ϣ���Ϸ����Ҳ����������������أ�"+returnMap+"��ԭʼ����:"+params);
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
				return ;
			}else {
				paramMap.put("comid", comid);
				paramMap.remove("park_uuid");
			}
		}else {
			returnMap.put("status", "2");
			returnMap.put("resultCode", "100");
			returnMap.put("message", "������Ϣ���Ϸ����Ҳ�������������ԭʼ����:"+params);
			returnMap.put("data", "{}");
			logger.error("payorder:error,������Ϣ���Ϸ����Ҳ����������������أ�"+returnMap+"��ԭʼ����:"+params);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
			return ;
		}
		Long uin = -1L;
		if(paramMap.get("car_number")!=null){
			paramMap.put("car_number", paramMap.get("car_number").toUpperCase());
			uin = zldUploadOperate.getUinByCarNumber(paramMap.get("car_number"), context);
		}
		paramMap.put("uin", uin+"");
		paramMap.put("c_type", "2");
		logger.error(">>>>>payorder,params:"+paramMap);
		String isEscape = paramMap.get("isescape");//�Ƿ���Ƿ��
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		Long neworderId = daService.getkey("seq_order_tb");
		if(isEscape!=null&&isEscape.equals("true")){//�ӵ�������ֱ��д�����ݿ⣬���۷�
			logger.error("payorder >>>д�ӵ���¼...");
			String escape = paramMap.get("escape");//��Ƿ�ѽ��. ��λ����
			Double t = 0.0;
			if(escape!=null&&Check.isNumber(escape)){
				t = StringUtils.formatDouble(escape)/100;//תΪԪ��������λС��
			}
			paramMap.put("total", t+"");
			paramMap.put("id", neworderId+"");
			//д������
			paramMap.put("state", "0");
			returnMap= zldUploadOperate.handleData(context,paramMap,params,"order_tb",0);
			if(returnMap.get("status").equals("1")){
				//д�ӵ���
				String sql = "insert into no_payment_tb(create_time,order_id,end_time,car_number,comid,uin,total) values(?,?,?,?,?,?,?)";
				Object[] values = new Object[]{Long.valueOf(paramMap.get("create_time")),neworderId,Long.valueOf(paramMap.get("end_time")),
						paramMap.get("car_number"),Long.valueOf(paramMap.get("comid")),uin,t};
				int ret = daService.update(sql, values);
				logger.error(">>>>>escape order>>>>���������㶩�����ϴ��ӵ���д���ӵ���������"+paramMap.get("car_number")+",��"+t+"�������"+ret);
			}
		}else {//���㶩��
			logger.error("payorder >>>���㶩��...");
			String allTotal = paramMap.get("all_total");
			String total = paramMap.get("total");
			if(allTotal!=null&&Check.isNumber(allTotal)&&total!=null&&Check.isNumber(total)){
				Integer t= Integer.valueOf(total);
				Integer at = Integer.valueOf(allTotal);
				String carNumber = (String)paramMap.get("car_number");
				if(at!=0&&at>t){//���ӵ����
					logger.error("payorder >>>���ӵ����:"+(at-t)+"�֣���ʼ�����ӵ�...");
					Integer et = 0;
					et = at-t;//�����ӵ����
					//����Ƿ�Ѷ���
					List<Map<String, Object>> list = daService.getAll("select * from no_payment_tb where state=?" +
							" and car_number= ?", new Object[]{0,paramMap.get("car_number")});
					//������Ҫд�복���˻����ӵ����
					Map<Long,String> comIdMoneyMap = new HashMap<Long, String>();
					if(list!=null&&!list.isEmpty()){
						Double tf = Double.valueOf(at-t)/100;
						for(Map<String, Object> map : list){
							Double itemTotal = StringUtils.formatDouble(map.get("total"));
							tf = tf -itemTotal;
							Long id = (Long)map.get("id");
							Long orderId = (Long)map.get("order_id");
							if(tf>=0){
								int r1 = daService.update("update order_tb set state = ? ,act_total=?,pay_type=? where id=? ",
										new Object[]{1,itemTotal,Integer.valueOf(paramMap.get("pay_type")),orderId});
								if(r1==1){
									int r2=daService.update("update no_payment_tb set state=?,pursue_time=? where id=? ", 
											new Object[]{1,System.currentTimeMillis()/1000,id});
									logger.error(">>>>�ӵ���Ӧ���볡����������ɣ����:"+orderId);
									if(r2==1){
										logger.error(">>>>�ӵ�����������ɣ����:"+id);
										//��Ҫд�복���˻����ӵ����
										comIdMoneyMap.put((Long)map.get("comid"), orderId+"_"+itemTotal);
									}else {
										logger.error(">>>>�ӵ���������ʧ�ܣ����:"+id+",ret��"+r2);
									}
								}else {
									logger.error(">>>>�ӵ���Ӧ���볡��������ʧ�ܣ����:"+orderId+",ret:"+r1);
								}
							}else{
								logger.error(">>>>�ӵ���������Ƿ�Ѷ�������Ƿ��:"+tf+",δ�ܽ�����ӵ���ţ�"+orderId);
							}
						}
					}
					if(paramMap.get("pay_type").equals("7")){
						payEscOrder(comIdMoneyMap,carNumber,daService);
					}
				}
				//�������㶩��
				paramMap.put("id", neworderId+"");
				paramMap.put("total",""+ Double.valueOf(total)/100);
				paramMap.put("state", "1");
				paramMap.put("c_type", "2");
				returnMap = zldUploadOperate.handleData(context,paramMap,params,"order_tb",0);
				logger.error("payorder >>>���㶩�����أ�"+returnMap);
				if(returnMap.get("status").equals("1")&&paramMap.get("pay_type").equals("7")){
					logger.error("payorder >>>��д�����궩������ʼд�˻���ϸ...");
					payNorOrder(Double.valueOf(t)/100,neworderId,carNumber,paramMap.get("comid"),daService);
				}
			}
		}
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	


	/**
	 * �ϴ�������
	 * http://127.0.0.1/zld/api/business/addblack
	 */
	@POST
	@Path("/addblack")//������
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void addblack(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"zld_black_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	@POST
	@Path("/uploadparkstatus")//ͣ����ʵʱ״̬�ϱ�
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadParkStatus(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		Map<String, Object> returnMap =  zldUploadOperate.handleData(context,paramMap,params,"com_parkstatus_tb",0);
		if(returnMap.get("status").equals("1")){
			try {
				DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
				int ret =daService.update("update com_info_tb set empty=?,share_number=? where id=? ", 
						new Object[]{Integer.valueOf(paramMap.get("empty")),Integer.valueOf(paramMap.get("total")),
						Long.valueOf(paramMap.get("comid"))}); 
				logger.error("uploadparkstatus,���³���״̬��"+ret);
			} catch (BeansException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		logger.error("uploadparkstatus result:"+returnMap);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	
	/**
	 * �շ�Ա���¸�
	 * http://127.0.0.1/zld/api/business/parkusercheck
	 */
	@POST
	@Path("/parkusercheck")//ͣ����ÿ���շѻ㱨�ӿ�
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void parkusercheck(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		if(paramMap.get("user_uuid")!=null){
			String uid =zldUploadOperate.getUserIdByUUID(paramMap.get("user_uuid"),context);
			paramMap.put("uid", uid);
			paramMap.remove("user_uuid");
		}
		if(paramMap.get("berthsec_uuid")!=null){
			String berthsecId =zldUploadOperate.getBerthsecIdIdByUUID(paramMap.get("berthsec_uuid"),context);
			paramMap.put("berthsec_id", berthsecId);
			paramMap.remove("berthsec_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"com_parkuser_check",0);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	
	/**
	 * �ϴ�������
	 * http://127.0.0.1/zld/api/business/uploadworkgroups
	 */
	@POST
	@Path("/uploadworkgroups")//ͣ����ÿ���շѻ㱨�ӿ�
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadWorkGroups(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("company_uuid")!=null){
			String companyId =zldUploadOperate.getCompanyIddByUUID(paramMap.get("company_uuid"),context);
			paramMap.put("company_id", companyId);
			paramMap.remove("company_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"work_group_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	/**
	 * �ϴ����׶���
	 * http://127.0.0.1/zld/api/business/addorder
	 */
	@POST
	@Path("/addorder")//�����ϴ�
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void addorder(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		if(paramMap.get("in_employeeid")!=null){
			String uid =zldUploadOperate.getUserIdByUUID(paramMap.get("in_employeeid"),context);
			paramMap.put("uid", uid);
			paramMap.remove("in_employeeid");
		}
		if(paramMap.get("out_employeeid")!=null){
			String outUid =zldUploadOperate.getComIdByParkUUID(paramMap.get("out_employeeid"),context);
			paramMap.put("out_uid", outUid);
			paramMap.remove("out_employeeid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"order_tb",0);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	/**
	 * �༭����
	 * http://127.0.0.1/zld/api/business/updateorder
	 */
	@POST
	@Path("/updateorder")//�༭�ϴ�
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void updateorder(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"order_tb",1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	

	
	/**
	 * ͣ����ÿ���շѻ㱨�ӿ�
	 * http://127.0.0.1/zld/api/business/uploadparkdaypay
	 */
	@POST
	@Path("/uploadparkdaypay")//ͣ����ÿ���շѻ㱨�ӿ�
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadparkdaypay(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"park_daypay_tb",0);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	/**
	 * 3.3.11ͣ����ÿ��ͣ�����㱨�ӿ�
	 * http://127.0.0.1/zld/api/business/uploadparkdaypay
	 */
	@POST
	@Path("/uploadparkdayuse")//ͣ����ÿ��ͣ�����㱨�ӿ�
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadparkdayuse(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		if(paramMap.get("park_uuid")!=null){
			String comid =zldUploadOperate.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			paramMap.put("comid", comid);
			paramMap.remove("park_uuid");
		}
		Map<String, Object> returnMap = zldUploadOperate.handleData(context,paramMap,params,"park_dayuse_tb",0);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	
	//�������㶩��
	private void payNorOrder(Double t,Long orderId, String carNumber,String comId,DataBaseService service) {
		writeToAccount(Long.valueOf(comId), orderId, t, carNumber, service);
	}

	//�����ӵ�
	private void payEscOrder(Map<Long,String> map, String carNumber,DataBaseService service) {
		if(map!=null&&!map.isEmpty()){
			for(Long comId : map.keySet()){
				if(comId>0){
					String orderIdMoney [] = map.get(comId).split("_");
					Double money = StringUtils.formatDouble(orderIdMoney[1]);
					Long orderId = Long.valueOf(orderIdMoney[0]);
					writeToAccount(comId, orderId, money, carNumber, service);
				}
			}
		}
	}
	//д���˻�
	private void writeToAccount(Long comId,Long orderId,Double money,String carNumber,DataBaseService service){
		Long ntime = System.currentTimeMillis()/1000;
		Map<String, Object> userMap = service.getMap("select uin from car_info_tb where car_number=? ",
				new Object[]{carNumber});
		Long uin = userMap!=null?(Long)userMap.get("uin"):-1L;
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map moneySetMap = service.getMap("select giveto from money_set_tb where comid=? ", new Object[]{comId});
		Integer giveTo =2;
		if(moneySetMap!=null&&moneySetMap.get("giveto")!=null){
			//'0:��˾�˻���1�������˻� ��2����Ӫ�����˻�';//Ĭ��д�������˻�
			giveTo = (Integer)moneySetMap.get("giveto");
		}
		if(giveTo==0){//д�������˻�
			Map<String, Object> parkAccountsqlMap =new HashMap<String, Object>();
			parkAccountsqlMap.put("sql", "insert into park_account_tb(comid,amount,type,create_time,remark,uid,source,orderid) values(?,?,?,?,?,?,?,?)");
			parkAccountsqlMap.put("values",  new Object[]{comId,money,0,ntime,"ͣ����",-1L,0,orderId});
			bathSql.add(parkAccountsqlMap);
			
			Map<String, Object> comSqlMap =new HashMap<String, Object>();
			comSqlMap.put("sql", "update com_info_tb  set total_money =total_money+?,money=money+? where id=?");
			comSqlMap.put("values", new Object[]{money,money,comId});
			bathSql.add(comSqlMap);
		}else {//д�������˻�
			Map groupMap = service.getMap("select groupid from com_info_tb where id =? ", new Object[]{comId});
			Long groupId =-1L;
			if(groupMap!=null&&groupMap.get("groupid")!=null)
				groupId = (Long)groupMap.get("groupid");
			if(groupId!=null&&groupId>0){
				Map<String, Object> groupAccountsqlMap =new HashMap<String, Object>();
				groupAccountsqlMap.put("sql", "insert into group_account_tb(comid,amount,type,create_time,remark,uid,source,orderid,groupid) values(?,?,?,?,?,?,?,?,?)");
				groupAccountsqlMap.put("values",  new Object[]{comId,money,0,ntime,"ͣ����",-1L,0,orderId,groupId});
				bathSql.add(groupAccountsqlMap);
				
				Map<String, Object> groupSqlMap =new HashMap<String, Object>();
				groupSqlMap.put("sql", "update org_group_tb  set balance =balance+? where id=?");
				groupSqlMap.put("values", new Object[]{money,groupId});
				bathSql.add(groupSqlMap);
			}else {//û�м��ű�Ż���д�������˻�
				Map<String, Object> parkAccountsqlMap =new HashMap<String, Object>();
				parkAccountsqlMap.put("sql", "insert into park_account_tb(comid,amount,type,create_time,remark,uid,source,orderid) values(?,?,?,?,?,?,?,?)");
				parkAccountsqlMap.put("values",  new Object[]{comId,money,0,ntime,"ͣ����",-1L,0,orderId});
				bathSql.add(parkAccountsqlMap);
				
				Map<String, Object> comSqlMap =new HashMap<String, Object>();
				comSqlMap.put("sql", "update com_info_tb  set total_money =total_money+?,money=money+? where id=?");
				comSqlMap.put("values", new Object[]{money,money,comId});
				bathSql.add(comSqlMap);
			}
		}
		//д�����˻�
		Map<String, Object> trueUsersqlMap =new HashMap<String, Object>();
		trueUsersqlMap.put("sql", "update user_info_tb set balance=balance-? where id=?");
		trueUsersqlMap.put("values", new Object[]{money, uin });
		bathSql.add(trueUsersqlMap);
		//д������ϸ
		Map<String, Object> userAccountsqlMap =new HashMap<String, Object>();
		userAccountsqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)");
		userAccountsqlMap.put("values", new Object[]{uin,money,1,ntime,"ͣ����",0,orderId});
		bathSql.add(userAccountsqlMap);
		boolean result= service.bathUpdate(bathSql);
		logger.error("payorder,д���˻���"+result);
	}
	
}
