package com.kgm.commands.ec.ecostatus.operation;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData.ChangeReviewData;
import com.kgm.commands.ec.ecostatus.model.EcoChangeData.StdInformData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;

/**
 * Template ���ε�
 * 
 * @author baek
 * 
 */
public class EcoRptListUploadOperation extends AbstractAIFOperation {

	private String templateFilePath = null;
	private WaitProgressBar waitProgress = null;
	private static int START_ROW_POS = 4; // Row ���� ��ġ
	private LinkedHashMap<ArrayList<String>, StdInformData> stdInformHash = null; // �������� ����Ʈ(KEY: �з� ,Project, O/SPEC ����)
	private LinkedHashMap<ArrayList<String>, ChangeReviewData> changeReviewHash = null; // ������� ����Ʈ(KEY:�з� ,Project, O/SPEC ����,Category,Review Contents)
	private LinkedHashMap<ArrayList<String>, EcoChangeData> changeListHash = null; // ���躯�� ����Ʈ(KEY:�з� ,Project, O/SPEC ����,Category,Review Contents,Function No,Part Name)
	private ArrayList<String> masterPuidList = null; // Master PUID ����Ʈ. Template ���ε� �Ŀ� ȭ�鿡 �ε��ϱ� ���� Key
	private EcoChangeData inputData = null;
	private TCSession tcSession = null;

	/**
	 * 
	 * @param templateFilePath
	 * @param inputData
	 *            �ʱ� ������(���̺� Data ����)
	 */
	public EcoRptListUploadOperation(String templateFilePath, EcoChangeData inputData) {
		this.templateFilePath = templateFilePath;
		this.inputData = inputData;
		this.tcSession =CustomUtil.getTCSession();
	}

