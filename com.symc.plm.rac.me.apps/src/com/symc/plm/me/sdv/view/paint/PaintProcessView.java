/**
 * 
 */
package com.symc.plm.me.sdv.view.paint;

import org.eclipse.swt.widgets.Composite;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : PaintProcessView
 * Class Description :
 * 
 * @date 2013. 12. 1.
 * 
 */
public class PaintProcessView extends AbstractSDVViewPane {
    private Label lblShopCode;
    private Label lblLineCode;
    private Label lblStationCode;
    private Label lblProductCode;
    private Label lblParallelStationNo;
    private Label lblStationKorName;
    private Label lblStationEngName;
    private SDVText txtShopCode;
    private SDVText txtLineCode;
    private SDVText txtProductCode;
    private SDVText txtStationKorName;
    private SDVText txtStationEngName;
//    private SDVLOVComboBox lovStationCode;
    private SDVText lovStationCode;
    private SDVLOVComboBox lovParallelStationNo;
    private String vehicleCode;

    private IDataMap curDataMap = null;

    public PaintProcessView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    public PaintProcessView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId, null);
    }

    public PaintProcessView(Composite parent, int style, String id, int configId, String order) {
        super(parent, style, id, DEFAULT_CONFIG_ID, order);
    }

    @Override
    protected void initUI(Composite parent) {
        Registry registry = Registry.getRegistry(this);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout(SWT.HORIZONTAL));

        Group group = new Group(composite, SWT.NONE);
        group.setText(registry.getString("StationInform.NAME"));
        GridLayout gl_group = new GridLayout(4, false);
        // gl_group.horizontalSpacing = 20;
        gl_group.horizontalSpacing = 10;
        gl_group.marginLeft = 10;
        group.setLayout(gl_group);

        lblShopCode = new Label(group, SWT.NONE);
        GridData gd_lblShopCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblShopCode.widthHint = 100;
        lblShopCode.setLayoutData(gd_lblShopCode);
        lblShopCode.setText(registry.getString("ShopCode.NAME"));

        txtShopCode = new SDVText(group, SWT.BORDER | SWT.READ_ONLY);
        // txtShopCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        GridData gd_txtShopCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtShopCode.widthHint = 30;
        txtShopCode.setLayoutData(gd_txtShopCode);

        lblLineCode = new Label(group, SWT.RIGHT);
        lblLineCode.setText(registry.getString("LineCode.NAME"));

        txtLineCode = new SDVText(group, SWT.BORDER | SWT.READ_ONLY);
        // txtLineCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        GridData gd_txtLineCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtLineCode.widthHint = 100;
        txtLineCode.setLayoutData(gd_txtLineCode);

        lblStationCode = new Label(group, SWT.NONE);
        lblStationCode.setText(registry.getString("StationCode.NAME"));

        lovStationCode = new SDVText(group, SWT.BORDER | SWT.SINGLE );

//        lovStationCode = new SDVLOVComboBox(group, "M7_BOPA_LINE_CODE");
        // lovStationCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        GridData gd_lovStationCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lovStationCode.widthHint = 200;
        lovStationCode.setLayoutData(gd_lovStationCode);

        lblProductCode = new Label(group, SWT.RIGHT);
        lblProductCode.setText(registry.getString("ProductCode.NAME"));

        txtProductCode = new SDVText(group, SWT.BORDER | SWT.READ_ONLY);
        // txtProductCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        GridData gd_txtProductCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtProductCode.widthHint = 100;
        txtProductCode.setLayoutData(gd_txtProductCode);

        lblParallelStationNo = new Label(group, SWT.NONE);
        lblParallelStationNo.setText(registry.getString("ParallelStationNo.NAME"));

        lovParallelStationNo = new SDVLOVComboBox(group, "M7_PARALLEL_LINE_VERSION");
        // lovParallelStationNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        GridData gd_lovParallelStationNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lovParallelStationNo.widthHint = 200;
        lovParallelStationNo.setLayoutData(gd_lovParallelStationNo);

        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);

        lblStationKorName = new Label(group, SWT.NONE);
        lblStationKorName.setText(registry.getString("StationKorName.NAME"));

        txtStationKorName = new SDVText(group, SWT.BORDER | SWT.SINGLE);
