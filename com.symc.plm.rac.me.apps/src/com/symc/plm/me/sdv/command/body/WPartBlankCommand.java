package com.symc.plm.me.sdv.command.body;

import com.kgm.common.utils.CustomUtil;
import com.symc.plm.me.sdv.operation.body.WPartBlankOperation;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Accountability Check Result 를 출력하는 Command
 * [SR150106-015] 20151023 taeku.jeong  Accountability Check 결과 Excel로 Export 기능 추가
 * @author Taeku
 *
 */
public class WPartBlankCommand extends AbstractAIFCommand {

	private TCSession session;
	
	public WPartBlankCommand() {

		this.session = CustomUtil.getTCSession();
		
	}

	@Override
	protected void executeCommand() throws Exception {

		WPartBlankOperation operation = new WPartBlankOperation();
		 session.queueOperation(operation);
		
	}


}
