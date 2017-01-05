package com.zld.pojo;

import java.io.Serializable;

public class Card implements Serializable {
	private Long id = -1L;
	private String nfc_uuid;//��Ƭ���õ�Ψһ���
	private Long comid = -1L;//���п�Ƭ�ĳ������
	private Long create_time;//����ʱ��
	private Integer state = 0;//-- 0���1������2�˿�
	private Integer use_times;//ʹ�ô���
	private Long uin = -1L;//��Ƭ���������˻�
	private Long uid = -1L;//���������˱��
	private Long update_time;//��Ƭ��Ϣ����ʱ��
	private Long nid = 0L;//ɨ��NFC�Ķ�ά���
	private String qrcode;//��Ƭ��ά��
	private Integer type;//��Ƭ����0��NFC  1�����ӱ�ǩ
	private String card_name;//��Ƭ����
	private String device;//�����豸
	private Integer is_delete = 0;//0������ 1����ɾ��
	private Double balance = 0d;//���
	private String card_number;//����ţ�ӡ�ڿ����ϵı�ţ�
	private Long tenant_id = -1L;//�����̻����
	private Long group_id = -1L;//��Ӫ���ű��
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Long getComid() {
		return comid;
	}
	public void setComid(Long comid) {
		if(comid == null)
			comid = -1L;
		this.comid = comid;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		if(state == null)
			state = 0;
		this.state = state;
	}
	public Integer getUse_times() {
		return use_times;
	}
	public void setUse_times(Integer use_times) {
		this.use_times = use_times;
	}
	public Long getUin() {
		return uin;
	}
	public void setUin(Long uin) {
		if(uin == null)
			uin = -1L;
		this.uin = uin;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		if(uid == null)
			uid = -1L;
		this.uid = uid;
	}
	public Long getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(Long update_time) {
		this.update_time = update_time;
	}
	public Long getNid() {
		return nid;
	}
	public void setNid(Long nid) {
		if(nid == null)
			nid = 0L;
		this.nid = nid;
	}
	public String getQrcode() {
		return qrcode;
	}
	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getCard_name() {
		return card_name;
	}
	public void setCard_name(String card_name) {
		this.card_name = card_name;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public Integer getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(Integer is_delete) {
		this.is_delete = is_delete;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		if(balance == null)
			balance = 0d;
		this.balance = balance;
	}
	public String getCard_number() {
		return card_number;
	}
	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}
	public Long getTenant_id() {
		return tenant_id;
	}
	public void setTenant_id(Long tenant_id) {
		if(tenant_id == null)
			tenant_id = -1L;
		this.tenant_id = tenant_id;
	}
	public Long getGroup_id() {
		return group_id;
	}
	public void setGroup_id(Long group_id) {
		if(group_id == null)
			group_id = -1L;
		this.group_id = group_id;
	}
	@Override
	public String toString() {
		return "Card [id=" + id + ", nfc_uuid=" + nfc_uuid + ", comid=" + comid
				+ ", create_time=" + create_time + ", state=" + state
				+ ", use_times=" + use_times + ", uin=" + uin + ", uid=" + uid
				+ ", update_time=" + update_time + ", nid=" + nid + ", qrcode="
				+ qrcode + ", type=" + type + ", card_name=" + card_name
				+ ", device=" + device + ", is_delete=" + is_delete
				+ ", balance=" + balance + ", card_number=" + card_number
				+ ", tenant_id=" + tenant_id + ", group_id=" + group_id + "]";
	}
	
}
