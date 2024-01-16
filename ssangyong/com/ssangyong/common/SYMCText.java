package com.ssangyong.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;

import com.ssangyong.common.utils.SYMDisplayUtil;

public class SYMCText extends StyledText {

	public static final int DEFAULT = 0;
	public static final int UPPER_CASE = 1;
	public static final int LOWER_CASE = 2;
	public static final int NUMERIC = 3;
	public static final int DOUBLE = 4;

	private int inputType = 0;
	private boolean mandatory = false;

	private VerifyKeyListener verifyKeyListener;
	private VerifyListener verifyListener;

	public SYMCText(Composite parent) {
		this(parent, SWT.NONE | SWT.BORDER | SWT.SINGLE);
	}

	public SYMCText(Composite parent, Object data) {
		this(parent, SWT.NONE | SWT.BORDER | SWT.SINGLE, data);
	}

	public SYMCText(Composite parent, int style) {
		super(parent, style);
		setTextInitUI();
	}

	public SYMCText(Composite parent, int style, Object data) {
		this(parent, style);
		setLayoutData(data);
	}

	public SYMCText(Composite parent, boolean mandatory) {
		this(parent, SWT.NONE | SWT.BORDER | SWT.SINGLE, mandatory);
	}

	public SYMCText(Composite parent, boolean mandatory, Object data) {
		this(parent, SWT.NONE | SWT.BORDER | SWT.SINGLE, mandatory, data);
	}

	public SYMCText(Composite parent, int style, boolean mandatory) {
		super(parent, style);
		this.mandatory = mandatory;
		setTextInitUI();
	}

	public SYMCText(Composite parent, int style, boolean mandatory, Object data) {
		this(parent, style, mandatory);
		setLayoutData(data);
	}

	

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 9.
	 * @param input
	 */
	public void setInputType(int input) {
		this.inputType = input;
		switch (inputType) {
		case DEFAULT:
			setVerifyListener();
			break;
		case UPPER_CASE:
			setVerifyListener();
			break;
		case LOWER_CASE:
			setVerifyListener();
			break;
		case NUMERIC:
			setVerifyKeyListener();
			break;
		case DOUBLE:
			setVerifyKeyListener();
			break;
		}
	}

	/**
	 * removeVerifyKeyListener > addVerifyListener
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 9.
	 */
	private void setVerifyListener() {
		if (verifyKeyListener != null) {
			removeVerifyKeyListener(verifyKeyListener);
			verifyKeyListener = null;
		}
		if (verifyListener == null) {
			verifyListener = new SYMCVerifyListener();
			addVerifyListener(verifyListener);
		}
	}

	/**
	 * removeVerifyListener > addVerifyKeyListener
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 9.
	 */
	private void setVerifyKeyListener() {
		if (verifyListener != null) {
			removeVerifyListener(verifyListener);
			verifyListener = null;
		}
		if (verifyKeyListener == null) {
			verifyKeyListener = new SYMCSYMCVerifyKeyListener();
			addVerifyKeyListener(verifyKeyListener);
		}
	}

	public int getInputType() {
		return inputType;
	}

	/**
	 * Default, Upper Case, Lower Case 처리
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 9. Package ID : com.ssangyong.common.SYMCText.java
	 */
	private class SYMCVerifyListener implements VerifyListener {
		@Override
		public void verifyText(VerifyEvent event) {
		    if (event.text != null && event.text.length() > 0)
		    {
    			switch (getInputType()) {
    			case SYMCText.DEFAULT:
    				break;
    			case SYMCText.UPPER_CASE:
    				if (Character.isLowerCase(event.text.charAt(0))) {
    					event.text = event.text.toUpperCase();
    				}
    				return;
    			case SYMCText.LOWER_CASE:
    				if (Character.isUpperCase(event.text.charAt(0))) {
    					event.text = event.text.toLowerCase();
    				}
    			}
		    }
		}

	}

	/**
	 * 숫자만 입력하기 위한 리스너
	 * VerifyListener 에서 백스페이스 바 처리가 안되어 VerifyKeyListener를 따로 생성하여 처리하였음
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 9. Package ID : com.ssangyong.common.SYMCText.java
	 */
	private class SYMCSYMCVerifyKeyListener implements VerifyKeyListener {

		@Override
		public void verifyKey(VerifyEvent event) {
			switch (getInputType()) {
			case SYMCText.NUMERIC:
				switch (event.keyCode) {
				case SWT.BS: // Backspace
				case SWT.DEL: // Delete
				case SWT.HOME: // Home
				case SWT.END: // End
				case SWT.ARROW_LEFT: // Left arrow
				case SWT.ARROW_RIGHT: // Right arrow
					return;
				}
				if (!Character.isDigit(event.character)) { // NUMERIC
					event.doit = false; // disallow the action
				}
				return;

			case SYMCText.DOUBLE:
				switch (event.keyCode) {
				case SWT.BS: // Backspace
				case SWT.DEL: // Delete
				case SWT.HOME: // Home
				case SWT.END: // End
				case SWT.ARROW_LEFT: // Left arrow
				case SWT.ARROW_RIGHT: // Right arrow
					return;
				}
				
				if (!getText().contains(".")) {
					if (event.keyCode == 46 || event.keyCode == 16777262) { // "."
						return;
					}
				}
				if (!Character.isDigit(event.character)) { // NUMERIC
					event.doit = false; // disallow the action
				}
			}
		}
	}
	
	public void setMandatory (boolean mandatory) {
		this.mandatory = mandatory;
		setTextInitUI();
	}
	
	private void setTextInitUI() {
		if(mandatory){
			SYMDisplayUtil.setRequiredFieldSymbol(this);
			redraw();
		}
	}
}
