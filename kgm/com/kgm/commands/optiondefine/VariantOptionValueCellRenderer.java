package com.kgm.commands.optiondefine;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.kgm.common.utils.variant.VariantValue;

/**
 * 사용중은 옶션 값은 회색으로 렌더링함. 
 * 비사용은 흰색으로 렌더링.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings("serial")
public class VariantOptionValueCellRenderer extends DefaultTableCellRenderer {

	@SuppressWarnings("unchecked")
    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		
		int[] selectedIdx = table.getSelectedRows();
		if( selectedIdx != null && selectedIdx.length > 0){
			for( int idx : selectedIdx){
				if( idx == row){
					com.setBackground(new Color(51,153,255));
					return com;
				}
			}
		}
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)table.getRowSorter();
		int modelRowIdx = sorter.convertRowIndexToModel(row);
		VariantValue variantValue = (VariantValue)model.getValueAt(modelRowIdx, 0);
		if( variantValue.getValueStatus() == VariantValue.VALUE_USE){
			com.setBackground(Color.LIGHT_GRAY);
		}else{
			com.setBackground(Color.white);
		}
		return com;
	}

}
