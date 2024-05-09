package com.kgm.commands.ec.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kgm.commands.ec.eco.admincheck.common.ECOAdminCheckConstants;
import com.kgm.common.SYMCClass;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.PreferenceService;
import com.kgm.dto.ApprovalLineData;
import com.kgm.dto.SYMCECOStatusData;
import com.kgm.dto.TCEcoModel;
import com.kgm.dto.VnetTeamInfoData;
import com.kgm.dto.VnetTeamReviewData;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCECODwgData;
import com.kgm.rac.kernel.SYMCPartListData;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;

/**
 * [20160620][ymjang] ECI �� ECR ���� I/F ��� ���� (through EAI)
 * [20170613][ljg] OrderNo �ߺ� üũ �߰�
 * ECO/ECI ���� Custom SQL Query ����
 * ssangyongweb call
 * [SR170828-015][LJG]Chassis module ������ ���� ���� ���� �߰� ��û
 * @author DJKIM
 *
 */
public class CustomECODao {
    
    private SYMCRemoteUtil remoteQuery;
    private DataSet ds;
    public static final String ECO_INFO_SERVICE_CLASS = "com.kgm.service.ECOService";//com.kgm.dao.ECOInfoDao�� �ص� ��
    public static final String ECO_HISTORY_SERVICE_CLASS = "com.kgm.service.ECOHistoryService";

    public CustomECODao() {
//      this.remoteQuery = new SYMCRemoteUtil(WEB_JDBC_URL()); // TC Preference���� Web JDBC URL�� ���� ��
        this.remoteQuery = new SYMCRemoteUtil();
//      this.remoteQuery = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
    }
    
    /**
     * status,itemPuid
     * @param data
     * @return
     * @throws Exception
     */
    public boolean changeECIStatus(String status, String itemPuid) throws Exception {
        boolean result = false;
            ds = new DataSet();
            ds.put("status", status);
            ds.put("itemPuid", itemPuid);
            result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "changeECIStatus", ds);
        return result;
    }
    
    public boolean changeMECOStatus(String itemPuid, String mecoStatus) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("mecoStatus", mecoStatus);
        ds.put("mecoRevPuid", itemPuid);
            result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "changeMECOStatus", ds);
        return result;
    }
    
    
    public boolean changeECOStatus(String status, String itemPuid) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("status", status);
        ds.put("itemPuid", itemPuid);
            result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "changeECOStatus", ds);
        return result;
    }
    
    

    public boolean deleteApprovalLine(ApprovalLineData data) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("data", data);
        result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "deleteApprovalLine", ds);
    return result;
}
    
    public boolean updateEcoStatus(String ecoRevPuid, String ecoStatus, String itemStatus) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("ecoRevPuid", ecoRevPuid);
        ds.put("ecoStatus", ecoStatus);
        ds.put("itemStatus", itemStatus);
        result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "changeECOStatus", ds);
        if(!result){
            throw (new Exception("updateEcoStatus error"));
        }
        return result;
    }
    
