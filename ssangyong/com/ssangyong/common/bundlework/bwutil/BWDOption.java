/**
 * 일괄작업 Download Option Class
 * DownLoad 가능한 Dataset List를 관리합니다.
 */

package com.ssangyong.common.bundlework.bwutil;

public class BWDOption extends BWOption
{
    // Level 사용여부
    private boolean isLevelAvailable = false;
    
    // Item 사용여부
    private boolean isItemAvailable = false;

    // BOM 사용여부
    private boolean isBOMAvailable = false;

    // Dataset 사용여부
    private boolean isDatasetAvailable = false;
    // Dataset File 다운로드 여부
    private boolean isDatasetDownloadable = false;
    
    
    // DownLoad 가능한 Dataset Type Array
    private String[] szDownLoadableDSType;
    
    public BWDOption(String[] szDSType)
    {
        this.szDownLoadableDSType = szDSType;
    }
    
    public String[] getDownLoadableDSType()
    {
        return this.szDownLoadableDSType;
    }
    
    public boolean isLevelAvailable(){return this.isLevelAvailable;}
    public boolean isItemAvailable(){return this.isItemAvailable;}
    public boolean isBOMAvailable(){return this.isBOMAvailable;}
    public boolean isDatasetAvailable(){return this.isDatasetAvailable;}
    public boolean isDatasetDownloadable(){return this.isDatasetDownloadable;}

    
    public void setLevelAvailable(boolean flag){this.isLevelAvailable = flag;}
    public void setItemAvailable(boolean flag){this.isItemAvailable = flag;}
    public void setBOMAvailable(boolean flag){this.isBOMAvailable = flag;}
    public void setDatasetAvailable(boolean flag){this.isDatasetAvailable = flag;}
    public void setDatasetDownloadable(boolean flag){this.isDatasetDownloadable = flag;}


    
}
