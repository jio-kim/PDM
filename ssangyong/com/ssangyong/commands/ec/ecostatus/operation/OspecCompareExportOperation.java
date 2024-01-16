package com.ssangyong.commands.ec.ecostatus.operation;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.ssangyong.commands.ec.ecostatus.ui.EcoStatusOptionTable;
import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.common.ui.mergetable.MultiSpanCellTable;
import com.teamcenter.rac.aif.AbstractAIFOperation;

/**
 * OSPEC Compare 된 결과를 Excel 로 Export 함
 * 
 * @author baek
 * 
 */
public class OspecCompareExportOperation extends AbstractAIFOperation {

	private EcoStatusOptionTable beforeOspecTable = null;
	private EcoStatusOptionTable afterOspecTable = null;
	private File exportFile = null; // 출력되는 파일
	private Map<String, HSSFCellStyle> cellStyles = null;
	private int headerLabelRowStart = 2;
	private int headerLabelRowCount = 0;
	private int dataLabelColumnCount = 4;
	private int DATA_ROW_START_IDX = 7;
	private int DATA_COLUMN_START_IDX = 7;
	private HashMap<String, ArrayList<String>> afterDataMap = null; // 변경 후 Data 정보
	private HashMap<String, ArrayList<String>> beforeDataMap = null; // 변경 전 Data 정보

	private String crLf = Character.toString((char) 13) + Character.toString((char) 10);
	private ArrayList<Map<String, Object>> ospecVarianList;
	private ArrayList<String> areaList;
	private HashMap<String, List<String>> areaMap;
	private ArrayList<String> passList;
	private HashMap<String, List<String>> passMap;
	private ArrayList<String> engineList;
	private HashMap<String, List<String>> engineMap;
	private ArrayList<String> gradeList;
	private HashMap<String, List<String>> gradeMap;

	/**
	 * 
	 * @param beforeOspecTable
	 * @param afterOspecTable
	 * @param exportFile
	 * @param afterOspecFile
	 * @param beforeDataMap
	 * @param afterDataMap
	 */
	public OspecCompareExportOperation(EcoStatusOptionTable beforeOspecTable, EcoStatusOptionTable afterOspecTable, File exportFile,
			HashMap<String, ArrayList<String>> beforeDataMap, HashMap<String, ArrayList<String>> afterDataMap) {
		this.beforeOspecTable = beforeOspecTable;
		this.afterOspecTable = afterOspecTable;
		this.exportFile = exportFile;
		this.beforeDataMap = beforeDataMap;
		this.afterDataMap = afterDataMap;
	}

	// 병합 유형
	static enum MERGE_TYPE {
		CATEGORY_GROUP, CATEGORY, OPTION_VALUE
	};

