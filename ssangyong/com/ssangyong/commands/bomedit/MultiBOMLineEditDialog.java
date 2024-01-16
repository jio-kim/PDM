package com.ssangyong.commands.bomedit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.search.ECOSearchDialog;
import com.ssangyong.common.SYMCLOVCombo;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.utils.SWTUtilities;
import com.teamcenter.rac.kernel.SYMCBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class MultiBOMLineEditDialog extends SYMCAbstractDialog {
	
	private TCComponentBOMLine[] targets;
	private Display display;
	private boolean modifiable;
	private Color modifiedColor;
	
	private Table table;
	private Text tEcoNo;
    private TCComponentItemRevision ecoRev;
	private SYMCLOVCombo cSupplyMode;
	//EBOM 개선과제 - SYSTEM CODE 추가
	private SYMCLOVCombo cSystemCode;
	private SYMCLOVCombo cAlterPart;
	private SYMCLOVCombo cModuleCode;
	private Text tPosDesc;
	
	/** Column Index of ECO No */
	private final static int COLUMN_ECO = 8;
	/** Column Index of Supply Mode */
	private final static int COLUMN_SMODE = 9;
	//EBOM 개선과제 - SYSTEM CODE 추가
	/** Column Index of System Code */
	private final static int COLUMN_SCODE = 10;
	/** Column Index of Alter Part */
	private final static int COLUMN_APART = 11;
	/** Column Index of Module Code*/
	private final static int COLUMN_MCODE = 12;
	/** Column Index of Position Desc */
	private final static int COLUMN_PDESC = 13;
	

	public MultiBOMLineEditDialog(Shell shell, TCComponentBOMLine[] targets) {
		super(shell);
		this.targets = targets;
		display = shell.getDisplay();
	}
	
	/**
	 * initialize control
	 */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parent) {
        modifiedColor = new Color(display, 255, 225, 225);
        modifiable = true;
        //setApplyButtonVisible(false);
        Composite main = new Composite(parent, SWT.None);
        getShell().setText("Edit Multi BOMLine");
        getShell().setMinimumSize(990, 320);
        SWTUtilities.skipESCKeyEvent(getShell());
        main.setLayout(new GridLayout(9, false));
        
        GridData gdSprator = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdSprator.horizontalSpan = 9;
        
        createBOMTableComposite(main);
        
        Label lSeparator = new Label(main, SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.setLayoutData(gdSprator);
        
        createPropertiesComposite(main);
        
        loadBOMLineProperties();
        
        return main;
    }
    
    /**
     * initialize bom properties table
     * @param parent
     */
    private void createBOMTableComposite(Composite parent) {
        GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true);
        gdTable.horizontalSpan = 9;

        table = new Table(parent, SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createTableColumn("Level", 40);
        createTableColumn("Parent No", 100);
        createTableColumn("Parent Rev", 70);
        createTableColumn("Find No", 55);
        createTableColumn("Part No", 100);
        createTableColumn("Part Rev", 60);
        createTableColumn("Part Name", 220);
        createTableColumn("Qty", 45);
        createTableColumn("ECO No", 74);
        createTableColumn("S/Mode", 60);
        //EBOM 개선과제 - SYSTEM CODE 추가
        createTableColumn("SYSTEM CODE", 100);
        createTableColumn("ALTER PART", 80);
        createTableColumn("Module Code", 90);
        createTableColumn("Position Desc", 120);
        table.setLayoutData(gdTable);
        /*table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {               
                if (e.button == 1) {
                    addCellEditEvent(e);
                }
            }
        });*/
    }
    
    /**
     * add table column
     * @param columnName
     * @param width
     * @return
     */
    private TableColumn createTableColumn(String columnName, int width) {
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(columnName);
        column.setWidth(width);
        return column;
    }

    /**
     * initialize attribute multi input control
     * @param parent
     */
    private void createPropertiesComposite(Composite parent) {
        Registry registry = Registry.getRegistry("com.ssangyong.common.common");
        
        GridData gdLFill = new GridData(SWT.END, SWT.TOP, false, false);
        gdLFill.widthHint = 80;
        GridData gdCPref = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdCPref.widthHint = 180;
        //gdCPref.heightHint = 22;
        
        Label lEcoNo = new Label(parent, SWT.RIGHT);
        lEcoNo.setLayoutData(gdLFill);
        lEcoNo.setText("ECO No");
        Composite pEcoNo = new Composite(parent, SWT.None);
        GridLayout pEcoNoLayout = new GridLayout(2, false);
        pEcoNoLayout.marginHeight = 0;
        pEcoNoLayout.marginWidth = 0;
        pEcoNo.setLayout(pEcoNoLayout);
        pEcoNo.setLayoutData(gdCPref);
        tEcoNo = new Text(pEcoNo, SWT.BORDER | SWT.READ_ONLY);
        GridData gdECO = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdECO.widthHint = 120;
        tEcoNo.setLayoutData(gdECO);
        Button bEcoNo = new Button(pEcoNo, SWT.CENTER);        
        bEcoNo.setText("Search...");
        gdECO = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdECO.widthHint = 72;
        gdECO.heightHint = 22;
        bEcoNo.setLayoutData(gdECO);
        bEcoNo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                searchEcoBtnListener(e);
            }
        });
        GridData gdApply = new GridData();
        gdApply.heightHint = 22;
        gdApply.widthHint = 22;
        Button bECOApply = new Button(parent, SWT.PUSH);
        bECOApply.setImage(registry.getImage("Apply_16.ICON"));
        bECOApply.setLayoutData(gdApply);
        bECOApply.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setECOProperties(ecoRev);
            }
        });
        
        Label lSMode = new Label(parent, SWT.RIGHT);
        lSMode.setLayoutData(gdLFill);
        lSMode.setText("S/Mode");
        cSupplyMode = new SYMCLOVCombo(parent, "S7_SUPPLY_MODE", SYMCLOVCombo.VIEW_VALUE, false);
        cSupplyMode.setLayoutData(gdCPref);
        Button bSModeApply = new Button(parent, SWT.PUSH);
        bSModeApply.setImage(registry.getImage("Apply_16.ICON"));
        bSModeApply.setLayoutData(gdApply);
        bSModeApply.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setBIProperties(COLUMN_SMODE, cSupplyMode.getText());
            }
        });
        
        //EBOM 개선과제 - SYSTEM CODE 추가
        Label lSCode = new Label(parent, SWT.RIGHT);
        lSCode.setLayoutData(gdLFill);
        lSCode.setText("SYSTEM CODE");
        cSystemCode = new SYMCLOVCombo(parent, "S7_SYSTEM_CODE", SYMCLOVCombo.VIEW_VALUE_DESC, false);
        cSystemCode.setLayoutData(gdCPref);
        Button bSCodeApply = new Button(parent, SWT.PUSH);
        bSCodeApply.setImage(registry.getImage("Apply_16.ICON"));
        bSCodeApply.setLayoutData(gdApply);
        bSCodeApply.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setBIProperties(COLUMN_SCODE, cSystemCode.getText());
            }
        });
        
        Label lAPart = new Label(parent, SWT.RIGHT);
        //GridData gdLFillT = new GridData(SWT.END, SWT.TOP, false, false);
        lAPart.setLayoutData(gdLFill);
        lAPart.setText("ALTER PART");
        cAlterPart = new SYMCLOVCombo(parent, "S7_ALTER_PART", SYMCLOVCombo.VIEW_VALUE_DESC, false);
        cAlterPart.setLayoutData(gdCPref);
        Button bAPartApply = new Button(parent, SWT.PUSH);
        bAPartApply.setImage(registry.getImage("Apply_16.ICON"));
        bAPartApply.setLayoutData(gdApply);
        bAPartApply.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setBIProperties(COLUMN_APART, cAlterPart.getText());
            }
        });

        Label lModuleCode = new Label(parent, SWT.RIGHT);
        lModuleCode.setLayoutData(gdLFill);
        lModuleCode.setText("Module Code");
        cModuleCode = new SYMCLOVCombo(parent, "S7_MODULE_CODE", SYMCLOVCombo.VIEW_VALUE, false);
        cModuleCode.setLayoutData(gdCPref);
        Button bMCodeApply = new Button(parent, SWT.PUSH);
        bMCodeApply.setImage(registry.getImage("Apply_16.ICON"));
        bMCodeApply.setLayoutData(gdApply);
        bMCodeApply.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setBIProperties(COLUMN_MCODE, cModuleCode.getText());
            }
        });
        
        GridData dsPosL = new GridData(SWT.END, SWT.TOP, false, false);
        dsPosL.verticalSpan = 2;
        gdLFill.widthHint = 80;
        GridData gdPosC = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdPosC.verticalSpan = 2;
        gdPosC.widthHint = 180;
        gdPosC.heightHint = 45;
        Label lPosDesc = new Label(parent, SWT.RIGHT);
        lPosDesc.setLayoutData(dsPosL);
        lPosDesc.setText("Position Desc");
        tPosDesc = new Text(parent, SWT.BORDER | SWT.MULTI| SWT.WRAP | SWT.V_SCROLL);
        tPosDesc.setEnabled(modifiable);
        tPosDesc.setLayoutData(gdPosC);
        bAPartApply = new Button(parent, SWT.PUSH);
        bAPartApply.setImage(registry.getImage("Apply_16.ICON"));
        bAPartApply.setLayoutData(gdApply);
        bAPartApply.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setBIProperties(COLUMN_PDESC, tPosDesc.getText());
            }
        });
        
    }
    
    /**
     * BOM Properties table 에 BOMLine attribute 설정
     * @param col
     * @param value
     */
    private void setBIProperties(int col, String value) {
        int[] selRows = table.getSelectionIndices();
        for(int r : selRows) {
            BOMLineItem item = (BOMLineItem)table.getItem(r);
            if(col == COLUMN_SMODE) {
                item.setSMode(value);
            } else if(col == COLUMN_APART) {
                item.setAPart(value);
            } else if(col == COLUMN_MCODE) {
                item.setMCode(value);
            } else if(col == COLUMN_PDESC) {
                item.setPDesc(value);
            } else if(col == COLUMN_SCODE) { //EBOM 개선과제 - SYSTEM CODE 추가
                item.setSCode(value);
            }
        }
    }
    
    /**
     * BOM Properties table 에 ECO 번호 설정.
     * @param ecoRev
     */
    private void setECOProperties(TCComponentItemRevision ecoRev) {
        int[] selRows = table.getSelectionIndices();
        for(int r : selRows) {
            BOMLineItem item = (BOMLineItem)table.getItem(r);
            item.setECO(ecoRev);
        }
    }

    /**
     * ECO 검색
     * @param me
     */
    private void searchEcoBtnListener(MouseEvent me) {
        ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(getShell(), SWT.SINGLE);
        ecoSearchDialog.getShell().setText("ECO Search");
        ecoSearchDialog.setAllMaturityButtonsEnabled(false);
        ecoSearchDialog.setBInProcessSelect(false);
        ecoSearchDialog.setBCompleteSelect(false);
        ecoSearchDialog.open();
        TCComponentItemRevision[] ecos = ecoSearchDialog.getSelectctedECO();
        if(ecos != null && ecos.length > 0) {
            TCComponentItemRevision ecoIR = ecos[0];
            try {
                if(!ecoIR.getProperty("date_released").equals("")) {
                    MessageBox.post(getShell(), "Select Working ECO!", "BOM Save", MessageBox.INFORMATION);
                    return;
                }
                ecoRev = ecoIR;
                tEcoNo.setText(ecoRev.getProperty("item_id"));
            } catch (TCException ex) {
                ex.printStackTrace();
            }
        }
        /*BOMECOSelectDialog searchECODialog = new BOMECOSelectDialog(getShell());
        searchECODialog.setApplyButtonVisible(false);
        ecoRev = searchECODialog.getECO();
        if(ecoRev != null) {
            try {
                tEcoNo.setText(ecoRev.getProperty("item_id"));
            } catch (TCException e1) {
                e1.printStackTrace();
            }
        }*/
    }
    
    /**
     * Load BOMLine properties
     */
    private void loadBOMLineProperties() {
        Job job = new Job("") {
            @Override
            protected IStatus run(IProgressMonitor p) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        try {
                            for(TCComponentBOMLine target : targets) {
                                new BOMLineItem(target);
                            }
                            cSupplyMode.setText(targets[0].getProperty("S7_SUPPLY_MODE"));
                            //EBOM 개선과제 - SYSTEM CODE 추가
                            cSystemCode.setText(targets[0].getProperty("S7_SYSTEM_CODE"));
                            cAlterPart.setText(targets[0].getProperty("S7_ALTER_PART"));
                            cModuleCode.setText(targets[0].getProperty("S7_MODULE_CODE"));
                        } catch (TCException e) {
                            e.printStackTrace();
                        }
                        table.redraw();
                    }
                });
                return Status.OK_STATUS;
            }
            
        };
        job.schedule();
        
    }

    @Override
    protected boolean validationCheck() {
        return true;
    }

    /**
     * Save BOMLine
     */
    @Override
    protected boolean apply() {
        SYMCBOMWindow bomWindow = null;
        try {
            bomWindow = (SYMCBOMWindow)targets[0].window();
            bomWindow.skipHistory(true);
            for(int i = 0 ; i < table.getItemCount() ; i++) {
                BOMLineItem bomItem = (BOMLineItem)table.getItem(i);
                bomItem.apply();
            }
            bomWindow.save();
            //bomWindow.refresh();
            return true;
        } catch(TCException e) {
            MessageBox.post(getShell(), "Fail to modify BOMLine", "BOMLine Edit", MessageBox.ERROR);
            e.printStackTrace();
        } finally {
            bomWindow.skipHistory(false);
        }
        return false;
    }
    
    /**
     * BOMLine attribute manage Table Item
     * @author Administrator
     *
     */
    private class BOMLineItem extends TableItem {
        
        /** Target BOMLine */
        private TCComponentBOMLine bomLine;
        
        /** Part Master modifiable flag */
        private boolean ecoModifiable;
        /** Part ECO Item Revision */
        private TCComponentItemRevision ecoRev;
        /** Part ECO No */
        private String ecoNo;
        /** BOMLine supply mode */
        private String sMode;
        //EBOM 개선과제 - SYSTEM CODE 추가
        /** BOMLine system code */
        private String sCode;
        /** BOMLine alter part */
        private String aPart;
        /** BOMLine module code */
        private String mCode;
        /** BOMLine position description */
        private String pDesc;
        
        private BOMLineItem(TCComponentBOMLine bomLine) {
            super(table, SWT.None);
            this.bomLine = bomLine;
            loadProperties();
        }
        
        /**
         * Load bom properties
         */
        private void loadProperties() {
            try {
                String[] parentProperties = bomLine.parent().getProperties(new String[]{"bl_item_item_id", "bl_rev_item_revision_id"});
                setText(1, parentProperties[0]);
                setText(2, parentProperties[1]);
                //EBOM 개선과제 - SYSTEM CODE 추가
                String[] childProperties = bomLine.getProperties(new String[]{"bl_level_starting_0", "bl_sequence_no"
                        , "bl_item_item_id", "bl_rev_item_revision_id", "bl_rev_object_name"
                        , "bl_quantity", "s7_ECO_NO", "S7_SUPPLY_MODE", "S7_SYSTEM_CODE", "S7_ALTER_PART", "S7_MODULE_CODE", "S7_POSITION_DESC"});
                setText(0, childProperties[0]);
                setText(3, childProperties[1]);
                setText(4, childProperties[2]);
                setText(5, childProperties[3]);
                setText(6, childProperties[4]);
                String quantity = childProperties[5];
                quantity = quantity.equals("") ? "1" : quantity;
                setText(7, quantity);
                ecoNo = childProperties[6];
                setText(COLUMN_ECO, ecoNo);
                TCComponentItemRevision lineRev = bomLine.getItemRevision();
                if(lineRev.okToModify() && lineRev.getProperty("s7_STAGE").equals("P")) {
                    ecoModifiable = true;
                    ecoRev = (TCComponentItemRevision)lineRev.getReferenceProperty("s7_ECO_NO");
                } else {
                    ecoModifiable = false;
                    setBackground(COLUMN_ECO, display.getSystemColor(SWT.COLOR_GRAY));
                }
                sMode = childProperties[7];
                setText(COLUMN_SMODE, sMode);
                //EBOM 개선과제 - SYSTEM CODE 추가
                sCode = childProperties[8];
                setText(COLUMN_SCODE, sCode);
                aPart = childProperties[9];
                setText(COLUMN_APART, aPart);
                mCode = childProperties[10];
                setText(COLUMN_MCODE, mCode);
                pDesc = childProperties[11];
                setText(COLUMN_PDESC, pDesc);
            } catch (TCException e) {
                e.printStackTrace();
            }
        }
        
        /**
         * ECO No 변경 여부 반환
         * @return
         */
        private boolean isECOModified() {
            return !this.ecoNo.equals(getText(COLUMN_ECO));
        }
        
        /**
         * Part Master 의 ECO 번호 설정
         * @param ecoRev
         */
        private void setECO(TCComponentItemRevision ecoRev) {
            if(!ecoModifiable) {
                return;
            }
            this.ecoRev = ecoRev;
            if(ecoRev == null) {
                setText(COLUMN_ECO, "");
            } else {
                try {
                    setText(COLUMN_ECO, ecoRev.getProperty("item_id"));
                } catch (TCException e) {
                    e.printStackTrace();
                }
            }
            if(isECOModified()) {
                setBackground(COLUMN_ECO, modifiedColor);
            } else {
                setBackground(COLUMN_ECO, table.getBackground());
            }
            
        }
        
        /**
         * Supply Mode 변경 여부 반환
         * @return
         */
        private boolean isSModeModified() {
            return !this.sMode.equals(getText(COLUMN_SMODE));
        }
        
        /**
         * Supply Mode set.
         * @param sMode
         */
        private void setSMode(String sMode) {
            setText(COLUMN_SMODE, sMode);
            if(isSModeModified()) {
                setBackground(COLUMN_SMODE, modifiedColor);
            } else {
                setBackground(COLUMN_SMODE, table.getBackground());
            }
        }
        
        //EBOM 개선과제 - SYSTEM CODE 추가
        /**
         * System Code 변경 여부 반환
         * @return
         */
        private boolean isSCodeModified() {
            return !this.sCode.equals(getText(COLUMN_SCODE));
        }
        
        /**
         * System Code set.
         * @param sCode
         */
        private void setSCode(String sCode) {
            setText(COLUMN_SCODE, sCode);
            if(isSCodeModified()) {
                setBackground(COLUMN_SCODE, modifiedColor);
            } else {
                setBackground(COLUMN_SCODE, table.getBackground());
            }
        }
        

        /**
         * Alter Part 변경 여부 반환
         * @return
         */
        private boolean isAPartModified() {
            return !this.aPart.equals(getText(COLUMN_APART));
        }
        
        /**
         * Alter Part 설정.
         * @param aPart
         */
        private void setAPart(String aPart) {
            setText(COLUMN_APART, aPart);
            if(isAPartModified()) {
                setBackground(COLUMN_APART, modifiedColor);
            } else {
                setBackground(COLUMN_APART, table.getBackground());
            }
        }

        /**
         * Module Code 변경 여부 반환.
         * @return
         */
        private boolean isMCodeModified() {
            return !this.mCode.equals(getText(COLUMN_MCODE));
        }
        
        /**
         * Module Code 설정.
         * @param mCode
         */
        private void setMCode(String mCode) {
            setText(COLUMN_MCODE, mCode);
            if(isMCodeModified()) {
                setBackground(COLUMN_MCODE, modifiedColor);
            } else {
                setBackground(COLUMN_MCODE, table.getBackground());
            }
        }
       
        /**
         * Position Description 변경 여부 반환
         * @return
         */
        private boolean isPDescModified() {
            return !this.pDesc.equals(getText(COLUMN_PDESC));
        }
        
        /**
         * Position Description 설정.
         * @param positionDesc
         */
        private void setPDesc(String positionDesc) {
            setText(COLUMN_PDESC, positionDesc);
            if(isMCodeModified()) {
               setBackground(COLUMN_PDESC, modifiedColor);
            } else {
               setBackground(COLUMN_PDESC, table.getBackground());
            }
        }
      
        /*private void setBIProperty(int column, String value) {
           switch(column) {
           case COLUMN_SMODE :
               setSMode(value);
               break;
           case COLUMN_APART :
               setAPart(value);
               break;
           case COLUMN_MCODE :
               setMCode(value);
               break;
           }
        }*/
       
        /**
         * BOMLine properties 저장
         * @throws TCException
         */
        private void apply() throws TCException {
            if(isECOModified()) {
                bomLine.getItemRevision().setReferenceProperty("s7_ECO_NO", ecoRev);
                ecoNo = getText(COLUMN_ECO);
                ecoRev = null;
                setBackground(COLUMN_ECO, table.getBackground());
            }
            if(isSModeModified()) {
                bomLine.setProperty("S7_SUPPLY_MODE", getText(COLUMN_SMODE));
                sMode = getText(COLUMN_SMODE);
                setBackground(COLUMN_SMODE, table.getBackground());
            }
            //EBOM 개선과제 - SYSTEM CODE 추가
            if(isSCodeModified()) {
                bomLine.setProperty("S7_SYSTEM_CODE", getText(COLUMN_SCODE));
                sCode = getText(COLUMN_SCODE);
                setBackground(COLUMN_SCODE, table.getBackground());
            }
            if(isAPartModified()) {
                bomLine.setProperty("S7_ALTER_PART", getText(COLUMN_APART));
                aPart = getText(COLUMN_APART);
                setBackground(COLUMN_APART, table.getBackground());
            }
            if(isMCodeModified()) {
                bomLine.setProperty("S7_MODULE_CODE", getText(COLUMN_MCODE));
                mCode = getText(COLUMN_MCODE);
                setBackground(COLUMN_MCODE, table.getBackground());
            }
            if(isPDescModified()) {
                bomLine.setProperty("S7_POSITION_DESC", getText(COLUMN_PDESC));
                pDesc = getText(COLUMN_PDESC);
                setBackground(COLUMN_PDESC, table.getBackground());
            }
            bomLine.refresh();
        }

        protected void checkSubclass() {
        }

    }
    
    /*
    private void addCellEditEvent(MouseEvent event) {
        final TableEditor editor = new TableEditor(table);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumHeight = 12;
        Point pt = new Point(event.x, event.y);
        int row = table.getSelectionIndex();
        final BOMLineItem item = (BOMLineItem)table.getItem(row);
        for (int i = COLUMN_ECO ; i < table.getColumnCount(); i++) {
            Rectangle rect = item.getBounds(i);
            if (rect.contains(pt)) {
                final int column = i;
                final SYMCLOVCombo cellEditor = getCellEditor(item, column);
                if (cellEditor == null) {
                    return;
                }
                Listener componentListener = new Listener() {
                    public void handleEvent(final Event e) {
                        switch (e.type) {
                        case SWT.FocusOut:
                            setBIProperties(column, cellEditor.getText());
                            cellEditor.dispose();
                            break;
                        case SWT.Selection:
                            setBIProperties(column, cellEditor.getText());
                            break;
                        case SWT.Traverse:
                            switch (e.detail) {
                            case SWT.TRAVERSE_RETURN:
                                setBIProperties(column, cellEditor.getText());
                            case SWT.TRAVERSE_ESCAPE:
                                cellEditor.dispose();
                                e.doit = false;
                            }
                            break;
                        }
                    }
                    
                    public void setBIProperties(int column, String value) {
                        int[] selRows = table.getSelectionIndices();
                        for(int r : selRows) {
                            BOMLineItem item = (BOMLineItem)table.getItem(r);
                            item.setBIProperty(column, value);
                        }
                    }
                };
                cellEditor.addListener(SWT.FocusOut, componentListener);
                cellEditor.addListener(SWT.Traverse, componentListener);
                editor.setEditor(cellEditor, item, column);
                cellEditor.setText(item.getText(column));
                cellEditor.setFocus();
            }
        }
    }
    
    private SYMCLOVCombo getCellEditor(BOMLineItem item, int colunIndex) {
        SYMCLOVCombo combo = null;
        switch (colunIndex) {
        case COLUMN_SMODE:
            combo = new SYMCLOVCombo(table, "S7_SUPPLY_MODE", false, false);
            break;
        case COLUMN_APART:
            combo = new SYMCLOVCombo(table, "S7_ALTER_PART", false, false);
            break;
        case COLUMN_MCODE:
            combo = new SYMCLOVCombo(table, "S7_MODULE_CODE", false, false);
            break;
        }
        return combo;
    }*/
    
    
}
