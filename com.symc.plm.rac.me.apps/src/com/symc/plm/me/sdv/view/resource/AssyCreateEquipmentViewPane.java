/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;

/**
 * Class Name : Equipment_Creation_Properties
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public class AssyCreateEquipmentViewPane extends CreateResourceViewPane {

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public AssyCreateEquipmentViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /**
     * Initiation ID Field (ID 표시 영역 생성)
     */
    @Override
    protected void initIdField(Composite topComposite) {
        super.initIdField(topComposite);
        drawIdField(5); // 조립설비, 조립JIG (5칸)
    }

    /**
     * Initiation Property Field (Property 영역 생성)
     */
    @Override
    protected void initPropertyField(Composite topComposite) {
        super.initPropertyField(topComposite);
    }

    /**
     * Set Mandatory Properties
     * 
     * @param composite
     * @param bopType
     *            A : ASSY, B : BODY, P : PAINT
     * @param resourceType
     *            Value is "Equip" or "Tool"
     * @param resourceCategory
     *            EXT : 일반설비 또는 공구, JIG : Jig & Fixture, ROB : Robot, GUN : Gun, SOC : Socket공구 ...
     */
    @Override
    protected void initMandatoryProperties(Composite composite, String bopType, String resourceType, String resourceCategory) {
        resource_category = drawPropCombo(composite, registry.getString("ResourceCategory.NAME"), "resource_category", getDefaultLovName(), true);
        resource_category.setEnabled(false);
        resource_category.getTextField().setBackground(getBackground());
        resource_category.setSelectedItem(resourceCategory);

        shop_code = drawPropCombo(composite, registry.getString("ShopCode.NAME"), "shop_code", registry.getString("Assy.ShopCode.LOV.NAME"), true);

        final String mainClassLovName = getChildLovName(resourceCategory);
        // 조립 일반설비 Only
        if (resourceCategory.equalsIgnoreCase(registry.getString("Resource.Category.EXT"))) {
            main_class = drawPropCombo(composite, registry.getString("MainClass.NAME"), "main_class", mainClassLovName, true);
            sub_class = drawPropCombo(composite, registry.getString("SubClass.NAME"), "sub_class", true);

            main_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    String seletedValue = main_class.getSelectedItem().toString();
                    final String subClassLovName = getChildLovName(seletedValue, mainClassLovName);

                    sub_class.removeAllItems();
                    sub_class = SDVLOVUtils.comboValueSetting(sub_class, subClassLovName);
                    sub_class.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                    // sub_class.setSelectedIndex(0);
                    sub_class.setText("");
                    idMap.get(2).setText(seletedValue);
                    idMap.get(3).setText("");
                }
            });

            sub_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(3).setText(sub_class.getSelectedItem().toString());
                }
            });
        }

        // 조립 JIG설비 Only
        if (resourceCategory.equals(registry.getString("Resource.Category.JIG"))) {
            vehicle_code = drawPropCombo(composite, registry.getString("VehicleCode.NAME"), "vehicle_code", registry.getString("VehicleCode.LOV.NAME"), true);
            main_class = drawPropCombo(composite, registry.getString("MainClass.NAME"), "main_class", mainClassLovName, true);

            vehicle_code.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(2).setText(vehicle_code.getSelectedItem().toString());
                }
            });

            main_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(3).setText(main_class.getSelectedItem().toString());
                }
            });
        }
    }

    /**
     * Set Additional Properties
     * 
     * @param composite
     * @param resourceType
     *            Value is "Equip" or "Tool"
     */
    @Override
    protected void initAdditionalProperties(Composite composite, String resourceType) {
        // Common
        object_name = drawPropText(composite, registry.getString("KorName.Name"), "object_name", 1, true);
        eng_name = drawPropText(composite, registry.getString("EngName.NAME"), "eng_name", 1, true);
        spec_kor = drawPropText(composite, registry.getString("SpecKor.NAME"), "spec_kor", 1, true);
        spec_eng = drawPropText(composite, registry.getString("SpecEng.NAME"), "spec_eng", 1, true);
        purpose_kor = drawPropText(composite, registry.getString("PurposeKor.NAME"), "purpose_kor", 1, true);
        purpose_eng = drawPropText(composite, registry.getString("PurposeEng.NAME"), "purpose_eng", 1, true);
        capacity = drawPropText(composite, registry.getString("Capacity.NAME"), "capacity", 1, false);
        install_year = drawPropText(composite, registry.getString("InstallYear.NAME"), "install_year", 1, false);
        maker = drawPropText(composite, registry.getString("Maker.NAME"), "maker", 1, false);
        nation = drawPropText(composite, registry.getString("Nation.NAME"), "nation", 1, false);
        rev_desc = drawPropText(composite, registry.getString("RevDesc.NAME"), "rev_desc", 3, false);
        object_desc = drawPropText(composite, registry.getString("Description.NAME"), "object_desc", 3, false);

        ResourceUtilities.setNumeric(capacity, 10);
        ResourceUtilities.setNumeric(install_year, 4);
    }

    @Override
    public void uiLoadCompleted() {
        // Revise화면일 경우 선택된 Item의 속성으로 화면 셋팅
        if (!createMode) {
            //공통
            for(int key : idMap.keySet()) {
                idMap.get(key).setEnabled(false);
                idMap.get(key).setBackground(getBackground());
            }
            resource_category.setEnabled(false);
            resource_category.getTextField().setBackground(getBackground());
            shop_code.setEnabled(false);
            shop_code.getTextField().setBackground(getBackground());
            main_class.setEnabled(false);
            main_class.getTextField().setBackground(getBackground());
            
            // 조립 일반설비 Only
            if (resourceCategory.equalsIgnoreCase(registry.getString("Resource.Category.EXT"))) {
                String mainClassValue = paramRevisionProperties.get(SDVPropertyConstant.EQUIP_MAIN_CLASS);
                String subClassLovName = getChildLovName(mainClassValue, getChildLovName(resourceCategory));
                SDVLOVUtils.comboValueSetting(sub_class, subClassLovName);

                sub_class.setEnabled(false);
                sub_class.getTextField().setBackground(getBackground());
            }

            // 조립 JIG설비 Only
            if (resourceCategory.equals(registry.getString("Resource.Category.JIG"))) {
                vehicle_code.setEnabled(false);
                vehicle_code.getTextField().setBackground(getBackground());
            }
            
            String itemId = paramRevisionProperties.get(SDVPropertyConstant.ITEM_ITEM_ID);
            String revisionId = paramRevisionProperties.get(SDVPropertyConstant.ITEM_REVISION_ID);
            String strNewRevisionId = ResourceUtilities.getNewRevisionId(revisionId);
            
            setItemId(itemId, strNewRevisionId);
            setControlValue(itemPropMap, paramItemProperties, resourceType);
            setControlValue(revisionPropMap, paramRevisionProperties, resourceType);
        }
    }
}
