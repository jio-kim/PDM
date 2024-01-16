package com.ssangyong.commands.ospec;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;

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

import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.commands.ospec.panel.OSpecTable;
import com.ssangyong.common.ui.mergetable.MultiSpanCellTable;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.util.MessageBox;

/**
 * WorkSpace에서 선택된 옵션정보를 Excel로 Report함
 * 
 * @author baek
 * 
 */
public class OspecReportExportOperation extends AbstractAIFOperation {

	private OSpecTable ospecTable = null;
	private File exportFile = null;
	private FileInputStream fis = null;
	private JDialog parentDlg = null;
	private Map<String, HSSFCellStyle> cellStyles = null;
	private int headerLabelRowStart = 2;
	private int headerLabelRowCount = 0;
	private int dataLabelColumnCount = 4;
	private int DATA_ROW_START_IDX = 7;

	public OspecReportExportOperation(JDialog parentDlg, OSpecTable ospecTable, File exportFile) {
		this.parentDlg = parentDlg;
		this.ospecTable = ospecTable;
		this.exportFile = exportFile;
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
		// Category Group 병합정보
		HashMap<String, MergeInform> categoryGroupMap = new HashMap<String, MergeInform>();
		// Category Code 병합정보
		HashMap<String, MergeInform> categoryMap = new HashMap<String, MergeInform>();
		// option Value 병합정보
		HashMap<String, MergeInform> optionValueMap = new HashMap<String, MergeInform>();
		try {
			fis = new FileInputStream(exportFile);
			wb = new HSSFWorkbook(fis);
			Sheet sheet = wb.getSheetAt(0);
			cellStyles = createCellStyles(wb);

			fis.close();
			fis = null;

			// 마지막 Row Number
			int lastRowNum = sheet.getLastRowNum();
			/**
			 * 1. 가져온 Excel 에서 Header 를 제외하고 나머지는 지움
			 */
			for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
				CellRangeAddress region = sheet.getMergedRegion(i);
				Row firstRow = sheet.getRow(region.getFirstRow());
				if (firstRow == null)
					continue;
				int rowNum = firstRow.getRowNum();
				// Header 아래에 Merge 된 것들 Merge 를 해제함
				if (rowNum < DATA_ROW_START_IDX)
					continue;
				sheet.removeMergedRegion(i);
			}
			// Row Data 를 지움
			for (int i = DATA_ROW_START_IDX; i <= lastRowNum; i++) {
				removeRow(sheet, i);
			}
			// 왼쪽 Header Table
			MultiSpanCellTable fixedTable = ospecTable.getFixedOspecViewTable();
			// Option Table
			MultiSpanCellTable viewTable = ospecTable.getOspecViewTable();

			int rowCnt = DATA_ROW_START_IDX; // Data 기록할 시작 Row 위치

			/**
			 * 2. 화면 UI Table Row 정보를 읽으면서 한 Row 씩 Excel 에 기록함
			 */
			for (int i = 0; i < fixedTable.getRowCount(); i++) {
				Row row = sheet.createRow((short) rowCnt);
				String categoryGroup = (String) fixedTable.getValueAt(i, 0);
				// Category Group Name 을 맨 처음에 추가함
				if (!categoryGroupMap.containsKey(categoryGroup)) {
					Cell cell = row.createCell(0);
					cell.setCellValue(categoryGroup);
					cell.setCellStyle(cellStyles.get(getCellStyle(rowCnt, 0)));
					cell = row.createCell(1);
					cell.setCellValue(getCatGroupName(categoryGroup));
					cell.setCellStyle(cellStyles.get(getCellStyle(rowCnt, 1)));
					// Category Group Name Row 빈값 입력
					for (int j = 0; j < viewTable.getColumnCount(); j++) {
						cell = row.createCell(j + fixedTable.getColumnCount());
						cell.setCellStyle(cellStyles.get(getCellStyle(rowCnt, j + j + fixedTable.getColumnCount())));
					}
					categoryGroupMap.put(categoryGroup, new MergeInform(rowCnt, rowCnt + 1, 0, 0, 2));
					rowCnt++;

					row = sheet.createRow((short) rowCnt);
				} else {
					// Category Group Merge 정보 저장
					MergeInform mergeInfrom = categoryGroupMap.get(categoryGroup);
					mergeInfrom.setLastRow(rowCnt);
					mergeInfrom.setCount(mergeInfrom.getCount() + 1);
				}

				// Option Value Merge 정보 저장
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
					cell.setCellValue(j == 0 ? "" : (String) fixedTable.getValueAt(i, j));
					cell.setCellStyle(cellStyles.get(getCellStyle(rowCnt, j)));
				}
				// Option Data 입력
				for (int j = 0; j < viewTable.getColumnCount(); j++) {
					Cell cell = row.createCell(j + fixedTable.getColumnCount());
					cell.setCellValue((String) viewTable.getValueAt(i, j));
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

			// Open
			FileOutputStream fos = new FileOutputStream(exportFile.getAbsolutePath());

			wb.write(fos);
			fos.flush();
			fos.close();

			fos = null;
			wb = null;

			// Open
			Desktop.getDesktop().open(exportFile);
		} catch (Exception ex) {
			wb = null;
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException ex1) {
					ex1.printStackTrace();
				}
			}
			MessageBox.post(parentDlg, ex.getMessage(), "ERROR", MessageBox.ERROR);
			throw ex;
		}
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

			if (mergeType == MERGE_TYPE.CATEGORY_GROUP) {
				// Category Group Name 병합
				sheet.addMergedRegion(new CellRangeAddress(mergeInfrom.getFirstRow(), mergeInfrom.getFirstRow(), 1, 3));
			} else if (mergeType == MERGE_TYPE.OPTION_VALUE) {
				// Option Value Name 병합
				sheet.addMergedRegion(new CellRangeAddress(mergeInfrom.getFirstRow(), mergeInfrom.getLastRow(), mergeInfrom.getFirstCol() - 1, mergeInfrom
						.getLastCol() - 1));
			}

			if (cnt < 2)
				continue;
			sheet.addMergedRegion(new CellRangeAddress(mergeInfrom.getFirstRow(), mergeInfrom.getLastRow(), mergeInfrom.getFirstCol(), mergeInfrom.getLastCol()));
		}
	}

	/**
	 * Sheet의 Row를 지움
	 * 
	 * @param sheet
	 * @param rowIndex
	 */
	private void removeRow(Sheet sheet, int rowIndex) {
		Row removingRow = sheet.getRow(rowIndex);
		if (removingRow != null) {
			sheet.removeRow(removingRow);
		}
	}

	private Map<String, HSSFCellStyle> createCellStyles(Workbook workbook) {
		Map<String, HSSFCellStyle> styleMap = new HashMap<String, HSSFCellStyle>();

		HSSFCellStyle style;

		Font headerFont = workbook.createFont();
		headerFont.setFontHeightInPoints((short) 11);
		headerFont.setFontName("Tahoma");

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

		styleMap.put("headerStyle", style);

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
		} else {
			if (col < dataLabelColumnCount)
				styleName = "dataBGStyle";
			else
				styleName = "dataStyle";
		}
		return styleName;
	}

	/**
	 * Category Group Name from H-BOM
	 * 
	 * @param catgroup
	 * @return
	 */
	public String getCatGroupName(String catgroup) {
		String catgroupName = null;

		if (catgroup.equals("2")) {
			catgroupName = "M/YEAR";
		} else if (catgroup.equals("3")) {
			catgroupName = "SPECIAL COUNTRY";
		} else if (catgroup.equals("4")) {
			catgroupName = "CLIMATE";
		} else if (catgroup.equals("A")) {
			catgroupName = "VEHICLE MODEL";
		} else if (catgroup.equals("B")) {
			catgroupName = "DRIVE";
		} else if (catgroup.equals("C")) {
			catgroupName = "ENGINE";
		} else if (catgroup.equals("D")) {
			catgroupName = "FUEL SYSTEM";
		} else if (catgroup.equals("E")) {
			catgroupName = "DRIVE TRAIN";
		} else if (catgroup.equals("F")) {
			catgroupName = "STEERING";
		} else if (catgroup.equals("G")) {
			catgroupName = "SUSPENSION";
		} else if (catgroup.equals("H")) {
			catgroupName = "BREAK";
		} else if (catgroup.equals("J")) {
			catgroupName = "WHEEL & TIRE";
		} else if (catgroup.equals("K")) {
			catgroupName = "EXTERIOR";
		} else if (catgroup.equals("L")) {
			catgroupName = "GLAZING";
		} else if (catgroup.equals("M")) {
			catgroupName = "INTERIOR";
		} else if (catgroup.equals("N")) {
			catgroupName = "SEAT";
		} else if (catgroup.equals("Q")) {
			catgroupName = "RESTRAINT";
		} else if (catgroup.equals("R")) {
			catgroupName = "INSTRUMENT";
		} else if (catgroup.equals("S")) {
			catgroupName = "ELELTRICS";
		} else if (catgroup.equals("T")) {
			catgroupName = "HVAC";
		} else if (catgroup.equals("U")) {
			catgroupName = "AUDIO & VIDEO";
		} else if (catgroup.equals("X")) {
			catgroupName = "Production Interface";
		} else if (catgroup.equals("Z")) {
			catgroupName = "Miscellaneous";
		} else {
			catgroupName = "Not Found";
		}

		return catgroupName;
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
