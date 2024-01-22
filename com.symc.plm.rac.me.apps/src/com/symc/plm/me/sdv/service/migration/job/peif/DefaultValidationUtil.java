package com.symc.plm.me.sdv.service.migration.job.peif;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;

public class DefaultValidationUtil {
	
	protected NewPEIFExecution peIFExecution;

	// Validation 결과 오류가 있으면 COMPARE_RESULT_DIFFERENT를 없으면 COMPARE_RESULT_EQUAL을
	// Return 한다.
	private int compareResultType = 0;
	// Add, Removed, Replaced, Revised 등의 변경형태를 기록하는 변수
	protected int validationResultChangeType = 0;

	// Validation 결과 BOMLine Attritute가 변경되면 True로 설정한다.
	protected boolean isBOMAttributeChanged = false;
	// Validation 결과 Item. ItemRevision등의 Attribute가 변경되면 True로 설정한다.
	protected boolean isMasterDataChanged = false;
	// Validation결과 해당 BOMLine의 ItemRevision이 Replaced 된경우 True로 설정한다.
	protected boolean isReplaced = false;
	
	// Validation 과정에 주요한 Error가 있는경우 True로 설정한다.
	public boolean haveMajorError = false;

	static int COMPARE_UNPERFORMED = 0;
	static int COMPARE_RESULT_DIFFERENT = 1;
	static int COMPARE_RESULT_EQUAL = 2;

	protected boolean bomLineNotFound = false;
	protected boolean masterDataNotFoundFlag = false;
	protected boolean bomDataNotFoundFlag = false;
	
	/*
	 * 공법하위 할당 FindNo 규칙 :
	 * 1 ~ 9 : 일반자재(END-ITEM)
	 * 10 ~ 200 : 공구(TOOL)
	 * 210 ~ 500 : 설비(EQUIPMENT)
	 * 510 ~ 800 : 부자재(SUBSIDIARY)
	 */

	public DefaultValidationUtil(NewPEIFExecution peIFExecution) {
		this.peIFExecution = peIFExecution;
	}
	
	/**
	 * Validation을 시작할때 호출해서 Validation 초기값을 설정하는 함수 매번 Validation을 시작할때마다
	 * 호출해준다.
	 */
	public void clearStatusValues() {
		
		compareResultType = COMPARE_UNPERFORMED;
		validationResultChangeType = TCData.DECIDED_NO_CHANGE;

		isBOMAttributeChanged = false;
		isMasterDataChanged = false;
		isReplaced = false;
		haveMajorError = false;
		
	}
	
	/**
	 * Validation 결과를 확인 하는 Function
	 * @return
	 */
	public boolean getValidationResult(){
		boolean isValide = true;
		
		if(isBOMAttributeChanged==true || 
				isMasterDataChanged==true || 
				validationResultChangeType!=TCData.DECIDED_NO_CHANGE){
			isValide = false;
		}
		
		return isValide;
	}

	/**
	 * Validation 결과를 설정한다.
	 * [COMPARE_UNPERFORMED/COMPARE_RESULT_DIFFERENT/COMPARE_RESULT_EQUAL]
	 * 
	 * @param compareResultType
	 */
	public void setCompareResult(int compareResultType) {
		this.compareResultType = compareResultType;
	}

	/**
	 * Validation 결과 현재 Tc의 내용과 N/F Data의 내용이 동일한지 다른지 비교한 결과를 Return 한다.
	 * 
	 * @return 
	 *         [COMPARE_UNPERFORMED/COMPARE_RESULT_DIFFERENT/COMPARE_RESULT_EQUAL
	 *         ]
	 */
	public int getCompareResult() {
		return compareResultType;
	}

	/**
	 * Validation 과정에 기록된 BOMLine Attribute 변경 여부 값을 읽어 Return 한다.
	 * 
	 * @return true 이면 BOMLine Attribute가 변경되었음을 의미한다.
	 */
	public boolean isBOMAttributeChanged() {
		return isBOMAttributeChanged;
	}

	/**
	 * Validation 과정에 기록된 Master Data 변경 여부 값을 읽어 Return 한다.
	 * 
	 * @return true 이면 Master Data가 변경되었음을 의미한다.
	 */
	public boolean isMasterDataChanged() {
		return isMasterDataChanged;
	}

	/**
	 * Validation 결과 Item의 변경 형태를 Return 한다.
	 * 
	 * @return [DECIDED_NO_CHANGE/DECIDED_ADD/DECIDED_REMOVE/DECIDED_REVISE/
	 *         DECIDED_REPLACE]
	 */
	public int getValidationResultChangeType() {
		return validationResultChangeType;
	}

