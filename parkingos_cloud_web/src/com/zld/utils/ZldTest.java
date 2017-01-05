package com.zld.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class ZldTest {
	public static String anlysisPhoneLocal(){
		String filenname="c:\\mobile.txt";
		Reader reader = null;
		BufferedReader bufferedReader = null;
		List<String > reList=new ArrayList<String>();
		String lineString = "";
		String mobiles = "";
		int k = 0;
		try {
			reader = new InputStreamReader(new FileInputStream(filenname));
			bufferedReader = new BufferedReader(reader);
			while ((lineString = bufferedReader.readLine()) != null ) {
				mobiles +=","+lineString;
			}
		}catch (Exception e) {
			
		}finally{
			try {
				reader.close();
				bufferedReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mobiles;
		//http://vip.showji.com/locating/?m=1878952&outfmt=json&callback=phone.callBack
	}
	
	
	private  static String weixin(){
		BufferedReader reader = null;
		String lineString=null;
		String result = "";
		BufferedWriter writer;
		try {
			reader = new BufferedReader(new FileReader(new File("d:/weixin0727_0930.csv")));
			writer = new BufferedWriter(new FileWriter(new File("d:/weixin0727_0930_final.csv"),true));
			int i=0;
			while ((lineString = reader.readLine()) != null) {
				if(i==0){
					i++;
					continue;
				}
				String []info = lineString.split(",");
				Integer t1 =0;
				Integer t2 =0; 
				if(Check.isNumber(info[6])&&Check.isNumber(info[5])){
					t1=Integer.valueOf(info[5]);
					t2=Integer.valueOf(info[6]);
				}
				int index = lineString.indexOf("oRo");
				if(t2<t1){
					result += lineString.substring(0,index)+"����ȯ,"+lineString.substring(index);
				}else {
					result += lineString.substring(0,index)+"��ͨȯ,"+lineString.substring(index);
				}
				result+="\n";
				if(i%100==0){
					writer.write(result);
					//writer.flush();
					result ="";
					System.err.println(i);
				}
				i++;
			}
			reader.close();
			writer.write(result);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	
		//System.err.println(result);
		return result;
	}

	private  static String getMobile(){
		BufferedReader reader = null;
		String lineString=null;
		String result = "";
		BufferedWriter writer;
		try {
			reader = new BufferedReader(new FileReader(new File("d:/ctime.txt")));
			writer = new BufferedWriter(new FileWriter(new File("d:/ctime1.txt")));
			int i=0;
			while ((lineString = reader.readLine()) != null) {
				if(lineString.length()>4)
					result +=TimeTools.getTime_yyyyMMdd_HHmmss(Long.valueOf(lineString)*1000)+"\n";
				else {
					result +="\n";
				}
				i++;
				if(i%1000==0){
					System.out.println(lineString+"-->>>>"+i);
					writer.write(result);
					result="";
				}
			}
			reader.close();
			writer.write(result);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	
		//System.err.println(result);
		return result;
	}
	

	/*����ʱ���*/
	private static void testPrice(Long start,Long end,Integer bHour,
			Integer bMinute,Integer eHour,Integer eMinute) {
		/*����ʱ���*/
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));//ȡ��ǰʱ�䣻
		
		calendar.setTimeInMillis(start*1000);
		Long pes =0L;
		
		if(eHour==24){
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			pes = calendar.getTimeInMillis()/1000+24*3600;//�볡������ײͽ���ʱ�䣨NTC��
		}else {
			calendar.set(Calendar.HOUR_OF_DAY, eHour);
			calendar.set(Calendar.MINUTE, eMinute);
			calendar.set(Calendar.SECOND, 0);
			pes = calendar.getTimeInMillis()/1000;//�볡������ײͽ���ʱ�䣨NTC��
		}
		
		calendar.set(Calendar.HOUR_OF_DAY, bHour);
		calendar.set(Calendar.MINUTE, bMinute);
		calendar.set(Calendar.SECOND, 0);
		Long pbs = calendar.getTimeInMillis()/1000;//�볡������ײͿ�ʼʱ�䣨NTC��
		
		List<List<Long>> out = new ArrayList<List<Long>>();//�ײ���ʱ���
		List<List<Long>> in = new ArrayList<List<Long>>(); //�ײ���ʱ���
		
		while( start<end){
			if(start<pbs){//������ʼʱ��С���ײͿ�ʼʱ��
				if(end<pbs){//��������ʱ��С���ײͿ�ʼʱ��
					List<Long> outList = new ArrayList<Long>();
					outList.add(start);
					outList.add(end);
					out.add(outList);//�ײ���ʱ������һ�Σ�������ʼʱ�䵽������ʱ�䣬����������������ѭ��
					break;
				}else {//��������ʱ����ڻ�����ײͿ�ʼʱ��
					List<Long> outList = new ArrayList<Long>();
					outList.add(start);
					outList.add(pbs);
					out.add(outList);//�ײ���ʱ������һ�Σ�������ʼʱ�䵽�ײͿ�ʼʱ��
					if(end>pes){//��������ʱ������ײͽ���ʱ��
						List<Long> inList = new ArrayList<Long>();
						inList.add(pbs);
						inList.add(pes);
						in.add(inList);//�ײ���ʱ������һ�Σ��ײͿ�ʼʱ�䵽�ײͽ���ʱ��
						start = pes;//������ʼʱ���ƶ����ײͽ���ʱ�䣬����һ��ѭ������Ҫ��������

					}else {//��������ʱ�����ײͽ���ʱ����
						List<Long> inList = new ArrayList<Long>();
						inList.add(pbs);
						inList.add(end);
						in.add(inList);//�ײ���ʱ������һ�Σ��ײͿ�ʼʱ�䵽��������ʱ�䣬 ����������������ѭ��
						break;
					}
				}
			}else {//������ʼʱ����ڻ�����ײͿ�ʼʱ��
				if(start<pes){//������ʼʱ��С���ײͽ���ʱ��
					if(end>pes){//��������ʱ��С���ײͽ���ʱ��
						List<Long> inList = new ArrayList<Long>();
						inList.add(start);
						inList.add(pes);
						in.add(inList);//�ײ���ʱ������һ�Σ�������ʼʱ�䵽�ײͽ���ʱ��
						start = pes;//������ʼʱ���ƶ����ײͽ���ʱ�䣬����һ��ѭ������Ҫ��������
					}else {
						List<Long> inList = new ArrayList<Long>();
						inList.add(start);
						inList.add(end);
						in.add(inList);//�ײ���ʱ������һ�Σ��ײͿ�ʼʱ�䵽��������ʱ�䣬 ����������������ѭ��
						break;
					}
				}else{//����ʱ����ڻ�����ײͽ���ʱ��
					pbs = pbs + 24*3600;//�ײ͵��ڶ���
					pes = pes + 24*3600;//�ײ͵��ڶ���
					if(end<pbs){//����ʱ��С�ڵڶ�����ײͿ�ʼʱ��
						List<Long> outList = new ArrayList<Long>();
						outList.add(start);
						outList.add(end);
						out.add(outList);//�ײ���ʱ������һ�Σ�������ʼʱ�䵽������ʱ��
						break;
					}else {//����ʱ����ڻ���ڵڶ�����ײͿ�ʼʱ��
						List<Long> outList = new ArrayList<Long>();
						outList.add(start);
						outList.add(pbs);
						out.add(outList);//�ײ���ʱ������һ�Σ�������ʼʱ�䵽�ڶ�����ײͿ�ʼʱ��
						start = pbs;//������ʼʱ���ƶ������ڶ�����ײͿ�ʼʱ��,����һ��ѭ������Ҫ��������
					}
				}
			}
		}
		if(!in.isEmpty()){
			System.out.println("�ײ��ڣ�");
			for(List<Long> l: in){
				System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(l.get(0)*1000)+"��"+TimeTools.getTime_yyyyMMdd_HHmmss(l.get(1)*1000));
			}
		}
		if(!out.isEmpty()){
			System.out.println("�ײ��⣺");
			for(List<Long> l: out){
				System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(l.get(0)*1000)+"-"+TimeTools.getTime_yyyyMMdd_HHmmss(l.get(1)*1000));
			}
		}
		/*����ʱ���*/
	}
	
	//���Ͷ���
	public static void main(String[] args) {
		Long start =1447541731L;//2015-11-15 6:55:31
		Long end =  1448009731L;//2015-11-18 16:55:31
		Integer bHour= 0;
		Integer bMiunte = 0;
		Integer eHour= 24;
		Integer eMiunte = 0;
		testPrice(start,end,bHour,bMiunte,eHour,eMiunte);
		//System.out.println(TimeTools.getTime_MMdd_HHmm(1447567045L*1000));
	//	weixin();//
	//	getMobile();
//		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
//		Map<String ,Object> m1 = new HashMap<String, Object>();
//		Map<String ,Object> m2 = new HashMap<String, Object>();
//		Map<String ,Object> m3 = new HashMap<String, Object>();
//		Map<String ,Object> m4 = new HashMap<String, Object>();
//		Map<String ,Object> m5 = new HashMap<String, Object>();
//		m1.put("create_time", 1L);
//		m2.put("create_time",2L);
//		m3.put("create_time", 3L);
//		m4.put("create_time", 4L);
//		m5.put("create_time", 5L);
//		m1.put("name", "1");
//		m2.put("name", "name1");
//		m3.put("name", "555");
//		list.add(m5);
//		list.add(m2);
//		list.add(m1);
//		list.add(m4);
//		System.out.println(list);
//		Collections.sort(list,new OrderSortCompare());
//		
		/*String carNumber =  "��A12334";
		if(carNumber.equals("���ƺ�δ֪"))
			carNumber = null;
		if(carNumber!=null&&!carNumber.equals("")&&carNumber.length()>5)
			carNumber = carNumber.substring(0,2)+"***"+carNumber.substring(5);
		else {
			carNumber="d";
		}
		System.out.println(carNumber);*/
//		System.out.println(list);
		/*for(int i =0;i<100;i++){
			int k =  RandomUtils.nextInt(20);
			//if(k==8)
			System.err.println(k);
		}*/
		//String mobiles = getMobile();
		//ȫ�峵����
		//String mesg = "���Ӹ����ѣ������Ѱ��ꡣΪ��л֧�֣����˳���ÿ�˻�3��4Ԫȯ��5�²���010-56450585��Ѱ���ͨ����˳����ʡ��˶���T ��ͣ������";
		//��ͨ��Ա������
		//String mesg = "�Ƽ����賵�����һԪ����֧���ŵ��ˣ�������һ���Ƚ���ͣ�����������������ֵ�����΢�Ź�עtingchebao2014�ء�������ѯ";
		
		//new SendMessage().sendMultiMessage("13860132164,15801482643,13041096867,18101333937"+mobiles, mesg);
/*//		
	   
	        String[] ss = getUUID(1); 
	        for(int i=0;i<ss.length;i++){ 
	        	String vInteger = "10258";
	        	String c = ss[i];
	        	StringBuffer nc = new StringBuffer();
	        	Integer charIndex=RandomUtils.nextInt(2);
	        	String stuf = "zd";
	        	c = stuf.charAt(charIndex)+c;
	        	System.out.println(c);
	        	for(int j=0;j<c.length();j++){
	        		Character chara = c.charAt(j);
	        		if(j>1&&j<9)
	        			chara = chara.toUpperCase(chara);
	        		nc.append(chara);
	        		if(j==1)
	        			nc.append(vInteger.charAt(0));
	        		else if(j==3){
	        			nc.append(vInteger.charAt(1));
	        		}else if(j==5){
	        			nc.append(vInteger.charAt(2));
	        		}else if(j==6){
	        			nc.append(vInteger.charAt(3));
	        		}else if(j==9){
	        			nc.append(vInteger.charAt(4));
	        		}
	        		
	        	}
	        	String result = nc.toString();
	            System.out.println(result); 
	            System.out.println(result.length());
	            System.out.println(result.charAt(2)+""+result.charAt(5)+result.charAt(8)+""+result.charAt(10)+""+result.charAt(14));
	        } */
		/*
		String url = "https://api.mch.weixin.qq.com/pay/downloadbill";
		String paramsm = "<xml>" +
				"<appid>wx2421b1c4370ec43b</appid>" +
				"<bill_date>20141110</bill_date>"+
				" <bill_type>ALL</bill_type>" +
				"<mch_id>10000100</mch_id> " +
				"<nonce_str>21df7dc9cd8616b56919f20d9f679233</nonce_str>"+
				"<sign>332F17B766FC787203EBE9D6E40457A1</sign>" +
				"</xml>";
		String dString = "20150610";
		SortedMap<Object,Object> params = new TreeMap<Object,Object>();
		String timestamp = Sha1Util.getTimeStamp();
		params.put("appid", Constants.WXPUBLIC_APPID);
        params.put("bill_date", dString);
        params.put("bill_type","SUCCESS");
        params.put("mch_id", Constants.WXPUBLIC_MCH_ID);
        params.put("nonce_str", timestamp);
        String paySign = PayCommonUtil.createSign("UTF-8", params);
        
        Map<String, Object> parMap = new HashMap<String, Object>();
        parMap.put("appid", Constants.WXPUBLIC_APPID);
        parMap.put("bill_date", dString);
        parMap.put("bill_type","SUCCESS");
        parMap.put("mch_id", Constants.WXPUBLIC_MCH_ID);
        parMap.put("nonce_str", timestamp);
        parMap.put("sign", paySign);
        
        StringBuffer xml = new StringBuffer();
		xml.append("<xml>");
		for(String key : parMap.keySet()){
			xml.append("<"+key+">"+parMap.get(key)+"</"+key+">");
		}
		xml.append("</xml>");
        
		 
        String result = CommonUtil.httpsRequest(url, "POST",xml.toString());*/
		/*BufferedReader reader =null;
		BufferedWriter writer=null;
		try {
			reader = new BufferedReader(new FileReader(new File("d:/pay.txt")));
			String lineString = "";
			Map<String, String> uidoidMap = new HashMap<String, String>();
			while ((lineString = reader.readLine()) != null) {
				String []temp = lineString.split(",");
				if(temp.length>6){
					String key = temp[6];
					if(key!=null&&key.length()==10)
						uidoidMap.put(key, temp[5]);
				}
			}
			reader.close();
			reader = new BufferedReader(new FileReader(new File("d:/weixin3.csv")));
			writer = new BufferedWriter(new FileWriter(new File("d:/weixin3new.csv"),true));
			while ((lineString = reader.readLine()) != null) {
				String []temp = lineString.split(",");
				if(temp.length==11){
					String uid = temp[10];
					if(uid.length()==9)
						uid ="0"+uid;
					String oid = uidoidMap.get(uid);
					if(oid!=null){
						lineString +=",'"+oid;
					}
					writer.write("\n"+lineString+" ");
				}
			}
			writer.flush();
			writer.close();
			reader.close();
			reader = new BufferedReader(new FileReader(new File("d:/weixin5.csv")));
			writer = new BufferedWriter(new FileWriter(new File("d:/weixin5new.csv"),true));
			while ((lineString = reader.readLine()) != null) {
				String []temp = lineString.split(",");
				if(temp.length==11){
					String uid = temp[10];
					if(uid.length()==9)
						uid ="0"+uid;
					String oid = uidoidMap.get(uid);
					if(oid!=null){
						lineString +=",'"+oid;
					}
					writer.write("\n"+lineString+" ");
				}
			}
			writer.flush();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				writer.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
	}
	
	 /** 
     * ���һ��UUID 
     * @return String UUID 
     */ 
    public static String getUUID(){ 
        String s = UUID.randomUUID().toString(); 
        //ȥ����-������ 
        return s.substring(s.lastIndexOf("-")+1);//.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24); 
    } 
    /** 
     * ���ָ����Ŀ��UUID 
     * @param number int ��Ҫ��õ�UUID���� 
     * @return String[] UUID���� 
     */ 
    public static String[] getUUID(int number){ 
        if(number < 1){ 
            return null; 
        } 
        String[] ss = new String[number]; 
        for(int i=0;i<number;i++){ 
            ss[i] = getUUID(); 
        } 
        return ss; 
    } 
}
