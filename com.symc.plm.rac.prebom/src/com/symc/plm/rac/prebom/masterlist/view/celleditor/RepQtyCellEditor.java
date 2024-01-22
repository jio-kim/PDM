package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.PlainDocument;

import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.TextFieldFilter;
import com.symc.plm.rac.prebom.masterlist.dialog.MasterListDlg;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.view.MasterListReq;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

public class RepQtyCellEditor extends DefaultCellEditor {

	private String selectedUOM = "EA";
	private JTable table = null;
	private int row = -1, column = -1;
	private JTextField tf = null;
	private int eaIdx = -1;
	private JComboBox innerComboBox = null;
	private MasterListReq dlg = null;
	private boolean isOwner = false;
	
	public RepQtyCellEditor(JComboBox comboBox, MasterListReq dlg) {
		super(comboBox);
		this.dlg = dlg;
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 한번 생성한 Item의 UOM은 API를 통해 수정 불가능함.
	 * @return
	 */
	private boolean isModifiable(){
		
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int modelRow = table.convertRowIndexToModel(row);
		CellValue partIdCellValue = (CellValue)model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
		if( partIdCellValue.getValue().equals("")){
			return true;
		}else{
			return false;
		}
//		HashMap data = partIdCellValue.getData();
//		String systemRowKey = (String)data.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
//		ArrayList<TCComponentBOMLine> bomLines = dlg.getBOMLines(systemRowKey);
//		if( bomLines != null && !bomLines.isEmpty()){
//			TCSession session = bomLines.get(0).getSession();
//			try {
//				TCProperty tcProperty = bomLines.get(0).getItem().getTCProperty("owning_user");
//				TCComponentUser owningUser = (TCComponentUser)tcProperty.getReferenceValue();
//				if( owningUser.equals(session.getUser())){
//					return true;
//				}
//			} catch (TCException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return false;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
		// TODO Auto-generated method stub
		this.table = table;
		this.row = row;
		this.column = column;
		
		JComboBox combo = (JComboBox)this.editorComponent;
		innerComboBox = new JComboBox(combo.getModel());
		if( isModifiable()){
			isOwner = true;
			innerComboBox.setEnabled(true);
		}else{
			innerComboBox.setEnabled(false);
		}
		
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int modelRow = table.convertRowIndexToModel(row);
		CellValue partIdCellValue = (CellValue)model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
		HashMap<String, Object> data = partIdCellValue.getData();
		Object uomObj = data.get(PropertyConstant.ATTR_NAME_UOMTAG);
		
		if( uomObj != null){
			selectedUOM = uomObj.toString();
		}
		
		ComboBoxModel comboModel = innerComboBox.getModel();
		for( int i = 0; i < comboModel.getSize(); i++){
			
			if( comboModel.getElementAt(i).equals("EA")){
				eaIdx = i;
			}
			
			if( comboModel.getElementAt(i).equals(selectedUOM)){
				innerComboBox.setSelectedIndex(i);
				break;
			}
		}
//		combo.setSelectedItem(selectedUOM);
		
		final JPanel panel = new JPanel(new BorderLayout(0, 0));
		tf = new JTextField(value.toString());
		tf.setHorizontalAlignment(JLabel.RIGHT);
		tf.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent keyevent) {
				// TODO Auto-generated method stub
				String s = tf.getText();
				try{
					Integer.parseInt(s);
					
					//StopCellEdiing이 호출됨.
//					innerComboBox.setSelectedIndex(eaIdx);
					
					innerComboBox.setEnabled(false);
				}catch(NumberFormatException nfe){
					try{
						Double.parseDouble(s);
						if( isOwner ){
							innerComboBox.setEnabled(true);
						}
					}catch(NumberFormatException nfe2){
						tf.setText("");
					}
				}
			}

		});
		PlainDocument doc = (PlainDocument) tf.getDocument();
		doc.setDocumentFilter(new TextFieldFilter());
		
		panel.add(BorderLayout.CENTER, tf);
		panel.add(BorderLayout.EAST, innerComboBox);
		
		return panel;
	}
	
	@Override
	public boolean shouldSelectCell(EventObject arg0) {
		// TODO Auto-generated method stub
		tf.setFocusable(true);
		int tfLength = tf.getText().length();
		if( tfLength > 0){
			tf.setSelectionStart( 0 );
			tf.setSelectionEnd( tfLength );
		}
		return super.shouldSelectCell(arg0);
	}

	@Override
	public boolean stopCellEditing() {
		// TODO Auto-generated method stub
		if( !innerComboBox.isFocusable() ){
			return false;
		}
		return super.stopCellEditing();
	}

	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int modelRow = table.convertRowIndexToModel(row);
		CellValue partIdCellValue = (CellValue)model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
		HashMap<String, Object> data = partIdCellValue.getData();
		
		data.put(PropertyConstant.ATTR_NAME_UOMTAG, innerComboBox.getSelectedItem().toString());
		
		CellValue repQtyCellValue = null;
		Object obj = model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX);
		if( obj instanceof CellValue){
			repQtyCellValue = (CellValue)obj;
			repQtyCellValue.setValue(tf.getText());
		}else{
			repQtyCellValue = new CellValue(tf.getText());
		}
		
		return repQtyCellValue;
	}
}
