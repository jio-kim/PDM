package com.ssangyong.soa.biz;

import java.math.BigInteger;
import java.util.ArrayList;

import com.ssangyong.soa.common.util.StringUtil;
import com.ssangyong.soa.tcservice.TcServiceManager;
import com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedConfigParameters;
import com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedInputData;
import com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedOutputData;
import com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedParentInfo;
import com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

public class TcDataUtil {
    @SuppressWarnings("unused")
    private Session tcSession = null;
    private TcServiceManager tcServiceManager;

    public TcDataUtil(Session tcSession) {
        this.tcSession = tcSession;
        tcServiceManager = new TcServiceManager(tcSession);
    }

    //TODOS ?��기�?? 바꿨?��.
    @SuppressWarnings("unchecked")
	public ItemRevision[] whereUsed(String rev_puid, int numLevels, boolean whereUsedPrecise, String revisionRuleName) throws Exception {
        ArrayList<ItemRevision> resultParentRevisions = new ArrayList<ItemRevision>();
        ModelObject[] modeComps = new ModelObject[1];
        modeComps[0] = tcServiceManager.getDataService().loadModelObject(rev_puid);
        tcServiceManager.getDataService().refreshObjects(modeComps[0]);
        ModelObject revisionRuleComp = (StringUtil.isEmpty(revisionRuleName))?null:tcServiceManager.getStructureService().getRevisionRule(revisionRuleName);

        WhereUsedInputData []whereUsedInput = new WhereUsedInputData[1];
        whereUsedInput[0] = new WhereUsedInputData();
        whereUsedInput[0].clientId = rev_puid + "-whereUsed";
        whereUsedInput[0].inputObject = (WorkspaceObject) modeComps[0];
        whereUsedInput[0].inputParams = new WhereUsedConfigParameters();

        whereUsedInput[0].inputParams.boolMap.put("whereUsedPreciseFlag", whereUsedPrecise);
        whereUsedInput[0].inputParams.intMap.put("numLevels", new BigInteger(String.valueOf(numLevels)));
        whereUsedInput[0].inputParams.tagMap.put("revision_rule", revisionRuleComp);

        WhereUsedResponse whereusedResponse = tcServiceManager.getDataService().whereUsed(whereUsedInput, whereUsedInput[0].inputParams);
        if (!tcServiceManager.getDataService().ServiceDataError(whereusedResponse.serviceData)) {
        	WhereUsedOutputData[] whereusedouts = whereusedResponse.output;
        	if (whereusedouts == null)
        		return new ItemRevision[0];

        	for (WhereUsedOutputData whereusedout : whereusedouts) {
        		WhereUsedParentInfo []parentInfos = whereusedout.info;
        		for (WhereUsedParentInfo parentInfo : parentInfos) {
        			if (parentInfo.parentObject instanceof ItemRevision)
        			{
        				tcServiceManager.getDataService().refreshObjects(parentInfo.parentObject);
        				resultParentRevisions.add((ItemRevision) parentInfo.parentObject);
        			}
        		}
        	}
        }

/*        WhereUsedResponse whereusedresp = tcServiceManager.getDataService().whereUsed(modeComps, numLevels, whereUsedPrecise, revisionRuleComp);
        if (!tcServiceManager.getDataService().ServiceDataError(whereusedresp.serviceData)) {
            WhereUsedOutput[] whereusedouts = whereusedresp.output;
            WhereUsedParentInfo[] whereusedparentinfos = null;
            int level;
            ItemRevision parentRevision;
            if (whereusedouts == null) {
                return new ItemRevision[0];
            }
            for (WhereUsedOutput whereusedout : whereusedouts) {
                whereusedparentinfos = whereusedout.info;
                for (WhereUsedParentInfo whereusedparentinfo : whereusedparentinfos) {
                    level = whereusedparentinfo.level;
                    parentRevision = whereusedparentinfo.parentItemRev;
                    tcServiceManager.getDataService().refreshObjects(parentRevision);
                    //tcItemUtil.getProperties(new ModelObject[] { parentRevision }, new String[] { TcConstants.PROP_ITEM_ID, TcConstants.PROP_ITEM_REVISION_ID, TcConstants.PROP_OBJECT_DESC, TcConstants.PROP_LAST_RELEASE_STATUS, TcConstants.PROP_LAST_MOD_DATE, TcConstants.PROP_LAST_MOD_USER });
                    resultParentRevisions.add(parentRevision);
                }
            }
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(whereusedresp.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
*/        return resultParentRevisions.toArray(new ItemRevision[resultParentRevisions.size()]);
    }
}
