package com.kgm.common;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.kgm.common.utils.SYMDisplayUtil;

public class SYMText extends Text {

	/**
	 * ������.
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2013. 1. 7.
	 * @param parent
	 * @param style
	 * @param mandatory
	 */
	public SYMText(Composite parent, int style) {
		this(parent, style, false);
	}
	
	/**
	 * Mandatory ������.
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2013. 1. 8.
	 * @param parent
	 * @param style
	 * @param mandatory
	 */
	public SYMText (Composite parent, int style, boolean mandatory) {
		super(parent, style);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		setLayoutData(data);
		
		if(mandatory){
			SYMDisplayUtil.setRequiredFieldSymbol(this, "DEC_REQUIRED", false);
		}
	}
	
	

}
