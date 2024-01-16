package com.ssangyong.commands.revise;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.ssangyong.common.FunctionField;
import com.ssangyong.common.SYMCAWTLabel;
import com.ssangyong.common.SYMCAWTTitledBorder;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.SYMCComboBox;
import com.ssangyong.common.SYMCInterfaceInfoPanel;
import com.ssangyong.common.utils.ComponentService;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

@SuppressWarnings("serial")
public class ChangeDatasetPanel extends JPanel implements SYMCInterfaceInfoPanel, ActionListener
{

	/** 2D CheckBox */
	public JCheckBox twoDCheckBox = new JCheckBox("2D CAD");
	/** 3D CheckBox */
	public JCheckBox threeDCheckBox = new JCheckBox("3D CAD");
	/** SoftWare CheckBox */
	public JCheckBox softwareCheckBox = new JCheckBox("Software");
	/** ECO No. Field */
	public FunctionField ecoNoField = new FunctionField(13, false);
	/** Revision ID Field */
	public FunctionField revIdField = new FunctionField(6, true);
	/** Part ID Field */
	public FunctionField partIDField = new FunctionField(13, true);
	/** Part Name Field */
	public FunctionField partNameField = new FunctionField(13, true);
	/** Stage Field */
	public FunctionField stageField = new FunctionField(3, true);
	/** Description Area */
	/** Stage Combo */
	public SYMCComboBox stageBox;

	private ChangeDatasetDialog dialog;
	private Registry registry;

	private TCComponentItemRevision targetRevision = null;
	
	private TCComponentItemRevision oldRevision = null;

	public ChangeDatasetPanel(JDialog dialog) throws Exception
	{
		this.registry = Registry.getRegistry("com.ssangyong.commands.revise.revise");
		this.dialog = (ChangeDatasetDialog) dialog;
		initUI();
		setItemInfo();
	}

	@SuppressWarnings("unused")
	private void setItemInfo() throws Exception
	{
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComponents[0] instanceof TCComponentBOMLine)
		{
			targetRevision = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
		}
		else if (targetComponents[0] instanceof TCComponentItemRevision)
		{
			targetRevision = (TCComponentItemRevision) targetComponents[0];
		}

		oldRevision = CustomUtil.getPreviousRevision(targetRevision);
		
		String strRevType = targetRevision.getType();
		String strCurrentRevID = targetRevision.getProperty("item_revision_id");
		String strOldRevID = "";
		if(oldRevision != null){
			strOldRevID = oldRevision.getProperty("item_revision_id");
		}
		TCComponent eco = targetRevision.getReferenceProperty("s7_ECO_NO");
		String strECO = "";
		if(eco != null){
			strECO = ((TCComponentItemRevision)eco).getProperty("item_id");
		}
		
		if (SYMCClass.S7_VEHPARTREVISIONTYPE.equals(strRevType)){
			String strStage = targetRevision.getProperty("s7_STAGE");
			/** 저장된 s7_STAGE의 값은 P, D이고, ComboBox의 value는 D (Concept), P (Production) 이므로 ComboBox의 형태에 맞도록 변환한다. */
			String tmpStage = "";
			for (int i = 0; i < stageBox.getItemCount(); i++)
			{
				// stageBox의 0번째 index 값은 빈 값이므로 index를 1 증가시켜 비교한다.
				if (stageBox.getItemAt(i).toString().startsWith(strStage))
				{
					tmpStage = stageBox.getItemAt(i).toString();
					break;
				}
				else if (stageBox.getItemAt(i).toString().startsWith(strStage))
				{
					tmpStage = stageBox.getItemAt(i).toString();
					break;
				}
			}
			stageBox.setSelectedItem(tmpStage);
			stageBox.setEnabled(false);
			ecoNoField.setMandatory(true);
		} else if (SYMCClass.S7_FNCMASTPARTREVISIONTYPE.equals(strRevType)) {
			stageBox.setSelectedItem("P (Production)");
			stageBox.setEnabled(false);
			ecoNoField.setMandatory(true);
		}

