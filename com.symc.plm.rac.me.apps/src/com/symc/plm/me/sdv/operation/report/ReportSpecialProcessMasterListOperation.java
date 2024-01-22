package com.symc.plm.me.sdv.operation.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.common.BOMTreeTableModel;
import com.teamcenter.rac.util.Registry;

public class ReportSpecialProcessMasterListOperation extends SimpleSDVExcelOperation {

    private Registry registry;

    private BOMTreeTableModel tableModel = null;

    private String productCode = "";
    private String processType = "";
    private String processType_kor_name = "";
    private String operationType = "";

    @Override
    public void executeOperation() throws Exception {
        try {
            registry = Registry.getRegistry(this);

            IDataSet dataSet = getData();
            if (dataSet != null) {
                String defaultFileName = productCode + "_" + registry.getString("SpecialProcessList.FileName", "SpecialProcessList") + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
            }
        } catch (Exception e) {
            setExecuteError(e);
            // MessageBox에 보여줄 메시지
            // 구현하지 않으면 default 메시지를 보여준다.
            // setErrorMessage("");
        }
    }

    @Override
    protected IDataSet getData() throws Exception {
        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

        InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
        if (component != null && component instanceof TCComponentBOPLine) {
            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOMLine topBOMLine = bomWindow.getTopBOMLine();

            // Product Code
            productCode = topBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);

            // process type
            processType = topBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE);
            if (processType.startsWith("B")) {
                operationType = SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM;
                processType_kor_name = registry.getString("body.NAME") + " ";
            } else if (processType.startsWith("P")) {
                operationType = SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM;
                processType_kor_name = registry.getString("paint.NAME") + " ";
            }

            // BOMTreeTableModel
            MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
            BOMTreeTable treeTable = application.getViewableTreeTable();
            tableModel = (BOMTreeTableModel) treeTable.getTreeTableModel();

            dataList = getChildrenList(dataList, (TCComponentBOPLine) component);
        }

        IDataSet dataSet = convertToDataSet("operationList", dataList);
        IDataMap dataMap = new RawDataMap();

        dataMap.put("processType_kor_name", processType_kor_name);
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
        String parent_type = parentLine.getItem().getType();
        if (parent_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || parent_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            BOMLineNode node = tableModel.getNode(parentLine);
            node.loadChildren();
        }

        if (parentLine.getChildrenCount() > 0) {
            AIFComponentContext[] context = parentLine.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();

                    // 미할당 Line 제외
                    if (type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                        if (SDVBOPUtilities.isAssyTempLine(childLine)) {
                            continue;
                        }
                    }

                    if (SDVTypeConstant.EBOM_MPRODUCT.equals(type) || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type)) {
                        continue;
                    }

                    if (operationType.equals(type)) {
                        if (childLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.OPERATION_REV_KPC)) {
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();

                            TCComponentBOPLine station = (TCComponentBOPLine) childLine.parent();
                            TCComponentItemRevision stationItemRevision = station.getItemRevision();

                            // 공정 NO
                            dataMap.put(SDVPropertyConstant.STATION_LINE, stationItemRevision.getProperty(SDVPropertyConstant.STATION_LINE));
                            dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, stationItemRevision.getProperty(SDVPropertyConstant.STATION_STATION_CODE));

                            // 공정명
                            dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, stationItemRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

                            // 작업 내용
                            dataMap = getEndItemList(dataMap, childLine);

                            // 설비/장비
                            dataMap = getEquipmentList(dataMap, childLine);

                            dataList.add(dataMap);
                        }
                    } else {
                        getChildrenList(dataList, (TCComponentBOPLine) context[i].getComponent());
                    }
                }
            }
        }

        return dataList;
    }

    /**
     * End Item List
     * 
     * @method getEndItem
     * @date 2013. 12. 18.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getEndItemList(HashMap<String, Object> dataMap, TCComponentBOPLine operation) throws TCException {
        List<String> endItemList = new ArrayList<String>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                String type = childLine.getItem().getType();
                if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type)) {
                    endItemList.add(childLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));
                }
            }
        }
        dataMap.put("endItemList", endItemList);

        return dataMap;
    }

    /**
     * 설비/장비(Robot, Gun) List
     * 
     * @method getEquipmentInfo
     * @date 2014. 1. 24.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getEquipmentList(HashMap<String, Object> dataMap, TCComponentBOPLine operation) throws TCException {
        int robot_count = 0;
        int gun_count = 0;

        List<TCComponentBOPLine> plantOPAreaList = getPlantOPAreaList(operation);
        for (int i = 0; i < plantOPAreaList.size(); i++) {
            TCComponentBOPLine plantOPArea = plantOPAreaList.get(i);

            if (plantOPArea.getChildrenCount() > 0) {
                AIFComponentContext[] context = plantOPArea.getChildren();
                for (int j = 0; j < context.length; j++) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[j].getComponent();
                    if (SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM.equals(childLine.getItem().getType())) {
                        robot_count++;
                    } else if (SDVTypeConstant.BOP_PROCESS_GUN_ITEM.equals(childLine.getItem().getType())) {
                        gun_count++;
                    }
                }
            }
        }

        List<String> equipmentList = new ArrayList<String>();
        if (robot_count > 0) {
            equipmentList.add("ROBOT : " + robot_count + "EA");
        }
        if (gun_count > 0) {
            equipmentList.add("GUN : " + gun_count + "EA");
        }
        if (robot_count > 0) {
            equipmentList.add("T/C : " + robot_count + "EA");
        }
        dataMap.put("equipmentList", equipmentList);

        return dataMap;
    }

    /**
     * Operation 하위 PlantOPArea List
     * 
     * @method getPlantOPAreaList
     * @date 2014. 1. 24.
     * @param
     * @return List<TCComponentBOPLine>
     * @exception
     * @throws
     * @see
     */
    private List<TCComponentBOPLine> getPlantOPAreaList(TCComponentBOPLine operation) throws TCException {
        List<TCComponentBOPLine> plantOPAreaList = new ArrayList<TCComponentBOPLine>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                if (SDVTypeConstant.PLANT_OPAREA_ITEM.equals(childLine.getItem().getType())) {
                    plantOPAreaList.add(childLine);
                }
            }
        }

        return plantOPAreaList;
    }

    private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();
        dataMap.put(dataName, dataList, IData.TABLE_FIELD);
        dataSet.addDataMap(dataName, dataMap);

        return dataSet;
    }

}
