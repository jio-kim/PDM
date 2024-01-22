package com.symc.plm.me.sdv.service.migration.work.peif;

import org.eclipse.swt.widgets.TreeItem;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.basic.ItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivityMasterData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivitySubData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EndItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EquipmentData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.LineItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.SubsidiaryData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ToolData;
import com.teamcenter.rac.eintegrator.ExternalDsAdapter.TCDsAdapter;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class NEWPEImportDataCreator {

	NewPEIFExecution peIFExecution;

	IFExecutOperationItemData ifExecutOperationItemData;
	IFExecutEndItemData ifExecutEndItemData;
	IFExecutSubsidiaryData ifExecutSubsidiaryData;
	IFExecutEquipmentData ifExecutEquipmentData;
	IFExecutToolData ifExecutToolData;
	IFExecutActivityMasterData ifExecutActivityMasterData;

	/**
	 * 
	 * @param processWindow
	 * @param productWindow
	 * @param mecoItemRev
	 * @param itemLineData
	 * @param peImportDataReaderUtil
	 * @param peIFMigrationViewControll
	 */
	public NEWPEImportDataCreator( NewPEIFExecution peIFExecution ) {
		this.peIFExecution = peIFExecution;
//		this.processWindow = peIFExecution.getProcessWindow();
//		this.productWindow = peIFExecution.getProductWindow();
//		this.mecoItemRev = peIFExecution.getMecoRevision();
//		this.lineItemData = peIFExecution.getItemLineData();
//		this.peImportDataReaderUtil = peIFExecution.getPeImportDataReaderUtil();
//		this.peIFMigrationViewControll = peIFExecution.getPeIFMigrationViewControll();
//		this.mecoNo = peIFExecution.getMecoNo();
	}
	
	public boolean isChangeInterfaceTarget(){
	
		LineItemData lineItemData = peIFExecution.getItemLineData();
		boolean haveChildeRevise = lineItemData.getChildNodeRevisedFlag();
		boolean haveChildeAdded = lineItemData.getChildNodeAddedFlag();
		boolean haveChildeRemoved = lineItemData.getChildNodeRemovedFlag();
		boolean haveChildeReplaced = lineItemData.getChildNodeReplacedFlag();
		
		System.out.println("haveChildeRevise = "+haveChildeRevise);
		System.out.println("haveChildeAdded = "+haveChildeAdded);
		System.out.println("haveChildeRemoved = "+haveChildeRemoved);
		System.out.println("haveChildeReplaced = "+haveChildeReplaced);
		
		boolean isChangeTarget = false;
		if(haveChildeRevise || haveChildeAdded || haveChildeRemoved || haveChildeReplaced){
			isChangeTarget = true;
		}
		
		return isChangeTarget;
	}
	
	/**
	 * 실제 Inteface를 실행 하기전에 Line이 개정되어 Interface 가능한 상태인지 Check 하는 함수.
	 * @return
	 */
	public boolean isLineHaveChangeAccessRight(){
		
		boolean isChangeAble = false;
		
		LineItemData lineItemData = peIFExecution.getItemLineData();
		
		TCComponentBOPLine lineBOPLine = (TCComponentBOPLine)lineItemData.getBopBomLine();
		
		System.out.println("lineBOPLine = "+lineBOPLine);
		
		if(lineBOPLine!=null){
			
			boolean isReleased = IFExecutDefault.isReleased(lineBOPLine);
			System.out.println("isReleased = "+isReleased);	
			if(isReleased==true){
				return isChangeAble;
			}
			
			boolean haveWriteAccess = IFExecutDefault.haveWriteAccessRight(lineBOPLine);
			System.out.println("haveWriteAccess = "+haveWriteAccess);	
			if(haveWriteAccess==false){
				return isChangeAble;
			}

			boolean isSameWithTargetMeco = false;
			TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision)peIFExecution.getMecoRevision();
			TCComponentChangeItemRevision[] referencedChangeRevisions = IFExecutDefault.getReferencedChangeItemRevision(lineBOPLine);
			if(referencedChangeRevisions!=null){
				for (int i = 0; referencedChangeRevisions!=null && i < referencedChangeRevisions.length; i++) {
					if(referencedChangeRevisions[i]!=null && referencedChangeRevisions[i].equals(changeRevision)==true){
						isSameWithTargetMeco = true;
						break;
					}
				}
			}else{
				try {
					TCComponentItemRevision lineItemRevision = lineBOPLine.getItemRevision();
					TCComponentChangeItemRevision lineMECORev = 
							(TCComponentChangeItemRevision)lineItemRevision.getReferenceProperty(SDVPropertyConstant.LINE_REV_MECO_NO);
					if(lineMECORev!=null && lineMECORev.equals(changeRevision)==true){
						isSameWithTargetMeco = true;
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			System.out.println("isSameWithTargetMeco = "+isSameWithTargetMeco);
			
			if(isSameWithTargetMeco==true && haveWriteAccess==true && isReleased==false){
				isChangeAble = true;
			}
		}
		
		return isChangeAble;
	}

	public void expandAndUpdateLineItemData() {

		TreeItem[] childNodeTreeItems = peIFExecution.getItemLineData().getItems();

		OperationItemData operationItemData = null;

		ifExecutOperationItemData = new IFExecutOperationItemData(peIFExecution);
		ifExecutEndItemData = new IFExecutEndItemData(peIFExecution);
		ifExecutSubsidiaryData = new IFExecutSubsidiaryData(peIFExecution);
		ifExecutEquipmentData = new IFExecutEquipmentData(peIFExecution);
		ifExecutToolData = new IFExecutToolData(peIFExecution);
		ifExecutActivityMasterData = new IFExecutActivityMasterData(peIFExecution);

		for (int i = 0; childNodeTreeItems != null
				&& i < childNodeTreeItems.length; i++) {
			
			this.peIFExecution.redrawUI();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			TreeItem currentTreeItem = childNodeTreeItems[i];
			
			if (currentTreeItem == null) {
				peIFExecution.waite();
				continue;
			}

			if (currentTreeItem instanceof OperationItemData) {
				operationItemData = (OperationItemData) currentTreeItem;
				
				peIFExecution.getPeIFMigrationViewControll().writeLogTextLine(
						"Execut : "+operationItemData.getItemId());
				
				if(operationItemData.isHaveMajorError()==true){
					peIFExecution.writeLogTextLine(operationItemData.getItemId() +" have Major Exception -> Exception out");
				}else{
					expandOperatoinItemData((OperationItemData) currentTreeItem);
				}
				currentTreeItem.setExpanded(false);
			}
			
			peIFExecution.waite();
			System.gc();
		}

	}

	/**
	 * Operation과 Operation을 구성하는 Child Node 들에 대한 Update 및 추가/제거등의 변경 사항을 반영하는
	 * 함수
	 * 
	 * @param operationItemData
	 * @return
	 */
	private OperationItemData expandOperatoinItemData(
			OperationItemData operationItemData) {

		boolean isOk = ifExecutOperationItemData.createOrUpdate(operationItemData);
		if(isOk==false){
			peIFExecution.writeLogTextLine("Error -> "+operationItemData.getItemId()+" [create or update]");
			return operationItemData;
		}
		
		TCComponentBOPLine operationBOPLine = ifExecutOperationItemData.operationBOPLine;
		if(operationBOPLine==null){
			String message = "Operation BOMLine is null : "+operationItemData.getItemId();
			this.peIFExecution.writeLogTextLine(message);
			operationItemData.setStatus(TCData.STATUS_ERROR, message);
			return operationItemData;
		}

		// Operation 자체의 Data는 Update 되었으나 Child Node의 Data를 Update 해야 한다.
		TreeItem[] childNodeTreeItems = operationItemData.getItems();
		for (int i = 0; childNodeTreeItems != null
				&& i < childNodeTreeItems.length; i++) {

			this.peIFExecution.redrawUI();
			peIFExecution.waite();
			
			TreeItem currentTreeItem = childNodeTreeItems[i];

			if (currentTreeItem instanceof EndItemData) {
				ifExecutEndItemData.createOrUpdate((TCData) currentTreeItem);
			} else if (currentTreeItem instanceof SubsidiaryData) {
				ifExecutSubsidiaryData.createOrUpdate((TCData) currentTreeItem);
			} else if (currentTreeItem instanceof ToolData) {
				ifExecutToolData.createOrUpdate((TCData) currentTreeItem);
			} else if (currentTreeItem instanceof EquipmentData) {
				ifExecutEquipmentData.createOrUpdate((TCData) currentTreeItem);
			} else if (currentTreeItem instanceof ActivityMasterData) {
				ifExecutActivityMasterData
						.createOrUpdate((TCData) currentTreeItem);
			}
			
			peIFExecution.waite();
		}
		
		try {
			peIFExecution.getProcessWindow().save();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		peIFExecution.waite();
		
		return operationItemData;
	}

	/**
	 * Tree Node를 Tree에서 제거한다. 실제 제거하는 것이 좋을지 아니면 제거되었음을 표혀하는 아이콘으로 표시하는것이 좋을지
	 * 좀더 고민해 봐야 되겠다.
	 * 
	 * @param targetTreeNodeItem
	 */
	private void removeTreeNode(TreeItem targetTreeNodeItem) {
		TreeItem parentTreeNodeItem = targetTreeNodeItem.getParentItem();
		if (parentTreeNodeItem != null) {
			int treeNodeIndex = parentTreeNodeItem.indexOf(targetTreeNodeItem);
			if (treeNodeIndex > -1) {
				parentTreeNodeItem.clear(treeNodeIndex, true);
			}
		}
	}

	private boolean isSameMECONo(TreeItem targetTreeNodeItem) {

		String dataClassType = null;
		TCComponentBOPLine bopLine = null;
		TCComponentItemRevision currentItemRevision = null;

		if (targetTreeNodeItem instanceof ItemData) {
			ItemData tempItemData = (ItemData) targetTreeNodeItem;

			dataClassType = tempItemData.getClassType();
			if (dataClassType != null) {
				if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_OPERATION)) {
					bopLine = (TCComponentBOPLine) ((OperationItemData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_END_ITEM)) {
					bopLine = (TCComponentBOPLine) ((EndItemData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_EQUIPMENT)) {
					bopLine = (TCComponentBOPLine) ((EquipmentData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_SUBSIDIARY)) {
					bopLine = (TCComponentBOPLine) ((SubsidiaryData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_TOOL)) {
					bopLine = (TCComponentBOPLine) ((ToolData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_ACTIVITY)) {
					bopLine = (TCComponentBOPLine) ((ActivityMasterData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_ACTIVITY_SUB)) {
					bopLine = (TCComponentBOPLine) ((ActivitySubData) targetTreeNodeItem)
							.getBopBomLine();
				}
			}
		}

		boolean isSameMECONo = false;

		TCComponentChangeItemRevision changeItemRevision = null;
		String currentItemMecoNo = null;
		if (bopLine != null) {
			try {
				currentItemRevision = bopLine.getItemRevision();
				if (currentItemRevision != null) {
					changeItemRevision = (TCComponentChangeItemRevision) currentItemRevision
							.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
				}
				if (changeItemRevision != null) {
					currentItemMecoNo = changeItemRevision
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}

		}
		if (currentItemMecoNo == null
				|| (currentItemMecoNo != null && currentItemMecoNo.trim()
						.length() < 1)
				|| (currentItemMecoNo != null && currentItemMecoNo.trim()
						.equalsIgnoreCase("NULL"))) {
			currentItemMecoNo = "";
		}

		// 현재 Current Node Item Revision의 MECO No 확인
		if (peIFExecution.getMecoNo().equalsIgnoreCase(currentItemMecoNo)
				&& currentItemMecoNo.trim().equalsIgnoreCase("") == false) {
			// blank가 아닌 동일한 MECO No를 가짐.
			isSameMECONo = true;
		}

		return isSameMECONo;
	}

	/**
	 * Validation 결과를 검토해 Data를 변경 해야 하는경우 True를 그렇지 않은 경우 False를 Return 한다.
	 * 
	 * @param targetTreeNodeItem
	 * @return
	 */
	private boolean compareResultIsRevise(TreeItem targetTreeNodeItem) {

		boolean isReviseAble = false;

		// Validation 결과 확인
		boolean isAttributeChanged = false;
		boolean isBOMLineAttributeChanged = false;
		int changeType = TCData.DECIDED_NO_CHANGE;
		boolean haveChildNodeAdded = false;
		boolean haveChildNodeRemoved = false;
		boolean haveChildNodeReplaced = false;
		boolean haveChildNodeRevised = false;
		boolean haveChildNodeAttributeChanged = false;
		boolean haveChildNodeBOMLineAttributeChanged = false;
		String dataClassType = null;

		if (targetTreeNodeItem instanceof ItemData) {
			ItemData tempItemData = (ItemData) targetTreeNodeItem;

			isAttributeChanged = tempItemData.getAttributeChangeFlag();
			isBOMLineAttributeChanged = tempItemData
					.getBOMAttributeChangeFlag();
			changeType = tempItemData.getDecidedChagneType();
			haveChildNodeAdded = tempItemData.getChildNodeAddedFlag();
			haveChildNodeRemoved = tempItemData.getChildNodeRemovedFlag();
			haveChildNodeReplaced = tempItemData.getChildNodeReplacedFlag();
			haveChildNodeRevised = tempItemData.getChildNodeRevisedFlag();
			haveChildNodeAttributeChanged = tempItemData
					.getChildAttributeChangedFlag();
			haveChildNodeBOMLineAttributeChanged = tempItemData
					.getChildBOMLineChangedFlag();
			dataClassType = tempItemData.getClassType();
		}

		boolean isReviseTargetCondition1 = false;
		if (isAttributeChanged || haveChildNodeAdded || haveChildNodeRemoved
				|| haveChildNodeReplaced
				|| haveChildNodeBOMLineAttributeChanged) {
			isReviseTargetCondition1 = true;
		}

		boolean isReviseTargetCondition2 = true;
		if (dataClassType.trim().equalsIgnoreCase(
				TCData.TC_TYPE_CLASS_NAME_OPERATION)) {
			if (isBOMLineAttributeChanged == false
					&& haveChildNodeAttributeChanged == false) {
				isReviseTargetCondition2 = false;
			}
		}

		boolean isReviseTargetCondition3 = false;
		if (changeType == TCData.DECIDED_REVISE) {
			isReviseTargetCondition3 = true;
		}

		if ((isReviseTargetCondition1 && isReviseTargetCondition2)
				|| isReviseTargetCondition3) {
			isReviseAble = true;
		}

		return isReviseAble;
	}

	private EndItemData updateEndItemData(EndItemData endItemData) {

		TCComponentBOPLine endItemBOPLine = null;

		// 변경 Type에 따라 추가 또는 개정된 bopLine을 받아 온다.
		int changeType = endItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("End Item Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("End Item Remove");
		} else if (changeType == ItemData.DECIDED_REVISE) {
			System.out.println("End Item Revise");
		} else if (changeType == ItemData.DECIDED_REPLACE) {
			System.out.println("End Item Replace");
		}

		if (endItemBOPLine == null) {
			// Operation이 삭제 된 Case로 봐야 한다.
		}

		return endItemData;
	}

	private SubsidiaryData updateSubsidiaryItemData(
			SubsidiaryData subsidiaryItemData) {

		TCComponentBOPLine subsidiaryItemBOPLine = null;

		// 변경 Type에 따라 추가 또는 개정된 bopLine을 받아 온다.
		int changeType = subsidiaryItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Subsidiary Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("Subsidiary Remove");
		} else if (changeType == ItemData.DECIDED_REVISE) {
			System.out.println("Subsidiary Revise");
		} else if (changeType == ItemData.DECIDED_REPLACE) {
			System.out.println("Subsidiary Relplace");
		}

		if (subsidiaryItemBOPLine == null) {
			// Operation이 삭제 된 Case로 봐야 한다.
		}
		return subsidiaryItemData;
	}

	private void createFacility(EquipmentData equipmentItemData) {

	}

	private EquipmentData updateFacilityItemData(EquipmentData equipmentItemData) {

		TCComponentBOPLine equipmentItemBOPLine = null;

		// 변경 Type에 따라 추가 또는 개정된 bopLine을 받아 온다.
		int changeType = equipmentItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Equipment Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("Equipment Remove");
		} else if (changeType == ItemData.DECIDED_REVISE) {
			System.out.println("Equipment Revise");
		} else if (changeType == ItemData.DECIDED_REPLACE) {
			System.out.println("Equipment Replace");
		}

		if (equipmentItemBOPLine == null) {
			// Operation이 삭제 된 Case로 봐야 한다.
		}

		return equipmentItemData;
	}

	private void createTool(ToolData toolItemData) {

	}

	private ToolData updateToolItemData(ToolData toolItemData) {

		TCComponentBOPLine toolItemBOPLine = null;

		// 변경 Type에 따라 추가 또는 개정된 bopLine을 받아 온다.
		int changeType = toolItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Tool Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("Tool Remove");
		} else if (changeType == ItemData.DECIDED_REVISE) {
			System.out.println("Tool Revise");
		} else if (changeType == ItemData.DECIDED_REPLACE) {
			System.out.println("Tool Replace");
		}

		if (toolItemBOPLine == null) {
			// Operation이 삭제 된 Case로 봐야 한다.
		}

		return toolItemData;
	}

	private void createActivity(ActivityMasterData activityMasterDat) {

	}

	private ActivityMasterData updateActivityMasterItemData(
			ActivityMasterData activityMasterItemData) {

		TCComponentMEActivity rootActivity = null;

		// 변경 Type에 따라 추가 또는 개정된 bopLine을 받아 온다.
		int changeType = activityMasterItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Activity Maseter Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("Activity Maseter Remove");
		} else {
			System.out.println("Activity Maseter No Change");
		}

		if (rootActivity == null) {
			// Operation이 삭제 된 Case로 봐야 한다.
		}

		if (changeType != ItemData.DECIDED_ADD) {
			return activityMasterItemData;
		}
		// Activity의 Sub Activity Data를 Update 해야 한다.
		TreeItem[] childNodeTreeItems = activityMasterItemData.getItems();
		for (int i = 0; childNodeTreeItems != null
				&& i < childNodeTreeItems.length; i++) {
			TreeItem currentTreeItem = childNodeTreeItems[i];
			if (currentTreeItem == null) {
				continue;
			}

			if (currentTreeItem instanceof ActivitySubData) {
				currentTreeItem = updateSubActivityItemData((ActivitySubData) currentTreeItem);
			}
		}

		return activityMasterItemData;
	}

	private void createSubActivity(ActivityMasterData activityMasterItemData,
			ActivitySubData activitySubData) {

	}

	private ActivitySubData updateSubActivityItemData(
			ActivitySubData subActivityItemData) {

		TCComponentMEActivity subActivity = null;

		// 변경 Type에 따라 추가 또는 개정된 bopLine을 받아 온다.
		int changeType = subActivityItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Sub Activity Add");
		}

		if (subActivity == null) {
			// Operation이 삭제 된 Case로 봐야 한다.
		}

		return subActivityItemData;
	}

}
