package com.kgm.commands.partmaster.materialpart;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.commands.ec.search.FileAttachmentComposite;
import com.kgm.commands.partmaster.Constants;
import com.kgm.commands.partmaster.validator.MatPartValidator;
import com.kgm.common.SYMCLOVComboBox;
import com.kgm.common.SYMCLOVComboBox10;
import com.kgm.common.SYMCLabel;
import com.kgm.common.SYMCText;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;

/**
 * Material Part Information Panel
 * 
 */
public class MatPartMasterInfoPanel extends Composite implements IPageComplete {

    /** TC Session */
    private TCSession session;

    /** Part No. Field */
    protected SYMCText partNoText;
    /** Part Rev. Field */
    protected SYMCText partRevisionText;

    /** Spec No. Field */
    protected SYMCText specNoText;
    /** Ses Code Field */
    protected SYMCText sesCodeText;
    /** KS Code Field */
    protected SYMCText ksCodeText;
    /** JIS Code Field */
    protected SYMCText jisCodeText;
    /** DIN Code Field */
    protected SYMCText dinCodeText;

    /** MB Code Field */
    protected SYMCText mbCodeText;
    /** SAE Code Field */
    protected SYMCText saeCodeText;
    /** GB Code Field */
    protected SYMCText gbCodeText;
    /** SUP Code Field */
    protected SYMCText supCodeText;
    /** Other Code Field */
    protected SYMCText otherCodeText;
    /** Density Field */
    protected SYMCText densityText;
    /** Description Field */
    protected SYMCText descText;

    /** Activity Combo */
    protected SYMCLOVComboBox activityCB;
    /** Material Type Combo */
    protected SYMCLOVComboBox materialTypeCB;
    /** Maturity Combo */
    protected SYMCLOVComboBox maturityCB;
    /** Material Source Combo */
    protected SYMCLOVComboBox materialSrcCB;

    /** SaveAs시 Target Revison */
    TCComponentItemRevision baseItemRev;
    /** 조회시 Target Revision */
    TCComponentItemRevision targetRevision;
    /** 화면에 입력된 속성 Map */
    HashMap<String, Object> attrMap;
    /** 최초 Loading시 속성 Map(수정항목 Check를 위해 사용, attrMap과 oldAttrMap을 비교) */
    HashMap<String, Object> oldAttrMap;
    /** PartManage Dialog에서 넘어온 Param Map */
    HashMap<String, Object> paramMap;

    /** File 첨부 Composite */
    FileAttachmentComposite fileComposite;

    /**
     * Create Part Menu를 통해 호출됨
     * 
     * @param parent
     * @param paramMap
     *            : PartManage Dialog에서 넘어온 Param Map
     * @param style
     *            : Dialog SWT Style
     */
    public MatPartMasterInfoPanel(Composite parent, HashMap<String, Object> paramMap, int style) {
        super(parent, style);
        this.session = CustomUtil.getTCSession();
        this.attrMap = new HashMap<String, Object>();
        this.paramMap = paramMap;

        initUI();
        setControlData();

        this.setInitData(paramMap);

    }

