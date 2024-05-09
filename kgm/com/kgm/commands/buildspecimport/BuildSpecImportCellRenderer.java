package com.kgm.commands.buildspecimport;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;

@SuppressWarnings("serial")
public class BuildSpecImportCellRenderer extends DefaultTableCellRenderer {

	HashMap<String, VariantOption> optionMap = null;
	
	public BuildSpecImportCellRenderer(HashMap<String, VariantOption> optionMap){
		this.optionMap= optionMap; 
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
		JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		
		Object objValue = table.getValueAt(row, column);
		if( objValue instanceof String){
			String str = (String)table.getValueAt(row, column);
			label.setToolTipText(str);
			int modelColumn = table.convertColumnIndexToModel(column);
			if( modelColumn == 4 ){
				label.setHorizontalAlignment(JLabel.LEFT);
			}else{
				label.setHorizontalAlignment(JLabel.CENTER);
			}
		}
		int[] selectedIdx = table.getSelectedRows();
		for( int idx : selectedIdx){
			if( idx == row){
				label.setBackground(new Color(51,153,255));
				return label;
			}
		}
		
		@SuppressWarnings("unchecked")
		TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)table.getRowSorter();
		String category = (String)model.getValueAt(sorter.convertRowIndexToModel(row), 5);
		if( optionMap.containsKey(category)){
			
			String optionValue = (String)model.getValueAt(sorter.convertRowIndexToModel(row), 6);
			VariantOption option = optionMap.get(category);
			HashMap<String, VariantValue> valueMap = option.getValueMap();
			if( valueMap.containsKey(optionValue)){
				label.setBackground(Color.WHITE);
			}else{
				label.setBackground(Color.ORANGE);
			}
			
		}else{
			
			label.setBackground(Color.ORANGE);
			
		}
		
		return label;
	}

}
