package com.kgm.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class VnetTeamReviewData implements Serializable  {
	private String item_puid;
	private String vnet_registered_id;
	private String tteam;
	private String rev_inve;
	private String rev_cost;
	private String descp1;
	private String descp2;
	private String append;
	private String status;
	private String r_approval;
	private String f_approval;
	private String nplm_received;
	private String nplm_received_date;
	private String create_date;
	public String getItem_puid() {
		return item_puid;
	}
	public void setItem_puid(String item_puid) {
		this.item_puid = item_puid;
	}
	public String getVnet_registered_id() {
		return vnet_registered_id;
	}
	public void setVnet_registered_id(String vnet_registered_id) {
		this.vnet_registered_id = vnet_registered_id;
	}
	public String getTteam() {
		return tteam;
	}
	public void setTteam(String tteam) {
		this.tteam = tteam;
	}
	public String getRev_inve() {
		return rev_inve;
	}
	public void setRev_inve(String rev_inve) {
		this.rev_inve = rev_inve;
	}
	public String getRev_cost() {
		return rev_cost;
	}
	public void setRev_cost(String rev_cost) {
		this.rev_cost = rev_cost;
	}
	public String getDescp1() {
		return descp1;
	}
	public void setDescp1(String descp1) {
		this.descp1 = descp1;
	}
	public String getDescp2() {
		return descp2;
	}
	public void setDescp2(String descp2) {
		this.descp2 = descp2;
	}
	public String getAppend() {
		return append;
	}
	public void setAppend(String append) {
		this.append = append;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getR_approval() {
		return r_approval;
	}
	public void setR_approval(String r_approval) {
		this.r_approval = r_approval;
	}
	public String getF_approval() {
		return f_approval;
	}
	public void setF_approval(String f_approval) {
		this.f_approval = f_approval;
	}
	public String getNplm_received() {
		return nplm_received;
	}
	public void setNplm_received(String nplm_received) {
		this.nplm_received = nplm_received;
	}
	public String getNplm_received_date() {
		return nplm_received_date;
	}
	public void setNplm_received_date(String nplm_received_date) {
		this.nplm_received_date = nplm_received_date;
	}
	public String getCreate_date() {
		return create_date;
	}
	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}

}
