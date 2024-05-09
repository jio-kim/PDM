package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.PreBOMUssageExportDao;

/**
 * [SR160621-031][20160707] taeku.jeong
 * 주간 단위로 생성된 Pre-BOM 데이터를 활용하여 엑셀로 출력할 수 있는 기능 개발
 */
public class PreBOMUssageExportService {

	private PreBOMUssageExportDao dao;
	
    public List<HashMap> getExportTargetProjectList(DataSet ds){
		dao = new PreBOMUssageExportDao();
        return dao.getExportTargetProjectList(ds);
    }

    public List<HashMap> geProjectUssageHeaderList(DataSet ds){
		dao = new PreBOMUssageExportDao();
        return dao.geProjectUssageHeaderList(ds);
    }
    
    public List<HashMap> geProjectMasterDataList(DataSet ds){
		dao = new PreBOMUssageExportDao();
        return dao.geProjectMasterDataList(ds);
    }
    
    public List<HashMap> geProjectUssageDataList(DataSet ds){
		dao = new PreBOMUssageExportDao();
        return dao.geProjectUssageDataList(ds);
    }
    
    public void updateCost(DataSet ds) throws Exception{
    	dao = new PreBOMUssageExportDao();
		dao.updateCost(ds);		
	}
    
    public String getEaiDate(DataSet ds) throws Exception{
    	dao = new PreBOMUssageExportDao();
    	return dao.getEaiDate(ds);
    }
}
