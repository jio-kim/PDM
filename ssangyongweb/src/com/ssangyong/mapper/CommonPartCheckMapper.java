package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.CommonPartCheckDao;
import com.ssangyong.dto.ExcludeFromCommonPartInEcoData;
import com.ssangyong.dto.TCBomLineData;
import com.ssangyong.dto.TCEcoModel;
import com.ssangyong.dto.TCPartModel;

/**
 * [SR180329-023][20180411] csh
 * 공용부품 품번변경 누락 검토기능
 */
public interface CommonPartCheckMapper {

	public List<TCEcoModel> getEcoList(DataSet ds) throws Exception ;
	
	public List<TCPartModel> getOldPartListWithN1 (DataSet ds)  throws Exception ;
	
	public List<TCBomLineData> getCommonPartCheckReport (DataSet ds)  throws Exception ;
	
	public List<ExcludeFromCommonPartInEcoData> getExcludePartData (DataSet ds)  throws Exception ;
	
	public void updateExcludePartData(ExcludeFromCommonPartInEcoData data)  throws Exception;
	
	public void insertExcludePartData(ExcludeFromCommonPartInEcoData data)  throws Exception;
	
	public void deleteExcludePartData(ExcludeFromCommonPartInEcoData data)  throws Exception;
	 
	public ArrayList<String> getFunctionList(DataSet ds) throws Exception ;
	 
	public ArrayList<String> getChildList(DataSet ds) throws Exception ;
	 
	public void updateNmcd(DataSet ds)  throws Exception;
	 
	public void insertNmcd(DataSet ds)  throws Exception;
	 
	public void deleteNmcd(DataSet ds)  throws Exception;
	 
	public void mergeNmcd(DataSet ds)  throws Exception;
	 
	public ArrayList<String> getEplList(DataSet ds) throws Exception ;
	
	public ArrayList<String> getVnetTeamNameK(DataSet ds) throws Exception ;
	 
	public Integer createReport(DataSet ds);
    
}
