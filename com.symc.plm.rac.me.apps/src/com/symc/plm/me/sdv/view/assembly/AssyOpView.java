/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly;

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
import org.eclipse.swt.layout.FillLayout;
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
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : AssyOpView
 * Class Description : 공법 생성 View
 * 
 * @date 2013. 11. 15.
 * 
 */
public class AssyOpView extends AbstractSDVViewPane {
    private SDVText txtVehicleCode;
    private SDVText txtLineCode;
    private SDVText txtFunctionCode;
    private SDVText txtOpCode;
    private SDVText txtProductNo;
    private SDVText txtBopVersion;
    private SDVText txtOpKorName;
    private SDVText txtOpEngName;
    private SDVText txtStationCode;
    private SDVText txtOperaterCode;
    private SDVText txtOpSequence;
    private Table table;
    private SDVLOVComboBox lovDR;
    private SDVLOVComboBox lovWorkLocation;
    private SDVLOVComboBox lovAssySystem;
    private SDVLOVComboBox lovUBodyWork;
    private SDVLOVComboBox lovItemLocation;
    // 이종화 차장님 요청 
    // 공법 생성 화면에서 특별 특성 속성 입력란 추가
    private SDVLOVComboBox combo_specialChar;
    
    private Button btnCheckMaxTime;
    private Button btnCheckRepVehicleButton;

    private TCSession tcSession = null;

    private IDataMap curDataMap = null;

