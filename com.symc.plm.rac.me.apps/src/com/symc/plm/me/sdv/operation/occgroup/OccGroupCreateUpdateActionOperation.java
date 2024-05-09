package com.symc.plm.me.sdv.operation.occgroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.symc.plm.activator.Activator;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.cme.framework.services.IMFGContextService;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.cme.kernel.bvr.BVRKernelUtils;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.CreateInstanceInput;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.create.ICreateInstanceInput;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentAppearanceGroup;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentStructureContext;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.PlatformHelper;

/**
 *  [SR150122-027][20150309] shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 *  [SR150529-025][20150828] shcho, 누적파트 생성/업데이트 방법 추가 개발 - Shop, Line 단위로 일괄적으로 누적파트 생성(업데이트) 할 수 있도록 기능 개선
 * 
 */
public class OccGroupCreateUpdateActionOperation extends AbstractSDVActionOperation {

    private TCComponentBOMLine mProductBomLine;
    private final String serviceClassName = "com.kgm.service.BopPertService";
    private TCSession tcsession;
    private String altOccGroupPreFix = "";
    private List<String> bomLineIDList;
    
    // 수행결과 메시지를 담는 변수
    private StringBuffer resultMessage = null;

    public OccGroupCreateUpdateActionOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public OccGroupCreateUpdateActionOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    public OccGroupCreateUpdateActionOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {
        if(resultMessage.length() > 0) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), resultMessage.toString(), "INFORMATION", MessageBox.INFORMATION);
        }
    }

    @Override
    public void executeOperation() throws Exception
    {
        TCComponentAppearanceGroup occGroup = null;
        TCComponentAppGroupBOPLine occGroupStationBopLine = null;
        resultMessage = new StringBuffer();

        // Preference 의 등록한 OccGroup 의 사용할 PreFix 를 가져온다
        tcsession = CustomUtil.getTCSession();
        altOccGroupPreFix = tcsession.getPreferenceService().getStringValue("OccurrenceGroupPreFix");

        IDataSet dataset = getDataSet();
        if (dataset != null)
        {
            @SuppressWarnings("unchecked")
            ArrayList<TCComponentBOPLine> stationBOPLineList = (ArrayList<TCComponentBOPLine>) dataset.getListValue("occGroupCreateUpdateView", "targetStation");
            TCComponentBOPLine targetShopBOPLine = (TCComponentBOPLine) dataset.getData("targetShop");
            
            // 현재 열려있는 MPP BOMWindow 중에서 BOPShop 과 연관되는 MProduct BOMWindow를 가져온다
            mProductBomLine = getMBOMLine(targetShopBOPLine);

            if (mProductBomLine == null) {
                throw new Exception("Cannot fine M-Product BOMLine.");
            }
            
            // TODO : 잠금처리 하여 작업중에 다른 사람이 동시 작업을 할 수 없도록 하는 로직 추가 필요.
            //mProductBomLine.lock();
            
            // 1. M-Product BomWindow 에서 OccurrenceGroupWindow를 오픈한다
            ocgWindowsOpen();

            for (TCComponentBOPLine stationBOPLine : stationBOPLineList) {
                tcsession.setStatus("Updating... : ".concat(stationBOPLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID)));

                // 2. Station ID와 동일한 OccGroup 이 있으면 가져오고, 없으면 생성한다.
                occGroup = getOccWindow(stationBOPLine);

                if(occGroup == null) {
                    continue;
                }
                
                // 3. Station OccGroup의 BopLine 을 가져온다
                occGroupStationBopLine = getOccGroupStationBopLine(occGroup);

                // 4. Station OccGroup BopLine 하위의 EndItemBOMLine을 모두 제거한다.
                occGroupStationBopLine.remove("bl_occgrp_visible_lines", SDVBOPUtilities.getChildrenBOMLine(occGroupStationBopLine));

                // 5. Station OccGroup BopLine 하위에 EndItemBOMLine 을 추가한다.
                setPnlItem(occGroupStationBopLine, stationBOPLine);

                // 6. Station OccGroup BopLine 을 Station BOPLine(공정) 하위에 할당한다.
                pasteStationOccGroup(stationBOPLine, occGroup);

            }
            
            //mProductBomLine.unlock();
        }
    }

    /**
     *  1. OccWindow 가 생성이 되어 있는지 체크한다
     *  2. 없으면 생성한다
     *
     * @method getOccWindowCheck
     * @date 2014. 3. 31.
     * @param
     * @return boolean
     * @throws Exception 
     * @exception
     * @throws
     * @see
     */
    private TCComponentAppearanceGroup getOccWindow(TCComponentBOPLine stationBopLine) throws Exception{
        TCComponentAppearanceGroup appGroupTop = null;
        TCComponentAppGroupBOPLine appGroupTopBopLine = null;

        String stationID = stationBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        List<TCComponentBOMWindow> occWindows = mProductBomLine.window().getOccurrenceGroupWindows();
        for (TCComponentBOMWindow occWindow : occWindows) {
            appGroupTopBopLine = (TCComponentAppGroupBOPLine)occWindow.getTopBOMLine();
            if(appGroupTopBopLine.getProperty(SDVPropertyConstant.BL_FORMATTED_TITLE).equals(altOccGroupPreFix + stationID)){
                appGroupTop = (TCComponentAppearanceGroup) appGroupTopBopLine.getComponentIdentifierInContext();
                return appGroupTop;
            }
        }
        
        // stationBOPLine 이 Release 또는 쓰기 권한이 있을 경우에만 신규 생성을 한다.
        if(checkWriteAccess(stationBopLine, resultMessage)) {
            appGroupTop = createOccGroup(mProductBomLine, stationID);            
        }
        

        return appGroupTop;
    }

    /**
     *       MPP의 활성화 되어있는 view 에서 BOMView 를 선택하여 BOMLine 정보를 담아간다
     *       MPP 의 여러개에 TAB View 중에 cc 정보와 TYPE 정보를 비교하여 선택한 BOP 탭과
     *       연관되어 있는 BOMVIEW 데이터를 가져온다
     *       
     *  [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
     *
     * @method getMBOMLine
     * @date 2014. 3. 31.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine getMBOMLine(TCComponentBOPLine selectBOPLine) {
        String rootBomView = null;
        String targetProduct = null;
        TCComponentBOMLine rootBomline = null;
        try {
            TCComponentItemRevision itemRevision = selectBOPLine.window().getTopBOMLine().getItemRevision();
            String productCode = itemRevision.getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            if(productCode != null) {
                targetProduct = "M".concat(productCode.substring(1));
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
        IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
        //ISelection iSelection = PlatformHelper.getCurrentPage().getSelection();
        for (IViewReference viewRerence : arrayOfIViewReference) {
            IViewPart localIViewPart = viewRerence.getView(false);
            if (localIViewPart == null){
                continue;
            }
            CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
            if (cmeBOMTreeTable == null){
                continue;
            }

            rootBomline = cmeBOMTreeTable.getBOMRoot();
            try {
                rootBomView = rootBomline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            } catch (TCException e) {
                e.printStackTrace();
            }
            if (targetProduct != null && rootBomView != null){
                if (targetProduct.equals(rootBomView)){
                    return rootBomline;
                }
            }
        }
        return null;
    }


    /**
     * BOP 최상의인 SHOP에 연결되어 있는 BOMView 정보(M7_Product) 를 확인하여 M_Product ID 정보를 반환 한다
     *
     * @method getMProduct
     * @date 2014. 3. 31.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    /*
     *  [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Shop과 MProduct Link해제로 더이상 사용안함.
    private String getMProduct(TCComponentItemRevision revision) throws TCException {
        String mProductType = null;
        String mProductId = null;
        TCComponent[] mProduct = revision.getRelatedComponents(SDVTypeConstant.MFG_TARGETS);
        if (mProduct.length == 1){
            mProductType = mProduct[0].getProperty(SDVPropertyConstant.ITEM_OBJECT_TYPE);
            if (mProductType.equals(SDVTypeConstant.EBOM_MPRODUCT_REV)){
                return mProductId = mProduct[0].getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            }
        }
        return mProductId;
    }
     */


    /**
     * occWindow 를 생성한다
     *
     * @method createOccGroupWindow
     * @date 2014. 3. 31.
     * @param
     * @return TCComponentAppGroupBOPLine
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    private TCComponentAppearanceGroup createOccGroup(TCComponentBOMLine mProductBomLine, String stationID) throws TCException{

        TCSession tcSession = CustomUtil.getTCSession();
        IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(tcSession, "AppearanceGroup");
        IMFGContextService mfgContextService = (IMFGContextService) OSGIUtil.getService(Activator.getDefault(), IMFGContextService.class.getName());

        // 생성시 필요한 입력값
        CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
        createInput.add(SDVPropertyConstant.ITEM_OBJECT_NAME, altOccGroupPreFix + stationID);
        createInput.add(SDVPropertyConstant.ITEM_OBJECT_TYPE, "AppearanceGroup");
        List<ICreateInstanceInput> inPutList = new ArrayList<ICreateInstanceInput>();
        inPutList.add(createInput);
        List<TCComponent> comps = mfgContextService.createView(createDefinition, inPutList, mProductBomLine);
        comps.get(0).save();
        mProductBomLine.getItem().refresh();

        return (TCComponentAppearanceGroup)comps.get(0);

    }

    /**
     * 기존에 생성된 OccGroup 이 있다면 삭제 한다
     *
     * @method removeOccGroup
     * @date 2014. 3. 31.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    /*
    private void removeOccGroup(TCComponentAppearanceGroup occGroupTop, TCComponentBOPLine stationBopLine) throws TCException{
        String stationID = stationBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);

        // BOP 에서 공정에 붙은 OccGroup 를 잘라낸다
        if (stationBopLine.getChildren().length > 0) {
            for (AIFComponentContext stationBopLineChild : stationBopLine.getChildren()) {
                if (stationBopLineChild.getComponent() instanceof TCComponentAppGroupBOPLine){
                    TCComponentAppGroupBOPLine occBopLine = (TCComponentAppGroupBOPLine)stationBopLineChild.getComponent();
                    TCComponentBOMLine parentBomLine = occBopLine.parent();
                    ArrayList<TCComponentBOMLine> deleteBopLines = new ArrayList<TCComponentBOMLine>();
                    deleteBopLines.add(occBopLine);
                    SDVBOPUtilities.disconnectObjects(parentBomLine, deleteBopLines);
                    //occBopLine.cut();
                    parentBomLine.save();
                    parentBomLine.refresh();
//                    if (occBopLine.getProperty(SDVPropertyConstant.BL_FORMATTED_TITLE).equals(altOccGroupPreFix + stationID)) {
//                        occBopLine.remove(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_GROUP, occBopLine);
//                    }
                }
            }
        }

        // OccGroup 에서 삭제함
        if (occGroupTop.getReferenceListProperty("appearance_groups").length > 0) {
            TCComponent referenceOccGroupList[] = occGroupTop.getReferenceListProperty("appearance_groups");
            for (TCComponent referenceOccGroup : referenceOccGroupList) {
                if (referenceOccGroup instanceof TCComponentAppearanceGroup){
                    TCComponentAppearanceGroup occGroup = (TCComponentAppearanceGroup)referenceOccGroup;
                    if (occGroup.getProperty("object_string").equals(altOccGroupPreFix + stationID)) {
                        occGroupTop.removeAppGroup(occGroup);
                        occGroupTop.save();
                        occGroupTop.refresh();
                    }
                }
            }
        }

    }
     */
    
    /**
     * OccGroup 을 생성한다
     *
     * @method createStationOccGroup
     * @date 2014. 3. 31.
     * @param
     * @return void
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    /*
    private TCComponentAppearanceGroup createStationOccGroup(TCComponentAppearanceGroup occGroupTopLine, String stationID) throws TCException{
        TCSession tcSession = CustomUtil.getTCSession();
        IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(tcSession, "AppearanceGroup");
        IMFGContextService mfgContextService = (IMFGContextService) OSGIUtil.getService(Activator.getDefault(), IMFGContextService.class.getName());

        // 생성시 필요한 입력값
        CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
        createInput.add(SDVPropertyConstant.ITEM_OBJECT_NAME, altOccGroupPreFix + stationID);
        createInput.add(SDVPropertyConstant.ITEM_OBJECT_TYPE, "AppearanceGroup");
        List<ICreateInstanceInput> inPutList = new ArrayList<ICreateInstanceInput>();
        inPutList.add(createInput);

        List<TCComponent> comps = mfgContextService.createView(createDefinition, inPutList, mProductBomLine);
        comps.get(0).save();

        // 생성된 OccGroup 을 루트 OccGroup 의 붙인다
        occGroupTopLine.addAppGroup((TCComponentAppearanceGroup) comps.get(0));

        mProductBomLine.getItem().refresh();
        occGroupTopLine.refresh();

        return (TCComponentAppearanceGroup) comps.get(0);
    }
    */


    /**
     * 생성했던 StationOccGroupBopLine 을 반환한다
     *
     * @method getOccGroupStationBopLine
     * @date 2014. 4. 3.
     * @param
     * @return TCComponentAppGroupBOPLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentAppGroupBOPLine getOccGroupStationBopLine(TCComponentAppearanceGroup stationOccGroup) throws TCException{
        TCComponentAppGroupBOPLine stationOccGroupBopLine = null;
        List<TCComponentBOMWindow> occGroupWindows = mProductBomLine.window().getOccurrenceGroupWindows();

        for (TCComponentBOMWindow occGroupWindow : occGroupWindows) {
            TCComponentBOMLine occBopLine = occGroupWindow.getTopBOMLine();
            if (occBopLine.getProperty(SDVPropertyConstant.BL_FORMATTED_TITLE).equals(stationOccGroup.getProperty("object_string"))) {
                return stationOccGroupBopLine = (TCComponentAppGroupBOPLine) occBopLine;
            }
        }

        return stationOccGroupBopLine;
    }

    /**
     * 공정의 OccGroup 을 할당한다
     *
     * @method pasteStationOccGroup
     * @date 2014. 4. 1.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void pasteStationOccGroup(TCComponentBOPLine targetStationBOPLine, TCComponentAppearanceGroup stationOccGroup) throws Exception{
        TCComponentBOMLine[] childrenBOMLine = SDVBOPUtilities.getChildrenBOMLine(targetStationBOPLine);
        
        for (TCComponentBOMLine childBOMLine : childrenBOMLine) {
            if(childBOMLine instanceof TCComponentAppGroupBOPLine) {
                return;
            }
        }
        
        // stationBOPLine 이 Release 또는 쓰기 권한이 있을 경우에만 Add 한다.
        if(checkWriteAccess(targetStationBOPLine, resultMessage)) {
            TCComponentBOMLine occStationBopLine = targetStationBOPLine.add(stationOccGroup, false, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_GROUP);
            occStationBopLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, "0");
        }

        /*
        // MproductItem 의 붙어 있는 Station occGroup 를 Cut 한다
        TCComponent[] stationList = {stationOccGroup};
        mProductBomLine.getItem().cutOperation("IMAN_MEView", stationList);
        mProductBomLine.getItem().refresh();
        //ocgWindowsOpen();
        */
    }

    /**
     *  판넬을 OccGroup 의 할당한다
     *
     * @method setPnlItem
     * @date 2014. 4. 1.
     * @param
     * @return void
     * @throws Exception
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    private void setPnlItem(TCComponentAppGroupBOPLine stationOccGroupBOPLine, TCComponentBOPLine targetStation) throws TCException, Exception {
        TCComponentBOPLine shopBopLine = (TCComponentBOPLine) targetStation.window().getTopBOMLine();
        // 현재 공정의 후단 공정 목록을 가져온다
        List<String> decessorsList = getDecessorsList(shopBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), targetStation.getProperty(SDVPropertyConstant.BL_ITEM_ID));

        String shopAbsOccID = shopBopLine.getProperty(SDVPropertyConstant.BL_ITEM_PUID);
        // 후단 공정 하위 PART 아이템에 bl_abs_occ_id 를 추출한다
        List<String> decessorsPartAbsOccIDList = getDecessorsPartAbsOccIDList(decessorsList, shopAbsOccID);

        // Occurrence Group 에 담는 BOMLine 리스트 (중복 ADD 를 막기위해 필요)
        bomLineIDList = new ArrayList<String>();

        if (decessorsPartAbsOccIDList != null) {
            for (String partAbsOccID : decessorsPartAbsOccIDList) {
                TCComponentBOMLine[] mBomLine = mProductBomLine.window().findAppearance(partAbsOccID);
                if (mBomLine != null && mBomLine.length > 0) {
                	
                	String currentItemId = ((TCComponentBOMLine)mBomLine[0]).getProperty(SDVPropertyConstant.BL_ITEM_ID);
                	System.out.println("currentItemId = "+currentItemId);
                	boolean isWPart = false;
                	if(currentItemId!=null && currentItemId.toUpperCase().startsWith("W")==true){
                		isWPart = true;
                	}
                	
                    if (isWPart==false && !bomLineIDList.contains(currentItemId)) {
                        TCComponentBOMLine occGroupChildBOMLine = stationOccGroupBOPLine.addBOMLine(stationOccGroupBOPLine, (TCComponentBOMLine)mBomLine[0], "");
                        if(occGroupChildBOMLine!=null){
                            // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation에 bl_abs_occ_id 값을 설정한다. 
                        	BOPLineUtility.updateLineToOperationAbsOccId(occGroupChildBOMLine);
                        }
                        bomLineIDList.add(((TCComponentBOMLine)mBomLine[0]).getProperty(SDVPropertyConstant.BL_ITEM_ID));
                    }
                }
            }
        }
    }

    /**
     * 중복 add 를 피하기위해 기존에 넣은 BOMLine 을 저장한다 (비교하기 위해 필요)
     *
     * @method setBomLineAddList
     * @date 2014. 4. 9.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    /*
    private void setBomLineAddList(TCComponentBOMLine bomLine) throws TCException {
        bomLineIDList.add(bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
        for (AIFComponentContext bomLineChilds : bomLine.getChildren()) {
            TCComponentBOMLine bomLineChild = (TCComponentBOMLine)bomLineChilds.getComponent();
            if (bomLineChild.getChildrenCount() > 0) {
                setBomLineAddList(bomLineChild);
            }else{
                bomLineIDList.add(bomLineChild.getProperty(SDVPropertyConstant.BL_ITEM_ID));
            }
        }
    }
    */

    /**
     *  후단 공정 E/I 의 bl_abs_occ_id 를 가져온다 (DB)
     *
     * @method getDecessorsPartAbsOccIDList
     * @date 2014. 4. 2.
     * @param
     * @return List<String>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<String> getDecessorsPartAbsOccIDList(List<String> decessorsList, String shopAbsOccID) throws Exception {
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
        ArrayList<String> decessorsPartAbsOccIDList = new ArrayList<String>();

        DataSet ds = new DataSet();
        ds.put("SHOP_ITEM_PUID", shopAbsOccID);
        ds.put("STATION_LIST", decessorsList);

        ArrayList<HashMap<String, Object>> results;
        
        if(decessorsList==null || (decessorsList!=null && decessorsList.size()<1)){
        	return null;
        }else{
        	results = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectBopStationDecessorsEndItemList", ds);
        }

        if (results == null) return null;

        for(HashMap result : results){
            if (result.get("ABS_OCC_ID") != null && !result.get("ABS_OCC_ID").equals("")) {
                decessorsPartAbsOccIDList.add((String) result.get("ABS_OCC_ID"));
            }
        }
        return decessorsPartAbsOccIDList;
    }

    /**
     * 후단 공정 리스트를 가져온다 (DB)
     *
     * @method getDecessorsList
     * @date 2014. 4. 2.
     * @param
     * @return List<String>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<String> getDecessorsList(String shopID, String stationID) throws Exception{
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
        ArrayList<String> decessorsList = new ArrayList<String>();

        DataSet ds = new DataSet();
        ds.put("SHOP_ID", shopID);
        ds.put("STATION_ID", stationID);
        
        if(stationID==null || (stationID!=null && stationID.trim().length()<1)){
        	return decessorsList;
        }

        ArrayList<HashMap<String, Object>> results;

        results = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectStationDecessorsList", ds);
        for(HashMap result : results){
            decessorsList.add((String) result.get("ID"));
        }
        return decessorsList;
    }

    /**
     * M-Product BomWindow 에서 OccurrenceGroupWindow 가 닫혀 있으면
     * OccurrenceGroup BOMLine 정보를 가져올수 없기 때문에 오픈한다
     * @param stationBopLine 
     *
     * @method ocgWindowsOpen
     * @date 2014. 4. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void ocgWindowsOpen() throws TCException {
        ArrayList<TCComponent> toShow = new ArrayList<TCComponent>();
        TCComponent[] atccomponents = BVRKernelUtils.getRefreshedOGs(mProductBomLine.window());
        List<TCComponentBOMWindow> occWindows = mProductBomLine.window().getOccurrenceGroupWindows();

        for (TCComponent atccomponent : atccomponents)
        {
            boolean isOpened = false;
            for (TCComponentBOMWindow occWindow : occWindows) 
            {
                if (occWindow.getTopBOMLine().getProperty(SDVPropertyConstant.BL_FORMATTED_TITLE).equals(atccomponent.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME))) 
                {
                    isOpened = true;
                    break;
                }
            }

            if (!isOpened) 
            {
                toShow.add(atccomponent);
            }
        }

        if(toShow.size() > 0) 
        {
            Object topObject = mProductBomLine.window().getTopBOMLine();

            TCComponentStructureContext localTCComponentStructureContext = mProductBomLine.window().getSC();

            Object osgService = (IMFGContextService) OSGIUtil.getService(Activator.getDefault(), IMFGContextService.class.getName());
            ((IMFGContextService) osgService).openViews((TCComponent) topObject, localTCComponentStructureContext, toShow);
        }
    }
    

    private boolean checkWriteAccess(TCComponentBOPLine stationBOPLine, StringBuffer message) throws Exception {
        TCComponentItemRevision stationRevision = stationBOPLine.getItemRevision();
        TCComponentBOMViewRevision bomViewRevision = SDVBOPUtilities.getBOMViewRevision(stationRevision, "View");
        if (bomViewRevision == null)
            return false;

        TCAccessControlService aclService = tcsession.getTCAccessControlService();
        boolean isWriteBOMLine = aclService.checkPrivilege(bomViewRevision, TCAccessControlService.WRITE);
        if (!isWriteBOMLine) {
            if(CustomUtil.isReleased(stationRevision)) {
                message.append(stationRevision.toDisplayString());
                message.append((" : Released."));
                message.append(System.lineSeparator());
            } else {
                message.append(stationRevision.toDisplayString());
                message.append((" : You do not have write access"));
                message.append(System.lineSeparator());
            }
            return false;
        }
        
        return true;
    }
}
