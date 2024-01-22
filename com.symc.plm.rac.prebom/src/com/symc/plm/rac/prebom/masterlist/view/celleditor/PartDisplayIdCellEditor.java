package com.symc.plm.rac.prebom.masterlist.view.celleditor;

import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.util.WebUtil;
import com.symc.plm.rac.prebom.masterlist.view.MasterListReq;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
/**
 * [20170215] Carry Over REPLACE 일 경우, Carry Over Id 로 변경 가능하도록 함
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 */
public class PartDisplayIdCellEditor extends DefaultCellEditor{
	
	private JTable table = null;
	private MasterListReq parentDlg = null;
	private MasterListTablePanel masterListTablePanel;
	
	public PartDisplayIdCellEditor(MasterListReq parentDlg, MasterListTablePanel masterListTablePanel){
		super(new JTextField());
		this.parentDlg = parentDlg;
		this.masterListTablePanel = masterListTablePanel;
		this.table = masterListTablePanel.getTable();
	}
	JTextField tf = null;
	int row = -1;
	
	@Override
	public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
		// TODO Auto-generated method stub
		tf = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
		this.row = row;
		return tf;
	}

	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		Object obj = super.getCellEditorValue();
		if( obj.equals("")){
			return obj;
		}
		
		String str = (String)obj;
		str = str.replaceAll(" ", "");
		//기존에 존재하는 Item ID 인지 확인.
		HashMap<String, String> resultMap = null;
		try {
			resultMap = WebUtil.getPart(str);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
			return "";
		}
		
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int modelRow = table.convertRowIndexToModel(row);
		CellValue partIdCellValue = null;
		Object partIdObj = model.getValueAt(modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
		if( resultMap == null || resultMap.isEmpty() ){
			if( partIdObj instanceof CellValue){
				partIdCellValue = (CellValue)partIdObj;
				//존재하는 Pre-Vehcle Part이면 삭제되면 안됨. 
				try {
					TCComponentItemRevision childRevision = BomUtil.findLatestItemRevision(TypeConstant.S7_PREVEHICLEPARTTYPE, partIdCellValue.getValue());
					if( childRevision == null){
						partIdCellValue.setValue("");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
					return "";
				}
				
			}else{
				MessageBox.post((Window)parentDlg, "Invalid Part ID Cell Type", "ERROR", MessageBox.ERROR);
				return "";
			}
			table.repaint();
			return obj;
		}else{
			int result = JOptionPane.showConfirmDialog(
					(Window)parentDlg,
				    "The pre-registration part No. Are you sure you want to 'Carry Over'?",
				    "Part No Check",
				    JOptionPane.YES_NO_OPTION);
			if( result == JOptionPane.YES_OPTION){
				try {
					String key = masterListTablePanel.getKeyInModel(modelRow);
					ArrayList<TCComponentBOMLine> lists = parentDlg.getBOMLines(key);
					
					TCSession session = CustomUtil.getTCSession();
					TCComponentItem item = (TCComponentItem)session.stringToComponent(resultMap.get("PUID"));
					TCComponentItemRevision revision = item.getLatestItemRevision();
					String systemCode = "";
					if(lists != null && lists.size() > 0){
						systemCode = lists.get(0).getProperty(PropertyConstant.ATTR_NAME_BL_BUDGETCODE);
					}
					String itemName = item.getProperty("object_name");
					String itemId = item.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
					String dispPartNo = revision.getProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO);
					String projectCode = revision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
					if( partIdObj instanceof CellValue){
						partIdCellValue = (CellValue)partIdObj;
						
						//Part ID(Unique NO)가 없는 경우에만 입력됨.
						//if( partIdCellValue.getValue().trim().equals("")){
						//	partIdCellValue.setValue(itemId);
						//}
						//[20170215] Carry Over REPLACE 일 경우, Carry Over Id 로 변경 가능하도록 함  
						partIdCellValue.setValue(itemId);
					}else{
						MessageBox.post((Window)parentDlg, "Invalid Part ID Cell Type", "ERROR", MessageBox.ERROR);
						return "";
					}
					
//					model.setValueAt(itemId, modelRow, MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
					model.setValueAt(systemCode, modelRow, MasterListTablePanel.MASTER_LIST_SYSTEM_IDX);
					obj = new CellValue(dispPartNo);
					model.setValueAt(itemName, modelRow, MasterListTablePanel.MASTER_LIST_PART_NAME_IDX);
					model.setValueAt(new CellValue("C"), modelRow, MasterListTablePanel.MASTER_LIST_NMCD_IDX);
					model.setValueAt(projectCode, modelRow, MasterListTablePanel.MASTER_LIST_PROJECT_IDX);
				} catch (TCException e) {
					e.printStackTrace();
					MessageBox.post((Window)parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
					return "";
				}finally{
					table.repaint();
				}
				return obj;
			}else if(result == JOptionPane.NO_OPTION){
				return "";
			}
		}
		return obj;
	}
	
};
