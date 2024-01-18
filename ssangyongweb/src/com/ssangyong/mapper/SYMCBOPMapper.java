package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dto.EndItemData;

public interface SYMCBOPMapper {

	public ArrayList<EndItemData> findReplacedEndItems(DataSet ds);
	
	/*
	 * SoaWeb에서 EndItemList 조회시 
	 * Mbom 과 Ebom 의 모든 EndItem을 조회 하여야 하나 속도가 너무 느려 
	 * 개선된 쿼리를 적용한 메서드 
	 */
	public ArrayList<EndItemData> findReplacedRootEndItems(DataSet ds);
	
	public void insertOperationOccurenceForInstructionSheets(DataSet dataSet);
	
	/**
	 * [NONE_SR][20151123] taeku.jeong Operation검색 속도 개선을위해 API를 이용한 전개 방식이 아닌 Query를 이용하는 방식으로 개선
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
     * [NONE-SR] [20151126] taeku.jeong 조립작업 표준서 Password 일괄변경을 위한 대상 Data 검색
     * @param ds
     * @return
     */
    public List<HashMap> findAISPasswordMigrationTarget(DataSet ds);
    
    /**
     * [SR151207-041] [20151211] taeku.jeong 용접점의 Occurrence 이름을 이용해 할당된 ABS_OCC_ID를 찾아준다.
     * @param ds
     * @return
     */
    public List<HashMap> findPWProductAbsOccurenceId(DataSet ds);
    
    /**
     * [SR151207-041] [20151215] taeku.jeong 용접점의 ABS_OCC_ID를 이용해 할당된 Occurrence 이름을 찾아준다.
     * @param ds
     * @return
     */
    public List<HashMap> findWPProductOccurenceName(DataSet ds);
}
