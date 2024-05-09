package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.SYMCBOPDao;
import com.kgm.dto.EndItemData;

public class SYMCBOPService {

	private SYMCBOPDao dao;
	
	public ArrayList<EndItemData> findReplacedEndItems(DataSet ds) {
		dao = new SYMCBOPDao();
        return dao.findReplacedEndItems(ds);
	}
	
	/*
	 * SoaWeb���� EndItemList ��ȸ�� 
	 * Mbom �� Ebom �� ��� EndItem�� ��ȸ �Ͽ��� �ϳ� �ӵ��� �ʹ� ���� 
	 * ������ ������ ������ �޼��� 
	 */
	public ArrayList<EndItemData> findReplacedRootEndItems(DataSet ds) {
		dao = new SYMCBOPDao();
		return dao.findReplacedRootEndItems(ds);
	}
	
    public boolean insertOperationOccurenceForInstructionSheets(DataSet dataSet) {
        dao = new SYMCBOPDao();
        return dao.insertOperationOccurenceForInstructionSheets(dataSet);
    }
	
	/**
	 * [NONE_SR][20151123] taeku.jeong Operation�˻� �ӵ� ���������� API�� �̿��� ���� ����� �ƴ� Query�� �̿��ϴ� ������� ����
	 * @param ds
	 * @return
	 */
    public List<HashMap> findOperationOccurenceForInstructionSheets(DataSet ds){
		dao = new SYMCBOPDao();
        return dao.findOperationOccurenceForInstructionSheets(ds);
    }
    
    
    public List<HashMap> findOperationOccurenceForInstructionSheetsNew(DataSet ds){
		dao = new SYMCBOPDao();
        return dao.findOperationOccurenceForInstructionSheetsNew(ds);
    }
    
    public boolean deleteOperationOccurenceForInstructionSheets(DataSet dataSet) {
        dao = new SYMCBOPDao();
        return dao.deleteOperationOccurenceForInstructionSheets(dataSet);
    }

    public boolean deleteOperationOccurenceForInstructionSheets2(DataSet dataSet) {
        dao = new SYMCBOPDao();
        return dao.deleteOperationOccurenceForInstructionSheets2(dataSet);
    }
    
    /**
     * [NONE-SR] [20151126] taeku.jeong �����۾� ǥ�ؼ� Password �ϰ������� ���� ��� Data �˻�
     * 
     * @param ds
     * @return
     */
    public List<HashMap> findAISPasswordMigrationTarget(DataSet ds){
		dao = new SYMCBOPDao();
        return dao.findAISPasswordMigrationTarget(ds);
    }
    
    /**
     * [SR151207-041] [20151211] taeku.jeong �������� Occurrence �̸��� �̿��� �Ҵ�� ABS_OCC_ID�� ã���ش�.
     * @param ds
     * @return
     */
    public List<HashMap> findPWProductAbsOccurenceId(DataSet ds){
		dao = new SYMCBOPDao();
        return dao.findPWProductAbsOccurenceId(ds);
    }
    
    /**
     * [SR151207-041] [20151215] taeku.jeong �������� ABS_OCC_ID�� �̿��� �Ҵ�� Occurrence �̸��� ã���ش�.
     * @param ds
     * @return
     */
    public List<HashMap> findWPProductOccurenceName(DataSet ds){
		dao = new SYMCBOPDao();
        return dao.findWPProductOccurenceName(ds);
    }
    
}
