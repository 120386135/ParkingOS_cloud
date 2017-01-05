package com.zld.pojo;

import java.io.Serializable;

public class GenPosOrderFacadeReq implements Serializable {
	private Long orderId = -1L;//�е����ɶ���֮ǰ��Ԥȡ������
	private String carNumber;//���ƺ�
	private Long berthId = -1L;//��λ���
	private String imei;//�ֻ�����
	private Integer version = -1;//�汾��
	private Long uid = -1L;//�շ�Ա���
	private Long parkId = -1L;//�������
	private Long groupId = -1L;//��Ӫ���ű��
	private Long curTime = System.currentTimeMillis()/1000;
	//---------Ԥ������-------------//
	private Integer payType = 0;//Ԥ������ 0:�ֽ�Ԥ�� 1��ˢ��Ԥ��
	private String nfc_uuid;//ˢ��Ԥ֧��ʱ�Ŀ���
	private Double prepay = 0d;//Ԥ�����
	private Integer bindcard = 0;//Ԥ֧���õ��Ĳ�����0:�ͻ��˵����󶨳����ֻ��ŵ���ʾ�� 1����������ʾ��ֱ��ˢ��Ԥ��
	private Integer carType = 0;
	
	public Integer getCarType() {
		return carType;
	}
	public void setCarType(Integer carType) {
		this.carType = carType;
	}
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public String getCarNumber() {
		return carNumber;
	}
	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	public Long getParkId() {
		return parkId;
	}
	public void setParkId(Long parkId) {
		this.parkId = parkId;
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
	public Double getPrepay() {
		return prepay;
	}
	public void setPrepay(Double prepay) {
		this.prepay = prepay;
	}
	public Integer getBindcard() {
		return bindcard;
	}
	public void setBindcard(Integer bindcard) {
		this.bindcard = bindcard;
	}
	public Long getBerthId() {
		return berthId;
	}
	public void setBerthId(Long berthId) {
		this.berthId = berthId;
	}
	public Integer getPayType() {
		return payType;
	}
	public void setPayType(Integer payType) {
		this.payType = payType;
	}
	public Long getCurTime() {
		return curTime;
	}
	@Override
	public String toString() {
		return "GenPosOrderFacadeReq [orderId=" + orderId + ", carNumber="
				+ carNumber + ", berthId=" + berthId + ", imei=" + imei
				+ ", version=" + version + ", uid=" + uid + ", parkId="
				+ parkId + ", groupId=" + groupId + ", curTime=" + curTime
				+ ", payType=" + payType + ", nfc_uuid=" + nfc_uuid
				+ ", prepay=" + prepay + ", bindcard=" + bindcard
				+ ", carType=" + carType + "]";
	}
}
