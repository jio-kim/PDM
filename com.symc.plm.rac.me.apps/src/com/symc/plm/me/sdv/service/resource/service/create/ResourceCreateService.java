/**
 * 
 */
package com.symc.plm.me.sdv.service.resource.service.create;

import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * 자원(Resource) Item 생성 서비스
 * 
 * Class Name : ResourceCreateService
 * Class Description :
 * 
 * @date 2013. 12. 16.
 * 
 */
public class ResourceCreateService {

    /**
     * Create Item
     * 
     * @method createResourceItem
     * @date 2013. 12. 16.
     * @param
     * @return TCComponentItem
     * @exception
     * @throws
     * @see
     */
    public static TCComponentItem createResourceItem(IDataMap datamap) throws Exception {
        String itemTCCompType = (String) datamap.getValue("itemTCCompType");
        TCComponentItemRevision resourceItemRevision = null;
        AbstractResourceCreaeItemService createItemService = null;
        // create EQUIPMENT
        if (SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM.equals(itemTCCompType)) {
            createItemService = new CreateEquipmentItemService(datamap);
            resourceItemRevision = createItemService.create();
        } else if (SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM.equals(itemTCCompType)) {
            createItemService = new CreateEquipmentItemService(datamap);
            resourceItemRevision = createItemService.create();
        } else if (SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM.equals(itemTCCompType)) {
            createItemService = new CreateEquipmentItemService(datamap);
            resourceItemRevision = createItemService.create();
        } else if (SDVTypeConstant.BOP_PROCESS_GUN_ITEM.equals(itemTCCompType)) {
            createItemService = new CreateEquipmentItemService(datamap);
            resourceItemRevision = createItemService.create();
        }
        // create TOOL
        else if (SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(itemTCCompType)) {
            createItemService = new CreateToolItemService(datamap);
            resourceItemRevision = createItemService.create();
        }
        // create Subsidiary
        else if (SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM.equals(itemTCCompType)) {
            createItemService = new CreateSubsidiaryItemService(datamap);
            resourceItemRevision = createItemService.create();
        } else {
            throw new ValidateSDVException("Create Resource Item Type is not valide - " + "");
        }
        return resourceItemRevision.getItem();
    }
}
