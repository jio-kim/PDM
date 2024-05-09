/*
 * �ۼ��� : 2010. 07. 19. File
 * Name : BWXLSImpDialog.java
 * 
 * �ϰ� Upload Abstract Class
 * �ϰ� Upload���� �۾��� ���������� ���� �� ���� Class�� ��ӹ޾Ƽ� ����ؾ� ��.
 * 
 * Excel �߿� Data ��ġ Setting
 * Option�� ���� ��� ����(���� Ŭ�������� �۾��� �°� �����ؾ� ��)
 * Option ���� (dialogs_locale_ko_KR.properties�� ����)
 * ########## Bundle Work Option Start ############
 * # Item ���� ����
 * BWXLSImpDialog.opt.isItemCreatable = true
 * # Item ���� ����
 * BWXLSImpDialog.opt.isItemModifiable = true
 * 
 * # Item Revision ���� ����
 * BWXLSImpDialog.opt.isRevCreatable = true
 * # Item Revision ���� ����
 * BWXLSImpDialog.opt.isRevModifiable = true
 * 
 * # DataSet ��� ����
 * BWXLSImpDialog.opt.isDSAvailable = true
 * # DataSet ���� �� ���� ����(��ü)
 * BWXLSImpDialog.opt.isDSChangable = true
 * 
 * 
 * # BOM ��� ����
 * BWXLSImpDialog.opt.isBOMAvailable = true
 * # BOM �籸�� ����(BOM Structure ���� �� �籸��)
 * BWXLSImpDialog.opt.isBOMRearrange = false
 * # BOM Line �Ӽ� ���� ����
 * BWXLSImpDialog.opt.isBOMLineModifiable = true
 * ########## Bundle Work Option End ############
 */

package com.symc.plm.me.sdv.dialog.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.commands.document.TechDocMasterOperation;
import com.kgm.common.bundlework.BWImportImpl;
import com.kgm.common.bundlework.BundleWorkDialog;
import com.kgm.common.bundlework.bwutil.BWItemData;
import com.kgm.common.bundlework.bwutil.BWItemModel;
import com.kgm.common.bundlework.bwutil.BWUOption;
import com.kgm.common.bundlework.bwutil.BWUOptionDialog;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DateUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.common.utils.TcDefinition;
import com.kgm.common.utils.TxtReportFactory;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.sdv.service.resource.service.create.ClassifyService;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.SYMCBOMWindow;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;



/**
 * [SR150209-019][20150402] shcho, 1) ��Ͻ� �ڵ� Release�ǵ��� ����. 2) �̹� ��ϵǾ� Release �� ��� Revise �Ͽ� ����ϵ��� ��� �߰�. 
 * 
 */
public abstract class BWXLSImpDialog extends BundleWorkDialog implements BWImportImpl {
    /* Item Sheet ��ȿ Data End Position�� ��ġ�� Column Index */
    public static final int ITEM_DATA_END_X_POS = 0;

    /* Item Type Value���� ��ġ�� Column Index */
    public static final int ITEM_TYPE_X_POS = 2;
    /* Item Type Value���� ��ġ�� Row Index */
    public static final int ITEM_TYPE_Y_POS = 0;

    /* Item Header Model�� ��ġ�� Excel Column Position */
    public static final int ITEM_HEADER_START_X_POS = 1;
    /* Item Header Model�� ��ġ�� Excel Row Position */
    public static final int ITEM_HEADER_START_Y_POS = 2;

    /* Item Sheet ��ȿ Data Column Index */
    public static final int ITEM_START_X_POS = 1;
    /* Item Sheet ��ȿ Data Row Index */
    public static final int ITEM_START_Y_POS = 5;

    /* Item Sheet Data ����(END) ���� ��ġ�� Column Index */
    public static final int ITEM_END_X_POS = 0;

    /* Excel Data ���� ������ */
    public static final String ITEM_END_VALUE = "End";

    /* Item Sheet Level Column Index */
    public static final int ITEM_XLS_COLUMN_INDEX_LEVEL = 1;

    /* TreeItem ItemID Column Index */
    protected static final int TREEITEM_COMLUMN_ITEMID = 0;
    /* TreeItem Revision Column Index */
    protected static final int TREEITEM_COMLUMN_REVISION = 1;
    /* TreeItem PartName Column Index */
    static final int TREEITEM_COMLUMN_PARTNAME = 2;

    /* Excel Level Attr Name */
    public static final String ITEM_ATTR_LEVEL = "Level";
    /* Excel ItemID Attr Name */
    public static final String ITEM_ATTR_ITEMID = "item_id";
    /* Excel RevisionID Attr Name */
    public static final String ITEM_ATTR_REVISIONID = "item_revision_id";
    /* Excel ItemName Attr Name */
    public static final String ITEM_ATTR_ITEMNAME = "object_name";
    public static final String ITEM_ATTR_ITEMTYPE = "object_type";

    /* Excel ClassID Attr Name */
    public static final String CLASSIFICATION_ATTR_ID = "class_id";

    /* Excel Sequence No Attr Name */
    public static final String BOMLINE_ATTR_SEQUENCENO = "bl_sequence_no";

    /* Excel Dataset Type Attr Name */
    public static final String DATASET_ATTR_TYPE = "dataset_type";
    /* Excel Dataset Name Attr Name */
    public static final String DATASET_ATTR_NAME = "object_name";

    public static final String DATASET_ATTR_FILEPATH = "attach_file_path";

    public static final String DATASET_ATTR_V4TYPE = "attach_v4_type";
    public static final String DATASET_ATTR_CAD_PART_REV = "attach_cad_part_rev";

    public static final String NEW_ITEM_ID = "NEW ID";
    public static final String NEW_ITEM_REV = "000";

    // BWItemData ����� Skip�� Attr List
    public static final String[] szSkipAttrs = { ITEM_ATTR_LEVEL, ITEM_ATTR_ITEMID, ITEM_ATTR_REVISIONID, ITEM_ATTR_ITEMNAME, DATASET_ATTR_TYPE, ITEM_ATTR_ITEMTYPE, "dynamic_qty", DATASET_ATTR_FILEPATH, DATASET_ATTR_V4TYPE, DATASET_ATTR_CAD_PART_REV, "bl_variant_condition", "old_item_id", "old_item_revision_id", "s7_ACT_WEIGHT", "s7_BOUNDINGBOX", "uom_tag", "dataset_revision_id" };
    public TCSession session;

    // �ϰ� Upload Option ���� Ŭ����
    public BWUOption bwOption;

    public ArrayList<TCComponentBOMWindow> bomWindowList;

    // TreeItem ���� ArrayList
    public ArrayList<ManualTreeItem> itemList;

    // Import�� TCComponentItemRevision ���� HashMap
    public HashMap<String, TCComponentItemRevision> tcItemRevSet;

    /* Excel�� ��õ� Item�� �Ӽ� Model */
    public BWItemModel headerModel;
    /* Excel�� ���ǵ� Item Type Name */
    public String strTargetItemType;

    /* Template Dataset Name */
    public String strTemplateDSName;

    // Excel ��ȿ Coumn Count
    public int nValidColumnCount = 0;

    public int nWraningCount = 0;

    // BOM Line �ߺ��� �������� ����
    private HashMap<String, Object> dupBomLineMap;

    public BWXLSImpDialog(Shell parent, int style, Class<?> cls) {
        super(parent, style, cls);

        this.session = (TCSession) AifrcpPlugin.getSessionService().getActivePerspectiveSession();
        this.dupBomLineMap = new HashMap<String, Object>();
        this.bomWindowList = new ArrayList<TCComponentBOMWindow>();

        this.createBundleWorkOption();

    }

    public BWXLSImpDialog(Shell parent, int style) {
        this(parent, style, BWXLSImpDialog.class);
    }

