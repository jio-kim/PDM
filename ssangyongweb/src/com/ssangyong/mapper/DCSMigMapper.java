package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface DCSMigMapper {

	public ArrayList<HashMap<String, Object>> selectWorkflowInfoList(DataSet dataSet);

	public ArrayList<HashMap<String, Object>> selectMyWorkflowInfoList(DataSet dataSet);

}
