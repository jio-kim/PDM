/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
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
 *       [SR140512-016][20140512] shcho, 차체 Resource ID 체계 변경
 *       LOV를 Vehicle Code -> Serial Character 변경 (대상 : 차체일반설비, 차체로봇부대설비, 차체 JIG)
 *       주의 : 저장되는 속성은 기존의 m7_VEHICLE_CODE를 그대로 사용한다.
 *       ID체계 4번째 필드 구성은 ShopCode + Serial Character + Station Code 의 조합 6자리로로 한다. (예 : B1A301)
 *       
 *       [SR150609-023][20150609] shcho, Maker고유번호 입력 시 특수문자 '-' 는 사용 못하도록 막음
 *       
 * 
 */
public class BodyCreateEquipmentViewPane extends CreateResourceViewPane {
    
    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public BodyCreateEquipmentViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /**
     * Initiation ID Field (ID 표시 영역 생성)
     */
    @Override
    protected void initIdField(Composite topComposite) {
        super.initIdField(topComposite);
        if (resourceCategory.equals(registry.getString("Resource.Category.EXT")) || resourceCategory.equals(registry.getString("Resource.Category.ROE")) || resourceCategory.equals(registry.getString("Resource.Category.JIG"))) {
            drawIdField(6); // 차체 일반설비, 로봇부대설비, JIG (6칸)
        } else {
            drawIdField(5); // 차체Robot, 차체Gun (5칸)
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
        resource_category.setSelectedItem(resourceCategory);

        shop_code = drawPropCombo(composite, registry.getString("ShopCode.NAME"), "shop_code", registry.getString("Body.ShopCode.LOV.NAME"), true);

        final String mainClassLovName = getChildLovName(resourceCategory);
        main_class = drawPropCombo(composite, registry.getString("MainClass.NAME"), "main_class", mainClassLovName, true);

        // 차체일반설비,로봇부대설비, JIG
        if (resourceCategory.equals(registry.getString("Resource.Category.EXT")) || resourceCategory.equals(registry.getString("Resource.Category.ROE")) || resourceCategory.equals(registry.getString("Resource.Category.JIG"))) {
            main_class.setSelectedIndex(0);
            String selectedValue = main_class.getSelectedItem().toString();
            idMap.get(2).setText(selectedValue);

            sub_class = drawPropCombo(composite, registry.getString("SubClass.NAME"), "sub_class", getChildLovName(selectedValue, mainClassLovName), true);
            // sub_class.setSelectedIndex(0);
            // idMap.get(3).setText(sub_class.getSelectedItem().toString());

            // [SR140512-016][20140512] shcho, 차체 Resource ID 체계 변경 (LOV를 Vehicle Code -> Serial Character 변경) ----------------------------------------------------------
            vehicle_code = drawPropCombo(composite, registry.getString("SerialCharacter.NAME"), "vehicle_code", registry.getString("SerialCharacter.LOV.NAME"), true);
            // -----------------------------------------------------------------------------------------------------------------------------------------------------------

            station_code = drawPropText(composite, registry.getString("StationCode.NAME"), "station_code", 1, true);
            position_code = drawPropText(composite, registry.getString("PositionCode.NAME"), "position_code", 1, true);
            ResourceUtilities.setNumeric(station_code, 3);
            ResourceUtilities.setTextLength(position_code, 2);

            //[SR140512-016][20140512] shcho, 차체 Resource ID 체계 변경 (4번째 ID필드에 ShopCode + Serial Character + Station Code 의 조합 6자리 지정) --------------
            shop_code.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    Object vehicleCodeValue = vehicle_code.getSelectedItem();
                    idMap.get(4).setText(shop_code.getSelectedItem().toString() + ((vehicleCodeValue != null) ? vehicleCodeValue.toString() : "") + station_code.getText());
                }
            });
            // -----------------------------------------------------------------------------------------------------------------------------------------------------

