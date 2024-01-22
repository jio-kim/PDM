/**
 * 
 */
package com.symc.plm.me.sdv.operation.common;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SyncEndItemFindNoOperation
 * Class Description : M Prdoduct BOMLine중 E/Item의 Find No.를 BOP Line의 M7_ENDITEM_SEQ_NO 컬럼에 Sync 하는 Operation
 * 
 * [SR141120-033][20141120] shcho, M-Product의 Find No. 와 BOP의 M7_ENDITEM_SEQ_NO 컬럼 Sync 하는 class 신규 생성
 * [SR번호][20141210] shcho, EndItem Sequence No. BOP에 Sync시 차체와 도장은 대상 EndItem 목록을 찾지 못하는 오류 수정
 * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 * 
 */
public class SyncEndItemFindNoOperation extends AbstractTCSDVOperation {

    private Shell shell;
    private Registry registry = null;
    private TCSession tcSession = null;

    private boolean isValidOK = false;

    @Override
    public void startOperation(String commandId) {
        shell = AIFUtility.getCurrentApplication().getDesktop().getShell();
        registry = Registry.getRegistry("com.symc.plm.me.sdv.operation.common.common");

        tcSession = (TCSession) getSession();
        tcSession.setStatus("Syncronizing E/Item Find No....");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        try {

            // MPPAppication
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            AIFDesktop.getActiveDesktop().getCurrentApplication().getApplicationView();

            // 현재 BOM Window
            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();
            if (bomWindow == null) {
                throw new Exception("Please, Load BOP CC.");
            }

            // TopBOMLine
            TCComponentBOMLine topBomLine = bomWindow.getTopBOMLine();
            if (topBomLine == null) {
                throw new Exception("Please, Load BOP CC.");
            }

            // M-Product BOM Window
            // [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
            TCComponentBOMWindow mProductBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(topBomLine.getItemRevision());
            if (mProductBOMWindow == null) {
                throw new Exception("Can not find M-Product View.");
            }

            // 선택한 BOM Line
            InterfaceAIFComponent[] selectedComponents = mfgApp.getTargetComponents();
            if (selectedComponents == null || selectedComponents.length == 0) {
                throw new Exception("Please, Select BOP BOMLine.");
            }

            HashMap<TCComponentBOMLine, String> bomLineMap = new HashMap<TCComponentBOMLine, String>();
            for (InterfaceAIFComponent selectedComponent : selectedComponents) {
                TCComponentBOMLine selectedBOMLine = (TCComponentBOMLine) selectedComponent;
                String selectedCompRevType = selectedBOMLine.getItemRevision().getType();
                bomLineMap.put(selectedBOMLine, selectedCompRevType);
            }

            // EndItemBOMLine List
            ArrayList<TCComponentBOMLine> endItemBOMLineList = new ArrayList<TCComponentBOMLine>();
            endItemBOMLineList = getEndItemBOMLineList(bomLineMap);
            if (endItemBOMLineList == null || endItemBOMLineList.size() == 0) {
                throw new Exception("There are no assigned E/Item.");
            }

            // Sync EndItem Find No.
            syncFindNo(mProductBOMWindow, endItemBOMLineList);

        } catch (Exception ex) {
            setAbortRequested(true);
            isValidOK = false;

            MessageBox.post(shell, ex.getMessage(), registry.getString("Warning.NAME"), MessageBox.WARNING);
            throw ex;
        }

    }

    /**
     * BOP BOMLine 하위에 할당된 E/Item 목록을 담는 함수
     * 
     * [SR번호][20141210] shcho, EndItem Sequence No. BOP에 Sync시 차체와 도장은 대상 EndItem 목록을 찾지 못하는 오류 수정
     * 
     * @param TCComponentBOMLine
     *            bomLine
     * @param String
     *            itemRevType
     * @throws Exception
     */
    private ArrayList<TCComponentBOMLine> getEndItemBOMLineList(HashMap<TCComponentBOMLine, String> bomLineMap) throws Exception {
        ArrayList<TCComponentBOMLine> endItemBOMLineList = new ArrayList<TCComponentBOMLine>();

        for (TCComponentBOMLine bomLine : bomLineMap.keySet()) {
            String itemRevType = bomLineMap.get(bomLine);

            if (itemRevType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV)) {
                throw new Exception("Please, Select Line or Operation.");
            } else if (itemRevType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV) || itemRevType.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV)) {
                HashMap<TCComponentBOMLine, String> childBOMLineMap = new HashMap<TCComponentBOMLine, String>();
                AIFComponentContext[] aifComponentContexts = bomLine.getChildren();
                
                for (AIFComponentContext aifComponentContext : aifComponentContexts) {
                    TCComponentBOMLine childBOMLine = (TCComponentBOMLine) aifComponentContext.getComponent();
                    childBOMLineMap.put(childBOMLine, childBOMLine.getItemRevision().getType());
                }
                
                endItemBOMLineList.addAll(getEndItemBOMLineList(childBOMLineMap));
            } else if (itemRevType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV) || itemRevType.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV) || itemRevType.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV)) {
                AIFComponentContext[] aifComponentContexts = bomLine.getChildren();
                for (AIFComponentContext aifComponentContext : aifComponentContexts) {
                    TCComponentBOMLine childBOMLine = (TCComponentBOMLine) aifComponentContext.getComponent();
                    if (childBOMLine.getType().equals("Mfg0BvrPart")) {
                        endItemBOMLineList.add(childBOMLine);
                    }
                }
            } else if (itemRevType.equals(SDVTypeConstant.EBOM_VEH_PART_REV) || itemRevType.equals(SDVTypeConstant.EBOM_STD_PART_REV)) {
                endItemBOMLineList.add(bomLine);
            }
        }
        return endItemBOMLineList;
    }

    /**
     * M-Product BOMLine의 Find No. 값을 BOP BOMLine의 M7_ENDITEM_SEQ_NO 속성에 Update
     * 
     * @param mProductBOMWindow
     * @param endItemBOMLineArrayList
     * @param endItemBOMLine
     * @throws Exception
     * @throws TCException
     */
    public void syncFindNo(TCComponentBOMWindow mProductBOMWindow, ArrayList<TCComponentBOMLine> endItemBOMLineArrayList) throws Exception, TCException {
        for (TCComponentBOMLine endItemBOMLine : endItemBOMLineArrayList) {
            TCComponentBOMLine assignSrcBOMLine = SDVBOPUtilities.getAssignSrcBomLine(mProductBOMWindow, endItemBOMLine);
            if (assignSrcBOMLine != null) {
                String seqNo = assignSrcBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
                endItemBOMLine.setProperty(SDVPropertyConstant.BL_NOTE_ENDITEM_SEQ_NO, seqNo);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
        if (isValidOK)
            return;

        MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "'E/Item Find No.' was successfully synced. ", registry.getString("Inform.NAME"), MessageBox.INFORMATION);
    }

    /**
     * Exception의 getStackTrace 내용을 String으로 변환 하는 함수
     * 
     * @param Throwable
     *            e
     * @return String
     */
    public String printStackTraceToString(Throwable e) {
        StringBuffer sb = new StringBuffer();

        try {
            sb.append(e.toString());
            sb.append("\n");
            StackTraceElement element[] = e.getStackTrace();

            for (int idx = 0; idx < element.length; idx++) {
                sb.append("\tat ");
                sb.append(element[idx].toString());
                sb.append("\n");
            }
        } catch (Exception ex) {
            return e.toString();
        }

        return sb.toString();
    }

}
