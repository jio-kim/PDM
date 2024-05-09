package com.symc.plm.rac.prebom.prebom.dialog.weightmasterlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.eclipse.core.runtime.IStatus;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpValueName;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.MultiLineHeaderRenderer;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.prebom.operation.weightmasterlist.LatestWeightMasterListSearchOperation;
import com.symc.plm.rac.prebom.prebom.operation.weightmasterlist.WeightMasterListDialogInitOperation;
import com.symc.plm.rac.prebom.prebom.operation.weightmasterlist.WeightMasterListExcelExportOperation;
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
import com.teamcenter.rac.util.Registry;

/***********************************************************************************************************************************************
 * [변경이력]
 * 
 * 1. [2017.07.18][CHC][송대영차장요청] : E100 차량 무게 추가 과련 수정 : E100의 경우 TRANSMISSION이 없음. WHEEL OPTION만 가지고 무게를 확인해야 함.
 * 
 * 
 * *********************************************************************************************************************************************/

/**
 * 최신 Weight Master List UI
 * @author baek
 *
 */
@SuppressWarnings({ "unchecked" })
public class LatestWeightMasterListDialog extends AbstractAIFDialog {
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

	private JComboBox<String> comboProduct;
	private JList<String> comboVariant;
	private HashMap<String, TCComponentItem> productMap = new HashMap<String, TCComponentItem>();
	private Registry registry;
	private HashMap<String, StoredOptionSet> sosMap = new HashMap<String, StoredOptionSet>();
	private HashMap<String, OpValueName> tmMap = new HashMap<String, OpValueName>();
	private HashMap<String, OpValueName> wtMap = new HashMap<String, OpValueName>();
	private Date dateCreateDate;
	private JTextField dataCreatedText;
	private HashMap<String, String> targetData = null; // 조회 대상 정보

	public LatestWeightMasterListDialog(Frame parentFrame, TCSession tcSession) throws Exception {
		super(parentFrame, false);
		registry = Registry.getRegistry("com.kgm.common.common");

		this.session = tcSession;

		init();

		execInitOperation(this);
	}

