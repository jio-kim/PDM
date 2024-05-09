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
 *  [SR150122-027][20150309] shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
 *  [SR150529-025][20150828] shcho, ������Ʈ ����/������Ʈ ��� �߰� ���� - Shop, Line ������ �ϰ������� ������Ʈ ����(������Ʈ) �� �� �ֵ��� ��� ����
 * 
 */
public class OccGroupCreateUpdateActionOperation extends AbstractSDVActionOperation {

    private TCComponentBOMLine mProductBomLine;
    private final String serviceClassName = "com.kgm.service.BopPertService";
    private TCSession tcsession;
    private String altOccGroupPreFix = "";
    private List<String> bomLineIDList;
    
    // ������ �޽����� ��� ����
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

        // Preference �� ����� OccGroup �� ����� PreFix �� �����´�
        tcsession = CustomUtil.getTCSession();
        altOccGroupPreFix = tcsession.getPreferenceService().getStringValue("OccurrenceGroupPreFix");

        IDataSet dataset = getDataSet();
        if (dataset != null)
        {
            @SuppressWarnings("unchecked")
            ArrayList<TCComponentBOPLine> stationBOPLineList = (ArrayList<TCComponentBOPLine>) dataset.getListValue("occGroupCreateUpdateView", "targetStation");
            TCComponentBOPLine targetShopBOPLine = (TCComponentBOPLine) dataset.getData("targetShop");
            
            // ���� �����ִ� MPP BOMWindow �߿��� BOPShop �� �����Ǵ� MProduct BOMWindow�� �����´�
            mProductBomLine = getMBOMLine(targetShopBOPLine);

            if (mProductBomLine == null) {
                throw new Exception("Cannot fine M-Product BOMLine.");
            }
            
            // TODO : ���ó�� �Ͽ� �۾��߿� �ٸ� ����� ���� �۾��� �� �� ������ �ϴ� ���� �߰� �ʿ�.
            //mProductBomLine.lock();
            
            // 1. M-Product BomWindow ���� OccurrenceGroupWindow�� �����Ѵ�
            ocgWindowsOpen();

            for (TCComponentBOPLine stationBOPLine : stationBOPLineList) {
                tcsession.setStatus("Updating... : ".concat(stationBOPLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID)));

                // 2. Station ID�� ������ OccGroup �� ������ ��������, ������ �����Ѵ�.
                occGroup = getOccWindow(stationBOPLine);

                if(occGroup == null) {
                    continue;
                }
                
                // 3. Station OccGroup�� BopLine �� �����´�
                occGroupStationBopLine = getOccGroupStationBopLine(occGroup);

                // 4. Station OccGroup BopLine ������ EndItemBOMLine�� ��� �����Ѵ�.
                occGroupStationBopLine.remove("bl_occgrp_visible_lines", SDVBOPUtilities.getChildrenBOMLine(occGroupStationBopLine));

                // 5. Station OccGroup BopLine ������ EndItemBOMLine �� �߰��Ѵ�.
                setPnlItem(occGroupStationBopLine, stationBOPLine);

                // 6. Station OccGroup BopLine �� Station BOPLine(����) ������ �Ҵ��Ѵ�.
                pasteStationOccGroup(stationBOPLine, occGroup);

            }
            
