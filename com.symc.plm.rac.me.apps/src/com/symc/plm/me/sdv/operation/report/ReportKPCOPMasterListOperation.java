package com.symc.plm.me.sdv.operation.report;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

public class ReportKPCOPMasterListOperation extends SimpleSDVExcelOperation {

    protected static final Map<String, Integer> propertyMap;

    static {
        propertyMap = new HashMap<String, Integer>();

        propertyMap.put(SDVPropertyConstant.BL_ITEM_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_DATE_RELEASED, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_OBJECT_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.ACTIVITY_CONTROL_POINT, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.OPERATION_REV_STATION_NO, SDVPropertyConstant.TYPE_STRING); // ASSYOP
        propertyMap.put(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.MECO_REV_PROJECT_CODE, SDVPropertyConstant.TYPE_STRING);

    }

    private String productCode = "";

    public ReportKPCOPMasterListOperation() {
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            IDataSet dataSet = getData();

            if (dataSet != null) {
                String defaultFileName = productCode + "_" + "KPCMasterList" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
            }
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

        InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
        if (component != null && component instanceof TCComponentBOPLine) {

            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOPLine comp = (TCComponentBOPLine) bomWindow.getTopBOMLine();

            // product Code
            productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);

            // shop or line - id
            compID = comp.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            productInfo = productCode + " - " + compID;

            TCComponentRevisionRule revisionRule = bomWindow.getRevisionRule();
            Date date = revisionRule.getDateProperty("rule_date");
            String rule_date = null;
            String today = ExcelTemplateHelper.getToday("yyyy-MM-dd");

            if (date != null) {
                rule_date = new SimpleDateFormat("yyyy-MM-dd").format(date);
            } else {
                rule_date = today;
            }

            bomStandardInfo = revisionRule + "(" + rule_date + ")";
            bomStandardInfo_date = "출력일시 : " + ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm");

            IDataMap dataMap = new RawDataMap();

            dataMap.put("productInfo", productInfo);
            dataMap.put("bomStandardInfo", bomStandardInfo);
            dataMap.put("bomStandardInfo_date", bomStandardInfo_date);

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

            /*** Activity 체크 ***********************************************************************/
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine bop_line = (TCComponentBOPLine) context[i].getComponent();

                    String type = bop_line.getItem().getType();

                    //미할당 Line 필터링
                    if (SDVBOPUtilities.isAssyTempLine(bop_line)) 
                        continue;
                    

                    //속도 개선을 위한 불필요한 작업 필터링
                    if (SDVTypeConstant.EBOM_MPRODUCT.equals(type) 
                            || SDVTypeConstant.EBOM_VEH_PART.equals(type) 
                            || SDVTypeConstant.EBOM_STD_PART.equals(type) 
                            || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type) 
                            || SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(type)) {
                        continue;
                    }

