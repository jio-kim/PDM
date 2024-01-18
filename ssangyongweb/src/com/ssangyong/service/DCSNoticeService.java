package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.DCSNoticeDao;

public class DCSNoticeService {

	private DCSNoticeDao dao;

	public DCSNoticeService() {

	}

	public boolean insertDCSNotice(DataSet dataSet) {
		dao = new DCSNoticeDao();

		return dao.insertDCSNotice(dataSet);
	}

	public boolean insertDCSNoticeContents(DataSet dataSet) {
		dao = new DCSNoticeDao();

		return dao.insertDCSNoticeContents(dataSet);
	}

	public boolean updateDCSNotice(DataSet dataSet) {
		dao = new DCSNoticeDao();

		return dao.updateDCSNotice(dataSet);
	}

	public boolean updateDCSNoticeContents(DataSet dataSet) {
		dao = new DCSNoticeDao();

		return dao.updateDCSNoticeContents(dataSet);
	}

	public boolean deleteDCSNotice(DataSet dataSet) {
		dao = new DCSNoticeDao();

		return dao.deleteDCSNotice(dataSet);
	}

	public boolean deleteDCSNoticeContents(DataSet dataSet) {
		dao = new DCSNoticeDao();

		return dao.deleteDCSNoticeContents(dataSet);
	}

	public ArrayList<HashMap<String, Object>> selectDCSNoticeList(DataSet dataSet) {
		dao = new DCSNoticeDao();

		return dao.selectDCSNoticeList();
	}

	public ArrayList<HashMap<String, Object>> selectDCSNoticeList() {
		dao = new DCSNoticeDao();

		return dao.selectDCSNoticeList();
	}
	
	public ArrayList<HashMap<String, Object>> selectDCSNoticeContentsList(DataSet dataSet) {
		dao = new DCSNoticeDao();

		return dao.selectDCSNoticeContentsList(dataSet);
	}

	public HashMap<String, Object> selectNextOUID() {
		dao = new DCSNoticeDao();

		return dao.selectNextOUID();
	}

}
