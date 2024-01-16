package com.ssangyong.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SYMCYesNoRadio extends Composite {

	private Button yes, no;
	
	public SYMCYesNoRadio(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(2,false));
		create();
	}
	
	private void create(){
		yes = new Button (this, SWT.RADIO);
		yes.setText("Yes");
		no = new Button (this, SWT.RADIO);
		no.setText("No");
	}

	public String getText() {
		if(yes.getSelection())
			return "Y";
		if(no.getSelection())
			return "N";
		return "";
	}
	
	public void setText(String yesOrNo) {
		if(yesOrNo != null){
			if(yesOrNo.equals("Y")){
				yes.setSelection(true);
			}
			if(yesOrNo.equals("N")){
				no.setSelection(true);
			}
		}
	}


}
