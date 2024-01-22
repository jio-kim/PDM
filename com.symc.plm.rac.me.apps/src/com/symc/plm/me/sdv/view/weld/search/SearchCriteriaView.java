package com.symc.plm.me.sdv.view.weld.search;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.wp.search.SearchWeldConditionSheetsInitOperation;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.util.Registry;

public class SearchCriteriaView extends AbstractSDVViewPane {

    private Composite ruleComposite;

    private Label lblProductCode;
    private Label lblShopValue;
    private Combo cmbLine;
    private Combo cmbStation;
    private Text txtWeldOpId;

    private IDataSet localDataSet;
    private IDataMap curDataMap;

    private TCComponentBOPLine topLine;

    private Registry registry;

    public SearchCriteriaView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI() {
    }

    @Override
    protected void initUI(Composite parent) {

        registry = Registry.getRegistry(this);

        parent.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gl_composite = new GridLayout(1, false);
        gl_composite.verticalSpacing = 0;
        composite.setLayout(gl_composite);

        Group group = new Group(composite, SWT.NONE);
        GridLayout gl_group = new GridLayout(12, false);
        gl_group.marginBottom = 5;
        gl_group.marginHeight = 0;
        group.setLayout(gl_group);

        ruleComposite = new Composite(group, SWT.NONE);
        GridData gd_ruleComposite = new GridData(SWT.FILL, SWT.CENTER, false, false, 12, 1);
        gd_ruleComposite.heightHint = 30;
        ruleComposite.setLayoutData(gd_ruleComposite);
        ruleComposite.setBackground(UIUtil.getColor(SWT.COLOR_DARK_GRAY));

        Label lblProdNo = new Label(group, SWT.NONE);
        GridData gd_lblProdNo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblProdNo.widthHint = 86;
        lblProdNo.setLayoutData(gd_lblProdNo);
        lblProdNo.setAlignment(SWT.RIGHT);
        lblProdNo.setText(registry.getString("SearchCriteria.Search.ProductNO", "Product No."));
        //lblProdNo.setText("Product No. : ");

        lblProductCode = new Label(group, SWT.BORDER | SWT.READ_ONLY);
        lblProductCode.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtProdNo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_txtProdNo.widthHint = 80;
        lblProductCode.setLayoutData(gd_txtProdNo);

        Label lblShop = new Label(group, SWT.NONE);
        GridData gd_lblShop = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblShop.widthHint = 56;
        lblShop.setLayoutData(gd_lblShop);
        lblShop.setAlignment(SWT.RIGHT);
        lblShop.setText(registry.getString("SearchCriteria.Search.Shop", "Shop"));
        //lblShop.setText("Shop : ");

        lblShopValue = new Label(group, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
        lblShopValue.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtShop = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_txtShop.widthHint = 140;
        lblShopValue.setLayoutData(gd_txtShop);

        Label lblLine = new Label(group, SWT.NONE);
        GridData gd_lblLine = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblLine.widthHint = 74;
        lblLine.setLayoutData(gd_lblLine);
        lblLine.setAlignment(SWT.RIGHT);
        //lblLine.setText("Line");
        lblLine.setText(registry.getString("SearchCriteria.Search.Line", "Line"));

        cmbLine = new Combo(group, SWT.NONE | SWT.SINGLE);
        GridData gd_cmbLine = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_cmbLine.widthHint = 71;
        cmbLine.setLayoutData(gd_cmbLine);
        cmbLine.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                changeStationComboBox(cmbLine.getText());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

        });

        Label lblStation = new Label(group, SWT.NONE);
        GridData gd_lblStation = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblStation.widthHint = 82;
        lblStation.setLayoutData(gd_lblStation);
        lblStation.setAlignment(SWT.RIGHT);
        //lblStation.setText("Station");
        lblStation.setText(registry.getString("SearchCriteria.Search.Station", "Station"));

        cmbStation = new Combo(group, SWT.NONE);
        cmbStation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblWeldOpId = new Label(group, SWT.NONE | SWT.SINGLE);
        GridData gd_lblOpId = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_lblOpId.widthHint = 100;
        lblWeldOpId.setLayoutData(gd_lblOpId);
        lblWeldOpId.setAlignment(SWT.RIGHT);
        //lblWeldOpId.setText("Operation ID");
        lblWeldOpId.setText(registry.getString("SearchCriteria.Search.WeldOperationID", "WeldOperation ID"));

        txtWeldOpId = new Text(group, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtOpId = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
        gd_txtOpId.widthHint = 103;
        txtWeldOpId.setLayoutData(gd_txtOpId);

    }

