package pay.wxnew;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.JDOMException;

import pay.wxnew.Constants;

import com.google.gson.Gson;
import com.zld.weixinpay.utils.util.MD5Util;
import com.zld.weixinpay.utils.util.TenpayHttpClient;
import com.zld.weixinpay.utils.util.TenpayUtil;
import com.zld.weixinpay.utils.util.XMLUtil;


/*
 '΢��֧��������ǩ��֧������������
 '============================================================================
 'api˵����
 'init(app_id, app_secret, partner_key, app_key);
 '��ʼ��������Ĭ�ϸ�һЩ������ֵ����cmdno,date�ȡ�
 'setKey(key_)'�����̻���Կ
 'getLasterrCode(),��ȡ�������
 'GetToken();��ȡToken
 'getTokenReal();Token���ں�ʵʱ��ȡToken
 'createMd5Sign(signParams);����Md5ǩ��
 'genPackage(packageParams);��ȡpackage��
 'createSHA1Sign(signParams);����ǩ��SHA1
 'sendPrepay(packageParams);�ύԤ֧��
 'getDebugInfo(),��ȡdebug��Ϣ
 '============================================================================
 '*/
public class RequestHandler {
	/** Token��ȡ���ص�ַ��ַ */
	private String tokenUrl;
	/** Ԥ֧������url��ַ */
	private String gateUrl;
	/** ��ѯ֧��֪ͨ����URL */
	private String notifyUrl;
	/** �̻����� */
	private String appid;
	private String appkey;
	private String partnerkey;
	private String appsecret;
	private String key;
	/** ����Ĳ��� */
	private SortedMap parameters;
	/** Token */
	private String Token;
	private String charset;
	/** debug��Ϣ */
	private String debugInfo;
	private String last_errcode;

	private HttpServletRequest request;

	private HttpServletResponse response;

	/**
	 * ��ʼ���캯����
	 * 
	 * @return
	 */
	public RequestHandler(HttpServletRequest request,
			HttpServletResponse response) {
		this.last_errcode = "0";
		this.request = request;
		this.response = response;
		this.charset = "UTF-8";
		this.parameters = new TreeMap();
		// ��ȡToken����
		tokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
		// �ύԤ֧��������
		gateUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		// ��֤notify֧����������
		notifyUrl = "https://gw.tenpay.com/gateway/simpleverifynotifyid.xml";
	}

	/**
	 * ��ʼ��������
	 */
	public void init(String app_id, String app_secret, String app_key,
			String partner, String key) {
		this.last_errcode = "0";
		this.Token = "token_";
		this.debugInfo = "";
		this.appkey = app_key;
		this.appid = app_id;
		this.partnerkey = partner;
		this.appsecret = app_secret;
		this.key = key;
	}

	public void init() {
	}

	/**
	 * ��ȡ�������
	 */
	public String getLasterrCode() {
		return last_errcode;
	}

	/**
	 *��ȡ��ڵ�ַ,����������ֵ
	 */
	public String getGateUrl() {
		return gateUrl;
	}

	/**
	 * ��ȡ����ֵ
	 * 
	 * @param parameter
	 *            ��������
	 * @return String
	 */
	public String getParameter(String parameter) {
		String s = (String) this.parameters.get(parameter);
		return (null == s) ? "" : s;
	}

	/**
	 * ������Կ
	 */
	public void setKey(String key) {
		this.key = key;
	}

	

	// �����ַ�����
	public String UrlEncode(String src) throws UnsupportedEncodingException {
		return URLEncoder.encode(src, this.charset).replace("+", "%20");
	}