	private void execInitOperation(final AbstractAIFDialog dlg) throws Exception {
		try {
			waitBar = new WaitProgressBar(this);
			waitBar.start();

			final WeightMasterListDialogInitOperation initOp = new WeightMasterListDialogInitOperation(session, waitBar);
			initOp.addOperationListener(new InterfaceAIFOperationListener() {
				@Override
				public void startOperation(String arg0) {
				}

				@Override
				public void endOperation() {
					try {
						ArrayList<TCComponentItem> findProducts = (ArrayList<TCComponentItem>) initOp.getOperationResult();

						waitBar.setStatus("Loading Product list.");
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

						waitBar.dispose();
					} catch (Exception ex) {
						waitBar.setStatus(ex.getMessage());
						waitBar.setShowButton(true);
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

		defaultTableColumnHeader.addAll(Arrays.asList(new String[] { "No", "TEAM", "담당자", "SYSCODE", "SYSTEM NAME", "FMP", "SEQ", "LEV\n(MAN)", "PART NO", "PART NAME", "S/MODE", "PROJECT", "NMCD", "WEIGHT\n(EA)" }));
		defaultColumnWidths.addAll(Arrays.asList(new Integer[] { 30, 80, 80, 60, 140, 110, 60, 50, 100, 200, 60, 60 , 60, 60 }));

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

		comboProduct = new JComboBox<String>();
		comboProduct.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent paramItemEvent) {
				Object selectItem = ((JComboBox<String>) paramItemEvent.getSource()).getSelectedItem();

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

		comboVariant = new JList<String>(new DefaultListModel<String>());
		JScrollPane pane = new JScrollPane();
		pane.setPreferredSize(new Dimension(100, 80));
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

		JLabel lbldateChooser1 = new JLabel("기준일");
		GridBagConstraints gbc_lbldateChooser1 = new GridBagConstraints();
		gbc_lbldateChooser1.anchor = GridBagConstraints.WEST;
		gbc_lbldateChooser1.insets = new Insets(0, 0, 0, 5);
		gbc_lbldateChooser1.gridx = 6;
		gbc_lbldateChooser1.gridy = 0;
		conditionPanel.add(lbldateChooser1, gbc_lbldateChooser1);

		dataCreatedText = new JTextField();
		GridBagConstraints gbc_comboTM = new GridBagConstraints();
		gbc_comboTM.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboTM.insets = new Insets(0, 0, 0, 5);
		gbc_comboTM.gridx = 7;
		gbc_comboTM.gridy = 0;
		conditionPanel.add(dataCreatedText, gbc_comboTM);
		dataCreatedText.setEditable(false);

		JLabel label_3 = new JLabel("  ");
		GridBagConstraints gbc_label_3 = new GridBagConstraints();
		gbc_label_3.insets = new Insets(0, 0, 0, 130);
		gbc_label_3.gridx = 11;
		gbc_label_3.gridy = 0;
		conditionPanel.add(label_3, gbc_label_3);

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

		// JLabel label_4 = new JLabel("");
		// GridBagConstraints gbc_label_4 = new GridBagConstraints();
		// gbc_label_4.insets = new Insets(0, 0, 0, 5);
		// gbc_label_4.gridx = 13;
		// gbc_label_4.gridy = 0;
		// conditionPanel.add(label_4, gbc_label_4);

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
				MessageBox.post(LatestWeightMasterListDialog.this, "조회된 결과가 없습니다.", "확인", MessageBox.INFORMATION);
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
			int result = fileChooser.showSaveDialog(LatestWeightMasterListDialog.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				try {
					waitBar = new WaitProgressBar(this);
					waitBar.setWindowSize(400, 300);
					waitBar.start();
					try {
						// [20240125] [CSO] 수정
						// WeightMasterListExcelExportOperation(File, Vector<Object>, Vector<Vector>, TCComponentItem) 생성자가 정의X
						// tableModel.getDataVector(); ==> Vector<Vector> 타입임
						// Vector<Vector> => Vector<Vector<Object>> 타입으로 캐스팅
						Vector<Vector> originalDataVector = tableModel.getDataVector();
						
						Vector<Vector<Object>> convertedDataVector = new Vector<>();
						for(Vector row : originalDataVector) {
							Vector<Object> newRow = new Vector<>(row);
							convertedDataVector.add(newRow);
						}
						
						final WeightMasterListExcelExportOperation exportOp = new WeightMasterListExcelExportOperation(selectedFile,
								tableModel.getIdentifier(), convertedDataVector, selectedProductItem);
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
									waitBar.dispose();
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

	protected void searchAction() {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String selectedProduct = (String) comboProduct.getSelectedItem();
			List<String> selectedVariant = comboVariant.getSelectedValuesList();

			if (selectedProduct == null || selectedProduct.trim().equals("")) {
				MessageBox.post(this, "조회할 Product를 선택해 주세요.", "확인", MessageBox.INFORMATION);
				return;
			}
			if (selectedVariant == null || selectedVariant.size() == 0) {
				MessageBox.post(this, "조회할 Variant를 선택해 주세요.", "확인", MessageBox.INFORMATION);
				return;
			}
			String createDate = dataCreatedText.getText();
			dateCreateDate =  sdf.parse(createDate);
			final ArrayList<StoredOptionSet> selectedSOS = new ArrayList<StoredOptionSet>();

			for (String variant : selectedVariant) {
				selectedSOS.add(sosMap.get(variant + "_STD"));
			}

			waitBar = new WaitProgressBar(this);
			waitBar.setWindowSize(400, 300);
			waitBar.start();
			try {
				selectedProductItem = productMap.get(selectedProduct);
				final LatestWeightMasterListSearchOperation searchOperation = new LatestWeightMasterListSearchOperation(table, selectedProductItem, selectedSOS,
						wtMap, tmMap, targetData, defaultTableColumnHeader.size(), waitBar);
				searchOperation.addOperationListener(new InterfaceAIFOperationListener() {
					@Override
					public void startOperation(String arg0) {
						table.removeAll();
						tableModel.getDataVector().clear();
						CustomCellRenderer alignRenderer = new CustomCellRenderer();

						for (int i = table.getColumnCount() - 1; i >= defaultTableColumnHeader.size(); i--) {
							table.removeColumn(table.getColumnModel().getColumn(i));
						}

						Vector<String> headerIdentifier = new Vector<String>();
						headerIdentifier.addAll(defaultTableColumnHeader);

						for (StoredOptionSet sos : selectedSOS) {
							for (String wt : wtMap.keySet()) {
								
								// [20170716][CHC]
								// [요청자 : 송대영 차장]
								// E100 PROJECT의 경우 전기차 이므로 TRANSMISSTION이 없다
								// tmMap.size() <= 0 인 경우는 전기차로 판단하고 Wheel Option만 가지고
								// Option Set을 구성한다.
								if(tmMap.size() <= 0) {
									
									TableColumn curColumn = new TableColumn();
									String headerValue = sdf.format(dateCreateDate) + "\n" + sos.getName().replaceAll("_STD", "") + "\n" + wt ;
									curColumn.setHeaderValue(headerValue);
									curColumn.setPreferredWidth(74);
									curColumn.setHeaderRenderer(headerRenderer);
									curColumn.setCellRenderer(alignRenderer);

									table.addColumn(curColumn);
									headerIdentifier.add(headerValue);
									
								// 기존소스	
								} else {
									for (String tm : tmMap.keySet()) {
										String postfixStr = "";
										if (tm.indexOf('(') > 0)
											postfixStr = tm.substring(tm.indexOf('('));
										tm = tm.replaceAll("/", "");
										tm = tm.replaceAll("-", "");

										// [SR없음][20151130][jclee] DCT tm명 보정
										// if (!tm.equals("DCT")) {
										if (!tm.contains("DCT")) {
											tm = tm.substring(2, 3) + tm.substring(0, 2) + postfixStr;
										}

										TableColumn curColumn = new TableColumn();
										String headerValue = sdf.format(dateCreateDate) + "\n" + sos.getName().replaceAll("_STD", "") + "\n" + wt + "\n" + tm;
										curColumn.setHeaderValue(headerValue);
										curColumn.setPreferredWidth(74);
										curColumn.setHeaderRenderer(headerRenderer);
										curColumn.setCellRenderer(alignRenderer);

										table.addColumn(curColumn);
										headerIdentifier.add(headerValue);
									}
								}

//  20170716 원본소스								
//								for (String tm : tmMap.keySet()) {
//									String postfixStr = "";
//									if (tm.indexOf('(') > 0)
//										postfixStr = tm.substring(tm.indexOf('('));
//									tm = tm.replaceAll("/", "");
//									tm = tm.replaceAll("-", "");
//
//									// [SR없음][20151130][jclee] DCT tm명 보정
//									// if (!tm.equals("DCT")) {
//									if (!tm.contains("DCT")) {
//										tm = tm.substring(2, 3) + tm.substring(0, 2) + postfixStr;
//									}
//
//									TableColumn curColumn = new TableColumn();
//									String headerValue = sdf.format(dateCreateDate) + "\n" + sos.getName().replaceAll("_STD", "") + "\n" + wt + "\n" + tm;
//									curColumn.setHeaderValue(headerValue);
//									curColumn.setPreferredWidth(74);
//									curColumn.setHeaderRenderer(headerRenderer);
//									curColumn.setCellRenderer(alignRenderer);
//
//									table.addColumn(curColumn);
//									headerIdentifier.add(headerValue);
//								}
							}

							TableColumn sumWeightColumn = new TableColumn();
							String headerValue = sos.getName().replaceAll("_STD", "") + "\n" + "Weight/Veh.";
							sumWeightColumn.setHeaderValue(headerValue);
							sumWeightColumn.setPreferredWidth(74);
							sumWeightColumn.setHeaderRenderer(headerRenderer);
							sumWeightColumn.setCellRenderer(alignRenderer);

							table.addColumn(sumWeightColumn);
							headerIdentifier.add(headerValue);
						}

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
					}

					@Override
					public void endOperation() {
						Object opResult = searchOperation.getOperationResult();
						if (opResult != null && !opResult.equals("Success") && opResult instanceof String) {
							waitBar.setStatus(opResult.toString());
							waitBar.setShowButton(true);
						} else if (opResult != null && opResult instanceof ArrayList) {
							waitBar.dispose();
						} else {
							waitBar.dispose();
						}
					}
				});
				session.queueOperation(searchOperation);
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
				((DefaultListModel<String>) comboVariant.getModel()).clear();
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
						waitBar.dispose();
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

				waitBar.setStatus("Load StoredOptionSet list of Product Item Reivison.");
				setOptionSet(prodRev, sosMap);

				waitBar.setStatus("Set StoredOptionSet.");
				if (sosMap.size() > 0) {
					((DefaultListModel<String>) comboVariant.getModel()).clear();

					SortedSet<String> sortKey = new TreeSet<String>(sosMap.keySet());
					for (String key : sortKey) {
						if (key.endsWith("_STD"))
							((DefaultListModel<String>) comboVariant.getModel()).addElement(key.replaceAll("_STD", ""));
					}
				}

				// ProductRevision에 의한 OSpec의 값을 모두 읽어서 E00에 해당하는 Value 및 Description을 뽑아온다.
				waitBar.setStatus("Get OSpec Revision.");
				oSpecRevision = getOSpecRevision(prodRev);

				waitBar.setStatus("Read OSpec Data from OSpec.");
				ospec = BomUtil.getOSpec(oSpecRevision);

				waitBar.setStatus("Set T/M and W/T ComboBox value.");

				tmMap.clear();
				wtMap.clear();

				for (OpValueName opValueName : ospec.getOpNameList()) {
					// TransMission
					if (opValueName.getCategory().equals("E00")) {
						tmMap.put(opValueName.getOptionName(), opValueName);
					}
					// Wheel Type
					if (opValueName.getCategory().equals("E10")) {
						wtMap.put(opValueName.getOptionName(), opValueName);
					}
				}
				/**
				 * 조회 대상정보를 조회함
				 */
				targetData = getLatestWMLMTargetData(prodRev);
			} catch (Exception ex) {
				throw ex;
			}
		}
	}

	protected void setOptionSet(TCComponentItemRevision productRevision, HashMap<String, StoredOptionSet> optionSetMap) throws Exception {
		optionSetMap.clear();

		AIFComponentContext[] relatedContexts = productRevision.getRelated("IMAN_reference");
		for (AIFComponentContext context : relatedContexts) {
			TCComponent com = (TCComponent) context.getComponent();
			if (com.getType().equals("StoredOptionSet")) {
				String sosName = com.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME);
				StoredOptionSet sos = optionSetMap.get(sosName);
				if (sos == null) {
					sos = new StoredOptionSet(sosName);
					optionSetMap.put(sosName, sos);
				}

				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("PUID", com.getUid());
				ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) remote.execute("com.kgm.service.MasterListService",
						"getStoredOptionSet", ds);
				for (int i = 0; list != null && i < list.size(); i++) {
					HashMap<String, String> resultMap = list.get(i);
					sos.add(resultMap.get("POPTION"), resultMap.get("PSTRING_VALUE"));
				}
			}
		}
	}

	protected TCComponentItemRevision getOSpecRevision(TCComponentItemRevision revision) throws Exception {
		String ospecNo = revision.getProperty("s7_OSPEC_NO");
		if (ospecNo == null || ospecNo.equals("")) {
			throw new TCException("Could not found OSPEC_NO.");
		}
		int idx = ospecNo.lastIndexOf("-");
		if (idx < 0) {
			throw new TCException("Invalid OSPEC_NO.");
		}
		String ospecId = ospecNo.substring(0, idx);
		String ospecRevId = ospecNo.substring(idx + 1);

		return CustomUtil.findItemRevision("S7_OspecSetRevision", ospecId, ospecRevId);
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
			if (column == 6) {
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
				if (table.getRowCount() > 0 && table.getValueAt(row, 9).equals("Total Sum Weight")) {
					label.setBackground(Color.LIGHT_GRAY);
					label.setForeground(table.getForeground());
				} else {
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
	 * 최신 Total Weight Master List 대상 정보를 가져옴
	 * 
	 * @param prodRevision
	 * @throws Exception
	 */
	private HashMap<String, String> getLatestWMLMTargetData(TCComponentItemRevision prodRevision) throws Exception {
		String projectCode = prodRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PROJECT_NO", projectCode);

		HashMap<String, String> result = (HashMap<String, String>) remote.execute("com.kgm.service.MasterListService", "getLatestWMLMTargetData", ds);
		String latestCreateTime = result.get("LATEST_M_CREATE_TIME");
		dataCreatedText.setText(latestCreateTime);
		result.put("PROJECT_NO", projectCode);
		return result;

	}
}
