package com.zld.struts.request;

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
import com.zld.impl.CommonMethods;
import com.zld.impl.PublicMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;

/**
 * 
 * ���ػ�������ʼ��
 *
 */
public class LocalInitAction extends Action{
	@Autowired
	private PgOnlyReadService readOnlyReadService;
	@Autowired
	private CommonMethods methods;
	@Autowired
	private PublicMethods publicMethods;
	//1.ͬ������������Ϣ
	private Logger logger = Logger.getLogger(LocalInitAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long comId = RequestUtil.getLong(request, "comid", -1L);
		Map localMap = readOnlyReadService.getMap("select * from local_info_tb where comid = ?", new Object[]{comId});
		if(localMap==null||localMap.size()==0){
			AjaxUtil.ajaxOutput(response, "not is etc local park");
			return null;
		}
		if("initComInfo".equals(action)){//������Ϣ
			Map map = readOnlyReadService.getMap("select * from com_info_tb where id = ? ", new Object[]{comId});
			String result = StringUtils.createJson(map);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initProPack".equals(action)){//�ײ�
			List list = readOnlyReadService.getAll("select * from product_package_tb where comid = ? ", new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initCarPro".equals(action)){//�¿���Ա
			List list = readOnlyReadService.getAll("select * from carower_product where pid in (select id from product_package_tb where comid = ?)"
					, new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initCarInfo".equals(action)){//����
			List list = readOnlyReadService.getAll("select * from car_info_tb where (uin in(select uin from carower_product " +
					"where pid in (select id from product_package_tb where comid = ?)) )", new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initLed".equals(action)){//led
			List list = readOnlyReadService.getAll("select * from com_led_tb where passid in(select id from com_pass_tb where comid = ?)", 
					new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initPass".equals(action)){//ͨ��
			List list = readOnlyReadService.getAll("select * from com_pass_tb where comid = ?", new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initWorkSite".equals(action)){//����վ
			List list = readOnlyReadService.getAll("select * from com_worksite_tb where comid = ?", new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initCarmera".equals(action)){//����ͷ
			List list = readOnlyReadService.getAll("select * from com_camera_tb where passid in(select id from com_pass_tb where comid = ?)",
					new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initPrice".equals(action)){//�۸�
			List list = readOnlyReadService.getAll("select * from price_tb where comid=?", new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initFreeRe".equals(action)){//���ԭ��
			List list = readOnlyReadService.getAll("select * from free_reasons_tb where comid = ? ", new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initPriceAst".equals(action)){//�����۸�
			List list = readOnlyReadService.getAll("select * from price_assist_tb where comid = ? ", new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initCarType".equals(action)){//����
			List list = readOnlyReadService.getAll("select * from car_type_tb where comid = ? ", new Object[]{comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initUser".equals(action)){//�û����շ�Ա���¿�������
			List list = readOnlyReadService.getAll("select * from user_info_tb where (id in(select uin from carower_product where pid in " +
					"(select id from product_package_tb where comid = ?)) or comid = ?)", new Object[]{comId,comId});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}else if("initOrder".equals(action)){
			List list = readOnlyReadService.getAll("select * from order_tb where comid = ? and state=? ", new Object[]{comId,0});
			String result = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response,result);
		}
		return null;
	}
}
