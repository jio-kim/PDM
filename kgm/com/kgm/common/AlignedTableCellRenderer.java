package com.kgm.common;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * CIS Table Cell Renderer ������. ��� Table �ʵ尪 �߾� ����.
 * @Copyright : S-PALM
 * @author   : �ǻ��
 * @since    : 2012. 9. 27.
 * Package ID : sns.teamcenter.commands.cisno.CISIDSearch.java
 */
public class AlignedTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (column == 0) {
            this.setHorizontalAlignment(SwingConstants.CENTER);
        } else if (column == 1) {
            this.setHorizontalAlignment(SwingConstants.CENTER);
        } else if (column == 2) {
            this.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            this.setHorizontalAlignment(SwingConstants.LEFT);
        }
        
        return this;
    }
}