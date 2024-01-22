package com.symc.plm.me.sdv.operation.accountabilitycheck;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.graphics.RGB;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Accountability Check 결과를 Excel로 출력 할 수 있도록 기능을 추가해주세요.
 * [SR150106-015] 20151028 taeku.jeong	이종화 차장님의 요구로 추가된 SR에 대한 대응
 *  
 * @author Taeku
 *
 */
public class AccountabilityDataExcelExporter {

	private TCSession session;
	private String exportTemplateName = "M7_TEM_DocItemId_AccountabilityReport";
	
	private String targetPath;
	private String exportTargetDir;
	
	private XSSFWorkbook excelWorkbook = null;
	private XSSFSheet excelWorkSheet = null;
	private XSSFRow beforMadeExcelRow = null;
	private XSSFRow createdExcelRow = null;
	
	private int currentExcelRowIndex = 0;

	// Template에서 Data를 채워넣기 시작 해야하는 Row의 Index
	private int dataStartRowIndex = 6;
	// Template의 외곽선이 그려져있는 마지막 Row의 Index
	private int templateBottomRowIndex = 50;
	// Template에서 Data를 출력하기 시작해야 하는Column의 Index
	private int startColumnIndex = 1;
	
	public void readyFile(String targetPath, TCSession session)  throws Exception {
		
		if(this.exportTemplateName==null || (this.exportTemplateName!=null && this.exportTemplateName.trim().length()<1)){
			throw new Exception("The template used to create the document must be defined.");
		}
		
		if(targetPath==null || (targetPath!=null && targetPath.trim().length()<4)){
			throw new Exception("Must specify where the document is to be created.");
		}else{
			this.targetPath = targetPath;
		}
		
		if(session!=null){
			this.session = session; 
		}
		
		FilePathPars filepathpars = null;
		try {
			filepathpars = new FilePathPars(targetPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.exportTargetDir = filepathpars.getStrFileDir();

		//String xlsFileName = filepathpars.getStrFileName();
		//String xlsFileExt = filepathpars.getStrFileExt();
		
		if(this.exportTargetDir!=null && this.exportTargetDir.trim().length()>0){
			downloadTemplateAndRename();
		}
		
		this.currentExcelRowIndex = 0;
		
		openWorkSheet();
		
	}

	public void writeRow(Hashtable<String, String> rowDataHash, String[] keyIndexList,
			RGB matchedTypeRGB){
		
		boolean needToCreateRow = false;
		
		if(currentExcelRowIndex==0 || (currentExcelRowIndex<dataStartRowIndex)){
			// 아직 한번도 출력된 기록이 없음.
			currentExcelRowIndex = dataStartRowIndex;
		}
		
		// 하한선으로 정한 Row Index보다 커지는 경우 Row를 생성 하도록 한다.
		if(currentExcelRowIndex>=(templateBottomRowIndex-1)){
			needToCreateRow = true;
		}
		
		if(needToCreateRow==true){
			// Excel의 Row를 추가한다.
			// 이때 주의해야 할 것이 기존의 Sheet에 있던 Row에 끼워넣기해야 하므로 shiftRows()를 이용해야 한다.
			excelWorkSheet.shiftRows(currentExcelRowIndex, excelWorkSheet.getLastRowNum(), 1);
			createdExcelRow = excelWorkSheet.createRow(currentExcelRowIndex);
		}else{
			createdExcelRow = excelWorkSheet.getRow(currentExcelRowIndex);
		}
		
		// Excel Column에 표기되는 Data의 수를 초기 Template을 기준으로 26으로 정했지만
		// Template이 변경되면 따라서 변경 되도록 처리함.
		// com.myapp.boptest.AccountabilityCheckResultExport Class에 정의된 전역변수 keyIndexList를 참조
		// 하면 된다.
		int columnLength = 26;
		if(keyIndexList!=null){
			columnLength = keyIndexList.length;
		}
		
		for (int dataIndex = 0; dataIndex < columnLength; dataIndex++) {
			
			// Hash Table의 Key 값을 Index 할 수 있는 뭔가가 필요함.
			String keyIndexValue = keyIndexList[dataIndex];
			String strValue = " ";
			if(keyIndexValue!=null && keyIndexValue.trim().length()>0){
				if(rowDataHash.get(keyIndexValue)!=null){
					strValue = rowDataHash.get(keyIndexValue);
				}
			}
			
			if(strValue==null || (strValue!=null && strValue.trim().length()<1)){
				strValue = " ";
			}
			
			int columnIndex = this.startColumnIndex + dataIndex;
	
			// Cell에 데이타 입력.
			XSSFCell currentCell = null;
			try {
				currentCell = writeCell(createdExcelRow, columnIndex, strValue, needToCreateRow);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		
		// 추가된 Row의 Cell Style 설정.
		if(beforMadeExcelRow!=null && needToCreateRow==true){
			for (int dataIndex = 0; dataIndex < columnLength; dataIndex++) {
				int columnIndex = this.startColumnIndex + dataIndex;
				
				String keyIndexValue = keyIndexList[dataIndex];
				if(keyIndexValue==null || (keyIndexValue!=null && keyIndexValue.trim().length()<1)){
					continue;
				}

				// 이전 Row의 Cell을 가져온다.
				XSSFCell currentCell = createdExcelRow.getCell(columnIndex);
				XSSFCell beforRowCell = beforMadeExcelRow.getCell(currentCell.getColumnIndex());
				if(beforRowCell!=null && beforRowCell.getCellStyle()!=null){
					currentCell.setCellStyle(beforRowCell.getCellStyle());
				}
			}
		}
		
		// 배경색상 칠한다.
		java.awt.Color color = new Color(matchedTypeRGB.red, matchedTypeRGB.green, matchedTypeRGB.blue);
		XSSFColor aXSSFColor  = new XSSFColor(color);

		for (int dataIndex = 0; dataIndex < columnLength; dataIndex++) {
			int columnIndex = this.startColumnIndex + dataIndex;
			
			String keyIndexValue = keyIndexList[dataIndex];
			if(keyIndexValue==null || (keyIndexValue!=null && keyIndexValue.trim().length()<1)){
				continue;
			}

			XSSFCell currentCell = createdExcelRow.getCell(columnIndex);
			XSSFCellStyle cellStyle = currentCell.getCellStyle();
			XSSFCellStyle cellStyle2 = (XSSFCellStyle)cellStyle.clone();
			
			cellStyle2.setFillForegroundColor(aXSSFColor);
			cellStyle2.setFillPattern(CellStyle.SOLID_FOREGROUND);
			currentCell.setCellStyle(cellStyle2);
		}

		currentExcelRowIndex++;
		beforMadeExcelRow = createdExcelRow;
	}

	public void closeWorkSheet() throws Exception {
		
		if(excelWorkSheet==null){
			return;
		}
		if(excelWorkbook==null){
			return;
		}
		
		try {
		    // Excel File 저장.
		    FileOutputStream fileOut = new FileOutputStream(targetPath);
		    excelWorkbook.write(fileOut);
		    fileOut.close();
		    
		} catch (Exception err) {
			throw err;
		} finally {
			
			if (excelWorkSheet != null) {
				excelWorkSheet = null;
			}
			
			if (excelWorkbook != null) {
				excelWorkbook = null;
			}
			
		}
	}


	/**
	 * Template Dataset으로 부터 TcFile을 가져와서 
	 * 			생성하고자 하는 파일명으로 Rename.
	 * @throws Exception
	 */
	private void downloadTemplateAndRename() throws Exception{
		
		TCPreferenceService prefService = this.session.getPreferenceService();
		String itemID = prefService.getStringValueAtLocation(this.exportTemplateName, TCPreferenceLocation.OVERLAY_LOCATION);
		TCComponentItemType itemType = (TCComponentItemType) this.session.getTypeComponent("Item");

		TCComponentDataset excelTemplateDataset = null;
		if(itemType.findItems(itemID) != null) {
			TCComponentItem item = itemType.findItems(itemID)[0];
			if(item.getReleasedItemRevisions() != null) {
				
				TCComponentItemRevision revision = null;
				TCComponentItemRevision[] releasedRevisions =item.getReleasedItemRevisions();
				if(releasedRevisions!=null && releasedRevisions.length>0){
					
					TCComponentItemRevision maxRevision = null;
					for (int i = 0; i < releasedRevisions.length; i++) {
						
						if(maxRevision==null){
							maxRevision= releasedRevisions[i];
							continue;
						}

						String maxRevId = maxRevision.getProperty("item_revision_id");
						String currentRevId = releasedRevisions[i].getProperty("item_revision_id");
						
						if(maxRevId.compareToIgnoreCase(currentRevId)<0){
							maxRevision = releasedRevisions[i];
						}
					}
					
					revision = maxRevision;
				}else{
					revision = item.getLatestItemRevision();
				}
				
				if(revision!=null){
					
			        AIFComponentContext[] contextList = revision.getChildren();

		            for(AIFComponentContext context : contextList) {
		                InterfaceAIFComponent component = context.getComponent();
		                if(component instanceof TCComponentDataset){
		                	String compType = component.getType();
		                	if(compType!=null && compType.trim().indexOf("Excel")>=0){
		                		excelTemplateDataset = (TCComponentDataset) component;
		                		break;
		                	}
		                }
		            }
					System.out.println("excelTemplateDataset = "+excelTemplateDataset);
				}
			}
		}
		
		if(excelTemplateDataset!=null){
			try {
				// CheckSheet Template 파일 다운로드.
				TCComponentTcFile tcfile = excelTemplateDataset.getTcFiles()[0];
				File templateFile = tcfile.getFile(this.exportTargetDir);
				
				if(templateFile==null){
					throw new Exception("Can't find accountability report template file");
				}
				
				// Template 파일명을 정의된 Export 대상 파일 이름으로 Rename. 
				File xlsFile = new File(targetPath);
				
				templateFile.renameTo(xlsFile);
			}catch (Exception err) {
				throw err;
			}finally{
				
			}
		}else{
			throw new Exception("Can't find accountability report template data set");
		}
	}

	
	
	//-----------------------------------------
	// 출력하는 부분은 아래에 정리 한다.
	//-----------------------------------------
	
	private void openWorkSheet() throws Exception {
		
		Exception err1 = null;
		try {
			// File 객체 가져오기.
			excelWorkbook = new XSSFWorkbook(new FileInputStream(targetPath));
			
//			// Workbook 객체를 가져온다.
//			workbook = new HSSFWorkbook(filesystem);
			
			// Worksheet 객체를 가져온다.
			// Worksheet가 여러개인 경우 필요한 WorkSheet를 추가로 활용하는 부분에대해서는
			// 구체적인 정의가 확정되면 다시 추가 구현 하면 될것 같다.
			// 현재 추정하기에는 별다른 추가 Sheet가 필요하지는 않을것 같다.
			//int intSheetNum = workbook.getNumberOfSheets();
			//String strSheetName = workbook.getSheetName(0);
			excelWorkSheet = excelWorkbook.getSheetAt(0);
		    
		} catch (Exception err) {
			err1 = err;
			throw err;
		} finally {
			if(err1!=null){
				if (excelWorkSheet != null) {
					excelWorkSheet = null;
				}
				
				if (excelWorkbook != null) {
					excelWorkbook = null;
				}
			}
		}
	}
	
	/**
	 * Excel File의 Column에 적용될 Data를 cell 단위로 기록한다.
	 * 
	 * @param excelSheetRow
	 * @param columnIndex
	 * @param strValue
	 * @return
	 * @throws Exception
	 */
	private XSSFCell writeCell(XSSFRow excelSheetRow, int columnIndex, String strValue, boolean isNewRow) throws Exception {
		XSSFCell cell = null;
		try {
			if(isNewRow==true){
				cell = excelSheetRow.createCell(columnIndex);
			}else{
				cell = excelSheetRow.getCell(columnIndex);
			}
			cell.setCellValue(new XSSFRichTextString(strValue));
			
		}
		catch (Exception err) {
			err.printStackTrace();
			throw err;
		}	
		return cell;
	}		
	
	/**
	 * 필요에따라 Cell의 Style을 정의해야 하는 경우에 사용할 수있을것임.
	 * @param ecxelSheetCell
	 * @throws Exception
	 */
	private void setCellStyle(XSSFCell ecxelSheetCell) throws Exception {
		
		// 양식에서 Data 입력 시작 위치 설정 이후에 이전 Row의 양식을 복사해서 적용 하도록
		// 구현 하면 될 것으로 생각된다.
		// Template이 정해지면 Cell의 Style이 정의된것으로 보이는데...
		// 이후에 Cell Style을 복사해서 붙이는 형식으로 처리하면 별도로 Cell Style을
		// 정의하고 구현하는 수고는 필요 없을 것으로 보인다.
		
		try {
			CellStyle style = excelWorkbook.createCellStyle();
			
			// CellStyle.BORDER_MEDIUM
			// CellStyle.BORDER_DOTTED
			// CellStyle.BORDER_THIN
			
			style.setBorderTop(CellStyle.BORDER_MEDIUM);
			style.setBorderLeft(CellStyle.BORDER_MEDIUM);				
			style.setBorderRight(CellStyle.BORDER_MEDIUM);
			style.setBorderBottom(CellStyle.BORDER_MEDIUM);
			
			ecxelSheetCell.setCellStyle(style);
		}
		catch (Exception err) {
			throw err;
		}	
	}

	/**
	 * Excel Template의 양식중에 M Product: 와 Shop 이라고 기록된 Cell의 값을 채워넣는 함수
	 * @param srcWindow M Product로 기록된 Cell과 관련된 Data를 기록한다.
	 * @param targetWindow Shop으로 기록된 Cell과 관련된 Data를 기록한다. 
	 */
	public void printSrcAndTargetInfomation(TCComponentBOMWindow srcWindow,
			TCComponentBOMWindow targetWindow) {
		
		int title2Row = 1;
		int productColumnIndex = 2;
		int processColumnIndex = 22;
		
		System.out.println("srcWindow = "+srcWindow);
		System.out.println("targetWindow = "+targetWindow);
		
		String srcString = null;
		String srcItemId = null;
		String srcItemRevId = null;
		String srcItemRevName = null;
		TCComponentBOMLine srcTopBOMLine = null;
		try {
			srcTopBOMLine = srcWindow.getTopBOMLine();
			if(srcTopBOMLine!=null){
				srcItemId = srcTopBOMLine.getItem().getProperty("item_id");
				TCComponentItemRevision srcRevision = srcTopBOMLine.getItemRevision();
				if(srcRevision!=null){
					srcItemRevId = srcRevision.getProperty("item_revision_id");
					srcItemRevName = srcRevision.getProperty("object_name");
				}
				srcString = srcItemId+"/"+srcItemRevId+" "+srcItemRevName;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		String targetString = null;
		String targetItemId = null;
		String targetItemRevId = null;
		String targetItemRevName = null;
		TCComponentBOMLine targetTopBOMLine = null;
		try {
			targetTopBOMLine = targetWindow.getTopBOMLine();
			if(targetTopBOMLine!=null){
				targetItemId = targetTopBOMLine.getItem().getProperty("item_id");
				TCComponentItemRevision targetRevision = targetTopBOMLine.getItemRevision();
				if(targetRevision!=null){
					targetItemRevId = targetRevision.getProperty("item_revision_id");
					targetItemRevName = targetRevision.getProperty("object_name");
				}
				
				targetString = targetItemId+"/"+targetItemRevId+" "+targetItemRevName;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		XSSFRow title2InformationExcelRow = null;
		title2InformationExcelRow = excelWorkSheet.getRow(title2Row);
		
		System.out.println("srcString = "+srcString);
		System.out.println("targetString = "+targetString);
		
		try {
			XSSFCell productCell = title2InformationExcelRow.getCell(productColumnIndex);
			if(productCell==null){
				productCell = title2InformationExcelRow.createCell(productColumnIndex);			
			}
			productCell.setCellValue(new XSSFRichTextString(srcString));			

			XSSFCell shopCell = title2InformationExcelRow.getCell(processColumnIndex);
			if(shopCell==null){
				shopCell = title2InformationExcelRow.createCell(processColumnIndex);			
			}
			shopCell.setCellValue(new XSSFRichTextString(targetString));
		}
		catch (Exception err) {
			err.printStackTrace();
		}	
		
		
	}

}
