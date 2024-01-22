/**
 * 
 */
package com.symc.plm.me.sdv.operation.body;

import java.util.ArrayList;
import java.util.Arrays;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class CreateBodyShopInitOperation extends AbstractSDVInitOperation {
	private Registry registry = Registry.getRegistry(CreateBodyShopInitOperation.class);

	/**
	 * 
	 */
	public CreateBodyShopInitOperation() {
		super();
	}


	@Override
	public void executeOperation() throws Exception {
		try
		{
			MFGLegacyApplication currentApplication = (MFGLegacyApplication) AIFUtility.getCurrentApplication();

			if (! (currentApplication instanceof MFGLegacyApplication))
			{
				// MPPApplication Check
				throw new Exception(registry.getString("WorkInMPPApplication.MESSAGE", "MPP Application에서 작업해야 합니다."));
			}

			TCComponentBOMLine[] selectedLines = currentApplication.getSelectedBOMLines();
			if (selectedLines != null && selectedLines.length != 1)
			{
				throw new Exception(registry.getString("SelectOneTargetLine.MESSAGE", "Please select one target Line."));
			}
			if (! (selectedLines[0] instanceof TCComponentBOMLine) && ! (selectedLines[0].getItem().getType().equals(SDVTypeConstant.EBOM_MPRODUCT)))
			{
				throw new Exception(registry.getString("TargetItemInvalid.MESSAGE", "BOP Shop의 대상은 Mproduct 이어야 합니다."));
			}

			ArrayList<TCComponentBOMLine> selectedList = new ArrayList<TCComponentBOMLine>();
			selectedList.addAll(Arrays.asList(selectedLines));

			RawDataMap targetDataMap = new RawDataMap();
			targetDataMap.put(SDVTypeConstant.EBOM_MPRODUCT, selectedList, IData.LIST_FIELD);

			DataSet targetDataset = new DataSet();
			targetDataset.addDataMap(SDVTypeConstant.EBOM_MPRODUCT, targetDataMap);

			setData(targetDataset);
//			AbstractViewableTreeTable []treeTables = currentApplication.getViewableTreeTables();

//			ArrayList<TCComponentBOPLine> bopLineList = new ArrayList<TCComponentBOPLine>();
//			ArrayList<TCComponentBOMLine> bomLineList = new ArrayList<TCComponentBOMLine>();
//			ArrayList<TCComponentBOMLine> plantLineList = new ArrayList<TCComponentBOMLine>();
//			if (treeTables != null && treeTables.length > 0)
//			{
//				for (AbstractViewableTreeTable treeTable : treeTables)
//				{
//					TCComponentBOMLine topLine = treeTable.getBOMRoot();
//					if (topLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
//						bopLineList.add((TCComponentBOPLine) topLine);
//					else if (topLine.getItem().getType().equals(SDVTypeConstant.EBOM_MPRODUCT))
//						bomLineList.add(topLine);
//					else if (topLine.getItem().getType().equals(SDVTypeConstant.PLANT_SHOP_ITEM))
//						plantLineList.add(topLine);
//					else
//						continue;
//				}
//			}
//
//			if (bomLineList.size() > 1)
//			{
//				
//			}
//
//			if (plantLineList.size() > 1)
//			{
//				
//			}
//
//			if (bomLineList.size() == 0)
//				throw new Exception(registry.getString("LoadFirstMProduct.MESSAGE", "대상 MProduct을 로드해 주세요."));
//
//			if (! (bomLineList.get(0) instanceof TCComponentBOMLine) && ! (bomLineList.get(0).getItem().getType().equals(SDVTypeConstant.EBOM_MPRODUCT)))
//			{
//				throw new Exception(registry.getString("TargetItemInvalid.MESSAGE", "BOP Shop의 대상은 Mproduct 이어야 합니다."));
//			}
//
//			RawDataMap targetDataMap = new RawDataMap();
//			targetDataMap.put(SDVTypeConstant.EBOM_MPRODUCT, bomLineList, IData.LIST_FIELD);
//
//			DataSet targetDataset = new DataSet();
//			targetDataset.addDataMap(SDVTypeConstant.EBOM_MPRODUCT, targetDataMap);
//
//			RawDataMap plantDataMap = new RawDataMap();
//			plantDataMap.put(SDVTypeConstant.PLANT_SHOP_ITEM, plantLineList, IData.LIST_FIELD);
//
//			targetDataset.addDataMap(SDVTypeConstant.PLANT_SHOP_ITEM, plantDataMap);
//
//			setData(targetDataset);
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}
}
