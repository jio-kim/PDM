/***********************************************************
 * 파  일  명   : SPALMSignoffDecisionOperation.java
 * 작  성  일   : 2011. 1. 5.
 * Copyright : S-PALM
 * 작  성  자   : 이정건
 *-----------------------------------------------------------
 * 프로그램명   : 코어의 SignoffDecisionOperation 를 상속받아서 재정의
 * 개      요   : 
 ************************************************************/
package com.ssangyong.commands.workflow;

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
	
	// 수정 2024.01.09   
	private TCComponentSignoff signoffObject;

    private String[] propertyArr = { "pk4_cut_design_weight", "pk4_cut_steel_weight",
			"pk4_nut_weight", "pk4_product_design_weight", "pk4_product_steel_weight",
			"pk4_short_weight", "pk4_subm_design_weight", "pk4_subm_steel_weight",
			"pk4_rawmaterial_spec" };
	private Markpoint mp;
 
	// 2024.01.09 수정     super 변경   aifdesktop  -->  decisionDialog
	public SSANGYONGSignoffDecisionOperation(SSANGYONGDecisionDialog decisionDialog, TCSession tcsession,
			/* AIFDesktop aifdesktop, */ TCComponentTask tccomponenttask,
			TCComponentSignoff tccomponentsignoff, TCCRDecision tccrdecision, String s) {
		super(tcsession, decisionDialog, tccomponenttask, tccomponentsignoff, tccrdecision, s);
		performSignoffTask = null;
		session = tcsession;
		performSignoffTask = tccomponenttask;
		decision = tccrdecision;
		
		// 수정 2024.01.09  
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
	 * 승인 완료 후 선택 된 ItemRevision의 타입별로 풍강 ERP Interface
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @throws TCException
	 * @since : 2012. 5. 8.
	 */
	private void erpInterface() throws TCException {
	}

	/**
	 * 승인 완료 후 대상이 제품 리비젼 일때 ERP Code가 동일한 제품 속성 정보 동일 하게 변경 처리.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 11. 1.
	 * @param itemRev
	 */
	private void productERPCodeUpdate(TCComponentItemRevision itemRev) {
		try {
		} catch (Exception e) {
			MessageBox.post(null, "제품 리비젼 ERP Code 동일 대상 속성 변경 중 오류가 발생 하였습니다.", e.getMessage()
					+ " >> " + e.toString(), "알림", MessageBox.INFORMATION, false);
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
	 * 승인후 다음타스크가 릴리즈타스크이고 엑셀의 특정문서일때 결제자 정보 입력
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