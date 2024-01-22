/**
 * 
 */
package com.symc.plm.me.sdv.operation.body;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class ApplyAlternativeToProductionBOPInitOperation extends AbstractSDVInitOperation {
	private Registry registry = Registry.getRegistry(ApplyAlternativeToProductionBOPInitOperation.class);

	/**
	 * 
	 */
	public ApplyAlternativeToProductionBOPInitOperation() {
	}

	/**
	 * @param dataMap
	 */
	public ApplyAlternativeToProductionBOPInitOperation(IDataMap dataMap) {
		super(dataMap);
	}

	/* (non-Javadoc)
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 */
	@Override
	public void executeOperation() throws Exception {
		try
		{
			if (! (AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication))
			{
				// MPPApplication Check
				throw new Exception(registry.getString("WorkInMPPApplication.MESSAGE", "MPP Application에서 작업해야 합니다."));
			}
			InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
			if (selectedTargets == null || selectedTargets.length != 1)
				throw new Exception(registry.getString("SelectOneTargetAltBOP.MESSAGE", "대상 Alternative BOP를 하나만 선택해 주세요."));

			if ((! (selectedTargets[0] instanceof TCComponentBOMLine)) ||
					((! ((TCComponentBOMLine) selectedTargets[0]).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)) &&
					(! ((TCComponentBOMLine) selectedTargets[0]).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) &&
					(! ((TCComponentBOMLine) selectedTargets[0]).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))))
			{
				throw new Exception(registry.getString("SelectionAltBOPInvalid.MESSAGE", "양산 적용할 대상 Alternative BOP(Shop, Line)를 선택해 주세요."));
			}

			if ((! (selectedTargets[0] instanceof TCComponentBOPLine)) ||
				((! ((TCComponentBOPLine) selectedTargets[0]).getItemRevision().getLogicalProperty(SDVPropertyConstant.SHOP_REV_IS_ALTBOP))))
			{
				throw new Exception(registry.getString("SelectTargetAltBOPItem.MESSAGE", "양산 적용할 대상 Alternative  BOP(Shop, Line)를 선택해 주세요."));
			}

			RawDataMap targetDataMap = new RawDataMap();
			targetDataMap.put("ApplyAltToTargetItem", selectedTargets[0], IData.OBJECT_FIELD);

			DataSet targetDataset = new DataSet();
			targetDataset.addDataMap("ApplyAltToTargetItem", targetDataMap);

			setData(targetDataset);
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

}
