package com.zld.struts.admin.menuforword;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
/**
 * ����֧���˵�
 * @author Administrator
 *
 */
public class MenuForwordAction extends Action{
	
	

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String uri = request.getRequestURI();
		request.setAttribute("authid", request.getParameter("authid"));
		if(uri.indexOf("devicemenu")!=-1){
			request.setAttribute("menuname","�豸����");
		}else if(uri.indexOf("syssetmenu")!=-1){
			request.setAttribute("menuname","�ۺ�����");
		}else if(uri.indexOf("authmenu")!=-1){
			request.setAttribute("menuname","Ȩ�޹���");
		}else if(uri.indexOf("anlysismenu")!=-1){
			request.setAttribute("menuname","ͳ�Ʒ���");
		}else if(uri.indexOf("parkepaymenu")!=-1){
			request.setAttribute("menuname","����֧��");
		}else if(uri.indexOf("parkordermenu")!=-1){
			request.setAttribute("menuname","��������");
		}else if(uri.indexOf("parkmanagemenu")!=-1){
			request.setAttribute("menuname","ϵͳ����");
		}else if(uri.indexOf("membermanage")!=-1){
			request.setAttribute("menuname","Ա��Ȩ��");
		}else if(uri.indexOf("carplate")!=-1){
			request.setAttribute("menuname","�豸����");
		}else if(uri.indexOf("parkanlysis")!=-1){
			request.setAttribute("menuname","ͳ�Ʒ���");
		}else if(uri.indexOf("logomanage")!=-1){
			request.setAttribute("menuname","ϵͳ����");
		}else if(uri.indexOf("sysmanage") != -1){
			request.setAttribute("menuname","ϵͳ����");
		}else if(uri.indexOf("vipmanage") != -1){
			request.setAttribute("menuname","��Ա����");
		}else if(uri.indexOf("citycommand") != -1){
			request.setAttribute("menuname","ָ������");
		}else if(uri.indexOf("inducemenu") != -1){
			request.setAttribute("menuname","�յ�����");
		}else if(uri.indexOf("paymenu") != -1){
			request.setAttribute("menuname","֧������");
		}else if(uri.indexOf("cityanlysis") != -1){
			request.setAttribute("menuname","���߷���");
		}
		return mapping.findForward("menu");
	}

	
}