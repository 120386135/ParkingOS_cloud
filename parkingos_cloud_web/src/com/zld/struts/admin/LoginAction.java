package com.zld.struts.admin;

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

import com.zld.impl.MongoDbUtils;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.ZLDType;
/**
 * ��¼���ܹ���Ա��ͣ������̨����Ա������Ƚ�ɫ���Ե�¼ 
 * @author Administrator
 *
 */
public class LoginAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(LoginAction.class);

	@SuppressWarnings("unchecked")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action= RequestUtil.getString(request, "action");
		if(action.equals("out")){//�˳�
			String ip = StringUtils.getIpAddr(request);
			Long uin = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)request.getSession().getAttribute("comid");
			if(uin!=null)
				mongoDbUtils.saveLogs(request,0, 0, uin+"�˳���¼");
			request.getSession().invalidate();
			return mapping.findForward("fail");
		}
		String username =RequestUtil.processParams(request, "username");
		String pass =RequestUtil.processParams(request, "pass");
		String sql = "select * from user_info_tb where state=? and ";//";
		Object [] values = null;
		if(Check.checkUin(username)){
			values=new Object[]{0,Long.valueOf(username),pass};
			sql+=" id=? and password=?" ;
		}else{
			values=new Object[]{0,username,pass};
			sql +=" strid=? and password=? ";
		}
		String target = "success";
		Map<String, Object> user = daService.getPojo(sql, values);
		if(user==null){
			request.setAttribute("errormessage", "�ʺŻ����벻��ȷ!");
			request.setAttribute("username", username);
			return mapping.findForward("fail");
		}
		String logourl = "images/logo_top.png";
		Long role = -1L;
		if(user.get("auth_flag") != null){
			role = Long.valueOf(user.get("auth_flag").toString());
		}
		Long roleId = (Long)user.get("role_id");
		Map<String, Object> roleMap = daService.getMap("select * from user_role_tb where id =?", 
				new Object[]{roleId});
		request.getSession().setAttribute("isadmin", 0);//�Ƿ��ǹ���Ա 0��1��
		request.getSession().setAttribute("loginroleid", roleId);//��ɫ -1û��
		request.getSession().setAttribute("adminid", -1L);//��ǰ��ɫ�����Ĺ���Ա�˻�
		request.getSession().setAttribute("supperadmin",0);//�Ƿ��ܹ���Ա 0��1��
		request.getSession().setAttribute("loginuin",user.get("id"));
		request.getSession().setAttribute("comid",user.get("comid"));
		if(roleMap!=null){
			if(roleId == 0 || roleId == 8){
				request.getSession().setAttribute("supperadmin",1);//�Ƿ��ܹ���Ա 0��1��
			}
		}
		if(roleId!=null && roleId > -1){
			Map<String, Object> orgMap = pgOnlyReadService.getMap("select name from zld_orgtype_tb where id=? ", 
					new Object[]{roleMap.get("oid")});
			if(orgMap == null){
				request.setAttribute("errormessage", "��֯���Ͳ����ڣ�");
				target="fail";
			}else{
				request.getSession().setAttribute("oid", roleMap.get("oid"));//�õ�¼��ɫ������֯����
				target ="parkmanage";
				String orgname = (String)orgMap.get("name");
				if(orgname.contains("����")){
					request.getSession().setAttribute("isadmin", 1);//�Ƿ��ǹ���Ա 0��1��
					request.setAttribute("cloudname", "�ǻ�ͣ����-������");
					Long count = pgOnlyReadService.getLong("select count(id) from com_info_tb where state=? and id=? ",
							new Object[] { 0, user.get("comid") });
					
					if(count == 0){
						request.setAttribute("errormessage", "���������ڻ��߳���δͨ�����!");
						target="fail";
					}else{
						Map<String, Object> comMap = daService.getMap("select chanid from com_info_tb where id=? ", 
								new Object[]{user.get("comid")});
						if(comMap != null && comMap.get("chanid") != null){
							Long chanid = (Long)comMap.get("chanid");
							if(chanid > 0){
								Map<String, Object> map = daService.getMap("select * from logo_tb where type=? and orgid=? ", 
										new Object[]{0, chanid});
								if(map != null&& map.get("url_fir") != null){
									logourl = "cloudlogo.do?action=downloadlogo&type=0&orgid="+chanid+"&number=0&r="+Math.random();
								}
							}
						}
					}
				}else if(orgname.contains("����")){
					request.getSession().setAttribute("isadmin", 1);//�Ƿ��ǹ���Ա 0��1��
					request.setAttribute("cloudname", "�ǻ�ͣ����-������");
					request.getSession().setAttribute("comid",0L);
					request.getSession().setAttribute("chanid",user.get("chanid"));
					Long chancount = pgOnlyReadService.getLong("select count(id) from org_channel_tb where id=? and state=? ", 
							new Object[]{user.get("chanid"), 0});
					if(chancount == 0){
						request.setAttribute("errormessage", "����������!");
						target="fail";
					}else{
						Map<String, Object> map = daService.getMap("select * from logo_tb where type=? and orgid=? ", 
								new Object[]{0, user.get("chanid")});
						if(map != null&& map.get("url_fir") != null){
							logourl = "cloudlogo.do?action=downloadlogo&type=0&orgid="+user.get("chanid")+"&number=0&r="+Math.random();
						}
					}
				}else if(orgname.contains("����")){
					request.getSession().setAttribute("isadmin", 1);//�Ƿ��ǹ���Ա 0��1��
					request.setAttribute("cloudname", "�ǻ�ͣ����-������");
					request.getSession().setAttribute("comid",0L);
					request.getSession().setAttribute("groupid",user.get("groupid"));
					Long groupcount = pgOnlyReadService.getLong("select count(id) from org_group_tb where id=? and state=? ", 
							new Object[]{user.get("groupid"), 0});
					if(groupcount == 0){
						request.setAttribute("errormessage", "���Ų�����!");
						target="fail";
					}
				}else if(orgname.contains("����")){
					request.getSession().setAttribute("isadmin", 1);//�Ƿ��ǹ���Ա 0��1��
					request.setAttribute("cloudname", "�ǻ�ͣ����-������");
					request.getSession().setAttribute("comid",0L);
					request.getSession().setAttribute("cityid",user.get("cityid"));
					Long groupcount = pgOnlyReadService.getLong("select count(id) from org_city_merchants where id=? and state=? ", 
							new Object[]{user.get("cityid"), 0});
					if(groupcount == 0){
						request.setAttribute("errormessage", "���в�����!");
						target="fail";
					}
				}
			}
			List<Map<String, Object>> authList = null;
			if(roleId == 0){//�ܹ���Աӵ������Ȩ��
				authList = daService.getAll("select actions,id auth_id,nname,pid,url,sort,sub_auth childauths from auth_tb where oid=? and state=? ", 
						new Object[]{roleMap.get("oid"), 0});
				if(authList != null){
					for(Map<String, Object> map : authList){
						if(map.get("childauths") != null){
							String childauths = (String)map.get("childauths");
							if(!childauths.equals("")){
								String[] subs = childauths.split(",");
								String subauth = null;
								for(int i=0; i<subs.length; i++){
									if(i == 0){
										subauth = "" + i;
									}else{
										subauth += ","+i;
									}
								}
								map.put("sub_auth", subauth);
							}
						}
					}
				}
			}else{
				//��ȡȨ��
				authList = daService.getAll("select a.actions,auth_id,ar.sub_auth,nname,a.pid,a.url,a.sort " +
						"from auth_role_tb ar left join" +
						" auth_tb a on ar.auth_id=a.id " +
						" where role_id=? order by  a.sort " , new Object[]{roleId});
			}
			
			request.getSession().setAttribute("ishdorder", user.get("order_hid"));
			request.getSession().setAttribute("authlist", authList);
			request.getSession().setAttribute("menuauthlist", StringUtils.createJson(authList));
			//����֯�µ����й����б�
			List<Map<String, Object>> allAuthList = daService.getAll("select * from auth_tb where oid=? and state=? ", 
					new Object[]{roleMap.get("oid"), 0});
			request.getSession().setAttribute("allauth", allAuthList);
		}else {
			//role: 0�ܹ���Ա��1ͣ������̨����Ա ��2�����շ�Ա��3����4����  5�г�רԱ 6¼��Ա
			if(role.intValue()==ZLDType.ZLD_COLLECTOR_ROLE||role.intValue()==ZLDType.ZLD_CAROWER_ROLE||role.intValue() == ZLDType.ZLD_KEYMEN){//�����շ�Ա���������ܵ�¼��̨
				request.setAttribute("errormessage", "û�в�ѯ��̨����Ȩ�ޣ�����ϵ����Ա!");
				target="fail";
			}else if(role.intValue()==ZLDType.ZLD_PARKADMIN_ROLE){
				target ="parkmanage";
				request.setAttribute("cloudname", "�ǻ�ͣ����-������");
				Long count = pgOnlyReadService.getLong(
						"select count(id) from com_info_tb where state=? and id=? ",
						new Object[] { 0, user.get("comid") });
				if(count == 0){
					request.setAttribute("errormessage", "���������ڻ��߳���δͨ�����!");
					target="fail";
				}
			}else if(role.intValue()==ZLDType.ZLD_ACCOUNTANT_ROLE){
				target ="finance";
			}else if(role.intValue()==ZLDType.ZLD_CARDOPERATOR){
				target ="cardoperator";
			}else if(role.intValue()==ZLDType.ZLD_MARKETER){//�г�רԱ ��¼��̨
				request.getSession().setAttribute("marketerid",user.get("id"));
				target ="marketer";
			}else if(role.intValue()==ZLDType.ZLD_RECORDER||role.intValue()==ZLDType.ZLD_KEFU||role.intValue()==ZLDType.ZLD_QUERYKEFU){
				target = "recorder";
			}
			if(role==0){//�ܹ���Ա
				request.getSession().setAttribute("supperadmin",1);
			}
		}
		request.getSession().setAttribute("role",role );
		request.getSession().setAttribute("userinfo",user);
		request.getSession().setAttribute("userid", username);
		String nickname = "";
		if(user.get("nickname") != null){
			nickname = (String)user.get("nickname");
			if(nickname.length() > 4){
				nickname = nickname.substring(0, 4) + "...";
			}
		}
		request.getSession().setAttribute("nickname", nickname);

		String logContent = username+"��¼������:"+target;
		String ip = StringUtils.getIpAddr(request);
		mongoDbUtils.saveLogs(request, 0, 0,logContent);
//		List<Object[]> valuesList = ReadFile.praseFile();
//		int result = daService.bathInsert("insert into com_info_tb (longitude,latitude,company_name,address,type) values(?,?,?,?,?)",
//				valuesList, new int[]{3,3,12,12,4});
//		System.out.println(result);
		request.setAttribute("logourl", logourl);
		return mapping.findForward(target);
	}

}