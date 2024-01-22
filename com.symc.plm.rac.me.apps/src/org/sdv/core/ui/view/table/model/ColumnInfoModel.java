/**
 *
 */
package org.sdv.core.ui.view.table.model;

import org.eclipse.swt.SWT;

/**
 *
 * Class Name : ColumnInfoModel
 * Class Description :
 *
 * @date 2013. 9. 24.
 *
 */
public class ColumnInfoModel {
    public static final int COLUMN_TYPE_TEXT = 1;
    public static final int COLUMN_TYPE_COMBO = 2;
    public static final int COLUMN_TYPE_CHECK = 3;
    public static final int COLUMN_TYPE_BUTTON = 4;
    //[SR141219-020][20150108] shcho, Open with Time 창에서의 Activity 작업순서 불일치 및 순서 편집 불가 대응 신규 화면 추가
    public static final int COLUMN_TYPE_TEXT_EDITOR = 5;

    String colId;
    String colName;
    boolean isSort;
    boolean isEditable;
    int columnWidth;
    int colType = COLUMN_TYPE_TEXT;
    int alignment = SWT.LEFT;

    /**
     * @return the colId
     */
    public String getColId() {
        return colId;
    }

    /**
     * @param colId
     *            the colId to set
     */
    public void setColId(String colId) {
        this.colId = colId;
    }

    /**
     * @return the colName
     */
    public String getColName() {
        return colName;
    }

    /**
     * @param colName
     *            the colName to set
     */
    public void setColName(String colName) {
        this.colName = colName;
    }

    /**
     * @return the isSort
     */
    public boolean isSort() {
        return isSort;
    }

    /**
     * @param isSort
     *            the isSort to set
     */
    public void setSort(boolean isSort) {
        this.isSort = isSort;
    }

    /**
     * @return the columnWidth
     */
    public int getColumnWidth() {
        return columnWidth;
    }

    /**
     * @param columnWidth
     *            the columnWidth to set
     */
    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
    }

    /**
     * @return the isEditable
     */
    public boolean isEditable() {
        return isEditable;
    }

    /**
     * @param isEditable the isEditable to set
     */
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public int getColType() {
        return colType;
    }

    public void setColType(int colType) {
        this.colType = colType;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

}
