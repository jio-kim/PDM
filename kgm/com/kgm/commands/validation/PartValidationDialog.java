package com.kgm.commands.validation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.ui.CheckComboBox;
import com.kgm.common.ui.MultiLineToolTip;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;

@SuppressWarnings({"serial", "rawtypes", "unchecked", "unused"})
public class PartValidationDialog extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable validateTable;
	private JComboBox fmCombo;
	private JTextField partNameTF;
	private JTextField quantityTF;
	private JTextField optionTF;
	private JTable editTable;
	private Vector headerVector = new Vector();
	private Vector validateHeaderVector = new Vector();
	private BigDecimal currentIdx = null;
	private JButton addBtn = null;
	private CheckComboBox specCombo = null;
	private TCComponentBOMLine target = null;
	private Vector<String> funcMasterList = new Vector();
	int[] columnWidth = { 5, 100, 150, 70, 100, 400};
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PartValidationDialog dialog = new PartValidationDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * @throws TCException 
	 */
	public PartValidationDialog(TCComponentBOMLine target) throws TCException {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
//		setBounds(100, 100, 578, 524);
		setTitle("Function Name Master");
		this.target = target;
		
		//Function Master ID 수집.
		AIFComponentContext[] contexts = target.getChildren();
		for(int i = 0; contexts != null && i < contexts.length; i++){
			TCComponentBOMLine function = (TCComponentBOMLine)contexts[i].getComponent();
			AIFComponentContext[] cxts = function.getChildren();
			for(int j = 0; cxts != null && j < cxts.length; j++){
				TCComponentBOMLine functionMaster = (TCComponentBOMLine)cxts[j].getComponent();
				funcMasterList.add(functionMaster.getItem().getProperty("item_id"));
			}
		}
		
		headerVector.add("idx");
		headerVector.add("Function Master");
		headerVector.add("Item Name");
		headerVector.add("Quantity");
		headerVector.add("Option");
		
		validateHeaderVector.addAll(headerVector);
		validateHeaderVector.add("ERROR SPEC");
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setPreferredSize(new Dimension(650, 550));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
			{
				JPanel panel = new JPanel();
				tabbedPane.addTab("Validation", null, panel, null);
				panel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_1 = new JPanel();
					panel.add(panel_1, BorderLayout.NORTH);
					panel_1.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_2 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						panel_1.add(panel_2, BorderLayout.CENTER);
						{
							specCombo = new CheckComboBox("Please select Specs"){

								@Override
								public JToolTip createToolTip() {
									MultiLineToolTip tip = new MultiLineToolTip();
							        tip.setComponent(this);
							        return tip;
								}
								
							};
							specCombo.setPreferredSize(new Dimension(300, 20));
							specCombo.setPopupWidth(300);
							specCombo.addMouseListener(new MouseAdapter(){

								@Override
								public void mouseEntered(MouseEvent arg0) {
									CheckComboBox obj = (CheckComboBox)(arg0.getSource());
									Object[] objs = obj.getSelectedItems();
									
									if( objs == null) {
										obj.setToolTipText(null);
										return;
									}
									
									String toolTipTxt = "";
									for( int i = 0; i < objs.length; i++){
										String val = objs[i].toString();
										toolTipTxt += (i==0 ? "":"\n" ) + val;
									}
									obj.setToolTipText(toolTipTxt);
									super.mouseEntered(arg0);
								}

								@Override
								public void mouseExited(MouseEvent arg0) {
									CheckComboBox obj = (CheckComboBox)(arg0.getSource());
									obj.setToolTipText(null);
									super.mouseExited(arg0);
								}
								
								
							});
							
							ArrayList specList = new ArrayList();
							AIFComponentContext[] context =  target.getItemRevision().getChildren("IMAN_reference");
							for( int j = 0; context != null && j < context.length; j++){
								TCComponent com =  (TCComponent)context[j].getComponent();
								String comType = com.getType();
								if( comType.equals("StoredOptionSet")){
									String buildSpecStr = com.getProperty("s7_BUILDSPEC");
									if( buildSpecStr.equalsIgnoreCase("Y")){
										StoredOptionSet sos = new StoredOptionSet(com.getProperty("object_name"), com.getUid());
										specList.add(sos);
									}
								}
							}
							
							Collections.sort(specList);
							HashSet set = new HashSet();
							set.addAll(specList);
							specCombo.resetObjs(set, false);
							
							panel_2.add(specCombo);
						}
					}
					{
						JPanel panel_2 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
						flowLayout.setAlignment(FlowLayout.RIGHT);
						panel_1.add(panel_2, BorderLayout.EAST);
						{
							JButton validationBtn = new JButton("Validation Check");
							validationBtn.addActionListener(new ActionListener() {
								
								@Override
								public void actionPerformed(ActionEvent event) {
									
									//* 가져와야 할 정보들.
									//1. 선택된 스펙이 가진 옵션 정보들( HashMap<SPEC, <Option Category, Option Value>> )
									//2. 저장된 Function Name Master 정보에서 스펙에 의해 쿼리된 BOM구조를 뺀 결과들(스펙의 개수만큼)
									//3. SOS_FUNCTION_NAME_MASTER Table 정보(해당하는 Function Master와 연관된 정보들)
									
									//SOS_FUNCTION_NAME_MASTER Table에 작성된 정보를 각 스펙 별로 하나씩 체크해 나감.
									//1. 차집합의 결과에 옵션이 없는 경우는 무조건 해당 Validation이 없어야 함.
									//	 있으면 해당 스펙 에러.
									//2. 차집합의 결과에 옵션이 있는 경우는 해당 스펙이 현재의 옵션값을 포함하는지 확인하고,
									//	 포함하고 있다면, 쿼리 자체가 에러
									//   현재의 옵션값을 포함하고 있지 않다면 해당 스펙 에러.
									
									SYMCRemoteUtil remote = new SYMCRemoteUtil();
									try{
									//Spec ComboBox에서 사용자가 선택한 스펙들
										Object[] selectedItems = specCombo.getSelectedItems();
										if( selectedItems == null || selectedItems.length < 1){
											return;
										}
										
										//SOS_FUNCTION_NAME_MASTER Table에 작성된 정보와 각 스펙의 차집합을 Map 으로 구성함.
										HashMap<String, ArrayList<String>> specMap = new HashMap();
										HashMap<String, StoredOptionSet> sosMap = new HashMap();
										ArrayList<String> sosList = new ArrayList();
										if( selectedItems != null && selectedItems.length > 0){
											for(Object item : selectedItems){
												String puid = ((StoredOptionSet)item).getPuid();
												sosMap.put(puid, (StoredOptionSet)item);
												sosList.add(puid);
												
												if( !specMap.containsKey(puid))
													specMap.put(puid, new ArrayList());
											}
										}
										String revisionPuid = PartValidationDialog.this.target.getItemRevision().getUid();
										
										DataSet ds = new DataSet();
										ds.put("SOS", sosList);
										//1. 선택된 스펙이 가진 옵션 정보들( PUID, CATEGORY, OPION_VALUE )
										HashMap optionMap = new HashMap();
										ArrayList<HashMap> specOptionlist = (ArrayList<HashMap>)remote.execute("com.kgm.service.VariantService", "getSpecOptions", ds);
										for( HashMap tmpMap : specOptionlist ){
											
											String puid = (String)tmpMap.get("PUID");
											String category = (String)tmpMap.get("CATEGORY");
											String optionValue = (String)tmpMap.get("OPTION_VALUE");
											HashMap<String, String> map = (HashMap)optionMap.get(puid);
											if( map == null){
												map = new HashMap();
											}
											map.put(category, optionValue);
											
											optionMap.put(puid, map);
										}
										
										//2.저장된 Function Name Master 정보에서 스펙에 의해 쿼리된 BOM구조를 뺀 결과들(스펙의 개수만큼)
										//각 스펙 별로 여기에 존재하는 것은,  FunctionMasterID-ChildItemName-Quantity가 일치하지 않는 에러들이다.
										ds.clear();
										ds.put("REVISION_PUID", PartValidationDialog.this.target.getItemRevision().getUid());
										ds.put("FUNCTION_MASTERS", funcMasterList);
										
										for( String specPuid : sosList){
											
											ds.put("SOS_PUID", specPuid);
											HashMap<String, Object> minusInfoMap= (HashMap)remote.execute("com.kgm.service.VariantService", "getMinusInfo", ds);
											
											ArrayList<HashMap> list = (ArrayList)(minusInfoMap.get(specPuid));
											if( list == null || list.isEmpty()){
												continue;
											}
											
											ArrayList errorInfo = (ArrayList)specMap.get(specPuid);
											
											for(HashMap tmpMap : list){
												String parentItemId = (String)tmpMap.get("PARENT_ITEM_ID");
												String childItemName = (String)tmpMap.get("CHILD_ITEM_NAME");
												BigDecimal qty = (BigDecimal)tmpMap.get("QTY");
												String key = parentItemId + "#" + childItemName + "#" + qty;
												if( !errorInfo.contains(key)){
													errorInfo.add(key);
												}
											}
											
										}
										
										
										//3. SOS_FUNCTION_NAME_MASTER Table 정보(해당하는 Function Master와 연관된 정보들)
										ds.clear();
										ds.put("FUNCTION_MASTERS", funcMasterList);
										ArrayList<HashMap<String, Object>> validationInfoList= (ArrayList)remote.execute("com.kgm.service.VariantService", "getValidationInfoList", ds);
										Vector data = new Vector();
										
										for( HashMap tmpMap : validationInfoList){
											
											Vector row = new Vector();
											
											row.add(tmpMap.get("IDX"));
											String parentItemId = (String)tmpMap.get("PARENT_ITEM_ID");
											String childItemName = (String)tmpMap.get("CHILD_ITEM_NAME");
											BigDecimal qty = (BigDecimal)tmpMap.get("QTY");
											String key = parentItemId + "#" + childItemName + "#" + qty;
											
											row.add(parentItemId);
											row.add(childItemName);
											row.add(qty);
											String optionValue = (String)tmpMap.get("OPTION_VALUE");
											row.add(optionValue);
											
											//SYMC_FUNCTION_VALIDATION에 작성된 정보를 각 스펙 별로 하나씩 체크해 나감.
											//1. 차집합의 결과에 옵션이 없는 경우는 무조건 해당 Validation이 없어야 함.
											//	 있으면 해당 스펙 에러.
											//2. 차집합의 결과에 옵션이 있는 경우는 해당 스펙이 현재의 옵션값을 포함하는지 확인하고,
											//	 포함하고 있다면, 쿼리 자체가 에러
											//   현재의 옵션값을 포함하고 있지 않다면 해당 스펙 에러.
											String errorStr = "";
											SpecVector sV = new SpecVector();
											if( optionValue == null || optionValue.equals("")){
												
												Set set = specMap.keySet();
												Iterator its = set.iterator();
												while(its.hasNext()){
													String specPuid = (String)its.next();
													ArrayList<String> errorInfo = (ArrayList)specMap.get(specPuid);
													if( errorInfo.contains(key)){
														StoredOptionSet sos = (StoredOptionSet) sosMap.get(specPuid);
														String sosName = sos.getName();
														if( !sV.contains(sosName)){
															sV.add(sosName);
														}
													}
												}
												row.add(sV);
												
											}else{
												
												Set set = specMap.keySet();
												Iterator its = set.iterator();
												while(its.hasNext()){
													String specPuid = (String)its.next();
													
													ArrayList<String> errorInfo = (ArrayList)specMap.get(specPuid);
													if( errorInfo.contains(key)){
														StoredOptionSet sos = (StoredOptionSet) sosMap.get(specPuid);
														String sosName = sos.getName();
														if( !sV.contains(sosName)){
															sV.add(sosName);
														}
													}
													
													HashMap<String,String> option = (HashMap)optionMap.get(specPuid);
													if( !option.containsValue(optionValue)){
														StoredOptionSet sos = (StoredOptionSet) sosMap.get(specPuid);
														String sosName = sos.getName();
														if( !sV.contains(sosName)){
															sV.add(sosName);
														}
													}
												}
												row.add(sV);
												
											}
											
											
											data.add(row);
										}
										
										DefaultTableModel model = (DefaultTableModel)validateTable.getModel();
										model.setDataVector(data, validateHeaderVector);
										tableInit(validateTable, validateHeaderVector);
										
									}catch( Exception e){
										e.printStackTrace();
									}
								}
							});
							panel_2.add(validationBtn);
						}
					}
				}
				{
					JPanel panel_1 = new JPanel();
					panel.add(panel_1, BorderLayout.CENTER);
					panel_1.setLayout(new BorderLayout(0, 0));
					{
						
						TableModel model = new DefaultTableModel(null, validateHeaderVector) {
							public Class getColumnClass(int col) {
								return String.class;
							}

							public boolean isCellEditable(int row, int col) {
								return false;
							}
						};
						
						validateTable = new JTable(model);
						
						TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
						validateTable.setRowSorter(sorter);	
						tableInit(validateTable, validateHeaderVector);
						
						JScrollPane pane = new JScrollPane();
						pane.setViewportView(validateTable);
						pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						pane.getViewport().setBackground(Color.WHITE);
						panel_1.add(pane);
						
					}
				}
			}
			{
				JPanel panel = new JPanel();
				tabbedPane.addTab("Edit", null, panel, null);
				panel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_1 = new JPanel();
					panel.add(panel_1, BorderLayout.NORTH);
					panel_1.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_1_1 = new JPanel();
						panel_1.add(panel_1_1);
						panel_1_1.setBorder(new TitledBorder(null, "Name Master Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
						panel_1_1.setLayout(new GridLayout(2, 2, 0, 0));
						{
							JPanel panel_2 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
							flowLayout.setAlignment(FlowLayout.LEADING);
							panel_1_1.add(panel_2);
							{
								JLabel lblNewLabel = new JLabel("Funtion Master");
								lblNewLabel.setPreferredSize(new Dimension(85,20));
								panel_2.add(lblNewLabel);
							}
							{
								fmCombo = new JComboBox();
								fmCombo.setPreferredSize(new Dimension(115,23));
								fmCombo.addItem("");
								for(String item : funcMasterList){
									fmCombo.addItem(item);
								}
								panel_2.add(fmCombo);
							}
						}
						{
							JPanel panel_2 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
							flowLayout.setAlignment(FlowLayout.LEADING);
							panel_1_1.add(panel_2);
							{
								JLabel lblNewLabel_1 = new JLabel("Part Name");
								panel_2.add(lblNewLabel_1);
							}
							{
								partNameTF = new JTextField();
								panel_2.add(partNameTF);
								partNameTF.setColumns(10);
							}
						}
						{
							JPanel panel_2 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
							flowLayout.setAlignment(FlowLayout.LEADING);
							panel_1_1.add(panel_2);
							{
								JLabel lblNewLabel_2 = new JLabel("Quantity");
								lblNewLabel_2.setPreferredSize(new Dimension(84, 15));
								panel_2.add(lblNewLabel_2);
							}
							{
								quantityTF = new JTextField();
								panel_2.add(quantityTF);
								quantityTF.setColumns(10);
							}
						}
						{
							JPanel panel_2 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
							flowLayout.setAlignment(FlowLayout.LEADING);
							panel_1_1.add(panel_2);
							{
								JLabel lblNewLabel_3 = new JLabel("Option");
								lblNewLabel_3.setPreferredSize(new Dimension(60, 15));
								panel_2.add(lblNewLabel_3);
							}
							{
								optionTF = new JTextField();
								panel_2.add(optionTF);
								optionTF.setColumns(10);
							}
						}
					}
					{
						JPanel panel_2 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
						flowLayout.setAlignment(FlowLayout.RIGHT);
						panel_1.add(panel_2, BorderLayout.SOUTH);
						{
							addBtn = new JButton("Add");
							addBtn.addActionListener(new ActionListener() {
								
								@Override
								public void actionPerformed(ActionEvent e) {
									String functionMaster = (String)fmCombo.getSelectedItem();
									String partName = partNameTF.getText().trim();
									String quantity = quantityTF.getText().trim();
									if( (fmCombo.getSelectedIndex() < 0 ||  functionMaster.equals("")) || partName.equals("") || quantity.equals("")){
										return;
									}
									
									SYMCRemoteUtil remote = new SYMCRemoteUtil();
									try{
										DataSet ds = new DataSet();
										ds.put("IDX", currentIdx);
										ds.put("Parent_ITEM_ID", functionMaster.trim().toUpperCase());
										ds.put("CHILD_ITEM_NAME",partNameTF.getText().trim().toUpperCase());
										ds.put("QTY", quantityTF.getText().trim().toUpperCase());
										ds.put("OPTION", optionTF.getText().trim().toUpperCase());
										
										remote.execute("com.kgm.service.VariantService", "insertValidationInfo", ds);
										
									}catch( Exception exception){
										exception.printStackTrace();
									}finally{
										getEditAllData();
										clear();
									}
								}
							});
							
							panel_2.add(addBtn);
						}
						{
							JButton delBtn = new JButton("Del");
							delBtn.addActionListener(new ActionListener() {
								
								@Override
								public void actionPerformed(ActionEvent e) {
									DefaultTableModel model = (DefaultTableModel)editTable.getModel();
									int selectedRow = editTable.getSelectedRow();
									if( selectedRow < 0) return;
									
									int modelIdx = editTable.convertRowIndexToModel(selectedRow);
									BigDecimal idx = (BigDecimal)model.getValueAt(modelIdx, 0);
									SYMCRemoteUtil remote = new SYMCRemoteUtil();
									try{
										DataSet ds = new DataSet();
										ds.put("IDX", idx);
										remote.execute("com.kgm.service.VariantService", "deleteValidationInfo", ds);
										
									}catch( Exception exception){
										exception.printStackTrace();
									}finally{
										getEditAllData();
										clear();
									}
								}
							});
							
							panel_2.add(delBtn);
						}
						{
							JButton clearBtn = new JButton("Clear");
							clearBtn.addActionListener(new ActionListener() {
								
								@Override
								public void actionPerformed(ActionEvent e) {
									clear();
								}
							});
							panel_2.add(clearBtn);
						}
					}
				}
				{
					JPanel panel_1 = new JPanel();
					panel.add(panel_1, BorderLayout.CENTER);
					panel_1.setLayout(new BorderLayout(0, 0));
					{
						
						
						TableModel model = new DefaultTableModel(null, headerVector) {
							public Class getColumnClass(int col) {
								return String.class;
							}

							public boolean isCellEditable(int row, int col) {
								return false;
							}
						};
						
						editTable = new JTable(model);
						TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
						editTable.setRowSorter(sorter);	
						getEditAllData();
						
						editTable.addMouseListener(new MouseAdapter(){

							@Override
							public void mouseReleased(MouseEvent e) {
								//테이블에서 특정 Row를 선택하여 더블클릭함.
								//수정 모드로 변경.
								if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
										&& e.isControlDown()==false) {
									editValidationInfo();
								}
								super.mouseReleased(e);
							}
							
						});
						
						JScrollPane pane = new JScrollPane();
						pane.setViewportView(editTable);
						pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						pane.getViewport().setBackground(Color.WHITE);
						panel_1.add(pane);
						
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton closeBtn = new JButton("Close");
				closeBtn.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						PartValidationDialog.this.dispose();
					}
					
				});
				buttonPane.add(closeBtn);
			}
		}
	}
	
	private void tableInit(JTable table, Vector header){
		
		PartValidationRenderer renderer = new PartValidationRenderer();
		TableColumnModel columnModel = table.getColumnModel();
		int n = header.size();
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
			columnModel.getColumn(i).setWidth(columnWidth[i]);
			columnModel.getColumn(i).setCellRenderer(renderer);
		}
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.removeColumn(columnModel.getColumn(0));
	}
	
	private void getEditAllData(){
		
		if( funcMasterList.isEmpty()) {
			tableInit(editTable, headerVector);
			return;
		}
		
		DefaultTableModel model = (DefaultTableModel)editTable.getModel();
		DataSet ds = new DataSet();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		try {
			Vector<Vector> dataVector = new Vector();
			ds.put("FUNCTION_MASTERS", funcMasterList);
			ArrayList<HashMap<String, Object>> list= (ArrayList)remote.execute("com.kgm.service.VariantService", "getValidationInfoList", ds);
			for( int i = 0; list != null && i < list.size(); i++){
				HashMap row = list.get(i);
				Vector rowVec = new Vector();
				rowVec.add(row.get("IDX"));
				rowVec.add(row.get("PARENT_ITEM_ID"));
				rowVec.add(row.get("CHILD_ITEM_NAME"));
				rowVec.add(row.get("QTY"));
				rowVec.add(row.get("OPTION_VALUE"));
				dataVector.add(rowVec);
			}
			model.setDataVector(dataVector, headerVector);
		} catch (Exception e1) {
			e1.printStackTrace();
		}finally{
			tableInit(editTable, headerVector);
		}
	}
	
	private void editValidationInfo(){
		DefaultTableModel model = (DefaultTableModel)editTable.getModel();
		int selectedRow = editTable.getSelectedRow();
		int modelIdx = editTable.convertRowIndexToModel(selectedRow);
		currentIdx = (BigDecimal)model.getValueAt(modelIdx, 0);
		fmCombo.setSelectedItem(model.getValueAt(modelIdx, 1));
		partNameTF.setText((String)model.getValueAt(modelIdx, 2));
		BigDecimal qty = (BigDecimal)model.getValueAt(modelIdx, 3);
		quantityTF.setText("" + qty);
		optionTF.setText((String)model.getValueAt(modelIdx, 4));
		addBtn.setText("Mod");
	}
	
	private void clear(){
		currentIdx = null;
		fmCombo.setSelectedIndex(0);
		partNameTF.setText("");
		quantityTF.setText("");
		optionTF.setText("");
		addBtn.setText("Add");
	}
	
	class SpecVector extends Vector{

		@Override
		public synchronized String toString() {
			String str = "";
			for( int i = 0; i < size(); i++){
				str += (str.equals("") ? "":", ") + get(i);
			}
			return str;
		}
		
	}
}
