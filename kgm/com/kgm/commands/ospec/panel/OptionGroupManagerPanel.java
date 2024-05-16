package com.kgm.commands.ospec.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.kgm.commands.ospec.OSpecMainDlg;
import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpGroup;
import com.kgm.commands.ospec.op.OpValueName;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.ui.mergetable.AttributiveCellTableModel;
import com.kgm.common.ui.mergetable.CellSpan;
import com.kgm.common.ui.mergetable.MultiSpanCellTable;
import com.teamcenter.rac.util.MessageBox;
import javax.swing.ImageIcon;

public class OptionGroupManagerPanel extends JPanel {

	private JCheckBox chckbxOnlyOwnOption = null;
	private JTextField tfFilter;
	private ArrayList<OpGroup> filteredList = new ArrayList();
	private JTable opGroupTable;
	private JButton btnSave = null;
	private JTextField tfNameGroup;
	private JTable opgSelectedTable;
	private ArrayList<OpGroup> opGroupList = new ArrayList();
	private MultiSpanCellTable enableOptionTable;
	private Vector<Vector<String>> opgSelectedData = new Vector();
	private ArrayList<OpValueName> opgSelectedValueName = new ArrayList();;
	private OSpecMainDlg parentDlg;
	private OSpec ospec;
	private String userID;
	private HashMap<String, ArrayList<OpValueName>> categoryMap = new HashMap();
	private JButton btnGroupSave = null; // Group 명, 비고 변경
	
	private static int COLUMN_OPGROUP_OBJ_INDEX = 3; //opGroupTable 의 OpGroup Object 정보를 저장하는 컬럼 Index 
	private static int COLUMN_OPGROUP_DESC_INDEX = 2; //opGroupTable 의 Description 컬럼 Index 
	private static int COLUMN_OPGROUP_NAME_INDEX = 1; //opGroupTable 의 GROUP NAME 컬럼 Index 
	
	public OptionGroupManagerPanel(OSpecMainDlg parentDlg) throws Exception {
		this.parentDlg = parentDlg;
		this.ospec = parentDlg.getOspec();
		this.userID = parentDlg.getUserID();
//		JPanel optionGroupManagerPanel = new JPanel();
		
		ArrayList<OpValueName> opNameList = ospec.getOpNameList();
		for( OpValueName op:opNameList){
			
			ArrayList<OpValueName> opNames = categoryMap.get(op.getCategory());
			if( opNames == null ){
				opNames = new ArrayList();
				opNames.add(op);
				categoryMap.put(op.getCategory(), opNames);
			}else{
				if( !opNames.contains(op)){
					opNames.add(op);
				}
			}
		}
		
		init();
	}
	
