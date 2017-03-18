package com.zld.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.zld.CustomDefind;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.CountPrice;
import com.zld.utils.ExecutorsUtil;
import com.zld.utils.HttpsProxy;
import com.zld.utils.SendMessage;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZLDType;


/**
 * memcached���ߣ�������²�Ʒ��֧����������ѯ���ƺ��� 
 * @author Administrator
 *
 */

@Repository
public class PublicMethods {

	
	private Logger logger = Logger.getLogger(PublicMethods.class);
	
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private LogService logService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private CommonMethods methods;
	

	/**
	 * ����ͣ��ȯ
	 * @param uin   �����˻�
	 * @param value ������
	 * @param number��������
	 * @param ptype ֧������ 0��� 1֧���� 2΢�ţ�4���+֧����,5���+΢��
	 * @return 
	 */
	public int buyTickets(Long uin, Integer value, Integer number,Integer ptype) {
		logger.error("buyticket>>>uin:"+uin+",value"+value+",number:"+number+",ptype:"+ptype);
		boolean isAuth = isAuthUser(uin);
		//�ۿ�
		Double discount = Double.valueOf(CustomDefind.getValue("NOAUTHDISCOUNT"));
		if(isAuth){
			discount=Double.valueOf(CustomDefind.getValue("AUTHDISCOUNT"));
		}
		logger.error("buyticket>>>uin:"+uin+",discount"+discount);
		 //�˻����֧��
		Double balance =null;
		Map userMap = null;
		//������ʵ�˻����
		userMap = daService.getPojo("select balance,wxp_openid from user_info_tb where id =?",	new Object[]{uin});
		if(userMap!=null&&userMap.get("balance")!=null){
			balance = Double.valueOf(userMap.get("balance")+"");
		}
		//ÿ��Ӧ�����
		Double etotal =  StringUtils.formatDouble(value*discount);
		//Ӧ�����
		Double total = StringUtils.formatDouble(etotal*number);
		logger.error(uin+",balance:"+balance+",total:"+total);
		logger.error("buyticket>>>uin:"+uin+",discount"+discount+",total:"+total+",balance:"+balance);
		if(total>balance){//����
			logger.error(uin+",balance:"+balance+",total:"+total+",����");
			return -1;
		}
		
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		//�����û����
		Map<String, Object> userSqlMap = new HashMap<String, Object>();
		//�����û����
		Map<String, Object> userAccSqlMap = new HashMap<String, Object>();
		
		Long ntime = System.currentTimeMillis()/1000;
		Long ttime = TimeTools.getToDayBeginTime();
	    userSqlMap.put("sql", "update user_info_tb  set balance =balance-? where id=?");
		userSqlMap.put("values", new Object[]{total,uin});
		bathSql.add(userSqlMap);
		
		userAccSqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,target) values(?,?,?,?,?,?,?)");
		userAccSqlMap.put("values", new Object[]{uin,total,1,ntime,"����ͣ��ȯ("+number+"��"+value+"Ԫ)",ptype,2});
		bathSql.add(userAccSqlMap);
		if(number > 0){
			for(int i=0;i<number;i++){
				Map<String, Object> ticketSqlMap = new HashMap<String, Object>();
				ticketSqlMap.put("sql", "insert into ticket_tb (create_time,limit_day,money,pmoney,state,uin,type,resources) values(?,?,?,?,?,?,?,?)" );
				ticketSqlMap.put("values",new Object[]{ttime,ttime+31*24*60*60-1,value,etotal,0,uin,0,1});
				bathSql.add(ticketSqlMap);
			}
		}
		boolean result = daService.bathUpdate(bathSql);
		logger.error("uin:"+uin+",value:"+value+",number:"+number+",result:"+result);
		if(result){
			return 1;
		} else {
			return 0;
		}
	}
	
	
	/**
	 * ���������շ�Ա
	 * @param uin
	 * @param uid
	 * @param orderId
	 * @param ticketId
	 * @param money
	 * @param comId
	 * @param ptype 0��1֧������2΢�ţ�4���+֧����,5���+΢��,7ͣ��ȯ 
	 * @return
	 */
	public int doparkUserReward(Long uin,Long uid,Long orderId,Long ticketId,Double money,Integer ptype,Integer bind_flag) {
		logger.error("doparkUserReward>>>uin:"+uin+",uid:"+uid+",orderid:"+orderId+",money:"+money+",ptype"+ptype+",bind_flag:"+bind_flag);
		Long comId = daService.getLong("select comid from user_info_tb where id=? ", new Object[]{uid});
		//��ͣ��ȯ���
		Long count = daService.getLong("select count(id) from parkuser_reward_tb where uin=? and order_id=? ", new Object[]{uin,orderId});
		if(count>0){
			logger.error("�Ѵ��͹�>>>uin:"+uin+",orderid:"+orderId+",uid:"+uid);
			//�Ѵ��͹�
			return -2;
		}
		Long ntime = System.currentTimeMillis()/1000;
		Double ticketMoney=0.0;
		if(ticketId != null && ticketId>0){
			ticketMoney = getTicketMoney(ticketId, 4, uid, money, 2, comId, orderId);
		}
		logger.error("uin:"+uin+",uid:"+uid+",orderid:"+orderId+",ticketMoney:"+ticketMoney+",money:"+money+",ticketid:"+ticketId);
		//���û����
		Map<String, Object> userMap = null;
		Double ubalance =null;
		//������ʵ�˻����
		if(bind_flag == 1){
			userMap = daService.getPojo("select balance from user_info_tb where id =?",	new Object[]{uin});
		}else{
			userMap = daService.getPojo("select balance from wxp_user_tb where uin=? ", new Object[]{uin});
		}
		if(userMap!=null&&userMap.get("balance")!=null){
			ubalance = Double.valueOf(userMap.get("balance")+"");
			logger.error(":uin:"+uin+",uid:"+uid+",orderid:"+orderId+",ubalance:"+ubalance+",ticketMoney:"+ticketMoney);
			ubalance +=ticketMoney;//�û��������Ż�ȯ���
		}
		if(ubalance==null||ubalance<money){//�ʻ�����
			logger.error("�����˻����㣬�˻���"+ubalance+",���ͷѽ�"+money+",uin:"+uin+",orderid:"+orderId+",ticketMoney:"+ticketMoney);
			return -1;
		}
		logger.error("uin:"+uin+",orderid:"+orderId+",uid:"+uid+",ticketMoney:"+ticketMoney);
		
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		//ͣ��ȯ
		Map<String, Object> ticketSqlMap = new HashMap<String, Object>();
		//�����û����
		Map<String, Object> userSqlMap = new HashMap<String, Object>();
		//�û��˻�
		Map<String, Object> userAccSqlMap = new HashMap<String, Object>();
		//�շ�Ա���
		Map<String, Object> parkuserSqlMap = new HashMap<String, Object>();
		//�շ�Ա�˻�
		Map<String, Object> parkuserAccSqlMap = new HashMap<String, Object>();
		//ͣ�����˻�
		Map<String, Object> tingchebaoAccountsqlMap = new HashMap<String, Object>();
		//���ͼ�¼
		Map<String, Object> prakuserRewardsqlMap = new HashMap<String, Object>();
		
		Map<String, Object> userTicketAccountsqlMap = new HashMap<String, Object>();
		//���ͻ���
		Map<String, Object> rewardsqlMap = new HashMap<String, Object>();
		//���ͻ�����ϸ
		Map<String, Object> rewardAccountsqlMap = new HashMap<String, Object>();
		
		String carNumber = getCarNumber(uin);
		if(ticketMoney>0){//����ͣ��ȯ
			ticketSqlMap.put("sql", "update ticket_tb  set state=?,comid=?,utime=?,umoney=?,orderid=? where id=?");
			ticketSqlMap.put("values", new Object[]{1,comId,ntime,ticketMoney,orderId,ticketId});
			bathSql.add(ticketSqlMap);
			
			tingchebaoAccountsqlMap.put("sql", "insert into tingchebao_account_tb(amount,type,create_time,remark,utype,orderid) values(?,?,?,?,?,?)");
			tingchebaoAccountsqlMap.put("values", new Object[]{ticketMoney,1,ntime,"����"+carNumber+"��ʹ��ͣ������ȯ�����շ�Ա"+uid,7,orderId});
			bathSql.add(tingchebaoAccountsqlMap);
		}
		if(money>ticketMoney){//Ҫ�������֧��
			if(bind_flag == 1){
				userSqlMap.put("sql", "update user_info_tb  set balance =balance-? where id=?");
			}else{
				userSqlMap.put("sql", "update wxp_user_tb  set balance =balance-? where uin=?");
			}
			userSqlMap.put("values", new Object[]{money-ticketMoney,uin});
			bathSql.add(userSqlMap);
		}
		
		userAccSqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)");
		userAccSqlMap.put("values", new Object[]{uin,money,1,ntime,"�����շ�Ա-"+uid,ptype,orderId});
		bathSql.add(userAccSqlMap);
		
		if(ticketMoney>0&&ticketId!=null){//ʹ��ͣ��ȯ���������˻��ȳ�ֵ
			userTicketAccountsqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,orderid) values(?,?,?,?,?,?,?)");
			userTicketAccountsqlMap.put("values", new Object[]{uin,ticketMoney,0,ntime-1,"����-ͣ��ȯ��ֵ",7,orderId});
			bathSql.add(userTicketAccountsqlMap);
		}
		
		//�����շ�Ա�˻�
		parkuserSqlMap.put("sql", "update user_info_tb  set balance =balance+? where id=?");
		parkuserSqlMap.put("values", new Object[]{money,uid});
		bathSql.add(parkuserSqlMap);
		
		parkuserAccSqlMap.put("sql", "insert into parkuser_account_tb(uin,amount,type,create_time,remark,target,orderid) values(?,?,?,?,?,?,?)");
		parkuserAccSqlMap.put("values", new Object[]{uid,money,0,ntime,"���ͷ�_"+carNumber,4,orderId});
		bathSql.add(parkuserAccSqlMap);
		
		Long rewardId = daService.getkey("seq_parkuser_reward_tb");
		prakuserRewardsqlMap.put("sql", "insert into parkuser_reward_tb(id,uin,uid,money,ctime,comid,order_id,ticket_id) values(?,?,?,?,?,?,?,?)");
		prakuserRewardsqlMap.put("values", new Object[]{rewardId,uin,uid,money,ntime,comId,orderId,ticketId});
		bathSql.add(prakuserRewardsqlMap);
		
		/*//���ͻ���
		Long btime = TimeTools.getToDayBeginTime();
		Long rewardCount = daService.getLong("select count(id) from parkuser_reward_tb where uid=? and ctime>=? ",
				new Object[] { uid, btime });
		Map<String, Object> tscoreMap = daService.getMap("select sum(score) tscore from reward_account_tb where type=? and create_time>? and uin=? ", new Object[]{0, btime, uid});
		Double tscore = 0d;
		Double rscore = (rewardCount+1)*money;
		if(tscoreMap != null && tscoreMap.get("tscore") != null){
			tscore = Double.valueOf(tscoreMap.get("tscore") + "");
		}
		logger.error("�շ�Ա���ջ������룺uid:"+uid+",tscore:"+tscore+",���λ���:"+rscore+",rewardCount:"+rewardCount);
		if(tscore < 5000){//ÿ���������5000����
			if(tscore + rscore > 5000){
				rscore = 5000 - tscore;
				logger.error("���ջ����Ѿ������ޣ�tscore:"+tscore+",rscore:"+rscore+",uid:"+uid);
			}
			rewardsqlMap.put("sql", "update user_info_tb set reward_score=reward_score+? where id=? ");
			rewardsqlMap.put("values", new Object[]{rscore, uid});
			bathSql.add(rewardsqlMap);
			
			rewardAccountsqlMap.put("sql", "insert into reward_account_tb(uin,score,type,create_time,target,reward_id,remark) values(?,?,?,?,?,?,?) ");
			rewardAccountsqlMap.put("values", new Object[]{uid, rscore, 0, ntime, 0, rewardId,"���� "+carNumber});
			bathSql.add(rewardAccountsqlMap);
		}*/
		boolean result = daService.bathUpdate(bathSql);
		logger.error("uin:"+uin+",uid:"+uid+",orderid:"+orderId+",result:"+result);
		if(result){
			if(ticketId > 0){//���洦��
				Map<Long, Long> tcacheMap = memcacheUtils.doMapLongLongCache("reward_userticket_cache", null, null);
				Long ttime = TimeTools.getToDayBeginTime();
				if(tcacheMap!=null){
					tcacheMap.put(uin, ttime);
				}else {
					tcacheMap = new HashMap<Long, Long>();
					tcacheMap.put(uin, ttime);
				}
				memcacheUtils.doMapLongLongCache("reward_userticket_cache", tcacheMap, "update");
			}
			
			if(ticketMoney > 0){//����ÿ�ղ�������
				updateAllowCache(comId, ticketId, ticketMoney);
				logger.error("update allowance today>>>uin:"+uin+",orderid:"+orderId+",ticketMoney:"+ticketMoney);
			}
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * 
	 * @param uin �û��ʺ�
	 * @param pid ��Ʒ��š�
	 * @param number ��������
	 * @param start ��ʼʱ�䣬��ʽ��20140815
	 * @param ptype :0��1֧������2΢�ţ�3������4���+֧����,5���+΢��,6���+����
	 * @return  0ʧ��,1�ɹ���-1����
	 */
	public int buyProducts(Long uin,Map productMap,Integer number,String start,int ptype){
//		Map productMap = daService.getPojo("select * from product_package_tb where id=? and state=? and remain_number>?",
//				new Object[]{pid,0,0});
//		if(productMap==null||productMap.isEmpty())
//			return 0;
		Long pid = (Long)productMap.get("id");
		//1��ѯ���
		BigDecimal _balance  = (BigDecimal)daService.getObject("select balance from user_info_Tb where id=?",
				new Object[]{uin}, BigDecimal.class);
		Double balance = 0d;
		if(_balance!=null)
			balance = _balance.doubleValue();
		logger.error("��ǰ�ͻ���"+balance);
		Double price = Double.valueOf(productMap.get("price")+"");
		//logger.error("��Ʒ�۸�:"+price);
		//2���û����
		//3����ͣ�����ʺŽ��
		//�Ǽ��û����²�Ʒ
		logger.error("��Ʒ�۸�:"+price);
		
		Long comid = (Long)productMap.get("comid");
		
		boolean b = false;
		Double total = number*price;
		String time = start.substring(0,4)+"-"+start.substring(4,6)+"-"+start.substring(6);
		Long btime = TimeTools.getLongMilliSecondFrom_HHMMDD(time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(btime);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+Integer.valueOf(number));
		Long etime = calendar.getTimeInMillis();
		if(balance>=total){//�����Թ����Ʒ
			List<Map<String , Object>> sqlMaps = new ArrayList<Map<String,Object>>();
			logger.error ("�����Թ����Ʒ...");
			
			Map<String, Object> usersqlMap = new HashMap<String, Object>();
			usersqlMap.put("sql", "update user_info_tb set balance = balance-? where id=? ");
			usersqlMap.put("values", new Object[]{total,uin});
			sqlMaps.add(usersqlMap);
			
			Map<String, Object> userAccountsqlMap = new HashMap<String, Object>();
			userAccountsqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type) values(?,?,?,?,?,?)");
			userAccountsqlMap.put("values", new Object[]{uin,total,1,System.currentTimeMillis()/1000,"����-"+productMap.get("p_name"),ptype});
			sqlMaps.add(userAccountsqlMap);
			
			Map<String, Object> comsqlMap = new HashMap<String, Object>();
			comsqlMap.put("sql", "update com_info_tb set money=money+?,total_money=total_money+? where id=? ");
			comsqlMap.put("values", new Object[]{total,total,comid});
			sqlMaps.add(comsqlMap);
			
			Map<String, Object> buysqlMap = new HashMap<String, Object>();
			buysqlMap.put("sql", "insert into carower_product(pid,uin,create_time,b_time,e_time,total) values(?,?,?,?,?,?)");
			buysqlMap.put("values", new Object[]{pid,uin,System.currentTimeMillis()/1000,btime/1000,etime/1000-1,total});
			sqlMaps.add(buysqlMap);
			
			Map<String, Object> ppSqlMap =new HashMap<String, Object>();
			ppSqlMap.put("sql", "update product_package_tb set remain_number=remain_number-? where id=?");
			ppSqlMap.put("values", new Object[]{1,pid});
			sqlMaps.add(ppSqlMap);
			
			b= daService.bathUpdate(sqlMaps);
			logger.error("�����Ʒ�ɹ�...");
		}else 
			return -1;
		//4д�û��۷���־
		//5д������ֵ��־
		if(b){//����ɹ�
			Map comMap = daService.getMap("select company_name from com_info_tb where id = ?", new Object[]{productMap.get("comid")});
			daService.update( "insert into money_record_tb(comid,create_time,amount,uin,type,remark,pay_type) values (?,?,?,?,?,?,?)", 
					new Object[]{productMap.get("comid"),System.currentTimeMillis()/1000,total,uin,
					ZLDType.MONEY_CONSUM,comMap.get("company_name")+" ����-"+productMap.get("p_name"),ptype});
			logger.error("��ֵ��־д��ɹ�...");
			//���Ͷ��� ,��������Ա������;
			
			Map userMap1 = daService.getMap("select mobile from user_info_tb where id=? ",new Object[]{uin});
			Map userMap2 = daService.getMap("select mobile,nickname from user_info_tb where comid=? and auth_flag=? limit ?", new Object[]{productMap.get("comid"),1,1});
			
			String umobile = userMap1.get("mobile")==null?"":userMap1.get("mobile")+"";//(String)daService.getObject("select mobile from user_info_tb where id=? ",new Object[]{uin},String.class);
			String pmobile = userMap2.get("mobile")==null?"":userMap2.get("mobile")+"";//(String)daService.getObject("select mobile from user_info_tb where comid=? and auth_flag=? ", new Object[]{productMap.get("comid"),1},String.class);
			String puserName = userMap2.get("nickname")==null?"":userMap2.get("nickname")+"";
			
			String exprise = "";
			//List userList = daService.getAll("select mobile,nickname,id from user_info_tb where (comid=? or id=?) ", new Object[]{uin});
			
			if(!umobile.equals(""))
				exprise = TimeTools.getTimeStr_yyyy_MM_dd(btime)+"��"+TimeTools.getTimeStr_yyyy_MM_dd(etime);
			String carNumber ="";
			if(!umobile.equals("")||!pmobile.equals(""))
				carNumber = getCarNumber(uin);//(String)daService.getObject("select id,car_number from car_info_tb where uin=? ", new Object[]{uin},String.class);
			//��ʼ������
			if(!umobile.equals("")&&Check.checkMobile(umobile));
//				SendMessage.sendMessage(umobile, "�𾴵�"+carNumber+"�������ã�����ͨ��ͣ��������"+comMap.get("company_name")+"���·��񣬷���"+total+"Ԫ����Ч��"+exprise+
//						"��������ƾ�˶��ŵ�"+comMap.get("company_name")+"��ȡ��Ӧ�¿����ͷ���01053618108 ��ͣ������");
				SendMessage.sendMultiMessage(umobile, "�𾴵�"+carNumber+"�������ã�����ͨ��ͣ��������"+comMap.get("company_name")+"���·��񣬷���"+total+"Ԫ����Ч��"+exprise+
						"��������ƾ�˶��ŵ�"+comMap.get("company_name")+"��ȡ��Ӧ�¿���ȷ���¿�����������ʱ�������ǰ�͸ó���������"+puserName+"(�ֻ���"+pmobile+")��ϵ���������ʿ���ѯͣ�����ͷ���01053618108 ��ͣ������");
				
				
			if(!pmobile.equals("")&&Check.checkMobile(pmobile))
				SendMessage.sendMultiMessage(pmobile,"�𾴵ĺ���������ã�����"+carNumber+"(�ֻ���"+umobile+")��ͨ��ͣ��������󳵳����·���1���£�����"+total+"Ԫ���������ں�̨�鿴��Ӧ��Ϣ��"+
						"������ƾ����ǰ����ȡ�¿�����������ǰ��֮��ϵȷ����Ӧ��Ϣ����������Ӧ�¿���лл���ͷ���01053618108 ��ͣ������");
				
//				SendMessage.sendMessage(pmobile, "�𾴵ĺ���������ã�����"+carNumber+"��ͨ��ͣ��������󳵳����·���1���£�����"+total+"Ԫ���������ں�̨�鿴��Ӧ��Ϣ��"+
//						"������ƾ����ǰ����ȡ�¿����뱸����Ӧ�¿���лл���ͷ���01053618108 ��ͣ������");
			return 1;
		}
		return 0;
	}
	

	/**
	 * ����Ԥ���Ѷ���
	 * @param orderMap ����
	 * @param total ʵ�ս��
	 * @return 0ʧ�� 1�ɹ�
	 */
	public Map<String, Object> doMidPayOrder(Map<String, Object> orderMap, Double total, Long uid){
		logger.error("�����ֽ�Ԥ֧������doMidPayOrder��orderid:"+orderMap.get("id")+",Ԥ֧����"+orderMap.get("total")+",uin:"+orderMap.get("uin")+",ͣ���ѽ�total:"+total+",car_number:"+orderMap.get("car_number"));
		Double prefee = Double.valueOf(orderMap.get("total") + "");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Long orderid = (Long)orderMap.get("id");
		Long comid = (Long)orderMap.get("comid");
		Integer state = (Integer)orderMap.get("state");
		Long create_time = (Long)orderMap.get("create_time");
		Integer car_type = (Integer)orderMap.get("car_type");//0��ͨ�ã�1��С����2����
		Integer pid = (Integer)orderMap.get("pid");
		if(state == 1){
			logger.error("doMidPayOrder>>>>orderid:"+orderid+",������֧�������أ�");
			resultMap.put("result", -1);
			return resultMap;
		}
		Long ntime = System.currentTimeMillis()/1000;
		
		//������ȯʹ�����
		Double distotal = 0d;
		Double umoney = 0d;
		Map<String, Object> shopticketMap = daService
				.getMap("select * from ticket_tb where (type=? or type=?) and orderid=? ",
						new Object[] { 3, 4, orderMap.get("id") });
		if(shopticketMap != null){
			Integer type = (Integer)shopticketMap.get("type");
			Integer money = (Integer)shopticketMap.get("money");
			umoney = Double.valueOf(shopticketMap.get("umoney") + "");
			Long end_time = ntime;
			logger.error("doMidPayOrder>>>>>:orderid:"+orderid+",shopticketid:"+shopticketMap.get("id")+",type:"+type+",umoney:"+umoney);
			if(type == 4){//ȫ��
				distotal = total;
				logger.error("doMidPayOrder>>>>ȫ��ȯ:orderid:"+orderid+",distotal:"+distotal);
			}else if(type == 3){
				if(create_time + money * 60 * 60 > end_time){
					distotal = total;
					logger.error("doMidPayOrder>>>>��ʱȯ:orderid:"+orderid+",distotal:"+distotal);
				}else{
					end_time = end_time - money * 60 *60;
					Double dtotal = 0d;
					if(pid>-1){
						dtotal = Double.valueOf(getCustomPrice(create_time, end_time, pid));
					}else {
						dtotal = Double.valueOf(getPrice(create_time, end_time, comid, car_type));
					}
					if(total > dtotal){
						distotal = StringUtils.formatDouble(total - dtotal);
					}
					logger.error("doMidPayOrder>>>>��ʱȯ:orderid:"+orderid+",distotal="+distotal);
				}
			}
			resultMap.put("ticket_type", type);
		}
		resultMap.put("distotal", distotal);
		
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		//���¶���״̬���շѳɹ�
		Map<String, Object> orderSqlMap = new HashMap<String, Object>();
		//������ȯ���
		Map<String, Object> ticketSqlMap = new HashMap<String, Object>();
		
		orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=? where id=?");
		orderSqlMap.put("values", new Object[]{1,total,System.currentTimeMillis()/1000,orderid});
		bathSql.add(orderSqlMap);
		if(shopticketMap != null){
			ticketSqlMap.put("sql", "update ticket_tb set bmoney=? where id=?");
			ticketSqlMap.put("values", new Object[]{distotal, shopticketMap.get("id")});
			bathSql.add(ticketSqlMap);
		}
		prefee = StringUtils.formatDouble(prefee - umoney + distotal);//����ʵ��Ԥ֧�����
		logger.error("doMidPayOrder>>>>>:���¼�����Ԥ֧�����prefee:"+prefee+",orderid:"+orderid);
		resultMap.put("prefee", prefee);
		if(prefee < total){
			//���ֽ��¼
			Map<String, Object> cashsqlMap = new HashMap<String, Object>();
			cashsqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,type,orderid,create_time) values(?,?,?,?,?)");
			cashsqlMap.put("values", new Object[]{uid, total - prefee, 0, orderid, System.currentTimeMillis()/1000});
			bathSql.add(cashsqlMap);
		}
		boolean result= daService.bathUpdate(bathSql);
		logger.error("doMidPayOrder>>>>,orderid:"+orderid+",result:"+result);
		if(result){
			resultMap.put("result", 1);
			return resultMap;
		}
		resultMap.put("result", -1);
		return resultMap;
	}
	
	private boolean backWeixinTicket(Double money, Long orderId, Long uin){
		Integer bonus = 5;//5��
		if(money>=1&&memcacheUtils.readBackTicketCache(uin)){//һ��ֻ��һ�κ��
			String sql = "insert into order_ticket_tb (uin,order_id,money,bnum,ctime,exptime,bwords,type) values(?,?,?,?,?,?,?,?)";
			Object []values = null;
			Long ctime = System.currentTimeMillis()/1000;
			Long exptime = ctime + 24*60*60;
			values = new Object[]{uin,orderId,bonus,5,ctime,exptime,"΢��֧������ȯ",1};
			logger.error(">>>>>΢��Ԥ֧�����,5��"+bonus+"��ȯ...");
			int ret = daService.update(sql, values);
			logger.error(">>>>>΢��Ԥ֧����� ret :"+ret);
			if(ret==1){
				memcacheUtils.updateBackTicketCache(uin);
				return true;
			}
		}else {
			if(money< 1){
				logger.error(">>>>>>>>֧�����С��1Ԫ���������>>>>>>uin:"+uin+",orderid:"+orderId+",money:"+money);
			}else if(!memcacheUtils.readBackTicketCache(uin)){
				logger.error(">>>>>>>>һ��ֻ��һ�κ�����Ѿ��������������>>>>>>uin:"+uin+",orderid:"+orderId+",money:"+money);
			}
			logger.error(">>>>>΢��֧�����,�Ѿ�������������.....");
		}
		return false;
		
	}

	/**
	 * �鳵�ƺ�
	 * @param uin
	 * @return
	 */
	public String getCarNumber(Long uin){
		String carNumber="���ƺ�δ֪";//�������ƺ�
		Map carNuberMap = daService.getPojo("select car_number from car_info_tb where uin=? and state=?  ", 
				new Object[]{uin,1});
		if(carNuberMap!=null&&carNuberMap.get("car_number")!=null&&!carNuberMap.get("car_number").toString().equals(""))
			carNumber = (String)carNuberMap.get("car_number");
		return carNumber;
	}
	
	
	public Map getPriceMap(Long comid){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		//��ʼСʱ
		int bhour = calendar.get(Calendar.HOUR_OF_DAY);
		List<Map<String, Object>> priceList=pgOnlyReadService.getAll("select * from price_tb where comid=? " +
				"and state=? and pay_type=? order by id desc", new Object[]{comid,0,0});
		if(priceList==null||priceList.size()==0){//û�а�ʱ�β���
			//�鰴�β���
			priceList=pgOnlyReadService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? order by id desc", new Object[]{comid,0,1});
			if(priceList==null||priceList.size()==0){//û�а��β��ԣ�������ʾ
				return null;
			}else {//�а��β��ԣ�ֱ�ӷ���һ�ε��շ�
				 return priceList.get(0);
			}
			//�����Ÿ�����Ա��ͨ�����úü۸�
		}else {//�Ӱ�ʱ�μ۸�����зּ���ռ��ҹ���շѲ���
			if(priceList.size()>0){
				for(Map map : priceList){
					Integer btime = (Integer)map.get("b_time");
					Integer etime = (Integer)map.get("e_time");
					if(btime<etime){//�ռ�
						if(bhour>=btime&&bhour<etime)
							return map;
					}else {
						if((bhour>=btime&&bhour<24)||(bhour>=0&&bhour<etime))
							return map;
					}
				}
			}
		}
		return null;
	}
	/**
	 * ���㶩�����
	 * @param start
	 * @param end
	 * @param comId
	 * @param car_type 0��ͨ�ã�1��С����2����
	 * @return �������_�Ƿ��Ż�
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public  String getPrice(Long start,Long end,Long comId,Integer car_type){
//		String pid = CustomDefind.CUSTOMPARKIDS;
//		if(pid.equals(comId.toString())){//���Ƽ۸����
//			return "������";
//		}
//		
		if(car_type == 0){//0:ͨ��
			Long count = daService.getLong("select count(*) from com_info_tb where id=? and car_type=?", new Object[]{comId,1});
			if(count > 0){//���ִ�С��
				car_type = 1;//Ĭ�ϳ�С���ƷѲ���
			}
		}
		Map priceMap1=null;
		Map priceMap2=null;
		List<Map<String, Object>> priceList=pgOnlyReadService.getAll("select * from price_tb where comid=? " +
				"and state=? and pay_type=? and car_type=? order by id desc", new Object[]{comId,0,0,car_type});
		if(priceList==null||priceList.size()==0){
			//�鰴�β���
			priceList=pgOnlyReadService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? and car_type=? order by id desc", new Object[]{comId,0,1,car_type});
			if(priceList==null||priceList.size()==0){//û���κβ���
				return "0.0";
			}else {//�а��β��ԣ�����N�ε��շ�
				Map timeMap =priceList.get(0);
				Object ounit  = timeMap.get("unit");
				Double total = Double.valueOf(timeMap.get("price")+"");
				try {
					if(ounit!=null){
						Integer unit = Integer.valueOf(ounit.toString());
						if(unit>0){
							Long du = (end-start)/60;//ʱ����
							int times = du.intValue()/unit;
							if(du%unit!=0)
								times +=1;
							total = times*total;
							
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				return StringUtils.formatDouble(total)+"";
			}
		}else {
			priceMap1=priceList.get(0);
			boolean pm1 = false;//�ҵ�map1,�����ǽ���ʱ����ڿ�ʼʱ��
			boolean pm2 = false;//�ҵ�map2
			Integer payType = (Integer)priceMap1.get("pay_type");
			if(payType==0&&priceList.size()>1){
				for(Map map : priceList){
					if(pm1&&pm2)
						break;
					payType = (Integer)map.get("pay_type");
					Integer btime = (Integer)map.get("b_time");
					Integer etime = (Integer)map.get("e_time");
					if(payType==0&&etime>btime){
						if(!pm1){
							priceMap1 = map;
							pm1=true;
						}else {
							priceMap2=map;
							pm2=true;
						}
					}else {
						if(!pm2){
							priceMap2=map;
							pm2=true;
						}
					}
				}
			}
		}
		double minPriceUnit = getminPriceUnit(comId);
		Map assistMap = daService.getMap("select * from price_assist_tb where comid = ? and type = ?", new Object[]{comId,0});
		Map orderInfp = CountPrice.getAccount(start, end, priceMap1, priceMap2,minPriceUnit,assistMap);
		
		//Double count= StringUtils.getAccount(start, end, priceMap1, priceMap2);
		return StringUtils.formatDouble(orderInfp.get("collect"))+"";	
	}
	/**
	 * 
	 * @param start
	 * @param end
	 * @param pid �Ʒѷ�ʽ��0��ʱ(0.5/15����)��1���Σ�12Сʱ��10Ԫ,ǰ1/30min����ÿСʱ1Ԫ��
	 * @return
	 */
	public String getCustomPrice(Long start,Long end,Integer pid) {
		/**һԪ/��Сʱ     12Сʱ�ڷⶥ10Ԫ��12Сʱ��ÿ��һСʱ����һԪ��*/
		logger.error(">>>>>>���Ƽ۸񳵳�,pid(0��ʱ(0.5/15����)��1���Σ�12Сʱ��10Ԫ,ǰ1/30min����ÿСʱ1Ԫ��)="+pid);
		Long duration = (end-start)/60;//����
		Long hour = duration/(60);//Сʱ��;
		if(pid==0){
			Long t = duration/15;
			if(duration%15!=0)
				t= t+1;
			return StringUtils.formatDouble(t*0.5)+"";
		}else if(pid==1){
			if(duration%60!=0)
				hour = hour+1;
			if(hour<12){
				if(hour<6){
					Long tLong = duration/30;
					if(duration%30!=0)
						tLong += 1L;
					return StringUtils.formatDouble(tLong)+"";
				}
				else 
					return 10.0+"";
			}else {
				return 10.0+(hour-12)+"";
			}
		}else {
			return "0";
		}
	}


	//@SuppressWarnings({ "rawtypes", "unchecked" })
	public String handleOrder(Long comId,Map orderMap) throws Exception{
		Map dayMap=null;//�ռ����
		Map nigthMap=null;//ҹ�����
		//��ʱ�μ۸����
		List<Map<String ,Object>> priceList=null;//SystemMemcachee.getPriceByComid(comId);
		priceList=pgOnlyReadService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? order by id desc", new Object[]{comId,0,0});
		Long ntime = System.currentTimeMillis()/1000;
		if(priceList==null||priceList.size()==0){//û�а�ʱ�β���
			//�鰴�β���
			priceList=pgOnlyReadService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? order by id desc", new Object[]{comId,0,1});
			Long btLong = (Long)orderMap.get("create_time");
			String btime = TimeTools.getTime_MMdd_HHmm(btLong*1000).substring(6);
			String etime = TimeTools.getTime_MMdd_HHmm(ntime*1000).substring(6);
			Map<String, Object> orMap=new HashMap<String, Object>();
			Long start = (Long)orderMap.get("create_time");
			Long end = ntime;
			orMap.put("btime", btime);
			orMap.put("etime", etime);
			orMap.put("duration", StringUtils.getTimeString(start, end));
			orMap.put("orderid", orderMap.get("id"));
			orMap.put("carnumber",orderMap.get("car_number")==null?"���ƺ�δ֪": orderMap.get("car_number"));
			orMap.put("handcash", "0");
			orMap.put("uin", orderMap.get("uin"));
			if(priceList==null||priceList.size()==0){//û�а��β��ԣ�������ʾ
				//���ظ��շ�Ա���ֹ�����۸�
				orMap.put("total", "0.00");
				orMap.put("collect", "0.00");
				orMap.put("handcash", "1");
			}else {//�а��β��ԣ�ֱ�ӷ���һ�ε��շ�
				Map timeMap =priceList.get(0);
				Object ounit  = timeMap.get("unit");
//				orMap.put("btime", btime);
//				orMap.put("etime", etime);
//				orMap.put("duration", StringUtils.getTimeString(start, end));
//				orMap.put("orderid", orderMap.get("id"));
//				orMap.put("carnumber",orderMap.get("car_number")==null?"���ƺ�δ֪": orderMap.get("car_number"));
//				
				orMap.put("collect", timeMap.get("price"));
				orMap.put("total", timeMap.get("price"));
				if(ounit!=null){
					Integer unit = Integer.valueOf(ounit.toString());
					if(unit>0){
						Long du = (end-start)/60;//ʱ����
						int times = du.intValue()/unit;
						if(du%unit!=0)
							times +=1;
						double total = times*Double.valueOf(timeMap.get("price")+"");
						orMap.put("collect", total);
						orMap.put("total", total);
					}
				}
			}
			return StringUtils.createJson(orMap);
		}else {//�Ӱ�ʱ�μ۸�����зּ���ռ��ҹ���շѲ���
			dayMap= priceList.get(0);
			boolean pm1 = false;//�ҵ�map1,�����ǽ���ʱ����ڿ�ʼʱ��
			boolean pm2 = false;//�ҵ�map2
			if(priceList.size()>1){
				for(Map map : priceList){
					if(pm1&&pm2)
						break;
					Integer btime = (Integer)map.get("b_time");
					Integer etime = (Integer)map.get("e_time");
					if(btime==null||etime==null)
						continue;
					if(etime>btime){
						if(!pm1){
							dayMap = map;
							pm1=true;
						}
					}else {
						if(!pm2){
							nigthMap=map;
							pm2=true;
						}
					}
				}
			}
		}
		double minPriceUnit = getminPriceUnit(comId);
		
		Map assistMap = daService.getMap("select * from price_assist_tb where comid = ? and type = ?", new Object[]{comId,0});
		
		Map<String, Object> orMap=CountPrice.getAccount((Long)orderMap.get("create_time"),ntime, dayMap, nigthMap,minPriceUnit,assistMap);
		orMap.put("orderid", orderMap.get("id"));
		orMap.put("uin", orderMap.get("uin"));
		String hascard = "1";//�Ƿ��г���
		String carNumber = (String)orderMap.get("car_number");
		if(carNumber==null||carNumber.toString().trim().equals("")){
			carNumber="���ƺ�δ֪";
			hascard = "0";
		}
		orMap.put("carnumber",carNumber);
		orMap.put("hascard", hascard);
		orMap.put("handcash", "0");
		orMap.put("car_type", orderMap.get("car_type"));
		logger.error("���㶩�������أ�"+orMap);
		return StringUtils.createJson(orMap);	
	}
	
	/**
	 * ֧�ֶ�۸����//20141118
	 * //V1115���ϰ汾ʵ�ְ��²�Ʒ����۸���Ե�֧��
	 * @param comId
	 * @param orderMap
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getOrderPrice(Long comId,Map orderMap) throws Exception{
		Long uin = (Long) orderMap.get("uin");
		Double pretotal = StringUtils.formatDouble(orderMap.get("total"));//Ԥ֧�����
		//Integer preState =(Integer)orderMap.get("pre_state");//Ԥ֧��״̬ ,1����Ԥ֧��,2�ȴ��������Ԥ֧��
	//	System.err.println("Ԥ֧�� ��"+pretotal);
		Long ntime = System.currentTimeMillis()/1000;
		Map<String, Object> orMap=new HashMap<String, Object>();
		Long btLong = (Long)orderMap.get("create_time");
//		if(ntime>btLong){
//			
//		}else {
//			ntime = ntime +60;
//		}
		Integer cType = (Integer)orderMap.get("c_type");//������ʽ ��0:NFC,1:IBeacon,2:����   3ͨ������ 4ֱ�� 5�¿��û� 6��λ��ά��
		String btimestr =  TimeTools.getTime_MMdd_HHmm(btLong*1000);
		String etimestr =  TimeTools.getTime_MMdd_HHmm(ntime*1000);
		String btime = btimestr.substring(6);
		String etime = etimestr.substring(6);
		Long start = (Long)orderMap.get("create_time");
		Integer pid = (Integer)orderMap.get("pid");//�Ʒѷ�ʽ��0����(0.5/h)��1��ʱ��12Сʱ��10Ԫ����ÿСʱ1Ԫ��
		Integer type = (Integer)orderMap.get("type");
		Integer state = (Integer)orderMap.get("state");
		Long end = ntime;
		String hascard = "1";//�Ƿ��г���
		//��ѯ���ƺ�
		String carNumber = (String)orderMap.get("car_number");
		if(carNumber==null||carNumber.toString().trim().equals("")){
			carNumber=null;
			if(uin!=null)
				carNumber = getCarNumber(uin);
			if(carNumber==null){
				carNumber="���ƺ�δ֪";
				hascard = "0";
			}
		}
		orMap.put("carnumber",carNumber);
		
		List<Map<String, Object>> cardList = pgOnlyReadService.getAll("select car_number from car_info_Tb where uin=? ", new Object[]{uin});
		if(cardList!=null&&cardList.size()>0){
			String cards = "";
			for(Map<String, Object> cMap: cardList){
				cards +=",\""+cMap.get("car_number")+"\"";
			}
			cards = cards.substring(1);
			orMap.put("cards", "["+cards+"]");
		}else {
			orMap.put("cards", "[]");
		}
		Integer isfast = (Integer)orderMap.get("type");
		if(isfast!=null&&isfast==2){//�������������ɵĶ���,���ƺ�Ӧ��д����������
			String cardno = (String) orderMap.get("nfc_uuid");
			if(cardno!=null&&cardno.indexOf("_")!=-1)
				orMap.put("carnumber", cardno.substring(cardno.indexOf("_")+1));
		}
		orMap.put("hascard", hascard);
		//orMap.put("handcash", "0");
		orMap.put("btime", btime);
		orMap.put("etime", etime);
		orMap.put("btimestr", btimestr);
		orMap.put("etimestr", etimestr);
		orMap.put("duration", StringUtils.getTimeString(start, end));
		orMap.put("orderid", orderMap.get("id"));
		//orMap.put("carnumber",orderMap.get("car_number")==null?"���ƺ�δ֪": orderMap.get("car_number"));
		orMap.put("uin", orderMap.get("uin"));
		orMap.put("total", "0.00");
		orMap.put("collect", "0.00");
		orMap.put("handcash", "1");
		orMap.put("isedit", 0);
		//orMap.put("state", orderMap.get("state")==null?"0":orderMap.get("state"));//����״̬��0���δ���㣬1�ѽ��㣬2�ӵ�
		orMap.put("car_type", orderMap.get("car_type"));
		orMap.put("prepay", pretotal);
		orMap.put("isfast", type);
		//String pid = CustomDefind.CUSTOMPARKIDS;
		
		if(pid!=null&&pid>-1){//���Ƽ۸����
//			orMap.put("collect0", getCustomPrice(start, end, pid));
			orMap.put("handcash", "0");
//			Long duration = (end-start)/60;//����
//			Long t = duration/15;
//			if(duration%15!=0)
//				t= t+1;
			orMap.put("collect",getCustomPrice(start, end, pid));
//			logger.error("���㶩�������أ�"+orMap);
			return StringUtils.createJson(orMap);	
		}
		//���ж��¿�
		Map<String, Object> pMap =null;
		if(uin!=null&&uin!=-1&&(cType==3||cType==5)){//ͨ�������볡ʱ������ʱҪ��һ���Ƿ����¿�
			pMap= daService.getMap("select p.* from product_package_tb p," +
					"carower_product c where c.pid=p.id and p.comid=? and c.uin=? and c.e_time>? order by c.id desc limit ?", 
					new Object[]{comId,uin,ntime,1});
			if(pMap!=null&&!pMap.isEmpty()){
				//System.out.println(pMap);
				Long limitDay = (Long)pMap.get("limitday");
				
				//Integer ptype = (Integer)pMap.get("type");//�ײ�����  0:ȫ�� 1ҹ�� 2�ռ� 3�¿�ʱ�����Ż� 4ָ��Сʱ�����
				Long day = (limitDay-ntime)/(24*60*60)+1;
				orMap.put("limitday", day+"");
				orMap.put("handcash", "2");
				//logger.error("���㶩�������أ�"+orMap);
				//return StringUtils.createJson(orMap);
			}
		}
		
		Integer car_type = (Integer)orderMap.get("car_type");
		if(car_type == 0){//0:ͨ��
			Long count = daService.getLong("select count(*) from com_info_tb where id=? and car_type=?", new Object[]{comId,1});
			if(count > 0){//���ִ�С��
				car_type = 1;//Ĭ�ϳ�С���ƷѲ���
			}
		}
		Map dayMap=null;//�ռ����
		Map nigthMap=null;//ҹ�����
		//��ʱ�μ۸����
		List<Map<String ,Object>> priceList1=pgOnlyReadService.getAll("select * from price_tb where comid=? " +
					"and state=? and pay_type=? and car_type=? order by id desc", new Object[]{comId,0,0,car_type});
		//�鰴�β���
		List<Map<String ,Object>> priceList2=pgOnlyReadService.getAll("select * from price_tb where comid=? " +
				"and state=? and pay_type=? and car_type=? order by id desc", new Object[]{comId,0,1,car_type});
		//boolean isHasTimePrice=false;//�Ƿ��а��μ۸�
		if(priceList2!=null&&!priceList2.isEmpty()){//���β���
			int i=0;
			String total0 = "";
			String total1 = "[";
			for(Map<String ,Object> timeMap: priceList2){
				Object ounit  = timeMap.get("unit");
				String total = timeMap.get("price")+"";
				if(ounit!=null){
					Integer unit = Integer.valueOf(ounit.toString());
					if(unit>0){
						Long du = (end-start)/60;//ʱ����
						int times = du.intValue()/unit;
						if(du%unit!=0)
							times +=1;
						total = StringUtils.formatDouble(times*Double.valueOf(timeMap.get("price")+""))+"";
					}
				}
				if(i==0){
					total0=total;
					total1 += total;
				}else {
					total1 +=","+total;
				}
				i++;
			}
			total1+="]";
			orMap.put("collect0", total0);
			orMap.put("collect1", total1);
			orMap.put("handcash", "0");
			//isHasTimePrice = true;
		}
		boolean isHasDatePrice = false;//�Ƿ��а�ʱ�μ۸�
		if(priceList1!=null&&!priceList1.isEmpty()){//�Ӱ�ʱ�μ۸�����зּ���ռ��ҹ���շѲ���
			dayMap= priceList1.get(0);
			boolean pm1 = false;//�ҵ�map1,�����ǽ���ʱ����ڿ�ʼʱ��
			boolean pm2 = false;//�ҵ�map2
			Integer isEdit = 0;//�Ƿ�ɱ༭�۸�Ŀǰֻ���ռ䰴ʱ�۸���Ч,0��1�ǣ�Ĭ��0
			if(priceList1.size()>1){
				for(Map map : priceList1){
					if(pm1&&pm2)
						break;
					Integer pbtime = (Integer)map.get("b_time");
					Integer petime = (Integer)map.get("e_time");
					if(btime==null||etime==null)
						continue;
					if(petime>pbtime){
						if(!pm1){
							dayMap = map;
							isEdit=(Integer)map.get("isedit");
							pm1=true;
						}
					}else {
						if(!pm2){
							nigthMap=map;
							pm2=true;
						}
					}
				}
			}
			double minPriceUnit = getminPriceUnit(comId);
			Long end_time = ntime;
			if(state == 1){
				end_time =  (Long)orderMap.get("end_time");
			}
			Map assistMap = daService.getMap("select * from price_assist_tb where comid = ? and type = ?", new Object[]{comId,0});

			Map<String, Object> oMap=CountPrice.getAccount((Long)orderMap.get("create_time"),end_time, dayMap, nigthMap,minPriceUnit,assistMap);
			//orMap.put("total", oMap.get("total"));
//			if(isHasTimePrice){
//				orMap.put("collect0", orMap.get("collect"));
//			}else {
//			}
			orMap.put("collect", oMap.get("collect"));
			orMap.put("isedit", isEdit);
			orMap.put("handcash", "0");
			isHasDatePrice = true;
		}
		
		if(!isHasDatePrice){//û�а�ʱ�μ۸�
			orMap.put("collect", orMap.get("collect0"));
			orMap.remove("collect0");
		}
		
		//orMap.put("prestate", preState);
		
		//logger.error("���㶩�������أ�"+orMap);
		return StringUtils.createJson(orMap);	
	}
	//����lala������ô˷��������Ա���һ���������ζ�����ɼ�
	/*public  boolean isCanLaLa(Integer number,Long uin,Long time) throws Exception{
		//logger.error("lala scroe ---uin:"+uin+",sharenumber:"+number+",time:"+TimeTools.getTime_yyyyMMdd_HHmmss(time*1000));
		Map<Long, Long> lalaMap = memcacheUtils.doMapLongLongCache("zld_lala_time_cache",null, null);
		String lastDate = "";
		boolean isLalaScore=true;
		if(lalaMap!=null){
			Long lastTime = lalaMap.get(uin);
			//logger.error("lala scroe ---uin:"+uin+",sharenumber:"+number+",cache time:"+lastTime);
			if(lastTime!=null){
				lastDate=TimeTools.getTime_yyyyMMdd_HHmmss(lastTime*1000);
				if(time<lastTime+15*60){
					isLalaScore=false;
					ParkingMap.setLastLalaTime(uin, lastTime);//ͬ��ʱ�䵽���ػ���
				}
			}
		}else {
//				logger.error("error, no memcached ������please check memcached ip config........");
			lalaMap=new HashMap<Long, Long>();
		}
		if(isLalaScore){
			lalaMap.put(uin, time);
			ParkingMap.setLastLalaTime(uin, time);//ͬ��ʱ�䵽���ػ���
			memcacheUtils.doMapLongLongCache("zld_lala_time_cache", lalaMap, "update");
		}
		logger.error("lala scroe ---return :"+isLalaScore+"---uin:"+uin+",sharenumber:"+number+",time:"+TimeTools.getTime_yyyyMMdd_HHmmss(time*1000)+",lastTime:"+lastDate);
		return isLalaScore;
	}*/
	/**
	 * �ӻ�����ȡ��ͨ���û�
	 * @param uuid
	 * @return
	 */
	public Long getUinByUUID(String uuid){
		Long uin = memcacheUtils.getUinUuid(uuid);
		if(uin!=null&&uin==-1){//δ��ʼ������ 
			logger.error("��ʼ����ͨ���û�.....");
			List<Map<String, Object>> list = pgOnlyReadService.getAll("select nfc_uuid,uin from com_nfc_tb where uin>?",new Object[]{0});
			logger.error(">>>>>>>>>>>>>>>��ʼ����NFC�û�����"+list.size());
			Map<String, Long> uinUuidMap = new HashMap<String, Long>();
			if(list!=null&&list.size()>0){
				for(Map<String, Object> map : list){
					uinUuidMap.put(map.get("nfc_uuid")+"",(Long)map.get("uin"));
				}
				uin = uinUuidMap.get(uuid);
				logger.error("������ͨ���û�.....size:"+uinUuidMap.size());
				memcacheUtils.setUinUuid(uinUuidMap);
			}
		}
		return uin;
	}
	/**
	 * ������ͨ������ 
	 * @param uuid
	 * @param uin
	 */
	public void updateUinUuidMap(String uuid,Long uin){
//		Map<String,Long> uuidUinMap = memcacheUtils.doUinUuidCache("uuid_uin_map", null, null);
//		if(uuidUinMap!=null){
//			logger.error("������ͨ������ ...");
//			uuidUinMap.put(uuid, uin);
//			memcacheUtils.setUinUuid(uuidUinMap);
//		}else {
			logger.error("��ʼ����ͨ���û�.....");
			List<Map<String, Object>> list = pgOnlyReadService.getAll("select nfc_uuid,uin from com_nfc_tb where uin>?",new Object[]{0});
			logger.error(">>>>>>>>>>>>>>>��ʼ����NFC�û�����"+list.size());
			Map<String, Long> uinUuidMap = new HashMap<String, Long>();
			if(list!=null&&list.size()>0){
				for(Map<String, Object> map : list){
					uinUuidMap.put(map.get("nfc_uuid")+"",(Long)map.get("uin"));
				}
				//uinUuidMap.put(uuid, uin);
				logger.error("������ͨ���û�.....size:"+uinUuidMap.size());
				memcacheUtils.setUinUuid(uinUuidMap);
			}
//		}
	}
	
	public int backNewUserTickets(Long ntime,Long key){
		return 0;//2015-03-10������������ʱ������д��ͣ��ȯ����¼ʱ�жϺ����������ͣ��ȯ
	}
	
	//��ȡ��ɫ�Ĺ���Ȩ��
	public List<Object> getAuthByRole(Long roleid) throws JSONException{
		String auth = "[]";
		List<Object> authids = new ArrayList<Object>();
		Map<String, Object> map = daService.getMap("select * from role_auth_tb where role_id=? ", new Object[]{roleid});
		if(map != null){
			auth = (String) map.get("auth");
		}
		JSONArray jsonArray = new JSONArray(auth);
		for(int i=0;i<jsonArray.length();i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			Long nid = jsonObject.getLong("nid");
			Long pId = jsonObject.getLong("pid");
			Map<String, Object> map2 = daService.getMap("select id from auth_tb where nid=? and pid=? ", new Object[]{nid,pId});
			if(map2 != null){
				authids.add(map2.get("id"));
			}
		}
		return authids;
	}
	
	//��ȡ����Ȩ��
	public List<Object> getDataAuth(Long id){
		List<Object> params = new ArrayList<Object>();
		params.add(id);//�Լ�
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		list = pgOnlyReadService.getAll("select authorizer from dataauth_tb where authorizee=? order by authorizer desc ", new Object[]{id});
		for(Map<String, Object> map : list){
			Long authorizer = (Long)map.get("authorizer");
			if(!params.contains(authorizer)){
				params.add(authorizer);
			}
		}
		return params;
	}
	/**
	 * ���ݳ���,������Ų�ѯ�Ƿ����¿�
	 * @param carNumber
	 * @return
	 */
	public boolean isMonthUser(Long uin,Long comId){
		//���ж��¿�
	/*	if(carNumber==null||"".equals(carNumber))
			return false;
		Long uin  = null;
		
		Map carMap = daService.getMap("select uin from car_info_tb where car_number=?", new Object[]{carNumber});
		
		if(carMap==null||carMap.get("uin")==null){
			return false;
		}
		uin=(Long) carMap.get("uin");*/
		if(uin!=null&&uin!=-1){
			Long ntime = System.currentTimeMillis()/1000;
			Map<String, Object> pMap = daService.getMap("select p.b_time,p.e_time,p.type from product_package_tb p," +
					"carower_product c where c.pid=p.id and p.comid=? and c.uin=? and c.e_time>? and c.b_time<? order by c.id desc limit ?", 
					new Object[]{comId,uin,ntime,ntime,1});
			if(pMap!=null&&!pMap.isEmpty()){
				//System.out.println(pMap);
				Integer b_time = (Integer)pMap.get("b_time");
				Integer e_time = (Integer)pMap.get("e_time");
				Calendar c = Calendar.getInstance();
				Integer hour = c.get(Calendar.HOUR_OF_DAY);
				Integer type = (Integer)pMap.get("type");//0:ȫ�� 1ҹ�� 2�ռ�
				boolean isVip = false;
				if(type==0){//0:ȫ�� 1ҹ�� 2�ռ�
					logger.error("ȫ������û���uin��"+uin);
					isVip = true;
				}else if(type==2){//0:ȫ�� 1ҹ�� 2�ռ�
					if(hour>=b_time&&hour<=e_time){
						logger.error("�ռ�����û���uin��"+uin);
						isVip = true;
					}
				}else if(type==1){//0:ȫ�� 1ҹ�� 2�ռ�
					if(hour<=e_time||hour>=b_time){
						logger.error("ҹ������û���uin��"+uin);
						isVip = true;
					}
				}
				return isVip;
			}
		}
		return false;
	}
	/**
	 * ȡ����ͣ��ȯ��δ��֤�������ʹ��3Ԫȯ�� ͣ����ר��ͣ��ȯ����
	 * @param uin
	 * @param fee
	 * @param type: //0����汾�������ȶ��������ȯ��1�ϰ汾���Զ�ѡȯʱ��������ͨȯ����ߵֿ۽�2�°汾������ȯ���ͷ�����ߵֿ۽��
	 * @param comId
	 * @return
	 */
	
	//http://127.0.0.1/zld/carowner.do?action=getaccount&mobile=13641309140&total=10&uid=21694&utype=1
	public  Map<String, Object> getTickets(Long uin,Double fee,Long comId,Integer type,Long uid){
		Map<String, Object> map=null;
		Integer limit = CustomDefind.getUseMoney(fee,0);
		Double splimit = StringUtils.formatDouble(CustomDefind.getValue("TICKET_LIMIT"));
		if(type==0){//0����汾�������ȶ��������ȯ��1�ϰ汾���Զ�ѡȯʱ��������ͨȯ����ߵֿ۽�2�°汾������ȯ���ͷ�����ߵֿ۽��
			 logger.error("uin:"+uin+",comid:"+comId+",type:"+type+",fee:"+fee);
			 map= useTickets(uin, fee, comId,uid,type);
		}else {
			logger.error("uin:"+uin+",comid:"+comId+",type:"+type+",fee:"+fee+",uselimit:"+limit);
			map = methods.getTickets(uin,fee,comId,uid);
			if(map!=null){
				Integer ttype = (Integer)map.get("type");
				Integer res = (Integer)map.get("resources");
				if(ttype==1||res==1)//��ͨȯ
					map.put("limit",StringUtils.formatDouble(fee-splimit));
				else {//����ר��ȯ
					map.put("limit",limit);
				}
			}
		}
		logger.error("uin:"+uin+",comid:"+comId+",fee:"+fee+",map:"+map);
		if(map!=null){//��һ����û����ͬ���ĳ���ר��ȯ
			Integer money = (Integer)map.get("money");
			Long limitday=(Long)map.get("limit_day");
			Integer ttype = (Integer)map.get("type");
			Integer res = (Integer)map.get("resources");
			if(ttype!=1&&res!=1){
				Map<String, Object> map1 = daService.getMap("select * from ticket_tb where comid=? and state=? and uin=? and  money=? and type=? and limit_day >=?  ",
						new Object[]{comId,0,uin,money,1,limitday});
				logger.error("uin:"+uin+",comid:"+comId+",fee:"+fee+",map1:"+map1);
				if(map1!=null&&!map1.isEmpty()){
					if(type==1)
						map1.put("limit",StringUtils.formatDouble(fee-splimit));
					return map1;
				}
			}
		}
		return map;
	}
	
	/**
	 * ȡ����ͣ��ȯ
	 * @param uin    �����˻�
	 * @param total  �������
	 * @param comId  ������� 
	 * @param uid    �շ�Ա���
	 * @param utype  0����汾�������ȶ��������ȯ��1�ϰ汾���Զ�ѡȯʱ��������ͨȯ����ߵֿ۽�2�°汾������ȯ���ͷ�����ߵֿ۽��
	 * @return   ����ͣ��ȯ
	 */
	public Map<String, Object> useTickets(Long uin,Double total,Long comId,Long uid,Integer utype){
		//������п��õ�ȯ
		//Long ntime = System.currentTimeMillis()/1000;
		Integer limit = CustomDefind.getUseMoney(total,0);
		boolean blackuser = isBlackUser(uin);
		boolean blackparkuser = isBlackParkUser(comId, false);
		boolean isauth = isAuthUser(uin);
		if(!isauth){
			if(blackuser||blackparkuser){
				if(blackuser){
					logger.error("�����ں�������uin:"+uin+",fee:"+total+",comid:"+comId);
				}
				if(blackparkuser){
					logger.error("�����ں�������uin:"+uin+",fee:"+total+",comid:"+comId);
				}
				return null;
			}
		}else{
			logger.error("����uin:"+uin+"����֤��������ȯ���ж��Ƿ��Ǻ������������Ƿ��������");
		}
		double ticketquota=-1;
		if(uid!=-1){
			Map usrMap =daService.getMap("select ticketquota from user_info_Tb where id =? and ticketquota<>?", new Object[]{uid,-1});
			if(usrMap!=null){
				ticketquota = Double.parseDouble(usrMap.get("ticketquota")+"");
				logger.error("���շ�Ա:"+uid+"����ȯ����ǣ�"+ticketquota+"��(-1����û����)");
			}
		}
		//���п���ͣ��ȯ
		List<Map<String,Object>> allTickets =methods.getUseTickets(uin, total);
		Map<String, Object> ticketMap=null;
		logger.error(allTickets);
		if(allTickets!=null&&!allTickets.isEmpty()){
			double spr_abs = 100;             //ר��ȯ�ֿ۽����֧�����Ĳ�ֵ
			Integer spr_money_limit_abs=100;  //ר��ȯ�ֿ۽����ȯ���Ĳ�ֵ
			double comm_abs = 100;            //��ͨȯ�ֿ۽����ȯ�����Ĳ�ֵ
			Integer comm_money_limit_abs=100; //��ͨȯ�ֿ۽����ȯ���Ĳ�ֵ
			double buy_abs = 100;             //����ȯ�ֿ۽����֧�����Ĳ�ֵ
			Integer buy_money_limit_abs=100;  //����ȯ�ֿ۽����ȯ���Ĳ�ֵ
			Integer comm_index=-1;  //��ͨȯ����      
			Integer spr_index=-1;  //ר��ȯ����
			Integer buy_index=-1;  //��ȯ����
			Integer comm_money=0;  //��ͨȯ�ֿ۽��
			Integer spr_money=0;   //ר��ȯ�ֿ۽��
			Integer buy_money=0;   //��ȯȯ�ֿ۽��
			Integer index=-1;      //��������
			Integer spr_ticket_money=0;  //ר��ȯ���
			Integer buy_ticket_money=0;  //��ȯȯ���
			Integer comm_ticket_money=0; //��ͨȯ���
			for(Map<String,Object> map: allTickets){
				Long cid = (Long)map.get("comid");//��˾���
				Integer money = (Integer)map.get("money");
				Integer type=(Integer)map.get("type");
				Integer useLimit = (Integer)map.get("limit");
				Integer res = (Integer)map.get("resources");
				index ++;
				if(utype==0&&money>=limit){//0����汾�������ֿ۽��ȶ��������ȯ
					continue;
				}
				if(type==1&&cid!=null&&!cid.equals(comId)){//�Ǵ˳�����ר��ȯ����
					continue;
				}
				if(ticketquota!=-1&&ticketquota>money){//ȯ������շ�Ա��ȯ��߽�������
					continue;
				}
				if(!isauth&&money>1){//����֤��������ʹ�ô���1Ԫ���ϵ�ȯ
					continue;
				}
				if(useLimit==0){//�ֿ۽��Ϊ0
					continue;
				}
				if(money>Math.ceil(total)&&res==1){//�����ȯ����ڶ������
					continue;
				}
				if(type==1){//ר��ȯ��ȡ��С֧�������ֿ۽����
					double abs = total-useLimit;   
					Integer mlabs = money-useLimit;
					if(spr_abs>abs){
						spr_abs =abs;
						spr_money_limit_abs=mlabs;
						spr_index=index;  //��������
						spr_money=useLimit; //����ֿ۽��
						spr_ticket_money=money;//����ȯ���
					}else if(spr_abs==abs&&spr_money_limit_abs>mlabs){//��ǰ֧�������ֿ۽���ֵ����һ��ȯһ��ʱ��ȡȯ�����ֿ۽���ֵ��С��
						spr_index=index;
						spr_money=useLimit;
						spr_ticket_money=money;
					}
				}else {
					if(res==1){//����ȯ
						double abs = total-useLimit;
						Integer mlabs = money-useLimit;
						if(buy_abs>abs){
							buy_abs =abs;
							buy_money_limit_abs=mlabs;
							buy_index=index;
							buy_money=useLimit;
							buy_ticket_money=money;
						}else if(buy_abs==abs&&buy_money_limit_abs>mlabs){
							buy_index=index;
							buy_money=useLimit;
							buy_ticket_money=money;
						}
						map.put("isbuy", "1");
					}else {//��ͨȯ
						double abs = total-useLimit;
						Integer mlabs = money-useLimit;
						if(comm_abs>abs){
							comm_abs =abs;
							comm_money_limit_abs=mlabs;
							comm_index=index;
							comm_money=useLimit;
							comm_ticket_money=money;
						}else if(comm_abs==abs&&comm_money_limit_abs>mlabs){
							comm_index=index;
							comm_money=useLimit;
							comm_ticket_money=money;
						}
					}
				}
			}
			logger.error(spr_index+":"+spr_money+":"+spr_ticket_money+","+buy_index+":"+buy_money+":"+
						buy_ticket_money+","+comm_index+":"+comm_money+":"+comm_ticket_money);
			if(spr_money>=comm_money&&spr_money>=buy_money){//���ݵֿ۽�ѡ���ģ�����ѡר��ȯ
				if(spr_money==buy_money){//ר��ȯ�͹���ȯ�ֿ۽����ͬʱ��ѡȯ����С��
					if(spr_ticket_money>buy_ticket_money){
						ticketMap=allTickets.get(buy_index);
					}
				}
				if(spr_money==comm_money){//ר��ȯ����ͨȯ�ֿ۽����ͬʱ��ѡȯ����С��
					if(spr_ticket_money>comm_ticket_money){
						ticketMap=allTickets.get(comm_index);
					}
				}
				if(ticketMap==null&&spr_index>-1){
					ticketMap=allTickets.get(spr_index);
				}
				if(utype<2&&ticketMap!=null)//�ϰ汾������ͨȯ�ĵֿ����ޣ���ֹ֧��ʧ��
					ticketMap.put("limit", limit<1?1:limit);
			}else if(comm_money>=buy_money&&comm_index>-1){//���ݵֿ۽�ѡ���ģ�û��ר��ȯʱ����ѡ��ͨȯ
				if(buy_money==comm_money){
					if(comm_ticket_money>buy_ticket_money){
						ticketMap=allTickets.get(buy_index);
					}
				}else {
					ticketMap=allTickets.get(comm_index);
				}
				if(utype<2&&ticketMap!=null)
					ticketMap.put("limit", limit<1?1:limit);
			}else if(buy_index!=-1){
				ticketMap=allTickets.get(buy_index);
				if(utype<2&&ticketMap!=null)
					ticketMap.put("limit", limit<1?1:limit);
			}
			logger.error("uin:"+uin+",total:"+total+",comid:"+comId+",uid:"+uid+",utype:"+utype+"ѡȯ�����"+ticketMap);
		}
		return ticketMap;
	}
	/**
	 * ȡ����ͣ��ȯ��δ��֤�������ʹ��3Ԫȯ��
	 * @param uin
	 * @param fee
	 * @return
	 */
	/*@SuppressWarnings("unchecked")
	public Map<String, Object> useTickets(Long uin,Double fee,Long comId,Long uid){
		//������п��õ�ȯ
		//Long ntime = System.currentTimeMillis()/1000;
		Integer limit = CustomDefind.getUseMoney(fee,0);
		Double splimit = StringUtils.formatDouble(CustomDefind.getValue("TICKET_LIMIT"));
		boolean blackuser = isBlackUser(uin);
		boolean blackparkuser = isBlackParkUser(comId, false);
		boolean isauth = isAuthUser(uin);
		if(!isauth){
			if(blackuser||blackparkuser){
				if(blackuser){
					logger.error("�����ں�������uin:"+uin+",fee:"+fee+",comid:"+comId);
				}
				if(blackparkuser){
					logger.error("�����ں�������uin:"+uin+",fee:"+fee+",comid:"+comId);
				}
				return null;
			}
		}else{
			logger.error("����uin:"+uin+"����֤��������ȯ���ж��Ƿ��Ǻ������������Ƿ��������");
		}
		List<Map<String, Object>> list = null;
		double ticketquota=-1;
		if(uid!=-1){
			Map usrMap =daService.getMap("select ticketquota from user_info_Tb where id =? and ticketquota<>?", new Object[]{uid,-1});
			if(usrMap!=null){
				ticketquota = Double.parseDouble(usrMap.get("ticketquota")+"");
				logger.error("���շ�Ա:"+uid+"����ȯ����ǣ�"+ticketquota+"��(-1����û����)");
			}
		}
		if(!isauth){//δ��֤�������ʹ��2Ԫȯ��
			double noAuth = 1.0;//δ��֤�����������noAuth(2)Ԫȯ,�Ժ�Ķ����ֵ��ok
			if(ticketquota>=0&&ticketquota<=noAuth){
//				ticketquota = ticketquota+1;
			}else{
				ticketquota=noAuth;//δ��֤�������ʹ��2Ԫȯ
			}
			list=	daService.getAll("select * from ticket_tb where uin = ? " +
					"and state=? and limit_day>=? and type<? and money<?  order by limit_day",
					new Object[]{uin,0,TimeTools.getToDayBeginTime(),2,ticketquota+1});
		}else {
			list  = daService.getAll("select * from ticket_tb where uin = ? " +
					"and state=? and limit_day>=? and type<? order by limit_day",
					new Object[]{uin,0,TimeTools.getToDayBeginTime(),2});
		}
		logger.error("uin:"+uin+",fee:"+fee+",comid:"+comId+",today:"+TimeTools.getToDayBeginTime());
		if(list!=null&&!list.isEmpty()){
			List<String> _over3day_moneys = new ArrayList<String>();
			int i=0;
			for(Map<String, Object> map : list){
				Double money = Double.valueOf(map.get("money")+"");
				//Long limit_day = (Long)map.get("limit_day");
				Long tcomid = (Long)map.get("comid");
				Integer type = (Integer)map.get("type");
//				logger.error("ticket>>>uin:"+uin+",comId:"+comId+",tcomid:"+tcomid+",type:"+type+",ticketid:"+map.get("id"));
				if(comId!=null&&comId!=-1&&tcomid!=null&&type == 1){
					if(comId.intValue()!=tcomid.intValue()){
						logger.error(">>>>get ticket:�������������ͣ��ȯ��������....comId:"+comId+",tcomid:"+tcomid+",uin:"+uin);
						i++;
						continue;
					}
				}
				Integer res = (Integer)map.get("resources");
				if(limit==0&&res==0&&type==0){//֧�����С��3Ԫ��������ͨȯ
					i++;					
					continue;
				}
				if(type==1||res==1){
					limit=Double.valueOf((fee-splimit)).intValue();
				}else {
					limit= CustomDefind.getUseMoney(fee,0);
				}
				map.put("isbuy", res);
				if(money>limit.intValue()){
					i++;
					continue;
				}else if(limit.intValue()==money){//ȯֵ+1Ԫ ���� ֧�����ʱֱ�ӷ���
					return map;
				}
				//�ж� �Ƿ� �� ���Ǹó�����ר��ȯ
				
				map.remove("comid");
				//map.remove("limit_day");
				_over3day_moneys.add(i+"_"+Math.abs(limit-money));
				i++;
			}
			if(_over3day_moneys.size()>0){//3������ͣ��ȯ��ͣ���ѵľ���ֵ���� ��ȡ����ֵ��С��
				int sk = 0;//����index
				double sv=0;//������Сֵ
				int index = 0;
				for(String s : _over3day_moneys){
					int k = Integer.valueOf(s.split("_")[0]);
					double v = Double.valueOf(s.split("_")[1]);
					if(index==0){
						sk=k;
						sv = v;
					}else {
						if(sv>v){
							sk=k;
							sv = v;
						}
					}
					index++;
				}
				logger.error("uin:"+uin+",comid:"+comId+",sk:"+sk);
				return list.get(sk);
			}
		}else{
			logger.error("δѡ��ȯuin:"+uin+",comid:"+comId+",fee:"+fee);
		}
		return null;
	}*/
	//������ƴ�������
	public void backTicket(double money,Long orderId,Long uin,Long comid,String openid){
		Long ctime = System.currentTimeMillis()/1000;
		//����ר��ȯ
		Map btcomMap = daService.getMap("select * from park_ticket_tb where comid=? ", new Object[]{comid});
		if(money>=1&&btcomMap!=null){
			logger.error(">>>>back ticket comid="+comid+",��ר��ͣ��ȯ");
			Integer num = (Integer)btcomMap.get("tnumber");
			Integer exptime = (Integer)btcomMap.get("exptime");
			Integer haveget = (Integer)btcomMap.get("haveget");
			Long btid = (Long)btcomMap.get("id");
			Double amount =StringUtils.formatDouble(btcomMap.get("money"));
			if(haveget<num){//��������
				int ret = daService.update("update park_ticket_tb set haveget=? where id = ? and tnumber>=? ",  new Object[]{haveget+1,btid,haveget+1});
				if(ret==1){
					ret = daService.update( "insert into ticket_tb (create_time,limit_day,money,state,uin,comid,type) values(?,?,?,?,?,?,?) ",
							new Object[]{TimeTools.getToDayBeginTime(),TimeTools.getToDayBeginTime()+(exptime+1)*24*60*60-1,amount,0,uin,comid,1});
					logger.error(">>>>back ticket comid="+comid+",ר��ͣ��ȯ����� ��"+amount+",������:"+num+",��ʹ��:"+(haveget+1)+"���û���"+uin+",��ȯ���:"+ret);
				}
			}
		}
		
		if(money>=1&&memcacheUtils.readBackTicketCache(uin)){//һ��ֻ��һ�κ��
			String sql = "insert into order_ticket_tb (uin,order_id,money,bnum,ctime,exptime,bwords) values(?,?,?,?,?,?,?)";
			Object []values = null;
			String content= CustomDefind.getValue("DESCRIPTION");
			Long exptime = ctime + 24*60*60;
			Long count = daService.getLong("select count(*) from user_account_tb where type=? and uin=? ", new Object[]{1, uin});
			if(count == 1){//�����ױ�֧������������
				values = new Object[]{uin,orderId,36,18,ctime,exptime,content};
				logger.error(">>>>>�����ױ����ѣ���18�����36Ԫ...");
			}else if(money>=1&&money<10){//�ֻ�֧������1ԪС��10Ԫ����3�������8Ԫ
				values = new Object[]{uin,orderId,18,12,ctime,exptime,content};
				logger.error(">>>>>������ƴ�������,����1ԪС��10Ԫ����3�������8Ԫ...");
			}else if(money>=10){// �ֻ�֧������10Ԫ����8�������18Ԫ
				values = new Object[]{uin,orderId,28,20,ctime,exptime,content};
				logger.error(">>>>>������ƴ�������,����10Ԫ����8�������18Ԫ...");
			}
			else {
				logger.error(">>>>>������ƴ�������,����һԪ��������.....");
				return;
			}
			int ret = daService.update(sql, values);
			logger.error(">>>>>������ƴ�������,money :"+money+" ret :"+ret);
			if(ret==1)
				memcacheUtils.updateBackTicketCache(uin);
			/*if(openid==null){//�ͻ���֧������������Ԫͣ��ȯ��openidΪ��ʱ���ǿͻ���֧��
				ret = daService.update( "insert into ticket_tb (create_time,limit_day,money,state,uin,type) values(?,?,?,?,?,?) ",
						new Object[]{TimeTools.getToDayBeginTime(),TimeTools.getToDayBeginTime()+16*24*60*60-1,3,0,uin,0});
				logger.error(">>>>back ticket �ͻ���֧������� ��3,�û���"+uin+",��ȯ���:"+ret);
				logService.insertUserMesg(5, uin, "��ϲ�����һ����Ԫͣ��ȯ", "ͣ��ȯ����");
			}*/
		}else {
			logger.error(">>>>>������ƴ�������,�Ѿ�������������.....");
		}
		
	}
	
	

	public boolean isBlackUser(Long uin){
		List<Long> blackUserList = memcacheUtils.doListLongCache("zld_black_users", null, null);
//		logger.error(">>>zld black users :"+blackUserList);
		//�Ƿ��ں����� 
		boolean isBlack = true;
		if(blackUserList==null||!blackUserList.contains(uin))//���ں������п��Դ����Ƽ�����
			isBlack=false;
//		if(blackUserList!=null&&blackUserList.size()>5)
//			clearBlackUser();
		return isBlack;
	}
	//�жϳ����Ƿ��ں������ڣ�uid���շ�Աʱ��isparkuser��true,uidΪ����ʱ��isparkuser��false
	public boolean isBlackParkUser(long uid,Boolean isparkuser){
		boolean isBlack = false;
		String parkback = CustomDefind.getValue("PARKBACK");
		if(StringUtils.isNotNull(parkback)){
			String []str = parkback.split(",");
			if(isparkuser){
				long count = 0;
				for (String string : str) {
					count += daService.getLong("select count(*) from user_info_tb where id=? and comid =?", new Object[]{uid,Long.parseLong(string)});
					if(count>0){
						isBlack=true;
						logger.error("�շ�Աuid:"+uid+"���ڵĳ����ں������ڣ����з���,�Ƽ���,����ȯȡ��");
						break;
					}
				}
			}else{
				for (String string : str) {
					if(Long.parseLong(string)==uid){
						isBlack=true;
						logger.error("����:"+uid+"�ں������ڣ����з���,�Ƽ���,����ȯȡ��");
						break;
					}
				}
			}
		}
		logger.error("�жϳ��������շ�Ա�����ڵĳ������Ƿ��ں������У�"+isBlack);
		return isBlack;
	}
	
	public boolean isAuthUser(long uin){
		Map userMap = daService.getMap("select is_auth from user_info_tb where id =? ", new Object[]{uin});
		Integer isAuth = 0;
		if(userMap!=null&&userMap.get("is_auth")!=null)
			isAuth=(Integer)userMap.get("is_auth");
		boolean ret = isAuth==1?true:false;
		logger.error("uin:"+uin+"�Ƿ�����֤�û�ret:"+ret+"(true:��֤�û���false:������֤�û�)");
		return ret;
	}
	
	public void clearBlackUser(){
		List<Long> blackUserList = memcacheUtils.doListLongCache("zld_black_users", null, null);
		//logger.error(">>>zld black users :"+blackUserList);
		if(blackUserList!=null){//���ں������п��Դ����Ƽ�����
			blackUserList=new ArrayList<Long>();
			memcacheUtils.doListLongCache("zld_black_users", blackUserList, "update");
		}
	}
	/**
	 * �ϴ���Ƭ
	 * @param request
	 * @param uin
	 * @return
	 * @throws Exception
	 */
	public String uploadPicToMongodb (HttpServletRequest request,Long uin,String table) throws Exception{
		logger.error(">>>>>begin upload order picture....");
		Map<String, String> extMap = new HashMap<String, String>();
	    extMap.put(".jpg", "image/jpeg");
	    extMap.put(".jpeg", "image/jpeg");
	    extMap.put(".png", "image/png");
	    extMap.put(".gif", "image/gif");
		request.setCharacterEncoding("UTF-8"); // ���ô�����������ı����ʽ
		DiskFileItemFactory  factory = new DiskFileItemFactory(); // ����FileItemFactory����
		factory.setSizeThreshold(16*4096*1024);
		ServletFileUpload upload = new ServletFileUpload(factory);
		// �������󣬲��õ��ϴ��ļ���FileItem����
		upload.setSizeMax(16*4096*1024);
		List<FileItem> items = null;
		try {
			items =upload.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
			return "-1";
		}
		String filename = ""; // �ϴ��ļ����浽���������ļ���
		InputStream is = null; // ��ǰ�ϴ��ļ���InputStream����
		// ѭ�������ϴ��ļ�
		for (FileItem item : items){
			// ������ͨ�ı���
			if (item.isFormField()){
				/*if(item.getFieldName().equals("comid")){
					if(!item.getString().equals(""))
						comId = item.getString("UTF-8");
				}*/
				
			}else if (item.getName() != null && !item.getName().equals("")){// �����ϴ��ļ�
				// �ӿͻ��˷��͹������ϴ��ļ�·���н�ȡ�ļ���
				logger.error(item.getName());
				filename = item.getName().substring(item.getName().lastIndexOf("\\")+1);
				is = item.getInputStream(); // �õ��ϴ��ļ���InputStream����
				
			}
		}
		String file_ext =filename.substring(filename.lastIndexOf(".")).toLowerCase();// ��չ��
		String picurl = uin+"_"+System.currentTimeMillis()+file_ext;
		BufferedInputStream in = null;  
		ByteArrayOutputStream byteout =null;
	    try {
	    	in = new BufferedInputStream(is);   
	    	byteout = new ByteArrayOutputStream(1024);        	       
		      
	 	    byte[] temp = new byte[1024];        
	 	    int bytesize = 0;        
	 	    while ((bytesize = in.read(temp)) != -1) {        
	 	          byteout.write(temp, 0, bytesize);        
	 	    }        
	 	      
	 	    byte[] content = byteout.toByteArray(); 
	 	    DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
		    mydb.requestStart();
			  
		    DBCollection collection = mydb.getCollection(table);
		  //  DBCollection collection = mydb.getCollection("records_test");
			  
			BasicDBObject document = new BasicDBObject();
			document.put("uin",  uin);
			document.put("ctime",  System.currentTimeMillis()/1000);
			document.put("type", extMap.get(file_ext));
			document.put("content", content);
			document.put("filename", picurl);
			  //��ʼ����
			mydb.requestStart();
			collection.insert(document);
			  //��������
			mydb.requestDone();
			in.close();        
		    is.close();
		    byteout.close();
		    logger.error(">>>>�ϴ�ͼƬ��� .....");
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}finally{
			if(in!=null)
				in.close();
			if(byteout!=null)
				byteout.close();
			if(is!=null)
				is.close();
		}
	    
		return picurl;
	}

