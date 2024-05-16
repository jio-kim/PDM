package com.kgm.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.kgm.commands.namegroup.model.PngCondition;
import com.kgm.commands.namegroup.model.PngMaster;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.util.MessageBox;

public class PngRegPanel extends JPanel {
	private JTextField tfFilter;
	private JTextField tfNameGroupID;
	private JTextField tfNameGroupDesc;
	private JTextField tfRefFunctions;
	private JTextField tfDescription;
	JTable partNameGroupTable = null;
	private PngDlg parentDlg = null;
	private JSpinner spQuantity = null;
	JTable partNameTable = null;
	JTable conditionTable = null;
	private JButton btnNameAdd = null;
	private JButton btnNameRemove = null;
	private JButton btnConditionAdd = null;
	private JButton btnConditionRemove = null; 
	private JCheckBox chkEnabled = null;
	private Vector groupNameData = null;
	
	private static final int COL_OBJECT = 5;
	
	/**
	 * Create the panel.
	 * @throws Exception 
	 */
	public PngRegPanel(PngDlg parentDlg) throws Exception {
		this.parentDlg = parentDlg;
		init();
		chkEnabled.setSelected(false);
	}
	
	private void init() throws Exception{
		setLayout(new BorderLayout(5, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.WEST);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Part Name Groups", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEADING);
		panel_2.add(panel_4, BorderLayout.NORTH);
		
		JLabel lblNewLabel = new JLabel("Group Name Filter : ");
		lblNewLabel.setIcon(new ImageIcon(PngRegPanel.class.getResource("/icons/filter_allocation_type_16.png")));
		panel_4.add(lblNewLabel);
		
		tfFilter = new JTextField();
		tfFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent keyevent) {
				if( keyevent.getKeyChar() == '\n'){
					
					chkEnabled.setSelected(false);
					Vector<Vector> filteredData = (Vector)groupNameData.clone();
					parentDlg.removeAllRow(partNameGroupTable);
					
					String filterStr = tfFilter.getText().trim().toUpperCase();
					DefaultTableModel model = (DefaultTableModel)partNameGroupTable.getModel();
					for( int i = filteredData.size() - 1; filteredData != null && i >= 0; i-- ){
						Vector row = filteredData.get(i);
						String groupName = (String)row.get(1);
						if( !filterStr.equals("")){
							if( groupName.toUpperCase().indexOf(filterStr) < 0 ){
								continue;
							}
						}
						model.addRow(row);
					}
				}
			}
		});		
		panel_4.add(tfFilter);
		tfFilter.setColumns(10);
		
		JPanel panel_5 = new JPanel();
		panel_2.add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		Vector<String> partNameGroupHeader = new Vector();
		partNameGroupHeader.add("Group ID");
		partNameGroupHeader.add("Group Name");
		partNameGroupHeader.add("Group");
		
		groupNameData = parentDlg.getPngDataVec();
		DefaultTableModel partNameGroupModel = new DefaultTableModel((Vector)groupNameData.clone(), partNameGroupHeader);
		
		/**
		 * [SR150416-025][2015.05.27][jclee] Row No를 Tooltip으로 볼 수 있도록 수정
		 */
		partNameGroupTable = new JTable(partNameGroupModel) {
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);

				try {
					tip = String.valueOf(rowIndex + 1);
				} catch (RuntimeException e1) {
					tip = String.valueOf(-1);
				}

				return tip;
			}
		};
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(partNameGroupModel);
		partNameGroupTable.setRowSorter(sorter);
	    
		partNameGroupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel selectionModel = partNameGroupTable.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				
				parentDlg.removeAllRow(partNameTable);
				parentDlg.removeAllRow(conditionTable);
				
				int rowIdx = partNameGroupTable.getSelectedRow();
				if( rowIdx < 0){
					return;
				}
				int modelIdx = partNameGroupTable.convertRowIndexToModel(rowIdx);
				if( modelIdx < 0){
					return;
				}
				
				DefaultTableModel model = (DefaultTableModel)partNameGroupTable.getModel();
				Object obj = model.getValueAt(modelIdx, 2);
				if( !(obj instanceof PngMaster)){
					return;
				}
				
				PngMaster pngMaster = (PngMaster)model.getValueAt(modelIdx, 2);
				tfRefFunctions.setText(pngMaster.getRefFunctions());
