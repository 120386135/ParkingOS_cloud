package com.zld.weixinpay.utils.util;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtil {

	public static String getJsonValue(String rescontent, String key) {
		JSONObject jsonObject;
		String v = null;
		try {
			jsonObject = new JSONObject(rescontent);
			v = jsonObject.getString(key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return v;
	}
	
	public static String [] parseJson(String result){
		JSONArray jsonArray ;
		try {
			jsonArray = new JSONArray(result);
			System.out.println(jsonArray.toString());
			System.out.println(jsonArray.getJSONArray(11));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		String value = "[\"1263\",\"�ϵػ�������ͣ����\",\"116.318700\",\"40.034339\",\"310\",\"1.0Ԫ/30����\",\"310\",\"�����б����к�����ũ����·1��Ժ-1��¥\",\"\",\"1\",\"\",[\"parkpics/1263_1409710102.jpeg\",\"parkpics/1263_1409710102.jpeg\"]]";
		String  [] ret = parseJson(value);
	}
}
