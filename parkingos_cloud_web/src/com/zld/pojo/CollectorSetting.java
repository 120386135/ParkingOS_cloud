package com.zld.pojo;

import java.io.Serializable;

public class CollectorSetting implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6355702540911862018L;
	private Long id;
	private Long role_id;
	private String photoset;//�������á�num1,num2,num3���ֱ����볡������Ƭ��������������Ƭ����δ�ɿ�����Ƭ��
	private Integer change_prepay;//�Ƿ�ɸ���Ԥ�ս�� 0���ɣ�1����
	private Integer view_plot;//0�б�1��ʾ��λ
	private String print_sign;//��ӡСƱ��Ϣ���볡��������
	private String prepayset;//Ԥ������ ��num1,num2,num3...��Ԥ�ս��ѡ��
	private Integer isprepay;//0����Ԥ��,1��Ԥ��
	private Integer hidedetail;//1���� 0��������ҳ�շѻ���
	private Integer is_sensortime;//0��ȡ������ʱ����Ϊ¼�붩��ʱ�� 1��ȡ��ǰʱ����Ϊ¼�붩��ʱ��
	private String password;//�鿴���ܵ�Ȩ������
	private String signout_password;//ǩ������
	private Integer signout_valid;//�ͻ���ǩ���Ƿ���Ҫ������֤ 0������Ҫ 1����Ҫ
	private Integer is_show_card;//�Ƿ����շѻ��ܺʹ�ӡСƱ����ʾ������Ƭ�����ݣ���Щ��Ӫ����û�п�Ƭ�� 0����ʾ 1������ʾ
	private Integer print_order_place2;//�������Ҫ�ڵ�����㶩����ʱ��ʹ�ӡСƱ���Ѿ���һ����ӡ����СƱ�ĵط����˴�Ϊ�ڶ����ط���0������ӡ 1����ӡ
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		if(id == null)
			id = -1L;
		this.id = id;
	}

	public Long getRole_id() {
		return role_id;
	}

	public void setRole_id(Long role_id) {
		if(role_id == null)
			role_id = -1L;
		this.role_id = role_id;
	}

	public String getPhotoset() {
		return photoset;
	}

	public void setPhotoset(String photoset) {
		this.photoset = photoset;
	}

	public Integer getChange_prepay() {
		return change_prepay;
	}

	public void setChange_prepay(Integer change_prepay) {
		if(change_prepay == null)
			change_prepay = 0;
		this.change_prepay = change_prepay;
	}

	public Integer getView_plot() {
		return view_plot;
	}

	public void setView_plot(Integer view_plot) {
		if(view_plot == null)
			view_plot = 0;
		this.view_plot = view_plot;
	}

	public String getPrint_sign() {
		return print_sign;
	}

	public void setPrint_sign(String print_sign) {
		this.print_sign = print_sign;
	}

	public String getPrepayset() {
		return prepayset;
	}

	public void setPrepayset(String prepayset) {
		this.prepayset = prepayset;
	}

	public Integer getIsprepay() {
		return isprepay;
	}

	public void setIsprepay(Integer isprepay) {
		if(isprepay == null)
			isprepay = 0;
		this.isprepay = isprepay;
	}

	public Integer getHidedetail() {
		return hidedetail;
	}

	public void setHidedetail(Integer hidedetail) {
		if(hidedetail == null)
			hidedetail = 0;
		this.hidedetail = hidedetail;
	}

	public Integer getIs_sensortime() {
		return is_sensortime;
	}

	public void setIs_sensortime(Integer is_sensortime) {
		if(is_sensortime == null)
			is_sensortime = 0;
		this.is_sensortime = is_sensortime;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSignout_password() {
		return signout_password;
	}

	public void setSignout_password(String signout_password) {
		this.signout_password = signout_password;
	}

	public Integer getSignout_valid() {
		return signout_valid;
	}

	public void setSignout_valid(Integer signout_valid) {
		if(signout_valid == null)
			signout_valid = 0;
		this.signout_valid = signout_valid;
	}

	public Integer getIs_show_card() {
		return is_show_card;
	}

	public void setIs_show_card(Integer is_show_card) {
		if(is_show_card == null)
			is_show_card = 0;
		this.is_show_card = is_show_card;
	}

	public Integer getPrint_order_place2() {
		return print_order_place2;
	}

	public void setPrint_order_place2(Integer print_order_place2) {
		if(print_order_place2 == null)
			print_order_place2 = 0;
		this.print_order_place2 = print_order_place2;
	}

	@Override
	public String toString() {
		return "CollecterSetting [id=" + id + ", role_id=" + role_id
				+ ", photoset=" + photoset + ", change_prepay=" + change_prepay
				+ ", view_plot=" + view_plot + ", print_sign=" + print_sign
				+ ", prepayset=" + prepayset + ", isprepay=" + isprepay
				+ ", hidedetail=" + hidedetail + ", is_sensortime="
				+ is_sensortime + ", password=" + password
				+ ", signout_password=" + signout_password + ", signout_valid="
				+ signout_valid + ", is_show_card=" + is_show_card
				+ ", print_order_place2=" + print_order_place2 + "]";
	}
}
