package com.symc.work.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.util.TcConstants;
import com.symc.soa.service.ProcessSheetExcelUpdateService;
import com.symc.soa.service.WeldOPExcelUpdateService;
import com.symc.work.model.SYMCBOPEditData;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * [SR140716-041][20140710] shcho, MECO 결재시 사용자 승인 후 System에서  용접조건표에 MECO 날짜 업데이트를 수행하지 않는 오류 수정 
 * 1. MEW가 아닌 경우 용접조건표 ExcelUpdateService가 누락되어 용접조건표 MECO 날짜 업데이트가 되지 않는 오류 수정
 * 2. 작업표준서와 용접조건표의 ExcelUpdateService에서 중복적으로 처리하고 있어서 오류 발생. MECOService에서 일원화 함
 *
 */
public class MECOService {

	private Session session;
    private TcItemUtil tcItemUtil;


    @SuppressWarnings("unchecked")
	public List<HashMap<String,String>> searchMecoReadyToPerform() throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap<String,String>> arrMecoReadyToCompleteList = null;
		HashMap<String, String> parmaMap = new HashMap<String, String>();
		parmaMap.put("IS_COMPLETED", null);
		parmaMap.put("TABLE_NAME", "MECOREVISION");
		arrMecoReadyToCompleteList = (List<HashMap<String,String>>)commonDao.selectList("com.symc.meco.searchWorkflowReadyToComplete", parmaMap);
		
		return arrMecoReadyToCompleteList;
	}
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<SYMCBOPEditData> selectMECOEplList(String mecoNo) throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		ArrayList<SYMCBOPEditData> resultList = null;
		HashMap<String, String> parmaMap = new HashMap<String, String>();
		parmaMap.put("mecoNo", mecoNo);

		resultList = (ArrayList<SYMCBOPEditData>)commonDao.selectList("selectMECOEplList", parmaMap);

		return resultList;
	}
	
	
	public void startService() throws Exception{
		System.out.println("MECOService Excuted!!!");
		session = null;
		TcLoginService tcLoginService = null;
		tcItemUtil = null;
		ProcessSheetExcelUpdateService processSheetExcelUpdateService = null;
		WeldOPExcelUpdateService weldOPExcelUpdateService = null;
		try{
			tcLoginService = new TcLoginService();
			
			List<HashMap<String,String>> arrMecoReadyToCompleteList = searchMecoReadyToPerform();
			
			
			if(arrMecoReadyToCompleteList.size() > 0) {
				session = tcLoginService.getTcSession();
				tcItemUtil = new TcItemUtil(session);
				String meco_no = null;
				String meco_uid = null;
				ItemRevision[] itemRevision = null;
				HashMap<String, Object> updateMap = new HashMap<String, Object>();
				processSheetExcelUpdateService = new ProcessSheetExcelUpdateService(session);
				weldOPExcelUpdateService = new WeldOPExcelUpdateService(session);
				for( HashMap<String, String> resultMap : arrMecoReadyToCompleteList ){
					meco_no = (String) resultMap.get("MECO_NO");
					meco_uid = (String) resultMap.get("MECO_UID");
					itemRevision = tcItemUtil.getItemRevisionObjects(new String[] {meco_uid});
					updateMap.put("m7_IS_COMPLETED", "Ongoing");
					tcItemUtil.setAttributes(itemRevision[0], updateMap);
					
					//TODO : To write contents that approve history on excel file, call method
					if(meco_no.startsWith("MEW")) {
						weldOPExcelUpdateService.updateMECOReleaseInfo(meco_no);
					}else{
						processSheetExcelUpdateService.updateMECOReleaseInfo(meco_no);
						
						//[SR140716-041][20140710] shcho,  MECO 결재시 사용자 승인 후 System에서  용접조건표에 MECO 날짜 업데이트를 수행하지 않는 오류 수정 (MEW가 아닌 경우 용접조건표 ExcelUpdateService가 누락되어 용접조건표 MECO 날짜 업데이트가 되지 않는 오류 수정)
						tcItemUtil.getProperties(new ModelObject[] {itemRevision[0]},  new String[] {"m7_ORG_CODE"});
						String orgCode = itemRevision[0].getPropertyObject("m7_ORG_CODE").getStringValue();
						if(orgCode.equalsIgnoreCase("PB")) {
						    weldOPExcelUpdateService.updateMECOReleaseInfo(meco_no);
						}
					}
					
					//After calling method
					// [SR140716-041][20140710] shcho,  MECO 결재시 사용자 승인 후 System에서  용접조건표에 MECO 날짜 업데이트를 수행하지 않는 오류 수정(작업표준서와 용접조건표의 ExcelUpdateService에서 중복적으로 처리하고 있어서 오류 발생. MECOService에서 일원화 함)
                    // MECO Release
		            releaseMECO(itemRevision[0]);
				}
			}

		}catch(Exception e){
			throw e;
		} finally {
			if (session != null)
				session.logout();
		}
	}


    /**
     * MECO Release
     * 
     * [SR140716-041][20140710] shcho,  MECO 결재시 사용자 승인 후 System에서  용접조건표에 MECO 날짜 업데이트를 수행하지 않는 오류 수정(작업표준서와 용접조건표의 ExcelUpdateService에서 중복적으로 처리하고 있어서 오류 발생. MECOService에서 일원화 함)
     * 
     * @param itemRevision
     */
    public void releaseMECO(ItemRevision mecoRevision) {
        try{
        	WorkflowCompleteService completeService = new WorkflowCompleteService();
            completeService.startServiceForMeco(mecoRevision.getUid());

        } catch (Exception e) {
            e.printStackTrace();
            String result = "FAIL";
            Map<String, Object> propertyMap = new HashMap<String, Object>();
            propertyMap.put(TcConstants.MECO_IS_COMPLETED, result);
            try {
                tcItemUtil.setAttributes(mecoRevision, propertyMap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
