package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.symc.plm.rac.prebom.masterlist.commands.CreateMasterListCommand;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.MessageBox;
import com.toedter.calendar.JDateChooser;

public class SelectDateDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private TCComponentBOMLine preProductLine = null;
	private JDateChooser dateChooser = null;
	private CreateMasterListCommand command = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SelectDateDlg dialog = new SelectDateDlg(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public SelectDateDlg(CreateMasterListCommand command) {
		super(AIFUtility.getActiveDesktop(), false);
		this.command = command;
		init();
	}

	private void init(){
		setResizable(false);
		setTitle("Master List View");
		setBounds(100, 100, 201, 119);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new TitledBorder(null, "Base date selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		dateChooser = new JDateChooser("yyyy-MM-dd", false);
		contentPanel.add(dateChooser);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						okButton.setEnabled(false);
						Date date = dateChooser.getDate();
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						String dateStr = sdf.format(date) + " 23:59:59";
						sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						try {
							date = sdf.parse(dateStr);
							command.loadPreProductInfo(dateChooser.getDate());
						}catch(Exception e){
							MessageBox.post(SelectDateDlg.this, e.getMessage(), "Error", MessageBox.ERROR);
							return;
						}finally{
							dispose();
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
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
}
