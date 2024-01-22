package com.symc.plm.me.sdv.operation.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;

public class ReportAssignmentWeldPointsListOperation extends SimpleSDVExcelOperation {

    private Map<String, Integer> propertyMap = new HashMap<String, Integer>();
    private String productCode = "";
    private String weldItemType = "";

    public ReportAssignmentWeldPointsListOperation() {
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            IDataSet dataSet = getData();

            if(dataSet != null) {
                String defaultFileName = productCode + "_" + "AssignmentWeldPointsList" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
                //AssignmentWeldPointsListDataExcelTransformer transformer = new AssignmentWeldPointsListDataExcelTransformer();
                //transformer.print(1, "M7_TEM_DocItemID_OperationMasterList", defaultFileName, dataSet);
            }
        } catch(Exception e) {
            setExecuteError(e);
            // MessageBox에 보여줄 메시지
            // 구현하지 않으면 default 메시지를 보여준다.
            //setErrorMessage("");
        }
    }

    @Override
    protected IDataSet getData() throws Exception {
        propertyMap.put(SDVPropertyConstant.BL_ITEM_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_OCCURRENCE_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.WELD_NUMBER_OF_SHEETS, SDVPropertyConstant.TYPE_STRING);

        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
        String compID = "";
        String revisionRule = "";
        String revRuleStandardDate = "";
        String variantRule = "";

        InterfaceAIFComponent component =  AIFUtility.getCurrentApplication().getTargetComponent();
        if(component != null && component instanceof TCComponentBOPLine) {
            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOPLine comp = (TCComponentBOPLine) bomWindow.getTopBOMLine();


            // product Code
            productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            // shop or line - id
            compID = comp.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

            // weldItem type
            weldItemType = SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM;

            // Revision Rule
            TCComponentRevisionRule bomWindowRevisionRule = bomWindow.getRevisionRule();
            revisionRule = bomWindowRevisionRule.toString();
            // Revision Rule 기준일
            Date rule_date = bomWindowRevisionRule.getDateProperty("rule_date");
            if(rule_date != null) {
                revRuleStandardDate = new SimpleDateFormat("yyyy-MM-dd").format(rule_date);
            }
            else {
                revRuleStandardDate = ExcelTemplateHelper.getToday("yyyy-MM-dd");
            }

            // Variant
            HashMap<String, String> variantMap = SDVBOPUtilities.getBOMConfiguredVariantSet(bomWindow);
            if(variantMap != null) {
                Iterator<String> iter = variantMap.keySet().iterator();
                while(iter.hasNext()) {
                    String key = iter.next();
                    String value = variantMap.get(key);
                    variantRule = variantRule + " " + key + "=" + value;
                }

                if(variantRule.length() > 1) {
                    variantRule = variantRule.substring(1);
                }
            }
            dataList = getChildrenList(dataList, (TCComponentBOPLine) component);
        }

        IDataSet dataSet = convertToDataSet("operationList", dataList);
        IDataMap dataMap = new RawDataMap();

        dataMap.put("productCode", productCode);
        dataMap.put("compID", compID);
        dataMap.put("revisionRule", revisionRule);
        dataMap.put("revRuleStandardDate", revRuleStandardDate);
        dataMap.put("variantRule", variantRule);
        dataMap.put("excelExportDate", ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm"));
        dataSet.addDataMap("additionalInfo", dataMap);

        return dataSet;
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
    private List<HashMap<String, Object>> getChildrenList(List<HashMap<String, Object>> dataList, TCComponentBOPLine parentLine) throws Exception {
        if(parentLine.getChildrenCount() > 0) {
            AIFComponentContext[] context = parentLine.getChildren();
            for(int i = 0; i < context.length; i++) {
                if(context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine)context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if(weldItemType.equals(type)) {
                        HashMap<String, Object> dataMap = convertComponent(childLine);

                        // bl_occ_mvl_condition 속성으로 option map 가져오기
                        HashMap<String, Object> option = SDVBOPUtilities.getVariant((String) dataMap.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION));

                        // option code
                        dataMap.put("optionCode", option.get("printValues"));

                        // option description
                        dataMap.put("optionDescription", option.get("printDescriptions"));

                        // Shop, Line, Station, Operation 속성 정보
                        dataMap = getParentInfo(childLine, dataMap, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
                        dataMap = getParentInfo(childLine, dataMap, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
                        dataMap = getParentInfo(childLine, dataMap, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                        dataMap = getParentInfo(childLine, dataMap, SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM);

                        dataList.add(dataMap);
                    } else {
                        if (type.equals(SDVTypeConstant.EBOM_MPRODUCT) || type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM)) {
                            continue;
                        }else{
                            getChildrenList(dataList, (TCComponentBOPLine)context[i].getComponent());
                        }
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
    public HashMap<String, Object> getParentInfo(TCComponentBOPLine weldPoint, HashMap<String, Object> dataMap, String type) throws Exception {
        if (weldPoint.parent() != null) {
            TCComponentBOPLine parent = (TCComponentBOPLine) weldPoint.parent();
            if (parent.getItem().getType().equals(type)) {
                if(type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)) {
                    String shop_id = parent.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    String shop_rev = parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_ID, shop_id);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, shop_rev);
                } else if(type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                    String line_code = parent.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
                    String line_rev = parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    dataMap.put(type + SDVPropertyConstant.LINE_REV_CODE, line_code);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, line_rev);
                } else if(type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                    String station_code = parent.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);
                    String station_rev = parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    dataMap.put(type + SDVPropertyConstant.STATION_STATION_CODE, station_code);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, station_rev);
                } else if(type.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM)) {
                    String weldOp_ID = parent.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    String weldOp_rev = parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    String weldOp_kor_name = parent.getProperty(SDVPropertyConstant.OPERATION_REV_KOR_NAME);
                    String targetOP = parent.getItemRevision().getProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
                    // GUN, ROBOT 에 ID 추출
                    AIFComponentContext[] childLists = parent.getChildren();
                    String gun_ID = null;
                    for (AIFComponentContext childList : childLists)
                    {
                        TCComponentBOPLine resourceItem = (TCComponentBOPLine) childList.getComponent();
                        if (resourceItem.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM)) {
                            TCComponentBOPLine gun = (TCComponentBOPLine) childList.getComponent();
                            gun_ID = gun.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                        }
                    }
                    if (gun_ID != null)
                        dataMap = getOPInfo(dataMap, parent, targetOP, gun_ID);

                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_ID, weldOp_ID);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, weldOp_rev);
                    dataMap.put(type + SDVPropertyConstant.OPERATION_REV_KOR_NAME, weldOp_kor_name);
                }
            } else {
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
    private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();
        dataMap.put(dataName, dataList, IData.TABLE_FIELD);
        dataSet.addDataMap(dataName, dataMap);

        return dataSet;
    }

    /**
     *  용접공법과 연결된 일반 공법 / Gun / Robot 정보를 가져온다
     *
     * @method getOPInfo
     * @date 2013. 11. 28.
     * @param
     * @return HashMap<String,Object>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getOPInfo(HashMap<String, Object> dataMap, TCComponentBOPLine weldOP, String targetOPID, String gunID) throws Exception{
        //boolean sharingCheck = false;
        // 용접공법 ID 를 기준으로 연결되어 있는 일반 공법에 ID 를 가져온다
        String weldOP_IDs[] = targetOPID.split("/");
        String oPID = weldOP_IDs[0];
//        for (int i = 0; i < (weldOP_IDs.length - 1); i++)
//        {
//            oPID += weldOP_IDs[i];
//            if (i == (weldOP_IDs.length - 2))
//                break;
//            oPID += "-";
//        }
        // 추출한 용접공법 ID로 일반공법 정보를 가져온다
        TCComponentBOPLine station = (TCComponentBOPLine)weldOP.parent();
        TCComponentBOPLine normalOP = null;
        AIFComponentContext[] stationChilds = station.getChildren();
        for (AIFComponentContext stationChild : stationChilds)
        {
            TCComponentBOPLine tempOP = (TCComponentBOPLine)stationChild.getComponent();
            if (tempOP.getProperty(SDVPropertyConstant.BL_ITEM_ID).equals(oPID))
                normalOP = tempOP;
//            if (tempOP.getItem().getType().equals(SDVTypeConstant.PLANT_OPAREA_ITEM))
//                sharingCheck = true;
        }
        if (normalOP != null)
        {
            String oPRev = normalOP.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
            String korOPName = normalOP.getItemRevision().getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME);
            String occMvl = normalOP.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
            dataMap.put(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM + SDVPropertyConstant.BL_ITEM_ID, oPID);
            dataMap.put(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM + SDVPropertyConstant.BL_ITEM_REV_ID, oPRev);
            dataMap.put(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM + SDVPropertyConstant.OPERATION_REV_KOR_NAME, korOPName);
            dataMap.put(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM + SDVPropertyConstant.BL_OCC_MVL_CONDITION, occMvl);

            if(occMvl != null && occMvl != ""){
                //공법 Option & Description
                HashMap<String, Object> option = SDVBOPUtilities.getVariant(occMvl);
                // option code
                dataMap.put("optionCode", option.get("printValues"));
                // option description
                dataMap.put("optionDescription", option.get("printDescriptions"));
            }

            dataMap = getPlantResource(dataMap, normalOP, gunID);
//            if (!sharingCheck)
//                dataMap = getPlantResource(dataMap, normalOP, gunID);
//            else
//                dataMap = getPlantResource(dataMap, station, gunID);
        }
        return dataMap;
    }

    /**
     *  공용 리소스 또는 공법에 할당된 리소스를 가져온다 (Gun, Robot)
     *
     * @method getPlantResource
     * @date 2013. 11. 29.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getPlantResource(HashMap<String, Object> dataMap, TCComponentBOPLine targetComp, String gunID) throws TCException
    {
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
                        String[] gunName = gunID.split("-");
                        String workAreaName = tempOPchild.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                        String[] waName = workAreaName.split("-");
                        dataMap.put(SDVTypeConstant.BOP_PROCESS_GUN_ITEM + SDVPropertyConstant.BL_ITEM_ID, gunName[2] + "-" + gunName[3]);
                        dataMap.put(SDVTypeConstant.PLANT_OPAREA_ITEM + SDVPropertyConstant.BL_ITEM_ID, waName[2] + "-" + waName[3]);
                        break;
                    }
                }
            }
        }
        return dataMap;
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
    private HashMap<String, Object> convertComponent(TCComponentBOPLine component) throws TCException {
        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        Iterator<String> iterator = propertyMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            int value = (int) propertyMap.get(key);

            switch(value) {
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

}
