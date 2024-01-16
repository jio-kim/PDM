package com.ssangyong.commands.workflow.dotask;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.workflow.commands.dotask.DoTaskPanel;

public class SSANGYONGDoTaskPanel extends DoTaskPanel {

	private static final long serialVersionUID = 1L;

	private TCComponentTask task;

	public SSANGYONGDoTaskPanel(AIFDesktop aifdesktop, AbstractAIFDialog abstractaifdialog, TCComponentTask tccomponenttask) {
		super(aifdesktop, abstractaifdialog, tccomponenttask);
		this.task = tccomponenttask;
	}

	public SSANGYONGDoTaskPanel(AIFDesktop aifdesktop, JPanel panel, TCComponentTask tccomponenttask) {
		super(aifdesktop, panel, tccomponenttask);
		this.task = tccomponenttask;
	}


	@Override
	public void endOperation() {
		super.endOperation();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				EcrReviewResultConfirmDialog dialog = new EcrReviewResultConfirmDialog(task);
				dialog.setModal(true);
			}
		});
	}
}
