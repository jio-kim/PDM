/**
 * 
 */
package com.symc.plm.me.sdv.view.paint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : PaintLineView
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 * @date 2013. 12. 2.
 * 
 */
public class PaintLineView extends AbstractSDVViewPane {
    private Label lblShopCode;
    private Label lblLineCode;
    private Label lblProductCode;
    private Label lblParallelStationNo;
    private Label lblLineKorName;
    private Label lblLineEngName;
    private Label lblJph;
    private Label lblAllowance;
    private SDVText txtLineKorName;
    private SDVText txtLineEngName;
    private SDVText txtShopCode;
    private SDVText txtProductCode;
    private SDVText txtJph;
    private SDVText txtAllowance;
    private SDVText txtFindNo;
    public String vehicle_code;
    private SDVLOVComboBox lovLine;
    private SDVLOVComboBox lovParallelStationNo;

    private TCSession tcSession = null;
    private IDataMap curDataMap = null;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public PaintLineView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    public PaintLineView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId, null);
    }

    public PaintLineView(Composite parent, int style, String id, int configId, String order) {
        super(parent, style, id, DEFAULT_CONFIG_ID, order);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        tcSession = SDVBOPUtilities.getTCSession();
        Registry registry = Registry.getRegistry(this);

        Composite composite0 = new Composite(parent, SWT.NONE);
        composite0.setLayout(new FillLayout(SWT.HORIZONTAL));

        Group group = new Group(composite0, SWT.NONE);
        group.setText(registry.getString("LineInform.NAME"));
        GridLayout gl_group = new GridLayout(2, false);
        gl_group.marginRight = 10;
        gl_group.marginLeft = 10;
        gl_group.verticalSpacing = 10;
        group.setLayout(gl_group);

        lblShopCode = new Label(group, SWT.NONE);
        lblShopCode.setText(registry.getString("ShopCode.NAME"));

        Composite composite = new Composite(group, SWT.NONE);
        GridLayout gl_composite = new GridLayout(2, false);
        gl_composite.marginWidth = 0;
        gl_composite.marginHeight = 0;
        gl_composite.horizontalSpacing = 20;
        gl_composite.verticalSpacing = 0;
        composite.setLayout(gl_composite);

        txtShopCode = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gb_txtShopCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gb_txtShopCode.widthHint = 100;
        txtShopCode.setLayoutData(gb_txtShopCode);
        txtShopCode.setBounds(0, 0, 73, 21);

        Composite composite_3 = new Composite(composite, SWT.NONE);
        // composite_3.setBounds(0, 0, 64, 64);
        composite_3.setLayout(new GridLayout(2, false));

        lblLineCode = new Label(composite_3, SWT.NONE);
        GridData gd_lblLineCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblLineCode.widthHint = 100;
        lblLineCode.setLayoutData(gd_lblLineCode);
        lblLineCode.setBounds(0, 0, 56, 15);
        lblLineCode.setText(registry.getString("LineCode.NAME"));

        lovLine = new SDVLOVComboBox(composite_3, SWT.BORDER, tcSession, "M7_BOPP_LINE_CODE");
        GridData gd_lovLine = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_lovLine.widthHint = 185;
        lovLine.setLayoutData(gd_lovLine);

        lblProductCode = new Label(group, SWT.NONE);
        lblProductCode.setText(registry.getString("ProductCode.NAME"));

        Composite composite_1 = new Composite(group, SWT.NONE);
        GridLayout gl_composite_1 = new GridLayout(2, false);
        gl_composite_1.horizontalSpacing = 20;
        gl_composite_1.marginWidth = 0;
        gl_composite_1.marginHeight = 0;
        composite_1.setLayout(gl_composite_1);

        txtProductCode = new SDVText(composite_1, SWT.BORDER | SWT.SINGLE);
        GridData gb_txtProductCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gb_txtProductCode.widthHint = 100;
        txtProductCode.setLayoutData(gb_txtProductCode);
        txtProductCode.setBounds(0, 0, 73, 21);

        Composite composite_5 = new Composite(composite_1, SWT.NONE);
        GridLayout gl_composite_5 = new GridLayout(2, false);
        composite_5.setLayout(gl_composite_5);

        lblParallelStationNo = new Label(composite_5, SWT.NONE);
        GridData gd_lblParallelStationNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblParallelStationNo.widthHint = 100;
        lblParallelStationNo.setLayoutData(gd_lblParallelStationNo);
        lblParallelStationNo.setBounds(0, 0, 56, 15);
        lblParallelStationNo.setText(registry.getString("ParallelStationNo.NAME"));

        lovParallelStationNo = new SDVLOVComboBox(composite_5, SWT.BORDER, tcSession, "M7_PARALLEL_LINE_VERSION");
        GridData gd_lovParallelStationNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lovParallelStationNo.widthHint = 50;
        lovParallelStationNo.setLayoutData(gd_lovParallelStationNo);
        lovParallelStationNo.setBounds(0, 0, 88, 23);

        lblLineKorName = new Label(group, SWT.NONE);
        lblLineKorName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        lblLineKorName.setText(registry.getString("LineKorName.NAME"));

        txtLineKorName = new SDVText(group, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtLineKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtLineKorName.widthHint = 420;
        txtLineKorName.setLayoutData(gd_txtLineKorName);

        lblLineEngName = new Label(group, SWT.NONE);
        lblLineEngName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblLineEngName.setText(registry.getString("LineEngName.NAME"));

        txtLineEngName = new SDVText(group, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtLineEngName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtLineEngName.widthHint = 420;
        txtLineEngName.setLayoutData(gd_txtLineEngName);

        lblJph = new Label(group, SWT.NONE);
        lblJph.setText(registry.getString("JPH.NAME"));

        Composite composite_2 = new Composite(group, SWT.NONE);
        GridLayout gl_composite_2 = new GridLayout(3, false);
        gl_composite_2.horizontalSpacing = 30;
        gl_composite_2.marginHeight = 0;
        gl_composite_2.marginWidth = 0;
        composite_2.setLayout(gl_composite_2);

        txtJph = new SDVText(composite_2, SWT.BORDER | SWT.SINGLE);
        GridData gb_txtJph = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gb_txtJph.widthHint = 100;
        txtProductCode.setLayoutData(gb_txtJph);

        lblAllowance = new Label(composite_2, SWT.NONE);
        lblAllowance.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblAllowance.setText(registry.getString("Allowance.NAME"));

        txtAllowance = new SDVText(composite_2, SWT.BORDER | SWT.SINGLE);
        GridData gb_txtAllowance = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gb_txtAllowance.widthHint = 100;
        txtProductCode.setLayoutData(gb_txtAllowance);

        Label lblFindNo = new Label(group, SWT.NONE);
        lblFindNo.setText("Find No");

        txtFindNo = new SDVText(group, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtFindNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtFindNo.widthHint = 65;
        txtFindNo.setLayoutData(gd_txtFindNo);

        /**
         * Mandatory 설정
         */
        txtShopCode.setMandatory(true);
        lovLine.setMandatory(true);
        txtProductCode.setMandatory(true);
        lovParallelStationNo.setMandatory(true);
        txtLineKorName.setMandatory(true);
        txtLineEngName.setMandatory(true);

        /**
         * 자릿수 지정
         * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
         */
        // txtJph.setTextLimit(2);
        txtJph.setTextLimit(5);
        txtFindNo.setTextLimit(3);

        /**
         * 유형 지정
         * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
         */
        // txtJph.setInputType(SDVText.NUMERIC);
        txtJph.setInputType(SDVText.DOUBLE);
        txtAllowance.setInputType(SDVText.DOUBLE);

        txtFindNo.setInputType(SDVText.NUMERIC);

        lovParallelStationNo.setText("00");
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

        savedDataMap.put(SDVPropertyConstant.LINE_REV_SHOP_CODE, txtShopCode.getText());
        savedDataMap.put(SDVPropertyConstant.LINE_REV_CODE, lovLine.getSelectedString());
        savedDataMap.put(SDVPropertyConstant.LINE_REV_PRODUCT_CODE, txtProductCode.getText());
        savedDataMap.put(SDVPropertyConstant.LINE_PARALLEL_LINE_NO, lovParallelStationNo.getSelectedString());
        savedDataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, txtLineKorName.getText());
        savedDataMap.put(SDVPropertyConstant.LINE_REV_ENG_NAME, txtLineEngName.getText());
        savedDataMap.put(SDVPropertyConstant.LINE_REV_JPH, txtJph.getText());
        savedDataMap.put(SDVPropertyConstant.LINE_REV_ALLOWANCE, txtAllowance.getText());
        savedDataMap.put(SDVPropertyConstant.LINE_REV_VEHICLE_CODE, vehicle_code);
        savedDataMap.put(SDVPropertyConstant.BL_SEQUENCE_NO, txtFindNo.getText());

        return savedDataMap;
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
        curDataMap = saveData();
        return curDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        curDataMap = saveData();
        return curDataMap;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new AbstractSDVInitOperation() {

            public IDataMap getInitData() {
                IDataMap dataMap = new RawDataMap();
                AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
                if (application instanceof MFGLegacyApplication) {
                    TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();
                    try {
                        TCComponentBOMLine topBOPLine = bopWindow.getTopBOMLine();

                        dataMap.put("shopCode", topBOPLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE), IData.STRING_FIELD);
                        dataMap.put("productCode", topBOPLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE), IData.STRING_FIELD);
                        vehicle_code = topBOPLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE);

                    } catch (TCException e) {
                        e.printStackTrace();
                    }
                }

                return dataMap;
            }

            @Override
            public void executeOperation() throws Exception {
                IDataSet dataset = new DataSet();
                dataset.addDataMap("lineInform", getInitData());

                setData(dataset);
            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset.containsMap("lineInform")) {
                IDataMap dataMap = dataset.getDataMap("lineInform");

                txtProductCode.setText(dataMap.getStringValue("productCode"));
                txtProductCode.setEnabled(false);
                txtShopCode.setText(dataMap.getStringValue("shopCode"));
                txtShopCode.setEnabled(false);
            }
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
}
