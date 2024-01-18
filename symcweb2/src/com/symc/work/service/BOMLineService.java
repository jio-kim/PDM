package com.symc.work.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;

import com.symc.common.exception.NotLoadedChildLineException;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcDataUtil;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.soa.util.TcUtil;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.model.EcoWhereUsedVO;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsInfo;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsOutput;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsPref;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsResponse2;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelResponse2;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.RelatedObjectTypeAndNamedRefs;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.RelationAndTypesFilter;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.ReleaseStatus;
import com.teamcenter.soa.client.model.strong.RevisionRule;

public class BOMLineService {
    private Session session;
    private TcServiceManager tcServiceManager;
    private com.teamcenter.services.strong.cad.StructureManagementService smCadService;
    private TcItemUtil tcItemUtil;
    private TcDataUtil tcDataUtil;

    public static final String[] DEFAULT_BOMLINE_PROPERTIES = { "bl_child_lines", "bl_parent", "bl_revision", "bl_occurrence_uid", "bl_plmxml_abs_xform", "bl_plmxml_occ_xform", "S7_SUPPLY_MODE", "bl_variant_condition", "bl_rev_mvl_text", "bl_sequence_no", "S7_MODULE_CODE", "S7_POSITION_DESC","S7_ALTER_PART" };
    public static final String[] DEFAULT_ITEM_REVISION_PROPERTIES = { "item_id", "item_revision_id", "object_name" };
    public static final String[] S7_VEH_PART_ITEM_REVISION = { "s7_KOR_NAME" };

    public BOMLineService(Session session) {
        this.session = session;
        tcServiceManager = new TcServiceManager(this.session);
        tcItemUtil = new TcItemUtil(this.session);
        tcDataUtil = new TcDataUtil(this.session);
        smCadService = com.teamcenter.services.strong.cad.StructureManagementService.getService(this.session.getConnection());
    }

