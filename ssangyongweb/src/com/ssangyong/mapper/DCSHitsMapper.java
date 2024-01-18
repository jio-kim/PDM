package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface DCSHitsMapper {

	public void insertDCSHits(DataSet dataSet);

	public ArrayList<HashMap<String, Object>> selectDCSHits(DataSet dataSet);

	public ArrayList<HashMap<String, Object>> selectDetailDCSHits(DataSet dataSet);

	public int getDCSWorkflowHistoryMaxSeq();
	
	public int isExistsDCSWorkflowHistory(DataSet dataSet);
	
	public void insertDCSWorkflowHistory(DataSet dataSet);

	public void updDCSWorkflowHistory(DataSet dataSet);
}