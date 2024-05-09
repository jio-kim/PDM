package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

public interface SYMCSubsidiaryMapper {
	public ArrayList<HashMap<String, String>> searchSubsidiary(DataSet ds);
}
