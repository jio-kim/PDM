package com.kgm.commands.partmaster.stdpart;

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

import com.kgm.commands.partmaster.Constants;
import com.kgm.commands.partmaster.validator.StdPartValidator;
import com.kgm.common.SYMCLOVComboBox;
import com.kgm.common.SYMCLOVComboBox10;
import com.kgm.common.SYMCLabel;
import com.kgm.common.SYMCText;
import com.kgm.common.swtsearch.SearchItemRevDialog;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Standard Part Information Panel
 * 
 */
public class StdPartMasterInfoPanel extends Composite implements IPageComplete {
    /** TC Registry */
    private Registry registry;
    /** TC Session */
    private TCSession session;

    /** Part No. Field */
    protected SYMCText partNoText;
    /** Part Rev. Field */
    protected SYMCText partRevisionText;
    /** Display Part No. Field */
    protected SYMCText dispPartNoText;
    /** Part Name Field */
    protected SYMCText partNameText;
    /** Kor Name Field */
    protected SYMCText koreanNameText;
    /** Actual Weight Field */
    protected SYMCText actWeightText;
    /** Finish Field */
    protected SYMCText fisnishText;
    /** Design Cost Field */
    protected SYMCText designCostText;
    /** Design Est. Field */
    protected SYMCText designEstText;
    /** Referenct No. Field */
    protected SYMCText refNoText;
    /** Description Field */
    protected SYMCText descText;
    /** SES Spec No. Field */
    // [SR140324-030][20140619] KOG DEV SES Spec No Field �߰� Std. Part
    protected SYMCText sesSpecNoText;
    /** ALC Code Field */
    // [SR140729-026][20140808] jclee ALC Code Field �߰� Std. Part
    protected SYMCText alcCodeText;

    /** Material Field */
    protected SYMCText materialText;
    /** Alter Material Field */
    protected SYMCText alterMaterialText;

    /** Unit Combo */
    protected SYMCLOVComboBox unitCB;
    /** Maturity Combo */
    protected SYMCLOVComboBox maturityCB;

    /** SaveAs�� Target Revison */
    TCComponentItemRevision baseItemRev;
    /** ��ȸ�� Target Revision */
    TCComponentItemRevision targetRevision;
    /** ȭ�鿡 �Էµ� �Ӽ� Map */
    HashMap<String, Object> attrMap;
    /** ���� Loading�� �Ӽ� Map(�����׸� Check�� ���� ���, attrMap�� oldAttrMap�� ��) */
    HashMap<String, Object> oldAttrMap;
    /** PartManage Dialog���� �Ѿ�� Param Map */
    HashMap<String, Object> paramMap;

