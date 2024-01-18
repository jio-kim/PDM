package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.DCSFavoritesDao;

public class DCSFavoritesService {

	private DCSFavoritesDao dao;

	public DCSFavoritesService() {

	}

	public boolean insertDCSFavoritesTeam(DataSet dataSet) {
		dao = new DCSFavoritesDao();

		return dao.insertDCSFavoritesTeam(dataSet);
	}

	public boolean deleteDCSFavoritesTeam(DataSet dataSet) {
		dao = new DCSFavoritesDao();

		return dao.deleteDCSFavoritesTeam(dataSet);
	}

	public ArrayList<HashMap<String, Object>> selectDCSFavoritesTeamList(DataSet dataSet) {
		dao = new DCSFavoritesDao();

		return dao.selectDCSFavoritesTeamList(dataSet);
	}

}
