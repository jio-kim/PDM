package com.ssangyong.commands.partmaster.productpart;


import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.SWT;
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
import com.ssangyong.commands.partmaster.validator.ProductPartValidator;
import com.ssangyong.common.SYMCLOVComboBox;
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
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * Product Part Information Panel
 * 
 */
public class ProdPartMasterInfoPanel extends Composite implements IPageComplete {

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

    /** Project Combo */
    protected SYMCLOVComboBox projCodeCB;
    /** Maturity Combo */
    protected SYMCLOVComboBox maturityCB;

    /** ��ȸ�� Target Revision */
    TCComponentItemRevision targetRevision;
    /** ȭ�鿡 �Էµ� �Ӽ� Map */
    HashMap<String, Object> attrMap;
    /** ���� Loading�� �Ӽ� Map(�����׸� Check�� ���� ���, attrMap�� oldAttrMap�� ��) */
    HashMap<String, Object> oldAttrMap;
    /** PartManage Dialog���� �Ѿ�� Param Map */
    HashMap<String, Object> paramMap;
    /** SaveAs�� Target Revison */
    TCComponentItemRevision baseItemRev;

    // [SR140702-058][20140630] KOG Prod. Part G-Model Text �߰�.
    private SYMCText gModelText;
    // [SR181205-063] ���� ���� Data ������ ���� Combobox�� ����
    //private SYMCText productTypeText;
    private SWTComboBox productTypeCombo;
    private SYMCText sopDateText;

    /**
     * Create Product Menu�� ���� ȣ���
     * 
     * @param parent
     * @param paramMap
     *            : PartManage Dialog���� �Ѿ�� Param Map
     * @param style
     *            : Dialog SWT Style
     */
    public ProdPartMasterInfoPanel(Composite parent, HashMap<String, Object> paramMap, int style) {
        super(parent, style);
        this.session = CustomUtil.getTCSession();
        this.attrMap = new HashMap<String, Object>();
        this.paramMap = paramMap;
        initUI();
        setControlData();
        this.setInitData(paramMap);

    }

