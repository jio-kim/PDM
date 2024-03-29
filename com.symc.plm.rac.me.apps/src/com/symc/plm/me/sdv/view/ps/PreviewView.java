package com.symc.plm.me.sdv.view.ps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleEvent;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.OleListener;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnsignedShortHex;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.SDVRuntimeException;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.ui.view.table.model.ColumnInfoModel;
import org.sdv.core.util.ProgressBar;
import org.sdv.core.util.UIUtil;

import swing2swt.layout.BorderLayout;

import com.ssangyong.dto.EndItemData;
import com.symc.plm.activator.Activator;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomBOPDao;
import com.symc.plm.me.sdv.operation.common.AISInstructionDatasetCopyUtil;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetDataHelper;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetUtils;
import com.symc.plm.me.sdv.view.excel.ExcelView;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR141226-017][20150205]shcho, 작업표준서 Preview 생성 시 BOM Line Reflesh 후 생성
 * [SR150317-021] [20150323] ymjang, 국문 작업표준서 Republish 방지토록 개선
 * [SR150312-024] [20150324] ymjang, Latest Wrking for ME 상태에서 영문 작업표준서 작업 가능토록 개선
 * [SR150605-012][20150605] shcho, 영문 편집이 불가능한 경우(In Wrok 상태인 공법을 영문 Preview 시) 알림 메시지 띄우기.
 * [SR150714-008][20150713] shcho, Excel Open이 되지 않는 오류 수정
 * [NON_SR][20151002] taeku.jeong 이종화 차장님의 확인 요청으로 인해
 *                                              End Item MECO List를 읽어와서 변경기호를 표기해주는 부분의 Query 및 
 *                                              Publist 버튼 활성화 조건 추가 
 * @author 
 *
 */
public class PreviewView extends AbstractSDVViewPane {
    private Registry registry;
    
    private String processType;

    private Composite bottomComposite;
    private Composite rightComposite;
    // Table
    private Table tblResource, tblMECO, tblActivity, tblEndItem, tblSubsidiary;
    // Header 정보 Text
    private Text txtShop, txtProjectCode, txtVehicleName, txtOption, txtLine, txtDate, txtDraw, txtAppr;
    // KPC & 작업시간 합산 Text
    private Text txtKpc, txtWorkerNet, txtAdditional, txtAuto, txtNet;
    // Operation 정보 Text
    private Text txtOpName, txtOpCode, txtOpNo, txtCount, txtDr, txt_specialCha;

    private Table tblInstallDrw;

    private IDataMap localDataMap;

    private Composite viewContainer;

    private TCComponentBOPLine targetOpLine;

    protected TCComponentDataset tcDataset;

    private boolean isShowProgress = false;
    private ProgressBar progressShell;
    private Shell thisShell;

	private boolean isEnableEngPreviewButton = false;

    public PreviewView(Composite parent, int style, String id) {
        this(parent, style, id, 0);
    }

