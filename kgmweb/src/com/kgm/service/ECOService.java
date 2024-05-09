package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.ECOInfoDao;
import com.kgm.dto.ApprovalLineData;
import com.kgm.dto.SYMCECOStatusData;
import com.kgm.dto.TCEcoModel;
import com.kgm.dto.VnetTeamInfoData;

/**
 * [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
 * [20160620][ymjang] ECI 및 ECR 정보 I/F 방식 개선 (through EAI)
 * [20160721][ymjang] java.io.NotSerializableException 오류 개선
 * [SR170828-015][LJG]Chassis module 관리를 위한 검증 조건 추가 요청
 */
public class ECOService {

	/**
	 * TC ITEM ID 변경
	 * @param ds
	 * @return
	 */
	public boolean changeItemId(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.changeItemId(ds);
	}
	
	public boolean changeECIStatus(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.changeECIStatus(ds);
	}
	
	public boolean changeECOStatus(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.changeECOStatus(ds);
	}
	
	public boolean changeMECOStatus(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.changeMECOStatus(ds);
	}
	
	public String getDuplicateCategoryInVC(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.getDuplicateCategoryInVC(ds);
	}
	
	public ArrayList<HashMap<String, String>> checkECOEPL(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
        return dao.checkECOEPL(ds);
	}
	
	
	public ArrayList<String> checkEndtoEnd(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.checkEndtoEnd(ds);
	}
	
	/**
	 * ECI I/F 테이블 업데이트 수신확인 업데이트
	 * @param ds
	 * @return
	 */
	public boolean confirmECIReceived(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.confirmECIReceived(ds);
	}
	
	/**
	 * IF_ECI_REVIEW_FROM_VNET 테이블 수신확인 업데이트
	 * @param ds
	 * @return
	 */
	public boolean confirmECIReviewReceived(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.confirmECIReviewReceived(ds);
	}
	
