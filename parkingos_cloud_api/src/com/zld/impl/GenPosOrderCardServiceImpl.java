package com.zld.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zld.pojo.Berth;
import com.zld.pojo.Card;
import com.zld.pojo.DefaultCardReq;
import com.zld.pojo.DefaultCardResp;
import com.zld.pojo.GenPosOrderReq;
import com.zld.pojo.GenPosOrderResp;
import com.zld.service.CardService;
import com.zld.service.DataBaseService;
import com.zld.service.GenPosOrderService;
import com.zld.service.PgOnlyReadService;

@Service("genCard")
public class GenPosOrderCardServiceImpl implements GenPosOrderService {
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private CardService cardService;
	@Autowired
	private CommonMethods commonMethods;
	
	Logger logger = Logger.getLogger(GenPosOrderCardServiceImpl.class);
	@Override
	public GenPosOrderResp genPosOrder(GenPosOrderReq req) {
		GenPosOrderResp resp = new GenPosOrderResp();
		try {
			logger.error(req.toString());
			Long orderId = req.getOrderId();//������
			String carNumber = req.getCarNumber();//���ƺ�
			Berth berth = req.getBerth();//��λ
			String imei = req.getImei();
			Long uid = req.getUid();//�շ�Ա���
			Long userId = req.getUserId();//�������
			Long workId = req.getWorkId();//�ϰ���
			Long berthOrderId = req.getBerthOrderId();//��Ҫ�󶨵ĳ������������
			Long startTime = req.getStartTime();
			Integer cType = req.getcType();//�������ɷ�ʽ 2��¼�복�� 5���¿���Ա
			Integer carType = req.getCarType();//��������
			Integer version = req.getVersion();//�汾��
			Long parkId = req.getParkId();//�������
			Long groupId = req.getGroupId();//��Ӫ���ű��
			Long curTime = req.getCurTime();//��ǰʱ��
			//-------------------------Ԥ������-----------------------//
			String nfc_uuid = req.getNfc_uuid();
			Integer bindcard = req.getBindcard();
			Double prepay = req.getPrepay();//Ԥ֧�����
			//-------------------------У�����----------------------//
			if(orderId <= 0 
					|| carNumber == null
					|| "".equals(carNumber)
					|| berth == null
					|| workId <= 0
					|| startTime <= 0
					|| uid <= 0
					|| parkId <= 0
					|| carType < 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//-------------------------��Ƭ��Ϣ----------------------//
			Card card = null;
			if(nfc_uuid == null || "".equals(nfc_uuid)){//û��ˢ��,ȡĬ�ϵĿ�Ƭ
				if(parkId > 0){
					DefaultCardReq defaultCardReq = new DefaultCardReq();
					defaultCardReq.setParkId(parkId);
					defaultCardReq.setUserId(userId);
					defaultCardReq.setCarNumber(carNumber);
					DefaultCardResp defaultCardResp = cardService.getDefaultCard(defaultCardReq);
					if(defaultCardResp.getResult() == 1 
							&& defaultCardResp.getCard() != null){
						card = defaultCardResp.getCard();
					}
				}
			}else{//ˢ��
				card = commonMethods.card(nfc_uuid, groupId);
			}
			if(card == null){
				resp.setResult(-7);
				resp.setErrmsg("�ÿ�Ƭδ�����������ڵ�ǰ��Ӫ����");
				return resp;
			}
			logger.error(card.toString());
			if(card.getGroup_id() <= 0){//��Ƭû��������Ӫ���ű��
				resp.setResult(-8);
				resp.setErrmsg("��Ƭ��Ϣ����");
				return resp;
			}
			if(card.getState() == 1){//ע��״̬
				resp.setResult(-10);
				resp.setErrmsg("��Ƭ��ע���������¿���");
				return resp;
			}
			if(card.getState() == 3){//����״̬
				resp.setResult(-5);
				resp.setErrmsg("��Ƭδ����");
				return resp;
			}
			if(bindcard == 0 && card.getState() == 0){//����״̬
				resp.setResult(-6);
				resp.setErrmsg("��Ƭδ���û�");
				return resp;
			}
			
			if(card.getBalance() < prepay){//����ֱ�ӷ��أ����������
				resp.setResult(-11);
				resp.setErrmsg("���㣬��Ƭ���:"+card.getBalance()+"Ԫ");
				return resp;
			}
			
			//-------------------------Ԥ������----------------------//
			int pay_type = 9;//9��ˢ��
			//-------------------------�����߼�----------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���ɶ���
			Map<String, Object> orderSqlMap = new HashMap<String, Object>();
			//��ι�����¼��
			Map<String, Object> workDetailSqlMap = new HashMap<String, Object>();
			//��Ƭ
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//��Ƭ������ˮ
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			//���²�λ������İ�״̬
			Map<String, Object> berthOrderSqlMap = new HashMap<String, Object>();
			//���²�λ��״̬
			Map<String, Object> berthSqlMap = new HashMap<String, Object>();
			
			orderSqlMap.put("sql", "insert into order_tb (id,comid,groupid,berthsec_id,uin,state," +
					"create_time,nfc_uuid,c_type,uid,imei,car_number,berthnumber,prepaid," +
					"prepaid_pay_time,pay_type,car_type) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			orderSqlMap.put("values", new Object[]{orderId, parkId, groupId, berth.getBerthsec_id(),
					userId, 0, curTime, card.getNfc_uuid(), cType, uid, imei, carNumber, berth.getId(), prepay, 
					curTime, pay_type, carType});//�����շѵ��ֶΣ�������ϸ����Ҫȡ��ǰʱ���ڣ�������һ��������п��ܶԲ�������
			//ע�ⶩ����洢�Ĳ��ǲ���nfc_uuid���ǿ�Ƭ�����nfc_uuid��������һ��
			bathSql.add(orderSqlMap);
			
			workDetailSqlMap.put("sql", "insert into work_detail_tb (uid,orderid,bid,workid,berthsec_id) " +
					"values(?,?,?,?,?)");
			workDetailSqlMap.put("values", new Object[]{uid, orderId, berth.getId(), workId,
					berth.getBerthsec_id()});
			bathSql.add(workDetailSqlMap);
			
			if(prepay > 0){//�̼ҿ�ˢ��
				cardSqlMap.put("sql", "update com_nfc_tb set balance=balance-? where id=? ");
				cardSqlMap.put("values", new Object[]{prepay, card.getId()});
				bathSql.add(cardSqlMap);
				cardAccountSqlMap.put("sql", "insert into card_account_tb(uin,card_id,type,consume_type," +
						"amount,create_time,remark,orderid,uid,comid,berthseg_id,berth_id,groupid) " +
						"values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
				cardAccountSqlMap.put("values", new Object[]{userId, card.getId(), 1, 1, prepay, curTime,
						"Ԥ��ͣ���� " + carNumber, orderId, uid, parkId, berth.getBerthsec_id(), berth.getId(), groupId});
				bathSql.add(cardAccountSqlMap);
			}
			if(berthOrderId > 0){//�󶨳���������
				berthOrderSqlMap.put("sql", "update berth_order_tb set orderid=?,in_uid=? where id=? ");
				berthOrderSqlMap.put("values", new Object[]{orderId, uid, berthOrderId});
				bathSql.add(berthOrderSqlMap);
			}
			berthSqlMap.put("sql", "update com_park_tb set order_id=?,state=?,enter_time=? where id=? ");
			berthSqlMap.put("values", new Object[]{orderId, 1, curTime, berth.getId()});
			bathSql.add(berthSqlMap);
			boolean b = writeService.bathUpdate2(bathSql);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("�����ɹ������ڴ�ӡ����ƾ��...");
				return resp;
			}
			resp.setResult(-12);
			resp.setErrmsg("����ʧ��");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("ϵͳ����");
		}
		return resp;
	}

}
