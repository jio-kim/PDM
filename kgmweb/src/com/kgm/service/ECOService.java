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
 * [20160606][ymjang] ���� �߼� ��� ���� (through EAI)
 * [20160620][ymjang] ECI �� ECR ���� I/F ��� ���� (through EAI)
 * [20160721][ymjang] java.io.NotSerializableException ���� ����
 * [SR170828-015][LJG]Chassis module ������ ���� ���� ���� �߰� ��û
 */
public class ECOService {

	/**
	 * TC ITEM ID ����
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
	 * ECI I/F ���̺� ������Ʈ ����Ȯ�� ������Ʈ
	 * @param ds
	 * @return
	 */
	public boolean confirmECIReceived(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.confirmECIReceived(ds);
	}
	
	/**
	 * IF_ECI_REVIEW_FROM_VNET ���̺� ����Ȯ�� ������Ʈ
	 * @param ds
	 * @return
	 */
	public boolean confirmECIReviewReceived(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.confirmECIReviewReceived(ds);
	}
	
	/**
	 * ������ �˻�
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
	 * ECI �� ECR ���� I/F ��� ���� (through EAI)
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
	 * ECO ǰ�� �߹�
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
	 * Vision Net���� �μ� ���� ������ ����
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
	 * ECO ���� Update (through EAI)
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
	 * ECO ������ �ҷ�����
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
	 * ����� ������ �ҷ�����
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
	 * SQL�� ������ Object�� ������Ʈ �� ���� �α׾ƿ� ���� refresh
	 * refreshTCTimeStamp�� ���� ���� �ؾ� ��.
	 * @param ds
	 * @return
	 */
	public boolean refreshTCObject(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.refreshTCObject(ds);
	}
	
	/**
	 * SQL�� ������ Object�� ������Ʈ �� ���� �α׾ƿ� ���� refresh
	 * @param ds
	 * @return
	 */
	public boolean refreshTCTimeStamp(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.refreshTCTimeStamp(ds);
	}

