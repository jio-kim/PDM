package com.symc.plm.me.sdv.operation.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;

public class ReportToolMasterListOperation extends SimpleSDVExcelOperation {

    protected static final Map<String, Integer> propertyMap;

    static {

        propertyMap = new HashMap<String, Integer>();

        propertyMap.put(SDVPropertyConstant.BL_ITEM_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_REV_OBJECT_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.TOOL_SPEC_ENG, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_NOTE_TORQUE, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_QUANTITY, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_OWNING_USER, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.STATION_REV_CODE, SDVPropertyConstant.TYPE_STRING);

    }

    private String productCode = "";
    private String operationType = "";
    private String processType = "";

    protected String currentOperation;
    protected String beforeOperation;
    protected int seq;

    public ReportToolMasterListOperation() {
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            IDataSet dataSet = getData();

            if (dataSet != null) {
                String defaultFileName = productCode + "_" + "ToolList" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
            }

            currentOperation = "";
            beforeOperation = "empty";
            seq = 0;
        } catch (Exception e) {
            setExecuteError(e);
        }
    }

    @Override
    protected IDataSet getData() throws Exception {

        String productInfo = "";
        String bomStandardInfo = "";
        String bomStandardInfo_date = "";
        String compID = "";
        String variantRule = "";

        InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
        if (component != null && component instanceof TCComponentBOPLine) {

            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOPLine comp = (TCComponentBOPLine) bomWindow.getTopBOMLine();

            TCComponentRevisionRule revisionRule = bomWindow.getRevisionRule();
            Date date = revisionRule.getDateProperty("rule_date");
            String rule_date = null;
            String today = ExcelTemplateHelper.getToday("yyyy-MM-dd");

            if (date != null) {
                rule_date = new SimpleDateFormat("yyyy-MM-dd").format(date);
            } else {
                rule_date = today;
            }

            // product Code
            productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);

            // shop or line - id
            compID = comp.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            productInfo = productCode + " - " + compID;

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

            // Variant
            HashMap<String, String> variantMap = SDVBOPUtilities.getBOMConfiguredVariantSet(bomWindow);
            if (variantMap != null) {
                Iterator<String> iter = variantMap.keySet().iterator();
                while (iter.hasNext()) {
                    String key = iter.next();
                    String value = variantMap.get(key);
                    variantRule = variantRule + " " + key + "=" + value;
                }

                if (variantRule.length() > 1) {
                    variantRule = variantRule.substring(1);
                }
            }

            bomStandardInfo = revisionRule + "(" + rule_date + ")";
            bomStandardInfo_date = "출력일시 : " + ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm");

            IDataMap dataMap = new RawDataMap();

            dataMap.put("productInfo", productInfo);
            dataMap.put("compID", compID);
            dataMap.put("bomStandardInfo", bomStandardInfo);
            dataMap.put("variantRule", variantRule);
            dataMap.put("bomStandardInfo_date", bomStandardInfo_date);
            dataMap.put("operationType", operationType);

            List<HashMap<String, Object>> dataList = getChildrenList(new ArrayList<HashMap<String, Object>>(), (TCComponentBOPLine) component);
            IDataSet dataSet = convertToDataSet("operationList", dataList);
            dataSet.addDataMap("additionalInfo", dataMap);

            return dataSet;

        } else {
            throw new NullPointerException("Target Component is not found or selected!!");
        }
    }

    /**
     * Shop 하위의 자식 Component들의 정보를 가져온다.
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
        if (parentLine.getChildrenCount() > 0) {

            AIFComponentContext[] context = parentLine.getChildren();

            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();

                    if (null != childLine.getItemRevision()) {

                        TCComponentItemRevision revisionType = childLine.getItemRevision();
                        String type = childLine.getItem().getType();

                        // 미할당 Line 필터링
                        if (SDVBOPUtilities.isAssyTempLine(childLine))
                            continue;

                        // 속도 개선을 위한 불필요한 작업 필터링
                        if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type) || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type) || SDVTypeConstant.EBOM_MPRODUCT.equals(type)) {
                            continue;
                        }

                        if (SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(type)) {

                            HashMap<String, Object> dataMap = convertComponent(childLine);
                            dataMap = getParentInfo(childLine, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, dataMap);
                            dataMap = getParentInfo(childLine, SDVTypeConstant.BOP_PROCESS_LINE_ITEM, dataMap);

                            if (!processType.startsWith("A")) {
                                dataMap = getParentInfo(childLine, SDVTypeConstant.BOP_PROCESS_STATION_ITEM, dataMap);
                            }

                            dataMap = getParentInfo(childLine, operationType, dataMap);
                            dataMap = getAdditionalProperty(childLine, dataMap);
                            dataMap = getFindNo(dataMap, operationType);
                            dataMap = getNameProperty(dataMap, operationType);
                            dataList.add(dataMap);

                            String torque = (String) dataMap.get(SDVPropertyConstant.BL_NOTE_TORQUE);
                            String torqueValue = (String) dataMap.get(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE);

                            String torqueResult = torque + " " + torqueValue;
                            dataMap.put("torqueResult", torqueResult);

                            dataMap.put("spec_code", revisionType.getProperty(SDVPropertyConstant.TOOL_SPEC_ENG));

                        } else {
                            getChildrenList(dataList, (TCComponentBOPLine) context[i].getComponent());
                        }
                    }
                }
            }
        }

        return dataList;
    }

    /**
     * Shop, Line, Station, Operaion ID 및 Rev 가져오기
     * 
     * @method getParentInfo
     * @date 2013. 10. 30.
     * @param
     * @return HashMap<String,Object>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public HashMap<String, Object> getParentInfo(TCComponentBOPLine childLine, String type, HashMap<String, Object> dataMap) throws Exception {
        if (childLine.parent() != null) {

            TCComponentBOPLine parent = (TCComponentBOPLine) childLine.parent();

            if (parent.getItem().getType().equals(type)) {

                if (type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {

                    String line_rev = parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    dataMap.put(SDVPropertyConstant.LINE_REV_CODE, parent.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, line_rev);

                } else if (type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                    String line_code = parent.getProperty(SDVPropertyConstant.STATION_LINE);
                    String station_code = parent.getProperty(SDVPropertyConstant.STATION_REV_CODE);
                    String station_rev = parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);

                    if (!line_code.equals("") && !station_code.equals("")) {
                        dataMap.put(SDVPropertyConstant.STATION_REV_CODE, line_code + "-" + station_code);
                    }
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, station_rev);
                } else if (type.equals(operationType)) {
                    if (processType.startsWith("A")) {
                        String station_no = parent.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
                        dataMap.put(SDVPropertyConstant.STATION_REV_CODE, station_no);
                    }
                }

                String quantity = (String) dataMap.get(SDVPropertyConstant.BL_QUANTITY);
                String quantitySplit = quantity.split("\\.")[0];

                dataMap.put("quantity", quantitySplit);

                dataMap.put(type + SDVPropertyConstant.BL_ITEM_ID, parent.getProperty(SDVPropertyConstant.BL_ITEM_ID));
                dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
                dataMap.put(type + SDVPropertyConstant.BL_OWNING_USER, parent.getProperty(SDVPropertyConstant.BL_OWNING_USER));
                dataMap.put(type + SDVPropertyConstant.BL_OBJECT_NAME, parent.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));
                dataMap.put("worker_code", parent.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE));

            } else {
                return getParentInfo(parent, type, dataMap);
            }
        }

        return dataMap;
    }

    /**
     * 기본 속성 외에 계산 또는 조건에 의해 나오는 값은 별도로 저장한다.
     * 
     * @method getAdditionalProperty
     * @date 2013. 10. 28.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */

    // CAD Data 속성
    private HashMap<String, Object> getAdditionalProperty(TCComponentBOPLine childLine, HashMap<String, Object> dataMap) throws TCException {

        AIFComponentContext[] context = childLine.getItemRevision().getChildren();

        // String cadData = "";
        String type = "";
        for (int i = 0; i < context.length; i++) {
            if (context[i].getComponent() instanceof TCComponentDataset) {
                type = context[i].getComponent().getType();

                if (type.equals("CATPart") || type.equals("CATDrawing")) {
                    dataMap.put("cad", "●");
                } else if (type.equals("DirectModel")) {
                    dataMap.put("jt", "●");
                } else if (type.equals("CATCache")) {
                    dataMap.put("cgr", "●");
                } else {
                    dataMap.put("etc", "●");
                }
            }
        }

        return dataMap;
    }

    // Find No.
    private HashMap<String, Object> getFindNo(HashMap<String, Object> dataMap, String type) throws TCException {

        // 공법 ID & 공법 REVISION
        String operationID = (String) dataMap.get(operationType + SDVPropertyConstant.BL_ITEM_ID);
        String operationRev = (String) dataMap.get(operationType + SDVPropertyConstant.BL_ITEM_REV_ID);
        String operationName = (String) dataMap.get(operationType + SDVPropertyConstant.BL_OBJECT_NAME);
        dataMap.put("operationID", operationID);
        dataMap.put("operationRev", operationRev);
        dataMap.put("operationName", operationName);

        // Find NO
        currentOperation = ((String) dataMap.get(operationType + SDVPropertyConstant.BL_ITEM_ID));
        if (currentOperation.equals(beforeOperation)) {
            seq += 10;
        } else {
            seq = 10;
        }
        beforeOperation = currentOperation;
        dataMap.put("seq", seq);

        return dataMap;
    }

    // 담당자 이름 속성
    private HashMap<String, Object> getNameProperty(HashMap<String, Object> dataMap, String type) throws TCException {

        String string = (String) dataMap.get(operationType + SDVPropertyConstant.BL_OWNING_USER);
        StringTokenizer st = new StringTokenizer(string, "(");

        String name = st.nextToken();

        dataMap.put("name", name);

        return dataMap;
    }

    private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
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
    private HashMap<String, Object> convertComponent(TCComponentBOPLine component) throws TCException {
        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        Iterator<String> iterator = propertyMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            int value = (int) propertyMap.get(key);

            switch (value) {
            case 0x01:
                dataMap.put(key, component.getProperty(key));
                break;
            case 0x02:
                dataMap.put(key, component.getIntProperty(key));
                break;
            case 0x03:
                dataMap.put(key, component.getDoubleProperty(key));
                break;
            case 0x04:
                dataMap.put(key, component.getProperty(key));
                break;
            case 0x05:
                dataMap.put(key, component.getReferenceProperty(key));
                break;
            default:
                break;
            }
        }

        return dataMap;
    }

}
