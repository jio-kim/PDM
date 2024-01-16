package com.ssangyong.commands.ec.workflow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.workflow.SYMCDecisionDialog;
import com.ssangyong.commands.workflow.changetoreplace.ChangeToReplace;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.PreferenceService;
import com.ssangyong.common.utils.ProcessUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.ssangyong.dto.ApprovalLineData;
import com.ssangyong.rac.kernel.SYMCBOMEditData;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentContextList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentSignoffType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCSignoffOriginType;
import com.teamcenter.rac.kernel.TCTaskState;
import com.teamcenter.rac.pse.services.PSEApplicationService;

/**
 * 최초 ECO 상신 및 ECO 프로세스 진행 중 반려 후 재 상신 시 호출 됨.
 * 
 * [SR140702-056][20140422] modified yunjae, 결재 관련 오류 수정. 
 * SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
 * [20161117][ymjang] CM으로 시작되는 ECO 는 S201 인도 마힌드라 ECO 로서 원가는 제외함.
 * [20170613][ljg] OrderNo 중복 되는 체크 로직이 기존에 있으나 완벽 하지 않아서, 체크 로직 추가 함.
 * [SR170828-015][LJG]Chassis module 관리를 위한 검증 조건 추가 요청
 *  1. ECO 내의 Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
 * 	2. Module code : FCM or RCM
 * 	3. Part의 Option : Z999을 포함하는 경우
 * [SR180130-037][LJG] ECO Validation 인증팀 삭제
 * [20180206][LJG] 인증팀 필수에서 -> 인증팀은 추가 되면 안되는거로 변경
 * [SR180207-027][LJG] 원가기획팀은 추가되면 안되고, 원가기술팀만 추가 되어야함
 * [CF-1635] [20201208 by SYChon] 엔진파트 관리를 위한 검증 조건 추가 요청 - 엔진옵션코드 미입력의 경우에도 ECO 상신이 되는 문제 해결
 * @author DJKIM
 *
 */
public class ECOProcessOperation extends AbstractAIFOperation {

	private TCSession session;
	private TCComponentChangeItemRevision changeRevision;
	private String ecoNo;

	private TCComponentProcess newProcess;

	private ArrayList<TCComponent> targetList = new ArrayList<TCComponent>();
	private HashMap<String, ArrayList<TCComponentGroupMember>> reviewers = new HashMap<String, ArrayList<TCComponentGroupMember>>();
	private TCComponent[] solutionList;
	private TCComponent[] problemList;

	private WaitProgressBar progress;
	private String message;

	private CustomECODao dao = new CustomECODao();
	private SYMCDecisionDialog parent;

	private int iReturnCode = -1;

	private final static String EVENT_START = "  ▶";

	public ECOProcessOperation(TCSession session, TCComponentChangeItemRevision changeRevision) {
		this.session = session;
		this.changeRevision = changeRevision;
	}

	public ECOProcessOperation(SYMCDecisionDialog parent, TCSession session, TCComponentChangeItemRevision changeRevision) {
		this.session = session;
		this.changeRevision = changeRevision;
		this.parent = parent;
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			if(parent != null && changeRevision == null){
				String processName = parent.getCurrentJob().getName();
				String ecoNo = processName.substring(processName.indexOf("[")+1, processName.indexOf("]"));
				String ecoRevisionPuid = dao.getEcoRevisionPuid(ecoNo);
				changeRevision = (TCComponentChangeItemRevision) session.stringToComponent(ecoRevisionPuid);
				// 20130611, 김대중C, 워크플로우 재상신 할 때 타겟이 전부 떨어지는 경우에 eco revision을 찾아서 붙여주는 로직 
				parent.getCurrentJob().getRootTask().add("root_target_attachments", changeRevision); 
			}
			
			ecoNo = changeRevision.getProperty("item_id");
			/**
			 * [SR141120-043][2014.11.21][jclee] ECO 내역 중 Color ID가 None이 아니면서 Color Section No가 없을 경우 Warning Message Box Open
			 */
			iReturnCode = -1;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						iReturnCode = checkColorID();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			if (iReturnCode == SWT.NO) {
				return;
			}

			/**
			 * [SR141205-027][2014.12.16][jclee] Color ID가 변경된 항목이 있을 경우 Warning Message Box Open
			 */
			iReturnCode = -1;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						iReturnCode = checkColorIDChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			if (iReturnCode == SWT.NO) {
				return;
			}

			/**
			 * [SR150313-028][2015.03.25][jclee] Change Type이 F2인 항목 중 기존 S/Mode와 비교하여 서로 다른 Part 목록이 존재할 경우 Warning Message Box Open
			 */
			iReturnCode = -1;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						iReturnCode = selectECOEplCOSModeCompareList();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			if (iReturnCode == SWT.NO) {
				return;
			}

			//			/**
			//			 * [SR없음][20151016][jclee] EPL Cut 후 Revise하여 다시 Paste한 경우(Revise 이력 누락)
			// [SR없음][20160205][jclee] 로직 삭제. 박태훈 주임 요청.
			//			 */
			//			iReturnCode = -1;
			//			Display.getDefault().syncExec(new Runnable() {
			//				@Override
			//				public void run() {
			//					try {
			//						iReturnCode = getCANNOTGeneratedReviseList();
			//					} catch (Exception e) {
			//						e.printStackTrace();
			//					}
			//				}
			//			});
			//			
			//			if (iReturnCode == SWT.OK) {
			//				return;
			//			}

			//[SR170828-015][LJG]Chassis module 관리를 위한 검증 조건 추가 요청
			// 1. ECO 내의 Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
			// 2. Module code : FCM or RCM
			// 3. Part의 Option : Z999을 포함하는 경우
			// [20191024]설계변경 검증 속도 개선 프로젝트에서 미사용하기로 결정(기술관리팀)
//			iReturnCode = -1;
//			Display.getDefault().syncExec(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						iReturnCode = checkChassisModule();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});
//
//			if (iReturnCode == SWT.OK) {
//				return;
//			}
			
			// Structure Manager에서 편집중인 BOMWindow 있는지 체크
//			iReturnCode = -1;
//			Display.getDefault().syncExec(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						iReturnCode = checkBOMEdit();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});
//
//			if (iReturnCode == SWT.OK) {
//				return;
//			}

			if(parent == null){
				progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
			}else{
				progress = new WaitProgressBar(parent);
			}
			
			progress.setWindowSize(500, 400);
			progress.start();						
			progress.setShowButton(true);
			progress.setStatus("ECO Workflow creation start.");
			progress.setAlwaysOnTop(true);

			//체크할 포인트 totalCount로 체크 포인트가 하나씩 늘어날 때마다 totalCount 변수를 수정해야 한다.
			int totalCount = 23;
			//결재 시 체크 진행 단계를 보여주는 변수 checkCount++로 하나씩 카운트를 올려준다.
			int checkCount = 1;
			
			// Structure Manager에서 편집중인 BOMWindow 있는지 체크
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking not saved BOMWindow...", false);
			checkBOMEdit();
			progress.setStatus("is done!");
			
			// # 0. FIXED, 2013.06.01, 타겟리스트에서 ECORevision이 떨어진 경우 찾아서 붙여 주고, CreateWorkflow인 경우는 메시징
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking ECO has Workflow...", false);
			checkHasWorkflow();
			progress.setStatus("is done!");
			
			// # 1. ECO EPL GENERATE CHECK
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking ECO EPL generation...", false);
			checkChangeEPL();
			progress.setStatus("is done!");
			
			
			// # 2-1. Cut and Paste 보정
			//SRME::[][20140926] Yun sung won. Cut & paste 시 TC BOM 정보 보정.
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Correcting 'Cut and Paste'...", false);
			cutNpasteCorrecting();		
			progress.setStatus("is done!");
			
			/** 
			 * [SR150213-010][2015.02.25][jclee] EPL에서 특정 FMP 하위 1Lv Part 중 Supply Mode에 P를 포함하는 EPL이 Car Project를 포함하고있지 않을 경우
			 */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking CarProject...", false);
			getCarProjectInEPL();
			progress.setStatus("is done!");

			// [CF-1635] [20201208 by SYChon] 엔진파트 관리를 위한 검증 조건 추가 요청 - 엔진옵션코드 미입력의 경우에도 ECO 상신이 되는 문제 해결
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking S/Mode P7/PD...", false);
			checkSModeP7PD();
			progress.setStatus("is done!");
			
			
			/**
			 * [2015.02.25][jclee] ECO No를 갖고 있으면서 EPL의 New Part No 에 포함되어 있지 않는 Part 목록이 존재할 경우
			 */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Missing parts in the EPL...", false);
			getCANNOTGeneratedList();
			progress.setStatus("is done!");
			
			/**
			 * [20170613][ljg] OrderNo 중복 체크 로직 추가(중복되면 안됨)
			 */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Duplicate Order No...", false);
			duplicateOrderNoCheck();
			progress.setStatus("is done!");

			// # 1.1. EPL VC VALIDATION
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking ECO EPL VC...", false);
			getDuplicateCategoryInVC();
			progress.setStatus("is done!");

			// # 2. ECO EPL VALIDATION
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking ECO EPL...", false);
			checkEPL();
			progress.setStatus("is done!");

			// # 2-2. Function 구성 여부 확인
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking BOM Connection With Function...", false);
//			System.out.println("checkParentConnection Start Time : "+ new Date());
			checkParentConnection();	
//			System.out.println("checkParentConnection End Time : "+ new Date());
			progress.setStatus("is done!");
			
			// # 2-3. 특정프로젝트에 대해 SYSTEM CODE Null Check
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Null Value (System Code)...", false);
			checkNullValue();		
			progress.setStatus("is done!");
			
			// # 3-3. Old Part 의 Revision 정합성 체크
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Old Part Revision...", false);
			checkOldRevIsMatched();
			progress.setStatus("is done!");
			
			// # 4. ECO 작업 내용[C지]을 보고 문제아이템 링크 생성
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Problem Items...", false);
			addProblemItems();
			progress.setStatus("is done!");

			// # 5. ECO 작업 내용[C지]을 보고 솔루션아이템 링크 생성
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Solution Items...", false);
			addSolutionItems();
			progress.setStatus("is done!");
			
			/**
			 * [20160415][jclee] Solution Part List 내 Part 중 Material, Alt Material Part가 Obsolete된 항목이 있을 경우
			 */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Obsoleted Material Part...", false);
			checkObsoleteMaterial();
			progress.setStatus("is done!");

			// # 6. ECO Affected Project 정보 셋팅
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Affected Project...", false);
			setArrectedProject();
			progress.setStatus("is done!");

			// # 3. 결재선 확인
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking approval line...", false);
			checkReviewer();
			// # 3-2.결재선 중복여부 체크
			checkDupApprovalLines();
			progress.setStatus("is done!");

			// # 7. ECO_EPL에 DVP 항목에 DR1,DR2가 하나라도 있으면 인증팀에 참조팀에 인증팀 추가
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking check Certification Team...", false);
			//[SR180130-037][LJG] ECO Validation 인증팀 삭제 -> 주석 처리함
			//[20180206][LJG] 인증팀 필수에서 -> 인증팀은 추가 되면 안되는거로 변경
			checkCertification();
			progress.setStatus("is done!");

			// # 7-1.[SR180207-027][LJG] 원가 기획 팀은 추가되면 안되고, 원가 기술팀만 추가 되어야함
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking check Cost Team...", false);
			checkCostTeam();
			progress.setStatus("is done!");

			// # 8. BOM Structure 상에서 end item 밑에 end item이 존재하면 않된다
			//[20191025] 설계변경 검증 속도 개선 프로젝트에서 제외 (기술관리팀) 
//			progress.setStatus(EVENT_START+"Checking end item to end item relation...", false);
//			checkEndToEnd();
//			progress.setStatus("is done!");

			// ## 프로세스 타겟 설정
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +")Checking targets...", false);
			getTargets();
			progress.setStatus("is done!");

