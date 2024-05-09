package com.kgm.commands.createfolder;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.kgm.common.FunctionField;
import com.kgm.common.SYMCAWTLabel;
import com.kgm.common.SYMCClass;
import com.kgm.common.dialog.AbstractSearchResultDialog;
import com.kgm.common.utils.ComponentService;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponent;

/**
 * �˻��� ���Ͽ� TC ��ϵ� ����� �˻� �ϴ� Ŭ����.
 * @Copyright : S-PALM
 * @author   : �ǻ��
 * @since    : 2013. 1. 11.
 * Package ID : com.kgm.commands.createfolder.ItemSearchDialog.java
 */
@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class ItemSearchDialog extends AbstractSearchResultDialog {

	private static final long serialVersionUID = 1L;

	/** ������ ID textfield */
	private FunctionField itemNoTF;

	/** Item �̸� textfield */
	private FunctionField itemNameTF;

    private static String QueryRevisionTypes = "����(����) ������;����(����) ������;����(��Ÿ) ������;��ǰ ������;����(��Ÿ) ������;����(����) ������;����(����) ������;����(��) ������;������ ������;������ ������";

	/**
	 * ���� ǰ�� �Ǵ� �ַ�� ǰ������ �Ǵ��ϱ� ���� string �� ���� ǰ��: 0 �ַ�� ǰ��: 1
	 */
	private int searchType;

	private String searchItemType;

	private FunctionField field;

	private JDialog dialog;

	public ItemSearchDialog(JDialog dialog, String title, String headerMessage, int searchType, String searchItemType, FunctionField field) {
		super(dialog, title, headerMessage, new String[] { "object_string", "item_id", "object_type",
				"owning_user", "owning_group" });
		this.searchType = searchType;
		this.searchItemType = searchItemType;
		this.field = field;
		this.dialog = dialog;
	}

	@Override
	protected JPanel getSearchConditionPanel() {

		super.getSearchConditionPanel();

		SYMCAWTLabel label7 = new SYMCAWTLabel("Model ID");
		itemNoTF = new FunctionField(0);
		itemNoTF.setText("*");
		conditionPanel.add("1.1.right.center.preferred.preferred", label7);
		conditionPanel.add("1.2.left.center.resizable.preferred", itemNoTF);

		SYMCAWTLabel label1 = new SYMCAWTLabel("Model Name");
		itemNameTF = new FunctionField(0);
		conditionPanel.add("2.1.right.center.preferred.preferred", label1);
		conditionPanel.add("2.2.left.center.resizable.preferred", itemNameTF);

		ComponentService.createService(session);
		ComponentService.setLabelSize(conditionPanel, 60, 21);
		ComponentService.setComboboxSize(conditionPanel, 120, 21);
		ComponentService.setTextfieldSizs(conditionPanel, 120, 21);

		return conditionPanel;
	}

	@Override
	public void invokeOperation(ActionEvent e) {
		try {
			if (table.getSelectedComponents() != null && table.getSelectedComponents().length != 0) {
				TCComponent selectedComp = (TCComponent) table
						.getSelectedComponents()[0];
				if (selectedComp == null) {
					return;
				}
				
				if(field != null){
					field.setText(selectedComp.toString());
				}
				
				if(dialog != null){
					if(dialog instanceof CreateFolderDialog){
						((CreateFolderDialog) dialog).setSelectComp(selectedComp);
					}
				}
			} else{
				if(dialog != null){
					if(dialog instanceof CreateFolderDialog){
						((CreateFolderDialog) dialog).getInfoPanel().getModelCopyCK().setSelected(false);
						((CreateFolderDialog) dialog).setSelectComp(null);
						((CreateFolderDialog) dialog).getInfoPanel().getModelIDTF().setText("");
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	

	@Override
	public void cancelButtonClicked(ActionEvent e) {
		if(dialog != null){
			if(dialog instanceof CreateFolderDialog){
				((CreateFolderDialog) dialog).getInfoPanel().getModelCopyCK().setSelected(false);
				((CreateFolderDialog) dialog).setSelectComp(null);
				((CreateFolderDialog) dialog).getInfoPanel().getModelIDTF().setText("");
			}
		}
		super.cancelButtonClicked(e);
	}

	@Override
	public TCComponent[] search() {
		try {
			String[] entryValue = { "*" + itemNoTF.getText().toUpperCase() +"*", "*" + itemNameTF.getText() + "*",
					searchItemType };
			TCComponent[] qryResult = CustomUtil.queryComponent(SYMCClass.ITEMS_SEARCH, SYMCClass.ITEMS_IDNAMETYPE_SEARCH_KEY,
					entryValue);
			ArrayList tempList = new ArrayList();
			if (qryResult != null && qryResult.length != 0) {
				int qryResultSize = qryResult.length;
				for (int i = 0; i < qryResultSize; i++) {
					if (CustomUtil.isWorkingStatus(qryResult[i])) {
						tempList.add(qryResult[i]);
					}
				}
			}
			int tempListSize = tempList.size();
			components = new TCComponent[tempListSize];
			for (int i = 0; i < tempListSize; i++) {
				components[i] = (TCComponent) tempList.get(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return components;
	}

	@Override
	public void preSearchAction() {

	}

	@Override
	protected JPanel getUIPanel() {
		return conditionPanel;
	}

	@Override
	public boolean confirmCheck() {
		return true;
	}
}