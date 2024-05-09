package com.symc.plm.me.sdv.command.accountabilitycheck;

import com.kgm.common.utils.CustomUtil;
import com.symc.plm.me.sdv.operation.accountabilitycheck.AccountabilityCheckResultExportOperation;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Accountability Check Result �� ����ϴ� Command
 * [SR150106-015] 20151023 taeku.jeong  Accountability Check ��� Excel�� Export ��� �߰�
 * @author Taeku
 *
 */
public class AccountabilityCheckResultExportCommand extends AbstractAIFCommand {

	private TCSession session;
	
	public AccountabilityCheckResultExportCommand() {

		this.session = CustomUtil.getTCSession();
		
		AccountabilityCheckResultExportOperation operation = new AccountabilityCheckResultExportOperation();
		 session.queueOperation(operation);
		
	}


}
