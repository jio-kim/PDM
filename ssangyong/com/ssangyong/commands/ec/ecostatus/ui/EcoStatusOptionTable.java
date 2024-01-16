package com.ssangyong.commands.ec.ecostatus.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.ssangyong.commands.ospec.groupheader.ColumnGroup;
import com.ssangyong.commands.ospec.groupheader.GroupableTableHeader;
import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.commands.ospec.op.Option;
import com.ssangyong.common.ui.mergetable.AttributiveCellTableModel;
import com.ssangyong.common.ui.mergetable.CellSpan;
import com.ssangyong.common.ui.mergetable.MultiSpanCellTable;

public class EcoStatusOptionTable {

	private MultiSpanCellTable fixedOspecViewTable = null;
	private MultiSpanCellTable ospecViewTable = null;
	private OSpec ospec = null;
	private JScrollPane scroll = null;
	@SuppressWarnings("rawtypes")
	private Vector<Vector> data = null;
	private JPanel ospecTablePanel = null;
	private boolean isValueEditable = false;
	private HashMap<String, ArrayList<String>> simpleDataMap = null;
	private boolean isOnlyChangedOption = false; // 변경된 Option 만 비교여부
	private ArrayList<String> validCategoryList = null;// 유효한 Category 리스트

	@SuppressWarnings("rawtypes")
	public EcoStatusOptionTable(OSpec ospec, Vector<Vector> customData, boolean isValueEditable) throws Exception {
		this.ospec = ospec;
		this.isValueEditable = isValueEditable;
		this.ospecTablePanel = getOspecTable(customData);
	}

	@SuppressWarnings("rawtypes")
	public EcoStatusOptionTable(OSpec ospec, Vector<Vector> customData) throws Exception {
		this(ospec, customData, false);
	}

	@SuppressWarnings("rawtypes")
	public EcoStatusOptionTable(OSpec ospec, Vector<Vector> customData, ArrayList<String> validCategoryList) throws Exception {
		this.ospec = ospec;
		this.isValueEditable = false;
		this.isOnlyChangedOption = true;
		this.validCategoryList = validCategoryList;
		this.ospecTablePanel = getOspecTable(customData);
	}

	public JPanel getOspecTable() {
		return ospecTablePanel;
	}

	public HashMap<String, ArrayList<String>> getSimpleDataMap() {
		return simpleDataMap;
	}

	public void setSimpleDataMap(HashMap<String, ArrayList<String>> simpleDataMap) {
		this.simpleDataMap = simpleDataMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	private JPanel getOspecTable(Vector<Vector> customData) throws Exception {
		ArrayList<OpTrim> trimList = ospec.getTrimList(); // getTrim(osiNo);
		Vector fiexedHeader = getHeader();
		final Vector dataHeader = getHeader();

		if (customData == null) {
			data = getData(ospec, null);
		} else {
			data = customData;
		}

		final Vector<Vector> fixedData = (Vector<Vector>) data.clone();
		final Vector<Vector> optionData = (Vector<Vector>) data.clone();
		final int columnCount = dataHeader.size();
		final int fixedColumnCount = 4;

		AttributiveCellTableModel fixedModel = new AttributiveCellTableModel(fixedData, fiexedHeader) {

			@Override
			public boolean isCellEditable(int i, int j) {
				// TODO Auto-generated method stub
				return false;
			}

			public int getColumnCount() {
				return fixedColumnCount;
			}
		};

		AttributiveCellTableModel model = new AttributiveCellTableModel(optionData, dataHeader) {

			@Override
			public boolean isCellEditable(int i, int j) {
				// TODO Auto-generated method stub
				return isValueEditable;
			}

			public int getColumnCount() {
				return columnCount - fixedColumnCount;
			}

			public int getRowCount() {
				return dataVector.size();
			}

			public String getColumnName(int i) {
				Object obj = null;
				if (i < columnIdentifiers.size() && i >= 0)
					obj = columnIdentifiers.elementAt(i + fixedColumnCount);
				return obj != null ? obj.toString() : super.getColumnName(i);
			}

			public Object getValueAt(int row, int col) {
				return data.get(row).get(col + fixedColumnCount);
			}

			public void setValueAt(Object obj, int row, int col) {
				Vector rowVec = data.get(row);
				rowVec.set(col + fixedColumnCount, obj);
			}
		};

		fixedOspecViewTable = new MultiSpanCellTable(fixedModel) {
			protected JTableHeader createDefaultTableHeader() {
				return new GroupableTableHeader(columnModel);
			}

			public void valueChanged(ListSelectionEvent e) {
				super.valueChanged(e);
				checkSelection(true);
			}

			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					jc.setToolTipText(getValueAt(row, column).toString());
				}
				return c;
			}

			@Override
			public void columnMarginChanged(ChangeEvent changeevent) {
				// TODO Auto-generated method stub
				if (scroll != null) {
					scroll.getRowHeader().setPreferredSize(fixedOspecViewTable.getPreferredSize());
				}
				super.columnMarginChanged(changeevent);
			}

		};
		TableColumnModel cm = fixedOspecViewTable.getColumnModel();
		int[] width = new int[] { 10, 120, 200, 40 };
		for (int i = 0; i < cm.getColumnCount(); i++) {
			cm.getColumn(i).setPreferredWidth(width[i]);
			cm.getColumn(i).setResizable(true);
		}
		setGroupColumn(true, fixedOspecViewTable, trimList, fixedColumnCount);
		cellMerge(fixedOspecViewTable, fixedColumnCount);

