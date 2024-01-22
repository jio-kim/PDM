/**
 * 
 */
package com.symc.plm.me.sdv.view.plant;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.ssangyong.common.utils.SYMDisplayUtil;
import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * Class Name : CreateBodyShopView
 * Class Description :
 * 
 * @date 2013. 11. 1.
 * 
 */
public class CreatePlantView extends AbstractSDVViewPane {

    private Registry registry;

    private Composite topComposite;

    private SDVText plantCodeText;

    private SDVText stationCodeText;

    protected SDVText WorkareaCodeText;

    private SWTComboBox shopCodeCombo;

    private SWTComboBox lineCodeCombo;

    private Button btnCheckAlt;

    private SDVText textAltPrefix;

    private ControlDecoration decoration;

    private boolean shopFlag;

    private boolean lineFlag;

    private boolean stationFlag;

    private boolean workareaFlag;

    private LinkedHashMap<String, Object> idComponentMap;
    private LinkedHashMap<String, Object> propertyComponentMap;

    public SDVText getStationCodeText() {
        return stationCodeText;
    }

    public void setStationCodeTextValue(String strValue) {
        this.stationCodeText.setText(strValue);
    }

    public SDVText getWorkareaCodeText() {
        return WorkareaCodeText;
    }

    public void setWorkareaCodeTextValue(String strValue) {
        WorkareaCodeText.setText(strValue);
    }

    public SWTComboBox getShopCodeCombo() {
        return shopCodeCombo;
    }

    public void setShopCodeComboValue(int selectedLine) {
        this.shopCodeCombo.setSelectedIndex(selectedLine);
    }

    public SWTComboBox getLineCodeCombo() {
        return lineCodeCombo;
    }

    public void setLineCodeComboValue(int selectedLine) {
        this.lineCodeCombo.setSelectedIndex(selectedLine);
    }

