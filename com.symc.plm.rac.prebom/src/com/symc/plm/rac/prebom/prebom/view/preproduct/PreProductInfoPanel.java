package com.symc.plm.rac.prebom.prebom.view.preproduct;

import java.awt.Frame;
import java.awt.Panel;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JRootPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.common.SYMCLOVComboBox;
import com.kgm.common.SYMCLabel;
import com.kgm.common.SYMCText;
import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.prebom.validator.preproduct.PreProductValidator;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.stylesheet.PropertyArray;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;

/**
 * Product Part Information Panel
 * 
 */
public class PreProductInfoPanel extends Composite implements IPageComplete {

    /** TC Session */
    private TCSession session;

    /** PreFix Part ID */
    protected SYMCText prefixText;
    /** Part No. */
    protected SYMCText partNoText;
    /** Part Rev. ID */
    protected SYMCText partRevisionText;
    /** Part Name */
    protected SYMCText partNameText;
    /** Description */
    protected SYMCText descText;
    /** Gate */
    protected SYMCText gateText;

    /** Project Combo */
    protected SYMCLOVComboBox projCodeCB;
    /** Maturity Combo */
    protected SYMCLOVComboBox maturityCB;

    /** 조회시 Target Revision */
    TCComponentItemRevision targetRevision;
    /** 화면에 입력된 속성 Map */
    public HashMap<String, Object> attrMap;
    /** 최초 Loading시 속성 Map(수정항목 Check를 위해 사용, attrMap과 oldAttrMap을 비교) */
    HashMap<String, Object> oldAttrMap;
    /** PartManage Dialog에서 넘어온 Param Map */
    HashMap<String, Object> paramMap;
    /** SaveAs시 Target Revison */
    TCComponentItemRevision baseItemRev;

    // [SR140702-058][20140630] KOG Prod. Part G-Model Text 추가.
    private SYMCText gModelText;
    private SYMCLOVComboBox projectTypeCB;
    private PropertyArray listContents;
//    private SYMCText sopDateText;

    /**
     * Create Product Menu를 통해 호출됨
     * 
     * @param parent
     * @param paramMap
     *            : PartManage Dialog에서 넘어온 Param Map
     * @param style
     *            : Dialog SWT Style
     */
    public PreProductInfoPanel(Composite parent, HashMap<String, Object> paramMap, int style) {
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
    public PreProductInfoPanel(Composite parent, int style, boolean isViewMode) {
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
     * 조회화면인 경우 수정불가 항목 Setting
     */
    public void setViewMode() {

        prefixText.setEnabled(false);
        partNoText.setEnabled(false);
        partRevisionText.setEnabled(false);
//        partNameText.setEnabled(true);
        maturityCB.setEnabled(false);
        gateText.setEnabled(false);
//        projCodeCB.setEnabled(true);
//        descText.setEnabled(true);
//        gModelText.setEnabled(true);
//        projectTypeCB.setEnabled(true);
//        listContents.setModifiable(true);
//
//        if (targetRevision != null && targetRevision.isCheckedOut())
//        {
//            partNameText.setEnabled(true);
//            projCodeCB.setEnabled(true);
//            descText.setEnabled(true);
//            gModelText.setEnabled(true);
//            projectTypeCB.setEnabled(true);
//            listContents.setModifiable(true);
//        }
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
        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMTYPE, TypeConstant.S7_PREPRODUCTTYPE);

        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMID, prefixText.getText() + partNoText.getText());
        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMREVID, partRevisionText.getText());

        attributeMap.put(PropertyConstant.ATTR_NAME_GATENO, gateText.getText());

        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMNAME, partNameText.getText());

        attributeMap.put(PropertyConstant.ATTR_NAME_PROJCODE, projCodeCB.getSelectedString());
        attributeMap.put(PropertyConstant.ATTR_NAME_MATURITY, maturityCB.getSelectedString());
        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMDESC, descText.getText());

        // [SR140702-058][20140630] KOG Product Part attributeMap 에 put Value 추가.
        attributeMap.put(PropertyConstant.ATTR_NAME_GMODELCODE, gModelText.getText());
        attributeMap.put(PropertyConstant.ATTR_NAME_PROJECTTYPE, projectTypeCB.getSelectedString());
