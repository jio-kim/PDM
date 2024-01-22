/**
 * 
 */
package com.symc.plm.me.sdv.view.body;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.body.CreateBodyOPInitOperation;
import com.symc.plm.me.sdv.operation.body.SaveAsBodyOPInitOperation;
import com.symc.plm.me.sdv.view.meco.MecoSelectView;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyOPView
 * Class Description : 
 * @date 2013. 11. 19.
 * 
 * [SR150515-034][20150515] shcho, 공법 복사시 화면에 국문명을 가져와 표시하지 못하는 오류 수정
 *
 */
public class CreateBodyOPView extends AbstractSDVViewPane {
    private SDVText textVehicleCode;
    private SDVText textShop;
    private SDVText textStation;
    private SDVText textOperation;
    private SDVText textOpNameKor;
    private SDVText textOpNameEng;
    private SDVText textWorkerCnt;
    // 이종화 차장님 요청 
    // 공법 생성 화면에서 특별 특성 속성 입력란 추가
    private SDVLOVComboBox combo_specialCahr;
    
    private SDVText textPlanningVer;
    private Table table;
    private TableViewer tableViewer;
    private Registry registry;
    private SDVLOVComboBox comboKPC;
    private SDVLOVComboBox comboDR;
    private TCComponentBOMLine parentStationLine;
    private String altPrefix;
    private boolean isAlt;
    private String parentLineCode;

    public static String BodyOPViewType = "BodyOPViewType";
    public static int CreateViewType = 0;
    public static int SaveAsViewType = 1;

    /**
     * @param parent
     * @param style
     * @param id
     * [CF-3537] [20230131]isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경
     */
    public CreateBodyOPView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        try
        {
            registry = Registry.getRegistry(CreateBodyOPView.class);

            Composite mainView = new Composite(parent, SWT.NONE);
            mainView.setLayout(new GridLayout(1, false));
            
            Group group = new Group(mainView, SWT.NONE);
            group.setText(registry.getString("CreateOPDialog.OP.Group.Name", "Operation Properties"));
            group.setLayout(new BorderLayout(0, 10));
            group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
            
            Composite idInfoComp = new Composite(group, SWT.NONE);
            idInfoComp.setLayoutData(BorderLayout.NORTH);
            idInfoComp.setLayout(new GridLayout(10, false));
            
            Label lblVehicleCode = new Label(idInfoComp, SWT.NONE);
            lblVehicleCode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblVehicleCode.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE));
            
            textVehicleCode = new SDVText(idInfoComp, SWT.BORDER | SWT.SINGLE);
            textVehicleCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            ((GridData) textVehicleCode.getLayoutData()).minimumWidth = 20;
            textVehicleCode.setTextLimit(2);
            textVehicleCode.setInputType(SDVText.NUMERIC);
            textVehicleCode.setEditable(false);
            
            Label lblShop = new Label(idInfoComp, SWT.NONE);
            lblShop.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblShop.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_SHOP));
            
            textShop = new SDVText(idInfoComp, SWT.BORDER | SWT.SINGLE);
            textShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textShop.setEditable(false);
            textShop.setBackground(getShell().getBackground());
            ((GridData) textShop.getLayoutData()).minimumWidth = 20;
            
            Label lblStation = new Label(idInfoComp, SWT.NONE);
            lblStation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblStation.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_STATION_CODE));
            
            textStation = new SDVText(idInfoComp, SWT.BORDER | SWT.SINGLE);
            textStation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textStation.setEditable(false);
            textStation.setBackground(getShell().getBackground());
            ((GridData) textStation.getLayoutData()).minimumWidth = 30;

            Label lblOperation = new Label(idInfoComp, SWT.NONE);
            lblOperation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblOperation.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_OPERATION_CODE));
            
            textOperation = new SDVText(idInfoComp, SWT.BORDER | SWT.SINGLE);
            textOperation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textOperation.setMandatory(true);
            textOperation.setTextLimit(1);
            ((GridData) textOperation.getLayoutData()).minimumWidth = 20;
            textOperation.setInputType(SDVText.ENGUPPER);
            
            Label lblPlanningVer = new Label(idInfoComp, SWT.NONE);
            lblPlanningVer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblPlanningVer.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_BOP_VERSION));
            
            textPlanningVer = new SDVText(idInfoComp, SWT.BORDER | SWT.SINGLE);
            textPlanningVer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textPlanningVer.setEditable(false);
            textPlanningVer.setBackground(getShell().getBackground());
            ((GridData) textPlanningVer.getLayoutData()).minimumWidth = 20;
            textPlanningVer.setText("00");
            
            Composite propInfoComp = new Composite(group, SWT.NONE);
            propInfoComp.setLayoutData(BorderLayout.CENTER);
            // 특별 특성 속성 추가를 위해 GridLayout 변경 6 -> 8 변경
            propInfoComp.setLayout(new GridLayout(8, false));
