package com.kgm.mapper;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;

/**
 * [SR160621-031][20160707] taeku.jeong
 * 주간 단위로 생성된 Pre-BOM 데이터를 활용하여 엑셀로 출력할 수 있는 기능 개발
 */
public interface PreBOMUssageExportMapper {

	public List<HashMap> getExportTargetProjectList(DataSet ds);
    
	public List<HashMap> geProjectUssageHeaderList(DataSet ds);
    
	public List<HashMap> geProjectMasterDataList(DataSet ds);
    
	public List<HashMap> geProjectUssageDataList(DataSet ds);
	
	public void updateCost(DataSet ds);
	
	public String getEaiDate(DataSet ds);
}
