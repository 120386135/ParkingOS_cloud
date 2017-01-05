/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package pay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;


/**
 * API���ÿͻ��˹���
 * 
 * @author taixu.zqq
 * @version $Id: AlipayAPIClientFactory.java, v 0.1 2014��7��23�� ����5:07:45 taixu.zqq Exp $
 */
public class AlipayAPIClientFactory {

    /** API���ÿͻ��� */
    private static AlipayClient alipayClient;
    
    /**
     * ���API���ÿͻ���
     * 
     * @return
     */
    public static AlipayClient getAlipayClient(){
        
        if(null == alipayClient){
            alipayClient = new DefaultAlipayClient(AlipayConfig.ALIPAY_GATEWAY, AlipayConfig.APP_ID, 
            		AlipayConfig.private_key, "json", AlipayConfig.input_charset,AlipayConfig.ALIPUBLICKEY4QR);
        }
        return alipayClient;
    }
}
