package com.ssangyong.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;

public class NewNameListDlg extends AbstractAIFDialog {

	private PngDlg parentDlg = null;
	private JTable newNameTable = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			NewNameListDlg dialog = new NewNameListDlg(null);
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
	public NewNameListDlg(PngDlg parentDlg) throws Exception {
		super(parentDlg, true);
		setTitle("New Name List");
		this.parentDlg = parentDlg;
		init();
	}
	
	private void init() throws Exception{
		setBounds(100, 100, 400, 400);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ArrayList<String> newNameList = new ArrayList();
						int[] rows = newNameTable.getSelectedRows();
						for( int i = 0; rows != null && i < rows.length; i++){
							newNameList.add((String)newNameTable.getValueAt(rows[i], 0));
						}
						parentDlg.addNewName(newNameList);
						dispose();
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
		{
			Vector<String> headerVec = new Vector();
			headerVec.add("New Part Name");
			headerVec.add("ECO");
			DefaultTableModel model = new DefaultTableModel(getNewNameList(), headerVec);
			newNameTable = new JTable(model);
			JScrollPane scrollPane = new JScrollPane(newNameTable);
			getContentPane().add(scrollPane, BorderLayout.CENTER);

			// [NoSR][20160404][jclee] Part Name Filtering TextField 추가 (송대영CJ, E-Mail로 요청)
			final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
			newNameTable.setRowSorter(sorter);

			JPanel pnlFilter = new JPanel();
			pnlFilter.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			JLabel lblFilter = new JLabel();
			lblFilter.setText("Filter : ");
			
			final JTextField tfFilter = new JTextField();
			tfFilter.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent event) {
					String sFilter = tfFilter.getText();
					
					if (sFilter == null || sFilter.equals("") || sFilter.length() == 0) {
						sorter.setRowFilter(null);
					} else {
						sorter.setRowFilter(RowFilter.regexFilter(sFilter));
					}
				}
			});
			tfFilter.setColumns(29);
			
			getContentPane().add(pnlFilter, BorderLayout.NORTH);
			pnlFilter.add(lblFilter);
			pnlFilter.add(tfFilter);
		}
	}
	
	private Vector getNewNameList() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("DATA", null);
		try {
			
			ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.PartNameGroupService", "getPngNewNameList", ds);
			
			Vector<Vector> data = new Vector();
			for( HashMap<String, String> map : list){
				String partName = map.get("PART_NAME");
				String ecoNo = map.get("ECO_NO");
				Vector row = new Vector();
				row.add(partName);
				row.add(ecoNo);
				data.add(row);
			}
			
			return data;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

}
