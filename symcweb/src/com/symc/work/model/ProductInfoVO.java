package com.symc.work.model;

import java.util.Date;

public class ProductInfoVO {
    // IF_ID
    private String ifId;
    // ECO_ID
    private String ecoId;
    // PROJECT_ID
    private String projectId;
    // PRODUCT_ID
    private String productId;
    // PRODUCT_REV_ID
    private String productRevId;
    // CREATION_DATE
    private Date creationDate;
    // IF_DATE
    private Date ifDate;
    // WAIT_DATE
    private Date waitDate;
    // COMPLETE_DATE
    private Date completeDate;
    // TRANS_TYPE
    private String transType;
    // STAT
    private String stat;

    public String getIfId() {
        return ifId;
    }

    public void setIfId(String ifId) {
        this.ifId = ifId;
    }

    public String getEcoId() {
        return ecoId;
    }

    public void setEcoId(String ecoId) {
        this.ecoId = ecoId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductRevId() {
        return productRevId;
    }

    public void setProductRevId(String productRevId) {
        this.productRevId = productRevId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getIfDate() {
        return ifDate;
    }

    public void setIfDate(Date ifDate) {
        this.ifDate = ifDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getWaitDate() {
        return waitDate;
    }

    public void setWaitDate(Date waitDate) {
        this.waitDate = waitDate;
    }

    public Date getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }
}
