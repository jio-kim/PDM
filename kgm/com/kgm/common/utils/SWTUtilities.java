package com.kgm.common.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class SWTUtilities {
	
	/**
	 * Composite의 'ESC' 키 이벤트를 무시하도록 한다.
	 * @param composite
	 */
	public static void skipESCKeyEvent(Composite composite) {
		composite.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {            
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					e.doit = false;
				}
			}
		});
	}
	
	public static void skipKeyEvent(Composite composite) {
	    composite.addListener(SWT.Traverse, new Listener() {
            public void handleEvent(Event e) {            
                if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });
    }
	
	/**
	 * Text 에 숫자만 입력가능 하도록 한다.
	 * @param text
	 */	
	public static void setTextInputToNumber(Text text) {
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(final VerifyEvent event) {
				switch (event.keyCode) {
				case SWT.BS: // Backspace
				case SWT.DEL: // Delete
				case SWT.HOME: // Home
				case SWT.END: // End
				case SWT.ARROW_LEFT: // Left arrow
				case SWT.ARROW_RIGHT: // Right arrow
					return;
				}
				if (!Character.isDigit(event.character)) {
					event.doit = false; // disallow the action
				}
			}
		});
	}

}
