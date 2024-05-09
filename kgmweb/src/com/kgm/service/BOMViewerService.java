package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.BOMViewerDao;

public class BOMViewerService {
	BOMViewerDao dao;
	
	public ArrayList<HashMap<String, Object>> selectBOMViewer(DataSet dataSet) {
        dao = new BOMViewerDao();
        return dao.selectBOMViewer(dataSet);
    }
}
