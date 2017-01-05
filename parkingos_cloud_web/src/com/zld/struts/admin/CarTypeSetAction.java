package com.zld.struts.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;


/**
 * �����趨
 * @author Administrator
 *
 */
public class CarTypeSetAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		request.setAttribute("authid", authId);
		if(comid==-1){
			comid = (Long)request.getSession().getAttribute("comid");
		}
		if(comid==null||comid==-1){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			Map<String, Object> comInfoMap = daService.getMap("select car_type from com_info_tb where id =? ", new Object[]{comid});
			request.setAttribute("cartype",comInfoMap.get("car_type"));
			request.setAttribute("comid", request.getParameter("comid"));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from car_type_tb where comid=?  ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			//System.out.println(sqlInfo);
			List list = daService.getAll(sql+" order by sort,id desc",new Object[]{comid});
			int count =0;
			if(list!=null)
				count = list.size();
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){//��ӳ���
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			Integer sort = RequestUtil.getInteger(request, "sort", 0);
			int result=0;
			try {
				Long nextid = daService.getLong(
						"SELECT nextval('seq_car_type_tb'::REGCLASS) AS newid", null);
				result = daService.update("insert into car_type_tb (id,comid,name,sort)" +
							" values(?,?,?,?)",
							new Object[]{nextid,comid,name,sort});
				if(result==1&&publicMethods.isEtcPark(comid)){
					daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_type_tb",nextid,System.currentTimeMillis()/1000,0});
				}
				if(result==1)
					mongoDbUtils.saveLogs(request, 0, 2, "����˳���(�������:"+comid+")��"+name);
			} catch (Exception e) {
				if(e.getMessage().indexOf("car_type_tb_comid_mtype_key")!=-1)
					result=-2;
				//e.printStackTrace();
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			Integer sort = RequestUtil.getInteger(request, "sort", 0);
			int	result = daService.update("update car_type_tb set name =?,sort=? where id=?",
					new Object[]{name,sort,id});
			if(result==1&&publicMethods.isEtcPark(comid)){
				daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_type_tb",id,System.currentTimeMillis()/1000,1});
			}
			if(result==1)
				mongoDbUtils.saveLogs(request, 0, 3, "�޸��˳���(�������:"+comid+")��"+name);
			AjaxUtil.ajaxOutput(response, ""+result);
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map carMap = daService.getMap("select * from  car_type_tb  where id=?",	new Object[]{id});
			int	result = daService.update("delete from  car_type_tb  where id=?",
					new Object[]{id});
			if(result==1&&publicMethods.isEtcPark(comid)){
				daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"car_type_tb",id,System.currentTimeMillis()/1000,2});
			}
			if(result==1)
				mongoDbUtils.saveLogs(request, 0, 4, "ɾ���˳���(�������:"+comid+")��"+carMap);
			AjaxUtil.ajaxOutput(response, ""+result);
		}else if(action.equals("setusecartype")){
			Integer carType = RequestUtil.getInteger(request, "cartype", 0);
			if(carType==0)
				carType=1;
			else {
				carType=0;
			}
			int	result = daService.update("update com_info_tb set car_type =? where id=?",
					new Object[]{carType,comid});
			if(result==1&&publicMethods.isEtcPark(comid)){
				daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_info_tb",comid,System.currentTimeMillis()/1000,1});
			}
			if(result==1)
				mongoDbUtils.saveLogs(request, 0, 3, "�����˳����Ƿ����ִ�С��(�������:"+comid+")��"+carType+",--0:�����֣�1������");
			AjaxUtil.ajaxOutput(response, ""+result);
		}else if(action.equals("getname")){
			Long passid = RequestUtil.getLong(request, "passid", -1L);
			String sql = "select name from car_type_tb where id=?";
			Map<String, Object> map = daService.getMap(sql, new Object[]{passid});
			String name = "";
			if(map != null && map.get("name") != null){
				name = (String)map.get("name");
			}
			AjaxUtil.ajaxOutput(response, name);
		}
		return null;
	}
	
}