//				if( !pngMaster.isChanged())
//					return;
				
				try {
					
					pngMaster = parentDlg.getPngDetail(pngMaster.getGroupID());
					if( pngMaster == null){
						return;
					}
					
					chkEnabled.setSelected(pngMaster.isEnable());
					tfRefFunctions.setText(pngMaster.getRefFunctions());
					spQuantity.setValue(pngMaster.getDefaultQuantity());
					
					/**
					 * [SR150416-025][2015.05.27][jclee] Description 컬럼 추가
					 */
					tfDescription.setText(pngMaster.getDescription());
					
					DefaultTableModel partNameModel = (DefaultTableModel)partNameTable.getModel();
					for( String partName : pngMaster.getPartNameList()){
						Vector row = new Vector();
						row.add(partName);
						partNameModel.addRow(row);
					}
					
					DefaultTableModel conditionModel = (DefaultTableModel) conditionTable.getModel();
					for( PngCondition pngCondition : pngMaster.getConditionList()){
//						Vector row = new Vector();
//						row.add(pngCondition.getGroupNumber());
//						row.add(pngCondition.getProduct());
//						row.add(pngCondition.getCondition());
//						row.add(pngCondition.getOperator() + " " + pngCondition.getQuantity());
//						row.add(pngCondition.getPartNameList());
//						row.add(pngCondition);
//						conditionModel.addRow(row);
						PngCondition pngConditionTmp = parentDlg.getSamePngCondition(pngMaster.getGroupID(), pngCondition.getGroupNumber(), pngCondition.getProduct(), pngCondition.getCondition(), pngCondition.getOperator(), pngCondition.getQuantity(), true);
						if( pngConditionTmp == null){
							Vector row = new Vector();
							row.add(pngCondition.getGroupNumber());
							row.add(pngCondition.getProduct());
							row.add(pngCondition.getCondition());
							row.add(pngCondition.getOperator() + " " + pngCondition.getQuantity());
							row.add(pngCondition.getPartNameList());
							row.add(pngCondition);
							conditionModel.addRow(row);
						}else{
							ArrayList<String> partNameList = pngConditionTmp.getPartNameList();
							if( partNameList == null || partNameList.isEmpty()){
								partNameList.addAll(pngCondition.getPartNameList());
							}else{
								for( String str : pngCondition.getPartNameList()){
									if( !partNameList.contains(str)){
										partNameList.add(str);
									}
								}
							}
						}
					}
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					MessageBox.post(parentDlg, e1.getMessage(), "ERROR", MessageBox.ERROR);
					return;
				}
				
			}

		});
		
		TableColumnModel tcm = partNameGroupTable.getColumnModel();
		int[] width = new int[]{80, 200};
		for( int i = 0; i < width.length; i++){
			tcm.getColumn(i).setPreferredWidth(width[i]);
		}
		tcm.removeColumn(tcm.getColumn(2));
		
		JScrollPane scrollPane = new JScrollPane(partNameGroupTable);
		scrollPane.setPreferredSize(new Dimension(200, 402));
		panel_5.add(scrollPane);
		
		JPanel panel_6 = new JPanel();
		panel_2.add(panel_6, BorderLayout.SOUTH);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new TitledBorder(null, "New Name Group", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		FlowLayout flowLayout = (FlowLayout) panel_7.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		panel_6.add(panel_7, BorderLayout.CENTER);
		
		tfNameGroupID = new JTextField();
		panel_7.add(tfNameGroupID);
		tfNameGroupID.setColumns(6);
		
		tfNameGroupDesc = new JTextField();
		panel_7.add(tfNameGroupDesc);
		tfNameGroupDesc.setColumns(15);
		
		JPanel panel_8 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_8.getLayout();
		flowLayout_2.setAlignment(FlowLayout.RIGHT);
		panel_6.add(panel_8, BorderLayout.SOUTH);
		
		JButton button = new JButton("");
		button.setIcon(new ImageIcon(PngRegPanel.class.getResource("/com/teamcenter/rac/aif/images/add_16.png")));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String groupID = tfNameGroupID.getText().trim().toUpperCase();
				String groupName = tfNameGroupDesc.getText().trim().toUpperCase();
				if( groupID.equals("") || groupName.equals("")){
					MessageBox.post(parentDlg, "Input a Group ID and a Group Name.", "ERROR", MessageBox.ERROR);
					return;
				}
				PngMaster group = new PngMaster(groupID, groupName);
				DefaultTableModel model = (DefaultTableModel)partNameGroupTable.getModel();
				for( int i = 0; model!=null && i < model.getRowCount(); i++){
					PngMaster master = (PngMaster)model.getValueAt(i, 2);
					if( groupID.equals(master.getGroupID().toUpperCase())){
						MessageBox.post(parentDlg, "Group ID is already present.", "ERROR", MessageBox.ERROR);
						return;
					}
					if( groupName.equals(master.getGroupName().toUpperCase())){
						MessageBox.post(parentDlg, "Group Name is already present.", "ERROR", MessageBox.ERROR);
						return;
					}
				}
				
				Vector row = new Vector();
				row.add(groupID);
				row.add(groupName);
				row.add(group);
				model.addRow(row);
				
				partNameGroupTable.getSelectionModel().setSelectionInterval(partNameGroupTable.getRowCount() - 1, partNameGroupTable.getRowCount() - 1);
				
				tfNameGroupID.setText("");
				tfNameGroupDesc.setText("");
				
				parentDlg.removeAllRow(partNameTable);
				parentDlg.removeAllRow(conditionTable);
				
				chkEnabled.setSelected(true);
				
			}
		});
		panel_8.add(button);
		
		JButton button_1 = new JButton("");
		button_1.setIcon(new ImageIcon(PngRegPanel.class.getResource("/com/teamcenter/rac/aif/images/remove_16.png")));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				
				int ret = JOptionPane.showConfirmDialog(null, "Do you want to delete it?", "Part Name Group", JOptionPane.YES_NO_OPTION);
				if (ret != JOptionPane.YES_OPTION)
	    			return;
				
				DefaultTableModel model = (DefaultTableModel)partNameGroupTable.getModel();
				int[] rows = partNameGroupTable.getSelectedRows();
				if( rows == null)
					return;
				int[] modelIdxes = new int[rows.length];
				try{
					for( int i = 0; rows != null && i < rows.length; i++){
						modelIdxes[i] = partNameGroupTable.convertRowIndexToModel(rows[i]);
						String groupID = (String)model.getValueAt(modelIdxes[i], 0);
						deleteNameGroup(groupID);
					}
				}catch(Exception e){
					e.printStackTrace();
					MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
					return;
				}
				parentDlg.removeSelectedRows(partNameGroupTable);
			}
		});
		panel_8.add(button_1);
		
		JPanel panel_3 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) panel_3.getLayout();
		flowLayout_4.setAlignment(FlowLayout.TRAILING);
		panel.add(panel_3, BorderLayout.SOUTH);
		
		JButton btnNewButton = new JButton("Export");
		btnNewButton.setIcon(new ImageIcon(PngRegPanel.class.getResource("/com/kgm/common/images/excel_16.png")));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToExcel();
			}
		});
		panel_3.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Save");
		btnNewButton_1.setIcon(new ImageIcon(PngRegPanel.class.getResource("/icons/save_16.png")));
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					
					if( (Integer)spQuantity.getValue() < 0){
						MessageBox.post(parentDlg, "Invalid Default Quantity", "ERROR", MessageBox.ERROR);
						return;
					};
					
					String refFuncs = tfRefFunctions.getText().trim();
					String tmpStr = "";
					String[] funcs = refFuncs.split(",");
					for( String func : funcs){
						if( func.trim().length() != 4){
							MessageBox.post(parentDlg, "Invalid Refered Functions!", "ERROR", MessageBox.ERROR);
							tfRefFunctions.setFocusable(true);
							return;
						}
						
						if( tmpStr.equals("")){
							tmpStr = func.toUpperCase().trim();
						}else{
							tmpStr += "," + func.toUpperCase().trim();
						}
					}
					tfRefFunctions.setText(tmpStr);
					
					if( partNameTable.getRowCount() < 1){
						MessageBox.post(parentDlg, "Input a Part Name", "ERROR", MessageBox.ERROR);
						return;
					}
					
					save();
					MessageBox.post(parentDlg, "Successfully saved." , "Infomation", MessageBox.INFORMATION);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					MessageBox.post(parentDlg, e1.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_3.add(btnNewButton_1);
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_9 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_9.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEADING);
		panel_1.add(panel_9, BorderLayout.NORTH);
		
		chkEnabled = new JCheckBox("Enabled");
		chkEnabled.setSelected(true);
		chkEnabled.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent event) {
				// TODO Auto-generated method stub
				if (event.getStateChange() == ItemEvent.SELECTED){
					setEnabledProp(true);
				}else if(event.getStateChange() == ItemEvent.DESELECTED){
					setEnabledProp(false);
				}
			}
			
		});
		panel_9.add(chkEnabled);
		
		JPanel panel_10 = new JPanel();
		panel_10.setBorder(new TitledBorder(null, "Name Group Property", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.add(panel_10, BorderLayout.CENTER);
		panel_10.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_11 = new JPanel();
		panel_10.add(panel_11, BorderLayout.WEST);
		panel_11.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_13 = new JPanel();
		panel_11.add(panel_13, BorderLayout.NORTH);
		panel_13.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_16 = new JPanel();
		FlowLayout flowLayout_7 = (FlowLayout) panel_16.getLayout();
		flowLayout_7.setAlignment(FlowLayout.LEADING);
		panel_13.add(panel_16, BorderLayout.NORTH);
		
		JLabel lblNewLabel_1 = new JLabel("Default Quantity : ");
		panel_16.add(lblNewLabel_1);
		
		spQuantity = new JSpinner();
		spQuantity.setValue(1);
		spQuantity.setPreferredSize(new Dimension(50, 22));
		panel_16.add(spQuantity);
		
		JPanel panel_17 = new JPanel();
		panel_17.setBorder(new TitledBorder(null, "Refered Function", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_13.add(panel_17, BorderLayout.SOUTH);
		panel_17.setLayout(new GridLayout(2, 0, 0, 0));
		
		JLabel lblNewLabel_2 = new JLabel("EX) F80C,F82A");
		panel_17.add(lblNewLabel_2);
		
		tfRefFunctions = new JTextField();
		tfRefFunctions.setHorizontalAlignment(SwingConstants.TRAILING);
		panel_17.add(tfRefFunctions);
		tfRefFunctions.setColumns(10);
		
		JPanel panel_14 = new JPanel();
		panel_11.add(panel_14, BorderLayout.CENTER);
		panel_14.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_15 = new JPanel();
		panel_15.setBorder(new TitledBorder(null, "Part Name List", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_14.add(panel_15, BorderLayout.CENTER);
		
		Vector<String> partNameHeader = new Vector();
		partNameHeader.add("Part Name");
		DefaultTableModel model = new DefaultTableModel(null, partNameHeader);
		panel_15.setLayout(new BorderLayout(0, 0));
		partNameTable = new JTable(model);
		TableRowSorter<TableModel> partNameSorter = new TableRowSorter<TableModel>(model);
		partNameTable.setRowSorter(partNameSorter);
		tcm = partNameTable.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(200);
		partNameTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane_1 = new JScrollPane(partNameTable);
		scrollPane_1.setPreferredSize(new Dimension(200, 402));
		panel_15.add(scrollPane_1);
		
		JPanel panel_18 = new JPanel();
		FlowLayout flowLayout_5 = (FlowLayout) panel_18.getLayout();
		flowLayout_5.setAlignment(FlowLayout.TRAILING);
		panel_14.add(panel_18, BorderLayout.SOUTH);
		
		btnNameAdd = new JButton("");
		btnNameAdd.setIcon(new ImageIcon(PngRegPanel.class.getResource("/com/teamcenter/rac/aif/images/add_16.png")));
		btnNameAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					NewNameListDlg dlg = new NewNameListDlg(parentDlg);
					dlg.setVisible(true);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					MessageBox.post(parentDlg, e1.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_18.add(btnNameAdd);
		
		btnNameRemove = new JButton("");
		btnNameRemove.setIcon(new ImageIcon(PngRegPanel.class.getResource("/com/teamcenter/rac/aif/images/remove_16.png")));
		btnNameRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentDlg.removeSelectedRows(partNameTable);
			}
		});
		panel_18.add(btnNameRemove);
		
		JPanel panel_12 = new JPanel();
		panel_12.setBorder(new TitledBorder(null, "Conditions to apply", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_10.add(panel_12, BorderLayout.CENTER);
		panel_12.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_19 = new JPanel();
		panel_12.add(panel_19, BorderLayout.CENTER);
		panel_19.setLayout(new BorderLayout(0, 0));
		
		int[] conditionWidth = new int[]{45, 80, 300, 40, 300};
		Vector<String> conditionHeader = new Vector();
		
		conditionHeader.add("Num");
		conditionHeader.add("Product");
		conditionHeader.add("Condition");
		conditionHeader.add("Qty");
		conditionHeader.add("Part Name");
		conditionHeader.add("Object");
		DefaultTableModel conditionModel = new DefaultTableModel(null, conditionHeader){

			@Override
			public boolean isCellEditable(int row, int column) {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		conditionTable = new JTable(conditionModel){

			@Override
			public Class getColumnClass(int i) {
				// TODO Auto-generated method stub
				int modelIdx = conditionTable.convertColumnIndexToModel(i);
				if( modelIdx == 4){
					return ArrayList.class;
				}else{
					return String.class;
				}
			}
			
		};
		conditionTable.setDefaultRenderer(ArrayList.class, new MultiLineTableCellRenderer());
		TableRowSorter<TableModel> conditionSorter = new TableRowSorter<TableModel>(conditionModel);
		conditionTable.setRowSorter(conditionSorter);
		conditionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumnModel cm = conditionTable.getColumnModel();
		cm.removeColumn(cm.getColumn(conditionHeader.size() - 1));
		for( int i = 0; i < conditionWidth.length; i++){
			cm.getColumn(i).setPreferredWidth(conditionWidth[i]);
		}
		
		// [NoSR][20160331][jclee] Condition 수정 기능 추가
		conditionTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent paramMouseEvent) {
				if (paramMouseEvent.getClickCount() == 2) {
					try {
						HashMap<String, Object> hmData = new HashMap<String, Object>();
						
						if (conditionTable.getSelectedRowCount() > 0) {
							TableModel model = conditionTable.getModel();
							int iRow = conditionTable.getSelectedRow();
							Object objPngCondition = model.getValueAt(iRow, COL_OBJECT);
							
							if (objPngCondition instanceof PngCondition) {
								PngCondition pngCondition = (PngCondition) objPngCondition;
								
								PngConditionSetDlg dlg = new PngConditionSetDlg(parentDlg, pngCondition);
								dlg.setVisible(true);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						MessageBox.post(e);
					}
				}
			}
		});
		
		JScrollPane scrollPane_2 = new JScrollPane(conditionTable);
		panel_19.add(scrollPane_2);
		
		JPanel panel_20 = new JPanel();
		FlowLayout flowLayout_6 = (FlowLayout) panel_20.getLayout();
		flowLayout_6.setAlignment(FlowLayout.TRAILING);
		panel_12.add(panel_20, BorderLayout.SOUTH);
		
		btnConditionAdd = new JButton("");
		btnConditionAdd.setIcon(new ImageIcon(PngRegPanel.class.getResource("/com/teamcenter/rac/aif/images/add_16.png")));
		btnConditionAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PngConditionSetDlg dlg;
				try {
					dlg = new PngConditionSetDlg(parentDlg);
					dlg.setVisible(true);
				} catch (Exception e1) {
					e1.printStackTrace();
					MessageBox.post(parentDlg, e1.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		panel_20.add(btnConditionAdd);
		
		btnConditionRemove = new JButton("");
		btnConditionRemove.setIcon(new ImageIcon(PngRegPanel.class.getResource("/com/teamcenter/rac/aif/images/remove_16.png")));
		btnConditionRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentDlg.removeSelectedRows(conditionTable);
			}
		});
		panel_20.add(btnConditionRemove);
		
		/**
		 * [SR150416-025][2015.05.26][jclee] Description 추가.
		 */
		JPanel panelDescription = new JPanel();
		panelDescription.setBorder(new TitledBorder(null, "Description", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		tfDescription = new JTextField();
		tfDescription.setColumns(70);
		panelDescription.add(tfDescription);
		panel_10.add(panelDescription, BorderLayout.SOUTH);
	}
	
	private void save() throws Exception{
		int pngIdx = partNameGroupTable.getSelectedRow();
		if( pngIdx < 0){
			throw new Exception("Nott selected Name Group.");
		}
		int modelIdx = partNameGroupTable.convertRowIndexToModel(pngIdx);
		DefaultTableModel model = (DefaultTableModel)partNameGroupTable.getModel();
		PngMaster pngGroup = (PngMaster)model.getValueAt(modelIdx, 2);
		pngGroup.setEnable(chkEnabled.isSelected());
		pngGroup.setDefaultQuantity((Integer)spQuantity.getValue());
		pngGroup.setRefFunctions(tfRefFunctions.getText());
		pngGroup.setDescription(tfDescription.getText());
		
		ArrayList<String> partNameList = new ArrayList<String>();
		for( int i = 0; i < partNameTable.getModel().getRowCount(); i++){
			partNameList.add((String)partNameTable.getModel().getValueAt(i, 0));
		}
		pngGroup.setPartNameList(partNameList);
		
		ArrayList<PngCondition> conditionList = new ArrayList<PngCondition>();
		for( int i = 0; i < conditionTable.getModel().getRowCount(); i++){
			conditionList.add((PngCondition)conditionTable.getModel().getValueAt(i, conditionTable.getModel().getColumnCount() - 1));
		}
		pngGroup.setConditionList(conditionList);
		
		insertPngGroup(pngGroup);
	}

	private void insertPngGroup(PngMaster pngGroup) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		HashMap pngMaster = new HashMap();
		pngMaster.put("GROUP_ID", pngGroup.getGroupID());
		pngMaster.put("GROUP_NAME", pngGroup.getGroupName());
		pngMaster.put("REF_FUNCS", pngGroup.getRefFunctions());
		pngMaster.put("DESCRIPTION", pngGroup.getDescription());
		pngMaster.put("DEFAULT_QTY", pngGroup.getDefaultQuantity());
		pngMaster.put("IS_ENABLED", pngGroup.isEnable() ? "1":"0");
		
		ds.put("PNG_MASTER", pngMaster);
		ds.put("PNG_NAME_LIST", pngGroup.getPartNameList());
		
		ArrayList<HashMap<String, Object>> pngConditionList = new ArrayList();
		ArrayList<PngCondition> conditions = pngGroup.getConditionList();
		for( PngCondition condition : conditions){
			HashMap map = new HashMap();
			//GROUP_ID, PRODUCT, CONDITION, QTY)
			map.put("GROUP_ID", pngGroup.getGroupID());
			map.put("GROUP_NUM", condition.getGroupNumber());
			map.put("PRODUCT", condition.getProduct());
			map.put("CONDITION", condition.getCondition());
			map.put("QTY", condition.getQuantity());
			map.put("OPERATOR", condition.getOperator());
			ArrayList<String> partNameList = condition.getPartNameList();
			if( partNameList == null || partNameList.isEmpty()){
				map.put("PART_NAME", "");
				map.put("PART_IDX", 0);
				pngConditionList.add(map);
			}else{
				int part_idx = 0;
				for( String partName : partNameList){
					
					HashMap map2 = (HashMap)map.clone();
					map2.put("PART_IDX", part_idx++);
					map2.put("PART_NAME", partName);
					pngConditionList.add(map2);
					
				}
			}
			
		}
		ds.put("PNG_CONDITION_LIST", pngConditionList);				
		
		try {
			
			remote.execute("com.kgm.service.PartNameGroupService", "insertPngMaster", ds);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}	
	}
	
	private void setEnabledProp(boolean bFlag){
		if( bFlag ){
			spQuantity.setEnabled(true);
			tfRefFunctions.setEnabled(true);
			btnNameAdd.setEnabled(true);
			btnNameRemove.setEnabled(true);
			btnConditionAdd.setEnabled(true);
			btnConditionRemove.setEnabled(true);
		}else{
			spQuantity.setEnabled(false);
			tfRefFunctions.setEnabled(false);
			btnNameAdd.setEnabled(false);
			btnNameRemove.setEnabled(false);
			btnConditionAdd.setEnabled(false);
			btnConditionRemove.setEnabled(false);
		}
	}
	
	public void addNewName(ArrayList<String> newNameList){
		ArrayList list = new ArrayList();
		DefaultTableModel model = (DefaultTableModel)partNameTable.getModel();
		for( int i = 0; i< model.getRowCount(); i++){
			list.add(model.getValueAt(i, 0));
		}
		parentDlg.removeAllRow(partNameTable);

		list.addAll(newNameList);
		
		Collections.sort(list);
		
		for( int i = 0; i< list.size(); i++){
			Vector row = new Vector();
			row.add(list.get(i));
			model.addRow(row);
		}
	}
	
	private void exportToExcel(){
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File("NameGroupList_" + sdf.format(now.getTime()) + ".xls");
		fileChooser.setSelectedFile(defaultFile);
//		fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("MSEXCEL"));
		fileChooser.setFileFilter(new FileFilter(){

			public boolean accept(File f) {
				// TODO Auto-generated method stub
				if( f.isFile()){
					return f.getName().endsWith("xls");
				}
				return false;
			}

			public String getDescription() {
				// TODO Auto-generated method stub
				return "*.xls";
			}
			
		});
		int result = fileChooser.showSaveDialog(parentDlg);
		if( result == JFileChooser.APPROVE_OPTION){
			File selectedFile = fileChooser.getSelectedFile();
			try
            {
				exportFor(selectedFile);
				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
            }
            catch (Exception e)
            {
            	e.printStackTrace();
            	MessageBox.post(parentDlg, e.getMessage(), "ERROR", MessageBox.ERROR);
            }
		}		
	}
	
	private void exportFor(File selectedFile) throws Exception{
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);
	    
	    WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    cellFormat.setAlignment(Alignment.CENTRE);
	    cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    WritableCellFormat autoLineFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    autoLineFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    autoLineFormat.setWrap(true);
	    autoLineFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    int startRow = 0;
	    int initColumnNum = 0;

	    Vector excelColumnHeader = new Vector();
	    excelColumnHeader.add("Group ID");
	    excelColumnHeader.add("Group Name");
	    excelColumnHeader.add("Enabled");
	    excelColumnHeader.add("Default Qty.");
	    excelColumnHeader.add("Ref. Functions");
	    excelColumnHeader.add("Part Name");
	    excelColumnHeader.add("Condition");
	    
	    
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	      label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), headerCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i + initColumnNum);
//	      cv.setSize(1500);
	      cv.setAutosize(true);
	      sheet.setColumnView(i + initColumnNum, cv);
	    }

	    int rowNum = 0;
	    startRow = 1;
	    
	    String value = null;
	    DefaultTableModel model = (DefaultTableModel)partNameGroupTable.getModel();
		WritableCellFormat format = null;
	    Vector<Vector> data = model.getDataVector();
	    for (int i = 0; i < data.size(); i++)
	    {
	    	Vector row = data.get(i);
	    	String groupID = (String)row.get(0);
	    	PngMaster pngMaster = parentDlg.getPngDetail(groupID);
	    	if( pngMaster == null){
	    		continue;
	    	}else{
	    		for (int j = 0; j < 7; j++)
		    	{
	    			value = null;
	    			switch(j){
	    			case 0:
	    				value = pngMaster.getGroupID();
	    				break;
	    			case 1:
	    				value = pngMaster.getGroupName();
	    				break;
	    			case 2:
	    				value = pngMaster.isEnable() ? "Y":"N";
	    				break;
	    			case 3:
	    				value = pngMaster.getDefaultQuantity() + "";
	    				break;
	    			case 4:
	    				value = pngMaster.getRefFunctions();
	    				break;
	    			case 5:
	    				ArrayList<String> nameList = pngMaster.getPartNameList();
	    				for( int k = 0; nameList != null && k < nameList.size(); k++){
	    					if( value == null){
	    						value = nameList.get(k);
	    					}else{
	    						value += "\012" + nameList.get(k);
	    					}
	    				}
	    				break;
	    			case 6:
	    				ArrayList<PngCondition> conditionList = pngMaster.getConditionList();
	    				for( int k = 0; conditionList != null && k < conditionList.size(); k++){
	    					PngCondition condition = conditionList.get(k);
	    					String condStr = "[" + condition.getProduct() + "] " + condition.getCondition() + "[Qty " + condition.getOperator() + " " + condition.getQuantity() + "]";
	    					if( value == null){
	    						value = condStr;
	    					}else{
	    						value += "\012" + condStr;
	    					}
	    				}
	    				break;
	    			}
	    			format = autoLineFormat;
		    		label = new jxl.write.Label(j + initColumnNum, (rowNum) + startRow, value, format);
		    		sheet.addCell(label);
		    	}
	    	}
	    	
	    	rowNum++;
	    }

	    //셀 Merge
//	    initColumnNum = opNameList.size();
//	    int startIdxToMerge = startRow;
//	    int endIdxToMerge = startRow;
//	    for (int i = 0; i < data.size(); i++){
//	    	
//	    	Cell cell = sheet.getCell(initColumnNum, i + startRow);
//	    	Cell nextCell = sheet.getCell(initColumnNum, i + startRow + 1);
//	    	
//	    	if( cell.getContents().equals(nextCell.getContents())){
//	    		endIdxToMerge = i + 1 + startRow;
//	    	}else{
//	    		if( startIdxToMerge < endIdxToMerge){
//		    		sheet.mergeCells(initColumnNum, startIdxToMerge, initColumnNum, endIdxToMerge);
//		    		WritableCell wCell = sheet.getWritableCell(initColumnNum, startIdxToMerge);
//	    			WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
//	    			cf.setWrap(true);
//	    			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
//	    		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
//	    		    wCell.setCellFormat(cf);
//	    		}
//	    		startIdxToMerge = i + 1 + startRow;
//	    	}
//	    }
	    
	    workBook.write();
	    workBook.close();	
	}
	
	private void deleteNameGroup(String groupID) throws Exception{
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		try {
			ds.put("GROUP_ID", groupID);
			remote.execute("com.kgm.service.PartNameGroupService", "deletePngMaster", ds);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}			
				
	}

	class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {
		private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();

		public MultiLineTableCellRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
			setOpaque(true);
		}
		
		public List<List<Integer>> getRowColHeight() {
			return rowColHeight;
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setFont(table.getFont());
			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				if (table.isCellEditable(row, column)) {
					setForeground(UIManager.getColor("Table.focusCellForeground"));
					setBackground(UIManager.getColor("Table.focusCellBackground"));
				}
			} else {
				setBorder(new EmptyBorder(1, 2, 1, 2));
			}
			if (value != null) {
				if( value instanceof ArrayList){
					ArrayList<String> valList = (ArrayList)value;
					String values = null;
					for( String str : valList){
						if( values == null){
							values = str;
						}else{
							values += "\n" + str;
						}
					}
					setText(values);
				}else{
					setText(value.toString());
				}
			} else {
				setText("");
			}
			adjustRowHeight(table, row, column);
			return this;
		}

		/**
		 * Calculate the new preferred height for a given row, and sets the height
		 * on the table.
		 */
		private void adjustRowHeight(JTable table, int row, int column) {
			// The trick to get this to work properly is to set the width of the
			// column to the
			// textarea. The reason for this is that getPreferredSize(), without a
			// width tries
			// to place all the text in one line. By setting the size with the with
			// of the column,
			// getPreferredSize() returnes the proper height which the row should
			// have in
			// order to make room for the text.
			int cWidth = table.getTableHeader().getColumnModel().getColumn(column)
					.getWidth();
			setSize(new Dimension(cWidth, 1000));
			int prefH = getPreferredSize().height;
			while (rowColHeight.size() <= row) {
				rowColHeight.add(new ArrayList<Integer>(column));
			}
			List<Integer> colHeights = rowColHeight.get(row);
			while (colHeights.size() <= column) {
				colHeights.add(0);
			}
			colHeights.set(column, prefH);
			int maxH = prefH;
			for (Integer colHeight : colHeights) {
				if (colHeight > maxH) {
					maxH = colHeight;
				}
			}
			
			if (table.getRowHeight(row) != maxH) {
				table.setRowHeight(row, maxH);
				
			}
			conditionTable.setRowHeight(row, table.getRowHeight(row));
		}
	}	
}
