package com.ssangyong.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.ECOHistoryDao;
import com.ssangyong.rac.kernel.SYMCBOMEditData;
import com.ssangyong.rac.kernel.SYMCECODwgData;
import com.ssangyong.rac.kernel.SYMCPartListData;

public class ECOHistoryService {

    public List<String> selectUserWorkingECO(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectUserWorkingECO(ds);
    }
    
    
    public Boolean isECOEPLChanged(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.isECOEPLChanged(ds);
    }
    
    public Boolean extractEPL(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.extractEPL(ds);
    }
    
    public Boolean generateECO(DataSet ds) {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.generateECO(ds);
    }

    /**
     * ECO-B(DWG) 테이블 리스트
     * 
     * @method selectECODwgList
     * @date 2013. 2. 20.
     * @param
     * @return List<String>
     * @exception
     * @throws
     * @see
     */
    public List<SYMCECODwgData> selectECODwgList(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectECODwgList(ds);
    }
    
    /**
     * ECO-B Properties Update
     * 
     * @method updateECOEPLProperties 
     * @date 2013. 3. 5.
     * @param
     * @return Boolean
     * @exception
     * @throws
     * @see
     */
    public Boolean updateECODwgProperties(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateECODwgProperties(ds);
    }
    
    /**
     * ECO-C(EPL) 테이블 리스트
     * 
     * @method selectECOEplList 
     * @date 2013. 2. 20.
     * @param
     * @return List<SYMCBOMEditData>
     * @exception
     * @throws
     * @see
     */
    public List<SYMCBOMEditData> selectECOEplList(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        List<SYMCBOMEditData> data = dao.selectECOEplList(ds); 
        return data;
    }
    
    public List<HashMap<String,String>> selectInECOlList(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        List<HashMap<String,String>> data = dao.selectInECOlList(ds); 
        return data;
    }

	/*public Boolean insertECOBOMWork(DataSet ds){
	    ECOHistoryDao dao = new ECOHistoryDao();
	    return dao.insertECOBOMWork(ds);
	}*/
    
	public List<String> selectEPLData(DataSet ds) {
	    ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectEPLData(ds);
	}
	
	/**
	 * ECO-C Properties Update
	 * 
	 * @method updateECOEPLProperties 
	 * @date 2013. 3. 5.
	 * @param
	 * @return Boolean
	 * @exception
	 * @throws
	 * @see
	 */
	public Boolean updateECOEPLProperties(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateECOEPLProperties(ds);
    }
	
	/**
	 * ECO-D(PartList) 테이블 리스트
	 * 
	 * @method selectECOPartList 
	 * @date 2013. 3. 8.
	 * @param
	 * @return List<SYMCPartListData>
	 * @exception
	 * @throws
	 * @see
	 */
    public List<SYMCPartListData> selectECOPartList(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectECOPartList(ds);
    }
	
	/**
	 * ECO-D Properties Update
	 * 
	 * @method updateECOPartListProperties 
	 * @date 2013. 3. 8.
	 * @param
	 * @return Boolean
	 * @exception
	 * @throws
	 * @see
	 */
    public Boolean updateECOPartListProperties(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateECOPartListProperties(ds);
    }
    
    public List<SYMCBOMEditData> selectECOBOMList(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectECOBOMList(ds);
    }
    
