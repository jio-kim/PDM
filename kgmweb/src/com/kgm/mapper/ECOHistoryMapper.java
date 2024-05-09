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
    /** [SR151204-016][20151209][jclee] DWG, EPL Change Desc�� Revision�� Change Desc �Է¿� Query */
    public String getChangeDescription(DataSet ds);
    
    public String getECOAdminCheckCommonMemo();
    
    public Integer insertECOAdminCheckCommonMemo(DataSet ds) throws Exception;
    
    /**
     * ���躯�� ��Ȳ Option Category  ����Ʈ
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getEcoStatusOptCategoryList(DataSet ds);
    
    /**
     * ���躯�� ��Ȳ ���渮��Ʈ ��ȸ
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getEcoStatusChangeList(DataSet ds);
    
    public List<HashMap<String, String>> getEcoNullValueList(DataSet ds);
    
    /**
     * SYS_GUID ������
     * @param ds
     * @return
     */
    public String getSysGuid(DataSet ds);
    
    /**
     * SYS_GUID ������
     * @param ds
     * @return
     */
    public  List<String> getMultiSysGuidList(DataSet ds);
    
    /**
     * ���躯�� ��Ȳ ������������Ʈ ����
     * @param dataMap
     */
    public void insertRptStdInfo(HashMap<String, String> dataMap);
    
    /**
     * ���躯�� ��Ȳ ���� ���� ����Ʈ ����
     * @param dataMap
     */
    public void insertRptChgReview(HashMap<String, String> dataMap);
    
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param dataMap
     */
    public void insertRptList(HashMap<String, String> dataMap);
    
    /**
     * ���躯�� ��Ȳ ������������Ʈ ��ȸ(�ߺ��� ���� Ȯ�ο�)
     * @param ds
     * @return
     */
    public List<String> getDupRptInfoList(DataSet ds);
    
    /**
     * ���躯�� ��Ȳ �������� ����
     * @param ds
     */
    public void deleteRptStdInfo(DataSet ds);
    
    /**
     * ���躯�� ��Ȳ ���� �������� ����
     * @param ds
     */
    public void deleteChgReview(DataSet ds);
    
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param ds
     */
    public void deleteRptList(DataSet ds);
    
    
    /**
     * ���躯�� ��Ȳ Option Condition ����
     * @param ds
     */
    public void deleteRptOptCnd(DataSet ds);
    
    /**
     * ���� ��Ȳ ��ü����Ʈ ��ȸ
     * @param ds
     * @return
     */
    public List<HashMap<String, Object>> getEcoTotalStatusList(DataSet ds);
    
    /**
     * ���� ��Ȳ ������������Ʈ ��ȸ
     * @param ds
     * @return
     */
    public List<HashMap<String, Object>> getEcoStatusStdList(DataSet ds);    
    
    
    
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param dataMap
     */
    public void updateRptList(HashMap<String,String> dataMap);
    
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ROW ����
     * @param ds
     */
    public void deleteRpListWithPuid(HashMap<String,String> dataMap);
    
    
    /**
     * ���躯�� ��Ȳ ���� ����  Row ����
     * @param ds
     */
    public void deleteChgReviewWithPuid(HashMap<String,String> dataMap);
    
    
    /**
     * ���躯�� ��Ȳ Option Condition Row ����
     * @param ds
     */
    public void deleteRptOptCndWithPuid(HashMap<String,String> dataMap);
    
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ Option Category Count
     * @param ds
     */
    public int getOpCategoryCount(HashMap<String,String> dataMap);
    
    /**
     * Project �� G Model �ڵ带 ������
     * @param ds
     * @return
     */
    public String getGmodelWithProject(DataSet ds);
    
    /**
     * ���躯�� ��Ȳ ������������Ʈ ����
     * @param dataMap
     */
    public void updateRptStdInfo(DataSet ds);
    
    /**
     * OSPEC Revision ���� ����Ʈ
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getOspecRevList(DataSet ds);
    
    /**
     * ������Ʈ�� �ش��ϴ� EPL Job PUID �� ������
     * @param ds
     * @return
     */
    public String getEPLJobPuid(DataSet ds);
    
    
    /**
     * ���躯����Ȳ ��� ��� �������� ����Ʈ
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getChangeTargetEPLList(DataSet ds);
    
    /**
     * ���躯����Ȳ EPL ����Ʈ(�ߺ�����)
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getAllChangeTargetEPLList(DataSet ds);
    
    
    /**
     * ���躯����Ȳ �߰� ���� �ɼ� ����
     * @param dataMap
     */
    public void insertRptOptCondition(HashMap<String, String> dataMap);

    
    /**
     * ECO ��Ȳ ������� Category ���� ����Ʈ
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getRptChgReviewCategory(DataSet ds);
    
    /**
     * ���躯�� ��Ȳ ���� ���� ����Ʈ Count
     * @param ds
     */
    public int getRptReviewCount(HashMap<String,String> dataMap);
    
    /**
     * ���躯�� ��Ȳ �������� With Master PUID
     * @param ds
     * @return
     */
    public List<HashMap<String, String>> getRptStdInformWithPuid(DataSet ds);
    
    /**
     * Function EPL Check ���
     * @param dataMap
     */
    public void insertFncEplCheck(HashMap<String, String> dataMap);
    
    /**
     * Function EPL Check ����Ʈ ��ȸ
     * @param ds
     * @return
     */
    public List<HashMap<String, Object>> getFncEplCheckList(DataSet ds);
    
    /**
     * Function EPL Check ����Ʈ ����
     * @param ds
     */
    public void deleteFncEpl(HashMap<String,String> dataMap);
    
    /**
     * Function EPL Check ����
     * @param dataMap
     */
    public void updateFncEplCheck(DataSet ds);
    
    /**
     * Function EPL Check ��Ȳ��ȸ
     * @param ds
     * @return
     */
    public List<HashMap<String, Object>> getFncEplCheckStatusList(DataSet ds);
    
    /////////  ECO Eci Ecr I/F �ű� ����� ���� �߰�
    public String searchEciNo(DataSet ds);
	
	public String searchEcrNo(DataSet ds);
	
	public void updateEci(DataSet ds);
	
	public void updateEcr(DataSet ds);
	
	public List<HashMap<String, String>> datasetCheck(DataSet ds);
    
}
