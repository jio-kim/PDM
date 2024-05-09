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
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.dto.EndItemData;
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
 * [SR140915-015][20141030]shcho, ���� DR Type LOVǥ�ÿ� '(����)' ���� �߰��� ���Ͽ� �� ǥ�ù�� ���� (�۾�ǥ�ؼ����� '(����)'���� ����)
 * [SR141118-031][20141118]shcho, Revise�� �Ŀ� Activity �߰���, �����ȣ �����ϴ� �κп��� ���� Revision�� Activity�� ���ϸ鼭 �迭�� ũ�Ⱑ ���� �ٸ����� ���Ͽ� �߻��ϴ� ���� ���� 
 * [SR150317-021][20150323]ymjang, ���� �۾�ǥ�ؼ� Republish ������� ����
 * [SR150312-024][20150324]ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
 * [SR150105-031][20150401] shcho, �۾�ǥ�ؼ� ������(E/Item) ǥ�� Logic ���� ( E/Item Part No., ���迡�� �ο��� Option, ����(����)���� �ο��� Find No.�� �����ϸ� Pack�Ͽ� �۾�ǥ�ؼ��� ǥ��)
 * [SR150414-017][20150413] shcho, MECO SYMBOL�� Null(�ű� ���� ���) �� ��쿡 ���Ͽ� Null ó�� �߰�
 * [SR150612-003][20150612] shcho, Find No. ��ο� (1���� �ǳʶٴ� ��ȣ ���� ���������� �����ϵ��� �ο�)
 *                                                Pack �۾��� ������ Find No.�� �ǳʶٴ� ��ȣ ���� ���������� �ο����ִ� ����
 *                                                Pack �۾� ���Ŀ� Find No.�� ���������� ��ο���� ������.
 * [NON-SR][20150706] shcho, Find No. �ܼ��� ���������� �ο��ϴ°��� �ƴ϶�, �ǳʶٴ� ��쿡�� ��ȣ�� ��� ���������� �ϵ��� ����. 
 *                                       (�� : ���� 1�̶�� ������ 2�� �� ���, 2�� ��� ���� 1�� �״�� ���� �ؾ� ��.)         
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
     * MEPL�� Reload �ؾ��ϴ��� ���θ� Check �ϰ� �� ����� Return �Ѵ�.
     * @param operationLine
     * @return MEPL Reload�� �ʿ��ϸ� True�� Return �Ѵ�.
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
            	// ��ü��  Paint�� Station�� �����Ƿ� Station�� Ȯ�� �Ѵ�.
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
     * �־��� Item Revision�� ���� �����ϰ� MECO�� MEPL Load �� �ð��� ���ؼ� MEPL�� Reload �ؾ� ���� ���θ� Return �Ѵ�.
     * @param itemRevision
     * @param mecoId
     * @return MEPL Reload�� �ʿ��ϸ� true�� Return �Ѵ�.
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
                        // EPL Load �� �ð��� Structure�� ���������� ������ �ð����� ������(���� ũ��) EPL Reload ��� �ƴ�.
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
     * MECO Process ��� ������ Validation�� ���� �ϴ� ������ EPL Load �ð��� Ȯ�� �ϱ����� static���� ����
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
        
        // ���� Query������ �ʿ���� ������ ����. 
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
     * Header ������ �����´�.
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
        // [SR170210-014] Y400 ���� �۾�ǥ�ؼ� ���� ǥ�� ���� ���� �Ƿ� �ǿ� ���� ���� �ǰ�
        //--------------------------------------------------------------------------------------------------
        //SDVTypeConstant.EBOM_MPRODUCT
        // �ʿ��� ��� Shop Revision �� Product Code�� �˻��ϰ� �ش� Id�� ���� Item�� ã���� Product Item�ε�
        // S7_ProductRevision�� Property�� Project Code�� ã�� ���ִ�.
        // ����� Project Code�� MECO�� �������� �о� �´�.

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
        // Ư�� Ư�� �Ӽ��� �߰�
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
        
        // [NON_SR] [2015-10-16] taeku.jeong Infodba�� Publish �ϴ°�� �ۼ����̸���Operatoin Item Revsion�� Owner�� �����ϵ��� ������
        // [NON_SR] [2015-10-20] taeku.jeong Infodba�� Publish �ϴ°�� �ۼ����� Released ��¥�� �����Ѵ�.
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
     * ������ ������ Publish ��¥�� �����´�.
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
        	
        	// [NON_SR] [2015-10-16] taeku.jeong Infodba�� Publish �ϴ°�� �ۼ����̸���Operatoin Item Revsion�� Owner�� �����ϵ��� ������ 
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
     * Operation ������ �����´�.
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
                // [Non-SR] ����ȭ ����� ��û 
                // ���� ID ����� ������ Revision ID ���� ���� �Ͽ� �۾� ǥ�ؼ� ������ ����
                SDVPropertyConstant.ITEM_REVISION_ID,
                // Ư��Ư�� �Ӽ��߰�
                SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC
        });
        // [Non-SR] ����ȭ ����� ��û 
        // ���� ID ����� ������ Revision ID ���� ���� �Ͽ� �۾� ǥ�ؼ� ������ ����
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
        // DRB�� ��� DR�� ǥ��
        // [SR] ����ȭ ����� ��û - DR ���� ���� �߰�
        // DR �Ӽ� ���� ���� �� Preview �� �Է½� ���� ����
        String dr = getDRProperty(bopLine);
