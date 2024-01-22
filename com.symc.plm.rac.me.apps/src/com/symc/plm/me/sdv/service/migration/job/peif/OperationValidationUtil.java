package com.symc.plm.me.sdv.service.migration.job.peif;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;

public class OperationValidationUtil extends DefaultValidationUtil {
	
	private boolean isSameInstruction = true;
	private boolean fileNotFoundInVolume = false;

	public OperationValidationUtil(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean isValide(TCComponentBOMLine tcOperationBOMLine,
			Node operationBOMLineDataNode, Node operationMasterDataNode) {

		boolean isValide = true;

		clearStatusValues();

		if (tcOperationBOMLine == null) {
			bomLineNotFound = true;
		}
		if (operationBOMLineDataNode == null) {
			bomDataNotFoundFlag = true;
		}
		if (operationMasterDataNode == null) {
			masterDataNotFoundFlag = true;
		}
		
		// -------------------------------------
		// Node 추가변경 여부를 확인
		// -------------------------------------
		if (tcOperationBOMLine == null) {
			// 추가된 경우임
			validationResultChangeType = TCData.DECIDED_ADD;
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
		} else {
			if (operationBOMLineDataNode != null) {
				// 변경내용 비교 대상.
			} else {
				// 삭제된 경우임.
				validationResultChangeType = TCData.DECIDED_REMOVE;
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
				isValide = false;
			}
		}

		// 추가 또는 삭제된 Activity인 경우 더이상 비교는 무의미함.
		if (validationResultChangeType == TCData.DECIDED_REMOVE
				|| validationResultChangeType == TCData.DECIDED_ADD) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			return isValide;
		}

		// ----------------------------------------------------
		// 비교 기준에 따라 하나씩 비교 해 나간다.
		// ----------------------------------------------------

		// BOMLine Data를 읽는다.
		String tcItemId = "";
		String tcItemRevId = "";
		String tcSequenceStr = "";
		String tcQuantityStr = "";
		TCComponentItem tcOperationItem = null;
		TCComponentItemRevision tcOperationItemRevision = null;

		if (tcOperationBOMLine != null) {
			try {
				tcItemId = tcOperationBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				tcItemRevId = tcOperationBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
				tcSequenceStr = tcOperationBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
				tcQuantityStr = tcOperationBOMLine
						.getProperty(SDVPropertyConstant.BL_QUANTITY);
				tcOperationItem = tcOperationBOMLine.getItem();
				tcOperationItemRevision = tcOperationBOMLine.getItemRevision();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		// N/F Data를 읽는다.
		String nfPlantCode = getNodeFirstChildElementText(
				operationBOMLineDataNode, "B");
		String nfShopCode = getNodeFirstChildElementText(
				operationBOMLineDataNode, "H");
		String nfProductNo = getNodeFirstChildElementText(
				operationBOMLineDataNode, "D");
		String nfLineCode = getNodeFirstChildElementText(
				operationBOMLineDataNode, "E");
		String nfOperationItemId = ((Element) operationBOMLineDataNode)
				.getAttribute("OperationItemId");
		String nfOptionCondition = getNodeFirstChildElementText(
				operationBOMLineDataNode, "K");
		String nfOperationQuantityStr = getNodeFirstChildElementText(
				operationBOMLineDataNode, "L");
		String nfOperationSequenceStr = getNodeFirstChildElementText(
				operationBOMLineDataNode, "M");

		String nfVehicleCode = getNodeFirstChildElementText(
				operationMasterDataNode, "B");
		String nfLinePlantCode = getNodeFirstChildElementText(
				operationMasterDataNode, "C");
		String nfOperationCode = getNodeFirstChildElementText(
				operationMasterDataNode, "D");
		String nfFunctionCode = nfOperationCode.split("-")[0];
		String nfOpCode = nfOperationCode.split("-")[1];
		String nfOperationVersion = getNodeFirstChildElementText(
				operationMasterDataNode, "E");
		String nfOperationLocation = getNodeFirstChildElementText(
				operationMasterDataNode, "F");
		String nfKorName = getNodeFirstChildElementText(
				operationMasterDataNode, "G");
		String nfEngName = getNodeFirstChildElementText(
				operationMasterDataNode, "H");
		String nfWorkerSectionCode = getNodeFirstChildElementText(
				operationMasterDataNode, "I");
		String nfItemInputLocation = getNodeFirstChildElementText(
				operationMasterDataNode, "J");
		String nfInstructionDrawingNo = getNodeFirstChildElementText(
				operationMasterDataNode, "K");
		String nfStationNo = getNodeFirstChildElementText(
				operationMasterDataNode, "L");
		String nfSafetyCode = getNodeFirstChildElementText(
				operationMasterDataNode, "M");
		String nfSystemCode = getNodeFirstChildElementText(
				operationMasterDataNode, "N");
		String nfMaintainCode = getNodeFirstChildElementText(
				operationMasterDataNode, "O");
		String nfSequenceCode = getNodeFirstChildElementText(
				operationMasterDataNode, "P");
		String nfClassifyUnderBodyWork = getNodeFirstChildElementText(
				operationMasterDataNode, "Q");
		String nfClassifyRepresentVehicle = getNodeFirstChildElementText(
				operationMasterDataNode, "R");
		String nfWorkInstructionSheetPath = getNodeFirstChildElementText(
				operationMasterDataNode, "S");
		String nfWorkInstructionInterfaceOrNot = getNodeFirstChildElementText(
				operationMasterDataNode, "T");

		// Validation 결과를 출력하는데 필요한 Data 생성
		String operationItemId = null;
		if (operationItemId == null && tcItemId != null) {
			operationItemId = tcItemId;
		}
		if (operationItemId == null && nfOperationItemId != null) {
			operationItemId = nfOperationItemId;
		}
		String targetType = "Operation";
		String targetItemId = operationItemId;

		// ----------------------------------------------------
		// 비교 기준에 따라 하나씩 비교 해 나간다.
		// ----------------------------------------------------

		// 기존 Code의 Operation 관련 Validation 요소 확인
		// 1. Excel 공법서
		String tcOperationWorkInstructionPath = null;
		TCComponentDataset workInstructionDataset = null;
		File workInstructionFile = null;
		fileNotFoundInVolume = false;
		if(tcOperationItemRevision!=null){
			TCComponentDataset[] datasets = findKorWorkSheetTcDataset(tcOperationItemRevision);
			if(datasets!=null && datasets.length>0){
				workInstructionDataset = datasets[0];
			}

			if(workInstructionDataset!=null){
				try {
					workInstructionFile = getExcelFile((String)null, workInstructionDataset);
				} catch (Exception e) {
					fileNotFoundInVolume = true;
				}
			}
			if(workInstructionFile!=null && workInstructionFile.exists()==true){
				tcOperationWorkInstructionPath = workInstructionFile.getPath();
			}
		}

		boolean nfIsIfTarget = false;
		if(nfWorkInstructionInterfaceOrNot!=null && nfWorkInstructionInterfaceOrNot.trim().equalsIgnoreCase("TRUE")==true){
			nfIsIfTarget = true;
		}

		
		String message = "#### W/I Update target : "+operationItemId+" -> "+nfWorkInstructionInterfaceOrNot +"("+nfIsIfTarget+")";
		System.out.println(message);
		peIFExecution.writeLogTextLine(message);

		
		// 2. Worker Code
		String tcWorkerCode = null;
		// 3. Operation Process Seq
		String tcOperationProcessSeq = null;
		// 4. Operation Work Area
		String tcOperationWorkArea = null;
		String tcOperationFindNo = null;

		if (tcOperationItem != null) {
			try {
				tcWorkerCode = tcOperationItem
						.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE);
				tcOperationProcessSeq = tcOperationItem
						.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ);
				tcOperationWorkArea = tcOperationItem
						.getProperty(SDVPropertyConstant.OPERATION_WORKAREA);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		// 5. KOR Operation Name
		String tcOperationKorName = null;
		// 6. ENG Operation Name
		String tcOperationEngName = null;
		// 7. Vehicle Code
		String tcOperationrevVehicleCode = null;
		// 8. Shop
		String tcOperationRevShop = null;
		// 9. Function Code
		String tcOperationRevFunctionCode = null;
		// 10. Operation Code
		String tcOperationRevOperationCode = null;
		// 11. BOP Version
		String tcOperationRevBopVersion = null;
		// 12. Station No
		String tcOperationRevStationNo = null;
		// 13. Product Code
		String tcOperationRevProductCode = null;
		// 14. Install Drawing No
		// OPERATION_REV_INSTALL_DRW_NO 는 TC에서 배열 타입으로 존재, PE에서는 단건으로 존재하므로 1건으로
		// TC에서 추출하여 PE와 비교한다.
		String tcOperationRevInstallDwgNo[] = null;
		if (tcOperationItemRevision != null) {
			try {
				tcOperationKorName = tcOperationItemRevision
						.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME);
				tcOperationEngName = tcOperationItemRevision
						.getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
				tcOperationrevVehicleCode = tcOperationItemRevision
						.getProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
				tcOperationRevShop = tcOperationItemRevision
						.getProperty(SDVPropertyConstant.OPERATION_REV_SHOP);
				tcOperationRevFunctionCode = tcOperationItemRevision
						.getProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
				tcOperationRevOperationCode = tcOperationItemRevision
						.getProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
				tcOperationRevBopVersion = tcOperationItemRevision
						.getProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
				tcOperationRevStationNo = tcOperationItemRevision
						.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
				tcOperationRevProductCode = tcOperationItemRevision
						.getProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE);
				tcOperationRevInstallDwgNo = tcOperationItemRevision
						.getTCProperty(
								SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO)
						.getStringValueArray();
			} catch (TCException e) {
				e.printStackTrace();
			}
			
		}

		// 15. Option Condition
		String tcOpertationBOMLineMVLCondition = null;

		// 16. Find No
		String tcOpertationSeqNo = null;
		String nfFindNo = null;
		if (tcOperationBOMLine != null) {
			try {
				tcOpertationBOMLineMVLCondition = tcOperationBOMLine
						.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
				tcOpertationSeqNo = tcOperationBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		// N/F에 있는 값을 조합해서 Find No를 만든다.
		String stationNo = null;
		String workerCode = null;
		String seqCode = null;
		if (nfStationNo != null && nfStationNo.trim().length() > 0) {
			stationNo = nfStationNo.replace("-", "");
		}
		if (nfWorkerSectionCode != null
				&& nfWorkerSectionCode.trim().length() > 0) {
			workerCode = nfWorkerSectionCode.replace("-", "");
		}

		if (stationNo == null
				|| (stationNo != null && stationNo.trim().length() < 1)
				|| (stationNo != null && stationNo.trim().equalsIgnoreCase(
						"NULL") == true)) {

		} else if (workerCode == null
				|| (workerCode != null && workerCode.trim().length() < 1)
				|| (workerCode != null && workerCode.trim().equalsIgnoreCase(
						"NULL") == true)) {

		} else if (nfSequenceCode == null
				|| (nfSequenceCode != null && nfSequenceCode.trim().length() < 1)
				|| (nfSequenceCode != null && nfSequenceCode.trim()
						.equalsIgnoreCase("NULL") == true)) {

		} else {
			nfFindNo = stationNo.concat("|").concat(workerCode).concat("|")
					.concat(nfSequenceCode);
		}

		// 00. Operation Id
		boolean isSameId = true;
		if (isSame(tcItemId, nfOperationItemId) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameId = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Operation Id", isSameId);

		// 2. Worker Code
		boolean isSameWorkerCode = true;
		if (isSame(tcWorkerCode, nfWorkerSectionCode) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isMasterDataChanged = true;
			isSameWorkerCode = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Worker Code", isSameWorkerCode);

		// 3. Find No.
		boolean isSameOperationProcessSeq = true;
		if (isSame(tcSequenceStr, nfFindNo) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isMasterDataChanged = true;
			isSameOperationProcessSeq = false;
			isValide = false;
			
			peIFExecution.writeLogTextLine("tcSequenceStr = "+ tcSequenceStr + 
					", nfFindNo=" + nfFindNo);
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Find No", isSameOperationProcessSeq);

		// 3-1. Process Seq -> Operation Item의 Attribute 임.
		boolean isSameProcessSeq = true;
		if (isSame(tcOperationProcessSeq, nfSequenceCode) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isMasterDataChanged = true;
			isSameProcessSeq = false;
			isValide = false;
			
			String kkMessage = "tcOperationProcessSeq=" + tcOperationProcessSeq
					+ ", nfSequenceCode=" + nfSequenceCode;
			peIFExecution.writeLogTextLine(kkMessage);
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Process Seq", isSameProcessSeq);
		
		// 4. Operation Work Area
		boolean isSameOperationWorkArea = true;
		if (isSame(tcOperationWorkArea, nfOperationLocation) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isMasterDataChanged = true;
			isSameOperationWorkArea = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Operation Work Area", isSameOperationWorkArea);

		// 5. KOR Operation Name
		boolean isSameKORName = true;
		if (isSame(tcOperationKorName, nfKorName) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isMasterDataChanged = true;
			isSameKORName = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"KOR Operation Name", isSameKORName);

		// 6. ENG Operation Name
		boolean isSameENGName = true;
		if (isSame(tcOperationEngName, nfEngName) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameENGName = false;
			isMasterDataChanged = true;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"ENG Operation Name", isSameENGName);

		// 7. Vehicle Code
		boolean isSameVehicleCode = true;
		if (isSame(tcOperationrevVehicleCode, nfVehicleCode) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameVehicleCode = false;
			isMasterDataChanged = true;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Vehicle Code", isSameVehicleCode);

		// 8. Shop
		boolean isSameShop = true;
		if (isSame(tcOperationRevShop, nfShopCode) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameShop = false;
			isBOMAttributeChanged = true;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Shop", isSameShop);

		// 9. Function Code
		boolean isSameFunctionCode = true;
		if (isSame(tcOperationRevFunctionCode, nfFunctionCode) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameFunctionCode = false;
			isMasterDataChanged = true;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Function Code", isSameFunctionCode);

		// 10. Operation Code
		boolean isSameOperationCode = true;
		if (isSame(tcOperationRevOperationCode, nfOpCode) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameOperationCode = false;
			isMasterDataChanged = true;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Operation Code", isSameOperationCode);

		// 11. BOP Version
		boolean isSameBOPVersion = true;
		if (isSame(tcOperationRevBopVersion, nfOperationVersion) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameBOPVersion = false;
			isMasterDataChanged = true;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"BOP Version", isSameBOPVersion);

		// 12. Station No
		boolean isSameStationNo = true;
		if (isSame(tcOperationRevStationNo, nfStationNo) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameStationNo = false;
			isMasterDataChanged = true;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Station No", isSameStationNo);

		// 13. Product Code
		boolean isSameProductCode = true;
		if (isSame(tcOperationRevProductCode, nfProductNo) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameProductCode = false;
			isBOMAttributeChanged = true;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Product Code", isSameProductCode);

		// 14. Install Drawing No
		boolean isSameInstallDrawingNo = true;
		if (tcOperationRevInstallDwgNo == null
				|| (tcOperationRevInstallDwgNo != null && tcOperationRevInstallDwgNo.length < 1)) {
			tcOperationRevInstallDwgNo = new String[] { "" };
		} else {
			for (int i = 0; tcOperationRevInstallDwgNo != null
					&& i < tcOperationRevInstallDwgNo.length; i++) {
				String tempStr = tcOperationRevInstallDwgNo[i];

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
				tcOperationRevInstallDwgNo[i] = tempStr;
			}
		}

		// N/F에서 읽은 Data를 이용해 비교를 위한 Data를 만든다.
		String[] dwgNoArray = null;
		if (nfInstructionDrawingNo != null) {
			dwgNoArray = nfInstructionDrawingNo.split("/");
		}
		ArrayList<String> dwgNoList = new ArrayList<String>();
		if (dwgNoArray != null && dwgNoArray.length > 0) {
			for (String dwg : dwgNoList) {
				dwg = BundleUtil.nullToString(dwg).trim();
				if (!"".equals(dwg)) {
					dwgNoList.add(dwg);
				}
			}
		}
		String[] peOperationRevInstallDwgNo = null;
		if (dwgNoList != null && dwgNoList.size() > 0) {
			peOperationRevInstallDwgNo = dwgNoList.toArray(new String[dwgNoList
					.size()]);
		} else {
			peOperationRevInstallDwgNo = new String[] { "" };
		}

		// Data 비교를 위한 Consome Out 출력 (Test)
//		if (tcOperationRevInstallDwgNo == null
//				|| (tcOperationRevInstallDwgNo != null && tcOperationRevInstallDwgNo.length < 1)) {
//			System.out.println("tcOperationRevInstallDwgNo = NULL");
//		} else {
//			for (int i = 0; tcOperationRevInstallDwgNo != null
//					&& i < tcOperationRevInstallDwgNo.length; i++) {
//				System.out.println("tcOperationRevInstallDwgNo[" + i + "] = "
//						+ tcOperationRevInstallDwgNo[i]);
//			}
//		}

//		if (peOperationRevInstallDwgNo == null
//				|| (peOperationRevInstallDwgNo != null && peOperationRevInstallDwgNo.length < 1)) {
//			System.out.println("peOperationRevInstallDwgNo = NULL");
//		} else {
//			for (int i = 0; peOperationRevInstallDwgNo != null
//					&& i < peOperationRevInstallDwgNo.length; i++) {
//				System.out.println("peOperationRevInstallDwgNo[" + i + "] = "
//						+ peOperationRevInstallDwgNo[i]);
//			}
//		}

		// Install Drawing No를 비교한다.
		String[] defferOperationRevInstallDwgNo = getDefferenceData(
				tcOperationRevInstallDwgNo, peOperationRevInstallDwgNo);
		if (defferOperationRevInstallDwgNo != null) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameInstallDrawingNo = false;
			isMasterDataChanged = true;
			isValide = false;
		}

		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Install Drawing No", isSameInstallDrawingNo);

		// 15. Option Condition
		boolean isSameOptionCondition = true;
		String peOpertationBOMLineMVLCondition = null;
		try {
			peOpertationBOMLineMVLCondition = BundleUtil.nullToString(
					getConversionOptionCondition(nfOptionCondition)).trim();
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] defferBOMLineMVLCondition = getDefferenceData(
				tcOpertationBOMLineMVLCondition,
				peOpertationBOMLineMVLCondition);
		if (defferBOMLineMVLCondition != null) {
			
			peIFExecution.writeLogTextLine("tcOpertationBOMLineMVLCondition="+tcOpertationBOMLineMVLCondition+", peOpertationBOMLineMVLCondition="+peOpertationBOMLineMVLCondition);
			
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameOptionCondition = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Option Condition", isSameOptionCondition);

		// 16. Find No
		boolean isSameFindNo = true;
		if (isSame(tcOpertationSeqNo, nfFindNo) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameFindNo = false;
			isValide = false;
			String kkMessage = "tcOpertationSeqNo=" + tcOpertationSeqNo
					+ ", nfFindNo=" + nfFindNo;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Find No", isSameFindNo);
		
		// 17. WorkInstruction File (ExcelFile)
		// Validation -> Execution 실행이후에 Validation해도 Invalide 한것으로 나온다.
		isSameInstruction = true;
		boolean havePEWorkInstruction = true;
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		
		if(nfIsIfTarget==true){
			File uploadTargetWorkInstructionSheetFile = null;
			//System.out.println("nfWorkInstructionSheetPath("+operationItemId+") : "+nfWorkInstructionSheetPath);
			
			if(nfWorkInstructionSheetPath!=null && nfWorkInstructionSheetPath.trim().length()>0){
				uploadTargetWorkInstructionSheetFile = new File(nfWorkInstructionSheetPath);
			}
			
			if(uploadTargetWorkInstructionSheetFile!=null && uploadTargetWorkInstructionSheetFile.exists()==true){
				
				// 파일이 있으며 Upload 대상임.
				Long newFileLastModified = uploadTargetWorkInstructionSheetFile.lastModified();
				Date newFileLastModifiedDate = new Date(newFileLastModified);
				Date tcFileLastModifiedDate = getFileLastModifiedDate(workInstructionDataset);
				
				if(tcFileLastModifiedDate!=null && tcFileLastModifiedDate.before(newFileLastModifiedDate)==true){
					isSameInstruction = false;
				}if(tcFileLastModifiedDate==null && newFileLastModifiedDate!=null){
					isSameInstruction = false;
				}
				
				//System.out.println("isSameInstruction = "+isSameInstruction);
				
			}else{
				// Interface 대상이지만 PE에서 저장된 File이 없는 경우 이것을 어떻게 처리 하지?
				isSameInstruction = false;
				havePEWorkInstruction = false;
				haveMajorError = true;
			}
		}
		
		String message2 = "@@@@ W/I i/f target "+operationItemId+" = "+nfIsIfTarget+" "+nfWorkInstructionSheetPath;
		System.out.println(message2);
		peIFExecution.writeLogTextLine(message2);
		
		//System.out.println("isSameInstruction = "+isSameInstruction);
		if (isSameInstruction == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameInstallDrawingNo = false;
			isMasterDataChanged = true;
			isValide = false;
			
//			String kkMessage = "Work instruction must be changed.";
//			peIFExecution.writeLogTextLine(kkMessage);
			
			// PE Work Instruction 관련 Message가 유사한것이 중복되어 보이지 않도록 처리함.
			if(havePEWorkInstruction!=false){
				printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
						"Work instruction", isSameInstruction);
			}
			// 작표 I/F 대상 임에도 불구하고 PE에서 저장된 파일이 없는경우 Message를 추가로 출력 하도록 한다. 
			printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
					"Work instruction file exist? ("+nfWorkInstructionSheetPath+")", havePEWorkInstruction );			
		}
		
		// Server의 Volume에서 File을 찾지 못하면 중요한 오류로 처리해 Interface 대상에서 제되 되도록 한다.
		if(fileNotFoundInVolume==true){
			haveMajorError = true;
			printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
					"Work instruction in volume", false);
		}
		
		
		if(tcOperationItemRevision!=null){
			boolean isReleased = false;
			try {
				isReleased = SYMTcUtil.isReleased(tcOperationItemRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
			
			boolean isChangeAble = false;
			Vector<String> grantedPrevilegeV = SYMTcUtil.getGrantedPrivilegeNameVector(tcOperationItemRevision);
			// DELETE, READ, WRITE, CHANGE, MARKUP, COPY, CHANGE_OWNER, EXPORT, IMPORT, PUBLISH, DIGITAL_SIGN...
			if(grantedPrevilegeV!=null && grantedPrevilegeV.contains("CHANGE")){
				// 개정 가능
				isChangeAble = true; 
			}
			
			if(isReleased==true && isChangeAble==false ){
				this.haveMajorError = true;
				printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
						"Have no change write", false);
			}
			
		}
		
		if(isValide==true){
			if (getCompareResult() != DefaultValidationUtil.COMPARE_RESULT_DIFFERENT) {
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_EQUAL);
				validationResultChangeType = TCData.DECIDED_NO_CHANGE;
			}
		}

		return isValide;
	}
	
	public static TCComponentDataset[] findKorWorkSheetTcDataset(TCComponentItemRevision itemRevision){
		
		TCComponentDataset[] datasets = null;
		Vector<TCComponentDataset> datasetV = new Vector<TCComponentDataset>();
		try {
			AIFComponentContext[] childContexts = itemRevision.getChildren(new String[]{SDVTypeConstant.PROCESS_SHEET_KO_RELATION});
			for (int i = 0; childContexts!=null && i < childContexts.length; i++) {
				AIFComponentContext currentContext = childContexts[i];
				if(currentContext!=null){
					TCComponent component = (TCComponent)currentContext.getComponent();
					if(component!=null && component instanceof TCComponentDataset){
						if(datasetV.contains((TCComponentDataset)component)==false){
							datasetV.add((TCComponentDataset)component);
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		if(datasetV!=null && datasetV.size()>0){
			datasets = new TCComponentDataset[datasetV.size()];
		}
		for (int i = 0; datasetV!=null && i < datasetV.size(); i++) {
			datasets[i] = datasetV.get(i);
		}
		
		return datasets;
	}
	
	public static Date getFileLastModifiedDate(TCComponentDataset dataset){
		Date modifiedDate = null;
		
		TCComponentTcFile[] tcFiles = null;
		if(dataset!=null){
			try {
				tcFiles = dataset.getTcFiles();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		// Dataset에 포함된 조립작업표준서 엑셀 파일이 있는지 확인하고 File을 가져온다.
		for (int i = 0; tcFiles!=null && i < tcFiles.length; i++) {
			
			File tempLocalFile = null;
			String tempOrgFileName = null;
			
			try {
				tempOrgFileName = tcFiles[i].getProperty("original_file_name");
			} catch (TCException e) {
				e.printStackTrace();
			}
            String[] orgFileNameSplit = tempOrgFileName.split("[.]");
            String extFileName = orgFileNameSplit[orgFileNameSplit.length - 1];

    		File fmsFile = null;
    		try {
				fmsFile = tcFiles[i].getFmsFile();
			} catch (TCException e) {
				
				String errorMessageText = "============================\n"
					+ "Error get file : " + dataset+"\n"
					+ "Error Code : "+e.getErrorCode()+", Severity : "+e.getSeverity()+"\n"
					+"Detail Message : "+e.getDetailsMessage()+"\n"
					+"============================\n";
				
				System.out.println(errorMessageText);
			}

			if(extFileName!=null ){
				if(extFileName.trim().equalsIgnoreCase("xls") || extFileName.trim().equalsIgnoreCase("xlsx")){

					try {
						modifiedDate = tcFiles[i].getDateProperty("last_mod_date");
					} catch (TCException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
		return modifiedDate;
	}
	
	public static File getExcelFile(String targetDirPath, TCComponentDataset dataset) throws Exception{
		
		// Dataset의 Excel File
		File targetExcelFile = null;
		String targetExcelFileName = null;

		TCComponentTcFile[] tcFiles = null;
		try {
			tcFiles = dataset.getTcFiles();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		// Dataset에 포함된 조립작업표준서 엑셀 파일이 있는지 확인하고 File을 가져온다.
		for (int i = 0; tcFiles!=null && i < tcFiles.length; i++) {
			
			File tempLocalFile = null;
			String tempOrgFileName = null;
			
			try {
				tempOrgFileName = tcFiles[i].getProperty("original_file_name");
			} catch (TCException e) {
				e.printStackTrace();
			}
            String[] orgFileNameSplit = tempOrgFileName.split("[.]");
            String extFileName = orgFileNameSplit[orgFileNameSplit.length - 1];

    		File fmsFile = null;
    		try {
				fmsFile = tcFiles[i].getFmsFile();
			} catch (TCException e) {
				
				String errorMessageText = "============================\n"
					+ "Error get file : " + dataset+"\n"
					+ "Error Code : "+e.getErrorCode()+", Severity : "+e.getSeverity()+"\n"
					+"Detail Message : "+e.getDetailsMessage()+"\n"
					+"============================\n";
				
				System.out.println(errorMessageText);
				
				throw new Exception("The file could not be found in the server.");
			}

    		if(fmsFile!=null){
    			if(extFileName!=null ){
    				if(extFileName.trim().equalsIgnoreCase("xls") || extFileName.trim().equalsIgnoreCase("xlsx")){
    					targetExcelFileName = tempOrgFileName.trim();
    					try {
    						tempLocalFile = tcFiles[i].getFile(targetDirPath, tempOrgFileName );
    					} catch (TCException e) {
    						e.printStackTrace();
    					}
    				}
    			}
    		}
            
            if(tempLocalFile!=null && tempLocalFile.exists()==true){
            	targetExcelFile = tempLocalFile;
            	break;
            }

		}
		
		return targetExcelFile;
		
	}

	/**
	 * 데이터 비교
	 * 
	 * 주의 : double인 경우 소숫점 10자리 이하 절삭
	 * 
	 * @method getDefferenceData
	 * @date 2013. 12. 11.
	 * @param
	 * @return String[]
	 * @exception
	 * @throws
	 * @see
	 */
	private String[] getDefferenceData(Object tcData, Object peData) {
		// double인 경우 소숫점 10자리 이하 절삭
		if (tcData instanceof Double) {
			tcData = (new Double(Double.parseDouble(longDouble2String(10,
					(Double) tcData)))).toString();
			peData = (new Double(Double.parseDouble(longDouble2String(10,
					(Double) peData)))).toString();
			if (!(tcData.equals(peData))) {
				String[] defferData = new String[2];
				defferData[0] = tcData.toString();
				defferData[1] = peData.toString();
				return defferData;
			}
		} else if (tcData instanceof String[]) {
			String[] tcStrings = (String[]) tcData;
			String[] peStrings = (String[]) peData;
			if (tcStrings.length != peStrings.length) {
				String[] defferData = new String[2];
				defferData[0] = Arrays.toString(tcStrings);
				defferData[1] = Arrays.toString(peStrings);
				return defferData;
			} else {
				for (int i = 0; i < tcStrings.length; i++) {
					String tcValue = BundleUtil.nullToString(tcStrings[i]);
					boolean isSameData = false;
					for (int j = 0; j < peStrings.length; j++) {
						String peValue = BundleUtil.nullToString(peStrings[j]);
						if (tcValue.equals(peValue)) {
							isSameData = true;
							break;
						}
					}
					if (isSameData == false) {
						String[] defferData = new String[2];
						defferData[0] = Arrays.toString(tcStrings);
						defferData[1] = Arrays.toString(peStrings);
						return defferData;
					}
				}
				return null;
			}
		} else {
			// null 초기화
			if (tcData == null) {
				tcData = "";
			}
			if (peData == null) {
				peData = "";
			}
			if (!(tcData.equals(peData))) {
				String[] defferData = new String[2];
				defferData[0] = tcData.toString();
				defferData[1] = peData.toString();
				return defferData;
			}
		}
		return null;
	}

	/**
	 * 적당한 길이로 자른다.
	 * 
	 * @param size
	 * @param value
	 * @return
	 */
	private String longDouble2String(int size, double value) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(size);
		nf.setGroupingUsed(false);
		return nf.format(value);
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
	private String getConversionOptionCondition(String condition)
			throws TCException, Exception {
		return ImportCoreService.conversionOptionCondition(
				peIFExecution.getProcessWindow(),
				condition);
	}
	
	/**
	 * Tc에 저장된 기존 Workinstruction 파일과 
	 * @return
	 */
	public boolean isSameInstruction() {
		return this.isSameInstruction;
	}
	
	/**
	 * Validation 과정에 Server에 저장된 Dataset 파일이 없는경우 Operation을 개정할때 Error가 발생 되므로 미리
	 * Error 처리 할 수 있도록 하기위해 Volume에 File이 없는 경우 True를 Return 한다.
	 * @return
	 */
	public boolean isFileNotFoundInVolume() {
		return fileNotFoundInVolume;
	}
}
