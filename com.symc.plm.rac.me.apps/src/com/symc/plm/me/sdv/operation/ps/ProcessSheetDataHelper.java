package com.symc.plm.me.sdv.operation.ps;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;

import com.ibm.icu.text.DecimalFormat;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.dto.EndItemData;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomBOPDao;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;


/**
 * [SR140915-015][20141030]shcho, 조립 DR Type LOV표시에 '(조립)' 문자 추가로 인하여 값 표시방식 변경 (작업표준서에는 '(조립)'문자 제거)
 * [SR141118-031][20141118]shcho, Revise된 후에 Activity 추가시, 변경기호 생성하는 부분에서 이전 Revision의 Activity와 비교하면서 배열의 크기가 서로 다름으로 인하여 발생하는 오류 수정 
 * [SR150317-021][20150323]ymjang, 국문 작업표준서 Republish 방지토록 개선
 * [SR150312-024][20150324]ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
 * [SR150105-031][20150401] shcho, 작업표준서 원자재(E/Item) 표시 Logic 변경 ( E/Item Part No., 설계에서 부여한 Option, 생기(공기)에서 부여한 Find No.가 동일하면 Pack하여 작업표준서에 표기)
 * [SR150414-017][20150413] shcho, MECO SYMBOL이 Null(신규 변경 등록) 인 경우에 대하여 Null 처리 추가
 * [SR150612-003][20150612] shcho, Find No. 재부여 (1부터 건너뛰는 번호 없이 순차적으로 증가하도록 부여)
 *                                                Pack 작업시 사전에 Find No.를 건너뛰는 번호 없이 순차적으로 부여해주던 것을
 *                                                Pack 작업 이후에 Find No.를 순차적으로 재부여토록 수정함.
 * [NON-SR][20150706] shcho, Find No. 단순히 순차적으로 부여하는것이 아니라, 건너뛰는 경우에만 번호를 당겨 순차적으로 하도록 변경. 
 *                                       (예 : 만약 1이라는 순번이 2개 인 경우, 2개 모두 순번 1을 그대로 유지 해야 함.)         
 */
public class ProcessSheetDataHelper {

    private String processType;
    private int configId;

    private Registry registry;

    private List<HashMap<String, Object>> mecoList;

    public ProcessSheetDataHelper(String processType, int configId) {
        this.processType = processType;
        this.configId = configId;
        this.registry = Registry.getRegistry(this);
    }

    /**
     * MEPL을 Reload 해야하는지 여부를 Check 하고 그 결과를 Return 한다.
     * @param operationLine
     * @return MEPL Reload가 필요하면 True를 Return 한다.
     */
    public boolean checkMeplLoad(TCComponentBOPLine operationLine) {
        boolean opLoadEplFlag = false;
        boolean stationLoadEplFlag = false;

        try {
            TCComponentItemRevision operationRevision = operationLine.getItemRevision();
            TCComponentChangeItemRevision opMecoRevision = (TCComponentChangeItemRevision) operationRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
            String opMecoId = opMecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

            TCComponentItemRevision stationRevision = null;
            TCComponentItemRevision stationMecoRevision = null;

            opLoadEplFlag = requiredLoadEPL(operationRevision, opMecoId);
            if("B".equals(processType) || "P".equals(processType)) {
            	// 차체와  Paint는 Station이 있으므로 Station도 확인 한다.
                TCComponentBOPLine stationLine = ProcessSheetUtils.getParent(operationLine, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                if(stationLine != null) {
                    stationRevision = stationLine.getItemRevision();
                    stationMecoRevision = (TCComponentChangeItemRevision) stationRevision.getReferenceProperty(SDVPropertyConstant.STATION_MECO_NO);
                    String stationMecoId = stationMecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

                    stationLoadEplFlag = requiredLoadEPL(stationRevision, stationMecoId);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            MessageBox.post(UIManager.getCurrentDialog().getShell(), "Fail to Check MECO EPL.", "Check MEPL", MessageBox.ERROR);
        }

        return opLoadEplFlag || stationLoadEplFlag;
    }

    /**
     * 주어진 Item Revision의 최종 수정일과 MECO의 MEPL Load 된 시간을 비교해서 MEPL을 Reload 해야 할지 여부를 Return 한다.
     * @param itemRevision
     * @param mecoId
     * @return MEPL Reload가 필요하면 true를 Return 한다.
     * @throws Exception
     */
    private boolean requiredLoadEPL(TCComponentItemRevision itemRevision, String mecoId) throws Exception {
        boolean loadFlag = true;

        TCComponent[] bomViews = itemRevision.getRelatedComponents(SDVTypeConstant.BOMLINE_RELATION);
        if(bomViews == null) {
            loadFlag = false;
        } else {
            for(TCComponent bomView : bomViews) {
                if(bomView.getType().equals(SDVTypeConstant.BOMLINE_ITEM_REVISION)) {
                	Date lastModfDate = bomView.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
                    //String lastModifiedDate = SDVStringUtiles.dateToString(lastModfDate, "yyyy-MM-dd HH:mm:ss");
                    Date lastEPLLoadDate = getLastEPLLoadDate(itemRevision, mecoId);
                    if(lastEPLLoadDate != null) {
                        //String strlastEPLLoadDate = SDVStringUtiles.dateToString(lastEPLLoadDate, "yyyy-MM-dd HH:mm:ss");
                        // EPL Load 한 시간이 Structure가 마지막으로 수정된 시간보다 늦으면(값이 크면) EPL Reload 대상 아님.
                        if(lastEPLLoadDate.compareTo(lastModfDate)>0){
                        	loadFlag = false;
                        }
						//if(strlastEPLLoadDate.compareTo(lastModifiedDate) >= 0) {
						//    loadFlag = false;
						//}
                    }
                    break;
                }
            }
        }

        return loadFlag;
    }
    
    /**
     * MECO Process 상신 과정에 Validation을 진행 하는 과정에 EPL Load 시간을 확인 하기위해 static으로 생성
     * [NON-SR][20160520] taeku.jeong 
     * @param mecoId
     * @return
     * @throws TCException
     */
    public static Date getLastEPLLoadDate(String mecoId) throws TCException {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("mecoid", mecoId);
        
        CustomMECODao dao = null;
        Date lastLoadDate = null;
        try {
            dao = new CustomMECODao();
            lastLoadDate = dao.getLastEPLLoadDate(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lastLoadDate;
    }

    public Date getLastEPLLoadDate(TCComponentItemRevision revision, String mecoId) throws TCException {
        String[] propertyNames = new String[] {
                SDVPropertyConstant.ITEM_ITEM_ID,
                SDVPropertyConstant.ITEM_REVISION_ID
        };

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("mecoid", mecoId);
        
        // 실제 Query에서는 필요없는 값으로 보임. 
        if(revision!=null){
        	String[] values = revision.getProperties(propertyNames);
        	if(values!=null && values.length>=2){
        		paramMap.put("itemid", values[0]);
        		paramMap.put("itemrev", values[1]);
        	}
        }

        CustomMECODao dao = null;
        Date lastLoadDate = null;
        try {
            dao = new CustomMECODao();
            lastLoadDate = dao.getLastEPLLoadDate(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lastLoadDate;
    }

    /**
     * Header 정보를 가져온다.
     *
     * @method getHeaderInfo
     * @date 2013. 11. 22.
     * @param
     * @return IDataMap
     * @exception
     * @throws
     * @see
     */
    public IDataMap getHeaderInfo(TCComponentBOPLine bopLine) throws Exception {
        IDataMap dataMap = new RawDataMap();

        TCComponentBOPLine line = ProcessSheetUtils.getLine(bopLine);
        TCComponentBOPLine shop = ProcessSheetUtils.getShop();
        if(shop == null) {
            throw new NullPointerException(registry.getString("ShopNull.Message"));
        }
        
        System.out.println("shop = "+shop);
        // [SR170210-014] Y400 도장 작업표준서 차종 표시 오류 수정 의뢰 건에 대한 검토 의견
        //--------------------------------------------------------------------------------------------------
        //SDVTypeConstant.EBOM_MPRODUCT
        // 필요한 경우 Shop Revision 의 Product Code를 검색하고 해당 Id를 가진 Item을 찾으면 Product Item인데
        // S7_ProductRevision의 Property중 Project Code를 찾을 수있다.
        // 현재는 Project Code를 MECO의 정보에서 읽어 온다.

        TCComponentItemRevision itemRevision = bopLine.getItemRevision();
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision) itemRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
        if(mecoRevision == null) {
            throw new NullPointerException(registry.getString("MECONull.Message"));
        }

        String projectCode = mecoRevision.getProperty(SDVPropertyConstant.MECO_REV_PROJECT_CODE);

        dataMap.put(SDVPropertyConstant.SHOP_REV_SHOP_CODE, shop.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE, processType, IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.MECO_REV_PROJECT_CODE, projectCode, IData.STRING_FIELD);

        String variant = bopLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
        if(variant != null && variant.length() > 0) {
            variant = (String) SDVBOPUtilities.getVariant(variant).get("printDescriptions");
        } else {
            variant = registry.getString("ProcessSheetCommonVariant." + configId);
        }
        dataMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, variant, IData.STRING_FIELD);

        dataMap.put(SDVPropertyConstant.LINE_REV_CODE, line.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE), IData.STRING_FIELD);
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 특별 특성 속성값 추가
        	String specialChar = bopLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
        
         if( null == specialChar ) {
        	 specialChar = "";
         }
         
         dataMap.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialChar, IData.STRING_FIELD);
         
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        String vehicleName = "";
        String lineName = "";
        if(configId == 0) {
            vehicleName = shop.getItem().getProperty(SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME);
            lineName = line.getItemRevision().getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME);
            dataMap.put(SDVPropertyConstant.WORKFLOW_SIGNOFF, ProcessSheetUtils.getTeamLeaderSignoff(configId, mecoRevision), IData.STRING_FIELD);
        } else {
            vehicleName = shop.getItem().getProperty(SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME);
            lineName = line.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_ENG_NAME);
            TCComponentItemRevision pubRev = getLastestPublishItemRevision(bopLine);
            if(pubRev != null) {
                dataMap.put(SDVPropertyConstant.WORKFLOW_SIGNOFF, ProcessSheetUtils.getTeamLeaderSignoff(configId, pubRev), IData.STRING_FIELD);
            }
        }
        
        // [NON_SR] [2015-10-16] taeku.jeong Infodba가 Publish 하는경우 작성자이름을Operatoin Item Revsion의 Owner로 설정하도록 수정함
        // [NON_SR] [2015-10-20] taeku.jeong Infodba가 Publish 하는경우 작성일을 Released 날짜로 설정한다.
        String currentUserName = AIFUtility.getCurrentApplication().getSession().getUserName();
       	if(currentUserName!=null && currentUserName.indexOf("infodba")>=0){
    		TCComponentUser itemRevisoinOwningUser = (TCComponentUser)bopLine.getItemRevision().getReferenceProperty("owning_user");
    		dataMap.put(SDVPropertyConstant.ITEM_OWNING_USER, ProcessSheetUtils.getUserName(configId, itemRevisoinOwningUser), IData.STRING_FIELD);
    		Date releasedDate = mecoRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
    		if(releasedDate!=null){
    			dataMap.put(SDVPropertyConstant.ITEM_CREATION_DATE, releasedDate, IData.OBJECT_FIELD);
    		}else{
    			dataMap.put(SDVPropertyConstant.ITEM_CREATION_DATE, getLatestPublishDateToString(bopLine), IData.OBJECT_FIELD);
    		}
    	}else{
    		dataMap.put(SDVPropertyConstant.ITEM_OWNING_USER, getLatestPublishUserName(bopLine), IData.STRING_FIELD);
    		dataMap.put(SDVPropertyConstant.ITEM_CREATION_DATE, getLatestPublishDateToString(bopLine), IData.OBJECT_FIELD);
    	}
        dataMap.put(SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME, vehicleName, IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, lineName, IData.STRING_FIELD);

        return dataMap;
    }

    /**
     * 공법의 마지막 Publish 날짜를 가져온다.
     *
     * @method getLatestPublishDateToString
     * @date 2013. 11. 22.
     * @param
     * @return Date
     * @exception
     * @throws
     * @see
     */
    private Date getLatestPublishDateToString(TCComponentBOPLine bopLine) throws Exception {
        String itemId = bopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String revId = bopLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);

        TCComponentItem item = SDVBOPUtilities.FindItem(registry.getString("ProcessSheetItemIDPrefix.0") + itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);
        TCComponentItemRevision itemRevision = null;
        if(item != null) {
            TCComponent[] revisions = item.getRelatedComponents("revision_list");
            for(int i = revisions.length - 1; i >= 0; i--) {
                if(revisions[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID).startsWith(revId)) {
                    itemRevision = (TCComponentItemRevision) revisions[i];
                    break;
                }
            }
        }

        if(itemRevision != null) {
            return itemRevision.getDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE);
        }