    public PreviewView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
    }
    
    @Override
	protected void create(Composite parent) {
    	
    	
        /*
         *작업표준서 변경기호 누락건에 대한 추가 로직
         * Revise BOP 기능 실행시 Replace EndItem 이 있을경우 BOM 을 새로 구성 하지만 
         * Revise 후에 Replace EndItem 이 생겼을 경우를 대비 하여 Preview 실행시 Replace End Item 유무 검사를 하여
         * 있을경우 BOM 구성을 다시함 
         */
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
       if ( getConfigId() == 0) {
    	try {
    		
    		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
    		String currentUserId = session.getUser().getUserId();
    		TCComponentBOPWindow newBopWindow = SDVBOPUtilities.createBOPWindow("Latest Working For ME");
    		Map<String, TCComponentItemRevision> endItemRevs = null;
    		TCComponentBOPLine targetOperation = null;
    		Map<String, Object> parameters =  UIManager.getCurrentDialog().getParameters();
            if(parameters!=null && parameters.containsKey("targetOperaion")) {
                HashMap<String, Object> paramMap = (HashMap<String, Object>) parameters.get("targetOperaion");
                if(paramMap != null) {
                	targetOperation = (TCComponentBOPLine) paramMap.get("OPERATION_BOPLINE");
                }
            } else {
            	targetOperation = (TCComponentBOPLine) AIFUtility.getCurrentApplication().getTargetComponent();
            } 
            String targetItemType =  targetOperation.getItem().getType();
//    		TCComponentBOPLine targetOperation = (TCComponentBOPLine) AIFUtility.getCurrentApplication().getTargetComponent();
//            'M7_BOPBodyOp', 'M7_BOPAssyOp', 'M7_BOPPaintOp'
            if( targetItemType.equals("M7_BOPBodyOp") || targetItemType.equals("M7_BOPAssyOp") || targetItemType.equals("M7_BOPPaintOp") ) {
    		TCComponentBOMLine topLine = targetOperation.window().getTopBOMLine();
    		TCComponent mProductRevision = SDVBOPUtilities.getConnectedMProductItemRevision(topLine.getItemRevision());
    		if( mProductRevision != null ) {
	    		 CustomBOPDao dao = new CustomBOPDao();
	    		 ArrayList<EndItemData> endItemList = dao.findReplacedEndItems(((TCComponentBOPLine) targetOperation).getProperty(SDVPropertyConstant.BL_ITEM_PUID),
	    				 					mProductRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
	    		 
	    		 List<EndItemData> targetEndItems = new ArrayList<EndItemData>();
	             if(endItemList != null) {
	                 String revId = ((TCComponentBOPLine) targetOperation).getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
	                 for(EndItemData endItem : endItemList) {
	                     if(revId.equals(endItem.getPitem_revision_id())) {
	                         targetEndItems.add(endItem);
	                     }
	                 }
	             }
	             
	             if(targetEndItems.size() > 0) {
	                 endItemRevs = getReplacedEndItems((TCComponentBOPLine) targetOperation, targetEndItems);
	             }
	             
	             if(endItemRevs != null && endItemRevs.size() > 0) {
	            	 
	            	boolean targetOpIsReleased = SYMTcUtil.isReleased(targetOperation.getItemRevision());
	            	int targetOpIsProcess = SYMTcUtil.isInProcess(targetOperation.getItemRevision());
	            		
	                 // End Item 교체 여부 확인 메세지 생성
	                 StringBuffer sb = new StringBuffer();
	                 sb.append(targetOperation.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
	//                 if( targetOpIsReleased || targetOpIsProcess != 0 ) {
	                	 if( targetOpIsReleased ) {
	                	 sb.append(" " + "공법에 자동 Replace 된 End Item 이 있습니다." + "\n" + "공법이 결재 완료 되었습니다." + "\n " +
	                	 		"Revise BOP 실행시 자동 변경 됩니다." + "\n");
	             	} else {
	             		sb.append(" " + "공법에 자동 Replace 된 End Item 으로  변경 합니다. " + "\n");
	             	}
	                 
	                 Iterator<String> it = endItemRevs.keySet().iterator();
	                 while(it.hasNext()) {
	                     String key = it.next();
	                     sb.append(key);
	                     sb.append(" -> ");
	                     sb.append(endItemRevs.get(key).getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
	                     sb.append("\n");
	                 }   
	//                if( targetOpIsReleased || targetOpIsProcess != 0 ) {
	                  if( targetOpIsReleased ) {
	                	 MessageBox.post(sb.toString(), "Warning", MessageBox.WARNING);
	//             	} else if ( (currentUserId.equals("infodba0") && session.hasBypass() == true) || !(targetOpIsReleased || targetOpIsProcess != 0)){
	             	} else {
	             		MessageBox.post(sb.toString(), "Warning", MessageBox.WARNING);
						TCComponentBOMViewRevision viewRevision = SDVBOPUtilities.getBOMViewRevision(targetOperation.getItemRevision(), "bom_view");
						newBopWindow.setWindowTopLine(targetOperation.getItem(), targetOperation.getItemRevision(), null, viewRevision);
						TCComponentBOMLine newOpLine = newBopWindow.getTopBOMLine();
						String newItemId = newOpLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
						// EndItemList의 OccThreadPuId를 중복 되지 않도록 List 한다.
						ArrayList<String> autoChangedOccThreadUs = new ArrayList<String>();
	                 for (int i = 0;endItemList!=null && i < endItemList.size(); i++) {
							// Operation Item Id가 동일하고 
							String parentNodeId = endItemList.get(i).getPitem_id();
	                 	if(parentNodeId!=null && parentNodeId.trim().equalsIgnoreCase(newItemId)==true){
							String occThreadPuid = endItemList.get(i).getOcc_threadu();
							if(autoChangedOccThreadUs.contains(occThreadPuid)==false){
								autoChangedOccThreadUs.add(occThreadPuid);
							}
	                 	}
	                 }
	                 
	                 // 자동변경된 BOMLine을 찾아서 변경된 Item Revsion으로 바꿔준다.
	                 for (int i = 0;autoChangedOccThreadUs!=null && i < autoChangedOccThreadUs.size(); i++) {
	                 	String occThreadPuid = autoChangedOccThreadUs.get(i);
	                 	
	                 	// Occurrence의 Thread PUID를 이용해 변경될 BOM Line을 찾는다.
	                 	TCComponentBOMLine[] findedBOMLines = newBopWindow.findConfigedBOMLinesForAbsOccID(occThreadPuid, true, newOpLine);
	                 	for (int j = 0; j < findedBOMLines.length; j++) {
	                         TCComponentItemRevision target = endItemRevs.get(findedBOMLines[j].getProperty(SDVPropertyConstant.BL_ITEM_ID));
	                         	if(target!=null){
	                         		findedBOMLines[j].replace(target.getItem(), target, null);
	                         	}
							}
						}
	
	                 newBopWindow.save();
	                 
	                 // BOMLine 일 경우 Refresh
	                 if (targetOperation instanceof TCComponentBOMLine) {
	                     ((TCComponentBOMLine) targetOperation).window().newIrfWhereConfigured(targetOperation.getItemRevision());
	                     ((TCComponentBOMLine) targetOperation).window().fireChangeEvent();
	                 	}
	             	  }
	             	}
    			}
    		
            }
    		
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
       }else
       {
			Map<String, Object> parameters = UIManager.getCurrentDialog().getParameters();
			if (parameters != null && parameters.containsKey("targetOperaion"))
			{
				HashMap<String, Object> paramMap = (HashMap<String, Object>) parameters.get("targetOperaion");
				if (paramMap != null)
				{
					isEnableEngPreviewButton = (Boolean) paramMap.get("IS_ENABLE_ENG_PREVIEW_BUTTON");
				}
			}
       }
       ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        TCComponentBOPLine operationLine = null;
        Map<String, Object> parameters =  UIManager.getCurrentDialog().getParameters();
        if(parameters!=null && parameters.containsKey("targetOperaion")) {
            HashMap<String, Object> paramMap = (HashMap<String, Object>) parameters.get("targetOperaion");
            if(paramMap != null) {
                operationLine = (TCComponentBOPLine) paramMap.get("OPERATION_BOPLINE");
            }
        } else {
            operationLine = (TCComponentBOPLine) AIFUtility.getCurrentApplication().getTargetComponent();
        } 
        
		boolean isReleased = false;
		TCComponentItemRevision mecoRevision = null;
		TCComponentItemRevision operationRevision = null;

		if (operationLine != null)
		{
			try
			{
				operationRevision = (TCComponentItemRevision) operationLine.getItemRevision();
				if (operationRevision != null)
				{
					mecoRevision = (TCComponentItemRevision) operationRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
				}
				if (mecoRevision != null)
				{
					isReleased = CustomUtil.isReleased(mecoRevision);
				}
			} catch (TCException e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			if (!isReleased && mecoRevision != null && operationRevision != null && operationRevision.okToModify())
			{
				new CustomUtil().buildMEPL((TCComponentChangeItemRevision) mecoRevision, true);
				System.out.println("MEPL 생성");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			MessageBox.post(getShell(), e, true);
		}
		super.create(parent);
	}

	@Override
    protected void initUI(Composite parent) {

        registry = Registry.getRegistry(this);
        thisShell = getShell();

        localDataMap = new RawDataMap();
        localDataMap.put("configId", new RawData("configId", getConfigId(), IData.INTEGER_FIELD));

        parent.setLayout(new FillLayout());

        SashForm leftRightSashForm = new SashForm(parent, SWT.NONE);
        SashForm topBottomSashForm = new SashForm(leftRightSashForm, SWT.VERTICAL);

        viewContainer = new Composite(topBottomSashForm, SWT.NONE);
        viewContainer.setLayout(new BorderLayout());
        addViewContainer("excelView", viewContainer);

        bottomComposite = new Composite(topBottomSashForm, SWT.NONE);
        bottomComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

        initResourceTable();
        initMECOTable();

        rightComposite = new Composite(leftRightSashForm, SWT.NONE);
        GridLayout gridLayout = new GridLayout(11, false);
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 0;
        rightComposite.setLayout(gridLayout);

        initHeader();
        initTimeSum();
        initActivityTable();
        initEndItemTable();
        initSubsidiaryTable();
        initOperationInfo();

        topBottomSashForm.setWeights(new int[] { 4, 1 });
        leftRightSashForm.setWeights(new int[] { 570, 630 });

        Map<String, Object> paramMap = getParameters();
        if(paramMap == null) {
            paramMap = new HashMap<String, Object>();
        }
        paramMap.put("langConfigId", getConfigId());
        setParameters(paramMap);
    }

    private void initResourceTable() {
        tblResource = new Table(bottomComposite, SWT.BORDER | SWT.FULL_SELECTION);
        tblResource.setHeaderVisible(true);
        tblResource.setLinesVisible(true);

        String[] columnNameArr = registry.getStringArray("table.column.resource.name." + getConfigId());
        String[] columnWidthArr = registry.getStringArray("table.column.resource.width");
        String[] columnAlignArr = registry.getStringArray("table.column.resource.alignment");

        if(columnNameArr != null) {
            List<ColumnInfoModel> columnInfoModelList = new ArrayList<ColumnInfoModel>();
            for(int i = 0; i < columnAlignArr.length; i++) {
                ColumnInfoModel columnInfo = new ColumnInfoModel();
                columnInfo.setColName(columnNameArr[i]);
                columnInfo.setColumnWidth(Integer.parseInt(columnWidthArr[i]));
                columnInfo.setAlignment(getAlignmentToInteger(columnAlignArr[i]));

                columnInfoModelList.add(columnInfo);
            }

            tblResource = setTableColumn(tblResource, columnInfoModelList);
        }
    }

    private void initMECOTable() {
        tblMECO = new Table(bottomComposite, SWT.BORDER | SWT.FULL_SELECTION);
        tblMECO.setHeaderVisible(true);
        tblMECO.setLinesVisible(true);

        String[] columnNameArr = registry.getStringArray("table.column.meco.name." + getConfigId());
        String[] columnWidthArr = registry.getStringArray("table.column.meco.width");
        String[] columnAlignArr = registry.getStringArray("table.column.meco.alignment");

        if(columnNameArr != null) {
            List<ColumnInfoModel> columnInfoModelList = new ArrayList<ColumnInfoModel>();
            for(int i = 0; i < columnAlignArr.length; i++) {
                ColumnInfoModel columnInfo = new ColumnInfoModel();
                columnInfo.setColName(columnNameArr[i]);
                columnInfo.setColumnWidth(Integer.parseInt(columnWidthArr[i]));
                columnInfo.setAlignment(getAlignmentToInteger(columnAlignArr[i]));

                columnInfoModelList.add(columnInfo);
            }

            tblMECO = setTableColumn(tblMECO, columnInfoModelList);
        }
    }

    private void setLable(Composite parent, String[] textArray, String[] widthArray, String[] heightArray, String[] horizontalSpanArray, String[] verticalSpanArray) {
        for (int i = 0; i < textArray.length; i++) {
            Label label = new Label(parent, SWT.BORDER);
            label.setAlignment(SWT.CENTER);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false, Integer.parseInt(horizontalSpanArray[i]), Integer.parseInt(verticalSpanArray[i]));
            if (widthArray != null) {
                gridData.widthHint = Integer.parseInt(widthArray[i]);
            }
            if (heightArray != null) {
                gridData.heightHint = Integer.parseInt(heightArray[i]);
            }
            label.setLayoutData(gridData);
            label.setText(textArray[i]);
        }
    }

    private void initHeader() {
        String[] textArray = registry.getStringArray("header.label.name." + getConfigId());
        String[] widthArray = registry.getStringArray("header.label.width");
        String[] horizontalSpanArray = registry.getStringArray("header.label.horizontalspan");
        String[] verticalSpanArray = registry.getStringArray("header.label.verticalspan");

        setLable(rightComposite, textArray, widthArray, null, horizontalSpanArray, verticalSpanArray);

        txtShop = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtShop.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtShop = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2);
        gd_txtShop.heightHint = 42;
        gd_txtShop.widthHint = 61;
        txtShop.setLayoutData(gd_txtShop);

        txtProjectCode = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtProjectCode.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtProjectCode = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_txtProjectCode.widthHint = 65;
        txtProjectCode.setLayoutData(gd_txtProjectCode);

        txtOption = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtOption.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtOption = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 2);
        gd_txtOption.widthHint = 69;
        txtOption.setLayoutData(gd_txtOption);

        txtLine = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtLine.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtLine = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 2);
        gd_txtLine.widthHint = 65;
        txtLine.setLayoutData(gd_txtLine);

        txtDate = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER | SWT.MULTI);
        txtDate.setFont(SWTResourceManager.getFont("맑은 고딕", 8, SWT.NORMAL));
        txtDate.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtDate = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2);
        gd_txtDate.widthHint = 35;
        txtDate.setLayoutData(gd_txtDate);

        txtDraw = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER) ;
        txtDraw.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtDraw = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2);
        gd_txtDraw.widthHint = 59;
        txtDraw.setLayoutData(gd_txtDraw);

        txtAppr = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtAppr.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtAppr = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2);
        gd_txtAppr.widthHint = 41;
        txtAppr.setLayoutData(gd_txtAppr);

        txtVehicleName = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtVehicleName.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtVehicleName = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_txtVehicleName.widthHint = 65;
        txtVehicleName.setLayoutData(gd_txtVehicleName);
    }

    private void initTimeSum() {
    	/**
    	 * 이종화 차장님 요청
    	 * KPC/특수공정 라벨 -> 특별 특성 라벨과 텍스트로 변경
    	 */
    	////////////////////////////////////////////////////////////////////////////////////////////////////////
        Composite com_special_char = new Composite(rightComposite, SWT.NONE);
        
        GridLayout grid_specialChar = new GridLayout(2, false);
        grid_specialChar.verticalSpacing = 0;
        grid_specialChar.marginWidth = 0;
        grid_specialChar.marginHeight = 0;
        grid_specialChar.horizontalSpacing = 0;
        com_special_char.setLayout(grid_specialChar);
        
        Label label_specialChar = new Label(com_special_char, SWT.BORDER);
        label_specialChar.setAlignment(SWT.CENTER);
        GridData gridData = new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1);
        gridData.widthHint = 30;
        gridData.heightHint = 37;
        label_specialChar.setLayoutData(gridData);
        if(getConfigId() == 0) {
        	label_specialChar.setText("특별\n특성");
        } else {
        	label_specialChar.setText("S.C");
        }
        
        txt_specialCha = new Text(com_special_char, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER | SWT.MULTI);
        txt_specialCha.setFont(SWTResourceManager.getFont("HY견고딕", 10, SWT.NORMAL));
        txt_specialCha.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        txt_specialCha.setForeground(UIUtil.getColor(SWT.COLOR_BLUE));
        GridData gd_txt_specialCha = new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1);
        gd_txt_specialCha.widthHint = 30;
        gd_txt_specialCha.heightHint = 37;
        // 텍스트 의 색상 입력 필요
        // 추후 속성이 추가 되면 여기에 입력 로직 필요
        txt_specialCha.setLayoutData(gd_txt_specialCha);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        
        txtKpc = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.LEFT | SWT.MULTI);
        txtKpc.setFont(SWTResourceManager.getFont("맑은 고딕", 8, SWT.NORMAL));
        txtKpc.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtKpc = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_txtKpc.widthHint = 65;
        gd_txtKpc.heightHint = 37;
        txtKpc.setLayoutData(gd_txtKpc);

        String[] textArray = registry.getStringArray("workernet.label.name." + getConfigId());
        String[] widthArray = registry.getStringArray("workernet.label.width");
        String[] heightArray = registry.getStringArray("workernet.label.height");
        String[] horizontalSpanArray = registry.getStringArray("workernet.label.horizontalspan");
        String[] verticalSpanArray = registry.getStringArray("workernet.label.verticalspan");

        setLable(rightComposite, textArray, widthArray, heightArray, horizontalSpanArray, verticalSpanArray);

        txtWorkerNet = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtWorkerNet.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtWorkerNet = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_txtWorkerNet.heightHint = 37;
        gd_txtWorkerNet.widthHint = 65;
        txtWorkerNet.setLayoutData(gd_txtWorkerNet);

        textArray = registry.getStringArray("additional.label.name." + getConfigId());
        widthArray = registry.getStringArray("additional.label.width");
        heightArray = registry.getStringArray("additional.label.height");
        horizontalSpanArray = registry.getStringArray("additional.label.horizontalspan");
        verticalSpanArray = registry.getStringArray("additional.label.verticalspan");

        setLable(rightComposite, textArray, widthArray, heightArray, horizontalSpanArray, verticalSpanArray);

        txtAdditional = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtAdditional.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtAdditional = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_txtAdditional.heightHint = 37;
        txtAdditional.setLayoutData(gd_txtAdditional);

        textArray = registry.getStringArray("auto.label.name." + getConfigId());
        widthArray = registry.getStringArray("auto.label.width");
        heightArray = registry.getStringArray("auto.label.height");
        horizontalSpanArray = registry.getStringArray("auto.label.horizontalspan");
        verticalSpanArray = registry.getStringArray("auto.label.verticalspan");

        setLable(rightComposite, textArray, widthArray, heightArray, horizontalSpanArray, verticalSpanArray);

        txtAuto = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtAuto.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtAuto = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_txtAuto.heightHint = 37;
        txtAuto.setLayoutData(gd_txtAuto);

        textArray = registry.getStringArray("net.label.name." + getConfigId());
        widthArray = registry.getStringArray("net.label.width");
        heightArray = registry.getStringArray("net.label.height");
        horizontalSpanArray = registry.getStringArray("net.label.horizontalspan");
        verticalSpanArray = registry.getStringArray("net.label.verticalspan");

        setLable(rightComposite, textArray, widthArray, heightArray, horizontalSpanArray, verticalSpanArray);

        txtNet = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtNet.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtNet = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_txtNet.heightHint = 37;
        txtNet.setLayoutData(gd_txtNet);
    }

    private void initActivityTable() {
//        tblActivity = new Table(rightComposite, SWT.BORDER | SWT.FULL_SELECTION);
        tblActivity = new Table(rightComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd_tblActivity = new GridData(SWT.FILL, SWT.FILL, true, true, 11, 1);
        gd_tblActivity.heightHint = 243;
        tblActivity.setLayoutData(gd_tblActivity);
        tblActivity.setHeaderVisible(true);
        tblActivity.setLinesVisible(true);

        String[] columnNameArr = registry.getStringArray("table.column.activity.name." + getConfigId());
        String[] columnWidthArr = registry.getStringArray("table.column.activity.width");
        String[] columnAlignArr = registry.getStringArray("table.column.activity.alignment");

        if(columnNameArr != null) {
            List<ColumnInfoModel> columnInfoModelList = new ArrayList<ColumnInfoModel>();
            for(int i = 0; i < columnAlignArr.length; i++) {
                ColumnInfoModel columnInfo = new ColumnInfoModel();
                columnInfo.setColName(columnNameArr[i]);
                columnInfo.setColumnWidth(Integer.parseInt(columnWidthArr[i]));
                columnInfo.setAlignment(getAlignmentToInteger(columnAlignArr[i]));

                columnInfoModelList.add(columnInfo);
            }

            tblActivity = setTableColumn(tblActivity, columnInfoModelList);
        }
    }

    private int getAlignmentToInteger(String alignment) {
        alignment = alignment.toUpperCase();

        if("LEFT".equals(alignment)) {
            return SWT.LEFT;
        } else if("RIGHT".equals(alignment)) {
            return SWT.RIGHT;
        } else {
            return SWT.CENTER;
        }
    }

    private void initEndItemTable() {
        tblEndItem = new Table(rightComposite, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_tblEndItem = new GridData(SWT.FILL, SWT.FILL, true, true, 11, 1);
        gd_tblEndItem.heightHint = 149;
        tblEndItem.setLayoutData(gd_tblEndItem);
        tblEndItem.setHeaderVisible(true);
        tblEndItem.setLinesVisible(true);

        String[] columnNameArr = registry.getStringArray("table.column.enditem.name." + getConfigId());
        String[] columnWidthArr = registry.getStringArray("table.column.enditem.width");
        String[] columnAlignArr = registry.getStringArray("table.column.enditem.alignment");

        if(columnNameArr != null) {
            List<ColumnInfoModel> columnInfoModelList = new ArrayList<ColumnInfoModel>();
            for(int i = 0; i < columnAlignArr.length; i++) {
                ColumnInfoModel columnInfo = new ColumnInfoModel();
                columnInfo.setColName(columnNameArr[i]);
                columnInfo.setColumnWidth(Integer.parseInt(columnWidthArr[i]));
                columnInfo.setAlignment(getAlignmentToInteger(columnAlignArr[i]));

                columnInfoModelList.add(columnInfo);
            }

            tblEndItem = setTableColumn(tblEndItem, columnInfoModelList);
        }
    }

    private void initSubsidiaryTable() {
        tblSubsidiary = new Table(rightComposite, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_tblSubsidiary = new GridData(SWT.FILL, SWT.FILL, true, true, 11, 1);
        gd_tblSubsidiary.heightHint = 148;
        tblSubsidiary.setLayoutData(gd_tblSubsidiary);
        tblSubsidiary.setHeaderVisible(true);
        tblSubsidiary.setLinesVisible(true);

        String[] columnNameArr = registry.getStringArray("table.column.subsidiary.name." + getConfigId());
        String[] columnWidthArr = registry.getStringArray("table.column.subsidiary.width");
        String[] columnAlignArr = registry.getStringArray("table.column.subsidiary.alignment");

        if(columnNameArr != null) {
            List<ColumnInfoModel> columnInfoModelList = new ArrayList<ColumnInfoModel>();
            for(int i = 0; i < columnAlignArr.length; i++) {
                ColumnInfoModel columnInfo = new ColumnInfoModel();
                columnInfo.setColName(columnNameArr[i]);
                columnInfo.setColumnWidth(Integer.parseInt(columnWidthArr[i]));
                columnInfo.setAlignment(getAlignmentToInteger(columnAlignArr[i]));

                columnInfoModelList.add(columnInfo);
            }

            tblSubsidiary = setTableColumn(tblSubsidiary, columnInfoModelList);
        }
    }

    private void initOperationInfo() {
        String[] textArray = registry.getStringArray("operationinfo1.label.name." + getConfigId());
        String[] horizontalSpanArray = registry.getStringArray("operationinfo1.label.horizontalspan");
        String[] verticalSpanArray = registry.getStringArray("operationinfo1.label.verticalspan");

        setLable(rightComposite, textArray, null, null, horizontalSpanArray, verticalSpanArray);

        txtOpName = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER | SWT.MULTI);
        txtOpName.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        txtOpName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 7, 3));

        tblInstallDrw = new Table(rightComposite, SWT.BORDER | SWT.FULL_SELECTION);
        tblInstallDrw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 3));
        tblInstallDrw.setLinesVisible(true);

        TableColumn column = new TableColumn(tblInstallDrw, SWT.NONE);
        column.setWidth(145);

        for (int i = 0; i < 3; i++) {
            new TableItem(tblInstallDrw, SWT.CENTER);
        }

        txtOpCode = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER | SWT.MULTI);
        txtOpCode.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        txtOpCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 3));

        textArray = registry.getStringArray("operationinfo2.label.name." + getConfigId());
        horizontalSpanArray = registry.getStringArray("operationinfo2.label.horizontalspan");
        verticalSpanArray = registry.getStringArray("operationinfo2.label.verticalspan");

        setLable(rightComposite, textArray, null, null, horizontalSpanArray, verticalSpanArray);

        txtOpNo = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtOpNo.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtOpNo = new GridData(SWT.FILL, SWT.FILL, true, false, 7, 1);
        gd_txtOpNo.heightHint = 40;
        txtOpNo.setLayoutData(gd_txtOpNo);

        txtCount = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtCount.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtCount = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        gd_txtCount.heightHint = 40;
        txtCount.setLayoutData(gd_txtCount);

        txtDr = new Text(rightComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        txtDr.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
        GridData gd_txtDr = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        gd_txtDr.heightHint = 40;
        txtDr.setLayoutData(gd_txtDr);
    }

    private Table setTableColumn(Table table, List<ColumnInfoModel> columnInfoModelList) {
        for (ColumnInfoModel columnInfo : columnInfoModelList) {
            TableColumn tableColumn = new TableColumn(table, SWT.CENTER);
            tableColumn.setText(columnInfo.getColName());
            tableColumn.setWidth(columnInfo.getColumnWidth());
            tableColumn.setAlignment(columnInfo.getAlignment());
        }

        return table;
    }

    private void setResourceData(IDataMap dataMap) {
        List<HashMap<String, Object>> resourceList = (List<HashMap<String, Object>>) dataMap.getTableValue("ResourceList");
        List<HashMap<String, Object>> newResourceList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < resourceList.size(); i++) {
            HashMap<String, Object> resourceMap = resourceList.get(i);
            HashMap<String, Object> newResourceMap = new HashMap<String, Object>();
            newResourceMap.put("0", resourceMap.get("SYMBOL"));
            newResourceMap.put("1", resourceMap.get("SEQ"));
            newResourceMap.put("2", resourceMap.get(SDVPropertyConstant.ITEM_OBJECT_NAME));
            newResourceMap.put("3", resourceMap.get(SDVPropertyConstant.EQUIP_SPEC_KOR));
            newResourceMap.put("4", resourceMap.get(SDVPropertyConstant.BL_QUANTITY));
            newResourceMap.put("5", resourceMap.get(SDVPropertyConstant.EQUIP_PURPOSE_KOR));

            newResourceList.add(newResourceMap);
        }

        setTableData(tblResource, newResourceList);
    }

    private void setMECOData(IDataMap dataMap) {
        List<HashMap<String, Object>> mecoList = (List<HashMap<String, Object>>) dataMap.getTableValue("MECOList");
        List<HashMap<String, Object>> newMecoList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < mecoList.size(); i++) {
            HashMap<String, Object> mecoMap = mecoList.get(i);
            HashMap<String, Object> newMecoMap = new HashMap<String, Object>();
            newMecoMap.put("0", mecoMap.get("SYMBOL"));
            Date date = (Date) mecoMap.get(SDVPropertyConstant.ITEM_DATE_RELEASED);
            if (date != null) {
                newMecoMap.put("1", SDVStringUtiles.dateToString(date, "yyyy-MM-dd"));
            }
            String description = null;
            if (getConfigId() == 0) {
                description = (String) mecoMap.get(SDVPropertyConstant.ITEM_ITEM_ID) + "," + (String) mecoMap.get(SDVPropertyConstant.ITEM_OBJECT_DESC);
            } else {
                if(i == 0) {
                    description = registry.getString("ProcessSheetEn.InitialMECODescPrefix");
                } else {
                    description = registry.getString("ProcessSheetEn.MECODescPrefix");
                }
                description = description + " " + (String) mecoMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
            }
            newMecoMap.put("2", description);
            newMecoMap.put("3", mecoMap.get(SDVPropertyConstant.ITEM_OWNING_USER));
            newMecoMap.put("4", mecoMap.get("APPR"));

            newMecoList.add(newMecoMap);
        }

        setTableData(tblMECO, newMecoList);
    }

    private void setActivityData(IDataMap dataMap, IDataMap resourceDataMap, IDataMap operationInfoMap) throws TCException {
        List<HashMap<String, Object>> activityList = (List<HashMap<String, Object>>) dataMap.getTableValue("ActivityList");
        List<HashMap<String, Object>> resourceList = (List<HashMap<String, Object>>) resourceDataMap.getTableValue("ResourceList");
        List<HashMap<String, Object>> newActivityList = new ArrayList<HashMap<String, Object>>();
        double workerNetSum = 0.0;
        double addtionalSum = 0.0;
        double autoSum = 0.0;
        
		/////////////////////////////////////////////////////////////////////////////////////////
		// 작업표준서 특별 특성 보완 으로 인한 BomLine 추가
        
        TCComponentBOPLine bomLine = (TCComponentBOPLine)dataMap.getValue("BOMLINE_OBJECT");
        TCComponentItemRevision bomLineRevision = bomLine.getItemRevision();
		/////////////////////////////////////////////////////////////////////////////////////////

        StringBuffer kpcSpecial = new StringBuffer();

        ArrayList<String> specialCharList = new ArrayList<String>();
        if(!"A".equals(processType)) {
            String special = operationInfoMap.getStringValue(SDVPropertyConstant.OPERATION_REV_KPC);
            if(special != null && special.equalsIgnoreCase("true")) {
                kpcSpecial.append(registry.getString("SpecialStation." + getConfigId()));
                kpcSpecial.append(" ");
            } else {
                kpcSpecial.append("-");
            }
        }

        for(int i = 0; i < activityList.size(); i++) {
            HashMap<String, Object> activityMap = activityList.get(i);
            HashMap<String, Object> newActivityMap = new HashMap<String, Object>();
            newActivityMap.put("0", activityMap.get("SYMBOL"));
            newActivityMap.put("1", activityMap.get("SEQ"));
            newActivityMap.put("3", activityMap.get(SDVPropertyConstant.ITEM_OBJECT_NAME));

            String code = (String) activityMap.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
            String workTimeStr = "";
            double frequency = (Double) activityMap.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
            double workTime = frequency * (Double) activityMap.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);
            workTime = (double) Math.round(workTime * 100) / 100;

            String category = (String) activityMap.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);
            if("A".equals(processType)) {
                // 작업자 정미
                if("01".equals(category)) {
                    workerNetSum += workTime;
                    workTimeStr = String.valueOf(workTime);
                    if (frequency != 1) {
                        code = code + " X " + frequency;
                    }
                } else if("02".equals(category)) {
                    autoSum += workTime;
                    workTimeStr = "[" + workTime + "]";
                } else if("03".equals(category)) {
                    addtionalSum += workTime;
                    workTimeStr = "(" + workTime + ")";
                }
            } else {
                String overlapType = (String) activityMap.get(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE);
                if("DUPLICATE".equals(overlapType)) {
                    workTimeStr = "(" + workTime + ")";
                } else if("STANDBY".equals(overlapType)) {
                    addtionalSum += workTime;
                    workTimeStr = "<" + workTime + ">";
                } else {
                    if("01".equals(category)) {
                        workerNetSum += workTime;
                        workTimeStr = String.valueOf(workTime);
                    } else if("02".equals(category)) {
                        autoSum += workTime;
                        workTimeStr = String.valueOf(workTime);
                    }
                }
            }
            newActivityMap.put("2", code);
            newActivityMap.put("4", workTimeStr);

            String controlPoint = "";
            TCComponentBOMLine[] toolList = null;
            String strTool = "";
            String activityTool = "";
            if(activityMap.containsKey(SDVPropertyConstant.ACTIVITY_CONTROL_POINT)) {
                controlPoint = (String) activityMap.get(SDVPropertyConstant.ACTIVITY_CONTROL_POINT);
            }
            if(activityMap.containsKey(SDVPropertyConstant.ACTIVITY_TOOL_LIST)) {
                toolList = (TCComponentBOMLine[]) activityMap.get(SDVPropertyConstant.ACTIVITY_TOOL_LIST);
            }

            if(!"".equals(controlPoint.trim())) {
//                if("A".equals(processType)) {
                	if(controlPoint.startsWith("CT") || controlPoint.startsWith("RE") || controlPoint.startsWith("TR")) {
                		kpcSpecial.append(controlPoint.substring(2));
                	} else {
                		kpcSpecial.append(controlPoint);
                	}
                    kpcSpecial.append(":");
                    kpcSpecial.append((String) activityMap.get(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS));
                    kpcSpecial.append("\n");
                    /////////////////////////////////////////////////////////////////////////
                    // KPC 속성값 추가로 인한 수정 부분
                    // 
                    if( controlPoint.startsWith("CT")) {
//                    	strTool = "▼C";
//                    	Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/newdataset_16.png").createImage()
                    	 newActivityMap.put("5",Activator.imageDescriptorFromPlugin("com.symc.plm.rac.me.apps", "icons/delta_c_table.png").createImage());
                    	
                    } else if (controlPoint.startsWith("RE")) {
//                    	strTool = "▼R";
                    	 newActivityMap.put("5", Activator.imageDescriptorFromPlugin("com.symc.plm.rac.me.apps", "icons/delta_r_table.png").createImage());
                    // 한글로 시작 하는 속성값 추출
//                    	 Pattern.matches("^[가-힣]$", currentSheetName)
                    } else if(Pattern.matches("^[가-힣]*$", controlPoint)) {
                    	strTool = "KPC";
                    	 newActivityMap.put("5", strTool);
                    }
                    specialCharList.add(controlPoint);
                    /////////////////////////////////////////////////////////////////////////
//                } else {
//                	strTool = "KPC";
//                	 newActivityMap.put("5", strTool);
//                }
            }

            if(toolList != null && toolList.length > 0) {
                for(int j = 0; j < toolList.length; j++) {
                    String toolId = toolList[j].getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    for(int k = 0; k < resourceList.size(); k++) {
                        if(toolId.equals(resourceList.get(k).get(SDVPropertyConstant.ITEM_ITEM_ID))) {
                            	if(strTool.length() > 0) {
                                strTool += ",";
                            	activityTool += ",";
                            }
                            strTool += resourceList.get(k).get("SEQ");
                            activityTool += resourceList.get(k).get("SEQ");
                            break;
                        }
                    }
                }
//                if( "A".equals(processType)) {
                	newActivityMap.put("6", strTool);
//                	newActivityMap.put("6", activityTool);
//                } else {
//                	newActivityMap.put("5", strTool);
//                }
                
            }
            // 공구 번호 입력 부분

            newActivityList.add(newActivityMap);
        }

