package com.kgm.commands.faq;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;

/**
 * [SR150421-027][20150811][ymjang] PLM system �������� - Manual ��ȸ ������� �߰�
 */
public class FaqQueryService {

    private SYMCRemoteUtil remoteUtil;

    private final String FAQSERVICECLASS = "com.kgm.service.FaqService";

    public FaqQueryService() {
        remoteUtil = new SYMCRemoteUtil();
    }

    public Boolean insertFaq(DataSet dataSet) {
        Object object = null;

        try {
            object = remoteUtil.execute(FAQSERVICECLASS, "insertFaq", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (Boolean) object;
    }

    public Boolean updateFaq(DataSet dataSet) {
        Object object = null;

        try {
            object = remoteUtil.execute(FAQSERVICECLASS, "updateFaq", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (Boolean) object;
    }

    public Boolean updateFaqSeq(DataSet dataSet) {
        Object object = null;

        try {
            object = remoteUtil.execute(FAQSERVICECLASS, "updateFaqSeq", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (Boolean) object;
    }

    public Boolean deleteFaq(DataSet dataSet) {
        Object object = null;

        try {
            object = remoteUtil.execute(FAQSERVICECLASS, "deleteFaq", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (Boolean) object;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, Object>> selectFaqList(DataSet dataSet) {
        ArrayList<HashMap<String, Object>> faqList = null;

        try {
        	faqList = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(FAQSERVICECLASS, "selectFaqList", dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return faqList;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Object> selectNextOUID() {
        HashMap<String, Object> ouidMap = null;

        try {
            ouidMap = (HashMap<String, Object>) remoteUtil.execute(FAQSERVICECLASS, "selectNextOUID", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ouidMap;
    }
    
}
