/**
 * 
 */
package com.symc.plm.me.sdv.view.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.table.SDVTableView;
import org.sdv.core.ui.view.table.model.ColumnInfoModel;
import org.sdv.core.util.UIUtil;

import swing2swt.layout.BorderLayout;

import com.kgm.common.utils.StringUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.operation.common.ManageActivityInitOperation;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.soa.client.model.LovValue;

/**
 * [SR141219-020][20150108] shcho, Open with Time 창에서의 Activity 작업순서 불일치 및 순서 편집 불가 대응 신규 화면 추가
 * Class Name : ManageActivityView
 * Class Description :
 * 
 * @date 2015. 01. 06.
 * 
 */
public class ManageActivityView extends SDVTableView {

    private Registry registry;
    private List<LovValue> activityCategoryLovList;
    private List<LovValue> workOverlapTypeLovList;
    /*
     * LOV 리스트 추가 
     * Control_Point 리스트 추가
     */
    private List<LovValue> controlPointLovList;
    
    private String releaseFlag;
    private Object operationLine;
    private ArrayList<HashMap<String, Object>> oldTableList;
    private ArrayList<HashMap<String, Object>> copyedList = new ArrayList<HashMap<String, Object>>();
    private String[] categoryLovList;
    private String[] workOverlapLovList;
    /*
     * LOV 리스트 추가 
     * Control_Point 리스트 추가
     */
    private String[] controlPointLov;
    private Button addRowBtn, removeRowBtn, copyBtn, pasteBtn, upBtn, downBtn;
    /**
     * [SR190104-050] 윤순식 부장 요청 Bop Activity 편집 후 저장시 걸리는 시간 단축 요청
     * 기존 로직 : createActivities -> resaveActivities 로 변경
     * 기존 Activity 편집 후 저장 시에는 공법 하위의 모든 Activity들을 삭제 재생성 한뒤에 속성정보를 저장 했으나
     * 불필요한 로직으로 인해 소요되는 시간이 많아 로직 변경
     * 편집 전후의 Activity들의 개수를 비교 하여 같으면 속성정보만 변경 
     * 다를 경우에는 차이나는 개수만큼 삭제 및 추가 생성을 하여 Activity 속성 정보 저장하여 소요 시간 단축
     */
    private int objectNameIdx, m7EngNameIdx, m7MecoIdx;
    private Clipboard clipboard = new Clipboard(getDisplay());
    
    private String meco_no;
    
    /**
     * @param parent
     * @param style
     * @param id
     */
    public ManageActivityView(Composite parent, int style, String id) {
        super(parent, style, id, 0);
    }

