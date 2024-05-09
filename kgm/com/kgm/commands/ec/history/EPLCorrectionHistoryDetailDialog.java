package com.kgm.commands.ec.history;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.teamcenter.rac.kernel.TCSession;

public class EPLCorrectionHistoryDetailDialog extends SYMCAbstractDialog {
	private TCSession session;
	private Button btnClose;
	private String sECONo = "";
	private Table resultTable;
	private Color evenColor;
	private Color deletedRowColor;
	private Color modifiedColumnColor;
	
	private final int COL_NO = 0;
	private final int COL_PROJECT_CODE = 1;
	private final int COL_PROJECT_CODE_CORRECTION = 2;
	private final int COL_FIND_NO = 3;
	private final int COL_CT = 4;
	private final int COL_PARENT_NO = 5;
	private final int COL_PARENT_REV = 6;
	private final int COL_PARENT_REV_CORRECTION = 7;
	private final int COL_PART_ORIGIN = 8;
	private final int COL_PART_NO = 9;
	private final int COL_PART_REV = 10;
	private final int COL_PART_REV_CORRECTION = 11;
	private final int COL_PART_NAME = 12;
	private final int COL_IC = 13;
	private final int COL_SMODE = 14;
	private final int COL_QTY = 15;
	private final int COL_ALT = 16;
	private final int COL_SEL = 17;
	private final int COL_CAT = 18;
	private final int COL_COLOR = 19;
	private final int COL_COLOR_SECTION = 20;
	private final int COL_MCODE = 21;
	private final int COL_PLT_STK = 22;
	private final int COL_AS_STK = 23;
	private final int COL_COST = 24;
	private final int COL_TOOL = 25;
	private final int COL_SHOWN_ON = 26;
	private final int COL_OPTION = 27;
	private final int COL_CHANGE_DESCRIPTION = 28;
	
	public EPLCorrectionHistoryDetailDialog(Shell parent, int paramInt, String sECONo) {
		super(parent);
		
		setBlockOnOpen(false);
		
		this.session = CustomUtil.getTCSession();
		this.sECONo = sECONo;
		
		evenColor = new Color(parent.getDisplay(), 190, 215, 250);
		deletedRowColor = new Color(parent.getDisplay(), 255, 100, 50);
		modifiedColumnColor = new Color(parent.getDisplay(), 100, 200, 100);
	}