    /**
     * Revision ���� �� ViewerTab���� ȣ��
     * 
     * @param parent
     * @param style
     */
    public ProdPartMasterInfoPanel(Composite parent, int style, boolean isViewMode) {
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
     * ��ȸȭ���� ��� �����Ұ� �׸� Setting
     */
    public void setViewMode() {

        prefixText.setEnabled(false);
        partNoText.setEnabled(false);
        partRevisionText.setEnabled(false);
        partNameText.setEnabled(false);
        maturityCB.setEnabled(false);

    }

    /**
     * ȭ�鿡 �Էµ� �Ӽ� ���� ����
     * 
     * @param attributeMap
     *            : �Ӽ����� ����� HashMap
     * @return
     * @throws Exception
     */
    public HashMap<String, Object> getPropDataMap(HashMap<String, Object> attributeMap) throws Exception {

        attributeMap.put("item_id", prefixText.getText() + partNoText.getText());
        attributeMap.put("item_revision_id", partRevisionText.getText());

        attributeMap.put("object_name", partNameText.getText());

        // [20240227] Select Item �� null �� ��� �������� ��ü
        String projCodeCBValue = projCodeCB.getSelectedString();
        attributeMap.put("s7_PROJECT_CODE", projCodeCBValue == null ? "" : projCodeCBValue);

        // [20240227] Select Item �� null �� ��� �������� ��ü
        String maturityCBValue = maturityCB.getSelectedString();
        attributeMap.put("s7_MATURITY", maturityCBValue == null ? "" : maturityCBValue);
        
        attributeMap.put("object_desc", descText.getText());

        // [SR140702-058][20140630] KOG Product Part attributeMap �� put Value �߰�.
        attributeMap.put("s7_GMODEL_CODE", gModelText.getText());
        // [SR181205-063] ���� ���� Data ������ ���� Combobox�� ����
        //attributeMap.put("s7_PRODUCT_TYPE", productTypeText.getText());
        
        // [20240227] Select Item �� null �� ��� �������� ��ü
        Object productTypeObj = productTypeCombo.getSelectedItem();
        attributeMap.put("s7_PRODUCT_TYPE", productTypeObj == null ? "" : productTypeObj);
        
        attributeMap.put("s7_SOP_DATE", CustomUtil.getDateFromStringDate(sopDateText.getText()));

        return attributeMap;
    }

    /**
     * Create Part ����� ���� ȣ��� ��� �Ӽ� �� Setting
     * 
     * @param paramMap
     *            : Part Manage Dialog���� �Ѿ�� Parameter Map
     */
    private void setInitData(HashMap<String, Object> paramMap) {

        this.baseItemRev = (TCComponentItemRevision) paramMap.get(Constants.ATTR_NAME_BASEITEMID);
        if (baseItemRev != null) {
            try {
                this.setInitData(this.baseItemRev);
                // ItemID �ʱ�ȭ
                partNoText.setText("");
                partRevisionText.setText("000");
                partNameText.setText("");

            } catch (TCException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ��ȸ�� Viewer Tab���� ȣ��
     * Revision �Ӽ��� ȭ�鿡 ǥ��
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
        // [SR140702-059][20140626] KOG Variant Part SaveAs �ÿ� ProjectCode Blank ó��.
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
        descText.setText(targetRevision.getProperty("object_desc"));

        // [SR140702-058][20140630] KOG Product Part Set Init Data (G-Model Code, Product Type, SOP Date) �߰�.
        gModelText.setText(targetRevision.getStringProperty("s7_GMODEL_CODE"));
        // [SR181205-063] ���� ���� Data ������ ���� Combobox�� ����
        //productTypeText.setText(targetRevision.getStringProperty("s7_PRODUCT_TYPE"));
        productTypeCombo.setSelectedObject(targetRevision.getStringProperty("s7_PRODUCT_TYPE"));
        Date dateProperty = targetRevision.getDateProperty("s7_SOP_DATE");
        if (dateProperty != null) {
            sopDateText.setText(CustomUtil.getStringDateFromDate(dateProperty));
        }
    }

    public String getFormatedString(double value, String format) {

        DecimalFormat df = new DecimalFormat(format);
        return df.format(value);
    }

    /**
     * CheckIn�� Validation Check
     * 
     * @return
     */
    public boolean isSavable() {
        try {
            this.getPropDataMap(this.attrMap);
            ProductPartValidator validator = new ProductPartValidator();
            String strMessage = validator.validate(this.attrMap, ProductPartValidator.TYPE_VALID_MODIFY);

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
            targetRevision.refresh();

            // targetRevision.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ȭ�� �ʱ�ȭ
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

        projCodeCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_PROJECT_CODE");
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

        maturityCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_MATURITY");
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
        SYMCLabel productTypeLabel = new SYMCLabel(composite, "Product Type", labelFormData);
        textFormData = new FormData(206, 18);
        textFormData.top = new FormAttachment(descText, 5);
        textFormData.left = new FormAttachment(productTypeLabel, 5);
        // [SR181205-063] ���� ���� Data ������ ���� Combobox�� ����
        productTypeCombo = new SWTComboBox(composite, SWT.BORDER);
        productTypeCombo.setLayoutData(textFormData);
        productTypeCombo.addItem("", "");
        productTypeCombo.addItem("Vehicle", "Vehicle");
        productTypeCombo.addItem("Engine", "Engine");
        productTypeCombo.addItem("Transmission", "Trencemission");
        productTypeCombo.addItem("Axle", "Axle");
        //Keyin �� ���� ��Ī�Ǵ°͸� ǥ���ϰ� unMatching�Ǵ°��� keyin�Ұ��ϵ��� �ϴ� �ɼ�
        productTypeCombo.setAutoCompleteSuggestive(false);
        
        //productTypeText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        // [SR181205-063] ���� ���� Data ������ ���� Combobox�� ����
        //labelFormData.top = new FormAttachment(productTypeText, 5);
        labelFormData.top = new FormAttachment(productTypeCombo, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel sopDateLabel = new SYMCLabel(composite, "SOP Date", labelFormData);
        textFormData = new FormData(202, 18);
        //textFormData.top = new FormAttachment(productTypeText, 5);
        textFormData.top = new FormAttachment(productTypeCombo, 5);
        textFormData.left = new FormAttachment(sopDateLabel, 5);
        sopDateText = new SYMCText(composite, textFormData);
        sopDateText.setEditable(false);
        textFormData = new FormData(26, 26);
        //textFormData.top = new FormAttachment(productTypeText, 5);
        textFormData.top = new FormAttachment(productTypeCombo, 5);
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
    }

    private void setControlData() {
        // actWeightText.setInputType(SYMCText.DOUBLE);
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }

    /**
     * TC ViewerTab���� ȭ�� �̵��� ������ �׸��� �����ϴ��� Check
     * 
     * ���� Loading�� �Ӽ����� ���� �Ӽ����� ��
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
