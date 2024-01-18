package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.WeldPointDao;

/**
 * [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
 */
public class WeldPointService {

	public void updateDateReleasedWithEco(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		dao.updateDateReleasedWithEco(ds);
	}

	public void insertWeldPointGroupInfo(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		dao.insertWeldPointGroupInfo(ds);
	}

	public ArrayList<HashMap<String, Object>> getDifferentWeldPoint(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getDifferentWeldPoint(ds);
	}

	public ArrayList<HashMap<String, Object>> getFeatureNameUpdateTargetList(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getFeatureNameUpdateTargetList(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getChildren(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getChildren(ds);
	}

	public HashMap<String, String> getLatestRevision(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getLatestRevision(ds);
	}

	public String getPreviousRevisionID(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getPreviousRevisionID(ds);
	}

	public ArrayList<HashMap<String, Object>> getWeldPoints(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getWeldPoints(ds);
	}

	public ArrayList<HashMap<String, Object>> getRemovedWeldPoint(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getRemovedWeldPoint(ds);
	}

	public ArrayList<HashMap<String, Object>> getAddedWeldPoint(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getAddedWeldPoint(ds);
	}

	public HashMap<String, Object> getEcoEplInfo(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getEcoEplInfo(ds);
	}

	/**
	 * 생성된 용접 그룹 이전 리비전의 정보를 DB 에 초기화함.
	 * @param ds
	 * @throws Exception
	 */
	public void deleteWeldPointGroupPreRevision(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		dao.deleteWeldPointGroupPreRevision(ds);
	}

	/**
	 * 새롭게 생성될 용접 그룹과 비교하기 위하여 용접 그룹 이전 리비전의 정보를 DB 에 저장함.
	 * @param ds
	 * @throws Exception
	 */
	public void insertWeldPointGroupPreRevision(DataSet ds) throws Exception{
		
		WeldPointDao dao = new WeldPointDao();
		dao.insertWeldPointGroupPreRevision(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getDifferentWeldPointUp(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getDifferentWeldPointUp(ds);
	}

	public ArrayList<HashMap<String, Object>> getRemovedWeldPointUp(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getRemovedWeldPointUp(ds);
	}

	public ArrayList<HashMap<String, Object>> getAddedWeldPointUp(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		return dao.getAddedWeldPointUp(ds);
	}

	/**
	 * [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
	 */
	public void updateWeldPointTransLog(DataSet ds) throws Exception{
		
		WeldPointDao dao = new WeldPointDao();
		dao.updateWeldPointTransLog(ds);
	}
	
}
