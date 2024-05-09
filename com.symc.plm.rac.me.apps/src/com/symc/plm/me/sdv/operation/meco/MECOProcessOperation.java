package com.symc.plm.me.sdv.operation.meco;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.remote.DataSet;
import com.kgm.dto.ApprovalLineData;
import com.kgm.rac.kernel.SYMCBOPEditData;
import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.workflow.SYMCDecisionDialog;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.PreferenceService;
import com.kgm.common.utils.ProcessUtil;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.MECOProcessCommand;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.sdv.operation.meco.validate.ValidateManager;
import com.symc.plm.me.sdv.operation.meco.validate.ValidateManager.BOPTYPE;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetDataHelper;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEOPRevision;
import com.teamcenter.rac.kernel.TCComponentMEProcessRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentSignoffType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCSignoffOriginType;
import com.teamcenter.rac.kernel.TCTaskState;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.workflow.commands.newperformsignoff.SignoffDecisionOperation;
//import com.symc.plm.me.sdv.dialog.meco.ValidationResultDialog;

/**
 * 최초 ECO 상신 및 ECO 프로세스 진행 중 반려 후 재 상신 시 호출 됨.
 * [SR140820-050][20140808] shcho, MEW는 Team Leader 결재만 하므로 Validate에서 BOPADMIN을 필수로 찾는 것 제외(확인함:이장원(정윤재))
 * [SR150605-007][20150605] shcho, Reject후 재 상신시 상신 오류  The Task "Creator" has not yet completed. 발생 하는 문제 해결
                                         (Creator Task를 던지던 것을 perform-signoffs Task를 던지도록 수정)
 * [SR150715-017][20150717] shcho, 차체 MECO 상신시 검증시간 과다 소요 문제로 Checking MECO EPL을 제거. 
 *                                       그 대신에, 상신 Process 진행 전 수행하도록 변경. (정윤재 수석과 윤순식 차장님 협의 결과임.)
 * @author DJKIM
 *
 */
@SuppressWarnings("unused")
public class MECOProcessOperation extends AbstractAIFOperation {

	private TCSession session;
	private TCComponentChangeItemRevision changeRevision;
	private String mecoNo;
	
	private TCComponentProcess newProcess;
	
	private ArrayList<TCComponent> targetList = new ArrayList<TCComponent>();
	private HashMap<String, ArrayList<TCComponentGroupMember>> reviewers = new HashMap<String, ArrayList<TCComponentGroupMember>>();
	private TCComponent[] solutionList;
	private TCComponent[] problemList;
    private Registry registry = null;
    
	private WaitProgressBar progress;
	private String message;
	
	private CustomMECODao dao = new CustomMECODao();
	private SYMCDecisionDialog parent;
	
	private final static String EVENT_START = "  ▶";
	private DataSet ds = null;
	
	private boolean isOkValidation = true;
	private boolean updateSignOffs = false;
	private String msg = "";
	private TCComponentTask rootTask;
	
	// 수정 삭제 요망
	String existWorkingChildrenList = "";
	
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
	
	public MECOProcessOperation(TCSession session, TCComponentChangeItemRevision changeRevision) {
		this.session = session;
		this.changeRevision = changeRevision;
		registry = Registry.getRegistry(this);
	}
	
	public MECOProcessOperation(SYMCDecisionDialog parent, TCSession session, TCComponentChangeItemRevision changeRevision) {
		this.session = session;
		this.changeRevision = changeRevision;
		this.parent = parent;
		registry = Registry.getRegistry(this);
	}
	
	@Override
	public void executeOperation() throws Exception
	{

		try
		{
			if (parent == null)
			{
				progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
			} else
			{
				progress = new WaitProgressBar(parent);
			}
			progress.setWindowSize(500, 400);
			progress.start();
			progress.setStatus("MCO Workflow creation start.");
			progress.setAlwaysOnTop(true);

			mecoNo = changeRevision.getProperty("item_id");

			// # 0. FIXED, 2013.06.01, 타겟리스트에서 ECORevision이 떨어진 경우 찾아서 붙여 주고, CreateWorkflow인 경우는 메시징
			progress.setStatus(EVENT_START + "Checking MECO has Workflow...", false);
			System.out.println("1.checkHasWorkflow");
			checkHasWorkflow();
			progress.setStatus("is done!");

			// # 3. 결재선 확인
			progress.setStatus(EVENT_START + "Checking approval line...", false);
			System.out.println("2.checkReviewer");
			checkReviewer();
			progress.setStatus("is done!");

			// # 5. ECO 작업 내용[C지]을 보고 솔루션아이템 링크 생성
			//순서 변경... 문제아이템, 솔루션아이템을 모두 다시 찾아내어 MECO 하위에 붙이거나 떼냄....
			progress.setStatus(EVENT_START + "Checking Solution Item(s)...", false);
			System.out.println("3.getSolutionItemsAfterReGenerate");
			solutionList = CustomUtil.getSolutionItemsAfterReGenerate(changeRevision);
			progress.setStatus("is done!");

			// # 4. MECO EPL GENERATE CHECK
			progress.setStatus(EVENT_START + "Checking MECO EPL generation...", false);
			System.out.println("4.checkExistMEPL");
//			checkExistMEPL();
			String org_code = changeRevision.getProperty(SDVPropertyConstant.MECO_ORG_CODE);
			BOPTYPE bopType = getBopType(org_code);
			if (bopType.equals(BOPTYPE.ASSEMBLY) || bopType.equals(BOPTYPE.BODY) || bopType.equals(BOPTYPE.PAINT))
			{
				CustomUtil customUtil = new CustomUtil();
				//솔루션 아이템을 다시 생성하지 않음.... false 값을 넣음.
				//위쪽에서 이미 먼저 솔루션 아이템 및 문제 아이템을 만들도록 했음.
				ArrayList<SYMCBOPEditData> arrResultEPL = customUtil.buildMEPL(changeRevision, false);
			}
			progress.setStatus("is done!");

			progress.setStatus(EVENT_START + "Checking Re-Publish Process Sheet(s)...", false);
			System.out.println("5.checkPublishedProcessSheet");
			checkPublishedProcessSheet();
			progress.setStatus("is done!");

			// # 6. Solution Items에 포함 된 설변대상 리비전 하위에 다른 MECO로 진행 중인 것이 있는지 체크
			progress.setStatus(EVENT_START + "Checking Exist working children(s)...", false);
			System.out.println("6.checkExistWorkingChildren");
			checkExistWorkingChildren();
			progress.setStatus("is done!");

			if (this.mecoNo.toUpperCase().startsWith("MEW") == false)
			{
				progress.setStatus(EVENT_START + "Checking broken link on the top assay...", false);
				System.out.println("6-1.checkBopTypeOfTopItem");
				checkBopTypeOfTopItem();
				progress.setStatus("is done!");
			}

			progress.setStatus(EVENT_START + "Checking parent meco no and used bop...", false);
			System.out.println("7.checkUsed");
			checkUsed();
			progress.setStatus("is done!");

			//# 6. BOP validation by meco type
			progress.setStatus(EVENT_START + "Checking validation BOP...\n", false);
			System.out.println("8.checkBOPValidate");
			checkBOPValidate();
			progress.setStatus("is done!");

			// ## 프로세스 타겟 설정
			progress.setStatus(EVENT_START + "Checking targets...", false);
			System.out.println("9.getTargets");
			getTargets();
			progress.setStatus("is done!");

			// ## 상태 변경
			progress.setStatus(EVENT_START + "Change Status...", false);
			System.out.println("10.changeStatus");
			changeStatus();
			progress.setStatus("is done!");

			// ## 프로세스 생성
			progress.setStatus(EVENT_START + "Creating process...", false);
			System.out.println("11.createProcess");
			createProcess();
			progress.setStatus("is done!");

			// ## 타스크 할당
			progress.setStatus(EVENT_START + "Assigning...", false);
			System.out.println("12.Assigning");
			if (updateSignOffs)
			{
				updateSignoffs();
			} else
			{
				assignSignoffs();
			}
			progress.setStatus("is done!");

			// ## 메일 발송 
			progress.setStatus(EVENT_START + "Mailing...", false);
			System.out.println("13.sendMail");
			sendMail();
			progress.setStatus("is done!");
		} catch (Exception e)
		{
			e.printStackTrace();
			if (progress != null)
			{
				progress.setShowButton(true);
				progress.setStatus("is fail!");
				progress.setStatus("＠ Error Message : ");
				message = " " + e.getMessage();
				rollback();
			}
		} finally
		{
			if (progress != null)
			{
				if (message != null)
				{
					progress.setStatus(message);
					progress.close("Error", true, true);
				} else
				{
					progress.close();
					createCompletePopUp();
					if (parent != null)
						parent.runOperation();
				}
			}
		}
	}
	
