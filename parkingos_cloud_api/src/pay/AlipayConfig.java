package pay;

/* *
 *������AlipayConfig
 *���ܣ�����������
 *��ϸ�������ʻ��й���Ϣ������·��
 *�汾��3.3
 *���ڣ�2012-08-10
 *˵����
 *���´���ֻ��Ϊ�˷����̻����Զ��ṩ���������룬�̻����Ը����Լ���վ����Ҫ�����ռ����ĵ���д,����һ��Ҫʹ�øô��롣
 *�ô������ѧϰ���о�֧�����ӿ�ʹ�ã�ֻ���ṩһ���ο���
	
 *��ʾ����λ�ȡ��ȫУ����ͺ��������ID
 *1.������ǩԼ֧�����˺ŵ�¼֧������վ(www.alipay.com)
 *2.������̼ҷ���(https://b.alipay.com/order/myOrder.htm)
 *3.�������ѯ���������(PID)��������ѯ��ȫУ����(Key)��

 *��ȫУ����鿴ʱ������֧�������ҳ��ʻ�ɫ��������ô�죿
 *���������
 *1�������������ã������������������������
 *2���������������ԣ����µ�¼��ѯ��
 */

public class AlipayConfig {
	
	//�����������������������������������Ļ�����Ϣ������������������������������
	// ���������ID����2088��ͷ��16λ��������ɵ��ַ���
	public static String partner = "2088411488582814";
	
	 /** ����appId  */
    //TODO !!!! ע����appId������Ϊ�������Լ��ķ���id  ����ֻ�Ǹ�����id
    public static final String APP_ID            = "2016080801718601";
    
	
	/**֧��������*/
    public static final String ALIPAY_GATEWAY    = "https://openapi.alipay.com/gateway.do";
	// �̻���˽Կ
	//public static String private_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDL/H9RKsETeGceVjt5Vc3ulilnUEFpjlgJ8pxhyJbEdfOFBJZdVePOfadwgnPW+68GWjq7o9ipz01Cf1akPsT3+0PKOtTxZZX8yYENSozcPhiUydWVEwxH+ON13qGuonkm2Y1nEerjbUOX3wxY8etoI91CEyI8UwqUXD7plIAcBwIDAQAB";
	//public static String private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANSd+DwFRq5hjWcxzMLNJXw1nkRG+c/aXbBse4JEX7UWhYW73bEwxlJ5gpbusxm1iM0wgGGfEBRL0tHCvyFfy5Ymw3pt+V/2kwgH77aSUHJN4HzmpJEtxXTqves2RfTG6ISDJEkzKSCB8jDTNl8YE0dNCu6WVGjx9O9lCRiL3NvxAgMBAAECgYEApwrJOVan1l88OgQtLCsCtVhm1JuyrrOQAgjo7EqNqvdbxdr4bLq2RZvDlpNI0P0H0rW1V30ho+CCbFyFz1G03v9AHtVwb0wVFfnb11C+t6GcR4YQmIWYgI83+fDNUlecnOhZcTcGJNlHm5FP1MbPBbs1UrknrIb4smyVHZUJx/UCQQDuIS06aNZSB4WkaM6WL77FqDGxzwHA2M8bWwt5xx7a9VYKQjbgtjrlLI3wMSdOiPvZ7D/iLVLW1HjZlb8Y+4jTAkEA5JKo0QZDtHaTwtCmKdl4spOF7Z7e+wCYJgtd+tZWBNrFj0AF/9GEuiYfVvgKtqcf0d6ovrYKgNOerUMUvutNqwJBAN5hx04b58KVWb4PTpY9ImiOSVJnIpkJIGjInq/sP6l1ohNUgNFb/SZWdHtSPAYAsUpzcbl0YD9WD0ILsXnQNZcCQAP/NIiDLYfoUukjMOSmct/ciIkBMDD/b6mqpTgPq21mTfIVUoWIqmN+6ylgKP/MqFfJJvY+xKxABxRMif5UqwUCQHFXPdcV4r7S6lR2c9233+x+/SgmQtJfa5x6fmkCKT61AUl1hAGdi1pa4IfNwdXRcVmHocwZK4/rQ8sEJIX0NNk=";
	public static String private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMaRQH6IIotqQkMu8WJlYI0sCFWccbCLpixrUEdppJRYpREAF3Fb6D4MX9t3Ng/b78Gfx5hN/+uJq8IfjoaxUuuVILKl4DdNfs7L3oA9B77gXkjXWHMhp8qkCkIBVzpWFI1M4uNwifmjsG7qnWw3c0aKYOhONCvJ8vOF0vrUOlt9AgMBAAECgYAkIk6QOAnBQQbp3uMCOtyFFBw4KA3wSJlmv+iN9kWC0VbIbzHU6RqsH+hZsane3PEWVRMnPhpyLE1bOA7hp+cksWrxWMY4eCAoXJL0YAuh53qTGXq3E6oQAX3lRQ+dCcm4zRMODJ2zPR1RMceuM+5phU6QyjbNW2o5wTROYFoy6QJBAPJbtGpnYdDmf0+StynIPUaEh9ogZynRQGCihNDTlsqKDRM4A8OwJ/dHhczL3SWRNEJoikjxpGROJSjd3LmlkQsCQQDRvol3BvQA5Wej5LrM7R84pMObVtwMa7DeQs05rr1nE3B+ykaAxANT35D/Z4TGT4PtNhm+LJmjY1qA48hFEiqXAkAzQiwNWSI6EZYZmVk98AjtsjgdbT8EfCpWhej7VdUNr1cGmpFJQSeiyDDHWNLEEEryRLQCq4Duagy38PyvExJzAkEAiXDi4WVkBSZOaL5cjiaf+90z9JnRbi4vYyXBF5hisqWxZNQlqQFI8PAiMsrh3ZWDbLCz48OUFdXoG2en92L1FwJBALHUc+agJaSysvth4X7RLCnj9L6PplIqv0ggnHhLV8K1KlnQ6FlRAaa5EILdln+DyyD8g+LNSO6aXvrgHBo9naU=";

	// ֧�����Ĺ�Կ�������޸ĸ�ֵ
	public static String ali_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

	//public static String ali_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
	//����֧������Կ
	 

	public static String ALIPUBLICKEY4QR  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";
										  //MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB
	//�����������������������������������Ļ�����Ϣ������������������������������
	

	// �����ã�����TXT��־�ļ���·��
	public static String log_path = "D:\\";

	// �ַ������ʽ Ŀǰ֧�� gbk �� utf-8
	public static String input_charset = "utf-8";
	
	// ǩ����ʽ �����޸�
	public static String sign_type = "RSA";

}
