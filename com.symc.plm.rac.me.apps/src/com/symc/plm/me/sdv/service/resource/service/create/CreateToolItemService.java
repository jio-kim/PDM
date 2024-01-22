/**
 * 
 */
package com.symc.plm.me.sdv.service.resource.service.create;

import java.util.HashMap;
import java.util.Map;

import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.sdv.validate.ResourceCreateSDVValidator;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * Class Name : CreateToolItem
 * Class Description :
 * 
 * @date 2013. 12. 16.
 * 
 */
public class CreateToolItemService extends AbstractResourceCreaeItemService {

    /**
     * @param datamap
     * @throws Exception
     */
    public CreateToolItemService(IDataMap datamap) throws Exception {
        super(datamap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.resource.service.create.AbstractResourceCreaeItemService#validate(org.sdv.core.common.data.IDataMap)
     */
    @Override
    @SuppressWarnings({ "unchecked", "serial", "rawtypes" })
    void validate() throws Exception {
        ResourceCreateSDVValidator resourceCreateValidator = new ResourceCreateSDVValidator();
        resourceCreateValidator.validate(null, (Map) new HashMap<String, IDataMap>() {
            {
                put("datamap", datamap);
            }
        }, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.resource.service.create.AbstractResourceCreaeItemService#create()
     */
    @Override
    @SuppressWarnings("unchecked")
    public TCComponentItemRevision create() throws Exception {
        Boolean createMode = (Boolean) datamap.getValue("createMode");
        String itemTCCompType = datamap.getStringValue("itemTCCompType");
        Map<String, String> itemProperties = (Map<String, String>) datamap.getValue("itemProperties");
        Map<String, String> revisionProperties = (Map<String, String>) datamap.getValue("revisionProperties");

        RawDataMap fileMap = (RawDataMap) datamap.getValue("File");

        // Item, ItemRevision 생성
        TCComponentItemRevision itemRevision = ResourceUtilities.createItem(createMode, itemTCCompType, itemProperties, revisionProperties);

        // Dataset 생성
        ResourceUtilities.createAndAddDataset(fileMap, itemRevision);

        // Relaese
        TCComponent processComponent = ResourceUtilities.releaseItemRevision(itemRevision);
        if (processComponent == null) {
            // Release에 실패하였습니다.
            throw new Exception(registry.getString("Release.Check.MSG"));
        }

        // Classified
        if (createMode) {
            ClassifyService classfiService = new ClassifyService();
            classfiService.classifyResource(itemRevision.getItem());
        }

        return itemRevision;
    }
}
