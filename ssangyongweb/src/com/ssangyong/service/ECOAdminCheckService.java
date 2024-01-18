package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.ECOAdminCheckDao;

public class ECOAdminCheckService
{
	public ArrayList<HashMap<String, Object>> getCheckList(DataSet ds) throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getCheckList(ds);
	}

	public ArrayList<HashMap<String, Object>> getMonthlyAllVehicleECOStatus(DataSet ds) throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getMonthlyAllVehicleECOStatus(ds);
	}

	public ArrayList<HashMap<String, Object>> getMonthlyAllVehicleEndItemStatus(DataSet ds) throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getMonthlyAllVehicleEndItemStatus(ds);
	}

	public ArrayList<HashMap<String, Object>> getMonthlyVehicleECOStatus(DataSet ds) throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getMonthlyVehicleECOStatus(ds);
	}

	public ArrayList<HashMap<String, Object>> getMonthlyEngineECOStatus(DataSet ds) throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getMonthlyEngineECOStatus(ds);
	}

	public ArrayList<HashMap<String, Object>> getEcoStatusByTeam(DataSet ds) throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getEcoStatusByTeam(ds);
	}

	public ArrayList<HashMap<String, Object>> getMonthlyVehicleECOAnalysis(DataSet ds) throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getMonthlyVehicleECOAnalysis(ds);
	}

	public ArrayList<HashMap<String, Object>> getMonthlyEngineECOAnalysis(DataSet ds) throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getMonthlyEngineECOAnalysis(ds);
	}

	public ArrayList<HashMap<String, Object>> getLOVData(DataSet ds) throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getLOVData(ds);
	}

	public ArrayList<HashMap<String, Object>> getSYMCSubGroup() throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getSYMCSubGroup();
	}
	
	public ArrayList<String> getYear() throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getYear();
	}
	
	public ArrayList<HashMap<String, Object>> getEngineList() throws Exception
	{
		ECOAdminCheckDao dao = new ECOAdminCheckDao();
		return dao.getEngineList();
	}
}
