package com.zld.struts.request;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;
import pay.SecurityUtils;

import com.zld.AjaxUtil;
import com.zld.impl.MemcacheUtils;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.weixinpay.utils.util.JsonUtil;
import com.zld.weixinpay.utils.util.RequestHandler;
import com.zld.weixinpay.utils.util.Sha1Util;

public class WeixinPrePayAction extends Action {
	
	private Logger logger = Logger.getLogger(WeixinPrePayAction.class);
	
	
	
	
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//---------------------------------------------------------
		//'΢��֧��������ǩ��֧������ʾ�����̻����մ��ĵ����п�������
		//'����˷������ݣ�App��ȡ�����ݺ��ֱ�ӵ���΢��֧��
		
		///http://192.168.0.188/zld/wxpreorder.do?action=preorder&body=test&total_fee=100&attach=15801482643_1_1022_3_20140815
		///http://121.40.130.8/zld/wxpreorder.do?action=preorder&body=%E5%81%9C%E8%BD%A6%E5%AE%9D%E8%B4%A6%E6%88%B7%E5%85%85%E5%80%BC&total_fee=100&attach=15801482643_1_1022_3_20140815
		//---------------------------------------------------------
		String body =RequestUtil.getString(request, "body");
		body = AjaxUtil.decodeUTF8(body);
		String total_fee = RequestUtil.getString(request, "total_fee");
		//�û����ݰ�����������
		String attach =RequestUtil.getString(request, "attach");// "15801482643_1_1022_3_20140815";
		
		String timeStamp = System.currentTimeMillis()/1000+"";
		logger.error("weixin preorder,attach:"+attach+",body:"+body+",total_fee:"+total_fee+",timeStamp="+timeStamp);
		
		boolean debug = false;
		String  notifyUrl="http://s.tingchebao.com/zld/weixihandle";
		
		//String notifyUrl="http://service.yzjttcgs.com/zld/weixihandle";
		if(debug)
			notifyUrl="http://yxiudongyeahnet.vicp.cc/zld/weixihandle";
		//�ӻ�����ȡaccess_token,�������û�г���2Сʱ������token��û�л��ѹ��ڷ���"notoken"
		String weixinToken = memcacheUtils.getWeixinToken();
		if(weixinToken.equals("notoken")){
			String url = Constants.WXPAY_GETTOKEN_URL;
			//��weixin�ӿ�ȡaccess_token
			String result = new HttpProxy().doGet(url);
			logger.error("access_token json:"+result);
			//{"access_token":"llllllll","aaa":"sdsfe}
			//ȡaccess_token
			weixinToken = JsonUtil.getJsonValue(result, "access_token");//result.substring(17,result.indexOf(",")-1);
			logger.error("access_token:"+weixinToken);
			//���浽���� 
			memcacheUtils.setWeixinToken(weixinToken);
		}
		//---------------���ɶ����� ��ʼ------------------------
		//��ǰʱ�� yyyyMMddHHmmss
		//String currTime = TenpayUtil.getCurrTime();
		//8λ����
		//String strTime = currTime.substring(8, currTime.length());
		//��λ�����
		//String strRandom = TenpayUtil.buildRandom(4) + "";
		//10λ���к�,�������е�����
		//String strReq = strTime + strRandom;
		//�����ţ��˴���ʱ�����������ɣ��̻������Լ����������ֻҪ����ȫ��Ψһ����
		//String out_trade_no = strReq;
		//---------------���ɶ����� ����------------------------

		//��ȡ�ύ����Ʒ�۸�
		//String order_price = request.getParameter("order_price");
		//��ȡ�ύ����Ʒ����
		//String product_name = request.getParameter("product_name");

		TreeMap<String, String> outParams = new TreeMap<String, String>();

		RequestHandler reqHandler = new RequestHandler(request, response);
		//TenpayHttpClient httpClient = new TenpayHttpClient();
	    //��ʼ�� 
		//reqHandler.init();
		//'reqHandler.init(app_id, app_secret, app_key, partner, partner_key);