		String partID = targetRevision.getProperty("item_id");
		String partName = targetRevision.getProperty("object_name");

		revIdField.setText(strOldRevID + " > " + strCurrentRevID);
		partIDField.setText(partID);
		partNameField.setText(partName);
		ecoNoField.setText(strECO);
		ecoNoField.setTcComponent(eco);
		ecoNoField.setEnabled(false);

	}

	private void initUI()
	{
		setLayout(new VerticalLayout(5, 5, 5, 5, 5));
		setOpaque(false);
		add("top.bind", createItemInfoPanel());
		add("top.bind", createCenterPanel());
		ComponentService.setLabelSize(this, 100, 21);
	}


	private JPanel createItemInfoPanel()
	{
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(new SYMCAWTTitledBorder("Part Info"));
		panel.setOpaque(false);
		partIDField.setEnabled(false);
		partNameField.setEnabled(false);
		revIdField.setEnabled(false);

		panel.add("1.1", new SYMCAWTLabel(registry.getString("ReviseDialog.LABEL.PartID")));
		panel.add("1.2.center.center.resizable.preferred", partIDField);
		panel.add("1.3", new SYMCAWTLabel(registry.getString("ReviseDialog.LABEL.RevisionId")));
		panel.add("1.4.center.center.preferred.preferred", revIdField);
		panel.add("2.1", new SYMCAWTLabel(registry.getString("ReviseDialog.LABEL.PartName")));
		panel.add("2.2.center.center.resizable.preferred", partNameField);
		panel.add("2.3", new SYMCAWTLabel("Stage"));
		panel.add("2.4.center.center.preferred.preferred", createStagePanel());
		return panel;
	}

	private Component createStagePanel()
	{
		JPanel panel = new JPanel(new HorizontalLayout());
		panel.setOpaque(false);

		stageBox = new SYMCComboBox("s7_STAGE", false);
		stageBox.setEditable(false);

		stageField.setEnabled(false);

		panel.add("left.bind", stageBox);
		return panel;
	}

	private Component createCenterPanel()
	{
		JPanel panel = new JPanel(new PropertyLayout());
		panel.setBorder(new SYMCAWTTitledBorder("Dataset Info"));
		panel.setOpaque(false);

		threeDCheckBox.setOpaque(false);
		twoDCheckBox.setOpaque(false);
		softwareCheckBox.setOpaque(false);

		twoDCheckBox.setActionCommand("2DStatusChange");
		twoDCheckBox.addActionListener(this);
		threeDCheckBox.setActionCommand("3DStatusChange");
		threeDCheckBox.addActionListener(this);
		softwareCheckBox.setActionCommand("SoftwareChange");
		softwareCheckBox.addActionListener(this);

		panel.add("1.1.right.center.preferred.preferred", new SYMCAWTLabel("DWG Chg"));
		panel.add("1.2.left.center.preferred.preferred", threeDCheckBox);
		panel.add("1.3.left.center.preferred.preferred", twoDCheckBox);
		panel.add("1.4.left.center.preferred.preferred", softwareCheckBox);
		panel.add("2.1.right.center.preferred.preferred", new SYMCAWTLabel(registry.getString("ReviseDialog.LABEL.ECONo")));
		panel.add("2.2.left.center.resizable.preferred", ecoNoField);
		return panel;
	}


	@Override
	public boolean validCheck(){
		
		String ecoNo = ecoNoField.getText().toString();
		if (ecoNo == null || ecoNo.equals("")) {
			MessageBox.post(dialog, registry.getString("ReviseDialog.MESSAGE.NoInputEcoNo"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return false;
		}
		
		if (oldRevision == null){
			MessageBox.post(dialog, "이전 Revision을 찾을 수 없습니다.", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return false;
		}

		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals("3DStatusChange"))
		{
			if (threeDCheckBox.isSelected())
			{
				twoDCheckBox.setSelected(true);
			}
		}
	}
	
	public TCComponentItemRevision getTargetRevision(){
		return targetRevision;
	}
	
	public TCComponentItemRevision getOldRevision(){
		return oldRevision;
	}
}
