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
 * 1. E-BOM Part Master(Eng. Info) �� "Responsibility" => "DWG Creator" �� ����
   2. Responsibility Filed �� LOV �� �߰� : Supplier, Collaboration, SYMC
   3. �ű� part ���� �� ���� LOV Black BOX, Gray Box, White Box ���úҰ� ó��
   4. Revision Up �� ���� Responsibiliy �� ���� => ���� �������ϵ��� ó��
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
	 * ������
	 * @copyright : S-PALM
	 * @author : �ǿ���
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
	 * ���� �� ����Ÿ�� ���̱�, ���� ����Ÿ�� ����
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǿ���
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

		//[SR180130-033][LJG] Revision Up �� ���� Responsibiliy �� ���� => ���� �������ϵ��� ó��
		//[20200521][CSH]Revise Description�� change_description���� ����ǵ��� ����. Name spec���� �Է��� �ȵǵ���. ������� �̺��� å�� ��û
//		TCComponentItemRevision newRevision = CustomUtil.reviseForItemRev(targetRevision, is3DCheck, is2DCheck, isSoftwareCheck, false, desc, tmpStage, ecoRev);
		TCComponentItemRevision newRevision = CustomUtil.reviseForItemRev(targetRevision, is3DCheck, is2DCheck, isSoftwareCheck, false, "", tmpStage, ecoRev);
		// 20231207 seho �̺��� å�� ��û���� vehicle part�� ��쿡�� object_desc�� ���� ���� �ʰ� s7_CHANGE_DESCRIPTION ���� ���� �ִ´�.
		// vehicle part�� ��� object desc�� name spec��� �׸����� ������̴�. ������ �������� ���� �ȵȴ�.
		// saveas�ÿ� object desc�� �������� �־��ָ� �������� �ڵ����� ����ȴ�.
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
	 * Dataset ���� �� ����� Part�� Rev Id�� ECO No�� Dataset �Ӽ��� Update �Ѵ�.
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

		/** ECO No�� Dataset�� �����Ѵ�. */
		revDataSet.setProperty("s7_ECO_NO", ecoNo);
		/** Part Revision�� Rev ID ���� Dataset�� �����Ѵ�. */
		revDataSet.setProperty("s7_REVISION_ID", newRevId);

		return revDataSet;
	}

	/**
	 * DataSetName / DataSetRevisionID - ParentItemRevisionID
	 * @Copyright : S-PALM
	 * @author : �ǿ���
	 * @since : 2012. 12. 20.
	 * @param dataSetName
	 * @return
	 * @throws Exception 
	 */
	private String getDataSetName(String dataSetName) throws Exception {
		if(dataSetName.indexOf("/") == -1) {
			//			String errMsg = "Dataset�� Name������ �߸��Ǿ����ϴ�. �����ڿ��� �����ϼ���.!!";
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
	 * Dataset Rev. ID�� ��ȯ
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
