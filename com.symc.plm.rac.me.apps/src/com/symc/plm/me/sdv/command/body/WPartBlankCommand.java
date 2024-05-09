package com.symc.plm.me.sdv.command.body;

import com.kgm.common.utils.CustomUtil;
import com.symc.plm.me.sdv.operation.body.WPartBlankOperation;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Accountability Check Result �� ����ϴ� Command
 * [SR150106-015] 20151023 taeku.jeong  Accountability Check ��� Excel�� Export ��� �߰�
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
