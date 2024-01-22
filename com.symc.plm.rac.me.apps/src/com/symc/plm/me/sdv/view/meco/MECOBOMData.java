package com.symc.plm.me.sdv.view.meco;

import java.io.Serializable;

public class MECOBOMData implements Serializable, Cloneable {

    public MECOBOMData() {

    }

    private static final long serialVersionUID = 1L;


//    bl_occ_fnd0ObjectId", "bl_occ_int_order_no", "bl_item_object_type", "bl_rev_object_name", "bl_quantity", "bl_variant_condition", "bl_abs_occ_id", "bl_item_item_revision", "bl_rev_s7_DISPLAY_PART_NO"
    private String bl_item_item_id;
    private String bl_occ_fnd0ObjectId;
    private int bl_occ_int_order_no;
    private String bl_item_object_type;
    private String bl_rev_object_name;
    private String bl_quantity;
    private String bl_variant_condition;
    private String bl_abs_occ_id;
    private String bl_rev_item_revision_id;
    private String bl_rev_s7_DISPLAY_PART_NO;
    
    
    
	public String getBl_item_item_id() {
		return bl_item_item_id;
	}
	public void setBl_item_item_id(String bl_item_item_id) {
		this.bl_item_item_id = bl_item_item_id;
	}
	public String getBl_occ_fnd0ObjectId() {
		return bl_occ_fnd0ObjectId;
	}
	public void setBl_occ_fnd0ObjectId(String bl_occ_fnd0ObjectId) {
		this.bl_occ_fnd0ObjectId = bl_occ_fnd0ObjectId;
	}
	public int getBl_occ_int_order_no() {
		return bl_occ_int_order_no;
	}
	public void setBl_occ_int_order_no(int bl_occ_int_order_no) {
		this.bl_occ_int_order_no = bl_occ_int_order_no;
	}
	public String getBl_item_object_type() {
		return bl_item_object_type;
	}
	public void setBl_item_object_type(String bl_item_object_type) {
		this.bl_item_object_type = bl_item_object_type;
	}
	public String getBl_rev_object_name() {
		return bl_rev_object_name;
	}
	public void setBl_rev_object_name(String bl_rev_object_name) {
		this.bl_rev_object_name = bl_rev_object_name;
	}
	public String getBl_quantity() {
		return bl_quantity;
	}
	public void setBl_quantity(String bl_quantity) {
		this.bl_quantity = bl_quantity;
	}
	public String getBl_variant_condition() {
		return bl_variant_condition;
	}
	public void setBl_variant_condition(String bl_variant_condition) {
		this.bl_variant_condition = bl_variant_condition;
	}
	public String getBl_rev_item_revision_id() {
		return bl_rev_item_revision_id;
	}
	public void setBl_rev_item_revision_id(String bl_rev_item_revision_id) {
		this.bl_rev_item_revision_id = bl_rev_item_revision_id;
	}
	public String getBl_abs_occ_id() {
		return bl_abs_occ_id;
	}
	public void setBl_abs_occ_id(String bl_abs_occ_id) {
		this.bl_abs_occ_id = bl_abs_occ_id;
	}
	
//	public String getBl_rev_item_Revision() {
//		return bl_rev_item_Revision;
//	}
//	public void setBl_rev_item_Revision(String bl_rev_item_Revision) {
//		this.bl_rev_item_Revision = bl_rev_item_Revision;
//	}
	public String getBl_rev_s7_DISPLAY_PART_NO() {
		return bl_rev_s7_DISPLAY_PART_NO;
	}
	public void setBl_rev_s7_DISPLAY_PART_NO(String bl_rev_s7_DISPLAY_PART_NO) {
		this.bl_rev_s7_DISPLAY_PART_NO = bl_rev_s7_DISPLAY_PART_NO;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
    
    

   



    /*
     * SYMCBOMEditData(TCComponentBOMLine bomLine, int editType) { this.occUid =
     * bomLine.getUid(); this.editType = editType;
     * loadParentProperties(bomLine); if(editType == BOM_ADD) {
     * loadNewBOMProperties(bomLine); } else if(editType == BOM_CUT) {
     * loadOldBOMProperties(bomLine); } else if(editType == BOM_PROPERTY_CHANGE)
     * { loadNewBOMProperties(bomLine); copyNewToOld(); } }
     * 
     * void loadParentProperties(TCComponentBOMLine bomLine) { try { parentNo =
     * bomLine.parent().getProperty("bl_item_item_id"); parentRev =
     * bomLine.parent().getProperty("bl_rev_item_revision_id"); } catch
     * (TCException e) { e.printStackTrace(); } }
     * 
     * void loadNewBOMProperties(TCComponentBOMLine bomLine) { try { partNoNew =
     * bomLine.getProperty("bl_item_item_id"); partRevNew =
     * bomLine.getProperty("bl_rev_item_revision_id"); seqNew =
     * bomLine.getProperty("bl_sequence_no"); supplyModeNew =
     * bomLine.getProperty("S7_SUPPLY_MODE"); altNew =
     * bomLine.getProperty("S7_ALTER_PART"); moduleCodeNew =
     * bomLine.getProperty("S7_MODULE_CODE"); vcNew =
     * bomLine.getProperty("bl_variant_condition"); } catch (TCException e) {
     * e.printStackTrace(); } }
     * 
     * void loadOldBOMProperties(TCComponentBOMLine bomLine) { try { partNoOld =
     * bomLine.getProperty("bl_item_item_id"); partRevOld =
     * bomLine.getProperty("bl_rev_item_revision_id"); seqOld =
     * bomLine.getProperty("bl_sequence_no"); supplyModeOld =
     * bomLine.getProperty("S7_SUPPLY_MODE"); altOld =
     * bomLine.getProperty("S7_ALTER_PART"); moduleCodeOld =
     * bomLine.getProperty("S7_MODULE_CODE"); vcOld =
     * bomLine.getProperty("bl_variant_condition"); } catch (TCException e) {
     * e.printStackTrace(); } }
     */


    



}
