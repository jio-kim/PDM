package com.ssangyong.rac.kernel;

import java.io.Serializable;

public class SYMCECODwgData implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 1L;
    private String ecoNo;
    private String project;
    private String modelType;
    private String catProduct;
    private String partOrigin;
    private String partNo;
    private String revisionNo;
    private String revUid;
    private String partName;
    /** Part Responsibility **/
    private String responsibility;
    private String has3d;
    private String has2d;
    private String zip;
    private String sMode;
    private String changeDesc;

    public String getEcoNo() {
        return ecoNo;
    }

    public void setEcoNo(String ecoNo) {
        this.ecoNo = ecoNo;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getCatProduct() {
        return catProduct;
    }

    public void setCatProduct(String catProduct) {
        this.catProduct = catProduct;
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

    public String getRevisionNo() {
        return revisionNo;
    }

    public void setRevisionNo(String revisionNo) {
        this.revisionNo = revisionNo;
    }
    
    public String getRevUid() {
        return revUid;
    }
    
    public void setRevUid(String revUid) {
        this.revUid = revUid;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }
    
    public String getResponsibility() {
        return responsibility;
    }
    
    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }
    
    public String getHas3d() {
        return has3d;
    }

    public void setHas3d(String has3d) {
        this.has3d = has3d;
    }

    public String getHas2d() {
        return has2d;
    }

    public void setHas2d(String has2d) {
        this.has2d = has2d;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getsMode() {
        return sMode;
    }

    public void setsMode(String sMode) {
        this.sMode = sMode;
    }

    public String getChangeDesc() {
        return changeDesc;
    }

    public void setChangeDesc(String changeDesc) {
        this.changeDesc = changeDesc;
    }    

}