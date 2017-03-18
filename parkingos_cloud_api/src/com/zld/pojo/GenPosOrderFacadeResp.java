package com.zld.pojo;

import java.io.Serializable;

public class GenPosOrderFacadeResp extends BaseResp implements Serializable {
	private Long orderid = -1L;
	private String btime;//ͣ����ʼʱ��
	private Integer ctype = 2;
	public Integer getCtype() {
		return ctype;
	}
	public void setCtype(Integer ctype) {
		this.ctype = ctype;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getBtime() {
		return btime;
	}
	public void setBtime(String btime) {
		this.btime = btime;
	}
	@Override
	public String toString() {
		return "GenOrderResp [orderid=" + orderid + ", btime=" + btime + "]";
	}
}
