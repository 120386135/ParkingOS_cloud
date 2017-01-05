package com.zld.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
import com.mongodb.WriteResult;
import com.zld.utils.StringUtils;

/**
 * ����MongoDb
 * @author Laoyao
 * @date 20131025
 */
@Service
public class MongoDbUtils {
	
	private Logger logger = Logger.getLogger(MongoDbUtils.class);
	
	public List<String> getParkPicUrls(Long uin,String dbName){
		List<String> result =new ArrayList<String>();
		DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = db.getCollection("parkuser_pics");
		DBCursor dbCursor = mdb.find(new BasicDBObject("uin", uin), new BasicDBObject("filename", true));
		while(dbCursor.hasNext()){
			DBObject dbObject = dbCursor.next();
			result.add(dbObject.get("filename")+"");
		}
		return result;
	}
	
	public List<String> getOrderPicUrls(String dbName,BasicDBObject condition){
		List<String> result =new ArrayList<String>();
		DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = db.getCollection(dbName);
		DBCursor dbCursor = mdb.find(condition);
		while(dbCursor.hasNext()){
			DBObject dbObject = dbCursor.next();
			result.add(dbObject.get("filename")+"");
		}
		return result;
	}

	public byte[] getParkPic(String id,String dbName){
		DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection collection = db.getCollection(dbName);
		BasicDBObject document = new BasicDBObject();
		document.put("filename", id);
		//document.put("uin", uin);
		DBObject obj = collection.findOne(document);
		if(obj == null){
			db = MongoDBFactory.getInstance().getMongoDBBuilder("zld");//
			collection = db.getCollection(dbName);
			document = new BasicDBObject();
			document.put("filename", id);
			//document.put("uin", uin);
			obj = collection.findOne(document);
		}
		if(obj == null){
			return null;
		}
		db.requestDone();
		return (byte[])obj.get("content");
	}
	
