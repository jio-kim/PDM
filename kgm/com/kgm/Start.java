package com.kgm;

import org.eclipse.ui.IStartup;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class Start implements IStartup{

	public Start(){

		TCComponentUser loginUser = CustomUtil.getTCSession().getUser();
		boolean isDEV = CustomUtil.getTCSession().getServerName().contains("DEV");
		if(isDEV)
		{
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "���� ������ �����ϼ̽��ϴ�.", "�˸�", MessageBox.INFORMATION);
		}
		String isLocalWebSQL = System.getProperty("isLocalWebSQL");
		if(isLocalWebSQL != null)
		{
			boolean isLocal = isLocalWebSQL.equalsIgnoreCase("true");
			if(isLocal)
			{
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "SsangyongWeb Local ��� ���� ��.", "�˸�", MessageBox.INFORMATION);
			}
		}
		try {
			TCComponent inBox = loginUser.getUserInBox();
			TCComponent[] child = inBox.getRelatedComponents("contents");
			TCComponent[] child1 = child[0].getRelatedComponents("contents");
			
			int newTasks = 0;
			
			int iECOCount = 0;
			int iMECOCount = 0;
			int iDCSCount = 0;
			
			for(int i=0; i<child1.length; i++){
				if(child1[i].toDisplayString().equals("Tasks To Perform")){
					newTasks = child1[i].getChildrenCount();
					
					AIFComponentContext[] tasks = child1[i].getChildren();
					for (int jnx = 0; jnx < tasks.length; jnx++) {
						InterfaceAIFComponent aifComponent = tasks[jnx].getComponent();
						if (aifComponent instanceof TCComponent) {
							TCComponent component = (TCComponent)aifComponent;
							if (component instanceof TCComponentTask) {
								TCComponentTask task = (TCComponentTask)component;
								String sProcessTemplate = task.getProcess().getProperty("process_template");
								
								if (sProcessTemplate.equals("SYMC_ECO")) {
									iECOCount++;
								} else if (sProcessTemplate.equals("SYMC_MECO_1Level")
										|| sProcessTemplate.equals("SYMC_MECO_2Level")
										|| sProcessTemplate.equals("SYMC_MECO_3Level")
										|| sProcessTemplate.equals("SYMC_MEW_1Level")
										|| sProcessTemplate.equals("SYMC_MEW_2Level")
										|| sProcessTemplate.equals("SYMC_ANY_Self_M_Release")
										|| sProcessTemplate.equals("SYMC_WORKTEMPLATE_EN_M_Release")
										|| sProcessTemplate.equals("SYMC_WORKTEMPLATE_KR_M_Release")) {
									iMECOCount++;
								} else if (sProcessTemplate.equals("SYMC_DCS")
										|| sProcessTemplate.equals("SYMC_DCS_TEAM")
										|| sProcessTemplate.equals("SYMC_PSC")) {
									iDCSCount++;
								} else {
									continue;
								}
							}
						}
					}
				}
			}
			
			if(newTasks > 0){
//				String sContent = "�� �۾� ����Ʈ�� ���ο� Ÿ��ũ�� " + newTasks +" �� ���� �Ͽ����ϴ�. " +
//						"\n���� ����� \"���۾� ����Ʈ\"�� Ŭ�� �Ͻð�, Inbox ������ Task To Perform ���� ������ Task�� ���� �Ͻð�, ������ ��� ���� ���� �Ͻ� �� ���縦 ���� �Ͻʽÿ�." +
//						"\n\n��, DCS(���豸�󼭽ý���) ����ڴ�\n���� �ϴ� Design Concept System�� Ŭ���ϰ� �̿��Ͻñ� �ٶ��ϴ�.";

				String sContent =
						"�� �۾� ����Ʈ�� ���ο� Ÿ��ũ�� ���� �Ͽ����ϴ�." +
						"\n\n�� ECO : " + iECOCount + "��" +
						"\n�� MECO : " + iMECOCount + "��" +
						"\n - ���� ����� \"���۾� ����Ʈ\"�� Ŭ�� �Ͻð�, Inbox ������ Task To Perform ���� ������ Task�� ���� �Ͻð�, ������ ��� ���� ���� �Ͻ� �� ���縦 ���� �Ͻʽÿ�." +
						"";
//						"\n\n�� DCS : " + iDCSCount + "��" +
//						"\n - DCS(���豸�󼭽ý���) ����ڴ� ���� �ϴ� Design Concept System�� Ŭ���ϰ� �̿��Ͻñ� �ٶ��ϴ�.";
				MessageBox.post(sContent, "�˸�", MessageBox.INFORMATION);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void earlyStartup() {
		
	}
}
