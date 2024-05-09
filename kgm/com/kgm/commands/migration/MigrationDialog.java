package com.kgm.commands.migration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.kgm.common.SYMCClass;
import com.kgm.common.randerer.TableRenderer;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.ExcelService;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemType;

public class MigrationDialog extends MasterMigrationDialog {

	private static final long serialVersionUID = 1L;

	private DefaultTableModel defaultTableModel;

	public static final int PRODUCT = 0;
	public static final int RAW_MATERIALS = 1;
	public static final int SUB_MATERIALS = 2;
	public static final int MOLD_FORMER = 3;
	public static final int MOLD_TAPPING = 4;
	public static final int MOLD_TAP = 5;
	public static final int MOLD_ETC = 6;
	public static final int EQUIPMENT_FORMER = 7;
	public static final int EQUIPMENT_TAPPING = 8;
	public static final int EQUIPMENT_ETC = 9;
	public static final int PROCESSING = 10;

	public MigrationDialog(MigrationCommand command, Frame feame) {
		super(command, feame, CustomUtil.getTCSession());
		applyButton.setVisible(false);
	}

	@SuppressWarnings("rawtypes")
    protected void comboBoxStateChanged(String actionCommand, JComboBox comboBox) {
		if (actionCommand.equals("MigrationType")) {
			clearTable();
			columns = registry.getStringArray(className + "." + comboBox.getSelectedIndex() + ".Columns");
			table.setModel(new DefaultTableModel(columns, 0));
			columnsWidth = registry.getStringArray(className + "." + comboBox.getSelectedIndex() + ".ColumnsWidth");
			for (int i = 0; i < table.getColumnCount(); i++) {
				TableColumn column = table.getColumnModel().getColumn(i);
				column.setPreferredWidth(Integer.parseInt(columnsWidth[i]));
				column.setCellRenderer(new TableRenderer());
			}
		}
		defaultTableModel = (DefaultTableModel)table.getModel();

		// 필수 입력 항목은 테이블 헤더에 빨간 색으로 표시~
		TableColumnModel columnModel = table.getColumnModel();
		Border border1 = UIManager.getBorder("TableHeader.cellBorder");

		JLabel[] labels = new JLabel[columnModel.getColumnCount()];

		for(int i=0; i<columns.length; i++){
			labels[i] = new JLabel(columns[i], JLabel.CENTER);
			labels[i].setBorder(border1);
			columnModel.getColumn(i).setHeaderRenderer(new TableHeaderRenderer());
			columnModel.getColumn(i).setHeaderValue(labels[i]);
		}

		String[] arrs = registry.getStringArray(className + "." + comboBox.getSelectedIndex() + ".MandatoryCheckColumns");
		int[] mandatoryColumns = CustomUtil.getStringArrayToIntArray(arrs);
		if(mandatoryColumns != null && mandatoryColumns.length !=0){
			for(int i=0; i<mandatoryColumns.length; i++){
				JLabel label = (JLabel)columnModel.getColumn(mandatoryColumns[i]).getHeaderValue();
				label.setForeground(Color.RED);
			}
		}
		// ~필수 입력 항목은 테이블 헤더에 빨간 색으로 표시
	}

	protected void upload(File file) {
		clearTable();
		int startRow = 2;
		Vector<String[]> vector = ExcelService.importExcel(file, registry.getString(className + "." + comboBox.getSelectedIndex() + ".SheetName"), startRow);
		for (int i = 0; i < vector.size(); i++) {
			((DefaultTableModel)table.getModel()).addRow(vector.elementAt(i));
		}
		progressBar.setMaximum(vector.size());
	}

	@Override
	protected boolean check() {
		boolean checkResult1 = false;
		boolean checkResult2 = false;
		boolean checkResult3 = false;

		if(comboBox.getSelectedIndex() == 11){ // 제품 도면  첨부의 경우
			okButton.setVisible(true);
			return true;
		}
		if(comboBox.getSelectedIndex() == 12){ // BOM일 경우
			checkResult1 = checkMandatoryField();
			checkResult3 = true;
			if(checkResult1 && checkResult3){
				okButton.setVisible(true);
				return true;
			}
			else{
				okButton.setVisible(false);
				return false;
			}
		}
		else{
			checkResult2 = checkDuplicate();

			if(checkResult2){
				okButton.setVisible(true);
				return true;
			}
			else{
				okButton.setVisible(false);
				return false;
			}
		}
	}

	@SuppressWarnings("unused")
    private boolean checkExistItem(){
		boolean check = true;
		waitProgress.setStatus("Item 존재 여부 검사 시작...", true);

		for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
			String parent = (String)defaultTableModel.getValueAt(i, 2);
			String parent_item_type = (String)defaultTableModel.getValueAt(i, 3);
			if(!parent_item_type.equals("10")){
				if(findParentItem(parent, i) == null){
					check = false;
					defaultTableModel.setValueAt(new String("Fail"), i, defaultTableModel.getColumnCount() - 2);
					defaultTableModel.setValueAt(parent + " is Not Exist", i, defaultTableModel.getColumnCount() - 1);
				}
				else{
					defaultTableModel.setValueAt(new String("OK"), i, defaultTableModel.getColumnCount() - 2);
				}
			}
		}

