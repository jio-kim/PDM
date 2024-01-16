package com.ssangyong.commands.vpm.report.dao;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;


public class CustomReportDao {
	
	private SYMCRemoteUtil remoteQuery;		
	public static final String VPM_IF_SERVICE_CLASS = "com.ssangyong.service.VPMIfService";

	public CustomReportDao() {
		this.remoteQuery = new SYMCRemoteUtil();
	}	
	
	/**
	 *  VPM Report Dialog 리스트용
	 * 
	 * @method getValidateVPMList 
	 * @date 2013. 5. 29.
	 * @param
	 * @return ArrayList<HashMap>
	 * @exception
	 * @throws
	 * @see
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public ArrayList<HashMap> getValidateVPMList(HashMap<String, Object> searchCondition) throws Exception {	    
	    DataSet ds = new DataSet();
	    ds.put("ECO_NO", searchCondition.get("ECO_NO"));
	    ds.put("CPNO", searchCondition.get("CPNO"));
        ds.put("FROM_DATE", searchCondition.get("FROM_DATE"));
        ds.put("TO_DATE", searchCondition.get("TO_DATE"));
        ds.put("NOT_INFORMED", searchCondition.get("NOT_INFORMED"));
        ds.put("INCOMPLETE_WORK", searchCondition.get("INCOMPLETE_WORK"));
        ds.put("IS_VALID", searchCondition.get("IS_VALID")); 
	    return (ArrayList<HashMap>) remoteQuery.execute(VPM_IF_SERVICE_CLASS, "getValidateVPMList", ds);	    
	}
	
	/**
	 * VehPart Report Dialog 리스트용
	 * 
	 * @method getValidateVehPartList 
	 * @date 2013. 5. 29.
	 * @param
	 * @return ArrayList<HashMap>
	 * @exception
	 * @throws
	 * @see
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public ArrayList<HashMap> getValidateVehPartList(HashMap<String, Object> searchCondition) throws Exception {        
        DataSet ds = new DataSet();
        ds.put("ITEM_ID", searchCondition.get("ITEM_ID"));
        ds.put("FROM_DATE", searchCondition.get("FROM_DATE"));
        ds.put("TO_DATE", searchCondition.get("TO_DATE"));
        ds.put("IS_VALID", searchCondition.get("IS_VALID"));
        ds.put("IF_STATUS", searchCondition.get("IF_STATUS")); 
        ds.put("NON_EPL", searchCondition.get("NON_EPL"));
        return (ArrayList<HashMap>) remoteQuery.execute(VPM_IF_SERVICE_CLASS, "getValidateVehPartList", ds);        
    }
	
	/**
	 * List 를 가지고 IF_VEHPART 상태를 일괄 변경한다.
	 * 
	 * @method updateListVehStatus 
	 * @date 2013. 5. 30.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	@SuppressWarnings({ "rawtypes" })
    public void updateListVehStatus(ArrayList updateList) throws Exception {
	    DataSet ds = new DataSet();
	    ds.put("UPDATE_LIST", updateList);
	    remoteQuery.execute(VPM_IF_SERVICE_CLASS, "updateListVehStatus", ds);
	}
	
	/**
	 * IF_ECO_INFO_FROM_VPM - 작업자 설정
	 * 
	 * @method updateVPMCustomSetWorker 
	 * @date 2013. 6. 3.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	@SuppressWarnings({ "rawtypes" })
	public void updateVPMCustomSetWorker(ArrayList updateCustomList) throws Exception {
        DataSet ds = new DataSet();
        ds.put("UPDATE_LIST", updateCustomList);
        remoteQuery.execute(VPM_IF_SERVICE_CLASS, "updateVPMCustomSetWorker", ds);
    }
	
	/**
	 * IF_ECO_INFO_FROM_VPM -통보처리
	 * 
	 * @method updateVPMCustomNoticeProcess 
	 * @date 2013. 6. 3.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	@SuppressWarnings({ "rawtypes" })
    public void updateVPMCustomNoticeProcess(ArrayList updateCustomList) throws Exception {
        DataSet ds = new DataSet();
        ds.put("UPDATE_LIST", updateCustomList);
        remoteQuery.execute(VPM_IF_SERVICE_CLASS, "updateVPMCustomNoticeProcess", ds);
    }
	
	/**
	 * IF_ECO_INFO_FROM_VPM - 완료처리
	 * 
	 * @method updateVPMCustomCompleteProcess 
	 * @date 2013. 6. 3.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	@SuppressWarnings({ "rawtypes" })
    public void updateVPMCustomCompleteProcess(ArrayList updateCustomList) throws Exception {
        DataSet ds = new DataSet();
        ds.put("UPDATE_LIST", updateCustomList);
        remoteQuery.execute(VPM_IF_SERVICE_CLASS, "updateVPMCustomCompleteProcess", ds);
    }
	
	/**
     * IF_ECO_INFO_FROM_VPM - 유져 Skip
     * 
     * @method updateVPMCustomCompleteProcess 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
	@SuppressWarnings({ "rawtypes" })
    public void updateVPMCustomUserSkip(ArrayList updateCustomList) throws Exception {
        DataSet ds = new DataSet();
        ds.put("UPDATE_LIST", updateCustomList);
        remoteQuery.execute(VPM_IF_SERVICE_CLASS, "updateVPMCustomUserSkip", ds);
    }
	
	/**
     *  TC Report Dialog 리스트용
     * 
     * @method getValidateTCList 
     * @date 2013. 5. 29.
     * @param
     * @return ArrayList<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ArrayList<HashMap> getValidateTCList(HashMap<String, Object> searchCondition) throws Exception {        
        DataSet ds = new DataSet();
        ds.put("ECO_NO", searchCondition.get("ECO_NO"));
        ds.put("CPNO", searchCondition.get("CPNO"));
        ds.put("FROM_DATE", searchCondition.get("FROM_DATE"));
        ds.put("TO_DATE", searchCondition.get("TO_DATE"));
        ds.put("NOT_INFORMED", searchCondition.get("NOT_INFORMED"));
        ds.put("INCOMPLETE_WORK", searchCondition.get("INCOMPLETE_WORK"));
        ds.put("IS_VALID", searchCondition.get("IS_VALID")); 
        return (ArrayList<HashMap>) remoteQuery.execute(VPM_IF_SERVICE_CLASS, "getValidateTCList", ds);        
    }
	
    /**
     * IF_ECO_INFO_FROM_TC - 작업자 설정
     * 
     * @method updateTCCustomSetWorker 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "rawtypes" })
    public void updateTCCustomSetWorker(ArrayList updateCustomList) throws Exception {
        DataSet ds = new DataSet();
        ds.put("UPDATE_LIST", updateCustomList);
        remoteQuery.execute(VPM_IF_SERVICE_CLASS, "updateTCCustomSetWorker", ds);
    }
    
    /**
     * IF_ECO_INFO_FROM_TC -통보처리
     * 
     * @method updateTCCustomNoticeProcess 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "rawtypes" })
    public void updateTCCustomNoticeProcess(ArrayList updateCustomList) throws Exception {
        DataSet ds = new DataSet();
        ds.put("UPDATE_LIST", updateCustomList);
        remoteQuery.execute(VPM_IF_SERVICE_CLASS, "updateTCCustomNoticeProcess", ds);
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - 완료처리
     * 
     * @method updateTCCustomCompleteProcess 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "rawtypes" })
    public void updateTCCustomCompleteProcess(ArrayList updateCustomList) throws Exception {
        DataSet ds = new DataSet();
        ds.put("UPDATE_LIST", updateCustomList);
        remoteQuery.execute(VPM_IF_SERVICE_CLASS, "updateTCCustomCompleteProcess", ds);
    }
    
    /**
     * IF_ECO_INFO_FROM_TC - 유져 Skip
     * 
     * @method updateTCCustomUserSkip 
     * @date 2013. 6. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "rawtypes" })
    public void updateTCCustomUserSkip(ArrayList updateCustomList) throws Exception {
        DataSet ds = new DataSet();
        ds.put("UPDATE_LIST", updateCustomList);
        remoteQuery.execute(VPM_IF_SERVICE_CLASS, "updateTCCustomUserSkip", ds);
    }
	
}
