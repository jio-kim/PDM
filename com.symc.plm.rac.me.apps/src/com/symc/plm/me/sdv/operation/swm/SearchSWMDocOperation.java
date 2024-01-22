/**
 * 
 */
package com.symc.plm.me.sdv.operation.swm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVQueryUtils;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.client.model.LovValue;

/**
 * Class Name : SearchSWMDocOperation
 * Class Description :
 * 
 * @date 2013. 11. 19.
 * 
 */
public class SearchSWMDocOperation extends AbstractSDVActionOperation {

    private String vehicleNo;
    private String shopCode;
    private String category;
    private String referenceInfo;
    private String group;
    private String workName;
    TCComponentItemRevision itemRevision;

    public SearchSWMDocOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public SearchSWMDocOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, actionId, ownerId, parameters, dataset);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {

        IDataSet dataset = getDataSet();
        IDataMap searchInfoMap = dataset.getDataMap("searchCriteriaSWMDocView");
        vehicleNo = searchInfoMap.getStringValue("vehicle_no");
        if (vehicleNo.equalsIgnoreCase("ALL")) {
            vehicleNo = "*";
        }
        shopCode = searchInfoMap.getStringValue("shop_code");
        category = searchInfoMap.getStringValue("category");
        if (category.equalsIgnoreCase("ALL")) {
            category = "*";
        }
        referenceInfo = searchInfoMap.getStringValue("reference_info");
        if (referenceInfo == "") {
            referenceInfo = "*";
        }

        group = searchInfoMap.getStringValue("group");
        if (group == "") {
            group = "*";
        }

        workName = searchInfoMap.getStringValue("workName");
        if (workName == "") {
            workName = "*";
        }

        List<HashMap<String, Object>> operationList = search();

        IDataMap dataMap = new RawDataMap();

        dataMap.put("operationList", operationList, IData.TABLE_FIELD);
        dataset.addDataMap("searchListSWMDocView", dataMap);

        setDataSet(dataset);
    }

    /**
     * 검색 조건에 따라 M7_STANDARD_WORK_METHOD_ITEM_REV 정보를 가져온다.
     * 
     * 
     */
    public List<HashMap<String, Object>> search() throws TCException, Exception {
        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
        TCComponent[] qryResult = SDVQueryUtils.executeSavedQuery("SYMC_Search_StdWorkMethod", new String[] { "m7_VEHICLE_CODE", "m7_SHOP_CODE", "m7_CATEGORY", "m7_REFERENCE_INFO", "object_name", "m7_GROUP" }, new String[] { vehicleNo, shopCode, category, referenceInfo, workName, group });

        if (qryResult != null && qryResult.length != 0) {
            for (int i = 0; i < qryResult.length; i++) {
                TCComponentItem item = (TCComponentItem) qryResult[i];
                itemRevision = item.getLatestItemRevision();
                itemRevision.refresh();

                HashMap<String, Object> dataMap = new HashMap<String, Object>();

                // 관리번호
                dataMap.put(SDVPropertyConstant.ITEM_ITEM_ID, itemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));

                // 리비전
                dataMap.put(SDVPropertyConstant.ITEM_REVISION_ID, itemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID));

                // 작업명
                dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, itemRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

                // 작성일
                dataMap.put(SDVPropertyConstant.ITEM_CREATION_DATE, SDVStringUtiles.dateToString(itemRevision.getDateProperty(SDVPropertyConstant.ITEM_CREATION_DATE), "yyyy-MM-dd"));

                // 관련근거
                dataMap.put(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO, itemRevision.getProperty(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO));

                // 게시일
                Date date = itemRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
                if (date != null) {
                    dataMap.put(SDVPropertyConstant.ITEM_DATE_RELEASED, SDVStringUtiles.dateToString(itemRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED), "yyyy-MM-dd"));
                }

                // 폐기일
                date = itemRevision.getDateProperty(SDVPropertyConstant.ITEM_M7_DISCARD_DATE);
                if (date != null) {
                    dataMap.put(SDVPropertyConstant.ITEM_M7_DISCARD_DATE, SDVStringUtiles.dateToString(itemRevision.getDateProperty(SDVPropertyConstant.ITEM_M7_DISCARD_DATE), "yyyy-MM-dd"));
                }

                // 작성자
                TCComponentUser user = (TCComponentUser) itemRevision.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
                dataMap.put(SDVPropertyConstant.ITEM_OWNING_USER, user.getProperty("user_name"));

                // 구분
                String category = itemRevision.getProperty("m7_CATEGORY");
                List<LovValue> categoryList = getLOVValues(ExcelTemplateHelper.getTCSession(), "M7_SWM_CATEGORY");
                for (LovValue categoryElement : categoryList) {
                    if (category.equals(categoryElement.getValue())) {
                        dataMap.put(SDVPropertyConstant.SWM_CATEGORY, categoryElement.getDescription());
                    }
                }

                // 승인
                dataMap = getSignoffInfo(itemRevision, SDVPropertyConstant.WORKFLOW_SIGNOFF, dataMap);

                // 직
                dataMap.put(SDVPropertyConstant.SWM_GROUP, itemRevision.getProperty(SDVPropertyConstant.SWM_GROUP));

                dataList.add(dataMap);
            }
        }
        return dataList;

    }

    // LOV 가져오기(구분)
    public static List<LovValue> getLOVValues(TCSession session, String lovName) throws TCException {
        TCComponentListOfValuesType type = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
        TCComponentListOfValues[] values = type.find(lovName);

        return values[0].getListOfValues().getValues();
    }

    // 결재자 정보 가져오기(승인)
    private HashMap<String, Object> getSignoffInfo(TCComponent component, String workflowSignoff, HashMap<String, Object> dataMap) throws TCException {
        AIFComponentContext[] contextArray = ((TCComponent) component).whereReferenced();
        String signoff = null;

        for (AIFComponentContext context : contextArray) {
            TCComponent comp = (TCComponent) context.getComponent();
            if (comp instanceof TCComponentTask) {
                TCComponentTask process = (TCComponentTask) comp;
                TCComponentTask rootTask[] = process.getSubtasks();
                for (TCComponentTask task : rootTask) {
                    if (task.getName().equals("Team Leader")) {
                        AIFComponentContext[] endtask = task.getChildren();
                        for (int i = 0; i < endtask.length; i++) {
                            if (endtask[i].getComponent().getType().equals("EPMPerformSignoffTask")) {
                                try {
                                    signoff = endtask[i].getComponent().getProperty("viewed_by");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
        dataMap.put("signoff", signoff);

        return dataMap;
    }
}
