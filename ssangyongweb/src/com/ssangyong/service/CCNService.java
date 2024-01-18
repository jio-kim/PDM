package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.CCNInfoDao;

/**
 * [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
 * [20160715][ymjang] CCN EPL 보정 기능 신규 추가
 * [20160718] IF CCN Master 정보 생성 로직 개선 --> Stored Procedure 로 이관함.
 * [20170622][ljg]whereUser() API 방식에서 DB쿼리로 변경 --> BOM을 역전개 하여 부모의 리비전이 여러개일 경우 해당 부모의 최종 리비전만 가져옴
 */
public class CCNService {

    private CCNInfoDao dao;

    public CCNService() {

    }

    /**
     * 선택한 CCN 에 마스터 정보에서 시스템 코드를 모두 가져온다
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectMasterSystemCode(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectMasterSystemCode(dataSet);
    }
    
    /**
     * 선택한 CCN 에 을지 정보 를 가져온다
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectMasterInfoList(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectMasterInfoList(dataSet);
    }
    
    /**
     * 선택한 CCN 에 을지 MasterList 에 Usage 정보 를 가져온다
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectMasterUsageInfoList(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectMasterUsageInfoList(dataSet);
    }
    
    /**
     *  OSPEC 에 정보를 가져온다 CCN 을지 헤더 정보
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectOSpecHeaderInfoList(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectOSpecHeaderInfoList(dataSet);
    }
    
    /**
     * SYMC 인트라넷을 통한 메일 발송
     * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
     * @return
     */
    public boolean sendMail(DataSet ds){
        dao = new CCNInfoDao();
        
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
        dao = new CCNInfoDao();
        return dao.sendMailEai(ds);
    }
    
    public boolean insertCCNMaster(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.insertCCNMaster(dataSet);
    }
    
    public boolean insertIfCCNMaster(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.insertIfCCNMaster(dataSet);
    }
    
    public boolean deleteCCNMaster(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.deleteCCNMaster(dataSet);
    }
    
    /**
     *  CCN 을지정보를 Insert 한다
     * @param arrayDataSet
     * @return
     */
    public boolean insertEPLList(DataSet dataSet) throws Exception {
        dao = new CCNInfoDao();
        
        return dao.insertEPLList(dataSet);
    }
    
    public boolean insertEPLList_(DataSet dataSet) throws Exception {
        dao = new CCNInfoDao();
        
        return dao.insertEPLList_(dataSet);
    }
    
    /**
     *  CCN 을지정보를 Insert 한다
     * @param arrayDataSet
     * @return
     */
    public boolean insertUsage(DataSet dataSet) {
    	dao = new CCNInfoDao();
    	
    	return dao.insertUsage(dataSet);
    }
    
    /**
     * [20160715][ymjang] CCN EPL 보정 기능 신규 추가
     * @param dataSet
     * @return
     */
    public boolean correctCCNEPL(DataSet dataSet) {
    	dao = new CCNInfoDao();
    	
    	return dao.correctCCNEPL(dataSet);
    }
        
    /**
     *  CCN 을지정보를 Insert 한다 (IF 용)
     * @param arrayDataSet
     * @return
     */
    public boolean insertIfEPLList(DataSet dataSet) {
        dao = new CCNInfoDao();
        
        return dao.insertIfEPLList(dataSet);
    }
    
    public boolean insertEPLListDiff(DataSet dataSet) {
        dao = new CCNInfoDao();
        
        return dao.insertEPLListDiff(dataSet);
    }
    
    public boolean insertIfEPLListDiff(DataSet dataSet) {
        dao = new CCNInfoDao();
        
        return dao.insertIfEPLListDiff(dataSet);
    }
    
    public Object getDwgDeployableDate(DataSet ds) throws Exception{
        dao = new CCNInfoDao();

        return dao.getDwgDeployableDate(ds);
    }

    public Object getDcsReleasedDate(DataSet ds) throws Exception{
        dao = new CCNInfoDao();

        return dao.getDcsReleasedDate(ds);
    }

    /**
     * CCN Validation 결과 Message
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectCCNValidateMessage(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectCCNValidateMessage(dataSet);
    }
    
    /**
     * [20160610] IF CCN Master 정보 삭제
     * @param ccnId
     * @return
     */
    public boolean deleteIFCCNMaster(DataSet ds) {
        dao = new CCNInfoDao();
        return dao.deleteIFCCNMaster(ds);
    }

    /**
     * [20160718] IF CCN Master 정보 생성 로직 개선 --> Stored Procedure 로 이관함.
     * @param dataSet
     * @return
     * @throws Exception
     */
    public Boolean createIfCCN(DataSet dataSet) throws Exception
    {
    	Boolean isOK = false;
        try
        {        	
        	dao = new CCNInfoDao();
        	isOK = dao.createIfCCN(dataSet);
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return isOK;
        
    }
    
    /**
     * CCN에 Reference 된 Pre BOM 파트 리스트를  가져옴
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, String>> selectPreBomPartsReferencedFromCCN(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectPreBomPartsReferencedFromCCN(dataSet);
    }
    
    /**
     * CCN EPL 리스트를 삭제함
     * @param dataSet
     * @return
     */
    public boolean deleteEPLAllList(DataSet ds) {
        dao = new CCNInfoDao();
        return dao.deleteEPLAllList(ds);
    }
    
    /**
     * BOM을 역전개 하여 부모의 리비전이 여러개일 경우 해당 부모의 최종 리비전만 가져옴
     * @Copyright : Plmsoft
     * @author : 이정건
     * @since  : 2017. 6. 22.
     * @param dataSet
     * @return
     */
    public ArrayList<String> whereUsed(DataSet dataSet) throws Exception{
        dao = new CCNInfoDao();

        return dao.whereUsed(dataSet);
    }
    
    public String getParent4Digit(DataSet dataSet) throws Exception{
        dao = new CCNInfoDao();

        return dao.getParent4Digit(dataSet);
    }
    
    public String getParent4DigitReleased(DataSet dataSet) throws Exception{
        dao = new CCNInfoDao();

        return dao.getParent4DigitReleased(dataSet);
    }
    
    public String getPreRevisionPuid(DataSet dataSet) throws Exception{
        dao = new CCNInfoDao();

        return dao.getPreRevisionPuid(dataSet);
    }
    
    public ArrayList<HashMap<String, Object>> arrParentEPLData(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.arrParentEPLData(dataSet);
    }
    
    public ArrayList<HashMap<String, Object>> arrParentEPLDataOld(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.arrParentEPLDataOld(dataSet);
    }

    public ArrayList<HashMap<String, Object>> arrParentEPLDataNew(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.arrParentEPLDataNew(dataSet);
    }
    
    public ArrayList<HashMap<String, Object>> getChildBOMPro(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.getChildBOMPro(dataSet);
    }
    
}