        return null;
    }

    private String getLatestPublishUserName(TCComponentBOPLine bopLine) throws Exception {
        TCComponentItemRevision itemRevision = getLastestPublishItemRevision(bopLine);
        if(itemRevision != null) {
        	
        	TCComponentUser user = null;
        	
        	// [NON_SR] [2015-10-16] taeku.jeong Infodba가 Publish 하는경우 작성자이름을Operatoin Item Revsion의 Owner로 설정하도록 수정함 
        	String currentUserName = itemRevision.getSession().getUserName();
        	if(currentUserName!=null && currentUserName.indexOf("infodba")>=0){
        		user = (TCComponentUser)bopLine.getItemRevision().getReferenceProperty("owning_user");
        	}else{
        		user = (TCComponentUser) itemRevision.getReferenceProperty(SDVPropertyConstant.PS_REV_LAST_PUB_USER);
        	}
        	
            if(user != null) {
                return ProcessSheetUtils.getUserName(configId, user);
            }
        }

        return null;
    }

    private TCComponentItemRevision getLastestPublishItemRevision(TCComponentBOPLine bopLine) throws Exception {
        String itemId = bopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String revId = bopLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);

        TCComponentItem item = SDVBOPUtilities.FindItem(registry.getString("ProcessSheetItemIDPrefix." + configId) + itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);
        TCComponentItemRevision itemRevision = null;
        if(item != null) {
            TCComponent[] revisions = item.getRelatedComponents("revision_list");
            for(int i = revisions.length - 1; i >= 0; i--) {
                if(revisions[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID).startsWith(revId)) {
                    itemRevision = (TCComponentItemRevision) revisions[i];
                    break;
                }
            }
        }

        return itemRevision;
    }

    /**
     * Operation 정보를 가져온다.
     *
     * @method getOperationInfo
     * @date 2013. 11. 22.
     * @param
     * @return IDataMap
     * @exception
     * @throws
     * @see
     */
    public IDataMap getOperationInfo(TCComponentBOPLine bopLine) throws TCException {
        TCComponentItem item = bopLine.getItem();
        TCComponentItemRevision revision = bopLine.getItemRevision();

        String[] properties = revision.getProperties(new String[] {
                SDVPropertyConstant.ITEM_ITEM_ID,
                SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO,
                SDVPropertyConstant.OPERATION_REV_STATION_NO,
                SDVPropertyConstant.OPERATION_REV_STATION_CODE,
                SDVPropertyConstant.OPERATION_REV_KPC,
                SDVPropertyConstant.OPERATION_REV_DR,
                SDVPropertyConstant.OPERATION_REV_LINE,
                SDVPropertyConstant.ITEM_DATE_RELEASED,
                // [Non-SR] 이종화 차장님 요청 
                // 공법 ID 추출시 공법의 Revision ID 또한 추출 하여 작업 표준서 하위에 기재
                SDVPropertyConstant.ITEM_REVISION_ID,
                // 특별특성 속성추가
                SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC
        });
        // [Non-SR] 이종화 차장님 요청 
        // 공법 ID 추출시 공법의 Revision ID 또한 추출 하여 작업 표준서 하위에 기재
        String operationId = properties[0] + "(" + properties[8] + ")" ;
        if(configId == 1) {
            operationId = ProcessSheetUtils.changeToEnglishOperationId(operationId, processType);
        }

        IDataMap dataMap = new RawDataMap();
        dataMap.put(SDVPropertyConstant.ITEM_ITEM_ID, operationId, IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, revision.getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, properties[1], IData.STRING_FIELD);
       
        if("A".equals(processType)) {
            dataMap.put(SDVPropertyConstant.OPERATION_REV_STATION_NO, properties[2], IData.STRING_FIELD);
        } else {
            dataMap.put(SDVPropertyConstant.OPERATION_REV_STATION_CODE, properties[6] + "-" + properties[3], IData.STRING_FIELD);
            dataMap.put(SDVPropertyConstant.OPERATION_REV_KPC, properties[4], IData.STRING_FIELD);
        }
        dataMap.put(SDVPropertyConstant.OPERATION_WORKER_CODE, item.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE), IData.STRING_FIELD);
        // DRB일 경우 DR로 표기
        // [SR] 이종화 차장님 요청 - DR 추출 로직 추가
        // DR 속성 정보 추출 후 Preview 에 입력시 로직 변경
        String dr = getDRProperty(bopLine);
