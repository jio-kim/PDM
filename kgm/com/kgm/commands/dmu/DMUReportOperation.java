package com.kgm.commands.dmu;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;


@SuppressWarnings("unused")
public class DMUReportOperation extends AbstractAIFOperation {
	
    private TCSession session;
	private static Registry registry;
	private File templateFile;	
	private static String dataFilePath;
	private static String imagePath;
	private static String excelFilePath;
	private static String excelTemplatePath;
	
	private static int pictureIndex = 0;			//imaget 삽입시 이미지 인덱스 정보를 저장
	private static int lastRow;						//image 삽입시 row정보를 저장
	private static ArrayList<String[]> arrCellList;	//읽어 온 result 데이터를 엑셀에 넣기 위한 배열로 저장
	private static HashMap<String, Integer> headLineMap;		//읽어 온 result 데이터 중 HeadLine 부분을 저장	
	
	
	public DMUReportOperation(TCSession session, String resultPath, String imagePath, String namePath) {
		this.session = session;
		registry = Registry.getRegistry(this);
		this.setDataFilePath(resultPath);
		this.setImagePath(imagePath);
		this.setExcelFilePath(namePath);
	}
	
	
	@Override
	public void executeOperation() {

		try {
			if(templateFileDownload("DMU_Report_Template")) {
				if(templateFile.isFile()) {
					excelTemplatePath = templateFile.getPath();
						//최대 5만개에 달하는 대용량 처리를 고려하여 텍스트 입력과 이미지 입력을 분리하여 처리 
						drawSheetText();		//텍스트 입력
						drawSheetImage();	//이미지 입력
					
				} else {
					MessageBox.post("Can't find DMU template file.", "ERROR", MessageBox.ERROR);
				}
			} else {
				MessageBox.post("Can't download DMU template file.", "ERROR", MessageBox.ERROR);
			}
		} catch (Exception e) {
			MessageBox.post(e.toString(), "ERROR", MessageBox.ERROR);
			e.printStackTrace();
		}
	}

	
	/**
	 * 템플릿 다운받기
	 * @param type
	 * @return file
	 * @throws TCException 
	 * @throws Exception
	 */
	private boolean templateFileDownload(String datasetname) throws Exception{
		TCComponent[] searchedDatasetList = CustomUtil.queryComponent("Dataset...",
				new String[] {"Name", "DatasetType"},
				new String[] { datasetname, "MSExcel" });
		
		if(searchedDatasetList != null && searchedDatasetList.length != 0){
			TCComponentDataset tempDataset = (TCComponentDataset)searchedDatasetList[0];
			File[] file =  tempDataset.getFiles("excel");
			templateFile = file[0];
			return true;
		}else{
			throw new Exception("Cannot find excelTemplate file");
		}
	}
	
	
	/**
	 * 엑셀 템플릿에 Text 데이터 입력하는 함수
	 * @throws Exception 
	*/
	private static void drawSheetText() throws Exception {
		createExcelData();
		
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(excelTemplatePath)); // 엑셀 템플릿 파일
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);	//첫번째 시트의 데이터를 sheet 변수에 담는다
		
		String[] headCellName = registry.getStringArray("DMUReportOperation.HeadCell");
		String[] CellNumber = registry.getStringArray("DMUReportOperation.ValidCellNumber");
		
		if(headCellName == null || headCellName.length <= 0) {			
			throw new Exception("Header 속성 Mapping 정보가 옳바르지 않습니다. 관리자에게 문의하세요.");
		} else {			
			for(int i=0; i<arrCellList.size(); i++) {
				String[] rowContents = arrCellList.get(i);
				
				//Row 생성
				int targetRowMap = headLineMap.get("Number");
				String targetRow = rowContents[targetRowMap];
				HSSFRow row = sheet.createRow(Integer.parseInt(targetRow));
				row.setHeight((short)(225*4));						
				
				//Cell 정보 입력
				for(int j=0; j<headCellName.length; j++) {
					int targetCell = Integer.parseInt(CellNumber[j]);
					int targetMapNum = headLineMap.get(headCellName[j]);
					String targetCellValue = rowContents[targetMapNum];
					
					HSSFCell cell = row.createCell(targetCell);
					cell.setCellValue(targetCellValue);					
				}
			}
		}
		
		FileOutputStream fileOut = new FileOutputStream(getExcelFilePath());		
		wb.write(fileOut);
		fileOut.close();
	}
	
	/**
	 * 텍스트 파일로 부터 읽은 모든 데이터를 ArrayList에 저장하는 함수
	 * @throws Exception 
	*/
	public static void createExcelData() throws Exception {
		try {
			boolean headLineFlag = true;
			String roadedText="";
			arrCellList = new ArrayList<String[]>();

			BufferedReader bffrdReader = new BufferedReader(new FileReader(getDataFilePath()));
			while ((roadedText = bffrdReader.readLine()) != null) {			
				
				/*
				 * 읽어들인 파일에 아스키코드 0X00의 NUL바이트 공백이 존재하여 정상적인 편집이 불가.
				 * 이를 제거하고자 형변환 시켜 int 0 인 (NUL바이트)공백을 제외하고 일반 문자들만
				 * 다시 String타입으로 lineContents에 담음
				*/
				char chr[] = roadedText.toCharArray();
				String lineContents = "";
				
				for(int j=0; j<chr.length; j++) {
					if((int)chr[j] != (int)0)
					lineContents = lineContents + chr[j];
				}

				/*
				 * lineContents에 담긴 한줄의 문장을 
				 * "부터 읽어들여 엑셀에 담기 시작,
				 * ###가 있는 문장이 나타나면 루프를 종료한다.
				*/				
				if(lineContents != null) {				
					if(lineContents.matches(".*###")) {	//한 줄씩 읽어들여 ###에 도달하면 루프 종료
						break;
					} else if(lineContents.matches(".*\"")) {	// "가 포함 된 줄만 읽는다.						
						String tempLine = lineContents.substring(lineContents.indexOf("\"")+1);	// "부터 문장 끝까지 변수 담는다.
						String[] arrLineContents = tempLine.split("\",\"");
						//첫 출은 headLine이므로 별도 저장
						if(headLineFlag) {
							setHeader(arrLineContents);
							headLineFlag = false;
						} else {
							arrCellList.add(arrLineContents);
						}
					}
				}					
			}			
			
			bffrdReader.close();
		} catch (IOException e) {
			e.printStackTrace(); // 에러가 있다면 메시지 출력
		}	
	}
	
	
	/**
	 * HeadLine 정보 저장 함수
	*/
	private static void setHeader(String[] headLine) throws Exception {
		headLineMap = new HashMap<String, Integer>();
		
		for(int i=0; i<headLine.length; i++) {
			headLineMap.put(headLine[i], i);
		}
		
		//생성된 headLine이 올바른지 검증
		if(headLineMap == null || headLineMap.isEmpty()) {
			throw new Exception(registry.getString("DMUReportOperation.MSG.HeadError"));	
		} else {
			String[] headCellName = registry.getStringArray("DMUReportOperation.HeadCell");
			for(int j=0; j<headCellName.length; j++) {
				if(headLineMap.get(headCellName[j])==null) {
					MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("DMUReportOperation.MSG.HeadError"), "ERROR", MessageBox.ERROR);
					throw new Exception(registry.getString("DMUReportOperation.MSG.HeadError"));													
				}
			}			
		}
	}
	

	/**
	 * 	10진수의 숫자인지 판별 하는 함수
	*/
	public static boolean isStringDouble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	
	/**
	 * 엑셀 템플릿에 이미지 생성
	*/
	private static void drawSheetImage() throws IOException {
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(getExcelFilePath())); // 원본엑셀파일
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);	//첫번째 시트의 데이터를 sheet 변수에 담는다
		
		makeImageFileList(wb, sheet, getImagePath());		

		FileOutputStream fileOut = new FileOutputStream(getExcelFilePath()); // 이미지 삽입된 엑셀파일
		wb.write(fileOut);
		fileOut.close();				

		MessageBox.post("Success!", "Success", MessageBox.INFORMATION);
	}
	
	
	/**
	 * 이미지 파일 선별작업 수행
	*/
	public static void makeImageFileList(HSSFWorkbook wb, HSSFSheet sheet, String filePathLoop) throws IOException {
		lastRow = 0;
		
		File fileLoop = new File(filePathLoop);
		
		if (fileLoop.isDirectory()) { // 디렉토리면 ...
			File subLoop[] = fileLoop.listFiles(); // 해당 디렉토리의 파일 목록을 구함.
			
			for (int i = 0; i < subLoop.length; i++) { // 파일 목록 만큼 반복
				if (subLoop[i].isFile()) { // sub[i] 가 파일이면 ...				
					String fileFullName = subLoop[i].getName();
					String fileExt = fileFullName.substring(fileFullName.lastIndexOf(".")+1).toUpperCase();
					
					if(fileExt.equals("JPG") || fileExt.equals("JPEG") || fileExt.equals("PNG")) {
//						if(fileExt.equals("JPG") || fileExt.equals("JPEG") || fileExt.equals("PNG") || fileExt.equals("BMP") || fileExt.equals("GIF") || fileExt.equals("TIF")) {

						importImage(sheet, wb, subLoop[i].getPath());
					}
//				} else if (subLoop[i].isDirectory()){
//					makeFileList(sheet, wb, filePathLoop + "/" + subLoop[i].getName());
				}
			}
		}
	}	

	
	/**
	 * 실제 이미지를 엑셀에 삽입하는 함수
	 */
    private static void importImage( HSSFSheet sheet, HSSFWorkbook wb, String imagePath ) throws IOException {
    	
    	int dx1 = 0;
    	int dy1 = 0;
    	int dx2 = 0;		
    	int dy2 = 0;		
    	short col1 = 1;
    	int row1 = lastRow+1;
    	short col2 = (short) (col1+1);
    	int row2 = lastRow+2;
    	
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        HSSFClientAnchor anchor;
        anchor = new HSSFClientAnchor(dx1,dy1,dx2,dy2,col1,row1,col2,row2); // 이미지 크기조절은 여기서..
        anchor.setAnchorType( 1 );	//셀에 이미지 자동 맞춤
        
        patriarch.createPicture(anchor, loadPicture( imagePath, wb )); // 삽입 할 이미지
        lastRow++;
    }

    
    /**
     * 이미지 로드 함수
     */
    private static int loadPicture( String path, HSSFWorkbook wb ) throws IOException {

        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        
        try {
            fis = new FileInputStream(path);
            bos = new ByteArrayOutputStream();
            int c;
            while ( (c = fis.read()) != -1) {
                bos.write( c );
            }

            String pictureType = path.substring(path.lastIndexOf(".")+1);        
            
            if("PNG".equals(pictureType.toUpperCase())) {
            	pictureIndex = wb.addPicture(bos.toByteArray(), HSSFWorkbook.PICTURE_TYPE_PNG);
            } else {
            	pictureIndex = wb.addPicture( bos.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG);
            }
        } finally {
            if (fis != null) fis.close();
            if (bos != null) bos.close();
        }
        return pictureIndex;
    }
	
	/**
	 * @return the dataFilePath
	 */
	public static String getDataFilePath() {
		return dataFilePath;
	}

	/**
	 * @param dataFilePath the dataFilePath to set
	 */
	public void setDataFilePath(String dataFilePath) {
		DMUReportOperation.dataFilePath = dataFilePath;
	}

	/**
	 * @return the imagePath
	 */
	public static String getImagePath() {
		return imagePath;
	}

	/**
	 * @param imagePath the imagePath to set
	 */
	public void setImagePath(String imagePath) {
		DMUReportOperation.imagePath = imagePath;
	}

	/**
	 * @return the excelFilePath
	 */
	public static String getExcelFilePath() {
		return excelFilePath;
	}

	/**
	 * @param excelFilePath the excelFilePath to set
	 */
	public void setExcelFilePath(String excelFilePath) {
		DMUReportOperation.excelFilePath = excelFilePath;
	}
}
