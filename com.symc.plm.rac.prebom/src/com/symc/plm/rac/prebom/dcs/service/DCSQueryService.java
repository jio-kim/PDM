package com.symc.plm.rac.prebom.dcs.service;

import java.util.List;
import java.util.Map;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;

/**
 * [20160510][ymjang] 검색속도개선 - DB 쿼리 방식으로 변경.
 */
public class DCSQueryService {

	private SYMCRemoteUtil remoteUtil;

	private final String SERVICE_CLASS_NAME = "com.ssangyong.service.DCSService";

	public DCSQueryService() {
		remoteUtil = new SYMCRemoteUtil();
	}

	public Boolean updateDCSStatus(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "updateDCSStatus", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Boolean) object;
	}

	public Boolean refreshTCObject(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "refreshTCObject", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Boolean) object;
	}

	public Boolean refreshTCTimeStamp(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "refreshTCTimeStamp", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Boolean) object;
	}

	public Boolean sendMail(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "sendMail", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Boolean) object;
	}

	public List<Map<String, Object>> getDCSList(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getDCSList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (List<Map<String, Object>>) object;
	}

	public List<Map<String, Object>> getPSCList(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getPSCList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (List<Map<String, Object>>) object;
	}
	
	public List<Map<String, Object>> getStandbyList(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getStandbyList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (List<Map<String, Object>>) object;
	}
	
	public List<Map<String, Object>> getSignOffbyTaskList(String taskUid) throws Exception {
		Object object = null;

		try {
			DataSet dataSet = new DataSet();
			dataSet.put("TASK_PUID", taskUid);
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getSignOffbyTaskList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (List<Map<String, Object>>) object;
	}

	public List<Map<String, Object>> getProcessingList(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getProcessingList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (List<Map<String, Object>>) object;
	}
	
	public List<Map<String, Object>> getConsultationDelayList(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getConsultationDelayList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (List<Map<String, Object>>) object;
	}
	
	public List<Map<String, Object>> getMyDCSList(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getMyDCSList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (List<Map<String, Object>>) object;
	}
	
	public List<Map<String, Object>> getMyPSCList(DataSet dataSet) throws Exception {
		Object object = null;

		try {
			object = remoteUtil.execute(SERVICE_CLASS_NAME, "getMyPSCList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (List<Map<String, Object>>) object;
	}

}
