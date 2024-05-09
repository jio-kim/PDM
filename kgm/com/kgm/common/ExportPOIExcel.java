package com.kgm.common;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.kgm.common.utils.progressbar.WaitProgressor;
import com.teamcenter.rac.util.MessageBox;

public class ExportPOIExcel
{
	/**
	 * SWT Table의 데이터를 Excel로 만드는 부분...
	 * 
	 * @param shell
	 * @param exportTable
	 *            SWT Table
	 * @param sheetName
	 *            sheet의 이름.
	 * @param title
	 *            최 상위에 적히는 타이틀...
	 * @param comments
	 *            타이틀 아래에 필요시 입력... 없으면 공백.
	 * @param noExportColumnList
	 *            제외하고 싶은 column이 있을 경우 리스트를 만듬.
	 * @return
	 */
	public static String exportDialog(final Shell shell, final Object exportTable, final String sheetName, final String title, final String comments, final ArrayList<String> noExportColumnList)
	{
		Date today = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm");
		FileDialog fDialog = new FileDialog(shell, SWT.SINGLE | SWT.SAVE);
		fDialog.setText("Save");
		fDialog.setFilterNames(new String[] { "Excel File" });
		fDialog.setFilterExtensions(new String[] { "*.xlsx" });
		fDialog.setFileName(title + "_" + df.format(today) + ".xlsx");
		final String fullPath = fDialog.open();
		if (fullPath == null)
		{
			return "";
		}
		new Thread(new Runnable()
		{
			public void run()
			{
				final WaitProgressor progress = new WaitProgressor(shell);
				progress.start();
				progress.setMessage("Table Data Export...");
				Display.getDefault().syncExec(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							String outputFile = "";
							if (exportTable instanceof Table)
							{
								outputFile = ExportPOIExcel.excelExport((Table) exportTable, sheetName, title, comments, noExportColumnList, fullPath);
							} else if (exportTable instanceof Grid)
							{
								outputFile = ExportPOIExcel.excelExport((Grid) exportTable, sheetName, title, comments, noExportColumnList, fullPath);
							}
							progress.end();
							MessageBox.post(shell, "Excel Export가 완료되었습니다. Excel 파일을 Open 합니다.", "완료", MessageBox.INFORMATION);
							Runtime.getRuntime().exec("cmd /c \"" + outputFile + "\"");
						} catch (Exception e2)
						{
							progress.end();
							e2.printStackTrace();
							MessageBox.post(shell, e2, true);
						}
					}
				});
			}
		}).start();
		return fullPath;
	}

	public static String excelExport(Table exportTable, String title, String comments, String fullFileName) throws Exception
	{
		return excelExport(exportTable, title, title, comments, new ArrayList<String>(), fullFileName);
	}

	public static String excelExport(Grid exportTable, String title, String comments, String fullFileName) throws Exception
	{
		return excelExport(exportTable, title, title, comments, new ArrayList<String>(), fullFileName);
	}

	public static String excelExport(Table exportTable, String sheetName, String title, String comments, ArrayList<String> noExportColumnList, String fullFileName) throws Exception
	{
		String outputFileNames = "";

		int excelMaxRow = 1000000;
		int excelFileNumber = 1;

		int tableHeaderRowIndex = 3;
		int tableHeaderColumnIndex = 1;

		SXSSFWorkbook wb = null;
		SXSSFSheet sheet = null;
		XSSFCellStyle tableHeaderStyle = null;
		XSSFCellStyle tableCellStyle = null;

		int excelRow = 0;
		for (int row = 0; row < exportTable.getItemCount(); row++)
		{
			if (excelRow == 0)
			{
				wb = new SXSSFWorkbook();
				sheet = (SXSSFSheet) wb.createSheet(sheetName);

				XSSFCellStyle titleStyle = (XSSFCellStyle) wb.createCellStyle();
				titleStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				titleStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
				titleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
				XSSFFont titleFont = (XSSFFont) wb.createFont();
				titleFont.setBold(true);
				titleFont.setFontHeight(20D);
				titleStyle.setFont(titleFont);

				tableHeaderStyle = (XSSFCellStyle) wb.createCellStyle();
				tableHeaderStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
				tableHeaderStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setFillForegroundColor(new XSSFColor(new Color(252, 213, 180)));
				tableHeaderStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

				tableCellStyle = (XSSFCellStyle) wb.createCellStyle();
				tableCellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				tableCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

				//title
				sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, exportTable.getColumnCount()));
				Row titleRow = sheet.createRow(1);
				Cell titleCell = titleRow.createCell(1);
				titleCell.setCellValue(title);
				titleCell.setCellStyle(titleStyle);

				//comments
				Row titleRow2 = sheet.createRow(tableHeaderRowIndex - 1);
				titleRow2.createCell(1).setCellValue(comments);

				//header
				int excelColumn = tableHeaderColumnIndex;
				Row xssfRow = sheet.createRow(excelRow++ + tableHeaderRowIndex);
				for (int i = 0; i < exportTable.getColumnCount(); i++)
				{
					int width = exportTable.getColumn(i).getWidth();
					String headerString = exportTable.getColumn(i).getText();
					if (width == 0 || (noExportColumnList != null && noExportColumnList.contains(headerString)))
					{
						continue;
					}
					Cell cell = xssfRow.createCell(excelColumn);
					cell.setCellValue(headerString);
					cell.setCellStyle(tableHeaderStyle);
					sheet.autoSizeColumn(excelColumn);
					excelColumn++;
				}
			}

			//data
			int excelColumn = tableHeaderColumnIndex;
			Row xssfRow = sheet.createRow(excelRow++ + tableHeaderRowIndex);
			for (int i = 0; i < exportTable.getColumnCount(); i++)
			{
				int width = exportTable.getColumn(i).getWidth();
				String headerString = exportTable.getColumn(i).getText();
				String cellString = exportTable.getItem(row).getText(i);
				if (width == 0 || (noExportColumnList != null && noExportColumnList.contains(headerString)))
				{
					continue;
				}
				org.eclipse.swt.graphics.Color c = exportTable.getItem(row).getBackground(i);
				XSSFCellStyle colorCellStyle = tableCellStyle;
				if (c != null)
				{
					colorCellStyle = (XSSFCellStyle) tableCellStyle.clone();
					colorCellStyle.setFillForegroundColor(new XSSFColor(new Color(c.getRed(), c.getGreen(), c.getBlue())));
					colorCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
				}
				Cell cell = xssfRow.createCell(excelColumn);
				cell.setCellValue(cellString);
				cell.setCellStyle(colorCellStyle);
				sheet.setColumnWidth(excelColumn, width * 40);
				excelColumn++;
			}
			if (excelRow > excelMaxRow)
			{
				excelRow = 0;
				int pindex = fullFileName.lastIndexOf(".");
				String outputFileName = fullFileName.substring(0, pindex) + "_" + excelFileNumber + fullFileName.substring(pindex);
				outputFileNames += outputFileName + "\n";
				FileOutputStream fileOut = new FileOutputStream(outputFileName);
				wb.write(fileOut);
				fileOut.close();

				excelFileNumber++;
			} else if (row == (exportTable.getItemCount() - 1))
			{
				String outputFileName = fullFileName;
				if (excelFileNumber != 1)
				{
					int pindex = fullFileName.lastIndexOf(".");
					outputFileName = fullFileName.substring(0, pindex) + "_" + excelFileNumber + fullFileName.substring(pindex);
				}
				outputFileNames += outputFileName;
				FileOutputStream fileOut = new FileOutputStream(outputFileName);
				wb.write(fileOut);
				fileOut.close();
			}
		}
		return outputFileNames;
	}

	public static String excelExport(Grid exportTable, String sheetName, String title, String comments, ArrayList<String> noExportColumnList, String fullFileName) throws Exception
	{
		String outputFileNames = "";

		int excelMaxRow = 1000000;
		int excelFileNumber = 1;

		int TITLE_ROW_INDEX = 1;
		int COMMENTS_ROW_INDEX = 2;
		int TABLE_HEADER_ROW_INDEX = 3;
		int TABLE_COLUMN_INDEX = 1;

		SXSSFWorkbook wb = null;
		SXSSFSheet sheet = null;
		XSSFCellStyle tableHeaderStyle = null;
		XSSFCellStyle tableCellStyle = null;

		int excelRow = 0;
		for (int row = 0; row < exportTable.getItemCount(); row++)
		{
			if (excelRow == 0)
			{
				wb = new SXSSFWorkbook();
				sheet = (SXSSFSheet) wb.createSheet(sheetName);

				XSSFCellStyle titleStyle = (XSSFCellStyle) wb.createCellStyle();
				titleStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				titleStyle.setVerticalAlignment(XSSFCellStyle.ALIGN_CENTER);
				titleStyle.setBorderBottom(XSSFCellStyle.BORDER_THICK);
				titleStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				titleStyle.setBorderLeft(XSSFCellStyle.BORDER_THICK);
				titleStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				titleStyle.setBorderRight(XSSFCellStyle.BORDER_THICK);
				titleStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				titleStyle.setBorderTop(XSSFCellStyle.BORDER_THICK);
				titleStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				titleStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
				titleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
				XSSFFont titleFont = (XSSFFont) wb.createFont();
				titleFont.setBold(true);
				titleFont.setFontHeight(20D);
				titleStyle.setFont(titleFont);

				tableHeaderStyle = (XSSFCellStyle) wb.createCellStyle();
				tableHeaderStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
				tableHeaderStyle.setVerticalAlignment(XSSFCellStyle.ALIGN_CENTER);
				tableHeaderStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				tableHeaderStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				tableHeaderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				tableHeaderStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				tableHeaderStyle.setFillForegroundColor(new XSSFColor(new Color(252, 213, 180)));
				tableHeaderStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

				tableCellStyle = (XSSFCellStyle) wb.createCellStyle();
				tableCellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				tableCellStyle.setVerticalAlignment(XSSFCellStyle.ALIGN_CENTER);
				tableCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				tableCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				tableCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				tableCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());

				//title
				sheet.addMergedRegion(new CellRangeAddress(TITLE_ROW_INDEX, TITLE_ROW_INDEX, TABLE_COLUMN_INDEX, exportTable.getColumnCount()));
				Row titleRow = sheet.createRow(1);
				for (int i = TABLE_COLUMN_INDEX; i < TABLE_COLUMN_INDEX + exportTable.getColumnCount(); i++)
				{
					Cell titleCell = titleRow.createCell(i);
					titleCell.setCellValue(title);
					titleCell.setCellStyle(titleStyle);
				}

				//comments
				Row titleRow2 = sheet.createRow(COMMENTS_ROW_INDEX);
				titleRow2.createCell(TABLE_COLUMN_INDEX).setCellValue(comments);

				//header
				//column group이 있는 경우 여러줄의 column을 미리 생성해둔다.
				int depth = exportTable.getColumnGroupHeaderDepth();
				//수정 HeaderDepth 김민석
				
				Row[] xssfRowArray = new Row[depth];
				for (int d = 0; d < depth; d++)
				{
					Row txssfRow = sheet.createRow(excelRow++ + TABLE_HEADER_ROW_INDEX);
					xssfRowArray[d] = txssfRow;
					for (int i = TABLE_COLUMN_INDEX; i < TABLE_COLUMN_INDEX + exportTable.getColumnCount(); i++)
					{
						Cell cell = txssfRow.createCell(i);
						cell.setCellStyle(tableHeaderStyle);
					}
				}
				//column이 있는 제일 아래 header row...
				Row xssfRow = sheet.createRow(excelRow++ + TABLE_HEADER_ROW_INDEX);
				Hashtable<GridColumn, Integer[]> columnHash = new Hashtable<GridColumn, Integer[]>();
				for (int i = 0; i < exportTable.getColumnCount(); i++)
				{
					GridColumn column = exportTable.getColumn(i);
					int width = column.getWidth();
					String headerString = column.getText();
					if (width == 0 || (noExportColumnList != null && noExportColumnList.contains(headerString)))
					{
						continue;
					}
					Cell cell = xssfRow.createCell(TABLE_COLUMN_INDEX + i);
					//column group 이 없다면... 위로 전부다 merge
					if(column.getColumnGroup() == null)
					{
						sheet.addMergedRegion(new CellRangeAddress(xssfRow.getRowNum()-depth, xssfRow.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex()));
						Cell aCell = sheet.getRow(xssfRow.getRowNum()-depth).getCell(cell.getColumnIndex());
						aCell.setCellValue(headerString);
						aCell.setCellStyle(tableHeaderStyle);
					}else
					{
						cell.setCellValue(headerString);
					}
					cell.setCellStyle(tableHeaderStyle);
					columnHash.put(column, new Integer[] { xssfRow.getRowNum(), TABLE_COLUMN_INDEX + i });
				}
				//column merge...
				if (exportTable.getColumnGroupCount() > 0)
				{
					Hashtable<GridColumnGroup, CellRangeAddress> columnGroupAddressHash = new Hashtable<GridColumnGroup, CellRangeAddress>();
					//column header merge
					GridColumnGroup[] gridColumnGroups = exportTable.getColumnGroups();
					//제일 아래의 group 부터.. 시작..
					for (int i = gridColumnGroups.length-1;i>=0;i--)
					{
						GridColumnGroup gridColumnGroup = gridColumnGroups[i];
						GridColumn[] groupColumns = gridColumnGroup.getColumns();
						int firstRow = -1;
						int lastRow = -1;
						int firstColumn = -1;
						int lastColumn = -1;
						for (GridColumn groupColumn : groupColumns)
						{
							Integer[] rowColIndex = columnHash.get(groupColumn);
							if (firstRow == -1 || firstRow > rowColIndex[0])
							{
								firstRow = rowColIndex[0] - 1;
							}
							if (lastRow == -1 || lastRow < rowColIndex[0])
							{
								lastRow = rowColIndex[0] - 1;
							}
							if (firstColumn == -1 || firstColumn > rowColIndex[1])
							{
								firstColumn = rowColIndex[1];
							}
							if (lastColumn == -1 || lastColumn < rowColIndex[1])
							{
								lastColumn = rowColIndex[1];
							}
						}
						//하위에 column이 있으면 column 만큼 merge
						//column 바로 상위의 group인 경우..
						CellRangeAddress cellRangeAddress = null;
						if (firstRow != -1 && lastRow != -1 && firstColumn != -1 && lastColumn != -1)
						{
							cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstColumn, lastColumn);
							sheet.getRow(firstRow).getCell(firstColumn).setCellValue(gridColumnGroup.getText());
							columnGroupAddressHash.put(gridColumnGroup, cellRangeAddress);
						}
						GridColumnGroup parentColumnGroup = gridColumnGroup.getParentGroup();
						//GridColumnGroup parentColumnGroup = null;
						//수정 김민석 parentColumnGroup
						
						if(parentColumnGroup != null)
						{
							if(columnGroupAddressHash.containsKey(parentColumnGroup))
							{
								CellRangeAddress parentRangeAddress = columnGroupAddressHash.get(parentColumnGroup);
								CellRangeAddress childRangeAddress = columnGroupAddressHash.get(gridColumnGroup);
								if(parentRangeAddress.getFirstColumn() > childRangeAddress.getFirstColumn())
								{
									parentRangeAddress.setFirstColumn(childRangeAddress.getFirstColumn());
								}
								if(parentRangeAddress.getLastColumn() < childRangeAddress.getLastColumn())
								{
									parentRangeAddress.setLastColumn(childRangeAddress.getLastColumn());
								}
//								sheet.getRow(parentRangeAddress.getFirstRow()).getCell(parentRangeAddress.getFirstColumn()).setCellValue(parentColumnGroup.getText());
							}else
							{
								CellRangeAddress childRangeAddress = columnGroupAddressHash.get(gridColumnGroup).copy();
								childRangeAddress.setFirstRow(childRangeAddress.getFirstRow()-1);
								childRangeAddress.setLastRow(childRangeAddress.getLastRow()-1);
								columnGroupAddressHash.put(parentColumnGroup, childRangeAddress);
							}
						}else
						{
							//상위가 없으면.. 첫번째 column까지 합치기..
							CellRangeAddress gridRangeAddress = columnGroupAddressHash.get(gridColumnGroup);
							if(TABLE_HEADER_ROW_INDEX < gridRangeAddress.getFirstRow())
							{
								gridRangeAddress.setFirstRow(TABLE_HEADER_ROW_INDEX);
							}
						}
						CellRangeAddress gridRangeAddress = columnGroupAddressHash.get(gridColumnGroup);
						sheet.addMergedRegion(gridRangeAddress);
						sheet.getRow(gridRangeAddress.getFirstRow()).getCell(gridRangeAddress.getFirstColumn()).setCellValue(gridColumnGroup.getText());
					}
				}
			}
			//data
			Row xssfRow = sheet.createRow(excelRow++ + TABLE_HEADER_ROW_INDEX);
			for (int i = 0; i < exportTable.getColumnCount(); i++)
			{
				int width = exportTable.getColumn(i).getWidth();
				String headerString = exportTable.getColumn(i).getText();
				String cellString = exportTable.getItem(row).getText(i);
				if (width == 0 || (noExportColumnList != null && noExportColumnList.contains(headerString)))
				{
					continue;
				}
				org.eclipse.swt.graphics.Color c = exportTable.getItem(row).getBackground(i);
				XSSFCellStyle colorCellStyle = (XSSFCellStyle) tableCellStyle.clone();
				if (c != null)
				{
					colorCellStyle.setFillForegroundColor(new XSSFColor(new Color(c.getRed(), c.getGreen(), c.getBlue())));
					colorCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
				}
				int alignment = exportTable.getColumn(i).getAlignment();
				if(alignment != SWT.LEFT)
				{
					if(alignment == SWT.CENTER)
					{
						colorCellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
					}else if(alignment == SWT.RIGHT)
					{
						colorCellStyle.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
					}
				}
				Cell cell = xssfRow.createCell(TABLE_COLUMN_INDEX + i);
				cell.setCellValue(cellString);
				cell.setCellStyle(colorCellStyle);
				sheet.setColumnWidth(TABLE_COLUMN_INDEX + i, width * 40);
			}
			if (excelRow > excelMaxRow)
			{
				excelRow = 0;
				int pindex = fullFileName.lastIndexOf(".");
				String outputFileName = fullFileName.substring(0, pindex) + "_" + excelFileNumber + fullFileName.substring(pindex);
				outputFileNames += outputFileName + "\n";
				FileOutputStream fileOut = new FileOutputStream(outputFileName);
				wb.write(fileOut);
				fileOut.close();

				excelFileNumber++;
			} else if (row == (exportTable.getItemCount() - 1))
			{
				String outputFileName = fullFileName;
				if (excelFileNumber != 1)
				{
					int pindex = fullFileName.lastIndexOf(".");
					outputFileName = fullFileName.substring(0, pindex) + "_" + excelFileNumber + fullFileName.substring(pindex);
				}
				outputFileNames += outputFileName;
				FileOutputStream fileOut = new FileOutputStream(outputFileName);
				wb.write(fileOut);
				fileOut.close();
			}
		}
		return outputFileNames;
	}

	public static String excelExport(ArrayList<ArrayList<String>> dataList, String sheetName, String title, String comments, ArrayList<String> noExportColumnList, String fullFileName) throws Exception
	{
		ArrayList<String> columnList = dataList.get(0);
		String outputFileNames = "";

		int excelMaxRow = 1000000;
		int excelFileNumber = 1;

		int tableHeaderRowIndex = 3;
		int tableHeaderColumnIndex = 1;

		SXSSFWorkbook wb = null;
		SXSSFSheet sheet = null;
		XSSFCellStyle tableHeaderStyle = null;
		XSSFCellStyle tableCellStyle = null;

		int excelRow = 0;
		for (int row = 2; row < dataList.size(); row++)
		{
			if (excelRow == 0)
			{
				wb = new SXSSFWorkbook();
				sheet = (SXSSFSheet) wb.createSheet(sheetName);

				XSSFCellStyle titleStyle = (XSSFCellStyle) wb.createCellStyle();
				titleStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				titleStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
				titleStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
				XSSFFont titleFont = (XSSFFont) wb.createFont();
				titleFont.setBold(true);
				titleFont.setFontHeight(20D);
				titleStyle.setFont(titleFont);

				tableHeaderStyle = (XSSFCellStyle) wb.createCellStyle();
				tableHeaderStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
				tableHeaderStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableHeaderStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableHeaderStyle.setFillForegroundColor(new XSSFColor(new Color(252, 213, 180)));
				tableHeaderStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

				tableCellStyle = (XSSFCellStyle) wb.createCellStyle();
				tableCellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				tableCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

				//title
				sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, columnList.size() - noExportColumnList.size()));
				Row titleRow = sheet.createRow(1);
				Cell titleCell = titleRow.createCell(1);
				titleCell.setCellValue(title);
				titleCell.setCellStyle(titleStyle);

				//comments
				Row titleRow2 = sheet.createRow(tableHeaderRowIndex - 1);
				titleRow2.createCell(1).setCellValue(comments);

				//header
				int excelColumn = tableHeaderColumnIndex;
				Row xssfRow = sheet.createRow(excelRow++ + tableHeaderRowIndex);
				for (int i = 0; i < columnList.size(); i++)
				{
					String headerString = columnList.get(i);
					if (noExportColumnList != null && noExportColumnList.contains(headerString))
					{
						continue;
					}
					Cell cell = xssfRow.createCell(excelColumn);
					cell.setCellValue(headerString);
					cell.setCellStyle(tableHeaderStyle);
					sheet.autoSizeColumn(excelColumn);
					excelColumn++;
				}
			}

			//data
			int excelColumn = tableHeaderColumnIndex;
			Row xssfRow = sheet.createRow(excelRow++ + tableHeaderRowIndex);
			for (int i = 0; i < columnList.size(); i++)
			{
				String headerString = columnList.get(i);
				String cellString = dataList.get(row).get(i);
				if (noExportColumnList != null && noExportColumnList.contains(headerString))
				{
					continue;
				}
				Cell cell = xssfRow.createCell(excelColumn);
				cell.setCellValue(cellString);
				cell.setCellStyle(tableCellStyle);
				excelColumn++;
			}
			if (excelRow > excelMaxRow)
			{
				excelRow = 0;
				int pindex = fullFileName.lastIndexOf(".");
				String outputFileName = fullFileName.substring(0, pindex) + "_" + excelFileNumber + fullFileName.substring(pindex);
				outputFileNames += outputFileName + "\n";
				FileOutputStream fileOut = new FileOutputStream(outputFileName);
				wb.write(fileOut);
//				wb.close();
				fileOut.flush();
				fileOut.close();

				excelFileNumber++;
			} else if (row == (dataList.size() - 1))
			{
				String outputFileName = fullFileName;
				if (excelFileNumber != 1)
				{
					int pindex = fullFileName.lastIndexOf(".");
					outputFileName = fullFileName.substring(0, pindex) + "_" + excelFileNumber + fullFileName.substring(pindex);
				}
				outputFileNames += outputFileName;
				FileOutputStream fileOut = new FileOutputStream(outputFileName);
				wb.write(fileOut);
//				wb.close();
				fileOut.flush();
				fileOut.close();
			}
		}
		return outputFileNames;
	}

	public static void excelExportToTemplate(Table exportTable, String sheetName, String fullFileName, Hashtable<String, String> cellByNameHash) throws Exception
	{
		int startRow = 0;
		int startColumn = 0;
		int endColumn = 0;
		XSSFCellStyle tableCellStyle = null;

		FileInputStream fis = new FileInputStream(fullFileName);
		SXSSFWorkbook xlsWorkBook = new SXSSFWorkbook(new XSSFWorkbook(fis));
		SXSSFSheet dataSheet = (SXSSFSheet) xlsWorkBook.getSheet(sheetName);
		if (dataSheet == null)
		{
			dataSheet = (SXSSFSheet) xlsWorkBook.getSheetAt(0);
			if (dataSheet == null)
			{
				throw new Exception("엑셀 파일에서 sheet를 찾을 수 없습니다. 확인 바랍니다.");
			}
		}
		Name startName = xlsWorkBook.getName("START");
		AreaReference aref = new AreaReference(startName.getRefersToFormula());
		CellReference[] crefs = aref.getAllReferencedCells();
		Cell startCell = null;
		for (int i = 0; i < crefs.length; i++)
		{
			SXSSFSheet s = (SXSSFSheet) xlsWorkBook.getSheet(crefs[i].getSheetName());
			Row r = s.getRow(crefs[i].getRow());
			Cell c = r.getCell(crefs[i].getCol());
			if (c != null)
			{
				startCell = c;
			}
		}
		if (startCell == null)
		{
			throw new Exception("엑셀 파일에서 시작점을 찾지 못했습니다. 편집과정에서 사라졌을 수 있습니다. 다시 파일을 자동 생성하여 사용하십시오.");
		}
		startColumn = startCell.getColumnIndex() + 1;
		startRow = startCell.getRowIndex() + 1;

		Name endName = xlsWorkBook.getName("END");
		aref = new AreaReference(endName.getRefersToFormula());
		crefs = aref.getAllReferencedCells();
		Cell endCell = null;
		for (int i = 0; i < crefs.length; i++)
		{
			SXSSFSheet s = (SXSSFSheet) xlsWorkBook.getSheet(crefs[i].getSheetName());
			Row r = s.getRow(crefs[i].getRow());
			Cell c = r.getCell(crefs[i].getCol());
			if (c != null)
			{
				endCell = c;
			}
		}
		if (endCell == null)
		{
			throw new Exception("엑셀 파일에서 끝지점을 찾지 못했습니다. 편집과정에서 사라졌을 수 있습니다. 다시 파일을 자동 생성하여 사용하십시오.");
		}
		endColumn = endCell.getColumnIndex() - 1;

		//특정 cell에 값 입력
		if (cellByNameHash != null)
		{
			Enumeration<String> enum1 = cellByNameHash.keys();
			while (enum1.hasMoreElements())
			{
				String name = enum1.nextElement();
				String value = cellByNameHash.get(name);
				Name cellName = xlsWorkBook.getName(name);
				aref = new AreaReference(cellName.getRefersToFormula());
				crefs = aref.getAllReferencedCells();
				for (int i = 0; i < crefs.length; i++)
				{
					SXSSFSheet s = (SXSSFSheet) xlsWorkBook.getSheet(crefs[i].getSheetName());
					Row r = s.getRow(crefs[i].getRow());
					Cell c = r.getCell(crefs[i].getCol());
					if (c != null)
					{
						c.setCellValue(value);
					}
				}
			}
		}
		int excelRow = 0;
		for (int row = 0; row < exportTable.getItemCount(); row++)
		{
			if (excelRow == 0)
			{
				tableCellStyle = (XSSFCellStyle) xlsWorkBook.createCellStyle();
				tableCellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				tableCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
				tableCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				tableCellStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
			}

			//data
			int excelColumn = startColumn;
			Row xssfRow = dataSheet.getRow(excelRow + startRow);
			if (xssfRow == null)
			{
				xssfRow = dataSheet.createRow(excelRow + startRow);
			}
			excelRow++;
			for (int i = 0; i < exportTable.getColumnCount(); i++)
			{
				int width = exportTable.getColumn(i).getWidth();
				String cellString = exportTable.getItem(row).getText(i);
				if (width == 0)
				{
					continue;
				}
				Cell cell = xssfRow.getCell(excelColumn);
				if (cell == null)
				{
					cell = xssfRow.createCell(excelColumn);
					cell.setCellStyle(tableCellStyle);
				}
				cell.setCellValue(cellString);
				excelColumn++;
			}
		}
		fis.close();
		FileOutputStream fileOut = new FileOutputStream(fullFileName);
		xlsWorkBook.write(fileOut);
		fileOut.close();
	}
}
