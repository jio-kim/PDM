package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerListModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;

public class SpinnerCheckEditor extends AbstractCellEditor implements TableCellEditor {
  final JSpinner spinner = new JSpinner();
//  JTable table = null;
  private MasterListTablePanel masterList = null;
  private JTable table = null;
  private int row = -1, column = -1;
  boolean isOpt = false;
  private JCheckBox chBox = null;
  
  public SpinnerCheckEditor(MasterListTablePanel masterList) {
	  this.masterList = masterList;
  }
  
  public SpinnerCheckEditor(String[] items) {
    spinner.setModel(new SpinnerListModel(Arrays.asList(items)));
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
      int row, int column) {
	  
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
	  
	  if( str != null && !str.equals("")){
		  num = Double.parseDouble(str);
	  }else{
		  num = 0;
	  }
	  
	  chBox = new JCheckBox();
	  chBox.setSelected(isOpt);
	  spinner.setValue((int)num);
	  JPanel panel = new JPanel(new BorderLayout(0, 0));
	  panel.add(spinner, BorderLayout.CENTER);
	  panel.add(chBox, BorderLayout.WEST);
	  
	  return panel;
  }

  public boolean isCellEditable(EventObject evt) {
    if (evt instanceof MouseEvent) {
      return ((MouseEvent) evt).getClickCount() >= 2;
    }
    return true;
  }

  public Object getCellEditorValue() {
	  
//	  CellValue cellValue = null;
//	  Object obj = spinner.getValue();
//	  if( chBox.isSelected()){
//		  cellValue = new CellValue("(" + obj.toString() + ")");
//	  }else{
//		  cellValue = new CellValue(obj.toString());
//	  }
//	  
//	  return cellValue;
//	  int selectedTableRow = masterList.getTable().getSelectedRow();
//	  int selectedTableColumn = masterList.getTable().getSelectedColumn();
//	  DefaultTableModel model = (DefaultTableModel)masterList.getTable().getModel(); 
//	  int selectedRow = masterList.getTable().convertRowIndexToModel(selectedTableRow);
//	  int selectedColumn = masterList.getTable().convertColumnIndexToModel(selectedTableColumn);
//	  String str = null;
//	  
//	  Object obj = null;
//	  int trimCount = masterList.getOspec().getTrimList().size();
//	  int fixedCount = masterList.getCurrentPreColumns().size();
//	  String str2 = null;
//	  int sum = 0;
//	  for( int i = fixedCount - 1; i < trimCount + fixedCount - 1; i++){
//		  int col = masterList.getTable().convertColumnIndexToModel(i);
//		  if( col == selectedColumn){
//			  obj = spinner.getValue();
//		  }else{
//			  obj = model.getValueAt(selectedRow, col);  
//		  }
//		  if( obj instanceof CellValue){
//			  CellValue cellValue = (CellValue)obj;
//			  str2 = cellValue.getValue();
//		  }else{
//			  str2 = obj.toString();
//		  }
//		  str2 = str2.replaceAll("\\(", "");
//		  str2 = str2.replaceAll("\\)", "");
//		  if( str2.equals("")){
//			  str2 = "0";
//		  }
//		  int result = Integer.parseInt(str2);
//		  sum += result;
//	  }
	  
//	  masterList.getTable().getModel().setValueAt(new CellValue(sum + "", sum + "", 0), selectedRow, trimCount + masterList.getFixedColumnPre().length - 1);
//	  masterList.getTable().repaint();
	  
	  int result = 0;
	  Object obj = spinner.getValue();
	  try{
		  result = Integer.parseInt(obj.toString());
	  }catch(NumberFormatException nfe){
		  result = 0;
	  }
	  
	  if( result < 1){
		  return new CellValue("");
	  }else{
		  if( chBox.isSelected()){
			  return new CellValue("(" + result + ")");
		  }else{
			  return new CellValue(result + "");
		  }
	  }
  }
}