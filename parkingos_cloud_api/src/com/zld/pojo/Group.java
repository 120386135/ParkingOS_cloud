package com.zld.pojo;

import java.io.Serializable;

public class Group implements Serializable {
	private Long id = -1L;
	private Integer state = 0;
	private String name;
	private Long chanid = -1L;//�������
	private Long create_time;//����ʱ��
	private Long cityid = -1L;//�����̻����
	private Integer type = 0;//��˾���� 0����ͨ��Ӫ��˾ 1�����׮��Ӫ��˾ 2�����г���Ӫ��˾
	private Double balance = 0d;//��Ӫ���ŵ����˻����
	private String address;//��ַ
	private Double longitude;//����
	private Double latitude;//γ��
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getChanid() {
		return chanid;
	}
	public void setChanid(Long chanid) {
		if(chanid == null)
			chanid = -1L;
		this.chanid = chanid;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Long getCityid() {
		return cityid;
	}
	public void setCityid(Long cityid) {
		if(cityid == null)
			cityid = -1L;
		this.cityid = cityid;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		if(type == null)
			type = 0;
		this.type = type;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		if(balance == null)
			balance = 0d;
		this.balance = balance;
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
	@Override
	public String toString() {
		return "Group [id=" + id + ", state=" + state + ", name=" + name
				+ ", chanid=" + chanid + ", create_time=" + create_time
				+ ", cityid=" + cityid + ", type=" + type + ", balance="
				+ balance + ", address=" + address + ", longitude=" + longitude
				+ ", latitude=" + latitude + "]";
	}
	
}
