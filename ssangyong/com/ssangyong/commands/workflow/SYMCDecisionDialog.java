package com.ssangyong.commands.workflow;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.workflow.ECOProcessOperation;
import com.ssangyong.commands.workflow.correction.EcoEplCorrectionDialog;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.workflow.commands.adhoc.SignoffTree;
import com.teamcenter.rac.workflow.commands.newperformsignoff.DecisionDialog;
import com.teamcenter.rac.workflow.commands.newperformsignoff.SignoffDecisionOperation;

/**
 * [20160727][ymjang] ECO 보정내역은 ECO 최종 결재시에만 표시하도록 변경함.
 * [SR170816-008][LJG] ECO 최종 승인 시 s7_ECO_MATURITY(ECO 속성) 속성에 각각 Completed로 변경을 해주도록 W/F Handler셋팅이 되어있는데,
 * 종종 Handler가 속성변경을 못하는 경우가 있어서, 실패 했을경우 관리자에게 메일보내도록
 * 
 */
/** 코어의 DecisionDialog 를 상속받아서 재정의 */
@SuppressWarnings({"serial", "unused", "unchecked"})
public class SYMCDecisionDialog extends DecisionDialog {
	private boolean validation = false;
	private TCComponentProcess currentJob;
	private boolean isMeco = false;
	private boolean isMecoForCreator= false;
	private boolean isEco = false;
	private boolean isDCS = false;
	private TCComponentChangeItemRevision  mecoRevision = null;

	private SignoffDecisionOperation decisionOp;
	
	
	// 2024.01.09 추가
	private TCComponentSignoff signoffObject;

	// PLM관리자 박세호(208748), 박건호(218583)
	public static final String[] PLM_ADMIN = {"208748", "218583"};

	public TCComponentProcess getCurrentJob() {
		return currentJob;
	}

	public void setCurrentJob(TCComponentProcess currentJob) {
		this.currentJob = currentJob;
	}

	public void setValidation(boolean validation) {
		this.validation = validation;
	}

	public SYMCDecisionDialog(AIFDesktop aifdesktop, TCComponentTask tccomponenttask, TCComponentSignoff tccomponentsignoff) throws Exception{
		super(aifdesktop, tccomponenttask, tccomponentsignoff);
		
		// 2024.01.09 추가
		this.signoffObject = tccomponentsignoff;
		
		this.setModal(true);
		isMecoForCreator = checkSignoffTaskOfMecoForCreator();
		isEco = checkIsEco();
		isDCS = checkIsDCS();
		if(isMecoForCreator) {
			super.disposeDialog();
			String msg = "This perform-signoffs is the approval re-demanding."+"\n"+" You shoud use button as 'Request Approval' in MECO. "+"\n";
			MessageBox.post(msg,"INFORMATION", MessageBox.INFORMATION);
			throw (new Exception("This perform-signoffs is the approval re-demanding."+"\n"+" You shoud use button as 'Request Approval' in MECO. "));
		} else if (isDCS) {
			super.disposeDialog();
			String msg = "My Worklist에서는 DCS결재를 진행할 수 없습니다.\n좌측하단의 Design Concept System 메뉴 내에서만 처리가능합니다.";
			MessageBox.post(msg,"INFORMATION", MessageBox.INFORMATION);
			throw (new Exception("My Worklist에서는 DCS결재를 진행할 수 없습니다.\n좌측하단의 Design Concept System 메뉴 내에서만 처리가능합니다."));
		} else{
			super.initializeDialog();
		}
	}

	/**
	 * [SR없음][20151207][jclee] ECO 재상신 시 Decision Dialog Update
	 */
	public void updateSYMCDecisionDialog() {
		if ((this.signoffObj != null) && (SignoffTree.isAccessableSignoff(this.signoffObj)) && (!(isMineToPerform(this.signoffObj))) && (!(isActiveSurrogate()))) {
			this.signoffObj.clearCache("is_mine_to_perform");
			super.updateDecisionDialog(true);
		}
	}

