package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

public interface MbomInterfaceMapper {

	public int insertBpnInfo(DataSet ds) throws Exception;
	public void updatePgInfo(DataSet ds) throws Exception;
    public ArrayList<HashMap<String, String>> searchProcessSheet(DataSet ds);
}
