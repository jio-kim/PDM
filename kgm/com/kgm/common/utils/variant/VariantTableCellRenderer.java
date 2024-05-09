package com.kgm.common.utils.variant;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class VariantTableCellRenderer extends DefaultTableCellRenderer {
	
	private ArrayList<VariantValue> valueList = null;
	
	public VariantTableCellRenderer(){
		
	}
	
	public VariantTableCellRenderer(ArrayList<VariantValue> valueList){
		this.valueList = valueList;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object obj,
			boolean isSelected, boolean hasFocus, int row, int column) {
		Component com = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
		try{
			Object val = table.getValueAt(row, 0);
			if(val instanceof VariantValue){
				VariantValue value = (VariantValue)val;
				
//				SRME:: [][20140812] swyoon  Prouct, Variant, Function�� �ɼ� ���� �ӵ� ����(�������� ��뿩�� üũ ����).				
				//�������� Variant value�� �������� �ٲ� ��� ������ ����.
				if( valueList != null){
					if( !valueList.contains( val) && value.getValueStatus() == VariantValue.VALUE_USE){
						com.setBackground(Color.ORANGE);
						return com;
					}
				}else{
					if( !table.isCellEditable(row, 0)){
						com.setBackground(Color.ORANGE);
						return com;
					}
				}
				
				if( value.getValueName().equals("A30H")){
					System.out.println("hhhh");
				}
				if( !value.isNew()){
					com.setBackground(Color.LIGHT_GRAY);
					if( !value.isUsing()){
						int[] selectedIdx = table.getSelectedRows();
						for( int idx : selectedIdx){
							if( idx == row){
								com.setBackground(new Color(51,153,255));
								return com;
							}
						}
						com.setBackground(Color.white);
					}
				}else{
					
					int[] selectedIdx = table.getSelectedRows();
					for( int idx : selectedIdx){
						if( idx == row){
							com.setBackground(new Color(51,153,255));
							return com;
						}
					}
					com.setBackground(Color.white);
				}
			}
			
		}catch( Exception e){
			e.printStackTrace();
		}
		return com;
	}

}
