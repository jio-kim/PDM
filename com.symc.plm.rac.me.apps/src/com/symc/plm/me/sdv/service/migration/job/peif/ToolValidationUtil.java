package com.symc.plm.me.sdv.service.migration.job.peif;

import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;

public class ToolValidationUtil extends DefaultValidationUtil {

	public ToolValidationUtil(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean isValide(TCComponentBOMLine tcToolBOMLine,
			Node toolBOMLineDataNode, Node toolMasterDataNode) {

		boolean isValide = true;

		clearStatusValues();

		if (tcToolBOMLine == null) {
			bomLineNotFound = true;
		}
		if (toolBOMLineDataNode == null) {
			bomDataNotFoundFlag = true;
		}
		if (toolMasterDataNode == null) {
			masterDataNotFoundFlag = true;
		}

		// Validation 결과를 출력하는데 필요한 Data 생성
		String operationItemId = null;
		String toolItemid = null;
		if (toolBOMLineDataNode != null) {
			operationItemId = ((Element) toolBOMLineDataNode)
					.getAttribute("OperationItemId");
			toolItemid = getNodeFirstChildElementText(toolBOMLineDataNode, "K");
		}

		// -------------------------------------
		// Node 추가변경 여부를 확인
		// -------------------------------------
		if (tcToolBOMLine == null) {
			// 추가된 경우임
			validationResultChangeType = TCData.DECIDED_ADD;
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
		} else {
			if (toolBOMLineDataNode != null) {
				// 변경내용 비교 대상.
			} else {
				// 삭제된 경우임.
				validationResultChangeType = TCData.DECIDED_REMOVE;
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
				isValide = false;
			}
		}

		if (validationResultChangeType != TCData.DECIDED_REMOVE
				&& validationResultChangeType != TCData.DECIDED_NO_CHANGE) {

			if (toolMasterDataNode == null) {
				System.out.println("Master data not found : " + operationItemId
						+ "/" + toolItemid);
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
		String tcToolTorqueType = "";
		String tcToolTorQueValue = "";

		if (tcToolBOMLine != null) {
			try {
				tcItemId = tcToolBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				tcItemRevId = tcToolBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
				tcSequenceStr = tcToolBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
				tcQuantityStr = tcToolBOMLine
						.getProperty(SDVPropertyConstant.BL_QUANTITY);
				tcToolTorqueType = tcToolBOMLine
						.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE);
				tcToolTorQueValue = tcToolBOMLine
						.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		// N/F Data를 읽는다.
		String nfToolItemId = getNodeFirstChildElementText(toolBOMLineDataNode,
				"K");
		String nfToolQuantityStr = getNodeFirstChildElementText(
				toolBOMLineDataNode, "L");
		String nfToolTorqueStr = getNodeFirstChildElementText(
				toolBOMLineDataNode, "M");
		String nfToolSequenceStr = getNodeFirstChildElementText(
				toolBOMLineDataNode, "N");

		String nfMasterToolItemId = getNodeFirstChildElementText(
				toolMasterDataNode, "B");
		String nfToolKorName = getNodeFirstChildElementText(toolMasterDataNode,
				"C");
		String nfToolEngName = getNodeFirstChildElementText(toolMasterDataNode,
				"D");
		String nfLargeCategory = getNodeFirstChildElementText(
				toolMasterDataNode, "E");
		String nfMiddleCategory = getNodeFirstChildElementText(
				toolMasterDataNode, "F");
		String nfToolUsage = getNodeFirstChildElementText(toolMasterDataNode,
				"G");
		String nfToolSPECCode = getNodeFirstChildElementText(
				toolMasterDataNode, "H");
		String nfToolKorSPEC = getNodeFirstChildElementText(toolMasterDataNode,
				"I");
		String nfToolEngSPEC = getNodeFirstChildElementText(toolMasterDataNode,
				"J");
		String nfToolUnitOfCount = getNodeFirstChildElementText(
				toolMasterDataNode, "K");
		String nfMaterial = getNodeFirstChildElementText(toolMasterDataNode,
				"L");
		String nfTorque = getNodeFirstChildElementText(toolMasterDataNode, "M");
		String nfMakingCompany = getNodeFirstChildElementText(
				toolMasterDataNode, "N");
		String nfCompanyAF = getNodeFirstChildElementText(toolMasterDataNode,
				"O");
		String nfAppearanceClassification = getNodeFirstChildElementText(
				toolMasterDataNode, "P");
		String nfLengthString = getNodeFirstChildElementText(
				toolMasterDataNode, "Q");
		String nfConnectionSizeStr = getNodeFirstChildElementText(
				toolMasterDataNode, "R");
		String nfHaveMagnet = getNodeFirstChildElementText(toolMasterDataNode,
				"S");
		String nfRemarkStr = getNodeFirstChildElementText(toolMasterDataNode,
				"T");
		String nfCADFilePath = getNodeFirstChildElementText(toolMasterDataNode,
				"U");

		String targetType = "Tool";
		String targetItemId = null;
		if (tcItemId != null) {
			targetItemId = tcItemId;
		}
		if (targetItemId == null) {
			targetItemId = nfToolItemId;
		}

		// 1. 이름
		boolean isSameId = true;
		if (isSame(tcItemId, nfToolItemId) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameId = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Tool Id", isSameId);

		// 2. Sequence (숫자로 변경해서 비교 한다.)
		boolean isSameSeq = true;
		double tcSeqValue = -1;
		double nfSeqValue = -1;

		if (tcSequenceStr != null && tcSequenceStr.trim().length() > 0
				&& tcSequenceStr.trim().equalsIgnoreCase("NULL") == false) {
			// java.lang.NumberFormatException
			tcSeqValue = Double.parseDouble(tcSequenceStr.trim());
		}
		if (nfToolSequenceStr != null && nfToolSequenceStr.trim().length() > 0
				&& nfToolSequenceStr.trim().equalsIgnoreCase("NULL") == false) {
			nfSeqValue = Double.parseDouble(nfToolSequenceStr.trim());
		}
		if (tcSeqValue > -1 && tcSeqValue != nfSeqValue) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameSeq = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Sequence", isSameSeq);

		if (isSameSeq == true && isSameId == false) {
			isReplaced = true;
			isValide = false;
			validationResultChangeType = TCData.DECIDED_REPLACE;
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Tool Replace", !(isReplaced));

		// 3. Quantity (숫자로 변경해서 비교 한다.)
		double tcQuantityValue = 0.0;
		double nfQuantityValue = 0.0;
		boolean isSameQuantity = true;
		if (tcQuantityStr != null && tcQuantityStr.trim().length() > 0
				&& tcQuantityStr.trim().equalsIgnoreCase("NULL") == false) {
			tcQuantityValue = Double.parseDouble(tcQuantityStr.trim());
		}
		if (nfToolQuantityStr != null && nfToolQuantityStr.trim().length() > 0
				&& nfToolQuantityStr.trim().equalsIgnoreCase("NULL") == false) {
			nfQuantityValue = Double.parseDouble(nfToolQuantityStr.trim());
		}
		if (tcQuantityValue != nfQuantityValue) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameQuantity = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Quantity", isSameQuantity);

		// Torque 체크
		String tcToolTorQue = "";
		if (tcToolTorqueType == null
				|| (tcToolTorqueType != null && tcToolTorqueType.trim()
						.length() < 1)) {
			tcToolTorqueType = "";
		}
		if (!StringUtils.isEmpty(tcToolTorQueValue)) {
			tcToolTorQue = tcToolTorqueType + " " + tcToolTorQueValue;
		}
		if (tcToolTorQue != null) {
			tcToolTorQue = tcToolTorQue.trim();
		}
		boolean isSameTorque = true;
		if (isSame(tcToolTorQue, nfTorque) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameTorque = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Torque", isSameTorque);

		if(isValide==true){
			if (getCompareResult() != DefaultValidationUtil.COMPARE_RESULT_DIFFERENT) {
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_EQUAL);
				validationResultChangeType = TCData.DECIDED_NO_CHANGE;
			}
		}

		return isValide;
	}
}
