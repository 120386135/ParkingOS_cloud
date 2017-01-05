package com.zld.struts.request;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pay.Constants;

import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.weixinpay.utils.util.Sha1Util;
import com.zld.wxpublic.response.Article;
import com.zld.wxpublic.response.BaseMessage;
import com.zld.wxpublic.response.NewsMessage;
import com.zld.wxpublic.response.TextMessage;
import com.zld.wxpublic.util.CommonUtil;
import com.zld.wxpublic.util.MessageUtil;

public class WeixinPublicServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	DataBaseService dataBaseService =null;
	
	PgOnlyReadService pgOnlyReadService = null;
	
	PublicMethods publicMethods = null;
	
	String TOKEN = "zhenlaidian";
	
	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// ΢�ż���ǩ��
        String signature = request.getParameter("signature");
        // ����ַ���
        String echostr = request.getParameter("echostr");
        // ʱ���
        String timestamp = request.getParameter("timestamp");
        // �����
        String nonce = request.getParameter("nonce");
        String[] str = { TOKEN, timestamp, nonce };
        Arrays.sort(str); // �ֵ�������
        String bigStr = str[0] + str[1] + str[2];
        // SHA1����
        String digest = Sha1Util.getSha1(bigStr);
        // ȷ����������΢��
        if (digest.equals(signature)) {
            response.getWriter().print(echostr);
            System.out.println("����ɹ�������");
        }
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// ��������Ӧ�ı��������ΪUTF-8����ֹ�������룩  
        request.setCharacterEncoding("UTF-8");  
        response.setCharacterEncoding("UTF-8");  
  
        // ���ú���ҵ���������Ϣ��������Ϣ  
        String respMessage = processRequest(request);  
          
        // ��Ӧ��Ϣ  
        PrintWriter out = response.getWriter();  
        out.print(respMessage);  
        out.close(); 
	}
	
	/** 
     * ����΢�ŷ��������� 
     *  
     * @param request 
     * @return 
     */  
    private String processRequest(HttpServletRequest request) {  
        String respMessage = null;  
        try {  
            // Ĭ�Ϸ��ص��ı���Ϣ����  
            String respContent = "";  
  
            // xml�������  
            Map<String, String> requestMap = MessageUtil.parseXml(request);  
  
            // ���ͷ��ʺţ�open_id��  
            String fromUserName = requestMap.get("FromUserName");  
            // �����ʺ�  
            String toUserName = requestMap.get("ToUserName");  
            // ��Ϣ����  
            String msgType = requestMap.get("MsgType");  
  
            // �ظ��ı���Ϣ  
            TextMessage textMessage = new TextMessage();  
            textMessage.setToUserName(fromUserName);  
            textMessage.setFromUserName(toUserName);  
            textMessage.setCreateTime(new Date().getTime());  
            textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);  
            
            Map<String, Object> userMap = pgOnlyReadService.getMap(
					"select * from user_info_tb where wxp_openid=? and state=? ",
					new Object[] { fromUserName, 0 });
            String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3a%2f%2f"+Constants.WXPUBLIC_REDIRECTURL+"%2fzld%2fwxpaccount.do&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
            // �ı���Ϣ  
            if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {  
                String content = requestMap.get("Content");
                if(content.contains("��")){
                	StringBuffer buffer = new StringBuffer();
                	buffer.append("4.20���𽱽𷢷Ź���������£�").append("\n");
                	buffer.append("һ�Ƚ����Ƚ���������������������ͨ���շ�Ա���־��� ����ͣ������ѡ���Ÿ�˭��").append("\n");
                	buffer.append("��ѡ��Ҫ���������������շ�Ա����̬�ȣ�����֧�����ȣ�ÿ��΢�Ź��ںŻᷢ�Ż�������").append("\n\n");
                	buffer.append("4.20������ֹ���������£�").append("\n");
                	buffer.append("������֧���������ָ�Ϊÿ��0.01�֡�").append("\n");
                	buffer.append("����֧������һԪ���ָ�Ϊÿ��2�֡�").append("\n\n");
                	buffer.append("�շ�Ա�Ƽ����������Ѿ��������£�").append("\n");
                	buffer.append("�Ƽ��ĳ������һԪ����֧�Ųŷ���5Ԫ�Ƽ�������");
					textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
                if(content.contains("��������")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "�շ�Ա����������顾7.13�ա�");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3FmEYU4ibrwQicTnnFTIudHZhY5aN7CjIw6GDbIcaLSja8PWPicrP3H9dCqxYCZNibK2jutH4mq1ianhlw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208349486&idx=1&sn=a59b4c5250cc4ca34941750d1bffc925#rd");
            		map.put("descp", "�շ�Ա����������顣");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
                    return respMessage;
                }
                if(content.contains("��Ϸ")){
                	StringBuffer buffer = new StringBuffer();
                	if(userMap == null){
                		buffer.append("<a href=\""+url+"\">���ע��</a>");
                	}else{
                		String url_game = "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/cargame.do?action=playagin&uin="+userMap.get("id");
                		buffer.append("<a href=\""+url_game+"\">���������Ϸ</a>");
                	}
                	textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
                
                if(content.contains("�����")){
                	/*StringBuffer buffer = new StringBuffer();
					int r = dataBaseService
							.update("update user_info_tb set wxp_openid=null where wxp_openid=? ",
									new Object[] { fromUserName });
					buffer.append("���ã��󶨹�ϵ�Ѿ����");
					textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;*/
                }
                
                if(content.contains("����")){
                	StringBuffer buffer = new StringBuffer();
                	buffer.append("�ظ������ѻ᡿���μ� VIP�����ѻ�").append("\n\n");
                	buffer.append("�ظ�����ͨ�������˽���ͨ������").append("\n\n");
                	buffer.append("�ظ�����һ�������ͣ��ȯ׬���").append("\n\n");
                	buffer.append("�ظ������ء�������ͣ����APP");
                	textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
                
                if(content.contains("���ѻ�")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "��Ȥ�����桢���Żݣ�����ͣ�������ѻᣬ�ȴ����ļ��룡");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3Fr5wJQc0VltxVc4St3dPIXWn7ect1hXNKUgRoCns0TSyZWmRPhhEuYsZ5faY5ZuhwVPibzurL8LgA/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=207160377&idx=1&sn=b987bf7393c5b029289ff7ac5111d34c#rd");
            		map.put("descp", "��VIP���ѻ���������ͣ�������ѻᣬ��ͣ���������û�֮��Ľ���ƽ̨��Ҳ��ͣ�����Żݻ����Ϣ����ƽ̨��");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
                    return respMessage;
                }
                
                if(content.contains("��ͨ��")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "����ͨ�����û��������˽����~");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3Fr5wJQc0VltxVc4St3dPIXWKT0qzLlU14wy2gibJ2NBNbze27vhgiaE9XGQIFK9BR1VBNurZrH9IAQ/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=207347884&idx=1&sn=22adb0b358047eb1e9c14c0c1eeb36b9#rd");
            		map.put("descp", "1��ͨ��������������ͨ��������ʹ��NFC�ֻ�ˢ����ͣ������ΪVIP�����ṩ�Ĵ�ֵ��Ա����2��ͨ����ʲô�ã�");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("���Ͳ���")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "�����˽���Ͳ���~");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3Fr5wJQc0VltxVc4St3dPIXiaNyYM1mojicQ390U9DIMJohHCUicylZicXs6QMy6ch5eaIysXuFnJ2icdw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=207349012&idx=1&sn=1bb40999f213b15a1a54008dcab89b31#rd");
            		map.put("descp", "1�����Ͳ����������������������Ͳ�������һ�㳣���ڸ߼��Ƶꡢ�����������ȵط�������Щ����ΪVIP�û����ض�");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("����")){
                	StringBuffer buffer = new StringBuffer();
                	url = "http://a.app.qq.com/o/simple.jsp?pkgname=com.tq.zld";
                	buffer.append("<a href=\""+url+"\">�������App</a>");
                	textMessage.setContent(buffer.toString());
					respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
                
                if(content.contains("ˢ��")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "7.17��ˢ��ͨ��");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GjAv77GPICy1kDO4kRuo6EUFCNqB3mocta2EViaziaSTjJAgia9sEbpryTAS7nproBZHIvuM0QlHwVQ/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208451367&idx=1&sn=a89626c3c1c19fffce3b5c67d032e21e#rd");
            		map.put("descp", "7��16�տ�ʼ���м��ٲ��ֳ������ֿ�ʼ����ˢ������һ�Σ�ͣ������Ϊ����ˢ�������൱�����졣��Ϊ���ֳ�����ˢ��");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("����")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "7����Ѯ���ߣ����ͻ�������ְ�&����ר��ȯ");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GjAv77GPICy1kDO4kRuo6ERiaySfAhWYz2sSEmKSglfWysDhzq9LJPeFoJcw5ovhicvcnf5bjC7icKQ/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208445618&idx=1&sn=b4d99d5233921ae53c847165c62dec2b#rd");
            		map.put("descp", "���ֵ���;����Դ��1�����ֵ���;��        ���ֿ��������һ�����ר��ȯ������������ 2�����ֵ���Դ��");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("����")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "�����շ�Ա");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3ErwxRu6Nic9klguqWd4YkMzo0ibC0ewM7S0MRDPjWLaeqq3hztp4lkQ1Q0Hv1uqkDTxjlODsgfYOBw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208550167&idx=1&sn=efa9c7fbafc28635324946590380e2ae#rd");
            		map.put("descp", "������ˣ���Ӧ�õ����ࡣ������");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("��֤")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "һ�󲨸�������������ͣ�������������֤");
            		map.put("picurl", "http://mmbiz.qpic.cn/mmbiz/zg069SDrV3EyRX6J6icAOptJ08OZj1GkibEXKXbWAOy83OibYQsibNOYMfib2icVUuUJSHHC95XicTGn2L5e1pU2Z1Jxw/640?wx_fmt=jpeg&tp=webp&wxfrom=5");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208679773&idx=1&sn=43d1fe06680c90efb11444f8b72bdff2#rd");
            		map.put("descp", "һ�󲨸�������������ͣ�������������֤��");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("ͣ��ȯ") || content.equals("ͣ����")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "ͣ����ͣ��ȯʹ��˵����");
            		map.put("picurl", "http://mmbiz.qpic.cn/mmbiz/zg069SDrV3EyRX6J6icAOptJ08OZj1GkibB79AmfDuWicE1vu3icU5hOyQ3yGV9RWcsrrqob4B1YFeJdSGK2sXk7VA/640?wx_fmt=jpeg&tp=webp&wxfrom=5");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208427587&idx=1&sn=6cec3794e585e4d31b5079f919b01614#rd");
            		map.put("descp", "ͣ��ȯʹ��˵����");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("���ö��")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "ͣ�������ö��˵����");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3EyRX6J6icAOptJ08OZj1Gkib8xgyTgvzjbSJ9t24rmoSicxDUOvO7wIBbZwXmhyhWz1Q4oK9jTvxXjQ/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208427120&idx=1&sn=6cb6719bf1520ef5a72097fe5c7fe56a#rd");
            		map.put("descp", "���ö�ȣ����ö�Ƚ������г��Ƶģ���ʱ����ʹ�á�");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("��һ�") || content.equals("��ɻ�")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "ͣ����������һ�����ȯ׬���");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3FautOBMESjlIias5CGLnj3R8LakMXaFibcKIR1k7FRGrCJaazVDWuzrnHfbgG1BbyHlPVrRFNVmh2A/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209080514&idx=1&sn=a0951330910bf2c4e41fca840a562d7e&ptlang=2052&ADUIN=2285180450&ADSESSION=1439946886&ADTAG=CLIENT.QQ.5425_.0&ADPUBNO=26509#rd");
            		map.put("descp", "����˭�Ļһ������ܼ�˭Ϊ�û��ѡ����� ���������ͣ��ȯ����򡣡���");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("�ڶ���")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "�ʸ��ڵڶ������ߺ���á�����");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3HUvy5xt9VREHgyNTVQYTuc1LRxmxU3vibrQVYTw2cO4NfkXKMLH5pWmVYJvXErZk5zTNfDxSEBIew/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209280360&idx=1&sn=c84f017607c19f6bbb0870ca7706fee2#rd");
            		map.put("descp", "���е��ʸ��������ڣ��ڶ������ߺ��ʹ�á� ps:�ڶ��ػ��ڲ߻��С������������ָ�㡣������");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("�˿�") || content.equals("����")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "���ڳ����˻�����˵��");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3HZL7EuRDahAt6DUjWnlCBD7nQ9aFPkichorcoRTZEVDStZEjibiczCvcSO7MfZRQTibQyhR14LQPyh5Q/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209376960&idx=1&sn=369c4bea18d70d656c4f3e30b86cc843#rd");
            		map.put("descp", "���ڳ����˻�����˵��");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("�ǹ���ͣ��ȯ")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "�ǹ���ͣ��ȯ�������");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GL94m1emzoHNA2gibgRAgNKJRURHLkbrZwaoRITwzRW5yHfx3WQt6Yz02uyyphMkSuTU2DbbpHpicA/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209488541&idx=1&sn=1b21a9f433bf0f7cc873f742e799b08b#rd");
            		map.put("descp", "���ڣ���������ͨ����һ����12Ԫȯ����ϳ���ˢ���ͣ��ȯ��6Ԫ���µ�ͣ���ѣ�ȴ����֧��13Ԫ����ʹ��12Ԫ��ͣ��ȯ������ͣ��ȯʹ�ù��򽫵�����");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("�������")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "ͣ��������ȫ�����");
            		map.put("picurl", "http://mmbiz.qpic.cn/mmbiz/zg069SDrV3EP4Vk0FK42CT3MtNaWhSPpJJh0Es7ibrW2urAJiaGDrkaaNmILlGLJgkkBhdAiarGFbfKY4gB2zahHw/640?wx_fmt=jpeg&tp=webp&wxfrom=5");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209615004&idx=1&sn=f94fd688b80e71f944efe61b60f7700b#rd");
            		map.put("descp", "����O2O����ģʽ������Ͷ���˺��г��ر𿴺á� ���ԣ�ͣ��������ȫ�������");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("���͹���")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "���͹������");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3HwfpcpDUGD5OjXicxXJbaDdUtRWeKzCvJTsiaJWf3mjyrumlH4LqxJTOa0HegXujCcvEEwQib2q9Giaw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209684836&idx=1&sn=99540a278fccd17750f5591a1330c443#rd");
            		map.put("descp", "��ʱ�������͹���");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("����")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "ͣ�����������ģʽ");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3H9qRCAn04s14RMRB7jzHwQ4H73qg08B3FXtanjjpr16Q9VsicVgKDuPVpgLQesgln37jxtPpDorJw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209755973&idx=1&sn=e28c267e19e14c2c099e79b1c848abaa#rd");
            		map.put("descp", "�������������������֣�Ӧ��ѧ�ᴢ�������������ڹ�������ů��������������");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("ʮ��Ԥ��")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "�������&ͣ����10���¹���Ԥ��");
            		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3HHpYTzklnRygibjxPHxmRy8VY3679ekRibtdsyibsOLPcNbgjvvHy54RruxH4valO6kQWOKtks8Frdw/0?wx_fmt=jpeg");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209772715&idx=1&sn=25ff40c14225fcf23e754185c2c242e4#rd");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.equals("��������")){
                	List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            		Map<String, String> map = new HashMap<String, String>();
            		map.put("title", "��������������");
            		map.put("picurl", "http://mmbiz.qpic.cn/mmbiz/zg069SDrV3GMhiaw92auib7JeSmwuX5qLia253foPYUMBCOMI4qVpx4G3JA769oiaNS5Nz72icCtSClZsnNCenDnozQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5");
            		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209867877&idx=1&sn=ad453eaddeeafe30a18188ddcca9f6bd#rd");
            		map.put("descp", "�������г�����5000������� ���Ǹ��ݳ��������������䡣");
            		list.add(map);
            		respMessage = articleMessage(list,fromUserName,toUserName);
            		return respMessage;
                }
                
                if(content.contains("��ϵ�ͷ�")){
                	System.out.println("to kefu openid:"+fromUserName);
                	BaseMessage kefuMessage = new BaseMessage();  
                	kefuMessage.setToUserName(fromUserName);  
                	kefuMessage.setFromUserName(toUserName);  
                	kefuMessage.setCreateTime(new Date().getTime());  
                	kefuMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_KEFU); 
                	
                	respMessage = MessageUtil.baseMessageToXml(kefuMessage);
					return respMessage;
                }
                
                if(content.contains("�һس���") || content.contains("��֤��") || content.contains("ͣ����")){
                	System.out.println("to kefu openid:"+fromUserName);
            		
            		boolean online = true;
        			String access_token = publicMethods.getWXPAccessToken();
        			String urlString = "https://api.weixin.qq.com/cgi-bin/customservice/getonlinekflist?access_token="+access_token;
        			String result = CommonUtil.httpsRequest(urlString, "GET", null);
        			JSONObject jsonObject = JSONObject.fromObject(result);
        			if(jsonObject != null && jsonObject.get("errcode") == null){
        				String kefulist = jsonObject.getString("kf_online_list");
        				JSONArray jsonArray = JSONArray.fromObject(kefulist);
        				if(jsonArray.size() > 0){
        					List<Object> stateList = new ArrayList<Object>();
        					for(int i=0; i<jsonArray.size();i++){
        						JSONObject jObject = jsonArray.getJSONObject(i);
        						int state = jObject.getInt("status");
        						if(state == 2){
        							stateList.add(state);
        						}
        					}
        					if(stateList.size() == jsonArray.size()){
        						online = false;
        					}
        				}else{
        					online = false;
        				}
        			}
            		if(online){
            			BaseMessage kefuMessage = new BaseMessage();  
                    	kefuMessage.setToUserName(fromUserName);  
                    	kefuMessage.setFromUserName(toUserName);  
                    	kefuMessage.setCreateTime(new Date().getTime());  
                    	kefuMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_KEFU); 
                    	
                    	respMessage = MessageUtil.baseMessageToXml(kefuMessage);
            		}else{
            			textMessage.setContent("��Ǹ���ͷ������ߣ�����ʱ�䣺������9:30-17:30�������Ժ���ѯ��лл��");
            			respMessage = MessageUtil.textMessageToXml(textMessage);
            		}
                	
					return respMessage;
                }
            }  
            // ͼƬ��Ϣ  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {  
