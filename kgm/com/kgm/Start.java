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
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "개발 서버로 접속하셨습니다.", "알림", MessageBox.INFORMATION);
		}
		String isLocalWebSQL = System.getProperty("isLocalWebSQL");
		if(isLocalWebSQL != null)
		{
			boolean isLocal = isLocalWebSQL.equalsIgnoreCase("true");
			if(isLocal)
			{
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "SsangyongWeb Local 사용 설정 됨.", "알림", MessageBox.INFORMATION);
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
//				String sContent = "내 작업 리스트에 새로운 타스크가 " + newTasks +" 개 도착 하였습니다. " +
//						"\n좌측 상단의 \"내작업 리스트\"를 클릭 하시고, Inbox 하위의 Task To Perform 폴더 하위의 Task를 선택 하시고, 우측의 뷰어 탭을 선택 하신 후 결재를 수행 하십시오." +
//						"\n\n단, DCS(설계구상서시스템) 사용자는\n좌측 하단 Design Concept System을 클릭하고 이용하시기 바랍니다.";

				String sContent =
						"내 작업 리스트에 새로운 타스크가 도착 하였습니다." +
						"\n\n■ ECO : " + iECOCount + "건" +
						"\n■ MECO : " + iMECOCount + "건" +
						"\n - 좌측 상단의 \"내작업 리스트\"를 클릭 하시고, Inbox 하위의 Task To Perform 폴더 하위의 Task를 선택 하시고, 우측의 뷰어 탭을 선택 하신 후 결재를 수행 하십시오." +
						"";
//						"\n\n■ DCS : " + iDCSCount + "건" +
//						"\n - DCS(설계구상서시스템) 사용자는 좌측 하단 Design Concept System을 클릭하고 이용하시기 바랍니다.";
				MessageBox.post(sContent, "알림", MessageBox.INFORMATION);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void earlyStartup() {
		
	}
}
