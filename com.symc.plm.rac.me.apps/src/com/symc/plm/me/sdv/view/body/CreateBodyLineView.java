/**
 * 
 */
package com.symc.plm.me.sdv.view.body;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.body.CreateBodyLineInitOperation;
import com.symc.plm.me.sdv.view.meco.MecoSelectView;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyLineView
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 * @date 2013. 12. 9.
 * 
 */
public class CreateBodyLineView extends AbstractSDVViewPane {
    private SDVText txtShop;
    private SDVLOVComboBox lovLine;
    private SDVText txtLineKorName;
    private SDVText txtLineEngName;
    private SDVText txtJph;
    private SDVText txtAllowance;
    private SDVText txtPlanningVer;
    private boolean isAlt;
    private String altPrefix;
    public String vehicle_code;
    public String product_code;
    private Registry registry;
    private TCComponentBOMLine parentShopLine;

    /**
     * @param parent
     * @param style
     * @param id
     * [CF-3537] [20230131]isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경
     */
    public CreateBodyLineView(Composite parent, int style, String id) {
        super(parent, style, id);
        ((GridData) getRootContext().getLayoutData()).heightHint = 201;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void initUI(Composite parent) {
        super.initUI(parent);

        registry = Registry.getRegistry(CreateBodyLineView.class);

        try {
            Group group = new Group(parent, SWT.NONE);
            group.setText(registry.getString("CreateShopDialog.Line.Group.Name", "Line Properties"));
            GridLayout gl_group = new GridLayout(2, false);
            gl_group.horizontalSpacing = 10;
            gl_group.marginLeft = 5;
            gl_group.marginHeight = 20;
            gl_group.verticalSpacing = 20;
            group.setLayout(gl_group);

            Label lblShop = new Label(group, SWT.NONE);
            lblShop.setBounds(0, 0, 56, 15);
            lblShop.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_SHOP_CODE));

            Composite composite = new Composite(group, SWT.NONE);
            GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gd_composite.heightHint = 21;
            composite.setLayoutData(gd_composite);
            composite.setBounds(0, 0, 64, 64);
            GridLayout gl_composite = new GridLayout(7, false);
            gl_composite.horizontalSpacing = 10;
            gl_composite.verticalSpacing = 0;
            gl_composite.marginWidth = 0;
            gl_composite.marginHeight = 0;
            composite.setLayout(gl_composite);

            txtShop = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
            GridData gb_txtShop = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gb_txtShop.widthHint = 40;
            txtShop.setLayoutData(gb_txtShop);
            txtShop.setEditable(false);
            new Label(composite, SWT.NONE);

            Label lblLineCode = new Label(composite, SWT.NONE);
            lblLineCode.setBounds(0, 0, 56, 15);
            lblLineCode.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.LINE_REV_CODE));

            lovLine = new SDVLOVComboBox(composite, "M7_BOPB_LINE_CODE");// new SDVText(composite, SWT.BORDER);
            GridData gb_txtLine = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gb_txtLine.widthHint = 100;
            lovLine.setLayoutData(gb_txtLine);
            // lovLine.setTextLimit(2);
            lovLine.setMandatory(true);
            // lovLine.setInputType(SDVText.ENGUPPERNUM);

            Label lblLineKorName = new Label(group, SWT.NONE);
            lblLineKorName.setBounds(0, 0, 56, 15);
            lblLineKorName.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.ITEM_OBJECT_NAME));

            txtLineKorName = new SDVText(group, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtLineKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gd_txtLineKorName.widthHint = 300;
            txtLineKorName.setLayoutData(gd_txtLineKorName);
            txtLineKorName.setTextLimit(80);
            txtLineKorName.setMandatory(true);

            Label lblLineEngName = new Label(group, SWT.NONE);
            lblLineEngName.setBounds(0, 0, 56, 15);
            lblLineEngName.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.LINE_REV_ENG_NAME));

            txtLineEngName = new SDVText(group, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtLineEngName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gd_txtLineEngName.widthHint = 300;
            txtLineEngName.setLayoutData(gd_txtLineEngName);
            txtLineEngName.setMandatory(true);
            txtLineEngName.setTextLimit(80);

            Label lblJph = new Label(group, SWT.NONE);
            lblJph.setBounds(0, 0, 56, 15);
            lblJph.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.LINE_REV_JPH));

            Composite composite_2 = new Composite(group, SWT.NONE);
            GridData gd_composite_2 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
            gd_composite_2.widthHint = 0;
            composite_2.setLayoutData(gd_composite_2);
            composite_2.setBounds(0, 0, 64, 64);
            GridLayout gl_composite_2 = new GridLayout(4, false);
            gl_composite_2.marginHeight = 0;
            gl_composite_2.marginWidth = 0;
            gl_composite_2.horizontalSpacing = 10;
            gl_composite_2.verticalSpacing = 0;
            composite_2.setLayout(gl_composite_2);

            txtJph = new SDVText(composite_2, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtJph = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gd_txtJph.widthHint = 90;
            txtJph.setLayoutData(gd_txtJph);
            // [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
            txtJph.setInputType(SDVText.DOUBLE);
            // txtJph.setTextLimit(2);
            txtJph.setTextLimit(5);

            new Label(composite, SWT.NONE);

            Label lblPlanningVer = new Label(composite, SWT.NONE);
            lblPlanningVer.setBounds(0, 0, 56, 15);
            lblPlanningVer.setVisible(false);

            txtPlanningVer = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
            GridData gb_txtPlanningVer = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gb_txtPlanningVer.widthHint = 30;
            txtPlanningVer.setLayoutData(gb_txtPlanningVer);
            txtPlanningVer.setVisible(false);
            txtPlanningVer.setText("00");

            new Label(composite_2, SWT.NONE);

            Label lblAllowance = new Label(composite_2, SWT.NONE);
            lblAllowance.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.LINE_REV_ALLOWANCE));

            txtAllowance = new SDVText(composite_2, SWT.BORDER | SWT.SINGLE);
            GridData gd_text_5 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gd_text_5.widthHint = 90;
            txtAllowance.setLayoutData(gd_text_5);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
        if (parameters != null) {
            if (parameters.containsKey(SDVPropertyConstant.SHOP_REV_SHOP_CODE)) {
                Object shopObject = parameters.get(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
                if (shopObject instanceof TCComponentBOMLine)
                    parentShopLine = (TCComponentBOMLine) shopObject;
            }

            if (parameters.containsKey(SDVPropertyConstant.LINE_REV_CODE))
                lovLine.setSelectedItem(parameters.get(SDVPropertyConstant.LINE_REV_CODE).toString());

            if (parameters.containsKey(SDVPropertyConstant.ITEM_OBJECT_NAME))
                txtLineKorName.setText(parameters.get(SDVPropertyConstant.ITEM_OBJECT_NAME).toString());

            if (parameters.containsKey(SDVPropertyConstant.LINE_REV_ENG_NAME))
                txtLineEngName.setText(parameters.get(SDVPropertyConstant.LINE_REV_ENG_NAME).toString());

            if (parameters.containsKey(SDVPropertyConstant.LINE_REV_JPH))
                txtJph.setText(parameters.get(SDVPropertyConstant.LINE_REV_JPH).toString());

            if (parameters.containsKey(SDVPropertyConstant.LINE_REV_ALLOWANCE))
                txtAllowance.setText(parameters.get(SDVPropertyConstant.LINE_REV_ALLOWANCE).toString());

            if (parameters.containsKey("PlanningVer"))
                txtPlanningVer.setText(parameters.get("PlanningVer").toString());

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

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        RawDataMap mecoData = new RawDataMap();

        mecoData.put(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, parentShopLine, IData.OBJECT_FIELD);
        mecoData.put(SDVPropertyConstant.LINE_REV_SHOP_CODE, txtShop.getText());
        mecoData.put(SDVPropertyConstant.LINE_REV_CODE, lovLine.getSelectedString());
        mecoData.put(SDVPropertyConstant.ITEM_OBJECT_NAME, txtLineKorName.getText());
        mecoData.put(SDVPropertyConstant.LINE_REV_ENG_NAME, txtLineEngName.getText());
        mecoData.put(SDVPropertyConstant.LINE_REV_JPH, txtJph.getText());
        mecoData.put(SDVPropertyConstant.LINE_REV_ALLOWANCE, txtAllowance.getText());
        mecoData.put(SDVPropertyConstant.LINE_REV_IS_ALTBOP, isAlt, IData.BOOLEAN_FIELD);
        mecoData.put(SDVPropertyConstant.LINE_REV_ALT_PREFIX, altPrefix);
        mecoData.put(SDVPropertyConstant.LINE_REV_VEHICLE_CODE, vehicle_code);
        mecoData.put(SDVPropertyConstant.LINE_REV_PRODUCT_CODE, product_code);
        mecoData.put("PlanningVer", txtPlanningVer.getText());

        return mecoData;
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
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new CreateBodyLineInitOperation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        // 오퍼의 결과를 화면에 설정하는 함수
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset != null) {
                AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
                parentShopLine = (TCComponentBOMLine) dataset.getValue(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);

                try {
                    txtShop.setText(parentShopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE));
                    txtShop.setEnabled(false);
                    isAlt = parentShopLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.SHOP_REV_IS_ALTBOP);
                    altPrefix = parentShopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
                    vehicle_code = parentShopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE);
                    product_code = parentShopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
            	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경 */
//                    IViewPane mecoView = dialog.getView("mecoView");
                    IViewPane mecoView = dialog.getView(SDVPropertyConstant.MECO_SELECT);
                    if (isAlt) {
                        dialog.setAddtionalTitle("Alternative BOP");
                        dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));

                        ((MecoSelectView) mecoView).setAlternative(false);
                    }
                    // else
                    // {
                    // String meco_no = parentShopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_MECO_NO);
                    // TCComponentItem mecoItem = SYMTcUtil.findItem(SDVBOPUtilities.getTCSession(), meco_no);
                    // if (mecoItem != null)
                    // {
                    // if (! CustomUtil.isReleased(mecoItem.getLatestItemRevision()))
                    // {
                    // HashMap<String, Object> dataMap = new HashMap<String, Object>();
                    //
                    // dataMap.put(SDVTypeConstant.MECO_ITEM_REV, mecoItem.getLatestItemRevision());
                    //
                    // mecoView.setParameters(dataMap);
                    // }
                    // }
                    // }
                    //
                    // ((SelectedMECOView) mecoView).visibleSearchBtn(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
