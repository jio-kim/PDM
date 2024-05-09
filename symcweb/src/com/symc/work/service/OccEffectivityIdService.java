package com.symc.work.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.service.TcDataManagementService;
import com.symc.common.soa.service.TcStructureManagementService;
import com.symc.common.util.IFConstants;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.CFM_date_info;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * [20160922][ymjang] Pack Line 처리 기능 추가
 */
public class OccEffectivityIdService {

    private TcStructureManagementService strService;
    private TcDataManagementService dataService;
    private TcQueryService queryService;   
    private TcItemUtil tcItemUtil;
	private Session tcSession;	 
	
	/**
	 * 해당 일자에 설계 변경된 ECO 에 대하여 BOM Line 에 Occurrence Effectivity ID (ECO) 정보를 저장한다.
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
			
			/* **************************************************************************************
			 * 1. 해당 일자에 설계변경된 Parent 목록을 구한다.
			 * ************************************************************************************** */
	        // ECO 변경 Assembly 검색
			Calendar today = Calendar.getInstance();
			today.add(Calendar.DATE, -1);
			
	        DateFormat df = new SimpleDateFormat("yyyyMMdd"); 
	        String yesterday = df.format(today.getTime());
			//yesterday = "20160624";
					
			DataSet paramDs = new DataSet();
			paramDs.put("PDATE_RELEASED", yesterday);
			List<HashMap<String, String>> ecoTargetList = (List<HashMap<String, String>>)commonDao.selectList("com.symc.occeffectivityid.selectECODemonTarget", paramDs);
	        if(ecoTargetList == null || ecoTargetList.size() == 0) {
	            return "not found.";
	        }
			
			/* **************************************************************************************
			 * 2. 해당 일자에 설계변경된 Parent 목록을 구한다.
			 * ************************************************************************************** */
	        CFM_date_info ecoEffectivity = null;
	        HashMap<String, String> occECOMap = new HashMap<String, String>();
	        BOMWindow bomWindow = null;
	        BOMLine parentLine = null;
	        for(HashMap<String, String> ecoMap : ecoTargetList) {
	            String ecoNo = ecoMap.get("ECO_NO");
	            String parentNo = ecoMap.get("PART_NO");
	            String parentRevNo = ecoMap.get("PART_REV");
	            
	            if(ecoEffectivity == null || !ecoEffectivity.get_id().equals(ecoNo)) {
					ModelObject[] findObjects  = queryService.searchTcObject("__SYMC_occ_effectivity", new String[]{"effectivity_id"}, new String[]{ecoNo}, 
                            											     new String[]{"effectivity_id", "eff_date"});
					if (findObjects == null || findObjects.length == 0) {
						System.out.println("Not founded ECO(" + ecoNo + ") CFM Effectivity for BOM Parent(" + parentNo + "/" + parentRevNo + "). Skip to set occurrence Effectivity..\n");
						log.append("Not founded ECO(" + ecoNo + ") CFM Effectivity for BOM Parent(" + parentNo + "/" + parentRevNo + "). Skip to set occurrence Effectivity..\n");
						continue;
					}
	            }
	            
	            paramDs.clear();
	            paramDs.put("parentBVR", ecoMap.get("PART_BVR"));
	            paramDs.put("ecoNo", ecoNo);
	            paramDs.put("parentNo", parentNo);
	            paramDs.put("parentRev", parentRevNo);
	            paramDs.put("baseNo", ecoMap.get("BASE_NO"));
	            paramDs.put("baseRev", ecoMap.get("BASE_REV"));
	            
	            // PSOccurrence 의 IN_ECO No 검색
				List<HashMap<String, String>> occECOList = (List<HashMap<String, String>>)commonDao.selectList("com.symc.occeffectivityid.selectOccurrenceECO", paramDs);
	            if(occECOList == null || occECOList.size() == 0) {
	            	System.out.println("Not founded BOM(" + ecoNo + " / " + parentNo + "/" + parentRevNo + "). Skip to set occurrence Effectivity..\n");
	                continue;
	            }
	            
	            occECOMap.clear();
	            for(HashMap<String, String> occMap : occECOList) {
	                occECOMap.put(occMap.get("ROCC_THREADU"), occMap.get("ECO_NO"));
	            }
	            
				ItemRevision parentRevision = tcItemUtil.getRevisionInfo(parentNo, parentRevNo);
				if (parentRevision == null) {
					System.out.println("Not founded Parent(" + ecoNo + " / " + parentNo + "/" + parentRevNo + "). Skip to set occurrence Effectivity..\n");
					log.append("Not founded Parent(" + ecoNo + " / " + parentNo + "/" + parentRevNo + "). Skip to set occurrence Effectivity..\n");
					continue;
				}
				
				// 각 Parent 별로 Latest Released 기준의 BOM Window 를 Open 한다.
				CreateBOMWindowsResponse res = strService.createTopLineBOMWindow(parentRevision, strService.getRevisionRule(IFConstants.BOMVIEW_LATEST_RELEASED), null);
				try
				{
					bomWindow = res.output[0].bomWindow;
				    parentLine = res.output[0].bomLine;
					
					tcItemUtil.getProperties(new ModelObject[]{parentLine}, new String[]{"bl_child_lines"});
					
					ModelObject[] packedLines = null;
					ModelObject[] childObjects = parentLine.get_bl_child_lines();
					tcItemUtil.getProperties(childObjects, new String[]{"bl_occurrence_uid", "bl_has_date_effectivity"});
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
	    				
						tcItemUtil.getProperties(packedLines, new String[]{"bl_occurrence_uid", "bl_has_date_effectivity"});
						for ( ModelObject packedLine : packedLines ) {
							
							BOMLine line = (BOMLine)packedLine;
							String occThread = line.get_bl_occurrence_uid();
							String effStr = line.get_bl_has_date_effectivity();
							
							if(effStr == null || effStr.equals("")) {
								if(occECOMap.containsKey(occThread)) {
									HashMap<String, Object> m = new HashMap<String, Object>();
									m.put("bl_has_date_effectivity", occECOMap.get(occThread));
									tcItemUtil.setAttributes(line, m);
								}
							}
						}
					}
				} catch (Exception e) {
					throw e;
				} finally {
					if(bomWindow != null) {
						strService.saveBOMWindow(bomWindow);
						strService.closeBOMWindow(bomWindow);
						dataService.unloadObjects(new ModelObject[]{bomWindow});
					}
				}
	        }
        } catch (Exception e) {
            throw e;
        } finally {
            if (tcSession != null) {
            	tcSession.logout();
            }

        }

        return log.toString();
	}

	public void setSession(Session session) {
        this.tcSession = session;
    }
		
}
