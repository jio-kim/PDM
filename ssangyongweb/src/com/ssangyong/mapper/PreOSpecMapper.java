package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface PreOSpecMapper {
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatory(DataSet ds);
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatoryInfo(DataSet ds);
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatoryTrim(DataSet ds);
	public void insertPreOSpecMandatoryInfo(DataSet ds);
	public void insertPreOSpecMandatoryTrim(DataSet ds);
	public void deletePreOSpecMandatoryInfo(DataSet ds);
	public void deletePreOSpecMandatoryTrim(DataSet ds);
}