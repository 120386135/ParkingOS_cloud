package com.zld.pojo;

import java.io.Serializable;

public class GenPosOrderReq implements Serializable {
	private Long orderId = -1L;//�е����ɶ���֮ǰ��Ԥȡ������
	private String carNumber;//���ƺ�
	private Berth berth;//
	private String imei;//�ֻ�����
	private Long userId = -1L;//�û����
	private Integer cType = 2;//�������ɷ�ʽ 2��¼�복�� 5���¿���Ա
	private Long workId = -1L;//�ϰ���
	private Integer version = -1;//�汾��
	private Long berthOrderId = -1L;//�󶨵ĳ������������
	private Long startTime;//������ʼʱ��
	private Long uid = -1L;//�շ�Ա���
	private Long parkId = -1L;//�������
	private Long groupId = -1L;//��Ӫ���ű��
	private Integer carType = 0;//��������
	private Long curTime = System.currentTimeMillis()/1000;//��ǰʱ��
	//---------Ԥ������-------------//
	private String nfc_uuid;//ˢ��Ԥ֧��ʱ�Ŀ���
	private Double prepay = 0d;//Ԥ�����
	private Integer bindcard = 0;//Ԥ֧���õ��Ĳ�����0:�ͻ��˵����󶨳����ֻ��ŵ���ʾ�� 1����������ʾ��ֱ��ˢ��Ԥ��
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getWorkId() {
		return workId;
	}
	public void setWorkId(Long workId) {
		this.workId = workId;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Long getBerthOrderId() {
		return berthOrderId;
	}
	public void setBerthOrderId(Long berthOrderId) {
		this.berthOrderId = berthOrderId;
	}
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
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
	public Integer getcType() {
		return cType;
	}
	public void setcType(Integer cType) {
		this.cType = cType;
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
	public Berth getBerth() {
		return berth;
	}
	public void setBerth(Berth berth) {
		this.berth = berth;
	}
	public Long getCurTime() {
		return curTime;
	}
	public Integer getCarType() {
		return carType;
	}
	public void setCarType(Integer carType) {
		this.carType = carType;
	}
	@Override
	public String toString() {
		return "GenOrderReq [orderId=" + orderId + ", carNumber=" + carNumber
				+ ", berth=" + berth + ", imei=" + imei + ", userId=" + userId
				+ ", cType=" + cType + ", workId=" + workId + ", version="
				+ version + ", berthOrderId=" + berthOrderId + ", startTime="
				+ startTime + ", uid=" + uid + ", parkId=" + parkId
				+ ", groupId=" + groupId + ", carType=" + carType
				+ ", curTime=" + curTime + ", nfc_uuid=" + nfc_uuid
				+ ", prepay=" + prepay + ", bindcard=" + bindcard + "]";
	}
}
