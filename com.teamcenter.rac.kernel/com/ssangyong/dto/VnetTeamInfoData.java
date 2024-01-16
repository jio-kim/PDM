package com.ssangyong.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class VnetTeamInfoData implements Serializable  {
	/** 부서 명 */
	private String team_name;
	/** 부서 코드 */
	private String team_code;
	private String ord1;
	private String ord2;
	private String ord3;
	private String[] codeList;
	
	public String getTeam_name() {
		return team_name;
	}
	public void setTeam_name(String team_name) {
		this.team_name = team_name;
	}
	public String getTeam_code() {
		return team_code;
	}
	public void setTeam_code(String team_code) {
		this.team_code = team_code;
	}
	public String getOrd1() {
		return ord1;
	}
	public void setOrd1(String ord1) {
		this.ord1 = ord1;
	}
	public String getOrd2() {
		return ord2;
	}
	public void setOrd2(String ord2) {
		this.ord2 = ord2;
	}
	public String getOrd3() {
		return ord3;
	}
	public void setOrd3(String ord3) {
		this.ord3 = ord3;
	}
	public String[] getCodeList() {
		return codeList;
	}
	public void setCodeList(String[] codeList) {
		this.codeList = codeList;
	}
}