	// 타겟리스트에서 MECORevision이 떨어진 경우 찾아서 붙여 주고, CreateWorkflow인 경우는 메시징
	private void checkHasWorkflow() throws Exception {

	    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedTasks 로 교체
		TCComponent[] process_stage_list = changeRevision.getReferenceListProperty(SDVPropertyConstant.PROP_STARTED_WORKFLOW_TASK);
		
		boolean isDuplicatedProcess = false;
		String jobPuid = "";
		String compPuid = "";
		for (int i = 0; i < process_stage_list.length; i++) {
			TCComponent process_stage = process_stage_list[i];
			
			if(process_stage instanceof TCComponentTask) {
				
				if(process_stage!=null && process_stage.getCurrentJob()!=null) {
					rootTask = process_stage.getCurrentJob().getRootTask();
					compPuid = rootTask.getUid();
					
					if("".equals(jobPuid)) {
						jobPuid = compPuid;
					}else{
						if(!jobPuid.equals(compPuid)) {
							isDuplicatedProcess = true;
						}
					}
					jobPuid =process_stage.getCurrentJob().getRootTask().getUid();
					if(!"".equals(BundleUtil.nullToString(jobPuid))){
						isDuplicatedProcess = true;
						continue;
					}
				}
			}
		}
		
		// Workflow 검색
//		if(!dao.workflowCount(changeRevision.getProperty("item_id")).equals("0")){
		if(isDuplicatedProcess) {
//			throw (new Exception("Workflow has been created already.\nCheck the task to perfrom folder in My Worklist, and please proceed by approval."));
			msg = "Workflow has been created already.\nCheck the task to perfrom folder in My Worklist, and please proceed by approval.";
			displayMessage(msg, true);
			isOkValidation = false;
		}
	
	}

