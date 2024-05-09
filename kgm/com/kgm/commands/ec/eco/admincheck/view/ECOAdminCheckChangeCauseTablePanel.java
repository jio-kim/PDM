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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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

public class ECOAdminCheckChangeCauseTablePanel {
	private Composite cpsParent;
	private TCComponentItemRevision ecoRevision;
	
	// Change Cause Table
	private Table tblChangeCause;
	// Change Cause Add Button
	private Button btnAddChangeCause;
	// Change Cause Del Button
	private Button btnDelChangeCause;
	
	private int iTotalCount = -1;
	
	/**
	 * Constructor
	 * @param cpsParent
	 * @param ecoRevision
	 */
	public ECOAdminCheckChangeCauseTablePanel(Composite cpsParent, TCComponentItemRevision ecoRevision) {
		this.cpsParent = cpsParent;
		this.ecoRevision = ecoRevision;
		
		initialize();
	}

	/**
	 * UI �ʱ�ȭ
	 */
	private void initialize() {
		/*
		 * �������
		 */
		Group groupChangeCause = new Group(cpsParent, SWT.NONE);
		groupChangeCause.setLayout(new GridLayout(3, false));
		groupChangeCause.setText("�������");
		groupChangeCause.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Change Cause Description
		GridData gdChangeCauseDescLbl = new GridData();
		gdChangeCauseDescLbl.horizontalAlignment = SWT.RIGHT;
		gdChangeCauseDescLbl.horizontalSpan = 3;
		Label lblChangeCauseDesc = new Label(groupChangeCause, SWT.NONE);
		lblChangeCauseDesc.setText("01: ���躯��(ǰ������,��������,����,�������,����ȭ��)\n" +
								   "02: �ű� �߰�(O/SPEC,�ű� �߰�,���ε� ����)\n" +
								   "03: EPL ����(������,Option ����, S/MODE,DR,SEQ,DMU��)");
		lblChangeCauseDesc.setLayoutData(gdChangeCauseDescLbl);
		
		/*
		 * Total Count
		 */
		GridData gdTotalCountLbl = new GridData();
		gdTotalCountLbl.horizontalAlignment = SWT.LEFT;
		Label lblTotalCount = new Label(groupChangeCause, SWT.NONE);
		
		try {
			CustomECODao dao = new CustomECODao();
			String sECONo = ecoRevision.getProperty("item_id");
			ArrayList<HashMap<String, String>> result = dao.selectECOEplEndItemList(sECONo);
			
			iTotalCount = result.size();
			
			lblTotalCount.setText("E/Item Total Name Group Count : " + iTotalCount);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		lblTotalCount.setLayoutData(gdTotalCountLbl);
		
		// Add Button
		GridData gdAddChangeCause = new GridData(60, 30);
		gdAddChangeCause.horizontalAlignment = SWT.RIGHT;
		gdAddChangeCause.grabExcessHorizontalSpace = true;
		btnAddChangeCause = new Button(groupChangeCause, SWT.PUSH);
		btnAddChangeCause.setText("Add");
		btnAddChangeCause.setLayoutData(gdAddChangeCause);
		btnAddChangeCause.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addRow();
			}
		});
		
		// Del Button
		GridData gdDelChangeCause = new GridData(60, 30);
		gdDelChangeCause.horizontalAlignment = SWT.RIGHT;
		btnDelChangeCause = new Button(groupChangeCause, SWT.PUSH);
		btnDelChangeCause.setText("Del");
		btnDelChangeCause.setLayoutData(gdDelChangeCause);
		btnDelChangeCause.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				delRow();
			}
		});

		// Table
		GridData gdTable = new GridData(GridData.FILL_HORIZONTAL);
		gdTable.horizontalSpan = 3;
		gdTable.heightHint = 100;
		tblChangeCause = new Table(groupChangeCause, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tblChangeCause.setLayoutData(gdTable);
		tblChangeCause.setHeaderVisible(true);
		tblChangeCause.setLinesVisible(true);
		
		// Table Header
		final String[] saHeaders = new String[] {	//
				"ECO No",
				"No.",
				"Project Code",
				"Change Cause",
				"End Item Count1",
				"End Item Count2"
				};
		final int[] iaWidths = new int[] {	//
				0,				// ECO No
				50, 			// No.
				120,			// Project Code
				120,			// Change Cause
				120,			// End Item Count1
				120				// End Item Count2
				};

		if (saHeaders.length == iaWidths.length) {
			for (int inx = 0; inx < saHeaders.length; inx++) {
				TableColumn tc = new TableColumn(tblChangeCause, SWT.NONE);
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
		final TableEditor editor = new TableEditor(tblChangeCause);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		tblChangeCause.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = tblChangeCause.getClientArea();
				Point pt = new Point(event.x, event.y);
				int iRowIdx = tblChangeCause.getTopIndex();
				while (iRowIdx < tblChangeCause.getItemCount()) {
					boolean isVisible = false;
					final TableItem item = tblChangeCause.getItem(iRowIdx);
					
					for (int inx = 0; inx < tblChangeCause.getColumnCount(); inx++) {
						if (inx == 0 || inx == 1) {
							continue;
						}
						
						Rectangle rect = item.getBounds(inx);
						if (rect.contains(pt)) {
							final int column = inx;
							final int row = iRowIdx;

							final Text text = new Text(tblChangeCause, SWT.NONE);
							
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
											setCellEditable(editor, row, column + 1 > tblChangeCause.getColumnCount() ? column : column + 1);
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
							
							if (inx == 3 || inx == 4 || inx == 5) {
								text.addVerifyListener(createVerifyAdapterForNumber());
							} else {
								text.addVerifyListener(createVerifyAdapterForUppercase());
							}
							
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
		final TableItem item = tblChangeCause.getItem(iRow);
		tblChangeCause.setSelection(iRow);
		
		if (iCol == 0 || iCol == 1) {
			return;
		}
		
		final int column = iCol;

		final Text text = new Text(tblChangeCause, SWT.NONE);
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
						setCellEditable(editor, iRow, column + 1 > tblChangeCause.getColumnCount() ? column : column + 1);
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
		
		if (iCol == 3 || iCol == 4 || iCol == 5) {
			text.addVerifyListener(createVerifyAdapterForNumber());
		} else {
			text.addVerifyListener(createVerifyAdapterForUppercase());
		}
		
		editor.setEditor(text, item, iCol);
		text.setText(item.getText(iCol));
		text.selectAll();
		text.setFocus();
		
		return;
	}

	/**
	 * �ű� Row �߰�
	 */
	private void addRow() {
		String sECONo = "";
		try {
			sECONo = ecoRevision.getProperty("item_id");
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		TableItem tiChangeCause = new TableItem(tblChangeCause, SWT.NONE);
		tiChangeCause.setText(0, sECONo);
		tiChangeCause.setText(1, String.valueOf(tblChangeCause.getItemCount()));
		tiChangeCause.setText(2, "");
		tiChangeCause.setText(3, "");
		tiChangeCause.setText(4, "");
		tiChangeCause.setText(5, "");

		addChangeCauseTableEditor(tiChangeCause, 3);
		
		// Editable Row Color
		tiChangeCause.setBackground(ECOAdminCheckConstants.COLOR_WHITE);
		
		// Table�� Scroll�� ���� �������� �߰��� Row�� �ڵ� ����
		tblChangeCause.setSelection(tblChangeCause.getItemCount() - 1);
		
		refreshTable();
	}
	
	/**
	 * ���� Row ����
	 */
	private void delRow() {
		int[] iaSelectionIndices = tblChangeCause.getSelectionIndices();
		
		this.delRow(iaSelectionIndices);
	}

	/**
	 * Row �߰�
	 */
	private void addRow(HashMap<String, Object> hmDataChangeCause) {
		String sECONo = "";
		String sSEQNo = "";
		String sProjectCode = "";
		String sChangeCause = "";
		String sEndItemCountA = "";
		String sEndItemCountM = "";

		if (hmDataChangeCause == null) {
			return;
		}

		sECONo = getString(hmDataChangeCause.get(ECOAdminCheckConstants.PROP_ECO_NO));
		sSEQNo = getString(hmDataChangeCause.get(ECOAdminCheckConstants.PROP_SEQ_NO));
		sProjectCode = getString(hmDataChangeCause.get(ECOAdminCheckConstants.PROP_PROJECT_CODE));
		sChangeCause = getString(hmDataChangeCause.get(ECOAdminCheckConstants.PROP_CHANGE_CAUSE));
		sEndItemCountA = getIntegerToString(hmDataChangeCause.get(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_A));
		sEndItemCountM = getIntegerToString(hmDataChangeCause.get(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_M));
		
		TableItem tiChangeCause = new TableItem(tblChangeCause, SWT.NONE);
		tiChangeCause.setText(0, sECONo);
		tiChangeCause.setText(1, sSEQNo);
		tiChangeCause.setText(2, sProjectCode);
		tiChangeCause.setText(3, sChangeCause);
		tiChangeCause.setText(4, sEndItemCountA);
		tiChangeCause.setText(5, sEndItemCountM);
		
		addChangeCauseTableEditor(tiChangeCause, 3);
		tiChangeCause.setBackground(ECOAdminCheckConstants.COLOR_WHITE);
		
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
			TableEditor editor = new TableEditor(tblChangeCause);
			String[] saChangeCauseLOVValues = SYMCLOVLoader.getLOV(ECOAdminCheckConstants.LOV_CHANGE_CAUSE).getListOfValues().getStringListOfValues();
			final SYMCRadioButton rdo = new SYMCRadioButton(tblChangeCause, SWT.NONE, saChangeCauseLOVValues);
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
	 * ���� Row ����
	 */
	private void delRow(int[] iaSelectionIndices) {
		for (int inx = 0; inx < iaSelectionIndices.length; inx++) {
			TableItem item = tblChangeCause.getItem(iaSelectionIndices[inx]);
			TableEditor editor = (TableEditor)item.getData(ECOAdminCheckConstants.PROP_CHANGE_CAUSE);
			SYMCRadioButton btn = (SYMCRadioButton)editor.getEditor();
			btn.dispose();
			editor.dispose();
		}
		
		// ������ Row ����.
		tblChangeCause.remove(iaSelectionIndices);
		
		// No ���Ҵ�
		for (int inx = 0; inx < tblChangeCause.getItemCount(); inx++) {
			tblChangeCause.getItem(inx).setText(1, String.valueOf(inx + 1));
		}

		// Table�� Scroll�� ������ ��� ������ Row �� ���� ù��° ������ Index�� �̵�.
		// ���� �ش� Index�� Table Row�� �Ѿ ��� Table�� ���� ������ Row ���� �̵�
		if (iaSelectionIndices == null || iaSelectionIndices.length == 0) {
			return;
		}
		int iSelectionRow = iaSelectionIndices[0];
		if (iSelectionRow > tblChangeCause.getItemCount() - 1) {
			iSelectionRow = tblChangeCause.getItemCount() - 1;
		}
		tblChangeCause.setSelection(iSelectionRow);
		
		refreshTable();
	}

	/**
	 * Clear Table
	 */
	public void clear() {
		tblChangeCause.removeAll();
	}
	
	/**
	 * ����ڰ� �ƹ��͵� �Է����� ���� Row �ڵ� ����
	 */
	public void clearEmptyRow() {
		int iItemCount = tblChangeCause.getItemCount();
		ArrayList<Integer> alDeleteTargetRow = new ArrayList<Integer>();
		
		for (int inx = 0; inx < iItemCount; inx++) {
			TableItem item = tblChangeCause.getItem(inx);
			
			String sProjectCode = item.getText(2);
			String sChangeCause = item.getText(3);
			String sEndItemCountA = item.getText(4);
			String sEndItemCountM = item.getText(5);
			
			if ((sProjectCode == null || sProjectCode.equals("") || sProjectCode.length() == 0) &&
				(sChangeCause == null || sChangeCause.equals("") || sChangeCause.length() == 0) &&
				(sEndItemCountA == null || sEndItemCountA.equals("") || sEndItemCountA.length() == 0) &&
				(sEndItemCountM == null || sEndItemCountM.equals("") || sEndItemCountM.length() == 0)) {
				alDeleteTargetRow.add(inx);
			}
		}
		
		Object[] oDeleteTargetRow = alDeleteTargetRow.toArray();
		int[] iaDeleteTargetRow = new int[oDeleteTargetRow.length];
		for (int inx = 0; inx < oDeleteTargetRow.length; inx++) {
			iaDeleteTargetRow[inx] = Integer.parseInt(oDeleteTargetRow[inx].toString());
		}
		
		delRow(iaDeleteTargetRow);
	}
	
	/**
	 * Load Change Cause
	 */
	public void load() {
		try {
			CustomECODao dao = new CustomECODao();
			
			if (ecoRevision == null) {
				return;
			}
			
			String sECONo = ecoRevision.getProperty("item_id");
			
			ArrayList<HashMap<String, Object>> result = dao.selectECOChangeCause(sECONo);
			
			// ���� Open �� �ڵ����� 1�� Empty Row Add
			if (result.size() == 0) {
				HashMap<String, Object> hmDataChangeCause = new HashMap<String, Object>();
				
				hmDataChangeCause.put(ECOAdminCheckConstants.PROP_ECO_NO, sECONo);
				hmDataChangeCause.put(ECOAdminCheckConstants.PROP_SEQ_NO, "1");
				hmDataChangeCause.put(ECOAdminCheckConstants.PROP_PROJECT_CODE, "");
				hmDataChangeCause.put(ECOAdminCheckConstants.PROP_CHANGE_CAUSE, "");
				hmDataChangeCause.put(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_A, null);
				hmDataChangeCause.put(ECOAdminCheckConstants.PROP_END_ITEM_COUNT_M, null);
				
				addRow(hmDataChangeCause);
			}
			
			for (int inx = 0; inx < result.size(); inx++) {
				HashMap<String, Object> hmResult = result.get(inx);
				addRow(hmResult);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * Table ��ȯ
	 * @return
	 */
	public Table getTable() {
		return tblChangeCause;
	}
	
	/**
	 * �ڵ� �빮�� ���� Listener
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
	 * ���ڸ� �Է� �����ϵ��� ���� Listener
	 * @return
	 */
	private VerifyListener createVerifyAdapterForNumber() {
		return new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent event) {
				Text text = (Text)event.getSource();

	            final String oldS = text.getText();
	            String newS = oldS.substring(0, event.start) + event.text + oldS.substring(event.end);

	            boolean isInteger = true;
	            try {
	                Integer.parseInt(newS);
	            } catch(NumberFormatException ex) {
	                isInteger = false;
	            }
	            
	            boolean isDeleteOrEnter = false;
	            if (event.keyCode == 0 || event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR || event.keyCode == SWT.DEL || event.keyCode == SWT.BS) {
	            	isDeleteOrEnter = true;
				}
	            
	            if(!isInteger && !isDeleteOrEnter)
	            	event.doit = false;
			}
		};
	}
	
	/**
	 * ECO End Item Total Count for Validation
	 * @return
	 */
	public int getEndItemTotalCount() {
		return iTotalCount;
	}
    
    /**
     * Get Null String To Empty.
     * @param object
     * @return
     */
    private String getString(Object object) {
    	if (object == null) {
			return "";
		} else {
			return object.toString();
		}
    }
    
    /**
     * Get Integer Object or Null Object To String
     * @param object
     * @return
     */
    private String getIntegerToString(Object object) {
    	if (object == null) {
			return "";
		} else {
			return Integer.valueOf(object.toString()).toString();
		}
    }
    
    /**
     * Table Refresh
     */
    private void refreshTable() {
		tblChangeCause.setSize(tblChangeCause.getSize().x, tblChangeCause.getSize().y-1);
		tblChangeCause.setSize(tblChangeCause.getSize().x, tblChangeCause.getSize().y+1);
    }
}
