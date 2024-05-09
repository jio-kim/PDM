package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

public interface DCSFavoritesMapper {

	public void insertDCSFavoritesTeam(DataSet dataSet);

	public void deleteDCSFavoritesTeam(DataSet dataSet);

	public ArrayList<HashMap<String, Object>> selectDCSFavoritesTeamList(DataSet dataSet);

}