package com.kgm.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SYMCECOSheetCData implements Serializable  {
	private String eco_no;
	private String occ_threads;
	private String ct;
	private String parent_no;
	private String parent_rev;
	private String old_part_no;
	private String old_part_rev;
	private String new_part_no;
	private String new_part_rev;
	private String old_seq;
	private String new_seq;
	private String old_qty;
	private String new_qty;
	private String old_smode;
	private String new_smode;
	private String old_apart;
	private String new_apart;
	private String old_mcode;
	private String new_mcode;
	private byte[] old_vc;
	private byte[] new_vc;
	private String old_ic;
	private String new_ic;
	private String old_plt_stk;
	private String old_as_stk;
	private String new_cost;
	private String new_tool;
	private String new_desc;
	private String old_desc;
	private String upd_user_id;
	private String upd_date;
	public String getEco_no() {
		return eco_no;
	}
	public void setEco_no(String eco_no) {
		this.eco_no = eco_no;
	}
	public String getOcc_threads() {
		return occ_threads;
	}
	public void setOcc_threads(String occ_threads) {
		this.occ_threads = occ_threads;
	}
	public String getCt() {
		return ct;
	}
	public void setCt(String ct) {
		this.ct = ct;
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
	public String getOld_part_no() {
		return old_part_no;
	}
	public void setOld_part_no(String old_part_no) {
		this.old_part_no = old_part_no;
	}
	public String getOld_part_rev() {
		return old_part_rev;
	}
	public void setOld_part_rev(String old_part_rev) {
		this.old_part_rev = old_part_rev;
	}
	public String getNew_part_no() {
		return new_part_no;
	}
	public void setNew_part_no(String new_part_no) {
		this.new_part_no = new_part_no;
	}
	public String getNew_part_rev() {
		return new_part_rev;
	}
	public void setNew_part_rev(String new_part_rev) {
		this.new_part_rev = new_part_rev;
	}
	public String getOld_seq() {
		return old_seq;
	}
	public void setOld_seq(String old_seq) {
		this.old_seq = old_seq;
	}
	public String getNew_seq() {
		return new_seq;
	}
	public void setNew_seq(String new_seq) {
		this.new_seq = new_seq;
	}
	public String getOld_qty() {
		return old_qty;
	}
	public void setOld_qty(String old_qty) {
		this.old_qty = old_qty;
	}
	public String getNew_qty() {
		return new_qty;
	}
	public void setNew_qty(String new_qty) {
		this.new_qty = new_qty;
	}
	public String getOld_smode() {
		return old_smode;
	}
	public void setOld_smode(String old_smode) {
		this.old_smode = old_smode;
	}
	public String getNew_smode() {
		return new_smode;
	}
	public void setNew_smode(String new_smode) {
		this.new_smode = new_smode;
	}
	public String getOld_apart() {
		return old_apart;
	}
	public void setOld_apart(String old_apart) {
		this.old_apart = old_apart;
	}
	public String getNew_apart() {
		return new_apart;
	}
	public void setNew_apart(String new_apart) {
		this.new_apart = new_apart;
	}
	public String getOld_mcode() {
		return old_mcode;
	}
	public void setOld_mcode(String old_mcode) {
		this.old_mcode = old_mcode;
	}
	public String getNew_mcode() {
		return new_mcode;
	}
	public void setNew_mcode(String new_mcode) {
		this.new_mcode = new_mcode;
	}
	public String getOld_vc() {
		return new String(old_vc,0,old_vc.length);
	}
	public void setOld_vc(byte[] old_vc) {
		this.old_vc = old_vc;
	}
	public String getNew_vc() {
		return new String(new_vc,0,new_vc.length);
	}
	public void setNew_vc(byte[] new_vc) {
		this.new_vc = new_vc;
	}
	public String getOld_ic() {
		return old_ic;
	}
	public void setOld_ic(String old_ic) {
		this.old_ic = old_ic;
	}
	public String getNew_ic() {
		return new_ic;
	}
	public void setNew_ic(String new_ic) {
		this.new_ic = new_ic;
	}
	public String getOld_plt_stk() {
		return old_plt_stk;
	}
	public void setOld_plt_stk(String old_plt_stk) {
		this.old_plt_stk = old_plt_stk;
	}
	public String getOld_as_stk() {
		return old_as_stk;
	}
	public void setOld_as_stk(String old_as_stk) {
		this.old_as_stk = old_as_stk;
	}
	public String getNew_cost() {
		return new_cost;
	}
	public void setNew_cost(String new_cost) {
		this.new_cost = new_cost;
	}
	public String getNew_tool() {
		return new_tool;
	}
	public void setNew_tool(String new_tool) {
		this.new_tool = new_tool;
	}
	public String getNew_desc() {
		return new_desc;
	}
	public void setNew_desc(String new_desc) {
		this.new_desc = new_desc;
	}
	public String getOld_desc() {
		return old_desc;
	}
	public void setOld_desc(String old_desc) {
		this.old_desc = old_desc;
	}
	public String getUpd_user_id() {
		return upd_user_id;
	}
	public void setUpd_user_id(String upd_user_id) {
		this.upd_user_id = upd_user_id;
	}
	public String getUpd_date() {
		return upd_date;
	}
	public void setUpd_date(String upd_date) {
		this.upd_date = upd_date;
	}
}