/**
 * Part �Ӽ� �ϰ� Upload Dialog
 * 
 * --------------- Upload Option ---------------------- 
 * # ItemID, Revision ���� ��뿩��(������ ID �űԹ߹�)
 * BWPartImpDialog.opt.isItemIDBlankable = false
 * 
 * # AutoCAD Validate Check ����( Migration���� ������� ���� )
 * BWPartImpDialog.opt.isAutoCADValidatable = false
 * 
 * # Item ���� ����
 * BWPartImpDialog.opt.isItemCreatable = true
 * # Item ���� ����
 * BWPartImpDialog.opt.isItemModifiable = true
 *   
 * # Item Revision ���� ����
 * BWPartImpDialog.opt.isRevCreatable = true
 * # Item Revision ���� ����
 * BWPartImpDialog.opt.isRevModifiable = true
 *   
 * # DataSet ��� ����
 * BWPartImpDialog.opt.isDSAvailable = false
 * # DataSet ���� �� ���� ����(��ü)
 * BWPartImpDialog.opt.isDSChangable = false
 * 
 *   
 * # BOM ��� ����
 * BWPartImpDialog.opt.isBOMAvailable = false
 * # BOM �籸�� ����(BOM Structure ���� �� �籸��)
 * BWPartImpDialog.opt.isBOMRearrange = false
 * # BOM Line �Ӽ� ���� ����
 * BWPartImpDialog.opt.isBOMLineModifiable = false
 * --------------------------------------------------- 
 * 
 * �۾�Option�� bundlework_locale_ko_KR.properties�� ���� �Ǿ� ����
 */
package com.kgm.common.bundlework.imp;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import com.kgm.common.SYMCClass;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.bundlework.bwutil.BWItemData;
import com.kgm.common.bundlework.bwutil.BWItemModel;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

@SuppressWarnings({"unused"})
public class BWPartImpDialog extends BWXLSImpDialog {

    String[] szClass = null;
    String[] szAttr = null;
    String[] szDbColumn = null;
    String[][] szLovNames = null;

    private static HashMap<String, String> itemTypeMap;

    /* Load Button */
    public Button loadButton; 
    public Combo comboObjectType;
    public Combo comboSeqType;

    private String objectType;
    private String seqType;
    
    public int totalItemCnt;
    
    public static final String ITEM_ATTR_CNT = "ROW_CNT_ITEM";
    /**
     * DB Update�� ITem ID ���� Function ��� -> Function Master ��� -> MIG_FUNCTION
     * ���̺� ITEM_ID ���� ���� ��� : treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM,
     * DB_ATTR_STR)
     */
    private static final String DB_ATTR_STR = "DB_UPDATE_ITEM_ID";
    
    private SYMCRemoteUtil remote;

    public BWPartImpDialog(Shell parent, int style, Class<?> cls) {
        super(parent, style, cls); 
    }

    public BWPartImpDialog(Shell parent, int style) {
        super(parent, style, BWPartImpDialog.class);
    }

    /**
     * Dialog UI Create
     * 
     * @method dialogOpen
     * @date 2013. 2. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @Override
    public void dialogOpen() {
        // super.dialogOpen();
        if(remote == null) {
            remote = new SYMCRemoteUtil();
        }
        // BOMLine ���� �ɼ�
        this.bwOption.setBOMLineModifiable(false);
        this.bwOption.setBOMRearrange(false);
        this.bwOption.setBOMAvailable(false);
        // Dataset ���� �ɼ�
        this.bwOption.setDSAvailable(true);
        this.bwOption.setDSChangable(true);
        // Item ���� �ɼ�        
        this.bwOption.setItemModifiable(true);
        // Revision ���� �ɼ�
        this.bwOption.setRevCreatable(true);
        this.bwOption.setRevModifiable(true);

        this.excelFileGroup.setBounds(10, 10, 769, 60);
        Label lblObjectType = new Label(excelFileGroup, SWT.NONE);
        lblObjectType.setBounds(20, 26, 70, 22);
        lblObjectType.setText("Object Type");

        this.comboObjectType = new Combo(excelFileGroup, SWT.READ_ONLY);
        this.comboObjectType.setItems(getItemTypeMap().keySet().toArray(new String[getItemTypeMap().size()]));
        this.comboObjectType.select(0);
        this.comboObjectType.setBounds(90, 26, 150, 22);

        Label lblSeqType = new Label(excelFileGroup, SWT.NONE);
        lblSeqType.setText("Seq Type");
        lblSeqType.setBounds(260, 26, 60, 22);

        this.comboSeqType = new Combo(excelFileGroup, SWT.READ_ONLY);
        this.comboSeqType.setItems(new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" });
        this.comboSeqType.select(0);
        this.comboSeqType.setBounds(320, 26, 77, 22);

        this.loadButton = new Button(excelFileGroup, SWT.NONE);
        this.loadButton.setText("Load");
        this.loadButton.setBounds(470, 26, 77, 22);

        this.logGroup.setBounds(10, 75, 863, 481);
        this.tree.setBounds(10, 22, 843, 300);
        this.text.setBounds(10, 330, 843, 141);
        this.executeButton.setBounds(338, 576, 77, 22);
        this.cancelButton.setBounds(459, 576, 77, 22);
        this.viewLogButton.setBounds(750, 576, 120, 22);  

        this.shell.open();
        this.shell.layout();

        // super.enableOptionButton();

        this.comboObjectType.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        tree.removeAll();
                        executeButton.setEnabled(false);
                    }
                });
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

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

    /**
     * 
     * MaualTreeItem�� ����Ǿ� �ִ� ��� ������ ������ Upload ��
     */
    @Override
    public void load() throws Exception {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    // Load ��ó��
                    loadPre();
                    // ManualTreeItem List
                    itemList = new ArrayList<ManualTreeItem>();
                    // TCComponentItemRevision List
                    tcItemRevSet = new HashMap<String, TCComponentItemRevision>();
                    shell.setCursor(waitCursor);
                    tree.removeAll();
                    objectType = comboObjectType.getItem(comboObjectType.getSelectionIndex());
                    seqType = comboSeqType.getItem(comboSeqType.getSelectionIndex());
                    // Header ���� Loading
                    loadHeader();
                    // Item Sheet Data Loading
                    loadData();
                    
                    
                    if (validate()) {
                        executeButton.setEnabled(true);
                    }
                    executeButton.setEnabled(true);
                        
                        
                        
                    // }
                } catch (Exception e) {
                    MessageBox.post(shell, e.getMessage(), "Notification", 2);
                    e.printStackTrace();
                } finally {
                    // Load ��ó��
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
     * ��� ����
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
        final String objectType = comboObjectType.getItem(comboObjectType.getSelectionIndex());
        this.headerModel = new BWItemModel();
        // ������ TC Object ITEM Type�� ����
        this.strTargetItemType = getItemTypeMap().get(objectType);
        // �÷� ������ ����
        this.setHeader(objectType);
        if (szClass == null) {
            throw new Exception("Object Type is invalid.");
        }
        for (int i = 0; i < szClass.length; i++) {
            this.headerModel.setModelData(szClass[i], szAttr[i], new Integer(i));
        }
    }

