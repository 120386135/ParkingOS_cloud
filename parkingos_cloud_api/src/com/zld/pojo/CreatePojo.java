package com.zld.pojo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSON תΪPOJO�����򼯺�
 * @author Laoyao
 */
public class CreatePojo {
	//�������������һ�����󼯺ϣ�Ҫ����һ�������·������һ�¡�������ϵ����Ʒ�������淶��
	//public List<Semgent> semgmentList,Semgent���Ǽ����еĶ��󣬼���path�������ɶ����ֶ�Ҫ����
	//path javaBean���ڰ�������βҪ��.,��"com.zld.pojo.";
	public static String POJOPATH="com.zld.pojo.";
	
	public static void main(String[] args) {
		String listStr = "[{\"IsNewData\":\"0\",\"RouteID\":110022,\"RouteName\":\"1002·�ڻ�\",\"RouteType\":\"2\",\"SegmentList\":[{\"SegmentID\":100210,\"SegmentName\":\"������Է\",\"FirstTime\":\"2016-01-16 06:30:00\",\"LastTime\":\"2016-01-16 18:30:00\",\"RoutePrice\":\"0\",\"NormalTimeSpan\":0,\"PeakTimeSpan\":0,\"StationList\":[{\"StationID\":\"107552\",\"StationName\":\"������Է\",\"StationNO\":\"107552\",\"StationPostion\":{\"Longitude\":119.38729,\"Latitude\":32.40852},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"111101\",\"StationName\":\"÷Է˫��ѧУ\",\"StationNO\":\"111101\",\"StationPostion\":{\"Longitude\":119.3854,\"Latitude\":32.41248},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"116592\",\"StationName\":\"���ù������������㻨԰����\",\"StationNO\":\"116592\",\"StationPostion\":{\"Longitude\":119.37957,\"Latitude\":32.41899},\"Stationmemo\":\"\"},{\"StationID\":\"116582\",\"StationName\":\"�����Է��\",\"StationNO\":\"116582\",\"StationPostion\":{\"Longitude\":119.38044,\"Latitude\":32.4225},\"Stationmemo\":\"\"},{\"StationID\":\"116572\",\"StationName\":\"ά����ѧ\",\"StationNO\":\"116572\",\"StationPostion\":{\"Longitude\":119.38095,\"Latitude\":32.42489},\"Stationmemo\":\"\"},{\"StationID\":\"104292\",\"StationName\":\"����ӡˢ��\",\"StationNO\":\"104292\",\"StationPostion\":{\"Longitude\":119.38587,\"Latitude\":32.42519},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"102142\",\"StationName\":\"��ׯ��������Ժ��\",\"StationNO\":\"102142\",\"StationPostion\":{\"Longitude\":119.39063,\"Latitude\":32.42449},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"116563\",\"StationName\":\"ɽˮ���Ƕ���\",\"StationNO\":\"116563\",\"StationPostion\":{\"Longitude\":119.39228,\"Latitude\":32.4235},\"Stationmemo\":\"\"},{\"StationID\":\"116552\",\"StationName\":\"����ӡ�󻪸�\",\"StationNO\":\"116552\",\"StationPostion\":{\"Longitude\":119.39298,\"Latitude\":32.42217},\"Stationmemo\":\"\"},{\"StationID\":\"107572\",\"StationName\":\"����������\",\"StationNO\":\"107572\",\"StationPostion\":{\"Longitude\":119.39717,\"Latitude\":32.42488},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"107862\",\"StationName\":\"�¶�����ѧ\",\"StationNO\":\"107862\",\"StationPostion\":{\"Longitude\":119.40234,\"Latitude\":32.42593},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"107563\",\"StationName\":\"������\",\"StationNO\":\"107563\",\"StationPostion\":{\"Longitude\":119.4051,\"Latitude\":32.42496},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"103593\",\"StationName\":\"����·\",\"StationNO\":\"103593\",\"StationPostion\":{\"Longitude\":119.40678,\"Latitude\":32.42186},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"106333\",\"StationName\":\"�������\",\"StationNO\":\"106333\",\"StationPostion\":{\"Longitude\":119.41055,\"Latitude\":32.41498},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"115284\",\"StationName\":\"����������\",\"StationNO\":\"115284\",\"StationPostion\":{\"Longitude\":119.41067,\"Latitude\":32.41152},\"Stationmemo\":\"\"},{\"StationID\":\"115274\",\"StationName\":\"����Է����\",\"StationNO\":\"115274\",\"StationPostion\":{\"Longitude\":119.40255,\"Latitude\":32.40852},\"Stationmemo\":\"\"},{\"StationID\":\"116524\",\"StationName\":\"������Է\",\"StationNO\":\"116524\",\"StationPostion\":{\"Longitude\":119.39905,\"Latitude\":32.40699},\"Stationmemo\":\"\"},{\"StationID\":\"115264\",\"StationName\":\"��ܰ��԰����\",\"StationNO\":\"115264\",\"StationPostion\":{\"Longitude\":119.39688,\"Latitude\":32.40609},\"Stationmemo\":\"\"},{\"StationID\":\"104891\",\"StationName\":\"ɽ��԰����ܰ��԰��\",\"StationNO\":\"104891\",\"StationPostion\":{\"Longitude\":119.39473,\"Latitude\":32.40603},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"107544\",\"StationName\":\"������Է\",\"StationNO\":\"107544\",\"StationPostion\":{\"Longitude\":119.39167,\"Latitude\":32.40926},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"107554\",\"StationName\":\"������Է\",\"StationNO\":\"107554\",\"StationPostion\":{\"Longitude\":119.38687,\"Latitude\":32.40858},\"Stationmemo\":\"����վ̨\"}],\"FirtLastShiftInfo\":\"��ĩ�ࣺ06:30--18:30\",\"FirtLastShiftInfo2\":null,\"Memos\":null}],\"TimeStamp\":\"2016-03-12 00:12:39\",\"RouteMemo\":null}]";
		String objStr = "{\"IsNewData\":\"0\",\"RouteID\":110022,\"RouteName\":\"1002·�ڻ�\",\"RouteType\":\"2\",\"SegmentList\":[{\"SegmentID\":100210,\"SegmentName\":\"������Է\",\"FirstTime\":\"2016-01-16 06:30:00\",\"LastTime\":\"2016-01-16 18:30:00\",\"RoutePrice\":\"0\",\"NormalTimeSpan\":0,\"PeakTimeSpan\":0,\"StationList\":[{\"StationID\":\"107552\",\"StationName\":\"������Է\",\"StationNO\":\"107552\",\"StationPostion\":{\"Longitude\":119.38729,\"Latitude\":32.40852},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"111101\",\"StationName\":\"÷Է˫��ѧУ\",\"StationNO\":\"111101\",\"StationPostion\":{\"Longitude\":119.3854,\"Latitude\":32.41248},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"116592\",\"StationName\":\"���ù������������㻨԰����\",\"StationNO\":\"116592\",\"StationPostion\":{\"Longitude\":119.37957,\"Latitude\":32.41899},\"Stationmemo\":\"\"},{\"StationID\":\"116582\",\"StationName\":\"�����Է��\",\"StationNO\":\"116582\",\"StationPostion\":{\"Longitude\":119.38044,\"Latitude\":32.4225},\"Stationmemo\":\"\"},{\"StationID\":\"116572\",\"StationName\":\"ά����ѧ\",\"StationNO\":\"116572\",\"StationPostion\":{\"Longitude\":119.38095,\"Latitude\":32.42489},\"Stationmemo\":\"\"},{\"StationID\":\"104292\",\"StationName\":\"����ӡˢ��\",\"StationNO\":\"104292\",\"StationPostion\":{\"Longitude\":119.38587,\"Latitude\":32.42519},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"102142\",\"StationName\":\"��ׯ��������Ժ��\",\"StationNO\":\"102142\",\"StationPostion\":{\"Longitude\":119.39063,\"Latitude\":32.42449},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"116563\",\"StationName\":\"ɽˮ���Ƕ���\",\"StationNO\":\"116563\",\"StationPostion\":{\"Longitude\":119.39228,\"Latitude\":32.4235},\"Stationmemo\":\"\"},{\"StationID\":\"116552\",\"StationName\":\"����ӡ�󻪸�\",\"StationNO\":\"116552\",\"StationPostion\":{\"Longitude\":119.39298,\"Latitude\":32.42217},\"Stationmemo\":\"\"},{\"StationID\":\"107572\",\"StationName\":\"����������\",\"StationNO\":\"107572\",\"StationPostion\":{\"Longitude\":119.39717,\"Latitude\":32.42488},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"107862\",\"StationName\":\"�¶�����ѧ\",\"StationNO\":\"107862\",\"StationPostion\":{\"Longitude\":119.40234,\"Latitude\":32.42593},\"Stationmemo\":\"�ϱ�վ̨\"},{\"StationID\":\"107563\",\"StationName\":\"������\",\"StationNO\":\"107563\",\"StationPostion\":{\"Longitude\":119.4051,\"Latitude\":32.42496},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"103593\",\"StationName\":\"����·\",\"StationNO\":\"103593\",\"StationPostion\":{\"Longitude\":119.40678,\"Latitude\":32.42186},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"106333\",\"StationName\":\"�������\",\"StationNO\":\"106333\",\"StationPostion\":{\"Longitude\":119.41055,\"Latitude\":32.41498},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"115284\",\"StationName\":\"����������\",\"StationNO\":\"115284\",\"StationPostion\":{\"Longitude\":119.41067,\"Latitude\":32.41152},\"Stationmemo\":\"\"},{\"StationID\":\"115274\",\"StationName\":\"����Է����\",\"StationNO\":\"115274\",\"StationPostion\":{\"Longitude\":119.40255,\"Latitude\":32.40852},\"Stationmemo\":\"\"},{\"StationID\":\"116524\",\"StationName\":\"������Է\",\"StationNO\":\"116524\",\"StationPostion\":{\"Longitude\":119.39905,\"Latitude\":32.40699},\"Stationmemo\":\"\"},{\"StationID\":\"115264\",\"StationName\":\"��ܰ��԰����\",\"StationNO\":\"115264\",\"StationPostion\":{\"Longitude\":119.39688,\"Latitude\":32.40609},\"Stationmemo\":\"\"},{\"StationID\":\"104891\",\"StationName\":\"ɽ��԰����ܰ��԰��\",\"StationNO\":\"104891\",\"StationPostion\":{\"Longitude\":119.39473,\"Latitude\":32.40603},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"107544\",\"StationName\":\"������Է\",\"StationNO\":\"107544\",\"StationPostion\":{\"Longitude\":119.39167,\"Latitude\":32.40926},\"Stationmemo\":\"����վ̨\"},{\"StationID\":\"107554\",\"StationName\":\"������Է\",\"StationNO\":\"107554\",\"StationPostion\":{\"Longitude\":119.38687,\"Latitude\":32.40858},\"Stationmemo\":\"����վ̨\"}],\"FirtLastShiftInfo\":\"��ĩ�ࣺ06:30--18:30\",\"FirtLastShiftInfo2\":null,\"Memos\":null}],\"TimeStamp\":\"2016-03-12 00:12:39\",\"RouteMemo\":null}";
		Object object1 = getObjectFromJson(RouteStation.class, objStr);
		List<Object> list = getListObjFromJson(RouteStation.class, listStr);
		Object object2 = CreatePojo.getObjFromJson(RouteStation.class, objStr);
		Object object3 = CreatePojo.getObjFromJson(RouteStation.class, listStr);
	}
	
