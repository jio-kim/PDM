package com.symc.plm.me.sdv.operation.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawData;
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
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.common.BOMTreeTableModel;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.soa.client.model.Property;

public class ReportBOPOperationMasterListOperation extends SimpleSDVExcelOperation {

    private Registry registry;

    private BOMTreeTableModel tableModel = null;

    private String productCode = "";
    private String processType = "";
    private String operationType = "";
    private int[] selectedValueFromDialog;

    @Override
    public void executeOperation() throws Exception {
        try {
            registry = Registry.getRegistry(this);

            selectedValueFromDialog = (int[]) localDataMap.get("selectedValue").getValue();

            IDataSet dataSet = getData();
            if (dataSet != null) {
                String defaultFileName = productCode + "_" + registry.getString("OperationMasterList.FileName", "OperationMasterList") + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
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

        String compID = "";
        String revisionRule = "";
        String revRuleStandardDate = "";
        String variantRule = "";

        InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
        if (component != null && component instanceof TCComponentBOPLine) {
            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOMLine topBOMLine = bomWindow.getTopBOMLine();
            TCComponentBOPLine comp = (TCComponentBOPLine) component;

            // product Code
            productCode = topBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            // shop or line or station - id
            compID = comp.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

            // process type
            processType = topBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE);
            if (processType.startsWith("B")) {
                operationType = SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM;
            } else if (processType.startsWith("P")) {
                operationType = SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM;
            } else if (processType.startsWith("A")) {
                operationType = SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM;
            }

            // Revision Rule
            TCComponentRevisionRule bomWindowRevisionRule = bomWindow.getRevisionRule();
            revisionRule = bomWindowRevisionRule.toString();

            // Revision Rule 기준일
            Date rule_date = bomWindowRevisionRule.getDateProperty("rule_date");
            if (rule_date != null) {
                revRuleStandardDate = new SimpleDateFormat("yyyy-MM-dd").format(rule_date);
            } else {
                revRuleStandardDate = ExcelTemplateHelper.getToday("yyyy-MM-dd");
            }

            // Variant
            variantRule = SDVBOPUtilities.getBOMConfiguredVariantSetToString(bomWindow);

            // BOMTreeTableModel
            MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
            BOMTreeTable treeTable = application.getViewableTreeTable();
            tableModel = (BOMTreeTableModel) treeTable.getTreeTableModel();

            dataList = getChildrenList(dataList, (TCComponentBOPLine) component);
        }

        IDataSet dataSet = convertToDataSet("operationList", dataList);
        IDataMap dataMap = new RawDataMap();
        IData data = new RawData();

        data.setValue(selectedValueFromDialog);
        dataMap.put("selectedValueFromDialog", data);

        dataMap.put("productCode", productCode);
        dataMap.put("processType", processType);
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
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();

                        // 공법(작업표준서)
                        dataMap = getOperationInfo(childLine, dataMap);

                        // 시간 정보
                        dataMap = getTimeInfo(childLine, dataMap);

                        // 공정 정보
                        dataMap = getStationInfo(childLine, dataMap);

                        // 공구 정보
                        dataMap = getToolList(childLine, dataMap);

                        // 설비 정보
                        dataMap = getEquipmentList(childLine, dataMap);

                        // 기타 정보
                        dataMap = getETCInfo(childLine, dataMap);

                        // User 선택 옵션
                        if (selectedValueFromDialog.length > 0) {
                            for (int num : selectedValueFromDialog) {
                                if (num == 1) {
                                    // 작업 정보
                                    dataMap = getWorkInfoList(childLine, dataMap);
                                } else if (num == 2) {
                                    // End Item
                                    dataMap = getEndItemList(childLine, dataMap);
                                } else if (num == 3) {
                                    // 부자재 정보
                                    dataMap = getSubsidiaryList(childLine, dataMap);
                                }
                            }
                        }

                        dataList.add(dataMap);
                    } else {
                        getChildrenList(dataList, (TCComponentBOPLine) context[i].getComponent());
                    }
                }
            }
        }

        return dataList;
    }

    /**
     * 공법(작업표준서)
     * 
     * @method getOperationInfo
     * @date 2014. 2. 5.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getOperationInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        // 공법(작업표준서) NO.
        dataMap.put(SDVPropertyConstant.BL_ITEM_ID, operation.getProperty(SDVPropertyConstant.BL_ITEM_ID));

        // 공법 REV.
        dataMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, operation.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));

        // 공법명
        dataMap.put(SDVPropertyConstant.BL_OBJECT_NAME, operation.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

        // 공법 영문명
        System.out.println("operation = "+operation);
        String a = operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
        
        dataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME));

        // option code, option description
        HashMap<String, Object> option = SDVBOPUtilities.getVariant(operation.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
        dataMap.put("optionCode", option.get("printValues"));
        dataMap.put("optionDescription", option.get("printDescriptions"));

        // DR
        dataMap = getDRProperty(operation, dataMap);

        // KPC/특수공정
        if (processType.startsWith("A")) {
            // KPC(조립)
            dataMap = getKPCProperty(operation, dataMap);
        } else {
            // 특수공정(차체/도장)
            dataMap.put("specialStation", operation.getItemRevision().getLogicalProperty(SDVPropertyConstant.OPERATION_REV_KPC) ? "Y" : "");
        }
        
        // 특별 특성 속성 추가
        String specialCharacter = operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
        dataMap.put("specialCharacterristic",  specialCharacter == null ? "" : specialCharacter );

        return dataMap;
    }

    /**
     * DR 속성 End Item DR 속성 우선( DR1 > DR2 > DR3 ) End Item DR 속성이 없으면 공법 DR 속성
     * 
     * @method getDRProperty
     * @date 2013. 11. 11.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getDRProperty(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        // End Item DR 속성 우선( DR1 > DR2 > DR3 )
        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type)) {
                        String dr = childLine.getItemRevision().getProperty("s7_REGULATION");
                        if (!dr.equals(".") && !dr.equals("")) {
                            if (dataMap.get("drProperty") == null) {
                                dataMap.put("drProperty", dr);
                            } else {
                                if (dr.compareToIgnoreCase((String) dataMap.get("drProperty")) < 0) {
                                    dataMap.put("drProperty", dr);
                                }
                            }
                        }
                    }
                }
            }
        }

        // End Item DR 속성이 없으면 공법 DR 속성
        if (dataMap.get("drProperty") == null) {
            dataMap.put("drProperty", operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_DR));
        }

        return dataMap;
    }

    /**
     * 공법 Activity 중 KPC 속성이 있을 경우 Y로 표시(조립)
     * 
     * @method getKPCProperty
     * @date 2013. 11. 11.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getKPCProperty(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) operation;
        TCComponent root = bvrOperation.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);
        if (root != null) {
            if (root instanceof TCComponentCfgActivityLine) {
                TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                if (children != null) {
                    for (TCComponent child : children) {
                        String activityControlPoint = child.getProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT);
                        // KPC 내용 추가로 인한 수정
                        String activityControlBasis = child.getProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS);
                        //////////////////////////////////////////////////////////////////////////////////////////////////
                        /*
                         * "KPC내용" 출력 내용 변경
                         *  AL_CONTROL_POINT 값이 있고 "TR추적" 이 아니면 출력
                         */
