package com.ssangyong.common.utils;

import java.util.ArrayList;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProfile;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentSignoffType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCComponentTaskType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCSignoffOriginType;
import com.teamcenter.rac.kernel.TCTaskState;
import com.teamcenter.rac.util.MessageBox;
/**
 * 프로세스 관련 Custom Util
 * @author DJKIM
 *
 */
public class ProcessUtil {

	/**
	 * 프로세스 타겟으로 포함 될 관계로 타겟을 얻는다.
	 * @param target
	 * @param relationArray
	 * @return
	 * @throws TCException
	 */
	public static TCComponent[] getAttechComponent(TCComponentItemRevision target, String[] relationArray) throws TCException{

		ArrayList<TCComponent> arrayList = new ArrayList<TCComponent>();
		TCComponent[] targetArray;
		arrayList.add(target);
		for(int j = 0 ; j < relationArray.length ; j++){
			TCComponent[] comp = target.getRelatedComponents(relationArray[j]);
			if(comp.length != 0){
				for( int i = 0 ; i < comp.length ; i++){
					arrayList.add((TCComponent)comp[i]);
				}
			}	
		}
		targetArray = new TCComponent[arrayList.size()];

		for( int k = 0 ; k < arrayList.size() ; k++){
			targetArray[k] = (TCComponent)arrayList.get(k);
		}
		return targetArray;
	}

	/**
	 * 타겟리스트의 타겟 타입리스트 획득
	 * @param targetArray
	 * @return
	 */
	public static int[] getAttachTargetInt( TCComponent[] targetArray ){

		int[] attachTargetInt = new int[targetArray.length];

		for( int i = 0 ; i < attachTargetInt.length ; i++){
			attachTargetInt[i]=TCAttachmentType.TARGET;
		}
		return attachTargetInt;
	}


	/**
	 * TCComponentTaskTemplateType 으로 TCComponentTaskTemplate을 가지고 온다
	 * @param session
	 * @param templateName
	 * @return
	 */
	public static TCComponentTaskTemplate getNamedProcessTemplate(TCSession session, String templateName){
		
		TCComponentTaskTemplate selectedTaskTemplate = null;
		TCComponentTaskTemplateType imancomponenttasktemplatetype = null;
		try {
			imancomponenttasktemplatetype = (TCComponentTaskTemplateType)session.getTypeComponent("EPMTaskTemplate");
			//TCComponentTaskTemplate[] aimancomponentList = (TCComponentTaskTemplate[])imancomponenttasktemplatetype.extentReadyTemplates(true);
			TCComponentTaskTemplate[] aimancomponentList = (TCComponentTaskTemplate[])imancomponenttasktemplatetype.getProcessTemplates(false, false, null, null, null);
			
			for(TCComponentTaskTemplate aimancomponent : aimancomponentList){
				if(aimancomponent.getName().equals(templateName)){
					selectedTaskTemplate = aimancomponent;
					break;
				}
			}
		} catch(TCException e){
			MessageBox.post(e);
			e.printStackTrace();
		}
		return selectedTaskTemplate;
	}

	/**
	 * TCComponentSignoff 생성
	 * @param reviewTask
	 * @param signoffType
	 * @param reviewMember
	 * @throws TCException
	 */
	public static void addRevieweSignoff(TCComponentTask reviewTask, TCComponentSignoffType signoffType,TCComponentGroupMember reviewMember) {
		try{

//			TCComponentTask selectSignoffTask = getSignoffTask(reviewTask, true);
			TCComponentTask selectSignoffTask = reviewTask;
			TCComponentProfile[] profiles = selectSignoffTask.getSignoffProfiles();

			if (profiles != null && profiles.length > 0) {
				for (int i = 0; i < profiles.length; i++) {
					if (profiles[i].getGroup().equals(reviewMember.getGroup())
							|| profiles[i].getRole().equals(reviewMember.getRole())) {
						TCComponentSignoff signoff = signoffType.create(reviewMember,
								TCSignoffOriginType.PROFILE, profiles[i]);
						if (signoff != null) {
							selectSignoffTask.add("signoff_attachments", signoff);
						}
					}
				}
			} else {
				signoffType.createAdhoc(selectSignoffTask, reviewMember);
			}
//			selectSignoffTask.setLogicalProperty("done", true);
			selectSignoffTask.setStringProperty("task_result", "Completed");

			if (selectSignoffTask.getState() == TCTaskState.STARTED) {
				selectSignoffTask.performAction(TCComponentTask.COMPLETE_ACTION, "Auto Complete");
			}

		}catch(Exception e){
			MessageBox.post(e);
			e.printStackTrace();
		}
	}
	
