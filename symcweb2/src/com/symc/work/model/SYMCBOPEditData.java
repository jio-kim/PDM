package com.symc.work.model;

import java.io.Serializable;

/**
 * @author jungy
 *
 */
public class SYMCBOPEditData implements Serializable, Cloneable {

    public SYMCBOPEditData() {

    }

    private static final long serialVersionUID = 1L;

    public final static String BOM_ADD = "A";
    public final static String BOM_CUT = "D";
    public final static String BOM_REPLACE = "R";
    public final static String BOM_PROPERTY_CHANGE = "C";
    
    private String changeType;

    private String eplId;

    private String parentType;

    private String parentNo;

    private String parentRev;
    
    private String parentName;
    
    private String parentPuid;
    
    private String seq;
    
    private String old_child_puid;
    
    private String old_child_type;
    
    private String old_child_no;
    
    private String old_child_rev;
    
    private String old_child_name;
    
    private String old_shown_no_no;
    
    private String old_qty;
    
    private String old_vc;
    
    private String old_occ_uid;
    
    private String new_child_puid;
    
    private String new_child_type;
    
    private String new_child_no;
    
    private String new_child_rev;
    
    private String new_child_name;
    
    private String new_shown_no_no;
    
    private String new_qty;
    
    private String new_vc;
    
    private String new_occ_uid;
    
    private String mecoNo;
    
    private String shopNo;

    private String userId;
    
    private String ecoNo;
    
    private String parent_mod_date;
    



//    SYMCBOPEditData(String bomUid, String changeType) {
//        this.occUid = bomUid;
//        this.changeType = changeType;
//    }

    void copyOldToNew() {
    	
    	new_qty = old_qty;
    	new_vc = old_vc;
    	new_occ_uid = old_occ_uid;
    	
    }
    public boolean isReplace() {
        if(new_child_no != null && old_child_no != null) {
            return true;
        }
        return false;
    }

	public String getParent_mod_date() {
		return parent_mod_date;
	}

	public void setParent_mod_date(String parent_mod_date) {
		this.parent_mod_date = parent_mod_date;
	}

	public String getChangeType() {
		return changeType;
	}

	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}

	public String getEplId() {
		return eplId;
	}

	public void setEplId(String eplId) {
		this.eplId = eplId;
	}


	public String getParentType() {
		return parentType;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public String getParentNo() {
		return parentNo;
	}

	public void setParentNo(String parentNo) {
		this.parentNo = parentNo;
	}

	public String getParentRev() {
		return parentRev;
	}

	public void setParentRev(String parentRev) {
		this.parentRev = parentRev;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getParentPuid() {
		return parentPuid;
	}

	public void setParentPuid(String parentPuid) {
		this.parentPuid = parentPuid;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getOld_child_puid() {
		return old_child_puid;
	}

	public void setOld_child_puid(String old_child_puid) {
		this.old_child_puid = old_child_puid;
	}

	public String getOld_child_type() {
		return old_child_type;
	}

	public void setOld_child_type(String old_child_type) {
		this.old_child_type = old_child_type;
	}

	public String getOld_child_no() {
		return old_child_no;
	}

	public void setOld_child_no(String old_child_no) {
		this.old_child_no = old_child_no;
	}

	public String getOld_child_rev() {
		return old_child_rev;
	}

	public void setOld_child_rev(String old_child_rev) {
		this.old_child_rev = old_child_rev;
	}

	public String getOld_child_name() {
		return old_child_name;
	}

	public void setOld_child_name(String old_child_name) {
		this.old_child_name = old_child_name;
	}

	public String getOld_shown_no_no() {
		return old_shown_no_no;
	}

	public void setOld_shown_no_no(String old_shown_no_no) {
		this.old_shown_no_no = old_shown_no_no;
	}

	public String getOld_qty() {
		return old_qty;
	}

	public void setOld_qty(String old_qty) {
		this.old_qty = old_qty;
	}

	public String getOld_vc() {
		return old_vc;
	}

	public void setOld_vc(String old_vc) {
		this.old_vc = old_vc;
	}

	public String getNew_child_puid() {
		return new_child_puid;
	}

	public void setNew_child_puid(String new_child_puid) {
		this.new_child_puid = new_child_puid;
	}

	public String getNew_child_type() {
		return new_child_type;
	}

	public void setNew_child_type(String new_child_type) {
		this.new_child_type = new_child_type;
	}

	public String getNew_child_no() {
		return new_child_no;
	}

	public void setNew_child_no(String new_child_no) {
		this.new_child_no = new_child_no;
	}

	public String getNew_child_rev() {
		return new_child_rev;
	}

	public void setNew_child_rev(String new_child_rev) {
		this.new_child_rev = new_child_rev;
	}

	public String getNew_child_name() {
		return new_child_name;
	}

	public void setNew_child_name(String new_child_name) {
		this.new_child_name = new_child_name;
	}

	public String getNew_shown_no_no() {
		return new_shown_no_no;
	}

	public void setNew_shown_no_no(String new_shown_no_no) {
		this.new_shown_no_no = new_shown_no_no;
	}

	public String getNew_qty() {
		return new_qty;
	}

	public void setNew_qty(String new_qty) {
		this.new_qty = new_qty;
	}

	public String getNew_vc() {
		return new_vc;
	}

	public void setNew_vc(String new_vc) {
		this.new_vc = new_vc;
	}

	public String getMecoNo() {
		return mecoNo;
	}

	public void setMecoNo(String mecoNo) {
		this.mecoNo = mecoNo;
	}

	public String getShopNo() {
		return shopNo;
	}

	public void setShopNo(String shopNo) {
		this.shopNo = shopNo;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEcoNo() {
		return ecoNo;
	}

	public void setEcoNo(String ecoNo) {
		this.ecoNo = ecoNo;
	}

	public String getOld_occ_uid() {
		return old_occ_uid;
	}

	public void setOld_occ_uid(String old_occ_uid) {
		this.old_occ_uid = old_occ_uid;
	}

	public String getNew_occ_uid() {
		return new_occ_uid;
	}

	public void setNew_occ_uid(String new_occ_uid) {
		this.new_occ_uid = new_occ_uid;
	}




    
}