/**
 * ���֧��������
 * @param id
 * @param total
 * @param ticketId �Ż�ȯ��� 
 * @return
 */
	public int payCarStopOrder(Long id,Double total,Long ticketId) {
		Long ntime = System.currentTimeMillis()/1000;
		
		//�鳵��������Ա���������ţ����ƺ�
		Map cotMap = daService.getMap("select cid,uin,euid,car_number from carstop_order_tb where id=?  ", new Object[]{id});
		Long uin =(Long) cotMap.get("uin");
		Long uid =(Long) cotMap.get("euid");
		Long cid =(Long) cotMap.get("cid");
		String carNumber = (String)cotMap.get("car_number");
		
		//����Ա����,comidΪ0��ͣ��������Ա��Ϊ����ʱ��Ϊ��Ӧ��ͣ����
		Long comId  = -1L;
		Map userMap=daService.getMap("select comid from user_info_Tb where id =?", new Object[]{uid});
		if(userMap!=null){
			comId = (Long)userMap.get("comid");
		}
		//�鲴�������� 
		Map csMap = daService.getMap("select name from car_stops_tb where id=? ", new Object[]{cid});
		String comName ="";
		if(csMap!=null)
			comName = (String)csMap.get("name");
		//��ͣ��ȯ
		Double ticketMoney = 0d;
		if(ticketId!=null&&ticketId>0){
			if(!memcacheUtils.readUseTicketCache(uin))//����ʹ��3�Σ����ز��ɹ�!
				return -13;
			Map ticketMap = daService.getMap("select money,type from ticket_tb where limit_day>=? and id=? and state=?",
					new Object[]{TimeTools.getToDayBeginTime(),ticketId,0});
			if(ticketMap!=null&&ticketMap.get("money")!=null&&Check.isDouble(ticketMap.get("money")+"")){
				Integer type = (Integer)ticketMap.get("type");
				if(type!=null&&type==2){//�����г�΢��ר��ȯ
					ticketMoney = Double.valueOf(ticketMap.get("money")+"");
					ticketMoney = (10-ticketMoney)*total*0.1;
				}else {
					ticketMoney = Double.valueOf(ticketMap.get("money")+"");
				}
			}
		}
		
		if(ticketMoney>total){//�Ż�ȯ������֧�����
			ticketMoney=total;
		}else {//�����˻��������Ż�ȯ���
			Double ubalance =null;
			//�����˻����
			userMap = daService.getPojo("select balance from user_info_tb where id =?",	new Object[]{uin});
			if(userMap!=null&&userMap.get("balance")!=null){
				ubalance = Double.valueOf(userMap.get("balance")+"");
				ubalance +=ticketMoney;//�û��������Ż�ȯ���
			}
			logger.error(">>>>>>>>>>>>>>>>>>ticket money��"+ticketMoney);
			if(ubalance==null||ubalance<total){//�ʻ�����
				return -12;
			}
		}
		logger.error(">>>>>>>>>>carstoporder,comid:"+comId+",ticket:"+ticketId+",uin:"+uin+",uid:"+uid);
		
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		//�����û����
		Map<String, Object> userSqlMap = new HashMap<String, Object>();
		//����ͣ�������
	    Map<String, Object> comSqlMap = new HashMap<String, Object>();
		//�����˻�
		Map<String, Object> userAccountsqlMap =new HashMap<String, Object>();
		//�����˻�
		Map<String, Object> parkAccountsqlMap =new HashMap<String, Object>();
		//�����˻���ͣ��ȯ
		Map<String, Object> userTicketAccountsqlMap =new HashMap<String, Object>();
		//ʹ��ͣ��ȯ����
		Map<String, Object> ticketsqlMap =new HashMap<String, Object>();
		//ͣ�����˻���ͣ��ȯ���
		Map<String, Object> tingchebaoAccountsqlMap =new HashMap<String, Object>();
		
		//�۳������˻����
		userSqlMap.put("sql", "update user_info_tb  set balance =balance-? where id=?");
		userSqlMap.put("values", new Object[]{total-ticketMoney,uin});
		if(total-ticketMoney>0)
			bathSql.add(userSqlMap);
		//�����˻��Ż�ȯ��ֵ
		if(ticketMoney>0&&ticketId!=null&&ticketId>0){//ʹ��ͣ��ȯ���������˻��ȳ�ֵ
			userTicketAccountsqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type) values(?,?,?,?,?,?)");
			userTicketAccountsqlMap.put("values", new Object[]{uin,ticketMoney,0,ntime-1,"ͣ��ȯ��ֵ",7});
			bathSql.add(userTicketAccountsqlMap);
		}
		//�����˻�֧��ͣ������ϸ
		userAccountsqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,uid,target) values(?,?,?,?,?,?,?,?)");
		userAccountsqlMap.put("values", new Object[]{uin,total,1,ntime,"������-"+comName,0,uid,1});
		bathSql.add(userAccountsqlMap);

		//������Ĭ�ϸ������˻�20141120����Ҫ���޸�
		if(comId!=0){//д�빫˾�˻�
			comSqlMap.put("sql", "update com_info_tb  set total_money =total_money+?,money=money+? where id=?");
			comSqlMap.put("values", new Object[]{total,total,comId});
			bathSql.add(comSqlMap);
			
			parkAccountsqlMap.put("sql", "insert into park_account_tb(comid,amount,type,create_time,remark,uid,source) values(?,?,?,?,?,?,?)");
			parkAccountsqlMap.put("values",  new Object[]{comId,total,0,ntime,"������_"+carNumber,uid,2});
			bathSql.add(parkAccountsqlMap);
		}else {//д��ͣ�����˻�
			parkAccountsqlMap.put("sql", "insert into tingchebao_account_tb(amount,type,create_time,remark,utype) values(?,?,?,?,?)");
			parkAccountsqlMap.put("values", new Object[]{total,1,ntime,"������-����"+carNumber,5});
			bathSql.add(parkAccountsqlMap);
		}
		
		//�Ż�ȯʹ�ú󣬸���ȯ״̬�����ͣ�����˻�֧����¼
		if(ticketMoney>0&&ticketId!=null&&ticketId>0){
			ticketsqlMap.put("sql", "update ticket_tb  set state=?,comid=?,utime=?,umoney=? where id=?");
			ticketsqlMap.put("values", new Object[]{1,comId,System.currentTimeMillis()/1000,ticketMoney,ticketId});
			bathSql.add(ticketsqlMap);
			
			tingchebaoAccountsqlMap.put("sql", "insert into tingchebao_account_tb(amount,type,create_time,remark,utype) values(?,?,?,?,?)");
			tingchebaoAccountsqlMap.put("values", new Object[]{ticketMoney,1,ntime,"����"+carNumber+"��ʹ��ͣ������ȯ",0});
			bathSql.add(tingchebaoAccountsqlMap);
			memcacheUtils.updateUseTicketCache(uin);//��ȯ����ʹ��ȯ����
		}
		
		boolean result= daService.bathUpdate(bathSql);
		logger.error(">>>>>>>>>>>>>>>֧�� ��"+result);
		if(result){//����ɹ�������ȯ������ 
			//�����֣�����������ȯ��������
			/* ÿ��������΢�Ż�֧����֧��1Ԫ���ϵ���ɵģ���������2Ԫ����������3Ԫ��ͣ��ȯ��
			 * �������ֲ���(ͬһ����ÿ��ֻ�ܷ�3��)��
			 * ����ÿ�շ�ȯ��3��ȯ
			 * ÿ������ÿ��ʹ��ͣ��ȯ������3�� */
			boolean isBlack = isBlackUser(uin);
			if(!isBlack){
				backTicket(total-ticketMoney, 997L, uin,comId,"");
			}else {
				logger.error(">>>>>black>>>>������"+uin+",�ں������ڣ����ܷ����......");
			}
			if(total>=1)
				handleRecommendCode(uin,isBlack);
			return 5;
		}else {
			return -7;
		}
	}
	
	/*
	 * ΢�Ź��ں�ҡһҡ���˺�
	 */
	public int sharkbinduser(String openid, Long uin, Long bind_count){//ҡһҡ���˺�
		Long curTime = System.currentTimeMillis()/1000;
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		Map<String, Object> wxuserMap = daService.getMap(
				"select * from wxp_user_tb where openid=? ",
				new Object[] { openid });//δ���˻�
		logger.error(">>>>>>>>>>>>>�����΢�Ź��ں�>>>>>>>>>>>>,openid:"+openid);
		if(wxuserMap != null){//ʹ�ù�ҡһҡֱ��
			logger.error(">>>>>>>>>>>>>>>>>�������˻�����Ϣת�Ƶ��󶨵���ʵ�ʻ���>>>>>>>>>>>>>�������˺�uin��"+wxuserMap.get("uin")+",��ʵ�˺�uin:"+uin);
			logger.error(">>>>>>>>>>>���������˻����Ƽ��߼�>>>>>>>>>>>>>>>>>>");
//			handleWxRecommendCode((Long)wxuserMap.get("uin"), bind_count);
			Double wx_balance = 0d;//�����˻�������
			if(wxuserMap.get("balance") != null){
				 wx_balance = Double.valueOf(wxuserMap.get("balance") + "");
			}
			/*Map<String, Object> recomsqlMap =new HashMap<String, Object>();
			recomsqlMap.put("sql", "update recommend_tb set nid=? where nid=?");
			recomsqlMap.put("values", new Object[]{ uin, wxuserMap.get("uin") });
			bathSql.add(recomsqlMap);*/
			
			Map<String, Object> trueUsersqlMap =new HashMap<String, Object>();
			trueUsersqlMap.put("sql", "update user_info_tb set balance=balance+? where id=?");
			trueUsersqlMap.put("values", new Object[]{ wx_balance, uin });
			bathSql.add(trueUsersqlMap);
			
			Map<String, Object> userAccountsqlMap =new HashMap<String, Object>();
			userAccountsqlMap.put("sql", "update user_account_tb set uin=? where uin=?");
			userAccountsqlMap.put("values", new Object[]{uin, wxuserMap.get("uin")});
			bathSql.add(userAccountsqlMap);
			
			//order_ticket_tb
			Map<String, Object> orderTicketsqlMap =new HashMap<String, Object>();
			orderTicketsqlMap.put("sql", "update order_ticket_tb set uin=? where uin=?");
			orderTicketsqlMap.put("values", new Object[]{uin, wxuserMap.get("uin")});
			bathSql.add(orderTicketsqlMap);
			
			//ticket_tb
			Map<String, Object> ticketsqlMap =new HashMap<String, Object>();
			ticketsqlMap.put("sql", "update ticket_tb set uin=? where uin=?");
			ticketsqlMap.put("values", new Object[]{uin, wxuserMap.get("uin")});
			bathSql.add(ticketsqlMap);
			
			//΢�Ź��ں��û���
			Map<String, Object> wxusersqlMap =new HashMap<String, Object>();
			wxusersqlMap.put("sql", "delete from wxp_user_tb where openid=?");
			wxusersqlMap.put("values", new Object[]{ openid });
			bathSql.add(wxusersqlMap);
			
			//�û��ʻ���
			Map<String, Object> userPayAccountsqlMap =new HashMap<String, Object>();
			userPayAccountsqlMap.put("sql", "update user_payaccount_tb set uin=? where uin=?");
			userPayAccountsqlMap.put("values", new Object[]{uin, wxuserMap.get("uin")});
			bathSql.add(userPayAccountsqlMap);
			
			//��־��
			Map<String, Object> logsqlMap =new HashMap<String, Object>();
			logsqlMap.put("sql", "update alipay_log set uin=? where uin=?");
			logsqlMap.put("values", new Object[]{uin, wxuserMap.get("uin")});
			bathSql.add(logsqlMap);
			
			Map<String, Object> ordersqlMap = new HashMap<String, Object>();
			ordersqlMap.put("sql", "update order_tb set uin=? where uin=? ");
			ordersqlMap.put("values", new Object[]{uin, wxuserMap.get("uin")});
			bathSql.add(ordersqlMap);
			
			Map<String, Object> rewardsqlMap = new HashMap<String, Object>();
			rewardsqlMap.put("sql", "update parkuser_reward_tb set uin=? where uin=? ");
			rewardsqlMap.put("values", new Object[]{uin, wxuserMap.get("uin")});
			bathSql.add(rewardsqlMap);
			
			Integer addcar_flag = 0;//����ӳ��ƺ�
			if(wxuserMap.get("car_number") != null){
				Long count = daService.getLong("select count(*) from car_info_tb where car_number=? ",
						new Object[] { wxuserMap.get("car_number") });
				if(count == 0){
					addcar_flag = 1;//��ӳ��ƺ�
					Map<String, Object> carsqlMap = new HashMap<String, Object>();
					carsqlMap.put("sql", "insert into car_info_Tb (uin,car_number,create_time) values(?,?,?)");
					carsqlMap.put("values", new Object[]{uin, wxuserMap.get("car_number"), curTime});
					bathSql.add(carsqlMap);
				}
			}
			
			
			boolean b = daService.bathUpdate2(bathSql);
			if(b){
				b = memcacheUtils.readUseTicketCache((Long)wxuserMap.get("uin"));
				
				if(!b){
					memcacheUtils.updateUseTicketCache(uin);
					logger.error(">>>>>>>>>>>>>>>΢�Ź��ںŰ��˻����������˺Ž������ù�ȯ���󶨵���ʵ�˺�д������������˺ţ�"+wxuserMap.get("uin")+"��ʵ�˺ţ�"+uin);
				}else{
					logger.error(">>>>>>>>>>>>>>>΢�Ź��ںŰ��˻����������˺Ž���û���ù�ȯ�������˺ţ�"+wxuserMap.get("uin")+"��ʵ�˺ţ�"+uin);
				}
				
				if(addcar_flag == 1){
					logger.error(">>>>>>>>>>>�����˺����г��ó��ƺţ�����֮ǰ����û��ע�ᳵ�ƣ��Ѹó���ע��Ϊ�û����ƣ������˺ţ�"+wxuserMap.get("uin")+",��ʵ�˺ţ�"+uin+",���ƺţ�"+wxuserMap.get("car_number"));
					Map<String, Object> userMap = daService.getMap(
							"select mobile from user_info_tb where id=? ",
							new Object[] { uin });
					if(userMap != null){
						logger.error(">>>>>>>>>>������ӳ��ƺź�Ĳ�ѯ����߼�,�����˺ţ�"+wxuserMap.get("uin")+",��ʵ�˺ţ�"+uin+",���ƺţ�"+wxuserMap.get("car_number"));
						methods.checkBonus((String)userMap.get("mobile"), uin);
					}
				}
				return 1;
			}
		}
		return 0;
	}
	
	public int handleWxRecommendCode(Long nid, Long bind_count){
		logger.error("handleWxRecommendCode>>>>>���Ƽ���nid:"+nid);
		Map<String, Object> userMap = daService.getMap("select wxp_openid from user_info_tb where id=? ", new Object[]{nid});
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
		list = pgOnlyReadService.getAll("select * from recommend_tb where (nid=? or openid=?) and type=? and state=? ",
						new Object[] { nid,userMap.get("wxp_openid"), 0, 0 });
		Long count = daService.getLong("select count(*) from car_info_tb where uin=? and state=? ", new Object[]{nid, 1});
		boolean isBlack = isBlackUser(nid);
		if(list.isEmpty()){
			logger.error("handleWxRecommendCode>>>>>���û�û�б��Ƽ���¼,���Ƽ���nid:"+nid);
			return 1;
		}else{
			logger.error("handleWxRecommendCode>>>>>���û��б��Ƽ���¼,���Ƽ���uin:"+nid);
			if(bind_count == 0){
				logger.error("handleWxRecommendCode>>>>>��ʼ����ɹ��Ƽ��߼������շ�Ա��Ǯ,���Ƽ���uin:"+nid);
			}else{
				logger.error("handleWxRecommendCode>>>>>�����Ƽ���¼��Ч,���Ƽ���uin:"+nid);
			}
			if(isBlack){
				logger.error("handleWxRecommendCode>>>>>���û��ں�������Ƽ�ʧЧ������Ǯuin:"+nid);
			}
		}
		if(count > 0){
			logger.error("handleWxRecommendCode>>>>>:�г���count:"+count+",uin:"+nid);
			for(Map<String, Object> map : list){
				Double money = 5d;//Ĭ�Ϸ�5��
				Long uid = -1L;
				Integer parker_flag = 0;//0:���շ�Ա�Ƽ���1�շ�Ա�Ƽ�
				if(map.get("pid") != null){
					uid = (Long)map.get("pid");
					Map usrMap =daService.getMap("select recommendquota from user_info_Tb where id =? ", new Object[]{uid});
					if(usrMap!=null){
						money = StringUtils.formatDouble(Double.parseDouble(usrMap.get("recommendquota")+""));
						logger.error("���շ�Ա���Ƽ�������ǣ�"+money);
					}
					boolean isParkBlack = isBlackParkUser(uid,true);
					if(isParkBlack)
//						continue;
						return 0;
					Long count1 = daService.getLong(
									"select count(*) from user_info_tb where id=? and (auth_flag=? or auth_flag=?) ",
									new Object[] { uid, 1, 2 });
					if(count1 > 0){
						parker_flag = 1;//���շ�Ա�Ƽ���
					}
				}
				if(map.get("money") != null && Double.valueOf(map.get("money") + "")>0){
					money = Double.valueOf(map.get("money") + "");
				}
				if(parker_flag == 1 && count> 0){
					if(bind_count == 0 && !isBlack ){
						Long comId = -1L;
						Map comMap = daService.getPojo("select comid from user_info_tb where id=?  ",new Object[] {uid});
						Map msetMap =null;
						Integer giveMoneyTo = null;//��ѯ�շ��趨 mtype:0:��˾�˻���1�������˻�'
						if(comMap!=null){
							comId =(Long)comMap.get("comid");
							if(comId!=null&&comId>0);
								msetMap = daService.getPojo("select giveto from money_set_tb where comid=? and mtype=? ",
									new Object[]{comId,4});
						}
						if(msetMap!=null)
							giveMoneyTo =(Integer)msetMap.get("giveto");
						if(giveMoneyTo!=null&&giveMoneyTo==0&&comId!=null&&comId>0){//���ָ�ͣ�����˻�
							Map<String, Object> comqlMap = new HashMap<String, Object>();
							//ͣ�����˻�����
							comqlMap.put("sql", "update com_info_tb set total_money=total_money+?,money=money+?  where id=? ");
							comqlMap.put("values", new Object[]{money,money,comId});
							bathSql.add(comqlMap);
							
							//д��ͣ�����˻���ϸ
							Map<String, Object> parkAccountMap = new HashMap<String, Object>();
							parkAccountMap.put("sql", "insert into park_account_tb(comid,amount,type,create_time,remark,uid,source) " +
									"values(?,?,?,?,?,?,?)");
							parkAccountMap.put("values", new Object[]{comId,money,0,System.currentTimeMillis()/1000,"�Ƽ�����",uid,3});
							bathSql.add(parkAccountMap);
							logger.error(uid+">>>�Ƽ�������ͣ����");
							
						}else {//���ָ��շ�Ա�˻�
							Map<String, Object> usersqlMap = new HashMap<String, Object>();
							//�շ�Ա�˻�����
							usersqlMap.put("sql", "update user_info_tb set balance=balance+? where id=? ");
							usersqlMap.put("values", new Object[]{money,uid});
							bathSql.add(usersqlMap);
							
							//д���շ�Ա�˻���ϸ
							Map<String, Object> parkuserAccountMap = new HashMap<String, Object>();
							parkuserAccountMap.put("sql", "insert into parkuser_account_tb(uin,amount,type,create_time,remark,target) " +
									"values(?,?,?,?,?,?)");
							parkuserAccountMap.put("values", new Object[]{uid,money,0,System.currentTimeMillis()/1000,"�Ƽ�����",3});
							bathSql.add(parkuserAccountMap);
						}
						//�����Ƽ���¼
						Map<String, Object> recomsqlMap = new HashMap<String, Object>();
						recomsqlMap.put("sql", "update recommend_tb set state=? where (nid=? or openid=?) and pid=?");
						recomsqlMap.put("values", new Object[]{1,nid,userMap.get("wxp_openid"),uid});
						bathSql.add(recomsqlMap);
					}else{
						//�����Ƽ���¼
						Map<String, Object> recomsqlMap = new HashMap<String, Object>();
						recomsqlMap.put("sql", "update recommend_tb set state=? where (nid=? or openid=?) and pid=?");
						recomsqlMap.put("values", new Object[]{3,nid,userMap.get("wxp_openid"),uid});
						bathSql.add(recomsqlMap);
					}
				}
			}
			boolean b = false;
			if(!bathSql.isEmpty()){
				b = daService.bathUpdate2(bathSql);
			}
			
			if(b){
				logger.error("handleWxRecommendCode>>>>>�Ƽ��߼�����ɹ�,���Ƽ���uin:"+nid);
			}else{
				logger.error("handleWxRecommendCode>>>>>�Ƽ��߼�����ʧ��uin:"+nid);
			}
			
			if(b){
				return 1;
			}else{
				return 0;
			}
		}else{
			logger.error("handleWxRecommendCode>>>>>:�޳���count:"+count+",uin:"+nid);
			return 0;
		}
	}
	
	/**
	 * ȡͣ����
	 * @param lat
	 * @param lon
	 * @param payable�Ƿ��֧��
	 * @return 2000���ڵ�ͣ����
	 */
	public List<Map<String, Object>> getPark2kmList(Double lat,Double lon,Integer payable){
//		payable=1;//ǿ�ƹ��˲���֧������
		double lon1 = 0.023482756;
		double lat1 = 0.017978752;
		String sql = "select id,company_name as name,longitude lng,latitude lat,parking_total total,share_number," +
				"address addr,phone,monthlypay,epay,type,isfixed from com_info_tb where longitude between ? and ? " +
				"and latitude between ? and ? and state=? and isview=? ";//and isfixed=? ";
		List<Object> params = new ArrayList<Object>();
		params.add(lon-lon1);
		params.add(lon+lon1);
		params.add(lat-lat1);
		params.add(lat+lat1);
		params.add(0);
		params.add(1);
	//	params.add(1);
//		if(payable==1){
//			sql +=" and isfixed=? and epay=? ";
//			params.add(1);
//			params.add(1);
//		}
		List list = null;//daService.getPage(sql, null, 1, 20);
		list = pgOnlyReadService.getAll(sql, params, 0, 0);
		return list;
	}
	
	/**
	 * ��ȡ���ע���û�
	 * @param mobile �ֻ���
	 * @param media ý����Դ 
	 * @param getcode �Ƿ��ȡ��֤��
	 * @return
	 */
	public Long regUser(String mobile,Long media,Long uid,boolean getcode){
		Long uin = daService.getkey("seq_user_info_tb");
		Long ntime = System.currentTimeMillis()/1000;
		String strid = "zlduser"+uin;
		//�û���
		String sql= "insert into user_info_tb (id,nickname,password,strid," +
				"reg_time,mobile,auth_flag,comid,media,recom_code) " +
				"values (?,?,?,?,?,?,?,?,?,?)";
		Object[] values= new Object[]{uin,"����",strid,strid,ntime,mobile,4,0,media.intValue(),uid};
		//2015-03-10������������ʱ������д��ͣ��ȯ����¼ʱ�жϺ����������ͣ��ȯ
		/*if(media==8||media==7){//7"��360",8"������"
			String tsql = "insert into ticket_tb (create_time,limit_day,money,state,uin) values(?,?,?,?,?) ";
			List<Object[]> insertvalues = new ArrayList<Object[]>();
			//Long ntime = System.currentTimeMillis()/1000;
			Object[] v1 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			Object[] v2 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			Object[] v3 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			Object[] v4 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			Object[] v5 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			Object[] v6 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			Object[] v7 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			Object[] v8 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			Object[] v9 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			Object[] v10 = new Object[]{ntime,ntime+15*24*60*60,10,0,uin};
			insertvalues.add(v1);insertvalues.add(v2);insertvalues.add(v3);insertvalues.add(v4);insertvalues.add(v5);
			insertvalues.add(v6);insertvalues.add(v7);insertvalues.add(v8);insertvalues.add(v9);insertvalues.add(v10);
			int result= daService.bathInsert(tsql, insertvalues, new int[]{4,4,4,4,4});
			if(result>0){
				logService.insertUserMesg(1, uin, "��ϲ�����ʮ��10Ԫͣ��ȯ!", "�������");
			}
		}else {*/
			int ts = backNewUserTickets(ntime, uin);
			logger.error("��ȡ���ע���û�����ȯ��"+ts+",��ֱ��д��ͣ��ȯ���У���¼ʱ��֤�Ƿ��Ǻ������󷵻�");
//		}
		int r = daService.update(sql,values);
		logger.error("��ȡ���ע���û���ע������"+r);
		if(r==1){
			//ע��ɹ�����һ���Ƿ����շ�Ա�Ƽ�
			if(media==999&&uid>0){
				Map userMap = daService.getMap("select comid from user_info_Tb where id =? and auth_flag in(?,?) and state=?", 
						new Object[]{uid,1,2,0}) ;
				Long comId =null;
				if(userMap!=null){
					comId =(Long)userMap.get("comid");
				}
				if(comId!=null&&comId>0){
					int rem = daService.update("insert into recommend_tb (pid,nid,type,state,create_time) values(?,?,?,?,?)",
							new Object[]{uid,uin,0,0,System.currentTimeMillis()/1000});
//					if(uid!=null&&comId!=null)
//						logService.updateScroe(5, uid, comId);//�Ƽ���������1���� 
					//int backmoney = daService.update("update user_info_tb set balance=balance+5 where id=?", new Object[]{uid});
					logger.error("�շ�Ա�Ƽ�������ͨ����ȡ���ע��ɹ�,�Ƽ���¼��"+rem);
				}else {
					logger.error("�շ�Ա�Ƽ�������ͨ����ȡ���ע��ɹ������Ƽ����շ�Ա������:"+uid);
				}
			}
			
			
			int	eb = daService.update("insert into user_profile_tb (uin,low_recharge,limit_money,auto_cash," +
					"create_time,update_time) values(?,?,?,?,?,?)", 
					new Object[]{uin,10,25,1,ntime,ntime});
			logger.error("�շ�Ա�Ƽ��������Զ�֧������:"+eb);
			if(!getcode){
				//ע��ɹ������Ͷ���
				String mesg ="��ʵ���ͣ���ѣ���������ͣ������ͣ���������Żݣ�8ԪǮͣ5�γ������ص�ַ�� http://t.cn/RZJ4UAv ��ͣ������";
				SendMessage.sendMultiMessage(mobile, mesg);
			}
			return uin;
		}
		return -1L;
	}
	
	/**
	 * �����շ�Ա�Ƽ����֣�������һ�ʣ���֧�����1Ԫ���ϣ����ں������ڵĳ������ŷ��ָ��շ�Ա
	 * @param uin  ����
	 */
	private void handleRecommendCode(Long uin,boolean isBlack){
		Long recom_code = null;
		Map recomMap = daService.getMap("select pid from recommend_tb where nid=? and state=? and type=? ", new Object[]{uin,0,0});
		if(recomMap==null||recomMap.isEmpty()){//û����س���δ������Ƽ���ֱ�ӷ���
			logger.error(">>>>>>>>>>handle recommend,error: no pid ,uin:"+uin);
			return ;
		}else {
			recom_code = (Long )recomMap.get("pid");
		}
		//logger.error();
		Map usrMap = daService.getMap("select recom_code from user_info_tb where id=?", new Object[]{uin});
		if(usrMap == null){//�����������˻�����δ���˻�
			return;
		}
		logger.error(">>>>>>>>>>handle recommend"+usrMap);
		Long uid = (Long)usrMap.get("recom_code");
		if(recom_code==null||uid==null||recom_code.intValue()!=uid.intValue()||isBlackParkUser(recom_code,true)){
			logger.error(">>>>>>>>>>handle recommend,error:  recomCode:"+recom_code+",uid:"+uid);
			return ;
		}
		usrMap =daService.getMap("select auth_flag,mobile,recommendquota from user_info_Tb where id =? ", new Object[]{uid});
		logger.error(">>>>>>>>>>handle recommend"+usrMap);
		String mobile = "";
		
		//�Ƽ��˽�ɫ
		Long auth_flag = null;
		Double recommendquota = 5d;
		if(usrMap!=null){
			auth_flag = (Long) usrMap.get("auth_flag");
			mobile = (String)usrMap.get("mobile");
			recommendquota = StringUtils.formatDouble(Double.parseDouble(usrMap.get("recommendquota")+""));
			logger.error("���շ�Ա���Ƽ�������ǣ�"+recommendquota);
		}
		
		if(isBlack){
			String mobile_end = mobile.substring(7);
			int result =daService.update("insert into parkuser_message_tb(type,ctime,uin,title,content) values(?,?,?,?,?)",
					new Object[]{0,System.currentTimeMillis()/1000, uid, "�Ƽ�����", "���Ƽ��ĳ������ֻ�β��"+mobile_end+"�����˻���ˢ�����ɣ�����ȡ����"} );
			int result1 = daService.update("update recommend_tb set state=? where nid=? and pid=?", new Object[]{2,uin,uid});
			logger.error(">>>>>>>>>���������շ�Ա �����Ƽ��ĳ����ں������У�����ȡ��:����Ϣ��"+result+"���Ƽ�����Ϊ��������"+result1);
			return ;
		}
		
		//���շ�Ա�Ƽ��ĳ�����Ŀǰû�г����Ƽ������ļ�¼
		if(auth_flag!=null&&(auth_flag==1||auth_flag==2)){
			Long count  = daService.getLong("select count(ID) from recommend_tb where nid=? and pid=? and state=? and type=?", new Object[]{uin,uid,0,0});
			//�Ƽ�����.0��������1:����
			logger.error("is recom:"+count);
			if(count!=null&&count>0){//���Ƽ������Ƽ��˵Ľ���û��֧��//���������շ�Ա�˺�5Ԫ
				List<Map<String , Object>> sqlMaps = new ArrayList<Map<String,Object>>();
				Long comId = -1L;
				Map comMap = daService.getPojo("select comid from user_info_tb where id=?  ",new Object[] {uid});
				Map msetMap =null;
				Integer giveMoneyTo = null;//��ѯ�շ��趨 mtype:0:��˾�˻���1�������˻�'
				if(comMap!=null){
					comId =(Long)comMap.get("comid");
					if(comId!=null&&comId>0);
						msetMap = daService.getPojo("select giveto from money_set_tb where comid=? and mtype=? ",
							new Object[]{comId,4});
				}
				if(msetMap!=null)
					giveMoneyTo =(Integer)msetMap.get("giveto");
				if(comId!=null&&comId>0&&giveMoneyTo!=null&&giveMoneyTo==0){//���ָ�ͣ�����˻�
					Map<String, Object> comqlMap = new HashMap<String, Object>();
					//ͣ�����˻�����
					comqlMap.put("sql", "update com_info_tb set total_money=total_money+?,money=money+?  where id=? ");
					comqlMap.put("values", new Object[]{recommendquota,recommendquota,comId});
					sqlMaps.add(comqlMap);
					
					//д��ͣ�����˻���ϸ
					Map<String, Object> parkAccountMap = new HashMap<String, Object>();
					parkAccountMap.put("sql", "insert into park_account_tb(comid,amount,type,create_time,remark,uid,source) " +
							"values(?,?,?,?,?,?,?)");
					parkAccountMap.put("values", new Object[]{comId,recommendquota,0,System.currentTimeMillis()/1000,"�Ƽ�����",uid,3});
					sqlMaps.add(parkAccountMap);
					logger.error(uid+">>>�Ƽ�������ͣ����");
				}else {
					Map<String, Object> usersqlMap = new HashMap<String, Object>();
					//�շ�Ա�˻���5Ԫ
					usersqlMap.put("sql", "update user_info_tb set balance=balance+? where id=? ");
					usersqlMap.put("values", new Object[]{recommendquota,uid});
					sqlMaps.add(usersqlMap);
				
					//д���շ�Ա�˻���ϸ
					Map<String, Object> parkuserAccountMap = new HashMap<String, Object>();
					parkuserAccountMap.put("sql", "insert into parkuser_account_tb(uin,amount,type,create_time,remark,target) " +
							"values(?,?,?,?,?,?)");
					parkuserAccountMap.put("values", new Object[]{uid,recommendquota,0,System.currentTimeMillis()/1000,"�Ƽ�����",3});
					sqlMaps.add(parkuserAccountMap);
					
				}
				//�����Ƽ���¼
				Map<String, Object> recomsqlMap = new HashMap<String, Object>();
				recomsqlMap.put("sql", "update recommend_tb set state=?,money=? where nid=? and pid=?");
				recomsqlMap.put("values", new Object[]{1,recommendquota,uin,uid});
				sqlMaps.add(recomsqlMap);
				
				logger.error(count);
				boolean ret = daService.bathUpdate(sqlMaps);
				if(ret){//д���շ�Ա��Ϣ��
					
					String mobile_end = mobile.substring(7);
					int result =daService.update("insert into parkuser_message_tb(type,ctime,uin,title,content) values(?,?,?,?,?)",
							new Object[]{0,System.currentTimeMillis()/1000, uid, "�Ƽ�����", "���Ƽ��ĳ������ֻ�β��"+mobile_end+"��ע��ɹ�������"+recommendquota+"Ԫ������"} );
					logger.error(">>>>>>>>>���������շ�Ա�Ƽ�����"+recommendquota+"Ԫ��Ϣ:"+result);
				}
				logger.error(">>>>>>>>>���������շ�Ա�Ƽ�����"+recommendquota+"Ԫ��"+ret);
			}
		}else {
			logger.error(uid);
		}
	}
	
	public String getCollectMesgSwith(){
		String swith = memcacheUtils.doStringCache("collectormesg_swith", null, null);
		if(swith==null)
			return "0";
		return swith;
	}
	
	
	/*public void updateSorceq(Long btime,Long etime,Integer cType,Long uid,Long comId){
		if(cType!=null&&btime!=null&&(etime-btime>=15*60)){//����ʱ������15���ӣ����Լ�һ��0.2�Ļ���
			if(cType==0)//NFC����  ( ˢNFC������ɨ��������Ч������������֧����0.01�֣�����֧������һԪ����2�֡�)
				logService.updateScroe(2, uid,comId);
			else if(cType==2||cType==3)//ɨ�ƻ����ƻ��� 
				logService.updateScroe(4, uid,comId);
		}
	}*/
	
	public void setCityCache(Long comid,Integer city){
		Map<Long, Integer> map = memcacheUtils.doMapLongIntegerCache("comid_backmoney_cache", null, null);
		if(map==null||map.size()<20){
			List<Map<String, Object>> idlist= pgOnlyReadService.getAll("select id from com_info_tb where city between ? and ? ", new Object[]{370100,370199});
			if(idlist!=null){
				if(map==null)
					map = new HashMap<Long, Integer>();
				for(Map<String, Object> maps: idlist){
					Long id = (Long)maps.get("id");
					if(id!=null)
						map.put(id, 1);
				}
				logger.error(">>>>>jinan city cache size:"+map.size());
				memcacheUtils.doMapLongIntegerCache("comid_backmoney_cache", map, "update");
			}
		}else {
			if(city>=110000&&city<120000){
				if(map!=null&&map.containsKey(comid)){
					map.remove(comid);
					memcacheUtils.doMapLongIntegerCache("comid_backmoney_cache", map, "update");
				}
			}else {
				if(!map.containsKey(comid)){
					map.put(comid, 0);
					memcacheUtils.doMapLongIntegerCache("comid_backmoney_cache", map, "update");
				}
			}
		}
	}
	
	public boolean isCanBackMoney(Long comid){
		logger.error(">>>���г���������");
		return false;
//		Map<Long, Integer> map = memcacheUtils.doMapLongIntegerCache("comid_backmoney_cache", null, null);
//		if(map==null){
//			logger.error(">>>û�зǱ���������ֱ�ӷ���...");
//			return true;
//		}else {
//			logger.error(">>>�Ǳ�����������Ϊ:"+map.size()+",comid:"+comid);
//			if(map.containsKey(comid)){
//				/*Integer times = map.get(comid);
//				Integer rand = RandomUtils.nextInt(10);//��0-9��ȡ�������Ϊ8ʱ����
//				if(rand==8){//����
//					times = times+1;
//					map.put(comid, times);
//					memcacheUtils.doMapLongIntegerCache("comid_backmoney_cache", map, "update");
//					logger.error(">>>���ϳ����������Ϸ��ּ���,�¸�������"+times+"������Ϊ:"+map);
//					return true;
//				}else {
//					return false;
//				}
//				logger.error(">>>���ϳ��������ּ���:"+times);
//				if(times%10==0){
//					logger.error(">>>���ϳ��������Ϸ��ּ���:"+times);
//					return true;
//				}else {
//					times = times+1;
//					map.put(comid, times);
//					memcacheUtils.doMapLongIntegerCache("comid_backmoney_cache", map, "update");
//					logger.error(">>>���ϳ����������Ϸ��ּ���,�¸�������"+times+"������Ϊ:"+map);
//					return false;
//				}*/
//				return false;
//			}else {
//				logger.error(">>>���ڷǱ������������ڣ�ֱ�ӷ���...");
//				return true;	
//			}
//		}
	}
	
	
	private Double getBackMoney(){
		Double moneys[] = new Double[]{0d,1d,2d,3d,4d};
		Integer rand = RandomUtils.nextInt(5);
		return moneys[rand];
	}
	
	private double getminPriceUnit(Long comId){
		Map com =daService.getPojo("select * from com_info_tb where id=? "
				, new Object[]{comId});
		double minPriceUnit = Double.valueOf(com.get("minprice_unit")+"");
		return minPriceUnit;
	}
	
	public boolean isCanSendShortMesg(String mobile){
		Map<String, String> sendCache = memcacheUtils.doMapStringStringCache("verification_code_cache", null, null);
		Long ttime = TimeTools.getToDayBeginTime();
		//System.err.println(sendCache);
		if(sendCache==null){
			sendCache = new HashMap<String, String>();
			sendCache.put(mobile, ttime+"_"+1);
		}else {
			String value = sendCache.get(mobile);
			if(value!=null&&value.indexOf("_")!=-1){
				String dayt[] =value.split("_");
				Long time= Long.valueOf(dayt[0]);
				if(time.equals(ttime)){
					Integer times = Integer.valueOf(dayt[1]);
					if(times>9){
						logger.error(mobile+"������֤�볬��10��");
						return false;
					}else {
						value = time+"_"+(times+1);
						sendCache.put(mobile,value);
					}
				}else {
					sendCache.put(mobile, ttime+"_"+1);
				}
			}else {
				sendCache.put(mobile, ttime+"_"+1);
			}
		}
		 memcacheUtils.doMapStringStringCache("verification_code_cache", sendCache, "update");
		return true;
	}

	/**
	 * ����ͣ��ȯ��ߵֿ۽��
	 * @param type 0���ͣ�1֧��
	 * @param uin  �����˻�
	 * @param uid  �շ�Ա�˻�
	 * @param total  ͣ���ѽ��
	 * @return �ֿ۽��
	 */
	public Double useTicket(Long uin,Long uid,Integer type,Double total){
		boolean isAuth = isAuthUser(uin);
		Double maxMoney = 1.0;//δ��֤����ߵ�һԪ
		if(type==0&&isAuth){//0����
			Double rewardquota = 2.0;
			Map user = daService.getMap("select rewardquota from user_info_tb where id = ?", new Object[]{uid});
			if(user!=null&&user.get("rewardquota")!=null)
				rewardquota =StringUtils.formatDouble(user.get("rewardquota"));
			if(rewardquota>total)//�շ�Ա��ߴ������ý����ڴ��ͽ��ʱ����ȯ�������Ϊ���ͽ��
				rewardquota= total;
			maxMoney= rewardquota;
		}else if(type==1&&isAuth){//1֧��
			//֧�������ͣ��ȯ����ֵ����ֿ۽���붩�����Ĳ�ֵΪ1���򶩵�Ϊ10Ԫʱ��ȯ��ߵֿ�9Ԫ��
			//20Ԫ��ȯҲֻ�ֿܵ�9Ԫ�����Ϊ2������ߵֿ�8Ԫ
			Double uselimit = StringUtils.formatDouble(CustomDefind.getValue("TICKET_LIMIT"));
			maxMoney= total-uselimit;
		}
		return maxMoney;
	}
	
	/**
	 * ��ȡ����ȯ�ֿ۽��
	 * @param ticketId
	 * @param ptype 0�˻���ֵ��1���²�Ʒ��2ͣ���ѽ��㣻3ֱ��;4���� 5����ͣ��ȯ
	 * @param uid
	 * @param total ���
	 * @param utype 0��ͨѡȯ��Ĭ�ϣ�1���ô������ֿ۽���ͣ��ȯ
	 * @param comid
	 * @param orderId
	 * @return
	 */
	public Double getTicketMoney(Long ticketId, Integer ptype, Long uid, Double total, Integer utype, Long comid, Long orderId){
		Double ticketMoney = 0d;
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> ticketMap = daService.getMap("select * from ticket_tb where id=? ", new Object[]{ ticketId });
		logger.error("orderid:"+orderId+",ticketid:"+ticketId+",ticketMap:"+ticketMap);
		if(ticketMap != null){
			list.add(ticketMap);
			list = methods.chooseTicketByLevel(list, ptype, uid, total, utype, comid, orderId);
			ticketMap = list.get(0);
			ticketMoney = Double.valueOf(ticketMap.get("limit") + "");
			logger.error("orderid:"+orderId+",ticketMap:"+ticketMap);
		}
		return ticketMoney;
	}
	
	/**
	 * ��ȡ����ȯ�ֿ۽��
	 * @param uin
	 * @param uid
	 * @param total
	 * @return
	 */
	public Double getDisTicketMoney(Long uin, Long uid, Double total){
		Double ticketMoney = 0d;
		Map<String, Object> ticketMap = methods.chooseDistotalTicket(uin, uid, total);
		if(ticketMap != null){
			ticketMoney = StringUtils.formatDouble(ticketMap.get("money"));
		}
		return ticketMoney;
	}
	public boolean isEtcPark(Long comid){
		boolean b = false;
		List<Long> tcache = memcacheUtils.doListLongCache("etclocal_park_cache", null, null);
		if(tcache!=null&&tcache.contains(comid)){
			b = true;
		}else {
			tcache = new ArrayList<Long>();
			List all = pgOnlyReadService.getAll("select comid from local_info_tb", null);
			for (Object object : all) {
				Map map = (Map)object;
				Long obj = Long.valueOf(map.get("comid")+"");
				tcache.add(obj);
			}
			if(tcache!=null&&tcache.contains(comid)){
				b = true;
			}
			memcacheUtils.doListLongCache("etclocal_park_cache", tcache, "update");
		}
//		String etcpark = CustomDefind.ETCPARK;
//		logger.error("comid:"+etcpark);
//		if(StringUtils.isNotNull(etcpark)){
//			String[] strs = etcpark.split(",");
//			for (String str : strs) {
//				if(Long.parseLong(str)==comid.longValue()){
//					b =  true;
//					break;
//				}
//			}
//		}
		logger.error("comid:"+comid+" is etc local park return :"+b);
		return b;
	}
	
	private void updateAllowCache(Long comid,Long ticketId, Double ticketMoney){
		logger.error("updateAllowCache>>>ticketId:"+ticketId+",ticketMoney:"+ticketMoney+",comid:"+comid);
		if(ticketMoney > 0){
			Double tcballow = ticketMoney;//ͣ���������Ĳ���
			if(ticketId != null && ticketId > 0){
				Map<String, Object> ticketMap = daService.getMap(
						"select * from ticket_tb where id=? ",
						new Object[] { ticketId });
				Integer type = (Integer)ticketMap.get("type");
				Integer resources = (Integer)ticketMap.get("resources");
				if(type == 0 && resources == 1){//����ȯ
					if(ticketMap.get("pmoney") != null){
						Double pmoney = Double.valueOf(ticketMap.get("pmoney") + "");
						logger.error("updateAllowCache>>>ticketId:"+ticketId+",ticketMoney:"+ticketMoney);
						if(ticketMoney > pmoney){
							tcballow = ticketMoney - pmoney;
						}else{
							tcballow = 0d;
						}
					}
				}
			}
			logger.error("updateAllowCache>>>ticketId:"+ticketId+",tcballow:"+tcballow);
			memcacheUtils.updateAllowanceCache(tcballow);
			memcacheUtils.updateAllowCacheByPark(comid, tcballow);
		}
	}

	public String getHXpass(Long uin){
		String pass="123456";
		try {
			pass = StringUtils.MD5(uin+System.currentTimeMillis()+"zldhxsys");
			pass = pass.substring(24);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pass ="hxzldpass";
		return pass;
	}
	
	public void updateShopTicket(Long orderid, Long uin){
		int r = daService.update("update ticket_tb set state=?,uin=?,utime=? where orderid=? ", 
				new Object[]{1, uin, System.currentTimeMillis()/1000, orderid});
	}
	/**
	 * ͬ������������
	 * @param uin
	 * @param money
	 */
	public void syncDeltePlateNumber(final Long uin,final String plateNumber){
		ExecutorService messagePool=ExecutorsUtil.getExecutorService();
		messagePool.execute(new Runnable() {
			@Override
			public void run() {
				logger.error("delete user plateNumber,��Ҫͬ��������ƽ̨");
				String url = CustomDefind.UNIONIP+"user/updateuser";
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("user_id", uin);
				paramMap.put("type",4);//1���㶩��  2�޸Ķ������ 4ɾ������
				paramMap.put("plate_number", plateNumber);
				paramMap.put("union_id", CustomDefind.UNIONID);
				paramMap.put("rand", Math.random());
				String ret = "";
				try {
					String linkParams = StringUtils.createLinkString(paramMap);
					logger.error("delete user plateNumbe:"+linkParams);
					String sign =StringUtils.MD5(linkParams+"key="+CustomDefind.UNIONKEY).toUpperCase();
					paramMap.put("sign", sign);
					String param = StringUtils.createJson(paramMap);
					ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
					logger.error("delete user plateNumbe ret :"+ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
