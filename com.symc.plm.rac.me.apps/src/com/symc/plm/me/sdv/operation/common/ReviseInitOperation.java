/**
 * 
 */
package com.symc.plm.me.sdv.operation.common;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.collections.CollectionUtils;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 *[SR141208-036] [20150203] shcho, 용접점의 추가 삭제에 의해 일반공법 변경 사유 발생 시 MEP와 MEW용 MECO를 별개로 발행해야만 하는 것을 하나의 MEP에서 할 수 있도록 변경
 *
 */
public class ReviseInitOperation extends AbstractSDVInitOperation {

	/**
	 * 
	 */
	public ReviseInitOperation() {
	}

	/**
	 * @param dataMap
	 */
	public ReviseInitOperation(IDataMap dataMap) {
		super(dataMap);
	}

	/* (non-Javadoc)
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 */
	@Override
	public void executeOperation() throws Exception {
        try
        {
        	Registry registry = Registry.getRegistry(ReviseInitOperation.class);
	        AbstractAIFUIApplication currentApplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
	        InterfaceAIFComponent []targetComponents = currentApplication.getTargetComponents();
	        ArrayList<InterfaceAIFComponent> tableList = new ArrayList<InterfaceAIFComponent>();
	        ArrayList<String> skipMECOCheckList = new ArrayList<String>();
	        String firstItemType = null;
	        skipMECOCheckList.addAll(Arrays.asList(registry.getStringArray("NeedToNotMECO.TYPE")));
    		ArrayList<String> targetMECOList = new ArrayList<String>();
    		ArrayList<Object> parentMECOList = new ArrayList<Object>();

	        for (InterfaceAIFComponent targetComponent : targetComponents)
	        {
	        	TCComponentItemRevision latestRevision = null;

	        	if (targetComponent instanceof TCComponentItem)
	        		latestRevision = ((TCComponentItem) targetComponent).getLatestItemRevision();
	        	else if (targetComponent instanceof TCComponentItemRevision)
	        		latestRevision = (TCComponentItemRevision) targetComponent;
	        	else if (targetComponent instanceof TCComponentBOMLine)
	        	{
	        		latestRevision = ((TCComponentBOMLine) targetComponent).getItemRevision();
	        		TCComponentItemRevision parentRevision = ((TCComponentBOMLine) targetComponent).parent() == null ? latestRevision : ((TCComponentBOMLine) targetComponent).parent().getItemRevision();
	        		
	        		// [SR150130-037][2015.02.06][jclee] 상위 Revise Owner와 현재 접속자가 같을 경우에만 MECO를 Parameter로 넘기도록 수정.
	        		TCComponentUser userOwner= (TCComponentUser) parentRevision.getReferenceProperty("owning_user");
	        		String sOwner = userOwner.getUserId();
	        		
	        		TCSession session = (TCSession)getSession();
	        		String sCurrentUser = session.getUser().getUserId();
	        		
	        		if (sOwner.equals(sCurrentUser)) {
	        			if (! CustomUtil.isReleased(parentRevision))
	        			{
	        				TCComponent parentMECO = parentRevision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
	        				if (! parentMECOList.contains(parentMECO))
	        					parentMECOList.add(parentMECO);
	        			}
					}
	        		
	        	}

	        	if (latestRevision != null && latestRevision != latestRevision.getItem().getLatestItemRevision())
	        		latestRevision = latestRevision.getItem().getLatestItemRevision();

	        	if (latestRevision != null && CustomUtil.isReleased(latestRevision))
	        		tableList.add(targetComponent);

	        	if (firstItemType == null)
	        		firstItemType = latestRevision.getItem().getType();

	        	ArrayList<String> itemTypeHierarchiesList = new ArrayList<String>();
	        	itemTypeHierarchiesList.addAll(Arrays.asList(latestRevision.getItem().getClassNameHierarchy()));
	        	itemTypeHierarchiesList.add(latestRevision.getItem().getType());

	        	if (CollectionUtils.intersection(itemTypeHierarchiesList, skipMECOCheckList).size() > 0)
	        	{
	        		if (! firstItemType.equals(latestRevision.getItem().getType()) && skipMECOCheckList.contains(latestRevision.getItem().getType()))
	        		{
	        			throw new Exception(registry.getString("SelectPlantItemType.MESSAGE", "Please select only Plant Item Type."));
	        		}
	        	}

	        	// [SR141208-036] [20150203] shcho, 용접점의 추가 삭제에 의해 일반공법 변경 사유 발생 시 MEP와 MEW용 MECO를 별개로 발행해야만 하는 것을 하나의 MEP에서 할 수 있도록 변경
	        	/*
	        	if (! firstItemType.equals(latestRevision.getItem().getType()) && latestRevision.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
	        	{
	        		throw new Exception(registry.getString("SelectWeldOPItemType.MESSAGE", "Please select only BODY Weld Operation."));
	        	}
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
    			throw new Exception(registry.getString("SelectWeldOPSameMECOType.MESSAGE", "Please select Weld Operation for same type MECO."));
    		}

	        if (tableList.size() > 0)
	        {
	        	DataSet targetDataset = new DataSet();

	        	RawDataMap targetDataMap = new RawDataMap();
	            targetDataMap.put("TargetObjects", tableList, IData.LIST_FIELD);

	            if ((parentMECOList.size() == 1 && targetMECOList.size() == 0) || (parentMECOList.size() == 1 && targetMECOList.size() == 1 && parentMECOList.get(0).equals(targetMECOList.get(0))))
	            {
		            RawDataMap mecoDataMap = new RawDataMap();
		            mecoDataMap.put("MECOObject", parentMECOList.get(0), IData.OBJECT_FIELD);

		            targetDataset.addDataMap("MECOObject", mecoDataMap);
	            }

	            targetDataset.addDataMap("TargetObjects", targetDataMap);

	            setData(targetDataset);
	        }
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
	}
}
