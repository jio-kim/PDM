package com.kgm.commands.commonpartcheck;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * 1. 아이콘과 테이블셀 색깔을 동시에 바꿀수 있는 렌더러 TCTable 처럼 한출씩 색깔이 교차해서 나타난다.
 * 2. JList에 아이콘을 삽입할 수 있다. 
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2011. 4. 1.
 * @Package ID : com.teamcenter.common.IconColorTableCellRenderer.java
 */
public class IconColorCellRenderer extends DefaultTableCellRenderer implements ListCellRenderer{

	private static final long serialVersionUID = 1L;

	protected Icon icon;

	protected Color color;

	public IconColorCellRenderer(){
		this(null, null);
	}

	public IconColorCellRenderer(Icon icon, Color color){
		this.icon = icon;
		if(color == null){
			this.color = new Color(230,230,230);
		}
		else{
			this.color = color;
		}
	}

	public IconColorCellRenderer(Color color){
		this(null, color);
		this.color = color;
	}

	public IconColorCellRenderer(Icon icon){
		this(icon, null);
		this.icon = icon;
	}

	/**
	 * row와 column 에 따라서 번갈아 가면서 파란색과 빨간색이 교차되며 렌더링된다.
	 * @Copyright : S-PALM
	 * @author : 이정건
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
			if(-1 < row) {
				jtable.setToolTipText( (value == null ? "" : value.toString()) );
			}
		} else {
			super.setForeground(jtable.getForeground());
			super.setBackground(jtable.getBackground());
			if(row%2 == 1)
				super.setBackground(color);
		}
		setValue(value);
		if(column == 0){
			setIcon(icon);
		}

		return this;
	}

	/**
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2011. 4. 8.
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 * @param list
	 * @param value
	 * @param index
	 * @param isSelected
	 * @param cellHasFocus
	 * @return Component
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		if(isSelected){
			setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
		} else {
			setForeground(list.getForeground());
			setBackground(list.getBackground());
			if(index%2 == 1)
				setBackground(color);
		}
		setValue(value);
		setIcon(icon);
		return this;
	}
}
