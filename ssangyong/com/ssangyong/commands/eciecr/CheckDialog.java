package com.ssangyong.commands.eciecr;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.StringUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * ECO ECI,ECR Interface ��� �� �Է� �˾� ȭ��
 * @author 188955
 *
 */
public class CheckDialog extends AbstractAIFDialog {
	public static final String ECOHISTORY_QUERY_SERVICE = "com.ssangyong.service.ECOHistoryService";
	private static final long serialVersionUID = 1L;
	private JPanel contentPanel = new JPanel();
	DefaultTableModel tableModel;
	JTextField tfEcoNo, tfEciNo, tfEcrNo;
	Frame frame = null;
	int row;
	/**
	 * Create the dialog.
	 */
	public CheckDialog(Frame frame, DefaultTableModel tableModel, int selectedRow) throws Exception {
		super(frame, true);
		this.frame = frame;
		this.tableModel = tableModel;
		this.row = selectedRow;
		initUI();
		loadData();
	}
	
	private void initUI() throws Exception{
		setTitle("�Է�");
		
		contentPanel.add(createPanel());
		
		getContentPane().setLayout(new VerticalLayout(5,5,5,5,5));
		getContentPane().add("unbound.bind.center.center", contentPanel);
		// [20240229] â�� ©���� ������ ���� ũ�� ���� 410 -> 500, 210 -> 230
		setPreferredSize(new Dimension(500,230));
	}
	
	private JPanel createPanel() {
		contentPanel = new JPanel(new VerticalLayout());
		contentPanel.add("top.bind.center.center", createAlertPanel());
		contentPanel.add("center.bind.center.center", createInputPanel());
		contentPanel.add("bottom.bind.center.center", createBtnPanel());
		
		return contentPanel;
	}
	
	private JPanel createAlertPanel(){

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JLabel msglabel = new JLabel();
		msglabel.setFont(new Font("���� ���", Font.BOLD, 13));
		msglabel.setForeground(Color.RED);
		msglabel.setText("�� ECI, ECR No�� ��Ƽ �Է� �� �޸�(,)�� �����ؼ� �Է��ϼ���. ��");
        
        
		panel.add(msglabel);

		return panel;
	}
	
