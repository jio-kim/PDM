package com.kgm.commands.namegroup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.kgm.commands.namegroup.model.PngCondition;
import com.kgm.commands.ospec.op.OpComboValue;
import com.kgm.commands.ospec.op.OpValueName;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.util.MessageBox;

public class PngConditionSetDlg extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable combTable = null;
	private PngDlg parentDlg = null;
	private JTextField tfCondition = null;
	private Vector<Object> conditionVec = null;
	private JComboBox cbOption = null;
	private JComboBox cbProduct = null;
	private JComboBox cbOperator = null;
	private JSpinner spQty = null;
	private JTable partNameTable = null;
	private JCheckBox chkEachCount = null;
	private JLabel lblNumber = null;
	private JSpinner spGroupNum = null;
	
	private boolean isModify = false;	// 기존 Condition 수정 여부
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PngConditionSetDlg dialog = new PngConditionSetDlg(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the Dialog for Modify a condition
	 * @param parentDlg
	 * @param hmData
	 * @throws Exception
	 */
	public PngConditionSetDlg(PngDlg parentDlg, PngCondition pngCondition) throws Exception {
		this(parentDlg, true);
		setData(pngCondition);
	}

	/**
	 * Create the dialog.
	 * @param parentDlg
	 * @throws Exception
	 */
	public PngConditionSetDlg(PngDlg parentDlg) throws Exception {
		this(parentDlg, false);
	}
	
	/**
	 * Create the dialog.
	 * @param parentDlg
	 * @param isModify
	 * @throws Exception
	 */
	public PngConditionSetDlg(PngDlg parentDlg, boolean isModify) throws Exception {
		super(parentDlg, true);
		setTitle("Condition Setting");
		setResizable(false);
		setBounds(100, 100, 504, 465);
		this.parentDlg = parentDlg;
		this.isModify = isModify;
		
		init();
	}
	
	private void init() throws Exception{
		
		conditionVec = new Vector<Object>(){

			@Override
			public synchronized String toString() {
				// TODO Auto-generated method stub
				String conditionStr = "";
				for(int i = 0; i < elementCount; i++){
					if( elementData[i] instanceof OpComboValue){
						conditionStr += " " + ((OpComboValue)elementData[i]).getOption();
					}else{
						conditionStr += " " + elementData[i].toString();
					}
				}
				
				return conditionStr.trim();
			}
			
		};
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(10, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_north = new JPanel();
				panel.add(panel_north, BorderLayout.NORTH);
				panel_north.setLayout(new GridLayout(2, 0, 0, 0));
				{
					JPanel panel_2 = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
					flowLayout.setAlignment(FlowLayout.LEADING);
					panel_north.add(panel_2);
					lblNumber = new JLabel("* Group Number : ");
					panel_2.add(lblNumber);
					
					spGroupNum = new JSpinner();
					spGroupNum.setValue( parentDlg.getMaxNumber() + 1 );
					spGroupNum.setPreferredSize(new Dimension(45, 22));
					spGroupNum.setEnabled(!isModify);
					panel_2.add(spGroupNum);
				}
				JPanel panel_1_1 = new JPanel();
				panel_north.add(panel_1_1);
				FlowLayout fl_panel_1_1 = (FlowLayout) panel_1_1.getLayout();
				fl_panel_1_1.setAlignment(FlowLayout.LEADING);
				{
					JLabel lblProduct = new JLabel("* Product : ");
					panel_1_1.add(lblProduct);
				}
				cbProduct = new JComboBox();
				cbProduct.setModel(new DefaultComboBoxModel(new String[] {PngDlg.SELECT_PRODUCT}));
				cbProduct.setEnabled(!isModify);
				panel_1_1.add(cbProduct);
			}
			{
				JPanel panel_center = new JPanel();
				panel.add(panel_center, BorderLayout.CENTER);
				panel_center.setLayout(new BorderLayout(0, 0));
				JPanel panel_2 = new JPanel();
				panel_center.add(panel_2, BorderLayout.NORTH);
				panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Logical operation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panel_2.setLayout(new GridLayout(2, 1, 0, 0));
				JPanel panel_1_1 = new JPanel();
				panel_2.add(panel_1_1);
				FlowLayout fl_panel_1_1 = (FlowLayout) panel_1_1.getLayout();
				fl_panel_1_1.setAlignment(FlowLayout.LEADING);
				
				cbOption = new JComboBox();
				cbOption.setMaximumRowCount(30);
				cbOption.addItem("Select Option");
				
				// [NoSR][20160331][jclee] Key Selection Manager에 의한 Item Selection으로 인해 Condition Text Box에 자동 입력되는 현상 수정 
//				cbOption.addItemListener(new ItemListener() {
//					public void itemStateChanged(ItemEvent event) {
//						if( event.getStateChange() == ItemEvent.SELECTED){
//							Object obj = cbOption.getSelectedItem();
//							if( obj instanceof OpValueName){
//								conditionVec.add(obj);
//								tfCondition.setText(conditionVec.toString());
//							}
//						}
//					}
//				});
				
				Object objPopup = cbOption.getAccessibleContext().getAccessibleChild(0);
				BasicComboPopup popupOption = (BasicComboPopup) objPopup;
				@SuppressWarnings("rawtypes")
				JList lstOption = popupOption.getList();
				lstOption.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent paramMouseEvent) {
						@SuppressWarnings("rawtypes")
						Object obj = ((JList)paramMouseEvent.getSource()).getSelectedValue();
						if( obj instanceof OpValueName){
							conditionVec.add(obj);
							tfCondition.setText(conditionVec.toString());
						}
					}
				});
				
				cbOption.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent paramKeyEvent) {
						if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
							@SuppressWarnings("rawtypes")
							Object obj = ((JComboBox)paramKeyEvent.getSource()).getSelectedItem();
							if( obj instanceof OpValueName){
								conditionVec.add(obj);
								tfCondition.setText(conditionVec.toString());
							}
						}
					}
				});
				
				panel_1_1.add(cbOption);
				{
					JPanel panel_11 = new JPanel();
					FlowLayout fl_panel_11 = (FlowLayout) panel_11.getLayout();
					fl_panel_11.setAlignment(FlowLayout.LEADING);
					panel_2.add(panel_11);
					JRadioButton rdbtnAnd = new JRadioButton("AND");
					panel_11.add(rdbtnAnd);
					rdbtnAnd.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							conditionVec.add("AND");
							tfCondition.setText(conditionVec.toString());
							JRadioButton btn = (JRadioButton)e.getSource();
							btn.setSelected(false);
						}
					});
					
					JRadioButton rdbtnOr = new JRadioButton("OR");
					panel_11.add(rdbtnOr);
					rdbtnOr.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							conditionVec.add("OR");
							tfCondition.setText(conditionVec.toString());
							JRadioButton btn = (JRadioButton)e.getSource();
							btn.setSelected(false);
						}
					});
					
					JRadioButton rdBtnLeft = new JRadioButton("(");
					panel_11.add(rdBtnLeft);
					rdBtnLeft.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							conditionVec.add("(");
							tfCondition.setText(conditionVec.toString());
							JRadioButton btn = (JRadioButton)e.getSource();
							btn.setSelected(false);
						}
					});
					
					JRadioButton rdBtnRight = new JRadioButton(")");
					rdBtnRight.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							conditionVec.add(")");
							tfCondition.setText(conditionVec.toString());
							JRadioButton btn = (JRadioButton)e.getSource();
							btn.setSelected(false);
						}
					});
					
					panel_11.add(rdBtnRight);
					
					JRadioButton rdbtnNot = new JRadioButton("NOT");
					rdbtnNot.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							conditionVec.add("NOT");
							tfCondition.setText(conditionVec.toString());
							JRadioButton btn = (JRadioButton)e.getSource();
							btn.setSelected(false);
						}
					});
					panel_11.add(rdbtnNot);
					JPanel panel_3 = new JPanel();
					panel_3.setPreferredSize(new Dimension(10, 40));
					panel_center.add(panel_3, BorderLayout.CENTER);
					panel_3.setBorder(new TitledBorder(null, "Condition", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					FlowLayout fl_panel_3 = (FlowLayout) panel_3.getLayout();
					fl_panel_3.setAlignment(FlowLayout.LEADING);
					tfCondition = new JTextField();
					Font font = tfCondition.getFont();
					// same font but bold
					Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
					tfCondition.setFont(boldFont);
					
					// Modify Mode일 경우 Condition Text Field 편집 가능.
//					tfCondition.setEditable(false);
					tfCondition.setEditable(isModify);
					tfCondition.addKeyListener(new KeyAdapter() {
						@Override
						public void keyTyped(KeyEvent paramKeyEvent) {
							resetConditionVec();
						}
					});
					
					panel_3.add(tfCondition);
					tfCondition.setColumns(25);
					{
						JButton btnCe = new JButton("CE");
						btnCe.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								conditionVec.remove(conditionVec.size() - 1);
								tfCondition.setText(conditionVec.toString());
								cbOption.setSelectedIndex(0);
							}
						});
						panel_3.add(btnCe);
						
						JButton btnReset = new JButton("C");
						btnReset.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								conditionVec.clear();
								tfCondition.setText(conditionVec.toString());
								cbOption.setSelectedIndex(0);
							}
						});
						panel_3.add(btnReset);
					}
					{
						JPanel panel_1_2 = new JPanel();
						panel_center.add(panel_1_2, BorderLayout.SOUTH);
						FlowLayout fl_panel_1_2 = (FlowLayout) panel_1_2.getLayout();
						fl_panel_1_2.setAlignment(FlowLayout.LEADING);
						{
							JLabel lblNewLabel = new JLabel("* Quantity : ");
							panel_1_2.add(lblNewLabel);
						}
						{
							cbOperator = new JComboBox();
							cbOperator.setModel(new DefaultComboBoxModel(new String[] {"=", "<", ">"}));
							panel_1_2.add(cbOperator);
						}
						{
							spQty = new JSpinner();
							spQty.setPreferredSize(new Dimension(50, 22));
							spQty.setValue(1);
							panel_1_2.add(spQty);
						}
					}
					
					JPanel panel_south = new JPanel();
					panel.add(panel_south, BorderLayout.SOUTH);
					panel_south.setLayout(new BorderLayout(0, 0));
					
					JPanel panel_1 = new JPanel();
					panel_1.setBorder(new TitledBorder(null, "Part Name Selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					panel_south.add(panel_1, BorderLayout.CENTER);
					panel_1.setLayout(new BorderLayout(0, 0));
					
//					JPanel panel_1_2 = new JPanel();
//					panel_1.add(panel_1_2, BorderLayout.NORTH);
//					FlowLayout fl_panel_1_2 = (FlowLayout) panel_1_2.getLayout();
//					fl_panel_1_2.setAlignment(FlowLayout.LEADING);
//					{
//						chkEachCount = new JCheckBox("Each Count");
//						chkEachCount.addItemListener(new ItemListener(){
//
//							@Override
//							public void itemStateChanged(ItemEvent event) {
//								// TODO Auto-generated method stub
//								if (event.getStateChange() == ItemEvent.SELECTED){
//									partNameTable.setEnabled(true);
//								}else if(event.getStateChange() == ItemEvent.DESELECTED){
//									partNameTable.setEnabled(false);
//								}
//							}
//							
//						});
//						panel_1_2.add(chkEachCount);
//					}
					
					Vector header = new Vector();
					header.add("");
					header.add("");
					
					DefaultTableModel model = new DefaultTableModel(null, header){
						
						@Override
						public boolean isCellEditable(int row, int column) {
							// TODO Auto-generated method stub
							if( column < 1){
								return true;
							}
							return false;
						}

						public Class getColumnClass(int column) {
							if( column == 0){
								return Boolean.class;
							}else{
								return String.class;
							}
						}
					};
					DefaultTableModel nameModel = (DefaultTableModel)parentDlg.nameGroupRegPanel.partNameTable.getModel();
					for( int i = 0; i < nameModel.getRowCount(); i++){
						String name = (String)nameModel.getValueAt(i, 0);
						Vector row = new Vector();
						row.add(new Boolean(false));
						row.add(name);
						model.addRow(row);
					}
					partNameTable = new JTable(model);
					TableColumnModel cm = partNameTable.getColumnModel();
					int[] width = new int[]{20, 250};
					for( int i = 0; i < cm.getColumnCount(); i++){
						cm.getColumn(i).setPreferredWidth(width[i]);
						cm.getColumn(i).setResizable(true);
					}
					
					partNameTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					partNameTable.setTableHeader(null);
					JScrollPane scrollPane = new JScrollPane(partNameTable);
					scrollPane.setPreferredSize(new Dimension(452, 100));
					panel_1.add(scrollPane);
					
				}
			}
		}
		{
			{
				ArrayList<OpValueName> opValueList = getAllOption();
				Collections.sort(opValueList);
				for( OpValueName opValueName : opValueList){
					cbOption.addItem(new OpComboValue(opValueName));
				};
			}
		}
		{
			{
				{
					ArrayList<String> list = parentDlg.getProductList();
					for( String productID : list){
						cbProduct.addItem(productID);
					}
				}
			}
		}
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						
						int spNumValue = 0;
						Object spObj = spGroupNum.getValue();
						try{
							Integer intObj = new Integer(spObj.toString());
							spNumValue = intObj.intValue();
						}catch(Exception e){
							MessageBox.post(PngConditionSetDlg.this, "Invalid Group Number", "ERROR", MessageBox.ERROR);
							return;
						}
						
						String result = null;
						String condition = conditionVec.toString();
						if( condition != null && !condition.equals("")){
							
							condition = condition.replaceAll("NOT", "!");
							condition = condition.replaceAll(" AND ", " && ");
							condition = condition.replaceAll(" OR ", " || ");
							Pattern p = Pattern.compile("[a-zA-Z0-9]{4}");
							Matcher m = p.matcher(condition);
							result = m.replaceAll("true");
							Object obj;
							try {
								obj = parentDlg.getEngine().eval(result);
								if( !(obj instanceof Boolean)){
									MessageBox.post(PngConditionSetDlg.this, "Invalid Condition", "ERROR", MessageBox.ERROR);
									return;
								}
								
								if(cbProduct.getSelectedItem().equals(PngDlg.SELECT_PRODUCT)){
									MessageBox.post(PngConditionSetDlg.this, PngDlg.SELECT_PRODUCT, "ERROR", MessageBox.ERROR);
									cbProduct.setFocusable(true);
									return;
								}
								
								int qty = -1;
								try{
									qty = (Integer)spQty.getValue();
									if( qty < 0){
										throw new Exception("Invalid Quantity");
									}
								}catch(Exception e1){
									MessageBox.post(PngConditionSetDlg.this, e1.getMessage(), "ERROR", MessageBox.ERROR);
									spQty.setFocusable(true); 
									return;
								}
								
								boolean isPartNameSelected = false;
								DefaultTableModel model = (DefaultTableModel)partNameTable.getModel();
								if (isModify) {
									ArrayList<String> alPartNames = new ArrayList<String>();
									for (int inx = 0; inx < model.getRowCount(); inx++) {
										Boolean bObj = (Boolean)model.getValueAt(inx, 0);
										if( bObj.booleanValue() ){
											isPartNameSelected = true;
											alPartNames.add((String)model.getValueAt(inx, 1));
										}
									}
									parentDlg.modifyCondition(spNumValue, (String)cbProduct.getSelectedItem(), conditionVec.toString(), (String)cbOperator.getSelectedItem(), qty, alPartNames);
								} else {
									for( int i = 0; i < model.getRowCount(); i++){
										Boolean bObj = (Boolean)model.getValueAt(i, 0);
										if( bObj.booleanValue() ){
											isPartNameSelected = true;
											parentDlg.addCondition(spNumValue, (String)cbProduct.getSelectedItem(), conditionVec.toString(), (String)cbOperator.getSelectedItem(), qty, (String)model.getValueAt(i, 1));
										}
									}
								}
								
								if( !isPartNameSelected){
									if (isModify) {
										ArrayList<String> alPartNames = new ArrayList<String>();
										alPartNames.add("");
										parentDlg.modifyCondition(spNumValue, (String)cbProduct.getSelectedItem(), conditionVec.toString() , (String)cbOperator.getSelectedItem(), qty, alPartNames);
									} else {
										parentDlg.addCondition(spNumValue, (String)cbProduct.getSelectedItem(), conditionVec.toString() , (String)cbOperator.getSelectedItem(), qty, "");
									}
								}
								
								dispose();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								MessageBox.post(PngConditionSetDlg.this, "Invalid Condition", "ERROR", MessageBox.ERROR);
								return;
							}
						}
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
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}		
	}
	
	private ArrayList<OpValueName> getAllOption() throws Exception{
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		
		ds.put("DATA", null);
		try {
			
			ArrayList<HashMap<String, String>> optionList = (ArrayList<HashMap<String, String>>)remote.execute("com.kgm.service.VariantService", "getVariantValueDesc", ds);
			
			ArrayList<OpValueName> list = new ArrayList();
			for( HashMap<String, String> map : optionList){
				String option = map.get("CODE_NAME");
				String optionName = map.get("CODE_DESC");
				OpValueName value = new OpValueName("", "", option, optionName){

					@Override
					public String toString() {
						return option + " : " + optionName;
					}
					
				};
				list.add(value);
			}
			
			return list;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}		
	}
	
	/**
	 * Set Data
	 * @param pngCondition
	 */
	private void setData(PngCondition pngCondition) {
		int iGroupNumber = pngCondition.getGroupNumber();
		String sProduct = pngCondition.getProduct();
		String sCondition = pngCondition.getCondition();
		String sOperator = pngCondition.getOperator();
		int iQTY = pngCondition.getQuantity();
		ArrayList<String> alPartName = pngCondition.getPartNameList();
		
		spGroupNum.setValue(iGroupNumber);
		cbProduct.setSelectedItem(sProduct);
		
		String[] saConditions = sCondition.split(" ");
		for (int inx = 0; inx < saConditions.length; inx++) {
			String sSplittedCondition = saConditions[inx];
			conditionVec.add(sSplittedCondition);
		}
		tfCondition.setText(conditionVec.toString());
		
		cbOperator.setSelectedItem(sOperator);
		spQty.setValue(iQTY);
		
		for (int inx = 0; inx < alPartName.size(); inx++) {
			String sPartName = alPartName.get(inx);
			
			DefaultTableModel model = (DefaultTableModel)partNameTable.getModel();
			for (int jnx = 0; jnx < model.getRowCount(); jnx++) {
				String sTablePartName = model.getValueAt(jnx, 1).toString();
				
				if (sPartName.equals(sTablePartName)) {
					model.setValueAt(true, jnx, 0);
				}
			}
		}
	}
	
	/**
	 * Reset Condition Vector
	 */
	private void resetConditionVec() {
		conditionVec.clear();
		String sCondition = tfCondition.getText();
		String[] saSplittedConditions = sCondition.split(" ");
		for (int inx = 0; inx < saSplittedConditions.length; inx++) {
			String sSplittedCondition = saSplittedConditions[inx];
			conditionVec.add(sSplittedCondition);
		}
	}
}
