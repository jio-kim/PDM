package com.ssangyong.commands.weight;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpCategory;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.commands.ospec.op.OpValueName;
import com.ssangyong.commands.ospec.op.Option;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Registry;

/**
 * [SR170707-024] E-BOM Weight Report 개발 요청
 * @Copyright : Plmsoft
 * @author   : 이정건
 * @since    : 2017. 7. 10.
 * Package ID : com.ssangyong.commands.weight.EBOMWeightMasterListDialog.java
 */
@SuppressWarnings({ "unchecked" })

// 2024.01.09 수정
public class EBOMWeightMasterListDialog extends AbstractAIFDialog {
	private static final long serialVersionUID = 1L;
	private TCSession session = null;
	private TCComponentItemRevision oSpecRevision = null;
	private OSpec ospec = null;
	private TCComponentItem selectedProductItem = null;

	private JPanel contentPanel = new JPanel();
	private JTable table;
	private JScrollPane scroll = null;
	private CustomTableModel tableModel = null;
	private MultiLineHeaderRenderer headerRenderer = new MultiLineHeaderRenderer();
	private WaitProgressBar waitBar = null;
	private ArrayList<String> defaultTableColumnHeader = new ArrayList<String>();
	private ArrayList<Integer> defaultColumnWidths = new ArrayList<Integer>();

	private JComboBox comboProduct;
	private JList comboVariant;
	private HashMap<String, TCComponentItem> productMap = new HashMap<String, TCComponentItem>();
	private Registry registry;
	private HashMap<String, OpValueName> tmMap = new HashMap<String, OpValueName>(); //Transmission
	private HashMap<String, OpValueName> wtMap = new HashMap<String, OpValueName>(); //Wheel Type

	private HashMap<String, OpValueName> tmMap1 = new HashMap<String, OpValueName>(); //Transmission
	private HashMap<String, OpValueName> wtMap1 = new HashMap<String, OpValueName>(); //Wheel Type
	// 옵션 컬럼 추가 
	public static String[] column = {"No", "TEAM", "담당자", "SYSCODE", "FUNCTION", "SEQ", "LEV\n(MAN)", "PART NO", "PART NAME", "S/MODE", "OPTION", "ACT WEIGHT\n(EA)" };
	public static Integer[] column_width =  { 30, 80, 80, 60, 110, 60, 50, 100, 200, 60, 100, 60 };
	private StoredOptionSet sos;
	private TCComponentItem variantItem;
	private EBOMUsageListOperation usageListOperation;
	ArrayList<StoredOptionSet> usageOptionSetList;
	
	public EBOMWeightMasterListDialog(Frame parentFrame, TCSession tcSession) throws Exception {
		super(parentFrame, false);
		registry = Registry.getRegistry("com.ssangyong.common.common");

		this.session = tcSession;

		init();

		execInitOperation(this);
	}

