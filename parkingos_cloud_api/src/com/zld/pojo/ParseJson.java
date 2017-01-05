package com.zld.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class ParseJson {

	
	public static List<Map<String, Object>> jsonToList(String json){
		JSONArray array=null;
		List<Map<String, Object>> busList = new ArrayList<Map<String,Object>>();
		try {
			array = new JSONArray(json);
			for(int i=0;i<array.length();i++){
				Map<String, Object>	 retMap = jsonObj2Map(array.getJSONObject(i));
				busList.add(retMap);
			}
			return busList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String,Object> jsonToMap(String json){
		try {
			JSONObject jobj = new JSONObject(json);
			Map<String, Object>	 retMap = jsonObj2Map(jobj);
			return retMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String,Object> jsonObj2Map(JSONObject jobj){
		Map<String,Object> map = new HashMap<String, Object>();
		for (Iterator<String> iter = jobj.keys(); iter.hasNext();) { 
		       String key = (String)iter.next();
		        try {
		        	Object value = jobj.get(key);
		        	key = key.substring(0,1).toLowerCase()+key.substring(1);
					if (value instanceof JSONObject) {
						Map<String,Object> map2 = jsonObj2Map((JSONObject)value);
						map.put(key, map2);
					}else if(value instanceof JSONArray){
						JSONArray value2 = (JSONArray)value;
						List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
						for(int i=0;i<value2.length();i++){
							Map<String,Object> map3 = jsonObj2Map(value2.getJSONObject(i));
							list.add(map3);
						}
						map.put(key, list);
					}else {
						map.put(key, value);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			 }
		return map;
	}
	
	public static List<Map<String, Object>> jsonToList1(String json){
		JSONArray array=null;
		List<Map<String, Object>> busList = new ArrayList<Map<String,Object>>();
		try {
			array = new JSONArray(json);
			for(int i=0;i<array.length();i++){
				Map<String, Object>	 retMap = jsonObj2Map1(array.getJSONObject(i));
				busList.add(retMap);
			}
			return busList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String,Object> jsonToMap1(String json){
		try {
			JSONObject jobj = new JSONObject(json);
			Map<String, Object>	 retMap = jsonObj2Map1(jobj);
			return retMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String,Object> jsonObj2Map1(JSONObject jobj){
		Map<String,Object> map = new HashMap<String, Object>();
		for (Iterator<String> iter = jobj.keys(); iter.hasNext();) { 
		       String key = (String)iter.next();
		        try {
		        	Object value = jobj.get(key);
		        	//key = key.substring(0,1).toLowerCase()+key.substring(1);
					if (value instanceof JSONObject) {
						Map<String,Object> map2 = jsonObj2Map1((JSONObject)value);
						map.put(key, map2);
					}else if(value instanceof JSONArray){
						JSONArray value2 = (JSONArray)value;
						List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
						for(int i=0;i<value2.length();i++){
							Map<String,Object> map3 = jsonObj2Map1(value2.getJSONObject(i));
							list.add(map3);
						}
						map.put(key, list);
					}else {
						map.put(key, value);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			 }
		return map;
	}
	
	public static void main(String[] args) {
		String listStr = "[{\"IsNewData\":\"0\",\"RouteID\":110022,\"RouteName\":\"1002·�ڻ�\",\"RouteType\":\"2\",\"SegmentList\":[{\"SegmentID\":100210,\"SegmentName\":\"������Է\",\"FirstTime\":\"2016-01-16 06:30:00\",\"LastTime\":\"2016-01-16 18:30:00\",\"RoutePrice\":\"0\",\"NormalTimeSpan\":0,\"PeakTimeSpan\":0,\"StationList\":[{\"StationID\":\"107552\",\"StationName\":\"������Է\",\"StationNO\":\"107552\",\"StationPostion\":{\"Longitude\":119.38729,\"Latitude\":32.40852},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"111101\",\"StationName\":\"÷Է˫��ѧУ\",\"StationNO\":\"111101\",\"StationPostion\":{\"Longitude\":119.3854,\"Latitude\":32.41248},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"116592\",\"StationName\":\"���ù������������㻨԰����\",\"StationNO\":\"116592\",\"StationPostion\":{\"Longitude\":119.37957,\"Latitude\":32.41899},\"Stationmemo\":\"\"},{\"StationID\":\"116582\",\"StationName\":\"�����Է��\",\"StationNO\":\"116582\",\"StationPostion\":{\"Longitude\":119.38044,\"Latitude\":32.4225},\"Stationmemo\":\"\"},{\"StationID\":\"116572\",\"StationName\":\"ά����ѧ\",\"StationNO\":\"116572\",\"StationPostion\":{\"Longitude\":119.38095,\"Latitude\":32.42489},\"Stationmemo\":\"\"},{\"StationID\":\"104292\",\"StationName\":\"����ӡˢ��\",\"StationNO\":\"104292\",\"StationPostion\":{\"Longitude\":119.38587,\"Latitude\":32.42519},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"102142\",\"StationName\":\"��ׯ��������Ժ��\",\"StationNO\":\"102142\",\"StationPostion\":{\"Longitude\":119.39063,\"Latitude\":32.42449},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"116563\",\"StationName\":\"ɽˮ���Ƕ���\",\"StationNO\":\"116563\",\"StationPostion\":{\"Longitude\":119.39228,\"Latitude\":32.4235},\"Stationmemo\":\"\"},{\"StationID\":\"116552\",\"StationName\":\"����ӡ�󻪸�\",\"StationNO\":\"116552\",\"StationPostion\":{\"Longitude\":119.39298,\"Latitude\":32.42217},\"Stationmemo\":\"\"},{\"StationID\":\"107572\",\"StationName\":\"����������\",\"StationNO\":\"107572\",\"StationPostion\":{\"Longitude\":119.39717,\"Latitude\":32.42488},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"107862\",\"StationName\":\"�¶�����ѧ\",\"StationNO\":\"107862\",\"StationPostion\":{\"Longitude\":119.40234,\"Latitude\":32.42593},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"107563\",\"StationName\":\"������\",\"StationNO\":\"107563\",\"StationPostion\":{\"Longitude\":119.4051,\"Latitude\":32.42496},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"103593\",\"StationName\":\"����·\",\"StationNO\":\"103593\",\"StationPostion\":{\"Longitude\":119.40678,\"Latitude\":32.42186},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"106333\",\"StationName\":\"�������\",\"StationNO\":\"106333\",\"StationPostion\":{\"Longitude\":119.41055,\"Latitude\":32.41498},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"115284\",\"StationName\":\"����������\",\"StationNO\":\"115284\",\"StationPostion\":{\"Longitude\":119.41067,\"Latitude\":32.41152},\"Stationmemo\":\"\"},{\"StationID\":\"115274\",\"StationName\":\"����Է����\",\"StationNO\":\"115274\",\"StationPostion\":{\"Longitude\":119.40255,\"Latitude\":32.40852},\"Stationmemo\":\"\"},{\"StationID\":\"116524\",\"StationName\":\"������Է\",\"StationNO\":\"116524\",\"StationPostion\":{\"Longitude\":119.39905,\"Latitude\":32.40699},\"Stationmemo\":\"\"},{\"StationID\":\"115264\",\"StationName\":\"��ܰ��԰����\",\"StationNO\":\"115264\",\"StationPostion\":{\"Longitude\":119.39688,\"Latitude\":32.40609},\"Stationmemo\":\"\"},{\"StationID\":\"104891\",\"StationName\":\"ɽ��԰����ܰ��԰��\",\"StationNO\":\"104891\",\"StationPostion\":{\"Longitude\":119.39473,\"Latitude\":32.40603},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"107544\",\"StationName\":\"������Է\",\"StationNO\":\"107544\",\"StationPostion\":{\"Longitude\":119.39167,\"Latitude\":32.40926},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"107554\",\"StationName\":\"������Է\",\"StationNO\":\"107554\",\"StationPostion\":{\"Longitude\":119.38687,\"Latitude\":32.40858},\"Stationmemo\":\"����վ̨\"}],\"FirtLastShiftInfo\":\"��ĩ�ࣺ06:30--18:30\",\"FirtLastShiftInfo2\":null,\"Memos\":null}],\"TimeStamp\":\"2016-03-12 00:12:39\",\"RouteMemo\":null}]";
		String objStr = "{\"IsNewData\":\"0\",\"RouteID\":110022,\"RouteName\":\"1002·�ڻ�\",\"RouteType\":\"2\",\"SegmentList\":[{\"SegmentID\":100210,\"SegmentName\":\"������Է\",\"FirstTime\":\"2016-01-16 06:30:00\",\"LastTime\":\"2016-01-16 18:30:00\",\"RoutePrice\":\"0\",\"NormalTimeSpan\":0,\"PeakTimeSpan\":0,\"StationList\":[{\"StationID\":\"107552\",\"StationName\":\"������Է\",\"StationNO\":\"107552\",\"StationPostion\":{\"Longitude\":119.38729,\"Latitude\":32.40852},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"111101\",\"StationName\":\"÷Է˫��ѧУ\",\"StationNO\":\"111101\",\"StationPostion\":{\"Longitude\":119.3854,\"Latitude\":32.41248},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"116592\",\"StationName\":\"���ù������������㻨԰����\",\"StationNO\":\"116592\",\"StationPostion\":{\"Longitude\":119.37957,\"Latitude\":32.41899},\"Stationmemo\":\"\"},{\"StationID\":\"116582\",\"StationName\":\"�����Է��\",\"StationNO\":\"116582\",\"StationPostion\":{\"Longitude\":119.38044,\"Latitude\":32.4225},\"Stationmemo\":\"\"},{\"StationID\":\"116572\",\"StationName\":\"ά����ѧ\",\"StationNO\":\"116572\",\"StationPostion\":{\"Longitude\":119.38095,\"Latitude\":32.42489},\"Stationmemo\":\"\"},{\"StationID\":\"104292\",\"StationName\":\"����ӡˢ��\",\"StationNO\":\"104292\",\"StationPostion\":{\"Longitude\":119.38587,\"Latitude\":32.42519},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"102142\",\"StationName\":\"��ׯ��������Ժ��\",\"StationNO\":\"102142\",\"StationPostion\":{\"Longitude\":119.39063,\"Latitude\":32.42449},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"116563\",\"StationName\":\"ɽˮ���Ƕ���\",\"StationNO\":\"116563\",\"StationPostion\":{\"Longitude\":119.39228,\"Latitude\":32.4235},\"Stationmemo\":\"\"},{\"StationID\":\"116552\",\"StationName\":\"����ӡ�󻪸�\",\"StationNO\":\"116552\",\"StationPostion\":{\"Longitude\":119.39298,\"Latitude\":32.42217},\"Stationmemo\":\"\"},{\"StationID\":\"107572\",\"StationName\":\"����������\",\"StationNO\":\"107572\",\"StationPostion\":{\"Longitude\":119.39717,\"Latitude\":32.42488},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"107862\",\"StationName\":\"�¶�����ѧ\",\"StationNO\":\"107862\",\"StationPostion\":{\"Longitude\":119.40234,\"Latitude\":32.42593},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"107563\",\"StationName\":\"������\",\"StationNO\":\"107563\",\"StationPostion\":{\"Longitude\":119.4051,\"Latitude\":32.42496},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"103593\",\"StationName\":\"����·\",\"StationNO\":\"103593\",\"StationPostion\":{\"Longitude\":119.40678,\"Latitude\":32.42186},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"106333\",\"StationName\":\"�������\",\"StationNO\":\"106333\",\"StationPostion\":{\"Longitude\":119.41055,\"Latitude\":32.41498},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"115284\",\"StationName\":\"����������\",\"StationNO\":\"115284\",\"StationPostion\":{\"Longitude\":119.41067,\"Latitude\":32.41152},\"Stationmemo\":\"\"},{\"StationID\":\"115274\",\"StationName\":\"����Է����\",\"StationNO\":\"115274\",\"StationPostion\":{\"Longitude\":119.40255,\"Latitude\":32.40852},\"Stationmemo\":\"\"},{\"StationID\":\"116524\",\"StationName\":\"������Է\",\"StationNO\":\"116524\",\"StationPostion\":{\"Longitude\":119.39905,\"Latitude\":32.40699},\"Stationmemo\":\"\"},{\"StationID\":\"115264\",\"StationName\":\"��ܰ��԰����\",\"StationNO\":\"115264\",\"StationPostion\":{\"Longitude\":119.39688,\"Latitude\":32.40609},\"Stationmemo\":\"\"},{\"StationID\":\"104891\",\"StationName\":\"ɽ��԰����ܰ��԰��\",\"StationNO\":\"104891\",\"StationPostion\":{\"Longitude\":119.39473,\"Latitude\":32.40603},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"107544\",\"StationName\":\"������Է\",\"StationNO\":\"107544\",\"StationPostion\":{\"Longitude\":119.39167,\"Latitude\":32.40926},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"107554\",\"StationName\":\"������Է\",\"StationNO\":\"107554\",\"StationPostion\":{\"Longitude\":119.38687,\"Latitude\":32.40858},\"Stationmemo\":\"����վ̨\"}],\"FirtLastShiftInfo\":\"��ĩ�ࣺ06:30--18:30\",\"FirtLastShiftInfo2\":null,\"Memos\":null}],\"TimeStamp\":\"2016-03-12 00:12:39\",\"RouteMemo\":null}";
		List<Map<String, Object>> object3 =jsonToList(listStr);
		Map<String, Object> obMap = jsonToMap(objStr);
		System.err.println(obMap);
	}
	
	public  static String createJson( List<Map<String, Object>> data) {
		String json = "[";
		int i=0;
		int j=0;
		if(data!=null&&data.size()>0){
			for(Map<String, Object > map : data){
				if(i!=0)
					json +=",";
				json+="{";
				for(String key : map.keySet()){
					if(j!=0)
						json +=",";
					Object v = map.get(key);
					if(v!=null){
						if(v instanceof List){
							v=createJson((List<Map<String, Object>>)v);
							json +="\""+key+"\":"+v+"";
						}else if(v instanceof Map){
							v = createJson((Map<String, Object>)v);
							json +="\""+key+"\":"+v+"";
						}else {
							v = v.toString().trim();
							json +="\""+key+"\":\""+v+"\"";
						}
					}
					j++;
				}
				json+="}";
				i++;
				j=0;
			}
			
		}
		json +="]";
		return json;
	}
	public  static String createJson( Map<String, Object> data) {
		String json = "{";
		int j=0;
		for(String key : data.keySet()){
			if(j!=0)
				json +=",";
			Object v = data.get(key);
			if(v!=null){
				if(v instanceof List){
					v=createJson((List<Map<String, Object>>)v);
					json +="\""+key+"\":"+v+"";
				}else if(v instanceof Map){
					v = createJson((Map<String, Object>)v);
					json +="\""+key+"\":"+v+"";
				}else {
					v = v.toString().trim();
					json +="\""+key+"\":\""+v+"\"";
				}
			}
			j++;
		}
		json +="}";
		return json;
	}
}
