package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.BOMViewerDao;

public class BOMViewerService {
	BOMViewerDao dao;
	
	public ArrayList<HashMap<String, Object>> selectBOMViewer(DataSet dataSet) {
        dao = new BOMViewerDao();
        return dao.selectBOMViewer(dataSet);
    }
}
