package com.symc.plm.me.sdv.operation.wp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVProcessUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentContextList;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;

/**
 * [SR140611-027][20140611] jwlee 용접조건표 을지 추가
 * [SR140702-044][20140702] shcho 용접 공법 ID체계 변경에 따른 용접조건표 시트 수정
 * [SR140709-043][20140709] jwlee, MECO 완료 후 용접공법 개정시 상위 리비전의 결재일자가  용접조건표에서 누락되는 오류 수정
 * [SR140902-070][201408011] shcho, 용접조건표 시트에 추가된 옵션 셀의 값을, SYSTEM에서 자동으로 입력 하도록 기능 추가      
 */
public class PreviewWeldConditionSheetDataHelper {

    private Map<String, Integer> propertyMap = new HashMap<String, Integer>();
    private String weldItemType = "";
    // BOMLine을 정의 해야 한다.
    private InterfaceAIFComponent userDefineTarget = null;
    
    private String weldConditionSheetType;

	public PreviewWeldConditionSheetDataHelper(String weldConditionSheetType) {
		this.weldConditionSheetType = weldConditionSheetType;
    }

    public IDataSet getDataSet() throws Exception{
        IDataSet dataSet = null;
        if(weldConditionSheetType!=null && weldConditionSheetType.equalsIgnoreCase("SPOT_TYPE")==true){
        	dataSet = getDataSpot();
        }else if(weldConditionSheetType!=null && weldConditionSheetType.equalsIgnoreCase("CO2_TYPE")==true){
        	dataSet = getDataCo2();
        }
        return dataSet;
    }

    protected IDataSet getDataSpot() throws Exception
    {
        IDataMap dataMap = new RawDataMap();

        propertyMap.put(SDVPropertyConstant.BL_OCCURRENCE_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_CONNECTED_PARTS, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_LINE, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_PRESSURIZATION, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_ETC, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.WELD_NUMBER_OF_SHEETS, SDVPropertyConstant.TYPE_STRING);

        List<HashMap<String, Object>> weldDataList = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> mecoDataList = new ArrayList<HashMap<String, Object>>();
        String compID = "";
        String revID = "";
        String productCode = "";
        String lineCode = "";
        String stationCode = "";
        String robotWorkArea = "";
        String gunNO = "";
        String occ_mvll = "";
        String option = "";
        String weldOptionCodeDescription = "";

        InterfaceAIFComponent component =  null;
        
        // 강제정의한 Target이 있으면 강제 정의된 Target을 우선 적용한다.
        // [NON-SR][20160217] taeku.jeong MECO에 포함된 용접 조건표를 강제 Update하는
        // 기능을 수행하도록 추가함.
        if(this.userDefineTarget!=null){
        	component = this.userDefineTarget;  
        }else{
        	component =  AIFUtility.getCurrentApplication().getTargetComponent();
        }
        
        if(component != null && component instanceof TCComponentBOPLine)
        {
            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOPLine comp = (TCComponentBOPLine) bomWindow.getTopBOMLine();

            // WeldOP ID
            compID = ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            String[] tempCompID = compID.split("-");
            compID = tempCompID[2] + "-" + tempCompID[4] + "-" + tempCompID[5];
            // WeldOP Rev.
            if (tempCompID.length > 6) {
                revID = "-" + tempCompID[6] + " / " + ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            }else{
                revID =  " / " + ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            }
            // Product Code (Product 코드는 두개의 BOP 를 가질수도 있는경우가 발생할수 있기 때문에 두개의 BOP 에서 가져와야 한다)
            productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            // Line Code
            lineCode = ((TCComponentBOPLine)component).parent().parent().getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
            // 공정 Code
            stationCode = ((TCComponentBOPLine)component).parent().getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);
            // 용접공법 ID 를 기준으로 연결되어 있는 일반 공법에 ID 를 가져온다
            TCComponent opItemRevision = ((TCComponentBOMLine)component).getItemRevision().getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
            String oPID = opItemRevision.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID);;
            // for (int i = 0; i < (weldOP_IDs.length - 2); i++)
            // {
            // oPID += weldOP_IDs[i];
            // if (i == (weldOP_IDs.length - 3))
            // break;
            // oPID += "-";
            // }

            // weldItem type
            weldItemType = SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM;

