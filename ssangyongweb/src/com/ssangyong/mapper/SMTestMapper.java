package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.SMTestDao;

public interface SMTestMapper {

	public ArrayList<HashMap<String, Object>> getEngPSRepublishingTarget(DataSet ds);
	
	public ArrayList<HashMap<String, Object>> getShopLineInfo(DataSet ds);

	public ArrayList<HashMap<String, Object>> getMigEngList();

	public ArrayList<HashMap<String, Object>> getMigRepublishingList(DataSet ds);

	public ArrayList<HashMap<String, Object>> getWeldOPItemList(DataSet ds);
	
	public ArrayList<HashMap<String, Object>> getOperationListForWorkCount(DataSet ds);

	public ArrayList<HashMap<String, Object>> getFuncListByProduct(DataSet ds);

	public ArrayList<HashMap<String, Object>> getInEcoBP(DataSet ds);	

	public int getDCSWorkflowHistoryMaxSeq();
	
	public int insertDCSWorkflowHistory(DataSet ds);
	
	public ArrayList<HashMap<String, Object>> selectVNetUserList(DataSet dataSet);
	
	public ArrayList<HashMap<String, Object>> getProductList(DataSet dataSet);

    public void createEndItemList(DataSet dataSet);
    
    public void deleteMfgSpec(DataSet dataSet);
    
    public void insertMfgSpec(DataSet dataSet);
    
    public ArrayList<HashMap<String, Object>> getEndItemListforNameValidation(DataSet dataSet);
	
    public ArrayList<HashMap<String, Object>> getDeleteTargetItemList(DataSet dataSet);

    public void sendMail(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> getFunctions(DataSet dataSet);
    
    public String getJobPuid();

    public void createEPL(DataSet dataSet);
    
	public int insertProcessedParent(DataSet ds);
	
	public ArrayList<HashMap<String, Object>> getProcessedParent(DataSet ds);
	
	public ArrayList<HashMap<String, Object>> getEPLInfo(DataSet ds);
	
	
	
	
	
	
	public String getJobPUIDNO(DataSet dataSet) ;
	public String getCountItems(DataSet dataSet) ;
	public ArrayList<HashMap<String, Object>> getItemMasterInfo(DataSet dataSet);
	public ArrayList<HashMap<String, Object>> getSpecInfo(DataSet dataSet);
	
	// 생기쪽 FTP 로 전송 되지 못한 CGR 파일 조회 쿼리
	public ArrayList<HashMap<String, Object>> getNotUploadCGRFile(DataSet dataSet);
	
	public int setLicenseLevel(DataSet dataSet);
	public int setUserInactive(DataSet dataSet);
	public int setGroupMemberInactive(DataSet dataSet);
	public void refreshTCObject(DataSet ds);
	public void refreshTCTimeStamp(DataSet ds);
	
}
