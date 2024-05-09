package com.kgm.commands.eciecr;

import org.eclipse.swt.widgets.Shell;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class EciEcrCommand extends AbstractAIFCommand {

	public EciEcrCommand() {
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		try {
			
			TCSession session = CustomUtil.getTCSession();
			TCComponentRole loginUserRole = session.getRole();
			System.out.println("session.hasBypass():"+session.hasBypass());
			if (!loginUserRole.toDisplayString().equals("DBA")) {
				MessageBox.post(shell, "DBA ������ ���� ����ڸ� �����մϴ�.", "����", MessageBox.ERROR);
				return;
			}
			
			EciEcrDialog dialog = new EciEcrDialog(shell, session);
			dialog.pack();
			setRunnable(dialog);

		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(shell, e.getMessage(), "����", MessageBox.ERROR);
		}
	}

}