    /**
     * Revision 선택 후 ViewerTab에서 호출
     * 
     * @param parent
     * @param style
     */
    public MatPartMasterInfoPanel(Composite parent, int style) {
        super(parent, style);
        session = CustomUtil.getTCSession();
        attrMap = new HashMap<String, Object>();
        oldAttrMap = new HashMap<String, Object>();
        initUI();
        setControlData();
        setViewMode();
        try {
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
     *  [SR140610-43][20140609]KOG 
     *  Text Control에 대한 LimitValueListener 세팅 (KeyListener)
     */
    private void setLimitValueListener() {
        sesCodeText.addKeyListener(new CodeKeyListener(sesCodeText, 20));
        ksCodeText.addKeyListener(new CodeKeyListener(ksCodeText, 20));
        jisCodeText.addKeyListener(new CodeKeyListener(jisCodeText, 20));
        dinCodeText.addKeyListener(new CodeKeyListener(dinCodeText, 20));
        mbCodeText.addKeyListener(new CodeKeyListener(mbCodeText, 20));
        saeCodeText.addKeyListener(new CodeKeyListener(saeCodeText, 20));
        supCodeText.addKeyListener(new CodeKeyListener(supCodeText, 20));
        otherCodeText.addKeyListener(new CodeKeyListener(otherCodeText, 26));
        gbCodeText.addKeyListener(new CodeKeyListener(gbCodeText, 20));
        specNoText.addKeyListener(new CodeKeyListener(specNoText, 20));
        descText.addKeyListener(new CodeKeyListener(descText, 128));
        densityText.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                String value = densityText.getText();
                Point selection = densityText.getSelection();
                if (e.character == '.') {
                    return;
                }
                String preno = new String();
                String posno = new String();
                if (value.contains(".")) {
                    preno = value.substring(0, value.lastIndexOf(".") );
                    posno = value.substring(value.lastIndexOf(".") + 1);
                } else {
                    preno = value;
                }
                System.out.println(preno);
                System.out.println(posno);
                if (preno.isEmpty() && posno.isEmpty()) {
                    return;
                }
                if (preno.length() > 8) {
                    postMessage();
                    preno = preno.substring(0, 8);
                }
                if (posno.length() > 4) {
                    postMessage();
                    posno = posno.substring(0, 4);
                }
                value = preno;
                if (!posno.isEmpty()) {
                    value += "." + posno;
                }
                densityText.setText(value);
                densityText.setSelection(selection);
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            private void postMessage() {
                MessageBox.post(getShell(), "정수 8자리 소수점 4자리 이하로 입력 가능합니다.", "Material Part", MessageBox.WARNING);
            }
        });
    }

    /**
     * 조회화면인 경우 수정불가 항목 Setting
     */
    public void setViewMode() {
        partNoText.setEnabled(false);
        partRevisionText.setEnabled(false);
        activityCB.setEnabled(false);
        activityCB.setEnabled(false);
        materialTypeCB.setEnabled(false);
        maturityCB.setEnabled(false);
        materialSrcCB.setEnabled(false);
        specNoText.setEnabled(false);
        dinCodeText.setEnabled(false);
    }

    /**
     * 화면에 입력된 속성 값을 저장
     * 
     * @param attributeMap
     *            : 속성값이 저장될 HashMap
     * @return
     * @throws Exception
     */
    public HashMap<String, Object> getPropDataMap(HashMap<String, Object> attributeMap) throws Exception {

        attributeMap.put("item_revision_id", partRevisionText.getText());

        attributeMap.put("s7_ACTIVATION", activityCB.getText());
        attributeMap.put("s7_TYPE", materialTypeCB.getText());

        // Part Name을 MatType으로 대체
        attributeMap.put("object_name", materialTypeCB.getText());

        attributeMap.put("s7_MATURITY", maturityCB.getText());
        attributeMap.put("s7_SOURCE", materialSrcCB.getText());
        attributeMap.put("s7_SPEC_NUMBER", specNoText.getText());
        attributeMap.put("s7_SES_CODE", sesCodeText.getText());
        attributeMap.put("s7_KS_CODE", ksCodeText.getText());
        attributeMap.put("s7_JIS_CODE", jisCodeText.getText());
        attributeMap.put("s7_DIN_CODE", dinCodeText.getText());
        attributeMap.put("s7_MB_CODE", mbCodeText.getText());
        attributeMap.put("s7_SAE_CODE", saeCodeText.getText());
        attributeMap.put("s7_GB_CODE", gbCodeText.getText());
        attributeMap.put("s7_SUP_CODE", supCodeText.getText());
        attributeMap.put("s7_OTHER_CODE", otherCodeText.getText());
        attributeMap.put("s7_DENSITY", densityText.getText());
        attributeMap.put("object_desc", descText.getText());

        return attributeMap;
    }

