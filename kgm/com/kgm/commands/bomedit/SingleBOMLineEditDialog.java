package com.kgm.commands.bomedit;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kgm.commands.ec.search.ECOSearchDialog;
import com.kgm.common.SYMCLOVCombo;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.SWTUtilities;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentOccEffectivityType;
import com.teamcenter.rac.kernel.TCComponentUnitOfMeasure;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class SingleBOMLineEditDialog extends SYMCAbstractDialog {
	
    /** Edit Target bomLine */
	private TCComponentBOMLine target;
	/** Dialog display */
	private Display display;
	/** BOMLine edit flag */
	private boolean modifiableBI;
	/** Part Mast edit flag */
	private boolean modifiableECO;
	
	/** parent part no Text */
	private Text tParentNo;
	/** parent part revision Text */
	private Text tParentRev;
	/** child part no Text */
	private Text tPartNo;
	/** child part revision Text */
	private Text tPartRev;
	/** child part name Text */
	private Text tPartName;
	/** child part ECO no Text */
	private Text tEcoNo;
	/** Occurrence IN ECO no Text(dba use) */
	private Text tOccEcoNo;
	/** Child ECO change target */
	private TCComponentItemRevision ecoRev;
	/** BOM quantity Text */
	private Text tQty;
	/** BOM find no Text */
	private Text tFindNo;
	/** BOM supply mode Combo */
	private SYMCLOVCombo cSupplyMode;
	//EBOM 개선과제 - SYSTEM CODE 추가
	/** BOM system code Combo */
	private SYMCLOVCombo cSystemCode;
	/** BOM alter part Combo */
	private SYMCLOVCombo cAlterPart;
	/** BOM module code Combo */
	private SYMCLOVCombo cModuleCode;
	/** BOM position description Text */
	private Text tPosDesc;

	public SingleBOMLineEditDialog(Shell shell, TCComponentBOMLine target) {
		super(shell);
		display = shell.getDisplay();
		this.target = target;
	}
	
    /**
     *  initialize control
     */
	@Override
    protected Composite createDialogPanel(ScrolledComposite parent) {
        setApplyButtonVisible(false);
        boolean isUpperBOM = false;
        try {
            modifiableBI = target.parent().getBOMViewRevision().okToModify();
            String parentType = target.parent().getProperty("bl_item_object_type");
            isUpperBOM = parentType.equals("Product") || parentType.equals("Variant") || parentType.equals("Function");
            modifiableECO = target.getItemRevision().okToModify() && target.getItemRevision().getProperty("s7_STAGE").equals("P");
            if(!modifiableBI && !modifiableECO) {
                setOKButtonVisible(false);
            }
        } catch (TCException e1) {
            e1.printStackTrace();
        }
        getShell().setText("Edit Single BOMLine" + (modifiableBI ? "" : "(Read Only)"));
        getShell().setMinimumSize(312, 390);
        SWTUtilities.skipESCKeyEvent(getShell());
        
        Composite main = new Composite(parent, SWT.None);
        main.setLayout(new GridLayout(2, false));
        //parent.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridData gdLFill = new GridData(SWT.END, SWT.TOP, false, false);
        gdLFill.widthHint = 80;
        
        GridData gdSprator = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdSprator.horizontalSpan = 2;
        
        GridData gdCFillH = new GridData(SWT.FILL, SWT.FILL, true, false);
        GridData gdCPref = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdCPref.widthHint = 173;
        
        Label lPPNo = new Label(main, SWT.RIGHT);
        lPPNo.setText("Parent No");
        lPPNo.setLayoutData(gdLFill);
        Composite pParent = new Composite(main, SWT.None);
        pParent.setLayoutData(gdCPref);
        GridLayout pParentLayout = new GridLayout(3, false);
        pParentLayout.marginHeight = 0;
        pParentLayout.marginWidth = 0;  
        pParent.setLayout(pParentLayout);
        tParentNo = new Text(pParent, SWT.BORDER | SWT.READ_ONLY);
        GridData gdPart = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdPart.widthHint = 100;
        tParentNo.setLayoutData(gdPart);
        Label lPPRev = new Label(pParent, SWT.RIGHT);
        lPPRev.setText("Rev");
        gdPart = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        gdPart.widthHint = 30;
        lPPRev.setLayoutData(gdPart);
        tParentRev = new Text(pParent, SWT.BORDER | SWT.READ_ONLY);
        gdPart = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdPart.widthHint = 41;
        tParentRev.setLayoutData(gdPart);
        
        Label lPNo = new Label(main, SWT.RIGHT);
        lPNo.setText("Part No");
        lPNo.setLayoutData(gdLFill);
        Composite pPart = new Composite(main, SWT.None);
        pPart.setLayoutData(gdCPref);
        GridLayout pPartLayout = new GridLayout(3, false);
        pPartLayout.marginHeight = 0;
        pPartLayout.marginWidth = 0;  
        pPart.setLayout(pPartLayout);
        tPartNo = new Text(pPart, SWT.BORDER | SWT.READ_ONLY);
        gdPart = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdPart.widthHint = 100;
        tPartNo.setLayoutData(gdPart);
        Label lPRev = new Label(pPart, SWT.RIGHT);
        lPRev.setText("Rev");
        gdPart = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        gdPart.widthHint = 30;
        lPRev.setLayoutData(gdPart);
        tPartRev = new Text(pPart, SWT.BORDER | SWT.READ_ONLY);
        gdPart = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdPart.widthHint = 41;
        tPartRev.setLayoutData(gdPart);
        
        Label lPName = new Label(main, SWT.RIGHT);
        lPName.setLayoutData(gdLFill);
        lPName.setText("Part Name");
        tPartName = new Text(main, SWT.BORDER | SWT.READ_ONLY);
        tPartName.setLayoutData(gdCFillH);
        
        Label lSeparator = new Label(main, SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.setLayoutData(gdSprator);
        
        Label lEcoNo = new Label(main, SWT.RIGHT);
        lEcoNo.setLayoutData(gdLFill);
        lEcoNo.setText("ECO No");
        Composite pEcoNo = new Composite(main, SWT.None);
        GridLayout pEcoNoLayout = new GridLayout(2, false);
        pEcoNoLayout.marginHeight = 0;
        pEcoNoLayout.marginWidth = 0;        
        pEcoNo.setLayout(pEcoNoLayout);
        pEcoNo.setLayoutData(gdCPref);
        tEcoNo = new Text(pEcoNo, SWT.BORDER | SWT.READ_ONLY);
        GridData gdECO = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdECO.widthHint = 113;
        tEcoNo.setLayoutData(gdECO);
        Button bEcoNo = new Button(pEcoNo, SWT.CENTER);        
        bEcoNo.setText("Search...");
        gdECO = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdECO.widthHint = 72;
        gdECO.heightHint = 22;
        bEcoNo.setLayoutData(gdECO);
        
        Label lQty = new Label(main, SWT.RIGHT);
        lQty.setLayoutData(gdLFill);
        lQty.setText("Qty");
        tQty = new Text(main, SWT.BORDER | SWT.READ_ONLY);
        tQty.setLayoutData(gdCPref);
        
        Label lFNo = new Label(main, SWT.RIGHT);
        lFNo.setLayoutData(gdLFill);
        lFNo.setText("Find No");
        tFindNo = new Text(main, SWT.BORDER);
        tFindNo.setLayoutData(gdCPref);
        tFindNo.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                switch (e.keyCode) {
                case SWT.BS: // Backspace
                case SWT.DEL: // Delete
                case SWT.HOME: // Home
                case SWT.END: // End
                case SWT.ARROW_LEFT: // Left arrow
                case SWT.ARROW_RIGHT: // Right arrow
                    return;
                }
                // Control + V 금지
                if (e.keyCode == 118 && (e.stateMask & SWT.CTRL)!=0) {
                    e.doit = false;
                    return;
                }
                // 7자리 이상 금지
                if(tFindNo.getText().length() > 6) {
                    e.doit = false;
                    return;
                }
                // 숫자만 가능
                if (!Character.isDigit(e.character)) { // NUMERIC
                    if (e.keyCode == 0 || e.keyCode == 16777261)
                        return; // 0 붙여넣기
                    e.doit = false; // disallow the action
                    return;
                }
            }
        });

        Label lSMode = new Label(main, SWT.RIGHT);
        lSMode.setLayoutData(gdLFill);
        lSMode.setText("S/Mode");
        cSupplyMode = new SYMCLOVCombo(main, "S7_Supply_Mode", SYMCLOVCombo.VIEW_VALUE, false);
        cSupplyMode.setEnabled(modifiableBI && !isUpperBOM);
        cSupplyMode.setLayoutData(gdCPref);

        //EBOM 개선과제 - SYSTEM CODE 추가
        Label lSCode = new Label(main, SWT.RIGHT);
        lSCode.setLayoutData(gdLFill);
        lSCode.setText("SYSTEM CODE");
        cSystemCode = new SYMCLOVCombo(main, "S7_SYSTEM_CODE", SYMCLOVCombo.VIEW_VALUE_DESC, false);
        cSystemCode.setEnabled(modifiableBI && !isUpperBOM);
        cSystemCode.setLayoutData(gdCPref);
        
        
        Label lAPart = new Label(main, SWT.RIGHT);
        lAPart.setLayoutData(gdLFill);
        lAPart.setText("ALTER PART");
        cAlterPart = new SYMCLOVCombo(main, "S7_ALTER_PART", SYMCLOVCombo.VIEW_VALUE_DESC, false);
        cAlterPart.setLayoutData(gdCPref);
        cAlterPart.setEnabled(modifiableBI && !isUpperBOM);
        
        Label lModuleCode = new Label(main, SWT.RIGHT);
        lModuleCode.setLayoutData(gdLFill);
        lModuleCode.setText("Module Code");
        cModuleCode = new SYMCLOVCombo(main, "S7_MODULE_CODE", SYMCLOVCombo.VIEW_VALUE, false);
        cModuleCode.setLayoutData(gdCPref);
        cModuleCode.setEnabled(modifiableBI && !isUpperBOM);
        
        Label lPosDesc = new Label(main, SWT.RIGHT);
        lPosDesc.setLayoutData(gdLFill);
        lPosDesc.setText("Position Desc");
        tPosDesc = new Text(main, SWT.BORDER | SWT.MULTI| SWT.WRAP | SWT.V_SCROLL);
        tPosDesc.setEnabled(modifiableBI && !isUpperBOM);
        GridData gdTA = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        gdTA.widthHint = 173;
        gdTA.heightHint = 35;
        tPosDesc.setLayoutData(gdTA);
        
        try {
            if(target.getSession().isUserSystemAdmin()) {
                Label lOccECONo = new Label(main, SWT.RIGHT);
                lOccECONo.setText("In ECO");
                lOccECONo.setLayoutData(gdLFill);
                tOccEcoNo = new Text(main, SWT.BORDER);
                tOccEcoNo.setLayoutData(gdCFillH);
            }
        } catch (TCException e1) {
            e1.printStackTrace();
        }

        if(modifiableECO) {
            bEcoNo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDown(MouseEvent e) {
                    searchECOBtnListener(e);
                }
            });
        } else {
            bEcoNo.setEnabled(false);
        }

        loadBOMLineProperties();
        return main;
    }
    
	/**
	 * load target BOMLine properties
	 */
    private void loadBOMLineProperties() {
        Job job = new Job("") {
            @Override
            protected IStatus run(IProgressMonitor arg0) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        try {
                            tParentNo.setText(target.parent().getProperty("bl_item_item_id"));
                            tParentRev.setText(target.parent().getProperty("bl_rev_item_revision_id"));
                            tPartNo.setText(target.getProperty("bl_item_item_id"));
                            tPartRev.setText(target.getProperty("bl_rev_item_revision_id"));
                            tPartName.setText(target.getProperty("bl_rev_object_name"));
                            tEcoNo.setText(target.getProperty("s7_ECO_NO"));
                            
                            String quantity = target.getProperty("bl_quantity");
                            tQty.setText(quantity.equals("") ? "1" : quantity);
                            TCComponentUnitOfMeasure uom = (TCComponentUnitOfMeasure)target.getReferenceProperty("bl_uom");
                            /**
                             * [SR141006-017][jclee][20141013] 부자재 수량 변경 가능하도록 수정. EA가 아닌 항목에 대해서만 수정 가능.
                             */
//                            if(uom == null || uom.getProperty("symbol").equals("EA")) {
//                                tQty.setEnabled(false);
//                            }
                            tQty.setEnabled(!(uom == null || uom.getProperty("symbol").equals("EA")));
                            tQty.setEditable(!(uom == null || uom.getProperty("symbol").equals("EA")));
                            
                            tFindNo.setText(target.getProperty("bl_sequence_no"));
                            cSupplyMode.setText(target.getProperty("S7_SUPPLY_MODE"));
                            //EBOM 개선과제 - SYSTEM CODE 추가
                            cSystemCode.setText(target.getProperty("S7_SYSTEM_CODE"));
                            cAlterPart.setText(target.getProperty("S7_ALTER_PART"));
                            cModuleCode.setText(target.getProperty("S7_MODULE_CODE"));
                            tPosDesc.setText(target.getProperty("S7_POSITION_DESC"));
                        } catch (TCException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return Status.OK_STATUS;
            }
            
        };
        job.schedule();
        
    }

    /**
     * ECO Search
     * @param me
     */
    private void searchECOBtnListener(MouseEvent me) {
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

    @Override
    protected boolean validationCheck() {
        return true;
    }

    @Override
    protected boolean apply() {
        try {
            if(modifiableECO && ecoRev != null && !tEcoNo.getText().equals(target.getProperty("s7_ECO_NO"))) {
                target.getItemRevision().setReferenceProperty("s7_ECO_NO", ecoRev);
                target.refresh();
            }
            if(modifiableBI) {
                DecimalFormat fnFormat = new DecimalFormat("000000");
                String findNo = fnFormat.format(Integer.parseInt(tFindNo.getText()));
                boolean isPacked = target.isPacked();
                ArrayList<String> propNames = new ArrayList<String>();
                ArrayList<String> propValues = new ArrayList<String>();
                if(!target.getProperty("bl_sequence_no").equals(findNo)) {
                    propNames.add("bl_sequence_no");
                    propValues.add(findNo);
                }
                if(!target.getProperty("S7_SUPPLY_MODE").equals(cSupplyMode.getText())) {
                    propNames.add("S7_SUPPLY_MODE");
                    propValues.add(cSupplyMode.getText());
                }
                //EBOM 개선과제 - SYSTEM CODE 추가
                if(!target.getProperty("S7_SYSTEM_CODE").equals(cSystemCode.getText())) {
                    propNames.add("S7_SYSTEM_CODE");
                    propValues.add(cSystemCode.getText());
                }
                if(!target.getProperty("S7_ALTER_PART").equals(cAlterPart.getText())) {
                    propNames.add("S7_ALTER_PART");
                    propValues.add(cAlterPart.getText());
                }
                if(!target.getProperty("S7_MODULE_CODE").equals(cModuleCode.getText())) {
                    propNames.add("S7_MODULE_CODE");
                    propValues.add(cModuleCode.getText());
                }
                if(!target.getProperty("S7_POSITION_DESC").equals(tPosDesc.getText())) {
                    propNames.add("S7_POSITION_DESC");
                    propValues.add(tPosDesc.getText());
                }
                /**
                 * [SR141006-017][jclee][20141013] 부자재 수량 변경 가능하도록 수정. EA가 아닌 항목에 대해서만 수정 가능.
                 */
                if(!target.getProperty("bl_quantity").equals(tQty.getText())) {
                	propNames.add("bl_quantity");
                	propValues.add(tQty.getText());
                }
                if(tOccEcoNo != null && !tOccEcoNo.getText().equals("")) {
                    String inECONo = tOccEcoNo.getText();//"35AD101"
//                    TCClassService localTCClassService = target.getSession().getClassService();
//                    TCComponent[] effComp = localTCClassService.findByClass("CFM_date_info", "id", inECONo);
//                    if ((effComp == null) || (effComp.length == 0)) {
//                        MessageBox.post(getShell(), "ECO effectivity not found!", "BOMLine Edit", MessageBox.ERROR);
//                    } else {
//                        target.setStringProperty("bl_has_date_effectivity", inECONo);
//                    }

                    TCComponentOccEffectivityType effType = (TCComponentOccEffectivityType) target.getSession().getTypeComponent("CFM_date_info");
                    TCComponent findEffComponent = null;
                    try
                    {
                    	findEffComponent = effType.findEffectivity(inECONo);
                    }
                    catch (TCException ex)
                    {
                    	if (ex.errorCodes[0] == 710073)
                    		findEffComponent = null;
                    }
                    if (findEffComponent == null)
                    {
                    	MessageBox.post(getShell(), "ECO effectivity not found!", "BOMLine Edit", MessageBox.ERROR);
	                } else {
	                    target.setStringProperty("bl_has_date_effectivity", inECONo);
	                }
                }
                if(propNames.size() == 0) {
                    return true;
                }
                if(isPacked) {
                    TCComponentBOMLine[] packedLines = target.getPackedLines();
                    for(TCComponentBOMLine bomLine : packedLines) {
                        bomLine.setProperties(propNames.toArray(new String[propNames.size()]), propValues.toArray(new String[propValues.size()]));
                    }
                }
            	target.unpack();
                target.setProperties(propNames.toArray(new String[propNames.size()]), propValues.toArray(new String[propValues.size()]));
                if(isPacked) {
                    target.pack();
                }
            }
            return true;
        } catch(TCException e) {
            MessageBox.post(getShell(), "Fail to modify BOMLine", "BOMLine Edit", MessageBox.ERROR);
            e.printStackTrace();
        }
        return false;
    }

}