	@Override
	public void executeOperation() throws Exception {
		FileInputStream fis = null;
		try {
			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.setWindowSize(500, 400);
			waitProgress.start();
			waitProgress.setStatus("Verify Excel Data...");
			/**
			 * �������� �б�
			 */
			extractDataFromExcelTemplateData(fis);
			/**
			 * �������� ����
			 */
			ArrayList<HashMap<String, String>> stdInfoSaveDataList = saveRptStdInfoList();
			// ������ ���� ��������
			if (stdInfoSaveDataList == null) {
				waitProgress.setStatus("Complete");
				waitProgress.close();
				return;
			}
			/**
			 * ���� ���� ���� ����
			 */
			ArrayList<HashMap<String, String>> changeReviewDataList = saveChgReviewList();
			/**
			 * ���� ����Ʈ ���� ����
			 */
			ArrayList<HashMap<String, String>> changeList = saveChangeList();

			waitProgress.setStatus("Uploading Excel Data...");

			/**
			 * �������� DB Table �� ����
			 */
			createRptStdInfo(stdInfoSaveDataList);

			/**
			 * ���� ���� DB Table �� ����
			 */
			createRptChgReview(changeReviewDataList);

			/**
			 * ���� ����Ʈ DB Table �� ����
			 */
			createRptList(changeList);

			/**
			 * ����� ����Ʈ �ε���
			 */
			waitProgress.setStatus("Loading  Data...");
			loadUploadData();
			waitProgress.setStatus("Complete");
			waitProgress.close();

			clearResource();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Upload Completed", "Complete", MessageBox.INFORMATION);
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
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * ���� ���Ϸ� ���� ������ �����ͼ� ������
	 * 
	 * @param fis
	 * @throws Exception
	 */
	private void extractDataFromExcelTemplateData(FileInputStream fis) throws Exception {
		// 1. �������� Hash �� ����
		// Key: Project, ����(O/SPEC), �з� , Value: ����������� O/SPEC, ������ , ECO �Ϸ��û��
		Workbook wb = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		fis = new FileInputStream(templateFilePath);
		if (templateFilePath.toLowerCase().endsWith(".xls"))
			wb = new HSSFWorkbook(fis);
		else
			wb = new XSSFWorkbook(fis);

		fis.close();
		fis = null;

		Sheet sheet = wb.getSheetAt(0);

		int lastRowNumber = sheet.getPhysicalNumberOfRows(); // ������ Row

		stdInformHash = new LinkedHashMap<ArrayList<String>, StdInformData>();
		changeReviewHash = new LinkedHashMap<ArrayList<String>, ChangeReviewData>();
		changeListHash = new LinkedHashMap<ArrayList<String>, EcoChangeData>();
		masterPuidList = new ArrayList<String>();
		StringBuffer errorMsgSb = new StringBuffer();

		Date toDate = new Date();
		SimpleDateFormat updateDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String updateDate = updateDateFormat.format(toDate);
		/**
		 * ���� Sheet Row ���� ������ �����´�.
		 */
		for (int i = START_ROW_POS; i <= lastRowNumber; i++) {
			Row row = sheet.getRow(i);

			String stageType = getCellText(row.getCell(1), sdf); // �з� (*)
			String projectId = getCellText(row.getCell(2), sdf); // Project (*)
			String ospeId = getCellText(row.getCell(3), sdf); // ���� (O/SPEC)
			String changeDesc = getCellText(row.getCell(4), sdf); // ���泻��(*)
			String applyDate = getCellText(row.getCell(5), sdf); // �����������
			String receiptDate = getCellText(row.getCell(6), sdf); // O/SPEC ������
			String ecoCompleteReqDate = getCellText(row.getCell(7), sdf); // ECO �Ϸ��û��
			String optionCategory = getCellText(row.getCell(8), sdf); // Option Category
			String ecoPublish = getCellText(row.getCell(9), sdf); // ECO ����(*)
			String functionNo = getCellText(row.getCell(10), sdf); // Function(*)
			String partName = getCellText(row.getCell(11), sdf); // Part Name(*)
			String changeReview = getCellText(row.getCell(12), sdf); // ������䳻��(*)
			String systemNo = getCellText(row.getCell(13), sdf); // System
			String userId = getCellText(row.getCell(14), sdf); // ���
			String userName = getCellText(row.getCell(15), sdf); // �����
			String ecoNo = getCellText(row.getCell(16), sdf); // ECO
			String description = getCellText(row.getCell(17), sdf); // ���

			ospeId = ospeId.replace(projectId.concat("-"), "");
			// ������������Ʈ
			StdInformData stdInformData = new StdInformData();
			stdInformData.setStageType(stageType);
			stdInformData.setProjectId(projectId);
			stdInformData.setOspecId(ospeId);
			stdInformData.setChangeDesc(changeDesc);
			stdInformData.setApplyDate(applyDate);
			stdInformData.setReceiptDate(receiptDate);
			stdInformData.setEcoCompleteReqDate(ecoCompleteReqDate);
			stdInformData.setRegisterType("�Ŵ���");
			stdInformData.setCreateDate(updateDate);

			/**
			 * ���� ���� Data ����
			 */
			StdInformData savedStdInformData = extractStdInfoDataList(stdInformData);
			/**
			 * ���� ���� ����Ʈ Data ����
			 */
			ChangeReviewData chgReviewData = new ChangeReviewData(savedStdInformData, optionCategory, changeReview, "");
			ChangeReviewData savedReviewData = extractChangeReviewDataList(chgReviewData);

			/**
			 * ���躯�� ����Ʈ Data ����
			 */

			EcoChangeData changeData = new EcoChangeData();
			changeData.setCreationDate(updateDate);
			changeData.setEcoPublish(ecoPublish);
			changeData.setFunctionNo(functionNo);
			changeData.setPartName(partName);
			changeData.setSystemNo(systemNo);
			changeData.setUserId(userId);
			changeData.setUserName(userName);
			changeData.setEcoNo(ecoNo);
			changeData.setDescription(description);
			changeData.setStdInformData(savedStdInformData); // ��������
			changeData.setChangeReviewData(savedReviewData); // ���� ���� ����

			extractChangeDataList(changeData);

			/**
			 * �ʼ� �� üũ
			 */
			StringBuffer emptyValueSb = new StringBuffer();
			if (stageType.isEmpty())
				emptyValueSb.append("�з� ");
			if (projectId.isEmpty())
				emptyValueSb.append("Project ");
			if (ospeId.isEmpty())
				emptyValueSb.append("����(O/SPEC) ");
			if (changeDesc.isEmpty())
				emptyValueSb.append("���泻�� ");
			if (ecoPublish.isEmpty())
				emptyValueSb.append("ECO �߻� ");
			if (functionNo.isEmpty())
				emptyValueSb.append("Functioin ");
			if (partName.isEmpty())
				emptyValueSb.append("Part Name ");
			if (partName.isEmpty())
				emptyValueSb.append("������䳻�� ");
			/**
			 * Date ���ռ� üũ
			 */
			StringBuffer wrongDateSb = new StringBuffer();
			if (!applyDate.isEmpty() && !isDateValid(applyDate))
				wrongDateSb.append("����������� ");
			if (!receiptDate.isEmpty() && !isDateValid(receiptDate))
				wrongDateSb.append("O/SPEC ������ ");
			if (!ecoCompleteReqDate.isEmpty() && !isDateValid(ecoCompleteReqDate))
				wrongDateSb.append("ECO �Ϸ��û�� ");

			/**
			 * ���� �޼��� ������
			 */
			if (emptyValueSb.length() > 0 || wrongDateSb.length() > 0) {
				errorMsgSb.append((errorMsgSb.length() == 0 ? "\n" : "") + (i + 1) + " �� �Է°� ����\n");
				if (emptyValueSb.length() > 0)
					errorMsgSb.append(" ���ʼ����� ����: " + emptyValueSb + "\n");
				if (wrongDateSb.length() > 0)
					errorMsgSb.append(" �ѳ�¥���� ����: " + wrongDateSb + "\n");
			}
		}

		// ���� �߻��� �޼��� ���
		if (errorMsgSb.length() > 0)
			throw (new Exception(errorMsgSb.toString()));
	}

	/**
	 * �������� Data �� ������
	 * 
	 * @param stdInformData
	 */
	private StdInformData extractStdInfoDataList(StdInformData stdInformData) throws Exception {

		String stageType = stdInformData.getStageType();
		String projectId = stdInformData.getProjectId();
		String ospecId = stdInformData.getOspecId();
		ArrayList<String> stdInfoKey = new ArrayList<String>();
		stdInfoKey.add(stageType);
		stdInfoKey.add(projectId);
		stdInfoKey.add(ospecId);

		if (!stdInformHash.containsKey(stdInfoKey)) {
			stdInformHash.put(stdInfoKey, stdInformData);
			return stdInformData;
		} else
			return stdInformHash.get(stdInfoKey);
	}

	/**
	 * ������� Data ����Ʈ�� ������
	 */
	private ChangeReviewData extractChangeReviewDataList(ChangeReviewData chgReviewData) throws Exception {
		StdInformData stdInformData = chgReviewData.getStdInformData(); // ���� ����
		String stageType = stdInformData.getStageType();
		String projectId = stdInformData.getProjectId();
		String ospecId = stdInformData.getOspecId();
		String category = chgReviewData.getCategory();
		String reviewContents = chgReviewData.getReviewContents();
		ArrayList<String> reviewKey = new ArrayList<String>();
		reviewKey.add(stageType);
		reviewKey.add(projectId);
		reviewKey.add(ospecId);
		reviewKey.add(category);
		reviewKey.add(reviewContents);

		if (!changeReviewHash.containsKey(reviewKey)) {
			changeReviewHash.put(reviewKey, chgReviewData);
			return chgReviewData;
		} else
			return changeReviewHash.get(reviewKey);
	}

	/**
	 * ���� ����Ʈ �����͸� ������
	 * 
	 * @param ecoChangeData
	 * @return
	 */
	private EcoChangeData extractChangeDataList(EcoChangeData ecoChangeData) {
		StdInformData stdInformData = ecoChangeData.getStdInformData(); // ��������
		ChangeReviewData chgReviewData = ecoChangeData.getChangeReviewData(); // ��������
		String stageType = stdInformData.getStageType();
		String projectId = stdInformData.getProjectId();
		String ospecId = stdInformData.getOspecId();
		String category = chgReviewData.getCategory();
		String reviewContents = chgReviewData.getReviewContents();
		String functionNo = ecoChangeData.getFunctionNo();
		String partName = ecoChangeData.getPartName();
		ArrayList<String> changeDataKey = new ArrayList<String>();
		changeDataKey.add(stageType);
		changeDataKey.add(projectId);
		changeDataKey.add(ospecId);
		changeDataKey.add(category);
		changeDataKey.add(reviewContents);
		changeDataKey.add(functionNo);
		changeDataKey.add(partName);

		if (!changeListHash.containsKey(changeDataKey)) {
			changeListHash.put(changeDataKey, ecoChangeData);
			return ecoChangeData;
		} else
			return changeListHash.get(changeDataKey);
	}

	/**
	 * ������ �������� ����Ʈ�� ������
	 * 
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> saveRptStdInfoList() throws Exception {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		ArrayList<String> alreadyDataMasterPuidList = new ArrayList<String>(); // �̹������ϴ� Data �� Master Puid ����Ʈ
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		String userId = tcSession.getUser().getUserId();
		StringBuffer msgSb = new StringBuffer();
		msgSb.append("�̹� �����Ͱ� �����մϴ�.  ����� �Էµ� ��簪�� �� ������ϴ�.\n��������Ͻðڽ��ϱ�?\n[�з�, Project, ����(O/Spec)]\n");
		/**
		 * �̹� �����ϴ� �����Ͱ� �����ϴ���(üũ����: ������ Project No, OSPEC ID , �з�)
		 */
		CustomECODao dao = new CustomECODao();
		for (ArrayList<String> stdInformKey : stdInformHash.keySet()) {
			StdInformData stdInformData = stdInformHash.get(stdInformKey);
			DataSet ds = new DataSet();
			ds.put("PROJECT_NO", stdInformData.getProjectId());
			ds.put("OSPEC_ID", stdInformData.getOspecId());
			ds.put("STAGE_TYPE", stdInformData.getStageType());
			ArrayList<String> dupMasterPuidList = dao.getDupRptInfoList(ds);
			// �ߺ��� Master PUID ����
			for (String masterPuid : dupMasterPuidList)
				alreadyDataMasterPuidList.add(masterPuid);
			// �޼��� ����
			if (dupMasterPuidList.size() > 0) {
				String stdMsgData = stdInformData.getStageType() + ", " + stdInformData.getProjectId() + ", " + stdInformData.getOspecId() + "\n";
				msgSb.append(stdMsgData);
			}
		}

		// �ߺ��� Data �� �����ϸ�
		if (alreadyDataMasterPuidList.size() > 0) {
			int retValue = ConfirmDialog.prompt(shell, "Confirm", msgSb.toString());
			if (retValue != IDialogConstants.YES_ID)
				return null;
			{
				retValue = ConfirmDialog.prompt(shell, "Confirm", "������ �缺�� �Ͻðڽ��ϱ�?");
				if (retValue != IDialogConstants.YES_ID)
					return null;
				/**
				 * ������Ѵٸ� ���� �����͸� ���� ����
				 */
				dao = new CustomECODao();
				for (String masterPuid : alreadyDataMasterPuidList) {
					dao.deleteRptChangeList(masterPuid);
				}
			}
		}

		// �������� ����Ű�� ������
		ArrayList<String> stdInformSysGuidList = getMultiSysGuidList(stdInformHash.size());
		if (stdInformHash.size() != stdInformSysGuidList.size())
			throw (new Exception("�������� ���� Ű ����"));

		int cnt = 0;
		for (ArrayList<String> stdInformKey : stdInformHash.keySet()) {
			StdInformData stdInformData = stdInformHash.get(stdInformKey);
			String masterPuid = stdInformSysGuidList.get(cnt);
			stdInformData.setMasterPuid(masterPuid);

			HashMap<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("MASTER_PUID", masterPuid);
			dataMap.put("PROJECT_NO", stdInformData.getProjectId());
			dataMap.put("OSPEC_ID", stdInformData.getOspecId());
			dataMap.put("STAGE_TYPE", stdInformData.getStageType());
			dataMap.put("CHANGE_DESC", stdInformData.getChangeDesc());
			dataMap.put("APPLY_DATE", "".equals(stdInformData.getApplyDate()) ? null : stdInformData.getApplyDate());
			dataMap.put("OSPEC_RECEIPT_DATE", "".equals(stdInformData.getReceiptDate()) ? null : stdInformData.getReceiptDate());
			dataMap.put("ECO_COMPLETE_REQ_DATE", "".equals(stdInformData.getEcoCompleteReqDate()) ? null : stdInformData.getEcoCompleteReqDate());
			dataMap.put("REGISTER_TYPE", stdInformData.getRegisterType());
			dataMap.put("REGISTER_ID", userId);
			dataMap.put("CREATE_DATE", stdInformData.getCreateDate());

			// dataMap.put("DESCRIPTION", stdInformData.getDescription());

			dataList.add(dataMap);

			masterPuidList.add(masterPuid);

			cnt++;
			// System.out.println("STD Data(" + cnt + ") : " + stdInformData);
		}
		return dataList;
	}

	/**
	 * ������ ������� ����Ʈ �����͸� ������
	 * 
	 * @return
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> saveChgReviewList() throws Exception {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		// ���� ���� ����Ű�� ������
		ArrayList<String> chgReviewSysGuidList = getMultiSysGuidList(changeReviewHash.size());
		if (changeReviewHash.size() != chgReviewSysGuidList.size())
			throw (new Exception("���䳻������ ���� Ű ����"));

		int cnt = 0;
		for (ArrayList<String> chgReviewKey : changeReviewHash.keySet()) {
			ChangeReviewData data = changeReviewHash.get(chgReviewKey);
			String sysGuid = chgReviewSysGuidList.get(cnt);
			data.setReviewPuid(sysGuid);

			HashMap<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("OPTION_CATEGORY_PUID", sysGuid);
			dataMap.put("MASTER_PUID", data.getStdInformData().getMasterPuid());
			dataMap.put("OPTION_CATEGORY", data.getCategory());
			dataMap.put("REVIEW_CONTENTS", data.getReviewContents());

			dataList.add(dataMap);
			cnt++;
			// System.out.println("Review Data(" + cnt + ") : " + data);
		}
		return dataList;
	}

	/**
	 * ������ ���� ����Ʈ �����͸� ������
	 * 
	 * @return
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> saveChangeList() throws Exception {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		// int cnt = 0;
		for (ArrayList<String> changeDataKey : changeListHash.keySet()) {
			EcoChangeData data = changeListHash.get(changeDataKey);

			HashMap<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("MASTER_PUID", data.getStdInformData().getMasterPuid());
			dataMap.put("OPTION_CATEGORY_PUID", data.getChangeReviewData().getReviewPuid());
			dataMap.put("ECO_PUBLISH", data.getEcoPublish());
			dataMap.put("FUNCTION_ID", data.getFunctionNo());
			dataMap.put("PART_NAME", data.getPartName());
			dataMap.put("SYSTEM", data.getSystemNo());
			dataMap.put("USER_ID", data.getUserId());
			dataMap.put("USER_NAME", data.getUserName());
			dataMap.put("TEAM_NAME", data.getTeamName());
			dataMap.put("ECO_NO", data.getEcoNo());
			// dataMap.put("ECO_COMPLETE_DATE", "".equals(data.getEcoCompleteDate()) ? null : data.getEcoCompleteDate());
			dataMap.put("DESCRIPTION", data.getDescription());
			dataMap.put("UPDATE_DATE", data.getCreationDate());

			dataList.add(dataMap);
			// cnt++;
			// System.out.println("Total Change Data(" + cnt + ") : " + data.getFunctionNo() + ", " + data.getPartName() + ", " + data.getChangeReviewData());
		}

		return dataList;
	}

	/**
	 * �������� DB Table �� ����
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptStdInfo(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptStdInfo(dataList);
	}

	/**
	 * ������� DB Table �� ����
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptChgReview(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptChgReview(dataList);
	}

	/**
	 * ���� ����Ʈ DB Table �� ����
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptList(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptList(dataList);
	}

	/**
	 * ����Ʈ �˻�
	 * 
	 * @throws Exception
	 */
	private void loadUploadData() throws Exception {
		CustomECODao dao = new CustomECODao();

		DataSet ds = new DataSet();
		ds.put("MASTER_PUID", masterPuidList);

		ArrayList<HashMap<String, String>> changList = dao.getEcoStatusChangeList(ds);

		for (HashMap<String, String> changeRowMap : changList) {
			String groupSeqNo = changeRowMap.get("GROUP_SEQ");
			String registerType = changeRowMap.get("REGISTER_TYPE");
			String optCategory = changeRowMap.get("OPTION_CATEGORY");
			String creationDate = changeRowMap.get("CREATE_DATE");
			String ecoPublish = changeRowMap.get("ECO_PUBLISH");
			String changeStatus = changeRowMap.get("STATUS");
			String functionNo = changeRowMap.get("FUNCTION_ID");
			String partName = changeRowMap.get("PART_NAME");
			String projectId = changeRowMap.get("PROJECT_NO");
			String ospecId = changeRowMap.get("OSPEC_ID");
			String changeDesc = changeRowMap.get("CHANGE_DESC");
			String reviewContents = changeRowMap.get("REVIEW_CONTENTS");
			String systemNo = changeRowMap.get("SYSTEM");
			String userId = changeRowMap.get("USER_NAME");
			String teamName = changeRowMap.get("TEAM_NAME");
			String mailStatus = changeRowMap.get("MAIL_STATUS");
			String ecoNo = changeRowMap.get("ECO_NO");
			String ecoCompleteDate = changeRowMap.get("ECO_COMPLETE_DATE");
			String description = changeRowMap.get("DESCRIPTION");
			String masterPuid = changeRowMap.get("MASTER_PUID");
			String opCategoryPuid = changeRowMap.get("OPTION_CATEGORY_PUID");
			String changeListPuid = changeRowMap.get("PUID");
			String engineFlag = changeRowMap.get("VORT_TYPE");
			String registerId = changeRowMap.get("REGISTER_ID");

			EcoChangeData rowData = new EcoChangeData();
			rowData.setGroupSeqNo(groupSeqNo);
			rowData.setRegisterType(registerType);
			rowData.setCategory(optCategory);
			rowData.setCreationDate(creationDate);
			rowData.setEcoPublish(ecoPublish);
			rowData.setChangeStatus(changeStatus);
			rowData.setEngineFlag(engineFlag);
			rowData.setFunctionNo(functionNo);
			rowData.setPartName(partName);
			rowData.setProjectId(projectId);
			rowData.setOspecId(ospecId);
			rowData.setChangeDesc(changeDesc);
			rowData.setReviewContents(reviewContents);
			rowData.setSystemNo(systemNo);
			rowData.setUserId(userId);
			rowData.setTeamName(teamName);
			rowData.setMailStatus(mailStatus);
			rowData.setEcoNo(ecoNo);
			rowData.setEcoCompleteDate(ecoCompleteDate);
			rowData.setDescription(description);
			rowData.setMasterPuid(masterPuid);
			rowData.setOpCategoryPuid(opCategoryPuid);
			rowData.setRowChangeType(EcoChangeData.ROW_CHANGE_TYPE_NONE);
			rowData.setRowDataObj(rowData);
			rowData.setChangeListPuid(changeListPuid);
			rowData.setRegisterId(registerId);
			EcoChangeData rowInitDataObj = (EcoChangeData) rowData.clone();// �ʱ� ������
			rowData.setRowInitDataObj(rowInitDataObj);
			// ���̺� Row �߰�
			inputData.getSearchEcoChangeList().add(rowData);
		}
	}

	/**
	 * Cell Text ��������
	 * 
	 * @param cell
	 * @return
	 * @throws Exception
	 */
	public String getCellText(Cell cell, SimpleDateFormat sdf) throws Exception {
		String value = "";
		if (cell != null) {

			switch (cell.getCellType()) {
			case XSSFCell.CELL_TYPE_FORMULA:
				value = cell.getCellFormula();
				break;
			case XSSFCell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell))
					value = sdf.format(cell.getDateCellValue());
				else {
					if (String.valueOf(cell.getNumericCellValue()).lastIndexOf("0") > 0)
						value = "" + (int) cell.getNumericCellValue();
					else
						value = "" + cell.getNumericCellValue();
				}
				break;
			case XSSFCell.CELL_TYPE_STRING:
				value = "" + cell.getStringCellValue();
				break;

			case XSSFCell.CELL_TYPE_BLANK:
				value = "";
				break;

			case XSSFCell.CELL_TYPE_ERROR:
				value = "" + cell.getErrorCellValue();
				break;
			default:
			}
		}
		return value;
	}

	/**
	 * ��¥ ������ �´��� üũ
	 * 
	 * @param str
	 * @return
	 */
	public boolean isDateValid(String str) {
		if (str == null || !str.matches("\\d{4}-[01]\\d-[0-3]\\d"))
			return false;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setLenient(false);
		try {
			df.parse(str);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * ������ Table Key ������ ������
	 * 
	 * @param count
	 *            ����
	 * @return
	 * @throws Exception
	 */
	private ArrayList<String> getMultiSysGuidList(int count) throws Exception {
		CustomECODao dao = new CustomECODao();
		ArrayList<String> tableNameList = new ArrayList<String>();
		for (int i = 0; i < count; i++)
			tableNameList.add("DUAL");
		return dao.getMultiSysGuidList(tableNameList);
	}

	/**
	 * Memory Clear
	 */
	private void clearResource() {
		if (stdInformHash != null) {
			stdInformHash.clear();
			stdInformHash = null;
		}

		if (changeReviewHash != null) {
			changeReviewHash.clear();
			changeReviewHash = null;
		}

		if (changeListHash != null) {
			changeListHash.clear();
			changeListHash = null;
		}
	}

}
