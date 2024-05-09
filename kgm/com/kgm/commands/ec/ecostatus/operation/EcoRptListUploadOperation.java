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
 * Template 업로드
 * 
 * @author baek
 * 
 */
public class EcoRptListUploadOperation extends AbstractAIFOperation {

	private String templateFilePath = null;
	private WaitProgressBar waitProgress = null;
	private static int START_ROW_POS = 4; // Row 시작 위치
	private LinkedHashMap<ArrayList<String>, StdInformData> stdInformHash = null; // 기준정보 리스트(KEY: 분류 ,Project, O/SPEC 구분)
	private LinkedHashMap<ArrayList<String>, ChangeReviewData> changeReviewHash = null; // 변경검토 리스트(KEY:분류 ,Project, O/SPEC 구분,Category,Review Contents)
	private LinkedHashMap<ArrayList<String>, EcoChangeData> changeListHash = null; // 설계변경 리스트(KEY:분류 ,Project, O/SPEC 구분,Category,Review Contents,Function No,Part Name)
	private ArrayList<String> masterPuidList = null; // Master PUID 리스트. Template 업로드 후에 화면에 로드하기 위한 Key
	private EcoChangeData inputData = null;
	private TCSession tcSession = null;

	/**
	 * 
	 * @param templateFilePath
	 * @param inputData
	 *            초기 데이터(테이블 Data 정보)
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
			 * 엑셀파일 읽기
			 */
			extractDataFromExcelTemplateData(fis);
			/**
			 * 기준정보 저장
			 */
			ArrayList<HashMap<String, String>> stdInfoSaveDataList = saveRptStdInfoList();
			// 진행을 하지 않을려면
			if (stdInfoSaveDataList == null) {
				waitProgress.setStatus("Complete");
				waitProgress.close();
				return;
			}
			/**
			 * 변경 검토 정보 저장
			 */
			ArrayList<HashMap<String, String>> changeReviewDataList = saveChgReviewList();
			/**
			 * 변경 리스트 정보 저장
			 */
			ArrayList<HashMap<String, String>> changeList = saveChangeList();

			waitProgress.setStatus("Uploading Excel Data...");

			/**
			 * 기준정보 DB Table 에 생성
			 */
			createRptStdInfo(stdInfoSaveDataList);

			/**
			 * 변경 검토 DB Table 에 생성
			 */
			createRptChgReview(changeReviewDataList);

			/**
			 * 변경 리스트 DB Table 에 생성
			 */
			createRptList(changeList);

			/**
			 * 변경된 리스트 로드함
			 */
			waitProgress.setStatus("Loading  Data...");
			loadUploadData();
			waitProgress.setStatus("Complete");
			waitProgress.close();

