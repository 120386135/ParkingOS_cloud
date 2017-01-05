package com.zld.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zld.pojo.ActCardReq;
import com.zld.pojo.BaseResp;
import com.zld.pojo.BindCardReq;
import com.zld.pojo.Car;
import com.zld.pojo.Card;
import com.zld.pojo.CardCarNumber;
import com.zld.pojo.CardChargeReq;
import com.zld.pojo.CardInfoReq;
import com.zld.pojo.CardInfoResp;
import com.zld.pojo.DefaultCardReq;
import com.zld.pojo.DefaultCardResp;
import com.zld.pojo.Group;
import com.zld.pojo.Order;
import com.zld.pojo.RegCardReq;
import com.zld.pojo.UnbindCardReq;
import com.zld.pojo.WorkRecord;
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
			double money = req.getMoney();
			int chargeType = req.getChargeType();
			long orderId = req.getOrderId();
			long groupId = req.getGroupId();
			String subOrderId = req.getSubOrderId();
			String nfc_uuid = req.getNfc_uuid();
			long parkId = req.getParkId();//�շ�Ա�е�ǰ��¼�ĳ������ԣ�������Աû�г�������
			//û�в�λ����
			if(nfc_uuid == null
					|| "".equals(nfc_uuid)
					|| money <= 0 
					|| chargeType < 0 
					|| cashierId <= 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;//��������
			}
			//----------------------------�ֲ�ʽ��--------------------------------//
			lock = commonMethods.getNFCLock(nfc_uuid);
			if(!memcacheUtils.addLock(lock, 60)){//Ϊ�˷�ֹ��ֵ����
				logger.error("lock:"+lock);
				resp.setResult(-8);
				resp.setErrmsg("ͬһ�ſ�Ƭһ����֮��ֻ�ܳ�ֵһ��");
				return resp;
			}
			//-------------------------��Ƭ��Ϣ---------------------------//
			Card card = commonMethods.card(nfc_uuid, groupId);
			//nfc_uuidģ����ѯ�޷�ʹ��index scan��ֻ����seq scanɨ������߲�ѯʱ���򱶣��ڴˣ�����group_id��������������
			//explain analyze select * from com_nfc_tb where .........
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("�ÿ�Ƭδ�����������ڵ�ǰ��Ӫ����");
				return resp;
			}
			logger.error(card.toString());
			if(card.getState() == 3){//����״̬
				resp.setResult(-7);
				resp.setErrmsg("��Ƭδ����");
				return resp;
			}
			long cardId = card.getId();
			long userId = card.getUin();
			//------------------------�ϰ��¼---------------------//
			WorkRecord workRecord = commonMethods.getWorkRecord(cashierId);
			long berthSegId = -1;
			if(workRecord != null){
				berthSegId = workRecord.getBerthsec_id();
			}
			logger.error("berthSegId:"+berthSegId);
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
			cardAccountSqlMap.put("sql", "insert into card_account_tb(id,uin,card_id,type,charge_type,amount," +
					"create_time,remark,orderid,uid,comid,berthseg_id,groupid) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{card_account_id, userId, cardId, 0, chargeType, 
					money, curTime, remark, orderId, cashierId, parkId, berthSegId, groupId});
			bathSql.add(cardAccountSqlMap);
			switch (chargeType) {
			case 0://�ֽ��ֵ
				cashAccountSqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,type,create_time," +
						"target,ctype,card_account_id,comid,berthseg_id,groupid) values(?,?,?,?,?,?,?,?,?,?)");
				cashAccountSqlMap.put("values", new Object[]{cashierId, money, 2, curTime, 5, 0, card_account_id
						, parkId, berthSegId, groupId});
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
	public BaseResp regCard(RegCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			String nfc_uuid = req.getNfc_uuid();
			String cardNo = req.getCardNo();
			Long regId = req.getRegId();
			Long curTime = req.getCurTime();
			Double money = req.getMoney();
			Long groupId = req.getGroupId();
			String cardName = req.getCardName();
			String device = req.getDevice();//�����豸
			//û�г������ԡ���λ�Ρ���λ����
			if(nfc_uuid == null
					|| "".equals(nfc_uuid) 
					|| nfc_uuid.length() < 8
					|| nfc_uuid.length() > 14
					|| regId <= 0
					|| groupId <= 0
					|| cardNo == null
					|| cardNo.length() != 6){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//----------------------------�ֲ�ʽ��--------------------------------//
			lock = commonMethods.getNFCLock(nfc_uuid);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-3);
				resp.setErrmsg("�����������");
				return resp;
			}
			//----------------------------ƴ�Ӳ���--------------------------------//
			String cardNum = nfc_uuid;
			while (cardNum.length() < 14) {
				cardNum += "0";
			}
			cardNum += cardNo;
			logger.error(cardNum);
			//----------------------------У�鿨Ƭ��Ϣ--------------------------------//
			Long count = writeService.getLong("select count(id) from com_nfc_tb " +
					" where nfc_uuid like ? and is_delete=? and type=? and state <>?" +
					" and group_id=? ", new Object[]{"%" + nfc_uuid + "%", 0, 2, 1, groupId});
			//����nfc_uuid��ƴ�Ӻ�Ĳ���,����������ģ����ѯ,state=1��ʾע��״̬
			//���������������ѯ
			logger.error("count:"+count);
			if(count > 0){
				resp.setResult(-3);
				resp.setErrmsg("�ÿ�Ƭ�ѿ���");
				return resp;
			}
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//���¿�Ƭ���
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			//д��Ƭ��ˮ
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			
			long cardId = writeService.getkey("seq_com_nfc_tb");
			logger.error("cardId:"+cardId);
			cardSqlMap.put("sql", "insert into com_nfc_tb(id,nfc_uuid," +
					"create_time,state,card_name,device,balance,card_number," +
					"group_id,reg_id,type) values(?,?,?,?,?,?,?,?,?,?,?)");
			cardSqlMap.put("values", new Object[]{cardId, cardNum, curTime, 3, 
					cardName, device, money, cardNo, groupId, regId, 2});
			bathSql.add(cardSqlMap);
			long accountId = writeService.getkey("seq_card_account_tb");
			logger.error("accountId:"+accountId);
			cardAccountSqlMap.put("sql", "insert into card_account_tb(id,card_id,type," +
					"amount,create_time,remark,uid,groupid) values(?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{accountId, cardId, 2, money, curTime,
					"��������ʼ���"+money+"Ԫ", regId, groupId});
			bathSql.add(cardAccountSqlMap);
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("cardId:"+cardId+",accountId:"+accountId+",b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("�����ɹ�");
				return resp;
			}
			resp.setResult(-5);
			resp.setErrmsg("����ʧ��");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("ϵͳ����");
		} finally {//ɾ����
			boolean b = memcacheUtils.delLock(lock);
			logger.error("ɾ����lock:"+lock+",b:"+b);
		}
		return resp;
	}

	@Override
	public BaseResp bindUserCard(BindCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			String nfc_uuid = req.getNfc_uuid();
			Long binder = req.getBinder();
			String mobile = req.getMobile();
			String carNumber = req.getCarNumber();
			Long curTime = req.getCurTime();
			long groupId = req.getGroupId();
			long parkId = req.getParkId();//������Ա���ڵ�ͣ������������Աû�г�������
			//û�в�λ����
			if(binder <= 0
					//�ֻ��źͳ��ƺŲ��ܶ�Ϊ��
					|| (carNumber == null || "".equals(carNumber))
					|| (mobile == null || "".equals(mobile))
					|| groupId <= 0
					|| nfc_uuid == null
					|| "".equals(nfc_uuid)){
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
			lock = commonMethods.getNFCLock(nfc_uuid);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-12);
				resp.setErrmsg("�����������");
				return resp;
			}
			//--------------------У�鿨Ƭ---------------------//
			Card card = commonMethods.card(nfc_uuid, groupId);
			//�����ѯ������
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("�ÿ�Ƭδ�����������ڵ�ǰ��Ӫ����");
				return resp;
			}
			logger.error(card.toString());
			long cardId = card.getId();
			int state = card.getState();
			long uin = card.getUin();
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
			//------------------------�ϰ��¼---------------------//
			WorkRecord workRecord = commonMethods.getWorkRecord(binder);
			long berthSegId = -1;
			if(workRecord != null){
				berthSegId = workRecord.getBerthsec_id();
			}
			logger.error("berthSegId:"+berthSegId);
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
						carSqlMap.put("values", new Object[]{userId, carNumber, curTime});
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
					binder, userId, parkId, berthSegId, groupId});
			bathSql.add(cardAccountSqlMap);
			
			bindAccountSqlMap.put("sql", "update card_account_tb set uin=? where card_id=? and uin<=? ");//state=0����
			bindAccountSqlMap.put("values", new Object[]{userId, card.getId(), 0});
			bathSql.add(bindAccountSqlMap);
			
			if(state == 4){//�󶨵ĳ��ƺ�
				//�����������ԭ���ĳ��ư����ݻ�ɾ��,���Զ����복�Ʊ�
				cardCarSqlMap.put("sql", "update card_carnumber_tb set is_delete=? where and card_id=? " +
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
			logger.error("ɾ����lock:"+lock+",b:"+b);
		}
		resp.setResult(-1);
		resp.setErrmsg("ϵͳ����");
		return resp;
	}

	@Override
	public DefaultCardResp getDefaultCard(DefaultCardReq req) {
		DefaultCardResp resp = new DefaultCardResp();
		try {
			logger.error(req.toString());
			Long parkId = req.getParkId();
			Long userId = req.getUserId();
			String carNumber = req.getCarNumber();
			if(parkId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			Map<String, Object> parkMap = readService.getMap("select " +
					" groupid from com_info_tb where id=? and state<>? ", 
					new Object[]{parkId, 1});
			if(parkMap == null){
				resp.setResult(-3);
				resp.setErrmsg("����������");
				return resp;
			}
			Long groupid = (Long)parkMap.get("groupid");
			logger.error("groupid:"+groupid);
			if(groupid > 0){
				Card card = null;
				if(userId > 0){
					card = writeService.getPOJO("select * from com_nfc_tb where group_id=? and state=? " +
							" and uin=? and is_delete=? and type=? order by balance desc limit ?",
							new Object[]{groupid, 2, userId, 0, 2, 1}, Card.class);
					//�����������ѯ��׷�ɵ�ʱ�����һ����׷�ɶ���ӵ����ñ�����ܲ������
				}
				if(card == null && carNumber != null){
					List<CardCarNumber> list = readService.getPOJOList("select card_id from card_carnumber_tb" +
							" where car_number=? and is_delete=? ", 
							new Object[]{carNumber, 0}, CardCarNumber.class);
					if(list != null && !list.isEmpty()){
						String preParam = "";
						List<Object> paramList = new ArrayList<Object>();
						for(CardCarNumber car : list){
							long cardId = car.getCard_id();
							paramList.add(cardId);
							if("".equals(preParam)){
								preParam = "?";
							}else{
								preParam = ",?";
							}
						}
						paramList.add(groupid);
						paramList.add(4);
						paramList.add(0);
						paramList.add(2);
						paramList.add(1);
						card = writeService.getPOJO("select * from com_nfc_tb where id in ("+preParam+") and group_id=? " +
								" and state=? and is_delete=? and type=? order by balance desc limit ?",
								paramList, Card.class);
						//�����������ѯ��׷�ɵ�ʱ�����һ����׷�ɶ���ӵ����ñ�����ܲ������
					}
				}
				if(card != null){
					logger.error(card.toString());
					resp.setCard(card);
				}
			}
			resp.setResult(1);
			resp.setErrmsg("��ѯ�ɹ�");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("ϵͳ����");
		}
		return resp;
	}

	@Override
	public CardInfoResp getCardInfo(CardInfoReq req) {
		CardInfoResp resp = new CardInfoResp();
		try {
			logger.error(req.toString());
			String nfc_uuid = req.getNfc_uuid();
			Long groupId = req.getGroupId();
			//------------------------У�����----------------------//
			if("".equals(nfc_uuid)
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//------------------------�����߼�----------------------//
			Group group = readService.getPOJO("select name from org_group_tb where" +
					" id=? and state=?", new Object[]{groupId, 0}, Group.class);
			if(group == null){
				resp.setResult(-6);
				resp.setErrmsg("��Ӫ���Ų�����");
				return resp;
			}
			Card card = commonMethods.card(nfc_uuid, groupId);
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("�ÿ�Ƭδ�����������ڵ�ǰ��Ӫ����");
				return resp;
			}
			logger.error(card.toString());
			int state = card.getState();
			if(state == 2){
				logger.error("���û���cardId:"+card.getId());
				Map<String, Object> userMap = readService.getMap("select mobile " +
						" from user_info_tb where id=? ", new Object[]{card.getUin()});
				if(userMap != null && userMap.get("mobile") != null){
					resp.setMobile((String)userMap.get("mobile"));
				}
				List<Car> cars = readService.getPOJOList("select car_number from" +
						" car_info_tb where uin=? and state=? ", 
						new Object[]{card.getUin(), 1}, Car.class);
				if(cars != null && !cars.isEmpty()){
					String carNumber = null;
					for(Car car : cars){
						if(carNumber == null){
							carNumber = car.getCar_number();
						}else{
							carNumber += "," + car.getCar_number();
						}
					}
					resp.setCarnumber(carNumber);
				}
			}else if(state == 4){
				logger.error("�󶨳��ƣ�cardId:"+card.getId());
				List<CardCarNumber> cardCarNumbers = readService.getPOJOList("select car_number " +
						" from card_carnumber_tb where card_id=? and is_delete=? ",
						new Object[]{card.getId(), 0}, CardCarNumber.class);
				if(cardCarNumbers != null && !cardCarNumbers.isEmpty()){
					String carNumber = null;
					for(CardCarNumber car : cardCarNumbers){
						if(carNumber == null){
							carNumber = car.getCar_number();
						}else{
							carNumber += "," + car.getCar_number();
						}
					}
					resp.setCarnumber(carNumber);
				}
			}
			resp.setCard(card);
			resp.setResult(1);
			resp.setGroup_name(group.getName());
			resp.setErrmsg("��ѯ�ɹ�");
		} catch (Exception e) {
			logger.error(e);
			resp.setResult(-1);
			resp.setErrmsg("ϵͳ����");
		}
		return resp;
	}

	@Override
	public BaseResp actCard(ActCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			String nfc_uuid = req.getNfc_uuid();
			Long uid = req.getUid();
			Long curTime = req.getCurTime();
			Long groupId = req.getGroupId();
			long parkId = req.getParkId();//������Ա���ڵ�ͣ������������Աû�г�������
			//û�в�λ����
			if(nfc_uuid == null
					|| "".equals(nfc_uuid)
					|| uid <= 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//---------------------�ֲ�ʽ��--------------------//
			lock = commonMethods.getNFCLock(nfc_uuid);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-3);
				resp.setErrmsg("�����������");
				return resp;
			}
			//--------------------��ƬУ��------------------------//
			Card card = commonMethods.card(nfc_uuid, groupId);
			//ע�����Ҫ����state<>1�����ƣ��ų���ע���ģ�������������
			//�����ѯ������
			if(card == null){
				resp.setResult(-4);
				resp.setErrmsg("�ÿ�Ƭδ�����������ڵ�ǰ��Ӫ����");
				return resp;
			}
			logger.error(card.toString());
			int state = card.getState();//0�������Ӧ֮ǰ�������� 1��ע������Ӧ֮ǰ�Ľ��ã�  2�����û� 3����������ʱ�Ŀ�Ƭ�������ã�Ҫ�����ſ�ʹ�ã�
			switch (state) {
			case 0://0������
			case 2://2�����û�
				resp.setResult(-8);
				resp.setErrmsg("�ÿ�Ƭ�Ѽ���");
				return resp;
			case 3://3������
				break;
			default:
				resp.setResult(-9);
				resp.setErrmsg("��Ƭ��Ϣ����");
				return resp;
			}
			//--------------------��Ƭ��Ϣ------------------------//
			long cardId = card.getId();
			double balance = card.getBalance();
			//------------------------�ϰ��¼---------------------//
			WorkRecord workRecord = commonMethods.getWorkRecord(uid);
			long berthSegId = -1;
			if(workRecord != null){
				berthSegId = workRecord.getBerthsec_id();
			}
			logger.error("berthSegId:"+berthSegId);
			//--------------------�����߼�------------------------//
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			//��Ƭ״̬
			Map<String, Object> cardSqlMap = new HashMap<String, Object>();
			cardSqlMap.put("sql", "update com_nfc_tb set " +
					" state=?,activate_id=?,activate_time=? where id=?");
			cardSqlMap.put("values", new Object[]{0, uid, curTime, cardId});
			bathSql.add(cardSqlMap);
			//��Ƭ��ˮ
			long accountId = writeService.getkey("seq_card_account_tb");
			Map<String, Object> cardAccountSqlMap = new HashMap<String, Object>();
			cardAccountSqlMap.put("sql", "insert into card_account_tb(id,card_id,type,create_time," +
					"remark,uid,comid,berthseg_id,groupid,amount) values(?,?,?,?,?,?,?,?,?,?)");
			cardAccountSqlMap.put("values", new Object[]{accountId, cardId, 3, curTime,
					"���Ƭ����ʼ���"+balance+"Ԫ", uid, parkId, berthSegId, groupId, balance});
			bathSql.add(cardAccountSqlMap);
			//���ֽ���ϸ
			Map<String, Object> cashSqlMap = new HashMap<String, Object>();
			cashSqlMap.put("sql", "insert into parkuser_cash_tb(uin,amount,type,create_time,ctype,target," +
					"card_account_id,comid,berthseg_id,groupid) values(?,?,?,?,?,?,?,?,?,?)");
			cashSqlMap.put("values", new Object[]{uid, balance, 2, curTime, 0, 5, accountId, 
					parkId, berthSegId, groupId});
			bathSql.add(cashSqlMap);
			
			boolean b = writeService.bathUpdate2(bathSql);
			logger.error("b:"+b);
			if(b){
				resp.setResult(1);
				resp.setErrmsg("����ɹ�");
				return resp;
			}
			resp.setResult(-10);
			resp.setErrmsg("����ʧ��");
		} catch (Exception e) {
			logger.error(e);
		} finally {
			boolean b = memcacheUtils.delLock(lock);
			logger.error("ɾ����lock:"+lock+",b:"+b);
		}
		resp.setResult(-1);
		resp.setErrmsg("ϵͳ����");
		return resp;
	}
	
	@Override
	public BaseResp returnCard(UnbindCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			String nfc_uuid = req.getNfc_uuid();
			Long unbinder = req.getUnBinder();
			Long curTime = req.getCurTime();
			Long groupId = req.getGroupId();
			//û�г�������λ�Ρ���λ����
			if(nfc_uuid == null
					|| "".equals(nfc_uuid)
					|| unbinder <= 0
					|| groupId <= 0){
				resp.setResult(-2);
				resp.setErrmsg("��������");
				return resp;
			}
			//-------------------------�ֲ�ʽ��---------------------------//
			lock = commonMethods.getNFCLock(nfc_uuid);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-3);
				resp.setErrmsg("�����������");
				return resp;
			}
			//-------------------------��Ƭ��Ϣ---------------------------//
			Card card = commonMethods.card(nfc_uuid, groupId);
			//�����ѯ������
			if(card == null){
				resp.setResult(-4);
				resp.setErrmsg("�ÿ�Ƭδ�����������ڵ�ǰ��Ӫ����");
				return resp;
			}
			logger.error(card);
			long cardId = card.getId();
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
						ids = order.getId() + "";
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
				cashSqlMap.put("values", new Object[]{unbinder, balance, 2, curTime, 6, 1, accountId,
						groupId});
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
				resp.setErrmsg("ע���ɹ���Ӧ�˽�" + balance + "Ԫ");
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
			logger.error("ɾ����lock:"+lock+",b:"+b);
		}
		return resp;
	}

	@Override
	public BaseResp bindPlateCard(BindCardReq req) {
		BaseResp resp = new BaseResp();
		String lock = null;
		try {
			logger.error(req.toString());
			String nfc_uuid = req.getNfc_uuid();
			Long binder = req.getBinder();
			String mobile = req.getMobile();
			String carNumber = req.getCarNumber();
			Long curTime = req.getCurTime();
			long groupId = req.getGroupId();
			long parkId = req.getParkId();//������Ա���ڵ�ͣ������������Աû�г�������
			//û�в�λ����
			if(binder <= 0
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
			lock = commonMethods.getLock(nfc_uuid);
			if(!memcacheUtils.addLock(lock)){
				logger.error("lock:"+lock);
				resp.setResult(-12);
				resp.setErrmsg("�����������");
				return resp;
			}
			//--------------------У�鿨Ƭ---------------------//
			Card card = commonMethods.card(nfc_uuid, groupId);
			//�����ѯ������
			if(card == null){
				resp.setResult(-3);
				resp.setErrmsg("��Ƭ������");
				return resp;
			}
			logger.error(card.toString());
			int state = card.getState();
			long cardId = card.getId();
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
			//------------------------�ϰ��¼---------------------//
			WorkRecord workRecord = commonMethods.getWorkRecord(binder);
			long berthSegId = -1;
			if(workRecord != null){
				berthSegId = workRecord.getBerthsec_id();
			}
			logger.error("berthSegId:"+berthSegId);
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
					binder, -1L, parkId, berthSegId, groupId});
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