    /**
     * @param parent
     * @param style
     * @param id
     */
    public CreatePlantView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @SuppressWarnings("unused")
    @Override
    protected void initUI(Composite parent) {
        String dialogId = UIManager.getCurrentDialog().getId();
        // symc.me.plant.CreateShopItemDialog
        shopFlag = false;
        lineFlag = false;
        stationFlag = false;
        workareaFlag = false;

        if (StringUtils.containsIgnoreCase(dialogId, "shop")) {
            shopFlag = true;
        }
        if (StringUtils.containsIgnoreCase(dialogId, "line")) {
            lineFlag = true;
        }
        if (StringUtils.containsIgnoreCase(dialogId, "station")) {
            stationFlag = true;
        }
        if (StringUtils.containsIgnoreCase(dialogId, "workarea")) {
            workareaFlag = true;
        }

        try {
            registry = Registry.getRegistry(this);
            idComponentMap = new LinkedHashMap<String, Object>();
            propertyComponentMap = new LinkedHashMap<String, Object>();

            topComposite = new Composite(parent, SWT.NONE);

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.verticalSpacing = 10;
            gridLayout.horizontalSpacing = 20;
            gridLayout.marginLeft = 10;
            gridLayout.marginRight = 10;
            gridLayout.marginHeight = 10;
            topComposite.setLayout(gridLayout);

            if (shopFlag || lineFlag) {
                Label lblPlantCode = drawLabel(topComposite, registry.getString("Plant.PlantCode.NAME"));
                plantCodeText = drawText(topComposite, registry.getString("Plant.PlantCode.NAME"), false, true);
                plantCodeText.setText("PTP");
            }

            Label lblShopCode = drawLabel(topComposite, registry.getString("Plant.ShopCode.NAME"));
            shopCodeCombo = drawCombo(topComposite, registry.getString("Plant.ShopCode.NAME"), true);
            SDVLOVUtils.comboValueSetting(shopCodeCombo, "M7_BOPA_SHOP_CODE");
            SDVLOVUtils.comboValueSetting(shopCodeCombo, "M7_BOPB_SHOP_CODE");
            if (shopFlag) {
                shopCodeCombo.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
            }

            if (!shopFlag) {
                Label lblLineCode = drawLabel(topComposite, registry.getString("Plant.LineCode.NAME"));
                lineCodeCombo = drawCombo(topComposite, registry.getString("Plant.LineCode.NAME"), true);
                if (lineFlag) {
                    lineCodeCombo.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                }
            }

            if (stationFlag || workareaFlag) {
                Label lblStationCode = drawLabel(topComposite, registry.getString("Plant.StationCode.NAME"));
                stationCodeText = drawText(topComposite, registry.getString("Plant.StationCode.NAME"), true, true);
                ResourceUtilities.setSDVTextListener(stationCodeText, true, true, String.valueOf(3));

                // Station 화면일 경우 ALT 속성 입력 필드 추가
                if (stationFlag) {
                    drawAlt(topComposite);
                }
            }

            if (workareaFlag) {
                Label lblWorkareaCode = drawLabel(topComposite, registry.getString("Plant.Workarea.NAME"));
                WorkareaCodeText = drawText(topComposite, registry.getString("Plant.Workarea.NAME"), true, true);
                ResourceUtilities.setSDVTextListener(stationCodeText, true, true, String.valueOf(2));
                WorkareaCodeText.setFocus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Label 생성 함수
     * 
     * @param composite
     * @param labelName
     * @return Label
     * @throws Exception
     */
    protected Label drawLabel(Composite composite, String labelName) throws Exception {
        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        label.setText(labelName);
        return label;
    }

    /**
     * Text 생성 함수
     * 
     * @param composite
     * @param textName
     * @param previousText
     * @return SDVText
     */
    protected SDVText drawText(Composite composite, String textName, boolean editable, boolean mandatory) {
        SDVText text = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        text.setInputType(SDVText.ENGUPPERNUM);
        ResourceUtilities.setSDVTextListener(text, true, true, null);
        if (!editable) {
            text.setEnabled(false);
            text.setBackground(getBackground());
        }

        if (mandatory) {
            text.setMandatory(mandatory);
        }

        idComponentMap.put(textName, text);
        return text;
    }

    /**
     * Combo 생성 함수
     * 
     * @param composite
     * @param comboName
     * @param lovName
     * @return SWTComboBox
     */
    protected SWTComboBox drawCombo(Composite composite, String comboName, boolean mandatory) {
        return drawCombo(composite, comboName, "", mandatory);
    }

    protected SWTComboBox drawCombo(Composite composite, String comboName, String lovName, boolean mandatory) {
        final SWTComboBox combo = new SWTComboBox(composite, SWT.BORDER);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        if (lovName != null && !lovName.isEmpty()) {
            SDVLOVUtils.comboValueSetting(combo, lovName);
        }

        if (mandatory) {
            ResourceUtilities.setMandatory(combo, mandatory);
        }

        idComponentMap.put(comboName, combo);
        return combo;
    }

    /**
     * ALT Plant 필드 생성 함수
     * (workarea 생성 화면에서만 사용한다)
     */
    private void drawAlt(Composite composite) {
        btnCheckAlt = new Button(composite, SWT.CHECK);
        // btnCheckAlt.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_IS_ALTBOP));
        btnCheckAlt.setText(registry.getString("Plant.IsAltPlant.Name", "Is Alt Plant"));
        btnCheckAlt.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getAvailableDialog("symc.me.bop.CreateStationItemDialog");

                if (((Button) arg0.widget).getSelection()) {
                    dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));
                    dialog.setAddtionalTitle("Alternative Plant");
                    textAltPrefix.setText("ALT");
                    textAltPrefix.setEnabled(true);
                    decoration.show();
                    textAltPrefix.redraw();
                } else {
                    dialog.setTitleBackground(null);
                    dialog.setAddtionalTitle("");
                    textAltPrefix.setText("");
                    textAltPrefix.setEnabled(false);
                    decoration.hide();
                    textAltPrefix.redraw();
                }
            }
        });

        textAltPrefix = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        textAltPrefix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textAltPrefix.setEnabled(false);
        textAltPrefix.setTextLimit(4);
        textAltPrefix.setInputType(SDVText.ENGUPPERNUM);
        decoration = SYMDisplayUtil.setRequiredFieldSymbol(textAltPrefix);
        decoration.hide();
        textAltPrefix.redraw();

        propertyComponentMap.put(registry.getString("Plant.IsAltPlant.Name", "Is Alt Plant"), btnCheckAlt);
        propertyComponentMap.put(registry.getString("Plant.AltPrefix.NAME", "Alt Prefix"), textAltPrefix);
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
        LinkedHashMap<String, String> idStrValueMap = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> propertyStrValueMap = new LinkedHashMap<String, String>();

        // ID 정보 수집하여 String Value를 idStrValueMap에 셋팅
        for (String key : idComponentMap.keySet()) {
            Object objec = idComponentMap.get(key);
            idStrValueMap.put(key, getStringValue(objec));
        }

        // Property 정보 수집 String Value를 propertyStrValueMap에 셋팅
        for (String key : propertyComponentMap.keySet()) {
            Object objec = propertyComponentMap.get(key);
            propertyStrValueMap.put(key, getStringValue(objec));
        }

        RawDataMap rawDataMap = new RawDataMap();
        rawDataMap.put("id", idStrValueMap, IData.OBJECT_FIELD);
        rawDataMap.put("property", propertyStrValueMap, IData.OBJECT_FIELD);

        return rawDataMap;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        Object targetObjet = null;
        Map<String, Object> paramMap = getParameters();
        if (paramMap != null) {
            // if (paramMap.containsKey("targetItemRevision")) {
            targetObjet = paramMap.get("targetItemRevision");
            // }
        }
        // Dialog Open시 기본 값 셋팅
        setDefaultValue(targetObjet);

        return null;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

    @Override
    public void uiLoadCompleted() {

    }

    /**
     * Control 을 Type(SDVText, SWTComboBox)에 따라 String Value를 뽑아내는 함수
     * 
     * @param propMap
     * @param key
     * @param object
     */
    protected String getStringValue(Object object) {
        String codeValue = null;
        if (object instanceof SDVText) {
            codeValue = ((SDVText) object).getText();
        }

        if (object instanceof SWTComboBox) {
            SWTComboBox comboBox = (SWTComboBox) object;
            Object selectedItem = comboBox.getSelectedItem();
            if (selectedItem != null) {
                codeValue = selectedItem.toString();
            }
        }

        if (object instanceof Button) {
            Button button = (Button) object;
            codeValue = (new Boolean(button.getSelection())).toString();
        }
        
        return codeValue;
    }

    /**
     * Dialog에 Default값 입력하는 함수 (현재 선택된 Item의 ID를 Dialog 속성에 반영)
     * 
     * @param targetObjet
     */
    public void setDefaultValue(Object targetObjet) {
        if (targetObjet != null) {
            TCComponentItemRevision targetRevision = (TCComponentItemRevision) targetObjet;
            try {
                Boolean isAltPlant = targetRevision.getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);
                String altPrefix = targetRevision.getProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX);
                String[] idCrumb = targetRevision.getProperty("item_id").split("-");
                if (idCrumb != null) {
                    int arrLength = idCrumb.length;
                    for (int i = 0; i < arrLength; i++) {
                        // Line 생성시만!
                        if (lineFlag) {
                            if (i == 0) {
                                plantCodeText.setText(idCrumb[i]);
                            } else if (i == 1) {
                                shopCodeCombo.setSelectedItem(idCrumb[i]);
                                shopCodeCombo.setEnabled(false);
                                setLineCodeLov(idCrumb, i);
                            }
                            // Station 생성시만!
                        } else if (stationFlag) {
                            if (i == 1) {
                                shopCodeCombo.setSelectedItem(idCrumb[i]);
                                shopCodeCombo.setEnabled(false);
                                setLineCodeLov(idCrumb, i);
                            } else if (i == 2) {
                                lineCodeCombo.setSelectedItem(idCrumb[i]);
                                lineCodeCombo.setEnabled(false);
                            }
                            // Workarea 생성시만!
                        } else if (workareaFlag) {
                            if (!isAltPlant) {
                                if (i == 0) {
                                    shopCodeCombo.setSelectedItem(idCrumb[i]);
                                    shopCodeCombo.setEnabled(false);
                                    setLineCodeLov(idCrumb, i);
                                } else if (i == 1) {
                                    lineCodeCombo.setSelectedItem(idCrumb[i]);
                                    lineCodeCombo.setEnabled(false);
                                } else if (i == 2) {
                                    stationCodeText.setText(idCrumb[i]);
                                    stationCodeText.setEditable(false);
                                    stationCodeText.setBackground(getBackground());
                                }

                                // Alt WorkArea 생성시 ID 값 지정 (Station이 Alt인 경우에 해당됨)
                            } else {
                                if (i == 1) {
                                    shopCodeCombo.setSelectedItem(idCrumb[i]);
                                    shopCodeCombo.setEnabled(false);
                                    setLineCodeLov(idCrumb, i);
                                } else if (i == 2) {
                                    lineCodeCombo.setSelectedItem(idCrumb[i]);
                                    lineCodeCombo.setEnabled(false);
                                } else if (i == 3) {
                                    stationCodeText.setText(idCrumb[i]);
                                    stationCodeText.setEditable(false);
                                    stationCodeText.setBackground(getBackground());
                                }
                            }
                        }
                    }

                    // Alt WorkArea 생성시 속성 지정 (Station이 Alt인 경우에 해당됨)
                    if (workareaFlag && isAltPlant) {
                        AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getAvailableDialog("symc.me.bop.CreateWorkareaItemDialog");
                        dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));
                        dialog.setAddtionalTitle("Alternative Plant");

                        SDVText textAltPrefix = new SDVText(topComposite, SWT.BORDER | SWT.SINGLE);
                        textAltPrefix.setText(altPrefix);
                        textAltPrefix.setVisible(false);
                        propertyComponentMap.put(registry.getString("Plant.AltPrefix.NAME", "Alt Prefix"), textAltPrefix);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param idCrumb
     * @param i
     */
    public void setLineCodeLov(String[] idCrumb, int i) {
        String bopType = idCrumb[i].substring(0, 1);
        if (bopType.equals("A") || bopType.equals("Z")) {
            SDVLOVUtils.comboValueSetting(lineCodeCombo, "M7_BOPA_LINE_CODE");
        } else if (bopType.equals("B") || bopType.equals("X")) {
            SDVLOVUtils.comboValueSetting(lineCodeCombo, "M7_BOPB_LINE_CODE");
        }
        // else {
        // SDVLOVUtils.comboValueSetting(lineCodeCombo, "M7_BOPP_LINE_CODE");
        // }
    }
}
