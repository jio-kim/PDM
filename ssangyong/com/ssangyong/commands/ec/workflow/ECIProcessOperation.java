package com.ssangyong.commands.ec.workflow;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.utils.ProcessUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentSignoffType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCSignoffOriginType;
import com.teamcenter.rac.kernel.TCTaskState;

public class ECIProcessOperation extends AbstractAIFOperation{

	private TCSession session = (TCSession) getSession();
	private TCComponentItemRevision eciRevision;
	private WaitProgressBar progress;
	private String message;
	private TCComponentGroupMember[] reviewerComp = new TCComponentGroupMember[4]; // 4단결재
	private TCComponentProcess newProcess;
	private final static String EVENT_START = "  ▶";

	public ECIProcessOperation(TCComponentItemRevision eciRevision){
		this.eciRevision = eciRevision;
		
	}
	@Override
	public void executeOperation() throws Exception {
		try{
			progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
			progress.setWindowSize(500, 350);
			progress.start();						
			progress.setShowButton(true);
			progress.setStatus("ECI Workflow creation start.");
			
			// 결재선 확인
			progress.setStatus("");
			progress.setStatus(EVENT_START+"Checking approval line...", false);
			if(checkReviewer()) return;
			progress.setStatus("is done!");
			progress.setProgressValue(30);					
			progress.setProgressString("30%");
			
			// 상태 변경
			progress.setStatus(EVENT_START+"Change Status...", false);
			changeStatus();
			progress.setStatus("is done!");
			progress.setProgressValue(40);					
			progress.setProgressString("40%");
			
			// 프로세스 타겟 설정
			progress.setStatus(EVENT_START+"Creating process...", false);
			createProcess();
			progress.setStatus("is done!");
			progress.setProgressValue(60);					
			progress.setProgressString("60%");
			
			// 결재자 입력
			progress.setStatus(EVENT_START+"Assigning...", false);
			assignSignoffs();
			progress.setStatus("is done!");
			progress.setProgressValue(80);					
			progress.setProgressString("80%");
			
			//메일 발송
			progress.setStatus(EVENT_START+"Mailing...", false);
			sendMail();
			progress.setStatus("is done!");
			progress.setProgressValue(100);					
			progress.setProgressString("100%");

		}catch(Exception e){
			e.printStackTrace();
			progress.setStatus("is fail!");
			progress.setStatus("＠ Error Message : ");
			message = e.getMessage();
		}finally{
			if(message != null){
				progress.setStatus(message);
				progress.close("Error", true, true);
			}else{
				progress.close("ECI Workflow creation is complete.", false, true);
			}
		}
	}
	
	private void changeStatus() throws TCException {
		eciRevision.setProperty("s7_ECI_MATURITY", "B");
	}

	/**
	 * 결재선이 템플릿에 맞게 구성 되어 있는지 확인
	 * @return
	 * @throws Exception
	 */
	private boolean checkReviewer() throws Exception {
		String workflow = eciRevision.getProperty("s7_WORKFLOW");
		String[] reviewers = null;
		if(workflow.equals("")){
			message = "\nCannot find approval line.\nPlease check the approval line.";
			return true;
		}else{
			reviewers = workflow.split(SYMCECConstant.SEPERATOR);
			for(int i = 0 ; i < reviewers.length ; i++){
				if(reviewers[i] != null){
					String[] reviewerInfo = reviewers[i].split(":");
					reviewerComp[i] = (TCComponentGroupMember) getSession().stringToComponent(reviewerInfo[2]);
				}
			}
		}

		boolean hasTeamLeader = false;
		for(int i = 0 ; i < 4 ; i++){
			if(reviewerComp[i] != null){
				String roleName = reviewerComp[i].getRole().getProperty("role_name");
				if(roleName.equals("TEAM_LEADER")){ // 팀장이 결재선에서 누락 되면 안됨.
					hasTeamLeader = true;
					break;
				}
			}
		}
		if(hasTeamLeader){
			return false;
		}else{
			message = "\nCannot find team leader in approval line.\nPlease check the approval line.";
			return true;
		}
    }
	
    /**
     * 프로세스 생성
     * @throws Exception
     */
    private void createProcess() throws Exception{
    	
		TCComponentTaskTemplateType compTaskTmpType = (TCComponentTaskTemplateType)session.getTypeComponent("EPMTaskTemplate");
		TCComponentTaskTemplate template = compTaskTmpType.find(SYMCECConstant.ECI_PROCESS_TEMPLATE, 0);

		TCComponentProcessType processtype = (TCComponentProcessType)session.getTypeComponent("Job");
		
		TCComponent[] componentList = new TCComponent[1];
		componentList[0] = eciRevision;
		
		newProcess = (TCComponentProcess)processtype.create("ECI [" + eciRevision.getProperty("item_id") + "] approval request.", "Please confirm ASAP.", template, componentList, ProcessUtil.getAttachTargetInt(componentList));
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
        	TCComponentGroupMember assignMember = null;
        	String[] eciTasks = SYMCECConstant.ECI_TASK_LIST;
        	for(int i = 0 ; i < eciTasks.length ; i++){
        		if(reviewTaskName.equals(eciTasks[i])){
        			assignMember = reviewerComp[i];
        		}
        	}
        	if(reviewTaskName.equals("Creator"))
        		assignMember = session.getUser().getGroupMembers()[0];
        	
        	TCComponentTask selectSignoffTeam = subTask.getSubtask("select-signoff-team");
        	if(selectSignoffTeam != null){
        		try{
        			selectSignoffTeam.lock();
        			if(assignMember == null){
//        				selectSignoffTeam.getTCProperty("done").setLogicalValue(true);
        				selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
        				selectSignoffTeam.save();
        			}else{
        				TCComponentSignoff[] sifnoffList = new TCComponentSignoff[1];
        				int[] attachTypeList = new int[1];
        				sifnoffList[0] = signoffType.create(assignMember, TCSignoffOriginType.ADHOC_USER, null);
        				attachTypeList[0] = TCAttachmentType.SIGNOFF;

        				selectSignoffTeam.addAttachments(TCAttachmentScope.LOCAL, sifnoffList, attachTypeList);
//        				selectSignoffTeam.getTCProperty("done").setLogicalValue(true);        				
        				selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");				
        				
        				selectSignoffTeam.save();
        				if(selectSignoffTeam.getParent().getState().equals(TCTaskState.STARTED)) {
        					selectSignoffTeam.performAction(TCComponentTask.COMPLETE_ACTION, "");
        				}
        			}
        		} catch(Exception e){
        			throw e;
        		} finally {
        			selectSignoffTeam.unlock();
        		}
        	}
        }
	}
	
	private void sendMail() throws Exception{
		CustomECODao dao = new CustomECODao();
		String fromUser = session.getUser().getUserId();
		String title = "NPLM : " + eciRevision.getProperty("item_id") + "결재 요청";
		String body = "결재 요청 바랍니다.";
		String toUsers = reviewerComp[0].getUserId();
		dao.sendMail(fromUser, title, body, toUsers);
	}
}
