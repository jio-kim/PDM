package com.kgm.commands.logview;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.util.MessageBox;

public class LogViewCommand extends AbstractAIFCommand {

	/**
	 * 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 8.
	 */
	public LogViewCommand() {
		AIFComponentContext[] comps = AIFUtility.getCurrentApplication().getTargetContexts();
		
		if(comps.length == 0 || comps.length > 1){
			MessageBox.post("DownLoad 이력을 조회 할 DataSet을 하나 선택 한 후 실행 하십시오.", "알림", MessageBox.INFORMATION);
			return;
		}
		
		if(comps[0].getComponent() instanceof TCComponentDataset){
			LogViewDialog dialog = new LogViewDialog(AIFUtility.getActiveDesktop(), comps[0]);
			dialog.setModal(true);
			setRunnable(dialog);
		} else {
			MessageBox.post("DownLoad 이력을 조회 할 DataSet을 하나 선택 한 후 실행 하십시오.", "알림", MessageBox.INFORMATION);
			return;
		}
	}
}
