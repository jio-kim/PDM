package com.ssangyong.common.randerer;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * 콤보박스의 리스트에 툴팁을 보여주는 렌더러
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2012. 4. 5.
 * Package ID : com.pungkang.common.renderer.ComboBoxRenderer.java
 */
public class ComboBoxRenderer extends BasicComboBoxRenderer {

	private static final long serialVersionUID = 1L;
	
	private Icon icon;

	public ComboBoxRenderer(Icon icon) {
		this.icon = icon;
	}

	@SuppressWarnings("rawtypes")
    @Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if(isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setText((value == null) ? "" : value.toString());
		if(index != -1){
			setIcon(icon);
		}
		return this;
	}
}
