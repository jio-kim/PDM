package com.ssangyong.commands.workflow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.namegroup.PngDlg;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.DateUtil;
import com.ssangyong.common.utils.FTPConnection;
import com.ssangyong.common.utils.PreferenceService;
import com.ssangyong.dto.ApprovalLineData;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentContextList;
import com.teamcenter.rac.kernel.TCComponentEffectivity;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentOccEffectivity;
import com.teamcenter.rac.kernel.TCComponentOccEffectivityType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentReleaseStatus;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcEffectivityService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.workflow.commands.newperformsignoff.SignoffDecisionOperation;
import com.teamcenter.services.rac.structuremanagement.StructureFilterWithExpandService;
import com.teamcenter.services.rac.structuremanagement._2014_06.StructureFilterWithExpand;
import com.teamcenter.services.rac.structuremanagement._2014_06.StructureFilterWithExpand.ExpandAndSearchOutput;

/**
 * [SR140702-056][20140422] 반려 시 상태 변경 안되는 문제 대응.  (code by 정윤재)
 * SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
 * [20140929] jclee, WF Handler 변경 이전 생성 ECO중 진행 중인 건이 더이상 없다고 판단하여 주석처리.
 *   : 추 후 Status 변경이 안되는 건이 나올 경우에는 DB Update 및 Edit Property 를 이용하여 처리할 것.
 * [SR140425-008][20140929][jclee] 결재 완료 시 Cost Engineer에게 메일 발송.
 * [20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선
 * [20160727][ymjang] 최종 결재 승인시에만 후속 작업 진행하도록 변경 (Reject 및 No Decision 제외)
 * [20160727][ymjang] 최종 결재 기각시 기각 메일 발송 기능 추가
 * [20160727][ymjang] java.lang.Exception: setRevisionEffectivity error 오류 개선
 * [20160818][ymjang] 저장안됨 --> 원상복구함.
 * [20160922][ymjang] Pack 된 라인 IN-ECO Update 오류 수정
 * [20160930][ymjang] Pack 된 라인 IN-ECO Update 오류 수정 (Unpack 후 하도록 재수정)
 * [20161117][ymjang] CM으로 시작되는 ECO 는 S201 인도 마힌드라 ECO 로서 원가는 제외함.
 * [20161117][ymjang] occ effectivity 생성 여부 체크하여 생성되어 있을 경우, eff_date 만 Update 하도록 수정함.
 * [20161226][ymjang] Validation 결과 표시 누락 개선
 * [20170214][ymjang] part_application_history table 은 현재 사용하지 않는 테이블로서 데이터 생성 불필요
 * [20180302][LJG]원가 기획팀에서 -> 원가 기술 팀으로 변경
 * @author bs
 *
 */
public class SYMCSignoffDecisionOperation extends SignoffDecisionOperation {
	private TCComponentTask performSignoffTask;
	private TCComponentSignoff tccomponentsignoff;
	private TCSession session;
	private SYMCDecisionDialog decisionDialog;
	private TCCRDecision decision;

	private TCComponentTask rootTask;
	private TCComponentItemRevision revision;

	private CustomECODao dao = new CustomECODao();
	// FIXED 진행현황 추가
	private WaitProgressBar progress;

	private int iReturnCode = -1;
	
	// 2024.01.09 수정
	private TCComponentSignoff signoffObject;
	
	public SYMCSignoffDecisionOperation(SYMCDecisionDialog decisionDialog, TCSession tcsession, 
			/*AIFDesktop aifdesktop,*/ TCComponentTask tccomponenttask,
			TCComponentSignoff tccomponentsignoff, TCCRDecision tccrdecision, String s) {
		super(tcsession, decisionDialog, tccomponenttask, tccomponentsignoff, tccrdecision, s);
		performSignoffTask = null;
		session = tcsession;
		performSignoffTask = tccomponenttask;
		this.decisionDialog = decisionDialog;
		this.tccomponentsignoff = tccomponentsignoff;
		this.decision = tccrdecision;
		
		// 2024.01.09 수정
		this.signoffObject = tccomponentsignoff;
	}

	public SYMCSignoffDecisionOperation(SYMCDecisionDialog decisionDialog, TCSession tcsession,
			/*AIFDesktop aifdesktop,*/ TCComponentTask tccomponenttask,
			TCComponentSignoff tccomponentsignoff, TCCRDecision tccrdecision, String s, String s1) {
		this(decisionDialog, tcsession, tccomponenttask, tccomponentsignoff,
				tccrdecision, s);
		this.decisionDialog = decisionDialog;
		this.tccomponentsignoff = tccomponentsignoff;
	}

