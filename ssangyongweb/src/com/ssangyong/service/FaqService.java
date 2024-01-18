package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.FaqDao;

/**
 * [SR150421-027][20150811][ymjang] PLM system
 */
public class FaqService {

    private FaqDao dao;

    public FaqService() {

    }

    public boolean insertFaq(DataSet dataSet) {
        dao = new FaqDao();

        return dao.insertFaq(dataSet);
    }

    public boolean updateFaqSeq(DataSet dataSet) {
        dao = new FaqDao();

        return dao.updateFaqSeq(dataSet);
    }

    public boolean updateFaq(DataSet dataSet) {
        dao = new FaqDao();

        return dao.updateFaq(dataSet);
    }

    public boolean deleteFaq(DataSet dataSet) {
        dao = new FaqDao();

        return dao.deleteFaq(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectFaqList(DataSet dataSet) {
        dao = new FaqDao();

        return dao.selectFaqList(dataSet);
    }

    public HashMap<String, Object> selectNextOUID() {
        dao = new FaqDao();

        return dao.selectNextOUID();
    }

}
