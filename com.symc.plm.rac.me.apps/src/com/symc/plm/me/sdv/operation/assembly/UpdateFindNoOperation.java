/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.workflow.commands.assign.AssignOperation;

/**
 * Class Name : UpdateFindNoOperation
 * Class Description : 공정편성번호 Update
 * 
 * @date 2013. 10. 29.
 * 
 */
public class UpdateFindNoOperation extends AbstractTCSDVOperation {

    private TCSession tcSession = null;
    private boolean isValidOK = true;
    private TCComponentBOMLine[] selectedBOMLines = null;
    private TCAccessControlService aclService = null;
    private Registry registry = null;
    private static String WORKFLOW_TEMPLATE_GRANT_PRIVILEGE = "SYMC_GRANT_PRIVILEGE_PROCESS";

    /*
     * (non-Javadoc)
     * TODO: Validation 기능 을 구현
     * 
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
     */
    @Override
    public void startOperation(String commandId) {

        registry = Registry.getRegistry(this);
        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        // 현재 BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

        try {
            // (체크)1. BOP Load 유무
            if (bomWindow == null) {
                com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), registry.getString("Warning.NAME"), com.teamcenter.rac.util.MessageBox.WARNING);
                isValidOK = false;
                return;
            }

            selectedBOMLines = mfgApp.getSelectedBOMLines();

            for (TCComponentBOMLine selectedBOMLine : selectedBOMLines) {
                String selectedItemType = selectedBOMLine.getItem().getType();
                boolean isEnableType = selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM);
                // (체크)3. Process BOP 선택 유무
                if (!isEnableType) {
                    com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("selectBOP.MSG"), registry.getString("Warning.NAME"), com.teamcenter.rac.util.MessageBox.WARNING);
                    isValidOK = false;
                    return;
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

        // 수정할 수 없는 Line BOMLine 리스트(Key:ItemId,OrderNo List, Value:TCComponentBOMViewRevision)
        Hashtable<List<String>, TCComponentBOMViewRevision> releasedLineBOMViewRevHash = new Hashtable<List<String>, TCComponentBOMViewRevision>();
        // 대상 Operation 리스트
        ArrayList<TCComponentBOMLine> targetOperationList = new ArrayList<TCComponentBOMLine>();
        // Line의 수정 가능상태를 중복 체크 유무확인
        ArrayList<List<String>> isLineDupCheckedList = new ArrayList<List<String>>();
        TCComponentProcess grantProcess = null;

        if (!isValidOK)
            return;

