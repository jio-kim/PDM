package com.symc.work.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * [20160922][ymjang] Pack Line 처리 기능 추가
 * [20160923][ymjang] 배치 처리일자 변경에 따라 현재일자로 변경함.
 */
public class BPInterfaceService {

    private TcStructureManagementService strService;
    private TcDataManagementService dataService;
    private TcQueryService queryService;   
    private TcItemUtil tcItemUtil;
	private Session tcSession;
	 
	/**
	 * IF_BP 테이블에 있는 BP_DATE 를 TC에 적용한다.
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Object startService() throws Exception{

		StringBuffer log = new StringBuffer();
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		
		try {
			
			if (tcSession == null)
			{
				TcLoginService tcLoginService = new TcLoginService();
				tcSession = tcLoginService.getTcSession();
			}
			
			strService = new TcStructureManagementService(tcSession);
			dataService = new TcDataManagementService(tcSession);
			queryService = new TcQueryService(tcSession);
			tcItemUtil = new TcItemUtil(tcSession);
			
			BOMWindow window = null;
			BOMLine parentLine = null;
			List<HashMap<String, String>> childBpList = null;
			DataSet paramDs = new DataSet();
			
			/* **************************************************************************************
			 * 1. Parent 별로 BOM Window 를 생성하기 위하여 Parent 목록을 구한다.
			 * ************************************************************************************** */
			// DB Scheduler 에 의해서 매일 새벽(03:00) 에 IF_BP 데이터가 생성
			// BOM Line 에 BP 정보 Update 는 동일한 일자에 04:00  
			Calendar today = Calendar.getInstance();
	        // [20160923][ymjang] 배치 처리일자 변경에 따라 현재일자로 변경함.
			//today.add(Calendar.DATE, -1);
					
	        DateFormat df = new SimpleDateFormat("yyyyMMdd");
	        DateFormat dtf = new SimpleDateFormat("yyyyMMddHHmmss");
	        String yesterday = df.format(today.getTime());
			//yesterday = "20160819";
					
			// BP 대상 PUID 를 구한다.
			paramDs.put("PDATE_RELEASED", yesterday);
			HashMap<String, String> bpPuidMap = (HashMap<String, String>)commonDao.selectOne("com.symc.interface.getMaxBpPuid", paramDs);
			if (bpPuidMap == null) {
				return null;
			}
			
			String bpUid = bpPuidMap.get("BP_PUID");
			