	/**
	 * �Է� Panel
	 * @return
	 */
	private JPanel createInputPanel(){
		JPanel attrPanel = new JPanel(new GridLayout(3,1));
		
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel ecolabel = new JLabel("ECO No : ");
		// [20240229] â�� ©���� ������ ���� ũ�� ���� 60 -> 80
		ecolabel.setPreferredSize(new Dimension(80,20));
		ecolabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel1.add(ecolabel);
		tfEcoNo = new JTextField(100);
		tfEcoNo.setColumns(10);
		panel1.add(tfEcoNo);
		attrPanel.add(panel1);
		
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel ecilabel = new JLabel(" ECI No : ");
		// [20240229] â�� ©���� ������ ���� ũ�� ���� 60 -> 80
		ecilabel.setPreferredSize(new Dimension(80,20));
		ecilabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel2.add(ecilabel);
		tfEciNo = new JTextField(100);
		tfEciNo.setColumns(25);
		panel2.add(tfEciNo);
		attrPanel.add(panel2);
	
		JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel ecrlabel = new JLabel("ECR No : ");
		// [20240229] â�� ©���� ������ ���� ũ�� ���� 60 -> 80
		ecrlabel.setPreferredSize(new Dimension(80,20));
		ecrlabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel3.add(ecrlabel);
		tfEcrNo = new JTextField(100);
		tfEcrNo.setColumns(25);
		panel3.add(tfEcrNo);
		attrPanel.add(panel3);
		
		return attrPanel;
	}
	
	
	/**
	 * ��ư �г�
	 * @return
	 */
	private JPanel createBtnPanel(){

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton confirmButton = new JButton("Ȯ��");
		confirmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				confirm();
			}
		});

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				dispose();
			}
		});
		buttonPanel.add(confirmButton);
		buttonPanel.add(closeButton);

		return buttonPanel;
	}
	
	/**
	 * ���� ��� â���� Row Ŭ�� �� Data Loading
	 */
	private void loadData() {
		if(row != -1) {
			setTitle("����");
			tfEcoNo.setText( (String)tableModel.getValueAt(row, 0) );
			tfEciNo.setText( (String)tableModel.getValueAt(row, 1) );
			tfEcrNo.setText( (String)tableModel.getValueAt(row, 2) );
		}
		
	}
	
	/**
	 * Ȯ�� ��ư Ŭ�� �� ȣ��
	 */
	private void confirm() {
		tfEcoNo.setText( tfEcoNo.getText().toUpperCase().replaceAll(" ", "") );
		tfEciNo.setText( tfEciNo.getText().toUpperCase().replaceAll(" ", "") );
		tfEcrNo.setText( tfEcrNo.getText().toUpperCase().replaceAll(" ", "") );
		
		if( checkValidation() ) {
			if(row != -1) { // ���� ��� â���� Table Row Ŭ�� ��
				tableModel.setValueAt(tfEcoNo.getText(), row, 0);
				tableModel.setValueAt(tfEciNo.getText(), row, 1);
				tableModel.setValueAt(tfEcrNo.getText(), row, 2);
			}else { // ���� ��� â���� 'Add' ��ư Ŭ�� ��
				tableModel.addRow(new String[]{"", "", ""});
				int nRow = tableModel.getRowCount() - 1;
				
				tableModel.setValueAt(tfEcoNo.getText(), nRow, 0);
				tableModel.setValueAt(tfEciNo.getText(), nRow, 1);
				tableModel.setValueAt(tfEcrNo.getText(), nRow, 2);
			}
			dispose();
		}
		
	}
	
	/**
	 * �Էµ� ECO ,ECI , ECR ��ȣ�� ���� Validation Check Method
	 * @return
	 */
	private boolean checkValidation() {
		boolean bln = true;
		String ecoNo = tfEcoNo.getText();
		String eciNo = tfEciNo.getText();
		String ecrNo = tfEcrNo.getText();
		TCComponentItem ecoItem = null;
		TCComponentItemRevision ecoRevision = null;
		String[] ecoInfoProperties = new String[]{"release_statuses", "s7_ECR_NO"};
		String[] properties = null;
		HashMap<String, String> ecoPropertyMap = new HashMap<String, String>();
		DataSet ds = new DataSet();
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		try {
			
			// ECO No �Է� Ȯ��
			if(ecoNo.equals("")) {
				MessageBox.post(this, "ECO No�� �Է��Ͻʽÿ�.", "Ȯ��", MessageBox.ERROR);
				bln = false;
			}
			
			if(bln) {
				// ECO ���翩�� Ȯ��
				ecoItem = CustomUtil.findItem(SYMCECConstant.ECOTYPE, (String) ecoNo);
				if(ecoItem == null) {
					MessageBox.post(this, "�������� �ʴ� ECO �Դϴ�.", "Ȯ��", MessageBox.ERROR);
					bln = false;
				}
			}
			
			if(bln) {
				//ECO �������ο��� Ȯ��
				ecoRevision = ecoItem.getLatestItemRevision();
				properties = ecoRevision.getProperties(ecoInfoProperties);
				for(int i = 0 ; i < ecoInfoProperties.length ; i++){
					ecoPropertyMap.put(ecoInfoProperties[i], properties[i]);
				}
				
				if(ecoPropertyMap.get("release_statuses") != null && !ecoPropertyMap.get("release_statuses").equals("S7_Released")) {
					MessageBox.post(this, "���οϷ� �������� ECO �Դϴ�.", "Ȯ��", MessageBox.ERROR);
					bln = false;
				}
			}
			
			if(bln) {
				if(StringUtil.nullToString(eciNo).equals("") && StringUtil.nullToString(ecrNo).equals("")) {
					MessageBox.post(this, "ECI Ȥ�� ECR �� �� �ϳ��� �ԷµǾ�� �մϴ�.", "Ȯ��", MessageBox.ERROR);
					bln = false;
				}
			}
			
			if(bln) {
				if(!StringUtil.nullToString(eciNo).equals("")) {
					if(eciNo.lastIndexOf(",") == eciNo.length()-1) {
						MessageBox.post(this, "�Է��� ECI No ���ڸ� ','�� ���� �Ͻʽÿ�.", "Ȯ��", MessageBox.ERROR);
						bln = false;
					}
				}
			}
			
			
			if(bln) {
				// �Էµ� ECI �ߺ� ���� Ȯ��
				if(!StringUtil.nullToString(eciNo).equals("")) {
					String[] sECINos = ecoRevision.getTCProperty("s7_ECI_NO").getStringValueArray();
					String[] splitEciNo = eciNo.split(",");
					
					//System.out.println("sECINos:"+sECINos.length);
					//System.out.println("splitEciNo:"+splitEciNo.length);
					
					//System.out.println("lastIndexOf:"+eciNo.lastIndexOf(","));
					//System.out.println("length:"+eciNo.length());
					Map<String, String> dualChkMap = new HashMap<String, String>();
					for(int j = 0; j < splitEciNo.length ; j++) 
					{
						if(dualChkMap.get(splitEciNo[j]) == null)
							dualChkMap.put(splitEciNo[j], splitEciNo[j]);
						else {
							MessageBox.post(this, "�Է��� ECI No ["+splitEciNo[j]+"] �� �ߺ��Դϴ�.", "Ȯ��", MessageBox.ERROR);
							bln = false;
							break;
						}
						
						for (int i = 0; i < sECINos.length; i++ ) 
						{
							if(sECINos[i].equals(splitEciNo[j].trim())) {
								MessageBox.post(this, "�ش� ECO�� �̹� �Էµ� ECI No ["+splitEciNo[j]+"] �Դϴ�.", "Ȯ��", MessageBox.ERROR);
								bln = false;
								break;
							}
						}
						
						if(!bln) break;
					}
				}
				
			}
			
			if(bln) {
				if(!StringUtil.nullToString(ecrNo).equals("")) {
					if(ecrNo.lastIndexOf(",") == ecrNo.length()-1) {
						MessageBox.post(this, "�Է��� ECR No ���ڸ� ','�� ���� �Ͻʽÿ�.", "Ȯ��", MessageBox.ERROR);
						bln = false;
					}
				}
			}
			
			if(bln) {
				// ���Էµ� ECR ���翩�� Ȯ��
				if(!StringUtil.nullToString(ecrNo).equals("")) {
					String[] sECRNos = ecoPropertyMap.get("s7_ECR_NO").trim().split(",");// ecoRevision.getTCProperty("s7_ECR_NO").
					String[] splitEcrNo = ecrNo.split(",");
					
					int splitEcrNoLen = splitEcrNo.length;
					int sECRNosLen = sECRNos.length;
					
					if(StringUtil.nullToString(ecoPropertyMap.get("s7_ECR_NO")).equals(""))
						sECRNosLen = 0;
						
					//System.out.println("sECRNos:"+sECRNosLen);
					//System.out.println("splitEcrNo:"+splitEcrNoLen);
					Map<String, String> dualChkMap = new HashMap<String, String>();
					for(int j = 0; j < splitEcrNoLen ; j++) 
					{
						if(dualChkMap.get(splitEcrNo[j]) == null)
							dualChkMap.put(splitEcrNo[j], splitEcrNo[j]);
						else {
							MessageBox.post(this, "�Է��� ECR No ["+splitEcrNo[j]+"] �� �ߺ��Դϴ�.", "Ȯ��", MessageBox.ERROR);
							bln = false;
							break;
						}
						
						for (int i = 0; i < sECRNosLen; i++ ) 
						{
							if(sECRNos[i].equals(splitEcrNo[j].trim())) {
								MessageBox.post(this, "�ش� ECO�� �̹� �Էµ� ECR No ["+splitEcrNo[j]+"] �Դϴ�.", "Ȯ��", MessageBox.ERROR);
								bln = false;
								break;
							}
						}
						
						if(!bln) break;
					}
					if(bln) {
						if(splitEcrNoLen + sECRNosLen > 5) {
							//System.out.println(splitEcrNoLen + sECRNosLen);
							MessageBox.post(this, "ECR�� ���Էµ� ECR No�� �����ؼ� �ִ� 5������ �Է� �����մϴ�.", "Ȯ��", MessageBox.ERROR);
							bln = false;
						}
					}
				}
			}
			
			if(bln) {
				//�Էµ� ECI ��ȣ ���翩�� Ȯ��(IF_ECI_ECR)
				if(!StringUtil.nullToString(eciNo).equals("")) {
					String[] splitEciNo = eciNo.split(",");
					
					for(int j = 0; j < splitEciNo.length ; j++) 
					{
						ds.put("ECINO", splitEciNo[j].trim());
						String rEciNo = (String)remoteQuery.execute(ECOHISTORY_QUERY_SERVICE, "searchEciNo", ds);
						if(StringUtil.nullToString(rEciNo).equals("")) {
							MessageBox.post(this, "�������� �ʴ� ECI No ["+splitEciNo[j]+"] �Դϴ�.", "Ȯ��", MessageBox.ERROR);
							bln = false;
							break;
						}
					}
					
					
				}
				
			}
			
			if(bln) {
				//�Էµ� ECR ��ȣ ���翩�� Ȯ��(IF_ECI_ECR)
				if(!StringUtil.nullToString(ecrNo).equals("")) {
					String[] splitEcrNo = ecrNo.split(",");
					
					for(int j = 0; j < splitEcrNo.length ; j++) 
					{
						ds.put("ECRNO", splitEcrNo[j].trim());
						String rEcrNo = (String)remoteQuery.execute(ECOHISTORY_QUERY_SERVICE, "searchEcrNo", ds);
						if(StringUtil.nullToString(rEcrNo).equals("")) {
							MessageBox.post(this, "�������� �ʴ� ECR No ["+splitEcrNo[j]+"] �Դϴ�.", "Ȯ��", MessageBox.ERROR);
							bln = false;
							break;
						}
					}
				}
			}
			
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		
		return bln;
	}
	
}
