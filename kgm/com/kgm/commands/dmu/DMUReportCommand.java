package com.kgm.commands.dmu;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.pse.services.PSEApplicationService;
import com.teamcenter.rac.util.MessageBox;

public class DMUReportCommand extends AbstractAIFCommand {


	public DMUReportCommand() {		
	}
	
	protected void executeCommand() {
		AbstractAIFUIApplication aifUtility = AIFUtility.getCurrentApplication();
		if(aifUtility instanceof PSEApplicationService) {
			PSEApplicationService service = (PSEApplicationService)aifUtility;
			TCComponentBOMLine totBomLine = service.getTopBOMLine();
			if(totBomLine == null) {
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "There is no Assembly Item in Structure Manager.", "ERROR", MessageBox.ERROR);
			} else {				
				/** Dialog »£√‚. */
				Shell shell = AIFUtility.getActiveDesktop().getShell();
				DMUReportDialog dialog = new DMUReportDialog(shell);
				dialog.setApplyButtonVisible(false);
				dialog.open();
			}
		}

	}
}