    /**
     * �ϰ� Upload Option Setting
     */
    public void createBundleWorkOption() {
        String strMiddleOptName = "opt";

        this.bwOption = new BWUOption();

        try {

            // Upload ������ Dataset Type List(������ => ',')
            String[] szUpLoadableDSType = this.getTextBundleArray("UploadableDatasetList", strMiddleOptName, this.dlgClass);

            // Upload ������ Dataset Type�� Ȯ���� List(������ => ',') , ������ �������� ��� ������ => '/'
            String[] szUpLoadableDSFileExt = this.getTextBundleArray("UploadableDatasetFileExtList", strMiddleOptName, this.dlgClass);

            if (szUpLoadableDSType.length == szUpLoadableDSFileExt.length) {
                for (int i = 0; i < szUpLoadableDSType.length; i++) {
                    String[] szExt = BundleWorkDialog.getSplitString(szUpLoadableDSFileExt[i], "/");
                    this.bwOption.setDataRefExt(szUpLoadableDSType[i], szExt);
                }
            } else {
                throw new Exception();
            }

            // Item ���� ����
            this.bwOption.setItemIDBlankable(Boolean.parseBoolean(super.getTextBundle("isItemIDBlankable", strMiddleOptName, dlgClass)));
            // Item ���� ����
            this.bwOption.setAutoCADValidatable(Boolean.parseBoolean(super.getTextBundle("isAutoCADValidatable", strMiddleOptName, dlgClass)));

            // Item ���� ����
            this.bwOption.setItemCreatable(Boolean.parseBoolean(super.getTextBundle("isItemCreatable", strMiddleOptName, dlgClass)));
            // Item ���� ����
            this.bwOption.setItemModifiable(Boolean.parseBoolean(super.getTextBundle("isItemModifiable", strMiddleOptName, dlgClass)));

            // Item Revision ���� ����
            this.bwOption.setRevCreatable(Boolean.parseBoolean(super.getTextBundle("isRevCreatable", strMiddleOptName, dlgClass)));
            // Item Revision ���� ����
            this.bwOption.setRevModifiable(Boolean.parseBoolean(super.getTextBundle("isRevModifiable", strMiddleOptName, dlgClass)));

            // DataSet ��� ����
            this.bwOption.setDSAvailable(Boolean.parseBoolean(super.getTextBundle("isDSAvailable", strMiddleOptName, dlgClass)));
            // DataSet ���� �� ���� ����(��ü)
            this.bwOption.setDSChangable(Boolean.parseBoolean(super.getTextBundle("isDSChangable", strMiddleOptName, dlgClass)));

            // BOM ��� ����
            this.bwOption.setBOMAvailable(Boolean.parseBoolean(super.getTextBundle("isBOMAvailable", strMiddleOptName, dlgClass)));
            // BOM Line �Ӽ� ���� ����
            this.bwOption.setBOMLineModifiable(Boolean.parseBoolean(super.getTextBundle("isBOMLineModifiable", strMiddleOptName, dlgClass)));
            // BOM �籸�� ����(BOM Structure ���� �� �籸��)
            this.bwOption.setBOMRearrange(Boolean.parseBoolean(super.getTextBundle("isBOMRearrange", strMiddleOptName, dlgClass)));

            // �ϰ� Upload�۾��� ���Ǵ� Template DataSet Name
            this.strTemplateDSName = super.getTextBundle("TemplateDatasetName", null, dlgClass);

        } catch (Exception e) {
            MessageBox.post(super.shell, this.getTextBundle("AdminWorkInvalid", "MSG", dlgClass), "ERROR", 2);
        }

    }

