package com.kgm.commands.getparent;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;

/**
 * [20160816][ymjang][SR160719-029] Get Parent ���� ����
 */
public class GetParentDao {
	private SYMCRemoteUtil remoteQuery;
	private DataSet ds;
	public static final String GET_PARENT_SERVICE_CLASS = "com.kgm.service.GetParentService";

	public GetParentDao() {
		this.remoteQuery = new SYMCRemoteUtil();
//		this.remoteQuery = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
	}
	
	/**
	 * [20160816][ymjang][SR160719-029] Get Parent ���� ����
	 * @param sPartNo
	 * @param sPartRev
	 * @param isWorking
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> searchUpperBOM(String sPartNo, String sPartRev, String isWorking) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("partNo", sPartNo);
		ds.put("partRev", sPartRev);
		ds.put("isWorking", isWorking); // '0'-latest working, '1'-latest released, 'ALL'-All
		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(GET_PARENT_SERVICE_CLASS, "searchUpperBOM", ds);
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> searchAll(String sPartNo, String sPartRev) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("partNo", sPartNo);
		ds.put("partRev", sPartRev);
		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(GET_PARENT_SERVICE_CLASS, "searchAll", ds);
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> searchLatestReleased(String sPartNo, String sPartRev) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("partNo", sPartNo);
		ds.put("partRev", sPartRev);
		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(GET_PARENT_SERVICE_CLASS, "searchLatestReleased", ds);
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> searchLatestWorking(String sPartNo, String sPartRev) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("partNo", sPartNo);
		ds.put("partRev", sPartRev);
		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(GET_PARENT_SERVICE_CLASS, "searchLatestWorking", ds);
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, String>> isConnectedFunction(ArrayList<String> alPartNo) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("partNoList", alPartNo);
		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(GET_PARENT_SERVICE_CLASS, "isConnectedFunction", ds);
		return resultList;
	}
}
