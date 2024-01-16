package com.ssangyong.commands.masterdata;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class MasterDataCommand extends AbstractAIFCommand {
	
	/**
	 * 생성자 MasterDataDialog 호출
	 * @copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2012. 12. 27.
	 */
	public MasterDataCommand() {
		MasterDataDialog dialog = new MasterDataDialog(AIFUtility.getActiveDesktop());
		dialog.setModal(true);
		setRunnable(dialog);
	}

}
