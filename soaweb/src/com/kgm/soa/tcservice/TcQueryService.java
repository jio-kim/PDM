package com.kgm.soa.tcservice;

import com.kgm.soa.biz.Session;
import com.kgm.soa.biz.TcItemUtil;
import com.kgm.soa.biz.TcQueryUtil;
import com.kgm.soa.common.constants.PropertyConstant;
import com.kgm.soa.common.constants.SavedQueryConstant;
import com.kgm.soa.common.constants.TcConstants;
import com.kgm.soa.common.constants.TcMessage;
import com.kgm.soa.util.TcUtil;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;

public class TcQueryService {
    //__SYMC_S7_ECO_Revision

    private TcServiceManager tcServiceManager;
    private Session tcSession;
    private TcItemUtil tcItemUtil;
    private TcQueryUtil tcQueryUtil;

    public TcQueryService(Session tcSession) throws Exception {
        this.tcSession = tcSession;
        this.tcItemUtil = new TcItemUtil(this.tcSession);
        this.tcQueryUtil = new TcQueryUtil(this.tcSession);
        tcServiceManager = new TcServiceManager(this.tcSession);
    }

    /**
     * 전체 ECO를 검색한다.
     *
     * @method getAllECOItem
     * @date 2013. 7. 18.
     * @param
     * @return ModelObject[]
     * @exception
     * @throws
     * @see
     */
    public ItemRevision[] getAllECOItem() throws Exception {
        ItemRevision[] revisions = null;
        String[] revisionProperties = {PropertyConstant.ATTR_NAME_ITEMID, "s7_ECO_REGULATION", "s7_CHANGE_REASON", "s7_EFFECT_POINT", "s7_ECO_TYPE", "s7_DESIGN_CONCEPT_NO", "s7_DESIGN_REG_CATG", "s7_ADR_YN", "s7_CANADA_YN", "s7_CHINA_YN", "s7_JAPAN_YN", "s7_DOM_YN", "s7_ECC_YN", "s7_ECE_YN", "s7_GCC_YN", "s7_FMVSS_YN", "s7_OTHERS_YN", "s7_DR_ITEM_YN", "s7_ENV_LAW_YN", "s7_ENV_LAW_DESC", "s7_WEIGHT_CHG_YN", "s7_COST_CHG_YN", "s7_MATERIAL_CHG_YN", "s7_RECYCLING_YN", "s7_SE_YN", "s7_DESIGN_VERIFY", "s7_VEH_DVP_YN", "s7_VEH_DVP_RESULT_YN", "s7_VEH_DVP_RESULT_DESC", "s7_DESIGN_CATG_DESC", "s7_SE_RELATED_DOC", "s7_ECR_NO", "s7_REPRESENTED_PROJECT", "s7_AFFECTED_PROJECT", "s7_ECO_MATURITY", "s7_EFFECT_POINT_DATE", "s7_ECO_KIND", "s7_IF_STAT"};
        String queryName = SavedQueryConstant.SEARCH_GENERAL;
        String[] entries = { "Type" };
        String[] values = { "S7_ECO" };
        SavedQueriesResponse executeSavedQueries = tcServiceManager.getSavedQueryService().executeSavedQueries(tcQueryUtil.setQueryInputforSingle(queryName, entries, values, "site"));
        if (!tcServiceManager.getDataService().ServiceDataError(executeSavedQueries.serviceData)) {
            String[] uids = tcQueryUtil.executeQueryResult(executeSavedQueries)[0].objectUIDS;
            if (uids == null || uids.length == 0) {
                throw new Exception("ECO Item이 없습니다.");
            }
            Item[] items = (Item[])tcServiceManager.getDataService().loadModelObjects(uids);
            revisions = new ItemRevision[items.length];
            tcItemUtil.getProperties(items, new String[] { PropertyConstant.ATTR_NAME_ITEMID, TcConstants.RELATION_REVISION_LIST });
            for (int i = 0; i < items.length; i++) {
                ModelObject[] itemRevisions = items[0].get_revision_list();
                if (itemRevisions == null || itemRevisions.length == 0) {
                    throw new Exception(items[0].get_item_id() + " : This revision does not exist.");
                }
                // 현재 Rev List의 제일 마지막을 Latest Revision 으로 간주
                revisions[i] = (ItemRevision)itemRevisions[itemRevisions.length - 1];
            }
            tcItemUtil.getProperties(revisions, revisionProperties);
            return revisions;
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(executeSavedQueries.serviceData).get(TcMessage.TC_RETURN_FAIL_REASON).toString());
        }
    }

    public ModelObject[] searchTcObject(String queryName, String[] entries, String[] values, String[] properties) throws Exception {

    	ModelObject[] modelObject = null;
    	SavedQueriesResponse executeSavedQueries = tcServiceManager.getSavedQueryService().executeSavedQueries(tcQueryUtil.setQueryInputforSingle(queryName, entries, values, "site"));
    	if (!tcServiceManager.getDataService().ServiceDataError(executeSavedQueries.serviceData)) {
            String[] uids = tcQueryUtil.executeQueryResult(executeSavedQueries)[0].objectUIDS;
            if (uids == null || uids.length == 0) {
                return null;
            }
            modelObject = tcServiceManager.getDataService().loadModelObjects(uids);
            tcItemUtil.getProperties(modelObject, properties);
    	}

    	return modelObject;
    }
}
