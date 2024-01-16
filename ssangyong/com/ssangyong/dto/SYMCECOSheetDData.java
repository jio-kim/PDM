package com.ssangyong.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SYMCECOSheetDData implements Serializable  {
	private String eco_no;
	private String bom_occ_thread;
	private String part_no;
	private String part_rev;
	private String chg_desc;
	public String getEco_no() {
		return eco_no;
	}
	public void setEco_no(String eco_no) {
		this.eco_no = eco_no;
	}
	public String getBom_occ_thread() {
		return bom_occ_thread;
	}
	public void setBom_occ_thread(String bom_occ_thread) {
		this.bom_occ_thread = bom_occ_thread;
	}
	public String getPart_no() {
		return part_no;
	}
	public void setPart_no(String part_no) {
		this.part_no = part_no;
	}
	public String getPart_rev() {
		return part_rev;
	}
	public void setPart_rev(String part_rev) {
		this.part_rev = part_rev;
	}
	public String getChg_desc() {
		return chg_desc;
	}
	public void setChg_desc(String chg_desc) {
		this.chg_desc = chg_desc;
	}
}
