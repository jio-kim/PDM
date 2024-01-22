/**
 * 
 */
package com.symc.plm.me.common;

import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;

import com.ssangyong.common.utils.SYMDisplayUtil;

/**
 * Class Name : SDVText
 * Class Description : 
 * @date 2013. 11. 8.
 *
 */
public class SDVText extends StyledText{

    public static final int DEFAULT = 0;
    public static final int UPPER_CASE = 1;
    public static final int LOWER_CASE = 2;
    public static final int NUMERIC = 3;
    public static final int DOUBLE = 4;
    public static final int ENGUPPERNUM = 5;
    public static final int ENGUPPER = 6;

    private int inputType = 0;
    private boolean mandatory = false;

    private VerifyKeyListener verifyKeyListener;
    private VerifyListener verifyListener;

    public SDVText(Composite parent) {
        this(parent, SWT.NONE | SWT.BORDER | SWT.SINGLE);
    }

    public SDVText(Composite parent, Object data) {
        this(parent, SWT.NONE | SWT.BORDER | SWT.SINGLE, data);
    }

    public SDVText(Composite parent, int style) {
        super(parent, style);
        setTextInitUI();
    }

    public SDVText(Composite parent, int style, Object data) {
        this(parent, style);
        setLayoutData(data);
    }

    public SDVText(Composite parent, boolean mandatory) {
        this(parent, SWT.NONE | SWT.BORDER | SWT.SINGLE, mandatory);
    }

    public SDVText(Composite parent, boolean mandatory, Object data) {
        this(parent, SWT.NONE | SWT.BORDER | SWT.SINGLE, mandatory, data);
    }

    public SDVText(Composite parent, int style, boolean mandatory) {
        super(parent, style);
        this.mandatory = mandatory;
        setTextInitUI();
    }

    public SDVText(Composite parent, int style, boolean mandatory, Object data) {
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
        case ENGUPPERNUM:
            setVerifyListener();
            break;
        case ENGUPPER:
            setVerifyListener();
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
            verifyListener = new SDVVerifyListener();
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
            verifyKeyListener = new SDVVerifyKeyListener();
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
     * @since : 2013. 1. 9. Package ID : com.ssangyong.common.SDVText.java
     */
    private class SDVVerifyListener implements VerifyListener {
        @Override
        public void verifyText(VerifyEvent event) {
            if (event.text.length() < 1)
                return;

            switch (getInputType()) {
            case SDVText.DEFAULT:
            	break;
            case SDVText.UPPER_CASE:
                if (Character.isLowerCase(event.text.charAt(0))) {
                    event.text = event.text.toUpperCase();
                }
                break;
            case SDVText.LOWER_CASE:
                if (Character.isUpperCase(event.text.charAt(0))) {
                    event.text = event.text.toLowerCase();
                }
                break;
            case SDVText.ENGUPPERNUM:
                if (Character.isLowerCase(event.text.charAt(0))) {
                    event.text = event.text.toUpperCase();
                }

                if (! Pattern.matches("^[a-zA-Z0-9]*$", event.text))
                	event.doit = false;

                break;
            case SDVText.ENGUPPER:
                if (Character.isLowerCase(event.text.charAt(0))) {
                    event.text = event.text.toUpperCase();
                }

                if (! Pattern.matches("^[a-zA-Z]*$", event.text))
                	event.doit = false;

                break;
            }
        }

    }

    /**
     * 숫자만 입력하기 위한 리스너
     * VerifyListener 에서 백스페이스 바 처리가 안되어 VerifyKeyListener를 따로 생성하여 처리하였음
     * @Copyright : S-PALM
     * @author : 권오규
     * @since : 2013. 1. 9. Package ID : com.ssangyong.common.SDVText.java
     */
    private class SDVVerifyKeyListener implements VerifyKeyListener {

        @Override
        public void verifyKey(VerifyEvent event) {
            switch (event.keyCode) {
            case SWT.BS: // Backspace
            case SWT.DEL: // Delete
            case SWT.HOME: // Home
            case SWT.END: // End
            case SWT.ARROW_LEFT: // Left arrow
            case SWT.ARROW_RIGHT: // Right arrow
                return;
            }
            
            switch (getInputType()) {
            case SDVText.NUMERIC:
                if (!Character.isDigit(event.character)) { // NUMERIC
                    event.doit = false; // disallow the action
                }
                return;

            case SDVText.DOUBLE:
                if (!getText().contains(".")) {
                    if (event.keyCode == 46 || event.keyCode == 16777262) { // "."
                        return;
                    }
                }
                if (!Character.isDigit(event.character)) { // NUMERIC
                    event.doit = false; // disallow the action
                }
                return;
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
    
    /**
     * Tab Next 이동
     */
    public void setEnableNextTab()
    {
        this.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                if(e.detail == SWT.TRAVERSE_TAB_NEXT){
                    e.doit = true;
                }
            }
        });

    }

}
