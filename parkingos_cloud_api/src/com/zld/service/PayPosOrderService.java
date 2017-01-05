package com.zld.service;

import com.zld.pojo.AutoPayPosOrderReq;
import com.zld.pojo.AutoPayPosOrderResp;
import com.zld.pojo.ManuPayPosOrderReq;
import com.zld.pojo.ManuPayPosOrderResp;
import com.zld.pojo.PayEscapePosOrderReq;
import com.zld.pojo.PayEscapePosOrderResp;

public interface PayPosOrderService {
	/**
	 * �Զ�֧��pos������
	 * @param req
	 * @return
	 */
	public AutoPayPosOrderResp autoPayPosOrder(AutoPayPosOrderReq req);
	
	/**
	 * �ֶ�����POS���������˽ӿ�����autoPayPosOrder�ӿڣ�
	 * ֻ��autoPayPosOrder�ӿڲ��ܽ����ʱ��Ż���øýӿڡ�
	 * @param req
	 * @return
	 */
	public ManuPayPosOrderResp manuPayPosOrder(ManuPayPosOrderReq req);
	
	/**
	 * POS�������ӵ�
	 * @param req
	 * @return
	 */
	public PayEscapePosOrderResp payEscapePosOrder(PayEscapePosOrderReq req);
}
