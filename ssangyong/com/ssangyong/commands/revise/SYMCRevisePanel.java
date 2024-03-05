package com.ssangyong.commands.revise;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.ssangyong.commands.partmaster.vehiclepart.VehiclePartMasterInfoPanel;
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
import com.teamcenter.rac.common.lov.LOVComboBox;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * class comments �߰�. 
 * [SR140616-019][20140619][bskwak] ������ ǰ�� Concept �ܰ� ǰ������ ���� ���� : Stage ���� ���� ��� �⺻ revision �ʱⰪ ����ϴ� ������ ���� ��.
 * @author bs
 * 
 * [SR180130-033][LJG]
 * 1. E-BOM Part Master(Eng. Info) �� "Responsibility" => "DWG Creator" �� ����
   2. Responsibility Filed �� LOV �� �߰� : Supplier, Collaboration, SYMC
   3. �ű� part ���� �� ���� LOV Black BOX, Gray Box, White Box ���úҰ� ó��
   4. Revision Up �� ���� Responsibiliy �� ���� => ���� �������ϵ��� ó��
 *
 */
@SuppressWarnings("serial")
public class SYMCRevisePanel extends JPanel implements SYMCInterfaceInfoPanel, ActionListener
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
	public FunctionField revIdField = new FunctionField(3, true);
	/** Part ID Field */
	public FunctionField partIDField = new FunctionField(13, true);
	/** Part Name Field */
	public FunctionField partNameField = new FunctionField(13, true);
	/** Stage Field */
	public FunctionField stageField = new FunctionField(3, true);
	/** Description Area */
	public JTextArea partDescArea = new JTextArea();
	/** Stage Combo */
	public SYMCComboBox stageBox;

	/** DWG Creator */
	public LOVComboBox dwgCreatorCombo; //[SR180130-033][LJG]�߰�

	private SYMCReviseDialog dialog;
	private Registry registry;

	private TCComponentItemRevision targetComponent = null;

	/**
	 * ������
	 * 
	 * @copyright : S-PALM
	 * @author : �ǿ���
	 * @since : 2012. 12. 20.
	 * @param dialog
	 * @throws Exception
	 */
	public SYMCRevisePanel(JDialog dialog) throws Exception
	{
		this.registry = Registry.getRegistry("com.ssangyong.commands.revise.revise");
		this.dialog = (SYMCReviseDialog) dialog;
		initUI();
		setItemInfo();
	}

	/**
	 * ������ �����۸������� ������ ȭ�鿡 ����
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǿ���
	 * @throws Exception
	 * @since : 2012. 12. 20.
	 */
	@SuppressWarnings("unused")
	private void setItemInfo() throws Exception
	{
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComponents[0] instanceof TCComponentBOMLine)
		{
			targetComponent = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
		}
		else if (targetComponents[0] instanceof TCComponentItemRevision)
		{
			targetComponent = (TCComponentItemRevision) targetComponents[0];
		}

		String strRevType = targetComponent.getType();
		String strCurrentRevID = targetComponent.getProperty("item_revision_id");
		String strNextRevID = "";
		if (SYMCClass.S7_VEHPARTREVISIONTYPE.equals(strRevType))
		{
			String strStage = targetComponent.getProperty("s7_STAGE");
			/** ����� s7_STAGE�� ���� P, D�̰�, ComboBox�� value�� D (Concept), P (Production) �̹Ƿ� ComboBox�� ���¿� �µ��� ��ȯ�Ѵ�. */
			String tmpStage = "";
			for (int i = 0; i < stageBox.getItemCount(); i++)
			{
				// stageBox�� 0��° index ���� �� ���̹Ƿ� index�� 1 �������� ���Ѵ�.
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

			// Stage ���� 'P'�� ��� Revision ID�� ���� 3�ڸ�(001->999)
			if ("P".equals(strStage))
			{
				stageBox.setEnabled(false);
				strNextRevID = CustomUtil.getNextRevID(targetComponent.getItem(), new String("Item"));
				ecoNoField.setMandatory(true);
			}
			// Stage ���� 'P'�� �ƴ� ��� Revision ID�� ���� 2�ڸ�(A->ZZ)
			// [SR140616-019][20140619][bskwak] ������ ǰ�� Concept �ܰ� ǰ������ ���� ���� : Stage ���� ���� ��� �⺻ revision �ʱⰪ ����ϴ� ������ ���� ��.
			//        => ���� Rev�� �ƴ� ���� rev�� ����ϴ� logic���� ��ü. 
			else
			{
				//strNextRevID = CustomUtil.getNextCustomRevID(strCurrentRevID);
				strNextRevID = CustomUtil.getNextRevID(targetComponent.getItem(), new String("Item"));
			}
		}
		else if (SYMCClass.S7_FNCMASTPARTREVISIONTYPE.equals(strRevType))
		{
			stageBox.setSelectedItem("P (Production)");
			stageBox.setEnabled(false);
			strNextRevID = CustomUtil.getNextRevID(targetComponent.getItem(), new String("Item"));
			ecoNoField.setMandatory(true);
		}
		else
		{
			strNextRevID = CustomUtil.getNextRevID(targetComponent.getItem(), new String("Item"));

		}

		String partID = targetComponent.getProperty("item_id");
		String partName = targetComponent.getProperty("object_name");
		revIdField.setText(strNextRevID);
		partIDField.setText(partID);
		partNameField.setText(partName);
		ecoNoField.setEnabled(false);

		//20231207 seho ȭ�� �ε� �ɶ� ���� �������� desc�� �����ͼ� �Է���.
		String descString = "";
		TCProperty cdProperty = targetComponent.getTCProperty("s7_CHANGE_DESCRIPTION");
		if(cdProperty == null)
		{
			descString = targetComponent.getProperty("object_desc");
		}else
		{
			descString = targetComponent.getProperty("s7_CHANGE_DESCRIPTION");
		}
		partDescArea.setText(descString);
	}

	/**
	 * ȭ�� ����
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǿ���
	 * @since : 2012. 12. 20.
	 */
	private void initUI()
	{
		setLayout(new VerticalLayout(5, 5, 5, 5, 5));
		setOpaque(false);
		add("top.bind", createItemInfoPanel());
		add("top.bind", createCenterPanel());
		add("unbound.bind", createBottomPanel());
		ComponentService.setLabelSize(this, 100, 21);
	}

	/**
	 * ��Ʈ ������ ���̵� �� �̸�
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǿ���
	 * @since : 2012. 12. 20.
	 * @return
	 */
	private JPanel createItemInfoPanel()
	{
		JPanel panel = new JPanel(new PropertyLayout());
		//    panel.setBorder(new SYMCAWTTitledBorder("��Ʈ ������ ����"));
		panel.setBorder(new SYMCAWTTitledBorder(registry.getString("ReviseDialog.TITLE")));
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
		panel.setBorder(new SYMCAWTTitledBorder(""));
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

		/** ECO No �˻� ��ư */
		JButton ecoNoSearchButton = new JButton(registry.getImageIcon("Search.ICON"));
		ecoNoSearchButton.setPreferredSize(new Dimension(20, 20));
		ecoNoSearchButton.setActionCommand("ECONoSearch");
		ecoNoSearchButton.addActionListener(this);
		/** End */

		panel.add("1.1.right.center.preferred.preferred", new SYMCAWTLabel("DWG Chg"));
		panel.add("1.2.left.center.preferred.preferred", threeDCheckBox);
		panel.add("1.3.left.center.preferred.preferred", twoDCheckBox);
		panel.add("1.4.left.center.preferred.preferred", softwareCheckBox);
		panel.add("2.1.right.center.preferred.preferred", new SYMCAWTLabel(registry.getString("ReviseDialog.LABEL.ECONo")));
		panel.add("2.2.left.center.resizable.preferred", ecoNoField);
		panel.add("2.3.left.center.preferred.preferred", ecoNoSearchButton);

		//[SR180130-033][LJG] �߰�
		if(isVisibleDwgCreator()){
			dwgCreatorCombo = new LOVComboBox();
			dwgCreatorCombo.setMandatory(true);
			dwgCreatorCombo.setEditable(false);
			TCComponentListOfValues lov = TCComponentListOfValuesType.findLOVByName("S7_RESPONSIBILITY");
			String[] str = null;
			try {
				str = lov.getListOfValues().getStringListOfValues();
				for(int i=0; i<str.length; i++){
					if(!str[i].startsWith("White Box") && !str[i].startsWith("Black Box") && !str[i].startsWith("Gray Box") && !str[i].startsWith("SYMC")){
						dwgCreatorCombo.addItem(str[i]);
					}
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
			panel.add("3.1.right.center.preferred.preferred", new SYMCAWTLabel("DWG Creator"));
			panel.add("3.2.left.center.preferred.preferred", dwgCreatorCombo);
		}
		return panel;
	}

	private boolean isVisibleDwgCreator(){
		try {
			InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComponents[0] instanceof TCComponentBOMLine)
			{
				targetComponent = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
			}
			else if (targetComponents[0] instanceof TCComponentItemRevision)
			{
				targetComponent = (TCComponentItemRevision) targetComponents[0];
			}

			String	s7_RESPONSIBILITY = targetComponent.getStringProperty("s7_RESPONSIBILITY");
			if(!CustomUtil.isNullString(s7_RESPONSIBILITY)){
				if(s7_RESPONSIBILITY.equalsIgnoreCase("White Box") || s7_RESPONSIBILITY.equalsIgnoreCase("Black Box") || s7_RESPONSIBILITY.equalsIgnoreCase("Gray Box")){
					return true;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * ����, ECO No
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǿ���
	 * @since : 2012. 12. 20.
	 * @return
	 */
	private JPanel createBottomPanel()
	{
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(new SYMCAWTTitledBorder(registry.getString("ReviseDialog.LABEL.Desc")));
		panel.setOpaque(false);
		partDescArea = new JTextArea(5, 39);
		partDescArea.setLineWrap(true);
		JScrollPane descScrollPane = new JScrollPane(partDescArea);
		descScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		//panel.add("top.bind", new SYMCAWTLabel(registry.getString("ReviseDialog.LABEL.Desc")));
		panel.add("unbound.bind", descScrollPane);

		return panel;
	}

	/**
	 * Validation üũ 2D CAD üũ���¸� 3D CAD ���� üũ �����̾�� �Ѵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǿ���
	 * @since : 2012. 12. 20.
	 * @override
	 * @see com.ssangyong.common.SYMCInterfaceInfoPanel#validCheck()
	 * @return
	 */
	@Override
	public boolean validCheck()
	{
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		TCComponentItemRevision targetRevision = null;

		try
		{
			if (targetComponents[0] instanceof TCComponentBOMLine)
			{
				targetRevision = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
			}
			else if (targetComponents[0] instanceof TCComponentItemRevision)
			{
				targetRevision = (TCComponentItemRevision) targetComponents[0];
			}

			String ecoNo = ecoNoField.getText().toString();
			// Stage ���� P�̸� ECO No�� �ʼ����̴�.
			if (stageBox.getSelectedItem().toString().startsWith("P"))
			{
				if (ecoNo == null || ecoNo.equals(""))
				{
					MessageBox.post(dialog, registry.getString("ReviseDialog.MESSAGE.NoInputEcoNo"), registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
					return false;
				}
			}

			//[SR180130-033][LJG]�߰�
			if(dwgCreatorCombo != null){
				if(CustomUtil.isNullString(dwgCreatorCombo.getSelectedItem().toString())){
					MessageBox.post(dialog, "DWG Creator Not Input", registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
					return false;
				}
			}

			String msg = CustomUtil.validateRevise(targetRevision, ecoNo).toString();
			if (!msg.equals(""))
			{
				MessageBox.post(dialog, msg, registry.getString("ReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
				return false;
			}
		}
		catch (TCException e)
		{
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals("ECONoSearch"))
		{

			//    	PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getDisplay().syncExec(new Runnable() {
			//    		public void run() {
			//    			ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), SWT.SINGLE);
			//    			ecoSearchDialog.getShell().setText("ECO Search");
			//    			ecoSearchDialog.setAllMaturityButtonsEnabled(false);
			//    			ecoSearchDialog.setBInProcessSelect(false);
			//    			ecoSearchDialog.setBCompleteSelect(false);
			//    			ecoSearchDialog.open();
			//    		}
			//    	});

			SYMCReviseSearchDialog searchDialog = new SYMCReviseSearchDialog(registry.getString("ReviseDialog.TITLE.ECOSearch"), registry.getString("ReviseDialog.MESSAGE_TITLE.ECOSearch"),
					new String[] { "item_id", "object_name", "owning_user" }, ecoNoField);
			searchDialog.run();

		}
		/* 3D �� Check �Ǿ� ������ 2D�� �ڵ� Check �ǵ��� �Ѵ�. */
		else if (actionCommand.equals("3DStatusChange"))
		{
			if (threeDCheckBox.isSelected())
			{
				twoDCheckBox.setSelected(true);
			}
		}
	}
}
