package com.kgm.commands.masterdata;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class MasterDataCommand extends AbstractAIFCommand {
	
	/**
	 * ������ MasterDataDialog ȣ��
	 * @copyright : S-PALM
	 * @author : �ǿ���
	 * @since  : 2012. 12. 27.
	 */
	public MasterDataCommand() {
		MasterDataDialog dialog = new MasterDataDialog(AIFUtility.getActiveDesktop());
		dialog.setModal(true);
		setRunnable(dialog);
	}

}
