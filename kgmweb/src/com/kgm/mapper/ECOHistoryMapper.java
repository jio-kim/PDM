package com.kgm.mapper;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCECODwgData;
import com.kgm.rac.kernel.SYMCPartListData;

public interface ECOHistoryMapper {
    public List<String> selectUserWorkingECO(DataSet ds);

    //public Integer insertECOBOMWork(SYMCBOMEditData bomEditData);
    public String isECOEPLChanged(DataSet ds);

    public String isECOEPLChangedByAnotherECO(DataSet ds);
    
    public Integer extractEPL(DataSet ds);
    
    public Integer generateECO(DataSet ds);

    public List<String> selectEPLData(DataSet ds);

    public List<SYMCECODwgData> selectECODwgList(DataSet ds);
    
    public Integer updateECODwgProperties(SYMCECODwgData bomEditData);

    public List<SYMCBOMEditData> selectECOEplList(DataSet ds);
    
    public List<HashMap<String,String>> selectInECOlList(DataSet ds);

    public Integer updateECOEPLProperties(SYMCBOMEditData bomEditData);
    
    public List<SYMCPartListData> selectECOPartList(DataSet ds);

    public Integer updateECOPartListProperties(SYMCPartListData partListData);
    
    public List<SYMCBOMEditData> selectECOBOMList(DataSet ds);
    
    // Demon
    public List<HashMap<String, String>> selectECODemonTarget() throws Exception;
    
    public List<HashMap<String, String>> selectOccurrenceECO(DataSet ds) throws Exception;
    
    public Integer updateOccurrenceECOApplied(DataSet ds) throws Exception;
    
    public List<HashMap<String, String>> selectReleasedECO(DataSet ds);
    
    public Integer insertECOInfoToVPM(DataSet ds) throws Exception;
    
    public Integer updateECOInfoInterfacedToVPM(DataSet ds) throws Exception;
    
    public List<HashMap<String, String>> selectECOEplCorrectionList(DataSet ds);
    
    public List<HashMap<String, String>> selectECOEplCOSModeCompareList(DataSet ds);
    
    public List<HashMap<String, String>> selectUnGeneratedCOPartList(DataSet ds);
    
    public List<HashMap<String, String>> selectECOChangeCause(DataSet ds);
    
    public void deleteECOChangeCause(DataSet ds);
    
    public Integer insertECOChangeCause(DataSet ds) throws Exception;
    
    public List<HashMap<String, String>> selectECOEplEndItemList(DataSet ds);
    
    public List<HashMap<String, String>> selectECOEplEndItemNameList(DataSet ds);
    
    public List<HashMap<String, String>> selectECOBOMListEndItemNameList(DataSet ds);
    
    public void deleteECOEplEndItemList(DataSet ds);
    
    public Integer insertECOEplEndItemList(DataSet ds) throws Exception;
    /*public Integer reviseBOMPart(DataSet ds);

    public Integer saveAsBOMPart(DataSet ds);*/
    /** [SR151204-016][20151209][jclee] DWG, EPL Change Desc에 Revision의 Change Desc 입력용 Query */
    public String getChangeDescription(DataSet ds);
    
    public String getECOAdminCheckCommonMemo();
    
    public Integer insertECOAdminCheckCommonMemo(DataSet ds) throws Exception;
    
    /**
     * 설계변경 현황 Option Category  리스트
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getEcoStatusOptCategoryList(DataSet ds);
    
    /**
     * 설계변경 현황 변경리스트 조회
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getEcoStatusChangeList(DataSet ds);
    
    public List<HashMap<String, String>> getEcoNullValueList(DataSet ds);
    
    /**
     * SYS_GUID 가져옴
     * @param ds
     * @return
     */
    public String getSysGuid(DataSet ds);
    
    /**
     * SYS_GUID 가져옴
     * @param ds
     * @return
     */
    public  List<String> getMultiSysGuidList(DataSet ds);
    
    /**
     * 설계변경 현황 기준정보리스트 저장
     * @param dataMap
     */
    public void insertRptStdInfo(HashMap<String, String> dataMap);
    
    /**
     * 설계변경 현황 변경 검토 리스트 저장
     * @param dataMap
     */
    public void insertRptChgReview(HashMap<String, String> dataMap);
    
    /**
     * 설계변경 현황 변경 리스트 저장
     * @param dataMap
     */
    public void insertRptList(HashMap<String, String> dataMap);
    
    /**
     * 설계변경 현황 기준정보리스트 조회(중복된 여부 확인용)
     * @param ds
     * @return
     */
    public List<String> getDupRptInfoList(DataSet ds);
    
