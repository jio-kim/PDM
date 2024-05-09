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
 * ������Ȳ ����Ʈ Excel Export
 * 
 * @author baek
 * 
 */
public class ExportEcoTotalStatusListOperation extends AbstractAIFOperation {

	private WaitProgressBar waitProgress;
	private EcoStatusData data = null;
	private File exportFile = null;
	private static int START_ROW_POS = 3; // Row ���� ��ġ

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
				waitProgress.setStatus("�� Error Message : ");
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

			// ù��° Row
			Row firstRow = sheet.getRow(START_ROW_POS);

			int columnCount = firstRow.getPhysicalNumberOfCells();

			int startPos = START_ROW_POS + 1;
			int endPos = tableDataList.size() + START_ROW_POS;
			// ��Row�� ���
			if (startPos == endPos) {
				for (int j = 0; j < columnCount; j++) {
					Cell orgCell = firstRow.getCell(j);
					// ������ Row Bottom Border Medium ����
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
							// ù��° Row Cell Style �� Clone
							CellStyle newCellStyle = wb.createCellStyle();
							newCellStyle.cloneStyleFrom(orgCell.getCellStyle());
							newCell.setCellStyle(newCellStyle);

							// ������ Row Bottom Border Medium ����
							if (i == endPos - 1)
								newCell.getCellStyle().setBorderBottom(CellStyle.BORDER_MEDIUM);
						}
					}
				}
			}

			// ù��° Row Top Border Medium ����
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

				// ���� ���� ������ �ִ��� Ȯ��
				boolean isExistWarning = delayCompleteCnt > 0 || missCompleteCnt > 0 || delayProcessCnt > 0 || missProcess > 0;

				Font font = null;

				if (isExistWarning) {
					font = wb.createFont();
					font.setFontHeightInPoints((short) 10);
					font.setFontName("���� ���");
					font.setColor(IndexedColors.RED.index);
				}

				Row row = sheet.getRow(i + START_ROW_POS);
				// ����
				Cell cell = row.getCell(0);
				cell.setCellValue(i + 1);

				// ����
				cell = row.getCell(1);
				cell.setCellValue(status);
				if (isExistWarning)
					cell.getCellStyle().setFont(font);

				// �з�
				cell = row.getCell(2);
				cell.setCellValue(statgeType);

				// Project
				cell = row.getCell(3);
				cell.setCellValue(projectId);

				// ����
				cell = row.getCell(4);
				cell.setCellValue(ospecId);

				// ���泻��
				cell = row.getCell(5);
				cell.setCellValue(changeDesc);

				// �������� ����
				cell = row.getCell(6);
				cell.setCellValue(estApplyDate);

				// O/SPEC ������
				cell = row.getCell(7);
				cell.setCellValue(receiptDate);

				// ���躯�� �����û ���� �߼���
				cell = row.getCell(8);
				cell.setCellValue(firstMailSendDate);

				// ECO �Ϸ� ��û��
				cell = row.getCell(9);
				cell.setCellValue(ecoCompleteReqDate);

				// ���� ���躯�� �Ⱓ
				cell = row.getCell(10);
				cell.setCellValue(estChangePeriod);

				// ���� ECO ó������
				cell = row.getCell(11);
				cell.setCellValue(ecoLastCompleteDate);

				// ���� ���躯�� �Ⱓ
				cell = row.getCell(12);
				cell.setCellValue(realChangePeriod);

				// ��ü ���� ����Ʈ
				cell = row.getCell(13);
				cell.setCellValue(totalReviewList);

				// �ʼ� ���躯�� �ʿ丮��Ʈ
				cell = row.getCell(14);
				cell.setCellValue(requiredEcoList);

				// �Ⱓ�� �Ϸ�
				cell = row.getCell(15);
				cell.setCellValue(inCompleteCnt);

				// �Ⱓ�� ����/����
				cell = row.getCell(16);
				cell.setCellValue(delayCompleteCnt);
				if (delayCompleteCnt > 0)
					cell.getCellStyle().setFont(font);

				// �Ⱓ�� �����Ϸ�
				cell = row.getCell(17);
				cell.setCellValue(missCompleteCnt);
				if (missCompleteCnt > 0)
					cell.getCellStyle().setFont(font);

				// �Ⱓ�� ����
				cell = row.getCell(18);
				cell.setCellValue(inProcessCnt);

				// ���� ����
				cell = row.getCell(19);
				cell.setCellValue(delayProcessCnt);
				if (delayProcessCnt > 0)
					cell.getCellStyle().setFont(font);

				// ���� ����
				cell = row.getCell(20);
				cell.setCellValue(missProcess);
				if (missProcess > 0)
					cell.getCellStyle().setFont(font);

				// ���
				cell = row.getCell(21);
				cell.setCellValue(description);

				// �����
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