//            ((GridData) propInfoComp.getLayoutData()).minimumHeight = 100;
            
            Label lblOpNameKor = new Label(propInfoComp, SWT.NONE);
            lblOpNameKor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblOpNameKor.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_KOR_NAME));
            
            textOpNameKor = new SDVText(propInfoComp, SWT.BORDER | SWT.SINGLE);
            // 특별 특성 속성 추가로 LayOut 변경으로 인한 수정 5-> 7
            textOpNameKor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
            textOpNameKor.setMandatory(true);
            textOpNameKor.setTextLimit(80);
            ((GridData) textOpNameKor.getLayoutData()).widthHint = 100;
            
            Label lblOpNameEng = new Label(propInfoComp, SWT.NONE);
            lblOpNameEng.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblOpNameEng.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_ENG_NAME));
            
            textOpNameEng = new SDVText(propInfoComp, SWT.BORDER | SWT.SINGLE);
            // 특별 특성 속성 추가로 LayOut 변경으로 인한 수정 5-> 7
            textOpNameEng.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
            textOpNameEng.setMandatory(true);
            textOpNameEng.setTextLimit(80);
            ((GridData) textOpNameEng.getLayoutData()).widthHint = 100;
            
            Label lblDR = new Label(propInfoComp, SWT.NONE);
            lblDR.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            lblDR.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_DR));
            
            comboDR = new SDVLOVComboBox(propInfoComp, "M7_DR_TYPE");
            GridData gd_comboDR = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_comboDR.widthHint = 100;
            comboDR.setLayoutData(gd_comboDR);
            gd_comboDR.minimumWidth = 100;
            comboDR.setFixedHeight(true);
            
            Label lblKPC = new Label(propInfoComp, SWT.NONE);
            lblKPC.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_KPC));
            
            comboKPC = new SDVLOVComboBox(propInfoComp, "M7_WORK_UBODY_CHECK");
            GridData gd_comboKPC = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_comboKPC.widthHint = 80;
            comboKPC.setLayoutData(gd_comboKPC);
            gd_comboKPC.minimumWidth = 80;
            comboKPC.setFixedHeight(true);

            Label lblWorkerCnt = new Label(propInfoComp, SWT.NONE);
            lblWorkerCnt.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV, SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT));
            
            textWorkerCnt = new SDVText(propInfoComp, SWT.BORDER | SWT.SINGLE);
            GridData gd_textWorkerCnt = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_textWorkerCnt.minimumWidth = 80;
            textWorkerCnt.setLayoutData(gd_textWorkerCnt);
            textWorkerCnt.setTextLimit(2);
            textWorkerCnt.setInputType(SDVText.NUMERIC);
            
            /**
        	 * 이종화 차장님 요청
        	 * 공법 생성 화면에서 특별 특성 속성입력란 추가
        	 */
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            Label label_specialCahr = new Label(propInfoComp, SWT.NONE);
            //  차후 BMIDE 에 특별 특성 속성 추가 후 속성이름 입력
            label_specialCahr.setText("S.C");
            // 차후 BMIDE 에 특별 특성 LOV 추가 후 LOV 이름 입력
            combo_specialCahr = new SDVLOVComboBox(propInfoComp, "M7_SPECIAL_CHARICTERISTIC");
            GridData gd_specialCahr = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_specialCahr.widthHint = 80;
            combo_specialCahr.setLayoutData(gd_specialCahr);
            gd_specialCahr.minimumWidth = 80;
            combo_specialCahr.setFixedHeight(true);
            
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            Composite dwgComp = new Composite(group, SWT.NONE);
            dwgComp.setLayoutData(BorderLayout.SOUTH);
            dwgComp.setLayout(new GridLayout(3, false));
            
            Label lblNewLabel_9 = new Label(dwgComp, SWT.NONE);
            lblNewLabel_9.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
            lblNewLabel_9.setText(SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO));
            
            table = new Table(dwgComp, SWT.BORDER | SWT.FULL_SELECTION);
            tableViewer = new TableViewer(table);

            TableLayout tableLayout = new TableLayout();
            tableLayout.addColumnData(new ColumnWeightData(27, 200, true));

            table.setLayout(tableLayout);
            table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
            table.setLinesVisible(true);

            TableColumn tblclmnInstallDwgNo = new TableColumn(table, SWT.NONE);