            // GunID
            TCComponentBOPLine gunBopLine = getGunNO((TCComponentBOPLine)component);
            if (gunBopLine != null)
                gunNO = gunBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);

            // 로봇 BOPLine 정보를 가져온다
            TCComponentBOPLine robotLine = getRobotNO((TCComponentBOPLine)component);
            // 로봇이 없으면 오토건으로 판단 WorkArea 정보를 가져오지 않는다
            if (robotLine != null)
            {
                // Robot에 WorkArea 값을 가져온다
                TCComponentBOMLine robotBopLine = getHavePathNodeBopLine(gunBopLine, comp);
                if (robotBopLine != null)
                {
                    String workAreaName = robotBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    String[] waName = workAreaName.split("-");
                    if (waName.length > 3)
                        robotWorkArea = waName[2] + "-" + waName[3];
                }

                if (robotWorkArea == null || robotWorkArea.equals(""))
                    robotWorkArea = getRobotWorkArea((TCComponentBOPLine)component, oPID, gunNO);
            }

            // GunID 를 Display 용으로 바꾼다
            String[] gunName = gunNO.split("-");
            if (gunName.length > 3){
                gunNO = "";
                for (int i = 3; i < gunName.length; i++) {
                    gunNO += gunName[i];
                    if (gunName.length == (i + 1)) {
                        break;
                    }
                    gunNO += "-";
                }
            }

            // Variant
            occ_mvll = ((TCComponentBOPLine)component).getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);

            weldDataList = getChildrenList(weldDataList, (TCComponentBOPLine) component);

            // Variant Description
            HashMap<String, Object> variantMap = SDVBOPUtilities.getVariant(((TCComponentBOPLine)component).getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));

            option = (String) variantMap.get("printDescriptions");

            // 선택된 용접공법 부터 상위 Revision 까지의 MECOList 를 가져온다
            List<String> mecoList = new ArrayList<String>();
            TCComponent mecoComponent = ((TCComponentBOPLine)component).getItemRevision().getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
            String mecoNO = "";
            if (mecoComponent != null)
            {
                mecoNO = mecoComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                mecoList.add(mecoNO);
            }
            else
            {
                mecoList.add(mecoNO);
            }

            // 가져온 MECOList 를 가지고 MECO 에 출력될 내용을 추출한다
            if (!mecoNO.equals(""))
            {
                mecoList = getMecoList(((TCComponentBOPLine)component).getItemRevision(), mecoList);
                mecoDataList = getMecoInfoList(mecoList);
            }
            
            //M7_BOPB_WELD_OPTION_CODE의 Description
            // [SR140902-070][201408011] shcho, 용접조건표 시트에 추가된 옵션 셀의 값을, SYSTEM에서 자동으로 입력 하도록 기능 추가   
            if (tempCompID.length > 6) {
                weldOptionCodeDescription = SDVLOVUtils.getLovValueDesciption("M7_BOPB_WELD_OPTION_CODE", tempCompID[6]);
            }
        }

        // 용접점 리스트
        IDataSet dataSet = convertToDataSet("weldList", weldDataList);
        // MECO List
        dataSet.addDataSet(convertToDataSet("mecoList", mecoDataList));

        dataMap.put("compID", compID);
        dataMap.put("revID", revID);

        dataMap.put("productCode", productCode);
        dataMap.put("lineCode", lineCode);
        dataMap.put("stationCode", stationCode);
        dataMap.put("gunNO", gunNO);
        dataMap.put("robotWorkArea", robotWorkArea);
        dataMap.put("occ_mvll", occ_mvll);
        // option code
        //dataMap.put("optionCode", variantMap.get("printValues"));
        // option description
        dataMap.put("optionDescription", option);
        // [SR140902-070][201408011] shcho, 용접조건표 시트에 추가된 옵션 셀의 값을, SYSTEM에서 자동으로 입력 하도록 기능 추가      
        dataMap.put("weldOptionCodeDescription", weldOptionCodeDescription);

        dataSet.addDataMap("weldCondSheetInfo", dataMap);

        return dataSet;
    }
    
    protected IDataSet getDataCo2() throws Exception
    {
        IDataMap dataMap = new RawDataMap();

        propertyMap.put(SDVPropertyConstant.BL_OCCURRENCE_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_CONNECTED_PARTS, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_LINE, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_PRESSURIZATION, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_ETC, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.WELD_NUMBER_OF_SHEETS, SDVPropertyConstant.TYPE_STRING);

        List<HashMap<String, Object>> weldDataList = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> mecoDataList = new ArrayList<HashMap<String, Object>>();
        String compID = "";
        String revID = "";
        String productCode = "";
        String lineCode = "";
        String stationCode = "";
        String robotWorkArea = "";
        String gunNO = "";
        String occ_mvll = "";
        String option = "";
        String weldOptionCodeDescription = "";

        InterfaceAIFComponent component =  null;
        
        if(this.userDefineTarget==null){
        	System.out.println("this.userDefineTarget = null");
        }
        
        // 강제정의한 Target이 있으면 강제 정의된 Target을 우선 적용한다.
        // [NON-SR][20160217] taeku.jeong MECO에 포함된 용접 조건표를 강제 Update하는
        // 기능을 수행하도록 추가함.
        if(this.userDefineTarget!=null){
        	component = this.userDefineTarget;
        	System.out.println("A");
        }else{
        	component =  AIFUtility.getCurrentApplication().getTargetComponent();
        	System.out.println("B");
        }
        
    	System.out.println("component.getClass().getName() = "+component.getClass().getName());
        
        if(component != null && component instanceof TCComponentBOPLine)
        {
            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOPLine comp = (TCComponentBOPLine) bomWindow.getTopBOMLine();

            // WeldOP ID
            compID = ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            String[] tempCompID = compID.split("-");
            compID = tempCompID[2] + "-" + tempCompID[4] + "-" + tempCompID[5];
            // WeldOP Rev.
            if (tempCompID.length > 6) {
                revID = "-" + tempCompID[6] + " / " + ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            }else{
                revID =  " / " + ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            }
            // Product Code (Product 코드는 두개의 BOP 를 가질수도 있는경우가 발생할수 있기 때문에 두개의 BOP 에서 가져와야 한다)
            productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            // Line Code
            lineCode = ((TCComponentBOPLine)component).parent().parent().getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
            // 공정 Code
            stationCode = ((TCComponentBOPLine)component).parent().getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);
            // 용접공법 ID 를 기준으로 연결되어 있는 일반 공법에 ID 를 가져온다
            TCComponent opItemRevision = ((TCComponentBOMLine)component).getItemRevision().getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
            String oPID = opItemRevision.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID);;
            // for (int i = 0; i < (weldOP_IDs.length - 2); i++)
            // {
            // oPID += weldOP_IDs[i];
            // if (i == (weldOP_IDs.length - 3))
            // break;
            // oPID += "-";
            // }

            // weldItem type
            weldItemType = SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM;

            // GunID
            TCComponentBOPLine gunBopLine = getGunNO((TCComponentBOPLine)component);
            if (gunBopLine != null)
                gunNO = gunBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);

            // 로봇 BOPLine 정보를 가져온다
            TCComponentBOPLine robotLine = getRobotNO((TCComponentBOPLine)component);
            // 로봇이 없으면 오토건으로 판단 WorkArea 정보를 가져오지 않는다
            if (robotLine != null)
            {
                // Robot에 WorkArea 값을 가져온다
                TCComponentBOMLine robotBopLine = getHavePathNodeBopLine(gunBopLine, comp);
                if (robotBopLine != null)
                {
                    String workAreaName = robotBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    String[] waName = workAreaName.split("-");
                    if (waName.length > 3)
                        robotWorkArea = waName[2] + "-" + waName[3];
                }

                if (robotWorkArea == null || robotWorkArea.equals(""))
                    robotWorkArea = getRobotWorkArea((TCComponentBOPLine)component, oPID, gunNO);
            }

            // GunID 를 Display 용으로 바꾼다
            String[] gunName = gunNO.split("-");
            if (gunName.length > 3){
                gunNO = "";
                for (int i = 3; i < gunName.length; i++) {
                    gunNO += gunName[i];
                    if (gunName.length == (i + 1)) {
                        break;
                    }
                    gunNO += "-";
                }
            }

            // Variant
            occ_mvll = ((TCComponentBOPLine)component).getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);

            weldDataList = getChildrenList(weldDataList, (TCComponentBOPLine) component);

            // Variant Description
            HashMap<String, Object> variantMap = SDVBOPUtilities.getVariant(((TCComponentBOPLine)component).getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));

            option = (String) variantMap.get("printDescriptions");

            // 선택된 용접공법 부터 상위 Revision 까지의 MECOList 를 가져온다
            List<String> mecoList = new ArrayList<String>();
            TCComponent mecoComponent = ((TCComponentBOPLine)component).getItemRevision().getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
            String mecoNO = "";
            if (mecoComponent != null)
            {
                mecoNO = mecoComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                mecoList.add(mecoNO);
            }
            else
            {
                mecoList.add(mecoNO);
            }

            // 가져온 MECOList 를 가지고 MECO 에 출력될 내용을 추출한다
            if (!mecoNO.equals(""))
            {
                mecoList = getMecoList(((TCComponentBOPLine)component).getItemRevision(), mecoList);
                mecoDataList = getMecoInfoList(mecoList);
            }
            
            //M7_BOPB_WELD_OPTION_CODE의 Description
            // [SR140902-070][201408011] shcho, 용접조건표 시트에 추가된 옵션 셀의 값을, SYSTEM에서 자동으로 입력 하도록 기능 추가   
            if (tempCompID.length > 6) {
                weldOptionCodeDescription = SDVLOVUtils.getLovValueDesciption("M7_BOPB_WELD_OPTION_CODE", tempCompID[6]);
            }
        }

        // 용접점 리스트
        IDataSet dataSet = convertToDataSet("weldList", weldDataList);
        // MECO List
        dataSet.addDataSet(convertToDataSet("mecoList", mecoDataList));

        dataMap.put("compID", compID);
        dataMap.put("revID", revID);

        dataMap.put("productCode", productCode);
        dataMap.put("lineCode", lineCode);
        dataMap.put("stationCode", stationCode);
        dataMap.put("gunNO", gunNO);
        dataMap.put("robotWorkArea", robotWorkArea);
        dataMap.put("occ_mvll", occ_mvll);
        // option code
        //dataMap.put("optionCode", variantMap.get("printValues"));
        // option description
        dataMap.put("optionDescription", option);
        // [SR140902-070][201408011] shcho, 용접조건표 시트에 추가된 옵션 셀의 값을, SYSTEM에서 자동으로 입력 하도록 기능 추가      
        dataMap.put("weldOptionCodeDescription", weldOptionCodeDescription);

        dataSet.addDataMap("weldCondSheetInfo", dataMap);

        return dataSet;
    }

    /**
     * 용접공법에 연결되어 있는 TargetOP 에 Plant 정보를 가져온다
     *
     *
     * @method getRobotWorkArea
     * @date 2013. 12. 2.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getRobotWorkArea(TCComponentBOPLine weldOP, String opID, String gunID) throws TCException
    {
        String robotWorkID = "";
//        boolean plantCheck = false;
        TCComponentBOPLine stationOP = (TCComponentBOPLine) weldOP.parent();
        TCComponentBOPLine normalOP = null;
        AIFComponentContext[] stationChilds = stationOP.getChildren();
        for (AIFComponentContext stationChild : stationChilds)
        {
            TCComponentBOPLine child = (TCComponentBOPLine) stationChild.getComponent();
            if (child.getProperty(SDVPropertyConstant.BL_ITEM_ID).equals(opID))
                normalOP = child;
//            if (child.getType().equals(SDVTypeConstant.PLANT_OPAREA_ITEM))
//                plantCheck = true;
        }
        robotWorkID = getPlantResource(normalOP, gunID);
//        if (plantCheck)
//            robotWorkID = getPlantResource(stationOP, gunID);
//        else
//            robotWorkID = getPlantResource(normalOP, gunID);

        return robotWorkID;
    }

    private TCComponentBOMLine getHavePathNodeBopLine(TCComponentBOPLine gunItem, TCComponentBOPLine targetOP) throws TCException
    {
        if (gunItem == null || targetOP == null) return null;

        TCComponentBOMLine plantLine = getPlantBOMLine(targetOP);
        TCComponentBOMWindow plantWindow = plantLine.window();

        TCComponentMEAppearancePathNode[] linkedAppearances = gunItem.askLinkedAppearances(false);
        if (linkedAppearances == null) return null;

        TCComponentBOMLine linkedBOMLine = null;

        for (TCComponentMEAppearancePathNode linkedAppearance : linkedAppearances)
        {
        	try {
        		
        		linkedBOMLine = plantWindow.getBOMLineFromAppearancePathNode(linkedAppearance, plantLine);
        	} catch (Exception e) {
        		e.getStackTrace();
        	}
        }
        if (linkedBOMLine == null) return null;
        return linkedBOMLine.parent();
    }

    private TCComponentBOMLine getPlantBOMLine(TCComponentBOMLine selectBOMLine) {
        TCComponentBOMLine rootBomline;
        String rootBomView = null;
        String targetPlant = null;
        try {
            TCComponentItemRevision itemRevision = selectBOMLine.window().getTopBOMLine().getItemRevision();
            targetPlant = getPlant(itemRevision);
        } catch (TCException e) {
            e.printStackTrace();
        }
        IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
        for (IViewReference viewRerence : arrayOfIViewReference) {
            IViewPart localIViewPart = viewRerence.getView(false);
            if (localIViewPart == null)
                continue;
            CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
            if (cmeBOMTreeTable == null)
                continue;

            rootBomline = cmeBOMTreeTable.getBOMRoot();
            try {
                rootBomView = rootBomline.getProperty("bl_item_item_id");
            } catch (TCException e) {
                e.printStackTrace();
            }
            if (targetPlant != null && rootBomView != null)
            {
                if (targetPlant.equals(rootBomView))
                    return rootBomline;
            }

        }
        return null;
    }

    private String getPlant(TCComponentItemRevision revision) throws TCException {
        String plantType = null;
        String plantId = null;
        TCComponent[] plant = revision.getRelatedComponents(SDVTypeConstant.MFG_WORKAREA);
        if (plant.length == 1)
        {
            plantType = plant[0].getProperty("object_type");
            if (plantType.equals("PlantShopRevision"))
                return plantId = plant[0].getProperty("item_id");
        }
        return plantId;
    }

    /**
     *  Target OP 하위에 있는 Plant 정보를 가져온다
     *
     * @method getPlantResource
     * @date 2013. 12. 2.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getPlantResource(TCComponentBOPLine targetComp, String gunID) throws TCException
    {
        String plantID = "";

        AIFComponentContext[] targetChilds = targetComp.getChildren();
        for (AIFComponentContext targetChild : targetChilds)
        {
            TCComponentBOPLine tempOPchild = (TCComponentBOPLine)targetChild.getComponent();
            if (tempOPchild.getItem().getType().equals(SDVTypeConstant.PLANT_OPAREA_ITEM))
            {
                AIFComponentContext[] plantChilds = tempOPchild.getChildren();
                for (AIFComponentContext plantChild : plantChilds)
                {
                    TCComponentBOPLine tempPlantchild = (TCComponentBOPLine)plantChild.getComponent();
                    if (tempPlantchild.getProperty(SDVPropertyConstant.BL_ITEM_ID).equals(gunID))
                    {
                        String workAreaName = tempOPchild.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                        String[] waName = workAreaName.split("-");
                        plantID = waName[2] + "-" + waName[3];
                        break;
                    }
                }
            }
        }
        return plantID;
    }

    /**
     *  선택한 용접공법에 Gun ID 를 가져온다
     *
     * @method getGunNO
     * @date 2013. 12. 2.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOPLine getGunNO(TCComponentBOPLine weldOP) throws TCException
    {
        AIFComponentContext[] weldOPChilds = weldOP.getChildren();
        for (AIFComponentContext weldOPChild : weldOPChilds)
        {
            TCComponentBOPLine gunComponent = (TCComponentBOPLine) weldOPChild.getComponent();
            if (gunComponent.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM))
                return gunComponent;
        }
        return null;
    }

    /**
     *  선택한 용접공법에 Robot ID 를 가져온다
     *
     * @method getRobotNO
     * @date 2014. 1. 29.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOPLine getRobotNO(TCComponentBOPLine weldOP) throws TCException
    {
        AIFComponentContext[] weldOPChilds = weldOP.getChildren();
        for (AIFComponentContext weldOPChild : weldOPChilds)
        {
            TCComponentBOPLine gunComponent = (TCComponentBOPLine) weldOPChild.getComponent();
            if (gunComponent.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM))
                return gunComponent;
        }
        return null;
    }

    /**
     * 사용자가 선택한 Component 하위의 자식 Component들의 정보를 가져온다.
     *
     * @method getChildrenList
     * @date 2013. 10. 28.
     * @param
     * @return List<HashMap<String,Object>>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    private List<HashMap<String, Object>> getChildrenList(List<HashMap<String, Object>> dataList, TCComponentBOPLine parentLine) throws Exception
    {
    	AIFComponentContext[] context = parentLine.getChildren();
//        if(parentLine.getChildrenCount() > 0)
          if(context.length > 0)
        {
            for(int i = 0; i < context.length; i++)
            {
                if(context[i].getComponent() instanceof TCComponentBOPLine)
                {
                    TCComponentBOPLine childLine = (TCComponentBOPLine)context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if(weldItemType.equals(type))
                    {
                        HashMap<String, Object> dataMap = convertComponent(childLine);
                        // 용접점에 bl_connected_lines 에 있는 Item 에 속성 값을 가져온다
                        dataMap = getEndItemProperties(dataMap);

                        dataList.add(dataMap);
                    }
                    else
                    {
                        getChildrenList(dataList, (TCComponentBOPLine)context[i].getComponent());
                    }
                }
            }
        }

        return dataList;
    }

    /**
     * 부모 정보 가져오기
     *
     * @method getParentInfo
     * @date 2013. 11. 11.
     * @param
     * @return HashMap<String,Object>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public IDataMap getParentInfo(TCComponentBOPLine weldOP, IDataMap dataMap, String type) throws Exception
    {
        if (weldOP.parent() != null) {
            TCComponentBOPLine parent = (TCComponentBOPLine) weldOP.parent();
            if (parent.getItem().getType().equals(type))
            {
                if(type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
                {
                    String shop_code = parent.getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_ID, shop_code);
                }
                else if(type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
                {
                    String line_code = parent.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
                    dataMap.put(type + SDVPropertyConstant.LINE_REV_CODE, line_code);
                }
                else if(type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
                {
                    String station_code = parent.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);
                    dataMap.put(type + SDVPropertyConstant.STATION_STATION_CODE, station_code);
                }
                else if(type.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
                {
                    String weldOp_ID = parent.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    String weldOp_rev = parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_ID, weldOp_ID);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, weldOp_rev);
                }
            }
            else
            {
                return getParentInfo(parent, dataMap, type);
            }
        }
        return dataMap;
    }

    /**
    *
    *
    * @method convertToDataSet
    * @date 2013. 11. 27.
    * @param
    * @return IDataSet
    * @exception
    * @throws
    * @see
    */
   private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList)
   {
       IDataSet dataSet = new DataSet();
       IDataMap dataMap = new RawDataMap();
       dataMap.put(dataName, dataList, IData.TABLE_FIELD);
       dataSet.addDataMap(dataName, dataMap);

       return dataSet;
   }


    /**
     * Component의 속성을 가져와 HashMap으로 저장한다.
     *
     * @method convertComponent
     * @date 2013. 10. 28.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> convertComponent(TCComponentBOPLine component) throws TCException
    {
        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        Iterator<String> iterator = propertyMap.keySet().iterator();
        while(iterator.hasNext())
        {
            String key = iterator.next();
            int value = (int) propertyMap.get(key);

            switch(value)
            {
                case 0x01 : dataMap.put(key, component.getProperty(key)); break;
                case 0x02 : dataMap.put(key, component.getIntProperty(key)); break;
                case 0x03 : dataMap.put(key, component.getDoubleProperty(key)); break;
                case 0x04 : dataMap.put(key, component.getProperty(key)); break;
                case 0x05 : dataMap.put(key, component.getReferenceProperty(key)); break;
                default : break;
            }
        }

        return dataMap;
    }

    /**
     *  End 아이템에 bl_connected_lines 에 들어 있는 아이템 리비전을 검색하여
     *  s7_material(재질) / s7_thickness(두께) 정보를 가져온다
     *
     * @method getEndItemProperties
     * @date 2013. 12. 4.
     * @param
     * @return HashMap<String,Object>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getEndItemProperties(HashMap<String, Object> dataMap) throws Exception
    {
        String[] connectedItems = ((String) dataMap.get(SDVPropertyConstant.BL_CONNECTED_PARTS)).split(",");
        String materialThickness = "";
        int MfgNumber = 0;
        if (!connectedItems[0].isEmpty())
        {
            String material;
            String thickness;
            MfgNumber = connectedItems.length;

            for (int i = 0; i < connectedItems.length; i++)
            {
                String endItemID = connectedItems[i].substring(0, connectedItems[i].indexOf("/"));  //[SR140611-027][20140611] jwlee 용접조건표 을지 추가
                String endItemRev = connectedItems[i].substring(connectedItems[i].indexOf("/") + 1);    //[SR140611-027][20140611] jwlee 용접조건표 을지 추가
                endItemID = endItemID.trim();
                endItemRev = endItemRev.trim();
                TCComponentItemRevision findEndItemRevision = CustomUtil.findItemRevision(SDVTypeConstant.EBOM_VEH_PART_REV, endItemID, endItemRev);
                //material = findEndItemRevision.getProperty(SDVPropertyConstant.S7_MATERIAL);
                TCComponent referenceItem = findEndItemRevision.getReferenceProperty(SDVPropertyConstant.S7_MATERIAL);
                if (referenceItem != null)
                {
                    material = referenceItem.getProperty(SDVPropertyConstant.S7_SES_CODE);
                    thickness = findEndItemRevision.getProperty(SDVPropertyConstant.S7_THICKNESS);
                    materialThickness += thickness + "t(" + material + ")";
                    if((i+1) != connectedItems.length)
                        materialThickness += "+";
                }
            }
        }
        dataMap.put("MaterialThickness", materialThickness);
        dataMap.put("MfgNumber", MfgNumber);
        return dataMap;
    }


    /**
     * 타겟 아이템 리비전 부터 상위 리비전 까지의 들어 있는 MECO List 를 가져온다
     *
     * @method getMecoList
     * @date 2013. 12. 10.
     * @param
     * @return List<String>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public static List<String> getMecoList(TCComponentItemRevision target, List<String> mecoList) throws Exception
    {
        TCComponentItemRevision revision = CustomUtil.getPreviousRevision(target);

        if (revision != null)
        {
            TCComponent mecoComponent = revision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
            String mecoNo = "";
            if (mecoComponent != null)
            {
                mecoNo = mecoComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                mecoList.add(mecoNo);
            }
            else
            {
                mecoList.add(mecoNo);
            }
            getMecoList(revision, mecoList);
        }
        return mecoList;
    }


    /**
     * MECO Item ID 를 받아서 MECO 상세정보를 추출 한다
     *
     * @method getMecoInfoList
     * @date 2013. 12. 11.
     * @param
     * @return List<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
    public static List<HashMap<String, Object>> getMecoInfoList(List<String> list) throws Exception
    {
        List<HashMap<String, Object>> DataList = new ArrayList<HashMap<String, Object>>();
        char lowerAlphabat = 97;
        char upperAlphabat = 65;
        int number = 0;

        for (int i = list.size(); i > 0; i--)
        {
            HashMap<String, Object> mecoInfo = new HashMap<String, Object>();
            TCComponentItemRevision mecoRev = CustomUtil.findItemRevision(SDVTypeConstant.MECO_ITEM_REV, list.get(i - 1), "000");
            //TCComponentItemRevision mecoRev = CustomUtil.findLatestItemRevision(SDVTypeConstant.MECO_ITEM_REV, list.get(i - 1));
            if (mecoRev == null) continue;

            // MECO Type 에 따라 순서를 따로 부여한다
            if (mecoRev.getProperty(SDVPropertyConstant.MECO_TYPE).equals("PBI"))
            {
                mecoInfo.put("changeNo", Integer.toString(number));
                number++;
            }
            else if (mecoRev.getProperty(SDVPropertyConstant.MECO_TYPE).equals("MEW"))
            {
                mecoInfo.put("changeNo", Character.toString(upperAlphabat));
                upperAlphabat++;
            }
            else
            {
                mecoInfo.put("changeNo", Character.toString(lowerAlphabat));
                lowerAlphabat++;
            }

            // 일자 (MECO 결재일)
            // [SR140709-043][20140709] jwlee, MECO 완료 후 용접공법 개정시 상위 리비전의 결재일자가  용접조건표에서 누락되는 오류 수정 (기존 PROCESS_STAGE_LIST에서 가져오던 날짜를 MECO 결재 Task signoff 날짜를 가져오도록 변경) 
            Date releaseDate = null;
            HashMap<String, TCComponent[]> signoffs = new HashMap<String, TCComponent[]>();
            AIFComponentContext[] ctx = mecoRev.whereReferenced();
            for (int j = 0; j < ctx.length; j++) {
                TCComponent component = (TCComponent) ctx[j].getComponent();
                if(component instanceof TCComponentTask) {
                    TCComponentTask task = (TCComponentTask) component;
                    signoffs = SDVProcessUtils.getSignOffs(signoffs, task);
                    if(signoffs.containsKey("Team Leader")){
                        TCComponentSignoff reader = (TCComponentSignoff) signoffs.get("Team Leader")[0];
                        releaseDate = reader.getDecisionDate();
                        break;
                    }
                }
            }
            if (releaseDate != null)
                mecoInfo.put(SDVPropertyConstant.ITEM_DATE_RELEASED, SDVStringUtiles.dateToString(releaseDate, "yyyy-MM-dd"));

            // MECO ID (MECO ID)
            mecoInfo.put(SDVPropertyConstant.ITEM_ITEM_ID, mecoRev.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));

            // 변경내용
            mecoInfo.put(SDVPropertyConstant.ITEM_OBJECT_DESC, mecoRev.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));

            // 담당
            TCComponentUser owningUser = (TCComponentUser) mecoRev.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
            mecoInfo.put(SDVPropertyConstant.ITEM_OWNING_USER, getUserName(owningUser));

            // 팀장
            mecoInfo.put("APPR", getMECOTeamLeaderSignoff(mecoRev));

            DataList.add(mecoInfo);
        }
        return DataList;
    }

    /**
     * MECO 결재자 중 팀장 이름을 조회한다.
     *
     * @method getMECOTeamLeaderSignoff
     * @date 2013. 11. 22.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getMECOTeamLeaderSignoff(TCComponentItemRevision mecoRev) throws TCException
    {
        HashMap<String, TCComponent[]> signoffs = new HashMap<String, TCComponent[]>();
        AIFComponentContext[] ctx = mecoRev.whereReferenced();
        if(ctx != null)
        {
            for(int i = 0; i < ctx.length; i++)
            {
                TCComponent component = (TCComponent) ctx[i].getComponent();
                if(component instanceof TCComponentTask)
                {
                    TCComponentTask task = (TCComponentTask) component;
                    signoffs = SDVProcessUtils.getSignOffs(signoffs, task);
                    if(signoffs.containsKey("Team Leader"))
                    {
                        TCComponentSignoff reader = (TCComponentSignoff) signoffs.get("Team Leader")[0];
                        TCComponentUser user = reader.getGroupMember().getUser();
                        return getUserName(user);
                    }
                }
            }
        }
        return "";
    }

    /**
     * User 이름을 가져온다
     *
     * @method getUserName
     * @date 2013. 12. 11.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getUserName(TCComponentUser user) throws TCException
    {
        String userName = "";
        TCComponentPerson person = (TCComponentPerson) user.getUserInformation().get(0);
        userName = person.getProperty("user_name");
        /*if(configId == 0) {
            if(person != null) {
            }
        } else {
            userName = user.getOSUserName();
        }*/

        return userName;
    }

    /**
     * MECO 정보를 가져온다
     *
     * @method getMECO
     * @date 2013. 12. 16.
     * @param
     * @return TCComponentItem
     * @exception
     * @throws
     * @see
     */
    public static TCComponentItem getMECO(String mecoID) throws Exception
    {
        TCComponentItem mecoItem = null;
        if (mecoID != null && !"".equals(mecoID))
            mecoItem = SDVBOPUtilities.FindItem(mecoID, SDVTypeConstant.MECO_ITEM);

        return mecoItem;
    }

   /**
    * MECOItemRevision 하위 원하는 폴더에서 Target Item을 가져온다
    *
    * @method getMecoTargetList
    * @date 2013. 12. 16.
    * @param
    * @return List<TCComponentItemRevision>
    * @exception
    * @throws
    * @see
    */
    @SuppressWarnings("unused")
    public static List<TCComponentItemRevision> getMecoTargetList(TCComponentItemRevision mecoItemRevision, String folderType, String targetType) throws TCException
    {
        List<TCComponentItemRevision> targetList = new ArrayList<TCComponentItemRevision>();
        TCComponentFolder targetFolder = null;
        //TCComponent[] tmpComponent = mecoItemRevision.getRelatedComponents(TcDefinition.TC_SPECIFICATION_RELATION);
        //TCComponent[] adsd = mecoItemRevision.getClassificationObjects();
        TCComponentContextList tmpComponent = mecoItemRevision.getRelatedList();
        TCComponent[] solutionFolderChilds = mecoItemRevision.getRelatedComponents(folderType);//getRelatedComponents(TcDefinition.TC_SPECIFICATION_RELATION);
        //CustomUtil.getDatasets(itemRevision, relationType, dataType)

        for (TCComponent Child : solutionFolderChilds)
        {
            if (Child.getType().equals(targetType))
            {
                targetList.add((TCComponentItemRevision) Child);
            }
        }

        return targetList;
    }

    /**
     * [NON-SR][20160217] taeku.jeong 용접조건표를 강제 Update 하기위해 용접조건표 검색 대상을 강제로 정의한다.
     * 용접조건표 강제 Update 대상이 되는 Object를 읽어옴.
     * @return
     */
    public InterfaceAIFComponent getUserDefineTarget() {
		return userDefineTarget;
	}

    /**
     * [NON-SR][20160217] taeku.jeong 용접조건표를 강제 Update 하기위해 용접조건표 검색 대상을 강제로 정의한다.
     * 용접조건표 강제 Update 대상이 되는 weld Operation의 BOMLine을 Target으로 강제 설정한다.
     * @param targetBOMLine
     */
	public void setUserDefineTarget( TCComponentBOPLine targetBOMLine) {
		this.userDefineTarget = (InterfaceAIFComponent)targetBOMLine;
	}

}