        try {
            tcSession = (TCSession) getSession();
            aclService = tcSession.getTCAccessControlService();

            /**
             * 1. 수정할 대상 Operation을 추가함
             */
            for (TCComponentBOMLine selectedBomline : selectedBOMLines) {
                String selectedItemType = selectedBomline.getItem().getType();
                // 공법일 경우
                if (selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM)) {

                    TCComponentBOMLine parentBOMLine = selectedBomline.parent();
                    if (parentBOMLine == null)
                        continue;
                    String parentItemType = parentBOMLine.getItem().getType();
                    if (!parentItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
                        continue;
                    /** 수정할 수 없는 Line BOM View 추가 **/
                    addLineBomViewRevision(selectedBomline.parent(), releasedLineBOMViewRevHash, isLineDupCheckedList);
                    /** 공법일 경우 추가 **/
                    targetOperationList.add(selectedBomline);

                } else
                    addTargetBOMLine(selectedBomline, releasedLineBOMViewRevHash, targetOperationList, isLineDupCheckedList);
            }

            /**
             * 2. 수정권한이 없는 Line BOM View Revision에 수정권한을 줌
             */
            if (releasedLineBOMViewRevHash.size() != 0)
                grantProcess = grantLineBomViewRevision(releasedLineBOMViewRevHash);

            /**
             * 3. 공법의 FindNo 속성을 Update함
             */
            updateFindNo(targetOperationList);

            selectedBOMLines[0].window().save();

            for (TCComponentBOMViewRevision lineBOMViewRevision : releasedLineBOMViewRevHash.values()) {
                lineBOMViewRevision.refresh();
            }

            if (releasedLineBOMViewRevHash.size() != 0)
                revokeProcess(grantProcess);

        } catch (Exception ex) {
            if (grantProcess != null)
                grantProcess.delete();
            isValidOK = false;
        }
    }

    /**
     * 
     * 수정 대상 공법을 추출함
     * 
     * @method addTargetBOMLine
     * @date 2013. 11. 1.
     * @param bomline
     *            BOMLine
     * @param releasedLineBOMViewRevHash
     *            수정 불가능한 Line BOMView Revision Hash
     * @param targetOperationList
     *            수정 대상 공법
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addTargetBOMLine(TCComponentBOMLine bomline, Hashtable<List<String>, TCComponentBOMViewRevision> releasedLineBOMViewRevHash, ArrayList<TCComponentBOMLine> targetOperationList, ArrayList<List<String>> isLineDupCheckedList) throws Exception {
        try {

            // 상위BOM 타입
            String parentItemType = bomline.getItem().getType();

            AIFComponentContext[] aifComps = bomline.getChildren();
            tcSession.setStatus(bomline.toString() + " " + registry.getString("findingChildOp.MSG"));

            for (AIFComponentContext aifComp : aifComps) {
                TCComponentBOMLine childBomline = (TCComponentBOMLine) aifComp.getComponent();
                // 하위BOM 타입
                String childItemType = childBomline.getItem().getType();
                // 상위 BOP가 Line일 경우
                if (parentItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) && childItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM)) {
                    /** 수정할 수 없는 Line BOM View 추가 **/
                    addLineBomViewRevision(bomline, releasedLineBOMViewRevHash, isLineDupCheckedList);
                    /** 공법일 경우 추가 **/
                    targetOperationList.add(childBomline);
                } else
                    addTargetBOMLine(childBomline, releasedLineBOMViewRevHash, targetOperationList, isLineDupCheckedList);
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * 
     * 수정할 수 없는 라인 BOMView Revision을 추가
     * 
     * @method addLineBomViewRevision
     * @date 2013. 11. 1.
     * @param bomline
     * @param releasedLineBOMViewRevHash
     *            수정할 수 없는 Line BOMView Revision 리스트
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addLineBomViewRevision(TCComponentBOMLine bomline, Hashtable<List<String>, TCComponentBOMViewRevision> releasedLineBOMViewRevHash, ArrayList<List<String>> isLineDupCheckedList) throws Exception {
        String itemType = bomline.getItem().getType();
        // 라인일 경우 적용
        if (!itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
            return;

        String itemId = bomline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String orderNo = bomline.getProperty(SDVPropertyConstant.BL_OCC_ORDER_NO);
        List<String> key = Arrays.asList(new String[] { itemId, orderNo });

        // 라인을 수정여부를 체크하였으면 Pass
        if (isLineDupCheckedList.contains(key))
            return;
        isLineDupCheckedList.add(key);

        // 이미 추가된 라인이면 Pass
        if (releasedLineBOMViewRevHash.containsKey(key))
            return;

        TCComponentBOMViewRevision lineViewRevision = SDVBOPUtilities.getBOMViewRevision(bomline.getItemRevision(), "view");
        if (lineViewRevision == null)
            return;
        boolean isWrite = aclService.checkPrivilege(lineViewRevision, TCAccessControlService.WRITE);
        if (isWrite)
            return;
        /** Released된 Line 을 추가 **/
        releasedLineBOMViewRevHash.put(key, lineViewRevision);
    }

    /**
     * 
     * 라인의 BOM View Revision 을 수정할 수 있도록 권한을 준다.
     * 
     * @method grantLineBomViewRevision
     * @date 2013. 11. 1.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private TCComponentProcess grantLineBomViewRevision(Hashtable<List<String>, TCComponentBOMViewRevision> releasedLineBOMViewRevHash) throws Exception {
        TCComponentProcess grantProcess = null;

        TCComponentTaskTemplate taskTemplate = getTaskTemplate(WORKFLOW_TEMPLATE_GRANT_PRIVILEGE);
        TCComponentProcessType newProcessType = (TCComponentProcessType) tcSession.getTypeComponent("Job");
        newProcessType.refresh();

        int cnt = 0;
        TCComponent[] targets = new TCComponent[releasedLineBOMViewRevHash.size()];
        int[] targetTypes = new int[releasedLineBOMViewRevHash.size()];
        for (TCComponentBOMViewRevision lineBOMViewRevision : releasedLineBOMViewRevHash.values()) {
            targets[cnt] = lineBOMViewRevision;
            targetTypes[cnt] = TCAttachmentType.TARGET;
            cnt++;
        }

        grantProcess = (TCComponentProcess) newProcessType.create(WORKFLOW_TEMPLATE_GRANT_PRIVILEGE, "", taskTemplate, targets, targetTypes);

        return grantProcess;
    }

    /**
     * 공법의 Find No를 속성을 Update함
     * 
     * @method updateFindNo
     * @date 2013. 11. 1.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void updateFindNo(ArrayList<TCComponentBOMLine> bomlineList) throws Exception {

        try {
            for (TCComponentBOMLine operation : bomlineList) {

                // 공정번호
                String stationNo = operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO).replace("-", "");// 공정번호
                String workerCode = operation.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE).replace("-", "");// 작업자코드
                String seq = operation.getItem().getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ);// 작업자 순번
                boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty() || seq.isEmpty(); // 하나라도 값이 없으면 반영안함
                String findNo = stationNo.concat("|").concat(workerCode).concat("|").concat(seq);
                if (findNo.length() > 15 || isExistEmptyValue)
                    return;
                operation.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
            }

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 결재 Job Object를 삭제함
     * 
     * @method revokeProcess
     * @date 2013. 11. 1.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void revokeProcess(final TCComponentProcess grantProcess) throws Exception {

        if (grantProcess == null)
            return;
        TCComponentTask doTask = grantProcess.getRootTask().getSubtask("GrantTask");
        AssignOperation operation = new AssignOperation(tcSession, AIFDesktop.getActiveDesktop(), new TCComponentTask[] { doTask }, tcSession.getUser());
        operation.addOperationListener(new InterfaceAIFOperationListener() {
            public void startOperation(String arg0) {
            }

            public void endOperation() {
                try {
                    grantProcess.refresh();
                    grantProcess.delete();
                } catch (Exception e) {
//                    try {
//                        attachErrorJobToFolder(grantProcess);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
                }
            }
        });
        tcSession.queueOperation(operation);
    }

    /**
     * 
     * Task Name 으로 결재 Template 을 가져옴
     * 
     * @method getTaskTemplate
     * @date 2013. 10. 31.
     * @param
     * @return TCComponentTaskTemplate
     * @exception
     * @throws
     * @see
     */
    private TCComponentTaskTemplate getTaskTemplate(String templateName) throws Exception {
        TCComponentTaskTemplate taskTemplate = null;
        TCComponentTaskTemplate taskTemplateList[] = null;

        try {
            // EPM Task Template 타입을 가져옴
            TCComponentTaskTemplateType taskTemplateType = (TCComponentTaskTemplateType) tcSession.getTypeComponent("EPMTaskTemplate");

            if (taskTemplateType != null)
                taskTemplateList = taskTemplateType.extentTemplates(0);
            // 결재 템플릿을 가져옴
            for (TCComponentTaskTemplate taskTemplateObj : taskTemplateList) {
                TCComponentTaskTemplate taskRootTemplate = taskTemplateObj.getRoot();
                if (taskRootTemplate.getName().equalsIgnoreCase(templateName)) {
                    taskTemplate = taskRootTemplate;
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return taskTemplate;
    }

    /**
     * 결재 시 오류가 발생 하였을 경우 BOPADM Error Folder
     * TODO: BOPADM 폴더위치변경시 수정 필요
     * 
     * @method attachErrorJobToFolder
     * @date 2013. 11. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unused")
    private void attachErrorJobToFolder(TCComponentProcess grantProcess) throws Exception {
        TCComponentUserType userType = (TCComponentUserType) tcSession.getTypeComponent("User");
        TCComponentUser bopAdmUser = userType.find("BOPADM");
        TCComponentFolder bopAdmHome = bopAdmUser.getHomeFolder();
        TCComponentFolder errorFolder = getChildFolder(bopAdmHome, "ERROR UPDATE FINDNO");
        errorFolder.add("contents", grantProcess);
    }

    /**
     * 
     * 하위 Component의 Folder 를 찾음
     * 
     * @method getChildFolder
     * @date 2013. 11. 4.
     * @param
     * @return TCComponentFolder
     * @exception
     * @throws
     * @see
     */
    private TCComponentFolder getChildFolder(TCComponent parentFolder, String findFolderName) throws Exception {

        AIFComponentContext[] aifContexts = parentFolder.getChildren("contents");

        for (AIFComponentContext aifContext : aifContexts) {
            TCComponent comp = (TCComponent) aifContext.getComponent();
            if (!(comp instanceof TCComponentFolder))
                continue;
            if (comp.toString().equals(findFolderName))
                return (TCComponentFolder) comp;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#afterExecuteSDVOperation()
     */
    @Override
    public void endOperation() {
        if (isValidOK)
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("FindNoComplete.MSG"), "OK", MessageBox.INFORMATION);

    }
}
