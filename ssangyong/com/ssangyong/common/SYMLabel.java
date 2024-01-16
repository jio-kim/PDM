package com.ssangyong.common;

import org.eclipse.swt.custom.CLabel;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

// 
//import com.swtdesigner.SWTResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

public class SYMLabel extends CLabel {

	public SYMLabel(Composite parent, int style) {
		super(parent, style);
		
		setBackground(SWTResourceManager.getColor(204, 204, 255));
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

}
