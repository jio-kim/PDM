package org.sdv.core.ui.view.table;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.ui.view.table.model.ColumnInfoModel;
import org.sdv.core.util.UIUtil;

import com.teamcenter.rac.util.MessageBox;

public class SDVTableView extends AbstractSDVViewPane implements SelectionListener {

    protected Table table;
    protected List<ColumnInfoModel> columnInfoList;
    protected List<HashMap<String, Object>> dataList;
    protected IDataMap dataMap;
//    private List<TableEditor> editorList;

    public SDVTableView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
    }

    public SDVTableView(Composite parent, int style, String id, int configId, String order) {
        super(parent, style, id, configId, order);
    }

    public void setColumnInfo(List<ColumnInfoModel> columnInfoList) {
        this.columnInfoList = columnInfoList;
    }

    @Override
    protected void initUI(Composite parent) {
        parent.setLayout(new FillLayout(SWT.HORIZONTAL));

        table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        if(columnInfoList != null) {
            for(int i = 0; i < columnInfoList.size(); i++) {
                ColumnInfoModel columnInfo = columnInfoList.get(i);
                TableColumn column = new TableColumn(table, SWT.MULTI);
                column.setData("index", i);
                column.setData("columnInfo", columnInfo);
                column.setText(columnInfo.getColName());
                column.setAlignment(columnInfo.getAlignment());
                column.setWidth(columnInfo.getColumnWidth());
                column.setResizable(true);
                column.addSelectionListener(new SelectionListener() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        TableColumn column = (TableColumn) e.widget;
                        if(((ColumnInfoModel) column.getData("columnInfo")).isSort()) {
                            doSort(column);
                        }
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {

                    }
                });
            }
        }
    }

    public void doSort(TableColumn currentColumn) {
        TableColumn sortColumn = table.getSortColumn();

        int dir = table.getSortDirection();
        if (sortColumn == currentColumn) {
          dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
        } else {
          dir = SWT.UP;
        }

        TableItem[] items = table.getItems();
        Collator collator = Collator.getInstance(Locale.getDefault());

        int index = (Integer) currentColumn.getData("index");
        for (int i = 1; i < items.length; i++) {
            String value1 = items[i].getText(index);
            for (int j = 0; j < i; j++){
                String value2 = items[j].getText(index);
                if(dir == SWT.UP) {
                    if (collator.compare(value1, value2) < 0) {
                        int dataIndex = (Integer) items[i].getData("dataMapIndex");
                        items[i].dispose();
                        createTableItem(dataList.get(dataIndex), dataIndex, j);
                        items = table.getItems();
                        break;
                    }
                } else if(dir == SWT.DOWN) {
                    if (collator.compare(value1, value2) > 0) {
                        int dataIndex = (Integer) items[i].getData("dataMapIndex");
                        items[i].dispose();
                        createTableItem(dataList.get(dataIndex), dataIndex, j);
                        items = table.getItems();
                        break;
                    }
                }
            }
        }

        // table sort시 table redraw를 수행해도 combo가 Redraw 안됨.
        // table size가 변경되어야 redraw 하여 table size 조정으로 처리.
        table.setSize(table.getSize().x, table.getSize().y - 1);
        table.setSize(table.getSize().x, table.getSize().y + 1);

//        table.redraw();
        table.setSortDirection(dir);
        table.setSortColumn(currentColumn);
    }

    protected void setTableData(List<HashMap<String, Object>> dataList) {
        this.table.removeAll();
        this.dataList = dataList;
        if(this.dataList!=null){
        	for(int i = 0; i < this.dataList.size(); i++) {
        		HashMap<String, Object> dataMap = this.dataList.get(i);
        		createTableItem(dataMap, i, i);
        	}
        }

    }

    @SuppressWarnings("unchecked")
    /**
     * [SR141219-020][20150108] shcho, Open with Time 창에서의 Activity 작업순서 불일치 및 순서 편집 불가 대응 신규 화면 추가
     */
    protected void createTableItem(HashMap<String, Object> dataMap, int dataIndex, int index) {
        TableItem tableItem = new TableItem(table, SWT.NONE, index);
        tableItem.setData("dataMapIndex", dataIndex);

        for (int i = 0; i < columnInfoList.size(); i++) {
            String id = columnInfoList.get(i).getColId();
            int type = columnInfoList.get(i).getColType();
            
            // [20151124] taeku.jeong 조립작업표준서 검색 과정에 NullPointerException 발생되어
            // 조건절에 ataMap!=null 추가
            if (dataMap!=null && dataMap.get(id) != null) {
                if (type == ColumnInfoModel.COLUMN_TYPE_TEXT) {
                    tableItem.setText(i, (String) dataMap.get(id));
                } else {
                    Control control = null;
                    TableEditor editor = new TableEditor(table);
                    if (type == ColumnInfoModel.COLUMN_TYPE_COMBO) {
                        control = new CCombo(table, SWT.NONE);
                        control.setData("tableItem", tableItem);
                        ((CCombo) control).addSelectionListener(this);
                        control.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
                        ((CCombo) control).setEditable(false);
                        Object[] comboDataArr = (Object[]) dataMap.get(id);
                        if (comboDataArr != null) {
                            int k;
                            for (k = 0; k < comboDataArr.length; k++) {
                                ((CCombo) control).add((String) comboDataArr[k]);
                            }
                            ((CCombo) control).select(k - 1);
                        }
                    } else if (type == ColumnInfoModel.COLUMN_TYPE_CHECK) {
                        control = new Button(table, SWT.CHECK);
                    } else if (type == ColumnInfoModel.COLUMN_TYPE_BUTTON) {
                        control = new Button(table, SWT.NONE);
                        ((Button) control).setText((String) dataMap.get(id));
                    } else if (type == ColumnInfoModel.COLUMN_TYPE_TEXT_EDITOR) {
                        control = new Text(table, SWT.NONE);
                        ((Text) control).setText((String) dataMap.get(id));
                    }
                    control.pack();
                    editor.grabHorizontal = true;
                    editor.setEditor(control, tableItem, i);
                    if (tableItem.getData("editors") == null) {
                        HashMap<Integer, TableEditor> editors = new HashMap<Integer, TableEditor>();
                        tableItem.setData("editors", editors);
                    }
                    ((HashMap<Integer, TableEditor>) tableItem.getData("editors")).put(i, editor);
                }
            }
        }

        tableItem.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                TableItem item = (TableItem) e.widget;
                HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>) item.getData("editors");
                if(editors != null) {
                    for(Integer key : editors.keySet()) {
                        Control control = editors.get(key).getEditor();
                        control.dispose();
                        editors.get(key).dispose();
                    }
                }
            }
        });
    }


    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        if(table.getSelectionCount() == 0) {
            MessageBox.post(new Throwable(), "공법을 선택해 주세요.");
        }

        IDataMap dataMap = new RawDataMap();

        List<HashMap<String, Object>> operationList = new ArrayList<HashMap<String,Object>>();
        TableItem[] items = table.getSelection();
        for(int i = 0; i < items.length; i++) {
            int dataMapIndex = (Integer) items[i].getData("dataMapIndex");
            operationList.add(dataList.get(dataMapIndex));
        }

        dataMap.put("targetOperationList", operationList, IData.LIST_FIELD);

        return dataMap;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    @Override
    public void uiLoadCompleted() {

    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

    @Override
    public void widgetSelected(SelectionEvent e) {

    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {

    }
}
