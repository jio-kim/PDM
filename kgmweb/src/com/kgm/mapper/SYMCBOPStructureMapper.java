package com.kgm.mapper;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;

/**
 * [NONE_SR][20160126] taeku.jeong BOP Structure 정보를 관리할 목적으로 만든 Class
 * @author Taeku
 *
 */
public interface SYMCBOPStructureMapper {
	
	public void saveShopStructureData(DataSet dataSet);
	
    public void deleteOldShopStructureData(DataSet dataSet);

    public List<HashMap> getAllStationCount(DataSet dataSet);
    
    public List<HashMap> getPredecessorLines(DataSet dataSet);

    public List<HashMap> getPredecessorStationsAtLine(DataSet dataSet);
    
    public List<HashMap> getPredecessorStationsAtAllLine(DataSet dataSet);    

    public List<HashMap> getUnPertedStationList(DataSet dataSet);

	public List<HashMap> keyCodeListFind(DataSet dataSet);

	public List<HashMap> getLatestKeyCodeForShop(DataSet dataSet);
}