    /**
     * ȭ�鱸��
     */
    public void dialogOpen() {
        try {

            super.dialogOpen();
            // Template ���� �ٿ�ε� ��ư Ȱ��ȭ
            super.enableTemplateButton();
            // super.shell.setImage(Activator.imageDescriptorFromPlugin("com.symc-newplm", "icons/import_16.png").createImage());

            this.shell.addDisposeListener(new DisposeListener() {

                @Override
                public void widgetDisposed(DisposeEvent e) {

                    try {
                        for (int i = 0; i < bomWindowList.size(); i++) {
                            TCComponentBOMWindow bomWindow = bomWindowList.get(i);
                            if (bomWindow != null) {
                                bomWindow.close();
                                bomWindow = null;
                            }
                        }
                    } catch (TCException ex) {
                        ex.printStackTrace();
                    }
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * ManualTreeItem���� Load�� Data�� Server�� Upload
     * 
     * @throws Exception
     */
    @Override
    public void execute() throws Exception {
        // ���� ��ó��
        executePre();

        if (this.nWraningCount > 0) {
            org.eclipse.swt.widgets.MessageBox box1 = new org.eclipse.swt.widgets.MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
            box1.setMessage(this.nWraningCount + this.getTextBundle("WarningIgnore", "MSG", dlgClass));

            if (box1.open() != SWT.OK) {
                return;
            }
        }

        // ���� ��ư Disable
        super.executeButton.setEnabled(false);
        // Excel �˻� ��ư Disable
        super.searchButton.setEnabled(false);

        // Top TreeItem Array
        TreeItem[] szTopItems = super.tree.getItems();

        // TreeItem�� �������� �ʴ� ���
        if (szTopItems == null || szTopItems.length == 0) {
            MessageBox.post(super.shell, super.getTextBundle("UploadInvalid", "MSG", dlgClass), "Notification", 2);
            return;
        }

        ExecutionJob job = new ExecutionJob(shell.getText(), szTopItems);
        job.schedule();

    }

    /**
     * BOM ����, BomLine �Ӽ� Import
     * 
     * @param topTreeItem
     * @throws Exception
     */
    private void importBOM(ManualTreeItem topTreeItem) throws Exception {

        String strRevID = topTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID);

        // Top TreeItem�� ItemRevision
        TCComponentItemRevision itemRevision = this.tcItemRevSet.get(topTreeItem.getItemID() + "/" + strRevID);

        try {

            if (itemRevision == null)
                throw new Exception(topTreeItem.getItemID() + this.getTextBundle("ItemNotFound", "MSG", dlgClass));

            // pack �� BOMLine �� �����Ͽ� �б�
//            this.session.getPreferenceService().setString(TCPreferenceService.TC_preference_user, "PSEAutoPackPref", "0");
            this.session.getPreferenceService().setStringValue("PSEAutoPackPref", "0");
            
            TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
            // Revision Rule ����
            TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
            TCComponentBOMWindow bomWindow = windowType.create(ruleType.getDefaultRule());
            this.bomWindowList.add(bomWindow);

            if (bomWindow instanceof SYMCBOMWindow) {
                ((SYMCBOMWindow) bomWindow).skipHistory(true);
            }

            TCComponentBOMLine topLine = bomWindow.setWindowTopLine(null, (TCComponentItemRevision) itemRevision, null, null);

            // Top TreeItem�� BomLine ������ �Ϸ�Ǿ��ٰ� ������.
            // topTreeItem.setStatus(STATUS_COMPLETED,
            // super.getTextBundle("BomCompleted", "MSG", super.dlgClass) );

            // Bom�� �ʱ�ȭ�ϰ� �ٽ� �����ϴ� ���
            if (this.bwOption.isBOMRearrange()) {
                // Top ItemRevision�� BomWindow�� �����Ѵٸ�.. Bom Structure�� ����(���� �������)
                this.clearBOM(topLine, true);
                bomWindow.save();
            }

            // Thread.sleep(10000);

            // BOm Structure ����
            this.importBOMLine(topLine, topTreeItem, true);

            // Bom Window Save/Close
            bomWindow.save();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            // BOM Import �Ϸ� �� BOM Window�� �ݴ´�.
            if (bomWindowList != null && bomWindowList.size() > 0) {
                for (int i = 0; i < bomWindowList.size(); i++) {
                    bomWindowList.get(i).close();
                }
            }
        }
    }

    /**
     * Bom Structure �ʱ�ȭ
     * 
     * @param bomLine
     * @param isTopLine
     * @throws Exception
     */
    private void clearBOM(TCComponentBOMLine bomLine, boolean isTopLine) throws Exception {
        TCComponent[] children = bomLine.getRelatedComponents("bl_child_lines");
        for (int i = (children.length - 1); i >= 0; i--) // TCComponent child : children)
        {

            TCComponentBOMLine childBOMLine = (TCComponentBOMLine) children[i];
            this.clearBOM(childBOMLine, false);
        }

        if (!isTopLine) {
            bomLine.cut();
            // this.bomWindow.save();
        }
    }

    private void updateBOMLineProp(TCComponentBOMLine bomLine, ManualTreeItem treeItem, boolean isTopLine) throws Exception {

        // BOMLine �Ӽ� Update
        if (this.bwOption.isBOMLineModifiable()) {
            // BOMLine �Ӽ� Update

            // TopLine�� �Ӽ��� Update���� ����
            if (!isTopLine) {
                this.setBWItemAttrValue(treeItem, bomLine, CLASS_TYPE_BOMLINE);
            }
            this.syncItemState(treeItem, STATUS_INPROGRESS, super.getTextBundle("BomLineUpdated", "MSG", super.dlgClass));

        }

    }

    /**
     * Bom Structure ����
     * 
     * @param bomLine
     *            : Parent BomLine
     * @param treeItem
     *            : Target TreeItem
     */
    private void importBOMLine(TCComponentBOMLine bomLine, ManualTreeItem treeItem, boolean isTopLine) throws Exception {

        // Child TreeItem
        ArrayList<ManualTreeItem> itemList = new ArrayList<ManualTreeItem>();
        this.syncGetChildItem(treeItem, itemList);

        try {
            TCComponent[] childLines = bomLine.getRelatedComponents("bl_child_lines");

            boolean[] szChildLineMatchFlag = new boolean[childLines.length];

            ArrayList<String> bomLineKeyList = new ArrayList<String>();

            for (int i = 0; i < itemList.size(); i++) {
                ManualTreeItem childItem = itemList.get(i);
                String strItemType = childItem.getItemType();
                if (!strItemType.equals(ManualTreeItem.ITEM_TYPE_TCITEM)) {
                    continue;
                }

                // TreeITem Selection
                this.syncSetItemSelection(childItem);

                String strRevID = childItem.getBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID);
                String strSeqNo = childItem.getBWItemAttrValue(CLASS_TYPE_BOMLINE, BOMLINE_ATTR_SEQUENCENO);
                TCComponentItemRevision itemRevision = this.tcItemRevSet.get(childItem.getItemID() + "/" + strRevID);

                if (itemRevision == null)
                    throw new Exception(childItem.getItemID() + this.getTextBundle("ItemNotFound", "MSG", dlgClass));

                TCComponentBOMLine childLine = null;
                for (int j = 0; j < childLines.length; j++) {
                    // Part Qty�� �������ΰ�� Excel Part�� Match
                    if (szChildLineMatchFlag[j]) {
                        continue;
                    }

                    TCComponentBOMLine cLine = (TCComponentBOMLine) childLines[j];

                    String strLineSeqNo = cLine.getProperty("bl_sequence_no");

                    // Seq No.�� ������ �ٸ� Item�� ���
                    if (!cLine.getItemRevision().equals(itemRevision) && strSeqNo.equals(strLineSeqNo)) {
                        throw new Exception("Different Part Exist At Sequnce No.('" + strSeqNo + "') ");
                    }

                    if (cLine.getItemRevision().equals(itemRevision) && strSeqNo.equals(strLineSeqNo)) {
                        childLine = cLine;
                        szChildLineMatchFlag[j] = true;

                        break;
                    }
                }

                // PSE BOM�� ���°�� ����
                if (childLine == null) {
                    // this.inValidStructure(childItem);

                    String seqNo = childItem.getBWItemAttrValue(CLASS_TYPE_BOMLINE, "bl_sequence_no");

                    String pItemID = childItem.getParentItemID();
                    String cItemID = childItem.getItemID();

                    String strBomLineKey = pItemID + "/" + seqNo + "/" + cItemID;

                    if (dupBomLineMap.containsKey(strBomLineKey)) {
                        this.syncItemState(childItem, STATUS_COMPLETED, super.getTextBundle("BomLineCreated", "MSG", super.dlgClass));
                        return;
                    } else {

                        String dynamicQty = childItem.getBWItemAttrValue(CLASS_TYPE_BOMLINE, "dynamic_qty");
                        int nQty = 1;
                        if (!CustomUtil.isEmpty(dynamicQty)) {
                            try {
                                nQty = Integer.parseInt(dynamicQty);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }

                        for (int z = 0; z < nQty; z++) {
                            childLine = bomLine.add(null, itemRevision, null, false);
                            this.syncItemState(childItem, STATUS_INPROGRESS, super.getTextBundle("BomLineCreated", "MSG", super.dlgClass));
                            updateBOMLineProp(childLine, childItem, false);
                            // �Ϸ� Status Setting
                        }

                        bomLineKeyList.add(strBomLineKey);
                    }

                    this.importBOMLine(childLine, childItem, false);
                } else {
                    updateBOMLineProp(childLine, childItem, false);
                    syncItemState(childItem, STATUS_COMPLETED, null);
                    // ���ȣ��
                    this.importBOMLine(childLine, childItem, false);
                }
            }

            syncItemState(treeItem, STATUS_COMPLETED, null);

            for (int i = 0; i < bomLineKeyList.size(); i++) {
                dupBomLineMap.put(bomLineKeyList.get(i), null);

            }

        } catch (TCException e) {
            // BomLine ������ Error �߻� Log Setting
            this.syncItemState(treeItem, STATUS_ERROR, e.toString());
            e.printStackTrace();
        }

    }

    /**
     * TreeItem ������ Bom ������ ��ġ���� �ʴ´ٸ�
     * TreeItem ChildItem�� ��� Error ó�� ��.
     * 
     * @param treeItem
     */
    protected void inValidStructure(ManualTreeItem treeItem) {
        this.syncItemState(treeItem, STATUS_ERROR, super.getTextBundle("BOMNotMatch", "MSG", this.dlgClass));

        // Child TreeItem
        ArrayList<ManualTreeItem> itemList = new ArrayList<ManualTreeItem>();
        this.syncGetChildItem(treeItem, itemList);

        for (int i = 0; i < itemList.size(); i++) {
            ManualTreeItem childItem = itemList.get(i);
            // Item Type �� ���
            if (childItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCITEM)) {
                // ���ȣ��
                this.inValidStructure(childItem);
            }
        }
    }

    /**
     * Item ����
     * 
     * @param treeItem
     */
    private void importData(ManualTreeItem treeItem) throws Exception {

        this.syncSetItemSelection(treeItem);
        String strItemType = treeItem.getItemType();

        // Item Type �� ���
        if (strItemType.equals(ManualTreeItem.ITEM_TYPE_TCITEM)) {

            try {
                // Import Item
                this.importItem(treeItem);
                // treeItem.setStatus(STATUS_INPROGRESS,
                // super.getTextBundle("UploadCompleted", "MSG",
                // super.dlgClass));

            } catch (Exception e) {

                this.syncItemState(treeItem, STATUS_ERROR, e.toString());
                e.printStackTrace();

                // BOM ���� ����� �����ϸ�.. Item Import Error �߻��� �۾��� �ߴ��մϴ�.
                if (this.bwOption.isBOMAvailable()) {
                    throw new Exception("�۾��� �ߴ��մϴ�.");
                }

            } finally {
                importDataPost(treeItem);
            }
        }
        // DataSet Type �� ���
        else {

            try {
                // DataSet Import ��� ����ϴ� ���
                if (this.bwOption.isDSAvailable()) {
                    // Master �׸� check
                    // AutoCad �ΰ�� ��� DataSet�� ������...

                    if (treeItem.getStatus() != STATUS_ERROR) {
                        // Import DataSet
                        this.importDataSet(treeItem);
                        this.syncItemState(treeItem, STATUS_COMPLETED, super.getTextBundle("UploadCompleted", "MSG", super.dlgClass));
                    }

                }
                // // DataSet Import ��� ������� �ʴ� ���
                else {

                    this.syncItemState(treeItem, STATUS_COMPLETED, null);
                }
            } catch (Exception e) {
                this.syncItemState(treeItem, STATUS_ERROR, e.toString());
                e.printStackTrace();
            } finally {
                importDataPost(treeItem);
            }
            // DataSet ���� TreeItem�� �������� ����
            return;
        }

        ArrayList<ManualTreeItem> itemList = new ArrayList<ManualTreeItem>();
        this.syncGetChildItem(treeItem, itemList);

        for (int i = 0; i < itemList.size(); i++) {
            // ���ȣ��
            this.importData(itemList.get(i));
        }

    }

    /**
     * Import Item : Item�� �������� ������ Item ����, ItemRevision�� �������� ������ Revise
     * 
     * [SR150209-019][20150402] shcho, 1) ��Ͻ� �ڵ� Release�ǵ��� ����. 2) �̹� ��ϵǾ� Release �� ��� Revise �Ͽ� ����ϵ��� ��� �߰�.
     * 
     * @param treeItem
     * @throws Exception
     */
    public void importItem(ManualTreeItem treeItem) throws Exception {
        StringBuffer szBuf = new StringBuffer();
        this.syncGetItemText(treeItem, TREEITEM_COMLUMN_ITEMID, szBuf);
        String strItemID = szBuf.toString();
        szBuf = new StringBuffer();

        this.syncGetItemText(treeItem, TREEITEM_COMLUMN_REVISION, szBuf);
        String strRevision = szBuf.toString();
        szBuf = new StringBuffer();

        this.syncGetItemText(treeItem, TREEITEM_COMLUMN_PARTNAME, szBuf);
        String strPartName = szBuf.toString();

        if (this.bwOption.isItemIDBlankable() && strItemID.trim().equals(NEW_ITEM_ID)) {

            // New Item ID( ��ӹ��� Class���� �����ؾ� ��)
            strItemID = generateNewID();

            // Tree�� �߹��� ItemID �ݿ�
            treeItem.setItemID(strItemID);
            treeItem.setBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID, strItemID);
            this.syncSetItemText(treeItem, TREEITEM_COMLUMN_ITEMID, strItemID);

            ArrayList<ManualTreeItem> itemList = new ArrayList<ManualTreeItem>();
            this.syncGetChildItem(treeItem, itemList);
            for (int i = 0; i < itemList.size(); i++) {
                ManualTreeItem childItem = itemList.get(i);
                if (childItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCDATASET)) {
                    childItem.setItemID(strItemID);
                }
            }

        }

        // item �� �̹� �����ϴ��� �˻��Ѵ�.
        TCComponentItem item = SYMTcUtil.findItem(session, strItemID);
        TCComponentItemRevision itemRevision = null;

        // item �� �̹� �����ϸ� ������ �ʿ����.
        if (item != null) {
            // Item ������ ����ϴ� Option �ΰ��
            if (this.bwOption.isItemCreatable() && !this.bwOption.isItemModifiable())
                throw new Exception(super.getTextBundle("ItemExist", "MSG", super.dlgClass));

            if (SYMTcUtil.isCheckedOut(item))
                throw new Exception(this.getTextBundle("ItemCheckouted", "MSG", dlgClass));

            // ������ �������� �����ϴ��� Check -> �������� ������ ItemRevision �� �����Ѵ�.

            itemRevision = SYMTcUtil.findItemRevision(session, strItemID, strRevision);

            // Item Revision �� �������� ���� ���
            if (itemRevision == null) {

                // // ItemRevision ������ ����ϴ� ���
                // if (true || !this.bwOption.isRevCreatable() && this.bwOption.isRevModifiable())
                // throw new Exception(super.getTextBundle("ItemRevisionNotExist", "MSG", super.dlgClass));

                // Item ������ ����ϴ� ���
                if (!this.bwOption.isRevCreatable() && this.bwOption.isRevModifiable())
                    throw new Exception(super.getTextBundle("ItemRevisionNotExist", "MSG", super.dlgClass));

                // Item �� �����Ұ�� Item ������ Revision �� �����Ҽ� �ִ� ������ �ִ��� Check �Ѵ�.
                if (!item.isModifiable("object_name")) {
                    throw new Exception(super.getTextBundle("RevAuthError", "MSG", super.dlgClass));
                }

                // Item Revision ����
                itemRevision = SYMTcUtil.createItemRevision(item.getLatestItemRevision(), strRevision);
                // refresh.
                // itemRevision.refresh();

                this.syncItemState(treeItem, STATUS_INPROGRESS, super.getTextBundle("ItemRevised", "MSG", super.dlgClass));

            }
            // Item Revision�� �����ϴ� ���
            else {

                if (SYMTcUtil.isCheckedOut(itemRevision))
                    throw new Exception(super.getTextBundle("RevisionCheckouted", "MSG", super.dlgClass));

                // ItemRevision ������ ����ϴ� ���
                if (this.bwOption.isRevModifiable()) {
                    //Release�� ��� Revise
                    if(SYMTcUtil.isReleased(itemRevision)) {
                        String nextRevisionID = String.format("%03d", Integer.parseInt(strRevision) + 1);
                        TCComponentItemRevision nextItemRevision = SYMTcUtil.findItemRevision(session, strItemID, nextRevisionID);
                        if(nextItemRevision == null) {
                            TCComponentItemRevision newItemRevision= SYMTcUtil.createItemRevision(itemRevision, nextRevisionID);
                            itemRevision = newItemRevision;
                            strRevision = nextRevisionID;
                            this.syncItemState(treeItem, STATUS_INPROGRESS, super.getTextBundle("ItemRevRevised", "MSG", dlgClass));
                        } else {
                            throw new Exception(this.getTextBundle("NextItemRevisionExist", "MSG", dlgClass));
                        }
                    }
                    
                    if (!itemRevision.isModifiable("object_name")) {
                        throw new Exception(this.getTextBundle("RevionModifyError", "MSG", dlgClass));
                    }
                }
            }

        } else {
            // item �� �̹� �����ϴ��� �˻����. Item ����.

            // Item ����/���� ���������� �ʴ� ���
            if (!this.bwOption.isItemCreatable() && !this.bwOption.isItemModifiable())
                throw new Exception(super.getTextBundle("ItemNotExist", "MSG", super.dlgClass));

            // Item ������ ����ϴ� ���
            if (!this.bwOption.isItemCreatable() && this.bwOption.isItemModifiable())
                throw new Exception(super.getTextBundle("ItemNotExist", "MSG", super.dlgClass));

            if (strRevision == null || strRevision.equals("")) {
                strRevision = NEW_ITEM_REV;
                treeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID, NEW_ITEM_REV);
                this.syncSetItemText(treeItem, TREEITEM_COMLUMN_REVISION, NEW_ITEM_REV);
            }

            String strOldItemID = treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "old_item_id");
            String strOldRevID = treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "old_item_revision_id");

