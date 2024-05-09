package com.kgm.mapper;

import com.kgm.common.remote.DataSet;

public interface SYMCActivityMapper {

    public String getTimeStamp(DataSet ds);
    public int updateTimeStamp(DataSet ds);
    public int updateEnglishName(DataSet ds);
    public int mergeTimeStamp(DataSet ds);

}
