package com.kgm.commands.document;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class TechDocCreCommand extends AbstractAIFCommand {

	/**
	 * ������.
	 * 
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2012. 12. 7.
	 */
	public TechDocCreCommand() {
		/** Dialog ȣ��. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		TechDocCreDialog parDialog = new TechDocCreDialog(shell);
		parDialog.setApplyButtonVisible(false);
		parDialog.open();
	}
}