			clearResource();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Upload Completed", "Complete", MessageBox.INFORMATION);
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
	 * 엑셀 파일로 부터 정보를 가져와서 저장함
	 * 
	 * @param fis
	 * @throws Exception
	 */
	private void extractDataFromExcelTemplateData(FileInputStream fis) throws Exception {
		// 1. 기준정보 Hash 에 저장
		// Key: Project, 구분(O/SPEC), 분류 , Value: 예상적용시점 O/SPEC, 접수일 , ECO 완료요청일
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

		int lastRowNumber = sheet.getPhysicalNumberOfRows(); // 마지막 Row

		stdInformHash = new LinkedHashMap<ArrayList<String>, StdInformData>();
		changeReviewHash = new LinkedHashMap<ArrayList<String>, ChangeReviewData>();
		changeListHash = new LinkedHashMap<ArrayList<String>, EcoChangeData>();
		masterPuidList = new ArrayList<String>();
		StringBuffer errorMsgSb = new StringBuffer();

		Date toDate = new Date();
		SimpleDateFormat updateDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String updateDate = updateDateFormat.format(toDate);
		/**
		 * 엑셀 Sheet Row 별로 정보를 가져온다.
		 */
		for (int i = START_ROW_POS; i <= lastRowNumber; i++) {
			Row row = sheet.getRow(i);

			String stageType = getCellText(row.getCell(1), sdf); // 분류 (*)
			String projectId = getCellText(row.getCell(2), sdf); // Project (*)
			String ospeId = getCellText(row.getCell(3), sdf); // 구분 (O/SPEC)
			String changeDesc = getCellText(row.getCell(4), sdf); // 변경내용(*)
			String applyDate = getCellText(row.getCell(5), sdf); // 예상적용시점
			String receiptDate = getCellText(row.getCell(6), sdf); // O/SPEC 접수일
			String ecoCompleteReqDate = getCellText(row.getCell(7), sdf); // ECO 완료요청일
			String optionCategory = getCellText(row.getCell(8), sdf); // Option Category
			String ecoPublish = getCellText(row.getCell(9), sdf); // ECO 발행(*)
			String functionNo = getCellText(row.getCell(10), sdf); // Function(*)
			String partName = getCellText(row.getCell(11), sdf); // Part Name(*)
			String changeReview = getCellText(row.getCell(12), sdf); // 변경검토내용(*)
			String systemNo = getCellText(row.getCell(13), sdf); // System
			String userId = getCellText(row.getCell(14), sdf); // 사번
			String userName = getCellText(row.getCell(15), sdf); // 담당자
			String ecoNo = getCellText(row.getCell(16), sdf); // ECO
			String description = getCellText(row.getCell(17), sdf); // 비고

			ospeId = ospeId.replace(projectId.concat("-"), "");
			// 기준정보리스트
			StdInformData stdInformData = new StdInformData();
			stdInformData.setStageType(stageType);
			stdInformData.setProjectId(projectId);
			stdInformData.setOspecId(ospeId);
			stdInformData.setChangeDesc(changeDesc);
			stdInformData.setApplyDate(applyDate);
			stdInformData.setReceiptDate(receiptDate);
			stdInformData.setEcoCompleteReqDate(ecoCompleteReqDate);
			stdInformData.setRegisterType("매뉴얼");
			stdInformData.setCreateDate(updateDate);

			/**
			 * 기준 정보 Data 저장
			 */
			StdInformData savedStdInformData = extractStdInfoDataList(stdInformData);
			/**
			 * 변경 검토 리스트 Data 저장
			 */
			ChangeReviewData chgReviewData = new ChangeReviewData(savedStdInformData, optionCategory, changeReview, "");
			ChangeReviewData savedReviewData = extractChangeReviewDataList(chgReviewData);

			/**
			 * 설계변경 리스트 Data 저장
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
			changeData.setStdInformData(savedStdInformData); // 기준정보
			changeData.setChangeReviewData(savedReviewData); // 변경 검토 정보

			extractChangeDataList(changeData);

			/**
			 * 필수 값 체크
			 */
			StringBuffer emptyValueSb = new StringBuffer();
			if (stageType.isEmpty())
				emptyValueSb.append("분류 ");
			if (projectId.isEmpty())
				emptyValueSb.append("Project ");
			if (ospeId.isEmpty())
				emptyValueSb.append("구분(O/SPEC) ");
			if (changeDesc.isEmpty())
				emptyValueSb.append("변경내용 ");
			if (ecoPublish.isEmpty())
				emptyValueSb.append("ECO 발생 ");
			if (functionNo.isEmpty())
				emptyValueSb.append("Functioin ");
			if (partName.isEmpty())
				emptyValueSb.append("Part Name ");
			if (partName.isEmpty())
				emptyValueSb.append("변경검토내용 ");
			/**
			 * Date 정합성 체크
			 */
			StringBuffer wrongDateSb = new StringBuffer();
			if (!applyDate.isEmpty() && !isDateValid(applyDate))
				wrongDateSb.append("예상적용시점 ");
			if (!receiptDate.isEmpty() && !isDateValid(receiptDate))
				wrongDateSb.append("O/SPEC 접수일 ");
			if (!ecoCompleteReqDate.isEmpty() && !isDateValid(ecoCompleteReqDate))
				wrongDateSb.append("ECO 완료요청일 ");

			/**
			 * 에러 메세지 조합함
			 */
			if (emptyValueSb.length() > 0 || wrongDateSb.length() > 0) {
				errorMsgSb.append((errorMsgSb.length() == 0 ? "\n" : "") + (i + 1) + " 행 입력값 오류\n");
				if (emptyValueSb.length() > 0)
					errorMsgSb.append(" ☞필수값이 없음: " + emptyValueSb + "\n");
				if (wrongDateSb.length() > 0)
					errorMsgSb.append(" ☞날짜유형 오류: " + wrongDateSb + "\n");
			}
		}

		// 에러 발생시 메세지 출력
		if (errorMsgSb.length() > 0)
			throw (new Exception(errorMsgSb.toString()));
	}

