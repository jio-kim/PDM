package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.AddfileDao;

/**
 * [SR150421-027][20150811][ymjang] PLM system �������� - Manual ��ȸ ������� �߰�
 */
public class AddfileService {

    private AddfileDao dao;

    public AddfileService() {

    }

    public boolean insertAddfile(DataSet dataSet) {
        dao = new AddfileDao();

        return dao.insertAddfile(dataSet);
    }

    public boolean deleteAddfile(DataSet dataSet) {
        dao = new AddfileDao();

        return dao.deleteAddfile(dataSet);
    }

    public boolean updAddfile(DataSet dataSet) {
        dao = new AddfileDao();

        return dao.updAddfile(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectAddfileList(DataSet dataSet) {
        dao = new AddfileDao();

        return dao.selectAddfileList(dataSet);
    }

    public int selectMaxSeq(DataSet dataSet) {
        dao = new AddfileDao();

        return dao.selectMaxSeq(dataSet);
    }

}
