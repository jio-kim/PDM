package com.kgm.common.utils.variant;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class VariantCheckBoxTableCellRenderer implements TableCellRenderer {
	
	private ArrayList<VariantValue> valueList = null;
	
	public VariantCheckBoxTableCellRenderer(){
		
	}
	
	public VariantCheckBoxTableCellRenderer(ArrayList<VariantValue> valueList){
		this.valueList = valueList;
	}

	public Component getTableCellRendererComponent(JTable table,
            Object obj,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
		JCheckBox checkBox = new JCheckBox();
		checkBox.setHorizontalAlignment(JLabel.CENTER);
		VariantValue value = (VariantValue)obj;
		if( value.getValueStatus() == VariantValue.VALUE_USE){
			checkBox.setSelected(true);
		}else{
			checkBox.setSelected(false);
		}
//		SRME:: [][20140812] swyoon  Prouct, Variant, Function�� �ɼ� ���� �ӵ� ����(�������� ��뿩�� üũ ����).		
		//�������� Variant value�� �������� �ٲ� ��� ������ ����.
		if( valueList != null){
			if( !valueList.contains( table.getValueAt(row, 0)) && checkBox.isSelected()){
				checkBox.setBackground(Color.ORANGE);
				return checkBox;
			}
		}else{
			if( !table.isCellEditable(row, 0)){
				checkBox.setBackground(Color.ORANGE);
				return checkBox;
			}
		}
		
		if( !value.isNew()){
			checkBox.setBackground(Color.LIGHT_GRAY);
			if( !value.isUsing()){
				int[] selectedIdx = table.getSelectedRows();
				for( int idx : selectedIdx){
					if( idx == row){
						checkBox.setBackground(new Color(51,153,255));
						return checkBox;
					}
				}
				checkBox.setBackground(Color.white);
			}
		}else{
			
			int[] selectedIdx = table.getSelectedRows();
			for( int idx : selectedIdx){
				if( idx == row){
					checkBox.setBackground(new Color(51,153,255));
					return checkBox;
				}
			}
			checkBox.setBackground(Color.white);
		}
		
//		if( !value.isNew() ){
//			cb.setBackground(Color.LIGHT_GRAY);
//			if( !value.isUsing()){
//				int[] selectedIdx = jtable.getSelectedRows();
//				for( int idx : selectedIdx){
//					if( idx == row){
//						cb.setBackground(new Color(51,153,255));
//						return cb;
//					}
//				}
//				cb.setBackground(Color.white);
//			}
//		}else{
//			int[] selectedIdx = jtable.getSelectedRows();
//			for( int idx : selectedIdx){
//				if( idx == row){
//					cb.setBackground(new Color(51,153,255));
//					return cb;
//				}
//			}
//			cb.setBackground(Color.white);
//		}
		return checkBox;
	}
}
