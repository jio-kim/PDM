/**
 * 
 */
package com.symc.plm.me.sdv.view.body;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.body.CreateBodyShopInitOperation;
import com.symc.plm.me.sdv.view.meco.MecoSelectView;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyShopView
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 * @date 2013. 11. 1.
 * 
 */
public class CreateBodyShopView extends AbstractSDVViewPane {
    private SDVText textAltPrefix;
    private SDVText textProductNo;
    // private SWTComboBox comboProductNo;
    // private SWTComboBox comboPlant;
    private SDVText textKorName;
    private SDVText textEngName;
    private SDVText textJPH;
    private SDVText textAllowance;
    private SDVLOVComboBox comboShop;
    private Button btnCheckAlt;
    private SDVText textVehicleKorName;
    private SDVText textVehicleEngName;
    private Registry registry;
    private boolean isWindowCreated = false;

    /**
     * @param parent
     * @param style
     * @param id
     * [CF-3537] [20230131]isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경
     */
    public CreateBodyShopView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        try {
            registry = Registry.getRegistry(CreateBodyShopView.class);

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(1, false));

            Group grpShop = new Group(composite, SWT.NONE);
            grpShop.setText(registry.getString("CreateShopDialog.Shop.Group.Name", "Shop Properties"));
            grpShop.setLayout(new GridLayout(5, false));
            GridData gd_grpShop = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
            gd_grpShop.heightHint = 155;
            grpShop.setLayoutData(gd_grpShop);

            Label lblShop = new Label(grpShop, SWT.NONE);
            lblShop.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

            lblShop.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_SHOP_CODE));

            Composite shopComposite = new Composite(grpShop, SWT.NONE);
            GridLayout shopGridLayout = new GridLayout(1, false);
            shopGridLayout.horizontalSpacing = 0;
            shopGridLayout.verticalSpacing = 0;
            shopGridLayout.marginHeight = 0;
            shopGridLayout.marginWidth = 0;

            shopComposite.setLayout(shopGridLayout);
            shopComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

            comboShop = new SDVLOVComboBox(shopComposite, "M7_BOPB_SHOP_CODE");
            comboShop.setMandatory(true);
            comboShop.setFixedHeight(true);

            GridData shopComboGrid = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            shopComboGrid.widthHint = 150;
            comboShop.setLayoutData(shopComboGrid);

            // new Label(grpShop, SWT.NONE);

            btnCheckAlt = new Button(grpShop, SWT.CHECK);
            btnCheckAlt.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_IS_ALTBOP));
            btnCheckAlt.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getAvailableDialog("symc.me.bop.CreateBodyShopDialog");
            	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
            	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경 */
//                    IViewPane mecoView = dialog.getView("mecoView");
                    IViewPane mecoView = dialog.getView(SDVPropertyConstant.MECO_SELECT);
                    if (((Button) arg0.widget).getSelection()) {
                        dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));
                        dialog.setAddtionalTitle("Alternative BOP");

                        textAltPrefix.setEnabled(true);
                        textAltPrefix.setMandatory(true);
