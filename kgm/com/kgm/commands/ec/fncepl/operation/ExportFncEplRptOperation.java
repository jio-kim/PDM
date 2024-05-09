package com.kgm.commands.ec.fncepl.operation;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.fncepl.model.FncEplCheckData;
import com.kgm.common.WaitProgressBar;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

public class ExportFncEplRptOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private FncEplCheckData inputData = null;
	private File exportFile = null;
	private static int START_ROW_POS = 3; // Row 시작 위치

	public ExportFncEplRptOperation(FncEplCheckData inputData, File exportFile) {
		this.inputData = inputData;
		this.exportFile = exportFile;
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			waitProgress.setStatus("Searching ...");
			executeExport();
			waitProgress.setStatus("Complete");
			waitProgress.close();
		} catch (Exception ex) {

			if (waitProgress != null) {
				waitProgress.setStatus("＠ Error Message : ");
				waitProgress.setStatus(ex.toString());
				waitProgress.close("Error", false);
			}
			setAbortRequested(true);
			ex.printStackTrace();
			throw ex;
		}

	}

	private void executeExport() throws Exception {
		Workbook wb = null;
		FileInputStream fis = null;
		try {
			String fileExtensioin = exportFile.getAbsolutePath().substring(exportFile.getAbsolutePath().lastIndexOf(".") + 1);
			fis = new FileInputStream(exportFile);
			if (fileExtensioin.toLowerCase().equals("xls"))
				wb = new HSSFWorkbook(fis);
			else
				wb = new XSSFWorkbook(fis);

			Sheet sheet = wb.getSheetAt(0);

			EventList<FncEplCheckData> tableDataList = inputData.getTableDataList();

			// 첫번째 Row
			Row firstRow = sheet.getRow(START_ROW_POS);

			int columnCount = firstRow.getPhysicalNumberOfCells();

			int startPos = START_ROW_POS + 1;
			int endPos = tableDataList.size() + START_ROW_POS;
			// 한Row일 경우
			if (startPos == endPos) {
				for (int j = 0; j < columnCount; j++) {
					Cell orgCell = firstRow.getCell(j);
					// 마지막 Row Bottom Border Medium 설정
					if (orgCell != null)
						orgCell.getCellStyle().setBorderBottom(CellStyle.BORDER_MEDIUM);
				}
			} else {
				for (int i = startPos; i < endPos; i++) {
					Row newRow = sheet.createRow(i);
					for (int j = 0; j < columnCount; j++) {
						Cell orgCell = firstRow.getCell(j);
						Cell newCell = newRow.createCell(j);
						if (orgCell != null) {
							// 첫번째 Row Cell Style 로 Clone
							CellStyle newCellStyle = wb.createCellStyle();
							newCellStyle.cloneStyleFrom(orgCell.getCellStyle());
							newCell.setCellStyle(newCellStyle);

							// 마지막 Row Bottom Border Medium 설정
							if (i == endPos - 1)
								newCell.getCellStyle().setBorderBottom(CellStyle.BORDER_MEDIUM);
						}
					}
				}
			}

			// 첫번째 Row Top Border Medium 설정
			for (int i = 0; i < columnCount; i++) {
				Cell orgCell = firstRow.getCell(i);
				orgCell.getCellStyle().setBorderTop(CellStyle.BORDER_MEDIUM);
			}

			for (int i = 0; i < tableDataList.size(); i++) {

				FncEplCheckData rowData = tableDataList.get(i);

				String isLatestCheck = rowData.getIsLatestCheck();
				String functionNo = rowData.getFunctionNo();
				String baseDate = rowData.getBaseDate();
				String applyEcoNo = rowData.getApplyEcoNo();
				String addEcoPublish = rowData.getAddEcoPublish();				
				String description = rowData.getDescription();				
				String createDate = rowData.getCreateDate();

				Row row = sheet.getRow(i + START_ROW_POS);

				// 순번
				Cell cell = row.getCell(0);
				cell.setCellValue(i + 1);

				// Latest
				cell = row.getCell(1);
				cell.setCellValue(isLatestCheck);

				// Function
				cell = row.getCell(2);
				cell.setCellValue(functionNo);

				// 기준 Date
				cell = row.getCell(3);
				cell.setCellValue(baseDate);

				// 반영 ECO
				cell = row.getCell(4);
				cell.setCellValue(applyEcoNo);

				// 추가ECO 발행
				cell = row.getCell(5);
				cell.setCellValue(addEcoPublish);

				// 비고
				cell = row.getCell(6);
				cell.setCellValue(description);
				
				// 등록일
				cell = row.getCell(7);
				cell.setCellValue(createDate);

			}

			fis.close();
			fis = null;

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
			throw ex;
		}
	}

}
