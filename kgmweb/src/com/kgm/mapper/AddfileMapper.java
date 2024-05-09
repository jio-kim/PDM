package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

/**
 * [SR150421-027][20150811][ymjang] PLM system �������� - Manual ��ȸ ������� �߰�
 */
public interface AddfileMapper {

    public void insertAddfile(DataSet dataSet);

    public void deleteAddfileAll(DataSet dataSet);

    public void deleteAddfile(DataSet dataSet);
    
    public void updAddfile(DataSet dataSet);
    
    public ArrayList<HashMap<String, Object>> selectAddfileList(DataSet dataSet);

    public int selectMaxSeq(DataSet dataSet);
    
}