	// ��ȡpackage��������ǩ����
	public String genPackage(SortedMap<String, String> packageParams)
			throws UnsupportedEncodingException {
		String sign = createSign(packageParams);

//		StringBuffer sb = new StringBuffer();
//		Set es = packageParams.entrySet();
//		Iterator it = es.iterator();
//		while (it.hasNext()) {
//			Map.Entry entry = (Map.Entry) it.next();
//			String k = (String) entry.getKey();
//			String v = (String) entry.getValue();
//			sb.append(k + "=" + UrlEncode(v) + "&");
//		}
//
//		// ȥ�����һ��&
//		String packageValue = sb.append("sign=" + sign).toString();
//		System.out.println("packageValue=" + packageValue);
		String packageValue="<xml>";
		// <trade_type><![CDATA[APP]]></trade_type>
		for(String key : packageParams.keySet()){
			packageValue +="<"+key+"><![CDATA["+packageParams.get(key)+"]]></"+key+">";
		}
		packageValue +="<sign><![CDATA["+sign+"]]></sign></xml>";
		return packageValue;
	}

	/**
	 * ����md5ժҪ,������:����������a-z����,������ֵ�Ĳ������μ�ǩ����
	 */
	public String createSign(SortedMap<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + Constants.WXPAY_PARTNERKEY);
		System.out.println("md5 sb:" + sb);
		String sign = MD5Util.MD5Encode(sb.toString(), this.charset)
				.toUpperCase();

		return sign;

	}
	
	/**
	 * ����md5ժҪ,������:����������a-z����,������ֵ�Ĳ������μ�ǩ����
	 */
	public String _createSign(SortedMap<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		List<String> keys = new ArrayList<String>(packageParams.keySet());
		Collections.sort(keys);
		for(String key :keys) {
			String k = key;
			String v = packageParams.get(key);
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + Constants.WXPAY_PARTNERKEY);
		System.out.println("md5 sb:" + sb);
		String sign = MD5Util.MD5Encode(sb.toString(), this.charset)
				.toUpperCase();

		return sign;

	}
	

	// �ύԤ֧��
	public String sendPrepay(String postData,String token) {
		String prepayid = "";
		// ת����json
		Gson gson = new Gson();
		/* String postData =gson.toJson(packageParams); */
		/*String postData = "{";
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (k != "appkey") {
				if (postData.length() > 1)
					postData += ",";
				postData += "\"" + k + "\":\"" + v + "\"";
			}
		}
		postData += "}";*/
		// �������Ӳ���
		String requestUrl = this.gateUrl + "?access_token=" + token;
		System.out.println("post url=" + requestUrl);
		System.out.println("post data=" + postData);
		TenpayHttpClient httpClient = new TenpayHttpClient();
		httpClient.setReqContent(requestUrl);
		String resContent = "";
		
		if (httpClient.callHttpPost(requestUrl, postData)) {
			try {
				resContent = httpClient.getResContent();
				Map<String, String> map = XMLUtil.doXMLParse(resContent);
				if ("SUCCESS".equals(map.get("return_code"))) {
					prepayid = map.get("prepay_id");
				} else {
					System.out.println("get prepayid err ,info =" + map);
				}
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ����debug info
			System.out.println("res json=" + resContent);
		}
		return prepayid;
	}

	/**
	 * ����packageǩ��
	 */
	public boolean createMd5Sign(String signParams) {
		StringBuffer sb = new StringBuffer();
		Set es = this.parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}

		// ���ժҪ
		String enc = TenpayUtil.getCharacterEncoding(this.request,
				this.response);
		String sign = MD5Util.MD5Encode(sb.toString(), enc).toLowerCase();

		String tenpaySign = this.getParameter("sign").toLowerCase();

		// debug��Ϣ
		this.setDebugInfo(sb.toString() + " => sign:" + sign + " tenpaySign:"
				+ tenpaySign);

		return tenpaySign.equals(sign);
	}

	/**
	 * ����debug��Ϣ
	 */
	protected void setDebugInfo(String debugInfo) {
		this.debugInfo = debugInfo;
	}
	public void setPartnerkey(String partnerkey) {
		this.partnerkey = partnerkey;
	}
	public String getDebugInfo() {
		return debugInfo;
	}
	public String getKey() {
		return key;
	}

}