		ospecViewTable = new MultiSpanCellTable(model) {
			protected JTableHeader createDefaultTableHeader() {
				return new GroupableTableHeader(columnModel);
			}

			public void valueChanged(ListSelectionEvent e) {
				super.valueChanged(e);
				checkSelection(false);
			}

			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					String toolTip = "Package : " + getValueAt(row, 0).toString() + "." + getValueAt(row, column).toString();
					jc.setToolTipText("<html>" + toolTip.replaceAll("\\.", "<br>") + "</html>");
					String columnName = ospecViewTable.getColumnName(column);
					if (columnName != null && simpleDataMap != null) {
						ArrayList<String> list = simpleDataMap.get(columnName);
						String opValue = (String) fixedOspecViewTable.getValueAt(row, 3);
						String value = (String) getValueAt(row, column);
						String compValue = opValue + "_" + value;
						// 해당 Column 은 붉은 색을 보여주지 않는다.
						// boolean isRedPass = "DrvType".equalsIgnoreCase(columnName) || "All".equalsIgnoreCase(columnName)
						// || "Eff-IN".equalsIgnoreCase(columnName) || "S: Standard O*: Option M*: Mandatory".equalsIgnoreCase(columnName);
						boolean isRedPass = false;

						if (getSelectedRow() == row) {
							jc.setBackground(new Color(51, 153, 255));
							jc.setForeground(Color.WHITE);
							return c;
						} else {
							jc.setForeground(Color.BLACK);
							if (list == null) {
								if (isRedPass)
									jc.setBackground(Color.WHITE);
								else
									jc.setBackground(Color.RED);
							} else {
								if (!list.contains(compValue)) {
									if (isRedPass)
										jc.setBackground(Color.WHITE);
									else
										jc.setBackground(Color.RED);
								} else {
									jc.setBackground(Color.WHITE);
								}
							}
						}

					}
				}
				return c;
			}
		};
		setGroupColumn(false, ospecViewTable, trimList, fixedColumnCount);

		// Width 조절
		TableColumnModel tcm = ospecViewTable.getColumnModel();
		for (int i = 0; i < tcm.getColumnCount(); i++) {
			switch (i) {
			case 0:
				tcm.getColumn(i).setPreferredWidth(40);
				break;
			case 1:
				tcm.getColumn(i).setPreferredWidth(60);
				break;
			case 2:
				tcm.getColumn(i).setPreferredWidth(20);
				break;
			default:
				if (i == (tcm.getColumnCount() - 2)) {
					tcm.getColumn(i).setPreferredWidth(70);
				} else if (i == (tcm.getColumnCount() - 1)) {
					tcm.getColumn(i).setPreferredWidth(400);
				} else {
					tcm.getColumn(i).setPreferredWidth(55);
				}
			}
		}

		fixedOspecViewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		ospecViewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		fixedOspecViewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ospecViewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scroll = new JScrollPane(ospecViewTable);
		JViewport viewport = new JViewport();
		viewport.setView(fixedOspecViewTable);
		viewport.setPreferredSize(fixedOspecViewTable.getPreferredSize());
		scroll.setRowHeaderView(viewport);
		scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixedOspecViewTable.getTableHeader());

		JPanel ospecTablePanel = new JPanel();
		ospecTablePanel.setLayout(new BorderLayout(0, 0));
		ospecTablePanel.add(scroll, BorderLayout.CENTER);
		return ospecTablePanel;
	}

	public MultiSpanCellTable getFixedOspecViewTable() {
		return fixedOspecViewTable;
	}

	public MultiSpanCellTable getOspecViewTable() {
		return ospecViewTable;
	}

	public OSpec getOspec() {
		return ospec;
	}

	public JScrollPane getScroll() {
		return scroll;
	}

	private void checkSelection(boolean isFixedTable) {
		int fixedSelectedIndex = fixedOspecViewTable.getSelectedRow();
		int selectedIndex = ospecViewTable.getSelectedRow();
		if (fixedSelectedIndex != selectedIndex) {
			if (isFixedTable) {
				ospecViewTable.setRowSelectionInterval(fixedSelectedIndex, fixedSelectedIndex);
			} else {
				fixedOspecViewTable.setRowSelectionInterval(selectedIndex, selectedIndex);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public Vector getData(OSpec ospec, ArrayList<String> opValueList) {

		int rowNum = -1, colNum = -1;
		int trimSize = ospec.getOptions().keySet().size();
		ArrayList<Option> list = ospec.getOptionList();
		Vector<Vector<String>> data = new Vector() {

			@Override
			public synchronized Object clone() {
				Vector cloneData = new Vector();
				for (int i = 0; i < this.elementCount; i++) {
					Vector row = new Vector();
					Vector source = (Vector) this.elementData[i];
					row.addAll(source);
					cloneData.add(row);
				}
				return cloneData;
			}

		};
		Vector row = null;
		Option preOption = null;

		for (Option option : list) {

			if (opValueList != null) {
				if (!opValueList.contains(option.getOpValue())) {
					continue;
				}
			}

			/**
			 * 해당 유효한 Category 만 나오도록 함
			 */
			if (isOnlyChangedOption) {
				String category = OpUtil.getCategory(option.getOpValue());
				if (validCategoryList.size() == 0 || validCategoryList != null && !validCategoryList.contains(category))
					continue;
			}

			if (rowNum != option.getRowOrder()) {

				if (row != null) {
					for (int j = row.size(); j < trimSize + 7; j++) {
						row.add("-");
						colNum++;
					}
					row.add(preOption.getEffIn());
					row.add(preOption.getRemark());
					colNum = -1;
					if (!data.contains(row)) {
						data.add(row);
						row = null;
					}
				}

				rowNum = option.getRowOrder();
				row = new Vector() {

					@Override
					public synchronized boolean equals(Object obj) {
						// TODO Auto-generated method stub
						if (obj instanceof Vector) {
							Vector vec = (Vector) obj;
							if (this.size() == vec.size()) {
								return this.toString().equals(vec.toString());
							} else {
								return super.equals(obj);
							}
						}

						return super.equals(obj);
					}

				};
				row.add(option.getOp().substring(0, 1));
				row.add(option.getOpName());
				row.add(option.getOpValueName());
				row.add(option.getOpValue());
				row.add(option.getPackageName());
				row.add(option.getDriveType());
				row.add(option.getAll());
				colNum = 7;
			}

			if ((colNum - 7) == option.getColOrder()) {
				row.add(option.getValue());
			} else {
				for (int j = colNum - 7; j < option.getColOrder(); j++) {
					row.add("-");
					colNum++;
				}
				row.add(option.getValue());
			}
			colNum++;
			// -------------------

			// -------------------
			preOption = option;
			if (colNum == (trimSize + 7)) {

				row.add(option.getEffIn());
				row.add(option.getRemark());
				colNum = -1;
				if (!data.contains(row)) {
					data.add(row);
					row = null;
				}
			}

		}

		if (row != null) {
			for (int j = row.size(); j < trimSize + 7; j++) {
				row.add("-");
				colNum++;
			}
			row.add(preOption.getEffIn());
			row.add(preOption.getRemark());
			colNum = -1;
			if (!data.contains(row)) {
				data.add(row);
				row = null;
			}
		}

		return data;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector getHeader() {
		Vector headerVector = new Vector();
		headerVector.add("C");

		// 표기되지 않는 Column
		headerVector.add("Category");
		headerVector.add("Option");

		headerVector.add("Code");
		headerVector.add("P/Opt");

		headerVector.add("DrvType");
		headerVector.add("All");

		ArrayList<OpTrim> trimList = ospec.getTrimList();
		for (OpTrim trim : trimList) {
			headerVector.add(trim.getTrim());
		}

		headerVector.add("Eff-IN");
		headerVector.add("S: Standard O*: Option M*: Mandatory");

		return headerVector;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setGroupColumn(boolean bIsFixedTable, JTable table, ArrayList<OpTrim> trimList, int fixedColumnCount) {
		TableColumnModel cm = table.getColumnModel();
		ArrayList<ColumnGroup> areaGroupList = new ArrayList();

		if (bIsFixedTable) {

			ColumnGroup releasedDate = new ColumnGroup(ospec.getReleasedDate().substring(0, 4) + "-" + ospec.getReleasedDate().substring(4, 6) + "-"
					+ ospec.getReleasedDate().substring(6));
			releasedDate.add(cm.getColumn(0));
			releasedDate.add(cm.getColumn(1));
			releasedDate.add(cm.getColumn(2));
			releasedDate.add(cm.getColumn(3));
			ColumnGroup osiTitle = new ColumnGroup(ospec.getOspecNo());
			osiTitle.add(releasedDate);
			areaGroupList.add(osiTitle);

		} else {
			ColumnGroup preAreaGroup = null;
			ColumnGroup prePassGroup = null;
			ColumnGroup preEngineGroup = null;
			ColumnGroup preGradeGroup = null;

			ColumnGroup gradeColumnGroup = null;
			ColumnGroup engineColumnGroup = null;
			ColumnGroup passColumnGroup = null;
			ColumnGroup areaColumnGroup = null;

			boolean isEqual = false;
			for (OpTrim trim : trimList) {

				isEqual = false;

				if (preAreaGroup == null) {
					areaColumnGroup = new ColumnGroup(trim.getArea());
					preAreaGroup = areaColumnGroup;
					isEqual = false;
				} else {
					if (trim.getArea().equals(preAreaGroup.getHeaderValue().toString())) {
						areaColumnGroup = preAreaGroup;
						isEqual = true;
					} else {
						areaColumnGroup = new ColumnGroup(trim.getArea());
						preAreaGroup = areaColumnGroup;
						isEqual = false;
					}
				}

				if (prePassGroup == null) {
					passColumnGroup = new ColumnGroup(trim.getPassenger());
					prePassGroup = passColumnGroup;
					isEqual = false;
				} else {
					if (isEqual && trim.getPassenger().equals(prePassGroup.getHeaderValue().toString())) {
						passColumnGroup = prePassGroup;
						isEqual = true;
					} else {
						passColumnGroup = new ColumnGroup(trim.getPassenger());
						prePassGroup = passColumnGroup;
						isEqual = false;
					}
				}
				if (!areaColumnGroup.getColumnGroups().contains(passColumnGroup))
					areaColumnGroup.add(passColumnGroup);

				if (preEngineGroup == null) {
					engineColumnGroup = new ColumnGroup(trim.getEngine());
					preEngineGroup = engineColumnGroup;
					isEqual = false;
				} else {
					if (isEqual && trim.getEngine().equals(preEngineGroup.getHeaderValue().toString())) {
						engineColumnGroup = preEngineGroup;
						isEqual = true;
					} else {
						engineColumnGroup = new ColumnGroup(trim.getEngine());
						preEngineGroup = engineColumnGroup;
						isEqual = false;
					}
				}
				if (!passColumnGroup.getColumnGroups().contains(engineColumnGroup))
					passColumnGroup.add(engineColumnGroup);

				// Grade Group 생성.
				if (preGradeGroup == null) {
					gradeColumnGroup = new ColumnGroup(trim.getGrade());
					preGradeGroup = gradeColumnGroup;
					isEqual = false;
				} else {
					if (isEqual && trim.getGrade().equals(preGradeGroup.getHeaderValue().toString())) {
						gradeColumnGroup = preGradeGroup;
						isEqual = true;
					} else {
						gradeColumnGroup = new ColumnGroup(trim.getGrade());
						preGradeGroup = gradeColumnGroup;
						isEqual = false;
					}
				}
				if (!engineColumnGroup.getColumnGroups().contains(gradeColumnGroup))
					engineColumnGroup.add(gradeColumnGroup);

				if (!gradeColumnGroup.getColumnGroups().contains(cm.getColumn(trim.getColOrder() + 7 - fixedColumnCount))) {
					gradeColumnGroup.add(cm.getColumn(trim.getColOrder() + 7 - fixedColumnCount));
				}

				if (!areaGroupList.contains(areaColumnGroup)) {
					areaGroupList.add(areaColumnGroup);
				}
			}
		}
		table.getColumnModel().setColumnMargin(0);
		GroupableTableHeader header = (GroupableTableHeader) table.getTableHeader();
		for (ColumnGroup cg : areaGroupList) {
			header.addColumnGroup(cg);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public static void cellMerge(MultiSpanCellTable table, int toColumn) {

		ArrayList<Integer> rowList = new ArrayList();
		int[] rows = null;

		String preValue = null;
		TableColumnModel cm = table.getColumnModel();
		AttributiveCellTableModel model = (AttributiveCellTableModel) table.getModel();
		CellSpan cellAtt = (CellSpan) model.getCellAttribute();
		for (int column = 0; column < toColumn; column++) {
			preValue = null;
			rowList.clear();
			for (int row = 0; row < table.getRowCount(); row++) {
				String value = (String) table.getValueAt(row, column);
				if (preValue == null) {
					preValue = value;
					rowList.add(new Integer(row));
				} else {
					if (preValue.equals(value)) {
						rowList.add(new Integer(row));
					} else {
						rows = new int[rowList.size()];
						for (int i = 0; i < rowList.size(); i++) {
							Integer in = rowList.get(i);
							rows[i] = in.intValue();
						}
						cellAtt.combine(rows, new int[] { column });
						preValue = value;
						rowList.clear();
						rowList.add(new Integer(row));
					}
				}
			}

			if (rowList.size() > 1) {
				rows = new int[rowList.size()];
				for (int i = 0; i < rowList.size(); i++) {
					Integer in = rowList.get(i);
					rows[i] = in.intValue();
				}
				cellAtt.combine(rows, new int[] { column });
			}
		}

		if (rowList.size() > 1) {
			rows = new int[rowList.size()];
			for (int i = 0; i < rowList.size(); i++) {
				Integer in = rowList.get(i);
				rows[i] = in.intValue();
			}
			cellAtt.combine(rows, new int[] { toColumn - 1 });
		}

		table.clearSelection();
		table.revalidate();
		table.repaint();
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public Vector<Vector> minus(Vector<Vector> data, boolean bFillEmpty) {
		Vector<Vector> result = null;
		if (this.data != null) {
			result = new Vector() {

				@Override
				public synchronized Object clone() {
					Vector cloneData = new Vector();
					for (int i = 0; i < this.elementCount; i++) {
						Vector row = new Vector();
						Vector source = (Vector) this.elementData[i];
						row.addAll(source);
						cloneData.add(row);
					}
					return cloneData;
				}

			};

			for (Vector row : this.data) {
				if (!data.contains(row)) {
					result.add(row);
				} else {
					if (bFillEmpty) {
						Vector tmpRow = new Vector();
						tmpRow.add(row.get(0));
						tmpRow.add(row.get(1));
						tmpRow.add(row.get(2));
						tmpRow.add(row.get(3));
						for (int i = 0; i < 20; i++) {
							tmpRow.add("");
						}
						result.add(tmpRow);
					}
				}
			}

			// for (Vector row : this.data) {
			//
			// Vector rowOptionVec = new Vector();
			// for (int i = 0; i < row.size(); i++) {
			// if (i == 5 || i == 6 || i == row.size() - 2 || i == row.size() - 1)
			// continue;
			// rowOptionVec.add(row.get(i));
			// }
			//
			// Vector newDataVec = new Vector();
			// for (int i = 0; i < data.size(); i++) {
			// Vector rowVec = data.get(i);
			// Vector newRowVec = new Vector();
			// for (int j = 0; j < rowVec.size(); j++) {
			// if (j == 5 || j == 6 || j == rowVec.size() - 2 || j == rowVec.size() - 1)
			// continue;
			// newRowVec.add(rowVec.get(j));
			// }
			// newDataVec.add(newRowVec);
			// }
			//
			// if (!newDataVec.contains(rowOptionVec))
			// result.add(row);
			// }
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HashMap<String, ArrayList<String>> getSimpleDataMap(Vector<Vector> data, Vector header) {
		HashMap<String, ArrayList<String>> map = new HashMap(); // <Trim, opValue_Value>

		int baseIdx = 4;
		for (int j = 0; j < data.size(); j++) {
			Vector row = data.get(j);
			int headerIdx = 7;
			for (int i = baseIdx; i < row.size(); i++) {
				headerIdx = i;
				String opValue = (String) row.get(3);
				String value = (String) row.get(i);
				String compValue = opValue + "_" + value;

				String trim = (String) header.get(headerIdx);
				if (map.containsKey(trim)) {
					ArrayList<String> list = map.get(trim);
					if (!list.contains(compValue)) {
						list.add(compValue);
					}
				} else {
					ArrayList<String> list = new ArrayList();
					list.add(compValue);
					map.put(trim, list);
				}
			}
		}
		return map;
	}

	@SuppressWarnings("rawtypes")
	public Vector<Vector> getData() {
		return data;
	}
}
