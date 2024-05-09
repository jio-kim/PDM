package com.kgm.commands.bomedit.option;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.MessageBox;

public class CopyOptionCommand extends AbstractAIFCommand {
	protected void executeCommand() throws Exception {
		TCComponent compSourceObj = this.getTarget();
		if (compSourceObj != null && compSourceObj instanceof SYMCBOMLine) {
			SYMCBOMLine bomLine = (SYMCBOMLine) compSourceObj;
			SYMCBOMLine parentBOMLine = (SYMCBOMLine) bomLine.parent();
			
			// FMP 하위 1LV Part만 Option을 가지므로 해당 Item에 한해서만 수행
			if (parentBOMLine.getItem().getType().equals("S7_FunctionMast")) {
				String sOption = bomLine.getProperty("bl_occ_mvl_condition");
				copyOptionToClipboard(sOption);
				return;
			}
		}
		
		copyOptionToClipboard("");
	}

	/**
	 * 선택한 Component를 가져온다.
	 * @return
	 */
	public TCComponent getTarget() {
		TCComponent target = null;
		AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();

		AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

		if (aaifcomponentcontext != null && aaifcomponentcontext.length == 1) {
			target = (TCComponent) aaifcomponentcontext[0].getComponent();
			return target;
		} else {
			MessageBox.post("Check Selected Part.", "Select only one BOM Line.", "Option Copy Error", MessageBox.ERROR);
			return null;
		}
	}

	/**
	 * 선택한 BOM Line의 Option을 Clipboard에 저장
	 * @param sOption
	 */
	public void copyOptionToClipboard(String sOption) {
		StringSelection ss = new StringSelection(sOption);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(ss, ss);
	}
}
