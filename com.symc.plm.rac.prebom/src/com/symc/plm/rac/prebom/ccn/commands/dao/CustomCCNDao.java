package com.symc.plm.rac.prebom.ccn.commands.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentUser;

/**
 * [20160715][ymjang] CCN EPL ������ ����
 * [20160718] IF CCN Master ���� ���� ���� ���� --> Stored Procedure �� �̰���.
 */
@SuppressWarnings("unchecked")
public class CustomCCNDao {

    public static final String CCN_INFO_SERVICE_CLASS = "com.kgm.service.CCNService";//com.kgm.dao.ECOInfoDao�� �ص� ��

    private SYMCRemoteUtil remoteQuery;
    private DataSet ds;
    public CustomCCNDao() {
        this.remoteQuery = new SYMCRemoteUtil();
    }
    
    /**
     * CCN �� ����Ǿ��ִ� Master List �� �ý����ڵ带 ���� �����´�
     * @param ccnId
     * @return
     * @throws Exception
     */
    public ArrayList<HashMap<String, Object>> selectMasterSystemCode(String ccnId) throws Exception {

        ArrayList<HashMap<String, Object>> resultList = null;
        ds = new DataSet();
        ds.put("ccnId", ccnId);

        resultList = (ArrayList<HashMap<String, Object>>)remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "selectMasterSystemCode", ds);

