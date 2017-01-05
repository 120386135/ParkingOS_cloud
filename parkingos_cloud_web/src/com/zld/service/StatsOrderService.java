package com.zld.service;

import org.springframework.stereotype.Service;

import com.zld.pojo.AccountReq;
import com.zld.pojo.AccountResp;
import com.zld.pojo.StatsOrderResp;
import com.zld.pojo.StatsReq;
@Service
public interface StatsOrderService {
	/**
	 * ����ͳ��
	 * @param req
	 * @return
	 */
	public StatsOrderResp statsOrder(StatsReq req);
	
	/**
	 * �鶩��
	 * @param req
	 * @return
	 */
	public AccountResp order(AccountReq req);
}
