package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.FunctionDao;

/**
 * [SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가를 위한 Class 신규 생성
 */
public class FunctionService {

    public List<HashMap<String, String>> serchProductFunction(DataSet ds) throws Exception {
        FunctionDao dao = new FunctionDao();
        return dao.serchProductFunction(ds);
    }

}
