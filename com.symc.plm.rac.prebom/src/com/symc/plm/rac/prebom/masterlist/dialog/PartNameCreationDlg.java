package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.util.lov.LovCombo;
import com.symc.plm.rac.prebom.masterlist.view.MasterListReq;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class PartNameCreationDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private LovCombo cbMainName = null;
	private LovCombo cbSubName = null;
	private LovCombo cbLoc1 = null;
	private LovCombo cbLoc2 = null; 
	private LovCombo cbLoc3 = null;
	private LovCombo cbLoc4 = null;
	private LovCombo cbLoc5 = null;
	private JTable table = null;
	private int row = -1;
	private int column = -1;
	private HashMap<String, String> attrMap = new HashMap<String, String>();
	private ArrayList<String> essentialNames = null;
	private MasterListReq parent = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PartNameCreationDlg dialog = new PartNameCreationDlg(null, null, -1, -1, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PartNameCreationDlg(MasterListReq parent) throws Exception{
		this(parent, null, -1, -1, null);
	}
	/**
	 * Create the dialog.
	 * @throws Exception 
	 */
	public PartNameCreationDlg(MasterListReq parent, JTable table, int row, int column, HashMap<String, String> attrMap) throws Exception {
		super((Frame)parent, true);
		this.parent = parent;
		this.table = table;
		this.row = row;
		this.column = column;
		this.attrMap = attrMap;
		
		init();
	}
	
	public void setCellInfo(JTable table, int row, int column, HashMap<String, String> attrMap){
		this.table = table;
		this.row = row;
		this.column = column;
		this.attrMap = attrMap;
	}
	
	private void init() throws Exception{
		setTitle("Part Name Creation");
		setResizable(false);
		setBounds(100, 100, 357, 275);
		TCSession session = CustomUtil.getTCSession();
		
		this.essentialNames = parent.getEssentialNames();
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(7, 1, 0, 0));
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.TRAILING);
			contentPanel.add(panel);
			{
				JLabel lblNewLabel = new JLabel("Main Name");
				panel.add(lblNewLabel);
			}
			{
//				List<LovValue> lov = BomUtil.getLovValues(session, "S7_MAIN_NAME");
				cbMainName = new LovCombo(session, "S7_MAIN_NAME");
//				String[] strArray = CustomUtil.getLOVDisplayValues(session, "S7_MAIN_NAME");
//				cbMainName.setModel(new DefaultComboBoxModel(strArray));
				cbMainName.setPreferredSize(new Dimension(200, 21));
				panel.add(cbMainName);
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.TRAILING);
			contentPanel.add(panel);
			{
				JLabel lblSubName = new JLabel("Sub Name");
				panel.add(lblSubName);
			}
			{
				cbSubName = new LovCombo(session, "S7_SUBNAME");
				cbSubName.setPreferredSize(new Dimension(200, 21));
				panel.add(cbSubName);
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.TRAILING);
			contentPanel.add(panel);
			{
				JLabel lblLocfrrr = new JLabel("Loc1(FR/RR)");
				panel.add(lblLocfrrr);
			}
			{
				cbLoc1 = new LovCombo(session, "S7_LOC_1");
				cbLoc1.setPreferredSize(new Dimension(200, 21));
				panel.add(cbLoc1);
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.TRAILING);
			contentPanel.add(panel);
			{
				JLabel lblLocinrotr = new JLabel("Loc2(INR/OTR)");
				panel.add(lblLocinrotr);
			}
			{
				cbLoc2 = new LovCombo(session, "S7_LOC_2");
				cbLoc2.setPreferredSize(new Dimension(200, 21));
				panel.add(cbLoc2);
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.TRAILING);
			contentPanel.add(panel);
			{
				JLabel lblLocuprlwr = new JLabel("Loc3(UPR/LWR)");
				panel.add(lblLocuprlwr);
			}
			{
				cbLoc3 = new LovCombo(session, "S7_LOC_3");
				cbLoc3.setPreferredSize(new Dimension(200, 21));
				panel.add(cbLoc3);
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.TRAILING);
			contentPanel.add(panel);
			{
				JLabel lblLocinletoutlet = new JLabel("Loc4(INLET/OUTLET)");
				panel.add(lblLocinletoutlet);
			}
			{
				cbLoc4 = new LovCombo(session, "S7_LOC_4");
				cbLoc4.setPreferredSize(new Dimension(200, 21));
				panel.add(cbLoc4);
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.TRAILING);
			contentPanel.add(panel);
			{
				JLabel lblLoclhrh = new JLabel("Loc5(LH/RH)");
				panel.add(lblLoclhrh);
			}
			{
				cbLoc5 = new LovCombo(session, "S7_LOC_5");
				cbLoc5.setPreferredSize(new Dimension(200, 21));
				panel.add(cbLoc5);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						okProcess();
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public void setCellInfo(JTable table, int row, int column){
		this.table = table;
		this.row = row;
		this.column = column;
	}
	
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
			if ("".equals(cbMainName.getSelectedString()))
			{
				MessageBox.post(this, "Main Name�� �ʼ� �Է� �׸��Դϴ�.", "���", MessageBox.WARNING);
				return;
			}

			// ���� Name
			String strEnName = "";
			// �ѱ� Name
			String strKrName = "";

			// [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����.
			// Step 1. All full name.
			strEnName = genPartName(cbMainName.getSelectedDisplayString(), cbSubName.getSelectedDisplayString()
					, cbLoc1.getSelectedDisplayString(), cbLoc2.getSelectedDisplayString()
					, cbLoc3.getSelectedDisplayString(), cbLoc4.getSelectedDisplayString()
					, cbLoc5.getSelectedDisplayString());
			
			// Step 2. Full Name�� 30�ڸ��� ������ Simple Name���� ��ü �մϴ�.
			// Simple Name ������ Simple Name���� ��ü
			if (strEnName.length() > 30)
			{
				// [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����.
				strEnName = genPartName(cbMainName.getSelectedDisplayString(), cbSubName.getSelectedString()
						, cbLoc1.getSelectedString(), cbLoc2.getSelectedString()
						, cbLoc3.getSelectedString(), cbLoc4.getSelectedString()
						, cbLoc5.getSelectedString());
			}
			 
			// Step 3. Simple Name ������ Simple Name���� ��ü �Ͽ����� 30�ڸ��� ������� MainName�� Simple Name���� ��ü
			if (strEnName.length() > 30)
			{
				// [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����.
				strEnName = genPartName(cbMainName.getSelectedString(), cbSubName.getSelectedString()
						, cbLoc1.getSelectedString(), cbLoc2.getSelectedString()
						, cbLoc3.getSelectedString(), cbLoc4.getSelectedString()
						, cbLoc5.getSelectedString());
			}

			// Step 4. 30�� �ʰ��ϴ� ��� Message ���
			if (strEnName.length() > 30)
			{
				MessageBox.post(this, "Part Name�� 30�ڸ� �ʰ��մϴ�. ������������ �����ϼ���.", "���", MessageBox.WARNING);
				return;
			}

			// �ѱ۸� ����
			// [20140414] SR140404-020, Part Name ���� �� Hyphen ���̴� ���� ����.
			strKrName = genPartName( cbMainName.getSelectedDescription(), cbSubName.getSelectedDescription()
					, cbLoc1.getSelectedDescription(), cbLoc2.getSelectedDescription()
					, cbLoc3.getSelectedDescription(), cbLoc4.getSelectedDescription()
					, cbLoc5.getSelectedDescription());

			this.attrMap.put("object_name", strEnName);
			this.attrMap.put("s7_KOR_NAME", strKrName);

			this.attrMap.put("s7_MAIN_NAME", cbMainName.getSelectedString());
			this.attrMap.put("s7_SUB_NAME", cbSubName.getSelectedString());
			this.attrMap.put("s7_LOC1_FR", cbLoc1.getSelectedString());
			this.attrMap.put("s7_LOC2_IO", cbLoc2.getSelectedString());
			this.attrMap.put("s7_LOC3_UL", cbLoc3.getSelectedString());
			this.attrMap.put("s7_LOC4_EE", cbLoc4.getSelectedString());
			this.attrMap.put("s7_LOC5_LR", cbLoc5.getSelectedString());
			
			/*
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
						int response = JOptionPane.showConfirmDialog(this, "DR Name Master�� �����ϴ� ��ǰ�� �Դϴ�. DR1, DR2 �� �� ������ �����Ͻðڽ��ϱ�?", "Confirm",
						        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						 if (response == JOptionPane.YES_OPTION) {
							 this.attrMap.put("DR_CHECK_FLAG", "Y");
						 }else if( response == JOptionPane.NO_OPTION ){
						 }else{
							 return;
						 }
//						org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
//						box.setText("Ask Proceed");
//						box.setMessage("DR Name Master�� �����ϴ� ��ǰ�� �Դϴ�. DR1, DR2 �� �� ������ �����Ͻðڽ��ϱ�?");
//	
//						int choice = box.open();
//						if (choice == SWT.YES)
//						{
//							this.attrMap.put("DR_CHECK_FLAG", "Y");
//						}
					}
				}
			}
			catch(Exception e)
			{
				// ���� �߻��ص� �������
				e.printStackTrace();
			}
*/
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void setVisible(boolean flag) {
		cbMainName.setSelectedIndex(0);
		cbSubName.setSelectedIndex(0);
		cbLoc1.setSelectedIndex(0);
		cbLoc2.setSelectedIndex(0);
		cbLoc3.setSelectedIndex(0);
		cbLoc4.setSelectedIndex(0);
		cbLoc5.setSelectedIndex(0);
		super.setVisible(flag);
	}

	public ArrayList<String> getEssentialNames() {
		return essentialNames;
	}

}
