package com.zld.service;

import com.zld.pojo.ActCardReq;
import com.zld.pojo.BaseResp;
import com.zld.pojo.BindCardReq;
import com.zld.pojo.CardChargeReq;
import com.zld.pojo.CardInfoReq;
import com.zld.pojo.CardInfoResp;
import com.zld.pojo.DefaultCardReq;
import com.zld.pojo.DefaultCardResp;
import com.zld.pojo.RegCardReq;
import com.zld.pojo.UnbindCardReq;

public interface CardService {
	
	/**
	 * ��ȡ��Ƭ��Ϣ
	 * @param req
	 * @return
	 */
	public CardInfoResp getCardInfo(CardInfoReq req);
	
	/**
	 * ��������Ƭ��⣨ֻ�п��������д˹��ܣ�
	 * @param req
	 * @return
	 */
	public BaseResp regCard(RegCardReq req);
	
	/**
	 * ���Ƭ���շ�Ա����Ϳ��������д˹��ܣ�
	 * @return
	 */
	public BaseResp actCard(ActCardReq req);
	
	/**
	 * �󶨻�Ա(�ֻ���)���շ�Ա����Ϳ��������д˹��ܣ�
	 * @param req
	 * @return
	 */
	public BaseResp bindUserCard(BindCardReq req);
	
	/**
	 * �󶨳���(ֻ�г���)���շ�Ա����Ϳ��������д˹��ܣ�
	 * @param req
	 * @return
	 */
	public BaseResp bindPlateCard(BindCardReq req);
	
	/**
	 * ��Ƭ��ֵ���շ�Ա����Ϳ��������д˹��ܣ�
	 * @param cardChargeReq
	 * @return
	 */
	public BaseResp cardCharge(CardChargeReq cardChargeReq);
	
	/**
	 * ע����Ƭ��ֻ�п��������д˹��ܣ�
	 * @return
	 */
	public BaseResp returnCard(UnbindCardReq req);
	
	/**
	 * ȡ��Ա�󶨵�������Ŀ�Ƭ
	 * @return
	 */
	public DefaultCardResp getDefaultCard(DefaultCardReq req);
}
