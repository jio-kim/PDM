/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.SimpleSDVDialog;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.ui.view.table.model.ColumnInfoModel;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.StringUtil;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.sdv.operation.resource.ExportExcelOperation;
import com.symc.plm.me.sdv.operation.resource.SearchSubsidiaryInitOperation;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.sdv.service.resource.SDVTableViewer;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [20140430][SR140507-042] shcho, 검색 결과 Excel로 내보내기 기능 추가
 * 
 * @author shcho
 * 
 */
public class SearchSubsidiaryViewPane extends AbstractSDVViewPane {
    private String viewId;
    private Registry registry;

    private SDVText partNoText;
    private SDVText partNameText;
    private SDVText oldPartNoText;
    private SDVText subsidiaryGroupText;
    private Table resultTable;
    private LinkedHashMap<String, String> subsidiaryPropMap;
    private SDVTableViewer tableViewer;
    private Button createButton;
    private Label resultCountLabel;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public SearchSubsidiaryViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
        this.viewId = id;
    }

    @Override
    protected void initUI(final Composite parent) {
        registry = Registry.getRegistry(this);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FormLayout());
        // 자재 = Part No.(ID), 자재내역 = Part Name, 기존자재번호=Old Part No., 자재그룹 = Subsidiary Group
        Label lblPartNo = new Label(composite, SWT.NONE);
        FormData fdPartNo = new FormData();
        fdPartNo.top = new FormAttachment(0, 20);
        fdPartNo.left = new FormAttachment(0, 5);
        lblPartNo.setLayoutData(fdPartNo);
        lblPartNo.setText(registry.getString("Subsidiary.PartNo.NAME"));

        partNoText = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        FormData fdPartNoText = new FormData();
        fdPartNoText.top = new FormAttachment(lblPartNo, 0, SWT.CENTER);
        fdPartNoText.left = new FormAttachment(lblPartNo, 5, SWT.RIGHT);
        fdPartNoText.width = 90;
        partNoText.setLayoutData(fdPartNoText);
        ResourceUtilities.setSDVTextListener(partNoText, true, false, null);
        textEnterKeyListener(partNoText);

        Label lblpartName = new Label(composite, SWT.NONE);
        FormData fdpartName = new FormData();
        fdpartName.top = new FormAttachment(partNoText, 0, SWT.CENTER);
        fdpartName.left = new FormAttachment(partNoText, 20, SWT.RIGHT);
        lblpartName.setLayoutData(fdpartName);
        lblpartName.setText(registry.getString("Subsidiary.PartName.NAME"));

        partNameText = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        FormData fdPartNameText = new FormData();
        fdPartNameText.top = new FormAttachment(lblpartName, 0, SWT.CENTER);
        fdPartNameText.left = new FormAttachment(lblpartName, 5, SWT.RIGHT);
        fdPartNameText.width = 110;
        partNameText.setLayoutData(fdPartNameText);
        ResourceUtilities.setSDVTextListener(partNameText, true, false, null);
        textEnterKeyListener(partNameText);

        Label lbloldPartNo = new Label(composite, SWT.NONE);
        FormData fboldPartNo = new FormData();
        fboldPartNo.top = new FormAttachment(partNameText, 0, SWT.CENTER);
        fboldPartNo.left = new FormAttachment(partNameText, 20, SWT.RIGHT);
        lbloldPartNo.setLayoutData(fboldPartNo);
        lbloldPartNo.setText(registry.getString("Subsidiary.OldPartNo.NAME"));

        oldPartNoText = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        FormData fdOldPartNoText = new FormData();
        fdOldPartNoText.top = new FormAttachment(lbloldPartNo, 0, SWT.CENTER);
        fdOldPartNoText.left = new FormAttachment(lbloldPartNo, 5, SWT.RIGHT);
        fdOldPartNoText.width = 90;
        oldPartNoText.setLayoutData(fdOldPartNoText);
        ResourceUtilities.setSDVTextListener(oldPartNoText, true, false, null);
        textEnterKeyListener(oldPartNoText);

        Label lbsubsidiaryGroup = new Label(composite, SWT.NONE);
        FormData fblbsubsidiaryGroup = new FormData();
        fblbsubsidiaryGroup.top = new FormAttachment(oldPartNoText, 0, SWT.CENTER);
        fblbsubsidiaryGroup.left = new FormAttachment(oldPartNoText, 20, SWT.RIGHT);
        lbsubsidiaryGroup.setLayoutData(fblbsubsidiaryGroup);
        lbsubsidiaryGroup.setText(registry.getString("Subsidiary.Group.NAME"));

        subsidiaryGroupText = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        FormData fdsubsidiaryGroupText = new FormData();
        fdsubsidiaryGroupText.top = new FormAttachment(lbsubsidiaryGroup, 0, SWT.CENTER);
        fdsubsidiaryGroupText.left = new FormAttachment(lbsubsidiaryGroup, 5, SWT.RIGHT);
        fdsubsidiaryGroupText.width = 50;
        subsidiaryGroupText.setLayoutData(fdsubsidiaryGroupText);
        ResourceUtilities.setSDVTextListener(subsidiaryGroupText, true, false, null);
        textEnterKeyListener(subsidiaryGroupText);

        tableViewer = new SDVTableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
        resultTable = tableViewer.getTable();
        FormData fdTable = new FormData();
        fdTable.top = new FormAttachment(lblPartNo, 20, SWT.BOTTOM);
        fdTable.left = new FormAttachment(0);
        fdTable.right = new FormAttachment(100);
        fdTable.bottom = new FormAttachment(100, -20);
        resultTable.setLayoutData(fdTable);

        // 테이블 클릭시 Create 버튼 옵션(활성/비활성) 지정
        resultTable.addSelectionListener(new SelectionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((HashMap<String, String>) e.item.getData()).get("tc_item_id") != null) {
                    createButton.setEnabled(false);
                } else {
                    createButton.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        resultCountLabel = new Label(composite, SWT.NONE);
        FormData fbresultCount = new FormData();
        fbresultCount.bottom = new FormAttachment(100);
        fbresultCount.right = new FormAttachment(100, -5);
        fbresultCount.width = 50;
        resultCountLabel.setLayoutData(fbresultCount);
        resultCountLabel.setAlignment(SWT.RIGHT);
        resultCountLabel.setText("0/0");
    }

    /**
     * 
     * @method textEnterKeyListener
     * @param sdvText
     * @return void
     * @see
     */
    private void textEnterKeyListener(SDVText sdvText) {
        sdvText.addVerifyKeyListener(new VerifyKeyListener() {
            @Override
            public void verifyKey(VerifyEvent event) {
                if (event.keyCode == 13) {
                    event.doit = false;
                    resultTable.removeAll();
                    searchSTEPS();
                }
            }
        });
    }

    /**
     * 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void searchSTEPS() {
        // 이전 검색 결과 지우기
        resultTable.removeAll();

        ArrayList<HashMap<String, String>> searchResult = null;
        // SYMCRemoteUtil remote = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
        SYMCRemoteUtil remote = new SYMCRemoteUtil();

        String partNo = partNoText.getText();
        String partName = partNameText.getText();
        String oldPartNo = oldPartNoText.getText();
        String subsidiaryGroup = subsidiaryGroupText.getText();

        if ((partNo == null || partNo.equals("") || partNo.equals("*")) && (partName == null || partName.equals("") || partName.equals("*")) && (oldPartNo == null || oldPartNo.equals("") || oldPartNo.equals("*")) && (subsidiaryGroup == null || subsidiaryGroup.equals("") || subsidiaryGroup.equals("*"))) {
            MessageBox.post(getShell(), "Please enter one or more search keyword.", "WARNING", MessageBox.WARNING);
            return;
        }

        try {
            DataSet ds = new DataSet();
            // -------------------------------------------------------------------
            //[20140509][SR140507-042] shcho, 검색 결과 Excel로 내보내기 기능 추가하면서 검색 조건 변경 (멀티검색조건추가)
            if (partNo.contains(";")) {
                String partNoSearch = makeInCondition(partNo);
                ds.put("PART_NO", partNoSearch);
                ds.put("PART_NO_MULTI", "Y");
            } else {
                ds.put("PART_NO", partNo.replaceAll("\\*", "%"));
                ds.put("PART_NO_MULTI", "N");
            }

            if (partName.contains(";")) {
                String partNoSearch = makeInCondition(partName);
                ds.put("PART_NAME", partNoSearch);
                ds.put("PART_NAME_MULTI", "Y");
            } else {
                ds.put("PART_NAME", partName.replaceAll("\\*", "%"));
                ds.put("PART_NAME_MULTI", "N");
            }

            if (oldPartNo.contains(";")) {
                String partNoSearch = makeInCondition(oldPartNo);
                ds.put("OLD_PART_NO", partNoSearch);
                ds.put("OLD_PART_NO_MULTI", "Y");
            } else {
                ds.put("OLD_PART_NO", oldPartNo.replaceAll("\\*", "%"));
                ds.put("OLD_PART_NO_MULTI", "N");
            }

            if (subsidiaryGroup.contains(";")) {
                String partNoSearch = makeInCondition(subsidiaryGroup);
                ds.put("SUBSIDIARY_GROUP", partNoSearch);
                ds.put("SUBSIDIARY_GROUP_MULTI", "Y");
            } else {
                ds.put("SUBSIDIARY_GROUP", subsidiaryGroup.replaceAll("\\*", "%"));
                ds.put("SUBSIDIARY_GROUP_MULTI", "N");
            }

            // ds.put("PART_NO", partNo.replaceAll("\\*", "%"));
            // ds.put("PART_NAME", partName.replaceAll("\\*", "%"));
            // ds.put("OLD_PART_NO", oldPartNo.replaceAll("\\*", "%"));
            // ds.put("SUBSIDIARY_GROUP", subsidiaryGroup.replaceAll("\\*", "%"));
            // -------------------------------------------------------------------
            
            searchResult = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.SYMCSubsidiaryService", "serchSubsidiary", ds);

            Iterator<HashMap<String, String>> iterator = searchResult.iterator();
            HashMap<String, String>[] arrHashMap = new HashMap[searchResult.size()];
            int rowNum = 0;
            while (iterator.hasNext()) {
                HashMap<String, String> targetRow = iterator.next();
                arrHashMap[rowNum] = targetRow;
                rowNum++;
            }

            if (rowNum == 0) {
                MessageBox.post(getShell(), "No Search Result.", "INFORMATION", MessageBox.INFORMATION);
                return;
            }

            tableViewer.setInput(arrHashMap);

            // Teamcenter에 이미 생성된 부자재 Row는 회색으로 표시
            int rowCount = resultTable.getItemCount();
            int createdItemCount = 0;
            for (int i = 0; i < rowCount; i++) {
                final TableItem tableItem = resultTable.getItem(i);
                HashMap tableItemMap = (HashMap) tableItem.getData();

                if (tableItemMap.get("tc_item_id") != null) {
                    createdItemCount++;
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            tableItem.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
                        }
                    });
                }
            }

            // RowCount 표기
            resultCountLabel.setText(createdItemCount + "/" + rowCount);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 입력받은 검색조건을 'aaa','bbb','ccc' 와 같은 형식으로 변경 (SQL 조건에 사용하는 형식)
     * 
     * [20140509][SR140507-042] shcho, 함수신규생성
     * 
     * @param searchCondition
     * @return
     */
    public String makeInCondition(String searchCondition) {
        String[] serachConditionList = searchCondition.split(";");
        String partSearch = "";
        for (int i = 0; i < serachConditionList.length; i++) {
            if (StringUtil.nullToString(serachConditionList[i]).equals("")) {
                continue;
            }
            if ("".equals(partSearch)) {
                partSearch += "'" + serachConditionList[i] + "'";
            } else {
                partSearch += ",'" + serachConditionList[i] + "'";
            }
        }
        return partSearch;
    }

    /**
     * 
     * @method openCreateDialog
     * @date 2013. 12. 26.
     * @author CS.Park
     * @param
     * @return void
     * @throws
     * @see
     */
    protected void openCreateDialog() {
        Display.getDefault().syncExec(new Runnable() {
            SimpleSDVDialog createDialog = null;
            RawDataMap targetDataMap = null;

            public void run() {
                try {
                    targetDataMap = (RawDataMap) getLocalSelectDataMap();
                    if (targetDataMap == null || targetDataMap.size() <= 0) {
                        throw new Exception(registry.getString("Select.Check.MSG"));
                    }

                    Map<String, Object> paramKeyMap = new HashMap<String, Object>();
                    paramKeyMap.put("paramKey", targetDataMap);
                    paramKeyMap.put("parentDialog", UIManager.getCurrentDialog().getId());

                    createDialog = (SimpleSDVDialog) UIManager.getDialog(AIFUtility.getActiveDesktop().getShell(), "symc.me.resource.CreateSubsidiaryDialog");
                    createDialog.setParameters(paramKeyMap);
                    int result = createDialog.open();
                    if (result == SimpleSDVDialog.DIALOG_RETURNCODE_OK) {
                        searchSTEPS();
                    }

                } catch (Exception e) {
                    MessageBox.post(getShell(), e.getMessage(), "Warning", MessageBox.WARNING);
                    e.printStackTrace();
                    return;
                }
            }
        });
    }

    /**
     * 검색 결과 Excel로 내보내기
     * 
     *[20140430][SR140507-042] shcho, 함수신규생성
     * 
     * @method exportExcel
     * @return void
     * @throws
     */
    public void exportExcel() {
        ExportExcelOperation exportExcelOperation = new ExportExcelOperation(getTableAllRowValues(), "O", 0);
        try {
            exportExcelOperation.executeOperation();
        } catch (Exception e) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "ERROR", MessageBox.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * TCTable에 표시된 모든 값을 return 하는 함수
     * [20140430][SR140507-042] shcho, 함수신규생성
     * 
     * @method getTableAllRowValues
     * @return StringBuilder
     */
    public List<List<String>> getTableAllRowValues() {
        try {

            List<List<String>> arrListRowValues = new ArrayList<List<String>>();
            List<String> arrListHeadColumnValues = new ArrayList<String>();

            int rowCount = resultTable.getItemCount();
            int columnCount = resultTable.getColumnCount();

            // Head Column 값
            arrListHeadColumnValues.add("TC");
            for (int j = 0; j < columnCount; j++) {
                String headValue = resultTable.getColumn(j).getText();
                arrListHeadColumnValues.add(headValue);
            }
            arrListRowValues.add(arrListHeadColumnValues);

            // Row 값 담기
            for (int i = 0; i < rowCount; i++) {
                // Column List 초기화
                ArrayList<String> arrListColumnValues = new ArrayList<String>();

                TableItem tableItem = resultTable.getItem(i);

                // TC에 존재하는 Item은 O,X 값 추가 (Table에는 없는 값이지만 필요하여 추가함)
                @SuppressWarnings("unchecked")
                HashMap<String, String> tableItemMap = (HashMap<String, String>) tableItem.getData();
                String tcItemID = StringUtil.nullToString(tableItemMap.get("tc_item_id"));

                // Column 값 담기
                arrListColumnValues.add(tcItemID.length() > 0 ? "O" : "X");
                for (int j = 0; j < columnCount; j++) {
                    String columnValue = tableItem.getText(j);
                    arrListColumnValues.add(columnValue);
                }

                arrListRowValues.add(arrListColumnValues);
            }

            System.out.println(arrListRowValues.size());

            return arrListRowValues;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {
        searchSTEPS();
    }

    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IDataMap getLocalSelectDataMap() {
        RawDataMap rawDataMap = new RawDataMap();
        TableItem[] arrTableItem = resultTable.getSelection();
        HashMap<String, String> rowData = null;
        for (TableItem tableItem : arrTableItem) {
            rowData = (HashMap<String, String>) tableItem.getData();
            for (String key : rowData.keySet()) {
                rawDataMap.put(key, rowData.get(key).toString());
            }
        }

        return rawDataMap;
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        SearchSubsidiaryInitOperation initOperation = new SearchSubsidiaryInitOperation(viewId);
        return initOperation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        // String viewName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset.containsMap(viewId)) {
                // setLocalDataMap(dataset.getDataMap(viewId));

                // IData targetData = dataset.getData("subsidiaryPropMap");
                // subsidiaryPropMap = (LinkedHashMap<String, String>) targetData.getValue();

                // 테이블의 컬럼에 보여줄 속성 리스트를 가져와 컬럼을 세팅해준다.
                subsidiaryPropMap = (LinkedHashMap<String, String>) dataset.getData("subsidiaryPropMap");
                if (subsidiaryPropMap != null) {
                    ArrayList<ColumnInfoModel> arrColumnModel = new ArrayList<ColumnInfoModel>();

                    for (String key : subsidiaryPropMap.keySet()) {
                        String columnName = subsidiaryPropMap.get(key);

                        ColumnInfoModel columnModel = new ColumnInfoModel();
                        columnModel.setColId(key);
                        columnModel.setColName(columnName);
                        columnModel.setColumnWidth(100);
                        columnModel.setSort(true);

                        arrColumnModel.add(columnModel);
                    }

                    tableViewer.createColModel(arrColumnModel);
                }
            }
        }
    }

    @Override
    public void uiLoadCompleted() {
        IButtonInfo buttonInfo = this.getActionToolButtons().get("Create");
        createButton = buttonInfo.getButton();
        createButton.setEnabled(false);
    }
}
