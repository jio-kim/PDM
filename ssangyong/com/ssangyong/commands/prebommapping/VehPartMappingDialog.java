package com.ssangyong.commands.prebommapping;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import swing2swt.layout.BorderLayout;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.SYMCLOVComboBox;
import com.ssangyong.common.SYMCText;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.ProgressBar;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.SYMCBOMWindow;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class VehPartMappingDialog extends SYMCAbstractDialog {
    private Registry registry;
    private TCSession session;
    private SYMCText textPartNo;
    private SYMCText textPartName;
    private SYMCText textFMPNo;
    private SYMCText textTargetFMPNo;
    private String[] vehPartColumnIds = new String[]{"FMP No", "ID", "Name", "S/Mode", "Variant Condition", "Project Code", "System Code"};
    private Integer[] vehPartColumnWids = new Integer[]{100, 90, 200, 70, 180, 90, 90};
    private String[] preVehPartColumnIds = new String[]{"NonMapping", "MappingType", "FMP No", "PrePart ID", "Unique ID", "PrePart Name", "PrePart S/Mode", "PrePart Variant Condition", "Project Code", "System Code"};
    private Integer[] preVehPartColumnWids = new Integer[]{50, 50, 100, 90, 90, 200, 70, 180, 90, 90};
    private String[] partMappingColumnIds = new String[]{"Project Code", "System Code", "Pre Veh Part ID", "Pre Veh Unique ID", "Veh Part ID"};
    private Integer[] partMappingColumnWids = new Integer[]{90, 90, 120, 120, 120};
    private Table tableVehPart;
    private Table tablePreVehPart;
    private Table tablePartMapping;
    private SYMCLOVComboBox comboSystemCode;
    private SYMCLOVComboBox comboProjectCode;
    private boolean bSearchCriteria = true;
    private HashMap<TCComponentItem, TCComponentItemRevision> mapParts = new HashMap<TCComponentItem, TCComponentItemRevision>();
    private HashMap<TCComponentItem, TCComponentItemRevision> delAssignedParts = new HashMap<TCComponentItem, TCComponentItemRevision>();
    private HashMap<TCComponentItem, TCComponentItemRevision> alreadyMappedParts = new HashMap<TCComponentItem, TCComponentItemRevision>();
    private TCComponentItemRevision selectedVehPartRev = null;
    private TCComponentItemRevision selectedPreVehPartRev = null;
    private SashForm sashPartList;
    public static String REL_ASSIGNED_PART = "S7_ASSIGNED_PART_REL";
    private String sys_code = null;
    private String proj_code = null;
    private Button btnMappingApply;
    private Button btnNonMapping;
    private Button btnMapping;
    TableColumn preVehcolumn;
    private ArrayList<HashMap<Integer, Object>> preItemList;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public VehPartMappingDialog(Shell parent, int style) {
        super(parent, style | SWT.SHELL_TRIM);

        registry = Registry.getRegistry("com.symc.plm.rac.prebom.prebom.dialog.dialog");
        session = CustomUtil.getTCSession();

        setApplyButtonVisible(true);
        setParentDialogCompositeSize(new Point(1244, 768));
        super.setScrollHeightAtResize(0);
    }

    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        setDialogTextAndImage("Vehicle Part Mapping Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));

        Composite mainComposite = new Composite(parentScrolledComposite, SWT.NONE);
        mainComposite.setLayout(new BorderLayout(0, 0));

        Composite compTable = new Composite(mainComposite, SWT.NONE);
        compTable.setLayoutData(BorderLayout.CENTER);
        compTable.setLayout(new FillLayout());
        
        SashForm sashAll = new SashForm(compTable, SWT.VERTICAL);
//        sashAll.setLayoutData(BorderLayout.CENTER);

        sashPartList = new SashForm(sashAll, SWT.NONE);

        Composite compVehPart = new Composite(sashPartList, SWT.BORDER | SWT.EMBEDDED);
        compVehPart.setLayout(new BorderLayout(0, 0));

        tableVehPart = new Table(compVehPart, SWT.FULL_SELECTION | SWT.SINGLE);
        tableVehPart.setHeaderVisible(true);
        tableVehPart.setLinesVisible(true);

        for (int i = 0; i < vehPartColumnIds.length; i++)
        {
            createTableColumn(tableVehPart, vehPartColumnIds[i], vehPartColumnWids[i], -1);
        }
        tableVehPart.addMouseListener(new org.eclipse.swt.events.MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent paramMouseEvent) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            if (bSearchCriteria)
                                searchPreVehPart();
                            else
                                addMappingTable();
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void mouseDown(MouseEvent paramMouseEvent) {
            }

            @Override
            public void mouseUp(MouseEvent paramMouseEvent) {
            }
        });
        
        Composite compPreVehPart = new Composite(sashPartList, SWT.BORDER | SWT.EMBEDDED);
        compPreVehPart.setLayout(new BorderLayout(0, 0));

        sashPartList.setWeights(new int[] {520, 520});

        tablePreVehPart = new Table(compPreVehPart, SWT.CHECK | SWT.FULL_SELECTION | SWT.SINGLE);
