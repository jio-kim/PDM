package com.kgm.commands.document;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.eclipse.nebula.widgets.datechooser.DateChooserCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.kgm.commands.ec.search.FileAttachmentComposite;
import com.kgm.commands.partmaster.validator.TechDocValidator;
import com.kgm.common.SYMCLabel;
import com.kgm.common.SYMCText;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;

public class TechDocInfoPanel extends Composite implements IPageComplete
{

	protected SYMCText partNoText;
	protected SYMCText partRevisionText;
	protected SYMCText nameText;
	protected SYMCText descText;

	protected SYMCText issueUserText;
	protected SYMCText issueDeptText;

	protected SYMCText reviseUserText;
	protected SYMCText reviseDeptText;
	
	
//	protected SYMCLOVCombo engDocTypeCB;
//	protected SYMCLOVCombo maturityCB;
//	protected SYMCLOVCombo sesClassificationCB;
	
	private DateChooserCombo issueDateBtn;
	private DateChooserCombo reviseDateBtn;

	TCComponentItem targetItem;
	HashMap<String, Object> attrMap;
	HashMap<String, Object> oldAttrMap;
	FileAttachmentComposite fileComposite;

	public TechDocInfoPanel(Composite parent, int style)
	{
		super(parent, style);
		this.attrMap = new HashMap<String, Object>();
		initUI();
		setControlData();
	}

