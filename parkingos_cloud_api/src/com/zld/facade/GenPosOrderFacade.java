package com.zld.facade;

import com.zld.pojo.GenPosOrderFacadeReq;
import com.zld.pojo.GenPosOrderFacadeResp;

public interface GenPosOrderFacade {
	/**
	 * pos�����ɶ���
	 * @param req
	 * @return
	 */
	public GenPosOrderFacadeResp genPosOrder(GenPosOrderFacadeReq req);
}
