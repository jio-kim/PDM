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
public class PaintCreateToolViewPane extends CreateResourceViewPane {

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public PaintCreateToolViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /**
     * Initiation ID Field (ID 표시 영역 생성)
     */
    @Override
    protected void initIdField(Composite topComposite) {
        super.initIdField( topComposite);
        if (resourceCategory.equals(registry.getString("Resource.Category.EXT"))) {
            drawIdField(4); // 도장공구 (4칸)
        }
        if (resourceCategory.equals(registry.getString("Resource.Category.STY"))) {
            drawIdField(5); // 도장 STAY (5칸)
        }
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
        // 일반공구
        if (resourceCategory.equals(registry.getString("Resource.Category.EXT"))) {
            drawBlankLabel(composite, registry.getString("Label.Blank.NAME")); // 정렬을 맞추기 위해 빈 Label 추가함
            final String mainClassLovName = getChildLovName(resourceCategory);
            main_class = drawPropCombo(composite, registry.getString("MainClass.NAME"), "main_class", mainClassLovName, true);
            main_class.setSelectedIndex(0);

            String selectedValue = main_class.getSelectedItem().toString();
            sub_class = drawPropCombo(composite, registry.getString("SubClass.NAME"), "sub_class", getChildLovName(selectedValue, mainClassLovName), true);

            idMap.get(1).setText(selectedValue);

            main_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    String selectedValue = main_class.getSelectedItem().toString();
                    final String subClassLovName = getChildLovName(selectedValue, mainClassLovName);
                    sub_class.removeAllItems();
                    sub_class = SDVLOVUtils.comboValueSetting(sub_class, subClassLovName);
                    sub_class.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                    idMap.get(1).setText(selectedValue);
                    if (ResourceUtilities.checkID(idMap, 1, 3)) {
                        idMap.get(3).setText(createSeqNum("2"));
                    }
                }
            });

            sub_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(2).setText(sub_class.getSelectedItem().toString());
                    if (ResourceUtilities.checkID(idMap, 1, 3)) {
                        idMap.get(3).setText(createSeqNum("2"));
                    }
                }
            });

        }

        // STAY공구
        if (resourceCategory.equals(registry.getString("Resource.Category.STY"))) {
            // 차종
            vehicle_code = drawPropCombo(composite, registry.getString("VehicleCode.NAME"), "vehicle_code", registry.getString("VehicleCode.LOV.NAME"), true);
            // 메인
            final String mainClassLovName = getChildLovName(resourceCategory);
            main_class = drawPropCombo(composite, registry.getString("MainClass.NAME"), "main_class", mainClassLovName, true);
            main_class.setSelectedIndex(0);
            // 서브
            text_sub_class = drawPropText(composite, registry.getString("SubClass.NAME"), "text_sub_class", 1, true);
            text_sub_class.setEditable(false);
            text_sub_class.setBackground(getBackground());
            
            // STAY,STOPPER,SUPPORT
            String selectedValue = main_class.getSelectedItem().toString();
            String stayTypeLovName = getChildLovName(selectedValue, mainClassLovName);
            stay_type = drawPropCombo(composite, registry.getString("StayType.NAME"), "stay_type", stayTypeLovName, true);
//            stay_type.setSelectedIndex(0);
            // 용도
            String specCodeLovName = getChildLovName("COM", stayTypeLovName);
            spec_code = drawPropCombo(composite, registry.getString("SpecCode.NAME"), "spec_code", specCodeLovName, true);
//            spec_code.setSelectedIndex(0);
//            text_sub_class.setText(stay_type.getSelectedItem().toString() + spec_code.getSelectedItem().toString());
            // 위치 구분
            String stayAreaLovName = getChildLovName("PT", mainClassLovName);
            stay_area = drawPropCombo(composite, registry.getString("StayArea.NAME"), "stay_area", stayAreaLovName, true);

//            idMap.get(1).setText(selectedValue);
//            idMap.get(2).setText(vehicle_code.getSelectedItem().toString());
//            idMap.get(3).setText(text_sub_class.getText());
//            idMap.get(4).setText(stay_area.getSelectedItem().toString());
//            if (checkID(1, 2, 3, 1)) {
//                idMap.get(5).setText(createSeqNum());
//            }

            vehicle_code.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(2).setText(vehicle_code.getSelectedItem().toString());
                    if(stay_area.getSelectedItem() != null) {
                        if (ResourceUtilities.checkID(idMap, 1, 2, 3)) {
                            idMap.get(4).setText(createSeqNum("2", stay_area.getSelectedItem().toString()));
                        }                           
                    }
                }
            });

            main_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(1).setText(main_class.getSelectedItem().toString());
                }
            });

            stay_type.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    Object specCode = spec_code.getSelectedItem();
                    text_sub_class.setText(stay_type.getSelectedItem().toString() + ((specCode != null) ? specCode.toString() : ""));
                    idMap.get(3).setText(text_sub_class.getText());
                    if(stay_area.getSelectedItem() != null) {
                        if (ResourceUtilities.checkID(idMap, 1, 2, 3)) {
                            idMap.get(4).setText(createSeqNum("2", stay_area.getSelectedItem().toString()));
                        }                           
                    }               
                }
            });
            
            spec_code.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    Object stayType = stay_type.getSelectedItem();
                    text_sub_class.setText(((stayType != null) ? stayType.toString() : "") + spec_code.getSelectedItem().toString());
                    idMap.get(3).setText(text_sub_class.getText());
                    if(stay_area.getSelectedItem() != null) {
                        if (ResourceUtilities.checkID(idMap, 1, 2, 3)) {
                            idMap.get(4).setText(createSeqNum("2", stay_area.getSelectedItem().toString()));
                        }                           
                    }                 
                }
            });
            
            stay_area.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    String selectedValue = stay_area.getSelectedItem().toString();
                    if (ResourceUtilities.checkID(idMap, 1, 2, 3)) {
                        idMap.get(4).setText(createSeqNum("2", selectedValue));
                    }                    
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
        object_name = drawPropText(composite, registry.getString("KorName.Name"), "object_name", 1, true);
        eng_name = drawPropText(composite, registry.getString("EngName.NAME"), "eng_name", 1, true);
        spec_kor = drawPropText(composite, registry.getString("SpecKor.NAME"), "spec_kor", 1, true);
        spec_eng = drawPropText(composite, registry.getString("SpecEng.NAME"), "spec_eng", 1, true);
        torque_value = drawPropText(composite, registry.getString("TorqueValue.NAME"), "torque_value", 1, false);
        purpose = drawPropText(composite, registry.getString("Purpose.NAME"), "purpose", 1, true);
        unit_usage = drawPropCombo(composite, registry.getString("UnitUsage.NAME"), "unit_usage", "M7_RESOURCE_UNIT_USAGE", false);
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
            
            // 도장 일반설비 Only
            if (resourceCategory.equalsIgnoreCase(registry.getString("Resource.Category.EXT"))) {
                String mainClassValue = paramRevisionProperties.get(SDVPropertyConstant.EQUIP_MAIN_CLASS);
                String subClassLovName = getChildLovName(mainClassValue, getChildLovName(resourceCategory));
                SDVLOVUtils.comboValueSetting(sub_class, subClassLovName);

                sub_class.setEnabled(false);
                sub_class.getTextField().setBackground(getBackground());
            }

            // 도장 STAY설비 Only
            if (resourceCategory.equals(registry.getString("Resource.Category.STY"))) {
                vehicle_code.setEnabled(false);
                vehicle_code.getTextField().setBackground(getBackground());
                text_sub_class.setEnabled(false);
                text_sub_class.setBackground(getBackground());
                stay_type.setEnabled(false);
                stay_type.getTextField().setBackground(getBackground());
                spec_code.setEnabled(false);
                spec_code.getTextField().setBackground(getBackground());
                stay_area.setEnabled(false);
                stay_area.getTextField().setBackground(getBackground());
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