//        if("DRB".equals(dr)) {
//            dr = "DR";
//        } else {
//            //[SR140915-015][20141030]shcho, 조립 DR Type LOV표시에 '(조립)' 문자 추가로 인하여 값 표시방식 변경 (작업표준서에는 '(조립)'문자 제거)
//            //dr = properties[5]; 
//        }
        dataMap.put(SDVPropertyConstant.OPERATION_REV_DR, dr, IData.STRING_FIELD);

        // [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
        // 공법 릴리즈 상태를 체크하기 위하여 데이터 맵에 Release 일자를 담는다.
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date pdate_released = revision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
        dataMap.put(SDVPropertyConstant.ITEM_DATE_RELEASED, pdate_released == null ? null : format.format(pdate_released), IData.STRING_FIELD);
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 특별특성  속성 추가
        dataMap.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, properties[9], IData.STRING_FIELD);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        return dataMap;
    }

    /**
     * 리소스 리스트를 가져온다.
     *
     * @method getResourceList
     * @date 2013. 11. 22.
     * @param
     * @return IDataMap
     * @exception
     * @throws
     * @see
     */
    public IDataMap getResourceList(TCComponentBOPLine bopLine) throws TCException {
    	
        List<HashMap<String, Object>> resourceList = new ArrayList<HashMap<String,Object>>();
        ArrayList<HashMap<String, String>> resourceMecoInfos = null;
        

        if("B".equals(processType) || "P".equals(processType)) {
            TCComponentBOPLine parentLine = (TCComponentBOPLine) bopLine.parent();
            if(parentLine != null && parentLine.getChildrenCount() > 0) {
                resourceList = getResources(resourceList, bopLine);
                resourceMecoInfos = getResourceMecoNo(bopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID),
                        parentLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
            }
        } else {
            resourceList = getChildResources(resourceList, bopLine);
            resourceMecoInfos = getResourceMecoNo(bopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), null);
        }

        // ID 기준으로 중복 제거
        List<HashMap<String, Object>> newResourceList = new ArrayList<HashMap<String,Object>>();
        for(HashMap<String, Object> resourceMap : resourceList) {
            boolean existFlag = false;
            String itemId = (String) resourceMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
            int quantity = Integer.parseInt((String) resourceMap.get(SDVPropertyConstant.BL_QUANTITY));
            for(HashMap<String, Object> newResourceMap : newResourceList) {
                String newItemId = (String) newResourceMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
                if(itemId.equals(newItemId)) {
                    existFlag = true;
                    int newQuantity = Integer.parseInt((String) newResourceMap.get(SDVPropertyConstant.BL_QUANTITY)) + quantity;
                    newResourceMap.put(SDVPropertyConstant.BL_QUANTITY, String.valueOf(newQuantity));
                    break;
                }
            }
            
            if(existFlag == false) {
                newResourceList.add(resourceMap);
            }
        }

        for(int i = 0; i < newResourceList.size(); i++) {
            newResourceList.get(i).put("SEQ", (i + 1) * 10);

            // MECONO setting
            String id = null;
            HashMap<String, Object> resourceMap = newResourceList.get(i);
            String parentType = (String) resourceMap.get("parent_type");
            if(SDVTypeConstant.PLANT_STATION_ITEM.equals(parentType) || SDVTypeConstant.PLANT_OPAREA_ITEM.equals(parentType)) {
                id = (String) resourceMap.get("parent_id");
            } else {
                id = (String) resourceMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
            }

            if(resourceMecoInfos != null) {
                for(HashMap<String, String> mecoInfo : resourceMecoInfos) {
                    if(id.equals(mecoInfo.get("NEW_CHILD_NO"))) {
                        newResourceList.get(i).put("SYMBOL", getMecoSymbol(mecoInfo.get("MECONO")));
                        break;
                    }
                }
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 수정자 : bc.kim
        // 이종화 차장님 요청 
        // 작업표준서 Preview 에서 공법 조회시 Resource 항목에 속성값이 변경( No Revise ) 되었을때 
        // 해당 Resource 의 기호가 MECO 의 기호에 맞게 변경 되는 로직 추가
        // 20210713 seho 변경....
        // 리비전 룰에 따른 리비전을 가져오는걸로 변경함.
        // 무조건 최종 리비전을 가져오면 안되지.
//        TCComponentItem bopItem = bopLine.getItem();
//        TCComponent[] bopItemRevisions = bopItem.getRelatedComponents("revision_list");
//        TCComponentItemRevision latestRevision = bopItem.getLatestItemRevision();

        TCComponentItemRevision opRevision = bopLine.getItemRevision();
        TCComponentItemRevision previousOpRevision = null;
		try
		{
			previousOpRevision = SYMTcUtil.getPreviousRevision(opRevision);
		} catch (Exception e)
		{
		}
        String bopItemId = opRevision.getProperty("item_id");
//        String latestRevisionId = latestRevision.getProperty("item_revision_id");
//        int previousCount = bopItemRevisions.length - 1;

        ArrayList<HashMap<String, String>> currentOpPropertyMapList  = null;
        ArrayList<HashMap<String, String>> previousOpPropertyMapList = null;
        	if(previousOpRevision != null) {
            	currentOpPropertyMapList = getSymbomResourceMecoNo(bopItemId, opRevision.getProperty("item_revision_id"));
        		previousOpPropertyMapList = getSymbomResourceMecoNo(bopItemId, previousOpRevision.getProperty("item_revision_id"));
        		
        		if( null != previousOpPropertyMapList && null != currentOpPropertyMapList ) {
                	for( HashMap<String, String> mapList : currentOpPropertyMapList ) {
                		if( null != mapList.get("PUID") ) {
                			String puid = mapList.get("PUID");
                			for( HashMap<String, String> preMapList : previousOpPropertyMapList ) {
                				if( null != preMapList.get("PUID")) {
                					if(  puid.equals(preMapList.get("PUID"))) {
                						if( null != mapList.get("CHANGE_VALUE") && null != preMapList.get("CHANGE_VALUE")) {
                							if(mapList.get("CHANGE_VALUE").equals(preMapList.get("CHANGE_VALUE"))) {
		                    					continue;
		                    				} else {
		                    						for( HashMap<String, Object> newResource : newResourceList) {
		                    							if( newResource.containsValue(mapList.get("PITEM_ID"))) {
		                    								String mecoNo[] = opRevision.getProperty("m7_MECO_NO").split("/");
		                    								newResource.put("SYMBOL", getMecoSymbol(mecoNo[0]));
		                    							}
		                    						}
		                    					
		                    				}
                						}
                					}
                				}
                			}
                		}
                	}
                }	
        	}
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        IDataMap dataMap = new RawDataMap();
        dataMap.put("ResourceList", newResourceList, IData.TABLE_FIELD);

        return dataMap;
    }

    public List<HashMap<String, Object>> getResources(List<HashMap<String, Object>> list, TCComponentBOPLine bopLine) throws TCException {
        TCComponentBOPLine parentLine = (TCComponentBOPLine) bopLine.parent();
        AIFComponentContext[] contexts = parentLine.getChildren();
        HashMap<String, Object> resourceMap = null;
        for(int i = 0; i < contexts.length; i++) {
            TCComponentBOPLine childLine = (TCComponentBOPLine) contexts[i].getComponent();
            if(ProcessSheetUtils.isEquipment(childLine, processType) || ProcessSheetUtils.isTool(childLine)) {
                resourceMap = getResourceProperties(childLine);
                if(resourceMap != null) {
                    resourceMap.put("TYPE", childLine.getItem().getType());
                    list.add(resourceMap);
                }
            } else if(ProcessSheetUtils.isPlant(childLine)) {
                list = getChildResources(list, childLine);
            } else if(ProcessSheetUtils.isOperation(childLine)) {
                if(childLine.equals(bopLine)) {
                    list = getChildResources(list, childLine);
                }
            }
        }

        return list;
    }

    private HashMap<String, Object> getResourceProperties(TCComponentBOPLine bopLine) throws TCException {
        HashMap<String, Object> propertyMap = new HashMap<String, Object>();

        String[] propertyNames = new String[] {
                SDVPropertyConstant.ITEM_ITEM_ID,
                SDVPropertyConstant.ITEM_REVISION_ID,
                SDVPropertyConstant.ITEM_OBJECT_NAME,
                SDVPropertyConstant.EQUIP_PURPOSE_KOR,
                SDVPropertyConstant.EQUIP_SPEC_KOR,
                SDVPropertyConstant.TOOL_SPEC_KOR,
                SDVPropertyConstant.EQUIP_PURPOSE_ENG,
                SDVPropertyConstant.EQUIP_SPEC_ENG,
                SDVPropertyConstant.TOOL_SPEC_ENG
        };

        TCComponentItemRevision itemRevision = bopLine.getItemRevision();
        if(itemRevision == null) {
            return null;
        }

        String[] revPropertyValues = itemRevision.getProperties(propertyNames);

        propertyNames = new String[] {
                SDVPropertyConstant.EQUIP_ENG_NAME,
                SDVPropertyConstant.TOOL_ENG_NAME
        };
        String[] itemPropertyValues = bopLine.getItem().getProperties(propertyNames);

        propertyNames = new String[] {
                SDVPropertyConstant.BL_QUANTITY,
                SDVPropertyConstant.BL_NOTE_TORQUE_VALUE
        };
        String[] bopPropertyValues = bopLine.getProperties(propertyNames);

        propertyMap.put(SDVPropertyConstant.ITEM_ITEM_ID, revPropertyValues[0]);
        propertyMap.put(SDVPropertyConstant.ITEM_REVISION_ID, revPropertyValues[1]);
        propertyMap.put(SDVPropertyConstant.BL_QUANTITY, bopPropertyValues[0]);

        String torque = bopPropertyValues[1];
        if(torque != null && torque.length() > 0) {
            TCProperty torqueTypeProp = bopLine.getTCProperty(SDVPropertyConstant.BL_NOTE_TORQUE);
            if(torqueTypeProp != null) {
                String torqueType = torqueTypeProp.getStringValue();
                if(!torqueType.equals("NotYet")) {
                    torque = torqueType + " " + torque;
                }
            }

            TCPreferenceService prefService = ((TCSession) AIFUtility.getDefaultSession()).getPreferenceService();
//            String torqueUnit = prefService.getString(TCPreferenceService.TC_preference_site, registry.getString("DefaultTorqueUnitPreference.Name"));
            String torqueUnit = prefService.getStringValueAtLocation(registry.getString("DefaultTorqueUnitPreference.Name"), TCPreferenceLocation.OVERLAY_LOCATION);
            if(torqueUnit != null && torqueUnit.length() > 0) {
                torque += " " + torqueUnit;
            }
        }

        String objectName = "";
        String purpose = "";
        String spec ="";
        if(configId == 0) {
            objectName = revPropertyValues[2];
            if(ProcessSheetUtils.isEquipment(bopLine, processType)) {
                purpose = revPropertyValues[3];
                spec = revPropertyValues[4];
            } else {
                purpose = torque;
                spec = revPropertyValues[5];
            }
        } else {
            if(ProcessSheetUtils.isEquipment(bopLine, processType)) {
                objectName = itemPropertyValues[0];
                purpose = revPropertyValues[6];
                spec = revPropertyValues[7];
            } else {
                objectName = itemPropertyValues[1];
                purpose = torque;
                spec = revPropertyValues[8];
            }
        }
        propertyMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, objectName);
        propertyMap.put(SDVPropertyConstant.EQUIP_PURPOSE_KOR, purpose);
        propertyMap.put(SDVPropertyConstant.EQUIP_SPEC_KOR, spec);

        TCComponentBOPLine parentLine = (TCComponentBOPLine) bopLine.parent();
        if(parentLine != null) {
            propertyNames = new String[] {
                    SDVPropertyConstant.ITEM_ITEM_ID,
                    SDVPropertyConstant.ITEM_REVISION_ID
            };

            String[] values = parentLine.getItemRevision().getProperties(propertyNames);

            propertyMap.put("parent_type", parentLine.getItem().getType());
            propertyMap.put("parent_id", values[0]);
            propertyMap.put("parent_rev", values[1]);
        }

        return propertyMap;
    }

    public List<HashMap<String, Object>> getChildResources(List<HashMap<String, Object>> list, TCComponentBOPLine bopLine) throws TCException {
        if(bopLine.getChildrenCount() > 0) {
            AIFComponentContext[] contexts = bopLine.getChildren();
            HashMap<String, Object> resourceMap = null;
            for(int i = 0; i < contexts.length; i++) {
                TCComponentBOPLine childLine = (TCComponentBOPLine) contexts[i].getComponent();
                if(ProcessSheetUtils.isEquipment(childLine, processType) || ProcessSheetUtils.isTool(childLine)) {
                    resourceMap = getResourceProperties(childLine);
                    if(resourceMap != null) {
                        resourceMap.put("TYPE", childLine.getItem().getType());
                        list.add(resourceMap);
                    }
                } else if(ProcessSheetUtils.isPlant(childLine)) {
                    list = getChildResources(list, childLine);
                }
            }
        }

        return list;
    }

    private ArrayList<HashMap<String, String>> getResourceMecoNo(String operationno, String stationno) {
        CustomMECODao dao = null;
        ArrayList<HashMap<String, String>> mecoEplInfos = null;
        try {
            dao = new CustomMECODao();
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("operationno", operationno);
            if(stationno != null) {
                paramMap.put("stationno", stationno);
            }
            mecoEplInfos = dao.getResourceMECONoForProcessSheet(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mecoEplInfos;
    }
    
    // 수정자 : bc.kim
    // 이종화 차장님 요청 
    // 작업표준서 Preview 에서 공법 조회시 Resource 항목에 속성값이 변경( No Revise ) 되었을때 
    // 해당 Resource 의 기호가 MECO 의 기호에 맞게 변경 되는 로직 추가
    private ArrayList<HashMap<String, String>> getSymbomResourceMecoNo( String operationNo, String revisionId ) {
    	CustomMECODao dao = null;
    	ArrayList<HashMap<String, String>> mecoEplInfos = null;
    	try {
    		dao = new CustomMECODao();
    		HashMap<String, String> paramMap = new HashMap<String, String>();
    		paramMap.put("operationNo", operationNo);
    		paramMap.put("revisionId", revisionId);
    		mecoEplInfos = dao.getSymbomResourceMecoNo(paramMap);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return mecoEplInfos;
    }
    
    
    // 수정자 : bc.kim
    // 이종화 차장님 요청 SR : [SR190131-060]
    // 작업표준서 Preview 에서 공법 조회시 Resource 항목에 속성값이 변경( No Revise ) 되었을때 
    // 해당 Resource 의 기호가 MECO 의 기호에 맞게 변경 되는 로직 추가
    private ArrayList<HashMap<String, String>> getSymbomSubsidiaryMecoNo( String operationNo, String revisionId ) {
    	CustomMECODao dao = null;
    	ArrayList<HashMap<String, String>> mecoEplInfos = null;
    	try {
    		dao = new CustomMECODao();
    		HashMap<String, String> paramMap = new HashMap<String, String>();
    		paramMap.put("operationNo", operationNo);
    		paramMap.put("revisionId", revisionId);
    		mecoEplInfos = dao.getSymbomSubsidiaryMecoNo(paramMap);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return mecoEplInfos;
    }

    /**
     * MECO 리스트를 가져온다.
     *
     * @method getMECOList
     * @date 2013. 11. 22.
     * @param
     * @return IDataMap
     * @exception
     * @throws
     * @see
     */
    public IDataMap getMECOList(TCComponentBOPLine bopLine) throws Exception {
        String operationRev = bopLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);

        TCComponentItem item = bopLine.getItem();
        TCComponent[] revisions = item.getRelatedComponents("revision_list");
        mecoList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> mecoMap = null;

        int pbiIndex = 0;
        int mecIndex = 0;
        for(int i = 0; i < revisions.length; i++) {
            String rev = revisions[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            if(operationRev.compareTo(rev) < 0) {
                break;
            }

            TCComponentItemRevision mecoRevision = (TCComponentItemRevision) revisions[i].getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
            if(mecoRevision != null) {
                String[] propertyNames = new String[] {
                        SDVPropertyConstant.ITEM_ITEM_ID,
                        SDVPropertyConstant.ITEM_OBJECT_DESC,
                        SDVPropertyConstant.MECO_TYPE
                };

                String[] values = mecoRevision.getProperties(propertyNames);

                mecoMap = new HashMap<String, Object>();
                if("PBI".equals(values[2])) {
                    mecoMap.put("SYMBOL", (char) (48 + pbiIndex));
                    pbiIndex++;
                } else {
                    mecoMap.put("SYMBOL", (char) (97 + mecIndex));
                    mecIndex++;
                }
                mecoMap.put(SDVPropertyConstant.ITEM_ITEM_ID, values[0]);
                mecoMap.put(SDVPropertyConstant.ITEM_OBJECT_DESC, values[1]);
                mecoMap.put(SDVPropertyConstant.ITEM_DATE_RELEASED, mecoRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED));
                mecoMap.put(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE, mecoRevision.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE));
                TCComponentUser owningUser = (TCComponentUser) mecoRevision.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
                mecoMap.put(SDVPropertyConstant.ITEM_OWNING_USER, ProcessSheetUtils.getUserName(configId, owningUser));
                mecoMap.put("APPR", ProcessSheetUtils.getTeamLeaderSignoff(configId, mecoRevision));
            }
            mecoList.add(mecoMap);
        }

        IDataMap dataMap = new RawDataMap();
        dataMap.put("MECOList", mecoList, IData.TABLE_FIELD);

        return dataMap;
    }

    private char getMecoSymbol(String mecoNo) {
        if(mecoList != null) {
            for(HashMap<String, Object> mecoMap : mecoList) {
                if(mecoNo.equals(mecoMap.get(SDVPropertyConstant.ITEM_ITEM_ID))) {
                    return (Character) mecoMap.get("SYMBOL");
                }
            }
        }

        return '0';
    }

    /**
     * Activity 리스트를 가져온다.
     *
     * @method getActivityList
     * @date 2013. 11. 22.
     * @param
     * @return IDataMap
     * @exception
     * @throws
     * @see
     */
    public IDataMap getActivityList(TCComponentBOPLine bopLine) throws TCException {
        TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) bopLine;
        TCComponent root = bvrOperation.getReferenceProperty("bl_me_activity_lines");
        List<HashMap<String, Object>> activityList = new ArrayList<HashMap<String, Object>>();

        if(root != null) {
            if(root instanceof TCComponentCfgActivityLine) {
                TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                boolean releaseFlag = false;
                Date releasedDate = rootActivity.getDateProperty(SDVPropertyConstant.ACTIVITY_DATE_RELEASED);
                if(releasedDate != null) {
                    releaseFlag = true;
                }
                TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
//                HashMap<String, Object> activityMap = null;
                if(children != null) {
                    for(int i = 0; i < children.length; i++) {
                        if(children[i] instanceof TCComponentMEActivity) {
                            TCComponentMEActivity child = (TCComponentMEActivity) children[i];
                            child.refresh();
                            String[] propertyNames = new String[] {
                                    SDVPropertyConstant.ITEM_OBJECT_NAME,
                                    SDVPropertyConstant.ACTIVITY_ENG_NAME,
                                    SDVPropertyConstant.ACTIVITY_SYSTEM_CODE,
                                    SDVPropertyConstant.ACTIVITY_CONTROL_POINT,
                                    SDVPropertyConstant.ACTIVITY_CONTROL_BASIS,
                                    SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE
                            };

                            String[] values = child.getProperties(propertyNames);

                            HashMap<String, Object> activityMap = new HashMap<String, Object>();
                            activityMap.put("SEQ", (i + 1) * 10);

                            String activityName = values[0];
                            if(configId == 1) {
                                String engActivityName = values[1];
                                if(engActivityName != null && engActivityName.length() > 0) {
                                    activityName = engActivityName;
                                }
                            }

                            activityMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, activityName);
                            activityMap.put(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE, values[2]);
                            activityMap.put(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY, child.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).getStringValue());
                            //[SR없음][20150224]shcho, 작업표준서 표기시 getDoubleProperty 를 이용하여 가져올 경우 소수점 뒤에 불필요한 00000000001 과 같은 값이 표기 되는 오류 보정 (String으로 가져와 Double로 변환)
                            activityMap.put(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY, Double.parseDouble(child.getProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY)));
                            activityMap.put(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME, child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME));

                            TCComponentBOMLine[] tools = child.getReferenceTools(bopLine);
                            if(tools != null && tools.length > 0) {
                                activityMap.put(SDVPropertyConstant.ACTIVITY_TOOL_LIST, tools);
                            }

                            activityMap.put(SDVPropertyConstant.ACTIVITY_CONTROL_POINT, values[3]);
                            activityMap.put(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS, values[4]);
                            activityMap.put(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE, values[5]);

                            // MECONO setting
                            String activityMecoId = getActivityMECOId(bopLine.getItemRevision(), child, i);
                            activityMap.put("SYMBOL", getMecoSymbol(activityMecoId));
                            if(!releaseFlag) {
                                child.setProperty(SDVPropertyConstant.ACTIVITY_MECO_NO, activityMecoId);
                            }

                            activityList.add(activityMap);
                        }
                    }
                }
            }
        }

        IDataMap dataMap = new RawDataMap();
        dataMap.put("ActivityList", activityList, IData.TABLE_FIELD);
		/////////////////////////////////////////////////////////////////////////////////////////
		// 작업표준서 특별 특성 보완 으로 인한 BomLine 추가
        dataMap.put("BOMLINE_OBJECT", bopLine, IData.OBJECT_FIELD);
		/////////////////////////////////////////////////////////////////////////////////////////

        return dataMap;
    }

    /**
     * End Item 리스트를 가져온다.
     *
     * [SR140828-014][20140827] shcho, BOPLine의 Option 정보를 가져오는 속성을 BL_OCC_MVL_CONDITION 에서 BL_VARIANT_CONDITION으로 변경
     *             (이유 : Copy&Paste로 할당된 End Item의 경우 BL_OCC_MVL_CONDITION에 값이 없다.)
     * [SR150105-031][20150401] shcho, 작업표준서 원자재(E/Item) 표시 Logic 변경 ( E/Item Part No., 설계에서 부여한 Option, 생기(공기)에서 부여한 Find No.가 동일하면 Pack하여 작업표준서에 표기)
     * [SR150612-003][20150612] shcho, Find No. 재부여 (1부터 건너뛰는 번호 없이 순차적으로 증가하도록 부여)
     *                                                Pack 작업시 사전에 Find No.를 건너뛰는 번호 없이 순차적으로 부여해주던 것을
     *                                                Pack 작업 이후에 Find No.를 순차적으로 재부여토록 수정함.
     * [NON-SR][20150706] shcho,  Find No. 단순히 순차적으로 부여하는것이 아니라, 건너뛰는 경우에만 번호를 당겨 순차적으로 하도록 변경. 
     *                                       (예 : 만약 1이라는 순번이 2개 인 경우, 2개 모두 순번 1을 그대로 유지 해야 함.)         
     * [NON-SR][20150706] taeku.jeong 작업기준서 EndItem List되는 부분에 MECO_EPL Data를 읽어 변경 기호를 표기해주는 부분 누락 되는 문제
     *                                                해당 부분의 문제의 원인은  MECO_EPL의 NEW_CHILD_TYPE Column의 값에 의도 되지 않은 값이 Teamcenter Upgrade 이후 발생됨
     *                                                'Vehicle Part', 'Standard Part'의 DB상의 실제 Type 이름인 'S7_Vehpart', 'S7_Stdpart' 라는 값이 추가 되어 있었음.
     *                                                해당 부분의 Query를 위한 SQL 문장에 Type 조건을 추가 처리하고 관련된 오류가 있을것으로 유추되는 부분도 같이 수정함.
     * 
     * @method getEndItemList
     * @date 2013. 11. 22.
     * @param
     * @return IDataMap
     * @throws Exception 
     * @exception
     * @throws
     * @see
     */
    public IDataMap getEndItemList(TCComponentBOPLine bopLine) throws Exception {
        List<HashMap<String, Object>> endItemList = new ArrayList<HashMap<String, Object>>();
        if(bopLine.getChildrenCount() > 0) {
        	// 2015-10.02 taeku.jeong End Item MECO List를 읽어와서 변경기호를 표기해주는 부분의 Query를 수정함.
        	// 이종화 차장님의 확인 요청으로 인해 문제점을 파악후 Query를 수정함.
            ArrayList<HashMap<String, String>> endItemMecoInfos = getEndItemMecoNo(bopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
            TCComponentBOMLine[] childBOMLineList = SDVBOPUtilities.getUnpackChildrenBOMLine(bopLine);
          
            String sequenceNo = "";
            int index = 0;
            for(int i = 0; i < childBOMLineList.length; i++) {
                TCComponentBOPLine component = (TCComponentBOPLine) childBOMLineList[i];
                component.refresh();
                
                if(ProcessSheetUtils.isEndItem(component)) {
                    TCComponentItemRevision itemRevision = component.getItemRevision();
                    if(itemRevision == null) {
                        continue;
                    }

                    String[] propertyNames = new String[] {
                            SDVPropertyConstant.ITEM_ITEM_ID,
                            SDVPropertyConstant.ITEM_REVISION_ID,
                            SDVPropertyConstant.ITEM_OBJECT_NAME,
                            SDVPropertyConstant.S7_KOR_NAME 
                    };

                    String[] itemPropValues = itemRevision.getProperties(propertyNames);

                    HashMap<String, Object> endItemMap = new HashMap<String, Object>();

                    endItemMap.put(SDVPropertyConstant.BL_ITEM_ID, itemPropValues[0]);
                    endItemMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, itemPropValues[1]);

                    if(configId == 0) {
                        endItemMap.put(SDVPropertyConstant.BL_OBJECT_NAME, itemPropValues[3]);
                    } else {
                        endItemMap.put(SDVPropertyConstant.BL_OBJECT_NAME, itemPropValues[2]);
                    }

                    propertyNames = new String[] {
                          SDVPropertyConstant.BL_QUANTITY,
                          SDVPropertyConstant.BL_UNIT_OF_MEASURES,
//                          SDVPropertyConstant.BL_VARIANT_CONDITION,
                          SDVPropertyConstant.BL_OCC_MVL_CONDITION,
                          SDVPropertyConstant.BL_SEQUENCE_NO
                    };
                    String[] blPropValues = component.getProperties(propertyNames);

                    /* [SR150612-003][20150612] shcho, Find No. 재부여 (1부터 건너뛰는 번호 없이 순차적으로 증가하도록 부여)
                     *                                                Pack 작업시 사전에 Find No.를 건너뛰는 번호 없이 순차적으로 부여해주던 것을
                     *                                                Pack 작업 이후에 Find No.를 순차적으로 재부여토록 수정함.
                    if(sequenceNo.equals(blPropValues[3])) {
                        endItemMap.put("SEQ", index);
                    } else {
                        endItemMap.put("SEQ", ++index);
                        sequenceNo = blPropValues[3];
                    }
                    */

                    endItemMap.put(SDVPropertyConstant.BL_QUANTITY, blPropValues[0]);
                    endItemMap.put(SDVPropertyConstant.BL_UNIT_OF_MEASURES, blPropValues[1]);
                    endItemMap.put("SEQ", blPropValues[3]);

                    String variant = blPropValues[2];
                    if(variant != null && variant.length() > 0) {
                        variant = (String) SDVBOPUtilities.getVariant(variant).get("printDescriptions");
                    } else {
                    	variant = component.getProperty( SDVPropertyConstant.BL_VARIANT_CONDITION);
                    	if(variant != null && variant.length() > 0) { 
                    		variant = (String) SDVBOPUtilities.getVariant(variant).get("printDescriptions");
                    	} else {
                    		variant = registry.getString("ProcessSheetCommonVariant." + configId);
                    	}
                    }
                    endItemMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, variant);
                    
                    // MECONO setting
                    if(endItemMecoInfos != null) {
                        for(HashMap<String, String> mecoInfo : endItemMecoInfos) {
                        		if(itemPropValues[0].equals(mecoInfo.get("NEW_CHILD_NO"))) {
                        			if( getMecoSymbol(mecoInfo.get("MECONO")) != 0) {
                        				endItemMap.put("SYMBOL", getMecoSymbol(mecoInfo.get("MECONO")));
                        				break;
                        				
                        		 }
                        	}
                        }
                    }
                    
                    ////////////////////////////////////////////////////////////////////////////////////////////////
                    /** 
                     * 이종화 차장님 요청 
                     * End Item 리스트중 E-BOM 에서 Replace 된 파트가 포함이 된 경우 
                     * Replace 여부를 판단 하여 맞을 경우 최신MECO Symbol 을 붙인다.
                     */
//                    boolean hasReplaceEndItem = false;
//                    Map<String, TCComponentItemRevision> endItemRevs = null;
//                   if ( null == endItemMap.get("SYMBOL") || "".equals(endItemMap.get("SYMBOL"))) {
//                	   TCComponentBOPLine topBopLine = (TCComponentBOPLine) bopLine.window().getTopBOMLine();
//                	   // [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
//                	   TCComponent mProductRevision = SDVBOPUtilities.getConnectedMProductItemRevision(topBopLine.getItemRevision());
//                	   String revId = ((TCComponentBOPLine) bopLine).getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
//                	   List<EndItemData> targetEndItems = new ArrayList<EndItemData>();
//                	   // 자동 Replace 된 아이템을 DB 쿼리하여 가져온다.
//                	   CustomBOPDao dao = new CustomBOPDao();  
//                	   ArrayList<EndItemData> replaceEndItems = dao.findReplacedEndItems(bopLine.getProperty(SDVPropertyConstant.BL_ITEM_PUID),
//                			   															mProductRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
//                	   // 공법 하위에 붙은 EndItem 과 Replace 아이템들과 비교 하여 같은 아이템이 있는지 검사
//                	   if( replaceEndItems != null) {
//                		   for( EndItemData endItem : replaceEndItems ) {
//                			   //Replace 가 된 EndItem 인 경우
////                		   if(itemPropValues[0].equals(endItem.getEbom_item_id())) {
////                			   hasReplaceEndItem = true;
////                			   break;
////                		   }
//                			   
//                			   if(revId.equals(endItem.getPitem_revision_id())) {
//                				   targetEndItems.add(endItem);
//                			   }
//                		   }
//                	   }
//                	   
//                	   
//	                	   if(targetEndItems.size() > 0) {
//	                		   endItemRevs = getReplacedEndItems((TCComponentBOPLine) bopLine, targetEndItems);
//	                		   
//	                		   if( endItemRevs.size() > 0 ) {
//	                			   hasReplaceEndItem = true;
//	                		   } 
//	                	   }
//                   }
//                  if(hasReplaceEndItem ) {
                    
                  if ( null == endItemMap.get("SYMBOL") || "".equals(endItemMap.get("SYMBOL"))) {
	    			   ArrayList maxChar = new ArrayList();
	    			   for( HashMap<String, Object> meco : mecoList) {
	    				   maxChar.add((Character) meco.get("SYMBOL"));
	    			   }
	    			   // 있다면 MECO 리스트중 가장 최근의 Symbol 을 들고와서 입력
	    			   Collections.sort(maxChar);
	    			   if( maxChar.size() > 0) {
	    				   endItemMap.put("SYMBOL", maxChar.get(maxChar.size() - 1));
	    			   } else  {
	    				   endItemMap.put("SYMBOL", "0");
	    			   }
    			   
                  }
                   ////////////////////////////////////////////////////////////////////////////////////////////////
                    boolean packFlag = false;
                    for(HashMap<String, Object> map : endItemList) {
                        //[SR150105-031][20150401] shcho, 작업표준서 원자재(E/Item) 표시 Logic 변경 ( E/Item Part No., 설계에서 부여한 Option, 생기(공기)에서 부여한 Find No.가 동일하면 Pack하여 작업표준서에 표기)
                        if(map.get(SDVPropertyConstant.BL_ITEM_ID).toString().equals(itemPropValues[0])
                           && map.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION).toString().equals(variant)
                           && map.get("SEQ").toString().equals(blPropValues[3]))
                        {
                            DecimalFormat format = new DecimalFormat("0");
                            String oldQuantity = map.get(SDVPropertyConstant.BL_QUANTITY).toString();
                            String newQuantity = (format.format(Double.parseDouble(oldQuantity) + 1));
                            
                            map.put(SDVPropertyConstant.BL_QUANTITY, newQuantity);
                            
                            // MECO 변경기호는 가장 큰 값으로 표기한다.
                            // [SR150414-017][20150413] shcho, MECO SYMBOL이 Null(신규 변경 등록) 인 경우에 대하여 Null 처리 추가
                            Object symbol = map.get("SYMBOL");
                            if(symbol != null) {
                                int compareResult = endItemMap.get("SYMBOL").toString().compareTo(symbol.toString());
                                if(compareResult > 0) {
                                    map.put("SYMBOL", endItemMap.get("SYMBOL").toString());
                                }
                            }
                            
                            packFlag = true;
                            break;
                        }
                    }

                    if(!packFlag) {
                        endItemList.add(endItemMap);
                    }
                    
                }
            }
            
            /* [SR150612-003][20150612] shcho, Find No. 재부여 (1부터 건너뛰는 번호 없이 순차적으로 증가하도록 부여)
             *                                                  Pack 작업시 사전에 Find No.를 건너뛰는 번호 없이 순차적으로 부여해주던 것을
             *                                                  Pack 작업 이후에 Find No.를 순차적으로 재부여토록 수정함.
             * [NON-SR][20150706] shcho, Find No. 단순히 순차적으로 부여하는것이 아니라, 건너뛰는 경우에만 번호를 당겨 순차적으로 하도록 변경. 
             *                                       (예 : 만약 1이라는 순번이 2개 인 경우, 2개 모두 순번 1을 그대로 유지 해야 함.)         
             * 
             */
            
            for(HashMap<String, Object> endItemMap : endItemList) {
                String oldSeqNo = (String) endItemMap.get("SEQ");
                if(sequenceNo.equals(oldSeqNo)) {
                    endItemMap.put("SEQ", index);
                } else {
                    endItemMap.put("SEQ", ++index);
                    sequenceNo = oldSeqNo;
                }
            }
        }
        
        IDataMap dataMap = new RawDataMap();
        dataMap.put("EndItemList", endItemList, IData.TABLE_FIELD);

        return dataMap;
    }

    private ArrayList<HashMap<String, String>> getEndItemMecoNo(String operationno) {
        CustomMECODao dao = null;
        ArrayList<HashMap<String, String>> mecoEplInfos = null;
        try {
            dao = new CustomMECODao();
            mecoEplInfos = dao.getEndItemMECONoForProcessSheet(operationno);
        } catch (Exception e) {
        	System.out.println("Exception : "+e.toString());
            e.printStackTrace();
        }
        
//        for (int i = 0;mecoEplInfos!=null && i < mecoEplInfos.size(); i++) {
//        	HashMap resultHash = (HashMap)mecoEplInfos.get(i);
//		}

        return mecoEplInfos;
    }

    /**
     * 부자재 리스트를 가져온다.
     *
     * @method getSubsidiaryList
     * @date 2013. 11. 22.
     * @param
     * @return IDataMap
     * @exception
     * @throws
     * @see
     */
    public IDataMap getSubsidiaryList(TCComponentBOPLine bopLine) throws TCException {
        List<HashMap<String, Object>> subsidiaryList = new ArrayList<HashMap<String, Object>>();
        if(bopLine.getChildrenCount() > 0) {
            ArrayList<HashMap<String, String>> subsidiaryMecoInfos = getSubsidiaryMecoNo(bopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
            AIFComponentContext[] contexts = bopLine.getChildren();
            int index = 97;
            for(int i = 0; i < contexts.length; i++) {
                TCComponentBOPLine component = (TCComponentBOPLine) contexts[i].getComponent();
                HashMap<String, Object> subsidiaryMap = null;
                if(ProcessSheetUtils.isSubsidiary(component)) {
                    String[] propertyNames = new String[] {
                            SDVPropertyConstant.ITEM_ITEM_ID,
                            SDVPropertyConstant.ITEM_REVISION_ID,
                            SDVPropertyConstant.ITEM_OBJECT_NAME,
                            SDVPropertyConstant.SUBSIDIARY_SPEC_KOR,
                            SDVPropertyConstant.SUBSIDIARY_SPEC_ENG,
                            SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT
                    };

                    TCComponentItemRevision itemRevision = component.getItemRevision();
                    if(itemRevision == null) {
                        continue;
                    }

                    String[] revPropertyValues = component.getItemRevision().getProperties(propertyNames);

                    propertyNames = new String[] {
                            SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY,
                            SDVPropertyConstant.BL_NOTE_DAYORNIGHT,
                            SDVPropertyConstant.BL_OCC_MVL_CONDITION
                    };

                    String[] bopPropertyValues = component.getProperties(propertyNames);

                    subsidiaryMap = new HashMap<String, Object>();
                    subsidiaryMap.put("SEQ", (char) index++);
                    subsidiaryMap.put(SDVPropertyConstant.ITEM_ITEM_ID, revPropertyValues[0]);
                    subsidiaryMap.put(SDVPropertyConstant.ITEM_REVISION_ID, revPropertyValues[1]);

                    String objectName = "";
                    String spec = "";
                    if(configId == 0) {
                        objectName = revPropertyValues[2];
                        spec = revPropertyValues[3];
                    } else {
                        objectName = component.getItem().getProperty(SDVPropertyConstant.SUBSIDIARY_ENG_NAME);
                        spec = revPropertyValues[4];
                    }

                    subsidiaryMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, objectName);
                    subsidiaryMap.put(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR, spec);
                    subsidiaryMap.put(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY, bopPropertyValues[0]);
                    subsidiaryMap.put(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT, revPropertyValues[5]);
                    subsidiaryMap.put(SDVPropertyConstant.BL_NOTE_DAYORNIGHT, bopPropertyValues[1]);

                    String variant = bopPropertyValues[2];
                    if(variant != null && variant.length() > 0) {
                        variant = (String) SDVBOPUtilities.getVariant(variant).get("printDescriptions");
                    } else {
                        variant = bopLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
                        if(variant != null && variant.length() > 0) {
                            variant = (String) SDVBOPUtilities.getVariant(variant).get("printDescriptions");
                        } else {
                            variant = registry.getString("ProcessSheetCommonVariant." + configId);
                        }
                    }
                    subsidiaryMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, variant);

                    // MECONO setting
                    if(subsidiaryMecoInfos != null) {
                        for(HashMap<String, String> mecoInfo : subsidiaryMecoInfos) {
                            if(revPropertyValues[0].equals(mecoInfo.get("NEW_CHILD_NO"))) {
                                subsidiaryMap.put("SYMBOL", getMecoSymbol(mecoInfo.get("MECONO")));
                                break;
                            }
                        }
                    }

                    subsidiaryList.add(subsidiaryMap);
                }
            }
        }
        
        
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 수정자 : bc.kim
        // 이종화 차장님 요청 SR : [SR190131-060]
        // 작업표준서 Preview 에서 공법 조회시 Subsidiary 항목에 속성값이 변경( No Revise ) 되었을때 
        // 해당 Resource 의 기호가 MECO 의 기호에 맞게 변경 되는 로직 추가
        
        
        TCComponentItem bopItem = bopLine.getItem();
        TCComponent[] bopItemRevisions = bopItem.getRelatedComponents("revision_list");
        TCComponentItemRevision latestRevision = bopItem.getLatestItemRevision();
        
        String bopItemId = bopItem.getProperty("item_id");
        String latestRevisionId = latestRevision.getProperty("item_revision_id");
        int previousCount = bopItemRevisions.length - 1;
        
        ArrayList<HashMap<String, String>> currentOpPropertyMapList  = null;
        ArrayList<HashMap<String, String>> previousOpPropertyMapList = null;
        	if(previousCount > 0) {
            	currentOpPropertyMapList = getSymbomSubsidiaryMecoNo(bopItemId, bopItemRevisions[previousCount].getProperty("item_revision_id"));
        		previousOpPropertyMapList = getSymbomSubsidiaryMecoNo(bopItemId, bopItemRevisions[previousCount - 1].getProperty("item_revision_id"));
        		
        		if( null != previousOpPropertyMapList && null != currentOpPropertyMapList ) {
                	for( HashMap<String, String> mapList : currentOpPropertyMapList ) {
                		if( null != mapList.get("PUID") ) {
                			String puid = mapList.get("PUID");
                			for( HashMap<String, String> preMapList : previousOpPropertyMapList ) {
                				if( null != preMapList.get("PUID")) {
                					if(  puid.equals(preMapList.get("PUID"))) {
                						if( null != mapList.get("CHANGE_VALUE") && null != preMapList.get("CHANGE_VALUE")) {
                							if(mapList.get("CHANGE_VALUE").equals(preMapList.get("CHANGE_VALUE"))) {
		                    					continue;
		                    				} else {
		                    						for( HashMap<String, Object> newResource : subsidiaryList) {
		                    							if( newResource.containsValue(mapList.get("PITEM_ID"))) {
		                    								String mecoNo[] = bopItemRevisions[previousCount].getProperty("m7_MECO_NO").split("/");
		                    								newResource.put("SYMBOL", getMecoSymbol(mecoNo[0]));
		                    							}
		                    						}
		                    					
		                    				}
                						}
                					}
                				}
                			}
                		}
                	}
                }	
        	}
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
		
        

        IDataMap dataMap = new RawDataMap();
        dataMap.put("SubsidiaryList", subsidiaryList, IData.TABLE_FIELD);

        return dataMap;
    }

    private ArrayList<HashMap<String, String>> getSubsidiaryMecoNo(String operationno) {
        CustomMECODao dao = null;
        ArrayList<HashMap<String, String>> mecoEplInfos = null;
        try {
            dao = new CustomMECODao();
            mecoEplInfos = dao.getSubsidiaryMECONoForProcessSheet(operationno);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mecoEplInfos;
    }

    /**
     * 공법에 붙어 있는 이미지 템플릿을 가져온다.
     *
     * @method exportImageTemplate
     * @date 2013. 11. 22.
     * @param
     * @return File
     * @exception
     * @throws
     * @see
     */
    public File exportImageTemplate(TCComponentBOPLine bopLine) throws TCException {
        File imageTemplate = null;
        TCComponentItemRevision revision = bopLine.getItemRevision();
        TCComponent[] comps = revision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
        if(comps != null) {
            for(TCComponent comp : comps) {
                if(comp instanceof TCComponentDataset) {
                    TCComponentTcFile[] files = ((TCComponentDataset) comp).getTcFiles();
                    if(files != null && files.length > 0) {
                        imageTemplate = files[0].getFile(null);
                        break;
                    }
                }
            }
        }

        return imageTemplate;
    }

    public String getActivityMECOId(TCComponentItemRevision itemRevision, TCComponentMEActivity activity, int sequence) throws TCException {
        TCComponent mecoRevision = itemRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
        String mecoId = mecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

        String activityMecoId = activity.getProperty(SDVPropertyConstant.ACTIVITY_MECO_NO);
        if(activityMecoId != null && activityMecoId.length() > 0 && !mecoId.equals(activityMecoId)) {
            TCComponentItem item = itemRevision.getItem();
            TCComponent[] revisions = item.getRelatedComponents("revision_list");
            for(TCComponent revision : revisions) {
                TCComponent targetMeco = revision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
                String targetMecoId = targetMeco.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                if(activityMecoId.equals(targetMecoId)) {
                    TCComponent root = revision.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
                    TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                    TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                  
                    //[SR141118-031][20141118]shcho, Revise된 후에 Activity 추가시, 변경기호 생성하는 부분에서 이전 Revision의 Activity와 비교하면서 배열의 크기가 서로 다름으로 인하여 발생하는 오류 수정
                    if(children != null && children.length > 0 && children.length > sequence ) {
                        TCComponentMEActivity targetActivity = (TCComponentMEActivity) children[sequence];
                        if(activityCompare(targetActivity, activity)) {
                            mecoId = targetMecoId;
                        }
                    }
                    break;
                }
            }
        }

        return mecoId;
    }

    private boolean activityCompare(TCComponentMEActivity targetActivity, TCComponentMEActivity activity) throws TCException {
        TCPreferenceService prefService = ((TCSession) AIFUtility.getDefaultSession()).getPreferenceService();
//        String[] propertyNames = prefService.getStringArray(TCPreferenceService.TC_preference_site, registry.getString("CompareActivityPropertiesPreference.Name"));
        String[] propertyNames = prefService.getStringValuesAtLocation(registry.getString("CompareActivityPropertiesPreference.Name"), TCPreferenceLocation.OVERLAY_LOCATION);
        if(propertyNames != null) {
            String[] activityValues = activity.getProperties(propertyNames);
            String[] targetActivityValues = targetActivity.getProperties(propertyNames);
            for(int i = 0; i < activityValues.length; i++) {
                if(activityValues[i].compareTo(targetActivityValues[i]) != 0) {
                    return false;
                }
            }
        }

        return true;
    }
    
    /**
     * Weld Operation을 구성하는 용접점 Type을 구분하는 함수
     * 이 함수는 Weld Operation의 용접 조건표를 구성하기위해 만듦.
     * Weld Operation의 용접조건표 양식이 2가지로 나눠지는데 주어진 Weld Operation Revision에
     * 붙어 있는 용접점 들이 Spot 으로 구성된 경우와 Co2, Brazing, Plug로 구성된 경우로 나눈다.
     * Co2 계열 용접점과 Spot 용접점이 섞여 있는 경우는 할당 오류에 해당되면 null을 Return 한다.
     * 
     * @param weldOperationRevision
     * @return
     */
    public static String getWeldConditionSheetType(TCComponentItemRevision weldOperationRevision) {
    	
    	String weldOperationSheeType = null;
    	
    	if(weldOperationRevision==null){
    		return weldOperationSheeType;
    	}
    	
    	String weldOperationItemId = null;
    	String weldOperationItemRevisionId = null;
    	
    	try {
			weldOperationItemId = weldOperationRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			weldOperationItemRevisionId = weldOperationRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
		} catch (TCException e) {
			e.printStackTrace();
		}
    	
    	if(weldOperationItemId!=null && weldOperationItemRevisionId!=null){
    		weldOperationSheeType = ProcessSheetDataHelper.getWeldConditionSheetType(weldOperationItemId, weldOperationItemRevisionId);
    	}
    	
    	return weldOperationSheeType;
    }
    
    /**
     * Weld Operation을 구성하는 용접점 Type을 구분하는 함수
     * 이 함수는 Weld Operation의 용접 조건표를 구성하기위해 만듦.
     * Weld Operation의 용접조건표 양식이 2가지로 나눠지는데 주어진 Weld Operation Revision에
     * 붙어 있는 용접점 들이 Spot 으로 구성된 경우와 Co2, Brazing, Plug로 구성된 경우로 나눈다.
     * Co2 계열 용접점과 Spot 용접점이 섞여 있는 경우는 할당 오류에 해당되면 null을 Return 한다.
     *  
     * @param weldOperationId Weld Operation Item Id
     * @param weldOperationRevId Weld Operation Item Revision Id
     * @return 용접조건표에 적용될 용접조건표 Sheet Type
     */
    public static String getWeldConditionSheetType(String weldOperationId, String weldOperationRevId) {

	    DataSet ds = new DataSet();
	    ds.put("weld_op_id", weldOperationId );
	    ds.put("weld_op_rev_id", weldOperationRevId );

	    ArrayList<HashMap<String, Object>> resultList = null;
	    
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		try {
			resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(
					"com.ssangyong.service.WeldPoint2ndService", 
					"getChildNodeWeldTypeList", 
					ds);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int spotWeldCount = 0;
		int co2WeldCount = 0;
		int barzingCount = 0;
		int plugWeldCount = 0;
		
		for (int i = 0;resultList!=null && i < resultList.size(); i++) {
			HashMap<String, Object> resultRowHash = resultList.get(i);
			String weldType = (String)resultRowHash.get("WELD_TYPE");
			Object weldTypeCount = resultRowHash.get("WELD_TYPE_COUNT");
			
			System.out.println("weldType["+i+"] = "+weldType);
			System.out.println("weldTypeCount["+i+"] = "+weldTypeCount);
			
			if(weldTypeCount!=null && weldTypeCount instanceof BigDecimal){
				int count = ((BigDecimal)weldTypeCount).intValue();
				
				if(weldType!=null && weldType.trim().equalsIgnoreCase("SPOT")){
					spotWeldCount = count;
				}else if(weldType!=null && weldType.trim().equalsIgnoreCase("CO2")){
					co2WeldCount = count;
				}else if(weldType!=null && weldType.trim().equalsIgnoreCase("BRAZING")){
					barzingCount = count;
				}else if(weldType!=null && weldType.trim().equalsIgnoreCase("PLUG")){
					plugWeldCount = count;
				}
			}
		}
		
		int co2GroupCount = co2WeldCount+barzingCount+plugWeldCount;
		
		String weldOperationSheeType = null;
		if(spotWeldCount>0 && co2GroupCount>0 ){
			// Spot과 Co2 계열 용접이 함께 있는 Weld Operation은 잘못 구성된 Weld Operation임
			weldOperationSheeType = null;
		}else if(spotWeldCount>0 && co2GroupCount < 1 ){
			weldOperationSheeType = "SPOT_TYPE";
		}else if(spotWeldCount<1 && co2GroupCount>0 ){
			weldOperationSheeType = "CO2_TYPE";
		}
		
		System.out.println("weldOperationSheeType = "+weldOperationSheeType);
		
        return weldOperationSheeType;
    }
    
    
    
    /**
     * 이종화 차장님 요청 DR 속성값 추출 로직 
     * DR값 우선 순위 ( DR1 > DR2 > DR3 )
     * @param operation
     * @param dataMap
     * @return
     * @throws TCException
     */
    public String getDRProperty(TCComponentBOPLine operation ) throws TCException {
        // End Item DR 속성 우선( DR1 > DR2 > DR3 )
    	String drCompare = "";
    	AIFComponentContext[] context = operation.getChildren();
        if (context.length > 0) {
//            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type)) {
                        String dr = childLine.getItemRevision().getProperty("s7_REGULATION");
                        if (null == dr || dr.equals(".") || dr.equals("")) {
                            continue;
                        }  else {
                        	if( drCompare.equals("") ) {
                        		drCompare = dr;
                        	} else {
                        		if(Integer.parseInt(dr.substring(2)) < Integer.parseInt( drCompare.substring(2)))  {
                        			drCompare = dr;
                        		}
                        	}
                        }
                    }
                }
            }
             String opDR = operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_DR);
             if( drCompare.length() > 2 ) {
            	 if( null != opDR && !opDR.equals("") && opDR.length() > 2 ) {
            		 if(Integer.parseInt(opDR.substring(2)) < Integer.parseInt( drCompare.substring(2)))  {
            			 drCompare = opDR;
            		 }
            	 } 
             } else {
            	 if( null != opDR && !"".equals(opDR)) {
             		drCompare = opDR;
             	} 
             }
            
        }  // 공법 하위에 End Item 이 없을 경우에는 공법의 DR 값을 적용
          else {
        	
        	if( null != operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_DR) && !"".equals(operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_DR))) {
        		drCompare = operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_DR);
        		
        	} 
        }



        return drCompare;
    }
    
    private Map<String, TCComponentItemRevision> getReplacedEndItems(TCComponentBOPLine operationLine, List<EndItemData> targetEndItems) throws Exception {
        Map<String, TCComponentItemRevision> revisions = new HashMap<String, TCComponentItemRevision>();
        AIFComponentContext[] contexts = operationLine.getChildren();
        if(contexts != null) {
            for(EndItemData endItem : targetEndItems) {
                String occPuid = endItem.getOcc_puid();
                for(AIFComponentContext context : contexts) {
                    TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
                    if(occPuid.equals(bopLine.getProperty(SDVPropertyConstant.BL_OCC_FND_OBJECT_ID))) {
                        revisions.put(endItem.getCitem_id(), findEndItemInMProduct(bopLine));
                    }
                }               
            }
        }
        
        return revisions;
    }
    
    private TCComponentItemRevision findEndItemInMProduct(TCComponentBOPLine bopLine) throws TCException, Exception {
        TCComponentItemRevision itemRevision = null;
        
        TCComponentBOMLine endItemBomline = SDVBOPUtilities.getAssignSrcBomLine(bopLine.window(), bopLine);
        if(endItemBomline != null) {
            itemRevision = endItemBomline.getItemRevision();
        }
        
        return itemRevision;
    }
    
    
