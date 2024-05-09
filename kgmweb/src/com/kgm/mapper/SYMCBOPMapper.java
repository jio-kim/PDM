package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dto.EndItemData;

public interface SYMCBOPMapper {

	public ArrayList<EndItemData> findReplacedEndItems(DataSet ds);
	
	/*
	 * SoaWeb���� EndItemList ��ȸ�� 
	 * Mbom �� Ebom �� ��� EndItem�� ��ȸ �Ͽ��� �ϳ� �ӵ��� �ʹ� ���� 
	 * ������ ������ ������ �޼��� 
	 */
	public ArrayList<EndItemData> findReplacedRootEndItems(DataSet ds);
	
	public void insertOperationOccurenceForInstructionSheets(DataSet dataSet);
	
	/**
	 * [NONE_SR][20151123] taeku.jeong Operation�˻� �ӵ� ���������� API�� �̿��� ���� ����� �ƴ� Query�� �̿��ϴ� ������� ����
	 * 
	 * @param ds
	 * @return
	 */
    @SuppressWarnings("rawtypes")
    public List<HashMap> findOperationOccurenceForInstructionSheets(DataSet ds);
    
    public List<HashMap> findOperationOccurenceForInstructionSheetsNew(DataSet ds);
    
    public void deleteOperationOccurenceForInstructionSheets(DataSet dataSet);
    
    public void deleteOperationOccurenceForInstructionSheets2(DataSet dataSet);
    
    /**
     * [NONE-SR] [20151126] taeku.jeong �����۾� ǥ�ؼ� Password �ϰ������� ���� ��� Data �˻�
     * @param ds
     * @return
     */
    public List<HashMap> findAISPasswordMigrationTarget(DataSet ds);
    
    /**
     * [SR151207-041] [20151211] taeku.jeong �������� Occurrence �̸��� �̿��� �Ҵ�� ABS_OCC_ID�� ã���ش�.
     * @param ds
     * @return
     */
    public List<HashMap> findPWProductAbsOccurenceId(DataSet ds);
    
    /**
     * [SR151207-041] [20151215] taeku.jeong �������� ABS_OCC_ID�� �̿��� �Ҵ�� Occurrence �̸��� ã���ش�.
     * @param ds
     * @return
     */
    public List<HashMap> findWPProductOccurenceName(DataSet ds);
}
