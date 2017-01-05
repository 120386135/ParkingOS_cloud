package com.zld.pojo;

import java.io.Serializable;
/**
 * ������ӳ�����
 * @author whx
 *
 */
public class Sensor implements Serializable {
	private Long id = -1L;//
	private String code;//
	private Long comid = -1L;//��������
	private Integer state = 0;//-- 0���� 1ռ��
	private String serid;//-- �ش�����ID
	private String did;//-- ������ID(Ψһ���)
	private Long operate_time;//-- ����ʱ��
	private Double battery;//-- ��ص�ѹ
	private Double magnetism;//-- ���ݵ�ѹ
	private Long beart_time;// -- ����ʱ��
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public String getSerid() {
		return serid;
	}
	public void setSerid(String serid) {
		this.serid = serid;
	}
	public String getDid() {
		return did;
	}
	public void setDid(String did) {
		this.did = did;
	}
	public Long getOperate_time() {
		return operate_time;
	}
	public void setOperate_time(Long operate_time) {
		this.operate_time = operate_time;
	}
	public Double getBattery() {
		return battery;
	}
	public void setBattery(Double battery) {
		this.battery = battery;
	}
	public Double getMagnetism() {
		return magnetism;
	}
	public void setMagnetism(Double magnetism) {
		this.magnetism = magnetism;
	}
	public Long getBeart_time() {
		return beart_time;
	}
	public void setBeart_time(Long beart_time) {
		this.beart_time = beart_time;
	}
	@Override
	public String toString() {
		return "Sensor [id=" + id + ", code=" + code + ", comid=" + comid
				+ ", state=" + state + ", serid=" + serid + ", did=" + did
				+ ", operate_time=" + operate_time + ", battery=" + battery
				+ ", magnetism=" + magnetism + ", beart_time=" + beart_time
				+ "]";
	}
	
}
