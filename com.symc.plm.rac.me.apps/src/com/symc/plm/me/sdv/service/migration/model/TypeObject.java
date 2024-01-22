/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model;

/**
 * Class Name : TypeObject
 * Class Description :
 * 
 * @date 2013. 11. 22.
 * 
 */
public class TypeObject {
    int typeId;
    String masterParsingText;
    String relParsingText;

    /**
     * @return the typeId
     */
    public int getTypeId() {
        return typeId;
    }

    /**
     * @param typeId
     *            the typeId to set
     */
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    /**
     * @return the masterParsingText
     */
    public String getMasterParsingText() {
        return masterParsingText;
    }

    /**
     * @param masterParsingText
     *            the masterParsingText to set
     */
    public void setMasterParsingText(String masterParsingText) {
        this.masterParsingText = masterParsingText;
    }

    /**
     * @return the relParsingText
     */
    public String getRelParsingText() {
        return relParsingText;
    }

    /**
     * @param relParsingText
     *            the relParsingText to set
     */
    public void setRelParsingText(String relParsingText) {
        this.relParsingText = relParsingText;
    }

}
