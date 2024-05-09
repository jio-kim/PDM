package com.kgm.commands.createfolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.kgm.common.SYMCClass;
import com.kgm.common.operation.SYMCAWTAbstractCreateOperation;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class CreateFolderOperation extends SYMCAWTAbstractCreateOperation {

	private String folderName;
	private String folderDesc;
	private String folderType;
	private TCSession session = CustomUtil.getTCSession();
	private TCComponentFolder folder;
	private Registry registry = Registry.getRegistry(this);

	/**
	 * ������.
	 * 
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2013. 1. 10.
	 * @param dialog
	 */
	public CreateFolderOperation(AbstractAIFDialog dialog) {
		super(dialog);
	}

	@SuppressWarnings("unused")
    @Override
	public void createItem() throws Exception {
		if (dialog instanceof CreateFolderDialog) {
			if(true) {
				folderName = ((CreateFolderDialog) dialog).getInfoPanel().getFolderNameTF().getText();
				folderDesc = ((CreateFolderDialog) dialog).getInfoPanel().getFolderDescTA().getText();
				HashMap<String, String> map = ((CreateFolderDialog) dialog).getInfoPanel().getFolderTypeMap();

				folderType = (String) map.get(((CreateFolderDialog) dialog).getInfoPanel().getSelRadioValue());
				folder = CustomUtil.createFolder(folderName, folderDesc, folderType);
			} else {
				MessageBox.post(dialog, registry.getString("CreateFolderDialog.MESSAGE.NoCreateRole"), 
						registry.getString("CreateFolderDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			}
		}
	}

	@Override
	public void endOperation() throws Exception {
		if (folder != null) {
			InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();
			if (comps == null || comps.length == 0) {
				homeFolderAdd();
			} else if (comps.length == 1) {
				TCComponent comp = (TCComponent) comps[0];
				if (comp instanceof TCComponentFolder) {
					comp.add(SYMCClass.CONTENT_REL, folder);
				} else {
					homeFolderAdd();
				}
			} else {
				homeFolderAdd();
			}

			/** ��� �� Copy ���� */
			copyModel();
			
			folder.refresh();
		}
	}

	/**
	 * ��� Model Copy üũ �� Copy ó��.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @throws TCException
	 * @since : 2013. 1. 10.
	 */
	@SuppressWarnings("rawtypes")
    private void copyModel() throws Exception {
		if (folder != null) {
			if (dialog instanceof CreateFolderDialog) {
				TCComponent comp = ((CreateFolderDialog) dialog).getSelectComp();

				if (comp != null) {
					if (comp instanceof TCComponentItem) {
						Vector getBomVtr = new Vector();
						ArrayList newItemList = new ArrayList();
						
						TCProperty[] pro = comp.getTCProperties(registry
								.getStringArray("CopyModel.PROPERTYS"));

						String name = pro[0].getStringValue();
						String desc = pro[1].getStringValue();

						String type = comp.getType();

						TCComponentItem copyItem = CustomUtil.createItem(type,
								CustomUtil.getNextItemId(type), SYMCClass.ITEM_REV_ID, name, desc);

						folder.add(SYMCClass.CONTENT_REL, copyItem);
						
						TCComponentItemRevision itemRev = ((TCComponentItem) comp).getLatestItemRevision();
						
						AIFComponentContext[] context = null;
						context = itemRev.getRelated("structure_revisions"); // ���� �����´�.
						getBomVtr = new Vector();
						newItemList = new ArrayList();

						getBomVtr = CustomUtil.getBomLine(context, getBomVtr); // �����ΰ����´�.
						newItemList = CustomUtil.getNewBomLine(copyItem, newItemList, getBomVtr);

						int bomVtrSize = getBomVtr.size();
						TCComponentItem saveNewTopItem = null;
						int parentIndex = -1;

						for (int i = 0; i < bomVtrSize; i++) {
							// ��� ��������
							TCComponentBOMLine bomLine = ((TCComponentBOMLine) getBomVtr.get(i));

							TCComponentBOMLine parentLine = bomLine.parent();

							if (parentLine != null) {
								for (int z = 0; z < bomVtrSize; z++) {
									if (getBomVtr.get(z) == parentLine) {
										parentIndex = z;
										break;
									}
								}
								/** ���� ���� �� BOM ���� ������ BOM ���� ����� ���� TopItem, �ڽ� ȹ�� */
								saveNewTopItem = (TCComponentItem) newItemList.get(parentIndex);

								/** BOM ���� ���� ����. */
								CustomUtil.bomViewItemRevisionCheck(saveNewTopItem.getLatestItemRevision(),
										(TCComponentItem) newItemList.get(i));
								
								folder.add(SYMCClass.CONTENT_REL, (TCComponentItem) newItemList.get(i));
							}
						} // for end
					} // if end
				} // if end
			} // if end
		} // if end
	}

	@Override
	public void setProperties() throws Exception {
	}

	@Override
	public void startOperation() throws Exception {
	}

	/**
	 * ������ ������ �α� ������ Ȩ������ �ٿ� �ִ´�.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @throws TCException
	 * @since : 2013. 1. 10.
	 */
	public void homeFolderAdd() throws TCException {
		TCComponentFolder folder = session.getUser().getHomeFolder();
		folder.add(SYMCClass.CONTENT_REL, this.folder);
	}

}
