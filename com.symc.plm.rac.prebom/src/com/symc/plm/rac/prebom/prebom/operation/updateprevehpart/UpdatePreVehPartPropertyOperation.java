package com.symc.plm.rac.prebom.prebom.operation.updateprevehpart;

import java.util.HashMap;

import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.ccn.operation.CCNProcessOperation;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

public class UpdatePreVehPartPropertyOperation extends AbstractAIFOperation {
    private TCComponentItemRevision targetRevision;
    private HashMap<String, Object> propMap;
    private HashMap<String, Object> ccnPropMap;
    private TCSession session;
    public static String AUTORELEASECCNTYPE = "02";
    private String errorMessage;

    public UpdatePreVehPartPropertyOperation(TCComponentItemRevision targetRevision, HashMap<String, Object> propMap, HashMap<String,Object> ccnPropMap) {
        this.targetRevision = targetRevision;
        this.propMap = propMap;
        this.ccnPropMap = ccnPropMap;
        this.session = targetRevision.getSession();
    }

    @Override
    public void executeOperation() throws Exception {
        Markpoint mp = new Markpoint(session);

        try
        {
            if (targetRevision == null || ! targetRevision.isValidUid())
            {
                throw new NullPointerException("Target Item Revision is invalid.");
            }

            // CCN 생성
            TCComponentItemRevision ccnRevision = SDVPreBOMUtilities.createCCNItem(AUTORELEASECCNTYPE, ccnPropMap);
            // Target ItemRevision Revise
            targetRevision = reviseTargetItemRevision(ccnRevision, targetRevision, propMap);
            // Workflow complete
            CCNProcessOperation ccnOp = new CCNProcessOperation(session, (TCComponentChangeItemRevision) ccnRevision, false);
            ccnOp.executeOperation();
//            SDVPreBOMUtilities.selfRelease(ccnRevision, "CSR");

            storeOperationResult("Success");
            mp.forget();
        } catch (Exception ex) {
            mp.rollBack();
            setAbortRequested(true);
            storeOperationResult("Failed");
            errorMessage = ex.getMessage();
            throw ex;
        }
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    private TCComponentItemRevision reviseTargetItemRevision(TCComponentItemRevision ccnRevision, TCComponentItemRevision reviseRevision, HashMap<String,Object> props) throws Exception
    {
        try
        {
            TCComponentItemRevision oldRevision = reviseRevision;
            if (! SYMTcUtil.isReleased(oldRevision)) {
                throw new Exception("target revision was not released. \n[" + reviseRevision.toString() + "]");
            }

            String newRevId = SDVPreBOMUtilities.getNextRevID(oldRevision.getItem(), "Item");
            TCComponentItemRevision newRevision = oldRevision.saveAs(newRevId);

            if (newRevision.isValidPropertyName(PropertyConstant.ATTR_NAME_MATURITY))
                newRevision.setProperty(PropertyConstant.ATTR_NAME_MATURITY, "In Work");

            for (String prop : props.keySet())
            {
                Object value = props.get(prop);
                if (value instanceof String)
                    newRevision.setProperty(prop, (String) value);
                else if (value instanceof Double)
                    newRevision.setDoubleProperty(prop, (Double) value);
                else if (value instanceof Boolean)
                    newRevision.setLogicalProperty(prop, (Boolean) value);
            }

            // CCN에 연결하기
            ccnRevision.add(TypeConstant.CCN_PROBLEM_ITEM, oldRevision);
            ccnRevision.add(TypeConstant.CCN_SOLUTION_ITEM, newRevision);

            newRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO, ccnRevision);
            newRevision.save();

            return newRevision;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }


}
