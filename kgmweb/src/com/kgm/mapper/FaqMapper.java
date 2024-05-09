package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

/**
 * [SR150421-027][20150811][ymjang] PLM system 
 */
public interface FaqMapper {

    public void insertFaq(DataSet dataSet);

    public void updateFaqSeq(DataSet dataSet);
    
    public void updateFaq(DataSet dataSet);

    public void deleteFaq(DataSet dataSet);

    public ArrayList<HashMap<String, Object>> selectFaqList(DataSet dataSet);

    public HashMap<String, Object> selectNextOUID();

}
