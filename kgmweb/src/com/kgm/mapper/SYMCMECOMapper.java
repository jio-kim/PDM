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
 * [SR140828-015][20140829] shcho, Migration 후 MECO ID 채번을 601 부터 할 수 있도록 변경. (2015년에는 다시 001부터 채번 할 수 있도록 Preference를 이용한 초기값 설정 적용)
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
	// 수정자 : bc.kim
    // 이종화 차장님 요청 
    // 작업표준서 Preview 에서 공법 조회시 Resource 항목에 속성값이 변경( No Revise ) 되었을때 
    // 해당 Resource 의 기호가 MECO 의 기호에 맞게 변경 되는 로직 추가
	public ArrayList<HashMap<String, String>> getSymbomResourceMecoNo(DataSet ds);
	
	// 수정자 : bc.kim
	// 이종화 차장님 요청  SR: [SR190131-060]
	// 작업표준서 Preview 에서 공법 조회시 Resource 항목에 속성값이 변경( No Revise ) 되었을때 
	// 해당 Resource 의 기호가 MECO 의 기호에 맞게 변경 되는 로직 추가
	public ArrayList<HashMap<String, String>> getSymbomSubsidiaryMecoNo(DataSet ds);
	public Date getLastEPLLoadDate(DataSet ds);
	public ArrayList<HashMap<String, String>> getEndItemListOnFunction(DataSet ds);
	public String checkExistMEPL(DataSet ds);
	public int updateMEcoStatus(DataSet ds);
	public List<HashMap> getChangedNewItemIdList(DataSet ds);
}