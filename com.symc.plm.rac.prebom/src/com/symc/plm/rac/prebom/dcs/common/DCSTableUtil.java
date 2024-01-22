package com.symc.plm.rac.prebom.dcs.common;

import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.util.Registry;

/**
 * [DCS 현업 사용문제 신고내역] [20150722][ymjang] 리스트 정렬기능중 건수 시리얼 숫자 올림차순 문제있음. 1, 11, 12…..2,21,22….
 */
public class DCSTableUtil {

	private Registry registry;

	private Table table;
	private String tableId;
	private HashMap<String, Object> tableColumnDataMap;

	public DCSTableUtil() {
		registry = Registry.getRegistry(this);
	}

	public DCSTableUtil(Table table, String tableId) {
		this();
		this.table = table;
		this.tableId = tableId;
	}

	public void setTableData(List<HashMap<String, Object>> dataList) {
		this.table.removeAll();
		this.table.setData(dataList);
		
		// [DCS 현업 사용문제 신고내역] [20150722][ymjang] 리스트 정렬기능중 건수 시리얼 숫자 올림차순 문제있음. 1, 11, 12…..2,21,22….
		if (dataList != null) {
			int sizeLength = String.valueOf(dataList.size()).length();
			for (int i = 0; i < dataList.size(); i++) {
				HashMap<String, Object> dataMap = dataList.get(i);
				String no = String.format("%0" + sizeLength + "d", i + 1);
				dataMap.put("No", no);
				//dataMap.put("No", String.valueOf(i + 1));
				createTableItem(dataMap, i);
			}
		}
	}

	public void createTableItem(HashMap<String, Object> dataMap, int index) {
		TableItem tableItem = new TableItem(table, SWT.NONE, index);
		tableItem.setData(dataMap);

		String[] ids = (String[]) tableColumnDataMap.get("ids");
		for (int i = 0; i < ids.length; i++) {
			String value = (String) dataMap.get(ids[i]);
			value = value == null ? "" : value;
			tableItem.setText(i + 1, value);
		}

		if (tableId != null && tableId.equals("dcsNoticeTable")) {
			if (dataMap.get("NOTICE_TYPE").equals("1")) {
				tableItem.setForeground(2, SWTResourceManager.getColor(255, 0, 0));
			}
		}
	}

	public void createTableColumn() {
		String[] indexs = registry.getStringArray(tableId + ".column.index");
		String[] ids = registry.getStringArray(tableId + ".column.id");
		String[] names = registry.getStringArray(tableId + ".column.name");
		String[] widths = registry.getStringArray(tableId + ".column.width");
		String[] alignments = registry.getStringArray(tableId + ".column.alignment");

		tableColumnDataMap = new HashMap<String, Object>();
		tableColumnDataMap.put("indexs", indexs);
		tableColumnDataMap.put("ids", ids);
		tableColumnDataMap.put("names", names);
		tableColumnDataMap.put("widths", widths);
		tableColumnDataMap.put("alignments", alignments);

		TableColumn firstTableColumn = new TableColumn(table, SWT.NONE);
		firstTableColumn.setWidth(0);

		for (int i = 0; i < indexs.length; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setData("index", Integer.valueOf(indexs[i]));
			tableColumn.setData("id", ids[i]);
			tableColumn.setText(names[i]);
			tableColumn.setWidth(Integer.valueOf(widths[i]));

			if (alignments[i].equals("LEFT")) {
				tableColumn.setAlignment(SWT.LEFT);
			} else if (alignments[i].equals("CENTER")) {
				tableColumn.setAlignment(SWT.CENTER);
			} else {
				tableColumn.setAlignment(SWT.RIGHT);
			}

			tableColumn.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent event) {
					TableColumn localTableColumn = (TableColumn) event.getSource();
					doSort(localTableColumn);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent event) {

				}

			});
		}
	}

	@SuppressWarnings("unchecked")
	public void doSort(TableColumn tableColumn) {
		int sortDirection = table.getSortDirection();
		if (sortDirection == SWT.UP) {
			sortDirection = SWT.DOWN;
		} else if (sortDirection == SWT.DOWN) {
			sortDirection = SWT.UP;
		} else {
			sortDirection = SWT.UP;
		}

		Collator collator = Collator.getInstance(Locale.getDefault());
		int index = (Integer) tableColumn.getData("index");

		TableItem[] tableItems = table.getItems();
		for (int i = 1; i < tableItems.length; i++) {
			String value1 = tableItems[i].getText(index);
			for (int j = 0; j < i; j++) {
				String value2 = tableItems[j].getText(index);
				if (sortDirection == SWT.UP) {
					if (collator.compare(value1, value2) < 0) {
						HashMap<String, Object> dataMap = (HashMap<String, Object>) tableItems[i].getData();
						tableItems[i].dispose();

						createTableItem(dataMap, j);
						tableItems = table.getItems();

						break;
					}
				} else if (sortDirection == SWT.DOWN) {
					if (collator.compare(value1, value2) > 0) {
						HashMap<String, Object> dataMap = (HashMap<String, Object>) tableItems[i].getData();
						tableItems[i].dispose();

						createTableItem(dataMap, j);
						tableItems = table.getItems();

						break;
					}
				}
			}
		}

		table.setSortDirection(sortDirection);
		table.setSortColumn(tableColumn);
	}

}
