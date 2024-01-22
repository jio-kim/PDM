/**
 * 
 */
package com.symc.plm.me.common;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.ssangyong.common.utils.SYMDisplayUtil;

/**
 *
 */
public class SDVComboBox {
	private Combo comboBox = null;
	private boolean mandatory = false;
	private ControlDecoration decoration = null;

	/**
	 * @param parent
	 * @param style
	 */
	public SDVComboBox(Composite parent, int style) {
		comboBox = new Combo(parent, style);
	}

	public SDVComboBox(Composite parent, int style, boolean mandatory) {
		comboBox = new Combo(parent, style);

		setMandatory(mandatory);
	}

	public SDVComboBox(Composite parent, int style, Object[] comboDatas)
	{
		comboBox = new Combo(parent, style);

		addItems(comboDatas);
	}

	public void addItems(Object[] comboDatas) {
		if (comboDatas == null || comboDatas.length == 0)
			return;

		if (comboBox.getItemCount() > 0)
			comboBox.removeAll();

		for (Object comboData : comboDatas)
		{
			comboBox.add(comboData.toString());
			comboBox.setData(comboData.toString(), comboData);
		}
	}

	public Object getSelectedItem() {
		if (comboBox.getSelectionIndex() < 0)
			return comboBox.getText();
		else
			return comboBox.getData(comboBox.getItem(comboBox.getSelectionIndex()));
	}

    public void setMandatory(boolean mandatory) {

        this.mandatory = mandatory;

        if (mandatory && decoration == null) {
            decoration = SYMDisplayUtil.setRequiredFieldSymbol(comboBox);
        }
        if (decoration == null) {
            return;
        }
        if (mandatory) {
            decoration.show();
        } else {
            decoration.hide();
        }

        comboBox.redraw();
    }

    public boolean isMandatory() {
    	return mandatory;
    }

	public void setLayoutData(Object layoutData) {
		comboBox.setLayoutData(layoutData);
	}

	public int getSelectionIndex() {
		return comboBox.getSelectionIndex();
	}

	public String getItem(int selectionIndex) {
		return comboBox.getItem(selectionIndex);
	}

	public Object getData(String key) {
		return comboBox.getData(key);
	}

	public void add(String string, int index) {
		comboBox.add(string, index);
	}

	public void setEnabled(boolean enabled) {
		comboBox.setEnabled(enabled);
	}

	public void setSelectedItem(Object object) {
		comboBox.setText(object.toString());
	}
}
