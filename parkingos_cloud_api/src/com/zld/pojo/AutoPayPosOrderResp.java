package com.zld.pojo;

import java.io.Serializable;

public class AutoPayPosOrderResp extends BaseResp implements Serializable {
	private String duration;//ͣ��ʱ��

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "AutoPayResp [duration=" + duration + "]";
	}
	
}
