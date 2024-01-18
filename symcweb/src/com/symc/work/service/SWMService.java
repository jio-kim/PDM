package com.symc.work.service;

import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.soa.service.SWMDocExcelUpdateService;

public class SWMService {

	@SuppressWarnings("unchecked")
	public List<HashMap<String,String>> searchSwmReadyToPerform() throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap<String,String>> arrMecoReadyToCompleteList = null;
		HashMap<String, String> parmaMap = new HashMap<String, String>();
		parmaMap.put("TABLE_NAME", "STDWORKMETHODREVISION");
		parmaMap.put("MATURITY", null);
		arrMecoReadyToCompleteList = (List<HashMap<String,String>>)commonDao.selectList("com.symc.meco.searchWorkflowReadyToComplete", parmaMap);

		return arrMecoReadyToCompleteList;
	}

	public void startService() throws Exception{
		System.out.println("SWMService.startService()");
		Session session = null;
		TcLoginService tcLoginService = null;
		SWMDocExcelUpdateService swmDocExcelUpdateService = null;
		try{
			tcLoginService = new TcLoginService();

			List<HashMap<String,String>> arrMecoReadyToCompleteList = searchSwmReadyToPerform();

			if(arrMecoReadyToCompleteList.size() > 0) {
				session = tcLoginService.getTcSession();
				swmDocExcelUpdateService = new SWMDocExcelUpdateService(session);
				
                swmDocExcelUpdateService.updateSWMDOCReleaseInvoker(arrMecoReadyToCompleteList);
			}

		}catch(Exception e){
			throw e;
		} finally {
			if (session != null)
				session.logout();
		}
	}
}