//        if( "A".equals(processType)) {
        	setTableDataWithImage(tblActivity, newActivityList);
        	
//        } else {
//        	setTableData(tblActivity, newActivityList);
//        }

        // Sum
        txtKpc.setText(kpcSpecial.toString());
        txtWorkerNet.setText(String.valueOf((double) Math.ceil(workerNetSum * 10) / 10));
        txtAdditional.setText(String.valueOf((double) Math.ceil(addtionalSum * 10) / 10));
        txtAuto.setText(String.valueOf((double) Math.ceil(autoSum * 10) / 10));
        if (!"A".equals(processType)) {
            txtNet.setText(String.valueOf((double) Math.ceil((workerNetSum + autoSum) * 10) / 10));
        }
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * 특별 특성 속성값 추가 로직
         */
        
        String specilaChar = operationInfoMap.getStringValue(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
        if ( specilaChar == null  ) {
        	specilaChar = "";
        } 
        
//        if( processType.equals("A")) {
        	boolean ctcode = false;
        	boolean recode = false;
        	boolean trcode = false;
        	
        		if( specialCharList.size() > 0 ) {
        		for( String specialChar : specialCharList ) {
        			if( specialChar.startsWith("CT")  ) {
        				ctcode = true;
        			} else if( specialChar.startsWith("RE")) {
        				recode = true;
        			} else if( specialChar.startsWith("TR")) {
        				trcode = true;
        			} 
        		}// for문 끝
        		
        		if( ctcode && trcode && !recode ) {
        			txt_specialCha.setText("C,T");
        		} else if( recode && trcode && !ctcode ) {
        			txt_specialCha.setText("R,T");
        		} else if( ctcode && !trcode && !recode ) {
        			txt_specialCha.setText("C");
        		} else if( recode && !trcode && !ctcode ) {
        			txt_specialCha.setText("R");
        		} else if ( ctcode && recode ) {
        			txt_specialCha.setText("R");
	       		}else if ( !ctcode && !recode && trcode ) {
        			txt_specialCha.setText("T");
        		} else  {
        			txt_specialCha.setText("");
        		}
        	} else {
        		txt_specialCha.setText("");
        	}
        	
//        } else {
//          txt_specialCha.setText(specilaChar);
//        }
        // BOMLine 특별 특성 속성 수정
        
        if( !specilaChar.equals(txt_specialCha.getText() == null ? "" : txt_specialCha.getText())) {
        	bomLineRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, txt_specialCha.getText() == null ? "" : txt_specialCha.getText());
        }
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
    }

    /**
     * End Item List를 Table 에 출력 해주는 함수
     * @param dataMap
     */
    private void setEndItemData(IDataMap dataMap) {
        DecimalFormat format = new DecimalFormat("0");
        List<HashMap<String, Object>> endItemList = (List<HashMap<String, Object>>) dataMap.getTableValue("EndItemList");
        List<HashMap<String, Object>> newEndItemList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < endItemList.size(); i++) {
            HashMap<String, Object> endItemMap = endItemList.get(i);
            HashMap<String, Object> newEndItemMap = new HashMap<String, Object>();
            newEndItemMap.put("0", endItemMap.get("SYMBOL"));
            newEndItemMap.put("1", endItemMap.get("SEQ"));
            newEndItemMap.put("2", endItemMap.get(SDVPropertyConstant.BL_ITEM_ID) + " " + endItemMap.get(SDVPropertyConstant.BL_ITEM_REV_ID));
            newEndItemMap.put("3", endItemMap.get(SDVPropertyConstant.BL_OBJECT_NAME));
            newEndItemMap.put("4", endItemMap.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
            String quantity = (String) endItemMap.get(SDVPropertyConstant.BL_QUANTITY);
            if(quantity != null && quantity.length() > 0) {
                double doubleQty = Double.parseDouble(quantity);
                newEndItemMap.put("5", format.format(doubleQty));
            }
            newEndItemMap.put("6", endItemMap.get(SDVPropertyConstant.BL_UNIT_OF_MEASURES));
            newEndItemList.add(newEndItemMap);
        }

        setTableData(tblEndItem, newEndItemList);
    }

    private void setSubsidiaryData(IDataMap dataMap) {
        List<HashMap<String, Object>> subsidiaryList = (List<HashMap<String, Object>>) dataMap.getTableValue("SubsidiaryList");
        List<HashMap<String, Object>> newSubsidiaryList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < subsidiaryList.size(); i++) {
            HashMap<String, Object> subsidiaryMap = subsidiaryList.get(i);
            HashMap<String, Object> newSubsidiaryMap = new HashMap<String, Object>();
            newSubsidiaryMap.put("0", subsidiaryMap.get("SYMBOL"));
            newSubsidiaryMap.put("1", subsidiaryMap.get("SEQ"));
            newSubsidiaryMap.put("2", subsidiaryMap.get(SDVPropertyConstant.ITEM_ITEM_ID) + " " + subsidiaryMap.get(SDVPropertyConstant.ITEM_REVISION_ID));
            newSubsidiaryMap.put("3", subsidiaryMap.get(SDVPropertyConstant.ITEM_OBJECT_NAME));

            String spec = (String) subsidiaryMap.get(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR);
            String option = (String) subsidiaryMap.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
            String dayOrNight = (String) subsidiaryMap.get(SDVPropertyConstant.BL_NOTE_DAYORNIGHT);
            if (spec != null && !"".equals(spec)) {
                spec = spec + "/" + option;
            } else {
                spec = option;
            }
            if (dayOrNight != null && !"".equals(dayOrNight)) {
                spec = spec + "/" + dayOrNight;
            }
            newSubsidiaryMap.put("4", spec);
            newSubsidiaryMap.put("5", subsidiaryMap.get(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY));
            newSubsidiaryMap.put("6", subsidiaryMap.get(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT));

            newSubsidiaryList.add(newSubsidiaryMap);
        }

        setTableData(tblSubsidiary, newSubsidiaryList);
    }

    private void setHeaderInfoData(IDataMap dataMap) {
        txtShop.setText(dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_SHOP_CODE));
        txtProjectCode.setText(dataMap.getStringValue(SDVPropertyConstant.MECO_REV_PROJECT_CODE));
        txtVehicleName.setText(dataMap.getStringValue(SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME));
        txtOption.setText(dataMap.getStringValue(SDVPropertyConstant.BL_OCC_MVL_CONDITION));

        if("P".equals(processType)) {
            txtLine.setText(dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME));
        } else {
            txtLine.setText(dataMap.getStringValue(SDVPropertyConstant.LINE_REV_CODE));
        }
        Date date = (Date) dataMap.getValue(SDVPropertyConstant.ITEM_CREATION_DATE);
        if(date != null) {
            txtDate.setText(SDVStringUtiles.dateToString(date, "yyyy-MM-dd HH:mm:ss"));
        }
        String owningUser = dataMap.getStringValue(SDVPropertyConstant.ITEM_OWNING_USER);
        if(owningUser != null) {
            txtDraw.setText(owningUser);
        }
        String signoff = dataMap.getStringValue(SDVPropertyConstant.WORKFLOW_SIGNOFF);
        if(signoff != null) {
            txtAppr.setText(signoff);
        }
    }

    private void setOperationInfoData(IDataMap dataMap) {
        String operationName = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_KOR_NAME);
        if(getConfigId() == 1) {
            String engOperationName = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
            if(engOperationName != null && engOperationName.length() > 0) {
                operationName = engOperationName;
            }
        }
        txtOpName.setText(operationName);

        String installDrws = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
        if (installDrws != null && installDrws.length() > 0) {
            String[] drwNos = installDrws.split(",");
            for (int i = 0; i < drwNos.length; i++) {
                tblInstallDrw.getItem(i).setText(drwNos[i]);
            }
        }

        String operationCode;
        if ("A".equals(processType)) {
            operationCode = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_STATION_NO);
            operationCode = operationCode + "\n(" + dataMap.getStringValue(SDVPropertyConstant.OPERATION_WORKER_CODE) + ")";
        } else {
            operationCode = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_STATION_CODE);
        }
        txtOpCode.setText(operationCode);
        txtOpNo.setText(dataMap.getStringValue(SDVPropertyConstant.ITEM_ITEM_ID));
        txtDr.setText(dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_DR));
        
        // [SR150317-021] [20150323] ymjang, 국문 작업표준서 Republish 방지토록 개선
        // 국문의 경우,
        if (getConfigId() == 0) {
        	
        	String pdate_released = dataMap.getStringValue(SDVPropertyConstant.ITEM_DATE_RELEASED);
        	AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
            if(pdate_released != null) {
            	
                // 2015-10-06 taeku.jeong Republish 강제로 가능하도록 수정
                boolean isForcedRepublish = false;
            	String currentUserName = AIFUtility.getCurrentApplication().getSession().getUserName();
            	if(currentUserName!=null && currentUserName.indexOf("infodba")>=0){
            		if(((TCSession)AIFUtility.getCurrentApplication().getSession()).hasBypass()==true){
            			isForcedRepublish = true;
            		}
            	}
            	
            	if(isForcedRepublish==true) {
            		setEabledButton(dialog, "Publish", true);
            	}else{
            		setEabledButton(dialog, "Publish", false);
            	}
            	
            } else {
            	
            	setEabledButton(dialog, "Publish", true);
            }
            
        }

        // [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
        // 영문의 경우,
        if (getConfigId() == 1)
        {
        	String pdate_released = dataMap.getStringValue(SDVPropertyConstant.ITEM_DATE_RELEASED);
        	AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
            if(pdate_released != null) {
             	setEabledButton(dialog, "Publish", true);
             	System.out.println("## Publish Set true --4");
             	setEabledButton(dialog, "Edit Operation", true);
            	setEabledButton(dialog, "Edit Activity", true);
            	setEabledButton(dialog, "Download", true);
            	setEabledButton(dialog, "Open KO", true);
            }
            else
            {
            	setEabledButton(dialog, "Publish", false);
            	System.out.println("## Publish Set true --5");
            	setEabledButton(dialog, "Edit Operation", false);
            	setEabledButton(dialog, "Edit Activity", false);
            	setEabledButton(dialog, "Download", false);
            	// [NON-SR][20160810] Taeku.jeong 영문작업 표준서 작업간에는 한글 작업 표준서는 볼 수 있도록 요청 (김용환 차장님)
            	setEabledButton(dialog, "Open KO", true);
            	//[SR150605-012][20150605] shcho, 영문 편집이 불가능한 경우(In Wrok 상태인 공법을 영문 Preview 시) 알림 메시지 띄우기. 
            	MessageBox.post(UIManager.getCurrentDialog().getShell(), "공법이 In Work 상태여서 영문 작업표준서를 편집 할 수 없습니다.", "INFORMATION", MessageBox.INFORMATION);
            }
        }
    }

    private void setTableData(Table table, List<HashMap<String, Object>> tableDataList) {
        TableColumn[] columns = table.getColumns();

        if(table.getItemCount() > 0) {
            table.removeAll();
        }

        for (int i = 0; i < tableDataList.size(); i++) {
            HashMap<String, Object> dataMap = tableDataList.get(i);
            String[] dataArr = new String[columns.length];
            for (int j = 0; j < columns.length; j++) {
                Object dataObj = dataMap.get(String.valueOf(j));
                if (dataObj != null) {
                    dataArr[j] = dataObj.toString();
                }
            }
            TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(dataArr);
        }
    }
    
    private void setTableDataWithImage(Table table, List<HashMap<String, Object>> tableDataList) {
        TableColumn[] columns = table.getColumns();

        if(table.getItemCount() > 0) {
            table.removeAll();
        }
     
        for (int i = 0; i < tableDataList.size(); i++) {
            HashMap<String, Object> dataMap = tableDataList.get(i);
            String[] dataArr = new String[columns.length];
            TableItem tableItem = new TableItem(table, SWT.CENTER );
            for (int j = 0; j < columns.length; j++) {
                Object dataObj = dataMap.get(String.valueOf(j));
                if (dataObj != null) {
                	if(dataObj instanceof String) {
                		tableItem.setText(j, dataObj.toString());
                	} else if ( dataObj instanceof Character) {
                		tableItem.setText(j, dataObj.toString());
                	} else if( dataObj instanceof Integer) {
                		tableItem.setText(j, dataObj.toString());
                	} else if( dataObj instanceof Image) {
                		tableItem.setImage(j, (Image)dataObj);
                		
                	}
                	
                	if( j == 5 && dataMap.containsKey("6")) {
            			tableItem.setText(j, (String)dataMap.get("6"));
            		}
                	
                }
            }// for문 j
        } // for문 i
    }

    @Override
    protected void validateConfig(int configId) {
        if (configId != 0 && configId != 1) {
            throw new SDVRuntimeException("View[" + getId() + " not supported config Id :" + configId);
        }
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {
        this.localDataMap = dataMap;
        if(dataMap.containsKey("actionId")) {
            String actionId = dataMap.getStringValue("actionId");
            List<HashMap<String, Object>> operation = localDataMap.getTableValue("targetOperationList");
            if("Publish".equals(actionId)) {
                Date date = (Date) operation.get(0).get(SDVPropertyConstant.ITEM_CREATION_DATE);
                String owningUserName = null;
            	if(operation.get(0)!=null && operation.get(0) instanceof HashMap){
            		TCComponentBOMLine bomLine = (TCComponentBOMLine)operation.get(0).get("OPERATION_BOPLINE");
            		try {
            			date = bomLine.getItem().getLatestItemRevision().getDateProperty("creation_date");
            			TCComponentUser owningUser = (TCComponentUser)bomLine.getItem().getReferenceProperty("owning_user");
            			owningUserName = owningUser.getProperty("user_name");
					} catch (TCException e) {
						e.printStackTrace();
					}
            	}else{
            		Exception aException = new Exception("Data error. Please contact your administrator");
            		aException.printStackTrace();
            	}
                txtDate.setText(SDVStringUtiles.dateToString(date, "yyyy-MM-dd HH:mm:ss"));
                txtDraw.setText(owningUserName);
            } else if("UpdateOperationName".equals(actionId)) {
                try {
                    targetOpLine.getItemRevision().refresh();
                    String engName = targetOpLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
                    if(engName != null) {
                        txtOpName.setText(engName);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else if("UpdateActivity".equals(actionId)) {
                try {
                    TCComponent root = targetOpLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
                    if(root != null) {
                        TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                        TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                        if(children != null) {
                            for(int i = 0; i < children.length; i++) {
                                children[i].refresh();
                                tblActivity.getItem(i).setText(3, children[i].getProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME));
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public IDataMap getLocalDataMap() {
        localDataMap.put("viewId", getId(), IData.STRING_FIELD);

        return localDataMap;
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

    	/**
    	 * 
    	 */
    	
        return new AbstractSDVInitOperation() {

            @SuppressWarnings("unchecked")
            public void initData() {
                Map<String, Object> parameters = getParameters();

                TCComponentBOPLine bopLine = null;
                TCComponentBOPLine operationLine = null;
                AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();

                try {
                    if(application instanceof MFGLegacyApplication) {
                        TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();
                        bopLine = (TCComponentBOPLine) bopWindow.getTopBOMLine();
                        if(bopLine != null) {
                            processType = bopLine.getItemRevision().getTCProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE).getStringValue();
                            localDataMap.put("process_type", processType, IData.STRING_FIELD);
                        }
                    }

                    if(parameters.containsKey("targetOperaion")) {
                        HashMap<String, Object> paramMap = (HashMap<String, Object>) parameters.get("targetOperaion");
                        if(paramMap != null) {
                            operationLine = (TCComponentBOPLine) paramMap.get("OPERATION_BOPLINE");
                        }
                    } else {
                        operationLine = (TCComponentBOPLine) AIFUtility.getCurrentApplication().getTargetComponent();
                    }

                    //[SR141226-017][20150205]shcho, 작업표준서 Preview 생성 시 BOM Line Reflesh 후 생성
                    operationLine.refresh();
                    targetOpLine = operationLine;
                    if (operationLine != null) {
                        // 대상 Operation을 localDataMap에 담는다.
                        List<TCComponentBOPLine> operationList = new ArrayList<TCComponentBOPLine>();
                        operationList.add(operationLine);
                        TCComponentItemRevision operationRev = operationLine.getItemRevision();

                        List<HashMap<String, Object>> targetOperationList = new ArrayList<HashMap<String, Object>>();
                        HashMap<String, Object> operationMap = new HashMap<String, Object>();
                        String[] propertyNames = new String[] {
                                SDVPropertyConstant.ITEM_ITEM_ID,
                                SDVPropertyConstant.ITEM_REVISION_ID,
                                SDVPropertyConstant.ITEM_OBJECT_NAME,
                                SDVPropertyConstant.OPERATION_REV_ENG_NAME
                        };

                        String[] values = operationRev.getProperties(propertyNames);
                        operationMap.put(SDVPropertyConstant.ITEM_ITEM_ID, values[0]);
                        operationMap.put(SDVPropertyConstant.ITEM_REVISION_ID, values[1]);
                        operationMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, values[2]);
                        operationMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, values[3]);
                        operationMap.put("UID", operationRev.getUid());
                        TCComponent mecoRev = operationRev.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
                        operationMap.put("MECO_OBJECT", mecoRev);
                        operationMap.put(SDVPropertyConstant.OPERATION_REV_MECO_NO, mecoRev.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                        operationMap.put("OPERATION_BOPLINE", operationLine);
                        targetOperationList.add(operationMap);
                        localDataMap.put("targetOperationList", targetOperationList, IData.TABLE_FIELD);

                        // 국문일 경우에는 Preview 전에 MECO EPL Load를 수행한다.
                        ProcessSheetDataHelper dataHelper = new ProcessSheetDataHelper(processType, getConfigId());
                        
                        //[NON-SR][20160817] taeku.jeong Preview 화면이 생성될때 MECO EPL 생성이나 Update가 필요한 경우
                        //                                               자동으로 생성/Update되는 기능이 추가되어 불필요한 부분으로 Remark 함.
                        //-----------------------------------------------------------------------------------------------------------------------------------(S)
                    	//// [NON-SR][20151118] taeku.jeong 일정조정협의때 나온 Issue로 불필요한 EPL Reload메시지 제거함.
                        //if(getConfigId() == 0) {
                        //    if(dataHelper.checkMeplLoad(operationLine) == true) {
                        //        //MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("RequiredEPL.Message"), "Infomation", MessageBox.INFORMATION);
                        //    	System.out.println("Information!! : "+registry.getString("RequiredEPL.Message"));
                        //    }
                        //}
                        //-----------------------------------------------------------------------------------------------------------------------------------(E)
                        
                        showProgress(true);

                        IDataMap operationInfoMap = dataHelper.getOperationInfo(operationLine); // 시간 좀 걸림 
                        setOperationInfoData(operationInfoMap);
                        setHeaderInfoData(dataHelper.getHeaderInfo(operationLine)); // 시간좀 걸림
                        IDataMap mecoMap = dataHelper.getMECOList(operationLine);
                        setMECOData(mecoMap);
                        IDataMap resourceDataMap = dataHelper.getResourceList(operationLine);
                        // 원자재 List에 표시될 Tool, Jig, Robot, Gun, GeneralEquipment, PlantOPArea, PlantStaiton 등의 List
                        setResourceData(resourceDataMap);
                        // Activity List
                        setActivityData(dataHelper.getActivityList(operationLine), resourceDataMap, operationInfoMap); // 시간 엄청 걸림
                        // 원자재 List에 표시된 End Iten에 해당하는 Vehical Part, Standard Part 등을 List
                        // [NON_SR][20151002] taeku.jeong 이종화 차장님의 확인 요청으로 인해 End Item MECO List를
                        //                                                        읽어와서 변경기호를 표기해주는 부분의 Query를 수정함.
                        IDataMap endItemDataMap = dataHelper.getEndItemList(operationLine); // 시간 엄청 오래 걸림 
                        setEndItemData(endItemDataMap);
                        // 부자재 List 해 List 될 Data
                        setSubsidiaryData(dataHelper.getSubsidiaryList(operationLine)); // 시간 엄청 오래 걸림
                        
                        boolean firstCreateEng = false;

                        if(getConfigId() == 0) {
                            tcDataset = (TCComponentDataset) operationRev.getRelatedComponent(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
                        } else {
                            tcDataset = (TCComponentDataset) operationRev.getRelatedComponent(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
                        }

                        if(tcDataset == null) {
                            if(getConfigId() == 0) {
                                tcDataset = SDVBOPUtilities.getTemplateDataset("M7_TEM_DocItemID_ProcessSheet_Kor", values[0] + "/" + values[1], values[0]);
                                operationRev.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, tcDataset);
                           
                            } else {
                                tcDataset = ProcessSheetUtils.translateProcessSheet(operationRev, ProcessSheetUtils.getProcessType(operationRev));
                                operationRev.add(SDVTypeConstant.PROCESS_SHEET_EN_RELATION, tcDataset);
                                firstCreateEng = true;
                            }
                        }
                        
                        /**
                         * 	이종화 차장님 요청 
                         *  작업표준서의 양식을 바꿔야 하는데 특정 Cell 1개만 바뀐거라 기존 템플릿을 이용해서 Cell 변환시도
                         *  
                         */
                        
                        TCComponentDataset oldSheetDataset = null;
                        
                        if(getConfigId() == 0) {
                        	oldSheetDataset = (TCComponentDataset) operationRev.getRelatedComponent(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
                        } else {
                        	oldSheetDataset = (TCComponentDataset) operationRev.getRelatedComponent(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
                        }

                        
                        replaceNewWorkSheet(oldSheetDataset, firstCreateEng);
                        

                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "Preview", MessageBox.ERROR);
                } finally {
                    showProgress(false);
                }
            }

            @Override
            public void executeOperation() throws Exception {
                final DataSet dataset = new DataSet();
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        initData();
                        
                        /* [SR150714-008][20150713] shcho, Excel Open이 되지 않는 오류 수정
                         * Preview Open 완료 후 ExcelView의 initalizeLocalData에서 Excel Template 파일 설정을 하게 되는데, 
                         * 사용자가 Preview Open 직후 (initalizeLocalData호출 전에) Excel Open 버튼을 클릭하게 되면 파일을 찾을 수 없어 오류가 발생
                         * Excel Template 파일 설정을 Preview에서 하도록 변경
                         */
                        IViewPane excelView = getView("excelView");
                        ((ExcelView) excelView).setTargetDataset(tcDataset);
                        ((ExcelView) excelView).enabledButton();
                        if(getConfigId() == 1)
                        {
                			Map<String, Object> parameters = UIManager.getCurrentDialog().getParameters();
                			if (parameters != null && parameters.containsKey("targetOperaion"))
                			{
                				HashMap<String, Object> paramMap = (HashMap<String, Object>) parameters.get("targetOperaion");
                				if (paramMap != null)
                				{
                					isEnableEngPreviewButton = (Boolean) paramMap.get("IS_ENABLE_ENG_PREVIEW_BUTTON");
                				}
                			}
                        	((ExcelView) excelView).setPreviewWritable(isEnableEngPreviewButton);
                        }
                    }
                });

                setData(dataset);
            }
        };

    }

    public void openProcessSheet() {
        List<HashMap<String, Object>> operationList = localDataMap.getTableValue("targetOperationList");
        if(operationList != null && operationList.size() > 0) {
            HashMap<String, Object> operationMap = operationList.get(0);
            String id = (String) operationMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
            String rev = (String) operationMap.get(SDVPropertyConstant.ITEM_REVISION_ID);

            try {
                File file = ProcessSheetUtils.getLatestPublishProcessSheet(0, id, rev);
                if(file == null) {
                    throw new Exception(registry.getString("NOExistPublishedProcessSheet.Message"));
                }

                ProcessSheetUtils.openExcleFile(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), getId(), MessageBox.INFORMATION);
            }
        }
    }

    public void openExcelFile(File file) {
    	
    	System.out.println("Befor Invoke.1...");
    	
        OleControlSite appControlSite = new OleControlSite(new OleFrame(this, SWT.NONE), SWT.NONE, "Excel.Application");
        appControlSite.doVerb(OLE.OLEIVERB_OPEN);
        OleListener windowDeactivateListener = new OleListener() {

            @Override
            public void handleEvent(OleEvent event) {
                enableOpenButtons((AbstractSDVSWTDialog) UIManager.getCurrentDialog(), true);
            }
        };
        OleAutomation application = new OleAutomation(appControlSite);
        appControlSite.addEventListener(application, ExcelView.IID_AppEvents, ExcelView.WindowDeactivate, windowDeactivateListener);

        application.setProperty(application.getIDsOfNames(new String[] {"Visible"})[0], new Variant(true));
        OleAutomation workbooks = application.getProperty(application.getIDsOfNames(new String[] {"Workbooks"})[0]).getAutomation();
        Variant varResult = workbooks.invoke(workbooks.getIDsOfNames(new String[] {"Open"})[0], new Variant[] {new Variant(file.getAbsolutePath())});
        
        System.out.println("After Invoke.1...");
        
        if(varResult != null) {
            System.out.println(" copy invoke result of BSHEET = " + varResult);
            varResult.dispose();

            // Open 버튼 Disable
            enableOpenButtons((AbstractSDVSWTDialog) UIManager.getCurrentDialog(), false);
        } else {
            System.out.println("=====failed invoke copySheet method ====");
        }

        System.out.println("After Invoke.2...");
        
        workbooks.dispose();
        application.dispose();
    }

    private void enableOpenButtons(AbstractSDVSWTDialog dialog, boolean flag) {
        LinkedHashMap<String, IButtonInfo> actionButtons = dialog.getCommandToolButtons();
        for(String key : actionButtons.keySet()) {
            if(actionButtons.get(key).getActionId().equals("Open KO")) {
                actionButtons.get(key).getButton().setEnabled(flag);
            }
        }
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

        try {
            if(!checkPublishAuth()) {
                IDialog dialog = UIManager.getCurrentDialog();
                if(dialog != null && dialog instanceof AbstractSDVSWTDialog) {
                    AbstractSDVSWTDialog swtDialog = (AbstractSDVSWTDialog) dialog;
                    LinkedHashMap<String, IButtonInfo> buttons = swtDialog.getCommandToolButtons();
                    for(String key : buttons.keySet()) {
                        IButtonInfo button = buttons.get(key);
                        if(button.getButtonId().length() > 1) {
                            String actionId = button.getActionId();
                            if(getConfigId() == 0) {
                                if("A".equals(processType)) {
                                    if("Load MEPL".equals(actionId)) {
                                        button.getButton().setEnabled(false);
                                    }
                                } else {
                                    if(!"Download".equals(actionId)) {
                                        button.getButton().setEnabled(false);
                                    }
                                }
                            } else {
                                if(!"Open KO".equals(actionId)) {
                                    button.getButton().setEnabled(false);
                                }
                            }
                        }
                    }
                }
            }
        } catch (TCException e) {
            e.printStackTrace();

        }
    }

    public boolean checkPublishAuth() throws TCException {
        if(getConfigId() == 0) {
            List<HashMap<String, Object>> targetOperationList = localDataMap.getTableValue("targetOperationList");
            if(targetOperationList != null && targetOperationList.size() > 0) {
                HashMap<String, Object> operation = targetOperationList.get(0);
                Object mecoRev = operation.get("MECO_OBJECT");
                if(mecoRev != null) {
                    String loginUserId = ((TCSession) AIFUtility.getDefaultSession()).getUser().getUserId();
                    TCComponentUser user = (TCComponentUser) ((TCComponentItemRevision) mecoRev).getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
                    if(loginUserId.equals(user.getUserId())) {
                        return true;
                    }
                }
            }
        } else {
//            AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
//            if(application instanceof MFGLegacyApplication) {
//                TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();
//                TCComponentRevisionRule revisionRuleComp = bopWindow.getRevisionRule();
//
//                if(revisionRuleComp != null) {
//                    String revisionRule = revisionRuleComp.toString();
//
//                    TCPreferenceService prefService = ((TCSession) AIFUtility.getDefaultSession()).getPreferenceService();
////                    String[] prefValues = prefService.getStringArray(TCPreferenceService.TC_preference_site, "M7_ProcessSheet_EN_EditableRevisionRule");
//                    String[] prefValues = prefService.getStringValuesAtLocation("M7_ProcessSheet_EN_EditableRevisionRule", TCPreferenceLocation.OVERLAY_LOCATION);
//                    if(prefValues != null) {
//                        for(String value : prefValues) {
//                            if(value.equals(revisionRule)) {
//                                return true;
//                            }
//                        }
//                    }
//                }
//            }
        	if(isEnableEngPreviewButton)
        		return isEnableEngPreviewButton;
        }
        
        
        // 2015-10-06 taeku.jeong Republish 강제로 가능하도록 수정
        boolean isForcedRepublish = false;
    	String currentUserName = AIFUtility.getCurrentApplication().getSession().getUserName();
    	if(currentUserName!=null && currentUserName.indexOf("infodba")>=0){
    		if(((TCSession)AIFUtility.getCurrentApplication().getSession()).hasBypass()==true){
    			isForcedRepublish = true;
    		}
    	}
    	
    	if(isForcedRepublish==true){
    		return true;
    	}

    	return false;        
    }

    @Override
    public void uiLoadCompleted() {
    	
    }

    /**
     * MEPL Data를 재생성 하는 Function
     * [NON-SR][20160816] Taeku.Jeong MECO EPL Load를 Operation 단위로 수행 하도록 변경과정에
     * 더이상 필요없어진 Function으로 Remark 처리함
     */
//    public void mecoEplReload() {
//        showProgress(true);
//
//        CustomUtil customUtil = new CustomUtil();
//
//        try {
//            TCComponentItemRevision operationRevision = targetOpLine.getItemRevision();
//            TCComponentItemRevision opMecoItemRevision = (TCComponentItemRevision) operationRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
//
//            // 공법과 공정의 MECO가 다르면 어차피 작업표준서 MECO 리스트에 공정 MECO는 표시되지 않으므로 공정 MECO를 로드하는 의미가 없다...
//            // 그러므로 ProcessType에 관계없이 공법 MECO만 로드
//            // MEPL 생성 (MECO_EPL Table에 Data 생성)
//            customUtil.buildMEPL((TCComponentChangeItemRevision) opMecoItemRevision, true);
//
//            ProcessSheetDataHelper dataHelper = new ProcessSheetDataHelper(processType, getConfigId());
//            IDataMap mecoMap = dataHelper.getMECOList(targetOpLine);
//            setMECOData(mecoMap);
//            IDataMap resourceDataMap = dataHelper.getResourceList(targetOpLine);
//            setResourceData(resourceDataMap);
//            setEndItemData(dataHelper.getEndItemList(targetOpLine));
//            setSubsidiaryData(dataHelper.getSubsidiaryList(targetOpLine));
//
//            showProgress(false);
//            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("OperationComplete.Message"), "EPL Reload", MessageBox.INFORMATION);
//        } catch(Exception e) {
//            e.printStackTrace();
//            showProgress(false);
//            MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "EPL Reload", MessageBox.ERROR);
//        }
//    }

	protected void showProgress(boolean show) {
		if(this.isShowProgress != show) {
			if(show) {
				if(progressShell == null) {
					try {
//						thisShell.setEnabled(false);
//						thisShell.update();
						progressShell = new ProgressBar(thisShell);
						progressShell.start();
						progressShell.setText("<html>Loading preview data ...<br>Please wait until completion.</html>");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} else if(progressShell != null) {
//				thisShell.setEnabled(true);
//				thisShell.update();
				progressShell.close();
				progressShell = null;
			}

			isShowProgress = show;
		}
	}

    /**
     * [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
     * @param dialog
     * @param buttonId
     * @param flag
     */
    private void setEabledButton(AbstractSDVSWTDialog dialog, String buttonId, boolean flag) {
        LinkedHashMap<String, IButtonInfo> actionButtons = dialog.getCommandToolButtons();
        for(String key : actionButtons.keySet()) {
            if(actionButtons.get(key).getActionId().equals(buttonId)) {
                actionButtons.get(key).getButton().setEnabled(flag);
            }
        }
    }
    
    
   /**
    * 특별특성 속성 추가로 인한 작업 표준서 템플릿 변경
    * @param oldDataSet
    */
    private void replaceNewWorkSheet(TCComponentDataset oldDataSet, boolean firstCreateEng) {
    	
    	try {
    		TCComponentTcFile[] tcFiles = oldDataSet.getTcFiles();
    		String fileName = tcFiles[0].getProperty("original_file_name");
    		File oldTemplate  = SDVBOPUtilities.getFiles(oldDataSet)[0];
    		FileInputStream oldFileInput = new FileInputStream(oldTemplate);
    		Workbook oldTemplateWorkBook = new XSSFWorkbook(oldFileInput);
    		
    		ArrayList<HashMap> sheetArray = new ArrayList<HashMap>();
    		int mergeIndex = 0;
    		int firstRow = 0;
		    int lastRow = 0;
		    int firstColumn = 0;
		    int lastColumn = 0;
    		
    		String containsSheetName = "";
    		String specialCharacteristicsName = "";
    		String oldToolName = "";
    		String oldToolNo = "";
    		String newToolName = "";
			String newToolNo = "";
    		if(getConfigId() == 0) {
    			containsSheetName = "갑";
    			specialCharacteristicsName = "특별 특성";
    			
    			oldToolName = "공구";
    			oldToolNo = "번호";
    			newToolName = "R/C,";
    			newToolNo = "공구";
    			
    		} else {
    			containsSheetName = "A";
    			specialCharacteristicsName = "S.C";
    			
    			oldToolName = "TOOL";
    			oldToolNo = "NO.";
    			
    			newToolName = "R/C,";
    			newToolNo = "TOOL";
    		}
    		
    		for( int i = 0; i < oldTemplateWorkBook.getNumberOfSheets(); i ++ ) {
    			
    			if( oldTemplateWorkBook.getSheetAt(i).getSheetName().startsWith(containsSheetName)) {
    				Sheet oldSheet = oldTemplateWorkBook.getSheetAt(i);
    				
    			// 영문일 경우 포함된 ASHEET 는 Skip
    			if( getConfigId() != 0 ) {
    				if( oldTemplateWorkBook.getSheetAt(i).getSheetName().equals("ASHEET")) {
    					continue;
    				}
    			}
    				
    			for( int j = 0; j < oldSheet.getNumMergedRegions(); j ++ ) {
    				 CellRangeAddress range = oldSheet.getMergedRegion(j);
    				 String message = "";
    				 Row row = oldSheet.getRow(range.getFirstRow());
    				 Cell cell = row.getCell(range.getFirstColumn());
    				 //[SR181005-008] Cell 타입이 다른 것에서 데이터 추출 에러 타입 명시 
    				 if( cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
    					 message = cell.getStringCellValue();
    				 }
    				 if( message.startsWith("KPC/")) {
    					 mergeIndex = j;
    					 firstRow = range.getFirstRow();
    					 lastRow = range.getLastRow();
    					 firstColumn = range.getFirstColumn();
    					 lastColumn = range.getLastColumn();
    					 HashMap <String, String> sheetHash = new HashMap<String, String>();
    					 sheetHash.put(oldSheet.getSheetName() + "_" + j, firstRow + ":"+ lastRow + ":" + firstColumn+ ":" + lastColumn );
    					 sheetArray.add(sheetHash);
    					 
    				 }  
    				 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    				 // 작업표준서 Template 변경으로 인해 Preview 실행시 구버전 -> 신버전으로 변경 
    				  if( message.equals(oldToolName) && range.getFirstRow() ==4 ) {
    					 mergeIndex = j;
    					 firstRow = range.getFirstRow();
    					 lastRow = range.getLastRow();
    					 firstColumn = range.getFirstColumn();
    					 lastColumn = range.getLastColumn();
    					 if( firstColumn == 99 && lastColumn == 103) {
    						 HashMap <String, String> sheetHash = new HashMap<String, String>();
    						 sheetHash.put(oldSheet.getSheetName() + "_" + j, firstRow + ":"+ lastRow + ":" + firstColumn+ ":" + lastColumn );
    						 sheetArray.add(sheetHash);
    					 }
    					 
    				 } else if(message.equals(oldToolNo) && range.getFirstRow() == 5){
    					 mergeIndex = j;
    					 firstRow = range.getFirstRow();
    					 lastRow = range.getLastRow();
    					 firstColumn = range.getFirstColumn();
    					 lastColumn = range.getLastColumn();
    					 if( firstColumn == 99 && lastColumn == 103) {
    						 HashMap <String, String> sheetHash = new HashMap<String, String>();
    						 sheetHash.put(oldSheet.getSheetName() + "_" + j, firstRow + ":"+ lastRow + ":" + firstColumn+ ":" + lastColumn );
    						 sheetArray.add(sheetHash);
    					 }
    				 }
    				 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    			}
    		    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    		     
    		  } // if문 end
    			
    		} // 외부 for문 end
    		
    		if( sheetArray.size() == 0) 
    		{
    			return;
    			
    		} else {
    			
    			
    			CellStyle leftCellStyle = oldTemplateWorkBook.createCellStyle();  // 셀 스타일 생성
	    		Font font = oldTemplateWorkBook.createFont(); // 폰트 스타일 생성
	    		
	    		
	    		font.setFontHeightInPoints((short)10);
	    		font.setBoldweight(Font.BOLDWEIGHT_BOLD); // 폰트 두께 굵게
	    		leftCellStyle.setFont(font);  // 폰트 셀 스타일에 적용
	    		leftCellStyle.setWrapText(true); // 셀 크기에 맞게 텍스트 적용
	    		leftCellStyle.setAlignment(CellStyle.ALIGN_CENTER); // 셀 정렬
	    		leftCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // 셀 수직 정렬
	    		leftCellStyle.setBorderRight(CellStyle.BORDER_THIN); // 셀 테두리(오른쪽) 가는굵기
	    		leftCellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM); // 셀 테두리 (왼쪽) 중간 굵기
	    		
	    		
	    		if(getConfigId() == 0) {
	    			font.setFontName("돋음"); // 폰트 글꼴
	    			font.setFontHeightInPoints((short)11);
	    			
	    		} else {
	    			font.setFontName("Tahoma"); // 폰트 글꼴
	    			font.setFontHeightInPoints((short)12);
	    		}
	    		
	    		CellStyle rightCellStyle = oldTemplateWorkBook.createCellStyle();  // 셀 스타일 생성
	    		Font font_rightCell = oldTemplateWorkBook.createFont();
	    		font_rightCell.setFontName("HY견고딕");
	    		font_rightCell.setFontHeightInPoints((short)16);
	    		font_rightCell.setBoldweight(Font.BOLDWEIGHT_BOLD);
	    		font_rightCell.setColor(HSSFColor.BLUE.index);
	    		rightCellStyle.setFont(font_rightCell);
	    		rightCellStyle.setBorderLeft(CellStyle.BORDER_THIN); // 셀 테두리 왼쪽
	    		rightCellStyle.setAlignment(CellStyle.ALIGN_CENTER); // 셀 정렬
	    		rightCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // 셀 수직 정렬
    			
    			
    			
    			// 영문 Preview 를 처음 실행 했을때 처리 부분
    			// 영문 Preview 를 처음 실행 했을때 생성된 Preview 작업 표준서는 영문 작업 표준서 템플릿을 가져와 국문 작업표준서에 Cell 내용을 복사 하여 생성 하므로
    			// 이미 템플릿은 국문처럼 변경되어 있음
    			if(firstCreateEng) {
    				
//		    			for( int i = 0; i < sheetArray.size(); i ++ ) {
		    			for( int i = 0; i < oldTemplateWorkBook.getNumberOfSheets(); i ++ ) {
		    				Sheet oldSheet = oldTemplateWorkBook.getSheetAt(i);
		    				if( !oldSheet.getSheetName().startsWith(containsSheetName) ) {
		    					continue;
		    				}
		    			for( int j = 0; j < sheetArray.size(); j ++ ) {
		    				HashMap sheetHashMap = sheetArray.get(j);
		    				Iterator iterator = sheetHashMap.keySet().iterator();
		    			   while(iterator.hasNext()) {
		    			    String key = (String)iterator.next();
		    				String[] keySplit = key.split("_");
		    				if( keySplit[0].equals(oldSheet.getSheetName())) {
		    					String placeInform = (String)sheetHashMap.get(key);
		    					String[] placeInformSplit = placeInform.split(":"); //[0] : firstRow, [1] : lastRow, [2] : firstColumn, [3] : lastColumn
		    		     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		    					
			    		     Row row = oldSheet.getRow(Integer.parseInt(placeInformSplit[0])); // 4라인
			    		     Cell leftCell = row.getCell(Integer.parseInt(placeInformSplit[2]));  // AQ 컬럼  AQ ~ AV 까지 셀병합 되어 있음
			    		     
			    		     if( null == leftCell) 
			    		     {
			    		    	 leftCell =  row.createCell(Integer.parseInt(placeInformSplit[2]));
			    		    	
			    		     }
			    		     
			    		   //시트 보호 해제
		       				  CTSheetProtection sheetProtection = ((XSSFSheet)oldSheet).getCTWorksheet().getSheetProtection();
		       		         if (sheetProtection != null) {
		       		       	  ((XSSFSheet)oldSheet).getCTWorksheet().unsetSheetProtection();
		       		         }
		       		     
		       		         CTWorkbookProtection workbookProtection =  ((XSSFWorkbook)oldSheet.getWorkbook()).getCTWorkbook().getWorkbookProtection();
			       		     if (workbookProtection != null) {
			       		   	  		((XSSFWorkbook)oldSheet.getWorkbook()).getCTWorkbook().unsetWorkbookProtection();
			       		     }
			    		     
			    		     if( Integer.parseInt(placeInformSplit[2]) == 42 && Integer.parseInt(placeInformSplit[3]) == 47) {
	    						 
	    		        		 oldSheet.removeMergedRegion(Integer.parseInt(keySplit[1])); // 셀 병합 취소  위치 27
	    		        		 
	    		        		 oldSheet.addMergedRegion(new CellRangeAddress(Integer.parseInt(placeInformSplit[0]), Integer.parseInt(placeInformSplit[0]), Integer.parseInt(placeInformSplit[2]), Integer.parseInt(placeInformSplit[2]) + 2)); // 셀병합  
	    		        		 oldSheet.addMergedRegion(new CellRangeAddress(Integer.parseInt(placeInformSplit[0]), Integer.parseInt(placeInformSplit[0]), Integer.parseInt(placeInformSplit[2]) + 3, Integer.parseInt(placeInformSplit[3])));
	    		        		 oldSheet.getRow(Integer.parseInt(placeInformSplit[0])).getCell(Integer.parseInt(placeInformSplit[2])).setCellStyle(leftCellStyle);
	    		        		 oldSheet.getRow(Integer.parseInt(placeInformSplit[0])).getCell(Integer.parseInt(placeInformSplit[2])).setCellValue(specialCharacteristicsName);
	    		        		 oldSheet.getRow(Integer.parseInt(placeInformSplit[0])).getCell(Integer.parseInt(placeInformSplit[2]) + 3).setCellStyle(rightCellStyle);
	    		        		 
	    		        		
			    		    	 
			    		     } else if(  Integer.parseInt(placeInformSplit[2]) == 99 &&  Integer.parseInt(placeInformSplit[3]) == 103) {
			    		    	 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    		        		 // 작업표준서 Template 변경으로 인해 Preview 실행시 구버전 -> 신버전으로 변경 
	    		        		 String toolName = oldSheet.getRow(4).getCell(Integer.parseInt(placeInformSplit[2])).getStringCellValue();
	    		        		 String toolNo = oldSheet.getRow(5).getCell(Integer.parseInt(placeInformSplit[2])).getStringCellValue();
	    		        		 
	    		        		 if( !toolName.equals(newToolName) && !toolNo.equals(newToolNo)) {
	    		        			 oldSheet.getRow(4).getCell(Integer.parseInt(placeInformSplit[2])).setCellValue(newToolName);
	    		        			 oldSheet.getRow(5).getCell(Integer.parseInt(placeInformSplit[2])).setCellValue(newToolNo);
	    		        			 oldSheet.getRow(4).getCell(99).setCellStyle(leftCellStyle);
	    		        			 oldSheet.getRow(5).getCell(99).setCellStyle(leftCellStyle);
	    		        		 }
	    		        		 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			    		     }
			    		     
			    		     else {
	    						 
	    						 leftCell.setCellValue(specialCharacteristicsName);
	    						 
	    						 Cell rightCell = row.getCell(Integer.parseInt(placeInformSplit[2]) + 3); // 우측 셀 가져오기 셀이 병합 되었어도 셀의 위치 정보(No.)는 변하지 않음
	    						 
	    						 if( null == rightCell ) {
	    							 rightCell = row.createCell(Integer.parseInt(placeInformSplit[2]) + 3);
	    						 }
	    						 
	    						 leftCell.setCellStyle(leftCellStyle);  // Cell 병합 했어도  Cell 의 번호는 바뀌지 않음 
	    						 
	    						 rightCell.setCellStyle(rightCellStyle);
	    					 }
			    		     
			    		  // 시트 보호 
	    	    		     	STUnsignedShortHex convertedPassword = AISInstructionDatasetCopyUtil.stringToExcelPassword(oldTemplateWorkBook, "symc");
	    	    		     	CTSheetProtection sheetProtection1 = ((XSSFSheet)oldSheet).getCTWorksheet().getSheetProtection();
	    	    			    if (sheetProtection1 == null) {
	    	    	                sheetProtection1 = ((XSSFSheet)oldSheet).getCTWorksheet().addNewSheetProtection();
	    	    	            }
	    	    			    sheetProtection1.xsetPassword(convertedPassword);
	    	    	            sheetProtection1.setSheet(true);
	    	    	            sheetProtection1.setScenarios(true);
	    	    	            sheetProtection1.setObjects(false);
	    	    	            CTWorkbookProtection workbookProtection1 =  ((XSSFWorkbook)oldSheet.getWorkbook()).getCTWorkbook().getWorkbookProtection();
	    	    	            if (workbookProtection1 == null) {
	    	    	                workbookProtection1 = ((XSSFWorkbook) oldTemplateWorkBook).getCTWorkbook().addNewWorkbookProtection();
	    	    	                workbookProtection1.setLockStructure(true);
	    	    	                workbookProtection1.setLockWindows(true);
	    	    	            }
	    	    	            workbookProtection1.xsetWorkbookPassword(convertedPassword);
			    		     
		    			} // if 문 end
		    		  } // while 문 end
		    		 }
		    		} // for 문 end
		    		
    				
    			} else {
    				
    			
    				for( int i = 0; i < oldTemplateWorkBook.getNumberOfSheets(); i ++ ) {

    					Sheet oldSheet = oldTemplateWorkBook.getSheetAt(i);
	    				if( !oldSheet.getSheetName().startsWith(containsSheetName) ) {
	    					continue;
	    				}
		    		for( int j = 0; j < sheetArray.size(); j ++ ) {
	    				HashMap sheetHashMap = sheetArray.get(j);
	    				Iterator iterator = sheetHashMap.keySet().iterator();
	    			   while(iterator.hasNext()) {
	    				   String key = (String)iterator.next();
	    			  
	    				String[] keySplit = key.split("_");
	    				if( keySplit[0].equals(oldSheet.getSheetName())) {
	    					String placeInform = (String)sheetHashMap.get(key);
	    					String[] placeInformSplit = placeInform.split(":"); //[0] : firstRow, [1] : lastRow, [2] : firstColumn, [3] : lastColumn
		    			
//		    				Sheet oldSheet = sheetArray.get(i);
		    		        System.out.println("Sheet Name :====================>"  + oldSheet.getSheetName());
		    		     
		    		     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		    		        	//시트 보호 해제
			       				  CTSheetProtection sheetProtection = ((XSSFSheet)oldSheet).getCTWorksheet().getSheetProtection();
			       		         if (sheetProtection != null) {
			       		       	  ((XSSFSheet)oldSheet).getCTWorksheet().unsetSheetProtection();
			       		         }
			       		     
			       		         CTWorkbookProtection workbookProtection =  ((XSSFWorkbook)oldSheet.getWorkbook()).getCTWorkbook().getWorkbookProtection();
				       		     if (workbookProtection != null) {
				       		   	  		((XSSFWorkbook)oldSheet.getWorkbook()).getCTWorkbook().unsetWorkbookProtection();
				       		     }
				       		     
				       		     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				       		     // 작업표준서 Template 변경으로 인해 Preview 실행시 구버전 -> 신버전으로 변경 
				       		  if( Integer.parseInt(placeInformSplit[2]) == 99 && Integer.parseInt(placeInformSplit[3]) == 103) {
		    		        		 String toolName = oldSheet.getRow(4).getCell(99).getStringCellValue();
		    		        		 String toolNo = oldSheet.getRow(5).getCell(99).getStringCellValue();
		    		        		 
		    		        		 if( !toolName.equals(newToolName) && !toolNo.equals(newToolNo)) {
		    		        			 oldSheet.getRow(4).getCell(99).setCellValue(newToolName);
		    		        			 oldSheet.getRow(5).getCell(99).setCellValue(newToolNo);
		    		        			 oldSheet.getRow(4).getCell(99).setCellStyle(leftCellStyle);
		    		        			 oldSheet.getRow(5).getCell(99).setCellStyle(leftCellStyle);
		    		        		 }
				       		  } 
				       		  
				       		  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				       		  else {
	    		        		 oldSheet.removeMergedRegion(Integer.parseInt(keySplit[1])); // 셀 병합 취소  위치 27
	    		        		 
	    		        		 oldSheet.addMergedRegion(new CellRangeAddress(Integer.parseInt(placeInformSplit[0]), Integer.parseInt(placeInformSplit[0]), Integer.parseInt(placeInformSplit[2]), Integer.parseInt(placeInformSplit[2]) + 2)); // 셀병합  
	    		        		 oldSheet.addMergedRegion(new CellRangeAddress(Integer.parseInt(placeInformSplit[0]), Integer.parseInt(placeInformSplit[0]), Integer.parseInt(placeInformSplit[2]) + 3, Integer.parseInt(placeInformSplit[3])));
	    		        		 oldSheet.getRow(Integer.parseInt(placeInformSplit[0])).getCell(Integer.parseInt(placeInformSplit[2])).setCellStyle(leftCellStyle);
	    		        		 oldSheet.getRow(Integer.parseInt(placeInformSplit[0])).getCell(Integer.parseInt(placeInformSplit[2])).setCellValue(specialCharacteristicsName);
	    		        		 oldSheet.getRow(Integer.parseInt(placeInformSplit[0])).getCell(Integer.parseInt(placeInformSplit[2]) + 3).setCellStyle(rightCellStyle);
				       		  }
	    		        		
		    		        	 
		    		    		     
		    	    		     	// 시트 보호 
		    	    		     	STUnsignedShortHex convertedPassword = AISInstructionDatasetCopyUtil.stringToExcelPassword(oldTemplateWorkBook, "symc");
		    	    		     	CTSheetProtection sheetProtection1 = ((XSSFSheet)oldSheet).getCTWorksheet().getSheetProtection();
		    	    			    if (sheetProtection1 == null) {
		    	    	                sheetProtection1 = ((XSSFSheet)oldSheet).getCTWorksheet().addNewSheetProtection();
		    	    	            }
		    	    			    sheetProtection1.xsetPassword(convertedPassword);
		    	    	            sheetProtection1.setSheet(true);
		    	    	            sheetProtection1.setScenarios(true);
		    	    	            sheetProtection1.setObjects(false);
		    	    	            CTWorkbookProtection workbookProtection1 =  ((XSSFWorkbook)oldSheet.getWorkbook()).getCTWorkbook().getWorkbookProtection();
		    	    	            if (workbookProtection1 == null) {
		    	    	                workbookProtection1 = ((XSSFWorkbook) oldTemplateWorkBook).getCTWorkbook().addNewWorkbookProtection();
		    	    	                workbookProtection1.setLockStructure(true);
		    	    	                workbookProtection1.setLockWindows(true);
		    	    	            }
		    	    	            workbookProtection1.xsetWorkbookPassword(convertedPassword);
		    				}  // else 문 End
		    		     ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		    		   }
    				  }
    				}
    			}// else End
    			FileOutputStream fos = new FileOutputStream(oldTemplate);
    			oldTemplateWorkBook.write(fos);
    			fos.flush();
    			fos.close();
    			
    			SDVBOPUtilities.datasetUpdate(oldTemplate, oldDataSet);
    	}  
    		
    		
    	} catch ( Exception e ) {
    		e.printStackTrace();
    	}
    }
    
    
    private Map<String, TCComponentItemRevision> getReplacedEndItems(TCComponentBOPLine operationLine, List<EndItemData> targetEndItems) throws Exception {
        Map<String, TCComponentItemRevision> revisions = new HashMap<String, TCComponentItemRevision>();
        AIFComponentContext[] contexts = operationLine.getChildren();
        if(contexts != null) {
            for(EndItemData endItem : targetEndItems) {
                String occPuid = endItem.getOcc_puid();
                for(AIFComponentContext context : contexts) {
                    TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
                    if(occPuid.equals(bopLine.getProperty(SDVPropertyConstant.BL_OCC_FND_OBJECT_ID))) {
                        revisions.put(endItem.getCitem_id(), findEndItemInMProduct(bopLine));
                    }
                }               
            }
        }
        return revisions;
    }
    
    private TCComponentItemRevision findEndItemInMProduct(TCComponentBOPLine bopLine) throws TCException, Exception {
        TCComponentItemRevision itemRevision = null;
        
        TCComponentBOMLine endItemBomline = SDVBOPUtilities.getAssignSrcBomLine(bopLine.window(), bopLine);
        if(endItemBomline != null) {
            itemRevision = endItemBomline.getItemRevision();
        }
        
        return itemRevision;
    }
    
	
}  // Class End
