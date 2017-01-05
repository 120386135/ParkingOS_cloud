package pay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlipayUtil {
	
	public static final String  SIGN_ALGORITHMS = "SHA1WithRSA";
	/** 
     * ��ȥ�����еĿ�ֵ��ǩ������
     * @param sArray ǩ��������
     * @return ȥ����ֵ��ǩ�����������ǩ��������
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /** 
     * ����������Ԫ�����򣬲����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ���
     * @param params ��Ҫ���򲢲����ַ�ƴ�ӵĲ�����
     * @return ƴ�Ӻ��ַ���
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//ƴ��ʱ�����������һ��&�ַ�
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }
    
    /**
     * ֧������Ϣ��֤��ַ
     */
    private static final String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify&";

    /**
     * ��֤��Ϣ�Ƿ���֧���������ĺϷ���Ϣ
     * @param params ֪ͨ�������Ĳ�������
     * @return ��֤���
     */
    public static boolean verify(Map<String, String> params) {

        //�ж�responsetTxt�Ƿ�Ϊtrue��isSign�Ƿ�Ϊtrue
        //responsetTxt�Ľ������true����������������⡢���������ID��notify_idһ����ʧЧ�й�
        //isSign����true���밲ȫУ���롢����ʱ�Ĳ�����ʽ���磺���Զ�������ȣ��������ʽ�й�
    	String responseTxt = "true";
		if(params.get("notify_id") != null) {
			String notify_id = params.get("notify_id");
			responseTxt = verifyResponse(notify_id);
		}
	    String sign = "";
	    if(params.get("sign") != null) {
	    	sign = params.get("sign");
	    	
	    }
	    boolean isSign = getSignVeryfy(params, sign);

        //д��־��¼����Ҫ���ԣ���ȡ����������ע�ͣ�
        //String sWord = "responseTxt=" + responseTxt + "\n isSign=" + isSign + "\n ���ػ����Ĳ�����" + AlipayCore.createLinkString(params);
	    //AlipayCore.logResult(sWord);

        if (isSign && responseTxt.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ���ݷ�����������Ϣ������ǩ�����
     * @param Params ֪ͨ�������Ĳ�������
     * @param sign �ȶԵ�ǩ�����
     * @return ���ɵ�ǩ�����
     */
	public static boolean getSignVeryfy(Map<String, String> Params, String sign) {
    	//���˿�ֵ��sign��sign_type����
    	Map<String, String> sParaNew =paraFilter(Params);
        //��ȡ��ǩ���ַ���
        String preSignStr = createLinkString(sParaNew);
        //���ǩ����֤���
       // System.err.println(preSignStr);
        boolean isSign = false;
        if(AlipayConfig.sign_type.equals("RSA")){
        	isSign = verify(preSignStr, sign, AlipayConfig.ali_public_key, AlipayConfig.input_charset);
        }
      //  System.err.println("alipay verify:"+isSign);
        return isSign;
    }
	  /**
     * ���ݷ�����������Ϣ������ǩ����� -----ɨ��֧����publickey��һ��
     * @param Params ֪ͨ�������Ĳ�������
     * @param sign �ȶԵ�ǩ�����
     * @return ���ɵ�ǩ�����
     */
	public static boolean getQrSignVeryfy(Map<String, String> Params, String sign) {
    	//���˿�ֵ��sign��sign_type����
    	Map<String, String> sParaNew =paraFilter(Params);
        //��ȡ��ǩ���ַ���
        String preSignStr = createLinkString(sParaNew);
        //���ǩ����֤���
       //ystem.err.println("qr verify:"+preSignStr);
        boolean isSign = false;
        if(AlipayConfig.sign_type.equals("RSA")){
        	isSign = verify(preSignStr, sign, AlipayConfig.ALIPUBLICKEY4QR, AlipayConfig.input_charset);
        }
       // System.err.println("alipay verify:"+isSign);
        return isSign;
    }
    /**
    * ��ȡԶ�̷�����ATN���,��֤����URL
    * @param notify_id ֪ͨУ��ID
    * @return ������ATN���
    * ��֤�������
    * invalid����������� ��������������ⷵ�ش�����partner��key�Ƿ�Ϊ�� 
    * true ������ȷ��Ϣ
    * false �������ǽ�����Ƿ�������ֹ�˿������Լ���֤ʱ���Ƿ񳬹�һ����
    */
    private static String verifyResponse(String notify_id) {
        //��ȡԶ�̷�����ATN�������֤�Ƿ���֧��������������������

        String partner = AlipayConfig.partner;
        String veryfy_url = HTTPS_VERIFY_URL + "partner=" + partner + "&notify_id=" + notify_id;

        return checkUrl(veryfy_url);
    }
    /**
    * ��ȡԶ�̷�����ATN���
    * @param urlvalue ָ��URL·����ַ
    * @return ������ATN���
    * ��֤�������
    * invalid����������� ��������������ⷵ�ش�����partner��key�Ƿ�Ϊ�� 
    * true ������ȷ��Ϣ
    * false �������ǽ�����Ƿ�������ֹ�˿������Լ���֤ʱ���Ƿ񳬹�һ����
    */
    private static String checkUrl(String urlvalue) {
        String inputLine = "";

        try {
            URL url = new URL(urlvalue);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
                .getInputStream()));
            inputLine = in.readLine().toString();
        } catch (Exception e) {
            e.printStackTrace();
            inputLine = "";
        }

        return inputLine;
    }
    
	/**
	* RSA��ǩ�����
	* @param content ��ǩ������
	* @param sign ǩ��ֵ
	* @param ali_public_key ֧������Կ
	* @param input_charset �����ʽ
	* @return ����ֵ
	*/
	public static boolean verify(String content, String sign, String ali_public_key, String input_charset)
	{
		try 
		{
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        byte[] encodedKey = Base64.decode(ali_public_key);
	        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

		
			java.security.Signature signature = java.security.Signature
			.getInstance(SIGN_ALGORITHMS);
		
			signature.initVerify(pubKey);
			signature.update( content.getBytes(input_charset) );
		
			boolean bverify = signature.verify( Base64.decode(sign) );
			return bverify;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	* RSAǩ��
	* @param content ��ǩ������
	* @param privateKey �̻�˽Կ
	* @param input_charset �����ʽ
	* @return ǩ��ֵ
	*/
	/*public static String sign(Map<String, String> parMap)
	{
		
        try 
        {
        	//content = new String(Base64.decode(content));
        	String content = "partner=\"2088411488582814\"&seller_id=\"caiwu@zhenlaidian.com\"&out_trade_no=\"041616213922193\"&subject=\"���Ե���Ʒ\"&body=\"�ò�����Ʒ����ϸ����\"&total_fee=\"0.01\"&notify_url=\"http://service.yzjttcgs.com/zld/rechage\"&service=\"mobile.securitypay.pay\"&payment_type=\"1\"&_input_charset=\"utf-8\"&it_b_pay=\"30m\"&return_url=\"m.alipay.com\"";
        	String preSignStr = createLinkString(parMap);
        	PKCS8EncodedKeySpec priPKCS8 	= new PKCS8EncodedKeySpec( Base64.decode(AlipayConfig.private_key) ); 
        	KeyFactory keyf 				= KeyFactory.getInstance("RSA");
        	PrivateKey priKey 				= keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature
                .getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update( content.getBytes(AlipayConfig.input_charset) );

            byte[] signed = signature.sign();
            
            return Base64.encode(signed);
        }
        catch (Exception e) 
        {
        	e.printStackTrace();
        }
        
        return null;
    }*/
	
	public static void main(String[] args) {
		
		/*
		 * buyer_id=2088702201663304
		trade_no=2014081250497230
		body=�����磨�������ƶ��Ƽ����޹�˾
		use_coupon=N
		notify_time=2014-08-12 19:36:49
		subject=ͣ�����˻���ֵ
		sign_type=RSA
		is_total_fee_adjust=Y
		notify_type=trade_status_sync
		out_trade_no=081219364012156
		trade_status=WAIT_BUYER_PAY
		discount=0.00
		sign=K5zNFe4y2pkhh2ORv+uRkQrQYobUkQR6hkhnkQzvpqUPjCJ8AV6g/WsaISE1Ilh+4iRvsJAL8OMdfVmkFCTbGgEjH/QzFmf+TzHEsaeYU9MljlqypmlIYvoL3muMF7cK+qJNP3SQplgesdWPA49G54ESb1zr/I2URXkd/Pi8XVM=
		gmt_create=2014-08-12 19:36:48
		buyer_email=ggchaifeng@gmail.com
		price=0.01
		total_fee=0.01
		seller_id=2088411488582814
		quantity=1
		seller_email=caiwu@zhenlaidian.com
		notify_id=c644ad6244f709f8893efa9f43c92dd93o
		payment_type=1
		 */
		Map<String, String> map = new HashMap<String, String>();
		map.put("buyer_id", "2088702201663304");
		map.put("trade_no", "2014081250497230");
		map.put("body", "�����磨�������ƶ��Ƽ����޹�˾");
		map.put("use_coupon", "N");
		map.put("notify_time", "2014-08-12 19:36:49");
		map.put("subject", "ͣ�����˻���ֵ");
		map.put("sign_type", "RSA");
		map.put("is_total_fee_adjust", "Y");
		map.put("notify_type", "trade_status_sync");
		map.put("out_trade_no", "081219364012156");
		map.put("trade_status", "WAIT_BUYER_PAY");
		map.put("discount", "0.00");
		map.put("sign", "K5zNFe4y2pkhh2ORv+uRkQrQYobUkQR6hkhnkQzvpqUPjCJ8AV6g/WsaISE1Ilh+4iRvsJAL8OMdfVmkFCTbGgEjH/QzFmf+TzHEsaeYU9MljlqypmlIYvoL3muMF7cK+qJNP3SQplgesdWPA49G54ESb1zr/I2URXkd/Pi8XVM=");
		map.put("gmt_create", "2014-08-12 19:36:48");
		map.put("buyer_email", "ggchaifeng@gmail.com");
		map.put("price", "0.01");
		map.put("total_fee", "0.01");
		map.put("seller_id", "2088411488582814");
		map.put("quantity", "1");
		map.put("seller_email", "caiwu@zhenlaidian.com");
		map.put("notify_id", "c644ad6244f709f8893efa9f43c92dd93o");
		map.put("payment_type", "1");
		String sign = "K5zNFe4y2pkhh2ORv+uRkQrQYobUkQR6hkhnkQzvpqUPjCJ8AV6g/WsaISE1Ilh+4iRvsJAL8OMdfVmkFCTbGgEjH/QzFmf+TzHEsaeYU9MljlqypmlIYvoL3muMF7cK+qJNP3SQplgesdWPA49G54ESb1zr/I2URXkd/Pi8XVM=";
		System.out.println(getSignVeryfy(map, sign));
	}
	
}
