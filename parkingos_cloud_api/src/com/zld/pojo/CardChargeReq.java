package com.zld.pojo;

import java.io.Serializable;

public class CardChargeReq implements Serializable {
	private String nfc_uuid;//��ƬΨһ���
	private Double money = 0d;//��ֵ���
	private Integer chargeType = 0;//��ֵ��ʽ��0���ֽ��ֵ 1��΢�Ź��ںų�ֵ 2��΢�ſͻ��˳�ֵ 3��֧������ֵ 4��Ԥ֧���˿� 5�������˿� 
	private Long cashierId = -1L;//�շ�Ա���
	private Long orderId = -1L;//�������
	private Long groupId = -1L;
	private String subOrderId;//������֧���Ķ�����
	private Long parkId = -1L;//�շ�Ա���ڳ������
	private Long curTime = System.currentTimeMillis()/1000;//��ǰʱ��
	
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		this.money = money;
	}
	public String getSubOrderId() {
		return subOrderId;
	}
	public void setSubOrderId(String subOrderId) {
		this.subOrderId = subOrderId;
	}
	public Long getCurTime() {
		return curTime;
	}
	public Long getCashierId() {
		return cashierId;
	}
	public void setCashierId(Long cashierId) {
		this.cashierId = cashierId;
	}
	public Integer getChargeType() {
		return chargeType;
	}
	public void setChargeType(Integer chargeType) {
		this.chargeType = chargeType;
	}
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Long getParkId() {
		return parkId;
	}
	public void setParkId(Long parkId) {
		this.parkId = parkId;
	}
	@Override
	public String toString() {
		return "CardChargeReq [nfc_uuid=" + nfc_uuid + ", money=" + money
				+ ", chargeType=" + chargeType + ", cashierId=" + cashierId
				+ ", orderId=" + orderId + ", curTime=" + curTime
				+ ", groupId=" + groupId + ", subOrderId=" + subOrderId
				+ ", parkId=" + parkId + "]";
	}
}
