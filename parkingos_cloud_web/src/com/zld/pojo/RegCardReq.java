package com.zld.pojo;

import java.io.Serializable;

public class RegCardReq implements Serializable {
	private String nfc_uuid;//��Ƭ���õ�ΨһӲ�����
	private String cardNo;//����ţ�ӡ�ڿ����ϵı�ţ�
	private Long regId = -1L;//�������˺�
	private Double money = 100d;//��ʼ�����
	private Long groupId = -1L;//��Ӫ���ű��
	private String cardName;//��Ƭ����
	private String device;//�����豸
	private Long curTime = System.currentTimeMillis()/1000;
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public Long getRegId() {
		return regId;
	}
	public void setRegId(Long regId) {
		this.regId = regId;
	}
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		this.money = money;
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
	public String getCardName() {
		return cardName;
	}
	public void setCardName(String cardName) {
		this.cardName = cardName;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	
	@Override
	public String toString() {
		return "RegCardReq [nfc_uuid=" + nfc_uuid + ", cardNo=" + cardNo
				+ ", regId=" + regId + ", money=" + money + ", curTime="
				+ curTime + "]";
	}
}
