package com.zld.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import jxl.CellView;
import jxl.Workbook;
import jxl.biff.DisplayFormat;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.Label;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WritableFont; 
import jxl.format.UnderlineStyle;
import jxl.format.Colour; 

import org.apache.log4j.Logger;

public class ExportExcelUtil {

static Logger logger = Logger.getLogger(ExportExcelUtil.class);
	
	public  String excelName="����";
	public  String[] headBody = null;
	public  List<List<String>> bodyList = null;
	public  List<Map<String,String>> mulitHeadList =null;
	public Map<String,String> headInfo=null;
	/**
	 * 
	 * @param excelName �ļ���
	 * @param headBody ��ͷ
	 * @param bodyList ����
	 * @param isEncrypt �绰�Ƿ����
	 */
	public ExportExcelUtil(String excelName,String[] headBody,List<List<String>> bodyList){
		this.excelName=excelName;
		this.headBody = headBody;
		this.bodyList = bodyList;
	}
	public void createExcelFile(OutputStream os) throws IOException {
	    try {
            //����һ���ļ�
			WritableWorkbook workbook = Workbook.createWorkbook(os);
	        //ʹ�õ�һ�Ź�����
	        WritableSheet sheet = workbook.createSheet(excelName, 0); 
	        CellView cellView = new CellView();  
	        //cellView.setAutosize(true); //�����Զ���С
	        cellView.setSize(4050);
	       
	        //�������ͷ
	        /*
	         *  ͨ��writablesheet.mergeCells(int x,int y,int m,int n);��ʵ�ֵġ�
 				��ʾ���ӵ�x+1�У�y+1�е�m+1�У�n+1�кϲ� (�ĸ��㶨�����������꣬���ϽǺ����½�)
 				����Ǻϲ���m-x+1�У�n-y+1�У����߳˻����Ǻϲ��ĵ�Ԫ��������
	         */
	        Integer start =0;
	        if(headInfo!=null&&!headInfo.isEmpty()){
	        	start++;
	        	Integer length  = Integer.valueOf(headInfo.get("length"));
        		sheet.mergeCells(0, 0, length, 0);
        	    WritableFont font = new WritableFont(WritableFont.ARIAL,14,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);  
        		  
        		WritableCellFormat wc = new WritableCellFormat(font); 
        	        // ���þ��� 
        	    wc.setAlignment(Alignment.CENTRE); 
        	        // ���ñ߿��� 
        	    wc.setBorder(Border.ALL, BorderLineStyle.THIN); 
        	    
        	        // ���õ�Ԫ��ı�����ɫ 
        	  //  wc.setBackground(jxl.format.Colour.YELLOW); 
        		Label cell= new Label(0, 0,headInfo.get("content"),wc);
        		
        		sheet.addCell(cell);
	        }
	        if(mulitHeadList!=null){
	        	Integer preKey =0;
	        	for(Map<String,String> map :mulitHeadList){
	        		Integer length  = Integer.valueOf(map.get("length"));
	        		sheet.mergeCells(preKey, start, preKey+length,start);
	        		WritableCellFormat wc = new WritableCellFormat(); 
	        	        // ���þ��� 
	        	    wc.setAlignment(Alignment.CENTRE); 
	        	        // ���ñ߿��� 
	        	    wc.setBorder(Border.ALL, BorderLineStyle.THIN); 
	        	        // ���õ�Ԫ��ı�����ɫ 
	        	    wc.setBackground(jxl.format.Colour.YELLOW); 
	        		Label cell= new Label(preKey,start,map.get("content"),wc);
	        		
	        		sheet.addCell(cell);
	        		preKey += length+1;
	        	}
	        	start++;
	        }
	        //������ͷ
	        for (int i=0;i<headBody.length;i++) {
	        	//���������ֱ��ʾcol+1�У�row+1�У�����������title��
	        	WritableCellFormat wc = new WritableCellFormat(); 
    	        // ���þ��� 
	        	wc.setAlignment(Alignment.CENTRE); 
    	        // ���ñ߿��� 
	        	wc.setBorder(Border.ALL, BorderLineStyle.THIN);
	        	wc.setBackground(jxl.format.Colour.GRAY_25); 
	        	wc.isShrinkToFit();
	        	Label cell= new Label(i, start, headBody[i],wc);
	        	//cellView.setSize(headBody[i].length()*600);
	        	sheet.setColumnView(i, cellView);//���������Զ������п�  
		        sheet.addCell(cell);
			}
	        //��������
	        if(bodyList != null) {     
	        	logger.info("��ʼ�����ļ�");
	        	//WritableFont wf = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD, false);
	        	//DisplayFormat displayFormat = NumberFormats.TEXT;
	        	//WritableCellFormat format = new WritableCellFormat(wf,displayFormat);
	        	for(int i = 0,j=start+1; i < bodyList.size(); i++,j++) {
	        		//��ȡд������
	        		List<String > dateList=bodyList.get(i);	              				        	
	        		//д������
	        		for(int k=0 ;k<dateList.size();k++){
	        			String value = dateList.get(k);//�������ͻ��绰���������ַ�������
	        			value = (value==null||value.equals("null"))?"":value;
//	        			if(Check.isNumber(value)||Check.isDouble(value)){
//	        				jxl.write.Number number = new jxl.write.Number(k,j, Double.parseDouble(value),format);
//	        				sheet.addCell(number);
//	        			}else {
	        				Label label = new Label(k,j,value);
	        				sheet.addCell(label);
//						}
	        		}
	        	}
	        }
	        logger.info("�����ļ�����");
	        //�رն����ͷ���Դ
	        workbook.write();
	        workbook.close();
	        os.close();
		} catch (Exception e) {
			os.close();
			logger.error(e);
		}
	}
}
