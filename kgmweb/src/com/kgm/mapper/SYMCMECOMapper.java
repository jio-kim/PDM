package com.kgm.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dto.ApprovalLineData;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCBOPEditData;

/**
 * [SR140828-015][20140829] shcho, Migration �� MECO ID ä���� 601 ���� �� �� �ֵ��� ����. (2015�⿡�� �ٽ� 001���� ä�� �� �� �ֵ��� Preference�� �̿��� �ʱⰪ ���� ����)
 * 
 */
public interface SYMCMECOMapper {


	public String getNextMECOSerial(DataSet ds);
	public void refreshTCObject(DataSet ds);
//	public void changeItemId(DataSet ds);
	public void refreshTCTimeStamp(DataSet ds);
	public ArrayList<ApprovalLineData> loadApprovalLine(ApprovalLineData paramMap);
	public ArrayList<ApprovalLineData> loadSavedUserApprovalLine(ApprovalLineData paramMap);
	public void saveApprovalLine(ApprovalLineData paramMap);
	public void saveUserApprovalLine(ApprovalLineData map);
	public void removeApprovalLine(ApprovalLineData paramMap);
	public ArrayList<ApprovalLineData> getApprovalLine(ApprovalLineData paramMap);
	public void insertMECOEPL(SYMCBOPEditData paramMap);
	public ArrayList<HashMap<String, String>> checkModifiedMEPL(DataSet ds);
	public void truncateModifiedMEPL(DataSet ds);
	public ArrayList<SYMCBOPEditData> selectMECOEplList(DataSet ds);
	public int deleteMECOEPL (DataSet ds);
	public ArrayList<SYMCBOMEditData> searchECOEplList(DataSet ds);
	public ArrayList<HashMap<String, String>> getEndItemMECONoForProcessSheet(DataSet ds);
	public ArrayList<HashMap<String, String>> getSubsidiaryMECONoForProcessSheet(DataSet ds);
	public ArrayList<HashMap<String, String>> getResourceMECONoForProcessSheet(DataSet ds);
	// ������ : bc.kim
    // ����ȭ ����� ��û 
    // �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Resource �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
    // �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
	public ArrayList<HashMap<String, String>> getSymbomResourceMecoNo(DataSet ds);
	
	// ������ : bc.kim
	// ����ȭ ����� ��û  SR: [SR190131-060]
	// �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Resource �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
	// �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
	public ArrayList<HashMap<String, String>> getSymbomSubsidiaryMecoNo(DataSet ds);
	public Date getLastEPLLoadDate(DataSet ds);
	public ArrayList<HashMap<String, String>> getEndItemListOnFunction(DataSet ds);
	public String checkExistMEPL(DataSet ds);
	public int updateMEcoStatus(DataSet ds);
	public List<HashMap> getChangedNewItemIdList(DataSet ds);
}