package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

public interface CCNInfoMapper {

    public String selectMasterListKey();
    
    public ArrayList<HashMap<String, Object>> selectMasterSystemCode(DataSet dataSet);
    
    public ArrayList<HashMap<String, Object>> selectMasterInfoList(DataSet dataSet);
    
    public ArrayList<HashMap<String, Object>> selectMasterUsageInfoList(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectOSpecHeaderInfoList(DataSet dataSet);

    public void sendMail(DataSet ds);
    
    public void sendMailEai(DataSet ds);
    
    public void insertCCNMaster(DataSet dataSet);
    
    public void insertIfCCNMaster(DataSet dataSet);
    
    public void deleteCCNMaster(DataSet dataSet);
    
    public void insertEPLList(HashMap<String, Object> dataMap);
    
    public void insertEPLList_(HashMap<String, Object> dataMap);
    
    public void insertEPLUsageInfo(HashMap<String, Object> dataMap);
    
    public void deleteEPLList(String ccnId);
    
    public ArrayList<HashMap<String, Object>> selectUsageListId(String ccnId);
    
    public void deleteEPLUsageInfo(String listId);
    
    public void deleteEPLUsageInfoAll(String ccnId);    
    
    public void insertIfEPLList(HashMap<String, Object> dataMap);
    
    public void insertIfEPLUsageInfo(HashMap<String, Object> dataMap);
    
    public void correctCCNEPL(HashMap<String, Object> dataMap);
    
    public ArrayList<HashMap<String, Object>> selectCCNValidateMessage(DataSet dataSet);
    
    public void deleteIFCCNMaster(DataSet dataSet);
    
    public void deleteIFEPLUsageInfo(String ccnId);
    
    public void deleteIFEPLList(String ccnId);
    
    public void createIfCCN(DataSet dataSet);
    /**
     * CCN�� Reference �� Pre BOM ��Ʈ ����Ʈ��  ������ 
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, String>> selectPreBomPartsReferencedFromCCN(DataSet dataSet);
    
    public String getParent4Digit();
    
    public String getParent4DigitReleased();
    
    public String getPreRevisionPuid();
    
    public ArrayList<HashMap<String, Object>> arrParentEPLData(DataSet dataSet);
    
    public ArrayList<HashMap<String, Object>> arrParentEPLDataOld(DataSet dataSet);
    
    public ArrayList<HashMap<String, Object>> arrParentEPLDataNew(DataSet dataSet);
    
    public ArrayList<HashMap<String, Object>> getChildBOMPro(DataSet dataSet);
}
