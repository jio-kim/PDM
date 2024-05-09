package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

public interface ECOAdminCheckMapper
{
	public ArrayList<HashMap<String, Object>> getCheckList(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getMonthlyAllVehicleECOStatus(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getMonthlyAllVehicleEndItemStatus(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getMonthlyVehicleECOStatus(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getMonthlyEngineECOStatus(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getEcoStatusByTeam(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getMonthlyVehicleECOAnalysis(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getMonthlyEngineECOAnalysis(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getLOVData(DataSet ds) throws Exception;

	public ArrayList<HashMap<String, Object>> getSYMCSubGroup() throws Exception;
	
	public ArrayList<String> getYear() throws Exception;
	
	public ArrayList<HashMap<String, Object>> getEngineList() throws Exception;
}