			/* **************************************************************************************
			 * 2. BOM Change Type in ('A', 'C') 처리
			 * ************************************************************************************** */
			paramDs.clear();
			paramDs.put("BP_PUID", bpUid);
			List<HashMap<String, String>> newParentList = (List<HashMap<String, String>>)commonDao.selectList("com.symc.interface.getNewParentList", paramDs);
			for (int i = 0; newParentList != null && i < newParentList.size(); i++) {
				
				HashMap<String, String> parentMap = newParentList.get(i);
				String parentId = parentMap.get("P_PART_NO");
				String parentRev = parentMap.get("P_PART_REV");
				String oldParentRev = parentMap.get("OLD_P_PART_REV");
				String eff_str = parentMap.get("EFF");
				
				if (parentId == null || eff_str == null ) {
					continue;
				}
				
				/* **************************************************************************************
				 * 2-1. 각 Parent 별로 Child 를 조회하고, DB 스케줄러에 의해 생성된 BP 정보를 가져온다.
				 * ************************************************************************************** */
				paramDs.clear();
				paramDs.put("BP_PUID", bpUid);
				paramDs.put("P_PART_NO", parentId);
				paramDs.put("P_PART_REV", parentRev);
				paramDs.put("BOM_CHANGE_TYPE", new String[]{"A", "C"});
				
				childBpList = (List<HashMap<String, String>>)commonDao.selectList("com.symc.interface.getBpnToUpdate", paramDs);
				if (childBpList != null && childBpList.size() > 0) {
					
					// Parent Revision Search
					ModelObject[] findParentObjects  = queryService.searchTcObject(TcConstants.SEARCH_ITEM_REVISION, new String[]{"Item ID", "Revision"}, 
							                                                                                          new String[]{parentId, parentRev}, 
																													  new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID});

					if (findParentObjects == null || findParentObjects.length == 0) {
						System.out.println(parentId + " - " + parentRev + " Parent Not Found.");
						continue;
					}
					
					// 각 Parent 별로 Latest Released 기준의 BOM Window 를 Open 한다.
					ItemRevision parentRevision = (ItemRevision) findParentObjects[0];
					Date eff_date = dtf.parse(eff_str);
					CreateBOMWindowsResponse res = strService.createTopLineBOMWindow(parentRevision, strService.getRevisionRule(IFConstants.BOMVIEW_LATEST_RELEASED), null);
					
					// BP 정보 Update 
					try
					{
						window = res.output[0].bomWindow;
						parentLine = res.output[0].bomLine;
						
						tcItemUtil.getProperties(new ModelObject[]{parentLine}, new String[]{"bl_child_lines"});
						
						ModelObject[] packedLines = null;
						ModelObject[] childObjects = parentLine.get_bl_child_lines();
						tcItemUtil.getProperties(childObjects, new String[]{"bl_revision", "bl_occ_int_order_no", "M7_BP_ID", "M7_BP_DATE"});
						for (ModelObject childObject : childObjects) {
							
							BOMLine childLine = (BOMLine)childObject;
							
							// [20160922][ymjang] Pack Line 처리 기능 추가
							if (childLine.get_bl_is_packed()) {
								packedLines = childLine.get_bl_packed_lines();
								packedLines = new ModelObject[childLine.get_bl_packed_lines().length + 1];
								packedLines[0] = childLine;
								System.arraycopy(childLine.get_bl_packed_lines(), 0, packedLines, 1, childLine.get_bl_packed_lines().length);
							} else {
								packedLines = new ModelObject[]{childLine};
							}
		    				
							tcItemUtil.getProperties(packedLines, new String[]{"bl_revision", "bl_occ_int_order_no", "M7_BP_ID", "M7_BP_DATE"});
							for ( ModelObject packedLine : packedLines ) {
								BOMLine line = (BOMLine)packedLine;
								ItemRevision revision = (ItemRevision)line.get_bl_revision();
								if (revision == null) {
									continue;
								}
								tcItemUtil.getProperties(new ModelObject[]{revision}, BOMLineService.DEFAULT_ITEM_REVISION_PROPERTIES);
								
								// BP 정보 Update
								setBPInfo (parentMap, childBpList, line, revision.get_item_id(), revision.get_item_revision_id(), String.valueOf(line.get_bl_occ_int_order_no()));
							}
						}
						
					} catch (Exception e) {
						throw e;
					} finally {
						if(window != null) {
							strService.saveBOMWindow(window);
							strService.closeBOMWindow(window);
							dataService.unloadObjects(new ModelObject[]{window});
						}
					}
				}
				
			} // for (int i = 0; newParentList != null && i < newParentList.size(); i++)
			
			/* **************************************************************************************
			 * 3. BOM Change Type in ('B', 'D') 처리
			 * ************************************************************************************** */
			/* **************************************************************************************
			 * Old Part 의 경우, 이전의 설계변경에서 최초로 붙었을 때의 Parent 와 
			 *                   Release 일자를 기준으로 BOM 을 전개하여야 한다.
			 * ************************************************************************************** */
			paramDs.clear();
			paramDs.put("BP_PUID", bpUid);
			List<HashMap<String, String>> oldParentList = (List<HashMap<String, String>>)commonDao.selectList("com.symc.interface.getOldParentList", paramDs);
			for (int i = 0; oldParentList != null && i < oldParentList.size(); i++) {
				
				HashMap<String, String> parentMap = oldParentList.get(i);
				String parentId = parentMap.get("P_PART_NO");
				String parentRev = parentMap.get("P_PART_REV");
				String oldParentRev = parentMap.get("OLD_P_PART_REV"); 
				String eff_str = parentMap.get("EFF");
				
				if (parentId == null || eff_str == null ) {
					continue;
				}
				
				/* **************************************************************************************
				 * 3-1. 각 Parent 별로 Child 를 조회하고, DB 스케줄러에 의해 생성된 BP 정보를 가져온다.
				 * ************************************************************************************** */
				paramDs.clear();
				paramDs.put("BP_PUID", bpUid);
				paramDs.put("P_PART_NO", parentId);
				paramDs.put("P_PART_REV", parentRev);
				paramDs.put("BOM_CHANGE_TYPE", new String[]{"B", "D"});
				
				childBpList = (List<HashMap<String, String>>)commonDao.selectList("com.symc.interface.getBpnToUpdate", paramDs);
				if (childBpList != null && childBpList.size() > 0) {
					
					// Parent Revision Search
					ModelObject[] findParentObjects  = queryService.searchTcObject(TcConstants.SEARCH_ITEM_REVISION, new String[]{"Item ID", "Revision"}, 
							                                                                                          new String[]{parentId, oldParentRev}, 
																													  new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID});

					if (findParentObjects == null || findParentObjects.length == 0) {
						System.out.println(parentId + " - " + oldParentRev + " Parent Not Found.");
						continue;
					}
					
					// 각 Parent 별로 Latest Released 기준의 BOM Window 를 Open 한다.
					ItemRevision parentRevision = (ItemRevision) findParentObjects[0];
					Date eff_date = dtf.parse(eff_str);
					CreateBOMWindowsResponse res = strService.createTopLineBOMWindow(parentRevision, strService.getRevisionRule(IFConstants.BOMVIEW_LATEST_RELEASED), eff_date);
					
					// BP 정보 Update 
					try
					{
						window = res.output[0].bomWindow;
						parentLine = res.output[0].bomLine;
						
						tcItemUtil.getProperties(new ModelObject[]{parentLine}, new String[]{"bl_child_lines"});
						
						ModelObject[] packedLines = null;
						ModelObject[] childObjects = parentLine.get_bl_child_lines();
						tcItemUtil.getProperties(childObjects, new String[]{"bl_revision", "bl_occ_int_order_no", "M7_BP_ID", "M7_BP_DATE"});
						for (ModelObject childObject : childObjects) {
							
							BOMLine childLine = (BOMLine)childObject;
							
							// [20160922][ymjang] Pack Line 처리 기능 추가
							if (childLine.get_bl_is_packed()) {
								packedLines = childLine.get_bl_packed_lines();
								packedLines = new ModelObject[childLine.get_bl_packed_lines().length + 1];
								packedLines[0] = childLine;
								System.arraycopy(childLine.get_bl_packed_lines(), 0, packedLines, 1, childLine.get_bl_packed_lines().length);
							} else {
								packedLines = new ModelObject[]{childLine};
							}
		    				
							tcItemUtil.getProperties(packedLines, new String[]{"bl_revision", "bl_occ_int_order_no", "M7_BP_ID", "M7_BP_DATE"});
							for ( ModelObject packedLine : packedLines ) {
								BOMLine line = (BOMLine)packedLine;
								ItemRevision revision = (ItemRevision)line.get_bl_revision();
								if (revision == null) {
									continue;
								}
								tcItemUtil.getProperties(new ModelObject[]{revision}, BOMLineService.DEFAULT_ITEM_REVISION_PROPERTIES);
								
								// BP 정보 Update
								setBPInfo (parentMap, childBpList, line, revision.get_item_id(), revision.get_item_revision_id(), String.valueOf(line.get_bl_occ_int_order_no()));
							}
						}
						
					} catch (Exception e) {
						throw e;
					} finally {
						if(window != null) {
							strService.saveBOMWindow(window);
							strService.closeBOMWindow(window);
							dataService.unloadObjects(new ModelObject[]{window});
						}
					}
				}
			} // for (int i = 0; oldParentList != null && i < oldParentList.size(); i++) {
			 
        } catch (Exception e) {
            throw e;
        } finally {
            if (tcSession != null) {
            	tcSession.logout();
            }

        }

        return log.toString();
	}

	/**
	 * 해당 Child 의 BP 정보를 찾아 Update 한다. 
	 * @param childBpList
	 * @param childLine
	 * @param childId
	 * @param childRev
	 * @param orderNo
	 * @return
	 * @throws Exception
	 */
	private boolean setBPInfo(HashMap<String, String> parentMap, List<HashMap<String, String>> childBpList, BOMLine childLine, String childId, String childRev, String orderNo) throws Exception {
		
		boolean isFound = false;
		String bpInDate = null; String bpOutDate = null;
		HashMap<String, Object> m = new HashMap<String, Object>();
		for (int i = 0; childBpList != null && i < childBpList.size(); i++) {
			HashMap<String, String> childBpMap = childBpList.get(i);
			if (childBpMap.get("PART_NO").equals(childId) && 
				childBpMap.get("PART_REV").equals(childRev) &&
				childBpMap.get("ORDER_NO").equals(orderNo) ) {
			
				if (childBpMap.get("BP_IN") != null) {
					bpInDate = childBpMap.get("BP_IN");
					bpOutDate = childBpMap.get("BP_OUT") == null ? "" : childBpMap.get("BP_OUT");
					//bpOutDate = childMap.get("BP_OUT") == null ? "9999-12-31 23:59:59" : childMap.get("BP_OUT");
					m.clear();
                    m.put("M7_BP_ID", childBpMap.get("BP_ID"));
                    m.put("M7_BP_DATE", (bpInDate + " TO " + bpOutDate).trim());
                    tcItemUtil.setAttributes(childLine, m);
                    
    				System.out.println("Parent : " + parentMap.get("P_PART_NO") + " " + parentMap.get("P_PART_REV") + " " + parentMap.get("OLD_P_PART_REV") +  " " + parentMap.get("EFF") +  " " + 
							           "Child : " + childBpMap.get("PART_NO") + " " + childBpMap.get("PART_REV") + " " + parentMap.get("ORDER_NO")  +  " " + 
							           "Bp : " + (bpInDate + " TO " + bpOutDate).trim() );
				}
				
                isFound = true;
				break;	
			}
		}
		
		return isFound;
	}
	
	public void setSession(Session session) {
        this.tcSession = session;
    }
		
}
