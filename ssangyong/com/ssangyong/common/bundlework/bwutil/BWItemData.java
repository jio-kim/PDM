/**
 * 일괄작업시 Data속성을 관리함.
 * 일괄다운로드시 TComponent, 일괄업로드시 Excel에 명시된 Item속성을 관리하는 Template Class
 */

package com.ssangyong.common.bundlework.bwutil;

import java.util.HashMap;
import com.ssangyong.common.bundlework.BundleWorkDialog;

public class BWItemData
{
    // Data관리 Map
    // HashMap<"Item Type", HashMap<"AttrName","AttrValue"> >
    HashMap<String, HashMap<String, String>> modelMap;
    
    public BWItemData()
    {
        this.modelMap = new HashMap<String, HashMap<String, String>>();
        
        // 일괄작업에 명시된 Item Type으로 초기화
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
