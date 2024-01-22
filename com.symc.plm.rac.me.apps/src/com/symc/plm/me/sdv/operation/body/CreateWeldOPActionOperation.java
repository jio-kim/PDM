package com.symc.plm.me.sdv.operation.body;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;


/**
 * [SR140702-044][20140702] jwLee 용접공법 ID체계 변경 (1. LOV추가, 2. Serial No.체계 변경, 3. 용접공법 중복 검사 소스 이동)
 * [SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
 * [NON-SR][20150729] shcho, 옵션 값 가져오는 위치를 BL_VARIANT_CONDITION 에서 BL_OCC_MVL_CONDITION으로 변경
 * 
*/

public class CreateWeldOPActionOperation extends AbstractSDVActionOperation {

    private IDataSet dataSet = null;

    private final String firstRevision = SDVPropertyConstant.ITEM_REV_ID_ROOT;

    private TCSession session;


    /**
     * @param operationId
     * @param ownerId
     * @param dataSet
     */
    public CreateWeldOPActionOperation(int operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
        this.dataSet = dataSet;
    }

    public CreateWeldOPActionOperation(String operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
        this.dataSet = dataSet;
    }

    public CreateWeldOPActionOperation(int operationId, String ownerId,  Map<String, Object> parameters, IDataSet dataSet) {
        super(operationId, ownerId, parameters, dataSet);
        this.dataSet = dataSet;
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception
    {
        session = CustomUtil.getTCSession();
        Markpoint mp = new Markpoint(session);
        try
        {
            dataSet = getDataSet();

            // MECO 검색 뷰에서 검색한 결과 데이터를 가져온다
            Object meco_no = dataSet.getValue("searchMECO", SDVPropertyConstant.SHOP_REV_MECO_NO);
            
            // [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
            if(meco_no!=null){
            	if(meco_no instanceof TCComponentItemRevision){
            		TCComponentItemRevision mecoRevision = isOwnedMECO((TCComponentItemRevision)meco_no);
            		if(mecoRevision==null){
            			throw new Exception("Check MECO owning user");
            		}
            	}
            }

            // createWeldOP 뷰에서 데이터를 가져온다
            IDataMap createWeldOP = dataSet.getDataMap("createWeldOP");
            TCComponentBOPLine targetOP = (TCComponentBOPLine) createWeldOP.getValue("targetOP");
            TCComponentBOPLine gunItem = (TCComponentBOPLine) createWeldOP.getValue("gunID");

            String serialNO = createWeldOP.getStringValue("serialNO");
            String weldOpOption = createWeldOP.getStringValue("weldOpOption");
            Object isAltObj = createWeldOP.getValue(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP);
            String altPrefix = createWeldOP.getStringValue(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);


            // 신규생성할 WeldOP 에 ID 를 만든다

            //String id = (((Boolean) isAltObj) ? altPrefix + "-" : "") + targetOP.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "-WEOP-" + serialNO;
            String id = targetOP.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "-WEOP-" + serialNO + "-" + weldOpOption;
            //용접공법에 포함되는 Gun / Robot 이 있는 Plant Window 정보를 가져온다
            TCComponentBOMLine plantLine = getPlantBOMLine(targetOP);
            TCComponentBOMWindow plantWindow = plantLine.window();

            // WeldOP 를 생성한다
            TCComponentItem item = CustomUtil.createItem(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM, id, firstRevision, id, "WeldOP");
            TCComponentItemRevision itemRevision = item.getLatestItemRevision();
            // 생성한 WeldOP 에 Reference 속성을 추가한다 (TargetOperation)
            itemRevision.setReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP, targetOP.getItemRevision());
            // 생성한 WeldOP를 해당 TargetOP가 속한 공정에 붙인다
            TCComponentBOPLine station = (TCComponentBOPLine)targetOP.parent();
            TCComponentBOPLine weldOP = (TCComponentBOPLine)station.add(null, itemRevision, null, false);
            
            // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
        	BOPLineUtility.updateLineToOperationAbsOccId(weldOP);

            // 새로 생성할 WeldOP 에 첨부할 Gun과 Robot 아이템을 가져온다
//            TCComponentBOPLine plantItem = (TCComponentBOPLine) gunItem.parent();
//
//            AIFComponentContext[] plantChildItem = plantItem.getChildren();

            //MECO 에 연결한다
            if (meco_no != null && !meco_no.equals(""))
            {
                itemRevision.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, (TCComponent) meco_no);

                // MECO Solution에 연결
                ((TCComponentChangeItemRevision) meco_no).add(SDVTypeConstant.MECO_SOLUTION_ITEM, itemRevision);
            }
            // TargetOperation 의 옵션을 가져와서 새로 생성한 WeldOP 의 추가한다
            //[NON-SR][20150729] shcho, 옵션 값 가져오는 위치를 BL_VARIANT_CONDITION 에서 BL_OCC_MVL_CONDITION으로 변경
            String option = targetOP.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);