    /**
     * Line ComboBox List 를 만든다
     *
     * @method initCoboBoxData
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void initComboBoxData(IDataSet dataset)
    {
        localDataSet = dataset;
        List<String> lineList = (List<String>) localDataSet.getData(SDVPropertyConstant.LINE_REV_CODE);

        // Line 정보를 가져온다
        String[] lineItem = new String[lineList.size() + 1];
        lineItem[0] = "All";
        for (int i = 0; i < lineList.size(); i++)
        {
            lineItem[i + 1] = lineList.get(i);
        }
        cmbLine.setItems(lineItem);
        cmbLine.select(0);
        changeStationComboBox(lineList.get(0));
    }

    /**
     * Station ComboBox List 를 만든다
     *
     * @method selectStationCombo
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void changeStationComboBox(String lineCode)
    {
        if (!lineCode.equals("All"))
        {
            List<String> stationList = (List<String>) localDataSet.getData(lineCode);
            // Station 정보를 가져온다
            String[] stationItem = new String[stationList.size() + 1];
            stationItem[0] = "All";
            for (int i = 0; i < stationList.size(); i++)
            {
                stationItem[i + 1] = stationList.get(i);
            }
            cmbStation.removeAll();
            cmbStation.setItems(stationItem);
            cmbStation.select(0);
        }
        else
        {
            cmbStation.removeAll();
            cmbStation.add("All");
            cmbStation.select(0);
        }

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
    private IDataMap saveData()
    {
        RawDataMap savedDataMap = new RawDataMap();

        savedDataMap.put(SDVPropertyConstant.LINE_REV_CODE, cmbLine.getText(), IData.STRING_FIELD);
        savedDataMap.put("stationCode", cmbStation.getText(), IData.STRING_FIELD);
        savedDataMap.put("weldOP", txtWeldOpId.getText(), IData.STRING_FIELD);
        savedDataMap.put("topLine", topLine, IData.OBJECT_FIELD);

        return savedDataMap;
    }

    protected IData getDataLocal() {
        return null;
    }

    protected IData getSelectedDataLocal() {
        return null;
    }


    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }


    @Override
    public IDataMap getLocalDataMap() {
        return this.curDataMap;
    }


    @Override
    public IDataMap getLocalSelectDataMap() {
        curDataMap = saveData();
        return curDataMap;
    }


    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        // 초기화 Init Operation 실행
        return new SearchWeldConditionSheetsInitOperation();
    }

    @Override
    public void initalizeData(int result, IViewPane owner, IDataSet dataset) {
        // Operation 결과
        if (result == SDVInitEvent.INIT_SUCCESS)
        {
            if (dataset != null)
            {
                topLine = (TCComponentBOPLine)dataset.getData("topLine");

                lblProductCode.setText((String) dataset.getData(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE));
                lblShopValue.setText((String) dataset.getData(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM));

                Point point = null;
                int startX = 5;

                CLabel lblRevisionRule = new CLabel(ruleComposite, SWT.NONE);
                lblRevisionRule.setImage(SWTResourceManager.getImage(SearchCriteriaView.class, "/icons/revisionrule_16.png"));
                lblRevisionRule.setText((String) dataset.getData("revisionRule"));
                point = lblRevisionRule.computeSize(-1, -1, true);
                lblRevisionRule.setBounds(startX, 3, point.x, 26);
                lblRevisionRule.setForeground(UIUtil.getColor(SWT.COLOR_WHITE));
                startX += point.x + 5;

                CLabel lblEffectivity = new CLabel(ruleComposite, SWT.NONE);
                lblEffectivity.setImage(SWTResourceManager.getImage(SearchCriteriaView.class, "/com/teamcenter/rac/common/images/effectivity_16.png"));
                lblEffectivity.setText((String) dataset.getData("windowRevisionDate"));
                point = lblEffectivity.computeSize(-1, -1, true);
                lblEffectivity.setBounds(startX, 3, point.x, 26);
                lblEffectivity.setForeground(UIUtil.getColor(SWT.COLOR_WHITE));
                startX += point.x + 5;

                CLabel lblVariantRule = new CLabel(ruleComposite, SWT.NONE);
                lblVariantRule.setImage(SWTResourceManager.getImage(SearchCriteriaView.class, "/icons/variantrule_16.png"));
                String variantRule = (String) dataset.getData("variantRule");
                if("".equals(variantRule))
                    variantRule = registry.getString("SearchCriteria.BOPVariantConditionNotConfigured", "Not Specified");
                lblVariantRule.setText(variantRule);
                point = lblVariantRule.computeSize(-1, -1, true);
                lblVariantRule.setBounds(startX, 3, point.x, 26);
                lblVariantRule.setForeground(UIUtil.getColor(SWT.COLOR_WHITE));

                initComboBoxData(dataset);
            }
        }
    }

    @Override
    public void uiLoadCompleted() {
        //

    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }
}
