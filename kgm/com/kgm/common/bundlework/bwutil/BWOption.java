/**
 * 일괄작업 Option Class
 * 일괄작업에 필요한 Dataset Type을 관리합니다.
 * 명시되지 않은 Option을 추가 관리 할 수 있습니다.
 */

package com.kgm.common.bundlework.bwutil;

import java.util.HashMap;

public abstract class BWOption
{
    

    
    // DataSet의 파일 확장자 관리 Map
    // HashMap< "Dataset Type", "파일확장자 Array">
    private HashMap<String, String[]> dataSetRefExtMap;
    // 추가 Option 관리 Map
    // HashMap<"Option Name" , "Option Flag">
    private HashMap<String, Boolean> extraOptionMap;
    
    public BWOption()
    {
        this.dataSetRefExtMap = new HashMap<String, String[]>();
        
        this.extraOptionMap = new HashMap<String, Boolean>();
    }
    
    /**
     * Dataset에 첨부될 유효한 파일 확장자 명시
     * @param strDatasetType : Dataset Type
     * @param szFileExt      : File Extention Array
     */
    public void setDataRefExt(String strDatasetType, String[] szFileExt)
    {
        this.dataSetRefExtMap.put(strDatasetType, szFileExt);
    }
    
    /**
     * Dataset 유효 파일 확장자 Getter
     * 
     * @param strDatasetName : Dataset Name
     * @return 파일 확장자 Array
     */
    public String[] getDataRefExts(String strDatasetName)
    {
        return this.dataSetRefExtMap.get(strDatasetName);
    }
    
    
    public String getDataSetType(String strFileExtension)
    {
      String[] szKey = dataSetRefExtMap.keySet().toArray(new String[dataSetRefExtMap.size()]);
      
      for( int i = 0 ; i < szKey.length ; i++ )
      {
        String[] szExt = dataSetRefExtMap.get(szKey[i]);
        
        for( int j = 0 ; j < szExt.length ; j++ )
        {
          if(szExt[j].toUpperCase().equals(strFileExtension.toUpperCase()))
          {
            return szKey[i];
          }
        }
        
      }
      
      return null;
      
      
    }
    
    /**
     * 추가 Option Setter
     * 
     * @param key : Option Name
     * @param flag : Option Flag
     */
    public void setExtraOption(String key, boolean flag)
    {
        this.extraOptionMap.put(key, new Boolean(flag));
    }
    
    /**
     * 추가 Option Getter
     * 
     * @param key : Option Name
     * @return Option Flag
     */
    public boolean getExtraOption(String key)
    {
        if (this.extraOptionMap.containsKey(key))
            return this.extraOptionMap.get(key).booleanValue();
        else
            return false;
        
    }
    
}
