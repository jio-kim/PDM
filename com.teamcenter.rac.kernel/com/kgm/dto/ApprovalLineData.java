package com.kgm.dto;

import java.io.Serializable;

/** ECO�� ���缱/ ������ ���� ������ �� */
@SuppressWarnings("serial")
public class ApprovalLineData implements Serializable  {
	/** ECO ��ȣ */
	private String eco_no;
	/** ���� */
	private String sort;
	/** Ÿ��ũ �̸� */
	private String task;
	/** ���� */
	private String team_name;
	/** �̸� */
	private String user_name;
	/** TCComponentGroupMember */
	private String tc_member_puid;
	/** ���缱 ���� �̸� */
	private String saved_name;
	/** ���缱 ���� ����� ID */
	private String saved_user;
	
	public String getEco_no() {
		return eco_no;
	}
	public void setEco_no(String eco_no) {
		this.eco_no = eco_no;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public String getTeam_name() {
		return team_name;
	}
	public void setTeam_name(String team_name) {
		this.team_name = team_name;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getTc_member_puid() {
		return tc_member_puid;
	}
	public void setTc_member_puid(String tc_member_puid) {
		this.tc_member_puid = tc_member_puid;
	}
	public String getSaved_name() {
		return saved_name;
	}
	public void setSaved_name(String saved_name) {
		this.saved_name = saved_name;
	}
	public String getSaved_user() {
		return saved_user;
	}
	public void setSaved_user(String saved_user) {
		this.saved_user = saved_user;
	}
}
