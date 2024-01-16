package com.ssangyong.common;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SYMCRadioButton extends Composite {
	private String[] saValues;
	private ArrayList<Button> alRadios;
	
	public SYMCRadioButton(Composite cpsParent, int style, String[] saValues) {
		super(cpsParent, style);
		
		alRadios = new ArrayList<Button>();
		this.saValues = saValues;
		
		initial();
		
		layout();
	}
	
	private void initial() {
		GridLayout layout = new GridLayout(saValues.length, false);
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		
		GridData gdMain = new GridData(GridData.FILL_BOTH);
		setLayoutData(gdMain);
		
		for (int inx = 0; inx < saValues.length; inx++) {
			Button btnRadio = new Button(this, SWT.RADIO);
			btnRadio.setText(saValues[inx]);
			GridData gdBtn = new GridData(GridData.FILL_BOTH);
			btnRadio.setLayoutData(gdBtn);
			btnRadio.pack();
			
			alRadios.add(btnRadio);
		}
	}
	
	/**
	 * Set Selection Value
	 * @param sValue
	 */
	public void setSelection(String sValue) {
		for (int inx = 0; inx < alRadios.size(); inx++) {
			Button btnRadio = alRadios.get(inx);
			String sButtonText = btnRadio.getText();
			
			if (sButtonText.equals(sValue)) {
				btnRadio.setSelection(true);
			} else {
				btnRadio.setSelection(false);
			}
		}
	}

	/**
	 * Return Selection Value
	 * @return
	 */
	public String getValue() {
		for (int inx = 0; inx < alRadios.size(); inx++) {
			Button btnRadio = alRadios.get(inx);
			if (btnRadio.getSelection()) {
				return btnRadio.getText();
			}
		}
		
		return "";
	}
	
	/**
	 * Return Button Array
	 * @return
	 */
	public ArrayList<Button> getRadioButtons() {
		return alRadios;
	}
	
	public void dispose() {
		for (int inx = 0; inx < alRadios.size(); inx++) {
			Button btn = alRadios.get(inx);
			btn.dispose();
		}
		
		super.dispose();
	}
}
