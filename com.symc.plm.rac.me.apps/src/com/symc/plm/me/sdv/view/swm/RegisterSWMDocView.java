/**
 * 
 */
package com.symc.plm.me.sdv.view.swm;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.nebula.widgets.datechooser.DateChooserCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.SDVRuntimeException;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCReservationService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;
import com.teamcenter.soa.client.model.LovValue;

/**
 * Class Name : RegisterSWMDocView
 * Class Description : getConfigId() == 0 이면 생성 다이얼로그, getConfigId() == 1 이면 수정 다이얼로그
 * 
 * @date 2013. 11. 6.
 * 
 */
public class RegisterSWMDocView extends AbstractSDVViewPane {
    public TCSession session;
    private SDVText textReferenceItemId;
    private SDVText textReferenceObjectName;
    private SDVText textGroup;
    private SWTComboBox comboVehicle;
    private SWTComboBox comboShop;
    private SWTComboBox comboCategory;
    private Button btnSearchOperation;
    private DateChooserCombo discardDate;
    private Label lblDesc;
    private Label lblDiscardDate;
    private IDataMap dataMap;
    private IDataMap localDataMap;
    private String dialogId;
    private String referenceItemId;
    private String group;
    private String referenceObjectName;
    TCComponentItemRevision itemRev;
    private Registry registry;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public RegisterSWMDocView(Composite parent, int style, String id) {
        super(parent, style, id);
        this.session = (TCSession) AIFUtility.getSessionManager().getDefaultSession();
    }

