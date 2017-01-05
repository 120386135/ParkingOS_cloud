package com.zld.tcp.client;
import java.security.MessageDigest;

public class SHA1 {

	/**
	 * 
	 * @param str ��Ҫ���ܵ�����
	 */
	public  String sha1(String str){
		try {
			// SHA1ǩ������
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] digest = md.digest();

			StringBuffer hexstr = new StringBuffer();
			String shaHex = "";
			for (int i = 0; i < digest.length; i++) {
				shaHex = Integer.toHexString(digest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexstr.append(0);
				}
				hexstr.append(shaHex);
			}
			return hexstr.toString();
		} catch (Exception e) {
		}
		return null;
	}
}