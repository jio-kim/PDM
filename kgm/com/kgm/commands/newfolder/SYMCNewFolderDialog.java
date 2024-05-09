package com.kgm.commands.newfolder;

import com.kgm.common.SYMCClass;
import com.teamcenter.rac.commands.newfolder.NewFolderCommand;
import com.teamcenter.rac.commands.newfolder.NewFolderDialog;
import com.teamcenter.rac.util.MessageBox;

@Deprecated
public class SYMCNewFolderDialog extends NewFolderDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * Folder 생성 재정의. SSANGYONGNewFolderDialog 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
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
				MessageBox.post(this, "[ " + session.getUser().toString() + " ] 님은 < " + selFolderType + " > 폴더를 생성 할 권한이 없습니다.",
						"알림!", MessageBox.INFORMATION);
			}
		} else {
			super.startCommandOperation();
		}
	}

	/**
	 * 생성 할 선택 Folder Type 확인 메소드.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
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
