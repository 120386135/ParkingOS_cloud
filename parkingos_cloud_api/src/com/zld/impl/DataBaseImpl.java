package com.zld.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.zld.dao.DataBaseDao;
import com.zld.utils.StringUtils;


@Repository
public class DataBaseImpl extends JdbcTemplate implements DataBaseDao {
	@Autowired
	@Override
	public void setDataSource(@Qualifier("dataSource")DataSource dataSource) {
		// TODO Auto-generated method stub
		super.setDataSource(dataSource);
	}
	public int update(String sql, Object[] values) {
		int r = super.update(sql, values);
		if(sql.trim().substring(0, 6).toUpperCase().equals("INSERT") 
				&& r == 0){//�������������ʱ����Ϊ�������ֱ�������Ӱ������Ϊ0
			r = 1;
		}
		return r;
	}

	
	public int bathInsert(String sql, List<Object[]> lists, int[] columnTypes) {
		final List<Object[]> valus = lists;
	 	final int [] argTypes = columnTypes;
	 	Long t1 = System.currentTimeMillis();
		BatchPreparedStatementSetter bpss= new BatchPreparedStatementSetter(){
		      public void setValues(PreparedStatement ps, int i) throws SQLException {
		    	   Object[] obj = valus.get(i);
		  	       try{
		  	    	   for(int j = 0;j<obj.length;j++){
		  	    		   if(obj[j]==null||obj[j].toString().equals("null")||obj[j].toString().equals("")){
		  	    			   ps.setNull(j+1, argTypes[j]);
		  	    			   continue;
		  	    		   }
		  	    		   if(argTypes[j]==4){
		  	    			   ps.setLong(j+1,Long.parseLong(obj[j].toString()));
		  	    		   }else if(argTypes[j]==91){
		  	    			   ps.setDate(j+1, (Date)obj[j]);
		  	    		   }else if(argTypes[j]==3){
		  	    			   ps.setDouble(j+1, (Double)obj[j]);
		  	    		   } else {
		  	    			   ps.setString(j+1,(String)obj[j]);
		  	    		   }
		  	    	   }
		  	       } catch(Exception e){
		  	    	   e.printStackTrace();
		  	       }
		      }
		  	  public int getBatchSize(){
		  	       return valus.size();
		  	  }
	 	};
	 	
		int reslut[] = batchUpdate(sql,bpss);
		Long t2 = System.currentTimeMillis();
		if(t2-t1>3000)
			logger.error("query too long ,sql :"+sql+",params:"+lists+" ,time:"+(t2-t1));
		
		return reslut.length;
	}

	public Long getLong(String sql, Object[] values) {
		return queryForLong(sql,values);
	}

	public List getAll(String sql, Object[] values) {
		Long t1 = System.currentTimeMillis();
		List list =  queryForList(sql,values);
		//System.out.println(sql+" ,params:"+StringUtils.objArry2String(values));
		Long t2 = System.currentTimeMillis();
		if(t2-t1>3000)
			logger.error("query too long ,sql :"+sql+",params:"+StringUtils.objArry2String(values)+" ,time:"+(t2-t1));
		return list;
	}

	public Object getObject(String sql, Object[] values, Class type) {
		return queryForObject(sql, values,type);
	}
	
	public Map getPojo(String sql,Object[] values){
		Long t1 = System.currentTimeMillis();
		List<Map> list = queryForList(sql,values);
		Long t2 = System.currentTimeMillis();
		if(t2-t1>3000)
			logger.error("query too long ,sql :"+sql+",params:"+StringUtils.objArry2String(values)+" ,time:"+(t2-t1));
		if(list!=null&&list.size()>0)
			return list.get(0);
		return null;
	}
	@Override
	public <T> List<T> getPOJOList(String sql, Object[] values, Class<T> type) {
		Long t1 = System.currentTimeMillis();
		RowMapper rm = ParameterizedBeanPropertyRowMapper.newInstance(type);
		List<T> list =  query(sql, values, rm);
		Long t2 = System.currentTimeMillis();
		if(t2-t1>3000)
			logger.error("query too long ,sql :"+sql+",params:"+StringUtils.objArry2String(values)+" ,time:"+(t2-t1));
		return list;
	}
	@Override
	public <T> T getPOJO(String sql, Object[] values, Class<T> type) {
		try {
			RowMapper rm = ParameterizedBeanPropertyRowMapper.newInstance(type);
			return (T)queryForObject(sql, values, rm);
		} catch (EmptyResultDataAccessException e) {//û�в鵽����ʱ�׳�EmptyResultDataAccessException�쳣
			return null;
		}
	}

}
