package com.symc.plm.me.sdv.command.meco;

import org.eclipse.swt.SWT;

import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.me.sdv.operation.meco.MECOProcessOperation;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentChangeItem;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
/**
 * ECI/ECO 결재 상신[Workflow 생성]
 * @author DJKIM
 *
 */
public class MECOProcessCommand extends AbstractAIFCommand {

	private InterfaceAIFComponent[] targetComponents;
	private TCSession session;
	private TCComponentChangeItemRevision changeRevision;
	
	public MECOProcessCommand() {
		this.targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		this.session = CustomUtil.getTCSession();
		String message = validation();
		if(message.equals("")){
			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			box.setText("Ask Proceed");
			box.setMessage("Do you want to proceed?");
		
			int choice = box.open();
			if(choice == SWT.NO) return;
			

		    if(changeRevision.getType().equals("M7_MECORevision")){
		        MECOProcessOperation operation = new MECOProcessOperation(session, changeRevision);
		        session.queueOperation(operation);
			}
		}else{
			com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), message, "Error", com.teamcenter.rac.util.MessageBox.ERROR);
			return;
		}
	}
	
	public MECOProcessCommand(TCComponentItemRevision itemRevision) {
		this.targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		this.session = CustomUtil.getTCSession();
		this.changeRevision = (TCComponentChangeItemRevision) itemRevision;
		String message = validation();
		if(message.equals("")){
			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			box.setText("Ask Proceed");
			box.setMessage("Do you want to proceed?");
		
			int choice = box.open();
			if(choice == SWT.NO) return;
		    
		    if(changeRevision.getType().equals("M7_MECORevision")){
		        MECOProcessOperation operation = new MECOProcessOperation(session, changeRevision);
		        session.queueOperation(operation);
			}
		}else{
			com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), message, "Error", com.teamcenter.rac.util.MessageBox.ERROR);
			return;
		}
	}

	private String validation(){
		StringBuffer message = new StringBuffer();
		
		/** targetComponents 갯수 확인 **/
		if(targetComponents == null || targetComponents.length == 0){
			message.append("ECO type Object is not selected.\n");
			return message.toString();
		}
		
		if(targetComponents.length > 1){
			message.append("Select just one ECO/ECI type object.\n");
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
				message.append("Select MECO type object.\n");
				return message.toString();
			}
		}
		
		try{
			/** 권한 체크 **/
			if(!changeRevision.okToModify())
				message.append("You don't have privilage to write.\n");

			/** 결재 여부 **/
			String[] processProps = changeRevision.getProperties(new String[]{"date_released", "process_stage_list"});
			if(!processProps[0].equals("") && !processProps[1].equals("Creator"))
				message.append("Already payment in progress.\n");
			
			/** infodba여부 확인 **/
			if(session.getUserName().equals("infodba"))
				message.append("infodba cannot request.\n");
			
			/** Revision check out 여부 확인 **/
			if(changeRevision.isCheckedOut())
				message.append(changeRevision+" is in checkout status\n");
			
			/** Item check out 여부 확인 **/
			if(changeRevision.getItem().isCheckedOut())
				message.append(changeRevision.getItem()+" is in checkout status\n");
			
		}catch(Exception e){
			e.printStackTrace();
			message.append("Error occurred in validation().\n");
			message.append(e.toString());
		}
		
		

		return message.toString();
	}
}