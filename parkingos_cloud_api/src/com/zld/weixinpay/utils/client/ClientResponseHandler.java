package com.zld.weixinpay.utils.client;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.JDOMException;

import pay.Constants;

import com.zld.weixinpay.utils.util.MD5Util;
import com.zld.weixinpay.utils.util.XMLUtil;
/**
 * ��̨Ӧ����<br/>
 * ========================================================================<br/>
 * api˵����<br/>
 * getKey()/setKey(),��ȡ/������Կ<br/>
 * getContent() / setContent(), ��ȡ/����ԭʼ����<br/>
 * getParameter()/setParameter(),��ȡ/���ò���ֵ<br/>
 * getAllParameters(),��ȡ���в���<br/>
 * isTenpaySign(),�Ƿ�Ƹ�ͨǩ��,true:�� false:��<br/>
 * getDebugInfo(),��ȡdebug��Ϣ<br/>
 * 
 * ========================================================================<br/>
 *
 */
public class ClientResponseHandler {
	
	/** Ӧ��ԭʼ���� */
	private String content;
	
	/** Ӧ��Ĳ��� */
	private SortedMap parameters; 
	
	/** debug��Ϣ */
	private String debugInfo;
	
	/** ��Կ */
	private String key;
	
	/** �ַ��� */
	private String charset;
	
	public ClientResponseHandler() {
		this.content = "";
		this.parameters = new TreeMap();
		this.debugInfo = "";
		this.key =  Constants.WXPAY_PARTNERKEY;
		this.charset = "UTF-8";
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) throws Exception {
		this.content = content;
		
		this.doParse();
	}
	
	/**
	 * ��ȡ����ֵ
	 * @param parameter ��������
	 * @return String 
	 */
	public String getParameter(String parameter) {
		String s = (String)this.parameters.get(parameter); 
		return (null == s) ? "" : s;
	}
	
	/**
	 * ���ò���ֵ
	 * @param parameter ��������
	 * @param parameterValue ����ֵ
	 */
	public void setParameter(String parameter, String parameterValue) {
		String v = "";
		if(null != parameterValue) {
			v = parameterValue.trim();
		}
		this.parameters.put(parameter, v);
	}
	
	/**
	 * �������еĲ���
	 * @return SortedMap
	 */
	public SortedMap getAllParameters() {
		return this.parameters;
	}	

	public String getDebugInfo() {
		return debugInfo;
	}
	
	/**
	*��ȡ��Կ
	*/
	public String getKey() {
		return key;
	}

	/**
	*������Կ
	*/
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getCharset() {
		return this.charset;
	}
	
	public void setCharset(String charset) {
		this.charset = charset;
	}	
	