            if (!CustomUtil.isEmpty(strOldItemID) && !CustomUtil.isEmpty(strOldRevID)) {
                TCComponentItemRevision oldItemRevision = SYMTcUtil.findItemRevision(session, strOldItemID, strOldRevID);

                item = oldItemRevision.saveAsItem(strItemID, strRevision, strPartName, "", false, null);
                CustomUtil.relateDatasetToItemRevision(oldItemRevision, item.getLatestItemRevision(), true, true, true, null, false);

                item.getLatestItemRevision().setReferenceProperty("s7_ECO_NO", null);

                this.syncItemState(treeItem, STATUS_INPROGRESS, "SaveAs Completed");
            } else {
                // ��������� ��� SOA API�� ���(������ �߰� �ʼ� �Ӽ����� ������)
                if ("BWTechDocImpDialog".equals(dlgClass.getSimpleName())) {
                    item = TechDocMasterOperation.createTechDoc(strItemID, strPartName);
                } else {
                    // Item ����
                    item = SYMTcUtil.createItem(session, strItemID, strPartName, "", this.strTargetItemType, strRevision);
                    // UOM�� Item���� �� ����(���� Property API�δ� String���� ������� ����)
                    // String strUOM = treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "uom_tag");
                    // �������� ��쿡�� uom_tag�� EA���� �ִ°Ϳ��� ��� �����ۿ� �ƹ��� ���� �־����� �ʴ� ������ ���� (2014.01.08)
                    // if (this.strTargetItemType.equals("M7_Subsidiary")) {
                    // item.setProperty("uom_tag", "EA");
                    // }
                    
                    //������ Item Classify
                    if(item != null) {
                    ClassifyService classifyService = new ClassifyService();
                    classifyService.classifyResource(item);
                    }
                }

                this.syncItemState(treeItem, STATUS_INPROGRESS, super.getTextBundle("ItemCreated", "MSG", super.dlgClass));
            }

