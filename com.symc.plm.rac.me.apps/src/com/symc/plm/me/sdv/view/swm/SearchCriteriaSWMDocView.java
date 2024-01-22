/**
 * 
 */
package com.symc.plm.me.sdv.view.swm;

import java.util.List;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.teamcenter.rac.util.controls.SWTComboBox;
import com.teamcenter.soa.client.model.LovValue;
import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVText;

/**
 * Class Name : SearchCriteriaSWMDocView
 * Class Description :
 * 
 * @date 2013. 11. 14.
 * 
 */
public class SearchCriteriaSWMDocView extends AbstractSDVViewPane {
    private SDVText txtReferenceInfo;
    private SDVText txtGroup;
    private SDVText txtWorkName;
    private SWTComboBox comboVehicle;
    private SWTComboBox comboShop;
    private SWTComboBox comboCategory;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public SearchCriteriaSWMDocView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout(SWT.HORIZONTAL));

        Group group = new Group(composite, SWT.NONE);
        GridLayout gl_group = new GridLayout(8, false);
        gl_group.horizontalSpacing = 10;
        gl_group.marginLeft = 10;
        gl_group.marginRight = 10;
        gl_group.marginHeight = 20;
        gl_group.verticalSpacing = 20;
        group.setLayout(gl_group);

        Label lblVehicle = new Label(group, SWT.NONE);
        lblVehicle.setText("차종");

        comboVehicle = new SWTComboBox(group, SWT.NONE);
        GridData gd_comboVehicle = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_comboVehicle.widthHint = 190;
        comboVehicle.setLayoutData(gd_comboVehicle);

        Label lblShop = new Label(group, SWT.RIGHT);
        GridData gd_lblShop = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gd_lblShop.widthHint = 50;
        lblShop.setLayoutData(gd_lblShop);
        lblShop.setText("샵");

        comboShop = new SWTComboBox(group, SWT.NONE);
        GridData gd_comboShop = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_comboShop.widthHint = 190;
        comboShop.setLayoutData(gd_comboShop);

        Label lblCategory = new Label(group, SWT.NONE);
        lblCategory.setAlignment(SWT.RIGHT);
        GridData gd_lblCategory = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gd_lblCategory.widthHint = 62;
        lblCategory.setLayoutData(gd_lblCategory);
        lblCategory.setText("구분");

        comboCategory = new SWTComboBox(group, SWT.NONE);
        GridData gd_comboCategory = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_comboCategory.widthHint = 190;
        comboCategory.setLayoutData(gd_comboCategory);

        Label lblReferenceInfo = new Label(group, SWT.NONE);
        lblReferenceInfo.setAlignment(SWT.RIGHT);
        GridData gd_lblReferenceInfo = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gd_lblReferenceInfo.widthHint = 85;
        lblReferenceInfo.setLayoutData(gd_lblReferenceInfo);
        lblReferenceInfo.setText("관련근거");

        txtReferenceInfo = new SDVText(group, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtReferenceInfo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_txtReferenceInfo.widthHint = 233;
        txtReferenceInfo.setLayoutData(gd_txtReferenceInfo);

        Label lblGroup = new Label(group, SWT.NONE);
        lblGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblGroup.setText("직");

        txtGroup = new SDVText(group, SWT.BORDER | SWT.SINGLE);
        txtGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        txtGroup.setTextLimit(12);

        Label lblWorkName = new Label(group, SWT.NONE);
        lblWorkName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblWorkName.setText("작업명");

        txtWorkName = new SDVText(group, SWT.BORDER | SWT.SINGLE);
        txtWorkName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
    }

    // combobox에 LOV 가져오기
    private void comboValueSetting(SWTComboBox combo, String lovName) {
        try {
            if (lovName != null) {

                List<LovValue> lovValues = SDVLOVUtils.getLOVValues(lovName);
                if (lovValues != null) {
                    for (LovValue lov : lovValues) {
                        String value = lov.getStringValue();
                        String desc = lov.getDescription();
                        combo.addItem(value + " (" + desc + ")", value);
                        // combo.addItem(desc);
                    }
                }
            }
            combo.setAutoCompleteSuggestive(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IDataMap getLocalDataMap() {
        IDataMap dataMap = new RawDataMap();

        dataMap.put("vehicle_no", (String) comboVehicle.getSelectedItem(), IData.STRING_FIELD);
        dataMap.put("shop_code", (String) comboShop.getSelectedItem(), IData.STRING_FIELD);
        dataMap.put("category", (String) comboCategory.getSelectedItem(), IData.STRING_FIELD);
        dataMap.put("reference_info", (String) txtReferenceInfo.getText(), IData.STRING_FIELD);
        dataMap.put("group", (String) txtGroup.getText(), IData.STRING_FIELD);
        dataMap.put("workName", (String) txtWorkName.getText(), IData.STRING_FIELD);

        return dataMap;

    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return getLocalDataMap();
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_SUCCESS) {
            comboVehicle.addItem("ALL");
            comboValueSetting(comboVehicle, "M7_VEHICLE_NO");
            comboVehicle.setSelectedIndex(0);

            comboValueSetting(comboShop, "M7_SWM_SHOP_CODE");
            comboShop.setSelectedIndex(9);

            comboCategory.addItem("ALL");
            comboValueSetting(comboCategory, "M7_SWM_CATEGORY");
            comboCategory.setSelectedIndex(0);
        }
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {

        return new AbstractSDVInitOperation() {

            @Override
            public void executeOperation() throws Exception {

            }
        };
    }

    @Override
    public void uiLoadCompleted() {

    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public Composite getRootContext() {

        return null;
    }

}
