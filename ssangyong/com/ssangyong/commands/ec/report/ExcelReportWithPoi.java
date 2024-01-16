package com.ssangyong.commands.ec.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * @author 고강민
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class ExcelReportWithPoi {
	protected HSSFWorkbook wb;
	protected HSSFSheet sheet[];
	protected HSSFRow row;
	protected HSSFCell cell;
	protected HSSFCellStyle defaultStyle;
	protected HSSFFont defaultFont;
	protected HSSFPrintSetup ps;
    protected Hashtable styleHash;
	protected Hashtable fontHash;
	protected int sheetCount;

	public ExcelReportWithPoi() {
		super();
	}

	public ExcelReportWithPoi(String[] iSheetNames) {
		wb = new HSSFWorkbook();
		styleHash = new Hashtable();
		fontHash = new Hashtable();

		sheetCount = iSheetNames.length;

		sheet = new HSSFSheet[sheetCount];

		for (int i = 0; i < sheetCount; ++i) {
			sheet[i] = wb.createSheet();
			wb.setSheetName(i, iSheetNames[i]);

			sheet[i].setZoom(8, 10);

			ps = sheet[i].getPrintSetup();

			// 출력 용지를 A4 사이즈로 ..
			ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);

			// Excel Shhet에는 Inch로 표현 되므로 setting * 2.54 = Excel Value
			ps.setHeaderMargin((double) 0.51);
			ps.setFooterMargin((double) 0.236);

			// 용지 설정 가로로 ...
			ps.setLandscape(true);
			ps.setFitHeight((short) 1);
			ps.setFitWidth((short) 1);
		}

		defaultStyle = wb.createCellStyle();

		defaultStyle.setFillForegroundColor(HSSFColor.WHITE.index);
		defaultStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		defaultStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);

		defaultFont = wb.createFont();
		defaultFont.setFontName("Book Antiqua");
		defaultFont.setFontHeightInPoints((short) 10);
		defaultFont.setColor(HSSFColor.BLACK.index);

		defaultStyle.setFont(defaultFont);

		styleHash.put("default", defaultStyle);
		fontHash.put("default", defaultFont);
	}

	public ExcelReportWithPoi(String[] iSheetNames, boolean landscapeType) {
		wb = new HSSFWorkbook();
		styleHash = new Hashtable();
		fontHash = new Hashtable();

		sheetCount = iSheetNames.length;

		sheet = new HSSFSheet[sheetCount];

		for (int i = 0; i < sheetCount; ++i) {
			sheet[i] = wb.createSheet();
			wb.setSheetName(i, iSheetNames[i]);

			sheet[i].setZoom(8, 10);

			ps = sheet[i].getPrintSetup();

			// 출력 용지를 A4 사이즈로 ..
			ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);

			// Excel Shhet에는 Inch로 표현 되므로 setting * 2.54 = Excel Value
			ps.setHeaderMargin((double) 0.51);
			ps.setFooterMargin((double) 0.236);

			// 용지 설정 가로로 ...
			ps.setLandscape(landscapeType);
			ps.setFitHeight((short) 1);
			ps.setFitWidth((short) 1);
		}

		defaultStyle = wb.createCellStyle();

		defaultStyle.setFillForegroundColor(HSSFColor.WHITE.index);
		defaultStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		defaultStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);

		defaultFont = wb.createFont();
		defaultFont.setFontName("Book Antiqua");
		defaultFont.setFontHeightInPoints((short) 10);
		defaultFont.setColor(HSSFColor.BLACK.index);

		defaultStyle.setFont(defaultFont);

		styleHash.put("default", defaultStyle);
		fontHash.put("default", defaultFont);
	}

	// 김욱 추가
	public void addSheet(String sheetName) {
		HSSFSheet tempSheet[] = sheet;

		sheet = new HSSFSheet[++sheetCount];

		for (int i = 0; i < tempSheet.length; i++) {
			sheet[i] = tempSheet[i];
		}

		HSSFSheet st = wb.createSheet();

		sheet[sheetCount - 1] = st;

		wb.setSheetName(sheetCount - 1, sheetName);

		st.setZoom(9, 10);

		ps = st.getPrintSetup();

		// 출력 용지를 A4 사이즈로 ..
		ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);

		// Excel Shhet에는 Inch로 표현 되므로 setting * 2.54 = Excel Value
		ps.setHeaderMargin((double) 0.51);
		ps.setFooterMargin((double) 0.236);

		// 용지 설정 가로로 ...
		ps.setLandscape(true);
	}

	public void setSheetName(int iSheetNum, String sheetName) {

		wb.setSheetName(iSheetNum, sheetName);

	}

	public String getPages(int isheetNum) {
		HSSFFooter footer = sheet[isheetNum].getFooter();

		return (HSSFFooter.page() + "/" + HSSFFooter.numPages());
	}

	public void setFooter(int isheetNum, String leftString, String centerString, String rightString) {
		HSSFFooter footer = sheet[isheetNum].getFooter();

		footer.setLeft(leftString);
		footer.setCenter(centerString);
		footer.setRight(rightString);
	}

	public void distroy() {
		styleHash.clear();
		styleHash = null;
		fontHash.clear();
		fontHash = null;
		ps = null;
		defaultFont = null;
		defaultStyle = null;
		cell = null;
		row = null;
		sheet = null;
		wb = null;

		Runtime.getRuntime().gc();
	}

	/*
	 *  Paper Setting
	 */

	public void setPrintColumns(int iSheetNum, int startCol, int endCol, int startRow, int endRow) {
		wb.setPrintArea(iSheetNum, startCol, endCol, startRow, endRow);
	}

	public void setPaperSize(int iSheetNum, short size) {
		sheet[iSheetNum].getPrintSetup().setPaperSize(size);
	}

	public void setPaperMargin(int iSheetNum, double headerMargin, double footerMargin) {
		sheet[iSheetNum].getPrintSetup().setHeaderMargin(headerMargin);
		sheet[iSheetNum].getPrintSetup().setFooterMargin(footerMargin);
	}

	public void setLandscape(int iSheetNum, boolean is) {
		sheet[iSheetNum].getPrintSetup().setLandscape(is);
	}

	public void setScale(int iSheetNum, int scale) {
		sheet[iSheetNum].getPrintSetup().setScale((short) scale);
	}

	public void setHeader(int isheetNum, String headerString) {
		HSSFHeader header = sheet[isheetNum].getHeader();

		header.setCenter(headerString);
	}

	public void setFooter(int isheetNum, String footerString) {
		HSSFFooter footer = sheet[isheetNum].getFooter();

		footer.setCenter(footerString);
		footer.setRight(HSSFFooter.page() + " sht / " + HSSFFooter.numPages() + " shts ");
	}

	public void setFooter(int isheetNum, String footerString, short fontSize) {
		HSSFFooter footer = sheet[isheetNum].getFooter();

		HSSFFooter.fontSize(fontSize);
		footer.setCenter(footerString);
		footer.setRight(HSSFFooter.page() + " sht / " + HSSFFooter.numPages() + " shts ");
	}

	public void setFooter(int isheetNum) {
		HSSFFooter footer = sheet[isheetNum].getFooter();

		footer.setRight(HSSFFooter.page() + " / " + HSSFFooter.numPages());
	}

	public void setRepeatingRows(int sheetIndex, int startCol, int endCol, int startRow, int endRow) {
//	    this.wb.setRepeatingRowsAndColumns(sheet, startCol, endCol, startRow, endRow);
	    CellRangeAddress rows = null;
	    CellRangeAddress cols = null;

	    if (startRow != -1) {
	      rows = new CellRangeAddress(startRow, endRow, -1, -1);
	    }
	    if (startCol != -1) {
	      cols = new CellRangeAddress(-1, -1, startCol, endCol);
	    }

	    sheet[sheetIndex].setRepeatingRows(rows);
	    sheet[sheetIndex].setRepeatingColumns(cols);	
	}

	public void setLeftMargin(int isheetNum, double size) {
		sheet[isheetNum].setMargin(HSSFSheet.LeftMargin, size);
	}

	public void setRightMargin(int isheetNum, double size) {
		sheet[isheetNum].setMargin(HSSFSheet.RightMargin, size);
	}

	public void setTopMargin(int isheetNum, double size) {
		sheet[isheetNum].setMargin(HSSFSheet.TopMargin, size);
	}

	public void setBottomMargin(int isheetNum, double size) {
		sheet[isheetNum].setMargin(HSSFSheet.BottomMargin, size);
	}

	public void setFitTopage(int isheetNum, boolean is) {
		sheet[isheetNum].setFitToPage(is);
	}

	/*
	 *  Row, Column Style Setting
	 */

	public HSSFRow getRow(int iSheetNum, int iRow) {
		return sheet[iSheetNum].getRow(iRow);
	}

	public void setRowSize(int isheetNum, int iStartRow, int rowsize) {
		row = sheet[isheetNum].getRow(iStartRow);

		if (row != null)
			row.setHeight((short) rowsize);
	}

	public void setRowSizeByPixel(int isheetNum, int iStartRow, int rowsize) {
		row = sheet[isheetNum].getRow(iStartRow);

		if (row != null) {
			rowsize *= 15;
			row.setHeight((short) rowsize);
		}
	}

	public void setRowSize(int isheetNum, int iStartRow, int iEndRow, int rowsize) {
		for (int i = iStartRow; i <= iEndRow; ++i) {
			row = sheet[isheetNum].getRow(i);

			if (row != null)
				row.setHeight((short) rowsize);
		}
	}

	public void setRowSizeByPixel(int isheetNum, int iStartRow, int iEndRow, int rowsize) {
		for (int i = iStartRow; i <= iEndRow; ++i) {
			row = sheet[isheetNum].getRow(i);

			if (row != null)
				row.setHeight((short) (rowsize * 15));
		}
	}
	public void SetColWidthSetting(int isheetNum, int iCol, short columnWidth) {
		sheet[isheetNum].setColumnWidth(iCol, columnWidth);
	}

	public void SetColWidthSettingByPixel(int isheetNum, int iCol, int pixcel) {
		int columnWidth = (int) (pixcel * 36.5);
		sheet[isheetNum].setColumnWidth(iCol, (short) columnWidth);
	}

	public void createFreezePane(int isheetNum, int iCol, int iRow) {
		sheet[isheetNum].createFreezePane(iCol, iRow);
	}

	public int getPhysicalNumberOfRows(int isheetNum) {
		return sheet[isheetNum].getPhysicalNumberOfRows();
	}

	/*
	 *  Cell Style Setting
	 */

	public void createStyleWithFont(String styleName, HSSFFont font) {
		HSSFCellStyle style = wb.createCellStyle();

		style.setFillForegroundColor(HSSFColor.WHITE.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setFont(font);

		styleHash.put(styleName, style);
	}

	public void createStyleWithFont(String styleName, String fontName) {
		HSSFCellStyle style = wb.createCellStyle();

		style.setFillForegroundColor(HSSFColor.WHITE.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

		HSSFFont font = (HSSFFont) fontHash.get(fontName);

		if (font != null) {
			style.setFont(font);
		}

		styleHash.put(styleName, style);
	}

	public void createStyle(String styleName) {
		HSSFCellStyle style = wb.createCellStyle();

		style.setFillForegroundColor(HSSFColor.WHITE.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

		styleHash.put(styleName, style);
	}

	public HSSFCellStyle getStyle(String styleName) {
		return (HSSFCellStyle) styleHash.get(styleName);
	}

	public void createFont(String fontName) {
		HSSFFont font = wb.createFont();

		font.setFontName("Book Antiqua");
		font.setFontHeightInPoints((short) 10);
		font.setColor(HSSFColor.BLACK.index);

		fontHash.put(fontName, font);
	}

	public HSSFFont getFont(String fontName) {
		return (HSSFFont) fontHash.get(fontName);
	}

	public void applyFont(String iStyleName, String iFontName) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		if (style != null) {
			HSSFFont font = (HSSFFont) fontHash.get(iFontName);

			if (font != null) {
				style.setFont(font);
			}
		}
	}

	public void applyStyle(String iStyleName, int iSheetNum, int iRow, int iCol) {
		HSSFCell workCell = sheet[iSheetNum].getRow(iRow).getCell(iCol);
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		workCell.setCellStyle(style);
	}

	public void setBorderStyle(String iStyleName, boolean top, boolean bottom, boolean left, boolean right) {
		this.setTopBorderStyle(iStyleName, top);
		this.setBottomBorderStyle(iStyleName, bottom);
		this.setLeftBorderStyle(iStyleName, left);
		this.setRightBorderStyle(iStyleName, right);
	}

	public void setBorderStyle(String iStyleName, int top, int bottom, int left, int right) {
		this.setTopBorderStyle(iStyleName, top);
		this.setBottomBorderStyle(iStyleName, bottom);
		this.setLeftBorderStyle(iStyleName, left);
		this.setRightBorderStyle(iStyleName, right);
	}

	public void setBorderStyle(String iStyleName, short top, short bottom, short left, short right) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setBorderStyle(style, top, bottom, left, right);
	}

	public void setBorderStyle(HSSFCellStyle style, boolean top, boolean bottom, boolean left, boolean right) {
		this.setTopBorderStyle(style, top);
		this.setBottomBorderStyle(style, bottom);
		this.setLeftBorderStyle(style, left);
		this.setRightBorderStyle(style, right);
	}

	public void setBorderStyle(HSSFCellStyle style, int top, int bottom, int left, int right) {
		this.setTopBorderStyle(style, top);
		this.setBottomBorderStyle(style, bottom);
		this.setLeftBorderStyle(style, left);
		this.setRightBorderStyle(style, right);
	}

	public void setBorderStyle(HSSFCellStyle style, short top, short bottom, short left, short right) {
		if (style != null) {
			style.setBorderTop(top);
			style.setBorderBottom(bottom);
			style.setBorderLeft(left);
			style.setBorderRight(right);
		}
	}

	public void setTopBorderStyle(String iStyleName, boolean is) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setTopBorderStyle(style, is);
	}

	public void setTopBorderStyle(HSSFCellStyle style, boolean is) {
		if (style != null) {
			if (is) {
				style.setBorderTop(HSSFCellStyle.BORDER_THIN);
				style.setTopBorderColor(HSSFColor.BLACK.index);
			} else {
				style.setBorderTop(HSSFCellStyle.BORDER_NONE);
			}
		}
	}

	public void setTopBorderStyle(String iStyleName, int thickness) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setTopBorderStyle(style, thickness);
	}

	public void setTopBorderStyle(HSSFCellStyle style, int thickness) {
		if (style != null) {

			switch (thickness) {
				case 0 :
					style.setBorderTop(HSSFCellStyle.BORDER_NONE);

					break;

				case 1 :
					style.setBorderTop(HSSFCellStyle.BORDER_THIN);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 2 :
					style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 3 :
					style.setBorderTop(HSSFCellStyle.BORDER_DOUBLE);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 4 :
					style.setBorderTop(HSSFCellStyle.BORDER_DASH_DOT);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 5 :
					style.setBorderTop(HSSFCellStyle.BORDER_DASH_DOT_DOT);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 6 :
					style.setBorderTop(HSSFCellStyle.BORDER_DASHED);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 7 :
					style.setBorderTop(HSSFCellStyle.BORDER_DOTTED);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 8 :
					style.setBorderTop(HSSFCellStyle.BORDER_HAIR);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 9 :
					style.setBorderTop(HSSFCellStyle.BORDER_THICK);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 10 :
					style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM_DASH_DOT);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 11 :
					style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM_DASH_DOT_DOT);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 12 :
					style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM_DASHED);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				case 13 :
					style.setBorderTop(HSSFCellStyle.BORDER_SLANTED_DASH_DOT);
					style.setTopBorderColor(HSSFColor.BLACK.index);

					break;

				default :
					style.setBorderTop(HSSFCellStyle.BORDER_NONE);
			}
		}
	}

	public void setBottomBorderStyle(String iStyleName, boolean is) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setBottomBorderStyle(style, is);
	}

	public void setBottomBorderStyle(HSSFCellStyle style, boolean is) {
		if (style != null) {
			if (is) {
				style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				style.setBottomBorderColor(HSSFColor.BLACK.index);
			} else {
				style.setBorderBottom(HSSFCellStyle.BORDER_NONE);
			}
		}
	}

	public void setBottomBorderStyle(String iStyleName, int thickness) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setBottomBorderStyle(style, thickness);
	}

	public void setBottomBorderStyle(HSSFCellStyle style, int thickness) {
		if (style != null) {

			switch (thickness) {
				case 0 :
					style.setBorderBottom(HSSFCellStyle.BORDER_NONE);

					break;

				case 1 :
					style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 2 :
					style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 3 :
					style.setBorderBottom(HSSFCellStyle.BORDER_DOUBLE);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 4 :
					style.setBorderBottom(HSSFCellStyle.BORDER_DASH_DOT);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 5 :
					style.setBorderBottom(HSSFCellStyle.BORDER_DASH_DOT_DOT);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 6 :
					style.setBorderBottom(HSSFCellStyle.BORDER_DASHED);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 7 :
					style.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 8 :
					style.setBorderBottom(HSSFCellStyle.BORDER_HAIR);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 9 :
					style.setBorderBottom(HSSFCellStyle.BORDER_THICK);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 10 :
					style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM_DASH_DOT);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 11 :
					style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM_DASH_DOT_DOT);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 12 :
					style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM_DASHED);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				case 13 :
					style.setBorderBottom(HSSFCellStyle.BORDER_SLANTED_DASH_DOT);
					style.setBottomBorderColor(HSSFColor.BLACK.index);

					break;

				default :
					style.setBorderBottom(HSSFCellStyle.BORDER_NONE);
			}
		}
	}

	public void setLeftBorderStyle(String iStyleName, boolean is) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setLeftBorderStyle(style, is);
	}

	public void setLeftBorderStyle(HSSFCellStyle style, boolean is) {
		if (style != null) {
			if (is) {
				style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				style.setLeftBorderColor(HSSFColor.BLACK.index);
			} else {
				style.setBorderLeft(HSSFCellStyle.BORDER_NONE);
			}
		}
	}

	public void setLeftBorderStyle(String iStyleName, int thickness) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setLeftBorderStyle(style, thickness);
	}

	public void setLeftBorderStyle(HSSFCellStyle style, int thickness) {
		if (style != null) {

			switch (thickness) {
				case 0 :
					style.setBorderLeft(HSSFCellStyle.BORDER_NONE);

					break;

				case 1 :
					style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 2 :
					style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 3 :
					style.setBorderLeft(HSSFCellStyle.BORDER_DOUBLE);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 4 :
					style.setBorderLeft(HSSFCellStyle.BORDER_DASH_DOT);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 5 :
					style.setBorderLeft(HSSFCellStyle.BORDER_DASH_DOT_DOT);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 6 :
					style.setBorderLeft(HSSFCellStyle.BORDER_DASHED);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 7 :
					style.setBorderLeft(HSSFCellStyle.BORDER_DOTTED);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 8 :
					style.setBorderLeft(HSSFCellStyle.BORDER_HAIR);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 9 :
					style.setBorderLeft(HSSFCellStyle.BORDER_THICK);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 10 :
					style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM_DASH_DOT);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 11 :
					style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM_DASH_DOT_DOT);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 12 :
					style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM_DASHED);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				case 13 :
					style.setBorderLeft(HSSFCellStyle.BORDER_SLANTED_DASH_DOT);
					style.setLeftBorderColor(HSSFColor.BLACK.index);

					break;

				default :
					style.setBorderLeft(HSSFCellStyle.BORDER_NONE);
			}
		}
	}

	public void setRightBorderStyle(String iStyleName, boolean is) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setRightBorderStyle(style, is);
	}

	public void setRightBorderStyle(HSSFCellStyle style, boolean is) {
		if (style != null) {
			if (is) {
				style.setBorderRight(HSSFCellStyle.BORDER_THIN);
				style.setRightBorderColor(HSSFColor.BLACK.index);
			} else {
				style.setBorderRight(HSSFCellStyle.BORDER_NONE);
			}
		}
	}

	public void setRightBorderStyle(String iStyleName, int thickness) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setRightBorderStyle(style, thickness);
	}

	public void setRightBorderStyle(HSSFCellStyle style, int thickness) {
		if (style != null) {

			switch (thickness) {
				case 0 :
					style.setBorderRight(HSSFCellStyle.BORDER_NONE);

					break;

				case 1 :
					style.setBorderRight(HSSFCellStyle.BORDER_THIN);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 2 :
					style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 3 :
					style.setBorderRight(HSSFCellStyle.BORDER_DOUBLE);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 4 :
					style.setBorderRight(HSSFCellStyle.BORDER_DASH_DOT);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 5 :
					style.setBorderRight(HSSFCellStyle.BORDER_DASH_DOT_DOT);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 6 :
					style.setBorderRight(HSSFCellStyle.BORDER_DASHED);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 7 :
					style.setBorderRight(HSSFCellStyle.BORDER_DOTTED);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 8 :
					style.setBorderRight(HSSFCellStyle.BORDER_HAIR);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 9 :
					style.setBorderRight(HSSFCellStyle.BORDER_THICK);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 10 :
					style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM_DASH_DOT);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 11 :
					style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM_DASH_DOT_DOT);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 12 :
					style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM_DASHED);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				case 13 :
					style.setBorderRight(HSSFCellStyle.BORDER_SLANTED_DASH_DOT);
					style.setRightBorderColor(HSSFColor.BLACK.index);

					break;

				default :
					style.setBorderRight(HSSFCellStyle.BORDER_NONE);
			}
		}
	}

	public void setBorderColor(String iStyleName, short top, short bottom, short left, short right) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setBorderColor(style, top, bottom, left, right);
	}

	public void setBorderColor(HSSFCellStyle style, short top, short bottom, short left, short right) {
		if (style != null) {
			style.setTopBorderColor(top);
			style.setBottomBorderColor(bottom);
			style.setLeftBorderColor(left);
			style.setRightBorderColor(right);
		}
	}

	public void setTopBorderColor(String iStyleName, short color) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setTopBorderColor(style, color);
	}

	public void setTopBorderColor(HSSFCellStyle style, short color) {
		if (style != null) {
			style.setTopBorderColor(color);
		}
	}

	public void setBottomBorderColor(String iStyleName, short color) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setBottomBorderColor(style, color);
	}

	public void setBottomBorderColor(HSSFCellStyle style, short color) {
		if (style != null) {
			style.setBottomBorderColor(color);
		}
	}

	public void setLeftBorderColor(String iStyleName, short color) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setLeftBorderColor(style, color);
	}

	public void setLeftBorderColor(HSSFCellStyle style, short color) {
		if (style != null) {
			style.setLeftBorderColor(color);
		}
	}

	public void setRightBorderColor(String iStyleName, short color) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);
		this.setRightBorderColor(style, color);
	}

	public void setRightBorderColor(HSSFCellStyle style, short color) {
		if (style != null) {
			style.setRightBorderColor(color);
		}
	}

	public void setCellColor(String iStyleName, short cellColor) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		if (style != null) {
			style.setFillForegroundColor(cellColor);
		}
	}

	public void setCellColor(HSSFCellStyle style, short cellColor) {
		if (style != null) {
			style.setFillForegroundColor(cellColor);
		}
	}

	public void setDataFormat(String iStyleName, short cellFormat) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		if (style != null) {
			style.setDataFormat(cellFormat);
		}
	}

	public void setFontStyles(String iFontName, String fontName, short fontSize, short fontColor) {
		HSSFFont font = (HSSFFont) fontHash.get(iFontName);

		this.setFontStyles(font, fontName, fontSize, fontColor);
	}

	public void setFontStyles(HSSFFont font, String fontName, short fontSize, short fontColor) {
		if (font != null) {
			font.setFontName(fontName);
			font.setFontHeightInPoints(fontSize);
			font.setColor(fontColor);
		}
	}

	public void setFontStyles(String iFontName, short fontSize, short fontColor) {
		HSSFFont font = (HSSFFont) fontHash.get(iFontName);

		this.setFontStyles(font, fontSize, fontColor);
	}

	public void setFontStyles(HSSFFont font, short fontSize, short fontColor) {
		if (font != null) {
			font.setFontHeightInPoints(fontSize);
			font.setColor(fontColor);
		}
	}

	public void setFontName(String iFontName, String fontName) {
		HSSFFont font = (HSSFFont) fontHash.get(iFontName);

		if (font != null)
			font.setFontName(fontName);
	}

	public void setFontName(HSSFFont font, String fontName) {
		if (font != null)
			font.setFontName(fontName);
	}

	public void setFontSize(String iFontName, short fontSize) {
		HSSFFont font = (HSSFFont) fontHash.get(iFontName);

		if (font != null)
			font.setFontHeightInPoints(fontSize);
	}

	public void setFontSize(HSSFFont font, short fontSize) {
		if (font != null)
			font.setFontHeightInPoints(fontSize);
	}

	public void setFontColor(String iFontName, short fontColor) {
		HSSFFont font = (HSSFFont) fontHash.get(iFontName);

		if (font != null)
			font.setColor(fontColor);
	}

	public void setFontColor(HSSFFont font, short fontColor) {
		if (font != null)
			font.setColor(fontColor);
	}

	public void setBoldWeight(String iFontName, boolean is) {
		HSSFFont font = (HSSFFont) fontHash.get(iFontName);

		this.setBoldWeight(font, is);
	}

	public void setBoldWeight(HSSFFont font, boolean is) {
		if (font != null) {
			if (is) {
				font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			} else {
				font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			}
		}
	}

	public void setItalic(String iFontName, boolean is) {
		HSSFFont font = (HSSFFont) fontHash.get(iFontName);

		this.setItalic(font, is);
	}

	public void setItalic(HSSFFont font, boolean is) {
		if (font != null)
			font.setItalic(is);
	}

	public void setUnderLine(String iFontName, boolean is) {
		HSSFFont font = (HSSFFont) fontHash.get(iFontName);

		this.setUnderLine(font, is);
	}

	public void setUnderLine(HSSFFont font, boolean is) {
		if (is) {
			font.setUnderline(HSSFFont.U_SINGLE);
		} else {
			font.setUnderline(HSSFFont.U_NONE);
		}
	}

	public void setWrapText(String iStyleName, boolean is) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		this.setWrapText(style, is);
	}

	public void setWrapText(HSSFCellStyle style, boolean is) {
		if (style != null)
			style.setWrapText(is);
	}

	public void setRightAlignment(String iStyleName) {
		this.setAlignment(iStyleName, HSSFCellStyle.ALIGN_RIGHT);
	}

	public void setLeftAlignment(String iStyleName) {
		this.setAlignment(iStyleName, HSSFCellStyle.ALIGN_LEFT);
	}

	public void setCenterAlignment(String iStyleName) {
		this.setAlignment(iStyleName, HSSFCellStyle.ALIGN_CENTER);
	}

	public void setAlignment(String iStyleName, short align) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		this.setAlignment(style, align);
	}

	public void setAlignment(String iStyleName, String align) {
		if (align.equalsIgnoreCase("LEFT")) {
			this.setLeftAlignment(iStyleName);
		} else if (align.equalsIgnoreCase("CENTER")) {
			this.setCenterAlignment(iStyleName);
		} else if (align.equalsIgnoreCase("RIGHT")) {
			this.setRightAlignment(iStyleName);
		} else {
			this.setLeftAlignment(iStyleName);
		}
	}

	public void setAlignment(HSSFCellStyle style, short align) {
		if (style != null)
			style.setAlignment(align);
	}

	public void setVerticalAlignment(String iStyleName, short align) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		style.setVerticalAlignment(align);
	}

	/*
	 *  Fill data
	 */

	public void setMergeCell(int iSheetNum, int iStartRow, int iStartCol, int iEndRow, int iEndCol) {
	    sheet[iSheetNum].addMergedRegion(new CellRangeAddress(iStartRow, iEndRow, iStartCol, iEndCol));
	}

	public void fillDataWithStyleAndMerge(HSSFCellStyle style, int iSheetNum, int iStartRow, int iEndRow, int iStartCol, int iEndCol, String Value) {

		int iRow, iCol;

		for (iRow = iStartRow; iRow <= iEndRow; iRow++) {
			row = sheet[iSheetNum].getRow((short) iRow);

			if (row == null) {
				row = sheet[iSheetNum].createRow((short) iStartRow);
			}

			for (iCol = iStartCol; iCol <= iEndCol; iCol++) {
				cell = row.getCell(iCol);

				if (row == null) {
					cell = row.createCell(iCol);
				}

				cell.setCellStyle(style);
				cell.setCellValue(Value);
			}
		}

		if (iEndRow > iStartRow || iEndCol > iStartCol) {
			sheet[iSheetNum].addMergedRegion(new CellRangeAddress(iStartRow, iEndRow, iStartCol, iEndCol));
		}
	}

	public void fillDataWithStyleAndMerge(String iStyleName, int iSheetNum, int iStartRow, int iEndRow, int iStartCol, int iEndCol, String Value) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		if (style != null) {
			int iRow, iCol;

			for (iRow = iStartRow; iRow <= iEndRow; iRow++) {
				row = sheet[iSheetNum].getRow((short) iRow);

				if (row == null) {
					row = sheet[iSheetNum].createRow((short) iStartRow);
				}

				for (iCol = iStartCol; iCol <= iEndCol; iCol++) {
					cell = row.getCell(iCol);

					if (cell == null) {
						cell = row.createCell(iCol);
					}

					cell.setCellStyle(style);
					cell.setCellValue(Value);
				}
			}

			if (iEndRow > iStartRow || iEndCol > iStartCol) {
			    sheet[iSheetNum].addMergedRegion(new CellRangeAddress(iStartRow, iEndRow, iStartCol, iEndCol));
			}
		}
	}

	public void fillDoubleWithStyle(HSSFCellStyle style, int isheetNum, int iStartRow, int iStartCol, double value) {
		row = sheet[isheetNum].getRow((short) iStartRow);

		if (row == null) {
			row = sheet[isheetNum].createRow((short) iStartRow);
		}

		cell = row.createCell(iStartCol);
		cell.setCellStyle(style);
		cell.setCellValue(value);
	}

	public void fillDoubleWithStyle(String iStyleName, int isheetNum, int iStartRow, int iStartCol, double value) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		if (style != null) {
			row = sheet[isheetNum].getRow((short) iStartRow);

			if (row == null) {
				row = sheet[isheetNum].createRow((short) iStartRow);
			}

			cell = row.createCell(iStartCol);
			cell.setCellStyle(style);
			cell.setCellValue(value);
		}
	}
	
	public void fillIntegerWithStyle(HSSFCellStyle style, int isheetNum, int iStartRow, int iStartCol, int value) {
		row = sheet[isheetNum].getRow((short) iStartRow);

		if (row == null) {
			row = sheet[isheetNum].createRow((short) iStartRow);
		}

		cell = row.createCell(iStartCol);
		cell.setCellStyle(style);
		cell.setCellValue(value);
	}

	public void fillIntegerWithStyle(String iStyleName, int isheetNum, int iStartRow, int iStartCol, int value) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		if (style != null) {
			row = sheet[isheetNum].getRow((short) iStartRow);

			if (row == null) {
				row = sheet[isheetNum].createRow((short) iStartRow);
			}

			cell = row.createCell(iStartCol);
			cell.setCellStyle(style);
			cell.setCellValue(value);
		}
	}

	public void fillNumericWithStyleAndMerge(HSSFCellStyle style, int iSheetNum, int iStartRow, int iEndRow, int iStartCol, int iEndCol, int Value) {

		int iRow, iCol;

		for (iRow = iStartRow; iRow <= iEndRow; iRow++) {
			row = sheet[iSheetNum].getRow((short) iRow);

			if (row == null) {
				row = sheet[iSheetNum].createRow((short) iStartRow);
			}

			for (iCol = iStartCol; iCol <= iEndCol; iCol++) {
				cell = row.getCell(iCol);

				if (row == null) {
					cell = row.createCell(iCol);
				}

				cell.setCellStyle(style);
				cell.setCellValue(Value);
			}
		}

		if (iEndRow > iStartRow || iEndCol > iStartCol) {
		    sheet[iSheetNum].addMergedRegion(new CellRangeAddress(iStartRow, iEndRow, iStartCol, iEndCol));
		}
	}

	public void fillNumericWithStyleAndMerge(String iStyleName, int iSheetNum, int iStartRow, int iEndRow, int iStartCol, int iEndCol, int Value) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		int iRow, iCol;

		for (iRow = iStartRow; iRow <= iEndRow; iRow++) {
			row = sheet[iSheetNum].getRow((short) iRow);

			if (row == null) {
				row = sheet[iSheetNum].createRow((short) iStartRow);
			}

			for (iCol = iStartCol; iCol <= iEndCol; iCol++) {
				cell = row.getCell(iCol);

				if (cell == null) {
					cell = row.createCell(iCol);
				}

				cell.setCellStyle(style);
				cell.setCellValue(Value);
			}
		}

		if (iEndRow > iStartRow || iEndCol > iStartCol) {
		    sheet[iSheetNum].addMergedRegion(new CellRangeAddress(iStartRow, iEndRow, iStartCol, iEndCol));
		}
	}

	public void fillNumericWithStyleAndMerge(HSSFCellStyle style, int iSheetNum, int iStartRow, int iEndRow, int iStartCol, int iEndCol, double Value) {

		int iRow, iCol;

		for (iRow = iStartRow; iRow <= iEndRow; iRow++) {
			row = sheet[iSheetNum].getRow((short) iRow);

			if (row == null) {
				row = sheet[iSheetNum].createRow((short) iStartRow);
			}

			for (iCol = iStartCol; iCol <= iEndCol; iCol++) {
				cell = row.getCell(iCol);

				if (row == null) {
					cell = row.createCell(iCol);
				}

				cell.setCellStyle(style);
				cell.setCellValue(Value);
			}
		}

		if (iEndRow > iStartRow || iEndCol > iStartCol) {
		    sheet[iSheetNum].addMergedRegion(new CellRangeAddress(iStartRow, iEndRow, iStartCol, iEndCol));
		}
	}

	public void fillNumericWithStyleAndMerge(String iStyleName, int iSheetNum, int iStartRow, int iEndRow, int iStartCol, int iEndCol, double Value) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		int iRow, iCol;

		for (iRow = iStartRow; iRow <= iEndRow; iRow++) {
			row = sheet[iSheetNum].getRow((short) iRow);

			if (row == null) {
				row = sheet[iSheetNum].createRow((short) iStartRow);
			}

			for (iCol = iStartCol; iCol <= iEndCol; iCol++) {
				cell = row.getCell(iCol);

				if (cell == null) {
					cell = row.createCell(iCol);
				}

				cell.setCellStyle(style);
				cell.setCellValue(Value);
			}
		}

		if (iEndRow > iStartRow || iEndCol > iStartCol) {
		    sheet[iSheetNum].addMergedRegion(new CellRangeAddress(iStartRow, iEndRow, iStartCol, iEndCol));
		}
	}

	public void fillArrayDataWithSingleStyle(String iStyleName, int iSheetNum, int iStartRow, String[] value) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		row = sheet[iSheetNum].getRow((short) iStartRow);

		if (row == null) {
			row = sheet[iSheetNum].createRow((short) iStartRow);
		}

		for (int iCol = 0; iCol < value.length; iCol++) {
			cell = row.getCell(iCol);

			if (cell == null) {
				cell = row.createCell(iCol);
			}

			cell.setCellStyle(style);
			cell.setCellValue(value[iCol]);
		}
	}

	public void fillArrayDataWithMultiStyle(String[] styleArr, int iSheetNum, int iStartRow, String[] value) {
		row = sheet[iSheetNum].getRow((short) iStartRow);

		if (row == null) {
			row = sheet[iSheetNum].createRow((short) iStartRow);
		}

		for (int iCol = 0; iCol < value.length; iCol++) {
			cell = row.createCell(iCol);
			cell.setCellStyle((HSSFCellStyle) styleHash.get(styleArr[iCol]));
			cell.setCellValue(value[iCol]);
		}
	}

	public void fillListDataWithStyle(String[] styleArr, int iSheetNum, int iStartRow, List list) {

		for (int i = 0; i < list.size(); ++i) {
			String[] value = (String[]) list.get(i);

			row = sheet[iSheetNum].getRow((short) iStartRow + i);

			if (row == null) {
				row = sheet[iSheetNum].createRow((short) iStartRow + i);
			}

			for (int iCol = 0; iCol < value.length; iCol++) {
				cell = row.createCell(iCol);
				cell.setCellStyle((HSSFCellStyle) styleHash.get(styleArr[iCol]));
				cell.setCellValue(value[iCol]);
			}

			if (i % 1000 == 0)
				System.out.println(i);
		}

	}

	public void fillArrayData(int iSheetNum, int iStartRow, String[] value) {
		row = sheet[iSheetNum].getRow((short) iStartRow);

		if (row == null) {
			row = sheet[iSheetNum].createRow((short) iStartRow);
		}

		for (int iCol = 0; iCol < value.length; iCol++) {
			cell = row.getCell(iCol);

			if (cell == null) {
				cell = row.createCell(iCol);
			}

			cell.setCellValue(value[iCol]);
		}
	}

	public void fillDataWithStyle(HSSFCellStyle style, int isheetNum, int iStartRow, int iStartCol, String value) {
		row = sheet[isheetNum].getRow((short) iStartRow);

		if (row == null) {
			row = sheet[isheetNum].createRow((short) iStartRow);
		}

		cell = row.createCell(iStartCol);
		cell.setCellStyle(style);
		cell.setCellValue(value);
	}

	public void fillDataWithStyle(String iStyleName, int isheetNum, int iStartRow, int iStartCol, String value) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		if (style != null) {
			row = sheet[isheetNum].getRow((short) iStartRow);

			if (row == null) {
				row = sheet[isheetNum].createRow((short) iStartRow);
			}

			cell = row.createCell(iStartCol);
			cell.setCellStyle(style);
			cell.setCellValue(value);
		}
	}

	public void fillData(int isheetNum, int iStartRow, int iStartCol, String value) {
		row = sheet[isheetNum].getRow(iStartRow);

		if (row == null) {
			row = sheet[isheetNum].createRow((short) iStartRow);
		}

		cell = row.createCell(iStartCol);
		cell.setCellValue(value);
	}

	public void fillFormulaWithStyle(String iStyleName, int isheetNum, int iStartRow, int iStartCol, String formula) {
		HSSFCellStyle style = (HSSFCellStyle) styleHash.get(iStyleName);

		row = sheet[isheetNum].createRow((short) iStartRow);

		cell = row.createCell(iStartCol);
		cell.setCellStyle(style);
		cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		cell.setCellFormula(formula);
	}

	public void fillFormula(int isheetNum, int iStartRow, int iStartCol, String formula) {
		row = sheet[isheetNum].createRow((short) iStartRow);

		cell = row.createCell(iStartCol);
		cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		cell.setCellFormula(formula);
	}

	public void createExcelFile(String fileName) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(fileName);
		wb.write(fileOut);
		fileOut.close();
	}
}