	public TechDocInfoPanel(Composite parent, int style, boolean isViewMode)
	{
		super(parent, style);
		attrMap = new HashMap<String, Object>();
		oldAttrMap = new HashMap<String, Object>();
		initUI();
		setControlData();
		setViewMode();
		try
		{
			InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
			if (comp != null && comp instanceof TCComponentItem)
			{
				targetItem = (TCComponentItem) comp;
				this.setInitData(targetItem);
				this.getPropDataMap(this.oldAttrMap);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setViewMode()
	{
		partNoText.setEnabled(false);
		partRevisionText.setEnabled(false);
	}

	public HashMap<String, Object> getPropDataMap(HashMap<String, Object> attributeMap) throws Exception
	{
		// Part No.
		attributeMap.put("item_id", partNoText.getText());
		attributeMap.put("object_name", nameText.getText());
		attributeMap.put("s7_REVISION", partRevisionText.getText());
		
		
		attributeMap.put("s7_ISSUED_DATE", issueDateBtn.getValue());
		attributeMap.put("s7_ISSUED_USER", issueUserText.getText());
		attributeMap.put("s7_ISSUED_DEPT", issueDeptText.getText());

		attributeMap.put("s7_REVISED_DATE", reviseDateBtn.getValue());
		attributeMap.put("s7_REVISED_USER", reviseUserText.getText());
		attributeMap.put("s7_REVISED_DEPT", reviseDeptText.getText());

		
		attributeMap.put("object_desc", descText.getText());
		return attributeMap;
	}

	private void setInitData(TCComponentItem targetItem) throws TCException
	{
		partNoText.setText(targetItem.getProperty("item_id"));
		partRevisionText.setText(targetItem.getProperty("s7_REVISION"));
		nameText.setText(targetItem.getProperty("object_name"));
		
		issueDateBtn.setValue(targetItem.getDateProperty("s7_ISSUED_DATE"));
		issueUserText.setText(targetItem.getProperty("s7_ISSUED_USER"));
		issueDeptText.setText(targetItem.getProperty("s7_ISSUED_DEPT"));
		
		reviseDateBtn.setValue(targetItem.getDateProperty("s7_REVISED_DATE"));
		reviseUserText.setText(targetItem.getProperty("s7_REVISED_USER"));
		reviseDeptText.setText(targetItem.getProperty("s7_REVISED_DEPT"));
		
		descText.setText(targetItem.getProperty("object_desc"));
		
		if (this.targetItem != null)
		{
			// 데이터 셋
			try
			{
				fileComposite.roadDataSet(targetItem);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				MessageBox.post(getShell(), e.toString(), "ERROR in setInitData()", MessageBox.ERROR);
			}
		}
	}

	public String getFormatedString(double value, String format)
	{
		DecimalFormat df = new DecimalFormat(format);//
		return df.format(value);
	}

	public boolean isSavable()
	{
		try
		{
			// 입력된 속성 값을 가져옴
			this.getPropDataMap(this.attrMap);
			// Validation Check
			TechDocValidator validator = new TechDocValidator();
			String strMessage = validator.validate(this.attrMap, TechDocValidator.TYPE_VALID_MODIFY);
			// Error 발생시 메시지 출력
			if (!CustomUtil.isEmpty(strMessage))
			{
				MessageBox.post(getShell(), strMessage, "Warning", MessageBox.WARNING);
				return false;
			}
		}
		catch (Exception e)
		{
			MessageBox.post(getShell(), e.getMessage(), "Warning", MessageBox.WARNING);
			return false;
		}

		return true;
	}

	public void saveAction()
	{
		try
		{
			this.attrMap.remove("item_id");
			String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
			TCProperty[] props = targetItem.getTCProperties(szKey);
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
			targetItem.setTCProperties(props);
			if (fileComposite.isFileModified())
			{
				fileComposite.createDatasetAndMakerelation(targetItem);
			}
			targetItem.refresh();
			// targetRevision.save();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void initUI()
	{
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
		composite.setText("Tech Doc Info");

		// /////////////////////////////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////Basic Info
		// Start//////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////////////

		FormData labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(0);
		labelFormData.left = new FormAttachment(0, 5);
		SYMCLabel partNoLabel = new SYMCLabel(composite, "Doc Code " , labelFormData);
		FormData textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(0);
		textFormData.left = new FormAttachment(partNoLabel);
		partNoText = new SYMCText(composite, true, textFormData);
		//partNoText.setEnabled(false);
		//partNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		
		labelFormData = new FormData(147, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(0);
		labelFormData.left = new FormAttachment(partNoText, 5);
		SYMCLabel revisionLabel = new SYMCLabel(composite, "Revision", labelFormData);
		
		textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(0);
		textFormData.left = new FormAttachment(revisionLabel, 4);
		partRevisionText = new SYMCText(composite,true,  textFormData);
		//partRevisionText.setEnabled(false);
		//partRevisionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		partRevisionText.setText("000");
		
		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(partRevisionText, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel specNoLabel = new SYMCLabel(composite, "Doc Name", labelFormData);
		textFormData = new FormData(569, 18);
		textFormData.top = new FormAttachment(partRevisionText, 5);
		textFormData.left = new FormAttachment(specNoLabel, 5);
		nameText = new SYMCText(composite, true, textFormData);
		
		
		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(nameText, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel issueDateLabel = new SYMCLabel(composite, "Issue Date", labelFormData);
		
		textFormData = new FormData(212, 25);
		textFormData.top = new FormAttachment(nameText, 5);
		textFormData.left = new FormAttachment(issueDateLabel, 5);
		issueDateBtn = new DateChooserCombo(composite , SWT.BORDER);
		issueDateBtn.setLayoutData(textFormData);
		//issueDateBtn.setLocale(Locale.KOREAN);
		//issueDateBtn.setFormatter(new DateFormatter(Locale.KOREAN));
		
		labelFormData = new FormData(144, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(nameText, 5);
		labelFormData.left = new FormAttachment(issueDateBtn, 5);
		SYMCLabel reviseDateLabel = new SYMCLabel(composite, "Revise Date", labelFormData);
		
		textFormData = new FormData(212, 25);
		textFormData.top = new FormAttachment(nameText, 5);
		textFormData.left = new FormAttachment(reviseDateLabel, 5);
		reviseDateBtn = new DateChooserCombo(composite, SWT.BORDER);
		
		reviseDateBtn.setLayoutData(textFormData);
		
		
		
		
		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(reviseDateBtn, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel issueUserLabel = new SYMCLabel(composite, "Issue User", labelFormData);
		
		textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(reviseDateBtn, 5);
		textFormData.left = new FormAttachment(issueUserLabel, 5);
		issueUserText = new SYMCText(composite, false, textFormData);
		
		labelFormData = new FormData(145, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(reviseDateBtn, 5);
		labelFormData.left = new FormAttachment(issueDateBtn, 5);
		SYMCLabel reviseUserLabel = new SYMCLabel(composite, "Revise User", labelFormData);
		
		textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(reviseDateBtn, 5);
		textFormData.left = new FormAttachment(reviseUserLabel, 5);
		reviseUserText = new SYMCText(composite, false, textFormData);
		
		
		
		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(reviseUserText, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel issueDeptLabel = new SYMCLabel(composite, "Issue Dept", labelFormData);
		
		textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(reviseUserText, 5);
		textFormData.left = new FormAttachment(issueDeptLabel, 5);
		issueDeptText = new SYMCText(composite, false, textFormData);
		
		labelFormData = new FormData(145, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(reviseUserText, 5);
		labelFormData.left = new FormAttachment(issueDeptText, 5);
		SYMCLabel reviseDeptLabel = new SYMCLabel(composite, "Revise Dept", labelFormData);
		
		textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(reviseUserText, 5);
		textFormData.left = new FormAttachment(reviseDeptLabel, 5);
		reviseDeptText = new SYMCText(composite, false, textFormData);	
		
		
		
		
		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(reviseDeptText, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel descLabel = new SYMCLabel(composite, "Description", labelFormData);
		textFormData = new FormData(573, 50);
		textFormData.top = new FormAttachment(reviseDeptText, 5);
		textFormData.left = new FormAttachment(descLabel, 5);
		descText = new SYMCText(composite, SWT.MULTI | SWT.BORDER ,  textFormData);
		
		
		GridData layoutData = new GridData(SWT.NONE, SWT.NONE, true, false);
		layoutData.minimumHeight = 250;
		fileComposite = new FileAttachmentComposite(this, layoutData);
		fileComposite.group.setSize(new Point(680, 200));
		this.pack();

		//
		// if(this.ecoRevision != null && !ecoRevision.isCheckedOut()){
		// fileComposite.resizeTable();
		// }
		//

	}

	private void setControlData()
	{
		// actWeightText.setInputType(SYMCText.DOUBLE);

	}

	@Override
	public boolean isPageComplete()
	{
		return true;
	}

	public boolean isModified()
	{
		if (this.targetItem == null || !this.targetItem.isCheckedOut())
		{
			return false;
		}
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
			{
				oldValue = "";
			}
			newValue = this.attrMap.get(key);
			if (newValue == null)
			{
				newValue = "";
			}
			if (!oldValue.equals(newValue))
			{
				return true;
			}
		}
		if (fileComposite.isFileModified())
		{
			return true;
		}
		return false;
	}

}
