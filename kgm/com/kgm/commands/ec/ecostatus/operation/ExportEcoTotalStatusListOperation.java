package com.kgm.commands.ec.ecostatus.operation;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.common.WaitProgressBar;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * 변경현황 리스트 Excel Export
 * 
 * @author baek
 * 
 */
public class ExportEcoTotalStatusListOperation extends AbstractAIFOperation {

	private WaitProgressBar waitProgress;
	private EcoStatusData data = null;
	private File exportFile = null;
	private static int START_ROW_POS = 3; // Row 시작 위치

	public ExportEcoTotalStatusListOperation(EcoStatusData data, File exportFile) {
		this.data = data;
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
			// MessageBox.post(AIFUtility.getActiveDesktop().getShell(), ex.toString(), "Error", MessageBox.ERROR);
			throw ex;
		}

	}

	/**
	 * Excel Export
	 * 
	 * @throws Exception
	 */
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

			EventList<EcoStatusData> tableDataList = data.getSearchChangeStatusList();

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
				EcoStatusData rowData = tableDataList.get(i);
				String status = rowData.getStatus();
				String statgeType = rowData.getStageType();
				String projectId = rowData.getProjectId();
				String ospecId = rowData.getOspecId();
				String changeDesc = rowData.getChangeDesc();
				String estApplyDate = rowData.getEstApplyDate();
				String receiptDate = rowData.getReceiptDate();
				String firstMailSendDate = rowData.getFirstMailSendDate();
				String ecoCompleteReqDate = rowData.getEcoCompleteReqDate();
				String estChangePeriod = rowData.getEstChangePeriod();
				String ecoLastCompleteDate = rowData.getEcoLastCompleteDate();
				String realChangePeriod = rowData.getRealChangePeriod();
				int totalReviewList = rowData.getTotalReviewList();
				int requiredEcoList = rowData.getRequiredEcoList();
				int inCompleteCnt = rowData.getInCompleteCnt();
				int delayCompleteCnt = rowData.getDelayCompleteCnt();
				int missCompleteCnt = rowData.getMissCompleteCnt();
				int inProcessCnt = rowData.getInProcessCnt();
				int delayProcessCnt = rowData.getDelayProcessCnt();
				int missProcess = rowData.getMissProcess();
				String description = rowData.getDescription();
				String registerDate = rowData.getRegisterDate();

				// 누락 지연 사항이 있는지 확인
				boolean isExistWarning = delayCompleteCnt > 0 || missCompleteCnt > 0 || delayProcessCnt > 0 || missProcess > 0;

				Font font = null;

				if (isExistWarning) {
					font = wb.createFont();
					font.setFontHeightInPoints((short) 10);
					font.setFontName("맑은 고딕");
					font.setColor(IndexedColors.RED.index);
				}

				Row row = sheet.getRow(i + START_ROW_POS);
				// 순번
				Cell cell = row.getCell(0);
				cell.setCellValue(i + 1);

				// 상태
				cell = row.getCell(1);
				cell.setCellValue(status);
				if (isExistWarning)
					cell.getCellStyle().setFont(font);

				// 분류
				cell = row.getCell(2);
				cell.setCellValue(statgeType);

				// Project
				cell = row.getCell(3);
				cell.setCellValue(projectId);

				// 구분
				cell = row.getCell(4);
				cell.setCellValue(ospecId);

				// 변경내용
				cell = row.getCell(5);
				cell.setCellValue(changeDesc);

				// 예상적용 시점
				cell = row.getCell(6);
				cell.setCellValue(estApplyDate);

				// O/SPEC 접수일
				cell = row.getCell(7);
				cell.setCellValue(receiptDate);

				// 설계변경 검토요청 메일 발송일
				cell = row.getCell(8);
				cell.setCellValue(firstMailSendDate);

				// ECO 완료 요청일
				cell = row.getCell(9);
				cell.setCellValue(ecoCompleteReqDate);

				// 예상 설계변경 기간
				cell = row.getCell(10);
				cell.setCellValue(estChangePeriod);

				// 최종 ECO 처리일자
				cell = row.getCell(11);
				cell.setCellValue(ecoLastCompleteDate);

				// 실제 설계변경 기간
				cell = row.getCell(12);
				cell.setCellValue(realChangePeriod);

				// 전체 검토 리스트
				cell = row.getCell(13);
				cell.setCellValue(totalReviewList);

				// 필수 설계변경 필요리스트
				cell = row.getCell(14);
				cell.setCellValue(requiredEcoList);

				// 기간내 완료
				cell = row.getCell(15);
				cell.setCellValue(inCompleteCnt);

				// 기간내 누락/오류
				cell = row.getCell(16);
				cell.setCellValue(delayCompleteCnt);
				if (delayCompleteCnt > 0)
					cell.getCellStyle().setFont(font);

				// 기간내 지연완료
				cell = row.getCell(17);
				cell.setCellValue(missCompleteCnt);
				if (missCompleteCnt > 0)
					cell.getCellStyle().setFont(font);

				// 기간내 진행
				cell = row.getCell(18);
				cell.setCellValue(inProcessCnt);

				// 지연 진행
				cell = row.getCell(19);
				cell.setCellValue(delayProcessCnt);
				if (delayProcessCnt > 0)
					cell.getCellStyle().setFont(font);

				// 누락 진행
				cell = row.getCell(20);
				cell.setCellValue(missProcess);
				if (missProcess > 0)
					cell.getCellStyle().setFont(font);

				// 비고
				cell = row.getCell(21);
				cell.setCellValue(description);

				// 등록일
				cell = row.getCell(22);
				cell.setCellValue(registerDate);
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
