package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

public interface GetParentMapper {
	public ArrayList<HashMap<String, String>> searchUpperBOM(DataSet ds);
	public ArrayList<HashMap<String, String>> searchAll(DataSet ds);
	public ArrayList<HashMap<String, String>> searchLatestReleased(DataSet ds);
	public ArrayList<HashMap<String, String>> searchLatestWorking(DataSet ds);
	public ArrayList<HashMap<String, String>> isConnectedFunction(DataSet ds);
	public ArrayList<HashMap<String, Object>> whereUsedStructure(DataSet ds);
	public ArrayList<HashMap<String, Object>> whereUsedPreBOMStructure(DataSet ds);
}
