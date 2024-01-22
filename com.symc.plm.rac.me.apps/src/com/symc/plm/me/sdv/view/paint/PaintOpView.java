/**
 * 
 */
package com.symc.plm.me.sdv.view.paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * 도장 공법 생성, 도장 공법 복제 Save As View
 * Class Name : PaintOpView
 * Class Description :
 * 
 * @date 2013. 11. 15.
 * 
 */
public class PaintOpView extends AbstractSDVViewPane {

    private SDVText txtVehicleCode;
    private SDVText txtShopCode;
    private SDVText txtOpCode;
    private SDVText txtProductNo;
    private SDVLOVComboBox lovParallel;
    private SDVText txtOpKorName;
    private SDVText txtOpEngName;
    private Table table;
    private SDVText txtWorkerCount;
    //////////////////////////////////////////////////////////////////////////////////////////////
    
    // 특별 특성 속성 추가
    private SDVLOVComboBox lovSpecialChar;
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    private SDVLOVComboBox lovDR;
    private Button btnSpecialStation;

    private TCSession tcSession = null;
    private IDataMap curDataMap = null;

    private int currentConfigId = 0; // 생성:0, 복제:1

    private String lineCode = "", stationCode = ""; // Line 코드, Station Code

    public PaintOpView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    public PaintOpView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId, null);
    }

    public PaintOpView(Composite parent, int style, String id, int configId, String order) {
        super(parent, style, id, DEFAULT_CONFIG_ID, order);
        currentConfigId = configId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        tcSession = SDVBOPUtilities.getTCSession();
        Registry registry = Registry.getRegistry(this);

        Group groupOp = new Group(parent, SWT.NONE);
        groupOp.setText(registry.getString("OpInform.NAME"));
        groupOp.setLayout(new GridLayout(1, false));

        Composite groupOpComposite = new Composite(groupOp, SWT.NONE);
        GridLayout gl_groupOpComposite = new GridLayout(7, false);
        groupOpComposite.setLayout(gl_groupOpComposite);
        gl_groupOpComposite.horizontalSpacing = 10;
        gl_groupOpComposite.marginLeft = 10;
        groupOpComposite.setLayout(gl_groupOpComposite);

        Label lblVehicleCode = new Label(groupOpComposite, SWT.NONE);
        GridData gd_lblVehicleCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblVehicleCode.widthHint = 100;
        lblVehicleCode.setLayoutData(gd_lblVehicleCode);
        lblVehicleCode.setText(registry.getString("VehicleCode.NAME"));

        txtVehicleCode = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtVehicleCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtVehicleCode.widthHint = 45;
        txtVehicleCode.setLayoutData(gd_txtVehicleCode);
        new Label(groupOpComposite, SWT.NONE);

        Label lblShopCode = new Label(groupOpComposite, SWT.NONE);
        lblShopCode.setText(registry.getString("ShopCode.NAME"));

        txtShopCode = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtLineCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtLineCode.widthHint = 45;
        txtShopCode.setLayoutData(gd_txtLineCode);
        new Label(groupOpComposite, SWT.NONE);
        new Label(groupOpComposite, SWT.NONE);

        Label lblOpCode = new Label(groupOpComposite, SWT.NONE);
        lblOpCode.setText(registry.getString("OperationCode.NAME"));

        txtOpCode = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtOpCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtOpCode.widthHint = 45;
        txtOpCode.setLayoutData(gd_txtOpCode);
        new Label(groupOpComposite, SWT.NONE);
        new Label(groupOpComposite, SWT.NONE);
        new Label(groupOpComposite, SWT.NONE);
        new Label(groupOpComposite, SWT.NONE);
        new Label(groupOpComposite, SWT.NONE);       

        Label lblProductNo = new Label(groupOpComposite, SWT.NONE);
        lblProductNo.setText("Product No.");

        txtProductNo = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtProductNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtProductNo.widthHint = 80;
        txtProductNo.setLayoutData(gd_txtProductNo);
        new Label(groupOpComposite, SWT.NONE);

        Label lblparallel = new Label(groupOpComposite, SWT.NONE);
        lblparallel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblparallel.setText(registry.getString("ParallelStationNo.NAME"));

        lovParallel = new SDVLOVComboBox(groupOpComposite, SWT.BORDER, tcSession, "M7_PARALLEL_LINE_VERSION");
        GridData gd_drParallel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_drParallel.widthHint = 45;
        lovParallel.setLayoutData(gd_drParallel);
        new Label(groupOpComposite, SWT.NONE);
        new Label(groupOpComposite, SWT.NONE);

        Label lblOpKorName = new Label(groupOpComposite, SWT.NONE);
        lblOpKorName.setText(registry.getString("OpKorName.NAME"));

        txtOpKorName = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtOpKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1);
        gd_txtOpKorName.widthHint = 340;
        txtOpKorName.setLayoutData(gd_txtOpKorName);

        Label lblOpEngName = new Label(groupOpComposite, SWT.NONE);
        lblOpEngName.setText(registry.getString("OpEngName.NAME"));

        txtOpEngName = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtOpEngName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1);
        gd_txtOpEngName.widthHint = 340;
        txtOpEngName.setLayoutData(gd_txtOpEngName);

        Label lblDR = new Label(groupOpComposite, SWT.NONE);
        lblDR.setText("DR");

        lovDR = new SDVLOVComboBox(groupOpComposite, SWT.BORDER, tcSession, "M7_DR_TYPE");
        GridData gridData_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData_1.widthHint = 45;
        lovDR.setLayoutData(gridData_1);
        new Label(groupOpComposite, SWT.NONE);

        Label lblWorkerCount = new Label(groupOpComposite, SWT.NONE);
        lblWorkerCount.setText(registry.getString("WorkerCount.NAME"));

        txtWorkerCount = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_text_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_text_1.widthHint = 45;
        txtWorkerCount.setLayoutData(gd_text_1);

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 왜 이것만 있는지 모르겠네.....
//        Label lblManUnit = new Label(groupOpComposite, SWT.NONE);
//        lblManUnit.setText("MAN");
//        new Label(groupOpComposite, SWT.NONE);
          // 특별 특성 UI 추가
          Label lblSpecialChar = new Label(groupOpComposite, SWT.NONE);
          lblSpecialChar.setText("S.C");
          lovSpecialChar = new SDVLOVComboBox(groupOpComposite, SWT.BORDER, tcSession, "M7_SPECIAL_CHARICTERISTIC");
          GridData gridData_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
          gridData_2.widthHint = 45;
          lovSpecialChar.setLayoutData(gridData_2);
          
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 도면 No.  
        Label lbldwgNo = new Label(groupOpComposite, SWT.NONE);
        lbldwgNo.setText(registry.getString("InsDwgNo.NAME"));

        
        
        table = new Table(groupOpComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.VIRTUAL);
        table.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1));
        table.setLinesVisible(true);
        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setWidth(155);

        Label lblSpecialStation = new Label(groupOpComposite, SWT.NONE);
        lblSpecialStation.setText(registry.getString("SpecialStation.NAME"));

        btnSpecialStation = new Button(groupOpComposite, SWT.CHECK);

        /**
         * Mandatory 설정
         */
        txtVehicleCode.setMandatory(true);
        txtShopCode.setMandatory(true);
        txtOpCode.setMandatory(true);
        txtProductNo.setMandatory(true);
        lovParallel.setMandatory(true);
        txtOpKorName.setMandatory(true);
        txtOpEngName.setMandatory(true);

        /**
         * 자릿수 지정
         */
        txtOpCode.setTextLimit(4);
        txtWorkerCount.setTextLimit(3);

        /**
         * Text 유형 지정
         */
        txtWorkerCount.setInputType(SDVText.NUMERIC);
        txtOpCode.setInputType(SDVText.DOUBLE);
        
        /**
         * Tab Next 이동 설정
         */
        txtOpCode.setEnableNextTab();
        lovParallel.setEnableNextTab();
        txtOpKorName.setEnableNextTab();
        txtOpEngName.setEnableNextTab();
        txtWorkerCount.setEnableNextTab();
        lovDR.setEnableNextTab();
        btnSpecialStation.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                if(e.detail == SWT.TRAVERSE_TAB_NEXT){
                    e.doit = true;
                }
            }
        });
        
        lovParallel.setSelectedIndex(0);

        /**
         * Table Editior
         */
        final TableEditor editor = new TableEditor(table);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;
        final int EDITABLECOLUMN = 0;

        for (int i = 0; i < 3; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "" });
        }

        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // Clean up any previous editor control
                Control oldEditor = editor.getEditor();
                if (oldEditor != null)
                    oldEditor.dispose();

                // Identify the selected row
                TableItem item = (TableItem) e.item;
                if (item == null)
                    return;

                // The control that will be the editor must be a child of the Table
                Text newEditor = new Text(table, SWT.NONE);
                newEditor.setText(item.getText(EDITABLECOLUMN));
                newEditor.addModifyListener(new ModifyListener() {
                    public void modifyText(ModifyEvent me) {
                        Text text = (Text) editor.getEditor();
                        editor.getItem().setText(EDITABLECOLUMN, text.getText());
                    }
                });

                newEditor.addKeyListener(new KeyListener() {
                    public void keyReleased(KeyEvent e) {
                        if (e.keyCode == 13) {
                            editor.dispose();
                        }
                    }

                    public void keyPressed(KeyEvent e) {
                    }
                });

                newEditor.selectAll();
                newEditor.setFocus();
                editor.setEditor(newEditor, item, EDITABLECOLUMN);
            }
        });
        
        lovParallel.setText("00");
        
        //groupOp.setTabList(new Control[] { txtOpCode,lovParallel,txtOpKorName,txtOpEngName,lovDR,btnSpecialStation });


    }

    /**
     * 초기 Data Init
     * 
     * @method loadInitData
     * @date 2013. 12. 5.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void loadInitData(IDataMap paramters) {
        if (paramters == null)
            return;
        String opCode = "";
        // 공법생성 시는 Line 코드와, Station Code를 합하여 입력
        if (currentConfigId == 0)
            opCode = paramters.getStringValue(SDVPropertyConstant.STATION_LINE) + paramters.getStringValue(SDVPropertyConstant.STATION_STATION_CODE);
        else
            opCode = paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);

        txtVehicleCode.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE));// vehicle Code
        txtShopCode.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_SHOP));
        txtOpCode.setText(opCode);
        txtProductNo.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE)); // Product Code

        txtOpKorName.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_KOR_NAME));
//        txtOpEngName.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_ENG_NAME));

        txtWorkerCount.setText(paramters.getStringValue(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT));
        
        btnSpecialStation.setSelection((Boolean) paramters.getValue(SDVPropertyConstant.OPERATION_REV_KPC));

        String bopVersion = paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        if (!bopVersion.isEmpty())
            lovParallel.setSelectedString(bopVersion);

        String dr = paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_DR);
        if (!dr.isEmpty())
            lovDR.setSelectedString(dr);

        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO)) {
            @SuppressWarnings("unchecked")
            List<String> insDwgNoList = (List<String>) paramters.getListValue(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
            table.clearAll();
            for (int i = 0; i < insDwgNoList.size(); i++) {
                TableItem tableItem = table.getItem(i);
                tableItem.setText(insDwgNoList.get(i));
            }
        }

        txtVehicleCode.setEnabled(false);
        txtShopCode.setEnabled(false);
        txtProductNo.setEnabled(false);
        lovParallel.setSelectedItem("00");

        lineCode = paramters.getStringValue(SDVPropertyConstant.STATION_LINE);
        stationCode = paramters.getStringValue(SDVPropertyConstant.STATION_STATION_CODE);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        curDataMap = saveData();
        return curDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        return getLocalDataMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new InitOperation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_FAILED)
            return;
        if (dataset == null)
            return;
        IDataMap dataMap = dataset.getDataMap(this.getId());
        loadInitData(dataMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
     */
    @Override
    public void uiLoadCompleted() {

    }

    /**
     * 작성후 저장된 Data
     * 
     * @method saveData
     * @date 2013. 11. 20.
     * @param
     * @return Map<String,Object>
     * @exception
     * @throws
     * @see
     */
    private IDataMap saveData() {

        RawDataMap savedDataMap = new RawDataMap();
        List<String> dwgNoList = new ArrayList<String>();
        for (TableItem item : table.getItems()) {
            String itemText = item.getText();
            if (itemText.equals(""))
                continue;
            dwgNoList.add(itemText);
        }

        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, txtVehicleCode.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_SHOP, txtShopCode.getText());

        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, txtOpCode.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, txtProductNo.getText());

        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, lovParallel.getSelectedString());

        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, txtOpKorName.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, txtOpEngName.getText());

        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_DR, lovDR.getSelectedString());

        savedDataMap.put(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT, txtWorkerCount.getText());

        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, dwgNoList, IData.LIST_FIELD);
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_KPC, btnSpecialStation.getSelection(), IData.BOOLEAN_FIELD);

        savedDataMap.put(SDVPropertyConstant.STATION_LINE, lineCode);
        savedDataMap.put(SDVPropertyConstant.STATION_STATION_CODE, stationCode);
        
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 특별 특성 속성  추가
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, lovSpecialChar.getSelectedString());
	
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return savedDataMap;
    }

    /**
     * 초기 Data Load Operation
     * Class Name : InitOperation
     * Class Description :
     * 
     * @date 2013. 12. 3.
     * 
     */
    public class InitOperation extends AbstractSDVInitOperation {

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
         */
        @Override
        public void executeOperation() throws Exception {
            IDataMap displayDataMap = new RawDataMap();
            String opKorName = "", opEngName = "", line = "", stationCode = "";
            String vehicleCode = "", productCode = "", shopCode = "", opCode = "";
            String bopVersion = "", dr = "", workerCount = "";
            /////////////////////////////////////////////////////////////////////////////////
            // 특별 특성 속성 추가
            String specialChar = "";
            ////////////////////////////////////////////////////////////////////////////////
            boolean isKPC = false;

            List<String> insDwgNoList = new ArrayList<String>();
            try {

                MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                TCComponentBOMLine topBOPLine = mfgApp.getBOMWindow().getTopBOMLine();

                // 선택된 공법
                TCComponentBOMLine selectedBOPLine = mfgApp.getSelectedBOMLines()[0];
                TCComponentItem selectedItem = selectedBOPLine.getItem();
                TCComponentItemRevision selectedItemRevision = selectedBOPLine.getItemRevision();

                // 공법 생성일 경우
                if (currentConfigId == 0) {
                    // 차종 코드
                    vehicleCode = topBOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
                    productCode = topBOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE);
                    shopCode = topBOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_SHOP);

                    // 선택된 것이 공정 일 경우
                    if (selectedItem.getType().equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)) {
                        line = selectedItemRevision.getProperty(SDVPropertyConstant.STATION_LINE); // Line 코드
                        stationCode = selectedItemRevision.getProperty(SDVPropertyConstant.STATION_STATION_CODE); // 공정 코드
                    }

                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vehicleCode);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productCode);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_SHOP, shopCode);

                } else {
                    /**
                     * Item 속성
                     */
                    opKorName = selectedItem.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME);
                    opEngName = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
                    /**
                     * Revision 속성
                     */
                    vehicleCode = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
                    shopCode = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_SHOP);
                    opCode = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
                    productCode = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE);
                    bopVersion = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
                    dr = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_DR);
                    
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 특별 특성 속성 추가
                    specialChar = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    TCProperty workerCountObj = selectedItemRevision.getTCProperty(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);// 작업인원
                    if (!workerCountObj.isNull())
                        workerCount = Integer.toString(workerCountObj.getIntValue());

                    line = selectedItemRevision.getProperty(SDVPropertyConstant.STATION_LINE); // Line 코드
                    stationCode = selectedItemRevision.getProperty(SDVPropertyConstant.STATION_STATION_CODE); // 공정 코드
                    isKPC = selectedItemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_KPC).getBoolValue(); // 특수공정여부

                    String[] insDwgNoArray = selectedItemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getStringArrayValue();
                    Collections.addAll(insDwgNoList, insDwgNoArray);
                }

                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, opKorName);
                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, opEngName);

                displayDataMap.put(SDVPropertyConstant.STATION_LINE, line);
                displayDataMap.put(SDVPropertyConstant.STATION_STATION_CODE, stationCode);

                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vehicleCode);
                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_SHOP, shopCode);
                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opCode);
                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productCode);
                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, bopVersion);
                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_DR, dr);
                displayDataMap.put(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT, workerCount);

                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, insDwgNoList, IData.LIST_FIELD);
                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_KPC, isKPC, IData.BOOLEAN_FIELD);
                
                ////////////////////////////////////////////////////////////////////////////////////////////////////////
                //특별 특성 속성 추가
                displayDataMap.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialChar);
                ////////////////////////////////////////////////////////////////////////////////////////////////////////

                DataSet viewDataSet = new DataSet();
                viewDataSet.addDataMap(PaintOpView.this.getId(), displayDataMap);
                setData(viewDataSet);

            } catch (TCException e) {
                e.printStackTrace();
            }
        }
    }

}