			/*
			 * [20230406][CF-3876]
			 * Vehicle ECO의 EPL Proj속성에 파워트레인 프로젝트가 존재 하는지 체크 
			 * Vehicle ECO(차량 ECO)의 EPL Proj속성에 파워트레인 프로젝트 존재시 오류 처리 
			 * 파워트레인 프로젝트는 Power Traing ECO(엔진 ECO)로 작업 해야한다.*/
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +")Checking Power Train check ...", false);
			checkPowerTrain();
			progress.setStatus("is done!");

			/*
			 *[CF-4217][20230719]ECO 적용 시점 35바이트 이내 작성
			 * 오류 발생 내용 : I/F데이터 생성중 35바이트를 초과하여 에러발생 (PLM최대 입력값(40), IF_LEC_ECO 테이블 컬럼 최대 입력값(35))
			 * 수정 내용 : 결제 시 Change Eff. Point값이 35바이트를 넘어가는 지 체크 하고 35바이트를 넘어갈 시 오류 메시지 발생 
			 * */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +")Change Eff. Point check ...", false);
			checkChangeEffPoint();
			progress.setStatus("is done!");
			
			// ## 상태 변경
			// [SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
			// 2014.10.31 jclee 반영 완료.
			//			progress.setStatus(EVENT_START+"Change Status...", false);
			//			changeStatus();
			//			progress.setStatus("is done!");

			if(parent == null){
				// ## 프로세스 생성
				progress.setStatus(EVENT_START+"Creating process...", false);
				createProcess();
				progress.setStatus("is done!");

				// ## 타스크 할당
				progress.setStatus(EVENT_START+"Assigning...", false);
				assignSignoffs();
				progress.setStatus("is done!");

				// ## 메일 발송 
				progress.setStatus(EVENT_START+"Mailing...", false);
				sendMail();
				progress.setStatus("is done!");
			}
			
			//[20180718][CSH]End Item수가 500개 초과시 HBOM(이광석 차장)에 Mail통보
			//[20191211]한성희 과장은 필요없다고 하네. 주석
//			senMailEndItemCount();

		}catch(Exception e){
			e.printStackTrace();
			if(progress != null){
				progress.setStatus("is fail!");
				progress.setStatus("＠ Error Message : ");
				message = " " + e.getMessage();
				rollback();
			}
		}finally{
			if(progress != null){
				if(message != null){
					progress.setStatus(message);
					progress.close("Error", true, true);
				}else{
					progress.close();
					
					if(parent != null){
						parent.runOperation();
					} else {
						createCompletePopUp();
					}
				}
			}
		}
	}

	/**
	 * [SR141120-043][2014.11.21][jclee] ECO 내역 중 Color ID가 None이 아니면서 Color Section No가 없을 경우 Warning Message Box Open
	 * OK : 결재 진행
	 * Cancel : 결재 진행 취소
	 * @throws Exception
	 */
	private int checkColorID() throws Exception {
		DataSet ds = new DataSet();
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getColorIDWarningList(ds);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sPartNo = result.get("PART_NO");
				String sPartRev = result.get("PART_REV");
				String sColorID = result.get("COLOR_ID");

				sbMessage.append(sPartNo);
				sbMessage.append("/");
				sbMessage.append(sPartRev);
				sbMessage.append(" ");
				sbMessage.append("Color ID : ");
				sbMessage.append(sColorID);
				sbMessage.append("\n");
			}
			sbMessage.append("\n");
			sbMessage.append("These Parts have no Color Section No.\n");
			sbMessage.append("Do you want to proceed?");

			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.YES | SWT.NO);
			box.setText("Ask Proceed");
			box.setMessage(sbMessage.toString());

			return box.open();
		}
		return -1;
	}

	/**
	 * [SR141205-027][2014.12.16][jclee] Color ID가 변경된 항목이 존재할 경우 Warning Message Box Open
	 * OK : 결재 진행
	 * Cancel : 결재 진행 취소
	 * @throws Exception
	 */
	private int checkColorIDChanged() throws Exception {
//		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
//		String sECONo = changeRevision.getProperty("item_id");
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getColorIDChangingList(ds);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sNewPartNo = result.get("NEW_PART_NO");
				String sNewPartRev = result.get("NEW_PART_REV");
				String sOldColorID = result.get("OLD_PART_COLOR_ID");
				String sNewColorID = result.get("NEW_PART_COLOR_ID");

				sbMessage.append(sNewPartNo);
				sbMessage.append("/");
				sbMessage.append(sNewPartRev);
				sbMessage.append(" ");
				sbMessage.append("Color ID : ");
				sbMessage.append(sOldColorID);
				sbMessage.append(" -> ");
				sbMessage.append(sNewColorID);
				sbMessage.append("\n");
			}
			sbMessage.append("\n");
			sbMessage.append("These Parts have changed Color ID.\n");
			sbMessage.append("Do you want to proceed?");

			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.YES | SWT.NO);
			box.setText("Ask Proceed");
			box.setMessage(sbMessage.toString());

			return box.open();
		}
		return -1;
	}

	/**
	 * [SR150213-010][2015.02.25][jclee] EPL에서 특정 FMP 하위 1Lv Part 중 Supply Mode에 P를 포함하는 EPL이 Car Project를 포함하는지 조회
	 * @return
	 * @throws Exception
	 */
	private void getCarProjectInEPL() throws Exception {
//		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
//		String sECONo = changeRevision.getProperty("item_id");
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getCarProjectInEPL(ds);
		int iResult = 0;

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sNewPartNo = result.get("NEW_PART_NO");
				String sNewProjectCode = result.get("NEW_PROJECT");

				if (sNewProjectCode == null || sNewProjectCode.length() == 0 || sNewProjectCode.equals("")) {
					sbMessage.append(sNewPartNo);
					sbMessage.append("\n");
					iResult++;
				}
			}

			if (iResult != 0) {
				sbMessage.append("\n");
				sbMessage.append("These Part's EPLs have no specific Project Codes.\n");
				sbMessage.append("Contact to Administrator.\n");

				throw (new Exception(sbMessage.toString()));
			}
		}
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private void getCANNOTGeneratedList() throws Exception {
//		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
//		String sECONo = changeRevision.getProperty("item_id");
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getCANNOTGeneratedList(ds);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sPartNo = result.get("PART_NO");
				String sPartRev = result.get("PART_REV");

				sbMessage.append(sPartNo).append("/").append(sPartRev);
				sbMessage.append("\n");
			}

