package com.kgm.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class VnetReceivedData implements Serializable {
	private String item_puid;
	private String vnet_registered_id;
	private String if_count;
	private String if_stage;
	private String eci_no;
	private String eco_no;
	private String approval_no;
	private String visionnet_url;
	private String create_date;
	private String nplm_received;
	private String nplm_received_date;
	
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
	public String getIf_count() {
		return if_count;
	}
	public void setIf_count(String if_count) {
		this.if_count = if_count;
	}
	public String getIf_stage() {
		return if_stage;
	}
	public void setIf_stage(String if_stage) {
		this.if_stage = if_stage;
	}
	public String getEci_no() {
		return eci_no;
	}
	public void setEci_no(String eci_no) {
		this.eci_no = eci_no;
	}
	public String getEco_no() {
		return eco_no;
	}
	public void setEco_no(String eco_no) {
		this.eco_no = eco_no;
	}
	public String getApproval_no() {
		return approval_no;
	}
	public void setApproval_no(String approval_no) {
		this.approval_no = approval_no;
	}
	public String getVisionnet_url() {
		return visionnet_url;
	}
	public void setVisionnet_url(String visionnet_url) {
		this.visionnet_url = visionnet_url;
	}
	public String getCreate_date() {
		return create_date;
	}
	public void setCreate_date(String create_date) {
		this.create_date = create_date;
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
}
