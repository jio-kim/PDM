package com.kgm.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SYMCECOSheetBData implements Serializable  {
	private String eco_no;
	private String bom_occ_thread;
	private String occ_thread;
	private String parent_no;
	private String parent_rev;
	private String part_no;
	private String part_rev;
	private String ic;
	private String plt_stk;
	private String as_stk;
	private String cost;
	private String tool;
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
	public String getOcc_thread() {
		return occ_thread;
	}
	public void setOcc_thread(String occ_thread) {
		this.occ_thread = occ_thread;
	}
	public String getParent_no() {
		return parent_no;
	}
	public void setParent_no(String parent_no) {
		this.parent_no = parent_no;
	}
	public String getParent_rev() {
		return parent_rev;
	}
	public void setParent_rev(String parent_rev) {
		this.parent_rev = parent_rev;
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
	public String getIc() {
		return ic;
	}
	public void setIc(String ic) {
		this.ic = ic;
	}
	public String getPlt_stk() {
		return plt_stk;
	}
	public void setPlt_stk(String plt_stk) {
		this.plt_stk = plt_stk;
	}
	public String getAs_stk() {
		return as_stk;
	}
	public void setAs_stk(String as_stk) {
		this.as_stk = as_stk;
	}
	public String getCost() {
		return cost;
	}
	public void setCost(String cost) {
		this.cost = cost;
	}
	public String getTool() {
		return tool;
	}
	public void setTool(String tool) {
		this.tool = tool;
	}
	public String getChg_desc() {
		return chg_desc;
	}
	public void setChg_desc(String chg_desc) {
		this.chg_desc = chg_desc;
	}
	
}
