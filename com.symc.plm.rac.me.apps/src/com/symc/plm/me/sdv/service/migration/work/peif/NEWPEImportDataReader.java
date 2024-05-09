package com.symc.plm.me.sdv.service.migration.work.peif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.util.PEIFBOMDataExcelToXML;
import com.symc.plm.me.sdv.service.migration.util.PEIFMasterDataExcelToXML;
import com.symc.plm.me.utils.BundleUtil;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class NEWPEImportDataReader {

	private String workingFolderPath;

	private PEIFMasterDataExcelToXML peIFMasterDataExcelToXMLUtil;
	private PEIFBOMDataExcelToXML peIFBOMDataExcelToXMLUtil;
	private SYMCRemoteUtil symcRemoteUtil;

	private XPath xpath = null;

	private String mecoNo;
	private TCComponentItemRevision mecoRevision;
	private TCComponentBOMWindow processWindow;

	public NEWPEImportDataReader(String workingFolderPath) {
		this.workingFolderPath = workingFolderPath;
		symcRemoteUtil = new SYMCRemoteUtil();
	}

	public void setProcessWindow(TCComponentBOMWindow processWindow) {
		this.processWindow = processWindow;
	}

	public void initMigrationInputData(String mecoNo) throws Exception {
		
		peIFMasterDataExcelToXMLUtil = new PEIFMasterDataExcelToXML(
				workingFolderPath);
		peIFMasterDataExcelToXMLUtil.readAndMakeMasterDataXML();
		
		
		if(peIFMasterDataExcelToXMLUtil.isErrorExist()==true){
			throw new Exception("An error is made to read the data from the Excel file and create an XML file.");
		}

		peIFBOMDataExcelToXMLUtil = new PEIFBOMDataExcelToXML(workingFolderPath);
		peIFBOMDataExcelToXMLUtil.readAndMakeBOMLineDataXML();
		if(peIFBOMDataExcelToXMLUtil.isErrorExist()==true){
			throw new Exception("An error is made to read the data from the Excel file and create an XML file.");
		}

		// xpath 생성
		this.xpath = XPathFactory.newInstance().newXPath();
		this.mecoNo = mecoNo;
		try {
			this.mecoRevision = ImportCoreService.getMecoRevision(mecoNo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 주어진 Line Id를 기준으로 모든 Child Node중 OperationBOMLine NodeList를 Return 한다.
	 * 
	 * @param lineItemId
	 * @return
	 */
	public NodeList getAllChildOperationBOMLineNodeList(String lineItemId) {

		NodeList findedNodeList = null;

		if (lineItemId == null
				|| (lineItemId != null && lineItemId.trim().length() < 1)) {
			return findedNodeList;
		}

		Document lineOperationBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getLineToOperationBOMXMLDocument();
		if (lineOperationBOMXMLDoc == null) {
			return findedNodeList;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//OperationBOMLine[@LineItemId='"
				+ lineItemId.trim() + "']";

		try {
			findedNodeList = (NodeList) xpath.compile(expression).evaluate(
					lineOperationBOMXMLDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		if (findedNodeList != null && findedNodeList.getLength() < 1) {
			findedNodeList = null;
		}

		return findedNodeList;
	}

	/**
	 * 주어진 Operation Id를 가진 모든 Line Item Id를 Array에 담아 Return 한다.
	 * 
	 * @param operationItemId
	 * @return
	 */
	public ArrayList<String> getAllParentLineItemIdForOperation(
			String operationItemId) {

		ArrayList<String> lineItemIdList = new ArrayList<String>();

		NodeList operationBOMLineNodeList = findAllOperationBOMLineNodeList(operationItemId);
		for (int i = 0; i < operationBOMLineNodeList.getLength(); i++) {
			Node currentNode = operationBOMLineNodeList.item(i);

			String currentValueStr = null;
			String keyXPathExpression = "@LineItemId";
			// XPATH를 이용해 Attribute를 읽는다.
			try {
				currentValueStr = xpath.evaluate(keyXPathExpression,
						currentNode);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}

			if (currentValueStr != null && currentValueStr.trim().length() > 0) {
				if (lineItemIdList.contains(currentValueStr.trim()) == false) {
					lineItemIdList.add(currentValueStr.trim());
				}
			}

		}

		if (lineItemIdList != null && lineItemIdList.size() < 1) {
			lineItemIdList = null;
		}

		return lineItemIdList;
	}

	/**
	 * Operation이 하나이상의 Line에 할당된 Data가 N/F에 있는지 확인 하기위한 함수 Operation Id가 같은 모든
	 * Operation BOMLineItem Node들을 찾아서 Return 한다.
	 * 
	 * @param operationItemId
	 * @return
	 */
	private NodeList findAllOperationBOMLineNodeList(String operationItemId) {

		NodeList findedNodeList = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return findedNodeList;
		}

		Document lineOperationBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getLineToOperationBOMXMLDocument();
		if (lineOperationBOMXMLDoc == null) {
			return findedNodeList;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//OperationBOMLine[@OperationItemId='"
				+ operationItemId.trim() + "']";

		try {
			findedNodeList = (NodeList) xpath.compile(expression).evaluate(
					lineOperationBOMXMLDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		if (findedNodeList != null && findedNodeList.getLength() < 1) {
			findedNodeList = null;
		}

		return findedNodeList;
	}

	/**
	 * BOM Line 구성정보중 Line Id와 Operation Id 조건을 만족하는 Operation BOMLine Node
	 * List를 검색하고 그 결과를 Return 한다.
	 * 
	 * @param lineItemId
	 * @param operationItemId
	 * @return
	 */
	public Node getOperationBOMLineNode(String lineItemId,
			String operationItemId) {

		Node findedNode = null;
		// NodeList findedNodeList = null;

		if (lineItemId == null
				|| (lineItemId != null && lineItemId.trim().length() < 1)) {
			return findedNode;
		}

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return findedNode;
		}

		Document lineOperationBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getLineToOperationBOMXMLDocument();
		if (lineOperationBOMXMLDoc == null) {
			System.out.println("Can't find XML Document");
			return findedNode;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		// (논리연산자 대문자 사용하면 javax.xml.xpath.XPathExpressionException Error 발생됨.)
		String expression = "//OperationBOMLine[@LineItemId='" + lineItemId
				+ "' and @OperationItemId='" + operationItemId.trim() + "']";

		System.out.println("XPATH expression : "+expression);
		
		try {
			// Returns the first element matching
			findedNode = (Node) xpath.compile(expression).evaluate(
					lineOperationBOMXMLDoc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		System.out.println("findedNode = "+findedNode);
		
		return findedNode;
	}

	/**
	 * Operation에 적용될 Master 정보를 가진 Data를 N/F에서 읽어 Return 한다.
	 * 
	 * @param operationItemId
	 * @return
	 */
	public Node getOperationMasterNode(String operationItemId) {
		Node operationMasterNode = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return operationMasterNode;
		}

		Document operationMasterXMLDoc = peIFMasterDataExcelToXMLUtil
				.getOperationMasterXMLDocument();
		if (operationMasterXMLDoc == null) {
			return operationMasterNode;
		}

		String expression = "//OperationItem[@OperationItemId='"
				+ operationItemId + "']";

		try {
			// Returns the first element matching
			operationMasterNode = (Node) xpath.compile(expression).evaluate(
					operationMasterXMLDoc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return operationMasterNode;
	}

	private String getPropertyString(String inputStr) {
		String newStr = "";

		if (inputStr != null && inputStr.trim().length() > 0
				&& inputStr.trim().equalsIgnoreCase("NULL") == false) {
			newStr = inputStr.trim();
		}

		return newStr;
	}

	public IDataSet getOperationNodeIDataSet(Element operationNodeBOMLineData,
			Element operationNodeMasterData) {
		IDataSet dataSet = new org.sdv.core.common.data.DataSet();
		IDataMap opInformDataMap = new RawDataMap();
		dataSet.addDataMap("opInform", opInformDataMap);

		String nfOperationItemId = null;
		String nfPlantCode = null;
		String nfShopCode = null;
		String nfProductNo = null;
		String nfLineCode = null;
		String nfOptionCondition = null;
		// String nfOperationQuantityStr = null;
		// String nfOperationSequenceStr = null;

		// N/F Data를 읽는다.
		if (operationNodeBOMLineData.getAttribute("OperationItemId") != null) {
			nfOperationItemId = operationNodeBOMLineData
					.getAttribute("OperationItemId");
		}
		nfOperationItemId = getPropertyString(nfOperationItemId);

		// BOMLine Attribute 읽음.
		if (operationNodeBOMLineData.getElementsByTagName("B") != null) {
			nfPlantCode = operationNodeBOMLineData.getElementsByTagName("B")
					.item(0).getTextContent();
		}
		nfPlantCode = getPropertyString(nfPlantCode);

		if (operationNodeBOMLineData.getElementsByTagName("C") != null) {
			nfShopCode = operationNodeBOMLineData.getElementsByTagName("C")
					.item(0).getTextContent();
		}
		nfShopCode = getPropertyString(nfShopCode);

		// OPERATION_REV_PRODUCT_CODE
		if (operationNodeBOMLineData.getElementsByTagName("D") != null) {
			nfProductNo = operationNodeBOMLineData.getElementsByTagName("D")
					.item(0).getTextContent();
		}
		nfProductNo = getPropertyString(nfProductNo);

		// Line Code
		if (operationNodeBOMLineData.getElementsByTagName("E") != null) {
			nfLineCode = operationNodeBOMLineData.getElementsByTagName("E")
					.item(0).getTextContent();
		}
		nfLineCode = getPropertyString(nfLineCode);

		// // 공정편성 버젼 (From BOMLine)
		// if(operationNodeBOMLineData.getElementsByTagName("F") != null){
		// operationNodeBOMLineData.getElementsByTagName("F").item(0).getTextContent();
		// }
		if (operationNodeBOMLineData.getElementsByTagName("K") != null) {
			nfOptionCondition = operationNodeBOMLineData
					.getElementsByTagName("K").item(0).getTextContent();
		}
		nfOptionCondition = getPropertyString(nfOptionCondition);

		// if(operationNodeMasterData.getElementsByTagName("L") != null){
		// nfOperationQuantityStr =
		// operationNodeMasterData.getElementsByTagName("L").item(0).getTextContent();
		// }
		// if(operationNodeBOMLineData.getElementsByTagName("M") != null){
		// nfOperationSequenceStr =
		// operationNodeBOMLineData.getElementsByTagName("M").item(0).getTextContent();
		// }

		// Item/ItemRevision Master 읽음.
		String nfVehicleCode = null;
		String nfLinePlantCode = null;
		String nfOperationCode = null;
		String nfFunctionCode = null;
		String nfOpCode = null;
		String nfOperationVersion = null;
		String nfOperationLocation = null;
		String nfKorName = null;
		String nfEngName = null;
		String nfWorkerSectionCode = null;
		String nfItemInputLocation = null;
		String nfInstructionDrawingNo = null;
		String nfStationNo = null;
		String nfSafetyCode = null;
		String nfSystemCode = null;
		String nfMaintainCode = null;
		String nfSequenceCode = null;
		String nfClassifyUnderBodyWork = null;
		String nfClassifyRepresentVehicle = null;
		String nfWorkInstructionSheetPath = null;
		String nfWorkInstructionInterfaceOrNot = null;

		// [index01] : OPERATION_REV_VEHICLE_CODE
		if (operationNodeMasterData.getElementsByTagName("B") != null) {
			nfVehicleCode = operationNodeMasterData.getElementsByTagName("B")
					.item(0).getTextContent();
		}
		nfVehicleCode = getPropertyString(nfVehicleCode);

		// [index02] : OPERATION_REV_SHOP
		if (operationNodeMasterData.getElementsByTagName("C") != null) {
			nfLinePlantCode = operationNodeMasterData.getElementsByTagName("C")
					.item(0).getTextContent();
		}
		nfLinePlantCode = getPropertyString(nfLinePlantCode);

		// [index03] : OPERATION_REV_OPERATION_CODE, OPERATION_REV_FUNCTION_CODE
		if (operationNodeMasterData.getElementsByTagName("D") != null) {
			nfOperationCode = operationNodeMasterData.getElementsByTagName("D")
					.item(0).getTextContent();
			nfOperationCode = getPropertyString(nfOperationCode);
			nfFunctionCode = nfOperationCode.split("-")[0];
			nfOpCode = nfOperationCode.split("-")[1];
		}
		nfOperationCode = getPropertyString(nfOperationCode);
		nfFunctionCode = getPropertyString(nfFunctionCode);
		nfOpCode = getPropertyString(nfOpCode);

		// [index04] : OPERATION_REV_BOP_VERSION
		if (operationNodeMasterData.getElementsByTagName("E") != null) {
			nfOperationVersion = operationNodeMasterData
					.getElementsByTagName("E").item(0).getTextContent();
		}
		nfOperationVersion = getPropertyString(nfOperationVersion);

		// [index05] : OPERATION_WORKAREA
		if (operationNodeMasterData.getElementsByTagName("F") != null) {
			nfOperationLocation = operationNodeMasterData
					.getElementsByTagName("F").item(0).getTextContent();
		}
		nfOperationLocation = getPropertyString(nfOperationLocation);

		// [index06] : OPERATION_REV_KOR_NAME
		if (operationNodeMasterData.getElementsByTagName("G") != null) {
			nfKorName = operationNodeMasterData.getElementsByTagName("G")
					.item(0).getTextContent();
		}
		nfKorName = getPropertyString(nfKorName);

		// [index07] : OPERATION_REV_ENG_NAME
		if (operationNodeMasterData.getElementsByTagName("H") != null) {
			nfEngName = operationNodeMasterData.getElementsByTagName("H")
					.item(0).getTextContent();
		}
		nfEngName = getPropertyString(nfEngName);

		// [index08] : OPERATION_WORKER_CODE
		if (operationNodeMasterData.getElementsByTagName("I") != null) {
			nfWorkerSectionCode = operationNodeMasterData
					.getElementsByTagName("I").item(0).getTextContent();
		}
		nfWorkerSectionCode = getPropertyString(nfWorkerSectionCode);

		// [index09] : OPERATION_ITEM_UL
		if (operationNodeMasterData.getElementsByTagName("J") != null) {
			nfItemInputLocation = operationNodeMasterData
					.getElementsByTagName("J").item(0).getTextContent();
		}
		nfItemInputLocation = getPropertyString(nfItemInputLocation);

		// [index10]
		if (operationNodeMasterData.getElementsByTagName("K") != null) {
			nfInstructionDrawingNo = operationNodeMasterData
					.getElementsByTagName("K").item(0).getTextContent();
		}
		nfInstructionDrawingNo = getPropertyString(nfInstructionDrawingNo);

		// [index11] : OPERATION_REV_STATION_NO
		if (operationNodeMasterData.getElementsByTagName("L") != null) {
			nfStationNo = operationNodeMasterData.getElementsByTagName("L")
					.item(0).getTextContent();
		}
		nfStationNo = getPropertyString(nfStationNo);

		// [index12] : OPERATION_REV_DR
		if (operationNodeMasterData.getElementsByTagName("M") != null) {
			nfSafetyCode = operationNodeMasterData.getElementsByTagName("M")
					.item(0).getTextContent();
		}
		nfSafetyCode = getPropertyString(nfSafetyCode);

		// [index13] : OPERATION_REV_ASSY_SYSTEM
		if (operationNodeMasterData.getElementsByTagName("N") != null) {
			nfSystemCode = operationNodeMasterData.getElementsByTagName("N")
					.item(0).getTextContent();
		}
		nfSystemCode = getPropertyString(nfSystemCode);

		// [index14]
		if (operationNodeMasterData.getElementsByTagName("O") != null) {
			nfMaintainCode = operationNodeMasterData.getElementsByTagName("O")
					.item(0).getTextContent();
		}
		nfMaintainCode = getPropertyString(nfMaintainCode);

		// [index15] : OPERATION_PROCESS_SEQ
		if (operationNodeMasterData.getElementsByTagName("P") != null) {
			nfSequenceCode = operationNodeMasterData.getElementsByTagName("P")
					.item(0).getTextContent();
		}
		nfSequenceCode = getPropertyString(nfSequenceCode);

		// [index16] : OPERATION_WORK_UBODY
		if (operationNodeMasterData.getElementsByTagName("Q") != null) {
			nfClassifyUnderBodyWork = operationNodeMasterData
					.getElementsByTagName("Q").item(0).getTextContent();
		}
		nfClassifyUnderBodyWork = getPropertyString(nfClassifyUnderBodyWork);

		// [index17]
		if (operationNodeMasterData.getElementsByTagName("R") != null) {
			nfClassifyRepresentVehicle = operationNodeMasterData
					.getElementsByTagName("R").item(0).getTextContent();
		}
		nfClassifyRepresentVehicle = getPropertyString(nfClassifyRepresentVehicle);

		// [index18]
		if (operationNodeMasterData.getElementsByTagName("S") != null) {
			nfWorkInstructionSheetPath = operationNodeMasterData
					.getElementsByTagName("S").item(0).getTextContent();
		}
		nfWorkInstructionSheetPath = getPropertyString(nfWorkInstructionSheetPath);

		// [index19]
		if (operationNodeMasterData.getElementsByTagName("T") != null) {
			nfWorkInstructionInterfaceOrNot = operationNodeMasterData
					.getElementsByTagName("T").item(0).getTextContent();
		}
		nfWorkInstructionInterfaceOrNot = getPropertyString(nfWorkInstructionInterfaceOrNot);

		// OPERATION_REP_VEHICLE_CHECK
		boolean vehicleCheck = ("Y".equals(nfClassifyRepresentVehicle)) ? true
				: false;
		// OPERATION_MAX_WORK_TIME_CHECK
		boolean maxWorkTimeCheck = false;
		String dwgNo = nfInstructionDrawingNo;
		String[] dwgNoArray = dwgNo.split("/");
		// OPERATION_REV_INSTALL_DRW_NO
		ArrayList<String> dwgNoList = new ArrayList<String>();
		if (dwgNoArray != null && dwgNoArray.length > 0) {
			for (String dwg : dwgNoList) {
				dwg = BundleUtil.nullToString(dwg).trim();
				if (!"".equals(dwg)) {
					dwgNoList.add(dwgNo);
				}
			}
		}

		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE,
				nfVehicleCode);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_SHOP,
				nfLinePlantCode);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE,
				nfFunctionCode);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE,
				nfOpCode);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION,
				nfOperationVersion);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME,
				nfKorName);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME,
				nfEngName);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_WORKER_CODE,
				nfWorkerSectionCode);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_PROCESS_SEQ,
				nfSequenceCode);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_WORKAREA,
				nfOperationLocation);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_STATION_NO,
				nfStationNo);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_DR, nfSafetyCode);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_WORK_UBODY,
				nfClassifyUnderBodyWork);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_ITEM_UL,
				nfItemInputLocation);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO,
				dwgNoList, IData.LIST_FIELD);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM,
				nfSystemCode);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK,
				maxWorkTimeCheck, IData.BOOLEAN_FIELD);
		opInformDataMap.put(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK,
				vehicleCheck, IData.BOOLEAN_FIELD);

		opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE,
				nfProductNo);

		// Teamcenter 형식의 Option Condition
		String tcTypeOptionConditionStr = null;
		try {
			tcTypeOptionConditionStr = getConversionOptionCondition(nfOptionCondition);
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		tcTypeOptionConditionStr = getPropertyString(tcTypeOptionConditionStr);

		// Operation 생성 과정에 필요한 Data를 추가로 첨부
		opInformDataMap.put("WorkInstructionFilePath",
				nfWorkInstructionSheetPath);
		opInformDataMap.put("WorkInstructionInterfaceFlag",
				nfWorkInstructionInterfaceOrNot);
		opInformDataMap.put(SDVPropertyConstant.BL_VARIANT_CONDITION,
				tcTypeOptionConditionStr);

		String stationNo = nfStationNo.replace("-", "");// 공정번호
		String workerCode = nfWorkerSectionCode.replace("-", "");// 작업자코드
		String seq = nfSequenceCode;// 작업자 순번

		// 공정편성번호(Find No / Seq No) 입력
		boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty()
				|| seq.isEmpty(); // 하나라도 값이 없으면 반영안함
		String findNo = stationNo.concat("|").concat(workerCode).concat("|")
				.concat(seq);
		if (findNo.length() <= 15 && isExistEmptyValue == false) {
			opInformDataMap.put(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
		}
		findNo = getPropertyString(findNo);

		// MECO 설정
		IDataMap mecoSelectDataMap = new RawDataMap();
		dataSet.addDataMap("mecoSelect", mecoSelectDataMap);
		mecoSelectDataMap.put("mecoNo", mecoNo);
		mecoSelectDataMap.put("mecoRev", this.mecoRevision, IData.OBJECT_FIELD);

		return dataSet;
	}

	/**
	 * Artgument로 받은 Operation BOMLine Node List를 순차적으로 읽어 Operation Item Id 를
	 * List에 담아 Return 한다.
	 * 
	 * @param operationBOMLineNodeList
	 *            OperationBOMLine NodeList
	 * @return
	 */
	public ArrayList<String> getOperationItemIdListFromOperationBOMLineNodeList(
			NodeList operationBOMLineNodeList) {

		ArrayList<String> operationItemIdListArray = null;

		operationItemIdListArray = new ArrayList<String>();

		for (int nodeIndex = 0; operationBOMLineNodeList != null
				&& nodeIndex < operationBOMLineNodeList.getLength(); nodeIndex++) {

			Element element = (Element) operationBOMLineNodeList
					.item(nodeIndex);

			String tempOperationItemId = null;
			// Attribute를 읽는 방법1
			// tempOperationItemId = element.getAttribute("OperationItemId");
			// System.out.println("tempOperationItemId = "+tempOperationItemId);

			// Attribute를 읽는 방법2
			try {
				tempOperationItemId = xpath.evaluate("@OperationItemId",
						element);
				// String f = xpath.evaluate("ItemRev/@ItemId", element);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}

			System.out.println("tempOperationItemId = " + tempOperationItemId);

			if (tempOperationItemId == null
					|| (tempOperationItemId != null && tempOperationItemId
							.trim().length() < 1)) {
				continue;
			}

			if (operationItemIdListArray.contains(tempOperationItemId) == false) {
				operationItemIdListArray.add(tempOperationItemId);
				System.out.println("tempOperationItemId(+) = "
						+ tempOperationItemId);
			}

		}

		if (operationItemIdListArray.size() < 1) {
			operationItemIdListArray = null;
		}

		return operationItemIdListArray;
	}

	/**
	 * Operation의 Child Node인 EndItem Element들을 찾아서 Return 한다.
	 * 
	 * @param operationItemId
	 * @return
	 */
	public NodeList getOperationChildEndItemBOMLineNodeList(
			String operationItemId) {

		NodeList findedNodeList = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return findedNodeList;
		}

		Document operationEndItemBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToEndItemBOMXMLDocument();
		if (operationEndItemBOMXMLDoc == null) {
			return findedNodeList;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//EndItemBOMLine[@OperationItemId='"
				+ operationItemId.trim() + "']";

		try {
			findedNodeList = (NodeList) xpath.compile(expression).evaluate(
					operationEndItemBOMXMLDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		if (findedNodeList != null && findedNodeList.getLength() < 1) {
			findedNodeList = null;
		}

		return findedNodeList;
	}

	/**
	 * Operation의 Child Node인 EndItem Element중 OperationId와 absOccId 조건에 맞는
	 * EndItem BOMLIne Node를 찾아서 Return 한다.
	 * XPath를 이용해 해당 Data Node를 찾는다.
	 * 
	 * @param operationItemId
	 * @param productParentOccThreadUid : ParentNodeCopyStableOccThreadPUId + PartCopyStableOccThreadPUId 형태 또는 ParentNodeCopyStableOccThreadPUId 형태로 입력한다.  
	 * @param productEndItemOccThreadUid
	 * @return
	 */
	public Node getOperationChildEndItemBOMLineNode(String operationItemId,
			String productParentOccThreadUid, String productEndItemOccThreadUid) {

		Node resultNode = null;

		NodeList findedNodeList = null;
		
		String parentCopyStableOccThreadUid = null;
		if(productParentOccThreadUid!=null && productParentOccThreadUid.trim().length()>0){
			String[] tempStrings = productParentOccThreadUid.split("\\+");
			if(tempStrings!=null && tempStrings.length>1){
				if(tempStrings[0]!=null){
					parentCopyStableOccThreadUid = tempStrings[0].trim();
				}
			}else{
				parentCopyStableOccThreadUid = productParentOccThreadUid.trim();
			}
		}

//		System.out.println("\n------------------------------------"
//				+ "\ngetOperationChildEndItemBOMLineNode"
//				+ "\n------------------------------------");
//		System.out.println("operationItemId : " + operationItemId);
//		System.out.println("productParentOccThreadUid : "
//				+ parentCopyStableOccThreadUid);
//		System.out.println("productEndItemOccThreadUid : "
//				+ productEndItemOccThreadUid);

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return resultNode;
		}

		if (parentCopyStableOccThreadUid == null
				|| ( parentCopyStableOccThreadUid != null && 
						parentCopyStableOccThreadUid.trim().length() < 1)
				) {
			return resultNode;
		}

		if (productEndItemOccThreadUid == null
				|| (productEndItemOccThreadUid != null && productEndItemOccThreadUid
						.trim().length() < 1)) {
			return resultNode;
		}

		Document operationEndItemBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToEndItemBOMXMLDocument();
		if (operationEndItemBOMXMLDoc == null) {
			return resultNode;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//EndItemBOMLine[@OperationItemId='"
				+ operationItemId.trim() + "' and L[text()='"
				+ parentCopyStableOccThreadUid.trim() + "+"
				+ productEndItemOccThreadUid.trim() + "']]";

		try {
			findedNodeList = (NodeList) xpath.compile(expression).evaluate(
					operationEndItemBOMXMLDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		if (findedNodeList != null && findedNodeList.getLength() < 1) {
			findedNodeList = null;
		}

		for (int i = 0; findedNodeList != null
				&& i < findedNodeList.getLength(); i++) {
			Node currentNode = findedNodeList.item(i);
			if (currentNode != null) {
				resultNode = currentNode;
				break;
			}
		}

		return resultNode;
	}

	/**
	 * Operation의 Child Node인 부자재 Element들을 찾아서 Return 한다.
	 * 
	 * @param operationItemId
	 * @return
	 */
	public NodeList getOperationChildFacilityBOMLineNodeList(
			String operationItemId) {

		NodeList findedNodeList = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return findedNodeList;
		}

		Document operationFacilityBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToFacilityBOMXMLDocument();
		
		if (operationFacilityBOMXMLDoc == null) {
			return findedNodeList;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//FacilityBOMLine[@OperationItemId='"
				+ operationItemId.trim() + "']";

		try {
			findedNodeList = (NodeList) xpath.compile(expression).evaluate(
					operationFacilityBOMXMLDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		if (findedNodeList != null && findedNodeList.getLength() < 1) {
			findedNodeList = null;
		}

		return findedNodeList;
	}

	public Node getFacilityBOMLineNode(String operationItemId, String tcSeqNo) {

		Node facilityBOMLineNode = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return facilityBOMLineNode;
		}

		if (tcSeqNo == null || (tcSeqNo != null && tcSeqNo.trim().length() < 1)) {
			return facilityBOMLineNode;
		}

		Document facilityBOMLineXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToFacilityBOMXMLDocument();
		if (facilityBOMLineXMLDoc == null) {
			return facilityBOMLineNode;
		}

		String expression = "//FacilityBOMLine[@OperationItemId='"
				+ operationItemId + "' and M[number()='" + tcSeqNo + "']]";

		try {
			// Returns the first element matching
			facilityBOMLineNode = (Node) xpath.compile(expression).evaluate(
					facilityBOMLineXMLDoc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return facilityBOMLineNode;
	}

	public Node getFacilityMasterNode(String facilityItemId) {

		Node facilityMasterNode = null;

		if (facilityItemId == null
				|| (facilityItemId != null && facilityItemId.trim().length() < 1)) {
			return facilityMasterNode;
		}

		Document facilityMasterXMLDoc = peIFMasterDataExcelToXMLUtil
				.getFacilityMasterXMLDocument();
		if (facilityMasterXMLDoc == null) {
			return facilityMasterNode;
		}

		String expression = "//FacilityItem[ C [ text()='" + facilityItemId + "' ] ]";

		try {
			// Returns the first element matching
			facilityMasterNode = (Node) xpath.compile(expression).evaluate(
					facilityMasterXMLDoc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return facilityMasterNode;
	}

	/**
	 * Operation의 Child Node인 부자재 Element들을 찾아서 Return 한다.
	 * 
	 * @param operationItemId
	 * @return
	 */
	public NodeList getOperationChildSubsidiaryBOMLineNodeList(
			String operationItemId) {

		NodeList findedNodeList = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return findedNodeList;
		}

		Document operationSubsidiaryBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToSubsidiaryBOMXMLDocument();
		if (operationSubsidiaryBOMXMLDoc == null) {
			return findedNodeList;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//SubsidiaryBOMLine[@OperationItemId='"+ operationItemId.trim() + "']";

		try {
			findedNodeList = (NodeList) xpath.compile(expression).evaluate(
					operationSubsidiaryBOMXMLDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		if (findedNodeList != null && findedNodeList.getLength() < 1) {
			findedNodeList = null;
		}

		return findedNodeList;
	}

	public Node getSubsidiaryBOMLineNode(String operationItemId, String nfSeqNo) {

		Node subsidiaryBOMLineNode = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return subsidiaryBOMLineNode;
		}

		if (nfSeqNo == null || (nfSeqNo != null && nfSeqNo.trim().length() < 1)) {
			return subsidiaryBOMLineNode;
		}

		Document subsidiaryBOMLineXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToSubsidiaryBOMXMLDocument();
		if (subsidiaryBOMLineXMLDoc == null) {
			return subsidiaryBOMLineNode;
		}

		String expression = "//SubsidiaryBOMLine[@OperationItemId='"
				+ operationItemId + "' and O[text()='" + nfSeqNo + "']]";

		try {
			// Returns the first element matching
			subsidiaryBOMLineNode = (Node) xpath.compile(expression).evaluate(
					subsidiaryBOMLineXMLDoc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return subsidiaryBOMLineNode;
	}

	/**
	 * Operation의 Child Node인 부자재 Element들을 찾아서 Return 한다.
	 * 
	 * @param operationItemId
	 * @return
	 */
	public NodeList getOperationChildToolBOMLineNodeList(String operationItemId) {

		NodeList findedNodeList = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return findedNodeList;
		}

		Document operationToolBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToToolBOMXMLDocument();
		if (operationToolBOMXMLDoc == null) {
			return findedNodeList;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//ToolBOMLine[@OperationItemId='"
				+ operationItemId.trim() + "']";

		try {
			findedNodeList = (NodeList) xpath.compile(expression).evaluate(
					operationToolBOMXMLDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		if (findedNodeList != null && findedNodeList.getLength() < 1) {
			findedNodeList = null;
		}

		return findedNodeList;
	}

	public Node getToolBOMLineNode(String operationItemId, String tcSeqNo) {

		Node toolBOMLineNode = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return toolBOMLineNode;
		}

		if (tcSeqNo == null || (tcSeqNo != null && tcSeqNo.trim().length() < 1)) {
			return toolBOMLineNode;
		}

		Document toolBOMLineXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToToolBOMXMLDocument();
		if (toolBOMLineXMLDoc == null) {
			return toolBOMLineNode;
		}

		String expression = "//ToolBOMLine[@OperationItemId='"
				+ operationItemId + "' and N[number()=" + tcSeqNo + "]]";

		try {
			// Returns the first element matching
			toolBOMLineNode = (Node) xpath.compile(expression).evaluate(
					toolBOMLineXMLDoc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return toolBOMLineNode;
	}

	public Node getToolMasterNode(String toolItemId) {

		Node facilityMasterNode = null;

		if (toolItemId == null
				|| (toolItemId != null && toolItemId.trim().length() < 1)) {
			return facilityMasterNode;
		}

		Document toolMasterXMLDoc = peIFMasterDataExcelToXMLUtil
				.getToolMasterXMLDocument();
		if (toolMasterXMLDoc == null) {
			return facilityMasterNode;
		}

		String expression = "//ToolItem[ B [ text()='" + toolItemId + "' ] ]";

		try {
			// Returns the first element matching
			facilityMasterNode = (Node) xpath.compile(expression).evaluate(
					toolMasterXMLDoc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return facilityMasterNode;
	}

	/**
	 * Operation의 Child Node인 부자재 Element들을 찾아서 Return 한다.
	 * 
	 * @param operationItemId
	 * @return
	 */
	public NodeList getOperationChildActivityLineNodeList(String operationItemId) {

		NodeList findedNodeList = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return findedNodeList;
		}

		Document operationToolBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToActivityBOMXMLDocument();
		if (operationToolBOMXMLDoc == null) {
			return findedNodeList;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//ActivityLine[@OperationItemId='"
				+ operationItemId.trim() + "']";
		XPathExpression xPathExpression = null;
		try {

			xPathExpression = xpath.compile(expression);
			if (xPathExpression != null) {
				findedNodeList = (NodeList) xPathExpression.evaluate(
						operationToolBOMXMLDoc, XPathConstants.NODESET);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		if (findedNodeList != null && findedNodeList.getLength() < 1) {
			findedNodeList = null;
		}

		return findedNodeList;
	}

	public List<Element> getActivityLineNodeList(String operationItemId) {
		NodeList activityLineNodeList = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return (List<Element>) null;
		}

		Document operationActivityBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToActivityBOMXMLDocument();
		if (operationActivityBOMXMLDoc == null) {
			return (List<Element>) null;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//ActivityLine[@OperationItemId='"
				+ operationItemId + "']";
		XPathExpression xPathExpression = null;
		try {
			xPathExpression = xpath.compile(expression);
			if (xPathExpression != null) {
				activityLineNodeList = (NodeList) xPathExpression.evaluate(
						operationActivityBOMXMLDoc, XPathConstants.NODESET);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		List<Element> nodeElementList = new ArrayList<Element>();
		for (int i = 0; activityLineNodeList != null
				&& i < activityLineNodeList.getLength(); i++) {
			Node activityLineNode = activityLineNodeList.item(i);
			nodeElementList.add((Element) activityLineNode);
		}

		Comparator<Element> comparator = new Comparator<Element>() {
			public int compare(Element comparatorElementA,
					Element comparatorElementB) {
				String comparatorElementStringA = comparatorElementA
						.getElementsByTagName("K").item(0).getTextContent();
				String comparatorElementStringB = comparatorElementB
						.getElementsByTagName("K").item(0).getTextContent();
				return comparatorElementStringA
						.compareTo(comparatorElementStringB);
			}
		};
		Collections.sort(nodeElementList, comparator);

		return nodeElementList;
	}

	public Node getActivityLineNode(String operationItemId,
			String subActivitySeq) {
		Node subActivityNode = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return subActivityNode;
		}

		if (subActivitySeq == null
				|| (subActivitySeq != null && subActivitySeq.trim().length() < 1)) {
			return subActivityNode;
		}

		Document operationActivityBOMXMLDoc = peIFBOMDataExcelToXMLUtil
				.getOperationToActivityBOMXMLDocument();
		if (operationActivityBOMXMLDoc == null) {
			return subActivityNode;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//ActivityLine[@OperationItemId='"
				+ operationItemId + "' and K[number()=" + subActivitySeq + "]]";
		XPathExpression xPathExpression = null;

		try {
			xPathExpression = xpath.compile(expression);
			if (xPathExpression != null) {
				subActivityNode = (Node) xPathExpression.evaluate(
						operationActivityBOMXMLDoc, XPathConstants.NODE);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return subActivityNode;
	}

	public NodeList getActivityMasterNodeList(String operationItemId) {
		NodeList activityMasterNodeList = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return activityMasterNodeList;
		}

		Document activityBOMLineMasterXMLDoc = peIFMasterDataExcelToXMLUtil
				.getActivityMasterXMLDocument();
		if (activityBOMLineMasterXMLDoc == null) {
			return activityMasterNodeList;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//ActivityItem[@OperationItemId='"
				+ operationItemId + "']";
		XPathExpression xPathExpression = null;
		try {

			xPathExpression = xpath.compile(expression);
			if (xPathExpression != null) {
				activityMasterNodeList = (NodeList) xPathExpression.evaluate(
						activityBOMLineMasterXMLDoc, XPathConstants.NODESET);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return activityMasterNodeList;
	}

	public Node getActivityMasterNode(String operationItemId,
			String subActivitySeq) {
		Node activityMasterNode = null;

		if (operationItemId == null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			return activityMasterNode;
		}

		if (subActivitySeq == null
				|| (subActivitySeq != null && subActivitySeq.trim().length() < 1)) {
			return activityMasterNode;
		}

		Document activityBOMLineMasterXMLDoc = peIFMasterDataExcelToXMLUtil
				.getActivityMasterXMLDocument();
		if (activityBOMLineMasterXMLDoc == null) {
			return activityMasterNode;
		}

		// Line Item Id 값을 만족하는 OperationBOMLine Element들을 찾아 오는 XPATH 표현식
		String expression = "//ActivityItem[@OperationItemId='"
				+ operationItemId + "' and F[number()=" + subActivitySeq + "]]";
		XPathExpression xPathExpression = null;
		try {

			xPathExpression = xpath.compile(expression);
			if (xPathExpression != null) {
				activityMasterNode = (Node) xPathExpression.evaluate(
						activityBOMLineMasterXMLDoc, XPathConstants.NODE);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return activityMasterNode;
	}

	/**
	 * Product Part BOMLine의 CopyStableOccThreadUID값과 Parent BOMLine의 CopyStableOccThreadUID값을
	 * 이용해 해당 Part의 Product BOMLine의 ABS OCC ID 값을 찾아서 Return 한다.
	 * 기본적으로 하나의 ABS_OCC_ID값을 찾아서 Return 해야 하지만 만약에 복수개의 검색결과를 Return 하는경우
	 * Product Window의 Function findConfigedBOMLinesForAbsOccID() 실제 검색 결과가 있는것을 사용하면 될것 같다.
	 *  어쨌던 이 함수는 하나 또는 그이상의 ABS_OCC_ID값을 찾아서 Return 한다.
	 * @param productItemId
	 * @param endItemOccThreadPuid
	 * @param parentOccThreadPuid
	 * @return
	 */
	public ArrayList<String> getABSOccPuidString(String productItemId,
			String endItemOccThreadPuid, String parentOccThreadPuid) {

		String absOccPuid = null;

		String peInterfaceService = "com.kgm.service.PEInterfaceService";

		ArrayList<HashMap> resultList = null;

		DataSet ds = new DataSet();
		ds.put("productItemId", productItemId);
		ds.put("endItemOccThreadPuid", endItemOccThreadPuid);
		ds.put("parentOccThreadPuid", parentOccThreadPuid);
		
		try {
			resultList = (ArrayList<HashMap>) symcRemoteUtil.execute(
					peInterfaceService, "getProductEndItemABSOccPuidList", ds);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<String> absOccPuidList = null;
		if (resultList != null && resultList.size() > 0) {
			absOccPuidList = new ArrayList<String>();
		}
		
		for (int i = 0; resultList != null && i < resultList.size(); i++) {
			HashMap aHash = resultList.get(i);
			String absOccId = null;

			if (aHash.get("ABS_OCC_PUID") != null) {
				absOccId = aHash.get("ABS_OCC_PUID").toString();

				if (absOccId != null && absOccId.trim().length() > 0) {
					absOccPuidList.add(absOccId.trim());
				}
			}
		}

		return absOccPuidList;
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

		return ImportCoreService.conversionOptionCondition(this.processWindow,
				condition);
	}
}
