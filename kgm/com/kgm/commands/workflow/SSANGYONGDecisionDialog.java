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
 * �ھ��� DecisionDialog �� ��ӹ޾Ƽ� ������
 * ���� �Ǵ� �ݷ��ÿ� �ּ� �ʼ���.
 * �ݷ��� ���� ����ڿ��� �ݷ� �˸� ������ mail box�� ����.
 * @Copyright : S-PALM
 * @author   : ������
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
		// 2024.01.09 ���� TCCRDecision.REJECT_DECISION -->  signoffObj.getRejectDecision()
		try {
			if(decision == signoffObj.getRejectDecision()){
				if(getComments() == null || getComments().equals("")){
					MessageBox.post("�ּ��� �����Ǿ� �ֽ��ϴ�.", "�˸�", MessageBox.INFORMATION);
					return;
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setEditable(false);

		//********************************* ���� commitDecision() �޼ҵ� �κ�~
		session.setReadyStatus();
		startOperation("");
		if(signoffObj != null){
			if(!is_secure_task){
				//SPALMSignoffDecisionOperation ���� �� ����
				decisionOp = new SSANGYONGSignoffDecisionOperation(this, session, /*desktop,*/ psTask, signoffObj, getDecision(), getComments());
			} 
			else{
				//SPALMSignoffDecisionOperation ���� �� ����
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
		//******************************************* ~���� commitDecision() �޼ҵ� �κ�

		//�߰� �κ� : �ݷ��� ���� ����ڿ��� �ݷ� �˸� ������ mail box�� ����.
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
		//				envelope = type.create("�ݷ� �˸� ����", "�ݷ� �˸� ���� ����", "Envelope");
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
