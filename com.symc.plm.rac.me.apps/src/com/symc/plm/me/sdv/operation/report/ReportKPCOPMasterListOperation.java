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

import com.kgm.common.utils.CustomUtil;
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
            bomStandardInfo_date = "����Ͻ� : " + ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm");

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
     * Shop ������ �ڽ� Component���� ������ �����´�.
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

            /*** Activity üũ ***********************************************************************/
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine bop_line = (TCComponentBOPLine) context[i].getComponent();

                    String type = bop_line.getItem().getType();

                    //���Ҵ� Line ���͸�
                    if (SDVBOPUtilities.isAssyTempLine(bop_line)) 
                        continue;
                    

                    //�ӵ� ������ ���� ���ʿ��� �۾� ���͸�
                    if (SDVTypeConstant.EBOM_MPRODUCT.equals(type) 
                            || SDVTypeConstant.EBOM_VEH_PART.equals(type) 
                            || SDVTypeConstant.EBOM_STD_PART.equals(type) 
                            || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type) 
                            || SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(type)) {
                        continue;
                    }

                    // �������� üũ
                    if (type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM))

                    {
                        // ���� ������
                        TCComponentItemRevision op_rev = bop_line.getItemRevision();

                        // ���� ���� ��Ƽ��Ƽ ��Ʈ
                        TCComponentMEActivity root_activity = (TCComponentMEActivity) op_rev.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);

                        // ��Ƽ��Ƽ ���� ��ü ��Ƽ��Ƽ ����Ʈ
                        TCComponentMEActivity[] child_activities = root_activity.listAllActivities();

                        for (TCComponentMEActivity child_activity : child_activities) {
                            String controlPoint;
                            String controlStd = null;
                            String toolProperty = null;

                            // ��Ƽ��Ƽ Ÿ��(������) üũ
                            if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_CONTROL_POINT)) {

                                controlPoint = child_activity.getProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT);

                                if (controlPoint != null && !controlPoint.isEmpty()) {
                                    TCSession session = CustomUtil.getTCSession();

                                    // Activity�� �Ҵ�� toolList ��������
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
                                        		System.out.println("�����Ǵ� ��������" + bop_line.toDisplayString());
                                        		//skip ���� ������ Tool �� �߷����� ��� ���� �߻���. �� ��� �����ϰ� �Ѿ���� ����.
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
                        /*** Activity üũ �� ***********************************************************************/

                    } 
                    else if( type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                        getChildrenList(dataList, (TCComponentBOPLine) context[i].getComponent());
                    }
                }
             }
        }
        
    	
        return dataList;
    }

    // Project Code �Ӽ�
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
     * Shop, Line, Station, Operaion ID �� Rev ��������
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

                // Shop, Line, Station, Operation ID, Rev ��������
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
     * �⺻ �Ӽ� �ܿ� ��� �Ǵ� ���ǿ� ���� ������ ���� ������ �����Ѵ�.
     * 
     * @method getAdditionalProperty
     * @date 2013. 10. 28.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */

    // �������� : �������� �Ӽ��� ���ũ�̸� Activity�� �Ҵ�� ������ ��ũ���� ǥ��. ü�� or �ἱ�̸� Activity�� �������� �Ӽ� �� ǥ��
    private HashMap<String, Object> getAdditionalProperty(HashMap<String, Object> dataMap, String controlPoint, String controlStd, String toolProperty, TCComponentMEActivity child_activity) throws TCException {
    	
    	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	/**
    	 * ���� ���� ����� Ư��Ư�� ���ϰ��� ����
    	 * KPC List ����
    	 */
    	String realControlPoint = controlPoint;
    	dataMap.put("realControlPoint", realControlPoint);
    	if(controlPoint.length() > 2) {
    		controlPoint = controlPoint.substring(2);
    	}
        if (controlPoint.contains("��ũ") || controlPoint.contains("ä��")) {
            dataMap.put("controlPoint", controlPoint);
            dataMap.put("controlSheet", "C/SHEET");
        } else if (controlPoint.contains("�ἱ")) {
        	dataMap.put("controlSheet", "C/SHEET");
            dataMap.put("controlPoint", controlPoint);
        } else if (controlPoint.contains("����") || controlPoint.contains("Ÿ��")) {
            dataMap.put("controlPoint", "����");
            dataMap.put("controlSheet", "����");
        } else if (controlPoint.contains("����")) {
        	dataMap.put("controlPoint", "������");
        	 dataMap.put("controlSheet", "ǰ����Ż");
        } else {
        	dataMap.put("controlPoint", controlPoint);
        	dataMap.put("controlSheet", "��Ÿ");
        }

        if (controlPoint.contains("��ũ") || controlPoint.contains("ä��")) {
            dataMap.put("controlStd", toolProperty);
        } else if ( controlPoint.equals("�ἱ") || controlPoint.equals("��Ÿ")) {
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
     * Component�� �Ӽ��� ������ HashMap���� �����Ѵ�.
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