//        txtStationKorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        GridData gd_txtStationKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        gd_txtStationKorName.widthHint = 300;
        txtStationKorName.setLayoutData(gd_txtStationKorName);

        lblStationEngName = new Label(group, SWT.NONE);
        lblStationEngName.setText(registry.getString("StationEngName.NAME"));

        txtStationEngName = new SDVText(group, SWT.BORDER | SWT.SINGLE);
//        txtStationEngName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        GridData gd_txtStationEngName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        gd_txtStationEngName.widthHint = 300;
        txtStationEngName.setLayoutData(gd_txtStationEngName);
        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);

        /**
         * Mandatory 설정
         */
        lovStationCode.setTextLimit(2);
        lovStationCode.setInputType(SDVText.NUMERIC);
        
        txtShopCode.setMandatory(true);
        txtLineCode.setMandatory(true);
        txtProductCode.setMandatory(true);
        txtStationKorName.setMandatory(true);
        txtStationEngName.setMandatory(true);
        lovStationCode.setMandatory(true);
        lovParallelStationNo.setMandatory(true);
        
        lovParallelStationNo.setText("00");
                
        /**
         * Tab Next 이동 설정
         */
        lovStationCode.setEnableNextTab();
        txtStationKorName.setEnableNextTab();
        txtStationEngName.setEnableNextTab();
        lovParallelStationNo.setEnableNextTab();
    }

    /**
     * 
     * @method saveData
     * @date 2013. 12. 2.
     * @param
     * @return Object
     * @exception
     * @throws
     * @see
     */
    private IDataMap saveData() {

        RawDataMap savedDataMap = new RawDataMap();

        savedDataMap.put(SDVPropertyConstant.STATION_SHOP, txtShopCode.getText());
        savedDataMap.put(SDVPropertyConstant.STATION_LINE, txtLineCode.getText());
        
//        savedDataMap.put(SDVPropertyConstant.STATION_STATION_CODE, lovStationCode.getSelectedString());
        savedDataMap.put(SDVPropertyConstant.STATION_STATION_CODE, lovStationCode.getText());
        savedDataMap.put(SDVPropertyConstant.STATION_PRODUCT_CODE, txtProductCode.getText());
        savedDataMap.put(SDVPropertyConstant.STATION_PARALLEL_STATION_NO, lovParallelStationNo.getSelectedString());
        savedDataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, txtStationKorName.getText());
        savedDataMap.put(SDVPropertyConstant.STATION_ENG_NAME, txtStationEngName.getText());
        savedDataMap.put(SDVPropertyConstant.STATION_VEHICLE_CODE, vehicleCode);
        
        return savedDataMap;
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        curDataMap = saveData();
        return curDataMap;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        curDataMap = saveData();
        return curDataMap;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new AbstractSDVInitOperation() {

            public IDataMap getInitData() {
                IDataMap dataMap = new RawDataMap();

                try {
                    MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                    TCComponentBOMLine targetBOMLine = mfgApp.getSelectedBOMLines()[0];

                    dataMap.put("shopCode", targetBOMLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_SHOP), IData.STRING_FIELD);
                    dataMap.put("lineCode", targetBOMLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE), IData.STRING_FIELD);
                    dataMap.put("productCode", targetBOMLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_PRODUCT_CODE), IData.STRING_FIELD);
                    
                    vehicleCode = targetBOMLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_VEHICLE_CODE);
                } catch (TCException e) {
                    e.printStackTrace();
                }

                return dataMap;
            }

            @Override
            public void executeOperation() throws Exception {
                IDataSet dataset = new DataSet();
                dataset.addDataMap("processInform", getInitData());

                setData(dataset);
            }

        };
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset.containsMap("processInform")) {
                IDataMap dataMap = dataset.getDataMap("processInform");

                txtShopCode.setText(dataMap.getStringValue("shopCode"));
                txtShopCode.setEnabled(false);
                txtLineCode.setText(dataMap.getStringValue("lineCode"));
                txtLineCode.setEnabled(false);
                txtProductCode.setText(dataMap.getStringValue("productCode"));
                txtProductCode.setEnabled(false);

            }
        }
    }

    @Override
    public void uiLoadCompleted() {

    }

    @Override
    public Composite getRootContext() {
        return null;
    }

}
