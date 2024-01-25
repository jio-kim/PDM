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

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.workflow.SYMCDecisionDialog;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.utils.PreferenceService;
import com.ssangyong.common.utils.ProcessUtil;
import com.ssangyong.dto.ApprovalLineData;
import com.ssangyong.rac.kernel.SYMCBOPEditData;
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
 * ���� ECO ��� �� ECO ���μ��� ���� �� �ݷ� �� �� ��� �� ȣ�� ��.
 * [SR140820-050][20140808] shcho, MEW�� Team Leader ���縸 �ϹǷ� Validate���� BOPADMIN�� �ʼ��� ã�� �� ����(Ȯ����:�����(������))
 * [SR150605-007][20150605] shcho, Reject�� �� ��Ž� ��� ����  The Task "Creator" has not yet completed. �߻� �ϴ� ���� �ذ�
                                         (Creator Task�� ������ ���� perform-signoffs Task�� �������� ����)
 * [SR150715-017][20150717] shcho, ��ü MECO ��Ž� �����ð� ���� �ҿ� ������ Checking MECO EPL�� ����. 
 *                                       �� ��ſ�, ��� Process ���� �� �����ϵ��� ����. (������ ������ ������ ����� ���� �����.)
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
	
	private final static String EVENT_START = "  ��";
	private DataSet ds = null;
	
	private boolean isOkValidation = true;
	private boolean updateSignOffs = false;
	private String msg = "";
	private TCComponentTask rootTask;
	
	// ���� ���� ���
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

			// # 0. FIXED, 2013.06.01, Ÿ�ٸ���Ʈ���� ECORevision�� ������ ��� ã�Ƽ� �ٿ� �ְ�, CreateWorkflow�� ���� �޽�¡
			progress.setStatus(EVENT_START + "Checking MECO has Workflow...", false);
			System.out.println("1.checkHasWorkflow");
			checkHasWorkflow();
			progress.setStatus("is done!");

			// # 3. ���缱 Ȯ��
			progress.setStatus(EVENT_START + "Checking approval line...", false);
			System.out.println("2.checkReviewer");
			checkReviewer();
			progress.setStatus("is done!");

			// # 5. ECO �۾� ����[C��]�� ���� �ַ�Ǿ����� ��ũ ����
			//���� ����... ����������, �ַ�Ǿ������� ��� �ٽ� ã�Ƴ��� MECO ������ ���̰ų� ����....
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
				//�ַ�� �������� �ٽ� �������� ����.... false ���� ����.
				//���ʿ��� �̹� ���� �ַ�� ������ �� ���� �������� ���鵵�� ����.
				ArrayList<SYMCBOPEditData> arrResultEPL = customUtil.buildMEPL(changeRevision, false);
			}
			progress.setStatus("is done!");

			progress.setStatus(EVENT_START + "Checking Re-Publish Process Sheet(s)...", false);
			System.out.println("5.checkPublishedProcessSheet");
			checkPublishedProcessSheet();
			progress.setStatus("is done!");

			// # 6. Solution Items�� ���� �� ������� ������ ������ �ٸ� MECO�� ���� ���� ���� �ִ��� üũ
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

			// ## ���μ��� Ÿ�� ����
			progress.setStatus(EVENT_START + "Checking targets...", false);
			System.out.println("9.getTargets");
			getTargets();
			progress.setStatus("is done!");

			// ## ���� ����
			progress.setStatus(EVENT_START + "Change Status...", false);
			System.out.println("10.changeStatus");
			changeStatus();
			progress.setStatus("is done!");

			// ## ���μ��� ����
			progress.setStatus(EVENT_START + "Creating process...", false);
			System.out.println("11.createProcess");
			createProcess();
			progress.setStatus("is done!");

			// ## Ÿ��ũ �Ҵ�
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

			// ## ���� �߼� 
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
				progress.setStatus("�� Error Message : ");
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
	
	// Ÿ�ٸ���Ʈ���� MECORevision�� ������ ��� ã�Ƽ� �ٿ� �ְ�, CreateWorkflow�� ���� �޽�¡
	private void checkHasWorkflow() throws Exception {

		TCComponent[] process_stage_list = changeRevision.getReferenceListProperty(SDVPropertyConstant.PROP_PROCESS_STAGE_LIST);
		
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
		
		// Workflow �˻�
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
				box.setMessage("���� ��û�� �Ϸ�Ǿ����ϴ�");
				box.open();
			}

		});
	}
	
	/**
	 * ���� �߻� �� ��� ���� �ʱ�ȭ
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
	 * ECO Affected Project ���� ����
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
	 * ���� ����
	 * IitemRevision�� Maturity��
	 * EcoRevision�� Eco Maturity�� ������Ʈ ��.
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
	 * MECO EPL üũ
	 * @throws Exception 
	 */
	private void checkExistMEPL() throws Exception{
//		ds = new DataSet();
//		ds.put("mecoNo", mecoNo);
//		boolean resultList = dao.checkExistMEPL(ds);
		
		//viewer â���� ��ư Ŭ�� �� �����ϴ� ���� �������� �Űܼ� �����ϰ� ��.
		String org_code = changeRevision.getProperty(SDVPropertyConstant.MECO_ORG_CODE);
		BOPTYPE bopType = getBopType(org_code);
		if (bopType.equals(BOPTYPE.ASSEMBLY) || bopType.equals(BOPTYPE.BODY) || bopType.equals(BOPTYPE.PAINT))
		{
			CustomUtil customUtil = new CustomUtil();
			//�ַ�� �������� �ٽ� �������� ����.... false ���� ����.
			//���ʿ��� �̹� ���� �ַ�� ������ �� ���� �������� ���鵵�� ����.
			ArrayList<SYMCBOPEditData> arrResultEPL = customUtil.buildMEPL(changeRevision, false);
		}

    	Vector<String> notPublishedV = new Vector<String>();
    	// [NON-SR][20160829] taeku.jeong ������ �������� ������ �ִµ�
    	// Operation�� �׸��� ����� ��� �߰����� Ȯ���� �ʿ��ϴ�.
    	// EPL ������ �ʿ��ѵ� ������ ���� ������ �˷� ��� �Ѵ�.
    	int missCount = 0;
		String mecoId = null;
		MECOCreationUtil aMECOCreationUtil = null;
		if(changeRevision!=null){
			
			mecoId = changeRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			aMECOCreationUtil = new MECOCreationUtil(changeRevision);
			//MEPL ���̺� ���� �ַ�ǿ� �����ϴ� ���������� parent�� ������� ���� �͵��� ã�ƿ´�.
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
						//mepl�� parent�� ������� ���� �������ε� ������ ����� �������� �����ϸ� ���� �̻���.
						//������ ����� �������� ���� �� MECO�� �ƴϸ� �����ϰ� ���� �� MECO�� �ش��ϴ°Ÿ� ������ ����.
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
	 * BOM Structure �󿡼� end item �ؿ� end item�� �����ϸ� �ʵȴ�
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
	 * ���缱�� ���ø��� �°� ���� �Ǿ� �ִ��� Ȯ��
	 * @return
	 * @throws Exception
	 */
    private void checkReviewer() throws Exception {
		ApprovalLineData theLine = new ApprovalLineData();
		theLine.setEco_no(changeRevision.getProperty("item_id"));
		
		// ���� �ÿ����� ���缱 üũ ����
		ArrayList<ApprovalLineData> paramList = null;
		if(parent == null){
			//���缱 ���� ����
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
		//Ÿ��ũ�� TCComponentGroupMember���� ���� �� �ʼ� ���� ���缱 Ȯ��
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
			
			// FIXED 2013.05.14, DJKIM, �ڼ��� CJ: ������� ���� ������ �߻� �Ҽ� �����Ƿ� ����� ���� Ȯ���Ͽ� �������� ����ڰ� ���缱�� �Ҵ� ���� �ʵ��� ��.
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
		
		// FIXED ���缱�� �����ȹ[BOPADMIN], �����[TEAM_LEADER]�� �ϳ� �̻����� üũ ��.
		PreferenceService.createService(session);
		String checkRole = PreferenceService.getValue("SYMC_MECO_WF_CHECK_ROLE"); // COST_ENGINEER,BOMADMIN,TEAM_LEADER
		if(checkRole.equals("")){
			checkRole = "BOPADMIN,TEAM_LEADER";
		}
		
		// [SR140820-050][20140808] shcho, MEW�� Team Leader ���縸 �ϹǷ� Validate���� BOPADMIN�� �ʼ��� ã�� �� ����(Ȯ����:�����(������))
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
     * �ű� ��Ʈ�� EPL�� Category�� DR1/2�� ������ ��Ʈ�� ���� �ϸ� �������� �ʼ��� �����Ǿ�� ��.
     * 2013.01.10
     * REQ. �۴뿵
     * REF. ������
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
    	     //[SR��ȣ����][20140929] shcho, ����ȭ ã�ƴԲ� BOP_PROCESS_SHOP_ITEM_REV ���� ���� Ȯ���ϱ�
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
    	
    	// EMCO ��� ������ Parent Node�� MECO No�� Ȯ�� �ϴ� �κ���
    	// ���⼭ Validation �Ҷ� Parent Node���� �߰� �ϰų� ������ Station, Operation�� �ƴѰ��
    	// Parent Node�� MECO No�� Child Node�� Station �Ǵ� Operation�� MECO No�� ���� ���� �ʾƵ� �ǵ��� ����� �Ѵ�.
    	// Validation �ð��� ���� �ɸ��� �����鼭 �ش� Validation�� �� �� �ֵ��� ����� �����ؾ� �Ѵ�.
    	
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
        					

        					// [NON-SR][20160219] taeku.jeong Parent MECO No Check ���� �߰� (MECO �и� ������ ����)
        					// ���⼭ Parent Revision�� Child Node�� solutionItemrevision�� �߰� �ǰų� ���ŵ� Item Revision���� Ȯ���� �ʿ��ϴ�.
        					// Parent Item Revision�� Working ���ΰ�� MECO No Ȯ�� �����
        					// 1. Parent Item Revision�� 000�ΰ�� -> ��� �߰��Ǵ� Item��.
        					// 2. Parent Item Revision�� 000�� �ƴѰ�� -> ���� Item�� �߰� �ǰų� ���ŵ� Item���� Ȯ���ʿ���.
        					//     a. BaseOnRevision�� �������� �ʴ� Item�� ��� �߰��� Item�̹Ƿ� Parent Node�� MECO�� ���ƾ���.
        					boolean isParentReleased = SYMTcUtil.isReleased(aWhereUsedResultItemRevision);
        					boolean isParentNodeMecoNoCompTarget = false;
        					TCComponentItemRevision baseOnItemRevision = null;
        					String tempMessage = null;
        					if(isParentReleased==false){
        						
        						// Parent Node�� Released Status�� ���� ���� �ű��� ���� ���� Revision�� ���� ���� �߰�(����)�� ��쿡 ���ؼ���
        						// Child Node�� Parent Node�� MECO No�� ���� �ؾ� �Ѵ�.
        						
        						baseOnItemRevision = aWhereUsedResultItemRevision.basedOn();
            					if(baseOnItemRevision!=null){
            						// Working ���� Revision�̹Ƿ� �߰��� Child �ΰ�� Child Node�� Parent Node�� MECO�� �����ؾ� �Ѵ�.
            						String currentParentItemId = aWhereUsedResultItemRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            						String baseOnItemItemId = baseOnItemRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            						
            						if(currentParentItemId!=null && baseOnItemItemId!=null && baseOnItemItemId.trim().equalsIgnoreCase(currentParentItemId.trim())==true){
            							// Item�� Version Up �� ��� �̹Ƿ� Parent Node�� Old�� New �߿� ������ Item�� �߰� �Ǵ� ���ŵ� ��Ȳ���� Ȯ�� �ؾ� �Ѵ�.
            							// ���࿡ currentSolutionItemrevision�� Parent Node�� aWhereUsedResultItemRevision��  
            							String newRevId = aWhereUsedResultItemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            							String oldRevId = baseOnItemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            							Vector<String> changedNewItemIdV = aCustomMECODao.getChangedNewItemIdList(currentParentItemId, newRevId, oldRevId);
            							if(changedNewItemIdV!=null && changedNewItemIdV.contains(currentSolutionItemId)==true){
            								// Parent Node Meco Id�� �����ؾ� �Ѵ�.
            								isParentNodeMecoNoCompTarget = true;
            								tempMessage = "Child Node Added";
            							}
            						}
            					}else{
            						// ������ Revision�̹Ƿ� Child Node�� Parent Node�� MECO�� �����ؾ� �Ѵ�.
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
										// �̰� ��� ó�� �ؾ� ����....
									}
								}
    						}
    						
    						if(isDifferentMeco==true){
    							unMatchMecoList = unMatchMecoList + "MECO Number Mismatch : "+aWhereUsedResultItemRevision +"("+parentNodeMECONo+")  <-> "+currentSolutionItemrevision+"("+childNodeMECONo+") Target MECO : "+targetMECONo+ "\n";
    							//System.out.println("���� �ɸ��ǵ�.....\n"+unMatchMecoList);
    						}

							// ������ ���� �ڵ�
    						// [NON-SR][20160222] taeku.jeong MECO ��� ������ Parent Node�� Child Node�� MECO No �������� �����ϴ� ���� ����
							//if(null !=mecoRevision) {
							//	// Parent Node�� Child Node�� MECO�� �ٸ� ��츦 Ȯ����.
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
    	
    	// ���ŵ� Item�߿����� Parent Node�� MECO No
    	// �̰��� �ʿ������ ����.
    	// Child Node�� ���ŵǴ� ������ ��� Parent Node�� Solution Items�� ���Եǰ�
    	// Parent Node MECO�� ���ԵǾ� ���� ���̹Ƿ� �ߺ� Check �ϰԵǴ� ����� �ȴ�.
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
				existWorkingChildrenList = existWorkingChildrenList + "���� ��� MECO Solution Item : " + solutioncomponent + ", " + "���� ��� MECO : " + errorHashmap.get("MECO_ID") + ", " + "�� ���� Item : " + errorHashmap.get("PITEM_ID") + "/" + errorHashmap.get("PITEM_REVISION_ID") + "-" + errorHashmap.get("POBJECT_NAME") + "\n";
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
			
			//TCTaskState.COMPLETED �� ��
			if(state!=null && state.getIntValue()==TCTaskState.COMPLETED.getIntValue() ){
				// Complete ��.
				isWorkflowComplete = true;
			}
		}
		
		return isWorkflowComplete;
	}
	
	
    /**
     * ���� Ÿ�� Ȯ��
     * check out ���ε� ���� Ȯ��
     * dataset �Ӽ��� eco no�� �Է�
     * @throws TCException
     */
    private void getTargets() throws TCException, Exception{
    	
    	TCComponentProcess process = null;
    	TCComponentTask rootTask = null;
    	
    	if(parent != null){
    		process = changeRevision.getCurrentJob();
    		rootTask = process.getRootTask();

    		// �ߺ� ���� ����
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
//        			// FIXED, 2013.06.01, DJKIM ���� ������ �ִ��� ������ üũ
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
    	
    	// FIXED, 2013.06.01, DJKIM ���� ������ �ִ��� ������ üũ
    	if(!noChildrenBVRList.equals("")){
    		retrunMessage = retrunMessage + "\nThe item that does not have a sub-structure exists.\nCheck BOMViewResion of below items.\n"+noChildrenBVRList;
    	}
    	
    	if(!retrunMessage.equals("")){
			msg = retrunMessage;
			displayMessage(msg, true);
			isOkValidation = false;
    	}

    	// �ݷ� �� ���  Ÿ�� �缳��
    	if(parent != null && rootTask != null){   		
    		// Ÿ�� �缳��
    		rootTask.add("root_target_attachments", targetList);
    	}
    }
    
    /**
     * ���μ��� ����
     * @throws Exception
     */
    private void createProcess() throws Exception{
    	
		String[] processProps = changeRevision.getProperties(new String[]{"date_released", "process_stage_list"});
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
     * ���缱 �Ҵ�
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
					//[SR150605-007][20150605] shcho, Reject�� �� ��Ž� ��� ����  The Task "Creator" has not yet completed. �߻� �ϴ� ���� �ذ�
					//Creator Task�� ������ ���� perform-signoffs Task�� �������� ����
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

        // Process Type(����,����,��ü ����)�� ������
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
        	
            //��� tcComponent�� parent�� solutionList�� ������� ��� tcComponent�� SKIP �Ѵ�. (parent�� validate�Ҷ� ������ �Բ� �ǹǷ�)
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
	 *  Parent�� ã�� solutionList�� �����ϴ��� üũ�ϴ� �Լ�
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
                //Parent�� Shop�ΰ�� �� ���� (Shop�� Validate���� �ʴ´�. ������ SolutionList�� �����ϴ��� �ǹ� ����.)
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
     * [NON-SR][20160822] taeku.jeong EPL ������ Operation������ ���� �ǵ����ϰ� ��Ű����� EPL Reload (Shop, Line, Station) �ڵ���������
     * ����Ǿ����Ƿ� Publish �� Check �ϵ��� �ϸ� �ɰ����� �Ǵܵ�.
     * 1) EPL Load�� Preview�� �ǽ��ϸ� �ڵ����� EPL Load�� (W/F ���� �� Release ��)
     * 2) 
     * @throws Exception
     */
	private void checkPublishedProcessSheet() throws Exception {
		
		// [NON-SR][20160520] taeku.jeong MECO ����� ��������� �۾�ǥ�ؼ����� �����ȣ�� ������ ���� �߰ߵǾ� Validation ������ ������.
		//                              EPL ������ Publish �������� �ʴ� ��찡 ������ �Ǿ���.
		//                              �ҽ��ڵ� �����ϴ� ������ �ҽ��ڵ� ������ ���� ��������.
    	String neededPublishList = "";
    	String neededPublishAfterEPLLoad = "";
    	String returnMessage = "";
    	
		// Publish Date�� Hash Table�� ��� Publish ������ Publish Date Ȯ�ο� ����Ѵ�.
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

		// [SR150106-027][20150107] ymjang, MECO ���� ���� ����(��ǥ ����) --> �۾� ǥ�ؼ��� Publishing ���� ���� ���, MECO ���� ��û �Ұ�
		for (int i = 0;solutionList!=null && i <solutionList.length; i++) {
    		
    		TCComponent solutionComponent = solutionList[i];
    		TCComponentItemRevision solutionItemRevision = (TCComponentItemRevision)solutionList[i];
    		
    		if(solutionComponent==null){
    			continue;
    		}
    		
    		// Operation Item Revision�� ���ؼ��� Publish ���¸� Ȯ���Ѵ�.
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

				// [NON-SR][20160824] taeku.jeong Operation ������ MECO EPL ����ó�� �� �߰ߵ� Validation���� �ذ�
				// Operation�� ���� �ߴ����� Child Node ������ ���°�쿡 ���� ó�� �߰�
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
	 * [NON-SR][20160822] taeku.jeong Operation������ MECO EPL ����, ��Ű����� Shop, Line, Station MECO EPL �ڵ�����
	 * ������ ����ϴ� ������� ���̻� ������� ����.
	 * 	 * @throws Exception
	 */
	private void checkPublishedProcessSheet_OLD20160822() throws Exception {
		
		// [NON-SR][20160520] taeku.jeong MECO ����� ��������� �۾�ǥ�ؼ����� �����ȣ�� ������ ���� �߰ߵǾ� Validation ������ ������.
		//                              EPL ������ Publish �������� �ʴ� ��찡 ������ �Ǿ���.
		//                              �ҽ��ڵ� �����ϴ� ������ �ҽ��ڵ� ������ ���� ��������.
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
		
		// Publish Date�� Hash Table�� ��� Publish ������ Publish Date Ȯ�ο� ����Ѵ�.
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

		// [SR150106-027][20150107] ymjang, MECO ���� ���� ����(��ǥ ����) --> �۾� ǥ�ؼ��� Publishing ���� ���� ���, MECO ���� ��û �Ұ�
		for (int i = 0;solutionList!=null && i <solutionList.length; i++) {
    		
    		TCComponent solutionComponent = solutionList[i];
    		
    		if(solutionComponent==null){
    			continue;
    		}
    		
    		// Operation Item Revision�� ���ؼ��� Publish ���¸� Ȯ���Ѵ�.
			if(solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV)  
					|| solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV) 
					|| solutionComponent.getType().equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV) ) {

				// [NON-SR][20160621] taeku.jeong ��Ž� EPL �� Publish ���� ���� �߰� ����
				// �Ʒ� �κ� �� ���� ���� �ϴ� �κ� ��� ����Ǿ���.
				
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
				
				// EPL Load ����
		        if(lastEPLLoadDate.after(revisionLastModifyDate) &&
		        		lastEPLLoadDate.after(instructionImageLastModifyDate) &&
		        		lastEPLLoadDate.after(bomViewLastModifyDate) ){
		        	
		        	// Publish ����
		        	if(instructionPublishDate.after(lastEPLLoadDate)){
		        		// ������� EPL Load, Publish ������ ���� ����.
		        		 ;
		        		 System.out.println("OK---- (Case1) : "+operationItemId);
		        	}else{
		        		// Publisth�� �ٽ� �ؾ� �մϴ�.
		        		needRepublish = true;
		        		System.out.println("Error---- (Case2) : "+operationItemId);
		        	}
		        }else{
		        	// EPL Load �ؾ� �մϴ�.
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
	 * ���� �߼�
	 * Vision-Net�� CALS ���ν��� ȣ��
	 * @throws Exception
	 */
	private void sendMail() throws Exception{
		String project = changeRevision.getProperty(SDVPropertyConstant.MECO_PROJECT);
		String changeDesc = changeRevision.getProperty("object_desc");
		
        String fromUser = session.getUser().getUserId();
        String title = "New PLM : MECO[" + mecoNo + "] ���� ��û";
        
		String body = "<PRE>";
		body += "New PLM���� �Ʒ��� ���� ���� ��û �Ǿ����� Ȯ�� �� ���� �ٶ��ϴ�." + "<BR>";
		body += " -MECO NO. : " + mecoNo + "<BR>";
		body += " -Project : " + project + "<BR>";
		body += " -Change Desc. : " + changeDesc + "<BR>";
		body += " -��û�μ� : " + changeRevision.getTCProperty("owning_group") + "<BR>";
		body += " -��û��  : " + changeRevision.getTCProperty("owning_user") + "<BR>";
		body += "</PRE>";
		
		// SR150604-024
		// taeku.jeong MECO ���� ��û Vision mail ���� ���� ������.
		// ������ �ڵ带 Test �� �� ��� �迭�� ù��°�� �׻� ù��° Review Task��� ���� �� �� ������ Ȯ��.
		// Template�� W/F�帧�� ���� �˻��Ǵ� ù��° Review Task�� Return �ϴ� �Լ��� ���� �ۼ���.
		
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
