package com.ssangyong.commands.ec.eco.module;

/**
 * 모듈BOM 검증 결과를 객체화.
 * [SR140722-022][20140704] swyoon 모듈BOM 검증 결과를 객체화.
 */
public class ModuleBomValidationInfo {

	private String eplId = null;
	private String msgType = null;
	private String msg = null;
	
	public ModuleBomValidationInfo(){
		
	}
	
	public ModuleBomValidationInfo(String eplId, String msgType, String msg){
		this.eplId = eplId;
		this.msgType = msgType;
		this.msg = msg;
	}
	
	public String getEplId() {
		return eplId;
	}
	public void setEplId(String eplId) {
		this.eplId = eplId;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