    // ECO Demon
    public List<HashMap<String, String>> selectECODemonTarget() throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectECODemonTarget();
    }
    
    public List<HashMap<String, String>> selectOccurrenceECO(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectOccurrenceECO(ds);
    }
    
    public Integer updateOccurrenceECOApplied(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateOccurrenceECOApplied(ds);
    }
    
    public List<HashMap<String, String>> selectReleasedECO(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectReleasedECO(ds);
    }

    public Boolean insertECOInfoToVPM(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertECOInfoToVPM(ds);
    }
    
    public List<HashMap<String, String>> selectECOEplCorrectionList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.selectECOEplCorrectionList(ds);
    }
    
    public List<HashMap<String, String>> selectECOEplCOSModeCompareList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.selectECOEplCOSModeCompareList(ds);
    }
    
    public List<HashMap<String, String>> selectUnGeneratedCOPartList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.selectUnGeneratedCOPartList(ds);
    }
    
    public List<HashMap<String, String>> selectECOChangeCause(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.selectECOChangeCause(ds);
    }
    
    public boolean deleteECOChangeCause(DataSet ds){
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.deleteECOChangeCause(ds);
    }
    
    public Boolean insertECOChangeCause(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.insertECOChangeCause(ds);
    }
    
    public List<HashMap<String, String>> selectECOEplEndItemList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.selectECOEplEndItemList(ds);
    }
    
    public List<HashMap<String, String>> selectECOEplEndItemNameList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.selectECOEplEndItemNameList(ds);
    }
    
    public List<HashMap<String, String>> selectECOBOMListEndItemNameList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.selectECOBOMListEndItemNameList(ds);
    }
    
	public boolean deleteECOEplEndItemList(DataSet ds){
		ECOHistoryDao dao = new ECOHistoryDao();
        return dao.deleteECOEplEndItemList(ds);
	}
	
    public Boolean insertECOEplEndItemList(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertECOEplEndItemList(ds);
    }
    
    /**
     * [SR151204-016][20151209][jclee] DWG, EPL Change Desc에 Revision의 Change Desc 입력용 Query
     */
    public String getChangeDescription(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.getChangeDescription(ds);
    }
    
    /**
     * get ECO Admin Check Common Memo
     */
    public String getECOAdminCheckCommonMemo() throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getECOAdminCheckCommonMemo();
    }
    
    /**
     * insert ECO Admin Check Common Memo
     */
    public Boolean insertECOAdminCheckCommonMemo(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.insertECOAdminCheckCommonMemo(ds);
    }
    
    /**
     * 변경 현황 변경리스트의 Option Category 를 가져옴
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> getEcoStatusOptCategoryList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getEcoStatusOptCategoryList(ds);
    }
    
    /**
     *설계변경현황 변경리스트
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> getEcoStatusChangeList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getEcoStatusChangeList(ds);
    }
    
    public List<HashMap<String, String>> getEcoNullValueList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getEcoNullValueList(ds);
    }
    
    public String getSysGuid(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.getSysGuid(ds);
    }
    
    /***
     * MULTI SYS_GUID를 가져옴
     * @param ds
     * @return
     * @throws Exception
     */
    public  List<String> getMultiSysGuidList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getMultiSysGuidList(ds);
    }
    
    /**
     * 설계변경 현황 기준정보리스트 저장
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertRptStdInfo(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertRptStdInfo(dataSet);
    }
	
    /**
     * 설계변경 현황 검토 리스트 저장
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertRptChgReview(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertRptChgReview(dataSet);
    }
	
    /**
     * 설계변경 현황 변경 리스트 저장
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertRptList(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertRptList(dataSet);
    }
	
	/**
	 * 설계변경현황 기준정보 리스트 조회(중복된 리스트 중복확인)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<String> getDupRptInfoList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getDupRptInfoList(ds);
    }
    
	/**
	 *  설계변경 현황 변경 리스트 삭제
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public Boolean deleteRptChangeList(DataSet ds) throws Exception{
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.deleteRptChangeList(ds);
    }
	
    
    /**
     *설계변경현황 변경리스트
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getEcoTotalStatusList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getEcoTotalStatusList(ds);
    }
    
    /**
     *설계변경현황 기준정보 리스트
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getEcoStatusStdList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getEcoStatusStdList(ds);
    }
    
    
    /**
     * 설계변경 현황 변경 리스트 저장
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean updateRptList(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateRptList(dataSet);
    }
	
    /**
     * 설계변경 현황 변경 리스트 저장
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean deleteRpListWithPuid(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.deleteRpListWithPuid(dataSet);
    }
	
	
    /**
     * 설계변경 현황 변경 리스트 저장
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean deleteChgReviewWithPuid(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.deleteChgReviewWithPuid(dataSet);
    }
	
	/**
	 * Project 의 G Model 코드를 가져옴
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public String getGmodelWithProject(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.getGmodelWithProject(ds);
    }
    
    /**
     * 설계변경 현황 변경 리스트 저장
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean updateRptStdInfo(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateRptStdInfo(dataSet);
    }
	
	
	/**
	 * OSPEC Revision 정보 리스트
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> getOspecRevList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getOspecRevList(ds);
    }
    
    
	/**
	 * EPL의 최신 JOB PUID를 가져옴
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public String getEPLJobPuid(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.getEPLJobPuid(ds);
    }
    
	/**
	 * OSPEC Revision 정보 리스트
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> getChangeTargetEPLList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getChangeTargetEPLList(ds);
    }
    
    
	/**
	 * 설계변경현황 EPL 리스트(중복포함)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> getAllChangeTargetEPLList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getAllChangeTargetEPLList(ds);
    }
    
    
    /**
     * 설계변경현황 추가 검토 옵션 생성
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertRptOptCondition(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertRptOptCondition(dataSet);
    }
	
	/**
	 * ECO 현황 변경검토 Category 정보 리스트
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> getRptChgReviewCategory(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getRptChgReviewCategory(ds);
    }
	
	/**
	 * 설계변경 현황 변경 검토 리스트 Count
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public int getRptReviewCount(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getRptReviewCount(ds);
    }
    
    
	/**
	 *  설계변경 현황 정보 삭제, 기준정보는 제외함
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public Boolean deleteRptReviewList(DataSet ds) throws Exception{
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.deleteRptReviewList(ds);
    }
    
    /**
     *설계변경 현황 기준정보 With Master PUID
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> getRptStdInformWithPuid(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getRptStdInformWithPuid(ds);
    }
    
    /**
     * Function EPL Check 등록
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertFncEplCheck(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertFncEplCheck(dataSet);
    }
    
	
    /**
     *Function EPL Check 리스트 조회
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getFncEplCheckList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getFncEplCheckList(ds);
    }
    
    /**
     * Function EPL Check 리스트 삭제
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean deleteFncEpl(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.deleteFncEpl(dataSet);
    }
	
    /**
     * Function EPL Check 수정
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean updateFncEplCheck(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateFncEplCheck(dataSet);
    }
	
    /**
     *Function EPL Check 현황 조회
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getFncEplCheckStatusList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getFncEplCheckStatusList(ds);
    }
    
    
    /**
     *  ECO ECIECR Interface 기능 
     */
    public String searchEciNo(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.searchEciNo(ds);
    }
    
    public String searchEcrNo(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.searchEcrNo(ds);
    }

    public Boolean updateEci(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateEci(dataSet);
    }
    
    public Boolean updateEcr(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateEcr(dataSet);
    }
    
    public List<HashMap<String, String>> datasetCheck(DataSet ds) throws Exception {
	    ECOHistoryDao dao = new ECOHistoryDao();
        return dao.datasetCheck(ds);
	}
}
