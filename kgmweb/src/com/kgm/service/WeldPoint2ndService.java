package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.WeldPoint2ndDao;
import com.kgm.dao.WeldPointDao;

public class WeldPoint2ndService {
	
	public void deleteWeldPointRawData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.deleteWeldPointRawData(ds);
	}

	public void insertWeldPointRawDataRow(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.insertWeldPointRawDataRow(ds);
	}
	
	public void deleteWeldPointRaw2Data(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.deleteWeldPointRaw2Data(ds);
	}

	public void makeArrangedStartPointData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.makeArrangedStartPointData(ds);
	}
	
	public void updateArrangedStartPointDataScaling(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.updateArrangedStartPointDataScaling(ds);
	}

	public ArrayList<HashMap<String, Object>> getECOId(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		return dao.getECOId(ds);
	}
	
	public void deleteCurrentSavedData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.deleteCurrentSavedData(ds);
	}
	
	public void deleteCurrentInboundData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.deleteCurrentInboundData(ds);
	}
	
	public void makeInBoundData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.makeInBoundData(ds);
	}

	public void makeEndDiffData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.makeEndDiffData(ds);
	}
	
	public void makeSaveDataForDelete(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.makeSaveDataForDelete(ds);
	}	
	
	public void makeSaveDataForInBound(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.makeSaveDataForInBound(ds);
	}
	
	public void makeSaveDataForEndDiff(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.makeSaveDataForEndDiff(ds);
	}
	
	public void makeSaveDataForAdd(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.makeSaveDataForAdd(ds);
	}
	
	public void deleteBOMWeldPointData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.deleteBOMWeldPointData(ds);
	}

	public void insertBOMWeldPointDataRow(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.insertBOMWeldPointDataRow(ds);
	}

	public void deleteBOMWeldPoint2Data(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.deleteBOMWeldPoint2Data(ds);
	}

	public void makeBOMArrangedStartPointData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		dao.makeBOMArrangedStartPointData(ds);
	}
	
	public ArrayList<HashMap<String, Object>> findHaveSameEcoWeldGroupRevisionData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		return dao.findHaveSameEcoWeldGroupRevisionData(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getECOMatchedFMPRevision(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		return dao.getECOMatchedFMPRevision(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getDeleteTargetBOMLineData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		return dao.getDeleteTargetBOMLineData(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getAddTargetWeldPointData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		return dao.getAddTargetWeldPointData(ds);
	}

	public  ArrayList<HashMap<String, Object>> getMaxOccSeqNo(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		return dao.getMaxOccSeqNo(ds);
	}
	
	public  ArrayList<HashMap<String, Object>> getAllNewBOMLineCount(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		return dao.getAllNewBOMLineCount(ds);
	}	
	
	public  ArrayList<HashMap<String, Object>> getUpdateTargetBOMLineData(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		return dao.getUpdateTargetBOMLineData(ds);
	}
	
	public  ArrayList<HashMap<String, Object>> getChildNodeWeldTypeList(DataSet ds) throws Exception{
		WeldPoint2ndDao dao = new WeldPoint2ndDao();
		return dao.getChildNodeWeldTypeList(ds);
	}
	
}