		for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
			String child = (String)defaultTableModel.getValueAt(i, 4);
			String child_item_type = (String)defaultTableModel.getValueAt(i, 5);
			if(!child_item_type.equals("10")){
				if(findChildItem(child, i) == null){
					check = false;
					defaultTableModel.setValueAt(new String("Fail"), i, defaultTableModel.getColumnCount() - 2);
					defaultTableModel.setValueAt(child + " is Not Exist", i, defaultTableModel.getColumnCount() - 1);
				}
				else{
					defaultTableModel.setValueAt(new String("OK"), i, defaultTableModel.getColumnCount() - 2);
				}
			}
		}
		table.repaint();
		waitProgress.setStatus("Item 존재 여부 검사 완료...", true);
		return check;
	}

	private TCComponent[] findParentItem(String parent, int row){
		try {
			String parent_item_type = (String)defaultTableModel.getValueAt(row, 3);

			int parent_type = Integer.parseInt(parent_item_type.trim());

			TCComponent[] queryResult = null;
			switch (parent_type) {
			case PRODUCT:
				queryResult = CustomUtil.queryComponent(SYMCClass.QryItemSearch, new String[]{"ItemID"}, new String[]{parent});
				break;
			}
			return queryResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private TCComponent[] findChildItem(String child, int row){
		try {
			String child_item_type = (String)defaultTableModel.getValueAt(row, 5);

			int child_type = Integer.parseInt(child_item_type.trim());

			TCComponent[] queryResult = null;
			switch (child_type) {
			case PRODUCT:
				queryResult = CustomUtil.queryComponent(SYMCClass.QryItemSearch, new String[]{"ItemID"}, new String[]{child});
				break;
			}
			return queryResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected boolean checkMandatoryField() {
		String[] arrs = registry.getStringArray(className + "." + comboBox.getSelectedIndex() + ".MandatoryCheckColumns");
		int[] mandatoryColumns = CustomUtil.getStringArrayToIntArray(arrs);
		boolean check = true;
		waitProgress.setStatus("필수 입력 항목 검사 시작...", true);
		for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
			for (int j = 0; j < mandatoryColumns.length; j++) {
				String columValue = (String)defaultTableModel.getValueAt(i, mandatoryColumns[j]);
				if (columValue == null || columValue.equals("") || columValue.length() == 0) {
					check = false;
					defaultTableModel.setValueAt(new String("Fail"), i, defaultTableModel.getColumnCount() - 2);
					defaultTableModel.setValueAt(defaultTableModel.getColumnName(mandatoryColumns[j]) + " is Empty", i, defaultTableModel.getColumnCount() - 1);
				}
			}
		}
		table.repaint();
		waitProgress.setStatus("필수 입력 항목 검사 완료...", true);

		return check;
	}

	protected boolean checkDuplicate() {
		boolean check = true;
		waitProgress.setStatus("중복 체크 시작...", true);
		for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
			String id = (String)defaultTableModel.getValueAt(i, 0);
			if (isDuplicated(id)) {
				check = false;
				defaultTableModel.setValueAt(new String("Fail"), i, defaultTableModel.getColumnCount() - 2);
				defaultTableModel.setValueAt(defaultTableModel.getColumnName(0) + " is Duplicated", i, defaultTableModel.getColumnCount() - 1);
			}
			table.repaint();
		}
		waitProgress.setStatus("중복 체크 완료...", true);
		return check;
	}

	private boolean isDuplicated(String id) {
		try {
			TCComponentItemType type = (TCComponentItemType)session.getTypeComponent("Item");
			//TCComponentItem item = type.find(id);
			TCComponentItem[] items = type.findItems(id);
			if(items == null || items.length == 0)
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Migration 대상 Type별 Operation 처리.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2012. 12. 24.
	 * @override
	 * @see com.kgm.commands.migration.MasterMigrationDialog#createOperation()
	 * @return
	 */
	protected AbstractAIFOperation createOperation() {
		if (comboBox.getSelectedIndex() == 1) {
			operation = new ProductMigrationOperation(this, registry, session, table, checkBox.isSelected(), progressBar);
		} 

		return operation;
	}

	/**
	 * 테이블 헤더에 필수 입력 항목은 빨간 색으로 표시하기 위한 렌더러
	 * @Copyright : S-PALM
	 * @author   : 이정건
	 * @since    : 2012. 6. 25.
	 * Package ID : com.pungkang.commands.migration.MigrationDialog.java
	 */
	private class TableHeaderRenderer extends DefaultTableCellRenderer{

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return (JComponent)value;
		}
	}
}
