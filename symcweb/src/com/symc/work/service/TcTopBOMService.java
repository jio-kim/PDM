package com.symc.work.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.biz.TcQueryUtil;
import com.symc.common.soa.biz.TcVariantUtil;
import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.soa.util.TcConstants;
import com.symc.common.soa.util.TcUtil;
import com.symc.common.util.IFConstants;
import com.symc.work.model.FunctionInfoVO;
import com.symc.work.model.ProductInfoVO;
import com.symc.work.model.VariantInfoVO;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;

public class TcTopBOMService {

    /**
     * 상부 BOM 등록
     *
     * @method createTopBomByProduct
     * @date 2013. 8. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createTopBomByProduct(Session session, ModularOption[] modularOptions, ItemRevision productItemRev, boolean isFirstDistribute, ProductInfoVO productInfoVO) throws Exception {
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        TcVariantUtil tcVariantUtil = new TcVariantUtil(session);
        BOMLineService bomLineService = new BOMLineService(session);
        // Daily Check DB 체크 - Product Daily Check - 이전 Trigger후 24시간이 지났는지
        // 확인
        // 초도배포가 아닌 EC등록인 경우 상부 BOM을 등록한다.
        if (!isFirstDistribute) {
            // 24시간 체크
            if(this.dailyCheck(productItemRev.get_item_id())) {
                return;
            }
        }
        // IF Create Date
        Date createDate = new Date();
        // 이전 PRODUCT 하위 상부 I/F 정보(IF_PE_VARIANT, IF_PE_FUNCTION) Clear
        this.cleanBeforeDBData(productItemRev.get_item_id());
        // Product Item Loop
        BOMWindow bomWindow = null;
        try {
            // Product Revision Properties 속성 설정
            tcItemUtil.getProperties(new ModelObject[] { productItemRev }, BOMLineService.DEFAULT_ITEM_REVISION_PROPERTIES);
            // 적용일자를 IF_DATE로 등록
            bomWindow = bomLineService.getCreateBOMWindow(productItemRev, IFConstants.BOMVIEW_LATEST_RELEASED, productInfoVO.getIfDate());
            // ItemRev PUID / BOMLine 을 Map을 가지고 있으나 문제가 되지않음
            // 이유 : ItemRevision이 아니라 BOMLine을 Value로 둔 이유는 Variant Option을 가지고 오려면 BOMLine이 필요하기 때문
            // 최초로 나오는 ItemRevisionPUID와 Value는 BOMLine 등록
            HashMap<String, BOMLine> bomlineItemRevMap = new HashMap<String, BOMLine>();
            bomLineService.expandAllLevelFuction(bomWindow.get_top_line(), bomlineItemRevMap);
            // Product Item 전개 Loop
            String[] itemRevPuids = bomlineItemRevMap.keySet().toArray(new String[bomlineItemRevMap.size()]);
            for (int i = 0; i < itemRevPuids.length; i++) {
                ItemRevision itemRevision = (ItemRevision)bomlineItemRevMap.get(itemRevPuids[i]).get_bl_revision();
                // BOMLine Item Revision Properties 속성 설정
                tcItemUtil.getProperties(new ModelObject[] { itemRevision }, BOMLineService.DEFAULT_ITEM_REVISION_PROPERTIES);
                if (IFConstants.CLASS_TYPE_S7_PRODUCT_REVISION.equals(itemRevision.getTypeObject().getClassName())) {
                    continue;
                }
                // Variant(IF_PE_VARIANT) 등록
                else if (IFConstants.CLASS_TYPE_S7_VARIANT_REVISION.equals(itemRevision.getTypeObject().getClassName())) {
                    this.createPeVariantInfo(tcVariantUtil, modularOptions, productItemRev, itemRevision, bomlineItemRevMap.get(itemRevPuids[i]));
                }
                // Function(IF_PE_FUNCTION) 등록
                else if (IFConstants.CLASS_TYPE_S7_FUNCTION_REVISION.equals(itemRevision.getTypeObject().getClassName())) {
                    this.createPeFunctionInfo(productItemRev, itemRevision);
                }
            }
            // Daily Check DB 등록
            this.createDailyCheck(productItemRev.get_item_id(), createDate);
        } catch (Exception e) {
            throw e;
        } finally {
            bomLineService.closeBOMWindow(bomWindow);
        }

    }

    /**
     * 이전 Variant, Function I/F DB 정보를 전체 Clear한다.
     *
     * @method cleanBeforeDBData
     * @date 2013. 7. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void cleanBeforeDBData(String productId) throws Exception {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("productId", productId);
        // Variant 정보 Clear
        TcCommonDao.getTcCommonDao().delete("com.symc.ifpe.deleteAllPeVariantInfo", paramMap);
        // Function 정보 Clear
        TcCommonDao.getTcCommonDao().delete("com.symc.ifpe.deleteAllPeFunctionInfo", paramMap);
    }

    /**
     * TC에 있는 모든 Product Item을 조회한다.
     *
     * - DB PRODUCT 하위의 Fuction 조회 방식으로 변경하여 현재 사용하지 않음
     *
     * @deprecated
     * @method getProductItemRev
     * @date 2013. 7. 26.
     * @param
     * @return ItemRevision[]
     * @exception
     * @throws
     * @see
     */
    public ItemRevision[] getProductItemRev(Session session) throws Exception {
        TcServiceManager tcServiceManager = new TcServiceManager(session);
        TcQueryUtil tcQueryUtil = new TcQueryUtil(session);
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        String queryName = TcConstants.SEARCH_ITEM;
        String[] entries = { "Type" };
        String[] values = { IFConstants.CLASS_TYPE_S7_PRODUCT };
        SavedQueriesResponse executeSavedQueries = tcServiceManager.getQueryService().executeSavedQueries(tcQueryUtil.setQueryInputforSingle(queryName, entries, values, "user"));
        if (!tcServiceManager.getDataService().ServiceDataError(executeSavedQueries.serviceData)) {
            String[] uids = tcQueryUtil.executeQueryResult(executeSavedQueries)[0].objectUIDS;
            if (uids == null || uids.length == 0) {
                throw new Exception("TC에 Product Item이 없습니다.");
            }
            ModelObject[] itemModels = tcServiceManager.getDataService().loadModelObjects(uids);
            ItemRevision[] productRevList = new ItemRevision[itemModels.length];
            for (int i = 0; i < itemModels.length; i++) {
                Item item = (Item) itemModels[i];
                tcItemUtil.getProperties(new ModelObject[] { item }, new String[] { TcConstants.PROP_ITEM_ID, "revision_list" });
                ModelObject[] itemRevisions = item.get_revision_list();
                if (itemRevisions == null || itemRevisions.length == 0) {
                    throw new Exception(item.get_item_id() + " : This revision does not exist.");
                }
                // 현재 Rev List의 제일 마지막을 Latest Revision 으로 간주하고 등록한다.
                productRevList[i] = (ItemRevision) itemRevisions[itemRevisions.length - 1];
            }
            return productRevList;
        } else {
            throw new Exception(TcUtil.makeMessageOfFail(executeSavedQueries.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }

    /**
     * Variant 정보를 IF_PE_VARIANT 테이블에 등록한다.
     *
     * @method createPeVariantInfo
     * @date 2013. 7. 22.
     * @param
     * @return void
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public void createPeVariantInfo(TcVariantUtil tcVariantUtil, ModularOption[] modularOptions, ItemRevision productItemRev, ItemRevision variantItemRev, BOMLine variantBOMLine) throws Exception {
        VariantInfoVO variantInfoVO = new VariantInfoVO();
        variantInfoVO.setProductId(productItemRev.get_item_id());
        variantInfoVO.setProductRevId(productItemRev.get_item_revision_id());
        variantInfoVO.setProductName(productItemRev.get_object_name());
        variantInfoVO.setVariantId(variantItemRev.get_item_id());
        variantInfoVO.setVariantName(variantItemRev.get_object_name());
        variantInfoVO.setVariantRevId(variantItemRev.get_item_revision_id());
        String constrainsOptions = tcVariantUtil.getConstrainsOptions(modularOptions, variantBOMLine);
        variantInfoVO.setOptions(constrainsOptions);
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createPeVariantInfo", variantInfoVO);

    }

    /**
     * Function 정보를 IF_PE_FUNCTION 테이블에 등록한다.
     *
     * @method createPeFunctionInfo
     * @date 2013. 7. 22.
     * @param
     * @return void
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public void createPeFunctionInfo(ItemRevision productItemRev, ItemRevision functionItemRev) throws Exception {
        FunctionInfoVO functionInfoVO = new FunctionInfoVO();
        functionInfoVO.setProductId(productItemRev.get_item_id());
        functionInfoVO.setProductRevId(productItemRev.get_item_revision_id());
        functionInfoVO.setProductName(productItemRev.get_object_name());
        functionInfoVO.setFunctionId(functionItemRev.get_item_id());
        functionInfoVO.setFunctionName(functionItemRev.get_object_name());
        functionInfoVO.setFunctionRevId(functionItemRev.get_item_revision_id());
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createPeFunctionInfo", functionInfoVO);
    }

    /**
     * Daily 시간체크
     *
     * (현재시간 < CHAECK_DATE + 24시간)
     * Daily 체크시간이 현재시간보다 크면(24시간이 안지났으면) true
     *
     * (현재시간 => CHAECK_DATE + 24시간)
     * Daily 체크시간이 현재시간보다 작으면(24시간이 지났으면) false
     *
     *
     * @method dailyCheck
     * @date 2013. 7. 26.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private boolean dailyCheck(String productId) throws Exception {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("productId", productId);
        List<HashMap<String, Object>> productDailyCheckList = (List<HashMap<String, Object>>)TcCommonDao.getTcCommonDao().selectList("com.symc.ifpe.getDailyCheck", paramMap);
        if(productDailyCheckList.size() > 0) {
            HashMap<String, Object> row = productDailyCheckList.get(0);
            Date checkDate = (Date)row.get("CHECK_DATE");
            // ChheckDate + 24시간
            long after24Time = checkDate.getTime() + (24*60*60*1000);
            // 현재시간
            long currentTime = System.currentTimeMillis();
            // 24시간이 경과하지 않은 경우
            if(currentTime < after24Time) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * PRODUCT 상부 BOM (IF_PE_VARIANT, IF_PE_FUNCTION)체크를 위한 Daily Check 정보를 등록
     *
     * @method createDailyCheck
     * @date 2013. 7. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createDailyCheck(String productId, Date checkDate) throws Exception {
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("productId", productId);
        paramMap.put("checkDate", checkDate);
        TcCommonDao.getTcCommonDao().delete("com.symc.ifpe.createDailyCheck", paramMap);
    }

}
