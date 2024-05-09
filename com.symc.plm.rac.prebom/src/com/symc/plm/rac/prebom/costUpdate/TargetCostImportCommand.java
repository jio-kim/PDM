package com.symc.plm.rac.prebom.costUpdate;

import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class TargetCostImportCommand extends AbstractAIFCommand {

	TCComponentItemRevision ospec = null;
	
	@Override
	protected void executeCommand() throws Exception {
		costImport();
		super.executeCommand();
	}

    private void costImport() throws Exception{
		
		final TargetCostImportDlg dlg = new TargetCostImportDlg();
		setRunnable(dlg);
	}
}
