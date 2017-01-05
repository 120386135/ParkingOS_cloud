package com.zld.struts.anlysis;

import java.text.SimpleDateFormat;
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
import com.zld.facade.StatsAccountFacade;
import com.zld.impl.CommonMethods;
import com.zld.pojo.StatsAccountClass;
import com.zld.pojo.StatsFacadeResp;
import com.zld.pojo.StatsReq;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;

public class StatsBerthAccountAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private StatsAccountFacade accountFacade;
	@Autowired
	private CommonMethods commonMethods;
	
	private Logger logger = Logger.getLogger(StatsBerthAccountAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//��¼���û�id
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
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
			if(groupid == -1){//�����̻�Ҫ��ɰ�����Ӫ����ɸ��
				groupid = RequestUtil.getLong(request, "groupid", -1L);
			}
			SqlInfo sqlInfo = RequestUtil.customSearch(request, "com_park");
			String sql = "select * from com_park_tb where is_delete=? ";
			String countSql = "select count(id) from com_park_tb where is_delete=? ";
			String sumSql = " select id from com_park_tb where is_delete=? ";
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			String res = "";
			List<Map<String, Object>> list = null;
			Long count = 0L;
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and comid in ("+preParams+") ";
				countSql += " and comid in ("+preParams+") ";
				sumSql += " and comid in ("+preParams+") ";
				params.addAll(parks);
				
				if(sqlInfo != null) {
					countSql += " and "+ sqlInfo.getSql();
					sql += " and "+sqlInfo.getSql();
					sumSql += " and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				count = pgOnlyReadService.getCount(countSql,params);
				
				if(count > 0){
					List<Map<String, Object>> berthList = pgOnlyReadService.getAllMap(sumSql, params);
					res = setTitle(berthList, b, e);
					list = pgOnlyReadService.getAll(sql +" order by id desc ", params, pageNum, pageSize);
					setList(list, b, e);
				}
			}
			
			String json = JsonUtil.anlysisMap2Json(list, pageNum, count, fieldsstr, "id", res);
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}
	
	private String setTitle(List<Map<String, Object>> list, Long startTime, Long endTime){
		double cashTotalFee = 0;
		double ePayTotalFee = 0;
		double cardTotalFee = 0;
		try {
			if(list != null && !list.isEmpty()){
				List<Object> idList = new ArrayList<Object>();
				String preParam = "";
				for(Map<String, Object> map : list){
					idList.add(map.get("id"));
					if(preParam.equals("")){
						preParam = "?";
					}else{
						preParam += ",?";
					}
				}
				List<Object> params = new ArrayList<Object>();
				params.add(startTime);
				params.add(endTime);
				params.addAll(idList);
				params.add(1);
				
				String sql = "select sum(prepay_cash) as prepay_cash," +
						"sum(add_cash) as add_cash,sum(refund_cash) as refund_cash,sum(pursue_cash) as pursue_cash,sum(pfee_cash) as pfee_cash," +
						"sum(prepay_epay) as prepay_epay,sum(add_epay) as add_epay,sum(refund_epay) as refund_epay,sum(pursue_epay) as pursue_epay," +
						"sum(pfee_epay) as pfee_epay,sum(escape) as escape,sum(prepay_escape) as prepay_escape,sum(sensor_fee) as sensor_fee," +
						"sum(prepay_card) as prepay_card,sum(add_card) as add_card,sum(refund_card) as refund_card,sum(pursue_card) as pursue_card," +
						"sum(pfee_card) as pfee_card from parkuser_income_anlysis_tb where create_time between ? and ? and uin in ("+preParam+") and " +
						" type=? ";
				Map<String, Object> infoMap = pgOnlyReadService.getMap(sql, params);
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
					
					double cashCustomFee = StringUtils.formatDouble(cashParkingFee + cashPrepayFee + cashAddFee - cashRefundFee);
					double epayCustomFee = StringUtils.formatDouble(ePayParkingFee + ePayPrepayFee + ePayAddFee - ePayRefundFee);
					double cardCustomFee = StringUtils.formatDouble(cardParkingFee + cardPrepayFee + cardAddFee - cardRefundFee);
					cashTotalFee = StringUtils.formatDouble(cashPursueFee + cashCustomFee);
					ePayTotalFee = StringUtils.formatDouble(ePayPursueFee + epayCustomFee);
					cardTotalFee = StringUtils.formatDouble(cardPursueFee + cardCustomFee);
					double totalFee = StringUtils.formatDouble(cashTotalFee + ePayTotalFee + cardTotalFee);
					double allTotalFee = StringUtils.formatDouble(totalFee + escapeFee);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String res = "ͣ����-->�ֽ�֧����"+cashTotalFee+"Ԫ������֧����"+ePayTotalFee+"Ԫ����Ƭ֧����"+cardTotalFee+"Ԫ";
		return res;
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
				}
				
				
				List<Object> idList = new ArrayList<Object>();
				String preParam = "";
				for(Map<String, Object> map : list){
					idList.add(map.get("id"));
					if(preParam.equals("")){
						preParam = "?";
					}else{
						preParam += ",?";
					}
				}
				List<Map<String, Object>> incomeList = commonMethods.getIncomeAnly(idList, startTime, endTime, 1);
				if(incomeList != null && !incomeList.isEmpty()){
					for(Map<String, Object> infoMap : incomeList){
						Long id = (Long)infoMap.get("id");
						Double cashPrepayFee = Double.valueOf(infoMap.get("prepay_cash") + "");//�ֽ�Ԥ֧��
						Double cashAddFee = Double.valueOf(infoMap.get("add_cash") + "");//�ֽ𲹽�
						Double cashRefundFee = Double.valueOf(infoMap.get("refund_cash") + "");//�ֽ��˿�
						Double cashPursueFee = Double.valueOf(infoMap.get("pursue_cash") + "");//�ֽ�׷��
						Double cashParkingFee = Double.valueOf(infoMap.get("pfee_cash") + "");//�ֽ�ͣ���ѣ���Ԥ����
						Double ePayPrepayFee = Double.valueOf(infoMap.get("prepay_epay") + "");//����Ԥ֧��
						Double ePayAddFee = Double.valueOf(infoMap.get("add_epay") + "");//���Ӳ���
						Double ePayRefundFee = Double.valueOf(infoMap.get("refund_epay") + "");//�����˿�
						Double ePayPursueFee = Double.valueOf(infoMap.get("pursue_epay") + "");//����׷��
						Double ePayParkingFee = Double.valueOf(infoMap.get("pfee_epay") + "");//����ͣ���ѣ���Ԥ����
						Double escapeFee = Double.valueOf(infoMap.get("escape") + "");//�ӵ�δ׷�ɵ�ͣ����
						Double cardPrepayFee = Double.valueOf(infoMap.get("prepay_card") + "");//ˢ��Ԥ֧��
						Double cardAddFee = Double.valueOf(infoMap.get("add_card") + "");//ˢ������
						Double cardRefundFee = Double.valueOf(infoMap.get("refund_card") + "");//ˢ���˿�
						Double cardPursueFee = Double.valueOf(infoMap.get("pursue_card") + "");//ˢ��׷��
						Double cardParkingFee = Double.valueOf(infoMap.get("pfee_card") + "");//ˢ��ͣ���ѣ���Ԥ����
						
						double cashCustomFee = StringUtils.formatDouble(cashParkingFee + cashPrepayFee + cashAddFee - cashRefundFee);
						double epayCustomFee = StringUtils.formatDouble(ePayParkingFee + ePayPrepayFee + ePayAddFee - ePayRefundFee);
						double cardCustomFee = StringUtils.formatDouble(cardParkingFee + cardPrepayFee + cardAddFee - cardRefundFee);
						double cashTotalFee = StringUtils.formatDouble(cashPursueFee + cashCustomFee);
						double ePayTotalFee = StringUtils.formatDouble(ePayPursueFee + epayCustomFee);
						double cardTotalFee = StringUtils.formatDouble(cardPursueFee + cardCustomFee);
						double totalFee = StringUtils.formatDouble(cashTotalFee + ePayTotalFee + cardTotalFee);
						double allTotalFee = StringUtils.formatDouble(totalFee + escapeFee);
						
						for(Map<String, Object> map : list){
							Long berthId = (Long)map.get("id");
							if(berthId.intValue() == id.intValue()){
								map.put("cashPursueFee", cashPursueFee);
								map.put("cashCustomFee", cashCustomFee);
								map.put("cashTotalFee", cashTotalFee);
								map.put("ePayPursueFee", ePayPursueFee);
								map.put("ePayCustomFee", epayCustomFee);
								map.put("ePayTotalFee", ePayTotalFee);
								map.put("cardPursueFee", cardPursueFee);
								map.put("cardCustomFee", cardCustomFee);
								map.put("cardTotalFee", cardTotalFee);
								map.put("totalFee", totalFee);
								map.put("escapeFee", escapeFee);
								map.put("allTotalFee", allTotalFee);
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
}
