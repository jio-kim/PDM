package com.symc.work.service;

import java.util.HashMap;

import com.symc.common.dao.TcCommonDao;

/**
 * [20150915][ymjang] 목표재료비시스템은 OSPEC HEADER 정보를 매주 새롭게 보내도록 기능 추가
 * [20160321][ymjang] 목표재료비시스템은 OSPEC HEADER 정보를 매주 새롭게 보내도록 기능 추가 - 다시 추가함.
 * [SR181206-041][20181206][KCH] SYSTEM_ROW_KEY 가져오는 값이 가끔 누락되는 현상이 있어 TC API -> DB Query 로 변경
 */
public class IFMasterFullService {

    /**
     * I/F 테이블에 VALUE정보를 등록
     * @param productAllChildPartsUsageList 
     *
     * @method createEnvValuesInfo
     * @date 2013. 8. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void insertMasterFullList(HashMap<String,HashMap<String,String>> productAllChildPartsList, HashMap<String,HashMap<String,HashMap<String,String>>> productAllChildPartsUsageList) throws Exception {
        TcCommonDao commonDao = TcCommonDao.getTcCommonDao();

        try
        {
            for (String mapKey : productAllChildPartsList.keySet())
            {
                String listID = commonDao.selectOne("com.symc.masterfull.selectMasterListKey", null).toString();

                productAllChildPartsList.get(mapKey).put("LIST_ID", listID);

                try {
                    commonDao.insert("com.symc.masterfull.insertPrebomMasterFullList", productAllChildPartsList.get(mapKey));
                } catch (Exception e) {
                    throw e;
                }

                HashMap<String,HashMap<String,String>> usageList = productAllChildPartsUsageList.get(mapKey);

                if (null != usageList && usageList.size() > 0) {
                    for (String sosKey : usageList.keySet())
                    {
                        HashMap<String, String> usageInfo = usageList.get(sosKey);
                        usageInfo.put("LIST_ID", listID);

                        try {
                            commonDao.insert("com.symc.masterfull.insertPrebomUsageFullList", usageInfo);
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                }
            }
            
            // [20150915][ymjang] 목표재료비시스템은 OSPEC HEADER 정보를 매주 새롭게 보내도록 기능 추가
            // [20160321][ymjang] 목표재료비시스템은 OSPEC HEADER 정보를 매주 새롭게 보내도록 기능 추가 - 다시 추가
            try {
//            	HashMap<String, String> paramMap = new HashMap<String, String> ();
//                commonDao.insert("com.symc.masterfull.insertPrebomTrimFullList", paramMap);
                commonDao.selectOne("com.symc.masterfull.insertPrebomTrimFullList", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
    
    /**
     * Bom Line에서 SYSTEM ROW KEY를 가져옴. 
     * @param parentRevPuid
     * @param childRevPuid
     * @return
     * @throws Exception
     */
    public String selectSystemRowKey(String occPuid) throws Exception {
    	
    	HashMap<String, String> revPuidList = new HashMap<String, String>();
    	revPuidList.put("PUID", occPuid);
    	
    	TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
    	String systemRowKey = null;
    	try {
    		systemRowKey = commonDao.selectOne("com.symc.masterfull.selectSystemRowKey", revPuidList).toString();
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return systemRowKey;
    }
    	
}