	private void execInitOperation(final AbstractAIFDialog dlg) throws Exception {
		try {
			final EBOMWeightMasterListDialogInitOperation initOp = new EBOMWeightMasterListDialogInitOperation();
			initOp.addOperationListener(new InterfaceAIFOperationListener() {
				@Override
				public void startOperation(String arg0) {
				}

				@Override
				public void endOperation() {
					try {
						ArrayList<TCComponentItem> findProducts = (ArrayList<TCComponentItem>) initOp.getOperationResult();

						if (initOp.isOperationDone() && findProducts != null && findProducts.size() > 0) {
							comboProduct.removeAllItems();
							comboProduct.addItem("");

							for (TCComponentItem prod : findProducts) {
								TCComponentItemRevision prodRev = prod.getLatestItemRevision();

								String item_id = prodRev.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
								comboProduct.addItem(item_id);
								if (!productMap.containsKey(item_id))
									productMap.put(item_id, prodRev.getItem());
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			session.queueOperation(initOp);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private void init() throws Exception {
		setTitle("Weight Master List");

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		defaultTableColumnHeader.addAll(Arrays.asList(column));
		defaultColumnWidths.addAll(Arrays.asList(column_width));

		tableModel = new CustomTableModel(defaultTableColumnHeader.toArray(new String[0]), 0);

		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		table.setShowGrid(true);
		table.setAutoCreateRowSorter(true);
		// table.setRowSorter(createRowSorter(table));
		Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			((TableColumn) cols.nextElement()).setHeaderRenderer(headerRenderer);
		}
		for (int i = 0; i < defaultColumnWidths.size(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(defaultColumnWidths.get(i));
		}

		CustomCellRenderer alignRenderer = new CustomCellRenderer();
		DefaultTableCellRenderer alignLeftRenderer = new DefaultTableCellRenderer();
		alignLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		DefaultTableCellRenderer alignCenterRenderer = new DefaultTableCellRenderer();
		alignCenterRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		for (int i = 0; i < table.getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setCellRenderer(alignRenderer);

		scroll = new JScrollPane(table);
		contentPanel.add(scroll, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		JButton cancelButton = new JButton("Close", registry.getImageIcon("Cancel_24.ICON"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				disposeDialog();
			}
		});
		buttonPanel.add(cancelButton);
		
		JPanel conditionPanel = new JPanel();
		contentPanel.add(conditionPanel, BorderLayout.NORTH);
		conditionPanel.setOpaque(false);
		GridBagLayout gbl_conditionPanel = new GridBagLayout();
		gbl_conditionPanel.columnWidths = new int[] { 43, 80, 10, 39, 80, 0, 10, 80, 0, 40, 80, 30, 25, 50, 15, 73, 20, 90, 0 };
		gbl_conditionPanel.rowHeights = new int[] { 80, 0 };
		gbl_conditionPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_conditionPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		conditionPanel.setLayout(gbl_conditionPanel);

		JLabel lblProduct = new JLabel("Product");
		lblProduct.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblProduct = new GridBagConstraints();
		gbc_lblProduct.anchor = GridBagConstraints.WEST;
		gbc_lblProduct.insets = new Insets(0, 0, 0, 5);
		gbc_lblProduct.gridx = 0;
		gbc_lblProduct.gridy = 0;
		conditionPanel.add(lblProduct, gbc_lblProduct);

		comboProduct = new JComboBox();
		comboProduct.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent paramItemEvent) {
				Object selectItem = ((JComboBox) paramItemEvent.getSource()).getSelectedItem();

				if (!paramItemEvent.getItem().equals(selectItem) && selectItem != null) {
					productChangeAction(selectItem.toString());
				}
			}
		});
		GridBagConstraints gbc_comboProduct = new GridBagConstraints();
		gbc_comboProduct.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboProduct.insets = new Insets(0, 0, 0, 5);
		gbc_comboProduct.gridx = 1;
		gbc_comboProduct.gridy = 0;
		conditionPanel.add(comboProduct, gbc_comboProduct);

		JLabel label = new JLabel("  ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 0, 5);
		gbc_label.gridx = 2;
		gbc_label.gridy = 0;
		conditionPanel.add(label, gbc_label);

		JLabel lblVariant = new JLabel("Variant");
		GridBagConstraints gbc_lblVariant = new GridBagConstraints();
		gbc_lblVariant.anchor = GridBagConstraints.WEST;
		gbc_lblVariant.insets = new Insets(0, 0, 0, 5);
		gbc_lblVariant.gridx = 3;
		gbc_lblVariant.gridy = 0;
		conditionPanel.add(lblVariant, gbc_lblVariant);

		comboVariant = new JList(new DefaultListModel());
//		comboVariant.setSize(new Dimension(200, 100));
		JScrollPane pane = new JScrollPane();
		pane.setPreferredSize(new Dimension(130, 100));
		pane.setViewportView(comboVariant);
		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.getViewport().setBackground(Color.WHITE);
		GridBagConstraints gbc_pane = new GridBagConstraints();
		gbc_pane.fill = GridBagConstraints.BOTH;
		conditionPanel.add(pane, gbc_pane);

		JLabel label_1 = new JLabel("  ");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 0, 5);
		gbc_label_1.gridx = 5;
		gbc_label_1.gridy = 0;
		conditionPanel.add(label_1, gbc_label_1);

//		JLabel label_3 = new JLabel("  ");
//		GridBagConstraints gbc_label_3 = new GridBagConstraints();
//		gbc_label_3.insets = new Insets(0, 0, 0, 130);
//		gbc_label_3.gridx = 11;
//		gbc_label_3.gridy = 0;
//		conditionPanel.add(label_3, gbc_label_3);

		JButton btnSearch = new JButton("Search");
		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.insets = new Insets(0, 0, 0, 10);
		gbc_btnSearch.anchor = GridBagConstraints.WEST;
		gbc_btnSearch.gridx = 12;
		gbc_btnSearch.gridy = 0;
		conditionPanel.add(btnSearch, gbc_btnSearch);
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				searchAction();
			}
		});

		JButton btnExportToExcel = new JButton("Download");
		GridBagConstraints gbc_btnExportToExcel = new GridBagConstraints();
		gbc_btnExportToExcel.insets = new Insets(0, 0, 0, 5);
		gbc_btnExportToExcel.anchor = GridBagConstraints.WEST;
		gbc_btnExportToExcel.gridx = 13;
		gbc_btnExportToExcel.gridy = 0;
		conditionPanel.add(btnExportToExcel, gbc_btnExportToExcel);
		btnExportToExcel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exportToExcelAction();
			}
		});

		setMinimumSize(new Dimension(1024, 768));

		pack();
		centerToScreen(1.1D, 1.0D);
	}