//			sbMessage.append("\n");
			sbMessage.append("Check These Part's revision \n");
			sbMessage.append("해당 설변으로 Part가 생성 되었지만 EPL에 포함되어있지 않습니다.\nBOM에 구성되어 있는지 확인하세요.");

//			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//			box.setText("Check EPL");
//			box.setMessage(sbMessage.toString());
//
//			return box.open();
			throw (new Exception(sbMessage.toString()));
		}
//		return -1;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private int selectECOEplCOSModeCompareList() throws Exception {
//		CustomECODao dao = new CustomECODao();
//		String sECONo = changeRevision.getProperty("item_id");
		ArrayList<HashMap<String, String>> results = dao.selectECOEplCOSModeCompareList(ecoNo);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sPartNo = result.get("NEW_PART_NO");
				String sPartRev = result.get("NEW_PART_REV");

				sbMessage.append(sPartNo).append("/").append(sPartRev);
				sbMessage.append("\n");
			}
			sbMessage.append("\n");
			sbMessage.append("Check These Part's S/Mode are changed.\n");
			sbMessage.append("Do you want to proceed?");

			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.YES | SWT.NO);
			box.setText("Ask Proceed");
			box.setMessage(sbMessage.toString());

			return box.open();
		}
		return -1;
	}

	/**
	 * EPL Cut 후 Revise하여 다시 Paste한 경우 확인 (Revise 이력 누락)
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private int getCANNOTGeneratedReviseList() throws Exception {
//		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
//		String sECONo = changeRevision.getProperty("item_id");
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getCANNOTGeneratedReviseList(ds);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sPartNo = result.get("PART_NO");

				sbMessage.append(sPartNo);
				sbMessage.append("\n");
			}

			sbMessage.append("\n");
			//2023-10 조직변경 하드 코딩된 그룹명을 Preference로 변경 
			sbMessage.append("These Parts are omitted from the revise history. Contact to the " + PreferenceService.getValue("RnD MANAGEMENT") + ".");

			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
			box.setText("Check EPL");
			box.setMessage(sbMessage.toString());

			return box.open();
		}
		return -1;
	}

	/**
	 * [20160415][jclee] Solution Part List 내 Part 중 Material, Alt Material Part가 Obsolete된 항목이 있을 경우 Error Message Box Open
	 * @return
	 * @throws Exception
	 */
	private void checkObsoleteMaterial() throws Exception {
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
		TCComponent[] cSolutions = this.changeRevision.getReferenceListProperty("CMHasSolutionItem");

		for (int inx = 0; inx < cSolutions.length; inx++) {
			TCComponent cSolution = cSolutions[inx];
			HashMap<String, String> hmResult = new HashMap<String, String>();

			if (cSolution instanceof TCComponentItemRevision) {
				TCComponentItemRevision irSolution = (TCComponentItemRevision) cSolution;

				if (irSolution.getType().equals("S7_VehpartRevision")) {
					TCComponentContextList materials = irSolution.getRelatedList(new String[] {"s7_MATERIAL", "s7_ALT_MATERIAL"});
					TCComponent[] caMaterials = materials.toTCComponentArray();

					for (int jnx = 0; jnx < caMaterials.length; jnx++) {
						String sMaturity = caMaterials[jnx].getProperty("s7_MATURITY");

						if (sMaturity.equals("Obsolete")) {
							hmResult.put(caMaterials[jnx].getType(), caMaterials[jnx].toString());
						}
					}
				}
			}

			if (!hmResult.isEmpty()) {
				result.put(cSolution.toString(), hmResult);
			}
		}

		StringBuffer sbResult = new StringBuffer();

		Set<String> ksSolution = result.keySet();
		Iterator<String> itSolution = ksSolution.iterator();
		while (itSolution.hasNext()) {
			String sSolution = itSolution.next();

			HashMap<String, String> hmResult = result.get(sSolution);
			Set<String> ksResult = hmResult.keySet();
			Iterator<String> itResult = ksResult.iterator();

			while (itResult.hasNext()) {
				String sKey = itResult.next();

				String sResult = hmResult.get(sKey);

				if (sbResult.length() > 0) {
					sbResult.append("\n");
				}

				sbResult.append(sSolution + " has an OBSOLETED " + sKey + " : " + sResult);
			}
		}

		if (sbResult.length() > 0) {
//			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//			box.setText("Check Part Master");
//			box.setMessage(sbResult.toString());

//			return box.open();
			throw (new Exception(sbResult.toString()));
		}

//		return -1;
	}

	/**
	 * SRME::[][20140926] Yun sung won. Cut & paste 시 TC BOM 정보 보정.
	 * 
	 * @return
	 * @throws Exception 
	 */
	private void cutNpasteCorrecting() throws Exception{

		ChangeToReplace op = new ChangeToReplace(ecoNo);
		try {
			op.execute();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * OrderNo 중복 체크 로직 추가
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 6. 13.
	 * @throws Exception
	 */
	private void duplicateOrderNoCheck() throws Exception{
		DataSet ds = new DataSet();
		ds.put("ecoNo", ecoNo);
		StringBuffer sbMessage = new StringBuffer();

		ArrayList<HashMap<String, Object>> childNodes = dao.duplicateOrderNoCheck(ds);
		if (childNodes.size() > 0) {
			for (int inx = 0; inx < childNodes.size(); inx++) {
				HashMap<String, Object> result = childNodes.get(inx);
				String parent_no = (String)result.get("PARENT_ID");
				String order_no = result.get("ORDER_NO").toString();
				sbMessage.append(parent_no + " : " + order_no);
				sbMessage.append("\n");
			}

			sbMessage.append("Order No. Duplicated\nPlease Contact Your Administrator.\n");
			
			throw (new Exception(sbMessage.toString()));
		}

//		if(CustomUtil.isNullString(sbMessage.toString())){
//			return -1;
//		}else{
//			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//			box.setText("Duplicate Order No.");
//			box.setMessage(sbMessage.toString());
//			return box.open();
//		}
	}
//	private int duplicateOrderNoCheck() throws Exception{
////		CustomECODao dao = new CustomECODao();
//		DataSet ds = new DataSet();
//		ds.put("ecoNo", changeRevision.getItem().getStringProperty("item_id"));
//		ArrayList<String> eco_epl = dao.getECOEPL(ds);
//		ds.clear();
//		StringBuffer sbMessage = new StringBuffer();
//
//		for(int i=0; i<eco_epl.size(); i++){
//			ds.clear();
//			ds.put("ITEM_ID", eco_epl.get(i));
//			ArrayList<HashMap<String, Object>> childNodes = dao.duplicateOrderNoCheck(ds);
//			if (childNodes.size() > 0) {
//				for (int inx = 0; inx < childNodes.size(); inx++) {
//					HashMap<String, Object> result = childNodes.get(inx);
//					String parent_no = (String)result.get("PARENT_ID");
//					String order_no = result.get("ORDER_NO").toString();
//					sbMessage.append(parent_no + " : " + order_no);
//					sbMessage.append("\n");
//				}
//
//				sbMessage.append("Order No. Duplicated\nPlease Contact Your Administrator.\nTel : 3176\n");
//			}
//		}
//
//		if(CustomUtil.isNullString(sbMessage.toString())){
//			return -1;
//		}else{
//			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//			box.setText("Duplicate Order No.");
//			box.setMessage(sbMessage.toString());
//			return box.open();
//		}
//	}	

	/**
	 * ECO를 참조하고 있는 모든 Part가 BOM에서 Function 하위에 구성되어있는지 확인
	 * @throws Exception
	 */
	private void checkParentConnection() throws Exception {
		try {
			DataSet ds = new DataSet();
			ds.put("ecoNo", ecoNo);
			
			ArrayList<HashMap<String, Object>> resultList = dao.notConnectedFunctionList(ds);
			if (resultList.size() > 0) {
				StringBuffer sbMessage = new StringBuffer();
				for (int inx = 0; inx < resultList.size(); inx++) {
					HashMap<String, Object> result = resultList.get(inx);
					String funcConnYn = result.get("FUNC_CONN_YN").toString();
					
					if(funcConnYn.equals("0")){
						String partNo = result.get("PART_NO").toString();
						String partRev = result.get("PART_REV").toString();
						sbMessage.append(partNo + "/" + partRev);
						sbMessage.append("\n");
					}
				}
				if(sbMessage.length() > 0){
					throw (new Exception(sbMessage.toString() + " is not connected with Function.\nPlease check the BOM Structure."));
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}
//	private void checkParentConnection() throws Exception {
//		try {
//			// 1. 현재 ECO를 참조로 하는 모든 Part를 가져온다. 
//			AIFComponentContext[] accReferenceds = changeRevision.whereReferenced();
//
//			// 2. 모든 Part에 대해 사용되는 Part를 상위로 찾아가 Function이 붙어있는지 확인
//			for (int inx= 0; inx < accReferenceds.length; inx++) {
//				AIFComponentContext accReferenced = accReferenceds[inx];
//				if (accReferenced.getComponent() instanceof TCComponentItemRevision) {
//					TCComponentItemRevision itemRevision = (TCComponentItemRevision) accReferenced.getComponent();
//					String[] sItemTypes = new String[] {SYMCClass.S7_VEHPARTREVISIONTYPE, SYMCClass.S7_STDPARTREVISIONTYPE, SYMCClass.S7_FNCMASTPARTREVISIONTYPE, SYMCClass.S7_FNCPARTREVISIONTYPE};
//					String sReferenceItemRevisionType = itemRevision.getProperty("object_type");
//					// 2.0. 현재 Part의 참조 Part가 Functoin, FMP, Vehicle Part, Standard Part 이외 다른 Part의 경우 건너뜀.
//					// (Concurrent ECO를 걸러내기 위함)
//					if (!(sReferenceItemRevisionType.equals(sItemTypes[0]) || 
//							sReferenceItemRevisionType.equals(sItemTypes[1]) ||
//							sReferenceItemRevisionType.equals(sItemTypes[2]) || 
//							sReferenceItemRevisionType.equals(sItemTypes[3]))
//							) {
//						continue;
//					}
//
//					// 2.1. 현재 Part의 상위 Part 중 Function, FMP가 있을 경우 다음 Part로 넘어감.
//					boolean isConnectWithFunction = false;
//
//					if (sReferenceItemRevisionType.equals(SYMCClass.S7_FNCPARTREVISIONTYPE) || sReferenceItemRevisionType.equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE)) {
//						isConnectWithFunction = true;
//					}
//
//					if (!isConnectWithFunction) {
//						isConnectWithFunction = findFunction(itemRevision);
//					}
//
//					// 3. ECO를 참조로 하는 Part 중 단 하나의 Part라도 모든 상위 Part중 Function이 없을 경우 Exception 발생.
//					if (!isConnectWithFunction) {
//						throw (new Exception(itemRevision.getProperty("item_id") + "/" + itemRevision.getProperty("item_revision_id") + " is not connected with Function.\nPlease check the BOM Structure."));
//					}
//				}
//			}
//		} catch (Exception e) {
//			throw e;
//		}
//	}

	/**
	 * 상위 Function이 있는지 Recursive로 확인.
	 * @param itemRevision
	 * @return
	 */
	private boolean findFunction(TCComponentItemRevision itemRevision) {
		try {
			TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(session, "Latest Working");
			TCComponent[] usedComponents = itemRevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);

			// 현재 Part의 상위 Part 중 1Lv 상위 Used 중 Function Master Part가 있을 경우 다음 Part로 넘어가기 위해 Return
			if (usedComponents.length > 0) {
				for (int jnx = 0; jnx < usedComponents.length; jnx++) {
					if (usedComponents[jnx] instanceof TCComponentItemRevision) {
						TCComponentItemRevision usedComponent = (TCComponentItemRevision) usedComponents[jnx];
						if (usedComponent.getProperty("object_type").equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE)) {
							return true;
						} else {
							// 현재 1Lv 하위 Part가 Function이 아닌 경우 다시 Function 찾으러 올라감.
							boolean isFindFunction = findFunction(usedComponent);
							if (isFindFunction) {
								return true;
							} else {
								continue;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			return false;
		}

		return false;
	}

	// 타겟리스트에서 ECORevision이 떨어진 경우 찾아서 붙여 주고, CreateWorkflow인 경우는 메시징
	private void checkHasWorkflow() throws Exception {
		if(parent == null){
			// Workflow 검색
			if(!dao.workflowCount(ecoNo).equals("0")){
				throw (new Exception("Workflow has been created already.\nCheck the task to perfrom folder in My Worklist, and please proceed by approval."));
			}
		}else{
			// FIXED, 2013.06.01, DJKIM, 타겟리스트에서 ECORevision이 떨어진 경우 찾아서 붙여 줌.
			if(changeRevision == null){
				String processName = parent.getCurrentJob().getName();
				String ecoNo = processName.substring(processName.indexOf("[")+1, processName.indexOf("]"));
				String ecoRevisionPuid = dao.getEcoRevisionPuid(ecoNo);
				changeRevision = (TCComponentChangeItemRevision) session.stringToComponent(ecoRevisionPuid);
				// 20130611, 김대중C, 워크플로우 재상신 할 때 타겟이 전부 떨어지는 경우에 eco revision을 찾아서 붙여주는 로직 
				parent.getCurrentJob().getRootTask().add("root_target_attachments", changeRevision); 
			}
		}
	}

	private void createCompletePopUp() {
		final Shell shell = AIFUtility.getActiveDesktop().getShell();

		shell.getDisplay().syncExec(new Runnable()
		{

			public void run()
			{
				org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				box.setText("Information");
				box.setMessage("결재 요청이 완료되었습니다");
				box.open();
			}

		});
	}

	/**
	 * 오류 발생 시 상신 시점 초기화
	 * @throws Exception
	 */
	private void rollback() {
		try{
			changeRevision.setProperty("s7_AFFECTED_PROJECT", dao.getAffectedProject(ecoNo));
			// SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
			dao.updateEcoStatus(changeRevision.getUid(), "In Work", "In Work");

			problemList = changeRevision.getRelatedComponents(SYMCECConstant.PROBLEM_REL);
			if(problemList != null && problemList.length > 0)
				changeRevision.remove(SYMCECConstant.PROBLEM_REL, problemList);

			solutionList = changeRevision.getRelatedComponents(SYMCECConstant.SOLUTION_REL);
			for(TCComponent solutionItemComponent : solutionList){
				TCComponentItemRevision solutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
				ArrayList<TCComponent> solutionDatasetList = ProcessUtil.getDatasets(solutionItemrevision, "IMAN_specification");
				for(TCComponent dataset : solutionDatasetList){
					if(ProcessUtil.isWorkingStatus(dataset) && !dataset.getType().equals("PDF")){
						if(!solutionItemrevision.getType().equals("S7_StdpartRevision")){
							dataset.setProperty("s7_ECO_NO", "");
						}
					}
				}
			}

			if(solutionList != null && solutionList.length > 0)
				changeRevision.remove(SYMCECConstant.SOLUTION_REL, solutionList);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * ECO Affected Project 정보 셋팅
	 * @throws Exception
	 */
	private void setArrectedProject() throws Exception {
		String affecedProject = "";
		String affecedProjects = dao.getAffectedProject(ecoNo);
		if(affecedProjects != null){
			String[] affProjs = affecedProjects.split(",");
			ArrayList<String> affecedProjectList = new ArrayList<String>();
			for(String affProj : affProjs){
				if(!affecedProjectList.contains(affProj))
					affecedProjectList.add(affProj);
			}

			for(String affProj : affecedProjectList){
				if(affecedProject.equals("")){
					affecedProject = affProj;
				}else{
					affecedProject = affecedProject+","+affProj;
				}
			}
		}
		changeRevision.setProperty("s7_AFFECTED_PROJECT", affecedProject);
	}

	/**
	 * 상태 변경
	 * IitemRevision의 Maturity와
	 * EcoRevision의 Eco Maturity를 업데이트 함.
	 * SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
	 * ==> java에서 maturity 변경 하지 않음. 따라서 method 주석 처리 함. 
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void changeStatus() throws Exception {
		boolean isok = dao.updateEcoStatus(changeRevision.getUid(), "In Review1", "Processing");
		if(!isok){
			throw (new Exception("changeStatus() Method Error"));
		}
	}

	/**
	 * ECO 작업 내용[C지]을 보고 문제아이템 링크 생성
	 * @throws Exception
	 */
	private void addProblemItems() throws Exception {
		// 중복 방지 삭제
		problemList = changeRevision.getRelatedComponents(SYMCECConstant.PROBLEM_REL);
		if(problemList != null && problemList.length > 0)
			changeRevision.remove(SYMCECConstant.PROBLEM_REL, problemList);

		ArrayList<String> resultList = dao.getProblemItems(ecoNo);
		if(resultList!=null && resultList.size() > 0){
			int resultCount = resultList.size();
			String[] itemRevisions = new String[resultCount];
			for(int i = 0 ; i < resultCount ; i++){
				itemRevisions[i] = resultList.get(i);
				//System.out.println(resultList.get(i));
			}
			TCComponent[] tcComponents = session.stringToComponent(itemRevisions);
			if(tcComponents.length > 0)
				changeRevision.add(SYMCECConstant.PROBLEM_REL, tcComponents);
		}
	}

	/**
	 * ECO 작업 내용[C지]을 보고 솔루션아이템 링크 생성
	 * @throws Exception
	 */
	private void addSolutionItems() throws Exception {
		// 중복 방지 삭제
		solutionList = changeRevision.getRelatedComponents(SYMCECConstant.SOLUTION_REL);
		if(solutionList != null && solutionList.length > 0)
			changeRevision.remove(SYMCECConstant.SOLUTION_REL, solutionList);
		/**
		 * Part 가 다른 ECO에 연결되어있는지 체크함
		 */
		checkRefEcoFromPart();

		ArrayList<String> resultList = dao.getSolutionItems(ecoNo);
		if(resultList!=null && resultList.size() > 0){
			int resultCount = resultList.size();
			String[] itemRevisions = new String[resultCount];
			for(int i = 0 ; i < resultCount ; i++){
				itemRevisions[i] = resultList.get(i);
			}
			TCComponent[] tcComponents = session.stringToComponent(itemRevisions);
			if(tcComponents.length > 0)
				changeRevision.add(SYMCECConstant.SOLUTION_REL, tcComponents);
		}
		
		//[csh 20180424] 결재 재상신 시 check out list 및 dataset에 eco no 미입력됨 방지.
		solutionList = changeRevision.getRelatedComponents(SYMCECConstant.SOLUTION_REL);
		
	}

	/**
	 * ECO EPL 정상 생성 확인
	 * 재생성 된 경우 ECO EPL 확인 통지 메시지
	 * @throws Exception
	 */
	private void checkChangeEPL() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("ecoNo", ecoNo);
		Boolean result = (Boolean)remote.execute("com.ssangyong.service.ECOHistoryService", "isECOEPLChanged", ds);
		if(result.booleanValue()) {
			/**
			 * [SR없음][2015.04.27][jclee] ECO Generate 시 모든 EPL 추출내역을 삭제한 후 Regenerate
			 */
			//        	remote.execute("com.ssangyong.service.ECOHistoryService", "extractEPL", ds);
			remote.execute("com.ssangyong.service.ECOHistoryService", "generateECO", ds);

			progress.close();
			if(parent != null){
				try {
					parent.setVisible(false);
					parent.disposeDialog();
					session.getUser().getUserInBox().refresh();
				} catch (TCException e1) {
					e1.printStackTrace();
				}
			}
			final Shell shell = AIFUtility.getActiveDesktop().getShell();

			shell.getDisplay().syncExec(new Runnable()
			{

				public void run()
				{
					org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.CLOSE);
					box.setText("Information");
					box.setMessage("ECO EPL is auto generated. \nPlease, Check the ECO EPL again.");
					box.open();
				}

			});
			throw (new Exception("Please check the ECO EPL."));
		}
	}

	/**
	 * ECO EPL VC 체크
	 * #1. Option 내 동일한 Category 내 옵션이 두개 이상 선택된 경우가 있는지 확인.
	 * @throws Exception 
	 */
	private void getDuplicateCategoryInVC() throws Exception{
		final String sMessage = dao.getDuplicateCategoryInVC(ecoNo);

		if(sMessage != null && sMessage.length() > 0){
			progress.close();

			final Shell shell = AIFUtility.getActiveDesktop().getShell();

			shell.getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					SYMCAbstractDialog dialog = new SYMCAbstractDialog(shell) {
						@Override
						protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
							getShell().setText("ECO EPL VC Validation Result");

							Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
							composite.setLayout(new GridLayout());

							Text txtMessage = new Text(composite, SWT.MULTI);
							txtMessage.setLayoutData(new GridData(GridData.FILL_BOTH));
							txtMessage.setEditable(false);
							txtMessage.setText(sMessage);

							return composite;
						}

						@Override
						protected boolean validationCheck() {
							return true;
						}

						@Override
						protected boolean apply() {
							return false;
						}

						@Override
						protected void okPressed() {
							this.close();
						}
					};

					dialog.setApplyButtonVisible(false);

					dialog.open();
				}

			});
			throw (new Exception("Please check the ECO EPL."));
		}
	}

	/**
	 * ECO EPL 체크
	 * #1. Function Master와 Vehicle Part 인 경우 신규 등록 아이템 중 ECO 번호가 현재 ECO 인지 확인
	 * #2. Standard Part와 Vehicle Part 인 경우 SUPPLY MODE가 누락 되었는지 확인.
	 * #3. IC(호환성), 재고(PLT/AS) 확인
	 * 오류 발생 시 테이블 형태로 나타내 준다.
	 * @throws Exception 
	 */
	private void checkEPL() throws Exception{
		final ArrayList<HashMap<String, String>> resultList = dao.checkECOEPL(ecoNo);

		if(resultList != null && resultList.size() > 0){
			progress.close();

			final Shell shell = AIFUtility.getActiveDesktop().getShell();

			shell.getDisplay().syncExec(new Runnable()
			{

				public void run()
				{
					ValidationResultDialog dialog = new ValidationResultDialog(shell, SWT.NONE, ecoNo, resultList);
					dialog.open();

				}

			});
			throw (new Exception("Please check the ECO EPL."));
		}
	}
	
	/**
	 * Null Value 체크
	 * #1. BI 속성 (System Code / NMCD)이 특정 프로젝트에서 Null 이면 안됨.
	 * 오류 발생 시 테이블 형태로 나타내 준다.
	 * @throws Exception 
	 */
	private void checkNullValue() throws Exception{
		String[] checkedProject = PreferenceService.getValues(TCPreferenceService.TC_preference_site, "SYMC_ECO_VAL_PRJ");
		if( checkedProject != null ){
			DataSet ds = new DataSet();
			ArrayList<String> projectCodeList = new ArrayList<String>();
			for( int i =0; i < checkedProject.length ; i++){
				projectCodeList.add(checkedProject[i]);
			}
			ds.put("PRJ_CD", projectCodeList);
			ds.put("ECO_NO", ecoNo);
//			ds.put("ECO_NO", "21BH006");
			
			ArrayList<HashMap<String, String>> resultList = dao.getEcoNullValueList(ds);
			
			if(resultList != null && resultList.size() > 0){
				String parent_no = "";
				String child_no = "";
				String seq = "";
				progress.setStatus("");
				for (HashMap<String, String> map : resultList) {
					parent_no = map.get("PARENT_NO");
					child_no = map.get("NEW_PART_NO");
					seq = map.get("NEW_SEQ");
		            progress.setStatus("    PARENT_NO : " + parent_no + ", NEW_PART_NO : " + child_no + ", NEW_SEQ : " + seq);
				}
				throw (new Exception("Please check the BI Null Value (System Code)."));
			}
		}
	}

	/**
	 * BOM Structure 상에서 end item 밑에 end item이 존재하면 않된다
	 * @throws TCException
	 * @throws Exception
	 */
	private void checkEndToEnd() throws TCException, Exception {
		ArrayList<String> resultList = dao.checkEndtoEnd(ecoNo);
		if(resultList !=null && resultList.size() > 0){
			String list = "";
			for(String result : resultList){
				list += result + "\n";
			}
			throw (new Exception("Cannot make end item to end item structure.\nCheck belows and fix it.\n"+list));
		}
	}

	/**
	 * 결재선이 템플릿에 맞게 구성 되어 있는지 확인
	 * @return
	 * @throws Exception
	 */
	private void checkReviewer() throws Exception {
		ApprovalLineData theLine = new ApprovalLineData();
		theLine.setEco_no(changeRevision.getProperty("item_id"));

		// 재상신 시에서는 결재선 체크 구분
		ArrayList<ApprovalLineData> paramList = null;
		if(parent == null){
			//결재선 정보 쿼리
			paramList = dao.getApprovalLine(theLine);
		}else{
			TCComponentTask rootTask = changeRevision.getCurrentJob().getRootTask();
			TCComponentTask[] subTasks = rootTask.getSubtasks();
			paramList = new ArrayList<ApprovalLineData>();

			for(TCComponentTask subTask : subTasks) {
				if(subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) 
						|| subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)){
					if(!subTask.getName().equals("Creator")){
						TCComponentTask performSignoffTask = subTask.getSubtask("perform-signoffs");
						TCComponentSignoff[] signoffs = performSignoffTask.getValidSignoffs();
						if(signoffs.length > 0) {

							for(TCComponentSignoff signoff : signoffs) {
								signoff.refresh();
								TCComponentGroupMember groupMember = signoff.getGroupMember();
								String[] groupMemberProperties = groupMember.getProperties(new String[]{"the_group","the_user"});
								ApprovalLineData approvalLine = new ApprovalLineData();
								approvalLine.setTask(subTask.getName());
								approvalLine.setTeam_name(groupMemberProperties[0]);
								approvalLine.setUser_name(groupMemberProperties[1]);
								approvalLine.setTc_member_puid(groupMember.getUid());
								paramList.add(approvalLine);
							}
						}
					}
				}
			}
		}

		if(paramList == null || paramList.size() < 1) {
			throw (new Exception("Cannot find approval line.\nPlease check the approval line."));
		}

		//타스크별 TCComponentGroupMember생성 맵핑 및 필수 지정 결재선 확인
		ArrayList<String> requiredAssingTask = new ArrayList<String>();
		String[] taskList = SYMCECConstant.ECO_TASK_LIST;
		for(String task : taskList){
			if(task.equals("Sub-team Leader") || task.equals("Reference Department")) continue;
			requiredAssingTask.add(task);
		}

		String mapTask = "";
		TCComponentGroupMember groupMember = null;
		ArrayList<TCComponentGroupMember> groupMemberList = null;

		for(ApprovalLineData map : paramList){
			mapTask = map.getTask();

			// FIXED 2013.05.14, DJKIM, 박수경 CJ: 사용자의 상태 변경이 발생 할수 있으므로 사용자 상태 확인하여 부적절한 사용자가 결재선에 할당 되지 않도록 함.
			try{
				groupMember =  (TCComponentGroupMember) session.stringToComponent(map.getTc_member_puid());
			}catch(Exception e){
				throw (new Exception("Cannot find group member.\n"+map.getUser_name() + " in " + map.getTeam_name() + " is removed. \nPlease check and replace him."));
			}
			if(groupMember.getMemberInactive()){
				throw (new Exception("Group member is in inactive status.\n"+map.getUser_name() + " in " + map.getTeam_name() + " is in inactive status. \nPlease check and replace him."));
			}
			if(!groupMember.getUser().isValid()){
				throw (new Exception("User is in inactive status.\n"+map.getUser_name() + " in " + map.getTeam_name() + " is in inactive status. \nPlease check and replace him."));
			}
			if(reviewers.containsKey(mapTask)){
				groupMemberList = reviewers.get(mapTask);
				groupMemberList.add(groupMember);
			}else{
				groupMemberList = new ArrayList<TCComponentGroupMember>();
				groupMemberList.add(groupMember);
			}
			reviewers.put(mapTask, groupMemberList);
			if(requiredAssingTask.contains(mapTask)) 
				requiredAssingTask.remove(mapTask);
		}

		if(requiredAssingTask.size() > 0){
			String addTasks = "";
			for(String requiredAssingTaskName : requiredAssingTask){
				addTasks = addTasks+requiredAssingTaskName+"\n";
			}
			throw (new Exception("Workflow task checking information.\nPlease, Add the following tasks.\n"+addTasks));
		}

		// FIXED 결재선에 원가[COST_ENGINEER], 기술관리[BOMADMIN], 팀장롤[TEAM_LEADER]이 하나 이상인지 체크 함.
		PreferenceService.createService(session);
		String checkRole = null;
		// [20161117][ymjang] CM으로 시작되는 ECO 는 S201 인도 마힌드라 ECO 로서 원가는 제외함.  
		if (theLine.getEco_no().startsWith("CM")) {
			checkRole = PreferenceService.getValue("SYMC_ECO_WF_CHECK_ROLE_S201"); // BOMADMIN,TEAM_LEADER
		} else {
			checkRole = PreferenceService.getValue("SYMC_ECO_WF_CHECK_ROLE"); // COST_ENGINEER,BOMADMIN,TEAM_LEADER
		}
		if(checkRole.equals("")){
			checkRole = "COST_ENGINEER,BOMADMIN,TEAM_LEADER";
		}
		String[] checkRoles = checkRole.split(",");
		ArrayList<String> checkRoleList = new ArrayList<String>();
		for(String role : checkRoles){
			if(!role.equals("")){
				checkRoleList.add(role.trim());
			}
		}
		Object[] tasks = reviewers.keySet().toArray();
		for(Object task : tasks){
			groupMemberList = reviewers.get(task+"");
			String roleName = "";
			for(TCComponentGroupMember member : groupMemberList){
				roleName = member.getRole().getProperty("role_name");
				if(checkRoleList.contains(roleName)){
					checkRoleList.remove(roleName);
				}
			}
		}

		if(checkRoleList.size() > 0){
			String addRoles = "";
			for(String addRole : checkRoleList){
				addRoles = addRoles+addRole+"\n";
			}
			throw (new Exception("Workflow role checking information.\nPlease, Add someone with the following roles.\n"+addRoles));
		}
	}

	/**
	 * [20180206][LJG] 인증팀 필수에서 -> 인증팀은 추가 되면 안되는거로 변경
	 * 신규 파트중 EPL의 Category가 DR1/2로 지정된 파트가 존재 하면 인증팀이 필수로 지정되어야 함.
	 * 2013.01.10
	 * REQ. 송대영
	 * REF. 정상일
	 * @return
	 * @throws Exception
	 */
	//    private void checkCertification() throws Exception {
	//        boolean hasCertificationPart = false;
	//        //CM일 경우 인증팀 체크 Pass
	//        String ecoId = changeRevision.getProperty(IPropertyName.ITEM_ID);
	//        if(ecoId.startsWith("CM"))
	//        	return;
	//        
	//        solutionList = changeRevision.getRelatedComponents(SYMCECConstant.SOLUTION_REL);
	//        problemList = changeRevision.getRelatedComponents(SYMCECConstant.PROBLEM_REL);
	//        
	//        ArrayList<String> checkCategory = new  ArrayList<String>();
	//        checkCategory.add("DR1");
	//        checkCategory.add("DR2");
	//        
	//    	for(TCComponent solutionItemComponent : solutionList){
	//    		TCComponentItemRevision solutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
	//    		if(checkCategory.contains(solutionItemrevision.getProperty("s7_REGULATION"))){
	//    			hasCertificationPart = true;
	//    			break;
	//    		}
	//    	}
	//        
	//    	boolean hasCerfiticationTeam = false;
	//        if(hasCertificationPart){
	//        	PreferenceService.createService(session);
	//        	String theTask = "Reference Department";
	//        	String certificationTeam = PreferenceService.getValue("SYMC_ECO_Certification_Team");
	//        	ArrayList<TCComponentGroupMember> memberList = reviewers.get(theTask);
	//        	
	//        	if(memberList == null){
	//        		throw (new Exception("Certification part is exist.\nCertification team must be added in workflow[Reference Department]."));
	//        	}
	//        	
	//        	for(TCComponentGroupMember member : memberList){
	//        		if(member.getGroup().getGroupName().equals(certificationTeam)){
	//        			hasCerfiticationTeam = true;
	//        			break;
	//        		}
	//        	}
	//        	
	//        	if(!hasCerfiticationTeam){
	//        		throw (new Exception("Certification part is exist.\nCertification team must be added in workflow[Reference Department]."));
	//        	}
	//        }
	//    }

	/**
	 * [20180206][LJG] 인증팀 필수에서 -> 인증팀은 추가 되면 안되는거로 변경
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 2. 6.
	 * @throws Exception
	 */
	private void checkCertification() throws Exception {
		boolean hasCertificationPart = false;
		PreferenceService.createService(session);
		String theTask = "Reference Department";
		String certificationTeam = PreferenceService.getValue("SYMC_ECO_Certification_Team");
		String certificationTeam1 = "VEHICLE CERTIFICATION";
		ArrayList<TCComponentGroupMember> memberList = reviewers.get(theTask);

		if(memberList != null && memberList.size() > 0){
			for(TCComponentGroupMember member : memberList){
				if(member.getGroup().getGroupName().equalsIgnoreCase(certificationTeam) || member.getGroup().getGroupName().equalsIgnoreCase(certificationTeam1)){
					hasCertificationPart = true;
					break;
				}
			}
		}

		if(hasCertificationPart){
			throw (new Exception("Certification part is exist.\n"+certificationTeam+" team must be removed in workflow[Reference Department]."));
		}
	}
	
	/**
	 * [SR180207-027][LJG] 원가기획팀은 추가되면 안되고, 원가기술팀만 추가 되어야함
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 2. 7.
	 * @throws Exception
	 */
	private void checkCostTeam() throws Exception {
		boolean hasCostTeam = false;
		PreferenceService.createService(session);
		String theTask = "Related Team Review";
		String costTeam = PreferenceService.getValue("SYMC_ECO_Cost_Team");
		ArrayList<TCComponentGroupMember> memberList = reviewers.get(theTask);

		if(memberList != null && memberList.size() > 0){
			for(TCComponentGroupMember member : memberList){
				if(member.getGroup().getGroupName().equalsIgnoreCase(costTeam)){
					hasCostTeam = true;
					break;
				}
			}
		}

		if(!hasCostTeam){
			throw (new Exception("Cost part is wrong.\n"+costTeam+" team must be added in workflow[Related Team Review]."));
		}
	}

	/**
	 * 결재 타켓 확인
	 * check out 여부도 같이 확인
	 * dataset 속성에 eco no도 입력
	 * @throws TCException
	 */
	private void getTargets() throws TCException, Exception{

		// FIXED, 2013.05.20, DJKIM, 반려 일 경우 일부 데이터 셋 누락 발생. 반려시 재작업 시 타겟을 미리 제거[데이터 셋이 작업 중인지 확인 하는 부분에 체크 되지 않도록]
		TCComponentProcess process = null;
		TCComponentTask rootTask = null;

		if(parent != null){
			process = changeRevision.getCurrentJob();
			rootTask = process.getRootTask();

			// 중복 방지 삭제
			TCComponent[] oldTargetList = rootTask.getRelatedComponents("root_target_attachments");
			if(oldTargetList != null && oldTargetList.length > 0){
				rootTask.remove("root_target_attachments", oldTargetList);
			}
		}

		String checkOutlist = "";
		// FIXED, 2013.06.01, DJKIM 하위 구조가 있는지 없는지 체크
		String noChildrenBVRList = "";
		targetList.add((TCComponent)changeRevision);
		ArrayList<TCComponent> datasetList = ProcessUtil.getDatasets(changeRevision, "IMAN_specification");
		for(TCComponent dataset : datasetList){
			if(dataset.isCheckedOut()){
				checkOutlist = checkOutlist + dataset + "\n";
			}else{
				//FIXED, 2013.06.12, YUNJAE, only adding Working dataset
				if(ProcessUtil.isWorkingStatus(dataset) && !dataset.getType().equals("PDF")) {
					//    				[SR140702-056][20140422] modified yunjae
					//    				targetList.add(dataset);
				}
			}
		}
		

		for(TCComponent solutionItemComponent : solutionList){
			TCComponentItemRevision solutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
			if(solutionItemrevision.isCheckedOut()){
				checkOutlist = checkOutlist + solutionItemrevision + "\n";
			}else{
				//    			[SR140702-056][20140422] modified yunjae
				//    			targetList.add(solutionItemrevision);
			}

			TCComponentBOMViewRevision view = (TCComponentBOMViewRevision) solutionItemrevision.getRelatedComponent("structure_revisions");
			if(view != null){
				if(view.isCheckedOut()){
					checkOutlist = checkOutlist + view + "\n";
				}else{
					// FIXED, 2013.06.01, DJKIM 하위 구조가 있는지 없는지 체크
					// [SR없음][20150727][jclee]FMP의 경우 임시 사양을 모두 제거하는 경우가 있으므로 하위구조가 없더라도 결재진행 허용
					if(dao.childrenCount(view.getUid()).equals("0") && !solutionItemrevision.getType().equals("S7_FunctionMastRevision")){
						noChildrenBVRList = noChildrenBVRList + solutionItemrevision + "\n";
					}else{
						//        				[SR140702-056][20140422] modified yunjae
						//        				targetList.add(view);
					}
				}
			}

			ArrayList<TCComponent> solutionDatasetList = ProcessUtil.getDatasets(solutionItemrevision, "IMAN_specification");
			for(TCComponent dataset : solutionDatasetList){
				if(ProcessUtil.isWorkingStatus(dataset) && !dataset.getType().equals("PDF")){
					if(dataset.isCheckedOut()){
						checkOutlist = checkOutlist + dataset + "\n";
					}else{
						if(!solutionItemrevision.getType().equals("S7_StdpartRevision")){
							dataset.setProperty("s7_ECO_NO", ecoNo);
						}
						//            			[SR140702-056][20140422] modified yunjae
						//            			targetList.add(dataset);
					}
				}
			}
		}

		String retrunMessage = "";
		if(!checkOutlist.equals("")){
			retrunMessage = "Check-out Componet is exist.\nCheck belows and fix it.\n"+checkOutlist;
		}

		// FIXED, 2013.06.01, DJKIM 하위 구조가 있는지 없는지 체크
		if(!noChildrenBVRList.equals("")){
			retrunMessage = retrunMessage + "\nThe part that does not have a sub-structure exists.\nCheck belows and BOMViewResion and CATProduct Dataset.\n"+noChildrenBVRList;
		}

		// 반려 일 경우  타켓 재설정
		if(parent != null && rootTask != null){   		
			// 타겟 재설정
			rootTask.add("root_target_attachments", targetList);
		}
		
		// [CSH]위치변경 >> 타겟 재설정 보다 Exception 발생이 먼저 일어나면 ECO도 target에서 떨어져나감.   
		if(!retrunMessage.equals("")){
			throw (new TCException(retrunMessage));
		}
	}

	//[SR170828-015][LJG]Chassis module 관리를 위한 검증 조건 추가 요청
	// 1. ECO 내의 Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
	// 2. Module code : FCM or RCM
	// 3. Part의 Option : Z999을 포함하는 경우
	private int checkChassisModule() throws Exception{
		String eco_no = changeRevision.getProperty("item_id");
		ArrayList<String> resultList = dao.checkChassisModule(eco_no);
		StringBuffer sbMessage = new StringBuffer();
		if(resultList !=null && resultList.size() > 0){
			for(String result : resultList){
				sbMessage.append(" → " + result + "\n");
			}
		}

		if(CustomUtil.isNullString(sbMessage.toString())){
			return -1;
		}else{
			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
			box.setText("Information");
			box.setMessage("Check Chassis Module Error.\nCheck belows and fix it.\n" + sbMessage.toString());
			return box.open();
		}
	}
	
	// Structure Manager에서 편집중인 BOMWindow 있는지 체크
	private void checkBOMEdit() throws Exception{
		List<AbstractAIFUIApplication>  applications = AIFDesktop.getActiveDesktop().getApplications();
		
		if(applications.size() > 0){
			StringBuffer sb = new StringBuffer();
			for(AbstractAIFUIApplication application : applications){
				if(application.getPerspectiveDef().getLabel().equals("Structure Manager")){
					PSEApplicationService pseApp = (PSEApplicationService) application;
					if(pseApp.getBOMWindow() != null && pseApp.getBOMWindow().isModified()){
//						org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//						box.setText("BOM Edit Check");
//						box.setMessage("편집중인 BOMWindow가 존재합니다.\nStructure Manager에서 수정중인 내용을 저장 후 진행하세요.\nTopLine : " + pseApp.getBOMWindow().getTopBOMLine().toString());
//						return box.open();
						sb.append("편집중인 BOMWindow가 존재합니다.\nStructure Manager에서 수정중인 내용을 저장 후 진행하세요.\nTopLine : " + pseApp.getBOMWindow().getTopBOMLine().toString());
						break;
					}
					if(application.isDirty()){
//						org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//						box.setText("BOM Edit Check");
//						box.setMessage("편집중인 BOMWindow가 존재합니다.\nStructure Manager에서 수정중인 내용을 저장 후 진행하세요.");
//						return box.open();
						sb.append("편집중인 BOMWindow가 존재합니다.\nStructure Manager에서 수정중인 내용을 저장 후 진행하세요.");
						break;
					}
				}
			}
			
			if(sb.length() >0)
				throw (new Exception(sb.toString()));
			}
		
	}

	/**
	 * 프로세스 생성
	 * @throws Exception
	 */
	private void createProcess() throws Exception{

		TCComponentTaskTemplateType compTaskTmpType = (TCComponentTaskTemplateType)session.getTypeComponent("EPMTaskTemplate");
		TCComponentTaskTemplate template = compTaskTmpType.find(SYMCECConstant.ECO_PROCESS_TEMPLATE, 0);

		TCComponentProcessType processtype = (TCComponentProcessType)session.getTypeComponent("Job");

		TCComponent[] componentList = new TCComponent[targetList.size()];
		for(int i = 0 ; i < targetList.size() ; i++){
			componentList[i] = targetList.get(i);
		}

		newProcess = (TCComponentProcess)processtype.create("ECO [" + changeRevision.getProperty("item_id") + "] approval request.", "Please confirm ASAP.", template, componentList, ProcessUtil.getAttachTargetInt(componentList));
	}

	/**
	 * 결재선 할당
	 * @throws TCException
	 */
	private void assignSignoffs() throws Exception {

		TCComponentTask rootTask = newProcess.getRootTask();
		TCComponentTask[] subTasks = rootTask.getSubtasks();
		TCComponentSignoffType signoffType = (TCComponentSignoffType) session.getTypeComponent("Signoff");
		for(TCComponentTask subTask : subTasks) {
			String reviewTaskName = subTask.getName();
			TCComponentTask selectSignoffTeam = subTask.getSubtask("select-signoff-team");

			ArrayList<TCComponentGroupMember> taskReviewers = new ArrayList<TCComponentGroupMember>();

			try{
				if(selectSignoffTeam == null) continue;

				selectSignoffTeam.lock();
				if(reviewers.containsKey(reviewTaskName)){
					taskReviewers = reviewers.get(reviewTaskName);
				}else{
					if(reviewTaskName.equals("Creator")){
						taskReviewers.add(session.getUser().getGroupMembers()[0]);
					}
				}

				TCComponentSignoff[] sifnoffList = new TCComponentSignoff[taskReviewers.size()];
				int[] attachTypeList = new int[taskReviewers.size()];
				int i = 0;
				for(TCComponentGroupMember reviewMember :taskReviewers){
					sifnoffList[i] = signoffType.create(reviewMember, TCSignoffOriginType.ADHOC_USER, null);
					attachTypeList[i] = TCAttachmentType.SIGNOFF;
					i++;
				}

				if(taskReviewers.size() > 0){
					selectSignoffTeam.addAttachments(TCAttachmentScope.LOCAL, sifnoffList, attachTypeList);
					//        			selectSignoffTeam.getTCProperty("done").setLogicalValue(true);
					selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
					selectSignoffTeam.save();
					if(selectSignoffTeam.getParent().getState().equals(TCTaskState.STARTED)) {
						selectSignoffTeam.performAction(TCComponentTask.COMPLETE_ACTION, "");
					}
				}else{
					//        			selectSignoffTeam.getTCProperty("done").setLogicalValue(true);
					selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
					selectSignoffTeam.save();
				}

			} catch(Exception e){
				throw e;
			} finally {
				if(selectSignoffTeam != null)
					selectSignoffTeam.unlock();
			}
		}
	}
	
	/**
	 * End Item수가 500개 초과시 HBOM(이광석 차장)에 Mail통보
	 * @throws Exception
	 */
	private void senMailEndItemCount(){
		try{
			//end item count
			DataSet ds = new DataSet();
			ds.put("ecoNo", ecoNo);
			String count = dao.getEcoEndItemCount(ds);
			
			//500개 초과시 Mail 발송
			if(count != null && !count.equals("") && Integer.parseInt(count) > 500){
				String title = "New PLM : ECO[" + ecoNo + "]의 End Item 수는 " +count+ "개 입니다.";
				String body = "<PRE>";
				body += "참고하세요." + "<BR>";
				body += "</PRE>";
				String fromUser = "";
				String toUsers = "208748,128698";
				dao.sendMail(fromUser, title, body, toUsers);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}

	/**
	 * 메일 발송
	 * Vision-Net의 CALS 프로시져 호출
	 * @throws Exception
	 */
	private void sendMail() throws Exception{
		//String products = changeRevision.getProperty("plant_code");
		String products = changeRevision.getProperty("s7_PLANT_CODE");
		String changeDesc = changeRevision.getProperty("object_desc");

		String fromUser = session.getUser().getUserId();
		String title = "New PLM : ECO[" + ecoNo + "] 결재 요청";

		String body = "<PRE>";
		body += "New PLM에서 아래와 같이 결재 요청 되었으니 확인 후 결재 바랍니다." + "<BR>";
		body += " -ECO NO. : " + ecoNo + "<BR>";
		body += " -Product : " + products + "<BR>";
		body += " -Change Desc. : " + changeDesc + "<BR>";
		body += " -요청부서 : " + changeRevision.getTCProperty("owning_group") + "<BR>";
		body += " -요청자  : " + changeRevision.getTCProperty("owning_user") + "<BR>";
		body += "</PRE>";

		String toUsers = "";
		String[] ecoTasks = SYMCECConstant.ECO_TASK_LIST;
		ArrayList<TCComponentGroupMember> receivedUserList = reviewers.get(ecoTasks[0]); // 검토 부서

		for(TCComponentGroupMember member : receivedUserList){
			//20190228 기술관리도 메일을 받겠습니다. 송대영 책임
//			if(!member.getGroup().getGroupName().startsWith("ENGINEERING MANAGEMENT")){ // FIXED 2013.05.20, BY DJKIM 기술관리는 메일 발송에서 제외
				if(toUsers.equals("")){
					toUsers = member.getUser().getUserId();
				}else{
					toUsers += SYMCECConstant.SEPERATOR + member.getUser().getUserId();
				}
//			}
		}
		/**
		 * 검토 부서가 없으면 메일 보내지 않음
		 */
		if(toUsers.isEmpty())
			return;

		dao.sendMail(fromUser, title, body, toUsers);
	}

	/**
	 * Part 가 다른 ECO에 연결되어있는지 체크함
	 */
	private void checkRefEcoFromPart() throws Exception
	{
//		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, String>> refEcoList = dao.getRefEcoFromPartList(ecoNo);
		StringBuffer sb = new StringBuffer();
		for(HashMap<String, String> refEcoHash: refEcoList)
		{
			String partNo = refEcoHash.get("PART_NO");
			String revision = refEcoHash.get("REV");
			String refEcoNo = refEcoHash.get("REF_ECO");

			sb.append(partNo +"/"+revision+ " is referenced by other ECO ["+refEcoNo+"] \n ");
		}
		if(sb.length() >0)
			throw (new Exception(sb.toString()));
	}

	/**
	 * 결재 Task 당  중복된 결재선이 존재하는지 체크함
	 * @throws Exception
	 */
	private void checkDupApprovalLines() throws Exception
	{
//		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, Object>> dupApprovalLines = dao.getEcoDupApprovalLines(ecoNo);
		StringBuffer sbTask = new StringBuffer();
		for(HashMap<String, Object> depApprovalLine: dupApprovalLines)
		{
			String taskName = (String)depApprovalLine.get("TASK");
			String userName = (String)depApprovalLine.get("USER_NAME");
			BigDecimal count = (BigDecimal)depApprovalLine.get("CNT");
			sbTask.append("☞ "+ taskName +" / "+ userName+" (x"+String.valueOf(count)+") \n ");
		}

		if(sbTask.length() >0)
		{
			StringBuffer sbMain = new StringBuffer();
			sbMain.append("The approval line was duplicated\n ");
			sbMain.append(sbTask);
			throw (new Exception(sbMain.toString()));
		}

	}

	/**
	 * Old Part Revision 이 올바른지 체크함
	 * @throws Exception
	 */
	private void checkOldRevIsMatched() throws Exception
	{
//		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, String>> notMatchedPartList = dao.getOldRevNotMatchedParts(ecoNo);
		StringBuffer sbNotMachedParts = new StringBuffer();
		for(HashMap<String, String> notMatchedPart: notMatchedPartList)
		{
			String partNo = notMatchedPart.get("OLD_PART_NO");
			String oldPartRev = notMatchedPart.get("OLD_PART_REV");
			String realPreRev = notMatchedPart.get("REAL_PRE_REV");
			String newPartRev = notMatchedPart.get("NEW_PART_REV");
			sbNotMachedParts.append(" New: "+ partNo +"/"+newPartRev + ", Old: "+ partNo +"/"+oldPartRev+"(→ "+realPreRev+") \n ");
		}

		if(sbNotMachedParts.length() >0)
		{
			StringBuffer sbMain = new StringBuffer();
			sbMain.append("Previous revision of part is invalid. Contact to administrator. \n ");
			sbMain.append(sbNotMachedParts);
			throw (new Exception(sbMain.toString()));
		}

	}

	/**	[CF-1635] [20201208 by SYChon] 엔진파트 관리를 위한 검증 조건 추가 요청 - 엔진옵션코드 미입력의 경우에도 ECO 상신이 되는 문제 해결
	// 1. Engine Product에서 SMode가 P7, PD일 때, A00?/C00?가 있는지 체크
	// 2. Module code : 
	// 3. Part의 Option : Engine Product에서 S/Mode가 P7, PD인 경우 차종+엔진옵션(A00? & C00?) 필수 입력 
	 * @throws Exception
	 */
	private void checkSModeP7PD() throws Exception{

		//해당 ECO의 Product를 불러옴(ECO A지 Product 값) 
		String products = changeRevision.getProperty("s7_PLANT_CODE");

		if (products == null || products.isEmpty())
		{
			return;
		}
		
		ArrayList<SYMCBOMEditData> resultList = dao.selectECOEplList(ecoNo);	

		if (resultList == null || resultList.isEmpty())
		{
			return;
		}

		if (products.contains("E")) {
			for (int i = 0; i < resultList.size(); i++)
			{
				String supplyModeNew = resultList.get(i).getSupplyModeNew(); 	// 현재 행의 신규 파트의 Supply Mode
				String vcNew = resultList.get(i).getVcNew();					// 현재 행의 신규 파트의 Variant Condition(Option)
				String partNew = resultList.get(i).getPartNoNew();				// 현재 행의 신규 Part No
				
				// Product가 엔진이고, Supply Mode가 P7,PD 이면
				if (supplyModeNew != null &&(supplyModeNew.equals("P7") || supplyModeNew.equals("PD")))				
				{
					// VA값(옵션값)이 A00,C00를 포함하지 않을 경우
					if (vcNew == null || (!vcNew.contains("A00") || !vcNew.contains("C00")))
					{
						throw (new Exception("PartNo : " + partNew + "\n" + " Options : " + vcNew + "\n" + " ▶엔진 Product의 경우 차종+엔진옵션(A00? & C00?) 필수입력! 미입력시 미상신"));						
					}
				}

			} 		
			
		}
		
	}
	
	
	/**
	 * [20230406][CF-3876] 기술관리팀 이보현 책임, 안추은 책임 요청
	 * #1. Vehicle ECO에 파워트레인 프로젝트 존재 유무 체크 
	 * #2. Vehicle ECO(차량 ECO)의 ECO C지 Proj속성에 파워트레인 프로젝트 존재시 오류 처리(파워트레인 프로젝트 존재시 Power Traing ECO(엔진 ECO)로 작업 해야한다.) 
	 */
	private void checkPowerTrain() throws Exception{
		DataSet ds = new DataSet();
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> resultList = dao.checkPowerTraing(ds);
		if(resultList.get(0).get("CHECK_ENGINE_PART").equals("error")){
			throw (new Exception("PT ECO로 작성 요망."));
		}
	}
	
	private void checkChangeEffPoint() throws Exception{
		String changeEffPoint = changeRevision.getProperty("s7_EFFECT_POINT_DATE");
		if(changeEffPoint.getBytes().length > 35) {
			throw (new Exception("적용 시점(Change Eff. Point) 35바이트 이내 입력 요망"));
		}
	}
}
