package com.symc.plm.rac.prebom.preospec.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridEditor;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.symc.plm.rac.prebom.masterlist.view.clipboard.TextTransfer;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class PreOSpecMandatoryTable extends Composite {
	private TCSession session;
	private Grid table;
	private OSpec ospec;
	
	/**
	 * Constructor
	 * @param parent
	 * @param ospec
	 * @param iType
	 */
	public PreOSpecMandatoryTable(Composite parent, OSpec ospec, int iType) {
		super(parent, iType);
		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		this.ospec = ospec;
		
		table = new Grid(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER | SWT.MULTI | SWT.WRAP);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setRowHeaderVisible(true);
        table.setLinesVisible(true);
        table.setCellSelectionEnabled(false);
        table.setRowsResizeable(false);
        
        initColumn();
        initListener();
	}

	/**
	 * Column 생성
	 */
	private void initColumn() {
		int iStyle = SWT.BORDER | SWT.CENTER;
		
		// Option Category
		GridColumn gcOptionCategory = new GridColumn(table, iStyle);
		gcOptionCategory.setText("Category");
		gcOptionCategory.setWidth(80);
		
		// Option Value
		GridColumn gcOptionValue = new GridColumn(table, iStyle);
		gcOptionValue.setText("Code");
		gcOptionValue.setWidth(80);
		
		// Trims
		ArrayList<OpTrim> alTrim = ospec.getTrimList();
		GridColumnGroup gcgPreArea = null;
		GridColumnGroup gcgPrePassenger = null;
		GridColumnGroup gcgPreEngine = null;
		
		for (int inx = 0; inx < alTrim.size(); inx++) {
			OpTrim ot = alTrim.get(inx);
			
			String sArea = ot.getArea();
			String sPassenger = ot.getPassenger();
			String sEngine = ot.getEngine();
			String sGrade = ot.getGrade();
			String sTrim = ot.getTrim();
			
			boolean isNewArea = false;
			if (gcgPreArea == null || !gcgPreArea.getText().equals(sArea)) {
				GridColumnGroup gcgArea = new GridColumnGroup(table, iStyle);
				gcgArea.setText(sArea);
				gcgPreArea = gcgArea;
				isNewArea = true;
			}
			
			boolean isNewPassenger = false;
			if (isNewArea || (gcgPrePassenger == null || !gcgPrePassenger.getText().equals(sPassenger))) {
				GridColumnGroup gcgPassenger = new GridColumnGroup(table, iStyle);
				gcgPassenger.setText(sPassenger);
				gcgPrePassenger = gcgPassenger;
				gcgPassenger.setParentGroup(gcgPreArea);
				isNewPassenger = true;
			}
			
			if (isNewPassenger || (gcgPreEngine == null || !gcgPreEngine.getText().equals(sEngine))) {
				GridColumnGroup gcgEngine = new GridColumnGroup(table, iStyle);
				gcgEngine.setText(sEngine);
				gcgPreEngine = gcgEngine;
				gcgEngine.setParentGroup(gcgPrePassenger);
			}
			
			GridColumnGroup gcgGrade = new GridColumnGroup(table, iStyle);
			gcgGrade.setText(sGrade);
			gcgGrade.setParentGroup(gcgPreEngine);
			
			GridColumn gcTrim = new GridColumn(gcgGrade, iStyle | SWT.CHECK);
			gcTrim.setText(sTrim);
			gcTrim.setWidth(100);
		}
		
		// OSpec Remark
		GridColumn gcOSpecRemark = new GridColumn(table, iStyle);
		gcOSpecRemark.setText("OSpec Remark");
		gcOSpecRemark.setWidth(200);
		
		// Remark
		GridColumn gcRemark = new GridColumn(table, iStyle);
		gcRemark.setText("Remark");
		gcRemark.setWidth(200);
	}
	
	/**
	 * Initailize Editor
	 */
	private void initListener() {
		final GridEditor editor = new GridEditor(table);
        table.addMouseListener( new MouseAdapter() {
            public void mouseDown( MouseEvent e) {
            	Point pt = new Point(e.x, e.y);
            	
            	final GridColumn column = table.getColumn(pt);
            	if (!(column.getText().equals("Category") || column.getText().equals("Code") || column.getText().equals("OSpec Remark") || column.getText().equals("Remark"))) {
            		return;
            	}
            	
                Control oldEditor = editor.getEditor();
                if ( oldEditor != null)
                    oldEditor.dispose();

                final GridItem item = table.getItem(pt);
                final Point cell = table.getCell(pt);
                if ( item == null || cell == null)
                    return;

                // The control that will be the editor must be a child of the Table
                final Text newEditor = new Text(table, SWT.MULTI | SWT.BORDER | SWT.WRAP);
                newEditor.setText( item.getText( cell.x));
                newEditor.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent arg0) {
						item.setText(cell.x, newEditor.getText());
						newEditor.dispose();
					}
				});
                
                newEditor.setFocus();
                editor.setEditor( newEditor, item, cell.x);
                editor.grabHorizontal = true;
                editor.grabVertical = true;
            }
        });
        
        table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent paramKeyEvent) {
				if (paramKeyEvent.stateMask == SWT.CTRL && (paramKeyEvent.keyCode == 'V' || paramKeyEvent.keyCode == 'v')) {
					pasteRow();
				}
			}
		});
	}
	
	/**
	 * Load
	 */
	@SuppressWarnings("unchecked")
	public void load() {
		try {
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.setString("OSPEC_NO", ospec.getOspecNo());
			ArrayList<HashMap<String, String>> alResult = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.PreOSpecService", "selectPreOSpecMandatoryInfo", ds);
			
			for (int inx = 0; inx < alResult.size(); inx++) {
				HashMap<String, String> hmResult = (HashMap<String, String>) alResult.get(inx);
				addRow(hmResult);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * Get Table
	 * @return
	 */
	public Grid getTable() { 
		return table;
	}

	/**
	 * Get O/Spec
	 * @return
	 */
	public OSpec getOSpec() {
		return ospec;
	}
	
	/**
	 * 
	 */
	public void addRow() {
		try {
			GridItem item = new GridItem(table, SWT.BORDER);
			int iColumnCount = table.getColumnCount();
			
			for (int inx = 0; inx < iColumnCount; inx++) {
				if (item.getCheckable(inx)) {
					item.setChecked(inx, false);
				}
				
				item.setText(inx, "");
			}
			
			String sListID = getSysGuid();
			item.setData("LIST_ID", sListID);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * 
	 * @param hmInfo
	 * @return
	 */
	public GridItem addRow(HashMap<String, String> hmInfo) {
		GridItem item = new GridItem(table, SWT.BORDER);
		
		loadInfo(item, hmInfo);
		loadTrim(item, hmInfo);
		
		return item;
	}
	
	/**
	 * Get Oracle SYS_GUID()
	 * @return
	 * @throws Exception
	 */
	private String getSysGuid() throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
        ds.put("PARAM", null);
		return (String) remote.execute("com.ssangyong.service.MasterListService", "getSysGuid", ds);
	}
	
	/**
	 * Load Info
	 * @param item
	 * @param hmInfo
	 */
	private void loadInfo(GridItem item, HashMap<String, String> hmInfo) {
		String sListID = hmInfo.get("LIST_ID");
		String sOptionCategory = hmInfo.get("OPTION_CATEGORY");
		String sOptionValue = hmInfo.get("OPTION_VALUE");
		String sOSpecRemark = hmInfo.get("OSPEC_REMARK");
		String sRemarkType = hmInfo.get("REMARK_TYPE");
		String sRemark = hmInfo.get("REMARK");

		item.setText(0, sOptionCategory);
		item.setText(1, sOptionValue);
		item.setText(table.getColumnCount() - 2, sOSpecRemark == null ? "" : sOSpecRemark);
		item.setText(table.getColumnCount() - 1, sRemarkType + " (" + sRemark + ")");
		
		item.setData("LIST_ID", sListID);
	}
	
	/**
	 * Load Trims
	 * @param item
	 * @param hmInfo
	 */
	@SuppressWarnings("unchecked")
	private void loadTrim(GridItem item, HashMap<String, String> hmInfo) {
		try {
			int iTrimStartColumn = 2;
			// Select From DB
			String sListID = hmInfo.get("LIST_ID");
			ArrayList<HashMap<String, String>> result = null;
			
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.setString("LIST_ID", sListID);
			result = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.PreOSpecService", "selectPreOSpecMandatoryTrim", ds);
			
			for (int inx = 0; inx < result.size(); inx++) {
				HashMap<String, String> hmResult = (HashMap<String, String>)result.get(inx);
				String sTrimResult = hmResult.get("TRIM");
				
				for (int jnx = iTrimStartColumn; jnx < table.getColumnCount(); jnx++) {
					GridColumn column = table.getColumn(jnx);
					String sTrim = column.getText();
					
					if (sTrim.equals(sTrimResult)) {
						if (item.getCheckable(jnx)) {
							item.setChecked(jnx, true);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * 
	 */
	public void delRow() {
		table.remove(table.getSelectionIndices());
	}
	
	/**
	 * Paste Data From Excel
	 * Cell Width : [1 : Category] + [1 : Option Value] + [n : Trim Length] + [1 : Remark]
	 */
	private void pasteRow() {
		int[] iaSelectionIndices = table.getSelectionIndices();
		Arrays.sort(iaSelectionIndices);
		
		if (!(iaSelectionIndices.length > 0)) {
			return;
		}
		
		TextTransfer transfer = new TextTransfer();
		String sContents = transfer.getClipboardContents();
		String[] sSplittedContentsReturn = sContents.split("\n");
		GridItem item = null;
		
		for (int inx = 0; inx < sSplittedContentsReturn.length; inx++) {
			try {
				// 선택한 Row에 최우선으로 insert
				item = table.getItem(iaSelectionIndices[inx]);
			} catch (Exception e) {
				try {
					// 선택한 Row가 없을 경우 마지막으로 Insert한 Row의 다음 Row에 Insert
					GridItem[] items = table.getItems();
					for (int jnx = 0; jnx < items.length; jnx++) {
						String sPrevListID = items[jnx].getData("LIST_ID").toString();
						
						if (item != null && item.getData("LIST_ID").toString().equals(sPrevListID)) {
							item = table.getItem(jnx + 1);
							break;
						} else {
							new Exception("Add Row");
						}
					}
				} catch (Exception e2) {
					// Table에 더이상의 Row가 남아있지 않을 경우 Row를 새로 추가하여 Insert
					addRow();
					item = table.getItem(table.getItemCount() - 1);
				}
			}
			
			if (item == null) {
				MessageBox.post("Paste Operation Error.\nPlease contact to administrator.", "Paste Operation Error", MessageBox.ERROR);
				return;
			}
			
			String[] sSplittedContents = sSplittedContentsReturn[inx].split("\t");
			
			if (sSplittedContents.length == table.getColumnCount() - 1 /* 가장 마지막 Column 1개는 사용자 입력 Column이므로 제외 */) {
				for (int jnx = 0; jnx < sSplittedContents.length; jnx++) {
					if (sSplittedContents[jnx] != null && !sSplittedContents[jnx].equals("")
							&& jnx != 0	// Category
							&& jnx != 1	// Code
							&& jnx != sSplittedContents.length - 1	// OSpec Remark
							) {
						item.setChecked(jnx, true);
					} else if ((sSplittedContents[jnx] == null || sSplittedContents[jnx].equals(""))
							&& jnx != 0	// Category
							&& jnx != 1	// Code
							&& jnx != sSplittedContents.length - 1	// OSpec Remark
							) {
						item.setChecked(jnx, false);
					} else {
						item.setText(jnx, sSplittedContents[jnx]);
					}
				}
			}
		}
	}
	
	/**
	 * Mandatory Info
	 * @param iRow
	 * @return
	 */
	public HashMap<String, String> getMandatoryInfo(int iRow, boolean isForValidation) {
		HashMap<String, String> hmMandatoryInfo = new HashMap<String, String>();
		String sOptionCategory = "";
		String sOptionValue = "";
		String sOSpecRemark = "";
		String sRemarkTemp = "";
		String sRemarkType = "";
		String sRemark = "";
		String sListID = "";
		
		GridItem item = table.getItem(iRow);
		sOptionCategory = item.getText(0);
		sOptionValue = item.getText(1);
		sOSpecRemark = item.getText(table.getColumnCount() - 2);
		sRemarkTemp = item.getText(table.getColumnCount() - 1);
		sRemarkType = sRemarkTemp.substring(0, sRemarkTemp.indexOf("(")).trim();
		sRemark = sRemarkTemp.substring(sRemarkTemp.indexOf("(") + 1, sRemarkTemp.indexOf(")")).trim();
		sListID = item.getData("LIST_ID").toString();
		
		hmMandatoryInfo.put("OSPEC_NO", ospec.getOspecNo());
		hmMandatoryInfo.put("OPTION_CATEGORY", sOptionCategory);
		hmMandatoryInfo.put("OPTION_VALUE", sOptionValue);
		hmMandatoryInfo.put("OSPEC_REMARK", sOSpecRemark);
		hmMandatoryInfo.put("REMARK_TYPE", sRemarkType);
		hmMandatoryInfo.put("REMARK", isForValidation ? sRemarkTemp : sRemark);
		
		try {
			hmMandatoryInfo.put("CREATE_USER", session.getUser().getUserId());
		} catch (Exception e) {
			MessageBox.post(e);
		}
		
		hmMandatoryInfo.put("LIST_ID", sListID);
		
		return hmMandatoryInfo;
	}
	
	/**
	 * Mandatory Checked Trims
	 * @param iRow
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getMandatoryTrim(int iRow) {
		int iTrimStartColumn = 2;
		
		ArrayList<HashMap<String, String>> alMandatoryTrim = new ArrayList<HashMap<String, String>>();
		String sListID = "";
		
		GridItem item = table.getItem(iRow);
		sListID = item.getData("LIST_ID").toString();
		
		for (int inx = iTrimStartColumn; inx < table.getColumnCount() - 3; inx++) {
			boolean checked = item.getChecked(inx);
			
			if (checked) {
				HashMap<String, String> hmMandatoryTrim = new HashMap<String, String>();
				
				hmMandatoryTrim.put("TRIM", table.getColumn(inx).getText());
				hmMandatoryTrim.put("LIST_ID", sListID);
				
				alMandatoryTrim.add(hmMandatoryTrim);
			}
		}
		
		return alMandatoryTrim;
	}
	
	/**
	 * Refresh the List IDs
	 */
	public void refreshListIDs() {
		Markpoint mp = null;
		
		try {
			mp = new Markpoint(session);
			
			int iRowCount = table.getItemCount();
			for (int inx = 0; inx < iRowCount; inx++) {
				refreshListID(inx);
			}
			
			mp.forget();
		} catch (Exception e) {
			try {
				mp.rollBack();
			} catch (TCException e1) {
				e1.printStackTrace();
				MessageBox.post(e1);
			}
			
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * Refresh a List ID
	 * @param iRow
	 */
	public void refreshListID(int iRow) {
		try {
			GridItem item = table.getItem(iRow);
			
			String sListID = getSysGuid();
			item.setData("LIST_ID", sListID);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * Remove All Rows
	 */
	public void removeAll() {
		table.removeAll();
	}
}