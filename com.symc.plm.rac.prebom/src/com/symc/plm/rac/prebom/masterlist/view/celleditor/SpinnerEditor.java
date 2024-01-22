package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerListModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;

public class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
  final JSpinner spinner = new JSpinner();
  private MasterListTablePanel masterList = null;
  
  public SpinnerEditor(MasterListTablePanel masterList) {
	  this.masterList = masterList;
  }
  
  public SpinnerEditor(String[] items) {
    spinner.setModel(new SpinnerListModel(Arrays.asList(items)));
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
      int row, int column) {
	  
	  String str = null;
	  double num = -1;
	  if( value instanceof CellValue){
		  CellValue cellValue = (CellValue)value;
		  str = cellValue.getValue();
		  HashMap<String, Object> dataMap = cellValue.getData();
	  }else{
		  str = value.toString();
	  }
	  
	  if( str != null && !str.equals("")){
		  num = Double.parseDouble(str);
	  }else{
		  num = 0;
	  }
	  
	  spinner.setValue((int)num);
    return spinner;
  }

  public boolean isCellEditable(EventObject evt) {
    if (evt instanceof MouseEvent) {
      return ((MouseEvent) evt).getClickCount() >= 2;
    }
    return true;
  }

  public Object getCellEditorValue() {
	  
	  int selectedTableRow = masterList.getTable().getSelectedRow();
	  int selectedTableColumn = masterList.getTable().getSelectedColumn();
	  DefaultTableModel model = (DefaultTableModel)masterList.getTable().getModel(); 
	  int selectedRow = masterList.getTable().convertRowIndexToModel(selectedTableRow);
	  int selectedColumn = masterList.getTable().convertColumnIndexToModel(selectedTableColumn);
	  
	  int sum = 0;
	  Object obj = null;
	  int trimCount = masterList.getOspec().getTrimList().size();
	  int fixedCount = masterList.getCurrentPreColumns().size();
	  String str2 = null;
	  CellValue cellValue = null;
	  for( int i = fixedCount - 1; i < trimCount + fixedCount - 1; i++){
		  int col = masterList.getTable().convertColumnIndexToModel(i);
		  obj = model.getValueAt(selectedRow, col);
		  if( obj instanceof CellValue){
			  cellValue = (CellValue)obj;
			  cellValue = new CellValue(cellValue.getValue(), cellValue.getSortValue(), cellValue.getOrder());
		  }else{
			  cellValue = new CellValue(obj.toString());
		  }
		  model.setValueAt(cellValue, selectedRow, col);
		  
		  str2 = cellValue.getValue();
		  boolean isOpt = false;
		  if( str2.indexOf("(") > -1 || str2.indexOf(")") > -1){
			  str2 = str2.replaceAll("\\(", "");
			  str2 = str2.replaceAll("\\)", "");
			  isOpt = true;
		  }
		  
		  if( str2.length() > 0){
			  int result = Integer.parseInt(str2);
			  if( result > 0){
				  obj = spinner.getValue();
				  int num = Integer.parseInt(obj.toString());
				  sum += num;
				  cellValue.setValue(isOpt ? "(" + num + ")":"" + num);
			  }
		  }
		  
	  }
	  masterList.getTable().repaint();
	  
	  obj = spinner.getValue();
	  cellValue = new CellValue(obj.toString());
	  
	  return cellValue;
  }
}