        return resultList;
    }
    
    /**
     * CCN �� ����Ǿ��ִ� Master List �� �����´�
     * @param ccnId
     * @return
     * @throws Exception
     */
    public ArrayList<HashMap<String, Object>> selectMasterInfoList(String ccnId) throws Exception {

        ArrayList<HashMap<String, Object>> resultList = null;
        ds = new DataSet();
        ds.put("ccnId", ccnId);

        resultList = (ArrayList<HashMap<String, Object>>)remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "selectMasterInfoList", ds);

        return resultList;
    }
    
    /**
     * ������ ����Ʈ�� ����Ǿ� �ִ� usage ������ �����´�
     * @param listId
     * @return
     * @throws Exception
     */
    public ArrayList<HashMap<String, Object>> selectMasterUsageInfoList(String listId, String historyType) throws Exception {

        ArrayList<HashMap<String, Object>> resultList = null;
        ds = new DataSet();
        ds.put("listId", listId);
        ds.put("historyType", historyType);

        resultList = (ArrayList<HashMap<String, Object>>)remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "selectMasterUsageInfoList", ds);

        return resultList;
    }
    
    /**
     * ������ ����Ʈ �ش��� ����� �ִ� Ospec ������ �����´�
     * @param ospecNo
     * @return
     * @throws Exception
     */
    public ArrayList<HashMap<String, Object>> selectOSpecHeaderInfoList(String ospecNo) throws Exception {

        ArrayList<HashMap<String, Object>> resultList = null;
        ds = new DataSet();
        ds.put("ospecNo", ospecNo);

        resultList = (ArrayList<HashMap<String, Object>>)remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "selectOSpecHeaderInfoList", ds);

        return resultList;
    }
    
    /**
     * Vision net �� ���� ���� �߼�
     * @param fromUser
     * @param title
     * @param body
     * @param toUsers
     * @return
     * @throws Exception
     */
    public boolean sendMail(String fromUser, String title, String body, String toUsers) throws Exception{
        ds = new DataSet();
        ds.put("the_sysid", "NPLM");
        
        if(fromUser == null || fromUser.equals("")){
            ds.put("the_sabun", "NPLM");
        } else {
            ds.put("the_sabun", fromUser);
        }
        
        ds.put("the_title", title);
        ds.put("the_remark", body);
        ds.put("the_tsabun", toUsers);
        return (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "sendMail", ds);
    }
    
    /**
     *  I/F ���� ����� CCN_MASTER ���Ϻ� ������ ���� �ִ´� ()
     * @param ccnRevision
     * @param ifStatus   (if ���̺� ������ ����)
     * @return
     * @throws Exception
     */
    public boolean insertCCNMaster(TCComponentChangeItemRevision ccnRevision, boolean ifStatus) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ArrayList<HashMap< String, Object>> resultList = selectMasterSystemCode(ccnRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID).toString());
        String sysCodes = "";
        for (int i = 0; i < resultList.size(); i++) {
            if (null != resultList.get(i)) {
                sysCodes += (String) resultList.get(i).get("MASTER_LIST_SYSCODE");
                if ((i + 1) == resultList.size()) {
                    break;
                }
                sysCodes += ", ";
            }
        }
        ds.put("AFFETED_SYS_CODE", sysCodes);
        
        ds.put("CCN_NO", ccnRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID).toString());
        ds.put("PROJECT_CODE", ccnRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE).toString());
        ds.put("PROJECT_TYPE", ccnRevision.getProperty(PropertyConstant.ATTR_NAME_PROJECTTYPE).toString());
        ds.put("SYSTEM_CODE", ccnRevision.getProperty(PropertyConstant.ATTR_NAME_SYSTEMCODE).toString());
        ds.put("CHG_DESC", ccnRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC).toString());
        
        ds.put("OSPEC_NO", ccnRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO).toString());
        TCComponentUser owingUser = (TCComponentUser) ccnRevision.getRelatedComponent(PropertyConstant.ATTR_NAME_OWNINGUSER);
        TCComponentGroup owingGroup = (TCComponentGroup) ccnRevision.getRelatedComponent(PropertyConstant.ATTR_NAME_OWNINGGROUP);
        ds.put("CREATOR", owingUser.getUserId());
        ds.put("DEPT_NAME", owingGroup.getGroupName());
        String resultBoolean = "N";
        if (ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_REGULATION)) {
            resultBoolean = "Y";
        }
        ds.put("REGULATION", resultBoolean);
        if (!ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_COSTDOWN)) {
            resultBoolean = "N";
        }
        ds.put("COST_DOWN", resultBoolean);
        if (ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_ORDERINGSPEC)) {
            resultBoolean = "Y";
        }
        ds.put("ORDERING_SPEC", resultBoolean);
        if (!ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT)) {
            resultBoolean = "N";
        }
        ds.put("QUALITY_IMPROVEMENT", resultBoolean);
        if (ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL)) {
            resultBoolean = "Y";
        }
        ds.put("CORRECTION_OF_EPL", resultBoolean);
        if (!ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_STYLINGUPDATE)) {
            resultBoolean = "N";
        }
        ds.put("STYLING_UPDATE", resultBoolean);
        if (ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_ORDERINGSPEC)) {
            resultBoolean = "Y";
        }
        ds.put("ORDERING_SPEC", resultBoolean);
        if (!ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_WEIGHTCHANGE)) {
            resultBoolean = "N";
        }
        ds.put("WEIGHT_CHANGE", resultBoolean);
        if (ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE)) {
            resultBoolean = "Y";
        }
        ds.put("MATERIAL_COST_CHANGE", resultBoolean);
        if (!ccnRevision.getLogicalProperty(PropertyConstant.ATTR_NAME_THEOTHERS)) {
            resultBoolean = "N";
        }
        ds.put("THE_OTHERS", resultBoolean);
        ds.put("GATE", ccnRevision.getProperty(PropertyConstant.ATTR_NAME_GATENO));
        
        ds.put("RELEASE_DATE", ccnRevision.getDateProperty(PropertyConstant.ATTR_NAME_DATERELEASED));

        result = (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "insertCCNMaster", ds);
        
        if (ifStatus) {
        	/**
        	 * [20160610] IF CCN Master ���� �� ����(�ߺ� �߻������� ���� ���� ������ ����)
        	 */
        	deleteIFCCNMaster(ccnRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID).toString());
        	
            result = (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "insertIfCCNMaster", ds);
        }
        return result;
    }
    
    /**
     * I/F ���� ����� CCN_MASTER ���Ϻ� ������ ����ִ� CCN ������ ���� �Ѵ�
     * @param ccnId
     * @return
     * @throws Exception
     */
    public boolean deleteCCNMaster(String ccnId) throws Exception {
        ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        return (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "deleteCCNMaster", ds);
    }
    
    /**
     * CCN EPL ������ Insert �Ѵ�
     * 
     * @param ccnId
     * @param eplList
     * @return
     * @throws Exception
     */
    public boolean insertCCNEplList(String ccnId, List<HashMap<String, Object>> eplList) throws Exception {
        ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        ds.put("EPL_LIST", eplList);
        // ���� CCN EPL ������ ���� �Ѵ�
        boolean result = (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "insertEPLList", ds);
        return result;
    }
    
    public boolean insertCCNEplList_(String ccnId, List<HashMap<String, Object>> eplList) throws Exception {
        ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        ds.put("EPL_LIST", eplList);
        // ���� CCN EPL ������ ���� �Ѵ�
        boolean result = (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "insertEPLList_", ds);
        return result;
    }

    /**
     * CCN EPL ������ Insert �Ѵ�
     * 
     * @param ccnId
     * @param eplList
     * @return
     * @throws Exception
     */
    public boolean insertOSpecEplList(String ccnId, HashMap<String, HashMap<String, Object>> eplList, HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> usageList) throws Exception {
        ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        ds.put("EPL_LIST", eplList);
        ds.put("USAGE_LIST", usageList);
        // ���� CCN EPL ������ ���� �Ѵ�
        boolean result = (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "insertEPLListDiff", ds);
        return result;
    }
    
    public boolean insertUsage(HashMap<String, Object> hashmap) throws Exception {
    	ds = new DataSet();
    	
    	Object oCCNID = hashmap.get("CCN_ID");
    	String sCCNID = oCCNID != null ? oCCNID.toString() : "";
    	
    	ds.put("CCN_NO", sCCNID);
    	ds.put("EPL_LIST", hashmap);
    	
    	boolean result = (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "insertUsage", ds);
    	return result;
    }
    
    /**
     * CCN EPL ������ Insert �Ѵ� (IF ��)
     * 
     * @param ccnId
     * @param eplList
     * @return
     * @throws Exception
     */
    public boolean insertIfCCNEplList(String ccnId, List<HashMap<String, Object>> eplList) throws Exception {
        ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        ds.put("EPL_LIST", eplList);
        boolean result = (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "insertIfEPLList", ds);
        return result;
    }

    /**
     * CCN EPL ������ Insert �Ѵ� (IF ��)
     * 
     * @param ccnId
     * @param eplList
     * @return
     * @throws Exception
     */
    public boolean insertIfOSpecEplList(String ccnId, HashMap<String, HashMap<String, Object>> eplList, HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> usageList) throws Exception {
        ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        ds.put("EPL_LIST", eplList);
        ds.put("USAGE_LIST", usageList);
        boolean result = (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "insertIfEPLListDiff", ds);
        return result;
    }
    
    
    /**
     * CCN Validation
     * @param ccnId
     * @return
     * @throws Exception
     */
    public ArrayList<HashMap<String, Object>> selectCCNValidateMessage(String ccnId) throws Exception {

        ArrayList<HashMap<String, Object>> resultList = null;
        ds = new DataSet();
        ds.put("ccnId", ccnId);

        resultList = (ArrayList<HashMap<String, Object>>)remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "selectCCNValidateMessage", ds);

        return resultList;
    }
    
    /**
     * IF CCN Master �� ������
     * @param ccnId
     * @return
     * @throws Exception
     */
    public boolean deleteIFCCNMaster(String ccnId) throws Exception {
    	DataSet ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        return (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "deleteIFCCNMaster", ds);
    }
    
    /**
     * [20160715][ymjang] CCN EPL ������ ����
     * @param ccnId
     * @return
     * @throws Exception
     */
    public boolean correctCCNEPL(String ccnId) throws Exception{
    	DataSet ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        return (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "correctCCNEPL", ds);
    }
    
    /**
     * [20160718] IF CCN Master ���� ���� ���� ���� --> Stored Procedure �� �̰���.
     * @param ccnId
     * @return
     * @throws Exception
     */
    public boolean createIfCCN(String ccnId) throws Exception{
    	DataSet ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        return (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "createIfCCN", ds);
    }
    
    /**
     * CCN�� Reference �� Pre BOM ��Ʈ ����Ʈ��  ������ 
     * @param ccnId
     * @return
     * @throws Exception
     */
    public ArrayList<HashMap<String, String>> selectPreBomPartsReferencedFromCCN(String ccnId, String ccnRev) throws Exception {

        ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        ds.put("CCN_REV", ccnRev);

        resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "selectPreBomPartsReferencedFromCCN", ds);

        return resultList;
    }
    
    /**
     * EPL ����Ʈ�� ������
     * @param ccnId
     * @return
     * @throws Exception
     */
    public boolean deleteEPLAllList(String ccnId) throws Exception {
        ds = new DataSet();
        ds.put("CCN_NO", ccnId);
        return (Boolean) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "deleteEPLAllList", ds);
    }
    
    public String getParent4Digit(String id, String rev, String type) throws Exception {
        ds = new DataSet();
        ds.put("ID", id);
        ds.put("REV", rev);
        ds.put("TYPE", type);
        return (String) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "getParent4Digit", ds);
    }
    
    public String getParent4DigitReleased(String id, String rev, String type) throws Exception {
        ds = new DataSet();
        ds.put("ID", id);
        ds.put("REV", rev);
        ds.put("TYPE", type);
        return (String) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "getParent4DigitReleased", ds);
    }
    
    public String getPreRevisionPuid(String id, String rev) throws Exception {
        ds = new DataSet();
        ds.put("ID", id);
        ds.put("REV", rev);
        return (String) remoteQuery.execute(CCN_INFO_SERVICE_CLASS, "getPreRevisionPuid", ds);
    }
    
}

