package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.kgm.commands.variantconditionset.ConditionVector;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.symc.plm.rac.prebom.masterlist.dialog.MasterListConditionDlg;
import com.symc.plm.rac.prebom.masterlist.dialog.MasterListDlg;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;

public class PartConditionCellEditor extends DefaultCellEditor {

	private MasterListDlg masterListDlg;
	private MasterListConditionDlg masterListConditionDlg;
	private JTable table = null;
	private int row = -1, column = -1;
	
	public PartConditionCellEditor(MasterListDlg masterListDlg, JTextField tf, MasterListConditionDlg masterListConditionDlg){
		super(tf);
		this.masterListDlg = masterListDlg;
		this.masterListConditionDlg = masterListConditionDlg;
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
		// TODO Auto-generated method stub
		JTextField tf = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
		this.table = table;
		this.row = row;
		this.column = column;
		
		int modelRow = table.convertRowIndexToModel(row);
		CellValue cellValue = (CellValue)((DefaultTableModel)table.getModel()).getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
		HashMap cellData = cellValue.getData();
		String key = (String)cellData.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
		ArrayList<TCComponentBOMLine> lines = masterListDlg.getBOMLines(key);
		OptionManager manager = masterListDlg.getOptionManager();
		if( manager != null && lines != null && lines.size() > 0){
			try {
				List<ConditionVector> conditions = manager.getConditionSet(lines.get(0));
				masterListConditionDlg.setConditions(conditions);
				masterListConditionDlg.setSelectedConditions(conditions);
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		masterListConditionDlg.setCellInfo(table, row, column);
		masterListConditionDlg.setVisible(true);
		tf.setEditable(false);
		Object obj = table.getValueAt(row, column);
		tf.setText(obj.toString());
		return tf;
	}
	
	public Object getCellEditorValue() {
		return table.getValueAt(row, column);
	}
}