    /**
     * @param parent
     * @param style
     * @param id
     * @param configId
     */
    public RegisterSWMDocView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
        this.session = (TCSession) AIFUtility.getSessionManager().getDefaultSession();
    }

    @Override
    protected void initUI(Composite parent) {
        registry = Registry.getRegistry(this);

        localDataMap = new RawDataMap();
        localDataMap.put("configId", new RawData("configId", getConfigId(), IData.INTEGER_FIELD));

        dialogId = UIManager.getCurrentDialog().getId();

        Composite composite = new Composite(parent, SWT.NONE);
        // composite.setLayout(new FormLayout());
        composite.setLayout(new FillLayout());

        Group group = new Group(composite, SWT.NONE);
        group.setBounds(10, 10, 800, 300);
        GridLayout gl_group = new GridLayout(10, false);
        gl_group.horizontalSpacing = 10;
        gl_group.marginLeft = 10;
        gl_group.marginRight = 10;
        // gl_group.marginHeight = 10;
        // gl_group.verticalSpacing = 10;
        group.setLayout(gl_group);

        Label lblCategory = new Label(group, SWT.NONE);
        lblCategory.setText("구분");

        comboCategory = new SWTComboBox(group, SWT.NONE);
        GridData gd_comboCategory = new GridData(SWT.FILL, SWT.CENTER, false, true, 2, 1);
        gd_comboCategory.widthHint = 185;
        comboCategory.setLayoutData(gd_comboCategory);

        Label lblVehicle = new Label(group, SWT.RIGHT);
        lblVehicle.setAlignment(SWT.RIGHT);
        GridData gd_lblVehicle = new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1);
        gd_lblVehicle.widthHint = 60;
        lblVehicle.setLayoutData(gd_lblVehicle);
        lblVehicle.setText("차종");

        comboVehicle = new SWTComboBox(group, SWT.NONE);
        GridData gd_comboVehicle = new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1);
        gd_comboVehicle.widthHint = 170;
        comboVehicle.setLayoutData(gd_comboVehicle);

        Label lblShop = new Label(group, SWT.NONE);
        lblShop.setAlignment(SWT.RIGHT);
        GridData gd_lblShop = new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1);
        gd_lblShop.widthHint = 47;
        lblShop.setLayoutData(gd_lblShop);
        lblShop.setText("샵");

        comboShop = new SWTComboBox(group, SWT.NONE);
        GridData gd_comboShop = new GridData(SWT.FILL, SWT.CENTER, true, true, 4, 1);
        gd_comboShop.widthHint = 170;
        comboShop.setLayoutData(gd_comboShop);

        Label lblReferenceInfo = new Label(group, SWT.NONE);
        lblReferenceInfo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
        lblReferenceInfo.setText("관련근거");

        textReferenceItemId = new SDVText(group, SWT.BORDER | SWT.SINGLE);
        textReferenceItemId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 4, 1));
        textReferenceItemId.setMandatory(true);
        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);

        btnSearchOperation = new Button(group, SWT.PUSH);
        GridData gd_btnSearchOperation = new GridData(SWT.FILL, SWT.CENTER, false, true, 3, 1);
        gd_btnSearchOperation.widthHint = 100;
        btnSearchOperation.setLayoutData(gd_btnSearchOperation);
        btnSearchOperation.setText("공법 검색");
        btnSearchOperation.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                Display.getDefault().syncExec(new Runnable() {
                    IDialog dialog = null;

                    public void run() {
                        try {
                            dialog = UIManager.getDialog(getShell(), "symc.dialog.registerSWMOperationSearchDocDialog");
                            Map<String, Object> paramMap = new HashMap<String, Object>();
                            if (getConfigId() == 0) {
                                // Validate (검색 조건에 차종이 필요 합니다)
                                if (comboVehicle.getSelectedItem() == null) {
                                    MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("SearchcConditionIsRequiredVehicleCodeField.MESSAGE"), "Warning", MessageBox.WARNING);
                                    return;
                                }
                                paramMap.put("vehicle_no", comboVehicle.getSelectedItem());
                                paramMap.put("shop_code", comboShop.getSelectedItem());
                            } else {
                                paramMap.put("vehicle_no", itemRev.getProperty(SDVPropertyConstant.SWM_VEHICLE_CODE));
                                paramMap.put("shop_code", itemRev.getProperty(SDVPropertyConstant.SWM_SHOP_CODE));
                            }
                            paramMap.put("targetId", dialogId + "/" + getId());

                            dialog.setParameters(paramMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.open();

                        // 공법 검색 결과를 다이얼로그에 setText
                        if (dataMap == null) {
                            return;
                        } else {
                            if (dataMap.containsKey(SDVPropertyConstant.ITEM_ITEM_ID)) {
                                textReferenceItemId.setText(dataMap.getStringValue(SDVPropertyConstant.ITEM_ITEM_ID));
                            }
                            if (dataMap.containsKey(SDVPropertyConstant.ITEM_OBJECT_NAME)) {
                                textReferenceObjectName.setText(dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME));
                            }
                        }
                    }
                });
            }
        });

        Label lblReferenceObjectName = new Label(group, SWT.NONE);
        lblReferenceObjectName.setText("작업명");

        textReferenceObjectName = new SDVText(group, SWT.BORDER | SWT.SINGLE);
        textReferenceObjectName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 9, 1));
        textReferenceObjectName.setMandatory(true);

        Label lblGroup = new Label(group, SWT.NONE);
        lblGroup.setText("직");

        textGroup = new SDVText(group, SWT.BORDER | SWT.SINGLE);
        // textGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        GridData gd_textGroup = new GridData(SWT.FILL, SWT.CENTER, false, true, 2, 1);
        gd_textGroup.widthHint = 185;
        textGroup.setLayoutData(gd_textGroup);
        textGroup.setTextLimit(12);
        textGroup.setMandatory(true);

        lblDesc = new Label(group, SWT.NONE);
        lblDesc.setAlignment(SWT.RIGHT);
        GridData gd_lblDesc = new GridData(SWT.RIGHT, SWT.CENTER, false, true, 2, 1);
        gd_lblDesc.widthHint = 300;
        lblDesc.setLayoutData(gd_lblDesc);
        lblDesc.setText("직 예시문: \"의장1직\" 또는 \"의장1/2직\"(주야간 운영시)");

        lblDiscardDate = new Label(group, SWT.NONE);
        lblDiscardDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
        lblDiscardDate.setAlignment(SWT.RIGHT);
        lblDiscardDate.setText("폐기일");

        discardDate = new DateChooserCombo(group, SWT.BORDER);
        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);
        new Label(group, SWT.NONE);
    }

    private void setMandatory(Control con) {
        ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
        Registry registry = Registry.getRegistry("com.kgm.common.common");
        dec.setImage(registry.getImage("CONTROL_MANDATORY"));
        dec.setDescriptionText("This value will be required.");
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {
        this.dataMap = dataMap;

        referenceItemId = dataMap.getStringValue(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO);
        group = dataMap.getStringValue(SDVPropertyConstant.SWM_GROUP);
        referenceObjectName = dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME);

        if (referenceItemId == null || referenceItemId.trim().length() == 0) {
            return;
        }
        if (group == null || group.trim().length() == 0) {
            return;
        }
        if (referenceObjectName == null || referenceObjectName.trim().length() == 0) {
            return;
        }

        if (dataMap.containsKey("actionId")) {
            String actionId = dataMap.getStringValue("actionId");
            if ("Save and Check-In".equals(actionId)) {
                setEditableFalse();

                Map<String, Object> paramMap = getParameters();
                if (paramMap.containsKey("targetId")) {
                    String targetId = "symc.dialog.modifySWMDocDialog/searchListSWMDocView";
                    if (targetId.equals(targetId)) {
                        try {
                            IDialog dialog = UIManager.getDialog(getShell(), "symc.dialog.SearchSWMDocDialog");
                            String dialogId = "symc.dialog.SearchSWMDocDialog";
                            Map<String, Object> modifyMap = new HashMap<String, Object>();

                            modifyMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME));
                            modifyMap.put(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO, dataMap.getStringValue(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO));
                            modifyMap.put(SDVPropertyConstant.ITEM_M7_DISCARD_DATE, dataMap.getValue(SDVPropertyConstant.ITEM_M7_DISCARD_DATE));
                            modifyMap.put(SDVPropertyConstant.SWM_GROUP, dataMap.getStringValue(SDVPropertyConstant.SWM_GROUP));
                            modifyMap.put("targetId", dialogId + "/" + "searchListSWMDocView");

                            SearchListSWMDocView searchListSWMDocView = (SearchListSWMDocView) dialog.getView("searchListSWMDocView");
                            searchListSWMDocView.tableModify((Map<String, Object>) modifyMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public IDataMap getLocalDataMap() {
        IDataMap dataMap = new RawDataMap();
        // 생성 다이얼로그
        dataMap.put(SDVPropertyConstant.SWM_CATEGORY, comboCategory.getSelectedItem(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.SWM_VEHICLE_CODE, comboVehicle.getSelectedItem(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.SWM_SHOP_CODE, comboShop.getSelectedItem(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO, textReferenceItemId.getText(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, textReferenceObjectName.getText(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.SWM_GROUP, textGroup.getText(), IData.STRING_FIELD);
        // 수정 다이얼로그
        if (getConfigId() == 1) {
            dataMap.put("targetComp", itemRev, IData.OBJECT_FIELD);
            dataMap.put(SDVPropertyConstant.ITEM_M7_DISCARD_DATE, discardDate.getValue(), IData.OBJECT_FIELD);
        }

        return dataMap;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        comboValueSetting(comboCategory, "M7_SWM_CATEGORY");
        comboValueSetting(comboVehicle, "M7_VEHICLE_NO");
        comboValueSetting(comboShop, "M7_SWM_SHOP_CODE");
        if (getConfigId() == 0) { // 생성 다이얼로그
            setMandatory(comboCategory);
            setMandatory(comboVehicle);
            setMandatory(comboShop);
            comboShop.setSelectedIndex(9);
            lblDiscardDate.setVisible(false);
            discardDate.setVisible(false);
        } else { // 수정 다이얼로그
            try {
                if (!itemRev.getProperty("checked_out").equals("Y")) {
                    setEditableFalse();
                } else {
                    setEditableTrue();
                }
            } catch (TCException e1) {
                e1.printStackTrace();
            }
            IDataMap dataMap = dataset.getDataMap("swmDocInfo");
            if (dataMap != null) {
                comboCategory.setText(dataMap.getStringValue(SDVPropertyConstant.SWM_CATEGORY));
                comboCategory.setEnabled(false);
                comboCategory.getTextField().setEnabled(false);
                comboCategory.getTextField().setBackground(comboCategory.getTextField().getBackground());
                comboCategory.redraw();

                comboVehicle.setText(dataMap.getStringValue(SDVPropertyConstant.SWM_VEHICLE_CODE));
                comboVehicle.setEnabled(false);
                comboVehicle.getTextField().setEnabled(false);
                comboVehicle.getTextField().setBackground(comboVehicle.getTextField().getBackground());
                comboVehicle.redraw();

                comboShop.setText(dataMap.getStringValue(SDVPropertyConstant.SWM_SHOP_CODE));
                comboShop.setEnabled(false);
                comboShop.getTextField().setEnabled(false);
                comboShop.getTextField().setBackground(comboShop.getTextField().getBackground());
                comboShop.redraw();

                textReferenceItemId.setText(dataMap.getStringValue(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO));
                textReferenceObjectName.setText(dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME));
                textGroup.setText(dataMap.getStringValue(SDVPropertyConstant.SWM_GROUP));

                String dateString = dataMap.getValue(SDVPropertyConstant.ITEM_M7_DISCARD_DATE).toString();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                Date date;
                try {
                    if (!dateString.equals("")) {
                        date = formatter.parse(dateString);
                        discardDate.setValue(date);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return getLocalDataMap();
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new AbstractSDVInitOperation() {

            @Override
            public void executeOperation() throws Exception {
                if (getConfigId() == 0) {// 생성 다이얼로그
                    return;
                }

                Map<String, Object> paramMap = getParameters();
                if (paramMap.containsKey("targetId")) {// 검색 다이얼로그 검색 결과에서 targetComp는 선택한 수정 할 아이템
                    TCComponentItemRevision targetComp = (TCComponentItemRevision) paramMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
                    itemRev = targetComp;
                } else {// 수정 다이얼로그에서 itemRev는 선택한 수정 할 아이템
                    itemRev = (TCComponentItemRevision) AIFUtility.getCurrentApplication().getTargetComponent();
                }

                IDataMap swmDocMap = new RawDataMap();

                if (itemRev != null) {
                    try {
                        IDataSet dataset = new DataSet();
                        String[] propertyNames = new String[] { SDVPropertyConstant.ITEM_ITEM_ID, SDVPropertyConstant.ITEM_M7_REFERENCE_INFO, SDVPropertyConstant.ITEM_OBJECT_NAME, SDVPropertyConstant.SWM_GROUP, SDVPropertyConstant.ITEM_M7_DISCARD_DATE };
                        String[] propertyValues = itemRev.getProperties(propertyNames);
                        if (propertyValues != null) {
                            for (int i = 0; i < propertyNames.length; i++) {
                                swmDocMap.put(propertyNames[i], propertyValues[i]);
                            }
                        }

                        List<LovValue> vehicleCodeList = getLOVValues(ExcelTemplateHelper.getTCSession(), "M7_VEHICLE_NO");
                        for (LovValue vehicleCodeElement : vehicleCodeList) {
                            if (itemRev.getProperty(SDVPropertyConstant.SWM_VEHICLE_CODE).equals(vehicleCodeElement.getValue())) {
                                String value = (String) vehicleCodeElement.getValue();
                                String desc = vehicleCodeElement.getDescription();
                                swmDocMap.put(SDVPropertyConstant.SWM_VEHICLE_CODE, value + " (" + desc + ")");
                            }
                        }

                        List<LovValue> categoryList = getLOVValues(ExcelTemplateHelper.getTCSession(), "M7_SWM_CATEGORY");
                        for (LovValue categoryElement : categoryList) {
                            if (itemRev.getProperty(SDVPropertyConstant.SWM_CATEGORY).equals(categoryElement.getValue())) {
                                String value = (String) categoryElement.getValue();
                                String desc = categoryElement.getDescription();
                                swmDocMap.put(SDVPropertyConstant.SWM_CATEGORY, value + " (" + desc + ")");
                            }
                        }

                        List<LovValue> shopList = getLOVValues(ExcelTemplateHelper.getTCSession(), "M7_SWM_SHOP_CODE");
                        for (LovValue shopElement : shopList) {
                            if (itemRev.getProperty(SDVPropertyConstant.SWM_SHOP_CODE).equals(shopElement.getValue())) {
                                String value = (String) shopElement.getValue();
                                String desc = shopElement.getDescription();
                                swmDocMap.put(SDVPropertyConstant.SWM_SHOP_CODE, value + " (" + desc + ")");
                            }
                        }
                        dataset.addDataMap("swmDocInfo", swmDocMap);
                        setData(dataset);

                    } catch (TCException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public void checkOut() throws TCException {
        if (!itemRev.okToCheckout()) {
            MessageBox.post(UIManager.getCurrentDialog().getShell(), "Check-Out And Edit was fail.", "Warning", MessageBox.WARNING);
            return;
        }

        TCReservationService itemRevisioncheckOut = itemRev.getSession().getReservationService();
        itemRevisioncheckOut.reserve(itemRev);

        setEditableTrue();

        if (SYMTcUtil.isCheckedOut(itemRev)) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.modifySWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("Check-OutAndEditWasSuccessful.MESSAGE"), "Information", MessageBox.INFORMATION);
        }
    }

    public void checkOutCancel() throws TCException {
        TCReservationService itemRevisioncheckOutCancel = itemRev.getSession().getReservationService();
        itemRevisioncheckOutCancel.cancelReservation(itemRev);

        // checkOutCancel 하기 전 dataRollback
        textReferenceItemId.setText(itemRev.getStringProperty(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO));
        textReferenceObjectName.setText(itemRev.getStringProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
        textGroup.setText(itemRev.getStringProperty(SDVPropertyConstant.SWM_GROUP));
        discardDate.setData(itemRev.getDateProperty(SDVPropertyConstant.ITEM_M7_DISCARD_DATE));

        setEditableFalse();

        if (!SYMTcUtil.isCheckedOut(itemRev)) {
            IDialog dialog = UIManager.getCurrentDialog();
            if (dialog.getShell() == null) {
                dialog = UIManager.getAvailableDialog("symc.dialog.modifySWMDocDialog");
            }
            MessageBox.post(dialog.getShell(), registry.getString("CancelCheck-OutWasSuccessful.MESSAGE"), "Information", MessageBox.INFORMATION);
        }
    }

    public void setEditableTrue() {
        setButtonVisibleTrue();

        textReferenceItemId.setEditable(true);
        textReferenceItemId.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        textReferenceItemId.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));

        textReferenceObjectName.setEditable(true);
        textReferenceObjectName.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        textReferenceObjectName.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));

        textGroup.setEditable(true);
        textGroup.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        textGroup.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));

        discardDate.setEnabled(true);
    }

    public void setEditableFalse() {
        setButtonVisibleFalse();

        textReferenceItemId.setEditable(false);
        textReferenceItemId.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
        textReferenceItemId.setBackground(getShell().getBackground());

        textReferenceObjectName.setEditable(false);
        textReferenceObjectName.setBackground(getShell().getBackground());
        textReferenceObjectName.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));

        textGroup.setEditable(false);
        textGroup.setBackground(getShell().getBackground());
        textGroup.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));

        discardDate.setEnabled(false);
    }

    public void setButtonVisibleTrue() {
        btnSearchOperation.setEnabled(true);

        try {
            IDialog dialog = UIManager.getDialog(getShell(), "symc.dialog.modifySWMDocDialog");
            LinkedHashMap<String, IButtonInfo> actionButtons = ((AbstractSDVSWTDialog) dialog).getCommandToolButtons();

            for (String key : actionButtons.keySet()) {
                if (actionButtons.get(key).getActionId().equals("Check-Out and Edit")) {
                    actionButtons.get(key).getButton().setVisible(false);
                }
                if (actionButtons.get(key).getActionId().equals("Save and Check-In")) {
                    actionButtons.get(key).getButton().setVisible(true);
                }
                if (actionButtons.get(key).getActionId().equals("SaveEdit")) {
                    actionButtons.get(key).getButton().setVisible(true);
                }
                if (actionButtons.get(key).getActionId().equals("Cancel Check-Out")) {
                    actionButtons.get(key).getButton().setVisible(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setButtonVisibleFalse() {
        btnSearchOperation.setEnabled(false);

        try {
            IDialog dialog = UIManager.getDialog(getShell(), "symc.dialog.modifySWMDocDialog");
            LinkedHashMap<String, IButtonInfo> actionButtons = ((AbstractSDVSWTDialog) dialog).getCommandToolButtons();

            for (String key : actionButtons.keySet()) {
                if (actionButtons.get(key).getActionId().equals("Check-Out and Edit")) {
                    actionButtons.get(key).getButton().setVisible(true);
                }
                if (actionButtons.get(key).getActionId().equals("Save and Check-In")) {
                    actionButtons.get(key).getButton().setVisible(false);
                }
                if (actionButtons.get(key).getActionId().equals("SaveEdit")) {
                    actionButtons.get(key).getButton().setVisible(false);
                }
                if (actionButtons.get(key).getActionId().equals("Cancel Check-Out")) {
                    actionButtons.get(key).getButton().setVisible(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void validateConfig(int configId) {
        if (configId != 0 && configId != 1) {
            throw new SDVRuntimeException("View[" + getId() + " not supported config Id :" + configId);
        }
    }

    public static List<LovValue> getLOVValues(TCSession session, String lovName) throws TCException {
        TCComponentListOfValuesType type = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
        TCComponentListOfValues[] values = type.find(lovName);

        return values[0].getListOfValues().getValues();
    }

    // combox에 LOV 가져오기
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
    public void uiLoadCompleted() {

    }

    @Override
    public Composite getRootContext() {

        return null;
    }

}