	/** 버튼 변경 */
	protected void createButtonsForButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		btnClose = new Button(composite, SWT.PUSH);
		btnClose.setText("Close");
		btnClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getShell().close();
			}
		});
	}
	
	@Override
	protected void createDialogWindow(Composite paramComposite) {
		Shell dialogParentShell = getShell();
		dialogParentShell.setBackground(dialogParentShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		dialogParentShell.setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayout gl = new GridLayout();
		gl.marginBottom = 5;
		gl.marginHeight = 0;
		gl.marginLeft = 5;
		gl.marginRight = 5;
		gl.marginTop = 5;
		gl.marginWidth = 0;
		paramComposite.setLayout(gl);
		createDialogPanel(paramComposite);
	}

	/**
	 * Implement Method이지만 사용하지 않음. (ScrolledComposite로 인한 가로 스크롤 불가현상 발생)
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected Composite createDialogPanel(Composite parentScrolledComposite) {
		getShell().setText("EPL Correction List");
		Composite cpsMain = new Composite(parentScrolledComposite, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginBottom = 0;
		gl.marginHeight = 0;
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginTop = 0;
		gl.marginWidth = 0;
		
		cpsMain.setLayout(gl);
		cpsMain.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		resultTable = new Table(cpsMain, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_BOTH);
        resultTable.setLayoutData(gd);
        resultTable.setHeaderVisible(true);
        resultTable.setLinesVisible(true);
		
		createTableColumn("No", 40);
        createTableColumn("Proj.", 50);
        createTableColumn("Correction Proj.", 50);
        createTableColumn("Find No", 57);
        createTableColumn("C/T", 35);
        createTableColumn("Parent No", 100);
        createTableColumn("Parent Rev", 70);
        createTableColumn("Correction Parent Rev", 70);
        createTableColumn("Part Origin", 25);
        createTableColumn("Part No", 100);
        createTableColumn("Part Rev", 60);
        createTableColumn("Correction Part Rev", 60);
        createTableColumn("Part Name", 180);
        createTableColumn("IC", 42);
        createTableColumn("Supply Mode", 90);
        createTableColumn("QTY", 35);
        createTableColumn("ALT", 40);
        createTableColumn("SEL", 40);
        createTableColumn("CAT", 50);
        createTableColumn("Color", 45);
        createTableColumn("Color Section", 120);
        createTableColumn("Module Code", 120);
        createTableColumn("PLT Stk", 80);
        createTableColumn("A/S Stk", 80);
        createTableColumn("Cost", 60);
        createTableColumn("Tool", 60);
        createTableColumn("Shown-On", 90);
        createTableColumn("Options", 160);
        createTableColumn("Change Desc", 180);
        
		load();
		
		return cpsMain;
	}
	
    private TableColumn createTableColumn(String columnName, int width) {
        TableColumn column = new TableColumn(resultTable, SWT.NONE);
        column.setText(columnName);
        column.setWidth(width);
        column.setResizable(true);
        column.setMoveable(true);
        return column;
    }

	/**
	 * 로드
	 */
	private void load() {
		try {
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
	        DataSet ds = new DataSet();
	        ds.put("ecoNo", sECONo);
	        // EPL Load
	        final ArrayList<SYMCBOMEditData> resultECOEPL = (ArrayList<SYMCBOMEditData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECOEplList", ds);
	        
	        // Correction Load
			ArrayList<HashMap<String, String>> resultCorrection = (new CustomECODao()).selectECOEplCorrectionList(sECONo);
			
			resultTable.removeAll();
			ArrayList<String> addedEPLs = new ArrayList<String>();
			for (int inx = 0; inx < resultECOEPL.size(); inx++) {
				SYMCBOMEditData bedECOEPL = resultECOEPL.get(inx);
				
				// EPL 중복 제거
				String eplId = bedECOEPL.getEplId();
				if (addedEPLs.contains(eplId)) {
					continue;
				}
				
				String[] rowOldItemData = new String[29];
				String[] rowNewItemData = new String[29];
				TableItem itemOld = new TableItem(resultTable, SWT.None);
				TableItem itemNew = new TableItem(resultTable, SWT.None);
				
				String sCorrectionOldProjectCode = "";
				String sCorrectionNewProjectCode = "";
				String sCorrectionParentRev = "";
				String sCorrectionOldPartRev = "";
				String sCorrectionNewPartRev = "";
				
				boolean isModifiedOldProject = false;
				boolean isModifiedNewProject = false;
				boolean isModifiedParentRev = false;
				boolean isModifiedOldRev = false;
				boolean isModifiedNewRev = false;
				
				for (int jnx = 0; jnx < resultCorrection.size(); jnx++) {
					HashMap<String, String> correctionRow = resultCorrection.get(jnx);
					String sEPLID = correctionRow.get("EPL_ID");
					String sOccThreads = correctionRow.get("OCC_THREADS");
					
					if (bedECOEPL.getEplId().equals(sEPLID) || bedECOEPL.getOccUid().equals(sOccThreads)) {
						sCorrectionOldProjectCode = correctionRow.get("OLD_PROJECT");
						sCorrectionNewProjectCode = correctionRow.get("NEW_PROJECT");
						sCorrectionParentRev = correctionRow.get("PARENT_REV");
						sCorrectionOldPartRev = correctionRow.get("OLD_PART_REV");
						sCorrectionNewPartRev = correctionRow.get("NEW_PART_REV");
						
						isModifiedOldProject = modifiedStrings(bedECOEPL.getProjectOld(), sCorrectionOldProjectCode);
			            isModifiedNewProject = modifiedStrings(bedECOEPL.getProjectNew(), sCorrectionNewProjectCode);
						isModifiedParentRev = modifiedStrings(bedECOEPL.getParentRev(), sCorrectionParentRev);
						isModifiedOldRev = modifiedStrings(bedECOEPL.getPartRevOld(), sCorrectionOldPartRev);
			            isModifiedNewRev = modifiedStrings(bedECOEPL.getPartRevNew(), sCorrectionNewPartRev);
						
						resultCorrection.remove(jnx);
						break;
					}
				}
				
				/**
				 * Old Part No
				 */
	            // NO
				rowOldItemData[0] = getRowNo(itemOld);
	            // Proj.
	            rowOldItemData[1] = bedECOEPL.getProjectOld();
	            rowOldItemData[2] = isModifiedOldProject || isModifiedNewProject ? sCorrectionOldProjectCode : "";
	            
	            // SEQ
	            rowOldItemData[3] = bedECOEPL.getSeqOld();
	            // C/T
	            rowOldItemData[4] = bedECOEPL.getChangeType().equals("D") ? bedECOEPL.getChangeType() : "";
	            // Parent No, Parent Rev
	            rowOldItemData[5] = bedECOEPL.getParentNo();
	            rowOldItemData[6] = bedECOEPL.getParentRev();
	            rowOldItemData[7] = isModifiedParentRev ? sCorrectionParentRev : "";
	            
	            // Part Origin
	            rowOldItemData[8] = bedECOEPL.getPartOriginOld();
	            // Part No
	            rowOldItemData[9] = bedECOEPL.getPartNoOld();
	            // Part Rev
	            rowOldItemData[10] = bedECOEPL.getPartRevOld();
	            rowOldItemData[11] = isModifiedOldRev || isModifiedNewRev ? sCorrectionOldPartRev : "";
	            
	            // Part Name
	            rowOldItemData[12] = bedECOEPL.getPartNameOld();
	            // IC
	            rowOldItemData[13] = bedECOEPL.getIcOld();
	            // Supply Mode
	            rowOldItemData[14] = bedECOEPL.getSupplyModeOld();
	            // QTY
	            rowOldItemData[15] = bedECOEPL.getQtyOld();
	            // ALT
	            rowOldItemData[16] = bedECOEPL.getAltOld();
	            // SEL
	            rowOldItemData[17] = bedECOEPL.getSelOld();
	            // CAT
	            rowOldItemData[18] = bedECOEPL.getCatOld();
	            // Color
	            rowOldItemData[19] = bedECOEPL.getColorIdOld();
	            // Color Section
	            rowOldItemData[20] = bedECOEPL.getColorSectionOld();
	            // Module Code
	            rowOldItemData[21] = bedECOEPL.getModuleCodeOld();
	            // PLT Stk
	            rowOldItemData[22] = bedECOEPL.getPltStkOld();
	            // A/S Stk
	            rowOldItemData[23] = bedECOEPL.getAsStkOld();
	            // Cost
	            rowOldItemData[24] = "";
	            // Tool
	            rowOldItemData[25] = "";
	            // Shown-On
	            rowOldItemData[26] = bedECOEPL.getShownOnOld();
	            // Options
	            rowOldItemData[27] = bedECOEPL.getVcOld() != null ? bedECOEPL.getVcOld().toString() : "";                       
	            // Change Desc
	            rowOldItemData[28] = bedECOEPL.getChangeType().equals("D") ? bedECOEPL.getChgDesc() : "";

	            itemOld.setText(rowOldItemData);
	            
	            /**
	             * New Part No
	             */
	            // NO
	            rowNewItemData[0] = bedECOEPL.getChangeType().equals("D") ? "" : getRowNo(itemNew);
	            // Proj.
	            rowNewItemData[1] = bedECOEPL.getChangeType().equals("D") ? "" : bedECOEPL.getProjectNew();
	            rowNewItemData[2] = bedECOEPL.getChangeType().equals("D") ? "" : (isModifiedOldProject || isModifiedNewProject ? sCorrectionNewProjectCode : "");
	            
	            // SEQ
	            rowNewItemData[3] = bedECOEPL.getSeqNew();
	            // C/T
	            if (!"".equals(StringUtil.nullToString(bedECOEPL.getPartNoNew()))) {
	                rowNewItemData[4] = bedECOEPL.getChangeType();
	            }
	            // Parent No, Parent Rev
	            if (!"".equals(StringUtil.nullToString(bedECOEPL.getPartNoNew()))) {
	                rowNewItemData[5] = bedECOEPL.getParentNo();
	                rowNewItemData[6] = bedECOEPL.getParentRev();
	                rowNewItemData[7] = isModifiedParentRev ? sCorrectionParentRev : "";
	            }
	            // Part Origin
	            rowNewItemData[8] = bedECOEPL.getPartOriginNew();
	            // Part No
	            rowNewItemData[9] = bedECOEPL.getPartNoNew();
	            // Part Rev
	            rowNewItemData[10] = bedECOEPL.getPartRevNew();
	            rowNewItemData[11] = isModifiedOldRev || isModifiedNewRev ? sCorrectionNewPartRev : "";
	            
	            // Part Name
	            rowNewItemData[12] = bedECOEPL.getPartNameNew();
	            // IC
	            rowNewItemData[13] = bedECOEPL.getIcNew();
	            // Supply Mode
	            rowNewItemData[14] = bedECOEPL.getSupplyModeNew();
	            // QTY
	            rowNewItemData[15] = bedECOEPL.getQtyNew();
	            // ALT
	            rowNewItemData[16] = bedECOEPL.getAltNew();
	            // SEL
	            rowNewItemData[17] = bedECOEPL.getSelNew();
	            // CAT
	            rowNewItemData[18] = bedECOEPL.getCatNew();
	            // Color
	            rowNewItemData[19] = bedECOEPL.getColorIdNew();
	            // Color Section
	            rowNewItemData[20] = bedECOEPL.getColorSectionNew();
	            // Module Code
	            rowNewItemData[21] = bedECOEPL.getModuleCodeNew();
	            // PLT Stk
	            rowNewItemData[22] = ""; // rows.get(i).getPltStkOld();
	            // A/S Stk
	            rowNewItemData[23] = ""; // rows.get(i).getAsStkOld();
	            // Cost
	            rowNewItemData[24] = bedECOEPL.getCostNew();
	            // Tool
	            rowNewItemData[25] = bedECOEPL.getToolNew();
	            // Shown-On
	            rowNewItemData[26] = bedECOEPL.getShownOnNew();
	            // Options
	            rowNewItemData[27] = bedECOEPL.getVcNew() != null ? bedECOEPL.getVcNew().toString() : "";
	            // Change Desc
	            rowNewItemData[28] = bedECOEPL.getChgDesc();

	            itemNew.setText(rowNewItemData);
	            String sRowNo = getRowNo(itemNew);
	            
	            // Table 기본 Row 색상
	            if (Integer.parseInt(sRowNo) % 2 == 0) {
	            	itemOld.setBackground(evenColor);
					itemNew.setBackground(evenColor);
				}
	            
	            // Project Column 색상
	            if (isModifiedOldProject || isModifiedNewProject) {
	            	itemOld.setBackground(COL_PROJECT_CODE, modifiedColumnColor);
	            	itemNew.setBackground(COL_PROJECT_CODE, modifiedColumnColor);
					itemOld.setBackground(COL_PROJECT_CODE_CORRECTION, modifiedColumnColor);
					itemNew.setBackground(COL_PROJECT_CODE_CORRECTION, modifiedColumnColor);
				}
	            
	            // Parent Rev Column 색상
	            if (isModifiedParentRev) {
	            	itemOld.setBackground(COL_PARENT_REV, modifiedColumnColor);
	            	itemNew.setBackground(COL_PARENT_REV, modifiedColumnColor);
	            	itemOld.setBackground(COL_PARENT_REV_CORRECTION, modifiedColumnColor);
	            	itemNew.setBackground(COL_PARENT_REV_CORRECTION, modifiedColumnColor);
				}
	            
	            // Part Rev Column 색상
	            if (isModifiedOldRev || isModifiedNewRev) {
	            	itemOld.setBackground(COL_PART_REV, modifiedColumnColor);
	            	itemNew.setBackground(COL_PART_REV, modifiedColumnColor);
	            	itemOld.setBackground(COL_PART_REV_CORRECTION, modifiedColumnColor);
	            	itemNew.setBackground(COL_PART_REV_CORRECTION, modifiedColumnColor);
				}
	            
	            addedEPLs.add(eplId);
			}
			
			for (int inx = 0; inx < resultCorrection.size(); inx++) {
				HashMap<String, String> hmCorrection = resultCorrection.get(inx);
				String[] rowOldItemData = new String[29];
				String[] rowNewItemData = new String[29];
				TableItem itemOld = new TableItem(resultTable, SWT.None);
				TableItem itemNew = new TableItem(resultTable, SWT.None);
				
				/**
				 * Old Part No
				 */
	            // NO
				rowOldItemData[0] = getRowNo(itemOld);
	            // Proj.
	            rowOldItemData[1] = hmCorrection.get("OLD_PROJECT");
	            rowOldItemData[2] = "";
	            // SEQ
	            rowOldItemData[3] = hmCorrection.get("OLD_SEQ");
	            // C/T
	            rowOldItemData[4] = hmCorrection.get("CT").equals("D") ? hmCorrection.get("CT") : "";
	            // Parent No, Parent Rev
	            rowOldItemData[5] = hmCorrection.get("PARENT_NO");
	            rowOldItemData[6] = hmCorrection.get("PARENT_REV");
	            rowOldItemData[7] = "";
	            // Part Origin
	            rowOldItemData[8] = hmCorrection.get("OLD_PART_ORIGIN");
	            // Part No
	            rowOldItemData[9] = hmCorrection.get("OLD_PART_NO");
	            // Part Rev
	            rowOldItemData[10] = hmCorrection.get("OLD_PART_REV");
	            // Part Rev
	            rowOldItemData[11] = "";
	            // Part Name
	            rowOldItemData[12] = hmCorrection.get("OLD_PART_NAME");
	            // IC
	            rowOldItemData[13] = hmCorrection.get("OLD_IC");
	            // Supply Mode
	            rowOldItemData[14] = hmCorrection.get("OLD_SMODE");
	            // QTY
	            rowOldItemData[15] = hmCorrection.get("OLD_QTY").toString();
	            // ALT
	            rowOldItemData[16] = hmCorrection.get("OLD_APART");
	            // SEL
	            rowOldItemData[17] = ""; // bedECOEPL.getSelOld();
	            // CAT
	            rowOldItemData[18] = hmCorrection.get("OLD_CAT");
	            // Color
	            rowOldItemData[19] = hmCorrection.get("OLD_COLOR");
	            // Color Section
	            rowOldItemData[20] = hmCorrection.get("OLD_COLOR_ID");
	            // Module Code
	            rowOldItemData[21] = hmCorrection.get("OLD_MCODE");
	            // PLT Stk
	            rowOldItemData[22] = hmCorrection.get("OLD_PLT_STK");
	            // A/S Stk
	            rowOldItemData[23] = hmCorrection.get("OLD_AS_STK");
	            // Cost
	            rowOldItemData[24] = "";
	            // Tool
	            rowOldItemData[25] = "";
	            // Shown-On
	            rowOldItemData[26] = hmCorrection.get("NEW_SHOWN_ON");
	            // Options
	            rowOldItemData[27] = hmCorrection.get("OLD_VC") != null ? hmCorrection.get("OLD_VC") : "";                       
	            // Change Desc
	            rowOldItemData[28] = hmCorrection.get("CT").equals("D") ? hmCorrection.get("CHG_DESC") : "";

	            itemOld.setText(rowOldItemData);
	            
	            /**
	             * New Part No
	             */
	            // NO
	            rowNewItemData[0] = hmCorrection.get("CT").equals("D") ? "" : getRowNo(itemNew);
	            // Proj.
	            rowNewItemData[1] = hmCorrection.get("CT").equals("D") ? "" : hmCorrection.get("NEW_PROJECT");
	            rowNewItemData[2] = "";
	            // SEQ
	            rowNewItemData[3] = hmCorrection.get("NEW_SEQ");
	            // C/T
	            if (!"".equals(StringUtil.nullToString(hmCorrection.get("NEW_PART_NO")))) {
	                rowNewItemData[4] = hmCorrection.get("CT");
	            }
	            // Parent No, Parent Rev
	            if (!"".equals(StringUtil.nullToString(hmCorrection.get("NEW_PART_NO")))) {
	                rowNewItemData[5] = hmCorrection.get("PARENT_NO");
	                rowNewItemData[6] = hmCorrection.get("PARENT_REV");
	                rowNewItemData[7] = "";
	            }
	            // Part Origin
	            rowNewItemData[8] = hmCorrection.get("NEW_PART_ORIGIN");
	            // Part No
	            rowNewItemData[9] = hmCorrection.get("NEW_PART_NO");
	            // Part Rev
	            rowNewItemData[10] = hmCorrection.get("NEW_PART_REV");
	            rowNewItemData[11] = "";
	            // Part Name
	            rowNewItemData[12] = hmCorrection.get("NEW_PART_NAME"); // bedECOEPL.getPartNameNew();
	            // IC
	            rowNewItemData[13] = hmCorrection.get("NEW_IC");
	            // Supply Mode
	            rowNewItemData[14] = hmCorrection.get("NEW_SMODE");
	            // QTY
	            rowNewItemData[15] = hmCorrection.get("NEW_QTY").toString();
	            // ALT
	            rowNewItemData[16] = hmCorrection.get("NEW_APART");
	            // SEL
	            rowNewItemData[17] = hmCorrection.get("NEW_SEL");
	            // CAT
	            rowNewItemData[18] = hmCorrection.get("NEW_CAT");
	            // Color
	            rowNewItemData[19] = hmCorrection.get("NEW_COLOR");
	            // Color Section
	            rowNewItemData[20] = hmCorrection.get("NEW_COLOR_SECTION");
	            // Module Code
	            rowNewItemData[21] = hmCorrection.get("NEW_MCODE");
	            // PLT Stk
	            rowNewItemData[22] = "";
	            // A/S Stk
	            rowNewItemData[23] = "";
	            // Cost
	            rowNewItemData[24] = hmCorrection.get("NEW_COST");
	            // Tool
	            rowNewItemData[25] = hmCorrection.get("NEW_TOOL");
	            // Shown-On
	            rowNewItemData[26] = hmCorrection.get("NEW_SHOWN_ON");
	            // Options
	            rowNewItemData[27] = hmCorrection.get("NEW_VC") != null ? hmCorrection.get("NEW_VC") : "";
	            // Change Desc
	            rowNewItemData[28] = hmCorrection.get("CHG_DESC");

	            itemNew.setText(rowNewItemData);
	            
	            itemOld.setBackground(deletedRowColor);
	            itemNew.setBackground(deletedRowColor);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * String 비교
	 * @param sSource
	 * @param sTarget
	 * @return
	 */
	private boolean modifiedStrings(String sSource, String sTarget) {
		if (sSource == null || sSource.equals("") || sSource.length() == 0) {
			if (sTarget != null && !sTarget.equals("")) {
				return true;
			}
			return false;
		}
		
		if (sTarget == null || sTarget.equals("") || sTarget.length() == 0) {
			if (sSource != null && !sSource.equals("")) {
				return true;
			}
			
			return false;
		}
		
		if (!sSource.equals(sTarget)) {
			return true;
		}
		
		return false;
	}
	
    private String getRowNo(TableItem item) {
        int row = resultTable.indexOf(item);
        return String.valueOf(row / 2 + 1);
    }

	@Override
	protected boolean validationCheck() {
		return false;
	}

	@Override
	protected boolean apply() {
		return false;
	}
}
