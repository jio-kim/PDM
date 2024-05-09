/**
 * Product Excel Import
 * [SR140725-012][20141111][jclee] 상부 BOM Part Excel Upload 신규 추가
 * 2014.11.11
 * jclee
 */
package com.kgm.common.bundlework.imp;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.commands.partmaster.validator.ProductPartValidator;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class BWProductImpDialog extends BWXLSImpDialog {

	TCComponentFolder targetFolder;

	/**
	 * Constructor
	 * @param parent
	 * @param style
	 * @param cls
	 */
	public BWProductImpDialog(Shell parent, int style, Class<?> cls) {
		super(parent, style, cls);
	}

	/**
	 * Constructor
	 * @param parent
	 * @param style
	 */
	public BWProductImpDialog(Shell parent, int style) {
		super(parent, style, BWProductImpDialog.class);
		try {
			// Target Folder 가져오기, 존재하지 않는 경우 NewStuff Folder로 지정
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
	 * Product Part Validation Check
	 */
	@Override
	public void validatePost() throws Exception {
		// Attr/Lov Mapping Array
		String[][] szLovNames = { { "item_id", "Part No" }, 
								  { "object_name", "Part Name" }, 
								  { "s7_PROJECT_CODE", "S7_PROJECT_CODE" }, 
								  { "s7_MATURITY", "S7_MATURITY" }, 
								  { "s7_GMODEL_CODE", "S7_GMODEL_CODE" }, 
								  { "s7_PRODUCT_TYPE", "S7_PRODUCT_TYPE" } };
		ProductPartValidator validator = new ProductPartValidator(szLovNames);
		TreeItem[] szTopItems = super.tree.getItems();
		for (int i = 0; i < szTopItems.length; i++) {
			// Top TreeItem
			ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
			this.validationProductPart(topTreeItem, validator);
		}

	}

	/**
	 * Product Part 개별 Validation Check
	 * @param treeItem
	 * @param validator
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void validationProductPart(ManualTreeItem treeItem, ProductPartValidator validator) {
		HashMap modelMap = treeItem.getModelMap();
		HashMap<String, Object> revionMap = (HashMap<String, Object>) modelMap.get(CLASS_TYPE_REVISION);

		revionMap.put("item_id", treeItem.getItemID());
		revionMap.put("object_name", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "object_name"));
		revionMap.put("s7_PROJECT_CODE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_PROJECT_CODE"));
		revionMap.put("s7_MATURITY", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_MATURITY"));
		revionMap.put("s7_GMODEL_CODE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_GMODEL_CODE"));
		revionMap.put("s7_PRODUCT_TYPE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_PRODUCT_TYPE"));

		try {
			String strMessage = validator.validate(revionMap, ProductPartValidator.TYPE_VALID_CREATE);
			
			if (!CustomUtil.isEmpty(strMessage))
				treeItem.setStatus(STATUS_ERROR, strMessage.replaceAll("\\n", ", "));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 작업 완료후 생성/수정된 ItemRevision을 Target Folder에 첨부
	 */
	@Override
	public void executePost() throws Exception {

		TCComponentItemRevision[] revSet = tcItemRevSet.values().toArray(new TCComponentItemRevision[tcItemRevSet.size()]);

		for (int i = 0; i < revSet.length; i++) {
			if (targetFolder != null) {
				try {
					targetFolder.add("contents", revSet[i].getItem());
				} catch (Exception e) {
					// 중복된 경우 Error 발생
				}
			}
		}
	}
}