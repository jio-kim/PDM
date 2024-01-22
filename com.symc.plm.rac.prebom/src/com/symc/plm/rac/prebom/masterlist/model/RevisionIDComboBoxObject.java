package com.symc.plm.rac.prebom.masterlist.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * Load in TC, Compare MLM에서 Item Revision ID 선택 Combobox에 들어갈 Object
 * @author jclee
 *
 */
public class RevisionIDComboBoxObject {
	private String sItemRevisionID = "";
	private Date dReleased;
	
	/**
	 * Constructor
	 * @param irFMP
	 */
	public RevisionIDComboBoxObject(TCComponentItemRevision irFMP) {
		try {
			this.sItemRevisionID = irFMP.getProperty("item_revision_id");
			this.dReleased = irFMP.getDateProperty("date_released");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Item Revision ID
	 * @return
	 */
	public String getItemRevisionID() {
		return sItemRevisionID;
	}
	
	/**
	 * Released Date
	 * @return
	 */
	public Date getReleasedDate() {
		return dReleased;
	}
	
	/**
	 * Display String
	 *  [Item Reivsion ID]_[Released Date : yyyy.MM.dd or Not Released]
	 */
	@Override
	public String toString() {
		String sDateReleased = "";
		if (dReleased == null) {
			sDateReleased = "Not Released";
		} else {
			SimpleDateFormat sdf  = new SimpleDateFormat("yyyy.MM.dd");
			sDateReleased = sdf.format(dReleased);
		}
		
		return sItemRevisionID + "_" + sDateReleased;
	}

}