	/**	멀티로 User Assign 되는 Task를 처리 하기 위한 함수임.
	 * @param reviewTask
	 * @param signoffType
	 * @param reviewMember           // 1명 이상의 user Assign
	 */
	public static void addRevieweSignoffs(TCComponentTask reviewTask, TCComponentSignoffType signoffType,TCComponentGroupMember[] reviewMember){
		try {
			TCComponentTask selectSignoffTask = getSignoffTask(reviewTask, true);
			TCComponentProfile[] profiles = selectSignoffTask.getSignoffProfiles();

			for(int i=0; i< reviewMember.length; i++){
				if (profiles != null && profiles.length > 0) {
					for (int j = 0; j < profiles.length; j++) {
						if (profiles[j].getGroup().equals(reviewMember[i].getGroup())
								|| profiles[j].getRole().equals(reviewMember[i].getRole())) {
							TCComponentSignoff signoff = signoffType.create(reviewMember[i],
									TCSignoffOriginType.PROFILE, profiles[j]);
							if (signoff != null) {
								selectSignoffTask.add("signoff_attachments", signoff);
							}
						}
					}
				} else {
					signoffType.createAdhoc(selectSignoffTask, reviewMember[i]);
				}			
			}
//			selectSignoffTask.setLogicalProperty("done", true);
			selectSignoffTask.setStringProperty("task_result", "Completed");
			if (selectSignoffTask.getState() == TCTaskState.STARTED) {
				selectSignoffTask.performAction(TCComponentTask.COMPLETE_ACTION, "Auto Complete");
			}
		} catch (TCException e) {
			MessageBox.post(e);
			e.printStackTrace();
		}

	}
	
	/**
	 * Item Revision 아래의 DataSet을 가져옴
	 * Usage: getDatasets(itemRevision,"IMAN_specification")
	 * @param itemRevision
	 * @param relationType
	 * @return
	 * @throws TCException
	 */
	public static ArrayList<TCComponent> getDatasets(TCComponentItemRevision itemRevision, String relationType) throws TCException {
		if(relationType == null)
			relationType = "IMAN_specification";

		ArrayList<TCComponent> datasets = new ArrayList<TCComponent>();
		TCComponent[] relatedComponents = itemRevision.getRelatedComponents(relationType);
		for (TCComponent component : relatedComponents) {
			if (component instanceof TCComponentDataset) {
				datasets.add(component);
			}
		}
		return datasets;
	}

	/**
	 * reviewTask 하위의 select singoff Task를 구한다.
	 * @param task
	 * @param isSelect
	 * @return
	 * @throws TCException
	 */
	public static TCComponentTask getSignoffTask(TCComponentTask task, boolean isSelect) throws TCException {

		TCComponentTask signoffTask = null;
		TCComponentTask[] aSubTask = task.getSubtasks();

		for (int i = 0; i < aSubTask.length; i++) {

			if (isSelect && aSubTask[i].getType().equals(TCComponentTaskType.EPM_SELECT_SIGNOFF_TASK_TYPE)) {
				signoffTask = aSubTask[i];
				break;
			}else if (!isSelect && aSubTask[i].getType().equals(TCComponentTaskType.EPM_PERFORM_SIGNOFF_TASK_TYPE)) {
				signoffTask = aSubTask[i];
				break;
			}
		}
		return signoffTask;
	}

	/**
	 * 해당 Component가 결재 진행 중인지 아닌지 확인
	 *  반려 상태도 working 으로 간주 함 (Creator) 20130715, bskwak
	 * @param components
	 * @return
	 * @throws TCException
	 */
	public static boolean isWorkingStatus(TCComponent components) throws TCException {
		components.refresh();
		if (components.getProperty("release_status_list").equalsIgnoreCase("") && 
				(components.getProperty("process_stage_list").equalsIgnoreCase("")
					|| components.getProperty("process_stage_list").equalsIgnoreCase("Creator"))) {
			return true;
		}
		return false;
	}


