package com.zld.struts.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.zld.utils.TimeTools;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.MemcacheUtils;
import com.zld.pojo.CollectorSetting;
import com.zld.pojo.Group;
import com.zld.service.DataBaseService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
/**
 * ͣ�����շ�Ա��¼ 
 * @author Administrator
 *
 */
public class ParkCollectorLoginAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	@Autowired 
	private MemcacheUtils memcacheUtils;
	private Logger logger = Logger.getLogger(ParkCollectorLoginAction.class);
	//http://127.0.0.1/zld/collectorlogin.do?username=1000005&action=forpass
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		String out = RequestUtil.processParams(request, "out");
		Map<String,Object> infoMap = new HashMap<String, Object>();
		Integer userState = 0;
		logger.error("action:"+action);
		if(action.equals("forpass")){//�һ����룬���͵��շ�Ա��ע���ֻ���
			String userId = RequestUtil.processParams(request, "username");
			Map userMap = daService.getPojo("select id,password,mobile from user_info_tb where id=?",
					new Object[]{Long.valueOf(userId)});
			if(userMap!=null){
				String mobile = (String)userMap.get("mobile");
				logger.equals(mobile);
//				if(mobile==null||"".equals(mobile)){
					AjaxUtil.ajaxOutput(response, "{\"info\":\"��ע���ʺ�ʱû����д�ֻ�������ϵ�ͷ���Ա��\"}");
//				}else if(Check.checkPhone(mobile,"m")){
//					String _mString = mobile.substring(0,3)+"****"+mobile.substring(7);
//					//SendMessage.sendMessage(mobile,(String)userMap.get("password"));
//					AjaxUtil.ajaxOutput(response, "{\"info\":\"������ͨ�����ŷ��͵���ע����ֻ���["+_mString+"]������գ���ͣ������\"}");
//				}else {
//					AjaxUtil.ajaxOutput(response, "{\"info\":\"��ע����ֻ��Ų��Ϸ���\"}");
//				}
			}else {
				AjaxUtil.ajaxOutput(response, "{\"info\":\"�ʺŲ����ڣ�\"}");
			}
			return null;
		}else if(action.equals("editpass")){//�޸����룬���͵��շ�Ա��ע���ֻ���
			//http://127.0.0.1/zld/collectorlogin.do?username=1000005&action=editpass&oldpass=&newpass=
			Long userId = RequestUtil.getLong(request, "username",-1L);
			String oldPass = RequestUtil.processParams(request, "oldpass");
			String newPass = RequestUtil.processParams(request, "newpass");
			if(oldPass.length()<32){
				oldPass =StringUtils.MD5(oldPass);
				oldPass = StringUtils.MD5(oldPass+"zldtingchebao201410092009");
			}
			Long count  = daService.getLong("select count(*) from user_info_tb where id=? and md5pass=? ", 
					new Object[]{userId,oldPass});
			int result = 0;
			if(newPass.length()<32){
				newPass =StringUtils.MD5(newPass);
				newPass = StringUtils.MD5(newPass+"zldtingchebao201410092009");
			}
			if(count>0){
				result = daService.update("update user_info_tb set md5pass=? where id=? ",
						new Object[]{newPass,userId});
			}else
				result = -1;
			logger.error("oldpass:"+oldPass+",newpass:"+newPass);
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
		}else if(action.equals("editname")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			Long uin = RequestUtil.getLong(request, "uin", -1L);
			int result = 0;
			if(uin!=-1){
				result = daService.update("update user_info_tb set nickname=? where id=? ",
						new Object[]{name,uin});
			}
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
			//http://192.168.199.240/zld/collectorlogin.do?action=editname&name=1020005&uin=1000005
		}else if(action.equals("editphone")){
			String mobile = RequestUtil.processParams(request, "mobile");
			Long uin = RequestUtil.getLong(request, "uin", -1L);
			int result = 0;
			if(uin!=-1){
				result = daService.update("update user_info_tb set  mobile=? where id=? ",
						new Object[]{mobile,uin});
			}
			AjaxUtil.ajaxOutput(response, result+"");
			return null;
			//http://192.168.199.240/zld/collectorlogin.do?action=editphone&mobile=18003005000&uin=1000005
		}else if(action.equals("inspectlogin")){//Ѳ��Ա��½�ӿ�
			String username =RequestUtil.processParams(request, "username");
			String pass =RequestUtil.processParams(request, "password");
			int result = 0;
			String version = RequestUtil.getString(request, "version");
			logger.error("user:"+username+",pass:"+pass);
			String sql = "select * from user_info_tb where id=? and md5pass=?";// and auth_flag=?";
			if(pass.length()<32){
				//md5���� �����ɹ���ԭ����md5�󣬼���'zldtingchebao201410092009'�ٴ�md5
				pass =StringUtils.MD5(pass);
				pass = StringUtils.MD5(pass +"zldtingchebao201410092009");
			}
			if(!StringUtils.isNumber(username)){
				infoMap.put("info", "fail");
				AjaxUtil.ajaxOutput(response, StringUtils.createXML(infoMap));
				return null;
			}

			Map user = daService.getPojo(sql, new Object[]{Long.valueOf(username),pass});//,ZLDType.ZLD_COLLECTOR_ROLE});
			//logger.error(user);
			if(user==null){
				infoMap.put("info", "�û������������");
			}else {
				userState = (Integer)user.get("state");//-- 0:������1�����ã�2������ˣ�3�������䣬4����������5�޼�ֵ
				int auth_flag = Integer.parseInt(user.get("auth_flag") + "");
				if(auth_flag==16){
					if(userState!=1){
						Long uin = (Long)user.get("id");
//				Long roleId = (Long)user.get("role_id");
						Long groupId = (Long)user.get("groupid");
						Long comId = (Long)user.get("comid");
						if(comId==null){
							comId=-1L;
						}
						String token = StringUtils.MD5(username+pass+System.currentTimeMillis());
						String oldtoken = doSaveSession(uin, comId, groupId, token, version);
						Map<String,String >  parkTokenCacheMap =memcacheUtils.doMapStringStringCache("parkuser_token", null, null);
						if(parkTokenCacheMap!=null){
							parkTokenCacheMap.put(token, uin+"_"+comId+"_"+groupId);
							if(oldtoken!=null){
								logger.error("....delete oldtoken:"+oldtoken+","+parkTokenCacheMap.remove(oldtoken));
							}
							memcacheUtils.doMapStringStringCache("parkuser_token", parkTokenCacheMap, "update");
						}
						infoMap.put("info", "success");
						infoMap.put("token", token);
						infoMap.put("role", user.get("auth_flag"));
						infoMap.put("name", user.get("nickname"));
						infoMap.put("comid",comId);
						infoMap.put("mobile", user.get("mobile"));
						infoMap.put("state", userState);//0�����û���1���ã�2�����
						infoMap.put("logontime", System.currentTimeMillis()/1000);//20150618���ϴ����¼ʱ��
						//ǩ��
						Map pwrMap = daService.getMap("select id,start_time from parkuser_work_record_tb where uid=? and end_time is null" +
								" and state=? ", new Object[]{uin,0});
						if(pwrMap==null||pwrMap.isEmpty()){
							Long workId  = daService.getkey("seq_parkuser_work_record_tb");
							List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
							//ǩ�����
							Map<String, Object> workRecordSqlMap = new HashMap<String, Object>();
							workRecordSqlMap.put("sql", "insert into parkuser_work_record_tb (id,start_time,uid,state) values(?,?,?,?) ");
							workRecordSqlMap.put("values", new Object[]{workId, System.currentTimeMillis()/1000, uin, 0});
							bathSql.add(workRecordSqlMap);
							//��Ϊ����
							Map<String, Object> onlineSqlMap = new HashMap<String, Object>();
							onlineSqlMap.put("sql", "update user_info_tb set online_flag=? where id=? ");
							onlineSqlMap.put("values", new Object[]{22, uin});
							bathSql.add(onlineSqlMap);
							boolean b = daService.bathUpdate2(bathSql);
							logger.error("b:" + b);
							infoMap.put("worktime", System.currentTimeMillis()/1000);
							infoMap.put("signstate",0);//��½��ǩ��
						}else {
							Long startTime =(Long)pwrMap.get("start_time");
							infoMap.put("worktime", startTime);
							infoMap.put("signstate",1);
						}
					}else{
						infoMap.put("info", "�˺ű�����");
					}
				}else{
					infoMap.put("info", "�˺���Ȩ��");
				}
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			return null;
			//http://localhost:8088/zld/collectorlogin.do?action=inspectlogin&username=21951&password=56c5d7d1f606032950e2b1b683b1cf37
		}else if(action.equals("cardlogin")){
			Long username =RequestUtil.getLong(request, "username", -1L);
			String pass =RequestUtil.processParams(request, "password");
			String version = RequestUtil.getString(request, "version");
			logger.error("user:"+username+",pass:"+pass);
			if(pass.length() < 32){
				//md5���� �����ɹ���ԭ����md5�󣬼���'zldtingchebao201410092009'�ٴ�md5
				pass =StringUtils.MD5(pass);
				pass = StringUtils.MD5(pass +"zldtingchebao201410092009");
			}
			if(username <= 0){
				infoMap.put("info", "��������");
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
				return null;
			}

			Map user = daService.getPojo("select * from user_info_tb where " +
					" id=? and md5pass=? and auth_flag=? ", 
					new Object[]{username, pass, 17});
			if(user == null){
				infoMap.put("info", "�û������������");
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
				return null;
			}
			Integer state = (Integer)user.get("state");
			if(state == 1){
				infoMap.put("info", "�˻�������");
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
				return null;
			}
			Long uin = (Long)user.get("id");
			Long groupId = (Long)user.get("groupid");
			Long comId = -1L;
			if(user.get("comid") != null){
				comId = (Long)user.get("comid");
			}
			String group_name = "";
			if(groupId != null && groupId > 0){
				Group group = daService.getPOJO("select name from org_group_tb where id=?",
						new Object[]{groupId}, Group.class);
				if(group != null && group.getName() != null){
					group_name = group.getName();
				}
			}
			String token = StringUtils.MD5(username+pass+System.currentTimeMillis());
			String oldtoken = doSaveSession(uin, comId, groupId, token, version);
			Map<String,String > parkTokenCacheMap =memcacheUtils.doMapStringStringCache("parkuser_token", null, null);
			if(parkTokenCacheMap != null){
				parkTokenCacheMap.put(token, uin+"_"+comId+"_"+groupId);
				if(oldtoken!=null){
					parkTokenCacheMap.remove(oldtoken);
				}
				memcacheUtils.doMapStringStringCache("parkuser_token", parkTokenCacheMap, "update");
			}
			infoMap.put("info", "success");
			infoMap.put("token", token);
			infoMap.put("role", user.get("auth_flag"));
			infoMap.put("name", user.get("nickname"));
			infoMap.put("mobile", user.get("mobile"));
			infoMap.put("state", userState);//0�����û���1���ã�2�����
			infoMap.put("logontime", System.currentTimeMillis()/1000);//20150618���ϴ����¼ʱ��
			infoMap.put("group_name", group_name);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
			return null;
		}
		//http://127.0.0.1/zld/collectorlogin.do?username=21934&password=123456&out=json
		//http://127.0.0.1/zld/collectorlogin.do?username=12703&password=123456&out=json
		String username =RequestUtil.processParams(request, "username");
		String pass =RequestUtil.processParams(request, "password");
		String version = RequestUtil.getString(request, "version");
		logger.error("user:"+username+",pass:"+pass);
		String sql = "select * from user_info_tb where id=? and md5pass=?";// and auth_flag=?";
		if(pass.length()<32){
			//md5���� �����ɹ���ԭ����md5�󣬼���'zldtingchebao201410092009'�ٴ�md5
			pass =StringUtils.MD5(pass);
			pass = StringUtils.MD5(pass +"zldtingchebao201410092009");
		}
		if(!StringUtils.isNumber(username)){
			infoMap.put("info", "fail");
			AjaxUtil.ajaxOutput(response, StringUtils.createXML(infoMap));
			return null;
		}

		Map user = daService.getPojo(sql, new Object[]{Long.valueOf(username),pass});//,ZLDType.ZLD_COLLECTOR_ROLE});
		//logger.error(user);
		if(user == null){
			infoMap.put("info", "�û������������");
		}else {
			Long roleId = (Long)user.get("role_id");
			Long groupId = (Long)user.get("groupid");
			Long comId = (Long)user.get("comid");
			//����pos��android�汾��¼
			Map<String,Object> comMap = null;
			boolean ispos = false;
			if(comId != null && comId > 0){
				comMap= daService.getMap("select * from com_info_tb where id =?", new Object[]{comId});
			}else {
				CollectorSetting setting = daService.getPOJO("select * from collector_set_tb where" +
						" role_id=? order by id desc limit ? ", new Object[]{roleId, 1} , CollectorSetting.class);
				if(setting != null){
					//updateCollectorSettingCache(roleId, setting);
					infoMap.put("photoset", setting.getPhotoset());
					infoMap.put("change_prepay", setting.getChange_prepay());
					infoMap.put("view_plot", setting.getView_plot());
					infoMap.put("print_sign", setting.getPrint_sign());
					infoMap.put("prepayset", setting.getPrepayset());
					infoMap.put("isprepay", setting.getIsprepay());
					infoMap.put("hidedetail", setting.getHidedetail());//hidedetail integer DEFAULT 0, -- 0������ 1������ҳ�շѻ���
					infoMap.put("signoutvalid", setting.getSignout_valid());//hidedetail integer DEFAULT 0, -- 0������ 1������ҳ�շѻ���
					infoMap.put("is_show_card", setting.getIs_show_card());
					infoMap.put("print_order_place2", setting.getPrint_order_place2());
					ispos=true; 
				}
				//��ѯ�Ƿ���ǩ����
				infoMap.put("berthid", -1);
				Map<String,Object> mp = daService.getMap("select berthsec_id from parkuser_work_record_tb where uid=? and state=?", new Object[]{user.get("id"),0});
				if(mp!=null&&mp.get("berthsec_id")!=null){
					Long berthid = (Long)mp.get("berthsec_id");
					if(berthid!=null&&berthid>0){//�Ӳ�λ����ȡͣ�������
						infoMap.put("berthid", berthid);
						Map berthMap = daService.getMap("select berthsec_name,comid from com_berthsecs_tb where id =? ", new Object[]{mp.get("berthsec_id")});
						if(berthMap!=null&&!berthMap.isEmpty()){
							comId=(Long)berthMap.get("comid");
							infoMap.put("berthsec_name", berthMap.get("berthsec_name"));
						}
					}
				}else if(roleId!=null&&roleId>0&&groupId!=null&&groupId>0){//pos��android��¼�����շ�Ա����
					
					List<Map<String, Object>> berthList = daService.getAll("select berthsec_name,b.id,b.comid from com_berthsecs_tb b where is_active=? and  id in" +
							"(select berthsec_id from work_berthsec_tb where state=? and is_delete=? and  work_group_id =" +
							"(select work_group_id from work_employee_tb where employee_id =? and state=? )) ", new Object[]{0,0,0,user.get("id"),0});
					
					if(berthList!=null&&!berthList.isEmpty()){
						infoMap.put("berths", StringUtils.createJson(berthList));
						comId = (Long)berthList.get(0).get("comid");
					}
				}
				comMap= daService.getMap("select * from com_info_tb where id =?", new Object[]{comId});
			}
			//String comPassInfo =isPassCom(comMap);
			Integer state =-1;
			if(comMap!=null&&comMap.get("state")!=null){
				state = (Integer)comMap.get("state");
			}
			userState = (Integer)user.get("state");//-- 0:������1�����ã�2������ˣ�3�������䣬4����������5�޼�ֵ
			
			if(state==1){
				infoMap.put("info", "������ɾ��");
			}else if(state==2&&userState<2){
				infoMap.put("info", "����δ���");
			}else if(comId==null||comId<1){
				infoMap.put("info", "û�п��ϸڵĲ�λ");
			}else if(state==-1){
				infoMap.put("info", "û�г�����Ϣ!");
			}else{
				Long uin = (Long)user.get("id");
				String token = StringUtils.MD5(username+pass+System.currentTimeMillis());
				infoMap.put("info", "success");
				infoMap.put("token", token);
				infoMap.put("role", user.get("auth_flag"));
				infoMap.put("name", user.get("nickname"));
				infoMap.put("comid",comId);
				infoMap.put("mobile", user.get("mobile"));
				infoMap.put("state", userState);//0�����û���1���ã�2�����
				infoMap.put("nfc", comMap.get("nfc"));//0�����û���1���ã�2�����
				infoMap.put("cname", comMap.get("company_name"));
				infoMap.put("ctotal", comMap.get("parking_total"));
				infoMap.put("firstprovince", comMap.get("firstprovince"));
				int isshowepay = 0;
				if(comId!=null){
					Map com = daService.getMap( "select isshowepay from com_info_tb where id=? and isshowepay=?",new Object[]{comId,1});
					if(com!=null&&com.get("isshowepay")!=null){
						isshowepay = 1;
					}
				}
				infoMap.put("isshowepay", isshowepay);
				infoMap.put("logontime", System.currentTimeMillis()/1000);//20150618���ϴ����¼ʱ��
				Integer etc = (Integer)comMap.get("etc");
				if(etc!=null&&etc==1){//����������ibeacon����ѯ���еĹ���վ
					List<Map<String, Object>> workList = daService.getAll("select id,worksite_name from " +
							"com_worksite_tb where comid = ? ", new Object[]{comId});
					if(workList!=null&&!workList.isEmpty()){
						infoMap.put("worksite",StringUtils.createJson(workList));
					}
				}
				infoMap.put("etc", etc);// 0:��֧�֣�1:Ibeacon 2:ͨ������ 3:�ֻ�����
				String pid = CustomDefind.CUSTOMPARKIDS;
				if(comId!=null&&pid.equals(comId.toString())){//���Ƴ���������Ҫɨ������
					infoMap.put("swipe",0);//ȥ��ɨ������
				}else {
					infoMap.put("swipe",1);//����ɨ������
				}
				infoMap.put("notemsg", "");
				Long authFlag = (Long)user.get("auth_flag");
				infoMap.put("authflag",authFlag);
				infoMap.put("qr", getQrCode(uin));
				//System.out.println(infoMap);
				String oldtoken = doSaveSession(uin,comId,groupId,token,version);
				if(authFlag==13){
					daService.update("update user_info_Tb set logon_time=?,online_flag=? where id=? ",
							new Object[]{System.currentTimeMillis()/1000,23,user.get("id")});
				}else {
					daService.update("update user_info_Tb set logon_time=? where id=? ",
							new Object[]{System.currentTimeMillis()/1000,user.get("id")});
				}
				Long worksiteid = RequestUtil.getLong(request, "worksite_id", -1L);
				long endtime = System.currentTimeMillis()/1000;
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:00");
//				String d = sdf.format(System.currentTimeMillis());
//				long endtime = sdf.parse(d).getTime()/1000;
				if(etc == 2&&userState==0){//ͨ��ɨ�Ƴ�������
					logger.error("�շ�Ա��½>>>>>>>>>>>>>����վ��"+worksiteid);
					if(worksiteid > 0){//��ͤ����վ��
						List<Map<String, Object>> ortherUids = daService.getAll("select uid from parkuser_work_record_tb where " +
								" worksite_id = ? and end_time is null and uid!=?", new Object[] { worksiteid, uin });
						if(ortherUids!=null&&!ortherUids.isEmpty()){
							for(Map<String, Object> map: ortherUids){
								Long ouid = (Long)map.get("uid");
								logger.error("collectorlogin>>>>>:ǿ���°�uid:"+ouid);
								doDeleteToken(ouid,comId,groupId);
							}
							int r = daService.update("update parkuser_work_record_tb set end_time=? where worksite_id = ? and end_time is null and uid!=?",
									new Object[] { endtime, worksiteid, uin });
							logger.error("collectorlogin>>>>>:ǿ���°�worksiteid:"+worksiteid+",r:"+r);
						}
					}
					Map usr = daService.getPojo(
							"select * from parkuser_work_record_tb where  worksite_id<>? and end_time is null and uid=?",
							new Object[] {worksiteid, uin });
					if(usr!=null){
						daService.update("update parkuser_work_record_tb set end_time=? where id = ?",
								new Object[]{endtime,(Long)usr.get("id")});
						logger.error((Long)usr.get("uid")+"��½����վ���ɹ���վ�°�...");
					}
					Long count = daService.getLong(
							"select count(*) from parkuser_work_record_tb where worksite_id =? and end_time is null and uid=?",
							new Object[] {worksiteid, uin });
					if(count == 0&&!ispos){//�ϰ�
						int r = daService.update("insert into parkuser_work_record_tb(start_time,uid,worksite_id) values(?,?,?)",
								new Object[]{endtime,uin,worksiteid});
						logger.error("collectorlogin>>>>>:ͨ���������ϰࣺuid:"+uin+",worksiteid:"+worksiteid+",r:"+r);
					}
				}		
				logger.error(username+"��¼�ɹ�...");
				//����memcached����
				Map<String,String >  parkTokenCacheMap =memcacheUtils.doMapStringStringCache("parkuser_token", null, null);
				if(parkTokenCacheMap!=null){
					parkTokenCacheMap.put(token, uin+"_"+comId+"_"+groupId);
					if(oldtoken!=null){ 
						logger.error("....delete oldtoken:"+oldtoken+","+parkTokenCacheMap.remove(oldtoken));
					}
					memcacheUtils.doMapStringStringCache("parkuser_token", parkTokenCacheMap, "update");
				}
			}
		}
		if(out.equals("json")){
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(infoMap));
		}else {
			if(userState==1){
				infoMap.put("info","�շ�Ա�˺ű�����");
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createXML(infoMap));
		}
		logger.error( StringUtils.createJson(infoMap));
		return null;
	}
	//ɾ�������������շ�Ա��TOKEN
	private void doDeleteToken(Long uin,Long comId,Long groupId) {
		Map<String, Object> map = daService.getMap("select token from user_session_tb where uin=? ", new Object[]{uin});
		String oldtoken = (String)map.get("token");
		String token = "zldtokenvoid"+System.currentTimeMillis();
		Map<String,String >  parkTokenCacheMap =memcacheUtils.doMapStringStringCache("parkuser_token", null, null);
		if(parkTokenCacheMap!=null){
			parkTokenCacheMap.put(token, uin+"_"+comId+"_"+groupId);
			if(oldtoken!=null){ 
				logger.error("....delete oldtoken:"+oldtoken+","+parkTokenCacheMap.remove(oldtoken));
				daService.update("update user_session_tb set token=? ,create_time=?,comid=?,groupid=?  where uin=? ", 
						new Object[]{token,System.currentTimeMillis()/1000,comId,groupId,uin});
			}
			memcacheUtils.doMapStringStringCache("parkuser_token", parkTokenCacheMap, "update");
		}
		logger.error("collectorlogin>>>>>:ǿ���°࣬����TOKEN��uid:"+uin);
	}
	/**
	 * ����token�����ݿ���
	 * @param uin
	 * @param comid
	 * @param token
	 */
	private String doSaveSession(Long uin,Long comid,Long groupId,String token,String version ){
		//��ɾ���շ�Ա�ϴε�¼ʱ��token
		//daService.update("delete from user_session_tb where uin=? ", new Object[]{uin});
		//�Ȳ�ѯ�շ�Ա�ϴε�¼ʱ��token
		Map<String, Object> map = daService.getMap("select token from user_session_tb where uin=? ", new Object[]{uin});
		String oldtoken =null;
		if(map!=null){
			oldtoken = (String)map.get("token");
			daService.update("update user_session_tb set token=? ,create_time=?,comid=?,groupid=?  where uin=? ", 
					new Object[]{token,System.currentTimeMillis()/1000,comid,groupId,uin});
		}else {
			//���汾�ε�¼��token
			daService.update("insert into user_session_tb (comid,groupid,uin,token,create_time,version) " +
					"values (?,?,?,?,?,?)", 
					new Object[]{comid,groupId,uin,token,System.currentTimeMillis()/1000,version});
		}
		
		return oldtoken;
	}
	
	public static void main(String[] args) {
		String pass = "276766";
		try {
			pass =StringUtils.MD5(pass);
			pass = StringUtils.MD5(pass +"zldtingchebao201410092009");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println(pass);
	}
	
	private String getQrCode(Long uin){
		Map qrMap = daService.getMap("select code from qr_code_tb where uid=? and type=?  ", new Object[]{uin,1});
		String code = "";
		if(qrMap!=null){
			code = (String)qrMap.get("code");
		}
		if(code==null||code.trim().equals("")){
			Long newId = daService.getkey("seq_qr_code_tb");
			String codes[] = StringUtils.getGRCode(new Long[]{newId});
			if(codes!=null&&codes.length>0){
				code = codes[0];
				int ret = daService.update("insert into qr_code_tb(id,ctime,type,code,uid,isuse) values(?,?,?,?,?,?)",
						new Object[]{newId,System.currentTimeMillis()/1000,1,code,uin,1});
				logger.error(">>>>new qrcode:uin:"+uin+",qrcode:"+code+",ret:"+ret);
			}
		}
		if(code!=null&&code.trim().length()==19)
			code = "qr/c/"+code.trim();
		return code;
	}
	
	private void updateCollectorSettingCache(Long roleId, CollectorSetting setting){
		try {
			if(roleId != null && roleId > 0 && setting != null){
				Gson gson = new Gson();
				Map<Long, String> map = memcacheUtils.doMapLongStringCache("pos_collector_setting", null, null);
				if(map == null){
					map = new HashMap<Long, String>();
				}
				map.put(roleId, gson.toJson(setting));
				memcacheUtils.doMapLongStringCache("pos_collector_setting", map, "update");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}