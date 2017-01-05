package com.zld.pojo;

import java.io.Serializable;

public class Car implements Serializable {
	private Long id;
	private Long uin = -1L;//���������˺�
	private String car_number;//���ƺ���
	private Integer is_comuse = 0;//-- �Ƿ��ǳ��ó��ƣ�0 ���ǣ�1��
	private String remark;//˵��
	private Integer is_auth = 0;//-- �Ƿ�����֤ 0δ��֤��1����֤ 2��֤��
	private Long create_time;//��¼ʱ��
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUin() {
		return uin;
	}
	public void setUin(Long uin) {
		if(uin == null)
			uin = -1L;
		this.uin = uin;
	}
	public String getCar_number() {
		return car_number;
	}
	public void setCar_number(String car_number) {
		this.car_number = car_number;
	}
	public Integer getIs_comuse() {
		return is_comuse;
	}
	public void setIs_comuse(Integer is_comuse) {
		this.is_comuse = is_comuse;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getIs_auth() {
		return is_auth;
	}
	public void setIs_auth(Integer is_auth) {
		this.is_auth = is_auth;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	@Override
	public String toString() {
		return "Car [id=" + id + ", uin=" + uin + ", car_number=" + car_number
				+ ", is_comuse=" + is_comuse + ", remark=" + remark
				+ ", is_auth=" + is_auth + ", create_time=" + create_time + "]";
	}
}
