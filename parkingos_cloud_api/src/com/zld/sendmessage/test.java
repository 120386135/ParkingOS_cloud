package com.zld.sendmessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

//��Demo��Ҫ���Java��Linux�Ȼ������������
public class test {
	public static void main(String[] args) throws UnsupportedEncodingException{
		//����������кź�����
				String sn="SDK-CSL-010-00270";//�������Լ������к�
				String pwd="016829";//�������Լ�������
				String mobiles="18101333937";
				String content=URLEncoder.encode("������֤����2209 ���������ݡ�", "utf8");
				
		
				Client client=new Client();
				String result_mt = client.mdSmsSend_u(mobiles, content, "", "", "");
				if(result_mt.startsWith("-")||result_mt.equals(""))//���Ͷ��ţ�������Ը��ſ�ͷ���Ƿ���ʧ�ܡ�
				{
					System.out.print("����ʧ�ܣ�����ֵΪ��"+result_mt+"��鿴webservice����ֵ���ձ�");
					return;
				}
				//������ر�ʶ��ΪС��19λ��������String���͵ġ���¼�����͵����Ρ�
				else
				{
					System.out.print("���ͳɹ�������ֵΪ��"+result_mt);
				}
		
		
	
	}
}
