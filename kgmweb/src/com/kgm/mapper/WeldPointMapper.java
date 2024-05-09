package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

/**
 * [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
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
