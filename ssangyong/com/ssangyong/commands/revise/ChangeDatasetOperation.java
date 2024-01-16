package com.ssangyong.commands.revise;

import java.util.HashMap;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.operation.SYMCAWTAbstractCreateOperation;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.MessageBox;


public class ChangeDatasetOperation extends SYMCAWTAbstractCreateOperation {

	private ChangeDatasetPanel changeDatasetPanel;
	@SuppressWarnings("unused")
	private String revId, partID, partName;
	
	private TCComponentItemRevision targetRevision;
	private TCComponentItemRevision oldRevision;

	public ChangeDatasetOperation(AbstractAIFDialog dialog, String startMessage) {
		super(dialog, startMessage);
	}

	public ChangeDatasetOperation(AbstractAIFDialog dialog, TCSession session, String startMessage) {
		super(dialog, session, startMessage);
	}

	public ChangeDatasetOperation(AbstractAIFDialog dialog) {
		super(dialog);
	}


	public ChangeDatasetOperation(ChangeDatasetDialog dialog, ChangeDatasetPanel changeDatasetPanel) {
		super(dialog);
		this.changeDatasetPanel = changeDatasetPanel;
		this.revId = changeDatasetPanel.revIdField.getText().toString();
		this.partID = changeDatasetPanel.partIDField.getText().toString();
		this.partName = changeDatasetPanel.partNameField.getText().toString();
		this.targetRevision = changeDatasetPanel.getTargetRevision();
		this.oldRevision = changeDatasetPanel.getOldRevision();
	}


	public void executeOperation() throws Exception {
//		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
//		TCComponentItemRevision targetRevision = null;
//		TCComponentItemRevision oldRevision = null;
//
//		if( targetComponents[0] instanceof TCComponentBOMLine){
//			targetRevision = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
//		} else if (targetComponents[0] instanceof TCComponentItemRevision) {
//			targetRevision = (TCComponentItemRevision) targetComponents[0];
//		}
//		oldRevision = CustomUtil.getPreviousRevision(targetRevision);

		boolean is3DCheck = changeDatasetPanel.threeDCheckBox.isSelected() ? true : false;
		boolean is2DCheck = changeDatasetPanel.twoDCheckBox.isSelected() ? true : false;
		boolean isSoftwareCheck = changeDatasetPanel.softwareCheckBox.isSelected() ? true : false;
//		String desc = changeDatasetPanel.partDescArea.getText().toString();
		String stage = changeDatasetPanel.stageBox.getSelectedItem().toString();
		TCComponentItemRevision ecoRev = (TCComponentItemRevision)changeDatasetPanel.ecoNoField.getTcComponent();

//		String tmpStage = "";
//		if(!(stage == null || stage.equals(""))) {
//			tmpStage = stage.substring(0, 1);
//		}

//		if(!(changeDatasetPanel.ecoNoField.getText() == null || changeDatasetPanel.ecoNoField.getText().equals(""))) {
//			ecoRev = (TCComponentItemRevision)changeDatasetPanel.ecoNoField.getTcComponent();
//		}
		
		int response = ConfirmationDialog.post(AIFUtility.getActiveDesktop(), "Confirm", "주의!\n실행시 Revision 하위 모든 Dataset들이 삭제되오니 수정한 2D,3D는 반드시 백업하신 후 진행하시기 바랍니다.\n지금 실행하시겠습니까?");
		if (response == ConfirmationDialog.YES) {
			removeDataset();
			CustomUtil.relateDatasetToItemRevision(oldRevision, targetRevision, is3DCheck, is2DCheck, isSoftwareCheck, ecoRev, true);
			
			//R,C 값 Catia Integration 개발로 인한 변경
			//Dataset Revise인경우 Old Part 속성을 New Part 속성으로 업데이트
			if (oldRevision.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE)){
				HashMap<String, String> rc = new HashMap<String, String>();
				rc.put("s7_R", oldRevision.getProperty("s7_R"));
				rc.put("s7_C", oldRevision.getProperty("s7_C"));
				targetRevision.setProperties(rc);
			}
			MessageBox.post("Normal processing completed.", "Result", MessageBox.INFORMATION);
		}
		

	}
	
	private void removeDataset() throws Exception{
		AIFComponentContext[] contexts =  targetRevision.getChildren();
		TCComponent comp = null;
		for(AIFComponentContext aif : contexts){
			comp = (TCComponent)aif.getComponent();
			if (comp instanceof TCComponentDataset) {
				targetRevision.cutOperation(aif.getContext().toString(), new TCComponent[]{comp});
				
			}
		}
	}

	@Override
	public void createItem() throws Exception {

	}

	@Override 
	public void setProperties() throws Exception {

	}

	@Override
	public void startOperation() throws Exception {
		
	}

	@Override
	public void endOperation() throws Exception {
	}
}
