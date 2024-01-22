package com.symc.plm.me.sdv.service.migration.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class PEIFMasterDataExcelToXML {

	// PE Interface를 위해 저장된 Excel파일이 저장된 폴더의 Path
	// ex ) D:\TcM_Interface\Interface
	//       위의 경로를 지정하면 실제 Master Data는 D:\TcM_Interface\Interface\MASTER 폴더에서 읽음.
	private String workFolderPath;
	
	// Operation 정보를 가진 XML Document 객체
	private Document operationMasterXMLDoc = null;
	// Activity 정보를 가진 XML Document 객체
	private Document activityMasterXMLDoc = null;
	// 설비정보를 가진 XML Document 객체
	private Document facilityMasterXMLDoc = null;
	// 공구정보를 가진 XML Document 객체
	private Document toolMasterXMLDoc = null;
	
	/**
	 * Class 생서자
	 * @param workFolderPath PE Interfac할 Data가 저장된 기본 폴더의 경로
	 */
	public PEIFMasterDataExcelToXML(String workFolderPath){
		this.workFolderPath = workFolderPath;
	}
	
	/**
	 * Work Folder에 저장된 Excel 파일들을 읽어서 각각의 Master Data를 저장한 XML Document를 생성한다.
	 */
	public void readAndMakeMasterDataXML(){
		readAndMakeOperationMasterXMLDocument();
		readAndMakeActivityMasterXMLDocument();
		readAndMakeFacilityMasterXMLDocument();
		readAndMakeToolMasterXMLDocument();
	}
	
	public boolean isErrorExist(){
		
		boolean isErrorExist = false;
		
		if(this.activityMasterXMLDoc!=null){
			NodeList childNodeList = this.activityMasterXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}
		
		if(this.facilityMasterXMLDoc!=null){
			NodeList childNodeList = this.facilityMasterXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}
		
		if(this.operationMasterXMLDoc!=null){
			NodeList childNodeList = this.operationMasterXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}
		
		if(this.toolMasterXMLDoc!=null){
			NodeList childNodeList = this.toolMasterXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}

		return isErrorExist;
	}
	
	/**
	 * Operation Master Data가 정의된 Excel 파일의 내용을 가진 XML Document를 Return 한다.
	 * @return
	 */
	public Document getOperationMasterXMLDocument(){
		return operationMasterXMLDoc;
	}
	
	/**
	 * Activity Master Data가 정의된 Excel 파일의 내용을 가진 XML Document를 Return 한다.
	 * @return
	 */
	public Document getActivityMasterXMLDocument(){
		return activityMasterXMLDoc;
	}
	
	/**
	 * Facility Master Data가 정의된 Excel 파일의 내용을 가진 XML Document를 Return 한다.
	 * @return
	 */
	public Document getFacilityMasterXMLDocument(){
		return facilityMasterXMLDoc;
	}
	
	/**
	 * Tool Master Data가 정의된 Excel 파일의 내용을 가진 XML Document를 Return 한다.
	 * @return
	 */
	public Document getToolMasterXMLDocument(){
		return toolMasterXMLDoc;
	}
	
	private Document readAndMakeOperationMasterXMLDocument(){
		
		String exclFilePath = workFolderPath+"\\MASTER\\Master-20공법.xls";
		String targetSheetName = "공법Master";
		int leftBlankColumnCount = 1;
		String rootElementName = "OperationMaster";
		String rowElementName = "OperationItem";
		String xmlFileSavePath = workFolderPath+"\\OperationMaster.xml";
		
		String[] operationMasterTcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T"};
		String[] operationMasterColumnNames = 
				new String[]{"관리번호-차종", "관리번호-라인", "관리번호", "공정편성버젼", "작업위치", "공법명-국문", "공법명-영문", "작업자구분코드", "자재투입위치", "장착도면번호", "Station No.", "보안", "시스템", "관리번호", "Sequence", "하체작업여부", "대표차종 유무", "국문작업표준서 파일경로", "국문작업표준서 I/F 유무"};
		String[] operationMasterColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String"};
		
		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[1];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{1,2,3,4});
		
		operationMasterXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				operationMasterTcColumnNames, operationMasterColumnNames, operationMasterColumnTypes,
				specialAttributeDefineData);
	
		return operationMasterXMLDoc;
	}
	
	private Document readAndMakeActivityMasterXMLDocument(){
		
		String exclFilePath = workFolderPath+"\\MASTER\\Master-20Activity.xls";
		String targetSheetName = "ActivityMaster";
		int leftBlankColumnCount = 1;
		String rootElementName = "ActivityMaster";
		String rowElementName = "ActivityItem";
		String xmlFileSavePath = workFolderPath+"\\ActivityMaster.xml";
		
		String[] activityMasterTcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};
		String[] activityMasterColumnNames = 
				new String[]{"관리번호-차종", "관리번호-라인", "관리번호", "공정편성버젼", "작업순서", "작업약어", "변수", "난이도", "작업내용(국문)", "작업내용(영문)", "작업시간", "자동/정미/보조", "공구ID", "KPC", "KPC관리기준"};
		String[] activityMasterColumnTypes = 
				new String[]{"String", "String", "String","String", "String", "String", "String", "double", "String", "String", "double", "String", "String", "String", "String"};
		
		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[1];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{1,2,3,4});
		
		activityMasterXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				activityMasterTcColumnNames, activityMasterColumnNames, activityMasterColumnTypes,
				specialAttributeDefineData);
	
		return activityMasterXMLDoc;
	}
	
	private Document readAndMakeFacilityMasterXMLDocument(){
		
		String exclFilePath = workFolderPath+"\\MASTER\\Master-30설비.xls";
		String targetSheetName = "설비Master";
		int leftBlankColumnCount = 1;
		String rootElementName = "FacilityMaster";
		String rowElementName = "FacilityItem";
		String xmlFileSavePath = workFolderPath+"\\FacilityMaster.xml";
		
		String[] facilityMasterTcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S"};
		String[] facilityMasterColumnNames = 
				new String[]{"공장", "설비번호", "Main Name(국문)", "Main Name(영문)", "사용 용도-국문", "사용 용도-영문", "설비 사양-국문", "설비 사양-영문", "대분류", "중분류", "처리능력", "제작사", "도입국가", "설치년도", "변경내역문자", "차종코드(JIG)", "대분류(JIG)", "CAD파일경로"};
		String[] facilityMasterColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "int", "String", "String", "String", "String", "String", "String", "String"};
		
		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = null;
		
		facilityMasterXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				facilityMasterTcColumnNames, facilityMasterColumnNames, facilityMasterColumnTypes,
				specialAttributeDefineData);
	
		return facilityMasterXMLDoc;
	}
	
	private Document readAndMakeToolMasterXMLDocument(){
		
		String exclFilePath = workFolderPath+"\\MASTER\\Master-40공구.xls";
		String targetSheetName = "공구Master";
		int leftBlankColumnCount = 1;
		String rootElementName = "ToolMaster";
		String rowElementName = "ToolItem";
		String xmlFileSavePath = workFolderPath+"\\ToolMaster.xml";
		
		String[] toolMasterTcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U"};
		String[] toolMasterColumnNames = 
				new String[]{"공구번호", "공구명-국문", "공구명-영문", "대분류", "중분류", "공구 용도", "사양코드", "기술 사양-국문", "기술 사양-영문", "소요량 단위", "공구 재질", "토크값", "제작사", "업체/AF", "형상분류", "길이", "연결부 Size", "자석삽입여부", "Remark", "CAD파일경로"};
		String[] toolMasterColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "double", "String", "String", "String", "String"};
		
		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = null;
		
		toolMasterXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				toolMasterTcColumnNames, toolMasterColumnNames, toolMasterColumnTypes,
				specialAttributeDefineData);
	
		return toolMasterXMLDoc;
	}
	
}
