package com.ssangyong.commands.common;

import org.eclipse.jface.dialogs.MessageDialog;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;

public class CutFromCorporateFolderCommand extends AbstractAIFCommand {

	public CutFromCorporateFolderCommand() {
		execute();
	}

	public void execute() {
		TCSession session = null;
		TCAccessControlService accessControlService = null;
		TCComponent accessor = null;
		TCComponentFolder folder = null;
		
		try {
			AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
			AIFComponentContext aifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();
			
			if (aifcomponentcontext.length == 0) {
				MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", "Select Target Objects");
				return;
			}
			
			session = (TCSession) AIFUtility.getDefaultSession();
			accessControlService = session.getTCAccessControlService();
			
			for (int inx = 0; inx < aifcomponentcontext.length; inx++) {
				InterfaceAIFComponent aifComponentSelected = aifcomponentcontext[inx].getComponent();
				InterfaceAIFComponent aifComponentParent = aifcomponentcontext[inx].getParentComponent();
				
				if (aifComponentSelected instanceof TCComponent) {
					TCComponent component = (TCComponent) aifComponentSelected;
					TCComponent owningUser = component.getReferenceProperty("owning_user");
					
					// Cut 하려는 Component의 Owning User일 경우에만 수행
					if (owningUser instanceof TCComponentUser) {
						TCComponentUser user = (TCComponentUser) owningUser;
						String sOwningUser = user.getUserId();
						
						if (session.getUser().getUserId().equals(sOwningUser)) {
							// Parent가 Corporate Folder일 경우
							if (aifComponentParent instanceof TCComponentFolder && validation(aifComponentParent)) {
								folder = (TCComponentFolder) aifComponentParent;
								boolean isWrite = accessControlService.checkPrivilege((TCComponent) folder, TCAccessControlService.WRITE);
								
								// Write권한이 없을 경우
								if (!isWrite) {
									// 권한 부여
									accessor = accessControlService.getEffectiveACLInfo((TCComponent) folder)[0].getAccessor();
									accessControlService.grantPrivilege((TCComponentFolder) folder, accessor, TCAccessControlService.WRITE);
									
									folder.cutOperation("contents", new TCComponent[] {component});
									
									accessControlService.unsetPrivilege((TCComponent) folder, accessor, TCAccessControlService.WRITE);
									folder.refresh();
								}
							} else {
								MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", component + " > Check a Target Object's Parent Folder");
								return;
							}
						} else {
							MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", component + " > Check a Target Object's Owner");
							return;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} finally {
			try {
				if(accessor != null) {
					accessControlService.unsetPrivilege((TCComponent) folder, accessor, TCAccessControlService.WRITE);
				}
				((TCComponent) folder).refresh();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
	}

	public boolean validation(InterfaceAIFComponent component) {
		String type = component.getType();
		if (type.equals("S7_CorpOptionF")) {
			return true;
		}

		return false;
	}

}
