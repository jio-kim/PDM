package com.symc.plm.me.sdv.command.meco.dao;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;

public class CustomActivityDao {

    public static final String ACTIVITY_SERVICE_CLASS = "com.kgm.service.SYMCActivityService";

    private SYMCRemoteUtil remoteQuery;
    private DataSet ds;
    public CustomActivityDao() {
//        this.remoteQuery = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
        this.remoteQuery = new SYMCRemoteUtil();
    }

    public void updateActivityEnglishName(String activityUid, String englishName, String userId, String target) throws Exception {
        ds = new DataSet();
        ds.put("activityUid", activityUid);
        ds.put("englishName", englishName);
        ds.put("userId", userId);
        ds.put("target", target);

        System.out.println("activityUid : " + activityUid);

        String ptimestamp = (String) remoteQuery.execute(ACTIVITY_SERVICE_CLASS, "getTimeStamp", ds);
        if(ptimestamp == null) {
            throw new Exception("Timestamp is null.");
        }

        if(ptimestamp.endsWith("X")) {
            ptimestamp = ptimestamp.substring(0, ptimestamp.length() - 1) + "Z";
        } else {
            ptimestamp = ptimestamp.substring(0, ptimestamp.length() - 1) + "X";
        }

        ds.put("ptimestamp", ptimestamp);

        boolean retVal = (Boolean) remoteQuery.execute(ACTIVITY_SERVICE_CLASS, "updateTimeStamp", ds);
        if(!retVal) {
            throw new Exception("Fail to execute 'updateTimeStamp'.");
        }

        retVal = (Boolean) remoteQuery.execute(ACTIVITY_SERVICE_CLASS, "updateEnglishName", ds);
        if(!retVal) {
            throw new Exception("Fail to execute 'updateEnglishName'.");
        }

        retVal = (Boolean) remoteQuery.execute(ACTIVITY_SERVICE_CLASS, "mergeTimeStamp", ds);
        if(!retVal) {
            throw new Exception("Fail to execute 'mergeTimeStamp'.");
        }
    }

}