//                        ((SelectedMECOView) mecoView).setAlternative(false);
                        ((MecoSelectView) mecoView).setAlternative(false);
                    } else {
                        dialog.setTitleBackground(null);
                        dialog.setAddtionalTitle("");
                        textAltPrefix.setText("");
                        textAltPrefix.setEnabled(false);
                        textAltPrefix.setMandatory(false);
//                        ((SelectedMECOView) mecoView).setAlternative(true);
                        ((MecoSelectView) mecoView).setAlternative(true);
                    }
                }
            });

            textAltPrefix = new SDVText(grpShop, SWT.BORDER | SWT.SINGLE);
            textAltPrefix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textAltPrefix.setEnabled(false);
            textAltPrefix.setTextLimit(4);
            textAltPrefix.setInputType(SDVText.ENGUPPERNUM);

            Label lblProductNo = new Label(grpShop, SWT.NONE);
            lblProductNo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblProductNo.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_PRODUCT_CODE));

            textProductNo = new SDVText(grpShop, SWT.BORDER | SWT.SINGLE);
            textProductNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
            textProductNo.setMandatory(true);
            textProductNo.setTextLimit(10);
            textProductNo.setInputType(SDVText.ENGUPPERNUM);
            textProductNo.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent arg0) {
                    if (isWindowCreated) {
                        try {
                            isWindowCreated = false;
                            ((TCComponentBOPLine) getData()).window().close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    setData(null);
                }
            });
            // comboProductNo = new SWTComboBox(grpShop, SWT.BORDER);
            // comboProductNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
            // setMandatory(comboProductNo);
            // // comboProductNo.getTextField().setEnabled(false);
            // // comboProductNo.getTextField().setBackground(comboShop.getTextField().getBackground());
            // // comboProductNo.redraw();

            Label nullLabel = new Label(grpShop, SWT.NONE);
            nullLabel.setVisible(false);

            // Label lblPlant = new Label(grpShop, SWT.NONE);
            // lblPlant.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            // lblPlant.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.PLANT_SHOP_ITEM, "Plant Code"));
            //
            // comboPlant = new SWTComboBox(grpShop, SWT.BORDER);
            // comboPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
            // // comboPlant.getTextField().setEnabled(false);
            // // comboPlant.getTextField().setBackground(comboShop.getTextField().getBackground());
            //
            // // Button btnSearchProduct = new Button(grpShop, SWT.NONE);
            // // btnSearchProduct.setText(registry.getString("CreateShopDialog.Poduct.Search.Button", "Search Product"));
            // // btnSearchProduct.setVisible(false);
            // Label nullLabel2 = new Label(grpShop, SWT.NONE);
            // nullLabel2.setVisible(false);

            Label lblKorName = new Label(grpShop, SWT.NONE);
            lblKorName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            //lblKorName.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_KOR_NAME));
            //[UPGRADE][0326] Korean Name 이 잘못나옴 수정 
            lblKorName.setText(registry.getString("ShopKorName.NAME"));


            textKorName = new SDVText(grpShop, SWT.BORDER | SWT.SINGLE);
            textKorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
            textKorName.setMandatory(true);
            textKorName.setTextLimit(80);

            Label lblEngName = new Label(grpShop, SWT.NONE);
            lblEngName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblEngName.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_ENG_NAME));

            textEngName = new SDVText(grpShop, SWT.BORDER | SWT.SINGLE);
            textEngName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
            textEngName.setMandatory(true);
            textEngName.setTextLimit(80);

            Label lblJPH = new Label(grpShop, SWT.NONE);
            lblJPH.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblJPH.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_JPH));

            textJPH = new SDVText(grpShop, SWT.BORDER | SWT.SINGLE);
            textJPH.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            // [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
            // textJPH.setTextLimit(3);
            textJPH.setTextLimit(5);
            textJPH.setMandatory(true);
            textJPH.setInputType(SDVText.DOUBLE);
            // textJPH.setInputType(SDVText.NUMERIC);

            Label lblAllowance = new Label(grpShop, SWT.NONE);
            lblAllowance.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblAllowance.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_ALLOWANCE));

            textAllowance = new SDVText(grpShop, SWT.BORDER | SWT.SINGLE);
            textAllowance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textAllowance.setTextLimit(5);
            textAllowance.setMandatory(true);
            textAllowance.setInputType(SDVText.DOUBLE);

            new Label(grpShop, SWT.NONE);

            Label lblVehicleKorName = new Label(grpShop, SWT.NONE);
            lblVehicleKorName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblVehicleKorName.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME));

            textVehicleKorName = new SDVText(grpShop, SWT.BORDER | SWT.SINGLE);
            textVehicleKorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
            textVehicleKorName.setTextLimit(80);

            Label lblVehicleEngName = new Label(grpShop, SWT.NONE);
            lblVehicleEngName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblVehicleEngName.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME));

            textVehicleEngName = new SDVText(grpShop, SWT.BORDER | SWT.SINGLE);
            textVehicleEngName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
            textVehicleEngName.setTextLimit(80);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param paramters
     *            the parameters to set
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
        if (parameters != null) {
        }
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    }

    @Override
    public IDataMap getLocalDataMap() {
        RawDataMap mecoData = new RawDataMap();

        // mecoData.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, comboProductNo.getSelectedItem(), IData.OBJECT_FIELD);
        if (textProductNo.getData() == null) {
            try {
                String productNo = textProductNo.getText();
                TCComponentItem sproductItem = SDVBOPUtilities.FindItem(productNo, SDVTypeConstant.EBOM_PRODUCT_ITEM);
                TCComponentItem mproductItem = SDVBOPUtilities.FindItem("M".concat(productNo.substring(1)), SDVTypeConstant.EBOM_PRODUCT_ITEM);

                if (sproductItem != null && mproductItem != null) {
                    TCComponentBOMLine mproductLine = CustomUtil.getBomline(mproductItem.getLatestItemRevision(), CustomUtil.getTCSession());
                    mecoData.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, mproductLine, IData.OBJECT_FIELD);
                    textProductNo.setData(mproductLine);
                    isWindowCreated = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else
            mecoData.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, textProductNo.getData(), IData.OBJECT_FIELD);

        mecoData.put(SDVPropertyConstant.SHOP_REV_KOR_NAME, textKorName.getText());
        mecoData.put(SDVPropertyConstant.SHOP_REV_ENG_NAME, textEngName.getText());
        mecoData.put(SDVPropertyConstant.SHOP_REV_JPH, textJPH.getText());
        mecoData.put(SDVPropertyConstant.SHOP_REV_ALLOWANCE, textAllowance.getText());
        mecoData.put(SDVPropertyConstant.SHOP_REV_ALT_PREFIX, textAltPrefix.getText());
        mecoData.put(SDVPropertyConstant.SHOP_REV_SHOP_CODE, comboShop.getSelectedString());
        mecoData.put(SDVPropertyConstant.SHOP_REV_IS_ALTBOP, String.valueOf(btnCheckAlt.getSelection()));
        // mecoData.put(SDVTypeConstant.PLANT_SHOP_ITEM, comboPlant.getSelectedItem(), IData.OBJECT_FIELD);
        mecoData.put(SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME, textVehicleKorName.getText());
        mecoData.put(SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME, textVehicleEngName.getText());

        return mecoData;
    }

    // private void setMandatory(Control con){
    // ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
    // Registry registry = Registry.getRegistry("com.ssangyong.common.common");
    // dec.setImage(registry.getImage("CONTROL_MANDATORY"));
    // dec.setDescriptionText("This value will be required.");
    // }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return getLocalDataMap();
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        CreateBodyShopInitOperation initOp = new CreateBodyShopInitOperation();

        return initOp;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset != null) {
                ArrayList<?> mProductLine = (ArrayList<?>) dataset.getListValue(SDVTypeConstant.EBOM_MPRODUCT, SDVTypeConstant.EBOM_MPRODUCT);
                ;
                if (mProductLine != null) {
                    try {
                        String productNo = mProductLine.toArray(new TCComponentBOMLine[0])[0].getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

                        textProductNo.setText("P".concat(productNo.substring(1)));
                        textProductNo.setData(mProductLine.get(0));
                        textProductNo.setEditable(false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                // ArrayList<?> mProductLine = (ArrayList<?>) dataset.getListValue(SDVTypeConstant.EBOM_MPRODUCT, SDVTypeConstant.EBOM_MPRODUCT);
                // if (mProductLine != null)
                // comboProductNo.addItems(null, mProductLine.toArray(new TCComponentBOMLine[0]));
                //
                // ArrayList<?> plantLine = (ArrayList<?>) dataset.getListValue(SDVTypeConstant.PLANT_SHOP_ITEM, SDVTypeConstant.PLANT_SHOP_ITEM);
                // if (plantLine != null)
                // comboPlant.addItems(null, plantLine.toArray(new TCComponentBOMLine[0]));
            }
        }

        textAllowance.setText(registry.getString("CreateBodyShop.Allowance.Default", "0.108"));
    }

    @Override
    public void uiLoadCompleted() {
    }
}
