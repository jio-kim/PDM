package com.ssangyong.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssangyong.common.CustomTCTable;
import com.teamcenter.rac.common.TCTableModel;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class ExcelService {
	private static ExcelService service;
    private TCSession session;
	private static Hashtable<String, CellStyle> styles;
	private static HSSFWorkbook xlsworkbook;
	private static HSSFSheet xlssheet;
	
	private static HSSFRow xlsrow;
	private static HSSFCell xlscell;
	private static XSSFWorkbook xlsxworkbook;
	private static XSSFSheet xlsxsheet;
	private static XSSFRow xlsxrow;
	private static XSSFCell xlsxcell;

	public static void createService() {
		if (service == null) {
			service = new ExcelService();
		}
	}

	private ExcelService() {
		styles = new Hashtable();
	}

	public static void setDocumentTemplate(Hashtable<String, String> taskInfoTable, TCComponentDataset dataset) throws Exception {
		if(dataset == null) {
			return;
		}	else {
			File[] files = DatasetService.getFiles(dataset);
			printTemplate(taskInfoTable, files[0], null);
			DatasetService.datasetUpdate(files[0], dataset);
		}
	}

	public static void setECRTemplate(Hashtable<String, String> taskInfoTable, File file, TCComponentItem item) throws Exception {
		String problum = "";
		TCComponent[] problumItem = item.getRelatedComponents("EC_problem_item_rel");
		for (int i = 0; i < problumItem.length; i++) {
			problum += "[" + problumItem[i].getProperty("object_string") + "]";
			if (i != problumItem.length - 1) {
				problum += ", ";
			}
		}
		TCComponent user = item.getReferenceProperty("owning_user");
		taskInfoTable.put("owning_user", user.getProperty("user_name"));
		taskInfoTable.put("wiq2ChangeReason", PreferenceService.getDisplayValue(TCPreferenceService.TC_preference_site, "wiq2ChangeReason", item.getProperty("wiq2ChangeReason")));
		if(PreferenceService.getDisplayValue(TCPreferenceService.TC_preference_site, "wiq2YN", item.getProperty("wiq2ReviewResult")) == null) {
			taskInfoTable.put("wiq2ReviewResult", "");
		}	else {
			taskInfoTable.put("wiq2ReviewResult", PreferenceService.getDisplayValue(TCPreferenceService.TC_preference_site, "wiq2YN", item.getProperty("wiq2ReviewResult")));
		}
		taskInfoTable.put("wiq2ReviewResult", PreferenceService.getDisplayValue(TCPreferenceService.TC_preference_site, "wiq2YN", item.getProperty("wiq2ReviewResult")));
		taskInfoTable.put("problum_item", problum);
		printTemplate(taskInfoTable, file, item);
	}

	public static void setECNTemplate(Hashtable<String, String> taskInfoTable, File file, TCComponentItem item) throws Exception {
		TCComponent[] relatedECR = item.getRelatedComponents("EC_reference_item_rel");
		if (relatedECR.length == 0) {
			taskInfoTable.put("relatedECR", "");
		} else {
			taskInfoTable.put("relatedECR", relatedECR[0].getProperty("object_string"));
		}
		String problum = "";
		TCComponent[] problumItem = item.getRelatedComponents("EC_problem_item_rel");
		for (int i = 0; i < problumItem.length; i++) {
			problum += "[" + problumItem[i].getProperty("object_string") + "]";
			if (i != problumItem.length - 1) {
				problum += ", ";
			}
		}
		String solution = "";
		TCComponent[] solutionItem = item.getRelatedComponents("EC_solution_item_rel");
		for (int i = 0; i < solutionItem.length; i++) {
			solution += "[" + solutionItem[i].getProperty("object_string") + "]";
			if (i != solutionItem.length - 1) {
				solution += ", ";
			}
		}
		TCComponent user = item.getReferenceProperty("owning_user");
		taskInfoTable.put("problum_item", problum);
		taskInfoTable.put("solution_item", solution);
		taskInfoTable.put("owning_user", user.getProperty("user_name"));
		taskInfoTable.put("wiq2ChangeReason", PreferenceService.getDisplayValue(TCPreferenceService.TC_preference_site, "wiq2ChangeReason", item.getProperty("wiq2ChangeReason")));
		taskInfoTable.put("wiq2DevelopType", PreferenceService.getDisplayValue(TCPreferenceService.TC_preference_site, "wiq2DevelopType", item.getProperty("wiq2DevelopType")));
		taskInfoTable.put("wiq2Inventory", PreferenceService.getDisplayValue(TCPreferenceService.TC_preference_site, "wiq2Inventory", item.getProperty("wiq2Inventory")));
		taskInfoTable.put("wiq2PartClassification", PreferenceService.getDisplayValue(TCPreferenceService.TC_preference_site, "wiq2PartClassification", item.getProperty("wiq2PartClassification")));
		taskInfoTable.put("wiq2ChangeType", PreferenceService.getDisplayValue(TCPreferenceService.TC_preference_site, "wiq2ChangeType", item.getProperty("wiq2ChangeType")));
		printTemplate(taskInfoTable, file, item);
	}

	public static void printTemplate(Hashtable<String, String> proxyTable, File file, TCComponent component) throws Exception {
		HSSFWorkbook workbook = getHSSFWorkbook(file);
		HSSFSheet sheet = workbook.getSheetAt(0);
		for (int ii = 0; ii < sheet.getPhysicalNumberOfRows(); ii++) {
			HSSFRow row = sheet.getRow(ii);
			int count = row.getPhysicalNumberOfCells();
			for (int jj = 0; jj < count; jj++) {
				HSSFCell cell = row.getCell(jj);
				String string = getCellData(cell);
				if (string == null) {
					continue;
				}
				if (string.startsWith("${") && string.endsWith("}")) {
					String property = string.substring(2, string.length() - 1);
					if (proxyTable.containsKey(property)) {
						cell.setCellValue(proxyTable.get(property));
					} else if (component != null) {
						cell.setCellValue(component.getProperty(property));
					}
				}
			}
		}
		write(file, workbook);
	}

	public static Vector<String[]> importExcel(File file, String sheetName, int startRow) {
		try {
			if (getExtension(file).equals("xls")) {
				return importHSSFWorkbook(file, sheetName, startRow);
			} else if (getExtension(file).equals("xlsx")) {
				return importXSSFWorkbook(file, sheetName, startRow);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static Vector<String[]> importHSSFWorkbook(File file, String sheetName, int startRow) throws Exception {
		HSSFWorkbook workbook = getHSSFWorkbook(file);
		HSSFSheet sheet = workbook.getSheet(sheetName);
		Vector<String[]> vector = new Vector();
		int count = sheet.getPhysicalNumberOfRows();
		for (int i = startRow; i < count; i++) {
			HSSFRow row = sheet.getRow(i);
			String[] strings = getCellData(row);
			if (!isRowEmpty(strings)) {
				vector.addElement(strings);
			}
		}
		return vector;
	}

	public static Vector<String[]> importXSSFWorkbook(File file, String sheetName, int startRow) throws Exception {
		XSSFWorkbook workbook = getXSSFWorkbook(file);
		XSSFSheet sheet = workbook.getSheet(sheetName);
		Vector<String[]> vector = new Vector();
		int count = sheet.getPhysicalNumberOfRows();
		for (int i = startRow; i < count; i++) {
			XSSFRow row = sheet.getRow(i);
			String[] strings = getCellData(row);
			if (!isRowEmpty(strings)) {
				vector.addElement(strings);
			}
		}
		return vector;
	}

	public static HSSFWorkbook getHSSFWorkbook(File file) throws Exception {
		POIFSFileSystem fileSystem = new POIFSFileSystem(new FileInputStream(file));
		return new HSSFWorkbook(fileSystem);
	}

	public static XSSFWorkbook getXSSFWorkbook(File file) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
		return workbook;
	}

	public static String[] getCellData(HSSFRow row) {
		// int count = row.getPhysicalNumberOfCells();
		int count = row.getLastCellNum();
		String[] data = new String[count];
		for (int i = 0; i < count; i++) {
			HSSFCell cell = row.getCell(i);
			String value = null;
			if (cell == null) {
				data[i] = new String();
				continue;
			}
			if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
				data[i] = cell.getCellFormula().trim();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
				data[i] = String.valueOf(cell.getNumericCellValue()).trim();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
				data[i] = cell.getStringCellValue().trim();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
				data[i] = new String();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
				data[i] = String.valueOf(cell.getBooleanCellValue()).trim();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
				data[i] = String.valueOf(cell.getNumericCellValue()).trim();
			}
		}
		return data;
	}

	public static String getCellData(HSSFCell cell) {
		String value = null;
		if (cell == null) {
			return new String();
		}
		if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
			return cell.getCellFormula().trim();
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
			return String.valueOf(cell.getNumericCellValue()).trim();
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
			return cell.getStringCellValue().trim();
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
			return new String();
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue()).trim();
		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
			return String.valueOf(cell.getNumericCellValue()).trim();
		}
		return new String();
	}

	public static String[] getCellData(XSSFRow row) {
		int count = row.getPhysicalNumberOfCells();
		String[] data = new String[count];
		for (int i = 0; i < count; i++) {
			XSSFCell cell = row.getCell(i);
			String value = null;
			if (cell == null) {
				data[i] = new String();
				continue;
			}
			if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
				data[i] = cell.getCellFormula().trim();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
				final DecimalFormat df = new DecimalFormat("#"); 
				data[i] = df.format(cell.getNumericCellValue()).trim();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
				data[i] = cell.getStringCellValue().trim();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
				data[i] = new String();
			} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
				data[i] = String.valueOf(cell.getBooleanCellValue()).trim();
			}
		}
		return data;
	}

	public static void downloadTable(File file, JTable table, String[] columns, String[] columnsWidth) {
		String name = file.getName();
		int ii = name.indexOf(".");
		if(ii == -1){
			file = new File(file.getAbsolutePath() + ".xls");
		}
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("검색 결과");
		setStyles(workbook);
		for (int i = 0; i < columnsWidth.length; i++) {
			sheet.setColumnWidth(i, Integer.parseInt(columnsWidth[i]) * 50);
		}
		HSSFRow header = sheet.createRow(0);
		for (int i = 0; i < columns.length; i++) {
			HSSFCell cell = header.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(getStyle("header"));
		}
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			HSSFRow row = sheet.createRow(i + 1);
			for (int j = 0; j < model.getColumnCount(); j++) {
				HSSFCell cell = row.createCell(j);
				cell.setCellValue((String)model.getValueAt(i, j));
				cell.setCellStyle(getStyle("content"));
			}
		}
		write(file, workbook);
	}
	
	public static void downloadTable(File file, CustomTCTable table, String[] columns, String[] columnsWidth) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("검색 결과");
		setStyles(workbook);
		for (int i = 0; i < columnsWidth.length; i++) {
			sheet.setColumnWidth(i, Integer.parseInt(columnsWidth[i]) * 50);
		}
		HSSFRow header = sheet.createRow(0);
		for (int i = 0; i < columns.length; i++) {
			HSSFCell cell = header.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(getStyle("header"));
		}
		TCTableModel model = (TCTableModel)table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			HSSFRow row = sheet.createRow(i + 1);
			for (int j = 0; j < model.getColumnCount(); j++) {
				HSSFCell cell = row.createCell(j);
				cell.setCellValue((String)model.getValueAt(i, j));
				cell.setCellStyle(getStyle("content"));
			}
		}
		write(file, workbook);
	}
	
	public static void downloadTable(File file, CustomTCTable table, String[] columns) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("검색 결과");
		setStyles(workbook);
		for (int i = 0; i < columns.length; i++) {
			sheet.setColumnWidth(i, 6000);
		}
		HSSFRow header = sheet.createRow(0);
		for (int i = 0; i < columns.length; i++) {
			HSSFCell cell = header.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(getStyle("header"));
		}
		TCTableModel model = (TCTableModel)table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			HSSFRow row = sheet.createRow(i + 1);
			for (int j = 0; j < model.getColumnCount(); j++) {
				HSSFCell cell = row.createCell(j);
				cell.setCellValue((String)model.getValueAt(i, j));
				cell.setCellStyle(getStyle("content"));
			}
		}
		write(file, workbook);
	}
	
	public static void downloadTable(File file, JTable table, String[] columns) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("검색 결과");
		setStyles(workbook);
		for (int i = 0; i < columns.length; i++) {
			sheet.setColumnWidth(i, 6000);
		}
		HSSFRow header = sheet.createRow(0);
		for (int i = 0; i < columns.length; i++) {
			HSSFCell cell = header.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(getStyle("header"));
		}
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			HSSFRow row = sheet.createRow(i + 1);
			for (int j = 0; j < model.getColumnCount(); j++) {
				HSSFCell cell = row.createCell(j);
				cell.setCellValue((String)model.getValueAt(i, j));
				cell.setCellStyle(getStyle("content"));
			}
		}
		write(file, workbook);
	}
	
	public static void downloadTable(File file, JTable table, String[] columns, String str) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		setStyles(workbook);
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int rowCount = model.getRowCount();
		int dpCount = 50000;
		
		int sheetNo = (rowCount / dpCount) + 1;
		int rCount = 0; // 마지막 Row
		int sCount = 0; //시작 Row
		HSSFSheet sheet;
		
		for (int s = 0; s < sheetNo; s++){
			sheet = workbook.createSheet("검색 결과(" + (s+1) + ")");
			
			for (int i = 0; i < columns.length; i++) {
				sheet.setColumnWidth(i, 6000);
			}
			
			HSSFRow header = sheet.createRow(0);
			for (int i = 0; i < columns.length; i++) {
				HSSFCell cell = header.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(getStyle("header"));
			}
			
			if(sheetNo == s+1){
				if(sheetNo == 1){
					rCount = rowCount;
				} else {
					rCount = rowCount - (s*dpCount);
				}
			} else {
				rCount = dpCount;
			}
			
			for (int i = 0; i < rCount; i++) {
				HSSFRow row = sheet.createRow(i + 1);
				for (int j = 0; j < model.getColumnCount(); j++) {
					sCount = s * dpCount + i;
					HSSFCell cell = row.createCell(j);
					cell.setCellValue((String)model.getValueAt(sCount, j));
					cell.setCellStyle(getStyle("content"));
				}
			}
		}
		
		write(file, workbook);
	}

	public static void write(File file, HSSFWorkbook workbook) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
			workbook.write(fileOutputStream);
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void write(File file, XSSFWorkbook workbook) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
			workbook.write(fileOutputStream);
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static CellStyle getStyle(String name) {
		return styles.get(name);
	}

	public static void setStyles(Workbook workbook) {
		CellStyle style = createBorderedStyle(workbook);
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font headerFont = workbook.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(headerFont);
		styles.put("header", style);
		style = createBorderedStyle(workbook);
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font contentFont = workbook.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		style.setFont(contentFont);
		styles.put("content", style);
	}

	private static CellStyle createBorderedStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		return style;
	}

	public static boolean isRowEmpty(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			if (strings[i].length() > 0) {
				return false;
			}
		}
		return true;
	}

	private static String getFileName(File file) {
		if (file.isDirectory())
			return file.getName();
		else {
			String filename = file.getName();
			int i = filename.lastIndexOf(".");
			if (i > 0) {
				return filename.substring(0, i);
			}
		}
		return null;
	}

	private static String getExtension(File file) {
		if (file.isDirectory())
			return null;
		String filename = file.getName();
		int i = filename.lastIndexOf(".");
		if (i > 0 && i < filename.length() - 1) {
			return filename.substring(i + 1).toLowerCase();
		}
		return null;
	}
	
	/**
	 * xls, xlsx 인지 확인 후 임시 데이터셋의 결재 정보를 초기화 한다.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2012. 5. 4.
	 * @param file2
	 */
	public static void fileUserAssignRemove(File file, int startRow, int startCell, String key) {
		try {
			String str = getExtension(file);
			
			if(str.equals("xls")){
				xlsworkbook = ExcelService.getHSSFWorkbook(file);
				
				xlssheet = xlsworkbook.getSheetAt(0);
				
				xlsrow = xlssheet.getRow(startRow);
				xlscell = xlsrow.getCell(startCell);
				xlscell.setCellValue(key);
				
				ExcelService.write(file, xlsworkbook);
			}else if(str.equals("xlsx")){
				xlsxworkbook = ExcelService.getXSSFWorkbook(file);
				
				xlsxsheet = xlsxworkbook.getSheetAt(0);
				
				xlsxrow = xlsxsheet.getRow(startRow);
				xlsxcell = xlsxrow.getCell(startCell);
				xlsxcell.setCellValue(key);
				
				ExcelService.write(file, xlsxworkbook);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