//            tblclmnInstallDwgNo.setResizable(true);
//            tblclmnInstallDwgNo.setWidth(180);
            tblclmnInstallDwgNo.setText("Install DWG No.");

            tableViewer.setLabelProvider(new ITableLabelProvider() {
                public Image getColumnImage(Object element, int columnIndex) {
                  return null;
                }

                public String getColumnText(Object element, int columnIndex) {
                  switch (columnIndex) {
                  case 0:
                    return ((EditableTableItem) element).name;
                  default:
                    return element.toString();
                  }
                }

                public void addListener(ILabelProviderListener listener) {
                }

                public void dispose() {
                }

                public boolean isLabelProperty(Object element, String property) {
                  return false;
                }

                public void removeListener(ILabelProviderListener lpl) {
                }
              });

            tableViewer.setContentProvider(new IStructuredContentProvider() {
                public Object[] getElements(Object inputElement) {
                    return (Object[]) inputElement;
                }

                public void dispose() {
                }

                public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
                }
            });

            tableViewer.setCellModifier(new ICellModifier() {
                
                @Override
                public void modify(Object element, String property, Object value) {
                    TableItem tableItem = (TableItem) element;
                    EditableTableItem data = (EditableTableItem) tableItem.getData();
                    if ("name".equals(property))
                    {
                    	if (value.toString().length() > 20)
                    		MessageBox.post(getShell(), "Enter the 20 characters in length.", "TITLE", MessageBox.INFORMATION);
                    	else
                    		data.name = value.toString();
                    }

                    tableViewer.refresh(data);                    
                }
                
                @Override
                public Object getValue(Object element, String property) {
                    if (property.equals("name"))
                        return ((EditableTableItem) element).name;
                    else
                        return element;
                }
                
                @Override
                public boolean canModify(Object element, String property) {
                    return true;
                }
            });
            tableViewer.setCellEditors(new CellEditor[]{new TextCellEditor(table)});
            tableViewer.setColumnProperties(new String[]{"name"});

            tableViewer.setInput(new Object[]{new EditableTableItem(""),new EditableTableItem(""),new EditableTableItem("")});
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    class EditableTableItem {
        public String name;

        public EditableTableItem(String n) {
            name = n;
        }
    }

    /**
     * @param paramters
     *            the parameters to set
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
    	if (parameters != null)
    	{
    		if (parameters.containsKey(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
    		{
    			Object stationObject = parameters.get(SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
    			if (stationObject instanceof TCComponentBOMLine)
    			parentStationLine = (TCComponentBOMLine) stationObject;
    		}

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE))
    			textOperation.setText(parameters.get(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE).toString());

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_KOR_NAME))
    			textOpNameKor.setText(parameters.get(SDVPropertyConstant.OPERATION_REV_KOR_NAME).toString());

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_ENG_NAME))
    			textOpNameEng.setText(parameters.get(SDVPropertyConstant.OPERATION_REV_ENG_NAME).toString());

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_BOP_VERSION))
    			textPlanningVer.setText(parameters.get(SDVPropertyConstant.OPERATION_REV_BOP_VERSION).toString());

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_LINE))
    			parentLineCode = parameters.get(SDVPropertyConstant.OPERATION_REV_LINE).toString();

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_STATION_CODE))
    			textStation.setText(parameters.get(SDVPropertyConstant.OPERATION_REV_STATION_CODE).toString());

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_SHOP))
    			textShop.setText(parameters.get(SDVPropertyConstant.OPERATION_REV_SHOP).toString());

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE))
    			textVehicleCode.setText(parameters.get(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE).toString());

    		if (parameters.containsKey(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT))
    			textWorkerCnt.setText(parameters.get(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT).toString());

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_DR))
    			comboDR.setSelectedItem(parameters.get(SDVPropertyConstant.OPERATION_REV_DR).toString());

    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_KPC))
    			comboKPC.setSelectedItem(parameters.get(SDVPropertyConstant.OPERATION_REV_KPC).toString());

    		/**
        	 * 이종화 차장님 요청
        	 * 공법 생성 화면에서 특별 특성 속성입력란 추가
        	 */
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    		
    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC))
    			combo_specialCahr.setSelectedItem(parameters.get(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC).toString());
    		
    		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    		
    		if (parameters.containsKey(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO))
    		{
    			Object tableData = parameters.get(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
    			if (tableData instanceof List)
    			{
    				ArrayList<EditableTableItem> tableItemList = new ArrayList<EditableTableItem>();

    				for (int i = 0; i < ((List<?>) tableData).size(); i++)
    				{
    					EditableTableItem item;
    					item = new EditableTableItem(((List<?>) tableData).get(i).toString());

    					tableItemList.add(item);
    				}

    				tableViewer.setInput(tableItemList);
    			}
//    			mecoData.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, installDwgNoList, IData.OBJECT_FIELD);
    		}
    	}
    }

	@Override
	public void setLocalDataMap(IDataMap dataMap) {
	}

	
	@Override
	public IDataMap getLocalDataMap() {
		RawDataMap mecoData = new RawDataMap();

		mecoData.put(SDVTypeConstant.BOP_PROCESS_STATION_ITEM, parentStationLine, IData.OBJECT_FIELD);
		mecoData.put(BodyOPViewType, textOperation.getData(), IData.OBJECT_FIELD);
		mecoData.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, textOperation.getText());
		mecoData.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, textOpNameKor.getText());
		mecoData.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, textOpNameEng.getText());
		mecoData.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, textPlanningVer.getText());
		mecoData.put(SDVPropertyConstant.OPERATION_REV_LINE, parentLineCode);
		mecoData.put(SDVPropertyConstant.OPERATION_REV_STATION_CODE, textStation.getText());
		mecoData.put(SDVPropertyConstant.OPERATION_REV_SHOP, textShop.getText());
		mecoData.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, textVehicleCode.getText());
		mecoData.put(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT, textWorkerCnt.getText());
		mecoData.put(SDVPropertyConstant.OPERATION_REV_DR, comboDR.getSelectedString());
		mecoData.put(SDVPropertyConstant.OPERATION_REV_KPC, comboKPC.getSelectedString());
		mecoData.put(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, isAlt, IData.BOOLEAN_FIELD);
		mecoData.put(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, altPrefix);
		
		/**
    	 * 이종화 차장님 요청
    	 * 공법 생성 화면에서 특별 특성 속성입력란 추가
    	 */
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		mecoData.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, combo_specialCahr.getSelectedString());
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		
		// [SR150323-035][20150507]shcho, 차체 공법 Copy 기능 오류 수정 (DwgNo 표기시 배열 오류 나는 부분 수정)
		ArrayList<String> installDwgNoList = new ArrayList<String>();
		
		if (getId().equals("createOPView")) {
			String installDwgNo1 = table.getItem(0).getText().trim();
			String installDwgNo2 = table.getItem(1).getText().trim();		
			String installDwgNo3 = table.getItem(2).getText().trim();
			
			if (installDwgNo1 != null && installDwgNo1.trim().length() > 0)
				installDwgNoList.add(installDwgNo1);
			if (installDwgNo2 != null && installDwgNo2.trim().length() > 0)
				installDwgNoList.add(installDwgNo2);
			if (installDwgNo3 != null && installDwgNo3.trim().length() > 0)
				installDwgNoList.add(installDwgNo3);
		} else {
			TableItem[] tableItem = table.getItems();
			for(int i=0; i<tableItem.length; i++) {
				if(i > 2) {
					break;
				}
				
				String installDwgNo = table.getItem(i).getText().trim();
				if (installDwgNo != null && installDwgNo.trim().length() > 0) {
					installDwgNoList.add(installDwgNo);
				}
			}
		}

		mecoData.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, installDwgNoList, IData.LIST_FIELD);

		return mecoData;
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
		if (getId().equals("createOPView"))
			return new CreateBodyOPInitOperation();
		else
			return new SaveAsBodyOPInitOperation();
	}

	@Override
	public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
		// 오퍼의 결과를 화면에 설정하는 함수
		if (result == SDVInitEvent.INIT_SUCCESS)
		{
			if (dataset != null)
			{
            	AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
				Object viewType = dataset.getValue(BodyOPViewType);
				if (viewType != null && viewType.equals(CreateViewType))
				{
					parentStationLine = (TCComponentBOMLine) dataset.getValue(SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
					
					try
					{
						parentLineCode = parentStationLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
						textStation.setText(parentStationLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_REV_CODE));
						textShop.setText(parentStationLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_SHOP));
						textVehicleCode.setText(parentStationLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_VEHICLE_CODE));
						isAlt = parentStationLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.STATION_IS_ALTBOP);
						altPrefix = parentStationLine.getItemRevision().getProperty(SDVPropertyConstant.STATION_ALT_PREFIX);
						// Default Value set
						textOperation.setText("N");

			    	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
			    	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경 */
//						IViewPane mecoView = dialog.getView("mecoView");
                		IViewPane mecoView = dialog.getView(SDVPropertyConstant.MECO_SELECT);

                		if (isAlt)
						{
		                	dialog.setAddtionalTitle("Alternative BOP");
		                	dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));
