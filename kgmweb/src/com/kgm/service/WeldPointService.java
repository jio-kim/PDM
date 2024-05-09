package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.WeldPointDao;

/**
 * [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
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
	 * ������ ���� �׷� ���� �������� ������ DB �� �ʱ�ȭ��.
	 * @param ds
	 * @throws Exception
	 */
	public void deleteWeldPointGroupPreRevision(DataSet ds) throws Exception{
		WeldPointDao dao = new WeldPointDao();
		dao.deleteWeldPointGroupPreRevision(ds);
	}

	/**
	 * ���Ӱ� ������ ���� �׷�� ���ϱ� ���Ͽ� ���� �׷� ���� �������� ������ DB �� ������.
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
	 * [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
	 */
	public void updateWeldPointTransLog(DataSet ds) throws Exception{
		
		WeldPointDao dao = new WeldPointDao();
		dao.updateWeldPointTransLog(ds);
	}
	
}