//  public boolean updateMEcoStatus(String ecoRevPuid, String ecoStatus, String itemStatus) throws Exception {
//      boolean result = false;
//      ds = new DataSet();
//      ds.put("mecoRevPuid", ecoRevPuid);
//      ds.put("mecoStatus", ecoStatus);
//      ds.put("itemStatus", itemStatus);
//      result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "changeMECOStatus", ds);
//      if(!result){
//          throw (new Exception("updateMEcoStatus error"));
//      }
//      return result;
//  }   
    
    public String getDuplicateCategoryInVC(String ecoNo) throws Exception {
    	String sMessage = null;
    	ds = new DataSet();
    	ds.put("ecoNo", ecoNo);
    	sMessage = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getDuplicateCategoryInVC", ds);
    	return sMessage;
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> checkECOEPL(String ecoNo) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "checkECOEPL", ds);
        return resultList;
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<String> checkEndtoEnd(String ecoNo) throws Exception {
        ArrayList<String> resultList = null;
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<String>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "checkEndtoEnd", ds);
        return resultList;
    }
    
    //[SR170828-015][LJG]Chassis module ������ ���� ���� ���� �߰� ��û
  	// 1. ECO ���� Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
  	// 2. Module code : FCM or RCM
  	// 3. Part�� Option : Z999�� �����ϴ� ���
    @SuppressWarnings("unchecked")
    public ArrayList<String> checkChassisModule(String ecoNo) throws Exception {
        ArrayList<String> resultList = null;
        ds = new DataSet();
        ds.put("ECO_NO", ecoNo);
        resultList = (ArrayList<String>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "checkChassisModule", ds);
        return resultList;
    }
    
    /**
     * ECO ������ �ҷ�����
     * @param data
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<ApprovalLineData> getApprovalLine(ApprovalLineData data) throws Exception {
        ArrayList<ApprovalLineData> resultList = null;
            ds = new DataSet();
            ds.put("data", data);
            resultList = (ArrayList<ApprovalLineData>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getApprovalLine", ds);
        return resultList;
    }
    
    /**
     * ECI�� ���� ���� Ȯ��
     * @param itemPuid
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, String> getECIfileInfo(String itemPuid) throws Exception {
        HashMap<String, String> result;
        ds = new DataSet();
        ds.put("itemPuid", itemPuid);
        result = (HashMap<String, String>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getECIfileInfo", ds);
        return result;
    }
    
    /**
     * ����޴� ������Ʈ�� ����
     * @param ecoNo
     * @return
     * @throws Exception
     */
    public String getAffectedProject(String ecoNo) throws Exception {
        String resultList = null;
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getAffectedProject", ds);
        return resultList;
    }
    
    /**
     * ECI �������� I/F ���̺��� ��������
     * @param itemPuid
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<VnetTeamReviewData> getECIReviewInfo(String itemPuid) throws Exception {
        ArrayList<VnetTeamReviewData> resultList = null;
        ds = new DataSet();
        ds.put("itemPuid", itemPuid);
        resultList = (ArrayList<VnetTeamReviewData>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getECIReviewInfo", ds);
    return resultList;
    }
    
    /**
     * ECO ǰ�� �߹��� ����
     * @param prefix
     * @return
     */
    public String getNextECOSerial(String prefix) throws Exception {
        String ecoID = null;
        ds = new DataSet();
        ds.put("prefix", prefix);
        ecoID = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getNextECOSerial", ds);
        System.out.println("ecoID = "+ecoID);
        return ecoID;
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<String> getProblemItems(String ecoNo) throws Exception {
        ArrayList<String> resultList = null;
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<String>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getProblemItems", ds);
        return resultList;
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<String> getSolutionItems(String ecoNo) throws Exception {
        ArrayList<String> resultList = null;
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<String>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getSolutionItems", ds);
        return resultList;
    }

    /**
     * Vision Net���� �μ� ���� ������ ����
     * @param data
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<VnetTeamInfoData> getVnetTeamInfo(VnetTeamInfoData data) throws Exception {
        ArrayList<VnetTeamInfoData> resultList = null;
        ds = new DataSet();
        ds.put("data", data);
        resultList = (ArrayList<VnetTeamInfoData>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getVnetTeamInfo", ds);
        return resultList;
    }
    
    /**
     * Vision Net���� �μ��ڵ� ����Ʈ�� �̸�����Ʈ ������ ����
     * @param data
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<VnetTeamInfoData> getVnetTeamNames(VnetTeamInfoData data) throws Exception {
        ArrayList<VnetTeamInfoData> resultList = null;
        ds = new DataSet();
        ds.put("data", data);
        resultList = (ArrayList<VnetTeamInfoData>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getVnetTeamNames", ds);
        return resultList;
    }
    
    public boolean interfaceECI(String itemPuid) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("itemPuid", itemPuid);
        result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "interfaceECI", ds);
        return result;
    }
    
    /**
     * VNet ���� ECI/ECR �� ���� ECO ���� Update (through EAI)
     * @param ecoNo
     * @return
     * @throws Exception
     */
    public boolean interfaceECONoToVnetEAI(String ecoNo) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("ECONO", ecoNo);
        result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "interfaceECONoToVnetEAI", ds);
        if(!result){
            throw (new Exception("interfaceECONoToVnetEAI error"));
        }
        return result;
    }
    
    public boolean updateStep(String ecoNo, String step, String stepName) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("ECONO", ecoNo);
        ds.put("STEP", step);
        ds.put("STEPNAME", stepName);
        result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "updateStep", ds);
