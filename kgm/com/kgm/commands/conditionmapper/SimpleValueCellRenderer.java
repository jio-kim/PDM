package com.kgm.commands.conditionmapper;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.kgm.common.utils.variant.VariantValue;

/**
 * 테이블에서 보여지는 데이타 형식을 정의함.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings("serial")
public class SimpleValueCellRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable jtable, Object obj,
			boolean flag, boolean flag1, int i, int j) {
		if( obj instanceof VariantValue){
			
			JLabel label = (JLabel)super.getTableCellRendererComponent(jtable, obj, flag, flag1, i, j);
			VariantValue value = (VariantValue)obj;
			label.setText(value.getValueName());
			return label;
		}else{
			return super.getTableCellRendererComponent(jtable, obj, flag, flag1, i, j);
		}
		
	}

}
