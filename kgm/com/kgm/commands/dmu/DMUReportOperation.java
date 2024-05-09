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
	
	private static int pictureIndex = 0;			//imaget ���Խ� �̹��� �ε��� ������ ����
	private static int lastRow;						//image ���Խ� row������ ����
	private static ArrayList<String[]> arrCellList;	//�о� �� result �����͸� ������ �ֱ� ���� �迭�� ����
	private static HashMap<String, Integer> headLineMap;		//�о� �� result ������ �� HeadLine �κ��� ����	
	
	
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
						//�ִ� 5������ ���ϴ� ��뷮 ó���� ����Ͽ� �ؽ�Ʈ �Է°� �̹��� �Է��� �и��Ͽ� ó�� 
						drawSheetText();		//�ؽ�Ʈ �Է�
						drawSheetImage();	//�̹��� �Է�
					
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
	 * ���ø� �ٿ�ޱ�
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
	 * ���� ���ø��� Text ������ �Է��ϴ� �Լ�
	 * @throws Exception 
	*/
	private static void drawSheetText() throws Exception {
		createExcelData();
		
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(excelTemplatePath)); // ���� ���ø� ����
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);	//ù��° ��Ʈ�� �����͸� sheet ������ ��´�
		
		String[] headCellName = registry.getStringArray("DMUReportOperation.HeadCell");
		String[] CellNumber = registry.getStringArray("DMUReportOperation.ValidCellNumber");
		
		if(headCellName == null || headCellName.length <= 0) {			
			throw new Exception("Header �Ӽ� Mapping ������ �ǹٸ��� �ʽ��ϴ�. �����ڿ��� �����ϼ���.");
		} else {			
			for(int i=0; i<arrCellList.size(); i++) {
				String[] rowContents = arrCellList.get(i);
				
				//Row ����
				int targetRowMap = headLineMap.get("Number");
				String targetRow = rowContents[targetRowMap];
				HSSFRow row = sheet.createRow(Integer.parseInt(targetRow));
				row.setHeight((short)(225*4));						
				
				//Cell ���� �Է�
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
	 * �ؽ�Ʈ ���Ϸ� ���� ���� ��� �����͸� ArrayList�� �����ϴ� �Լ�
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
				 * �о���� ���Ͽ� �ƽ�Ű�ڵ� 0X00�� NUL����Ʈ ������ �����Ͽ� �������� ������ �Ұ�.
				 * �̸� �����ϰ��� ����ȯ ���� int 0 �� (NUL����Ʈ)������ �����ϰ� �Ϲ� ���ڵ鸸
				 * �ٽ� StringŸ������ lineContents�� ����
				*/
				char chr[] = roadedText.toCharArray();
				String lineContents = "";
				
				for(int j=0; j<chr.length; j++) {
					if((int)chr[j] != (int)0)
					lineContents = lineContents + chr[j];
				}

				/*
				 * lineContents�� ��� ������ ������ 
				 * "���� �о�鿩 ������ ��� ����,
				 * ###�� �ִ� ������ ��Ÿ���� ������ �����Ѵ�.
				*/				
				if(lineContents != null) {				
					if(lineContents.matches(".*###")) {	//�� �پ� �о�鿩 ###�� �����ϸ� ���� ����
						break;
					} else if(lineContents.matches(".*\"")) {	// "�� ���� �� �ٸ� �д´�.						
						String tempLine = lineContents.substring(lineContents.indexOf("\"")+1);	// "���� ���� ������ ���� ��´�.
						String[] arrLineContents = tempLine.split("\",\"");
						//ù ���� headLine�̹Ƿ� ���� ����
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
			e.printStackTrace(); // ������ �ִٸ� �޽��� ���
		}	
	}
	
	
	/**
	 * HeadLine ���� ���� �Լ�
	*/
	private static void setHeader(String[] headLine) throws Exception {
		headLineMap = new HashMap<String, Integer>();
		
		for(int i=0; i<headLine.length; i++) {
			headLineMap.put(headLine[i], i);
		}
		
		//������ headLine�� �ùٸ��� ����
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
	 * 	10������ �������� �Ǻ� �ϴ� �Լ�
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
	 * ���� ���ø��� �̹��� ����
	*/
	private static void drawSheetImage() throws IOException {
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(getExcelFilePath())); // ������������
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);	//ù��° ��Ʈ�� �����͸� sheet ������ ��´�
		
		makeImageFileList(wb, sheet, getImagePath());		

		FileOutputStream fileOut = new FileOutputStream(getExcelFilePath()); // �̹��� ���Ե� ��������
		wb.write(fileOut);
		fileOut.close();				

		MessageBox.post("Success!", "Success", MessageBox.INFORMATION);
	}
	
	
	/**
	 * �̹��� ���� �����۾� ����
	*/
	public static void makeImageFileList(HSSFWorkbook wb, HSSFSheet sheet, String filePathLoop) throws IOException {
		lastRow = 0;
		
		File fileLoop = new File(filePathLoop);
		
		if (fileLoop.isDirectory()) { // ���丮�� ...
			File subLoop[] = fileLoop.listFiles(); // �ش� ���丮�� ���� ����� ����.
			
			for (int i = 0; i < subLoop.length; i++) { // ���� ��� ��ŭ �ݺ�
				if (subLoop[i].isFile()) { // sub[i] �� �����̸� ...				
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
	 * ���� �̹����� ������ �����ϴ� �Լ�
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
        anchor = new HSSFClientAnchor(dx1,dy1,dx2,dy2,col1,row1,col2,row2); // �̹��� ũ�������� ���⼭..
        anchor.setAnchorType( 1 );	//���� �̹��� �ڵ� ����
        
        patriarch.createPicture(anchor, loadPicture( imagePath, wb )); // ���� �� �̹���
        lastRow++;
    }

    
    /**
     * �̹��� �ε� �Լ�
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
