package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.SYMCBOPFindAISDataDao;

public class SYMCBOPFindAISDataService {

	private SYMCBOPFindAISDataDao dao;

    /**
     * �ӽ������� ���� Data���� �˻����ǿ� �´� ���� �����۾� ǥ�ؼ� Data�� Query�ϴ� Service Function
     * @param ds
     * @return
     */
    public ArrayList<HashMap> findKORInstructionSheets(DataSet ds){
		dao = new SYMCBOPFindAISDataDao();
        return dao.findKORInstructionSheets(ds);
    }

    /**
     * �ӽ������� ���� Data���� �˻����ǿ� �´� ���� �����۾� ǥ�ؼ� Data�� Query�ϴ� Service Function
     * @param ds
     * @return
     */
    public ArrayList<HashMap> findENGInstructionSheets(DataSet ds){
		dao = new SYMCBOPFindAISDataDao();
        return dao.findENGInstructionSheets(ds);
    }

    /**
     * ���� ���� �����۾�ǥ�ؼ� �˻������� �ӽ÷� Data�� �����ϴ� Service Function
     * @param dataSet
     * @return
     */
    public int insertKORAssySheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertKORAssySheetsData(dataSet);
    }
    
    /**
     * ��ü ���� �����۾�ǥ�ؼ� �˻������� �ӽ÷� Data�� �����ϴ� Service Function
     * @param dataSet
     * @return
     */
    public int insertKORBodySheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertKORBodySheetsData(dataSet);
    }
    
    /**
     * ���� ���� �����۾�ǥ�ؼ� �˻������� �ӽ÷� Data�� �����ϴ� Service Function
     * @param dataSet
     * @return
     */
    public int insertKORPaintSheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertKORPaintSheetsData(dataSet);
    }
    
    /**
     * ���� ���� �����۾�ǥ�ؼ� �˻������� �ӽ÷� Data�� �����ϴ� Service Function
     * @param dataSet
     * @return
     */
    public int insertENGAssySheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertENGAssySheetsData(dataSet);
    }
    
    /**
     * ��ü ���� �����۾�ǥ�ؼ� �˻������� �ӽ÷� Data�� �����ϴ� Service Function
     * @param dataSet
     * @return
     */
    public int insertENGBodySheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertENGBodySheetsData(dataSet);
    }
    
    /**
     * ���� ���� �����۾�ǥ�ؼ� �˻������� �ӽ÷� Data�� �����ϴ� Service Function 
     * @param dataSet
     * @return
     */
    public int insertENGPaintSheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertENGPaintSheetsData(dataSet);
    }
    
    /**
     * XML Element ���·� Data�� �˻��ؼ� Return�ϴ� Service Function
     * Return Type�� �޴� �κп��� ����ȯ�� �� �� ���� ������ �߻���
     * �Ƹ��� ������ ����ȯ������ �߰����� ó���� ����� �� ������ ����.
     * @param ds
     * @return
     */
    public ArrayList<HashMap<String, Object>> findPublishItemRevListDataXML(DataSet ds){
		dao = new SYMCBOPFindAISDataDao();
        return dao.findPublishItemRevListDataXML(ds);
    }
    
    /**
     * �����۾�ǥ�ؼ� �˻� ��� Row�� ComboBox�� �ѱ۶Ǵ� ���� �����۾�ǥ�ؼ� Document Item Revision Id�� List �ϴµ�
     * �̰��� ������ �ݺ����� �ʰ� �ѹ��� �о XML ������ ����� ��Ƴ��� ���� ���� findPublishItemRevListDataXML��
     * ��ü�ϱ����� ���� Service Function ��. 
     * @param ds
     * @return
     */
    public ArrayList<HashMap<String, Object>> findPublishItemRevListDataList(DataSet ds){
		dao = new SYMCBOPFindAISDataDao();
        return dao.findPublishItemRevListDataList(ds);
    }
    
    /**
     * �������� 7���� ���� Data�� �����ϴ� Service Function
     * @param dataSet
     * @return
     */
    public int deleteBeforDatas(DataSet dataSet){
		dao = new SYMCBOPFindAISDataDao();
        return dao.deleteBeforDatas(dataSet);
    }
    
}
