package com.ssangyong.commands.ospec;

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

import com.ssangyong.commands.ospec.op.OpGroup;
import com.teamcenter.rac.aif.AbstractAIFDialog;

/**
 * 옵션 그룹 선택
 * 
 * @author baek
 * 
 */
public class OspecSelectOptGroupDlog extends AbstractAIFDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private ArrayList<OpGroup> groupList = null;
	private JTextField textFieldOptGroup;
	private JTextField textFieldDesc;
	private JButton btnSearch;
	private DefaultTableModel tableModel;
	private JTable resultTable;
	private OpGroup selectedOpGroup = null;
	private boolean isOkAction = false;

	public enum Status {
		SELECTED, DESELECTED, INDETERMINATE
	}

	/**
	 * Create the dialog.
	 */
	public OspecSelectOptGroupDlog(JDialog parentDlg, ArrayList<OpGroup> groupList) {
		super(parentDlg, true);
		this.groupList = groupList;
		initUI();
		iniData();

	}

	private void initUI() {
		setTitle("Select Option Group");
		setBounds(100, 100, 640, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent paramActionEvent) {
					isOkAction = true;
					dispose();
				}
			});
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);

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
		{
			JPanel topPanel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) topPanel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			getContentPane().add(topPanel, BorderLayout.NORTH);
			{
				JLabel lblGroupName = new JLabel("Option Group");
				topPanel.add(lblGroupName);
			}
			{
				textFieldOptGroup = new JTextField();
				topPanel.add(textFieldOptGroup);
				textFieldOptGroup.setColumns(12);
			}
			{
				JLabel lblDesc = new JLabel("Description");
				topPanel.add(lblDesc);
			}
			{
				textFieldDesc = new JTextField();
				topPanel.add(textFieldDesc);
				textFieldDesc.setColumns(15);
			}
			btnSearch = new JButton("");
			btnSearch.setIcon(new ImageIcon(OSpecBomCompareDlg.class.getResource("/icons/search_16.png")));
			btnSearch.setPreferredSize(new Dimension(25, 25));
			topPanel.add(btnSearch);
			btnSearch.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent paramActionEvent) {
					String optGroup = "".equals(textFieldOptGroup.getText()) ? null : textFieldOptGroup.getText();
					String desc = "".equals(textFieldDesc.getText()) ? null : textFieldDesc.getText();
					doSearch(optGroup, desc);
				}
			});
			getRootPane().setDefaultButton(btnSearch);

			Vector<Object> headerVec = new Vector<Object>();
			headerVec.add(Status.INDETERMINATE);
			headerVec.add("Option Group");
			headerVec.add("Description");
			headerVec.add("Condition");

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

			int width[] = { 32, 150, 200,  300 };
			for (int i = 0; i < tableColModel.getColumnCount(); i++) {
				tableColModel.getColumn(i).setPreferredWidth(width[i]);
			}

			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
			resultTable.setRowSorter(sorter);

			JScrollPane resultScrollPane = new JScrollPane(resultTable);
			resultScrollPane.setPreferredSize(new Dimension(400, 400));
			getContentPane().add(resultScrollPane, BorderLayout.CENTER);

			resultTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					// 마우스 더블클릭일 경우
					if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && e.isControlDown() == false) {
						if (resultTable.getSelectedRow() > -1) {
							DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
							int rowIdx = resultTable.convertRowIndexToModel(resultTable.getSelectedRow());
							selectedOpGroup = (OpGroup) model.getValueAt(rowIdx, 1);
							isOkAction = true;
						}
						OspecSelectOptGroupDlog.this.dispose();
					}
				}
			});

		}
	}

	/**
	 * 초기 데이터 로드
	 */
	private void iniData() {
		doSearch(null, null);
	}

	/**
	 * 검색 실행
	 * 
	 * @param findOptGroup
	 * @param findDesc
	 */
	protected void doSearch(String findOptGroup, String findDesc) {
		removeAllRow(resultTable);
		btnSearch.setEnabled(false);

		if (groupList == null) {
			btnSearch.setEnabled(true);
			return;
		}

		for (OpGroup opGroupObj : groupList) {
			String optGroupName = opGroupObj.getOpGroupName();
			String desc = opGroupObj.getDesciption();
			String condition = opGroupObj.getCondition();
			Vector<Object> rowData = new Vector<Object>();
			rowData.add(false);
			rowData.add(opGroupObj);
			rowData.add(desc);
			rowData.add(condition);
			if (findOptGroup != null || findDesc != null) {
				if (findOptGroup != null && findDesc != null) {
					if (optGroupName.toUpperCase().indexOf(findOptGroup.toUpperCase()) > -1 && desc != null
							&& desc.toUpperCase().indexOf(findDesc.toUpperCase()) > -1)
						tableModel.addRow(rowData);
				} else if (findOptGroup != null) {
					if (optGroupName.toUpperCase().indexOf(findOptGroup.toUpperCase()) > -1)
						tableModel.addRow(rowData);
				} else if (findDesc != null) {
					if (desc != null && desc.toUpperCase().indexOf(findDesc.toUpperCase()) > -1)
						tableModel.addRow(rowData);
				}
			} else
				// 검색 조건을 입력하지 않았을경우
				tableModel.addRow(rowData);

		}
		btnSearch.setEnabled(true);
	}

	/**
	 * 선택된 결과를 가져옴
	 * 
	 * @return
	 */
	public ArrayList<OpGroup> getSelectOpGroupList() {
		ArrayList<OpGroup> selectedOpGroupList = new ArrayList<OpGroup>();
		if (!isOkAction)
			return selectedOpGroupList;
		// 마우스 더블 클릭시 하나가 선택된 경우가 아니면 체크된 리스트 정보를 가져옴
		if (selectedOpGroup == null) {
			@SuppressWarnings("unchecked")
//			Vector<Vector<Object>> dataVec = tableModel.getDataVector();
			Vector<Vector> rawData = tableModel.getDataVector();
			Vector<Vector<Object>> dataVec = new Vector<>(rawData);
			
			for (Vector<Object> data : dataVec) {

				Boolean isChecked = (Boolean) data.get(0);
				if (!isChecked)
					continue;
				selectedOpGroupList.add((OpGroup) data.get(1));
			}
		} else
			selectedOpGroupList.add(selectedOpGroup);

		return selectedOpGroupList;
	}

	/**
	 * Row 삭제
	 * 
	 * @param table
	 */
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