	protected void exportToExcelAction() {
		try {
			if (table.getModel().getRowCount() == 0) {
				MessageBox.post(EBOMWeightMasterListDialog.this, "조회된 결과가 없습니다.", "확인", MessageBox.INFORMATION);
				return;
			}

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			Calendar now = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
			sdf.format(now.getTime());
			File defaultFile = new File("WeightMasterList_" + sdf.format(now.getTime()) + ".xls");
			fileChooser.setSelectedFile(defaultFile);
			fileChooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isFile()) {
						return f.getName().endsWith("xls");
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "*.xls";
				}
			});
			int result = fileChooser.showSaveDialog(EBOMWeightMasterListDialog.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				try {
					waitBar = new WaitProgressBar(this);
					waitBar.start();
					try {
						//수정
						Vector<Vector> rawData = tableModel.getDataVector();
						Vector<Vector<Object>> dataVec = new Vector<>(rawData);
						final EBOMWeightMasterListExcelExportOperation exportOp = new EBOMWeightMasterListExcelExportOperation( selectedFile,
								tableModel.getIdentifier(), dataVec, selectedProductItem);
//						final EBOMWeightMasterListExcelExportOperation exportOp = new EBOMWeightMasterListExcelExportOperation( selectedFile,
//								tableModel.getIdentifier(), tableModel.getDataVector(), selectedProductItem);
						exportOp.addOperationListener(new InterfaceAIFOperationListener() {
							@Override
							public void startOperation(String paramString) {
							}

							@Override
							public void endOperation() {
								Object opResult = exportOp.getOperationResult();
								if (opResult != null && !opResult.equals("Success") && opResult instanceof String) {
									waitBar.setStatus(opResult.toString());
									waitBar.setShowButton(true);
								} else {
									waitBar.close();
								}
							}
						});
						session.queueOperation(exportOp);
					} catch (Exception ex) {
						waitBar.setStatus(ex.getMessage());
						waitBar.setShowButton(true);
						throw ex;
					}
				} catch (Exception ioe) {
					ioe.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private StoredOptionSet getStoredOptionSet( String trim, HashMap<String, OpCategory> categoryMap, String E00, String E10 ) throws Exception {
		
		System.out.println("getStoredOptionSet E00 : ==>" +E00 + " ,E10 : ==>" + E10);
		
		StoredOptionSet sosTemp = new StoredOptionSet( trim + " " + E00 + " " + E10);
		
		OpCategory e00Category = categoryMap.get("E00");
		
		OpCategory e10Category = categoryMap.get("E10");
		
		List<Option> e00List = e00Category.getOpValueList();
		
		List<Option> e10List = e10Category.getOpValueList();
		
		Set<String> categoryKeySet = categoryMap.keySet();

		Option e00Opton = null;
		
		Option e10Option = null;
		
		for (Iterator iterator = e00List.iterator(); iterator.hasNext();) {
		
			Option e00OptonTmp = (Option) iterator.next();
			
			if( e00OptonTmp.getOpValue().equals(E00) ){
				e00Opton = e00OptonTmp;
				sosTemp.add(e00Opton.getOp() , e00Opton.getOpValue() );
				break;
			}
			
		}
		
		for (Iterator iterator = e10List.iterator(); iterator.hasNext();) {
			Option e10OptionTmp = (Option) iterator.next();
			
			if( e10OptionTmp.getOpValue().equals(E10) ){
				e10Option = e10OptionTmp;
				sosTemp.add(e10Option.getOp() , e10Option.getOpValue() );
				break;
			}
			
		}
		
		if( e00Opton == null || e10Option == null ){
			System.out.println("NOTEXIST");
			return null;
		}
		
		for (Iterator<String> iterator = categoryKeySet.iterator(); iterator.hasNext();) {
			
			String categoryKey = iterator.next();
			
			if( "E00".equals(categoryKey) || "E10".equals(categoryKey) )
				continue;
			else{
				
				OpCategory opCategory = categoryMap.get ( categoryKey );
				
				ArrayList<Option> opList = opCategory.getOpValueList();
				Option std  = null;
				Option mand = null;
				for (Iterator iterator2 = opList.iterator(); iterator2.hasNext();) {
					
					Option option = (Option) iterator2.next();
					String value = option.getValue();
					if( value != null ){
						if( "S".equals(value) ){
							std = option;
						}else if ( value.substring(0,1).equals("M") ){
							
							String remark = option.getRemark();
							
							if( remark != null ){
								int start = remark.indexOf("(") +1;
								int end = remark.indexOf(")");
								
								String remarkOption =  remark.substring(start, end);
								
								
								String[] orList = remarkOption.split("OR");
								boolean result = true;
								for (int i = 0; i < orList.length; i++) {
									String[] andList = orList[i].trim().split("AND");
									
									for (int j = 0; j < andList.length; j++) {
									
										String tmpValue = andList[j].trim();
										
										if( ! E00.equals(tmpValue) && ! E10.equals( tmpValue) ){
											result = false;
											break;
										}
									}
									
									if( result ) break;
									
									
								}
								
								if( result ){

									mand = option;
									
								}
							}
							
						}
								
					}
					
					
				}
				
				if( mand != null ){
					sosTemp.add(mand.getOp() , mand.getOpValue() );
				}else if (  std != null ) {
					sosTemp.add(std.getOp() , std.getOpValue() );
				}
				
			}
			
			
		}
		ArrayList<String> sosList = sosTemp.getValueList();
		
		Collections.sort(sosList);
		
		for (Iterator iterator = sosList.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			
			System.out.println(string);
			
		}
		System.out.println("--------------------------------------");
		
		return sosTemp;
		
	}

	protected void searchAction() {
		try {
			String selectedProduct = (String) comboProduct.getSelectedItem();
			String selectedVariant = (String) comboVariant.getSelectedValue();

			if (selectedProduct == null || selectedProduct.trim().equals("")) {
				MessageBox.post(this, "조회할 Product를 선택해 주세요.", "확인", MessageBox.INFORMATION);
				return;
			}
			if (selectedVariant == null || selectedVariant.trim().equals("")) {
				MessageBox.post(this, "조회할 Variant를 선택해 주세요.", "확인", MessageBox.INFORMATION);
				return;
			}
			variantItem = CustomUtil.findItem(SYMCClass.S7_VARIANTPARTTYPE, selectedVariant);
			setOptionSet(variantItem.getLatestItemRevision());
			try {
				selectedProductItem = productMap.get(selectedProduct);
				
				PlatformHelper.getCurrentDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						usageListOperation = new EBOMUsageListOperation(EBOMWeightMasterListDialog.this);
						String selectedTrim = null;
						
						try {
							ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformHelper.getCurrentShell());
							dialog.run(true, false, usageListOperation);
							selectedTrim = variantItem.getStringProperty("item_id").substring(1,6);
							
							String[] TRANSMISSION_ARRAY = new String[]{"E00C","EOOL"};
							String[] TRANSFERCASE_ARRAY = new String[]{"E10A","E102"};
							
							HashMap<String, HashMap<String, OpCategory>> trimCategoryMap = ospec.getCategory();
							HashMap<String, OpCategory> categoryMap = trimCategoryMap.get( selectedTrim );
							
//							OpCategory e00Category = categoryMap.get("E00");
//							OpCategory e10Category = categoryMap.get("E10");
							usageOptionSetList = new ArrayList<StoredOptionSet>();
							for( int i = 0; i < TRANSMISSION_ARRAY.length ; i ++  ){
								
								for( int j = 0; j < TRANSFERCASE_ARRAY.length ; j ++  ){
									
									String E00 = TRANSMISSION_ARRAY[i];
									String E10 = TRANSFERCASE_ARRAY[j];
									
									StoredOptionSet sosTemp = getStoredOptionSet(selectedTrim, categoryMap, E00, E10);
									
									if( sosTemp != null ){
										usageOptionSetList.add(sosTemp );
										usageOptionSetList.add(sosTemp );
									}
								}
							
							}
							
							
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (Exception e ) {
							e.printStackTrace();
						}
						
						
						final EBOMWeightMasterListSearchOperation searchOperation = new EBOMWeightMasterListSearchOperation(EBOMWeightMasterListDialog.this, table, selectedProductItem,  sos, usageOptionSetList, wtMap, tmMap, defaultTableColumnHeader.size(), waitBar);
						
						searchOperation.addOperationListener(new InterfaceAIFOperationListener() {
							@Override
							public void startOperation(String arg0) {
								table.removeAll();
								tableModel.setNumRows(0);
//								table.getModel().
								//tableModel.getDataVector().clear();
								CustomCellRenderer alignRenderer = new CustomCellRenderer();

								for (int i = table.getColumnCount() - 1; i >= defaultTableColumnHeader.size(); i--) {
									table.removeColumn(table.getColumnModel().getColumn(i));
								}
								
								table.repaint();

								Vector<String> headerIdentifier = new Vector<String>();
								headerIdentifier.addAll(defaultTableColumnHeader);
								for( StoredOptionSet ssos : usageOptionSetList ){
									TableColumn estWeightColumn = new TableColumn();
									String headerValue = ssos.getName().replaceAll("_BASE", "") ;
									estWeightColumn.setHeaderValue(headerValue);
									estWeightColumn.setPreferredWidth(74);
									estWeightColumn.setHeaderRenderer(headerRenderer);
									estWeightColumn.setCellRenderer(alignRenderer);
									
									table.addColumn(estWeightColumn);
									headerIdentifier.add(headerValue+" W ");
								
								}
								/**
								String[] weight_header = {"Team Weight", "Act Weight"};
								for (String wt : wtMap1.keySet()) {
									for (String tm : tmMap1.keySet()) {
										String postfixStr = "";
										if (tm.indexOf('(') > 0)
											postfixStr = tm.substring(tm.indexOf('('));
										tm = tm.replaceAll("/", "");
										tm = tm.replaceAll("-", "");

										if (!tm.contains("DCT")) {
											tm = tm.substring(2, 3) + tm.substring(0, 2) + postfixStr;
										}

										TableColumn estWeightColumn = new TableColumn();
										String headerValue = sos.getName().replaceAll("_BASE", "") + "\n" + wt + "\n" + tm + "\n" + weight_header[0];
										estWeightColumn.setHeaderValue(headerValue);
										estWeightColumn.setPreferredWidth(74);
										estWeightColumn.setHeaderRenderer(headerRenderer);
										estWeightColumn.setCellRenderer(alignRenderer);

										table.addColumn(estWeightColumn);
										headerIdentifier.add(headerValue);

										TableColumn actWeightColumn = new TableColumn();
										String headerValue1 = sos.getName().replaceAll("_BASE", "") + "\n" + wt + "\n" + tm + "\n" + weight_header[1];
										actWeightColumn.setHeaderValue(headerValue1);
										actWeightColumn.setPreferredWidth(74);
										actWeightColumn.setHeaderRenderer(headerRenderer);
										actWeightColumn.setCellRenderer(alignRenderer);

										table.addColumn(actWeightColumn);
										headerIdentifier.add(headerValue1);
									}
								}
								/**
								TableColumn estWeightColumn = new TableColumn();
								String headerValue = sos.getName().replaceAll("_BASE", "") + "\n" + "Weight/Veh." + "\n" + weight_header[0];
								estWeightColumn.setHeaderValue(headerValue);
								estWeightColumn.setPreferredWidth(74);
								estWeightColumn.setHeaderRenderer(headerRenderer);
								estWeightColumn.setCellRenderer(alignRenderer);

								table.addColumn(estWeightColumn);
								headerIdentifier.add(headerValue);
								
								TableColumn actWeightColumn = new TableColumn();
								String headerValue1 = sos.getName().replaceAll("_BASE", "") + "\n" + "Weight/Veh." + "\n" + weight_header[1];
								actWeightColumn.setHeaderValue(headerValue1);
								actWeightColumn.setPreferredWidth(74);
								actWeightColumn.setHeaderRenderer(headerRenderer);
								actWeightColumn.setCellRenderer(alignRenderer);

								table.addColumn(actWeightColumn);
								headerIdentifier.add(headerValue1);
								*/
								ArrayList<TableCellRenderer> cellRenderers = new ArrayList<TableCellRenderer>();
								ArrayList<Integer> columnWidths = new ArrayList<Integer>();
								ArrayList<TableCellRenderer> headerRenderers = new ArrayList<TableCellRenderer>();
								for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
									cellRenderers.add(table.getColumnModel().getColumn(i).getCellRenderer());
									columnWidths.add(table.getColumnModel().getColumn(i).getWidth());
									headerRenderers.add(table.getColumnModel().getColumn(i).getHeaderRenderer());
								}

								tableModel.setColumnIdentifiers(headerIdentifier);
								for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
									table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderers.get(i));
									table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths.get(i));
									table.getColumnModel().getColumn(i).setCellRenderer(cellRenderers.get(i));
								}

								scroll.revalidate();
								table.repaint();

								/** BOM 전개를 API가 아닌 DB쿼리로 하게 될 경우 사용 하는 쿼리[LJG]
								try {
									ArrayList<String> functionList = getChildren(comboVariant.getSelectedValue()); //variant 하위의 모든 Function 리스트
									TCComponentItem productItem = productMap.get(comboProduct.getSelectedItem().toString());
									ArrayList<HashMap<String, String>> variantEpl = new ArrayList<HashMap<String, String>>(); //variant 하위의 모든 EPL(VehPart만)을 담음
									for(int i=0; i<functionList.size(); i++){
										ArrayList<String> fmpList = getChildren(functionList.get(i)); //function 하위의 모든 FMP 리스트
										for(int j=0; j<fmpList.size(); j++){
											ArrayList<HashMap<String, String>> fmpEpl = getEplData(productItem.getStringProperty("item_id"), comboVariant.getSelectedValue(), functionList.get(i), fmpList.get(j));
											for(int k=0; k<fmpEpl.size(); k++){
												variantEpl.add(fmpEpl.get(k));
												System.out.println("[" + k + "] " + fmpEpl.get(k));
											}
										}
									}

									System.out.println("전체 Size ==> " + variantEpl.size());
								} catch (Exception e) {
									e.printStackTrace();
								} */
							}

							@Override
							public void endOperation() {
								Object opResult = searchOperation.getOperationResult();
								if (opResult != null && !opResult.equals("Success") && opResult instanceof String) {
									waitBar.setStatus(opResult.toString());
									waitBar.setShowButton(true);
								} else if (opResult != null && opResult instanceof ArrayList) {
									waitBar.close();
								} else {
									waitBar.close();
								}
							}
						});
						session.queueOperation(searchOperation);
					}
				});
			} catch (Exception ex) {
				waitBar.setStatus(ex.getMessage());
				waitBar.setShowButton(true);
				throw ex;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void productChangeAction(String selectedProduct) {
		try {
			if (selectedProduct == null || selectedProduct.equals("")) {
				((DefaultListModel) comboVariant.getModel()).clear();
				return;
			}

			waitBar = new WaitProgressBar(this);
			waitBar.start();

			final ProductChangeOperation postInit = new ProductChangeOperation(productMap.get(selectedProduct), waitBar);
			postInit.addOperationListener(new InterfaceAIFOperationListener() {
				@Override
				public void startOperation(String paramString) {
				}

				@Override
				public void endOperation() {
					if (postInit.getResult().equals(IStatus.ERROR)) {
						waitBar.setStatus(postInit.getResult().getMessage());
						waitBar.setShowButton(true);
					} else {
						waitBar.close();
					}
				}
			});

			session.queueOperation(postInit);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public class ProductChangeOperation extends AbstractAIFOperation {
		private TCComponentItem prodItem = null;
		private WaitProgressBar waitBar = null;

		public ProductChangeOperation(TCComponentItem selectedProdItem, WaitProgressBar waitBar) {
			prodItem = selectedProdItem;
			this.waitBar = waitBar;
		}

		@Override
		public void executeOperation() throws Exception {
			try {
				waitBar.setStatus("Get Latest Released Revision of Product Item.");
				TCComponentItemRevision prodRev = SYMTcUtil.getLatestReleasedRevision(prodItem);

				String product_id = prodRev.getItem().getStringProperty("item_id");
				ArrayList<String> variantList = null;
				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("PRODUCT_ID", product_id);
				try {
					variantList = (ArrayList<String>)remote.execute("com.ssangyong.service.MasterListService", "getVariantList", ds);
				} catch (Exception e) {
					throw e;
				}
				((DefaultListModel) comboVariant.getModel()).clear();
				for(int i=0; i<variantList.size(); i++){
					((DefaultListModel) comboVariant.getModel()).addElement(variantList.get(i));
				}

				// ProductRevision에 의한 OSpec의 값을 모두 읽어서 E00에 해당하는 Value 및 Description을 뽑아온다.
				waitBar.setStatus("Get OSpec Revision.");
				oSpecRevision = getOSpecRevision(product_id);

				waitBar.setStatus("Read OSpec Data from OSpec.");
				ospec = BomUtil.getOSpec(oSpecRevision);

				waitBar.setStatus("Set T/M and W/T ComboBox value.");

				tmMap.clear();
				wtMap.clear();

				tmMap1.clear();
				wtMap1.clear();

				for (OpValueName opValueName : ospec.getOpNameList()) {
					// TransMission
					if (opValueName.getCategory().equals("E00")) {
						tmMap.put(opValueName.getOption(), opValueName);
						tmMap1.put(opValueName.getOptionName(), opValueName);
					}
					// Wheel Type
					if (opValueName.getCategory().equals("E10")) {
						wtMap.put(opValueName.getOption(), opValueName);
						wtMap1.put(opValueName.getOptionName(), opValueName);
					}
				}
			} catch (Exception ex) {
				throw ex;
			}
		}
	}

	public void setOptionSet(TCComponentItemRevision productRevision) throws Exception {
		AIFComponentContext[] relatedContexts = productRevision.getRelated("IMAN_reference");
		for (AIFComponentContext context : relatedContexts) {
			TCComponent com = (TCComponent) context.getComponent();
			if (com.getType().equals("StoredOptionSet")) {
				String sosName = com.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME);
				sos = new StoredOptionSet(sosName);
				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("PUID", com.getUid());
				ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.MasterListService", "getStoredOptionSet", ds);
				for (int i = 0; list != null && i < list.size(); i++) {
					HashMap<String, String> resultMap = list.get(i);
					sos.add(resultMap.get("POPTION"), resultMap.get("PSTRING_VALUE"));
				}
			}
		}
	}

	public TCComponentItemRevision getOSpecRevision(String product_id) throws Exception {
		String ospecNo = null;
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();

		ds.put("PRODUCT_NO", product_id);
		try {
			ospecNo = (String)remote.execute("com.ssangyong.service.MasterListService", "getOSINo", ds);
		} catch (Exception e) {
			throw e;
		}
		int idx = ospecNo.lastIndexOf("-");
		if (idx < 0) {
			throw new TCException("Invalid OSPEC_NO.");
		}
		String ospecId = ospecNo.substring(0, idx);
		String ospecRevId = ospecNo.substring(idx + 1);
		// 임시방편 수정해야함
		return CustomUtil.findItemRevision("S7_OspecSetRevision", ospecId, ospecRevId);
//		return CustomUtil.findItemRevision("S7_OspecSetRevision", ospecId, "020");
	}

	public class CustomTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		public CustomTableModel(String[] columnHeaders, int defaultRowCount) {
			super(columnHeaders, defaultRowCount);
		}

		public Vector<Object> getIdentifier() {
			return columnIdentifiers;
		}
	}

	public class CustomCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			JLabel label = (JLabel) com;

			if (column == 5) {
				label.setHorizontalAlignment(SwingConstants.LEFT);
			} else {
				label.setHorizontalAlignment(SwingConstants.CENTER);
			}

			if (column == 0) {
				label.setText(row + "");
			}

			if (isSelected) {
				label.setBackground(table.getSelectionBackground());
				label.setForeground(table.getSelectionForeground());
			} else {
				if (table.getRowCount() > 0 && table.getValueAt(row, 8).equals("Total Sum Weight")) {
					label.setBackground(Color.LIGHT_GRAY);
					label.setForeground(table.getForeground());
				} 
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				// Team별 중량 체크 에서 실제 중량과 Team별 중량이 달라진 값의 글자색을 변경
				else if ( column >=12 && column % 2 == 0 ) {
					if( !table.getValueAt(row, 11).equals(table.getValueAt(row, column))){
						label.setForeground(Color.RED);
					}
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				else {
					try {
						label.setBackground(table.getBackground());
						label.setForeground(table.getForeground());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			return com;
		}
	}

	/**
	 * [SR170707-024][ljg] 인자로 받은 Function 하위의 모든 1레벨 Vehpart들
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 9. 28.
	 * @param prodRevision
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, String>> getEplData(String product_id, String variant_id, String function_id, String fmp_id) throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PRODUCT_ID", product_id);
		ds.put("VARIANT_ID", variant_id);
		ds.put("FUNCTION_ID", function_id);
		ds.put("FMP_ID", fmp_id);

		ArrayList<HashMap<String, String>> result = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.MasterListService", "getEpl", ds);
		return result;
	}

	/**
	 * 부모 바로 1레벨 하위의 모든 자식들을 가져옴
	 * @Copyright : ISMS
	 * @author : 이정건
	 * @since  : 2013. 3. 27.
	 * @param revision
	 * @return
	 * @throws TCException
	 */
	public ArrayList<String> getChildren(String parent) throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PARENT_ID", parent);

		ArrayList<String> result = (ArrayList<String>) remote.execute("com.ssangyong.service.MasterListService", "getChildren", ds);
		return result;
	}

	public OSpec getOspec(){
		return ospec;
	}

	public TCComponentItem getProductItem(){
		return selectedProductItem;
	}

	public TCComponentItem getVariantItem(){
		return variantItem;
	}

	public StoredOptionSet getSOS(){
		return sos;
	}

	public HashMap<String, OpValueName> getWtMap(){
		return wtMap;
	}

	public HashMap<String, OpValueName> getTmMap(){
		return tmMap;
	}

	public HashMap<String, HashMap<String, String>> getProductAllChildPartsList(){
		return usageListOperation.getProductAllChildPartsList();
	}

	public HashMap<String, HashMap<String, HashMap<String, String>>> getProductAllChildPartsUsageList(){
		return usageListOperation.getProductAllChildPartsUsageList();
	}
	
	public HashMap<String, String> getTeamWeightMap() {
		return usageListOperation.getTeamWeightMap();
	}
}
