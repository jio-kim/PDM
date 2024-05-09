package com.kgm.commands.optiondefine.excel.vo;

import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;

/**
 * [Excel] Validation Option Combination - Condition VO
 * 
 */
public class OptionPart {
    // Item Id
    String itemId;
    // Vehicle Item Object Name
    String objectName;
    // Function Master Id
    String fmId;
    // Option Condition
    String strCondition;
    // ConditionElements
    ConditionElement[] conditionElements;
    // TCComponentBOMLine
    TCComponentBOMLine tccomponentbomline;
    // Function에 정의된 Options
    OVEOption[] setOptions;
    // Function에 정의된 Option들의 Value List
    HashMap<String, String> setOptionValueList;
    // Item 수량
    String qty;    
    // Excel Line Number
    String excelLineNumber;

    public OVEOption[] getSetOptions() {
        return setOptions;
    }

    public HashMap<String, String> getSetOptionValueList() {
        return setOptionValueList;
    }

    public void setSetOptionValueList(HashMap<String, String> setOptionValueList) {
        this.setOptionValueList = setOptionValueList;
    }

    public void setSetOptions(OVEOption[] setOptions) {
        this.setOptions = setOptions;
    }  

    public String getItemId() {
        return itemId;
    }

    public String getExcelLineNumber() {
        return excelLineNumber;
    }

    public void setExcelLineNumber(String excelLineNumber) {
        this.excelLineNumber = excelLineNumber;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getFmId() {
        return fmId;
    }

    public void setFmId(String fmId) {
        this.fmId = fmId;
    }

    public String getStrCondition() {
        return strCondition;
    }

    public void setStrCondition(String strCondition) {
        this.strCondition = strCondition;
    }

    public ConditionElement[] getConditionElements() {
        return conditionElements;
    }

    public void setConditionElements(ConditionElement[] conditionElements) {
        this.conditionElements = conditionElements;
    }

    public TCComponentBOMLine getTccomponentbomline() {
        return tccomponentbomline;
    }

    public void setTccomponentbomline(TCComponentBOMLine tccomponentbomline) {
        this.tccomponentbomline = tccomponentbomline;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

}
