package com.zld.tcp.client;
public class ResponseEntity {

	//��Ӧ״̬��
	private String statusCode;
	//��Ӧ��Ϣ
	private String message;
	//��Ӧ��Ϣ���壬������ʽ
	private String jsonResult;

	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}

	public String getJsonResult() {
		return jsonResult;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setJsonResult(String jsonResult) {
		this.jsonResult = jsonResult;
	}
}