		//��ȡtokenֵ 
		//String token = reqHandler.GetToken();
		if (!"".equals(weixinToken)) {
			//=========================
			//����Ԥ֧����
			//=========================
			//����package��������
			
			String out_trade_no =  SecurityUtils.md5(System.currentTimeMillis()+""+new Random().nextInt(10000000));
			
			SortedMap<String, String> packageParams = new TreeMap<String, String>();
			packageParams.put("attach",attach);
			packageParams.put("bank_type", "WX"); //��Ʒ����   
			packageParams.put("body", body); //��Ʒ����   
			packageParams.put("fee_type", "1"); //���֣�1�����   66
			packageParams.put("input_charset", "UTF-8"); //�ַ�����
			packageParams.put("notify_url", notifyUrl); //���ղƸ�֪ͨͨ��URL  
			packageParams.put("out_trade_no", out_trade_no); //�̼Ҷ�����  
			packageParams.put("partner", Constants.WXPAY_PARTNERID); //�̻���    
			packageParams.put("spbill_create_ip", request.getRemoteAddr()); //�������ɵĻ���IP��ָ�û��������IP  
			packageParams.put("total_fee",  String.valueOf((new BigDecimal(total_fee)
							.multiply(new BigDecimal(100))).intValue())); //��Ʒ���,�Է�Ϊ��λ  

			//��ȡpackage��
			String packageValue = reqHandler.genPackage(packageParams);

			String noncestr = Sha1Util.getNonceStr();
			//String timestamp = Sha1Util.getTimeStamp();
			String traceid= 15375242041L+ timeStamp;

			//����֧������
			SortedMap<String, String> signParams = new TreeMap<String, String>();
			signParams.put("appid", Constants.WXPAY_APPID);
			signParams.put("appkey", Constants.WXPAY_APPKEY);
			signParams.put("noncestr", noncestr);
			signParams.put("package", packageValue);
			signParams.put("timestamp", timeStamp);
			signParams.put("traceid", traceid);

			//����֧��ǩ����Ҫ����URLENCODER��ԭʼֵ����SHA1�㷨��
			String sign = Sha1Util.createSHA1Sign(signParams);
			//���ӷǲ���ǩ���Ķ������
			signParams.put("app_signature", sign);
			signParams.put("sign_method", "sha1");

			//��ȡprepayId
			String prepayid = reqHandler.sendPrepay(signParams,weixinToken);
			if (null != prepayid && !"".equals(prepayid)) {//�ɹ�ȡ�أ������ݷ��ظ��ͻ��ˣ��ɿͻ��˷��������֧����
		 		
		 		List<NameValuePair> _signParams = new LinkedList<NameValuePair>();
		 		_signParams.add(new NameValuePair("appid", Constants.WXPAY_APPID));
		 		_signParams.add(new NameValuePair("appkey",Constants.WXPAY_APPKEY));
		 		_signParams.add(new NameValuePair("noncestr",noncestr));
		 		_signParams.add(new NameValuePair("package", "Sign=WXpay"));
		 		_signParams.add(new NameValuePair("partnerid", Constants.WXPAY_PARTNERID));
		 		_signParams.add(new NameValuePair("prepayid", prepayid));
		 		_signParams.add(new NameValuePair("timestamp", timeStamp));
		 		String _sign = genSign(_signParams);
		 		String rString = "{\"prepayId\":\""+prepayid+"\",\"nonceStr\":\""+noncestr+"\"," +
		 				"\"timeStamp\":\""+timeStamp+"\",\"sign\":\""+_sign+"\"}";
		 		System.err.println(">>>>>>>>>>>"+rString);
		 		AjaxUtil.ajaxOutput(response,rString);
		 	}else {
				AjaxUtil.ajaxOutput(response, "{}");
			}
		} else {
			outParams.put("retcode", "-1");
			outParams.put("retmsg", "���󣺻�ȡ����Token");
		}
		return null;
	}
	
	
	
	
	/**
	 * ����֧��ǩ����app_signature��
	 * 
	 * @param params
	 * @return
	 */
	private String genSign(List<NameValuePair> params) {
		// 1���Ƚ�params���ֵ�������
		// Collections.sort(params, new Comparator<NameValuePair>() {
		// @Override
		// public int compare(NameValuePair lhs, NameValuePair rhs) {
		//
		// return lhs.getName().compareTo(rhs.getName());
		// }
		// });

		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (; i < params.size() - 1; i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append(params.get(i).getName());
		sb.append('=');
		sb.append(params.get(i).getValue());
		System.err.println(sb.toString());
		return SecurityUtils.sha1(sb.toString());
	}
}
