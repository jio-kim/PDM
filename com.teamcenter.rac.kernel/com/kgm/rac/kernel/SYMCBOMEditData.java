package com.kgm.rac.kernel;

import java.io.Serializable;

/**
 * [20140417][SR140113-029] bskwak, Project code Old/New 분리 대응
 * @author bs
 *
 */
public class SYMCBOMEditData implements Serializable, Cloneable {

    public SYMCBOMEditData() {

    }

    private static final long serialVersionUID = 1L;

    /** BOM 편집 부품 추가 */
    public final static String BOM_ADD = "A";
    /** BOM 편집 부품 제거 */
    public final static String BOM_CUT = "D";
    /** BOM 편집 부품 교체 */
    public final static String BOM_REPLACE = "R";
    /** BOM 편집 속성 변경 */
    public final static String BOM_PROPERTY_CHANGE = "C";
    /** BOM 변경 유형(A, D, R, C) */
    private String changeType;

    /** EPL ID */
    private String eplId;
    /** BOM OCCURRENCE UID */
    private String occUid;
    /** Parent Item Type */
    private String parentType;
    /** Parent Part No */
    private String parentNo;
    /** Parent Part Revision No */
    private String parentRev;
//	[20140417][SR140113-029] bskwak,  Project code Old/New 분리 대응
//    /** Parent Project */
//    private String project;
    private String projectOld;
    private String projectNew;
    /** Child New BOM Sequence No */
    private String seqNew;
    /** Child Old BOM Sequence No */
    private String seqOld;
    /** Child New Part Origin */
    private String partOriginNew;
    /** Child New Part No */
    private String partNoNew;
    /** Child Old Part Origin */
    private String partOriginOld;
    /** Child Old Part No */
    private String partNoOld;
    /** Child New Part Revision No */
    private String partRevNew;
    /** Child Old Part Revision No */
    private String partRevOld;
    /** Child New Part Type */
    private String partTypeNew;
    /** Child Old Part Type */
    private String partTypeOld;
    /** Child New Part Name **/
    private String partNameNew;
    /** Child Old Part Name **/
    private String partNameOld;
    /** Child New BOM Supply Mode */
    private String supplyModeNew;
    /** Child Old BOM Supply Mode */
    private String supplyModeOld;
    /** Child New BOM Alter Part */
    private String altNew;
    /** Child Old BOM Alter Part */
    private String altOld;
    /** Child New BOM Module Code */
    private String moduleCodeNew;
    /** Child Old BOM Module Code */
    private String moduleCodeOld;
    /** Child New BOM Variant Condition */
    private String vcNew;
    /** Child Old BOM Variant Condition */
    private String vcOld;
    /** ECO No MYBATIS 에서 사용 */
    private String ecoNo;
    /** Edit User Id MYBATIS 에서 사용 */
    private String userId;

    private String ecoNoOld;

    /** Child New Qty */
    private String qtyNew;
    /** Child Old Qty */
    private String qtyOld;
    /** Child New IC */
    private String icNew;
    /** Child Old IC */
    private String icOld;
    /** Child Old PLT STK */
    private String pltStkOld;
    /** Child Old AS STK */
    private String asStkOld;
    /** Child New Cost */
    private String costNew;
    /** Child New Tool */
    private String toolNew;
    /** Child New Color Id */
    private String colorIdNew;
    /** Child Old Color Id */
    private String colorIdOld;
    /** Child New Color Section */
    private String colorSectionNew;
    /** Child Old Color Section */
    private String colorSectionOld;
    /** Child New Shown On **/
    private String shownOnNew;
    /** Child Old Shown On **/
    private String shownOnOld;
    /** Child New SEL **/
    private String selNew;
    /** Child Old SEL **/
    private String selOld;
    /** Child New CAT **/
    private String catNew;
    /** Child Old CAT **/
    private String catOld;
    /** Change Description */
    private String chgDesc;
    
    /**search epl only MFG*/
    private String releaseDate;
    

