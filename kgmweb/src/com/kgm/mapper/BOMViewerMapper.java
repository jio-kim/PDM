package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

public interface BOMViewerMapper {
    public ArrayList<HashMap<String, Object>> selectBOMViewer(DataSet dataSet);
}
