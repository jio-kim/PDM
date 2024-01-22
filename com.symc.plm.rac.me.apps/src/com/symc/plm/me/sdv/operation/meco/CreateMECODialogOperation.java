package com.symc.plm.me.sdv.operation.meco;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialogOpertation;

import com.symc.plm.me.sdv.dialog.meco.MECOSWTDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.util.MessageBox;

public class CreateMECODialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

	public CreateMECODialogOperation() {
	}


	@Override
	public void startOperation(String commandId) {

	}

	@Override
	public void endOperation() {
		
	}

	@Override
	public void executeOperation() throws Exception {
		
    	Shell shell = AIFUtility.getActiveDesktop().getShell();
		MECOSWTDialog mecodialog = new MECOSWTDialog(shell);
		InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
		
		if (comps.length > 0){
			
			TCComponent comp = (TCComponent) comps[0];
			if (!(comp instanceof TCComponentFolder)) {
				
				MessageBox.post(shell, "Should select folder when try to create MECO!", "Warning", MessageBox.WARNING);
				mecodialog.close();
				
			}else{
				
				mecodialog.open();	
				
			}
		}
    	

	}

}
