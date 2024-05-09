package com.kgm.commands.delete;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;

public class DeleteVehiclePartCommand extends AbstractAIFCommand {

	@Override
	protected void executeCommand() throws Exception {
		DeleteVehiclePartDlg dialog = new DeleteVehiclePartDlg(getTargets());
		setRunnable(dialog);

		super.executeCommand();
	}

	/**
	 * 선택한 Part 목록 가져오기
	 * @return
	 */
	public TCComponent[] getTargets() {
		TCComponent[] targets = null;
		AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
		AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

		if (aaifcomponentcontext != null && aaifcomponentcontext.length > 0) {
			targets = new TCComponent[aaifcomponentcontext.length];

			for (int inx = 0; inx < aaifcomponentcontext.length; inx++) {
				targets[inx] = (TCComponent) aaifcomponentcontext[inx].getComponent();
			}
			return targets;
		}
		return null;
	}
}
