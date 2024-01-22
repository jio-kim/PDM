package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.masterlist.dialog.PartNameCreationDlg;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;

public class PartNameCellEditor extends DefaultCellEditor{

	HashMap attrMap = new HashMap();
	boolean isOpenDlg = false;
	PartNameCreationDlg partNameCreationDlg = null;
	private JTable table = null;
	private int row = -1, column = -1;
	private JTextField tf = null;
	
	public PartNameCellEditor(JTextField tf, PartNameCreationDlg partNameCreationDlg){
		super(tf);
		this.partNameCreationDlg = partNameCreationDlg;
		this.tf = tf;
//		this.
	}
	
	@Override
	public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
		// TODO Auto-generated method stub
//		JTextField tf = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
		attrMap.clear();
		isOpenDlg = false;
		
		this.table = table;
		this.row = row;
		this.column = column;
		
		String partName = null;
		if( attrMap.isEmpty()){
			if( value instanceof CellValue){
				HashMap oldMap = ((CellValue)value).getData();
				if( oldMap != null && !oldMap.isEmpty()){
					attrMap = oldMap;
				}else{
					attrMap.put("object_name", value.toString());
				}
			}else if( value instanceof String){
				attrMap.put("object_name", value);
			}
		}
		partName = (String)attrMap.get("object_name");
		tf.setText(partName);
		
		JPanel panel = new JPanel(new BorderLayout(0, 0));
		panel.add(tf, BorderLayout.CENTER);
		JButton btn = new JButton("");
		btn.setPreferredSize(new Dimension(15,20));
		btn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				// TODO Auto-generated method stub
				isOpenDlg = true;
				partNameCreationDlg.setCellInfo(table, row, column, attrMap);
				partNameCreationDlg.setVisible(true);
				
				Object obj = attrMap.get("s7_MAIN_NAME");
				if( obj != null && !obj.equals("")){
					attrMap.put("IS_OPEN_DLG", new Boolean(true));
				}else{
					attrMap.put("IS_OPEN_DLG", new Boolean(false));
				}
				//Modal이므로 창닫은 후, 나머지 실행됨.
				stopCellEditing();
			}
			
		});
		panel.add(btn, BorderLayout.EAST);
		return panel;
	}

	@Override
	public Object getCellEditorValue() {
		String partName = null;
		if( attrMap == null || attrMap.isEmpty()){
			partName = tf.getText();
		}else{
			partName = (String)attrMap.get(PropertyConstant.ATTR_NAME_ITEMNAME);
			String tfValue = tf.getText().trim();
			if( partName != null && !partName.equals("")){
				if( !isOpenDlg ){
					partName = tfValue;
				}
			}else{
				partName = tfValue;
			}
			attrMap.put(PropertyConstant.ATTR_NAME_ITEMNAME, partName);
		}
		CellValue cellValue = new CellValue(partName);
		
		HashMap newMap = (HashMap)attrMap.clone();
		cellValue.setData(newMap);
		
//		DefaultTableModel model = (DefaultTableModel)table.getModel();
//		int modelRow = table.convertRowIndexToModel(row);
//		ArrayList<String> essentialNames = partNameCreationDlg.getEssentialNames();
//		if( essentialNames != null && essentialNames.contains(partName)){
//			model.setValueAt("O", modelRow, MasterListTablePanel.MASTER_LIST_ESSENTIAL_NAME);
//		}else{
//			model.setValueAt("", modelRow, MasterListTablePanel.MASTER_LIST_ESSENTIAL_NAME);
//		}
//		table.repaint();
		
		return cellValue;
	}
	
}


