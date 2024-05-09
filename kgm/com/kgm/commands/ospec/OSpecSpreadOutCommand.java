package com.kgm.commands.ospec;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.kgm.common.WaitProgressBar;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

public class OSpecSpreadOutCommand extends AbstractAIFCommand {

	TCComponentItemRevision ospec = null;
	OSpecMainDlg dialog = null;

	@Override
	protected void executeCommand() throws Exception {
		ospecImport();
	}

	private void ospecImport() throws Exception{
		final OSpecImportDlg dlg = new OSpecImportDlg();
		dlg.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				ospec = dlg.getSelectedOSpec();
				if( ospec == null ){
					return;
				}
				// [20240313][UPGRADE] 오류 발생 시 ProgressBar 에 출력하도록 수정
				WaitProgressBar waitBar = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
				try {
					waitBar.start();
					waitBar.setStatus("Loading.......");
					dialog = new OSpecMainDlg(ospec);
					waitBar.close();
					dialog.showDialog();
				} catch (Exception e1) {
					e1.printStackTrace();
					waitBar.setStatus(e1.getMessage());
					waitBar.setShowButton(true);
				}
			}
		});
		dlg.showDialog();
		dlg.setModal(true);
		setRunnable(dlg);
	}
}
