/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.dialog.revise;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

/**
 * @author jinil
 *
 */
public class PreReviseOperation extends AbstractAIFOperation {
    private boolean isBOMLine;
    private boolean isCCNRequired;
    private TCComponent ccnObject;
    private TCComponent[] targetObjects;
    private String changeDesc;

    public PreReviseOperation(HashMap<String, Object> param) {
        isBOMLine = (boolean) param.get("TargetBOMLine");
        isCCNRequired = (boolean) param.get("CCNRequired");
        ccnObject = (TCComponent) param.get("CCNObject");
        targetObjects = (TCComponent []) param.get("TargetRevisions");
        changeDesc = (String) param.get("ChangeDesc");
    }

    /* (non-Javadoc)
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try
        {
            if (targetObjects == null || targetObjects.length == 0)
            {
                setAbortRequested(true);
                throw new NullPointerException("target TCComponent is null.");
            }

            if (isCCNRequired && ccnObject == null)
            {
                setAbortRequested(true);
                throw new NullPointerException("CCN Object is null.");
            }

            ArrayList<TCComponent> toReviseList = new ArrayList<TCComponent>();
            if (isBOMLine)
            {
                for (TCComponent targetObject : targetObjects)
                    toReviseList.add(((TCComponentBOMLine) targetObject).getItemRevision());
            }
            else
            {
                for (TCComponent targetObject : targetObjects)
                    toReviseList.add(targetObject);
            }

            TCSession session = CustomUtil.getTCSession();
            // Mark Point
            Markpoint mp = new Markpoint(session);

            try
            {
                int i = 0;
                for (TCComponent toReviseRev : toReviseList)
                {
                    if (isAbortRequested()) {
                        return;
                    }

                    TCComponentItemRevision oldRevision = (TCComponentItemRevision) toReviseRev;

                    if (! SYMTcUtil.isReleased(oldRevision)) {
                        continue;
                    }
//                    String newRevId = oldRevision.getItem().getNewRev();
                    String newRevId = SDVPreBOMUtilities.getNextRevID(oldRevision.getItem(), "Item");
                    String itemType = oldRevision.getType();

                    TCComponent refComp = null;
                    if (itemType.equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE))
                    {
                        refComp = oldRevision.getRelatedComponent(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF);
                    }
                    // TCComponentItemRevision newRevision = oldRevision.getItem().revise(newRevId, oldRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), oldRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
                    TCComponentItemRevision newRevision = oldRevision.saveAs(newRevId);

                    if (itemType.equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE))
                    {
                        String[] prop = new String[]{PropertyConstant.ATTR_NAME_EST_COST_MATERIAL, PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL};
                        String[] val = new String[]{refComp.getProperty(PropertyConstant.ATTR_NAME_EST_COST_MATERIAL), refComp.getProperty(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL)};

                        TCComponent newRefComp = SYMTcUtil.createApplicationObject(newRevision.getSession(), TypeConstant.S7_PREVEHTYPEDREFERENCE, prop, val);

                        newRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF, newRefComp);
                        newRevision.save();
                    }

                    if (newRevision.isValidPropertyName(PropertyConstant.ATTR_NAME_MATURITY))
                        newRevision.setProperty(PropertyConstant.ATTR_NAME_MATURITY, "In Work");
                    if (changeDesc != null && changeDesc.trim().length() > 0)
                        newRevision.setProperty(PropertyConstant.ATTR_NAME_ITEMDESC, changeDesc);
                    newRevision.save();

                    if (isCCNRequired && ccnObject != null)
                    {
                        // CCN에 연결하기
                        ((TCComponentChangeItemRevision) ccnObject).add(TypeConstant.CCN_PROBLEM_ITEM, oldRevision);
                        ((TCComponentChangeItemRevision) ccnObject).add(TypeConstant.CCN_SOLUTION_ITEM, newRevision);

                        newRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO, ccnObject);
                        newRevision.save();
                    }

                    if (isBOMLine)
                    {
                        // BOMLine 일 경우 Refresh
                        ((TCComponentBOMLine) targetObjects[i]).window().newIrfWhereConfigured(newRevision);
                        ((TCComponentBOMLine) targetObjects[i]).window().fireChangeEvent();

                        i = i + 1;
                    }
                }
            }
            catch (Exception ex)
            {
                setAbortRequested(true);
                mp.rollBack();
                throw ex;
            }
            mp.forget();
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

}
