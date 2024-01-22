/**
 * 
 */
package com.symc.plm.me.sdv.operation.paint;

import java.util.Map;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.utils.BOPLineUtility;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Class Name : CreatePaintLineOperation
 * Class Description : 
 * @date 2013. 12. 2.
 *
 *[SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
 *
 */
public class CreatePaintLineOperation extends AbstractSDVActionOperation{
    
    public static final String DEFAULT_REV_ID = "000";
    
    public int configId;
    
    private TCComponentItem createdItem = null;
    private IDataSet dataSet = null;
    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    
    public CreatePaintLineOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public CreatePaintLineOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, actionId, ownerId, parameters, dataset);
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
        
    }


    /* (non-Javadoc)
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        dataSet = getDataSet();
        
        // [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
        TCComponentItemRevision mecoRevision = isOwnedMECO();
        if(mecoRevision==null){
        	throw new Exception("Check MECO owning user");
        }
        
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();

        TCComponentBOMLine shopBOMLine = mfgApp.getBOMWindow().getTopBOMLine();
        TCComponentBOMLine targetBOMLine = mfgApp.getSelectedBOMLines()[0];
        
        String shopCode = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_SHOP_CODE);
        String productCode = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_PRODUCT_CODE);
        String ParallelStationNo = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_PARALLEL_LINE_NO);
        String Line = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_CODE);
        // Item Id 조합
        String itemId = "PTP" + "-" + shopCode + "-" + Line + "-" + productCode + "-" + ParallelStationNo;
        String korName = dataSet.getStringValue("lineInform", SDVPropertyConstant.ITEM_OBJECT_NAME);
        
        /**
         * 1.Line Item 생성
         * 
         */
        String selectedItemType = targetBOMLine.getItem().getType();
        // 선택된 BOMLine이 Shop일 경우
        if (selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)) {
            createdItem = SDVBOPUtilities.createItem(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, itemId, DEFAULT_REV_ID, korName, "");
        } else {
            // 선택된 BOMLine이 Shop이 아닐 경우 미할당 Shop에 Line을 생성한다.
            targetBOMLine = getTempBOPShop(shopBOMLine);
            createdItem = SDVBOPUtilities.createItem(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, itemId, DEFAULT_REV_ID, korName, "");
        }

 
        /**
         * 2.Line 속성 Update
         */
        setProperties();

        /**
         * 3.선택된 Shop BOP Line에 Line BOP Line을 추가
         */
        addLineBOPLine(targetBOMLine, createdItem);

        /**
         * 4.MECO에 생성된 Item Revision을 붙임
         */

        AddRevisionToMecoRevision(createdItem);
        /**
         * 윈도우 저장
         */
        shopBOMLine.window().save();
        
        SDVBOPUtilities.executeExpandOneLevel();
        
    }

    /**
     * 
     * @method AddRevisionToMecoRevision 
     * @date 2013. 12. 3.
     * @param
     * @return void
     * @throws Exception 
     * @exception
     * @throws
     * @see
     */
    private void AddRevisionToMecoRevision(TCComponentItem createdItem) throws Exception {
        String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItem mecoItem = SDVBOPUtilities.FindItem(mecoNo, SDVTypeConstant.MECO_ITEM);
        if (mecoItem == null)
            return;
        TCComponentItemRevision mecoItemRevision = mecoItem.getLatestItemRevision();
        mecoItemRevision.add("CMHasSolutionItem", createdItem.getLatestItemRevision());
        
    }
    
	/**
     * [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
     * MECO의 Owner 와 현재 Login 한 User가 다른 경우 Operation을 더이상 진행 할 수 없도록 한다.
     * @return
     */
    private TCComponentItemRevision isOwnedMECO(){
    	
    	TCComponentItemRevision mecoItemRevision =  null;
        String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        if(mecoNo!=null && mecoNo.trim().length()>0){
        	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoNo, (TCSession)this.getSession());
        	mecoItemRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
        }
		
        return mecoItemRevision;
    }

    /**
     * [SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
     * 
     * @method addOperationBOPLine 
     * @date 2013. 12. 3.
     * @param
     * @return void
     * @throws TCException 
     * @exception
     * @throws
     * @see
     */
    private void addLineBOPLine(TCComponentBOMLine targetBOMLine, TCComponentItem createdItem) throws TCException {
        TCComponentBOMLine newOpBOMLine = targetBOMLine.add(null, createdItem.getLatestItemRevision(), null, false);
        // 수량 입력
        //newOpBOMLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
        String findNo = dataSet.getStringValue("lineInform", SDVPropertyConstant.BL_SEQUENCE_NO);
        if(!findNo.isEmpty()){
            newOpBOMLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
        }
        
        // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
    	BOPLineUtility.updateLineToOperationAbsOccId(newOpBOMLine);
    }

    /**
     * 
     * @method setProperties 
     * @date 2013. 12. 3.
     * @param
     * @return void
     * @throws TCException 
     * @exception
     * @throws
     * @see
     */
    private void setProperties() throws TCException {
        /**
         * Item 속성 Update
         */
        String shopCode = dataSet.getStringValue("lineInform",SDVPropertyConstant.LINE_REV_SHOP_CODE);
        String lineCode = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_CODE);
        String productCode = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_PRODUCT_CODE);
        String ParallelStationNo = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_PARALLEL_LINE_NO);
        String lineKorName = dataSet.getStringValue("lineInform", SDVPropertyConstant.ITEM_OBJECT_NAME);
        String lineEngName = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_ENG_NAME);
        String jph = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_JPH);
        String allowance = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_ALLOWANCE);
        String vehicle_code = dataSet.getStringValue("lineInform", SDVPropertyConstant.LINE_REV_VEHICLE_CODE);
        //String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision)dataSet.getValue("mecoSelect", "mecoRev");
        
        TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();
        
        //createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, mecoNo);
        createdItemRevision.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, mecoRevision);
        createdItemRevision.setProperty(SDVPropertyConstant.LINE_REV_SHOP_CODE, shopCode);
        createdItemRevision.setProperty(SDVPropertyConstant.LINE_REV_CODE, lineCode);
        createdItemRevision.setProperty(SDVPropertyConstant.LINE_REV_PRODUCT_CODE, productCode);
        createdItemRevision.setProperty(SDVPropertyConstant.LINE_PARALLEL_LINE_NO, ParallelStationNo);
        createdItemRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, lineKorName);
        createdItemRevision.setProperty(SDVPropertyConstant.LINE_REV_VEHICLE_CODE, vehicle_code);
        if (!lineEngName.isEmpty())
            createdItemRevision.setProperty(SDVPropertyConstant.LINE_REV_ENG_NAME, lineEngName);
        
        if (jph != null)
            createdItemRevision.setProperty(SDVPropertyConstant.LINE_REV_JPH, jph);
        
        if (allowance != null)
            createdItemRevision.setProperty(SDVPropertyConstant.LINE_REV_ALLOWANCE, allowance);
        
        createdItemRevision.setProperty(SDVPropertyConstant.LINE_REV_PROCESS_TYPE, "P");
        
    }

    /**
     * 
     * @method getTempBOPShop 
     * @date 2013. 12. 3.
     * @param
     * @return TCComponentBOMLine
     * @throws TCException 
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine getTempBOPShop(TCComponentBOMLine topBOMLine) throws TCException {
        String shopId = topBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String[] idSplit = shopId.split("-");
        // 미할당 SHOP ID
        String tempShopId = idSplit[0] + "-" + idSplit[1] + "-TEMP-" + idSplit[2];

        AIFComponentContext[] aifComps = topBOMLine.getPrimary();
        for (AIFComponentContext aifComp : aifComps) {
            TCComponentBOMLine lineBOMLine = (TCComponentBOMLine) aifComp.getComponent();
            String lineId = lineBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            if (tempShopId.equals(lineId))
                return lineBOMLine;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
        
    }
}
