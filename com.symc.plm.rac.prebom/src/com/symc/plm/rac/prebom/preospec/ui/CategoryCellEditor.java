package com.symc.plm.rac.prebom.preospec.ui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

public class CategoryCellEditor extends DefaultCellEditor{
	protected JCheckBox checkBox = null; 
	
	public CategoryCellEditor(JCheckBox checkBox) {
		super(checkBox);
		this.checkBox = checkBox;
		// TODO Auto-generated constructor stub
	}

	@Override
	public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
		// TODO Auto-generated method stub
		final JCheckBox checkBox = (JCheckBox)super.getTableCellEditorComponent(table,
	            value,
	            isSelected,
	            row,
	            column);
		checkBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent itemevent) {
				// TODO Auto-generated method stub
				ItemListener[] listener = checkBox.getItemListeners();
				for( int i = 0; listener != null && i < listener.length; i++){
					checkBox.removeItemListener(listener[i]);
				}
			}
		});
		checkBox.setText(value.toString());
		return checkBox;
	}

	@Override
	public boolean isCellEditable(EventObject eventobject) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		return super.getCellEditorValue();
	}

    @Override  
    public boolean shouldSelectCell(EventObject anEvent) {  
        return true;  
    }  
  
    @Override  
    public boolean stopCellEditing() {  
        return true;  
    }  
}
