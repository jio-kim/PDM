package com.symc.plm.me.sdv.service.migration.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class PEIFBOMDataExcelToXML {

	private String workFolderPath;
	
	private Document lineOperationBOMXMLDoc = null;
	private Document operationEndItemBOMXMLDoc = null;
	private Document operationActivityBOMXMLDoc = null;
	private Document operationSubsidiaryBOMXMLDoc = null;
	private Document operationFacilityBOMXMLDoc = null;
	private Document operationToolBOMXMLDoc = null;
	
	public PEIFBOMDataExcelToXML(String workFolderPath){
		this.workFolderPath = workFolderPath;
	}
	
	public boolean isErrorExist(){
		
		boolean isErrorExist = false;
		
		if(this.lineOperationBOMXMLDoc!=null){
			NodeList childNodeList = this.lineOperationBOMXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}

		if(this.operationEndItemBOMXMLDoc!=null){
			NodeList childNodeList = this.operationEndItemBOMXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}

		if(this.operationActivityBOMXMLDoc!=null){
			NodeList childNodeList = this.operationActivityBOMXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}

		if(this.operationSubsidiaryBOMXMLDoc!=null){
			NodeList childNodeList = this.operationSubsidiaryBOMXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}

		if(this.operationFacilityBOMXMLDoc!=null){
			NodeList childNodeList = this.operationFacilityBOMXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}
		
		if(this.operationToolBOMXMLDoc!=null){
			NodeList childNodeList = this.operationToolBOMXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}

		return isErrorExist;
	}
	
	/**
	 * Work Folder에 저장된 Excel 파일들을 읽어서 각각의 BOMLine Data를 저장한 XML Document를 생성한다.
	 */
	public void readAndMakeBOMLineDataXML(){
		readAndMakeLineToOperationBOMXMLDocument();
		readAndMakeOperationToEndItemBOMXMLDocument();
		readAndMakeOperationToSubsidiaryBOMXMLDocument();
		readAndMakeOperationToFacilityBOMXMLDocument();
		readAndMakeOperationToToolBOMXMLDocument();
		readAndMakeOperationToActivityBOMXMLDocument();
	}
	
	public Document readAndMakeLineToOperationBOMXMLDocument(){
		String exclFilePath = workFolderPath+"\\BOM\\21-라인-공법.xls";
		String targetSheetName = "공법BOM";
		int leftBlankColumnCount = 1;
		String rootElementName = "OperationBOMLines";
		String rowElementName = "OperationBOMLine";
		String xmlFileSavePath = workFolderPath+"\\OperationBOMLines.xml";
		
		String[] tcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};
		String[] excelColumnNames = 
				new String[]{"Plant", "Shop Code", "Product No", "Line Code", "공정편성버젼", "관리번호-차종", "관리번호-라인", "관리번호", "공정편성버젼", "Option"};
		String[] defineColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String"};

		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[2];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("LineItemId",
				new int[]{1,2,4,3,5});
		specialAttributeDefineData[1] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{6,7,8,9});
		
		lineOperationBOMXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				tcColumnNames, excelColumnNames, defineColumnTypes,
				specialAttributeDefineData);
	
		return lineOperationBOMXMLDoc;
	}
	
	public Document getLineToOperationBOMXMLDocument(){
		return lineOperationBOMXMLDoc;
	}
	
	public Document readAndMakeOperationToEndItemBOMXMLDocument(){
		String exclFilePath = workFolderPath+"\\BOM\\41-공법-일반자재.xls";
		String targetSheetName = "일반자재BOM";
		int leftBlankColumnCount = 1;
		String rootElementName = "EndItemBOMLines";
		String rowElementName = "EndItemBOMLine";
		String xmlFileSavePath = workFolderPath+"\\EndItemBOMLines.xml";
		
		String[] tcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O"};
		String[] excelColumnNames = 
				new String[]{"Plant", "Shop Code", "Product No", "Line Code", "공정편성버젼", "관리번호-차종", "관리번호-라인", "관리번호", "공정편성버젼", "Part No", "EBOM ABS Occurrence PUID", "EBOM Occurrence PUID", "Function Part Number", "자재SEQ"};
		String[] defineColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String"};
		
		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[2];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("LineItemId",
				new int[]{1,2,4,3,5});
		specialAttributeDefineData[1] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{6,7,8,9});
		
		operationEndItemBOMXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				tcColumnNames, excelColumnNames, defineColumnTypes,
				specialAttributeDefineData);
	
		return operationEndItemBOMXMLDoc;
	}
	
	public Document getOperationToEndItemBOMXMLDocument(){
		return operationEndItemBOMXMLDoc;
	}
	
	public Document readAndMakeOperationToActivityBOMXMLDocument(){
		String exclFilePath = workFolderPath+"\\BOM\\20-공법-Activity.xls";
		String targetSheetName = "ActivityBOM";
		int leftBlankColumnCount = 1;
		String rootElementName = "ActivityLines";
		String rowElementName = "ActivityLine";
		String xmlFileSavePath = workFolderPath+"\\ActivityLines.xml";
		
		String[] tcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};
		String[] excelColumnNames = 
				new String[]{"Plant", "Shop Code", "Product No", "Line Code", "공정편성버젼", "관리번호-차종", "관리번호-라인", "관리번호", "공정편성버젼", "작업순서"};
		String[] defineColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String"};

		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[2];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("LineItemId",
				new int[]{1,2,4,3,5});
		specialAttributeDefineData[1] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{6,7,8,9});
		
		operationActivityBOMXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				tcColumnNames, excelColumnNames, defineColumnTypes,
				specialAttributeDefineData);
	
		return operationActivityBOMXMLDoc;
	}
	
	public Document getOperationToActivityBOMXMLDocument(){
		return operationActivityBOMXMLDoc;
	}
	
	public Document readAndMakeOperationToSubsidiaryBOMXMLDocument(){
		String exclFilePath = workFolderPath+"\\BOM\\42-공법-부자재.xls";
		String targetSheetName = "부자재BOM";
		int leftBlankColumnCount = 1;
		String rootElementName = "SubsidiaryBOMLines";
		String rowElementName = "SubsidiaryBOMLine";
		String xmlFileSavePath = workFolderPath+"\\SubsidiaryBOMLines.xml";
		
		String[] tcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O"};
		String[] excelColumnNames = 
				new String[]{"Plant", "Shop Code", "Product No", "Line Code", "공정편성버젼", "관리번호-차종", "관리번호-라인", "관리번호", "공정편성버젼", "부품번호", "OPTION", "소요량", "조구분", "자재SEQ"};
		String[] defineColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "double", "String", "String"};

		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[2];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("LineItemId",
				new int[]{1,2,4,3,5});
		specialAttributeDefineData[1] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{6,7,8,9});
		
		operationSubsidiaryBOMXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				tcColumnNames, excelColumnNames, defineColumnTypes,
				specialAttributeDefineData);
	
		return operationSubsidiaryBOMXMLDoc;
	}
	
	public Document getOperationToSubsidiaryBOMXMLDocument(){
		return operationSubsidiaryBOMXMLDoc;
	}
	
	public Document readAndMakeOperationToFacilityBOMXMLDocument(){
		String exclFilePath = workFolderPath+"\\BOM\\44-공법-설비.xls";
		String targetSheetName = "설비BOM";
		int leftBlankColumnCount = 1;
		String rootElementName = "FacilityBOMLines";
		String rowElementName = "FacilityBOMLine";
		String xmlFileSavePath = workFolderPath+"\\FacilityBOMLines.xml";
		
		String[] tcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"};
		String[] excelColumnNames = 
				new String[]{"Plant", "Shop Code", "Product No", "Line Code", "공정편성버젼", "관리번호-차종", "관리번호-라인", "관리번호", "공정편성버젼", "설비번호", "설비 수량", "SEQ"};
		String[] defineColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "int", "String"};

		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[2];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("LineItemId",
				new int[]{1,2,4,3,5});
		specialAttributeDefineData[1] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{6,7,8,9});
		
		operationFacilityBOMXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				tcColumnNames, excelColumnNames, defineColumnTypes,
				specialAttributeDefineData);
	
		return operationFacilityBOMXMLDoc;
	}
	
	public Document getOperationToFacilityBOMXMLDocument(){
		return operationFacilityBOMXMLDoc;
	}
	
	
	public Document readAndMakeOperationToToolBOMXMLDocument(){
		String exclFilePath = workFolderPath+"\\BOM\\43-공법-공구.xls";
		String targetSheetName = "공구BOM";
		int leftBlankColumnCount = 1;
		String rootElementName = "ToolBOMLines";
		String rowElementName = "ToolBOMLine";
		String xmlFileSavePath = workFolderPath+"\\ToolBOMLines.xml";
		
		String[] tcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};
		String[] excelColumnNames = 
				new String[]{"Plant", "Shop Code", "Product No", "Line Code", "공정편성버젼", "관리번호-차종", "관리번호-라인", "관리번호", "공정편성버젼", "공구번호", "수량", "Torque", "설비SEQ"};
		String[] defineColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "int", "String", "String"};

		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[2];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("LineItemId",
				new int[]{1,2,4,3,5});
		specialAttributeDefineData[1] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{6,7,8,9});
		
		operationToolBOMXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				tcColumnNames, excelColumnNames, defineColumnTypes,
				specialAttributeDefineData);
	
		return operationToolBOMXMLDoc;
	}
	
	public Document getOperationToToolBOMXMLDocument(){
		return operationToolBOMXMLDoc;
	}
}
