package com.kgm.soa.tcservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.kgm.soa.biz.Session;
import com.kgm.soa.biz.TcQueryUtil;
import com.kgm.soa.common.constants.SavedQueryConstant;
import com.kgm.soa.common.constants.TcMessage;
import com.kgm.soa.util.TcUtil;
import com.teamcenter.services.strong.cad.StructureManagementService;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CloseBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelResponse2;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.RelatedObjectTypeAndNamedRefs;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.RelationAndTypesFilter;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.SaveBOMWindowsResponse;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.RevisionRule;

/**
 *
 * Desc :
 *
 * @author yunjae.jung
 */
public class TcStructureManagementService {

    private Session tcSession = null;
    private TcServiceManager tcServiceManager;
    private TcQueryUtil tcQueryUtil;

    public TcStructureManagementService(Session tcSession) {
        this.tcSession = tcSession;
        tcServiceManager = new TcServiceManager(tcSession);
        this.tcQueryUtil = new TcQueryUtil(tcSession);
    }

    public StructureManagementService getService() {
        return StructureManagementService.getService(this.tcSession.getConnection());
    }   

    /**
     * ?��?�� Revision Rule?�� ?��짜�?? ?��?��?��?�� 리턴.
     *
     * @param parentItemRev
     * @param revisionRule
     * @param date
     * @return
     * @throws Exception
     */
    public CreateBOMWindowsResponse createTopLineBOMWindow(ItemRevision parentItemRev, RevisionRule revisionRule, Date date) throws Exception {
        CreateBOMWindowsInfo[] createBOMWindowsInfo = populateBOMWindowInfo(parentItemRev, revisionRule, date);
        CreateBOMWindowsResponse createBOMWindowsResponse = getService().createBOMWindows(createBOMWindowsInfo);
        if (tcServiceManager.getDataService().ServiceDataError(createBOMWindowsResponse.serviceData)) {
            throw new Exception(TcUtil.makeMessageOfFail(createBOMWindowsResponse.serviceData).get(TcMessage.TC_RETURN_FAIL_REASON).toString());
        }
        if (createBOMWindowsResponse.output != null && createBOMWindowsResponse.output.length > 0) {
            return createBOMWindowsResponse;
        } else {
            return null;
        }
    }

    /**
     * Populate BOMWindow Information
     *
     * @method populateBOMWindowInfo
     * @date 2013. 7. 10.
     * @param
     * @return CreateBOMWindowsInfo[]
     * @exception
     * @throws
     * @see
     */
    public static CreateBOMWindowsInfo[] populateBOMWindowInfo(ItemRevision itemRev, RevisionRule revisionRule) {
    	return populateBOMWindowInfo(itemRev, revisionRule);
    }

    /**
     * 리비?�� 룰에 ?��짜�?? ?��?��.
     *
     * @param itemRev
     * @param revisionRule
     * @param date
     * @return
     */
    public static CreateBOMWindowsInfo[] populateBOMWindowInfo(ItemRevision itemRev, RevisionRule revisionRule, Date date) {
        CreateBOMWindowsInfo[] bomInfo = new CreateBOMWindowsInfo[1];
        bomInfo[0] = new CreateBOMWindowsInfo();
        bomInfo[0].itemRev = itemRev;
        if (revisionRule != null) {
            com.teamcenter.services.strong.cad._2007_01.StructureManagement.RevisionRuleConfigInfo revisionRuleConfigInfo = new com.teamcenter.services.strong.cad._2007_01.StructureManagement.RevisionRuleConfigInfo();
			revisionRuleConfigInfo.revRule = revisionRule;
			if( date != null){
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
	            revisionRuleConfigInfo.props.date = cal;
			}
            bomInfo[0].revRuleConfigInfo = revisionRuleConfigInfo;
        }
        return bomInfo;
    }

