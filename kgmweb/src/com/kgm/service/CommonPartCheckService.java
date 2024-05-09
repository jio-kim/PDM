package com.kgm.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.dao.CommonPartCheckDao;
import com.kgm.dto.ExcludeFromCommonPartInEcoData;
import com.kgm.dto.TCBomLineData;
import com.kgm.dto.TCEcoModel;
import com.kgm.dto.TCPartModel;
import com.teamcenter.rac.kernel.TCException;

/**
 * [SR180329-023][20180411] csh
 * 공용부품 품번변경 누락 검토기능
 */
public class CommonPartCheckService {

	private CommonPartCheckDao dao;
	
    public List<TCEcoModel> getEcoList(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        return dao.getEcoList(ds);
    }
    
    public List<TCPartModel> getOldPartListWithN1(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        return dao.getOldPartListWithN1(ds);
    }
    
    public List<TCBomLineData> getCommonPartCheckReport (DataSet ds)  throws Exception {
    	dao = new CommonPartCheckDao();
        return dao.getCommonPartCheckReport(ds);
    }
    
    public List<ExcludeFromCommonPartInEcoData> getExcludePartData (DataSet ds)  throws Exception{
    	dao = new CommonPartCheckDao();
        return dao.getExcludePartData(ds);
    }
    
    public void saveExcludePartData ( DataSet ds  ) throws Exception{
    	
    	List<ExcludeFromCommonPartInEcoData> dataList = (List<ExcludeFromCommonPartInEcoData>) ds.get("saveData");
    	
    	dao = new CommonPartCheckDao();
    	dao.saveExcludePartData( dataList);
    	
    }
    
    public void createReport ( DataSet ds  ) throws Exception{
    	
    	dao = new CommonPartCheckDao();
    	dao.createReport( ds );
    	
    }
    
    public ArrayList<String> getFunctionList(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        return dao.getFunctionList(ds);
    }
    
    public ArrayList<String> getChildList(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        return dao.getChildList(ds);
    }
    
    public void updateNmcd(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        dao.updateNmcd(ds);
    }

    public void insertNmcd(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        dao.insertNmcd(ds);
    }
    
    public void deleteNmcd(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        dao.deleteNmcd(ds);
    }
    
    public void mergeNmcd(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        dao.mergeNmcd(ds);
    }
    
    public ArrayList<String> getEplList(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        return dao.getEplList(ds);
    }
    
    public ArrayList<String> getVnetTeamNameK(DataSet ds) throws Exception {
		dao = new CommonPartCheckDao();
        return dao.getVnetTeamNameK(ds);
    }
    
}
