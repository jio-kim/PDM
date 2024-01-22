/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class CheckUpperItemReviseTargetSDVValidator implements ISDVValidator {
	private Registry reviseRegistry = Registry.getRegistry(CheckUpperItemReviseTargetSDVValidator.class);

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map, java.lang.Object)
	 */
	@Override
	public void validate(String commandId, Map<String, Object> parameter,
			Object applicationCtx) throws SDVException {
        InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
        ArrayList<InterfaceAIFComponent> tableList = new ArrayList<InterfaceAIFComponent>();
//        ArrayList<String> skipMECOCheckList = new ArrayList<String>();
//        String firstItemType = null;
//        skipMECOCheckList.addAll(Arrays.asList(reviseRegistry.getStringArray("NeedToNotMECO.TYPE")));

		try
		{
			for (InterfaceAIFComponent targetComponent : selectedTargets)
			{
				TCComponentItemRevision latestRevision = null;
				
				if (targetComponent instanceof TCComponentItem)
					latestRevision = ((TCComponentItem) targetComponent).getLatestItemRevision();
				else if (targetComponent instanceof TCComponentItemRevision)
					latestRevision = (TCComponentItemRevision) targetComponent;
				else if (targetComponent instanceof TCComponentBOMLine)
					latestRevision = ((TCComponentBOMLine) targetComponent).getItemRevision();
				
				if (latestRevision != null && latestRevision != latestRevision.getItem().getLatestItemRevision())
					latestRevision = latestRevision.getItem().getLatestItemRevision();
				
				if (latestRevision != null && CustomUtil.isReleased(latestRevision))
					tableList.add(targetComponent);

				String itemType = latestRevision.getItem().getType();
				if (! (itemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)))
				{
					throw new Exception(reviseRegistry.getString("SelectUpperItemTarget.MESSAGE", "Please select Upper BOP Item."));
				}

//				if (firstItemType == null)
//					firstItemType = latestRevision.getItem().getType();
				
//				ArrayList<String> itemTypeHierarchiesList = new ArrayList<String>();
//				itemTypeHierarchiesList.addAll(Arrays.asList(latestRevision.getItem().getClassNameHierarchy()));
//				itemTypeHierarchiesList.add(latestRevision.getItem().getType());
				
//				if (CollectionUtils.intersection(itemTypeHierarchiesList, skipMECOCheckList).size() > 0)
//				{
//					if (! firstItemType.equals(latestRevision.getItem().getType()) && skipMECOCheckList.contains(latestRevision.getItem().getType()))
//					{
//						throw new Exception(reviseRegistry.getString("SelectPlantItemType.MESSAGE", "Please select only Plant Item Type."));
//					}
//				}
			}
		}
		catch (Exception ex)
		{
			throw new ValidateSDVException(ex.getMessage());
		}
	}

}
