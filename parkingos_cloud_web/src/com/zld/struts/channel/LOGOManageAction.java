package com.zld.struts.channel;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.zld.AjaxUtil;
import com.zld.impl.MongoClientFactory;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;

public class LOGOManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	
	private Logger logger = Logger.getLogger(LOGOManageAction.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//��¼���û�id
		Long chanid = (Long)request.getSession().getAttribute("chanid");
		request.setAttribute("authid", request.getParameter("authid"));
		logger.error("���������̨>>>chanid:"+chanid+",action:"+action+",uin:"+uin);
		if(uin==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("cloud")){
			request.setAttribute("type", 0);
			return mapping.findForward("list");
		}else if(action.equals("querycloud")){
			String sql = "select * from logo_tb where type=? and orgid=? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map> list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(chanid);
			list = daService.getAllMap(sql, params);
			int count = 0;
			if(list != null){
//				setLogo(list, 0);
				count = list.size();
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("uploadpic")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String table = RequestUtil.getString(request, "table");
			Integer type = RequestUtil.getInteger(request, "type", 0);//0:��ʾ���� 
			Integer logotype = RequestUtil.getInteger(request, "logotype", 1);
			logger.error("���������̨�ϴ�logo>>chanid:"+chanid+",id:"+id+",table:"+table+",type:"+type+",logotype:"+logotype);
			if(id!=-1&&!"".equals(table)){
				String picurl = uploadPicToMongodb(request, id, table, type, logotype);
				logger.error("���������̨�ϴ�logo>>chanid:"+chanid+",id:"+id+",picurl:"+picurl);
				int ret = 0;
				if(picurl!=null&&!"".equals(picurl)){
					if(logotype == 1){//������̨�ϴ�logo
						ret = daService.update("update logo_tb set url_fir=? where id=? ", new Object[]{picurl,id});
					}else if(logotype == 2){
						ret = daService.update("update logo_tb set url_sec=? where id=? ", new Object[]{picurl,id});
					}
				}
				logger.error("���������̨�ϴ�logo>>chanid:"+chanid+",id:"+id+",ret:"+ret);
				if(ret==1)
					request.setAttribute("result", "�ϴ��ɹ�����رյ�ǰ����!");
				else 
					request.setAttribute("result", "�ϴ�ʧ��!");
			}else {
				request.setAttribute("result", "�ϴ�ʧ��!");
			}
			return mapping.findForward("uploadret");
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "logoname"));
			Integer type = RequestUtil.getInteger(request, "type", 0);
			logger.error("���������̨���LOGO��¼>>chanid:"+chanid+",name:"+name+",type:"+type);
			if(chanid != null && chanid > 0){
				Long count = daService.getLong("select count(*) from logo_tb where type=? and orgid=? ", new Object[]{type, chanid});
				logger.error("���������̨���LOGO��¼>>chanid:"+chanid+",count:"+count);
				if(count > 0){
					AjaxUtil.ajaxOutput(response, "2");
					return null;
				}
				int r = daService.update("insert into logo_tb(orgid,type,name) values(?,?,?) ", 
						new Object[]{chanid, type, name});
				logger.error("���������̨���LOGO��¼>>chanid:"+chanid+",r:"+r);
				AjaxUtil.ajaxOutput(response, r+"");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
		}else if(action.equals("edit")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			Long id = RequestUtil.getLong(request, "id", -1L);
			logger.error("���������̨�༭LOGO��¼>>chanid:"+chanid+",name:"+name+",id:"+id);
			if(id > 0){
				int r = daService.update("update logo_tb set name=? where id=? ", new Object[]{name,id});
				logger.error("���������̨�༭LOGO��¼>>chanid:"+chanid+",r:"+r+",id:"+id);
				AjaxUtil.ajaxOutput(response, r + "");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
			
		}else if(action.equals("downloadlogo")){
			Long orgid = RequestUtil.getLong(request, "orgid", -1L);
			Integer type = RequestUtil.getInteger(request, "type", 0);
			Integer number = RequestUtil.getInteger(request, "number", 0);
			if(orgid > 0){
				String sql = "select * from logo_tb where type=? and orgid=? ";
				Map<String, Object> map = daService.getMap(sql, new Object[]{type, orgid});
				if(map != null){
					if(number == 0 && map.get("url_fir") != null){
						String picurl = (String)map.get("url_fir");
						downloadLogoPics(picurl, request, response);
					}
				}
			}
		}
		return null;
	}
	
	private void downloadLogoPics (String picurl, HttpServletRequest request,HttpServletResponse response) throws Exception{
		logger.error("download from mongodb....");
		System.err.println("downloadlogo from mongodb picurl="+picurl);
		if(picurl!=null ){
			DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
			DBCollection collection = db.getCollection("logo_pics");
			BasicDBObject document = new BasicDBObject();
			document.put("filename", picurl);
			DBObject obj  = collection.findOne(document);
			if(obj == null){
				AjaxUtil.ajaxOutput(response, "");
				logger.error("ȡͼƬ����.....");
				return;
			}
			byte[] content = (byte[])obj.get("content");
			logger.error("ȡͼƬ�ɹ�.....��С:"+content.length);
			db.requestDone();
			response.setDateHeader("Expires", System.currentTimeMillis()+12*60*60*1000);
			response.setContentLength(content.length);
			response.setContentType("image/jpeg");
		    OutputStream o = response.getOutputStream();
		    o.write(content);
		    o.flush();
		    o.close();
		    System.out.println("mongdb over.....");
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setLogo(List<Map> list, Integer type){
		if(list != null && !list.isEmpty()){
			List<Object> params = new ArrayList<Object>();
			params.add(type);
			String preParams = "";
			for(Map<String, Object> map : list){
				Long id = (Long)map.get("id");
				params.add(id);
				if(preParams.equals("")){
					preParams ="?";
				}else{
					preParams += ",?";
				}
			}
			List<Map> rList = daService.getAllMap("select * from logo_tb where type=? and orgid in ("+preParams+") ", params);
			if(rList != null){
				for(Map map : rList){
					Long orgid = (Long)map.get("orgid");
					for(Map map2 : list){
						Long id = (Long)map2.get("id");
						if(orgid.intValue() == id.intValue()){
							map2.put("logoname", map.get("name"));
							map2.put("url_fir", map.get("url_fir"));
							map2.put("url_sec", map.get("url_sec"));
							break;
						}
					}
				}
			}
		}
	}
	
	private String uploadPicToMongodb (HttpServletRequest request,Long id,String table,int type,int logotype) throws Exception{
		Map<String, String> extMap = new HashMap<String, String>();
	    extMap.put(".jpg", "image/jpeg");
	    extMap.put(".jpeg", "image/jpeg");
	    extMap.put(".png", "image/png");
	    extMap.put(".gif", "image/gif");
		request.setCharacterEncoding("UTF-8"); // ���ô�����������ı����ʽ
		DiskFileItemFactory  factory = new DiskFileItemFactory(); // ����FileItemFactory����
		factory.setSizeThreshold(16*4096*1024);
		ServletFileUpload upload = new ServletFileUpload(factory);
		// �������󣬲��õ��ϴ��ļ���FileItem����
		upload.setSizeMax(16*4096*1024);
		List<FileItem> items = null;
		try {
			items =upload.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
			return "-1";
		}
		String filename = ""; // �ϴ��ļ����浽���������ļ���
		InputStream is = null; // ��ǰ�ϴ��ļ���InputStream����
		// ѭ�������ϴ��ļ�
		for (FileItem item : items){
			// ������ͨ�ı���
			if (item.isFormField()){
				/*if(item.getFieldName().equals("comid")){
					if(!item.getString().equals(""))
						comId = item.getString("UTF-8");
				}*/
				
			}else if (item.getName() != null && !item.getName().equals("")){// �����ϴ��ļ�
				// �ӿͻ��˷��͹������ϴ��ļ�·���н�ȡ�ļ���
				filename = item.getName().substring(item.getName().lastIndexOf("\\")+1);
				is = item.getInputStream(); // �õ��ϴ��ļ���InputStream����
				
			}
		}
		String file_ext =filename.substring(filename.lastIndexOf(".")).toLowerCase();// ��չ��
		String picurl = type+"_"+id+"_"+logotype+"_"+System.currentTimeMillis()+file_ext;
		BufferedInputStream in = null;  
		ByteArrayOutputStream byteout =null;
	    try {
	    	in = new BufferedInputStream(is);   
	    	byteout = new ByteArrayOutputStream(1024);        	       
		      
	 	    byte[] temp = new byte[1024];        
	 	    int bytesize = 0;        
	 	    while ((bytesize = in.read(temp)) != -1) {        
	 	          byteout.write(temp, 0, bytesize);        
	 	    }        
	 	      
	 	    byte[] content = byteout.toByteArray(); 
	 	    DB mydb = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
		    mydb.requestStart();
			  
		    DBCollection collection = mydb.getCollection(table);
		  //  DBCollection collection = mydb.getCollection("records_test");
			  
			BasicDBObject document = new BasicDBObject();
			document.put("id", id);
			document.put("ctime",  System.currentTimeMillis()/1000);
			document.put("logotype", logotype);
			document.put("orgtype", type);
			document.put("type", extMap.get(file_ext));
			document.put("content", content);
			document.put("filename", picurl);
			  //��ʼ����
			mydb.requestStart();
			collection.insert(document);
			  //��������
			mydb.requestDone();
			in.close();        
		    is.close();
		    byteout.close();
		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}finally{
			if(in!=null)
				in.close();
			if(byteout!=null)
				byteout.close();
			if(is!=null)
				is.close();
		}
	    
		return picurl;
	}
}
