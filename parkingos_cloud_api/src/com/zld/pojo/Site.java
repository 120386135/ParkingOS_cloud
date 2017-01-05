package com.zld.pojo;

import java.io.Serializable;

/**
 * ��վӳ�����
 * @author whx
 *
 */
public class Site implements Serializable {
	private Long id = -1L;//
	private String uuid;//��վΨһ���
	private Double voltage;
	private Long update_time;//
	private Double longitude;//
	private Double latitude;//
	private Long create_time;//����ʱ��
	private Long heartbeat;//����ʱ��
	private String address;//��ַ
	private String name;//��վ����
	private Integer state = 0;// -- ��վ״̬ 0:���� 1:����
	private Integer is_delete = 0;// -- 0������ 1������
	private Long cityid = -1L;//-- �����̻����
	private Long comid = -1L;//��������
	private Long groupid = -1L;//������Ӫ����
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
	public Double getVoltage() {
		return voltage;
	}
	public void setVoltage(Double voltage) {
		this.voltage = voltage;
	}
	public Long getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(Long update_time) {
		this.update_time = update_time;
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
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Long getHeartbeat() {
		return heartbeat;
	}
	public void setHeartbeat(Long heartbeat) {
		this.heartbeat = heartbeat;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public Integer getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(Integer is_delete) {
		this.is_delete = is_delete;
	}
	public Long getCityid() {
		return cityid;
	}
	public void setCityid(Long cityid) {
		if(cityid == null)
			cityid = -1L;
		this.cityid = cityid;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
	}
	public Long getGroupid() {
		return groupid;
	}
	public void setGroupid(Long groupid) {
		if(groupid == null)
			groupid = -1L;
		this.groupid = groupid;
	}
	@Override
	public String toString() {
		return "Site [id=" + id + ", uuid=" + uuid + ", voltage=" + voltage
				+ ", update_time=" + update_time + ", longitude=" + longitude
				+ ", latitude=" + latitude + ", create_time=" + create_time
				+ ", heartbeat=" + heartbeat + ", address=" + address
				+ ", name=" + name + ", state=" + state + ", is_delete="
				+ is_delete + ", cityid=" + cityid + ", comid=" + comid
				+ ", groupid=" + groupid + "]";
	}
	
}
