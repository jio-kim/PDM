package com.symc.plm.rac.prebom.prebom.view.prefuncmaster;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ssangyong.common.SYMCLOVComboBox;
import com.ssangyong.common.SYMCLabel;
import com.ssangyong.common.SYMCText;
import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.prebom.dialog.preccn.CCNSearchDialog;
import com.symc.plm.rac.prebom.prebom.validator.prefuncmaster.PreFuncMasterValidator;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Function Master Part Information Panel
 * 
 */
public class PreFuncMasterInfoPanel extends Composite implements IPageComplete {

    /** TC Registry */
    private Registry registry;
    /** TC Session */
    private TCSession session;

    /** Part Prefix No. Field */
    protected SYMCText prefixText;
    /** Part No. Field */
    protected SYMCText partNoText;
    /** Part Rev. Field */
    protected SYMCText partRevisionText;
    /** Part Name Field */
    protected SYMCText partNameText;
    /** Description Field */
    protected SYMCText descText;
    /** CCN No. Field */
    protected SYMCText ccnNoText;

    /** Project Code Combo */
    protected SYMCLOVComboBox projCodeCB;
    /** Maturity Combo */
    protected SYMCLOVComboBox maturityCB;

    /** 조회시 Target Revision */
    TCComponentItemRevision targetRevision;
    /** 화면에 입력된 속성 Map */
    public HashMap<String, Object> attrMap;
    /** 최초 Loading시 속성 Map(수정항목 Check를 위해 사용, attrMap과 oldAttrMap을 비교) */
    HashMap<String, Object> oldAttrMap;
    /** SaveAs시 Target Revison */
    TCComponentItemRevision baseItemRev;
    // [SR140702-059][20140626] KOG Fnc. Mast. Part SaveAs 시에 isSaveAs Flag.
    private boolean isSaveAS = false;

    // [SR140702-058][20140630] KOG Fnc. Mast. Part G-Model Text 추가.
    private SYMCText gModelText;

    // [SR140702-058][20140630] KOG Fnc. Mast. Part Function Type Text 추가.
    private SYMCText funcTypeText;

    // [SR140702-058][20140630] KOG Fnc. Mast. Part SOP Date Text 추가.
//    private StyledText sopDateText;
    private Button ccnNoButton;

    /**
     * Create Product Menu를 통해 호출됨
     * 
     * @param parent
     * @param paramMap
     *            : PartManage Dialog에서 넘어온 Param Map
     * @param style
     *            : Dialog SWT Style
     */
    public PreFuncMasterInfoPanel(Composite parent, HashMap<String, Object> paramMap, int style) {
        super(parent, style);
        this.registry = Registry.getRegistry("com.symc.plm.rac.prebom.prebom.view.view");
        this.session = CustomUtil.getTCSession();
        this.attrMap = new HashMap<String, Object>();

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
    public PreFuncMasterInfoPanel(Composite parent, int style, boolean isViewMode) {
        super(parent, style);
        registry = Registry.getRegistry("com.symc.plm.rac.prebom.prebom.view.view");
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
//        partNameText.setEnabled(false);
        maturityCB.setEnabled(false);
//        projCodeCB.setEnabled(false);
        ccnNoButton.setEnabled(false);
//        gModelText.setEnabled(false);
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
        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMTYPE, TypeConstant.S7_PREFUNCMASTERTYPE);

        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMID, prefixText.getText() + partNoText.getText());
        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMREVID, partRevisionText.getText());

        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMNAME, partNameText.getText());

        attributeMap.put(PropertyConstant.ATTR_NAME_PROJCODE, projCodeCB.getSelectedString());
        attributeMap.put(PropertyConstant.ATTR_NAME_MATURITY, maturityCB.getSelectedString());

        // CCN No.(TypedReference)
        attributeMap.put(PropertyConstant.ATTR_NAME_CCNNO, ccnNoText.getData());

        // [SR140702-058][20140630] KOG Product Part attributeMap 에 put Value 추가.
        attributeMap.put(PropertyConstant.ATTR_NAME_GMODELCODE, gModelText.getText());
        attributeMap.put("s7_FUNCTION_TYPE", funcTypeText.getText());