//        if("DRB".equals(dr)) {
//            dr = "DR";
//        } else {
//            //[SR140915-015][20141030]shcho, ���� DR Type LOVǥ�ÿ� '(����)' ���� �߰��� ���Ͽ� �� ǥ�ù�� ���� (�۾�ǥ�ؼ����� '(����)'���� ����)
//            //dr = properties[5]; 
//        }
        dataMap.put(SDVPropertyConstant.OPERATION_REV_DR, dr, IData.STRING_FIELD);

        // [SR150312-024] [20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
        // ���� ������ ���¸� üũ�ϱ� ���Ͽ� ������ �ʿ� Release ���ڸ� ��´�.
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date pdate_released = revision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
        dataMap.put(SDVPropertyConstant.ITEM_DATE_RELEASED, pdate_released == null ? null : format.format(pdate_released), IData.STRING_FIELD);
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Ư��Ư��  �Ӽ� �߰�
        dataMap.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, properties[9], IData.STRING_FIELD);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        return dataMap;
    }

    /**
     * ���ҽ� ����Ʈ�� �����´�.
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

        // ID �������� �ߺ� ����
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
        // ������ : bc.kim
        // ����ȭ ����� ��û 
        // �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Resource �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
        // �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
        // 20210713 seho ����....
        // ������ �꿡 ���� �������� �������°ɷ� ������.
        // ������ ���� �������� �������� �ȵ���.
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
    
    // ������ : bc.kim
    // ����ȭ ����� ��û 
    // �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Resource �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
    // �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
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
    
    
    // ������ : bc.kim
    // ����ȭ ����� ��û SR : [SR190131-060]
    // �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Resource �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
    // �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
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
     * MECO ����Ʈ�� �����´�.
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
     * Activity ����Ʈ�� �����´�.
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
                            //[SR����][20150224]shcho, �۾�ǥ�ؼ� ǥ��� getDoubleProperty �� �̿��Ͽ� ������ ��� �Ҽ��� �ڿ� ���ʿ��� 00000000001 �� ���� ���� ǥ�� �Ǵ� ���� ���� (String���� ������ Double�� ��ȯ)
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
		// �۾�ǥ�ؼ� Ư�� Ư�� ���� ���� ���� BomLine �߰�
        dataMap.put("BOMLINE_OBJECT", bopLine, IData.OBJECT_FIELD);
		/////////////////////////////////////////////////////////////////////////////////////////

        return dataMap;
    }

    /**
     * End Item ����Ʈ�� �����´�.
     *
     * [SR140828-014][20140827] shcho, BOPLine�� Option ������ �������� �Ӽ��� BL_OCC_MVL_CONDITION ���� BL_VARIANT_CONDITION���� ����
     *             (���� : Copy&Paste�� �Ҵ�� End Item�� ��� BL_OCC_MVL_CONDITION�� ���� ����.)
     * [SR150105-031][20150401] shcho, �۾�ǥ�ؼ� ������(E/Item) ǥ�� Logic ���� ( E/Item Part No., ���迡�� �ο��� Option, ����(����)���� �ο��� Find No.�� �����ϸ� Pack�Ͽ� �۾�ǥ�ؼ��� ǥ��)
     * [SR150612-003][20150612] shcho, Find No. ��ο� (1���� �ǳʶٴ� ��ȣ ���� ���������� �����ϵ��� �ο�)
     *                                                Pack �۾��� ������ Find No.�� �ǳʶٴ� ��ȣ ���� ���������� �ο����ִ� ����
     *                                                Pack �۾� ���Ŀ� Find No.�� ���������� ��ο���� ������.
     * [NON-SR][20150706] shcho,  Find No. �ܼ��� ���������� �ο��ϴ°��� �ƴ϶�, �ǳʶٴ� ��쿡�� ��ȣ�� ��� ���������� �ϵ��� ����. 
     *                                       (�� : ���� 1�̶�� ������ 2�� �� ���, 2�� ��� ���� 1�� �״�� ���� �ؾ� ��.)         
     * [NON-SR][20150706] taeku.jeong �۾����ؼ� EndItem List�Ǵ� �κп� MECO_EPL Data�� �о� ���� ��ȣ�� ǥ�����ִ� �κ� ���� �Ǵ� ����
     *                                                �ش� �κ��� ������ ������  MECO_EPL�� NEW_CHILD_TYPE Column�� ���� �ǵ� ���� ���� ���� Teamcenter Upgrade ���� �߻���
     *                                                'Vehicle Part', 'Standard Part'�� DB���� ���� Type �̸��� 'S7_Vehpart', 'S7_Stdpart' ��� ���� �߰� �Ǿ� �־���.
     *                                                �ش� �κ��� Query�� ���� SQL ���忡 Type ������ �߰� ó���ϰ� ���õ� ������ ���������� ���ߵǴ� �κе� ���� ������.
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
        	// 2015-10.02 taeku.jeong End Item MECO List�� �о�ͼ� �����ȣ�� ǥ�����ִ� �κ��� Query�� ������.
        	// ����ȭ ������� Ȯ�� ��û���� ���� �������� �ľ��� Query�� ������.
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

                    /* [SR150612-003][20150612] shcho, Find No. ��ο� (1���� �ǳʶٴ� ��ȣ ���� ���������� �����ϵ��� �ο�)
                     *                                                Pack �۾��� ������ Find No.�� �ǳʶٴ� ��ȣ ���� ���������� �ο����ִ� ����
                     *                                                Pack �۾� ���Ŀ� Find No.�� ���������� ��ο���� ������.
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
                     * ����ȭ ����� ��û 
                     * End Item ����Ʈ�� E-BOM ���� Replace �� ��Ʈ�� ������ �� ��� 
                     * Replace ���θ� �Ǵ� �Ͽ� ���� ��� �ֽ�MECO Symbol �� ���δ�.
                     */
