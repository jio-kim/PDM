package com.symc.plm.me.sdv.operation.wp.search;

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

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.sdv.excel.common.PreviewWeldConditionSheetExcelHelper;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;

public class SearchWeldConditionSheetSearchOperation extends AbstractSDVActionOperation {

    //private SearchCriteriaView searchCriteriaView;
    //private Button searchBT;

    private String lineCode;
    private String lineRev;
    private String stationCode;
    private String stationRev;

    public SearchWeldConditionSheetSearchOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        //registry = Registry.getRegistry(this);
    }

    public SearchWeldConditionSheetSearchOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, actionId, ownerId, parameters, dataset);
        //registry = Registry.getRegistry(this);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        List<HashMap<String, Object>> weldOperationList = new ArrayList<HashMap<String, Object>>();

        // 임시 (버튼 비활성)
//        Display.getDefault().asyncExec(new Runnable() {
//            public void run() {
//                AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
//                searchCriteriaView = (SearchCriteriaView) dialog.getView("searchCriteriaView");
//                LinkedHashMap<String, IButtonInfo> button = searchCriteriaView.getActionToolButtons();
//                IButtonInfo searchButton = button.get("Search");
//                searchBT = searchButton.getButton();
//                searchBT.setEnabled(false);
//            }
//        });


        IDataSet dataset = getDataSet();
        if(dataset.containsMap("searchCriteriaView"))
        {
            IDataMap dataMap = dataset.getDataMap("searchCriteriaView");

            HashMap<String, String> conditionMap = new HashMap<String, String>();
            String value = dataMap.getStringValue(SDVPropertyConstant.LINE_REV_CODE);
            if(value != null && !"All".equals(value)) conditionMap.put("line_code", value);
            value = dataMap.getStringValue("stationCode");
            if(value != null && !"All".equals(value)) conditionMap.put("station_code", value);
            value = dataMap.getStringValue("weldOP");
            if(value != null && value.length() > 0) conditionMap.put("weldOP", value);
            weldOperationList = getChildLine(weldOperationList, (TCComponentBOPLine) dataMap.getValue("topLine"), conditionMap);
        }

        IDataMap dataMap = new RawDataMap();
        dataMap.put("weldOperationList", weldOperationList, IData.TABLE_FIELD);
        dataset.addDataMap("listView", dataMap);

        setDataSet(dataset);

        // 임시 (버튼 활성)