	/**
	 * 배포선 검색
	 * @param ds
	 * @return
	 */
	public ArrayList<ApprovalLineData> getApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.getApprovalLine(ds);
	}
	
	public HashMap<String,String> getECIfileInfo(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getECIfileInfo(ds);
	}
	
	/**
	 * ECI 및 ECR 정보 I/F 방식 개선 (through EAI)
	 * @param ds
	 * @return
	 */
	public TCEcoModel getEcoInfoEAI(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getEcoInfoEAI(ds);
	}
	
	public TCEcoModel getEcoInfo(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getEcoInfo(ds);
	}
	
	public String getAffectedProject(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getAffectedProject(ds);
	}
	
	/**
	 * ECO 품번 발번
	 * @param ds
	 * @return
	 */
	public String getNextECOSerial(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.getNextECOSerial(ds);
	}
	
	public ArrayList<String> getProblemItems(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.getProblemItems(ds);
	}
	
	public ArrayList<String> getSolutionItems(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.getSolutionItems(ds);
	}
	
	/**
	 * Vision Net에서 부서 정보 가지고 오기
	 * @param ds
	 * @return
	 */
	public ArrayList<VnetTeamInfoData> getVnetTeamInfo(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.getVnetTeamInfo(ds);
	}
	
	public ArrayList<VnetTeamInfoData> getVnetTeamNames(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.getVnetTeamNames(ds);
	}
	
	public boolean interfaceECI(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.interfaceECI(ds);
	}
	
	/**
	 * ECO 정보 Update (through EAI)
	 * @param ds
	 * @return
	 */
	public boolean interfaceECONoToVnetEAI(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.interfaceECONoToVnetEAI(ds);
	}
	
	public boolean interfaceECONoToVnet(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.interfaceECONoToVnet(ds);
	}
	
	public boolean updateFileName(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.updateFileName(ds);
	}
	
	/**
	 * ECO 배포선 불러오기
	 * @param object ApprovalLineMap
	 * @return
	 */
	public ArrayList<ApprovalLineData> loadApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.loadApprovalLine(ds);
	}
	
	public boolean deleteApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.deleteApprovalLine(ds);
	}

	/**
	 * 사용자 배포선 불러오기
	 * @param ds
	 * @return
	 */
	public ArrayList<ApprovalLineData> loadSavedUserApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.loadSavedUserApprovalLine(ds);
	}
	
	public boolean makePartHistory(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.makePartHistory(ds);
	}
	
	/**
	 * SQL로 팀센터 Object를 업데이트 후 세션 로그아웃 없이 refresh
	 * refreshTCTimeStamp와 동시 적용 해야 함.
	 * @param ds
	 * @return
	 */
	public boolean refreshTCObject(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.refreshTCObject(ds);
	}
	
	/**
	 * SQL로 팀센터 Object를 업데이트 후 세션 로그아웃 없이 refresh
	 * @param ds
	 * @return
	 */
	public boolean refreshTCTimeStamp(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.refreshTCTimeStamp(ds);
	}

	/**
	 * 배포선 삭제
	 * @param ds
	 * @return
	 */
	public boolean removeApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.removeApprovalLine(ds);
	}
	
	/**
	 * ECO 배포선 저장
	 * @param ds
	 * @return
	 */
	public boolean saveApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.saveApprovalLine(ds);
	}
	
	/**
	 * 사용자 배포선 저장
	 * @param ds
	 * @return
	 */
	public boolean saveUserApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.saveUserApprovalLine(ds);
	}
	
	/**
	 * ECO 현황 조회
	 * @param ds
	 * @return
	 */
	public ArrayList<SYMCECOStatusData> searchEOStatus(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.searchEOStatus(ds);
	}
	
	/**
	 * ECR 현황 조회 (through EAI)
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECREAI(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECREAI(ds);
	}
	
	/**
	 * ECR 현황 조회
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECR(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECR(ds);
	}

	/**
	 * ECI 현황 조회 (through EAI)
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECIEAI(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECIEAI(ds);
	}
	
	/**
	 * ECI 현황 조회
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECI(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECI(ds);
	}
	
    /**
     * SYMC 인트라넷을 통한 메일 발송
     * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
     * @return
     */
    public boolean sendMail(DataSet ds){
    	ECOInfoDao dao = new ECOInfoDao();
        
        // [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
        dao.sendMailEai(ds);
        
        // [20160606][ymjang] 기존 메일 발송 방식
        //dao.sendMail(ds);
        
        return true;
    }
    
    /**
     * SYMC 인트라넷을 통한 메일 발송 (through EAI)
     * @param ds
     * @return
     */
    public boolean sendMailEai(DataSet ds){
    	ECOInfoDao dao = new ECOInfoDao();
        return dao.sendMailEai(ds);
    }
	
	public boolean updateECIRevisionWithInterface(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.updateECIRevisionWithInterface(ds);
	}
	
	public ArrayList<HashMap<String,String>> getEcoWorkflowInfo(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getEcoWorkflowInfo(ds);
	}
	
	public String childrenCount(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.childrenCount(ds);
	}
	
	public String workflowCount(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.workflowCount(ds);
	}
	
	public String getEcoRevisionPuid(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getEcoRevisionPuid(ds);
	}
	
	public ArrayList<HashMap<String,String>> searchUserOnVnet(DataSet ds) {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchUserOnVnet(ds);
		
	}
	
	/**
	 * [SR140806-002][20140725] swyoon ALC값 복사(PG_ID, PG_ID_VERSION). Replace에 해당되는 경우만, New가 Null이고, Old가 Null이 아닌경우 Old값을 New로 복사함.
	 * 
	 * @param ds
	 */	
	public void updateALC(DataSet ds) {
		ECOInfoDao dao = new ECOInfoDao();
		dao.updateALC(ds);
	}
	
	/**
	 * [SR번호없음(Migration시 개발)][20140820] swyoon EPL 보정.
	 * 
	 * @param ds
	 * @throws Exception 
	 */	
	public void correctEPL(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		dao.correctEPL(ds);
	}	
	
	/**
	 * [SR번호없음(Migration시 개발)][20140820] swyoon EPL 원본 가져 오기.
	 * [20160721][ymjang] java.io.NotSerializableException 오류 개선
	 * @param ds
	 * @throws Exception 
	 */		
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap<String, String>> getIncorrectList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getIncorrectList(ds);
	}
	
	/**
	 * [SR번호없음(Migration시 개발)][20140820] swyoon EPL 정보 가져 오기.
	 * 
	 * @param ds
	 * @throws Exception 
	 */		
	@SuppressWarnings("rawtypes")
	public HashMap getEPL(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getEPL(ds);
	}	
	
	/**
	 * [][20140916] Cut and paste 발생시 OccThread 보정 작업을 위한 정보 리턴.
	 * 
	 * @param ds
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public List getChangedOcc(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getChangedOcc(ds);
	}	
	
	/**
	 * [][20140919] Cut and paste 발생시 ECO_BOM_LIST에 있는 Occ_Threads 를 보정함.
	 * 
	 * @param ds
	 */
	public void updateOccthread(DataSet ds) {
		ECOInfoDao dao = new ECOInfoDao();
		dao.updateOccthread(ds);
	}		
	
	/**
	 * SRME:: [][20141007] Order No가 중복되는 List 리턴.
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public List getDuplicatedOrderNoList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getDuplicatedOrderNoList(ds);
	}	
	
	/**
	 * SRME:: [][20141007] 최대 Order No 리턴.
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public Object getMaxOrderNo(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getMaxOrderNo(ds);
	}
	
	public String getInEcoFromECOBOMList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getInEcoFromECOBOMList(ds);
		
	}		
	
	/**
	 * [SR141120-043][2014.11.21][jclee] Color ID가 존재하면서 Color Section No가 존재하지 않는 항목 리스트 조회
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getColorIDWarningList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getColorIDWarningList(ds);
	}	
	
	/**
	 * [20180718][CSH]End Item수가 500개 초과시 HBOM(이광석 차장)에 Mail통보
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public String getEcoEndItemCount(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getEcoEndItemCount(ds);
	}	
	
	/**
	 * [SR141205-027][2014.12.16][jclee] Color ID가 변경된 항목 리스트 조회
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getColorIDChangingList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getColorIDChangingList(ds);
	}
	
	/**
	 * [2015.02.11][jclee] IN ECO 정보 생성
	 * @param ds
	 */
	public void makeBOMHistoryMaster(DataSet ds) {
		ECOInfoDao dao = new ECOInfoDao();
		dao.makeBOMHistoryMaster(ds);
	}
	
	/**
	 * [SR141205-027][2015.01.26][jclee] Function에 연결되어있는지(BOM에 구성되어있는지) 확인
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public String isConnectedFunction(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.isConnectedFunction(ds);
	}
	
	/**
	 * [SR150213-010][2015.02.25][jclee] EPL에서 특정 FMP 하위 1Lv Part 중 Supply Mode에 P를 포함하는 EPL이 Car Project를 포함하는지 조회
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getCarProjectInEPL(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getCarProjectInEPL(ds);
	}
	
	/**
	 * ECO No를 갖고 있으면서 EPL의 New Part No 에 포함되어 있지 않는 Part 목록 반환
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getCANNOTGeneratedList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getCANNOTGeneratedList(ds);
	}
	
	/**
	 * EPL Cut 후 Revise하여 다시 Paste한 경우 확인 (Revise 이력 누락)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getCANNOTGeneratedReviseList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getCANNOTGeneratedReviseList(ds);
	}
	
	/**
	 * ECO EPL 보정 조회
	 * @param ds
	 * @return
	 */
	public ArrayList<SYMCECOStatusData> searchECOCorrectionHistory(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECOCorrectionHistory(ds);
	}
	
	/**
	 * Part 가 참조중인 ECO 리스트
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getRefEcoFromPartList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getRefEcoFromPartList(ds);
	}
	
	/**
	 * 중복된 ECO 결재선 리스트
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,Object>> getEcoDupApprovalLines(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getEcoDupApprovalLines(ds);
	}
	
	
	/**
	 * 이전 Revision 이 잘못된 Part 리스트를 가져옴
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> getOldRevNotMatchedParts(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getOldRevNotMatchedParts(ds);
	}
	
	/**
	 * Order No 중복 체크
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 6. 13.
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String, Object>> duplicateOrderNoCheck(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.duplicateOrderNoCheck(ds);
	}
	
	/**
	 * ECO의 EPL을 가져옴
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 6. 13.
	 * @param ds
	 * @return
	 */
	public ArrayList<String> getECOEPL(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getECOEPL(ds);
	}
	
	/**
	 * [SR170828-015][LJG]Chassis module 관리를 위한 검증 조건 추가 요청
  	   1. ECO 내의 Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
  	   2. Module code : FCM or RCM
  	   3. Part의 Option : Z999을 포함하는 경우
	 */
	public ArrayList<String> checkChassisModule(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.checkChassisModule(ds);
	}
	
	public boolean updateStep(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.updateStep(ds);
	}
	
	public String getNMCDUpdatePartList(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getNMCDUpdatePartList(ds);
	}
	
	public String getAdmin(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getAdmin(ds);
	}
	
	public ArrayList<HashMap<String,String>> getProjectCodeList(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getProjectCodeList(ds);
	}
	
	public ArrayList<HashMap<String, Object>> notConnectedFunctionList(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.notConnectedFunctionList(ds);
	}
/*	  
	 * [20230406][CF-3876] 기술관리팀 이보현 책임, 안추은 책임 요청
	 * #1. Vehicle ECO에 파워트레인 프로젝트가 존재 유무 체크 
	 * #2. Vehicle ECO(차량 ECO)의 EPL Proj속성에 파워트레인 프로젝트 존재시 오류 처리 
	 * #3. 파워트레인 프로젝트는 Power Traing ECO(엔진 ECO)로 작업 해야한다.
	 * ECO 결재 요청 시 차량 ECO번호를 채번하고 엔진이나 미션 파트를 추가한 경우 error 가 나옴.
	 */ 
	public ArrayList<HashMap<String, String>> checkPowerTraing(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
        return dao.checkPowerTraing(ds);
	}
	

}