package com.kgm.commands.ospec;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpFunction;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Compare with BOM에서 Function 을 선택하는 화면
 * 
 * @author baek
 * 
 */
public class OspecSelectFunctionDlg extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldFunctionNo;
	private JTable resultTable;
	private DefaultTableModel tableModel = null;
	private OSpecMainDlg ospecMainDlog = null;
	private TCSession tcSession = null;
	private JButton btnSearch;
	private OpFunction selectedFunction = null; // 마우스 더블 클릭시 선택된 Function 정보
	private LinkedList<Vector<Object>> allFunctionList = null; // 전체 Function 리스트
	private boolean isOkAction = false;

	public enum Status {
		SELECTED, DESELECTED, INDETERMINATE
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			OspecSelectFunctionDlg dialog = new OspecSelectFunctionDlg();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public OspecSelectFunctionDlg() {
		initUI();
	}

	public OspecSelectFunctionDlg(JDialog parentDlg, OSpecMainDlg ospecMainDlog) {
		super(parentDlg, true);
		tcSession = CustomUtil.getTCSession();
		this.ospecMainDlog = ospecMainDlog;
		initUI();
		iniData();
	}

	private void initUI() {
		setTitle("Select Function");
		setBounds(100, 100, 420, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JPanel topPanel = new JPanel();
		getContentPane().add(topPanel, BorderLayout.NORTH);
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 10));

		JLabel lblFunctionNo = new JLabel("Function No");
		topPanel.add(lblFunctionNo);

		textFieldFunctionNo = new JTextField();
		topPanel.add(textFieldFunctionNo);
		textFieldFunctionNo.setColumns(10);

		btnSearch = new JButton("");
		btnSearch.setIcon(new ImageIcon(OSpecBomCompareDlg.class.getResource("/icons/search_16.png")));
		btnSearch.setPreferredSize(new Dimension(25, 25));
		topPanel.add(btnSearch);
		btnSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				String functionNo = "".equals(textFieldFunctionNo.getText()) ? null : textFieldFunctionNo.getText();
				doSearch(functionNo);
			}
		});
		getRootPane().setDefaultButton(btnSearch);

		Vector<Object> headerVec = new Vector<Object>();
		headerVec.add(Status.INDETERMINATE);
		headerVec.add("FUNC No");
		headerVec.add("NAME");

		tableModel = new DefaultTableModel(null, headerVec) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int column) {
				if (this.getRowCount() <= 0)
					return Object.class;
				return getValueAt(0, column).getClass();
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 0 ? true : false;
			}
		};

		resultTable = new JTable(tableModel) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private static final int MODEL_COLUMN_INDEX = 0;
			private transient HeaderCheckBoxHandler handler;

			@Override
			public void updateUI() {
				getTableHeader().removeMouseListener(handler);
				TableModel m = getModel();
				if (m != null) {
					m.removeTableModelListener(handler);
				}
				super.updateUI();

				m = getModel();
				for (int i = 0; i < m.getColumnCount(); i++) {
					TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
					if (r instanceof Component) {
						SwingUtilities.updateComponentTreeUI((Component) r);
					}
				}
				TableColumn column = getColumnModel().getColumn(MODEL_COLUMN_INDEX);
				column.setHeaderRenderer(new HeaderRenderer());
				column.setHeaderValue(Status.INDETERMINATE);

				handler = new HeaderCheckBoxHandler(this, MODEL_COLUMN_INDEX);
				m.addTableModelListener(handler);
				getTableHeader().addMouseListener(handler);
			}

			@Override
			public Component prepareEditor(TableCellEditor editor, int row, int column) {
				Component c = super.prepareEditor(editor, row, column);
				if (c instanceof JCheckBox) {
					JCheckBox b = (JCheckBox) c;
					b.setBackground(getSelectionBackground());
					b.setBorderPainted(true);
				}
				return c;
			}
		};
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		TableColumnModel tableColModel = resultTable.getColumnModel();

		int width[] = { 32, 100, 250 };
		for (int i = 0; i < tableColModel.getColumnCount(); i++) {
			tableColModel.getColumn(i).setPreferredWidth(width[i]);
		}

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		resultTable.setRowSorter(sorter);

		JScrollPane resultScrollPane = new JScrollPane(resultTable);
		resultScrollPane.setPreferredSize(new Dimension(400, 400));
		add(resultScrollPane, BorderLayout.CENTER);

		resultTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// 마우스 더블클릭일 경우
				if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && e.isControlDown() == false) {
					if (resultTable.getSelectedRow() > -1) {
						DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
						int rowIdx = resultTable.convertRowIndexToModel(resultTable.getSelectedRow());
						selectedFunction = (OpFunction) model.getValueAt(rowIdx, 1);
						isOkAction = true;
					}
					OspecSelectFunctionDlg.this.dispose();
				}
			}
		});

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				isOkAction = true;
				dispose();
			}
		});
		buttonPane.add(okButton);
		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

	}

	/**
	 * 초기 데이터 로드
	 */
	private void iniData() {
		doSearch(null);
	}

	/**
	 * 검색
	 */
	protected void doSearch(final String functionNo) {
		try {
			removeAllRow(resultTable);
			AbstractAIFOperation op = new AbstractAIFOperation() {
				@Override
				public void executeOperation() throws Exception {
					btnSearch.setEnabled(false);
					// Function No가 없을 경우는 전체를 조회함
					if (functionNo == null) {
						allFunctionList = new LinkedList<Vector<Object>>(); // 전체 검색된 리스트
						ArrayList<HashMap<String, String>> functionList = getFunctionList(functionNo);
						for (HashMap<String, String> functionMap : functionList) {
							String functionName = (String) functionMap.get("ITEM_NAME");
							OpFunction function = new OpFunction((String) functionMap.get("ITEM_ID"), (String) functionMap.get("ITEM_REV_ID"), functionName,
									(String) functionMap.get("PRODUCT_ID"), (String) functionMap.get("PROJECT_CODE"));

							Vector<Object> rowData = new Vector<Object>();
							rowData.add(false);
							rowData.add(function);
							rowData.add(functionName);
							tableModel.addRow(rowData);
							allFunctionList.add(rowData);
						}
					} else {
						// 이미 조회된 전체가 검색된 경우에는 검색된 결과에서 검색조건에 맞는 리스트를 가져옴
						if (allFunctionList == null) {
							btnSearch.setEnabled(true);
							return;
						}
						for (Vector<Object> rowData : allFunctionList) {
							if (functionNo != null) {
								OpFunction function = (OpFunction) rowData.get(1);
								String funcId = function.getItemId();
								if (funcId.toUpperCase().indexOf(functionNo.toUpperCase()) > -1)
									tableModel.addRow(rowData);
							} else
								tableModel.addRow(rowData);
						}

					}
					btnSearch.setEnabled(true);
				}

			};
			tcSession.queueOperation(op);

		} catch (Exception e) {
			MessageBox.post(OspecSelectFunctionDlg.this, e.toString(), "Error", MessageBox.ERROR);
		}

	}

	/**
	 * Function 정보를 가져옴
	 * 
	 * @param functionNo
	 * @return
	 * @throws Exception
	 */
	private ArrayList<HashMap<String, String>> getFunctionList(String functionNo) throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		OSpec ospec = ospecMainDlog.getOspec();
		try {
			ds.put("PROJECT", ospec.getProject());
			ds.put("FUNCTION_NO", functionNo);
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> functionList = (ArrayList<HashMap<String, String>>) remote.execute("com.kgm.service.OSpecService",
					"getFunctionList", ds);
			return functionList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 선택된 Function 리스트
	 */
	public ArrayList<OpFunction> getSelectFunctionList() {
		ArrayList<OpFunction> selectedFunctionList = new ArrayList<OpFunction>();
		if (!isOkAction)
			return selectedFunctionList;
		// 마우스 더블 클릭시 하나가 선택된 경우가 아니면 체크된 리스트 정보를 가져옴
		if (selectedFunction == null) {
			@SuppressWarnings("unchecked")
//			Vector<Vector<Object>> dataVec = tableModel.getDataVector();
			Vector<Vector> rawData = tableModel.getDataVector();
//			Vector<Vector<Object>> dataVec = new Vector<>(rawData);
			Vector<Vector<Object>> dataVec = new Vector<>();
			for(Vector innerVector : rawData) {
				Vector<Object> objectVector = new Vector<>();
				for(Object element : innerVector) {
					objectVector.add(innerVector);
				}
				dataVec.add(objectVector);
			}
			
			
			for (Vector<Object> data : dataVec) {

				Boolean isChecked = (Boolean) data.get(0);
				if (!isChecked)
					continue;
				selectedFunctionList.add((OpFunction) data.get(1));
			}
		} else
			selectedFunctionList.add(selectedFunction);

		return selectedFunctionList;
	}

	public void removeAllRow(JTable table) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();

		for (int i = model.getRowCount() - 1; i >= 0; i--) {
			model.removeRow(i);
		}
	}

	/**
	 * Header Renderer
	 * 
	 * @author baek
	 * 
	 */
	public class HeaderRenderer implements TableCellRenderer {
		private final JCheckBox checkBox = new JCheckBox("");
		private final JLabel checkBoxLabel = new JLabel("");

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof Status) {
				switch ((Status) value) {
				case SELECTED:
					checkBox.setSelected(true);
					checkBox.setEnabled(true);
					break;
				case DESELECTED:
					checkBox.setSelected(false);
					checkBox.setEnabled(true);
					break;
				case INDETERMINATE:
					checkBox.setSelected(true);
					checkBox.setEnabled(false);
					break;
				default:
					throw new AssertionError("Unknown Status");
				}
			} else {
				checkBox.setSelected(true);
				checkBox.setEnabled(false);
			}
			checkBox.setOpaque(false);
			checkBox.setFont(table.getFont());
			TableCellRenderer cellRender = table.getTableHeader().getDefaultRenderer();
			JLabel cellLabel = (JLabel) cellRender.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			checkBoxLabel.setIcon(new ComponentIcon(checkBox));
			cellLabel.setIcon(new ComponentIcon(checkBoxLabel));
			cellLabel.setText(null);

			return cellLabel;
		}
	}

	/**
	 * 체크박스 Image
	 * 
	 * @author baek
	 * 
	 */
	public class ComponentIcon implements Icon {
		private final JComponent cmp;

		protected ComponentIcon(JComponent cmp) {
			this.cmp = cmp;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight());
		}

		@Override
		public int getIconWidth() {
			return cmp.getPreferredSize().width;
		}

		@Override
		public int getIconHeight() {
			return cmp.getPreferredSize().height;
		}
	}

	/**
	 * Header Check Box 처리
	 * 
	 * @author baek
	 * 
	 */
	public class HeaderCheckBoxHandler extends MouseAdapter implements TableModelListener {
		private final JTable table;
		private final int targetColumnIndex;

		protected HeaderCheckBoxHandler(JTable table, int index) {
			super();
			this.table = table;
			this.targetColumnIndex = index;
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
				int vci = table.convertColumnIndexToView(targetColumnIndex);
				TableColumn column = table.getColumnModel().getColumn(vci);
				Object status = column.getHeaderValue();
				TableModel model = table.getModel();
				if (fireUpdateEvent(model, column, status)) {
					JTableHeader header = table.getTableHeader();
					header.repaint(header.getHeaderRect(vci));
				}
			}
		}

		private boolean fireUpdateEvent(TableModel model, TableColumn column, Object status) {
			if (Status.INDETERMINATE.equals(status)) {
				boolean selected = true;
				boolean deselected = true;
				for (int i = 0; i < model.getRowCount(); i++) {
					Boolean value = (Boolean) model.getValueAt(i, targetColumnIndex);
					selected &= value;
					deselected &= !value;
					if (selected == deselected) {
						return false;
					}
				}
				if (deselected) {
					column.setHeaderValue(Status.DESELECTED);
				} else if (selected) {
					column.setHeaderValue(Status.SELECTED);
				} else {
					return false;
				}
			} else {
				column.setHeaderValue(Status.INDETERMINATE);
			}
			return true;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			JTableHeader header = (JTableHeader) e.getComponent();
			JTable table = header.getTable();
			TableColumnModel columnModel = table.getColumnModel();
			TableModel model = table.getModel();
			int columnIndex = columnModel.getColumnIndexAtX(e.getX());
			int modelIndex = table.convertColumnIndexToModel(columnIndex);
			if (modelIndex == targetColumnIndex && model.getRowCount() > 0) {
				TableColumn column = columnModel.getColumn(columnIndex);
				Object v = column.getHeaderValue();
				boolean value = Status.DESELECTED.equals(v);
				for (int i = 0; i < model.getRowCount(); i++) {
					model.setValueAt(value, i, modelIndex);
				}
				column.setHeaderValue(value ? Status.SELECTED : Status.DESELECTED);
			}
		}
	}

}
