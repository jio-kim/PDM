package com.kgm.commands.downdataset;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

public class DownDatasetCommand extends AbstractAIFCommand {

	/**
	 * 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 7.
	 */
	public DownDatasetCommand() {
		AIFComponentContext[] comps = AIFUtility.getCurrentApplication().getTargetContexts();
		
		if(comps.length == 0){
			MessageBox.post("DownLoad 할 DataSet을 선택 한 후 실행 하십시오.", "알림", MessageBox.INFORMATION);
			return;
		}
		
		DownDataSetDialog dialog = new DownDataSetDialog(AIFUtility.getActiveDesktop(), comps);
		// Modal 상태 (true:Modal, false:NonModal)
		dialog.setModal(true);
		// 스레드 단위로 Dialog 실행.
		setRunnable(dialog);
		
//		Shell shell = AIFUtility.getActiveDesktop().getShell();
//		DownDatasetSWTDialog dialog = new DownDatasetSWTDialog(shell, comps);
//		dialog.open();
	}
	
}
