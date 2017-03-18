package com.zld.pojo;

import java.io.Serializable;

public class AutoPayPosOrderReq implements Serializable {
	private Order order;//��������
	private Long uid = -1L;//�շ�Ա���
	private Long workId = -1L;//��ǰ�ϰ��¼���
	private String imei;//�ֻ�����
	private Double money = 0d;//������
	private Long userId;//��ǰ�����û����
	private Long berthOrderId = -1L;//�󶨵ĳ������������
	private Long endTime;//��������ʱ��
	private Integer version = -1;//�汾��
	private Long groupId = -1L;//�շ�Ա������Ӫ���ű��
	private Long curTime = System.currentTimeMillis()/1000;
	private Integer payType=0;
	
	
	
	public Integer getPayType() {
		return payType;
	}
	public void setPayType(Integer payType) {
		this.payType = payType;
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
	public Long getCurTime() {
		return curTime;
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
	public Long getWorkId() {
		return workId;
	}
	public void setWorkId(Long workId) {
		this.workId = workId;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getBerthOrderId() {
		return berthOrderId;
	}
	public void setBerthOrderId(Long berthOrderId) {
		this.berthOrderId = berthOrderId;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	@Override
	public String toString() {
		return "AutoPayPosOrderReq [order=" + order + ", uid=" + uid
				+ ", workId=" + workId + ", imei=" + imei + ", money=" + money
				+ ", userId=" + userId + ", berthOrderId=" + berthOrderId
				+ ", endTime=" + endTime + ", version=" + version
				+ ", groupId=" + groupId + ", curTime=" + curTime + "]";
	}
	
}
