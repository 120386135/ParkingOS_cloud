package com.zld.service;

import com.zld.pojo.AccountReq;
import com.zld.pojo.AccountResp;
import com.zld.pojo.StatsAccountResp;
import com.zld.pojo.StatsReq;

/**
 * ��Ŀͳ��
 * @author whx
 *
 */
public interface StatsAccountService {
	
	/**
	 * ͳ����ˮ��Ŀ
	 * @param req
	 * @return
	 */
	public StatsAccountResp statsAccount(StatsReq req);
	
	/**
	 * ����ˮ��Ŀ��ϸ
	 * @param req
	 * @return
	 */
	public AccountResp account(AccountReq req);
	
}
