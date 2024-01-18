package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface BOMViewerMapper {
    public ArrayList<HashMap<String, Object>> selectBOMViewer(DataSet dataSet);
}
