package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;

import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;

/**
 * [SR170703-020][LJG]Proto Tooling 컬럼 추가 대응
 * @Copyright : Plmsoft
 * @author   : 이정건
 * @since    : 2017. 7. 21.
 * Package ID : com.symc.plm.rac.prebom.masterlist.view.celleditor.CheckboxCellEditor.java
 */
public class CheckboxCellEditor extends DefaultCellEditor{
	
	private JTable table;
	private int row, column;

	public CheckboxCellEditor(JCheckBox paramJCheckBox) {
		super(paramJCheckBox);
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		JCheckBox check = (JCheckBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
		this.table = table;
		this.row = row;
		this.column = column;
		if(CustomUtil.isNullString(value.toString())){
			check.setSelected(false);
		}else{
			check.setSelected(true);
		}
		return check;
	}
	
	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		JCheckBox cb = (JCheckBox)editorComponent;
		boolean isSelected = cb.isSelected();
		String selectedValue = "";
		if(isSelected){
			selectedValue = "●";
		}
		CellValue cellValue = null;
		Object obj = table.getValueAt(row, column);
		if( obj instanceof CellValue){
			cellValue = (CellValue)obj;
			cellValue = new CellValue(selectedValue) ;
		}else{
			cellValue = new CellValue(selectedValue) ;
		}
		return cellValue;
//		return super.getCellEditorValue();
	}
}