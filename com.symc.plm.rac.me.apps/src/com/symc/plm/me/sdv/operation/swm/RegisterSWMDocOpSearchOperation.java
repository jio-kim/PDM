/**
 * 
 */
package com.symc.plm.me.sdv.operation.swm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVQueryUtils;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

/**
 * Class Name : RegisterSWMDocOpSearchOperation
 * Class Description : 공법 검색 Operation
 * 
 * @date 2013. 11. 30.
 * 
 */
public class RegisterSWMDocOpSearchOperation extends AbstractSDVActionOperation {

    public RegisterSWMDocOpSearchOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    private String operationNo;
    private String operationName;
    private String vehicleNo;
    private String shopCode;
    private int selectedValueFromDialog;
    private String shopCodeType;

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        IDataSet dataset = getDataSet();
        IDataMap dataMap = dataset.getDataMap("registerSWMOperationSearchView");
        List<HashMap<String, Object>> dataList;

        selectedValueFromDialog = (Integer) dataMap.get("selectedValue").getValue();

        vehicleNo = dataMap.getStringValue("vehicle_no");
        shopCode = (dataMap.getStringValue("shop_code")).substring(1, 2);

        dataMap = dataset.getDataMap("registerSWMOperationSearchView");
        operationNo = dataMap.getStringValue("operation_no");
        if (operationNo == "") {
            operationNo = "*";
        }

        operationName = dataMap.getStringValue("operation_name");
        if (operationName == "") {
            operationName = "*";
        }

        if (shopCode.equals("B")) {
            shopCodeType = SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV;
        } else if (shopCode.equals("C")) {
            shopCodeType = SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV;
        } else if (shopCode.equals("D")) {
            shopCodeType = SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV;
        }

        if (selectedValueFromDialog == 1) {

            dataList = allRevSearch();

        } else {

            dataList = latestReleasedRevSearch();

        }
        
        IDataMap swmItemListMap = new RawDataMap();
        swmItemListMap.put("swmItemList", dataList, IData.TABLE_FIELD);
        dataset.addDataMap("swmItemList", swmItemListMap);
    }

    /**
     * 공법 검색(itemId, shopCode, vehicleCode 조건으로 검색)
     * 
     * 
     */
	public List<HashMap<String, Object>> allRevSearch() throws TCException, Exception {
		List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

		String[] entryNames = { "ID", "Name", "Vehicle Code", "Type" };
		String[] entryValues = { operationNo, operationName, vehicleNo, shopCodeType };
		TCComponent[] qryResult = SDVQueryUtils.executeSavedQuery("SYMC_Search_Operation_Revision", entryNames, entryValues);
		if (qryResult != null && qryResult.length != 0) {
			for (int i = 0; i < qryResult.length; i++) {
				TCComponentItemRevision itemRevision = (TCComponentItemRevision) qryResult[i];

				dataList.add(getProperty(itemRevision));
			}
		}

		return dataList;
	}

	public List<HashMap<String, Object>> latestReleasedRevSearch() throws TCException, Exception {
		List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

		String[] entryNames = { "ID", "Name", "Vehicle Code", "Type" };
		String[] entryValues = { operationNo, operationName, vehicleNo, shopCodeType };
		TCComponent[] qryResult = SDVQueryUtils.executeSavedQuery("SYMC_Search_Operation_Revision", entryNames, entryValues);
		if (qryResult != null && qryResult.length != 0) {
			for (int i = 0; i < qryResult.length; i++) {
				TCComponentItemRevision itemRevision = (TCComponentItemRevision) qryResult[i];
				if (itemRevision.getItem().getReleasedItemRevisions().length > 0) {
					TCComponentItemRevision latestReleasedRevision = itemRevision.getItem().getReleasedItemRevisions()[0];

					dataList.add(getProperty(latestReleasedRevision));
				}
			}
		}

		return dataList;
	}

    public HashMap<String, Object> getProperty(TCComponentItemRevision itemRevision) throws TCException {
        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        // 공법 NO.
        dataMap.put(SDVPropertyConstant.ITEM_ITEM_ID, itemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));

        // Rev
        dataMap.put(SDVPropertyConstant.ITEM_REVISION_ID, itemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID));

        // 공법명
        dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, itemRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

        return dataMap;
    }

}
