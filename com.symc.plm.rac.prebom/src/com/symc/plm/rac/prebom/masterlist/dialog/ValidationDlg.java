package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class ValidationDlg extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private Vector<Vector> data = null;
	private Vector<String> header = new Vector();
	private MasterListDlg parentDlg = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ValidationDlg dialog = new ValidationDlg(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ValidationDlg(MasterListDlg parentDlg, Vector<Vector> data) {
		super(parentDlg, "Validation" ,Dialog.ModalityType.MODELESS);
		setIconImage( Toolkit.getDefaultToolkit().getImage(ValidationDlg.class.getResource("/icons/tcdesktop_16.png")) );
		this.parentDlg = parentDlg;
		this.data = data;
		init();
	}
	
	private void init(){
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			header.add("Row");
			header.add("Column");
			header.add("Message");
			final DefaultTableModel model = new DefaultTableModel(data, header);
			final JTable table = new JTable(model){

				@Override
				public boolean isCellEditable(int i, int j) {
					// TODO Auto-generated method stub
					return false;
				}
				
			};
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			TableColumnModel tcm = table.getColumnModel();
			tcm.removeColumn(tcm.getColumn(1));
			tcm.removeColumn(tcm.getColumn(0));
			final JScrollPane scrollPane = new JScrollPane(table);
			
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent event) {
					// TODO Auto-generated method stub
					
					JTable masterTable = parentDlg.getMasterListTablePanel().getTable();
					int selectedRow = table.getSelectedRow();
					int selectedModelRow = table.convertRowIndexToModel(selectedRow);
					Object obj = model.getValueAt(selectedModelRow, 0);
					int masterModelRow = Integer.parseInt(obj.toString());
					
					obj = model.getValueAt(selectedModelRow, 1);
					int masterModelColumn = Integer.parseInt(obj.toString());
					int masterTableRow = masterTable.convertRowIndexToView(masterModelRow);
					int masterTableColumn = masterTable.convertColumnIndexToView(masterModelColumn);
					
					masterTable.clearSelection();
					if( masterTableRow > -1){
						masterTable.setRowSelectionInterval(masterTableRow, masterTableRow);
						masterTable.setColumnSelectionInterval(masterTableColumn, masterTableColumn);
						
						Rectangle rect = masterTable.getCellRect(masterTableRow, masterTableColumn, true);
						
						JViewport viewPort = (JViewport)masterTable.getParent();
						Point pt = viewPort.getViewPosition();
				        rect.setLocation(rect.x-pt.x, rect.y-pt.y);

				        viewPort.scrollRectToVisible(rect);
						
					}
				}
				
			});
			
			contentPanel.add(scrollPane);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton closeButton = new JButton("Close");
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						dispose();
					}
				});
				closeButton.setActionCommand("Close");
				buttonPane.add(closeButton);
			}
		}
	}

}
