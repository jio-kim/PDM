package com.symc.work.service;

import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.util.IFConstants;

/**
 * [20150922][ymjang] 대상 MECO 검색 쿼리 변경함
 */
public class RoutingInfoService {

	private String getCurrentTime() throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		String curTime = (String)commonDao.selectOne("com.symc.weld.getCurrentTime", null);

		return curTime;
	}

	@SuppressWarnings("unused")
	public String startService() throws Exception{
		System.out.println("startService");
		StringBuffer log = new StringBuffer();

		try{
			String curTime = getCurrentTime();
			TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
			//1. Weld pont Service의 마지막 실행 시간을 가져온다.
			String latestServiceTimeStr = getLatestServiceTime(log);

			//2. Effect_Date 변경된건 내용 적용
			// ==> Preferences REVISION_EFFECTIVITY_MANAGE_ITEM에 해당하는 아이템으로 부터 Effectivity_Date가 변경된 사항이 발견되면
			//	이를 기존 Routing 정보에 적용함.
			commonDao.update("com.symc.routing.update_routing_info_with_eff", null);

			//3. 해당 ECO 추출
			List<HashMap<String, String>> list = getEcoFromLatestServiceTime(latestServiceTimeStr, log);
			for( int i = 0; list != null &&  i < list.size(); i++){
				HashMap<String, String> eco = list.get(i);
				String ecoNo = eco.get("MECO_NO");
				commonDao.update("com.symc.routing.insertRoutingInfo", eco);
			}

			HashMap<String, String> tmpMap = new HashMap<String, String>();
			tmpMap.put("CURRENT_TIME", curTime);
			commonDao.insert("com.symc.routing.insertRoutingServiceTime", tmpMap);
		}catch(Exception e){
			throw e;
		}

		return log.toString();
	}

	/**
	 * [20150922][ymjang] 대상 MECO 검색 쿼리 변경함
	 * @param latestServiceTimeStr
	 * @param log
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<HashMap<String, String>> getEcoFromLatestServiceTime( String latestServiceTimeStr, StringBuffer log )throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("MECO_NO", "MECPA15026");
//		map.put("DATE_STR", "");
//		map.put("LATEST_SERVICE_TIME", "");
		
		map.put("MECO_NO", "");
		map.put("DATE_STR", "");
		map.put("LATEST_SERVICE_TIME", latestServiceTimeStr);
		List<HashMap<String, String>> list = (List<HashMap<String, String>>)commonDao.selectList("com.symc.routing.getEcoFromLatestServiceTime", map);
		if( list == null || list.isEmpty()){
			log.append(IFConstants.TEXT_RETURN);
			log.append("ECO not found.");
		}

		return list;
	}

	private String getLatestServiceTime(StringBuffer log) throws Exception {
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();

		//1. Weld pont Service의 마지막 실행 시간을 가져온다.
		String latestServiceTimeStr = (String)commonDao.selectOne("com.symc.routing.getLatestServiceTime", null);
		if( latestServiceTimeStr == null || latestServiceTimeStr.equals("")){
			StringBuffer errorBuffer = new StringBuffer();
			errorBuffer.append(IFConstants.TEXT_RETURN);
			errorBuffer.append("Routing Service Start Time not found.");
			errorBuffer.append(IFConstants.TEXT_RETURN);
			errorBuffer.append("See the CUSTOM_WEB_ENV table and insert a LATEST_ROUTING_SERVICE_START_TIME value.");

			throw new Exception(errorBuffer.toString());
		}
		log.append("LATEST_ROUTING_SERVICE_START_TIME = [" + latestServiceTimeStr + "]");

		return latestServiceTimeStr;
	}
}
