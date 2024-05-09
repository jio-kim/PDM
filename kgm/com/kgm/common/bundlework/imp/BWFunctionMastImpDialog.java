/**
 * FMP Excel Import
 * [SR140725-012][20141111][jclee] ��� BOM Part Excel Upload �ű� �߰�
 * 2014.11.11
 * jclee
 */
package com.kgm.common.bundlework.imp;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.commands.partmaster.validator.FncMastPartValidator;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class BWFunctionMastImpDialog extends BWXLSImpDialog {

	TCComponentFolder targetFolder;

	/**
	 * Constructor
	 * @param parent
	 * @param style
	 * @param cls
	 */
	public BWFunctionMastImpDialog(Shell parent, int style, Class<?> cls) {
		super(parent, style, cls);
	}

	/**
	 * Constructor
	 * @param parent
	 * @param style
	 */
	public BWFunctionMastImpDialog(Shell parent, int style) {
		super(parent, style, BWFunctionMastImpDialog.class);
		try {
			// Target Folder ��������, �������� �ʴ� ��� NewStuff Folder�� ����
			InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();

			if (comps.length > 0) {

				TCComponent comp = (TCComponent) comps[0];
				if (comp instanceof TCComponentFolder) {
					targetFolder = (TCComponentFolder) comp;
				} else {
					TCSession tcSession = (TCSession) AIFUtility.getSessionManager().getDefaultSession();
					targetFolder = tcSession.getUser().getNewStuffFolder();
				}
			} else {
				TCSession tcSession = (TCSession) AIFUtility.getSessionManager().getDefaultSession();
				targetFolder = tcSession.getUser().getNewStuffFolder();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dialog Open
	 */
	@Override
	public void dialogOpen() {
		super.dialogOpen();
	}

	/**
	 * Function Part Validation Check
	 */
	@Override
	public void validatePost() throws Exception {
		// Attr/Lov Mapping Array
		String[][] szLovNames = { { "item_id", "Part No" }, 
								  { "object_name", "Part Name" }, 
								  { "s7_PROJECT_CODE", "S7_PROJECT_CODE" }, 
								  { "s7_MATURITY", "S7_MATURITY" }, 
								  { "s7_GMODEL_CODE", "S7_GMODEL_CODE" }, 
								  { "s7_FUNCTION_TYPE", "S7_FUNCTION_TYPE" } };
		FncMastPartValidator validator = new FncMastPartValidator(szLovNames);
		TreeItem[] szTopItems = super.tree.getItems();
		for (int i = 0; i < szTopItems.length; i++) {
			// Top TreeItem
			ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
			this.validationFunctionMastPart(topTreeItem, validator);
		}

	}

	/**
	 * Function Part ���� Validation Check
	 * @param treeItem
	 * @param validator
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void validationFunctionMastPart(ManualTreeItem treeItem, FncMastPartValidator validator) {
		HashMap modelMap = treeItem.getModelMap();
		HashMap<String, Object> revionMap = (HashMap<String, Object>) modelMap.get(CLASS_TYPE_REVISION);

		revionMap.put("item_id", treeItem.getItemID());
		revionMap.put("object_name", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "object_name"));
		revionMap.put("s7_PROJECT_CODE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_PROJECT_CODE"));
		revionMap.put("s7_MATURITY", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_MATURITY"));
		revionMap.put("s7_GMODEL_CODE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_GMODEL_CODE"));
		revionMap.put("s7_FUNCTION_TYPE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_FUNCTION_TYPE"));

		try {
			String strMessage = validator.validate(revionMap, FncMastPartValidator.TYPE_VALID_CREATE);
			
			if (!CustomUtil.isEmpty(strMessage))
				treeItem.setStatus(STATUS_ERROR, strMessage.replaceAll("\\n", ", "));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �۾� �Ϸ��� ����/������ ItemRevision�� Target Folder�� ÷��
	 */
	@Override
	public void executePost() throws Exception {

		TCComponentItemRevision[] revSet = tcItemRevSet.values().toArray(new TCComponentItemRevision[tcItemRevSet.size()]);

		for (int i = 0; i < revSet.length; i++) {
			if (targetFolder != null) {
				try {
					targetFolder.add("contents", revSet[i].getItem());
				} catch (Exception e) {
					// �ߺ��� ��� Error �߻�
				}
			}
		}
	}
}