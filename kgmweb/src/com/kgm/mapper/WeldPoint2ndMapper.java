package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.WeldPoint2ndDao;

/**
 * [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
 */
public interface WeldPoint2ndMapper {
	
	public void deleteWeldPointRawData(DataSet ds) throws Exception;
	
	public void insertWeldPointRawDataRow(DataSet ds) throws Exception;

	public void deleteWeldPointRaw2Data(DataSet ds) throws Exception;

	public void makeArrangedStartPointData(DataSet ds) throws Exception;
	
	public void updateArrangedStartPointDataScaling(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getECOId(DataSet ds) throws Exception;
	
	public void deleteCurrentSavedData(DataSet ds) throws Exception;
	
	public void deleteCurrentInboundData(DataSet ds) throws Exception;
	
	public void makeInBoundData(DataSet ds) throws Exception;
	
	public void makeEndDiffData(DataSet ds) throws Exception;
	
	public void makeSaveDataForDelete(DataSet ds) throws Exception;
	
	public void makeSaveDataForInBound(DataSet ds) throws Exception;
	
	public void makeSaveDataForEndDiff(DataSet ds) throws Exception;
	
	public void makeSaveDataForAdd(DataSet ds) throws Exception;
	
	public void deleteBOMWeldPointData(DataSet ds) throws Exception;

	public void insertBOMWeldPointDataRow(DataSet ds) throws Exception;

	public void deleteBOMWeldPoint2Data(DataSet ds) throws Exception;

	public void makeBOMArrangedStartPointData(DataSet ds) throws Exception;
	
	public ArrayList<HashMap<String, Object>> findHaveSameEcoWeldGroupRevisionData(DataSet ds) throws Exception;
	
	public ArrayList<HashMap<String, Object>> getECOMatchedFMPRevision(DataSet ds) throws Exception;
	
	public ArrayList<HashMap<String, Object>> getDeleteTargetBOMLineData(DataSet ds) throws Exception;
	
	public ArrayList<HashMap<String, Object>> getAddTargetWeldPointData(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getMaxOccSeqNo(DataSet ds) throws Exception;
	
	public  ArrayList<HashMap<String, Object>> getAllNewBOMLineCount(DataSet ds) throws Exception;
	
	public  ArrayList<HashMap<String, Object>> getUpdateTargetBOMLineData(DataSet ds) throws Exception;
	
	public  ArrayList<HashMap<String, Object>> getChildNodeWeldTypeList(DataSet ds) throws Exception;
	
}
