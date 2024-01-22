/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

public class CreateSubsidiaryViewPane extends AbstractSDVViewPane {
    private Registry registry;

    private SDVText itemIdText;
    private SDVText objectNameText;
    private SDVText oldPartNoText;
    private HashMap<String, Object> componentMap;
    private SDVText engNameText;
    private SDVText materialTypeText;
    private SDVText specKorText;
    private SDVText specEngText;
    private SDVText unitOfAmountText;
    private SDVText unitOfPurchasingText;
    private SDVText makerText;
    private SDVText reMarkText;

    private SWTComboBox subsidiaryGroupCombo;
    
    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public CreateSubsidiaryViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(final Composite parent) {
        try {
            registry = Registry.getRegistry(this);
            componentMap = new HashMap<String, Object>();

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new FormLayout());

            Label lblItemId = new Label(composite, SWT.NONE);
            FormData fdItemId = new FormData();
            fdItemId.top = new FormAttachment(0, 20);
            fdItemId.left = new FormAttachment(0, 5);
            fdItemId.width = 100;
            lblItemId.setLayoutData(fdItemId);
            lblItemId.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM, SDVPropertyConstant.ITEM_ITEM_ID));

            itemIdText = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
            FormData fditemIdText = generateFormData(lblItemId);
            itemIdText.setLayoutData(fditemIdText);
            itemIdText.setEditable(false);
            itemIdText.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
            itemIdText.setMandatory(true);
            ResourceUtilities.setSDVTextListener(itemIdText, true, true, null);
            componentMap.put(SDVPropertyConstant.ITEM_ITEM_ID, itemIdText);

            Label lblObjectName = createLabel(composite, lblItemId, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM, SDVPropertyConstant.ITEM_OBJECT_NAME));
            objectNameText = ResourceUtilities.createText(composite, generateFormData(lblObjectName), getBackground(), true, true, true, true, null);
            componentMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, objectNameText);

            Label lblEngName = createLabel(composite, lblObjectName, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM, SDVPropertyConstant.SUBSIDIARY_ENG_NAME));
            engNameText = ResourceUtilities.createText(composite, generateFormData(lblEngName), getBackground(), true, true, true, true, null);

            componentMap.put(SDVPropertyConstant.SUBSIDIARY_ENG_NAME, engNameText);

            Label lblMaterialType = createLabel(composite, lblEngName, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_MATERIAL_TYPE));
            materialTypeText = ResourceUtilities.createText(composite, generateFormData(lblMaterialType), getDisplay().getSystemColor(SWT.COLOR_GRAY), false, true, true, true, null);
            componentMap.put(SDVPropertyConstant.SUBSIDIARY_MATERIAL_TYPE, materialTypeText);

            Label lblSubsidiaryGroup = createLabel(composite, lblMaterialType, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP));
            subsidiaryGroupCombo = createCombo(composite, lblSubsidiaryGroup, false, true);
            SDVLOVUtils.comboValueSetting(subsidiaryGroupCombo, registry.getString("SubsidiaryGroup.LOV.NAME"));
            componentMap.put(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP, subsidiaryGroupCombo);

            Label lblQuality = createLabel(composite, lblSubsidiaryGroup, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_PARTQUAL));
            specKorText = ResourceUtilities.createText(composite, generateFormData(lblQuality), getBackground(), true, false, true, true, null);
            componentMap.put(SDVPropertyConstant.SUBSIDIARY_PARTQUAL, specKorText);

            Label lblSpecKor = createLabel(composite, lblQuality, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SPEC_KOR));
            specKorText = ResourceUtilities.createText(composite, generateFormData(lblSpecKor), getBackground(), true, true, true, true, null);
            componentMap.put(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR, specKorText);

            Label lblSpecEng = createLabel(composite, lblSpecKor, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SPEC_ENG));
            specEngText = ResourceUtilities.createText(composite, generateFormData(lblSpecEng), getBackground(), true, false, true, true, null);

            componentMap.put(SDVPropertyConstant.SUBSIDIARY_SPEC_ENG, specEngText);

            Label lblOldPartNo = createLabel(composite, lblSpecEng, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_OLDPART));
            oldPartNoText = ResourceUtilities.createText(composite, generateFormData(lblOldPartNo), getBackground(), true, false, true, true, null);
            componentMap.put(SDVPropertyConstant.SUBSIDIARY_OLDPART, oldPartNoText);

            Label lblUnitOfAmount = createLabel(composite, lblOldPartNo, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT));
            unitOfAmountText = ResourceUtilities.createText(composite, generateFormData(lblUnitOfAmount), getBackground(), true, false, true, true, null);
            componentMap.put(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT, unitOfAmountText);

            Label lblUnitOfPurchasing = createLabel(composite, lblUnitOfAmount, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_BUY_UNIT));
            unitOfPurchasingText = ResourceUtilities.createText(composite, generateFormData(lblUnitOfPurchasing), getBackground(), true, false, true, true, null);
            componentMap.put(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT, unitOfPurchasingText);

            Label lblMaker = createLabel(composite, lblUnitOfPurchasing, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_MAKER));
            makerText = ResourceUtilities.createText(composite, generateFormData(lblMaker), getBackground(), true, false, true, true, null);
            componentMap.put(SDVPropertyConstant.SUBSIDIARY_MAKER, makerText);

            Label lblRemark = createLabel(composite, lblMaker, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_REMARK));
            reMarkText = ResourceUtilities.createText(composite, generateFormData(lblRemark), getBackground(), true, false, true, true, null);
            componentMap.put(SDVPropertyConstant.SUBSIDIARY_REMARK, reMarkText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param composite
     * @param labelName
     * @return Label
     * @throws Exception
     */
    protected Label createLabel(Composite composite, Label previousLabel, String labelName) throws Exception {
        Label label = new Label(composite, SWT.NONE);
        FormData formData = new FormData();
        formData.top = new FormAttachment(previousLabel, 10, SWT.BOTTOM);
        formData.left = new FormAttachment(previousLabel, 0, SWT.LEFT);
        formData.right = new FormAttachment(previousLabel, 0, SWT.RIGHT);
        label.setLayoutData(formData);
        label.setText(labelName);
        return label;
    }

    /**
     * FormData »ý¼º
     * 
     * @param previousLabel
     * @return
     */
    public FormData generateFormData(Label previousLabel) {
        FormData formData = new FormData();
        formData.top = new FormAttachment(previousLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(previousLabel, 5, SWT.RIGHT);
        formData.right = new FormAttachment(100, -5);
        return formData;
    }

    /**
     * @param composite
     * @param previousLabel
     * @param b
     * @param b
     * @return SWTComboBox
     */
    private SWTComboBox createCombo(Composite composite, Label previousLabel, boolean enable, boolean mandatory) {
        SWTComboBox combo = new SWTComboBox(composite, SWT.BORDER);
        FormData formData = generateFormData(previousLabel);
        combo.setLayoutData(formData);
        combo.setEnabled(enable);

        return (SWTComboBox)ResourceUtilities.setMandatory((Control)combo, mandatory);
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        RawDataMap rawDataMap = new RawDataMap();
        HashMap<String, String> itemProperties = new HashMap<String, String>();
        HashMap<String, String> revisionProperties = new HashMap<String, String>();

        for (String key : componentMap.keySet()) {
            Object objec = componentMap.get(key);
            if (key.equals(SDVPropertyConstant.ITEM_ITEM_ID) || key.equals(SDVPropertyConstant.SUBSIDIARY_ENG_NAME)) {
                setMapValue(itemProperties, key, objec);
            } else {
                setMapValue(revisionProperties, key, objec);
            }
        }

        revisionProperties.put(SDVPropertyConstant.ITEM_REVISION_ID, "000");
        
        rawDataMap.put("itemTCCompType", SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM, IData.STRING_FIELD);
        rawDataMap.put("itemProperties", itemProperties, IData.OBJECT_FIELD);
        rawDataMap.put("revisionProperties", revisionProperties, IData.OBJECT_FIELD);

        return rawDataMap;
    }

    /**
     * @param propMap
     * @param key
     * @param objec
     */
    protected void setMapValue(HashMap<String, String> propMap, String key, Object objec) {
        if (objec instanceof SDVText) {
            propMap.put(key, ((SDVText) objec).getText());
        }

        if (objec instanceof SWTComboBox) {
            String value = "";
            SWTComboBox comboBox = (SWTComboBox) objec;
            Object selectedItem = comboBox.getSelectedItem();
            if (selectedItem != null) {
                value = selectedItem.toString();
            }
            propMap.put(key, value);
        }
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        Map<String, Object> paramMap = getParameters();

        if (paramMap.containsKey("paramKey")) {
            RawDataMap rawDataMap = (RawDataMap) paramMap.get("paramKey");
            for (String key : rawDataMap.keySet()) {
                Object objec = componentMap.get(key);

                if (objec instanceof SDVText) {
                    ((SDVText) objec).setText(rawDataMap.getStringValue(key));
                }

                if (objec instanceof SWTComboBox) {
                    ((SWTComboBox) objec).setSelectedItem((rawDataMap.getStringValue(key)));
                }
            }
        }

        return null;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

    @Override
    public void uiLoadCompleted() {

    }
}
