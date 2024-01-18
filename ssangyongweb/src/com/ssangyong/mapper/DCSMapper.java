package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface DCSMapper {

	public void updateDCSStatus(DataSet dataSet);

	public void refreshTCObject(DataSet dataSet);

	public void refreshTCTimeStamp(DataSet dataSet);

	public void sendMail(DataSet dataSet);
	
    public void sendMailEai(DataSet ds);

    public ArrayList<HashMap<String, Object>> getDCSList(DataSet ds);	

	public ArrayList<HashMap<String, Object>> getPSCList(DataSet ds);	

	public int getDCSInWorkCount(DataSet ds);	

	public int getPSCInWorkCount(DataSet ds);	
	
	public ArrayList<HashMap<String, Object>> getStandbyList(DataSet ds);

	public ArrayList<HashMap<String, Object>> getSignOffbyTaskList(DataSet ds);
	
	public ArrayList<HashMap<String, Object>> getProcessingList(DataSet ds);

	public ArrayList<HashMap<String, Object>> getConsultationDelayList(DataSet ds);

	public ArrayList<HashMap<String, Object>> getMyDCSList(DataSet ds);
	
	public ArrayList<HashMap<String, Object>> getMyPSCList(DataSet ds);
}
