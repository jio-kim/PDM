package com.symc.plm.me.sdv.operation.meco;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialogOpertation;

import com.symc.plm.me.sdv.dialog.meco.UpdateEffectivityDialog;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class UpdateEffectivityOperation extends AbstractTCSDVOperation implements IDialogOpertation {

	public UpdateEffectivityOperation() {
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
		UpdateEffectivityDialog updateEffectivityDialog = new UpdateEffectivityDialog(shell);
		InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
		
		if (comps.length > 0){
			
			TCComponent comp = (TCComponent) comps[0];
			if (!(comp instanceof TCComponentChangeItemRevision)) {
				
				MessageBox.post(shell, "Should you select meco's revision when try to update effectivity on MECO!", "Warning", MessageBox.WARNING);
				updateEffectivityDialog.close();
				
			}else{
				
				updateEffectivityDialog.open();	
				
			}
		}
    	

	}

}
