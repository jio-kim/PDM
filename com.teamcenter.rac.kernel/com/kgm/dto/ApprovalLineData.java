package com.kgm.dto;

import java.io.Serializable;

/** ECO의 결재선/ 배포선 정보 데이터 모델 */
@SuppressWarnings("serial")
public class ApprovalLineData implements Serializable  {
	/** ECO 번호 */
	private String eco_no;
	/** 순번 */
	private String sort;
	/** 타스크 이름 */
	private String task;
	/** 팀명 */
	private String team_name;
	/** 이름 */
	private String user_name;
	/** TCComponentGroupMember */
	private String tc_member_puid;
	/** 결재선 저장 이름 */
	private String saved_name;
	/** 결재선 저장 사용자 ID */
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