	/**
	 * 0. 해당 OSPEC 원본 Excel 을 복제하여 가져옴 <BR>
	 * 1. 가져온 Excel 에서 Header 를 제외하고 나머지는 지움<BR>
	 * 2. 화면 UI Table Row 정보를 읽으면서 한 Row씩 Excel에 기록함 <BR>
	 * 3. 왼쪽 Header 에 Merge 될 항목들을 Merge 함
	 */
	@Override
	public void executeOperation() throws Exception {
		Workbook wb = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(exportFile.getAbsolutePath());
			wb = new HSSFWorkbook();
			Sheet beforeSheet = wb.createSheet("변경전");
			Sheet afterSheet = wb.createSheet();
			wb.setSheetName(1, "변경후");
			cellStyles = createCellStyles(wb);

			/**
			 * Header 정보 작성
			 */
			writeHeader(beforeSheet, beforeOspecTable);
			writeHeader(afterSheet, afterOspecTable);

			// 이전 정보 작성
			writeOptionContent(beforeSheet, beforeOspecTable, afterDataMap);
			// 변경후 작성
			writeOptionContent(afterSheet, afterOspecTable, beforeDataMap);

			wb.write(fos);
			fos.flush();
			fos.close();

			fos = null;
			wb = null;

			// Open
			Desktop.getDesktop().open(exportFile);
		} catch (Exception ex) {
			wb = null;
			if (fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (IOException ex1) {
					ex1.printStackTrace();
				}
			}
			throw ex;
		}
	}

	/**
	 * Sheet 내용을 작성함
	 * 
	 * @param sheet
	 * @param ospecTable
	 */
	private void writeOptionContent(Sheet sheet, EcoStatusOptionTable ospecTable, HashMap<String, ArrayList<String>> compareDataMap) {
		// Category Group 병합정보
		HashMap<String, MergeInform> categoryGroupMap = new HashMap<String, MergeInform>();
		// Category Code 병합정보
		HashMap<String, MergeInform> categoryMap = new HashMap<String, MergeInform>();
		// option Value 병합정보
		HashMap<String, MergeInform> optionValueMap = new HashMap<String, MergeInform>();
		// 마지막 Row Number
		// int lastRowNum = sheet.getLastRowNum();
		/**
		 * 1. 가져온 Excel 에서 Header 를 제외하고 나머지는 지움
		 */
		// for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
		// CellRangeAddress region = sheet.getMergedRegion(i);
		// Row firstRow = sheet.getRow(region.getFirstRow());
		// if (firstRow == null)
		// continue;
		// int rowNum = firstRow.getRowNum();
		// // Header 아래에 Merge 된 것들 Merge 를 해제함
		// if (rowNum < DATA_ROW_START_IDX)
		// continue;
		// sheet.removeMergedRegion(i);
		// }

		// Header 이외의 Row Data 를 지움
		// for (int i = DATA_ROW_START_IDX; i <= lastRowNum; i++) {
		// removeRow(sheet, i);
		// }

		// 왼쪽 Header Table
		MultiSpanCellTable fixedTable = ospecTable.getFixedOspecViewTable();
		// Option Table
		MultiSpanCellTable viewTable = ospecTable.getOspecViewTable();

		// Table 의 Column 수 이상인 것이 존재하면 해당 Column 들을 삭제함
		// lastRowNum = sheet.getLastRowNum();
		// int tableColumnCount = fixedTable.getColumnCount() + viewTable.getColumnCount();
		// Row firstRow = sheet.getRow(headerLabelRowStart);
		// int lastCellNum = firstRow.getLastCellNum();
		// if (lastCellNum > tableColumnCount) {
		// for (int i = tableColumnCount; i < lastCellNum; i++) {
		// for (int j = 0; j <= lastRowNum; j++) {
		// Row removingRow = sheet.getRow(j);
		// Cell cell = removingRow.getCell(i);
		// if (cell == null)
		// continue;
		// removingRow.removeCell(cell);
		// }
		// sheet.setColumnWidth(i, sheet.getColumnWidth(i + 1));
		// }
		// // 지운후 Column 사이즈 맞춤
		// for (int i = tableColumnCount; i < lastCellNum; i++) {
		// sheet.setColumnWidth(i, sheet.getColumnWidth(lastCellNum + 1));
		// }
		// }

		int rowCnt = DATA_ROW_START_IDX; // Data 기록할 시작 Row 위치

		/**
		 * 2. 화면 UI Table Row 정보를 읽으면서 한 Row 씩 Excel 에 기록함
		 */
		for (int i = 0; i < fixedTable.getRowCount(); i++) {
			Row row = sheet.createRow((short) rowCnt);
			String categoryGroup = (String) fixedTable.getValueAt(i, 0);

			// Category Group Name Merge 정보 저장
			if (!categoryGroupMap.containsKey(categoryGroup)) {
				categoryGroupMap.put(categoryGroup, new MergeInform(rowCnt, rowCnt, 0, 0, 1));
			} else {
				// Category Group Merge 정보 저장
				MergeInform mergeInfrom = categoryGroupMap.get(categoryGroup);
				mergeInfrom.setLastRow(rowCnt);
				mergeInfrom.setCount(mergeInfrom.getCount() + 1);
			}

			// Category Merge 정보 저장
			String optionValue = (String) fixedTable.getValueAt(i, 3);
			String category = OpUtil.getCategory(optionValue);
			if (!categoryMap.containsKey(category))
				categoryMap.put(category, new MergeInform(rowCnt, rowCnt, 1, 1, 1));
			else {
				MergeInform cateMergeInfrom = categoryMap.get(category);
				cateMergeInfrom.setLastRow(rowCnt);
				cateMergeInfrom.setCount(cateMergeInfrom.getCount() + 1);
			}

			// Option Value Name Merge 정보 저장
			if (!optionValueMap.containsKey(optionValue))
				optionValueMap.put(optionValue, new MergeInform(rowCnt, rowCnt, 3, 3, 1));
			else {
				MergeInform mergeInfrom = optionValueMap.get(optionValue);
				mergeInfrom.setLastRow(rowCnt);
				mergeInfrom.setCount(mergeInfrom.getCount() + 1);
			}

			// Left Header Data 입력
			for (int j = 0; j < fixedTable.getColumnCount(); j++) {
				Cell cell = row.createCell(j);
				cell.setCellValue((String) fixedTable.getValueAt(i, j));
				cell.setCellStyle(cellStyles.get(getCellStyle(rowCnt, j)));
			}
			// Option Data 입력
			for (int j = 0; j < viewTable.getColumnCount(); j++) {
				Cell cell = row.createCell(j + fixedTable.getColumnCount());
				cell.setCellValue((String) viewTable.getValueAt(i, j));

				String columnName = viewTable.getColumnName(j);
				if (columnName != null && compareDataMap != null) {
					ArrayList<String> list = compareDataMap.get(columnName);
					String opValue = (String) fixedTable.getValueAt(rowCnt - DATA_ROW_START_IDX, 3);
					String value = (String) viewTable.getValueAt(rowCnt - DATA_ROW_START_IDX, j);
					String compValue = opValue + "_" + value;
					if (list == null) {
						cell.setCellStyle(cellStyles.get("redColorDataStyle"));
					} else {
						if (!list.contains(compValue))
							cell.setCellStyle(cellStyles.get("redColorDataStyle"));
						else
							cell.setCellStyle(cellStyles.get(getCellStyle(rowCnt, j + fixedTable.getColumnCount())));
					}
				} else
					cell.setCellStyle(cellStyles.get(getCellStyle(rowCnt, j + fixedTable.getColumnCount())));

				if (j >= viewTable.getColumnCount() - 2)
					cell.getCellStyle().setAlignment(CellStyle.ALIGN_LEFT);

			}
			rowCnt++;
		}

		/**
		 * 3. 왼쪽 Header 에 Merge 될 항목들을 Merge 함
		 */
		// Category Group
		addMergedRegion(sheet, categoryGroupMap, MERGE_TYPE.CATEGORY_GROUP);
		// Category
		addMergedRegion(sheet, categoryMap, MERGE_TYPE.CATEGORY);
		// Option Value
		addMergedRegion(sheet, optionValueMap, MERGE_TYPE.OPTION_VALUE);

	}

	/**
	 * 왼쪽 Header Merge 를 함
	 * 
	 * @param sheet
	 * @param mergeInformMap
	 *            Merge 될 정보
	 * @param mergeType
	 *            MERGE 유형
	 */
	private void addMergedRegion(Sheet sheet, HashMap<String, MergeInform> mergeInformMap, MERGE_TYPE mergeType) {
		for (String key : mergeInformMap.keySet()) {
			MergeInform mergeInfrom = mergeInformMap.get(key);
			int cnt = mergeInfrom.getCount();

			if (cnt < 2)
				continue;
			if (mergeType == MERGE_TYPE.OPTION_VALUE) {
				// Option Value Name 병합
				sheet.addMergedRegion(new CellRangeAddress(mergeInfrom.getFirstRow(), mergeInfrom.getLastRow(), mergeInfrom.getFirstCol() - 1, mergeInfrom
						.getLastCol() - 1));
			}
			sheet.addMergedRegion(new CellRangeAddress(mergeInfrom.getFirstRow(), mergeInfrom.getLastRow(), mergeInfrom.getFirstCol(), mergeInfrom.getLastCol()));
		}
	}

	private Map<String, HSSFCellStyle> createCellStyles(Workbook workbook) {
		Map<String, HSSFCellStyle> styleMap = new HashMap<String, HSSFCellStyle>();

		HSSFCellStyle style;

		Font headerFont = workbook.createFont();
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setFontName("Tahoma");

		Font titleFont = workbook.createFont();
		titleFont.setFontHeightInPoints((short) 11);
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleFont.setFontName("Tahoma");

		Font dataFont = workbook.createFont();
		dataFont.setFontHeightInPoints((short) 8);
		dataFont.setFontName("Tahoma");

		// header style
		style = (HSSFCellStyle) workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor((short) 22);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(HSSFColor.BLACK.index);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(HSSFColor.BLACK.index);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(HSSFColor.BLACK.index);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(HSSFColor.BLACK.index);
		style.setFont(headerFont);
		style.setWrapText(true);

		styleMap.put("headerStyle", style);

		// title style
		style = (HSSFCellStyle) workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.WHITE.index);
		style.setFillPattern(CellStyle.ALIGN_LEFT);
		style.setFont(titleFont);
		style.setWrapText(false);
		styleMap.put("titleStyle", style);

		// data style
		style = (HSSFCellStyle) workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.WHITE.index);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setFont(dataFont);
		styleMap.put("dataStyle", style);

		// data + bg
		style = (HSSFCellStyle) workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor((short) 22);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(HSSFColor.BLACK.index);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setFont(dataFont);
		styleMap.put("dataBGStyle", style);

		// Red Color data style
		style = (HSSFCellStyle) workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.RED.index);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setFont(dataFont);
		styleMap.put("redColorDataStyle", style);

		return styleMap;
	}

	/**
	 * Get Cell Style
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	private String getCellStyle(int row, int col) {
		String styleName = "";

		if (row >= headerLabelRowStart && row < headerLabelRowCount) {
			styleName = "headerStyle";
		} else if (row < headerLabelRowStart) {
			styleName = "titleStyle";
		} else {
			if (col < dataLabelColumnCount)
				styleName = "dataBGStyle";
			else
				styleName = "dataStyle";
		}
		return styleName;
	}

	/**
	 * Header 정보 작성
	 * 
	 * @param sheet
	 * @param ospecTable
	 * @throws Exception
	 */
	private void writeHeader(Sheet sheet, EcoStatusOptionTable ospecTable) throws Exception {
		areaList = new ArrayList<String>();
		passList = new ArrayList<String>();
		engineList = new ArrayList<String>();
		gradeList = new ArrayList<String>();

		areaMap = new HashMap<String, List<String>>();
		passMap = new HashMap<String, List<String>>();
		engineMap = new HashMap<String, List<String>>();
		gradeMap = new HashMap<String, List<String>>();

		Map<String, String[]> ospecVariantMap = new HashMap<String, String[]>();
		HashMap<String, OpTrim> variantMap = ospecTable.getOspec().getTrims();
		Iterator<String> srcVarKeys = variantMap.keySet().iterator();
		while (srcVarKeys.hasNext()) {
			String variantID = srcVarKeys.next();
			OpTrim opTrim = variantMap.get(variantID);
			String[] arrTrim = (opTrim.getColOrder() + "_" + opTrim.toString()).split("_");
			if (ospecVariantMap.get(variantID) == null) {
				ospecVariantMap.put(variantID, arrTrim);
			}
		}

		sortByVariant(ospecVariantMap);

		for (int i = 0; i < ospecVarianList.size(); i++) {
			Map<String, Object> variantMapOuter = ospecVarianList.get(i);
			String[] arrTrim = (String[]) variantMapOuter.get("arryTrim");

			// Area
			if (!areaList.contains(arrTrim[1])) {
				areaList.add(arrTrim[1]);
			}
			if (areaMap.get(arrTrim[1]) == null) {
				areaMap.put(arrTrim[1], getVarList(arrTrim, "area"));
			}
			// Pass
			if (!passList.contains(arrTrim[1] + "_" + arrTrim[2])) {
				passList.add(arrTrim[1] + "_" + arrTrim[2]);
			}
			if (passMap.get(arrTrim[1] + "_" + arrTrim[2]) == null) {
				passMap.put(arrTrim[1] + "_" + arrTrim[2], getVarList(arrTrim, "pass"));
			}
			// Engine
			if (!engineList.contains(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3])) {
				engineList.add(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3]);
			}
			if (engineMap.get(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3]) == null) {
				engineMap.put(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3], getVarList(arrTrim, "engine"));
			}
			// Grade
			if (!gradeList.contains(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4])) {
				gradeList.add(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4]);
			}
			if (gradeMap.get(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4]) == null) {
				gradeMap.put(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4], getVarList(arrTrim, "grade"));
			}
		}

		List<List<String>> labelRowList = new ArrayList<List<String>>();
		List<String> columnList = null;
		OSpec ospec = ospecTable.getOspec();
		String ospecNo = ospecTable.getOspec().getOspecNo();
		// Row No. 1
		columnList = new ArrayList<String>();
		columnList.add("OSPEC_Version_Detail_(" + ospecNo + ")");
		labelRowList.add(columnList);

		// Row No. 2
		columnList = new ArrayList<String>();
		columnList.add("");
		labelRowList.add(columnList);

		// Row No. 3
		columnList = new ArrayList<String>();
		columnList.add(ospec.getProject() + " PRODUCTION ORDERING SPECIFICATION");
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("Drv" + crLf + "Type");
		columnList.add("All");

		for (int i = 0; i < areaList.size(); i++) {
			String area = areaList.get(i);
			List<String> variantList = areaMap.get(area);
			for (int j = 0; j < variantList.size(); j++) {
				columnList.add(area.split("_")[0]);
			}
		}

		columnList.add("Eff-IN");
		columnList.add("REMARK");
		labelRowList.add(columnList);

		// Row No. 4
		columnList = new ArrayList<String>();
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");

		for (int i = 0; i < passList.size(); i++) {
			String pass = passList.get(i);
			List<String> variantList = passMap.get(pass);
			for (int j = 0; j < variantList.size(); j++) {
				columnList.add(pass.split("_")[1]);
			}
		}

		columnList.add("");
		columnList.add("");
		labelRowList.add(columnList);

		// Row No. 5
		columnList = new ArrayList<String>();
		columnList.add("OSI-No:" + ospec.getOspecNo());
		columnList.add("");
		SimpleDateFormat sb = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat releasedDateSb = new SimpleDateFormat("yyyy-MM-dd");
		Date srcReleasdDate = sb.parse(ospec.getReleasedDate());

		columnList.add(releasedDateSb.format(srcReleasdDate) + " Released");
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");

		for (int i = 0; i < engineList.size(); i++) {
			String engine = engineList.get(i);
			List<String> variantList = engineMap.get(engine);
			for (int j = 0; j < variantList.size(); j++) {
				columnList.add(engine.split("_")[2]);
			}
		}

		columnList.add("");
		columnList.add("S: Standard" + crLf + "O*: Option" + crLf + "M*: Mandatory");
		labelRowList.add(columnList);

		// Row No. 6
		columnList = new ArrayList<String>();
		columnList.add("Category Option");
		columnList.add("");
		columnList.add("");
		columnList.add("Code");
		columnList.add("P/Opt");
		columnList.add("");
		columnList.add("");

		for (int i = 0; i < gradeList.size(); i++) {
			String grade = gradeList.get(i);
			List<String> variantList = gradeMap.get(grade);
			for (int j = 0; j < variantList.size(); j++) {
				columnList.add(grade.split("_")[3]);
			}
		}

		columnList.add("");
		columnList.add("");
		labelRowList.add(columnList);

		// Row No. 7
		columnList = new ArrayList<String>();
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");
		columnList.add("");

		for (int i = 0; i < gradeList.size(); i++) {
			String grade = gradeList.get(i);
			for (int j = 0; j < ospecVarianList.size(); j++) {
				Map<String, Object> vMap = ospecVarianList.get(j);
				String trim = (String) vMap.get("trim");
				String[] arrTrim = (String[]) vMap.get("arryTrim");
				if (grade.equals(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4]))
					columnList.add(trim);
			}
		}

		columnList.add("");
		columnList.add("");
		labelRowList.add(columnList);

		if (labelRowList != null) {
			this.headerLabelRowCount = labelRowList.size();
			for (int i = 0; i < labelRowList.size(); i++) {
				List<String> args = (List<String>) labelRowList.get(i);
				createColumnRow(sheet.getWorkbook(), sheet, args, i);
			}
		}

		// 3. 헤더 셀 병합
		setMergedRegion(sheet, getHeaderMargeInfoList());

	}

	/**
	 * Column, Row Information of Cell to Merge int firstRow, int lastRow, int firstCol, int lastCol
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<Map<Integer, Integer>> getHeaderMargeInfoList() throws Exception {
		int startColVariant = 0;
		Map<Integer, Integer> cell = null;
		List<Map<Integer, Integer>> mergeInfo = new ArrayList<Map<Integer, Integer>>();

		// PRODUCTION ORDERING SPECIFICATION
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 2);
		cell.put(1, 3);
		cell.put(2, 0);
		cell.put(3, 4);
		mergeInfo.add(cell);

		// Drv Type
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 2);
		cell.put(1, 6);
		cell.put(2, 5);
		cell.put(3, 5);
		mergeInfo.add(cell);

		// All
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 2);
		cell.put(1, 6);
		cell.put(2, 6);
		cell.put(3, 6);
		mergeInfo.add(cell);

		// OSI-No:
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 4);
		cell.put(1, 4);
		cell.put(2, 0);
		cell.put(3, 1);
		mergeInfo.add(cell);

		// Released
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 4);
		cell.put(1, 4);
		cell.put(2, 2);
		cell.put(3, 4);
		mergeInfo.add(cell);

		// Category Option
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 5);
		cell.put(1, 6);
		cell.put(2, 0);
		cell.put(3, 2);
		mergeInfo.add(cell);

		// Code
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 5);
		cell.put(1, 6);
		cell.put(2, 3);
		cell.put(3, 3);
		mergeInfo.add(cell);

		// P/Opt
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 5);
		cell.put(1, 6);
		cell.put(2, 4);
		cell.put(3, 4);
		mergeInfo.add(cell);

		// Area
		startColVariant = 7;
		for (int i = 0; i < areaList.size(); i++) {
			String area = areaList.get(i);
			List<String> variantList = areaMap.get(area);

			cell = new HashMap<Integer, Integer>();
			cell.put(0, 2);
			cell.put(1, 2);
			cell.put(2, startColVariant);
			cell.put(3, startColVariant + variantList.size() - 1);

			startColVariant = startColVariant + variantList.size();

			mergeInfo.add(cell);
		}

		// Passenger
		startColVariant = 7;
		for (int i = 0; i < passList.size(); i++) {
			String pass = passList.get(i);
			List<String> variantList = passMap.get(pass);

			cell = new HashMap<Integer, Integer>();
			cell.put(0, 3);
			cell.put(1, 3);
			cell.put(2, startColVariant);
			cell.put(3, startColVariant + variantList.size() - 1);

			startColVariant = startColVariant + variantList.size();

			mergeInfo.add(cell);
		}

		// Engine
		startColVariant = 7;
		for (int i = 0; i < engineList.size(); i++) {
			String engine = engineList.get(i);
			List<String> variantList = engineMap.get(engine);

			cell = new HashMap<Integer, Integer>();
			cell.put(0, 4);
			cell.put(1, 4);
			cell.put(2, startColVariant);
			cell.put(3, startColVariant + variantList.size() - 1);

			startColVariant = startColVariant + variantList.size();

			mergeInfo.add(cell);
		}

		// Grade
		startColVariant = 7;
		for (int i = 0; i < gradeList.size(); i++) {
			String grade = gradeList.get(i);
			List<String> variantList = gradeMap.get(grade);

			cell = new HashMap<Integer, Integer>();
			cell.put(0, 5);
			cell.put(1, 5);
			cell.put(2, startColVariant);
			cell.put(3, startColVariant + variantList.size() - 1);

			startColVariant = startColVariant + variantList.size();

			mergeInfo.add(cell);
		}

		// Eff-IN
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 2);
		cell.put(1, 6);
		cell.put(2, startColVariant);
		cell.put(3, startColVariant);
		mergeInfo.add(cell);

		// REMARK
		startColVariant = startColVariant + 1;
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 2);
		cell.put(1, 3);
		cell.put(2, startColVariant);
		cell.put(3, startColVariant);
		mergeInfo.add(cell);

		// S: Standard
		// O*: Option
		// M*: Mandatory
		cell = new HashMap<Integer, Integer>();
		cell.put(0, 4);
		cell.put(1, 6);
		cell.put(2, startColVariant);
		cell.put(3, startColVariant);
		mergeInfo.add(cell);

		return mergeInfo;
	}

	/**
	 * Variant 를 Sort Order 순으로 정렬한다.
	 * 
	 * @param mergedVariantMap
	 */
	private ArrayList<Map<String, Object>> sortByVariant(Map<String, String[]> mergedVariantMap) {
		Map<String, Object> sortOrderMap = null;
		ospecVarianList = new ArrayList<Map<String, Object>>();
		String[] keys = mergedVariantMap.keySet().toArray(new String[0]);
		for (int i = 0; i < keys.length; i++) {

			String[] arrTrim = mergedVariantMap.get(keys[i]);
			String sortOrder = String.format("%04d", Integer.parseInt(arrTrim[0])) + keys[i];

			sortOrderMap = new HashMap<String, Object>();
			sortOrderMap.put("order", getSortOrder(arrTrim[1], "area") + "_" + getSortOrder(arrTrim[2], "pass") + "_" + getSortOrder(arrTrim[3], "engine")
					+ "_" + getSortOrder(arrTrim[4], "grade") + "_" + sortOrder);
			sortOrderMap.put("trim", keys[i]);
			sortOrderMap.put("arryTrim", arrTrim);

			ospecVarianList.add(sortOrderMap);
		}

		MapComparator comp = new MapComparator("order");
		Collections.sort(ospecVarianList, comp);

		return ospecVarianList;
	}

	/**
	 * O/Spec Header 병합을 위한 각 항목별 Variant List 생성
	 * 
	 * @param value
	 * @param flag
	 * @return
	 */
	private List<String> getVarList(String[] _arrTrim, String flag) {
		List<String> varList = new ArrayList<String>();
		for (int i = 0; i < ospecVarianList.size(); i++) {

			Map<String, Object> variantMap = ospecVarianList.get(i);
			String variantId = (String) variantMap.get("trim");
			String[] arrTrim = (String[]) variantMap.get("arryTrim");

			if (flag == "area") {
				if (_arrTrim[1].equals(arrTrim[1])) {
					varList.add(variantId);
				}
			} else if (flag == "pass") {
				if (_arrTrim[1].equals(arrTrim[1]) && _arrTrim[2].equals(arrTrim[2])) {
					varList.add(variantId);
				}

			} else if (flag == "engine") {
				if (_arrTrim[1].equals(arrTrim[1]) && _arrTrim[2].equals(arrTrim[2]) && _arrTrim[3].equals(arrTrim[3])) {
					varList.add(variantId);
				}
			} else if (flag == "grade") {
				if (_arrTrim[1].equals(arrTrim[1]) && _arrTrim[2].equals(arrTrim[2]) && _arrTrim[3].equals(arrTrim[3]) && _arrTrim[4].equals(arrTrim[4])) {
					varList.add(variantId);
				}
			}
		}

		return varList;
	}

	/**
	 * 컬럼 생성
	 */
	private void createColumnRow(Workbook workbook, Sheet sheet, List<String> args, int rowNum) {
		Row row = sheet.createRow((short) rowNum);
		Cell cell;

		row.setHeightInPoints(getRowHeight(rowNum, 0, sheet));

		for (int i = 0; i < args.size(); i++) {
			cell = row.createCell(i);

			String cellData = (String) args.get(i);
			if (cellData != null) {
				cell.setCellValue(cellData);
				cell.setCellStyle(this.cellStyles.get(getCellStyle(rowNum, i)));
				sheet.setColumnWidth(cell.getColumnIndex(), getColumnWidth(rowNum, cell.getColumnIndex(), sheet));
				cell.setCellType(Cell.CELL_TYPE_STRING);
			}
		}
	}

	/**
	 * Get Row Height
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	private float getRowHeight(int row, int col, Sheet sheet) {
		float height = 0.0f;

		if (row >= headerLabelRowStart && row < headerLabelRowCount) {
			height = 24f;
		} else if (row < headerLabelRowStart) {
			// height = sheet.getDefaultRowHeightInPoints();
			height = 16.5f;
		} else {
			height = 16.5f;
			// height = sheet.getRow(row).getHeight() * 5;
		}

		return height;
	}

	/**
	 * Get Row Height
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	private int getColumnWidth(int row, int col, Sheet sheet) {
		int width = 0;
		int colindex = 0;

		switch (col) {
		case 0:
		case 3:
		case 4:
		case 5:
		case 6:
			width = 5 * 256;
			break;
		case 1:
		case 2:
			width = 12 * 256;
			break;
		default:
			width = 8 * 256;
			break;
		}

		colindex = DATA_COLUMN_START_IDX + ospecVarianList.size();
		// Eff-IN
		if (col == colindex) {
			width = 8 * 256;
		}

		// REMARK
		if (col == colindex + 1) {
			width = 50 * 256;
		}

		return width;
	}

	/**
	 * Set Cell Merge
	 * 
	 * @param sheet
	 * @param args
	 */
	private void setMergedRegion(Sheet sheet, List<Map<Integer, Integer>> args) {
		for (int i = 0; i < args.size(); i++) {
			Map<Integer, Integer> cell = args.get(i);
			sheet.addMergedRegion(new CellRangeAddress(cell.get(0), cell.get(1), cell.get(2), cell.get(3)));
		}
	}

	class MapComparator implements Comparator<Map<String, Object>> {
		private final String key;

		public MapComparator(String key) {
			this.key = key;
		}

		@Override
		public int compare(Map<String, Object> first, Map<String, Object> second) {
			int result = ((String) first.get(key)).compareTo((String) second.get(key));
			return result;
		}
	}

	/**
	 * Variant Header Sort 순서
	 * 
	 * @param value
	 * @param flag
	 * @return
	 */
	public String getSortOrder(String value, String flag) {
		if (flag == "area") {
			return value;
		} else if (flag == "pass") {
			value = value.replaceAll("PASS", "").trim();
			value = String.format("%04d", Integer.parseInt(value)) + " PASS";
			return value;

		} else if (flag == "engine") {
			return value;
		} else if (flag == "grade") {
			if (value == "STD") {
				return "001";
			} else if (value == "DLX") {
				return "002";
			} else if (value == "H/DLX") {
				return "003";
			} else {
				return "999";
			}
		} else {
			return value;
		}
	}

	/**
	 * 병합 정보
	 * 
	 * @author baek
	 * 
	 */
	public class MergeInform {
		private int firstRow = 0;
		private int lastRow = 0;
		private int firstCol = 0;
		private int lastCol = 0;
		private int count = 0;

		public MergeInform(int firstRow, int lastRow, int firstCol, int lastCol, int count) {
			this.firstRow = firstRow;
			this.lastRow = lastRow;
			this.firstCol = firstCol;
			this.lastCol = lastCol;
			this.count = count;
		}

		/**
		 * @return the firstRow
		 */
		public int getFirstRow() {
			return firstRow;
		}

		/**
		 * @param firstRow
		 *            the firstRow to set
		 */
		public void setFirstRow(int firstRow) {
			this.firstRow = firstRow;
		}

		/**
		 * @return the lastRow
		 */
		public int getLastRow() {
			return lastRow;
		}

		/**
		 * @param lastRow
		 *            the lastRow to set
		 */
		public void setLastRow(int lastRow) {
			this.lastRow = lastRow;
		}

		/**
		 * @return the firstCol
		 */
		public int getFirstCol() {
			return firstCol;
		}

		/**
		 * @param firstCol
		 *            the firstCol to set
		 */
		public void setFirstCol(int firstCol) {
			this.firstCol = firstCol;
		}

		/**
		 * @return the lastCol
		 */
		public int getLastCol() {
			return lastCol;
		}

		/**
		 * @param lastCol
		 *            the lastCol to set
		 */
		public void setLastCol(int lastCol) {
			this.lastCol = lastCol;
		}

		/**
		 * @return the count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @param count
		 *            the count to set
		 */
		public void setCount(int count) {
			this.count = count;
		}

	}

}