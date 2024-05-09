package com.kgm.common;

import javax.swing.table.DefaultTableModel;

/**
 * Spalm Table Model 재정의. 모든 Table 필드값 변경 불가 처리.
 * @Copyright : S-PALM
 * @author   : 권상기
 * @since    : 2012. 9. 27.
 * Package ID : sns.teamcenter.commands.cisno.CISIDSearch.java
 */
public class SYMCAWTTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	public SYMCAWTTableModel(String[][] valueArr, String[] strings) {
		super(valueArr, strings);
	}
	
	public SYMCAWTTableModel(String[] valueArr, int rowCount) {
		super(valueArr, rowCount);
	}

	/**
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2012. 9. 26.
	 * @override
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 * @param row
	 * @param column
	 * @return
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}