//        attributeMap.put("s7_SOP_DATE", CustomUtil.getDateFromStringDate(sopDateText.getText()));

        List<String> arrayValue = Arrays.asList(listContents.getValues());
        String arrayToString = "";
        for (int i = 0; i < arrayValue.size(); i++)
            arrayToString = arrayToString + (i == 0 ? "" : "|") + arrayValue.get(i);

        attributeMap.put(PropertyConstant.ATTR_NAME_CONTENTS, arrayToString);

        return attributeMap;
    }

    /**
     * Create Part 기능을 통해 호출된 경우 속성 값 Setting
     * 
     * @param paramMap
     *            : Part Manage Dialog에서 넘어온 Parameter Map
     */
    private void setInitData(HashMap<String, Object> paramMap) {

        // 여기는 임시로 막음.
//        this.baseItemRev = (TCComponentItemRevision) paramMap.get(PropertyConstant.ATTR_NAME_BASEITEMID);
//        if (baseItemRev != null) {
//            try {
//                this.setInitData(this.baseItemRev);
//                // ItemID 초기화
//                partNoText.setText("");
//                partRevisionText.setText("000");
//                partNameText.setText("");
//
//            } catch (TCException e) {
//                e.printStackTrace();
//            }
//        }
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

        if (targetRevision == null)
            return;

        String itemID = targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
        prefixText.setText(itemID.substring(0, 1));
        partNoText.setText(itemID.substring(1, itemID.length()));

        partRevisionText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID));
        gateText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_GATENO));
        partNameText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME));
        // [SR140702-059][20140626] KOG Variant Part SaveAs 시에 ProjectCode Blank 처리.
        if (paramMap != null) {
            // 여기도 임시로 막음.
//            String strSaveAs = (String) paramMap.get(PropertyConstant.COMMAND_SAVE_AS);
//            if (strSaveAs != null && strSaveAs.equals("TRUE")) {
//                projCodeCB.setSelectedIndex(0);
//            } else {
                projCodeCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE));
