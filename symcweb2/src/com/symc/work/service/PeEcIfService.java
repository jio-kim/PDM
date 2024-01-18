package com.symc.work.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.ssangyong.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.service.TcPreferenceManagementService;
import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.util.ContextUtil;
import com.symc.common.util.IFConstants;
import com.symc.common.util.StringUtil;
import com.symc.work.model.BOMChangeInfoVO;
import com.symc.work.model.EcoBomVO;
import com.symc.work.model.EcoWhereUsedVO;
import com.symc.work.model.FunctionInfoVO;
import com.symc.work.model.PartBOMInfoVO;
import com.symc.work.model.ProductInfoVO;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * EC등록 Service
 * [20150717] [ymjang] 오류 발생시 관리자 메일 발송 기능 추가
 * 1) Product 하위에 ECO 대상 Function 이 구성되어 있지 않은 경우,
 * 2) ECO 대상 Item 이 Function에 구성되어 있지 않은 경우,
 * [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
 * 
 */
public class PeEcIfService {
    TcPeIFService tcPeIFService;

    public void setTcPeIFService(TcPeIFService tcPeIFService) {
        this.tcPeIFService = tcPeIFService;
    }

    /**
     * * [EC 등록] PRODUCT 기준 EC 등록
     *
     * @method createProductEc
     * @date 2013. 8. 8.
     * @param
     * @return StringBuffer
     * @exception
     * @throws
     * @see
     */
    public StringBuffer createProductEc(Session session, ModularOption[] modularOptions, ProductInfoVO productInfoVO) throws Exception {
        StringBuffer log = new StringBuffer();
        TcServiceManager tcServiceManager = new TcServiceManager(session);
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        BOMLineService bomLineService = new BOMLineService(session);
        if("".equals(StringUtil.nullToString(productInfoVO.getEcoId()))) {
            throw new Exception("[ERROR] PeEcIfService.createProductEc : " + productInfoVO.getProductId() + "의 ECO ID가 존재하지 않습니다.");
        }
        // 이미 등록한 Item정보는 등록하지 않는다.
        HashMap<String, ItemRevision> insertedItem = new HashMap<String, ItemRevision>();
        // FUNCTION 정보 Map : MAP<String(FUNCTION ITEM_ID), DefaultMutableTreeNode(FUNCTION NODE 정보)>
        // DefaultMutableTreeNode 의 userObject에는
        // PartBOMInfoVO를 가지고 있고
        // PartBOMInfoVO의 속성으로 PartInfoVO(Part 정보), BOMChangeInfoVO(BOM 속성) 이 두개의 속성을 가지고있다.
        HashMap<String, DefaultMutableTreeNode> functionNodeMap = new HashMap<String, DefaultMutableTreeNode>();
        ItemRevision productItemRev = tcItemUtil.getLastReleaseRevItem(productInfoVO.getProductId());
        if (productItemRev == null) {
            throw new Exception(productInfoVO.getIfId() + " 의 Product ID : " + productInfoVO.getProductId() + " 리비젼 정보가 없습니다.");
        }
        // 1. 상부 BOM PART 정보 등록 (IF_PE_VARIANT, IF_PE_FUNCTION) - 이전 등록 후 24시간이 지나면 등록
        TcTopBOMService tcTopBOMService = new TcTopBOMService();
        tcTopBOMService.createTopBomByProduct(session, modularOptions, productItemRev, false, productInfoVO);
        TcEcoService tcEcoService = (TcEcoService)ContextUtil.getBean("tcEcoService");
        // 2. PRODUCT 하위 전체 Function 리스트를 조회한다.
        List<FunctionInfoVO> productFunctionList = tcPeIFService.getFunctionList(productInfoVO.getProductId());
        // 3. ECO 변경 List를 가지고온다.
        List<EcoBomVO> ecoList = tcEcoService.getEcoBomList(productInfoVO.getEcoId(), productInfoVO.getProjectId());
        // FUNCTION Map - ECO Item 조회 시 관련된 WhereUsed FUNCTION Map
        HashMap<String, EcoWhereUsedVO> functionEcoWhereUsedVO = new HashMap<String, EcoWhereUsedVO>();
        HashMap<String, EcoWhereUsedVO> itemsEcoWhereUsedVO = new HashMap<String, EcoWhereUsedVO>();
        // ECO Item이 속한 FUCTION List
        HashMap<String, HashMap<String, EcoWhereUsedVO>> itemsFuctions = new HashMap<String, HashMap<String, EcoWhereUsedVO>>();
        for (int i = 0; ecoList != null && i < ecoList.size(); i++) {
            EcoBomVO ecoBomVO = ecoList.get(i);
            // 변경Part의 Release Revision을 가지고온다.
            ItemRevision lastParentReleaseRevItem = tcItemUtil.getRevisionInfo(ecoBomVO.getParentNo(), ecoBomVO.getParentRev());
            // 변경Part의 Release Revision을 가지고온다.
            //ItemRevision lastChildReleaseRevItem = tcItemUtil.getLastReleaseRevItem(ecoBomVO.getNewPartNo());
            // Parent Part의 Release Revision을 가지고 parent를 reculsive하여 FUNCTION을 가지고온다.
            // Change Type = "D" : 삭제
            if("D".equals(ecoBomVO.getCt())) {
                //String[] deleteOccPuids = this.getArrayOccThreadPuids(ecoBomVO.getOldOccs());
                String[] deleteOccPuids = this.getArrayOccThreadPuids(ecoBomVO.getOccThreads());
                for (int j = 0; j < deleteOccPuids.length; j++) {
                    // 삭제는 BOM정보를 알아낼 수 없으므로 ECO정보에서 알아낸다.
                    // BOM ECO 정보 저장
                    BOMChangeInfoVO bomChangeInfoVO = new BOMChangeInfoVO();
                    bomChangeInfoVO.setIfId(productInfoVO.getIfId());
                    // OLD/NEW OccPuid가 같음
                    bomChangeInfoVO.setOccPuid(deleteOccPuids[j]);
                    bomChangeInfoVO.setOldOccPuid(deleteOccPuids[j]);
                    bomChangeInfoVO.setParentId(ecoBomVO.getParentNo());
                    bomChangeInfoVO.setParentRevId(ecoBomVO.getParentRev());
                    bomChangeInfoVO.setParentType(this.getItemClassTypeToItemrevClassType(ecoBomVO.getParentType()));
                    bomChangeInfoVO.setChildId(ecoBomVO.getOldPartNo());
                    bomChangeInfoVO.setChildRevId(ecoBomVO.getOldPartRev());
                    bomChangeInfoVO.setChildType(this.getItemClassTypeToItemrevClassType(ecoBomVO.getOldPartType()));
                    bomChangeInfoVO.setChangeType(ecoBomVO.getCt());
                    bomChangeInfoVO.setEplId(ecoBomVO.getEplId());
                    bomChangeInfoVO.setAbsOccPuid(deleteOccPuids[j]);
                    tcPeIFService.createPeBOMChangeInfo(bomChangeInfoVO);
                }
            }
            else {
                // Parent에 존재하는 FUUNCTION을 TC WhereUsed를 이용 검색하여 리스트를 가져온다.
                this.getParentFunction(bomLineService, tcItemUtil, functionEcoWhereUsedVO, itemsEcoWhereUsedVO, itemsFuctions, lastParentReleaseRevItem);
                EcoWhereUsedVO[] affectedFunctions = this.getItemMapFromFindFunctionList(itemsFuctions, lastParentReleaseRevItem);
                // ECO 변경 Part에 영향받는 FUNCTION 정보 리스트가 없는 경우 Log 기록
                if(affectedFunctions == null || affectedFunctions.length == 0) {
                    log.append(IFConstants.TEXT_RETURN);
                    log.append("** [IF_ID : " + productInfoVO.getIfId() + " / PRODUCT_ID : " + productInfoVO.getProductId() + " / ECO_NO : " + ecoBomVO.getEcoNo() + "] **");
                    log.append(IFConstants.TEXT_RETURN);
                    log.append("[" +ecoBomVO.getEplId() + "] " + ecoBomVO.getNewPartNo() + " - ECO 변경 Item이 사용되는 Function Item 정보가 없습니다.");
                    log.append(IFConstants.TEXT_RETURN);
                    
                    // [20150717] [ymjang] 오류 발생시 관리자 메일 발송 기능 추가
                    // 에러 발생 시 메일을 발송
                    //sendMail(session, log);
                }
                // 변경Part의 affected FUNCTION 리스트가 PRODUCT 하위 FUNCTION 리스트에 존재하는지 확인
                FunctionInfoVO[] ecoFunctionInfos = tcPeIFService.getEcoFunctions(productInfoVO, productFunctionList, affectedFunctions);

            	/* [20150104][ymjang] 여러 차종에서 사용되는 Item 의 경우, 해당 Product Function 에는 존재하지 않는 것을 수 있음
            	 * --> 에러로 기록되는 것을 방지하기 위하여 로그 기록 삭제
            	 * ex.8661008620 의 경우, F678Y2012,F678Q2012,F678XA2015,F678A2013,F678G2016,F678T2007 에서도 함께 사용됨.
            	 *    그러나, PVW2007 하위에는 F678Y2012,F678Q2012,F678XA2015,F678A2013,F678G2016,F678T2007 존재하지 않음.
            	 */
            	/*
                // ECO 변경 Part에 영향받는 FUNCTION 정보를 가지고 PRODUCT 하위 FUNCTION에 1개도 존재하지 않는 경우 Log 기록
                if(ecoFunctionInfos == null || ecoFunctionInfos.length == 0) {
                    log.append(IFConstants.TEXT_RETURN);
                    log.append("** [IF_ID : " + productInfoVO.getIfId() + " / PRODUCT_ID : " + productInfoVO.getProductId() + " / ECO_NO : " + ecoBomVO.getEcoNo() + "] **");
                    log.append(IFConstants.TEXT_RETURN);
                    log.append("[" +ecoBomVO.getEplId() + "] " + ecoBomVO.getNewPartNo() + " - ECO 변경 Item이 사용되는 Function Item 정보가 PRODUCT 하위 Function에 존재하지 않습니다.");
                    log.append(IFConstants.TEXT_RETURN);
                    log.append("Affected Fuction List : ");
                    for (int j = 0; j < affectedFunctions.length; j++) {
                        if(j>0) {
                            log.append(",");
                        }
                        log.append(affectedFunctions[j].getItemId());
                    }
                    log.append(IFConstants.TEXT_RETURN);
                    log.append(IFConstants.TEXT_RETURN);
                	
                    // [20150717] [ymjang] 오류 발생시 관리자 메일 발송 기능 추가
                    // 에러 발생 시 메일을 발송
                    //sendMail(session, log);
                }
                */
                
                ItemRevision[] ecoFunctionRevs = this.getFunctionRevs(session, ecoFunctionInfos);
                // FUNCTION을 ALL Expand한 FUNCTION ITEM PUID를 Key로 가지고 Value는 노드(DefaultMutableTreeNode)정보를 가지는 Map을 구성한다.
                this.makeFunctionTree(session, modularOptions, productInfoVO, functionNodeMap, log, productItemRev, ecoFunctionRevs);
                // FUNCTION 정보와 ECO NEW BOM OCC_PUIDS를 가지고 PARENT FUNCTION 별로 하위 전개하여 등록 대상 PartInfoVO, BOMChangeInfoVO 리스트를 조회한다. (FUNCTION 별 ECO Part, BOM 정보)
                // OLD BOM OCC_PUIDS
                String[] newOccs = this.getArrayOccThreadPuids(ecoBomVO.getOccThreads());
                DefaultMutableTreeNode[] newPartBOMInfoNodes = this.getFunctionUnderBomLine(functionNodeMap , ecoFunctionRevs, newOccs);
                // OLD BOM OCC_PUIDS
                String[] oldOccs = this.getArrayOccThreadPuids(ecoBomVO.getOldOccs());
                // New Occ를 Key, Old Occ를 Value로 가지는 Map을 구성 (New OCC를 가지고 Old OCC를 찾기위해 사용)
                HashMap<String, String> newOccOldOccMap = new HashMap<String, String>();
                for (int j = 0; j < newOccs.length; j++) {
                    // New OCC index가 Old OCC보다 작은 경우 등록 (ArrayIndexBound Exception 처리)
                    if(j < oldOccs.length) {
                        newOccOldOccMap.put(newOccs[j], oldOccs[j]);
                    }
                }
                for (int j = 0; j < newPartBOMInfoNodes.length; j++) {
                    this.createPartBOMInfos(session, tcServiceManager, log, insertedItem, ecoBomVO, newOccOldOccMap, newPartBOMInfoNodes[j], true);
                }
            }
        }
        // ECO_ID를 가지고 IF_PE_ECO_BOM_LIST 테이블에 TC의 ECO_BOM_LIST 테이블 정보를 가져와 insert 한다.
        tcPeIFService.createEcoBomListInfo(productInfoVO.getIfId(), productInfoVO.getEcoId());
        // I/F PRODUCT 정보 테이블(IF_PE_PRODUCT)에 PART, BOM COUNT를 업데이트한다.
        tcPeIFService.changePeProductPartBomCount(productInfoVO.getIfId());
        return log;
    }
    

    /**
     * OCC_PUID를 가지고 찾은 PART 정보를 등록하고 더불어 자식 정보들도 등록한다.
     *
     * @method createPartBOMInfos
     * @date 2013. 8. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createPartBOMInfos(Session session, TcServiceManager tcServiceManager, StringBuffer log, HashMap<String, ItemRevision> insertedItem, EcoBomVO ecoBomVO, HashMap<String, String> newOccOldOccMap, DefaultMutableTreeNode node, boolean isRoot) throws Exception {
        PartBOMInfoVO partBOMInfoVO = (PartBOMInfoVO)node.getUserObject();
        this.createPartBOMInfo(session, tcServiceManager, log, insertedItem, ecoBomVO, newOccOldOccMap, partBOMInfoVO, isRoot);
        if(node.children() == null) {
            return;
        }
        @SuppressWarnings("rawtypes")
        Enumeration enumeration = node.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) enumeration.nextElement();
            if(childNode == null) {
                continue;
            }
            this.createPartBOMInfos(session, tcServiceManager, log, insertedItem, ecoBomVO, newOccOldOccMap, childNode, false);
        }
    }

    /**
     * PART, BOM 정보 등록
     *
     * @method createPartBOMInfo
     * @date 2013. 8. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createPartBOMInfo(Session session, TcServiceManager tcServiceManager, StringBuffer log, HashMap<String, ItemRevision> insertedItem, EcoBomVO ecoBomVO, HashMap<String, String> newOccOldOccMap, PartBOMInfoVO partBOMInfoVO, boolean isRoot) throws Exception {
        // Part 정보 저장 - 중복등록 체크
        if(!insertedItem.containsKey(partBOMInfoVO.getPartInfoVO().getPartNumber())) {
            // Part 정보 저장
            tcPeIFService.createPePartInfo(partBOMInfoVO.getPartInfoVO());
            // Part Dataset FTP 전송 & DB(IF_PE_FILE_PATH) 저장
            ItemRevision itemRevision = (ItemRevision) tcServiceManager.getDataService().loadModelObject(partBOMInfoVO.getPartInfoVO().getObjectId());
            tcPeIFService.setDatasetFileSave(session, itemRevision, partBOMInfoVO.getPartInfoVO(), log);
            insertedItem.put(partBOMInfoVO.getPartInfoVO().getPartNumber(), itemRevision);
        }
        // BOM ECO 정보 저장
        // Node Root인 경우(EPL 변경 ROOT)에만 BOM Change 정보 등록
        if(isRoot) {
            partBOMInfoVO.getBomChangeInfoVO().setEplId(ecoBomVO.getEplId());
            partBOMInfoVO.getBomChangeInfoVO().setChangeType(ecoBomVO.getCt());
            String occPuid = partBOMInfoVO.getBomChangeInfoVO().getOccPuid();
            partBOMInfoVO.getBomChangeInfoVO().setOldOccPuid(newOccOldOccMap.get(occPuid));
        }
        tcPeIFService.createPeBOMChangeInfo(partBOMInfoVO.getBomChangeInfoVO());
    }

    /**
     * Item Class Type 명을 가지고 Item Revision Class Type 명을 가지고 온다.
     *
     * @method getItemClassTypeToItemrevClassType
     * @date 2013. 8. 13.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getItemClassTypeToItemrevClassType(String itemClassType) {
        if("".equals(StringUtil.nullToString(itemClassType))) {
            return "";
        }
        if(IFConstants.CLASS_TYPE_S7_PRODUCT.equals(itemClassType)) {
            return IFConstants.CLASS_TYPE_S7_PRODUCT_REVISION;
        } else if(IFConstants.CLASS_TYPE_S7_VARIANT.equals(itemClassType)) {
            return IFConstants.CLASS_TYPE_S7_VARIANT_REVISION;
        } else if(IFConstants.CLASS_TYPE_S7_FUNCTION.equals(itemClassType)) {
            return IFConstants.CLASS_TYPE_S7_FUNCTION_REVISION;
        } else if(IFConstants.CLASS_TYPE_S7_FUNCTION_MAST.equals(itemClassType)) {
            return IFConstants.CLASS_TYPE_S7_FUNCTION_MAST_REVISION;
        } else if(IFConstants.CLASS_TYPE_S7_VEH_PART.equals(itemClassType)) {
            return IFConstants.CLASS_TYPE_S7_VEH_PART_REVISION;
        } else if(IFConstants.CLASS_TYPE_S7_STD_PART.equals(itemClassType)) {
            return IFConstants.CLASS_TYPE_S7_STD_PART_REVISION;
        } else {
            return "";
        }
    }

    /**
     * FUNCTION NODE Map 생성
     * (속도 이슈로 인해 FUNCTION을 AllExpand한 노드정보를 Map으로 미리 구성 함.)
     *
     * @method makeFunctionTree
     * @date 2013. 8. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void makeFunctionTree(Session session, ModularOption[] modularOptions, ProductInfoVO productInfoVO, HashMap<String, DefaultMutableTreeNode> functionNodeMap, StringBuffer log, ItemRevision productItemRev, ItemRevision[] ecoFunctionRevs) throws Exception {
        for (int i = 0; i < ecoFunctionRevs.length; i++) {
            //이미 등록된 Function이 있으면 skip
            if(functionNodeMap.containsKey(ecoFunctionRevs[i].getUid())) {
                continue;
            }
            BOMLineService bomLineService = new BOMLineService(session);
            BOMWindow bomWindow = null;
            try {
                // 적용일자를 IF_DATE를 기입한다.
                bomWindow = bomLineService.getCreateBOMWindow(ecoFunctionRevs[i], IFConstants.BOMVIEW_LATEST_RELEASED, productInfoVO.getIfDate());
                DefaultMutableTreeNode functionNode = tcPeIFService.makeTreeExpandAllLevel(session, productInfoVO, productItemRev, modularOptions, log, (BOMLine)bomWindow.get_top_line(), new DefaultMutableTreeNode());
                functionNodeMap.put(ecoFunctionRevs[i].getUid(), functionNode);
            } catch(Exception e) {
                throw e;
            } finally {
                bomLineService.closeBOMWindow(bomWindow);
            }
        }
    }

    /**
     * Function 리스트를 가지고 전개하여 occThreadPuid를 가지고 BOMLine 리스트를 검색한다.
     *
     * @method getFunctionUnderBomLine
     * @date 2013. 8. 9.
     * @param
     * @return BOMLine
     * @exception
     * @throws
     * @see
     */
    public DefaultMutableTreeNode[] getFunctionUnderBomLine(HashMap<String, DefaultMutableTreeNode> functionNodeMap , ItemRevision[] ecoFunctionRevs, String[] occThreadPuids) {
        ArrayList<DefaultMutableTreeNode> findPartBOMInfoNodesList = new ArrayList<DefaultMutableTreeNode>();
        HashMap<String, String>occThreadPuidsMap = new HashMap<String, String >();
        if(occThreadPuids == null || occThreadPuids.length == 0) {
            return new DefaultMutableTreeNode[0];
        }
        for (int i = 0; i < occThreadPuids.length; i++) {
            occThreadPuidsMap.put(occThreadPuids[i], occThreadPuids[i]);
        }
        for (int i = 0; i < ecoFunctionRevs.length; i++) {
            DefaultMutableTreeNode functionNode = functionNodeMap.get(ecoFunctionRevs[i].getUid());
            DefaultMutableTreeNode[] findPartBOMInfoNodes = this.findPartBOMInfoVO(functionNode, occThreadPuidsMap);
            for (int j = 0; j < findPartBOMInfoNodes.length; j++) {
                findPartBOMInfoNodesList.add(findPartBOMInfoNodes[j]);
            }
        }
        return findPartBOMInfoNodesList.toArray(new DefaultMutableTreeNode[findPartBOMInfoNodesList.size()]);
    }

    /**
     * Node 검색
     *
     * @method findPartBOMInfoVO
     * @date 2013. 8. 12.
     * @param
     * @return PartBOMInfoVO[]
     * @exception
     * @throws
     * @see getFunctionUnderBomLine
     */
    private DefaultMutableTreeNode[] findPartBOMInfoVO(DefaultMutableTreeNode node, HashMap<String, String>occThreadPuidsMap) {
        ArrayList<DefaultMutableTreeNode> findPartBOMInfoNodes = new ArrayList<DefaultMutableTreeNode>();
        this.findReculsiveNode(node, findPartBOMInfoNodes, occThreadPuidsMap);
        return findPartBOMInfoNodes.toArray(new DefaultMutableTreeNode[findPartBOMInfoNodes.size()]);
    }

    /**
     * ReculsiveNode
     *
     * @method findReculsiveNode
     * @date 2013. 8. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see findPartBOMInfoVO
     */
    @SuppressWarnings("rawtypes")
    private void findReculsiveNode(DefaultMutableTreeNode node, ArrayList<DefaultMutableTreeNode> findPartBOMInfoNodes, HashMap<String, String>occThreadPuidsMap) {
        PartBOMInfoVO partBOMInfoVO = (PartBOMInfoVO)node.getUserObject();
        // OCC PUID가 존재하면 등록 대상
        if(occThreadPuidsMap.containsKey(partBOMInfoVO.getBomChangeInfoVO().getOccPuid())) {
            findPartBOMInfoNodes.add(node);
        }
        if(node.children() == null) {
            return;
        }
        Enumeration enumeration = node.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) enumeration.nextElement();
            this.findReculsiveNode(childNode, findPartBOMInfoNodes, occThreadPuidsMap);
        }
    }

    /**
     * OccThreadPuid를 자리수(14)별로 끊어 배열로 분리한다.
     *
     * @method getArrayOccThreadPuids
     * @date 2013. 8. 12.
     * @param
     * @return String[]
     * @exception
     * @throws
     * @see
     */
    private String[] getArrayOccThreadPuids(String puidStr) throws Exception {
       String[] occPuids = null;
       if("".equals((StringUtil.nullToString(puidStr)))) {
           return new String[0];
       }
       int puidsLehgth = puidStr.length();
       int tcPuidLength = 14;
       int delimitCount = puidsLehgth / tcPuidLength;
       occPuids = new String[delimitCount];
       for (int i = 0; i < delimitCount; i++) {
           occPuids[i] = puidStr.substring(i*tcPuidLength, (i*tcPuidLength) + tcPuidLength);
       }
       return occPuids;
    }

    /**
     * 연관된 상위 Function을 찾는다.
     *
     * @method getParentFunction
     * @date 2013. 8. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void getParentFunction(BOMLineService bomLineService, TcItemUtil tcItemUtil, HashMap<String, EcoWhereUsedVO> functionEcoWhereUsedVO,  HashMap<String, EcoWhereUsedVO> itemsEcoWhereUsedVO, HashMap<String, HashMap<String, EcoWhereUsedVO>> itemsFuctions, ItemRevision ir) throws Exception {
        //Latest Released를 하지않고 Working으로하여 전체를 찾는다.
        bomLineService.getParentFunction(functionEcoWhereUsedVO, itemsEcoWhereUsedVO, ir, null);
        String[] key = functionEcoWhereUsedVO.keySet().toArray(new String[functionEcoWhereUsedVO.size()]);
        for (int i = 0; i < key.length; i++) {
            this.reculsiveExpandFunction(functionEcoWhereUsedVO.get(key[i]), functionEcoWhereUsedVO.get(key[i]), itemsFuctions);
        }
    }

    /**
     * 상위 Function을 전개하여 ItemKey(ITEM_ID + "_" + ITEM_VERSION_ID)를 가지는 Map에 FUNCTION정보를 담는다.
     *
     * itemsFuctions
     *
     *  HashMap<String(ITEM_KEY), HashMap<String(FUNCTION_ITEM_KEY), EcoWhereUsedVO(FUNCTION INFO VO)>
     *
     * @method reculsiveExpandFunction
     * @date 2013. 8. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void reculsiveExpandFunction(EcoWhereUsedVO vo, EcoWhereUsedVO functionVO, HashMap<String, HashMap<String, EcoWhereUsedVO>> itemsFunctions) throws Exception {
        HashMap<String, EcoWhereUsedVO> itemFunctionMap = null;
        if(itemsFunctions.containsKey(vo.getItemKey())) {
            itemFunctionMap = itemsFunctions.get(vo.getItemKey());
            if(itemFunctionMap == null) {
                itemFunctionMap = new HashMap<String, EcoWhereUsedVO>();
            }
            itemFunctionMap.put(functionVO.getItemKey(), functionVO);
        }
        else {
            itemFunctionMap = new HashMap<String, EcoWhereUsedVO>();
            itemFunctionMap.put(functionVO.getItemKey(), functionVO);
            itemsFunctions.put(vo.getItemKey(), itemFunctionMap);
        }
        String[] key = vo.getChildMap().keySet().toArray(new String[vo.getChildMap().size()]);
        for (int i = 0; i < key.length; i++) {
            EcoWhereUsedVO childVo = vo.getChildMap().get(key[i]);
            this.reculsiveExpandFunction(childVo, functionVO, itemsFunctions);
        }
    }

    /**
     * Item Key를 가지고 Item이 사용되고 있는 상위 Function을 조회한다.
     *
     * @method getItemMapFromFindFunctionList
     * @date 2013. 8. 16.
     * @param
     * @return EcoWhereUsedVO[]
     * @exception
     * @throws
     * @see
     */
    private EcoWhereUsedVO[] getItemMapFromFindFunctionList(HashMap<String, HashMap<String, EcoWhereUsedVO>> itemsFunctions, ItemRevision ir) throws Exception {
        String itemKey = TcItemUtil.getItemkey(ir);
        if(!(itemsFunctions.containsKey(itemKey))){
            return new EcoWhereUsedVO[0];
        }
        HashMap<String, EcoWhereUsedVO> itemHaveFunctionList = itemsFunctions.get(itemKey);
        String[] key = itemHaveFunctionList.keySet().toArray(new String[itemHaveFunctionList.size()]);
        EcoWhereUsedVO[] functionEcoWhereUsedVOs = new EcoWhereUsedVO[key.length];
        for (int i = 0; i < key.length; i++) {
            functionEcoWhereUsedVOs[i] = itemHaveFunctionList.get(key[i]);
        }
        return functionEcoWhereUsedVOs;
    }

    /**
     * FunctionInfoVO -> ItemRevsion을 Convert
     *
     * @method getFunctionRevs
     * @date 2013. 8. 16.
     * @param
     * @return ItemRevision[]
     * @exception
     * @throws
     * @see
     */
    private ItemRevision[] getFunctionRevs(Session session, FunctionInfoVO[] infos) throws Exception {
        if(infos == null || infos.length == 0) {
            return new ItemRevision[0];
        }
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        ItemRevision[] functionItemRevs = new ItemRevision[infos.length];
        for (int i = 0; i < infos.length; i++) {
            functionItemRevs[i] = tcItemUtil.getRevisionInfo(infos[i].getFunctionId(), infos[i].getFunctionRevId());
        }
        return functionItemRevs;
    }
    
    /**
     * [20150717] [ymjang] 오류 발생시 관리자 메일 발송 기능 추가
     * @param session
     * @param log
     */
    @SuppressWarnings({ "unchecked" })
	private void sendMail(Session session, StringBuffer log){

    	TcServiceManager manager = new TcServiceManager(session);
    	TcPreferenceManagementService prefManager = null;
    	CompletePreference retPrefValue = null;
		try {
			prefManager = manager.getPreferenceService();
			GetPreferencesResponse ret = prefManager.getPreferences(new String[]{"PE_IF_ADMIN"}, true);
			if (ret != null && ret.data.sizeOfPartialErrors() == 0)
			{
				for (CompletePreference pref : ret.response)
					if (pref.definition.protectionScope.toUpperCase().equals("site".toUpperCase()))
						retPrefValue = pref;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		String[] list = retPrefValue.values.values;
		if( list != null && list.length > 0){

    		String toUsers = "";
    		String body = "";
    		String title = "";
    		for( int i = 0; i < list.length; i++ ){
    			String toUser = list[i];
    			if( i > 0){
					toUsers += "," + toUser;
				}else{
					toUsers = toUser;
				}
    		}
        	title = "New PLM : TC to PE I/F Error 알림";
        	body = "<PRE>";
    	    body += log.toString();
    	    body += "</PRE>";

        	DataSet ds = new DataSet();
    		ds.put("the_sysid", "NPLM");
    		ds.put("the_sabun", "NPLM");

    		ds.put("the_title", title);
    		ds.put("the_remark", body);
    		ds.put("the_tsabun", toUsers);

			try {
				// [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
				TcCommonDao.getTcCommonDao().update("com.symc.interface.sendMailEai", ds);
				//TcCommonDao.getTcCommonDao().update("com.symc.interface.sendMail", ds);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
    }
        
}
