package com.symc.plm.me.sdv.service.migration.work.peif;

import org.w3c.dom.Element;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EndItemData;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNodeType;
import com.teamcenter.rac.kernel.TCException;

public class IFExecutEndItemData extends IFExecutDefault {

	private EndItemData endItemData;

	private boolean isNeedToBOMLineAdd = false;
	private boolean isNeedToBOMLineReplace = false;

	private TCComponentBOPLine endItemBOPLine;
	private TCComponentItem endItemItem;
	private TCComponentItemRevision endItemItemRevision;
	private String endItemId;

	private TCComponentBOPLine oldEndItemBOPLine;
	private TCComponentItem oldEndItemItem;
	private TCComponentItemRevision oldEndItemItemRevision;

	public IFExecutEndItemData(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean createOrUpdate(TCData endItemData) {

		this.endItemData = (EndItemData) endItemData;

		endItemBOPLine = null;
		isNeedToBOMLineAdd = false;
		isNeedToBOMLineReplace = false;

		endItemItem = null;
		endItemItemRevision = null;

		super.createOrUpdate(endItemData);
		initTargetItem();

		boolean isUpdateTarget = false;

		int changeType = this.endItemData.getDecidedChagneType();
		boolean bomChanged = this.endItemData.getBOMAttributeChangeFlag();
		boolean attributeChanged = this.endItemData.getAttributeChangeFlag();
		
		if (changeType == TCData.DECIDED_NO_CHANGE && bomChanged==false) {
			// 추가적인 처리 없이 Return
			return true;
		} else if (changeType == TCData.DECIDED_REMOVE) {
			// 삭제 처리를 수행 한다.
			boolean haveRemoveException = false;
			try {
				removeTargetObject();
				peIFExecution.waite();
			} catch (TCException e) {
				e.printStackTrace();
				haveRemoveException = true;
			} catch (Exception e) {
				e.printStackTrace();
				haveRemoveException = true;
			}
			return !(haveRemoveException);
		} else if (changeType == TCData.DECIDED_ADD) {
			bomChanged=true;
			attributeChanged=true;
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		} else if (changeType == TCData.DECIDED_REVISE) {
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		} else if (changeType == TCData.DECIDED_REPLACE) {
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		}

		if (this.oldEndItemBOPLine == null) {
			if (endItemItemRevision != null) {
				isNeedToBOMLineAdd = true;
			}
		}

		if (changeType == TCData.DECIDED_ADD) {
			isNeedToBOMLineAdd = true;
			isUpdateTarget = true;
		} else if (changeType == TCData.DECIDED_REPLACE) {
			isNeedToBOMLineReplace = true;
			isUpdateTarget = true;
		} else if (changeType == TCData.DECIDED_REVISE) {
			// End Item의 경우 큰 의미가 없음.
			isUpdateTarget = true;
		}
		
		if(bomChanged==true || attributeChanged==true){
			isUpdateTarget = true;
		}

		boolean isOk = true;
		if (this.endItemBOPLine == null && this.operationBOPLine != null) {
			try {
				addBOMLine();
				peIFExecution.waite();
			} catch (Exception e) {
				e.printStackTrace();
				isOk = false;
			}
		}

		if (isUpdateTarget == true) {
			try {
				updateTargetObject();
				peIFExecution.waite();
			} catch (TCException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return isOk;
	}

	/**
	 * Subsidiary의 경우 Tc에 없는경우에라도 Subsidiary Item을 임의로 생성 하지 않는다. 따라서
	 * SubSidiary의 경우 Subsidiary를 추가하는 경우에라도 Subsidiary를 이미등록된 것들 중에 찾아서 추가하는 것만
	 * 가능함.
	 */
	public void createTargetObject() throws Exception, TCException {
		// End Item은 MBOM의 End Item을 찾아 할당 하는 것이므로 Item 생서을 BOP 모듈에서 할 필요가 없다.
		// 할당 대상 Data가 없으면 Exception 처리 하면 된다.
	}

	/**
	 * 부자재의 경우 Revise도 Interface P/G에서 진행 되지 않도록 한다.
	 */
	public void reviseTargetObject() throws Exception, TCException {
		// Tool/Equipment/Sub Sidiary/End Item Revise 하지 않는다.
	}

	public void removeTargetObject() throws Exception, TCException {

		if (this.oldEndItemBOPLine != null) {
			if (this.operationBOPLine != null) {
				if (haveWriteAccessRight(this.operationBOPLine)) {
					this.oldEndItemBOPLine.cut();
				}
			}
		}

		this.operationBOPLine.save();
	}

	public void updateTargetObject() throws Exception, TCException {

		boolean isReleased = false;
		boolean isWriteAble = false;
		
		if(this.endItemBOPLine==null){
			return;
		}
		
		try {
			isReleased = SYMTcUtil.isReleased(this.endItemBOPLine);
			// 쓰기 권한이 있는지 Check 한다.
			if (isReleased == false) {
				isWriteAble = haveWriteAccessRight(this.endItemBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (isReleased == true) {
			System.out.println("[" + this.endItemData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;
		}else if(isWriteAble == false){
			System.out.println("[" + this.endItemData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;			
		}

		// BOMLine Attribute 설정
		Element bomLineDataElement = (Element) this.endItemData
				.getBomLineNode();

		try {
			// Qty = 1
			endItemBOPLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
		} catch (Exception e) {
			String message = "End item quantity set error : "+operationItemId + " -> "+endItemId+" "+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.endItemData.setStatus(TCData.STATUS_ERROR, message);
		}

		// Find No. 등록
		String sequenceStr = null;
		if (bomLineDataElement.getElementsByTagName("O") != null) {
			if (bomLineDataElement.getElementsByTagName("O").getLength() > 0) {
				sequenceStr = bomLineDataElement.getElementsByTagName("O")
						.item(0).getTextContent();
			}
		}
		if (sequenceStr == null
				|| (sequenceStr != null && sequenceStr.trim().length() < 1)) {
			sequenceStr = "";
		}
		
		try {
			endItemBOPLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO,
					getPropertyString(sequenceStr) );
		} catch (Exception e) {
			String message = "End item seq set error : "+operationItemId + " -> "+endItemId+" "+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.endItemData.setStatus(TCData.STATUS_ERROR, message);

		}

		endItemBOPLine.save();
		endItemData.setBopBomLine(endItemBOPLine);

	}

	private void addBOMLine() throws Exception {

		int changeType = this.endItemData.getDecidedChagneType();
		if (changeType == TCData.DECIDED_REMOVE) {
			return;
		}

		boolean isReplaceTarget = false;
		if (this.oldEndItemBOPLine != null) {
			String oldItemId = this.oldEndItemItem.getProperty("item_id");
			String targetItemId = this.endItemData.getItemId();

			if (oldItemId.equalsIgnoreCase(targetItemId) == false) {
				isReplaceTarget = true;
			}
		}

		if (isReplaceTarget == true) {
			// Do Replace
			return;
		} else if (this.oldEndItemBOPLine != null) {
			this.endItemBOPLine = this.oldEndItemBOPLine;
			this.endItemItem = this.endItemBOPLine.getItem();
			this.endItemItemRevision = this.endItemBOPLine.getItemRevision();

			if (this.endItemBOPLine != null) {
				this.endItemData.setBopBomLine(this.endItemBOPLine);
			}
			return;
		}

		// ---------------------------
		// 위의 조건에 포함 되지 않고 여기까지 오면 어떤 이유 인지는 몰라도
		// End Item BOMLine이 없는 경우이므로
		// End Item BOMLine을 추가해 주어야 한다.
		// ---------------------------

		// Validation을 거친 경우 Product BOMLine은 설정되어 있는 상태임.
		TCComponentBOMLine productBOMLine = this.endItemData.getProductBomLine();

		String productABSOccId = null;
		if (productBOMLine != null) {
			productABSOccId = productBOMLine
					.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
		} else {
			this.peIFExecution.writeLogTextLine("Product BOMLine Not Found : "+this.operationItemId+" -> "+endItemId +"("+this.endItemData.getAbsOccPuids()+")");
			this.endItemData.setHaveMajorError(true);
			return;
			//throw new Exception("Can't find product BOM Line ..");
		}

		boolean isProcess = true;
		TCComponentBOPLine targetBOMLine = operationBOPLine;
		TCComponentBOMLine[] processBOMLines = getCurrentBOPLine(
				productABSOccId, isProcess, null);
		if (processBOMLines != null && processBOMLines.length > 0) {
			this.endItemBOPLine = (TCComponentBOPLine) processBOMLines[0];
			return;
		}

		if (this.operationBOPLine != null) {

			boolean haveWriteAccess = haveWriteAccessRight(this.operationBOPLine);

			if (haveWriteAccess == true) {
				try {
					this.endItemBOPLine = (TCComponentBOPLine) this.operationBOPLine
							.assignAsChild(productBOMLine, "MEConsumed");
					this.endItemBOPLine.setProperty(
							SDVPropertyConstant.BL_ABS_OCC_ID, productABSOccId);
					this.endItemBOPLine.save();
				} catch (TCException e) {
					e.printStackTrace();
				}
			} else {
				throw new Exception("You need write permissions : "
						+ this.operationBOPLine);
			}
		}

		if (this.endItemBOPLine != null) {
			this.endItemData.setBopBomLine(this.endItemBOPLine);
		}

	}

	private void initTargetItem() {

		this.oldEndItemBOPLine = (TCComponentBOPLine) this.endItemData
				.getBopBomLine();
		if (this.oldEndItemBOPLine == null) {

			// Validation을 거친 경우 Product BOMLine은 설정되어 있는 상태임.
			TCComponentBOMLine productBOMLine = this.endItemData
					.getProductBomLine();

			String productABSOccId = null;
			if (productBOMLine != null) {
				try {
					productABSOccId = productBOMLine
							.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}

			if (productABSOccId != null && productABSOccId.trim().length() > 0) {
				boolean isProcess = true;
				TCComponentBOPLine targetBOMLine = operationBOPLine;
				TCComponentBOMLine[] processBOMLines = getCurrentBOPLine(
						productABSOccId, isProcess, null);
				if (processBOMLines != null && processBOMLines.length > 0) {
					this.oldEndItemBOPLine = (TCComponentBOPLine) processBOMLines[0];
				}
			}
		}

		if (this.oldEndItemBOPLine != null) {
			// Old Data를 찾아서 정의한다.
			try {
				this.oldEndItemItemRevision = this.oldEndItemBOPLine
						.getItemRevision();
				this.oldEndItemItem = this.oldEndItemBOPLine.getItem();
				if (this.oldEndItemItem != null) {
					this.endItemId = this.oldEndItemItem
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		int changeType = this.endItemData.getDecidedChagneType();
		if (changeType == TCData.DECIDED_REMOVE) {
			// New에 대해서는 기술할 필요 없음.
		} else {
			TCComponentBOMLine currentProductBOMLine = (TCComponentBOMLine) this.endItemData
					.getProductBomLine();
			if (currentProductBOMLine != null) {
				try {
					this.endItemItem = currentProductBOMLine.getItem();
					this.endItemItemRevision = currentProductBOMLine.getItemRevision();
				} catch (TCException e) {
					e.printStackTrace();
				}
				if (this.endItemItem != null) {
					try {
						this.endItemId = this.endItemItem.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
					} catch (TCException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * BOP Line에 Assign된 Part를 BOM Line중에 찾아서 새로 Assign한다. 이것은 Process에서는
	 * Product Part를 찾을 수 있는데 Product에서는 Process에 Assign 된 Part를 찾을 수 없는경우 사용된다.
	 * -> 이런경우가 발생하는 이유는 아직 모른다.
	 * 
	 * @param operationBOPLine
	 * @param partBOMLinesInProduct
	 * @param assignedBOMLine
	 * @throws TCException
	 */
	private void refreshAssignedPartBOMLine(
			TCComponentBOPLine operationBOPLine,
			TCComponentBOMLine[] partBOMLinesInProduct,
			TCComponentBOMLine assignedBOMLine) throws TCException {

		// --------------------------------------------
		// Test 과정에 구현해보는 함수로 실제 사용여부는 아직 결정 하지 않음
		// 실제 사용 여부가 결정되면 그에 맞게 수정후 본 Remark 제거 할 것임.
		// [2017 01 17] Taeku.Jeong
		// --------------------------------------------

		// IMANComponentBOMWindow productWindow, IMANComponentBOPWindow
		// processWindow
		if (operationBOPLine == null || assignedBOMLine == null
				|| partBOMLinesInProduct == null) {
			return;
		}

		for (int i = 0; i < partBOMLinesInProduct.length; i++) {
			TCComponentBOMLine productBOMLine = partBOMLinesInProduct[i];

			TCComponent tempComp = assignedBOMLine
					.getReferenceProperty("bl_me_refline");
			if (tempComp == null) {
				String idInContextTopLine = "####"; // 임의로 지정했음. 실제로는 Unique한
													               // Id생성 기준이 필요함.
				String processIdInContextTopLine = "####"; 	// 임의로 지정했음. 실제로는
															                 	// Unique한 Id생성 기준이
															    				// 필요함.

				TCComponentBOPLine newAttachedBOPLine = null;
				
				//---------------------------------
				// Function Ussage (S)
				//---------------------------------
				// newAttachedBOPLine = (TCComponentBOPLine) operationBOPLine.add(productBOMLine, false, "MEConsumed",false);

				// This allows a current BOMLine to be "copied" to a new line as a child or substitute of this line, with an occurrence type.
				// add(TCComponentBOMLine bomLine, boolean asSubstitute, java.lang.String occType)

				// This allows a current BOMLine to be "copied" to a new line as a child or substitute of this line
				// add(TCComponentBOMLine bomLine, boolean asSubstitute)

				// Add a new line as a child of this line
				// add(TCComponentItem item, TCComponentItemRevision rev, TCComponent bv, boolean asSubstitute)

				// Add a new line as a child of this line, specifying the occurrence type.
				// add(TCComponentItem item, TCComponentItemRevision rev, TCComponent bv, boolean asSubstitute, java.lang.String occType)
				//---------------------------------
				// Function Ussage (E)
				//---------------------------------

				newAttachedBOPLine = (TCComponentBOPLine) operationBOPLine
						.assignAsChild(productBOMLine, "MEConsumed");

				newAttachedBOPLine.save();

				TCComponentMEAppearancePathNodeType appPathNodeType = 
						(TCComponentMEAppearancePathNodeType) operationBOPLine
						.getSession().getTypeComponent("MEAppearancePathNode");
				TCComponentMEAppearancePathNode bomLineAppPathNode = appPathNodeType
						.findOrCreateMEAppearancePathNode(productBOMLine);
				newAttachedBOPLine.linkToAppearance(bomLineAppPathNode, false);

				if (newAttachedBOPLine != null) {
					newAttachedBOPLine.setStringProperty("bl_abs_occ_id", processIdInContextTopLine);
					String partInstance = productBOMLine.getProperty("bl_occurrence_name");
					
					if (partInstance != null && partInstance.trim().length() > 0) {
						newAttachedBOPLine.setStringProperty( "bl_occurrence_name", partInstance);
					}
					assignedBOMLine.cut();
				}
			}

			break;
		}

	}

}
