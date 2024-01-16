package com.ssangyong.commands.partmaster.variantpart;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ssangyong.commands.partmaster.Constants;
import com.ssangyong.commands.partmaster.validator.VariantPartValidator;
import com.ssangyong.common.SYMCLOVComboBox;
import com.ssangyong.common.SYMCLOVComboBox10;
import com.ssangyong.common.SYMCLabel;
import com.ssangyong.common.SYMCText;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.controls.DateControlDialog;

/**
 * Variant Part Information Panel
 * 
 */
public class VariantPartMasterInfoPanel extends Composite implements IPageComplete {

    /** TC Session */
    private TCSession session;

    /** Variant Part PreFix ID Field */
    protected SYMCText prefixText;
    /** Part No. */
    protected SYMCText partNoText;
    /** Rev No. */
    protected SYMCText partRevisionText;
    /** Part Name */
    protected SYMCText partNameText;

    /** Engine No. */
    protected SYMCText enginNoText;
    /** Description */
    protected SYMCText descText;
    /** Project Code Combo */
    protected SYMCLOVComboBox projCodeCB;
    /** Maturity Combo */
    protected SYMCLOVComboBox maturityCB;

    /** Target Revision */
    TCComponentItemRevision targetRevision;
    /** 화면에 입력된 Attr Map */
    HashMap<String, Object> attrMap;
    /** 최초 Loading시 Attr Map(수정여부 판별시 사용) */
    HashMap<String, Object> oldAttrMap;

    // [SR140702-059][20140626] KOG Variant Part Info Panel에 parameter hash map 추가.
    private HashMap<String, Object> paramMap;

    // [SR140702-058][20140630] KOG Var. Part G-Model Text 추가.
    private SYMCText gModelText;
    private SYMCText variantTypeText;
    @SuppressWarnings("unused")
	private SYMCText engNumText;
    private SYMCText locText;
    private SYMCText bodyTypeText;
    private SYMCText seaterText;
    private SYMCText trimText;
    private SYMCText sopDateText;

    // END 20140630

    /**
     * Create Product 기능에서 호출
     * 
     * @param parent
     * @param style
     */
    public VariantPartMasterInfoPanel(Composite parent, int style) {
        super(parent, style);
        this.session = CustomUtil.getTCSession();
        this.attrMap = new HashMap<String, Object>();

        initUI();
        setControlData();

    }

