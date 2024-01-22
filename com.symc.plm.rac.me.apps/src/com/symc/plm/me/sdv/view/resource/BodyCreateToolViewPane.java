/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;

/**
 * Class Name : Equipment_Creation_Properties
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public class BodyCreateToolViewPane extends CreateResourceViewPane {

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public BodyCreateToolViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /**
     * Initiation ID Field (ID 표시 영역 생성)
     */
    @Override
    protected void initIdField(Composite topComposite) {
        super.initIdField(topComposite);
        drawIdField(4); // 차체공구 (4칸)
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

        drawBlankLabel(composite, registry.getString("Label.Blank.NAME")); // 정렬을 맞추기 위해 빈 Label 추가함
        final String mainClassLovName = getChildLovName(resourceCategory);
        main_class = drawPropCombo(composite, registry.getString("MainClass.NAME"), "main_class", mainClassLovName, true);
        main_class.setSelectedIndex(0);

        String selectedValue = main_class.getSelectedItem().toString();
        String subClassLovName = getChildLovName(selectedValue, mainClassLovName);
        sub_class = drawPropCombo(composite, registry.getString("SubClass.NAME"), "sub_class", subClassLovName, true);
//        sub_class.setSelectedIndex(0);
        sub_class.setText("");

        idMap.get(1).setText(selectedValue);
        idMap.get(2).setText("");

        spec_code = drawPropCombo(composite, registry.getString("SpecCode.NAME"), "spec_code", getChildLovName("COM", subClassLovName), true);
         

        main_class.addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
            }
        });

        sub_class.addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {              
                idMap.get(2).setText(sub_class.getSelectedItem().toString());
            }
        });
        
        spec_code.addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {              
                idMap.get(3).setText(spec_code.getSelectedItem().toString());
            }
        });
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
        object_name = drawPropText(composite, registry.getString("KorName.Name"), "object_name", 1, true);
        eng_name = drawPropText(composite, registry.getString("EngName.NAME"), "eng_name", 1, true);
        spec_kor = drawPropText(composite, registry.getString("SpecKor.NAME"), "spec_kor", 1, true);
        spec_eng = drawPropText(composite, registry.getString("SpecEng.NAME"), "spec_eng", 1, true);
        torque_value = drawPropText(composite, registry.getString("TorqueValue.NAME"), "torque_value", 1, false);
        purpose = drawPropText(composite, registry.getString("Purpose.NAME"), "purpose", 1, true);
        unit_usage = drawPropCombo(composite, registry.getString("UnitUsage.NAME"), "unit_usage", registry.getString("UnitUsage.LOV.NAME"), false);
        material = drawPropText(composite, registry.getString("Material.NAME"), "material", 1, false);
        maker = drawPropText(composite, registry.getString("Maker.NAME"), "maker", 1, false);
        drawBlankLabel(composite, registry.getString("Label.Blank.NAME")); // 정렬을 맞추기 위해 빈 Label 추가함
        object_desc = drawPropText(composite, registry.getString("Description.NAME"), "object_desc", 3, false);
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
            main_class.setEnabled(false);
            main_class.getTextField().setBackground(getBackground());
            sub_class.setEnabled(false);
            sub_class.getTextField().setBackground(getBackground());
            spec_code.setEnabled(false);
            spec_code.getTextField().setBackground(getBackground());
            
  
            String itemId = paramRevisionProperties.get(SDVPropertyConstant.ITEM_ITEM_ID);
            String revisionId = paramRevisionProperties.get(SDVPropertyConstant.ITEM_REVISION_ID);
            String strNewRevisionId = ResourceUtilities.getNewRevisionId(revisionId);
            
            setItemId(itemId, strNewRevisionId);
            setControlValue(itemPropMap, paramItemProperties, resourceType);
            setControlValue(revisionPropMap, paramRevisionProperties, resourceType);
        }
    }
}
