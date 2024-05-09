package com.kgm.commands.newfolder;

import com.kgm.common.SYMCClass;
import com.teamcenter.rac.commands.newfolder.NewFolderCommand;
import com.teamcenter.rac.commands.newfolder.NewFolderDialog;
import com.teamcenter.rac.util.MessageBox;

@Deprecated
public class SYMCNewFolderDialog extends NewFolderDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * Folder ���� ������. SSANGYONGNewFolderDialog ������.
	 * 
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2012. 12. 12.
	 * @param arg0
	 */
	public SYMCNewFolderDialog(NewFolderCommand arg0) {
		super(arg0);
		
		setTitle("New Folder");
	}

	@Override
	public void startCommandOperation() {
		if (selectdTypeCheck()) {
			if (SYMCClass.userACLCheck()) {
				super.startCommandOperation();
			} else {
				String selFolderType = folderTypesPanel.getSelectedType(true);
				MessageBox.post(this, "[ " + session.getUser().toString() + " ] ���� < " + selFolderType + " > ������ ���� �� ������ �����ϴ�.",
						"�˸�!", MessageBox.INFORMATION);
			}
		} else {
			super.startCommandOperation();
		}
	}

	/**
	 * ���� �� ���� Folder Type Ȯ�� �޼ҵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2012. 12. 12.
	 * @return
	 */
	private boolean selectdTypeCheck() {
		String selFolderType = folderTypesPanel.getSelectedType(true);
		System.out.println("Selected Folder Type : " + selFolderType);
		
		String[] folderTypes = SYMCClass.FOLDERTYPECHECK();
		
		if(folderTypes == null){
			return false;
		}
		
		int folderTypesSize = folderTypes.length;
		String folderType = "";
		for(int i=0; i<folderTypesSize; i++){
			folderType = folderTypes[i].toString();
			if(selFolderType.equals(folderType)){
				return true;
			}
		}
		return false;
	}

}
