package com.kgm.common.randerer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.JTable;

import com.teamcenter.rac.common.TCTypeRenderer;

/**
 * 1. �����ܰ� ���̺� ������ ���ÿ� �ٲܼ� �ִ� ������ TCTable ó�� ���⾿ ������ �����ؼ� ��Ÿ����.
 * 2. JList�� �������� ������ �� �ִ�. 
 * @Copyright : S-PALM
 * @author   : ������
 * @since    : 2011. 4. 1.
 * @Package ID : com.teamcenter.common.IconColorTableCellRenderer.java
 */
public class TCTypeColorCellRenderer extends IconColorCellRenderer {

	private static final long serialVersionUID = 1L;

	public TCTypeColorCellRenderer(Color color){
		super(null, color);
	}

	/**
	 * row�� column �� ���� ������ ���鼭 �Ķ����� �������� �����Ǹ� �������ȴ�.
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since  : 2011. 4. 8.
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 * @param jtable
	 * @param value
	 * @param isSelected
	 * @param hasFocus
	 * @param row
	 * @param column
	 * @return Component
	 */
	public Component getTableCellRendererComponent(JTable jtable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		if(isSelected){
			super.setForeground(jtable.getSelectionForeground());
			super.setBackground(jtable.getSelectionBackground());
		} else {
			super.setForeground(jtable.getForeground());
			super.setBackground(jtable.getBackground());
			if(row%2 == 1)
				super.setBackground(color);
		}
		super.setValue(value);
		if(column == 0){
			setIcon(icon);
		}

		return this;
	}

	/**
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since  : 2011. 4. 8.
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 * @param list
	 * @param value
	 * @param index
	 * @param isSelected
	 * @param cellHasFocus
	 * @return Component
	 */
	@SuppressWarnings("rawtypes")
    @Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		if(isSelected){
			setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
		} else {
			setForeground(list.getForeground());
			setBackground(list.getBackground());
			if(index%2 == 1)
				super.setBackground(color);
		}
		setValue(value);
		setIcon(TCTypeRenderer.getIcon(value, false));
		return this;
	}
}
