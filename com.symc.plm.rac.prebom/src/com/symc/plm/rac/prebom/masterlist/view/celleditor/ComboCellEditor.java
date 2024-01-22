package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import com.symc.plm.rac.prebom.masterlist.model.CellValue;

public class ComboCellEditor extends DefaultCellEditor{
	
	private JTable table = null;
	private int row = -1, column = -1;
	
	public ComboCellEditor(JComboBox comboBox) {
		super(comboBox);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		// TODO Auto-generated method stub
		this.table = table;
		this.row = row;
		this.column = column;
		
		JComboBox combo = (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
		for( int i = 0; i < combo.getItemCount();i++){
			Object obj = combo.getItemAt(i);
			if( obj.toString().equals(value.toString())){
				combo.setSelectedIndex(i);
				break;
			}
		}
		return combo;
	}

	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		JComboBox combo = (JComboBox)editorComponent;
		Object selectedObj = combo.getSelectedItem();
		if( selectedObj == null){
			selectedObj = "";
		}
		
		CellValue cellValue = null;
		Object obj = table.getValueAt(row, column);
		if( obj instanceof CellValue){
			cellValue = (CellValue)obj;
			cellValue = new CellValue(selectedObj.toString()) ;
		}else{
			cellValue = new CellValue(selectedObj.toString()) ;
		}
		return cellValue;
	}
	
	
};