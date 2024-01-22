/**
 * 
 */
package com.symc.plm.me.sdv.view.swm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.table.SDVTableView;
import org.sdv.core.ui.view.table.model.ColumnInfoModel;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : RegisterSWMOperationSearchResultView
 * Class Description :
 * 
 * @date 2013. 12. 1.
 * 
 */
public class RegisterSWMOperationSearchResultView extends SDVTableView {
    private Registry registry;
    private Label lblResultCount;

    public RegisterSWMOperationSearchResultView(Composite parent, int style, String id) {
        super(parent, style, id, 0);
    }

    @Override
    protected void initUI(Composite parent) {
        setColInfoModel();

        parent.setLayout(new BorderLayout());

        Composite labelComposite = new Composite(parent, SWT.NONE);
        labelComposite.setLayoutData(BorderLayout.SOUTH);
        labelComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

        lblResultCount = new Label(labelComposite, SWT.NONE);
        lblResultCount.setAlignment(SWT.RIGHT);

        Composite tableComposite = new Composite(parent, SWT.NONE);
        tableComposite.setLayoutData(BorderLayout.CENTER);

        super.initUI(tableComposite);

    }

    private void setColInfoModel() {
        registry = Registry.getRegistry(this);

        String[] ids = registry.getStringArray("register.table.column.swm.id");
        String[] names = registry.getStringArray("register.table.column.swm.name");
        String[] widths = registry.getStringArray("register.table.column.swm.width");
        String[] sorts = registry.getStringArray("register.table.column.swm.sort");
        String[] alignments = registry.getStringArray("register.table.column.swm.alignment");

        List<ColumnInfoModel> colModelList = new ArrayList<ColumnInfoModel>();
        for (int i = 0; i < ids.length; i++) {
            ColumnInfoModel columnModel = new ColumnInfoModel();
            columnModel.setColId(ids[i]);
            columnModel.setColName(names[i]);
            columnModel.setSort(Boolean.parseBoolean(sorts[i]));
            columnModel.setColumnWidth(Integer.parseInt(widths[i]));
            String align = alignments[i].toUpperCase();
            if ("LEFT".equals(align)) {
                columnModel.setAlignment(SWT.LEFT);
            } else if ("CENTER".equals(align)) {
                columnModel.setAlignment(SWT.CENTER);
            } else {
                columnModel.setAlignment(SWT.RIGHT);
            }

            colModelList.add(columnModel);
        }
        setColumnInfo(colModelList);
    }

    protected void setDialogCursor(final int cursorType) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                setCursor(new Cursor(getDisplay(), cursorType));
            }
        });
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {
        this.dataMap = dataMap;

        List<HashMap<String, Object>> operationList = (List<HashMap<String, Object>>) dataMap.getTableValue("swmItemList");

        if (operationList.size() > 0) {
            lblResultCount.setText("SearchResultCount : " + operationList.size() + " " + "EA");
        } else {
            lblResultCount.setText("SearchResultCount : " + 0 + " " + "EA");
        }
        setTableData(operationList);
    }

    public IDataMap getLocalDataMap() {
        return getLocalSelectDataMap();
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        IDataMap searchData = new RawDataMap();

        TableItem[] items = table.getSelection();
        String id = items[0].getText(0);
        String name = items[0].getText(2);

        searchData.put(SDVPropertyConstant.ITEM_ITEM_ID, id, IData.STRING_FIELD);
        searchData.put(SDVPropertyConstant.ITEM_OBJECT_NAME, name, IData.STRING_FIELD);

        searchData.put("targetId", (String) getParameters().get("targetId"), IData.STRING_FIELD);

        return searchData;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

}