	/**
	 * ������ ����
	 * @param ds
	 * @return
	 */
	public boolean removeApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.removeApprovalLine(ds);
	}
	
	/**
	 * ECO ������ ����
	 * @param ds
	 * @return
	 */
	public boolean saveApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.saveApprovalLine(ds);
	}
	
	/**
	 * ����� ������ ����
	 * @param ds
	 * @return
	 */
	public boolean saveUserApprovalLine(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.saveUserApprovalLine(ds);
	}
	
	/**
	 * ECO ��Ȳ ��ȸ
	 * @param ds
	 * @return
	 */
	public ArrayList<SYMCECOStatusData> searchEOStatus(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
        return dao.searchEOStatus(ds);
	}
	
	/**
	 * ECR ��Ȳ ��ȸ (through EAI)
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECREAI(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECREAI(ds);
	}
	
	/**
	 * ECR ��Ȳ ��ȸ
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECR(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECR(ds);
	}

	/**
	 * ECI ��Ȳ ��ȸ (through EAI)
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECIEAI(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECIEAI(ds);
	}
	
	/**
	 * ECI ��Ȳ ��ȸ
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> searchECI(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECI(ds);
	}
	
    /**
     * SYMC ��Ʈ����� ���� ���� �߼�
     * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
     * @return
     */
    public boolean sendMail(DataSet ds){
    	ECOInfoDao dao = new ECOInfoDao();
        
        // [20160606][ymjang] ���� �߼� ��� ���� (through EAI)
        dao.sendMailEai(ds);
        
        // [20160606][ymjang] ���� ���� �߼� ���
        //dao.sendMail(ds);
        
        return true;
    }
    
    /**
     * SYMC ��Ʈ����� ���� ���� �߼� (through EAI)
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
	 * [SR140806-002][20140725] swyoon ALC�� ����(PG_ID, PG_ID_VERSION). Replace�� �ش�Ǵ� ��츸, New�� Null�̰�, Old�� Null�� �ƴѰ�� Old���� New�� ������.
	 * 
	 * @param ds
	 */	
	public void updateALC(DataSet ds) {
		ECOInfoDao dao = new ECOInfoDao();
		dao.updateALC(ds);
	}
	
	/**
	 * [SR��ȣ����(Migration�� ����)][20140820] swyoon EPL ����.
	 * 
	 * @param ds
	 * @throws Exception 
	 */	
	public void correctEPL(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		dao.correctEPL(ds);
	}	
	
	/**
	 * [SR��ȣ����(Migration�� ����)][20140820] swyoon EPL ���� ���� ����.
	 * [20160721][ymjang] java.io.NotSerializableException ���� ����
	 * @param ds
	 * @throws Exception 
	 */		
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap<String, String>> getIncorrectList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getIncorrectList(ds);
	}
	
	/**
	 * [SR��ȣ����(Migration�� ����)][20140820] swyoon EPL ���� ���� ����.
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
	 * [][20140916] Cut and paste �߻��� OccThread ���� �۾��� ���� ���� ����.
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
	 * [][20140919] Cut and paste �߻��� ECO_BOM_LIST�� �ִ� Occ_Threads �� ������.
	 * 
	 * @param ds
	 */
	public void updateOccthread(DataSet ds) {
		ECOInfoDao dao = new ECOInfoDao();
		dao.updateOccthread(ds);
	}		
	
	/**
	 * SRME:: [][20141007] Order No�� �ߺ��Ǵ� List ����.
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
	 * SRME:: [][20141007] �ִ� Order No ����.
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
	 * [SR141120-043][2014.11.21][jclee] Color ID�� �����ϸ鼭 Color Section No�� �������� �ʴ� �׸� ����Ʈ ��ȸ
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
	 * [20180718][CSH]End Item���� 500�� �ʰ��� HBOM(�̱��� ����)�� Mail�뺸
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
	 * [SR141205-027][2014.12.16][jclee] Color ID�� ����� �׸� ����Ʈ ��ȸ
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
	 * [2015.02.11][jclee] IN ECO ���� ����
	 * @param ds
	 */
	public void makeBOMHistoryMaster(DataSet ds) {
		ECOInfoDao dao = new ECOInfoDao();
		dao.makeBOMHistoryMaster(ds);
	}
	
	/**
	 * [SR141205-027][2015.01.26][jclee] Function�� ����Ǿ��ִ���(BOM�� �����Ǿ��ִ���) Ȯ��
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
	 * [SR150213-010][2015.02.25][jclee] EPL���� Ư�� FMP ���� 1Lv Part �� Supply Mode�� P�� �����ϴ� EPL�� Car Project�� �����ϴ��� ��ȸ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getCarProjectInEPL(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getCarProjectInEPL(ds);
	}
	
	/**
	 * ECO No�� ���� �����鼭 EPL�� New Part No �� ���ԵǾ� ���� �ʴ� Part ��� ��ȯ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getCANNOTGeneratedList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getCANNOTGeneratedList(ds);
	}
	
	/**
	 * EPL Cut �� Revise�Ͽ� �ٽ� Paste�� ��� Ȯ�� (Revise �̷� ����)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getCANNOTGeneratedReviseList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getCANNOTGeneratedReviseList(ds);
	}
	
	/**
	 * ECO EPL ���� ��ȸ
	 * @param ds
	 * @return
	 */
	public ArrayList<SYMCECOStatusData> searchECOCorrectionHistory(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.searchECOCorrectionHistory(ds);
	}
	
	/**
	 * Part �� �������� ECO ����Ʈ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> getRefEcoFromPartList(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getRefEcoFromPartList(ds);
	}
	
	/**
	 * �ߺ��� ECO ���缱 ����Ʈ
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,Object>> getEcoDupApprovalLines(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getEcoDupApprovalLines(ds);
	}
	
	
	/**
	 * ���� Revision �� �߸��� Part ����Ʈ�� ������
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String,String>> getOldRevNotMatchedParts(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getOldRevNotMatchedParts(ds);
	}
	
	/**
	 * Order No �ߺ� üũ
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 6. 13.
	 * @param ds
	 * @return
	 */
	public ArrayList<HashMap<String, Object>> duplicateOrderNoCheck(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.duplicateOrderNoCheck(ds);
	}
	
	/**
	 * ECO�� EPL�� ������
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 6. 13.
	 * @param ds
	 * @return
	 */
	public ArrayList<String> getECOEPL(DataSet ds){
		ECOInfoDao dao = new ECOInfoDao();
		return dao.getECOEPL(ds);
	}
	
	/**
	 * [SR170828-015][LJG]Chassis module ������ ���� ���� ���� �߰� ��û
  	   1. ECO ���� Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
  	   2. Module code : FCM or RCM
  	   3. Part�� Option : Z999�� �����ϴ� ���
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
	 * [20230406][CF-3876] ��������� �̺��� å��, ������ å�� ��û
	 * #1. Vehicle ECO�� �Ŀ�Ʈ���� ������Ʈ�� ���� ���� üũ 
	 * #2. Vehicle ECO(���� ECO)�� EPL Proj�Ӽ��� �Ŀ�Ʈ���� ������Ʈ ����� ���� ó�� 
	 * #3. �Ŀ�Ʈ���� ������Ʈ�� Power Traing ECO(���� ECO)�� �۾� �ؾ��Ѵ�.
	 * ECO ���� ��û �� ���� ECO��ȣ�� ä���ϰ� �����̳� �̼� ��Ʈ�� �߰��� ��� error �� ����.
	 */ 
	public ArrayList<HashMap<String, String>> checkPowerTraing(DataSet ds) throws Exception {
		ECOInfoDao dao = new ECOInfoDao();
        return dao.checkPowerTraing(ds);
	}
	

}