            main_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    String selectedValue = main_class.getSelectedItem().toString();
                    final String subClassLovName = getChildLovName(selectedValue, mainClassLovName);
                    sub_class.removeAllItems();
                    sub_class = SDVLOVUtils.comboValueSetting(sub_class, subClassLovName);
                    sub_class.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                    // sub_class.setSelectedIndex(0);
                    // sub_class.setText("");
                    idMap.get(2).setText(selectedValue);
                    // idMap.get(3).setText("");
                }
            });

            sub_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(3).setText(sub_class.getSelectedItem().toString());
                }
            });

            vehicle_code.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    //[SR140512-016][20140512] shcho, 차체 Resource ID 체계 변경 (4번째 ID필드에 ShopCode + Serial Character + Station Code 의 조합 6자리 지정) --------------
                    Object shopCodeValue = shop_code.getSelectedItem();
                    idMap.get(4).setText(((shopCodeValue != null) ? shopCodeValue.toString() : "") + vehicle_code.getSelectedItem().toString() + station_code.getText());
                    // -----------------------------------------------------------------------------------------------------------------------------------------------------
                }
            });

            station_code.addKeyListener(new KeyListener() {
                @Override
                public void keyReleased(KeyEvent e) {
                    //[SR140512-016][20140512] shcho, 차체 Resource ID 체계 변경 (4번째 ID필드에 ShopCode + Serial Character + Station Code 의 조합 6자리 지정) --------------
                    Object shopCodeValue = shop_code.getSelectedItem();
                    Object vehicleCodeValue = vehicle_code.getSelectedItem();
                    idMap.get(4).setText(((shopCodeValue != null) ? shopCodeValue.toString() : "") + ((vehicleCodeValue != null) ? vehicleCodeValue.toString() : "") + station_code.getText());
                    // -----------------------------------------------------------------------------------------------------------------------------------------------------
                }

                @Override
                public void keyPressed(KeyEvent e) {
                }
            });

            position_code.addKeyListener(new KeyListener() {
                @Override
                public void keyReleased(KeyEvent e) {
                    idMap.get(5).setText(position_code.getText());
                }

                @Override
                public void keyPressed(KeyEvent e) {
                }
            });
        }

        // 차체 ROBOT
        if (resourceCategory.equals(registry.getString("Resource.Category.ROB"))) {
            // 서브
            text_sub_class = drawPropText(composite, registry.getString("SubClass.NAME"), "text_sub_class", 1, true);
            text_sub_class.setEditable(false);
            text_sub_class.setBackground(getBackground());

            axis = drawPropCombo(composite, registry.getString("Axis.NAME"), "axis", getChildLovName("AXIS", mainClassLovName), true);
            drawRadioButton(composite, "Servo");
            robot_type = drawPropCombo(composite, registry.getString("RobotType.NAME"), "robot_type", getChildLovName("SHAPE", mainClassLovName), true);

            maker_no = drawPropText(composite, registry.getString("MakerNo.NAME"), "maker_no", 1, true);

            main_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    String selectedValue = main_class.getSelectedItem().toString();
                    idMap.get(2).setText(selectedValue);
                }
            });

            axis.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    Object robotTypeValue = robot_type.getSelectedItem();
                    text_sub_class.setText(axis.getSelectedItem().toString() + (true_servo.getSelection() ? "Y" : false_servo.getSelection() ? "N" : "") + ((robotTypeValue != null) ? robotTypeValue.toString() : ""));
                    idMap.get(3).setText(text_sub_class.getText());
                }
            });

            true_servo.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (true_servo.getSelection())
                        text_sub_class.setText(((axis.getSelectedItem() != null) ? axis.getSelectedItem().toString() : "") + "Y" + ((robot_type.getSelectedItem() != null) ? robot_type.getSelectedItem().toString() : ""));
                    idMap.get(3).setText(text_sub_class.getText());
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });

            false_servo.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (false_servo.getSelection())
                        text_sub_class.setText(((axis.getSelectedItem() != null) ? axis.getSelectedItem().toString() : "") + "N" + ((robot_type.getSelectedItem() != null) ? robot_type.getSelectedItem().toString() : ""));
                    idMap.get(3).setText(text_sub_class.getText());
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });

            robot_type.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    Object axisValue = axis.getSelectedItem();
                    text_sub_class.setText(((axisValue != null) ? axisValue.toString() : "") + (true_servo.getSelection() ? "Y" : false_servo.getSelection() ? "N" : "") + robot_type.getSelectedItem().toString());
                    idMap.get(3).setText(text_sub_class.getText());
                }
            });

            maker_no.addKeyListener(new KeyListener() {
                @Override
                public void keyReleased(KeyEvent e) {
                    e.doit = true;
                    idMap.get(4).setText(maker_no.getText());
                }

                @Override
                public void keyPressed(KeyEvent e) {

                }
            });
            
            //[SR150609-023][20150609] shcho, Maker고유번호 입력 시 특수문자 '-' 는 사용 못하도록 막음
            maker_no.addVerifyKeyListener(new VerifyKeyListener() {
                
                @Override
                public void verifyKey(VerifyEvent event) {
                    if(event.character == '-') {
                        event.doit = false;
                    }
                }
            });
        }

        // 차체 GUN
        if (resourceCategory.equals(registry.getString("Resource.Category.GUN"))) {
            sub_class = drawPropCombo(composite, registry.getString("SubClass.NAME"), "sub_class", true);
            maker_no = drawPropText(composite, registry.getString("MakerNo.NAME"), "maker_no", 1, true);

            main_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    String selectedValue = main_class.getSelectedItem().toString();
                    final String subClassLovName = getChildLovName(selectedValue, mainClassLovName);
                    sub_class.removeAllItems();
                    sub_class = SDVLOVUtils.comboValueSetting(sub_class, subClassLovName);
                    sub_class.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                    sub_class.setSelectedIndex(0);
                    sub_class.setText("");
                    idMap.get(2).setText(selectedValue);
                    idMap.get(3).setText("");
                    idMap.get(2).setText(selectedValue);
                }
            });

            sub_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(3).setText(sub_class.getSelectedItem().toString());
                }
            });

            maker_no.addKeyListener(new KeyListener() {
                @Override
                public void keyReleased(KeyEvent e) {
                    e.doit = true;
                    idMap.get(4).setText(maker_no.getText());
                }

                @Override
                public void keyPressed(KeyEvent e) {

                }
            });
            
            //[SR150609-023][20150609] shcho, Maker고유번호 입력 시 특수문자 '-' 는 사용 못하도록 막음
            maker_no.addVerifyKeyListener(new VerifyKeyListener() {
                
                @Override
                public void verifyKey(VerifyEvent event) {
                    if(event.character == '-') {
                        event.doit = false;
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
            // 공통
            for (int key : idMap.keySet()) {
                idMap.get(key).setEnabled(false);
                idMap.get(key).setBackground(getBackground());
            }
            resource_category.setEnabled(false);
            resource_category.getTextField().setBackground(getBackground());
            shop_code.setEnabled(false);
            shop_code.getTextField().setBackground(getBackground());
            main_class.setEnabled(false);
            main_class.getTextField().setBackground(getBackground());

            // 차체 일반설비, 로봇부대설비, JIG
            if (resourceCategory.equals(registry.getString("Resource.Category.EXT")) || resourceCategory.equals(registry.getString("Resource.Category.ROE")) || resourceCategory.equals(registry.getString("Resource.Category.JIG"))) {
                String mainClassValue = paramRevisionProperties.get(SDVPropertyConstant.EQUIP_MAIN_CLASS);
                String subClassLovName = getChildLovName(mainClassValue, getChildLovName(resourceCategory));
                SDVLOVUtils.comboValueSetting(sub_class, subClassLovName);

                sub_class.setEnabled(false);
                sub_class.getTextField().setBackground(getBackground());
                station_code.setEnabled(false);
                station_code.setBackground(getBackground());
                vehicle_code.setEnabled(false);
                vehicle_code.getTextField().setBackground(getBackground());
                position_code.setEnabled(false);
                position_code.setBackground(getBackground());
            }

            // 차체 Robot Only
            if (resourceCategory.equals(registry.getString("Resource.Category.ROB"))) {
                String subClassValue = paramRevisionProperties.get(SDVPropertyConstant.EQUIP_SUB_CLASS);
                String servoValue = subClassValue.substring(1, 2);
                if (servoValue.equals("Y")) {
                    true_servo.setSelection(true);
                }
                if (servoValue.equals("N")) {
                    false_servo.setSelection(true);
                }

                text_sub_class.setEnabled(false);
                text_sub_class.setBackground(getBackground());
                axis.setEnabled(false);
                axis.getTextField().setBackground(getBackground());
                true_servo.setEnabled(false);
                true_servo.setBackground(getBackground());
                false_servo.setEnabled(false);
                false_servo.setBackground(getBackground());
                robot_type.setEnabled(false);
                robot_type.getTextField().setBackground(getBackground());
                maker_no.setEnabled(false);
                maker_no.setBackground(getBackground());
            }

            // 차체 Gun Only
            if (resourceCategory.equals(registry.getString("Resource.Category.GUN"))) {
                String mainClassValue = paramRevisionProperties.get(SDVPropertyConstant.EQUIP_MAIN_CLASS);
                String subClassLovName = getChildLovName(mainClassValue, getChildLovName(resourceCategory));
                SDVLOVUtils.comboValueSetting(sub_class, subClassLovName);

                sub_class.setEnabled(false);
                sub_class.getTextField().setBackground(getBackground());
                maker_no.setEnabled(false);
                maker_no.setBackground(getBackground());
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
