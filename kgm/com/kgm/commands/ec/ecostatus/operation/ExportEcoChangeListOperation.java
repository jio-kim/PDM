package com.kgm.commands.ec.ecostatus.operation;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ca.odell.glazedlists.EventList;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData.StdInformData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * ���躯�� ���� ����Ʈ ���
 * 
 * @author baek
 * 
 */
public class ExportEcoChangeListOperation extends AbstractAIFOperation {
	private WaitProgressBar waitProgress;
	private File exportFile = null;
	private static int START_ROW_POS = 4; // Row ���� ��ġ
	private EcoChangeData inputData = null;

	public ExportEcoChangeListOperation(EcoChangeData inputData, File exportFile) {
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
			throw ex;
		}
	}

	/**
	 * Excel Export
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

			EventList<EcoChangeData> tableDataList = inputData.getSearchEcoChangeList();

			// ù��° Row
			Row firstRow = sheet.getRow(START_ROW_POS);

			int columnCount = firstRow.getPhysicalNumberOfCells();

			int startPos = START_ROW_POS + 1;
			int endPos = tableDataList.size() + START_ROW_POS;

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
					}
				}
			}

			HashMap<String, StdInformData> stdInformMap = getStdInformMap(tableDataList);

			for (int i = 0; i < tableDataList.size(); i++) {

				EcoChangeData data = tableDataList.get(i);
				Row row = sheet.getRow(i + START_ROW_POS);

				String masterPuid = data.getMasterPuid();

				StdInformData stdInformData = stdInformMap.get(masterPuid);
				
				String userInform = data.getUserId();
				String userId = null, userName = null;
				
				if (userInform != null && userInform.indexOf("(") > 0) {
					userId = userInform.substring(userInform.indexOf("(") + 1, userInform.indexOf(")"));
					userName = userInform.substring(0, userInform.indexOf("("));
				}else if(userInform != null && userInform.indexOf("(") < 0)
				{
					userName = userInform;
				}
				
				// �з�
				Cell cell = row.getCell(1);
				cell.setCellValue(stdInformData.getStageType());
				// Project
				cell = row.getCell(2);
				cell.setCellValue(stdInformData.getProjectId());
				// ����(O/SPEC)
				cell = row.getCell(3);
				cell.setCellValue(stdInformData.getOspecId());
				// ���泻��
				cell = row.getCell(4);
				cell.setCellValue(stdInformData.getChangeDesc());
				// �����������
				cell = row.getCell(5);
				cell.setCellValue(stdInformData.getApplyDate());
				// O/SPEC ������
				cell = row.getCell(6);
				cell.setCellValue(stdInformData.getReceiptDate());
				// ECO �Ϸ��û��
				cell = row.getCell(7);
				cell.setCellValue(stdInformData.getEcoCompleteReqDate());
				// Option Category
				cell = row.getCell(8);
				cell.setCellValue(data.getCategory());
				// ECO ����
				cell = row.getCell(9);
				cell.setCellValue(data.getEcoPublish());
				// Function
				cell = row.getCell(10);
				cell.setCellValue(data.getFunctionNo());
				// Part Name
				cell = row.getCell(11);
				cell.setCellValue(data.getPartName());
				// ���� ���䳻��
				cell = row.getCell(12);
				cell.setCellValue(data.getReviewContents());
				// System
				cell = row.getCell(13);
				cell.setCellValue(data.getSystemNo());
				// User Id
				cell = row.getCell(14);
				cell.setCellValue(userId);
				// User Name
				cell = row.getCell(15);
				cell.setCellValue(userName);
				// ECO
				cell = row.getCell(16);
				cell.setCellValue(data.getEcoNo());
				// ���
				cell = row.getCell(17);
				cell.setCellValue(data.getDescription());
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

	/**
	 * Master PUID �� �ش��ϴ� ���������� �����´�.
	 * 
	 * @param tableDataList
	 * @return
	 * @throws Exception
	 */
	private HashMap<String, StdInformData> getStdInformMap(EventList<EcoChangeData> tableDataList) throws Exception {
		HashMap<String, StdInformData> stdInformMap = new HashMap<String, StdInformData>();
		ArrayList<String> masterPuidList = new ArrayList<String>();
		for (EcoChangeData data : tableDataList) {
			String masterPuid = data.getMasterPuid();
			if (masterPuidList.contains(masterPuid))
				continue;
			masterPuidList.add(masterPuid);
		}
		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
		ds.put("MASTER_PUID", masterPuidList);
		ArrayList<HashMap<String, String>> stdInformList = dao.getRptStdInformWithPuid(ds);

		for (HashMap<String, String> rowMap : stdInformList) {
			String masterPuid = rowMap.get("MASTER_PUID");
			String stageType = rowMap.get("STAGE_TYPE");
			String projectId = rowMap.get("PROJECT_NO");
			String ospecId = rowMap.get("OSPEC_ID");
			String changeDesc = rowMap.get("CHANGE_DESC");
			String applyDate = rowMap.get("APPLY_DATE");
			String receiptDate = rowMap.get("OSPEC_RECEIPT_DATE");
			String ecoCompleteReqDate = rowMap.get("ECO_COMPLETE_REQ_DATE");
			String registerId = rowMap.get("REGISTER_ID");

			StdInformData data = new StdInformData();
			data.setMasterPuid(masterPuid);
			data.setStageType(stageType);
			data.setProjectId(projectId);
			data.setOspecId(ospecId);
			data.setChangeDesc(changeDesc);
			data.setApplyDate(applyDate);
			data.setReceiptDate(receiptDate);
			data.setEcoCompleteReqDate(ecoCompleteReqDate);
			data.setRegisterId(registerId);

			stdInformMap.put(masterPuid, data);
		}
		return stdInformMap;
	}

}
