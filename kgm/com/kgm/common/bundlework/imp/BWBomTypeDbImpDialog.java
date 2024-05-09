package com.kgm.common.bundlework.imp;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.common.bundlework.bwutil.BWItemData;
import com.kgm.common.bundlework.bwutil.BWItemModel;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

@SuppressWarnings("unused")
public class BWBomTypeDbImpDialog extends BWPartBOMImpDialog {    

    String[] szClass = null;
    String[] szAttr = null;
    String[] szDbColumn = null;
    String[][] szLovNames = null;

    /* Load Button */
    public Button loadButton;
    public Combo comboSeqType;
    private String seqType;

    public BWBomTypeDbImpDialog(Shell parent, int style) {
        super(parent, style);
    }
    
    @Override
    public void dialogOpen() {
        // ITEM 관련 옵션
        this.bwOption.setItemCreatable(false);
        // BOM 관련 옵션
        this.bwOption.setBOMAvailable(true);
        this.bwOption.setBOMLineModifiable(false);
        this.bwOption.setBOMRearrange(true);
        // UI
        this.excelFileGroup.setBounds(10, 10, 769, 60);
        Label lblSeqType = new Label(excelFileGroup, SWT.NONE);
        lblSeqType.setText("Seq Type");
        lblSeqType.setBounds(20, 26, 60, 22);
        this.comboSeqType = new Combo(excelFileGroup, SWT.READ_ONLY);
        this.comboSeqType.setItems(new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" });
        this.comboSeqType.select(0);
        this.comboSeqType.setBounds(80, 26, 77, 22);
        this.loadButton = new Button(excelFileGroup, SWT.NONE);
        this.loadButton.setText("Load");
        this.loadButton.setBounds(190, 26, 77, 22);
        this.logGroup.setBounds(10, 75, 863, 481);
        this.tree.setBounds(10, 22, 843, 300);
        this.text.setBounds(10, 330, 843, 141);
        this.executeButton.setBounds(338, 576, 77, 22);
        this.cancelButton.setBounds(459, 576, 77, 22);
        this.viewLogButton.setBounds(750, 576, 120, 22);
        this.shell.open();
        this.shell.layout();
        // super.enableOptionButton();
        // Load Button Listener
        this.loadButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                new Job("loadData") {
                    @Override
                    protected IStatus run(IProgressMonitor arg0) {
                        // Item Sheet Data Loading
                        try {
                            load();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Status.OK_STATUS;
                    }
                }.schedule();

            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    @Override
    public void load() throws Exception {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    // Load 전처리
                    loadPre();
                    // ManualTreeItem List
                    itemList = new ArrayList<ManualTreeItem>();
                    // TCComponentItemRevision List
                    tcItemRevSet = new HashMap<String, TCComponentItemRevision>();
                    shell.setCursor(waitCursor);
                    tree.removeAll();
                    // Header 정보 Loading
                    loadHeader();
                    // Item Sheet Data Loading
                    loadData();
                    if (validate()) {
                        executeButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.post(shell, e.getMessage(), "Notification", 2);
                } finally {
                    // Load 후처리
                    try {
                        loadPost();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        shell.setCursor(arrowCursor);
                    }
                }
            }
        });
    }

