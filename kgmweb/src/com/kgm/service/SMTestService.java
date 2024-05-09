package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.SMTestDao;

public class SMTestService {

	private SMTestDao dao;
	
	public ArrayList<HashMap<String, Object>> getEngPSRepublishingTarget(DataSet ds) {
		dao = new SMTestDao();
        return dao.getEngPSRepublishingTarget(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getShopLineInfo(DataSet ds) {
		dao = new SMTestDao();
        return dao.getShopLineInfo(ds);
	}

	public ArrayList<HashMap<String, Object>> getMigEngList() {
		dao = new SMTestDao();
        return dao.getMigEngList();
	}

	public ArrayList<HashMap<String, Object>> getMigRepublishingList(DataSet ds) {
		dao = new SMTestDao();
        return dao.getMigRepublishingList(ds);
	}

	public ArrayList<HashMap<String, Object>> getWeldOPItemList(DataSet ds) {
		dao = new SMTestDao();
        return dao.getWeldOPItemList(ds);
	}

	public ArrayList<HashMap<String, Object>> getOperationListForWorkCount(DataSet ds) {
		dao = new SMTestDao();
        return dao.getOperationListForWorkCount(ds);
	}

	public ArrayList<HashMap<String, Object>> getFuncListByProduct(DataSet ds) {
		dao = new SMTestDao();
        return dao.getFuncListByProduct(ds);
	}

	public ArrayList<HashMap<String, Object>> getInEcoBP(DataSet ds) {
		dao = new SMTestDao();
        return dao.getInEcoBP(ds);
	}
	
    public int getDCSWorkflowHistoryMaxSeq() {
        dao = new SMTestDao();

        return dao.getDCSWorkflowHistoryMaxSeq();
    }
	
	public boolean insertDCSWorkflowHistory(DataSet ds) {
		dao = new SMTestDao();
        return dao.insertDCSWorkflowHistory(ds);
	}
	
	public ArrayList<HashMap<String, Object>> selectVNetUserList(DataSet dataSet) {
		dao = new SMTestDao();

		return dao.selectVNetUserList(dataSet);
	}
	
	public ArrayList<HashMap<String, Object>> getProductList(DataSet dataSet) {
		dao = new SMTestDao();
        return dao.getProductList(dataSet);
	}

    public Boolean createEndItemList(DataSet dataSet) throws Exception
    {
    	Boolean isOK = false;

    	HashMap<String, String> rtnMap = new HashMap<String, String>();
        try
        {
        	dataSet.put("RTN_KEY", "");
        	dataSet.put("RTN_MSG", "");
        	
        	dao = new SMTestDao();
        	isOK = dao.createEndItemList(dataSet);
            
        }
        catch (Exception e)
        {
            rtnMap.put("RTN_CD", "ERROR");
            rtnMap.put("RTN_MSG", e.getCause().toString());
            e.printStackTrace();
        }
        return isOK;
        
    }

    public Boolean deleteMfgSpec(DataSet dataSet) throws Exception
    {
    	Boolean isOK = false;
        try
        {
        	dao = new SMTestDao();
        	isOK = dao.deleteMfgSpec(dataSet);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return isOK;
    }
    
    public Boolean insertMfgSpec(DataSet dataSet) throws Exception
    {
    	Boolean isOK = false;
        try
        {
        	dao = new SMTestDao();
        	isOK = dao.insertMfgSpec(dataSet);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return isOK;
    }
    
	public ArrayList<HashMap<String, Object>> getEndItemListforNameValidation(DataSet dataSet) {
		dao = new SMTestDao();
        return dao.getEndItemListforNameValidation(dataSet);
	}

	public ArrayList<HashMap<String, Object>> getDeleteTargetItemList(DataSet dataSet) {
		dao = new SMTestDao();
        return dao.getDeleteTargetItemList(dataSet);
	}
    
    public Boolean sendMail(DataSet dataSet) throws Exception
    {
    	Boolean isOK = false;
        try
        {
        	dao = new SMTestDao();
        	isOK = dao.sendMail(dataSet);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return isOK;
    }
	
	public ArrayList<HashMap<String, Object>> getFunctions(DataSet dataSet) {
		dao = new SMTestDao();
        return dao.getFunctions(dataSet);
	}
    
	public String getJobPuid() {
		dao = new SMTestDao();
        return dao.getJobPuid();
	}
	
    public Boolean createEPL(DataSet dataSet) throws Exception
    {
    	Boolean isOK = false;

    	HashMap<String, String> rtnMap = new HashMap<String, String>();
        try
        {
        	dataSet.put("RTN_KEY", "");
        	dataSet.put("RTN_MSG", "");
        	
        	dao = new SMTestDao();
        	isOK = dao.createEPL(dataSet);
        	
            System.out.println(dataSet.get("RTN_KEY").toString());
            System.out.println(dataSet.get("RTN_MSG").toString());
            
        }
        catch (Exception e)
        {
            rtnMap.put("RTN_CD", "ERROR");
            rtnMap.put("RTN_MSG", e.getCause().toString());
            e.printStackTrace();
        }
        return isOK;
        
    }

	public boolean insertProcessedParent(DataSet ds) throws Exception {
		dao = new SMTestDao();
        return dao.insertProcessedParent(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getProcessedParent(DataSet dataSet) throws Exception {
		dao = new SMTestDao();
        return dao.getProcessedParent(dataSet);
	}
	
	public ArrayList<HashMap<String, Object>> getEPLInfo(DataSet dataSet) throws Exception {
		dao = new SMTestDao();
        return dao.getEPLInfo(dataSet);
	}
	
	
	
	
	
	
	
	// 마힌드라 전송을 위한 CKD 리스트 조회 쿼리
	public String getJobPUIDNO(DataSet dataSet) throws Exception {
		dao = new SMTestDao();
		return dao.getJobPUIDNO(dataSet);
	}
	public String getCountItems(DataSet dataSet) throws Exception {
		dao = new SMTestDao();
		return dao.getCountItems(dataSet);
	}
	public ArrayList<HashMap<String, Object>> getItemMasterInfo(DataSet dataSet) throws Exception {
		dao = new SMTestDao();
		return dao.getItemMasterInfo(dataSet);
	}
	
	
	public ArrayList<HashMap<String, Object>> getSpecInfo(DataSet dataSet) throws Exception {
		dao = new SMTestDao();
		return dao.getSpecInfo(dataSet);
	}
	
	
	// 생기쪽 FTP 로 전송 되지 못한 CGR 파일 조회 쿼리
	public ArrayList<HashMap<String, Object>> getNotUploadCGRFile(DataSet dataSet ) throws Exception {
		dao = new SMTestDao();
		return dao.getNotUploadCGRFile(dataSet);
	}
	
	public boolean setLicenseLevel(DataSet ds) throws Exception{
		dao = new SMTestDao();
        return dao.setLicenseLevel(ds);
	}
	
	public boolean setUserInactive(DataSet ds) throws Exception{
		dao = new SMTestDao();
        return dao.setUserInactive(ds);
	}
	
	public boolean setGroupMemberInactive(DataSet ds) throws Exception{
		dao = new SMTestDao();
        return dao.setGroupMemberInactive(ds);
	}
	
}
