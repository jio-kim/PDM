package com.kgm.commands.ec.eco.module;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *  모듈BOM 검증 결과 테이블에 각 Cell에 대한 렌더러
 * [SR140722-022][20140704] swyoon 모듈BOM 검증 결과 테이블에 각 Cell에 대한 렌더러
 */
@SuppressWarnings({"serial", "unused"})
public class ModuleBomTableCellRenderer extends DefaultTableCellRenderer {

	private HashMap<String, ArrayList<ModuleBomValidationInfo>> validationResultMap = null;
	private ArrayList<String> validationErrorList = null;
	private ArrayList<String> validationWarningList = null;
	
	public ModuleBomTableCellRenderer(ArrayList<String> validationErrorList, ArrayList<String> validationWarningList){
		this.validationErrorList = validationErrorList;
		this.validationWarningList = validationWarningList;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if(comp instanceof JLabel){
			JLabel label = (JLabel)comp;
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			int modelRow = table.convertRowIndexToModel(row);
			int modelColumn = table.convertColumnIndexToModel(column);
			String eplId = (String)model.getValueAt(modelRow, 6);
			String noStr = (String)model.getValueAt(modelRow, 0);
			if( validationErrorList.contains(eplId)){
				label.setForeground(Color.RED);
			}else if(validationWarningList.contains(eplId)){
				label.setForeground(new Color(63, 72, 204));
			}else{
				label.setForeground(Color.BLACK);
			}
			
			if( !isSelected){
				if( ((modelRow/2) % 2) == 1 ){
					label.setBackground(new Color(192, 214, 248));
				}else{
					label.setBackground(Color.WHITE);
				}
			}

			if( modelColumn == 0 || modelColumn == 3 || modelColumn == 4){
				label.setHorizontalAlignment(CENTER);
			}			
			
			return label;
		}
		return comp;
	}

}