	/**
	 * �Ƿ�Ƹ�ͨǩ��,������:����������a-z����,������ֵ�Ĳ������μ�ǩ����
	 * @return boolean
	 */
	public boolean isTenpaySign() {
		StringBuffer sb = new StringBuffer();
		Set es = this.parameters.entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			String v = (String)entry.getValue();
			if(!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}
		
		sb.append("key=" + this.getKey());
		
		//���ժҪ
		String sign = MD5Util.MD5Encode(sb.toString(), this.charset).toLowerCase();
		
		String tenpaySign = this.getParameter("sign").toLowerCase();
		
		//debug��Ϣ
		this.setDebugInfo(sb.toString() + " => sign:" + sign +
				" tenpaySign:" + tenpaySign);
		System.err.println("weixin....,tenpaySign:"+tenpaySign+",sign:"+sign);
		return tenpaySign.equals(sign);
	}
	
	
	public static void main(String[] args) {
		/**
		 * 		 *   attach=15375242041_1_25_1_20140925, 
			 *   bank_billno=201409257727769,
			 *   bank_type=3006, 
			 *   discount=0, 
			 *   fee_type=1, 
			 *   input_charset=UTF-8, 
			 *   notify_id=UfkifSJxOiiL-WgfF1XVsNYYhKFfffy46cJI4V9_-_YluNFK44LjE4o4qFuUnAGdDqo1ZHkKZW4TNMEb4TE5guQz54K8AoKc, 
			 *   out_trade_no=dc36f18a9a0a776671d4879cae69b551,
			 *   partner=1220886701, 
			 *   product_fee=2, 
			 *   sign=459604EA471EFE10CC7016189E30E86E,
			 *   sign_type=MD5, 
			 *   time_end=20140925104544, 
			 *   total_fee=2,
			 *   trade_mode=1,
			 *   trade_state=0, 
			 *   transaction_id=1220886701201409253369167825,*- 
			 *   transport_fee=0
		 */
		
		SortedMap test = new TreeMap();
		test.put("attach", "15375242041_1_25_1_20140925");
		test.put("bank_billno", "201409257727769");
		test.put("bank_type", "3006");
		test.put("discount", "0");
		test.put("fee_type", "1");
		test.put("input_charset", "UTF-8");
		test.put("notify_id", "UfkifSJxOiiL-WgfF1XVsNYYhKFfffy46cJI4V9_-_YluNFK44LjE4o4qFuUnAGdDqo1ZHkKZW4TNMEb4TE5guQz54K8AoKc");
		test.put("out_trade_no", "dc36f18a9a0a776671d4879cae69b551");
		test.put("partner", "1220886701");
		test.put("product_fee", "2");
		test.put("sign_type", "MD5");
		test.put("time_end", "20140925104544");
		test.put("total_fee", "2");
		test.put("trade_mode", "1");
		test.put("trade_state", "0");
		test.put("transaction_id", "1220886701201409253369167825");
		test.put("transport_fee", "0");
		
		StringBuffer sb = new StringBuffer();
		Set es = test.entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			String v = (String)entry.getValue();
			if(!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}
		
		sb.append("key=d7b993008827a203659ca008372382fe");
		
		//���ժҪ
		String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8").toLowerCase();
		
		String tenpaySign ="459604EA471EFE10CC7016189E30E86E".toLowerCase();
		
		//debug��Ϣ
		System.err.println("weixin....,tenpaySign:"+tenpaySign+",sign:"+sign);
		System.err.println(tenpaySign.equals(sign));
	}
	
	
	
	/**
	 * �Ƿ�Ƹ�ͨǩ��
	 * @param signParameterArray ǩ���Ĳ�������
	 * @return boolean
	 */
	protected boolean isTenpaySign(String signParameterArray[]) {

		StringBuffer signPars = new StringBuffer();
		for(int index = 0; index < signParameterArray.length; index++) {
			String k = signParameterArray[index];
			String v = this.getParameter(k);
			if(null != v && !"".equals(v)) {
				signPars.append(k + "=" + v + "&");
			}
		}
		
		signPars.append("key=" + this.getKey());
				
		//���ժҪ
		String sign = MD5Util.MD5Encode(
				signPars.toString(), this.charset).toLowerCase();
		
		String tenpaySign = this.getParameter("sign").toLowerCase();
		
		//debug��Ϣ
		this.setDebugInfo(signPars.toString() + " => sign:" + sign +
				" tenpaySign:" + tenpaySign);
		
		return tenpaySign.equals(sign);
	}
	

	protected void setDebugInfo(String debugInfo) {
		this.debugInfo = debugInfo;
	}
	
	/**
	 * ����XML����
	 */
	protected void doParse() throws JDOMException, IOException {
		String xmlContent = this.getContent();
		
		//����xml,�õ�map
		Map m = XMLUtil.doXMLParse(xmlContent);
		
		//���ò���
		Iterator it = m.keySet().iterator();
		while(it.hasNext()) {
			String k = (String) it.next();
			String v = (String) m.get(k);
			this.setParameter(k, v);
		}
		
	}
	

}
