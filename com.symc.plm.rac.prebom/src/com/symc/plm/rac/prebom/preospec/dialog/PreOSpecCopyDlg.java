package com.symc.plm.rac.prebom.preospec.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.SWTUtilities;
import com.symc.plm.rac.prebom.preospec.ui.PreOSpecMandatoryTable;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * [20160517][ymjang] O/Spec No 정렬
 */
public class PreOSpecCopyDlg extends SYMCAbstractDialog {
	private TCSession session;
	private Composite cpsParent;
	private PreOSpecMandatoryTable table;

	private Combo cmbProject;
	private Combo cmbOSpecNo;
	private Button btnSearch;
	
	private Grid tableResult;
	private HashMap<String, ArrayList<String>> hmOSpecNo;
	
	/**
	 * Constructor
	 * @param parent
	 * @param table
	 */
	public PreOSpecCopyDlg(Shell parent, PreOSpecMandatoryTable table) {
		super(parent);

		this.session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		this.table = table;
	}

	/**
	 * 
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		SWTUtilities.skipESCKeyEvent(getShell());
		SWTUtilities.skipKeyEvent(getShell());
		
		getShell().setText("Copy Pre OSpec Mandatory Options");
		
		setBlockOnOpen(false);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.MODELESS);
		setApplyButtonVisible(false);
		
		cpsParent = new Composite(parentScrolledComposite, SWT.None);
        cpsParent.setLayout(new GridLayout());
        cpsParent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
        initUI();
        initTable();
        initListener();
        
		return cpsParent;
	}
	
	/**
	 * Initialize UI
	 */
	private void initUI() {
		// UI Main Composite
		Composite cpsUI = new Composite(cpsParent, SWT.NONE);
		GridLayout glUI = new GridLayout(5, false);
		cpsUI.setLayout(glUI);
		GridData gdUI = new GridData(GridData.FILL_HORIZONTAL);
		cpsUI.setLayoutData(gdUI);
		
		// Project
		GridData gdProject = new GridData();
		gdProject.horizontalAlignment = SWT.LEFT;
		Label lblProject = new Label(cpsUI, SWT.BOLD);
		lblProject.setText("Project : ");
		lblProject.setLayoutData(gdProject);
		
		String[] saProjects = getPreProjects();
		cmbProject = new Combo(cpsUI, SWT.BORDER);
		cmbProject.setItems(saProjects);
		
		// OSpec No
		GridData gdOSpecNo = new GridData();
		gdOSpecNo.horizontalAlignment = SWT.LEFT;
		Label lblOSpecNo = new Label(cpsUI, SWT.BOLD);
		lblOSpecNo.setText("OSpec No : ");
		lblOSpecNo.setLayoutData(gdOSpecNo);
		
		cmbOSpecNo = new Combo(cpsUI, SWT.BORDER | SWT.READ_ONLY);
		cmbOSpecNo.setEnabled(false);
		
		// Buttons
		GridData gdSearch = new GridData();
		gdSearch.horizontalAlignment = SWT.RIGHT;
		gdSearch.grabExcessHorizontalSpace = true;
		btnSearch = new Button(cpsUI, SWT.PUSH);
		btnSearch.setText("Search");
		btnSearch.setLayoutData(gdSearch);
	}
	
	/**
	 * Initialize Pre OSpec Search Result Table
	 */
	private void initTable() {
		Composite cpsTable = new Composite(cpsParent, SWT.NONE);
		cpsTable.setLayout(new GridLayout());
		cpsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tableResult = new Grid(cpsTable, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER | SWT.WRAP);
		tableResult.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableResult.setHeaderVisible(true);
        tableResult.setRowHeaderVisible(true);
        tableResult.setLinesVisible(true);
        tableResult.setCellSelectionEnabled(false);
        tableResult.setRowsResizeable(false);
        
		initTableColumn();
		
		cpsTable.pack();
		cpsTable.layout();
	}
	