            // ���� ������ ������ �������� ���� �´�.
            itemRevision = item.getLatestItemRevision();

        }

        // Item �߰��Ӽ� Update
        if (this.bwOption.isItemModifiable())
            this.setBWItemAttrValue(treeItem, item, CLASS_TYPE_ITEM);

        // Item Revision �߰��Ӽ� Update
        if (this.bwOption.isRevModifiable())
            this.setBWItemAttrValue(treeItem, itemRevision, CLASS_TYPE_REVISION);

        if (this.bwOption.isItemModifiable() || this.bwOption.isRevModifiable())
            this.syncItemState(treeItem, STATUS_INPROGRESS, super.getTextBundle("ItemUpdated", "MSG", super.dlgClass));

        if (!this.bwOption.isBOMAvailable())
            this.syncItemState(treeItem, STATUS_COMPLETED, null);
        else
            this.syncItemState(treeItem, STATUS_INPROGRESS, "Item Loaded");

        //szBuf = new StringBuffer();
        //this.syncGetItemText(treeItem, TREEITEM_COMLUMN_REVISION, szBuf);
        //strRevision = szBuf.toString();

        String strRevKey = strItemID + "/" + strRevision;

        if (!this.tcItemRevSet.containsKey(strRevKey))
            this.tcItemRevSet.put(strRevKey, itemRevision);

        // DataSet Import ��� ��� Check
        if (!this.bwOption.isDSAvailable() || !this.bwOption.isDSChangable()) {
            return;
        }

    }

    /**
     * Import DataSet DataSet ���� �� ItemRevision�� ����
     * 
     * @param treeItem
     * @throws Exception
     */
    private void importDataSet(final ManualTreeItem treeItem) throws Exception {
        ArrayList<ManualTreeItem> refList = new ArrayList<ManualTreeItem>();
        syncGetParentItem(treeItem, refList);
        ManualTreeItem pTreeItem = refList.get(0);

        String strItemID = pTreeItem.getItemID();
        TCComponentItem item = SYMTcUtil.findItem(session, strItemID);
        TCComponentItemRevision itemRevision = item.getLatestItemRevision();
        //String strRevID = pTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID);
        String strRevID = itemRevision.getStringProperty(SDVPropertyConstant.ITEM_REVISION_ID);
        String strDataSetName = strItemID + "/" + strRevID;

        String strCadPartRev = pTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "dataset_revision_id");
        // CAD REV�� ����Ѵ�.
        // String strCadPartRev = ((ManualTreeItem)treeItem.getParentItem()).getBWItemAttrValue(CLASS_TYPE_REVISION, "dataset_revision_id");
        if (strCadPartRev != null && !"".equals(strCadPartRev)) {
            strDataSetName = strDataSetName + "-" + strCadPartRev;
        }

        String strFilePath = treeItem.getBWItemAttrValue(CLASS_TYPE_DATASET, DATASET_ATTR_FILEPATH);

        String strFileExt = strFilePath.substring(strFilePath.lastIndexOf(".") + 1, strFilePath.length());

        // ���� Dataset�� ��ȿ Ȯ���� Array
        String strDataSetType = this.bwOption.getDataSetType(strFileExt);
        if (CustomUtil.isEmpty(strDataSetType))
            throw new Exception(super.getTextBundle("DSTypeInvalid", "MSG", super.dlgClass));

        // String[] szDataSetExts = this.bwOption.getDataRefExts(strDataSetType);

        TCComponent tcComp = null;
        TCComponentItemRevision orgRevision = this.tcItemRevSet.get(strItemID + "/" + strRevID);
        // ��������� ���
        if ("BWTechDocImpDialog".equals(dlgClass.getSimpleName())) {
            tcComp = orgRevision.getItem();
        } else {

            tcComp = orgRevision;
        }

        if (tcComp == null)
            throw new Exception("itemRevision(" + strItemID + ")" + super.getTextBundle("CanNotFind", "MSG", super.dlgClass));

        // ���� DataSet�� �����ϰ� �űԻ����ϴ� ���
        if (this.bwOption.isDSChangable() && !"catia".equals(strDataSetType)) {

            // ���� Dataset�� �����ϸ� ������
            AIFComponentContext[] context = tcComp.getChildren();
            for (int j = 0; j < tcComp.getChildrenCount(); j++) {
                TCComponent component = (TCComponent) context[j].getComponent();

                if (!(component instanceof TCComponentDataset))
                    continue;

                // Upload���� Dataset�� ���
                if (component.isTypeOf(strDataSetType)) {

                    TCComponentDataset dataset = (TCComponentDataset) component;
                    String strDSName = dataset.getProperty("object_name");
                    // ��������� ���
                    if ("BWTechDocImpDialog".equals(dlgClass.getSimpleName())) {
                        if (strDataSetName.equals(strDSName))
                            tcComp.remove("IMAN_reference", component);
                    } else {

                        // ���� �̸��� DataSet�� �����ϸ� ���� ��..
                        if ("DirectModel".equals(strDataSetType)) {

                            if (strDataSetName.equals(strDSName))
                                tcComp.remove("IMAN_Rendering", component);
                        } else {
                            if (strDataSetName.equals(strDSName))
                                tcComp.remove(TcDefinition.TC_SPECIFICATION_RELATION, component);
                        }
                    }

                }
            }
        }

        Vector<File> importFiles = new Vector<File>();

        File importFile = new File(strFilePath);
        if (!importFile.exists()) {
            throw new Exception(strFilePath + " " + super.getTextBundle("CanNotFindFile", "MSG", super.dlgClass));
        }

        // ��������� ���
        if ("BWTechDocImpDialog".equals(dlgClass.getSimpleName())) {
            strDataSetName = importFile.getName();
        }

        importFiles.addElement(importFile);

        // Dataset ����
        // TCComponentDataset createdDataset = SYMTcUtil.createDataSet(session, itemRevision, strDataSetType, strDataSetName, importFiles);
        // Dataset ���� - model dataset ���ܷ� ���� ���� ���� - 2013.04.24
        TCComponentDataset createdDataset = SYMTcUtil.makeDataSet(session, tcComp, strDataSetType, strDataSetName, importFiles);

        // Dataset �߰��Ӽ� Update
        //this.setBWItemAttrValue(treeItem, createdDataset, CLASS_TYPE_DATASET);
    }

    public static void selfReleaseDataSet(TCComponent dataset, String procName) throws Exception {

        TCComponentTaskTemplate template = null;
        TCComponentTaskTemplateType imancomponenttasktemplatetype = (TCComponentTaskTemplateType) dataset.getSession().getTypeComponent("EPMTaskTemplate");
//        TCComponentTaskTemplate[] tasktemplate = imancomponenttasktemplatetype.extentReadyTemplates(false);
        TCComponentTaskTemplate[] tasktemplate = imancomponenttasktemplatetype.getProcessTemplates(false, false, null, null, null);
        for (int j = 0; j < tasktemplate.length; j++) {
            if (tasktemplate[j].toString().equals(procName)) {
                template = tasktemplate[j];
                break;
            }
        }

        if (template == null)
            throw new Exception("Can't Find SelfRelease Process Template");

        // �ڰ����� ����

        TCComponent[] aimancomponent = new TCComponent[] { dataset };
        int a[] = new int[] { 1 };
        if (dataset != null) {
            TCComponentProcessType processtype = (TCComponentProcessType) dataset.getSession().getTypeComponent("Job");
            processtype.create("Self Release", "", template, aimancomponent, a);
        }
    }

    /**
     * Excel�� ���ǵǾ� �ִ� Item�� �Ӽ����� TCComponent�� ����
     * 
     * 1. Excel Loading�� ManualTreeItem.BWItemData�� ��� �Ӽ��� �����
     * 2. Upload�� ManualTreeItem.BWItemData�� ��� �Ӽ��� �ش� Item Type TCComponent�� ������
     * 
     * @param treeItem
     *            : TreeItem
     * @param component
     *            : Data�� Setting�� TCComponent
     * @param strType
     *            : Model ����
     * @throws Exception
     */
    private void setBWItemAttrValue(ManualTreeItem treeItem, TCComponent component, String strType) throws Exception {
        // �ش� ���� ��� �Ӽ� ��
        HashMap<String, Integer> attrIndexMap = this.headerModel.getModelAttrs(strType);
        Object[] attrNames = attrIndexMap.keySet().toArray();

        ArrayList<String> attrList = new ArrayList<String>();
        ArrayList<String> valueList = new ArrayList<String>();

        for (int i = 0; i < attrNames.length; i++) {
            String strAttrName = (String) attrNames[i];

            // �Ӽ� Update�� ���ʿ��� �Ӽ��� Skip �Ѵ�.
            boolean isSkipAttr = false;
            for (int j = 0; j < szSkipAttrs.length; j++) {
                if (strAttrName.equals(szSkipAttrs[j])) {
                    if (component instanceof SYMCBOMLine && strAttrName.equals("bl_variant_condition")) {
                        ((SYMCBOMLine) component).setMVLCondition(treeItem.getBWItemAttrValue(CLASS_TYPE_BOMLINE, "bl_variant_condition"));
                    }

                    if (component instanceof TCComponentItemRevision && (strAttrName.equals("s7_ACT_WEIGHT") || strAttrName.equals("s7_BOUNDINGBOX"))) {
                        TCComponent refComp = component.getReferenceProperty("s7_Vehpart_TypedReference");
                        if (refComp != null) {
                            refComp.setProperty(strAttrName, treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, strAttrName));
                        } else {
                            refComp = SYMTcUtil.createApplicationObject(component.getSession(), "S7_Vehpart_TypedReference", new String[] { strAttrName }, new String[] { treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, strAttrName) });

                            component.setReferenceProperty("s7_Vehpart_TypedReference", refComp);
                        }
                    }

                    isSkipAttr = true;
                    break;
                }
            }

            if (isSkipAttr)
                continue;

            // ��ȿ�� �Ӽ����� Validation Check
            if (!component.isValidPropertyName(strAttrName)) {
                this.syncItemState(treeItem, STATUS_ERROR, strAttrName + this.getTextBundle("AttrInvalid", "MSG", dlgClass));

                continue;
            }

            String strAttrValue = treeItem.getBWItemAttrValue(strType, strAttrName);
            attrList.add(strAttrName);
            valueList.add(strAttrValue);

        }

        /*
         * if( strType.equals(CLASS_TYPE_ITEM) )
         * {
         * //component.setReferenceProperty("uom_tag", "Piece");
         * 
         * attrList.add("uom_tag");
         * valueList.add("Piece");
         * }
         */

        // �Ӽ� �ϰ� Update�� ���� Array ����
        String[] szAttr = new String[attrList.size()];
        String[] szValue = new String[attrList.size()];
        for (int i = 0; i < attrList.size(); i++) {
            szAttr[i] = attrList.get(i);
            szValue[i] = valueList.get(i);

        }

        TCProperty[] props = component.getTCProperties(szAttr);

        for (int i = 0; i < props.length; i++) {

            if (props[i] == null) {
                System.out.println(szAttr[i] + " is Null");
                continue;
            }

            Object value = szValue[i];
            CustomUtil.setObjectToPropertyValue(props[i], value);

        }

        if (szAttr.length > 0) {
            try {
                component.setTCProperties(props);

            } catch (TCException e) {
                this.syncItemState(treeItem, STATUS_ERROR, e.toString());
                throw new Exception(e.toString());
            }

        }

    }

    /**
     * Job�� UI Thread���� �浹�� ���� UI Update
     * 
     * @param treeItem
     */
    public void syncGetParentItem(final ManualTreeItem treeItem, final ArrayList<ManualTreeItem> refList) {
        shell.getDisplay().syncExec(new Runnable() {

            public void run() {
                refList.add((ManualTreeItem) treeItem.getParentItem());
            }

        });

    }

    /**
     * Job�� UI Thread���� �浹�� ���� UI Update
     * 
     * @param treeItem
     */
    public void syncGetItemText(final ManualTreeItem treeItem, final int nCulumn, final StringBuffer szBuf) {
        shell.getDisplay().syncExec(new Runnable() {

            public void run() {
                szBuf.append(treeItem.getText(nCulumn));
            }

        });

    }

    /**
     * Job�� UI Thread���� �浹�� ���� UI Update
     * 
     * @param treeItem
     */
    public void syncSetItemText(final ManualTreeItem treeItem, final int nCulumn, final String strText) {
        shell.getDisplay().syncExec(new Runnable() {

            public void run() {
                if (treeItem == null)
                    return;

                treeItem.setText(nCulumn, strText);
            }

        });

    }

    /**
     * Job�� UI Thread���� �浹�� ���� UI Update
     * 
     * @param treeItem
     */
    public void syncSetItemTextField(final String strText) {
        shell.getDisplay().syncExec(new Runnable() {

            public void run() {
                text.append(strText + "\n");
            }

        });

    }

    /**
     * Job�� UI Thread���� �浹�� ���� UI Update
     * 
     * @param treeItem
     */
    public void syncSetItemSelection(final ManualTreeItem treeItem) {
        shell.getDisplay().syncExec(new Runnable() {

            public void run() {
                tree.setSelection(treeItem);
            }

        });

    }

    /**
     * Job�� UI Thread���� �浹�� ���� UI Update
     * 
     * 
     * @param treeItem
     * @param nStatus
     * @param strMessage
     */
    public void syncItemState(final ManualTreeItem treeItem, final int nStatus, final String strMessage) {
        shell.getDisplay().syncExec(new Runnable() {

            public void run() {
                if (treeItem == null)
                    return;

                if (strMessage == null)
                    treeItem.setStatus(nStatus);
                else
                    treeItem.setStatus(nStatus, strMessage);
            }

        });
    }

    public void syncGetChildItem(final ManualTreeItem treeItem, final ArrayList<ManualTreeItem> itemList) {
        shell.getDisplay().syncExec(new Runnable() {

            public void run() {
                TreeItem[] childItems = treeItem.getItems();
                for (int i = 0; i < childItems.length; i++) {
                    itemList.add((ManualTreeItem) childItems[i]);
                }
            }

        });

    }

    /**
     * 
     * Excel�� ��õ� �Ӽ��׸��� Loading
     */
    @Override
    public void load() throws Exception {
        // Load ��ó��
        loadPre();

        // ManualTreeItem List
        this.itemList = new ArrayList<ManualTreeItem>();

        // TCComponentItemRevision List
        this.tcItemRevSet = new HashMap<String, TCComponentItemRevision>();

        this.tree.removeAll();

        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                Workbook wb = null;
                FileInputStream fis = null;
                try {
                    shell.setCursor(waitCursor);
                    String strFilePath = fileText.getText();
                    fis = new FileInputStream(strFilePath);

                    String strExt = fileText.getText().substring(strFilePath.lastIndexOf(".") + 1);

                    if (strExt.toLowerCase().equals("xls")) {
                        // Excel WorkBook
                        wb = new HSSFWorkbook(fis);
                    } else {
                        // Excel WorkBook
                        wb = new XSSFWorkbook(fis);
                    }

                    fis.close();
                    fis = null;
                    // Excel Header ���� Loading
                    loadHeader(wb);

                    // Item Sheet Data Loading
                    loadData(wb);

                } catch (Exception e) {
                    MessageBox.post(shell, getTextBundle("ExcelInValid", "MSG", dlgClass), "Notification", 2);
                    e.printStackTrace();
                } finally {
                    wb = null;
                    if (fis != null) {
                        try {
                            fis.close();
                            fis = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    shell.setCursor(arrowCursor);
                }

            }

        });

        // Load ��ó��
        loadPost();

    }

    /**
     * Excel Header ���� Loading
     * 
     * @param wb
     * @throws Exception
     */
    private void loadHeader(Workbook wb) throws Exception {
        Sheet sheet = wb.getSheetAt(0);

        this.headerModel = new BWItemModel();

        Row itemTypeRow = sheet.getRow(ITEM_TYPE_Y_POS);
        this.strTargetItemType = getCellText(itemTypeRow.getCell(ITEM_TYPE_X_POS));

        if (strTargetItemType == null || strTargetItemType.equals("")) {
            throw new Exception(super.getTextBundle("XlsItemTypeBlank", "MSG", super.dlgClass));
        }

        Row classRow = sheet.getRow(ITEM_HEADER_START_Y_POS);
        Row attrRow = sheet.getRow(ITEM_HEADER_START_Y_POS + 1);

        this.nValidColumnCount = classRow.getPhysicalNumberOfCells() + 1;
        for (int i = ITEM_HEADER_START_X_POS; i < nValidColumnCount; i++) {
            String strClass = getCellText(classRow.getCell(i)).trim();
            String strAttr = getCellText(attrRow.getCell(i)).trim();

            this.headerModel.setModelData(strClass, strAttr, new Integer(i));

        }

    }

    /**
     * Excel Sheet�� ��õ� Data�� Load�Ͽ� ManualTreeItem�� ����
     * 
     * @param wb
     */
    private void loadData(Workbook wb) {
        Sheet sheet = wb.getSheetAt(0);
        int rows = sheet.getPhysicalNumberOfRows() + 1;

        Row classRow = sheet.getRow(ITEM_HEADER_START_Y_POS);
        Row attrRow = sheet.getRow(ITEM_HEADER_START_Y_POS + 1);

        for (int r = ITEM_START_Y_POS; r < rows; r++) {
            Row row = sheet.getRow(r);

            if (row == null)
                continue;

            /*
             * String strItemID = this.getCellText(row.getCell(ITEM_XLS_COLUMN_INDEX_ITEMID)); String strRevision = this.getCellText(row.getCell(ITEM_XLS_COLUMN_INDEX_REVISION)); String strLevel = this.getCellText(row.getCell(ITEM_XLS_COLUMN_INDEX_LEVEL));
             * 
             * // ItemID, Revision, Level ���� �ʼ��Է� �׸� if (strItemID.equals("") || strRevision.equals("") || strLevel.equals("")) { System.out.println("�ʼ� �Է��׸��� �����Ǿ����ϴ�."); continue; }
             */
            BWItemData stcItemData = new BWItemData();

            for (int i = ITEM_START_X_POS; i < this.nValidColumnCount; i++) {
                String strItemName = getCellText(classRow.getCell(i)).trim();
                String strAttrName = getCellText(attrRow.getCell(i)).trim();
                String strAttrValue = getCellText(row.getCell(i));

                if (strAttrValue == null) {
                    strAttrValue = "";
                } else {
                    if (!"bl_variant_condition".equals(strAttrName))
                        strAttrValue = strAttrValue.replaceAll("\n", " ");

                }

                stcItemData.setItemData(strItemName, strAttrName, strAttrValue);
            }

            String strItemID = stcItemData.getItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID);
            String strRevID = stcItemData.getItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID);
            String strDatasetFilePath = stcItemData.getItemAttrValue(CLASS_TYPE_DATASET, DATASET_ATTR_FILEPATH);

            // Excel Item Row�� ����� TreeItem Object
            ManualTreeItem treeItem = null;

            int nLevel = super.getIntValue(stcItemData.getItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_LEVEL));

            // Level 0 ���� TopPart
            if (nLevel == 0) {
                // Top TreeItem
                treeItem = new ManualTreeItem(this.tree, this.tree.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strItemID);
                treeItem.setLevel(nLevel);

            } else if (nLevel > 0) {

                // Child TreeItem
                ManualTreeItem parentTreeItem = this.getParentTreeItem(nLevel);

                if (parentTreeItem != null) {
                    treeItem = new ManualTreeItem(parentTreeItem, parentTreeItem.getItems().length, ManualTreeItem.ITEM_TYPE_TCITEM, strItemID);
                    treeItem.setLevel(nLevel);
                } else {
                    treeItem = new ManualTreeItem(this.tree, SWT.NONE, ManualTreeItem.ITEM_TYPE_TCITEM, strItemID);
                    treeItem.setLevel(0);
                    treeItem.setStatus(STATUS_ERROR, super.getTextBundle("ParentNotExist", "MSG", super.dlgClass));
                }
            }

            if (treeItem != null) {
                // Tree Table�� ǥ�õǴ� �Ӽ�
                // treeItem.setText(TREEITEM_COMLUMN_ITEMID, stcItemData.getItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID));
                // treeItem.setText(TREEITEM_COMLUMN_REVISION, stcItemData.getItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID));

                treeItem.setBWItemData(stcItemData);
                this.setTreeItemData(treeItem, CLASS_TYPE_BOMLINE);
                this.setTreeItemData(treeItem, CLASS_TYPE_ITEM);
                this.setTreeItemData(treeItem, CLASS_TYPE_REVISION);

                this.itemList.add(treeItem);

            }

            // ItemID ������ ������� �ʴ� �� ������ ���
            if (treeItem != null && !this.bwOption.isItemIDBlankable() && strItemID.trim().equals("")) {
                treeItem.setStatus(STATUS_ERROR, this.getTextBundle("ItemIDRequired", "MSG", dlgClass));
            }
            // ItemID ������ ���, ������ ���
            else if (treeItem != null && this.bwOption.isItemIDBlankable() && strItemID.trim().equals("")) {
                treeItem.setBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID, NEW_ITEM_ID);
                treeItem.setText(TREEITEM_COMLUMN_ITEMID, NEW_ITEM_ID);

            }

            // Revision ���� Option�� �ƴԿ��� Revision���� ���� ��� Error ó��
            if (treeItem != null && !this.bwOption.isRevCreatable() && strRevID.equals("")) {

                treeItem.setStatus(STATUS_ERROR, this.getTextBundle("RevisionIDRequired", "MSG", dlgClass));
            } else if (treeItem != null && strRevID.equals("")) {

                treeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID, NEW_ITEM_REV);
                treeItem.setText(TREEITEM_COMLUMN_REVISION, NEW_ITEM_REV);
            }

            // Revision ID ��ȿ�� Check
            if (strRevID.equals("")) {
                treeItem.setStatus(STATUS_ERROR, this.getTextBundle("RevisionInvalid", "MSG", dlgClass));
            }

            // DataSet ������ ����..
            if (!CustomUtil.isEmpty(strDatasetFilePath)) {
                // int nFileCnt = 1;

                // ������ ������ �����ϴ� ���
                String[] szFilePath = StringUtil.getSplitString(strDatasetFilePath, ",");

                for (int i = 0; i < szFilePath.length; i++) {
                    ManualTreeItem dsTreeItem = null;
                    BWItemData stcDataData = new BWItemData();

                    // ���� Row�� Item/Dataset ��� �����ϴ� ���
                    if (treeItem != null) {
                        dsTreeItem = new ManualTreeItem(treeItem, treeItem.getItems().length, ManualTreeItem.ITEM_TYPE_TCDATASET, strItemID);

                    }
                    // Row�� DataSet�� �����ϴ� ���
                    else {
                        ManualTreeItem parentTreeItem = this.getParentTreeItem();
                        if (parentTreeItem != null) {
                            dsTreeItem = new ManualTreeItem(parentTreeItem, parentTreeItem.getItems().length, ManualTreeItem.ITEM_TYPE_TCDATASET, parentTreeItem.getItemID());
                        } else {
                            dsTreeItem = new ManualTreeItem(this.tree, SWT.NONE, ManualTreeItem.ITEM_TYPE_TCDATASET, strItemID);
                            dsTreeItem.setLevel(0);
                            dsTreeItem.setStatus(STATUS_ERROR, super.getTextBundle("ParentNotExist", "MSG", super.dlgClass));
                        }

                    }

                    stcDataData.setItemData(CLASS_TYPE_DATASET, DATASET_ATTR_FILEPATH, szFilePath[i]);

                    dsTreeItem.setBWItemData(stcDataData);
                    this.setTreeItemData(dsTreeItem, CLASS_TYPE_DATASET);
                    dsTreeItem.setText(TREEITEM_COMLUMN_ITEMID, szFilePath[i]);
                }

            }
            // Level ���� -1(Dataset ������ �����ϴ� Row)�� ��� �ݵ�� Dataset Type,Name�� �����ؾ� ��.
            else if (nLevel < 0 && CustomUtil.isEmpty(strDatasetFilePath)) {
                ManualTreeItem dsTreeItem = new ManualTreeItem(this.tree, SWT.NONE, ManualTreeItem.ITEM_TYPE_TCDATASET, "");
                dsTreeItem.setLevel(0);
                dsTreeItem.setStatus(STATUS_ERROR, "Excel " + r + this.getTextBundle("ExcelRowInvaild", "MSG", dlgClass));
            }

            String strEndPos = getCellText(row.getCell(ITEM_END_X_POS));
            if (strEndPos.equals(ITEM_END_VALUE)) {
                break;
            }

        }
    }

    /**
     * Excel�� ��õ� �Ӽ����� TreeItem Cell�� �ݿ�
     * 
     * @param treeItem
     */
    protected void setTreeItemData(ManualTreeItem treeItem, String strItemType) {
        for (int i = 0; i < super.attrMappingMap.size(); i++) {
            String[] szItemAttrMapping = super.attrMappingMap.get(i);

            // Not Applicable Check
            if (szItemAttrMapping == null || szItemAttrMapping[0].equals("N/A"))
                continue;

            if (strItemType.equals(szItemAttrMapping[0])) {
                String strValue = treeItem.getBWItemAttrValue(szItemAttrMapping[0], szItemAttrMapping[1]);
                // strValue = strValue.replaceAll("\n", " ");
                treeItem.setText(i, strValue);

            }

        }
    }

    /**
     * ���� Parent TreeItem�� ã�� Excel File�� ��õ� �������� Partent Item�� ã��� �����Ƿ� �˻��Ͽ� ã��
     * 
     * @param nLevel
     *            : Parent TreeItem Level
     * @return
     * 
     */
    public ManualTreeItem getParentTreeItem(int nLevel) {

        for (int i = (this.itemList.size() - 1); i > -1; i--) {
            ManualTreeItem parentTreeItem = this.itemList.get(i);
            int nParentLevel = parentTreeItem.getLevel();

            if ((nLevel - 1) == nParentLevel)
                return parentTreeItem;

        }

        return null;
    }

    /**
     * ���� Parent TreeItem�� ã�� Excel File�� ��õ� �������� Partent Item�� ã��� �����Ƿ� �˻��Ͽ� ã��
     * 
     * @param nLevel
     *            : Parent TreeItem Level
     * @return
     * 
     */
    public ManualTreeItem getParentTreeItem() {

        for (int i = (this.itemList.size() - 1); i > -1; i--) {
            ManualTreeItem parentTreeItem = this.itemList.get(i);

            if (parentTreeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCITEM)) {
                return parentTreeItem;
            }
        }

        return null;
    }

    /**
     * �˻� Button Click�� ����
     */
    @Override
    public void selectTarget() throws Exception {
        super.selectTargetFile();

        if (super.strImageRoot == null || super.strImageRoot.equals(""))
            return;

        super.searchButton.setEnabled(false);

        // Excel Data Loading
        load();

        // Validation ���� �� �����ư Ȱ��ȭ
        if (validate())
            super.executeButton.setEnabled(true);

        // super.executeButton.setEnabled(true);
    }

    /**
     * Validation Check
     */
    @Override
    public boolean validate() throws Exception {
        // Validation ��ó��
        validatePre();

        // Upload File Check
        TreeItem[] szTopItems = super.tree.getItems();

        if (szTopItems == null || szTopItems.length == 0) {
            return false;
        }

        try {
            TCComponentType cItemType = session.getTypeComponent(this.strTargetItemType);
            this.checkAttribute((ManualTreeItem) szTopItems[0], cItemType, CLASS_TYPE_ITEM);

            TCComponentType cDatasetType = session.getTypeComponent("Dataset");
            this.checkAttribute((ManualTreeItem) szTopItems[0], cDatasetType, CLASS_TYPE_DATASET);

            TCComponentType cBomLineType = session.getTypeComponent("BOMLine");
            this.checkAttribute((ManualTreeItem) szTopItems[0], cBomLineType, CLASS_TYPE_BOMLINE);

        } catch (TCException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < szTopItems.length; i++) {

            // Top TreeItem
            ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];

            // End ǥ�� Ȯ�� �ؾ� ��..
            // �ʼ� �Ӽ� ���� Ȯ�� �ؾ� ��..

            // File ���� ���� Check
            this.checkFile(topTreeItem);

            // this.checkDuplicateItem(topTreeItem);

            // Item ����/���� ��� �Ұ��� ��� Item�� //
            if (!this.bwOption.isItemCreatable() && !this.bwOption.isItemModifiable()) {
                // importData(topTreeItem);
            }

        }

        // Validation ��ó��
        validatePost();

        // Error Count, Warning Count
        int[] szErrorCount = { 0, 0 };
        for (int i = 0; i < szTopItems.length; i++) {
            // Top TreeItem
            ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
            // Error Count ����
            this.getErrorCount(topTreeItem, szErrorCount);
        }

        this.nWraningCount = szErrorCount[1];

        super.text.append("\n----------------------------------\n");
        super.text.append("Warning : " + szErrorCount[1] + "\n\n");
        super.text.append("Error : " + szErrorCount[0]);
        super.text.append("\n----------------------------------\n");

        if (szErrorCount[0] > 0) {
            super.text.append(super.getTextBundle("ErrorOccured", "MSG", super.dlgClass));
            return false;
        } else
            return true;

    }

    private void checkAttribute(ManualTreeItem treeItem, TCComponentType compType, String strType) {
        HashMap<String, Integer> attrIndexMap = this.headerModel.getModelAttrs(strType);
        Object[] attrNames = attrIndexMap.keySet().toArray();

        for (int i = 0; i < attrNames.length; i++) {
            String strAttrName = (String) attrNames[i];

            // �Ӽ� Update�� ���ʿ��� �Ӽ��� Skip �Ѵ�.
            boolean isSkipAttr = false;
            for (int j = 0; j < szSkipAttrs.length; j++) {
                if (strAttrName.equals(szSkipAttrs[j])) {
                    isSkipAttr = true;
                    break;
                }
            }

            if (isSkipAttr)
                continue;

            if (!compType.isValidPropertyName(strAttrName)) {
                treeItem.setStatus(STATUS_ERROR, strType + "." + strAttrName + this.getTextBundle("AttrInvalid", "MSG", dlgClass));
                continue;
            }
        }

    }

    /**
     * Upload ���� ���翩�� Check
     * 
     * @param treeItem
     */
    private void checkFile(ManualTreeItem treeItem) throws Exception {

        if (treeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCDATASET)) {

            String strFilePath = treeItem.getBWItemAttrValue(CLASS_TYPE_DATASET, DATASET_ATTR_FILEPATH);

            String strFileExt = strFilePath.substring(strFilePath.lastIndexOf(".") + 1, strFilePath.length());

            // ���� Dataset�� ��ȿ Ȯ���� Array
            String strDataSetType = this.bwOption.getDataSetType(strFileExt);
            if (CustomUtil.isEmpty(strDataSetType)) {
                // throw new Exception(super.getTextBundle("DSTypeInvalid", "MSG", super.dlgClass));
                treeItem.setStatus(STATUS_ERROR, super.getTextBundle("DSTypeInvalid", "MSG", super.dlgClass));
                return;
            }

            String[] szDataSetExts = this.bwOption.getDataRefExts(strDataSetType);

            if (szDataSetExts == null || szDataSetExts.length == 0) {
                treeItem.setStatus(STATUS_ERROR, this.getTextBundle("DSTypeInvalid", "MSG", dlgClass));
                return;
            }

            for (int i = 0; i < szDataSetExts.length; i++) {
                String strFileFullPath = "";
                if (strFilePath.startsWith("/")) {
                    strFileFullPath = this.strImageRoot + strFilePath;
                } else if (strFilePath.indexOf(":") < 0) {
                    strFileFullPath = this.strImageRoot + File.separatorChar + strFilePath;
                } else {
                    strFileFullPath = strFilePath;
                }

                File importFile = new File(strFileFullPath);
                if (!importFile.exists()) {
                    treeItem.setStatus(STATUS_ERROR, strFileFullPath + this.getTextBundle("CanNotFindFile", "MSG", dlgClass));
                } else {
                    treeItem.setBWItemAttrValue(CLASS_TYPE_DATASET, DATASET_ATTR_FILEPATH, strFileFullPath);
                }
            }

            return;
        }

        TreeItem[] childItems = treeItem.getItems();
        for (int i = 0; i < childItems.length; i++) {
            ManualTreeItem cItem = (ManualTreeItem) childItems[i];
            this.checkFile(cItem);

        }

    }

    /**
     * Top Level�� ������ ���� Level������
     * ItemID, RevID�� ���� Item�� ������ �� ����.
     * 
     * @param treeItem
     */
    protected void checkDuplicateItem(ManualTreeItem treeItem) {
        TreeItem[] childItems = treeItem.getItems();

        for (int i = 0; i < childItems.length; i++) {
            ManualTreeItem cItem = (ManualTreeItem) childItems[i];
            String strItemID = cItem.getBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID);

            for (int j = 0; j < childItems.length; j++) {
                if (i == j)
                    continue;

                if (!cItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCITEM))
                    continue;

                if (strItemID.equals(NEW_ITEM_ID))
                    continue;

                ManualTreeItem ccItem = (ManualTreeItem) childItems[j];

                String itemID = ccItem.getBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID);
                if (strItemID.equals(itemID)) {
                    cItem.setStatus(STATUS_ERROR, this.getTextBundle("DuplicateItemID", "MSG", dlgClass));
                    break;
                }

            }

            if (cItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCITEM))
                this.checkDuplicateItem(cItem);

        }

    }

    /**
     * Error, Warning Count ���
     * 
     * @param treeItem
     * @param szError
     */
    protected void getErrorCount(ManualTreeItem treeItem, int[] szError) {
        if (treeItem.getStatus() == STATUS_ERROR)
            szError[0]++;
        else if (treeItem.getStatus() == STATUS_WARNING)
            szError[1]++;

        TreeItem[] childItems = treeItem.getItems();
        for (int i = 0; i < childItems.length; i++) {
            ManualTreeItem cItem = (ManualTreeItem) childItems[i];

            this.getErrorCount(cItem, szError);

        }

    }

    /**
     * Template ���� Download Button Click�� ����
     * Download ��� ���� �� �ش� ������ �����ޱ�..
     */
    @Override
    public void downLoadTemplate() {
        if (this.strTemplateDSName.equals("")) {
            MessageBox.post(this.shell, this.getTextBundle("TemplateDSInvalid", "MSG", dlgClass), "Notification", 2);
            return;
        }

        try {

            FileDialog fDialog = new FileDialog(this.shell, SWT.SINGLE | SWT.SAVE);
            fDialog.setFilterNames(new String[] { "Excel File" });
            fDialog.setFileName(strTemplateDSName);
            // *.xls, *.xlsx Filter ����
            fDialog.setFilterExtensions(new String[] { "*.xlsx" });
            String strRet = fDialog.open();

            if ((strRet == null) || (strRet.equals("")))
                return;

            String strfileName = fDialog.getFileName();
            if ((strfileName == null) || (strfileName.equals("")))
                return;

            String strDownLoadFilePath = fDialog.getFilterPath() + File.separatorChar + strfileName;

            File checkFile = new File(strDownLoadFilePath);
            if (checkFile.exists()) {

                org.eclipse.swt.widgets.MessageBox box1 = new org.eclipse.swt.widgets.MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
                box1.setMessage(strDownLoadFilePath + this.getTextBundle("FileExist", "MSG", dlgClass));

                if (box1.open() != SWT.OK) {
                    return;
                }
            }

            File tempFile = SYMTcUtil.getTemplateFile(this.session, this.strTemplateDSName, null);

            if (checkFile.exists())
                checkFile.delete();

            tempFile.renameTo(new File(strDownLoadFilePath));

            MessageBox.post(this.shell, strDownLoadFilePath + " ���� �ٿ�ε尡 �Ϸ�Ǿ����ϴ�.", "Notification", 2);
        } catch (Exception e) {
            MessageBox.post(this.shell, e.toString(), "Notification", 2);
        }

    }

    /**
     * Option Button Ȱ��ȭ
     * �⺻���� Option�� Property���Ͽ� ���ǵǾ� ������
     * �����ڰ� ��Ȳ�� ���� Option�� ������ �� �ִ� ��� ����.
     * 
     * ���� : �Ϲ� ����ڿ��� �ش� ����� �ο� ���� �ʾƾ� �մϴ�.(������, Dummy Data, Bom���� ���� ���� �ɰ��� ������ ���� �� �ֽ��ϴ�.)
     * 
     */
    public void enableOptionButton() {
        Button optionButton = new Button(super.excelFileGroup, SWT.NONE);
        optionButton.setBounds(500, 26, 60, 22);
        optionButton.setText(this.getTextBundle("optionButton", null, this.dlgClass));
        // templateButton.setImage(Activator.imageDescriptorFromPlugin("com.stc.cms", "icons/exceldataset_16.png").createImage());

        optionButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                BWUOptionDialog dialog = new BWUOptionDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, bwOption);
                dialog.open();

            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

    }

    /** �ʿ�� ���� Ŭ�������� ���� */
    @Override
    public String generateNewID() throws Exception {
        throw new Exception(this.getTextBundle("NewIDLogicInvalid", "MSG", dlgClass));
    }

    /** �ʿ�� ���� Ŭ�������� ���� */
    @Override
    public void validatePre() throws Exception {

    }

    /** �ʿ�� ���� Ŭ�������� ���� */
    @Override
    public void validatePost() throws Exception {

    }

    /** �ʿ�� ���� Ŭ�������� ���� */
    @Override
    public void loadPre() throws Exception {

    }

    /** �ʿ�� ���� Ŭ�������� ���� */
    @Override
    public void loadPost() throws Exception {

    }

    /** �ʿ�� ���� Ŭ�������� ���� */
    @Override
    public void executePre() throws Exception {

    }

    /** �ʿ�� ���� Ŭ�������� ���� */
    @Override
    public void executePost() throws Exception {

    }

    /** �ʿ�� ���� Ŭ�������� ���� */
    @Override
    public void importDataPost(ManualTreeItem treeItem) throws Exception {

    }

    public class ExecutionJob extends Job {
        TreeItem[] szTopItems;

        public ExecutionJob(String name, TreeItem[] szTopItems) {
            super(name);
            this.szTopItems = szTopItems;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {

            try {
                for (int i = 0; i < szTopItems.length; i++) {
                    // Top TreeItem
                    ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];

                    // Item,DataSet ����
                    importData(topTreeItem);

                    // Item,Dataset Release
                    ReleaseData(topTreeItem);
                    
                    if (bwOption.isBOMAvailable()) {
                        // BOM Structure ����
                        importBOM(topTreeItem);
                    }
                }

                shell.getDisplay().syncExec(new Runnable() {

                    public void run() {
                        shell.setCursor(waitCursor);
                        // **************************************//
                        // Txt Upload Report ���� //
                        // --------------------------------------//

                        // Report Factory Instance : HeaderNames, HeaderWidths, Num Column
                        // Display Flag, Level Column Display Flag
                        TxtReportFactory rptFactory = generateReport(true, true);

                        if (strImageRoot == null || "".equals(strImageRoot)) {
                            strImageRoot = "c:/temp";

                            File temp = new File(strImageRoot);
                            if (!temp.exists()) {
                                temp.mkdirs();
                            }
                        }

                        String strDate = DateUtil.getClientDay("yyMMddHHmm");
                        String strFileName = "Import_" + strDate + ".log";
                        // Upload Log File FullPath
                        strLogFileFullPath = strImageRoot + File.separatorChar + strFileName;

                        // Import Log File ����
                        rptFactory.saveReport(strLogFileFullPath);

                        // Error Count, Warning Count
                        int[] szErrorCount = { 0, 0 };
                        for (int i = 0; i < szTopItems.length; i++) {

                            // Top TreeItem
                            ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
                            getErrorCount(topTreeItem, szErrorCount);
                        }
                        nWraningCount = szErrorCount[1];

                        text.append("--------------------------\n");
                        text.append("Warning : " + szErrorCount[1] + "\n\n");
                        text.append("Error : " + szErrorCount[0] + "\n\n\n");
                        text.append("[" + strLogFileFullPath + "] " + getTextBundle("LogCreated", "MSG", dlgClass) + "\n\n");

                        if (szErrorCount[0] > 0) {
                            text.append(getTextBundle("ActionNotCompleted", "MSG", dlgClass) + "\n");
                            MessageBox.post(shell, getTextBundle("ActionNotCompleted", "MSG", dlgClass), "Error", 2);

                        } else {
                            text.append(getTextBundle("ActionCompleted", "MSG", dlgClass) + "\n");
                            MessageBox.post(shell, getTextBundle("ActionCompleted", "MSG", dlgClass), "Notification", 2);
                        }
                        text.append("--------------------------\n");

                        searchButton.setEnabled(true);
                        viewLogButton.setEnabled(true);

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    // ���� �� ó��
                    executePost();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                shell.getDisplay().syncExec(new Runnable() {

                    public void run() {
                        shell.setCursor(arrowCursor);
                    }
                });

            }

            return new Status(IStatus.OK, "Exporting", "Job Completed");

        }

    }

    
    /**
     * Release
     * @param treeItem
     * @throws Exception
     */
    private void ReleaseData(ManualTreeItem treeItem) throws Exception {
        String strItemType = treeItem.getItemType();

        // Item Type �� ���
        if (strItemType.equals(ManualTreeItem.ITEM_TYPE_TCITEM)) {

            try {
                // ItemRevision �� �̹� �����ϴ��� �˻��Ѵ�.
                String strItemID = treeItem.getItemID();
                TCComponentItem item = SYMTcUtil.findItem(session, strItemID);
                TCComponentItemRevision itemRevision = item.getLatestItemRevision();
                if(!CustomUtil.isReleased(itemRevision)) {
                    // Item Revision Release
                    TCComponent processComponent = ResourceUtilities.releaseItemRevision(itemRevision);
                    if (processComponent == null) {
                        // Release�� �����Ͽ����ϴ�.
                        throw new Exception("Failed Release.");
                    } else {
                        this.syncItemState(treeItem, STATUS_COMPLETED, super.getTextBundle("ItemRevReleased", "MSG", dlgClass));
                    }
                }

            } catch (Exception e) {

                this.syncItemState(treeItem, STATUS_ERROR, e.toString());
                e.printStackTrace();

                // BOM ���� ����� �����ϸ�.. Item Import Error �߻��� �۾��� �ߴ��մϴ�.
                if (this.bwOption.isBOMAvailable()) {
                    throw new Exception("�۾��� �ߴ��մϴ�.");
                }

            } finally {
                importDataPost(treeItem);
            }
        }            
    }


}
