package com.kgm.commands.downdataset;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

public class DownDatasetCommand extends AbstractAIFCommand {

	/**
	 * ������.
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2013. 1. 7.
	 */
	public DownDatasetCommand() {
		AIFComponentContext[] comps = AIFUtility.getCurrentApplication().getTargetContexts();
		
		if(comps.length == 0){
			MessageBox.post("DownLoad �� DataSet�� ���� �� �� ���� �Ͻʽÿ�.", "�˸�", MessageBox.INFORMATION);
			return;
		}
		
		DownDataSetDialog dialog = new DownDataSetDialog(AIFUtility.getActiveDesktop(), comps);
		// Modal ���� (true:Modal, false:NonModal)
		dialog.setModal(true);
		// ������ ������ Dialog ����.
		setRunnable(dialog);
		
//		Shell shell = AIFUtility.getActiveDesktop().getShell();
//		DownDatasetSWTDialog dialog = new DownDatasetSWTDialog(shell, comps);
//		dialog.open();
	}
	
}