    /**
     * Create Part Menu�� ���� ȣ���
     * 
     * @param parent
     * @param paramMap
     *            : PartManage Dialog���� �Ѿ�� Param Map
     * @param style
     *            : Dialog SWT Style
     */
    public StdPartMasterInfoPanel(Composite parent, HashMap<String, Object> paramMap, int style) {
        super(parent, style);
        this.registry = Registry.getRegistry(this);
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
    public StdPartMasterInfoPanel(Composite parent, int style) {
        super(parent, style);
        registry = Registry.getRegistry(this);
        session = CustomUtil.getTCSession();
        attrMap = new HashMap<String, Object>();
        oldAttrMap = new HashMap<String, Object>();
        initUI();
        setControlData();
        setViewMode();
        try {
            InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
            // Target�� Revision�� ���
            if (comp != null && comp instanceof TCComponentItemRevision) {
                targetRevision = (TCComponentItemRevision) comp;
                this.setInitData(targetRevision);
                this.getPropDataMap(this.oldAttrMap);
            }
            // Target�� BomLine�� ���
            else if (comp != null && comp instanceof TCComponentBOMLine) {
                targetRevision = ((TCComponentBOMLine) comp).getItemRevision();
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
        partNoText.setEnabled(false);
        partRevisionText.setEnabled(false);
        dispPartNoText.setEnabled(false);
        partNameText.setEnabled(false);
        koreanNameText.setEnabled(false);
        maturityCB.setEnabled(false);
        unitCB.setEnabled(false);

        materialText.setEnabled(false);
        alterMaterialText.setEnabled(false);
        
        alcCodeText.setEnabled(false);
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

        attributeMap.put("item_id", partNoText.getText());
        attributeMap.put("item_revision_id", partRevisionText.getText());

        attributeMap.put("object_name", partNameText.getText());
        attributeMap.put("s7_KOR_NAME", koreanNameText.getText());
        attributeMap.put("s7_ACT_WEIGHT", actWeightText.getText());
        attributeMap.put("uom_tag", unitCB.getSelectedString());
        // Finish
        attributeMap.put("s7_FINISH", fisnishText.getText());
        // Material(TypedReference)
        attributeMap.put("s7_MATERIAL", materialText.getData());
        // Alter Material(TypedReference)
        attributeMap.put("s7_ALT_MATERIAL", alterMaterialText.getData());
        // Material Thickness

        attributeMap.put("s7_MATURITY", maturityCB.getSelectedString());
        attributeMap.put("s7_DESIGN_COST", designCostText.getText());
        attributeMap.put("s7_DESIGN_ESTIMATION", designEstText.getText());
        attributeMap.put("s7_REFERENCE", refNoText.getText());
        attributeMap.put("object_desc", descText.getText());

        // [SR140324-030][20140619] KOG DEV Std. Part Attribute Map�� SES Spec No. �߰�
        attributeMap.put("s7_SES_SPEC_NO", sesSpecNoText.getText().toString());
        // Part Orign(���� Setting)
        attributeMap.put("s7_PART_TYPE", "M");

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

            } catch (TCException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * SaveAs Ȥ�� ViewerTab(��ȸ) ��ɿ��� ���õ� Revision�� �� Setting
     * 
     * @param targetRevision
     * @throws TCException
     */
    private void setInitData(TCComponentItemRevision targetRevision) throws TCException {

        partNoText.setText(targetRevision.getProperty("item_id"));
        partRevisionText.setText(targetRevision.getProperty("item_revision_id"));
        dispPartNoText.setText(targetRevision.getProperty("s7_DISPLAY_PART_NO"));
        partNameText.setText(targetRevision.getProperty("object_name"));
        koreanNameText.setText(targetRevision.getProperty("s7_KOR_NAME"));
        actWeightText.setText(this.getFormatedString(targetRevision.getDoubleProperty("s7_ACT_WEIGHT"), "########.#####"));
        unitCB.setSelectedString(targetRevision.getItem().getProperty("uom_tag"));
        maturityCB.setSelectedString(targetRevision.getProperty("s7_MATURITY"));
        designCostText.setText(this.getFormatedString(targetRevision.getDoubleProperty("s7_DESIGN_COST"), "########.#####"));
        designEstText.setText(targetRevision.getProperty("s7_DESIGN_ESTIMATION"));
        refNoText.setText(targetRevision.getProperty("s7_REFERENCE"));
        descText.setText(targetRevision.getProperty("object_desc"));
        
        // [SR140729-026][20140827][jclee] Save As�� ��� ALC Code�� Blank ó��.
        if (paramMap != null) {
            String strSaveAs = (String) paramMap.get(Constants.COMMAND_SAVE_AS);
            if (strSaveAs != null && strSaveAs.equals("TRUE")) {
            	alcCodeText.setText("");
			} else {
				alcCodeText.setText(targetRevision.getProperty("m7_PG_ID"));
			}
        }

        // [SR140324-030][20140619] KOG DEV Std. Part ��ȸ�� SES Spec No. Property Text �� ������
        TCComponent sesSpecNoComp = targetRevision.getReferenceProperty("s7_Stdpart_TypedReference");
        if (sesSpecNoComp != null) {
            sesSpecNoText.setText(sesSpecNoComp.getStringProperty("s7_SES_SPEC_NO"));
        }

        // Finish
        fisnishText.setText(targetRevision.getProperty("s7_FINISH"));

        // Material(TypedReference)
        TCComponent matComp = targetRevision.getReferenceProperty("s7_MATERIAL");
        if (matComp != null) {
            materialText.setText(matComp.toDisplayString());
            materialText.setData(matComp);
        }

        // Alter Material(TypedReference)
        TCComponent altMatComp = targetRevision.getReferenceProperty("s7_ALT_MATERIAL");
        if (altMatComp != null) {
            alterMaterialText.setText(altMatComp.toDisplayString());
            alterMaterialText.setData(altMatComp);
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
            StdPartValidator validator = new StdPartValidator();
            String strMessage = validator.validate(this.attrMap, StdPartValidator.TYPE_VALID_MODIFY);

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
            // [SR140324-030][20140703] KOG SES Spec No. �߰�.
            TCComponent refComp = targetRevision.getReferenceProperty("s7_Stdpart_TypedReference");
            // �Ӽ� Object�� ���� ��� ����
            if (refComp != null) {
                refComp.setProperty("s7_SES_SPEC_NO", attrMap.get("s7_SES_SPEC_NO").toString());
                refComp.refresh();
            }
            // �Ӽ� Object�� �����ϴ� ��� ����
            else {
                refComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Stdpart_TypedReference", new String[] { "s7_SES_SPEC_NO" }, new String[] { attrMap.get("s7_SES_SPEC_NO").toString() });
                targetRevision.setReferenceProperty("s7_Stdpart_TypedReference", refComp);
            }
            // attrMap�� �����ϴ� ��� �Ӽ��� �ϰ� Update �ǹǷ� Revision �Ӽ��� �ƴѰ�� ����
            this.attrMap.remove("s7_SES_SPEC_NO");
            this.attrMap.remove("item_id");
            this.attrMap.remove("uom_tag");

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
        FormData textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(partNoLabel);

        partNoText = new SYMCText(composite, true, textFormData);
        textFormData = new FormData(20, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(partNoText, 4);
        partRevisionText = new SYMCText(composite, textFormData);
        partRevisionText.setEnabled(false);
        partRevisionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        partRevisionText.setText("000");

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(partRevisionText, 22);
        SYMCLabel dispPartNoLabel = new SYMCLabel(composite, "Display Part No", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(dispPartNoLabel, 5);
        dispPartNoText = new SYMCText(composite, textFormData);
        dispPartNoText.setEnabled(false);
        dispPartNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(dispPartNoText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel partNameLabel = new SYMCLabel(composite, "Part Name", labelFormData);
        textFormData = new FormData(573, 18);
        textFormData.top = new FormAttachment(dispPartNoText, 5);
        textFormData.left = new FormAttachment(partNameLabel, 5);
        partNameText = new SYMCText(composite, true, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partNameText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel koreanNameLabel = new SYMCLabel(composite, "Korean Name", labelFormData);
        textFormData = new FormData(573, 18);
        textFormData.top = new FormAttachment(partNameText, 5);
        textFormData.left = new FormAttachment(koreanNameLabel, 5);
        koreanNameText = new SYMCText(composite, true, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(koreanNameText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel unitLabel = new SYMCLabel(composite, "Unit", labelFormData);
        textFormData = new FormData(207, 18);
        textFormData.top = new FormAttachment(koreanNameText, 5);
        textFormData.left = new FormAttachment(unitLabel, 5);
        textFormData.height = 20;
        unitCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "Unit of Measures");
        unitCB.setLayoutData(textFormData);
        unitCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(koreanNameText, 5);
        labelFormData.left = new FormAttachment(unitCB, 55);
        SYMCLabel maturityLabel = new SYMCLabel(composite, "Maturity", labelFormData);
        textFormData = new FormData(207, 18);
        textFormData.top = new FormAttachment(koreanNameText, 5);
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
        SYMCLabel materialLabel = new SYMCLabel(composite, "Material", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(maturityCB, 5);
        textFormData.left = new FormAttachment(materialLabel, 5);
        materialText = new SYMCText(composite, textFormData);
        materialText.setEnabled(false);
        materialText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        FormData buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(maturityCB, 5);
        buttonFormData.left = new FormAttachment(materialText, 5);
        Button materialButton = new Button(composite, SWT.PUSH);
        materialButton.setImage(registry.getImage("Search.ICON"));
        materialButton.setLayoutData(buttonFormData);
        materialButton.addSelectionListener(new RevSelectionAdapter(materialText, "S7_MaterialRevision"));
        
        // [SR160328-033][20160330][jclee] Material Clear Button
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(maturityCB, 5);
        buttonFormData.left = new FormAttachment(materialButton, 2);
        Button materialClearButton = new Button(composite, SWT.PUSH);
        materialClearButton.setImage(registry.getImage("Clear.ICON"));
        materialClearButton.setLayoutData(buttonFormData);
        materialClearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				materialText.setText("");
				materialText.setData(null);
			}
		});

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(maturityCB, 5);
        labelFormData.left = new FormAttachment(materialButton, 29);
        SYMCLabel alterMaterialLabel = new SYMCLabel(composite, "Alter Material", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(maturityCB, 5);
        textFormData.left = new FormAttachment(alterMaterialLabel, 5);
        alterMaterialText = new SYMCText(composite, textFormData);
        alterMaterialText.setEnabled(false);
        alterMaterialText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(maturityCB, 5);
        buttonFormData.left = new FormAttachment(alterMaterialText, 5);
        Button altermaterialButton = new Button(composite, SWT.PUSH);
        altermaterialButton.setImage(registry.getImage("Search.ICON"));
        altermaterialButton.setLayoutData(buttonFormData);
        altermaterialButton.addSelectionListener(new RevSelectionAdapter(alterMaterialText, "S7_MaterialRevision"));
        
        // [SR160328-033][20160330][jclee] Alter Material Clear Button
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(maturityCB, 5);
        buttonFormData.left = new FormAttachment(altermaterialButton, 2);
        Button altermaterialClearButton = new Button(composite, SWT.PUSH);
        altermaterialClearButton.setImage(registry.getImage("Clear.ICON"));
        altermaterialClearButton.setLayoutData(buttonFormData);
        altermaterialClearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				alterMaterialText.setText("");
				alterMaterialText.setData(null);
			}
		});
        
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(alterMaterialText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel actWeightLabel = new SYMCLabel(composite, "Actual Weight", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(alterMaterialText, 5);
        textFormData.left = new FormAttachment(actWeightLabel, 5);
        actWeightText = new SYMCText(composite, true, textFormData);
        actWeightText.setInputType(SYMCText.DOUBLE);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(alterMaterialText, 5);
        labelFormData.left = new FormAttachment(actWeightText, 55);
        SYMCLabel finishLabel = new SYMCLabel(composite, "Finish", labelFormData);
        textFormData = new FormData(202, 16);
        textFormData.top = new FormAttachment(alterMaterialText, 5);
        textFormData.left = new FormAttachment(finishLabel, 5);
        fisnishText = new SYMCText(composite, textFormData);

        // LOVUIComponent uomLovComboBox = new LOVUIComponent(session, "Unit of Measures");

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(fisnishText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel designCostLabel = new SYMCLabel(composite, "Design Cost", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(fisnishText, 5);
        textFormData.left = new FormAttachment(designCostLabel, 5);
        designCostText = new SYMCText(composite, textFormData);
        designCostText.setInputType(SYMCText.DOUBLE);

        labelFormData = new FormData(120, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(fisnishText, 5);
        labelFormData.left = new FormAttachment(designCostText, 35);
        SYMCLabel designEstLabel = new SYMCLabel(composite, "Design Estimation", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(fisnishText, 5);
        textFormData.left = new FormAttachment(designEstLabel, 5);
        designEstText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(designEstText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel referenceLabel = new SYMCLabel(composite, "Reference No", labelFormData);
        textFormData = new FormData(573, 18);
        textFormData.top = new FormAttachment(designEstText, 5);
        textFormData.left = new FormAttachment(referenceLabel, 5);
        refNoText = new SYMCText(composite, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(refNoText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel descLabel = new SYMCLabel(composite, "Description", labelFormData);
        textFormData = new FormData(573, 18);
        textFormData.top = new FormAttachment(refNoText, 5);
        textFormData.left = new FormAttachment(descLabel, 5);
        descText = new SYMCText(composite, textFormData);

        // [SR140324-030][20140625] KOG DEV Std. Part UI Initialize �κп� SES Spec No. Label, Text �߰�
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(descText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel sesSpecNoLabel = new SYMCLabel(composite, "SES Spec No.", labelFormData);
        textFormData = new FormData(560, 36);
        textFormData.top = new FormAttachment(descText, 5);
        textFormData.left = new FormAttachment(sesSpecNoLabel, 5);
        sesSpecNoText = new SYMCText(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI, textFormData);
        sesSpecNoText.setData("SES_SPEC_NO_TEXT");
        
        // [SR140729-026][20140808] jclee ALC Code Label, Text �߰�
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(sesSpecNoText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel alcCodeLabel = new SYMCLabel(composite, "ALC Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(sesSpecNoText, 5);
        textFormData.left = new FormAttachment(alcCodeLabel, 5);
        alcCodeText = new SYMCText(composite, textFormData);
    }

    private void setControlData() {
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }

    /**
     * [SR140324-030][20140626] KOG DEV Std. Part Data ����
     */
    public void refreshData() {
        if (targetRevision != null) {
            try {
                this.setInitData(targetRevision);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    /**
     * Revision �˻��� ���Ǵ� Selection Adapter
     */
    class RevSelectionAdapter extends SelectionAdapter {
        /** �˻��� Item ID���� Setting�� Field */
        SYMCText targetText;
        /** �˻��� Revision Type */
        String strItemRevType;

        /**
         * Selection Adapter ������
         * 
         * @param targetText
         *            : �˻��� Item ID���� Setting�� Field
         * @param strItemRevType
         *            : �˻��� Revision Type
         */
        RevSelectionAdapter(SYMCText targetText, String strItemRevType) {
            this.targetText = targetText;
            this.strItemRevType = strItemRevType;
        }

        public void widgetSelected(SelectionEvent event) {

            // �˻� Dialog
            SearchItemRevDialog itemDialog = new SearchItemRevDialog(getShell(), SWT.SINGLE, strItemRevType);
            // ���õ� Revision
          //20230831 cf-4357 seho ��Ʈ �˻� dialog ��������..  �˻� ��� �κ� ����.
            TCComponent[] selectedItems = (TCComponent[]) itemDialog.open();

            if (selectedItems != null) {
                targetText.setText(selectedItems[0].toDisplayString());
                targetText.setData(selectedItems[0]);
            }
        }
    }

}
