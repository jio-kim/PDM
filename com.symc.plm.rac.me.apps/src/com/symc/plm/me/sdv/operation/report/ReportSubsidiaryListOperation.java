package com.symc.plm.me.sdv.operation.report;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;

public class ReportSubsidiaryListOperation extends SimpleSDVExcelOperation implements Comparator<HashMap<String, Object>> {

    private Map<String, Integer> propertyMap = new HashMap<String, Integer>();
    private String productCode = "";
    private String processType = "";
    private String operationType = "";

    public ReportSubsidiaryListOperation() {
    }

    /**
     * 
     * @method executeOperation
     * @date 2013. 10. 30.
     * @param
     * @return void
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    @Override
    public void executeOperation() throws Exception {
    	
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
		String startTime = df.format(new Date());
		String expandingTime = null;
		String endTime = null;
		
        try {

            IDataSet dataSet = getData();
            
            expandingTime = df.format(new Date());

            if (dataSet != null) {
                String defaultFileName = productCode + "_" + "SubsidiaryMaterialMasterList" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
            }
        } catch (Exception e) {
            setExecuteError(e);
            // MessageBox에 보여줄 메시지
            // 구현하지 않으면 default 메시지를 보여준다.
            // setErrorMessage("");
        }finally{

			endTime = df.format(new Date());
			System.out.println("Start Time = "+startTime);
			System.out.println("Expand  Time = "+expandingTime);
			System.out.println("End Time = "+endTime);

		}
    }

    /**
     * 
     * @method executeOperation
     * @date 2013. 10. 30.
     * @param
     * @return IDataSet
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    @Override
    protected IDataSet getData() throws Exception {

        propertyMap.put(SDVPropertyConstant.BL_ITEM_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_REV_OBJECT_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR, SDVPropertyConstant.TYPE_STRING);
        //20201113 seho buy unit을 unit amount로 변경
        propertyMap.put(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT, SDVPropertyConstant.TYPE_STRING);
//        propertyMap.put(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.SUBSIDIARY_MAKER, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.SUBSIDIARY_OLDPART, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_DATE_RELEASED, SDVPropertyConstant.TYPE_STRING);

        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
        String compID = "";
        String revisionRule = "";
        String revRuleStandardDate = "";
        String variantRule = "";

        InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
        if (component != null && component instanceof TCComponentBOPLine) {
            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOPLine comp = (TCComponentBOPLine) component;

            // product Code
            productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            // shop or line - id
            compID = comp.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

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

            dataList = getChildrenList(dataList, (TCComponentBOPLine) component);

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
        } else {
            throw new NullPointerException("Target Component is not found or selected!!");
        }
    }

    private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();
        dataMap.put(dataName, dataList, IData.TABLE_FIELD);
        dataSet.addDataMap(dataName, dataMap);

        return dataSet;
    }

    /**
     * 사용자가 선택한 Component 하위의 자식 Component들의 정보를 가져온다.
     * 
     * @method getChildrenList
     * @date 2013. 10. 30.
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
                    TCComponentItemRevision subItemRevision = childLine.getItemRevision();
                    String type = childLine.getItem().getType();
                    // TCComponentBOPLine line = getParentBOPLine(childLine, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
                    if (subItemRevision != null) {
                        if (SDVBOPUtilities.isAssyTempLine(childLine) == true) {
                            continue;
                        }

                        if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type) || SDVTypeConstant.EBOM_MPRODUCT.equals(type) || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type)) {
                            continue;
                        }

                        if (SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM.equals(type)) {
                            HashMap<String, Object> dataMap = convertComponent(childLine);

                            dataMap = getSubsidiaryInfo(childLine, type, dataMap);

                            // 도장 - 편성버전이 00인것만 출력한다
                            if (processType.startsWith("P")) {
                                if (childLine.parent() != null) {
                                    TCComponentBOPLine parent = (TCComponentBOPLine) childLine.parent();
                                    if (parent.parent().parent().getItem().getType().equals("M7_BOPLine")) {
                                        String paintLine = parent.parent().getProperty(SDVPropertyConstant.BL_ITEM_ID);
                                        if (paintLine != null && !paintLine.isEmpty()) {
                                            String paintLineCode = paintLine.substring(paintLine.length() - 2);
                                            if (paintLineCode.equals("00")) {
                                                dataMap = getSubsidiaryInfo(childLine, type, dataMap);
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                dataMap = getSubsidiaryInfo(childLine, type, dataMap);
                            }
                            dataList.add(dataMap);
                        } else {
                            getChildrenList(dataList, (TCComponentBOPLine) context[i].getComponent());
                        }

                    }

                }
            }

            Collections.sort(dataList, new Comparator<HashMap<String, Object>>() {
                public int compare(final HashMap<String, Object> o1, final HashMap<String, Object> o2) {
                    return ((String) o1.get(SDVPropertyConstant.BL_ITEM_ID)).compareTo((String) o2.get(SDVPropertyConstant.BL_ITEM_ID));
                }
            });

        }

        return dataList;
    }

    /**
     * sort
     * 
     * @method compare
     * @date 2013. 10. 30.
     * @param HashMap
     *            <String, Object>, HashMap<String, Object>
     * @return List<HashMap<String,Object>>
     * @throws ParseException
     * @exception
     * @throws
     * @see
     */
    @Override
    public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
        return 0;
    }

    /**
     * CheckComplete
     * 
     * @method getCheckComplete
     * @date 2013. 10. 30.
     * @param dataMap
     * @return HashMap<String, Object>
     * @throws TCException
     *             , ParseException
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getSubsidiaryInfo(TCComponentBOPLine childLine, String type, HashMap<String, Object> dataMap) throws TCException, ParseException {

        String mecoID = null;
        String subsidiaryQty = childLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY);
        TCComponentItemRevision subItemRevision = childLine.getItemRevision();
        TCComponent mecoNo = childLine.parent().getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
        if (mecoNo != null) {
            mecoID = ((TCComponentItemRevision) mecoNo).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
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
            dataMap.put("line_revision", line.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
            String line_id = childLine.parent().parent().getProperty(SDVPropertyConstant.BL_ITEM_ID);
            dataMap.put("line_id", line_id.substring(line_id.length() - 2));
        }

        // Station Code, Station Revision
        if (processType.startsWith("B") || processType.startsWith("P")) {
            TCComponentBOPLine station = getParentBOPLine(childLine, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
            if (station != null) {
                dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, station.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE) + "-" + station.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE));
                dataMap.put("station_revision", station.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
            }
        }

        // 공법(작업표준서) NO, 공법 Revision
        TCComponentBOPLine operation = getParentBOPLine(childLine, operationType);
        if (operation != null) {
            dataMap.put("operation_id", operation.getProperty(SDVPropertyConstant.BL_ITEM_ID));
            dataMap.put("operation_revision", operation.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));

            // 조립
            if (processType.startsWith("A")) {
                dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));
            }
        }

        // 부자재 정보
        dataMap.put("description", SDVBOPUtilities.getVariant(childLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).get("printDescriptions"));
        dataMap.put("quantity", SDVStringUtiles.getDoubleFromString(subsidiaryQty));
        dataMap.put("effectDate", childLine.parent().getProperty(SDVPropertyConstant.MECO_EFFECT_DATE));
        dataMap.put("mecoNo", mecoID);
        dataMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(childLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).get("printValues"));
        dataMap.put(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR, subItemRevision.getProperty(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR));
        //20201113 seho buy unit을 unit amount로 변경
        dataMap.put(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT, subItemRevision.getProperty(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT));
//        dataMap.put(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT, subItemRevision.getProperty(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT));
        dataMap.put(SDVPropertyConstant.SUBSIDIARY_OLDPART, subItemRevision.getProperty(SDVPropertyConstant.SUBSIDIARY_OLDPART));
        dataMap.put(SDVPropertyConstant.SUBSIDIARY_MAKER, subItemRevision.getProperty(SDVPropertyConstant.SUBSIDIARY_MAKER));
        dataMap = getReleaseDate(childLine, dataMap);

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
     * ReleaseDate
     * 
     * @method getReleaseDate
     * @date 2013. 10. 30.
     * @param dataMap
     * @return HashMap<String, Object>
     * @throws TCException
     *             , ParseException
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getReleaseDate(TCComponentBOPLine childLine, HashMap<String, Object> dataMap) throws TCException, ParseException {

        DateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        String beforeDate = childLine.parent().getProperty(SDVPropertyConstant.BL_DATE_RELEASED);
        String afterDate = "";
        if (!(beforeDate == null || beforeDate.isEmpty())) {
            Date date = format.parse(beforeDate);
            afterDate = sdFormat.format(date);
        }
        dataMap.put("afterDate", afterDate);

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
