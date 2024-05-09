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
 * [20160620][ymjang] ECI 및 ECR 정보 I/F 방식 개선 (through EAI)
 * [20170613][ljg] OrderNo 중복 체크 추가
 * ECO/ECI 관련 Custom SQL Query 모음
 * ssangyongweb call
 * [SR170828-015][LJG]Chassis module 관리를 위한 검증 조건 추가 요청
 * @author DJKIM
 *
 */
public class CustomECODao {
    
    private SYMCRemoteUtil remoteQuery;
    private DataSet ds;
    public static final String ECO_INFO_SERVICE_CLASS = "com.kgm.service.ECOService";//com.kgm.dao.ECOInfoDao로 해도 됨
    public static final String ECO_HISTORY_SERVICE_CLASS = "com.kgm.service.ECOHistoryService";

    public CustomECODao() {
//      this.remoteQuery = new SYMCRemoteUtil(WEB_JDBC_URL()); // TC Preference에서 Web JDBC URL을 가져 옴
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
    
    //[SR170828-015][LJG]Chassis module 관리를 위한 검증 조건 추가 요청
  	// 1. ECO 내의 Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
  	// 2. Module code : FCM or RCM
  	// 3. Part의 Option : Z999을 포함하는 경우
    @SuppressWarnings("unchecked")
    public ArrayList<String> checkChassisModule(String ecoNo) throws Exception {
        ArrayList<String> resultList = null;
        ds = new DataSet();
        ds.put("ECO_NO", ecoNo);
        resultList = (ArrayList<String>) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "checkChassisModule", ds);
        return resultList;
    }
    
