package com.ssangyong.commands.ec.ecostatus.operation;

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

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.ecostatus.model.EcoChangeData;
import com.ssangyong.commands.ec.ecostatus.model.EcoStatusData;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * 설계변경 현황 상세 리포트 출력
 * 
 * @author baek
 * 
 */
public class ExportEcoStatusDescReportOperation extends AbstractAIFOperation {

	private WaitProgressBar waitProgress;
	private EcoStatusData inputData = null;
	private File exportFile = null;

	private static final String SPACE = " ";
	private static final int CHG_DESC_START_ROW_IDX = 4; // O/SPEC 변경내용 첫번째 Row Index
	private static final int CHG_LIST_START_ROW_IDX = 21; // 변경리스트 Row Index

	private LinkedList<String> changeDesList = null; // O/Spec 변경내용 리스트
	private LinkedList<EcoChangeData> changeNotSpecList = null; // 사양을 제외한 리스트
	private LinkedList<EcoChangeData> specArrageList = null; // 사양관리인 리스트

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
			EcoStatusData selectedRowData = inputData.getRowDataObj();

			/**
			 * 설계변경 리스트를 가져옴
			 */
			getChangeList(selectedRowData.getMasterPuid());

			String fileExtensioin = exportFile.getAbsolutePath().substring(exportFile.getAbsolutePath().lastIndexOf(".") + 1);
			fis = new FileInputStream(exportFile);
			if (fileExtensioin.toLowerCase().equals("xls"))
				wb = new HSSFWorkbook(fis);
			else
				wb = new XSSFWorkbook(fis);

			Sheet sheet = wb.getSheetAt(0);

			// 변경 내용 추가로 늘어난 Row 수
			int chgDescShftRowCnt = changeDesList.size() == 0 ? changeDesList.size() : changeDesList.size() - 1;

			/**
			 * 타이틀
			 */

			Row titleRow = sheet.getRow(0);
			Cell cell = titleRow.getCell(0);
			String title = "■ " + selectedRowData.getProjectId() + SPACE + selectedRowData.getStageType() + "(" + selectedRowData.getOspecId() + ") : "
					+ selectedRowData.getChangeDesc() + SPACE + "[" + selectedRowData.getStatus() + "]";
			cell.setCellValue(title);

			SimpleDateFormat printSd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date toDay = new Date();
			String printDate = " [출력: " + printSd.format(toDay) + "]";

			Row row = sheet.getRow(1);
			row.getCell(9).setCellValue(printDate);

