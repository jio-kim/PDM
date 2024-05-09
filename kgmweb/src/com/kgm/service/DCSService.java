package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.DCSDao;

/**
 * [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
 */
public class DCSService {

	private DCSDao dao;

	public DCSService() {

	}

	public boolean updateDCSStatus(DataSet dataSet) {
		dao = new DCSDao();

		return dao.updateDCSStatus(dataSet);
	}

	public boolean refreshTCObject(DataSet dataSet) {
		dao = new DCSDao();

		return dao.refreshTCObject(dataSet);
	}

	public boolean refreshTCTimeStamp(DataSet dataSet) {
		dao = new DCSDao();

		return dao.refreshTCTimeStamp(dataSet);
	}

    /**
     * SYMC 인트라넷을 통한 메일 발송
     * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
     * @return
     */
    public boolean sendMail(DataSet ds){
        dao = new DCSDao();
        
        // [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
        dao.sendMailEai(ds);
        
        // [20160606][ymjang] 기존 메일 발송 방식
        //dao.sendMail(ds);
        
        return true;
    }
    
    /**
     * SYMC 인트라넷을 통한 메일 발송 (through EAI)
     * @param ds
     * @return
     */
    public boolean sendMailEai(DataSet ds){
        dao = new DCSDao();
        return dao.sendMailEai(ds);
    }

	public ArrayList<HashMap<String, Object>> getDCSList(DataSet ds) {
		dao = new DCSDao();
    
		return dao.getDCSList(ds);
	}

	public ArrayList<HashMap<String, Object>> getPSCList(DataSet ds) {
		dao = new DCSDao();
        
		return dao.getPSCList(ds);
	}

	public int getDCSInWorkCount(DataSet ds) {
		dao = new DCSDao();
        
		return dao.getDCSInWorkCount(ds);
	}

	public int getPSCInWorkCount(DataSet ds) {
		dao = new DCSDao();
        
		return dao.getPSCInWorkCount(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getStandbyList(DataSet ds) {
		dao = new DCSDao();
    
		return dao.getStandbyList(ds);
	}

	public ArrayList<HashMap<String, Object>> getSignOffbyTaskList(DataSet ds) {
		dao = new DCSDao();
    
		return dao.getSignOffbyTaskList(ds);
	}

	public ArrayList<HashMap<String, Object>> getProcessingList(DataSet ds) {
		dao = new DCSDao();
    
		return dao.getProcessingList(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getConsultationDelayList(DataSet ds) {
		dao = new DCSDao();
    
		return dao.getConsultationDelayList(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getMyDCSList(DataSet ds) {
		dao = new DCSDao();
    
		return dao.getMyDCSList(ds);
	}
	
	public ArrayList<HashMap<String, Object>> getMyPSCList(DataSet ds) {
		dao = new DCSDao();
    
		return dao.getMyPSCList(ds);
	}
}
