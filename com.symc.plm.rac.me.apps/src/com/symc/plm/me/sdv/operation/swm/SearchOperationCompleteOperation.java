/**
 * 
 */
package com.symc.plm.me.sdv.operation.swm;

import java.util.Map;

import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

/**
 * Class Name : SearchOperationCompleteOperation
 * Class Description :
 * 
 * @date 2013. 12. 10.
 * 
 */
public class SearchOperationCompleteOperation extends AbstractSDVActionOperation {

    public SearchOperationCompleteOperation(int actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
    }

    public SearchOperationCompleteOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
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
        if (dataset.containsMap("registerSWMOperationSearchResultView")) {
            IDataMap dataMap = dataset.getDataMap("registerSWMOperationSearchResultView");
            if (dataMap != null) {
                // String targetId = getTargetId();
                String targetId = dataMap.getStringValue("targetId");
                String[] targetInfo = targetId.split("/");
                if (targetInfo.length > 1) {
                    targetId = targetInfo[targetInfo.length - 1];
                }

                dataset.addDataMap(targetId, dataMap);
            }
        }

        setDataSet(dataset);
    }

}