	private void createCompletePopUp() {
		final Shell shell = AIFUtility.getActiveDesktop().getShell();

		shell.getDisplay().asyncExec(new Runnable()
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
//			changeRevision.setProperty("s7_AFFECTED_PROJECT", dao.getAffectedProject(ecoNo));
//			dao.updateEcoStatus(changeRevision.getUid(), "In Work", "In Work");

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

//			if(solutionList != null && solutionList.length > 0)
//				changeRevision.remove(SYMCECConstant.SOLUTION_REL, solutionList);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * ECO Affected Project 정보 셋팅
	 * @throws Exception
	 */
//	private void setArrectedProject() throws Exception {
//		String affecedProject = "";
//		String affecedProjects = dao.getAffectedProject(mecoNo);
//		if(affecedProjects != null){
//			String[] affProjs = affecedProjects.split(",");
//			ArrayList<String> affecedProjectList = new ArrayList<String>();
//			for(String affProj : affProjs){
//				if(!affecedProjectList.contains(affProj))
//					affecedProjectList.add(affProj);
//			}
//
//			for(String affProj : affecedProjectList){
//				if(affecedProject.equals("")){
//					affecedProject = affProj;
//				}else{
//					affecedProject = affecedProject+","+affProj;
//				}
//			}
//		}
//		changeRevision.setProperty("s7_AFFECTED_PROJECT", affecedProject);
//	}

	/**
	 * 상태 변경
	 * IitemRevision의 Maturity와
	 * EcoRevision의 Eco Maturity를 업데이트 함.
	 * @throws Exception
	 */
	private void changeStatus() throws Exception {
		boolean isok = false;
		
		if(rootTask == null) {
			TCComponentTaskTemplateType compTaskTmpType = (TCComponentTaskTemplateType)session.getTypeComponent("EPMTaskTemplate");
			TCComponentTaskTemplate template = compTaskTmpType.find(changeRevision.getProperty(SDVPropertyConstant.MECO_WORKFLOW_TYPE), 0);
			
			if(template.getName().contains("MECO_2Level")) {
				isok = dao.updateMEcoStatus(changeRevision.getUid(), "In Approval", "Processing");
			}else if(template.getName().contains("MECO_3Level")) {
				isok = dao.updateMEcoStatus(changeRevision.getUid(), "In Review", "Processing");
			}else if(template.getName().contains("MEW_1Level")) {
				isok = dao.updateMEcoStatus(changeRevision.getUid(), "In Approval", "Processing");
			}else if(template.getName().contains("MEW_2Level")) {
				isok = dao.updateMEcoStatus(changeRevision.getUid(), "In Review", "Processing");
			}
		}else{
			
			if(rootTask.getName().contains("MECO_2Level")) {
				isok = dao.updateMEcoStatus(changeRevision.getUid(), "In Approval", "Processing");
			}else if(rootTask.getName().contains("MECO_3Level")) {
				isok = dao.updateMEcoStatus(changeRevision.getUid(), "In Review", "Processing");
			}else if(rootTask.getName().contains("MEW_1Level")) {
				isok = dao.updateMEcoStatus(changeRevision.getUid(), "In Approval", "Processing");
			}else if(rootTask.getName().contains("MEW_2Level")) {
				isok = dao.updateMEcoStatus(changeRevision.getUid(), "In Review", "Processing");
			}
		}
		
		if(!isok){
			throw (new Exception("changeStatus() Method Error"));
		}
	}

	/**
	 * MECO EPL 체크
	 * @throws Exception 
	 */
	private void checkExistMEPL() throws Exception{
//		ds = new DataSet();
//		ds.put("mecoNo", mecoNo);
//		boolean resultList = dao.checkExistMEPL(ds);
		
		//viewer 창에서 버튼 클릭 시 동작하던 것을 이쪽으로 옮겨서 수행하게 함.
		String org_code = changeRevision.getProperty(SDVPropertyConstant.MECO_ORG_CODE);
		BOPTYPE bopType = getBopType(org_code);
		if (bopType.equals(BOPTYPE.ASSEMBLY) || bopType.equals(BOPTYPE.BODY) || bopType.equals(BOPTYPE.PAINT))
		{
			CustomUtil customUtil = new CustomUtil();
			//솔루션 아이템을 다시 생성하지 않음.... false 값을 넣음.
			//위쪽에서 이미 먼저 솔루션 아이템 및 문제 아이템을 만들도록 했음.
			ArrayList<SYMCBOPEditData> arrResultEPL = customUtil.buildMEPL(changeRevision, false);
		}

    	Vector<String> notPublishedV = new Vector<String>();
    	// [NON-SR][20160829] taeku.jeong 좀많이 복잡해진 경향이 있는데
    	// Operation의 그림만 변경된 경우 추가적인 확인이 필요하다.
    	// EPL 생성이 필요한데 누락한 것이 있으면 알려 줘야 한다.
    	int missCount = 0;
		String mecoId = null;
		MECOCreationUtil aMECOCreationUtil = null;
		if(changeRevision!=null){
			
			mecoId = changeRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			aMECOCreationUtil = new MECOCreationUtil(changeRevision);
			//MEPL 테이블에 현재 솔루션에 존재하는 리비전들이 parent에 들어있지 않은 것들을 찾아온다.
			ArrayList<HashMap> resultList = aMECOCreationUtil.getMissingMEPLObjectList(mecoId);
			for (int i = 0; resultList!=null && i < resultList.size(); i++) {
				HashMap rowHash = resultList.get(i);
				String itemRevPuid = (String) rowHash.get("ITEM_REV_PUID");
				if(itemRevPuid!=null){
					TCComponent component = session.stringToComponent(itemRevPuid);
					if(component!=null && component instanceof TCComponentItemRevision){
						TCComponentItemRevision tempRev = (TCComponentItemRevision)component;
						
						String currentItemId = null;
						String currentRevId = null;
						String oldRevId = null;
						
						TCComponentItemRevision baseOnRevision = null;
						String baseOnItemId = null;
						String baseOnRevId = null;
						try {
							currentItemId = tempRev.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
							currentRevId = tempRev.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
							baseOnRevision = tempRev.basedOn();
							if(baseOnRevision!=null){
								baseOnItemId = baseOnRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
								baseOnRevId = baseOnRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
							}
						} catch (TCException e1) {
							e1.printStackTrace();
						}
						
						if(baseOnItemId!=null && baseOnItemId.trim().equalsIgnoreCase(currentItemId.trim())){
							oldRevId = baseOnRevId;
						}else{
							oldRevId = "";
						}
						//mepl의 parent에 들어있지 않은 아이템인데 하위에 변경된 아이템이 존재하면 뭔가 이상함.
						//하위에 변경된 아이템이 내가 낸 MECO가 아니면 무시하고 내가 낸 MECO에 해당하는거면 에러인 것임.
						ArrayList<CompResultDiffInfo> diffListArray = aMECOCreationUtil.getDifrentList(currentItemId, oldRevId, currentRevId);
						//m7_MECO_NO
						if(diffListArray != null)
						{
							for(CompResultDiffInfo compResultDiffInfo : diffListArray)
							{
								String itemPuid = compResultDiffInfo.newChildItemu;
								if(itemPuid == null || itemPuid.trim().isEmpty())
								{
									continue;
								}
								TCComponentItem childItem = (TCComponentItem) session.stringToComponent(itemPuid);
								TCComponent tmpMecoRevision = childItem.getLatestItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
								if(changeRevision.equals(tmpMecoRevision))
								{
									missCount = missCount+1;
									notPublishedV.add(currentItemId+"/"+currentRevId);
									break;
								}
							}
						}
					}
				}
			}
		}
		
		System.out.println("missCount = "+missCount);

		if(missCount>0){

//			final Shell shell = AIFUtility.getActiveDesktop().getShell();
//
//			shell.getDisplay().syncExec(new Runnable()
//			{
//
//				public void run()
//				{
//					ValidationResultDialog dialog = new ValidationResultDialog(shell, SWT.NONE, mecoNo, resultList);
//					dialog.open();
//
//				}
//
//			});
//			throw (new Exception("Please check the MECO EPL."));
			msg = "Does not exist change history. Please check the MECO EPL.\n"+notPublishedV.toString();
			displayMessage(msg, true);
			isOkValidation = false;
    	}
		
    	if(msg!=null && msg.trim().length()>0 ){
    		throw new Exception("Check MECO EPL!!");
    	}
	}

	/**
	 * BOM Structure 상에서 end item 밑에 end item이 존재하면 않된다
	 * @throws TCException
	 * @throws Exception
	 */
//	private void checkEndToEnd() throws TCException, Exception {
//		ArrayList<String> resultList = dao.checkEndtoEnd(mecoNo);
//		if(resultList !=null && resultList.size() > 0){
//			String list = "";
//			for(String result : resultList){
//				list += result + "\n";
//			}
//			throw (new Exception("Cannot make end item to end item structure.\nCheck belows and fix it.\n"+list));
//		}
//	}

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
//			TCComponentTask rootTask = changeRevision.getCurrentJob().getRootTask();
			TCComponentTask[] subTasks = null;
			if(rootTask!=null){
				subTasks = rootTask.getSubtasks();
			}
			paramList = new ArrayList<ApprovalLineData>();
			
			for (int i = 0;subTasks!=null && i < subTasks.length; i++) {
				
				TCComponentTask subTask = subTasks[i];

				if(subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) 
						|| subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)){
					if(!subTask.getName().equals("Creator")){
						TCComponentTask performSignoffTask = subTask.getSubtask("perform-signoffs");
				        TCComponentSignoff[] signoffs = null;
				        if(performSignoffTask!=null){
				        	performSignoffTask.getValidSignoffs();
				        }
				        
				        for (int j = 0; signoffs!=null && j < signoffs.length; j++) {
				        	TCComponentSignoff signoff = signoffs[j];
				        	
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
		
		if(paramList == null || paramList.size() < 1) {
//			throw (new Exception("Cannot find approval line.\nPlease check the approval line."));
			msg="Cannot find approval line.\nPlease check the approval line.";
			displayMessage(msg, true);
			isOkValidation = false;
			
		}
		
		ArrayList<String> taskList = CustomUtil.getWorkflowTask(changeRevision.getProperty(SDVPropertyConstant.MECO_WORKFLOW_TYPE), session); 
		//타스크별 TCComponentGroupMember생성 맵핑 및 필수 지정 결재선 확인
		ArrayList<String> requiredAssingTask = new ArrayList<String>();

		for(String task : taskList){
//			if(task.equals("Sub-team Leader") || task.equals("References")) continue;
			if(task.equals("References")) continue;
			requiredAssingTask.add(task);
		}
		
		String mapTask = "";
		TCComponentGroupMember groupMember = null;
		ArrayList<TCComponentGroupMember> groupMemberList = null;
		
		boolean isSkip = false;
		for(ApprovalLineData map : paramList){
			mapTask = map.getTask();
			
			// FIXED 2013.05.14, DJKIM, 박수경 CJ: 사용자의 상태 변경이 발생 할수 있으므로 사용자 상태 확인하여 부적절한 사용자가 결재선에 할당 되지 않도록 함.
			try{
				if("References".equals(map.getTask()) || "Creator".equals(map.getTask())){
					isSkip = true;
				}else{
					isSkip = false;
					groupMember =  (TCComponentGroupMember) session.stringToComponent(map.getTc_member_puid());
				}
			}catch(Exception e){
				throw (new Exception("Cannot find group member.\n"+map.getUser_name() + " in " + map.getTeam_name() + " is removed. \nPlease check and replace him."));
			}
			
			if(!isSkip){
				
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
		}

		if(requiredAssingTask.size() > 0){
			String addTasks = "";
			for(String requiredAssingTaskName : requiredAssingTask){
				addTasks = addTasks+requiredAssingTaskName+"\n";
			}
			throw (new Exception("Workflow task checking information.\nPlease, Add the following tasks.\n"+addTasks));
		}
		
		// FIXED 결재선에 생산기획[BOPADMIN], 팀장롤[TEAM_LEADER]이 하나 이상인지 체크 함.
		PreferenceService.createService(session);
		String checkRole = PreferenceService.getValue("SYMC_MECO_WF_CHECK_ROLE"); // COST_ENGINEER,BOMADMIN,TEAM_LEADER
		if(checkRole.equals("")){
			checkRole = "BOPADMIN,TEAM_LEADER";
		}
		
		// [SR140820-050][20140808] shcho, MEW는 Team Leader 결재만 하므로 Validate에서 BOPADMIN을 필수로 찾는 것 제외(확인함:이장원(정윤재))
		String mecoType = changeRevision.getProperty(SDVPropertyConstant.MECO_TYPE);
		if(mecoType.equalsIgnoreCase("MEW")) {
            checkRole = "TEAM_LEADER";
		}
		//-----------------------------------------------------------------------------------------------------
		
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
     * 신규 파트중 EPL의 Category가 DR1/2로 지정된 파트가 존재 하면 인증팀이 필수로 지정되어야 함.
     * 2013.01.10
     * REQ. 송대영
     * REF. 정상일
     * @return
     * @throws Exception
     */
//    private void checkCertification() throws Exception {
//        boolean hasCertificationPart = false;
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
	
    private boolean getkBopTypeOfTopItem (TCComponentItemRevision itemrevision, TCComponentRevisionRule revRule) throws Exception {
    	
    	boolean isOk = false;
    	
    	if(itemrevision!=null && itemrevision instanceof TCComponentMEProcessRevision) {
    	     //[SR번호없음][20140929] shcho, 이종화 찾아님께 BOP_PROCESS_SHOP_ITEM_REV 넣은 내용 확인하기
    		 String  type = itemrevision.getType();
    		 
    		 System.out.println("type = "+type);
    		 
    		 if(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV.equals(type) || SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV.equals(type)) {
    			 return true;
    		 }
    	}
    	
    	boolean returnBoolean = false;
    	TCComponent[] imanComps = null;
    	if(itemrevision!=null){
    		imanComps = itemrevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
    	}
    	//for(TCComponent tcomponent : imanComps) {
    	for (int i = 0;imanComps!=null && i < imanComps.length; i++) {
    		
    		TCComponent tcomponent = imanComps[i];
    		
    		isOk = getkBopTypeOfTopItem((TCComponentItemRevision)tcomponent, revRule);
    		
    		if(isOk) {
    			returnBoolean = true;
    			break;
    		}
    	}
    	
    	return returnBoolean;
    	
    }
    
    
    private void checkBopTypeOfTopItem() throws Exception {
    	
    	String noWhereUsedList = "";
    	String returnMessage = "";
    	boolean isOk;
    	TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(CustomUtil.getTCSession(), "Latest Working For ME");
    	//for(TCComponent solutionItemComponent : solutionList) {
    	for (int i = 0;solutionList!=null && i < solutionList.length; i++) {
    		TCComponent solutionItemComponent = solutionList[i];
    		
    		System.out.println( "["+i+"/"+solutionList.length+"] ("+df.format(new Date())+") : "+solutionItemComponent.toString() );
    		isOk = getkBopTypeOfTopItem((TCComponentItemRevision)solutionItemComponent, revRule);
    		if(!isOk) {
    			noWhereUsedList = noWhereUsedList + (TCComponentItemRevision)solutionItemComponent+ "\n";
    		} 
    	}
    	
    	if(!noWhereUsedList.equals("")){
    		returnMessage = returnMessage + "\nThe items seem to be broken link on the line or the shop..\nCheck parent BOP of below items : \n"+noWhereUsedList;
    	}
    	
    	if(!"".equals(returnMessage)) {
    		
    		msg = returnMessage;
    		displayMessage(msg, true);
    		isOkValidation = false;
    	}
    }
    
    @SuppressWarnings("null")
    private void checkUsed() throws Exception {
    	
    	// EMCO 상신 과정에 Parent Node의 MECO No를 확인 하는 부분임
    	// 여기서 Validation 할때 Parent Node에서 추가 하거나 삭제한 Station, Operation이 아닌경우
    	// Parent Node의 MECO No와 Child Node인 Station 또는 Operation의 MECO No가 동일 하지 않아도 되도록 해줘야 한다.
    	// Validation 시간이 오래 걸리지 않으면서 해당 Validation을 할 수 있도록 기능을 수정해야 한다.
    	
    	String noWhereUsedList = "";
    	String unMatchMecoList = "";
    	String returnMessage = "";
		CustomMECODao aCustomMECODao = new CustomMECODao();
    	
    	for(TCComponent solutionItemComponent : solutionList){
    		
    		TCComponentItemRevision currentSolutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
    		if(currentSolutionItemrevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID).equals(SDVPropertyConstant.ITEM_REV_ID_ROOT)) {
    			
    			String currentSolutionItemId = currentSolutionItemrevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
    			
        		TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(solutionItemComponent.getSession(), "Latest Working For ME");
        		
        		if(!currentSolutionItemrevision.getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV)
        				&& !currentSolutionItemrevision.getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV)
        				) {
        			
        			TCComponent[] whereUsedResultComponents = currentSolutionItemrevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);	
        			
        			if(whereUsedResultComponents.length ==0) {
        				noWhereUsedList = noWhereUsedList + currentSolutionItemrevision + "\n";
        			}else{
        				TCComponentItemRevision aWhereUsedResultItemRevision = null;
        				for(TCComponent currentWhereUsedResultComponent : whereUsedResultComponents ){
        					
        					if(currentWhereUsedResultComponent instanceof TCComponentItem) {
        						aWhereUsedResultItemRevision = ((TCComponentItem) currentWhereUsedResultComponent).getLatestItemRevision();
        					}else if( currentWhereUsedResultComponent instanceof TCComponentItemRevision) {
        						aWhereUsedResultItemRevision = (TCComponentItemRevision)currentWhereUsedResultComponent;
        					}
        					
        					//If meco'org_code is "PA" and the type is line, skip to check.
        					if(SDVPropertyConstant.MECO_ORG_CODE_PA.equals(changeRevision.getProperty(SDVPropertyConstant.MECO_ORG_CODE))) {
        						
        						if(aWhereUsedResultItemRevision instanceof TCComponentMEProcessRevision) {
            						if(aWhereUsedResultItemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID).contains("TEMP")) {
            							continue;
            						}
        						}
        					}
        					

        					// [NON-SR][20160219] taeku.jeong Parent MECO No Check 조건 추가 (MECO 분리 진행을 위함)
        					// 여기서 Parent Revision의 Child Node인 solutionItemrevision이 추가 되거나 제거된 Item Revision인지 확인이 필요하다.
        					// Parent Item Revision이 Working 중인경우 MECO No 확인 대상임
        					// 1. Parent Item Revision이 000인경우 -> 모두 추가되는 Item임.
        					// 2. Parent Item Revision이 000가 아닌경우 -> 현재 Item이 추가 되거나 제거된 Item인지 확인필요함.
        					//     a. BaseOnRevision에 존재하지 않는 Item인 경우 추가된 Item이므로 Parent Node의 MECO와 같아야함.
        					boolean isParentReleased = SYMTcUtil.isReleased(aWhereUsedResultItemRevision);
        					boolean isParentNodeMecoNoCompTarget = false;
        					TCComponentItemRevision baseOnItemRevision = null;
        					String tempMessage = null;
        					if(isParentReleased==false){
        						
        						// Parent Node가 Released Status가 없는 경우는 신규인 경우와 이전 Revision에 없는 것이 추가(변경)된 경우에 대해서는
        						// Child Node와 Parent Node의 MECO No가 동일 해야 한다.
        						
        						baseOnItemRevision = aWhereUsedResultItemRevision.basedOn();
            					if(baseOnItemRevision!=null){
            						// Working 중인 Revision이므로 추가된 Child 인경우 Child Node와 Parent Node의 MECO는 동일해야 한다.
            						String currentParentItemId = aWhereUsedResultItemRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            						String baseOnItemItemId = baseOnItemRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            						
            						if(currentParentItemId!=null && baseOnItemItemId!=null && baseOnItemItemId.trim().equalsIgnoreCase(currentParentItemId.trim())==true){
            							// Item이 Version Up 된 경우 이므로 Parent Node의 Old와 New 중에 현재의 Item이 추가 또는 제거된 상황인지 확인 해야 한다.
            							// 만약에 currentSolutionItemrevision이 Parent Node인 aWhereUsedResultItemRevision의  
            							String newRevId = aWhereUsedResultItemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            							String oldRevId = baseOnItemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            							Vector<String> changedNewItemIdV = aCustomMECODao.getChangedNewItemIdList(currentParentItemId, newRevId, oldRevId);
            							if(changedNewItemIdV!=null && changedNewItemIdV.contains(currentSolutionItemId)==true){
            								// Parent Node Meco Id가 동일해야 한다.
            								isParentNodeMecoNoCompTarget = true;
            								tempMessage = "Child Node Added";
            							}
            						}
            					}else{
            						// 최초의 Revision이므로 Child Node와 Parent Node의 MECO는 동일해야 한다.
            						isParentNodeMecoNoCompTarget = true;
            						tempMessage = "Newly created Parent Node";
            					}
        					}
        					
        					System.out.println("isMecoNoCompTarget = "+isParentNodeMecoNoCompTarget+"\n"
        							+ "Message String = "+tempMessage+"\n"
        							+ "currentSolutionItemrevision = "+currentSolutionItemrevision+"\n"
        							+ "Parent ItemRevision = "+aWhereUsedResultItemRevision+"\n"
									+ "Parent BaseOnItemRevision = "+baseOnItemRevision+"\n"
        							+ "isParentReleased = "+isParentReleased
									);
        					
    						String parentNodeMECONo = null;
    						String childNodeMECONo = null;
    						String targetMECONo = null;
    						TCComponentItemRevision mecoRevision = (TCComponentItemRevision) aWhereUsedResultItemRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
    						if(mecoRevision!=null){
    							parentNodeMECONo = mecoRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
    						}
    						TCComponentItemRevision childNodeMecoRevision = (TCComponentItemRevision) currentSolutionItemrevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
    						if(childNodeMecoRevision!=null){
    							childNodeMECONo = childNodeMecoRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
    						}
    						if(changeRevision!=null){
    							targetMECONo = changeRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
    						}
    						
    						//System.out.println("targetMECONo = "+targetMECONo+", parentNodeMECONo = "+parentNodeMECONo+", childNodeMECONo = "+childNodeMECONo);
    						boolean isDifferentMeco = false;
    						if(targetMECONo!=null){
								if(isParentNodeMecoNoCompTarget==true){
									
									if(parentNodeMECONo==null || (parentNodeMECONo!=null && parentNodeMECONo.trim().equalsIgnoreCase(targetMECONo)==true)){
										if(parentNodeMECONo==null || (parentNodeMECONo!=null && parentNodeMECONo.trim().equalsIgnoreCase(targetMECONo)==false)){
											isDifferentMeco = true;
										}
									}else{
										// 이건 어떻게 처리 해야 할지....
									}
								}
    						}
    						
    						if(isDifferentMeco==true){
    							unMatchMecoList = unMatchMecoList + "MECO Number Mismatch : "+aWhereUsedResultItemRevision +"("+parentNodeMECONo+")  <-> "+currentSolutionItemrevision+"("+childNodeMECONo+") Target MECO : "+targetMECONo+ "\n";
    							//System.out.println("여기 걸린건데.....\n"+unMatchMecoList);
    						}

							// 변경전 기존 코드
    						// [NON-SR][20160222] taeku.jeong MECO 상신 과정에 Parent Node와 Child Node의 MECO No 동일한지 검토하는 조건 변경
							//if(null !=mecoRevision) {
							//	// Parent Node와 Child Node의 MECO가 다른 경우를 확인함.
							//	if(!((changeRevision.getItem()).getProperty(SDVPropertyConstant.ITEM_ITEM_ID)).equals((mecoRevision.getItem()).getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
							//	unMatchMecoList = unMatchMecoList + currentSolutionItemrevision + " is used by bop: "+aWhereUsedResultItemRevision +" meco: "+mecoRevision+ "\n";
							//	}
							//}else{
							//	if(!((changeRevision.getItem()).getProperty(SDVPropertyConstant.ITEM_ITEM_ID)).equals((mecoRevision.getItem()).getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
							//		unMatchMecoList = unMatchMecoList + currentSolutionItemrevision + " is used by bop: "+aWhereUsedResultItemRevision +" , The bop has not meco no."+ "\n";
							//	}
							//}
        					
        				}
        			}
        		}
    		}

    	}
    	
    	// 제거된 Item중에서도 Parent Node의 MECO No
    	// 이것은 필요없을것 같다.
    	// Child Node가 제거되는 설변의 경우 Parent Node가 Solution Items에 포함되고
    	// Parent Node MECO에 포함되어 있을 것이므로 중복 Check 하게되는 결과가 된다.
    	// [NON-SR][20160219] taeku.jeong
    	//for (int i = 0; i < problemList.length; i++) {
    	//	TCComponent problemItemComponent = problemList[i];
		//}
    	
    	
    	if(noWhereUsedList!=null && noWhereUsedList.trim().length()>10){
    		returnMessage = returnMessage + "\nThe items are not used in BOP.\nCheck parent BOP of below items : \n"+noWhereUsedList;
    	}
    	
    	if(unMatchMecoList!=null && unMatchMecoList.trim().length()>10){
    		returnMessage = returnMessage + "\nThe items are not same as parent' meco no.\nCheck parent BOP of below items : \n"+unMatchMecoList;
    	}
    	
    	if(returnMessage!=null && returnMessage.trim().length()>10){
//    		throw (new TCException(returnMessage));
			msg = returnMessage;
			displayMessage(msg, true);
			isOkValidation = false;
    	}
    	
    }

