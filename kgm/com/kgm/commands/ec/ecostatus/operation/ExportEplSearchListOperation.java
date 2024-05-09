package com.kgm.commands.ec.ecostatus.operation;

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

import com.kgm.commands.ec.ecostatus.model.EplSearchData;
import com.kgm.common.WaitProgressBar;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * EPL 검색 결과 리스트 Excel 출력
 * 
 * @author baek
 * 
 */
public class ExportEplSearchListOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private File exportFile = null;
	private static int START_ROW_POS = 3; // Row 시작 위치
	private EplSearchData inputData = null;

	public ExportEplSearchListOperation(EplSearchData inputData, File exportFile) {
		this.inputData = inputData;
		this.exportFile = exportFile;
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			waitProgress.setStatus("Excel Export...");
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

			EventList<EplSearchData> tableDataList = inputData.getTableDataList();

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

				EplSearchData eplSearchData = tableDataList.get(i);

				String functionNo = eplSearchData.getFunctionNo();
				String partName = eplSearchData.getPartName();
				String options = eplSearchData.getOptions();
				String systemNo = eplSearchData.getSystemNo();
				String userName = eplSearchData.getUserName();
				String teamName = eplSearchData.getTeamName();

				Row row = sheet.getRow(i + START_ROW_POS);

				// 순번
				Cell cell = row.getCell(0);
				cell.setCellValue(i + 1);

				// Function No
				cell = row.getCell(1);
				cell.setCellValue(functionNo);

				// Part Name
				cell = row.getCell(2);
				cell.setCellValue(partName);

				// Option
				cell = row.getCell(3);
				cell.setCellValue(options);

				// System No
				cell = row.getCell(4);
				cell.setCellValue(systemNo);

				// 담당자 명
				cell = row.getCell(5);
				cell.setCellValue(userName);

				// 팀명
				cell = row.getCell(6);
				cell.setCellValue(teamName);

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
