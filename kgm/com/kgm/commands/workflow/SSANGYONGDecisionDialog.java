package com.kgm.commands.workflow;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponentEnvelope;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.log.Debug;
import com.teamcenter.rac.workflow.commands.newperformsignoff.DecisionDialog;
import com.teamcenter.rac.workflow.commands.newperformsignoff.SignoffDecisionOperation;

/**
 * 코어의 DecisionDialog 를 상속받아서 재정의
 * 승인 또는 반려시에 주석 필수로.
 * 반려시 그전 담당자에게 반려 알림 메일이 mail box로 간다.
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2012. 5. 2.
 * Package ID : com.pungkang.commands.workflow.SpalmDecisionDialog.java
 */
@SuppressWarnings("unused")
public class SSANGYONGDecisionDialog extends DecisionDialog {

	private static final long serialVersionUID = 1L;

	private TCComponentTask currentTask;

	private TCComponentEnvelope envelope;
	
	private SignoffDecisionOperation decisionOp;


	public SSANGYONGDecisionDialog(AIFDesktop aifdesktop, TCComponentTask tccomponenttask, TCComponentSignoff tccomponentsignoff){
		super(aifdesktop, tccomponenttask, tccomponentsignoff);
		this.setModal(true);
		currentTask = tccomponenttask;
		super.initializeDialog();
	}

	@Override
	public void setEditable(boolean flag) {
		super.setEditable(flag);
		cancelB.setEnabled(flag);
	}

	@Override
	public void commitDecision(){
		// 2024.01.09 수정 TCCRDecision.REJECT_DECISION -->  signoffObj.getRejectDecision()
		try {
			if(decision == signoffObj.getRejectDecision()){
				if(getComments() == null || getComments().equals("")){
					MessageBox.post("주석이 누락되어 있습니다.", "알림", MessageBox.INFORMATION);
					return;
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setEditable(false);

		//********************************* 원본 commitDecision() 메소드 부분~
		session.setReadyStatus();
		startOperation("");
		if(signoffObj != null){
			if(!is_secure_task){
				//SPALMSignoffDecisionOperation 으로 재 정의
				decisionOp = new SSANGYONGSignoffDecisionOperation(this, session, /*desktop,*/ psTask, signoffObj, getDecision(), getComments());
			} 
			else{
				//SPALMSignoffDecisionOperation 으로 재 정의
				decisionOp = new SSANGYONGSignoffDecisionOperation(this, session, /*desktop,*/ psTask, signoffObj, getDecision(), getComments(), getPassword());
				passwordTextField.setText("");
			}
			decisionOp.addOperationListener(this);
			try{
				session.queueOperation(decisionOp);
			}
			catch(Exception exception){
				Debug.printStackTrace("PERFORMSIGNOFFUI", exception);
				MessageBox messagebox = new MessageBox(exception);
				messagebox.setVisible(true);
				return;
			}
		}
		//******************************************* ~원본 commitDecision() 메소드 부분

		//추가 부분 : 반려시 그전 담당자에게 반려 알림 메일이 mail box로 간다.
		//		if(decision == TCCRDecision.REJECT_DECISION){
		//
		//			TCComponent[] recipientByReject = new TCComponent[1];
		//			TCComponent[] attachProcess = new TCComponent[1];
		//
		//			try {
		//				recipientByReject[0] = currentTask.getReferenceProperty("last_mod_user");
		//
		//				attachProcess[0] = currentTask.getProcess();
		//
		//				TCComponentEnvelopeType type = (TCComponentEnvelopeType)session.getTypeComponent("Envelope");
		//
		//				envelope = type.create("반려 알림 도착", "반려 알림 도착 메일", "Envelope");
		//
		//				envelope.addReceivers(recipientByReject);
		//				envelope.addAttachments(attachProcess);
		//
		//				envelope.send();
		//
		//			} catch (TCException e) {
		//				e.printStackTrace();
		//			}
		//		}
	}
}