    private int currentConfigId = 0;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public AssyOpView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    public AssyOpView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId, null);
    }

    public AssyOpView(Composite parent, int style, String id, int configId, String order) {
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

        BorderLayout borderLayout = new BorderLayout();
        parent.setLayout(borderLayout);

        Group groupOp = new Group(parent, SWT.NONE);
        groupOp.setText(registry.getString("OpInform.NAME"));
        groupOp.setLayoutData(BorderLayout.NORTH);
        groupOp.setLayout(new GridLayout(1, false));

        Composite groupOpComposite = new Composite(groupOp, SWT.NONE);
        GridLayout gl_groupOpComposite = new GridLayout(5, false);
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

        Label lblLineCode = new Label(groupOpComposite, SWT.NONE);
        lblLineCode.setText(registry.getString("LineCode.NAME"));

        txtLineCode = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtLineCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtLineCode.widthHint = 45;
        txtLineCode.setLayoutData(gd_txtLineCode);

        Label lblFunctionCode = new Label(groupOpComposite, SWT.NONE);
        lblFunctionCode.setText(registry.getString("FunctionCode.NAME"));

        txtFunctionCode = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtFunctionCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtFunctionCode.widthHint = 45;
        txtFunctionCode.setLayoutData(gd_txtFunctionCode);

        new Label(groupOpComposite, SWT.NONE);

        Label lblOpCode = new Label(groupOpComposite, SWT.NONE);
        lblOpCode.setText(registry.getString("OperationCode.NAME"));

        txtOpCode = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txt = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txt.widthHint = 45;
        txtOpCode.setLayoutData(gd_txt);

        Label lblProductNo = new Label(groupOpComposite, SWT.NONE);
        lblProductNo.setText("Product No.");

        txtProductNo = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtProductNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtProductNo.widthHint = 80;
        txtProductNo.setLayoutData(gd_txtProductNo);
        new Label(groupOpComposite, SWT.NONE);

        Label lblBopVersion = new Label(groupOpComposite, SWT.NONE);
        lblBopVersion.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        lblBopVersion.setText(registry.getString("BopVersion.NAME"));

        txtBopVersion = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtBopVersion = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtBopVersion.widthHint = 45;
        txtBopVersion.setLayoutData(gd_txtBopVersion);

        Label lblOpKorName = new Label(groupOpComposite, SWT.NONE);
        lblOpKorName.setText(registry.getString("OpKorName.NAME"));

        txtOpKorName = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtOpKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);
        gd_txtOpKorName.widthHint = 340;
        txtOpKorName.setLayoutData(gd_txtOpKorName);

        Label lblOpEngName = new Label(groupOpComposite, SWT.NONE);
        lblOpEngName.setText(registry.getString("OpEngName.NAME"));

        txtOpEngName = new SDVText(groupOpComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtOpEngName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);
        gd_txtOpEngName.widthHint = 340;
        txtOpEngName.setLayoutData(gd_txtOpEngName);

        Label lblDR = new Label(groupOpComposite, SWT.NONE);
        lblDR.setText("DR");

        lovDR = new SDVLOVComboBox(groupOpComposite, SWT.BORDER, tcSession, "M7_DR_TYPE");
        GridData gridData_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData_1.widthHint = 45;
        lovDR.setLayoutData(gridData_1);
        
        /**
    	 * 이종화 차장님 요청
    	 * 공법 생성 화면에서 특별 특성 속성입력란 추가
    	 */
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        Label label_specialCahr = new Label(groupOpComposite, SWT.NONE);
        //  차후 BMIDE 에 특별 특성 속성 추가 후 속성이름 입력
        label_specialCahr.setText("S.C");
        // 차후 BMIDE 에 특별 특성 LOV 추가 후 LOV 이름 입력
        combo_specialChar = new SDVLOVComboBox(groupOpComposite, "M7_SPECIAL_CHARICTERISTIC");
        GridData gd_specialCahr = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_specialCahr.widthHint = 80;
        combo_specialChar.setLayoutData(gd_specialCahr);
        gd_specialCahr.minimumWidth = 80;
        combo_specialChar.setFixedHeight(true);
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Group groupStation = new Group(parent, SWT.NONE);
        groupStation.setLayoutData(BorderLayout.CENTER);
        groupStation.setText(registry.getString("OpStationInform.NAME"));
        groupStation.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite groupStationComposite = new Composite(groupStation, SWT.NONE);
        GridLayout gl_groupStationComposite = new GridLayout(5, false);
        groupStationComposite.setLayout(gl_groupStationComposite);
        gl_groupStationComposite.horizontalSpacing = 10;
        gl_groupStationComposite.marginLeft = 10;

        Label lblStationCode = new Label(groupStationComposite, SWT.NONE);
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData.widthHint = 100;
        lblStationCode.setLayoutData(gridData);
        lblStationCode.setText(registry.getString("OpStationNo.NAME"));

        txtStationCode = new SDVText(groupStationComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtStationCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtStationCode.widthHint = 80;
        txtStationCode.setLayoutData(gd_txtStationCode);
        new Label(groupStationComposite, SWT.NONE);

        Label lblOperaterCode = new Label(groupStationComposite, SWT.NONE);
        GridData gridData_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData_2.widthHint = 80;
        lblOperaterCode.setLayoutData(gridData_2);
        lblOperaterCode.setText(registry.getString("WorkerCode.NAME"));

        txtOperaterCode = new SDVText(groupStationComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtOperaterCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtOperaterCode.widthHint = 80;
        txtOperaterCode.setLayoutData(gd_txtOperaterCode);

        Label lblOpSequence = new Label(groupStationComposite, SWT.NONE);
        lblOpSequence.setText(registry.getString("OpProcessSeq.NAME"));

        txtOpSequence = new SDVText(groupStationComposite, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtOpSequence = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtOpSequence.widthHint = 80;
        txtOpSequence.setLayoutData(gd_txtOpSequence);
        new Label(groupStationComposite, SWT.NONE);

        Label lblOpWorkLocation = new Label(groupStationComposite, SWT.NONE);
        lblOpWorkLocation.setText(registry.getString("WorkArea.NAME"));

        lovWorkLocation = new SDVLOVComboBox(groupStationComposite, SWT.BORDER, tcSession, "M7_WORK_LOCATION");
        GridData gd_lovOpLocation = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lovOpLocation.widthHint = 190;
        lovWorkLocation.setLayoutData(gd_lovOpLocation);

        Group groupEtc = new Group(parent, SWT.NONE);
        groupEtc.setLayoutData(BorderLayout.SOUTH);
        groupEtc.setText(registry.getString("EtcInform.NAME"));
        groupEtc.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite groupEtcComposite = new Composite(groupEtc, SWT.NONE);
        GridLayout gl_groupEtcComposite = new GridLayout(4, false);
        groupEtcComposite.setLayout(gl_groupEtcComposite);
        gl_groupEtcComposite.horizontalSpacing = 10;
        gl_groupEtcComposite.marginLeft = 10;
        groupEtcComposite.setLayout(gl_groupEtcComposite);

        Label lbldwgNo = new Label(groupEtcComposite, SWT.NONE);
        lbldwgNo.setText(registry.getString("InsDwgNo.NAME"));

        table = new Table(groupEtcComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.VIRTUAL);
        GridData gd_table = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
        gd_table.widthHint = 140;
        gd_table.heightHint = 40;
        table.setLayoutData(gd_table);
        table.setLinesVisible(true);

        Label lblSystem = new Label(groupEtcComposite, SWT.NONE);
        lblSystem.setText(registry.getString("AssySystem.NAME"));

        lovAssySystem = new SDVLOVComboBox(groupEtcComposite, SWT.BORDER, tcSession, "M7_SYSTEM_CODE");
        GridData gd_comboSystem = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gd_comboSystem.widthHint = 80;
        lovAssySystem.setLayoutData(gd_comboSystem);

        Label lblUBodyWork = new Label(groupEtcComposite, SWT.NONE);
        lblUBodyWork.setText("U/Body Work");

        lovUBodyWork = new SDVLOVComboBox(groupEtcComposite, SWT.BORDER, tcSession, "M7_WORK_UBODY_CHECK");
        GridData gd_lovUBodyWork = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lovUBodyWork.widthHint = 45;
        lovUBodyWork.setLayoutData(gd_lovUBodyWork);

        Label lblPartLocation = new Label(groupEtcComposite, SWT.NONE);
        lblPartLocation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        lblPartLocation.setText(registry.getString("ItemLocation.NAME"));

        lovItemLocation = new SDVLOVComboBox(groupEtcComposite, SWT.BORDER, tcSession, "M7_ITEM_LOCATION");
        GridData gd_lovItemLocation = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lovItemLocation.widthHint = 80;
        lovItemLocation.setLayoutData(gd_lovItemLocation);

        Label lblMaxTimeCheck = new Label(groupEtcComposite, SWT.NONE);
        lblMaxTimeCheck.setText(registry.getString("IsMaxTime.NAME"));

        btnCheckMaxTime = new Button(groupEtcComposite, SWT.CHECK);

        Label lblMainVehicleCheck = new Label(groupEtcComposite, SWT.NONE);
        lblMainVehicleCheck.setText(registry.getString("IsRepVehicle.NAME"));

        btnCheckRepVehicleButton = new Button(groupEtcComposite, SWT.CHECK);
        btnCheckRepVehicleButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setWidth(155);

        /**
         * Mandatory 설정
         */
        txtVehicleCode.setMandatory(true);
        txtLineCode.setMandatory(true);
        txtFunctionCode.setMandatory(true);
        txtOpCode.setMandatory(true);
        txtProductNo.setMandatory(true);
        txtBopVersion.setMandatory(true);
        txtOpKorName.setMandatory(true);
        // txtOpEngName.setMandatory(true);
        txtStationCode.setMandatory(true);
        txtOperaterCode.setMandatory(true);
        txtOpSequence.setMandatory(true);
        lovWorkLocation.setMandatory(true);
        lovAssySystem.setMandatory(true);
        lovUBodyWork.setMandatory(true);

        /**
         * 자릿수 지정
         */
        txtFunctionCode.setTextLimit(3);
        txtOpCode.setTextLimit(4);
        txtBopVersion.setTextLimit(2);
        txtStationCode.setTextLimit(6);
        txtOperaterCode.setTextLimit(6);
        txtOpSequence.setTextLimit(3);

        /**
         * Text 유형 지정
         */
        txtBopVersion.setInputType(SDVText.NUMERIC);
        //txtOpSequence.setInputType(SDVText.NUMERIC);

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
        
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * 공법 수정 화면 추가로 인한 로직 수정 
         * Dilaog Id 가 Modify 일때만 작동
         * MECO 는 수정 불가 
         * 공법의 Item ID 에 영향을 주는 속성 정보들도 수정 불가
         */
        
        try {
        
        	IDialog dialog = UIManager.getCurrentDialog();
        	
        	if( dialog.getId().contains("Modify")) {
        		   MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        	        // 현재 BOM WINDOW
        		   TCComponentBOMLine selectedBOMLine = mfgApp.getSelectedBOMLines()[0];
        		   
        		   TCComponentItemRevision selectedBOMRevision = selectedBOMLine.getItemRevision();
        		   String revisionId = selectedBOMRevision.getStringProperty("item_revision_id");
        		   
        			    txtVehicleCode.setEnabled(false);
        		        txtLineCode.setEnabled(false);
        		        txtFunctionCode.setEnabled(false);
        		        txtOpCode.setEnabled(false);
        		        txtProductNo.setEnabled(false);
        		        txtBopVersion.setEnabled(false);
        		        txtOpKorName.setEnabled(false);
        		        txtOpEngName.setEnabled(false);
        		   }
        	
        }catch ( Exception e ) {
        	e.printStackTrace();
        }
        
     ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

        /**
         * 공법 정보
         */
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, txtVehicleCode.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_SHOP, this.txtLineCode.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, this.txtFunctionCode.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, this.txtOpCode.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, this.txtProductNo.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, this.txtBopVersion.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, this.txtOpKorName.getText());
        
        // [Non-SR] 이종화 차장님 요청
        // 수정자 : bc.kim
        // 수정내용 : 공법 복사시 기존 공법의 영문작업표준서 이름 복사 항목에서 제외
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, this.txtOpEngName.getText());
        ////////////////////////////////////////////////////////////////////////////////////////////////
        
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_DR, this.lovDR.getSelectedString());
        /**
         * 공정 정보
         */
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_STATION_NO, this.txtStationCode.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_WORKER_CODE, this.txtOperaterCode.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_PROCESS_SEQ, this.txtOpSequence.getText());
        savedDataMap.put(SDVPropertyConstant.OPERATION_WORKAREA, this.lovWorkLocation.getSelectedString());
        
        ////////////////////////////////////////////////////////////////////////////////////////////////
        
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, this.combo_specialChar.getSelectedString());
        
        ////////////////////////////////////////////////////////////////////////////////////////////////
        
        /**
         * 기타정보
         */
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, dwgNoList, IData.LIST_FIELD);
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, this.lovAssySystem.getSelectedString());
        savedDataMap.put(SDVPropertyConstant.OPERATION_WORK_UBODY, this.lovUBodyWork.getSelectedString());
        savedDataMap.put(SDVPropertyConstant.OPERATION_ITEM_UL, this.lovItemLocation.getSelectedString());
        savedDataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, this.btnCheckMaxTime.getSelection(), IData.BOOLEAN_FIELD);
        savedDataMap.put(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, this.btnCheckRepVehicleButton.getSelection(), IData.BOOLEAN_FIELD);

        return savedDataMap;
    }

    /**
     * Intit Data Load
     * 
     * @method loadInitData
     * @date 2013. 11. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void loadInitData(IDataMap paramters) {
        if (paramters == null)
            return;

        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE)) {
            txtProductNo.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE)); // Product Code
            txtProductNo.setEnabled(false);
        }

        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE)) {
            txtVehicleCode.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE));// vehicle Code
            txtVehicleCode.setEnabled(false);
        }

        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_KOR_NAME)) {
            txtOpKorName.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_KOR_NAME));
        }
        // [Non-SR] 이종화 차장님 요청
        // 수정자 : bc.kim
        // 수정내용 : 공법 복사시 기존 공법의 영문작업표준서 이름 복사 항목에서 제외
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_ENG_NAME)) {
            txtOpEngName.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_ENG_NAME));
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_WORKER_CODE)) {
            txtOperaterCode.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_WORKER_CODE));
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_PROCESS_SEQ)) {
            txtOpSequence.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_PROCESS_SEQ));
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_WORKAREA)) {
            String workArea = paramters.getStringValue(SDVPropertyConstant.OPERATION_WORKAREA);
            if (!workArea.isEmpty())
                lovWorkLocation.setSelectedString(workArea);
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_ITEM_UL)) {
            String itemLocation = paramters.getStringValue(SDVPropertyConstant.OPERATION_ITEM_UL);
            if (!itemLocation.isEmpty())
                lovItemLocation.setSelectedString(itemLocation);
        }
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC)) {
        	String combo_specialChartext = paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
        	if (!combo_specialChartext.isEmpty())
        		combo_specialChar.setSelectedString(combo_specialChartext);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_SHOP)) {
            txtLineCode.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_SHOP));
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE)) {
            txtFunctionCode.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE));
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE)) {
            txtOpCode.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE));
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_BOP_VERSION)) {
            txtBopVersion.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_BOP_VERSION));
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_DR)) {
            String dr = paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_DR);
            if (!dr.isEmpty())
                lovDR.setSelectedString(dr);
        }

        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_STATION_NO)) {
            txtStationCode.setText(paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_STATION_NO));
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM)) {
            String assySystem = paramters.getStringValue(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM);
            if (!assySystem.isEmpty())
                lovAssySystem.setSelectedString(assySystem);
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_WORK_UBODY)) {
            String ubody = paramters.getStringValue(SDVPropertyConstant.OPERATION_WORK_UBODY);
            if (!ubody.isEmpty())
                lovUBodyWork.setSelectedString(ubody);
        }

        if (paramters.containsKey(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK)) {
            btnCheckMaxTime.setSelection((Boolean) paramters.getValue(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK));
        }
        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK)) {
            btnCheckRepVehicleButton.setSelection((Boolean) paramters.getValue(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK));
        }

        if (paramters.containsKey(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO)) {
            @SuppressWarnings("unchecked")
            List<String> insDwgNoList = (List<String>) paramters.getListValue(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
            table.clearAll();
            for (int i = 0; i < insDwgNoList.size(); i++) {
                TableItem tableItem = table.getItem(i);
                tableItem.setText(insDwgNoList.get(i));
            }
        }
        
        
        /**
         * [Non-SR] 수정 : bc.kim
         * 			수정 내용 : 티볼리(35) 조립 공법 생성시 Shop Code를 2개(A1,A2)를 쓰기 때문에
         * 						Shop 코드에 맞게 공법 속성값을 변경 기존엔 1D로 하드코딩 되있었음
         */						
        
        try {
        	MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        	TCComponentBOMLine topBOPLine = mfgApp.getBOMWindow().getTopBOMLine();
        	String shopCode = topBOPLine.getItemRevision().getStringProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
        	if( null != shopCode && txtVehicleCode.getText().equals("35")) {
        		if(shopCode.equals("A1")) {
        			txtLineCode.setEnabled(false);
        			txtBopVersion.setText("00");
        			txtLineCode.setText("1D"); // 조립은 무조건 1D
        		} else {
        			txtLineCode.setEnabled(false);
        			txtBopVersion.setText("00");
        			txtLineCode.setText("2D"); // 조립은 무조건 1D
        		}
        	} else {
        		txtLineCode.setText("1D"); // 조립은 무조건 1D
        		txtLineCode.setEnabled(false);
        		txtBopVersion.setText("00");
        	}
        	
        } catch(Exception e) {
        	e.getStackTrace();
        }
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
        curDataMap = saveData();
        return curDataMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getRootContext()
     */
    @Override
    public Composite getRootContext() {
        return null;
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
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
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
     * 초기 Data Load Operation
     * Class Name : InitOperation
     * Class Description :
     * 
     * @date 2013. 12. 3.
     * 
     */
    public class InitOperation extends AbstractSDVInitOperation implements InterfaceAIFOperationListener {

        public InitOperation() {
            addOperationListener(this);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
         */
        @Override
        public void executeOperation() throws Exception {
            final IDataMap displayDataMap = new RawDataMap();
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            try {

                TCComponentBOMLine topBOPLine = mfgApp.getBOMWindow().getTopBOMLine();

                // 선택된 공법
                TCComponentBOMLine selectedBOPLine = mfgApp.getSelectedBOMLines()[0];
                TCComponentItem opItem = selectedBOPLine.getItem();
                TCComponentItemRevision selectedItemRevision = selectedBOPLine.getItemRevision();

                // 공법 선택이 아니면. 공법 생성일 경우...
                if (currentConfigId == 0) {
                    // 차종 코드
                    String vehicleCode = topBOPLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE);
                    String productCode = topBOPLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vehicleCode);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productCode);

                    // Line 선택이 경우 , 공정코드, 작업자 코드에 Line 코드가 입력되도록
                    if (selectedItemRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV)) {
                        String lineCode = selectedItemRevision.getProperty(SDVPropertyConstant.LINE_REV_CODE);
                        if (!lineCode.isEmpty()) {
                            displayDataMap.put(SDVPropertyConstant.OPERATION_REV_STATION_NO, lineCode.concat("-")); // Line 코드
                            displayDataMap.put(SDVPropertyConstant.OPERATION_WORKER_CODE, lineCode.concat("-")); // 작업자 코드
                        }
                    }

                } else {
                    /**
                     * Item 속성
                     */
                    String opKorName = opItem.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME);
                    String opEngName = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
                    String workerCode = opItem.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE);
                    String processSeq = opItem.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ);
                    String workArea = opItem.getProperty(SDVPropertyConstant.OPERATION_WORKAREA);
                    String workUbody = opItem.getProperty(SDVPropertyConstant.OPERATION_WORK_UBODY);
                    String itemUL = opItem.getProperty(SDVPropertyConstant.OPERATION_ITEM_UL);
                    
                    boolean isMaxWorkTimeCheck = opItem.getTCProperty(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK).getBoolValue();
                    boolean isVehicleCheck = opItem.getTCProperty(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK).getBoolValue();


                    /**
                     * Revision 속성
                     */
                    /**
                     * 공법 정보
                     */
                    String vehicleCode = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
                    String shopCode = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_SHOP);
                    String fcCode = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
                    String opCode = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
                    String productCode = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE);
                    String bopVersion = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
                    String dr = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_DR);
                    
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////
                    String specialChar = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC); 
        		    ////////////////////////////////////////////////////////////////////////////////////////////////////////
                    /**
                     * 공정 정보
                     */
                    String stationNo = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
                    /**
                     * 기타 정보
                     */
                    String[] insDwgNoArray = selectedItemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getStringArrayValue();
                    List<String> insDwgNoList = new ArrayList<String>();
                    Collections.addAll(insDwgNoList, insDwgNoArray);

                    String assySystem = selectedItemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM);

                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, opKorName);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, opEngName);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_WORKER_CODE, workerCode);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_PROCESS_SEQ, processSeq);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_WORKAREA, workArea);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_WORK_UBODY, workUbody);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_ITEM_UL, itemUL);

                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vehicleCode);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_SHOP, shopCode);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, fcCode);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opCode);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productCode);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, bopVersion);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_DR, dr);
                    
                    ////////////////////////////////////////////////////////////////////////////////////////
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialChar);
                    ////////////////////////////////////////////////////////////////////////////////////////

                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_STATION_NO, stationNo);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, insDwgNoList, IData.LIST_FIELD);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, assySystem);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, isMaxWorkTimeCheck, IData.BOOLEAN_FIELD);
                    displayDataMap.put(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, isVehicleCheck, IData.BOOLEAN_FIELD);

                }
                DataSet viewDataSet = new DataSet();
                viewDataSet.addDataMap(AssyOpView.this.getId(), displayDataMap);
                setData(viewDataSet);

            } catch (TCException e) {
                e.printStackTrace();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.InterfaceAIFOperationListener#startOperation(java.lang.String)
         */
        @Override
        public void startOperation(String paramString) {

        }

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.InterfaceAIFOperationListener#endOperation()
         */
        @Override
        public void endOperation() {

        }
    }

}
