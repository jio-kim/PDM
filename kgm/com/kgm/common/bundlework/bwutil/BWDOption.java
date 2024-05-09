/**
 * �ϰ��۾� Download Option Class
 * DownLoad ������ Dataset List�� �����մϴ�.
 */

package com.kgm.common.bundlework.bwutil;

public class BWDOption extends BWOption
{
    // Level ��뿩��
    private boolean isLevelAvailable = false;
    
    // Item ��뿩��
    private boolean isItemAvailable = false;

    // BOM ��뿩��
    private boolean isBOMAvailable = false;

    // Dataset ��뿩��
    private boolean isDatasetAvailable = false;
    // Dataset File �ٿ�ε� ����
    private boolean isDatasetDownloadable = false;
    
    
    // DownLoad ������ Dataset Type Array
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
