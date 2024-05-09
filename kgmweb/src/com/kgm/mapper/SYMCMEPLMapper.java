package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dto.EndItemData;
import com.kgm.rac.kernel.SYMCBOPEditData;

public interface SYMCMEPLMapper {

	public ArrayList<EndItemData> findReplacedEndItems(DataSet ds);
	
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getChangedStructureCompareResultList(DataSet ds);
	
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getItemRevisionList(DataSet ds);
	
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getOperationEPLCrationDate(DataSet ds);
	
	public void deleteOperationEPL (DataSet ds);
	
	public void insertOperationMECOEPL (DataSet ds);
	
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap> getMissingMEPLObjectList(DataSet ds);
	
	public ArrayList<HashMap> getMEPLResultList(DataSet ds);

	public ArrayList<HashMap> getBOPChildErrorList(DataSet ds);
}
