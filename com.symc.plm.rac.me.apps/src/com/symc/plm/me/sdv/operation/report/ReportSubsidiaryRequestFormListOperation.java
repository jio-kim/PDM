package com.symc.plm.me.sdv.operation.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawData;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class ReportSubsidiaryRequestFormListOperation extends SimpleSDVExcelOperation {

    private static final String SUBSIDIRARY_REQ_FORM_FILE_NAME_FORMAT = "%s_SubsidiaryRequestFormList_%tY%tm%td";
    private String productCode = "";
    private String processType = "";
    private String operationType = "";

    private TCComponent targetComponent;

    public ReportSubsidiaryRequestFormListOperation() {
    }

    /**
     * 
     * 
     * @method executeOperation
     * @date 2013. 10. 28.
     * @param
     * @return void
     * @exception
     * @throws Exception
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            MFGLegacyApplication mfgApllication = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            this.targetComponent = (TCComponent) mfgApllication.getTargetComponent();
            if (targetComponent != null && targetComponent instanceof TCComponentBOPLine) {
                IDataSet dataSet = getData();
                if (dataSet != null) {
                    productCode = (String) dataSet.getData("SubsidiaryListTargetProduct");
                    Date today = new Date();
                    String defaultFileName = String.format(SUBSIDIRARY_REQ_FORM_FILE_NAME_FORMAT, productCode, today, today, today); // ExcelTemplateHelper.getToday("yyyyMMdd"));
                    transformer.print(mode, templatePreference, defaultFileName, dataSet);
                }
            } else {
                throw new TCException("Not found selected Shop Item Component.!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            setErrorMessage(e.toString());
            setExecuteResult(FAIL);
            // MessageBox에 보여줄 메시지
        }
    }

    /**
     * 
     * 
     * @method getData
     * @date 2013. 10. 28.
     * @param
     * @return IDataSet
     * @throws Exception
     * @exception
     * @throws TCException
     * @see
     */
    @Override
    protected IDataSet getData() throws Exception {

        String targetComponentName = null;

        if (targetComponent == null)
            return null;

        TCComponentBOPLine comp = (TCComponentBOPLine) targetComponent;
        // 선택한 아이템의 이름을 가져온다.
        targetComponentName = comp.getItemRevision().getObjectString();

        HashMap<String, ArrayList<HashMap<String, Object>>> subsidiaryListMap = new HashMap<String, ArrayList<HashMap<String, Object>>>();

        // product Code
        productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);

        // process type
        processType = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE);

        // operation type(차체(B), 도장(P), 조립(A))
        if (processType.startsWith("B")) {
            operationType = SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM;
        } else if (processType.startsWith("P")) {
            operationType = SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM;
        } else if (processType.startsWith("A")) {
            operationType = SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM;
        }

        subsidiaryListMap = getChildrenList(subsidiaryListMap, (TCComponentBOPLine) targetComponent);
        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();
        IData data = new RawData();
        data.setValue(subsidiaryListMap);

        dataMap.put("SubsidiaryListTargetName", targetComponentName);
        dataMap.put("SubsidiaryListTargetProduct", productCode);
        dataMap.put("operationType", operationType);
        dataMap.put("SubsidiaryListMap", data);
        dataSet.addDataMap("SubsidiaryListMap", dataMap);

        return dataSet;
    }

    /**
     * 
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
    private HashMap<String, ArrayList<HashMap<String, Object>>> getChildrenList(HashMap<String, ArrayList<HashMap<String, Object>>> subsidiaryListMap, TCComponentBOPLine parentLine) throws Exception {
        if (parentLine.getChildrenCount() > 0) {
            AIFComponentContext[] context = parentLine.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();
                    TCComponentItemRevision subItemRevision = childLine.getItemRevision();

                    if (SDVBOPUtilities.isAssyTempLine(childLine) == true) {
                        continue;
                    }

                    if (SDVTypeConstant.EBOM_MPRODUCT.equals(type) || SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type) || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type)) {
                        continue;
                    }

                    if (SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM.equals(type)) {
                        String subId = childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                        ArrayList<HashMap<String, Object>> list = null;
                        HashMap<String, Object> dataMap = null;
                        if (!subsidiaryListMap.containsKey(subId)) {
                            list = new ArrayList<HashMap<String, Object>>();
                            dataMap = new HashMap<String, Object>();
                            if (subItemRevision == null) {
                                break;
                            } else {
                                dataMap = getSubsidiaryInfo(childLine);
                            }

                            if (processType.startsWith("P")) {
                                if (childLine.parent() != null) {
                                    TCComponentBOPLine parent = (TCComponentBOPLine) childLine.parent();
                                    if (parent.parent().parent().getItem().getType().equals("M7_BOPLine")) {
                                        String paintLine = parent.parent().getProperty(SDVPropertyConstant.BL_ITEM_ID);
                                        if (paintLine != null && !paintLine.isEmpty()) {
                                            String paintLineCode = paintLine.substring(paintLine.length() - 2);
                                            if (paintLineCode.equals("00")) {
                                                list.add(dataMap);
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                list.add(dataMap);
                            }
                            subsidiaryListMap.put(subId, list);
                        } else {
                            list = subsidiaryListMap.get(subId);
                            if (subItemRevision == null) {
                                break;
                            } else {
                                dataMap = getSubsidiaryInfo(childLine);
                            }
                            boolean flag = false;
                            for (int j = 0; j < list.size(); j++) {
                                String operationId = (String) list.get(j).get(operationType + SDVPropertyConstant.BL_ITEM_ID);
                                String option = (String) list.get(j).get(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
                                if (operationId.equals(dataMap.get(operationType + SDVPropertyConstant.BL_ITEM_ID))) {
                                    if (option.equals(dataMap.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION))) {
                                        String strAmount = (String) list.get(j).get(SDVPropertyConstant.BL_QUANTITY);
                                        String strAmount2 = (String) dataMap.get(SDVPropertyConstant.BL_QUANTITY);
                                        Double amount = 0.0;
                                        Double camount = 0.0;
                                        if (strAmount != null && !"".equals(strAmount)) {
                                            amount = Double.parseDouble(strAmount);
                                        }
                                        if (strAmount2 != null && !"".equals(strAmount2)) {
                                            camount = Double.parseDouble(strAmount2);
                                        }
                                        list.get(j).put(SDVPropertyConstant.BL_QUANTITY, Double.toString(amount + camount));
                                        flag = true;
                                        break;

                                    }
                                }
                            }
                            if (!flag) {
                                if (processType.startsWith("P")) {
                                    if (childLine.parent() != null) {
                                        TCComponentBOPLine parent = (TCComponentBOPLine) childLine.parent();
                                        if (parent.parent().parent().getItem().getType().equals("M7_BOPLine")) {
                                            String paintLine = parent.parent().getProperty(SDVPropertyConstant.BL_ITEM_ID);
                                            if (paintLine != null && !paintLine.isEmpty()) {
                                                String paintLineCode = paintLine.substring(paintLine.length() - 2);
                                                if (paintLineCode.equals("00")) {
                                                    list.add(dataMap);
                                                } else {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    list.add(dataMap);
                                }
                            }
                        }

                    } else {
                        subsidiaryListMap = getChildrenList(subsidiaryListMap, (TCComponentBOPLine) context[i].getComponent());
                    }
                }
            }

        }

        return subsidiaryListMap;
    }

    /**
     * 
     * 
     * @method getSubsidiaryInfo
     * @date 2013. 10. 28.
     * @param childLine
     * @return HashMap<String, Object>
     * @exception TCException
     * @throws
     * @see
     */
    private HashMap<String, Object> getSubsidiaryInfo(TCComponentBOPLine childLine) throws TCException {

        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        TCComponentItemRevision subItemRevision = childLine.getItemRevision();
        String subsidiaryQty = childLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY);
        dataMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
        dataMap.put(SDVPropertyConstant.BL_OBJECT_NAME, childLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));
        dataMap.put(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR, subItemRevision.getProperty(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR));
        dataMap.put(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT, subItemRevision.getProperty(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT));
        dataMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(childLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).get("printValues"));
        dataMap.put("description", SDVBOPUtilities.getVariant(childLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).get("printDescriptions"));
        dataMap.put("quantity", SDVStringUtiles.getDoubleFromString(subsidiaryQty));
        dataMap = getProperty(dataMap);

        String subsidiaryGroup = subItemRevision.getProperty(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP);
        if (subsidiaryGroup.equals("910")) {
            dataMap.put(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP, 0);
        } else if (subsidiaryGroup.equals("920") || subsidiaryGroup.equals("930")) {
            dataMap.put(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP, 1);
        } else if (subsidiaryGroup.equals("940")) {
            dataMap.put(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP, 2);
        }
        // 부모 정보
        // Shop Code
        TCComponentBOPLine shop = getParentBOPLine(childLine, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
        if (shop != null) {
            dataMap.put(SDVPropertyConstant.SHOP_REV_SHOP_CODE, shop.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE));
        }

        // Line Code, Line Revision
        TCComponentBOPLine line = getParentBOPLine(childLine, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
        if (line != null) {
            dataMap.put(SDVPropertyConstant.LINE_REV_CODE, line.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
        }

        // Station Code, Station Revision
        if (processType.startsWith("B") || processType.startsWith("P")) {
            TCComponentBOPLine station = getParentBOPLine(childLine, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
            if (station != null) {
                dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, station.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE) + "-" + station.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE));
            }
        }

        // 공법(작업표준서) NO, 공법 Revision
        TCComponentBOPLine operation = getParentBOPLine(childLine, operationType);
        if (operation != null) {
            String operation_id = childLine.parent().getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String operation_rev = childLine.parent().getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
            dataMap.put(operationType + SDVPropertyConstant.BL_ITEM_ID, operation_id);
            dataMap.put(operationType + SDVPropertyConstant.BL_ITEM_REV_ID, operation_rev);
            dataMap.put(operationType + SDVPropertyConstant.BL_OBJECT_NAME, childLine.parent().getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

            // 조립
            if (processType.startsWith("A")) {
                dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));
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
     * 
     * 
     * @method getProperty
     * @date 2013. 10. 28.
     * @param dataMap
     * @return HashMap<String, Object>
     * @exception TCException
     * @throws
     * @see
     */
    private HashMap<String, Object> getProperty(HashMap<String, Object> dataMap) throws TCException {

        // 작성인
        TCSession session = ExcelTemplateHelper.getTCSession();
        String str = session.getUser().toString();
        StringTokenizer st = new StringTokenizer(str, "(");
        String name = st.nextToken();
        String group = session.getCurrentGroup().getFullName();
        dataMap.put("name", name);
        dataMap.put("group", group.substring(0, group.lastIndexOf(".")));
        // 차종코드
        dataMap.put("productCode", productCode);

        return dataMap;
    }

    /**
     * 
     * 
     * @method getPropertyTypeMap
     * @date 2013. 10. 28.
     * @param
     * @return Map<String, Integer>
     * @exception
     * @throws
     * @see
     */
    protected Map<String, Integer> getPropertyTypeMap() {

        Map<String, Integer> propertyMap = new HashMap<String, Integer>();

        propertyMap.put(SDVPropertyConstant.BL_ITEM_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_OBJECT_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_QUANTITY, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.SUBSIDIARY_MAKER, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.SUBSIDIARY_OLDPART, SDVPropertyConstant.TYPE_STRING);

        return propertyMap;
    }
}
