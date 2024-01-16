package com.ssangyong.commands.common;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;

public class PasteToCorporateFolderCommand extends AbstractAIFCommand {

	public PasteToCorporateFolderCommand() {
		execute();
	}

	public void execute() {
		TCSession session = null;
		TCAccessControlService accessControlService = null;

		InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
		TCComponent accessor = null;

		try {
			if (validation(component)) {
				session = (TCSession) component.getSession();
				accessControlService = session.getTCAccessControlService();
				boolean isWrite = accessControlService.checkPrivilege((TCComponent) component, TCAccessControlService.WRITE);
				if (!isWrite) {
					accessor = accessControlService.getEffectiveACLInfo((TCComponent) component)[0].getAccessor();
					accessControlService.grantPrivilege((TCComponentFolder) component, accessor, TCAccessControlService.WRITE);

					AIFClipboard aifclipboard = AIFPortal.getClipboard();
					Transferable transferable = aifclipboard.getContents(null);
					if (transferable == null) {
						MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", "Clipboard is empty.");
					} else {
						Vector vector = (Vector) transferable.getTransferData(new DataFlavor(Vector.class, "AIF Vector"));
						for (int i = 0; i < vector.size(); i++) {
							Object objVector = vector.elementAt(i);
							TCComponent componentVector = null;
							boolean isOwner = false;
							
							if (objVector instanceof TCComponent) {
								componentVector = (TCComponent)vector.elementAt(i); 
							} else {
								MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", componentVector + " > Check a Target Object.");
								continue;
							}
							
							// Component의 Owner만 Paste 가능
							TCComponent componentOwningUser = componentVector.getReferenceProperty("owning_user");
							if (componentOwningUser instanceof TCComponentUser) {
								TCComponentUser user = (TCComponentUser)componentOwningUser;
								String sComponentOwningUserID = user.getUserId();
								isOwner = session.getUser().getUserId().equals(sComponentOwningUserID);
							}
							
							if (!isOwner) {
								MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", componentVector + " > Check Owner.");
								continue;
							}
							
							// Folder는 Paste 불가
							if (componentVector instanceof TCComponentFolder) {
								MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", componentVector + " > Can not Paste a Folder to Corporate Folder.");
								continue;
							}
							
							((TCComponentFolder) component).add("contents", componentVector);
						}
					}
				}
			} else {
				MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Information", "Corporate Folder를 선택해주세요.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} finally {
			try {
				if(accessor != null) {
					accessControlService.unsetPrivilege((TCComponent) component, accessor, TCAccessControlService.WRITE);
				}
				((TCComponent) component).refresh();
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