//        if(!result){
//            throw (new Exception("updateStep error"));
//        }
        return result;
    }

    /**
     * VNet ���� ECI/ECR �� ���� ECO ���� Update
     * @param ecoNo
     * @return
     * @throws Exception
     */
    public boolean interfaceECONoToVnet(String ecoNo) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("ECONO", ecoNo);
        result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "interfaceECONoToVnet", ds);
        if(!result){
            throw (new Exception("interfaceECONoToVnet error"));
        }
        return result;
    }
    
    /**
     * ����� ������ �ҷ�����
     * @param data
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<ApprovalLineData> loadApprovalLine(ApprovalLineData data) throws Exception {
        ArrayList<ApprovalLineData> resultList = null;
            ds = new DataSet();
            ds.put("data", data);
            resultList = (ArrayList<ApprovalLineData>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "loadApprovalLine", ds);
        return resultList;
    }

    /**
     * ����� ���� ������ �̸� ��� �ҷ�����
     * @param data
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<ApprovalLineData> loadSavedUserApprovalLine(ApprovalLineData data) throws Exception {
        ArrayList<ApprovalLineData> resultList = null;
        ds = new DataSet();
        ds.put("data", data);
        resultList = (ArrayList<ApprovalLineData>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "loadSavedUserApprovalLine", ds);
        return resultList;
    }
    
    public boolean makePartHistory(String ecoNo) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "makePartHistory", ds);
        if(!result){
            throw (new Exception("makePartHistory error"));
        }
        return result;
    }
    
    /**
     * SQL�� ������ Object�� ������Ʈ �� ���� �α׾ƿ� ���� refresh
     * @param itemPuid
     * @return
     * @throws Exception
     */
    public boolean refreshTCObject(String itemPuid) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("itemPuid", itemPuid);
        result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "refreshTCObject", ds);
        return result;
    }
    
    /**
     * ECO ������ ����
     * @param list
     * @throws Exception
     */
    public void removeApprovalLine(ApprovalLineData data) throws Exception {
        ds = new DataSet();
        ds.put("data", data);
        remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "removeApprovalLine", ds);
    }
    
    /**
     * ECO ������ ����
     * @param list
     * @throws Exception
     */
    public void saveApprovalLine(ArrayList<ApprovalLineData> list) throws Exception {
        ds = new DataSet();
        ds.put("data", list);
        remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "saveApprovalLine", ds);
    }
    

    /**
     * ����� ������ ����
     * @param list
     * @throws Exception
     */
    public void saveUserApprovalLine(ArrayList<ApprovalLineData> list) throws Exception {
        ds = new DataSet();
        ds.put("data", list);
        remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "saveUserApprovalLine", ds);
    }
    
    /**
     * ECO ��Ȳ ��ȸ
     * @param searchCondition
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<SYMCECOStatusData> searchEOStatus(SYMCECOStatusData searchCondition) throws Exception {
        ArrayList<SYMCECOStatusData> resultList = null;
        ds = new DataSet();
        ds.put("data", searchCondition);
        resultList = (ArrayList<SYMCECOStatusData>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchEOStatus", ds);
        return resultList;
    }
    
    /**
     * ECR ��ȸ (through EAI)
     * @param cdateFrom
     * @param cdateTo
     * @param docNo
     * @param title
     * @param cteam
     * @param cuser
     * @return
     * @throws Exception
     */
    public ArrayList<HashMap<String, String>> searchECREAI(String cdateFrom, String cdateTo, String docNo, String title, String cteam, String cuser) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        ds.put("CDATEFROM", cdateFrom);
        ds.put("CDATETO", cdateTo);
        ds.put("DOCNO", docNo);
        ds.put("TITLE", title);
        ds.put("CTEAM", cteam);
        ds.put("CUSER", cuser);
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchECREAI", ds);
        return resultList;
    }
    
    /**
     * ECR ��ȸ
     * @param cdateFrom
     * @param cdateTo
     * @param docNo
     * @param title
     * @param cteam
     * @param cuser
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> searchECR(String cdateFrom, String cdateTo, String docNo, String title, String cteam, String cuser) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        ds.put("CDATEFROM", cdateFrom);
        ds.put("CDATETO", cdateTo);
        ds.put("DOCNO", docNo);
        ds.put("TITLE", title);
        ds.put("CTEAM", cteam);
        ds.put("CUSER", cuser);
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchECR", ds);
        return resultList;
    }
    
    /**
     * ECO-B�� ���� �ҷ�����
     * @param ecoNo
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<SYMCECODwgData> selectECODwgList(String ecoNo) throws Exception {
        ArrayList<SYMCECODwgData> resultList = null;
        DataSet ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<SYMCECODwgData>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectECODwgList", ds);
        return resultList;
    }
    
    /**
     * ECO-D�� ���� �ҷ�����
     * @param ecoNo
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<SYMCPartListData> selectECOPartList(String ecoNo) throws Exception {
        ArrayList<SYMCPartListData> resultList = null;
        DataSet ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<SYMCPartListData>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectECOPartList", ds);
        return resultList;
    }
    
    
    
    /**
     * ECO-C�� ���� �ҷ�����
     * @param ecoNo
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<SYMCBOMEditData> selectECOEplList(String ecoNo) throws Exception {
        ArrayList<SYMCBOMEditData> resultList = null;
        DataSet ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<SYMCBOMEditData>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectECOEplList", ds);
        return resultList;
    }
    
    /**
     * SYMC ��Ʈ����� ���� ���� �߼�
     * ���� ���ν���  CALS.MAILSEND@TOVNET
     * ���� �ּҰ� �ƴ� ����� �Է�
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
        
        if(fromUser == null || fromUser.equals(""))
            ds.put("the_sabun", "NPLM");
        else
            ds.put("the_sabun", fromUser);
        
        ds.put("the_title", title);
        ds.put("the_remark", body);
        ds.put("the_tsabun", toUsers);
        return (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "sendMail", ds);
    }
    
    public boolean updateFileName(String itemPuid) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("itemPuid", itemPuid);
        result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "updateFileName", ds);
        return result;
    }
    
    /**
     * [20160620][ymjang] ECI �� ECR ���� I/F ��� ���� (through EAI)
     * @param ecoNo
     * @return
     * @throws Exception
     */
    public TCEcoModel getEcoInfoEAI(String ecoNo) throws Exception {
        TCEcoModel ecoModel = null;
        DataSet ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        //2023-10 �������� �ϵ� �ڵ��� �׷���� Preference�� ���� 
        ds.put("rndManagement", PreferenceService.getValue("RnD MANAGEMENT"));
        ds.put("engineeringCost", PreferenceService.getValue("ENGINEERING COST"));
        ecoModel = (TCEcoModel) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getEcoInfoEAI", ds);
        return ecoModel;
    }

    public TCEcoModel getEcoInfo(String ecoNo) throws Exception {
        TCEcoModel ecoModel = null;
        DataSet ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        // 2023-10 �������� �ϵ� �ڵ��� �׷���� Preference�� ����
        ds.put("rndManagement", PreferenceService.getValue("RnD MANAGEMENT"));
        ds.put("engineeringCost", PreferenceService.getValue("ENGINEERING COST"));
        ecoModel = (TCEcoModel) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getEcoInfo", ds);
        return ecoModel;
    }
    
    /**
     * ECO ���� ���� ����
     * @param ecoNo
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> getEcoWorkflowInfo(String ecoNo) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getEcoWorkflowInfo", ds);
        return resultList;
    }
    
    /**
     * FIXED 2013.06.01, DJKIM, �ش� ������ ���� ������ ���� ���� �ʴ� ���
     * @param bvrPuid
     * @return
     * @throws Exception
     */
    public String childrenCount(String bvrPuid) throws Exception {
        String childrenCount = "1";
        ds = new DataSet();
        ds.put("bvrPuid", bvrPuid);
        childrenCount = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "childrenCount", ds);
        return childrenCount;
    }
    
    /**
     * FIXED 2013.06.01, DJKIM, ECO NO�� Workflow�� ���� �Ǿ����� Ȯ��
     * @param ecoNo
     * @return
     * @throws Exception
     */
    public String workflowCount(String ecoNo)  throws Exception {
        String workflowCount = "0";
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        workflowCount = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "workflowCount", ds);
        return workflowCount;
    }
    
    /**
     * FIXED 2013.06.01, DJKIM, ECO NO�� EcoRevision Puid �˻�
     * @param ecoNo
     * @return
     * @throws Exception
     */
    public String getEcoRevisionPuid(String ecoNo)  throws Exception {
        String ecoRevisionPuid = "";
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        ecoRevisionPuid = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getEcoRevisionPuid", ds);
        return ecoRevisionPuid;
    }
    
    public String getNMCDUpdatePartList(DataSet ds)  throws Exception {
        String partList = "";
        partList = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getNMCDUpdatePartList", ds);
        return partList;
    }
    
    public ArrayList<HashMap<String,String>> getProjectCodeList(DataSet ds)  throws Exception {
    	ArrayList<HashMap<String, String>> resultList = null;
    	resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getProjectCodeList", ds);
        return resultList;
    }
    
    public String getAdmin(DataSet ds)  throws Exception {
        String str = "";
        str = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getAdmin", ds);
        return str;
    }

    /** 
     * TC Preference���� Web JDBC URL�� ���� ��
     * @return
     */
    public static final String WEB_JDBC_URL(){
        TCSession session = CustomUtil.getTCSession();
        TCPreferenceService preferenceService = session.getPreferenceService();

        //String webJDBCURL = preferenceService.getString(TCPreferenceService.TC_preference_all, SYMCClass.SYMC_WEBJDBC_URL);
        String webJDBCURL = preferenceService.getStringValue(SYMCClass.SYMC_WEBJDBC_URL);
        
        return webJDBCURL;
//      return "http://127.0.0.1:8080";//FIXME
    }
    
    @SuppressWarnings({ "unused", "unchecked" })
    public ArrayList<HashMap<String,String>> searchUserOnVnet(String team, String hname) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        ds.put("team", team);
        ds.put("hname", hname);
        return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchUserOnVnet", ds);
    }
    
    /**
     * ECI ���� ��ȸ. (through EAI)
     * @param sECINo
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public HashMap<String,String> searchECIEAI(String sECINo) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        ds.put("ECINO", sECINo);
        resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchECIEAI", ds);
        
        if (resultList.size() > 1 || resultList.size() == 0) {
            throw new Exception("Can't Not Find ECI From Vision NET.");
        } else {
            return resultList.get(0);
        }
    }

    /**
     * [SR140701-022][20140902] jclee �߰�
     * ECI ���� ��ȸ. (From Vision Net)
     * @param ds
     * @return
     */
    @SuppressWarnings("unchecked")
    public HashMap<String,String> searchECI(String sECINo) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        ds.put("ECINO", sECINo);
        resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchECI", ds);
        
        if (resultList.size() > 1 || resultList.size() == 0) {
            throw new Exception("Can't Not Find ECI From Vision NET.");
        } else {
            return resultList.get(0);
        }
    }
    
    /**
     * ECI ���� ��ȸ. (through EAI)
     * @param ds
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "unused", "unchecked" })
    public ArrayList<HashMap<String, String>> searchECIEAI(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchECIEAI", ds);
    }
    
    /**
     * [SR140701-022][20140902] jclee �߰�
     * ECI ���� ��ȸ. (From Vision Net)
     * @param ds
     * @return
     */
    @SuppressWarnings({ "unused", "unchecked" })
    public ArrayList<HashMap<String, String>> searchECI(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchECI", ds);
    }
    
	/**
	 * [SR141120-043][2014.11.21][jclee] Color ID�� �����ϸ鼭 Color Section No�� �������� �ʴ� �׸� ����Ʈ ��ȸ
	 * @param ds
	 * @return
	 */
    @SuppressWarnings({ "unused", "unchecked" })
	public ArrayList<HashMap<String, String>> getColorIDWarningList(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getColorIDWarningList", ds);
	}
    
    /**
	 * [20180718][CSH]End Item���� 500�� �ʰ��� HBOM(�̱��� ����)�� Mail�뺸
	 * @param ds
	 * @return
	 */
    @SuppressWarnings({ "unused", "unchecked" })
	public String getEcoEndItemCount(DataSet ds) throws Exception {
		String result = "";
		return result = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getEcoEndItemCount", ds);
	}
	
	/**
	 * [SR141205-027][2014.12.16][jclee] Color ID�� ����� �׸� ����Ʈ ��ȸ
	 * @param ds
	 * @return
	 */
    @SuppressWarnings({ "unused", "unchecked" })
	public ArrayList<HashMap<String, String>> getColorIDChangingList(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getColorIDChangingList", ds);
	}

	
	/**
	 * IN ECO, OUT ECO ����
	 */
	@SuppressWarnings("unchecked")
	public boolean makeBOMHistoryMaster(String ecoNo) throws Exception {
		if (ecoNo == null || ecoNo.equals("") || ecoNo.length() == 0) {
			throw new Exception("ECO No will be not to be Null when Make BOM History!\nContact to Administrator.");
		}
//		boolean result = false;
		ds = new DataSet();
		ds.put("ECO_NO", ecoNo);
		remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "makeBOMHistoryMaster", ds);