	@Override
	public void executeOperation() {
		try {

			progress = new WaitProgressBar(decisionDialog);
			progress.setWindowSize(320, 140);
			progress.start();						
			progress.setShowButton(false);
			progress.setStatus("Working...\nPlease wait.\n");
			progress.setAlwaysOnTop(true);

			rootTask = performSignoffTask.getRoot();
			String rootTaskName = rootTask.getName();

			progress.setStatus("Before Operation");
			// ECO 결재 시 선 작업
			if(rootTaskName.equals(SYMCECConstant.ECO_PROCESS_TEMPLATE)){
				if (addBeforeEcoOperation() == SWT.NO) {
					throw new Exception("Operation is canceled");
				}
			}

			progress.setStatus("Super Operation");
			super.executeOperation();

			progress.setStatus("After Operation");
			// ECO 결재 시 추가 작업
			if(rootTaskName.equals(SYMCECConstant.ECO_PROCESS_TEMPLATE)){
				addAfterEcoOperation();
			}

			// ECI 결재 시 추가 작업
			if(rootTaskName.equals(SYMCECConstant.ECI_PROCESS_TEMPLATE)){
				addAfterEciOperation();
			}

			if(rootTaskName.startsWith("SYMC_MECO") || rootTaskName.startsWith("SYMC_MEW")){
				addAfterMecoOperation();
			}

			progress.setStatus("Operation Completed....\n");
		} catch (Exception e) {
			progress.setStatus(e.getMessage() + "\n");
			e.printStackTrace();
//			try {
//				decisionDialog.setVisible(false);
//				decisionDialog.disposeDialog();
//				session.getUser().getUserInBox().refresh();
//			} catch (TCException e1) {
//				e1.printStackTrace();
//			}
		}finally{
			try {
				progress.close();
				decisionDialog.setVisible(false);
				decisionDialog.disposeDialog();
				session.getUser().getUserInBox().refresh();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * ECO 승인 전 확인 단계
	 */
	private int addBeforeEcoOperation() throws Exception {
		if ("Reject".equals(decisionDialog.getDecision().toString())) {
			return -1;
		}
		
		String thisTaskName = "";
		String thisSignoffName = "";

		thisTaskName = performSignoffTask.getParent().getName();

//		thisSignoffName = tccomponentsignoff.getProperty("object_name");
		thisSignoffName = tccomponentsignoff.toString();
		thisSignoffName = thisSignoffName.substring(thisSignoffName.indexOf("/") + 1, thisSignoffName.length());

		// Technical Management : 결재 완료 시 처리
		if((thisTaskName.equals("Related Team Review") || thisTaskName.equals("Technical Management")) && !thisSignoffName.equals("COST_ENGINEER")){
			// 현재 결재 타겟에서 S7_ECORevision를 찾음
			TCComponent[] comps = rootTask.getRelatedComponents("root_target_attachments");
			for(TCComponent comp : comps){
				if(comp.getType().equals("S7_ECORevision")){
					revision = (TCComponentItemRevision)comp;
					break;
				}
			}
			if(revision != null){
				if (!thisTaskName.equals("Technical Management")) {
					/**
					 * [SR141120-043][2014.11.21][jclee] ECO 내역 중 Color ID가 None이 아니면서 Color Section No가 없을 경우 Warning Message Box Open
					 */
					iReturnCode = -1;
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							try {
								progress.setStatus("[1/3] checkColorID....");
								iReturnCode = checkColorID();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
	
					if (iReturnCode != SWT.NO) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								try {
									progress.setStatus("[2/3] checkColorIDChanged....");
									iReturnCode = checkColorIDChanged();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}
	
					if (iReturnCode != SWT.NO) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								try {
									progress.setStatus("[3/3] selectECOEplCOSModeCompareList....");
									iReturnCode = selectECOEplCOSModeCompareList();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}
				}

				// [SR150818-041][20150826][jclee] CO추가 시 자동으로 ECO Generation 수행
//				if (thisTaskName.equals("Technical Management") && !"Reject".equals(decisionDialog.getDecision().toString())) {
//				if (thisTaskName.equals("Technical Management")) {
//					if (iReturnCode != SWT.NO) {
//						Display.getDefault().syncExec(new Runnable() {
//							@Override
//							public void run() {
//								try {
//									progress.setStatus("selectUnGeneratedCOPartList....");
//									iReturnCode = selectUnGeneratedCOPartList();
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//							}
//						});
//					}
//				}

				// [20161226][ymjang] Validation 결과 표시 누락 개선
				// Solution Part List 내 Part 중 Material, Alt Material Part가 Obsolete된 항목이 있을 경우 Error Message Box Open
				//[20191025] 설계변경 검증 속도 개선 프로젝트에서 제외 결정 - 결재 상신시에만 체크하고 결재 승인시에는 제외(기술관리팀) 
//				if (iReturnCode != SWT.NO) {
//					Display.getDefault().syncExec(new Runnable() {
//						@Override
//						public void run() {
//							try {
//								progress.setStatus("[5/5] checkObsoleteMaterial....");
//								iReturnCode = checkObsoleteMaterial();
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					});
//				}
			}
		}

		return iReturnCode;
	}

	/**
	 * [SR141120-043][2014.11.21][jclee] ECO 내역 중 Color ID가 None이 아니면서 Color Section No가 없을 경우 Warning Message Box Open
	 * OK : 결재 진행
	 * Cancel : 결재 진행 취소
	 * @throws Exception
	 */
	private int checkColorID() throws Exception {
		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
		String sECONo = revision.getProperty("item_id");
		ds.put("ecoNo", sECONo);
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
		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
		String sECONo = revision.getProperty("item_id");
		ds.put("ecoNo", sECONo);
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
	 * 타 차종 C/OVER 경우(CHANGE TYPE : F2) S/MODE 입력이 맞는지 검증하는 방법 추가
	 * @return
	 * @throws Exception
	 */
	private int selectECOEplCOSModeCompareList() throws Exception {
		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
		String sECONo = revision.getProperty("item_id");
		ArrayList<HashMap<String, String>> results = dao.selectECOEplCOSModeCompareList(sECONo);

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
	 * 최종승인 시점에서 해당 ECO EPL내 Part 중 Carry Over되어 추가된 ECO가 존재하는지 여부 확인.
	 * @return
	 * @throws Exception
	 */
	private int selectUnGeneratedCOPartList() throws Exception {
		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
		String sECONo = revision.getProperty("item_id");
		ArrayList<HashMap<String, String>> results = dao.selectUnGeneratedCOPartList(sECONo);

		if (results.size() > 0) {
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			ds.put("ecoNo", sECONo);
			remote.execute("com.ssangyong.service.ECOHistoryService", "generateECO", ds);

			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sParentNo = result.get("PARENT_NO");
				String sParentRev = result.get("PARENT_REV");
				String sPartNo = result.get("NEW_PART_NO");
				String sPartRev = result.get("NEW_PART_REV");
				String sPartSeq = result.get("NEW_SEQ");

				sbMessage.append(sParentNo).append("/").append(sParentRev).append(" -> ").append(sPartNo).append("/").append(sPartRev).append(" ; ").append(sPartSeq);
				sbMessage.append("\n");
			}
			sbMessage.append("\n");
			sbMessage.append("These Part's those are carried over.\nECO Regenerated.");

			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.YES);
			box.setMessage(sbMessage.toString());

			return box.open();
		}
		return SWT.YES;
	}

	/**
	 * [20160415][jclee] Solution Part List 내 Part 중 Material, Alt Material Part가 Obsolete된 항목이 있을 경우 Error Message Box Open
	 * @return
	 * @throws Exception
	 */
	private int checkObsoleteMaterial() throws Exception {
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
		TCComponent[] cSolutions = revision.getReferenceListProperty("CMHasSolutionItem");

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
			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
			box.setText("Check Part Master");
			box.setMessage(sbResult.toString());
			// [20161226][ymjang] Validation 결과 표시 누락 개선
			return box.open();
		}

		return SWT.YES;
	}
	/**
	 * #1.ECI가 최종 승인 시 Vision-net 인터페이스
	 */
	private void addAfterEciOperation() {
		try{

			TCComponent[] comps = rootTask.getRelatedComponents("root_target_attachments");
			for(TCComponent comp : comps){
				if(comp.getType().equals("S7_ECIRevision")){

					revision = (TCComponentItemRevision)comp;
					break;
				}
			}

			// #1.ECI가 최종 승인 시 Vision-net 인터페이스
			String[] eciTasks = SYMCECConstant.ECI_TASK_LIST;
			int i = 1;
			for(String eciTask : eciTasks){
				if(eciTask.equals(performSignoffTask.getParent().getName())){
					break;
				}
				i++;
			}

			if(i < 4){
				TCComponentTask[] taskList = rootTask.getSubtasks();
				TCComponentTask nextTask = null;

				for(int j=0; j< taskList.length; j++){
					if(taskList[j].getName().equals(eciTasks[i])){
						nextTask = taskList[j];
						break;
					}
				}

				if(nextTask != null){
					TCComponentTask selectSignoffTeamTask = nextTask.getSubtask("select-signoff-team");
					TCComponentSignoff[] signoffs = selectSignoffTeamTask.getValidSignoffs();
					if(signoffs != null & signoffs.length > 0){ // 결재 요청 메일 발송
						TCComponentGroupMember groupMember = signoffs[0].getGroupMember();
						String toUsers = groupMember.getUserId();

						sendRequestMail(toUsers);
					}else{ // 인터페이스
						String eciPuid = revision.getItem().getUid();
						dao.interfaceECI(eciPuid); // 인터페이스 테이블 인서트

						HashMap<String, String> map = dao.getECIfileInfo(eciPuid); // 파일명 변경 IF_ECI_INFO_TO_VNET의 SEQ+파일명으로 변경
						if(map != null){
							FTPConnection ftp = new FTPConnection();
							ftp.rename(eciPuid, map.get("FILENAME"));
							ftp.disconnect();
						}
						sendRequestMail(null);
					}
				}
			}
		}catch(Exception e){
			MessageBox messagebox = new MessageBox(e);
			messagebox.setVisible(true);
			return;
		}
	}


	private void addAfterMecoOperation() {
		String thisTaskName = "";

		try{

			TCComponent[] comps = rootTask.getRelatedComponents("root_target_attachments");
			for(TCComponent comp : comps){
				if(comp.getType().equals("M7_MECORevision")){

					revision = (TCComponentItemRevision)comp;
					break;
				}
			}
			thisTaskName = performSignoffTask.getParent().getName();

			boolean taskComplete = true;
			TCComponentSignoff[] signoffs = performSignoffTask.getValidSignoffs();
			if(signoffs.length > 0) {
				for(TCComponentSignoff signoff : signoffs) {
					signoff.refresh();
					if(signoff.getProperty("decision_date").equals("")){
						taskComplete = false;
						break;
					}
				}
			}
			if(taskComplete){

				String nextTaskName = "";
				//	        	String[] ecoTasks = SYMCECConstant.MECO_TASK_LIST;
				TCComponentTask[] taskList = rootTask.getSubtasks();


				if("Reject".equals(decisionDialog.getDecision().toString())) {
					// SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
					dao.changeMECOStatus(revision.getUid(), "In Work");
					nextTaskName ="Creator";
				}else{

					if(rootTask.getName().contains("MEW")) {

						if(thisTaskName.equals("Sub Team Leader")) {
							// SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
							dao.changeMECOStatus(revision.getUid(), "In Approval");

							nextTaskName ="Team Leader";

						}else if(thisTaskName.equals("Team Leader")) {
							// SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
							dao.changeMECOStatus(revision.getUid(), "Completed");

							nextTaskName ="SYSTEM";

						}

					}else if(rootTask.getName().contains("MECO")) {

						if(thisTaskName.equals("Sub Team Leader")) {
							// SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
							dao.changeMECOStatus(revision.getUid(), "In Approval");

							nextTaskName ="Team Leader";

						}else if(thisTaskName.equals("Team Leader")) {
							// SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
							dao.changeMECOStatus(revision.getUid(), "Approved");

							nextTaskName ="BOP ADMIN";

						}else if(thisTaskName.equals("BOP ADMIN")) {
							// SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
							dao.changeMECOStatus(revision.getUid(), "Completed");

							nextTaskName ="SYSTEM";
						}
					}

				}
				// #2. 메일 발송
				TCComponentTask nextTask = null;

				for(int j=0; j< taskList.length; j++){
					if(taskList[j].getName().equals(nextTaskName)){
						nextTask = taskList[j];
						break;
					}
				}
				if(nextTask != null){
					TCComponentSignoff[] nextSignoffs = nextTask.getValidSignoffs();
					if(nextSignoffs != null && nextSignoffs.length > 0) {
						for(TCComponentSignoff nextSignoff : nextSignoffs){
							TCComponentGroupMember groupMember = nextSignoff.getGroupMember();
							String toUsers = groupMember.getUserId();
							sendRequestMailForMECO(toUsers);
						}
					}
				}
			}

		}catch(Exception e){
			e.printStackTrace();
			MessageBox.post(e);
			return;
		}

	}
	/** 
	 * #1.최종 승인 시 ECO 번호를 Vision-net으로 인터페이스
	 * #2.ECO 각 타스크 별 완료 시 상태 변경[s7_MATURITY]
	 * #3.Effectivity 생성
	 * #4.완료 메일 발송
	 * #4.1 COST Engineer 메일 발송
	 * #5.이력 생성
	 */
	private void addAfterEcoOperation() {
		String thisTaskName = "";
		try{

			// 현재 결재 타겟에서 S7_ECORevision를 찾음
			TCComponent[] comps = rootTask.getRelatedComponents("root_target_attachments");
			for(TCComponent comp : comps){
				if(comp.getType().equals("S7_ECORevision")){

					revision = (TCComponentItemRevision)comp;
					break;
				}
			}

			thisTaskName = performSignoffTask.getParent().getName();

			// Technical Management : 결재 완료 시 처리
			if(thisTaskName.equals("Technical Management")){
				if(revision != null) {
					// [20160727][ymjang] 최종 결재 승인시에만 후속 작업 진행하도록 변경 (Reject 및 No Decision 제외)	
					// [20160727][ymjang] 최종 결재 기각시 기각 메일 발송 기능 추가
					// 2024.01.09 수정 TCCRDecision.REJECT_DECISION -->  signoffObj.getRejectDecision()
					if (decision.equals(signoffObject.getRejectDecision())) {
						// 기각 메일 발송
						sendRejecttMail(null);
					} 
					// 2024.01.09 수정 TCCRDecision.APPROVE_DECISION -->  signoffObj.getApproveDecision()
					else if (decision.equals(signoffObject.getApproveDecision())) {
						String itemId = revision.getProperty("item_id");
						
						
						try{
							SYMCRemoteUtil remote = new SYMCRemoteUtil();
							DataSet ds = new DataSet();
							ds.put("ECO_NO", itemId);
							
							dao.updateStep(itemId, "ECO_DATE", "");
							
							// #1.최종 승인 시 ECO 번호를 Vision-net으로 인터페이스
							//[20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선
							progress.setStatus("[1/10] interfaceECONoToVnetEAI....");
							try{
								dao.interfaceECONoToVnetEAI(itemId);
								if(!dao.updateStep(itemId, "STEP01", "interfaceECONoToVnetEAI")){
						            throw (new Exception("Update Step Error - [STEP01] interfaceECONoToVnetEAI"));
						        }
							} catch(Exception e){
								sendAdminEmail(e.getMessage());
							}

							/*
							 * SRME:: [][20140820] swyoon EPL 보정.
							 * 
							 */
							progress.setStatus("[2/10] correctEPL....");
							remote.execute(CustomECODao.ECO_INFO_SERVICE_CLASS, "correctEPL", ds);	
							if(!dao.updateStep(itemId, "STEP02", "correctEPL")){
					            throw (new Exception("Update Step Error - [STEP02] correctEPL"));
					        }
							//스케줄러(JOB_SEARCH_SAVE_EPL) 수행 시 19단계 완료된 ECO만 처리하기 위해 값 입력
							if(!dao.updateStep(itemId, "STEP19", "complete")){
					            throw (new Exception("Update Step Error - [STEP19] complete"));
					        }

							/**
							 * [SR140806-002][20140725] swyoon ALC값 복사(PG_ID, PG_ID_VERSION). Replace에 해당되는 경우만, New가 Null이고, Old가 Null이 아닌경우 Old값을 New로 복사함.
							 * 기술관리의 최종 승인시에만 ALC값이 Update되도록 수정함.[20140806]
							 */	
							progress.setStatus("[3/10] updateALC....");
							remote.execute(CustomECODao.ECO_INFO_SERVICE_CLASS, "updateALC", ds);	
							if(!dao.updateStep(itemId, "STEP03", "updateALC")){
					            throw (new Exception("Update Step Error - [STEP03] updateALC"));
					        }
							
							progress.setStatus("[4/10] checkNewPartName....");
							//[20150127]Yun Sung Won, New Part Name이 추가되면, PNG_NEW_NAME_LIST에 추가되고, 담당자(preference[SYMC_NEW_NAME_RCV])에게 메일로 통보함.
							//[20191025] 설계변경 검증 속도 개선 프로젝트에서 메일통보 제외 결정 - PNG_NEW_NAME_LIST에 추가는 유지, 메일통보만 제외(기술관리팀) 
							PngDlg.checkNewPartName(itemId);
							if(!dao.updateStep(itemId, "STEP04", "checkNewPartName")){
					            throw (new Exception("Update Step Error - [STEP04] checkNewPartName"));
					        }
							
							// #3.Effectivity 생성
							progress.setStatus("[5/10] setRevisionEffectivity....");
							setRevisionEffectivity();
							if(!dao.updateStep(itemId, "STEP05", "setRevisionEffectivity")){
					            throw (new Exception("Update Step Error - [STEP05] setRevisionEffectivity"));
					        }

							// #4.완료 메일 발송
							progress.setStatus("[6/10] sendRequestMail....");
							sendRequestMail(null);
							if(!dao.updateStep(itemId, "STEP06", "sendRequestMail")){
					            throw (new Exception("Update Step Error - [STEP06] sendRequestMail"));
					        }
							
							//[CSH][SR190201-022]NMCD Update 필요한 ECO에 대해 담당자 메일 보내기
							progress.setStatus("[7/10] sendMailToNMCDECO....");
							sendMailToNMCDECO();
							if(!dao.updateStep(itemId, "STEP07", "sendMailToNMCDECO")){
					            throw (new Exception("Update Step Error - [STEP07] sendMailToNMCDECO"));
					        }
							
							/**
							 * [20161117][ymjang] CM으로 시작되는 ECO 는 S201 인도 마힌드라 ECO 로서 원가는 제외함.
							 * [SR140425-008][20140929][jclee] 결재 완료 시 Cost Engineer에게 메일 발송.
							 * [20180302][LJG]원가 기획팀에서 -> 원가 기술 팀으로 변경
							 */
							// #4.1 Cost Engineer에게 이메일 발송
							if (!itemId.startsWith("CM")) {
								progress.setStatus("[8/10] sendMailToCostEngineer....");
								sendMailToCostEngineer();
								if(!dao.updateStep(itemId, "STEP08", "sendMailToCostEngineer")){
						            throw (new Exception("Update Step Error - [STEP08] sendMailToCostEngineer"));
						        }
							}
							
							progress.setStatus("[9/10] setInEcoOnBOMLine....");
							setInEcoOnBOMLine();
							if(!dao.updateStep(itemId, "STEP09", "setInEcoOnBOMLine")){
					            throw (new Exception("Update Step Error - [STEP09] setInEcoOnBOMLine"));
					        }
							
							/**
							 * [2015.02.11][jclee] BOM History 생성
							 * In ECO, Out ECO 생성
							 * 모든 보정 작업 이후 실행할 것!
							 */
							progress.setStatus("[10/10] makeBOMHistoryMaster....");
							dao.makeBOMHistoryMaster(itemId);
							if(!dao.updateStep(itemId, "STEP10", "makeBOMHistoryMaster")){
					            throw (new Exception("Update Step Error - [STEP10] makeBOMHistoryMaster"));
					        }
							
							if(!dao.updateStep(itemId, "STEP20", "complete")){
					            throw (new Exception("Update Step Error - [STEP20] complete"));
					        }

						}catch(Exception e){
							e.printStackTrace();
							progress.setStatus(e.getMessage());
							sendAdminEmail(e.getMessage());
//							MessageBox.post(AIFUtility.getActiveDesktop(), "Please contact your administrator.\nResume 'correctEPL' and 'updateALC'.", "INFORMATION", MessageBox.ERROR);
						}
						
						// #2.ECO 각 타스크 별 완료 시 상태 변경[s7_MATURITY]
						// [SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
						// [20140929] jclee, WF Handler 변경 이전 생성 ECO중 진행 중인 건이 더이상 없다고 판단하여 주석처리.
						//  : 추 후 Status 변경이 안되는 건이 나올 경우에는 DB Update 및 Edit Property 를 이용하여 처리할 것.
						// dao.updateEcoStatus(revision.getUid(), "Completed", "Released");

						// #5.이력 생성
						//[20170214][ymjang] part_application_history table 은 현재 사용하지 않는 테이블로서 데이터 생성 불필요
						//dao.makePartHistory(itemId);

//				        System.out.println("EEEEEEEEEnd");
					}
				}
			}else{
				// #2.ECO 각 타스크 별 완료 시 상태 변경[s7_MATURITY]
				boolean taskComplete = true;
				TCComponentSignoff[] signoffs = performSignoffTask.getValidSignoffs();
				if(signoffs.length > 0) {
					for(TCComponentSignoff signoff : signoffs) {
						signoff.refresh();

						// UG DJKIM 2014.02.14, 완료 여부를 decision_date에서 decision으로 변경
						TCProperty localTCProperty = signoff.getTCProperty("decision");
						int i = localTCProperty.getIntValue();
						
						//2024.01.09  수정   TCCRDecision.NO_DECISION -->  signoff.getNoDecision()
						if (signoff.getNoDecision().getIntValue() == i ){
							//		            	// UG DJKIM 2014.02.14, 완료 여부를 decision_date에서 decision으로 변경
							//		        		TCProperty localTCProperty = signoff.getTCProperty("decision");
							//		        		int i = localTCProperty.getIntValue();
							//		        		if (TCCRDecision.NO_DECISION.getIntValue() == i || TCCRDecision.REJECT_DECISION.getIntValue() == i){
							//		            		taskComplete = false;
							//		            		break;
							//		        		}

							// 20140218 bskwak 반려가 안되는 문제가 발생해서 워복하고 개선 로직은 재검토 하기로 함. 
							if(signoff.getProperty("decision_date").equals("")){
								taskComplete = false;
								break;
							}

						}
					}
					if(taskComplete){
						// #1. 상태 업데이트
						String nextTaskName = "";
						String[] ecoTasks = SYMCECConstant.ECO_TASK_LIST;
						TCComponentTask[] taskList = rootTask.getSubtasks();

						boolean existSubLeaderTask = false;
						// [SR140702-056][20140422] 반려 시 상태 변경 안되는 문제 대응.  (code by 정윤재)
						boolean isRejectedProcess = false;
						if("Reject".equals(decisionDialog.getDecision().toString())) {
							isRejectedProcess = true;
						}

						for(int j=0; j< taskList.length; j++){
							if(taskList[j].getName().equals(ecoTasks[1])){ // Sub-team Leader
								TCComponentSignoff[] nextSignoffs = taskList[j].getValidSignoffs();
								if(nextSignoffs != null && nextSignoffs.length > 0) {
									existSubLeaderTask = true;
								}
								break;
							}
						}

						// [SR140702-056][20140422] 반려 시 상태 변경 안되는 문제 대응.  (code by 정윤재)
						// SRME::[반영보류][SR140702-027][20140702] bskwak, status 변경 처리를 WF handler로 이관, (요청 정윤재, WF handler작업:정윤재)
						//   ==> dao.updateEcoStatus 부분 모두 주석.
						if(isRejectedProcess) {
							//nothing
						}else{
							if(thisTaskName.equals(ecoTasks[0])){ // Related Team Review
								if(existSubLeaderTask){
									dao.updateEcoStatus(revision.getUid(), "In Review2", "Processing");
									nextTaskName = ecoTasks[1];
								}else{
									dao.updateEcoStatus(revision.getUid(), "In Approval", "Processing");
									nextTaskName = ecoTasks[2];
								}
							}else if(thisTaskName.equals(ecoTasks[1])){ // Sub-team Leader
								dao.updateEcoStatus(revision.getUid(), "In Approval", "Processing");
								nextTaskName = ecoTasks[2];
							}else if(thisTaskName.equals(ecoTasks[2])){ // Design Team Leader
								dao.updateEcoStatus(revision.getUid(), "Approved", "Processing");
								nextTaskName = ecoTasks[3];
							}else if(thisTaskName.equals("Creator")){ // Creator[반려시]
								dao.updateEcoStatus(revision.getUid(), "In Review1", "Processing");
								nextTaskName = ecoTasks[0];
							}
						}


						// #2. 메일 발송
						TCComponentTask nextTask = null;

						for(int j=0; j< taskList.length; j++){
							if(taskList[j].getName().equals(nextTaskName)){
								nextTask = taskList[j];
								break;
							}
						}

						// [SR140702-056][20140422] 반려 시 상태 변경 안되는 문제 대응.  (code by 정윤재)
						if(isRejectedProcess) {
							TCComponentUser owningUser= (TCComponentUser)revision.getReferenceProperty("owning_user");
							sendRequestMail(owningUser.getUserId());
						}else{

							if(nextTask != null ){
								TCComponentSignoff[] nextSignoffs = nextTask.getValidSignoffs();
								if(nextSignoffs != null && nextSignoffs.length > 0) {
									for(TCComponentSignoff nextSignoff : nextSignoffs){
										TCComponentGroupMember groupMember = nextSignoff.getGroupMember();
										String toUsers = groupMember.getUserId();
										if(thisTaskName.equals(ecoTasks[2])){
											// 기술관리는 메일 발송 제외 요청 : 2013.04.26
										}else{
											sendRequestMail(toUsers);
										}
									}
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			MessageBox.post(e);
			return;
		}
	}
	
	/**
	 * 다음 타스크에 결재 요청 메일 발송
	 * @throws TCException 
	 */
	private void sendRequestMail(String toUsers) throws TCException, Exception {
		try{
			String ecNo = revision.getProperty("item_id");
			//String products = revision.getProperty("plant_code");
			String products = revision.getProperty("s7_PLANT_CODE");
			String changeDesc = revision.getProperty("object_desc");
			String fromUser = session.getUser().getUserId();
			String title = "New PLM : ECO[" + ecNo + "] 결재 요청";
			String body = "<PRE>";
			body += "New PLM에서 아래와 같이 결재 요청 되었으니 확인 후 결재 바랍니다." + "<BR>";
			body += " -ECO NO. : " + ecNo + "<BR>";
			body += " -Product : " + products + "<BR>";
			body += " -Change Desc. : " + changeDesc + "<BR>";
			body += " -작성부서 : " + rootTask.getProcess().getProperty("owning_group") + "<BR>";
			body += " -작성자  : " + rootTask.getProcess().getProperty("owning_user") + "<BR>";
			body += "</PRE>";

			if(toUsers == null){
				title = "New PLM : " + ecNo + "결재 완료";
				body = "ECO NO[" + ecNo + "] 결재가 완료 되었습니다.";
				//				dao.sendMail(fromUser, title, body, rootTask.getProcess().getProperty("owning_user"));
				TCComponent owningUser = rootTask.getProcess().getReferenceProperty("owning_user");
				dao.sendMail(fromUser, title, body, ((TCComponentUser) owningUser).getUserId());
			}else{
				dao.sendMail(fromUser, title, body, toUsers);
			}
		}catch(Exception e){
			throw (new Exception("sendRequestMail error"));
		}
	}
	
	private void sendAdminEmail(String er) throws TCException, Exception {
		try{
			String ecNo = revision.getProperty("item_id");
			String fromUser = "NPLM";
			DataSet ds = new DataSet();
			ds.put("ER", "");
			String toUsers = dao.getAdmin(ds);
			String title = "New PLM : ECO[" + ecNo + "] 최종 승인 후속작업 오류";
			String body = "<PRE>";
			body += er + "<BR>";
			body += "</PRE>";

			dao.sendMail(fromUser, title, body, toUsers);
			
		}catch(Exception e){
			throw (new Exception("sendAdminEmail error"));
		}
	}
	
	/**
	 * NMCD Update가 필요한 ECO에 대해 상신자에게 메일발송
	 * @throws TCException 
	 */
	private void sendMailToNMCDECO() throws TCException, Exception {
		try{
			DataSet ds = new DataSet();
			ds.put("PROD_ID", "");
				
			ArrayList<HashMap<String,String>> projectCodeList = dao.getProjectCodeList(ds);
			if(projectCodeList != null && projectCodeList.size() > 0){
				ArrayList<String> prjCodeList = new ArrayList<String>();
				for(HashMap hm :projectCodeList){
					prjCodeList.add((String)hm.get("PROJECT_CODE"));
				}
				
				if(prjCodeList != null && prjCodeList.size() > 0){
					ds = new DataSet();
					String ecNo = revision.getProperty("item_id");
					ds.put("PRJ_CD", prjCodeList);
					ds.put("ECO_NO", ecNo);
					
					String part = dao.getNMCDUpdatePartList(ds);
					if(part != null && !part.equals("")){
						String fromUser = "NPLM";
						String toUser = ((TCComponentUser)revision.getReferenceProperty("owning_user")).getUserId();
//						String toUser = "188729";
						String title = "[" + ecNo + "] NMCD Update 바랍니다";
						String body = "<PRE><font size=3>";
						body += "<B>ECO NO[" + ecNo + "]로 BOM LINE이 생성되었습니다.</B>" + "<BR>";
						body += "<B>NMCD Management 화면에서 NMCD, Project Code, 팀명을 Update 바랍니다.</B>"+ "<BR>";
//						body += "NMCD Management 화면에서 아래 Part의 NMCD Update 바랍니다."+ "<BR>";
//						body += "Part : " + part + "<BR>";
						body += "</font></PRE>";
			            
						dao.sendMail(fromUser, title, body, toUser);
					}
				}
			}
//				System.out.println("part : "+part);
//			}
		}catch(Exception e){
			throw (new Exception("sendMailToNMCDECO error"));
		}
	}


	private void sendRequestMailForMECO(String toUsers) throws TCException, Exception {
		try{
			String mecNo = revision.getProperty("item_id");
			String products = revision.getProperty("m7_PROJECT");
			String changeDesc = revision.getProperty("object_desc");
			String fromUser = session.getUser().getUserId();
			String title = "New PLM : MECO[" + mecNo + "] 결재 요청";
			String body = "<PRE>";
			body += "New PLM에서 아래와 같이 결재 요청 되었으니 확인 후 결재 바랍니다." + "<BR>";
			body += " -MECO NO. : " + mecNo + "<BR>";
			body += " -Project : " + products + "<BR>";
			body += " -Change Desc. : " + changeDesc + "<BR>";
			body += " -작성부서 : " + rootTask.getProcess().getProperty("owning_group") + "<BR>";
			body += " -작성자  : " + rootTask.getProcess().getProperty("owning_user") + "<BR>";
			body += "</PRE>";

			if(toUsers == null){
				title = "New PLM : " + mecNo + "결재 완료";
				body = "MECO NO[" + mecNo + "] 결재가 완료 되었습니다.";
				//				dao.sendMail(fromUser, title, body, rootTask.getProcess().getProperty("owning_user"));
				TCComponent owningUser = rootTask.getProcess().getReferenceProperty("owning_user");
				dao.sendMail(fromUser, title, body, ((TCComponentUser) owningUser).getUserId());
			}else{
				dao.sendMail(fromUser, title, body, toUsers);
			}
		}catch(Exception e){
			throw (new Exception("sendRequestMailForMECO error"));
		}
	}

	/**
	 * [SR140425-008][20140929][jclee] 결재 완료 시 Cost Engineer에게 메일 발송.
	 */
	private void sendMailToCostEngineer() throws TCException, Exception {
		try{
			String ecoNo = revision.getProperty("item_id");

			// 결재선 조회
			ApprovalLineData theLine = new ApprovalLineData();
			theLine.setEco_no(ecoNo);
			ArrayList<ApprovalLineData> paramList = dao.getApprovalLine(theLine);
			String sCostPlanningUserID = "";

			// 결재선에서 Cost Planning 팀 사용자를 찾는다.
			for(ApprovalLineData map : paramList){
				String sTeamName = map.getTeam_name();
				//[20180302][LJG]원가 기획팀에서 -> 원가 기술 팀으로 변경
				//if (sTeamName.contains("COST PLANNING")) {
				//[20190327][CSH]DESIGN COST TECH 에서 -> ENGINEERING COST 팀으로 변경
//				if (sTeamName.contains("DESIGN COST TECH")) {
				if (sTeamName.contains("ENGINEERING COST")) {
					String sTCMemberPuid = map.getTc_member_puid();
					TCComponentGroupMember tcComponent = (TCComponentGroupMember)session.stringToComponent(sTCMemberPuid);
					sCostPlanningUserID = tcComponent.getProperty("user_name");

					break;
				}
			}

			// Title, Contents Generation
			if (sCostPlanningUserID != null && sCostPlanningUserID.length() > 0) {
				String changeDesc = revision.getProperty("object_desc");
				String fromUser = session.getUser().getUserId();
				String title = "New PLM : [ECO Complete] " + ecoNo + " - " + changeDesc;
				String body = "<PRE>";
				body += "ECO Complete 알림" + "<BR>";
				body += " -ECO NO. : " + ecoNo + "<BR>";
				body += " -Change Desc. : " + changeDesc + "<BR>";
				body += " -Complete Date. : " + DateUtil.getClientDay("yyyy-MM-dd hh:mm:ss") + "<BR>";
				body += "</PRE>";

				dao.sendMail(fromUser, title, body, sCostPlanningUserID);
			} else {
				//2023-10 조직변경 하드 코딩된 그룹명을 Preference로 변경 
				MessageBox.post(AIFUtility.getActiveDesktop(), "The ECO [" + ecoNo + "] doesn't have a approval line of " + PreferenceService.getValue("ENGINEERING COST") + " Role.", "INFORMATION", MessageBox.ERROR);
			}
		}catch(Exception e){
			throw (new Exception("sendMailToCostEngineer error"));
		}
	}

	/**
	 * [20160727][ymjang] 최종 결재 기각시 기각 메일 발송 기능 추가
	 * 기각에 대한 메일 발송 
	 * @throws TCException 
	 */
	private void sendRejecttMail(String toUsers) throws TCException, Exception {
		try{
			String ecNo = revision.getProperty("item_id");
			String products = revision.getProperty("plant_code");
			String changeDesc = revision.getProperty("object_desc");
			String fromUser = session.getUser().getUserId();
			String title = "New PLM : ECO[" + ecNo + "] 결재 기각";
			String body = "<PRE>";
			body += "New PLM에서 아래와 같이 기각되었으니 확인 후 처리 바랍니다." + "<BR>";
			body += " -ECO NO. : " + ecNo + "<BR>";
			body += " -Product : " + products + "<BR>";
			body += " -Change Desc. : " + changeDesc + "<BR>";
			body += " -작성부서 : " + rootTask.getProcess().getProperty("owning_group") + "<BR>";
			body += " -작성자  : " + rootTask.getProcess().getProperty("owning_user") + "<BR>";
			body += "</PRE>";

			if(toUsers == null){
				title = "New PLM : " + ecNo + "결재 기각";
				body = "ECO NO[" + ecNo + "] 결재가 기각되었습니다.";
				//				dao.sendMail(fromUser, title, body, rootTask.getProcess().getProperty("owning_user"));
				TCComponent owningUser = rootTask.getProcess().getReferenceProperty("owning_user");
				dao.sendMail(fromUser, title, body, ((TCComponentUser) owningUser).getUserId());
			}else{
				dao.sendMail(fromUser, title, body, toUsers);
			}
		}catch(Exception e){
			throw (new Exception("sendRejecttMail error"));
		}
	}

	/**
	 * Revision Effectivity 설정
	 * ECO Release Date로 셋팅 됨
	 * 00시 00분 00초 로 리셋 적용
	 * @throws Exception
	 */
	private void setRevisionEffectivity() throws Exception{
		try{
			TCComponentReleaseStatus status=(TCComponentReleaseStatus)revision.getRelatedComponent("release_status_list");
			if(status != null){
				Date releaseDate = revision.getDateProperty("date_released");

				Date adate[] = new Date[1];
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String stringReleasDate = sdf.format(releaseDate);
				// FIXED, 2013.05.15, DJKIM, "ECO Released 날짜의 00:00:00 초"에서 "ECO Released 날짜의 23:59:59 초"로 변경
				adate[0] = sdf.parse(stringReleasDate);
				adate[0].setTime(sdf.parse(stringReleasDate).getTime() + (( (long) 1000 * 60 * 60 * 24 )-1000) );

				TcEffectivityService.createReleaseStatusEffectivity(session, (TCComponent) status, "", null, null, "", adate,
						TCComponentEffectivity.OpenEndedStatus.UP.getPropertyValue(), false);

				// FIXED, 2013.05.15, DJKIM, SOA로 TCComponentOccEffectivity를 setting 하기 위해 객체 생성.
				// [20161117][ymjang] occ effectivity 생성 여부 체크하여 생성되어 있을 경우, eff_date 만 Update 하도록 수정함.
				String ecoNo = revision.getProperty("item_id");
				TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
				TCComponentQuery queryForTarget = (TCComponentQuery) queryType.find("SYMC_occ_effectivity");				
				TCComponent[] cTargets = queryForTarget.execute(new String[] {"effectivity_id"}, new String[] {ecoNo});

				TCComponentOccEffectivityType effType = (TCComponentOccEffectivityType)session.getTypeComponent("CFM_date_info");
				TCComponentOccEffectivity effComp = null;
				if (cTargets != null && cTargets.length > 0) {
					effComp = (TCComponentOccEffectivity) cTargets[0];
					TCProperty tcproperty = effComp.getTCProperty("eff_date");
					tcproperty.setPropertyArrayData(adate);
					effComp.setTCProperty(tcproperty);
				} else {
					effComp = (TCComponentOccEffectivity) effType.create(ecoNo);
					effComp.setStringProperty("id", ecoNo);
					TCProperty tcproperty = effComp.getTCProperty("eff_date");
					tcproperty.setPropertyArrayData(adate);
					effComp.setTCProperty(tcproperty);
				}
				// [20160727][ymjang] java.lang.Exception: setRevisionEffectivity error 오류 개선
				// [20160818][ymjang] 저장안됨 --> 원상복구함.
				effComp.save();
			}
		}catch(Exception e){
			e.printStackTrace();
			throw (new Exception("setRevisionEffectivity error"));
		}
	}
	
	private LinkedList<TCComponentBOMLine> getChildrens(ArrayList<String> findIdList, TCComponentBOMLine topBOMLine){
		LinkedList<TCComponentBOMLine> findedBOMLineList = new LinkedList<TCComponentBOMLine>();
		StructureFilterWithExpand.ExpandAndSearchResponse expandSearchResponse = null;
		StructureFilterWithExpandService strExpandService = StructureFilterWithExpandService.getService(topBOMLine.getSession());
		TCComponentBOMLine[] expandedBOMLines = { topBOMLine };
		
		StructureFilterWithExpand.SearchCondition[] conditions = new StructureFilterWithExpand.SearchCondition[findIdList.size()];
		for (int i = 0; i < conditions.length; i++)
		{
			StructureFilterWithExpand.SearchCondition localSearchCondition = new StructureFilterWithExpand.SearchCondition();
			localSearchCondition.logicalOperator = "OR";
			//ItemID기준
			localSearchCondition.propertyName = "bl_item_item_id";
			//OCC 기준 (DB rocc_threadu와 bl_clone_stable_occurrence_id가 다른 경우도 있어서 Item ID 기준으로 변경)
//			localSearchCondition.propertyName = "bl_clone_stable_occurrence_id";
			localSearchCondition.relationalOperator = "=";
			localSearchCondition.inputValue = findIdList.get(i);
			conditions[i] = localSearchCondition;
		}

		expandSearchResponse = strExpandService.expandAndSearch(expandedBOMLines, conditions);
		
		for (ExpandAndSearchOutput output : expandSearchResponse.outputLines)
		{
			TCComponentBOMLine findedBOMLine = output.resultLine;
//			if (findedBOMLineList.contains(findedBOMLine))
//				continue;
			findedBOMLineList.add(findedBOMLine);
		}
		
		
		return findedBOMLineList;
	}
	
	class SingleUpdateThread extends Thread{
		
		String ppuid = null;
		ArrayList<HashMap<String,String>> rows = null;
		ArrayList<String> childOccList = null;
		ArrayList<HashMap<String,String>> rows_temp = null;
		String ecNo = null;
		
		SingleUpdateThread(String ppuid, ArrayList<HashMap<String,String>> rows, ArrayList<HashMap<String,String>> rows_temp, String ecNo, ArrayList<String> childOccList){
			this.ppuid = ppuid;
			this.rows = rows;
			this.rows_temp = rows_temp;
			this.ecNo = ecNo;
			this.childOccList = childOccList;
		}
		
		public void run(){
			//속도 개선 4안
			//SOA getchild 가져오기
			TCComponentBOMWindow bwParent = null;
			try {
				TCComponentBOMLine[] packedLines = null;
				int rowCount = 0;
				HashMap<String,String> symcBomEditData = null;
				boolean isComplete = false;
				String occ = "";
				LinkedList<TCComponentBOMLine> childLines;
				
//				System.out.println("stringToComponent Start Time : "+ new Date() + " " + ppuid);
				TCComponentItemRevision itemrevision = (TCComponentItemRevision)session.stringToComponent(ppuid);
//				System.out.println("stringToComponent End Time : "+ new Date() + " " + ppuid);
//				System.out.println("Create BOMWindow Start Time : "+ new Date() + " " + saParent[0]);
				bwParent = getBOMWindow(itemrevision, "Latest Released", "bom_view");
//				System.out.println("Create BOMWindow End Time : "+ new Date() + " " + saParent[0]);
				TCComponentBOMLine blParent = (TCComponentBOMLine)bwParent.getTopBOMLine();
//				System.out.println("Get TopBOMLine End Time : "+ new Date() + " " + saParent[0]);
				
//				System.out.println("getChild Start Time : "+ new Date() + " " + saParent[0]);
				childLines = getChildrens(childOccList, blParent);
//				System.out.println("getChild End Time : "+ new Date() + " " + saParent[0]);
				
				for (TCComponentBOMLine cbomLine : childLines) {
					boolean isPacked = cbomLine.isPacked();
					if (isPacked) {
						packedLines = getUnpackBOMLines(cbomLine);
					} else {
						packedLines = new TCComponentBOMLine[] { cbomLine };
					}
					for (TCComponentBOMLine packLine : packedLines) {
						rowCount = rows.size();
						for (int jnx = 0; jnx < rowCount; jnx++) {
							symcBomEditData = rows.get(jnx);
							occ = symcBomEditData.get("OCC_THREADS");
							if (occ.equals(packLine.getProperty("bl_occurrence_uid"))) {
//								System.out.println("InECO 입력 : "+ blParent.toString() + ", "+packLine.toString());
								((SYMCBOMLine) packLine).setProperty_mig("S7_IN_ECO", ecNo);
								rows_temp.remove(symcBomEditData);
								rows.remove(symcBomEditData);
								break;
							}
						}
					}
				}
				
//				for (TCComponentBOMLine cbomLine : childLines) {
//					rowCount = rows.size();
//					isComplete = false;
//					for (int jnx = 0; jnx < rowCount; jnx++) {
//						symcBomEditData = rows.get(jnx);
//						occ = symcBomEditData.get("OCC_THREADS");
//						boolean isPacked = cbomLine.isPacked();
//						if (isPacked) {
//							packedLines = getUnpackBOMLines(cbomLine);
//						} else {
//							packedLines = new TCComponentBOMLine[] { cbomLine };
//						}
//						for (TCComponentBOMLine packLine : packedLines) {
//							if (occ.equals(packLine.getProperty("bl_occurrence_uid"))) {
//								isComplete = true;
////								((SYMCBOMLine) packLine).setProperty_mig("S7_IN_ECO", "");
//								
////								String oldInECO = ((SYMCBOMLine) packLine).getProperty("S7_IN_ECO");
////								if(oldInECO == null || !oldInECO.equals(ecNo)){
//									((SYMCBOMLine) packLine).setProperty_mig("S7_IN_ECO", ecNo);
////	//								((SYMCBOMLine) packLine).setProperty_mig("S7_IN_ECO", "test");
////								}
//								rows_temp.remove(symcBomEditData);
//								rows.remove(symcBomEditData);
//								break;
//							}
//						}
//						if(isComplete){
//							break;
//						}
//					}
//				}

				// Bom Windows Save and Close
				bwParent.save();
				bwParent.close();
//				System.out.println("Parent : "+blParent.toString());
//				System.out.println(rows_temp.size()+"개 남음");
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally{
				if (bwParent != null && !bwParent.isWindowClosed()){
					try{
						bwParent.close();
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	private ArrayList<HashMap<String,String>> start(ArrayList<HashMap<String,String>> rows, String ecNo) throws Exception{
		ArrayList<HashMap<String,String>> rows_temp = null;
		// 1. EPL List 의 Parent 수집
		ArrayList<String> alTempParent = new ArrayList<String>();
		int rowCount = rows.size();
		HashMap<String,String> sbedTemp = null;
		for (int inx = rowCount - 1; inx > -1 ; inx--) {
			sbedTemp = rows.get(inx);
			alTempParent.add(sbedTemp.get("PPUID"));
		}

		// 1.1 Parent 중복 제거
		ArrayList<String> alParent = new ArrayList<String>();
		if (!alTempParent.isEmpty()) {
			String parentPuid = "";
			for (int inx = 0; inx < alTempParent.size(); inx++) {
				parentPuid = alTempParent.get(inx);

				if(!alParent.contains(parentPuid)){
					alParent.add(parentPuid);
				}
			}
		}
		
		// Parent 별 처리대상 그루핑하기
//		System.out.println("Parent 별 처리대상 그룹핑하기 Start Time : "+ new Date());
		HashMap<String,ArrayList<HashMap<String,String>>> phash = new HashMap<String,ArrayList<HashMap<String,String>>>();
		HashMap<String,ArrayList<String>> pHashChildOcc = new HashMap<String,ArrayList<String>>();
		if (!alParent.isEmpty()) {
			int alParentSize = alParent.size();
			String parent = "";
			ArrayList<HashMap<String,String>> parry = null;
			ArrayList<String> childOccList;
			
			for (int inx = 0; inx < alParentSize; inx++) {
				parent = alParent.get(inx);
				parry = new ArrayList<HashMap<String,String>>();
				childOccList = new ArrayList<String>();
				for (int jnx = 0; jnx < rowCount; jnx++) {
					HashMap<String,String> temp = rows.get(jnx);
					if(temp.get("PPUID").equals(parent)){
						parry.add(temp);
						//occ 기준
//						childOccList.add(temp.get("OCC_THREADS"));
						//itemID 기준
						childOccList.add(temp.get("NEW_PART_NO"));
					}
				}
				phash.put(parent, parry);
				pHashChildOcc.put(parent, childOccList);
			}
		}
//		System.out.println("Parent 별 처리대상 그루핑하기 End Time : "+ new Date());

		
		// 2. Parent BOM Window 수집 및 BOM Line Set INECO
		if (!alParent.isEmpty()) {
			int alParentSize = alParent.size();
//			int alParentSize = 1;
			ExecutorService executor = Executors.newFixedThreadPool(10);
			rows_temp = (ArrayList<HashMap<String,String>>)rows.clone();
			String ppuid = "";
			for (int inx = 0; inx < alParentSize; inx++) {
				ppuid = alParent.get(inx);
				SingleUpdateThread t = new SingleUpdateThread(ppuid, phash.get(ppuid), rows_temp, ecNo, pHashChildOcc.get(ppuid));
				executor.execute(t);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
		}
		
		return rows_temp;
	}

	/**
	 * [20160922][ymjang] Pack 된 라인 IN-ECO Update 오류 수정
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private void setInEcoOnBOMLine() throws Exception {
		String ecNo = null;
		SYMCRemoteUtil remote = null;
		DataSet ds = null;
		ArrayList<HashMap<String,String>> rows = null;
		ArrayList<HashMap<String,String>> rows_temp = null;
		int rowCount = 0;
		try {
			ecNo = revision.getProperty("item_id");
			remote = new SYMCRemoteUtil();
			ds = new DataSet();
			ds.put("ecoNo", ecNo);
			// InECO 처리 대상 리스트만 가져오는 쿼리로 변경 (Add/Replace는 BOM 편집시 처리, Revise 건에 대해서만 여기서 처리)
			rows = (ArrayList<HashMap<String,String>>) remote.execute("com.ssangyong.service.ECOHistoryService", "selectInECOlList", ds);

			// 0. Pack 되어있는 Part를 모두 분리하여 기존 결과에 추가.
			rowCount = rows.size();
			for (int inx = rowCount - 1; inx > -1 ; inx--) {
//				SYMCBOMEditData bedTemp = rows.get(inx);
				HashMap bedTemp = rows.get(inx);
				String occ = (String)bedTemp.get("OCC_THREADS");

				if (occ.length() > 14) {
					for (int jnx = 0; jnx < occ.length() / 14; jnx++) {
						HashMap bed = (HashMap)bedTemp.clone();
						String sOccUid = "";
						sOccUid = occ.substring(14 * jnx, 14 * (jnx+1));
						bed.put("OCC_THREADS", sOccUid);

						rows.add(bed);
					}
				}
			}

			// 0.1. Pack 된 항목은 제거.
			rowCount = rows.size();
			for (int inx = rowCount - 1; inx > -1 ; inx--) {
				HashMap<String,String> bedTemp = rows.get(inx);

				if (bedTemp.get("OCC_THREADS").length() > 14) {
					rows.remove(inx);
				}
			}
			
			rows_temp = start(rows, ecNo);

			// 3. 위 로직을 타지 않는 그 외 EPL List에 대해 BOM Line 수정
			// 한번 실행해서 안 잡히면 두번 실행해도 안잡히더라... lock, occ 변경 등....
//			if (rows_temp != null && !rows_temp.isEmpty()) {
//				rows_temp = start(rows_temp, ecNo);
//			}
			
			if (rows_temp != null && !rows_temp.isEmpty()) {
				StringBuffer inEcoMessage = new StringBuffer();
				inEcoMessage.append(rows_temp.size() + "건 InECO 입력 누락 발생\n");
				for (int inx = rows_temp.size() - 1; inx > -1 ; inx--) {
					HashMap hash = rows_temp.get(inx);
					inEcoMessage.append("  모 : " + (String)hash.get("PARENT_NO") + ", 자 : "+ (String)hash.get("NEW_PART_NO") + "\n");
				}
				
//				progress.setStatus("  " + rows_temp.size() + "건 누락 발생");
//				for (int inx = rows_temp.size() - 1; inx > -1 ; inx--) {
//					HashMap hash = rows_temp.get(inx);
//					progress.setStatus("  모 : " + (String)hash.get("PARENT_NO") + ", 자 : "+ (String)hash.get("NEW_PART_NO"));
//				}
				
				sendAdminEmail(inEcoMessage.toString());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw (new Exception("setInEcoOnBOMLine error"));
		} finally {
		}
	}
	
	/**
	 * BOM Window 를 가져옴
	 * 
	 * @param itemRevision
	 *            아이템 리비전
	 * @return
	 * @throws Exception
	 */
	public static TCComponentBOMWindow getBOMWindow(TCComponentItemRevision itemRevision, String ruleName, String viewType) throws Exception {        
		TCComponentBOMWindow bomWindow = null;
		TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession();
		TCComponentBOMViewRevision viewRevision = getBOMViewRevision(itemRevision, viewType);
		// 리비전 룰을 가져옴
		TCComponentRevisionRule revRule = CustomUtil.getRevisionRule(session, ruleName);
		// BOMWindow를 생성
		TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		bomWindow = windowType.create(revRule);
		bomWindow.setWindowTopLine(itemRevision.getItem(), itemRevision, null, viewRevision);

		return bomWindow;
	}



	/**
	 * 리비전 하위에 view와 타입이 일치하는 BOMViewRevision 검색하여 반환한다.
	 * [SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가에 따른 BOMView Revision 권한관련 속성업데이트 기능 추가
	 * 
	 * @param revision
	 *            ItemRevision TCComponent
	 * @param viewType
	 *            뷰타입 String
	 * @return bomViewRevision TCComponentBOMViewRevision
	 * @throws TCException
	 */
	public static TCComponentBOMViewRevision getBOMViewRevision(TCComponent comp, String viewType) throws Exception {
		comp.refresh();

		//Component 타입이 TCComponentBOMLine인 경우에는 getRelatedComponents를 가져오기 위해서 TCComponentItemRevision 으로 변경한다.
		if(comp.getType().equals("BOMLine")) {
			comp = ((TCComponentBOMLine) comp).getItemRevision();
		}

		TCComponent[] arrayStructureRevision = comp.getRelatedComponents("structure_revisions");
		for (TCComponent bvr : arrayStructureRevision) {
			TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) bvr;
			if (bomViewRevision.getReferenceProperty("bom_view").getProperty("view_type").equals(viewType)) {
				return bomViewRevision;
			}
		}

		return null;
	}

	// [20160930][ymjang] Pack 된 라인 IN-ECO Update 오류 수정 (Unpack 후 하도록 재수정)
	public TCComponentBOMLine[] getUnpackBOMLines(TCComponentBOMLine packBOMLine) throws Exception {
		if (packBOMLine == null) {
			return null;
		}
		TCComponentBOMLine[] packedLines = packBOMLine.getPackedLines();
		TCComponentBOMLine[] unpackLines = new TCComponentBOMLine[packedLines.length + 1];
		System.arraycopy(packedLines, 0, unpackLines, 0, packedLines.length);
		packBOMLine.unpack();
		packBOMLine.refresh();
		packBOMLine.parent().refresh();
		unpackLines[unpackLines.length - 1] = packBOMLine;
		return unpackLines;
	}
}