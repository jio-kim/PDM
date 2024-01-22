/**
 * 
 */
package com.symc.plm.me.sdv.service.resource.service.create;

import org.apache.commons.lang.StringUtils;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.classification.ClassificationService;
import com.teamcenter.services.rac.classification._2007_01.Classification.ClassificationObject;

/**
 * 자원(Resource) Item 생성 서비스
 * 
 * Class Name : ResourceCreateService
 * Class Description :
 * 
 * @date 2013. 12. 16.
 * 
 */
public class ClassifyService {

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
    public void classifyResource(TCComponentItem item) throws Exception {
        ClassificationService classificationService = ClassificationService.getService((TCSession) AIFUtility.getSessionManager().getDefaultSession());

        TCComponent wsoItem = (TCComponent) item;
        String classID = getClassID(item);
        
        if (wsoItem != null && classID != null) {
            // Item을 ClassificationObject Item 형식으로 변경
            ClassificationObject classifiedItem = new ClassificationObject();
            classifiedItem.classId = classID;
            classifiedItem.instanceId = item.getProperty("item_id");
            classifiedItem.unitBase = "METRIC";
            classifiedItem.wsoId = wsoItem;
            classificationService.createClassificationObjects(new ClassificationObject[] { classifiedItem });
        }
    }

    /**
     * @param item
     */
    public String getClassID(TCComponentItem item) {
        String classID = null;
        String itemType = item.getType();

        if (StringUtils.containsIgnoreCase(itemType, "general")) {
            classID = "GEN";
        } else if (StringUtils.containsIgnoreCase(itemType, "robot")) {
            classID = "ROB";
        } else if (StringUtils.containsIgnoreCase(itemType, "gun")) {
            classID = "GUN";
        } else if (StringUtils.containsIgnoreCase(itemType, "jig")) {
            classID = "JIG";
        } else if (StringUtils.containsIgnoreCase(itemType, "tool")) {
            classID = "TOOL";
        } else if (StringUtils.containsIgnoreCase(itemType, "subsidiary")) {
            classID = "SUBSIDIARY";
        }

        return classID;
    }
}
