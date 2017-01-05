package com.zld.pojo;

import java.io.Serializable;

public class ManuPayPosOrderFacadeReq implements Serializable {
	private Long orderId;// ��������
	private Long uid = -1L;// �շ�Ա���
	private String imei;// �ֻ�����
	private Double money = 0d;// ������
	private String nfc_uuid;//ˢ������Ŀ�Ƭ���
	private Integer payType = 0;//֧����ʽ 0���ֽ�֧�� 1��ˢ��֧��
	private Integer version = -1;// �汾��
	private Integer bindcard = 0;//0:�ͻ��˵����󶨳����ֻ��ŵ���ʾ�� 1����������ʾ��ֱ��ˢ��Ԥ��
	private Long groupId = -1L;//�շ�Ա���ڵ���Ӫ���ű��
	private Long endTime = -1L;//��������ʱ��
	private Long curTime = System.currentTimeMillis() / 1000;
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Integer getPayType() {
		return payType;
	}
	public void setPayType(Integer payType) {
		this.payType = payType;
	}
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
	public void setCurTime(Long curTime) {
		this.curTime = curTime;
	}
	public Integer getBindcard() {
		return bindcard;
	}
	public void setBindcard(Integer bindcard) {
		this.bindcard = bindcard;
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
		return "ManuPayPosOrderFacadeReq [orderId=" + orderId + ", uid=" + uid
				+ ", imei=" + imei + ", money=" + money + ", nfc_uuid="
				+ nfc_uuid + ", payType=" + payType + ", version=" + version
				+ ", bindcard=" + bindcard + ", groupId=" + groupId
				+ ", endTime=" + endTime + ", curTime=" + curTime + "]";
	}
	
}