	private void init() throws Exception{
		setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Option Groups", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1);
				panel_1.setLayout(new BorderLayout(5, 0));
				{
					JPanel panel_2 = new JPanel();
					panel_2.setMinimumSize(new Dimension(100, 10));
					panel_1.add(panel_2, BorderLayout.CENTER);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_3 = new JPanel();
						panel_2.add(panel_3, BorderLayout.NORTH);
						panel_3.setLayout(new GridLayout(2, 1, 0, 0));
						{
							JPanel panel_4 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) panel_4.getLayout();
							flowLayout.setAlignment(FlowLayout.LEADING);
							panel_3.add(panel_4);
							{
								chckbxOnlyOwnOption = new JCheckBox("Only own Option Groups");
								chckbxOnlyOwnOption.addActionListener(new ActionListener() {
									
									@Override
									public void actionPerformed(ActionEvent event) {
										// TODO Auto-generated method stub
										try {
											setOpGroup();
											tfFilter.setText("");
											filteredList.clear();
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
											MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
										}
									}
								});
								chckbxOnlyOwnOption.setSelected(true);
								panel_4.add(chckbxOnlyOwnOption);
							}
						}
						{
							JPanel panel_4 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) panel_4.getLayout();
							flowLayout.setAlignment(FlowLayout.LEADING);
							panel_3.add(panel_4);
							{
								JLabel lblNewLabel_3 = new JLabel("Filter : ");
								lblNewLabel_3.setIcon(new ImageIcon(OptionGroupManagerPanel.class.getResource("/icons/filter_allocation_type_16.png")));
								panel_4.add(lblNewLabel_3);
							}
							{
								tfFilter = new JTextField();
								panel_4.add(tfFilter);
								tfFilter.addKeyListener(new KeyAdapter() {
									@Override
									public void keyTyped(KeyEvent keyevent) {
										if( keyevent.getKeyChar() == '\n'){
											
											OptionGroupManagerPanel.this.parentDlg.removeAllRow(opGroupTable);
											filteredList.clear();
											
											String filterStr = tfFilter.getText().trim().toUpperCase();
											DefaultTableModel model = (DefaultTableModel)opGroupTable.getModel();
											for( OpGroup group : opGroupList ){
												Vector row = new Vector();
												//row.add(group.getOwner());
												//row.add(group);
												
												//2016-05-30 수정: OP GROUP Column 변경
												row.add(group.getOwner());
												row.add(group.getOpGroupName());
												row.add(group.getDesciption());
												row.add(group);
												row.add(group.getCondition());
												
												if( !filterStr.equals("")){
													if( group.getOpGroupName().indexOf(filterStr) > -1){
														if( chckbxOnlyOwnOption.isSelected()){
															if( group.getOwner().equals(userID)){
																filteredList.add(group);
																model.addRow(row);
															}
														}else{
															filteredList.add(group);
															model.addRow(row);
														}
														
													}
												}else{
													if( chckbxOnlyOwnOption.isSelected()){
														if( group.getOwner().equals(userID)){
															model.addRow(row);
														}
													}else{
														model.addRow(row);
													}
												}
											}
										}
									}
								});
								tfFilter.setColumns(10);
							}
						}
					}
					{
						JPanel panel_3 = new JPanel();
						panel_2.add(panel_3, BorderLayout.CENTER);
						panel_3.setLayout(new BorderLayout(0, 0));
						{
							//2016-05-30 수정: 컬럼 변경
							Vector header = new Vector();
							header.add("Owner");
							header.add("Option Group");
							header.add("Description");
							header.add("OpGroupObj");
							header.add("Condition");

							Vector data = getOptionGroupModelData(false);
							
							DefaultTableModel model = new DefaultTableModel(data, header);
							opGroupTable = new JTable(model);
							TableColumnModel cm = opGroupTable.getColumnModel();
							
							//int[] width = new int[]{50, 120, 120};
							//2016-05-30 수정: Description 컬럼 추가
							int[] width = new int[]{55, 120, 140, 0, 400};
						    for( int i = 0; i < cm.getColumnCount(); i++){						    	
						    	cm.getColumn(i).setPreferredWidth(width[i]);
						    }
						    cm.getColumn(COLUMN_OPGROUP_OBJ_INDEX).setMinWidth(0);
						    cm.getColumn(COLUMN_OPGROUP_OBJ_INDEX).setMaxWidth(0);
						    
							ListSelectionModel selectionModel = opGroupTable.getSelectionModel();
							selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

							selectionModel.addListSelectionListener(new ListSelectionListener() {
								public void valueChanged(ListSelectionEvent e) {
									
									parentDlg.removeAllRow(opgSelectedTable);
									
									int rowIdx = opGroupTable.getSelectedRow();
									int modelIdx = opGroupTable.convertRowIndexToModel(rowIdx);
									if( modelIdx < 0){
										return;
									}
									
									opgSelectedValueName.clear();
									DefaultTableModel model = (DefaultTableModel)opGroupTable.getModel();
									
									//Object obj = model.getValueAt(modelIdx, 1);
									//2016-05-30 수정: opGroup Column Index 변경
									Object obj = model.getValueAt(modelIdx, COLUMN_OPGROUP_OBJ_INDEX);
									if( !(obj instanceof OpGroup)){
										return;
									}
									//2016-05-30 수정: opGroup Column Index 변경
									//OpGroup group = (OpGroup)model.getValueAt(modelIdx, 1);
									OpGroup group = (OpGroup)model.getValueAt(modelIdx, COLUMN_OPGROUP_OBJ_INDEX);
									
//									if( group.isChanged() && group.getOptionList() == null)
//										return;
									
									if( group.getOwner().equals(userID)){
										btnSave.setEnabled(true);
										btnGroupSave.setEnabled(true);
									}else{
										btnSave.setEnabled(false);
										btnGroupSave.setEnabled(false);
									}
									
									ArrayList<OpValueName> list = null;
									if( group.isChanged()){
										list = group.getOptionList();
									}else{
										try {
											list = parentDlg.getOptionGroupDetail(group.getOpGroupName(), chckbxOnlyOwnOption.isSelected());
											group.setOptionList(list);
										} catch (Exception e1) {
											// TODO Auto-generated catch block
											MessageBox.post(parentDlg, e1.getMessage(), "ERROR", MessageBox.ERROR);
											return;
										}
									}
									
									DefaultTableModel opgSelectedModel = (DefaultTableModel)opgSelectedTable.getModel();
									for (int i = 0; list != null && i < list.size(); i++) {
										Vector row = new Vector();
										row.add(ospec.getProject());
										row.add(list.get(i).getOptionName());
										row.add(list.get(i));
										opgSelectedModel.addRow(row);
										opgSelectedValueName.add(list.get(i));
									}
								}

							});
							opGroupTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
							JScrollPane pane = new JScrollPane(opGroupTable);
							panel_3.add(pane);
						}
					}
					{
						JPanel panel_3 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						panel_2.add(panel_3, BorderLayout.SOUTH);
						{
							tfNameGroup = new JTextField();
							panel_3.add(tfNameGroup);
							tfNameGroup.setColumns(10);
						}
						{
							JButton btnNewButton_4 = new JButton("");
							btnNewButton_4.setPreferredSize(new Dimension(25, 25));
							btnNewButton_4.setIcon(new ImageIcon(OptionGroupManagerPanel.class.getResource("/com/teamcenter/rac/aif/images/add_16.png")));
							btnNewButton_4.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									
									String opGroupName = tfNameGroup.getText();
									if(opGroupName == null || opGroupName.trim().equals("")){
										return;
									}
									
									parentDlg.removeAllRow(opgSelectedTable);
									
									DefaultTableModel model = (DefaultTableModel)opGroupTable.getModel();
									opGroupTable.clearSelection();
									
									if( opGroupName!=null ){
										opGroupName = opGroupName.toUpperCase();
										OpGroup opGroup = new OpGroup(opGroupName, userID,"", "");
										if( !opGroupList.contains(opGroup)){
											opGroupList.add(opGroup);
											
											Vector row = new Vector();
											//row.add(opGroup.getOwner());
											//row.add(opGroup);
											
											//2016-05-30 수정: OP GROUP Column 변경
											
											row.add(opGroup.getOwner());
											row.add(opGroup.getOpGroupName());
											row.add(opGroup.getDesciption());
											row.add(opGroup);
											
											model.addRow(row);

											opGroupTable.setRowSelectionInterval(model.getRowCount()-1, model.getRowCount()-1);
											opgSelectedValueName.clear();
										}
										tfNameGroup.setText("");
									}
								}
							});
							panel_3.add(btnNewButton_4);
						}
						{
							JButton btnNewButton_5 = new JButton("");
							btnNewButton_5.setPreferredSize(new Dimension(25, 25));
							btnNewButton_5.setIcon(new ImageIcon(OptionGroupManagerPanel.class.getResource("/com/teamcenter/rac/aif/images/remove_16.png")));
							btnNewButton_5.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									
									int ret = JOptionPane.showConfirmDialog(null, "Do you want to delete it?", "Option Group", JOptionPane.YES_NO_OPTION);
									if (ret != JOptionPane.YES_OPTION)
						    			return;
						    		
									int tableRowIdx = opGroupTable.getSelectedRow();
									int modelRowIdx = opGroupTable.convertRowIndexToModel(tableRowIdx);
									
									if( modelRowIdx < 0) {
										MessageBox.post(parentDlg, "Please select a group name.", "INFORMATION", MessageBox.WARNING);
										return;
									}
									
									DefaultTableModel model = (DefaultTableModel)opGroupTable.getModel();
									//2016-05-30 수정: opGroup Column Index 변경
									//OpGroup opGroup = (OpGroup)model.getValueAt(modelRowIdx, 1);
									OpGroup opGroup = (OpGroup)model.getValueAt(modelRowIdx, COLUMN_OPGROUP_OBJ_INDEX);
									
									try {
										parentDlg.deleteOptionGroup(opGroup);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
										return;
									}
									
									// opGroup를 삭제
									model.removeRow(modelRowIdx);
									// Row Delete
									opGroupList.remove(opGroup);
									
									parentDlg.removeAllRow(opgSelectedTable);
									opgSelectedValueName.clear();
								}
							});
							panel_3.add(btnNewButton_5);
						}
						
						/**
						 * 그룹명, 비고 수정
						 */
						btnGroupSave  = new JButton("");
						btnGroupSave.setPreferredSize(new Dimension(25, 25));
						btnGroupSave.setIcon(new ImageIcon(OptionGroupManagerPanel.class.getResource("/icons/save_16.png")));
						btnGroupSave.setToolTipText("Modify Group Name or Description");
						btnGroupSave.addActionListener( new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent paramActionEvent) {
								doSaveOptionMaster();
								
							}
						});
						panel_3.add(btnGroupSave);
					}
				}
				

				{
					JPanel panel_2 = new JPanel();
					panel_1.add(panel_2, BorderLayout.EAST);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						Vector header = new Vector();
						header.add("Project");
						header.add("OptionName");
						header.add("Option");
						
						DefaultTableModel model = new DefaultTableModel(opgSelectedData, header);
						
						opgSelectedTable = new JTable(model);
						TableColumnModel cm = opgSelectedTable.getColumnModel();
						int[] width = new int[]{50, 120, 40};
					    for( int i = 0; i < cm.getColumnCount(); i++){
					    	cm.getColumn(i).setPreferredWidth(width[i]);
					    }
						JScrollPane pane = new JScrollPane(opgSelectedTable);
						pane.setPreferredSize(new Dimension(250, 0));
						panel_2.add(pane);
					}
				}
			}
			{
				JPanel panel_1 = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
				flowLayout.setAlignment(FlowLayout.TRAILING);
				panel.add(panel_1, BorderLayout.SOUTH);
				{
					btnSave = new JButton("Save");
					btnSave.setIcon(new ImageIcon(OptionGroupManagerPanel.class.getResource("/icons/save_16.png")));
					btnSave.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							
							try {
								insertOptionGroup();
								MessageBox.post(parentDlg, "Successfully saved." , "Infomation", MessageBox.INFORMATION);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
							}
							
						}
					});
					{
						JButton btnReload = new JButton("Reload");
						btnReload.setIcon(new ImageIcon(OptionGroupManagerPanel.class.getResource("/icons/refresh_16.png")));
						btnReload.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent actionevent) {
								try {
									setOpGroup();
									tfFilter.setText("");
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
								}
							}
						});
						panel_1.add(btnReload);
					}
					panel_1.add(btnSave);
				}
			}
		}
		{
			JPanel panel = new JPanel();
			add(panel, BorderLayout.EAST);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.WEST);
				panel_1.setLayout(new GridLayout(2, 1, 0, 0));
				{
					JPanel panel_1_1 = new JPanel();
					panel_1.add(panel_1_1);
					panel_1_1.setLayout(new BorderLayout(0, 0));
					{
						JButton btnNewButton_6 = new JButton("");
						btnNewButton_6.setPreferredSize(new Dimension(40, 40));
						btnNewButton_6.setIcon(new ImageIcon(OptionGroupManagerPanel.class.getResource("/com/kgm/commands/variantoptioneditor/images/backarrow2.png")));
						btnNewButton_6.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent actionevent) {
								
								int tableRowIdx = opGroupTable.getSelectedRow();
								int modelRowIdx = opGroupTable.convertRowIndexToModel(tableRowIdx);
								if( modelRowIdx < 0){
									MessageBox.post(parentDlg, "Please select a group name.", "INFORMATION", MessageBox.WARNING);
									return;
								}
								
								DefaultTableModel groupModel = (DefaultTableModel)opGroupTable.getModel();
								//OpGroup group = (OpGroup)groupModel.getValueAt(modelRowIdx, 1);
								//2016-05-30 수정: opGroup Column Index 변경
								OpGroup group = (OpGroup)groupModel.getValueAt(modelRowIdx, COLUMN_OPGROUP_OBJ_INDEX);
								group.setChanged(true);
								
								int[] rows = enableOptionTable.getSelectedRows();
								int[] columns = enableOptionTable.getSelectedColumns();
								if( columns.length < 1) return ;
								
								if( columns.length == 1){
									if( columns[0] == 0 ){
										for(int i = 0; i < rows.length; i++){
											int row = rows[i];
											OpValueName opName = (OpValueName)enableOptionTable.getValueAt(row, 2);
											ArrayList<OpValueName> opNames = categoryMap.get(opName.getCategory());
											for( int j = 0;  j < opNames.size(); j++){
												if( !opgSelectedValueName.contains(opNames.get(j))){
													opgSelectedValueName.add(opNames.get(j));
												}
											}
										}
									}else{
										for(int i = 0; i < rows.length; i++){
											int row = rows[i];
											OpValueName opName = (OpValueName)enableOptionTable.getValueAt(row, 2);
											if( !opgSelectedValueName.contains(opName)){
												opgSelectedValueName.add(opName);
											}
										}
									}
								}else{
									for(int i = 0; i < rows.length; i++){
										int row = rows[i];
										OpValueName opName = (OpValueName)enableOptionTable.getValueAt(row, 2);
										if( !opgSelectedValueName.contains(opName)){
											opgSelectedValueName.add(opName);
										}
									}
								}
								
								Collections.sort(opgSelectedValueName);
								
								DefaultTableModel model = (DefaultTableModel)opgSelectedTable.getModel();
								model.getDataVector().clear();
								setOpgSelectedData(group);
								
							}
						});
						panel_1_1.add(btnNewButton_6, BorderLayout.SOUTH);
					}
				}
				{
					JPanel panel_1_1 = new JPanel();
					panel_1.add(panel_1_1);
					panel_1_1.setLayout(new BorderLayout(0, 0));
					{
						JButton btnNewButton_7 = new JButton("");
						btnNewButton_7.setIcon(new ImageIcon(OptionGroupManagerPanel.class.getResource("/com/kgm/commands/variantoptioneditor/images/forwardarrow2.png")));
						btnNewButton_7.setPreferredSize(new Dimension(40, 40));
						btnNewButton_7.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent actionevent) {
								
								int tableRowIdx = opGroupTable.getSelectedRow();
								int modelRowIdx = opGroupTable.convertRowIndexToModel(tableRowIdx);
								if( modelRowIdx < 0){
									MessageBox.post(parentDlg, "Please select a group name.", "INFORMATION", MessageBox.WARNING);
									return;
								}
								
								DefaultTableModel groupModel = (DefaultTableModel)opGroupTable.getModel();
								//OpGroup group = (OpGroup)groupModel.getValueAt(modelRowIdx, 1);
								//2016-05-30 수정: opGroup Column Index 변경
								OpGroup group = (OpGroup)groupModel.getValueAt(modelRowIdx, COLUMN_OPGROUP_OBJ_INDEX);
								group.setChanged(true);
								
								DefaultTableModel model = (DefaultTableModel)opgSelectedTable.getModel();
								int[] rows = opgSelectedTable.getSelectedRows();
								for( int i = rows.length - 1; i >= 0; i--){
									int modelIdx = opgSelectedTable.convertRowIndexToModel(rows[i]);
									OpValueName opValue = (OpValueName)model.getValueAt(modelIdx, 2);
									model.removeRow(modelIdx);
									
									opgSelectedValueName.remove(opValue);
									
									ArrayList<OpValueName> list = group.getOptionList();	
									list.remove(opValue);
								}
							}
						});
						panel_1_1.add(btnNewButton_7, BorderLayout.NORTH);
					}
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBorder(new TitledBorder(null, "Enable Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panel.add(panel_1);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					
					Vector header = new Vector();
					header.add("Category");
					header.add("Option Name");
					header.add("Option");
					
					Vector<Vector> data = getEnableOption(ospec);
					
					AttributiveCellTableModel model = new AttributiveCellTableModel(data, header){

						@Override
						public boolean isCellEditable(int i,
								int j) {
							// TODO Auto-generated method stub
							return false;
						}
						
					};
				      
				    CellSpan cellAtt = (CellSpan) model.getCellAttribute();
				    enableOptionTable = new MultiSpanCellTable(model);
//				    enableOptionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				    int[] width = new int[]{170, 210, 40};
				    TableColumnModel cm = enableOptionTable.getColumnModel();
				    for( int i = 0; i < cm.getColumnCount(); i++){
				    	cm.getColumn(i).setPreferredWidth(width[i]);
				    }
				    enableOptionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				    OSpecTable.cellMerge(enableOptionTable, 2);
				    JScrollPane pane = new JScrollPane(enableOptionTable);
					panel_1.add(pane);
				}
			}
		}		
	}
	
	private Vector getEnableOption(OSpec ospec){
		
		int rowNum = -1, colNum = -1;
		int trimSize = ospec.getOptions().keySet().size();
		ArrayList<OpValueName> list = ospec.getOpNameList();
		Vector<Vector<String>> data = new Vector();
		Vector row = null;
		
		for(OpValueName option:list){
			row = new Vector();
			row.add(option.getCategoryName());
			row.add(option.getOptionName());
			row.add(option);
			data.add(row);
		}
		return data;
	}	
	
	private Vector getOptionGroupModelData(boolean bSetModel) throws Exception{
		ArrayList<OpGroup> list = parentDlg.getOptionGroup(chckbxOnlyOwnOption.isSelected());
		Vector data = new Vector();
		
		for( int i = 0; list != null && i < list.size(); i++){
			OpGroup group = list.get(i);
			Vector row = new Vector();
			//row.add(group.getOwner());
			//row.add(group);
			//2016-05-30 수정: 컬럼 추가
			row.add(group.getOwner());
			row.add(group.getOpGroupName());
			row.add(group.getDesciption());
			row.add(group);
			row.add(group.getCondition());
			
			if( bSetModel ){
				DefaultTableModel model = (DefaultTableModel)opGroupTable.getModel();
				model.addRow(row);
			}
			data.add(row);
			if( !opGroupList.contains(group)){
				opGroupList.add(group);
			}
		}
		
		return data;
	}
	
	private void setOpgSelectedData(OpGroup group){
		if( opgSelectedValueName == null )
			return;
		
		ArrayList<OpValueName> opList = group.getOptionList();
		if( opList == null){
			opList = new ArrayList();
		}
		
		DefaultTableModel model = (DefaultTableModel)opgSelectedTable.getModel();
		Vector data = model.getDataVector();
		data.clear();
		for(OpValueName opValue : opgSelectedValueName){
			Vector row = new Vector();
			row.add(ospec.getProject());
			row.add(opValue.getOptionName());
			row.add(opValue);
			model.addRow(row);
		}
		
		opList.clear();
		opList.addAll(opgSelectedValueName);
		group.setOptionList(opList);
	}	
	
	private void setOpGroup() throws Exception{
		if( opgSelectedTable == null )
			return;
		//2016-05-30 수정: Option 추가 후 저장하지 않고,  Reload 하면 한번 추가된 Option 은 다시 추가가 안된 현상 수정
		opGroupList.clear();
		parentDlg.removeAllRow(opgSelectedTable);
		parentDlg.removeAllRow(opGroupTable);
		
		getOptionGroupModelData(true);
	}
	
	private void insertOptionGroup() throws Exception{
		//2016-05-30 수정: OpGroup Table Editing Stop
		if (opGroupTable.isEditing()) 
			opGroupTable.getCellEditor().stopCellEditing();
		ArrayList<OpGroup> savedGroupList = new ArrayList();
		ArrayList<HashMap<String, String>> list = new ArrayList();
		DefaultTableModel model = (DefaultTableModel)opGroupTable.getModel();
		for( int i = 0; i < model.getDataVector().size(); i++){
			Vector row = (Vector)model.getDataVector().get(i);
			//2016-05-30 수정: OpGroup object Column Index 변경
			//OpGroup group = (OpGroup)row.get(1);
			OpGroup group = (OpGroup)row.get(COLUMN_OPGROUP_OBJ_INDEX);
			String description = row.get(COLUMN_OPGROUP_DESC_INDEX) ==null?"":(String) row.get(COLUMN_OPGROUP_DESC_INDEX); 
			
			if( group.isChanged() && group.getOptionList() != null && !group.getOptionList().isEmpty()){
				
				HashMap map = new HashMap();				
				map.put("GROUP_NAME", group.getOpGroupName());
				map.put("OWNER", group.getOwner());
				map.put("PROJECT", ospec.getProject());
				//2016-05-30 수정: OpGroup PARMETER 추가
				map.put("DESCRIPTION", description);
				ArrayList<OpValueName> opList = group.getOptionList();
				for( OpValueName value : opList){
					HashMap map2 = (HashMap)map.clone();
					map2.put("VALUE_NAME", value.getOptionName());
					map2.put("VALUE", value.getOption());
					list.add(map2);
					if( !savedGroupList.contains(group)){
						savedGroupList.add(group);
					}
				}
			}
		}
		
		if( list.isEmpty()){
			return;
		}
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("DATA", list);
		try {
			
			remote.execute("com.kgm.service.OSpecService", "insertOptionGroup", ds);
			
			for( OpGroup group : savedGroupList){
				group.setChanged(false);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	

	/**
	 * Option Name, Description 정보를 저장함
	 */
	private void doSaveOptionMaster() {
		int tableRowIdx = opGroupTable.getSelectedRow();
		int modelRowIdx = opGroupTable.convertRowIndexToModel(tableRowIdx);
		if (opGroupTable.isEditing()) 
			opGroupTable.getCellEditor().stopCellEditing();
		try
		{
			if( modelRowIdx < 0) {
				MessageBox.post(parentDlg, "Please select a group name.", "INFORMATION", MessageBox.WARNING);
				return;
			}
	
			DefaultTableModel model = (DefaultTableModel)opGroupTable.getModel();
			//저장된 GROUP 정보
			OpGroup opGroup = (OpGroup)model.getValueAt(modelRowIdx, COLUMN_OPGROUP_OBJ_INDEX);
			String currentOpGroupName = opGroup.getOpGroupName();
			String currentDescription = opGroup.getDesciption();
			
			//수정되는 GROUP 정보
			String newOpGroupName = (String)model.getValueAt(modelRowIdx, COLUMN_OPGROUP_NAME_INDEX);
			String newDescription = (String)model.getValueAt(modelRowIdx, COLUMN_OPGROUP_DESC_INDEX);
			
			newOpGroupName = newOpGroupName !=null ? newOpGroupName.toUpperCase(): "";
			
			//수정된 것이 없으며 pass
			if(currentOpGroupName.equals(newOpGroupName) && currentDescription.equals(newDescription))
				return;
			
			if(opGroup.getOptionList() == null)
			{
				MessageBox.post(parentDlg, "The Option Group does not yet exist.\nPlease create Option Group ", "INFORMATION", MessageBox.WARNING);
				return;
			}
			
			
			//Group Name 에 공백을 입력하였으면
			if( newOpGroupName.isEmpty())
			{
				model.setValueAt(opGroup.getOpGroupName(), modelRowIdx, COLUMN_OPGROUP_NAME_INDEX);
				return;
			}
			
			//수정된 OpGroup
			OpGroup newOpGroup = new OpGroup(newOpGroupName, userID);
			
			//이미 생성된 Group Name 이면 Pass
			if(!newOpGroupName.equals(currentOpGroupName) && opGroupList.contains(newOpGroup))
			{
				MessageBox.post(parentDlg, "The same group name exists", "INFORMATION", MessageBox.WARNING);
				model.setValueAt(opGroup.getOpGroupName(), modelRowIdx, COLUMN_OPGROUP_NAME_INDEX);
				return;
			}
			
			/**
			 * 1. OSPEC_OP_GROUP DB Table Update
			 */
			updateOpGroupMaster(newOpGroupName, newDescription, opGroup) ;
			
			/**
			 * 2. 선택된 Row  OpGroup Object 변경
			 */
			opGroup.setOpGroupName(newOpGroupName);
			opGroup.setDesciption(newDescription);
			model.setValueAt(newOpGroupName, modelRowIdx, COLUMN_OPGROUP_NAME_INDEX);
			
			MessageBox.post(parentDlg, "Option Group Updated.", "INFORMATION", MessageBox.INFORMATION);
			
		}catch(Exception ex)
		{
			MessageBox.post(parentDlg, "Error occurred in Updating", "ERROR", MessageBox.ERROR);
			return;
		}

	}
	
	/**
	 * OSPEC Master 정보(GROUP_NAME, DESCRIPTIOIN)
	 * @param newGroupName
	 * @param newDescription
	 * @param currentOpGroup
	 * @return
	 * @throws Exception
	 */
	private void updateOpGroupMaster(String newGroupName, String newDescription, OpGroup currentOpGroup) throws Exception
	{
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.put("PROJECT", ospec.getProject());
			ds.put("OWNER", currentOpGroup.getOwner());
			ds.put("OLD_GROUP_NAME", currentOpGroup.getOpGroupName());
			if(!newGroupName.equals(currentOpGroup.getOpGroupName()))
				ds.put("NEW_GROUP_NAME", newGroupName);
			ds.put("DESCRIPTION", newDescription == null || newDescription.isEmpty() ? "": newDescription );

	        remote.execute("com.kgm.service.OSpecService", "updateOpGroupMaster", ds);
	}	
}
