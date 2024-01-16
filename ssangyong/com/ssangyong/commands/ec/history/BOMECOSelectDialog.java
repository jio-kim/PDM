package com.ssangyong.commands.ec.history;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.commands.ec.search.ECOSearchDialog;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SWTUtilities;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.ssangyong.rac.kernel.InterfaceSYMCECOSelect;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class BOMECOSelectDialog extends SYMCAbstractDialog implements InterfaceSYMCECOSelect {
    
    private Shell parent;
    
    private String ecoNo;
    private TCComponentItemRevision ecoRev;
    
    private Combo cECO;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public BOMECOSelectDialog(Shell parent) {
        super(parent, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
        this.parent = parent;
        setApplyButtonVisible(false);
        super.create();
    }
    
    public void create() {
    }
  
    /**
     * Open the dialog.
     * 
     * @return the result
     
    public Object open() {
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }*/

    /**
     * Create contents of the dialog.
     */
    @SuppressWarnings("unchecked")
    protected Composite createDialogPanel(ScrolledComposite scrolledComposite) {
        ecoNo = null;
        getShell().setMinimumSize(360, 115);
        getShell().setText("BOM SAVE");
        getShell().setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/save_16.png"));
        SWTUtilities.skipKeyEvent(getShell());
        Composite composite = new Composite(scrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        Label lId = new Label(composite, SWT.RIGHT);
        lId.setText("ECO No");
        GridData gdCombo = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gdCombo.widthHint = 180;
        cECO = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        cECO.setLayoutData(gdCombo);
        Button searchButton = new Button(composite, SWT.NONE);
        searchButton.setText("Search");
        searchButton.setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
        searchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                searchAction();
            }
        });

        GridData gdSprator = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdSprator.horizontalSpan = 3;
        //Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        //lSeparator.setLayoutData(gdSprator);
        SYMDisplayUtil.centerToParent(parent, getShell());
        
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        try{
            TCSession session = CustomUtil.getTCSession();
            DataSet ds = new DataSet();
            ds.put("user_id", session.getUser().getUserId());
            ArrayList<String> ecoRevUids = (ArrayList<String>)remote.execute("com.ssangyong.service.ECOHistoryService", "selectUserWorkingECO", ds);
            if( ecoRevUids != null ){
                int inx = 0;
                for(String ecoRevUid : ecoRevUids){
                    TCComponent ecoRev = session.stringToComponent(ecoRevUid);
                    cECO.add(ecoRev.getProperty("item_id"));
                    cECO.setData(inx + "_no", ecoRev.getProperty("item_id"));
                    cECO.setData(inx + "_rev", ecoRev);
                    inx++;
                }
                if(inx > 0) {
                    cECO.select(0);
                }
            }
        }catch( Exception e){
            e.printStackTrace();
        }
        
        return composite;
    }

    private void searchAction() {
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
                cECO.add(ecoIR.toString());
                int index = cECO.getItemCount() - 1;
                cECO.select(index);
                cECO.setData(index + "_no", ecos[0].getProperty("item_id"));
                cECO.setData(index + "_rev", ecos[0]);
            } catch (TCException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected boolean validationCheck() {
        String text = cECO.getText();
        if(text == null || text.equals("")) {
            MessageBox.post(getShell(), "Select Working ECO. and try again!", "BOM Save", MessageBox.INFORMATION);
            return false;
        }
        return true;
    }

    @Override
    protected boolean apply() {
        int row = cECO.getSelectionIndex();
        ecoNo = (String)cECO.getData(row + "_no");
        ecoRev = (TCComponentItemRevision)cECO.getData(row + "_rev");
        return true;
    }

    @Override
    public String getECONo() {
        parent.getDisplay().syncExec(new Runnable() {
            public void run() {
                open();
            }
        });
        return ecoNo;
    }

    @Override
    public TCComponentItemRevision getECO() {
        parent.getDisplay().syncExec(new Runnable() {
            public void run() {
                open();
            }
        });
        return ecoRev;
    }
    
}