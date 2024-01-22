package com.symc.plm.me.sdv.service.migration.util;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExcelToXMLDocumentSaveUtil {
	
	//LineItemId='PTP-A2-I1-PVXA2015-00'인 OperationBOMLine Element 검색
    // result : //OperationBOMLine[@LineItemId='PTP-A2-I1-PVXA2015-00']
	
	/**
	 * 지정된 Excel 파일의 Sheet를 읽고 XMLDocument후 XML파일로 저장하는 기능을 수행한다.
	 * @param srcExcelFilePath 읽어 들일 Excel 파일의 Path
	 * @param targetSheetName Excel 파일에서 XML로 만들 Data를 읽어들일 Sheet의 이름
	 * @param rootElementName XMLDocument의 Root Element Name
	 * @param rowElementName Excel의 Row Data를 저장하는 Element Name 
	 * @param xmlFileSavePath	생성된 XML 파일을 저장할 경로
	 * @param tcColumnNames Excel의 Column 이름에 대응하는 Teamcenter의 Property 이름을 정의한다.
	 * @param excelColumnHeadText Excel의 Header에 해당하는 Row의 Column Text를 Row Element의 Attribute로 저장한다.
	 * @param targetColumnTypes
	 * @return 생성된 XMLDocument 객체를 Return 한다.
	 */
	public static Document getXMLDocumentFromExcel(String srcExcelFilePath, String targetSheetName, int leftBlankRowCount,
			String rootElementName, String rowElementName, String xmlFileSavePath,
			String[] tcColumnNames, String[] excelColumnHeadText, String[] targetColumnTypes,
			InterfaceSpecialAttributeDefineData[] specialAttributeDef){

		HSSFWorkbook  workbook = null;
		HSSFSheet activitySheet = null;
		try {
			workbook = new HSSFWorkbook(new FileInputStream(srcExcelFilePath));
			if(workbook!=null){
				activitySheet = workbook.getSheet(targetSheetName);
			}
		} catch (Exception err) {
			workbook = null;
		}
		
		if(activitySheet==null){
			return (Document)null;
		}
		
		// XML Document 객체를 생성한다.
		Document xmlDoc = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			if(docBuilder!=null){
				xmlDoc = docBuilder.newDocument();
			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		
		if(xmlDoc==null){
			return xmlDoc;
		}
		
		Element rootElement = xmlDoc.createElement(rootElementName);
		xmlDoc.appendChild(rootElement);
		
		// 전체 읽어 들일 Row의 갯수를 파악하기 위함.
		int rowCount = activitySheet.getPhysicalNumberOfRows();
		
		// Excel에서 읽은 Row의 순서에따른 Index를 기록한다.
		int dataCount = 1;
		for (int rowIndex = 2; activitySheet!=null && rowIndex < rowCount; rowIndex++) {
			HSSFRow currentRow = activitySheet.getRow(rowIndex);
			
			// 만약에 첫번째 Column에 기록된 문자열이 "EOF"이면 Excel에서 Data 읽기를 중단 한다.
			HSSFCell firstColumnCell = currentRow.getCell(0);
			if(firstColumnCell!=null){
				String tempString = firstColumnCell.getStringCellValue();
				if(tempString!=null && tempString.trim().equalsIgnoreCase("EOF")==true){
					break;
				}
			}
			
			// OperationItem Elements
			Element elementForRow = xmlDoc.createElement(rowElementName);
			rootElement.appendChild(elementForRow);
			
		
			// set attribute to OperationItem Elements
			Attr rowIndexAttr = xmlDoc.createAttribute("rowIndex");
			rowIndexAttr.setValue((""+dataCount));
			elementForRow.setAttributeNode(rowIndexAttr);
			
			String[] columnStringValues = new String[excelColumnHeadText.length];
			
			for (int targetDataIndex = 0; targetDataIndex < excelColumnHeadText.length; targetDataIndex++) {
				
				// Template에 따라 
				int excelColumnIndex = targetDataIndex+leftBlankRowCount;
				
				HSSFCell columnCell = currentRow.getCell(excelColumnIndex);
				
				String tcColumnNameString = tcColumnNames[targetDataIndex];
				String columnNameString = excelColumnHeadText[targetDataIndex];
				String columnTypeDefString = targetColumnTypes[targetDataIndex];
				String columnStrValue = "";
				
				if(columnCell==null){
					continue;
				}
				
				switch (columnCell.getCellType()) {
				case HSSFCell.CELL_TYPE_FORMULA:
					columnStrValue = columnCell.getCellFormula();
					break;

				case HSSFCell.CELL_TYPE_NUMERIC:
					columnStrValue = columnCell.getNumericCellValue()+"";
					break;

				case HSSFCell.CELL_TYPE_STRING:
					columnStrValue = columnCell.getStringCellValue();
					break;
					
				case HSSFCell.CELL_TYPE_BOOLEAN:
					columnStrValue = columnCell.getBooleanCellValue()+"";
					break;

				case HSSFCell.CELL_TYPE_BLANK:
					columnStrValue = "";
					break;

				case HSSFCell.CELL_TYPE_ERROR:
					columnStrValue = columnCell.getErrorCellValue()+"";
					break;
					
				default:
					columnStrValue = "";
					break;
				}
				
				columnStringValues[targetDataIndex] = columnStrValue;
				
				// Element 정의
				Element elementForColumn = xmlDoc.createElement(tcColumnNameString);
				
				// Column에 하당하는 Element의 Attribute로 PropertyDesc를 생성
				Attr descAttr = xmlDoc.createAttribute("PropertyDesc");
				descAttr.setValue(columnNameString);
				elementForColumn.setAttributeNode(descAttr);
				
				// Column에 하당하는 Element의 Attribute로 PropertyType을 생성
				Attr typeAttr = xmlDoc.createAttribute("PropertyType");
				typeAttr.setValue(columnTypeDefString);
				elementForColumn.setAttributeNode(typeAttr);
				
				// Column에 해당하는 Element의 Text를 설정한다.
				elementForColumn.appendChild(xmlDoc.createTextNode(columnStrValue));

				// Row Element에 생성된 Column Element를 추가한다.
				elementForRow.appendChild(elementForColumn);

			}
			
			// 특별히 정의된 RowElement에 대한 추가 Attribute 정의
			for (int i = 0; specialAttributeDef!=null && i < specialAttributeDef.length; i++) {
				
				String specialAttributeName = specialAttributeDef[i].getAttributeName();
				int[] valueIndexList = specialAttributeDef[i].getStringConstructionColumns();

				String madeAttributeValueStr = "";
				int strCount = 0;
				for (int j = 0; j < valueIndexList.length; j++) {
					// 입력할때 Excel의 Column을 0부터 시작 하는것으로 하고 보이는 Index데로 입력했기 때문에 
					// 이것에 대한 Data의 Index로 보정해 준다.
					// 예를 들어 Column B 부터 Data가 시작되면 Data를 정의할때 Index값으로 1을 입력한다.
					// 그러나 실제 Data의 측면에서는 엑셀에서의 Data 시작전 비워둔 Column의 수를 반영하면
					// 실제 Data에서는 0번 Index가 된다.
					int targetDataIndex = valueIndexList[j] - leftBlankRowCount;
					
					String tempString = columnStringValues[targetDataIndex];
					if(tempString==null || (tempString!=null && tempString.trim().length()<1)){
						tempString = "";
					}
					
					if(tempString==null || (tempString!=null && tempString.trim().length()<1)){
						continue;
					}else{
						tempString = tempString.trim();
					}
					
					if(strCount==0){
						madeAttributeValueStr = tempString;
					}else{
						madeAttributeValueStr = madeAttributeValueStr+"-"+tempString;
					}
					
					strCount++;
				}
				
				if(madeAttributeValueStr==null || (madeAttributeValueStr!=null && madeAttributeValueStr.trim().length()<1)){
					continue;
				}

				// 몇개의 Column 값을 모하 하나의 Attribute로 만든다.
				// 주로 특성값을 조합해 Item Id를 만들때 사용한다.
				Attr specialAttribute = xmlDoc.createAttribute(specialAttributeName.trim());
				specialAttribute.setValue(madeAttributeValueStr);
				elementForRow.setAttributeNode(specialAttribute);
				
			}
			
			dataCount++;
		}
		
		if(xmlDoc!=null){
			ExcelToXMLDocumentSaveUtil.saveXMLDocumentFile(xmlDoc, xmlFileSavePath);
		}else{
			System.out.println("XML creation error!!");
		}
		
		return xmlDoc;
	}

	public static void saveXMLDocumentFile(Document doc, String xmlFileSavePath){
		
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(xmlFileSavePath));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		if(transformer!=null){
			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}

		System.out.println("File saved!");

	}
	
}