//        tablePreVehPart = new Table(compPreVehPart, SWT.NONE);
        tablePreVehPart.setHeaderVisible(true);
        tablePreVehPart.setLinesVisible(true);

        for (int i = 0; i < preVehPartColumnIds.length; i++)
        {
            TableColumn tempcolumn = createTableColumn(tablePreVehPart, preVehPartColumnIds[i], preVehPartColumnWids[i], -1);
            if (i == 6) {
                preVehcolumn = tempcolumn; 
                tablePreVehPart.setSortColumn(preVehcolumn);
                tablePreVehPart.setSortDirection(SWT.UP);
                preVehcolumn.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event e) {
                        TableItem[] items = tablePreVehPart.getItems();
                        int dir = tablePreVehPart.getSortDirection();
                        Collator collator = Collator.getInstance(Locale.getDefault());
                        TableColumn column = (TableColumn)e.widget;
                        int index = 6;
                        for (int i = 1; i < items.length; i++) {
                            String value1 = items[i].getText(index);
                            for (int j = 0; j < i; j++){
                                String value2 = items[j].getText(index);
                                if (dir == 1024) {
                                    tablePreVehPart.setSortDirection(SWT.UP);
                                    if (collator.compare(value1, value2) < 0) {
                                        TableItem item = new TableItem(tablePreVehPart, SWT.NONE, j);
                                        if (items[i].getText(1).equals("N")) {
                                            item.setChecked(true);
                                        }else{
                                            item.setChecked(false);
                                        }
                                        String[] values = {items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5), items[i].getText(6), items[i].getText(7), items[i].getText(8), items[i].getText(9)};
                                        item.setData("PreVehPart", items[i].getData("PreVehPart"));
                                        item.setData("CurPart", items[i].getData("CurPart"));
                                        item.setData("MappingType", items[i].getData("MappingType"));
                                        items[i].dispose();
                                        item.setText(values);
                                        items = tablePreVehPart.getItems();
                                        break;
                                    }
                                }else{
                                    tablePreVehPart.setSortDirection(SWT.DOWN);
                                    if (collator.compare(value1, value2) > 0) {
                                        TableItem item = new TableItem(tablePreVehPart, SWT.NONE, j);
                                        if (items[i].getText(1).equals("N")) {
                                            item.setChecked(true);
                                        }else{
                                            item.setChecked(false);
                                        }
                                        String[] values = {items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5), items[i].getText(6), items[i].getText(7), items[i].getText(8), items[i].getText(9)};
                                        item.setData("PreVehPart", items[i].getData("PreVehPart"));
                                        item.setData("CurPart", items[i].getData("CurPart"));
                                        item.setData("MappingType", items[i].getData("MappingType"));
                                        items[i].dispose();
                                        item.setText(values);
                                        items = tablePreVehPart.getItems();
                                        break;
                                    }
                                }
                                
                            }
                        }
                        tablePreVehPart.setSortColumn(column);
                    }
                });
            }
        }
        
        tablePreVehPart.addMouseListener(new org.eclipse.swt.events.MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent paramMouseEvent) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            if (bSearchCriteria)
                                addMappingTable();
                            else
                                searchPreVehPart();
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void mouseDown(MouseEvent paramMouseEvent) {
            }

            @Override
            public void mouseUp(MouseEvent paramMouseEvent) {
            }
        });

        Composite compMapping = new Composite(sashAll, SWT.NONE | SWT.EMBEDDED);
        compMapping.setLayout(new BorderLayout(0, 0));
        
        tablePartMapping = new Table(compMapping, SWT.FULL_SELECTION | SWT.SINGLE);
        tablePartMapping.setHeaderVisible(true);
        tablePartMapping.setLinesVisible(true);

        for (int i = 0; i < partMappingColumnIds.length; i++)
            createTableColumn(tablePartMapping, partMappingColumnIds[i], partMappingColumnWids[i], -1);

        tablePartMapping.addMouseListener(new org.eclipse.swt.events.MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent paramMouseEvent) {
                Display.getDefault().asyncExec(new Runnable() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void run() {
                        int []selectRows = tablePartMapping.getSelectionIndices();
                        ArrayList<TCComponentItemRevision> tableItemList = new ArrayList<TCComponentItemRevision>();
                        TableItem[] tableItems = bSearchCriteria ? tableVehPart.getItems() : tablePreVehPart.getItems();
                        for (TableItem tableItem : tableItems){
                            tableItemList.add((TCComponentItemRevision) tableItem.getData("CurPart"));
                        }

                        for (int selectRow : selectRows)
                        {
                            if (tablePartMapping.getItem(selectRow).getData("isSaved") != null && (Boolean) tablePartMapping.getItem(selectRow).getData("isSaved"))
                            {
                                ArrayList<TCComponentBOMLine> tableDatas = new ArrayList<TCComponentBOMLine>();
                                tableDatas = (ArrayList<TCComponentBOMLine>) (bSearchCriteria ? tableVehPart.getData() : tablePreVehPart.getData());
//                                if ((bSearchCriteria && tableDatas.contains(tablePartMapping.getItem(selectRow).getData("VehPart"))) || (!bSearchCriteria && tableDatas.contains(tablePartMapping.getItem(selectRow).getData("PreVehPart"))))
//                                    continue;

                                if (alreadyMappedParts.containsKey(tablePartMapping.getItem(selectRow).getData("VehPart")))
                                    delAssignedParts.put((TCComponentItem) tablePartMapping.getItem(selectRow).getData("VehPart"), (TCComponentItemRevision) tablePartMapping.getItem(selectRow).getData("PreVehPart"));

//                                if (! tableItemList.contains(tablePartMapping.getItem(selectRow).getData(bSearchCriteria ? "VehPart" : "PreVehPart")))
//                                {
//                                    TableItem newItem = new TableItem(bSearchCriteria ? tableVehPart : tablePreVehPart, SWT.NONE);
//
//                                    if (tablePartMapping.getItem(selectRow).getData("MappingType").toString().equals("N")) {
//                                        newItem.setChecked(true);
//                                    }
//                                    newItem.setText(1, tablePartMapping.getItem(selectRow).getData("MappingType").toString());
//                                    newItem.setText(2, tablePartMapping.getItem(selectRow).getData("FMP").toString());
//                                    newItem.setText(3, tablePartMapping.getItem(selectRow).getText(2));
//                                    newItem.setText(4, tablePartMapping.getItem(selectRow).getText(3));
//                                    newItem.setText(5, tablePartMapping.getItem(selectRow).getData("ObjectName").toString());
//                                    newItem.setText(6, tablePartMapping.getItem(selectRow).getData("SupplyMode").toString());
//                                    newItem.setText(7, tablePartMapping.getItem(selectRow).getData("VC").toString());
//                                    newItem.setText(8, tablePartMapping.getItem(selectRow).getText(0));
//                                    newItem.setText(9, tablePartMapping.getItem(selectRow).getText(1));
//
//                                    newItem.setData("CurPart", tablePartMapping.getItem(selectRow).getData(bSearchCriteria ? "VehPart" : "PreVehPart"));
//                                }
                            }

                            mapParts.remove(tablePartMapping.getItem(selectRow).getData("VehPart"));
                            tablePartMapping.remove(selectRow);
                            setPreTableData();
                        }
                    }
                });
                
            }
            @Override
            public void mouseDown(MouseEvent paramMouseEvent) {
            }

            @Override
            public void mouseUp(MouseEvent paramMouseEvent) {
            }
        });

        Composite compHead = new Composite(mainComposite, SWT.NONE);
        compHead.setLayoutData(BorderLayout.NORTH);
        compHead.setLayout(new BorderLayout(0, 0));
        
        Composite compCondition = new Composite(compHead, SWT.NONE);
        compCondition.setLayoutData(BorderLayout.NORTH);
        compCondition.setLayout(new GridLayout(1, false));
        
        Composite composite = new Composite(compCondition, SWT.NONE);
        composite.setLayout(new GridLayout(13, false));
        
        Label lblSystemCode = new Label(composite, SWT.NONE);
        lblSystemCode.setSize(71, 15);
        lblSystemCode.setText("System Code");
        
        comboSystemCode = new SYMCLOVComboBox(composite);
        comboSystemCode.setLOVComponent("s7_SYSTEM_CODE");
        GridData gdSystemCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        gdSystemCode.widthHint = 220;
        comboSystemCode.setLayoutData(gdSystemCode);

        Label lblProjectCode = new Label(composite, SWT.NONE);
        lblProjectCode.setSize(69, 15);
        lblProjectCode.setText("Project Code");
        
        comboProjectCode = new SYMCLOVComboBox(composite);
        comboProjectCode.setLOVComponent("s7_PROJECT_CODE");
        GridData gdProjectCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdProjectCode.widthHint = 120;
        comboProjectCode.setLayoutData(gdProjectCode);  

        Label gapLabel2 = new Label(composite, SWT.NONE);
        GridData gdGapLabel2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdGapLabel2.widthHint = 80;
        gapLabel2.setLayoutData(gdGapLabel2);

        Label lblFMP = new Label(composite, SWT.NONE);
        lblFMP.setText("FMP No");
        textFMPNo = new SYMCText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gdFMPNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdFMPNo.widthHint = 100;
        textFMPNo.setLayoutData(gdFMPNo);
        textFMPNo.setTextLimit(11);
        textFMPNo.setInputType(SYMCText.UPPER_CASE);
        textFMPNo.setMandatory(true);

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Button btnSearchCriteria = new Button(composite, SWT.CHECK);
        GridData gdSearchCriteria = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdSearchCriteria.widthHint = 100;
        btnSearchCriteria.setLayoutData(gdSearchCriteria);
        btnSearchCriteria.setText("EBOM 기준");
        btnSearchCriteria.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {
                if ((delAssignedParts != null && delAssignedParts.size() > 0) || (mapParts != null && mapParts.size() > 0))
                {
                    ConfirmDialog qDialog = new ConfirmDialog(getShell(), "확인", "변경된 내용이 존재합니다. \"Yes\"를 선택하시면 저장하지 않고 진행합니다.");
                    qDialog.open();
                    if (! qDialog.isOkayClicked())
                    {
                        ((Button) event.widget).setSelection(! ((Button) event.widget).getSelection());
                        return;
                    }
                }
                
                delAssignedParts.clear();
                mapParts.clear();
                tablePartMapping.removeAll();
                alreadyMappedParts.clear();
                selectedPreVehPartRev = null;
                selectedVehPartRev = null;

                if (bSearchCriteria)
                {
                    ((Button) event.getSource()).setText("PreBOM 기준");
                    bSearchCriteria = false;
                    tablePreVehPart.removeAll();
                    tableVehPart.removeAll();
                }
                else
                {
                    ((Button) event.getSource()).setText("EBOM 기준");
                    bSearchCriteria = true;
                    tableVehPart.removeAll();
                    tablePreVehPart.removeAll();
                }
            }
        });

        Label skipLabel = new Label(composite, SWT.NONE);
        skipLabel.setSize(50, 15);
        skipLabel.setText("Target FMP No");

        textTargetFMPNo = new SYMCText(composite, SWT.BORDER | SWT.SINGLE);
        GridData gdTargetFMPNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdTargetFMPNo.widthHint = 100;
        textTargetFMPNo.setLayoutData(gdTargetFMPNo);
        textTargetFMPNo.setTextLimit(11);
        textTargetFMPNo.setInputType(SYMCText.UPPER_CASE);

        Label lblPartNo = new Label(composite, SWT.NONE);
        lblPartNo.setSize(44, 15);
        lblPartNo.setText("Part No.");
        
        textPartNo = new SYMCText(composite, SWT.BORDER | SWT.SINGLE);
        textPartNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        GridData gdPartNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdPartNo.widthHint = 100;
        textPartNo.setLayoutData(gdPartNo);
        textPartNo.setInputType(SYMCText.UPPER_CASE);
        
        Label gapLabel = new Label(composite, SWT.NONE);
        GridData gdGapLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdGapLabel.widthHint = 20;
        gapLabel.setLayoutData(gdGapLabel);
        
        Label lblPartName = new Label(composite, SWT.NONE);
        lblPartName.setSize(57, 15);
        lblPartName.setText("Part Name");

        textPartName = new SYMCText(composite, SWT.BORDER | SWT.SINGLE);
        textPartName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
        textPartName.setSize(364, 21);
        
        Label gapLabel3 = new Label(composite, SWT.NONE);
        GridData gdGapLabel3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        gdGapLabel3.widthHint = 30;
        gapLabel3.setLayoutData(gdGapLabel3);
        
        Button btnSearch = new Button(composite, SWT.NONE);
        btnSearch.setSize(48, 25);
        btnSearch.setText("Search");
        btnSearch.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {
                searchVehParts();
                String tfn = textTargetFMPNo.getText();
                if(tfn != null && tfn.length() > 1){
                	searchPreVehPart();
                }
            }
        });
        
        Label label = new Label(compHead, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(BorderLayout.CENTER);

        btnSearchCriteria.setSelection(true);
        bSearchCriteria = false;
        btnSearchCriteria.setText("PreBOM 기준");
        
        // 추가
        btnMappingApply = new Button(composite, SWT.NONE);
        GridData gdMappingApply = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        gdMappingApply.widthHint = 140;
        btnMappingApply.setLayoutData(gdMappingApply);
        btnMappingApply.setText("Non-MappingType 적용");
        btnMappingApply.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {
                try {
                    applyMappingType();
                    mappingShowData();
                    MessageBox.post(getShell(), "Non-Mapping Type 적용이 완료 되었습니다.", "확인", MessageBox.INFORMATION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        btnNonMapping = new Button(composite, SWT.CHECK);
        GridData gdNonMapping = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 10, 1);
        gdNonMapping.widthHint = 150;
        btnNonMapping.setLayoutData(gdNonMapping);
        btnNonMapping.setText("Non-MappingPart 표시");
        btnNonMapping.setSelection(true);
        btnNonMapping.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {
                mappingShowData();
            }
        });
        
        btnMapping = new Button(composite, SWT.CHECK);
        GridData gdMapping = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gdMapping.widthHint = 150;
        btnMapping.setLayoutData(gdMapping);
        btnMapping.setText("UnMappingPart만 표시");
        btnMapping.setSelection(false);
        btnMapping.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {
                mappingShowData();
            }
        });
        
        return mainComposite;
    }
    
    private void mappingShowData(){
        tablePreVehPart.removeAll();
        for (HashMap<Integer, Object> preItemMap : preItemList) {
            TableItem newItem = new TableItem(tablePreVehPart, SWT.NONE);
            setItem(newItem, preItemMap);
        }
        for (TableItem preItem : tablePreVehPart.getItems()) {
            if (!btnNonMapping.getSelection() && preItem.getChecked()) {
                preItem.dispose();
            }else if (btnMapping.getSelection() && preItem.getText(1).equals("M")){
                preItem.dispose();
            }
        }
    }
    
    private void setItem(TableItem newItem, HashMap<Integer, Object> preItemMap) {
        if ((Boolean) preItemMap.get(0)) {
            newItem.setChecked(true);
        }
        newItem.setText(1, preItemMap.get(1).toString());
        newItem.setText(2, preItemMap.get(2).toString());
        newItem.setText(3, preItemMap.get(3).toString());
        newItem.setText(4, preItemMap.get(4).toString());
        newItem.setText(5, preItemMap.get(5).toString());
        newItem.setText(6, preItemMap.get(6).toString());
        newItem.setText(7, preItemMap.get(7).toString());
        newItem.setText(8, preItemMap.get(8).toString());
        newItem.setText(9, preItemMap.get(9).toString());
        newItem.setData("PreVehPart", preItemMap.get(10));
        newItem.setData("CurPart", preItemMap.get(11));
    }
    
    private void applyMappingType() throws Exception{
        ArrayList<HashMap<String, String>> trims = new ArrayList<HashMap<String, String>>();
        TableItem[] preVehItem = tablePreVehPart.getItems();
        for (TableItem tableItem : preVehItem) {
            if (tableItem.getChecked()) {
                if (!((TCComponentItemRevision)tableItem.getData("PreVehPart")).getItem().getProperty("s7_MAPPING_TYPE").equals("N") && !checkMapping(tableItem.getText(4))) {
                    HashMap<String, String> trimData = new HashMap<String, String>();
                    trimData.put("PRD_PART_NO", "");
                    trimData.put("PRE_PART_NO", tableItem.getText(4));
                    trimData.put("PRE_DISP_NO", tableItem.getText(3));
                    trimData.put("PROJ_NO", tableItem.getText(8));
                    trimData.put("SYS_CODE", tableItem.getText(9));
                    trimData.put("MAPPING_TYPE", "N");
                    trimData.put("PRE_SYSTEM_ROW_KEY", getSystemRowKey((TCComponentItemRevision)tableItem.getData("PreVehPart")));
                    trims.add(trimData);
                }
                ((TCComponentItemRevision)tableItem.getData("PreVehPart")).getItem().setProperty("s7_MAPPING_TYPE", "N");
                tableItem.setText(1, "N");
            }else{
                if (checkMapping(tableItem.getText(4))) {
                    ((TCComponentItemRevision)tableItem.getData("PreVehPart")).getItem().setProperty("s7_MAPPING_TYPE", "M");
                    tableItem.setText(1, "M");
                }else{
                    if (!((TCComponentItemRevision)tableItem.getData("PreVehPart")).getItem().getProperty("s7_MAPPING_TYPE").equals("U")) {
                        HashMap<String, String> trimData = new HashMap<String, String>();
                        trimData.put("PRD_PART_NO", "");
                        trimData.put("PRE_PART_NO", tableItem.getText(4));
                        trimData.put("PRE_DISP_NO", tableItem.getText(3));
                        trimData.put("PROJ_NO", tableItem.getText(8));
                        trimData.put("SYS_CODE", tableItem.getText(9));
                        trimData.put("MAPPING_TYPE", "U");
                        trimData.put("PRE_SYSTEM_ROW_KEY", getSystemRowKey((TCComponentItemRevision)tableItem.getData("PreVehPart")));
                        trims.add(trimData);
                    }
                    ((TCComponentItemRevision)tableItem.getData("PreVehPart")).getItem().setProperty("s7_MAPPING_TYPE", "U");
                    tableItem.setText(1, "U");
                }
            }
        }
        if (trims.size() > 0) {
            SYMCRemoteUtil remote = new SYMCRemoteUtil();
            DataSet ds = new DataSet();
            ds.put("DATA", trims);
            remote.execute("com.ssangyong.service.PartMappingService", "insertTrim", ds);
        }
        setPreTableData();
    }
    
    private boolean checkMapping(String prePartID){
        TableItem[] partMappingItem = tablePartMapping.getItems();
        for (TableItem tableItem : partMappingItem) {
            if (tableItem.getText(3).equals(prePartID)) {
                return true;
            }
        }
        return false;
    }

    private TableColumn createTableColumn(Table targetTable, String columnName, int columnWidth, int columnIndex) {
        TableColumn column;

        if (columnIndex < 0)
            column = new TableColumn(targetTable, SWT.NONE);
        else
            column = new TableColumn(targetTable, SWT.NONE, columnIndex);

        column.setText(columnName);
        column.setWidth(columnWidth);

        return column;
    }

    protected void addMappingTable() throws Exception {
        TableItem []selected = bSearchCriteria ? tablePreVehPart.getSelection() : tableVehPart.getSelection();

        if (selected == null  || selected.length == 0)
            return;

        for (TableItem select : selected)
        {
//            InterfaceAIFComponent selectedPartRevision = (TCComponentItemRevision) select.getData("CurPart");
//            if (bSearchCriteria)
//            {
//                selectedPreVehPartRev = (TCComponentItemRevision) selectedPartRevision;
//            }
//            else
//            {
//                TableItem[] preSelected = tablePreVehPart.getSelection();
//                InterfaceAIFComponent selectedPart = (TCComponentItemRevision) preSelected[0].getData("CurPart");
//                selectedPreVehPartRev = (TCComponentItemRevision)selectedPart;
//                selectedVehPartRev = (TCComponentItemRevision) selectedPartRevision;
//            }
            
            TableItem[] preSelected = tablePreVehPart.getSelection();
            TableItem[] eSelected = tableVehPart.getSelection();
            InterfaceAIFComponent selectedPrePart = (TCComponentItemRevision) preSelected[0].getData("CurPart");
            InterfaceAIFComponent selectedEPart = (TCComponentItemRevision) eSelected[0].getData("CurPart");
            selectedPreVehPartRev = (TCComponentItemRevision)selectedPrePart;
            selectedVehPartRev = (TCComponentItemRevision)selectedEPart;
            
            
            if (selectedPreVehPartRev.getItem().getProperty("s7_MAPPING_TYPE").equals("N")) {
                MessageBox.post(getShell(), "Target Part는 Non-Mapping Part 입니다.\nNon-Mapping 체크를 풀어주세요.", "확인", MessageBox.INFORMATION);
                return;
            }

//            if (! mapParts.containsKey(selectedVehPartRev) && ! mapParts.containsValue(selectedPreVehPartRev))
//            {
//                for (TableItem mapTableItem : tablePartMapping.getItems())
//                {
//                    if (mapTableItem.getData("VehPart") == null || mapTableItem.getData("VehPart").equals(selectedVehPartRev))
//                        return;
//                }

                TableItem newItem = new TableItem(tablePartMapping, SWT.NONE);

                newItem.setText(0, selectedPreVehPartRev.getProperty("s7_PROJECT_CODE"));
                newItem.setText(1, selectedPreVehPartRev.getProperty("s7_BUDGET_CODE"));
                newItem.setText(2, selectedPreVehPartRev.getProperty("s7_DISPLAY_PART_NO"));  
                newItem.setText(3, selectedPreVehPartRev.getProperty("item_id"));  
                newItem.setText(4, selectedVehPartRev.getProperty("item_id"));

                if (delAssignedParts.containsKey(selectedVehPartRev.getItem()) && delAssignedParts.containsValue(selectedPreVehPartRev.getItem()))
                {
                    newItem.setData("VehPart", selectedVehPartRev.getItem());
                    newItem.setData("PreVehPart", selectedPreVehPartRev);
                    newItem.setData("isSaved", true);

                    delAssignedParts.remove(selectedVehPartRev.getItem());
                    alreadyMappedParts.put(selectedVehPartRev.getItem(), selectedPreVehPartRev.getItem().getLatestItemRevision());
                }
                else
                {
                    newItem.setData("VehPart", selectedVehPartRev.getItem());
                    newItem.setData("PreVehPart", selectedPreVehPartRev);
                    newItem.setData("isSaved", true);

                    mapParts.put(selectedVehPartRev.getItem(), selectedPreVehPartRev);
                    alreadyMappedParts.put(selectedVehPartRev.getItem(), selectedPreVehPartRev.getItem().getLatestItemRevision());
                }
//            }
        }
    }

  //[20180709 CSH][송대영 책임 요청사항] target fmp no 으로 target을 가지고 옴.
    protected void searchPreVehPart() {
        try {
            TableItem []selected = bSearchCriteria ? tableVehPart.getSelection() : tablePreVehPart.getSelection();
//            final TCComponentItemRevision selectRevision = (TCComponentItemRevision) selected[0].getData("CurPart"); //selected[0];
//            final HashMap<String, String> attrMap = new HashMap<String, String>();

//            String part_name = selectRevision.getProperty("object_name");
//            String sysCode;
//            String projCode;
//            final String targetFMPNo1 = textTargetFMPNo.getText().replaceAll("[*]{1,}", "");
            final String targetFMPNo = textTargetFMPNo.getText().replaceAll("[*]{2,}", "*");
            
            if (targetFMPNo != null && targetFMPNo.trim().length() > 0){
//            	targetFMPNo = targetFMPNo.replaceAll("[*]{2,}", "*");

                if (targetFMPNo.length() < 6){
                    MessageBox.post(getShell(), "Target Function No는 최소 6자 이상 입력해 주세요.", "Information", MessageBox.INFORMATION);
                    return;
                }

//                attrMap.put("FMP_NO", targetFMPNo);
            } else {
            	MessageBox.post(getShell(), "Target FMP는 필수 입력입니다.", "Information", MessageBox.INFORMATION);
            	return;
            }
            
            
            

//            sysCode = selectRevision.getProperty("s7_BUDGET_CODE");
//            projCode = selectRevision.getProperty("s7_PROJECT_CODE");
//
//            if (sysCode != null && sysCode.trim().length() > 0)
//                attrMap.put("s7_BUDGET_CODE", sysCode);
//            if (part_name != null && part_name.trim().length() > 0)
//                attrMap.put("object_name", part_name);

            final ProgressBar progressBar = new ProgressBar(getShell());
            progressBar.start();
            progressBar.setText("검색조건에 따라 시간이 다소 걸릴 수 있습니다.");

            final AbstractAIFOperation queryOp = new AbstractAIFOperation() {
                
                @Override
                public void executeOperation() throws Exception {
                    ArrayList<String> queryFMPNames = new ArrayList<String>();
                    ArrayList<String> queryFMPValues = new ArrayList<String>();
                    queryFMPNames.add("item_id");
                    queryFMPValues.add(targetFMPNo);
                    final TCComponent[] targetFMPs= CustomUtil.queryComponent(bSearchCriteria ? "__SYMC_S7_PreFMP_Revision_working" : "__SYMC_S7_FMP_Revision_working", queryFMPNames.toArray(new String[0]), queryFMPValues.toArray(new String[0]));

//                    final TCComponent[] results= CustomUtil.queryComponent(bSearchCriteria ? "__SYMC_S7_PreVehpartRevision_Mapping" : "__SYMC_S7_VehpartRevision_Mapping", queryNames.toArray(new String[0]), queryValues.toArray(new String[0]));
                    if(targetFMPs == null || targetFMPs.length == 0){
//                    	MessageBox.post(getShell(), "Target FMP를 찾지 못했습니다. 다시 검색해 주세요.", "확인", MessageBox.INFORMATION);
//                        return;
                        
                        getShell().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                MessageBox.post(getShell(), "Target FMP를 찾지 못했습니다. 다시 검색해 주세요.", "확인", MessageBox.INFORMATION);
                            }
                        });

                        throw new Exception("Target FMP를 찾지 못했습니다. 다시 검색해 주세요.");
                    }
                    
                    getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                        	try{
	                        	ArrayList<TCComponentItem> allList = new ArrayList<TCComponentItem>();
//	                            TCComponent findFMP = null;
	                            String fmpID = "";
	                            proj_code = comboProjectCode.getSelectedString();
	                            String part_no = textPartNo.getText();
	                            String part_name = textPartName.getText();
	                            ArrayList<String> queryNames = new ArrayList<String>();
	                            ArrayList<String> queryValues = new ArrayList<String>();
	                            
	                            if (part_no != null){
	                                part_no = part_no.replaceAll("[*]{2,}", "*");
	                            }
	                            if (part_name != null){
	                            	part_name = part_name.replaceAll("[*]{2,}", "*");
	                            }
	                            
	                            
	                            if (proj_code != null && proj_code.trim().length() > 0){
		                            queryNames.add("Project Code");
		                            queryValues.add(proj_code);
	                            }
	                            
	                            if (part_no != null && part_no.trim().length() > 0){
	                            	queryNames.add("ID");
		                            queryValues.add(part_no);
	                            }
	                            
	                            if (part_name != null && part_name.trim().length() > 0){
	                            	queryNames.add("Name");
		                            queryValues.add(part_name);
	                            }
	                            
	                            if (queryNames.size() == 0){
	                            	queryNames.add("ID");
		                            queryValues.add("*");
	                            }
	                            
	                            
	                            for (TCComponent targetFMP : targetFMPs){
	                                if (((TCComponentItemRevision) targetFMP).getItem().getLatestItemRevision().equals(targetFMP)){
//	                                    findFMP = targetFMP;
	                                    
	                                    fmpID = targetFMP.getProperty("item_id");
	
	                                    TCComponentBOMLine fmpBOMLine = CustomUtil.getBomline((TCComponentItemRevision) targetFMP, session);
	                                    BOMLineSearchOperation op = new BOMLineSearchOperation(fmpBOMLine, queryNames.toArray(new String[0]), queryValues.toArray(new String[0]), !bSearchCriteria);
	                                    op.executeOperation();
	                                    @SuppressWarnings("unchecked")
	                                    Collection<TCComponentBOMLine> searchResults = (Collection<TCComponentBOMLine>) op.getOperationResult();
	                                    
	                                    for (TCComponentBOMLine result : searchResults){
	                                    	TCComponentItemRevision resultRevision = (TCComponentItemRevision) result.getItemRevision();
	                                    	TCComponentItem resultItem = (TCComponentItem) result.getItem();
//	                                        if (resultItem.getLatestItemRevision().equals(resultRevision)){
//	                                            AIFComponentContext []assignedParts = resultItem.getRelated(REL_ASSIGNED_PART);
	                                            AIFComponentContext []assignedParts;
	                                            if (bSearchCriteria)
	                                                assignedParts = resultItem.getRelated(REL_ASSIGNED_PART);
	                                            else
	                                                assignedParts = resultItem.whereReferencedByTypeRelation(new String[]{SYMCClass.S7_VEHPARTTYPE}, new String[]{REL_ASSIGNED_PART});
	
	                                            if (assignedParts != null && assignedParts.length > 0 && ! delAssignedParts.containsKey(assignedParts[0]) && ! delAssignedParts.containsValue(assignedParts[0]))
	                                                continue;
	
	                                            TableItem newItem;
	                                                
	                                            if (bSearchCriteria){
	                                                if (allList.contains(resultItem)) continue;
	                                                
	                                                newItem = new TableItem(tablePreVehPart, SWT.NONE);
	                                                
	                                                if (resultItem.getProperty("s7_MAPPING_TYPE").toString().equals("N")) {
	                                                    newItem.setChecked(true);
	                                                }
	                                                newItem.setText(1, resultItem.getProperty("s7_MAPPING_TYPE"));
	                                                newItem.setText(2, fmpID);
	                                                newItem.setText(3, resultRevision.getProperty("s7_DISPLAY_PART_NO"));
	                                                newItem.setText(4, resultRevision.getProperty("item_id"));
	                                                newItem.setText(5, resultRevision.getProperty("object_name"));
	                                                newItem.setText(6, result.getProperty("S7_SUPPLY_MODE").toString());
	                                                newItem.setText(7, convertToSimpleCondition(result.getProperty("bl_variant_condition").toString()));
	                                                newItem.setText(8, resultRevision.getProperty("s7_PROJECT_CODE"));
	                                                newItem.setText(9, result.getProperty("S7_BUDGET_CODE"));
	
	                                                newItem.setData("CurPart", resultRevision);
	                                                newItem.setData("PreVehPart", resultRevision);
	                                                
	                                                allList.add(resultItem);
	                                                
	                                            } else {
	                                                newItem = new TableItem(tableVehPart, SWT.NONE);
	                                                newItem.setText(0, fmpID);
	                                                newItem.setText(1, resultRevision.getProperty("item_id"));
	                                                newItem.setText(2, resultRevision.getProperty("object_name"));
	                                                newItem.setText(3, result.getProperty("S7_SUPPLY_MODE").toString());
	                                                newItem.setText(4, convertToSimpleCondition(result.getProperty("bl_variant_condition").toString()));
	                                                newItem.setText(5, resultRevision.getProperty("s7_PROJECT_CODE"));
	                                                newItem.setText(6, resultRevision.getProperty("s7_BUDGET_CODE"));
	                                                
	                                                newItem.setData("CurPart", resultRevision);
	                                                
	                                                allList.add(resultItem);
	                                            }
//	                                        }
	                                    }
	                                }
	                            }
	                            setPreTableData();
	                            if (bSearchCriteria) {
//	                                selectedVehPartRev = selectRevision;
	
	                                tablePreVehPart.setData(allList);
	                            } else {
//	                                selectedPreVehPartRev = selectRevision;
	
	                                tableVehPart.setData(allList);
	                            }
                        	}catch(Exception e){
                            	e.printStackTrace();
                            }
                        }
                        
                    });
                }
            };
            queryOp.addOperationListener(new InterfaceAIFOperationListener() {
                @Override
                public void startOperation(String s) {
                    getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (bSearchCriteria)
                            {
                                tablePreVehPart.removeAll();

                                selectedPreVehPartRev = null;
                            }
                            else
                            {
                                tableVehPart.removeAll();

                                selectedVehPartRev = null;
                            }
                        }
                    });
                }

                @Override
                public void endOperation() {
                    getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.close();
                        }
                    });
                }
            });

            session.queueOperationAndWait(queryOp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //기존 로직 selectRevision의 system code, object name.... 으로 target을 가지고 옴.
//    protected void searchPreVehPart() {
//        try {
//            TableItem []selected = bSearchCriteria ? tableVehPart.getSelection() : tablePreVehPart.getSelection();
//            final TCComponentItemRevision selectRevision = (TCComponentItemRevision) selected[0].getData("CurPart"); //selected[0];
//            final HashMap<String, String> attrMap = new HashMap<String, String>();
//
//            String part_name = selectRevision.getProperty("object_name");
//            String sysCode;
//            String projCode;
//            final String targetFMPNo = textTargetFMPNo.getText().replaceAll("[*]{1,}", "");
//
//            sysCode = selectRevision.getProperty("s7_BUDGET_CODE");
//            projCode = selectRevision.getProperty("s7_PROJECT_CODE");
//
//            if (sysCode != null && sysCode.trim().length() > 0)
//                attrMap.put("s7_BUDGET_CODE", sysCode);
//            if (part_name != null && part_name.trim().length() > 0)
//                attrMap.put("object_name", part_name);
//
//            final ProgressBar progressBar = new ProgressBar(getShell());
//            progressBar.start();
//            progressBar.setText("검색조건에 따라 시간이 다소 걸릴 수 있습니다.");
//
//            final AbstractAIFOperation queryOp = new AbstractAIFOperation() {
//                
//                @Override
//                public void executeOperation() throws Exception {
//                    ArrayList<String> queryNames = new ArrayList<String>();
//                    ArrayList<String> queryValues = new ArrayList<String>();
//
//                    if (attrMap.containsKey("item_id"))
//                    {
//                        queryNames.add("ID");
//                        queryValues.add(attrMap.get("item_id").toString());
//                    }
//
//                    if (attrMap.containsKey("object_name"))
//                    {
//                        queryNames.add("Name");
//                        queryValues.add(attrMap.get("object_name").toString());
//                    }
//
//                    if (attrMap.containsKey("s7_BUDGET_CODE"))
//                    {
//                        queryNames.add("Budget Code");
//                        queryValues.add(attrMap.get("s7_BUDGET_CODE").toString());
//                    }
//
//                    if (attrMap.containsKey("s7_PROJECT_CODE"))
//                    {
//                        queryNames.add("Project Code");
//                        queryValues.add(attrMap.get("s7_PROJECT_CODE").toString());
//                    }
//
//                    if (attrMap.containsKey("user_id"))
//                    {
//                        queryNames.add("Id");
//                        queryValues.add(attrMap.get("user_id").toString());
//                    }
//                    
//                    final TCComponent[] results= CustomUtil.queryComponent(bSearchCriteria ? "__SYMC_S7_PreVehpartRevision_Mapping" : "__SYMC_S7_VehpartRevision_Mapping", queryNames.toArray(new String[0]), queryValues.toArray(new String[0]));
//                    if(results == null || results.length == 0){
//                        return;
//                    }
//
//                        getShell().getDisplay().asyncExec(new Runnable() {
//                            @Override
//                            public void run() {
//                                try
//                                {
//                                    ArrayList<TCComponentItem> allList = new ArrayList<TCComponentItem>();
//
//                                    for (int i = 0; i < results.length; i++)
//                                    {
//                                        TCComponentItemRevision resultRevision = (TCComponentItemRevision) results[i];
//                                        if (resultRevision.getItem().getLatestItemRevision().equals(results[i]))// && CustomUtil.isReleased(((TCComponentItemRevision) results[i]).getItem().getLatestItemRevision()))
//                                        {
//                                            AIFComponentContext []assignedParts = resultRevision.getItem().getRelated(REL_ASSIGNED_PART);
//                                            if (bSearchCriteria)
//                                                assignedParts = resultRevision.getItem().getRelated(REL_ASSIGNED_PART);
//                                            else
//                                                assignedParts = resultRevision.getItem().whereReferencedByTypeRelation(new String[]{SYMCClass.S7_VEHPARTTYPE}, new String[]{REL_ASSIGNED_PART});
//
//                                            if (assignedParts != null && assignedParts.length > 0 && ! delAssignedParts.containsKey(assignedParts[0]) && ! delAssignedParts.containsValue(assignedParts[0]))
//                                                continue;
//
//                                            SYMCRemoteUtil remote = new SYMCRemoteUtil();
//                                            DataSet ds = new DataSet();
//                                            ds.put("PUID", resultRevision.getUid());
//
//                                            String serviceName = bSearchCriteria ? "whereUsedPreBOMStructure" : "whereUsedStructure";
//                                            @SuppressWarnings("unchecked")
//                                            ArrayList<HashMap<String, Object>> retParents = (ArrayList<HashMap<String, Object>>)remote.execute("com.ssangyong.service.GetParentService", serviceName, ds);
//                                            
//                                            if (retParents != null)
//                                            {
//                                                for (HashMap<String, Object> ret : retParents)
//                                                {
//                                                    if (null == ret.get("FUNC_NO")) continue;
//                                                    
//                                                    String fmpID = ret.get("FUNC_NO").toString();
//
//                                                    if ((targetFMPNo != null && targetFMPNo.trim().length() > 0 && fmpID.startsWith(targetFMPNo)) || (targetFMPNo == null || targetFMPNo.trim().equals("")))
//                                                    {
//                                                        TableItem newItem;
//                                                            
//                                                        if (bSearchCriteria){
//                                                            if (allList.contains(((TCComponentItemRevision)results[i]).getItem())) continue;
//                                                            
//                                                            newItem = new TableItem(tablePreVehPart, SWT.NONE);
//                                                            
//                                                            if (resultRevision.getItem().getProperty("s7_MAPPING_TYPE").toString().equals("N")) {
//                                                                newItem.setChecked(true);
//                                                            }
//                                                            newItem.setText(1, resultRevision.getItem().getProperty("s7_MAPPING_TYPE"));
//                                                            newItem.setText(2, fmpID);
//                                                            newItem.setText(3, resultRevision.getProperty("s7_DISPLAY_PART_NO"));
//                                                            newItem.setText(4, resultRevision.getProperty("item_id"));
//                                                            newItem.setText(5, resultRevision.getProperty("object_name"));
//                                                            newItem.setText(6, ret.get("SMODE").toString());
//                                                            newItem.setText(7, convertToSimpleCondition(ret.get("VARIANT_CONDITION").toString()));
//                                                            newItem.setText(8, resultRevision.getProperty("s7_PROJECT_CODE"));
//                                                            newItem.setText(9, resultRevision.getProperty("s7_BUDGET_CODE"));
//
//                                                            newItem.setData("CurPart", results[i]);
//                                                            newItem.setData("PreVehPart", results[i]);
//                                                            
//                                                            allList.add((TCComponentItem) ((TCComponentItemRevision)results[i]).getItem());
//                                                            
//                                                        } else {
//                                                            newItem = new TableItem(tableVehPart, SWT.NONE);
//                                                            newItem.setText(0, fmpID);
//                                                            newItem.setText(1, resultRevision.getProperty("item_id"));
//                                                            newItem.setText(2, resultRevision.getProperty("object_name"));
//                                                            newItem.setText(3, ret.get("SMODE").toString());
//                                                            newItem.setText(4, convertToSimpleCondition(ret.get("VARIANT_CONDITION").toString()));
//                                                            newItem.setText(5, resultRevision.getProperty("s7_PROJECT_CODE"));
//                                                            newItem.setText(6, resultRevision.getProperty("s7_BUDGET_CODE"));
//                                                            
//                                                            newItem.setData("CurPart", results[i]);
//                                                            
//                                                            allList.add((TCComponentItem) ((TCComponentItemRevision)results[i]).getItem());
//                                                        }
//
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                    setPreTableData();
//                                    if (bSearchCriteria)
//                                    {
//                                        selectedVehPartRev = selectRevision;
//
//                                        tablePreVehPart.setData(allList);
//                                    }
//                                    else
//                                    {
//                                        selectedPreVehPartRev = selectRevision;
//
//                                        tableVehPart.setData(allList);
//                                    }
//                                }
//                                catch (Exception ex)
//                                {
//                                    ex.printStackTrace();
//                                }
//                            }
//                        });
//                }
//            };
//            queryOp.addOperationListener(new InterfaceAIFOperationListener() {
//                @Override
//                public void startOperation(String s) {
//                    getShell().getDisplay().asyncExec(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (bSearchCriteria)
//                            {
//                                tablePreVehPart.removeAll();
//
//                                selectedPreVehPartRev = null;
//                            }
//                            else
//                            {
//                                tableVehPart.removeAll();
//
//                                selectedVehPartRev = null;
//                            }
//                        }
//                    });
//                }
//
//                @Override
//                public void endOperation() {
//                    getShell().getDisplay().asyncExec(new Runnable() {
//                        @Override
//                        public void run() {
//                            progressBar.close();
//                        }
//                    });
//                }
//            });
//
//            session.queueOperationAndWait(queryOp);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    protected void searchVehParts() {
        try {
            final HashMap<String, String> attrMap = new HashMap<String, String>();
            sys_code = comboSystemCode.getSelectedString();
            proj_code = comboProjectCode.getSelectedString();
            String part_no = textPartNo.getText();
            String part_name = textPartName.getText();
            String fmp_no = textFMPNo.getText();

            if (fmp_no != null && fmp_no.trim().length() > 0)
            {
                fmp_no = fmp_no.replaceAll("[*]{2,}", "*");

                if (fmp_no.length() < 6)
                {
                    MessageBox.post(getShell(), "Function No는 최소 6자 이상 입력해 주세요.", "Information", MessageBox.INFORMATION);
                    return;
                }

                attrMap.put("FMP_NO", fmp_no);
            }

            if (part_no != null)
                part_no = part_no.replaceAll("[*]{2,}", "*");

            if ((sys_code == null || sys_code.trim().length() == 0) && (proj_code == null || proj_code.trim().length() == 0) &&
                    (part_no == null || part_no.trim().length() == 0) && (part_name == null || part_name.trim().length() == 0))
            {
                part_no = "*";
            }

            if (sys_code != null && sys_code.trim().length() > 0)
                attrMap.put("s7_BUDGET_CODE", sys_code);
            if (proj_code != null && proj_code.trim().length() > 0)
                attrMap.put("s7_PROJECT_CODE", proj_code);
            if (part_no != null && part_no.trim().length() > 0)
                attrMap.put("item_id", part_no);
            if (part_name != null && part_name.trim().length() > 0)
                attrMap.put("object_name", part_name);

            final ProgressBar progressBar = new ProgressBar(getShell());
            progressBar.start();
            progressBar.setText("검색조건에 따라 시간이 다소 걸릴 수 있습니다.");

            final AbstractAIFOperation queryOp = new AbstractAIFOperation() {
                
                @Override
                public void executeOperation() throws Exception {
                    ArrayList<String> queryNames = new ArrayList<String>();
                    ArrayList<String> queryValues = new ArrayList<String>();

                    if (attrMap.containsKey("item_id"))
                    {
                        queryNames.add("ID");
                        queryValues.add(attrMap.get("item_id").toString());
                    }

                    if (attrMap.containsKey("object_name"))
                    {
                        queryNames.add("Name");
                        queryValues.add(attrMap.get("object_name").toString());
                    }

                    if (attrMap.containsKey("s7_PROJECT_CODE"))
                    {
                        queryNames.add("Project Code");
                        queryValues.add(attrMap.get("s7_PROJECT_CODE").toString());
                    }

                    if (attrMap.containsKey("s7_BUDGET_CODE"))
                    {
                        queryNames.add("Budget Code");
                        queryValues.add(attrMap.get("s7_BUDGET_CODE").toString());
                    }

                    if (attrMap.containsKey("user_id"))
                    {
                        queryNames.add("Id");
                        queryValues.add(attrMap.get("user_id").toString());
                    }

                    if (attrMap.containsKey("FMP_NO"))
                    {
                        ArrayList<String> queryFMPNames = new ArrayList<String>();
                        ArrayList<String> queryFMPValues = new ArrayList<String>();
                        if (attrMap.containsKey("s7_PROJECT_CODE"))
                        {
                            queryFMPNames.add("Project Code");
                            queryFMPValues.add(attrMap.get("s7_PROJECT_CODE"));
                        }
                        queryFMPNames.add("item_id");
                        queryFMPValues.add(attrMap.get("FMP_NO"));
                        TCComponent[] results= CustomUtil.queryComponent(bSearchCriteria ? "__SYMC_S7_FMP_Revision_working" : "__SYMC_S7_PreFMP_Revision_working", queryFMPNames.toArray(new String[0]), queryFMPValues.toArray(new String[0]));
                        if(results == null || results.length == 0){
                            getShell().getDisplay().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    MessageBox.post(getShell(), "조회 조건에 해당하는 작업중인 FMP를 찾지 못했습니다. 다시 검색해 주세요.", "확인", MessageBox.INFORMATION);
                                }
                            });

                            return;
                        }
                        TCComponent findFMP = null;
                        for (TCComponent result : results)
                            if (((TCComponentItemRevision) result).getItem().getLatestItemRevision().equals(result))
                                findFMP = result;

                        if (findFMP == null)
                        {
                            getShell().getDisplay().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    MessageBox.post(getShell(), "조회 조건에 해당하는 작업중인 FMP를 찾지 못했습니다. 다시 검색해 주세요.", "확인", MessageBox.INFORMATION);
                                }
                            });

                            throw new Exception("조회 조건에 해당하는 FMP를 찾지 못했습니다. 다시 검색해 주세요.");
                        }
                        final String topItemId = findFMP.getProperty("item_id");

                        final TCComponentBOMLine fmpBOMLine = CustomUtil.getBomline((TCComponentItemRevision) findFMP, session);
                        BOMLineSearchOperation op = new BOMLineSearchOperation(fmpBOMLine, queryNames.toArray(new String[0]), queryValues.toArray(new String[0]), bSearchCriteria);
                        op.executeOperation();
                        @SuppressWarnings("unchecked")
                        final Collection<TCComponentBOMLine> searchResults = (Collection<TCComponentBOMLine>) op.getOperationResult();

                        getShell().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<TCComponentItem> allList = new ArrayList<TCComponentItem>();

                                tablePartMapping.removeAll();
                                tableVehPart.removeAll();
                                tablePreVehPart.removeAll();

                                selectedPreVehPartRev = null;
                                selectedVehPartRev = null;

                                for (TCComponentBOMLine result : searchResults)
                                {
                                    try
                                    {
                                    	
                                        if (((TCComponentBOMLine) result).getItem().getLatestItemRevision().equals(((TCComponentBOMLine) result).getItemRevision()) && !allList.contains(((TCComponentBOMLine) result).getItem()))
                                        {
                                            if (CustomUtil.isWorkingStatus(((TCComponentBOMLine) result).getItem().getLatestItemRevision()))
                                                continue;

                                            AIFComponentContext []assignedParts;
                                            if (bSearchCriteria)
                                                assignedParts = ((TCComponentBOMLine) result).getItem().getLatestItemRevision().getItem().getRelated(REL_ASSIGNED_PART);
                                            else
                                                assignedParts = ((TCComponentBOMLine) result).getItem().getLatestItemRevision().getItem().whereReferencedByTypeRelation(new String[]{SYMCClass.S7_VEHPARTTYPE}, new String[]{REL_ASSIGNED_PART});

                                            if (assignedParts != null && assignedParts.length > 0)
                                            {
                                                for (AIFComponentContext assignedPart : assignedParts) {
                                                    TableItem newItem = new TableItem(tablePartMapping, SWT.NONE);
                                                    
                                                    TCComponentItem vehPart = (TCComponentItem) (bSearchCriteria ? ((TCComponentBOMLine) result).getItem() : assignedPart.getComponent());
                                                    TCComponentItemRevision preVehPartRevision = (TCComponentItemRevision) (bSearchCriteria ? ((TCComponentItem)assignedPart.getComponent()).getLatestItemRevision() : ((TCComponentBOMLine) result).getItem().getLatestItemRevision());
                                                    
                                                    newItem.setText(0, preVehPartRevision.getProperty("s7_PROJECT_CODE"));
                                                    newItem.setText(1, preVehPartRevision.getProperty("s7_BUDGET_CODE"));
                                                    newItem.setText(2, preVehPartRevision.getProperty("s7_DISPLAY_PART_NO"));  
                                                    newItem.setText(3, preVehPartRevision.getProperty("item_id")); 
                                                    newItem.setText(4, vehPart.getProperty("item_id"));
                                                    
                                                    newItem.setData("MappingType", preVehPartRevision.getItem().getProperty("s7_MAPPING_TYPE"));
                                                    newItem.setData("FMP", topItemId);
                                                    newItem.setData("ObjectName", ((TCComponentBOMLine) result).getProperty("bl_rev_object_name"));
                                                    newItem.setData("SupplyMode", ((TCComponentBOMLine) result).getProperty("S7_SUPPLY_MODE"));
                                                    newItem.setData("VC", convertToSimpleCondition(((TCComponentBOMLine) result).getProperty("bl_variant_condition")));
                                                    newItem.setData("VehPart", bSearchCriteria ? ((TCComponentBOMLine) result).getItem() : assignedPart.getComponent());
                                                    newItem.setData("PreVehPart", bSearchCriteria ? ((TCComponentItem)assignedPart.getComponent()).getLatestItemRevision() : ((TCComponentBOMLine) result).getItem().getLatestItemRevision());
                                                    newItem.setData("isSaved", true);
                                                    
                                                    alreadyMappedParts.put(bSearchCriteria ? ((TCComponentBOMLine) result).getItem() : (TCComponentItem) assignedPart.getComponent(), bSearchCriteria ? (TCComponentItemRevision) ((TCComponentItem)assignedPart.getComponent()).getLatestItemRevision() : ((TCComponentBOMLine) result).getItem().getLatestItemRevision());
                                                    allList.add(((TCComponentBOMLine) result).getItem());
                                                }
                                            }

                                            if (bSearchCriteria)
                                            {
                                                TableItem newItem = new TableItem(tableVehPart, SWT.NONE);

                                                newItem.setText(0, topItemId);
                                                newItem.setText(1, ((TCComponentBOMLine) result).getProperty("bl_item_item_id"));
                                                newItem.setText(2, ((TCComponentBOMLine) result).getProperty("bl_rev_object_name"));
                                                newItem.setText(3, ((TCComponentBOMLine) result).getProperty("S7_SUPPLY_MODE"));
                                                newItem.setText(4, convertToSimpleCondition(((TCComponentBOMLine) result).getProperty("bl_variant_condition")));
                                                newItem.setText(5, ((TCComponentBOMLine) result).getItemRevision().getProperty("s7_PROJECT_CODE"));
                                                newItem.setText(6, ((TCComponentBOMLine) result).getItemRevision().getProperty("s7_BUDGET_CODE"));

                                                newItem.setData("CurPart", ((TCComponentBOMLine) result).getItem().getLatestItemRevision());
                                            }
                                            else
                                            {
                                                TableItem newItem = new TableItem(tablePreVehPart, SWT.NONE);

                                                if (((TCComponentBOMLine) result).getItem().getProperty("s7_MAPPING_TYPE").toString().equals("N")) {
                                                    newItem.setChecked(true);
                                                }
                                                newItem.setText(1, ((TCComponentBOMLine) result).getItem().getProperty("s7_MAPPING_TYPE"));
                                                newItem.setText(2, topItemId);
                                                newItem.setText(3, ((TCComponentBOMLine) result).getProperty("s7_DISPLAY_PART_NO"));
                                                newItem.setText(4, ((TCComponentBOMLine) result).getProperty("bl_item_item_id"));
                                                newItem.setText(5, ((TCComponentBOMLine) result).getProperty("bl_rev_object_name"));
                                                newItem.setText(6, ((TCComponentBOMLine) result).getProperty("S7_SUPPLY_MODE"));
                                                newItem.setText(7, convertToSimpleCondition(((TCComponentBOMLine) result).getProperty("bl_variant_condition")));
                                                newItem.setText(8, ((TCComponentBOMLine) result).getItemRevision().getProperty("s7_PROJECT_CODE"));
                                                newItem.setText(9, ((TCComponentBOMLine) result).getItemRevision().getProperty("s7_BUDGET_CODE"));

                                                newItem.setData("MappingType", ((TCComponentBOMLine) result).getItem().getProperty("s7_MAPPING_TYPE"));
                                                newItem.setData("CurPart", ((TCComponentBOMLine) result).getItem().getLatestItemRevision());
                                                newItem.setData("PreVehPart", ((TCComponentBOMLine) result).getItem().getLatestItemRevision());
                                            }
                                        }
                                        allList.add(((TCComponentBOMLine) result).getItem());
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                if (bSearchCriteria)
                                    tableVehPart.setData(allList);
                                else
                                    tablePreVehPart.setData(allList);
                            }
                        });
                    }
                    else
                    {
                        getShell().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                MessageBox.post(getShell(), "FMP는 필수 입력입니다.", "확인", MessageBox.INFORMATION);
                            }
                        });

                        throw new Exception("FMP는 필수 입력입니다.");
                    }
                }
            };
            queryOp.addOperationListener(new InterfaceAIFOperationListener() {
                @Override
                public void startOperation(String s) {
                    getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }

                @Override
                public void endOperation() {
                    getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            setPreTableData();
                            mappingShowData();
                            progressBar.close();
                        }
                    });
                }
            });

            session.queueOperationAndWait(queryOp);
            delAssignedParts.clear();
            mapParts.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setPreTableData(){
        TableItem[] items = tablePreVehPart.getItems();
        preItemList = new ArrayList<HashMap<Integer, Object>>();
        for (TableItem item : items) {
            HashMap<Integer, Object> mapData = new HashMap<Integer, Object>(); 
            mapData.put(0, item.getChecked());
            mapData.put(1, item.getText(1));
            mapData.put(2, item.getText(2));
            mapData.put(3, item.getText(3));
            mapData.put(4, item.getText(4));
            mapData.put(5, item.getText(5));
            mapData.put(6, item.getText(6));
            mapData.put(7, item.getText(7));
            mapData.put(8, item.getText(8));
            mapData.put(9, item.getText(9));
            mapData.put(10, item.getData("CurPart"));
            mapData.put(11, item.getData("PreVehPart"));
            preItemList.add(mapData);
        }
    }

    @Override
    protected void afterCreateContents() {
        getShell().setDefaultButton(null);
    }

    @Override
    protected boolean validationCheck() {
        if ((mapParts == null || mapParts.size() == 0) && (delAssignedParts == null || delAssignedParts.size() == 0))
        {
            MessageBox.post(getShell(), "연결할 파트가 선택되지 않았거나, 변경사항이 없습니다.", "확인", MessageBox.INFORMATION);
            return false;
        }

        return true;
    }

    @Override
    protected boolean apply() {
        try
        {
            final ProgressBar progressBar = new ProgressBar(getShell());
            progressBar.start();
            progressBar.setText("Relation 정보를 저장하고 있습니다.");

            AbstractAIFOperation saveOp = new AbstractAIFOperation() {
                @Override
                public void executeOperation() throws Exception {
                    ArrayList<HashMap<String, String>> trims = new ArrayList<HashMap<String, String>>();

                    if (delAssignedParts != null && delAssignedParts.size() > 0)
                    {
                        for (TCComponentItem delAssignedPart : delAssignedParts.keySet())
                        {
                            if (!CustomUtil.isReleased(delAssignedPart.getLatestItemRevision()))
                                continue;

                            delAssignedPart.remove(REL_ASSIGNED_PART, delAssignedParts.get(delAssignedPart).getItem());

                            if (! mapParts.containsKey(delAssignedPart))
                            {
                                HashMap<String, String> trimData = new HashMap<String, String>();
                                trimData.put("PRD_PART_NO", delAssignedPart.getProperty("item_id"));
                                trimData.put("PRE_PART_NO", delAssignedParts.get(delAssignedPart).getProperty("item_id"));
                                trimData.put("PRE_DISP_NO", delAssignedParts.get(delAssignedPart).getProperty("s7_DISPLAY_PART_NO"));
                                trimData.put("PROJ_NO", delAssignedParts.get(delAssignedPart).getProperty("s7_PROJECT_CODE"));
                                trimData.put("SYS_CODE", delAssignedParts.get(delAssignedPart).getProperty("s7_BUDGET_CODE"));
                                trimData.put("MAPPING_TYPE", "U");
                                trimData.put("PRE_SYSTEM_ROW_KEY", getSystemRowKey(delAssignedParts.get(delAssignedPart)));

                                trims.add(trimData);
                                delAssignedParts.get(delAssignedPart).getItem().setProperty("s7_MAPPING_TYPE", "U");
                            }
                        }
                    }

                    if (mapParts != null && mapParts.size() > 0)
                    {
                        for (TCComponentItem newRelated : mapParts.keySet())
                        {
//                            if (!CustomUtil.isReleased(newRelated))
//                                continue;

                            newRelated.add(REL_ASSIGNED_PART, mapParts.get(newRelated).getItem());

                            HashMap<String, String> trimData = new HashMap<String, String>();
                            trimData.put("PRD_PART_NO", newRelated.getProperty("item_id"));
                            trimData.put("PRE_PART_NO", mapParts.get(newRelated).getProperty("item_id"));
                            trimData.put("PRE_DISP_NO", mapParts.get(newRelated).getProperty("s7_DISPLAY_PART_NO"));
                            trimData.put("PROJ_NO", mapParts.get(newRelated).getProperty("s7_PROJECT_CODE"));
                            trimData.put("SYS_CODE", mapParts.get(newRelated).getProperty("s7_BUDGET_CODE"));
                            trimData.put("MAPPING_TYPE", "M");
                            trimData.put("PRE_SYSTEM_ROW_KEY", getSystemRowKey(mapParts.get(newRelated)));

                            trims.add(trimData);
                            mapParts.get(newRelated).getItem().setProperty("s7_MAPPING_TYPE", "M");
                        }
                    }

                    if (trims.size() > 0)
                    {
                        SYMCRemoteUtil remote = new SYMCRemoteUtil();
                        DataSet ds = new DataSet();
                        ds.put("DATA", trims);
                        remote.execute("com.ssangyong.service.PartMappingService", "insertTrim", ds);
                    }
                }
            };
            saveOp.addOperationListener(new InterfaceAIFOperationListener() {
                @Override
                public void startOperation(String arg0) {
                }
                
                @Override
                public void endOperation() {
                    getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.close();
                        }
                    });
                }
            });

            session.queueOperationAndWait(saveOp);
            mapParts.clear();
            delAssignedParts.clear();
            applyMappingType();

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }
    
    /**
     * System Row Key 반환
     * @param childItem
     * @return
     */
    private String getSystemRowKey(TCComponent child) {
    	SYMCBOMWindow window = null;
    	String sSystemRowKey = "";
    	TCComponentItem childItem = null;
    	TCComponentItemRevision childItemRevision = null;
    	
    	/*
    	 * Parent Item Revision
    	 * └ BOMViewRevision
    	 *    └ Child Item
    	 *       └ Child Item Revision
    	 */
    	try {
    		if (child instanceof TCComponentItemRevision) {
    			childItemRevision = (TCComponentItemRevision) child;
    			childItem = childItemRevision.getItem();
    		}
    		
			AIFComponentContext[] whereReferenced = childItem.whereReferenced();
			if (whereReferenced == null || whereReferenced.length == 0) {
				return "";
			}
			
			for (int inx = 0; inx < whereReferenced.length; inx++) {
				InterfaceAIFComponent component = whereReferenced[inx].getComponent();
				
				if (component instanceof TCComponentBOMViewRevision) {
					TCComponentBOMViewRevision bvr = (TCComponentBOMViewRevision)component;
					
					AIFComponentContext[] whereReferenced2 = bvr.whereReferenced();
					
					if (whereReferenced2 == null || whereReferenced2.length == 0) {
						continue;
					}
					
					for (int jnx = 0; jnx < whereReferenced2.length; jnx++) {
						InterfaceAIFComponent component2 = whereReferenced2[jnx].getComponent();
						if (component2 instanceof TCComponentItemRevision) {
							TCComponentItemRevision parentRevision = (TCComponentItemRevision)component2;
							
					        TCComponentRevisionRule revRule = CustomUtil.getRevisionRule(session, "Latest Working");
					        TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
					        window = (SYMCBOMWindow) windowType.create(revRule);
					        window.setWindowTopLine(parentRevision.getItem(), parentRevision, null, bvr);
					        SYMCBOMLine blParent = (SYMCBOMLine)window.getTopBOMLine();
							AIFComponentContext[] children = blParent.getChildren();
							
							if (children == null || children.length == 0) {
								continue;
							}
							
							for (int knx = 0; knx < children.length; knx++) {
								InterfaceAIFComponent component3 = children[knx].getComponent();
								
								if (component3 instanceof SYMCBOMLine) {
									SYMCBOMLine bomLine = (SYMCBOMLine)component3;
									
									String sSource = bomLine.getItemRevision().getUid();
									String sTarget = childItemRevision.getUid();
									
									if (sSource.equals(sTarget)) {
										sSystemRowKey = bomLine.getProperty("S7_SYSTEM_ROW_KEY");
										break;
									}
								}
							}
							
							window.close();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			if (window != null) {
				try {
					window.close();
				} catch (TCException e) {
					e.printStackTrace();
					MessageBox.post(e);
				}
			}
		}
    	
    	return sSystemRowKey;
    }

    protected String convertToSimpleCondition(String condition){
        if (condition == null)
            return "";

        ArrayList<String> foundOpValueList = new ArrayList<String>();
        Pattern p = Pattern.compile(" or | and |\"[a-zA-Z0-9]{4}\"|\"[a-zA-Z0-9]{5}_STD\"|\"[a-zA-Z0-9]{5}_OPT\"");
        Matcher m = p.matcher(condition);
        while (m.find()) {
//          System.out.println(m.start() + " " + m.group());
            foundOpValueList.add(m.group().trim());
        }
        
        String conditionResult = null;
        for( String opValue : foundOpValueList){
            String con = opValue.replaceAll("\"", "");
            if( conditionResult == null){
                conditionResult = con;
            }else{
                conditionResult += " " + con;
            }
        }
        
        return conditionResult == null ? "" : conditionResult;
    }
}