    /**
     * 설계변경 현황 기준정보 삭제
     * @param ds
     */
    public void deleteRptStdInfo(DataSet ds);
    
    /**
     * 설계변경 현황 변경 검토정보 삭제
     * @param ds
     */
    public void deleteChgReview(DataSet ds);
    
    /**
     * 설계변경 현황 변경 리스트 삭제
     * @param ds
     */
    public void deleteRptList(DataSet ds);
    
    
    /**
     * 설계변경 현황 Option Condition 삭제
     * @param ds
     */
    public void deleteRptOptCnd(DataSet ds);
    
    /**
     * 변경 현황 전체리스트 조회
     * @param ds
     * @return
     */
    public List<HashMap<String, Object>> getEcoTotalStatusList(DataSet ds);
    
    /**
     * 변경 현황 기준정보리스트 조회
     * @param ds
     * @return
     */
    public List<HashMap<String, Object>> getEcoStatusStdList(DataSet ds);    
    
    
    
    /**
     * 설계변경 현황 변경 리스트 수정
     * @param dataMap
     */
    public void updateRptList(HashMap<String,String> dataMap);
    
    /**
     * 설계변경 현황 변경 리스트 ROW 삭제
     * @param ds
     */
    public void deleteRpListWithPuid(HashMap<String,String> dataMap);
    
    
    /**
     * 설계변경 현황 변경 검토  Row 삭제
     * @param ds
     */
    public void deleteChgReviewWithPuid(HashMap<String,String> dataMap);
    
    
    /**
     * 설계변경 현황 Option Condition Row 삭제
     * @param ds
     */
    public void deleteRptOptCndWithPuid(HashMap<String,String> dataMap);
    
    /**
     * 설계변경 현황 변경 리스트 Option Category Count
     * @param ds
     */
    public int getOpCategoryCount(HashMap<String,String> dataMap);
    
    /**
     * Project 의 G Model 코드를 가져옴
     * @param ds
     * @return
     */
    public String getGmodelWithProject(DataSet ds);
    
    /**
     * 설계변경 현황 기준정보리스트 수정
     * @param dataMap
     */
    public void updateRptStdInfo(DataSet ds);
    
    /**
     * OSPEC Revision 정보 리스트
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getOspecRevList(DataSet ds);
    
    /**
     * 프로젝트에 해당하는 EPL Job PUID 를 가져옴
     * @param ds
     * @return
     */
    public String getEPLJobPuid(DataSet ds);
    
    
    /**
     * 설계변경현황 등록 대상 변경정보 리스트
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getChangeTargetEPLList(DataSet ds);
    
    /**
     * 설계변경현황 EPL 리스트(중복포함)
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getAllChangeTargetEPLList(DataSet ds);
    
    
    /**
     * 설계변경현황 추가 검토 옵션 생성
     * @param dataMap
     */
    public void insertRptOptCondition(HashMap<String, String> dataMap);

    
    /**
     * ECO 현황 변경검토 Category 정보 리스트
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getRptChgReviewCategory(DataSet ds);
    
    /**
     * 설계변경 현황 변경 검토 리스트 Count
     * @param ds
     */
    public int getRptReviewCount(HashMap<String,String> dataMap);
    
    /**
     * 설계변경 현황 기준정보 With Master PUID
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getRptStdInformWithPuid(DataSet ds);
    
    /**
     * Function EPL Check 등록
     * @param dataMap
     */
    public void insertFncEplCheck(HashMap<String, String> dataMap);
    
    /**
     * Function EPL Check 리스트 조회
     * @param ds
     * @return
     */
    public List<HashMap<String, Object>> getFncEplCheckList(DataSet ds);
    
    /**
     * Function EPL Check 리스트 삭제
     * @param ds
     */
    public void deleteFncEpl(HashMap<String,String> dataMap);
    
    /**
     * Function EPL Check 수정
     * @param dataMap
     */
    public void updateFncEplCheck(DataSet ds);
    
    /**
     * Function EPL Check 현황조회
     * @param ds
     * @return
     */
    public List<HashMap<String, Object>> getFncEplCheckStatusList(DataSet ds);
    
    /////////  ECO Eci Ecr I/F 신규 기능을 위해 추가
    public String searchEciNo(DataSet ds);
	
	public String searchEcrNo(DataSet ds);
	
	public void updateEci(DataSet ds);
	
	public void updateEcr(DataSet ds);
	
	public List<HashMap<String, String>> datasetCheck(DataSet ds);
    
}
