/**
 * 
 */
package com.symc.work.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.biz.TcVariantUtil;
import com.symc.common.util.IFConstants;
import com.symc.work.model.PartInfoVO;
import com.symc.work.model.ProductInfoVO;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * Function 하위 I/F
 * 
 * Class Name : PeFunctionIfService
 * Class Description : 
 * @date 2014. 2. 5.
 *
 */
public class PeFunctionIfService {
    
    TcPeIFService tcPeIFService;
    
    public void setTcPeIFService(TcPeIFService tcPeIFService) {
        this.tcPeIFService = tcPeIFService;
    }
    
    /**
     * Crate under Function BOM Part, BOMLine
     * 
     * @method createFunctionExpandItem 
     * @date 2014. 2. 5.
     * @param
     * @return StringBuffer
     * @exception
     * @throws
     * @see
     */
    public StringBuffer createFunctionExpandItem(Session session, ModularOption[] modularOptions, ProductInfoVO productInfoVO) throws Exception {
        StringBuffer log = new StringBuffer();
        BOMLineService bomLineService = new BOMLineService(session);
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        TcVariantUtil tcVariantUtil = new TcVariantUtil(session);
        BOMWindow bomWindow = null;
        // 이미 등록한 Function포함 하위 Item 그리고 BOM정보는 등록하지 않는다.
        HashMap<String, String> insertedFunctionItem = new HashMap<String, String>();
        // 중복 Item (IF_PE_PART_INFO) 등록 방지
        HashMap<String, ItemRevision> insertedItem = new HashMap<String, ItemRevision>();
        // Function ID를 Top으로 가지고 BOM Windows 생성 후 FULL 전개한다.        
        // I/F Function List
        List<HashMap<String, String>> productFunctionList = this.tcPeIFService.getIfPproductFunctionList(productInfoVO.getIfId());
        for (int i = 0; i < productFunctionList.size(); i++) {
            try {
                // 이미 등록한 Function이 있으면 Skip한다.
                if (insertedFunctionItem.containsKey(productFunctionList.get(i).get("FUNCTION_ID"))) {
                    continue;
                }
                // 3. Function ID를 Top으로 가지고 BOM Windows 생성 후 FULL 전개한다.
                insertedFunctionItem.put(productFunctionList.get(i).get("FUNCTION_ID"), productFunctionList.get(i).get("FUNCTION_ID"));
                ItemRevision functionRev = tcItemUtil.getRevisionInfo(productFunctionList.get(i).get("FUNCTION_ID"), productFunctionList.get(i).get("FUNCTION_REV_ID"));
                bomWindow = bomLineService.getCreateBOMWindow(functionRev, IFConstants.BOMVIEW_LATEST_RELEASED, productInfoVO.getIfDate());
                // 적용일자를 IF_DATE를 기입한다.
                ArrayList<ModelObject> bomlineList = new ArrayList<ModelObject>();
                bomLineService.expandAllLevel(bomWindow.get_top_line(), bomlineList);
                for (int j = 0; j < bomlineList.size(); j++) {
                    // BOMLine에서 revision정보를 가져온다.
                    ItemRevision itemRevision = (ItemRevision) bomlineList.get(j).getPropertyObject("bl_revision").getModelObjectValue();
                    // Function은 등록하지 않는다.
                    if (IFConstants.CLASS_TYPE_S7_FUNCTION_REVISION.equals(itemRevision.getTypeObject().getClassName())) {
                        continue;
                    }
                    // Class Type에 따른 속성정보 조회 후 PartInfo 생성
                    PartInfoVO partInfoVO = tcPeIFService.getTypeAttrItemVO(session, productInfoVO, itemRevision, log);
                    // Item 정보를 IF_PE_PART_INFO 테이블에 파트 정보를 등록
                    // Item 중복 등록 체크
                    if (!insertedItem.containsKey(itemRevision.get_item_id())) {
                        // partInfoVO에 Datset 정보 VO 설정
                        tcPeIFService.setVoDatasetFile(session, itemRevision, partInfoVO);
                        // Part 정보 저장
                        tcPeIFService.createPePartInfo(partInfoVO);
                        // Part Dataset FTP 전송
                        tcPeIFService.setDatasetFileSave(session, itemRevision, partInfoVO, log);
                        insertedItem.put(itemRevision.get_item_id(), itemRevision);
                    }
                    // BOM 정보를 IF_PE_BOM_CHANG 테이블에 BOM 정보를 등록
                    // Parent(부모의) Option정보를 등록한다.
                    String constrainsOptions = tcVariantUtil.getConstrainsOptions(modularOptions, (BOMLine) ((BOMLine) bomlineList.get(j)).get_bl_parent());
                    tcPeIFService.createPeBOMChange(session, productInfoVO, modularOptions, (BOMLine) bomlineList.get(j), constrainsOptions);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                // 8. BOMWindow close
                bomLineService.closeBOMWindow(bomWindow);
            }
        }
        // I/F PRODUCT 정보 테이블(IF_PE_PRODUCT)에 PART, BOM COUNT를 업데이트한다.
        tcPeIFService.changePeProductPartBomCount(productInfoVO.getIfId());
        return log;
    }
    
}
