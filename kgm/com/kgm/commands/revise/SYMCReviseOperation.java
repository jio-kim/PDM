package com.kgm.commands.revise;

import java.util.Vector;

import com.kgm.common.SYMCClass;
import com.kgm.common.operation.SYMCAWTAbstractCreateOperation;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR180130-033][LJG]
 * 1. E-BOM Part Master(Eng. Info) 중 "Responsibility" => "DWG Creator" 로 변경
   2. Responsibility Filed 내 LOV 값 추가 : Supplier, Collaboration, SYMC
   3. 신규 part 생성 시 기존 LOV Black BOX, Gray Box, White Box 선택불가 처리
   4. Revision Up 시 기존 Responsibiliy 값 삭제 => 설계 재지정하도록 처리
 */
public class SYMCReviseOperation extends SYMCAWTAbstractCreateOperation {

	private SYMCRevisePanel sYRevisePanel;
	@SuppressWarnings("unused")
	private String revId, partID, partName;

	public SYMCReviseOperation(AbstractAIFDialog dialog, String startMessage) {
		super(dialog, startMessage);
	}

	public SYMCReviseOperation(AbstractAIFDialog dialog, TCSession session, String startMessage) {
		super(dialog, session, startMessage);
	}

	public SYMCReviseOperation(AbstractAIFDialog dialog) {
		super(dialog);
	}

	/**
	 * 생성자
	 * @copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2012. 12. 20.
	 * @param dialog
	 * @param sYRevisePanel
	 */
	public SYMCReviseOperation(SYMCReviseDialog dialog, SYMCRevisePanel sYRevisePanel) {
		super(dialog);
		this.sYRevisePanel = sYRevisePanel;
		this.revId = sYRevisePanel.revIdField.getText().toString();
		this.partID = sYRevisePanel.partIDField.getText().toString();
		this.partName = sYRevisePanel.partNameField.getText().toString();
	}