//                respContent = "�����͵���ͼƬ��Ϣ��";  
            }  
            // ����λ����Ϣ  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {  
            	StringBuffer buffer = new StringBuffer();
            	String location_X = requestMap.get("Location_X");
            	String location_Y = requestMap.get("Location_Y");
            	String label = requestMap.get("Label");
//            	respContent = buffer.toString();
            }  
            // ������Ϣ  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {  
//                respContent = "�����͵���������Ϣ��";  
            }  
            // ��Ƶ��Ϣ  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {  
//                respContent = "�����͵�����Ƶ��Ϣ��";  
            }  
            // �¼�����  
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {  
                // �¼�����  
                String eventType = requestMap.get("Event");
                System.out.println("wxpublic event >>>openid:"+fromUserName+",eventType:"+eventType);
                // ����  
                if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {  
                	StringBuffer buffer = new StringBuffer();
                	if(userMap == null){
                		buffer.append("<a href=\""+url+"\">���ע��</a>��������ȡ10Ԫ���");
                	}else{
                		buffer.append("һ����֤�û�ר����������Ϯ���ظ�����֤���˽����顣").append("\n\n");
                		buffer.append("�ظ���ͣ��ȯ���˽�ͣ��ȯʹ������").append("\n\n");
                		buffer.append("�ظ�����Ϸ������Ϸ����С��ͣ��ȯ��").append("\n\n");
                		buffer.append("�ظ������ࡱ���˽���ࡣ");
                	}
                	respContent = buffer.toString();  
                }  
                // ȡ������  
                else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {  
                    // TODO ȡ�����ĺ��û����ղ������ںŷ��͵���Ϣ����˲���Ҫ�ظ���Ϣ  
                }  
                // �Զ���˵�����¼�  
                else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {  
                	String key = requestMap.get("EventKey");
                	if(key.equals("aboutus")){
                		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
                		Map<String, String> map = new HashMap<String, String>();
                		map.put("title", "��ÿλ����,��������ͣ��");
                		map.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GyKvMNS7wzicsHafr1u1JzSvkxXpR9K3pldhT3GYDpmcicN2AAeYBEJcTxpQxXBd9nBE1h4s7a44AQ/0?wx_fmt=jpeg");
                		map.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=205960173&idx=1&sn=3750c6db17c115041a77eef72988a53a#rd");
                		list.add(map);
                		Map<String, String> map1 = new HashMap<String, String>();
                		map1.put("title", "��Ҫ����,������");
                		map1.put("picurl", "https://mmbiz.qlogo.cn/mmbiz/zg069SDrV3GyKvMNS7wzicsHafr1u1JzS3Rt0iaQ3hH1N82apWOZdlic3KWicZpXicvwuse8MDew1dzHZ9Asa8mNibWw/0?wx_fmt=jpeg");
                		map1.put("url", "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=205960173&idx=2&sn=de3da5f5af2504c66cdf56344b9170fe#rd");
                		list.add(map1);
                		respMessage = articleMessage(list,fromUserName,toUserName);
                        return respMessage;
                	}else if(key.equals("kefu")){
                		StringBuffer buffer = new StringBuffer();
            			buffer.append("�������⽫��ת�ӵ��˹��ͷ�:").append("\n\n");
                    	buffer.append("�ظ����һس��ơ���������Ʊ�����ע�������;").append("\n\n");
                    	buffer.append("�ظ�����֤�롿�������֤���ղ���������;").append("\n\n");
                    	buffer.append("�ظ���ͣ���ѡ������ͣ����֧������;");
            			
            			textMessage.setContent(buffer.toString());
            			respMessage = MessageUtil.textMessageToXml(textMessage);
            			return respMessage;
                	}
                }  
                //�ϱ�����λ���¼�
                else if(eventType.equals(MessageUtil.EVENT_TYPE_LOCATION)){
                	StringBuffer buffer = new StringBuffer();
                	String latitude = requestMap.get("Latitude");
                	String longitude = requestMap.get("Longitude");
                	String precision = requestMap.get("Precision");
//                	respContent = buffer.toString();
                }
                //����˵���ת�����¼�����
                else if(eventType.equals(MessageUtil.EVENT_TYPE_VIEW)){
//                	respContent = "���ǲ˵���ת����";
                }
                //�ͷ��رջỰ����
                else if(eventType.equals(MessageUtil.EVENT_TYPE_KF_CLOSE_SESSION)){
                	System.out.println("kf_close_session>>>openid:"+fromUserName);
                	StringBuffer buffer = new StringBuffer();
                	buffer.append("�˴λỰ�ѽ�����ף��������죬лл��");
                	textMessage.setContent(buffer.toString());
                	respMessage = MessageUtil.textMessageToXml(textMessage);
					return respMessage;
                }
            }  
            if(respContent.equals("")){
            	StringBuffer buffer = new StringBuffer();
            	
            	if(userMap == null){
            		buffer.append("<a href=\""+url+"\">���ע��</a>��������ȡ10Ԫ���");
            	}else{
            		buffer.append("һ����֤�û�ר����������Ϯ���ظ�����֤���˽����顣").append("\n\n");
            		buffer.append("�ظ���ͣ��ȯ���˽�ͣ��ȯʹ������").append("\n\n");
            		buffer.append("�ظ�����һ�������ͣ��ȯ׬��").append("\n\n");
            		buffer.append("�ظ������ࡱ���˽���ࡣ");
            	}
            	respContent = buffer.toString();  
            }
            textMessage.setContent(respContent);  
            respMessage = MessageUtil.textMessageToXml(textMessage);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
  
        return respMessage;  
    }  
	
    private String articleMessage(List<Map<String, String>> list, String fromUserName, String toUserName){
    	List<Article> articleList = new ArrayList<Article>(); 
    	String respMessage = null; 
    	// ����ͼ����Ϣ  
        NewsMessage newsMessage = new NewsMessage();  
        newsMessage.setToUserName(fromUserName);  
        newsMessage.setFromUserName(toUserName);  
        newsMessage.setCreateTime(new Date().getTime());  
        newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);  
        newsMessage.setFuncFlag(0);
        for(Map<String, String> map : list){
        	Article article = new Article();  
        	article.setTitle(map.get("title"));   
            article.setPicUrl(map.get("picurl"));  
            article.setUrl(map.get("url"));
            article.setDescription(map.get("descp"));
            articleList.add(article);
        }
        // ����ͼ����Ϣ����  
        newsMessage.setArticleCount(articleList.size());  
        // ����ͼ����Ϣ������ͼ�ļ���  
        newsMessage.setArticles(articleList);  
        // ��ͼ����Ϣ����ת����xml�ַ���  
        respMessage = MessageUtil.newsMessageToXml(newsMessage); 
    	return respMessage;
    }
    
	@Override
	public void init() throws ServletException {
		ApplicationContext ctx = WebApplicationContextUtils
				.getWebApplicationContext(getServletContext());
		dataBaseService= (DataBaseService) ctx.getBean("dataBaseService");
		pgOnlyReadService = (PgOnlyReadService)ctx.getBean("pgOnlyReadService");
		pgOnlyReadService = (PgOnlyReadService)ctx.getBean("pgOnlyReadService");
		publicMethods = (PublicMethods)ctx.getBean("publicMethods");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}
}