	private boolean checkSignoffTaskOfMecoForCreator() {

		try {

			if(psTask.getRoot().getName().contains("SYMC_MECO")){
				TCComponent[] comps = psTask.getRoot().getRelatedComponents("root_target_attachments");
				if("Creator".equals(psTask.getParent().getName())) {
					isMecoForCreator = true;
					isMeco = true;
				}else{
					isMecoForCreator = false;
					isMeco = true;
				}

			}


		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(e);
		}

		return isMecoForCreator;
	}

	private boolean checkIsEco() {

		try {

			if(psTask.getRoot().getName().equals(SYMCECConstant.ECO_PROCESS_TEMPLATE)){
				isEco = true;
			}
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(e);
		}

		return isEco;
	}

	private boolean checkIsDCS() {
		/** [SR151105-029][20151110][jclee] DCS 결재 타스크 목록 */
		String[] saDCSProcessTemplates = new String[] {"SYMC_DCS", "SYMC_DCS_TEAM", "SYMC_DCS_MIG"};

		try {
			for (int inx = 0; inx < saDCSProcessTemplates.length; inx++) {
				String sDCSProcessTemplate = saDCSProcessTemplates[inx];

				if(psTask.getRoot().getName().equals(sDCSProcessTemplate)){
					isDCS = true;
					break;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(e);
		}

		return isDCS;
	}
	@Override
	public void setEditable(boolean flag) {
		super.setEditable(flag);
		cancelB.setEnabled(flag);
	}

	@Override
	public void commitDecision(){
		//************* Validation 시작 ***********//  
		// 2024.01.09  TCCRDecision.REJECT_DECISION  -->>  signoffObj.getRejectDecision()
		try {
			if(decision == signoffObj.getRejectDecision()){
				if(getComments() == null || getComments().equals("")){
					MessageBox.post(Registry.getRegistry(this).getString("SYMCDecisionDialog.MESSAGE.CommentsMiss"), "INFORMATION", MessageBox.INFORMATION);
					this.disposeDialog();
					return;
				}

				changeStatus();
				//반려 메일 발송
				sendRejectMail();
			}
			setEditable(false);

			
			// 2024.01.09  TCCRDecision.REJECT_DECISION  -->>  signoffObj.getApproveDecision()
			if(decision == signoffObject.getApproveDecision()){
				checkReProcess();
			}else{
				validation = true;
			}
			//************* Validation 끝	***********//

			if(validation){
				runOperation();
			}
		}catch(Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
	}

	private void changeStatus() {

		if(isEco) {

			CustomECODao dao = new CustomECODao();
			try {
				TCComponent[] comps = psTask.getRoot().getRelatedComponents("root_target_attachments");
				TCComponentChangeItemRevision changeRevision = null;
				for(TCComponent comp : comps){
					if(comp.getType().equals("S7_ECORevision")){

						changeRevision = (TCComponentChangeItemRevision)comp;
						break;
					}
				}
				dao.updateEcoStatus(changeRevision.getUid(), "In Work", "In Work");
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			}
		}else if(isMeco) {
			//nothing.
		}



	}

	public void runOperation(){
		//************* 원본 commitDecision() 메소드 부분 재정의 ~시작 ***********//
		session.setReadyStatus();
		startOperation("");
		if(signoffObj != null){
			if(!is_secure_task){
				decisionOp = new SYMCSignoffDecisionOperation(this, session, /*desktop,*/ psTask, signoffObj, getDecision(), getComments());
			} else{
				decisionOp = new SYMCSignoffDecisionOperation(this, session, /*desktop,*/ psTask, signoffObj, getDecision(), getComments(), getPassword());
				passwordTextField.setText("");
			}

			decisionOp.addOperationListener(this);

			/*
			 * SRME:: [][20140820] swyoon EPL 보정 리스트 출력.
			 * 
			 */
			decisionOp.addOperationListener(new InterfaceAIFOperationListener() {

				@Override
				public void startOperation(String arg0) {

				}

				@Override
				public void endOperation() {
					System.out.println("2. endOperation()");
					try{
						String rootTaskName = psTask.getRoot().getName();
						String thisTaskName = psTask.getParent().getName();
						if(rootTaskName.equals(SYMCECConstant.ECO_PROCESS_TEMPLATE)){
							
							if(!(thisTaskName.equals("Design Team Leader") || thisTaskName.equals("Technical Management"))) {
								return;
							}
							
							TCComponent[] comps = psTask.getRoot().getRelatedComponents("root_target_attachments");
							TCComponentChangeItemRevision changeRevision = null;
							for(TCComponent comp : comps){
								if(comp.getType().equals("S7_ECORevision")){								
									changeRevision = (TCComponentChangeItemRevision)comp;
									break;
								}
							}

							if (changeRevision == null) 
								return;
							
							// Technical Management : 결재 완료 시 처리
							if(thisTaskName.equals("Technical Management") ) {
								String ecoNo = changeRevision.getProperty("item_id");
								SYMCRemoteUtil remote = new SYMCRemoteUtil();
								DataSet ds = new DataSet();
								ds.put("ECO_NO", ecoNo);

								ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>)remote.execute(CustomECODao.ECO_INFO_SERVICE_CLASS, "getIncorrectList", ds);
								if( list != null && !list.isEmpty()){
									EcoEplCorrectionDialog dlg = new EcoEplCorrectionDialog(ecoNo, list);
									dlg.setVisible(true);
								}
								
								/**
								 * [SR170816-008][LJG] ECO 최종 승인 시 s7_ECO_MATURITY(ECO 속성) Completed로 변경을 해주도록 W/F Handler셋팅이 되어있는데,
								 * 종종 Handler가 속성변경을 못하는 경우가 있어서, 실패 했을경우 관리자에게 메일보내도록
								 */
								
								// 2024.01.09  수정   TCCRDecision.APPROVE_DECISION  -->   signoffObj.getApproveDecision()
								if(signoffObj.getDecision() == signoffObj.getApproveDecision()){
									changeRevision.refresh();
									String eco_maturity = changeRevision.getStringProperty("s7_ECO_MATURITY");
									if(!"Completed".equalsIgnoreCase(eco_maturity)){
										sendMaturityMail(changeRevision);
									}
								}
							}
							
							// [20170901][LJG] Design Team Leader : 결재 완료 시 처리
							// 2024.01.09  수정   TCCRDecision.APPROVE_DECISION  -->   signoffObj.getApproveDecision()
							if(thisTaskName.equals("Design Team Leader") ) {
								if(signoffObj.getDecision() == signoffObj.getApproveDecision()){
									changeRevision.refresh();
									String eco_maturity = changeRevision.getStringProperty("s7_ECO_MATURITY");
									if(!"Approved".equalsIgnoreCase(eco_maturity)){
										sendMaturityMail(changeRevision);
									}
								}
							}
						}
						
					}catch(Exception e){
						e.printStackTrace();
						return;
					}
				}

				/* [20160727][ymjang] ECO 보정내역은 ECO 최종 결재시에만 표시하도록 변경함.
				@Override
				public void endOperation() {
					String ecoNo = null;

					try{
						TCComponent[] comps = psTask.getRoot().getRelatedComponents("root_target_attachments");
						TCComponentChangeItemRevision changeRevision = null;
						for(TCComponent comp : comps){
							if(comp.getType().equals("S7_ECORevision")){

								changeRevision = (TCComponentChangeItemRevision)comp;
								break;
							}
						}				

						ecoNo = changeRevision.getProperty("item_id");
					}catch(Exception e){
						e.printStackTrace();
						return;
					}

					SYMCRemoteUtil remote = new SYMCRemoteUtil();
					DataSet ds = new DataSet();
					ds.put("ECO_NO", ecoNo);

					try {
						ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>)remote.execute(CustomECODao.ECO_INFO_SERVICE_CLASS, "getIncorrectList", ds);
						if( list != null && !list.isEmpty()){
							EcoEplCorrectionDialog dlg = new EcoEplCorrectionDialog(ecoNo, list);
							dlg.setVisible(true);
						}

					} catch (Exception e1) {
						e1.printStackTrace();
					}						
				}
				 */				
			});

			session.queueOperation(decisionOp);
		}
	}

	private void checkReProcess() {

		if(isEco){

			try{
				if(psTask.getRoot().getName().equals(SYMCECConstant.ECO_PROCESS_TEMPLATE)){
					if(psTask.getParent().getName().equals("Creator")){

						TCComponent[] comps = psTask.getRoot().getRelatedComponents("root_target_attachments");
						TCComponentChangeItemRevision changeRevision = null;
						for(TCComponent comp : comps){
							if(comp.getType().equals("S7_ECORevision")){

								changeRevision = (TCComponentChangeItemRevision)comp;
								break;
							}
						}
						//					setCurrentJob(psTask.getCurrentJob());
						setCurrentJob(psTask.getProcess());	// 20130611, 김대중C, 워크플로우 재상신 할 때 타겟이 전부 떨어지는 경우에 eco revision을 찾아서 붙여주는 로직 

						ECOProcessOperation operation = new ECOProcessOperation(this, session, changeRevision);
						session.queueOperation(operation);

					}else{
						validation = true;
					}
				}else{
					validation = true;
				}
			}catch(Exception e){
				e.printStackTrace();
				MessageBox.post(e);
			}
		}else if(isMeco){
			validation = true;
			//			MECOProces
		}else{
			validation = true;
		}
	}

	private void sendRejectMail() {

		CustomECODao dao = new CustomECODao();

		try{

			String fromUser = session.getUser().getUserId();
			String title = "New PLM : " + psTask.getRoot().getProcess().getName() + " 결재 반려 통보";

			String body = "<PRE>";
			body += "아래와 같이 결재가 반려 되었습니다." + "<BR>";
			body += " -TASK : " + psTask.getParent().getName() + "<BR>";
			body += " -USER : " + signoffObj.getMember() + "<BR>";
			body += " -COMMENTS : " + "<BR>";
			body += "     " + getComments().replace("\n","<BR>     ");
			body += "</PRE>";

			//			String toUsers = psTask.getRoot().getProcess().getProperty("owning_user");
			TCComponent owningUser = psTask.getRoot().getProcess().getReferenceProperty("owning_user");
			String toUsers = ((TCComponentUser)owningUser).getUserId();
			dao.sendMail(fromUser, title, body, toUsers);

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * [SR170816-008][LJG] ECO 최종 승인 시 s7_MATURITY(파트 속성), s7_ECO_MATURITY(ECO 속성) 속성에 각각 Released, Completed로 변경을 해주도록 W/F Handler셋팅이 되어있는데,
	 * 종종 Handler가 속성변경을 못하는 경우가 있어서, 실패 했을경우 관리자에게 메일보내도록
	 */
	private void sendMaturityMail(TCComponentChangeItemRevision changeRevision) throws TCException, Exception {
		try{
			CustomECODao dao = new CustomECODao();
			
			String ecNo = changeRevision.getProperty("item_id");
			String fromUser = session.getUser().getUserId();
			String title = "New PLM : ECO[" + ecNo + "] Release 후 s7_ECO_MATURITY 업데이트 오류";
			String body = "New PLM : ECO[" + ecNo + "] Release 후 s7_ECO_MATURITY 업데이트 오류";
			
			for(int i=0; i<PLM_ADMIN.length; i++){
				dao.sendMail(fromUser, title, body, PLM_ADMIN[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
