package com.symc.plm.me.sdv.service.migration.job.peif;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;

public class SubActivityValidationUtil extends DefaultValidationUtil {

	/**
	 * TC Activity 수와 PE I/F Activity수가 같지않으면 무조건 재등록
	 * 
	 * @param activitySubData
	 */

	public SubActivityValidationUtil(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean isValide(TCComponentMfgBvrOperation operationBOMLine,
			TCComponentMEActivity tcActivityComponent, Node activityLineNode,
			Node activityMasterDataNode, String activitySeqStr) {

		boolean isValide = true;

		clearStatusValues();

		// ----------------------------------------------------
		// 비교를 위한 Tc Attribute를 읽는다.
		// ----------------------------------------------------
		String activityCategory = getTCComponentStringPropertyValue(
				tcActivityComponent,
				SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);
		String activityWorkCode = getTCComponentStringPropertyValue(
				tcActivityComponent, SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
		String activityControlPoint = getTCComponentStringPropertyValue(
				tcActivityComponent, SDVPropertyConstant.ACTIVITY_CONTROL_POINT);
		String activityObjectName = getTCComponentStringPropertyValue(
				tcActivityComponent, SDVPropertyConstant.ACTIVITY_OBJECT_NAME);
		String activityEngName = getTCComponentStringPropertyValue(
				tcActivityComponent, SDVPropertyConstant.ACTIVITY_ENG_NAME);
		String activityTimeSystemFrequency = getTCComponentStringPropertyValue(
				tcActivityComponent,
				SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
		String activitySystemUnitTime = getTCComponentStringPropertyValue(
				tcActivityComponent,
				SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);
		String activityWorker = getTCComponentStringPropertyValue(
				tcActivityComponent, SDVPropertyConstant.ACTIVITY_WORKER);
		String activityWorkTime = getTCComponentStringPropertyValue(
				tcActivityComponent, SDVPropertyConstant.ACTIVITY_WORK_TIME);
		String activityControlBasis = getTCComponentStringPropertyValue(
				tcActivityComponent, SDVPropertyConstant.ACTIVITY_CONTROL_BASIS);

		// -------------------------------------
		// Node 추가변경 여부를 확인
		// -------------------------------------
		if (tcActivityComponent == null) {
			// 추가된 경우임
			validationResultChangeType = TCData.DECIDED_ADD;
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
		} else {
			if (activityLineNode != null) {
				// 변경내용 비교 대상.
			} else {
				// 삭제된 경우임.
				validationResultChangeType = TCData.DECIDED_REMOVE;
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
				isValide = false;
			}
		}

		// 추가 또는 삭제된 Activity인 경우 더이상 비교는 무의미함.
		if (isValide == false) {
			return isValide;
		}

		// ----------------------------------------------------
		// 비교를 위한 N/F Data Attribute를 읽는다.
		// ----------------------------------------------------
		String nfActivityLineSeq = getNodeFirstChildElementText(
				activityLineNode, "K");

		String nfVehicleCode = getNodeFirstChildElementText(
				activityMasterDataNode, "B");
		String nfLineCode = getNodeFirstChildElementText(
				activityMasterDataNode, "C");
		String nfOperationCode = getNodeFirstChildElementText(
				activityMasterDataNode, "D");
		String nfOperationVersion = getNodeFirstChildElementText(
				activityMasterDataNode, "E");
		
		String nfActivityMasterSeq = getNodeFirstChildElementText(
				activityMasterDataNode, "F");
		
		String nfShortWorkCode = getNodeFirstChildElementText(
				activityMasterDataNode, "G");
		String nfVariableValue = getNodeFirstChildElementText(
				activityMasterDataNode, "H");
		String nfActivityTimeSystemFreq = getNodeFirstChildElementText(
				activityMasterDataNode, "I");
		String nfWorkNameKor = getNodeFirstChildElementText(
				activityMasterDataNode, "J");
		String nfWorkNameEng = getNodeFirstChildElementText(
				activityMasterDataNode, "K");
		String nfWorkingTime = getNodeFirstChildElementText(
				activityMasterDataNode, "L");
		String nfWorkingType = getNodeFirstChildElementText(
				activityMasterDataNode, "M");
		String nfToolId = getNodeFirstChildElementText(activityMasterDataNode,
				"N");
		String nfKPC = getNodeFirstChildElementText(activityMasterDataNode, "O");
		String nfKPCManagementCriteria = getNodeFirstChildElementText(
				activityMasterDataNode, "P");

		// Validation 결과를 출력하는데 필요한 Data 생성
		String operationItemId = ((Element) activityMasterDataNode)
				.getAttribute("OperationItemId");
		String targetType = "Sub Activity";
		String targetItemId = null;
		if (activityObjectName != null) {
			targetItemId = activityObjectName;
		}
		if (targetItemId == null) {
			targetItemId = nfWorkNameKor;
		}

		// ----------------------------------------------------
		// 비교 기준에 따라 하나씩 비교 해 나간다.
		// ----------------------------------------------------
		// 1. 이름
		boolean isSameName = true;
		if (isSame(activityObjectName, nfWorkNameKor) == false) {
			setCompareResult(SubActivityValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameName = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Activity Name", isSameName);

		// 비교의 의미가 없음.
//		// 2. Sequence (숫자로 변경해서 비교 한다.)
//		int tcSeqValue = Integer.parseInt(activitySeqStr);
//		
//		double nfLineSeqValue = -1;
//		double nfMasterSeqValue = -1;
//		boolean isSameSequence = true;
//		if (nfActivityLineSeq != null && nfActivityLineSeq.trim().length() > 0
//				&& nfActivityLineSeq.trim().equalsIgnoreCase("NULL") == false) {
//			nfLineSeqValue = Double.parseDouble(nfActivityLineSeq.trim());
//		}
//		if (nfActivityMasterSeq != null
//				&& nfActivityMasterSeq.trim().length() > 0
//				&& nfActivityMasterSeq.trim().equalsIgnoreCase("NULL") == false) {
//			nfMasterSeqValue = Double.parseDouble(nfActivityMasterSeq.trim());
//		}
//
//		if (nfLineSeqValue != nfMasterSeqValue) {
//			peIFExecution.writeLogTextLine("tcSeqValue = "+tcSeqValue+", nfLineSeqValue="+nfLineSeqValue);
//			
//			// 여기까지 진행 된 경우 이름이 같으나 Seq가 다름.
//			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
//			isSameSequence = false;
//			isValide = false;
//		}
//		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
//				"Sequence", isSameSequence);

		// system category 비교
		boolean isSameSystemCategory = true;
		// 숫자형태의 Category값 
		String peCategoryValueStr = ImportCoreService.getPEActivityCategolyLOV(nfWorkingType);
		String activityCategoryValueStr = ImportCoreService.getPEActivityCategolyLOV(activityCategory);
//		peCategory = checkValueNullOrBlank(peCategory);
//		// PE LOV를 TC String으로 재변환(정미(PE) -> 01 -> 작업자정미(TC))
//		if (peCategory != null && peCategory.trim().length() > 0) {
//			peCategory = ImportCoreService
//					.getActivityCategolyLOVToString(peCategory);
//		}
		
		if (isSame(activityCategoryValueStr, peCategoryValueStr) == false) {
			
			peIFExecution.writeLogTextLine("activityCategoryValueStr="+activityCategoryValueStr+", peCategoryValue="+peCategoryValueStr);
			
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameSystemCategory = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"System Category", isSameSystemCategory);

		// WorkCode 비교
		boolean isSameWorkCode = true;
		String peWorkCode = null;
		if (nfShortWorkCode != null) {
			peWorkCode = nfShortWorkCode.trim();
			if (nfVariableValue != null && nfVariableValue.trim().length() > 0) {
				peWorkCode = peWorkCode + "-"+ nfVariableValue.replace(",", "-");
			}
		}

		if (isSame(activityWorkCode, peWorkCode) == false) {
			peIFExecution.writeLogTextLine("activityWorkCode="+activityWorkCode+", peWorkCode="+peWorkCode);
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameWorkCode = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Work Code", isSameWorkCode);

		// system unit time
		boolean isSameSystemUnitTime = true;
		double tcTimeSystemUnitTime = 0;
		if (activitySystemUnitTime != null
				&& activitySystemUnitTime.trim().length() > 0) {
			tcTimeSystemUnitTime = Double.parseDouble(activitySystemUnitTime);
		}

		double peTimeSystemUnitTime = 0;
		if (nfWorkingTime != null && nfWorkingTime.trim().length() > 0) {
			peTimeSystemUnitTime = Double.parseDouble(nfWorkingTime);
		}

		if (tcTimeSystemUnitTime != peTimeSystemUnitTime) {
			peIFExecution.writeLogTextLine("tcTimeSystemUnitTime = "+tcTimeSystemUnitTime+", peTimeSystemUnitTime = "+peTimeSystemUnitTime);
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameSystemUnitTime = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"System Unit Time", isSameSystemUnitTime);

		// system frequency (난이도)
		boolean isSameSystemFrequency = true;
		double tcTimeSystemFrequency = 0;
		if (activityTimeSystemFrequency != null
				&& activityTimeSystemFrequency.trim().length() > 0) {
			tcTimeSystemFrequency = Double.parseDouble(activityTimeSystemFrequency);
		}

		double peTimeSystemFrequency = 0;
		if (nfActivityTimeSystemFreq != null
				&& nfActivityTimeSystemFreq.trim().length() > 0) {
			peTimeSystemFrequency = Double.parseDouble(nfActivityTimeSystemFreq);
		}
		
		if (tcTimeSystemFrequency != peTimeSystemFrequency) {
			peIFExecution.writeLogTextLine("tcTimeSystemFrequency = "+tcTimeSystemFrequency+", peTimeSystemFrequency = "+peTimeSystemFrequency);
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameSystemFrequency = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"System Frequency", isSameSystemFrequency);

		// Control Point
		boolean isSameControlPoint = true;
		String tcControlPoint = activityControlPoint;
		String peControlPoint = nfKPC;
		if (isSame(tcControlPoint, peControlPoint) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameControlPoint = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Control Point", isSameControlPoint);

		// Control Basis
		boolean isSameControlBasis = true;
		String tcControlBasis = activityControlBasis;
		String peControlBasis = nfKPCManagementCriteria;
		if (isSame(tcControlBasis, peControlBasis) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Control Basis", isSameControlBasis);

		// Activity Tools
		boolean isSameActivityTools = true;
		Vector<String> peToolIdVector = new Vector<String>();
		if (nfToolId != null && nfToolId.trim().indexOf(",") >= 0) {
			String[] tempIdList = nfToolId.split(",");
			for (int i = 0; i < tempIdList.length; i++) {
				if (tempIdList[i] != null && tempIdList[i].trim().length() > 0) {
					peToolIdVector.add(tempIdList[i].trim().toUpperCase());
				}
			}
		}
		if (peToolIdVector.size() < 1) {
			peToolIdVector = null;
		}

		TCComponentMEActivity activity = (TCComponentMEActivity) tcActivityComponent;
		TCComponentMfgBvrOperation bvrOperationBOMLine = operationBOMLine;
		TCComponentBOMLine[] tcToolList = null;
		try {
			tcToolList = activity.getReferenceTools(bvrOperationBOMLine);
		} catch (TCException e) {
			e.printStackTrace();
		}
		if (tcToolList == null || (tcToolList != null && tcToolList.length < 1)) {
			tcToolList = null;
		}

		boolean isSameAbleTarget = true;
		if ((tcToolList == null && peToolIdVector != null)
				|| (tcToolList != null && peToolIdVector == null)) {
			isSameAbleTarget = false;
		} else if (tcToolList != null && peToolIdVector != null
				&& tcToolList.length != peToolIdVector.size()) {
			isSameAbleTarget = false;
		}

		if (isSameAbleTarget == true) {
			for (int i = 0; tcToolList != null && i < tcToolList.length; i++) {
				String itemId = null;
				try {
					itemId = tcToolList[i]
							.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				} catch (TCException e) {
					e.printStackTrace();
				}
				if (itemId != null && peToolIdVector != null
						&& peToolIdVector.contains(itemId.trim()) == false) {
					setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
					isSameActivityTools = false;
					isValide = false;
				}
			}
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Activity Tools", isSameActivityTools);

		if(isValide==true){
			if (getCompareResult() != DefaultValidationUtil.COMPARE_RESULT_DIFFERENT) {
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_EQUAL);
				validationResultChangeType = TCData.DECIDED_NO_CHANGE;
			}
		}

		return isValide;
	}

}