	private void checkExistWorkingChildren() throws Exception
	{
		String returnMessage = "";
		String mecoId = changeRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

		if (solutionList == null)
		{
			return;
		}
		for (TCComponent solutioncomponent : solutionList)
		{
			String parentItemId = solutioncomponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			String parentRevId = solutioncomponent.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
			ArrayList<HashMap> resultList = MECOCreationUtil.getBOPChildErrorList(mecoId, parentItemId, parentRevId);
			for (HashMap<String, String> errorHashmap : resultList)
			{
				existWorkingChildrenList = existWorkingChildrenList + "결재 상신 MECO Solution Item : " + solutioncomponent + ", " + "결재 상신 MECO : " + errorHashmap.get("MECO_ID") + ", " + "미 결재 Item : " + errorHashmap.get("PITEM_ID") + "/" + errorHashmap.get("PITEM_REVISION_ID") + "-" + errorHashmap.get("POBJECT_NAME") + "\n";
				;
			}
		}
		if (!"".equals(existWorkingChildrenList))
		{
			returnMessage = returnMessage + "\nThe items contain working children(s). Your meco is able to process after release children(s) : \n" + existWorkingChildrenList;
		}
		if (!"".equals(returnMessage))
		{
			msg = returnMessage;
			displayMessage(msg, true);
			isOkValidation = false;
		}
		if (returnMessage != null && returnMessage.trim().length() > 0)
		{
			System.out.println(returnMessage);
			throw new Exception("Check Working children(s) !!");
		}
	}
	
