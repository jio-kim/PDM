package com.ssangyong.common.bundlework.command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.bundlework.imp.BWVariantOptionImpDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.TcDefinition;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class VariantOptionImportCommand extends AbstractAIFCommand {

	public VariantOptionImportCommand() throws TCException {
		/** Dialog 호출. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		
		//PSE에서만 가능한 기능이며 Command Supression에서 설정해야함.
		//현재 선택한 아이템은 Product Type이어야 한다.
		if( coms != null && coms.length > 0){
			TCComponentBOMLine target = (TCComponentBOMLine)coms[0];
			if( target.getItem().getType().equals(TcDefinition.PRODUCT_ITEM_TYPE)){
				BWVariantOptionImpDialog dialog = new BWVariantOptionImpDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, (TCComponentBOMLine)coms[0]);
				dialog.dialogOpen();
			}else{
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "select a Product Item.", "INFORMATION", MessageBox.WARNING);
				return;
			}
			
		}
	}
}
