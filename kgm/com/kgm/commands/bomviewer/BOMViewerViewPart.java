package com.kgm.commands.bomviewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.teamcenter.rac.util.MessageBox;

public class BOMViewerViewPart extends ViewPart {
	private BOMViewerPanel panel;
	
	public BOMViewerViewPart() {
		
	}

	@Override
	public void createPartControl(Composite parent) {
		try {
			panel = new BOMViewerPanel(parent, SWT.NONE);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}

	@Override
	public void setFocus() {
		if (panel == null) {
			return;
		}
		
		panel.setFocus();
	}
	
	public BOMViewerPanel getPanel() {
		return panel;
	}
}
