package pay;

public class Constants {
	// ----------------------΢��֧��Key--------------------------------------
	//----------------------ͣ����--------------------------------------
	public static final String WXPAY_APPID = "";
	public static final String WXPAY_PARTNERID = "";
	public static final String WXPAY_APPSECRET = "";
	public static final String WXPAY_PARTNERKEY = "";
	public static final String WXPAY_APPKEY = "kkyTJVjVnxOYDh7hhRQinfxWyIYEDBSRSm72VZWvKyxwpczPjODQpqRHdy3JIHuVJKjlJS0UwaINGBx5HziqfERh0W8tQ3v0aXmheFLIzscSdBA0vrkjkLeoyOffA2PW";

	public static final String WXPAY_GETTOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			+ WXPAY_APPID + "&secret=" + WXPAY_APPSECRET;
	public static final String WXPAY_GETPREPAYID_URL = "https://api.weixin.qq.com/pay/genprepay";
	
	//----------------------΢�Ź��ں�-------------------------------------

	public static  String WXPUBLIC_APPID = "";
	public static  String WXPUBLIC_SECRET = "";
	public static  String WXPUBLIC_REDIRECTURL = "";
	
	public static  String LOCAL_NAME = "zld";
	
//	public static  String WXPUBLIC_REDIRECTURL = "192.168.199.239";
//	public static  String LOCAL_NAME = "zldi";
	
	public static  String WXPUBLIC_S_DOMAIN = "";
	
	public static final String WXPUBLIC_MCH_ID = "";
	public static final String WXPUBLIC_APPKEY = "";
	//��ȡaccess_token
	public static String WXPUBLIC_GETTOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			+ WXPUBLIC_APPID + "&secret=" + WXPUBLIC_SECRET;
	//ͳһ֧���ӿ�
	public static String WXPUBLIC_UNIFIEDORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	//֪ͨ��ַ
	public static String WXPUBLIC_NOTIFY_URL = "http://"+WXPUBLIC_S_DOMAIN+"/zld/wxphandle";
	
	//�˿��ַ
	public static String WXPUBLIC_BACK_URL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
	
	//֪ͨ��ַ(����)
//	public static final String WXPUBLIC_NOTIFY_URL = "http://wang151068941.oicp.net/zld/wxphandle";
	
	public static String WXPUBLIC_SUCCESS_NOTIFYMSG_ID = "dhyfJiJAhe8iZE39HD2m5U-Qc1jeGa0h4-_ynhrlrgA";//����֧���ɹ�
	
	public static String WXPUBLIC_FAIL_NOTIFYMSG_ID = "DoUGP9qSXSeIV_Y0mY0tHdpbx0qXvTUxmsDWP19a-H0";//����֧��ʧ��
	
	public static String WXPUBLIC_BONUS_NOTIFYMSG_ID = "DFzXEfyMtIO2GmAreD7pDd02t2KEBAGpAOpF4aOB4E4";//����֪ͨ
	//δ�����֪ͨ
	public static String WXPUBLIC_ORDER_NOTIFYMSG_ID = "gCTGzyprc1N1dhIcRYBlTIKOMTExo0QPBKSW6NF480o";
	
	public static String WXPUBLIC_BACK_NOTIFYMSG_ID = "2Th_VLboH7OotEa4csiXw0_-ejXVs31B0lMn42ftpN8";//�˿�
	
	public static String WXPUBLIC_TICKET_ID = "9bgsmMKaKKfGpNbQoFFuyvqfv2Pd504GZamIcBm6AAU";//��ô���ȯ֪ͨ
	
	public static String WXPUBLIC_AUDITRESULT_ID = "DP2IHNX-OH7NyR54mIA0VPa77h8BaJhqtp1XiCJ4FSc";//��˽��֪ͨ
	
	public static String WXPUBLIC_FLYGMAMEMESG_ID = "2Fdm4xKSfE8jI_jmYcFQKRq5iBn1HDn0_O16ksWAZBk";//��Ƭ����֪ͨ����һ��Ӻû���
	
	public static String WXPUBLIC_LEAVE_MESG_ID = "IS-0WgX_gcrxGHgYypAxRojx0jQl_nLBq4PBAst5GFE";//����
	
	public static class ShowMsgActivity {
		public static final String STitle = "showmsg_title";
		public static final String SMessage = "showmsg_message";
		public static final String BAThumbData = "showmsg_thumb_data";
	}
}
