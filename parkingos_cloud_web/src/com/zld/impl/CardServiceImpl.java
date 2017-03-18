package com.zld.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zld.pojo.BaseResp;
import com.zld.pojo.BindCardReq;
import com.zld.pojo.Car;
import com.zld.pojo.Card;
import com.zld.pojo.CardCarNumber;
import com.zld.pojo.CardChargeReq;
import com.zld.pojo.Order;
import com.zld.pojo.ReturnCardReq;
import com.zld.pojo.UnbindCardReq;
import com.zld.service.CardService;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.StringUtils;

@Service
public class CardServiceImpl implements CardService {
	@Autowired
	private DataBaseService writeService;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private CommonMethods commonMethods;
	
	Logger logger = Logger.getLogger(CardServiceImpl.class);
	@Override
	public BaseResp cardCharge(CardChargeReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			long curTime = req.getCurTime();
			long cashierId = req.getCashierId();
			long cardId = req.getCardId();
			double money = req.getMoney();
			int chargeType = req.getChargeType();
			long orderId = req.getOrderId();
			long groupId = req.getGroupId();
			String subOrderId = req.getSubOrderId();
			if(cardId <= 0 
					|| money <= 0 
					|| chargeType < 0 
					|| cashierId <= 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;//��������
			}
			//----------------------------�ֲ�ʽ��--------------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock, 60)){//Ϊ�˷�ֹ��ֵ����
				logger.error("lock:"+lock);
				resp.setResult(-8);
				resp.setErrmsg("ͬһ�ſ�Ƭһ����֮��ֻ�ܳ�ֵһ��");
				return resp;
			}
			//---------------------------��Ƭ��Ϣ------------------------------------//
			Card card = readService.getPOJO("select * from com_nfc_tb where " +
					" id=? and is_delete=? and type=? limit ? ", new Object[]{cardId, 0, 2, 1}, 
					Card.class);
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("�ÿ�Ƭδ����");
				return resp;
			}
			logger.error(card.toString());
			if(card.getGroup_id() <= 0){
				resp.setResult(-4);
				resp.setErrmsg("��Ƭ��Ϣ����");
				return resp;
			}
			if(card.getGroup_id().intValue() != groupId){
				resp.setResult(-5);
				resp.setErrmsg("��Ƭ�����ڵ�ǰ��Ӫ���ţ����ܳ�ֵ");
				return resp;
			}
			if(card.getState() == 1){//ע��״̬
				resp.setResult(-6);
				resp.setErrmsg("��Ƭ��ע���������¿���");
				return resp;
			}
			if(card.getState() == 3){//����״̬
				resp.setResult(-7);
				resp.setErrmsg("��Ƭδ����");
				return resp;
			}
			long userId = card.getUin();
			//---------------------------֧����ʽ------------------------------------//
			String remark = null;
			switch (chargeType) {
			case 0:
				remark = "�ֽ��ֵ" + money + "Ԫ";
				break;
			case 1:
				remark = "΢�Ź��ںų�ֵ" + money + "Ԫ";
				break;
			case 2:
				remark = "΢�ſͻ��˳�ֵ" + money + "Ԫ";
				break;
			case 3:
				remark = "֧�����ͻ��˳�ֵ" + money + "Ԫ";
				break;
			case 4:
				remark = "Ԥ֧���˿�" + money + "Ԫ";
				break;
			case 5:
				remark = "�����˿�" + money + "Ԫ";
				break;
			default:
				break;
			}
			//---------------------------�����߼�------------------------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���¿�Ƭ���
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//��Ƭ��ˮ
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			//�շ�Ա�ֽ���ˮ
			Map<String, Object> cashAccountSqlMap = new HashMap<String, Object>();
			//������֧�����˺ų�ֵ��ˮ
			Map<String, Object> subAccountSqlMap = new HashMap<String, Object>();
			
			cardSqlMap.put("sql", "update com_nfc_tb set balance=balance+? where id=?");
			cardSqlMap.put("values", new Object[]{money, cardId});
			bathSql.add(cardSqlMap);
			Long card_account_id = writeService.getkey("seq_card_account_tb");
			cardAccountSqlMap.put("sql", "insert into card_account_tb(id,uin,card_id,type,charge_type," +
					"amount,create_time,remark,orderid,uid,groupid) values(?,?,?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{card_account_id, userId, cardId, 0, chargeType, 
					money, curTime, remark, orderId, cashierId, groupId});
			bathSql.add(cardAccountSqlMap);
			switch (chargeType) {
			case 0://�ֽ��ֵ
				cashAccountSqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,type,create_time," +
						"target,ctype,card_account_id,groupid) values(?,?,?,?,?,?,?,?)");
				cashAccountSqlMap.put("values", new Object[]{cashierId, money, 2, curTime, 5, 0, 
						card_account_id, groupId});
				bathSql.add(cashAccountSqlMap);
				break;
			case 1://΢�Ź��ںų�ֵ
			case 2://΢�ſͻ��˳�ֵ
			case 3://֧�����ͻ��˳�ֵ
				int subType = 0;//���߳�ֵ��ʽ 0��΢�Ź��ں� 1��΢�ſͻ��� 2��֧�����ͻ���
				if(chargeType == 2){
					subType = 1;
				}else if(chargeType == 3){
					subType = 2;
				}
				subAccountSqlMap.put("sql", "insert into sub_account_tb(groupid,amount,sub_orderid," +
						"create_time,card_account_id,uin,type) values(?,?,?,?,?,?,?,?)");
				subAccountSqlMap.put("values", new Object[]{groupId, money, subOrderId, curTime, 
						card_account_id, userId, subType});
				bathSql.add(subAccountSqlMap);
				break;
			case 4://Ԥ֧���˿�
				//�߼����油��........
				break;
			case 5://�����˿�
				//�߼����油��........
				break;
			default:
				break;
			}
			boolean r = writeService.bathUpdate2(bathSql);
			logger.error("r:"+r);
			if(r){
				resp.setResult(1);
				resp.setErrmsg("��ֵ�ɹ�");
				return resp;
			}
			resp.setResult(-6);
			resp.setErrmsg("��ֵʧ��");
		} catch (Exception e) {
			resp.setResult(-1);
			resp.setErrmsg("ϵͳ����");
		}
		return resp;
	}

	@Override
	public BaseResp returnCard(ReturnCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			Long cardId = req.getCardId();
			Long unbinder = req.getUnBinder();
			Long curTime = req.getCurTime();
			Long groupId = req.getGroupId();
			if(cardId <= 0 
					|| unbinder <= 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//-------------------------�ֲ�ʽ��---------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-3);
				resp.setErrmsg("�����������");
				return resp;
			}
			//-------------------------��Ƭ��Ϣ---------------------------//
			Card card = writeService.getPOJO("select * from com_nfc_tb where " +
					" id=? and is_delete=? and type=? limit ? ", new Object[]{cardId, 0, 2, 1}, 
					Card.class);
			//�����ѯ������
			if(card == null){
				resp.setResult(-4);
				resp.setErrmsg("�ÿ�Ƭδ����");
				return resp;
			}
			logger.error(card);
			if(card.getGroup_id() <= 0){
				resp.setResult(-5);
				resp.setErrmsg("��Ƭ��Ϣ����");
				return resp;
			}
			if(card.getGroup_id().intValue() != groupId){
				resp.setResult(-6);
				resp.setErrmsg("��Ƭ�����ڵ�ǰ��Ӫ���ţ�����ע��");
				return resp;
			}
			if(card.getState() == 1){//ע��״̬
				resp.setResult(-7);
				resp.setErrmsg("��Ƭ��ע��");
				return resp;
			}
			Double balance = card.getBalance();
			int state = card.getState();
			//-------------------------�ж��Ƿ���δ������---------------------------//
			List<Order> orderList = readService.getPOJOList("select * from order_tb" +
					" where nfc_uuid=? and state<>? ", 
					new Object[]{card.getNfc_uuid(), 1}, Order.class);//nfc_uuid������
			if(orderList != null && !orderList.isEmpty()){
				String ids = null;
				for(Order order : orderList){
					if(ids == null){
						ids += order.getId();
					}else{
						ids += "," + order.getId();
					}
				}
				logger.error("orderids:"+ids);
				resp.setResult(-8);
				resp.setErrmsg("��Ƭ����δ�����������ȴ���������ţ�"+ids);
				return resp;
			}
			//-------------------------�����߼�---------------------------//
			String remark = "ע����Ƭ���˻����"+balance+"Ԫ";
			if(state == 3){//��������ʱ�Ŀ�Ƭ�������ã�Ҫ�����ſ�ʹ�ã�
				remark = "ע��δ���Ƭ";
				balance = 0d;//��Ƭ��δ������û���˿������ʵ�ʲ������Ϊ0
			}
			logger.error("balance:"+balance);
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���¿�Ƭ���
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//д��Ƭ��ˮ
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			//д�շ�Ա�ֽ���ˮ
			Map<String, Object> cashSqlMap = new HashMap<String, Object>();
			//��Ƭ�ͳ��ƹ�����
			Map<String, Object> cardCarSqlMap = new HashMap<String, Object>();
			
			cardSqlMap.put("sql", "update com_nfc_tb set state=?,cancel_id=?," +
					"cancel_time=?,balance=?,uin=? where id=?");
			cardSqlMap.put("values", new Object[]{1, unbinder, curTime, 0d, -1, cardId});
			bathSql.add(cardSqlMap);
			long accountId = writeService.getkey("seq_card_account_tb");
			cardAccountSqlMap.put("sql", "insert into card_account_tb(id,card_id,type," +
					"amount,create_time,remark,uid,uin,groupid) values(?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{accountId, cardId, 5, balance, curTime,
					remark, unbinder, card.getUin(), groupId});
			bathSql.add(cardAccountSqlMap);
			if(state != 3){//3����������ʱ�Ŀ�Ƭ�������ã�Ҫ�����ſ�ʹ�ã�����ʱ�Ŀ�Ƭ��δ����������д�˿���ˮ
				cashSqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,type,create_time," +
						"target,ctype,card_account_id,groupid) values(?,?,?,?,?,?,?,?)");
				cashSqlMap.put("values", new Object[]{unbinder, balance, 2, curTime,
						6, 1, accountId, groupId});
				bathSql.add(cashSqlMap);
			}
			if(state == 4){
				cardCarSqlMap.put("sql", "update card_carnumber_tb set is_delete=? where card_id=? " +
						" and is_delete=? ");
				cardCarSqlMap.put("values", new Object[]{1, cardId, 0});
				bathSql.add(cardCarSqlMap);
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("ע���ɹ�");
				return resp;
			}
			resp.setResult(-9);
			resp.setErrmsg("ע��ʧ��");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("ϵͳ����");
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("ɾ����lock:"+lock+"b:"+b);
		}
		return resp;
	}

	@Override
	public BaseResp bindUserCard(BindCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			Long cardId = req.getCardId();
			Long binder = req.getBinder();
			String mobile = req.getMobile();
			String carNumber = req.getCarNumber();
			Long curTime = req.getCurTime();
			long groupId = req.getGroupId();
			//û�в�λ����
			if(cardId <= 0
					|| binder <= 0
					//�ֻ��źͳ��ƺŲ��ܶ�Ϊ��
					|| (carNumber == null || "".equals(carNumber))
					|| (mobile == null || "".equals(mobile))
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//----------------------------У���ֻ��źͳ��ƺ�------------------------//
			boolean m = StringUtils.checkMobile(mobile);
			if(!m){
				resp.setResult(-13);
				resp.setErrmsg("��������ȷ���ֻ���");
				return resp;
			}
			List<String> plateList = new ArrayList<String>();
			if(carNumber != null){
				//�೵����Ӣ�Ķ��Ÿ���
				String[] cars = carNumber.split(",");
				for(int i = 0; i< cars.length; i++){
					String plate = cars[i];
					plateList.add(plate);
					if(!StringUtils.checkPlate(plate)){
						resp.setResult(-14);
						resp.setErrmsg("��������ȷ�ĳ��ƺţ����������Ӣ�Ķ��Ÿ���");
						return resp;
					}
				}
			}
			//----------------------------�ֲ�ʽ��--------------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-12);
				resp.setErrmsg("�����������");
				return resp;
			}
			//--------------------У�鿨Ƭ---------------------//
			Card card = writeService.getPOJO("select * from com_nfc_tb where " +
					" id=? and is_delete=? and type=? ", 
					new Object[]{cardId, 0, 2}, Card.class);
			//�����ѯ������
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("��Ƭ������");
				return resp;
			}
			logger.error(card.toString());
			int state = card.getState();
			switch (state) {
				case 0://0���Ѽ���δ��
				case 2://2���Ѱ��û�
				case 4://4���Ѱ󶨳��ƺ�
					break;
				case 1://ע��״̬
					resp.setResult(-6);
					resp.setErrmsg("�ÿ�Ƭ�ѱ�ע���������¿���");
					return resp;
				case 3://3������
					resp.setResult(-8);
					resp.setErrmsg("��Ƭû�м���");
					return resp;
				default:
					resp.setResult(-9);
					resp.setErrmsg("��Ƭ��Ϣ����");
					return resp;
			}
			//--------------------�����߼�---------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//�û�
			Map<String, Object> userSqlMap = new HashMap<String, Object>();
			//��Ƭ�ͳ��ƹ�����
			Map<String, Object> cardCarSqlMap = new HashMap<String, Object>();
			//�󶨿�Ƭ
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//��Ƭ��ˮ���û�
			Map<String, Object> bindAccountSqlMap = new HashMap<String, Object>();
			//��Ƭ��ˮ
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			
			Long userId = -1L;
			Map<String, Object> userMap = readService.getMap("select id from user_info_tb" +
					" where mobile=? and auth_flag=? ", new Object[]{mobile, 4});
			if(userMap == null){
				userId = writeService.getkey("seq_user_info_tb");
				String strid = userId+"zld";
				userSqlMap.put("sql", "insert into user_info_tb (id,nickname,password,strid," +
						"reg_time,mobile,auth_flag,comid) values (?,?,?,?,?,?,?,?)");
				userSqlMap.put("values", new Object[]{userId, "����", strid, strid, curTime, mobile, 4, 0});
				bathSql.add(userSqlMap);
			}else{
				userId = (Long)userMap.get("id");
			}
			logger.error("userId:"+userId+",mobile:"+mobile);
			if(plateList != null && !plateList.isEmpty()){
				//-------------------------------��鳵���Ƿ񱻱��˰󶨣���û�а󶨾Ͳ���һ��--------------------------//
				for(String car : plateList){
					//ɾ�����ƺ�(��������ģ���֪ͨ�û�ǿ��ɾ���û��ͳ��Ƶİ󶨹�ϵ)
					Map<String, Object> delCarSqlMap = new HashMap<String, Object>();
					delCarSqlMap.put("sql", "update car_info_tb set state=? where car_number=? and " +
						" state=? and uin>? and uin<>? ");
					delCarSqlMap.put("values", new Object[]{0, car, 1, 0, userId});
					bathSql.add(delCarSqlMap);
					
					Long count = readService.getLong("select count(id) from car_info_tb where car_number=? and " +
							" state=? and uin=? ", new Object[]{car, 1, userId});
					if(count == 0){
						//��ӳ��ƺ�
						Map<String, Object> carSqlMap = new HashMap<String, Object>();
						carSqlMap.put("sql", "insert into car_info_tb (uin,car_number,create_time) values (?,?,?)");
						carSqlMap.put("values", new Object[]{userId, car, curTime});
						bathSql.add(carSqlMap);
					}
				}
				//----------------------------------ɾ����ǰ�ĳ���-----------------------------------//
				List<Car> carList = readService.getPOJOList("select id,car_number from car_info_tb where uin=? and " +
						" state=? ", new Object[]{userId, 1}, Car.class);
				if(carList != null && !carList.isEmpty()){
					for(Car car : carList){
						String plate = car.getCar_number();
						Long id = car.getId();
						if(!plateList.contains(plate)){//�����ĳ���ɾ����
							//ɾ�����ƺ�
							Map<String, Object> carSqlMap = new HashMap<String, Object>();
							carSqlMap.put("sql", "update car_info_tb set state=? where id=? ");
							carSqlMap.put("values", new Object[]{0, id});
							bathSql.add(carSqlMap);
						}
					}
				}
			}
			cardSqlMap.put("sql", "update com_nfc_tb set state=?,uin=?,uid=?,update_time=? where id=?");
			cardSqlMap.put("values", new Object[]{2, userId, binder, curTime, card.getId()});
			bathSql.add(cardSqlMap);
			
			cardAccountSqlMap.put("sql", "insert into card_account_tb(card_id,type,create_time,remark,uid," +
					"uin,comid,berthseg_id,groupid) values(?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{card.getId(), 4, curTime, "���û����ֻ���:" + mobile,
					binder, userId, -1L, -1L, groupId});
			bathSql.add(cardAccountSqlMap);
			
			bindAccountSqlMap.put("sql", "update card_account_tb set uin=? where card_id=? and uin<=? ");//state=0����
			bindAccountSqlMap.put("values", new Object[]{userId, card.getId(), 0});
			bathSql.add(bindAccountSqlMap);
			
			if(state == 4){//�󶨵ĳ��ƺ�
				//�����������ԭ���ĳ��ư����ݻ�ɾ��,���Զ����복�Ʊ�
				cardCarSqlMap.put("sql", "update card_carnumber_tb set is_delete=? where card_id=? " +
						" and is_delete=? ");
				cardCarSqlMap.put("values", new Object[]{1, cardId, 0});
				bathSql.add(cardCarSqlMap);
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("�󶨳ɹ�");
				return resp;
			}
			resp.setResult(-11);
			resp.setErrmsg("��ʧ��");
		} catch (Exception e) {
			logger.error(e);
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("ɾ����lock:"+lock+"b:"+b);
		}
		resp.setResult(-1);
		resp.setErrmsg("ϵͳ����");
		return resp;
	}

	@Override
	public BaseResp unBindCard(UnbindCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			Long cardId = req.getCardId();
			Long unbinder = req.getUnBinder();
			Long curTime = req.getCurTime();
			Long groupId = req.getGroupId();
			if(cardId <= 0 
					|| unbinder <= 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//-------------------------�ֲ�ʽ��---------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-3);
				resp.setErrmsg("�����������");
				return resp;
			}
			//-------------------------��Ƭ��Ϣ---------------------------//
			Card card = writeService.getPOJO("select * from com_nfc_tb where " +
					" id=? and is_delete=? and type=? limit ? ", new Object[]{cardId, 0, 2, 1}, 
					Card.class);
			//�����ѯ������
			if(card == null){
				resp.setResult(-4);
				resp.setErrmsg("�ÿ�Ƭδ����");
				return resp;
			}
			logger.error(card);
			if(card.getGroup_id() <= 0){
				resp.setResult(-5);
				resp.setErrmsg("��Ƭ��Ϣ����");
				return resp;
			}
			if(card.getGroup_id().intValue() != groupId){
				resp.setResult(-6);
				resp.setErrmsg("��Ƭ�����ڵ�ǰ��Ӫ���ţ����ܽ��");
				return resp;
			}
			int state = card.getState();
			long userId = card.getUin();
			switch (state) {
			case 0://0:����
			case 3://0:����
				resp.setResult(-10);
				resp.setErrmsg("��Ƭδ���û����߳���");
				return resp;
			case 1://ע��״̬
				resp.setResult(-7);
				resp.setErrmsg("��Ƭ��ע��");
				return resp;
			default:
				break;
			}
			//-------------------------��ѯ����Ϣ------------------------//
			String remark = null;
			if(state == 2 && userId > 0){
				logger.error("��Ƭ�󶨵��û�������󶨣�userId:" + userId);
				Map<String, Object> userMap = readService.getMap("select mobile from user_info_tb " +
						" where id=? ", new Object[]{userId});
				if(userMap != null){
					remark = "����û����ֻ��ţ�" + userMap.get("mobile");
				}
			}else if(state == 4){
				List<CardCarNumber> ccList = readService.getPOJOList("select car_number from card_carnumber_tb" +
						" where card_id=? and is_delete=? ", new Object[]{cardId, 0}, CardCarNumber.class);
				String carNumber = "";
				if(ccList != null && !ccList.isEmpty()){
					for(CardCarNumber c : ccList){
						if("".equals(carNumber)){
							carNumber = c.getCar_number();
						}else{
							carNumber += ("��" + c.getCar_number());
						}
					}
				}
				remark = "����ƺţ����ƺţ�" + carNumber;
			}
			logger.error("remark:" + remark);
			//-------------------------�����߼�---------------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���¿�Ƭ���
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//д��Ƭ��ˮ
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			//��Ƭ�ͳ��ƹ�����
			Map<String, Object> cardCarSqlMap = new HashMap<String, Object>();
			//����֮ǰ��ˮ
			Map<String, Object> bindAccountSqlMap = new HashMap<String, Object>();
			
			cardSqlMap.put("sql", "update com_nfc_tb set state=?,uin=? where id=?");
			cardSqlMap.put("values", new Object[]{0, -1, cardId});
			bathSql.add(cardSqlMap);
			long accountId = writeService.getkey("seq_card_account_tb");
			cardAccountSqlMap.put("sql", "insert into card_account_tb(id,card_id,type,create_time," +
					"remark,uid,uin,groupid) values(?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{accountId, cardId, 6, curTime, remark, 
					unbinder, -1, groupId});
			bathSql.add(cardAccountSqlMap);
			if(state == 2){//�����û�
				bindAccountSqlMap.put("sql", "update card_account_tb set uin=? where card_id=? and uin>? ");//state=0����
				bindAccountSqlMap.put("values", new Object[]{-1, card.getId(), 0});
				bathSql.add(bindAccountSqlMap);
			}else if(state == 4){//�󶨳��ƺ�
				cardCarSqlMap.put("sql", "update card_carnumber_tb set is_delete=? where card_id=? " +
						" and is_delete=? ");
				cardCarSqlMap.put("values", new Object[]{1, cardId, 0});
				bathSql.add(cardCarSqlMap);
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("���ɹ�");
				return resp;
			}
			resp.setResult(-9);
			resp.setErrmsg("���ʧ��");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("ϵͳ����");
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("ɾ����lock:"+lock+"b:"+b);
		}
		return resp;
	}

	@Override
	public BaseResp bindPlateCard(BindCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			Long cardId = req.getCardId();
			Long binder = req.getBinder();
			String mobile = req.getMobile();
			String carNumber = req.getCarNumber();
			Long curTime = req.getCurTime();
			long groupId = req.getGroupId();
			//û�в�λ����
			if(cardId <= 0
					|| binder <= 0
					//�ֻ��źͳ��ƺŲ��ܶ�Ϊ��
					|| (carNumber == null || "".equals(carNumber))
					|| (mobile != null && !"".equals(mobile))
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//----------------------------У�鳵�ƺ�------------------------//
			List<String> plateList = new ArrayList<String>();
			//�೵����Ӣ�Ķ��Ÿ���
			String[] cars = carNumber.split(",");
			for(int i = 0; i< cars.length; i++){
				String plate = cars[i];
				plateList.add(plate);
				if(!StringUtils.checkPlate(plate)){
					resp.setResult(-14);
					resp.setErrmsg("��������ȷ�ĳ��ƺţ����������Ӣ�Ķ��Ÿ���");
					return resp;
				}
			}
			//----------------------------�ֲ�ʽ��--------------------------------//
			lock = commonMethods.getLock(cardId);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-12);
				resp.setErrmsg("�����������");
				return resp;
			}
			//--------------------У�鿨Ƭ---------------------//
			Card card = writeService.getPOJO("select * from com_nfc_tb where " +
					" id=? and is_delete=? and type=? ", 
					new Object[]{cardId, 0, 2}, Card.class);
			//�����ѯ������
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("��Ƭ������");
				return resp;
			}
			logger.error(card.toString());
			int state = card.getState();
			switch (state) {
				case 0://0���Ѽ���δ��
				case 2://2���Ѱ��û�
				case 4://4���Ѱ󶨳��ƺ�
					break;
				case 1://ע��״̬
					resp.setResult(-6);
					resp.setErrmsg("�ÿ�Ƭ�ѱ�ע���������¿���");
					return resp;
				case 3://3������
					resp.setResult(-8);
					resp.setErrmsg("��Ƭû�м���");
					return resp;
				default:
					resp.setResult(-9);
					resp.setErrmsg("��Ƭ��Ϣ����");
					return resp;
			}
			//--------------------�����߼�---------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//�󶨿�Ƭ
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//��Ƭ��ˮ���û�
			Map<String, Object> bindAccountSqlMap = new HashMap<String, Object>();
			//��Ƭ��ˮ
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			
			cardSqlMap.put("sql", "update com_nfc_tb set state=?,uin=?,uid=?,update_time=? where id=?");
			cardSqlMap.put("values", new Object[]{4, -1L, binder, curTime, card.getId()});
			bathSql.add(cardSqlMap);
			
			cardAccountSqlMap.put("sql", "insert into card_account_tb(card_id,type,create_time,remark,uid," +
					"uin,comid,berthseg_id,groupid) values(?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{card.getId(), 4, curTime, "�󶨳��ƺţ����ƺ�:" + carNumber,
					binder, -1L, -1L, -1L, groupId});
			bathSql.add(cardAccountSqlMap);
			
			bindAccountSqlMap.put("sql", "update card_account_tb set uin=? where card_id=? and uin>? ");//state=0����
			bindAccountSqlMap.put("values", new Object[]{-1, card.getId(), 0});
			bathSql.add(bindAccountSqlMap);
			
			for(String plate : plateList){
				Long count = readService.getLong("select count(id) from card_carnumber_tb where car_number=?" +
						" and card_id=? and is_delete=? ", new Object[]{plate, cardId, 0});
				logger.error("plate:"+plate+"count:"+count);
				if(count == 0){
					//��Ƭ�ͳ��ƹ�����
					Map<String, Object> cardCarSqlMap = new HashMap<String, Object>();
					cardCarSqlMap.put("sql", "insert into card_carnumber_tb(car_number,card_id,create_time) values(?,?,?)");
					cardCarSqlMap.put("values", new Object[]{plate, cardId, curTime});
					bathSql.add(cardCarSqlMap);
				}
			}
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("�󶨳ɹ�");
				return resp;
			}
			resp.setResult(-11);
			resp.setErrmsg("��ʧ��");
		} catch (Exception e) {
			logger.error(e);
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("ɾ����lock:"+lock+"b:"+b);
		}
		resp.setResult(-1);
		resp.setErrmsg("ϵͳ����");
		return resp;
	}
}
