package com.zld.pojo;

import java.io.Serializable;

public class CardAccount implements Serializable {
	private Long id = -1L;
	private Long card_id = -1L;// -- ��Ƭ���
	private Integer type = 0;// -- 0����ֵ 1������
	private Integer charge_type = 0;//��ֵ��ʽ��0���ֽ��ֵ 1��΢�Ź��ںų�ֵ 2��΢�ſͻ��˳�ֵ 3��֧������ֵ 4��Ԥ֧���˿� 5�������˿�
	private Integer consume_type = 0;//���ѷ�ʽ 0��֧��ͣ���ѣ���Ԥ���� 1��Ԥ��ͣ���� 2������ͣ����
	private Double amount = 0d;//���
	private Long orderid = -1L;//�������
	private Long create_time;//��¼ʱ��
	private String remark;//˵��
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCard_id() {
		return card_id;
	}
	public void setCard_id(Long card_id) {
		this.card_id = card_id;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getCharge_type() {
		return charge_type;
	}
	public void setCharge_type(Integer charge_type) {
		if(charge_type == null)
			charge_type = 0;
		this.charge_type = charge_type;
	}
	public Integer getConsume_type() {
		return consume_type;
	}
	public void setConsume_type(Integer consume_type) {
		if(consume_type == null)
			consume_type = 0;
		this.consume_type = consume_type;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		if(amount == null)
			amount = 0d;
		this.amount = amount;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		if(orderid == null)
			orderid = -1L;
		this.orderid = orderid;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
