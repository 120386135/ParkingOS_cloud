package com.zld.facade;

import com.zld.pojo.StatsFacadeResp;
import com.zld.pojo.StatsReq;

public interface StatsAccountFacade {
	/**
	 * ͳ���շ�Ա��Ŀ
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsParkUserAccount(StatsReq req);
	
	/**
	 * ͳ�Ƴ�����Ŀ
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsParkAccount(StatsReq req);
	
	/**
	 * ͳ�Ʋ�λ����Ŀ
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsBerthSegAccount(StatsReq req);
	
	/**
	 * ͳ�Ʋ�λ��Ŀ
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsBerthAccount(StatsReq req);
	
	/**
	 * ͳ����Ӫ������Ŀ
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsGroupAccount(StatsReq req);
}
