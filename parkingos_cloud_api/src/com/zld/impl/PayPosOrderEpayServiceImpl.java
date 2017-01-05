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
import com.zld.service.PgOnlyReadService;

/**
 * ���ӽ��㶩��
 * @author whx
 *
 */
@Service("payEpay")
public class PayPosOrderEpayServiceImpl implements PayPosOrderService {
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	private DataBaseService writeService;
	
	Logger logger = Logger.getLogger(PayPosOrderEpayServiceImpl.class);
	
	/**
	 * �������Ԥ֧������㹻�Ķ��������ߵ���֧��û��Ԥ֧�����Ķ���
	 */
	@Override
	public AutoPayPosOrderResp autoPayPosOrder(AutoPayPosOrderReq req) {
		AutoPayPosOrderResp resp = new AutoPayPosOrderResp();
		try {
			logger.error("req:"+req.toString());
			//----------------------------����--------------------------------//
			Long curTime = req.getCurTime();
			Order order = req.getOrder();
			Double money = req.getMoney();//�ܽ��
			String imei = req.getImei();//�ֻ�����
			Long workId = req.getWorkId();//��ǰ�ϰ��¼
			Long uid = req.getUid();//�շ�Ա���
			Integer version = req.getVersion();//�汾��
			Long userId = req.getUserId();//�û����
			Long brethOrderId = req.getBerthOrderId();
			Long endTime = req.getEndTime();
			//----------------------------У�����--------------------------------//
			if(order == null 
					|| uid <= 0 
					|| workId <= 0 
					|| money < 0
					|| curTime == null){//money����Ϊ��
				resp.setResult(-1);
				resp.setErrmsg("��������");
				return resp;//��������
			}
			//----------------------------��ȡ������Ϣ--------------------------------//
			Map<String, Object> orderMap = new org.apache.commons.beanutils.BeanMap(order);
			logger.error("orderMap:"+orderMap);
			Integer state = (Integer)orderMap.get("state");
			if(state == 1){
				resp.setResult(-2);
				resp.setErrmsg("�����ѽ���");
				return resp;
			}
			if(state == 2){
				resp.setResult(-3);
				resp.setErrmsg("��������Ϊ�ӵ�");
				return resp;
			}
			Double prepay = Double.valueOf(orderMap.get("prepaid") + "");
			Integer pay_type = (Integer)orderMap.get("pay_type");
			long orderId = order.getId();
			//-----------------------------�߼�����----------------------------------//
			if(userId > 0){
				boolean result = false;
				if(prepay >= money && pay_type == 2){
					logger.error("������Ԥ֧���㹻>>>orderid:"+order.getId());
					int r = publicMethods.doPrePayOrder(orderMap, money);
					logger.error("r:"+r);
					if(r == 1){
						result = true;
					}
				}else if(prepay == 0 && pay_type == 0){//δ��Ԥ֧�����Ķ�������֧��
					logger.error("���Ե���֧��>>>orderid:"+order.getId());
					//�鳵�����ã��Ƿ��������Զ�֧����û������ʱ��Ĭ��25Ԫ�����Զ�֧�� 
					Integer autoCash = 1;//Ĭ���Զ�֧��
					Integer limitMoney = 25;//Ĭ��������Զ�֧��25Ԫ
					Map<String, Object> upMap = readService.getPojo("select auto_cash,limit_money " +
							" from user_profile_tb where uin =?", new Object[]{userId});
					if(upMap !=null && upMap.get("auto_cash") != null){
						autoCash = (Integer)upMap.get("auto_cash");
						limitMoney = (Integer)upMap.get("limit_money");//-1��ʾû������
					}
					logger.error("autoCash:"+autoCash+",limitMoney:"+limitMoney);
					if(autoCash == 1 
							&& (limitMoney == -1 || limitMoney >= money)){
						int r = publicMethods.payOrder(orderMap, money, userId, 2, 0,
								-1L, null, -1L, uid);
						logger.error("r:"+r);
						if(r == 5){
							result = true;
						}
					}
				}
				logger.error("result:"+result);
				if(result){
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					if(order.getBerthnumber() > 0){
						//���²�λ״̬
						Map<String, Object> berthSqlMap = new HashMap<String, Object>();
						berthSqlMap.put("sql", "update com_park_tb set state=?,order_id=?," +
								"end_time=? where id =? and order_id=?");
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
					logger.error("b:"+b+",orderid:"+orderId);
					resp.setResult(1);
					resp.setErrmsg("����֧�����㶩���ɹ�");
					return resp;
				}
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

	@Override
	public ManuPayPosOrderResp manuPayPosOrder(ManuPayPosOrderReq req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PayEscapePosOrderResp payEscapePosOrder(PayEscapePosOrderReq req) {
		// TODO Auto-generated method stub
		return null;
	}

}
