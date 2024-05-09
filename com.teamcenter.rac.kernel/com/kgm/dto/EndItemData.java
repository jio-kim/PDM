package com.kgm.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class EndItemData implements Serializable {

	/** 공법 Item id */
	private String pitem_id;
	
	/** 공법 Item revision id */
	private String pitem_revision_id;

	/** BOP Occ Puid */	
	private String occ_puid;
	
	/** BOP Occ Thread Puid */	
	private String occ_threadu;

	/** End Item id */
	private String citem_id;
	
	/** End Item revision id */
	private String citem_revision_id;
	
	/** End Item object type */
	private String cobject_type;

	/** EBOM Item id */
	private String ebom_item_id;

	public String getPitem_id() {
		return pitem_id;
	}

	public void setPitem_id(String pitem_id) {
		this.pitem_id = pitem_id;
	}

	public String getPitem_revision_id() {
		return pitem_revision_id;
	}

	public void setPitem_revision_id(String pitem_revision_id) {
		this.pitem_revision_id = pitem_revision_id;
	}

	public String getOcc_puid() {
		return occ_puid;
	}

	public void setOcc_puid(String occ_puid) {
		this.occ_puid = occ_puid;
	}
	
	public String getOcc_threadu() {
		return occ_threadu;
	}

	public void setOcc_threadu(String occ_threadu) {
		this.occ_threadu = occ_threadu;
	}

	public String getCitem_id() {
		return citem_id;
	}

	public void setCitem_id(String citem_id) {
		this.citem_id = citem_id;
	}

	public String getCitem_revision_id() {
		return citem_revision_id;
	}

	public void setCitem_revision_id(String citem_revision_id) {
		this.citem_revision_id = citem_revision_id;
	}

	public String getCobject_type() {
		return cobject_type;
	}

	public void setCobject_type(String cobject_type) {
		this.cobject_type = cobject_type;
	}

	public String getEbom_item_id() {
		return ebom_item_id;
	}

	public void setEbom_item_id(String ebom_item_id) {
		this.ebom_item_id = ebom_item_id;
	}

}
