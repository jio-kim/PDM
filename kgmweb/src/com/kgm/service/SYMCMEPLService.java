package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.SYMCMEPLDao;
import com.kgm.dto.EndItemData;
import com.kgm.rac.kernel.SYMCBOPEditData;

public class SYMCMEPLService {

	private SYMCMEPLDao dao;
	
	public ArrayList<EndItemData> findReplacedEndItems(DataSet ds) {
		dao = new SYMCMEPLDao();
        return dao.findReplacedEndItems(ds);
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getChangedStructureCompareResultList(DataSet ds) {
		dao = new SYMCMEPLDao();
        return dao.getChangedStructureCompareResultList(ds);
	}
	
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getItemRevisionList(DataSet ds) {
		dao = new SYMCMEPLDao();
        return dao.getItemRevisionList(ds);
	}
	
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getOperationEPLCrationDate(DataSet ds) {
		dao = new SYMCMEPLDao();
        return dao.getOperationEPLCrationDate(ds);
	}
	
	public void deleteOperationEPL (DataSet ds) {
		dao = new SYMCMEPLDao();
		dao.deleteOperationEPL(ds);
	}
	
	public void insertOperationMECOEPL (DataSet ds) {
		dao = new SYMCMEPLDao();
		dao.insertOperationMECOEPL(ds);
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getMissingMEPLObjectList(DataSet ds) {
		dao = new SYMCMEPLDao();
        return dao.getMissingMEPLObjectList(ds);
	}
	
	public ArrayList<HashMap> getMEPLResultList(DataSet ds) {
		dao = new SYMCMEPLDao();
        return dao.getMEPLResultList(ds);
	}

	public ArrayList<HashMap> getBOPChildErrorList(DataSet ds) {
		dao = new SYMCMEPLDao();
        return dao.getBOPChildErrorList(ds);
	}
}
