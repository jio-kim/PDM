package com.kgm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.MECOInfoDao;
import com.kgm.dto.ApprovalLineData;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCBOPEditData;

public class MECOService {

//	public MECOService() {
//	}
	private MECOInfoDao dao;

	public String getNextMECOSerial(DataSet ds){
		dao = new MECOInfoDao();
        return dao.getNextMECOSerial(ds);
	}


	public ArrayList<ApprovalLineData> loadSavedUserApprovalLine(DataSet ds){
		dao = new MECOInfoDao();
        return dao.loadSavedUserApprovalLine(ds);
	}


	public ArrayList<ApprovalLineData> getApprovalLine(DataSet ds){
		dao = new MECOInfoDao();
        return dao.getApprovalLine(ds);
	}


	public boolean saveApprovalLine(DataSet ds){
		dao = new MECOInfoDao();
        return dao.saveApprovalLine(ds);
	}


	public boolean saveUserApprovalLine(DataSet ds){
		dao = new MECOInfoDao();
        return dao.saveUserApprovalLine(ds);
	}


	public boolean removeApprovalLine(DataSet ds){
		dao = new MECOInfoDao();
        return dao.removeApprovalLine(ds);
	}

	public boolean insertMECOEPL (DataSet ds) {
		dao = new MECOInfoDao();
		return dao.insertMECOEPL(ds);

	}

	public int deleteMECOEPL (DataSet ds) {
		dao = new MECOInfoDao();
		return dao.deleteMECOEPL(ds);
	}

	public ArrayList<HashMap<String, String>> checkModifiedMEPL (DataSet ds) {
		dao = new MECOInfoDao();
		return dao.checkModifiedMEPL(ds);
	}

	public void truncateModifiedEPL (DataSet ds) {
		dao = new MECOInfoDao();
		dao.truncateModifiedEPL(ds);
	}

	public ArrayList<SYMCBOPEditData> selectMECOEplList(DataSet ds) {
		dao = new MECOInfoDao();
		return dao.selectMECOEplList(ds);
	}

    public ArrayList<HashMap<String, String>> getEndItemMECONoForProcessSheet(DataSet ds){
        dao = new MECOInfoDao();
        return dao.getEndItemMECONoForProcessSheet(ds);
    }

    public ArrayList<HashMap<String, String>> getSubsidiaryMECONoForProcessSheet(DataSet ds){
        dao = new MECOInfoDao();
        return dao.getSubsidiaryMECONoForProcessSheet(ds);
    }

    public ArrayList<HashMap<String, String>> getResourceMECONoForProcessSheet(DataSet ds){
        dao = new MECOInfoDao();
        return dao.getResourceMECONoForProcessSheet(ds);
    }
    
    // ������ : bc.kim
    // ����ȭ ����� ��û 
    // �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Resource �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
    // �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
    public ArrayList<HashMap<String, String>> getSymbomResourceMecoNo(DataSet ds){
    	dao = new MECOInfoDao();
    	return dao.getSymbomResourceMecoNo(ds);
    }
    
    // ������ : bc.kim
    // ����ȭ ����� ��û  SR: [SR190131-060]
    // �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Resource �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
    // �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
    public ArrayList<HashMap<String, String>> getSymbomSubsidiaryMecoNo(DataSet ds){
    	dao = new MECOInfoDao();
    	return dao.getSymbomSubsidiaryMecoNo(ds);
    }

    public Date getLastEPLLoadDate(DataSet ds) {
        //System.out.println("getLastEPLLoadDate()");
        dao = new MECOInfoDao();
        return dao.getLastEPLLoadDate(ds);
    }

	public ArrayList<SYMCBOMEditData> searchECOEplList(DataSet ds) {
		dao = new MECOInfoDao();
		return dao.searchECOEplList(ds);
	}

	public ArrayList<HashMap<String, String>> getEndItemListOnFunction(DataSet ds) {
		dao = new MECOInfoDao();
		return dao.getEndItemListOnFunction(ds);
	}

    public String checkExistMEPL(DataSet ds) {
        dao = new MECOInfoDao();
        return dao.checkExistMEPL(ds);
    }
    
	public boolean updateMEcoStatus(DataSet ds){
		dao = new MECOInfoDao();
        return dao.updateMEcoStatus(ds);
	}

	public List<HashMap> getChangedNewItemIdList(DataSet ds){
		dao = new MECOInfoDao();
        return dao.getChangedNewItemIdList(ds);
	}
	
    
}
