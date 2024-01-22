package com.symc.plm.rac.prebom.preospec.commands;

import com.symc.plm.rac.prebom.preospec.dialog.PreOSpecMandatoryDlg;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.MessageBox;

public class PreOSpecMandatoryCommand extends AbstractAIFCommand {
	@Override
	protected void executeCommand() throws Exception {
		// Target
		TCComponent cOSpec = getTarget();
		
		// OSpec을 선택해야만 수행 가능
		if (cOSpec == null || !cOSpec.getType().equals("S7_OspecSetRevision")) {
			MessageBox.post("Select a Pre OSpec Revision.", "Check Target", MessageBox.ERROR);
			return;
		}
		
		PreOSpecMandatoryDlg dlg = new PreOSpecMandatoryDlg(AIFDesktop.getActiveDesktop().getShell(), cOSpec);
		dlg.open();
	}
	
	/**
	 * Get Selected Component
	 * @return
	 */
	public TCComponent getTarget() throws Exception {
        TCComponent target = null;
        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

        if(aaifcomponentcontext != null && aaifcomponentcontext.length == 1) {
            target = (TCComponent) aaifcomponentcontext[0].getComponent();
            return target;
        }
        
        return target;
    }
}
