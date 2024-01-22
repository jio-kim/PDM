package com.symc.plm.me.sdv.view.weld.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.exception.SDVRuntimeException;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.table.SDVTableView;
import org.sdv.core.ui.view.table.model.ColumnInfoModel;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.util.MessageBox;

public class ListView extends SDVTableView {
    //private Table table;

    //private Registry registry;

    private Label lblResultCount;

    public ListView(Composite parent, int style, String id) {
        this(parent, style, id, 0);
    }

    public ListView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
    }

    public ListView(Composite parent, int style, String id, int configId, String order) {
        super(parent, style, id, configId, order);
    }

    @Override
    protected void initUI(Composite parent) {
        setColInfoModel();
        //registry = Registry.getRegistry(this);

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

//        String[] ids = registry.getStringArray("listview.table.column.search.id");
//        String[] names = registry.getStringArray("listview.table.column.search.name");
//        String[] widths = registry.getStringArray("listview.table.column.search.width");
//        String[] sorts = registry.getStringArray("listview.table.column.search.sort");
//        String[] types = registry.getStringArray("listview.table.column.search.type");

        String[] ids = {"line_code","line_rev","station_code","station_rev","item_id","released_rev","variant","meco_id","release_date","owner"};
        String[] names = {"Line","Line\nRev","Station","Station\nRev","WeldOperation","WeldOperation\nRev","Variant","MECO","Release\nDate","Owner"};
        String[] widths = {"60","70","60","70","187","70","200","120","100","70"};
        String[] sorts = {"true","true","true","true","true","false","true","true","true","true"};
        String[] types = {"1","1","1","1","1","2","1","1","1","1"};

        List<ColumnInfoModel> colModelList = new ArrayList<ColumnInfoModel>();
        for(int i = 0; i < ids.length; i++) {
            ColumnInfoModel colModel = new ColumnInfoModel();
            colModel.setColId(ids[i]);
            colModel.setColName(names[i]);
            colModel.setColumnWidth(Integer.parseInt(widths[i]));
            colModel.setSort(Boolean.parseBoolean(sorts[i]));
            colModel.setColType(Integer.parseInt(types[i]));
            colModel.setEditable(true);
            colModelList.add(colModel);
        }
        setColumnInfo(colModelList);
    }

    public void weldConditionSearchReset()
    {

    }

    public void weldConditionSheetPreview()
    {
        if(table.getSelectionCount() == 0){
            //MessageBox.post(new Throwable(), registry.getString("NoSelectOperation.Message"));
            MessageBox.post(new Throwable(), "No Select WeldOperation");
        }

        if(table.getSelection().length > 1){
            //MessageBox.post(new Throwable(), registry.getString("MultiSelectOperation.Message"));
            MessageBox.post(new Throwable(), "Multi Select WeldOperation");
        }

        TableItem tableItem = table.getSelection()[0];
        int dataMapIndex = (Integer) tableItem.getData("dataMapIndex");

        try {
            IDialog weldConditionDialog = null;
            weldConditionDialog = UIManager.getDialog(getShell(), "symc.dialog.WeldConditionSheetDialog");
            Map<String, Object> parameter = new HashMap<String, Object>();
            parameter.put("targetOperaion", dataList.get(dataMapIndex));
            weldConditionDialog.setParameters(parameter);
            weldConditionDialog.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked")
    public void weldConditionSheetOpen()
    {
        if(table.getSelectionCount() == 0) {
            //MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("NoSelectOperation.Message"), "Open", MessageBox.ERROR);
            MessageBox.post(UIManager.getCurrentDialog().getShell(), "No Select WeldOperation", "Open", MessageBox.ERROR);
            return;
        }

        if(table.getSelection().length > 1) {
            //MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("MultiSelectOperation.Message"), "Open", MessageBox.ERROR);
            MessageBox.post(UIManager.getCurrentDialog().getShell(), "Multi Select WeldOperation", "Open", MessageBox.ERROR);
            return;
        }

        TableItem tableItem = table.getSelection()[0];
        HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>) tableItem.getData("editors");
        TableEditor editor = editors.get(5);
        CCombo combo = (CCombo) editor.getEditor();
        String releasedRev = combo.getItem(combo.getSelectionIndex());
        String itemId = tableItem.getText(4);
        try {
            getReleasedRev(itemId, releasedRev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*if(editors == null) {
            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("NotPublished.Message"), "Open", MessageBox.ERROR);
            return;
        } else {
        }*/
    }

    private void getReleasedRev(String itemId, String itemRev) throws Exception {
        //itemId = registry.getString("ProcessSheetItemIDPrefix." + getConfigId()) + itemId;

        TCComponentItemRevision revision = CustomUtil.findItemRevision(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM_REV, itemId, itemRev);
        if(revision != null)
        {
            TCComponent component = revision.getRelatedComponent(SDVTypeConstant.WELD_CONDITION_SHEET_RELATION);
            if(component instanceof TCComponentDataset)
            {
                TCComponentDataset dataset = (TCComponentDataset) component;
                TCComponentTcFile[] files = dataset.getTcFiles();
                if(files != null && files.length > 0)
                {
                    String filePath = files[0].getFile(null).getAbsolutePath();
                    openExcelFile(filePath);
                }
            }
            else
            {
                MessageBox.post(UIManager.getCurrentDialog().getShell(), "Welding condition sheet does not exist", "Open", MessageBox.ERROR);
                return;
            }
        }
    }

    private void openExcelFile(String filePath) {
        Composite oleComposite = new Composite(this, SWT.NONE);
        oleComposite.setLayoutData(BorderLayout.CENTER);
        oleComposite.setLayout(new FillLayout());

        OleControlSite appControlSite = new OleControlSite(new OleFrame(oleComposite, SWT.NONE), SWT.NONE, "Excel.Application");
        appControlSite.doVerb(OLE.OLEIVERB_OPEN);

        OleAutomation application = new OleAutomation(appControlSite);
        application.setProperty(application.getIDsOfNames(new String[] {"Visible"})[0], new Variant(true));
        OleAutomation workbooks = application.getProperty(application.getIDsOfNames(new String[] {"Workbooks"})[0]).getAutomation();
        Variant varResult = workbooks.invoke(workbooks.getIDsOfNames(new String[] {"Open"})[0], new Variant[] {new Variant(filePath)});
        if(varResult != null) {
            System.out.println(" copy invoke result of BSHEET = " + varResult);
            varResult.dispose();
        } else {
            System.out.println("=====failed invoke copySheet method ====");
        }
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {
        this.dataMap = dataMap;

        if(dataMap.containsKey("actionId")) {
            String actionId = dataMap.getStringValue("actionId");
            if(actionId.startsWith("Search")) {
                List<HashMap<String, Object>> operationList = (List<HashMap<String, Object>>) dataMap.getTableValue("weldOperationList");
                //lblResultCount.setText(operationList.size() + registry.getString("SearchResultCountLabel.Message"));
                lblResultCount.setText(operationList.size() + "개의 공법이 검색 되었습니다.");
                setTableData(operationList);
            }
        } else {
            //throw new SDVRuntimeException(registry.getString("MissingActionId.Message"));
            throw new SDVRuntimeException("Missing actionId");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public IDataMap getLocalDataMap() {
        if(table.getSelectionCount() == 0) {
            //MessageBox.post(new Throwable(), registry.getString("NoSelectOperation.Message"));
            MessageBox.post(UIManager.getCurrentDialog().getShell(), "Please choose a WeldOperation", "Download", MessageBox.ERROR);
            throw new SDVRuntimeException("Missing select");
        }

        List<HashMap<String, Object>> weldOperationList = new ArrayList<HashMap<String,Object>>();
        List<String> weldOperationRevList = new ArrayList<String>();
        TableItem[] items = table.getSelection();

        for(int i = 0; i < items.length; i++)
        {
            // 선택한 combobox rev 추가로 데이터 가져오기 -S-
            TableItem tableItem = table.getSelection()[i];
            HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>) tableItem.getData("editors");
            TableEditor editor = editors.get(5);
            CCombo combo = (CCombo) editor.getEditor();
            String releasedRev = combo.getItem(combo.getSelectionIndex());
            // 선택한 combobox rev 추가로 데이터 가져오기 -E-
            int dataMapIndex = (Integer) items[i].getData("dataMapIndex");
            weldOperationList.add(dataList.get(dataMapIndex));
            weldOperationRevList.add(releasedRev);
        }

        dataMap.put("targetWeldOperationList", weldOperationList, IData.LIST_FIELD);
        dataMap.put("targetWeldOperationRevList", weldOperationRevList, IData.LIST_FIELD);
        dataMap.put("configId", getConfigId(), IData.INTEGER_FIELD);

        return dataMap;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return getLocalDataMap();
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        // 초기화
        return null;
    }

    @Override
    public void initalizeData(int result, IViewPane owner, IDataSet dataset) {
        // Operation 결과

    }

    @Override
    public void uiLoadCompleted() {
        //

    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }
}
