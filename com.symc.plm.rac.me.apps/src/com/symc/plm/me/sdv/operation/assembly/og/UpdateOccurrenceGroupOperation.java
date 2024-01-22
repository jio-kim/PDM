/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly.og;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.cme.framework.views.primary.og.IOccurrenceGroupUIManager;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Registry;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;

/**
 * Class Name : SyncOptionsetVsBOMOperation
 * Class Description :
 * 
 * @date 2013. 10. 25.
 * 
 */
public class UpdateOccurrenceGroupOperation extends AbstractTCSDVOperation {

    // Process Top BOPLINE
    private TCComponentBOMLine topProcessBomline = null;
    // Occurrence Group Top BOPLINE
    private TCComponentAppGroupBOPLine appGroupBopline = null;
    // Occurrence Group 이름
    private String systemName = null;
    // 추가될 Operation 리스트(Key:ItemId,OrderNo List, Value:TCComponentBOMLine)
    private Hashtable<List<String>, TCComponentBOMLine> addOperationHash = null;
    private TCSession tcSession = null;
    private boolean isValidOK = true;
    private Registry registry = null;

    /*
     * (non-Javadoc)
     * TODO: Validation 클래스에서 이동될 코드
     * 
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
     */
    @Override
    public void startOperation(String commandId) {
        try {
            registry = Registry.getRegistry(this);
            // MPPAppication
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            // 현재 BOM WINDOW
            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

            // (체크)1. BOP Load 유무
            if (bomWindow == null) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), registry.getString("Warning.NAME"), MessageBox.WARNING);
                isValidOK = false;
                return;
            }

            // (체크)2. Occurrence Group 선택 유무
//            if (MFGStructureTypeUtil.getStructureType(bomWindow.getTopBOMLine()) != MFGStructureType.OccurrenceGroup) {
            if(!(bomWindow.getTopBOMLine() instanceof TCComponentAppGroupBOPLine)) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("selectOpGroup.MSG"), registry.getString("Warning.NAME"), MessageBox.WARNING);
                isValidOK = false;
                return;
            }

            appGroupBopline = (TCComponentAppGroupBOPLine) bomWindow.getTopBOMLine();

            // Process Top BOMLine을 지정함
            IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
            for (IViewReference viewRerence : arrayOfIViewReference) {
                IViewPart localIViewPart = viewRerence.getView(false);
                if (localIViewPart == null)
                    continue;
                CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
                if (cmeBOMTreeTable == null)
                    continue;
                TCComponentBOMLine rootBomline = cmeBOMTreeTable.getBOMRoot();
//                MFGStructureType mfgType = MFGStructureTypeUtil.getStructureType(rootBomline);
//                // Occurrence 그룹 일 경우
//                if (mfgType != MFGStructureType.OccurrenceGroup)
//                    continue;
                
                if(!(rootBomline instanceof TCComponentAppGroupBOPLine)) {
                	continue;
                }
                
                // Base BOM Window를 가져옴
                IOccurrenceGroupUIManager ioccurrencegroupuimanager = (IOccurrenceGroupUIManager) AdapterUtil.getAdapter(localIViewPart, IOccurrenceGroupUIManager.class);
                if (ioccurrencegroupuimanager != null) {
                    TCComponentBOMWindow baseBomWindow = (TCComponentBOMWindow) ioccurrencegroupuimanager.getAdapterForBaseView(TCComponentBOMWindow.class);
                    topProcessBomline = baseBomWindow.getTopBOMLine();
                    break;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        isValidOK = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        if (!isValidOK)
            return;
        tcSession = (TCSession) getSession();
        addOperationHash = new Hashtable<List<String>, TCComponentBOMLine>();

        String appGroupName = appGroupBopline.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
        this.systemName = appGroupName.equals("미할당") ? "" : appGroupName;

        /**
         * 동일한 System 에 해당하는 Operation을 찾음
         */
        AIFComponentContext[] aifComps = topProcessBomline.getChildren();
        for (AIFComponentContext aifComp : aifComps) {
            TCComponentBOMLine bomline = (TCComponentBOMLine) aifComp.getComponent();
            tcSession.setStatus(bomline.toString() + " " + registry.getString("findingChildOp.MSG"));
            getOperationLikeSystem(bomline);
        }

        /**
         * 선택된 System 공법그룹에 공법을 추가함
         */
        addOperationToAppGroup();

        tcSession.setReadyStatus();

    }

    /**
     * 동일한 System의 Operation을 가져온다.
     * 
     * @method getOperationLikeSystem
     * @date 2013. 10. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void getOperationLikeSystem(TCComponentBOMLine bomline) throws Exception {
        try {
            AIFComponentContext[] aifComps = bomline.getChildren();
            for (AIFComponentContext aifComp : aifComps) {
                TCComponentBOMLine operation = (TCComponentBOMLine) aifComp.getComponent();
                String itemId = operation.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                String orderNo = operation.getProperty(SDVPropertyConstant.BL_OCC_ORDER_NO);
                String system = operation.getItemRevision().getProperty(SDVPropertyConstant.BOPOP_REV_M7_ASSY_SYSTEM).replace(".", "");

                List<String> key = Arrays.asList(new String[] { itemId, orderNo });
                if (systemName.equals(system)) {
                    if (!addOperationHash.containsKey(key))
                        addOperationHash.put(key, operation);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * 선택된 시스템의 Occurrence 그룹에 동일한 시스템인 Operation으로 Update를 한다.
     * 
     * @method AddOperationToAppGroup
     * @date 2013. 10. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addOperationToAppGroup() throws Exception {

        try {
            tcSession.setStatus(registry.getString("AddingToGroup.MSG"));
            AIFComponentContext[] aifComps = appGroupBopline.getChildren();

            for (AIFComponentContext aifComp : aifComps) {
                TCComponentBOMLine operation = (TCComponentBOMLine) aifComp.getComponent();
                String itemId = operation.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                String orderNo = operation.getProperty(SDVPropertyConstant.BL_OCC_ORDER_NO);
                List<String> key = Arrays.asList(new String[] { itemId, orderNo });
                // 기존에 이미 있으면 추가될 리스트에서 제거
                if (addOperationHash.containsKey(key))
                    addOperationHash.remove(key);
                else {
                    // 추가 리스트에 없으면 제거함
                    operation.cut();
                }

            }
            // 공법을 추가함
            for (TCComponentBOMLine addOperation : addOperationHash.values()) {
                appGroupBopline.addBOMLine(appGroupBopline, addOperation, "");
            }

        } catch (Exception ex) {
            throw ex;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#afterExecuteSDVOperation()
     */
    @Override
    public void endOperation() {
        if (isValidOK)
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), systemName + " " + registry.getString("Complete.MSG"), "Information", MessageBox.INFORMATION);

    }

}