    public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	SYMCBOMEditData(String bomUid, String changeType) {
        this.occUid = bomUid;
        this.changeType = changeType;
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

    void copyOldToNew() {
        partNoNew = partNoOld;
        partRevNew = partRevOld;
        seqNew = seqOld;
        supplyModeNew = supplyModeOld;
        altNew = altOld;
        moduleCodeNew = moduleCodeOld;
        vcNew = vcOld;
    }

    void setNewToNull() {
        partNoNew = null;
        partRevNew = null;
        seqNew = null;
        supplyModeNew = null;
        altNew = null;
        moduleCodeNew = null;
        vcNew = null;
    }
    
    public boolean isReplace() {
        if(partNoNew != null && partNoOld != null) {
            return true;
        }
        return false;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getChangeType() {
        return changeType;
    }
    
    public void setParentType(String parentType) {
        this.parentType = parentType;
    }
    
    public String getParentType() {
        return parentType;
    }

    public void setParentNo(String parentNo) {
        this.parentNo = parentNo;
    }

    public String getParentNo() {
        return parentNo;
    }

    public String getParentRev() {
        return parentRev;
    }

    public void setParentRev(String parentRev) {
        this.parentRev = parentRev;
    }
    
    public String getPartOriginNew() {
        return partOriginNew;
    }

    public void setPartOriginNew(String partOriginNew) {
        this.partOriginNew = partOriginNew;
    }

    public String getPartNoNew() {
        return partNoNew;
    }

    public void setPartNoNew(String partNoNew) {
        this.partNoNew = partNoNew;
    }

    public String getPartOriginOld() {
        return partOriginOld;
    }

    public void setPartOriginOld(String partOriginOld) {
        this.partOriginOld = partOriginOld;
    }

    public String getPartNoOld() {
        return partNoOld;
    }

    public void setPartNoOld(String partNoOld) {
        this.partNoOld = partNoOld;
    }

    public String getPartRevNew() {
        return partRevNew;
    }

    public void setPartRevNew(String partRevNew) {
        this.partRevNew = partRevNew;
    }

    public String getPartRevOld() {
        return partRevOld;
    }

    public void setPartRevOld(String partRevOld) {
        this.partRevOld = partRevOld;
    }

    public String getPartTypeNew() {
        return partTypeNew;
    }

    public void setPartTypeNew(String partTypeNew) {
        this.partTypeNew = partTypeNew;
    }

    public String getPartTypeOld() {
        return partTypeOld;
    }

    public void setPartTypeOld(String partTypeOld) {
        this.partTypeOld = partTypeOld;
    }

    public String getSeqNew() {
        return seqNew;
    }

    public void setSeqNew(String seqNew) {
        this.seqNew = seqNew;
    }

    public String getSeqOld() {
        return seqOld;
    }

    public void setSeqOld(String seqOld) {
        this.seqOld = seqOld;
    }

    public String getSupplyModeNew() {
        return supplyModeNew;
    }

    public void setSupplyModeNew(String supplyModeNew) {
        this.supplyModeNew = supplyModeNew;
    }

    public String getSupplyModeOld() {
        return supplyModeOld;
    }

    public void setSupplyModeOld(String supplyModeOld) {
        this.supplyModeOld = supplyModeOld;
    }

    public String getAltNew() {
        return altNew;
    }

    public void setAltNew(String altNew) {
        this.altNew = altNew;
    }

    public String getAltOld() {
        return altOld;
    }

    public void setAltOld(String altOld) {
        this.altOld = altOld;
    }

    public String getVcNew() {
        return vcNew;
    }   

    public void setVcNew(String vcNew) {
        this.vcNew = vcNew;
    }

    public String getVcOld() {
        return vcOld;
    }    

    public void setVcOld(String vcOld) {
        this.vcOld = vcOld;
    }

    public String getModuleCodeNew() {
        return moduleCodeNew;
    }

    public void setModuleCodeNew(String moduleCodeNew) {
        this.moduleCodeNew = moduleCodeNew;
    }

    public String getModuleCodeOld() {
        return moduleCodeOld;
    }

    public void setModuleCodeOld(String moduleCodeOld) {
        this.moduleCodeOld = moduleCodeOld;
    }

    public String getEcoNo() {
        return ecoNo;
    }

    public void setEcoNo(String ecoNo) {
        this.ecoNo = ecoNo;
    }

    public String getEcoNoOld() {
        return ecoNoOld;
    }

    public void setEcoNoOld(String ecoNoOld) {
        this.ecoNoOld = ecoNoOld;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQtyNew() {
        return qtyNew;
    }

    public void setQtyNew(String qtyNew) {
        this.qtyNew = qtyNew;
    }

    public String getQtyOld() {
        return qtyOld;
    }

    public void setQtyOld(String qtyOld) {
        this.qtyOld = qtyOld;
    }

    public String getIcNew() {
        return icNew;
    }

    public void setIcNew(String icNew) {
        this.icNew = icNew;
    }

    public String getIcOld() {
        return icOld;
    }

    public void setIcOld(String icOld) {
        this.icOld = icOld;
    }

    public String getPltStkOld() {
        return pltStkOld;
    }

    public void setPltStkOld(String pltStkOld) {
        this.pltStkOld = pltStkOld;
    }

    public String getAsStkOld() {
        return asStkOld;
    }

    public void setAsStkOld(String asStkOld) {
        this.asStkOld = asStkOld;
    }

    public String getCostNew() {
        return costNew;
    }

    public void setCostNew(String costNew) {
        this.costNew = costNew;
    }

    public String getToolNew() {
        return toolNew;
    }

    public void setToolNew(String toolNew) {
        this.toolNew = toolNew;
    }

    public String getChgDesc() {
        return chgDesc;
    }

    public void setChgDesc(String chgDesc) {
        this.chgDesc = chgDesc;
    }

//	[20140417][SR140113-029] bskwak,  Project code Old/New 분리 대응
//    public String getProject() {
//        return project;
//    }
//
//    public void setProject(String project) {
//        this.project = project;
//    }


    public String getProjectOld() {
		return projectOld;
	}

	public void setProjectOld(String projectOld) {
		this.projectOld = projectOld;
	}

	public String getProjectNew() {
		return projectNew;
	}

	public void setProjectNew(String projectNew) {
		this.projectNew = projectNew;
	}

	public String getColorIdNew() {
        return colorIdNew;
    }

    public void setColorIdNew(String colorIdNew) {
        this.colorIdNew = colorIdNew;
    }

    public String getColorIdOld() {
        return colorIdOld;
    }

    public void setColorIdOld(String colorIdOld) {
        this.colorIdOld = colorIdOld;
    }

    public String getColorSectionNew() {
        return colorSectionNew;
    }

    public void setColorSectionNew(String colorSectionNew) {
        this.colorSectionNew = colorSectionNew;
    }

    public String getColorSectionOld() {
        return colorSectionOld;
    }

    public void setColorSectionOld(String colorSectionOld) {
        this.colorSectionOld = colorSectionOld;
    }

    public String getShownOnNew() {
        return shownOnNew;
    }

    public void setShownOnNew(String shownOnNew) {
        this.shownOnNew = shownOnNew;
    }

    public String getShownOnOld() {
        return shownOnOld;
    }

    public void setShownOnOld(String shownOnOld) {
        this.shownOnOld = shownOnOld;
    }

    public String getSelNew() {
        return selNew;
    }

    public void setSelNew(String selNew) {
        this.selNew = selNew;
    }

    public String getSelOld() {
        return selOld;
    }

    public void setSelOld(String selOld) {
        this.selOld = selOld;
    }

    public String getCatNew() {
        return catNew;
    }

    public void setCatNew(String catNew) {
        this.catNew = catNew;
    }

    public String getCatOld() {
        return catOld;
    }

    public void setCatOld(String catOld) {
        this.catOld = catOld;
    }

    public String getPartNameNew() {
        return partNameNew;
    }

    public void setPartNameNew(String partNameNew) {
        this.partNameNew = partNameNew;
    }

    public String getPartNameOld() {
        return partNameOld;
    }

    public void setPartNameOld(String partNameOld) {
        this.partNameOld = partNameOld;
    }

    public String getOccUid() {
        return occUid;
    }

    public void setOccUid(String occUid) {
        this.occUid = occUid;
    }

    public String getEplId() {
        return eplId;
    }

    public void setEplId(String eplId) {
        this.eplId = eplId;
    }

    public SYMCBOMEditData clone() {
    	SYMCBOMEditData temp = new SYMCBOMEditData();
    	
    	temp.setAltNew(getAltNew());
    	temp.setAltOld(getAltOld());
    	temp.setAsStkOld(asStkOld);
    	temp.setCatNew(catNew);
    	temp.setCatOld(catOld);
    	temp.setChangeType(changeType);
    	temp.setChgDesc(chgDesc);
    	temp.setColorIdNew(colorIdNew);
    	temp.setColorIdOld(colorIdOld);
    	temp.setColorSectionNew(colorSectionNew);
    	temp.setColorSectionOld(colorSectionOld);
    	temp.setCostNew(costNew);
    	temp.setEcoNo(ecoNo);
    	temp.setEcoNoOld(ecoNoOld);
    	temp.setEplId(eplId);
    	temp.setIcNew(icNew);
    	temp.setIcOld(icOld);
    	temp.setModuleCodeNew(moduleCodeNew);
    	temp.setModuleCodeOld(moduleCodeOld);
    	temp.setOccUid(occUid);
    	temp.setParentNo(parentNo);
    	temp.setParentRev(parentRev);
    	temp.setParentType(parentType);
    	temp.setPartNameNew(partNameNew);
    	temp.setPartNameOld(partNameOld);
    	temp.setPartNoNew(partNoNew);
    	temp.setPartNoOld(partNoOld);
    	temp.setPartOriginNew(partOriginNew);
    	temp.setPartOriginOld(partOriginOld);
    	temp.setPartRevNew(partRevNew);
    	temp.setPartRevOld(partRevOld);
    	temp.setPartTypeNew(partTypeNew);
    	temp.setPartTypeOld(partTypeOld);
    	temp.setPltStkOld(pltStkOld);
    	temp.setProjectNew(projectNew);
    	temp.setProjectOld(projectOld);
    	temp.setQtyNew(qtyNew);
    	temp.setQtyOld(qtyOld);
    	temp.setReleaseDate(releaseDate);
    	temp.setSelNew(selNew);
    	temp.setSelOld(selOld);
    	temp.setShownOnNew(shownOnNew);
    	temp.setShownOnOld(shownOnOld);
    	temp.setSupplyModeNew(supplyModeNew);
    	temp.setSupplyModeOld(supplyModeOld);
    	temp.setToolNew(toolNew);
    	temp.setUserId(userId);
    	temp.setVcNew(vcNew);
    	temp.setVcOld(vcOld);
    	
    	return temp;
    }
}