//                        if (!activityControlPoint.equals("") || !activityControlBasis.equals("")) {
//                            dataMap.put("isExistKPC", "Y");
//                        // KPC 내용 추가로 인한 수정
//                            dataMap.put("kpcContents", activityControlPoint + ":" + activityControlBasis);
//                            break;
//                        }
                        if (!activityControlPoint.equals("") || !activityControlPoint.equals("TR추적")) {
                        	dataMap.put("isExistKPC", "Y");
                        	// KPC 내용 추가로 인한 수정
                        	 if(activityControlPoint.length() > 2) {
                       		  activityControlPoint = activityControlPoint.substring(2);
                       	  	}
                        	dataMap.put("kpcContents", activityControlPoint + ":" + activityControlBasis);
                        	break;
                        }
                        ///////////////////////////////////////////////////////////////////////////////////////////////////
                    }
                }
            }
        }

        return dataMap;
    }

    /**
     * 시간 정보
     * 
     * @method getTimeInfo
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getTimeInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        Double time1 = 0.0;
        Double time2 = 0.0;
        Double time3 = 0.0;
        Double time4 = 0.0;
        Double time5 = 0.0;
        Double cycleTime = 0.0;
        Double allowance = 0.0;

        // 부대 계수 - Line 우선
        TCComponentBOPLine shop = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
        TCComponentBOPLine line = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);

        if (shop != null) {
            String shop_allowance = shop.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_ALLOWANCE);
            if (!shop_allowance.trim().equals("")) {
                allowance = Double.valueOf(shop_allowance);
            }
        }

        if (line != null) {
            String line_allowance = line.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_ALLOWANCE);
            if (!line_allowance.trim().equals("")) {
                allowance = Double.valueOf(line_allowance);
            }
        }

        // Activity 시간
        TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) operation;
        TCComponent root = bvrOperation.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);
        if (root != null) {
            if (root instanceof TCComponentCfgActivityLine) {
                TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                if (children != null) {
                    for (TCComponent child : children) {
                        String activity_system_category = child.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).getStringValue();
                        // 작업자 정미 시간
                        if (activity_system_category.equals("01")) {
                            time1 += (child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME) * child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY));
                        }
                        // 자동 시간
                        else if (activity_system_category.equals("02")) {
                            time2 += (child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME) * child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY));
                        }
                        // 보조 시간
                        else if (activity_system_category.equals("03")) {
                            time3 += (child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME) * child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY));
                        }
                    }

                    // [SR140905-044][20140918] bykim, 시간 계산식이 잘못되어 수정
                    // 표준시간(작업자 정미 시간 * 부대 계수)
                    time4 = time1 * allowance;

                    // 작업 시간(표준 시간 + 보조 시간)
                    time5 = time3 + time4;

                    // cycle time(자동 시간 + 작업자 정미 시간)
                    cycleTime = time1 + time2;
                }
            }
        }

        // 편성 시간(최대)
        if (operation.getItem().getLogicalProperty(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK)) {
            dataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, ((Double) (Math.ceil(time5 * 10.0) / 10.0)).toString());
        } else {
            dataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, "");
        }

        // 편성 시간(대표차종)
        if (operation.getItem().getLogicalProperty(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK)) {
            dataMap.put(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, ((Double) (Math.ceil(time5 * 10.0) / 10.0)).toString());
        } else {
            dataMap.put(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, "");
        }

        dataMap.put("time1", Math.ceil(time1 * 10.0) / 10.0);
        dataMap.put("time2", Math.ceil(time2 * 10.0) / 10.0);
        dataMap.put("time3", Math.ceil(time3 * 10.0) / 10.0);
        dataMap.put("time4", Math.ceil(time4 * 10.0) / 10.0);
        dataMap.put("time5", Math.ceil(time5 * 10.0) / 10.0);
        dataMap.put("cycleTime", Math.ceil(cycleTime * 10.0) / 10.0);
        dataMap.put("allowance", allowance);

        return dataMap;
    }

    /**
     * 공정 정보
     * 
     * @method getStationInfo
     * @date 2014. 2. 5.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getStationInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        // Line Code, Line Revision
        TCComponentBOPLine line = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
        if (line != null) {
            dataMap.put(SDVPropertyConstant.LINE_REV_CODE, line.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
            dataMap.put("line_revision", line.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
        }

        if (processType.startsWith("B") || processType.startsWith("P")) {
            // Station Code, Station Revision
            TCComponentBOPLine station = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
            if (station != null) {
                dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, station.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE) + "-" + station.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE));
                dataMap.put("station_revision", station.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
            }
        } else {
            // Station No(조립)
            dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, operation.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));
        }

        // 작업자, 작업순, 위치
        TCComponentItem operation_item = operation.getItem();
        dataMap.put(SDVPropertyConstant.OPERATION_WORKER_CODE, operation_item.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE));
        dataMap.put(SDVPropertyConstant.OPERATION_PROCESS_SEQ, operation_item.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ));
        dataMap.put(SDVPropertyConstant.OPERATION_WORKAREA, operation_item.getProperty(SDVPropertyConstant.OPERATION_WORKAREA));

        return dataMap;
    }

    /**
     * 공구 정보
     * 
     * @method getToolList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getToolList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> toolList = new ArrayList<HashMap<String, Object>>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    if (SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(childLine.getItem().getType())) {
                        HashMap<String, Object> toolMap = new HashMap<String, Object>();

                        // 공구 ID
                        toolMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

                        // 공구 영문명
                        toolMap.put(SDVPropertyConstant.TOOL_ENG_NAME, childLine.getItemRevision().getProperty(SDVPropertyConstant.TOOL_ENG_NAME));

                        // 공구 스펙
                        toolMap.put(SDVPropertyConstant.TOOL_SPEC_ENG, childLine.getItemRevision().getProperty(SDVPropertyConstant.TOOL_SPEC_ENG));

                        // TORQUE
                        toolMap.put(SDVPropertyConstant.BL_NOTE_TORQUE, childLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE));
                        toolMap.put(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE, childLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE));

                        // 공구 수량 - 정수로 표시
                        String quantity = childLine.getProperty(SDVPropertyConstant.BL_QUANTITY).split("\\.")[0];
                        toolMap.put(SDVPropertyConstant.BL_QUANTITY, quantity);

                        toolList.add(toolMap);
                    }
                }
            }
        }
        dataMap.put("toolList", toolList);

        return dataMap;
    }

    /**
     * 설비 정보
     * 
     * @method getEquipmentList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getEquipmentList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> equipmentList = new ArrayList<HashMap<String, Object>>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if (SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_GUN_ITEM.equals(type)) {
                        HashMap<String, Object> equipmentMap = new HashMap<String, Object>();

                        // 설비 ID
                        equipmentMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

                        // 설비명
                        equipmentMap.put(SDVPropertyConstant.BL_OBJECT_NAME, childLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

                        // 설비 스펙/목적
                        equipmentMap.put(SDVPropertyConstant.EQUIP_SPEC_ENG, childLine.getItemRevision().getProperty(SDVPropertyConstant.EQUIP_SPEC_ENG));
                        equipmentMap.put(SDVPropertyConstant.EQUIP_PURPOSE_ENG, childLine.getItemRevision().getProperty(SDVPropertyConstant.EQUIP_PURPOSE_ENG));

                        // 설비 수량 - 정수로 표시
                        String quantity = childLine.getProperty(SDVPropertyConstant.BL_QUANTITY).split("\\.")[0];
                        equipmentMap.put(SDVPropertyConstant.BL_QUANTITY, quantity);

                        equipmentList.add(equipmentMap);
                    }
                }
            }
        }
        dataMap.put("equipmentList", equipmentList);

        return dataMap;
    }

    /**
     * 기타 정보
     * 
     * @method getETCInfo
     * @date 2014. 2. 5.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getETCInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        // 조립 시스템
        dataMap.put(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM));

        // 생산 담당자(공법 owner 이름)
        String owning_user = operation.getProperty(SDVPropertyConstant.BL_OWNING_USER);
        dataMap.put(SDVPropertyConstant.BL_OWNING_USER, getNameProperty(owning_user));

        // INSTL DWG NO
        dataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO));
        
        /*
         * 수정점 : 20200110
         * [CF196] 속성 추가 요청 "m7_P_FMEA_NO", "m7_CP_NO"
         * 데이터 속성 추출 후 dataMap 에 입력
         */
        // 위 속성이 Item 속성이라 BOMLine 의 아이템을 가져옴
        
        if( this.processType.startsWith("A")) {
        	TCComponentItem opItem = (TCComponentItem)operation.getItem();
        	String pFmeaNo = "";
        	String cpNo = "";
        	
        	pFmeaNo = opItem.getProperty(SDVPropertyConstant.OPERATION_ITEM_P_MEFA_NO) == null ? "" : opItem.getProperty(SDVPropertyConstant.OPERATION_ITEM_P_MEFA_NO) ;
        	cpNo = opItem.getProperty(SDVPropertyConstant.OPERATION_ITEM_CP_NO) == null ? "" :opItem.getProperty(SDVPropertyConstant.OPERATION_ITEM_CP_NO);
        	
	        	if( pFmeaNo.equals("Y") || cpNo.equals("Y") ) {
	        		String projectCode = "";
					String functionCode = "";
					String shopCode = "";
					InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
			        if (component != null && component instanceof TCComponentBOPLine) {
			            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
			            TCComponentBOMLine topBOMLine = bomWindow.getTopBOMLine();
			            // shop Code
			            shopCode = topBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
			            TCComponentItemRevision  mecoRevision = (TCComponentItemRevision)operation.getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
			            projectCode = mecoRevision.getProperty(SDVPropertyConstant.MECO_REV_PROJECT_CODE);
			            projectCode = reNameProjectCode(projectCode);
			            functionCode =operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
					
					  if(((String)pFmeaNo).equals("Y")) {
						  dataMap.put(SDVPropertyConstant.OPERATION_ITEM_P_MEFA_NO,  "PF" + "-" + projectCode + "-" + shopCode + "-" + "F" + functionCode + "-" + "XX");
					  } else {
						  dataMap.put(SDVPropertyConstant.OPERATION_ITEM_P_MEFA_NO, "");
					  } 
					  
					  if(((String)cpNo).equals("Y")) {
						  dataMap.put(SDVPropertyConstant.OPERATION_ITEM_CP_NO,  "CP" + "-" + projectCode + "-" + shopCode + "-" + "F" + functionCode + "-" + "XX");
					  } else {
						  dataMap.put(SDVPropertyConstant.OPERATION_ITEM_CP_NO, "");
					  }
	        	}
	        	
	        }
        }
        

        return dataMap;
    }

    /**
     * 작업 정보
     * 
     * @method getWorkInfoList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getWorkInfoList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> workInfoList = new ArrayList<HashMap<String, Object>>();

        TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) operation;
        TCComponent root = bvrOperation.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);
        if (root != null) {
            if (root instanceof TCComponentCfgActivityLine) {
                TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                if (children != null) {
                    for (TCComponent child : children) {
                        HashMap<String, Object> workInfoMap = new HashMap<String, Object>();

                        workInfoMap.put("workCode", child.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE));
                        workInfoMap.put("workInfo", child.getProperty(SDVPropertyConstant.ACTIVITY_OBJECT_NAME));

                        if (processType.startsWith("A")) {
                            String time_system_frequency = child.getProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
                            if (!"1".equals(time_system_frequency)) {
                                workInfoMap.put("workCode", child.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE) + "X" + time_system_frequency);
                            }
                        }

                        workInfoList.add(workInfoMap);
                    }
                    dataMap.put("workInfoList", workInfoList);
                }
            }
        }

        return dataMap;
    }

    /**
     * End Item
     * 
     * [SR140828-014][20140827] shcho, BOPLine의 Option 정보를 가져오는 속성을 BL_OCC_MVL_CONDITION 에서 BL_VARIANT_CONDITION으로 변경 (이유 : Copy&Paste로 할당된 End Item의 경우 BL_OCC_MVL_CONDITION에 값이 없다.)
     * 
     * @method getEndItemList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getEndItemList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> endItemList = new ArrayList<HashMap<String, Object>>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type)) {
                        HashMap<String, Object> endItemMap = new HashMap<String, Object>();

                        // PART NO
                        endItemMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

                        // PART 명
                        endItemMap.put(SDVPropertyConstant.BL_OBJECT_NAME, childLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

                        // 수량 - 정수로 표시
                        String quantity = childLine.getProperty(SDVPropertyConstant.BL_QUANTITY).split("\\.")[0];
                        endItemMap.put(SDVPropertyConstant.BL_QUANTITY, quantity);

                        // End Item 옵션이 없으면 공법의 옵션 적용
                        String endItem_option = childLine.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                        if ("".equals(endItem_option)) {
                            String operation_option = operation.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
                            if ("".equals(operation_option)) {
                                endItemMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, "공통");
                            } else {
                                endItemMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(operation_option).get("printValues"));
                            }
                        } else {
                            endItemMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(endItem_option).get("printValues"));
                        }

                        endItemList.add(endItemMap);

                        // Wiring Harness는 최대 20개만 출력(설계서)
                        // Wiring Harness와 관계없이 End Item은 최대 20개만 출력(2013-12-12)
                        if (endItemList.size() == 20) {
                            break;
                        }
                    }
                }
            }
        }
        dataMap.put("endItemList", endItemList);

        return dataMap;
    }

    /**
     * 부자재
     * 
     * @method getSubsidiaryList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getSubsidiaryList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> subsidiaryList = new ArrayList<HashMap<String, Object>>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    if (SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM.equals(childLine.getItem().getType())) {
                        HashMap<String, Object> subsidiaryMap = new HashMap<String, Object>();

                        // 부자재 NO
                        subsidiaryMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

                        // 부자재 명
                        subsidiaryMap.put(SDVPropertyConstant.BL_OBJECT_NAME, childLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

                        // 부자재 영문명
                        subsidiaryMap.put(SDVPropertyConstant.SUBSIDIARY_ENG_NAME, childLine.getItemRevision().getProperty(SDVPropertyConstant.SUBSIDIARY_ENG_NAME));

                        // 단위
                        subsidiaryMap.put(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT, childLine.getProperty(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT));

                        // 소요량
                        subsidiaryMap.put(SDVPropertyConstant.SUB_SUBSIDIARY_QTY, childLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY));

                        // 부자재 옵션이 없으면 공법의 옵션 적용
                        String subsidiary_option = childLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
                        if ("".equals(subsidiary_option)) {
                            String operation_option = operation.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION).trim();
                            if ("".equals(operation_option)) {
                                subsidiaryMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, "공통");
                            } else {
                                subsidiaryMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(operation_option).get("printValues"));
                            }
                        } else {
                            subsidiaryMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(subsidiary_option).get("printValues"));
                        }

                        subsidiaryList.add(subsidiaryMap);
                    }
                }
            }
        }
        dataMap.put("subsidiaryList", subsidiaryList);

        return dataMap;
    }

    /**
     * 부모 TCComponentBOPLine return
     * 
     * @method getParentBOPLine
     * @date 2014. 2. 5.
     * @param
     * @return TCComponentBOPLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOPLine getParentBOPLine(TCComponentBOPLine bopLine, String itemType) throws TCException {
        TCComponentBOPLine parentBOPLine = null;

        if (bopLine.parent() != null) {
            parentBOPLine = (TCComponentBOPLine) bopLine.parent();
            if (!parentBOPLine.getItem().getType().equals(itemType)) {
                return getParentBOPLine(parentBOPLine, itemType);
            }
        }

        return parentBOPLine;
    }

    /**
     * owning_user 값에서 이름만 가져오기 ex) BOPADM (bopadm) -> BOPADM
     * 
     * @method getNameProperty
     * @date 2013. 11. 18.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getNameProperty(String owning_user) {
        String name = owning_user.split(" ")[0];

        return name;
    }

    private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();
        dataMap.put(dataName, dataList, IData.TABLE_FIELD);
        dataSet.addDataMap(dataName, dataMap);

        return dataSet;
    }
    
	/**
	 * 수정점 : [CF-196]20200114
	 * ProjectCode Name 변경 로직 
	 * Ex) X100, X150, X151 -> X150
	 * 	   C300, C301 		-> C300  으로 변경 추가로 변경 항목 가능
	 * @param projectCode
	 * @return
	 */
	private String reNameProjectCode(String projectCode) {
		String projectCodeRename = "";
		if( projectCode.startsWith("X1")) {
			projectCodeRename = "X150";
		}
		
		if( projectCode.startsWith("C3")) {
			projectCodeRename = "C300";
		}
		
		return projectCodeRename;
	}

}