    /**
     * Create Part 기능을 통해 호출된 경우 속성 값 Setting
     * 
     * @param paramMap
     *            : Part Manage Dialog에서 넘어온 Parameter Map
     */
    private void setInitData(HashMap<String, Object> paramMap) {

        this.baseItemRev = (TCComponentItemRevision) paramMap.get(Constants.ATTR_NAME_BASEITEMID);
        if (baseItemRev != null) {
            try {
                this.setInitData(this.baseItemRev);

            } catch (TCException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 조회시 Viewer Tab에서 호출
     * Revision 속성을 화면에 표시
     * 
     * @param targetRevision
     *            : Target Revision
     * @throws TCException
     */
    private void setInitData(TCComponentItemRevision targetRevision) throws TCException {

        partNoText.setText(targetRevision.getProperty("item_id"));
        partRevisionText.setText(targetRevision.getProperty("item_revision_id"));

        activityCB.setText(targetRevision.getProperty("s7_ACTIVATION"));
        materialTypeCB.setText(targetRevision.getProperty("s7_TYPE"));
        maturityCB.setText(targetRevision.getProperty("s7_MATURITY"));
        materialSrcCB.setText(targetRevision.getProperty("s7_SOURCE"));
        specNoText.setText(targetRevision.getProperty("s7_SPEC_NUMBER"));
        sesCodeText.setText(targetRevision.getProperty("s7_SES_CODE"));
        ksCodeText.setText(targetRevision.getProperty("s7_KS_CODE"));
        jisCodeText.setText(targetRevision.getProperty("s7_JIS_CODE"));
        dinCodeText.setText(targetRevision.getProperty("s7_DIN_CODE"));
        mbCodeText.setText(targetRevision.getProperty("s7_MB_CODE"));
        saeCodeText.setText(targetRevision.getProperty("s7_SAE_CODE"));
        gbCodeText.setText(targetRevision.getProperty("s7_GB_CODE"));
        supCodeText.setText(targetRevision.getProperty("s7_SUP_CODE"));
        otherCodeText.setText(targetRevision.getProperty("s7_OTHER_CODE"));
        densityText.setText(this.getFormatedString(targetRevision.getDoubleProperty("s7_DENSITY"), "########.#####"));
        descText.setText(targetRevision.getProperty("object_desc"));

        if (this.targetRevision != null) {
            // 데이터 셋
            try {
                fileComposite.roadDataSet(targetRevision);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.post(getShell(), e.toString(), "ERROR in setInitData()", MessageBox.ERROR);
            }
        }

        // Material Source 유형에 따라 Part ID 발번 로직이 다름
        // ex) 유형Code(Spec No.)
        String strMatSource = targetRevision.getProperty("s7_SOURCE");

        if ("SES".equals(strMatSource)) {
            sesCodeText.setEditable(false);
            sesCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if ("KS".equals(strMatSource)) {

            ksCodeText.setEditable(false);
            ksCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if ("JIS".equals(strMatSource)) {
            jisCodeText.setEditable(false);
            jisCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if ("DIN".equals(strMatSource)) {
            dinCodeText.setEditable(false);
            dinCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if ("MB".equals(strMatSource)) {
            mbCodeText.setEditable(false);
            mbCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if ("SAE".equals(strMatSource)) {
            saeCodeText.setEditable(false);
            saeCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if ("SUPPLY".equals(strMatSource)) {
            supCodeText.setEditable(false);
            supCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if ("OTHERS".equals(strMatSource)) {
            otherCodeText.setEditable(false);
            otherCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if ("GB".equals(strMatSource)) {
            gbCodeText.setEditable(false);
            gbCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        }

    }

    public String getFormatedString(double value, String format) {

        DecimalFormat df = new DecimalFormat(format);
        return df.format(value);
    }

    /**
     * CheckIn시 Validation Check
     * 
     * @return
     */
    public boolean isSavable() {
        try {
            this.getPropDataMap(this.attrMap);
            MatPartValidator validator = new MatPartValidator();
            String strMessage = validator.validate(this.attrMap, MatPartValidator.TYPE_VALID_MODIFY);

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
            this.attrMap.remove("item_id");

            String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
            TCProperty[] props = targetRevision.getTCProperties(szKey);

            for (int i = 0; i < props.length; i++) {

                if (props[i] == null) {
                    System.out.println(szKey[i] + " is Null");
                    continue;
                }

                Object value = attrMap.get(szKey[i]);
                CustomUtil.setObjectToPropertyValue(props[i], value);

            }

            targetRevision.setTCProperties(props);

            if (fileComposite.isFileModified()) {
                fileComposite.createDatasetAndMakerelation(targetRevision);
            }

            targetRevision.refresh();

            // targetRevision.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 화면 초기화
     */
    private void initUI() {
        setBackground(new Color(null, 255, 255, 255));
        setLayout(new GridLayout(1, false));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginTop = 5;
        groupLayout.marginBottom = 5;
        groupLayout.marginLeft = 5;
        groupLayout.marginRight = 5;

        Group composite = new Group(this, SWT.NONE);
        composite.setLayout(groupLayout);
        composite.setBackground(new Color(null, 255, 255, 255));
        composite.setText("Material Part Info");

        // /////////////////////////////////////////////////////////////////////////////////////////
        // ///////////////////////////////////////Basic Info Start//////////////////////////////////
        // /////////////////////////////////////////////////////////////////////////////////////////

        FormData labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(0, 5);

        SYMCLabel partNoLabel = new SYMCLabel(composite, "Material No ", labelFormData);
        FormData textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(partNoLabel);

        partNoText = new SYMCText(composite, true, textFormData);
        partNoText.setEnabled(false);
        partNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        textFormData = new FormData(20, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(partNoText, 4);
        partRevisionText = new SYMCText(composite, textFormData);
        partRevisionText.setEnabled(false);
        partRevisionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        partRevisionText.setText("000");

        labelFormData = new FormData(97, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(partRevisionText, 27);
        SYMCLabel activityLabel = new SYMCLabel(composite, "Activity", labelFormData);
        textFormData = new FormData(207, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(activityLabel, 5);
        textFormData.height = 20;
        activityCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_YN");
        activityCB.setMandatory(true);
        activityCB.setLayoutData(textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partNoText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel materialTypeLabel = new SYMCLabel(composite, "Material Type", labelFormData);
        textFormData = new FormData(207, 18);
        textFormData.top = new FormAttachment(partNoText, 5);
        textFormData.left = new FormAttachment(materialTypeLabel, 5);
        textFormData.height = 20;
        materialTypeCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_MATERIAL_TYPE");
        materialTypeCB.setMandatory(true);
        materialTypeCB.setLayoutData(textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partNoText, 5);
        labelFormData.left = new FormAttachment(materialTypeCB, 57);
        SYMCLabel maturityLabel = new SYMCLabel(composite, "Maturity", labelFormData);
        textFormData = new FormData(207, 18);
        textFormData.top = new FormAttachment(partNoText, 5);
        textFormData.left = new FormAttachment(maturityLabel, 5);
        textFormData.height = 20;
        maturityCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_MATURITY");
        maturityCB.setMandatory(true);
        maturityCB.setLayoutData(textFormData);
        maturityCB.setText("In Work");
        maturityCB.setEnabled(false);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(maturityCB, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel materialSrcLabel = new SYMCLabel(composite, "Material Source", labelFormData);
        textFormData = new FormData(207, 18);
        textFormData.top = new FormAttachment(maturityCB, 5);
        textFormData.left = new FormAttachment(materialSrcLabel, 5);
        textFormData.height = 20;
        materialSrcCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_MATERIAL_SOURCE");
        materialSrcCB.setMandatory(true);
        materialSrcCB.setLayoutData(textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(maturityCB, 5);
        labelFormData.left = new FormAttachment(materialSrcCB, 57);
        SYMCLabel specNoLabel = new SYMCLabel(composite, "Spec No", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(maturityCB, 5);
        textFormData.left = new FormAttachment(specNoLabel, 5);
        specNoText = new SYMCText(composite, true, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(specNoText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel sesCodeLabel = new SYMCLabel(composite, "SES Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(specNoText, 5);
        textFormData.left = new FormAttachment(sesCodeLabel, 5);
        sesCodeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(specNoText, 5);
        labelFormData.left = new FormAttachment(sesCodeText, 57);
        SYMCLabel ksCodeLabel = new SYMCLabel(composite, "KS Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(specNoText, 5);
        textFormData.left = new FormAttachment(ksCodeLabel, 5);
        ksCodeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(ksCodeText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel jisLabel = new SYMCLabel(composite, "JIS Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(ksCodeText, 5);
        textFormData.left = new FormAttachment(jisLabel, 5);
        jisCodeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(ksCodeText, 5);
        labelFormData.left = new FormAttachment(jisCodeText, 57);
        SYMCLabel dinCodeLabel = new SYMCLabel(composite, "DIN Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(ksCodeText, 5);
        textFormData.left = new FormAttachment(dinCodeLabel, 5);
        dinCodeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(dinCodeText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel mbCodeLabel = new SYMCLabel(composite, "MB Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(dinCodeText, 5);
        textFormData.left = new FormAttachment(mbCodeLabel, 5);
        mbCodeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(dinCodeText, 5);
        labelFormData.left = new FormAttachment(mbCodeText, 57);
        SYMCLabel saeCodeLabel = new SYMCLabel(composite, "SAE Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(dinCodeText, 5);
        textFormData.left = new FormAttachment(saeCodeLabel, 5);
        saeCodeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(saeCodeText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel bbCodeLabel = new SYMCLabel(composite, "GB Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(saeCodeText, 5);
        textFormData.left = new FormAttachment(bbCodeLabel, 5);
        gbCodeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(saeCodeText, 5);
        labelFormData.left = new FormAttachment(gbCodeText, 57);
        SYMCLabel supCodeLabel = new SYMCLabel(composite, "Supplier Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(saeCodeText, 5);
        textFormData.left = new FormAttachment(supCodeLabel, 5);
        supCodeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(supCodeText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel otherCodeLabel = new SYMCLabel(composite, "Other Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(supCodeText, 5);
        textFormData.left = new FormAttachment(otherCodeLabel, 5);
        otherCodeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(supCodeText, 5);
        labelFormData.left = new FormAttachment(gbCodeText, 57);
        SYMCLabel densityLabel = new SYMCLabel(composite, "Density", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(supCodeText, 5);
        textFormData.left = new FormAttachment(densityLabel, 5);
        densityText = new SYMCText(composite, textFormData);
        densityText.setInputType(SYMCText.DOUBLE);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(densityText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel descLabel = new SYMCLabel(composite, "Description", labelFormData);
        textFormData = new FormData(573, 18);
        textFormData.top = new FormAttachment(densityText, 5);
        textFormData.left = new FormAttachment(descLabel, 5);
        descText = new SYMCText(composite, textFormData);

        GridData layoutData = new GridData(SWT.NONE, SWT.NONE, true, false);
        layoutData.minimumHeight = 250;
        fileComposite = new FileAttachmentComposite(this, layoutData);
        fileComposite.group.setSize(new Point(680, 200));

        //  [SR140610-43][20140609] KOG
//        LimitValueListener 추가
        setLimitValueListener();

        this.pack();

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

        if (fileComposite.isFileModified()) {
            return true;
        }

        return false;
    }

    /**
     * [SR140610-43][20140609] KOG
     * Text Control에 대한 LimitValueListener Class
     */
    class CodeKeyListener implements KeyListener {

        private SYMCText text;
        private int limit;

        public CodeKeyListener(SYMCText text, int limit) {
            this.text = text;
            this.limit = limit;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            text.removeKeyListener(this);
            if (text.getText().getBytes().length > limit) {
                MessageBox.post(getShell(), limit + "Byte 이하로 입력 가능합니다.", "Material Part", MessageBox.WARNING);
                String value = text.getText();
                String subStringBytes = subStringBytes(value, limit);
                text.setText(subStringBytes);
                text.setSelection(text.getText().length());
            }
            text.addKeyListener(this);
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        public String subStringBytes(String str, int byteLength) {
            // String 을 byte 길이 만큼 자르기.
            int retLength = 0;
            int tempSize = 0;
            int asc;
            if (str == null || "".equals(str) || "null".equals(str)) {
                str = "";
            }
            int length = str.length();
            for (int i = 1; i <= length; i++) {
                asc = (int) str.charAt(i - 1);
                if (asc > 127) {
                    if (byteLength >= tempSize + 2) {
                        tempSize += 2;
                        retLength++;
                    } else {
                        return str.substring(0, retLength);
                    }
                } else {
                    if (byteLength > tempSize) {
                        tempSize++;
                        retLength++;
                    }
                }
            }
            return str.substring(0, retLength);
        }
    }
}
