/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 *[SR141208-036] [20150203] shcho, 용접점의 추가 삭제에 의해 일반공법 변경 사유 발생 시 MEP와 MEW용 MECO를 별개로 발행해야만 하는 것을 하나의 MEP에서 할 수 있도록 변경
 *
 */
public class CheckBodyItemReviseTargetSDVValidator implements ISDVValidator {
	private Registry reviseRegistry = Registry.getRegistry(CheckBodyItemReviseTargetSDVValidator.class);

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map, java.lang.Object)
	 */
	@Override
	public void validate(String commandId, Map<String, Object> parameter,
			Object applicationCtx) throws SDVException {
        InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
        ArrayList<InterfaceAIFComponent> tableList = new ArrayList<InterfaceAIFComponent>();
        ArrayList<String> skipMECOCheckList = new ArrayList<String>();
        String firstItemType = null;
        skipMECOCheckList.addAll(Arrays.asList(reviseRegistry.getStringArray("NeedToNotMECO.TYPE")));
		ArrayList<String> targetMECOList = new ArrayList<String>();

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

        		List<String> bodyTypeList = Arrays.asList(reviseRegistry.getStringArray("Property.BodyBOP.Type"));
				if ((! bodyTypeList.contains(latestRevision.getProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE).toUpperCase()) &&
					    ! latestRevision.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) &&
					    ! latestRevision.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM)) ||
					latestRevision.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) ||
					latestRevision.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
				{
					throw new Exception(reviseRegistry.getString("SelectBodyItemTarget.MESSAGE", "Please select body bop item."));
				}

				if (firstItemType == null)
					firstItemType = latestRevision.getItem().getType();
				
				ArrayList<String> itemTypeHierarchiesList = new ArrayList<String>();
				itemTypeHierarchiesList.addAll(Arrays.asList(latestRevision.getItem().getClassNameHierarchy()));
				itemTypeHierarchiesList.add(latestRevision.getItem().getType());
				
				if (CollectionUtils.intersection(itemTypeHierarchiesList, skipMECOCheckList).size() > 0)
				{
					if (! firstItemType.equals(latestRevision.getItem().getType()) && skipMECOCheckList.contains(latestRevision.getItem().getType()))
					{
						throw new Exception(reviseRegistry.getString("SelectPlantItemType.MESSAGE", "Please select only Plant Item Type."));
					}
				}
				//[SR141208-036] [20150203] shcho, 용접점의 추가 삭제에 의해 일반공법 변경 사유 발생 시 MEP와 MEW용 MECO를 별개로 발행해야만 하는 것을 하나의 MEP에서 할 수 있도록 변경
				/*
				if (! firstItemType.equals(latestRevision.getItem().getType()) && latestRevision.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
				{
					throw new Exception(reviseRegistry.getString("SelectWeldOPItemType.MESSAGE", "Please select only BODY Weld Operation."));
				}
				else
				*/ 
				if (latestRevision.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
				{
					TCComponent targetOP = latestRevision.getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
					if (! SYMTcUtil.isReleased(targetOP))
					{
						String targetMECO = targetOP.getProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
						if (! targetMECOList.contains(targetMECO))
							targetMECOList.add(targetMECO);
					}
					else
						if (! targetMECOList.contains("MEW MECO"))
							targetMECOList.add("MEW MECO");
				}
			}
			
			if (targetMECOList.size() > 0 && targetMECOList.size() != 1)
			{
				throw new Exception(reviseRegistry.getString("SelectWeldOPSameMECOType.MESSAGE", "Please select Weld Operation for same type MECO."));
			}
		}
		catch (Exception ex)
		{
			throw new ValidateSDVException(ex.getMessage());
		}
	}

}