//            }
        } else {
            projCodeCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE));
        }
        maturityCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_MATURITY));
        descText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC));

        // [SR140702-058][20140630] KOG Product Part Set Init Data (G-Model Code, Product Type, SOP Date) 추가.
        gModelText.setText(targetRevision.getStringProperty(PropertyConstant.ATTR_NAME_GMODELCODE));
        projectTypeCB.setSelectedString(targetRevision.getStringProperty(PropertyConstant.ATTR_NAME_PROJECTTYPE));
        try {
            listContents.load(targetRevision.getTCProperty(PropertyConstant.ATTR_NAME_CONTENTS));
        } catch (Exception e) {
            e.printStackTrace();
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
            PreProductValidator validator = new PreProductValidator();
            String strMessage = validator.validate(this.attrMap, PreProductValidator.TYPE_VALID_MODIFY);

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

            this.attrMap.remove(PropertyConstant.ATTR_NAME_ITEMID);
            this.attrMap.remove(PropertyConstant.ATTR_NAME_ITEMTYPE);
            this.attrMap.remove(PropertyConstant.ATTR_NAME_ITEMREVID);

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
        prefixText.setText("P");

        textFormData = new FormData(180, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(prefixText);
        partNoText = new SYMCText(composite, true, textFormData);
        partNoText.setTextLimit(7);

        textFormData = new FormData(20, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(partNoText, 4);
        partRevisionText = new SYMCText(composite, textFormData);
        partRevisionText.setEnabled(false);
        partRevisionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        partRevisionText.setText("000");

        labelFormData = new FormData(120, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(partRevisionText, 5);
        SYMCLabel gateNoLabel = new SYMCLabel(composite, "Gate No.", labelFormData);
        textFormData = new FormData(200, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(gateNoLabel, 5);
        gateText = new SYMCText(composite, true, textFormData);
        gateText.setInputType(SYMCText.NUMERIC);
        gateText.setTextLimit(3);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partRevisionText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel partNameLabel = new SYMCLabel(composite, "Part Name", labelFormData);
        textFormData = new FormData(573, 18);
        textFormData.top = new FormAttachment(partRevisionText, 5);
        textFormData.left = new FormAttachment(partNameLabel, 5);
        partNameText = new SYMCText(composite, true, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partNameText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel projCodeLabel = new SYMCLabel(composite, "Project Code", labelFormData);
        textFormData = new FormData(206, 18);
        textFormData.top = new FormAttachment(partNameText, 5);
        textFormData.left = new FormAttachment(projCodeLabel, 5);
        textFormData.height = 20;

        projCodeCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, PropertyConstant.ATTR_NAME_PROJCODE);
        projCodeCB.setLayoutData(textFormData);
        projCodeCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partNameText, 5);
        labelFormData.left = new FormAttachment(projCodeCB, 57);
        SYMCLabel maturityLabel = new SYMCLabel(composite, "Maturity", labelFormData);
        textFormData = new FormData(206, 18);
        textFormData.top = new FormAttachment(partNameText, 5);
        textFormData.left = new FormAttachment(maturityLabel, 5);
        textFormData.height = 20;

        maturityCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, PropertyConstant.ATTR_NAME_MATURITY);
        maturityCB.setLayoutData(textFormData);
        maturityCB.setText("In Work");
        maturityCB.setEnabled(false);
        maturityCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(maturityCB, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel descLabel = new SYMCLabel(composite, "Description", labelFormData);
        textFormData = new FormData(573, 18);
        textFormData.top = new FormAttachment(maturityCB, 5);
        textFormData.left = new FormAttachment(descLabel, 5);
        descText = new SYMCText(composite, textFormData);

        // [SR140702-058][20140630] KOG Prod. Part Init UI G-Model.
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(descText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel gModelLabel = new SYMCLabel(composite, "G-Model Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(descText, 5);
        textFormData.left = new FormAttachment(gModelLabel, 5);
        gModelText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(descText, 5);
        labelFormData.left = new FormAttachment(gModelText, 55);
        SYMCLabel projectTypeLabel = new SYMCLabel(composite, "Project Type", labelFormData);
        textFormData = new FormData(206, 18);
        textFormData.top = new FormAttachment(descText, 5);
        textFormData.left = new FormAttachment(projectTypeLabel, 5);
        projectTypeCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, PropertyConstant.ATTR_NAME_PROJECTTYPE);
        projectTypeCB.setLayoutData(textFormData);
        projectTypeCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(gModelText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel contentsLabel = new SYMCLabel(composite, "Contents", labelFormData);

        textFormData = new FormData(580, 120);
        textFormData.top = new FormAttachment(gModelText, 5);
        textFormData.left = new FormAttachment(contentsLabel, 5);
        Composite arrayComposite = new Composite(composite, SWT.EMBEDDED);
        arrayComposite.setLayoutData(textFormData);

        Frame frame = SWT_AWT.new_Frame(arrayComposite);
        
        Panel panel = new Panel();
        frame.add(panel);
        panel.setLayout(new java.awt.BorderLayout(0, 0));

        JRootPane rootPane = new JRootPane();
        panel.add(rootPane);
        listContents = new PropertyArray();
        rootPane.getContentPane().add(listContents);

        try {
            TCComponentType productType = null;
            productType = session.getTypeComponent(TypeConstant.S7_PREPRODUCTREVISIONTYPE);
            listContents.setProperty(PropertyConstant.ATTR_NAME_CONTENTS);
            try {
                listContents.load(productType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            listContents.setModifiable(true);
        } catch (TCException e) {
            e.printStackTrace();
        }
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
