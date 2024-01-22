/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly.temp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

/**
 * Class Name : MecoSelectView
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class SyncOptionsetVsBOMViewTemp extends AbstractSDVViewPane {
    private Text txtSrcProductNo;
    private Text txtTargetBomNo;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public SyncOptionsetVsBOMViewTemp(Composite parent, int style, String id) {
        super(parent, style, id);
        FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
        fillLayout.marginWidth = 5;
        fillLayout.marginHeight = 5;
        setLayout(fillLayout);

        Group grpSelectProduct = new Group(this, SWT.NONE);
        grpSelectProduct.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite composite = new Composite(grpSelectProduct, SWT.NONE);
        GridLayout gl_composite = new GridLayout(3, false);
        gl_composite.marginLeft = 10;
        composite.setLayout(gl_composite);

        Label lblSrcProductNo = new Label(composite, SWT.NONE);
        GridData gd_lblSrcProductNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblSrcProductNo.widthHint = 121;
        lblSrcProductNo.setLayoutData(gd_lblSrcProductNo);
        lblSrcProductNo.setText("Source Product No");

        txtSrcProductNo = new Text(composite, SWT.BORDER);
        GridData gd_txtSrcProductNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtSrcProductNo.widthHint = 160;
        txtSrcProductNo.setLayoutData(gd_txtSrcProductNo);

        Button btnSearch = new Button(composite, SWT.NONE);
        GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnSearch.widthHint = 60;
        gd_btnSearch.minimumWidth = 100;
        btnSearch.setLayoutData(gd_btnSearch);
        btnSearch.setText("Search");

        Label lblTargetBomNo = new Label(composite, SWT.NONE);
        lblTargetBomNo.setText("Target BOM No");

        txtTargetBomNo = new Text(composite, SWT.BORDER);
        txtTargetBomNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        new Label(composite, SWT.NONE);
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI() {
        

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {
        

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getRootContext()
     */
    @Override
    public Composite getRootContext() {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
     */
    @Override
    public void uiLoadCompleted() {
        
    }

}
