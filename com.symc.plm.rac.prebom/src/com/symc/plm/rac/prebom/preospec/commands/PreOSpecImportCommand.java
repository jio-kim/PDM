package com.symc.plm.rac.prebom.preospec.commands;

import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.preospec.dialog.PreOSpecImportDlg;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class PreOSpecImportCommand extends AbstractAIFCommand {

	TCComponentItemRevision ospec = null;
	InterfaceAIFComponent []targets = null;
	
//	@Override
//	protected void executeCommand() throws Exception {
//		if (! targetCheck())
//		    return;
//		ospecImport();
//		super.executeCommand();
//	}

	private boolean targetCheck() throws Exception {
	    try
	    {
	    	AbstractAIFUIApplication currentApplication = AIFUtility.getCurrentApplication();
	    	
	    	String appId = currentApplication.getApplicationId();
	    	//[20240308][UPGRADE] BOM 구조에서만 실행되도록 함
	    	if(!"com.teamcenter.rac.pse.PSEApplication".equals(appId) && !"com.teamcenter.rac.cme.mpp.MPPApplication".equals(appId) 
	    			&& !"com.teamcenter.rac.cme.mbm.mbmApplication".equals(appId))
	    	{
	    		MessageBox.post(AIFUtility.getActiveDesktop(), "This menu cannot be executed in the current application.", "INFO", MessageBox.INFORMATION);
	    		return false;
	    	}
	    	
	        targets = CustomUtil.getTargets();

    	    if (targets == null || targets.length != 1 || ! (targets[0] instanceof TCComponentBOMLine) || ! ((TCComponentBOMLine) targets[0]).getItem().getType().equals(TypeConstant.S7_PREPRODUCTTYPE))
    	    {
                MessageBox.post(AIFUtility.getActiveDesktop(), "Select a Pre-Product item", "INFO", MessageBox.INFORMATION);
    	        return false;
    	    }

    	    return true;
	    }
	    catch (Exception ex)
	    {
	        throw ex;
	    }
    }

    private void ospecImport() throws Exception{
		
		final PreOSpecImportDlg dlg = new PreOSpecImportDlg(AIFUtility.getActiveDesktop().getFrame(), targets);
		dlg.setModal(true);
		setRunnable(dlg);
	}
    
    public PreOSpecImportCommand() throws Exception{
    	if (! targetCheck())
		    return;
		ospecImport();
    }
}
