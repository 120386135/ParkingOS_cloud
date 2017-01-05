package com.zld.pojo;

import java.io.Serializable;

public class PayEscapePosOrderReq implements Serializable {
	private Order order;//��������
	private Long berthId = -1L;//׷�ɶ������ڵ�
	private Long uid = -1L;//�շ�Ա���
	private Long berthSegId = -1L;//��λ�α��
	private String imei;//�ֻ�����
	private Double money = 0d;//������
	private Long userId;//��ǰ�����û����
	private Long berthOrderId = -1L;//�󶨵ĳ������������
	private Integer version = -1;//�汾��
	private String nfc_uuid;//ˢ��֧��ʱ�Ŀ�ƬΨһ���
	private Integer bindcard = 0;//0:�ͻ��˵����󶨳����ֻ��ŵ���ʾ�� 1����������ʾ��ֱ��ˢ��Ԥ��
	private Long groupId = -1L;//׷���շ�Ա���ڵ���Ӫ����
	private Long parkId = -1L;//׷���շ�Ա����ͣ����
	private Long curTime = System.currentTimeMillis()/1000;
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
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
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Integer getBindcard() {
		return bindcard;
	}
	public void setBindcard(Integer bindcard) {
		this.bindcard = bindcard;
	}
	public Long getCurTime() {
		return curTime;
	}
	public Long getBerthOrderId() {
		return berthOrderId;
	}
	public void setBerthOrderId(Long berthOrderId) {
		this.berthOrderId = berthOrderId;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public Long getParkId() {
		return parkId;
	}
	public void setParkId(Long parkId) {
		this.parkId = parkId;
	}
	public Long getBerthId() {
		return berthId;
	}
	public void setBerthId(Long berthId) {
		this.berthId = berthId;
	}
	public Long getBerthSegId() {
		return berthSegId;
	}
	public void setBerthSegId(Long berthSegId) {
		this.berthSegId = berthSegId;
	}
	@Override
	public String toString() {
		return "PayEscapePosOrderReq [order=" + order + ", berthId=" + berthId
				+ ", uid=" + uid + ", berthSegId=" + berthSegId + ", imei="
				+ imei + ", money=" + money + ", userId=" + userId
				+ ", berthOrderId=" + berthOrderId + ", version=" + version
				+ ", nfc_uuid=" + nfc_uuid + ", bindcard=" + bindcard
				+ ", groupId=" + groupId + ", parkId=" + parkId + ", curTime="
				+ curTime + "]";
	}
}
