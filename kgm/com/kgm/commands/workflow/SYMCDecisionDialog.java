package com.kgm.commands.workflow;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.workflow.ECOProcessOperation;
import com.kgm.commands.workflow.correction.EcoEplCorrectionDialog;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
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
 * [20160727][ymjang] ECO ���������� ECO ���� ����ÿ��� ǥ���ϵ��� ������.
 * [SR170816-008][LJG] ECO ���� ���� �� s7_ECO_MATURITY(ECO �Ӽ�) �Ӽ��� ���� Completed�� ������ ���ֵ��� W/F Handler������ �Ǿ��ִµ�,
 * ���� Handler�� �Ӽ������� ���ϴ� ��찡 �־, ���� ������� �����ڿ��� ���Ϻ�������
 * [20240404][UPGRADE] Decision Ÿ�� üũ�ϴ� �κ� TC13 �������� ���� ����
 * 
 */
/** �ھ��� DecisionDialog �� ��ӹ޾Ƽ� ������ */
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
	
	
	// 2024.01.09 �߰�
	private TCComponentSignoff signoffObject;

	// PLM������ �ڼ�ȣ(208748), �ڰ�ȣ(218583)
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
		
		// 2024.01.09 �߰�
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
			String msg = "My Worklist������ DCS���縦 ������ �� �����ϴ�.\n�����ϴ��� Design Concept System �޴� �������� ó�������մϴ�.";
			MessageBox.post(msg,"INFORMATION", MessageBox.INFORMATION);
			throw (new Exception("My Worklist������ DCS���縦 ������ �� �����ϴ�.\n�����ϴ��� Design Concept System �޴� �������� ó�������մϴ�."));
		} else{
			super.initializeDialog();
		}
	}

	/**
	 * [SR����][20151207][jclee] ECO ���� �� Decision Dialog Update
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
		/** [SR151105-029][20151110][jclee] DCS ���� Ÿ��ũ ��� */
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
		//************* Validation ���� ***********//  
		// 2024.01.09  TCCRDecision.REJECT_DECISION  -->>  signoffObj.getRejectDecision()
		try {
			
			//[UPGRADE][2024.0404] Acknowledge Task üũ �� ���� 
			boolean isAcknowledgeTask =	psTask != null && psTask.getParent().getTaskType().equals("EPMAcknowledgeTask");
			
			if(!isAcknowledgeTask && decision.getIntValue() == signoffObj.getRejectDecision().getIntValue()){
				if(getComments() == null || getComments().equals("")){
					MessageBox.post(Registry.getRegistry(this).getString("SYMCDecisionDialog.MESSAGE.CommentsMiss"), "INFORMATION", MessageBox.INFORMATION);
					this.disposeDialog();
					return;
				}

				changeStatus();
				//�ݷ� ���� �߼�
				sendRejectMail();
			}
			setEditable(false);

			//[UPGRADE][2024.0404] ����
			if(decision.getIntValue() == signoffObject.getApproveDecision().getIntValue()){
				checkReProcess();
			}else{
				validation = true;
			}
			//************* Validation ��	***********//

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
		//************* ���� commitDecision() �޼ҵ� �κ� ������ ~���� ***********//
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
			 * SRME:: [][20140820] swyoon EPL ���� ����Ʈ ���.
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
							
							// Technical Management : ���� �Ϸ� �� ó��
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
								 * [SR170816-008][LJG] ECO ���� ���� �� s7_ECO_MATURITY(ECO �Ӽ�) Completed�� ������ ���ֵ��� W/F Handler������ �Ǿ��ִµ�,
								 * ���� Handler�� �Ӽ������� ���ϴ� ��찡 �־, ���� ������� �����ڿ��� ���Ϻ�������
								 */
								
								// 2024.01.09  ����   TCCRDecision.APPROVE_DECISION  -->   signoffObj.getApproveDecision()
								if(signoffObj.getDecision().getIntValue() == signoffObj.getApproveDecision().getIntValue()){
									changeRevision.refresh();
									String eco_maturity = changeRevision.getStringProperty("s7_ECO_MATURITY");
									if(!"Completed".equalsIgnoreCase(eco_maturity)){
										sendMaturityMail(changeRevision);
									}
								}
							}
							
							// [20170901][LJG] Design Team Leader : ���� �Ϸ� �� ó��
							// 2024.01.09  ����   TCCRDecision.APPROVE_DECISION  -->   signoffObj.getApproveDecision()
							if(thisTaskName.equals("Design Team Leader") ) {
								if(signoffObj.getDecision().getIntValue() == signoffObj.getApproveDecision().getIntValue()){
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

				/* [20160727][ymjang] ECO ���������� ECO ���� ����ÿ��� ǥ���ϵ��� ������.
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
						setCurrentJob(psTask.getProcess());	// 20130611, �����C, ��ũ�÷ο� ���� �� �� Ÿ���� ���� �������� ��쿡 eco revision�� ã�Ƽ� �ٿ��ִ� ���� 

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
			String title = "New PLM : " + psTask.getRoot().getProcess().getName() + " ���� �ݷ� �뺸";

			String body = "<PRE>";
			body += "�Ʒ��� ���� ���簡 �ݷ� �Ǿ����ϴ�." + "<BR>";
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
	 * [SR170816-008][LJG] ECO ���� ���� �� s7_MATURITY(��Ʈ �Ӽ�), s7_ECO_MATURITY(ECO �Ӽ�) �Ӽ��� ���� Released, Completed�� ������ ���ֵ��� W/F Handler������ �Ǿ��ִµ�,
	 * ���� Handler�� �Ӽ������� ���ϴ� ��찡 �־, ���� ������� �����ڿ��� ���Ϻ�������
	 */
	private void sendMaturityMail(TCComponentChangeItemRevision changeRevision) throws TCException, Exception {
		try{
			CustomECODao dao = new CustomECODao();
			
			String ecNo = changeRevision.getProperty("item_id");
			String fromUser = session.getUser().getUserId();
			String title = "New PLM : ECO[" + ecNo + "] Release �� s7_ECO_MATURITY ������Ʈ ����";
			String body = "New PLM : ECO[" + ecNo + "] Release �� s7_ECO_MATURITY ������Ʈ ����";
			
			for(int i=0; i<PLM_ADMIN.length; i++){
				dao.sendMail(fromUser, title, body, PLM_ADMIN[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
