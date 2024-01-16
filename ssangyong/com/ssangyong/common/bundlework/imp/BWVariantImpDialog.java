/**
 * Product Excel Import
 * [SR140725-012][20141111][jclee] 상부 BOM Part Excel Upload 신규 추가
 * 2014.11.11
 * jclee
 */
package com.ssangyong.common.bundlework.imp;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.ssangyong.commands.partmaster.validator.VariantPartValidator;
import com.ssangyong.common.bundlework.BWXLSImpDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class BWVariantImpDialog extends BWXLSImpDialog {

	TCComponentFolder targetFolder;

	/**
	 * Constructor
	 * @param parent
	 * @param style
	 * @param cls
	 */
	public BWVariantImpDialog(Shell parent, int style, Class<?> cls) {
		super(parent, style, cls);
	}

	/**
	 * Constructor
	 * @param parent
	 * @param style
	 */
	public BWVariantImpDialog(Shell parent, int style) {
		super(parent, style, BWVariantImpDialog.class);
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
	 * Variant Part Validation Check
	 */
	@Override
	public void validatePost() throws Exception {
		// Attr/Lov Mapping Array
		String[][] szLovNames = { { "item_id", "item_id" }, 
								  { "object_name", "object_name" }, 
								  { "s7_PROJECT_CODE", "S7_PROJECT_CODE" }, 
								  { "s7_MATURITY", "S7_MATURITY" }, 
								  { "s7_GMODEL_CODE", "S7_GMODEL_CODE" }, 
								  { "s7_VARIANT_TYPE", "S7_VARIANT_TYPE" }, 
								  { "s7_ENGINE_NO", "S7_ENGINE_NO" }, 
								  { "s7_LOCATION", "S7_LOCATION" }, 
								  { "s7_BODY_TYPE", "S7_BODY_TYPE" }, 
								  { "s7_SEATER", "S7_SEATER" }, 
								  { "s7_TRIM_LEVEL", "S7_TRIM_LEVEL" } };
		VariantPartValidator validator = new VariantPartValidator(szLovNames);
		TreeItem[] szTopItems = super.tree.getItems();
		for (int i = 0; i < szTopItems.length; i++) {
			// Top TreeItem
			ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
			this.validationVariantPart(topTreeItem, validator);
		}
	}

	/**
	 * Variant Part 개별 Validation Check
	 * @param treeItem
	 * @param validator
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void validationVariantPart(ManualTreeItem treeItem, VariantPartValidator validator) {
		HashMap modelMap = treeItem.getModelMap();
		HashMap<String, Object> revionMap = (HashMap<String, Object>) modelMap.get(CLASS_TYPE_REVISION);
		
		revionMap.put("item_id", treeItem.getItemID());
		revionMap.put("object_name", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "object_name"));
		revionMap.put("s7_PROJECT_CODE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_PROJECT_CODE"));
		revionMap.put("s7_MATURITY", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_MATURITY"));
		revionMap.put("s7_GMODEL_CODE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_GMODEL_CODE"));
		revionMap.put("s7_VARIANT_TYPE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_VARIANT_TYPE"));
		revionMap.put("s7_ENGINE_NO", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_ENGINE_NO"));
		revionMap.put("s7_LOCATION", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_LOCATION"));
		revionMap.put("s7_BODY_TYPE", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_BODY_TYPE"));
		revionMap.put("s7_SEATER", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_SEATER"));
		revionMap.put("s7_TRIM_LEVEL", treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_TRIM_LEVEL"));

		try {
			String strMessage = validator.validate(revionMap, VariantPartValidator.TYPE_VALID_CREATE);
			
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