package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;

import com.symc.plm.rac.prebom.common.util.TextFieldFilter;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;

public class UsageCellEditor extends DefaultCellEditor {

		private JTextField tf = new JTextField();
	  private JTable table = null;
	  private int row = -1, column = -1;
	  boolean isOpt = false;
	  private JCheckBox chBox = null;
	  
	public UsageCellEditor(JTextField arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		  String str = tf.getText();
		  
		  if( chBox.isSelected()){
			  return new CellValue("(" + str + ")");
		  }else{
			  return new CellValue(str + "");
		  }
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		this.table = table;
		  this.row = row;
		  this.column = column;
		  
		  isOpt = false;
		  
		  String str = null;
		  double num = -1;
		  if( value instanceof CellValue){
			  CellValue cellValue = (CellValue)value;
			  str = cellValue.getValue();
			  HashMap<String, Object> dataMap = cellValue.getData();
		  }else{
			  str = value.toString();
		  }
		  
		  if( str.indexOf("(") > -1 || str.indexOf(")") > -1){
			  isOpt = true;
		  }
		  
		  str = str.replaceAll("\\(", "");
		  str = str.replaceAll("\\)", "");
		  
		  String usageStr = "";
		  if( str != null && !str.equals("")){
			  try{
				  num = Integer.parseInt(str);
				  usageStr = (int)num + "";
			  }catch(NumberFormatException nfe){
				  num = Double.parseDouble(str);
				  usageStr = num + "";
			  }
		  }else{
			  num = 0;
		  }
		  
		  chBox = new JCheckBox();
		  chBox.setSelected(isOpt);
		  
		  tf.setText(num == 0 ? "" : usageStr);
		  PlainDocument doc = (PlainDocument) tf.getDocument();
		  doc.setDocumentFilter(new TextFieldFilter());
			
		  JPanel panel = new JPanel(new BorderLayout(0, 0));
		  panel.add(tf, BorderLayout.CENTER);
		  panel.add(chBox, BorderLayout.WEST);
		  tf.setFocusable(true);
		  return panel;
	}

}
