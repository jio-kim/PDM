/**
 * 
 */
package com.symc.plm.me.sdv.operation.resource;

import java.util.LinkedHashMap;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.SYMTcUtil;

/**
 * Class Name : CreateAssemblyLineDialogOperation
 * Class Description :
 * 
 * @date 2013. 11. 15.
 * 
 */
public class SearchSubsidiaryInitOperation extends AbstractSDVInitOperation {
    private String viewId;

    public SearchSubsidiaryInitOperation(String viewId) {
        this.viewId = viewId;
    }

    @Override
    public void executeOperation() throws Exception {
        LinkedHashMap<String, String> subsidiaryPropMap = new LinkedHashMap<String, String>();

        String itemId = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM, SDVPropertyConstant.ITEM_ITEM_ID);
        String objectName = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM, SDVPropertyConstant.ITEM_OBJECT_NAME);
        String engName = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM, SDVPropertyConstant.SUBSIDIARY_ENG_NAME);
        String materialType = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_MATERIAL_TYPE);
        String subsidiaryGroup = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP);
        String partqual = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_PARTQUAL);
        String specKor = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SPEC_KOR);
        String specEng = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SPEC_ENG);
        String oldPart = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_OLDPART);
        String unitAmount = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT);
        String buyUnit = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_BUY_UNIT);
        String maker = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_MAKER);
        String remark = SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_REMARK);

        subsidiaryPropMap.put(SDVPropertyConstant.ITEM_ITEM_ID, itemId);
        subsidiaryPropMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, objectName);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_ENG_NAME, engName);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_MATERIAL_TYPE, materialType);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP, subsidiaryGroup);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_PARTQUAL, partqual);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR, specKor);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_SPEC_ENG, specEng);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_OLDPART, oldPart);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT, unitAmount);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT, buyUnit);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_MAKER, maker);
        subsidiaryPropMap.put(SDVPropertyConstant.SUBSIDIARY_REMARK, remark);

        RawDataMap targetDataMap = new RawDataMap();
        targetDataMap.put("subsidiaryPropMap", subsidiaryPropMap, IData.OBJECT_FIELD);

        DataSet targetDataset = new DataSet();
        targetDataset.addDataMap(viewId, targetDataMap);

        setData(targetDataset);
    }
}
