package com.zld.struts.parkadmin.menuforword;

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
public class EpayMenuForwordAction extends Action{
	
	

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setAttribute("authid", request.getParameter("authid"));
		return mapping.findForward("parkepay");
	}

	
}