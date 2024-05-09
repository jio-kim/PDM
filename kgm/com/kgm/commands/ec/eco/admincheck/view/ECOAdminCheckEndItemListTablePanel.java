package com.kgm.commands.ec.eco.admincheck.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.eco.admincheck.common.ECOAdminCheckConstants;
import com.kgm.common.SYMCRadioButton;
import com.kgm.common.lov.SYMCLOVLoader;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ECOAdminCheckEndItemListTablePanel {
	private Composite cpsParent;
	private TCComponentItemRevision ecoRevision;
	
	// End Item List Table
	private Table tblEndItem;
	// End Item List Refresh Button
	private Button btnRefreshEndItem;
	// End Item List Add Button
	private Button btnAddEndItem;
	// End Item List Del Button
	private Button btnDelEndItem;
	
	public ECOAdminCheckEndItemListTablePanel(Composite cpsParent, TCComponentItemRevision ecoRevision) {
		this.cpsParent = cpsParent;
		this.ecoRevision = ecoRevision;
		
		initialize();
	}

	/**
	 * UI 초기화
	 */
	private void initialize() {
		Registry registry = Registry.getRegistry("com.kgm.common.common");
		/*
		 * E/Item Name List
		 */
		Group groupEndItem = new Group(cpsParent, SWT.NONE);
		groupEndItem.setLayout(new GridLayout(3, false));
		groupEndItem.setText("E/Item Name List");
		groupEndItem.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Refresh Button
		GridData gdRefreshEndItem = new GridData(30, 30);
		gdRefreshEndItem.horizontalAlignment = SWT.LEFT;
		gdRefreshEndItem.grabExcessHorizontalSpace = true;
		btnRefreshEndItem = new Button(groupEndItem, SWT.PUSH);
		btnRefreshEndItem.setImage(registry.getImage("Refresh.ICON"));
		btnRefreshEndItem.setToolTipText("Refresh End Item Name List");
		btnRefreshEndItem.setLayoutData(gdRefreshEndItem);
		btnRefreshEndItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					refreshDiseditableRows();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Add Button
		GridData gdAddEndItem = new GridData(60, 30);
		gdAddEndItem.horizontalAlignment = SWT.RIGHT;
		gdAddEndItem.grabExcessHorizontalSpace = true;
		btnAddEndItem = new Button(groupEndItem, SWT.PUSH);
		btnAddEndItem.setText("Add");
		btnAddEndItem.setLayoutData(gdAddEndItem);
		btnAddEndItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addRow();
			}
		});
		
		// Del Button
		GridData gdDelEndItem = new GridData(60, 30);
		gdDelEndItem.horizontalAlignment = SWT.RIGHT;
		btnDelEndItem = new Button(groupEndItem, SWT.PUSH);
		btnDelEndItem.setText("Del");
		btnDelEndItem.setLayoutData(gdDelEndItem);
		btnDelEndItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				delRow();
			}
		});

		// Table
		GridData gdTable = new GridData(GridData.FILL_HORIZONTAL);
		gdTable.horizontalSpan = 3;
		gdTable.heightHint = 300;
		tblEndItem = new Table(groupEndItem, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tblEndItem.setLayoutData(gdTable);
		tblEndItem.setHeaderVisible(true);
		tblEndItem.setLinesVisible(true);

		// Table Header
		final String[] saHeaders = new String[] {	//
				"No.",      	//
				"Part Name",	//
				"C/Type",   	//
				"S/Mode",   	//
				"변경사유",   	//
				"EDITABLE"  	//
				};
		final int[] iaWidths = new int[] {	//
				50, 			// No.
				530,			// Part Name
				80,				// C/Type
				100,			// S/Mode
				120,			// 변경사유
				0				// EDITABLE
				};

		if (saHeaders.length == iaWidths.length) {
			for (int inx = 0; inx < saHeaders.length; inx++) {
				TableColumn tc = new TableColumn(tblEndItem, SWT.NONE);
				tc.setText(saHeaders[inx]);
				tc.setWidth(iaWidths[inx]);
			}
		}

		setTableEditable();		
	}

	/**
	 * <pre>
	 * Table Editable
	 * * Reference : http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/TableEditorexample.htm
	 * </pre>
	 */
	private void setTableEditable() {
		final TableEditor editor = new TableEditor(tblEndItem);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		tblEndItem.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = tblEndItem.getClientArea();
				Point pt = new Point(event.x, event.y);
				int iRowIdx = tblEndItem.getTopIndex();
				while (iRowIdx < tblEndItem.getItemCount()) {
					boolean isVisible = false;
					final TableItem item = tblEndItem.getItem(iRowIdx);
					String sEditable = item.getText(tblEndItem.getColumnCount() - 1);
					
					if ("FALSE".equals(sEditable)) {
						iRowIdx++;
						continue;
					}
					
					for (int inx = 0; inx < tblEndItem.getColumnCount(); inx++) {
						if (inx == 0) {
							continue;
						}
						
						Rectangle rect = item.getBounds(inx);
						if (rect.contains(pt)) {
							final int column = inx;
							final int row = iRowIdx;
							final Text text = new Text(tblEndItem, SWT.NONE);
							Listener textListener = new Listener() {
								public void handleEvent(final Event e) {
									switch (e.type) {
									case SWT.FocusOut:
										item.setText(column, text.getText());
										text.dispose();
										break;
									case SWT.Traverse:
										switch (e.detail) {
										case SWT.TRAVERSE_RETURN:
											item.setText(column, text.getText());
											text.dispose();
//											setCellEditable(editor, row + 1 > tblEndItem.getItemCount() ? row : row + 1, 1);
										case SWT.TRAVERSE_ESCAPE:
											text.dispose();
											e.doit = false;
										case SWT.TRAVERSE_TAB_PREVIOUS:
											item.setText(column, text.getText());
											text.dispose();
											setCellEditable(editor, row, column - 1 == -1 ? column : column - 1);
										case SWT.TRAVERSE_TAB_NEXT:
											item.setText(column, text.getText());
											text.dispose();
											setCellEditable(editor, row, column + 1 > tblEndItem.getColumnCount() ? column : column + 1);
										}
										
										break;
									case SWT.KeyDown:
										String sText = text.getText();
										if (sText != null && !sText.equals("") && sText.length() != 0) {
											item.setText(column, sText.toUpperCase());
										}
										break;
									}
								}
							};
							
							text.addListener(SWT.FocusOut, textListener);
							text.addListener(SWT.Traverse, textListener);
							text.addVerifyListener(createVerifyAdapterForUppercase());
							editor.setEditor(text, item, inx);
							text.setText(item.getText(inx));
							text.selectAll();
							text.setFocus();
							return;
						}
						if (!isVisible && rect.intersects(clientArea)) {
							isVisible = true;
						}
					}
					if (!isVisible)
						return;
					iRowIdx++;
				}
			}
		});
	}
	
	/**
	 * Key Traversal Cell Edit
	 * @param editor
	 */
	private void setCellEditable(final TableEditor editor, final int iRow, int iCol) {
		final TableItem item = tblEndItem.getItem(iRow);
		tblEndItem.setSelection(iRow);
		String sEditable = item.getText(tblEndItem.getColumnCount() - 1);
		
		if ("FALSE".equals(sEditable)) {
			return;
		}
		
		if (iCol == 0) {
			return;
		}
		
		final int column = iCol;
		final Text text = new Text(tblEndItem, SWT.NONE);
		Listener textListener = new Listener() {
			public void handleEvent(final Event e) {
				switch (e.type) {
				case SWT.FocusOut:
					item.setText(column, text.getText());
					text.dispose();
					break;
				case SWT.Traverse:
					switch (e.detail) {
					case SWT.TRAVERSE_RETURN:
						item.setText(column, text.getText());
						text.dispose();
//						setCellEditable(editor, iRow + 1 > tblEndItem.getItemCount() ? iRow : iRow + 1, 1);
					case SWT.TRAVERSE_ESCAPE:
						text.dispose();
						e.doit = false;
					case SWT.TRAVERSE_TAB_PREVIOUS:
						item.setText(column, text.getText());
						text.dispose();
						setCellEditable(editor, iRow, column - 1 == -1 ? column : column - 1);
					case SWT.TRAVERSE_TAB_NEXT:
						item.setText(column, text.getText());
						text.dispose();
						setCellEditable(editor, iRow, column + 1 > tblEndItem.getColumnCount() ? column : column + 1);
					}
					
					break;
				case SWT.KeyDown:
					String sText = text.getText();
					if (sText != null && !sText.equals("") && sText.length() != 0) {
						item.setText(column, sText.toUpperCase());
					}
					break;
				}
			}
		};
		
		text.addListener(SWT.FocusOut, textListener);
		text.addListener(SWT.Traverse, textListener);
		text.addVerifyListener(createVerifyAdapterForUppercase());
		editor.setEditor(text, item, iCol);
		text.setText(item.getText(iCol));
		text.selectAll();
		text.setFocus();
		
		return;
	}
	
	/**
	 * Refresh ECO End Item Name List From ECO BOM List
	 * @throws Exception
	 */
	private void refreshDiseditableRows() throws Exception {
		ArrayList<HashMap<String, String>> alEditableItems = new ArrayList<HashMap<String, String>>();
		int iCount = tblEndItem.getItemCount();
		
		// Editable이 TRUE인 항목 백업
		for (int inx = 0; inx < iCount ; inx++) {
			TableItem item = tblEndItem.getItem(inx);
			String sEditableTemp = item.getText(5);
			
			if (sEditableTemp != null && sEditableTemp.equals("TRUE")) {
				HashMap<String, String> hmEditable = new HashMap<String, String>();
				
				String sPartName = item.getText(1);
				String sCT = item.getText(2);
				String sSMode = item.getText(3);
				String sChangeCause = item.getText(4);
				String sEditable = item.getText(5);
				
				hmEditable.put(ECOAdminCheckConstants.PROP_PART_NAME, sPartName);
				hmEditable.put(ECOAdminCheckConstants.PROP_CT, sCT);
				hmEditable.put(ECOAdminCheckConstants.PROP_SMODE, sSMode);
				hmEditable.put(ECOAdminCheckConstants.PROP_CHANGE_CAUSE, sChangeCause);
				hmEditable.put(ECOAdminCheckConstants.PROP_EDITABLE, sEditable);
				
				alEditableItems.add(hmEditable);
			}
		}
		
		// 현재 Table Clear
		int[] iRemoveItems = new int[iCount];
		for (int inx = 0; inx < iCount; inx++) {
			iRemoveItems[inx] = inx;
		}
		
		delRow(iRemoveItems, true);	// Editable 항목이 FALSE인 항목도 강제 삭제
		
		// ECO C지의 항목 재 로드
		String sECONo = ecoRevision.getProperty("item_id");
		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, String>> resultECOBOMList = dao.selectECOBOMListEndItemNameList(sECONo);
		
		if (resultECOBOMList != null && resultECOBOMList.size() > 0) {
			for (int inx = 0; inx < resultECOBOMList.size(); inx++) {
				HashMap<String, String> hmResultECOBOMList = resultECOBOMList.get(inx);
				addRow(hmResultECOBOMList);
			}
		}

		// 기존 사용자가 직접 입력했던 항목 백업 내용 다시 입력
		if (alEditableItems != null && alEditableItems.size() > 0) {
			for (int inx = 0; inx < alEditableItems.size(); inx++) {
				addRow(alEditableItems.get(inx));
			}
		}
	}

	/**
	 * 신규 Row 추가
	 */
	private void addRow() {
		TableItem tiEndItem = new TableItem(tblEndItem, SWT.NONE);
		tiEndItem.setText(0, String.valueOf(tblEndItem.getItemCount()));
		tiEndItem.setText(1, "");
		tiEndItem.setText(2, "");
		tiEndItem.setText(3, "");
		tiEndItem.setText(4, "");
		tiEndItem.setText(5, "TRUE");	// Editable = TRUE
		
		addChangeCauseTableEditor(tiEndItem, 4);
		
		// Table의 Scroll을 제일 마지막에 추가한 Row로 자동 선택
		tblEndItem.setSelection(tblEndItem.getItemCount() - 1);
		
		// Editable Row Color
		tiEndItem.setBackground(ECOAdminCheckConstants.COLOR_EDITABLE);
		
		refreshTable();
	}
	
	/**
	 * 선택 Row 삭제
	 */
	private void delRow() {
		int[] iaSelectionIndices = tblEndItem.getSelectionIndices();
		
		this.delRow(iaSelectionIndices, false);
	}

	/**
	 * Row 추가
	 * @param hmDataEndItemList
	 */
	private void addRow(HashMap<String, String> hmDataEndItemList) {
		String sPartName = "";
		String sCT = "";
		String sSMode = "";
		String sChangeCause = "";
		String sEditable = "TRUE";

		if (hmDataEndItemList == null) {
			return;
		}

		sPartName = hmDataEndItemList.get(ECOAdminCheckConstants.PROP_PART_NAME);
		sCT = hmDataEndItemList.get(ECOAdminCheckConstants.PROP_CT);
		sSMode = hmDataEndItemList.get(ECOAdminCheckConstants.PROP_SMODE);
		sChangeCause = hmDataEndItemList.get(ECOAdminCheckConstants.PROP_CHANGE_CAUSE);
		sEditable = hmDataEndItemList.get(ECOAdminCheckConstants.PROP_EDITABLE);
		
		TableItem tiEndItem = new TableItem(tblEndItem, SWT.NONE);
		tiEndItem.setText(0, String.valueOf(tblEndItem.getItemCount()));
		tiEndItem.setText(1, sPartName);
		tiEndItem.setText(2, sCT);
		tiEndItem.setText(3, sSMode);
		tiEndItem.setText(4, sChangeCause == null ? "" : sChangeCause);
		tiEndItem.setText(5, sEditable);
		
		addChangeCauseTableEditor(tiEndItem, 4);
		
		if ("FALSE".equals(sEditable)) {
			tiEndItem.setBackground(ECOAdminCheckConstants.COLOR_NOT_EDITABLE);
		} else if ("TRUE".equals(sEditable)) {
			tiEndItem.setBackground(ECOAdminCheckConstants.COLOR_EDITABLE);
		}
		
		refreshTable();
	}

	/**
	 * 
	 * @param tiChangeCause
	 * @param iCol
	 */
	private void addChangeCauseTableEditor(final TableItem tiChangeCause, final int iCol) {
		try {
			GridData gd = new GridData(GridData.FILL_BOTH);
			TableEditor editor = new TableEditor(tblEndItem);
			String[] saChangeCauseLOVValues = SYMCLOVLoader.getLOV(ECOAdminCheckConstants.LOV_CHANGE_CAUSE).getListOfValues().getStringListOfValues();
			final SYMCRadioButton rdo = new SYMCRadioButton(tblEndItem, SWT.NONE, saChangeCauseLOVValues);
			rdo.setBackground(ECOAdminCheckConstants.COLOR_WHITE);
			rdo.setLayoutData(gd);
			editor.grabHorizontal = true;
			editor.setEditor(rdo, tiChangeCause, iCol);
			
			rdo.setSelection(tiChangeCause.getText(iCol));
			
			ArrayList<Button> rdos = rdo.getRadioButtons();
			for (int inx = 0; inx < rdos.size(); inx++) {
				Button btn = rdos.get(inx);
				btn.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						tiChangeCause.setText(iCol, rdo.getValue());
					}
				});
			}
			
			tiChangeCause.setData(ECOAdminCheckConstants.PROP_CHANGE_CAUSE, editor);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * 선택 Row 삭제
	 * @param iaSelectionIndices
	 * @param isForceDeleteRow
	 */
	private void delRow(int[] iaSelectionIndices, boolean isForceDeleteRow) {
		// 선택한 Row 삭제.
		for (int inx = 0; inx < iaSelectionIndices.length; inx++) {
			int iSelectionIndex = iaSelectionIndices[inx];
			
			TableItem item = tblEndItem.getItem(iSelectionIndex);
			String sEditable = item.getText(5);
			
			// 삭제 불가 E/Item Bypass
			if (!isForceDeleteRow && "FALSE".equals(sEditable)) {
				MessageBox.post("You can't remove the rows those are contained 'Not Editable Rows'.", "Check Selection Row", MessageBox.ERROR);
				return;
			}
			
			// [][20160129][jclee] Change Cause 추가
			TableEditor editor = (TableEditor)item.getData(ECOAdminCheckConstants.PROP_CHANGE_CAUSE);
			SYMCRadioButton btn = (SYMCRadioButton)editor.getEditor();
			btn.dispose();
			editor.dispose();
		}
		
		tblEndItem.remove(iaSelectionIndices);
		
		// No 재할당
		for (int inx = 0; inx < tblEndItem.getItemCount(); inx++) {
			tblEndItem.getItem(inx).setText(0, String.valueOf(inx + 1));
		}
		
		refreshTable();
	}

	/**
	 * Clear Table
	 */
	public void clear() {
		tblEndItem.removeAll();
	}
	
	/**
	 * 사용자가 아무것도 입력하지 않은 Row 자동 삭제
	 */
	public void clearEmptyRow() {
		int iItemCount = tblEndItem.getItemCount();
		ArrayList<Integer> alDeleteTargetRow = new ArrayList<Integer>();
		
		for (int inx = 0; inx < iItemCount; inx++) {
			TableItem item = tblEndItem.getItem(inx);
			
			String sPartName = item.getText(1);
			String sCT = item.getText(2);
			String sSMode = item.getText(3);
			String sChangeCause = item.getText(4);
			String sEditable = item.getText(5);
			
			if (sEditable.equals("FALSE")) {
				continue;
			}
			
			if ((sPartName == null || sPartName.equals("") || sPartName.length() == 0) &&
				(sCT == null || sCT.equals("") || sCT.length() == 0) &&
				(sSMode == null || sSMode.equals("") || sSMode.length() == 0) &&
				(sChangeCause == null || sChangeCause.equals("") || sChangeCause.length() == 0)) {
				alDeleteTargetRow.add(inx);
			}
		}
		
		Object[] oDeleteTargetRow = alDeleteTargetRow.toArray();
		int[] iaDeleteTargetRow = new int[oDeleteTargetRow.length];
		for (int inx = 0; inx < oDeleteTargetRow.length; inx++) {
			iaDeleteTargetRow[inx] = Integer.parseInt(oDeleteTargetRow[inx].toString());
		}
		
		delRow(iaDeleteTargetRow, false);
	}
	
	/**
	 * Load End Item List
	 */
	public void load() {
		try {
			CustomECODao dao = new CustomECODao();
			
			if (ecoRevision == null) {
				return;
			}
			
			String sECONo = ecoRevision.getProperty("item_id");
			
			ArrayList<HashMap<String, String>> result = dao.selectECOEplEndItemNameList(sECONo);
			
			for (int inx = 0; inx < result.size(); inx++) {
				HashMap<String, String> hmResult = result.get(inx);
				addRow(hmResult);
			}
			
			// Empty Row일 경우 ECO C지에서 목록 로드
			if (result.size() == 0) {
				ArrayList<HashMap<String, String>> resultECOBOMList = dao.selectECOBOMListEndItemNameList(sECONo);
				
				for (int inx = 0; inx < resultECOBOMList.size(); inx++) {
					HashMap<String, String> hmResultECOBOMList = resultECOBOMList.get(inx);
					addRow(hmResultECOBOMList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * Table 반환
	 * @return
	 */
	public Table getTable() {
		return tblEndItem;
	}
	
	/**
	 * 자동 대문자 변경 Listener
	 * @return
	 */
	private VerifyListener createVerifyAdapterForUppercase() {
		return new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent event) {
				event.text = event.text.toUpperCase();
			}
		};
	}
    
    /**
     * Table Refresh
     */
    private void refreshTable() {
		tblEndItem.setSize(tblEndItem.getSize().x, tblEndItem.getSize().y-1);
		tblEndItem.setSize(tblEndItem.getSize().x, tblEndItem.getSize().y+1);
    }
}