    /**
     * 헤더 생성
     * 
     * @method loadHeader
     * @date 2013. 2. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void loadHeader() throws Exception {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name" };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "NAME" };
        this.headerModel = new BWItemModel();
        // Type 설정 (BOM)
        this.strTargetItemType = "S7_Product";
        for (int i = 0; i < szClass.length; i++) {
            this.headerModel.setModelData(szClass[i], szAttr[i], new Integer(i));
        }
    }

    /**
     * DB 데이터를 Load한다.
     * 
     * @method loadData
     * @date 2013. 2. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void loadData() {
        ArrayList<HashMap> rows = null;
        // SYMCRemoteUtil remote = new SYMCRemoteUtil(REMOTE_URL);
        // try {
        // DataSet ds = new DataSet();
        // ds.put("seqType", seqType);
        // rows = (ArrayList<HashMap>)
        // remote.execute("com.kgm.service.MigrationService",
        // "getBOMValidationList", ds);
        // } catch (ConnectException ce) {
        // ce.printStackTrace();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        rows = new ArrayList<HashMap>();
        HashMap data1 = new HashMap();
        data1.put("LEVEL", "0");
        data1.put("ITEM_ID", "000351");
        data1.put("REVISION_ID", "000");
        data1.put("NAME", "A-ROOT");
        data1.put("EXIST_ITEM", "Y");
        rows.add(data1);
        HashMap data2 = new HashMap();
        data2.put("LEVEL", "1");
        data2.put("ITEM_ID", "000352");
        data2.put("REVISION_ID", "000");
        data2.put("NAME", "B-SUB-1");
        data2.put("EXIST_ITEM", "Y");
        rows.add(data2);
        HashMap data3 = new HashMap();
        data3.put("LEVEL", "1");
        data3.put("ITEM_ID", "000353");
        data3.put("REVISION_ID", "000");
        data3.put("NAME", "B-SUB-2");
        data3.put("EXIST_ITEM", "Y");
        rows.add(data3);

        for (int r = 0; r < rows.size(); r++) {
            HashMap rMap = rows.get(r);
            // String[] szKey = rMap.keySet().toArray(new String[rMap.size()]);
            BWItemData stcItemData = new BWItemData();
            for (int i = 0; i < szAttr.length; i++) {
                String strItemName = szClass[i];
                String strAttrName = szAttr[i];
                if (!"".equals(StringUtil.nullToString(strAttrName))) {
                    String strAttrValue = getRowData(rMap, szDbColumn[i]);
                    stcItemData.setItemData(strItemName, strAttrName, strAttrValue);
                }
            }
            String strItemID = stcItemData.getItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID);
            String strRevID = stcItemData.getItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID);
            // Excel Item Row가 저장될 TreeItem Object
            ManualTreeItem treeItem = null;
            int nLevel = super.getIntValue(stcItemData.getItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_LEVEL));
            // Level 0 값은 TopPart
            if (nLevel == 0) {
                // Top TreeItem
                treeItem = new ManualTreeItem(this.tree, this.tree.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strItemID);
                treeItem.setLevel(nLevel);
            } else if (nLevel > 0) {
                // Child TreeItem
                ManualTreeItem parentTreeItem = super.getParentTreeItem(nLevel);
                if (parentTreeItem != null) {
                    treeItem = new ManualTreeItem(parentTreeItem, parentTreeItem.getItems().length, ManualTreeItem.ITEM_TYPE_TCITEM, strItemID);
                    treeItem.setLevel(nLevel);
                } else {
                    treeItem = new ManualTreeItem(this.tree, SWT.NONE, ManualTreeItem.ITEM_TYPE_TCITEM, strItemID);
                    treeItem.setLevel(0);
                    treeItem.setStatus(STATUS_ERROR, super.getTextBundle("ParentNotExist", "MSG", super.dlgClass));
                }
            }
            treeItem.setBWItemData(stcItemData);
            setTreeItemData(treeItem, CLASS_TYPE_BOMLINE);
            setTreeItemData(treeItem, CLASS_TYPE_ITEM);
            setTreeItemData(treeItem, CLASS_TYPE_REVISION);
            // ItemID 공백을 허용하지 않는 데 공백인 경우
            if (treeItem != null && !this.bwOption.isItemIDBlankable() && strItemID.trim().equals("")) {
                treeItem.setStatus(STATUS_ERROR, this.getTextBundle("ItemIDRequired", "MSG", dlgClass));
            }
            // ItemID 공백을 허용, 공백인 경우
            else if (treeItem != null && this.bwOption.isItemIDBlankable() && strItemID.trim().equals("")) {
                treeItem.setBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID, NEW_ITEM_ID);
                treeItem.setText(TREEITEM_COMLUMN_ITEMID, NEW_ITEM_ID);
            }
            // Revision 생성 Option이 아님에도 Revision값이 없는 경우 Error 처리
            if (treeItem != null && !this.bwOption.isRevCreatable() && strRevID.equals("")) {
                treeItem.setStatus(STATUS_ERROR, this.getTextBundle("RevisionIDRequired", "MSG", dlgClass));
            } else if (treeItem != null && strRevID.equals("")) {
                treeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID, NEW_ITEM_REV);
                treeItem.setText(TREEITEM_COMLUMN_REVISION, NEW_ITEM_REV);
            }
            // Revision ID 유효성 Check
            if (strRevID.equals("")) {
                treeItem.setStatus(STATUS_ERROR, this.getTextBundle("RevisionInvalid", "MSG", dlgClass));
            }
            // Item 존재 여부 체크
            if (treeItem != null && !"Y".equals((String) rMap.get("EXIST_ITEM"))) {
                treeItem.setStatus(STATUS_ERROR, "Item does not exist.");
            }
            this.itemList.add(treeItem);
        }
    }

    /**
     * 
     * 
     * @method getData
     * @date 2013. 2. 20.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    private String getRowData(HashMap rowData, String key) {
        if (!rowData.containsKey(key) && rowData.get(key) == null) {
            return "";
        } else {
            String rtData = StringUtil.nullToString(rowData.get(key).toString());
            // "." 데이터는 blank 처리한다.
            if (".".equals(rtData)) {
                rtData = "";
            }
            return rowData.get(key).toString();
        }
    }

    /**
     * 실행 후 DB에 상태
     */
    @Override
    public void executePost() throws Exception {
        this.updateMigrationStatus();
    }

