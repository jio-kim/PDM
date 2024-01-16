package com.ssangyong.common;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.ssangyong.common.lov.SYMCLOVLoader;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.common.controls.LOVComboBox;
import com.teamcenter.rac.kernel.TCSession;

@Deprecated
public class SYMCLOVComboBox extends LOVComboBox
{

	private ControlDecoration decoration;
	
	public SYMCLOVComboBox(Composite paramComposite) {
		super(paramComposite, SWT.BORDER);
	}
	
	public SYMCLOVComboBox(Composite paramComposite, String paramString)
	{
		this(paramComposite, SWT.BORDER, CustomUtil.getTCSession(), paramString);
		
	}
	
	public SYMCLOVComboBox(Composite paramComposite, int paramInt, TCSession paramTCSession, String paramString)
	{
//		super(paramComposite, paramInt, paramTCSession, paramString);
//		super(paramComposite, paramInt, paramString);
		super(paramComposite, paramInt);
		super.setLOVComponent(SYMCLOVLoader.getLOV(paramString));
	}
	
	public void setMandatory(boolean mandatory) {
		
		super.setMandatory(mandatory);
		
		if(mandatory && decoration == null) {
			decoration = SYMDisplayUtil.setRequiredFieldSymbol(this);
		}
		if(decoration == null) {
			return;
		}
		if(mandatory) {
			decoration.show();
		} else {
			decoration.hide();
		}
		
		redraw();
	}

	public String getText() {
		return super.getSelectedString();
	}
	
	public void setText(String value){
		super.setSelectedString(value);
	}
	
	public void add(String string) {
		super.addItem(string);
	}
	
	public void add(String value, String desc) {
		super.addItem(value, desc);
		
	}


}
