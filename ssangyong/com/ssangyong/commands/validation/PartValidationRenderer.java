package com.ssangyong.commands.validation;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class PartValidationRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable jtable, Object obj,
			boolean flag, boolean flag1, int i, int j) {
		JLabel label = (JLabel)super.getTableCellRendererComponent(jtable, obj, flag, flag1, i, j);
		label.setToolTipText(label.getText());
		return label;
	}

}