    /**
     * ViewerTab 조회 기능에서 호출
     * 
     * @param parent
     * @param style
     * @param isViewMode
     */
    public VariantPartMasterInfoPanel(Composite parent, int style, boolean isViewMode) {
        super(parent, style);
        session = CustomUtil.getTCSession();
        attrMap = new HashMap<String, Object>();
        oldAttrMap = new HashMap<String, Object>();
        initUI();
        setControlData();
        setViewMode();
        try {
            // 선택된 Item Revision
            InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
            if (comp != null && comp instanceof TCComponentItemRevision) {
                targetRevision = (TCComponentItemRevision) comp;
                this.setInitData(targetRevision);
                this.getPropDataMap(this.oldAttrMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * [SR140702-059][20140626] KOG Variant Part Info Panel에 parameter hash map 세팅 생성자.
     */
    public VariantPartMasterInfoPanel(ScrolledComposite parent, HashMap<String, Object> paramMap, int style) {
        super(parent, style);
        this.session = CustomUtil.getTCSession();
        this.attrMap = new HashMap<String, Object>();
        this.paramMap = paramMap;
        initUI();
        setControlData();
    }

    /**
     * 조회시 화면 Setting
     */
    public void setViewMode() {
        prefixText.setEnabled(false);
        partNoText.setEnabled(false);
        partRevisionText.setEnabled(false);
        partNameText.setEnabled(false);
        maturityCB.setEnabled(false);
        // projCodeCB.setEnabled(false);
    }

    /**
     * 화면에 표시된 속성 값을 HashMap으로 Setting
     * 
     * @param attributeMap
     *            : Setting될 HashMap
     * @return
     * @throws Exception
     */
    public HashMap<String, Object> getPropDataMap(HashMap<String, Object> attributeMap) throws Exception {

        attributeMap.put("item_id", prefixText.getText() + partNoText.getText());
        attributeMap.put("item_revision_id", partRevisionText.getText());

        attributeMap.put("object_name", partNameText.getText());

        attributeMap.put("s7_PROJECT_CODE", projCodeCB.getSelectedString());
        attributeMap.put("s7_MATURITY", maturityCB.getSelectedString());
        attributeMap.put("s7_ENGINE_NO", enginNoText.getText());

        // [SR140702-058][20140630] KOG Variant Part Attribut Map에 Value 추가.
        attributeMap.put("s7_GMODEL_CODE", gModelText.getText());
        attributeMap.put("s7_VARIANT_TYPE", variantTypeText.getText());
        attributeMap.put("s7_SOP_DATE", CustomUtil.getDateFromStringDate(sopDateText.getText()));
        // attributeMap.put("s7_ENGINE_NUMBER", engNumText.getText());
        attributeMap.put("s7_LOCATION", locText.getText());
        attributeMap.put("s7_BODY_TYPE", bodyTypeText.getText());
        attributeMap.put("s7_SEATER", seaterText.getText());
        attributeMap.put("s7_TRIM_LEVEL", trimText.getText());

        attributeMap.put("object_desc", descText.getText());

        return attributeMap;
    }

    /**
     * Revision의 속성값을 화면에 표시
     * 
     * @param targetRevision
     *            : Target Revision
     * @throws TCException
     */
    private void setInitData(TCComponentItemRevision targetRevision) throws TCException {

        String itemID = targetRevision.getProperty("item_id");
        prefixText.setText(itemID.substring(0, 1));
        partNoText.setText(itemID.substring(1, itemID.length()));

        partRevisionText.setText(targetRevision.getProperty("item_revision_id"));
        partNameText.setText(targetRevision.getProperty("object_name"));
        // [SR140702-059][20140626] KOG Variant Part SaveAs 시에 ProjectCode Blank 처리.
        if (paramMap != null) {
            String strSaveAs = (String) paramMap.get(Constants.COMMAND_SAVE_AS);
            if (strSaveAs != null && strSaveAs.equals("TRUE")) {
                projCodeCB.setSelectedIndex(0);
            } else {
                projCodeCB.setSelectedString(targetRevision.getProperty("s7_PROJECT_CODE"));
            }
        } else {
            projCodeCB.setSelectedString(targetRevision.getProperty("s7_PROJECT_CODE"));
        }
        maturityCB.setSelectedString(targetRevision.getProperty("s7_MATURITY"));
        enginNoText.setText(targetRevision.getProperty("s7_ENGINE_NO"));

        // [SR140702-058][20140701] KOG Variant Part 조회시 Property 값 Control에 세팅 추가.
        gModelText.setText(targetRevision.getStringProperty("s7_GMODEL_CODE"));
        variantTypeText.setText(targetRevision.getStringProperty("s7_VARIANT_TYPE"));
        Date dateProperty = targetRevision.getDateProperty("s7_SOP_DATE");
        if (dateProperty != null) {
            sopDateText.setText(CustomUtil.getStringDateFromDate(dateProperty));
        }
        locText.setText(targetRevision.getStringProperty("s7_LOCATION"));
        bodyTypeText.setText(targetRevision.getStringProperty("s7_BODY_TYPE"));
        seaterText.setText(targetRevision.getStringProperty("s7_SEATER"));
        trimText.setText(targetRevision.getStringProperty("s7_TRIM_LEVEL"));

        descText.setText(targetRevision.getProperty("object_desc"));

    }

    public String getFormatedString(double value, String format) {

        DecimalFormat df = new DecimalFormat(format);//
        return df.format(value);
    }

    /**
     * CheckIn시 Validation Check
     * 
     * @return
     */
    public boolean isSavable() {
        try {
            // 화면에 입력된 속성값을 Map형태로 가져옴
            this.getPropDataMap(this.attrMap);
            // Variant Part Validator
            VariantPartValidator validator = new VariantPartValidator();
            String strMessage = validator.validate(this.attrMap, VariantPartValidator.TYPE_VALID_MODIFY);

            // Error 존재시 MessageBox 표시
            if (!CustomUtil.isEmpty(strMessage)) {
                MessageBox.post(getShell(), strMessage, "Warning", MessageBox.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * CheckIn Action
     * 
     */
    public void saveAction() {
        try {
            // Item ID는 저장 대상이 아님
            this.attrMap.remove("item_id");

            // 전체 Attribute Name
            String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
            // 전체 Property
            TCProperty[] props = targetRevision.getTCProperties(szKey);

            for (int i = 0; i < props.length; i++) {

                if (props[i] == null) {
                    System.out.println(szKey[i] + " is Null");
                    continue;
                }

                Object value = attrMap.get(szKey[i]);
                CustomUtil.setObjectToPropertyValue(props[i], value);

            }

            // 일괄 저장
            targetRevision.setTCProperties(props);
            targetRevision.refresh();

            // targetRevision.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 화면초기화
     */
    private void initUI() {
        setBackground(new Color(null, 255, 255, 255));
        setLayout(new GridLayout(1, false));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginTop = 5;
        groupLayout.marginBottom = 5;
        groupLayout.marginLeft = 5;
        groupLayout.marginRight = 5;

        Composite composite = new Composite(this, SWT.NONE);
        composite.setLayout(groupLayout);
        composite.setBackground(new Color(null, 255, 255, 255));

        // /////////////////////////////////////////////////////////////////////////////////////////
        // ///////////////////////////////////////Basic Info Start//////////////////////////////////
        // /////////////////////////////////////////////////////////////////////////////////////////

        FormData labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(0, 5);

        SYMCLabel partNoLabel = new SYMCLabel(composite, "Part No ", labelFormData);

        FormData textFormData = new FormData(12, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(partNoLabel);
        prefixText = new SYMCText(composite, true, textFormData);
        prefixText.setEnabled(false);
        prefixText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        prefixText.setText("V");

        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(prefixText);
        partNoText = new SYMCText(composite, true, textFormData);
//        partNoText.setTextLimit(9);	// jclee 수정, 송대영CJ 요청. 2014.10.23
        partNoText.setTextLimit(10);

        textFormData = new FormData(20, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(partNoText, 4);
        partRevisionText = new SYMCText(composite, textFormData);
        partRevisionText.setEnabled(false);
        partRevisionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        partRevisionText.setText("000");

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partRevisionText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel partNameLabel = new SYMCLabel(composite, "Part Name", labelFormData);
        textFormData = new FormData(222, 18);
        textFormData.top = new FormAttachment(partRevisionText, 5);
        textFormData.left = new FormAttachment(partNameLabel, 5);
        partNameText = new SYMCText(composite, true, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partRevisionText, 5);
        labelFormData.left = new FormAttachment(partNameText, 37);
        SYMCLabel enginNoLabel = new SYMCLabel(composite, "Engine No.", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(partRevisionText, 5);
        textFormData.left = new FormAttachment(enginNoLabel, 5);
        enginNoText = new SYMCText(composite, false, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partNameText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel projCodeLabel = new SYMCLabel(composite, "Project Code", labelFormData);
        textFormData = new FormData(227, 18);
        textFormData.top = new FormAttachment(partNameText, 5);
        textFormData.left = new FormAttachment(projCodeLabel, 5);
        textFormData.height = 20;

        projCodeCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_PROJECT_CODE");
        projCodeCB.setLayoutData(textFormData);
        projCodeCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partNameText, 5);
        labelFormData.left = new FormAttachment(projCodeCB, 37);
        SYMCLabel maturityLabel = new SYMCLabel(composite, "Maturity", labelFormData);
        textFormData = new FormData(206, 18);
        textFormData.top = new FormAttachment(partNameText, 5);
        textFormData.left = new FormAttachment(maturityLabel, 5);
        textFormData.height = 20;

        maturityCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_MATURITY");
        maturityCB.setLayoutData(textFormData);
        maturityCB.setText("In Work");
        maturityCB.setEnabled(false);
        maturityCB.setMandatory(true);

        // [SR140702-058][20140630] KOG Var. Part Init UI G-Model.
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(maturityCB, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel gModelLabel = new SYMCLabel(composite, "G-Model Code", labelFormData);
        textFormData = new FormData(222, 18);
        textFormData.top = new FormAttachment(maturityCB, 5);
        textFormData.left = new FormAttachment(gModelLabel, 5);
        gModelText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(maturityCB, 5);
        labelFormData.left = new FormAttachment(gModelText, 37);
        SYMCLabel variantTypeLabel = new SYMCLabel(composite, "Variant Type", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(maturityCB, 5);
        textFormData.left = new FormAttachment(variantTypeLabel, 5);
        variantTypeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(variantTypeText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel sopDateLabel = new SYMCLabel(composite, "SOP Date", labelFormData);
        textFormData = new FormData(222, 18);
        textFormData.top = new FormAttachment(variantTypeText, 5);
        textFormData.left = new FormAttachment(sopDateLabel, 5);
        sopDateText = new SYMCText(composite, textFormData);
        sopDateText.setEditable(false);
        textFormData = new FormData(26, 26);
        textFormData.top = new FormAttachment(variantTypeText, 5);
        textFormData.left = new FormAttachment(sopDateText, 0);
        Button button = new Button(composite, SWT.NONE);
        button.setImage(CustomUtil.getImage("icons/calendar_16.png"));
        button.setLayoutData(textFormData);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                DateControlDialog dateControlDialog = new DateControlDialog(getShell(), 0, 0, 0, 0, 0, 0);
                int flag = dateControlDialog.open();
                if (flag == DateControlDialog.CANCEL) {
                    return;
                } else if (flag == DateControlDialog.CLEAR) {
                    sopDateText.setText("");
                    return;
                }
                Date selectedDate = dateControlDialog.getSelectedDate();
                sopDateText.setText(CustomUtil.getStringDateFromDate(selectedDate));
            }
        });

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(variantTypeText, 5);
        labelFormData.left = new FormAttachment(sopDateText, 37);
        SYMCLabel locLabel = new SYMCLabel(composite, "Location", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(variantTypeText, 5);
        textFormData.left = new FormAttachment(locLabel, 5);
        locText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(locText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel bodyTypeLabel = new SYMCLabel(composite, "Body Type", labelFormData);
        textFormData = new FormData(222, 18);
        textFormData.top = new FormAttachment(locText, 5);
        textFormData.left = new FormAttachment(bodyTypeLabel, 5);
        bodyTypeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(locText, 5);
        labelFormData.left = new FormAttachment(bodyTypeText, 37);
        SYMCLabel seaterLabel = new SYMCLabel(composite, "Seater", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(locText, 5);
        textFormData.left = new FormAttachment(seaterLabel, 5);
        seaterText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(seaterText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel trimLabel = new SYMCLabel(composite, "Trim Level", labelFormData);
        textFormData = new FormData(222, 18);
        textFormData.top = new FormAttachment(seaterText, 5);
        textFormData.left = new FormAttachment(trimLabel, 5);
        trimText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(trimText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel descLabel = new SYMCLabel(composite, "Description", labelFormData);
        textFormData = new FormData(573, 18);
        textFormData.top = new FormAttachment(trimText, 5);
        textFormData.left = new FormAttachment(descLabel, 5);
        descText = new SYMCText(composite, textFormData);

    }

    private void setControlData() {
        // actWeightText.setInputType(SYMCText.DOUBLE);

    }

    @Override
    public boolean isPageComplete() {
        return true;
    }

    /**
     * TC ViewerTab에서 화면 이동시 수정된 항목이 존재하는지 Check
     * 
     * 최초 Loading시 속성값과 현재 속성값을 비교
     */
    public boolean isModified() {
        if (this.targetRevision == null || !this.targetRevision.isCheckedOut())
            return false;
        try {
            this.getPropDataMap(this.attrMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object newValue = "";
        Object oldValue = null;
        for (Object key : this.oldAttrMap.keySet().toArray()) {
            oldValue = this.oldAttrMap.get(key);
            if (oldValue == null)
                oldValue = "";
            newValue = this.attrMap.get(key);
            if (newValue == null)
                newValue = "";
            if (!oldValue.equals(newValue))
                return true;
        }

        return false;
    }

}
