package com.symc.plm.rac.prebom.preospec.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class CategoryCellRenerer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		// TODO Auto-generated method stub
		JCheckBox check = new JCheckBox();
		check.setBackground(Color.WHITE);
		check.setText(value.toString());
		return check;
	}

}
