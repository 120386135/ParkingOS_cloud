package com.zld.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class ZldXMLUtils {
	/**
	 * 
	 * @author Laoyao ����XML�ĵ������XML�ĵ�
	 */

	public void createXml(String fileName) {
		Document document = DocumentHelper.createDocument();
		Element employees = document.addElement("employees");
		Element employee = employees.addElement("employee");
		Element name = employee.addElement("name");
		name.setText("ddvip");
		Element sex = employee.addElement("sex");
		sex.setText("m");
		Element age = employee.addElement("age");
		age.setText("29");
		try {
			Writer fileWriter = new FileWriter(fileName);
			XMLWriter xmlWriter = new XMLWriter(fileWriter);
			xmlWriter.write(document);
			xmlWriter.close();
		} catch (IOException e) {

			System.out.println(e.getMessage());
		}

	}

	public static Map<String, Object> parserStrXml(String xmlStr) {
		//<ROOT><RET>0</RET><ZDName>ְ����</ZDName><JDInfo>119.382417</JDInfo>
		//<WDInfo>32.39void6822</WDInfo><ZDAddr>���··��</ZDAddr><TOTALCWS>32</TOTALCWS>
		//<LEFTCWS>29</LEFTCWS></ROOT>
		//xmlStr="<ROOT><RET>0</RET><ZDName>ְ����</ZDName><JDInfo>119.382417</JDInfo><WDInfo>32.396822</WDInfo><ZDAddr>���··��</ZDAddr><TOTALCWS>32</TOTALCWS><LEFTCWS>29</LEFTCWS></ROOT>";
		Map<String, Object> result = new HashMap<String, Object>();
		Document document = null;
		try {
			document = DocumentHelper.parseText(xmlStr); // ���ַ���תΪXML
			Element employees = document.getRootElement();
			for (Iterator i = employees.elementIterator(); i.hasNext();) {
				Element employee = (Element) i.next();
				String name = employee.getName();
				Object object=employee.getData();
				result.put(name.toLowerCase(), object);
			}
		} catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
		//System.err.println(result);
		return result;
//		System.out.println("dom4j parserXml");
	}
	public static void main(String[] args) {
		String string ="<xml><appid><![CDATA[wx485c58b62cbb4dd0]]></appid><attach><![CDATA[18101333937_0_yangzhou]]></attach><bank_type><![CDATA[CFT]]></bank_type><cash_fee><![CDATA[1]]></cash_fee><fee_type><![CDATA[CNY]]></fee_type><is_subscribe><![CDATA[N]]></is_subscribe><mch_id><![CDATA[1332937401]]></mch_id><nonce_str><![CDATA[2996962656838a97af4c5f926fe6f1b0]]></nonce_str><openid><![CDATA[oETT-w27Rb51THrC40CA7dQ1I4H8]]></openid><out_trade_no><![CDATA[a098c2bdee540174f426981175c6fa76]]></out_trade_no><result_code><![CDATA[SUCCESS]]></result_code><return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[270A06203952F16FEFA00289508B2823]]></sign><time_end><![CDATA[20160430143327]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[APP]]></trade_type><transaction_id><![CDATA[4006662001201604305371742366]]></transaction_id></xml>";
		System.err.println(parserStrXml(string));
	}
	public void parserFileXml(String fileName) {
		File inputXml = new File(fileName);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element employees = document.getRootElement();
			for (Iterator i = employees.elementIterator(); i.hasNext();) {
				Element employee = (Element) i.next();
				for (Iterator j = employee.elementIterator(); j.hasNext();) {
					Element node = (Element) j.next();
					System.out.println(node.getName() + ":" + node.getText());
				}

			}
		} catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("dom4j parserXml");
	}
	
	public void readStringXml(String xml) {
        Document doc = null;
        try {

            // ��ȡ������XML�ĵ�

            // SAXReader����һ���ܵ�����һ�����ķ�ʽ����xml�ļ�������

            // SAXReader reader = new SAXReader(); //User.hbm.xml��ʾ��Ҫ������xml�ĵ�

            // Document document = reader.read(new File("User.hbm.xml"));

            // �������ͨ������xml�ַ�����

            doc = DocumentHelper.parseText(xml); // ���ַ���תΪXML


            Element rootElt = doc.getRootElement(); // ��ȡ���ڵ�

            System.out.println("���ڵ㣺" + rootElt.getName()); // �õ����ڵ������


            Iterator iter = rootElt.elementIterator("head"); // ��ȡ���ڵ��µ��ӽڵ�head


            // ����head�ڵ�

            while (iter.hasNext()) {

                Element recordEle = (Element) iter.next();
                String title = recordEle.elementTextTrim("title"); // �õ�head�ڵ��µ��ӽڵ�titleֵ

                System.out.println("title:" + title);

                Iterator iters = recordEle.elementIterator("script"); // ��ȡ�ӽڵ�head�µ��ӽڵ�script


                // ����Header�ڵ��µ�Response�ڵ�

                while (iters.hasNext()) {

                    Element itemEle = (Element) iters.next();

                    String username = itemEle.elementTextTrim("username"); // �õ�head�µ��ӽڵ�script�µ��ֽڵ�username��ֵ

                    String password = itemEle.elementTextTrim("password");

                    System.out.println("username:" + username);
                    System.out.println("password:" + password);
                }
            }
            Iterator iterss = rootElt.elementIterator("body"); ///��ȡ���ڵ��µ��ӽڵ�body

            // ����body�ڵ�

            while (iterss.hasNext()) {

                Element recordEless = (Element) iterss.next();
                String result = recordEless.elementTextTrim("result"); // �õ�body�ڵ��µ��ӽڵ�resultֵ

                System.out.println("result:" + result);

                Iterator itersElIterator = recordEless.elementIterator("form"); // ��ȡ�ӽڵ�body�µ��ӽڵ�form

                // ����Header�ڵ��µ�Response�ڵ�

                while (itersElIterator.hasNext()) {

                    Element itemEle = (Element) itersElIterator.next();

                    String banlce = itemEle.elementTextTrim("banlce"); // �õ�body�µ��ӽڵ�form�µ��ֽڵ�banlce��ֵ

                    String subID = itemEle.elementTextTrim("subID");

                    System.out.println("banlce:" + banlce);
                    System.out.println("subID:" + subID);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}
