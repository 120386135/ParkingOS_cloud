package com.zld.pojo;

import java.io.Serializable;

public class AutoPayPosOrderFacadeReq implements Serializable {
	private Long orderId;//��������
	private Long uid = -1L;//�շ�Ա���
	private String imei;//�ֻ�����
	private Double money = 0d;//������
	private Integer version = -1;//�汾��
	private Long groupId = -1L;//�շ�Ա������Ӫ����
	private Long endTime = -1L;//��������ʱ�䣨���ɽ������ʱ�䣬�ͻ��˵Ͱ汾δ����ò�����
	private Long curTime = System.currentTimeMillis()/1000;
	
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		this.money = money;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Long getCurTime() {
		return curTime;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	@Override
	public String toString() {
		return "AutoPayPosOrderFacadeReq [orderId=" + orderId + ", uid=" + uid
				+ ", imei=" + imei + ", money=" + money + ", version="
				+ version + ", groupId=" + groupId + ", endTime=" + endTime
				+ ", curTime=" + curTime + "]";
	}
	
}
