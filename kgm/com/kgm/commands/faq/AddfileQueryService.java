package com.kgm.commands.faq;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;

/**
 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
 */
public class AddfileQueryService {

    private SYMCRemoteUtil remoteUtil;

    private final String ADDFILESERVICECLASS = "com.kgm.service.AddfileService";

    public AddfileQueryService() {
        remoteUtil = new SYMCRemoteUtil();
    }

    public Boolean insertAddfile(DataSet dataSet) {
        Object object = null;

        try {
            object = remoteUtil.execute(ADDFILESERVICECLASS, "insertAddfile", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (Boolean) object;
    }

    public Boolean deleteAddfile(DataSet dataSet) {
        Object object = null;

        try {
            object = remoteUtil.execute(ADDFILESERVICECLASS, "deleteAddfile", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (Boolean) object;
    }

    public Boolean updAddfile(DataSet dataSet) {
        Object object = null;

        try {
            object = remoteUtil.execute(ADDFILESERVICECLASS, "updAddfile", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (Boolean) object;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, Object>> selectAddfileList(DataSet dataSet) {
    	ArrayList<HashMap<String, Object>> addfileList = null;

        try {
            addfileList = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(ADDFILESERVICECLASS, "selectAddfileList", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return addfileList;
    }

    @SuppressWarnings("unchecked")
    public int selectMaxSeq(DataSet dataSet) {
        int maxSeq = 0;

        try {
        	maxSeq = (Integer) remoteUtil.execute(ADDFILESERVICECLASS, "selectMaxSeq", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return maxSeq;
    }
    
}
