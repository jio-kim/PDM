/**
 * �ϰ��۾��� Data�Ӽ��� ������.
 * �ϰ��ٿ�ε�� TComponent, �ϰ����ε�� Excel�� ��õ� Item�Ӽ��� �����ϴ� Template Class
 */

package com.kgm.common.bundlework.bwutil;

import java.util.HashMap;

import com.kgm.common.bundlework.BundleWorkDialog;

public class BWItemData
{
    // Data���� Map
    // HashMap<"Item Type", HashMap<"AttrName","AttrValue"> >
    HashMap<String, HashMap<String, String>> modelMap;
    
    public BWItemData()
    {
        this.modelMap = new HashMap<String, HashMap<String, String>>();
        
        // �ϰ��۾��� ��õ� Item Type���� �ʱ�ȭ
        for (int i = 0; i < BundleWorkDialog.CLASS_TYPE_LIST.length; i++)
        {
            this.modelMap.put(BundleWorkDialog.CLASS_TYPE_LIST[i], new HashMap<String, String>());
        }
    }
    
    /**
     * Item Data Setter
     * 
     * @param strItemName : Item Type Name
     * @param strAttrName : Item Attribute Name
     * @param strAttrValue : Item Attribute Value
     */
    public void setItemData(String strItemName, String strAttrName, String strAttrValue)
    {
        if (this.modelMap.containsKey(strItemName))
        {
            this.modelMap.get(strItemName).put(strAttrName, strAttrValue);
        }
    }
    
    /**
     * Item Attriubte Map Getter
     * 
     * @param strItemName : Item Type Name
     * @return Item Attriubte Map
     */
    public HashMap<String, String> getItemAttrs(String strItemName)
    {
        if (this.modelMap.containsKey(strItemName))
            return this.modelMap.get(strItemName);
        else
            return null;
    }
    
    /**
     * Item Attribute Value Getter
     * 
     * @param strItemName : Item Type Name
     * @param strAttrName : Item Attribute Name
     * @return Attribute Value
     */
    public String getItemAttrValue(String strItemName, String strAttrName)
    {
        HashMap<String, String> itemAttrs = this.getItemAttrs(strItemName);
        if (itemAttrs == null)
        {
            return "";
        }
        else
        {
            if (itemAttrs.containsKey(strAttrName))
                return itemAttrs.get(strAttrName);
            else
                return "";
        }
    }
    
    public HashMap<String, HashMap<String, String>> getModelMap()
    {
      return this.modelMap;
    }
    
}
