/**
 * 
 */
package com.symc.plm.me.common;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;

import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.common.controls.LOVComboBox;
import com.teamcenter.rac.kernel.TCSession;

public class SDVLOVComboBox extends LOVComboBox {

	private ControlDecoration decoration;

	public SDVLOVComboBox(Composite paramComposite) {
		super(paramComposite, SWT.BORDER);
	}

	public SDVLOVComboBox(Composite paramComposite, String paramString) {
		this(paramComposite, SWT.BORDER, CustomUtil.getTCSession(), paramString);

	}

	public SDVLOVComboBox(Composite paramComposite, int paramInt, TCSession paramTCSession, String paramString) {
		// super(paramComposite, paramInt, paramTCSession, paramString);
		super(paramComposite, paramInt, paramString);
	}

	public void setMandatory(boolean mandatory) {
		super.setMandatory(mandatory);

		if (mandatory && decoration == null) {
			decoration = SYMDisplayUtil.setRequiredFieldSymbol(this);
		}
		if (decoration == null) {
			return;
		}
		if (mandatory) {
			decoration.show();
		} else {
			decoration.hide();
		}

		redraw();
	}

	public String getText() {
		return super.getSelectedString();
	}

	public void setText(String value) {
		super.setSelectedString(value);
	}

	public void add(String string) {
		super.addItem(string);
	}

	public void add(String value, String desc) {
		super.addItem(value, desc);
	}

	/**
	 * Tab Next ¿Ãµø
	 */
	public void setEnableNextTab() {
		this.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
					e.doit = true;
				}
			}
		});
	}

}
