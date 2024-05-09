package com.symc.plm.rac.prebom.masterlist.commands;

import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.masterlist.dialog.ComparePreBOMDlg;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class ComparePreBOMCommand extends AbstractAIFCommand {

	private TCComponentItemRevision selectedRevision = null;
	private static ComparePreBOMDlg dlg = null;
	
	@Override
	protected void executeCommand() throws Exception {
		InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		
		if( coms[0] instanceof TCComponentBOMLine ){
			if( ((TCComponentBOMLine)coms[0]).window().isModified()){
				MessageBox.post(AIFUtility.getActiveDesktop().getFrame(), "Save window first...", "INFO", MessageBox.INFORMATION);
				return;
			}
			selectedRevision = ((TCComponentBOMLine)coms[0]).getItem().getLatestItemRevision();
		}else if( coms[0] instanceof TCComponentItem){
			selectedRevision = ((TCComponentItem)coms[0]).getLatestItemRevision();
		}else if(coms[0] instanceof TCComponentItemRevision){
			selectedRevision = ((TCComponentItemRevision)coms[0]).getItem().getLatestItemRevision();
		}else{
			MessageBox.post(AIFUtility.getActiveDesktop().getFrame(), "Select a Pre_Product, Pre_Function or Pre-FMP Type.", "INFO", MessageBox.INFORMATION);
			return;
		}
		
		String selectedType = selectedRevision.getType();
		
		if( selectedType.equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE) 
				|| selectedType.equals(TypeConstant.S7_PREFUNCTIONREVISIONTYPE)
				|| selectedType.equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)){
			preBomCompareAction();
		}else{
			MessageBox.post(AIFUtility.getActiveDesktop().getFrame(), "Select a Pre_Product, Pre_Function or Pre-FMP BOM Line.", "INFO", MessageBox.INFORMATION);
			return;
		}
		super.executeCommand();
	}

	private void preBomCompareAction() throws TCException{
		if( dlg != null){
			dlg.dispose();
		}
		
		dlg = new ComparePreBOMDlg(selectedRevision.getItem());
		dlg.setVisible(true);
	}
}
