package com.symc.common.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.util.TcConstants;
import com.symc.soa.service.SDVTCDataManager;
import com.symc.work.service.TcQueryService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.User;

/**
 * [20160913][ymjang] CCN IF 생성
 * [20160913][ymjang] CCN IF 생성 기능 프로시저로 이관함.
 * [20160903][ymjang] 동일한 LIST ID 를 계속 채번하는 문제 수정
 */
public class CCNUtil {

	public static TcCommonDao dbDao;
    public static TcItemUtil tcItemUtil;
    public static TcQueryService tcQueryService;
    public static SDVTCDataManager dataManager;
    
    public CCNUtil(Session tcSession) throws Exception {
    	this.dbDao = TcCommonDao.getTcCommonDao();
    	this.tcQueryService = new TcQueryService(tcSession);
    	this.tcItemUtil = new TcItemUtil(tcSession);        
    	this.dataManager =  new SDVTCDataManager(tcSession);
    }

    @SuppressWarnings("unchecked")
    public static void insertCCNInformation(HashMap<String, HashMap<String, Object>> ccnAttachedItemList) throws Exception {
        try
        {
        	// CCN_PREBOM_MASTER_LIST 및 해당 하는 CCN_PREBOM_USAGE 정보와 PREBOM_MASTER_FULL_LIST 및 해당하는 PREBOM_USAGE_FULL 정보를 읽어와서 최신의 정보를 가지고 작업을 해야한다. 
            // 최신정보에서 NEW에 있는 정보를 OLD로 모두 복제한다.
            // 재료비, 리비전, CCN ID 정보를 수정한다.
            // CCN_PREBOM_MASTER_LIST 및 CCN_PREBOM_USAGE 테이블에 Insert 한다.

            HashMap<String, String> queryParamMap = new HashMap<>();
            HashMap<String, HashMap<String, Object>> ccnNewInfos = new HashMap<String, HashMap<String, Object>>();

            for (String itemId : ccnAttachedItemList.keySet())
            {
                if (null == ccnAttachedItemList.get(itemId).get("CCN_NO") || ccnAttachedItemList.get(itemId).get("CCN_NO").equals("")) {
                    continue;
                }
                HashMap<String, Object> newData;
                queryParamMap.put("ITEM_ID", itemId);
                queryParamMap.put("ITEM_REV", ccnAttachedItemList.get(itemId).get("OLD_REV_ID").toString());
                
                String listID = dbDao.selectOne("com.symc.masterfull.selectMasterListKey", null).toString();
                System.out.println("listID : " + listID);
                List<HashMap<String, Object>> oldCCNInfos = (List<HashMap<String, Object>>) dbDao.selectList("com.symc.masterfull.getCCNMasterFullList", queryParamMap);
                List<HashMap<String, Object>> oldFullInfos = (List<HashMap<String, Object>>) dbDao.selectList("com.symc.masterfull.getPrebomMasterFullList", queryParamMap);

                if ((oldCCNInfos == null || oldCCNInfos.size() == 0) && (oldFullInfos != null && oldFullInfos.size() > 0))
                {
                    newData = copyOldPreFullToCCNMasterInfos(oldFullInfos.get(0), ccnAttachedItemList.get(itemId));
                }
                else if ((oldCCNInfos != null && oldCCNInfos.size() > 0) && (oldFullInfos == null || oldFullInfos.size() == 0))
                {
                    newData = copyOldCCNToCCNMasterInfos(oldCCNInfos.get(0), ccnAttachedItemList.get(itemId));
                }
                else
                {
                    int compDate = ((Date) oldCCNInfos.get(0).get("CREATION_DATE")).compareTo((Date) oldFullInfos.get(0).get("CREATION_DATE"));
                    if (compDate <= 0)
                    {
                        newData = copyOldPreFullToCCNMasterInfos(oldFullInfos.get(0), ccnAttachedItemList.get(itemId));
                    }
                    else
                    {
                        newData = copyOldCCNToCCNMasterInfos(oldCCNInfos.get(0), ccnAttachedItemList.get(itemId));
                    }
                }

                // 모두 하나로 묶자.
                newData.put("LIST_ID", listID);
                ccnNewInfos.put(itemId, newData);

                queryParamMap.clear();
                
                // [20160903][ymjang] 동일한 LIST ID 를 계속 채번하는 문제 수정
                dbDao.getTcSqlSession().clearCache();
            }
            if (null != ccnNewInfos && ccnNewInfos.size() > 0) {
                insertCCNEPLInfo(ccnNewInfos);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
        	if (dbDao.getTcSqlSession() != null) {
            	dbDao.getTcSqlSession().clearCache();
        	}
        }
    }
    
    /**
     * [20160913][ymjang] CCN IF 생성 기능 프로시저로 이관함.
     * @param ccnNewInfos
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    private static void insertCCNEPLInfo(HashMap<String, HashMap<String, Object>> ccnNewInfos) throws Exception {
    	
    	for (String itemId : ccnNewInfos.keySet()) {
            HashMap<String, Object> ccnEPLInfo = ccnNewInfos.get(itemId);            
            dbDao.insert("com.symc.masterfull.insertEPLList", ccnEPLInfo);
            //dbDao.insert("com.symc.masterfull.insertIfEPLList", ccnEPLInfo);
            List<HashMap<String, Object>> usageList = (List<HashMap<String, Object>>)ccnEPLInfo.get("USAGE_LIST");
            for (HashMap<String, Object> usageInfo : usageList) {
                usageInfo.put("LIST_ID", ccnEPLInfo.get("LIST_ID").toString());
                usageInfo.put("USAGE_QTY", usageInfo.get("USAGE_QTY").toString());
                usageInfo.put("HISTORY_TYPE", "OLD");
                dbDao.insert("com.symc.masterfull.insertEPLUsageInfo", usageInfo);
                //dbDao.insert("com.symc.masterfull.insertIfEPLUsageInfo", usageInfo);
                usageInfo.put("HISTORY_TYPE", "NEW");
                dbDao.insert("com.symc.masterfull.insertEPLUsageInfo", usageInfo);
                //dbDao.insert("com.symc.masterfull.insertIfEPLUsageInfo", usageInfo);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> copyOldPreFullToCCNMasterInfos(HashMap<String, Object> oldValue, HashMap<String, Object> toNewValue) throws Exception {
    	
    	HashMap<String, Object> newValue = new HashMap<String, Object>();
        try
        {
            newValue.put("CCN_ID", toNewValue.get("CCN_NO"));
            newValue.put("OLD_PROJECT", oldValue.get("PROJECT"));
            newValue.put("NEW_PROJECT", oldValue.get("PROJECT"));
            newValue.put("OLD_SYSTEM_CODE", oldValue.get("SYSTEM_CODE"));
            newValue.put("NEW_SYSTEM_CODE", oldValue.get("SYSTEM_CODE"));
            newValue.put("OLD_SYSTEM_NAME", oldValue.get("SYSTEM_NAME"));
            newValue.put("NEW_SYSTEM_NAME", oldValue.get("SYSTEM_NAME"));
            newValue.put("OLD_FUNCTION", oldValue.get("FUNCTION"));
            newValue.put("NEW_FUNCTION", oldValue.get("FUNCTION"));
            newValue.put("PARENT_NO", oldValue.get("PARENT_NO"));
            newValue.put("PARENT_UNIQUE_NO", oldValue.get("PARENT_UNIQUE_NO"));
            newValue.put("PARENT_REV", oldValue.get("PARENT_REV"));
            newValue.put("PARENT_NAME", oldValue.get("PARENT_NAME"));
            newValue.put("PREBOM_UNIQUE_ID", oldValue.get("PREBOM_UNIQUE_ID"));
            newValue.put("OLD_CHILD_NO", oldValue.get("CHILD_NO"));
            newValue.put("OLD_CHILD_UNIQUE_NO", oldValue.get("CHILD_UNIQUE_NO"));
            newValue.put("NEW_CHILD_NO", oldValue.get("CHILD_NO"));
            newValue.put("NEW_CHILD_UNIQUE_NO", oldValue.get("CHILD_UNIQUE_NO"));
            newValue.put("OLD_CHILD_REV", toNewValue.get("OLD_REV_ID"));
            newValue.put("NEW_CHILD_REV", toNewValue.get("NEW_REV_ID"));
            newValue.put("OLD_CHILD_NAME", oldValue.get("CHILD_NAME"));
            newValue.put("NEW_CHILD_NAME", oldValue.get("CHILD_NAME"));
            newValue.put("OLD_SEQ", oldValue.get("SEQ"));
            newValue.put("NEW_SEQ", oldValue.get("SEQ"));
            newValue.put("OLD_MANDATORY_OPT", oldValue.get("MANDATORY_OPT"));
            newValue.put("NEW_MANDATORY_OPT", oldValue.get("MANDATORY_OPT"));
            newValue.put("OLD_SPECIFICATION", oldValue.get("SPECIFICATION"));
            newValue.put("NEW_SPECIFICATION", oldValue.get("SPECIFICATION"));
            newValue.put("OLD_MODULE", oldValue.get("MODULE"));
            newValue.put("NEW_MODULE", oldValue.get("MODULE"));
            newValue.put("OLD_SMODE", oldValue.get("SMODE"));
            newValue.put("NEW_SMODE", oldValue.get("SMODE"));
            newValue.put("OLD_LEV", oldValue.get("LEV") == null ? "" : oldValue.get("LEV").toString());
            newValue.put("NEW_LEV", oldValue.get("LEV") == null ? "" : oldValue.get("LEV").toString());
            newValue.put("OLD_COLOR_ID", oldValue.get("COLOR_ID"));
            newValue.put("NEW_COLOR_ID", oldValue.get("COLOR_ID"));
            newValue.put("OLD_CATEGORY", oldValue.get("CATEGORY"));
            newValue.put("NEW_CATEGORY", oldValue.get("CATEGORY"));
            newValue.put("OLD_EST_WEIGHT", oldValue.get("EST_WEIGHT") == null ? "" : oldValue.get("EST_WEIGHT").toString());
            newValue.put("NEW_EST_WEIGHT", oldValue.get("EST_WEIGHT") == null ? "" : oldValue.get("EST_WEIGHT").toString());
            newValue.put("OLD_TGT_WEIGHT", oldValue.get("TGT_WEIGHT") == null ? "" : oldValue.get("TGT_WEIGHT").toString());
            newValue.put("NEW_TGT_WEIGHT", oldValue.get("TGT_WEIGHT") == null ? "" : oldValue.get("TGT_WEIGHT").toString());
            newValue.put("OLD_BOX", oldValue.get("BOX"));
            newValue.put("NEW_BOX", oldValue.get("BOX"));
            newValue.put("OLD_CONTENTS", oldValue.get("CONTENTS"));
            newValue.put("NEW_CONTENTS", oldValue.get("CONTENTS"));
            newValue.put("OLD_CHG_TYPE_ENGCONCEPT", oldValue.get("CHG_TYPE_ENGCONCEPT"));
            newValue.put("NEW_CHG_TYPE_ENGCONCEPT", oldValue.get("CHG_TYPE_ENGCONCEPT"));
            newValue.put("OLD_ORIGIN_PROJ", oldValue.get("ORIGIN_PROJ"));
            newValue.put("NEW_ORIGIN_PROJ", oldValue.get("ORIGIN_PROJ"));
            newValue.put("OLD_DC_ID", oldValue.get("DC_ID"));
            newValue.put("NEW_DC_ID", oldValue.get("DC_ID"));
            newValue.put("OLD_DC_REV", oldValue.get("DC_REV"));
            newValue.put("NEW_DC_REV", oldValue.get("DC_REV"));
            newValue.put("OLD_RELEASED_DATE", oldValue.get("RELEASED_DATE"));
            newValue.put("NEW_RELEASED_DATE", oldValue.get("RELEASED_DATE"));
            newValue.put("OLD_CON_DWG_PLAN", oldValue.get("CON_DWG_PLAN"));
            newValue.put("NEW_CON_DWG_PLAN", oldValue.get("CON_DWG_PLAN"));
            newValue.put("OLD_CON_DWG_PERFORMANCE", oldValue.get("CON_DWG_PERFORMANCE"));
            newValue.put("NEW_CON_DWG_PERFORMANCE", oldValue.get("CON_DWG_PERFORMANCE"));
            newValue.put("OLD_CON_DWG_TYPE", oldValue.get("CON_DWG_TYPE"));
            newValue.put("NEW_CON_DWG_TYPE", oldValue.get("CON_DWG_TYPE"));
            newValue.put("OLD_DWG_DEPLOYABLE_DATE", oldValue.get("DWG_DEPLOYABLE_DATE"));
            newValue.put("NEW_DWG_DEPLOYABLE_DATE", oldValue.get("DWG_DEPLOYABLE_DATE"));
            newValue.put("OLD_PRD_DWG_PLAN", oldValue.get("PRD_DWG_PLAN"));
            newValue.put("NEW_PRD_DWG_PLAN", oldValue.get("PRD_DWG_PLAN"));
            newValue.put("OLD_PRD_DWG_PERFORMANCE", oldValue.get("PRD_DWG_PERFORMANCE"));
            newValue.put("NEW_PRD_DWG_PERFORMANCE", oldValue.get("PRD_DWG_PERFORMANCE"));
            newValue.put("OLD_DVP_NEEDED_QTY", oldValue.get("DVP_NEEDED_QTY") == null ? "" : oldValue.get("DVP_NEEDED_QTY").toString());
            newValue.put("NEW_DVP_NEEDED_QTY", oldValue.get("DVP_NEEDED_QTY") == null ? "" : oldValue.get("DVP_NEEDED_QTY").toString());
            newValue.put("OLD_DVP_USE", oldValue.get("DVP_USE"));
            newValue.put("NEW_DVP_USE", oldValue.get("DVP_USE"));
            newValue.put("OLD_DVP_REQ_DEPT", oldValue.get("DVP_REQ_DEPT"));
            newValue.put("NEW_DVP_REQ_DEPT", oldValue.get("DVP_REQ_DEPT"));
            newValue.put("OLD_ENG_DEPT_NM", oldValue.get("ENG_DEPT_NM"));
            newValue.put("NEW_ENG_DEPT_NM", oldValue.get("ENG_DEPT_NM"));
            newValue.put("OLD_ENG_RESPONSIBLITY", oldValue.get("ENG_RESPONSIBLITY"));
            newValue.put("NEW_ENG_RESPONSIBLITY", oldValue.get("ENG_RESPONSIBLITY"));
            newValue.put("EST_COST_MATERIAL", toNewValue.get(TcConstants.PROP_S7_EST_COST_MATERIAL));
            newValue.put("TGT_COST_MATERIAL", toNewValue.get(TcConstants.PROP_S7_TARGET_COST_MATERIAL));
            newValue.put("SELECTED_COMPANY", oldValue.get("SELECTED_COMPANY"));
            newValue.put("PRT_TOOLG_INVESTMENT", oldValue.get("PRT_TOOLG_INVESTMENT"));
            newValue.put("PRD_TOOL_COST", oldValue.get("PRD_TOOL_COST"));
            newValue.put("PRD_SERVICE_COST", oldValue.get("PRD_SERVICE_COST"));
            newValue.put("PRD_SAMPLE_COST", oldValue.get("PRD_SAMPLE_COST"));
            newValue.put("PUR_TEAM", oldValue.get("PUR_TEAM"));
            newValue.put("PUR_RESPONSIBILITY", oldValue.get("PUR_RESPONSIBILITY"));
            newValue.put("EMPLOYEE_NO", oldValue.get("EMPLOYEE_NO"));
            newValue.put("CHANGE_DESC", "");
            newValue.put("OLD_PRD_PROJECT", oldValue.get("PRD_PROJECT"));
            newValue.put("NEW_PRD_PROJECT", oldValue.get("PRD_PROJECT"));
            newValue.put("OLD_ALTER_PART", oldValue.get("ALTER_PART"));
            newValue.put("NEW_ALTER_PART", oldValue.get("ALTER_PART"));
            newValue.put("OLD_REGULATION", oldValue.get("REGULATION"));
            newValue.put("NEW_REGULATION", oldValue.get("REGULATION"));
            newValue.put("OLD_ECO", oldValue.get("ECO"));
            newValue.put("NEW_ECO", oldValue.get("ECO"));
            newValue.put("OLD_SYSTEM_ROW_KEY", oldValue.get("SYSTEM_ROW_KEY"));
            newValue.put("NEW_SYSTEM_ROW_KEY", oldValue.get("SYSTEM_ROW_KEY"));

            List<HashMap<String, Object>> usageList = (List<HashMap<String, Object>>) dbDao.selectList("com.symc.masterfull.getPrebomMasterFullUsageList", oldValue.get("LIST_ID").toString());
            newValue.put("USAGE_LIST", usageList);
            return newValue;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> copyOldCCNToCCNMasterInfos(HashMap<String, Object> oldValue, HashMap<String, Object> toNewValue) throws Exception
    {
    	HashMap<String, Object> newValue;
        try
        {
            newValue = (HashMap<String, Object>) oldValue.clone();

            newValue.put("CCN_ID", toNewValue.get("CCN_NO"));
            newValue.put("OLD_PROJECT", oldValue.get("NEW_PROJECT"));
            newValue.put("OLD_SYSTEM_CODE", oldValue.get("NEW_SYSTEM_CODE"));
            newValue.put("OLD_SYSTEM_NAME", oldValue.get("NEW_SYSTEM_NAME"));
            newValue.put("OLD_FUNCTION", oldValue.get("NEW_FUNCTION"));
            newValue.put("OLD_CHILD_NO", oldValue.get("NEW_CHILD_NO"));
            newValue.put("OLD_CHILD_UNIQUE_NO", oldValue.get("NEW_CHILD_UNIQUE_NO"));
            newValue.put("OLD_CHILD_REV", toNewValue.get("OLD_REV_ID"));
            newValue.put("NEW_CHILD_REV", toNewValue.get("NEW_REV_ID"));
            newValue.put("OLD_CHILD_NAME", oldValue.get("NEW_CHILD_NAME"));
            newValue.put("OLD_SEQ", oldValue.get("NEW_SEQ"));
            newValue.put("OLD_MANDATORY_OPT", oldValue.get("NEW_MANDATORY_OPT"));
            newValue.put("OLD_SPECIFICATION", oldValue.get("NEW_SPECIFICATION"));
            newValue.put("OLD_MODULE", oldValue.get("NEW_MODULE"));
            newValue.put("OLD_SMODE", oldValue.get("NEW_SMODE"));
            newValue.put("OLD_LEV", oldValue.get("NEW_LEV") == null ? "" : oldValue.get("NEW_LEV").toString());
            newValue.put("NEW_LEV", oldValue.get("NEW_LEV") == null ? "" : oldValue.get("NEW_LEV").toString());
            newValue.put("OLD_COLOR_ID", oldValue.get("NEW_COLOR_ID"));
            newValue.put("OLD_CATEGORY", oldValue.get("NEW_CATEGORY"));
            newValue.put("OLD_EST_WEIGHT", oldValue.get("NEW_EST_WEIGHT") == null ? "" : oldValue.get("NEW_EST_WEIGHT").toString());
            newValue.put("OLD_TGT_WEIGHT", oldValue.get("NEW_TGT_WEIGHT") == null ? "" : oldValue.get("NEW_TGT_WEIGHT").toString());
            newValue.put("NEW_EST_WEIGHT", oldValue.get("NEW_EST_WEIGHT") == null ? "" : oldValue.get("NEW_EST_WEIGHT").toString());
            newValue.put("NEW_TGT_WEIGHT", oldValue.get("NEW_TGT_WEIGHT") == null ? "" : oldValue.get("NEW_TGT_WEIGHT").toString());
            newValue.put("OLD_BOX", oldValue.get("NEW_BOX"));
            newValue.put("OLD_CONTENTS", oldValue.get("NEW_CONTENTS"));
            newValue.put("OLD_CHG_TYPE_ENGCONCEPT", oldValue.get("NEW_CHG_TYPE_ENGCONCEPT"));
            newValue.put("OLD_ORIGIN_PROJ", oldValue.get("NEW_ORIGIN_PROJ"));
            newValue.put("OLD_DC_ID", oldValue.get("NEW_DC_ID"));
            newValue.put("OLD_DC_REV", oldValue.get("NEW_DC_REV"));
            newValue.put("OLD_RELEASED_DATE", oldValue.get("NEW_RELEASED_DATE"));
            newValue.put("OLD_CON_DWG_PLAN", oldValue.get("NEW_CON_DWG_PLAN"));
            newValue.put("OLD_CON_DWG_PERFORMANCE", oldValue.get("NEW_CON_DWG_PERFORMANCE"));
            newValue.put("OLD_CON_DWG_TYPE", oldValue.get("NEW_CON_DWG_TYPE"));
            newValue.put("OLD_DWG_DEPLOYABLE_DATE", oldValue.get("NEW_DWG_DEPLOYABLE_DATE"));
            newValue.put("OLD_PRD_DWG_PLAN", oldValue.get("NEW_PRD_DWG_PLAN"));
            newValue.put("OLD_PRD_DWG_PERFORMANCE", oldValue.get("NEW_PRD_DWG_PERFORMANCE"));
            newValue.put("OLD_DVP_NEEDED_QTY", oldValue.get("NEW_DVP_NEEDED_QTY") == null ? "" : oldValue.get("NEW_DVP_NEEDED_QTY").toString());
            newValue.put("NEW_DVP_NEEDED_QTY", oldValue.get("NEW_DVP_NEEDED_QTY") == null ? "" : oldValue.get("NEW_DVP_NEEDED_QTY").toString());
            newValue.put("OLD_DVP_USE", oldValue.get("NEW_DVP_USE"));
            newValue.put("OLD_DVP_REQ_DEPT", oldValue.get("NEW_DVP_REQ_DEPT"));
            newValue.put("OLD_ENG_DEPT_NM", oldValue.get("NEW_ENG_DEPT_NM"));
            newValue.put("OLD_ENG_RESPONSIBLITY", oldValue.get("NEW_ENG_RESPONSIBLITY"));
            newValue.put("EST_COST_MATERIAL", toNewValue.get(TcConstants.PROP_S7_EST_COST_MATERIAL));
            newValue.put("TGT_COST_MATERIAL", toNewValue.get(TcConstants.PROP_S7_TARGET_COST_MATERIAL));
            newValue.put("CHANGE_DESC", "");
            newValue.put("OLD_PRD_PROJECT", oldValue.get("NEW_PRD_PROJECT"));
            newValue.put("OLD_ALTER_PART", oldValue.get("NEW_ALTER_PART"));
            newValue.put("OLD_REGULATION", oldValue.get("NEW_REGULATION"));
            newValue.put("OLD_ECO", oldValue.get("NEW_ECO"));
            
            List<HashMap<String, Object>> usageList = (List<HashMap<String, Object>>) dbDao.selectList("com.symc.masterfull.getCCNMasterFullUsageList", oldValue.get("LIST_ID").toString());
            newValue.put("USAGE_LIST", usageList);
            return newValue;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static void insertCCNMaster(ArrayList<ItemRevision> ccnList, String type) throws Exception {
    	
    	tcItemUtil.getProperties(ccnList.toArray(new ModelObject[0]), new String[]{TcConstants.PROP_ITEM_ID, TcConstants.PROP_S7_PROJCODE, TcConstants.PROP_S7_SYSTEM_CODE, TcConstants.PROP_OWNING_USER, TcConstants.PROP_OWNING_GROUP, TcConstants.PROP_DATE_RELEASED, TcConstants.PROP_S7_OSPEC_NO, TcConstants.PROP_S7_GATENO});
        for (ItemRevision ccnRevision : ccnList) {
            HashMap<String, Object> ccnMasterInfo = new HashMap<String, Object>();
            ccnMasterInfo.put("CCN_NO", ccnRevision.get_item_id());
            System.out.println("CCNMaster CCN_NO : " + ccnRevision.get_item_id());
            List<HashMap<String, Object>> resultList = (List<HashMap<String, Object>>) dbDao.selectList("com.symc.masterfull.selectMasterSystemCode", ccnMasterInfo);
            String sysCodes = "";
            for (int i = 0; i < resultList.size(); i++) {
                if (null != resultList.get(i)) {
                    sysCodes += (String) resultList.get(i).get("MASTER_LIST_SYSCODE");
                    if ((i + 1) == resultList.size()) {
                        break;
                    }
                    sysCodes += ", ";
                }
            }
            ccnMasterInfo.put("AFFETED_SYS_CODE", sysCodes);
            ccnMasterInfo.put("PROJECT_CODE", ccnRevision.getPropertyObject(TcConstants.PROP_S7_PROJCODE).getStringValue());
            ccnMasterInfo.put("PROJECT_TYPE", "02");
            ccnMasterInfo.put("SYSTEM_CODE", ccnRevision.getPropertyObject(TcConstants.PROP_S7_SYSTEM_CODE).getStringValue());
//            ccnMasterInfo.put("OSPEC_NO", "");
            User user = (User) ccnRevision.get_owning_user();
            String createUser = user.get_user_id();
            
            // ccn owing user 가 'if_system' 인 경우 'system' 으로 변경함.
            // if_ccn_master I/F 시 CREATOR column size가 수신하는 측은 6 bytes 라서 발생하는 오류를 대응하기 위함.
            try {
            	if( createUser != null && createUser.equals("if_system") )
                {
                	createUser = "system";
                }
            }catch(Exception e) {
            	
            }
            
            Group group = (Group) ccnRevision.get_owning_group();
            String dept = group.get_name();
            
            ccnMasterInfo.put("CREATOR", createUser);
            ccnMasterInfo.put("DEPT_NAME", dept);
//            ccnMasterInfo.put("CREATOR", "001862");
//            ccnMasterInfo.put("DEPT_NAME", "e/i");
            ccnMasterInfo.put("REGULATION", "N");
            ccnMasterInfo.put("COST_DOWN", "N");
            ccnMasterInfo.put("ORDERING_SPEC", "N");
            ccnMasterInfo.put("QUALITY_IMPROVEMENT", "N");
            ccnMasterInfo.put("CORRECTION_OF_EPL", "N");
            ccnMasterInfo.put("STYLING_UPDATE", "N");
            ccnMasterInfo.put("WEIGHT_CHANGE", "N");
            if (type.endsWith("cost")) {
                ccnMasterInfo.put("MATERIAL_COST_CHANGE", "Y");
                ccnMasterInfo.put("THE_OTHERS", "N");
                ccnMasterInfo.put("CHG_DESC", "modify Cost");
            }else{
                ccnMasterInfo.put("MATERIAL_COST_CHANGE", "N");
                ccnMasterInfo.put("THE_OTHERS", "Y");
                ccnMasterInfo.put("CHG_DESC", "modify Vendor");
            }
            ccnMasterInfo.put("OSPEC_NO", ccnRevision.getPropertyObject(TcConstants.PROP_S7_OSPEC_NO).getStringValue()); 
            ccnMasterInfo.put("GATE", ccnRevision.getPropertyObject(TcConstants.PROP_S7_GATENO).getStringValue());
            
            ccnMasterInfo.put("RELEASE_DATE", ccnRevision.get_date_released().getTime());
            
            dbDao.selectList("com.symc.masterfull.insertCCNMaster", ccnMasterInfo);
            //dbDao.selectList("com.symc.masterfull.insertIfCCNMaster", ccnMasterInfo);
        }
    }
    
    /**
     * [20160913][ymjang] CCN IF 생성
     * @param ccnList
     * @param type
     * @throws Exception
     */
    public static void insertIfCCN(ArrayList<ItemRevision> ccnList, String type) throws Exception {
        
        for (ItemRevision ccnRevision : ccnList) {
        	String ccnNo = ccnRevision.get_item_id();        	
        	HashMap<String, Object> ccnMasterInfo = new HashMap<String, Object>();
        	ccnMasterInfo.put("CCN_NO", ccnNo);
            System.out.println("IfCCNMaster CCN_NO : " + ccnRevision.get_item_id());
        	dbDao.selectOne("com.symc.masterfull.createIfCCN", ccnMasterInfo);
        }
    }
    
    public static String[] getOspecId(String projectCode) throws Exception{
        ModelObject[] modelObjects = tcQueryService.searchTcObject("__SYMC_S7_PreProductRevision", new String[]{"Project Code"}, new String[]{projectCode}, new String[]{"s7_OSPEC_NO"});
        if (modelObjects == null) 
        	return null;
        
        ItemRevision preProductRev = null;
        for (ModelObject modelObject : modelObjects) {
            preProductRev = tcItemUtil.getLastReleaseRevItem(((ItemRevision) modelObject).get_item_id());
            if (null != preProductRev) {
                tcItemUtil.getProperties(new ItemRevision[]{preProductRev}, new String[]{"s7_OSPEC_NO", "s7_GATE_NO"});
                String[] preProductInfo = {preProductRev.getPropertyObject("s7_OSPEC_NO").getStringValue(), preProductRev.getPropertyObject("s7_GATE_NO").getStringValue()};
                return preProductInfo;
            }
        }
        return null;
    }
}