package com.symc.plm.me.sdv.command.assembily;

import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.me.sdv.operation.assembly.CatpartToCGROperation;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.kernel.TCSession;

public class CatpartToCGRCommand extends AbstractAIFCommand {

	private TCSession session;
	
	public CatpartToCGRCommand() {

		this.session = CustomUtil.getTCSession();

		//MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Test", "Error", MessageBox.ERROR);

		CatpartToCGROperation operation = new CatpartToCGROperation();
        session.queueOperation(operation);

	}

}
