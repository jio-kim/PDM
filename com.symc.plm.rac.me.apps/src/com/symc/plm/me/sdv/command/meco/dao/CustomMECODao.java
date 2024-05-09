package com.symc.plm.me.sdv.command.meco.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.dto.ApprovalLineData;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCBOPEditData;

@SuppressWarnings("unchecked")
public class CustomMECODao {

	public static final String MECO_INFO_SERVICE_CLASS = "com.kgm.service.MECOService";//com.kgm.dao.ECOInfoDao�� �ص� ��
	public static final String MECO_HISTORY_SERVICE_CLASS = "com.kgm.service.MECOHistoryService";
	public static final String ECO_INFO_SERVICE_CLASS = "com.kgm.service.ECOService";//com.kgm.dao.ECOInfoDao�� �ص� ��

	private SYMCRemoteUtil remoteQuery;
	private DataSet ds;
	public CustomMECODao() {
		//		this.remoteQuery = new SYMCRemoteUtil(WEB_JDBC_URL()); // TC Preference���� Web JDBC URL�� ���� ��
		this.remoteQuery = new SYMCRemoteUtil();
	}

	/**
	 * ECO ǰ�� �߹��� ����
	 * 
	 * [SR140828-015][20140829] shcho, Migration �� MECO ID ä���� 601 ���� �� �� �ֵ��� ����. (2015�⿡�� �ٽ� 001���� ä�� �� �� �ֵ��� Preference�� �̿��� �ʱⰪ ���� ����)
	 * 
	 * @param prefix
	 * @return
	 */
	public String getNextMECOSerial(String prefix, String init_no) throws Exception {
		System.out.println("TEST");
		String mecoID = null;
		ds = new DataSet();
		ds.put("prefix", prefix);
		ds.put("init_no", init_no);
		mecoID = (String) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getNextMECOSerial", ds);
		System.out.println("mecoID = "+mecoID);
		return mecoID;
	}

	public ArrayList<ApprovalLineData> loadApprovalLine(ApprovalLineData data) throws Exception {
		ArrayList<ApprovalLineData> resultList = null;
		ds = new DataSet();
		ds.put("data", data);
		resultList = (ArrayList<ApprovalLineData>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "loadApprovalLine", ds);
		return resultList;
	}

	public ArrayList<ApprovalLineData> loadSavedUserApprovalLine(ApprovalLineData data) throws Exception {
		ds = new DataSet();
		ds.put("data", data);
		ArrayList<ApprovalLineData> resultList = (ArrayList<ApprovalLineData>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "loadSavedUserApprovalLine", ds);
		return resultList;
	}

	public void removeApprovalLine(ApprovalLineData data) throws Exception {
		ds = new DataSet();
		ds.put("data", data);
		remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "removeApprovalLine", ds);
	}

	/**
	 * ECO ������ ����
	 * @param list
	 * @throws Exception
	 */
	public void saveApprovalLine(ArrayList<ApprovalLineData> list) throws Exception {
		ds = new DataSet();
		ds.put("data", list);
		remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "saveApprovalLine", ds);
	}

	/**
	 * MECO ������ �ҷ�����
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public ArrayList<ApprovalLineData> getApprovalLine(ApprovalLineData data) throws Exception {
		ds = new DataSet();
		ds.put("data", data);
		ArrayList<ApprovalLineData> resultList = (ArrayList<ApprovalLineData>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getApprovalLine", ds);
		return resultList;
	}

	/**
	 * MECO_EPL Table��  Data�� �Է� �ϴ� �Լ�
	 * @param list
	 * @throws Exception
	 */
	public void insertMECOEPL(ArrayList<SYMCBOPEditData> list) throws Exception {
		ds = new DataSet();
		ds.put("data", list);
		remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "insertMECOEPL", ds);

	}

	public void deleteMECOEPL(String mecoNo) throws Exception {

		ds = new DataSet();
		ds.put("mecoNo", mecoNo);
		remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "deleteMECOEPL", ds);

	}


    public ArrayList<SYMCBOPEditData> selectMECOEplList(String mecoNo) throws Exception {

		ArrayList<SYMCBOPEditData> resultList = null;
		ds = new DataSet();
		ds.put("mecoNo", mecoNo);

		resultList = (ArrayList<SYMCBOPEditData>)remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "selectMECOEplList", ds);

		return resultList;
	}

	public void truncateModifiedMEPL(String mecoNo) throws Exception {

		ds = new DataSet();
		ds.put("data", mecoNo);
		remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "truncateModifiedEPL",  ds);

	}

	public ArrayList<HashMap<String, String>> getEndItemMECONoForProcessSheet(String operationno) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("operationno", operationno);

		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getEndItemMECONoForProcessSheet", ds);
		return resultList;
	}

	public ArrayList<HashMap<String, String>> getSubsidiaryMECONoForProcessSheet(String operationno) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("operationno", operationno);

		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getSubsidiaryMECONoForProcessSheet", ds);
		return resultList;
	}

	public ArrayList<HashMap<String, String>> getResourceMECONoForProcessSheet(HashMap<String, String> paramMap) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("operationno", paramMap.get("operationno"));
		if(paramMap.containsKey("stationno")) {
			ds.put("stationno", paramMap.get("stationno"));
		}

		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getResourceMECONoForProcessSheet", ds);
		return resultList;
	}
	
	
	// ������ : bc.kim
    // ����ȭ ����� ��û 
    // �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Resource �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
    // �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
	public ArrayList<HashMap<String, String>> getSymbomResourceMecoNo(HashMap<String, String> paramMap) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("operationNo", paramMap.get("operationNo"));
		ds.put("revisionId", paramMap.get("revisionId"));
		
		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getSymbomResourceMecoNo", ds);
		return resultList;
	}
	
	
	// ������ : bc.kim
	// ����ȭ ����� ��û SR : [SR190131-060]
	// �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Subsidiary �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
	// �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
	public ArrayList<HashMap<String, String>> getSymbomSubsidiaryMecoNo(HashMap<String, String> paramMap) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		ds = new DataSet();
		ds.put("operationNo", paramMap.get("operationNo"));
		ds.put("revisionId", paramMap.get("revisionId"));
		
		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getSymbomSubsidiaryMecoNo", ds);
		return resultList;
	}

	public Date getLastEPLLoadDate(HashMap<String, String> paramMap) throws Exception {
	    Date lastEPLLoadDate = null;
	    ds = new DataSet();
	    ds.put("mecoid", paramMap.get("mecoid"));
//	    ds.put("itemid", paramMap.get("itemid"));
//	    ds.put("itemrev", paramMap.get("itemrev"));

	    lastEPLLoadDate = (Date) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getLastEPLLoadDate", ds);
	    return lastEPLLoadDate;
	}

	public ArrayList<SYMCBOMEditData> searchECOEplList(DataSet ds) throws Exception {

		ArrayList<SYMCBOMEditData> resultList = null;

		resultList = (ArrayList<SYMCBOMEditData>)remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "searchECOEplList", ds);


		return resultList;
	}

	public ArrayList<HashMap<String, Object>> getEndItemListOnFunction (DataSet ds) throws Exception {
		ArrayList<HashMap<String, Object>> resultList = null;

		resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getEndItemListOnFunction", ds);

		return resultList;
	}
	//-------------------------------------------
	//2014.01.08
	public ArrayList<HashMap<String, String>> checkModifiedMEPL(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		resultList = (ArrayList<HashMap<String, String>>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "checkModifiedMEPL", ds);
		return resultList;
	}