    /**
     *
     *
     * @method getChildrenBomLine
     * @date 2013. 7. 11.
     * @param
     * @return void
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public BOMLine[] getChildrenBomLine(BOMLine parentBomLine) throws Exception {
        ArrayList<BOMLine> childBOMLines = new ArrayList<BOMLine>();
        // 하위 1level 전개를 해서, 삭제대상 bomline을 가져옴.
        ExpandPSOneLevelInfo oneLevelInfo = new ExpandPSOneLevelInfo();
        oneLevelInfo.parentBomLines = new BOMLine[1];
        oneLevelInfo.parentBomLines[0] = parentBomLine;
        oneLevelInfo.excludeFilter = "None2";
        ExpandPSOneLevelPref oneLevelPref = new ExpandPSOneLevelPref();
        oneLevelPref.info = new RelationAndTypesFilter[1];
        RelationAndTypesFilter relAndTypefilter = new RelationAndTypesFilter();
        relAndTypefilter.relationName = "None2"; // View??
        relAndTypefilter.relatedObjAndNamedRefs = new RelatedObjectTypeAndNamedRefs[1];
        relAndTypefilter.relatedObjAndNamedRefs[0] = new RelatedObjectTypeAndNamedRefs();
        // relAndTypefilter.relatedObjAndNamedRefs[0].objectTypeName =
        // "UGMASTER";
        relAndTypefilter.relatedObjAndNamedRefs[0].objectTypeName = "None2";
        // relAndTypefilter.relatedObjAndNamedRefs[1] = new
        // RelatedObjectTypeAndNamedRefs();
        // relAndTypefilter.relatedObjAndNamedRefs[1].objectTypeName = "UGPART";
        relAndTypefilter.namedRefHandler = "NoNamedRefs";
        oneLevelPref.info[0] = relAndTypefilter;
        oneLevelPref.expItemRev = true;

        ExpandPSOneLevelResponse2 oneLevel = smCadService.expandPSOneLevel(oneLevelInfo, oneLevelPref);
        if (oneLevel != null && oneLevel.serviceData.sizeOfPlainObjects() > 0) {
            for (int i = 0; i < oneLevel.serviceData.sizeOfPlainObjects(); i++) {
                if (oneLevel.serviceData.getPlainObject(i).getTypeObject().getClassName().equals("BOMLine")) {
//                    System.out.println("class name : " + oneLevel.serviceData.getPlainObject(i).getTypeObject().getClassName());
//                    System.out.println("uid : " + oneLevel.serviceData.getPlainObject(i).getUid());
                    // BOMLine이 Parent와 같으면 Skip
                    if (parentBomLine.getUid().equals(oneLevel.serviceData.getPlainObject(i).getUid())) {
                        continue;
                    } else {
                        childBOMLines.add((BOMLine) oneLevel.serviceData.getPlainObject(i));
                    }
                }
            }
        }
        if (childBOMLines.size() > 0) {
            return childBOMLines.toArray(new BOMLine[childBOMLines.size()]);
        } else {
            return null;
        }
    }

    /**
     * ExpandAll - SOA API
     * (주의 : 현재 이메소드는
     *
     *   java.lang.Exception: [TC_ERR_CODE]: 215028
     *       [TC_ERR_LEV]: 3
     *       [TC_ERR_MSG]: Null return from find relation.
     *
     *  에러가 발생하므로 사용금지)
     * @deprecated
     * @method getExpandAllBomLine
     * @date 2013. 7. 16.
     * @param
     * @return ExpandPSAllLevelsOutput[]
     * @exception
     * @throws
     * @see
     */
    public ExpandPSAllLevelsOutput[] getExpandAllBomLine(BOMLine parentBomLine) throws Exception {
        // 하위 1level 전개를 해서, 삭제대상 bomline을 가져옴.
        ExpandPSAllLevelsInfo levelInfo = new ExpandPSAllLevelsInfo();
        levelInfo.parentBomLines = new BOMLine[] {parentBomLine};
        levelInfo.excludeFilter = "None2";
        ExpandPSAllLevelsPref levelPref = new ExpandPSAllLevelsPref();
        levelPref.info = new RelationAndTypesFilter[1];
        RelationAndTypesFilter relAndTypefilter = new RelationAndTypesFilter();
        relAndTypefilter.relationName = "None2";
        relAndTypefilter.relatedObjAndNamedRefs = new RelatedObjectTypeAndNamedRefs[1];
        relAndTypefilter.relatedObjAndNamedRefs[0] = new RelatedObjectTypeAndNamedRefs();
        // relAndTypefilter.relatedObjAndNamedRefs[0].objectTypeName =
        // "UGMASTER";
        relAndTypefilter.relatedObjAndNamedRefs[0].objectTypeName = "None2";
        // relAndTypefilter.relatedObjAndNamedRefs[1] = new
        // RelatedObjectTypeAndNamedRefs();
        // relAndTypefilter.relatedObjAndNamedRefs[1].objectTypeName = "UGPART";
        relAndTypefilter.namedRefHandler = "NoNamedRefs";
        levelPref.info[0] = relAndTypefilter;
        levelPref.expItemRev = true;

        ExpandPSAllLevelsResponse2 allLevelResponse = smCadService.expandPSAllLevels(levelInfo, levelPref);
        if (!tcServiceManager.getDataService().ServiceDataError(allLevelResponse.serviceData)) {
            ExpandPSAllLevelsOutput[] allLevels = allLevelResponse.output;
            return allLevels;
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(allLevelResponse.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }

    /**
     * Revision을가지고 BOMWindow 생성
     *
     * @method getCreateBOMWindow
     * @date 2013. 7. 16.
     * @param
     * @return BOMLine
     * @exception
     * @throws
     * @see
     */
    public BOMWindow getCreateBOMWindow(ItemRevision ir, String revisionRuleStr, Date date) throws Exception {
        RevisionRule revisionRule = null;
        if (!"".equals(StringUtil.nullToString(revisionRuleStr))) {
            revisionRule = tcServiceManager.getStructureService().getRevisionRule(revisionRuleStr);
        }
        if (!"".equals(StringUtil.nullToString(revisionRuleStr)) && revisionRule == null) {
            throw new Exception("Expand All 대상 RevisionRule이 정확하지않습니다. RevisionRule을 확인하세요.");
        }
        CreateBOMWindowsResponse createBOMWindowsResponse = tcServiceManager.getStructureService().createTopLineBOMWindow(ir, revisionRule, date);
        if (!tcServiceManager.getDataService().ServiceDataError(createBOMWindowsResponse.serviceData)) {
            return createBOMWindowsResponse.output[0].bomWindow;
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(createBOMWindowsResponse.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }

    /**
     * ExpandAll By using SOA
     *
     * @deprecated
     * @method expandAllLevelCustom
     * @date 2013. 7. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void expandAllLevelCustom(BOMLine bomLine) throws Exception {
        BOMLine[] child = this.getChildrenBomLine(bomLine);
        for (int i = 0; child != null && i < child.length; i++) {
            // tcItemUtil.getProperties(new ModelObject[] { child[i] }, new
            // String[] { "bl_revision", "s7_KOR_NAME" });
            ItemRevision childItemRevision = (ItemRevision) child[i].get_bl_revision();
            // RevisionProperties setting
            if (childItemRevision == null) {
                System.out.println();
            }
            tcItemUtil.getProperties(new ModelObject[] { childItemRevision }, new String[] { "item_id" });
            System.out.println("Item_ID :  >> " + childItemRevision.get_item_id());
            this.expandAllLevelCustom(child[i]);
        }
    }

    /**
     * ExpandAll By using BOMLine property
     *
     * @method expandAllLevel
     * @date 2013. 7. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public ArrayList<ModelObject> expandAllLevel(ModelObject bomLine, ArrayList<ModelObject> bomlineList) throws Exception {

    	try{
	        // BOMLine 속성 Setting..
	        tcItemUtil.getProperties(new ModelObject[] { bomLine }, DEFAULT_BOMLINE_PROPERTIES);
    	}catch(Exception e){
    		tcItemUtil.getProperties(new ModelObject[]{bomLine}, new String[]{"bl_revision"});
    		ItemRevision tRev = (ItemRevision)((BOMLine)bomLine).get_bl_revision();
    		tcItemUtil.getProperties(new ModelObject[]{tRev}, BOMLineService.DEFAULT_ITEM_REVISION_PROPERTIES);
    		System.out.println("에러 아이템 아이디 : " + tRev.get_item_id() + "/" + tRev.get_item_revision_id());

    		NotLoadedChildLineException exception = new NotLoadedChildLineException(e);
    		exception.addInfo(NotLoadedChildLineException.ITEM_ID, tRev.get_item_id());
    		exception.addInfo(NotLoadedChildLineException.ITEM_REVISION_ID, tRev.get_item_revision_id());

    		throw exception;
    	}
        ItemRevision bomLineItemRev = (ItemRevision) bomLine.getPropertyObject("bl_revision").getModelObjectValue();
        if(bomLineItemRev == null) {
            return bomlineList;
        }
        // ItemProperties 속성 Setting..
        tcItemUtil.getProperties(new ModelObject[] { bomLineItemRev }, DEFAULT_ITEM_REVISION_PROPERTIES);
        bomlineList.add(bomLine);
        ModelObject[] child = bomLine.getPropertyObject("bl_child_lines").getModelObjectArrayValue();
        //ModelObject[] child = this.getChildrenBomLine((BOMLine)bomLine);
        for (int i = 0; child != null && i < child.length; i++) {
            ItemRevision childItemRevision = (ItemRevision) child[i].getPropertyObject("bl_revision").getModelObjectValue();
            if (childItemRevision == null) {
                continue;
            }
            this.expandAllLevel(child[i], bomlineList);
        }
        /*
        BOMLine[] childs = tcServiceManager.getStructureService().getExpandPSOneLevel(this.session, (BOMLine)bomLine);
        for (int i = 0; i < childs.length; i++) {
            ItemRevision childItemRevision = (ItemRevision) childs[i].getPropertyObject("bl_revision").getModelObjectValue();
            if (childItemRevision == null) {
                continue;
            }
            // Fuction Mast Item Revision이면 continue 하여 하위 전개를 중단한다.
            if((IFConstants.CLASS_TYPE_S7_FUNCTION_MAST_REVISION).equals(childItemRevision.getTypeObject().getClassName())) {
                continue;
            }
            this.expandAllLevel(childs[i], bomlineList);
        }
        */
        return bomlineList;
    }

    /**
     * ExpandAll Function By using BOMLine property
     *
     * Fuction ItemRevision 까지만 전개한다.
     *
     * HashMap<String, BOMLine> bomlineItemRevMap
     *    String : rev Puid
     *    BOMLine : rev Puid를 가지고있는 BOMLine
     * -> 같은 Revision을 가지는 BOMLine이 여러개 나와도 문제 되지않음. (Variant Option 검색으로만 사용하기 때문)
     *
     * @method expandAllLevel
     * @date 2013. 7. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void expandAllLevelFuction(ModelObject bomLine, HashMap<String, BOMLine> bomlineItemRevMap) throws Exception {
    	try{
	        // BOMLine 속성 Setting..
	        tcItemUtil.getProperties(new ModelObject[] { bomLine }, DEFAULT_BOMLINE_PROPERTIES);
	    }catch(Exception e){
	    	e.printStackTrace();
	        tcItemUtil.getProperties(new ModelObject[]{bomLine}, new String[]{"bl_revision"});
    		ItemRevision tRev = (ItemRevision)((BOMLine)bomLine).get_bl_revision();
    		tcItemUtil.getProperties(new ModelObject[]{tRev}, BOMLineService.DEFAULT_ITEM_REVISION_PROPERTIES);
    		System.out.println("에러 아이템 아이디 : " + tRev.get_item_id() + "/" + tRev.get_item_revision_id());

    		NotLoadedChildLineException exception = new NotLoadedChildLineException(e);
    		exception.addInfo(NotLoadedChildLineException.ITEM_ID, tRev.get_item_id());
    		exception.addInfo(NotLoadedChildLineException.ITEM_REVISION_ID, tRev.get_item_revision_id());

    		throw exception;
		}

        ItemRevision bomLineItemRev = (ItemRevision) bomLine.getPropertyObject("bl_revision").getModelObjectValue();
        if(bomLineItemRev == null) {
            return;
        }
        // 이미 존재하면 return;
        if(bomlineItemRevMap.containsKey(bomLineItemRev.getUid())) {
            return;
        }
        // ItemProperties 속성 Setting..
        tcItemUtil.getProperties(new ModelObject[] { bomLineItemRev }, DEFAULT_ITEM_REVISION_PROPERTIES);
        bomlineItemRevMap.put(bomLineItemRev.getUid(), (BOMLine)bomLine);
        BOMLine parentBOMLine = (BOMLine)bomLine;
        ModelObject[] child = parentBOMLine.get_bl_child_lines();
        //ModelObject[] child = this.getChildrenBomLine((BOMLine)bomLine);
        for (int i = 0; child != null && i < child.length; i++) {
            ItemRevision childItemRevision = this.getBOMLineRevision(child[i]);
            if (childItemRevision == null) {
                continue;
            }
            // Fuction Mast Item Revision이면 continue 하여 하위 전개를 중단한다.
            if((IFConstants.CLASS_TYPE_S7_FUNCTION_MAST_REVISION).equals(childItemRevision.getTypeObject().getClassName())) {
                continue;
            }
            this.expandAllLevelFuction(child[i], bomlineItemRevMap);
        }
        /*
        BOMLine[] childs = tcServiceManager.getStructureService().getExpandPSOneLevel(this.session, (BOMLine)bomLine);
        for (int i = 0; i < childs.length; i++) {
            ItemRevision childItemRevision = (ItemRevision) childs[i].getPropertyObject("bl_revision").getModelObjectValue();
            if (childItemRevision == null) {
                continue;
            }
            // Fuction Mast Item Revision이면 continue 하여 하위 전개를 중단한다.
            if((IFConstants.CLASS_TYPE_S7_FUNCTION_MAST_REVISION).equals(childItemRevision.getTypeObject().getClassName())) {
                continue;
            }
            this.expandAllLevelFuction(childs[i], bomlineItemRevMap);
        }
         */
    }

    public ItemRevision getBOMLineRevision(ModelObject obj) throws Exception {
        return (ItemRevision) obj.getPropertyObject("bl_revision").getModelObjectValue();
    }

    /**
     * 속성배열을 속성 ArrayList 에 Add 한다.
     *
     * @method setItemRevisionProperties
     * @date 2013. 7. 11.
     * @param
     * @return ArrayList<String>
     * @exception
     * @throws
     * @see
     */
    public ArrayList<String> setItemRevisionProperties(String[] typeProperties, ArrayList<String> itemRevisionProperties) {
        for (int i = 0; typeProperties != null && i < typeProperties.length; i++) {
            itemRevisionProperties.add(typeProperties[i]);
        }
        return itemRevisionProperties;
    }

    /**
     * Function Master Revision을 Top으로 Occ puid를 가지고 하위 BomLine을 검색한다.
     *
     * @method getRevInfoUnderFnMasterRev
     * @date 2013. 7. 15.
     * @param
     * @return ArrayList<ModelObject>
     * @exception
     * @throws
     * @see
     */
//    public BOMLine getRevInfoUnderFnMasterRev(ItemRevision fnMasterRev, String searchOccurrenceUid) throws Exception {
//        String revisionRuleName = IFConstants.BOMVIEW_LATEST_RELEASED;
//        return getRevInfoUnderFnMasterRevReculsive(this.getCreateBOMWindow(fnMasterRev, revisionRuleName).get_top_line(), searchOccurrenceUid);
//    }

    /**
     * getRevInfoUnderFnMasterRev - reculsive
     *
     * @method getRevInfoUnderFnMasterRevReculsive
     * @date 2013. 7. 15.
     * @param
     * @return ArrayList<ModelObject>
     * @exception
     * @throws
     * @see
     */
//    public BOMLine getRevInfoUnderFnMasterRevReculsive(ModelObject bomLine, String searchOccurrenceUid) throws Exception {
//        // BOMLine 속성 Setting..
//        tcItemUtil.getProperties(new ModelObject[] { bomLine }, DEFAULT_BOMLINE_PROPERTIES );
//        ItemRevision itemRevision = (ItemRevision) bomLine.getPropertyObject("bl_revision").getModelObjectValue();
//        if(itemRevision == null) {
//            return null;
//        }
//        // 나중에는 searchChildRev의 occurrenceUid를 가지고 expand bomline의 occurrenceUid와 비교한다.
//        String occurrenceUid = ((BOMLine) bomLine).get_bl_occurrence_uid();
//        if(searchOccurrenceUid.equals(occurrenceUid)) {
//            return (BOMLine)bomLine;
//        }
//        ModelObject[] child = bomLine.getPropertyObject("bl_child_lines").getModelObjectArrayValue();
//        for (int i = 0; child != null && i < child.length; i++) {
//            ItemRevision childItemRevision = (ItemRevision) child[i].getPropertyObject("bl_revision").getModelObjectValue();
//            if (childItemRevision == null) {
//                continue;
//            }
//            // Item Revision속성 Setting.. (Revision Class Type별로 다르게 설정)
//            ArrayList<String> itemRevisionProperties = new ArrayList<String>();
//            // 기본 ItemRevision 속성
//            this.setItemRevisionProperties(DEFAULT_ITEM_REVISION_PROPERTIES, itemRevisionProperties);
//            // S7_VehPartRevision 속성
//            if (IFConstants.CLASS_TYPE_S7_VEH_PART_REVISION.equals(childItemRevision.getTypeObject().getClassName())) {
//                this.setItemRevisionProperties(S7_VEH_PART_ITEM_REVISION, itemRevisionProperties);
//            }
//            tcItemUtil.getProperties(new ModelObject[] { childItemRevision }, itemRevisionProperties.toArray(new String[itemRevisionProperties.size()]));
//            BOMLine childBomLine = this.getRevInfoUnderFnMasterRevReculsive(child[i], searchOccurrenceUid);
//            if(childBomLine != null) {
//                return childBomLine;
//            }
//        }
//        return null;
//    }

    /**
     * BOMWindow Close
     *
     * @method closeBOMWindow
     * @date 2013. 7. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void closeBOMWindow(BOMWindow bomWindow) throws Exception {
        if(bomWindow == null) {
            return;
        }
        tcServiceManager.getStructureService().closeBOMWindow(bomWindow);
    }

    /**
     * 상위를 역전개하여 Function Master Revision을 가져온다.
     *
     * whereUsed로 가져온 Function Master Revision이 Release되지 않은 Revision이 있으므로 필터링
     * 후 리턴 함.
     *
     * @method getParentFunction2
     * @date 2013. 8. 14.
     * @param
     * @return ArrayList<DefaultMutableTreeNode>
     * @exception
     * @throws
     * @see
     */
    public ArrayList<DefaultMutableTreeNode> getParentFunction(HashMap<String, EcoWhereUsedVO> functionEcoWhereUsedVO, HashMap<String, EcoWhereUsedVO> itemsEcoWhereUsedVO, ItemRevision ir, String revisionRuleName) throws Exception {
        ArrayList<DefaultMutableTreeNode> topFunctionList = new ArrayList<DefaultMutableTreeNode>();
        tcItemUtil.getProperties(new ModelObject[] { ir }, DEFAULT_ITEM_REVISION_PROPERTIES);
        this.getParentFunctionReculsive(functionEcoWhereUsedVO, itemsEcoWhereUsedVO, null, ir, revisionRuleName);
        return topFunctionList;
    }

    /**
     * 상위를 역전개하여 Function Master Revision을 가져온다. (Reculsive)
     *
     * @method getParentFunctionReculsive4
     * @date 2013. 8. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void getParentFunctionReculsive(HashMap<String, EcoWhereUsedVO> functionEcoWhereUsedVO, HashMap<String, EcoWhereUsedVO> itemsEcoWhereUsedVO, EcoWhereUsedVO childEcoWhereUsedVO, ItemRevision ir, String revisionRuleName) throws Exception {
        // Child가 WhereUsed 조회 대상 Item이 존재하는 지확인한다.
        EcoWhereUsedVO currentEcoWhereUsedVO = null;
        if(!itemsEcoWhereUsedVO.containsKey(TcItemUtil.getItemkey(ir))) {
            currentEcoWhereUsedVO = this.createEcoWhereUsedVO(ir);
            this.setEcoWhereUsedVO(childEcoWhereUsedVO, currentEcoWhereUsedVO);
            itemsEcoWhereUsedVO.put(TcItemUtil.getItemkey(ir), currentEcoWhereUsedVO);
        }else{
            // 이미 조회한 Parent 정보이므로 Child에 VO정보를 등록 후 Return
            currentEcoWhereUsedVO = itemsEcoWhereUsedVO.get(TcItemUtil.getItemkey(ir));
            this.setEcoWhereUsedVO(childEcoWhereUsedVO, currentEcoWhereUsedVO);
            return;
        }
        // FUNCTION Revision이면 FUNCTION Map 저장 후 Return
        if (IFConstants.CLASS_TYPE_S7_FUNCTION_REVISION.equals(ir.getTypeObject().getClassName())) {
            functionEcoWhereUsedVO.put(TcItemUtil.getItemkey(ir), currentEcoWhereUsedVO);
            return;
        }
        ItemRevision[] parentRevisions = tcDataUtil.whereUsed(ir.getUid(), 1, false, revisionRuleName);
        tcItemUtil.getProperties(parentRevisions, new String[] { "item_id", "item_revision_id", "object_name", "release_status_list" });
        for (int i = 0; parentRevisions != null && i < parentRevisions.length; i++) {
            ReleaseStatus[] releaseStatusList = parentRevisions[i].get_release_status_list();
            // Release Status List가 없으면 Release Revision이 아니므로 대상에서 제외
            if (releaseStatusList == null || releaseStatusList.length == 0) {
                continue;
            }
            // Parent가 Map에 등록되어있는지 확인
            if(itemsEcoWhereUsedVO.containsKey(TcItemUtil.getItemkey(parentRevisions[i]))) {
                // Parent의 Child에 currentEcoWhereUsedVO가 등록되어있는지 확인
                if(itemsEcoWhereUsedVO.get(TcItemUtil.getItemkey(parentRevisions[i])).getChild(currentEcoWhereUsedVO.getItemKey()) != null) {
                    continue;
                }
            }
            this.getParentFunctionReculsive(functionEcoWhereUsedVO, itemsEcoWhereUsedVO, currentEcoWhereUsedVO, parentRevisions[i], revisionRuleName);
        }
    }

    private void setEcoWhereUsedVO(EcoWhereUsedVO childEcoWhereUsedVO, EcoWhereUsedVO currentEcoWhereUsedVO) {
        if(childEcoWhereUsedVO != null) {
            currentEcoWhereUsedVO.setChild(childEcoWhereUsedVO.getItemKey(), childEcoWhereUsedVO);
            childEcoWhereUsedVO.setParent(currentEcoWhereUsedVO.getItemKey(), currentEcoWhereUsedVO);
        }
    }

    private EcoWhereUsedVO createEcoWhereUsedVO(ItemRevision ir) throws Exception {
        return new EcoWhereUsedVO(TcItemUtil.getItemkey(ir), ir.getTypeObject().getClassName());
    }

}
