/**
 * 
 */
package com.symc.plm.me.sdv.view.swm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.table.SDVTableView;
import org.sdv.core.ui.view.table.model.ColumnInfoModel;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVQueryUtils;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.excel.transformer.DownloadSWMDocExcelTransformer;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SearchListSWMDocView
 * Class Description
 * 
 * @date 2013. 11. 14.
 * 
 */
public class SearchListSWMDocView extends SDVTableView {
    private Registry registry;
    private Label lblResultCount;
    TCComponentItemRevision itemRevision;
    TableItem tableItem;

    public SearchListSWMDocView(Composite parent, int style, String id) {
        super(parent, style, id, 0);
    }

    /**
     * @param parent
     * @param style
     * @param id
     * @param configId
     */
    public SearchListSWMDocView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
    }

    @Override
    protected void initUI(Composite parent) {
        registry = Registry.getRegistry(this);

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

        String[] ids = registry.getStringArray("search.table.column.swm.id");
        String[] names = registry.getStringArray("search.table.column.swm.name");
        String[] widths = registry.getStringArray("search.table.column.swm.width");
        String[] sorts = registry.getStringArray("search.table.column.swm.sort");
        String[] alignments = registry.getStringArray("search.table.column.swm.alignment");

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

    public void tableModify(Map<String, Object> parameters) {
        if (parameters.containsKey("targetId")) {
            tableItem = table.getSelection()[0];

            int dataIndex = (Integer) tableItem.getData("dataMapIndex");
            HashMap<String, Object> dataMap = dataList.get(dataIndex);

            if (parameters.get(SDVPropertyConstant.ITEM_OBJECT_NAME) != null) {
                String workNameModify = (String) parameters.get(SDVPropertyConstant.ITEM_OBJECT_NAME);
                tableItem.setText(2, workNameModify);
                dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, workNameModify);
            }
            if (parameters.get(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO) != null) {
                String referenceInfoModify = (String) parameters.get(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO);
                tableItem.setText(4, referenceInfoModify);
                dataMap.put(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO, referenceInfoModify);
            }
            if (parameters.get(SDVPropertyConstant.ITEM_M7_DISCARD_DATE) != null) {
                String discardDateModify = SDVStringUtiles.dateToString((Date) parameters.get(SDVPropertyConstant.ITEM_M7_DISCARD_DATE), "yyyy-MM-dd");
                tableItem.setText(6, (String) discardDateModify);
                dataMap.put(SDVPropertyConstant.ITEM_M7_DISCARD_DATE, discardDateModify);
            } else {
                tableItem.setText(6, "");
                dataMap.put(SDVPropertyConstant.ITEM_M7_DISCARD_DATE, "");
            }
            if (parameters.get(SDVPropertyConstant.SWM_GROUP) != null) {
                String groupModify = (String) parameters.get(SDVPropertyConstant.SWM_GROUP);
                tableItem.setText(10, groupModify);
                dataMap.put(SDVPropertyConstant.SWM_GROUP, groupModify);
            }

        }
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {
        this.dataMap = dataMap;
        if (dataMap.containsKey("actionId")) {
            String actionId = dataMap.getStringValue("actionId");
            if (actionId.equals("Search")) {
                List<HashMap<String, Object>> operationList = (List<HashMap<String, Object>>) dataMap.getTableValue("operationList");
                if (operationList.size() > 0) {
                    lblResultCount.setText("SearchResultCount : " + operationList.size() + " " + "EA");
                } else {
                    lblResultCount.setText("SearchResultCount : " + 0 + " " + "EA");
                }
                setTableData(operationList);
            }
        }
    }

    @Override
    public IDataMap getLocalDataMap() {

        return dataMap;
    }

    /**
     * List 버튼 클릭시 action
     * 
     */
    public void exportSearchResult() {
        if (dataMap.containsKey("actionId")) {
            String actionId = dataMap.getStringValue("actionId");

            if (!actionId.equals("Search") && !actionId.equals("Download") && !actionId.equals("Modify") && !actionId.equals("Workflow Process")) {
                return;
            }
        }

        List<HashMap<String, Object>> operationList = new ArrayList<HashMap<String, Object>>();

        TableItem[] items = table.getItems();
        if (items != null) {
            for (TableItem item : items) {
                HashMap<String, Object> itemMap = new HashMap<String, Object>();
                TableColumn[] columns = table.getColumns();
                for (int i = 0; i < columns.length; i++) {
                    Object columnData = columns[i].getData("columnInfo");
                    if (columnData != null && columnData instanceof ColumnInfoModel) {
                        ColumnInfoModel columnModel = (ColumnInfoModel) columnData;
                        itemMap.put(columnModel.getColId(), item.getText(i));
                    }
                }
                operationList.add(itemMap);
            }
        }

        dataMap.put("operationList", operationList, IData.TABLE_FIELD);
        IDataSet dataset = new DataSet();
        dataset.addDataMap("downloadList", dataMap);

        int mode = ExcelTemplateHelper.EXCEL_SAVE;
        String templatePreference = "M7_TEM_DocItemID_StdWorkMethodList";
        String defaultFileName = "표준작업요령 관리대장" + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");

        DownloadSWMDocExcelTransformer downloadSWMDocExcelTransformer = new DownloadSWMDocExcelTransformer();
        downloadSWMDocExcelTransformer.print(mode, templatePreference, defaultFileName, dataset);
    }

    /**
     * Download 버튼 클릭시 action
     * 
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        if (table.getSelectionCount() == 0) {
            if (dataMap.containsKey("targetOperationList")) {
                dataMap.getTableValue("targetOperationList").clear();
            }
        } else {
            List<HashMap<String, Object>> operationList = new ArrayList<HashMap<String, Object>>();
            TableItem[] items = table.getSelection();
            for (int i = 0; i < items.length; i++) {
                int dataMapIndex = (Integer) items[i].getData("dataMapIndex");
                operationList.add(dataList.get(dataMapIndex));
            }

            dataMap.put("targetOperationList", operationList, IData.TABLE_FIELD);
        }

        return dataMap;
    }

    /**
     * Modify 버튼 클릭시 action
     * 
     */
    public void getModifydialog() throws Exception {
        if (dataMap.containsKey("actionId")) {
            String actionId = dataMap.getStringValue("actionId");

            if (!actionId.equals("Search") && !actionId.equals("Download") && !actionId.equals("List") && !actionId.equals("Workflow Process")) {
                return;
            }
        }
        if (table.getSelectionCount() == 0 || table.getSelectionCount() > 1) {
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("SelectOneTargetItem.MESSAGE"), "Warning", MessageBox.WARNING);
            return;
        }
        tableItem = table.getSelection()[0];
        String itemId = tableItem.toString();
        String targetItem = itemId.substring(itemId.indexOf('{') + 1, itemId.lastIndexOf('}'));

        TCComponent[] qryResult = SDVQueryUtils.executeSavedQuery("SYMC_Search_StdWorkMethod", new String[] { SDVPropertyConstant.ITEM_ITEM_ID }, new String[] { targetItem });
        TCComponentItem item = (TCComponentItem) qryResult[0];
        itemRevision = item.getLatestItemRevision();

        if (CustomUtil.isInProcess((TCComponent) itemRevision)) {
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("SelectedTargetIsInAWorkflowProcess.MESSAGE"), "Warning", MessageBox.WARNING);
            return;
        }

        if (CustomUtil.isReleased((TCComponent) itemRevision)) {
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("SelectedTargetWasReleased.MESSAGE"), "Warning", MessageBox.WARNING);
            return;
        }

        String checkOutUser = itemRevision.getProperty("checked_out_user");
        if (!CustomUtil.getTCSession().getUser().toString().equals(checkOutUser) && !checkOutUser.trim().equals("")) {
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("SelectedTargetIsWorking.MESSAGE"), "Warning", MessageBox.WARNING);
            return;
        }

        IDialog dialog = UIManager.getDialog(getShell(), "symc.dialog.modifySWMDocDialog");
        String dialogId = UIManager.getCurrentDialog().getId();
        Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.put(SDVPropertyConstant.ITEM_ITEM_ID, itemRevision);
        paramMap.put("targetId", dialogId + "/" + getId());

        dialog.setParameters((Map<String, Object>) paramMap);

        dialog.open();
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