    /**
     * �÷� ������ ����
     * 
     * @method setHeader
     * @date 2013. 2. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setHeader(String objectType) {
        this.szClass = null;
        this.szAttr = null;
        this.szDbColumn = null;
        this.szLovNames = null;
        /**
         * this.szClass -> ���̺� ��� �÷� �� this.szAttr -> ���� TC Property �Ӽ���
         * this.szDbColumn -> ���� TC Property �Ӽ���(this.szAttr)�� ���ε��� DB �÷���
         * this.szLovNames -> ���� TC Property �Ӽ���(this.szAttr)�� ���ε��� TC LOV
         * (Validation üũ��)
         */        
        if (SYMCClass.S7_PROJECTTYPE.equals(this.strTargetItemType)) {
            this.setProjectItemAttr();
        } else if (SYMCClass.S7_PRODUCTPARTTYPE.equals(this.strTargetItemType)) {
            this.setProductItemAttr();
        } else if (SYMCClass.S7_FNCPARTTYPE.equals(this.strTargetItemType)) {
            this.setFunctionItemAttr();
        } else if (SYMCClass.S7_FNCMASTPARTTYPE.equals(this.strTargetItemType)) {
            this.setFunctionMasterItemAttr();
        } else if (SYMCClass.S7_STDPARTTYPE.equals(this.strTargetItemType)) {
            this.setStdPartItemAttr();
        } else if (SYMCClass.S7_VARIANTPARTTYPE.equals(this.strTargetItemType)) {
            this.setVariantItemAttr();
        } else if (SYMCClass.S7_VEHPARTTYPE.equals(this.strTargetItemType)) {
            this.setVehPartItemAttr();
        } else if (SYMCClass.S7_MATPARTTYPE.equals(this.strTargetItemType)) {
            this.setMaterialItemAttr();
        } else if (SYMCClass.S7_TECHDOCTYPE.equals(this.strTargetItemType)) {
            this.setTechDocItemAttr();
        } else {
            this.setClearAttr();
        }
    }

    /**
     * Project Item(S7_Project) �Ӽ� ����
     * 
     * @method setProjectItemAttr 
     * @date 2013. 4. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setProjectItemAttr() {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", "Revision", "Revision", "Revision" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_VEHICLE_NO", "s7_IS_NEW", "s7_BASE_PRJ" , "s7_IS_VEHICLE_PRJ"  };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "NAME", "S7_VEHICLE_NO", "S7_IS_NEW", "S7_BASE_PRJ", "S7_IS_VEHICLE_PRJ" };
        this.szLovNames = null;
    }
    
    /**
     * Product Item(S7_Product) �Ӽ� ����
     * 
     * @method setProductItemAttr
     * @date 2013. 2. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setProductItemAttr() {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", "Revision" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_PROJECT_CODE", "s7_MATURITY" };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "NAME", "S7_PROJECT_CODE", "S7_MATURITY" };
        this.szLovNames = null;
    }

    /**
     * Vehicle Part Item(S7_Vehpart) �Ӽ� ����
     *
     * ** ���� - ���� MIG_VEHPART ���̺��� S7_SYSTEM_CODE �÷��� TC�� s7_BUDGET_CODE �Ӽ��� �ɷ������Ƿ� ���� �Ѵ�. **
     * 
     * @method setVehPartItemAttr
     * @date 2013. 2. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setVehPartItemAttr() {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", /*"Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision",*/ "Revision", "Revision",
                "Revision", "Revision", "Item", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision",
                "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_KOR_NAME", /*"s7_MAIN_NAME", "s7_SUB_NAME", "s7_LOC1_FR", "s7_LOC2_IO", "s7_LOC3_UL", "s7_LOC4_EE",
                "s7_LOC5_LR",*/ "s7_PART_TYPE", "s7_PROJECT_CODE", "s7_STAGE", "s7_DISPLAY_PART_NO", "uom_tag", "s7_BUDGET_CODE", "s7_DRW_STAT", "s7_SHOWN_PART_NO", "s7_DRW_SIZE", "s7_REFERENCE",
                "s7_VPM_ECO_NO", "s7_REGULAR_PART", "s7_REGULATION", "s7_COLOR", "s7_COLOR_ID", "s7_MATERIAL", "s7_ALT_MATERIAL", "s7_THICKNESS", "s7_ALT_THICKNESS", "s7_FINISH", "s7_RESPONSIBILITY",
                "s7_BOUNDINGBOX", "s7_EST_WEIGHT", "s7_CAL_SURFACE", "s7_CAL_WEIGHT", "s7_AS_END_ITEM", "s7_ACT_WEIGHT", "s7_DVP_RESULT", "s7_CHANGE_DESCRIPTION", "s7_CAT_V4_TYPE", "object_desc" };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "NAME", "S7_KOR_NAME", /*"S7_MAIN_NAME", "S7_SUB_NAME", "S7_LOC1_FR", "S7_LOC2_IO", "S7_LOC3_UL", "S7_LOC4_EE", "S7_LOC5_LR",*/
                "S7_PART_TYPE", "S7_PROJECT_CODE", "S7_STAGE", "S7_DISPLAY_PART_NO", "S7_UNIT", "S7_SYSTEM_CODE", "S7_DRW_STAT", "S7_SHOW_PART_NO", "S7_DRW_SIZE", "S7_REFERENCE", "S7_ECO_NO",
                "S7_REGULAR_PART", "S7_REGULATION", "S7_COLOR", "S7_COLOR_ID", "S7_MATTERIAL", "S7_ALT_MATERIAL", "S7_THICKNESS", "S7_ALT_THICKNESS", "S7_FINISH", "S7_RESPONSIBILITY",
                "S7_BOUNDINGBOX", "S7_EST_WEIGHT", "S7_CAL_SURFACE", "S7_CAL_WEIGHT", "S7_AS_END_ITEM", "S7_ACT_WEIGHT", "S7_DVP_RESULT", "S7_CHANGE_DESCRIPTION", "S7_CAT_V4_TYPE", "DESCRIPTION" };
        this.szLovNames = new String[][] { { "s7_PROJECT_CODE", "S7_PROJECT_CODE" }, { "s7_PART_TYPE", "S7_PART_ORIGIN" }, { "s7_STAGE", "S7_STAGE" }, { "uom_tag", "Unit of Measures" },
                { "s7_BUDGET_CODE", "S7_SYSTEM_CODE" }, { "s7_DRW_STAT", "S7_DRW_STAT" }, { "s7_DRW_SIZE", "S7_DRW_SIZE" }, { "s7_REGULAR_PART", "S7_REGULAR_PART" },
                { "s7_REGULATION", "S7_CATEGORY" }, { "s7_COLOR", "S7_COLOR" }, { "s7_COLOR_ID", "S7_COLOR_SECTION_ID" }, { "s7_RESPONSIBILITY", "S7_RESPONSIBILITY" } };
    }

    /**
     * Standard Part Item(S7_Stdpart) �Ӽ� ����
     * 
     * @method setStdPartItemAttr
     * @date 2013. 2. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setStdPartItemAttr() {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", "Revision", "Revision", "Revision", "Item", "Revision" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_DISPLAY_PART_NO", "s7_MATURITY", "object_desc", "s7_KOR_NAME", "uom_tag", "s7_ACT_WEIGHT" };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "NAME", "S7_DISPLAY_PART_NO", "S7_MATURITY", "DESCRIPTION", "S7_KOR_NAME", "S7_UNIT", "S7_ACT_WEIGHT" };
        this.szLovNames = null;
    }

    /**
     * Function Item(S7_Function) �Ӽ� ����
     * 
     * @method setFunctionItemAttr
     * @date 2013. 2. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setFunctionItemAttr() {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", "Revision" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_PROJECT_CODE", "s7_MATURITY" };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "NAME", "S7_PROJECT_CODE", "S7_MATURITY" };
        this.szLovNames = null;
    }

    /**
     * Function Master Item(S7_FunctionMast) �Ӽ� ����
     * 
     * @method setFunctionMasterItemAttr
     * @date 2013. 3. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setFunctionMasterItemAttr() {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", "Revision" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_PROJECT_CODE", "s7_MATURITY" };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "NAME", "S7_PROJECT_CODE", "S7_MATURITY" };
        this.szLovNames = null;
    }

    /**
     * Variant Item(S7_Variant) �Ӽ� ����
     * 
     * @method setVariantItemAttr
     * @date 2013. 2. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setVariantItemAttr() {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", "Revision" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_PROJECT_CODE", "s7_MATURITY" };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "NAME", "S7_PROJECT_CODE", "S7_MATURITY" };
        this.szLovNames = null;
    }

    /**
     * Material Item(S7_Material) �Ӽ� ����
     * 
     * @method setMaterialItemAttr
     * @date 2013. 2. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setMaterialItemAttr() {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision", "Revision" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_MATURITY", "s7_TYPE", "s7_SOURCE", "s7_SPEC_NUMBER", "s7_SES_CODE", "s7_KS_CODE", "s7_JIS_CODE", "s7_DIN_CODE", "s7_MB_CODE", "s7_SAE_CODE", "s7_GB_CODE", "s7_SUP_CODE", "s7_OTHER_CODE", "s7_DENSITY", "object_desc", "s7_ACTIVATION" };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "S7_TYPE", "S7_MATURITY", "S7_TYPE", "S7_SOURCE", "S7_SPEC_NUMBER", "S7_SES_CODE", "S7_KS_CODE", "S7_JIS_CODE", "S7_DIN_CODE", "S7_MB_CODE", "S7_SAE_CODE", "S7_GB_CODE", "S7_SUPPLIER_CODE", "S7_OTHER_CODE", "S7_DENSITY", "DESCRIPTION", "S7_ACTIVATION"};
        this.szLovNames = null;
    }

    /**
     * Tech Doc(S7_ENGDOC) �Ӽ� ����
     * 
     * @method setTechDocItemAttr
     * @date 2013. 3. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setTechDocItemAttr() {
        this.szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", "Revision", "Revision", "Revision" };
        this.szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_ENG_DOC_TYPE", "s7_MATURITY", "s7_SES_CLASSIFICATION", "DESCRIPTION" };
        this.szDbColumn = new String[] { "LEVEL", "ITEM_ID", "REVISION_ID", "NAME", "S7_TYPE", "S7_MATURITY", "S7_SES_CLASSIFICATION", "DESCRIPTION" };
        this.szLovNames = new String[][] { { "s7_ENG_DOC_TYPE", "S7_ENG_DOC_DIVISION" }, { "s7_SES_CLASSIFICATION", "S7_SES_CLASSIFICATION" } };
    }

    /**
     * Clear Attribute �Ӽ�
     * 
     * @method setClearAttr
     * @date 2013. 2. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setClearAttr() {
        this.szClass = null;
        this.szAttr = null;
        this.szDbColumn = null;
        this.szLovNames = null;
    }

    /**
     * DB �����͸� Load�Ѵ�.
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
        // ������Ʈ Item ����Ʈ �ʱ�ȭ
        if (this.itemList != null) {
            this.itemList.clear();
        }
        // Tree Item �ʱ�ȭ
        if (this.tree != null) {
            this.tree.removeAll();
        }
        // Log Text �ʱ�ȭ
        if (this.text != null) {
            this.text.setText("");
        }
        ArrayList<HashMap> rows = null;        
        try {
            TCSession session = CustomUtil.getTCSession();
            DataSet ds = new DataSet();
            ds.put("userId", session.getUser().getUserId());
            ds.put("objectType", objectType);
            ds.put("seqType", seqType);
            rows = (ArrayList<HashMap>) remote.execute("com.kgm.service.MigrationService", "getItemValidationList", ds);
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rows == null) {
            return;
        }
        totalItemCnt = rows.size(); // �� Item ��
        for (int r = 0; r < rows.size(); r++) {
            HashMap rMap = rows.get(r);
            BWItemData stcItemData = new BWItemData();                 
            for (int i = 0; i < szAttr.length; i++) {
                String strItemName = szClass[i];
                String strAttrName = szAttr[i];
                // String strAttrValue = rMap.get(szAttr[i]);
                String strAttrValue = "";
                // ITEM Type�� LEVEL������ �����Ƿ� ������ ����Ѵ�.
                if (i == 0) { // Column 0 => "0" ������ ���
                    strAttrValue = "0"; // Level : 0
                } else {
                    strAttrValue = getRowData(rMap, szDbColumn[i]);
                }
                stcItemData.setItemData(strItemName, strAttrName, strAttrValue);
            }
            // ITEM CNT
            stcItemData.setItemData(CLASS_TYPE_ITEM, ITEM_ATTR_CNT, (r+1)+"");            
            String strItemID = stcItemData.getItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID);
            String strRevID = stcItemData.getItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID);
            // Excel Item Row�� ����� TreeItem Object
            ManualTreeItem treeItem = null;
            int nLevel = super.getIntValue(stcItemData.getItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_LEVEL));                    
            // Top TreeItem
            treeItem = new ManualTreeItem(this.tree, this.tree.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strItemID);
            treeItem.setLevel(nLevel);            
            if (treeItem != null) {
                // Tree Table�� ǥ�õǴ� �Ӽ�
                // treeItem.setText(TREEITEM_COMLUMN_ITEMID,
                // stcItemData.getItemAttrValue(CLASS_TYPE_ITEM,
                // ITEM_ATTR_ITEMID));
                // treeItem.setText(TREEITEM_COMLUMN_REVISION,
                // stcItemData.getItemAttrValue(CLASS_TYPE_REVISION,
                // ITEM_ATTR_REVISIONID));
                treeItem.setBWItemData(stcItemData);
                setTreeItemData(treeItem, CLASS_TYPE_BOMLINE);
                setTreeItemData(treeItem, CLASS_TYPE_ITEM);
                setTreeItemData(treeItem, CLASS_TYPE_REVISION);
            }
            // ItemID ������ ������� �ʴ� �� ������ ���
            if (treeItem != null && !this.bwOption.isItemIDBlankable() && strItemID.trim().equals("")) {
                setErrorStatus(treeItem, "ItemIDRequired");
            }
            // ItemID ������ ���, ������ ���
            else if (treeItem != null && this.bwOption.isItemIDBlankable() && strItemID.trim().equals("")) {
                treeItem.setBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID, NEW_ITEM_ID);
                treeItem.setText(TREEITEM_COMLUMN_ITEMID, NEW_ITEM_ID);

            }
            // Revision ���� Option�� �ƴԿ��� Revision���� ���� ��� Error ó��
            if (treeItem != null && !this.bwOption.isRevCreatable() && strRevID.equals("")) {
                setErrorStatus(treeItem, "RevisionIDRequired");
            } else if (treeItem != null && strRevID.equals("")) {
                treeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID, NEW_ITEM_REV);
                treeItem.setText(TREEITEM_COMLUMN_REVISION, NEW_ITEM_REV);
            }
            // Revision ID ��ȿ�� Check
            if (strRevID.equals("")) {
                setErrorStatus(treeItem, "RevisionInvalid");
            }
            // ITEM ��� �� DB ���� ������Ʈ��
            treeItem.setBWItemAttrValue(CLASS_TYPE_ITEM, DB_ATTR_STR, strItemID);
            this.itemList.add(treeItem);
            // ����ڷṮ�� ����̸� �⺻ ���̺��������� ���ε� ��� ����Ʈ�� ������־� ����Ѵ�.
            if("MIG_ENGDOC".equals(objectType)) {
                HashMap dataset = new HashMap();
                dataset.put("ITEM_ID", strItemID);
                dataset.put("REVISION_ID", strRevID);
                dataset.put("FILE_PATH", rMap.get("FILE_PATH"));                
                ArrayList datasetList = new ArrayList();
                datasetList.add(dataset);                
                rMap.put("DATASET_FILE_LIST", datasetList);
            }
            // Dataset Item ����
            //this.createDatasetList(treeItem, rMap);            
        }
    }

    /**
     * Item������ Dataset List ������ ������ �������� �����Ѵ�.
     * 
     * @method createDatasetList 
     * @date 2013. 4. 10.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    private void createDatasetList(ManualTreeItem treeItem, HashMap rMap) {
        if("MIG_STDPART".equals(objectType)) {
            this.createDatasetTreeItem(treeItem, new HashMap());
            return;
        }
        if(rMap == null || !rMap.containsKey("DATASET_FILE_LIST")) {
            return;
        }
        List datasetList = (List)rMap.get("DATASET_FILE_LIST");        
        for (int i = 0; datasetList != null && i < datasetList.size(); i++) {
            this.createDatasetTreeItem(treeItem, (HashMap)datasetList.get(i));
            String strV4Type = StringUtil.nullToString((String)((HashMap)datasetList.get(i)).get("V4_TYPE"));
            String strDatasetType = StringUtil.nullToString((String)((HashMap)datasetList.get(i)).get("DATASET_TYPE"));
            if("model".equals(strDatasetType) && "Master".equals(strV4Type)) {                
                // JT Item ����
                createJTDatasetItem(treeItem, copyHashMap((HashMap)datasetList.get(i)));
            }
            if("X100_MIG_VEHPART".equals(objectType)) {
                if("CATPart".equals(strDatasetType) && "Master".equals(strV4Type)) {
                 // JT Item ����
                    createJTDatasetItem(treeItem, copyHashMap((HashMap)datasetList.get(i)));
                }
                if("CATDrawing".equals(strDatasetType) && "Detail".equals(strV4Type)) {
                    // PDF Item ����
                    createPdfDatasetItem(treeItem, copyHashMap((HashMap)datasetList.get(i)));
                }
            }
        }        
    }
    
   /**
    * HashMap Copy
    * 
    * @method copyHashMap 
    * @date 2013. 5. 2.
    * @param
    * @return HashMap
    * @exception
    * @throws
    * @see
    */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private HashMap copyHashMap(HashMap sourceMap) {
        if(sourceMap == null) {
            return null;
        }
        HashMap rtInfo = new HashMap();     
        for (Object key : sourceMap.keySet().toArray()) {
            rtInfo.put(key, sourceMap.get(key));
        }
        return rtInfo;
    }
    
    /**
     * Copy �� DatasetFile�� ������ JT ������ �����Ѵ�. 
     * 
     * @method createJTDatasetItem 
     * @date 2013. 5. 2.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void createJTDatasetItem(ManualTreeItem treeItem, HashMap datasetItemMap) {
        String strFilePath = StringUtil.nullToString((String) datasetItemMap.get("FILE_PATH"));
        if(!"".equals(strFilePath)) {
            if(strFilePath.lastIndexOf(".") > 0) {
                strFilePath = strFilePath.substring(0, strFilePath.lastIndexOf(".")) + ".jt";   // Ȯ���� ���� (.jt)
                datasetItemMap.put("FILE_PATH", strFilePath);
                this.createDatasetTreeItem(treeItem, datasetItemMap);
            }
        }
    }
    
    /**
     * Copy �� DatasetFile�� ������ PDF ������ �����Ѵ�. 
     * 
     * @method createPdfDatasetItem 
     * @date 2013. 5. 2.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void createPdfDatasetItem(ManualTreeItem treeItem, HashMap datasetItemMap) {
        String strFilePath = StringUtil.nullToString((String) datasetItemMap.get("FILE_PATH"));
        if(!"".equals(strFilePath)) {
            if(strFilePath.lastIndexOf(".") > 0) {
                strFilePath = strFilePath.substring(0, strFilePath.lastIndexOf(".")) + ".pdf";   // Ȯ���� ���� (.pdf)
                datasetItemMap.put("FILE_PATH", strFilePath);
                this.createDatasetTreeItem(treeItem, datasetItemMap);
            }
        }
    }
    
    /**
     * Dataset File ����
     * 
     * @method createDatasetTreeItem
     * @date 2013. 3. 11.
     * @param
     * @return ManualTreeItem
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    private ManualTreeItem createDatasetTreeItem(ManualTreeItem parentTreeItem, HashMap datasetInfo) {       
        String strItemID = parentTreeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID);
        String strRevID = parentTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID);       
        String strFilePath = StringUtil.nullToString((String) datasetInfo.get("FILE_PATH"));
        String strDatasetType = StringUtil.nullToString((String) datasetInfo.get("DATASET_TYPE"));
        String strV4Type = StringUtil.nullToString((String) datasetInfo.get("V4_TYPE"));
        String strCadPartRev = getCadPartRev(StringUtil.nullToString((String) datasetInfo.get("PART_REV")));
        // MIG_STDPART - S
        if("MIG_STDPART".equals(objectType)) {
            strFilePath = "c:/catpart/";
            String fileName =  strItemID.substring(0, 5) + "_" + strItemID.substring(5, strItemID.length()) + ".CATPart";            
            strFilePath = strFilePath + fileName;            
            File importFile = new File(strFilePath);
            if (!importFile.exists()) {
                return null;
            }
            ManualTreeItem dsTreeItem = new ManualTreeItem(parentTreeItem, parentTreeItem.getItems().length, ManualTreeItem.ITEM_TYPE_TCDATASET, parentTreeItem.getItemID());        
            BWItemData stcItemData = new BWItemData();
            stcItemData.setItemData(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID, strItemID);
            stcItemData.setItemData(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID, strRevID);        
            strDatasetType = "CATPart";
            strV4Type = "Detail";            
            stcItemData.setItemData(CLASS_TYPE_DATASET, DATASET_ATTR_FILEPATH, strFilePath);
            stcItemData.setItemData(CLASS_TYPE_DATASET, DATASET_ATTR_V4TYPE, strV4Type);
            stcItemData.setItemData(CLASS_TYPE_DATASET, DATASET_ATTR_CAD_PART_REV, strCadPartRev);
            dsTreeItem.setBWItemData(stcItemData);        
            super.setTreeItemData(dsTreeItem, CLASS_TYPE_DATASET);
            dsTreeItem.setText(TREEITEM_COMLUMN_ITEMID, strFilePath);
            this.itemList.add(dsTreeItem);
            return dsTreeItem;
        }
        // MIG_STDPART - E
        if (CustomUtil.isEmpty(strItemID) || CustomUtil.isEmpty(strRevID) || CustomUtil.isEmpty(strFilePath) || CustomUtil.isEmpty(strDatasetType)) {
            return null;
        }
        ManualTreeItem dsTreeItem = new ManualTreeItem(parentTreeItem, parentTreeItem.getItems().length, ManualTreeItem.ITEM_TYPE_TCDATASET, parentTreeItem.getItemID());        
        BWItemData stcItemData = new BWItemData();
        stcItemData.setItemData(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID, strItemID);
        stcItemData.setItemData(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID, strRevID);
        if(strFilePath.startsWith("/DATA_MIG/MigModels/")) {
            strFilePath = strFilePath.replace("/DATA_MIG", "T:");
        } else {        
            strFilePath = "S:" + strFilePath;
        }
        /*
        if("model".equals(strDatasetType)) {
            // model�� ��� �������� '/DATA_MIG' �����ϹǷ� �����̸��� �����Ѵ�.
            strFilePath = strFilePath.`("/DATA_MIG", "T:");                        
        } else if("CATPart".equals(strDatasetType)) {
           
        } else if("CATDrawing".equals(strDatasetType)) {
            strFilePath = "S:" + strFilePath;
        } else if("CATProduct".equals(strDatasetType)) {
            strFilePath = "S:" + strFilePath;
        } else {                        
        }
        */
        stcItemData.setItemData(CLASS_TYPE_DATASET, DATASET_ATTR_FILEPATH, strFilePath);
        stcItemData.setItemData(CLASS_TYPE_DATASET, DATASET_ATTR_V4TYPE, strV4Type);
        stcItemData.setItemData(CLASS_TYPE_DATASET, DATASET_ATTR_CAD_PART_REV, strCadPartRev);
        dsTreeItem.setBWItemData(stcItemData);        
        super.setTreeItemData(dsTreeItem, CLASS_TYPE_DATASET);
        dsTreeItem.setText(TREEITEM_COMLUMN_ITEMID, strFilePath);
        this.itemList.add(dsTreeItem);
        return dsTreeItem;
    }
    
    private String getCadPartRev(String strCadPartRev) {
        if("".equals(strCadPartRev) || "---".equals(strCadPartRev)) {
            return "";
        } else {
            return strCadPartRev.substring(1);
        }
    }


    /**
     * DB�� HashMap �����Ϳ� Item������ ��ų �����͸� �����Ͽ� �����Ų��. (LOV ������ ȣȯ�� ������ ���� ���)
     * 
     * @method beforInitMigData 
     * @date 2013. 4. 18.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    private String getRowData(HashMap rowData, String key) {
        String rtData = StringUtil.nullToString((rowData.get(key) != null)?rowData.get(key).toString():"");
        // MIG_MATERIAL
        if("MIG_MATERIAL".equals(objectType) && "S7_ACTIVATION".equals(key)) { // MIG_MATERIAL - s7_ACTIVATION �Ӽ��� �ʱ�ȭ (������ 'Y')
            return "Y";
        } else if("MIG_MATERIAL".equals(objectType) && "S7_MATURITY".equals(key)) { // MIG_MATERIAL - S7_MATURITY �Ӽ��� �ʱ�ȭ (������ 'Released')
            return "Released";
        } else if("MIG_MATERIAL".equals(objectType) && "REVISION_ID".equals(key)) { // MIG_MATERIAL - REVISION_ID �Ӽ��� �ʱ�ȭ (������ '000')
            return "000";
        }
        // MIG_PROJECT
        else if("MIG_PROJECT".equals(objectType) && "S7_VEHICLE_NO".equals(key)) { // MIG_PROJECT - S7_VEHICLE_NO �Ӽ��� �ʱ�ȭ ('')
            return "";
        }
        else if("MIG_PROJECT".equals(objectType) && "S7_IS_NEW".equals(key)) { // MIG_PROJECT - S7_IS_NEW �Ӽ��� �ʱ�ȭ ('')
            return "";
        }
        else if("MIG_PROJECT".equals(objectType) && "S7_IS_VEHICLE_PRJ".equals(key)) { // MIG_PROJECT - S7_IS_VEHICLE_PRJ �Ӽ��� �ʱ�ȭ ('')
            return "";
        }
        // MIG_STDPART
        /*
        if("MIG_STDPART".equals(objectType) && "S7_ACT_WEIGHT".equals(key)) { // MIG_STDPART - S7_ACT_WEIGHT �Ӽ��� �ʱ�ȭ ("0.1")
            return "0.01";
        }
        if("MIG_STDPART".equals(objectType) && "S7_KOR_NAME".equals(key)) { // MIG_STDPART - S7_KOR_NAME �Ӽ��� �ʱ�ȭ ("-")
            return "-";
        }
        */
        // MIG_VEHPART & X100_MIG_VEHPART
        else if("MIG_VEHPART".equals(objectType) || "X100_MIG_VEHPART".equals(objectType)) {
            if("S7_DRW_STAT".equals(key)) { // VEHPART - S7_DRW_STAT �Ӽ��� �ʱ�ȭ            
                if("".equals(rtData)) {
                    rtData = "."; 
                } else {
                    // ���ڸ� 1���� �����ͷ� ���� (H : Shown On -> H)
                    if(rtData.length() > 1) {
                        rtData = rtData.substring(0, 1);
                    }
                }
            } else if("S7_RESPONSIBILITY".equals(key)) { // MIG_VEHPART - S7_RESPONSIBILITY �Ӽ��� �ʱ�ȭ
                if(".".equals(rtData)) {
                    rtData = ""; 
                }
            } else if("S7_MATTERIAL".equals(key)) { // MIG_VEHPART - S7_MATTERIAL �Ӽ��� �ʱ�ȭ (�ڿ� �׻� �ʱ� ������ ��ȣ�� ���)
                if(!"".equals(rtData)) {
                    rtData = rtData + "/000"; 
                }
            } else if("S7_ALT_MATERIAL".equals(key)) { // MIG_VEHPART - S7_ALT_MATERIAL �Ӽ��� �ʱ�ȭ (�ڿ� �׻� �ʱ� ������ ��ȣ�� ���)
                if(!"".equals(rtData)) {
                    rtData = rtData + "/000"; 
                }
            } else if("S7_UNIT".equals(key)) { // VEHPART - S7_UNIT �Ӽ��� �ʱ�ȭ 
                if("".equals(rtData)) {
                    rtData = "EA";
                }         
            } else if("S7_STAGE".equals(key)) { // MIG_VEHPART - S7_STAGE �Ӽ��� �ʱ�ȭ ("P")
                rtData = "P";
            } else if("S7_REGULAR_PART".equals(key)) { // MIG_VEHPART - S7_REGULAR_PART �Ӽ��� �ʱ�ȭ ("R")
                rtData = "R";
            } 
        }
        return rtData;
    }

    /**
     * DB Migration ���̺�� TC OBJCT TYPE Mapping
     * 
     * @method getItemTypeMap
     * @date 2013. 2. 19.
     * @param
     * @return HashMap<String,String>
     * @exception
     * @throws
     * @see
     */
    public static HashMap<String, String> getItemTypeMap() {
        if (itemTypeMap == null) {
            itemTypeMap = new HashMap<String, String>();
            itemTypeMap.put("MIG_PROJECT", SYMCClass.S7_PROJECTTYPE);
            itemTypeMap.put("MIG_PRODUCT", SYMCClass.S7_PRODUCTPARTTYPE);
            itemTypeMap.put("MIG_FUNCTION", SYMCClass.S7_FNCPARTTYPE);
            // itemTypeMap.put("MIG_FUNCTIONMAST",
            // SYMCClass.S7_FNCMASTPARTTYPE);
            itemTypeMap.put("MIG_STDPART", SYMCClass.S7_STDPARTTYPE);
            //itemTypeMap.put("MIG_VARIANT", SYMCClass.S7_VARIANTPARTTYPE);
            itemTypeMap.put("MIG_VEHPART", SYMCClass.S7_VEHPARTTYPE);
            itemTypeMap.put("MIG_MATERIAL", SYMCClass.S7_MATPARTTYPE);
            itemTypeMap.put("MIG_ENGDOC", SYMCClass.S7_TECHDOCTYPE);
            itemTypeMap.put("X100_MIG_VEHPART", SYMCClass.S7_VEHPARTTYPE);
        }
        return itemTypeMap;
    }

    /**
     * ���� �� DB�� ���� - ���� ������� �ʰ� importDataPost �޼ҵ� ��� - 2013.04.29
     */
    /*
    @Override
    public void executePost() throws Exception {
        try {
            // Function ���̱׷��̼��̸� �ٽ� ���̱׷��̼� �� ��ŭ Function Master�� �����Ѵ�.
            if (SYMCClass.S7_FNCPARTTYPE.equals(this.strTargetItemType)) {
                createFunctionMasterItems();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // Function ���̱׷��̼��̸� Function Master ��� �� DB���¸� ������Ʈ�Ѵ�.
            // �� �ܿ��� ������ ������Ʈ
            if (!SYMCClass.S7_FNCPARTTYPE.equals(this.strTargetItemType)) {
                this.updateMigrationStatus();
            }

        }
    }
     */
    
    /**
     * Function Master ����
     * 
     * Function Item ID�� Function Master Item ID�������� ���� �� Function Master ����
     * 
     * @method createFunctionMasterItems
     * @date 2013. 3. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createFunctionMasterItems() throws Exception {
        this.strTargetItemType = SYMCClass.S7_FNCMASTPARTTYPE;
        for (int i = 0; i < this.itemList.size(); i++) {
            ManualTreeItem item = this.itemList.get(i);
            // Function Item ���� ������ ��� Function Master Item�� �������� �ʴ´�.
            if (STATUS_ERROR == item.getStatus()) {
                continue;
            }
            // Function Item ID�� Function Master Item ID�������� ����
            String strFunctionMasterItemId = getFunctionMasterItemId(item.getItemID());
            item.setItemID(strFunctionMasterItemId);
            item.setBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID, strFunctionMasterItemId);
            syncSetItemText(item, TREEITEM_COMLUMN_ITEMID, strFunctionMasterItemId);
        }
        // Migration Execute..
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Function Master�� ��� Item ID ���� ����
     * 
     * @method getFunctionMasterItemId
     * @date 2013. 3. 11.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getFunctionMasterItemId(String itemId) {
        if (CustomUtil.isEmpty(itemId)) {
            return null;
        }
        String middleId = itemId.substring(1, itemId.length());
        // param Function ItemId : "F010G18F"
        // 1. 'F'->'M' ����.
        // 2. �ڿ� 'A'�� �߰�
        // Result Function Master ItemId : "M010G18FA"
        return "M" + middleId + "A";
    }

    private void updateMigrationStatus() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                ArrayList<HashMap<String, String>> tableList = getTableStructure(tree.getItems());                
                try {
                    DataSet ds = new DataSet();
                    ds.put("updateDataList", tableList);
                    ds.put("objectType", objectType);
                    remote.execute("com.kgm.service.MigrationService", "updateMigrationStatus", ds);
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

    /**
     * Item ���� �� DB�� ���¸� ������Ʈ �� Item ����Ʈ�� ���´�.
     * 
     * @method getTableStructure
     * @date 2013. 3. 8.
     * @param
     * @return ArrayList<HashMap<String,String>>
     * @exception
     * @throws
     * @see
     */
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

    /**
     * Top ���� ������ ����Ʈ �˻�
     * 
     * @method getTableStructure
     * @date 2013. 3. 8.
     * @param
     * @return ArrayList<HashMap<String,String>>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<HashMap<String, String>> getTableStructure(ManualTreeItem treeItem, ArrayList<HashMap<String, String>> tableItemList) {
        if (tableItemList == null) {
            tableItemList = new ArrayList<HashMap<String, String>>();
        }
        HashMap<String, String> data = new HashMap<String, String>();
        // DB�� ������Ʈ �� STATUS�� �����Ѵ�.
        if (STATUS_ERROR == treeItem.getStatus()) {
            data.put("MIG_FLAG", "F");
        } else {
            data.put("MIG_FLAG", "C");
        }
        if (CLASS_TYPE_ITEM.equals(treeItem.getItemType())) {
            // data.put("ITEM_ID", treeItem.getItemID());
            /**
             * ���� ������Ʈ�� �������� treeItem.getItemID() �� ���������� Funtion ��� �� Function
             * Master������� ���� tree text�� ������ ������Ʈ �ϴ� ������� ���� ��
             */
            // data.put("ITEM_ID", treeItem.getText());
            data.put("ITEM_ID", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, DB_ATTR_STR));
            data.put("NAME", treeItem.getBWItemAttrValue("Item", "object_name"));
            tableItemList.add(data);
        } else if (CLASS_TYPE_DATASET.equals(treeItem.getItemType())) {
            // �����ͼ� Ÿ���� ���̱׷��̼� ��� DB�� MIG_FLAG�� ���ܽ�Ų��.
        }
        // Child TreeItem
        ArrayList<ManualTreeItem> childItemList = new ArrayList<ManualTreeItem>();
        super.syncGetChildItem(treeItem, childItemList);
        for (int i = 0; i < childItemList.size(); i++) {
            ManualTreeItem childItem = childItemList.get(i);
            this.getTableStructure(childItem, tableItemList);
        }
        return tableItemList;
    }

    /**
     * Tree ITEM�� ���� �޼��� ���
     * 
     * @method setErrorStatus
     * @date 2013. 3. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setErrorStatus(ManualTreeItem item, String strBundleMsgKey) {
        this.nWraningCount += this.nWraningCount;
        item.setStatus(STATUS_ERROR, super.getTextBundle(strBundleMsgKey, "MSG", super.dlgClass));
    }
    
    /**
     * DB Status ���� - ROW �� ����
     * 
     */
    @Override
    public void importDataPost(ManualTreeItem treeItem) throws Exception
    {        
        String status = "";
        if (STATUS_ERROR == treeItem.getStatus()) {
            status = "F";
        } else {
            status = "C";
        }        
        try {
            DataSet ds = new DataSet();            
            ds.put("objectType", objectType);
            ds.put("ITEM_ID", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID));
            ds.put("MIG_FLAG", status);
            remote.execute("com.kgm.service.MigrationService", "updateMigrationStatusChange", ds);
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
}
