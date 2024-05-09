/**
 * Part/BOM �Ӽ� �ϰ� Upload Dialog
 * ����Ŭ����(BWXLSImpDialog)��  ������ ����� ����( ���� �߰� ���߽� ���� ����� Override�Ͽ� ���� �Ͽ��� ��)
 * �۾�Option�� bundlework_locale_ko_KR.properties�� ���� �Ǿ� ����
 */
package com.kgm.common.bundlework.imp;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.commands.partmaster.validator.VehiclePartValidator;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Vehicle Part Excel Upload Dialog
 */
@SuppressWarnings("unused")
public class BWVehiclePartImpDialog extends BWXLSImpDialog
{

	TCComponentFolder targetFolder;

	public BWVehiclePartImpDialog(Shell parent, int style, Class<?> cls)
	{
		super(parent, style, cls);
	}

	public BWVehiclePartImpDialog(Shell parent, int style)
	{
		super(parent, style, BWVehiclePartImpDialog.class);
		try
		{
			// Target Folder ��������, �������� �ʴ� ��� NewStuff Folder�� ����
			InterfaceAIFComponent[] comps = AIFUtility.getCurrentApplication().getTargetComponents();

			if (comps.length > 0)
			{

				TCComponent comp = (TCComponent) comps[0];
				if (comp instanceof TCComponentFolder)
				{
					targetFolder = (TCComponentFolder) comp;

				} else
				{

					TCSession tcSession = (TCSession) AIFUtility.getSessionManager().getDefaultSession();
					targetFolder = tcSession.getUser().getNewStuffFolder();

				}
			} 
			else
			{
				TCSession tcSession = (TCSession) AIFUtility.getSessionManager().getDefaultSession();
				targetFolder = tcSession.getUser().getNewStuffFolder();
			}

		} catch (TCException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void dialogOpen()
	{
		super.dialogOpen();
		// super.enableOptionButton();
	}

	/**
	 * Vehicle Part Validation Check
	 * 
	 */
	@Override
	public void validatePost() throws Exception
	{

		// Attr/Lov Mapping Array
		String[][] szLovNames = { { "s7_PART_TYPE", "S7_PART_ORIGIN" }, { "s7_PROJECT_CODE", "S7_PROJECT_CODE" }, { "s7_STAGE", "S7_STAGE" }, { "uom_tag", "Unit of Measures" },
				{ "s7_BUDGET_CODE", "s7_SYSTEM_CODE" }, { "s7_DRW_STAT", "s7_DRW_STAT" }, { "s7_DRW_SIZE", "s7_DRW_SIZE" }, { "s7_REGULAR_PART", "s7_REGULAR_PART" },
				{ "s7_REGULATION", "S7_CATEGORY" }, { "s7_COLOR", "s7_COLOR" }, { "s7_COLOR_ID", "S7_COLOR_SECTION_ID" }, { "s7_RESPONSIBILITY", "S7_RESPONSIBILITY" } };
		VehiclePartValidator validator = new VehiclePartValidator(szLovNames);
		TreeItem[] szTopItems = super.tree.getItems();
		for (int i = 0; i < szTopItems.length; i++)
		{

			// Top TreeItem
			ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
			this.validationVehiclePart(topTreeItem, validator);
		}
		
		
	}

	/**
	 * Vehicle Part ���� Validation Check
	 * 
	 * @param treeItem
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void validationVehiclePart(ManualTreeItem treeItem, VehiclePartValidator validator)
	{
		HashMap modelMap = treeItem.getModelMap();
		HashMap<String, Object> revionMap = (HashMap<String, Object>) modelMap.get(CLASS_TYPE_REVISION);

		revionMap.put("item_id", treeItem.getItemID());
		revionMap.put("object_name", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "object_name"));
		revionMap.put("uom_tag", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "uom_tag"));

		try
		{

			// DPV�� ����ϴ� OLD Item ID
			String strOldItemID = treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "old_item_id");
			String strOldItemRev = treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "old_item_revision_id");
			// �ű� Item ID
			String strNewItemID = treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "item_id");

			int nValidType = 0;
			// Old Item ID�� ���� ��� Modify �������� Validation
			if (CustomUtil.isEmpty(strOldItemID))
				nValidType = VehiclePartValidator.TYPE_VALID_MODIFY;
			else
				nValidType = VehiclePartValidator.TYPE_VALID_CREATE;

			String strMessage = validator.validate(revionMap, nValidType);
			if (!CustomUtil.isEmpty(strMessage))
				treeItem.setStatus(STATUS_ERROR, strMessage.replaceAll("\\n", ", "));

			if (!CustomUtil.isEmpty(strMessage))
			{
				// OLD/New Item ID�� ������ ��� Error
				if (strOldItemID.equals(strNewItemID))
				{
					treeItem.setStatus(STATUS_ERROR, "New, Old Item Has Same ID");
				}

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// ��� ȣ��
		TreeItem[] childItems = treeItem.getItems();
		for (int i = 0; i < childItems.length; i++)
		{
			ManualTreeItem cItem = (ManualTreeItem) childItems[i];
			this.validationVehiclePart(cItem, validator);

		}
	}

	/**
	 * �۾� �Ϸ��� ����/������ ItemRevision�� Target Folder�� ÷��
	 */
	@Override
	public void executePost() throws Exception
	{

		TCComponentItemRevision[] revSet = tcItemRevSet.values().toArray(new TCComponentItemRevision[tcItemRevSet.size()]);

		for (int i = 0; i < revSet.length; i++)
		{
			if (targetFolder != null)
			{
				try
				{
					targetFolder.add("contents", revSet[i].getItem());
				}
				catch(Exception e)
				{
					// �ߺ��� ��� Error �߻�
				}
			}
		}

	}

}
