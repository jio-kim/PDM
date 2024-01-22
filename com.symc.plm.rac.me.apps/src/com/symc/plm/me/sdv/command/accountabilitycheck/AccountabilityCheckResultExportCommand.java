package com.symc.plm.me.sdv.command.accountabilitycheck;

import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.me.sdv.operation.accountabilitycheck.AccountabilityCheckResultExportOperation;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Accountability Check Result 를 출력하는 Command
 * [SR150106-015] 20151023 taeku.jeong  Accountability Check 결과 Excel로 Export 기능 추가
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