//                    boolean hasReplaceEndItem = false;
//                    Map<String, TCComponentItemRevision> endItemRevs = null;
//                   if ( null == endItemMap.get("SYMBOL") || "".equals(endItemMap.get("SYMBOL"))) {
//                	   TCComponentBOPLine topBopLine = (TCComponentBOPLine) bopLine.window().getTopBOMLine();
//                	   // [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
//                	   TCComponent mProductRevision = SDVBOPUtilities.getConnectedMProductItemRevision(topBopLine.getItemRevision());
//                	   String revId = ((TCComponentBOPLine) bopLine).getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
//                	   List<EndItemData> targetEndItems = new ArrayList<EndItemData>();
//                	   // �ڵ� Replace �� �������� DB �����Ͽ� �����´�.
//                	   CustomBOPDao dao = new CustomBOPDao();  
//                	   ArrayList<EndItemData> replaceEndItems = dao.findReplacedEndItems(bopLine.getProperty(SDVPropertyConstant.BL_ITEM_PUID),
//                			   															mProductRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
//                	   // ���� ������ ���� EndItem �� Replace �����۵�� �� �Ͽ� ���� �������� �ִ��� �˻�
//                	   if( replaceEndItems != null) {
//                		   for( EndItemData endItem : replaceEndItems ) {
//                			   //Replace �� �� EndItem �� ���
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
	    			   // �ִٸ� MECO ����Ʈ�� ���� �ֱ��� Symbol �� ���ͼ� �Է�
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
                        //[SR150105-031][20150401] shcho, �۾�ǥ�ؼ� ������(E/Item) ǥ�� Logic ���� ( E/Item Part No., ���迡�� �ο��� Option, ����(����)���� �ο��� Find No.�� �����ϸ� Pack�Ͽ� �۾�ǥ�ؼ��� ǥ��)
                        if(map.get(SDVPropertyConstant.BL_ITEM_ID).toString().equals(itemPropValues[0])
                           && map.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION).toString().equals(variant)
                           && map.get("SEQ").toString().equals(blPropValues[3]))
                        {
                            DecimalFormat format = new DecimalFormat("0");
                            String oldQuantity = map.get(SDVPropertyConstant.BL_QUANTITY).toString();
                            String newQuantity = (format.format(Double.parseDouble(oldQuantity) + 1));
                            
                            map.put(SDVPropertyConstant.BL_QUANTITY, newQuantity);
                            
                            // MECO �����ȣ�� ���� ū ������ ǥ���Ѵ�.
                            // [SR150414-017][20150413] shcho, MECO SYMBOL�� Null(�ű� ���� ���) �� ��쿡 ���Ͽ� Null ó�� �߰�
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
            
            /* [SR150612-003][20150612] shcho, Find No. ��ο� (1���� �ǳʶٴ� ��ȣ ���� ���������� �����ϵ��� �ο�)
             *                                                  Pack �۾��� ������ Find No.�� �ǳʶٴ� ��ȣ ���� ���������� �ο����ִ� ����
             *                                                  Pack �۾� ���Ŀ� Find No.�� ���������� ��ο���� ������.
             * [NON-SR][20150706] shcho, Find No. �ܼ��� ���������� �ο��ϴ°��� �ƴ϶�, �ǳʶٴ� ��쿡�� ��ȣ�� ��� ���������� �ϵ��� ����. 
             *                                       (�� : ���� 1�̶�� ������ 2�� �� ���, 2�� ��� ���� 1�� �״�� ���� �ؾ� ��.)         
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
     * ������ ����Ʈ�� �����´�.
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
        // ������ : bc.kim
        // ����ȭ ����� ��û SR : [SR190131-060]
        // �۾�ǥ�ؼ� Preview ���� ���� ��ȸ�� Subsidiary �׸� �Ӽ����� ����( No Revise ) �Ǿ����� 
        // �ش� Resource �� ��ȣ�� MECO �� ��ȣ�� �°� ���� �Ǵ� ���� �߰�
        
        
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
     * ������ �پ� �ִ� �̹��� ���ø��� �����´�.
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
                  
                    //[SR141118-031][20141118]shcho, Revise�� �Ŀ� Activity �߰���, �����ȣ �����ϴ� �κп��� ���� Revision�� Activity�� ���ϸ鼭 �迭�� ũ�Ⱑ ���� �ٸ����� ���Ͽ� �߻��ϴ� ���� ����
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
     * Weld Operation�� �����ϴ� ������ Type�� �����ϴ� �Լ�
     * �� �Լ��� Weld Operation�� ���� ����ǥ�� �����ϱ����� ����.
     * Weld Operation�� ��������ǥ ����� 2������ �������µ� �־��� Weld Operation Revision��
     * �پ� �ִ� ������ ���� Spot ���� ������ ���� Co2, Brazing, Plug�� ������ ���� ������.
     * Co2 �迭 �������� Spot �������� ���� �ִ� ���� �Ҵ� ������ �ش�Ǹ� null�� Return �Ѵ�.
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
     * Weld Operation�� �����ϴ� ������ Type�� �����ϴ� �Լ�
     * �� �Լ��� Weld Operation�� ���� ����ǥ�� �����ϱ����� ����.
     * Weld Operation�� ��������ǥ ����� 2������ �������µ� �־��� Weld Operation Revision��
     * �پ� �ִ� ������ ���� Spot ���� ������ ���� Co2, Brazing, Plug�� ������ ���� ������.
     * Co2 �迭 �������� Spot �������� ���� �ִ� ���� �Ҵ� ������ �ش�Ǹ� null�� Return �Ѵ�.
     *  
     * @param weldOperationId Weld Operation Item Id
     * @param weldOperationRevId Weld Operation Item Revision Id
     * @return ��������ǥ�� ����� ��������ǥ Sheet Type
     */
    public static String getWeldConditionSheetType(String weldOperationId, String weldOperationRevId) {

	    DataSet ds = new DataSet();
	    ds.put("weld_op_id", weldOperationId );
	    ds.put("weld_op_rev_id", weldOperationRevId );

	    ArrayList<HashMap<String, Object>> resultList = null;
	    
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		try {
			resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(
					"com.kgm.service.WeldPoint2ndService", 
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
			// Spot�� Co2 �迭 ������ �Բ� �ִ� Weld Operation�� �߸� ������ Weld Operation��
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
     * ����ȭ ����� ��û DR �Ӽ��� ���� ���� 
     * DR�� �켱 ���� ( DR1 > DR2 > DR3 )
     * @param operation
     * @param dataMap
     * @return
     * @throws TCException
     */
    public String getDRProperty(TCComponentBOPLine operation ) throws TCException {
        // End Item DR �Ӽ� �켱( DR1 > DR2 > DR3 )
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
            
        }  // ���� ������ End Item �� ���� ��쿡�� ������ DR ���� ����
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
//        // ���� BOP�� Active �� BOP Table�� �����´�.
//        MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
//
//        // MPP�� ���µǾ� �ִ� View�� �߿��� MProduct�� ����� View�� ���̺��� ã�´�.
//        AbstractViewableTreeTable[] treeTables = application.getViewableTreeTables();
//        for(AbstractViewableTreeTable treeTable : treeTables) {
//            if(treeTable instanceof CMEBOMTreeTable) {
//                if(mProductRevision.equals(treeTable.getBOMRoot().getItemRevision())) {
//                	table[0] = (BOMTreeTable) treeTable;
//                    
//                    break;
//                }
//            }
//        } // for�� ��
//        
//			BOMTreeTableModel tableModel = (BOMTreeTableModel) table[0].getTreeTableModel();
//			 Date fmpReleasedDate = null;
//			        for(int i = 0; i < endItemLines.length; i ++) {
//			        	TCComponentBOMLine endItem = endItemLines[i];
//			            TCComponentBOMLine endItemBomline = SDVBOPUtilities.getAssignSrcBomLine(table[0].getBOMWindow(), endItem);
//			            // [SR150421-019][20150518] shcho, Pack �Ǿ��ִ� E/Item�� ��� Unpack�� �Ͽ� ��� ���������� ǥ�� �ǵ��� ����
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
//                /* [SR150421-019][20150518] shcho, Pack �Ǿ��ִ� E/Item�� ��� Unpack�� �Ͽ� ��� ���������� ǥ�� �ǵ��� ����
//                // [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item �޴� ��� ����
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
//            /*[SR150421-019][20150518] shcho, Pack �Ǿ��ִ� E/Item�� ��� Unpack�� �Ͽ� ��� ���������� ǥ�� �ǵ��� ����
//            // [SR150312-025][20150401] ymjang, Find Automatically Replaced E/Item �޴� ��� ����
//            latestOperation.refresh();
//            */
//        }
//        
//        return null;
//    }
    
    
}  // Class ��
