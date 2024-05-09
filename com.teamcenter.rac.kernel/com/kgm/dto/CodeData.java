package com.kgm.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CodeData implements Serializable  {
	/** 리스트 아이디 */
	private String list_id;
	/** 리스트 네임 */
	private String list_name;
	/** 코드 아이디 */
	private String code_id;
	/** 코드 이름 */
	private String code_name;
	/** 정렬 순번 */
	private String sort_order;
	
	public String getList_id() {
		return list_id;
	}
	public void setList_id(String list_id) {
		this.list_id = list_id;
	}
	public String getList_name() {
		return list_name;
	}
	public void setList_name(String list_name) {
		this.list_name = list_name;
	}
	public String getCode_id() {
		return code_id;
	}
	public void setCode_id(String code_id) {
		this.code_id = code_id;
	}
	public String getCode_name() {
		return code_name;
	}
	public void setCode_name(String code_name) {
		this.code_name = code_name;
	}
	public String getSort_order() {
		return sort_order;
	}
	public void setSort_order(String sort_order) {
		this.sort_order = sort_order;
	}

}