//		result = (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "makeBOMHistoryMaster", ds);
//		if(!result){
//			throw (new Exception("makeBOMHistoryMaster error"));
//		}
//		return result;
		return true;
	}
	
	/**
	 * [SR150213-010][2015.02.25][jclee] EPL���� Ư�� FMP ���� 1Lv Part �� Supply Mode�� P�� �����ϴ� EPL�� Car Project�� �����ϴ��� ��ȸ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> getCarProjectInEPL(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getCarProjectInEPL", ds);
	}
	
	/**
	 * ECO No�� ���� �����鼭 EPL�� New Part No �� ���ԵǾ� ���� �ʴ� Part ��� ��ȯ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> getCANNOTGeneratedList(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getCANNOTGeneratedList", ds);
	}
	
	/**
	 * EPL Cut �� Revise�Ͽ� �ٽ� Paste�� ��� Ȯ�� (Revise �̷� ����)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> getCANNOTGeneratedReviseList(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getCANNOTGeneratedReviseList", ds);
	}
	
	/**
	 * ECO ���� ��Ȳ ��ȸ
	 * @param searchCondition
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<SYMCECOStatusData> searchECOCorrectionHistory(SYMCECOStatusData searchCondition) throws Exception {
		ArrayList<SYMCECOStatusData> resultList = null;
		ds = new DataSet();
		ds.put("data", searchCondition);
		resultList = (ArrayList<SYMCECOStatusData>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchECOCorrectionHistory", ds);
		return resultList;
	}
	
	/**
	 * ECO EPL ���� ��Ȳ ��ȸ(��)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> selectECOEplCorrectionList(String sECONo) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		DataSet ds = new DataSet();
		ds.setString("ecoNo", sECONo);
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectECOEplCorrectionList", ds);
	}
	
	/**
	 * ECO EPL Carry Over Part�� S/Mode �� ���
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> selectECOEplCOSModeCompareList(String sECONo) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		DataSet ds = new DataSet();
		ds.setString("ecoNo", sECONo);
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectECOEplCOSModeCompareList", ds);
	}
	
	/**
	 * ECO EPL �� Generate���� ���� Carry Over Part ����Ʈ ��ȸ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> selectUnGeneratedCOPartList(String sECONo) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		DataSet ds = new DataSet();
		ds.setString("ecoNo", sECONo);
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectUnGeneratedCOPartList", ds);
	}

	/**
	 * ECO Admin Check ������� ��ȸ
	 * @param sECONo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> selectECOChangeCause(String sECONo) throws Exception {
		DataSet ds = new DataSet();
		ds.setString(ECOAdminCheckConstants.PROP_ECO_NO, sECONo);
		return (ArrayList<HashMap<String, Object>>)remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectECOChangeCause", ds);
	}

	/**
	 * ECO Admin Check ������� �߰�
	 * @param hmEndItemList
	 * @return
	 * @throws Exception
	 */
    public boolean insertECOChangeCause(HashMap<String, Object> hmEndItemList) throws Exception {
        ds = new DataSet();
        ds.put(ECOAdminCheckConstants.PROP_ECO_NO, hmEndItemList.get(ECOAdminCheckConstants.PROP_ECO_NO));
        ds.put(ECOAdminCheckConstants.PROP_PROJECT_CODE, hmEndItemList.get(ECOAdminCheckConstants.PROP_PROJECT_CODE));
        ds.put(ECOAdminCheckConstants.PROP_SEQ_NO, hmEndItemList.get(ECOAdminCheckConstants.PROP_SEQ_NO));
        ds.put(ECOAdminCheckConstants.PROP_CHANGE_CAUSE, hmEndItemList.get(ECOAdminCheckConstants.PROP_CHANGE_CAUSE));
        ds.put(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_A, hmEndItemList.get(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_A));
        ds.put(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_M, hmEndItemList.get(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_M));
        
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "insertECOChangeCause", ds);
        return result;
    }
    
    /**
     * ECO Admin Check ������� ����
     * @param sECONo
     * @return
     * @throws Exception
     */
    public boolean deleteECOChangeCause(String sECONo) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put(ECOAdminCheckConstants.PROP_ECO_NO, sECONo);
        
        result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "deleteECOChangeCause", ds);
        return result;
    }
	
	/**
	 * ECO EPL�� End Item List ��ȸ
	 * @param sECONo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> selectECOEplEndItemList(String sECONo) throws Exception {
		DataSet ds = new DataSet();
		ds.setString(ECOAdminCheckConstants.PROP_ECO_NO, sECONo);
		return (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectECOEplEndItemList", ds);
	}

	/**
	 * ECO EPL�� End Item Name List ��ȸ
	 * @param sECONo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> selectECOEplEndItemNameList(String sECONo) throws Exception {
		DataSet ds = new DataSet();
		ds.setString(ECOAdminCheckConstants.PROP_ECO_NO, sECONo);
		return (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectECOEplEndItemNameList", ds);
	}
	
	/**
	 * ECO BOM List �� End Item Name List ��ȸ
	 * @param sECONo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> selectECOBOMListEndItemNameList(String sECONo) throws Exception {
		DataSet ds = new DataSet();
		ds.setString(ECOAdminCheckConstants.PROP_ECO_NO, sECONo);
		return (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "selectECOBOMListEndItemNameList", ds);
	}

	/**
	 * ECO EPL End Item List �߰�
	 * @param hmEndItemList
	 * @return
	 * @throws Exception
	 */
    public boolean insertECOEplEndItemList(HashMap<String, Object> hmEndItemList) throws Exception {
        ds = new DataSet();
        ds.put(ECOAdminCheckConstants.PROP_ECO_NO, hmEndItemList.get(ECOAdminCheckConstants.PROP_ECO_NO));
        ds.put(ECOAdminCheckConstants.PROP_PART_NAME, hmEndItemList.get(ECOAdminCheckConstants.PROP_PART_NAME));
        ds.put(ECOAdminCheckConstants.PROP_CT, hmEndItemList.get(ECOAdminCheckConstants.PROP_CT));
        ds.put(ECOAdminCheckConstants.PROP_SMODE, hmEndItemList.get(ECOAdminCheckConstants.PROP_SMODE));
        ds.put(ECOAdminCheckConstants.PROP_CHANGE_CAUSE, hmEndItemList.get(ECOAdminCheckConstants.PROP_CHANGE_CAUSE));
        ds.put(ECOAdminCheckConstants.PROP_EDITABLE, hmEndItemList.get(ECOAdminCheckConstants.PROP_EDITABLE));
        
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "insertECOEplEndItemList", ds);
        return result;
    }
    
    /**
     * ECO EPL End Item List ����
     * @param sECONo
     * @return
     * @throws Exception
     */
    public boolean deleteECOEplEndItemList(String sECONo) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put(ECOAdminCheckConstants.PROP_ECO_NO, sECONo);
        
        result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "deleteECOEplEndItemList", ds);
        return result;
    }
    
    /**
     * Rev �Ӽ��� Change Description ��ȯ
     * @param sPartNo
     * @param sPartRevNo
     * @return
     * @throws Exception
     */
    public String getChangeDescription(String sPartNo, String sPartRevNo) throws Exception {
    	String sMessage = null;
    	ds = new DataSet();
    	ds.put("partNo", sPartNo);
    	ds.put("partRevNo", sPartRevNo);
    	
    	sMessage = (String) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getChangeDescription", ds);
    	return sMessage;
    }
    
    /**
     * get ECO Admin Check Common Memo
     * @param sPartNo
     * @param sPartRevNo
     * @return
     * @throws Exception
     */
    public String getECOAdminCheckCommonMemo() throws Exception {
    	return (String) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getECOAdminCheckCommonMemo", null);
    }

	/**
	 * insert ECO Admin Check Common Memo
	 * @param hmEndItemList
	 * @return
	 * @throws Exception
	 */
    public boolean insertECOAdminCheckCommonMemo(String sMemo) throws Exception {
        ds = new DataSet();
        ds.put(ECOAdminCheckConstants.PROP_COMMON_MEMO, sMemo);
        
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "insertECOAdminCheckCommonMemo", ds);
        return result;
    }
    
	/**
	 * Part �� �������� ECO ����Ʈ�� Ȯ����. �������̸� ���� �޼����� ������
	 * @param ecoNo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getRefEcoFromPartList(String  ecoNo) throws Exception {
		DataSet ds = new DataSet();
		ds.setString("ecoNo", ecoNo);
		return (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getRefEcoFromPartList", ds);
	}
	
    /**
     * �ߺ��� ECO ���� Line ����Ʈ�� ������
     * @param ecoNo
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, Object>> getEcoDupApprovalLines(String ecoNo) throws Exception {
        ArrayList<HashMap<String, Object>> resultList = null;
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getEcoDupApprovalLines", ds);
        return resultList;
    }
    
    /**
     * ���� Revision �� �߸��� Part ����Ʈ�� ������
     * @param ecoNo
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> getOldRevNotMatchedParts(String ecoNo) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getOldRevNotMatchedParts", ds);
        return resultList;
    }
    
    /**
     * ������Ȳ ���渮��Ʈ �� Option Category �� ������
     * @param gmodelNo gmodel No
     * @param projectNo project No
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getEcoStatusOptCategoryList(String gmodelNo, String projectNo) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        DataSet ds = new DataSet();
        ds.put("GMODEL_NO", gmodelNo);
        ds.put("PROJECT_NO", projectNo);
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getEcoStatusOptCategoryList", ds);
        return resultList;
    }
    
    
    /**
     * ������Ȳ ���渮��Ʈ ��ȸ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getEcoStatusChangeList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        //2023-10 �������� �ϵ� �ڵ��� �׷���� Preference�� ���� 
        ds.put("powerControlDevelopment", PreferenceService.getValue("POWER CONTROL DEVELOPMENT"));
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getEcoStatusChangeList", ds);
        return resultList;
    }
	
	/**
     * System Code Null Value ����Ʈ ��ȸ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getEcoNullValueList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getEcoNullValueList", ds);
        return resultList;
    }
    
	/**
	 * SYS GUI�� ������
	 * @param ecoNo
	 * @return
	 * @throws Exception
	 */
    public String getSysGuid() throws Exception {
    	String sMessage = null;
    	ds = new DataSet();
    	ds.put("NO-PARAM", null);
    	sMessage = (String) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getSysGuid", ds);
    	return sMessage;
    }
    
    
    /**
     * ������Ȳ ���渮��Ʈ ��ȸ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<String> getMultiSysGuidList(ArrayList<String> tableNameList) throws Exception {
		ArrayList<String>resultList = null;
        ds = new DataSet();
        ds.put("tableList", tableNameList);
        resultList = (ArrayList<String>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getMultiSysGuidList", ds);
        return resultList;
    }
	
	/**
	 * ���躯�� ��Ȳ �������� ����Ʈ ����
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
    public boolean insertRptStdInfo(List<HashMap<String, String>> dataList) throws Exception {
        ds = new DataSet();
        ds.put("DATA_LIST", dataList);
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "insertRptStdInfo", ds);
        return result;
    }
    
	/**
	 * ���躯�� ��Ȳ ���� ����Ʈ ����
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
    public boolean insertRptChgReview(List<HashMap<String, String>> dataList) throws Exception {
        ds = new DataSet();
        ds.put("DATA_LIST", dataList);
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "insertRptChgReview", ds);
        return result;
    }
    
    
	/**
	 * ���躯�� ��Ȳ �������� ����Ʈ ����
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
    public boolean insertRptList(List<HashMap<String, String>> dataList) throws Exception {
        ds = new DataSet();
        ds.put("DATA_LIST", dataList);
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "insertRptList", ds);
        return result;
    }
    
    
    /**
     * ������Ȳ ���渮��Ʈ ��ȸ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<String> getDupRptInfoList(DataSet ds) throws Exception {
		ArrayList<String> resultList = null;
        resultList = (ArrayList<String>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getDupRptInfoList", ds);
        return resultList;
    }
	
	/**
	 * ������Ȳ ���� ����
	 * @param masterPuid
	 * @return
	 * @throws Exception
	 */
    public boolean deleteRptChangeList(String masterPuid) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("MASTER_PUID", masterPuid);
        
        result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "deleteRptChangeList", ds);
        return result;
    }
    
    /**
     * ���� ��Ȳ ��ü����Ʈ ��ȸ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> getEcoTotalStatusList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, Object>> resultList = null;
        resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getEcoTotalStatusList", ds);
        return resultList;
    }
	
    /**
     * ���� ��Ȳ ������������Ʈ ��ȸ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> getEcoStatusStdList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, Object>> resultList = null;
        resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getEcoStatusStdList", ds);
        return resultList;
    }
	
	/**
	 * ���躯�� ��Ȳ ���� ����Ʈ ����
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
    public boolean updateRptList(List<HashMap<String, String>> dataList) throws Exception {
        ds = new DataSet();
        ds.put("DATA_LIST", dataList);
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "updateRptList", ds);
        return result;
    }
	
	/**
	 * ���躯�� ��Ȳ ���� ����Ʈ ROW ����
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
    public boolean deleteRpListWithPuid(List<HashMap<String, String>> dataList) throws Exception {
        ds = new DataSet();
        ds.put("DATA_LIST", dataList);
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "deleteRpListWithPuid", ds);
        return result;
    }
    
	/**
	 * ���躯�� ��Ȳ ���� ����  Row ����
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
    public boolean deleteChgReviewWithPuid(List<HashMap<String, String>> dataList) throws Exception {
        ds = new DataSet();
        ds.put("DATA_LIST", dataList);
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "deleteChgReviewWithPuid", ds);
        return result;
    }
    
    
    /**
	 * Project �� G Model �ڵ带 ������
	 * @param projectNo
	 * @return
	 * @throws Exception
	 */
    public String getGmodelWithProject(String projectNo) throws Exception {
    	String sMessage = null;
    	ds = new DataSet();
    	ds.put("PROJECT_NO", projectNo);
    	sMessage = (String) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getGmodelWithProject", ds);
    	return sMessage;
    }
    
    /**
     * ���躯�� ��Ȳ ���� ����Ʈ ����
     * @param ds
     * @return
     * @throws Exception
     */
    public boolean updateRptStdInfo(DataSet ds) throws Exception {
        boolean result = false;
        result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "updateRptStdInfo", ds);
        if(!result){
            throw (new Exception("updateRptStdInfo error"));
        }
        return result;
    }
    
    /**
     * OSPEC Revision  ����Ʈ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> getOspecRevList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, Object>> resultList = null;
        resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getOspecRevList", ds);
        return resultList;
    }
	
	/**
	 * Product�� �ش��ϴ� EPL Puid�� ������
	 * @param projectNo
	 * @return
	 * @throws Exception
	 */
    public String getEPLJobPuid(String projectNo) throws Exception {
    	String sMessage = null;
    	ds = new DataSet();
    	ds.put("PROJECT_NO", projectNo);
    	sMessage = (String) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getEPLJobPuid", ds);
    	return sMessage;
    }
    
    /**
     * ���躯����Ȳ ��� ��� �������� ����Ʈ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getChangeTargetEPLList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getChangeTargetEPLList", ds);
        return resultList;
    }
	
	
    /**
     * ���躯����Ȳ EPL ����Ʈ(�ߺ��� �� ����)
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getAllChangeTargetEPLList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getAllChangeTargetEPLList", ds);
        return resultList;
    }
	
	/**
	 * ���躯����Ȳ �߰����� �ɼ� ����
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
    public boolean insertRptOptCondition(List<HashMap<String, String>> dataList) throws Exception {
        ds = new DataSet();
        ds.put("DATA_LIST", dataList);
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "insertRptOptCondition", ds);
        return result;
    }
    
    
    /**
     * ECO ��Ȳ ������� Category ���� ����Ʈ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getRptChgReviewCategory(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getRptChgReviewCategory", ds);
        return resultList;
    }
    
    /**
     * ECO ��Ȳ �߰����� �ɼ� ����Ʈ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getRptOptCondition(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getRptOptCondition", ds);
        return resultList;
    }
    
    /**
     * ���躯�� ��Ȳ ���� ���� Count
     * @param masterPuid
     * @return
     * @throws Exception
     */
    public int getRptReviewCount(String masterPuid) throws Exception {
    	  int result = 0;
          ds = new DataSet();
          ds.put("MASTER_PUID", masterPuid);
          
          result = (Integer) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getRptReviewCount", ds);
          return result;
    }
    
	/**
	 * ������Ȳ ���� ����
	 * @param masterPuid
	 * @return
	 * @throws Exception
	 */
    public boolean deleteRptReviewList(String masterPuid) throws Exception {
        boolean result = false;
        ds = new DataSet();
        ds.put("MASTER_PUID", masterPuid);
        
        result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "deleteRptReviewList", ds);
        return result;
    }
    
    /**
     * ���躯�� ��Ȳ �������� With Master PUID
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getRptStdInformWithPuid(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getRptStdInformWithPuid", ds);
        return resultList;
    }
	
	/**
	 * Function EPL Check ���
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
    public boolean insertFncEplCheck(List<HashMap<String, String>> dataList) throws Exception {
        ds = new DataSet();
        ds.put("DATA_LIST", dataList);
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "insertFncEplCheck", ds);
        return result;
    }
    
    /**
     * Function EPL Check ����Ʈ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> getFncEplCheckList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, Object>> resultList = null;
        resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getFncEplCheckList", ds);
        return resultList;
    }
	
	
	/**
	 * Function EPL Check ����Ʈ ����
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
    public boolean deleteFncEpl(List<HashMap<String, String>> dataList) throws Exception {
        ds = new DataSet();
        ds.put("DATA_LIST", dataList);
        boolean result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "deleteFncEpl", ds);
        return result;
    }
    
    /**
     * Function EPL Check  ����
     * @param ds
     * @return
     * @throws Exception
     */
    public boolean updateFncEplCheck(DataSet ds) throws Exception {
        boolean result = false;
        result = (Boolean) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "updateFncEplCheck", ds);
        if(!result){
            throw (new Exception("updateFncEplCheck error"));
        }
        return result;
    }
    
    /**
     * Function EPL Check ��Ȳ
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> getFncEplCheckStatusList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, Object>> resultList = null;
        resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getFncEplCheckStatusList", ds);
        return resultList;
    }
	
	/**
	 * ECO�� EPL�� �������� ����
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 6. 13.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getECOEPL(DataSet ds) throws Exception {
        ArrayList<String> resultList = null;
        resultList = (ArrayList<String>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getECOEPL", ds);
        return resultList;
    }
	
	/**
	 * Order No �ߺ� üũ �߰�
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 6. 13.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, Object>> duplicateOrderNoCheck(DataSet ds) throws Exception {
		ArrayList<HashMap<String, Object>> resultList = null;
        resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "duplicateOrderNoCheck", ds);
        return resultList;
    }
	
	public ArrayList<HashMap<String, Object>> notConnectedFunctionList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, Object>> resultList = null;
        resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "notConnectedFunctionList", ds);
        return resultList;
    }
	/*
	 * [20230406][CF-3876] ��������� �̺��� å��, ������ å�� ��û
	 * #1. Vehicle ECO�� �Ŀ�Ʈ���� ������Ʈ�� ���� ���� üũ 
	 * #2. Vehicle ECO(���� ECO)�� EPL Proj�Ӽ��� �Ŀ�Ʈ���� ������Ʈ ����� ���� ó�� 
	 * #3. �Ŀ�Ʈ���� ������Ʈ�� Power Traing ECO(���� ECO)�� �۾� �ؾ��Ѵ�.
	 * ECO ���� ��û �� ���� ECO��ȣ�� ä���ϰ� �����̳� �̼� ��Ʈ�� �߰��� ��� error �� ����.
	 */ 
    public ArrayList<HashMap<String, String>> checkPowerTraing(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "checkPowerTraing", ds);
        return resultList;
    }

    
    public ArrayList<String> getYear() throws Exception {
        ArrayList<String> resultList = null;
        ds = new DataSet();
        resultList = (ArrayList<String>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getYear", ds);
        return resultList;
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> getPlantList() throws Exception {
    	ArrayList<HashMap<String, String>> resultList = null;
        ds = new DataSet();
        resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getPlantList", ds);
        
        return resultList;
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> getAnnualReport(DataSet ds) throws Exception {
    	ArrayList<HashMap<String, String>> resultList = null;
        resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getAnnualReport", ds);
        
        return resultList;
    }
}
