package com.symc.plm.rac.prebom.dcs.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;

public class DCSVisionNetQueryService {

	private SYMCRemoteUtil remoteUtil;

	private final String SERVICE_CLASS_NAME = "com.kgm.service.DCSVisionNetService";

	public DCSVisionNetQueryService() {
		remoteUtil = new SYMCRemoteUtil();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> selectVNetTeamList(DataSet dataSet) {
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			resultList = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(SERVICE_CLASS_NAME, "selectVNetTeamList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultList;
	}
	
	/**
	 * [SR160920-012][20161012] taeku.jeong Vistion Net�� Teamcenter�� ��ϵ� ��ȿ�� Team Code�� List �ؼ� Return �Ѵ�.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> getVnetAndTcLiveSameTeamCode() {
		
		ArrayList<String> teamCodeList = null;
		
		ArrayList<HashMap<String, Object>> resultList = null;

		DataSet dataSet = new DataSet();
		dataSet.put("tempKey", "tempValue");
		
		try {
			resultList = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(SERVICE_CLASS_NAME, "getVnetAndTcLiveSameTeamCode", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(resultList!=null && resultList.size()>0){
			
			teamCodeList = new ArrayList<String>();
			
			for (int i = 0; resultList!=null && i < resultList.size(); i++) {
				HashMap rowHash = resultList.get(i);
				
				String tempTeamCode = null;
				Object tempTeamCodeObject = (Object)rowHash.get("TEAM_CODE");
				if(tempTeamCodeObject!=null && tempTeamCodeObject instanceof String){
					tempTeamCode = tempTeamCodeObject.toString();
				}
				if(tempTeamCode!=null && tempTeamCode.trim().length()>0){
					teamCodeList.add(tempTeamCode.trim());
				}
			}
		}

		return teamCodeList;
	}
	
	/**
	 * [SR160920-012][20161012] taeku.jeong Vision Net�� ��ϵ� Team Code�� �ش��ϴ� Team Name�� ã�Ƽ� Return �Ѵ�.
	 * @param teamCode
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> getVnetTeamName(String teamCode) {
		
		ArrayList<HashMap<String, Object>> resultList = null;
		if(teamCode==null || (teamCode!=null && teamCode.trim().length()<1)){
			return resultList;
		}
		
		DataSet dataSet = new DataSet();
		dataSet.put("team_code", teamCode.trim());

		try {
			resultList = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(SERVICE_CLASS_NAME, "getVnetTeamName", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultList;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> selectVNetTeamHistList(DataSet dataSet) {
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			resultList = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(SERVICE_CLASS_NAME, "selectVNetTeamHistList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultList;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> selectVNetUserList(DataSet dataSet) {
		ArrayList<HashMap<String, Object>> resultList = null;

		try {
			resultList = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(SERVICE_CLASS_NAME, "selectVNetUserList", dataSet);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultList;
	}

}
