package com.kgm.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.ECOHistoryDao;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCECODwgData;
import com.kgm.rac.kernel.SYMCPartListData;

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
     * ECO-B(DWG) ���̺� ����Ʈ
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
     * ECO-C(EPL) ���̺� ����Ʈ
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
	 * ECO-D(PartList) ���̺� ����Ʈ
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
     * [SR151204-016][20151209][jclee] DWG, EPL Change Desc�� Revision�� Change Desc �Է¿� Query
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
     * ���� ��Ȳ ���渮��Ʈ�� Option Category �� ������
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> getEcoStatusOptCategoryList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getEcoStatusOptCategoryList(ds);
    }
    
    /**
     *���躯����Ȳ ���渮��Ʈ
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
     * MULTI SYS_GUID�� ������
     * @param ds
     * @return
     * @throws Exception
     */
    public  List<String> getMultiSysGuidList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getMultiSysGuidList(ds);
    }
    
    /**
     * ���躯�� ��Ȳ ������������Ʈ ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertRptStdInfo(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertRptStdInfo(dataSet);
    }
	
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertRptChgReview(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertRptChgReview(dataSet);
    }
	
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertRptList(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertRptList(dataSet);
    }
	
	/**
	 * ���躯����Ȳ �������� ����Ʈ ��ȸ(�ߺ��� ����Ʈ �ߺ�Ȯ��)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<String> getDupRptInfoList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getDupRptInfoList(ds);
    }
    
	/**
	 *  ���躯�� ��Ȳ ���� ����Ʈ ����
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public Boolean deleteRptChangeList(DataSet ds) throws Exception{
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.deleteRptChangeList(ds);
    }
	
    
    /**
     *���躯����Ȳ ���渮��Ʈ
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getEcoTotalStatusList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getEcoTotalStatusList(ds);
    }
    
    /**
     *���躯����Ȳ �������� ����Ʈ
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getEcoStatusStdList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getEcoStatusStdList(ds);
    }
    
    
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean updateRptList(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateRptList(dataSet);
    }
	
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean deleteRpListWithPuid(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.deleteRpListWithPuid(dataSet);
    }
	
	
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean deleteChgReviewWithPuid(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.deleteChgReviewWithPuid(dataSet);
    }
	
	/**
	 * Project �� G Model �ڵ带 ������
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public String getGmodelWithProject(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.getGmodelWithProject(ds);
    }
    
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean updateRptStdInfo(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateRptStdInfo(dataSet);
    }
	
	
	/**
	 * OSPEC Revision ���� ����Ʈ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> getOspecRevList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getOspecRevList(ds);
    }
    
    
	/**
	 * EPL�� �ֽ� JOB PUID�� ������
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public String getEPLJobPuid(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.getEPLJobPuid(ds);
    }
    
	/**
	 * OSPEC Revision ���� ����Ʈ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> getChangeTargetEPLList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getChangeTargetEPLList(ds);
    }
    
    
	/**
	 * ���躯����Ȳ EPL ����Ʈ(�ߺ�����)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> getAllChangeTargetEPLList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getAllChangeTargetEPLList(ds);
    }
    
    
    /**
     * ���躯����Ȳ �߰� ���� �ɼ� ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertRptOptCondition(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertRptOptCondition(dataSet);
    }
	
	/**
	 * ECO ��Ȳ ������� Category ���� ����Ʈ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> getRptChgReviewCategory(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getRptChgReviewCategory(ds);
    }
	
	/**
	 * ���躯�� ��Ȳ ���� ���� ����Ʈ Count
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public int getRptReviewCount(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getRptReviewCount(ds);
    }
    
    
	/**
	 *  ���躯�� ��Ȳ ���� ����, ���������� ������
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public Boolean deleteRptReviewList(DataSet ds) throws Exception{
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.deleteRptReviewList(ds);
    }
    
    /**
     *���躯�� ��Ȳ �������� With Master PUID
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> getRptStdInformWithPuid(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getRptStdInformWithPuid(ds);
    }
    
    /**
     * Function EPL Check ���
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean insertFncEplCheck(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertFncEplCheck(dataSet);
    }
    
	
    /**
     *Function EPL Check ����Ʈ ��ȸ
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getFncEplCheckList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getFncEplCheckList(ds);
    }
    
    /**
     * Function EPL Check ����Ʈ ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean deleteFncEpl(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.deleteFncEpl(dataSet);
    }
	
    /**
     * Function EPL Check ����
     * @param dataSet
     * @return
     * @throws Exception
     */
	public Boolean updateFncEplCheck(DataSet dataSet) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateFncEplCheck(dataSet);
    }
	
    /**
     *Function EPL Check ��Ȳ ��ȸ
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getFncEplCheckStatusList(DataSet ds) throws Exception {
    	ECOHistoryDao dao = new ECOHistoryDao();
    	return dao.getFncEplCheckStatusList(ds);
    }
    
    
    /**
     *  ECO ECIECR Interface ��� 
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
