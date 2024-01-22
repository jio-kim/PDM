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
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVUtils;
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
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.common.BOMTreeTableModel;
import com.teamcenter.rac.util.Registry;

public class ReportEquipmentMasterListOperation extends SimpleSDVExcelOperation {

    private Registry registry;

    private BOMTreeTableModel tableModel = null;

    private String productCode = "";
    private String processType = "";
    private String operationType = "";

    @Override
    public void executeOperation() throws Exception {
        try {
            registry = Registry.getRegistry(this);

            IDataSet dataSet = getData();
            if (dataSet != null) {
                String defaultFileName = productCode + "_" + registry.getString("EquipmentList.FileName", "EquipmentList") + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
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
        dataMap.put("productCode", productCode);
        dataMap.put("compID", compID);
        dataMap.put("revisionRule", revisionRule);
        dataMap.put("revRuleStandardDate", revRuleStandardDate);
        dataMap.put("variantRule", variantRule);
        dataMap.put("excelExportDate", ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm"));
        dataMap.put("operationType", operationType);
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

                    if (SDVTypeConstant.EBOM_MPRODUCT.equals(type) || SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type) || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type)) {
                        continue;
                    }

                    if (SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_GUN_ITEM.equals(type)) {
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
                        TCComponentItemRevision equipItemRevision = childLine.getItemRevision();

                        // Shop Code
                        TCComponentBOPLine shop = getParentBOPLine(childLine, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
                        if (shop != null) {
                            dataMap.put(SDVPropertyConstant.SHOP_REV_SHOP_CODE, shop.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE));
                        }

                        // Line Code, Line Revision
                        TCComponentBOPLine line = getParentBOPLine(childLine, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
                        if (line != null) {
                            dataMap.put(SDVPropertyConstant.LINE_REV_CODE, line.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
                            dataMap.put("line_revision", line.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
                        }

                        // Station Code, Station Revision
                        if (processType.startsWith("B") || processType.startsWith("P")) {
                            TCComponentBOPLine station = getParentBOPLine(childLine, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                            if (station != null) {
                                dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, station.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE) + "-" + station.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE));
                                dataMap.put("station_revision", station.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
                            }
                        }

                        // 공법(작업표준서) NO, 공법 Revision, 담당자
                        TCComponentBOPLine operation = getParentBOPLine(childLine, operationType);
                        if (operation != null) {
                            dataMap.put("operation_id", operation.getProperty(SDVPropertyConstant.BL_ITEM_ID));
                            dataMap.put("operation_revision", operation.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
                            dataMap.put("operation_owning_user", getNameProperty(operation.getProperty(SDVPropertyConstant.BL_OWNING_USER)));

                            // 조립
                            if (processType.startsWith("A")) {
                                dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));
                            }
                        }

                        // 설비 Code
                        dataMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

                        // Type
                        dataMap = getDescriptionOfMainClassProp(equipItemRevision, dataMap);

                        // 설비명
                        //20201110 seho 설비명을 리비전 이름으로 변경.
                        dataMap.put(SDVPropertyConstant.BL_OBJECT_NAME, childLine.getProperty(SDVPropertyConstant.BL_REV_OBJECT_NAME));
//                        dataMap.put(SDVPropertyConstant.BL_OBJECT_NAME, childLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

                        // Cad Data 표시
                        dataMap = getCadData(equipItemRevision, dataMap);

                        // 주요사양(규격)
                        dataMap.put(SDVPropertyConstant.EQUIP_SPEC_ENG, equipItemRevision.getProperty(SDVPropertyConstant.EQUIP_SPEC_ENG));

                        // 수량 - 정수로 표시
                        String quantity = childLine.getProperty(SDVPropertyConstant.BL_QUANTITY).split("\\.")[0];
                        dataMap.put(SDVPropertyConstant.BL_QUANTITY, quantity);

                        // 생산능력
                        dataMap.put(SDVPropertyConstant.EQUIP_CAPACITY, equipItemRevision.getProperty(SDVPropertyConstant.EQUIP_CAPACITY));

                        // 용도
                        dataMap.put(SDVPropertyConstant.EQUIP_PURPOSE_ENG, equipItemRevision.getProperty(SDVPropertyConstant.EQUIP_PURPOSE_ENG));

                        // 설치년도
                        dataMap.put(SDVPropertyConstant.EQUIP_INSTALL_YEAR, equipItemRevision.getProperty(SDVPropertyConstant.EQUIP_INSTALL_YEAR));

                        // MAKER
                        dataMap.put(SDVPropertyConstant.EQUIP_MAKER, equipItemRevision.getProperty(SDVPropertyConstant.EQUIP_MAKER));

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
     * SDVPropertyConstant.BL_EQUIP_RESOURCE_CATEGORY SDVPropertyConstant.BL_EQUIP_MAIN_CLASS 위 속성 값으로 LOV Description 가져오기
     * 
     * @method getDescriptionOfMainClassProp
     * @date 2013. 11. 21.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getDescriptionOfMainClassProp(TCComponentItemRevision equipItemRevision, HashMap<String, Object> dataMap) throws TCException {
        String resource_category = equipItemRevision.getProperty(SDVPropertyConstant.EQUIP_RESOURCE_CATEGORY).toUpperCase();
        String main_class = equipItemRevision.getProperty(SDVPropertyConstant.EQUIP_MAIN_CLASS).toUpperCase();
        String lovName = "M7_" + processType.substring(0, 1) + "_EQUIP_" + resource_category;
        String lovValueDescription = "";

        if (!"".equals(resource_category) && !"".equals(main_class)) {
            lovValueDescription = SDVLOVUtils.getLovValueDesciption(lovName, main_class);
        }

        dataMap.put(SDVPropertyConstant.EQUIP_MAIN_CLASS, lovValueDescription);

        return dataMap;
    }

    /**
     * 설비 하위 Dataset 중 2D/3D 도면 Type 표시
     * 
     * @method setCadData
     * @date 2013. 10. 29.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getCadData(TCComponentItemRevision equipItemRevision, HashMap<String, Object> dataMap) throws TCException {
        AIFComponentContext[] context = equipItemRevision.getChildren();

        for (int i = 0; i < context.length; i++) {
            if (context[i].getComponent() instanceof TCComponentDataset) {
                String type = context[i].getComponent().getType();
                if (type.equals("CATPart") || type.equals("CATDrawing")) {
                    dataMap.put("cad", "●");
                } else if (type.equals("CATCache")) {
                    dataMap.put("cgr", "●");
                } else if (type.equals("DirectModel")) {
                    dataMap.put("jt", "●");
                } else if (type.equals("Zip")) {
                    dataMap.put("etc", "●");
                }
            }
        }

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

}
