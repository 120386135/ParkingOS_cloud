package com.zld;

/**
 * ���ֶ�
 * @author Gecko
 *
 */
public class TableField {


	private String name;
	private int fieldType;
	private int fieldLength;
	private boolean isInsertNull;
	private boolean isUpdateNull;
	
	
	public String getName() {
		return name;
	}
	public int getFieldType() {
		return fieldType;
	}
	public int getFieldLength() {
		return fieldLength;
	}
	public boolean isInsertNull() {
		return isInsertNull;
	}
	public boolean isUpdateNull() {
		return isUpdateNull;
	}
	
	public void setUpdateNull(boolean isUpdateNull) {
		this.isUpdateNull = isUpdateNull;
	}
	
	/**
	 * 
	 * @param name �ֶ�����
	 * @param fieldType �ֶ�����  ��2ö�� 3���㣬4������5������ ��12�ַ���
	 * @param fieldLength �ֶ��ֽڳ��ȣ�����ʱ���޳��ȣ�����0������Ϊ����С���������ַ���Ϊʵ���ַ����ȣ�һ������Ϊ�����ֽ�
	 * @param isInsertNull ע��ʱ�ɷ�Ϊ��
	 * @param isUpdateNull ����ʱ�ɷ�Ϊ��
	 */
	public TableField(String name, int fieldType, int fieldLength,
			boolean isInsertNull, boolean isUpdateNull) {
		super();
		this.name = name;
		this.fieldType = fieldType;
		this.fieldLength = fieldLength;
		this.isInsertNull = isInsertNull;
		this.isUpdateNull = isUpdateNull;
	}
	
}
