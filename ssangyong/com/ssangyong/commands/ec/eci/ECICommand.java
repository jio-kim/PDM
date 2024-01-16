package com.ssangyong.commands.ec.eci;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class ECICommand extends AbstractAIFCommand {
	
	public ECICommand() {
//		AIFDesktop activeDesktop = AIFUtility.getActiveDesktop();
//		ECIDialog dialog = new ECIDialog(activeDesktop);
//		dialog.setModal(false);
//		setRunnable(dialog);

//		TCComponentItemRevision selectedItemRevision = null;
//		
//		InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
//		if(comp != null ){
//			if(comp instanceof TCComponentItem){
//				TCComponentItem changeItem = (TCComponentItem)comp;
//				try {
//					selectedItemRevision = changeItem.getLatestItemRevision();
//				} catch (TCException e) {
//					e.printStackTrace();
//				}
//			}
//
//			if(comp instanceof TCComponentItemRevision){
//				selectedItemRevision = (TCComponentItemRevision)comp;
//			}
//		}
//		
//		Shell shell = AIFUtility.getActiveDesktop().getShell();
//		ECISWTDialog dialog = new ECISWTDialog(shell, selectedItemRevision);
		
		// [SR140701-022] jclee, ECI 생성 시 현재 선택한 Part와는 무관하게 작업 수행하도록 변경. 
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		ECISWTDialog dialog = new ECISWTDialog(shell, null);
		dialog.open();
	}
}