	/**
	 * Object가 Delete & Add 가 아니라 Replace된 경우 Replace된 상태임을 Return한다.
	 * 
	 * @return Replaced된경우 True를 Return 한다.
	 */
	public boolean isReplaced() {
		return isReplaced;
	}

	/**
	 * 주어진 두 문자열이 동일한지 비교한 결과를 Return 한다.
	 * 
	 * @param tcDataStr
	 * @param peDataStr
	 * @return
	 */
	public boolean isSame(String tcDataStr, String peDataStr) {
		boolean isSame = false;

		tcDataStr = checkValueNullOrBlank(tcDataStr);
		peDataStr = checkValueNullOrBlank(peDataStr);

		if ((tcDataStr == null || (tcDataStr != null && tcDataStr.trim()
				.length() < 1))
				|| (peDataStr == null || (peDataStr != null && peDataStr.trim()
						.length() < 1))) {
			isSame = true;
		}

		if ((tcDataStr != null && tcDataStr.trim().length() > 0)
				&& (peDataStr != null && peDataStr.trim().length() > 0)) {
			if (tcDataStr.trim().equalsIgnoreCase(peDataStr.trim()) == true) {
				isSame = true;
			}
		}

		return isSame;
	}

	/**
	 * 문자열의 값이 null인경우 Blank문자를 그렇지 않은 경우 해당 문자열을 Trim해서 Return 한다. 문자열 비교 편의성을
	 * 위해 필요한 함수
	 * 
	 * @param inputString
	 * @return
	 */
	public String checkValueNullOrBlank(String inputString) {
		String outputString = "";

		if (inputString == null) {
			return outputString;
		}

		if (inputString != null && inputString.trim().length() < 1) {
			return outputString;
		}

		if (inputString.trim().equalsIgnoreCase("NULL") == true) {
			return outputString;
		}

		outputString = inputString.trim();

		return outputString;
	}

	/**
	 * Element의 Child Node중 주어진 Element 이름을 가진 Node를 찾고 그중 처음으로 발견된 Node의 Text를
	 * Return 한다.
	 * 
	 * @param dataNode
	 *            검토대상인 XML Node
	 * @param elementName
	 *            찾고자 하는 Element 이름
	 * @return 첫번째 찾은 Node의 Text 값을 Return 한다.
	 */
	public String getNodeFirstChildElementText(Node dataNode, String elementName) {
		String nfDataStr = "";

		if (dataNode == null) {
			return "";
		}

		elementName = elementName.trim();

		Element targetElement = (Element) dataNode;
		if (targetElement.getElementsByTagName(elementName) != null) {
			if (targetElement.getElementsByTagName(elementName).getLength() > 0) {
				nfDataStr = ((Element) dataNode)
						.getElementsByTagName(elementName).item(0)
						.getTextContent();
			}
		}

		nfDataStr = checkValueNullOrBlank(nfDataStr);

		return nfDataStr;
	}

	/**
	 * 주어진 TCComponent의 문자열 Property 값을 읽어서 Return 한다. 이때 Property Value가 null
	 * 이면 Blank 문자를 Return 한다. 이것은 N/F Data의 값고 비교를 편리하게 하기 위함이다.
	 * 
	 * @param targetComponent
	 * @param propertyName
	 * @return
	 */
	public String getTCComponentStringPropertyValue(
			TCComponent targetComponent, String propertyName) {
		String propertyStrValue = "";

		if (targetComponent == null) {
			return propertyStrValue;
		}

		if (propertyName == null
				|| (propertyName != null && propertyName.trim().length() < 1)) {
			return propertyStrValue;
		}

		if (propertyName != null
				&& propertyName.trim().equalsIgnoreCase("NULL") == true) {
			return propertyStrValue;
		}

		try {
			propertyStrValue = targetComponent.getProperty(propertyName.trim());
		} catch (TCException e) {
			e.printStackTrace();
		}
		propertyStrValue = checkValueNullOrBlank(propertyStrValue);

		return propertyStrValue;
	}

	/**
	 * Validation 결과를 Validation Dialog의 Text 출력을 위한 UI에 표현 해주는 함수.
	 * 
	 * @param operationItemId
	 * @param targetType
	 * @param targetItemId
	 * @param validationName
	 * @param validationResult
	 */
	public void printValidationMessageWhenFalse(String operationItemId,
			String targetType, String targetItemId, String validationName,
			boolean validationResult) {

		if (validationResult == true) {
			return;
		}

		String message = "[" + operationItemId + "] " + targetItemId + "("
				+ targetType + ") : " + validationName + " => "
				+ validationResult;
		peIFExecution.writeLogTextLine(message);
	}

}
