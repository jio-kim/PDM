/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 * 
 * Class Name : SyncOptionsetVsBOMView
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class SyncOptionsetVsBOMView extends AbstractSDVViewPane {
    private Text txtSrcProductNo;
    private Text txtTargetBomNo;
    private TCComponentItemRevision srcProductRevision = null;
    private Registry registry = null;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public SyncOptionsetVsBOMView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {

        registry = Registry.getRegistry(this);
        // FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
        // fillLayout.marginWidth = 5;
        // fillLayout.marginHeight = 5;
        // setLayout(fillLayout);

        Group grpSelectProduct = new Group(parent, SWT.NONE);
        // grpSelectProduct.setLayout(new FillLayout(SWT.HORIZONTAL));
        grpSelectProduct.setLayout(new GridLayout(1, false));

        Composite composite = new Composite(grpSelectProduct, SWT.NONE);
        GridLayout gl_composite = new GridLayout(3, false);
        gl_composite.marginLeft = 10;
        composite.setLayout(gl_composite);

        Label lblSrcProductNo = new Label(composite, SWT.NONE);
        GridData gd_lblSrcProductNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblSrcProductNo.widthHint = 121;
        lblSrcProductNo.setLayoutData(gd_lblSrcProductNo);
        lblSrcProductNo.setText(registry.getString("SrcProductNo.NAME"));

        txtSrcProductNo = new Text(composite, SWT.BORDER);
        GridData gd_txtSrcProductNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtSrcProductNo.widthHint = 160;
        txtSrcProductNo.setLayoutData(gd_txtSrcProductNo);

        Button btnSearch = new Button(composite, SWT.NONE);
        GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnSearch.widthHint = 60;
        gd_btnSearch.minimumWidth = 100;
        btnSearch.setLayoutData(gd_btnSearch);
        btnSearch.setText(registry.getString("Search.NAME"));

        Label lblTargetBomNo = new Label(composite, SWT.NONE);
        lblTargetBomNo.setText(registry.getString("TargetProductNo.NAME"));

        txtTargetBomNo = new Text(composite, SWT.BORDER);
        txtTargetBomNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        new Label(composite, SWT.NONE);

        btnSearch.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                IDialog dialog;
                try {
                    dialog = UIManager.getDialog(getShell(), "symc.me.bop.SearchProductItemDialog");
                    dialog.open();

                    IDataSet datSet = dialog.getSelectDataSetAll();
                    if (datSet == null)
                        return;
                    IDataMap dataMap = datSet.getDataMap("searchProduct");
                    if (dataMap != null && dataMap.containsKey("SRC_PRODUCT_REV")) {
                        srcProductRevision = (TCComponentItemRevision) dataMap.getValue("SRC_PRODUCT_REV");
                        txtSrcProductNo.setText(srcProductRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        txtTargetBomNo.setEnabled(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        RawDataMap inputDataMap = new RawDataMap();
        inputDataMap.put("TARGET_BOM_NO", txtTargetBomNo.getText());

        if (!txtSrcProductNo.getText().isEmpty()) {
            try {
                TCComponentItem findItem = SDVBOPUtilities.FindItem(txtSrcProductNo.getText(), "Item");
                if (findItem != null)
                    srcProductRevision = findItem.getLatestItemRevision();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        inputDataMap.put("SRC_PRODUCT_NO", txtSrcProductNo.getText());
        inputDataMap.put("SRC_PRODUCT_REV", srcProductRevision, IData.OBJECT_FIELD);

        return inputDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {

        return getLocalDataMap();
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

    private void loadInitData(IDataMap maps) {
        String targetBomNo = null;
        if (maps == null)
            return;
        try {
            if (!maps.containsKey("TARGET_BOM_NO"))
                return;

            targetBomNo = maps.getStringValue("TARGET_BOM_NO"); // Load된 BOM Revision
            txtTargetBomNo.setText(targetBomNo);

            if (!maps.containsKey("SRC_PRODUCT_REV"))
                return;

            srcProductRevision = (TCComponentItemRevision) maps.getValue("SRC_PRODUCT_REV");
            txtSrcProductNo.setText(maps.getStringValue("SRC_PRODUCT_NO"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_FAILED)
            return;
        if (dataset == null)
            return;
        IDataMap dataMap = dataset.getDataMap(this.getId());
        loadInitData(dataMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
     */
    @Override
    public void uiLoadCompleted() {

    }

    /**
     * 초기 Data Load Operation
     * Class Name : InitOperation
     * Class Description :
     * 
     * @date 2013. 12. 3.
     * 
     */
    public class InitOperation extends AbstractSDVInitOperation {
        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
         */
        @Override
        public void executeOperation() throws Exception {

            IDataMap displayDataMap = new RawDataMap();
            // MPPAppication
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            // 현재 BOM WINDOW
            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();
            TCComponentBOMLine targetTopBomLine = bomWindow.getTopBOMLine();
            // 연결된 MProduct가 있으면 MProduct를 가져옴
            // [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
            TCComponentItemRevision mproductRevision = SDVBOPUtilities.getConnectedMProductItemRevision(targetTopBomLine.getItemRevision());
            
            displayDataMap.put("TARGET_BOM_NO", targetTopBomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

            if (mproductRevision != null) {
                displayDataMap.put("SRC_PRODUCT_NO", mproductRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                displayDataMap.put("SRC_PRODUCT_REV", mproductRevision, IData.OBJECT_FIELD);
            }

            DataSet viewDataSet = new DataSet();
            viewDataSet.addDataMap(SyncOptionsetVsBOMView.this.getId(), displayDataMap);
            setData(viewDataSet);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    /**
     * 연결된 MProduct Revision을 가져옴
     * 
     * @method getConnectedMProductRevision
     * @date 2013. 12. 26.
     * @param
     * @return TCComponentItemRevision
     * @exception
     * @throws
     * @see
     */
    /* [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제로 더이상 사용 안함.
    private TCComponentItemRevision getConnectedMProductRevision(TCComponentItemRevision itemRevision) throws Exception {
        TCComponentItemRevision mProductRevision = null;
        TCComponent[] meTargetComps = itemRevision.getRelatedComponents("IMAN_METarget");
        for (TCComponent meTargetComp : meTargetComps) {
            String type = meTargetComp.getType();
            if (type.equals(SDVTypeConstant.BOP_MPRODUCT_REVISION))
                return (TCComponentItemRevision) meTargetComp;
        }
        return mProductRevision;
    }
     */
}
