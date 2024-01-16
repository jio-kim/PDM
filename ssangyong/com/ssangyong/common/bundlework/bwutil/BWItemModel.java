/**
 * 일괄작업시 Excel Template에 명시된 Data Header정보를 읽어 Model정보를 생성합니다.
 * Item Type별 속성과 해당 속성의 Excel Column 위치를 관리합니다.
 */

package com.ssangyong.common.bundlework.bwutil;

import java.util.HashMap;
import com.ssangyong.common.bundlework.BundleWorkDialog;

public class BWItemModel
{
    // Data Model 관리 Map
    // HashMap<"Item Type Name" , HashMap<"Item AttrName","Item Attr Column Position"> >
    HashMap<String, HashMap<String, Integer>> modelMap;
    
    public BWItemModel()
    {
        this.modelMap = new HashMap<String, HashMap<String, Integer>>();
        
        // 일괄작업에 명시된 Item Type으로 초기화
        for (int i = 0; i < BundleWorkDialog.CLASS_TYPE_LIST.length; i++)
        {
            this.modelMap.put(BundleWorkDialog.CLASS_TYPE_LIST[i], new HashMap<String, Integer>());
        }
        
    }
    
    /**
     * ModelData Setter
     * 
     * @param strItemName : Item Type Name
     * @param strAttr     : Item Attribute Name
     * @param intXlsIndex : Item Attribute Excel Column Position
     */
    public void setModelData(String strItemName, String strAttr, Integer intXlsIndex)
    {
        if (this.modelMap.containsKey(strItemName))
        {
            this.modelMap.get(strItemName).put(strAttr, intXlsIndex);
        }
    }
    
    /**
     * Item Model Attribute Map Getter
     * 
     * @param strItemName : Item Type Name
     * @return Item Model Attribute Map
     */
    public HashMap<String, Integer> getModelAttrs(String strItemName)
    {
        if (this.modelMap.containsKey(strItemName))
            return this.modelMap.get(strItemName);
        else
            return null;
    }
    
    public boolean isExistAttr(String strItemName, String strAttrName)
    {
    	HashMap<String,Integer> attrMap = this.getModelAttrs(strItemName);
    	if( attrMap == null)
    		return false;
    	
    	if( attrMap.containsKey(strAttrName))
    		return true;
    	else
    		return false;
    	
    }
    
    
    /**
     * Item Attribute의 Excel Column 위치를 가져옴
     * 
     * @param strItemName : Item Type Name
     * @param strAttrName : Item Attribute Name
     * @return : Attribute Excel Column Position
     */
    public int getItemAttrIndex(String strItemName, String strAttrName)
    {
        HashMap<String, Integer> itemAttrs = this.getModelAttrs(strItemName);
        if (itemAttrs == null)
        {
            return -1;
        }
        else
        {
            if (itemAttrs.containsKey(strAttrName))
                return itemAttrs.get(strAttrName);
            else
                return -1;
        }
    }
    
    
}

