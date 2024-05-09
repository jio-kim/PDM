package com.symc.plm.rac.prebom.prebom.view.preproject;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.eclipse.swt.SWT;
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
import com.symc.plm.rac.prebom.prebom.validator.preproject.PreProjectValidator;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;

/**
 * Project Part Information Panel
 * [20170116] Project Code �ڿ� 'PRE' subfix �� �ڵ��� �ٵ��� ó����
 */
public class PreProjectInfoPanel extends Composite implements IPageComplete
{

	/** TC Session */
	private TCSession session;

	/** Part No. Field */
	protected SYMCText partNoText;
	/** Part Rev. Field */
	protected SYMCText partRevisionText;
	/** Part Name Field */
	protected SYMCText partNameText;
	/** Description Field */
	protected SYMCText descText;
	/** Car Code Field */
	protected SYMCText partCarCode;
	/**
	 * ������Ʈ No Sub Fix
	 */
	protected SYMCText partNoSubFixText;

	/** Base Proj Combo */
	protected SYMCLOVComboBox baseProjCB;
	/** Is New Combo */
	protected SYMCLOVComboBox isNewCB;
	/** Vehicle Project Combo */
	protected SYMCLOVComboBox isVehProjCB;
	/** Vehicle No. Combo */
	protected SYMCLOVComboBox vehNoCB;
	/** Maturity Combo */
	protected SYMCLOVComboBox maturityCB;

	/** ��ȸ�� Target Revision */
	TCComponentItemRevision targetRevision;
	/** ȭ�鿡 �Էµ� �Ӽ� Map */
	public HashMap<String, Object> attrMap;
	/** ���� Loading�� �Ӽ� Map(�����׸� Check�� ���� ���, attrMap�� oldAttrMap�� ��) */
	HashMap<String, Object> oldAttrMap;
	/** PartManage Dialog���� �Ѿ�� Param Map */
	HashMap<String, Object> paramMap;
	/** SaveAs�� Target Revison */
	TCComponentItemRevision baseItemRev;