    /**
     * Save BOMWindow
     *
     * @method saveBOMWindow
     * @date 2013. 5. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void saveBOMWindow(BOMWindow bomWindow) throws Exception {
        SaveBOMWindowsResponse rsp = getService().saveBOMWindows(new BOMWindow[] { bomWindow });
        if (tcServiceManager.getDataService().ServiceDataError(rsp.serviceData)) {
            throw new Exception(TcUtil.makeMessageOfFail(rsp.serviceData).get(TcMessage.TC_RETURN_FAIL_REASON).toString());
        }
    }

    public void closeBOMWindow(BOMWindow bomWindow) throws Exception {
        CloseBOMWindowsResponse rsp = getService().closeBOMWindows(new BOMWindow[] { bomWindow });
        if (tcServiceManager.getDataService().ServiceDataError(rsp.serviceData)) {
            throw new Exception(TcUtil.makeMessageOfFail(rsp.serviceData).get(TcMessage.TC_RETURN_FAIL_REASON).toString());
        }
    }

    public RevisionRule getRevisionRule(String revisionRuleName) throws Exception {
        String queryName = SavedQueryConstant.SEARCH_GENERAL;
        String[] entries = { "Type", "Name" };
        String[] values = { "RevisionRule", revisionRuleName };
        SavedQueriesResponse executeSavedQueries = tcQueryUtil.executeSavedQueries(tcQueryUtil.setQueryInputforSingle(queryName, entries, values, "user"));
        if (!tcServiceManager.getDataService().ServiceDataError(executeSavedQueries.serviceData)) {
            String[] uids = tcQueryUtil.executeQueryResult(executeSavedQueries)[0].objectUIDS;
            if (uids == null || uids.length == 0) {
                throw new Exception("RevisionRule?�� ?��?��?��?��.");
            } else {
                return (RevisionRule)tcServiceManager.getDataService().loadModelObject(uids[0]);
            }
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(executeSavedQueries.serviceData).get(TcMessage.TC_RETURN_FAIL_REASON).toString());
        }
    }

    public BOMLine[] getExpandPSOneLevel(Session session, BOMLine parentBOMLine) throws Exception {
        com.teamcenter.services.strong.cad.StructureManagementService smCadService = com.teamcenter.services.strong.cad.StructureManagementService.getService( session.getConnection() );
        ExpandPSOneLevelInfo oneLevelInfo = new  ExpandPSOneLevelInfo();
        oneLevelInfo.parentBomLines = new BOMLine[1];
        oneLevelInfo.parentBomLines[0] = parentBOMLine;
        oneLevelInfo.excludeFilter = "None2";
        ExpandPSOneLevelPref oneLevelPref = new  ExpandPSOneLevelPref();
        oneLevelPref.info = new RelationAndTypesFilter[1];
        RelationAndTypesFilter relAndTypefilter = new RelationAndTypesFilter();
        relAndTypefilter.relationName = "None2";
        relAndTypefilter.relatedObjAndNamedRefs = new RelatedObjectTypeAndNamedRefs[1];
        relAndTypefilter.relatedObjAndNamedRefs[0] = new RelatedObjectTypeAndNamedRefs();
        relAndTypefilter.relatedObjAndNamedRefs[0].objectTypeName = "None2";
        relAndTypefilter.namedRefHandler = "NoNamedRefs";
        oneLevelPref.info[0] = relAndTypefilter;
        oneLevelPref.expItemRev = true;
        ArrayList<BOMLine> vLineObj = new ArrayList<BOMLine>();
        ExpandPSOneLevelResponse2 oneLevel = smCadService.expandPSOneLevel(oneLevelInfo, oneLevelPref);
        if( oneLevel != null && oneLevel.serviceData.sizeOfPlainObjects() > 0 ) {
            for( int i = 0; i < oneLevel.serviceData.sizeOfPlainObjects(); i++) {
                if( oneLevel.serviceData.getPlainObject(i).getTypeObject().getClassName().equals("BOMLine") ) {
                    BOMLine tmp_child_line = (BOMLine)oneLevel.serviceData.getPlainObject(i);
                    if( tmp_child_line.get_bl_occurrence() != null ) {
                        vLineObj.add(tmp_child_line);
                    }
                }
            }
        }
        return vLineObj.toArray(new BOMLine[vLineObj.size()]);
    }

}
