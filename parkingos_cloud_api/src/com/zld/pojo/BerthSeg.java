package com.zld.pojo;

import java.io.Serializable;

public class BerthSeg implements Serializable {
	private Long id;//
	private String uuid;// Ψһ��ʶ
	private String berthsec_name;//��λ������
	private String park_uuid;//����ͣ����uuid
	private Long create_time;// ��������
	private String address;//��ϸ��ַ
	private Double longitude = 0d;//����
	private Double latitude = 0d;//γ��
	private Integer is_active = 0;//״̬ 0������ 1������
	private Long comid = -1L;//�������
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getBerthsec_name() {
		return berthsec_name;
	}
	public void setBerthsec_name(String berthsec_name) {
		this.berthsec_name = berthsec_name;
	}
	public String getPark_uuid() {
		return park_uuid;
	}
	public void setPark_uuid(String park_uuid) {
		this.park_uuid = park_uuid;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Integer getIs_active() {
		return is_active;
	}
	public void setIs_active(Integer is_active) {
		this.is_active = is_active;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		this.comid = comid;
	}
	@Override
	public String toString() {
		return "BerthSeg [id=" + id + ", uuid=" + uuid + ", berthsec_name="
				+ berthsec_name + ", park_uuid=" + park_uuid + ", create_time="
				+ create_time + ", address=" + address + ", longitude="
				+ longitude + ", latitude=" + latitude + ", is_active="
				+ is_active + ", comid=" + comid + "]";
	}
	
}
