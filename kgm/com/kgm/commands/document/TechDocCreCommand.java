package com.kgm.commands.document;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class TechDocCreCommand extends AbstractAIFCommand {

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 7.
	 */
	public TechDocCreCommand() {
		/** Dialog 호출. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		TechDocCreDialog parDialog = new TechDocCreDialog(shell);
		parDialog.setApplyButtonVisible(false);
		parDialog.open();
	}
}