	private boolean isWorkflowComplete(TCComponentItemRevision itemRevision){
		
		boolean isWorkflowComplete = false;
		
		TCComponentProcess process = null;
		try {
			process = CustomUtil.getWorkFlowProcess(itemRevision);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		if(process!=null){

			TCTaskState state = null;
			try {
				state = process.getRootTask().getState();
			} catch (TCException e) {
				e.printStackTrace();
			}
			
			//TCTaskState.COMPLETED 와 비교
			if(state!=null && state.getIntValue()==TCTaskState.COMPLETED.getIntValue() ){
				// Complete 임.
				isWorkflowComplete = true;
			}
		}
		
		return isWorkflowComplete;
	}
	
	
    /**
     * 결재 타켓 확인
     * check out 여부도 같이 확인
     * dataset 속성에 eco no도 입력
     * @throws TCException
     */
    private void getTargets() throws TCException, Exception{
    	
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
    	String noChildrenBVRList = "";
    	
    	targetList.add((TCComponent)changeRevision);
    	ArrayList<TCComponent> datasetList = ProcessUtil.getDatasets(changeRevision, "IMAN_specification");
    	for(TCComponent dataset : datasetList){
    		if(dataset.isCheckedOut()){
    			checkOutlist = checkOutlist + dataset + "\n";
    		}else{
    			//FIXED, 2013.06.12, YUNJAE, only adding Working dataset
    			if(ProcessUtil.isWorkingStatus(dataset) && !dataset.getType().equals("PDF")) {
//    				targetList.add(dataset);
    			}
    		}
    	}
    	
    	for(TCComponent solutionItemComponent : solutionList){
    		TCComponentItemRevision solutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
    		
    		if(solutionItemrevision.isCheckedOut()){
    			checkOutlist = checkOutlist + solutionItemrevision + "\n";
    		}else{
//    			targetList.add(solutionItemrevision);
    		}
    		
//    		TCComponentBOMViewRevision view = (TCComponentBOMViewRevision) solutionItemrevision.getRelatedComponent("structure_revisions");
//    		
//    		if(view != null){
//        		if(view.isCheckedOut()){
//        			checkOutlist = checkOutlist + view + "\n";
//        		}else{
//        			// FIXED, 2013.06.01, DJKIM 하위 구조가 있는지 없는지 체크
//        			if(dao.childrenCount(view.getUid()).equals("0")){
//        				noChildrenBVRList = noChildrenBVRList + solutionItemrevision + "\n";
//        			}else{
//        				targetList.add(view);
//        			}
//        		}
//    		}
    		
//    		ArrayList<TCComponent> solutionDatasetList = ProcessUtil.getDatasets(solutionItemrevision, "IMAN_specification");
//    		for(TCComponent dataset : solutionDatasetList){
//    			if(ProcessUtil.isWorkingStatus(dataset) && !dataset.getType().equals("PDF")){
//            		if(dataset.isCheckedOut()){
//            			checkOutlist = checkOutlist + dataset + "\n";
//            		}else{
////            			if(!solutionItemrevision.getType().equals("S7_StdpartRevision")){
////            				dataset.setProperty("s7_ECO_NO", ecoNo);
////            			}
//            			targetList.add(dataset);
//            		}
//    			}
//    		}
    		

    	}
    	

    	
    	String retrunMessage = "";
    	if(!checkOutlist.equals("")){
    		retrunMessage = "Check-out Componet is exist.\nCheck belows and fix it.\n"+checkOutlist;
    	}
    	
    	// FIXED, 2013.06.01, DJKIM 하위 구조가 있는지 없는지 체크
    	if(!noChildrenBVRList.equals("")){
    		retrunMessage = retrunMessage + "\nThe item that does not have a sub-structure exists.\nCheck BOMViewResion of below items.\n"+noChildrenBVRList;
    	}
    	
    	if(!retrunMessage.equals("")){
			msg = retrunMessage;
			displayMessage(msg, true);
			isOkValidation = false;
    	}

    	// 반려 일 경우  타켓 재설정
    	if(parent != null && rootTask != null){   		
    		// 타겟 재설정
    		rootTask.add("root_target_attachments", targetList);
    	}
    }
    
    /**
     * 프로세스 생성
     * @throws Exception
     */
    private void createProcess() throws Exception{

	    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체
		String[] processProps = changeRevision.getProperties(new String[]{"date_released", "fnd0StartedWorkflowTasks"});
//		if(processProps.length >2) {
//			
//			throw new Exception("Retry to request an approval after should confirm a acknowledge task in your worklist.");
//			
//		}
		if(!processProps[1].equals("") )
//				&& (processProps[1].contains("Creator"))) 
				{
			TCComponent[] process_stage_list = changeRevision.getReferenceListProperty(SDVPropertyConstant.PROP_PROCESS_STAGE_LIST);
			AIFComponentContext[] aifComponentContexts = null;
			TCComponent process_stage = process_stage_list[0];
			newProcess = ((TCComponentTask) process_stage).getProcess();
			aifComponentContexts =  newProcess.getChildren();
//			for(TCComponent process_stage : process_stage_list) {
//				
//				if(process_stage instanceof TCComponentTask) {
//					
//					if(((TCComponentTask) process_stage).getProcess()!=null) {
//						
//						newProcess = ((TCComponentTask) process_stage).getProcess();
//						
//						newProcess.setProperty("ip_classification", "secret");
//
//						aifComponentContexts =  newProcess.getChildren();
//						
//						break;
////						process_stage.processPendingCut();
////						newProcess.setPendingCut(true);
////						newProcess.processPendingCut();
//					}
//				}
//				newProcess.setProperty("ip_classification", "secret");
//				newProcess.getCurrentJob().delete();
//				newProcess.delete();
				
//			}
			
			TCComponentTask tccomponentTask = null;
			for(AIFComponentContext aifComponentContext : aifComponentContexts) {
				if(aifComponentContext.getComponent() instanceof TCComponentTask) {
					tccomponentTask = (TCComponentTask)aifComponentContext.getComponent();
					if("EPMReviewTask".equals(tccomponentTask.getTaskType())) {
						((TCComponentTask)aifComponentContext.getComponent()).performAction(4, "");	
					}
					
				}
			}
			updateSignOffs = true;
			
		}else{
		TCComponentTaskTemplateType compTaskTmpType = (TCComponentTaskTemplateType)session.getTypeComponent("EPMTaskTemplate");
		TCComponentTaskTemplate template = compTaskTmpType.find(changeRevision.getProperty(SDVPropertyConstant.MECO_WORKFLOW_TYPE), 0);

		TCComponentProcessType processtype = (TCComponentProcessType)session.getTypeComponent("Job");
		
		TCComponent[] componentList = new TCComponent[targetList.size()];
		for(int i = 0 ; i < targetList.size() ; i++){
			componentList[i] = targetList.get(i);
		}
		
		newProcess = (TCComponentProcess)processtype.create("MECO [" + changeRevision.getProperty("item_id") + "] approval request.", "Please confirm ASAP.", template, componentList, ProcessUtil.getAttachTargetInt(componentList));
		newProcess.setProperty("ip_classification", "");
		}

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
	

	@SuppressWarnings("rawtypes")
    private void updateSignoffs() throws Exception {
		
        TCComponentTask rootTask = newProcess.getRootTask();
        
        TCComponentTask[] subTasks = rootTask.getSubtasks();
        TCComponentSignoffType signoffType = (TCComponentSignoffType) session.getTypeComponent("Signoff");
        for(TCComponentTask subTask : subTasks) {
        	String reviewTaskName = subTask.getName();
        	TCComponentTask selectSignoffTeam = subTask.getSubtask("select-signoff-team");
        	
        	ArrayList<TCComponentGroupMember> taskReviewers = new ArrayList<TCComponentGroupMember>();
        	Vector signoffTeams = new Vector();
        	try{
        		if(selectSignoffTeam == null) continue;
        		
        		selectSignoffTeam.lock();
        		signoffTeams = selectSignoffTeam.getAllAttachments(TCAttachmentScope.LOCAL);
//        		selectSignoffTeam.getAttachments(paramTCAttachmentScope, paramInt)
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
        		String userId= "";
        		String groupId="";
        		String role="";
        		TCComponentSignoff tccomponentsignoff = null;
        		
        		for(TCComponentGroupMember reviewMember :taskReviewers){
        			userId= reviewMember.getUserId();  //old
        			groupId = reviewMember.getGroup().getGroupName();
        			role = reviewMember.getRole().getObjectString();
        			tccomponentsignoff = (TCComponentSignoff) signoffTeams.elementAt(0);
        			
        			if(!userId.equals(tccomponentsignoff.getGroupMember().getUserId())) {
    					TCComponentSignoff signoffObj = selectSignoffTeam.getValidSignoffs()[0];
    					signoffObj.setGroupMember(reviewMember);
//            			sifnoffList[i] = signoffType.create(reviewMember, TCSignoffOriginType.ADHOC_USER, null);
            			
            			attachTypeList[i] = TCAttachmentType.SIGNOFF;
            			i++;
        			}else{
            			sifnoffList[i] = signoffType.create(reviewMember, TCSignoffOriginType.ADHOC_USER, null); 
            			attachTypeList[i] = TCAttachmentType.SIGNOFF;
            			i++;
        			}


        		}

//				selectSignoffTeam.getTCProperty("done").setLogicalValue(true);
				selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
				selectSignoffTeam.save();
				selectSignoffTeam.performAction(TCComponentTask.ASSIGN_APPROVER_ACTION, "");
				
				if(selectSignoffTeam.getParent().getState().equals(TCTaskState.STARTED)) {
//					selectSignoffTeam.getTCProperty("done").setLogicalValue(true);
					selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
					TCComponentSignoff signoffObj = selectSignoffTeam.getValidSignoffs()[0];
					//[SR150605-007][20150605] shcho, Reject후 재 상신시 상신 오류  The Task "Creator" has not yet completed. 발생 하는 문제 해결
					//[2024.01.23]수정
					//TCCRDecision.APPROVE_DECISION -> signoffObj.getApproveDecision()
					//getCurrentDesktop() -> this.parent
					SignoffDecisionOperation decisionOp = 
							new SignoffDecisionOperation(session, this.parent,  subTask.getSubtask("perform-signoffs"), signoffObj, signoffObj.getApproveDecision(), "Request Approval");
					decisionOp.executeOperation();
				}
/*
        		if(taskReviewers.size() > 0){
        			
        			if(!"Creator".equals(reviewTaskName)) {
        				
        				if(!userId.equals(tccomponentsignoff.getGroupMember().getUserId())) {
        					TCComponent[] paramArrayOfTCComponent = null;
//        					selectSignoffTeam.getTCProperty("done").setLogicalValue(false);
        					selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
        					selectSignoffTeam.removeAttachments(new TCComponent[]{tccomponentsignoff});
        					
//        					selectSignoffTeam.changeOwner(tccomponentsignoff.getGroupMember().getUser(), tccomponentsignoff.getG);
//        					selectSignoffTeam.initialize(arg0, arg1)
//        					selectSignoffTeam.rep
//        					selectSignoffTeam.save();

        					selectSignoffTeam.addAttachments(TCAttachmentScope.LOCAL, sifnoffList, attachTypeList);
//        					selectSignoffTeam.getTCProperty("done").setLogicalValue(true);
        					selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
        					selectSignoffTeam.save();
        					selectSignoffTeam.performAction(TCComponentTask.ASSIGN_APPROVER_ACTION, "");
        				}

        			}


        		}else{
//        			selectSignoffTeam.getTCProperty("done").setLogicalValue(true);
        			selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
        			selectSignoffTeam.save();
        		}
*/
        	} catch(Exception e){
        		throw e;
        	} finally {
        		if(selectSignoffTeam != null)
        			selectSignoffTeam.unlock();
        	}
        }
	}	
	
	
	private void checkBOPValidate() throws Exception {
		
		String org_code = changeRevision.getProperty(SDVPropertyConstant.MECO_ORG_CODE);
		BOPTYPE bopType = getBopType(org_code);
		
		//-----------------------------

        // Process Type(조립,도장,차체 유무)를 가져옴
//        if (itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
//            String processType = targetItemRevision.getProperty(SDVPropertyConstant.LINE_REV_PROCESS_TYPE);
//            if (processType.isEmpty()) {
//                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("NotExistProcessType.MSG"), "Warning", MessageBox.WARNING);
//                isValidOK = false;
//                return;
//            }
//            bopType = getBopType(processType);
//        } else
//            bopType = getBopType(itemType);
		//------------------------------
        ValidateManager vldMgr = new ValidateManager(progress);
        TCComponentBOMWindow bomWindow = null;
        boolean isValid = false;
        //for(TCComponent tcComponent : solutionList) {
        for (int i = 0;solutionList!=null && i < solutionList.length; i++) {
        	
        	TCComponent tcComponent = solutionList[i];
        	
        	System.out.println( "["+i+"/"+solutionList.length+"] ("+df.format(new Date())+") : "+tcComponent.toString() );
        	
            //대상 tcComponent의 parent가 solutionList에 있을경우 대상 tcComponent는 SKIP 한다. (parent가 validate할때 하위도 함께 되므로)
            if(checkSolutionList(tcComponent)) {
                continue;
            }
                    	
        	bomWindow  = SDVBOPUtilities.getBOPWindow((TCComponentItemRevision)tcComponent, "Latest Working For ME", "bom_view");
            isValid = vldMgr.executeValidation(bopType, bomWindow.getTopBOMLine());
            bomWindow.close();
            if(!isValid) {
            	throw (new Exception("Please check the result of BOP Validation."+"\n"));
            }
            
        }
  
	}
	
	/**
	 *  Parent를 찾아 solutionList에 존재하는지 체크하는 함수
	 * @param childComponent
	 * @return
	 * @throws Exception
	 */
    private boolean checkSolutionList(TCComponent childComponent) throws Exception {        
        TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(CustomUtil.getTCSession(), "Latest Working For ME");
        TCComponent[] imanComps = ((TCComponentItemRevision)childComponent).whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
        
        if(imanComps!= null) {
            for(TCComponent parentComponent : imanComps) {  
                String parentCompType =  ((TCComponentItemRevision)parentComponent).getType();
                String parentItemID = ((TCComponentItemRevision)parentComponent).getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                //Parent가 Shop인경우 는 제외 (Shop은 Validate하지 않는다. 때문에 SolutionList에 존재하더라도 의미 없음.)
                if(parentCompType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV)) {
                    return false;
                }
                
                for (TCComponent solutionComponent : solutionList) {
                    String solutionItemID = ((TCComponentItemRevision)solutionComponent).getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                    if(solutionItemID.equals(parentItemID)) {
                        return true;
                    }
                }
            }        
        }

        return false;
    }
    
    /**
     * [NON-SR][20160822] taeku.jeong EPL 생성을 Operation단위로 생성 되도록하고 상신과정에 EPL Reload (Shop, Line, Station) 자동수행으로
     * 변경되었으므로 Publish 만 Check 하도록 하면 될것으로 판단됨.
     * 1) EPL Load는 Preview를 실시하면 자동으로 EPL Load됨 (W/F 생성 및 Release 전)
     * 2) 
     * @throws Exception
     */
	private void checkPublishedProcessSheet() throws Exception {
		
		// [NON-SR][20160520] taeku.jeong MECO 상신후 검토과정에 작업표준서에서 변경기호가 누락된 것이 발견되어 Validation 조건을 수정함.
		//                              EPL 생성이 Publish 시점보다 늦는 경우가 원인이 되었음.
		//                              소스코드 수정하는 과정에 소스코드 정리도 같이 수행했음.
    	String neededPublishList = "";
    	String neededPublishAfterEPLLoad = "";
    	String returnMessage = "";
    	
		// Publish Date를 Hash Table에 담아 Publish 유무와 Publish Date 확인에 사용한다.
		Hashtable<String, Date> publishedDateHash = new Hashtable<String, Date>();
		TCComponent[] process_sheet_list = changeRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
		for (int i = 0; i < process_sheet_list.length; i++) {
			String koreamInstructionSheetName = process_sheet_list[i].getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			String operationId = koreamInstructionSheetName.substring(4);
			Date publishedDate = process_sheet_list[i].getDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE);
			
			if(operationId!=null && operationId.trim().length()>0 && publishedDate!=null){
				publishedDateHash.put(operationId.trim(), publishedDate);
			}
		}
		
		String mecoId = null;
		MECOCreationUtil aMECOCreationUtil = null;
		if(this.changeRevision!=null){
			mecoId = changeRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			aMECOCreationUtil = new MECOCreationUtil(this.changeRevision);
		}

		// [SR150106-027][20150107] ymjang, MECO 검증 로직 보완(작표 유무) --> 작업 표준서가 Publishing 되지 않은 경우, MECO 결재 요청 불가
		for (int i = 0;solutionList!=null && i <solutionList.length; i++) {
    		
    		TCComponent solutionComponent = solutionList[i];
    		TCComponentItemRevision solutionItemRevision = (TCComponentItemRevision)solutionList[i];
    		
    		if(solutionComponent==null){
    			continue;
    		}
    		
    		// Operation Item Revision에 대해서만 Publish 상태를 확인한다.
			if(solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV)  
					|| solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV) 
					|| solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV) ) {
				
				
				boolean valideMepl = false;
				
				TCComponentItemRevision baseOnRevision = null;
				Date lastModifyDate = null;
				Date eplCreationDate = null;
				String currentRevId = null;
				
				String operationItemId = null;
				try {
					operationItemId = solutionItemRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				} catch (TCException e1) {
					e1.printStackTrace();
				}
				
				eplCreationDate = aMECOCreationUtil.getOperationEPLCreateionDate(mecoId, operationItemId);
				int diffCount = -1;
				if(eplCreationDate==null){
					diffCount = aMECOCreationUtil.getOperationDiffCount(changeRevision, solutionItemRevision);
				}
				
				try {
					lastModifyDate = solutionItemRevision.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
				} catch (TCException e) {
					e.printStackTrace();
				}
				
				String targetViewType = "View";
				Date bomViewLastModifyDate = null;
				try {
					TCComponentBOMViewRevision bomviewRevision = SDVBOPUtilities.getBOMViewRevision(solutionItemRevision, targetViewType);
					if(bomviewRevision != null) {					
						bomViewLastModifyDate = bomviewRevision.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Date instructionPublishDate = publishedDateHash.get(operationItemId);

				// [NON-SR][20160824] taeku.jeong Operation 단위로 MECO EPL 생성처리 후 발견된 Validation문제 해결
				// Operation을 개정 했는지만 Child Node 변경이 없는경우에 대한 처리 추가
				if(diffCount<1){
					if(instructionPublishDate!=null){
						continue;
					}else{
						neededPublishList = neededPublishList + solutionComponent +"\n";
						continue;
					}
				}
				
				if(eplCreationDate != null && lastModifyDate != null && bomViewLastModifyDate != null){
					if(lastModifyDate.before(eplCreationDate)==true && bomViewLastModifyDate.before(eplCreationDate)==true){
						valideMepl = true;
					}
				}

				boolean needRepublish = true;

				if( instructionPublishDate!=null && valideMepl==true ) {
					if(instructionPublishDate.after(eplCreationDate)){
						needRepublish = false;
					}
				}
				
		        if(needRepublish==true){
		        	neededPublishList = neededPublishList + solutionComponent +"\n";
		        }
		        if(valideMepl == false){
		        	neededPublishAfterEPLLoad = neededPublishAfterEPLLoad + solutionComponent +"\n";
		        }

			}
			
    	}
    	
    	if(neededPublishList!=null && neededPublishList.trim().length()>0 ){
    		returnMessage = returnMessage + "\n You did not re-publish process sheet after changing BOP. \n Check to publish below process sheet. : \n"+neededPublishList;
    	}
    	if(neededPublishAfterEPLLoad!=null && neededPublishAfterEPLLoad.trim().length()>0 ){
    		returnMessage = returnMessage + "\n You did not \"EPL Load\" after changing BOP. \n Publish after \"EPL Load\" below process sheet. : \n"+neededPublishAfterEPLLoad;
    	}
    	
    	if(returnMessage!=null && returnMessage.trim().length()>0 ){
    		msg = returnMessage;
    		displayMessage(msg, true);
    		isOkValidation = false;
    	}
    	
    	if(returnMessage!=null && returnMessage.trim().length()>0 ){
    		throw new Exception("Check publish !!");
    	}
		
	}
    
	/**
	 * [NON-SR][20160822] taeku.jeong Operation단위로 MECO EPL 생성, 상신과정에 Shop, Line, Station MECO EPL 자동생성
	 * 예전에 사용하던 기능으로 더이상 사용하지 않음.
	 * 	 * @throws Exception
	 */
	private void checkPublishedProcessSheet_OLD20160822() throws Exception {
		
		// [NON-SR][20160520] taeku.jeong MECO 상신후 검토과정에 작업표준서에서 변경기호가 누락된 것이 발견되어 Validation 조건을 수정함.
		//                              EPL 생성이 Publish 시점보다 늦는 경우가 원인이 되었음.
		//                              소스코드 수정하는 과정에 소스코드 정리도 같이 수행했음.
    	String neededPublishList = "";
    	String neededPublishAfterEPLLoad = "";
    	String returnMessage = "";
    	
    	Date lastEPLLoadDate = null;
		if(changeRevision!=null){
			String mecoId = changeRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			if(mecoId!=null){
				lastEPLLoadDate = ProcessSheetDataHelper.getLastEPLLoadDate(mecoId);
			}
		}
		
		System.out.println("lastEPLLoadDate = "+df.format(lastEPLLoadDate));
		
		if(lastEPLLoadDate==null){
			returnMessage = returnMessage + "\n You must generat a \"MECO_EPL\".";
		}
		
		// Publish Date를 Hash Table에 담아 Publish 유무와 Publish Date 확인에 사용한다.
		Hashtable<String, Date> publishedDateHash = new Hashtable<String, Date>();
		TCComponent[] process_sheet_list = changeRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
		for (int i = 0; i < process_sheet_list.length; i++) {
			String koreamInstructionSheetName = process_sheet_list[i].getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			String operationId = koreamInstructionSheetName.substring(4);
			Date publishedDate = process_sheet_list[i].getDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE);
			
			if(operationId!=null && operationId.trim().length()>0 && publishedDate!=null){
				publishedDateHash.put(operationId.trim(), publishedDate);
			}
		}

		// [SR150106-027][20150107] ymjang, MECO 검증 로직 보완(작표 유무) --> 작업 표준서가 Publishing 되지 않은 경우, MECO 결재 요청 불가
		for (int i = 0;solutionList!=null && i <solutionList.length; i++) {
    		
    		TCComponent solutionComponent = solutionList[i];
    		
    		if(solutionComponent==null){
    			continue;
    		}
    		
    		// Operation Item Revision에 대해서만 Publish 상태를 확인한다.
			if(solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV)  
					|| solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV) 
					|| solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV) ) {

				// [NON-SR][20160621] taeku.jeong 상신시 EPL 및 Publish 순서 검증 추가 수정
				// 아래 부분 중 조건 검증 하는 부분 모두 변경되었음.
				
		    	Date revisionLastModifyDate = null;
		    	Date instructionImageLastModifyDate = null;
		    	Date bomViewLastModifyDate = null;
		        Date instructionPublishDate = null;
		        
		        boolean needEPLReload = false;
		        boolean needRepublish = false;

				String operationItemId = solutionComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				
				revisionLastModifyDate = solutionComponent.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
				
				AIFComponentContext[] korRelationListContext = solutionComponent.getRelated(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
				if(korRelationListContext!=null){
					TCComponentDataset processsheetDataset = (TCComponentDataset)korRelationListContext[0].getComponent();
					instructionImageLastModifyDate = processsheetDataset.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
				}
				
				String targetViewType = "View";
				TCComponentBOMViewRevision bomviewRevision = SDVBOPUtilities.getBOMViewRevision((TCComponentItemRevision)solutionComponent, targetViewType);
				if(bomviewRevision != null) {					
					bomViewLastModifyDate = bomviewRevision.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
				}
				
				instructionPublishDate = publishedDateHash.get(operationItemId);
				
				System.out.println("instructionImageLastModifyDate = "+df.format(instructionImageLastModifyDate)+" [ "+operationItemId+"]");
				System.out.println("instructionPublishDate = "+df.format(instructionPublishDate)+" [ "+operationItemId+"]");
				
				// EPL Load 조건
		        if(lastEPLLoadDate.after(revisionLastModifyDate) &&
		        		lastEPLLoadDate.after(instructionImageLastModifyDate) &&
		        		lastEPLLoadDate.after(bomViewLastModifyDate) ){
		        	
		        	// Publish 조건
		        	if(instructionPublishDate.after(lastEPLLoadDate)){
		        		// 검증결과 EPL Load, Publish 순서에 문제 없음.
		        		 ;
		        		 System.out.println("OK---- (Case1) : "+operationItemId);
		        	}else{
		        		// Publisth를 다시 해야 합니다.
		        		needRepublish = true;
		        		System.out.println("Error---- (Case2) : "+operationItemId);
		        	}
		        }else{
		        	// EPL Load 해야 합니다.
		        	needEPLReload = true;
		        	System.out.println("Error---- (Case3) : "+operationItemId);
		        }

		        if(needRepublish==true){
		        	neededPublishList = neededPublishList + solutionComponent +"\n";
		        }
		        if(needEPLReload == true){
		        	neededPublishAfterEPLLoad = neededPublishAfterEPLLoad + solutionComponent +"\n";
		        }

			}
			
    	}
    	
    	if(neededPublishList!=null && neededPublishList.trim().length()>0 ){
    		returnMessage = returnMessage + "\n You did not re-publish process sheet after changing BOP. \n Check to publish below process sheet. : \n"+neededPublishList;
    	}
    	if(neededPublishAfterEPLLoad!=null && neededPublishAfterEPLLoad.trim().length()>0 ){
    		returnMessage = returnMessage + "\n You did not \"EPL Load\" after changing BOP. \n Publish after \"EPL Load\" below process sheet. : \n"+neededPublishAfterEPLLoad;
    	}
    	
    	if(returnMessage!=null && returnMessage.trim().length()>0 ){
    		msg = returnMessage;
    		displayMessage(msg, true);
    		isOkValidation = false;
    	}
    	
    	if(returnMessage!=null && returnMessage.trim().length()>0 ){
    		throw new Exception("Check publish !!");
    	}
		
	}
	
	@SuppressWarnings("unchecked")
    private void checkExistProcessSheet() throws TCException {
    	String noWhereUsedList = "";
    	String returnMessage = "";
    	
		HashMap<String, TCComponent> solutionItemRevisionMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> processItemRevisionMap = new HashMap<String, TCComponent>();
		
		ArrayList<TCComponentItemRevision> processRevisionList = new ArrayList<TCComponentItemRevision>();
		for(TCComponent solutionComponent : solutionList) {
			
			if(solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV)  
					|| solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV) 
					|| solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV) ) {
				
				solutionItemRevisionMap.put("KPS-"+solutionComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID), solutionComponent);
			}
		}
		
		TCComponent[] process_sheet_list = changeRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
		
		Object[] missingProcessSheet_Ids = {};
		if(process_sheet_list.length > 0){
			for(TCComponent process_sheet : process_sheet_list) {
				
				if(!processItemRevisionMap.containsKey(process_sheet.getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
					processItemRevisionMap.put(process_sheet.getProperty(SDVPropertyConstant.ITEM_ITEM_ID), process_sheet);	
				}
			}
		}
		
		missingProcessSheet_Ids = CollectionUtils.subtract(solutionItemRevisionMap.keySet(), processItemRevisionMap.keySet()).toArray(new Object[0]);
		
		for(Object str : missingProcessSheet_Ids) {
			noWhereUsedList = noWhereUsedList + str.toString() +"\n";
		}
		
    	if(!noWhereUsedList.equals("")){
    		returnMessage = returnMessage + "\n The process sheet(s) must attach on the meco. \n Check to publish below process sheet. : \n"+noWhereUsedList;
    	}
    	
    	if(!"".equals(returnMessage)) {
    		
    		msg = returnMessage;
    		displayMessage(msg, true);
    		isOkValidation = false;
    	}
    	
//        TCComponentItem publishItem = SDVBOPUtilities.FindItem(publishItemPrefix + itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);
//        if(publishItem != null) {
//            TCComponentItemRevision publishItemRev = null;
//            TCComponent[] publishItemRevs = publishItem.getRelatedComponents("revision_list");
//            List<String> publishRevList = new ArrayList<String>();
//            for(int i = 0; i < publishItemRevs.length; i++) {
//                String tempRev = publishItemRevs[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
//                if((tempRev.substring(0, itemRev.length())).compareToIgnoreCase(itemRev) <= 0) {
//                    publishRevList.add(tempRev);
//                    publishItemRev = (TCComponentItemRevision) publishItemRevs[i];
//                }
//            }
	}
    private void displayMessage(String msg, boolean nextLine) {

        if (msg == null)
            return;
        progress.setStatus(msg, nextLine);
    }
    
	public BOPTYPE getBopType(String org_code) {
		
		if(SDVPropertyConstant.MECO_ORG_CODE_PA.equals(org_code)){
			return BOPTYPE.ASSEMBLY;
		}else if(SDVPropertyConstant.MECO_ORG_CODE_PB.equals(org_code)){
			return BOPTYPE.BODY;
		}else if(SDVPropertyConstant.MECO_ORG_CODE_PP.equals(org_code)) {
			return BOPTYPE.PAINT;
		}
		return null;
	}
	
	/**
	 * 메일 발송
	 * Vision-Net의 CALS 프로시져 호출
	 * @throws Exception
	 */
	private void sendMail() throws Exception{
		String project = changeRevision.getProperty(SDVPropertyConstant.MECO_PROJECT);
		String changeDesc = changeRevision.getProperty("object_desc");
		
        String fromUser = session.getUser().getUserId();
        String title = "New PLM : MECO[" + mecoNo + "] 결재 요청";
        
		String body = "<PRE>";
		body += "New PLM에서 아래와 같이 결재 요청 되었으니 확인 후 결재 바랍니다." + "<BR>";
		body += " -MECO NO. : " + mecoNo + "<BR>";
		body += " -Project : " + project + "<BR>";
		body += " -Change Desc. : " + changeDesc + "<BR>";
		body += " -요청부서 : " + changeRevision.getTCProperty("owning_group") + "<BR>";
		body += " -요청자  : " + changeRevision.getTCProperty("owning_user") + "<BR>";
		body += "</PRE>";
		
		// SR150604-024
		// taeku.jeong MECO 결재 요청 Vision mail 공지 오류 수정건.
		// 기존의 코드를 Test 해 본 결과 배열의 첫번째가 항상 첫번째 Review Task라고 간주 할 수 없음을 확인.
		// Template의 W/F흐름에 따라 검색되는 첫번째 Review Task를 Return 하는 함수를 새로 작성함.
		
		//ArrayList<String> taskList  = CustomUtil.getWorkflowTask(changeRevision.getProperty(SDVPropertyConstant.MECO_WORKFLOW_TYPE), session);
		//ArrayList<TCComponentGroupMember> receivedUserList = reviewers.get(taskList.get(0)); //1st task.
		
		String fristReviewTaskName = CustomUtil.getFirstEPMReviewTaskName((TCSession)session,
				changeRevision.getProperty(SDVPropertyConstant.MECO_WORKFLOW_TYPE));
		ArrayList<TCComponentGroupMember> receivedUserList = reviewers.get(fristReviewTaskName);
		
		// End of SR150604-024 
		
		String toUsers = "";		
		for(TCComponentGroupMember member : receivedUserList){
				if(toUsers.equals("")){
					toUsers = member.getUser().getUserId();
				}else{
					toUsers += SYMCECConstant.SEPERATOR + member.getUser().getUserId();
				}
		}
		
//        dao.sendMail(fromUser, title, body, toUsers);
	}
}
