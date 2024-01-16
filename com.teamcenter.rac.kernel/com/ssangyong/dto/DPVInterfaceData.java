package com.ssangyong.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DPVInterfaceData implements Serializable  {
	private String if_type;
	private String part_no;
	private String part_rev_puid;
	private String dpv_no;
	private String if_status;
	
	public String getIf_type() {
		return if_type;
	}
	public void setIf_type(String if_type) {
		this.if_type = if_type;
	}
	public String getPart_no() {
		return part_no;
	}
	public void setPart_no(String part_no) {
		this.part_no = part_no;
	}
	public String getPart_rev_puid() {
		return part_rev_puid;
	}
	public void setPart_rev_puid(String part_rev_puid) {
		this.part_rev_puid = part_rev_puid;
	}
	public String getDpv_no() {
		return dpv_no;
	}
	public void setDpv_no(String dpv_no) {
		this.dpv_no = dpv_no;
	}
	public String getIf_status() {
		return if_status;
	}
	public void setIf_status(String if_status) {
		this.if_status = if_status;
	}
	
}
