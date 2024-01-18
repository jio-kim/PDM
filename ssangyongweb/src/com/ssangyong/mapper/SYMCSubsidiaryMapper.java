package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface SYMCSubsidiaryMapper {
	public ArrayList<HashMap<String, String>> searchSubsidiary(DataSet ds);
}
