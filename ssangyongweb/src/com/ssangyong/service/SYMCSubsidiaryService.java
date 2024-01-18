package com.ssangyong.service;

import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.SYMCSubsidiaryDao;

public class SYMCSubsidiaryService {

    public List<HashMap<String, String>> serchSubsidiary(DataSet ds) throws Exception {
        SYMCSubsidiaryDao dao = new SYMCSubsidiaryDao();
        return dao.serchSubsidiary(ds);
    }

}
