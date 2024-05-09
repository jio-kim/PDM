package com.kgm.service;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.SYMCActivityDao;

public class SYMCActivityService {

    private SYMCActivityDao dao;

    public String getTimeStamp(DataSet ds) {
        dao = new SYMCActivityDao();
        return dao.getTimeStamp(ds);
    }

    public boolean updateTimeStamp(DataSet ds) {
        dao = new SYMCActivityDao();
        return dao.updateTimeStamp(ds);
    }

    public boolean updateEnglishName(DataSet ds) {
        dao = new SYMCActivityDao();
        return dao.updateEnglishName(ds);
    }

    public boolean mergeTimeStamp(DataSet ds) {
        dao = new SYMCActivityDao();
        return dao.mergeTimeStamp(ds);
    }

}
