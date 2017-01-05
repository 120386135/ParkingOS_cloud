package com.zld.pojo;

import java.io.Serializable;
import java.util.List;

import com.zld.utils.SqlInfo;

public class AccountReq implements Serializable {
	private long id;
	private long startTime = -1;//��ѯ��ʼʱ��
	private long endTime = -1;//��ѯ����ʱ��
	private int type;//0�����շ�Ա���ͳ�� 1�����������ͳ�� 2������λ�α�Ų�ѯ 3������λ��ѯ 4:����Ӫ���Ų�ѯ
	private int pageNum = -1;
	private int pageSize = -1;
	private SqlInfo sqlInfo;
	
	public int getPageNum() {
		return pageNum;
	}
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public SqlInfo getSqlInfo() {
		return sqlInfo;
	}
	public void setSqlInfo(SqlInfo sqlInfo) {
		this.sqlInfo = sqlInfo;
	}
	@Override
	public String toString() {
		return "AccountReq [id=" + id + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", type=" + type + ", pageNum="
				+ pageNum + ", pageSize=" + pageSize + ", sqlInfo=" + sqlInfo
				+ "]";
	}
}