	/**
	 * Initialize Table Columns
	 */
	private void initTableColumn() {
		int iStyle = SWT.BORDER | SWT.CENTER;
		
		// Project
		GridColumn gcProject = new GridColumn(tableResult, iStyle);
		gcProject.setText("Project");
		gcProject.setWidth(120);
		
		// OSpec No
		GridColumn gcOSpecNo = new GridColumn(tableResult, iStyle);
		gcOSpecNo.setText("OSpec No");
		gcOSpecNo.setWidth(200);
	}

	/**
	 * Initialize Listeners
	 */
	private void initListener() {
		tableResult.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent paramMouseEvent) {
				okPressed();
			}
		});
		
		cmbProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setOSpecNoCmb();
			}
		});
		
		cmbProject.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.BS || event.keyCode == SWT.DEL) {
					cmbProject.select(0);
					setOSpecNoCmb();
				}
				
				if (!(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR)) {
					return;
				}
				
				int iSelectedProject = -1;
				
				iSelectedProject = cmbProject.getSelectionIndex();
				
				if (iSelectedProject > 0) {
					setOSpecNoCmb();
				} else {
					String s = cmbProject.getText();
					String[] items = cmbProject.getItems();
					
					for (int inx = 0; inx < items.length; inx++) {
						String sItem = items[inx];
						
						if (s != null && sItem != null) {
							s = s.toUpperCase();
							
							if (s.equals(sItem)) {
								cmbProject.select(inx);
								setOSpecNoCmb();
							}
						}
					}
				}
			}
		});
		
		cmbOSpecNo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (!(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR)) {
					return;
				}
				
				int iSelectedOSpecNo = -1;
				
				iSelectedOSpecNo = cmbOSpecNo.getSelectionIndex();
				
				if (iSelectedOSpecNo > 0) {
					search();
				} else {
					String s = cmbOSpecNo.getText();
					String[] items = cmbOSpecNo.getItems();
					
					for (int inx = 0; inx < items.length; inx++) {
						String sItem = items[inx];
						
						if (s != null && sItem != null) {
							s = s.toUpperCase();
							
							if (s.equals(sItem)) {
								cmbOSpecNo.select(inx);
								search();
							}
						}
					}
				}
			}
		});
		
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				search();
			}
		});
	}
	
	/**
	 * Set Pre Project Combobox and Collect OSpecs
	 * @return
	 */
	private String[] getPreProjects() {
		ArrayList<String> alProjects = new ArrayList<String>();
		hmOSpecNo = new HashMap<String, ArrayList<String>>();
		
		try {
			TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			TCComponentQuery query = (TCComponentQuery) queryType.find("SYMC_Search_PreProductRevision_Released");
			TCComponent[] caPreProductRevisionReleased = query.execute(new String[] {"Type"}, new String[] {"Pre Product Revision"});
			if (caPreProductRevisionReleased.length > 0) {
				for (int inx = 0; inx < caPreProductRevisionReleased.length; inx++) {
					String sProjectCode = caPreProductRevisionReleased[inx].getProperty("s7_PROJECT_CODE");
					String sOSpecNo = caPreProductRevisionReleased[inx].getProperty("s7_OSPEC_NO");
					
					alProjects.add(sProjectCode);
					
					// Collecting OSpec No
					if (hmOSpecNo.containsKey(sProjectCode)) {
						hmOSpecNo.get(sProjectCode).add(sOSpecNo);
					} else {
						ArrayList<String> alOSpecNos = new ArrayList<String>();
						alOSpecNos.add(sOSpecNo);
						hmOSpecNo.put(sProjectCode, alOSpecNos);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		if (alProjects != null && alProjects.size() > 0) {
			alProjects = new ArrayList<String>(new LinkedHashSet<String>(alProjects));	// remove duplicates
			Collections.sort(alProjects);	// sort
			alProjects.add(0, "");
			return alProjects.toArray(new String[0]);
		}
		
		return null;
	}
	
	/**
	 * Set OSpec No Combobox
	 */
	private void setOSpecNoCmb() {
		int iSelectedProject = cmbProject.getSelectionIndex();
		
		if (iSelectedProject < 0) {
			return;
		}
		
		String sProject = cmbProject.getItem(iSelectedProject);
		
		if (hmOSpecNo.containsKey(sProject)) {
			ArrayList<String> alOSpecNos = hmOSpecNo.get(sProject);
			//[20160517][ymjang] O/Spec No 정렬 
			Collections.sort(alOSpecNos);
			
			if (alOSpecNos.size() > 0) {
				@SuppressWarnings("unchecked")
				ArrayList<String> alClonedOSpecNos = (ArrayList<String>) alOSpecNos.clone();
				alClonedOSpecNos.add(0, "");
				
				String[] saOSpecNos = alClonedOSpecNos.toArray(new String[0]);
				
				cmbOSpecNo.setItems(saOSpecNos);
				cmbOSpecNo.setEnabled(true);
				cmbOSpecNo.setFocus();
			}
		} else {
			cmbOSpecNo.clearSelection();
			cmbOSpecNo.removeAll();
			cmbOSpecNo.setEnabled(false);
		}
	}
	
	/**
	 * Search
	 */
	private void search() {
		tableResult.removeAll();
		
		int iSelectedProject = -1;
		iSelectedProject = cmbProject.getSelectionIndex();
		
		String sSelectedProject = iSelectedProject > 0 ? cmbProject.getItem(iSelectedProject) : ""; 
		if (iSelectedProject > 0) {
			int iSelectedOSpecNo = -1;
			iSelectedOSpecNo = cmbOSpecNo.getSelectionIndex();
			ArrayList<String> alOSpecNos = hmOSpecNo.get(sSelectedProject);
			String sSelectedOSpecNo = iSelectedOSpecNo > 0 ? cmbOSpecNo.getItem(iSelectedOSpecNo) : "";
			
			for (int inx = 0; inx < alOSpecNos.size(); inx++) {
				String sOSpecNo = alOSpecNos.get(inx);
				
				if (iSelectedOSpecNo > 0) {
					if (sOSpecNo.equals(sSelectedOSpecNo)) {
						addRow(sSelectedProject, sOSpecNo);
						break;
					} else {
						continue;
					}
				} else {
					addRow(sSelectedProject, sOSpecNo);
				}
			}
		} else {
			Set<String> keySet = hmOSpecNo.keySet();
			Iterator<String> iterator = keySet.iterator();
			while (iterator.hasNext()) {
				String sKey = iterator.next();
				ArrayList<String> alOSpecNos = hmOSpecNo.get(sKey);
				
				for (int inx = 0; inx < alOSpecNos.size(); inx++) {
					String sOSpecNo = alOSpecNos.get(inx);
					
					if (sOSpecNo == null || sOSpecNo.equals("") || sOSpecNo.length() == 0) {
						continue;
					}
					
					addRow(sKey, sOSpecNo);
				}
			}
		}
	}
	
	/**
	 * Add Row
	 * @param sProject
	 * @param sOSpecNo
	 */
	private void addRow(String sProject, String sOSpecNo) {
		GridItem item = new GridItem(tableResult, SWT.BORDER);
		
		item.setText(0, sProject);
		item.setText(1, sOSpecNo);
	}
	
	/**
	 * 
	 */
	@Override
	protected boolean validationCheck() {
		GridItem[] giSelected = tableResult.getSelection();
		if (giSelected.length != 1) {
			MessageBox.post("Select a row.", "Select a row.", MessageBox.ERROR);
			return false;
		}
		
		return true;
	}

	/**
	 * 
	 */
	@Override
	protected boolean apply() {
		try {
			table.removeAll();
			
			GridItem[] giSelected = tableResult.getSelection();
			String sOSpecNo = giSelected[0].getText(1);
			
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.setString("OSPEC_NO", sOSpecNo);
			
			@SuppressWarnings("unchecked")
			ArrayList<HashMap<String, String>> alResult = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.PreOSpecService", "selectPreOSpecMandatoryInfo", ds);
			
			for (int inx = 0; inx < alResult.size(); inx++) {
				HashMap<String, String> hmResult = (HashMap<String, String>) alResult.get(inx);
				table.addRow(hmResult);
			}
			
			table.refreshListIDs();	// Change the New List IDs
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return false;
	}
}
