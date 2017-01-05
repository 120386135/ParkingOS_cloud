package com.zld.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.zld.service.PgOnlyReadService;

/**
 * �����̲߳�ѯ�б�
 * @author whx
 */
public class QueryList implements Callable<List> {
	private String sql;
	private ArrayList<Object> params;
	private int pageNum;
	private int pageSize;
	private PgOnlyReadService readService;
	public QueryList(PgOnlyReadService readService, String sql,
			ArrayList<Object> params, int pageNum, int pageSize){
		this.sql = sql;
		//������Ҫ�õ����������򵥵ĸ�ֵֻ�����õ�ͬһ�����󣬶�getAll��ı�params��ֵ
		this.params = (ArrayList<Object>) params.clone();
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.readService = readService;
	}
	@Override
	public List call() throws Exception {
		List<Map<String, Object>> result = null;
		try {
			result = readService.getAll(sql, params, pageNum, pageSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
