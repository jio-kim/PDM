package com.ssangyong.common;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.common.utils.SYMDisplayUtil;

public class SYMText extends Text {

	/**
	 * 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 7.
	 * @param parent
	 * @param style
	 * @param mandatory
	 */
	public SYMText(Composite parent, int style) {
		this(parent, style, false);
	}
	
	/**
	 * Mandatory 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
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