	/**
	 * 기준정보 Data 를 추출함
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
	 * 변경검토 Data 리스트를 추출함
	 */
	private ChangeReviewData extractChangeReviewDataList(ChangeReviewData chgReviewData) throws Exception {
		StdInformData stdInformData = chgReviewData.getStdInformData(); // 기준 정보
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
	 * 변경 리스트 데이터를 추출함
	 * 
	 * @param ecoChangeData
	 * @return
	 */
	private EcoChangeData extractChangeDataList(EcoChangeData ecoChangeData) {
		StdInformData stdInformData = ecoChangeData.getStdInformData(); // 기준정보
		ChangeReviewData chgReviewData = ecoChangeData.getChangeReviewData(); // 검토정보
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
	 * 생성할 기준정보 데이트를 저장함
	 * 
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> saveRptStdInfoList() throws Exception {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		ArrayList<String> alreadyDataMasterPuidList = new ArrayList<String>(); // 이미존재하는 Data 의 Master Puid 리스트
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		String userId = tcSession.getUser().getUserId();
		StringBuffer msgSb = new StringBuffer();
		msgSb.append("이미 데이터가 존재합니다.  진행시 입력된 모든값이 다 사라집니다.\n계속진행하시겠습니까?\n[분류, Project, 구분(O/Spec)]\n");
		/**
		 * 이미 존재하는 데이터가 존재하는지(체크기준: 동일한 Project No, OSPEC ID , 분류)
		 */
		CustomECODao dao = new CustomECODao();
		for (ArrayList<String> stdInformKey : stdInformHash.keySet()) {
			StdInformData stdInformData = stdInformHash.get(stdInformKey);
			DataSet ds = new DataSet();
			ds.put("PROJECT_NO", stdInformData.getProjectId());
			ds.put("OSPEC_ID", stdInformData.getOspecId());
			ds.put("STAGE_TYPE", stdInformData.getStageType());
			ArrayList<String> dupMasterPuidList = dao.getDupRptInfoList(ds);
			// 중복된 Master PUID 저장
			for (String masterPuid : dupMasterPuidList)
				alreadyDataMasterPuidList.add(masterPuid);
			// 메세지 만듬
			if (dupMasterPuidList.size() > 0) {
				String stdMsgData = stdInformData.getStageType() + ", " + stdInformData.getProjectId() + ", " + stdInformData.getOspecId() + "\n";
				msgSb.append(stdMsgData);
			}
		}

		// 중복된 Data 가 존재하면
		if (alreadyDataMasterPuidList.size() > 0) {
			int retValue = ConfirmDialog.prompt(shell, "Confirm", msgSb.toString());
			if (retValue != IDialogConstants.YES_ID)
				return null;
			{
				retValue = ConfirmDialog.prompt(shell, "Confirm", "정말로 재성성 하시겠습니까?");
				if (retValue != IDialogConstants.YES_ID)
					return null;
				/**
				 * 재생성한다면 기존 데이터를 삭제 수행
				 */
				dao = new CustomECODao();
				for (String masterPuid : alreadyDataMasterPuidList) {
					dao.deleteRptChangeList(masterPuid);
				}
			}
		}

		// 기준정보 유일키를 추출함
		ArrayList<String> stdInformSysGuidList = getMultiSysGuidList(stdInformHash.size());
		if (stdInformHash.size() != stdInformSysGuidList.size())
			throw (new Exception("기준정보 유일 키 오류"));

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
	 * 생성할 변경검토 리스트 데이터를 저장함
	 * 
	 * @return
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> saveChgReviewList() throws Exception {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		// 변경 검토 유일키를 추출함
		ArrayList<String> chgReviewSysGuidList = getMultiSysGuidList(changeReviewHash.size());
		if (changeReviewHash.size() != chgReviewSysGuidList.size())
			throw (new Exception("검토내용정보 유일 키 오류"));

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
	 * 생성할 변경 리스트 데이터를 저장함
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
	 * 기준정보 DB Table 에 생성
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptStdInfo(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptStdInfo(dataList);
	}

	/**
	 * 변경검토 DB Table 에 생성
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptChgReview(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptChgReview(dataList);
	}

	/**
	 * 변경 리스트 DB Table 에 생성
	 * 
	 * @param dataList
	 * @throws Exception
	 */
	private void createRptList(ArrayList<HashMap<String, String>> dataList) throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.insertRptList(dataList);
	}

	/**
	 * 리스트 검색
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
			EcoChangeData rowInitDataObj = (EcoChangeData) rowData.clone();// 초기 데이터
			rowData.setRowInitDataObj(rowInitDataObj);
			// 테이블에 Row 추가
			inputData.getSearchEcoChangeList().add(rowData);
		}
	}

	/**
	 * Cell Text 가져오기
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
	 * 날짜 형식이 맞는지 체크
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
	 * 유일한 Table Key 정보를 가져오
	 * 
	 * @param count
	 *            갯수
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
