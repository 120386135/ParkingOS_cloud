package com.zld.service;

import com.zld.pojo.GenPosOrderReq;
import com.zld.pojo.GenPosOrderResp;

public interface GenPosOrderService {
	/**
	 * pos�����ɶ���
	 * @param req
	 * @return
	 */
	public GenPosOrderResp genPosOrder(GenPosOrderReq req);
}
