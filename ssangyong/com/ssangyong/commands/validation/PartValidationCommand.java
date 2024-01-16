package com.ssangyong.commands.validation;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class PartValidationCommand extends AbstractAIFCommand {

	private static PartValidationDialog dialog = null;
	
	@Override
	protected void executeCommand() throws Exception {
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		
		if( coms.length > 0 && coms[0] instanceof TCComponentBOMLine){
			
			TCComponentItem item = ((TCComponentBOMLine)coms[0]).getItem();
			
			Registry registry = Registry.getRegistry(this);
			if( !item.getType().equals(SYMCClass.S7_VARIANTPARTTYPE)){
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.onlyVariant"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			
			if( dialog != null ){
				dialog.dispose();
			}
			
			dialog = new PartValidationDialog((TCComponentBOMLine)coms[0]);
			setRunnable(dialog);
			
		}
			
		
		
		
		super.executeCommand();
	}

}
