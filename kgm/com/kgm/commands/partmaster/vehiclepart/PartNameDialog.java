package com.kgm.commands.partmaster.vehiclepart;

import java.util.HashMap;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import swing2swt.layout.FlowLayout;

import com.kgm.common.SYMCLabel;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMDisplayUtil;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.controls.LOVComboBox;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * ���� ǰ���� ��� PartName�� KeyIn �Ұ�, Name LOV���� �������� �������
 * [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����. 
 * [SR140409-033][20140513] bskwak, Part Name ���ڿ� ��ȯ ��ɿ� " (and) " -> "&" �� ��ȯ�ϴ� ��� �߰�. 
 */
public class PartNameDialog extends Dialog
{

	/** Action Type */
	private boolean action = false;
	private Object result;
	private Shell shell;
	/** OK Button */
	private Button okButton;
	/** */
	private HashMap<String, Object> attrMap;

	/** Main Name */
	LOVComboBox mainCB;
	/** Sub Name */
	LOVComboBox subCB;
	/** Loc1 Name */
	LOVComboBox loc1CB;
	/** Loc2 Name */
	LOVComboBox loc2CB;
	/** Loc3 Name */
	LOVComboBox loc3CB;
	/** Loc4 Name */
	LOVComboBox loc4CB;
	/** Loc5 Name*/
	LOVComboBox loc5CB;
	/** TC Registry*/
	private Registry registry;

	private boolean isDRCheck = true; // DR �� üũ�� �� Ȯ��
	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public PartNameDialog(Shell parent, int _selection, HashMap<String, Object> attrMap)
	{
		super(parent);

		registry = Registry.getRegistry("com.kgm.common.common");
		setText("Part Name Create");
		this.attrMap = attrMap;

	}
	
	/**
	 * 
	 * @param parent
	 * @param _selection
	 * @param attrMap
	 * @param isDRCheck DR üũ ����
	 */
	public PartNameDialog(Shell parent, int _selection, HashMap<String, Object> attrMap, boolean isDRCheck)
	{
		this(parent, _selection, attrMap);
		this.isDRCheck = isDRCheck;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open()
	{
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents()
	{
		shell = new Shell(getParent(), SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
		shell.setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
		shell.setSize(420, 300);
		shell.setBackground(new Color(null, 255, 255, 255));
		SYMDisplayUtil.centerToParent(getParent().getShell(), shell);
		shell.setText(getText());

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = 10;
		formLayout.marginBottom = 10;
		formLayout.marginLeft = 5;
		formLayout.marginRight = 5;

		shell.setLayout(formLayout);

		FormData labelFormData = new FormData(150, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(0);
		labelFormData.left = new FormAttachment(0);

		SYMCLabel mainLabel = new SYMCLabel(shell, "Main Name", labelFormData);
		FormData comboFormData = new FormData(200, 20);
		comboFormData.top = new FormAttachment(0);
		comboFormData.left = new FormAttachment(mainLabel, 5);

//		mainCB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, CustomUtil.getTCSession(), "S7_MAIN_NAME");
		mainCB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, "S7_MAIN_NAME");
		// mainCB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, CustomUtil.getTCSession(), "S7_REGULAR_PART");

		mainCB.setLayoutData(comboFormData);

		labelFormData = new FormData(150, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(mainCB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel subLabel = new SYMCLabel(shell, "Sub Name", labelFormData);
		comboFormData = new FormData(200, SWT.DEFAULT);
		comboFormData.top = new FormAttachment(mainCB, 5);
		comboFormData.left = new FormAttachment(subLabel, 5);
//		subCB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, CustomUtil.getTCSession(), "S7_SUBNAME");
		subCB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, "S7_SUBNAME");
		
		subCB.setLayoutData(comboFormData);

		labelFormData = new FormData(150, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(subCB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel loc1Label = new SYMCLabel(shell, "Loc1(FR/RR)", labelFormData);
		comboFormData = new FormData(200, SWT.DEFAULT);
		comboFormData.top = new FormAttachment(subCB, 5);
		comboFormData.left = new FormAttachment(loc1Label, 5);
//		loc1CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, CustomUtil.getTCSession(), "S7_LOC_1");
		loc1CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, "S7_LOC_1");
		loc1CB.setLayoutData(comboFormData);

		labelFormData = new FormData(150, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(loc1CB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel loc2Label = new SYMCLabel(shell, "Loc2(INR/OTR)", labelFormData);
		comboFormData = new FormData(200, SWT.DEFAULT);
		comboFormData.top = new FormAttachment(loc1CB, 5);
		comboFormData.left = new FormAttachment(loc2Label, 5);
//		loc2CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, CustomUtil.getTCSession(), "S7_LOC_2");
		loc2CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, "S7_LOC_2");
		loc2CB.setLayoutData(comboFormData);

		labelFormData = new FormData(150, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(loc2CB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel loc3Label = new SYMCLabel(shell, "Loc3(UPR/LWR)", labelFormData);
		comboFormData = new FormData(200, SWT.DEFAULT);
		comboFormData.top = new FormAttachment(loc2CB, 5);
		comboFormData.left = new FormAttachment(loc3Label, 5);
//		loc3CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, CustomUtil.getTCSession(), "S7_LOC_3");
		loc3CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, "S7_LOC_3");
		loc3CB.setLayoutData(comboFormData);

		labelFormData = new FormData(150, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(loc3CB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel loc4Label = new SYMCLabel(shell, "Loc4(INLET/OUTLET)", labelFormData);
		comboFormData = new FormData(200, SWT.DEFAULT);
		comboFormData.top = new FormAttachment(loc3CB, 5);
		comboFormData.left = new FormAttachment(loc4Label, 5);
//		loc4CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, CustomUtil.getTCSession(), "S7_LOC_4");
		loc4CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, "S7_LOC_4");
		loc4CB.setLayoutData(comboFormData);

		labelFormData = new FormData(150, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(loc4CB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel loc5Label = new SYMCLabel(shell, "Loc5(LH/RH)", labelFormData);
		comboFormData = new FormData(200, SWT.DEFAULT);
		comboFormData.top = new FormAttachment(loc4CB, 5);
		comboFormData.left = new FormAttachment(loc5Label, 5);
//		loc5CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, CustomUtil.getTCSession(), "S7_LOC_5");
		loc5CB = new LOVComboBox(shell, SWT.SCROLL_LOCK | SWT.BORDER, "S7_LOC_5");
		loc5CB.setLayoutData(comboFormData);

		labelFormData = new FormData(370, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(loc5CB, 5);
		labelFormData.left = new FormAttachment(0, 10);
		Label lSeparator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
		lSeparator.setLayoutData(labelFormData);

		labelFormData = new FormData(370, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(lSeparator, 5);
		labelFormData.left = new FormAttachment(0);

		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(labelFormData);
		composite_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		composite_1.setBackground(new Color(null, 255, 255, 255));
		okButton = new Button(composite_1, SWT.NONE);
		okButton.setImage(registry.getImage("OK_16.ICON"));
		okButton.addSelectionListener(new SelectionAdapter()
		{
			/**
			 * OK ��ư ���� ó��
			 */
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				okProcess();
			}
		});

		// okButton.setBounds(0, 0, 77, 24);
		okButton.setText(IDialogConstants.OK_LABEL);
		Button closeButton = new Button(composite_1, SWT.NONE);
		closeButton.setImage(registry.getImage("Cancel_16.ICON"));

		closeButton.addSelectionListener(new SelectionAdapter()
		{
			/**
			 * Close ��ư ���� ó��
			 */
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				action = false;
				shell.close();
			}
		});
		closeButton.setText(IDialogConstants.CANCEL_LABEL);

		mainCB.setSelectedString((String) this.attrMap.get("s7_MAIN_NAME"));

		
		subCB.setSelectedString((String) this.attrMap.get("s7_SUB_NAME"));
		loc1CB.setSelectedString((String) this.attrMap.get("s7_LOC1_FR"));
		loc2CB.setSelectedString((String) this.attrMap.get("s7_LOC2_IO"));
		loc3CB.setSelectedString((String) this.attrMap.get("s7_LOC3_UL"));
		loc4CB.setSelectedString((String) this.attrMap.get("s7_LOC4_EE"));
		loc5CB.setSelectedString((String) this.attrMap.get("s7_LOC5_LR"));

	}

	public boolean isOK()
	{
		return action;
	}

	public Button getOkButton()
	{
		return okButton;
	}

	/**
	 * OK ��ư ����
	 * 
	 * LOV Value : Simple Name
	 * LOV Display Name  : ���� Name
	 * LOV Desc          : �ѱ� Name
	 * 
	 * Main/Sub/LOC1-5 ���� ����(LOV Display Name)���� PartName, KorName ����
	 * PartName�� 30�� �ʰ� �� ��� SimpleName(LOV Value) ����Ͽ� �ٽ� ����
	 * SimpleName ���� ���� 30�� �ʰ� �� ��� �����ڿ��� ��û�ؾ� ��
	 * [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����.
	 * 
	 */
	private void okProcess()
	{
		try
		{
			if ("".equals(mainCB.getSelectedString()))
			{
				MessageBox.post(shell, "Main Name�� �ʼ� �Է� �׸��Դϴ�.", "���", MessageBox.WARNING);
				return;
			}

			// ���� Name
			String strEnName = "";
			// �ѱ� Name
			String strKrName = "";

			// [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����.
			// Step 1. All full name.
			strEnName = genPartName(mainCB.getSelectedDisplayString(), subCB.getSelectedDisplayString(), loc1CB.getSelectedDisplayString(), loc2CB.getSelectedDisplayString()
					, loc3CB.getSelectedDisplayString(), loc4CB.getSelectedDisplayString(), loc5CB.getSelectedDisplayString());
			
			// Step 2. Full Name�� 30�ڸ��� ������ Simple Name���� ��ü �մϴ�.
			// Simple Name ������ Simple Name���� ��ü
			if (strEnName.length() > 30)
			{
				// [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����.
				strEnName = genPartName(mainCB.getSelectedDisplayString(), subCB.getSelectedString(), loc1CB.getSelectedString(), loc2CB.getSelectedString()
						, loc3CB.getSelectedString(), loc4CB.getSelectedString(), loc5CB.getSelectedString());
			}
			 
			// Step 3. Simple Name ������ Simple Name���� ��ü �Ͽ����� 30�ڸ��� ������� MainName�� Simple Name���� ��ü
			if (strEnName.length() > 30)
			{
				// [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����.
				strEnName = genPartName(mainCB.getSelectedString(), subCB.getSelectedString(), loc1CB.getSelectedString(), loc2CB.getSelectedString()
						, loc3CB.getSelectedString(), loc4CB.getSelectedString(), loc5CB.getSelectedString());
			}

			// Step 4. 30�� �ʰ��ϴ� ��� Message ���
			if (strEnName.length() > 30)
			{
				MessageBox.post(shell, "Part Name�� 30�ڸ� �ʰ��մϴ�. ������������ �����ϼ���.", "���", MessageBox.WARNING);
				return;
			}

			// �ѱ۸� ����
			// [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����.
			strKrName = genPartName(this.getLOVDesc(mainCB), this.getLOVDesc(subCB), this.getLOVDesc(loc1CB), this.getLOVDesc(loc2CB)
					, this.getLOVDesc(loc3CB), this.getLOVDesc(loc4CB), this.getLOVDesc(loc5CB));

			this.attrMap.put("object_name", strEnName);
			this.attrMap.put("s7_KOR_NAME", strKrName);

			this.attrMap.put("s7_MAIN_NAME", mainCB.getSelectedString());
			this.attrMap.put("s7_SUB_NAME", subCB.getSelectedString());
			this.attrMap.put("s7_LOC1_FR", loc1CB.getSelectedString());
			this.attrMap.put("s7_LOC2_IO", loc2CB.getSelectedString());
			this.attrMap.put("s7_LOC3_UL", loc3CB.getSelectedString());
			this.attrMap.put("s7_LOC4_EE", loc4CB.getSelectedString());
			this.attrMap.put("s7_LOC5_LR", loc5CB.getSelectedString());
			
			//DR üũ�� �ƴϸ� DR üũ�� �������
			if(!isDRCheck)
			{
				action = true;
				shell.close();
				return;
			}
			
			try
			{
				// Vechicle Part ����ǰ�� ������ VPM�� �����ϴ� Part No.���� Check�ؾ� ��..
				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("partName", strEnName);
				Object result = remote.execute("com.kgm.service.VPMIfService", "getExistDRNameCnt", ds);
	
				if (result instanceof Integer)
				{
					if (((Integer) result).intValue() > 0)
					{
						org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
						box.setText("Ask Proceed");
						box.setMessage("DR Name Master�� �����ϴ� ��ǰ�� �Դϴ�. DR1, DR2 �� �� ������ �����Ͻðڽ��ϱ�?");
	
						int choice = box.open();
						if (choice == SWT.YES)
						{
							this.attrMap.put("DR_CHECK_FLAG", "Y");
						}
					}
				}
			}
			catch(Exception e)
			{
				// ���� �߻��ص� �������
				e.printStackTrace();
			}
			
			
			action = true;
			shell.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * generate Part Name. 
	 * [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����. 
	 * <Main Name>[-<[SubName][loc1][loc2][loc3][loc4]>][-<loc5>] �� ����.
	 * (Main)-(Sub block)-(loc5)
	 * 
	 * ��ȯ �� " and " ��  "&"�� replace.
	 * Main Name�� �ʼ���.
	 * 
	 * [SR140409-033][20140513] bskwak, Part Name ���ڿ� ��ȯ ��ɿ� " (and) " -> "&" �� ��ȯ�ϴ� ��� �߰�.  
	 * 
	 * @param sMainName
	 * @param sSubName
	 * @param sLoc1
	 * @param sLoc2
	 * @param sLoc3
	 * @param sLoc4
	 * @param sLoc5
	 * @return
	 */
	private String genPartName(String sMainName, String sSubName, String sLoc1, String sLoc2, String sLoc3, String sLoc4, String sLoc5)
	{
		String sPartName = "";
		
		// trim
		sMainName = StringUtil.nullToString(sMainName);
		if ("".equals(sMainName))
			return sPartName;
		sSubName = StringUtil.nullToString(sSubName);
		sLoc1 = StringUtil.nullToString(sLoc1);
		sLoc2 = StringUtil.nullToString(sLoc2);
		sLoc3 = StringUtil.nullToString(sLoc3);
		sLoc4 = StringUtil.nullToString(sLoc4);
		sLoc5 = StringUtil.nullToString(sLoc5);


		StringBuilder sbPartName = new StringBuilder();

		// Sub name ~ loc4
		if (!"".equals(sSubName))
			sbPartName.append(sSubName);
		if (!"".equals(sLoc1))
			sbPartName.append(sbPartName.length() > 0 ? " " : "").append(sLoc1);
		if (!"".equals(sLoc2))
			sbPartName.append(sbPartName.length() > 0 ? " " : "").append(sLoc2);
		if (!"".equals(sLoc3))
			sbPartName.append(sbPartName.length() > 0 ? " " : "").append(sLoc3);
		if (!"".equals(sLoc4))
			sbPartName.append(sbPartName.length() > 0 ? " " : "").append(sLoc4);
		if (sbPartName.length() > 0)
		{
			sbPartName.insert(0, "-");
		}

		// Main Name
		sbPartName.insert(0, sMainName);

		// loc5
		if (!"".equals(sLoc5))
			sbPartName.append('-').append(sLoc5);

		sPartName = sbPartName.toString();
		// [SR140409-033][20140513] bskwak, Part Name ���ڿ� ��ȯ ��ɿ� " (and) " -> "&" �� ��ȯ�ϴ� ��� �߰�. 
		sPartName = sPartName.replaceAll(" (and) ", "&");
		// And ���� ��ȣ�� ��ü
		sPartName = sPartName.replaceAll(" and ", " & ");
		
		return sPartName;
	}
	
	/**
	 * �ѱ۸� Getter
	 * @param combo : Name LOVComboBox
	 * @return
	 * @throws TCException
	 */
	private String getLOVDesc(LOVComboBox combo) throws TCException
	{
		TCComponentListOfValues listofvalue = combo.getLovComponent();

		String[] lovValues = listofvalue.getListOfValues().getStringListOfValues();
		String[] lovDesces = listofvalue.getListOfValues().getDescriptions();

		String strSelectedValue = combo.getSelectedString();

		for (int i = 0; i < lovValues.length; i++)
		{
			if (lovValues[i].equals(strSelectedValue))
			{
				return lovDesces[i];
			}
		}

		return null;
	}

}
