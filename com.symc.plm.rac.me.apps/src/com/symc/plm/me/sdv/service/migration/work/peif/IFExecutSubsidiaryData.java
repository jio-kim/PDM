package com.symc.plm.me.sdv.service.migration.work.peif;

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.SubsidiaryData;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class IFExecutSubsidiaryData extends IFExecutDefault {

	private SubsidiaryData subsidiaryData;

	private boolean isNeedToBOMLineAdd = false;
	private boolean isNeedToBOMLineReplace = false;

	private TCComponentBOPLine subsidiaryBOPLine;
	private TCComponentItem subsidiaryItem;
	private TCComponentItemRevision subsidiaryItemRevision;
	private String subsidiaryItemId;

	public IFExecutSubsidiaryData(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean createOrUpdate(TCData subsidiaryData) {
		this.subsidiaryData = (SubsidiaryData) subsidiaryData;

		subsidiaryBOPLine = null;
		isNeedToBOMLineAdd = false;
		isNeedToBOMLineReplace = false;

		subsidiaryItem = null;
		subsidiaryItemRevision = null;

		super.createOrUpdate(subsidiaryData);
		initTargetItem();

		boolean isUpdateTarget = false;

		int changeType = this.subsidiaryData.getDecidedChagneType();
		boolean bomChanged = this.subsidiaryData.getBOMAttributeChangeFlag();
		boolean bomLineChangeFlag = this.subsidiaryData.getBOMAttributeChangeFlag();
		boolean attributeChangeFlag = this.subsidiaryData.getAttributeChangeFlag();
		
		System.out.println("Subsidiary Create&Update ["+this.operationItemId+"/"+this.subsidiaryItemId+"] changeType = "+changeType);
		
		if (changeType == TCData.DECIDED_NO_CHANGE && bomChanged==false) {
			// 추가적인 처리 없이 Return
			return true;
		} else if (changeType == TCData.DECIDED_REMOVE) {
			// 삭제 처리를 수행 한다.
			boolean haveRemoveException = false;
			try {
				removeTargetObject();
			} catch (TCException e) {
				e.printStackTrace();
				haveRemoveException = true;
			} catch (Exception e) {
				e.printStackTrace();
				haveRemoveException = true;
			}
			return !(haveRemoveException);
		} else if (changeType == TCData.DECIDED_ADD) {
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
			bomLineChangeFlag=true;
			attributeChangeFlag=true;
		} else if (changeType == TCData.DECIDED_REVISE) {
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		} else if (changeType == TCData.DECIDED_REPLACE) {
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
		}

		if (subsidiaryBOPLine == null) {
			isNeedToBOMLineAdd = true;
			isUpdateTarget = true;
		} else {
			if (changeType == TCData.DECIDED_REPLACE) {
				isNeedToBOMLineReplace = true;
				isUpdateTarget = true;
			} else if (changeType == TCData.DECIDED_REVISE) {
				isUpdateTarget = true;
			}
		}
		
		
		if(bomLineChangeFlag==true || attributeChangeFlag==true ){
			isUpdateTarget = true;
		}

		boolean haveCreateException = false;
		if (isNeedToBOMLineAdd || isNeedToBOMLineReplace) {
			try {
				createTargetObject();
				peIFExecution.waite();
				isUpdateTarget = true;
			} catch (TCException e) {
				e.printStackTrace();
				haveCreateException = true;
			} catch (Exception e) {
				e.printStackTrace();
				haveCreateException = true;
			}
		}
		
		if (haveCreateException==false && isUpdateTarget == true) {
			if (this.subsidiaryBOPLine == null) {
				addBOMLine();
				peIFExecution.waite();
			}
			
			try {
				updateTargetObject();
				peIFExecution.waite();
			} catch (TCException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		boolean isOk = false;
		if(haveCreateException== false){
			isOk = true;
		}
		
		return isOk;
	}

	/**
	 * Subsidiary의 경우 Tc에 없는경우에라도 Subsidiary Item을 임의로 생성 하지 않는다. 따라서
	 * SubSidiary의 경우 Subsidiary를 추가하는 경우에라도 Subsidiary를 이미등록된 것들 중에 찾아서 추가하는 것만
	 * 가능함.
	 */
	public void createTargetObject() throws Exception, TCException {

	}

	/**
	 * 부자재의 경우 Revise도 Interface P/G에서 진행 되지 않도록 한다.
	 */
	public void reviseTargetObject() throws Exception, TCException {
		// Tool/Equipment/Sub Sidiary/End Item Revise 하지 않는다.
	}

	public void removeTargetObject() throws Exception, TCException {

		if (this.operationBOPLine != null) {
			if (this.subsidiaryBOPLine != null) {
				if (haveWriteAccessRight(this.operationBOPLine) == true) {
					this.subsidiaryBOPLine.cut();
				} else {
					throw new Exception("You need write permissions. : "
							+ this.operationBOPLine);
				}
			}

			this.operationBOPLine.save();
		}

	}

	public void updateTargetObject() throws Exception, TCException {
		
		System.out.println("[" + this.subsidiaryData.getText() + "] Subsisiary BOMLine Update");

		if (this.subsidiaryBOPLine == null) {
			return;
		}

		boolean isReleased = false;
		boolean isWriteAble = false;
		try {
			isReleased = SYMTcUtil.isReleased(this.subsidiaryBOPLine);
			// 개정 권한이 있는지 Check 한다.
			if (isReleased == false) {
				isWriteAble = haveWriteAccessRight(this.subsidiaryBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

//		if (isReleased == true || isWriteAble == false) {
//			System.out.println("[" + this.subsidiaryData.getText() + "] "
//					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
//					+ ", isChangeAble=" + isWriteAble);
//		}

		// BOMLine Attribute 설정
		Element bomLineDataElement = (Element) this.subsidiaryData
				.getBomLineNode();

		try {
			// Qty = 1
			subsidiaryBOPLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
		} catch (Exception e) {
			String message = "Subsisiary qty setting error ["+operationItemId+" -> "+subsidiaryItemId+"]"+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.subsidiaryData.setStatus(TCData.STATUS_ERROR, message);
		}

		// 소요량 등록
		String quantityOfRequirement = null;
		if (bomLineDataElement.getElementsByTagName("M") != null) {
			if (bomLineDataElement.getElementsByTagName("M").getLength() > 0) {
				quantityOfRequirement = bomLineDataElement
						.getElementsByTagName("M").item(0).getTextContent();
			}
		}
		if (quantityOfRequirement == null
				|| (quantityOfRequirement != null && quantityOfRequirement
						.trim().length() < 1)) {
			quantityOfRequirement = "";
		}
		
//		System.out.println("[" + this.subsidiaryData.getText() + "] (Subsidiary BOMLine Update) quantityOfRequirement = "+quantityOfRequirement);
		
		try {
			subsidiaryBOPLine.setProperty(
					SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY,
					quantityOfRequirement);
		} catch (Exception e) {
			String message = "Subsisiary subsisiary_qty setting error ["+operationItemId+" -> "+subsidiaryItemId+"]"+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.subsidiaryData.setStatus(TCData.STATUS_ERROR, message);
		}
		
		// Find No. 등록
		String sequenceStr = null;
		if (bomLineDataElement.getElementsByTagName("O") != null) {
			if (bomLineDataElement.getElementsByTagName("O").getLength() > 0) {
				sequenceStr = bomLineDataElement.getElementsByTagName("O")
						.item(0).getTextContent();
			}
			if (sequenceStr != null && sequenceStr.trim().length() > 0) {
				sequenceStr = ImportCoreService
						.conversionSubsidiaryFindNo(sequenceStr);
			}

		}
		if (sequenceStr == null
				|| (sequenceStr != null && sequenceStr.trim().length() < 1)) {
			sequenceStr = "";
		}
		
		try {
			subsidiaryBOPLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO,
					sequenceStr);
		} catch (Exception e) {
			String message = "Subsisiary seq no setting error ["+operationItemId+" -> "+subsidiaryItemId+"]"+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.subsidiaryData.setStatus(TCData.STATUS_ERROR, message);
		}

		// 조구분 등록
		String groupConditionStr = null;
		if (bomLineDataElement.getElementsByTagName("N") != null) {
			if (bomLineDataElement.getElementsByTagName("N").getLength() > 0) {
				groupConditionStr = bomLineDataElement
						.getElementsByTagName("N").item(0).getTextContent();
			}
		}
		try {
			groupConditionStr = getPropertyString(groupConditionStr);
			subsidiaryBOPLine.setProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT,
					groupConditionStr);
		} catch (Exception e) {
			String message = "Subsisiary day & night setting error ["+operationItemId+" -> "+subsidiaryItemId+"]"+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.subsidiaryData.setStatus(TCData.STATUS_ERROR, message);
		}

		// 부자재 Option condition 등록
		String optionConditionStr = null;
		if (bomLineDataElement.getElementsByTagName("L") != null) {
			if (bomLineDataElement.getElementsByTagName("L").getLength() > 0) {
				optionConditionStr = bomLineDataElement
						.getElementsByTagName("L").item(0).getTextContent();
			}
			if (optionConditionStr != null
					&& optionConditionStr.trim().length() > 0) {
				optionConditionStr = getConversionOptionCondition(optionConditionStr);
			}
		}
		
		optionConditionStr = getPropertyString(optionConditionStr);
		if(optionConditionStr==null){
			optionConditionStr = "";
		}
		
		try {
			SDVBOPUtilities.updateAssiginOptionCondition(subsidiaryBOPLine,
					optionConditionStr);
		} catch (Exception e) {
			String message = "Option set error : ["+this.operationItemId+" -> "+this.subsidiaryItemId+"] "+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.subsidiaryData.setStatus(TCData.STATUS_ERROR, message);
		}

		subsidiaryBOPLine.save();
		subsidiaryData.setBopBomLine(subsidiaryBOPLine);

	}

	private void addBOMLine() {
		
		if(this.subsidiaryData.getDecidedChagneType()==TCData.DECIDED_REMOVE){
			return;
		}

		// BOMLine Attribute Update
		Element bomLineAttNode = (Element) subsidiaryData.getBomLineNode();
		// sequance
		String sequanceStr = null;
		if (bomLineAttNode.getElementsByTagName("O") != null) {
			if (bomLineAttNode.getElementsByTagName("O").getLength() > 0) {
				sequanceStr = bomLineAttNode.getElementsByTagName("O").item(0)
						.getTextContent();
			}
		}

		if (this.subsidiaryItem != null) {

			TCComponentBOMLine[] findedBOMLines = getCurrentBOPLine(
					this.operationBOPLine, subsidiaryItemId, sequanceStr);
			if (findedBOMLines != null && findedBOMLines.length > 0) {
				this.subsidiaryBOPLine = (TCComponentBOPLine) findedBOMLines[0];
			} else {
				subsidiaryData.setResourceItem(this.subsidiaryItem);
				ArrayList<InterfaceAIFComponent> subsidiaryDataList = new ArrayList<InterfaceAIFComponent>();
				subsidiaryDataList.add(this.subsidiaryItem);

				TCComponent[] resultBOMLineList = null;
				try {
					resultBOMLineList = SDVBOPUtilities.connectObject(
							operationBOPLine, subsidiaryDataList,
							SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (resultBOMLineList != null && resultBOMLineList.length > 0) {
					this.subsidiaryBOPLine = (TCComponentBOPLine) resultBOMLineList[0];
				}
			}

			// BOMLine 설정
			subsidiaryData.setBopBomLine(this.subsidiaryBOPLine);
		}

	}

	private void initTargetItem() {

		this.subsidiaryBOPLine = (TCComponentBOPLine) this.subsidiaryData
				.getBopBomLine();

		if (this.subsidiaryBOPLine == null) {

			Element subsidiaryBOMLineAttNode = (Element) this.subsidiaryData
					.getBomLineNode();
			// toolItemId
			if (subsidiaryBOMLineAttNode.getElementsByTagName("K") != null) {
				if (subsidiaryBOMLineAttNode.getElementsByTagName("K")
						.getLength() > 0) {
					subsidiaryItemId = subsidiaryBOMLineAttNode
							.getElementsByTagName("K").item(0).getTextContent();
				}
			}

			try {
				subsidiaryItem = SYMTcUtil.getItem(subsidiaryItemId);
				if (subsidiaryItem != null) {
					subsidiaryItemRevision = subsidiaryItem
							.getLatestItemRevision();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		} else {
			try {
				subsidiaryItemRevision = this.subsidiaryBOPLine
						.getItemRevision();
				subsidiaryItem = this.subsidiaryBOPLine.getItem();
				if (subsidiaryItem != null) {
					subsidiaryItemId = subsidiaryItem
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}

}
