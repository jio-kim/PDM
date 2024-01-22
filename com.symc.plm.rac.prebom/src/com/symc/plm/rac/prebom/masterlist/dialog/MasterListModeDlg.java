package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import com.symc.plm.rac.prebom.masterlist.commands.CreateMasterListCommand;
import com.symc.plm.rac.prebom.masterlist.model.SimpleTcObject;
import com.symc.plm.rac.prebom.masterlist.util.WebUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.UIUtilities;

public class MasterListModeDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JRadioButton rdbtnView = null;
	private JRadioButton rdbtnEdit = null;
	private JComboBox cbCCN = null;
	private CreateMasterListCommand command = null;
	private TCComponentItemRevision preProductRevision = null;
	private TCComponentItemRevision fmpRevision = null;
//	private TCComponentBOMLine fmpLine = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MasterListModeDlg dialog = new MasterListModeDlg(null, null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * @throws Exception 
	 */
	public MasterListModeDlg(CreateMasterListCommand command, TCComponentItemRevision preProductRevision, TCComponentItemRevision fmpRevision) throws Exception {
		super(AIFUtility.getActiveDesktop(), false);
		
		this.command = command;
		this.preProductRevision = preProductRevision;
		this.fmpRevision = fmpRevision;
		
		init();
		pack();
		UIUtilities.centerToScreen(this);
	}
	
	private void init() throws Exception{
		setTitle("Master List Mode Selection");
//		setBounds(100, 100, 210, 153);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			contentPanel.add(panel, BorderLayout.NORTH);
			
			
			{
				rdbtnView = new JRadioButton("View");
				rdbtnView.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if( rdbtnView.isSelected()){
							cbCCN.setEnabled(false);
						}
					}
				});
				rdbtnView.setSelected(true);
				panel.add(rdbtnView);
				
				rdbtnEdit = new JRadioButton("Edit");
				rdbtnEdit.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if( rdbtnEdit.isSelected()){
							cbCCN.setEnabled(true);
						}
					}
				});
				panel.add(rdbtnEdit);
				
				ButtonGroup buttonGroup = new ButtonGroup();
				buttonGroup.add(rdbtnView);
				buttonGroup.add(rdbtnEdit);
				
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			contentPanel.add(panel, BorderLayout.CENTER);
			{
				cbCCN = new JComboBox();
				cbCCN.setEnabled(false);
				ArrayList<HashMap> ccns = WebUtil.getWorkingCCN();
				cbCCN.addItem("Select a CCN");
				if( ccns != null){
					for( HashMap map : ccns){
						String ccnNo = (String)map.get("CCN_NO");
						String puid = (String)map.get("PUID");
						SimpleTcObject ccn = new SimpleTcObject(ccnNo, puid);
						cbCCN.addItem(ccn);
					}
				}
				panel.add(cbCCN);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						
						if( rdbtnEdit.isSelected()){
							Object obj = cbCCN.getSelectedItem();
							if( obj instanceof SimpleTcObject){
								setVisible(false);
								try {
									command.openMasterListDlg(preProductRevision, fmpRevision, true);
								} catch (Exception e) {
									MessageBox.post(MasterListModeDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
									return;
								}
							}else{
								MessageBox.post(MasterListModeDlg.this, cbCCN.getSelectedItem().toString(), "ERROR", MessageBox.ERROR);
								return;
							}
						}else{
							setVisible(false);
							try {
								command.openMasterListDlg(preProductRevision, fmpRevision, false);
							} catch (Exception e) {
								MessageBox.post(MasterListModeDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
								return;
							}
						}
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
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public boolean isEditMode(){
		return rdbtnEdit.isSelected();
	}
	
	public SimpleTcObject getSelectedCCN(){
		Object obj = cbCCN.getSelectedItem();
		if( obj instanceof SimpleTcObject){
			return (SimpleTcObject)obj;
		}else{
			return null;
		}
	}
}
