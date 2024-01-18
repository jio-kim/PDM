package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.NoticeDao;

public class NoticeService {

    private NoticeDao dao;

    public NoticeService() {

    }

    public boolean insertNotice(DataSet dataSet) {
        dao = new NoticeDao();

        return dao.insertNotice(dataSet);
    }

    public boolean insertNoticeContents(DataSet dataSet) {
        dao = new NoticeDao();

        return dao.insertNoticeContents(dataSet);
    }

    public boolean updateNotice(DataSet dataSet) {
        dao = new NoticeDao();

        return dao.updateNotice(dataSet);
    }

    public boolean deleteNotice(DataSet dataSet) {
        dao = new NoticeDao();

        return dao.deleteNotice(dataSet);
    }

    public boolean deleteNoticeContents(DataSet dataSet) {
        dao = new NoticeDao();

        return dao.deleteNoticeContents(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectNoticeList() {
        dao = new NoticeDao();

        return dao.selectNoticeList();
    }

    public ArrayList<HashMap<String, Object>> selectNoticeContentsList(DataSet dataSet) {
        dao = new NoticeDao();

        return dao.selectNoticeContentsList(dataSet);
    }

    public ArrayList<HashMap<String, Object>> selectPopUpList() {
        dao = new NoticeDao();

        return dao.selectPopUpList();
    }

    public HashMap<String, Object> selectNextOUID() {
        dao = new NoticeDao();

        return dao.selectNextOUID();
    }

}
