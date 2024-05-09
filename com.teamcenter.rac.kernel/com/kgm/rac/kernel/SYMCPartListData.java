package com.kgm.rac.kernel;

import java.io.Serializable;

public class SYMCPartListData implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    /** Part Id */
    private String partId;
    /** BOM 변경 유형(A, D, R, C) */
    private String changeType;
    /** Parent Part No */
    private String parentNo;
    /** Parent Part Revision No */
    private String parentRev;
    /** Child Part Origin */
    private String partOrigin;
    /** Child Part No */
    private String partNo;
    /** Child Part Revision No */
    private String partRev;
    /** Child Part Name **/
    private String partName;
    /** Child BOM Sequence No */
    private String seq;
    /** Child BOM Supply Mode */
    private String supplyMode;
    /** Child BOM Alter Part */
    private String alt;
    /** Child BOM Module Code */
    private String moduleCode;
    /** Child BOM Variant Condition */
    private String vc;
    /** ECO No MYBATIS 에서 사용 */
    private String ecoNo;
    /** Edit User Id MYBATIS 에서 사용 */
    private String userId;
    /** Child Qty */
    private String qty;
    /** Child PLT STK */
    private String pltStk;
    /** Child AS STK */
    private String asStk;
    /** Child Cost */
    private String cost;
    /** Child Tool */
    private String tool;
    /** Child Desc */
    private String desc;
    /** Parent Project */
    private String project;
    /** Child Color Id */
    private String colorId;
    /** Child Color Section */
    private String colorSection;
    /** Child Shown On **/
    private String shownOn;
    /** Child SEL **/
    private String sel;
    /** Child CAT **/
    private String cat;

    public SYMCPartListData() {
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
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

    public String getPartOrigin() {
        return partOrigin;
    }

    public void setPartOrigin(String partOrigin) {
        this.partOrigin = partOrigin;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getPartRev() {
        return partRev;
    }

    public void setPartRev(String partRev) {
        this.partRev = partRev;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getSupplyMode() {
        return supplyMode;
    }

    public void setSupplyMode(String supplyMode) {
        this.supplyMode = supplyMode;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getVc() {
        return vc;
    }

    public void setVc(String vc) {
        this.vc = vc;
    }

    public String getEcoNo() {
        return ecoNo;
    }

    public void setEcoNo(String ecoNo) {
        this.ecoNo = ecoNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getPltStk() {
        return pltStk;
    }

    public void setPltStk(String pltStk) {
        this.pltStk = pltStk;
    }

    public String getAsStk() {
        return asStk;
    }

    public void setAsStk(String asStk) {
        this.asStk = asStk;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getColorId() {
        return colorId;
    }

    public void setColorId(String colorId) {
        this.colorId = colorId;
    }

    public String getColorSection() {
        return colorSection;
    }
    
    public void setColorSection(String colorSection) {
        this.colorSection = colorSection;
    }
    
    public String getShownOn() {
        return shownOn;
    }

    public void setShownOn(String shownOn) {
        this.shownOn = shownOn;
    }

    public String getSel() {
        return sel;
    }

    public void setSel(String sel) {
        this.sel = sel;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }
    
    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }    
    
}