            //mProductBomLine.unlock();
        }
    }

    /**
     *  1. OccWindow �� ������ �Ǿ� �ִ��� üũ�Ѵ�
     *  2. ������ �����Ѵ�
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
        
        // stationBOPLine �� Release �Ǵ� ���� ������ ���� ��쿡�� �ű� ������ �Ѵ�.
        if(checkWriteAccess(stationBopLine, resultMessage)) {
            appGroupTop = createOccGroup(mProductBomLine, stationID);            
        }
        

        return appGroupTop;
    }

    /**
     *       MPP�� Ȱ��ȭ �Ǿ��ִ� view ���� BOMView �� �����Ͽ� BOMLine ������ ��ư���
     *       MPP �� �������� TAB View �߿� cc ������ TYPE ������ ���Ͽ� ������ BOP �ǰ�
     *       �����Ǿ� �ִ� BOMVIEW �����͸� �����´�
     *       
     *  [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
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
     * BOP �ֻ����� SHOP�� ����Ǿ� �ִ� BOMView ����(M7_Product) �� Ȯ���Ͽ� M_Product ID ������ ��ȯ �Ѵ�
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
     *  [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link������ ���̻� ������.
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
     * occWindow �� �����Ѵ�
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

        // ������ �ʿ��� �Է°�
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
     * ������ ������ OccGroup �� �ִٸ� ���� �Ѵ�
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

        // BOP ���� ������ ���� OccGroup �� �߶󳽴�
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

        // OccGroup ���� ������
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
     * OccGroup �� �����Ѵ�
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

        // ������ �ʿ��� �Է°�
        CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
        createInput.add(SDVPropertyConstant.ITEM_OBJECT_NAME, altOccGroupPreFix + stationID);
        createInput.add(SDVPropertyConstant.ITEM_OBJECT_TYPE, "AppearanceGroup");
        List<ICreateInstanceInput> inPutList = new ArrayList<ICreateInstanceInput>();
        inPutList.add(createInput);

        List<TCComponent> comps = mfgContextService.createView(createDefinition, inPutList, mProductBomLine);
        comps.get(0).save();

        // ������ OccGroup �� ��Ʈ OccGroup �� ���δ�
        occGroupTopLine.addAppGroup((TCComponentAppearanceGroup) comps.get(0));

        mProductBomLine.getItem().refresh();
        occGroupTopLine.refresh();

        return (TCComponentAppearanceGroup) comps.get(0);
    }
    */


    /**
     * �����ߴ� StationOccGroupBopLine �� ��ȯ�Ѵ�
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
     * ������ OccGroup �� �Ҵ��Ѵ�
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
        
        // stationBOPLine �� Release �Ǵ� ���� ������ ���� ��쿡�� Add �Ѵ�.
        if(checkWriteAccess(targetStationBOPLine, resultMessage)) {
            TCComponentBOMLine occStationBopLine = targetStationBOPLine.add(stationOccGroup, false, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_GROUP);
            occStationBopLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, "0");
        }

        /*
        // MproductItem �� �پ� �ִ� Station occGroup �� Cut �Ѵ�
        TCComponent[] stationList = {stationOccGroup};
        mProductBomLine.getItem().cutOperation("IMAN_MEView", stationList);
        mProductBomLine.getItem().refresh();
        //ocgWindowsOpen();
        */
    }

    /**
     *  �ǳ��� OccGroup �� �Ҵ��Ѵ�
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
        // ���� ������ �Ĵ� ���� ����� �����´�
        List<String> decessorsList = getDecessorsList(shopBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), targetStation.getProperty(SDVPropertyConstant.BL_ITEM_ID));

        String shopAbsOccID = shopBopLine.getProperty(SDVPropertyConstant.BL_ITEM_PUID);
        // �Ĵ� ���� ���� PART �����ۿ� bl_abs_occ_id �� �����Ѵ�
        List<String> decessorsPartAbsOccIDList = getDecessorsPartAbsOccIDList(decessorsList, shopAbsOccID);

        // Occurrence Group �� ��� BOMLine ����Ʈ (�ߺ� ADD �� �������� �ʿ�)
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
                            // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation�� bl_abs_occ_id ���� �����Ѵ�. 
                        	BOPLineUtility.updateLineToOperationAbsOccId(occGroupChildBOMLine);
                        }
                        bomLineIDList.add(((TCComponentBOMLine)mBomLine[0]).getProperty(SDVPropertyConstant.BL_ITEM_ID));
                    }
                }
            }
        }
    }

    /**
     * �ߺ� add �� ���ϱ����� ������ ���� BOMLine �� �����Ѵ� (���ϱ� ���� �ʿ�)
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
     *  �Ĵ� ���� E/I �� bl_abs_occ_id �� �����´� (DB)
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
     * �Ĵ� ���� ����Ʈ�� �����´� (DB)
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
     * M-Product BomWindow ���� OccurrenceGroupWindow �� ���� ������
     * OccurrenceGroup BOMLine ������ �����ü� ���� ������ �����Ѵ�
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
