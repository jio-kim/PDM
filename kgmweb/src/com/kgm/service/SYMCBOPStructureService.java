package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.SYMCBOPStructureDao;

/**
 * [NONE_SR][20160126] taeku.jeong BOP Structure 정보를 관리할 목적으로 만든 Class
 * @author Taeku
 *
 */
public class SYMCBOPStructureService {

	private SYMCBOPStructureDao dao;

    public boolean saveShopStructureData(DataSet dataSet) {
        dao = new SYMCBOPStructureDao();
        return dao.saveShopStructureData(dataSet);
    }
    
    public boolean deleteOldShopStructureData(DataSet dataSet) {
        dao = new SYMCBOPStructureDao();
        return dao.deleteOldShopStructureData(dataSet);
    }

    public List<HashMap> getAllStationCount(DataSet dataSet){
		dao = new SYMCBOPStructureDao();
        return dao.getAllStationCount(dataSet);
    }
    
    public List<HashMap> getPredecessorLines(DataSet dataSet){
		dao = new SYMCBOPStructureDao();
        return dao.getPredecessorLines(dataSet);
    }
    
    public List<HashMap> getPredecessorStationsAtLine(DataSet dataSet){
		dao = new SYMCBOPStructureDao();
        return dao.getPredecessorStationsAtLine(dataSet);
    }
    
    public List<HashMap> getPredecessorStationsAtAllLine(DataSet dataSet){
		dao = new SYMCBOPStructureDao();
        return dao.getPredecessorStationsAtAllLine(dataSet);
    }
    
    public List<HashMap> getUnPertedStationList(DataSet dataSet){
		dao = new SYMCBOPStructureDao();
        return dao.getUnPertedStationList(dataSet);
    }    
    
	public List<HashMap> keyCodeListFind(DataSet dataSet){
		dao = new SYMCBOPStructureDao();
        return dao.keyCodeListFind(dataSet);
    }

	public List<HashMap> getLatestKeyCodeForShop(DataSet dataSet){
		dao = new SYMCBOPStructureDao();
        return dao.getLatestKeyCodeForShop(dataSet);
    }
	
}
