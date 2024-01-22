package com.symc.plm.me.sdv.service.migration.work.peif;

import java.util.Vector;

import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Element;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.LineItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMECfgLine;
import com.teamcenter.rac.kernel.TCComponentMEOP;
import com.teamcenter.rac.kernel.TCComponentMEOPRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;

public abstract class IFExecutDefault {

	protected NewPEIFExecution peIFExecution;

	protected OperationItemData operationItemData;
	protected TCComponentBOPLine operationBOPLine;
	protected TCComponentItem meOperationItem;
	protected TCComponentItemRevision meOperationRevision;
	protected String operationItemId;

	protected TCData currentTCData;

	public IFExecutDefault( NewPEIFExecution peIFExecution ) {
		this.peIFExecution = peIFExecution;
		
//		this.peImportDataReaderUtil = peIFExecution.getPeImportDataReaderUtil();
//		this.targetMECOItemRev = (TCComponentChangeItemRevision) peIFExecution.getMecoRevision();
//		this.targetMECONo = peIFExecution.getMecoNo();
//		this.processWindow = peIFExecution.getProcessWindow();
//		this.productWindow = peIFExecution.getProductWindow();
	}

	public boolean createOrUpdate(TCData tcData) {

		// Object의 생성/개정/Update 여부를 결정하는 Function 필요한 경우 추가적인 구현을 해야 한다.
		
		boolean isOk = true;
		
		this.currentTCData = tcData;
		operationItemData = null;
		operationBOPLine = null;
		meOperationRevision = null;

		if (tcData != null) {

			if (tcData.getBomLineNode() != null) {
				operationItemId = ((Element) tcData.getBomLineNode())
						.getAttribute("OperationItemId");
			} else if (tcData.getMasterDataNode() != null) {
				operationItemId = ((Element) tcData.getMasterDataNode())
						.getAttribute("OperationItemId");
			}

			if (operationItemId != null) {
				operationItemId = operationItemId.trim();
			}

			TreeItem tempTreeItem = tcData.getParentItem();
			if (tempTreeItem != null
					&& tempTreeItem instanceof OperationItemData) {
				this.operationItemData = (OperationItemData) tempTreeItem;
			}
		}

		if (this.operationItemData != null) {
			operationBOPLine = (TCComponentBOPLine) this.operationItemData.getBopBomLine();
		}
		
		if(operationBOPLine==null){
			if(this.operationItemData!=null){
				String opId = this.operationItemData.getItemId();
				if(opId!=null && this.operationItemData.getParentItem()!=null && this.operationItemData.getParentItem() instanceof LineItemData){
					LineItemData tempLineItemData = (LineItemData)this.operationItemData.getParentItem();
					if(tempLineItemData!=null){
						TCComponentBOPLine lineBOPLine = (TCComponentBOPLine)tempLineItemData.getBopBomLine();
						try {
							AIFComponentContext[] childBOMLines = lineBOPLine.getChildren();
							for (int i = 0; i < childBOMLines.length; i++) {
								if(childBOMLines[i]!=null){
									TCComponentBOMLine bomLine = (TCComponentBOMLine)childBOMLines[i].getComponent();
									String tempItemId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
									if(tempItemId!=null && tempItemId.trim().equalsIgnoreCase(opId.trim())){
										operationBOPLine = (TCComponentBOPLine)bomLine;
										break;
									}
								}
							}
						} catch (TCException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		// init Operation Revision
		if (operationBOPLine != null) {

			TCComponentItemRevision tempOperatoinItemRevision = null;
			try {
				tempOperatoinItemRevision = operationBOPLine.getItemRevision();
			} catch (TCException e) {
				e.printStackTrace();
			}
			if (tempOperatoinItemRevision != null
					&& tempOperatoinItemRevision instanceof TCComponentMEOPRevision) {
				meOperationRevision = (TCComponentMEOPRevision) tempOperatoinItemRevision;
			}
			if (meOperationRevision != null) {
				try {
					meOperationItem = (TCComponentMEOP) meOperationRevision
							.getItem();
				} catch (TCException e) {
					e.printStackTrace();
				}
				if(meOperationItem!=null){
					try {
						operationItemId = meOperationItem.getProperty("item_id");
					} catch (TCException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return isOk;
	}

	/**
	 * Object가 추가되는 경우 Object를 생성하는 기능을 수행하는 Function을 구현 하도록 한다.
	 * 
	 * @throws Exception
	 * @throws TCException
	 */
	public abstract void createTargetObject() throws Exception, TCException;

	/**
	 * Object가 개정되는 경우 Object를 개정하는 기능을 수행하는 Function을 구현 하도록 한다.
	 * 
	 * @throws Exception
	 * @throws TCException
	 */
	public abstract void reviseTargetObject() throws Exception, TCException;

	/**
	 * Object가 BOMLine에서 제거 되는 경우 Object를 제거하는 기능을 수행하는 Function을 구현 하도록 한다.
	 * 
	 * @throws Exception
	 * @throws TCException
	 */
	public abstract void removeTargetObject() throws Exception, TCException;

	/**
	 * 생성/개정 된 Object의 BOMLine 특성, ItemRevision 특성들을 Update 하는기능을 수행 하는
	 * Function을 구현 하도록 한다.
	 * 
	 * @throws Exception
	 * @throws TCException
	 */
	public abstract void updateTargetObject() throws Exception, TCException;

	protected void refreshBOMLine(Object tcComponent) throws Exception {
		// Activity Refresh
		if (tcComponent instanceof TCComponentMECfgLine) {
			TCComponentMECfgLine tcComponentMECfgLine = (TCComponentMECfgLine) tcComponent;
			tcComponentMECfgLine.clearCache();
			tcComponentMECfgLine.window().fireChangeEvent();
			tcComponentMECfgLine.refresh();
		} else if (tcComponent instanceof TCComponentCfgAttachmentLine) {
			TCComponentCfgAttachmentLine tcComponentCfgAttachmentLine = (TCComponentCfgAttachmentLine) tcComponent;
			tcComponentCfgAttachmentLine.clearCache();
			tcComponentCfgAttachmentLine.window().fireChangeEvent();
			tcComponentCfgAttachmentLine.refresh();
		}
		// BOMLine Refresh
		else if (tcComponent instanceof TCComponentBOMLine) {
			TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) tcComponent;
			tcComponentBOMLine.clearCache();
			tcComponentBOMLine.refresh();
			tcComponentBOMLine.window().newIrfWhereConfigured(
					tcComponentBOMLine.getItemRevision());
			tcComponentBOMLine.window().fireComponentChangeEvent();
		}
	}

	/**
	 * Option Condition Conversion
	 * 
	 * @method setConversionOptionCondition
	 * @date 2013. 12. 12.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	protected String getConversionOptionCondition(String condition)
			throws TCException, Exception {
		return ImportCoreService.conversionOptionCondition(peIFExecution.getProcessWindow(), condition);
	}

	protected String getPropertyString(String inputStr) {
		String newStr = "";

		if (inputStr != null && inputStr.trim().length() > 0
				&& inputStr.trim().equalsIgnoreCase("NULL") == false) {
			newStr = inputStr.trim();
		}

		return newStr;
	}

	protected static boolean isReleased(TCComponent component) {
		boolean isReleased = false;

		TCComponent targetComponent = component;
		
		if(component!=null && component instanceof TCComponentBOMLine){
			try {
				targetComponent = ((TCComponentBOMLine)component).getItemRevision();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		try {
			TCComponent[] releasedStatusComponents = targetComponent
					.getReferenceListProperty("release_status_list");
			if (releasedStatusComponents != null
					&& releasedStatusComponents.length > 0) {
				isReleased = true;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return isReleased;
	}

	/**
	 * 개정 할 수 있는 권한이 있는지 확인하는 Function
	 *  
	 * @param component
	 * @return
	 */
	protected boolean haveChangeAccessRight(TCComponent component) {
		
		boolean isChangeAble = false;
		
		Vector<String> grantedPrevilegeV = SYMTcUtil.getGrantedPrivilegeNameVector(component);
		// DELETE, READ, WRITE, CHANGE, MARKUP, COPY, CHANGE_OWNER, EXPORT, IMPORT, PUBLISH, DIGITAL_SIGN...
		if(grantedPrevilegeV!=null && grantedPrevilegeV.contains("CHANGE")){
			// 개정 가능
			isChangeAble = true; 
		}

		return isChangeAble;
	}

	/**
	 * 쓰기 권한이 있는지 확인 하는 Function
	 * @param component
	 * @return
	 */
	protected static boolean haveWriteAccessRight(TCComponent component) {

		boolean haveAccessRight = false;

		TCAccessControlService accessSvc = component.getSession()
				.getTCAccessControlService();
		
		boolean itemRevisionWriteRight = false;
		boolean isBOMLine = false;
		if(component instanceof TCComponentBOMLine){
			isBOMLine = true;
			TCComponentBOMViewRevision tempBOMViewRevision = null;
			try {
				tempBOMViewRevision = ((TCComponentBOMLine)component).parent().getBOMViewRevision();
			} catch (TCException e) {
				e.printStackTrace();
			}
			itemRevisionWriteRight = haveWriteAccessRight(tempBOMViewRevision);
		}
		
		try {
			haveAccessRight = accessSvc.checkPrivilege(component,
					TCAccessControlService.WRITE);
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		if(isBOMLine==true){
			if(itemRevisionWriteRight==false){
				haveAccessRight = false;
			}
		}

		return haveAccessRight;
	}
	
	private TCComponentBOMLine findSameIdChildNode(String absOccId, TCComponentBOMLine targetBOMLine){
		TCComponentBOMLine findedBOMLine = null;
		
		if(targetBOMLine==null){
			return findedBOMLine;
		}
		
		try {
			targetBOMLine.refresh();
		} catch (TCException e) {
			e.printStackTrace();
		}
		AIFComponentContext[] childNodes = null;
		try {
			childNodes = targetBOMLine.getChildren();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; childNodes!=null && i < childNodes.length; i++) {
			TCComponentBOMLine tempBOMLine = (TCComponentBOMLine)childNodes[i].getComponent();
			String itemId = null;
			try {
				itemId = tempBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
			} catch (TCException e) {
				e.printStackTrace();
			}
			if(itemId!=null && itemId.trim().equalsIgnoreCase(absOccId.trim())){
				findedBOMLine = tempBOMLine;
				break;
			}
		}
		
		return findedBOMLine;
	}

	protected TCComponentBOMLine[] getCurrentBOPLine(String absOccId,
			boolean isProcess, TCComponentBOMLine targetBOMLine) {

		TCComponentBOMLine[] findedBOMLines = null;
		boolean findAllTarget = true;

		TCComponentBOMLine selectedTargetBOMLine = null;

		if (isProcess == true) {

			try {
				if (selectedTargetBOMLine == null) {
					selectedTargetBOMLine = peIFExecution.getProcessLine();
				}
				findedBOMLines = peIFExecution.getProcessWindow().findConfigedBOMLinesForAbsOccID(
						absOccId, findAllTarget, selectedTargetBOMLine);
			} catch (TCException e) {
				e.printStackTrace();
			}
			if(findedBOMLines==null || (findedBOMLines!=null && findedBOMLines.length<1)){
				TCComponentBOMLine tempBOMLine = findSameIdChildNode(absOccId, targetBOMLine);
				if(tempBOMLine!=null){
					findedBOMLines = new TCComponentBOMLine[]{tempBOMLine};
				}
			}
		} else {
			try {
				if (selectedTargetBOMLine == null) {
					selectedTargetBOMLine = peIFExecution.getProductLine();
				}
				findedBOMLines = peIFExecution.getProductWindow().findConfigedBOMLinesForAbsOccID(
						absOccId, findAllTarget, selectedTargetBOMLine);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		return findedBOMLines;
	}

	protected TCComponentBOMLine[] getCurrentBOPLine(
			TCComponentBOMLine parentBOMLine, String itemId, String seqId) {
		TCComponentBOMLine[] findedBOMLine = null;

		if (this.currentTCData == null) {
			return findedBOMLine;
		}

		AIFComponentContext[] childContext = null;
		if(parentBOMLine!=null){
			try {
				childContext = parentBOMLine.getChildren();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		Vector<TCComponentBOMLine> tempTargetBOMLineV = new Vector<TCComponentBOMLine>();

		for (int i = 0; childContext != null && i < childContext.length; i++) {
			if (childContext[i].getComponent() == null) {
				continue;
			}
			if ((childContext[i].getComponent() instanceof TCComponentBOMLine) == false) {
				continue;
			}

			TCComponentBOMLine tempBOMLine = (TCComponentBOMLine) childContext[i]
					.getComponent();

			String tempItemType = null;
			String tempItemId = null;
			String tempSeqNo = null;
			try {
				TCComponentItem tempItem = tempBOMLine.getItem();
				if (tempItem != null) {
					tempItemType = tempItem.getType();
				}
				tempItemId = tempBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				tempSeqNo = tempBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
			} catch (TCException e) {
				e.printStackTrace();
			}

			if (tempItemType == null
					|| (tempItemType != null && tempItemType.trim().length() < 1)) {
				continue;
			}

			if (tempSeqNo != null && seqId != null) {
				if (isNumeric(tempSeqNo) == true) {
					if (isNumeric(seqId) == true) {
						double doubleTempSeqNo = Double.parseDouble(tempSeqNo);
						double doubleSeqId = Double.parseDouble(seqId);
						if (doubleTempSeqNo == doubleSeqId) {
							tempTargetBOMLineV.add(tempBOMLine);
						}
					}
				} else {
					if (seqId.trim().equalsIgnoreCase(tempSeqNo.trim()) == true) {
						tempTargetBOMLineV.add(tempBOMLine);
					}
				}
			}
		}

		if (tempTargetBOMLineV != null) {
			findedBOMLine = new TCComponentBOMLine[tempTargetBOMLineV.size()];
			for (int i = 0; tempTargetBOMLineV != null
					&& i < tempTargetBOMLineV.size(); i++) {
				findedBOMLine[i] = tempTargetBOMLineV.get(i);
			}
		}

		return findedBOMLine;
	}

	private boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}

	protected void getReleasedMECO(TCComponent component) {

		// ProcessUtil.getProcess(imancomponent)
		// TCAttachmentType.TARGET
		// TCAttachmentType.RELEASE_STATUS

		try {
			component.whereReferenced();
		} catch (TCException e) {
			e.printStackTrace();
		}

	}

	protected  static TCComponentChangeItemRevision[] getReferencedChangeItemRevision(
			TCComponent component) {

		Vector<TCComponentProcess> processVector = new Vector<TCComponentProcess>();

		TCComponent targetComponent = component;

		boolean findRef = true;
		if (component instanceof TCComponentBOMLine) {
			try {
				targetComponent = (TCComponent) ((TCComponentBOMLine) component)
						.getItemRevision();
			} catch (TCException e) {
				e.printStackTrace();
			}
		} else if (component instanceof TCComponentTask) {
			TCComponentProcess rootProcess = null;
			try {
				rootProcess = ((TCComponentTask) component).getRoot()
						.getProcess();
			} catch (TCException e) {
				e.printStackTrace();
			}
			if (rootProcess != null
					&& processVector.contains(rootProcess) == false) {
				processVector.add(rootProcess);
			}
			findRef = false;
		} else if (component instanceof TCComponentProcess) {
			TCComponentProcess rootProcess = null;
			try {
				rootProcess = ((TCComponentProcess) component).getRootTask()
						.getProcess();
			} catch (TCException e) {
				e.printStackTrace();
			}
			if (rootProcess != null
					&& processVector.contains(rootProcess) == false) {
				processVector.add(rootProcess);
			}
			findRef = false;
		}

		AIFComponentContext[] contexts = null;
		if (findRef == true) {
			try {
				contexts = targetComponent.whereReferenced();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		for (AIFComponentContext context : contexts) {

			InterfaceAIFComponent tempComponent = context.getComponent();
			if (tempComponent instanceof TCComponentTask) {
				TCComponentTask task = (TCComponentTask) tempComponent;
				TCComponentProcess rootProcess = null;
				try {
					rootProcess = task.getRoot().getProcess();
				} catch (TCException e) {
					e.printStackTrace();
				}

				if (rootProcess != null
						&& processVector.contains(rootProcess) == false) {
					processVector.add(rootProcess);
				}
			}
		}

		Vector<TCComponentChangeItemRevision> changeItemRevVector = new Vector<TCComponentChangeItemRevision>();

		for (int j = 0; processVector != null && j < processVector.size(); j++) {

			TCComponentProcess rootProcess = (TCComponentProcess) processVector
					.get(j);
			TCComponent[] targetComponents = null;
			try {
				targetComponents = rootProcess.getRootTask().getAttachments(
						TCAttachmentScope.LOCAL, TCAttachmentType.TARGET);
			} catch (TCException e1) {
				e1.printStackTrace();
			}

			for (int k = 0; targetComponents != null
					&& k < targetComponents.length; k++) {
				if (targetComponents[k] != null
						&& targetComponents[k] instanceof TCComponentChangeItemRevision) {
					if (changeItemRevVector
							.contains((TCComponentChangeItemRevision) targetComponents[k]) == false) {
						changeItemRevVector
								.add((TCComponentChangeItemRevision) targetComponents[k]);
					}
				}
			}

		}

		processVector.clear();
		processVector = null;

		TCComponentChangeItemRevision[] changeItemRevisionList = null;
		if (changeItemRevVector != null && changeItemRevVector.size() > 0) {
			changeItemRevisionList = new TCComponentChangeItemRevision[changeItemRevVector
					.size()];
		}

		for (int j = 0; changeItemRevVector != null
				&& j < changeItemRevVector.size(); j++) {
			TCComponentChangeItemRevision changeItemRevision = (TCComponentChangeItemRevision) changeItemRevVector
					.get(j);
			changeItemRevisionList[j] = changeItemRevision;
		}

		return changeItemRevisionList;
	}

	protected String getNullChangedStr(String inputStr){
		String outStr = null;
		
		if(inputStr==null || (inputStr!=null && inputStr.trim().length()<1)){
			outStr = "";
		}else if(inputStr!=null && inputStr.trim().equalsIgnoreCase("NULL")){
			outStr = "";
		}else{
			outStr = inputStr.trim();
		}
		
		return outStr;
	}
}
