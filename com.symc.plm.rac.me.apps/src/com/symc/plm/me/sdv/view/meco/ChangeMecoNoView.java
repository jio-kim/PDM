/**
 * 
 */
package com.symc.plm.me.sdv.view.meco;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * Class Name : MecoChangeNoView
 * Class Description : MECO No를 변경하는 View
 * 
 * @date 2014. 1. 28.
 * 
 */
public class ChangeMecoNoView extends AbstractSDVViewPane {
    private Text txtMecoNo;
    private Table table;
    private ArrayList<TCComponentBOMLine> targetBOMList = null; // 선택된 Item Revision 리스트
    private TCComponentItemRevision newMecoRevision = null;

    public ChangeMecoNoView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    @Override
    protected void initUI(Composite parent) {

        Composite parentComposite = new Composite(parent, SWT.NONE);
        parentComposite.setLayout(new GridLayout(1, false));

        Composite composite = new Composite(parentComposite, SWT.NONE);
        GridLayout gl_composite = new GridLayout(4, false);
        composite.setLayout(gl_composite);

        GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        composite.setLayoutData(gd_composite);

        Label lblNewMecoNo = new Label(composite, SWT.NONE);
        GridData gd_lblNewMecoNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblNewMecoNo.widthHint = 105;
        lblNewMecoNo.setLayoutData(gd_lblNewMecoNo);
        lblNewMecoNo.setText("New MECO No.");

        txtMecoNo = new Text(composite, SWT.BORDER);
        GridData gd_txtMecoNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtMecoNo.widthHint = 160;
        txtMecoNo.setLayoutData(gd_txtMecoNo);

        Button btnSearch = new Button(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData.widthHint = 60;
        gridData.minimumWidth = 100;
        btnSearch.setLayoutData(gridData);
        btnSearch.setText("Search");
        new Label(composite, SWT.NONE);

        Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gdSprator = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdSprator.horizontalSpan = 4;
        lSeparator.setLayoutData(gdSprator);

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        table.setHeaderVisible(true);

        TableColumn tblclmnId = new TableColumn(table, SWT.NONE);
        tblclmnId.setWidth(150);
        tblclmnId.setText("ID");

        TableColumn tblclmnName = new TableColumn(table, SWT.NONE);
        tblclmnName.setWidth(200);
        tblclmnName.setText("Name");

        TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
        tblclmnNewColumn.setMoveable(true);
        tblclmnNewColumn.setWidth(95);
        tblclmnNewColumn.setText("Old MECO No.");

        btnSearch.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                IDialog dialog;
                try {
                    dialog = UIManager.getDialog(getShell(), "symc.me.bop.SearchMECODlg");
                    dialog.open();
                    IDataSet datSet = dialog.getSelectDataSetAll();
                    if (datSet == null)
                        return;
                    IDataMap mecoDataMap = datSet.getDataMap("mecoSearch");
                    if (mecoDataMap != null && mecoDataMap.containsKey("mecoNo")) {
                        txtMecoNo.setText(mecoDataMap.getStringValue("mecoNo"));
                        newMecoRevision = (TCComponentItemRevision) mecoDataMap.getValue("mecoRev");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
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
        return getLocalSelectDataMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        RawDataMap savedDataMap = new RawDataMap();
        savedDataMap.put(SDVPropertyConstant.ITEM_REV_MECO_NO, newMecoRevision, IData.OBJECT_FIELD);
        savedDataMap.put("TARGET_LIST", targetBOMList, IData.OBJECT_FIELD);
        return savedDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new InitOperation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_FAILED)
            return;
        if (dataset == null)
            return;

        try {
            IDataMap dataMap = dataset.getDataMap(this.getId());
            targetBOMList = new ArrayList<TCComponentBOMLine>();
            targetBOMList = (ArrayList<TCComponentBOMLine>) dataMap.getListValue("TARGET_LIST");
            for (TCComponentBOMLine targetBOMLine : targetBOMList) {
                TableItem rowItem = new TableItem(table, SWT.NONE);
                String itemId = targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                String objectName = targetBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
                TCComponent mecoComp = targetBOMLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
                rowItem.setText(0, itemId);
                rowItem.setText(1, objectName);
                rowItem.setText(2, mecoComp == null ? "" : mecoComp.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
     */
    @Override
    public void uiLoadCompleted() {

    }

    public class InitOperation extends AbstractSDVInitOperation {

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
         */
        @Override
        public void executeOperation() throws Exception {
            IDataMap displayDataMap = new RawDataMap();
            try {
                MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                TCComponentBOMLine[] selectedBOMLines = mfgApp.getSelectedBOMLines();
                ArrayList<TCComponentBOMLine> revList = new ArrayList<TCComponentBOMLine>();
                for (TCComponentBOMLine selectedBOMLine : selectedBOMLines) {
                    revList.add(selectedBOMLine);
                }
                displayDataMap.put("TARGET_LIST", revList, IData.LIST_FIELD);

                DataSet viewDataSet = new DataSet();
                viewDataSet.addDataMap(ChangeMecoNoView.this.getId(), displayDataMap);
                setData(viewDataSet);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
