/***********************************************************
 * ��  ��  ��   : SPALMSignoffDecisionOperation.java
 * ��  ��  ��   : 2011. 1. 5.
 * Copyright : S-PALM
 * ��  ��  ��   : ������
 *-----------------------------------------------------------
 * ���α׷���   : �ھ��� SignoffDecisionOperation �� ��ӹ޾Ƽ� ������
 * ��      ��   : 
 ************************************************************/
package com.kgm.commands.workflow;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.workflow.commands.newperformsignoff.SignoffDecisionOperation;

@SuppressWarnings("unused")
public class SSANGYONGSignoffDecisionOperation extends SignoffDecisionOperation {
	private TCCRDecision decision;
	private TCComponentTask performSignoffTask;
	private TCSession session;
	private SSANGYONGDecisionDialog decisionDialog;
	
	// ���� 2024.01.09   
	private TCComponentSignoff signoffObject;

    private String[] propertyArr = { "pk4_cut_design_weight", "pk4_cut_steel_weight",
			"pk4_nut_weight", "pk4_product_design_weight", "pk4_product_steel_weight",
			"pk4_short_weight", "pk4_subm_design_weight", "pk4_subm_steel_weight",
			"pk4_rawmaterial_spec" };
	private Markpoint mp;
 
	// 2024.01.09 ����     super ����   aifdesktop  -->  decisionDialog
	public SSANGYONGSignoffDecisionOperation(SSANGYONGDecisionDialog decisionDialog, TCSession tcsession,
			/* AIFDesktop aifdesktop, */ TCComponentTask tccomponenttask,
			TCComponentSignoff tccomponentsignoff, TCCRDecision tccrdecision, String s) {
		super(tcsession, decisionDialog, tccomponenttask, tccomponentsignoff, tccrdecision, s);
		performSignoffTask = null;
		session = tcsession;
		performSignoffTask = tccomponenttask;
		decision = tccrdecision;
		
		// ���� 2024.01.09  
		signoffObject = tccomponentsignoff;
		
		this.decisionDialog = decisionDialog;

	}

	public SSANGYONGSignoffDecisionOperation(SSANGYONGDecisionDialog decisionDialog, TCSession tcsession,
			/* AIFDesktop aifdesktop, */ TCComponentTask tccomponenttask,
			TCComponentSignoff tccomponentsignoff, TCCRDecision tccrdecision, String s, String s1) {
		this(decisionDialog, tcsession, /* aifdesktop, */ tccomponenttask, tccomponentsignoff,
				tccrdecision, s);
		this.decisionDialog = decisionDialog;
	}

	@Override
	public void executeOperation() {
		try {
			
			// TCCRDecision.APPROVE_DECISION  ->    TCComponentSignoff.getApproveDecision() 
			if (decision == signoffObject.getApproveDecision()) {
				TCComponentTask parent = performSignoffTask.getParent();
				TCComponent[] successors = parent.getTCProperty("successors").getReferenceValueArray();
				for (int i = 0; i < successors.length; i++) {
					if (successors[i].getType().equals("EPMAddStatusTask")) {
						workflowTemplateSignOperation();
						erpInterface();
					}
				}
			}
			super.executeOperation();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				decisionDialog.setVisible(false);
				decisionDialog.disposeDialog();
				session.getUser().getUserInBox().refresh();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * ���� �Ϸ� �� ���� �� ItemRevision�� Ÿ�Ժ��� ǳ�� ERP Interface
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @throws TCException
	 * @since : 2012. 5. 8.
	 */
	private void erpInterface() throws TCException {
	}

	/**
	 * ���� �Ϸ� �� ����� ��ǰ ������ �϶� ERP Code�� ������ ��ǰ �Ӽ� ���� ���� �ϰ� ���� ó��.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2012. 11. 1.
	 * @param itemRev
	 */
	private void productERPCodeUpdate(TCComponentItemRevision itemRev) {
		try {
		} catch (Exception e) {
			MessageBox.post(null, "��ǰ ������ ERP Code ���� ��� �Ӽ� ���� �� ������ �߻� �Ͽ����ϴ�.", e.getMessage()
					+ " >> " + e.toString(), "�˸�", MessageBox.INFORMATION, false);
			e.printStackTrace();
		} finally {
			if (mp != null) {
				try {
					mp.rollBack();
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * ������ ����Ÿ��ũ�� ������Ÿ��ũ�̰� ������ Ư�������϶� ������ ���� �Է�
	 * 
	 * @throws Exception
	 */
	private void workflowTemplateSignOperation() throws Exception {
		TCComponentTaskTemplate processName = performSignoffTask.getParent().getProcessDefinition();
		AIFComponentContext[] context = performSignoffTask.getRelated();
		TCComponent component = null;
		for (int j = 0; j < context.length; j++) {
			component = (TCComponent) context[j].getComponent();
		}
	}
}