//        attributeMap.put("s7_SOP_DATE", CustomUtil.getDateFromStringDate(sopDateText.getText()));

        attributeMap.put(PropertyConstant.ATTR_NAME_ITEMDESC, descText.getText());

        return attributeMap;
    }

    /**
     * Create Part 기능을 통해 호출된 경우 속성 값 Setting
     * 
     * @param paramMap
     *            : Part Manage Dialog에서 넘어온 Parameter Map
     */
    private void setInitData(HashMap<String, Object> paramMap) {

        this.baseItemRev = (TCComponentItemRevision) paramMap.get("baseitem");//Constants.ATTR_NAME_BASEITEMID);
        if (baseItemRev != null) {
            try {
                // [SR140702-059][20140626] KOG Fnc. Master Part SaveAs 시에 isSaveAs Flag 처리.
                String strSaveAs = (String) paramMap.get("SaveAs");//Constants.COMMAND_SAVE_AS);
                if (strSaveAs != null && strSaveAs.equals("TRUE")) {
                    this.isSaveAS = true;
                } else {
                    this.isSaveAS = false;
                }
                this.setInitData(this.baseItemRev);
                // ItemID 초기화
                partNoText.setText("");
                partRevisionText.setText("000");
                partNameText.setText("");
                maturityCB.setText("In Work");

                TCComponentItemRevision ecoItemRev = (TCComponentItemRevision) paramMap.get("ecoitem");//Constants.ATTR_NAME_ECOITEMID);

                if (ecoItemRev != null) {
                    ccnNoText.setText(ecoItemRev.toDisplayString());
                    ccnNoText.setData(ecoItemRev);
                }

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
        if (targetRevision == null)
            return;

        String itemID = targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
        prefixText.setText(itemID.substring(0, 1));
        partNoText.setText(itemID.substring(1, itemID.length()));
        partRevisionText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID));
        partNameText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME));
        // [SR140702-059][20140626] KOG Fnc. Master Part SaveAs 시에 ProjectCode Blank 처리.
        if (isSaveAS) {
            projCodeCB.setSelectedIndex(0);
        } else {
            projCodeCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE));
        }
        maturityCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_MATURITY));

        // CCN No.(TypedReference)
        TCComponent ccnComp = targetRevision.getReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO);
        if (ccnComp != null) {
            ccnNoText.setText(ccnComp.toDisplayString());
            ccnNoText.setData(ccnComp);
        }

        // [SR140702-058][20140630] KOG Product Part Set Init Data (G-Model Code, Product Type, SOP Date) 추가.
        gModelText.setText(targetRevision.getStringProperty(PropertyConstant.ATTR_NAME_GMODELCODE));
        funcTypeText.setText(targetRevision.getStringProperty("s7_FUNCTION_TYPE"));
//        Date dateProperty = targetRevision.getDateProperty("s7_SOP_DATE");
//        if (dateProperty != null) {
//            sopDateText.setText(CustomUtil.getStringDateFromDate(dateProperty));
//        }

        descText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC));

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
            this.getPropDataMap(this.attrMap);
            PreFuncMasterValidator validator = new PreFuncMasterValidator();
            String strMessage = validator.validate(this.attrMap, PreFuncMasterValidator.TYPE_VALID_MODIFY);

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
        prefixText.setText("M");

        textFormData = new FormData(180, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(prefixText);
        partNoText = new SYMCText(composite, true, textFormData);
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
        SYMCLabel ecoNoLabel = new SYMCLabel(composite, "CCN No", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(maturityCB, 5);
        textFormData.left = new FormAttachment(ecoNoLabel, 5);
        ccnNoText = new SYMCText(composite, textFormData);
        ccnNoText.setEnabled(false);
        ccnNoText.setMandatory(true);
        ccnNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        FormData buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(maturityCB, 5);
        buttonFormData.left = new FormAttachment(ccnNoText, 5);
        ccnNoButton = new Button(composite, SWT.PUSH);
        ccnNoButton.setImage(registry.getImage("Search.ICON"));
        ccnNoButton.setLayoutData(buttonFormData);
        ccnNoButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                CCNSearchDialog ccnSearchDialog = new CCNSearchDialog(getShell(), SWT.SINGLE);
                ccnSearchDialog.getShell().setText("CCN Search");
                ccnSearchDialog.open();
                TCComponentItemRevision[] ccns = ccnSearchDialog.getSelectctedECO();
                if (ccns != null && ccns.length > 0) {
                    TCComponentItemRevision ccnIR = ccns[0];
                    ccnNoText.setText(ccnIR.toDisplayString());
                    ccnNoText.setData(ccnIR);
                }
            }
        });

        // [SR140702-058][20140630] KOG Fnc. Mast. Part Init UI G-Model.
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(maturityCB, 5);
        labelFormData.left = new FormAttachment(ccnNoText, 55);
        SYMCLabel gModelLabel = new SYMCLabel(composite, "G-Model Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(maturityCB, 5);
        textFormData.left = new FormAttachment(gModelLabel, 5);
        gModelText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(gModelText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel funcLabel = new SYMCLabel(composite, "Function Type", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(gModelText, 5);
        textFormData.left = new FormAttachment(funcLabel, 5);
        funcTypeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(funcTypeText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel descLabel = new SYMCLabel(composite, "Description", labelFormData);
        textFormData = new FormData(573, 18);
        textFormData.top = new FormAttachment(funcTypeText, 5);
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
