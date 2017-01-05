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
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZLDType;

/**
 * ��������ͳ��
 * @author Administrator
 *
 */
public class ParkOrderanlysisAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	
	private Logger logger = Logger.getLogger(ParkOrderanlysisAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Integer role = RequestUtil.getInteger(request, "role",-1);
		Long uin = (Long)request.getSession().getAttribute("loginuin");//��¼���û�id
		request.setAttribute("authid", request.getParameter("authid"));
		Integer isHd = (Integer)request.getSession().getAttribute("ishdorder");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(ZLDType.ZLD_ACCOUNTANT_ROLE==role||ZLDType.ZLD_CARDOPERATOR==role)
			request.setAttribute("role", role);
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		
		if(comid == 0){
			comid = RequestUtil.getLong(request, "comid", 0L);
		}
		request.setAttribute("groupid", groupid);
		request.setAttribute("cityid", cityid);
		if(comid == 0){
			comid = getComid(comid, cityid, groupid);
		}
		if(action.equals("")){
//			String monday = StringUtils.getMondayOfThisWeek();
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("btime", df2.format(System.currentTimeMillis()));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String type = RequestUtil.processParams(request, "type");
			String sql = "select count(*) scount,sum(total) total,uid from order_tb  ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;//daService.getPage(sql, null, 1, 20);
			List freeorder = null;
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			if(btime.equals(""))
				btime = nowtime;
			if(etime.equals(""))
				etime = nowtime;
			SqlInfo sqlInfo =null;
			List<Object> params = null;
			Long b = TimeTools.getToDayBeginTime();
			Long e = System.currentTimeMillis()/1000;
			String dstr = btime+"-"+etime;
			if(type.equals("today")){
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
				dstr = "����";
			}else if(type.equals("toweek")){
				b = TimeTools.getWeekStartSeconds();
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
				dstr = "����";
			}else if(type.equals("lastweek")){
				e = TimeTools.getWeekStartSeconds();
				b= e-7*24*60*60;
				e = e-1;
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
				dstr = "����";
			}else if(type.equals("tomonth")){
				b=TimeTools.getMonthStartSeconds();
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
				dstr="����";
			}else if(!btime.equals("")&&!etime.equals("")){
				b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
				e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}
			sql +=" where "+sqlInfo.getSql()+" and comid=?  and state= ? and uid> ? and ishd=? ";
			List<Object> subParams =new ArrayList<Object>();
			params= sqlInfo.getParams();
			for(Object object :params){
				subParams.add(object);
			}
			params.add(comid);
			params.add(1);
			params.add(0);
			params.add(0);
			
			list = pgOnlyReadService.getAllMap(sql +" group by uid order by scount desc ",params);
			freeorder = pgOnlyReadService.getAllMap(sql +" and pay_type=8 group by uid order by scount desc ",params);//��ѯ��ѵ�
			int count = list!=null?list.size():0;
			int freeordercount = freeorder!=null?freeorder.size():0;
			//setName(list);
			String tc = "0_0_0_0";
			String tc2 = "0_0_0_0";
			if(list!=null)
				 tc=setName(list,dstr);
			if(freeorder!=null)
				 tc2=setName(freeorder,dstr);
			Double tmoney=0d;
			Double ticketmoney=0d;
			Double centermoney=0d;
			Double tcbmoney=0d;
			Integer monthordercount=0;
			//�����շѷ�ʽ
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				
				
				Long uid = (Long)map.get("uid");
				
				//ͳ��ͣ��ȯ֧��������Ԥ֧��
				String ticketAndCenter = commonMethods.getTicketAndCenterPay(uid,b,e,isHd,comid);
				Double ticket = Double.parseDouble(ticketAndCenter.split("_")[0]);
				ticketmoney+=ticket;
				map.put("ticketpay",StringUtils.formatDouble(ticket));
				Double center = Double.parseDouble(ticketAndCenter.split("_")[1]);
				centermoney=+center;
				map.put("centerprepay",StringUtils.formatDouble(center));
				//ͳ���ֽ�֧��
				Double cash = Double.parseDouble(ticketAndCenter.split("_")[2]);
				tmoney +=cash;
				map.put("pmoney", StringUtils.formatDouble(cash));
				Double free = 0.0;
				for(int j=0;j<freeordercount;j++){
					Map order = (Map)freeorder.get(j);
					if(((Long)order.get("uid")).longValue()==uid.longValue()){
						Double value = Double.valueOf(order.get("total")+"");
						free+=value;
					}
					
				}
				//����¿�������ͳ��
				int monthcount =0;
				String msql = "select count(*) scount from order_tb  where  " +
						"end_time between ? and ? and comid=? and state= ? and uid =? and c_type=? and ishd=? ";
				Object v[] = new Object[]{b,e,comid,1,uid,5, 0};
				
				Map monthorder = daService.getMap(msql,v);
				if(monthorder!=null&&monthorder.get("scount")!=null){
					monthcount = Integer.valueOf(monthorder.get("scount")+"");
					monthordercount+=monthcount;
				}
				map.put("monthcount", monthcount);
				map.put("free", StringUtils.formatDouble(free));//ͳ����ѽ��
//				map.put("pmobile", StringUtils.formatDouble(total-money-free-center-ticket));//ͣ����֧�����
				Double tcb = Double.valueOf(ticketAndCenter.split("_")[3]);
				map.put("pmobile", tcb);//ͣ����֧�����
				tcbmoney+=tcb;
				
				Double total = cash + tcb + free + center + ticket;//�ܽ��
				map.put("total", total);
			}
			Double all = Double.valueOf(tc.split("_")[0]);//�ܵĽ��
			Double free = Double.valueOf(tc2.split("_")[0]);//����ܽ��
			String money = "�ܶ�������"+tc.split("_")[1]+",�¿�������:"+monthordercount+",�ܽ�����:"+StringUtils.formatDouble(tmoney+tcbmoney+free)+"Ԫ,�ֽ�֧��:"+StringUtils.formatDouble(tmoney)+"Ԫ,ͣ����֧�� :"+StringUtils.formatDouble(tcbmoney)+"Ԫ,��ѽ��:"+StringUtils.formatDouble(free)+"Ԫ";
			String json = JsonUtil.anlysisMap2Json(list,1,count, fieldsstr,"uid",money);
			System.out.println(json);
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("detail")){
			requestUtil(request);
			return mapping.findForward("detail");
		}else if(action.equals("work")){
			requestUtil(request);
			return mapping.findForward("work");
		}else if(action.equals("workdetail")){
			String bt = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "btime"));
			String et = RequestUtil.processParams(request, "etime");
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String pay_type = RequestUtil.processParams(request, "pay_type");
			String type = RequestUtil.processParams(request, "otype");
			Long btime = TimeTools.getToDayBeginTime();
			Long etime = System.currentTimeMillis()/1000;
			List list = null;//daService.getPage(sql, null, 1, 20);
			List freeorder = null;
			if(type.equals("today")){
			}else if(type.equals("toweek")){
				btime = TimeTools.getWeekStartSeconds();
			}else if(type.equals("lastweek")){
				etime = TimeTools.getWeekStartSeconds();
				btime= etime-7*24*60*60;
				etime = etime-1;
			}else if(type.equals("tomonth")){
				btime=TimeTools.getMonthStartSeconds();
			}else if(type.equals("custom")){
				btime = TimeTools.getLongMilliSecondFrom_HHMMDD(bt)/1000;
				etime =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(et+" 23:59:59");
			}else if(!bt.equals("")&&!et.equals("")){
				btime = TimeTools.getLongMilliSecondFrom_HHMMDD(bt)/1000;
				etime =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(et+" 23:59:59");
			}
			long uid = RequestUtil.getLong(request, "uid", -1L);
			String sql = "select a.id,a.start_time,a.end_time,a.uid,b.worksite_name worksite_id from parkuser_work_record_tb a ,com_worksite_tb b where (a.end_time " +
					"between ? and ? or a.start_time between ? and ? or (a.start_time between ? and ? and (a.end_time>? or a.end_time is null))) and a.uid = ? and b.id=a.worksite_id ";// order by a.end_time desc";//��ѯ�ϰ���Ϣ
			List<Object> params = new ArrayList();
			params.add(btime);
			params.add(etime);
			params.add(btime);
			params.add(etime);
			params.add(btime);
			params.add(etime);
			params.add(etime);
			params.add(uid);
			
			sql +=" order by a.end_time desc";
			list = daService.getAllMap(sql,params);
			
			double totalmoney = 0.0;
			double freemoney = 0.0;
			double money = 0.0;
			int count =0;
			int monthcount =0;
			Double ticketmoney=0d;
			Double centermoney=0d;
			Double tcb = 0d;
			for (int i = 0; i < list.size(); i++) {//ѭ����֯ÿ�����ͳ��
				List<Object> p = new ArrayList(); 
				Map work = (Map)list.get(i);
				long start_time = (Long)work.get("start_time");
				long end_time = Long.MAX_VALUE;
				try {
					end_time = (Long)work.get("end_time");
				} catch (Exception e) {
				}
				p.add(start_time);
				p.add(end_time);
				p.add(1);
				p.add(uid);
				p.add(0);
				List list2 = new ArrayList();//�ܵĶ���
				List list3 = new ArrayList();//���
				List list4 = new ArrayList();//�ֽ�

					//�ܵĶ��������ܵĽ��
				String sql2 = "select count(*) ordertotal,sum(total) total from order_tb where end_time between ? and ? " +
						" and state= ? and uid = ? and ishd=? ";
				list2 = daService.getAllMap(sql2 ,p);
				String sql5 = "select count(*) ordertotal from order_tb where end_time between ? and ? " +
						" and state= ? and uid = ? and c_type =? and ishd=? ";
				Object []v5 = new Object[]{start_time,end_time,1,uid,5, 0};
				Map list5 = daService.getMap(sql5 ,v5);
				work.put("monthcount", list5.get("ordertotal"));
				monthcount+=Integer.parseInt(list5.get("ordertotal")+"");
				count+=Integer.parseInt((((Map)list2.get(0)).get("ordertotal"))+"");
				if(list2!=null&&list2.size()==1){
					double total =0;
					int ordertotal = 0;
					double free = 0 ;
					try{
						total = Double.parseDouble((((Map)list2.get(0)).get("total"))+"");
						ordertotal = Integer.parseInt((((Map)list2.get(0)).get("ordertotal"))+"");
						
					}catch (Exception e) {
						total=0.0;
					}
					work.put("total",StringUtils.formatDouble(total));
					work.put("ordertotal",ordertotal);
					totalmoney+=total;
					//��ѵĽ��Ͷ�����
					String sql3 = "select count(*) ordertotal,sum(total) total from order_tb where end_time between ? and ? " +
							" and state= ? and uid = ? and pay_type = 8 and ishd=? ";
					list3 = daService.getAllMap(sql3,p);
					if(list3!=null&&list3.size()==1){
						try{
							free = Double.parseDouble((((Map)list3.get(0)).get("total"))+"");
						}catch (Exception e) {
							free=0.0;
						}
						work.put("free",StringUtils.formatDouble(free));
						freemoney+=free;
					}
					
					List<Object> subparams = new ArrayList();
					long b = Long.valueOf(work.get("start_time")+"");
					subparams.add(b);
					long e = work.get("end_time")!=null?Long.valueOf(work.get("end_time")+""):Long.MAX_VALUE;
					subparams.add(e);
					SqlInfo sqlInfo = new SqlInfo(" end_time between ? and ? ",
							new Object[]{btime,etime});
					//ͣ��ȯ֧��������Ԥ֧���Ľ��
					String ticketAndCenter = commonMethods.getTicketAndCenterPay(uid,b,e,isHd,comid);
					Double ticket = Double.parseDouble(ticketAndCenter.split("_")[0]);
					ticketmoney+=ticket;
					work.put("ticketpay",StringUtils.formatDouble(ticket));
					
					work.put("total",StringUtils.formatDouble(total + ticket));
					
					Double center = Double.parseDouble(ticketAndCenter.split("_")[1]);
					centermoney=+center;
					work.put("centerprepay",StringUtils.formatDouble(center));
					Double cash = Double.parseDouble(ticketAndCenter.split("_")[2]);
					work.put("money",StringUtils.formatDouble(cash));
					money+=cash;//�ֽ�
//					double pmoney = new BigDecimal(total+"").subtract(new BigDecimal(m+"")).subtract(new BigDecimal(free+"")).subtract(new BigDecimal(ticket+"")).subtract(new BigDecimal(center+"")).doubleValue();
					tcb = Double.valueOf(ticketAndCenter.split("_")[3]);
					work.put("pmoney", tcb);
				}
			}
			Double pmoney = 0d;
			
			String title = "�ܶ�������"+count+"���¿���������"+monthcount+"���ܽ����"+StringUtils.formatDouble(totalmoney)+"Ԫ�������ֽ�֧����"+StringUtils.formatDouble(money)+"Ԫ��ͣ����֧�� ��"+StringUtils.formatDouble(tcb)+"Ԫ������Ԥ֧����"+StringUtils.formatDouble(centermoney)+"Ԫ������ȯ֧����"+StringUtils.formatDouble(ticketmoney)+"Ԫ����ѽ�"+StringUtils.formatDouble(freemoney)+"Ԫ";
			String ret = JsonUtil.anlysisMap2Json(list,1,list.size(), fieldsstr,"id",title);
			logger.error(ret);
			AjaxUtil.ajaxOutput(response, ret);
			return null;			
			
		}else if(action.equals("orderdetail")){
			String sql = "select * from order_tb  ";
			Long uid = RequestUtil.getLong(request, "uid", -2L);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			String btime = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "btime"));
			String etime = RequestUtil.processParams(request, "etime");
			String type = RequestUtil.processParams(request, "otype");
			SqlInfo sqlInfo =null;
			List<Object> params = null;
			Long b = TimeTools.getToDayBeginTime();
			Long e = System.currentTimeMillis()/1000;
			if(type.equals("today")){
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(type.equals("toweek")){
				b = TimeTools.getWeekStartSeconds();
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(type.equals("lastweek")){
				e = TimeTools.getWeekStartSeconds();
				b= e-7*24*60*60;
				e = e-1;
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(type.equals("tomonth")){
				b=TimeTools.getMonthStartSeconds();
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(type.equals("workcustom")){
				b = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime);
				e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime);
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}else if(!btime.equals("")&&!etime.equals("")){
				b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
				e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
				sqlInfo =new SqlInfo(" end_time between ? and ? ",
						new Object[]{b,e});
			}
			
			sql +=" where "+sqlInfo.getSql()+" and uid=?  and state= ? and comid=? and ishd=?  ";
			params= sqlInfo.getParams();
			params.add(uid);
			params.add(1);
			params.add(comid);
			params.add(0);
			
			if(uid!=-2){
				String pay_type = RequestUtil.processParams(request, "pay_type");
				if(pay_type!=null&&"8".equals(pay_type)){
					list = daService.getAllMap(sql+" and pay_type=8 order by end_time desc",params);
					request.setAttribute("countfree", list.size());
				}else if(pay_type!=null&&"1".equals(pay_type)){//��ѯ�ֽ�
					List cashlist = daService.getAll("select orderid, amount from parkuser_cash_tb where is_delete=? and uin=? and type=? and create_time between ? and ? order by create_time desc",
					new Object[]{0, uid,0,params.get(0),params.get(1)});
					for (int j = 0; j < cashlist.size(); j++) {
						long orderId = Long.parseLong(((Map)cashlist.get(j)).get("orderid")+"");
						String msql = "select * from order_tb where id =? and ishd=? ";
						Object []mv =  new Object[]{orderId, 0};
						Map map = daService.getMap(msql,mv);
						if(map!=null){
							map.put("amount", StringUtils.formatDouble(((Map)cashlist.get(j)).get("amount")));
							list.add(map);
						}
					}
				}else{
					list = daService.getAllMap(sql+" order by end_time desc ",params);
				}
				for (int i = 0; i < list.size(); i++) {
					Map orderMap = list.get(i);
					Double total = Double.valueOf(orderMap.get("total")+"");
					long orderId = Long.parseLong(orderMap.get("id")+"");
					Map tmap = daService.getPojo("select umoney from ticket_tb where orderid = ?", new Object[]{orderId});
					if(tmap!=null&&tmap.get("umoney")!=null){
						Double umoney = Double.valueOf(tmap.get("umoney")+"");
						total += umoney;
						orderMap.put("umoney",Double.valueOf(tmap.get("umoney")+""));
					}
					orderMap.put("atotal", total);
					Map cmap = daService.getPojo("select amount from parkuser_cash_tb where is_delete=? and orderid = ? and type = ?",
							new Object[]{0, orderId, 1});
					if(cmap!=null&&cmap.get("amount")!=null){
						orderMap.put("center",Double.valueOf(cmap.get("amount")+""));
					}
					Map pmap = daService.getPojo("select amount from parkuser_cash_tb where is_delete=? and orderid = ? and type = ?",
							new Object[]{0, orderId, 0});
					if(pmap!=null&&pmap.get("amount")!=null){
						orderMap.put("amount",Double.valueOf(pmap.get("amount")+""));
					}
				}
				int count = list!=null?list.size():0;
				String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
				AjaxUtil.ajaxOutput(response, json);
				return null;
			}else {
				AjaxUtil.ajaxOutput(response, "{\"page\":1,\"total\":0,\"rows\":[]}");
			}
		}
		return null;
	}

	private String setName(List list,String dstr){
		List<Object> uins = new ArrayList<Object>();
		String total_count="";
		Double total = 0d;
		Long count = 0l;
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				uins.add(map.get("uid"));
				Double t = Double.valueOf(map.get("total")+"");
				Long c = (Long)map.get("scount");
				map.put("sdate", dstr);
				map.put("total", StringUtils.formatDouble(t));
				total+=t;
				count+=c;
			}
		}
		if(!uins.isEmpty()){
			String preParams  ="";
			for(Object uin : uins){
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> resultList = daService.getAllMap("select id,nickname  " +
					"from user_info_tb " +
					" where id in ("+preParams+") ", uins);
			if(resultList!=null&&!resultList.isEmpty()){
				for(int i=0;i<list.size();i++){
					Map map1 = (Map)list.get(i);
					for(Map<String,Object> map: resultList){
						Long uin = (Long)map.get("id");
						if(map1.get("uid").equals(uin)){
							map1.put("name", map.get("nickname"));
							break;
						}
					}
				}
			}
		}
		return total+"_"+count;
	}
	
	private void setList(List<Map<String, Object>> lists,String dstr){
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		List<Long> comidList = new ArrayList<Long>();
		for(Map<String, Object> map :lists){
			//Long comId = (Long)map.get("comid");
			//Integer state = (Integer)map.get("state");
			map.put("sdate", dstr);
//			if(state==1){
//				comidList.add(comId);
//				result.add(map);
//			}else {
//				if(comidList.contains(comId)){
//					for(Map<String, Object> dMap : result){
//						Long cid = (Long)dMap.get("comid");
//						if(cid.intValue()==comId.intValue()){
//							dMap.put("corder",map.get("scount"));
//							break;
//						}
//					}
//				}else {
//					map.put("corder", map.get("scount"));
//					map.put("scount", null);
//					result.add(map);
//				}
//			}
		}
	}
	private void requestUtil(HttpServletRequest request){
		request.setAttribute("uid", RequestUtil.processParams(request, "uid"));
		request.setAttribute("btime", RequestUtil.processParams(request, "btime"));
		request.setAttribute("etime", RequestUtil.processParams(request, "etime"));
		request.setAttribute("otype", RequestUtil.processParams(request, "otype"));
		Integer paytype = RequestUtil.getInteger(request, "pay_type", 0);
		request.setAttribute("pay_type",paytype);
		if(paytype==8){
			request.setAttribute("total", RequestUtil.getDouble(request, "free", 0d));
		}else{
			request.setAttribute("total", RequestUtil.getDouble(request, "total", 0d));
		}
		request.setAttribute("free", RequestUtil.getDouble(request, "free", 0d));
		request.setAttribute("pmoney", RequestUtil.getDouble(request, "pmoney", 0d));
		request.setAttribute("pmobile", RequestUtil.getDouble(request, "pmobile", 0d));
		request.setAttribute("count", RequestUtil.getInteger(request, "count", 0));
		request.setAttribute("comid", RequestUtil.getInteger(request, "comid", 0));
		request.setAttribute("pay_type", RequestUtil.getInteger(request, "pay_type", 0));
	}
	
	private Double getPayMoney2 (Long uid,Long comid,SqlInfo sqlInfo,List<Object> params){
		String sql ="select sum(total) money from order_tb where comid=? and pay_type=? and uid=? and "+sqlInfo.getSql();
//		params.add(0,uid);
//		params.add(0,1);
//		params.add(0,comid);
		Object[] values = new Object[]{comid,1,uid,params.get(0),params.get(1)};
		Map map = pgOnlyReadService.getMap(sql, values);
		if(map!=null&&map.get("money")!=null)
			return Double.valueOf(map.get("money")+"");
		return 0d;
	}
	//�ֽ�֧��
	private Double getPayMoney (Long uid,List<Object> params){
		String sql ="select sum(amount) money from parkuser_cash_tb where uin=? and type=? and create_time between ? and ? ";
		Object[] values = new Object[]{uid,0,params.get(0),params.get(1)};
		Map map = daService.getMap(sql, values);
		if(map!=null&&map.get("money")!=null)
			return Double.valueOf(map.get("money")+"");
		return 0d;
	}
	
	private Long getComid(Long comid, Long cityid, Long groupid){
		List<Object> parks = null;
		if(groupid != null && groupid > 0){
			parks = commonMethods.getParks(groupid);
			if(parks != null && !parks.isEmpty()){
				comid = (Long)parks.get(0);
			}else{
				comid = -999L;
			}
		}else if(cityid != null && cityid > 0){
			parks = commonMethods.getparks(cityid);
			if(parks != null && !parks.isEmpty()){
				comid = (Long)parks.get(0);
			}else{
				comid = -999L;
			}
		}
		
		return comid;
	}
}