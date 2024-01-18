package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface DCSNoticeMapper {

	public void insertDCSNotice(DataSet dataSet);

	public void insertDCSNoticeContents(DataSet dataSet);

	public void updateDCSNotice(DataSet dataSet);

	public void updateDCSNoticeContents(DataSet dataSet);

	public void deleteDCSNotice(DataSet dataSet);

	public void deleteDCSNoticeContents(DataSet dataSet);

	public ArrayList<HashMap<String, Object>> selectDCSNoticeList();

	public ArrayList<HashMap<String, Object>> selectDCSNoticeContentsList(DataSet dataSet);

	public HashMap<String, Object> selectNextOUID();

}
