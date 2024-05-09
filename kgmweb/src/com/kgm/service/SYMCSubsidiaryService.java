package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.SYMCSubsidiaryDao;

public class SYMCSubsidiaryService {

    public List<HashMap<String, String>> serchSubsidiary(DataSet ds) throws Exception {
        SYMCSubsidiaryDao dao = new SYMCSubsidiaryDao();
        return dao.serchSubsidiary(ds);
    }

}
