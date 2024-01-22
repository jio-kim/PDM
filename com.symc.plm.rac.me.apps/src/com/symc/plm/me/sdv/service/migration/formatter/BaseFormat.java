/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.formatter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Class Name : BaseFormat
 * Class Description :
 * 
 * @date 2013. 11. 21.
 * 
 */
public abstract class BaseFormat implements IImportFormat {
    Shell shell;
    Tree tree;
    String formatFilePath;
    LinkedHashMap<String, String> displayOrderData;
    // STATUS 컬럼 ID
    public final static String STATUS_COLUMN_ID = "migrationDisplaySatus";

    public BaseFormat(Shell shell, Tree tree, String formatFilePath) {
        this.shell = shell;
        this.tree = tree;
        this.formatFilePath = formatFilePath;
        // Display Column 설정
        this.displayOrderData = new LinkedHashMap<String, String>();
        displayOrderData.put("Level", "Level");
        displayOrderData.put("item_id", "Item Id");
        displayOrderData.put("item_revision_id", "Item Revision.");
        displayOrderData.put("object_name", "Object Name");
    }

    /**
     * @return the displayOrderData
     */
    public LinkedHashMap<String, String> getDisplayOrderData() {
        return displayOrderData;
    }

    /**
     * @param displayOrderData
     *            the displayOrderData to set
     */
    public void setDisplayOrderData(LinkedHashMap<String, String> displayOrderData) {
        this.displayOrderData = displayOrderData;
    }
    
    /**
     * 컬럼생성
     * 
     * @method creatColumn
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected void creatColumn(final Tree tree, final LinkedHashMap<String, HashMap<String, String>> columnInfos, final LinkedHashMap<String, String> displayOrderData) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                if(columnInfos != null) {
                    @SuppressWarnings("rawtypes")                
                    Iterator keyData = columnInfos.keySet().iterator();
                    while (keyData.hasNext()) {
                        String columnId = (String) keyData.next();
                        TreeColumn dataColumn = new TreeColumn(tree, SWT.NONE);
                        dataColumn.setData((columnInfos.get(columnId)));
                        dataColumn.setWidth(100);
                        dataColumn.setText((columnInfos.get(columnId)).get("NAME"));
                    }
                } else {
                    @SuppressWarnings("rawtypes")                
                    Iterator keyData = displayOrderData.keySet().iterator();
                    while (keyData.hasNext()) {
                        String id = (String) keyData.next();
                        TreeColumn dataColumn = new TreeColumn(tree, SWT.NONE);
                        dataColumn.setData(id);
                        dataColumn.setWidth(100);
                        dataColumn.setText((displayOrderData.get(id)));
                    }
                }
                // 제일 마지막 컬럼에 상태(STATUS컬럼 추가)
                TreeColumn statusColumn = new TreeColumn(tree, SWT.NONE);
                statusColumn.setData(STATUS_COLUMN_ID);
                statusColumn.setWidth(105);
                statusColumn.setText("Status");
                tree.setHeaderVisible(true);
            }
        });
    }

}
