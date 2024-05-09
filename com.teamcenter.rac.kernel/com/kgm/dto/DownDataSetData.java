package com.kgm.dto;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class DownDataSetData implements Serializable {
	
	private String item_id;
	private String login_user;
	private Date creation_date;
	private String dataset_uid;
	private String dataset_name;
	private String down_path;
	private Date down_date;
	
	public Date getDown_date() {
		return down_date;
	}
	public void setDown_date(Date downDate) {
		down_date = downDate;
	}
	public String getItem_id() {
		return item_id;
	}
	public void setItem_id(String itemId) {
		item_id = itemId;
	}
	public String getLogin_user() {
		return login_user;
	}
	public void setLogin_user(String loginUser) {
		login_user = loginUser;
	}
	public Date getCreation_date() {
		return creation_date;
	}
	public void setCreation_date(Date creation_date) {
		this.creation_date = creation_date;
	}
	public String getDataset_uid() {
		return dataset_uid;
	}
	public void setDataset_uid(String datasetUid) {
		dataset_uid = datasetUid;
	}
	public String getDataset_name() {
		return dataset_name;
	}
	public void setDataset_name(String datasetName) {
		dataset_name = datasetName;
	}
	public String getDown_path() {
		return down_path;
	}
	public void setDown_path(String downPath) {
		down_path = downPath;
	}
}