            if (!option.equals(""))
            {
                TCVariantService svc = session.getVariantService();
                svc.setLineMvlCondition(weldOP, option);
            }

            // ALT BOP 인 경우 추가로 속성을 넣는다
            if (((Boolean) isAltObj))
            {
                itemRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, true);
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, altPrefix);
            }

            // 생성한 WeldOP에 하위에 Gun과 Robot 을 할당한다
            TCComponentMEAppearancePathNode[] linkedAppearances = gunItem.parent().askLinkedAppearances(false);
            TCComponentBOMLine linkedBOMLine = null;

            for (TCComponentMEAppearancePathNode linkedAppearance : linkedAppearances)
            {
                linkedBOMLine = plantWindow.getBOMLineFromAppearancePathNode(linkedAppearance, plantLine);
            }
            ArrayList<InterfaceAIFComponent> resourceList = new ArrayList<InterfaceAIFComponent>();
            AIFComponentContext[] plantChildItem = linkedBOMLine.getChildren();
            for (AIFComponentContext plantChild : plantChildItem)
            {
                if (resourceList.size() == 2)
                    break;

                TCComponentBOMLine plantResource = (TCComponentBOMLine) plantChild.getComponent();
                if (plantResource.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM))
                    resourceList.add(plantChild.getComponent());

                if (plantResource.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).equals(gunItem.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID)))
                    resourceList.add(plantChild.getComponent());
            }

            //[SR140820-017][20150209]shcho, BOM Line 수량 표시 일관성 유지 필요의 일환으로 Shop-Line-공정-공법에 수량정보는 공백으로 한다.
            //weldOP.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");

            SDVBOPUtilities.connectObject(weldOP, resourceList, "MEWorkArea");
            weldOP.save();

            //SDVBOPUtilities.executeExpandOneLevel();
        }
        catch (Exception ex)
        {
            mp.rollBack();
            setErrorMessage(ex.getMessage());
            setExecuteError(ex);
            throw ex;
        }

        mp.forget();
    }
    
	/**
	 * [SR160224-028][20160328] taeku.jeong MECO Owner 확인기능 추가
	 * MECO의 Owner 와 현재 Login 한 User가 다른 경우 Operation을 더이상 진행 할 수 없도록 한다.
	 * @return
	 */
	private TCComponentItemRevision isOwnedMECO(TCComponentItemRevision mecoRevision){
		
		TCComponentItemRevision ownMecoItemRevision = null;
	
    	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoRevision, (TCSession)this.getSession());
    	ownMecoItemRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
		
	    return ownMecoItemRevision;
	}

    /**
     *
     *
     * @method getPlantBOMLine
     * @date 2013. 12. 27.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    protected TCComponentBOMLine getPlantBOMLine(TCComponentBOMLine selectBOMLine) {
        TCComponentBOMLine rootBomline;
        String rootBomView = null;
        String targetPlant = null;
        try {
            TCComponentItemRevision itemRevision = selectBOMLine.window().getTopBOMLine().getItemRevision();
            targetPlant = getPlant(itemRevision);
        } catch (TCException e) {
            e.printStackTrace();
        }
        IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
        for (IViewReference viewRerence : arrayOfIViewReference) {
            IViewPart localIViewPart = viewRerence.getView(false);
            if (localIViewPart == null)
                continue;
            CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
            if (cmeBOMTreeTable == null)
                continue;

            rootBomline = cmeBOMTreeTable.getBOMRoot();
            try {
                rootBomView = rootBomline.getProperty("bl_item_item_id");
            } catch (TCException e) {
                e.printStackTrace();
            }
            if (targetPlant != null && rootBomView != null)
            {
                if (targetPlant.equals(rootBomView))
                    return rootBomline;
            }

        }
        return null;
    }

    /**
     *
     *
     * @method getPlant
     * @date 2013. 12. 27.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    protected static String getPlant(TCComponentItemRevision revision) throws TCException {
        String plantType = null;
        String plantId = null;
        TCComponent[] plant = revision.getRelatedComponents(SDVTypeConstant.MFG_WORKAREA);
        if (plant.length == 1)
        {
            plantType = plant[0].getProperty("object_type");
            if (plantType.equals("PlantShopRevision"))
                return plantId = plant[0].getProperty("item_id");
        }
        return plantId;
    }

}
