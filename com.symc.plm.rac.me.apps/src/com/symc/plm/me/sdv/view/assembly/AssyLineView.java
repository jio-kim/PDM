/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly;

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
 * Class Name : AssyLineView
 * Class Description :
 * [SR140723-010][20140718] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 * @date 2013. 11. 15.
 * 
 */
public class AssyLineView extends AbstractSDVViewPane {

    private Label lblShopCode;
    private Label lblLineCode;
    private Label lblProductCode;
    private Label lblLineKorName;
    private Label lblLineEngName;
    private Label lblJph;
    private Label lblAllowance;
    private SDVText txtShopCode;
    private SDVText txtProductCode;
    private SDVText txtLineKorName;
    private SDVText txtLineEngName;
    private SDVText txtJph;
    private SDVText txtAllowance;
    private SDVText txtFindNo;
    private SDVLOVComboBox lovLine;
    public String vehicle_code;
    private TCSession tcSession = null;

    private IDataMap curDataMap = null;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public AssyLineView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    public AssyLineView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId, null);
    }

    public AssyLineView(Composite parent, int style, String id, int configId, String order) {
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

        Group grpLine = new Group(composite0, SWT.NONE);
        grpLine.setText(registry.getString("LineInform.NAME"));
        grpLine.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite composite = new Composite(grpLine, SWT.NONE);
        GridLayout gl_composite = new GridLayout(2, false);
        gl_composite.marginRight = 10;
        gl_composite.marginTop = 10;
        gl_composite.horizontalSpacing = 20;
        gl_composite.verticalSpacing = 10;
        gl_composite.marginLeft = 10;
        composite.setLayout(gl_composite);

        lblShopCode = new Label(composite, SWT.NONE);
        lblShopCode.setText(registry.getString("ShopCode.NAME"));

        Composite composite_1 = new Composite(composite, SWT.NONE);
        GridLayout gl_composite_1 = new GridLayout(6, false);
        gl_composite_1.horizontalSpacing = 15;
        gl_composite_1.marginWidth = 0;
        composite_1.setLayout(gl_composite_1);

        txtShopCode = new SDVText(composite_1, SWT.BORDER | SWT.SINGLE);
        GridData gb_txtShopCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gb_txtShopCode.widthHint = 100;
        txtShopCode.setLayoutData(gb_txtShopCode);
        new Label(composite_1, SWT.NONE);

        lblLineCode = new Label(composite_1, SWT.NONE);
        lblLineCode.setText(registry.getString("LineCode.NAME"));
        new Label(composite_1, SWT.NONE);

        lovLine = new SDVLOVComboBox(composite_1, SWT.BORDER, tcSession, "M7_BOPA_LINE_CODE");
        GridData gb_lovLine = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gb_lovLine.widthHint = 200;
        lovLine.setLayoutData(gb_lovLine);
        new Label(composite_1, SWT.NONE);

        lblProductCode = new Label(composite, SWT.NONE);
        lblProductCode.setText(registry.getString("ProductCode.NAME"));

        txtProductCode = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gb_txtProductCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gb_txtProductCode.widthHint = 100;
        txtProductCode.setLayoutData(gb_txtProductCode);

        lblLineKorName = new Label(composite, SWT.NONE);
        lblLineKorName.setText(registry.getString("LineKorName.NAME"));

        txtLineKorName = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtShopKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtShopKorName.widthHint = 420;
        txtLineKorName.setLayoutData(gd_txtShopKorName);

        lblLineEngName = new Label(composite, SWT.NONE);
        lblLineEngName.setText(registry.getString("LineEngName.NAME"));

        txtLineEngName = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_text.widthHint = 420;
        txtLineEngName.setLayoutData(gd_text);

        lblJph = new Label(composite, SWT.NONE);
        lblJph.setText(registry.getString("JPH.NAME"));

        Composite composite_2 = new Composite(composite, SWT.NONE);
        GridLayout gl_composite_2 = new GridLayout(6, false);
        gl_composite_2.horizontalSpacing = 15;
        gl_composite_2.verticalSpacing = 0;
        gl_composite_2.marginWidth = 0;
        composite_2.setLayout(gl_composite_2);

        txtJph = new SDVText(composite_2, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtJph = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 2);
        gd_txtJph.widthHint = 100;
        txtJph.setLayoutData(gd_txtJph);
        new Label(composite_2, SWT.NONE);

        lblAllowance = new Label(composite_2, SWT.NONE);
        lblAllowance.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
        new Label(composite_2, SWT.NONE);
        lblAllowance.setText(registry.getString("Allowance.NAME"));

        txtAllowance = new SDVText(composite_2, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtAllowance = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2);
        gd_txtAllowance.widthHint = 100;
        txtAllowance.setLayoutData(gd_txtAllowance);
        new Label(composite_2, SWT.NONE);

        Label lblFindNo = new Label(composite, SWT.NONE);
        lblFindNo.setText("Find No");

        txtFindNo = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtFindNo = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gd_txtFindNo.widthHint = 99;
        txtFindNo.setLayoutData(gd_txtFindNo);

        /**
         * Mandatory 설정
         */
        txtShopCode.setMandatory(true);
        lovLine.setMandatory(true);
        txtProductCode.setMandatory(true);
        txtLineKorName.setMandatory(true);
        txtLineEngName.setMandatory(true);

        /**
         * 자릿수 지정
         * [SR140723-010][20140718] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
         */
        txtJph.setTextLimit(5);
        txtFindNo.setTextLimit(3);

        /**
         * 유형 지정
         * [SR140723-010][20140718] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
         */
        txtJph.setInputType(SDVText.DOUBLE);
        txtAllowance.setInputType(SDVText.DOUBLE);
        txtFindNo.setInputType(SDVText.NUMERIC);

    }

    /**
     * 작성후 저장된 Data
     * 
     * @method saveData
     * @date 2013. 11. 20.
     * @param
     * @return Map<String,Object>
     * @exception
     * @throws
     * @see
     */
    private IDataMap saveData() {

        RawDataMap savedDataMap = new RawDataMap();

        savedDataMap.put(SDVPropertyConstant.LINE_REV_SHOP_CODE, txtShopCode.getText());
        savedDataMap.put(SDVPropertyConstant.LINE_REV_CODE, lovLine.getSelectedString());
        savedDataMap.put(SDVPropertyConstant.LINE_REV_PRODUCT_CODE, txtProductCode.getText());
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