	/**
	 * add 20160106 by yao
	 * @param comId ������� 0ͣ����������Ϊ����
	 * @param uin
	 * @param itype 0������־
	 * @param otype 0��¼��1�˳���2��ӣ�3�޸ģ�4ɾ�� 5���� 6����
	 * @param uri
	 * @param content
	 * @param time
	 * @param ip
	 * @return
	 */
	public String saveLogs(HttpServletRequest request, Integer itype,Integer otype,String content){
		try {
			String ip = StringUtils.getIpAddr(request);
			Long uin = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)request.getSession().getAttribute("comid");
			Long cityid = (Long)request.getSession().getAttribute("cityid");
			Long groupid = (Long)request.getSession().getAttribute("groupid");
			
			DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
			mydb.requestStart();
			
			DBCollection collection = mydb.getCollection("zld_logs");
			//  DBCollection collection = mydb.getCollection("records_test");
			BasicDBObject object = new BasicDBObject();
			object.put("comId", comid);
			object.put("cityid", cityid);
			object.put("groupid", groupid);
			object.put("uin", uin);
			object.put("itype",  itype);
			object.put("otype",  otype);
			object.put("uri", request.getServletPath());
			object.put("content", content);
			object.put("time", System.currentTimeMillis()/1000);
			object.put("ip", ip);
			//��ʼ����
			mydb.requestStart();
			WriteResult result = collection.insert(object);
			//��������
			mydb.requestDone();
			return result.toString();
		} catch (Exception e) {
			logger.error("saveLogs", e);
		}
		return "";
	}
	/**
	 * ��ѯ��¼��
	 * @param dbName
	 * @param conditions
	 * @return
	 */
	public Long queryMongoDbCount(String dbName,BasicDBObject conditions){
		DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = mydb.getCollection(dbName);
		Long count =0L;
		mydb.requestStart();
		if(conditions!=null&&!conditions.isEmpty()){
			count=mdb.count(conditions);
		}else {
			count=mdb.count();
		}
		mydb.requestDone();
		return count;
	}
	/**
	 * ��ҳ��ѯ
	 * @param dbName
	 * @param conditions
	 * @param sort
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public List<Map<String, Object>> queryMongoDbResult(String dbName,BasicDBObject conditions,BasicDBObject sort,int pageNum,int pageSize){
		DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = mydb.getCollection(dbName);
		DBCursor dbCursor =null;
		mydb.requestStart();
		if(pageSize==0){//����ҳ
			if(conditions!=null&&!conditions.isEmpty()){
				dbCursor = mdb.find(conditions).sort(sort);  ;
			}else {
				dbCursor = mdb.find().sort(sort);  ;
			}
		}else {
			if(conditions!=null&&!conditions.isEmpty()){
				dbCursor = mdb.find(conditions).skip((pageNum - 1) * 10).sort(new BasicDBObject()).limit(pageSize).sort(sort);  ;
			}else {
				dbCursor = mdb.find().skip((pageNum - 1) * 10).sort(new BasicDBObject()).limit(pageSize).sort(sort);  ;
			}
		}
		mydb.requestDone();
		List<Map<String, Object>> retMaps = new ArrayList<Map<String, Object>>();  
		while(dbCursor.hasNext()){
			Map<String, Object> map = parseRet(dbCursor.next());  
        	retMaps.add(map);
		}
		return retMaps;
	}
	/**
	 * ��װ��Map
	 * @param dbObject
	 * @return
	 */
	 private Map<String, Object> parseRet(DBObject dbObject){
	    Map<String, Object> retMap= new HashMap<String, Object>();
	    for(String key : dbObject.keySet()){
	    	retMap.put(key, dbObject.get(key));
	    }
	    return retMap;
	}
	
	/** 
     * ��ҳʾ��
     * @param page 
     * @param pageSize 
     * @return 
     */  
    public List<Map<String, Object>> pageList(int page,int pageSize){  
    	DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
		DBCollection mdb = db.getCollection("parkuser_pics");
          
        DBCursor limit = mdb.find().skip((page - 1) * 10).sort(new BasicDBObject()).limit(pageSize);    
        List<Map<String, Object>> retMaps = new ArrayList<Map<String, Object>>();  
        while (limit.hasNext()) {  
        	Map<String, Object> map = parseRet(limit.next());  
        	retMaps.add(map);  
        }  
        return retMaps;  
    }  
    
 
    
    //��ѯʾ��
    public void query() {
        //��ѯ����
        //queryAll();
    	DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
    	DBCollection users = db.getCollection("parkuser_pics");
        //��ѯid = 4de73f7acd812d61b4626a77
        print("find id = 4de73f7acd812d61b4626a77: " + users.find(new BasicDBObject("_id", new ObjectId("4de73f7acd812d61b4626a77"))).toArray());
        
        //��ѯage = 24
        print("find age = 24: " + users.find(new BasicDBObject("age", 24)).toArray());
        
        //��ѯage >= 24
        print("find age >= 24: " + users.find(new BasicDBObject("age", new BasicDBObject("$gte", 24))).toArray());
        print("find age <= 24: " + users.find(new BasicDBObject("age", new BasicDBObject("$lte", 24))).toArray());
        
        print("��ѯage!=25��" + users.find(new BasicDBObject("age", new BasicDBObject("$ne", 25))).toArray());
        print("��ѯage in 25/26/27��" + users.find(new BasicDBObject("age", new BasicDBObject(QueryOperators.IN, new int[] { 25, 26, 27 }))).toArray());
        print("��ѯage not in 25/26/27��" + users.find(new BasicDBObject("age", new BasicDBObject(QueryOperators.NIN, new int[] { 25, 26, 27 }))).toArray());
        print("��ѯage exists ����" + users.find(new BasicDBObject("age", new BasicDBObject(QueryOperators.EXISTS, true))).toArray());
        
        print("ֻ��ѯage���ԣ�" + users.find(null, new BasicDBObject("age", true)).toArray());
        print("ֻ�����ԣ�" + users.find(null, new BasicDBObject("age", true), 0, 2).toArray());
        print("ֻ�����ԣ�" + users.find(null, new BasicDBObject("age", true), 0, 2, Bytes.QUERYOPTION_NOTIMEOUT).toArray());
        
        //ֻ��ѯһ�����ݣ�����ȥ��һ��
        print("findOne: " + users.findOne());
        print("findOne: " + users.findOne(new BasicDBObject("age", 26)));
        print("findOne: " + users.findOne(new BasicDBObject("age", 26), new BasicDBObject("name", true)));
        
        //��ѯ�޸ġ�ɾ��
        print("findAndRemove ��ѯage=25�����ݣ�����ɾ��: " + users.findAndRemove(new BasicDBObject("age", 25)));
        
        //��ѯage=26�����ݣ������޸�name��ֵΪAbc
        print("findAndModify: " + users.findAndModify(new BasicDBObject("age", 26), new BasicDBObject("name", "Abc")));
        print("findAndModify: " + users.findAndModify(
            new BasicDBObject("age", 28), //��ѯage=28������
            new BasicDBObject("name", true), //��ѯname����
            new BasicDBObject("age", true), //����age����
            false, //�Ƿ�ɾ����true��ʾɾ��
            new BasicDBObject("name", "Abc"), //�޸ĵ�ֵ����name�޸ĳ�Abc
            true, 
            true));
        
        DBCursor cur = users.find();
        while (cur.hasNext()) {
            print(cur.next());
        }
    }
    //��ӡ
    private void print (Object value){
    	System.out.println(value.toString());
    }
}
