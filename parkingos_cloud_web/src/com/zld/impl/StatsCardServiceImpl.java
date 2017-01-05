package com.zld.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zld.pojo.AccountReq;
import com.zld.pojo.AccountResp;
import com.zld.pojo.QueryCount;
import com.zld.pojo.QueryList;
import com.zld.pojo.StatsCard;
import com.zld.pojo.StatsCardResp;
import com.zld.pojo.StatsReq;
import com.zld.service.PgOnlyReadService;
import com.zld.service.StatsCardService;
import com.zld.utils.ExecutorsUtil;
import com.zld.utils.SqlInfo;

@Service("card")
public class StatsCardServiceImpl implements StatsCardService {
	@Autowired
	private PgOnlyReadService readService;
	
	Logger logger = Logger.getLogger(StatsCardServiceImpl.class);
	
	@Override
	public StatsCardResp statsCard(StatsReq req) {
		//logger.error(req.toString());
		StatsCardResp resp = new StatsCardResp();
		try {
			long startTime = req.getStartTime();
			long endTime = req.getEndTime();
			List<Object> idList = req.getIdList();
			int type = req.getType();//0�����շ�Ա���ͳ�� 1�����������ͳ�� 2������λ�α�Ų�ѯ 3������λ��ѯ
			if(startTime <= 0
					|| endTime <= 0
					|| idList == null
					|| idList.isEmpty()){
				resp.setResult(-1);
				resp.setErrmsg("��������");
				return resp;
			}
			String column = null;
			if(type == 0){
				column = "uid";//���շ�Ա���ͳ��
			}else if(type == 1){
				column = "comid";//���������ͳ��
			}else if(type == 2){
				column = "berthseg_id";//����λ�α��ͳ��
			}else if(type == 3){
				column = "berth_id";//����λ���ͳ��
			}else if(type == 4){
				column = "groupid";
			}
			if(column == null){
				resp.setResult(-1);
				resp.setErrmsg("��������");
				return resp;
			}
			String preParams = "";
			for(int i = 0; i<idList.size(); i++){
				if(i == 0){
					preParams ="?";
				}else{
					preParams += ",?";
				}
			}
			List<Object> params = new ArrayList<Object>();
			params.add(0);//״̬����
			params.add(startTime);
			params.add(endTime);
			params.addAll(idList);
			String sql = "select sum(amount) summoney,count(id) ccount,charge_type,consume_type,type," + column +
					" from card_account_tb where is_delete=? and create_time between ? and ? and " + column +
					" in ("+preParams+") group by " + column + ",charge_type,consume_type,type ";
			List<Map<String, Object>> list = readService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				List<Object> existIds = new ArrayList<Object>();//�б��Ѵ��ڵ�����
				List<StatsCard> cards = new ArrayList<StatsCard>();
				for(Map<String, Object> map : list){
					Long id = (Long)map.get(column);
					Double summoney = Double.valueOf(map.get("summoney") + "");
					Long count = (Long)map.get("ccount");
					Integer charge_type = (Integer)map.get("charge_type");//��ֵ��ʽ��0���ֽ��ֵ 1��΢�Ź��ںų�ֵ 2��΢�ſͻ��˳�ֵ 3��֧������ֵ 4��Ԥ֧���˿� 5�������˿�
					Integer consume_type = (Integer)map.get("consume_type");//���ѷ�ʽ 0��֧��ͣ���ѣ���Ԥ���� 1��Ԥ��ͣ���� 2������ͣ����  3��׷��ͣ����
					Integer cardType = (Integer)map.get("type");//����Ƭ���������ڣ�0����ֵ 1������ 2����������Ƭ��ʼ������ʱ�Ŀ�Ƭ������ʹ�ã� 3�����Ƭ����ʱ��Ƭ����ʹ�ã� 4�����û� 5��ע����Ƭ
					
					StatsCard card = null;
					if(existIds.contains(id)){
						for(StatsCard statsCard : cards){
							long statsId = statsCard.getId();
							if(id.intValue() == statsId){//����ƥ�������
								card = statsCard;
								break;
							}
						}
					}else{
						existIds.add(id);
						card = new StatsCard();
						card.setId(id);
						cards.add(card);//�����
					}
					switch (cardType) {
					case 0://��ֵ
						if(charge_type == 0){//�ֽ��ֵ
							card.setChargeCashFee(summoney);
						}else if(charge_type == 4){//Ԥ���˿�
							card.setRefundFee(summoney);
						}
						break;
					case 1://����
						if(consume_type == 0){//֧��ͣ���ѣ���Ԥ����
							card.setParkingFee(summoney);
						}else if(consume_type == 1){//Ԥ��ͣ����
							card.setPrepayFee(summoney);
						}else if(consume_type == 2){//����ͣ����
							card.setAddFee(summoney);
						}else if(consume_type == 3){//׷��ͣ����
							card.setPursueFee(summoney);
						}
						break;
					case 2://����
						card.setRegFee(summoney);
						card.setRegCount(count);
						break;
					case 3://���Ƭ
						card.setActFee(summoney);
						card.setActCount(count);
						break;
					case 4://��Ƭ���û�
						card.setBindCount(count);
						break;
					case 5://ע����Ƭ
						card.setReturnFee(summoney);
						card.setReturnCount(count);
						break;
					default:
						break;
					}
				}
				resp.setCards(cards);
				return resp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("ϵͳ����");
		return resp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public AccountResp account(AccountReq req) {
		//logger.error(req.toString());
		AccountResp resp = new AccountResp();
		try {
			ExecutorService pool = ExecutorsUtil.getExecutorService();//��ȡ�̳߳�
			long startTime = req.getStartTime();
			long endTime = req.getEndTime();
			long id = req.getId();
			int pageNum = req.getPageNum();
			int pageSize = req.getPageSize();
			int type = req.getType();//0�����շ�Ա���ͳ�� 1�����������ͳ�� 2������λ�α�Ų�ѯ 3������λ��ѯ
			SqlInfo sqlInfo = req.getSqlInfo();
			if(startTime <= 0
					|| endTime <= 0
					|| id <= 0){
				resp.setResult(-1);
				resp.setErrmsg("��������");
				return resp;
			}
			String column = null;
			if(type == 0){
				column = "uid";//���շ�Ա���ͳ��
			}else if(type == 1){
				column = "comid";//���������ͳ��
			}else if(type == 2){
				column = "berthseg_id";//����λ�α��ͳ��
			}else if(type == 3){
				column = "berth_id";//����λ���ͳ��
			}else if(type == 4){
				column = "groupid";
			}
			if(column == null){
				resp.setResult(-1);
				resp.setErrmsg("��������");
				return resp;
			}
			ArrayList<Object> params = new ArrayList<Object>();
			params.add(0);//״̬����
			params.add(startTime);
			params.add(endTime);
			params.add(id);
			String sql = "select * from card_account_tb where is_delete=? and create_time between " +
					" ? and ? and "+column+"=? ";
			String countSql = "select count(id) from card_account_tb where is_delete=? and create_time " +
					" between ? and ? and "+column+"=? ";
			if(sqlInfo != null) {
				countSql += " and "+ sqlInfo.getSql();
				sql += " and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			sql += " order by create_time desc ";
			QueryCount queryCount = new QueryCount(readService, countSql, params);
			QueryList queryList = new QueryList(readService, sql, params, pageNum, pageSize);
			Future<Long> future0 = pool.submit(queryCount);
			Future<List> future1 = pool.submit(queryList);
			Long count = future0.get();
			List list = future1.get();
			resp.setCount(count);
			resp.setList(list);
			resp.setResult(1);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("ϵͳ����");
		return resp;
	}

}