//		                	((SelectedMECOView) mecoView).setAlternative(false);
		                	((MecoSelectView) mecoView).setAlternative(false);
						}
                		else
                		{
                			TCComponent mecoItemRevision = parentStationLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.STATION_MECO_NO);
                			if (mecoItemRevision != null)
                			{
                				if (! CustomUtil.isReleased(mecoItemRevision))
                				{
                					HashMap<String, Object> dataMap = new HashMap<String, Object>();

                					dataMap.put(SDVTypeConstant.MECO_ITEM_REV, mecoItemRevision);

                					mecoView.setParameters(dataMap);
                				}
                			}
                		}
//                        ((SelectedMECOView) mecoView).visibleSearchBtn(false);
                        ((MecoSelectView) mecoView).visibleSearchBtn(false);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				else if (viewType != null && viewType.equals(SaveAsViewType))
				{
					try
					{
						TCComponentBOMLine targetOPLine = (TCComponentBOMLine) dataset.getValue(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM);
						parentStationLine = targetOPLine.parent();

						parentLineCode = targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_LINE);
						textStation.setText(targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_CODE));
						textStation.setEditable(true);
						textShop.setText(targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_SHOP));
						textVehicleCode.setText(targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE));
						isAlt = targetOPLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP);
						altPrefix = targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
						textOpNameEng.setText(targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME));
						textOpNameKor.setText(targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME)); //[SR150515-034][20150515] shcho, 공법 복사시 화면에 국문명을 가져와 표시하지 못하는 오류 수정
						String op_code = targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
						textOperation.setText(op_code.substring(op_code.length() - 1));
						textOperation.setData(targetOPLine);
						comboDR.setSelectedItem(targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_DR));
						boolean kpc = targetOPLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.OPERATION_REV_KPC);
						comboKPC.setSelectedItem(kpc ? "Y" : "N");
						textWorkerCnt.setText(targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT));
						String[] dwg_nos = targetOPLine.getItemRevision().getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getStringValueArray();
						
						/////////////////////////////////////////////////////////////////////////////////////////////////////////////
						
						String specialChar = targetOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
						combo_specialCahr.setText(specialChar);
						
						////////////////////////////////////////////////////////////////////////////////////////////////////////////
						
						
						if (dwg_nos != null && dwg_nos.length > 0)
						{
							ArrayList<EditableTableItem> table_item_list = new ArrayList<CreateBodyOPView.EditableTableItem>();

							for (String dwg_no : dwg_nos)
							{
								if (dwg_no != null && dwg_no.trim().length() > 0)
									table_item_list.add(new EditableTableItem(dwg_no));
							}

							if (table_item_list.size() > 0)
								tableViewer.setInput(table_item_list.toArray(new EditableTableItem[0]));
						}

						IViewPane mecoView = dialog.getView("mecoView");
						if (isAlt)
						{
		                	dialog.setAddtionalTitle("Alternative BOP");
		                	dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));

		                	((SelectedMECOView) mecoView).setAlternative(false);
						}
						else
						{
							TCComponent mecoComponent = parentStationLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.STATION_MECO_NO);
							if (mecoComponent != null)
							{
								if (! CustomUtil.isReleased(mecoComponent))
								{
									HashMap<String, Object> dataMap = new HashMap<String, Object>();
									
									dataMap.put(SDVTypeConstant.MECO_ITEM_REV, mecoComponent);
									
									mecoView.setParameters(dataMap);
								}
							}
						}

	                    ((SelectedMECOView) mecoView).visibleSearchBtn(false);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void uiLoadCompleted() {
	}
}
