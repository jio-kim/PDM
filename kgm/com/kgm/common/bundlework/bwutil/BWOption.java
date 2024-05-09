/**
 * �ϰ��۾� Option Class
 * �ϰ��۾��� �ʿ��� Dataset Type�� �����մϴ�.
 * ��õ��� ���� Option�� �߰� ���� �� �� �ֽ��ϴ�.
 */

package com.kgm.common.bundlework.bwutil;

import java.util.HashMap;

public abstract class BWOption
{
    

    
    // DataSet�� ���� Ȯ���� ���� Map
    // HashMap< "Dataset Type", "����Ȯ���� Array">
    private HashMap<String, String[]> dataSetRefExtMap;
    // �߰� Option ���� Map
    // HashMap<"Option Name" , "Option Flag">
    private HashMap<String, Boolean> extraOptionMap;
    
    public BWOption()
    {
        this.dataSetRefExtMap = new HashMap<String, String[]>();
        
        this.extraOptionMap = new HashMap<String, Boolean>();
    }
    
    /**
     * Dataset�� ÷�ε� ��ȿ�� ���� Ȯ���� ���
     * @param strDatasetType : Dataset Type
     * @param szFileExt      : File Extention Array
     */
    public void setDataRefExt(String strDatasetType, String[] szFileExt)
    {
        this.dataSetRefExtMap.put(strDatasetType, szFileExt);
    }
    
    /**
     * Dataset ��ȿ ���� Ȯ���� Getter
     * 
     * @param strDatasetName : Dataset Name
     * @return ���� Ȯ���� Array
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
     * �߰� Option Setter
     * 
     * @param key : Option Name
     * @param flag : Option Flag
     */
    public void setExtraOption(String key, boolean flag)
    {
        this.extraOptionMap.put(key, new Boolean(flag));
    }
    
    /**
     * �߰� Option Getter
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
