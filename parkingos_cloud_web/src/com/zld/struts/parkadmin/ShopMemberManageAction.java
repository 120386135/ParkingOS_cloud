package com.zld.struts.parkadmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;

public class ShopMemberManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	
	private Logger logger = Logger.getLogger(ShopManageAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}
		Long shop_id = RequestUtil.getLong(request, "shop_id", -1L);
		if(action.equals("")){
			request.setAttribute("shop_id", shop_id);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			String sql = "select * from user_info_tb where state=? and shop_id=? ";
			String sqlcount = "select count(*) from user_info_tb where state=? and shop_id=? ";
			params.add(0);
			params.add(shop_id);
			Long count = daService.getCount(sqlcount, params);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			int result = createMember(request);
			if(result==1)
				AjaxUtil.ajaxOutput(response, "1");
			else {
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("edit")){
			String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
			String strid =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "strid"));
			String phone =RequestUtil.processParams(request, "phone");
			String mobile =RequestUtil.processParams(request, "mobile");
			Integer auth_flag = RequestUtil.getInteger(request, "auth_flag", -1);
			Long id =RequestUtil.getLong(request, "id", -1L);
			Long count = daService.getLong("select count(*) from user_info_tb where mobile=? and auth_flag=? and id<>? ", new Object[]{mobile,auth_flag,id});
			if(count > 0){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String sql = "update user_info_tb set nickname=?,strid=?,phone=?,mobile=?,auth_flag=? where id=? ";
			int result = daService.update(sql, new Object[]{nickname,strid,phone,mobile,auth_flag,Long.valueOf(id)});
			
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("delete")){
			String id =RequestUtil.processParams(request, "selids");
			String sql = "update user_info_tb set state =?  where id =?";
			Object [] values = new Object[]{1,Long.valueOf(id)};
			int result = daService.update(sql, values);
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("editpass")){
			String uin =RequestUtil.processParams(request, "id");
			String sql = "update user_info_tb set password =? ,md5pass=? where id =?";
			String newPass = RequestUtil.processParams(request, "newpass");
			String confirmPass = RequestUtil.processParams(request, "confirmpass");
			String md5pass = newPass;
			try {
				md5pass = StringUtils.MD5(newPass);
				md5pass = StringUtils.MD5(md5pass+"zldtingchebao201410092009");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(newPass.length()<6){
				AjaxUtil.ajaxOutput(response, "���볤��С��6λ�����������룡");
			}else if(newPass.equals(confirmPass)){
				Object [] values = new Object[]{newPass,md5pass,Long.valueOf(uin)};
				int result = daService.update(sql, values);
				AjaxUtil.ajaxOutput(response, result+"");
			}else {
				AjaxUtil.ajaxOutput(response, "�����������벻һ�£����������룡");
			}
			return null;
		}
		return null;
	}
	
	//ע��ͣ�����շ�Ա�ʺ�
		@SuppressWarnings({ "rawtypes" })
		private int createMember(HttpServletRequest request){
			String strid =RequestUtil.processParams(request, "strid");
			String nickname =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "nickname"));
			String phone =RequestUtil.processParams(request, "phone");
			String mobile =RequestUtil.processParams(request, "mobile");
			Long role =RequestUtil.getLong(request, "auth_flag", 15L);//14:������ 15��������Ա
			if(nickname.equals("")) nickname=null;
			if(phone.equals("")) phone=null;
			if(mobile.equals("")) mobile=null;
			Map adminMap = (Map) request.getSession().getAttribute("userinfo");
			Long time = System.currentTimeMillis()/1000;
			if(!checkStrid(strid))
				return 0;
			Long shop_id = RequestUtil.getLong(request, "shop_id", -1L);
			if(shop_id == -1){
				return 0;
			}
			//�û���
			String sql="insert into user_info_tb (nickname,password,strid," +
					"address,reg_time,mobile,phone,auth_flag,shop_id) " +
					"values (?,?,?,?,?,?,?,?,?)";
			Object [] values= new Object[]{nickname,strid,strid,
					adminMap.get("address"),time,mobile,phone,role,shop_id};
			int r = daService.update(sql, values);
			return r;
		}
		
		private boolean checkStrid(String strid){
			String sql = "select count(*) from user_info_tb where strid =?";
			Long result = daService.getLong(sql, new Object[]{strid});
			if(result>0){
				return false;
			}
			return true;

		}
}
