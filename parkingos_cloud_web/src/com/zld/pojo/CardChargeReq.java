package com.zld.pojo;

import java.io.Serializable;

public class CardChargeReq implements Serializable {
	private Long cardId = -1L;//��Ƭ���
	private Double money = 0d;//��ֵ���
	private Integer chargeType = 0;//��ֵ��ʽ��0���ֽ��ֵ 1��΢�Ź��ںų�ֵ 2��΢�ſͻ��˳�ֵ 3��֧������ֵ 4��Ԥ֧���˿� 5�������˿� 
	private Long cashierId = -1L;//�շ�Ա���
	private Long orderId = -1L;//�������
	private Long groupId = -1L;//������������Ӫ����
	private Long curTime = System.currentTimeMillis()/1000;//��ǰʱ��
	
	private String subOrderId;//������֧���Ķ�����
	public Long getCardId() {
		return cardId;
	}
	public void setCardId(Long cardId) {
		if(cardId == null)
			cardId = -1L;
		this.cardId = cardId;
	}
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		if(money == null)
			money = 0d;
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
		if(cashierId == null)
			cashierId = -1L;
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
		if(orderId == null)
			orderId = -1L;
		this.orderId = orderId;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	@Override
	public String toString() {
		return "CardChargeReq [cardId=" + cardId + ", money=" + money
				+ ", chargeType=" + chargeType + ", cashierId=" + cashierId
				+ ", orderId=" + orderId + ", groupId=" + groupId
				+ ", curTime=" + curTime + ", subOrderId=" + subOrderId + "]";
	}
	
}
