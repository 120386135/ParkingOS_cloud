package com.zld.service;

import com.zld.pojo.BaseResp;
import com.zld.pojo.BindCardReq;
import com.zld.pojo.CardChargeReq;
import com.zld.pojo.ReturnCardReq;
import com.zld.pojo.UnbindCardReq;

public interface CardService {
	
	/**
	 * �󶨻�Ա(�ֻ���)
	 * @param req
	 * @return
	 */
	public BaseResp bindUserCard(BindCardReq req);
	
	/**
	 * �󶨳���(ֻ�г���)
	 * @param req
	 * @return
	 */
	public BaseResp bindPlateCard(BindCardReq req);
	
	/**
	 * ���
	 * @param req
	 * @return
	 */
	public BaseResp unBindCard(UnbindCardReq req);
	
	/**
	 * ��Ƭ��ֵ
	 * @param cardChargeReq
	 * @return
	 */
	public BaseResp cardCharge(CardChargeReq cardChargeReq);
	
	/**
	 * ע����Ƭ
	 * @return
	 */
	public BaseResp returnCard(ReturnCardReq req);
}
