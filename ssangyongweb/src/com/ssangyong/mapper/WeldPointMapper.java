package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

/**
 * [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
 */
public interface WeldPointMapper {

	public void updateDateReleasedWithEco(DataSet ds) throws Exception;
	public void insertWeldPointGroupInfo(DataSet ds) throws Exception;
	public ArrayList<HashMap<String, Object>> getDifferentWeldPoint(DataSet ds) throws Exception;
	public ArrayList<HashMap<String, Object>> getFeatureNameUpdateTargetList(DataSet ds) throws Exception;
	public ArrayList<HashMap<String, Object>> getChildren(DataSet ds) throws Exception;
	public HashMap<String, String> getLatestRevision(DataSet ds) throws Exception;
	public String getPreviousRevisionID(DataSet ds) throws Exception;
	public ArrayList<HashMap<String, Object>> getWeldPoints(DataSet ds) throws Exception;
	public ArrayList<HashMap<String, Object>> getRemovedWeldPoint(DataSet ds) throws Exception;
	public ArrayList<HashMap<String, Object>> getAddedWeldPoint(DataSet ds) throws Exception;
	public HashMap<String, Object> getEcoEplInfo(DataSet ds) throws Exception;
	public void deleteWeldPointGroupPreRevision(DataSet ds) throws Exception;	
	public void insertWeldPointGroupPreRevision(DataSet ds) throws Exception;	
	public ArrayList<HashMap<String, Object>> getDifferentWeldPointUp(DataSet ds) throws Exception;
	public ArrayList<HashMap<String, Object>> getRemovedWeldPointUp(DataSet ds) throws Exception;
	public ArrayList<HashMap<String, Object>> getAddedWeldPointUp(DataSet ds) throws Exception;
	public void updateWeldPointTransLog(DataSet ds) throws Exception;	
}
