/**
 *
 */
package com.symc.plm.me.sdv.service.resource;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.sdv.core.ui.view.table.model.ColumnInfoModel;

import com.symc.plm.me.utils.BundleUtil;

/**
 * TableViewer Composite (테이블 헤더 정렬 기능)
 *
 * 1. 컬럼설정
 *
 * @see org.sdv.core.ui.view.table.model.ColumnInfoModel
 *      String colId; 컬럼 ID
 *      String colName; 컬럼명
 *      boolean isSort; 헤더Sorting 유무
 *      boolean isEditable; Cell 편집 유무
 *      int columnWidth; 컬럼 넓이
 *
 *      2. 데이터 로드
 *      public void setInput(HashMap[] datas)
 *      HashMap<String #컬럼 ID(colId)#, Object #Value#>
 *
 *      Class Name : TableView
 *      Class Description :
 *
 * @date 2013. 9. 24.
 *
 */
public class SDVTableViewer {

    protected TableViewer tableViewer;
    protected ArrayList<ColumnInfoModel> columnInfos;
    protected HashMap<TableViewerColumn, Integer> colIndexMap;
    @SuppressWarnings("rawtypes")
    protected HashMap<HashMap, Object> modifyDatas;

    
    /**
     * Create the composite.
     *
     * @param parent
     * @param style
     */    
    public SDVTableViewer(Composite parent, int style) {
        tableViewer = new TableViewer(parent, style);
        tableContentProvider(tableViewer);
    }

    public void tableContentProvider(TableViewer tableViewer) {
        tableViewer.setContentProvider(new MyContentProvider());
    }

    @SuppressWarnings("rawtypes")
    public void createColModel(final ArrayList<ColumnInfoModel> columnInfos) {
        this.columnInfos = columnInfos;
        colIndexMap = new HashMap<TableViewerColumn, Integer>();
        modifyDatas = new HashMap<HashMap, Object>();
        for (int i = 0; i < this.columnInfos.size(); i++) {
            TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
            colIndexMap.put(column, i);
            column.getColumn().setWidth(this.columnInfos.get(i).getColumnWidth());
            column.getColumn().setText(this.columnInfos.get(i).getColName());
            column.getColumn().setMoveable(true);
            final int cnt = i;
            column.setLabelProvider(new ColumnLabelProvider() {
                public String getText(Object element) {
                    return (String) ((HashMap) element).get(columnInfos.get(cnt).getColId());
                }
            });
            if (this.columnInfos.get(i).isEditable()) {
                column.setEditingSupport(new AbstractEditingSupport(tableViewer) {
                    protected Object getValue(Object element) {
                        return ((HashMap) element).get(columnInfos.get(cnt).getColId());
                    }

                    @SuppressWarnings({ "unchecked" })
                    protected void doSetValue(Object element, Object value) {
                        ((HashMap)element).put(columnInfos.get(cnt).getColId(), value.toString());
                    }
                });
            }
            if (this.columnInfos.get(i).isSort()) {
                @SuppressWarnings("unused")
                ColumnViewerSorter cSorter = new ColumnViewerSorter(tableViewer, column) {
                    protected int doCompare(Viewer viewer, Object e1, Object e2) {
                        HashMap p1 = (HashMap) e1;
                        HashMap p2 = (HashMap) e2;
                        return BundleUtil.nullToString((String)p1.get(columnInfos.get(cnt).getColId())).toString().compareToIgnoreCase( BundleUtil.nullToString((String)p2.get(columnInfos.get(cnt).getColId())).toString());
                    }
                };
                // cSorter.setSorter(cSorter, ColumnViewerSorter.ASC);
            }
        }

        Table table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
    }

    /**
     * Data Load..
     *
     * @method setInput
     * @date 2013. 9. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public void setInput(HashMap[] datas) {
        getTableViewer().setInput(datas);
    }


    public TableViewer getTableViewer(){
        return this.tableViewer;
    }
    
    public Table getTable(){
        return this.tableViewer.getTable();
    }


    /**
     * @return the columnInfos
     */
    public ArrayList<ColumnInfoModel> getColumnInfos() {
        return columnInfos;
    }

    /**
     * @return the colIndexMap
     */
    public HashMap<TableViewerColumn, Integer> getColIndexMap() {
        return colIndexMap;
    }


    /**
     * 변경된 Row Data 정보
     *
     * @method getModifyDatas
     * @date 2013. 9. 24.
     * @param
     * @return HashMap[]
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public HashMap[] getModifyDatas() {
        return this.modifyDatas.keySet().toArray(new HashMap[modifyDatas.size()]);
    }

    private class MyContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return (HashMap[]) inputElement;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    protected abstract class AbstractEditingSupport extends EditingSupport {

        private TextCellEditor editor;

        public AbstractEditingSupport(TableViewer viewer) {
            super(viewer);
            this.editor = new TextCellEditor(viewer.getTable());
        }

        protected boolean canEdit(Object element) {
            return true;
        }

        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @SuppressWarnings("rawtypes")
        protected void setValue(Object element, Object value) {
            doSetValue(element, value);
            getViewer().update(element, null);
            modifyDatas.put((HashMap)element, value);
        }

        protected abstract void doSetValue(Object element, Object value);
    }

    private static abstract class ColumnViewerSorter extends ViewerComparator {
        public static final int ASC = 1;

        public static final int NONE = 0;

        public static final int DESC = -1;

        private int direction = 0;

        private TableViewerColumn column;

        private ColumnViewer viewer;

        public ColumnViewerSorter(ColumnViewer viewer, TableViewerColumn column) {
            this.column = column;
            this.viewer = viewer;
            this.column.getColumn().addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    if (ColumnViewerSorter.this.viewer.getComparator() != null) {
                        if (ColumnViewerSorter.this.viewer.getComparator() == ColumnViewerSorter.this) {
                            int tdirection = ColumnViewerSorter.this.direction;

                            if (tdirection == ASC) {
                                setSorter(ColumnViewerSorter.this, DESC);
                            } else if (tdirection == DESC) {
                                setSorter(ColumnViewerSorter.this, NONE);
                            }
                        } else {
                            setSorter(ColumnViewerSorter.this, ASC);
                        }
                    } else {
                        setSorter(ColumnViewerSorter.this, ASC);
                    }
                }
            });
        }

        public void setSorter(ColumnViewerSorter sorter, int direction) {
            if (direction == NONE) {
                column.getColumn().getParent().setSortColumn(null);
                column.getColumn().getParent().setSortDirection(SWT.NONE);
                viewer.setComparator(null);
            } else {
                column.getColumn().getParent().setSortColumn(column.getColumn());
                sorter.direction = direction;

                if (direction == ASC) {
                    column.getColumn().getParent().setSortDirection(SWT.DOWN);
                } else {
                    column.getColumn().getParent().setSortDirection(SWT.UP);
                }
                if (viewer.getComparator() == sorter) {
                    viewer.refresh();
                } else {
                    viewer.setComparator(sorter);
                }

            }
        }

        public int compare(Viewer viewer, Object e1, Object e2) {
            return direction * doCompare(viewer, e1, e2);
        }

        protected abstract int doCompare(Viewer viewer, Object e1, Object e2);
    }
}
