package com.zld.pojo;

import java.io.Serializable;

public class Order implements Serializable {
	private Long id;
	private Long comid = -1L;//ͣ�������
	private Long uin = -1L;//�������
	private Double total = 0d;//������
	private Integer state = 0;//-- 0δ֧�� 1��֧�� 2:�ӵ�
	private Long create_time;//��������ʱ��
	private Long end_time;//��������ʱ��
	private Integer auto_pay = 0;//-- 0:�������㣬1�������쳣����Ķ�����2�����������ƵĶ�����3:��¼�������ɵĶ���
	private Integer pay_type = 0;//-- 0:�ʻ�֧��,1:�ֽ�֧��,2:�ֻ�֧�� 3:���� 4:�ֽ�Ԥ֧�� 5�������� 6���̼ҿ�8:��ѷ���
	private String nfc_uuid;//NFC�����õ�Ψһ���
	private Integer c_type = 1;//-- 0:NFC,1:IBeacon,2:����,3:ͨ��ɨ�� 4ֱ�� 5�¿��û�6:��λ��ά�� 7���¿��û���2..3�����볡 8���ֶ��¿�
	private Long uid = -1L;// -- �볡�շ�Ա�ʺ�
	private String car_number;//���ƺ�
	private String imei;//�ֻ�����
	private Integer pid = -1;//-- �Ʒѷ�ʽ��0��ʱ(0.5/15����)��1���Σ�12Сʱ��10Ԫ,ǰ1/30min����ÿСʱ1Ԫ��
	private Integer car_type = 0;//-- 0��ͨ�ã�1��С����2����
	private Long in_passid = -1L;//-- ����ͨ��id
	private Long out_passid = -1L;//-- ����ͨ��id
	private Integer pre_state = 0;//-- 0:Ĭ��״̬ 1������Ԥ֧���� 2������Ԥ֧���в����շ�Աˢ�� 3��Ԥ֧�����
	private Integer type = 0;//-- ���ͣ�0��ͨ������1����ͨ��3���ػ����� 4���ط��������� 5�����������Ͻ��㶩��
	private Integer need_sync = 0;//-- Ԥ֧��������Ҫͬ��������  0:����Ҫ  1:��Ҫ  2ͬ�����   3�����л��������������ɵ���Ҫ  4:���Ͻ���Ķ���Ҫͬ����ȥ
	private Integer ishd = 0;// -- 0�� 1�ǲ���ʾ
	private Long freereasons = -1L;//-- ���ԭ��   Ĭ��-1 �����
	private Integer isclick = 0;// -- 0ϵͳ���㣬1�ֶ�����
	private Double prepaid = 0d;//Ԥ�����
	private Long prepaid_pay_time;//Ԥ��ʱ��
	private Long berthnumber = -1L;//-- ��λ���
	private Long berthsec_id = -1L;//-- ��λ�α��
	private Long groupid = -1L;//-- �������ű��
	private Long out_uid = -1L;//-- �����շ�Ա
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
	}
	public Long getUin() {
		return uin;
	}
	public void setUin(Long uin) {
		if(uin == null)
			uin = -1L;
		this.uin = uin;
	}
	public Double getTotal() {
		return total;
	}
	public void setTotal(Double total) {
		if(total == null)
			total = 0d;
		this.total = total;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Long getEnd_time() {
		return end_time;
	}
	public void setEnd_time(Long end_time) {
		this.end_time = end_time;
	}
	public Integer getAuto_pay() {
		return auto_pay;
	}
	public void setAuto_pay(Integer auto_pay) {
		if(auto_pay == null)
			auto_pay = 0;
		this.auto_pay = auto_pay;
	}
	public Integer getPay_type() {
		return pay_type;
	}
	public void setPay_type(Integer pay_type) {
		if(pay_type == null)
			pay_type = 0;
		this.pay_type = pay_type;
	}
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Integer getC_type() {
		return c_type;
	}
	public void setC_type(Integer c_type) {
		if(c_type == null)
			c_type = 1;
		this.c_type = c_type;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		if(uid == null)
			uid = -1L;
		this.uid = uid;
	}
	public String getCar_number() {
		return car_number;
	}
	public void setCar_number(String car_number) {
		this.car_number = car_number;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		if(pid == null)
			pid = -1;
		this.pid = pid;
	}
	public Integer getCar_type() {
		return car_type;
	}
	public void setCar_type(Integer car_type) {
		if(car_type == null)
			car_type = 0;
		this.car_type = car_type;
	}
	public Long getIn_passid() {
		return in_passid;
	}
	public void setIn_passid(Long in_passid) {
		if(in_passid == null)
			in_passid = -1L;
		this.in_passid = in_passid;
	}
	public Long getOut_passid() {
		return out_passid;
	}
	public void setOut_passid(Long out_passid) {
		if(out_passid == null)
			out_passid = -1L;
		this.out_passid = out_passid;
	}
	public Integer getPre_state() {
		return pre_state;
	}
	public void setPre_state(Integer pre_state) {
		if(pre_state == null)
			pre_state = 0;
		this.pre_state = pre_state;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		if(type == null)
			type = 0;
		this.type = type;
	}
	public Integer getNeed_sync() {
		return need_sync;
	}
	public void setNeed_sync(Integer need_sync) {
		if(need_sync == null)
			need_sync = 0;
		this.need_sync = need_sync;
	}
	public Integer getIshd() {
		return ishd;
	}
	public void setIshd(Integer ishd) {
		if(ishd == null)
			ishd = 0;
		this.ishd = ishd;
	}
	public Long getFreereasons() {
		return freereasons;
	}
	public void setFreereasons(Long freereasons) {
		if(freereasons == null)
			freereasons = -1L;
		this.freereasons = freereasons;
	}
	public Integer getIsclick() {
		return isclick;
	}
	public void setIsclick(Integer isclick) {
		if(isclick == null)
			isclick = 0;
		this.isclick = isclick;
	}
	public Double getPrepaid() {
		return prepaid;
	}
	public void setPrepaid(Double prepaid) {
		if(prepaid == null)
			prepaid = 0d;
		this.prepaid = prepaid;
	}
	public Long getPrepaid_pay_time() {
		return prepaid_pay_time;
	}
	public void setPrepaid_pay_time(Long prepaid_pay_time) {
		this.prepaid_pay_time = prepaid_pay_time;
	}
	public Long getBerthnumber() {
		return berthnumber;
	}
	public void setBerthnumber(Long berthnumber) {
		if(berthnumber == null)
			berthnumber = -1L;
		this.berthnumber = berthnumber;
	}
	public Long getBerthsec_id() {
		return berthsec_id;
	}
	public void setBerthsec_id(Long berthsec_id) {
		if(berthsec_id == null)
			berthsec_id = -1L;
		this.berthsec_id = berthsec_id;
	}
	public Long getGroupid() {
		return groupid;
	}
	public void setGroupid(Long groupid) {
		if(groupid == null)
			groupid = -1L;
		this.groupid = groupid;
	}
	public Long getOut_uid() {
		return out_uid;
	}
	public void setOut_uid(Long out_uid) {
		if(out_uid == null)
			out_uid = -1L;
		this.out_uid = out_uid;
	}
	@Override
	public String toString() {
		return "Order [id=" + id + ", comid=" + comid + ", uin=" + uin
				+ ", total=" + total + ", state=" + state + ", create_time="
				+ create_time + ", end_time=" + end_time + ", auto_pay="
				+ auto_pay + ", pay_type=" + pay_type + ", nfc_uuid="
				+ nfc_uuid + ", c_type=" + c_type + ", uid=" + uid
				+ ", car_number=" + car_number + ", imei=" + imei + ", pid="
				+ pid + ", car_type=" + car_type + ", in_passid=" + in_passid
				+ ", out_passid=" + out_passid + ", pre_state=" + pre_state
				+ ", type=" + type + ", need_sync=" + need_sync + ", ishd="
				+ ishd + ", freereasons=" + freereasons + ", isclick="
				+ isclick + ", prepaid=" + prepaid + ", prepaid_pay_time="
				+ prepaid_pay_time + ", berthnumber=" + berthnumber
				+ ", berthsec_id=" + berthsec_id + ", groupid=" + groupid
				+ ", out_uid=" + out_uid + "]";
	}
	
}
