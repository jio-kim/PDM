package com.symc.work.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.service.TcDataManagementService;
import com.symc.common.soa.service.TcStructureManagementService;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ItemRevision;

public class MBomInterfaceDataSyncService {

	private final static String SUCCESS = "SUCCESS";
	private final static String ERROR = "ERROR";
	private final static String CHILD_NOT_FOUND = "CHILD_NOT_FOUND";
	private final static String PARENT_NOT_FOUND = "PARENT_NOT_FOUND";
	private final static String DATA_INVALID = "DATA_INVALID";

	/**
	 * IF_MBOM_BPN 테이블에 있는 BP_DATE 를 TC에 적용한다.
	 * 
	 * [SR141020-047][20141021] shcho, 속도 개선 (1. 동일한 Parent를 가진 것들만 모아서 BOMWindow를 열고 한꺼번에 처리, 2. child 속성 getProperties 수행시 children배열 단위로 한꺼번에 수행)
	 * 
	 *
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String updateBpnDate() throws Exception{

		StringBuffer log = new StringBuffer();
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap<String, String>> parentIDList = (List<HashMap<String, String>>)commonDao.selectList("com.symc.interface.getBpnToUpdateParentIdList", null);
		if(parentIDList != null) {

        	TcLoginService tcLoginService = new TcLoginService();
        	Session session = null;
        	try{
        		session = tcLoginService.getTcSession();
        		BOMLineService bomLineService = new BOMLineService(session);
            	TcItemUtil tcItemUtil = new TcItemUtil(session);
            	TcStructureManagementService structureSVC = new TcStructureManagementService(session);
            	TcQueryService tcQueryService = new TcQueryService(session);
            	TcDataManagementService dmService = new TcDataManagementService(session);

            		try{
            			log.append(findParentItemRevision(bomLineService, tcItemUtil, structureSVC, tcQueryService,dmService, parentIDList));
            		}catch( Exception e){
            			e.printStackTrace();
            			log.append(IFConstants.TEXT_RETURN);
            			log.append(StringUtil.getStackTraceString(e));
            		}
	        } catch (Exception e) {
	            throw e;
	        } finally {
	            if (session != null) {
	                session.logout();
	            }

	        }

        }

//        Success된 항목들은 제거함.
        commonDao.delete("com.symc.interface.clearUpdatedBpnInfo", null);

        return log.toString();
	}

	/**
	 * Parent Item Revision을 TC에서 검색.
	 *
	 * @param bomLineService
	 * @param tcItemUtil
	 * @param structureSVC
	 * @param tcQueryService
	 * @param dmService
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private String findParentItemRevision(BOMLineService bomLineService, TcItemUtil tcItemUtil, TcStructureManagementService structureSVC, TcQueryService tcQueryService
			,TcDataManagementService dmService, List<HashMap<String, String>> parentIDList) throws Exception{

	    StringBuffer log = new StringBuffer();

	    for( HashMap<String, String> parentIDmap : parentIDList){
	        
    		String parentID = parentIDmap.get("PARENT_ID");
    		String IFParentRevID = parentIDmap.get("PARENT_REV_ID");
    		String ecoID = parentIDmap.get("ECO_ID");
    
    		// [SR140723-025][20140522] Parent Revision ID 가져오는 로직 추가.
    		HashMap<String, String> paraMap = new HashMap<String, String>();
    		paraMap.put("ECO_ID", ecoID);
    		paraMap.put("PARENT_ID", parentID);
    		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
    		String parentRevID = (String)commonDao.selectOne("com.symc.interface.getParentRev", paraMap);
    
    		//parentID 와 parentRevID 으로 Parent ItemRevision Object 찾기
            String queryName = TcConstants.SEARCH_ITEM_REVISION;
            String[] entries = { "Item ID", "Revision"};
            String[] values = { parentID, parentRevID};
            String[] properties = {TcConstants.PROP_ITEM_ID, "item_revision_id", "date_released", "object_desc"};

            ModelObject[] modelObject = tcQueryService.searchTcObject(queryName, entries, values, properties);
    		if( modelObject == null || modelObject.length == 0){
    			return updateBPNStat(parentID, parentRevID, null, null, PARENT_NOT_FOUND, log);
    		}
    		
    		BOMWindow bomWindow = null;
    		try {
    		    //Parent ItemRevision Object를 Top으로 하는 BOMWindow 열기
    		    bomWindow = bomLineService.getCreateBOMWindow((ItemRevision)modelObject[0], IFConstants.BOMVIEW_LATEST_RELEASED, null);
    		    
    		    //Parent Line 하위 Children Lines 가져오기
    		    BOMLine topLine = (BOMLine)bomWindow.get_top_line();
                String[] topLineProperties = new String[]{"bl_child_lines"};
                tcItemUtil.getProperties(new ModelObject[]{topLine}, topLineProperties);
                ModelObject[] child_lines = topLine.get_bl_child_lines();
                String[] childProperties = new String[]{"bl_revision", "bl_occ_int_order_no", "M7_BP_ID", "M7_BP_DATE"};
                tcItemUtil.getProperties(child_lines, childProperties);
                
                //동일한 Parent ItemRevision을 부모로 두고 있는 BPN Update 대상 Children 목록 조회
    		    HashMap<String, String> paraMap2 = new HashMap<String, String>();
    		    paraMap2.put("PARENT_ID", parentID);
    		    paraMap2.put("PARENT_REV_ID", parentRevID);
    		    List<HashMap<String, String>> list = (List<HashMap<String, String>>)commonDao.selectList("com.symc.interface.getBpnToUpdate", paraMap2);
    		    
    		    //BPN 속성 Update
    		    if(list != null && list.size() > 0) {
    		        for( HashMap<String, String> map : list){
    		            log.append(setBPN(tcItemUtil, map, child_lines));
    		        }
    		    } else {
    		        log.append(updateBPNStat(parentID, IFParentRevID, null, null, CHILD_NOT_FOUND, log));
    		    }
    		    
            } catch (Exception e) {
                throw e;
            } finally {
                if(bomWindow != null) {
                    structureSVC.saveBOMWindow(bomWindow);
                    structureSVC.closeBOMWindow(bomWindow);
                    dmService.unloadObjects(new ModelObject[]{bomWindow});
                }
            }
	    }
		return log.toString();
	}

	/**
	 * Parent Revision에서 Child와 Order 를 검색하여, 해당하는 BOM Line에 BP_ID 및 BP_DATE를 셋.
	 *
	 * @param tcItemUtil
	 * @param map
	 * @param child_lines 
	 * @return
	 * @throws Exception
	 */
	private String setBPN(TcItemUtil tcItemUtil,  HashMap<String, String> map, ModelObject[] child_lines) throws Exception{
		StringBuffer log = new StringBuffer();
		String parentID = map.get("PARENT_ID");
		String parentRevID = map.get("PARENT_REV_ID");
		String childID = map.get("CHILD_ID");
		String ecoID = map.get("ECO_ID");
		String orderNo = map.get("ORDER_NO");
		String bpDateFrom = map.get("BP_DATE_FROM");
		String bpDateTo = map.get("BP_DATE_TO");
		SimpleDateFormat interfaceFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		try{
			boolean bFound = false;
            
            if(child_lines != null) {
                SimpleDateFormat standardFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                
                for( int i = 0;  i < child_lines.length; i++){
                    
                    BOMLine child = (BOMLine)child_lines[i];
                    ItemRevision revision = (ItemRevision)child.get_bl_revision();
                    tcItemUtil.getProperties(new ModelObject[]{revision}, BOMLineService.DEFAULT_ITEM_REVISION_PROPERTIES);
                    
				    //Child ID와 Order No를 비교
                    if ( childID.equals(revision.get_item_id())
                            && ("" + child.get_bl_occ_int_order_no()).equals(orderNo)){
                        
                        if( (bpDateFrom == null || bpDateFrom.trim().isEmpty()) && (bpDateTo == null || bpDateTo.trim().isEmpty())){
                            log.append(IFConstants.TEXT_RETURN);
                            log.append(updateBPNStat(parentID, parentRevID, childID, orderNo, DATA_INVALID, log));
                            return log.toString();
                        }
                        
                        Date fromDate = null;
                        Date toDate = null;
                        String fromDateStr = "";
                        String toDateStr = "";
                        String preFromDateStr = "";
                        String preToDateStr = "";
                        
                        String preBpDateStr = child.getPropertyDisplayableValue("M7_BP_DATE");
                        if( preBpDateStr != null && !preBpDateStr.equals("")){
                            int tIdx = preBpDateStr.indexOf("TO");
                            preFromDateStr = preBpDateStr.substring(0, tIdx).trim();
                            preToDateStr = preBpDateStr.substring(tIdx + 2).trim();
                        }
                        
                        if( bpDateFrom != null){
                            fromDate = interfaceFormat.parse(bpDateFrom);
                            fromDateStr = standardFormat.format(fromDate);
                        }else{
                            fromDateStr = preFromDateStr;
                        }
                        
                        if( bpDateTo != null ){
                            toDate = interfaceFormat.parse(bpDateTo);
                            toDateStr = standardFormat.format(toDate);
                        }else{
                            toDateStr = preToDateStr;
                        }
                        
                        //BPN 속성 Update
                        HashMap<String, Object> m = new HashMap<String, Object>();
                        m.put("M7_BP_ID", ecoID + "_" + parentID + "_" + child.get_bl_occ_int_order_no());
                        m.put("M7_BP_DATE", (fromDateStr + " TO " + toDateStr).trim());
                        tcItemUtil.setAttributes(child, m);
                        bFound = true;
                        break;
                    }
                }
            }

			if( bFound ){
				log.append(IFConstants.TEXT_RETURN);
				log.append(updateBPNStat(parentID, parentRevID, childID, orderNo, SUCCESS, log));
			}else{
				log.append(IFConstants.TEXT_RETURN);
				log.append(updateBPNStat(parentID, parentRevID, childID, orderNo, CHILD_NOT_FOUND, log));
			}

		}catch(Exception e){
			log.append(IFConstants.TEXT_RETURN);
			log.append(updateBPNStat(parentID, parentRevID, childID, orderNo, ERROR, log));
			throw e;
		}

		return log.toString();
	}

	/**
	 * BPNStat Update 함수
	 *
	 * @param parentID
	 * @param parentRevID
	 * @param childID
	 * @param orderNo
	 * @param stat
	 * @param log
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private String updateBPNStat(String parentID, String parentRevID, String childID, String orderNo, String stat, StringBuffer log) throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		try{
			DataSet ds = new DataSet();
			ds.put("parent_id", parentID);
			ds.put("parent_rev_id", parentRevID);
			ds.put("child_id", childID);
			ds.put("order_no", orderNo);
			ds.put("stat", stat);
			commonDao.update("com.symc.interface.updateBpnStat", ds);
		}catch(Exception e){
			e.printStackTrace();
			log.append(IFConstants.TEXT_RETURN);
			log.append("ITEM_ID : " + parentID + ", ITEM_REVISION_ID : " + parentRevID + " is not found.");
			log.append(IFConstants.TEXT_RETURN);
			log.append("com.symc.interface.updateBpnStat execute Fail");
		}

		return log.toString();
	}
}