                    // 공법인지 체크
                    if (type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM))

                    {
                        // 공법 리비전
                        TCComponentItemRevision op_rev = bop_line.getItemRevision();

                        // 공법 하위 엑티비티 루트
                        TCComponentMEActivity root_activity = (TCComponentMEActivity) op_rev.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);

                        // 엑티비티 하위 전체 엑티비티 리스트
                        TCComponentMEActivity[] child_activities = root_activity.listAllActivities();

                        for (TCComponentMEActivity child_activity : child_activities) {
                            String controlPoint;
                            String controlStd = null;
                            String toolProperty = null;

                            // 엑티비티 타입(관리점) 체크
                            if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_CONTROL_POINT)) {

                                controlPoint = child_activity.getProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT);

                                if (controlPoint != null && !controlPoint.isEmpty()) {
                                    TCSession session = CustomUtil.getTCSession();

                                    // Activity에 할당된 toolList 가져오기
                                    TCProperty toolProp = child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TOOL_LIST);// SDVPropertyConstant.ACTIVITY_TOOL_LIST
                                    String[] value = toolProp.getStringArrayValue();
                                    for (String val : value) {
                                        TCComponent toolComp = null;
										try
										{
											toolComp = session.stringToComponent(val);
										} catch (Exception e1)
										{
											e1.printStackTrace();
											continue;
										}
                                        if (toolComp != null && toolComp instanceof TCComponentMEAppearancePathNode) {
                                        	TCComponentBOMLine toolBOPLine = null;
                                        	try {
                                        		
                                        		 toolBOPLine = bop_line.window().getBOMLineFromAppearancePathNode((TCComponentMEAppearancePathNode) toolComp, bop_line);
                                        	} catch(Exception e) {
                                        		System.out.println("문제되는 공법라인" + bop_line.toDisplayString());
                                        		//skip 공법 하위에 Tool 이 잘려나간 경우 오류 발생함. 이 경우 무시하고 넘어가도록 변경.
                                        		continue;
                                        	}
                                        	
                                            String toolTorque = toolBOPLine.getProperty("M7_TORQUE");// BL_NOTE_TORQUE
                                            String toolTorqueValue = toolBOPLine.getProperty(SDVPropertyConstant.M7_TORQUE_VALUE);// BL_NOTE_TORQUE_VALUE
                                            

                                            toolProperty = toolTorque + " " + toolTorqueValue;
                                            
                                        }
                                    }
                                    if(toolProperty != null)
                                    {
	                                    controlStd = child_activity.getProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS);
	                                    HashMap<String, Object> dataMap = convertComponent(bop_line);
	                                    dataMap = getParentInfo(bop_line, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, dataMap);
	                                    dataMap = getParentInfo(bop_line, SDVTypeConstant.BOP_PROCESS_LINE_ITEM, dataMap);
	                                    dataMap = getAdditionalProperty(dataMap, controlPoint, controlStd, toolProperty, child_activity);
	                                    dataMap = getReleaseDate(dataMap);
	                                    dataMap = getProjectCode(dataMap, bop_line);
	                                    dataList.add(dataMap);
                                    }
                                } 
                            }
                        }
                        /*** Activity 체크 끝 ***********************************************************************/

                    } 
                    else if( type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                        getChildrenList(dataList, (TCComponentBOPLine) context[i].getComponent());
                    }
                }
             }
        }
        
    	
        return dataList;
    }

    // Project Code 속성
    private HashMap<String, Object> getProjectCode(HashMap<String, Object> dataMap, TCComponentBOPLine bop_line) throws Exception {

        String projectCode = "";

        TCComponentItemRevision itemRevision = bop_line.getItemRevision();
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision) itemRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);

        if (mecoRevision != null) {
            projectCode = mecoRevision.getProperty(SDVPropertyConstant.MECO_REV_PROJECT_CODE);
            dataMap.put("projectCode", projectCode);
        } else {
            dataMap.put("projectCode", projectCode);
        }

        return dataMap;
    }

    /**
     * Shop, Line, Station, Operaion ID 및 Rev 가져오기
     * 
     * @method getParentInfo
     * @date 2013. 10. 30.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    public HashMap<String, Object> getParentInfo(TCComponentBOPLine bop_line, String type, HashMap<String, Object> dataMap) throws TCException {
        if (bop_line.parent() != null) {
            TCComponentBOPLine parent = (TCComponentBOPLine) bop_line.parent();
            if (parent.getItem().getType().equals(type)) {

                if (type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                    String line_rev = parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    dataMap.put(SDVPropertyConstant.LINE_REV_CODE, parent.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, line_rev);

                }

                // Shop, Line, Station, Operation ID, Rev 가져오기
                dataMap.put(type + SDVPropertyConstant.BL_ITEM_ID, parent.getProperty(SDVPropertyConstant.BL_ITEM_ID));
                dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
                dataMap.put(type + SDVPropertyConstant.BL_DATE_RELEASED, parent.getProperty(SDVPropertyConstant.BL_DATE_RELEASED));
                dataMap.put(type + SDVPropertyConstant.BL_OBJECT_NAME, parent.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));
                dataMap.put(type + SDVPropertyConstant.ACTIVITY_CONTROL_POINT, parent.getProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT));

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

    // 관리기준 : 관리점의 속성이 토오크이면 Activity에 할당된 공구의 토크정보 표시. 체결 or 결선이면 Activity의 관리기준 속성 값 표시
    private HashMap<String, Object> getAdditionalProperty(HashMap<String, Object> dataMap, String controlPoint, String controlStd, String toolProperty, TCComponentMEActivity child_activity) throws TCException {
    	
    	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	/**
    	 * 조립 생산 기술팀 특별특성 보완관련 수정
    	 * KPC List 수정
    	 */
    	String realControlPoint = controlPoint;
    	dataMap.put("realControlPoint", realControlPoint);
    	if(controlPoint.length() > 2) {
    		controlPoint = controlPoint.substring(2);
    	}
        if (controlPoint.contains("토크") || controlPoint.contains("채결")) {
            dataMap.put("controlPoint", controlPoint);
            dataMap.put("controlSheet", "C/SHEET");
        } else if (controlPoint.contains("결선")) {
        	dataMap.put("controlSheet", "C/SHEET");
            dataMap.put("controlPoint", controlPoint);
        } else if (controlPoint.contains("램프") || controlPoint.contains("타각")) {
            dataMap.put("controlPoint", "법규");
            dataMap.put("controlSheet", "전산");
        } else if (controlPoint.contains("추적")) {
        	dataMap.put("controlPoint", "추적성");
        	 dataMap.put("controlSheet", "품질포탈");
        } else {
        	dataMap.put("controlPoint", controlPoint);
        	dataMap.put("controlSheet", "기타");
        }

        if (controlPoint.contains("토크") || controlPoint.contains("채결")) {
            dataMap.put("controlStd", toolProperty);
        } else if ( controlPoint.equals("결선") || controlPoint.equals("기타")) {
            dataMap.put("controlStd", controlStd);
        }
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        return dataMap;
    }

    // Release Date
    private HashMap<String, Object> getReleaseDate(HashMap<String, Object> dataMap) throws TCException, ParseException {

        DateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

        String beforeDate = (String) dataMap.get(SDVPropertyConstant.BL_DATE_RELEASED);
        String afterDate = "";
        if (!(beforeDate == null || beforeDate.isEmpty())) {
            Date date = format.parse(beforeDate);
            afterDate = sdFormat.format(date);
        }
        dataMap.put("afterDate", afterDate);
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
