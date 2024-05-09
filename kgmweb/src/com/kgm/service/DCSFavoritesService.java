package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.DCSFavoritesDao;

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