	/**
	 * 개정 및 데이타셋 붙이기, 도면 데이타셋 개정
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2012. 12. 20.
	 * @override
	 * @see com.kgm.common.operation.SYMCAWTAbstractCreateOperation#executeOperation()
	 * @throws Exception
	 */
	public void executeOperation() throws Exception {
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		TCComponentItemRevision targetRevision = null;

		if( targetComponents[0] instanceof TCComponentBOMLine){
			targetRevision = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
		} else if (targetComponents[0] instanceof TCComponentItemRevision) {
			targetRevision = (TCComponentItemRevision) targetComponents[0];
		}

		boolean is3DCheck = sYRevisePanel.threeDCheckBox.isSelected() ? true : false;
		boolean is2DCheck = sYRevisePanel.twoDCheckBox.isSelected() ? true : false;
		boolean isSoftwareCheck = sYRevisePanel.softwareCheckBox.isSelected() ? true : false;
		String desc = sYRevisePanel.partDescArea.getText().toString();
		String stage = sYRevisePanel.stageBox.getSelectedItem().toString();
		TCComponentItemRevision ecoRev = null;

		String tmpStage = "";
		if(!(stage == null || stage.equals(""))) {
			tmpStage = stage.substring(0, 1);
		}

		if(!(sYRevisePanel.ecoNoField.getText() == null || sYRevisePanel.ecoNoField.getText().equals(""))) {
			ecoRev = (TCComponentItemRevision)sYRevisePanel.ecoNoField.getTcComponent();
		}

		//[SR180130-033][LJG] Revision Up 시 기존 Responsibiliy 값 삭제 => 설계 재지정하도록 처리
		//[20200521][CSH]Revise Description이 change_description으로 저장되도록 변경. Name spec에는 입력이 안되도록. 기술관리 이보현 책임 요청
//		TCComponentItemRevision newRevision = CustomUtil.reviseForItemRev(targetRevision, is3DCheck, is2DCheck, isSoftwareCheck, false, desc, tmpStage, ecoRev);
		TCComponentItemRevision newRevision = CustomUtil.reviseForItemRev(targetRevision, is3DCheck, is2DCheck, isSoftwareCheck, false, "", tmpStage, ecoRev);
		// 20231207 seho 이보현 책임 요청으로 vehicle part의 경우에는 object_desc의 값은 넣지 않고 s7_CHANGE_DESCRIPTION 에만 값을 넣는다.
		// vehicle part의 경우 object desc를 name spec라는 항목으로 사용중이다. 강제로 공백으로 만들어도 안된다.
		// saveas시에 object desc를 공백으로 넣어주면 이전값이 자동으로 저장된다.
        if (newRevision.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE))
        {
        	newRevision.setProperty("s7_CHANGE_DESCRIPTION", desc);
		}else
		{
			newRevision.setProperty("object_desc", desc);
		}
		if(sYRevisePanel.dwgCreatorCombo != null){
			newRevision.refresh();
			newRevision.setStringProperty("s7_RESPONSIBILITY", sYRevisePanel.dwgCreatorCombo.getSelectedString());
			newRevision.refresh();
		}
		newComp = newRevision.getItem();
	}

	/**
	 * Dataset 생성 후 연결되 Part의 Rev Id와 ECO No를 Dataset 속성에 Update 한다.
	 * @param relatedComponent
	 * @param ecoNo
	 * @param newRevId
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	public TCComponentDataset updateDataSetForEcoNoAndNewRev(TCComponent relatedComponent, String ecoNo, String newRevId) throws Exception, Exception {
		String dataSetName = getDataSetName(relatedComponent.getProperty("object_string"));
		TCComponentDataset revDataSet = ((TCComponentDataset) relatedComponent).saveAs(dataSetName);

		/** ECO No를 Dataset에 저장한다. */
		revDataSet.setProperty("s7_ECO_NO", ecoNo);
		/** Part Revision의 Rev ID 값을 Dataset에 저장한다. */
		revDataSet.setProperty("s7_REVISION_ID", newRevId);

		return revDataSet;
	}

	/**
	 * DataSetName / DataSetRevisionID - ParentItemRevisionID
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2012. 12. 20.
	 * @param dataSetName
	 * @return
	 * @throws Exception 
	 */
	private String getDataSetName(String dataSetName) throws Exception {
		if(dataSetName.indexOf("/") == -1) {
			//			String errMsg = "Dataset의 Name형식이 잘못되었습니다. 관리자에게 문의하세요.!!";
			Registry reg = Registry.getRegistry(this);
			String errMsg = reg.getString("ReviseDialog.MESSAGE.WrongDSName");
			MessageBox.post(errMsg, "Error", MessageBox.ERROR);
			throw new Exception(errMsg);
		}
		String newDatasetName = dataSetName.substring(0, dataSetName.indexOf("/"));
		if (dataSetName.contains("/") && dataSetName.contains("-")) {
			String dataSetRevId = dataSetName.substring(dataSetName.lastIndexOf("-") + 1, dataSetName.length());
			dataSetName = newDatasetName + "/" + revId + "-" + getNextDataSetRevID(dataSetRevId);
		} else if(!dataSetName.contains("-")){
			dataSetName = newDatasetName + "/" + revId + "-" + "A";
		}
		return dataSetName;
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

	/**
	 * Dataset Rev. ID를 반환
	 * 
	 * ''=>A-Z=>AA-ZZ
	 * 
	 * @param revId : Dataset Rev. ID
	 * @return
	 */
	private String getNextDataSetRevID(String revId) {
		Vector<String> temp = new Vector<String>();
		String nextRevId = "";
		boolean next = true;

		for (int i = revId.length(); i > 0; i--) {
			if (revId.charAt(i - 1) != 'Z') {
				if (next) {
					int intchar = revId.charAt(i - 1);
					intchar++;
					char[] char1 = Character.toChars(intchar);
					if (revId.length() > 1) {
						temp.add(String.valueOf(char1[0]));
						next = false;
					} else {
						temp.add(String.valueOf(char1[0]));
					}
				} else {
					temp.add(String.valueOf(revId.charAt(i - 1)));
					next = false;
				}
			} else if (revId.charAt(i - 1) == 'Z') {
				if (next) {
					if (i == 1) {
						temp.add("AA");
					} else {
						temp.add("A");
						next = true;;
					}
				} else {
					temp.add("Z");
				}
			} else {
				return revId;
			}
		}

		for (int i = temp.size(); i > 0 ; i--) {
			nextRevId += temp.get(i - 1);
		}
		return nextRevId;
	}

}
