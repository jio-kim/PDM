package com.symc.work.model;

import java.util.HashMap;

import com.symc.common.util.StringUtil;

public class EcoWhereUsedVO {
    String itemKey;
    String revItemType;
    String itemId;
    String itemRevId;
    HashMap<String, EcoWhereUsedVO> parentMap;
    HashMap<String, EcoWhereUsedVO> childMap;

    public EcoWhereUsedVO(String itemKey, String revItemType) {
        this.itemKey = itemKey;
        this.itemKey = StringUtil.nullToString(this.itemKey);
        String[] itemInfo = this.itemKey.split("_");
        this.itemId = itemInfo[0];
        this.itemRevId = itemInfo[1];
        this.revItemType = revItemType;
        this.parentMap = new HashMap<String, EcoWhereUsedVO>();
        this.childMap = new HashMap<String, EcoWhereUsedVO>();
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemRevId() {
        return itemRevId;
    }

    public String getRevItemType() {
        return revItemType;
    }

    public void setRevItemType(String revItemType) {
        this.revItemType = revItemType;
    }

    public void setParent(String itemKey, EcoWhereUsedVO parentEcoWhereUsedVO) {
        parentMap.put(itemKey, parentEcoWhereUsedVO);
    }

    public void setChild(String itemKey, EcoWhereUsedVO childEcoWhereUsedVO) {
        childMap.put(itemKey, childEcoWhereUsedVO);
    }

    public EcoWhereUsedVO getParent(String itemKey) {
        return parentMap.get(itemKey);
    }

    public EcoWhereUsedVO getChild(String itemKey) {
        return childMap.get(itemKey);
    }

    public HashMap<String, EcoWhereUsedVO> getParentMap() {
        return parentMap;
    }

    public HashMap<String, EcoWhereUsedVO> getChildMap() {
        return childMap;
    }
}