//        Display.getDefault().asyncExec(new Runnable() {
//            public void run() {
//                searchBT.setEnabled(true);
//            }
//        });
    }

    /**
     * TOP Line 에서 검색하면서 하위 BOMLine 을 내려간다
     *
     * @method getChildLine
     * @date 2013. 12. 13.
     * @param
     * @return List<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
    private List<HashMap<String, Object>> getChildLine(List<HashMap<String, Object>> list, TCComponentBOPLine topLine, HashMap<String, String> conditionMap) throws Exception
    {
        if(PreviewWeldConditionSheetExcelHelper.isLine(topLine))
        {
            lineCode = topLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
            lineRev = topLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
        }

        if(topLine.getChildrenCount() > 0)
        {
            AIFComponentContext[] contexts = topLine.getChildren();
            for(AIFComponentContext context : contexts)
            {
                TCComponentBOPLine childLine = (TCComponentBOPLine) context.getComponent();
                if (childLine instanceof TCComponentAppGroupBOPLine || childLine.getType() == null) {
                    continue;
                }
                if(PreviewWeldConditionSheetExcelHelper.isLine(childLine))
                {
                    lineCode = childLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
                    lineRev = childLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    list = getChildLine(list, childLine, conditionMap);
                }
                else if(PreviewWeldConditionSheetExcelHelper.isStation(childLine))
                {
                    stationCode = childLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);
                    stationRev = childLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    list = getChildLine(list, childLine, conditionMap);
                }
                else if(PreviewWeldConditionSheetExcelHelper.isWeldOperation(childLine))
                {
                    HashMap<String, Object> operationMap = getWeldOperationInfo(childLine, conditionMap);
                    if(operationMap != null)
                        list.add(getWeldOperationInfo(childLine, conditionMap));
                }
            }
        }
        return list;
    }

    /**
     *  용접공법에 Infomation 을 가져온다
     *
     * @method getWeldOperationInfo
     * @date 2013. 12. 13.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    //@SuppressWarnings("unused")
    private HashMap<String, Object> getWeldOperationInfo(TCComponentBOPLine bopLine, HashMap<String, String> conditionMap) throws Exception {
        TCComponentItem item = bopLine.getItem();
        TCComponentItemRevision itemRevision = bopLine.getItemRevision();
        TCComponent[] releasedItemRevs = item.getRelatedComponents("revision_list");

        TCComponentItemRevision releasedItemRev = null;
        List<String> releasedRevList = new ArrayList<String>();
        for(int i = 0; i < releasedItemRevs.length; i++) {
            String tempRev = releasedItemRevs[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            if(!releasedItemRevs[i].getProperty(SDVPropertyConstant.ITEM_DATE_RELEASED).equals(""))
            {
                releasedRevList.add(tempRev);
                releasedItemRev = (TCComponentItemRevision) releasedItemRevs[i];
            }
        }
        if (releasedRevList.size() == 0) {
            return null;
        }


        String[] propNames = new String[] {
                SDVPropertyConstant.ITEM_ITEM_ID,
                SDVPropertyConstant.ITEM_REVISION_ID,
                SDVPropertyConstant.OPERATION_REV_MECO_NO,
                SDVPropertyConstant.OPERATION_REV_STATION_NO
        };
        String[] propValues =  itemRevision.getProperties(propNames);

        String itemId = propValues[0];
        String itemRev = propValues[1];
        HashMap<String, Object> operationMap = new HashMap<String, Object>();
        operationMap.put("released_rev", releasedRevList.toArray());

        operationMap.put("line_code", lineCode);
        operationMap.put("line_rev", lineRev);
        operationMap.put("weldOP", item.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
        operationMap.put("station_code", stationCode);
        operationMap.put("station_rev", stationRev);

        operationMap.put("item_id", itemId);
        operationMap.put("item_revision_id", itemRev);
        operationMap.put("operation_name_ko", item.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

//        if(releasedItem != null) {
//
//            TCComponent[] releaseStatusList = releasedItemRev.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
//            if(releaseStatusList != null && releaseStatusList.length > 0) {
//                operationMap.put("released_status", "Released");
//            }
//            operationMap.put("publsih_date", SDVStringUtiles.dateToString(publishItemRev.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE), "yyyy-MM-dd"));
//        }

        String variant = bopLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
        if(variant != null && variant.length() > 0)
        {
            variant = (String) SDVBOPUtilities.getVariant(variant).get("printDescriptions");
        }
        else
        {
            //variant = registry.getString("ProcessSheetCommonVariant." + configId);
            variant = "NO Value";
        }
        operationMap.put("variant", variant);
        operationMap.put("meco_id", propValues[2]);

        TCComponentUser user = (TCComponentUser) itemRevision.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
        TCComponentPerson person = (TCComponentPerson) user.getUserInformation().get(0);
        if(person != null)
        {
            operationMap.put("person", person);
            operationMap.put("owner", person.getProperty("user_name"));
        }

        if(checkCondition(operationMap, conditionMap))
        {
            //CustomUtil.findItemRevision(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV, itemRevision., revisionId);
            Date releaseDate = releasedItemRev.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
            if(releaseDate != null)
            {
                operationMap.put("release_date", SDVStringUtiles.dateToString(releaseDate, "yyyy-MM-dd"));
            }

            operationMap.put("operation_name_en", itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME));
            operationMap.put("UID", itemRevision.getUid());
        }
        else
        {
            return null;
        }

        return operationMap;
    }

    private boolean checkCondition(HashMap<String, Object> operationMap, HashMap<String, String> conditionMap) throws TCException
    {
        boolean result = true;

        for(String key : conditionMap.keySet())
        {
            String conditionValue = conditionMap.get(key);
            if("empty_operation".equals(key))
            {
                Object[] revs = (Object[]) operationMap.get("publish_rev");
                if(revs != null && revs.length > 0)
                    result = false;
            }
            else if("different_operation".equals(key))
            {
                Object[] revs = (Object[]) operationMap.get("publish_rev");
                String operationRev = (String) operationMap.get("item_revision_id");
                if(revs == null)
                    result = false;
                else if(revs.length == 0)
                    result = false;
                else if(revs.length > 0)
                {
                    String latestRev = (String) revs[revs.length - 1];
                    if(latestRev.startsWith(operationRev))
                        result = false;
                }
            }
            else if("owner".equals(key))
            {
                TCComponentPerson person = (TCComponentPerson) operationMap.get("person");
                result = checkConditionValue(conditionValue, person.getProperty("user_id"));
                if(!result)
                    result = checkConditionValue(conditionValue, person.getProperty("user_name"));
            }
            else
                result = checkConditionValue(conditionValue, (String) operationMap.get(key));

            if(!result)
                break;
        }

        return result;
    }

    /**
     * 검색 값 중에 * 변환하여 유,무를 리턴한다
     *
     * @method checkConditionValue
     * @date 2013. 12. 13.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean checkConditionValue(String condition, String value)
    {
        boolean result = false;
        condition = condition.toUpperCase();
        value = value.toUpperCase();
        if(condition.startsWith("*") && condition.endsWith("*"))
        {
            if(value.contains(condition.replace("*", "")))
                return true;
        }
        else if(condition.startsWith("*") && !condition.endsWith("*"))
        {
            if(value.endsWith(condition.replace("*", "")))
                return true;
        }
        else if(!condition.startsWith("*") && condition.endsWith("*"))
        {
            if(value.startsWith(condition.replace("*", "")))
                return true;
        }
        else
        {
            if(value.equals(condition))
                return true;
        }

        return result;
    }

}