	/**
	 * Create Project Menu�� ���� ȣ���
	 * 
	 * @param parent
	 * @param paramMap : PartManage Dialog���� �Ѿ�� Param Map
	 * @param style : Dialog SWT Style
	 */
	public PreProjectInfoPanel(Composite parent, HashMap<String, Object> paramMap, int style)
	{
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
	public PreProjectInfoPanel(Composite parent, int style, boolean isViewMode)
	{
		super(parent, style);
		session = CustomUtil.getTCSession();
		attrMap = new HashMap<String, Object>();
		oldAttrMap = new HashMap<String, Object>();
		initUI();
		setControlData();
		setViewMode();
		try
		{
			InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
			if (comp != null && comp instanceof TCComponentItemRevision)
			{
				targetRevision = (TCComponentItemRevision) comp;
				this.setInitData(targetRevision);
				this.getPropDataMap(this.oldAttrMap);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * ��ȸȭ���� ��� �����Ұ� �׸� Setting
	 */
	public void setViewMode()
	{
		partNoText.setEnabled(false);
		partRevisionText.setEnabled(false);
		partNoSubFixText.setEnabled(false);
//		partNameText.setEnabled(false);

		partNoText.setBackground(partRevisionText.getBackground());
		partNameText.setBackground(partRevisionText.getBackground());
		partNoSubFixText.setBackground(partRevisionText.getBackground());
	}

	/**
	 * ȭ�鿡 �Էµ� �Ӽ� ���� ����
	 * 
	 * @param attributeMap : �Ӽ����� ����� HashMap
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Object> getPropDataMap(HashMap<String, Object> attributeMap) throws Exception
	{
	    attributeMap.put(PropertyConstant.ATTR_NAME_ITEMTYPE, TypeConstant.S7_PREPROJECTTYPE);

	    attributeMap.put(PropertyConstant.ATTR_NAME_ITEMID, partNoText.getText() +partNoSubFixText.getText());
		attributeMap.put(PropertyConstant.ATTR_NAME_ITEMREVID, partRevisionText.getText());

		attributeMap.put(PropertyConstant.ATTR_NAME_ITEMNAME, partNameText.getText());

		attributeMap.put("s7_IS_NEW", isNewCB.getSelectedString());
		attributeMap.put("s7_IS_VEHICLE_PRJ", isVehProjCB.getSelectedString());
		attributeMap.put("s7_BASE_PRJ", baseProjCB.getSelectedString());
		attributeMap.put("s7_VEHICLE_NO", vehNoCB.getSelectedString());
		attributeMap.put(PropertyConstant.ATTR_NAME_ITEMDESC, descText.getText());

		attributeMap.put(PropertyConstant.ATTR_NAME_MATURITY, maturityCB.getSelectedString());
		attributeMap.put("s7_CAR_CODE", partCarCode.getText());
		return attributeMap;
	}

	/**
	 * Create Part ����� ���� ȣ��� ��� �Ӽ� �� Setting
	 * 
	 * @param paramMap : Part Manage Dialog���� �Ѿ�� Parameter Map
	 */
	private void setInitData(HashMap<String, Object> paramMap)
	{
		partNoText.setText("");
		partRevisionText.setText("000");
		partNameText.setText("");
		partNoSubFixText.setText("PRE");
	}


	/**
	 * ��ȸ�� Viewer Tab���� ȣ��
	 * Revision �Ӽ��� ȭ�鿡 ǥ��
	 * 
	 * @param targetRevision : Target Revision
	 * @throws TCException
	 */
	private void setInitData(TCComponentItemRevision targetRevision) throws TCException
	{

		partNoText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID).replace("PRE", ""));
		partRevisionText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID));
		partNameText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME));
		isNewCB.setSelectedString(targetRevision.getProperty("s7_IS_NEW"));
		isVehProjCB.setSelectedString(targetRevision.getProperty("s7_IS_VEHICLE_PRJ"));
		baseProjCB.setSelectedString(targetRevision.getProperty("s7_BASE_PRJ"));
		vehNoCB.setSelectedString(targetRevision.getProperty("s7_VEHICLE_NO"));
		descText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC));

		maturityCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_MATURITY));
		partCarCode.setText(targetRevision.getProperty("s7_CAR_CODE"));
		partNoSubFixText.setText("PRE");
	}

	public String getFormatedString(double value, String format)
	{

		DecimalFormat df = new DecimalFormat(format);//
		return df.format(value);
	}

	/**
	 * CheckIn�� Validation Check
	 * 
	 * @return
	 */
	public boolean isSavable()
	{
		try
		{
			this.getPropDataMap(this.attrMap);
			PreProjectValidator validator = new PreProjectValidator();
			String strMessage = validator.validate(this.attrMap, PreProjectValidator.TYPE_VALID_MODIFY);

			if (!CustomUtil.isEmpty(strMessage))
			{
				MessageBox.post(getShell(), strMessage, "Warning", MessageBox.WARNING);
				return false;
			}
			else
				return true;

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * CheckIn Action
	 * 
	 */
	public void saveAction()
	{
		try
		{

			this.attrMap.remove(PropertyConstant.ATTR_NAME_ITEMID);
            this.attrMap.remove(PropertyConstant.ATTR_NAME_ITEMTYPE);
            this.attrMap.remove(PropertyConstant.ATTR_NAME_ITEMREVID);

			String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
			TCProperty[] props = targetRevision.getTCProperties(szKey);

			for (int i = 0; i < props.length; i++)
			{

				if (props[i] == null)
				{
					System.out.println(szKey[i] + " is Null");
					continue;
				}

				Object value = attrMap.get(szKey[i]);
				CustomUtil.setObjectToPropertyValue(props[i], value);

			}

			targetRevision.setTCProperties(props);
			targetRevision.refresh();

			// targetRevision.save();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * ȭ�� �ʱ�ȭ
	 */
	private void initUI()
	{
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

		SYMCLabel partNoLabel = new SYMCLabel(composite, "Project No ", labelFormData);
		FormData textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(0);
		textFormData.left = new FormAttachment(partNoLabel);

		partNoText = new SYMCText(composite, true, textFormData);
		
		textFormData = new FormData(20, 18);
		textFormData.top = new FormAttachment(0);
		textFormData.left = new FormAttachment(partNoText);
		
		partNoSubFixText = new SYMCText(composite, true, textFormData);
		partNoSubFixText.setEnabled(false);
		partNoSubFixText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		
		textFormData = new FormData(20, 18);
		textFormData.top = new FormAttachment(0);
		textFormData.left = new FormAttachment(partNoSubFixText, 4);
		partRevisionText = new SYMCText(composite, textFormData);
		partRevisionText.setEnabled(false);
		partRevisionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		partRevisionText.setText("000");

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(0);
		labelFormData.left = new FormAttachment(partRevisionText, 22);
		SYMCLabel maturityLabel = new SYMCLabel(composite, "Maturity", labelFormData);
		textFormData = new FormData(207, 18);
		textFormData.top = new FormAttachment(0);
		textFormData.left = new FormAttachment(maturityLabel, 5);
		textFormData.height = 20;
		maturityCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_MATURITY");
		maturityCB.setLayoutData(textFormData);
		maturityCB.setText("In Work");
		maturityCB.setMandatory(true);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(partRevisionText, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel partNameLabel = new SYMCLabel(composite, "Project Name", labelFormData);
		textFormData = new FormData(573, 18);
		textFormData.top = new FormAttachment(partRevisionText, 5);
		textFormData.left = new FormAttachment(partNameLabel, 5);
		partNameText = new SYMCText(composite, true, textFormData);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(partNameText, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel newLabel = new SYMCLabel(composite, "Is New", labelFormData);
		textFormData = new FormData(207, 18);
		textFormData.top = new FormAttachment(partNameText, 5);
		textFormData.left = new FormAttachment(newLabel, 5);
		textFormData.height = 20;

		isNewCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "s7_YN");
		isNewCB.setLayoutData(textFormData);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(partNameText, 5);
		labelFormData.left = new FormAttachment(isNewCB, 55);
		SYMCLabel baseProjectLabel = new SYMCLabel(composite, "Base Project", labelFormData);
		textFormData = new FormData(207, 18);
		textFormData.top = new FormAttachment(partNameText, 5);
		textFormData.left = new FormAttachment(baseProjectLabel, 5);
		textFormData.height = 20;

		baseProjCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_PROJECT_CODE");
		baseProjCB.setLayoutData(textFormData);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(baseProjCB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel isVehicleLabel = new SYMCLabel(composite, "Is Vehicle Proj.", labelFormData);
		textFormData = new FormData(207, 18);
		textFormData.top = new FormAttachment(baseProjCB, 5);
		textFormData.left = new FormAttachment(isVehicleLabel, 5);
		textFormData.height = 20;

		isVehProjCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "s7_YN");
		isVehProjCB.setLayoutData(textFormData);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(baseProjCB, 5);
		labelFormData.left = new FormAttachment(isNewCB, 55);
		SYMCLabel vehNoLabel = new SYMCLabel(composite, "Vehicle No.", labelFormData);
		textFormData = new FormData(207, 18);
		textFormData.top = new FormAttachment(baseProjCB, 5);
		textFormData.left = new FormAttachment(vehNoLabel, 5);
		textFormData.height = 20;

		vehNoCB = new SYMCLOVComboBox(composite, SWT.BORDER, session, "S7_VEHICLE_NO");
		vehNoCB.setLayoutData(textFormData);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(vehNoCB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel carCodeLabel = new SYMCLabel(composite, "Car Code", labelFormData);
		textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(vehNoCB, 5);
		textFormData.left = new FormAttachment(carCodeLabel, 5);

		partCarCode = new SYMCText(composite, false, textFormData);
		partCarCode.setTextLimit(5);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(partCarCode, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel descLabel = new SYMCLabel(composite, "Description", labelFormData);
		textFormData = new FormData(573, 18);
		textFormData.top = new FormAttachment(partCarCode, 5);
		textFormData.left = new FormAttachment(descLabel, 5);
		descText = new SYMCText(composite, textFormData);

	}

	private void setControlData()
	{
		//actWeightText.setInputType(SYMCText.DOUBLE);

	}

	@Override
	public boolean isPageComplete()
	{
		return true;
	}

	/**
	 * TC ViewerTab���� ȭ�� �̵��� ������ �׸��� �����ϴ��� Check
	 * 
	 * ���� Loading�� �Ӽ����� ���� �Ӽ����� ��
	 */
	public boolean isModified()
	{
		if (this.targetRevision == null || !this.targetRevision.isCheckedOut())
			return false;
		try
		{
			this.getPropDataMap(this.attrMap);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Object newValue = "";
		Object oldValue = null;
		for (Object key : this.oldAttrMap.keySet().toArray())
		{
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
