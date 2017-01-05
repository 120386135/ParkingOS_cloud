package com.zld.facade;

import com.zld.pojo.AutoPayPosOrderFacadeReq;
import com.zld.pojo.AutoPayPosOrderResp;
import com.zld.pojo.ManuPayPosOrderFacadeReq;
import com.zld.pojo.ManuPayPosOrderResp;
import com.zld.pojo.PayEscapePosOrderFacadeReq;
import com.zld.pojo.PayEscapePosOrderResp;

public interface PayPosOrderFacade {
	/**
	 * �Զ�֧��pos��������������򲻽��㶩����ֱ�ӷ��أ���Ϊ������������ֶ�֧����ʽ�����ֽ�֧����ˢ��֧����
	 * @param req
	 * @return
	 */
	public AutoPayPosOrderResp autoPayPosOrder(AutoPayPosOrderFacadeReq req);
	
	/**
	 * �ֶ�����POS���������˽ӿ�����autoPayPosOrder�ӿڣ�
	 * ֻ��autoPayPosOrder�ӿڲ��ܽ����ʱ��Ż���øýӿڡ�
	 * @param req
	 * @return
	 */
	public ManuPayPosOrderResp manuPayPosOrder(ManuPayPosOrderFacadeReq req);
	
	/**
	 * ׷��POS������
	 * @param req
	 * @return
	 */
	public PayEscapePosOrderResp payEscapePosOrder(PayEscapePosOrderFacadeReq req);
}
