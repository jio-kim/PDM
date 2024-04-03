package com.symc.plm.rac.prebom.ccn.commands;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.ccn.operation.CCNProcessOperation;
import com.symc.plm.rac.prebom.ccn.view.PreCCNInfoPanel;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItem;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

public class CCNProcessCommand extends AbstractAIFCommand {

    private InterfaceAIFComponent[] targetComponents;
    private TCSession session;
    private TCComponentChangeItemRevision changeRevision;
    private Registry registry;
    private String ospec;
    
    public CCNProcessCommand() {
        this.registry = Registry.getRegistry(this);
        this.targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
        this.session = SDVPreBOMUtilities.getTCSession();
        String message = validation();
        if(message.equals("")){
            org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
            box.setText(registry.getString("CCNProcessCommand.CCN.AskProceed"));
            box.setMessage(registry.getString("CCNProcessCommand.CCN.ProceedMessage"));
        
            int choice = box.open();
            if(choice == SWT.NO) return;

            if(changeRevision.getType().equals(TypeConstant.S7_PRECCNREVISIONTYPE)){
                CCNProcessOperation operation = new CCNProcessOperation(session, changeRevision);
                session.queueOperation(operation);
            }
        }else{
            com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), message, "Error", com.teamcenter.rac.util.MessageBox.ERROR);
            return;
        }
    }
    
    /**
     * [20160610] 결재 중복 방지를 위해서 결재상신시 결재버튼 비활성화 기능 적용
     * @param itemRevision
     * @param ccnViewComposite
     */
    public CCNProcessCommand(TCComponentItemRevision itemRevision, PreCCNInfoPanel ccnViewComposite) {
        this.registry = Registry.getRegistry(this);
        this.targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
        this.session = SDVPreBOMUtilities.getTCSession();
        this.changeRevision = (TCComponentChangeItemRevision) itemRevision;
        try {
            String message = validation();
//            message = "";
            if(message.equals("")){
                org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
                int choice = 0;
                if (!isOspecCheck(changeRevision)) {
                    box.setText(registry.getString("CCNProcessCommand.CCN.AskProceed"));
                    box.setMessage(registry.getString("CCNProcessCommand.CCN.ProceedMessage1"));
                    choice = box.open();
                    if(choice == SWT.NO) {
                        ospecInfoUpdate(changeRevision);
                        return;
                    }
                } else {
                    box.setText(registry.getString("CCNProcessCommand.CCN.AskProceed"));
                    box.setMessage(registry.getString("CCNProcessCommand.CCN.ProceedMessage2"));
                    choice = box.open();
                    if(choice == SWT.NO) {
                        return;
                    }
                }
                if(changeRevision.getType().equals(TypeConstant.S7_PRECCNREVISIONTYPE)){                	
                	//결재 상신시 승인 버튼 비활성화
                	setEnableAppprvalButton(ccnViewComposite, false);
            		/*	
            		 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
            		 *	데이터 생성 로직 변경으로 사용하지 않아 주석 처리 buildCCNEPL__을 사용함
            		 *  사용하지 않는 생성 로직을 구분 할려고 String type값을 받아 왔는데 사용 하지 않는 로직 주석 처리로 필요 없어서 제거함
            		 *  CCNProcessOperation operation = new CCNProcessOperation(session, changeRevision, ccnViewComposite, type); 
            		*/
                    CCNProcessOperation operation = new CCNProcessOperation(session, changeRevision, ccnViewComposite);
                    session.queueOperation(operation);
                }
            }else{
                com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), message, "Error", com.teamcenter.rac.util.MessageBox.ERROR);
                return;
            }
        } catch (Exception e) {
            com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("CCNProcessCommand.CCN.ProceedErrorMessage"), "Error", com.teamcenter.rac.util.MessageBox.ERROR);
            e.printStackTrace();
        }
    }

    private void ospecInfoUpdate(TCComponentChangeItemRevision itemRevision) throws TCException {
        itemRevision.setProperty(PropertyConstant.ATTR_NAME_OSPECNO, ospec);
    }

    private boolean isOspecCheck(TCComponentChangeItemRevision itemRevision) throws Exception {
        String ospecId = itemRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
        String projCode = itemRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
        String[] resultArray = getOspecId(projCode);
        ospec = resultArray[0];
        if (ospec.equals(ospecId)) {
            return true;
        }
        return false;
    }
    
    private String[] getOspecId(String projectId) throws Exception {
        TCComponent[] tcComponents = SDVPreBOMUtilities.queryComponent("__SYMC_S7_PreProductRevision", new String[]{"Project Code"}, new String[]{projectId});
        if (null != tcComponents && tcComponents.length > 0) {
            TCComponentItemRevision productRevision = null;
            for (TCComponent tcComponent : tcComponents) {
                if (tcComponent.getType().equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE)) {
                    productRevision = SYMTcUtil.getLatestReleasedRevision(((TCComponentItemRevision)tcComponent).getItem());
                    break;
                }
            }
            String[] resultArray = new String[2];
            resultArray[0] = productRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
            resultArray[1] = productRevision.getProperty(PropertyConstant.ATTR_NAME_GATENO);

            return resultArray;
        }
        return null;
    }

    private String validation(){
        StringBuffer message = new StringBuffer();
        
        /** targetComponents 갯수 확인 **/
        if(targetComponents == null || targetComponents.length == 0){
            message.append(registry.getString("CCNProcessCommand.CCN.ValidationMessage1"));
            return message.toString();
        }
        
        if(targetComponents.length > 1){
            message.append(registry.getString("CCNProcessCommand.CCN.ValidationMessage2"));
            return message.toString();
        }

        /** targetComponents Type 확인 **/
        if(changeRevision == null){
            if(targetComponents[0] instanceof TCComponentChangeItemRevision){
                changeRevision = (TCComponentChangeItemRevision) targetComponents[0];
            }else if(targetComponents[0] instanceof TCComponentChangeItem){
                TCComponentChangeItem changeItem = (TCComponentChangeItem) targetComponents[0];
                try {
                    changeRevision = (TCComponentChangeItemRevision) changeItem.getLatestItemRevision();
                } catch (TCException e) {
                    e.printStackTrace();
                }
            }else{
                message.append(registry.getString("CCNProcessCommand.CCN.ValidationMessage3"));
                return message.toString();
            }
        }
        
        try{
            /** 권한 체크 **/
            if(!changeRevision.okToModify()){
                message.append(registry.getString("CCNProcessCommand.CCN.ValidationMessage4"));
            }

            /** 결재 여부 **/
            // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체
//            String[] processProps = changeRevision.getProperties(new String[]{PropertyConstant.ATTR_NAME_DATERELEASED, PropertyConstant.ATTR_NAME_PROCESSSTAGELIST});
            String[] processProps = changeRevision.getProperties(new String[]{PropertyConstant.ATTR_NAME_DATERELEASED, PropertyConstant.ATTR_NAME_STARTEDWORKFLOWTASKS});
            if(!processProps[0].equals("") && !processProps[1].equals("Creator")){
                message.append(registry.getString("CCNProcessCommand.CCN.ValidationMessage5"));
            }
            
            /** infodba여부 확인 **/
            if(session.getUserName().equals("infodba")){
                message.append(registry.getString("CCNProcessCommand.CCN.ValidationMessage6"));
            }
            
            /** Revision check out 여부 확인 **/
            if(changeRevision.isCheckedOut()){
                message.append(registry.getString("CCNProcessCommand.CCN.ValidationMessage7"));
            }
            
            /** Item check out 여부 확인 **/
            if(changeRevision.getItem().isCheckedOut()){
                message.append(registry.getString("CCNProcessCommand.CCN.ValidationMessage8"));
            }
            
        }catch(Exception e){
            e.printStackTrace();
            message.append(registry.getString("CCNProcessCommand.CCN.ValidationMessage9"));
            message.append(e.toString());
        }

        return message.toString();
    }
    
    /**
     *  결재 상신 버튼 활성화 비활성화 기능
     * @param isEnable
     */
    private void setEnableAppprvalButton(PreCCNInfoPanel ccnViewComposite, final boolean isEnable)
    {
    	if(ccnViewComposite == null)
    		return;
    	
		final Button approvalButton = ccnViewComposite.getApprovalButton();
//		final Button approvalButtonOld = ccnViewComposite.getApprovalButtonOld();
		
//    	if(approvalButton== null || approvalButtonOld== null)
    	if(approvalButton== null)
    		return;
    	Shell shell = AIFUtility.getActiveDesktop().getShell();
    	shell.getDisplay().syncExec(new Runnable() {
            public void run() {
//                	approvalButton.setEnabled(isEnable);
            	approvalButton.setVisible(isEnable);
//            	approvalButtonOld.setVisible(isEnable);
            }
        });
    	
    }
}