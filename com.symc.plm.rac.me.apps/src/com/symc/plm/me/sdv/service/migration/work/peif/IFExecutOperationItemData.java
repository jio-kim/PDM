package com.symc.plm.me.sdv.service.migration.work.peif;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Element;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.job.peif.OperationValidationUtil;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.LineItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEOP;
import com.teamcenter.rac.kernel.TCComponentMEOPRevision;
import com.teamcenter.rac.kernel.TCException;

public class IFExecutOperationItemData extends IFExecutDefault {

//	private IDataSet dataSet;

	private TCComponentBOMLine oldOperationBOPLine;
	private TCComponentItem oldOperationItem;
	private TCComponentItemRevision oldMEOperationRevision;
	

	private TCComponentChangeItemRevision oldOperationChangeItemRevision;
	private String oldOperationMecoNo;

	boolean isNeedToBOMLineAdd = false;
	boolean isNeedToBOMLineReplace = false;

	public IFExecutOperationItemData(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	private static String DEFAULT_REV_ID = "000";

	@Override
	public boolean createOrUpdate(TCData tcData) {

		this.meOperationItem = null;
		this.meOperationRevision = null;
		this.operationBOPLine = null;

		this.operationItemData = (OperationItemData) tcData;

		isNeedToBOMLineAdd = false;
		isNeedToBOMLineReplace = false;

		operationItemId = null;
		oldOperationItem = null;
		oldMEOperationRevision = null;
		oldOperationChangeItemRevision = null;
		oldOperationMecoNo = null;
		
		// Reader Class를 통해 Operation 등록을위한 Data를 읽어 dataSet에 저장한다.
		Element operationBOMLineDataElement = (Element) this.operationItemData
				.getBomLineNode();
		Element operationNodeMasterData = (Element) this.operationItemData
				.getMasterDataNode();
		
//		this.dataSet = peIFExecution.getPeImportDataReaderUtil().
//				getOperationNodeIDataSet(
//					operationBOMLineDataElement, 
//					operationNodeMasterData
//				);

		initTargetItem();

		boolean isCreateTarget = false;
		boolean isReviseTarget = false;
		boolean isUpdateTarget = false;

		int changeType = this.operationItemData.getDecidedChagneType();
		boolean bomChanged = this.operationItemData.getBOMAttributeChangeFlag();
		boolean attributeChanged = this.operationItemData.getAttributeChangeFlag();
		
		if (changeType == TCData.DECIDED_NO_CHANGE && bomChanged==false) {
			
			System.out.println("Change Type : No Change");
			
			// 추가적인 처리 없이 Return
			return true;
		} else if (changeType == TCData.DECIDED_REMOVE) {
			
			boolean haveRemoveException = false;
			
			// 삭제 처리를 수행 한다.
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
			isNeedToBOMLineAdd = true;
			isUpdateTarget = true;
			bomChanged=true;
			attributeChanged=true;
		} else if (changeType == TCData.DECIDED_REVISE) {
			
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
			isReviseTarget = true;
			isUpdateTarget = true;
		} else if (changeType == TCData.DECIDED_REPLACE) {
			
			// 아래의 추가적인 Data 확인과 후속처리를 수행한다.
			isNeedToBOMLineReplace = true;
			isUpdateTarget = true;
		}
		
		if(bomChanged==true || attributeChanged==true ){
			isUpdateTarget = true;
		}
		
		if (this.oldOperationItem == null) {
			isCreateTarget = true;
		} else {
			if (this.oldOperationChangeItemRevision != null) {
				if (peIFExecution.getMecoRevision() != null
						&& peIFExecution.getMecoRevision().equals(
									this.oldOperationChangeItemRevision
								) == true) {
					// 동일한 MECO No
					this.meOperationItem = this.oldOperationItem;
					this.meOperationRevision = this.oldMEOperationRevision;
					if (this.oldOperationBOPLine != null) {
						this.operationBOPLine = (TCComponentBOPLine) this.oldOperationBOPLine;
					}
					isUpdateTarget = true;
				} else {
					// 다른 MECO NO
					isReviseTarget = true;
					isUpdateTarget = true;
				}
			} else {
				// MECO 없음 그냥 Update 하면 됨.
				isUpdateTarget = true;
			}
		}
		
		boolean haveCreateException = false;
		
		if (isCreateTarget == true) {
			try {
				createTargetObject();
				peIFExecution.waite();
				isUpdateTarget = true;
			} catch (TCException e) {
				
				haveCreateException = true;
				String message = "Operation create error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
			} catch (Exception e) {
				
				haveCreateException = true;
				String message = "Operation create error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
			}
		}
		
		boolean haveReviseException = false;
		if (isCreateTarget == false && isReviseTarget == true) {
			try {
				reviseTargetObject();
				peIFExecution.waite();
				isUpdateTarget = true;
			} catch (TCException e) {
				haveReviseException = true;

				String message = "Operation revise error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
				
			} catch (Exception e) {
				haveReviseException = true;
				
				String message = "Operation revise error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
			}
		}
		
		if ((haveCreateException== false && haveReviseException==false) && isNeedToBOMLineReplace == true) {
			// Operation을 변경 하는 경우가 있을까?
			if (this.operationBOPLine != null) {
				try {
					this.operationBOPLine.replace(this.meOperationItem,
							this.meOperationRevision, null);
					this.operationBOPLine.save();
					peIFExecution.waite();
				} catch (TCException e) {
					String message = "BOPLine replace Error ["+operationItemId+"] : "+e.getMessage();
					this.peIFExecution.writeLogTextLine(message);
					this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
				}
			}
		}

		boolean haveUpdateException = false;
		boolean isUpdate = true;
		
		if (isUpdateTarget == true) {
			
			if(isCreateTarget && haveCreateException== true){
				isUpdate = false;
			}
			if(isReviseTarget == true && haveReviseException==true){
				isUpdate = false;
			}
			
		}
		
		if(isUpdate==true){
			try {
				updateTargetObject();
				peIFExecution.waite();
			} catch (TCException e) {
				e.printStackTrace();
				haveUpdateException = true;
			} catch (Exception e) {
				e.printStackTrace();
				haveUpdateException = true;
			}
		}

		if (this.operationBOPLine != null) {
			this.operationItemData.setBopBomLine(this.operationBOPLine);
		}
		
		boolean isOk = true;
		if(isCreateTarget == true && haveCreateException == true){
			isOk = false;
		}
		if(isReviseTarget == true && haveReviseException == true){
			isOk = false;
		}
		if(isUpdateTarget==true && haveUpdateException == true){
			isOk = false;
		}

		return isOk;
	}

	private void initTargetItem() {

		this.oldOperationBOPLine = (TCComponentBOPLine) this.operationItemData
				.getBopBomLine();

		if (this.oldOperationBOPLine != null) {
			try {
				this.oldOperationItem = (TCComponentMEOP) this.oldOperationBOPLine
						.getItem();
				if (this.oldOperationItem != null) {
					this.operationItemId = this.oldOperationItem
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
				this.oldMEOperationRevision = (TCComponentMEOPRevision) this.oldOperationBOPLine
						.getItemRevision();
			} catch (TCException e) {
				e.printStackTrace();
			}
		} else {

			Element operationBOMLineDataElement = (Element) this.operationItemData
					.getBomLineNode();
			this.operationItemId = operationBOMLineDataElement
					.getAttribute("OperationItemId");

			// 존재 하는 Item 인경우 마지막 Item Revision을 찾는다.
			if (this.operationItemId != null) {
				try {
					this.oldMEOperationRevision = (TCComponentMEOPRevision) SYMTcUtil
							.getLatestedRevItem(operationItemId);
					if (this.oldMEOperationRevision != null) {
						this.oldOperationItem = (TCComponentMEOP) this.oldMEOperationRevision
								.getItem();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// 이미 존재하는 Item Revision의 MECO 정보를 확인.
		if (this.oldMEOperationRevision != null) {
			try {
				this.oldOperationChangeItemRevision = (TCComponentChangeItemRevision) oldMEOperationRevision
						.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
				if (oldOperationChangeItemRevision != null) {
					this.oldOperationMecoNo = oldOperationChangeItemRevision
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}

	public void createTargetObject() throws Exception, TCException {

		// Reader Class를 통해 Operation 등록을위한 Data를 읽어 dataSet에 저장한다.
		Element operationBOMLineDataElement = (Element) this.operationItemData
				.getBomLineNode();
		Element operationNodeMasterData = (Element) this.operationItemData
				.getMasterDataNode();

		// Operation Item을 생한다.
		if (this.meOperationItem == null && this.oldOperationItem == null) {
			
			String korName = null;
			
			if (operationNodeMasterData.getElementsByTagName("G") != null) {
				if (operationNodeMasterData.getElementsByTagName("G")
						.getLength() > 0) {
					korName = operationNodeMasterData
							.getElementsByTagName("G").item(0)
							.getTextContent();
				}
			}
			
			try {
				this.meOperationItem = null;
				this.meOperationItem = (TCComponentMEOP) SDVBOPUtilities
						.createItem(
								SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM,
								this.operationItemId, DEFAULT_REV_ID, korName,
								"");
			} catch (Exception e) {
				this.peIFExecution.writeLogTextLine("Operation Creation Error ["+operationItemId+"] : "+e.getMessage());
			}
			
			if (this.meOperationItem != null) {
				try {
					this.meOperationRevision = (TCComponentMEOPRevision) this.meOperationItem
							.getLatestItemRevision();
				} catch (TCException e) {
					this.peIFExecution.writeLogTextLine("Operation Revision Find Error ["+operationItemId+"] : "+e.getMessage());
				}
			}
			if (this.meOperationRevision != null) {
				try {
					this.meOperationRevision.setReferenceProperty(
							SDVPropertyConstant.OPERATION_REV_MECO_NO,
							peIFExecution.getMecoRevision());
				} catch (TCException e) {
					this.peIFExecution.writeLogTextLine("MECO Set Error ["+operationItemId+"] : "+e.getMessage());
				}
				
				// BOPLine이 생성되지 않은 상태이면 BOPLine을 생성한다.
				addBOMLine();
			}
		}

		if (this.meOperationRevision != null) {

			// 속성 Update
			try {
				updateOperationItemAndItemRevisionDataUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (this.meOperationRevision
					.isValidPropertyName(SDVPropertyConstant.S7_MATURITY)) {
				try {
					this.meOperationRevision.setProperty(
							SDVPropertyConstant.S7_MATURITY, "In Work");
				} catch (TCException e) {
					e.printStackTrace();
				}
			}

			// 공법Revision에 작업표준서를 붙임
			try {
				attachProcessExcelToOP(meOperationRevision, operationNodeMasterData);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/**
			 * MECO에 생성된 Item Revision을 붙임
			 */
			try {
				addRevisionToMecoRevision();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public void reviseTargetObject() throws Exception, TCException {

		System.out.println("this.oldMEOperationRevision = "
				+ this.oldMEOperationRevision);

		boolean isReleased = false;
		if (this.oldMEOperationRevision != null) {
			isReleased = SYMTcUtil.isReleased(this.oldMEOperationRevision);
		}

		if (isReleased == true) {

			// 개정 권한이 있는지 Check 한다.
			boolean isChangeAble = haveChangeAccessRight(this.oldMEOperationRevision);
			boolean haveVolumeError = this.operationItemData.isHaveWorkInstructionVolumeFileError();
			
			
			// Test 서버에서 Test 진행시 Dataset의 파일이 Volume에 없는 경우 개정할때 Exception이 발생된다.
			// 이것을 방지할 수 있는 방법이 있는지 고민해봐야 한다.
			// --> 개발서버가 아닌 실제 Server에서는 발생하지 않을 것임
			// --> 개발서버에서는 운영 Server에서 File을 Copy해서 등록하는 Admin 메뉴를 추가해서 사용하도록 함.
			// --> Validation에서 이런 경우 Error처리되어 붉은색으로 표기되고 Interface 제외 되도록 처리 했음.
			
			if (isChangeAble == true && haveVolumeError==false) {
				
				String newRevId = null;
				try {
					newRevId = this.oldMEOperationRevision.getItem()
							.getNewRev();
					if (newRevId != null) {
						this.meOperationRevision = this.oldMEOperationRevision
								.saveAs(newRevId);
						if (this.meOperationRevision != null) {
							
							this.meOperationRevision.lock();
							
							this.meOperationRevision.setReferenceProperty(
									SDVPropertyConstant.OPERATION_REV_MECO_NO,
									peIFExecution.getMecoRevision());
							
							this.meOperationRevision.save();
							
							this.meOperationRevision.unlock();
						}
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			} else {
				throw new Exception("You need change permissions or work instruction file is exist : "
						+ this.oldMEOperationRevision);
			}

		} else {
			this.meOperationRevision = this.oldMEOperationRevision;
		}

		if (this.meOperationItem == null && this.meOperationRevision!=null) {
			this.meOperationItem = this.meOperationRevision.getItem();
		}

		isReleased = false;
		if (this.meOperationRevision != null) {
			isReleased = isReleased(this.meOperationRevision);
		}

		if (this.meOperationRevision != null && isReleased == false) {

			if (this.meOperationRevision
					.isValidPropertyName(SDVPropertyConstant.S7_MATURITY)) {
				
				this.meOperationRevision.lock();
				
				try {
					this.meOperationRevision.setProperty(
							SDVPropertyConstant.S7_MATURITY, "In Work");
					this.meOperationRevision.save();
				} catch (TCException e) {
					e.printStackTrace();
				}
				
				this.meOperationRevision.unlock();
			}

			/**
			 * MECO에 생성된 Item Revision을 붙임
			 */
			try {
				addRevisionToMecoRevision();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public void removeTargetObject() throws Exception, TCException {
		if (this.operationBOPLine == null) {
			return;
		}

		TCComponentBOPLine lineBOPLine = (TCComponentBOPLine) this.operationBOPLine
				.parent();
		if (lineBOPLine != null) {
			if (haveWriteAccessRight(lineBOPLine) == true) {
				this.operationBOPLine.cut();
			} else {
				throw new Exception("You need write permissions. : "
						+ this.operationBOPLine);
			}
			lineBOPLine.save();
		}
	}

	private void updateOperationItemData(Element nfMasterDataElement) throws Exception {
		if (nfMasterDataElement == null) {
			return;
		}
		if (meOperationItem == null) {
			return;
		}

		try {
			this.meOperationItem.lock();
		} catch (Exception e) {

		}

		// 하체작업 여부 (U/Body Work)
		String isUnderBodyWork = null;
		if (nfMasterDataElement.getElementsByTagName("Q") != null) {
			if (nfMasterDataElement.getElementsByTagName("Q").getLength() > 0) {
				isUnderBodyWork = nfMasterDataElement.getElementsByTagName("Q")
						.item(0).getTextContent();
			}
		}
		if ((isUnderBodyWork != null && isUnderBodyWork.equalsIgnoreCase("Y")) == false) {
			isUnderBodyWork = "N";
		}

		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_WORK_UBODY, getPropertyString(isUnderBodyWork) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Ubody Error : "+isUnderBodyWork);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Ubody Error : "+e.getMessage());
		}

		// 작업위치 (Working Position)
		String operationWorkArea = null;
		if (nfMasterDataElement.getElementsByTagName("F") != null) {
			if (nfMasterDataElement.getElementsByTagName("F").getLength() > 0) {
				operationWorkArea = nfMasterDataElement
						.getElementsByTagName("F").item(0).getTextContent();
			}
		}

		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_WORKAREA, getPropertyString(operationWorkArea) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Worker area Error : "+operationWorkArea);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Worker area Error : "+e.getMessage());
			//e.printStackTrace();
		}

		// 작업자 구분코드 (Worker Code)
		String operationWorkerCode = null;
		if (nfMasterDataElement.getElementsByTagName("I") != null) {
			if (nfMasterDataElement.getElementsByTagName("I").getLength() > 0) {
				operationWorkerCode = nfMasterDataElement
						.getElementsByTagName("I").item(0).getTextContent();
			}
		}
		
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_WORKER_CODE,
					 getPropertyString(operationWorkerCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Worker code Error : "+operationWorkerCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Worker code Error : "+e.getMessage());
		}

		// 자재투입위치 상하 (Item Location-U/L)
		String operationItemUL = null;
		if (nfMasterDataElement.getElementsByTagName("J") != null) {
			if (nfMasterDataElement.getElementsByTagName("J").getLength() > 0) {
				operationItemUL = nfMasterDataElement.getElementsByTagName("J")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_ITEM_UL, getPropertyString(operationItemUL) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set UL Error : "+operationItemUL);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set UL Error : "+e.getMessage());
		}

		// Process Sequence
		String operationProcessSeq = null;
		if (nfMasterDataElement.getElementsByTagName("P") != null) {
			if (nfMasterDataElement.getElementsByTagName("P").getLength() > 0) {
				operationProcessSeq = nfMasterDataElement
						.getElementsByTagName("P").item(0).getTextContent();
			}
		}
		if ((operationProcessSeq != null && operationProcessSeq.trim().length() > 0) == false) {
			operationProcessSeq = "";
		}
		// Process Sequence 두자리 입력시 앞에 0 을 붙인다.
		if (operationProcessSeq.length() == 2) {
			operationProcessSeq = "0".concat(operationProcessSeq);
		}
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_PROCESS_SEQ,
					 getPropertyString(operationProcessSeq) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Op Seq Error : "+operationProcessSeq);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Op Seq Error : "+e.getMessage());
		}

		// 대표차종 유무 (Is Representative Vehicle)
		String operationRepVehicleCheck = null;
		if (nfMasterDataElement.getElementsByTagName("R") != null) {
			if (nfMasterDataElement.getElementsByTagName("R").getLength() > 0) {
				operationRepVehicleCheck = nfMasterDataElement
						.getElementsByTagName("R").item(0).getTextContent();
			}
		}
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK,
					getPropertyString(operationRepVehicleCheck) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set m7_REP_VHICLE_CHECK Error : "+operationRepVehicleCheck);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set m7_REP_VHICLE_CHECK Error : "+e.getMessage());
		}

		// Name (object_name)
		String operationName = null;
		if (nfMasterDataElement.getElementsByTagName("G") != null) {
			if (nfMasterDataElement.getElementsByTagName("G").getLength() > 0) {
				operationName = nfMasterDataElement.getElementsByTagName("G")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.ITEM_OBJECT_NAME, 
					getPropertyString(operationName) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Name Error : "+operationName);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Name Error : "+e.getMessage());
		}

		// Max Working Time 유무 (Is Representative Vehicle)
		// SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK

		try {
			this.meOperationItem.save();
			System.out.println("" + this.operationItemId
					+ " Item Data Save --- ");
		} catch (TCException e) {
			e.printStackTrace();
		}

		try {
			this.meOperationItem.unlock();
		} catch (Exception e) {

		}

	}

	private void updateOperationItemRevisionData(Element nfBOMLineDataElement,
			Element nfMasterDataElement) throws Exception {

		if (nfMasterDataElement == null) {
			return;
		}
		if (meOperationRevision == null) {
			return;
		}
		
		boolean isReleased = false;
		if (this.meOperationRevision != null) {
			try {
				isReleased = SYMTcUtil.isReleased(this.meOperationRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		if(isReleased==true){
			String exceptionMessage = "Error : "+this.meOperationRevision+" is released. (Can't property update!!)"; 
			System.out.println(exceptionMessage);
			return;
			//throw new Exception(exceptionMessage);
		}

		try {
			this.meOperationRevision.lock();
		} catch (Exception e) {

		}

		// Name (object_name)
		String operationName = null;
		if (nfMasterDataElement.getElementsByTagName("G") != null) {
			if (nfMasterDataElement.getElementsByTagName("G").getLength() > 0) {
				operationName = nfMasterDataElement.getElementsByTagName("G")
						.item(0).getTextContent();
			}
		}

		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.ITEM_OBJECT_NAME, 
					getPropertyString(operationName) );
			// this.meOperationRevision.setProperty(SDVPropertyConstant.OPERATION_REV_KOR_NAME,
			// operationName);
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Name Error : "+operationName);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Name Error : "+e.getMessage());
		}

		// 영문명 (English Name)
		String englishName = null;
		if (nfMasterDataElement.getElementsByTagName("H") != null) {
			if (nfMasterDataElement.getElementsByTagName("H").getLength() > 0) {
				englishName = nfMasterDataElement.getElementsByTagName("H")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_ENG_NAME, 
					getPropertyString(englishName) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Eng Name Error : "+englishName);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Eng Name Error : "+e.getMessage());
		}

		// vehicleCode
		String vehicleCode = null;
		if (nfMasterDataElement.getElementsByTagName("B") != null) {
			if (nfMasterDataElement.getElementsByTagName("B").getLength() > 0) {
				vehicleCode = nfMasterDataElement.getElementsByTagName("B")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision
					.setProperty(
							SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE,
							getPropertyString(vehicleCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Vehicle Code Error : "+vehicleCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Vehicle Code Error : "+e.getMessage());
		}

		// Shop Code
		String shopCode = null;
		if (nfMasterDataElement.getElementsByTagName("C") != null) {
			if (nfMasterDataElement.getElementsByTagName("C").getLength() > 0) {
				shopCode = nfMasterDataElement.getElementsByTagName("C")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_SHOP, 
					getPropertyString(shopCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Shop Error : "+shopCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Shop Error : "+e.getMessage());
		}

		// Function Code / Operation Code
		String functionCode = null;
		String operationCode = null;
		if (nfMasterDataElement.getElementsByTagName("D") != null) {
			if (nfMasterDataElement.getElementsByTagName("D").getLength() > 0) {
				operationCode = nfMasterDataElement.getElementsByTagName("D")
						.item(0).getTextContent();
			}
		}

		if ((operationCode != null && operationCode.trim().length() > 0) == false) {
			functionCode = "";
			operationCode = "";
		} else {
			functionCode = operationCode.split("-")[0];
			operationCode = operationCode.split("-")[1];
		}

		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE,
					getPropertyString(functionCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Function Code Error : "+functionCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Function Code Error : "+e.getMessage());
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_OPERATION_CODE,
					getPropertyString(operationCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Operation Code Error : "+operationCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Operation Code Error : "+e.getMessage());
		}

		// Assembly System
		String assySystem = null;
		if (nfMasterDataElement.getElementsByTagName("N") != null) {
			if (nfMasterDataElement.getElementsByTagName("N").getLength() > 0) {
				assySystem = nfMasterDataElement.getElementsByTagName("N")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, 
					getPropertyString(assySystem) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Assy System Error : "+assySystem);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Assy System Error : "+e.getMessage());
		}

		// 보안 (DR)
		String drCode = null;
		if (nfMasterDataElement.getElementsByTagName("M") != null) {
			if (nfMasterDataElement.getElementsByTagName("M").getLength() > 0) {
				drCode = nfMasterDataElement.getElementsByTagName("M").item(0)
						.getTextContent();
			}
		}
		
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_DR, 
					getPropertyString(drCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set DR Code Error : "+drCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set DR Code Error : "+e.getMessage());
		}

		// Station No
		String stationNo = null;
		if (nfMasterDataElement.getElementsByTagName("L") != null) {
			if (nfMasterDataElement.getElementsByTagName("L").getLength() > 0) {
				stationNo = nfMasterDataElement.getElementsByTagName("L")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_STATION_NO, 
					getPropertyString(stationNo) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Station No Error : "+stationNo);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Station No Error : "+e.getMessage());
		}

		// Install Drawing No
		String installDrawingNo = null;
		if (nfMasterDataElement.getElementsByTagName("K") != null) {
			if (nfMasterDataElement.getElementsByTagName("K").getLength() > 0) {
				installDrawingNo = nfMasterDataElement
						.getElementsByTagName("K").item(0).getTextContent();
			}
		}
		if ((installDrawingNo != null && installDrawingNo.trim().length() > 0) == false) {
			installDrawingNo = "";
		}

		String[] dwgNoArray = installDrawingNo.split("/");
		// OPERATION_REV_INSTALL_DRW_NO
		ArrayList<String> dwgNoList = new ArrayList<String>();
		if (dwgNoArray != null && dwgNoArray.length > 0) {
			for (String dwg : dwgNoList) {
				dwg = BundleUtil.nullToString(dwg).trim();
				if (!"".equals(dwg)) {
					dwgNoList.add(dwg);
				}
			}
		}

		dwgNoArray = dwgNoList.toArray(new String[dwgNoList.size()]);
		if (dwgNoList == null || (dwgNoList != null && dwgNoList.size() < 1)) {
			dwgNoArray = new String[] { "" };
		} else {
			for (int i = 0; dwgNoArray != null && i < dwgNoArray.length; i++) {
				String tempStr = dwgNoArray[i];

				if (tempStr == null
						|| (tempStr != null && tempStr.trim().length() < 1)) {
					tempStr = "";
				} else {
					if (tempStr.trim().equalsIgnoreCase("NULL")) {
						tempStr = "";
					} else {
						tempStr = tempStr.trim();
					}
				}
				dwgNoArray[i] = tempStr;
			}
		}

		try {
			this.meOperationRevision.getTCProperty(
					SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO)
					.setStringValueArray(dwgNoArray);
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Install Drawing No Error : "+dwgNoArray);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Install Drawing No Error : "+e.getMessage());
		}

		// Product Code
		String productCode = null;
		if (nfBOMLineDataElement.getElementsByTagName("D") != null) {
			if (nfBOMLineDataElement.getElementsByTagName("D").getLength() > 0) {
				productCode = nfBOMLineDataElement.getElementsByTagName("D")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision
					.setProperty(
							SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE,
							getPropertyString(productCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Product Code Error : "+productCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Product Code Error : "+e.getMessage());
		}

		// // Line Code
		// String lineCode = null;
		// if( nfBOMLineDataElement.getElementsByTagName("E") != null){
		// if( nfBOMLineDataElement.getElementsByTagName("E").getLength()>0 ){
		// lineCode =
		// nfBOMLineDataElement.getElementsByTagName("E").item(0).getTextContent();
		// }
		// }
		// if((lineCode!=null && lineCode.trim().length()>0)==false){
		// lineCode = "";
		// }
		// try {
		// this.meOperationRevision.setProperty(SDVPropertyConstant.OPERATION_REV_LINE,
		// lineCode);
		// } catch (TCException e) {
		// e.printStackTrace();
		// }

		// MECO No
		if(peIFExecution.getMecoRevision()!=null){
			try {
				this.meOperationRevision.setReferenceProperty(
						SDVPropertyConstant.OPERATION_REV_MECO_NO,
						peIFExecution.getMecoRevision());
			} catch (TCException e) {
				this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set MECO No Error : "+peIFExecution.getMecoRevision());
				this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set MECO No Error : "+e.getMessage());
			}
		}

		try {
			this.meOperationRevision.save();
			//System.out.println("" + this.operationItemId+ " Item Revision Data Save --- ");
		} catch (TCException e1) {
			//e1.printStackTrace();
		}

		try {
			this.meOperationRevision.unlock();
		} catch (Exception e) {

		}
		
		// Operation의 Work Instruction이 Update 대상인 경우 Excel을 Update 한다.
		if(this.operationItemData.isWorkInstructionUpdateTarget()==true){
			try {
				attachProcessExcelToOP(this.meOperationRevision, nfMasterDataElement);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// SDVPropertyConstant.OPERATION_REV_ALT_PREFIX
		// SDVPropertyConstant.OPERATION_REV_BOP_VERSION
		// SDVPropertyConstant.OPERATION_REV_IS_ALTBOP
		// SDVPropertyConstant.OPERATION_REV_KPC
		// SDVPropertyConstant.OPERATION_REV_MAX_WORK_TIME_CHECK
		// SDVPropertyConstant.OPERATION_REV_REP_VHICLE_CHECK
		// SDVPropertyConstant.OPERATION_REV_STATION_CODE

	}

	private void updateOperationItemAndItemRevisionDataUpdate() throws Exception {
		// -----------------------------------------------------------------------------
		// Operation Item 또는 Operation Item Revision의 Property 설정
		// -----------------------------------------------------------------------------
		Element nfBOMLineDataElement = (Element) operationItemData
				.getBomLineNode();
		Element nfMasterDataElement = (Element) operationItemData
				.getMasterDataNode();

		boolean isReleased = false;

		// Item Property Update
		if (this.meOperationRevision != null) {
			try {
				isReleased = SYMTcUtil.isReleased(this.meOperationRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
			if (isReleased == false) {
				// 쓰기 권한이 있는지 Check 한다.
				boolean isWriteAble = haveWriteAccessRight(this.meOperationItem);
				if (isWriteAble == true) {
					try {
						updateOperationItemData(nfMasterDataElement);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		// Item Revision Property Update
		isReleased = false;
		if (this.meOperationRevision != null) {
			try {
				isReleased = SYMTcUtil.isReleased(this.meOperationRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
			if (isReleased == false) {
				// 쓰기 권한이 있는지 Check 한다.
				boolean isWriteAble = haveWriteAccessRight(this.meOperationRevision);
				if (isWriteAble == true) {
					try {
						updateOperationItemRevisionData(nfBOMLineDataElement,
								nfMasterDataElement);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	public void updateTargetObject() throws Exception, TCException {

		System.out.println("Operatoin updateTargetObject() : " + this.operationItemId);

		// BOPLine이 생성되지 않은 상태이면 BOPLine을 생성한다.
		addBOMLine();

		// 속성 Update
		updateOperationItemAndItemRevisionDataUpdate();
		
		if(this.operationBOPLine==null){
			return;
		}

		boolean isReleased = false;
		boolean isWriteAble = false;
		try {
			isReleased = SYMTcUtil.isReleased(this.operationBOPLine);
			// 쓰기 권한이 있는지 Check 한다.
			if (isReleased == false) {
				isWriteAble = haveWriteAccessRight(this.operationBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (isReleased == true) {
			System.out.println("[" + this.operationItemData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;
		}else if(isWriteAble == false){
			System.out.println("[" + this.operationItemData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;			
		}

		// --------------------------------------------------------------------------------------------------------
		// BOMLine Attribute Update 한다.
		// --------------------------------------------------------------------------------------------------------
		String stationNo = meOperationRevision.getProperty(
				SDVPropertyConstant.OPERATION_REV_STATION_NO).replace("-", "");// 공정번호
		stationNo = getPropertyString(stationNo);

		String workerCode = meOperationItem.getProperty(
				SDVPropertyConstant.OPERATION_WORKER_CODE).replace("-", "");// 작업자코드
		workerCode = getPropertyString(workerCode);

		String seq = meOperationItem
				.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ);// 작업자
																		// 순번
		seq = getPropertyString(seq);

		// 수량 입력
		this.operationBOPLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");

		// 공정편성번호 입력
		boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty()
				|| seq.isEmpty(); // 하나라도 값이 없으면 반영안함
		String findNo = stationNo.concat("|").concat(workerCode).concat("|")
				.concat(seq);
		findNo = getPropertyString(findNo);
		if (findNo.length() > 15 || isExistEmptyValue) {
			;
		} else {
			this.operationBOPLine.setProperty(
					SDVPropertyConstant.BL_SEQUENCE_NO,
					getPropertyString(findNo) );
		}
		
		// Option Condition 설정
		Element nfBOMLineDataElement = (Element) operationItemData
				.getBomLineNode();
		String optionCode = null;
		if (nfBOMLineDataElement.getElementsByTagName("K") != null) {
			if (nfBOMLineDataElement.getElementsByTagName("K").getLength() > 0) {
				optionCode = nfBOMLineDataElement.getElementsByTagName("K")
						.item(0).getTextContent();
			}
		}
		if ((optionCode != null && optionCode.trim().length() > 0) == false) {
			optionCode = "";
		}
		
		System.out.println("optionCode = "+optionCode);
		
		String kk = getConversionOptionCondition(optionCode);

		System.out.println("optionCondition = "+kk);
		TCException optionConditionTCException = null;
		Exception optionConditionException = null;
		int exceptionType = 0;
		try {
			System.out.println("Update Operation ["+operationItemId+"] -> Option Condition");
			SDVBOPUtilities.updateOptionCondition(this.operationBOPLine,kk);
		} catch (TCException e) {
			optionConditionTCException = e;
			exceptionType = 1;
			//e.printStackTrace();
		} catch (Exception e) {
			optionConditionException = e;
			exceptionType = 2;
			//e.printStackTrace();
		}
		
//		System.out.println("KKKKKKKK-- 3 -- : optionConditionTCException = "+optionConditionTCException);
//		System.out.println("KKKKKKKK-- 3 -- : optionConditionException = "+optionConditionException);

		if(exceptionType==1){
			this.peIFExecution.writeLogTextLine(operationItemId+" have Major Exception [Option Condition Update] -> :\n"+optionConditionTCException.getDetailsMessage());
			this.operationItemData.setHaveMajorError(true);
		}
		if(exceptionType==2){
			this.peIFExecution.writeLogTextLine(operationItemId+" have Major Exception [Option Condition Update] -> :\n"+optionConditionException.getMessage());
			this.operationItemData.setHaveMajorError(true);
		}
		
		// [NON-SR][20160113] taeku.jeong Line, Station, Operation,
		// Operation에 bl_abs_occ_id 값을 설정한다.
		try {
			BOPLineUtility.updateLineToOperationAbsOccId(this.operationBOPLine);
		} catch (TCException e) {
			e.printStackTrace();
		}

		try {
			this.operationBOPLine.save();
			this.operationBOPLine.window().save();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		System.out.println("Operation Update End.............");

		this.operationItemData.setBopBomLine(this.operationBOPLine);
	}

	private void addBOMLine() {
		
		if(this.operationItemData.getDecidedChagneType()==TCData.DECIDED_REMOVE){
			return;
		}

		LineItemData lineItemData = (LineItemData) this.operationItemData
				.getParentItem();
		TCComponentBOPLine lineBOPLine = (TCComponentBOPLine) lineItemData
				.getBopBomLine();

		if (lineBOPLine == null) {
			return;
		}

		TCComponentBOMLine[] findedBOMLines = 
				getCurrentBOPLine(this.operationItemId, true, lineBOPLine);
		
		if (findedBOMLines != null && findedBOMLines.length > 0) {
			// Find Current Operation BOP Line
			this.operationBOPLine = (TCComponentBOPLine) findedBOMLines[0];
		} else {
			// OperationBOPLine이 등록되어 있지 않은 경우
			try {
				this.operationBOPLine = (TCComponentBOPLine) lineBOPLine.add(
						this.meOperationItem, this.oldMEOperationRevision,
						null, false);
				if (this.operationBOPLine != null) {
					this.operationBOPLine.setProperty(
							SDVPropertyConstant.BL_ABS_OCC_ID,
							this.operationItemId);
				}
				this.operationBOPLine.save();
			} catch (TCException e) {
				String message = "Operation BOMLine attach error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
			}
		}

		if (lineBOPLine!=null && this.operationBOPLine != null) {
			try {
				lineBOPLine.save();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		if(this.operationBOPLine!=null){
			this.operationItemData.setBopBomLine(this.operationBOPLine);
		}
		
	}

	/**
	 * 작업표준서 Excel Template 파일을 공법아래에 붙임
	 * 
	 * @method attachProcessExcelToOP
	 * @date 2013. 11. 21.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void attachProcessExcelToOP(TCComponentItemRevision opRevision, Element nfMasterDataElement)
			throws Exception {
		
		// 이부분은 무조건 실행되면 안된다.
		// Work Instruction 수정 대상이 아닐 수도 있기 때문이다.

		// Work Instruction I/F 대상인지 확인 한다.
		boolean isIFTarget = false;
		String isIFTargetString = null;
		if (nfMasterDataElement.getElementsByTagName("T") != null) {
			if (nfMasterDataElement.getElementsByTagName("T").getLength() > 0) {
				isIFTargetString = nfMasterDataElement.getElementsByTagName("T")
						.item(0).getTextContent();
			}
		}
		if ((isIFTargetString != null && isIFTargetString.equalsIgnoreCase("TRUE")) == true) {
			isIFTarget = true;
		}
		
		String message = "#### W/I Update target : "+operationItemId+" -> "+isIFTargetString +"("+isIFTarget+")";
		System.out.println(message);
		peIFExecution.writeLogTextLine(message);
		
		String nfFilePath = null;
		if (nfMasterDataElement.getElementsByTagName("S") != null) {
			if (nfMasterDataElement.getElementsByTagName("S").getLength() > 0) {
				nfFilePath = nfMasterDataElement.getElementsByTagName("S")
						.item(0).getTextContent();
			}
		}

		if(isIFTarget==false){
			return;
		}
		
		File nfWorkInstructionFile = null;
		if(nfFilePath!=null && nfFilePath.trim().length()>0){
			nfWorkInstructionFile = new File(nfFilePath.trim());
		}

		
		// 기존에 있는 DataSet이 있으면 삭제 하고 다시 추가 한다.
		boolean haveWorkInstructionUpdateTaget = false;
		TCComponentDataset dataSet = null;
		TCComponentDataset[] dataSets = OperationValidationUtil.findKorWorkSheetTcDataset(opRevision);
		if(dataSets!=null && dataSets.length>0){
			dataSet = dataSets[0];
		}
		
		if(dataSet!=null){
			File workInstructionFile = OperationValidationUtil.getExcelFile((String)null, dataSet);
			Date tcmodifiedDate = OperationValidationUtil.getFileLastModifiedDate(dataSet);
			
			if(workInstructionFile!=null && workInstructionFile.exists()==true){

				if(nfWorkInstructionFile!=null && nfWorkInstructionFile.exists()==true){
					
					
					// 파일이 있으며 Upload 대상임.
					Long newFileLastModified = nfWorkInstructionFile.lastModified();
					Date newFileLastModifiedDate = new Date(newFileLastModified);
					
					// TC에 기록된 Reference의 최종 수정일이 N/F 경로에 있는 work instruction 파일보다
					// 빠른 경우 Update 대상이 되는것임.
					if(tcmodifiedDate.before(newFileLastModifiedDate)==true){
						haveWorkInstructionUpdateTaget = true;
					}
				}
			}else{
				haveWorkInstructionUpdateTaget = true;
			}
		}else{
			haveWorkInstructionUpdateTaget = true;
		}
		
		message = "#### W/I Update target : "+operationItemId+" -> haveWorkInstructionUpdateTaget :  "+haveWorkInstructionUpdateTaget;
		System.out.println(message);
		peIFExecution.writeLogTextLine(message);
		
		// WorkInstruction을 Update 할 필요가 없는경우 더이상 진행  하지 않는다.
		if(haveWorkInstructionUpdateTaget==false){
			return;
		}
		
		// Work Instruction을 다시 Attache 해야 하므로 기존의 Work Instruction이 있으면 삭제 처리한다.
		if (dataSets != null && dataSets.length > 0) {
			try {
				opRevision.remove(SDVTypeConstant.PROCESS_SHEET_KO_RELATION,
						dataSets);
			} catch (TCException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < dataSets.length; i++) {
				try {
					dataSets[i].delete();
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
		
		String message2 = "#### W/I Update : "+operationItemId+" -> "+nfFilePath;
		System.out.println(message2);
		peIFExecution.writeLogTextLine(message2);

		if(nfWorkInstructionFile==null || (nfWorkInstructionFile!=null && nfWorkInstructionFile.exists()==false)){
			// Work Instruction Update 대상으로 되어 있지만 N/F Data에 지정된 File이 없는 경우 우선은 Template File을
			// Attache 한다.
			
			String itemId = opRevision
					.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			String revision = opRevision
					.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
			TCComponentDataset procDataSet = SDVBOPUtilities.getTemplateDataset(
					"M7_TEM_DocItemID_ProcessSheet_Kor", itemId + "/" + revision,
					itemId);
			opRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, procDataSet);
		}else if(nfWorkInstructionFile!=null && nfWorkInstructionFile.exists()==true){
			// N/F에 있는 File을 찾아서 Work Instruction Dataset을 생성하고 Attach 한다. 
			TCComponentDataset procDataSet = SDVBOPUtilities.createDataset(nfFilePath.trim());
			opRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, procDataSet);
		}
	}

	/**
	 * MECO에 생성된 Item Revision을 Solution Item에 붙인다.
	 * 
	 * @method AddRevisionToMecoRevision
	 * @date 2013. 11. 22.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void addRevisionToMecoRevision() throws Exception {
		
		TCComponentItemRevision mecoItemRevision = peIFExecution.getMecoRevision();

		if (this.meOperationRevision
				.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_MECO_NO)) {
			try {
				this.meOperationRevision.getTCProperty(
						SDVPropertyConstant.OPERATION_REV_MECO_NO)
						.setReferenceValue(mecoItemRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		// MECO에 연결하기
		// [NON-SR][2016.01.07] taeku.jeong PE->TC Migration Test 진행중
		// Exception발생으로 Problem, Solution Items에 이미 존재하는지 Check 하도록 수정함.
		if (this.oldMEOperationRevision!=null && 
				CustomUtil.isExistInProblemItems( 
							(TCComponentChangeItemRevision) peIFExecution.getMecoRevision(),
							this.oldMEOperationRevision
						) == false
				) {

			TCComponentItemRevision tempItemRevision = 
					(TCComponentItemRevision) this.meOperationRevision.getReferenceProperty(
				SDVPropertyConstant.OPERATION_REV_MECO_NO
					);
			if(tempItemRevision!=null && 
					tempItemRevision.equals(mecoItemRevision)==false){
				try {
					((TCComponentChangeItemRevision) mecoItemRevision).add(
							SDVTypeConstant.MECO_PROBLEM_ITEM,
							this.oldMEOperationRevision);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		if (this.meOperationRevision!=null && 
				CustomUtil.isExistInSolutionItems(
						(TCComponentChangeItemRevision) mecoItemRevision,
						this.meOperationRevision) == false) {
			try {
				((TCComponentChangeItemRevision) mecoItemRevision).add(
						SDVTypeConstant.MECO_SOLUTION_ITEM,
						this.meOperationRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}

}