//	public ArrayList<String> checkEndtoEnd(String ecoNo) throws Exception {
//		ArrayList<String> resultList = null;
//		ds = new DataSet();
//		ds.put("ecoNo", ecoNo);
//		resultList = (ArrayList<String>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "checkEndtoEnd", ds);
//		return resultList;
//	}

	public boolean checkExistMEPL(DataSet ds) throws Exception {
//		ECOHistoryMapper mapper = session.getMapper(ECOHistoryMapper.class);
		String result =  (String) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "checkExistMEPL", ds);

		if(result.equals("true")) {
			return Boolean.TRUE;
		}else{
			return Boolean.FALSE;
		}

	}

	public boolean updateMEcoStatus(String ecoRevPuid, String ecoStatus, String itemStatus) throws Exception {
		boolean result = false;
		ds = new DataSet();
		ds.put("mecoRevPuid", ecoRevPuid);
		ds.put("mecoStatus", ecoStatus);
		ds.put("itemStatus", itemStatus);
		result = (Boolean) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "updateMEcoStatus", ds);
		if(!result){
			throw (new Exception("updateMEcoStatus error"));
		}
		return result;
	}

	public String childrenCount(String bvrPuid) throws Exception {
		String childrenCount = "1";
		ds = new DataSet();
		ds.put("bvrPuid", bvrPuid);
		childrenCount = (String) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "childrenCount", ds);
		return childrenCount;
	}

	public boolean sendMail(String fromUser, String title, String body, String toUsers) throws Exception{
		ds = new DataSet();
		ds.put("the_sysid", "NPLM");
		
		if(fromUser == null || fromUser.equals(""))
			ds.put("the_sabun", "NPLM");
		else
			ds.put("the_sabun", fromUser);
		
		ds.put("the_title", title);
		ds.put("the_remark", body);
		ds.put("the_tsabun", toUsers);
		return (Boolean) remoteQuery.execute(ECO_INFO_SERVICE_CLASS, "sendMail", ds);
	}

	//-------------------------------------------

	/**
	 * ECO ǰ�� �߹��� ����
	 * 
	 * [SR140828-015][20140829] shcho, Migration �� MECO ID ä���� 601 ���� �� �� �ֵ��� ����. (2015�⿡�� �ٽ� 001���� ä�� �� �� �ֵ��� Preference�� �̿��� �ʱⰪ ���� ����)
	 * 
	 * @param prefix
	 * @return
	 */
	public Vector<String> getChangedNewItemIdList(String itemId, String newRevId, String oldRevId ) throws Exception {

		Vector<String> changedItemIdV = null;
		
    	ArrayList<HashMap> resultList = null;

    	DataSet ds = new DataSet();
		ds = new DataSet();
		ds.put("Target_Item_Id", itemId);
		ds.put("New_Rev_Id", newRevId);
		ds.put("Old_Rev_Id", oldRevId);
		
    	resultList = (ArrayList<HashMap>) remoteQuery.execute(MECO_INFO_SERVICE_CLASS, "getChangedNewItemIdList", ds);

    	if(resultList!=null && resultList.size()>0){
    		
    		changedItemIdV = new Vector<String>();
    		
    		for (int i = 0; i < resultList.size(); i++) {
    			HashMap currentRowHash = resultList.get(i);
    			if(currentRowHash!=null){
    				String tempItemId = (String)currentRowHash.get("ITEM_ID");
    				String tempItemType = (String)currentRowHash.get("ITEM_TYPE");
    				
    				if(changedItemIdV.contains(tempItemId)==false){
    					changedItemIdV.add(tempItemId);
    				}
    			}
			}
    	}
    	return changedItemIdV;
	}
}

