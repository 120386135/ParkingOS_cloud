package com.zld.pojo;

import java.io.Serializable;

public class DefaultCardReq implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long userId = -1L;//�������
	private String carNumber;//���ƺ�
	private Long parkId = -1L;//ͣ�������
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getParkId() {
		return parkId;
	}
	public void setParkId(Long parkId) {
		this.parkId = parkId;
	}
	public String getCarNumber() {
		return carNumber;
	}
	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}
	@Override
	public String toString() {
		return "DefaultCardReq [userId=" + userId + ", carNumber=" + carNumber
				+ ", parkId=" + parkId + "]";
	}
	
}