//    private Date getFMPReleasedDate(TCComponentBOPLine operationBopLine , TCComponentBOMLine[] endItemLines) throws Exception {
//    	
//    	TCComponentBOPLine topBopLine = (TCComponentBOPLine)operationBopLine.window().getTopBOMLine();
//    	TCComponent mProductRevision = SDVBOPUtilities.getConnectedMProductItemRevision(topBopLine.getItemRevision());
//    	
//    	
//        BOMTreeTable[] table = new BOMTreeTable[2];
//
//        // 현재 BOP에 Active 된 BOP Table을 가져온다.
//        MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
//
//        // MPP에 오픈되어 있는 View들 중에서 MProduct와 연결된 View의 테이블을 찾는다.
//        AbstractViewableTreeTable[] treeTables = application.getViewableTreeTables();
//        for(AbstractViewableTreeTable treeTable : treeTables) {
//            if(treeTable instanceof CMEBOMTreeTable) {
//                if(mProductRevision.equals(treeTable.getBOMRoot().getItemRevision())) {
//                	table[0] = (BOMTreeTable) treeTable;
//                    
//                    break;
//                }
//            }
//        } // for문 끝
//        
//			BOMTreeTableModel tableModel = (BOMTreeTableModel) table[0].getTreeTableModel();
//			 Date fmpReleasedDate = null;
//			        for(int i = 0; i < endItemLines.length; i ++) {
//			        	TCComponentBOMLine endItem = endItemLines[i];
//			            TCComponentBOMLine endItemBomline = SDVBOPUtilities.getAssignSrcBomLine(table[0].getBOMWindow(), endItem);
//			            // [SR150421-019][20150518] shcho, Pack 되어있는 E/Item의 경우 Unpack을 하여 모두 빨간색으로 표시 되도록 수정
//			            TCComponentBOMLine fmpBomLine = null;
//			            
//			            while ( true ) {
//			            	 fmpBomLine =  endItemBomline.parent();
//			            	if( fmpBomLine.getItemRevision().getType().equals("S7_FunctionMastRevision")) {
//			            		break;
//			            	}
//			            }
//			            
//			            TCComponentItemRevision fmpRevision = fmpBomLine.getItemRevision();
//			            TCComponentBOMViewRevision fmpBomviewRevision = SDVBOPUtilities.getBOMViewRevision(fmpRevision, "bom_view");
//			            fmpReleasedDate = fmpBomviewRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
//			            
//			        }
//    
//        return fmpReleasedDate;
//    }
//    
//    
//    private TCComponentBOPLine findEndItemByOccPuid( TCComponentBOPLine latestOperation, String occPuid) throws TCException {
//        AIFComponentContext[] contexts = latestOperation.getChildren();
//        
//        if(contexts != null) {
//            for(AIFComponentContext context : contexts) {
//                TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
//                /* [SR150421-019][20150518] shcho, Pack 되어있는 E/Item의 경우 Unpack을 하여 모두 빨간색으로 표시 되도록 수정
//                // [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item 메뉴 기능 보완
//                if (bopLine.isPacked())
//                {
//                    bopLine.unpack();
//                    bopLine.refresh();
//                }
//                */
//                if(occPuid.equals(bopLine.getProperty(SDVPropertyConstant.BL_OCC_FND_OBJECT_ID))) {
//                    return bopLine;
//                }
//            }
//            /*[SR150421-019][20150518] shcho, Pack 되어있는 E/Item의 경우 Unpack을 하여 모두 빨간색으로 표시 되도록 수정
//            // [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item 메뉴 기능 보완
//            latestOperation.refresh();
//            */
//        }
//        
//        return null;
//    }
    
    
}  // Class 끝
