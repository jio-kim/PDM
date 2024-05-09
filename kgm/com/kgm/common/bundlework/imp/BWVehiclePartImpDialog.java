/**
 * Part/BOM 속성 일괄 Upload Dialog
 * 상위클래스(BWXLSImpDialog)와  상이한 기능은 없음( 차후 추가 개발시 세부 기능을 Override하여 구현 하여야 함)
 * 작업Option은 bundlework_locale_ko_KR.properties에 정의 되어 있음
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
			// Target Folder 가져오기, 존재하지 않는 경우 NewStuff Folder로 지정
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
	 * Vehicle Part 개별 Validation Check
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

			// DPV시 사용하는 OLD Item ID
			String strOldItemID = treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "old_item_id");
			String strOldItemRev = treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "old_item_revision_id");
			// 신규 Item ID
			String strNewItemID = treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "item_id");

			int nValidType = 0;
			// Old Item ID가 없는 경우 Modify 기준으로 Validation
			if (CustomUtil.isEmpty(strOldItemID))
				nValidType = VehiclePartValidator.TYPE_VALID_MODIFY;
			else
				nValidType = VehiclePartValidator.TYPE_VALID_CREATE;

			String strMessage = validator.validate(revionMap, nValidType);
			if (!CustomUtil.isEmpty(strMessage))
				treeItem.setStatus(STATUS_ERROR, strMessage.replaceAll("\\n", ", "));

			if (!CustomUtil.isEmpty(strMessage))
			{
				// OLD/New Item ID가 동일한 경우 Error
				if (strOldItemID.equals(strNewItemID))
				{
					treeItem.setStatus(STATUS_ERROR, "New, Old Item Has Same ID");
				}

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// 재귀 호출
		TreeItem[] childItems = treeItem.getItems();
		for (int i = 0; i < childItems.length; i++)
		{
			ManualTreeItem cItem = (ManualTreeItem) childItems[i];
			this.validationVehiclePart(cItem, validator);

		}
	}

	/**
	 * 작업 완료후 생성/수정된 ItemRevision을 Target Folder에 첨부
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
					// 중복된 경우 Error 발생
				}
			}
		}

	}

}
