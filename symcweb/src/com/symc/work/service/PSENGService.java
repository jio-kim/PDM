package com.symc.work.service;

import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.soa.service.ProcessSheetExcelUpdateService;

/**
 * [SR141119-021][20150119] ymjang, 영문 작업표준서 결재란 공백 오류 수정 의뢰
 * 1. 최초 생성
 *
 */
public class PSENGService {

	private Session session;


    @SuppressWarnings("unchecked")
    public List<HashMap<String,String>> searchReadyToComplete() throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap<String,String>> arrReadyToCompleteList = null;
		HashMap<String, String> parmaMap = new HashMap<String, String>();
		parmaMap.put("ps7_maturity", "In Work");   // 결재는 되었는데, Maturity 가 'In Work' 인 경우
		parmaMap.put("pitem_id", "EPS%");          // 영문 작업표준서 만
		arrReadyToCompleteList = (List<HashMap<String,String>>)commonDao.selectList("com.symc.ps.searchWorkflowReadyToComplete", parmaMap);
		
		return arrReadyToCompleteList;
	}
	
	@SuppressWarnings("rawtypes")
    public void startService() throws Exception{
		System.out.println("PSENGService Excuted!!!");
		session = null;
		TcLoginService tcLoginService = null; 
		ProcessSheetExcelUpdateService processSheetExcelUpdateService = null; 
		try{
			tcLoginService = new TcLoginService();
			
			List<HashMap<String,String>> arrReadyToCompleteList = searchReadyToComplete();
			
			if(arrReadyToCompleteList != null && arrReadyToCompleteList.size() > 0) {
				session = tcLoginService.getTcSession(); 
				String ps_item_id = null;
				String ps_item_puid = null;
				processSheetExcelUpdateService = new ProcessSheetExcelUpdateService(session); 
				for( HashMap resultMap : arrReadyToCompleteList ){
					ps_item_id = (String) resultMap.get("PITEM_ID");
					ps_item_puid = (String) resultMap.get("ITEM_REV_PUID");
					processSheetExcelUpdateService.updatePSReleaseInfo(ps_item_id, ps_item_puid);
				}
			}

		}catch(Exception e){
			throw e;
		} finally {
			if (session != null)
				session.logout();
		}
	}
}
