package com.kgm.commands.ec.ecostatus.operation;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData;
import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * ���躯�� ��Ȳ �� ����Ʈ ���
 * 
 * @author baek
 * 
 */
public class ExportEcoStatusDescReportOperation extends AbstractAIFOperation {

	private WaitProgressBar waitProgress;
	private EcoStatusData inputData = null;
	private File exportFile = null;

	private static final String SPACE = " ";
	private static final int CHG_DESC_START_ROW_IDX = 4; // O/SPEC ���泻�� ù��° Row Index
	private static final int CHG_LIST_START_ROW_IDX = 21; // ���渮��Ʈ Row Index

	private LinkedList<String> changeDesList = null; // O/Spec ���泻�� ����Ʈ
	private LinkedList<EcoChangeData> changeNotSpecList = null; // ����� ������ ����Ʈ
	private LinkedList<EcoChangeData> specArrageList = null; // �������� ����Ʈ

	public ExportEcoStatusDescReportOperation(EcoStatusData inputData, File exportFile) {
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
			EcoStatusData selectedRowData = inputData.getRowDataObj();

			/**
			 * ���躯�� ����Ʈ�� ������
			 */
			getChangeList(selectedRowData.getMasterPuid());

			String fileExtensioin = exportFile.getAbsolutePath().substring(exportFile.getAbsolutePath().lastIndexOf(".") + 1);
			fis = new FileInputStream(exportFile);
			if (fileExtensioin.toLowerCase().equals("xls"))
				wb = new HSSFWorkbook(fis);
			else
				wb = new XSSFWorkbook(fis);

			Sheet sheet = wb.getSheetAt(0);

			// ���� ���� �߰��� �þ Row ��
			int chgDescShftRowCnt = changeDesList.size() == 0 ? changeDesList.size() : changeDesList.size() - 1;

			/**
			 * Ÿ��Ʋ
			 */

			Row titleRow = sheet.getRow(0);
			Cell cell = titleRow.getCell(0);
			String title = "�� " + selectedRowData.getProjectId() + SPACE + selectedRowData.getStageType() + "(" + selectedRowData.getOspecId() + ") : "
					+ selectedRowData.getChangeDesc() + SPACE + "[" + selectedRowData.getStatus() + "]";
			cell.setCellValue(title);

			SimpleDateFormat printSd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date toDay = new Date();
			String printDate = " [���: " + printSd.format(toDay) + "]";

			Row row = sheet.getRow(1);
			row.getCell(9).setCellValue(printDate);

			/**
			 * 1) O/Spec ���泻�� ����Ʈ
			 */
			if (chgDescShftRowCnt > 0)
				sheet.shiftRows(CHG_DESC_START_ROW_IDX + 1, sheet.getLastRowNum(), chgDescShftRowCnt, true, false);// Row �� Insert ��

			int changeDescIndex = 0;
			Cell changeDescFirstCell = null;
			for (int i = CHG_DESC_START_ROW_IDX; i < CHG_DESC_START_ROW_IDX + changeDesList.size(); i++) {
				Row changeDesRow = sheet.getRow(i);
				Cell changeDesCell = null;
				if (changeDesRow != null) {
					changeDesCell = changeDesRow.getCell(0);
					changeDesCell.setCellValue(selectedRowData.getOspecId() + " : " + changeDesList.get(changeDescIndex));
					changeDescFirstCell = changeDesCell;
				} else {
					changeDesRow = sheet.createRow(i);
					changeDesCell = changeDesRow.createCell(0);
					CellStyle newCellStyle = wb.createCellStyle();
					newCellStyle.cloneStyleFrom(changeDescFirstCell.getCellStyle());
					changeDesCell.setCellStyle(newCellStyle);
					changeDesCell.setCellValue(selectedRowData.getOspecId() + " : " + changeDesList.get(changeDescIndex));
				}
				changeDescIndex++;
			}

			/**
			 * 2) ���躯�� ��Ȳ�м�
			 */
			row = sheet.getRow(7 + chgDescShftRowCnt);
			cell = row.getCell(0);

			String printEstChangePeriod = "".equals(selectedRowData.getEstChangePeriod()) ? "" : " (" + selectedRowData.getEstChangePeriod() + ")";
			String planedChgPeriod = "��ȹ ���躯�� �Ⱓ : " + selectedRowData.getReceiptDate() + " ~ " + selectedRowData.getEcoCompleteReqDate()
					+ printEstChangePeriod;

			cell.setCellValue(planedChgPeriod);
			row = sheet.getRow(8 + chgDescShftRowCnt);
			cell = row.getCell(0);

			// ���� ���躯�� �Ⱓ
			// String printLastFirstPeriod = "".equals(selectedRowData.getLastFirstPeriod()) ? "" : " (" + selectedRowData.getLastFirstPeriod() + ")";
			String printRealChangePeriod = "".equals(selectedRowData.getRealChangePeriod()) ? "" : " (" + selectedRowData.getRealChangePeriod() + ")";

			String lastChangePeriod = "���� ���躯�� �Ⱓ : ";
			// �Ϸ��̸�
			// if (selectedRowData.getStatus().equals("�Ϸ�"))
			// lastChangePeriod += selectedRowData.getEcoFirstCompleteDate() + ("".equals(selectedRowData.getEcoFirstCompleteDate()) ? "" : " ~ ")
			// + selectedRowData.getEcoLastCompleteDate() + printRealChangePeriod;
			// else
			// lastChangePeriod += selectedRowData.getEcoFirstCompleteDate() + ("".equals(selectedRowData.getEcoFirstCompleteDate()) ? "" : " ~ ");
			if (selectedRowData.getStatus().equals("�Ϸ�"))
				lastChangePeriod += selectedRowData.getReceiptDate() + ("".equals(selectedRowData.getReceiptDate()) ? "" : " ~ ")
						+ selectedRowData.getEcoLastCompleteDate() + printRealChangePeriod;
			else
				lastChangePeriod += selectedRowData.getReceiptDate() + ("".equals(selectedRowData.getReceiptDate()) ? "" : " ~ ");

			cell.setCellValue(lastChangePeriod);

			row = sheet.getRow(9 + chgDescShftRowCnt);
			cell = row.getCell(0);
			cell.setCellValue("��ü ���丮��Ʈ: " + selectedRowData.getTotalReviewList() + "��");

			row = sheet.getRow(10 + chgDescShftRowCnt);
			cell = row.getCell(0);
			cell.setCellValue("�ʼ� ���躯�� �ʿ丮��Ʈ: " + selectedRowData.getRequiredEcoList() + "��");

			Font tableRedBoldFont = createTableFont(wb, true, true); // ���̺� Red, Bold ��Ʈ
			Font tableRedNormalFont = createTableFont(wb, true, false);// ���̺� Red, not Bold ��Ʈ

			row = sheet.getRow(12 + chgDescShftRowCnt);
			cell = row.getCell(8);
			cell.setCellValue("ECO ����Ϸ� ��û����: " + selectedRowData.getEcoCompleteReqDate().replace("-", "."));

			/**
			 * Table Count
			 */
			row = sheet.getRow(15 + chgDescShftRowCnt);
			// �Ⱓ�� �Ϸ�
			cell = row.getCell(2);
			cell.setCellValue(selectedRowData.getInCompleteCnt());
			// �����Ϸ�
			cell = row.getCell(3);
			cell.setCellValue(selectedRowData.getDelayCompleteCnt());
			if (selectedRowData.getDelayCompleteCnt() > 0)
				cell.getCellStyle().setFont(tableRedBoldFont);

			// ����/���� �Ϸ�
			cell = row.getCell(4);
			cell.setCellValue(selectedRowData.getMissCompleteCnt());
			if (selectedRowData.getMissCompleteCnt() > 0)
				cell.getCellStyle().setFont(tableRedBoldFont);

			// �Ⱓ�� ����
			cell = row.getCell(5);
			cell.setCellValue(selectedRowData.getInProcessCnt());
			// ��������
			cell = row.getCell(6);
			cell.setCellValue(selectedRowData.getDelayProcessCnt());
			if (selectedRowData.getDelayProcessCnt() > 0)
				cell.getCellStyle().setFont(tableRedBoldFont);

			// ����/���� ����
			cell = row.getCell(7);
			cell.setCellValue(selectedRowData.getMissProcess());
			if (selectedRowData.getMissProcess() > 0)
				createTableNewCellStyle(wb, cell, tableRedBoldFont, false);

			// ������
			cell = row.getCell(8);
			cell.setCellValue(selectedRowData.getSpecArrange());
			/**
			 * Table Percent
			 */
			row = sheet.getRow(16 + chgDescShftRowCnt);
			// �����Ϸ�
			cell = row.getCell(3);
			if (selectedRowData.getDelayCompleteCnt() > 0)
				createTableNewCellStyle(wb, cell, tableRedNormalFont, true);

			// ����/���� �Ϸ�
			cell = row.getCell(4);
			if (selectedRowData.getMissCompleteCnt() > 0)
				createTableNewCellStyle(wb, cell, tableRedNormalFont, true);

			// ��������
			cell = row.getCell(6);
			if (selectedRowData.getDelayProcessCnt() > 0)
				createTableNewCellStyle(wb, cell, tableRedNormalFont, true);

			// ����/���� ����
			cell = row.getCell(7);
			if (selectedRowData.getMissProcess() > 0)
				createTableNewCellStyle(wb, cell, tableRedNormalFont, true);

			// ǥ�� Font ����
			// Font stdFont = createStdFont(wb);
			/**
			 * 3) ���� ����Ʈ ���
			 */
			printChangeList(wb, chgDescShftRowCnt + CHG_LIST_START_ROW_IDX);
			// printChangeList(wb, chgDescShftRowCnt + CHG_LIST_START_ROW_IDX, false);
			/**
			 * ������ ����Ʈ ���
			 */
			// printChangeList(wb, chgDescShftRowCnt + CHG_LIST_START_ROW_IDX + changeNotSpecList.size(), true);

			fis.close();
			fis = null;
			// wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
			wb.setForceFormulaRecalculation(true);

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

	/**
	 * ���渮��Ʈ ���
	 * 
	 * @param wb
	 * @param startRowIndex
	 *            ���� ����Ʈ ù��° Row
	 * @param stdFont
	 *            ���� ����Ʈ Font
	 * @param isSpecList
	 *            ������ ����
	 * @throws Exception
	 */
	public void printChangeList(Workbook wb, int startRowIndex, boolean isSpecList) throws Exception {

		LinkedList<EcoChangeData> changeList = null;

		Sheet sheet = wb.getSheetAt(0);

		if (isSpecList)
			changeList = specArrageList;
		else
			changeList = changeNotSpecList;

		SimpleDateFormat ecoDateSd = new SimpleDateFormat("yyyy-MM-DD");
		for (int i = 0; i < changeList.size(); i++) {
			EcoChangeData data = changeList.get(i);

			String changeStatus = data.getChangeStatus();
			String functionNo = data.getFunctionNo();
			String partName = data.getPartName();
			String teamName = data.getTeamName() != null ? data.getTeamName() : "";
			String userId = data.getUserId() != null ? data.getUserId().split("\\(")[0] : "";
			String ecoNo = data.getEcoNo();
			String ecoCompleteDateStr = data.getEcoCompleteDate() != null ? data.getEcoCompleteDate() : "";
			String description = data.getDescription() != null ? data.getDescription() : "";
			String ecoCompleteReqDateStr = inputData.getEcoCompleteReqDate() != null ? inputData.getEcoCompleteReqDate() : "";
			String refDesc = "";
			// ���� �Ϸ�, ������� �ϰ�� ���� ��¥�� �ٿ��ش�.
			if (changeStatus.equals("���� �Ϸ�") || changeStatus.equals("�������")) {
				if (!ecoCompleteDateStr.isEmpty() && !ecoCompleteReqDateStr.isEmpty()) {
					Date ecoCompleteDate = ecoDateSd.parse(ecoCompleteDateStr);
					Date ecoCompleteReqDate = ecoDateSd.parse(ecoCompleteReqDateStr);
					// ECO�Ϸ��û���� ECO�Ϸ��� �����̸�
					if (ecoCompleteReqDate.after(ecoCompleteDate)) {
						long refTime = ecoCompleteReqDate.getTime() - ecoCompleteDate.getTime();
						refDesc = "+" + Long.toString(refTime) + "day";
					}
				}
			}

			String printStatus = "";
			if (changeStatus.equals("�Ⱓ�� �Ϸ�"))
				printStatus = "�Ϸ�";
			else if (changeStatus.equals("�Ⱓ�� ����"))
				printStatus = "����";
			else if (changeStatus.startsWith("����"))
				printStatus = "����";
			else if (changeStatus.startsWith("����"))
				printStatus = "����";
			else if (changeStatus.startsWith("�������"))
				printStatus = "�������";
			// Contents ����
			String contents = ecoNo != null ? ecoNo + "(" + ecoCompleteDateStr + ") " + refDesc : description + refDesc;
			// �߰��Ǵ� Row ��
			String changeRowValue = printStatus + " - " + functionNo + " - " + partName + " - " + teamName + " - " + userId
					+ (contents.equals("") ? "" : " : " + contents);

			boolean isWarningData = printStatus.equals("����") || printStatus.equals("����") ? true : false;

			Font stdFont = createStdFont(wb, isWarningData);

			Row row = sheet.createRow(startRowIndex + i);
			Cell cell = row.createCell(0);

			// Cell Style ����
			CellStyle newCellStyle = wb.createCellStyle();
			newCellStyle.setAlignment(CellStyle.ALIGN_LEFT);
			newCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			newCellStyle.setIndention((short) 2);
			cell.setCellStyle(newCellStyle);
			newCellStyle.setFont(stdFont);
			cell.setCellValue(changeRowValue);
		}

	}

	/**
	 * ���渮��Ʈ ���
	 * 
	 * @param wb
	 * @param startRowIndex
	 * @throws Exception
	 */
	private void printChangeList(Workbook wb, int startRowIndex) throws Exception {
		Sheet sheet = wb.getSheetAt(0);
		Row firstRow = sheet.getRow(startRowIndex);

		LinkedList<EcoChangeData> changeList = changeNotSpecList;

		int columnCount = firstRow.getPhysicalNumberOfCells();
		// int columnCount = 12;
		int cloneStartIndex = startRowIndex + 1; // ù��° Row Cell Style �� �����ؼ� ����� ���� Row ���� Index
		int endPos = changeNotSpecList.size() + startRowIndex;

		// ù��° Row�� �����ؼ� Cell�� ������
		for (int i = cloneStartIndex; i < endPos; i++) {
			Row newRow = sheet.createRow(i);

			for (int j = 0; j < columnCount; j++) {
				Cell orgCell = firstRow.getCell(j);
				Cell newCell = newRow.createCell(j);
				if (orgCell != null) {
					// ù��° Row Cell Style �� Clone
					CellStyle newCellStyle = wb.createCellStyle();
					newCellStyle.cloneStyleFrom(orgCell.getCellStyle());
					newCell.setCellStyle(newCellStyle);
					if (j == 2 || j == 4) {
						sheet.addMergedRegion(new CellRangeAddress(i, i, j, j + 1));
					}
				}
			}
		}
		HashMap<Integer, ArrayList<EcoChangeData>> changeListMap = new HashMap<Integer, ArrayList<EcoChangeData>>();
		for (EcoChangeData data : changeList) {
			ArrayList<EcoChangeData> dataList = null;
			String changeStatus = data.getChangeStatus();
			int statusLevel = getStatusSortLevel(changeStatus);

			if (changeListMap.get(statusLevel) == null) {
				dataList = new ArrayList<EcoChangeData>();
				dataList.add(data);
				changeListMap.put(statusLevel, dataList);
			} else {
				dataList = changeListMap.get(statusLevel);
				dataList.add(data);
			}

		}

		/**
		 * �����͸� �����
		 */
		Map<Integer, ArrayList<EcoChangeData>> changePrintListMap = new TreeMap<Integer, ArrayList<EcoChangeData>>(changeListMap);
		int index = 0;
		for (int level : changePrintListMap.keySet()) {
			ArrayList<EcoChangeData> dataList = changePrintListMap.get(level);
			for (EcoChangeData data : dataList) {
				String changeStatus = data.getChangeStatus();
				String functionNo = data.getFunctionNo();
				String partName = data.getPartName();
				String reviewContents = data.getReviewContents();
				String userId = data.getUserId() != null ? data.getUserId().split("\\(")[0] : "";
				String ecoNo = data.getEcoNo();
				String ecoCompleteDateStr = data.getEcoCompleteDate() != null ? data.getEcoCompleteDate() : "";
				String desctiption = data.getDescription() != null ? data.getDescription() : "";

				boolean isWarningData = changeStatus.indexOf("����") >= 0 || changeStatus.indexOf("����") >= 0 ? true : false;

				Row row = sheet.getRow(index + startRowIndex);
				// ù��° ��� ���� ���� ��� �ٹٲ��� �ǵ�����
				if (index == 0 && !"".equals(desctiption)) {
					firstRow.setHeight((short) -1);
					Cell desc = firstRow.getCell(9);
					desc.getCellStyle().setWrapText(true);
				}

				// ����
				Cell cell = row.getCell(0);
				cell.setCellValue(changeStatus);

				if (isWarningData) {
					Font redFont = null;
					redFont = wb.createFont();
					redFont.setFontHeightInPoints((short) 8);
					redFont.setFontName("���� ���");
					redFont.setColor(IndexedColors.RED.index);
					cell.getCellStyle().setFont(redFont);
				}

				// Function
				cell = row.getCell(1);
				cell.setCellValue(functionNo);

				// Part Name
				cell = row.getCell(2);
				cell.setCellValue(partName);

				// ������䳻��
				cell = row.getCell(4);
				cell.setCellValue(reviewContents);

				// �����
				cell = row.getCell(6);
				cell.setCellValue(userId);

				// ECO
				cell = row.getCell(7);
				cell.setCellValue(ecoNo);

				// ECO Date
				cell = row.getCell(8);
				cell.setCellValue(ecoCompleteDateStr);

				// ���
				cell = row.getCell(9);
				cell.setCellValue(desctiption);

				index++;

			}

		}

	}

	/**
	 * ���� ���� ����Ʈ ������
	 * 
	 * @param masterPuid
	 * @throws Exception
	 */
	private void getChangeList(String masterPuid) throws Exception {
		changeDesList = new LinkedList<String>();
		changeNotSpecList = new LinkedList<EcoChangeData>();
		specArrageList = new LinkedList<EcoChangeData>();

		CustomECODao dao = new CustomECODao();
		ArrayList<String> masterPuidList = new ArrayList<String>(Arrays.asList(masterPuid));
		DataSet ds = new DataSet();
		ds.put("MASTER_PUID", masterPuidList);

		ArrayList<HashMap<String, String>> changList = dao.getEcoStatusChangeList(ds);

		for (HashMap<String, String> changeRowMap : changList) {
			String ecoPublish = changeRowMap.get("ECO_PUBLISH");
			String changeStatus = changeRowMap.get("STATUS");
			String functionNo = changeRowMap.get("FUNCTION_ID");
			String partName = changeRowMap.get("PART_NAME");
			String reviewContents = changeRowMap.get("REVIEW_CONTENTS");
			String userId = changeRowMap.get("USER_NAME");
			String teamName = changeRowMap.get("TEAM_NAME");
			String ecoNo = changeRowMap.get("ECO_NO");
			String ecoCompleteDate = changeRowMap.get("ECO_COMPLETE_DATE");
			String description = changeRowMap.get("DESCRIPTION");
			String creationDate = changeRowMap.get("CREATE_DATE");

			String adminDesc = changeRowMap.get("ADMIN_DESC");
			description = description == null && adminDesc != null ? adminDesc : description;

			EcoChangeData rowData = new EcoChangeData();
			// �ʿ��׸��� �͸� �ش�
			rowData.setEcoPublish(ecoPublish);
			rowData.setChangeStatus(changeStatus);
			rowData.setFunctionNo(functionNo);
			rowData.setPartName(partName);
			rowData.setReviewContents(reviewContents);
			rowData.setUserId(userId);
			rowData.setTeamName(teamName);
			rowData.setEcoNo(ecoNo);
			rowData.setEcoCompleteDate(ecoCompleteDate);
			rowData.setDescription(description);
			rowData.setCreationDate(creationDate);

			/**
			 * ���泻��(���䳻��) ����Ʈ ����
			 */
			if (!changeDesList.contains(reviewContents))
				changeDesList.add(reviewContents);

			/**
			 * ���� ���渮��Ʈ ����
			 */
			// ������� �̿��� �ʿ��� ��� ����Ʈ
			if (ecoPublish.equals("�ʿ�")) {
				changeNotSpecList.add(rowData);
				// ��������� ���
			} else if (ecoPublish.equals("�������")) {
				specArrageList.add(rowData);
			}

		}
	}

	/**
	 * ǥ�� ��Ʈ ����
	 * 
	 * @param wb
	 * @return
	 * @throws Exception
	 */
	private Font createStdFont(Workbook wb, boolean isRedColor) throws Exception {
		// Font ����
		Font stdFont = null;
		stdFont = wb.createFont();
		stdFont.setFontHeightInPoints((short) 10);
		stdFont.setFontName("���� ���");
		if (isRedColor)
			stdFont.setColor(IndexedColors.RED.index);
		return stdFont;
	}

	/**
	 * Table Font ����
	 * 
	 * @param wb
	 * @param isRedColor
	 * @param isBold
	 * @return
	 * @throws Exception
	 */
	private Font createTableFont(Workbook wb, boolean isRedColor, boolean isBold) throws Exception {
		// Font ����
		Font font = null;
		font = wb.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("���� ���");
		if (isRedColor)
			font.setColor(IndexedColors.RED.index);
		if (isBold)
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		return font;
	}

	/**
	 * Table Cell Style ����
	 * 
	 * @param wb
	 * @param cell
	 * @param font
	 * @throws Exception
	 */
	private void createTableNewCellStyle(Workbook wb, Cell cell, Font font, boolean isPercentFormat) throws Exception {
		// Cell Style ����
		CellStyle newCellStyle = wb.createCellStyle();
		DataFormat format = wb.createDataFormat();

		newCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		newCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		newCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		newCellStyle.setBorderRight(CellStyle.BORDER_THIN);
		if (isPercentFormat)
			newCellStyle.setDataFormat(format.getFormat("0%"));
		cell.setCellStyle(newCellStyle);
		newCellStyle.setFont(font);
	}

	/**
	 * ���º� Sort ����
	 * 
	 * @param status
	 * @return
	 */
	private int getStatusSortLevel(String status) {
		if ("����/���� ������".equals(status))
			return 1;
		else if ("���� ������".equals(status))
			return 2;
		else if ("�Ⱓ�� ������".equals(status))
			return 3;
		else if ("����/���� �Ϸ�".equals(status))
			return 4;
		else if ("���� �Ϸ�".equals(status))
			return 5;
		else if ("�Ⱓ�� �Ϸ�".equals(status))
			return 6;
		else
			return 7;
	}
}
