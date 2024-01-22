package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.toedter.calendar.JDateChooser;

public class DateCellEditor extends DefaultCellEditor {

	private JDateChooser dateChooser = null;
	
	public DateCellEditor(JTextField textField) {
		super(textField);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
		Date date = dateChooser.getDate();
		return sdf.format(date);
//		return dateChooser.getSpinner().getValue();
	}

	@Override
	public Component getTableCellEditorComponent(JTable jtable, Object obj,
			boolean flag, int i, int j) {
		// TODO Auto-generated method stub
//		return super.getTableCellEditorComponent(jtable, obj, flag, i, j);
		if( dateChooser == null ){
			dateChooser = new JDateChooser(null, "yyyy-MM-dd", false, null);
		}
		
		String dateStr = "";
		if( obj instanceof CellValue){
			CellValue cellValue = (CellValue)obj;
			dateStr = cellValue.getValue();
		}else{
			dateStr = obj.toString();
		}
		
		Date date = null;
		SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
		if( !dateStr.equals("")){
			try {
				date = sdf.parse(dateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				return dateChooser;
			}
			dateChooser.setDate(date);
		}
		
		return dateChooser;
	}


}
