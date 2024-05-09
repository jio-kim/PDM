package com.kgm.mapper;

import java.util.ArrayList;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.CommonPartCheckDao;
import com.kgm.dto.ExcludeFromCommonPartInEcoData;
import com.kgm.dto.TCBomLineData;
import com.kgm.dto.TCEcoModel;
import com.kgm.dto.TCPartModel;

/**
 * [SR180329-023][20180411] csh
 * �����ǰ ǰ������ ���� ������
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
