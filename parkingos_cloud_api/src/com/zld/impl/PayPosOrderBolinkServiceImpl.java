package com.zld.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zld.pojo.AutoPayPosOrderReq;
import com.zld.pojo.AutoPayPosOrderResp;
import com.zld.pojo.ManuPayPosOrderReq;
import com.zld.pojo.ManuPayPosOrderResp;
import com.zld.pojo.Order;
import com.zld.pojo.PayEscapePosOrderReq;
import com.zld.pojo.PayEscapePosOrderResp;
import com.zld.service.DataBaseService;
import com.zld.service.PayPosOrderService;
import com.zld.utils.StringUtils;

/**
 * �������㶩��
 * @author Administrator
 *
 */
@Service("bolinkPay")
public class PayPosOrderBolinkServiceImpl implements PayPosOrderService {
	@Autowired
	private DataBaseService writeService;
	Logger logger = Logger.getLogger(PayPosOrderBolinkServiceImpl.class);
	
	/**
	 * �������㶩��,��¼������ϸ�ͳ����˻����
	 */
	@Override
	public AutoPayPosOrderResp autoPayPosOrder(AutoPayPosOrderReq req) {
		AutoPayPosOrderResp resp = new AutoPayPosOrderResp();
		try {
			logger.error(req.toString());
			Long curTime = req.getCurTime();
			Order order = req.getOrder();
			long uid = req.getUid();
			String imei = req.getImei();
			double money = req.getMoney();
			Long brethOrderId = req.getBerthOrderId();
			Long endTime = req.getEndTime();
			long groupid = req.getGroupId();
			long berthSegId = order.getBerthsec_id();
			
			//-----------------------------��ȡ������Ϣ-----------------------------//
			logger.error("order:"+order.toString());
			long orderId = order.getId();
			long berthId = order.getBerthnumber();
			long parkId = order.getComid();
			//Long groupid =order.getGroupid();
			
			//��ѯ�շ��趨 mtype:0:ͣ����,1:Ԥ����,2:ͣ��������
			Map msetMap = writeService.getPojo("select giveto from money_set_tb where comid=? and mtype=? ",
					new Object[]{parkId,0});
			Integer giveTo = null;//0:��˾�˻���1�������˻� ��2����Ӫ�����˻�
			if(msetMap != null){
				giveTo =(Integer)msetMap.get("giveto");
			}
			
			
			//-------------------------------�����߼�-----------------------------//
			logger.error("bolinkpay orderid:"+orderId+"payType:2,money:"+money+",giveto:"+giveTo);
			
			if(giveTo==null){
				if(groupid>0)//Ĭ�ϵ������˻�
					giveTo=2;
				else {
					giveTo=0;//û�м����˻����������˻�
				}
			}else {
				if(giveTo==2&&groupid<1)
					giveTo=0;
			}
			
			Long ntime = System.currentTimeMillis()/1000;
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���¶���״̬
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			//������Ӫ�������
		    Map<String, Object> groupSqlMap = new HashMap<String, Object>();
		    //������Ӫ������ˮ
		    Map<String, Object> groupAccountSqlMap = new HashMap<String, Object>();
			//�շ�Ա�˻�
			Map<String, Object> parkuserAccountsqlMap =new HashMap<String, Object>();
			//�����˻�
			Map<String, Object> parkAccountsqlMap =new HashMap<String, Object>();
			//�շ�Ա���
			Map<String, Object> parkusersqlMap =new HashMap<String, Object>();
			//����ͣ�������
		    Map<String, Object> comSqlMap = new HashMap<String, Object>();
		    //��¼��������
		    Map<String, Object> bolinkSqlMap = new HashMap<String, Object>();
			orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=?,pay_type=?,imei=?,out_uid=? where id=?");
			orderSqlMap.put("values", new Object[]{1, money, endTime,2, imei, uid, orderId});
			bathSql.add(orderSqlMap);
			if(berthId > 0){
				//���²�λ״̬
				Map<String, Object> berthSqlMap = new HashMap<String, Object>();
				berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=?,end_time=? where id =? and order_id=?");
				berthSqlMap.put("values", new Object[]{0, null, endTime, berthId, orderId});
				bathSql.add(berthSqlMap);
			}
			if(brethOrderId > 0){
				//���³���������״̬
				Map<String, Object> berthOrderSqlMap = new HashMap<String, Object>();
				berthOrderSqlMap.put("sql", "update berth_order_tb set out_uid=?,order_total=? where id=? ");
				berthOrderSqlMap.put("values", new Object[]{uid, money, brethOrderId});
				bathSql.add(berthOrderSqlMap);
			}

			if(giveTo == 0){//0:д�빫˾�˻�
				comSqlMap.put("sql", "update com_info_tb set total_money =total_money+?,money=money+? where id=?");
				comSqlMap.put("values", new Object[]{money,money,parkId});
				bathSql.add(comSqlMap);
				
				parkAccountsqlMap.put("sql", "insert into park_account_tb(comid,amount,type,create_time,remark,uid,source,orderid," +
						"berthseg_id,berth_id,groupid) values(?,?,?,?,?,?,?,?,?,?,?)");
				parkAccountsqlMap.put("values",  new Object[]{parkId,money,0,ntime,"ͣ����_"+order.getCar_number(),uid,
						0, orderId, berthSegId, berthId, groupid});
				bathSql.add(parkAccountsqlMap);
			}else if(giveTo == 1){//1�������˻�
				parkusersqlMap.put("sql", "update user_info_tb  set balance =balance+? where id=?");
				parkusersqlMap.put("values", new Object[]{money,uid});
				bathSql.add(parkusersqlMap);
				
				parkuserAccountsqlMap.put("sql", "insert into parkuser_account_tb(uin,amount,type,create_time,remark,target,orderid,comid," +
						"berthseg_id,berth_id,groupid) values(?,?,?,?,?,?,?,?,?,?,?)");
				parkuserAccountsqlMap.put("values", new Object[]{uid,money,0,ntime,"ͣ����_"+order.getCar_number(),
						0, orderId, parkId, berthSegId, berthId, groupid});
				bathSql.add(parkuserAccountsqlMap);
			}else if(giveTo == 2){//2����Ӫ�����˻�
				if(groupid > 0){
					groupSqlMap.put("sql", "update org_group_tb set balance=balance+? where id=?");
					groupSqlMap.put("values", new Object[]{money, groupid});
					bathSql.add(groupSqlMap);
					
					groupAccountSqlMap.put("sql", "insert into group_account_tb(groupid,comid,amount,create_time,uid,type,source,orderid," +
							"remark,berthseg_id,berth_id) values(?,?,?,?,?,?,?,?,?,?,?)");
					groupAccountSqlMap.put("values",  new Object[]{groupid, parkId, money, ntime,uid, 0, 0, orderId, 
							"ͣ����_"+order.getCar_number(), berthSegId, berthId});
					bathSql.add(groupAccountSqlMap);
				}
			}
			
			bolinkSqlMap.put("sql", "insert into bolink_ccount_tb(groupid,comid,money,ctime,orderid,giveto) values(?,?,?,?,?,?)");
			bolinkSqlMap.put("values",  new Object[]{groupid, parkId, money, ntime,orderId+"", giveTo});
			bathSql.add(bolinkSqlMap);
			
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("payMonthOrder b :"+ b+",orderid:"+orderId+",brethOrderid:"+brethOrderId);
			if(b){
				//--------------------------���ؽ��-------------------------//
				resp.setResult(1);
				resp.setErrmsg("�ֽ����ɹ�");
				return resp;
			}
			resp.setResult(0);
			resp.setErrmsg("��������ʧ��");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-4);
			resp.setErrmsg("ϵͳ����");
		}
		return resp;
	}

	/**
	 * �ֶ�����POS���������˽ӿ�����autoPayPosOrder�ӿڣ�
	 * ֻ��autoPayPosOrder�ӿڲ��ܽ����ʱ��Ż���øýӿڡ�
	 * @param req
	 * @return
	 */
	@Override
	public ManuPayPosOrderResp manuPayPosOrder(ManuPayPosOrderReq req) {
		ManuPayPosOrderResp resp = new ManuPayPosOrderResp();
		try {
			logger.error(req.toString());
			Long curTime = req.getCurTime();
			Order order = req.getOrder();
			long uid = req.getUid();//�շ�Ա���
			String imei = req.getImei();
			double money = req.getMoney();//������
			int version = req.getVersion();
			Long brethOrderId = req.getBerthOrderId();//�󶨵ĳ���������
			Long endTime = req.getEndTime();//����ʱ��
			Long workId = req.getWorkId();//�ϰ���
			Long groupId = req.getGroupId();
			if(order == null 
					|| uid <= 0 
					|| money < 0 
					|| endTime == null 
					|| curTime == null
					|| groupId <= 0){//money����Ϊ0
				resp.setResult(-1);
				resp.setErrmsg("��������");
				return resp;
			}
			//-----------------------------��ȡ������Ϣ-----------------------------//
			logger.error("order:"+order.toString());
			
			long orderId = order.getId();
			double prepay = order.getPrepaid();
			int payType = order.getPay_type();
			int state = order.getState();
			long parkId = order.getComid();
			long berthId = order.getBerthnumber();
			long berthSegId = order.getBerthsec_id();
			int cType = order.getC_type();
			if(state == 1){
				resp.setResult(-2);
				resp.setErrmsg("�����ѽ���");
				return resp;
			}else if(state == 2){
				resp.setResult(-3);
				resp.setErrmsg("����Ϊδ�ɲ�����������!");
				return resp;
			}
			if(prepay >= money
					|| cType == 5){//Ԥ��������֧������ʱӦ����autoPayPosOrder����
				resp.setResult(-4);
				resp.setErrmsg("����ʧ��!");
				return resp;
			}
			int target = 0;
			double pursueMoney = money;
			if(prepay > 0){
				target = 3;
				pursueMoney = StringUtils.formatDouble(money - prepay);
			}
			logger.error("target:"+target+",pursueMoney:"+pursueMoney);
			//-----------------------------�����߼�-----------------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���¶���״̬
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			orderSqlMap.put("sql", "update order_tb set state=?,total=?,end_time=?" +
					",pay_type=?,imei=?,out_uid=? where id=?");
			orderSqlMap.put("values", new Object[]{1, money, endTime, 1, imei, uid, orderId});
			bathSql.add(orderSqlMap);
			//�ֽ���ϸ��
			Map<String, Object> cashAccountsqlMap =new HashMap<String, Object>();
			cashAccountsqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,orderid,create_time," +
					"target,ctype,comid,berthseg_id,berth_id,groupid) values(?,?,?,?,?,?,?,?,?,?)");
			cashAccountsqlMap.put("values",  new Object[]{uid, pursueMoney, orderId, curTime, target, 0, 
					parkId, berthSegId, berthId, groupId});
			bathSql.add(cashAccountsqlMap);
			if(order.getBerthnumber() > 0){
				//���²�λ״̬
				Map<String, Object> berthSqlMap = new HashMap<String, Object>();
				berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=?,end_time=? where id =?" +
						" and order_id=?");
				berthSqlMap.put("values", new Object[]{0, null, endTime, order.getBerthnumber(), orderId});
				bathSql.add(berthSqlMap);
			}
			if(brethOrderId > 0){
				//���³���������״̬
				Map<String, Object> berthOrderSqlMap = new HashMap<String, Object>();
				berthOrderSqlMap.put("sql", "update berth_order_tb set out_uid=?,order_total=? where id=? ");
				berthOrderSqlMap.put("values", new Object[]{uid, money, brethOrderId});
				bathSql.add(berthOrderSqlMap);
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("orderid:"+orderId+",b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("�ֽ����ɹ�");
				return resp;
			}
			resp.setResult(0);
			resp.setErrmsg("�ֽ����ʧ��");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-5);
			resp.setErrmsg("ϵͳ����");
		}
		return resp;
	}

	@Override
	public PayEscapePosOrderResp payEscapePosOrder(PayEscapePosOrderReq req) {
		PayEscapePosOrderResp resp = new PayEscapePosOrderResp();
		try {
			logger.error(req.toString());
			Long curTime = req.getCurTime();
			Order order = req.getOrder();
			long uid = req.getUid();//�շ�Ա���
			String imei = req.getImei();
			double money = req.getMoney();//������
			int version = req.getVersion();
			long berthSegId = req.getBerthSegId();
			Long brethOrderId = req.getBerthOrderId();
			Long groupId = req.getGroupId();//׷���շ�Ա���ڵ���Ӫ����
			Long berthId = req.getBerthId();//׷�ɶ����Ĳ�λ,����Ϊ-1��2016-10-14��ӣ�
			long parkId = req.getParkId();//׷���շ�Ա���ڵ�ͣ����
			if(order == null 
					|| uid <= 0 
					|| money < 0 
					|| curTime == null
					|| groupId <= 0
					|| parkId <= 0){//money����Ϊ0
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//-----------------------------��ȡ������Ϣ-----------------------------//
			logger.error("order:"+order.toString());
			long orderId = order.getId();
			double prepay = order.getPrepaid();
			int state = order.getState();
			int cType = order.getC_type();
			if(state == 0){
				resp.setResult(-3);
				resp.setErrmsg("���ӵ�������������");
				return resp;
			}
			if(state == 1){
				resp.setResult(-4);
				resp.setErrmsg("�����ѽ���");
				return resp;
			}
			if(prepay >= money
					|| cType == 5){//Ԥ��������֧������ʱӦ����autoPayPosOrder����
				resp.setResult(-5);
				resp.setErrmsg("����ʧ��!");
				return resp;
			}
			int target = 4;//׷��ͣ����
			double pursueMoney = money;
			if(prepay > 0){
				pursueMoney = StringUtils.formatDouble(money - prepay);
			}
			logger.error("target:"+target+",pursueMoney:"+pursueMoney);
			//-----------------------------�����߼�-----------------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���¶���״̬
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			orderSqlMap.put("sql", "update order_tb set state=?,total=?," +
					"pay_type=?,imei=?,out_uid=? where id=?");
			orderSqlMap.put("values", new Object[]{1, money, 1, imei, uid, orderId});
			bathSql.add(orderSqlMap);
			//�ֽ���ϸ��
			Map<String, Object> cashAccountsqlMap =new HashMap<String, Object>();
			cashAccountsqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,orderid,create_time,target," +
					"ctype,comid,berthseg_id,berth_id,groupid) values(?,?,?,?,?,?,?,?,?,?)");
			cashAccountsqlMap.put("values",  new Object[]{uid, pursueMoney, orderId, curTime, target, 0, parkId,
					berthSegId, berthId, groupId});
			bathSql.add(cashAccountsqlMap);
			//����׷�ɱ�����
			Map<String, Object> escapeSqlMap = new HashMap<String, Object>();
			escapeSqlMap.put("sql", "update no_payment_tb set state=?,pursue_uid=?,pursue_time=?,act_total=?," +
					"pursue_comid=?,pursue_berthseg_id=?,pursue_berth_id=?,pursue_groupid=? where order_id=? ");
			escapeSqlMap.put("values", new Object[]{1, uid, curTime, money, parkId, berthSegId, berthId, groupId,
					orderId});
			bathSql.add(escapeSqlMap);
			
			if(brethOrderId > 0){
				//���³���������״̬
				Map<String, Object> berthOrderSqlMap = new HashMap<String, Object>();
				berthOrderSqlMap.put("sql", "update berth_order_tb set out_uid=?,order_total=? where id=? ");
				berthOrderSqlMap.put("values", new Object[]{uid, money, brethOrderId});
				bathSql.add(berthOrderSqlMap);
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("orderid:"+orderId+",b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("�ֽ�׷�ɳɹ�");
				return resp;
			}
			resp.setResult(0);
			resp.setErrmsg("�ֽ�׷��ʧ��");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("ϵͳ����");
		}
		return resp;
	}

}
