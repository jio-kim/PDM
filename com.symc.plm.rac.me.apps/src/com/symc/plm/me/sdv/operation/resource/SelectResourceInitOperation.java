/**
 * 
 */
package com.symc.plm.me.sdv.operation.resource;

import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.soa.client.model.LovValue;

/**
 * Class Name : CreateAssemblyLineDialogOperation
 * Class Description :
 * 
 * @date 2013. 11. 15.
 * 
 */
public class SelectResourceInitOperation extends AbstractSDVInitOperation {
    private String viewId;
    private Registry registry;

    public SelectResourceInitOperation(String viewId) {
        this.viewId = viewId;
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.resource.resource");
    }

    @Override
    public void executeOperation() throws Exception {
        String[] arrViewId = viewId.split(":");
        String bopType = arrViewId[0].substring(0, 1);
        String resourceType = arrViewId[1].toUpperCase();

        List<LovValue> shopLovList = SDVLOVUtils.getLOVValues(registry.getString("ResourceShop.LOV.NAME"));
        List<LovValue> resourceTypeLovList = SDVLOVUtils.getLOVValues(registry.getString("ResourceType.LOV.NAME"));
        List<LovValue> resourceCategoryLovList = SDVLOVUtils.getLOVValues(registry.getString(ResourceUtilities.getDefaultLovName(bopType, resourceType)));

        for (LovValue lov : shopLovList) {
            lov.getDescription();
            lov.getDisplayValue();
        }

        RawDataMap targetDataMap = new RawDataMap();
        targetDataMap.put("shopLovList", shopLovList, IData.LIST_FIELD);
        targetDataMap.put("resourceTypeLovList", resourceTypeLovList, IData.LIST_FIELD);
        targetDataMap.put("resourceCategoryLovList", resourceCategoryLovList, IData.LIST_FIELD);

        DataSet targetDataset = new DataSet();
        targetDataset.addDataMap("SelectResourceViewPane", targetDataMap);

        setData(targetDataset);
    }
}
