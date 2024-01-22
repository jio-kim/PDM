/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * Class Name : Equipment_Creation_Properties
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public class CreateResourceViewPane extends AbstractSDVViewPane {

    private String viewId;
    protected TCSession tcsession;
    protected Registry registry;

    /* Create 또는 Revise 상태 값 */
    protected boolean createMode = false;
    protected String bopType;
    protected String resourceType;
    protected String resourceCategory;

    /*
     * ID 필드를 위한 Text Component
     * Key 값의 숫자와 생성된 위치 순번이 같다. 예) 4번째 Text상자 : idMap.get(4);
     */
    protected HashMap<Integer, SDVText> idMap;
    /* Create시 화면에서 입력받은 Item속성 */
    protected HashMap<String, Control> itemPropMap = new HashMap<String, Control>();
    /* Create시 화면에서 입력받은 ItemRevision 속성 */
    protected HashMap<String, Control> revisionPropMap = new HashMap<String, Control>();
    /* Revise시 선택된 ItemRevision의 속성 */
    protected Map<String, String> paramItemProperties;
    protected Map<String, String> paramRevisionProperties;

    protected Composite topComposite;
    protected Group idGroup;

    /* ----------- Common (공통) ----------- */
    /* Resource Category */
    protected SWTComboBox resource_category;
    /* Shop Code */
    protected SWTComboBox shop_code;
    /* Main Class */
    protected SWTComboBox main_class;
    /* Sub Class */
    protected SWTComboBox sub_class;
    /* Korean Name. */
    protected SDVText object_name;
    /* English Name */
    protected SDVText eng_name;
    /* Spec(Korean) */
    protected SDVText spec_kor;
    /* Spec(English) */
    protected SDVText spec_eng;
    /* Maker */
    protected SDVText maker;
    /* Description */
    protected SDVText object_desc;
    /* Vehicle Code */
    protected SWTComboBox vehicle_code;

    /* ----------- Equipment ----------- */
    /* Position Code */
    protected SDVText position_code;
    /* Line Code */
    protected SWTComboBox line_code;
    /* Station Code */
    protected SDVText station_code;
    /* 축 */
    protected SWTComboBox axis;
    /* Servo True */
    protected Button true_servo;
    /* Servo False */
    protected Button false_servo;
    /* Robot Type */
    protected SWTComboBox robot_type;
    /* Maker No. */
    protected SDVText maker_no;
    /* Purpose(Korean) */
    protected SDVText purpose_kor;
    /* Purpose(English) */
    protected SDVText purpose_eng;
    /* Capacity */
    protected SDVText capacity;
    /* Install Year */
    protected SDVText install_year;
    /* Nation */
    protected SDVText nation;
    /* Revision Description */
    protected SDVText rev_desc;

    /* ----------- Tool ----------- */
    /* 사양 코드 */
    protected SWTComboBox spec_code;
    /* 조립 업체/AF 코드 */
    protected SWTComboBox maker_af_code;
    protected SWTComboBox maker_torque_code;

    /* 조립 Socket 형상 분류 */
    protected SWTComboBox tool_shape;
    /* 조립 Socket 길이 */
    protected SWTComboBox tool_length;
    /* 조립 Socket 연결부 Size */
    protected SWTComboBox tool_size;
    /* 조립 Socket 자석삽입여부 */
    protected SWTComboBox tool_magnet;
    /* 도장 Stay Type */
    protected SWTComboBox stay_type;
    /* 도장 Stay Area */
    protected SWTComboBox stay_area;
    /* 차체, 도장 subClass Text */
    protected SDVText text_sub_class;
    /* Torque Value */
    protected SDVText torque_value;
    /* Purpose (국문영문 구분없음) */
    protected SDVText purpose;
    /* Unit of Usage (소요량 단위) */
    protected SWTComboBox unit_usage;
    /* Material (재질) */
    protected SDVText material;

    /*
     * String bopType = "";
     * String resourceType = "";
     * String resourceCategory = "";
     * 
     * //조립 일반설비
     * bopType="A"; resourceType="Equip"; resourceCategory=registry.getString("Resource.Category.EXT");
     * //조립 JIG
     * bopType="A"; resourceType="Equip"; resourceCategory=registry.getString("Resource.Category.ROE");
     * //차체 일반설비
     * bopType="B"; resourceType="Equip"; resourceCategory="EXT";
     * //차체 Robot부대설비
     * bopType="B"; resourceType="Equip"; resourceCategory="ROE";
     * //차체 Robot
     * bopType="B"; resourceType="Equip"; resourceCategory="ROB";
     * //차체 GUN
     * bopType="B"; resourceType="Equip"; resourceCategory="GUN";
     * //차체 JIG
     * bopType="B"; resourceType="Equip"; resourceCategory=registry.getString("Resource.Category.JIG");
     * //도장 일반설비
     * bopType="P"; resourceType="Equip"; resourceCategory="EXT";
     * 
     * //공구
     * bopType="A"; resourceType="Tool"; resourceCategory="EXT";
     * bopType="A"; resourceType="Tool"; resourceCategory="SOC";
     * bopType="B"; resourceType="Tool"; resourceCategory="EXT";
     * bopType="P"; resourceType="Tool"; resourceCategory="EXT";
     * bopType="P"; resourceType="Tool"; resourceCategory="STY";
     */

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public CreateResourceViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
        this.viewId = id;
        this.tcsession = SDVBOPUtilities.getTCSession();
    }

    @Override
    protected void initUI(Composite parent) {
        registry = Registry.getRegistry(this);

        topComposite = new Composite(parent, SWT.NONE);
        topComposite.setLayout(new FormLayout());
    }

    /**
     * Initiation ID Field (ID 표시 영역 생성)
     */
    protected void initIdField(Composite topComposite) {
        final Composite idComposite = new Composite(topComposite, SWT.NONE);
        idComposite.setLayout(new FormLayout());

        FormData fd_idComposite = new FormData();
        fd_idComposite.top = new FormAttachment(0);
        fd_idComposite.left = new FormAttachment(0);
        fd_idComposite.right = new FormAttachment(100, 0);
        fd_idComposite.bottom = new FormAttachment(0, 80);
        idComposite.setLayoutData(fd_idComposite);

        idGroup = new Group(idComposite, SWT.NONE);
        idGroup.setText("ID");
        idGroup.setLayout(new FormLayout());
        FormData fd_grpId = new FormData();
        fd_grpId.top = new FormAttachment(0, 10);
        fd_grpId.left = new FormAttachment(0, 10);
        fd_grpId.right = new FormAttachment(100, -10);
        fd_grpId.bottom = new FormAttachment(0, 70);
        idGroup.setLayoutData(fd_grpId);

        idMap = new HashMap<Integer, SDVText>();
    }

    /**
     * Initiation Property Field (Property 영역 생성)
     */
    protected void initPropertyField(Composite topComposite) {
        final Composite composite = new Composite(topComposite, SWT.NONE);

        FormData fd_composite = new FormData();
        fd_composite.top = new FormAttachment(0, 80);
        fd_composite.left = new FormAttachment(0);
        fd_composite.right = new FormAttachment(100, 0);
        fd_composite.bottom = new FormAttachment(100, -5);
        composite.setLayoutData(fd_composite);

        GridLayout gridLayout = new GridLayout(4, false);
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 20;
        gridLayout.marginLeft = 10;
        gridLayout.marginRight = 10;
        gridLayout.marginHeight = 10;
        composite.setLayout(gridLayout);

        // 필수속성 시작
        initMandatoryProperties(composite, bopType, resourceType, resourceCategory);
        // 구분 선
        drawBlankLabel(composite, registry.getString("Label.Separator.NAME"));
        // 기타 속성 시작
        initAdditionalProperties(composite, resourceType);
        // 구분 선
        drawBlankLabel(composite, registry.getString("Label.Separator.NAME"));

        composite.layout();
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
    protected void initMandatoryProperties(Composite composite, String bopType, String resourceType, String resourceCategory) {
        // 각 하위 화면에서 구현
    }

    /**
     * Set Additional Properties
     * 
     * @param composite
     * @param resourceType
     *            Value is "Equip" or "Tool"
     */
    protected void initAdditionalProperties(Composite composite, String resourceType) {
        // 각 하위 화면에서 구현
    }

    /**
     * Draw Blank or Separator Label Component
     * 
     * @param composite
     * @param type
     *            "Blank" or "Separator"
     */
    protected void drawBlankLabel(Composite composite, String type) {
        int swtStyle = SWT.NONE;
        int horizonSpan = 1;

        if (type.equalsIgnoreCase(registry.getString("Label.Blank.NAME"))) {
            swtStyle = SWT.NONE;
            horizonSpan = 2;
        }

        if (type.equalsIgnoreCase(registry.getString("Label.Separator.NAME"))) {
            swtStyle = SWT.SEPARATOR | SWT.HORIZONTAL;
            horizonSpan = 4;
        }

        Label separatLabel = new Label(composite, swtStyle);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = horizonSpan;
        separatLabel.setLayoutData(gridData);
    }

    /**
     * Draw Text Component
     */
    protected SDVText drawPropText(Composite composite, String labelName, int spanSize, boolean mandatory) {
        return drawPropText(composite, labelName, null, spanSize, mandatory);
    }

    protected SDVText drawPropText(Composite composite, String labelName, String textName, int spanSize, boolean mandatory) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(labelName);

        SDVText text = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = spanSize;
        text.setLayoutData(gridData);
        text.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
                    e.doit = true;
                }
            }
        });

        if (textName != null) {
            setControlData(textName, text);
        }

        text.setMandatory(mandatory);

        return text;
    }

    /**
     * Draw Combo Component
     * 
     * @param lovName
     */
    protected SWTComboBox drawPropCombo(Composite composite, String labelName, String comboName, boolean mandatory) {
        return drawPropCombo(composite, labelName, comboName, "", mandatory);
    }

    protected SWTComboBox drawPropCombo(Composite composite, String labelName, String comboName, String lovName, boolean mandatory) {
        return drawPropCombo(composite, labelName, comboName, lovName, mandatory, true);
    }

    protected SWTComboBox drawPropCombo(Composite composite, String labelName, String comboName, String lovName, boolean mandatory, boolean addProperty) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(labelName);

        // final SWTComboBox combo = new SWTComboBox(composite);
        final SWTComboBox combo = new SWTComboBox(composite, SWT.BORDER);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        if (lovName != null && !lovName.isEmpty()) {
            SDVLOVUtils.comboValueSetting(combo, lovName);
            combo.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        }

        if (addProperty) {
            setControlData(comboName, combo);
        }

        return (SWTComboBox) ResourceUtilities.setMandatory((Control) combo, mandatory);
    }

    /**
     * Control 종류에 따라 itemProplMap, revisionProplMap에 값을 담는 함수
     * 
     * @param controlName
     * @param control
     */
    protected void setControlData(String controlName, Control control) {
        if (controlName.equals("eng_name")) {
            itemPropMap.put(controlName, control);
        } else {
            revisionPropMap.put(controlName, control);
        }
    }

    /**
     * Draw RadioButton Component
     */
    protected void drawRadioButton(Composite composite, String labelName) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(labelName);

        Composite radioComposite = new Composite(composite, SWT.NONE);
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        rowLayout.marginTop = 0;
        rowLayout.marginRight = 0;
        rowLayout.marginLeft = 0;
        rowLayout.marginBottom = 0;
        rowLayout.spacing = 20;
        radioComposite.setLayout(rowLayout);
        radioComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        true_servo = new Button(radioComposite, SWT.RADIO);
        true_servo.setText("True");
        setControlData(labelName, true_servo);

        false_servo = new Button(radioComposite, SWT.RADIO);
        false_servo.setText("False");
        ResourceUtilities.setMandatory(false_servo, true);
        // controlMap.put(labelName, false_servo);
    }

    /**
     * Dreaw ID Field
     * 
     * @param idDivisionNum
     */
    protected void drawIdField(int idDivisionNum) {
        Label label = null;
        for (int i = 1; i <= idDivisionNum; i++) {
            idMap.put(i, drawIdText(idGroup, label));

            if (i < idDivisionNum - 1) {
                label = drawIdLabel(idGroup, "-", idMap.get(i));
            } else if (i == idDivisionNum - 1) {
                label = drawIdLabel(idGroup, "/", idMap.get(i));
            }

            if (i == idDivisionNum) {
                idMap.get(i).setText(registry.getString("Item.revision.DEFAULT"));
            }
        }

        // 차체,도장,조립 구분 Code (ID 필드 첫번째 자리) - (조립 & 공구)가 아닌 경우 bopType을 첫번째칸에 자동 기입한다.
        if (!(bopType.equals(registry.getString("BOP.Type.Assy")) && resourceType.equalsIgnoreCase(registry.getString("Resource.Type.Tool")))) {
            idMap.get(1).setText(bopType);
        }
        // 조립 설비(일반,JIG)인 경우 4번째 자리 일련번호 수동 기입
        if ((bopType.equals(registry.getString("BOP.Type.Assy")) && resourceType.equalsIgnoreCase(registry.getString("Resource.Type.Equip")))) {
            idMap.get(4).setEditable(true);
            idMap.get(4).setBackground(null);
            ResourceUtilities.setNumeric(idMap.get(4), 3);
        }
    }

    /**
     * Draw ID Text
     * 
     * @param group
     * @param previousComponent
     * @return
     * 
     */
    protected SDVText drawIdText(Group group, Label previousComponent) {
        SDVText text = new SDVText(group, SWT.CENTER | SWT.BORDER | SWT.SINGLE);
        FormData formData = new FormData();
        if (previousComponent != null) {
            formData.top = new FormAttachment(previousComponent, 0, SWT.CENTER);
            formData.left = new FormAttachment(previousComponent, 3, SWT.RIGHT);
            formData.right = new FormAttachment(previousComponent, 73, SWT.RIGHT);
        } else {
            formData.top = new FormAttachment(0, 10);
            formData.left = new FormAttachment(0, 10);
            formData.right = new FormAttachment(0, 73);
        }
        text.setLayoutData(formData);
        text.setEditable(false);
        text.setBackground(getBackground());

        return text;
    }

    /**
     * Draw "-" Label
     * 
     * @param group
     * @param labelText
     * @param SDVText
     * @return label
     */
    protected Label drawIdLabel(Group group, String labelText, SDVText textComponent) {
        Label lable = new Label(group, SWT.HORIZONTAL | SWT.CENTER);
        lable.setAlignment(SWT.RIGHT);
        FormData formData = new FormData();
        formData.top = new FormAttachment(textComponent, 0, SWT.CENTER);
        formData.left = new FormAttachment(textComponent, 3, SWT.CENTER);
        formData.right = new FormAttachment(textComponent, 7, SWT.RIGHT);
        lable.setLayoutData(formData);
        lable.setText(labelText);

        return lable;
    }

    /**
     * Get Resource Default LOV Name
     * 
     * @return String
     */
    protected String getDefaultLovName() {
        String lovName = "M7_" + bopType + "_" + resourceType;
        return lovName;
    }

    /**
     * Make Child LOV Name
     * 
     * @param parentLov
     * @param childKey
     * @return String
     */
    protected String getChildLovName(String childKey) {
        return getChildLovName(childKey, null);
    }

    protected String getChildLovName(String childKey, String parentLov) {
        if (parentLov == null || parentLov.isEmpty()) {
            parentLov = getDefaultLovName();
        }
        String lovName = parentLov + "_" + childKey;
        return lovName;
    }

    // /**
    // * Validation (Item 생성 실행버튼 클릭시 체크 수행)
    // *
    // * @param composite
    // * @return
    // */
    // protected void validation() {
    // Iterator<Integer> iterator = idMap.keySet().iterator();
    // while (iterator.hasNext()) {
    // String idValue = idMap.get(iterator.next()).getText();
    // if (idValue == null || idValue.isEmpty()) {
    // MessageBox.post(getShell(), "ID does not exist!", "ERROR", MessageBox.ERROR);
    // return;
    // }
    // }
    // }

    /**
     * Get Item ID
     * 
     * @return String
     */
    protected String getItemId() {
        String itemId = "";
        int idMapSize = idMap.keySet().size();
        for (int i = 1; i <= idMapSize; i++) {
            SDVText text = idMap.get(i);
            String idCode = text.getText();

            if (i < idMapSize) {
                itemId += idCode;
                if (i < idMapSize - 1) {
                    itemId += "-";
                }
            }
        }
        return itemId;
    }

    /**
     * Get Item Revision ID
     * 
     * @return String
     */
    protected String getRevisionId() {
        String revisionId = "";
        int idMapSize = idMap.keySet().size();
        SDVText text = idMap.get(idMapSize);
        revisionId = text.getText();
        return revisionId;
    }

    /**
     * Set Item ID : Revise 화면 Open시 ItemId 표시
     * 
     * @param itemId
     * @param revisionId
     */
    protected void setItemId(String itemId, String revisionId) {
        String[] arrItemId = itemId.split("-"); // 4칸
        int idMapSize = idMap.keySet().size(); // 5칸
        for (int i = 1; i <= idMapSize; i++) {
            if (i == idMapSize) {
                idMap.get(i).setText(revisionId);
            } else {
                idMap.get(i).setText(arrItemId[i - 1]);
            }
        }
    }

    /**
     * Get Item Component Type Name
     * 
     * @param type
     * @return String
     */
    protected String getItemCompType() {
        String itemTCCompType = "";
        if (resourceType.equals(registry.getString("Resource.Type.Equip"))) {
            if (resourceCategory.equals(registry.getString("Resource.Category.EXT")) || resourceCategory.equals(registry.getString("Resource.Category.ROE"))) {
                itemTCCompType = SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM;
            } else if (resourceCategory.equals(registry.getString("Resource.Category.JIG"))) {
                itemTCCompType = SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM;
            } else if (resourceCategory.equals(registry.getString("Resource.Category.ROB"))) {
                itemTCCompType = SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM;
            } else if (resourceCategory.equals(registry.getString("Resource.Category.GUN"))) {
                itemTCCompType = SDVTypeConstant.BOP_PROCESS_GUN_ITEM;
            }
        }

        if (resourceType.equalsIgnoreCase(registry.getString("Resource.Type.Tool"))) {
            itemTCCompType = SDVTypeConstant.BOP_PROCESS_TOOL_ITEM;
        }

        return itemTCCompType;
    }

    // 화면의 값을 가져와서 Map에 담아 Item 생성시 사용 할 데이터를 준비한다.
    protected Map<String, String> getItemData() {
        Map<String, String> itemProperties = setPropertyMap(itemPropMap);
        return itemProperties;
    }

    // 화면의 값을 가져와서 Map에 담아 Revision 생성시 사용 할 데이터를 준비한다.
    protected Map<String, String> getRevisionData() {
        Map<String, String> revisionProperties = setPropertyMap(revisionPropMap);
        return revisionProperties;
    }

    // 화면의 값을 가져와서 Map에 담아 Return
    protected Map<String, String> setPropertyMap(HashMap<String, Control> map) {
        Map<String, String> properties = new HashMap<String, String>();
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Control control = map.get(key);

            if (control instanceof SDVText) {
                String value = ((SDVText) control).getText();
                properties.put(getPropertyName(key, resourceType), value);
            }

            if (control instanceof SWTComboBox) {
                String value = "";
                SWTComboBox comboBox = (SWTComboBox) control;
                Object selectedItem = comboBox.getSelectedItem();
                if (selectedItem != null) {
                    value = selectedItem.toString();
                }

                properties.put(getPropertyName(key, resourceType), value);
            }

            if (control instanceof Button) {
                properties.put(getPropertyName(key, resourceType), ((Button) control).getSelection() ? "Y" : "");
            }
        }
        return properties;
    }

    /**
     * Revise화면 Open시 Item 또는 Revision Properties(Map)의 값으로 하면의 Control에 값을 셋팅하는 함수
     * 
     * @param propControlMap
     * @param paramProperties
     * @param resourceType
     */
    protected void setControlValue(HashMap<String, Control> propControlMap, Map<String, String> paramProperties, String resourceType) {
        for (String key : propControlMap.keySet()) {
            Control control = propControlMap.get(key);

            if (control instanceof SDVText) {
                ((SDVText) control).setText(paramProperties.get(getPropertyName(key, resourceType)));
            }

            if (control instanceof SWTComboBox) {
                SWTComboBox comboBox = (SWTComboBox) control;
                comboBox.setSelectedItem(paramProperties.get(getPropertyName(key, resourceType)));
            }
        }
    }

    /**
     * Control Name으로 SDVPropertyConstant에 정의된 Property Name을 가져오는 함수
     * 
     * @param controlName
     *            , resourceType( ex : EQUIP, TOOL)
     * @return String
     */
    protected String getPropertyName(String controlName, String resourceType) {
        String propName = "";
        String varName = "";
        if (controlName.equals("text_sub_class"))
            controlName = "sub_class";

        if (controlName.equals(SDVPropertyConstant.ITEM_OBJECT_NAME) || controlName.equals(SDVPropertyConstant.ITEM_OBJECT_DESC)) {
            varName = "ITEM_" + controlName.toUpperCase();
        } else {
            varName = resourceType.toUpperCase() + "_" + controlName.toUpperCase();
        }

        try {
            SDVPropertyConstant propConst = new SDVPropertyConstant();
            propName = (String) propConst.getClass().getDeclaredField(varName).get(new String());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return propName;
    }

    /**
     * Sequence ID Number 생성
     * 
     * @return String
     */
    protected String createSeqNum(String length) {
        return createSeqNum(length, "");
    }

    /**
     * Sequence ID Number 생성
     * 
     * @param partOfId
     *            (ID를 구성하는 일부 문자열)
     * @return String
     */
    protected String createSeqNum(String length, String partOfId) {
        String seqStrNum = "";
        int count = 0;
        if (length.equals("2")) {
            count = 99;
        }
        if (length.equals("3")) {
            count = 999;
        }

        for (int i = 1; i <= count; i++) {
            seqStrNum = "";
            if (i < 10) {
                if (length.equals("3")) {
                    seqStrNum += "00" + Integer.toString(i);
                }
                if (length.equals("2")) {
                    seqStrNum += "0" + Integer.toString(i);
                }
            } else {
                if (length.equals("3") && (i < 100)) {
                    seqStrNum += "0" + Integer.toString(i);
                } else {
                    seqStrNum += Integer.toString(i);
                }
            }

            try {
                String itemId = getPartOfItemId() + partOfId + seqStrNum;
                TCComponentItem item = SYMTcUtil.findItem(tcsession, itemId);
                if (item == null) {
                    return partOfId + seqStrNum;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return seqStrNum;
    }

    /**
     * Get PartOfItemId
     * 
     * @return String
     */
    protected String getPartOfItemId() {
        String itemId = "";
        int idMapSize = idMap.keySet().size();
        for (int i = 1; i <= idMapSize; i++) {
            SDVText text = idMap.get(i);
            String idCode = text.getText();

            if (i < idMapSize - 1) {
                itemId += idCode;
                itemId += "-";
            }
        }
        return itemId;
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
        String itemId = getItemId();
        String revisionId = getRevisionId();

        if (!itemId.isEmpty()) {
            Map<String, String> itemProperties = getItemData();
            Map<String, String> revisionProperties = getRevisionData();
            boolean isModified1 = checkModified(itemProperties, paramItemProperties);
            boolean isModified2 = checkModified(revisionProperties, paramRevisionProperties);

            itemProperties.put(SDVPropertyConstant.ITEM_ITEM_ID, itemId);
            revisionProperties.put(SDVPropertyConstant.ITEM_REVISION_ID, revisionId);

            RawDataMap targetMap = new RawDataMap();

            targetMap.put("isModified", (isModified1 || isModified2) ? true : false, IData.BOOLEAN_FIELD);
            targetMap.put("createMode", createMode, IData.BOOLEAN_FIELD);
            targetMap.put("itemTCCompType", getItemCompType(), IData.STRING_FIELD);
            targetMap.put("itemProperties", itemProperties, IData.OBJECT_FIELD);
            targetMap.put("revisionProperties", revisionProperties, IData.OBJECT_FIELD);
            return targetMap;
        }
        return null;
    }

    /**
     * 화면에서 변경된 사항이 있는지 검사 (변경사항 있으면 true Return)
     * 
     * @param revisionProperties
     * @param itemProperties
     */
    public boolean checkModified(Map<String, String> newProperties, Map<String, String> oldProperties) {
        boolean isModified = false;

        // 처음 생성인 경우 (= oldProperties가 없는경우) true 반환
        if (oldProperties == null || oldProperties.size() == 0) {
            return true;
        }

        // Revise시 변경사항이 있을 경우 true 반환
        for (String key : newProperties.keySet()) {
            String newPropValue = newProperties.get(key);
            String oldPropValue = oldProperties.get(key);
            if (!newPropValue.equals(oldPropValue)) {
                isModified = true;
                break;
            }
        }

        return isModified;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractSDVInitOperation getInitOperation() {
        Map<String, Object> paramMap = getParameters();
        // Create
        if (paramMap.containsKey("paramKey")) {
            // 자원선택창 Close
            if (paramMap.containsKey("parentDialog")) {
                AbstractSDVSWTDialog parentDialog = (AbstractSDVSWTDialog) paramMap.get("parentDialog");
                parentDialog.close();
            }

            RawDataMap rawDataMap = (RawDataMap) paramMap.get("paramKey");

            createMode = rawDataMap.getStringValue("createMode").equals(registry.getString("Create.Mode.KEY"));
            bopType = rawDataMap.getStringValue("shopValue");
            resourceType = rawDataMap.getStringValue("resourceType");
            resourceCategory = rawDataMap.getStringValue("resourceCategory");
        }

        // Revise
        if (paramMap.containsKey("itemProperties") && paramMap.containsKey("revisionProperties")) {
            String[] arrViewId = viewId.split(":");
            paramItemProperties = (Map<String, String>) paramMap.get("itemProperties");
            paramRevisionProperties = (Map<String, String>) paramMap.get("revisionProperties");

            bopType = arrViewId[0].substring(0, 1);
            resourceType = arrViewId[1].toUpperCase();
            resourceCategory = paramRevisionProperties.get(SDVPropertyConstant.EQUIP_RESOURCE_CATEGORY);
        }

        // ID 표시 영역 생성
        initIdField(topComposite);
        // 속성 영역 생성
        initPropertyField(topComposite);

        return null;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

    @Override
    public void uiLoadCompleted() {

    }

}
