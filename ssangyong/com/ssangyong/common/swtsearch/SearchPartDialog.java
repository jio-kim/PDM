package com.ssangyong.common.swtsearch;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
/**
 * 파트 검색 화면
 * Select 버튼 혹은 테이블 더블클릭하면
 * 선택 정보를 반환함.
 * @author DJKIM
 *
 */
public class SearchPartDialog {
	
	private Shell shell;
	/** 검색 조건 */
	private Text partNoText, partNameText;
	
	private Button searchButton;
	private Table resultTable;
	
	/** 테이블 헤더 */
	private String[] tableColumns = new String[]{"Part No.", "Part Name", "Revision", "Creator", "Creation Date"};
	private int[] tableColSizes = new int[]{100, 220, 60, 100, 100};
	private int lastSortColumn= -1;
	
	/** 검색 조건 */
	private String[] searchConditions = new String[]{"item_id", "object_name"};
	private String[] searchValues = new String[]{"", ""};
	private Button selectButton, cancelButton;
	
	private String[] wantProperties = new String[]{"item_id", "object_name", "item_revision_id", "owning_user", "owning_group", "creation_date"};
	
	private GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
	private HashMap<String, Object> selectedInfo;

	public SearchPartDialog(Shell parent) {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText("Search Part");
		shell.setLayoutData(gridData);
		SYMDisplayUtil.centerToScreen(shell);
	}

	/** <"rowData", String[]> <"tcComponent", TCComponentItemRevision> */
	public HashMap<String, Object> open() {
		createSearchLayout();
		createTableLayout();
		createButtonLayout();
		shell.pack();
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return selectedInfo;
	}

	private void createSearchLayout() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout (6, false));
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		Label lbl_ecoID = new Label(composite, SWT.RIGHT);
		lbl_ecoID.setText("Part NO. : ");
		
		partNoText = new Text(composite, SWT.BORDER);
		partNoText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});
		
		Label idLabel = new Label(composite, SWT.RIGHT);
		idLabel.setText("Part Name : ");
		
		partNameText = new Text(composite, SWT.BORDER);
		partNameText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});

		Label serchLabel = new Label(composite, SWT.RIGHT);
		GridData data = new GridData (SWT.FILL, SWT.CENTER, true, true);
		serchLabel.setLayoutData(data);
		
		searchButton = new Button(composite, SWT.PUSH);
		searchButton.setText("Search");
		searchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchAction();
			}
		});
		
	}
	
	private void createTableLayout() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout ());
		GridData data = new GridData (SWT.FILL, SWT.FILL, true, true);
		data.minimumHeight = 140;
		composite.setLayoutData(data);
		
		resultTable = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		resultTable.setLayoutData(gridData);
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				getValues();
				shell.close();
			}
		});
		
		int i = 0;
		for(String value : tableColumns){
			TableColumn column = new TableColumn(resultTable, SWT.NONE);
			column.setText(value);
			column.setWidth(tableColSizes[i]);
			final int columnIndex = i;
			column.addSelectionListener(new SelectionAdapter() {		
				public void widgetSelected(SelectionEvent e) {
					sort(columnIndex);
				}
			});
			i++;
		}
	}
	
	private void createButtonLayout() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		selectButton = new Button(composite, SWT.PUSH);
		selectButton.setText("Select");
		selectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getValues();
				shell.close();
			}
		});
		
		cancelButton = new Button(composite, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		
	}
	
	private void getValues(){
		 if(resultTable.getSelectionCount() < 1) return;
		 
		 TableItem[] selectItems = resultTable.getSelection();
		 String[] rowData = new String[resultTable.getColumnCount()];
		 TCComponentItemRevision tcComponent = null;
		 for(int i=0 ; i < resultTable.getColumnCount() ; i++){
			 rowData[i] = selectItems[0].getText(i);
			 tcComponent = (TCComponentItemRevision) selectItems[0].getData("TCComponentItemRevision");
		 }
		 selectedInfo = new HashMap<String, Object>();
		 selectedInfo.put("rowData", rowData);
		 selectedInfo.put("tcComponent", tcComponent);
	}

	private void searchAction() {
		String itemID = partNoText.getText();
		String itemName = partNameText.getText();
		if(searchValues[0].equals(itemID) && searchValues[1].equals(itemName) && resultTable.getItemCount() > 0 )  return;
		
		if(itemID.equals("") && itemName.equals("")) return;
			
		if(!itemID.equals("") && !itemName.equals("")) {
			searchValues[0] = itemID;
			searchValues[1] = itemName;
		}else if(!itemID.equals("") && itemName.equals("")){
			searchValues[0] = itemID;
			searchValues[1] = itemName+"*";
		}else if(itemID.equals("") && !itemName.equals("")){
			searchValues[0] = itemID+"*";
			searchValues[1] = itemName;
		}

		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				resultTable.removeAll();
				search();
			}
		});
	}
	
	private void search() {
		try {
			TCComponent[] results= CustomUtil.queryComponent("__SYMC_S7_VehpartRevision", searchConditions, searchValues);
			if(results == null || results.length == 0){
				MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				box.setText("Information");
				box.setMessage("No search results.");
				box.open();
				return;
			}
			for (int i = 0; i < results.length; i++) {
				TCComponentItemRevision itemRevision = (TCComponentItemRevision) results[i];
				String[] properties = itemRevision.getProperties(wantProperties);
				TableItem rowItem = new TableItem(resultTable, SWT.NONE);
				rowItem.setText(properties);
				rowItem.setData("TCComponentItemRevision", itemRevision);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void sort(int column) {
		if(resultTable.getItemCount() <= 1) return;

		TableItem[] items = resultTable.getItems();
		String[][] data = new String[items.length][resultTable.getColumnCount()];
		for(int i = 0; i < items.length; i++) {
			for(int j = 0; j < resultTable.getColumnCount(); j++) {
				data[i][j] = items[i].getText(j);
			}
		}
		
		Arrays.sort(data, new RowComparator(column));
		
		if (lastSortColumn != column) {
			resultTable.setSortColumn(resultTable.getColumn(column));
			resultTable.setSortDirection(SWT.DOWN);
			for (int i = 0; i < data.length; i++) {
				items[i].setText(data[i]);
			}
			lastSortColumn = column;
		} else {
			// reverse order if the current column is selected again
			resultTable.setSortDirection(SWT.UP);
			int j = data.length -1;
			for (int i = 0; i < data.length; i++) {
				items[i].setText(data[j--]);
			}
			lastSortColumn = -1;
		}
		
	}
	
	/**
	 * To compare entries (rows) by the given column
	 */
	@SuppressWarnings("rawtypes")
	private class RowComparator implements Comparator {
		private int column;
		
		/**
		 * Constructs a RowComparator given the column index
		 * @param col The index (starting at zero) of the column
		 */
		public RowComparator(int col) {
			column = col;
		}
		
		/**
		 * Compares two rows (type String[]) using the specified
		 * column entry.
		 * @param obj1 First row to compare
		 * @param obj2 Second row to compare
		 * @return negative if obj1 less than obj2, positive if
		 * 			obj1 greater than obj2, and zero if equal.
		 */
		public int compare(Object obj1, Object obj2) {
			String[] row1 = (String[])obj1;
			String[] row2 = (String[])obj2;
			
			return row1[column].compareTo(row2[column]);
		}
	}
}
