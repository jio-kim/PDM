package com.kgm.common.randerer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;

import com.teamcenter.rac.util.Registry;

/**
 * 1. 아이콘과 테이블셀 색깔을 동시에 바꿀수 있는 렌더러 TCTable 처럼 한출씩 색깔이 교차해서 나타난다.
 * 2. JList에 아이콘을 삽입할 수 있다. 
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2011. 4. 1.
 * @Package ID : com.teamcenter.common.IconColorTableCellRenderer.java
 */
public class IconColorCellRenderer2 extends IconColorCellRenderer{

	public IconColorCellRenderer2(Icon icon, Color color) {
		super(icon, color);
	}
	
	public IconColorCellRenderer2(Color color) {
		super(color);
	}
	
	public IconColorCellRenderer2(Icon icon) {
		super(icon);
	}

	private static final long serialVersionUID = 1L;
	
	/**
	 * 결재 상태에 따라서 아이콘이 다르게 표시 됨.
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
		} else {
			super.setForeground(jtable.getForeground());
			super.setBackground(jtable.getBackground());
			if(row%2 == 1)
				super.setBackground(color);
		}
		setValue(value);
		if(column == 4){
			Registry registry = Registry.getRegistry(this);
			if(value.toString().equals("승인")){
				setIcon(registry.getImageIcon("GreenCircle.ICON"));
			}
			else if(value.toString().equals("완료")){
				setIcon(registry.getImageIcon("BlueCircle.ICON"));
			}
			else if(value.toString().equals("거부")){
				setIcon(registry.getImageIcon("RedCircle.ICON"));
			}
			else if(value.toString().equals("보류")){
				setIcon(registry.getImageIcon("YellowCircle.ICON"));
			}
			else{
				setIcon(registry.getImageIcon("GrayCircle.ICON"));
			}
		}
		return this;
	}
}
