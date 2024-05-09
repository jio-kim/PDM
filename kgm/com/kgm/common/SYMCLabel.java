package com.kgm.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings({"unused"})
public class SYMCLabel extends CLabel {

	private boolean mandantory = false;

	public SYMCLabel(Composite parent, int style) {
		super(parent, style);
		setLabelUI();
	}

	public SYMCLabel(Composite parent, String text) {
		this(parent, SWT.NONE);
		setText(text);
	}

	public SYMCLabel(Composite parent, String text, Object layoutData) {
		this(parent, text);
		setLayoutData(layoutData);
	}

	public SYMCLabel(Composite parent, String text, boolean mandantory) {
		super(parent, SWT.NONE);
		this.mandantory = mandantory;
		setText(text);
		setLabelUI();
	}

	public SYMCLabel(Composite parent, String text, boolean mandantory, Object layoutData) {
		this(parent, text, mandantory);
		setLayoutData(layoutData);
	}

	private void setLabelUI() {
		setAlignment(SWT.RIGHT);
		setBackground(new Color(null, 255, 255, 255));

		/*
		addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent event) {
				GC gc = event.gc;
				if (mandantory)
					gc.setForeground(new Color(null, 255, 80, 80));
				else
					gc.setForeground(new Color(null, 204, 204, 255));
				int i = 3;
				int j = 4;
				int k = 3;
				int l = 2;
				gc.drawLine(i - k, j, i + k, j);
				gc.drawLine(i, j - k, i, j + k);
				gc.drawLine(i - l, j - l, i + l, j + l);
				gc.drawLine(i - l, j + l, i + l, j - l);
			}
		});
        */
	}

	public void setMandantory(boolean mandantory) {
		this.mandantory = mandantory;
		redraw();
	}
}
