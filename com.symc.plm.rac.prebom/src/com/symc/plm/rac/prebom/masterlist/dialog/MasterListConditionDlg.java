package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.kgm.commands.variantconditionset.ConditionVector;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.view.MasterListReq;
import com.symc.plm.rac.prebom.optionedit.dialog.tree.VariantNode;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [20161031][ymjang] 옵션입력 Dialog 창에서 Cancel 버튼을 클릭할 경우, Usage 수량 초기화하지 않도록 수정
 * [20161028][ymjang] TRIM 옵션은 보이지 않게 처리함.
 */
public class MasterListConditionDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable detailTable;
//	private JTable combinationTable;
	private JTable masterListTable;
	private int masterListRow = -1, masterListColumn = -1;
	private MasterListReq parentDlg;
	private JTree tree = null;
	private OptionManager manager = null;
	private List<ConditionVector> conditions = null;
	private TCSession session = null;
	private JList combinationResultList = new JList();
	private Registry registry = null;
	private Vector headerVector = new Vector();
	private int[] columnWidth = {40, 100, 100, 100, 150};
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MasterListConditionDlg dialog = new MasterListConditionDlg(null, null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public MasterListConditionDlg(MasterListReq parentDlg, List<ConditionVector> conditions, OptionManager manager) {
		super((Frame)parentDlg, true);
		this.parentDlg = parentDlg;
		this.conditions = conditions;
		this.manager = manager;
		this.registry = Registry.getRegistry(com.kgm.commands.variantconditionset.ConditionSetDialog.class);
		init();
	}
	
	public void setCellInfo(JTable masterListTable, int masterListRow, int masterListColumn){
		this.masterListTable = masterListTable;
		this.masterListRow = masterListRow;
		this.masterListColumn = masterListColumn;
	}
	
	private void init(){
		setTitle("Condition Set Dialog");
		
		session = CustomUtil.getTCSession();
		initTree();
		
		headerVector.add("USE");
		headerVector.add("CATEGORY");
		headerVector.add("CATEGORY DESC");
		headerVector.add("OPTION CODE");
		headerVector.add("OPTION DESC");
		
		setBounds(100, 100, 819, 452);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_2 = new JPanel();
					panel_2.setMinimumSize(new Dimension(200, 10));
					panel_2.setBorder(new TitledBorder(null, "Enable Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					panel_1.add(panel_2, BorderLayout.WEST);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
//						enableOptionTree.setVisibleRowCount(2);
//						enableOptionTree.setRootVisible(false);
						JScrollPane scrollPane = new JScrollPane(tree);
						scrollPane.setPreferredSize(new Dimension(300, 300));
						panel_2.add(scrollPane, BorderLayout.CENTER);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2, BorderLayout.CENTER);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_3 = new JPanel();
						panel_3.setPreferredSize(new Dimension(60, 10));
						panel_2.add(panel_3, BorderLayout.WEST);
						panel_3.setLayout(new BorderLayout(0, 0));
						{
							JPanel panel_4 = new JPanel();
							panel_4.setPreferredSize(new Dimension(10, 50));
							panel_3.add(panel_4, BorderLayout.NORTH);
						}
						{
							JPanel panel_4 = new JPanel();
							panel_3.add(panel_4, BorderLayout.CENTER);
							{
								JButton btnAddOption = new JButton("");
								btnAddOption.setPreferredSize(new Dimension(50, 40));
								btnAddOption.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent arg0) {
										add();
									}
								});
								btnAddOption.setIcon(registry.getImageIcon("ProuctOptionManageForwardArrow2.ICON"));
								panel_4.add(btnAddOption);
							}
							{
								JButton btnRemoveOption = new JButton("");
								btnRemoveOption.setPreferredSize(new Dimension(50, 40));
								btnRemoveOption.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										remove();
									}
								});
								btnRemoveOption.setIcon(registry.getImageIcon("ProuctOptionManageBackArrow2.ICON"));
								panel_4.add(btnRemoveOption);
							}
						}
					}
					{
						JPanel panel_3 = new JPanel();
						panel_3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Selected Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
						panel_2.add(panel_3, BorderLayout.CENTER);
						panel_3.setLayout(new BorderLayout(0, 0));
						{
							TableModel model = new DefaultTableModel(null, headerVector) {
								public Class getColumnClass(int col) {
									if( col == 0 ){
										return VariantValue.class;
									}
									return String.class;
								}

								public boolean isCellEditable(int row, int col) {
									return false;
								}
						    };
							detailTable = new JTable(model);
							TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
						    detailTable.setRowSorter(sorter);
						    detailTable.addMouseListener(new MouseAdapter(){

								@Override
								public void mouseReleased(MouseEvent e) {
									if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
											&& e.isControlDown()==false) {
										remove();
									}
									super.mouseReleased(e);
								}
								
							});
						    columnInit();
							JScrollPane scrollPane = new JScrollPane(detailTable);
							panel_3.add(scrollPane, BorderLayout.CENTER);
						}
					}
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.SOUTH);
				{
					JButton btnAddRow = new JButton("Add Row");
					btnAddRow.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							try{
								DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
								ConditionVector condition = manager.getConditionSet(model.getDataVector());
								
								//SRME:: [][20140812] 특정 category는 다른 특정 category랑 함께 쓸수없게 제한(special country)
								String tmpStr = condition.toString();
								boolean bFlag = CustomUtil.isCompatibleOptions(session, tmpStr, false);
								if( !bFlag ){
									MessageBox.post(AIFUtility.getActiveDesktop(), "This option includes incompatible.", "INFORMATION", MessageBox.ERROR);
									return;
								}
								
								DefaultListModel listModel = (DefaultListModel)combinationResultList.getModel();
								if( listModel == null ){
									listModel = new DefaultListModel();
								}
								listModel.addElement(condition);
								
								//[SR140722-022][20140522] Condition Sorting
								//[20140626] YunSungWon. 'Or' Sorting
								Enumeration<ConditionVector> enums = (Enumeration<ConditionVector>)listModel.elements();
								if( enums != null && enums.hasMoreElements()){
									
									ArrayList<ConditionVector> list = new ArrayList();
									while(enums.hasMoreElements()){
										ConditionVector v = enums.nextElement();
										list.add(v);
									}
									Collections.sort(list);
									
									listModel.clear();
									for( ConditionVector v : list){
										listModel.addElement(v);
									}
								}

							}catch(Exception e){
								e.printStackTrace();
							}
						}
					});
					panel_1.add(btnAddRow);
				}
				{
					JButton btnDelRow = new JButton("Del Row");
					btnDelRow.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							Object[] selectedObj = combinationResultList.getSelectedValuesList().toArray();
							DefaultListModel listModel = (DefaultListModel)combinationResultList.getModel();
							for( int i = 0; selectedObj != null && i < selectedObj.length; i++){
								listModel.removeElement(selectedObj[i]);
							}
						}
					});
					panel_1.add(btnDelRow);
				}
				{
					JButton btnClear = new JButton("Clear");
					btnClear.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// 컨디션 조합 테이블에 수집된 옵션을 모두 제거함.
							DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
							for( int i = model.getRowCount() - 1; model != null && i >= 0; i--){
								model.removeRow(i);
							}
						}
					});
					panel_1.add(btnClear);
				}
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Combination Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(panel, BorderLayout.SOUTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
//				combinationTable = new JTable();
				DefaultListModel listModel = new DefaultListModel();
				if( conditions != null){
					for( ConditionVector conditionVec : conditions){
						listModel.addElement(conditionVec);
					}
				}
				combinationResultList.setModel(listModel);
				combinationResultList.addMouseListener(new MouseAdapter(){

					@Override
					public void mouseReleased(MouseEvent e) {
						if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
								&& e.isControlDown()==false) {
							
							DefaultTableModel tableModel = (DefaultTableModel)detailTable.getModel();
							for( int i = tableModel.getRowCount() - 1; tableModel != null && i >= 0; i--){
								tableModel.removeRow(i);
							}
							combinationResultList.clearSelection();
							int selectedIdx =  combinationResultList.getAnchorSelectionIndex();
							DefaultListModel listModel = (DefaultListModel)combinationResultList.getModel();
							ConditionVector conditions = (ConditionVector)listModel.get(selectedIdx);
							for( ConditionElement elm : conditions){
								VariantValue value = manager.getValue(elm.option + ":" + elm.value);
								
								if( value == null ){
									MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.notFoundValue"), "INFORMATION", MessageBox.WARNING);
									return;
								}
								VariantOption option = value.getOption();
								Vector row = new Vector();
								row.add(value);
								row.add(option.getOptionName());
								row.add(option.getOptionDesc());
								row.add(value.getValueName());
								row.add(value.getValueDesc());
								tableModel.addRow(row);
							}
						}
						super.mouseReleased(e);
					}
					
				});
				
				JScrollPane scrollPane = new JScrollPane(combinationResultList);
				scrollPane.setPreferredSize(new Dimension(452, 100));
				panel.add(scrollPane, BorderLayout.CENTER);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Object obj = masterListTable.getValueAt(masterListRow, masterListColumn);
						int modelRow = masterListTable.convertRowIndexToModel(masterListRow);
						int modelColumn = masterListTable.convertColumnIndexToModel(masterListColumn);
						DefaultTableModel masterListTableModel = (DefaultTableModel)masterListTable.getModel();
						CellValue cellValue = null;
						if( obj instanceof CellValue){
							CellValue tmpCellValue = (CellValue)obj;
							cellValue = new CellValue(tmpCellValue.getValue(), tmpCellValue.getSortValue(), tmpCellValue.getOrder());
							masterListTableModel.setValueAt(cellValue, modelRow, modelColumn);
						}else{
							cellValue = new CellValue(obj.toString());
							masterListTableModel.setValueAt(cellValue, modelRow, modelColumn);
						}
						
						HashMap<String, Object> dataMap = cellValue.getData();
						if(	dataMap == null ){
							dataMap = new HashMap<String, Object>();
							cellValue.setData(dataMap);
						}
						
						String conditionStr = "";
						Vector<ConditionVector> allCondition = new Vector();
						DefaultListModel listModel = (DefaultListModel)combinationResultList.getModel();
						for( int i = 0; i < listModel.size(); i++){
							ConditionVector conditions = (ConditionVector)listModel.get(i);
							allCondition.add(conditions);
							
							if( i > 0){
								conditionStr += " or ";
							}
							for( int j = 0; j < conditions.size(); j++){
								ConditionElement elm = conditions.get(j);
								
								if( j > 0){
									conditionStr += " and ";
								}
								
								conditionStr += elm.value;
							}
						}
						cellValue.setValue(conditionStr);
						dataMap.put("SPEC_DATA", allCondition);
						
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						// [20161031][ymjang] 옵션입력 Dialog 창에서 Cancel 버튼을 클릭할 경우, Usage 수량 초기화하지 않도록 수정
						Object obj = masterListTable.getValueAt(masterListRow, masterListColumn);
						int modelRow = masterListTable.convertRowIndexToModel(masterListRow);
						int modelColumn = masterListTable.convertColumnIndexToModel(masterListColumn);
						DefaultTableModel masterListTableModel = (DefaultTableModel)masterListTable.getModel();
						CellValue cellValue = null;
						if( obj instanceof CellValue){
							((CellValue)obj).setIsCancel(true);
						} else {
							cellValue = new CellValue(obj.toString(), true);
							masterListTableModel.setValueAt(cellValue, modelRow, modelColumn);
						}
						
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	/**
	 * 컬럼 사이즈 초기화
	 */
	public void columnInit(){
		TableColumnModel columnModel = detailTable.getColumnModel();
		int n = headerVector.size();
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
			columnModel.getColumn(i).setWidth(columnWidth[i]);
		}
		columnModel.removeColumn(columnModel.getColumn(columnModel.getColumnIndex("USE")));
	}

	public List<ConditionVector> getConditions() {
		return conditions;
	}

	public void setConditions(List<ConditionVector> conditions) {
		this.conditions = conditions;
	}

	private JTree initTree(){
		
		ArrayList<VariantOption> enableOptionSet = parentDlg.getEnableOptionSet();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Option Set");
		if( enableOptionSet != null && enableOptionSet.size() > 0){
			for( VariantOption option : enableOptionSet){
				
				String desc = option.getOptionDesc() == null || option.getOptionDesc().equals("") ? "" : " | " + option.getOptionDesc();
				VariantNode optionNode = new VariantNode(option);
				List<VariantValue> values = option.getValues();
				
				//사용가능한 옵션이 존재하는경우만 Option을 추가한다.
				if( values != null && !values.isEmpty() ){
					int enableChildCount = 0;
					for( VariantValue value : values){
						if( value.getValueStatus() == VariantValue.VALUE_USE){
							enableChildCount++;
						}
					}
					if( enableChildCount > 0 ){
						root.add(optionNode);
					}
				}
			}
		}
		
		tree = new JTree(root);
		tree.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseReleased(MouseEvent e) {
				if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
						&& e.isControlDown()==false) {
					add();
				}
				super.mouseReleased(e);
			}
			
		});
		for( int i = 0; i < tree.getRowCount(); i++){
			tree.expandRow(i);
		}
		return tree;
	}
	
	public void setSelectedConditions(List<ConditionVector> conditions){
		this.conditions = conditions;
		
		DefaultListModel model = (DefaultListModel) combinationResultList.getModel();
		for( int i = model.getSize() - 1 ; i >= 0; i--){
			model.remove(i);
		}
		
		DefaultTableModel detailModel = (DefaultTableModel)detailTable.getModel();
		for( int i = detailModel.getRowCount() - 1 ; i >= 0; i--){
			detailModel.removeRow(i);
		}
		
		if( conditions != null){
			for( ConditionVector conditionVec : conditions){
				// [20161028][ymjang] TRIM 옵션은 보이지 않게 처리함.
				if (conditionVec.elementAt(0).option != null && !conditionVec.elementAt(0).option.equals("TRIM")) {
					model.addElement(conditionVec);
				}
			}
		}
	}
	
	/**
	 * Condition 조합테이블에 옵션값을 추가함.
	 */
	private void add(){
		TreePath[] paths = tree.getSelectionPaths();
		for( int i = 0; paths != null && i < paths.length; i++){
			TreePath path = paths[i];
			VariantNode node = (VariantNode)path.getLastPathComponent();
			Object obj = node.getUserObject();
			DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
			if( obj instanceof VariantValue){
				
				VariantValue value = (VariantValue)obj;
				VariantOption option = value.getOption();
				if( isValidAndCheck(value, model.getDataVector())){
					value.setNew(true );
					value.setValueStatus( VariantValue.VALUE_USE);
					Vector row = new Vector();
					row.add( value );
					row.add(option.getOptionName());
					row.add(option.getOptionDesc());
					row.add(value.getValueName());
					row.add(value.getValueDesc());
					
					model.addRow(row);
				}
			}
		}
	}
	
	/**
	 * 해당 옵션이 이미 포함되어 있는지 확인
	 * 
	 * @param value
	 * @param data
	 * @return
	 */
	private boolean isValidAndCheck(VariantValue value, Vector<Vector> data){
		String optionName = value.getOption().getOptionName();
		String valueName = value.getValueName();
		
		for( int i = 0; i < data.size(); i++ ){
			Vector row = data.get(i);
			if( value.equals(row.get(0))){
				if( value.getValueStatus() == VariantValue.VALUE_NOT_DEFINE)
					value.setValueStatus(VariantValue.VALUE_USE);
				return false;
			}else{
				VariantValue val = (VariantValue)row.get(0);
				if( value.getOption().equals(val.getOption())){
					row.removeAllElements();
					row.add( value );
					row.add(value.getOption().getOptionName());
					row.add(value.getOption().getOptionDesc());
					row.add(value.getValueName());
					row.add(value.getValueDesc());
					data.remove(i);
					data.insertElementAt(row, i);
					DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
					model.fireTableDataChanged();
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 추가된 Conditiond을 제거함.
	 */
	private void remove(){
		int[] selectedIdxs = detailTable.getSelectedRows();
		DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
		ArrayList<VariantValue> selectedValues = new ArrayList();
		for( int i = selectedIdxs.length - 1; i >= 0; i--){
			VariantValue value = (VariantValue)model.getValueAt(selectedIdxs[i], 0);
			selectedValues.add(value);
			if( value.isNew() || ( !value.isNew() && !value.isUsing())){
				
				VariantOption option = value.getOption();
				for( int j = model.getRowCount() - 1; j >= 0; j--){
					VariantValue val = (VariantValue)model.getValueAt(j, 0);
					if( val.equals(value)){
						model.removeRow(j);
						break;
					}
				}
				
				//현재의 테이블 모델에서 모든 Value를 모두 빼면 사용되지 않는 옵션은 AllData에서 빼야함.
				boolean bNeedDataRemove = true;
				for( Vector row : (Vector<Vector>)model.getDataVector()){
					if( row.get(1).equals(option.getOptionName())){
						bNeedDataRemove = false;
						break;
					}
				}
				
			}
		}
		
		model.fireTableDataChanged();
	}	
}