    /**
     * ECO 배포선 불러오기
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
     * ECI의 파일 정보 확인
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
     * 영향받는 프로젝트를 구함
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
     * ECI 검토결과를 I/F 테이블에서 가져오기
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
     * ECO 품번 발번용 쿼리
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
     * Vision Net에서 부서 정보 가지고 오기
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
     * Vision Net에서 부서코드 리스트로 이름리스트 가지고 오기
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
     * VNet 으로 ECI/ECR 에 대한 ECO 정보 Update (through EAI)
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
     * VNet 으로 ECI/ECR 에 대한 ECO 정보 Update
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
     * 사용자 배포선 불러오기
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
     * 사용자 저장 배포선 이름 모두 불러오기
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
     * SQL로 팀센터 Object를 업데이트 후 세션 로그아웃 없이 refresh
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
     * ECO 배포선 삭제
     * @param list
     * @throws Exception
     */
    public void removeApprovalLine(ApprovalLineData data) throws Exception {
        ds = new DataSet();
        ds.put("data", data);
        remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "removeApprovalLine", ds);
    }
    
    /**
     * ECO 배포선 저장
     * @param list
     * @throws Exception
     */
    public void saveApprovalLine(ArrayList<ApprovalLineData> list) throws Exception {
        ds = new DataSet();
        ds.put("data", list);
        remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "saveApprovalLine", ds);
    }
    

    /**
     * 사용자 배포선 저장
     * @param list
     * @throws Exception
     */
    public void saveUserApprovalLine(ArrayList<ApprovalLineData> list) throws Exception {
        ds = new DataSet();
        ds.put("data", list);
        remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "saveUserApprovalLine", ds);
    }
    
    /**
     * ECO 현황 조회
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
     * ECR 조회 (through EAI)
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
     * ECR 조회
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
     * ECO-B지 정보 불러오기
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
     * ECO-D지 정보 불러오기
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
     * ECO-C지 정보 불러오기
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
     * SYMC 인트라넷을 통한 메일 발송
     * 관련 프로시져  CALS.MAILSEND@TOVNET
     * 메일 주소가 아닌 사번을 입력
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
     * [20160620][ymjang] ECI 및 ECR 정보 I/F 방식 개선 (through EAI)
     * @param ecoNo
     * @return
     * @throws Exception
     */
    public TCEcoModel getEcoInfoEAI(String ecoNo) throws Exception {
        TCEcoModel ecoModel = null;
        DataSet ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        //2023-10 조직변경 하드 코딩된 그룹명을 Preference로 변경 
        ds.put("rndManagement", PreferenceService.getValue("RnD MANAGEMENT"));
        ds.put("engineeringCost", PreferenceService.getValue("ENGINEERING COST"));
        ecoModel = (TCEcoModel) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getEcoInfoEAI", ds);
        return ecoModel;
    }

    public TCEcoModel getEcoInfo(String ecoNo) throws Exception {
        TCEcoModel ecoModel = null;
        DataSet ds = new DataSet();
        ds.put("ecoNo", ecoNo);
        // 2023-10 조직변경 하드 코딩된 그룹명을 Preference로 변경
        ds.put("rndManagement", PreferenceService.getValue("RnD MANAGEMENT"));
        ds.put("engineeringCost", PreferenceService.getValue("ENGINEERING COST"));
        ecoModel = (TCEcoModel) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getEcoInfo", ds);
        return ecoModel;
    }
    
    /**
     * ECO 결재 정보 쿼리
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
     * FIXED 2013.06.01, DJKIM, 해당 리비전 하위 구조가 존재 하지 않는 경우
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
     * FIXED 2013.06.01, DJKIM, ECO NO로 Workflow가 생성 되었는지 확인
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
     * FIXED 2013.06.01, DJKIM, ECO NO로 EcoRevision Puid 검색
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
     * TC Preference에서 Web JDBC URL을 가져 옴
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
     * ECI 정보 조회. (through EAI)
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
     * [SR140701-022][20140902] jclee 추가
     * ECI 정보 조회. (From Vision Net)
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
     * ECI 정보 조회. (through EAI)
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
     * [SR140701-022][20140902] jclee 추가
     * ECI 정보 조회. (From Vision Net)
     * @param ds
     * @return
     */
    @SuppressWarnings({ "unused", "unchecked" })
    public ArrayList<HashMap<String, String>> searchECI(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "searchECI", ds);
    }
    
	/**
	 * [SR141120-043][2014.11.21][jclee] Color ID가 존재하면서 Color Section No가 존재하지 않는 항목 리스트 조회
	 * @param ds
	 * @return
	 */
    @SuppressWarnings({ "unused", "unchecked" })
	public ArrayList<HashMap<String, String>> getColorIDWarningList(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getColorIDWarningList", ds);
	}
    
    /**
	 * [20180718][CSH]End Item수가 500개 초과시 HBOM(이광석 차장)에 Mail통보
	 * @param ds
	 * @return
	 */
    @SuppressWarnings({ "unused", "unchecked" })
	public String getEcoEndItemCount(DataSet ds) throws Exception {
		String result = "";
		return result = (String) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getEcoEndItemCount", ds);
	}
	
	/**
	 * [SR141205-027][2014.12.16][jclee] Color ID가 변경된 항목 리스트 조회
	 * @param ds
	 * @return
	 */
    @SuppressWarnings({ "unused", "unchecked" })
	public ArrayList<HashMap<String, String>> getColorIDChangingList(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getColorIDChangingList", ds);
	}

	
	/**
	 * IN ECO, OUT ECO 생성
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
	 * [SR150213-010][2015.02.25][jclee] EPL에서 특정 FMP 하위 1Lv Part 중 Supply Mode에 P를 포함하는 EPL이 Car Project를 포함하는지 조회
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> getCarProjectInEPL(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getCarProjectInEPL", ds);
	}
	
	/**
	 * ECO No를 갖고 있으면서 EPL의 New Part No 에 포함되어 있지 않는 Part 목록 반환
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> getCANNOTGeneratedList(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getCANNOTGeneratedList", ds);
	}
	
	/**
	 * EPL Cut 후 Revise하여 다시 Paste한 경우 확인 (Revise 이력 누락)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> getCANNOTGeneratedReviseList(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		return resultList = (ArrayList<HashMap<String, String>>)remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "getCANNOTGeneratedReviseList", ds);
	}
	
	/**
	 * ECO 보정 현황 조회
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
	 * ECO EPL 보정 현황 조회(상세)
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
	 * ECO EPL Carry Over Part의 S/Mode 비교 결과
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
	 * ECO EPL 내 Generate되지 않은 Carry Over Part 리스트 조회
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
	 * ECO Admin Check 변경사유 조회
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
	 * ECO Admin Check 변경사유 추가
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
     * ECO Admin Check 변경사유 삭제
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
	 * ECO EPL내 End Item List 조회
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
	 * ECO EPL내 End Item Name List 조회
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
	 * ECO BOM List 내 End Item Name List 조회
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
	 * ECO EPL End Item List 추가
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
     * ECO EPL End Item List 삭제
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
     * Rev 속성의 Change Description 반환
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
	 * Part 가 참조중인 ECO 리스트를 확인함. 참조중이면 오류 메세지를 보여줌
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
     * 중복된 ECO 결재 Line 리스트를 가져옴
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
     * 이전 Revision 이 잘못된 Part 리스트를 가져옴
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
     * 변경현황 변경리스트 의 Option Category 를 가져옴
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
     * 변경현황 변경리스트 조회
     * @param ds
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> getEcoStatusChangeList(DataSet ds) throws Exception {
        ArrayList<HashMap<String, String>> resultList = null;
        //2023-10 조직변경 하드 코딩된 그룹명을 Preference로 변경 
        ds.put("powerControlDevelopment", PreferenceService.getValue("POWER CONTROL DEVELOPMENT"));
        resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(ECO_HISTORY_SERVICE_CLASS, "getEcoStatusChangeList", ds);
        return resultList;
    }
	
	/**
     * System Code Null Value 리스트 조회
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
	 * SYS GUI를 가져옴
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
     * 변경현황 변경리스트 조회
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
	 * 설계변경 현황 기준정보 리스트 저장
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
	 * 설계변경 현황 검토 리스트 저장
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
	 * 설계변경 현황 기준정보 리스트 저장
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
     * 변경현황 변경리스트 조회
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
	 * 변경현황 정보 삭제
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
     * 변경 현황 전체리스트 조회
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
     * 변경 현황 기준정보리스트 조회
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
	 * 설계변경 현황 변경 리스트 수정
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
	 * 설계변경 현황 변경 리스트 ROW 삭제
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
	 * 설계변경 현황 변경 검토  Row 삭제
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
	 * Project 의 G Model 코드를 가져옴
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
     * 설계변경 현황 변경 리스트 수정
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
     * OSPEC Revision  리스트
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
	 * Product에 해당하는 EPL Puid를 가져옴
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
     * 설계변경현황 등록 대상 변경정보 리스트
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
     * 설계변경현황 EPL 리스트(중복된 값 포함)
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
	 * 설계변경현황 추가검토 옵션 생성
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
     * ECO 현황 변경검토 Category 정보 리스트
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
     * ECO 현황 추가검토 옵션 리스트
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
     * 설계변경 현황 변경 검토 Count
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
	 * 변경현황 정보 삭제
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
     * 설계변경 현황 기준정보 With Master PUID
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
	 * Function EPL Check 등록
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
     * Function EPL Check 리스트
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
	 * Function EPL Check 리스트 삭제
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
     * Function EPL Check  수정
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
     * Function EPL Check 현황
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
	 * ECO의 EPL을 가져오는 쿼리
	 * @Copyright : Plmsoft
	 * @author : 이정건
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
	 * Order No 중복 체크 추가
	 * @Copyright : Plmsoft
	 * @author : 이정건
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
	 * [20230406][CF-3876] 기술관리팀 이보현 책임, 안추은 책임 요청
	 * #1. Vehicle ECO에 파워트레인 프로젝트가 존재 유무 체크 
	 * #2. Vehicle ECO(차량 ECO)의 EPL Proj속성에 파워트레인 프로젝트 존재시 오류 처리 
	 * #3. 파워트레인 프로젝트는 Power Traing ECO(엔진 ECO)로 작업 해야한다.
	 * ECO 결재 요청 시 차량 ECO번호를 채번하고 엔진이나 미션 파트를 추가한 경우 error 가 나옴.
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
