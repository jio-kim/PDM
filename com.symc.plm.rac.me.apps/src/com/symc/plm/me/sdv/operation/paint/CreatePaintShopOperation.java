/**
 * 
 */
package com.symc.plm.me.sdv.operation.paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.framework.services.IMFGContextService;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.CreateInstanceInput;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.create.ICreateInstanceInput;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.IOpenService;
import com.teamcenter.rac.util.OSGIUtil;

/**
 * Class Name : CreatePaintShopOperation
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Shop과 MProduct Link해제
 * 
 * @date 2013. 11. 7.
 * 
 */
public class CreatePaintShopOperation extends AbstractSDVActionOperation {
    private IDataSet dataSet = null;
    private TCSession tcSession = null;
    private TCComponentItem createdItem = null; // 생성된 Item
    private TCComponentBOMLine createdBOPLine = null; // 생성된 Item BOPLine
    private static String DEFAULT_REV_ID = "000";

    public CreatePaintShopOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        tcSession = (TCSession) getSession();

        dataSet = getDataSet();
        
        // [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
        TCComponentItemRevision mecoRevision = isOwnedMECO();
        if(mecoRevision==null){
        	throw new Exception("Check MECO owning user");
        }

        String itemId = makeItemId();

        String shopKorName = dataSet.getStringValue("shopInform", SDVPropertyConstant.ITEM_OBJECT_NAME);
        /**
         * Item 생성
         */
        createRootItem(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, itemId, shopKorName);

        /**
         * 속성정보 입력
         */
        setProperties();

        /**
         * MECO에 생성된 Item Revision을 붙임
         */
        AddRevisionToMecoRevision(createdItem);
        /**
         * BOP Window를 Open함
         */
        OpenCreatedItemBOMWindow();

    }

    /**
     * Root Item을 생성함
     * 
     * @method createRootItem
     * @date 2013. 11. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createRootItem(String itemType, String itemId, String objectName) throws Exception {

        IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(tcSession, itemType);
        IMFGContextService mfgContextService = (IMFGContextService) OSGIUtil.getService(Activator.getDefault(), IMFGContextService.class.getName());

        CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
        createInput.add(SDVPropertyConstant.ITEM_ITEM_ID, itemId);
        createInput.add(SDVPropertyConstant.ITEM_OBJECT_NAME, objectName);
        createInput.add(SDVPropertyConstant.ITEM_REVISION_ID, DEFAULT_REV_ID);

        List<ICreateInstanceInput> inPutList = new ArrayList<ICreateInstanceInput>();

        Map<?, ?> secondaryCreateDefinitions = createDefinition.getSecondaryCreateDefinitions();
        List<?> revList = (ArrayList<?>) secondaryCreateDefinitions.get("revision");
        IBOCreateDefinition createDefinitionRev = (IBOCreateDefinition) revList.get(0);
        CreateInstanceInput createInputRev = new CreateInstanceInput(createDefinitionRev);
        createInputRev.add(SDVPropertyConstant.ITEM_REVISION_ID, DEFAULT_REV_ID);
        inPutList.add(createInputRev);

        inPutList.add(createInput);

        try {
//            List<TCComponent> createCompList = mfgContextService.createContext(createDefinition, inPutList, null, null);
            List<TCComponent> createCompList = mfgContextService.createContext(createDefinition, inPutList, null, null, null, null);

            createdItem = (TCComponentItem) createCompList.get(0);
            createdBOPLine = (TCComponentBOMLine) createCompList.get(1);

        } catch (TCException e) {
            e.printStackTrace();
        }

    }

    /**
     * 속성정보 입력
     * 
     * @method SetProperties
     * @date 2013. 11. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setProperties() throws Exception {

        /**
         * Item 속성 Update
         */
        String shopEngName = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_ENG_NAME);
        String vehicleKorName = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME);
        String vehicleEngName = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME);
        createdItem.setProperty(SDVPropertyConstant.SHOP_ENG_NAME, shopEngName);
        createdItem.setProperty(SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME, vehicleKorName);
        createdItem.setProperty(SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME, vehicleEngName);
        //createdItem.setProperty("uom_tag", "EA");

        /**
         * Revision 속성 Update
         */
        TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();

        //String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision)dataSet.getValue("mecoSelect", "mecoRev");

        String shopCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_SHOP_CODE);
        String productCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);

        String jph = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_JPH);
        String allowance = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_ALLOWANCE);
        String vehicleCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_VEHICLE_CODE);

        createdItemRevision.setProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE, shopCode);
        createdItemRevision.setProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, productCode);
        createdItemRevision.setProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE, vehicleCode);
        //createdItemRevision.setProperty(SDVPropertyConstant.SHOP_REV_MECO_NO, mecoNo);
        createdItemRevision.setProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE, "P");
        createdItemRevision.setReferenceProperty(SDVPropertyConstant.SHOP_REV_MECO_NO, mecoRevision);
        
        //[SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
        if (!jph.isEmpty())
            //createdItemRevision.setIntProperty(SDVPropertyConstant.SHOP_REV_JPH, Integer.parseInt(jph));
            createdItemRevision.setDoubleProperty(SDVPropertyConstant.SHOP_REV_JPH, Double.parseDouble(jph));
        if (!allowance.isEmpty())
            createdItemRevision.setDoubleProperty(SDVPropertyConstant.SHOP_REV_ALLOWANCE, Double.parseDouble(allowance));
    }

    /**
     * 
     * 생성된 BOP를 Open함
     * 
     * @method OpenCreatedItemBOMWindow
     * @date 2013. 11. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void OpenCreatedItemBOMWindow() {

        OpenBomWindowOperation operation = new OpenBomWindowOperation();
        operation.addOperationListener(new InterfaceAIFOperationListener() {

            @Override
            public void endOperation() {
                try {
                    TCComponentItemRevision mTopItemRevision = (TCComponentItemRevision) dataSet.getValue("shopInform", SDVTypeConstant.BOP_MPRODUCT_REVISION);

                    /**
                     * Shop을 대상 제품으로 연결함
                     */
                    /* [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Shop과 MProduct Link해제
                    if (mTopItemRevision != null)
                        createdBOPLine.getItemRevision().add("IMAN_METarget", mTopItemRevision);
                     */

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void startOperation(String arg0) {

            }

        });
        tcSession.queueOperation(operation);
    }

    /**
     * BOM Window Open
     * Class Name : OpenBomWindowOperation
     * Class Description :
     * 
     * @date 2013. 11. 11.
     * 
     */
    public class OpenBomWindowOperation extends AbstractAIFOperation {

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
         */
        @Override
        public void executeOperation() throws Exception {
            IOpenService openService = AIFUtility.getCurrentOpenService();
            openService.open(createdBOPLine.window());

        }

    }

    /**
     * MECO에 생성된 Item Revision을 Solution Item에 붙인다.
     * 
     * @method AddRevisionToMecoRevision
     * @date 2013. 11. 22.
     * @param
     * @return void
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {

    }

    /**
     * 
     * Item Id를 조합함
     * 
     * @method makeItemId
     * @date 2013. 12. 3.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String makeItemId() {
        String itemId = "";
        String shopCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_SHOP_CODE);
        String productCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
        itemId = SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shopCode + "-" + productCode;
        return itemId;
    }

}