    /**
     * BOM Migration DB Status(MIG_FLAG)를 변경한다.
     * 
     * @method updateMigrationStatus
     * @date 2013. 2. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void updateMigrationStatus() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                ArrayList<HashMap<String, String>> tableList = getTableStructure(tree.getItems());
                SYMCRemoteUtil remote = new SYMCRemoteUtil();
                try {
                    DataSet ds = new DataSet();
                    ds.put("updateDataList", tableList);
                    remote.execute("com.kgm.service.MigrationService", "updateMigrationBOMStatus", ds);
                } catch (ConnectException ce) {
                    ce.printStackTrace();
                    // throw new
                    // MigrationException("Migration Data DB connect Error occurred!");
                } catch (Exception e) {
                    e.printStackTrace();
                    // throw new
                    // MigrationException("Migration Data DB import Error occurred!");
                }
            }
        });
    }

    private ArrayList<HashMap<String, String>> getTableStructure(TreeItem[] szTopItems) {
        ArrayList<HashMap<String, String>> tableList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < szTopItems.length; i++) {
            // Top TreeItem
            ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
            ArrayList<HashMap<String, String>> topList = this.getTableStructure(topTreeItem, null);
            tableList.addAll(topList);
        }
        return tableList;
    }

    private ArrayList<HashMap<String, String>> getTableStructure(ManualTreeItem treeItem, ArrayList<HashMap<String, String>> itemList) {
        if (itemList == null) {
            itemList = new ArrayList<HashMap<String, String>>();
        }
        HashMap<String, String> data = new HashMap<String, String>();
        if (STATUS_ERROR == treeItem.getStatus()) {
            data.put("MIG_FLAG", "F");
        } else {
            data.put("MIG_FLAG", "C");
        }
        if ("Item".equals(treeItem.getItemType())) {
            data.put("ITEM_ID", treeItem.getItemID());
            data.put("NAME", treeItem.getBWItemAttrValue("Item", "object_name"));
        } else {

        }
        itemList.add(data);
        // Child TreeItem
        ArrayList<ManualTreeItem> childItemList = new ArrayList<ManualTreeItem>();
        super.syncGetChildItem(treeItem, childItemList);
        for (int i = 0; i < childItemList.size(); i++) {
            ManualTreeItem childItem = childItemList.get(i);
            this.getTableStructure(childItem, itemList);
        }
        return itemList;
    }

}