    public ManageActivityView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
    }

    public ManageActivityView(Composite parent, int style, String id, int configId, String order) {
        super(parent, style, id, configId, order);
    }

    /**
     * [SR160830-036][20160905] taeku.jeong Activity 편집중 ESC 키를 누르면 편집 Dialog가 닫히는 현상 방지 
     */
    @Override
	protected void create(Composite parent) {
		super.create(parent);
		
		addListener(SWT.Traverse, new Listener() {

		    @Override
		    public void handleEvent(Event event) {
		        if (event.character == SWT.ESC)
		        {
		            System.out.println("escape key2");
		            event.doit = false;
		        }

		    }
		});

	}

	/*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        try {
            registry = Registry.getRegistry(ManageActivityView.class);

            parent.setLayout(new BorderLayout());

            Composite buttonBarComposite = new Composite(parent, SWT.NONE);
            buttonBarComposite.setLayoutData(BorderLayout.NORTH);
            buttonBarComposite.setLayout(new GridLayout(6, false));

            createButton(buttonBarComposite); // 버튼 생성

            Composite tableComposite = new Composite(parent, SWT.NONE);
            tableComposite.setLayoutData(BorderLayout.CENTER);

            setColInfoModel(); // Table ColumnModel 설정
            super.initUI(tableComposite); // Table 생성

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param buttonBarComposite
     */
    protected void createButton(Composite buttonBarComposite) {

        // 추가
        addRowBtn = new Button(buttonBarComposite, SWT.PUSH);
        addRowBtn.setAlignment(SWT.CENTER);
        addRowBtn.setText("Add");
        addRowBtn.setImage(registry.getImage("Add.ICON"));
        addRowBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int targetIdx = 0;
                final List<HashMap<String, Object>> newList = getCurrentTableList();

                HashMap<String, Object> newActivityMap = new HashMap<String, Object>();
                String[] propertyNames = registry.getStringArray("table.column.search.id.body");

                int selectedIdx = table.getSelectionIndex();

                // 선택 한 항목이 있는 경우 그 바로 하위에 신규 생성을 한다.
                if (selectedIdx >= 0) {
                    targetIdx = selectedIdx + 1;
                    addNewActivityLine(newList, newActivityMap, propertyNames, selectedIdx + 1);
                }
                // 선택 한 항목이 없는 경우 제일 하단에 신규 생성을 한다.
                else {
                    targetIdx = newList.size();
                    addNewActivityLine(newList, newActivityMap, propertyNames, targetIdx);

                }

                // Sequence번호 재정렬
                resetSequenceNo(newList);
                // Table에 적용
                setTableData(newList);
                table.setSelection(targetIdx);
                targetIdx = 0;
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

        });

        // 삭제
        removeRowBtn = new Button(buttonBarComposite, SWT.PUSH);
        removeRowBtn.setAlignment(SWT.CENTER);
        removeRowBtn.setText("Remove");
        removeRowBtn.setImage(registry.getImage("Remove.ICON"));
        removeRowBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] selectedItems = table.getSelection();
                if (selectedItems.length > 0) {
                    final List<HashMap<String, Object>> newList = getCurrentTableList();
                    int[] selectedIdxs = new int[selectedItems.length];

                    // 선택한 대상 Index 정보를 배열에 담기
                    for (int i = 0; i < selectedItems.length; i++) {
                        int dataMapIndex = (Integer) selectedItems[i].getData("dataMapIndex");
                        selectedIdxs[i] = dataMapIndex;
                    }

                    // NewActivity List에서 제거
                    for (int j = selectedIdxs.length - 1; j >= 0; j--) {
                        newList.remove(selectedIdxs[j]);
                    }

                    // Sequence번호 재정렬
                    resetSequenceNo(newList);
                    // Table에 적용
                    setTableData(newList);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        // Copy
        copyBtn = new Button(buttonBarComposite, SWT.PUSH);
        copyBtn.setAlignment(SWT.CENTER);
        copyBtn.setText("Copy");
        copyBtn.setImage(ResourceManager.getPluginImage("com.symc.plm.rac.me.apps", "icons/copy_16.png"));
        copyBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                copy();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        // Paste
        pasteBtn = new Button(buttonBarComposite, SWT.PUSH);
        pasteBtn.setAlignment(SWT.CENTER);
        pasteBtn.setText("Paste");
        pasteBtn.setImage(ResourceManager.getPluginImage("com.symc.plm.rac.me.apps", "icons/paste_16.png"));
        pasteBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                paste();
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        // 위로
        upBtn = new Button(buttonBarComposite, SWT.PUSH);
        upBtn.setAlignment(SWT.CENTER);
        upBtn.setText("Up");
        upBtn.setImage(ResourceManager.getPluginImage("com.symc.plm.rac.me.apps", "icons/Arrow_Up.png"));
        upBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                final List<HashMap<String, Object>> newList = getCurrentTableList();

                int selecttionCount = table.getSelectionCount();
                if (selecttionCount == 0)
                    return;

                TableItem[] selectedItems = table.getSelection();
                final List<HashMap<String, Object>> tempActivityList = new ArrayList<HashMap<String, Object>>();
                int[] selectedIdxs = new int[selectedItems.length];

                // NewActivity List에서 선택한 대상을 임시 List에 담기
                for (int i = 0; i < selectedItems.length; i++) {
                    int dataMapIndex = (Integer) selectedItems[i].getData("dataMapIndex");
                    tempActivityList.add(newList.get(dataMapIndex));
                    selectedIdxs[i] = dataMapIndex;
                }

                // 더이상 위가 없는 경우 처리 안함
                if (selectedIdxs[0] == 0) {
                    return;
                }

                // NewActivity List에서 제거
                for (int j = selectedIdxs.length - 1; j >= 0; j--) {
                    newList.remove(selectedIdxs[j]);
                }

                // 임시 List에서 NewActivity List의 새로운 위치로 담기
                for (int k = 0; k < tempActivityList.size(); k++) {
                    newList.add(selectedIdxs[k] - 1, tempActivityList.get(k));
                }

                // 선택한 항목의 연속성 여부 검토
                boolean sequentialFlag = true;
                for (int l = 0; l < selectedIdxs.length - 1; l++) {
                    if (selectedIdxs[l] + 1 != selectedIdxs[l + 1]) {
                        sequentialFlag = false;
                        break;
                    }
                }

                // 연속된 항목을 선택한 경우 적용하며, 비연속된 항목을 선택 한 경우 Message를 띄운다.
                if (sequentialFlag) {
                    // Sequence번호 재정렬
                    resetSequenceNo(newList);
                    // Table에 적용
                    setTableData(newList);

                    // table 선택항목 다시 지정
                    for (int n = 0; n < selectedIdxs.length; n++) {
                        selectedIdxs[n] = selectedIdxs[n] - 1;
                    }
                    table.setSelection(selectedIdxs);
                } else {
                    MessageBox.post(getShell(), "연속된 항목을 선택하세요.", "INFORMATION", MessageBox.INFORMATION);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        // 아래로
        downBtn = new Button(buttonBarComposite, SWT.PUSH);
        downBtn.setAlignment(SWT.CENTER);
        downBtn.setText("Down");
        downBtn.setImage(ResourceManager.getPluginImage("com.symc.plm.rac.me.apps", "icons/Arrow_Down.png"));
        downBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                final List<HashMap<String, Object>> newList = getCurrentTableList();

                int selecttionCount = table.getSelectionCount();
                if (selecttionCount == 0)
                    return;

                TableItem[] selectedItems = table.getSelection();
                final List<HashMap<String, Object>> tempActivityList = new ArrayList<HashMap<String, Object>>();
                int[] selectedIdxs = new int[selectedItems.length];

                // NewActivity List에서 선택한 대상을 임시 List에 담기
                for (int i = 0; i < selectedItems.length; i++) {
                    int dataMapIndex = (Integer) selectedItems[i].getData("dataMapIndex");
                    tempActivityList.add(newList.get(dataMapIndex));
                    selectedIdxs[i] = dataMapIndex;
                }

                // 더이상 아래가 없는 경우 처리 안함
                if (selectedIdxs[selectedItems.length - 1] == newList.size() - 1) {
                    return;
                }

                // NewActivity List에서 제거
                for (int j = selectedIdxs.length - 1; j >= 0; j--) {
                    newList.remove(selectedIdxs[j]);
                }

                // 임시 List에서 NewActivity List의 새로운 위치로 담기
                for (int k = 0; k < tempActivityList.size(); k++) {
                    newList.add(selectedIdxs[k] + 1, tempActivityList.get(k));
                }

                // 선택한 항목의 연속성 여부 검토
                boolean sequentialFlag = true;
                for (int l = 0; l < selectedIdxs.length - 1; l++) {
                    if (selectedIdxs[l] + 1 != selectedIdxs[l + 1]) {
                        sequentialFlag = false;
                        break;
                    }
                }

                // 연속된 항목을 선택한 경우 적용하며, 비연속된 항목을 선택 한 경우 Message를 띄운다.
                if (sequentialFlag) {
                    // Sequence번호 재정렬
                    resetSequenceNo(newList);
                    // Table에 적용
                    setTableData(newList);

                    // table 선택항목 다시 지정
                    for (int n = 0; n < selectedIdxs.length; n++) {
                        selectedIdxs[n] = selectedIdxs[n] + 1;
                    }
                    table.setSelection(selectedIdxs);
                } else {
                    MessageBox.post(getShell(), "연속된 항목을 선택하세요.", "INFORMATION", MessageBox.INFORMATION);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });
    }

    /**
     * Table ColumnModel 설정
     */
    private void setColInfoModel() {
//        String[] ids = registry.getStringArray("table.column.search.id.body");
//        String[] names = registry.getStringArray("table.column.search.name.body");
//        String[] widths = registry.getStringArray("table.column.search.width.body");
//        String[] sorts = registry.getStringArray("table.column.search.sort.body");
//        String[] types = registry.getStringArray("table.column.search.type.body");
//        String[] alignments = registry.getStringArray("table.column.search.alignment.body");
        
        /**
         *  table.column.search.id.body=seq,object_name,time_system_category,m7_WORK_OVERLAP_TYPE,time_system_unit_time,m7_CONTROL_POINT,m7_CONTROL_BASIS,m7_WORKERS,m7_MECO_NO,m7_ENG_NAME
			table.column.search.name.body=Seq No.,Line,Category,Work Overlap Type(Body/Paint),Unit Time,Control Point,Control Description,Workers(Body),MECO No.,English Working Description
			table.column.search.width.body=60,400,150,200,100,100,100,100,100,200
			table.column.search.sort.body=false,false,false,false,false,false,false,,false,false,false
			table.column.search.type.body=1,5,2,2,5,2,2,5,1,1
			table.column.search.alignment.body=CENTER,LEFT,LEFT,LEFT,LEFT,LEFT,LEFT,LEFT,CENTER,CENTER
         * 
         * 
         */
    	String id = "seq,object_name,time_system_category,m7_WORK_OVERLAP_TYPE,time_system_unit_time,m7_CONTROL_POINT,m7_CONTROL_BASIS,m7_WORKERS,m7_MECO_NO,m7_ENG_NAME";
    	String name = "Seq No.,Line,Category,Work Overlap Type(Body/Paint),Unit Time,Control Point,Control Description,Workers(Body),MECO No.,English Working Description";
    	String width = "60,400,150,200,100,100,150,100,100,200";
    	String sort = "false,false,false,false,false,false,false,,false,false,false";
    	String type = "1,5,2,2,5,2,5,5,1,1";
    	String alignment = "CENTER,LEFT,LEFT,LEFT,LEFT,LEFT,LEFT,LEFT,CENTER,CENTER";
	      String[] ids = id.split(",");
	      String[] names = name.split(",");
	      String[] widths = width.split(",");
	      String[] sorts = sort.split(",");
	      String[] types = type.split(",");
	      String[] alignments = alignment.split(",");
        

        
        /**
         * [SR190104-050] 윤순식 부장 요청 Bop Activity 편집 후 저장시 걸리는 시간 단축 요청
         * 기존 로직 : createActivities -> resaveActivities 로 변경
         * 기존 Activity 편집 후 저장 시에는 공법 하위의 모든 Activity들을 삭제 재생성 한뒤에 속성정보를 저장 했으나
         * 불필요한 로직으로 인해 소요되는 시간이 많아 로직 변경
         * 편집 전후의 Activity들의 개수를 비교 하여 같으면 속성정보만 변경 
         * 다를 경우에는 차이나는 개수만큼 삭제 및 추가 생성을 하여 Activity 속성 정보 저장하여 소요 시간 단축
         */
        List<ColumnInfoModel> colModelList = new ArrayList<ColumnInfoModel>();
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(SDVPropertyConstant.ACTIVITY_OBJECT_NAME)) {
                objectNameIdx = i;
            } else if (ids[i].equals(SDVPropertyConstant.ACTIVITY_ENG_NAME)) {
                m7EngNameIdx = i;
            } else if( ids[i].equals(SDVPropertyConstant.ACTIVITY_MECO_NO)) {
            	m7MecoIdx = i;
            }

            ColumnInfoModel colModel = new ColumnInfoModel();
            colModel.setColId(ids[i]);
            colModel.setColName(names[i]);
            colModel.setColumnWidth(Integer.parseInt(widths[i]));
            colModel.setSort(Boolean.parseBoolean(sorts[i]));
            colModel.setColType(Integer.parseInt(types[i]));
            colModel.setEditable(false);
            String align = alignments[i].toUpperCase();
            if ("LEFT".equals(align)) {
                colModel.setAlignment(SWT.LEFT);
            } else if ("CENTER".equals(align)) {
                colModel.setAlignment(SWT.CENTER);
            } else {
                colModel.setAlignment(SWT.RIGHT);
            }
            colModelList.add(colModel);
        }

        setColumnInfo(colModelList);
    }

    @Override
    protected void createTableItem(HashMap<String, Object> dataMap, int dataIndex, int index) {
        TableItem item = new TableItem(table, SWT.NONE, index);
        item.setData("dataMapIndex", dataIndex);

        for (int i = 0; i < columnInfoList.size(); i++) {
            String id = columnInfoList.get(i).getColId();
            if (dataMap.get(id) != null) {
                item.setText(i, (String) dataMap.get(id));
            }
        }
    }

    /**
     * Table Listener 추가
     */
    protected void setTableListener() {
        // 마우스 클릭시 editor 추가하는 Listener 생성
        final TableEditor editor = new TableEditor(table);
        table.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent event) {
                // Clean up any previous editor control
                Control old = editor.getEditor();
                if (old != null) {
                    old.dispose();
                }

                // Identify the selected row
                Point pt = new Point(event.x, event.y);
                final TableItem item = table.getItem(pt);
                if (item == null) {
                    return;
                }

                // 선택한 column 및 column Type 정보 지정
                int column = -1;
                int columnType = -1;
                String columnID = "";
                for (int i = 0, n = table.getColumnCount(); i < n; i++) {
                    Rectangle rect = item.getBounds(i);
                    if (rect.contains(pt)) {
                        column = i;
                        columnID = columnInfoList.get(i).getColId();
                        columnType = columnInfoList.get(i).getColType();
                        break;
                    }
                }

                // Control 생성
                Control control = null;
                if (columnType == ColumnInfoModel.COLUMN_TYPE_BUTTON) {
                    control = new Button(table, SWT.NONE);
                    ((Button) control).setText(item.getText(column));
                } else if (columnType == ColumnInfoModel.COLUMN_TYPE_CHECK) {
                    control = new Button(table, SWT.CHECK);
                } else if (columnType == ColumnInfoModel.COLUMN_TYPE_COMBO) {
                    control = new CCombo(table, SWT.NONE);
                    control.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
                    ((CCombo) control).setEditable(false);
                    Object[] comboDataArr = null;
                    if (columnID.equals(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY)) {
                        comboDataArr = categoryLovList;
                    } else if (columnID.equals(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE)) {
                        comboDataArr = workOverlapLovList;
                    } else if (columnID.equals(SDVPropertyConstant.ACTIVITY_CONTROL_POINT)){
                    	comboDataArr = controlPointLov;
                    }
                    if (comboDataArr != null) {
                        int k;
                        for (k = 0; k < comboDataArr.length; k++) {
                            ((CCombo) control).add(StringUtil.nullToString((String) comboDataArr[k]));
                        }

                        // Combo Selection 지정
                        String comboValue = findComboValue(comboDataArr, (String) item.getText(column));
                        if (comboValue != null) {
                            int selectionIdx = ((CCombo) control).indexOf(comboValue);
                            ((CCombo) control).select(selectionIdx);
                        }

                        addControlModifyListener(item, column, control);
                    }
                } else if (columnType == ColumnInfoModel.COLUMN_TYPE_TEXT_EDITOR) {
                    control = new Text(table, SWT.NONE);
                    ((Text) control).setText(item.getText(column));
                    ((Text) control).setForeground(item.getForeground());
                    ((Text) control).setFocus();
                    ((Text) control).selectAll();
                    addControlModifyListener(item, column, control);
                    if (columnID.equals(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME)) {
                        addVerifyListener(control);
                    }
                } else {
                    return;
                }

                control.pack();
                editor.grabHorizontal = true;
                editor.minimumWidth = control.getBounds().width;
                editor.setEditor(control, item, column);
            }
        });
        
        table.addKeyListener(new KeyListener() {
            
            @Override
            public void keyReleased(KeyEvent e) {
                if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'c'))
                {
                    copy();
                }
                if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'v'))
                {
                    paste();
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                
            }
        });
    }

    /**
     * @param control
     */
    protected void addVerifyListener(Control control) {
        if (control instanceof Text) {
            ((Text) control).addVerifyListener(new VerifyListener() {

                @Override
                public void verifyText(VerifyEvent e) {
                    switch (e.keyCode) {
                    case SWT.BS: // Backspace
                    case SWT.DEL: // Delete
                    case SWT.HOME: // Home
                    case SWT.END: // End
                    case SWT.ARROW_LEFT: // Left arrow
                    case SWT.ARROW_RIGHT: // Right arrow
                        return;
                    }

                    String textValue = ((Text) e.getSource()).getText();
                    if (!textValue.contains(".") && textValue.length() != 0) {
                        if (e.keyCode == 46 || e.keyCode == 16777262) { // "."
                            return;
                        }
                    }
                    if (!Character.isDigit(e.character)) { // NUMERIC
                        e.doit = false; // disallow the action
                    }

                    return;
                }
            });
        }
    }

    /**
     * @param index
     * @param control
     * @param editor
     */
    public void addControlModifyListener(final TableItem item, final int index, final Control control) {
        if (control instanceof Text) {
            ((Text) control).addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent me) {
                    item.setText(index, ((Text) control).getText());
                    // 영문 작표 작업을 위하여 국문 object_name이 변경 된 경우 m7_ENG_NAME 속성의 값을 Reset한다. (ITK 기능을 대체해줌)
                    if (index == objectNameIdx) {
                        item.setText(m7EngNameIdx, item.getText(1));
                        item.setText(m7MecoIdx, meco_no);
                    } else {
                    	item.setText(m7MecoIdx, meco_no);
                    }
                }
            });

        } else if (control instanceof CCombo) {
        	
            ((CCombo) control).addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    item.setText(index, ((CCombo) control).getText().split(" ; ")[0]);
                    item.setText(m7MecoIdx, meco_no);
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {

                }
            });
            
            
        }
    }

    private String findComboValue(Object[] comboDataArr, String string) {
        if (comboDataArr != null) {
            for (int i = 0; i < comboDataArr.length; i++) {
                String comboData = StringUtil.nullToString((String) comboDataArr[i]);
                if (comboData.contains(string)) {
                    return comboData;
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param valueList
     * @return String[]
     */
    public String[] getLovValues(List<LovValue> valueList, boolean addEmptyLine) {
        int arrSize = addEmptyLine ? valueList.size() + 1 : valueList.size();
        String[] arrLovValues = new String[arrSize];
        for (int i = 0; i < valueList.size(); i++) {
            LovValue value = valueList.get(i);
            int arrLovValueIdx = addEmptyLine ? i + 1 : i;
            arrLovValues[arrLovValueIdx] = value.getDisplayValue() + " ; " + value.getDisplayDescription();
        }
        return arrLovValues;
    }

    /**
     * 현재 테이블의 모든 값을 List로 가져오는 함수
     * 
     * @return
     */
    public List<HashMap<String, Object>> getCurrentTableList() {
        return getCurrentTableList(table.getItems());
    }

    /**
     * 현재 테이블의 모든 값을 List로 가져오는 함수
     * 
     * @return
     */
    public List<HashMap<String, Object>> getCurrentTableList(TableItem[] items) {
        List<HashMap<String, Object>> tableValueList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < items.length; i++) {
            HashMap<String, Object> tableMap = new HashMap<String, Object>();
            for (int j = 0; j < columnInfoList.size(); j++) {
                String value = items[i].getText(j);
                tableMap.put(columnInfoList.get(j).getColId(), value);
            }
            tableValueList.add(tableMap);
        }

        return tableValueList;
    }

    /**
     * @param activityList
     * @param activityMap
     * @param propertyNames
     * @param targetIdx
     */
    protected void addNewActivityLine(final List<HashMap<String, Object>> activityList, HashMap<String, Object> activityMap, String[] propertyNames, int targetIdx) {
        activityMap.put(propertyNames[0], String.valueOf(targetIdx * 10));
        for (int j = 1; j < propertyNames.length; j++) {
            if (propertyNames[j].equals(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY)) {
                activityMap.put(propertyNames[j], "자동");
             // [SR190104-050] 윤순식 부장 요청 
                // Activity 편집 시 요청 사항
                // Add 버튼 클릭시 Row 추가 시 MECO_NO 정보 표시
                // Copy 후 Paste 할시 영문 속성내용 미 복사 
                // Activity 편집시 속도 개선
            } else if( propertyNames[j].equals(SDVPropertyConstant.ACTIVITY_MECO_NO)) {
                activityMap.put(propertyNames[j], meco_no);
            }  else {
            	activityMap.put(propertyNames[j], "");
            }
        }

        activityList.add(targetIdx, activityMap);
    }

    /**
     * Sequence 번호 재정렬 하는 함수
     * 
     * @param activityList
     */
    protected void resetSequenceNo(final List<HashMap<String, Object>> activityList) {
        for (int i = 0; i < activityList.size(); i++) {
            String newSeq = String.valueOf((i + 1) * 10);
            activityList.get(i).put("seq", newSeq);
        }
    }

    /**
     * Clipboard로 보낼 Data 형식으로 문자열 만들기
     * 
     * @param copyedList
     * @return
     */
    private String transformToClipboardData(ArrayList<HashMap<String, Object>> copyedList) {
        StringBuffer strBffr = new StringBuffer();
        int listCount = 0;
        for (HashMap<String, Object> map : copyedList) {
            listCount++;
            int keyCount = 0;
            for (int i = 0; i < columnInfoList.size(); i++) {
                String id = columnInfoList.get(i).getColId();
                if (map.get(id) != null) {
                    keyCount++;
                    String value = map.get(id).toString();
                    strBffr.append(value);
                    if (keyCount != map.size()) {
                        strBffr.append("\t");
                    }
                }
            }

            if (listCount != copyedList.size()) {
                strBffr.append("\n");
            }
        }

        return strBffr.toString();
    }

    /**
     * Table로 보낼 Data 형식으로 ArrayList<HashMap<String, Object>> 만들기
     * 
     * @return
     */
    private ArrayList<HashMap<String, Object>> transformToTableData() {
        ArrayList<HashMap<String, Object>> arrList = new ArrayList<HashMap<String, Object>>();
        String str = (String) clipboard.getContents(TextTransfer.getInstance());
        if (str != null) {
            String[] rowData = str.split("\n");
            for (int i = 0; i < rowData.length; i++) {
            	// [SR190104-050] 윤순식 부장 요청 
                // Activity 편집 시 요청 사항
                // Add 버튼 클릭시 Row 추가 시 MECO_NO 정보 표시
                // Copy 후 Paste 할시 영문 속성내용 미 복사 
                // Activity 편집시 속도 개선
               String[] columnData = rowData[i].split("\t", columnInfoList.size());
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                if (columnData.length < columnInfoList.size() -2 || columnData.length > columnInfoList.size()) {
                    return null;
                }
                for (int j = 0; j < columnData.length; j++) {
                	// [SR190104-050] 윤순식 부장 요청 
                    // Activity 편집 시 요청 사항
                    // Add 버튼 클릭시 Row 추가 시 MECO_NO 정보 표시
                    // Copy 후 Paste 할시 영문 속성내용 미 복사 
                    // Activity 편집시 속도 개선
                	if(j == (columnData.length -2)) {
                		columnData[j] = meco_no;
                	}
                	if( j == (columnData.length -1) ) {
                		columnData[j] = "";
                	}
                    hashMap.put(columnInfoList.get(j).getColId(), columnData[j]);
                }
                arrList.add(hashMap);
            }
        }
        return arrList;
    }

    /**
     * OS의 Clipboard로 복사
     */
    protected void copyToClipboard(String string) {
        if (string != null && string.length() > 0) {
            TextTransfer textTransfer = TextTransfer.getInstance();
            clipboard.setContents(new Object[] { string }, new Transfer[] { textTransfer });
        }
    }


    /**
     * Copy 작업 수행
     */
    protected void copy() {
        TableItem[] selectedItems = table.getSelection();
        if (selectedItems.length > 0) {
            if(copyedList == null) {
                copyedList = new ArrayList<HashMap<String,Object>>();
            } else {
                copyedList.removeAll(copyedList);                
            }
            copyedList.addAll(getCurrentTableList(selectedItems));
            copyToClipboard(transformToClipboardData(copyedList));
        }
    }
    
    /**
     * Paste 작업 수행
     */
    protected void paste() {
        if (copyedList != null && copyedList.size() > 0) {
            int targetIdx = 0;
            int selectedIdx = table.getSelectionIndex();
            final List<HashMap<String, Object>> newList = getCurrentTableList();

            // 선택 한 항목이 있는 경우 그 바로 하위에 신규 생성을 한다.
            if (selectedIdx >= 0) {
                targetIdx = selectedIdx + 1;
            }
            // 선택 한 항목이 없는 경우 제일 하단에 신규 생성을 한다.
            else {
                targetIdx = newList.size();
            }

            // Clipboard로부터 Data 읽어오기
            ArrayList<HashMap<String, Object>> clipboardList = transformToTableData();
            if(clipboardList == null) {
                MessageBox.post(getShell(), "붙여 넣기에 부적합 한 Data가 복사 되어 있습니다.", "INFORMATION", MessageBox.INFORMATION);
                return;
            }
            
            newList.addAll(targetIdx, transformToTableData());

            // Sequence번호 재정렬
            resetSequenceNo(newList);
            // Table에 적용
            setTableData(newList);
            table.setSelection(targetIdx);
            targetIdx = 0;
        }
    }
    
    public ArrayList<HashMap<String, Object>> getOldTableList() {
        return oldTableList;
    }

    public void setOldTableList(ArrayList<HashMap<String, Object>> oldTableList) {
        this.oldTableList = oldTableList;
    }

    /**
     * @param paramters
     *            the parameters to set
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
        if (parameters != null) {

        }
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new ManageActivityInitOperation();
    }

    /**
     * 초기 Data를 가져온다.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        // ManageActivityInitOperation 결과를 화면에 설정
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset != null) {
                IDataMap activityDataMap = dataset.getDataMap("ActivityList");

                releaseFlag = activityDataMap.getStringValue("ReleaseFlag");
                operationLine = activityDataMap.getValue("OperationLine");
                activityCategoryLovList = (List<LovValue>) activityDataMap.getListValue("Activity Category");
                workOverlapTypeLovList = (List<LovValue>) activityDataMap.getListValue("M7_WORK_OVERLAP_TYPE");
                /*
                 * LOV 리스트 추가 
                 * Control_Point LOV 리스트 추가
                 */
                controlPointLovList = (List<LovValue>)activityDataMap.getListValue("M7_MANAGEMENT_POINT");
                

                List<HashMap<String, Object>> activityList = activityDataMap.getTableValue("ActivityList");
                // [SR190104-050] 윤순식 부장 요청 
                // Activity 편집 시 요청 사항
                // Add 버튼 클릭시 Row 추가 시 MECO_NO 정보 표시
                // Copy 후 Paste 할시 영문 속성내용 미 복사 
                // Activity 편집시 속도 개선
                meco_no = activityDataMap.getStringValue("MECO_NO");
                oldTableList = new ArrayList<HashMap<String, Object>>();
                oldTableList.addAll(activityList);

                setTableData(activityList); // Table에 Data 표시
            }
        }
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        return getLocalSelectDataMap();
    }

    @Override
    public IDataMap getLocalSelectDataMap() {

        RawDataMap rawDataMap = new RawDataMap();
        List<HashMap<String, Object>> newTableList = getCurrentTableList();

        rawDataMap.put("oldTableList", oldTableList, IData.TABLE_FIELD);
        rawDataMap.put("newTableList", newTableList, IData.TABLE_FIELD);
        rawDataMap.put("ReleaseFlag", releaseFlag, IData.STRING_FIELD);
        rawDataMap.put("OperationLine", operationLine, IData.OBJECT_FIELD);
        rawDataMap.put("viewId", getId());

        return rawDataMap;
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public void uiLoadCompleted() {

        categoryLovList = getLovValues(activityCategoryLovList, false);
        workOverlapLovList = getLovValues(workOverlapTypeLovList, true);
        
        controlPointLov = getLovValues(controlPointLovList, false);

        // Activity가 Release 되어있음 수정하지 않는다.
        if (releaseFlag.equals("true")) {
            addRowBtn.setEnabled(false);
            removeRowBtn.setEnabled(false);
            upBtn.setEnabled(false);
            downBtn.setEnabled(false);
        }
        // Activity InWork 이면 Table 값을 수정 할 수 있다.
        else if (releaseFlag.equals("false")) {
            setTableListener();
        }
        //OS Clipboard 에서 Activity 값이 있을경우 copyedList 에 담는다. 
        copyedList = transformToTableData();
    }
}
