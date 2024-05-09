package com.kgm.commands.variantoptioncompare;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;

import com.kgm.common.utils.variant.VariantValue;

/**
 * Product에 설정된 Option과 Variant에 설정된 옵션을 비교하여
 * 사용하지 않는 옵션은 오렌지 색으로 표기함.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings("serial")
public class OptionCompareTableCellRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object obj,
			boolean flag, boolean flag1, int row, int column) {
		Component com = super.getTableCellRendererComponent(table, obj, flag, flag1, row, column);
		
		//Variant SUM 컬럼의 column index 는 3이다.
		int targetColumn = 3;
		DefaultTableColumnModel tcm = (DefaultTableColumnModel)table.getColumnModel();
		for( int i = 0; i < tcm.getColumnCount(); i++){
			javax.swing.table.TableColumn tc = tcm.getColumn(i);
			if( "Variant SUM".equals(tc.getHeaderValue())){
				targetColumn = i;
			}
		}
		
		if( obj instanceof VariantValue){
			
			JLabel label = (JLabel)com;
			VariantValue value = (VariantValue)obj;
			label.setText(value.getValueName());
			label.setToolTipText(value.getValueDesc());
			
			if( table.getValueAt(row, targetColumn) instanceof VariantValue){
				int[] selectedIdx = table.getSelectedRows();
				for( int idx : selectedIdx){
					if( idx == row){
						com.setBackground(new Color(51,153,255));
						return com;
					}
				}
				com.setBackground(Color.white);
			}else{
				label.setBackground(Color.ORANGE);
			}
		}else{
			if( table.getValueAt(row, targetColumn) instanceof VariantValue){
				int[] selectedIdx = table.getSelectedRows();
				for( int idx : selectedIdx){
					if( idx == row){
						com.setBackground(new Color(51,153,255));
						return com;
					}
				}
				com.setBackground(Color.white);
			}else{
				com.setBackground(Color.ORANGE);
			}
		}
		
		return com;
	}

}