	/**
	 * 해당 Component가 결재 완료 인지 확인
	 * @param components
	 * @return 결재 완료 true
	 * @throws TCException
	 */
	public static boolean isReleased(TCComponent components) throws TCException {
		components.refresh();
		if (!components.getProperty("release_status_list").equalsIgnoreCase("")) {
			return true;
		}
		return false;
	}

	/**
	 * 타겟에 대한 Process를 가지고 온다
	 * @param imancomponent
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<TCComponentProcess> getProcess(TCComponent imancomponent) throws Exception {
		
		ArrayList<TCComponentProcess> list = new ArrayList<TCComponentProcess>();
		AIFComponentContext[] aaifcomponentcontext = null;
		InterfaceAIFComponent interfaceaifcomponent = null;
		TCComponent com;

		if (imancomponent instanceof TCComponentProcess) {
			list.add((TCComponentProcess) imancomponent);
		}
		else {
			imancomponent.refresh(); //refresh
			aaifcomponentcontext = imancomponent.whereReferenced(true);
			for (int i = 0; i < aaifcomponentcontext.length; i++) {
				interfaceaifcomponent = aaifcomponentcontext[i].getComponent();
				com = (TCComponent) interfaceaifcomponent;
				if (com instanceof TCComponentProcess) {
					list.add((TCComponentProcess) com);
				}
			}
		}
		return list;
	}

	/**
	 * 타겟에 대한 Process를 가지고 온다
	 * Copyright : (주) ENES Solutions
	 * @author : mskim
	 * @since  : 2009. 12. 1.
	 * @param imancomponent
	 * @return
	 * @throws Exception
	 */
	public static TCComponentTask getRootTask(TCComponent imancomponent) throws Exception{
		
		TCComponentProcess process = null;
		TCComponentTask rootTask = null;
		ArrayList<TCComponentProcess> list = getProcess(imancomponent);
		
		if( list.size() != 0 ){
			if( list.size() == 1){
				process = list.get(0);
				rootTask = process.getRootTask();
			}else{
				
				for( int i = 0 ; i < list.size() ; i++ ){
					process = list.get(i);
					rootTask = process.getRootTask();
					if(rootTask.getProperty("task_result").equals("Unset")){
						break;
					}else{
						rootTask = null;
					}
				}
			}
		}
		
		return rootTask;
	}
	/**
	 * Form에 정의 되어있는 결재자 정보를 TCComponentGroupMember[]로 가지고 온다
	 * @param form_rel
	 * @throws TCException
	 */
	public static TCComponentGroupMember[] getFormsUserFind(TCSession session, TCComponent targetCom, String form_rel, String[] assingUserField) throws TCException{

		TCComponentGroupMember[] assignMemver = null;

		return assignMemver;
	}
	
	/** 타겟에 받아온 프로세서에서 각 단계별 결재 관련 정보 리스트를 뽑아온다. 
	 * @return 
	 * @return
	 * @throws Exception 
	 */
	public static TCComponent[] hasDecision(TCComponentTask rootTask, String taskName) throws TCException{
		
		TCComponentTask[] levelTasks = null;
		TCComponentTask[] subTasks = null;
		TCComponent[] comp1 = null;
		
		levelTasks = rootTask.getSubtasks();
		for( int i = 0 ; i < levelTasks.length ; i++ ){
			
			if(levelTasks[i].getName().equals(taskName)){
				subTasks = levelTasks[i].getSubtasks();
				
				for( int j = 0 ; j < subTasks.length ; j++){
					
					if(subTasks[j].getName().equals("select-signoff-team")){
						comp1 = subTasks[j].getReferenceListProperty("valid_signoffs");
					}
				}
			}
		}
		return comp1;
	}
	
	/**
	 * 해당 Task TCComponent의 singoff(결재자정보)를 리턴
	 * @param comp
	 * @return
	 * @throws Exception
	 */
	public static TCComponentSignoff[] getChangeSignoffComp(TCComponent[] comp) throws Exception{

		TCComponentSignoff[] singoff = new TCComponentSignoff[comp.length];
		for(int i = 0 ; i < comp.length ; i++){
			singoff[i] = (TCComponentSignoff)comp[i];
		}
		return singoff;
	}

}
