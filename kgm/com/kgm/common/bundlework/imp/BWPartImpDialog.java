/**
 * Part 속성 일괄 Upload Dialog
 * 
 * --------------- Upload Option ---------------------- 
 * # ItemID, Revision 공백 허용여부(생성시 ID 신규발번)
 * BWPartImpDialog.opt.isItemIDBlankable = false
 * 
 * # AutoCAD Validate Check 여부( Migration에서 사용하지 않음 )
 * BWPartImpDialog.opt.isAutoCADValidatable = false
 * 
 * # Item 생성 여부
 * BWPartImpDialog.opt.isItemCreatable = true
 * # Item 수정 여부
 * BWPartImpDialog.opt.isItemModifiable = true
 *   
 * # Item Revision 생성 여부
 * BWPartImpDialog.opt.isRevCreatable = true
 * # Item Revision 수정 여부
 * BWPartImpDialog.opt.isRevModifiable = true
 *   
 * # DataSet 사용 여부
 * BWPartImpDialog.opt.isDSAvailable = false
 * # DataSet 삭제 후 생성 여부(교체)
 * BWPartImpDialog.opt.isDSChangable = false
 * 
 *   
 * # BOM 사용 여부
 * BWPartImpDialog.opt.isBOMAvailable = false
 * # BOM 재구성 여부(BOM Structure 삭제 후 재구성)
 * BWPartImpDialog.opt.isBOMRearrange = false
 * # BOM Line 속성 수정 여부
 * BWPartImpDialog.opt.isBOMLineModifiable = false
 * --------------------------------------------------- 
 * 
 * 작업Option은 bundlework_locale_ko_KR.properties에 정의 되어 있음
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
     * DB Update용 ITem ID 저장 Function 등록 -> Function Master 등록 -> MIG_FUNCTION
     * 테이블 ITEM_ID 상태 변경 사용 : treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM,
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
        // BOMLine 관련 옵션
        this.bwOption.setBOMLineModifiable(false);
        this.bwOption.setBOMRearrange(false);
        this.bwOption.setBOMAvailable(false);
        // Dataset 관련 옵션
        this.bwOption.setDSAvailable(true);
        this.bwOption.setDSChangable(true);
        // Item 관련 옵션        
        this.bwOption.setItemModifiable(true);
        // Revision 관련 옵션
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
     * MaualTreeItem에 저장되어 있는 모든 내용을 서버로 Upload 함
     */
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
                    objectType = comboObjectType.getItem(comboObjectType.getSelectionIndex());
                    seqType = comboSeqType.getItem(comboSeqType.getSelectionIndex());
                    // Header 정보 Loading
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
        final String objectType = comboObjectType.getItem(comboObjectType.getSelectionIndex());
        this.headerModel = new BWItemModel();
        // 생성할 TC Object ITEM Type를 설정
        this.strTargetItemType = getItemTypeMap().get(objectType);
        // 컬럼 데이터 생성
        this.setHeader(objectType);
        if (szClass == null) {
            throw new Exception("Object Type is invalid.");
        }
        for (int i = 0; i < szClass.length; i++) {
            this.headerModel.setModelData(szClass[i], szAttr[i], new Integer(i));
        }
    }

    /**
     * 컬럼 데이터 생성
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
         * this.szClass -> 테이블 헤더 컬럼 명 this.szAttr -> 실제 TC Property 속성명
         * this.szDbColumn -> 실제 TC Property 속성명(this.szAttr)에 바인딩된 DB 컬럼명
         * this.szLovNames -> 실제 TC Property 속성명(this.szAttr)에 바인딩된 TC LOV
         * (Validation 체크용)
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
     * Project Item(S7_Project) 속성 설정
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
     * Product Item(S7_Product) 속성 설정
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
     * Vehicle Part Item(S7_Vehpart) 속성 설정
     *
     * ** 주의 - 현재 MIG_VEHPART 테이블의 S7_SYSTEM_CODE 컬럼은 TC의 s7_BUDGET_CODE 속성에 걸려있으므로 주의 한다. **
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
     * Standard Part Item(S7_Stdpart) 속성 설정
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
     * Function Item(S7_Function) 속성 설정
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
     * Function Master Item(S7_FunctionMast) 속성 설정
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
     * Variant Item(S7_Variant) 속성 설정
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
     * Material Item(S7_Material) 속성 설정
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
     * Tech Doc(S7_ENGDOC) 속성 설정
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
     * Clear Attribute 속성
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
        // 업데이트 Item 리스트 초기화
        if (this.itemList != null) {
            this.itemList.clear();
        }
        // Tree Item 초기화
        if (this.tree != null) {
            this.tree.removeAll();
        }
        // Log Text 초기화
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
        totalItemCnt = rows.size(); // 총 Item 수
        for (int r = 0; r < rows.size(); r++) {
            HashMap rMap = rows.get(r);
            BWItemData stcItemData = new BWItemData();                 
            for (int i = 0; i < szAttr.length; i++) {
                String strItemName = szClass[i];
                String strAttrName = szAttr[i];
                // String strAttrValue = rMap.get(szAttr[i]);
                String strAttrValue = "";
                // ITEM Type은 LEVEL정보가 없으므로 강제로 등록한다.
                if (i == 0) { // Column 0 => "0" 무조건 등록
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
            // Excel Item Row가 저장될 TreeItem Object
            ManualTreeItem treeItem = null;
            int nLevel = super.getIntValue(stcItemData.getItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_LEVEL));                    
            // Top TreeItem
            treeItem = new ManualTreeItem(this.tree, this.tree.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strItemID);
            treeItem.setLevel(nLevel);            
            if (treeItem != null) {
                // Tree Table에 표시되는 속성
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
            // ItemID 공백을 허용하지 않는 데 공백인 경우
            if (treeItem != null && !this.bwOption.isItemIDBlankable() && strItemID.trim().equals("")) {
                setErrorStatus(treeItem, "ItemIDRequired");
            }
            // ItemID 공백을 허용, 공백인 경우
            else if (treeItem != null && this.bwOption.isItemIDBlankable() && strItemID.trim().equals("")) {
                treeItem.setBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID, NEW_ITEM_ID);
                treeItem.setText(TREEITEM_COMLUMN_ITEMID, NEW_ITEM_ID);

            }
            // Revision 생성 Option이 아님에도 Revision값이 없는 경우 Error 처리
            if (treeItem != null && !this.bwOption.isRevCreatable() && strRevID.equals("")) {
                setErrorStatus(treeItem, "RevisionIDRequired");
            } else if (treeItem != null && strRevID.equals("")) {
                treeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID, NEW_ITEM_REV);
                treeItem.setText(TREEITEM_COMLUMN_REVISION, NEW_ITEM_REV);
            }
            // Revision ID 유효성 Check
            if (strRevID.equals("")) {
                setErrorStatus(treeItem, "RevisionInvalid");
            }
            // ITEM 등록 후 DB 상태 업데이트용
            treeItem.setBWItemAttrValue(CLASS_TYPE_ITEM, DB_ATTR_STR, strItemID);
            this.itemList.add(treeItem);
            // 기술자료문서 등록이면 기본 테이블정보에서 업로드 대상 리스트를 만들어주어 등록한다.
            if("MIG_ENGDOC".equals(objectType)) {
                HashMap dataset = new HashMap();
                dataset.put("ITEM_ID", strItemID);
                dataset.put("REVISION_ID", strRevID);
                dataset.put("FILE_PATH", rMap.get("FILE_PATH"));                
                ArrayList datasetList = new ArrayList();
                datasetList.add(dataset);                
                rMap.put("DATASET_FILE_LIST", datasetList);
            }
            // Dataset Item 생성
            //this.createDatasetList(treeItem, rMap);            
        }
    }

    /**
     * Item정보의 Dataset List 정보를 가지고 아이템을 생성한다.
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
                // JT Item 생성
                createJTDatasetItem(treeItem, copyHashMap((HashMap)datasetList.get(i)));
            }
            if("X100_MIG_VEHPART".equals(objectType)) {
                if("CATPart".equals(strDatasetType) && "Master".equals(strV4Type)) {
                 // JT Item 생성
                    createJTDatasetItem(treeItem, copyHashMap((HashMap)datasetList.get(i)));
                }
                if("CATDrawing".equals(strDatasetType) && "Detail".equals(strV4Type)) {
                    // PDF Item 생성
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
     * Copy 된 DatasetFile을 가지고 JT 파일을 생성한다. 
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
                strFilePath = strFilePath.substring(0, strFilePath.lastIndexOf(".")) + ".jt";   // 확장자 변경 (.jt)
                datasetItemMap.put("FILE_PATH", strFilePath);
                this.createDatasetTreeItem(treeItem, datasetItemMap);
            }
        }
    }
    
    /**
     * Copy 된 DatasetFile을 가지고 PDF 파일을 생성한다. 
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
                strFilePath = strFilePath.substring(0, strFilePath.lastIndexOf(".")) + ".pdf";   // 확장자 변경 (.pdf)
                datasetItemMap.put("FILE_PATH", strFilePath);
                this.createDatasetTreeItem(treeItem, datasetItemMap);
            }
        }
    }
    
    /**
     * Dataset File 생성
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
            // model일 경우 시작점이 '/DATA_MIG' 시작하므로 폴더이름을 제거한다.
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
     * DB의 HashMap 데이터와 Item에설정 시킬 데이터를 가공하여 저장시킨다. (LOV 데이터 호환성 유지를 위해 사용)
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
        if("MIG_MATERIAL".equals(objectType) && "S7_ACTIVATION".equals(key)) { // MIG_MATERIAL - s7_ACTIVATION 속성값 초기화 (무조건 'Y')
            return "Y";
        } else if("MIG_MATERIAL".equals(objectType) && "S7_MATURITY".equals(key)) { // MIG_MATERIAL - S7_MATURITY 속성값 초기화 (무조건 'Released')
            return "Released";
        } else if("MIG_MATERIAL".equals(objectType) && "REVISION_ID".equals(key)) { // MIG_MATERIAL - REVISION_ID 속성값 초기화 (무조건 '000')
            return "000";
        }
        // MIG_PROJECT
        else if("MIG_PROJECT".equals(objectType) && "S7_VEHICLE_NO".equals(key)) { // MIG_PROJECT - S7_VEHICLE_NO 속성값 초기화 ('')
            return "";
        }
        else if("MIG_PROJECT".equals(objectType) && "S7_IS_NEW".equals(key)) { // MIG_PROJECT - S7_IS_NEW 속성값 초기화 ('')
            return "";
        }
        else if("MIG_PROJECT".equals(objectType) && "S7_IS_VEHICLE_PRJ".equals(key)) { // MIG_PROJECT - S7_IS_VEHICLE_PRJ 속성값 초기화 ('')
            return "";
        }
        // MIG_STDPART
        /*
        if("MIG_STDPART".equals(objectType) && "S7_ACT_WEIGHT".equals(key)) { // MIG_STDPART - S7_ACT_WEIGHT 속성값 초기화 ("0.1")
            return "0.01";
        }
        if("MIG_STDPART".equals(objectType) && "S7_KOR_NAME".equals(key)) { // MIG_STDPART - S7_KOR_NAME 속성값 초기화 ("-")
            return "-";
        }
        */
        // MIG_VEHPART & X100_MIG_VEHPART
        else if("MIG_VEHPART".equals(objectType) || "X100_MIG_VEHPART".equals(objectType)) {
            if("S7_DRW_STAT".equals(key)) { // VEHPART - S7_DRW_STAT 속성값 초기화            
                if("".equals(rtData)) {
                    rtData = "."; 
                } else {
                    // 앞자리 1개만 데이터로 인정 (H : Shown On -> H)
                    if(rtData.length() > 1) {
                        rtData = rtData.substring(0, 1);
                    }
                }
            } else if("S7_RESPONSIBILITY".equals(key)) { // MIG_VEHPART - S7_RESPONSIBILITY 속성값 초기화
                if(".".equals(rtData)) {
                    rtData = ""; 
                }
            } else if("S7_MATTERIAL".equals(key)) { // MIG_VEHPART - S7_MATTERIAL 속성값 초기화 (뒤에 항상 초기 리비전 번호를 등록)
                if(!"".equals(rtData)) {
                    rtData = rtData + "/000"; 
                }
            } else if("S7_ALT_MATERIAL".equals(key)) { // MIG_VEHPART - S7_ALT_MATERIAL 속성값 초기화 (뒤에 항상 초기 리비전 번호를 등록)
                if(!"".equals(rtData)) {
                    rtData = rtData + "/000"; 
                }
            } else if("S7_UNIT".equals(key)) { // VEHPART - S7_UNIT 속성값 초기화 
                if("".equals(rtData)) {
                    rtData = "EA";
                }         
            } else if("S7_STAGE".equals(key)) { // MIG_VEHPART - S7_STAGE 속성값 초기화 ("P")
                rtData = "P";
            } else if("S7_REGULAR_PART".equals(key)) { // MIG_VEHPART - S7_REGULAR_PART 속성값 초기화 ("R")
                rtData = "R";
            } 
        }
        return rtData;
    }

    /**
     * DB Migration 테이블과 TC OBJCT TYPE Mapping
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
     * 실행 후 DB에 상태 - 현재 사용하지 않고 importDataPost 메소드 사용 - 2013.04.29
     */
    /*
    @Override
    public void executePost() throws Exception {
        try {
            // Function 마이그레이션이면 다시 마이그레이션 수 만큼 Function Master를 생성한다.
            if (SYMCClass.S7_FNCPARTTYPE.equals(this.strTargetItemType)) {
                createFunctionMasterItems();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // Function 마이그레이션이면 Function Master 등록 후 DB상태를 업데이트한다.
            // 그 외에는 무조건 업데이트
            if (!SYMCClass.S7_FNCPARTTYPE.equals(this.strTargetItemType)) {
                this.updateMigrationStatus();
            }

        }
    }
     */
    
    /**
     * Function Master 생성
     * 
     * Function Item ID를 Function Master Item ID형식으로 변경 후 Function Master 재등록
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
            // Function Item 생성 에러인 경우 Function Master Item을 생성하지 않는다.
            if (STATUS_ERROR == item.getStatus()) {
                continue;
            }
            // Function Item ID를 Function Master Item ID형식으로 변경
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
     * Function Master일 경우 Item ID 변경 로직
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
        // 1. 'F'->'M' 변경.
        // 2. 뒤에 'A'를 추가
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
     * Item 생성 후 DB에 상태를 업데이트 할 Item 리스트를 얻어온다.
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
     * Top 하위 아이템 리스트 검색
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
        // DB에 업데이트 할 STATUS를 변경한다.
        if (STATUS_ERROR == treeItem.getStatus()) {
            data.put("MIG_FLAG", "F");
        } else {
            data.put("MIG_FLAG", "C");
        }
        if (CLASS_TYPE_ITEM.equals(treeItem.getItemType())) {
            // data.put("ITEM_ID", treeItem.getItemID());
            /**
             * 상태 업데이트는 기존에는 treeItem.getItemID() 로 가져왔으나 Funtion 등록 후 Function
             * Master등록으로 인해 tree text를 가지고 업데이트 하는 방식으로 변경 함
             */
            // data.put("ITEM_ID", treeItem.getText());
            data.put("ITEM_ID", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, DB_ATTR_STR));
            data.put("NAME", treeItem.getBWItemAttrValue("Item", "object_name"));
            tableItemList.add(data);
        } else if (CLASS_TYPE_DATASET.equals(treeItem.getItemType())) {
            // 데이터셋 타입은 마이그레이션 대상 DB의 MIG_FLAG를 제외시킨다.
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
     * Tree ITEM에 에러 메세지 등록
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
     * DB Status 변경 - ROW 별 변경
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
