package com.zld.service;

import com.zld.pojo.AccountReq;
import com.zld.pojo.AccountResp;
import com.zld.pojo.StatsCardResp;
import com.zld.pojo.StatsReq;

public interface StatsCardService {
	/**
	 * ͳ�ƿ�Ƭ
	 * @param req
	 * @return
	 */
	public StatsCardResp statsCard(StatsReq req);
	
	/**
	 * ����ˮ��Ŀ��ϸ
	 * @param req
	 * @return
	 */
	public AccountResp account(AccountReq req);
}
