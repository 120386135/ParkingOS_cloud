package com.zld.struts.anlysis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.zld.service.PgOnlyReadService;
import com.zld.utils.ExportExcelUtil;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;

public class CollectByCollectorStaticAnlyAction extends Action{
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	
	private Logger logger = Logger.getLogger(CollectByCollectorStaticAnlyAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//��¼���û�id
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		request.setAttribute("authid", request.getParameter("authid"));
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null && groupid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;
		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long today = TimeTools.getToDayBeginTime();
			request.setAttribute("btime", df2.format(today * 1000 - 24 * 60 * 60 * 1000));
			request.setAttribute("etime",  df2.format(today * 1000 -1));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime) + 24 * 60 *60;
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime) + 24 * 60 *60;
			if(groupid == -1){
				groupid = RequestUtil.getLong(request, "groupid", -1L);
			}
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "user_info");
			List<Object> params = new ArrayList<Object>();
			String sql = "select id,nickname,resume from user_info_tb where ";
			String countSql = "select count(id) from user_info_tb where ";
			List<Object> collectors = null;
			if(cityid > 0){
				collectors = commonMethods.getcollctors(cityid);
			}else if(groupid > 0){
				collectors = commonMethods.getCollctors(groupid);
			}
			if(collectors != null && !collectors.isEmpty()){
				String preParams = "";
				for(Object object : collectors){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " id in ("+preParams+") ";
				countSql += " id in ("+preParams+") ";
				params.addAll(collectors);
			}
			if(sqlInfo != null) {
				countSql += " and "+ sqlInfo.getSql();
				sql += " and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			List<Map<String, Object>> list = null;
			String res = "";
			Long count = pgOnlyReadService.getCount(countSql, params);
			if(count > 0){
				List<Map<String, Object>> titleList = pgOnlyReadService.getAllMap(sql, params);
				res = setTitle(titleList, b, e);
				list = pgOnlyReadService.getAll(sql, params, pageNum, pageSize);
				setList(list, b, e);
			}
			String json = JsonUtil.anlysisMap3Json(list, pageNum, count, fieldsstr,"id",res);
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("export")){
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime) + 24 * 60 *60;
			Long e = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime) + 24 * 60 *60;
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "user_info");
			List<Object> params = new ArrayList<Object>();
			String sql = "select id,nickname,resume from user_info_tb where ";
			List<Object> collectors = null;
			if(cityid > 0){
				collectors = commonMethods.getcollctors(cityid);
			}else if(groupid > 0){
				collectors = commonMethods.getCollctors(groupid);
			}
			if(collectors != null && !collectors.isEmpty()){
				String preParams = "";
				for(Object object : collectors){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " id in ("+preParams+") ";
				params.addAll(collectors);
				
				if(sqlInfo != null) {
					sql += " and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				List<Map<String, Object>> list = pgOnlyReadService.getAllMap(sql, params);
				if(list != null && !list.isEmpty()){
					setList(list, b, e);
					export(response, list, btime, etime);
				}
			}
		}
		
		return null;
	}
	private void export(HttpServletResponse response, List<Map<String, Object>> list, String btime, String etime){
		try {
			if(list != null && !list.isEmpty()){
				String heards[] = new String[]{"�˺�","����","��ע","��ͨ����","׷�ɶ���","�ϼ�","��ͨ����","׷�ɶ���","�ϼ�",
						"��ͨ����","׷�ɶ���","�ϼ�","ʵ��ͣ����","δ��ͣ����","Ӧ��ͣ����","�ۿ�����ֵ","��Ƭ��ֵ","ע������ֵ"};
				List<List<String>> bodyList = new ArrayList<List<String>>();
				for(Map<String, Object> map : list){
					List<String> valueList = new ArrayList<String>();
					valueList.add(map.get("id") + "");
					valueList.add(map.get("nickname") + "");
					valueList.add(map.get("resume") + "");
					valueList.add(map.get("cashCustomFee") + "");
					valueList.add(map.get("cashPursueFee") + "");
					valueList.add(map.get("cashTotalFee") + "");
					
					valueList.add(map.get("ePayCustomFee") + "");
					valueList.add(map.get("ePayPursueFee") + "");
					valueList.add(map.get("ePayTotalFee") + "");
					
					valueList.add(map.get("cardCustomFee") + "");
					valueList.add(map.get("cardPursueFee") + "");
					valueList.add(map.get("cardTotalFee") + "");
					
					valueList.add(map.get("totalFee") + "");
					valueList.add(map.get("escapeFee") + "");
					valueList.add(map.get("allTotalFee") + "");
					
					valueList.add(map.get("cardActFee") + "");
					valueList.add(map.get("cardChargeCashFee") + "");
					valueList.add(map.get("cardReturnFee") + "");
					bodyList.add(valueList);
				}
				String fname = "�շ�Ա�շѱ���" + btime + "��" + etime;
				java.io.OutputStream os = response.getOutputStream();
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ StringUtils.encodingFileName(fname) + ".xls");
				ExportExcelUtil importExcel = new ExportExcelUtil("�շ�Ա�շѱ���",
						heards, bodyList);
				List<Map<String,String>> mulitHeadList = new ArrayList<Map<String,String>>();
				Map<String, String> mhead0 = new HashMap<String, String>();
				mhead0.put("length", "0");
				mhead0.put("content", "");
				mulitHeadList.add(mhead0);
				Map<String, String> mhead1 = new HashMap<String, String>();
				mhead1.put("length", "0");
				mhead1.put("content", "");
				mulitHeadList.add(mhead1);
				Map<String, String> mhead7 = new HashMap<String, String>();
				mhead7.put("length", "0");
				mhead7.put("content", "");
				mulitHeadList.add(mhead7);
				Map<String, String> mhead2 = new HashMap<String, String>();
				mhead2.put("length", "2");
				mhead2.put("content", "ͣ����-�ֽ�֧��");
				mulitHeadList.add(mhead2);
				Map<String, String> mhead3 = new HashMap<String, String>();
				mhead3.put("length", "2");
				mhead3.put("content", "ͣ����-����֧��");
				mulitHeadList.add(mhead3);
				Map<String, String> mhead4 = new HashMap<String, String>();
				mhead4.put("length", "2");
				mhead4.put("content", "ͣ����-ˢ��֧��");
				mulitHeadList.add(mhead4);
				Map<String, String> mhead5 = new HashMap<String, String>();
				mhead5.put("length", "2");
				mhead5.put("content", "ͣ����-�ϼ�");
				mulitHeadList.add(mhead5);
				Map<String, String> mhead6 = new HashMap<String, String>();
				mhead6.put("length", "2");
				mhead6.put("content", "��Ƭ");
				mulitHeadList.add(mhead6);
				importExcel.mulitHeadList = mulitHeadList;
				Map<String, String> headInfoMap=new HashMap<String, String>();
				headInfoMap.put("length", heards.length - 1 + "");
				headInfoMap.put("content", fname);
				importExcel.headInfo = headInfoMap;
				importExcel.createExcelFile(os);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setList(List<Map<String, Object>> list, Long startTime, Long endTime){
		try {
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> infoMap : list){
					infoMap.put("cashPursueFee", 0);
					infoMap.put("cashCustomFee", 0);
					infoMap.put("cashTotalFee", 0);
					infoMap.put("ePayPursueFee", 0);
					infoMap.put("ePayCustomFee", 0);
					infoMap.put("ePayTotalFee", 0);
					infoMap.put("cardPursueFee", 0);
					infoMap.put("cardCustomFee", 0);
					infoMap.put("cardTotalFee", 0);
					infoMap.put("totalFee", 0);
					infoMap.put("escapeFee", 0);
					infoMap.put("allTotalFee", 0);
					infoMap.put("cardChargeCashFee", 0);
					infoMap.put("cardReturnFee", 0);
					infoMap.put("cardActFee", 0);
					infoMap.put("totalPursueFee", 0);
				}
				
				List<Object> idList = new ArrayList<Object>();
				String preParams = "";
				for(Map<String, Object> map : list){
					idList.add(map.get("id"));
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				List<Map<String, Object>> rList = commonMethods.getIncomeAnly(idList, startTime, endTime, 0);
				if(rList != null && !rList.isEmpty()){
					for(Map<String, Object> infoMap : rList){
						Long uin = (Long)infoMap.get("id");
						Double prepay_cash = Double.valueOf(infoMap.get("prepay_cash") + "");//�ֽ�Ԥ֧��
						Double add_cash = Double.valueOf(infoMap.get("add_cash") + "");//�ֽ𲹽�
						Double refund_cash = Double.valueOf(infoMap.get("refund_cash") + "");//�ֽ��˿�
						Double pursue_cash = Double.valueOf(infoMap.get("pursue_cash") + "");//�ֽ�׷��
						Double pfee_cash = Double.valueOf(infoMap.get("pfee_cash") + "");//�ֽ�ͣ���ѣ���Ԥ����
						
						Double prepay_epay = Double.valueOf(infoMap.get("prepay_epay") + "");//����Ԥ֧��
						Double add_epay = Double.valueOf(infoMap.get("add_epay") + "");//���Ӳ���
						Double refund_epay = Double.valueOf(infoMap.get("refund_epay") + "");//�����˿�
						Double pursue_epay = Double.valueOf(infoMap.get("pursue_epay") + "");//����׷��
						Double pfee_epay = Double.valueOf(infoMap.get("pfee_epay") + "");//����ͣ���ѣ���Ԥ����
						Double escape = Double.valueOf(infoMap.get("escape") + "");//�ӵ�δ׷�ɵ�ͣ����
						
						Double prepay_card = Double.valueOf(infoMap.get("prepay_card") + "");//ˢ��Ԥ֧��
						Double add_card = Double.valueOf(infoMap.get("add_card") + "");//ˢ������
						Double refund_card = Double.valueOf(infoMap.get("refund_card") + "");//ˢ���˿�
						Double pursue_card = Double.valueOf(infoMap.get("pursue_card") + "");//ˢ��׷��
						Double pfee_card = Double.valueOf(infoMap.get("pfee_card") + "");//ˢ��ͣ���ѣ���Ԥ����
						
						Double charge_card_cash = Double.valueOf(infoMap.get("charge_card_cash") + "");//��Ƭ�ֽ��ֵ���
						Double return_card_fee = Double.valueOf(infoMap.get("return_card_fee") + "");//ע����Ƭ�˻������
						Double act_card_fee = Double.valueOf(infoMap.get("act_card_fee") + "");//���Ƭ����ֵ����������ֵ��
						
						double cashCustomFee = StringUtils.formatDouble(pfee_cash + prepay_cash + add_cash - refund_cash);
						double epayCustomFee = StringUtils.formatDouble(pfee_epay + prepay_epay + add_epay - refund_epay);
						double cardCustomFee = StringUtils.formatDouble(pfee_card + prepay_card + add_card - refund_card);
						double cashTotalFee = StringUtils.formatDouble(pursue_cash + cashCustomFee);
						double ePayTotalFee = StringUtils.formatDouble(pursue_epay + epayCustomFee);
						double cardTotalFee = StringUtils.formatDouble(pursue_card + cardCustomFee);
						double totalFee = StringUtils.formatDouble(cashTotalFee + ePayTotalFee + cardTotalFee);
						double allTotalFee = StringUtils.formatDouble(totalFee + escape);
						double totalPursueFee = StringUtils.formatDouble(pursue_cash + pursue_epay + pursue_card);
						
						for(Map<String, Object> map : list){
							Long id = (Long)map.get("id");
							if(id.intValue() == uin.intValue()){
								map.put("cashPursueFee", pursue_cash);
								map.put("cashCustomFee", cashCustomFee);
								map.put("cashTotalFee", cashTotalFee);
								map.put("ePayPursueFee", pursue_epay);
								map.put("ePayCustomFee", epayCustomFee);
								map.put("ePayTotalFee", ePayTotalFee);
								map.put("cardPursueFee", pursue_card);
								map.put("cardCustomFee", cardCustomFee);
								map.put("cardTotalFee", cardTotalFee);
								map.put("totalFee", totalFee);
								map.put("escapeFee", escape);
								map.put("allTotalFee", allTotalFee);
								map.put("cardChargeCashFee", charge_card_cash);
								map.put("cardReturnFee", return_card_fee);
								map.put("cardActFee", act_card_fee);
								map.put("totalPursueFee", totalPursueFee);
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String setTitle(List<Map<String, Object>> list, Long startTime, Long endTime){
		double cashTotalFee = 0;
		double ePayTotalFee = 0;
		double cardTotalFee = 0;
		double chargeCardFee = 0;//��Ƭ��ֵ
		double returnCardFee = 0;//�˿����
		double actCardFee = 0;//������ֵ
		try {
			if(list != null && !list.isEmpty()){
				List<Object> collectors = new ArrayList<Object>();
				for(Map<String, Object> map : list){
					collectors.add(map.get("id"));
				}
				Map<String, Object> infoMap = commonMethods.sumIncomeAnly(collectors, startTime, endTime, 0);
				if(infoMap != null && !infoMap.isEmpty()){
					Double cashPrepayFee = 0d;//�ֽ�Ԥ֧��
					Double cashAddFee = 0d;//�ֽ𲹽�
					Double cashRefundFee = 0d;//�ֽ��˿�
					Double cashPursueFee = 0d;//�ֽ�׷��
					Double cashParkingFee = 0d;//�ֽ�ͣ���ѣ���Ԥ����
					Double ePayPrepayFee = 0d;//����Ԥ֧��
					Double ePayAddFee = 0d;//���Ӳ���
					Double ePayRefundFee = 0d;//�����˿�
					Double ePayPursueFee = 0d;//����׷��
					Double ePayParkingFee = 0d;//����ͣ���ѣ���Ԥ����
					Double escapeFee = 0d;//�ӵ�δ׷�ɵ�ͣ����
					Double cardPrepayFee = 0d;//ˢ��Ԥ֧��
					Double cardAddFee = 0d;//ˢ������
					Double cardRefundFee = 0d;//ˢ���˿�
					Double cardPursueFee = 0d;//ˢ��׷��
					Double cardParkingFee = 0d;//ˢ��ͣ���ѣ���Ԥ����
					if(infoMap.get("prepay_cash") != null){
						cashPrepayFee = Double.valueOf(infoMap.get("prepay_cash") + "");
					}
					if(infoMap.get("add_cash") != null){
						cashAddFee = Double.valueOf(infoMap.get("add_cash") + "");
					}
					if(infoMap.get("refund_cash") != null){
						cashRefundFee = Double.valueOf(infoMap.get("refund_cash") + "");
					}
					if(infoMap.get("pursue_cash") != null){
						cashPursueFee = Double.valueOf(infoMap.get("pursue_cash") + "");
					}
					if(infoMap.get("pfee_cash") != null){
						cashParkingFee = Double.valueOf(infoMap.get("pfee_cash") + "");
					}
					if(infoMap.get("prepay_epay") != null){
						ePayPrepayFee = Double.valueOf(infoMap.get("prepay_epay") + "");
					}
					if(infoMap.get("add_epay") != null){
						ePayAddFee = Double.valueOf(infoMap.get("add_epay") + "");
					}
					if(infoMap.get("refund_epay") != null){
						ePayRefundFee = Double.valueOf(infoMap.get("refund_epay") + "");
					}
					if(infoMap.get("pursue_epay") != null){
						ePayPursueFee = Double.valueOf(infoMap.get("pursue_epay") + "");
					}
					if(infoMap.get("pfee_epay") != null){
						ePayParkingFee = Double.valueOf(infoMap.get("pfee_epay") + "");
					}
					if(infoMap.get("prepay_card") != null){
						cardPrepayFee = Double.valueOf(infoMap.get("prepay_card") + "");
					}
					if(infoMap.get("add_card") != null){
						cardAddFee = Double.valueOf(infoMap.get("add_card") + "");
					}
					if(infoMap.get("refund_card") != null){
						cardRefundFee = Double.valueOf(infoMap.get("refund_card") + "");
					}
					if(infoMap.get("pursue_card") != null){
						cardPursueFee = Double.valueOf(infoMap.get("pursue_card") + "");
					}
					if(infoMap.get("pfee_card") != null){
						cardParkingFee = Double.valueOf(infoMap.get("pfee_card") + "");
					}
					if(infoMap.get("charge_card_cash") != null){
						chargeCardFee = Double.valueOf(infoMap.get("charge_card_cash") + "");
					}
					if(infoMap.get("return_card_fee") != null){
						returnCardFee = Double.valueOf(infoMap.get("return_card_fee") + "");
					}
					if(infoMap.get("act_card_fee") != null){
						actCardFee = Double.valueOf(infoMap.get("act_card_fee") + "");
					}
					
					cashTotalFee = StringUtils.formatDouble(cashParkingFee + cashPrepayFee + 
							cashAddFee + cashPursueFee - cashRefundFee);
					ePayTotalFee = StringUtils.formatDouble(ePayParkingFee + ePayPrepayFee + 
							ePayAddFee + ePayPursueFee - ePayRefundFee);
					cardTotalFee = StringUtils.formatDouble(cardParkingFee + cardPrepayFee + 
							cardAddFee + cardPursueFee - cardRefundFee);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String res = "ͣ����-->�ֽ�֧����"+cashTotalFee+"Ԫ������֧����"+ePayTotalFee+
				"Ԫ����Ƭ֧����"+cardTotalFee+"Ԫ"+"����Ƭ-->��Ƭ��ֵ��"+chargeCardFee+"Ԫ���ۿ�����ֵ��"+
				actCardFee+"Ԫ��ע������ֵ��"+returnCardFee+"Ԫ";
		return res;
	}
}
