package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.CCNInfoDao;

/**
 * [20160606][ymjang] ���� �߼� ��� ���� (through EAI)
 * [20160715][ymjang] CCN EPL ���� ��� �ű� �߰�
 * [20160718] IF CCN Master ���� ���� ���� ���� --> Stored Procedure �� �̰���.
 * [20170622][ljg]whereUser() API ��Ŀ��� DB������ ���� --> BOM�� ������ �Ͽ� �θ��� �������� �������� ��� �ش� �θ��� ���� �������� ������
 */
public class CCNService {

    private CCNInfoDao dao;

    public CCNService() {

    }

    /**
     * ������ CCN �� ������ �������� �ý��� �ڵ带 ��� �����´�
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectMasterSystemCode(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectMasterSystemCode(dataSet);
    }
    
    /**
     * ������ CCN �� ���� ���� �� �����´�
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectMasterInfoList(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectMasterInfoList(dataSet);
    }
    
    /**
     * ������ CCN �� ���� MasterList �� Usage ���� �� �����´�
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectMasterUsageInfoList(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectMasterUsageInfoList(dataSet);
    }
    
    /**
     *  OSPEC �� ������ �����´� CCN ���� ��� ����
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectOSpecHeaderInfoList(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectOSpecHeaderInfoList(dataSet);
    }
    
    /**
     * SYMC ��Ʈ����� ���� ���� �߼�
     * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
     * @return
     */
    public boolean sendMail(DataSet ds){
        dao = new CCNInfoDao();
        
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
     *  CCN ���������� Insert �Ѵ�
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
     *  CCN ���������� Insert �Ѵ�
     * @param arrayDataSet
     * @return
     */
    public boolean insertUsage(DataSet dataSet) {
    	dao = new CCNInfoDao();
    	
    	return dao.insertUsage(dataSet);
    }
    
    /**
     * [20160715][ymjang] CCN EPL ���� ��� �ű� �߰�
     * @param dataSet
     * @return
     */
    public boolean correctCCNEPL(DataSet dataSet) {
    	dao = new CCNInfoDao();
    	
    	return dao.correctCCNEPL(dataSet);
    }
        
    /**
     *  CCN ���������� Insert �Ѵ� (IF ��)
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
     * CCN Validation ��� Message
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, Object>> selectCCNValidateMessage(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectCCNValidateMessage(dataSet);
    }
    
    /**
     * [20160610] IF CCN Master ���� ����
     * @param ccnId
     * @return
     */
    public boolean deleteIFCCNMaster(DataSet ds) {
        dao = new CCNInfoDao();
        return dao.deleteIFCCNMaster(ds);
    }

    /**
     * [20160718] IF CCN Master ���� ���� ���� ���� --> Stored Procedure �� �̰���.
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
     * CCN�� Reference �� Pre BOM ��Ʈ ����Ʈ��  ������
     * @param dataSet
     * @return
     */
    public ArrayList<HashMap<String, String>> selectPreBomPartsReferencedFromCCN(DataSet dataSet) {
        dao = new CCNInfoDao();

        return dao.selectPreBomPartsReferencedFromCCN(dataSet);
    }
    
    /**
     * CCN EPL ����Ʈ�� ������
     * @param dataSet
     * @return
     */
    public boolean deleteEPLAllList(DataSet ds) {
        dao = new CCNInfoDao();
        return dao.deleteEPLAllList(ds);
    }
    
    /**
     * BOM�� ������ �Ͽ� �θ��� �������� �������� ��� �ش� �θ��� ���� �������� ������
     * @Copyright : Plmsoft
     * @author : ������
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
