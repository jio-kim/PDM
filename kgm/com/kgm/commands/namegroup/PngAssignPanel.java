package com.kgm.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.teamcenter.rac.util.MessageBox;

public class PngAssignPanel extends JPanel {

	private PngDlg parentDlg;
	private JComboBox cbProduct = null;
	private PngAssignTablePanel assignPanel = null;
	/**
	 * Create the panel.
	 * @throws Exception 
	 */
	public PngAssignPanel(PngDlg parentDlg) throws Exception {
		this.parentDlg = parentDlg;
		init();
	}

	private void init() throws Exception{
		setLayout(new BorderLayout(0, 0));
		
		assignPanel = new PngAssignTablePanel(parentDlg, null, null, null);
		add(assignPanel, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		panel_1.add(panel_2, BorderLayout.WEST);
		
		cbProduct = new JComboBox();
		cbProduct.setModel(new DefaultComboBoxModel(new String[] {PngDlg.SELECT_PRODUCT}));
		ArrayList<String> list = parentDlg.getProductList();
		for( String productID : list){
			cbProduct.addItem(productID);
		}
		panel_2.add(cbProduct);
		
		JButton button = new JButton("");
		button.setIcon(new ImageIcon(PngAssignPanel.class.getResource("/com/teamcenter/rac/aif/images/add_16.png")));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				try {
					String product = (String)cbProduct.getSelectedItem();
					Vector headerVec = assignPanel.getCurrentProductHeader();
					if( headerVec.contains(product)){
						MessageBox.post(parentDlg, "The Product has already been registered." , "Infomation", MessageBox.INFORMATION);
						return;
					}
					
					if( cbProduct.getSelectedItem().equals(PngDlg.SELECT_PRODUCT)){
						MessageBox.post(parentDlg, PngDlg.SELECT_PRODUCT , "Infomation", MessageBox.INFORMATION);
						return;
					}
					
					PngAssignTablePanel newAssignPanel = new PngAssignTablePanel(parentDlg, (String)cbProduct.getSelectedItem(), null, assignPanel.getCurrentProductHeader());
					remove(assignPanel);
					assignPanel = newAssignPanel;
					add(assignPanel, BorderLayout.CENTER);
					revalidate();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_2.add(button);
		
		JButton button_1 = new JButton("");
		button_1.setIcon(new ImageIcon(PngAssignPanel.class.getResource("/com/teamcenter/rac/aif/images/remove_16.png")));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				try {
					String product = (String)cbProduct.getSelectedItem();
					Vector headerVec = assignPanel.getCurrentProductHeader();
					if( !headerVec.contains(product)){
						MessageBox.post(parentDlg, "Not registered Product." , "Infomation", MessageBox.INFORMATION);
						return;
					}
					
					PngAssignTablePanel newAssignPanel = new PngAssignTablePanel(parentDlg, null, (String)cbProduct.getSelectedItem(), assignPanel.getCurrentProductHeader());
					remove(assignPanel);
					assignPanel = newAssignPanel;
					add(assignPanel, BorderLayout.CENTER);
					revalidate();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_2.add(button_1);
		
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
		flowLayout_1.setAlignment(FlowLayout.RIGHT);
		panel_1.add(panel_3);
		
		/**
		 * [SR150416-025][2015.05.27][jclee] Assignment 체크박스 일괄 체크 기능 추가
		 */
		JButton btnSelectAll = new JButton("Select All");
		btnSelectAll.setIcon(new ImageIcon(PngAssignPanel.class.getResource("/icons/mvcondition_16.png")));
		btnSelectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				try {
					int iColumnCount = assignPanel.getPngViewTable().getColumnCount();
					int iRowCount = assignPanel.getPngViewTable().getRowCount();
					
					for (int inx = 0; inx < iColumnCount; inx++) {
						for (int jnx = 0; jnx < iRowCount; jnx++) {
							assignPanel.getPngViewTable().setValueAt(Boolean.TRUE, jnx, inx);
						}
					}
					assignPanel.revalidate();
					assignPanel.repaint();
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_3.add(btnSelectAll);
		
		JButton btnReload = new JButton("Reload");
		btnReload.setIcon(new ImageIcon(PngAssignPanel.class.getResource("/icons/refresh_16.png")));
		btnReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				PngAssignTablePanel newAssignPanel;
				try {
					newAssignPanel = new PngAssignTablePanel(parentDlg, null, null, null);
					remove(assignPanel);
					assignPanel = newAssignPanel;
					add(assignPanel, BorderLayout.CENTER);
					revalidate();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_3.add(btnReload);
		
		JButton btnSave = new JButton("Save");
		btnSave.setIcon(new ImageIcon(PngAssignPanel.class.getResource("/icons/save_16.png")));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				try {
					assignPanel.save();
					MessageBox.post(parentDlg, "Successfully saved." , "Infomation", MessageBox.INFORMATION);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_3.add(btnSave);
	}
}