			/**
			 * 1) O/Spec 변경내용 리스트
			 */
			if (chgDescShftRowCnt > 0)
				sheet.shiftRows(CHG_DESC_START_ROW_IDX + 1, sheet.getLastRowNum(), chgDescShftRowCnt, true, false);// Row 를 Insert 함

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
			 * 2) 설계변경 현황분석
			 */
			row = sheet.getRow(7 + chgDescShftRowCnt);
			cell = row.getCell(0);

			String printEstChangePeriod = "".equals(selectedRowData.getEstChangePeriod()) ? "" : " (" + selectedRowData.getEstChangePeriod() + ")";
			String planedChgPeriod = "계획 설계변경 기간 : " + selectedRowData.getReceiptDate() + " ~ " + selectedRowData.getEcoCompleteReqDate()
					+ printEstChangePeriod;

			cell.setCellValue(planedChgPeriod);
			row = sheet.getRow(8 + chgDescShftRowCnt);
			cell = row.getCell(0);

			// 실제 설계변경 기간
			// String printLastFirstPeriod = "".equals(selectedRowData.getLastFirstPeriod()) ? "" : " (" + selectedRowData.getLastFirstPeriod() + ")";
			String printRealChangePeriod = "".equals(selectedRowData.getRealChangePeriod()) ? "" : " (" + selectedRowData.getRealChangePeriod() + ")";

			String lastChangePeriod = "최종 설계변경 기간 : ";
			// 완료이면
			// if (selectedRowData.getStatus().equals("완료"))
			// lastChangePeriod += selectedRowData.getEcoFirstCompleteDate() + ("".equals(selectedRowData.getEcoFirstCompleteDate()) ? "" : " ~ ")
			// + selectedRowData.getEcoLastCompleteDate() + printRealChangePeriod;
			// else
			// lastChangePeriod += selectedRowData.getEcoFirstCompleteDate() + ("".equals(selectedRowData.getEcoFirstCompleteDate()) ? "" : " ~ ");
			if (selectedRowData.getStatus().equals("완료"))
				lastChangePeriod += selectedRowData.getReceiptDate() + ("".equals(selectedRowData.getReceiptDate()) ? "" : " ~ ")
						+ selectedRowData.getEcoLastCompleteDate() + printRealChangePeriod;
			else
				lastChangePeriod += selectedRowData.getReceiptDate() + ("".equals(selectedRowData.getReceiptDate()) ? "" : " ~ ");

			cell.setCellValue(lastChangePeriod);

			row = sheet.getRow(9 + chgDescShftRowCnt);
			cell = row.getCell(0);
			cell.setCellValue("전체 검토리스트: " + selectedRowData.getTotalReviewList() + "건");

			row = sheet.getRow(10 + chgDescShftRowCnt);
			cell = row.getCell(0);
			cell.setCellValue("필수 설계변경 필요리스트: " + selectedRowData.getRequiredEcoList() + "건");

			Font tableRedBoldFont = createTableFont(wb, true, true); // 테이블 Red, Bold 폰트
			Font tableRedNormalFont = createTableFont(wb, true, false);// 테이블 Red, not Bold 폰트

			row = sheet.getRow(12 + chgDescShftRowCnt);
			cell = row.getCell(8);
			cell.setCellValue("ECO 발행완료 요청일자: " + selectedRowData.getEcoCompleteReqDate().replace("-", "."));

			/**
			 * Table Count
			 */
			row = sheet.getRow(15 + chgDescShftRowCnt);
			// 기간내 완료
			cell = row.getCell(2);
			cell.setCellValue(selectedRowData.getInCompleteCnt());
			// 지연완료
			cell = row.getCell(3);
			cell.setCellValue(selectedRowData.getDelayCompleteCnt());
			if (selectedRowData.getDelayCompleteCnt() > 0)
				cell.getCellStyle().setFont(tableRedBoldFont);

			// 누락/오류 완료
			cell = row.getCell(4);
			cell.setCellValue(selectedRowData.getMissCompleteCnt());
			if (selectedRowData.getMissCompleteCnt() > 0)
				cell.getCellStyle().setFont(tableRedBoldFont);

			// 기간내 진행
			cell = row.getCell(5);
			cell.setCellValue(selectedRowData.getInProcessCnt());
			// 지연진행
			cell = row.getCell(6);
			cell.setCellValue(selectedRowData.getDelayProcessCnt());
			if (selectedRowData.getDelayProcessCnt() > 0)
				cell.getCellStyle().setFont(tableRedBoldFont);

			// 누락/오류 진행
			cell = row.getCell(7);
			cell.setCellValue(selectedRowData.getMissProcess());
			if (selectedRowData.getMissProcess() > 0)
				createTableNewCellStyle(wb, cell, tableRedBoldFont, false);

			// 사양관리
			cell = row.getCell(8);
			cell.setCellValue(selectedRowData.getSpecArrange());
			/**
			 * Table Percent
			 */
			row = sheet.getRow(16 + chgDescShftRowCnt);
			// 지연완료
			cell = row.getCell(3);
			if (selectedRowData.getDelayCompleteCnt() > 0)
				createTableNewCellStyle(wb, cell, tableRedNormalFont, true);

			// 누락/오류 완료
			cell = row.getCell(4);
			if (selectedRowData.getMissCompleteCnt() > 0)
				createTableNewCellStyle(wb, cell, tableRedNormalFont, true);

			// 지연진행
			cell = row.getCell(6);
			if (selectedRowData.getDelayProcessCnt() > 0)
				createTableNewCellStyle(wb, cell, tableRedNormalFont, true);

			// 누락/오류 진행
			cell = row.getCell(7);
			if (selectedRowData.getMissProcess() > 0)
				createTableNewCellStyle(wb, cell, tableRedNormalFont, true);

			// 표준 Font 정의
			// Font stdFont = createStdFont(wb);
			/**
			 * 3) 변경 리스트 출력
			 */
			printChangeList(wb, chgDescShftRowCnt + CHG_LIST_START_ROW_IDX);
			// printChangeList(wb, chgDescShftRowCnt + CHG_LIST_START_ROW_IDX, false);
			/**
			 * 사양관리 리스트 출력
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
	 * 변경리스트 출력
	 * 
	 * @param wb
	 * @param startRowIndex
	 *            변경 리스트 첫번째 Row
	 * @param stdFont
	 *            변경 리스트 Font
	 * @param isSpecList
	 *            사양관리 유무
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
			// 지연 완료, 사양정리 일경우 지연 날짜를 붙여준다.
			if (changeStatus.equals("지연 완료") || changeStatus.equals("사양정리")) {
				if (!ecoCompleteDateStr.isEmpty() && !ecoCompleteReqDateStr.isEmpty()) {
					Date ecoCompleteDate = ecoDateSd.parse(ecoCompleteDateStr);
					Date ecoCompleteReqDate = ecoDateSd.parse(ecoCompleteReqDateStr);
					// ECO완료요청일이 ECO완료일 이후이면
					if (ecoCompleteReqDate.after(ecoCompleteDate)) {
						long refTime = ecoCompleteReqDate.getTime() - ecoCompleteDate.getTime();
						refDesc = "+" + Long.toString(refTime) + "day";
					}
				}
			}

			String printStatus = "";
			if (changeStatus.equals("기간내 완료"))
				printStatus = "완료";
			else if (changeStatus.equals("기간내 진행"))
				printStatus = "진행";
			else if (changeStatus.startsWith("지연"))
				printStatus = "지연";
			else if (changeStatus.startsWith("누락"))
				printStatus = "누락";
			else if (changeStatus.startsWith("사양정리"))
				printStatus = "사양정리";
			// Contents 내용
			String contents = ecoNo != null ? ecoNo + "(" + ecoCompleteDateStr + ") " + refDesc : description + refDesc;
			// 추가되는 Row 값
			String changeRowValue = printStatus + " - " + functionNo + " - " + partName + " - " + teamName + " - " + userId
					+ (contents.equals("") ? "" : " : " + contents);

			boolean isWarningData = printStatus.equals("지연") || printStatus.equals("누락") ? true : false;

			Font stdFont = createStdFont(wb, isWarningData);

			Row row = sheet.createRow(startRowIndex + i);
			Cell cell = row.createCell(0);

			// Cell Style 적용
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
	 * 변경리스트 출력
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
		int cloneStartIndex = startRowIndex + 1; // 첫번째 Row Cell Style 을 복제해서 만들어 지는 Row 시작 Index
		int endPos = changeNotSpecList.size() + startRowIndex;

		// 첫번째 Row를 복제해서 Cell을 생성함
		for (int i = cloneStartIndex; i < endPos; i++) {
			Row newRow = sheet.createRow(i);

			for (int j = 0; j < columnCount; j++) {
				Cell orgCell = firstRow.getCell(j);
				Cell newCell = newRow.createCell(j);
				if (orgCell != null) {
					// 첫번째 Row Cell Style 로 Clone
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
		 * 데이터를 기록함
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

				boolean isWarningData = changeStatus.indexOf("지연") >= 0 || changeStatus.indexOf("누락") >= 0 ? true : false;

				Row row = sheet.getRow(index + startRowIndex);
				// 첫번째 비고에 값이 있을 경우 줄바꿈이 되도록함
				if (index == 0 && !"".equals(desctiption)) {
					firstRow.setHeight((short) -1);
					Cell desc = firstRow.getCell(9);
					desc.getCellStyle().setWrapText(true);
				}

				// 상태
				Cell cell = row.getCell(0);
				cell.setCellValue(changeStatus);

				if (isWarningData) {
					Font redFont = null;
					redFont = wb.createFont();
					redFont.setFontHeightInPoints((short) 8);
					redFont.setFontName("맑은 고딕");
					redFont.setColor(IndexedColors.RED.index);
					cell.getCellStyle().setFont(redFont);
				}

				// Function
				cell = row.getCell(1);
				cell.setCellValue(functionNo);

				// Part Name
				cell = row.getCell(2);
				cell.setCellValue(partName);

				// 변경검토내용
				cell = row.getCell(4);
				cell.setCellValue(reviewContents);

				// 담당자
				cell = row.getCell(6);
				cell.setCellValue(userId);

				// ECO
				cell = row.getCell(7);
				cell.setCellValue(ecoNo);

				// ECO Date
				cell = row.getCell(8);
				cell.setCellValue(ecoCompleteDateStr);

				// 비고
				cell = row.getCell(9);
				cell.setCellValue(desctiption);

				index++;

			}

		}

	}

	/**
	 * 설계 변경 리스트 가져옴
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
			// 필요항목인 것만 해당
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
			 * 변경내용(검토내용) 리스트 저장
			 */
			if (!changeDesList.contains(reviewContents))
				changeDesList.add(reviewContents);

			/**
			 * 설계 변경리스트 저장
			 */
			// 사양정리 이외의 필요인 경우 리스트
			if (ecoPublish.equals("필요")) {
				changeNotSpecList.add(rowData);
				// 사양정리인 경우
			} else if (ecoPublish.equals("사양정리")) {
				specArrageList.add(rowData);
			}

		}
	}

	/**
	 * 표준 폰트 생성
	 * 
	 * @param wb
	 * @return
	 * @throws Exception
	 */
	private Font createStdFont(Workbook wb, boolean isRedColor) throws Exception {
		// Font 정의
		Font stdFont = null;
		stdFont = wb.createFont();
		stdFont.setFontHeightInPoints((short) 10);
		stdFont.setFontName("맑은 고딕");
		if (isRedColor)
			stdFont.setColor(IndexedColors.RED.index);
		return stdFont;
	}

	/**
	 * Table Font 설정
	 * 
	 * @param wb
	 * @param isRedColor
	 * @param isBold
	 * @return
	 * @throws Exception
	 */
	private Font createTableFont(Workbook wb, boolean isRedColor, boolean isBold) throws Exception {
		// Font 정의
		Font font = null;
		font = wb.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("맑은 고딕");
		if (isRedColor)
			font.setColor(IndexedColors.RED.index);
		if (isBold)
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		return font;
	}

	/**
	 * Table Cell Style 생성
	 * 
	 * @param wb
	 * @param cell
	 * @param font
	 * @throws Exception
	 */
	private void createTableNewCellStyle(Workbook wb, Cell cell, Font font, boolean isPercentFormat) throws Exception {
		// Cell Style 적용
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
	 * 상태별 Sort 레벨
	 * 
	 * @param status
	 * @return
	 */
	private int getStatusSortLevel(String status) {
		if ("누락/오류 진행중".equals(status))
			return 1;
		else if ("지연 진행중".equals(status))
			return 2;
		else if ("기간내 진행중".equals(status))
			return 3;
		else if ("누락/오류 완료".equals(status))
			return 4;
		else if ("지연 완료".equals(status))
			return 5;
		else if ("기간내 완료".equals(status))
			return 6;
		else
			return 7;
	}
}
