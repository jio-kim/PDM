package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface NoticeMapper {

    public void insertNotice(DataSet dataSet);

    public void insertNoticeContents(DataSet dataSet);

    public void updateNotice(DataSet dataSet);

    public void deleteNotice(DataSet dataSet);

    public void deleteNoticeContents(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectNoticeList();

    public ArrayList<HashMap<String, Object>> selectNoticeContentsList(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectPopUpList();

    public HashMap<String, Object> selectNextOUID();

}
