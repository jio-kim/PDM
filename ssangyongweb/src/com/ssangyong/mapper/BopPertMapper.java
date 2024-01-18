package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface BopPertMapper {

    public ArrayList<HashMap<String, Object>> selectBopPertList(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectStationDecessorsList(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectBopEndItemList(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectBopVehpartList(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectBopStationPertList(DataSet dataSet);
    
    public ArrayList<HashMap<String, Object>> selectBopStationPertCountList(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectBopStationCount(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectBopStationDecessorsList(DataSet dataSet);

    public void insertBopStationDecessorsInfo(DataSet dataSet);

    public void updateBopPertInfo(DataSet dataSet);

    public void deleteBopPertInfo(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectBopStationDecessorsLastModDateList(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectBopStationDecessorsEndItemList(DataSet dataSet);

}
