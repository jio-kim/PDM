package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.BopPertDao;

public class BopPertService {

    private BopPertDao dao;

    public BopPertService() {

    }

    public ArrayList<HashMap<String, Object>> selectBopPertList(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.selectBopPertList(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectStationDecessorsList(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.selectStationDecessorsList(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectBopEndItemList(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.selectBopEndItemList(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectBopVehpartList(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.selectBopVehpartList(dataSet);
    }
    
    public ArrayList<HashMap<String, Object>> selectBopStationPertList(DataSet dataSet) {
        dao = new BopPertDao();
        
        return dao.selectBopStationPertList(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectBopStationPertCountList(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.selectBopStationPertCountList(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectBopStationCount(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.selectBopStationCount(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectBopStationDecessorsList(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.selectBopStationDecessorsList(dataSet);
    }

    public boolean insertBopStationDecessorsInfo(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.insertBopStationDecessorsInfo(dataSet);
    }

    public boolean updateBopPertInfo(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.updateBopPertInfo(dataSet);
    }

    public boolean deleteBopPertInfo(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.deleteBopPertInfo(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectBopStationDecessorsLastModDateList(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.selectBopStationDecessorsLastModDateList(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectBopStationDecessorsEndItemList(DataSet dataSet) {
        dao = new BopPertDao();

        return dao.selectBopStationDecessorsEndItemList(dataSet);
    }

}