	/**
	 * @param c ��������
	 * @param json 
	 * @return ��ȷ�����ǵ������� c
	 */
	public static Object getObjectFromJson(Class<?> c,String json){
		try {
			JSONObject jobj = new JSONObject(json);
			Map<String, Object>	 retMap = jsonObj2Map(jobj);
			Object object = createObject(c, retMap);
			return object;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * @param c ��������
	 * @param json 
	 * @return ��ȷ���ض��󼯺� List<c>
	 */
	public static List<Object> getListObjFromJson(Class<?> c,String json){
		JSONArray array=null;
		List<Map<String, Object>> busList = new ArrayList<Map<String,Object>>();
		try {
			array = new JSONArray(json);
			for(int i=0;i<array.length();i++){
				Map<String, Object>	 retMap = jsonObj2Map(array.getJSONObject(i));
				busList.add(retMap);
			}
			List<Object> list = createObjectList(c, busList);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param c ��������
	 * @param json 
	 * @return ����ȷ���ؼ��ϻ��Ƕ���c �� List<c> ,Ҫ�Է��صĶ�������ж�
	 */
	public static Object getObjFromJson(Class<?> c,String json){
		if(json.startsWith("[")){
			JSONArray array=null;
			List<Map<String, Object>> busList = new ArrayList<Map<String,Object>>();
			try {
				array = new JSONArray(json);
				for(int i=0;i<array.length();i++){
					Map<String, Object>	 retMap = jsonObj2Map(array.getJSONObject(i));
					busList.add(retMap);
				}
				List<Object> list =createObjectList(c, busList);
				return list;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else if(json.startsWith("{")){
			try {
				JSONObject jobj = new JSONObject(json);
				Map<String, Object>	 retMap = jsonObj2Map(jobj);
				return createObject(c, retMap);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	private static Map<String,Object> jsonObj2Map(JSONObject jobj){
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
	
	/**
	 * ���ص�������
	 * @param c
	 * @param objMap
	 * @return
	 * @throws Exception
	 */
	private static Object createObject(Class<?> c,Map<String, Object> objMap)
			throws Exception {
		Iterator<String> keys = objMap.keySet().iterator();
		Object t = c.newInstance();
		JXPathContext jxpcontext = JXPathContext.newContext(t);
		while (keys.hasNext()) {
			String key = keys.next();
			try {
				Object value = objMap.get(key);
				if(value==null||value.toString().trim().equals("null"))
					continue;
				if(value instanceof Map ){
					Field field = c.getField(key);
					Class<?> class1 = field.getType();
					Object o = createObject(class1, (Map<String, Object>)value);
					jxpcontext.setValue(key,o);
				}else if(value instanceof List){
					Field field = c.getField(key);
					String name = field.getName();
					name = name.substring(0,1).toUpperCase()+name.substring(1,name.length()-4);
					Object o = createObjectList(Class.forName(POJOPATH+name), (List<Map<String, Object>>)value);
					jxpcontext.setValue(key,o);
				}else {
					jxpcontext.setValue(key, objMap.get(key));
				}
			} catch (Exception e) {
				System.err.println(c.getName()+",error:method=" + key);
			}
		}
		return t;
	}
	
	/**
	 * ���ض��󼯺�
	 * @param c
	 * @param objMapList
	 * @return
	 * @throws Exception
	 */
	private static List<Object> createObjectList(Class<?> c,List<Map<String, Object>> objMapList)
			throws Exception {
		List<Object> list = new ArrayList<Object>();
		for(Map<String, Object> objMap:objMapList){
			list.add(createObject(c, objMap));
		}
		return list;
	}
}
