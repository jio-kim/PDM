package com.kgm.common.bundlework.imp;

import static com.kgm.common.bundlework.BundleWorkDialog.STATUS_COMPLETED;
import static com.kgm.common.bundlework.BundleWorkDialog.STATUS_ERROR;
import static com.kgm.common.bundlework.BundleWorkDialog.STATUS_INPROGRESS;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import jxl.Cell;
import jxl.LabelCell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.poi.xssf.usermodel.XSSFSheet;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpTrim;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.commands.ospec.op.Option;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.bundlework.bwutil.BWItemModel;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.DateUtil;
import com.kgm.common.utils.TcDefinition;
import com.kgm.common.utils.TxtReportFactory;
import com.kgm.common.utils.variant.OptionManager;
import com.kgm.common.utils.variant.VariantErrorCheck;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCComponentVariantRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.services.PSEApplicationService;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.sosvi.SelectedOptionSetDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [NON-SR] [20150430] ymjang, IF_OSPEC_MASTER_FROM_HBOM 에 insert 시에 Variant Option 일부 누락 발생
 * [NON-SR] [20150609] [ymjang] Base Spec IF 용 데이터 생성을 위한 Map 생성
 * [SR150408-022] [20150529] [ymjang] Variant/Function EPL 수량표현 개선방안(양산 및 개발프로젝트 반영하여 출력할수 있도록)
 * [NON-SR] [20160825] [ymjang] 동일한 Vaiant 에 각 옵션별로 업로드시간이 조금씩 틀려서 HBOM에서 최신 O/Spec 정보를 읽을 때 문제 발행.
 * [20170314][ymjang] 암호화 문서 경고 문구 삽입
 */

/**
 * O-SPEC을 Excel로 Import하는 기능.
 * #### 유효성 검증 ######
 * 1. 현재의 Product BOM Line 하위에 Excel에 표기된 Variant BOM Line이 모두 존재 하는가?
 *     Y : continue          N : throw ERROR
 * 2. Excel의 Product에 정의된 옵션코드가 Corporate Option Item에 존재하는가?
 *     Y : continue          N : throw ERROR(옵션코드를 우선 정의 하시오).
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"unused", "rawtypes", "unchecked", "static-access"})
public class BWVariantOptionImpDialog extends BWXLSImpDialog {

	private TCComponentBOMWindow bomWindow = null;
	private OptionManager optionManager = null;
	private TCComponentBOMLine target = null;
	private org.eclipse.swt.widgets.List list = null; 
	private Button addBtn = null;
	private boolean isAllValidFiles = true;
	
	private String osi_no = "";
	private String product_no = "";
	
	//Excel을 로드하여 Product 옵션 코드를 담아 둘곳.
	private HashMap<String, VariantOption> productOption = new HashMap();
	//Excel을 로드하여 Variant별로 옵션 코드를 담아 둘곳.
	private HashMap<String, HashMap> variantOptions = new HashMap();
	//Excel에서 S:Standard로 표기된 옵션만 따로 저장.
	private HashMap<String, HashMap> standardVariantOptions = new HashMap();
	
	private HashMap<String, ManualTreeItem> treeItemMap = new HashMap();
	
	private int finishedCount = 0;
	private boolean isChecked = false;
	private String currentFilePath = null;
	private File exlFile = null;
	private HashMap<String, ArrayList<String>> osiSpec = new HashMap();
	
	// [NON-SR] [20150609] [ymjang] Base Spec IF 용 데이터 생성을 위한 Map 생성
	private HashMap<String, ArrayList<String>> baseSpec = new HashMap();
	
	public BWVariantOptionImpDialog(Shell parent, int style,
			TCComponentBOMLine coms) {
		super(parent, style, BWVariantOptionImpDialog.class);
		this.target = coms;
	}

	@Override
	public void dialogOpen() {
		
		this.shell.setSize(890, 708);
		this.excelFileGroup.setBounds(10, 10, 863, 126);
		
        this.lblAttachFileMsg = new Label(excelFileGroup, SWT.NONE);
        this.lblAttachFileMsg.setFont(SWTResourceManager.getFont("맑은 고딕", 13, SWT.BOLD));
        this.lblAttachFileMsg.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        this.lblAttachFileMsg.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 8, 1));
        this.lblAttachFileMsg.setText("※ 암호화된 문서는 반드시 암호를 해제하신 후, 등록하셔야 합니다. ※");
        this.lblAttachFileMsg.setBounds(10, 25, 600, 30);
        
		list = new org.eclipse.swt.widgets.List(excelFileGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		list.setBounds(10, 55, 600, 60);		
		
		addBtn = new Button(excelFileGroup, SWT.NONE);
		addBtn.setBounds(610, 55, 40, 30);
		addBtn.setText("Add");
		addBtn.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {

             
                shell.setCursor(waitCursor);
                try
                {
                    // Excel 파일 선택, 선택된 Excel Loading
                    selectTarget();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    shell.setCursor(arrowCursor);
                }
            }
            
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
		
		this.logGroup.setBounds(10, 141, 863, 481);
		//this.logGroup.setBounds(10, 111, 863, 481);
		
		String[] strColumnWidths = this.getTextBundleArray("TreeColumnWidths", null, this.dlgClass);
        String[] strTxtColmnWidths = this.getTextBundleArray("TxtRptColumnWidths", null, this.dlgClass);
		this.tree = new Tree(logGroup, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER | SWT.CHECK);
        for (int i = 0; i < this.szHeaderNames.length; i++)
        {
            TreeColumn tc = new TreeColumn(this.tree, SWT.BORDER);
            tc.setText(this.szHeaderNames[i]);
            tc.setWidth(Integer.parseInt(strColumnWidths[i]));
            
            this.szTxtHeaderWidth[i] = Integer.parseInt(strTxtColmnWidths[i]);
            
        }
        
        Listener heightSetter = new Listener()
        {
            public void handleEvent(Event event)
            {
                event.height = 20;
            }
        };
        this.tree.addListener(SWT.MeasureItem, heightSetter);
        tree.addListener(SWT.Selection, new Listener() {
	        public void handleEvent(Event event) {
	            if (event.detail == SWT.CHECK) {
	               
	               ManualTreeItem item = (ManualTreeItem)event.item;
	               ManualTreeItem parentItem = (ManualTreeItem)item.getParentItem();
	               
	               //Variant를 선택하여 체크한 경우
	               if( parentItem != null){
	            	   if( item.getChecked()){
	            		   parentItem.setChecked(item.getChecked());
	            	   }
	               }else{
	            	   //Product를 선택한 경우
	            	   TreeItem[] children = item.getItems();
	            	   
	            	   for( TreeItem child : children){
	            		   child.setChecked(item.getChecked());
	            	   }
	               }
	               
	            }
	        }
	    });
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);
	        
		this.tree.setBounds(10, 22, 843, 300);
		
		this.text.setBounds(10, 330, 843, 141);
		this.executeButton.setBounds(338, 642, 77, 22);
		this.cancelButton.setBounds(459, 642, 77, 22);
		this.viewLogButton.setBounds(750, 642, 120, 22);
		
		this.shell.addDisposeListener(new DisposeListener(){

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if( optionManager != null ){
					optionManager.clear(false);
				}
			}
			
		});
		this.shell.open();
		this.shell.layout();
	}

	
	@Override
	public void execute() throws Exception {
        // 실행 버튼 Disable
        super.executeButton.setEnabled(false);
        // Excel 검색 버튼 Disable
        super.searchButton.setEnabled(false);
        
		Job job = new Job("Option Apply"){

			@Override
			protected IStatus run(IProgressMonitor arg0) {
				try {
					
					apply();

	                shell.getDisplay().syncExec(new Runnable()
	                {
	                    
	                    public void run()
	                    {
	                        shell.setCursor(waitCursor);
	                        // **************************************//
	                        // Txt Upload Report 생성 //
	                        // --------------------------------------//
	                        
	                        // Report Factory Instance : HeaderNames, HeaderWidths, Num Column
	                        // Display Flag, Level Column Display Flag
	                        TxtReportFactory rptFactory = generateReport(true, true);
	                        
	                        if( strImageRoot == null || "".equals(strImageRoot) )
	                        {
	                          strImageRoot = "c:/temp";
	                          
	                          File temp = new File(strImageRoot);
	                          if(!temp.exists())
	                          {
	                            temp.mkdirs();
	                          }
	                        }
	                        
	                        String strDate = DateUtil.getClientDay("yyMMddHHmm");
	                        String strFileName = "Import_" + strDate + ".log";
	                        // Upload Log File FullPath
	                        strLogFileFullPath = strImageRoot + File.separatorChar + strFileName;
	                        
	                        // Import Log File 생성
	                        rptFactory.saveReport(strLogFileFullPath);
	                        
	                        try{
		                        // Error Count, Warning Count
		                        ManualTreeItem[] szTopItems = new ManualTreeItem[]{treeItemMap.get(target.getItem().getProperty("item_id"))};
		                        int[] szErrorCount = { 0, 0 };
		                        for (int i = 0; i < szTopItems.length; i++)
		                        {
		                            // Top TreeItem
		                            ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
		                            getErrorCount(topTreeItem, szErrorCount);
		                        }
		                        
		                        nWraningCount = szErrorCount[1];
		                        
		                        text.append("--------------------------\n");
		                        text.append("Warning : " + szErrorCount[1] + "\n\n");
		                        text.append("Error : " + szErrorCount[0] + "\n\n\n");
		                        text.append("[" + strLogFileFullPath + "] " + getTextBundle("LogCreated", "MSG", dlgClass) + "\n\n");
		                        
		                        if( szErrorCount[0] > 0 )
		                        {
		                            text.append(getTextBundle("ActionNotCompleted", "MSG", dlgClass) + "\n");
		                            MessageBox.post(shell, getTextBundle("ActionNotCompleted", "MSG", dlgClass), "Error", 2);
		        
		                        }
		                        else
		                        {
		                            text.append(getTextBundle("ActionCompleted", "MSG", dlgClass) + "\n");
		                            MessageBox.post(shell, getTextBundle("ActionCompleted", "MSG", dlgClass), "Notification", 2);
		                        }
		                        text.append("--------------------------\n");
	                        }catch( TCException tce){
	                        	tce.printStackTrace();
	                        }finally{
	                        	searchButton.setEnabled(true);
		                        viewLogButton.setEnabled(true);
	                        }
	                        
	                    }
	                });
					
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					shell.getDisplay().syncExec(new Runnable()
	                {
	                    public void run()
	                    {
	                    	shell.setCursor(arrowCursor);
	                    }
	                });
				}
				return new Status(IStatus.OK, "Loading ", "Job Completed");
			}
			
		};
		job.schedule();
	}

	
	/**
	 * 사용자가 선택한 Excel을 로드하고
	 * 현재 선택된 Product BOM Line 하위와 Excel에 정의된 Variant를 비교함. 
	 * 1. Excel에 정의되어 있는 Variant 아이템은 현재의 BOM에 모두 구성되어 있어야 한다.
	 * 2. Product에 정의된 모든 옵션 카테고리 및 옵션 코드는 Corporate Option Item에 존재해야한다.
	 */
	public void loadPre() throws Exception {
		if(optionManager != null){
			optionManager.clear(false);
		}
		
		// [SR150408-022] [20150529] [ymjang] Variant/Function EPL 수량표현 개선방안(양산 및 개발프로젝트 반영하여 출력할수 있도록)
		// Merge 된 O/Spec 은 xlsx 로서 POI 로 읽고, 기존의 O/Spec 은 xls 로서 jxl 로 읽음.
		shell.setCursor(waitCursor);
		String strFilePath = currentFilePath;
		if( strFilePath != null && !strFilePath.trim().equals("")){
			
			exlFile = new File(strFilePath);
            String strExt = strFilePath.substring(strFilePath.lastIndexOf(".")+1);
            
            if (strExt.toLowerCase().equals("xls"))
            {
            	excelLoad(exlFile);
            } else if (strExt.toLowerCase().equals("xlsx"))
            {
            	mergeExcelLoad(exlFile);
            }
		}
		
		Set set = variantOptions.keySet();
		
		HashMap childrenMap = new HashMap();
		AIFComponentContext[] contexts = target.getChildren();
		for(int i = 0; contexts != null && i < contexts.length; i++){
			TCComponentBOMLine childLine = (TCComponentBOMLine)contexts[i].getComponent();
			String item_id = childLine.getItem().getProperty("item_id").toUpperCase();
			if( item_id.length() > 5){
				item_id = item_id.substring(1, 6);
				childrenMap.put(item_id, childLine);
			}
		}
		Set bomlineSet = childrenMap.keySet();
		
		//1. Excel에 정의되어 있는 Variant 아이템은 현재의 BOM에 모두 구성되어 있어야 한다.
		Iterator<String> its = set.iterator();
		while(its.hasNext()){
			String itemId = its.next().toUpperCase();
			if( !bomlineSet.contains(itemId)){
				throw new Exception("not found '" + itemId + "' Variant Item");
			}
		}
		
		//2. Product에 정의된 모든 옵션 카테고리 및 옵션 코드는 Corporate Option Item에 존재해야한다.
		//OptionManager 생성
		optionManager = new OptionManager(target, true);
		ArrayList<VariantOption> corpOptionSet = optionManager.getCorpOptionSet();
		HashMap<String, VariantOption> corpOptionMap = optionManager.getCorpOptionMap();
		Iterator<String> corpKeyIts = corpOptionMap.keySet().iterator();
		
		Iterator<String> prdOptionIts = productOption.keySet().iterator();
		while(prdOptionIts.hasNext()){
			String optionName = prdOptionIts.next();
			if( corpOptionMap.keySet().contains(optionName)){
				//Product의 옵션 카테고리가 Corporate Option에 존재한다.
				VariantOption vOption = productOption.get(optionName);
				VariantOption corpOption = corpOptionMap.get(optionName);
				
				List<VariantValue> values = vOption.getValues();
				List<VariantValue> corpValues = corpOption.getValues();
				
				for( VariantValue val : values){
					if( !corpValues.contains(val)){
						throw new TCException(optionName + ":" + val.getValueName() + " is not found.\nplease register option code " + val.getValueName() + " at the Option Dictionary");
					}
				}
				
				//Coroporate Option에 정의된 추가적인 옵션 코드를 Excel에거 가져온 Option에 추가함. 
				for( VariantValue val : corpValues){
					if( !values.contains(val)){
						VariantValue newValue = new VariantValue(vOption, val.getValueName(), val.getValueDesc(), VariantValue.VALUE_NOT_DEFINE, false);
						vOption.addValue(newValue);
					}
				}
				
			}else{
				throw new TCException("Option category" + " " + optionName + " is not found.\nplease register option category " + optionName + " at the Option Dictionary");
			}
		}
		
	}

    @Override
	public void selectTarget() throws Exception {
        selectTargetFile();
        
        if (currentFilePath == null || currentFilePath.equals(""))
            return;
        
        // Excel Data Loading
        load();
        
        // Validation 수행 후 실행버튼 활성화
        if (validate()){
        	
        }
	}

    public void selectTargetFile()
    {
        
        FileDialog fDialog = new FileDialog(this.shell, SWT.SINGLE);
        fDialog.setFilterNames(new String[] { "Excel File" });
        // *.xls, *.xlsx Filter 설정
        fDialog.setFilterExtensions(new String[] { "*.xls*" });
        fDialog.open();
        
        String strfileName = fDialog.getFileName();
        if ((strfileName == null) || (strfileName.equals("")))
            return;
        
        this.strImageRoot = fDialog.getFilterPath();
        list.add(this.strImageRoot + File.separatorChar + strfileName);
        
        // 선택된 Excel File Loading
        // this.load();
        currentFilePath = this.strImageRoot + File.separatorChar + strfileName;
    }
	
	@Override
	public void loadPost() throws Exception {
		super.loadPost();
	}

	/**
	 * [SR150408-022] [20150529] [ymjang] Variant/Function EPL 수량표현 개선방안(양산 및 개발프로젝트 반영하여 출력할수 있도록)
	 * 기존의 O/Spec 의 경우, xls 타입으로 jxl 로 읽어야 함.
	 * @param file
	 * @throws Exception
	 */
	private void excelLoad(File file) throws Exception{
		
		// Excel Load Start
    	Workbook workBook = Workbook.getWorkbook(file);
    	Sheet sheet = workBook.getSheet(0);
    	
    	//all 을 찾은 후 그 다음 셀부터 시작해야함. 즉 7idx부터 시작하여 Eff-IN idx전까지 Variant를 읽어 간다.
    	int startVariantIdx = 7;
    	int endVariantIdx = -1;
    	for( int column = 7; column < sheet.getColumns(); column++){
    		Cell cell = sheet.getCell(column, 2);
    		if( cell instanceof LabelCell){
    			LabelCell label = (LabelCell)cell;
    			if("Eff-IN".equals(label.getString().trim())){
    				endVariantIdx = column ;
    			}
    		}
    	}
    	
    	Cell osiCell = sheet.getCell(0, 4);
    	String tmpStr = osiCell.getContents();
    	osi_no = tmpStr.substring(tmpStr.indexOf(":") + 1).trim();
    	
    	//imp.properties에서 값을 가져온다.
    	Registry customRegistry = Registry.getRegistry("com.kgm.common.bundlework.imp.imp");
    	int startVariantRowIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startVariantRowIdx"));
    	int startOptionCodeColumnIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startOptionCodeColumnIdx"));
    	int startOptionRowIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startOptionRowIdx"));
    	int optionCategoryDescColumnIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.optionCategoryDescColumnIdx"));
    	int optionCodeDescColumnIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.optionCodeDescColumnIdx"));
    	
    	int currentOptionRowIdx = startOptionRowIdx;
    	int endOptionCodeIdx = sheet.getRows() ;
    	String itemId = target.getItem().getProperty("item_id");
    	for( int column = startVariantIdx; column < endVariantIdx; column++){
    		
    		Cell variantCell = sheet.getCell(column, startVariantRowIdx);
    		String categoryDesc = "";
    		String codeDesc = "";
    		Cell codeCell = null, mergedCodeCell = null;
    		
    		HashMap<String, VariantOption> options = variantOptions.get(variantCell.getContents().toUpperCase());
    		if( options == null){
    			options = new HashMap();
    		}
    		
//    				Standard옵션에 값 추가
			HashMap<String, String> standardOptions = standardVariantOptions.get(variantCell.getContents().toUpperCase());
			if( standardOptions == null ){
				standardOptions = new HashMap();
			}
			
    		for( int row = currentOptionRowIdx; row < endOptionCodeIdx; row++){
    			codeCell = sheet.getCell(startOptionCodeColumnIdx,row);
    			if( !(codeCell instanceof LabelCell) && (row - 1) >= currentOptionRowIdx){
    				codeCell = mergedCodeCell;
    			}
    			
    			if( !(codeCell instanceof LabelCell) ){
    				continue;
    			}
    			
    			mergedCodeCell = codeCell;
    			Cell categoryDescCell = sheet.getCell(optionCategoryDescColumnIdx, row);
    			if( categoryDescCell instanceof LabelCell){
    				categoryDesc = categoryDescCell.getContents();
    			}
    			Cell codeDescCell = sheet.getCell(optionCodeDescColumnIdx, row);
    			if( codeDescCell instanceof LabelCell){
    				codeDesc = codeDescCell.getContents();
    			}
    			
    			String tmpCode = codeCell.getContents();
    			
    			//옵션 코드에서 앞 3자리는 옵션 카테고리이다.
    			if( tmpCode.length() < 3 ) continue;
    			String optionCategory = tmpCode.substring(0, 3);
    			VariantOption option = options.get(optionCategory);
    			if( option == null ){
    				option = new VariantOption(null, itemId, optionCategory, categoryDescCell.getContents());
    			}
    			
    			Cell cell = sheet.getCell(column, row);
    			if( cell instanceof LabelCell){
    				// [20140113] Label Cell에 대한 value가 ""이 아닌 " "이 들어옴으로 인해 잘못된 Option value가 추가되는 문제 발생.
    				// 이를 해결하기 위해 Cell 값을 trim 해서 비교하는 것으로 변경. 또한 null string 오류 예방을 위해 equals 기준 값 변경. 
    				//if( !((LabelCell) cell).getString().equals("") && !((LabelCell) cell).getString().equals("-")){
    				String sCellText = ((LabelCell) cell).getString();
    				if (sCellText != null)
    					sCellText = sCellText.trim();
    				if( !"".equals(sCellText) && !"-".equals(sCellText))
    				{
    					
    					VariantValue value = new VariantValue(option, tmpCode, codeDesc, VariantValue.VALUE_USE, false);
    					option.addValue(value);
    					options.put(option.getOptionName(), option);
    					
    					if(((LabelCell) cell).getString().equalsIgnoreCase("S")) {
    						if( !standardOptions.containsKey(tmpCode)){
    							standardOptions.put(optionCategory, tmpCode);
    						}
    					}
    					
    				}
    			}
    			
    		}
    		
    		variantOptions.put(variantCell.getContents().toUpperCase(), options);
    		standardVariantOptions.put(variantCell.getContents().toUpperCase(), standardOptions);
    		
    		//VariantOption의 모든 Option은 Product에 추가되어야 하며,
    		//duplicate 하여 ProductOptio에 추가함.
    		Collection collection = options.values();
    		Iterator<VariantOption> its = collection.iterator();
    		while( its.hasNext()){
    			
    			VariantOption option = its.next().duplicate();
    			VariantOption savedOption = null;
    			if( productOption.containsKey(option.getOptionName())){
    				savedOption = productOption.get(option.getOptionName());
    				
    				if( option.getValues() != null){
    					for( VariantValue value : option.getValues()){
        					VariantValue savedValue = savedOption.getValue(value.getValueName());
        					if( savedValue == null){
        						savedOption.addValue(value);
        					}
            			}	
    				}
    				
    			}else{
    				productOption.put(option.getOptionName(), option);
    			}
    			
    		}
    	}
		
	}
	
	/**
	 * [SR150408-022] [20150529] [ymjang] Variant/Function EPL 수량표현 개선방안(양산 및 개발프로젝트 반영하여 출력할수 있도록)
	 * Merge 된 O/Spec 의 경우, xlsx 타입으로 Poi 로 읽어야 함.
	 * @param file
	 * @throws Exception
	 */
	private void mergeExcelLoad(File file) throws Exception{
		
		// Excel Load Start
    	FileInputStream fin = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fin);
        XSSFSheet sheet = workbook.getSheetAt(0);
        
    	//all 을 찾은 후 그 다음 셀부터 시작해야함. 즉 7idx부터 시작하여 Eff-IN idx전까지 Variant를 읽어 간다.
        int headerLabelRowStart = 2;
        int lastColIndex = sheet.getRow(headerLabelRowStart).getLastCellNum();
    	int startVariantIdx = 7;
    	int endVariantIdx = -1;
    	for( int column = 7; column < lastColIndex; column++){
    		org.apache.poi.ss.usermodel.Cell cell = sheet.getRow(headerLabelRowStart).getCell(column);
			if("Eff-IN".equals(cell.getStringCellValue())){
				endVariantIdx = column ;
			}
    	}
    	
    	org.apache.poi.ss.usermodel.Cell osiCell = sheet.getRow(4).getCell(0);
    	String tmpStr = osiCell.getStringCellValue();
    	osi_no = tmpStr.substring(tmpStr.indexOf(":") + 1).trim();
    	
    	//imp.properties에서 값을 가져온다.
    	Registry customRegistry = Registry.getRegistry("com.kgm.common.bundlework.imp.imp");
    	int startVariantRowIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startVariantRowIdx"));
    	int startOptionCodeColumnIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startOptionCodeColumnIdx"));
    	int startOptionRowIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.startOptionRowIdx"));
    	int optionCategoryDescColumnIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.optionCategoryDescColumnIdx"));
    	int optionCodeDescColumnIdx = Integer.parseInt(customRegistry.getString("BWVariantOptionImpDialog.optionCodeDescColumnIdx"));
    	
    	int currentOptionRowIdx = startOptionRowIdx;
    	int endOptionCodeIdx = sheet.getLastRowNum();
    	String itemId = target.getItem().getProperty("item_id");
    	for( int column = startVariantIdx; column < endVariantIdx; column++){
    		
    		org.apache.poi.ss.usermodel.Cell variantCell = sheet.getRow(startVariantRowIdx).getCell(column);
    		String categoryDesc = "";
    		String codeDesc = "";
    		org.apache.poi.ss.usermodel.Cell codeCell = null;
    				
    		HashMap<String, VariantOption> options = variantOptions.get(variantCell.getStringCellValue().toUpperCase());
    		if( options == null){
    			options = new HashMap();
    		}
    		
    		// Standard옵션에 값 추가
			HashMap<String, String> standardOptions = standardVariantOptions.get(variantCell.getStringCellValue().toUpperCase());
			if( standardOptions == null ){
				standardOptions = new HashMap();
			}
			
    		for( int row = currentOptionRowIdx; row < endOptionCodeIdx; row++){
    			codeCell = sheet.getRow(row).getCell(startOptionCodeColumnIdx);

    			org.apache.poi.ss.usermodel.Cell categoryDescCell = sheet.getRow(row).getCell(optionCategoryDescColumnIdx);
				categoryDesc = categoryDescCell.getStringCellValue();
				org.apache.poi.ss.usermodel.Cell codeDescCell = sheet.getRow(row).getCell(optionCodeDescColumnIdx);
				codeDesc = codeDescCell.getStringCellValue();
    			
    			String tmpCode = codeCell.getStringCellValue();
    			
    			//옵션 코드에서 앞 3자리는 옵션 카테고리이다.
    			if( tmpCode.length() < 3 ) continue;
    			String optionCategory = tmpCode.substring(0, 3);
    			VariantOption option = options.get(optionCategory);
    			if( option == null ){
    				option = new VariantOption(null, itemId, optionCategory, categoryDescCell.getStringCellValue());
    			}
    			
    			org.apache.poi.ss.usermodel.Cell cell = sheet.getRow(row).getCell(column);
				// [20140113] Label Cell에 대한 value가 ""이 아닌 " "이 들어옴으로 인해 잘못된 Option value가 추가되는 문제 발생.
				// 이를 해결하기 위해 Cell 값을 trim 해서 비교하는 것으로 변경. 또한 null string 오류 예방을 위해 equals 기준 값 변경. 
				//if( !((LabelCell) cell).getString().equals("") && !((LabelCell) cell).getString().equals("-")){
				String sCellText = cell.getStringCellValue();
				if (sCellText != null)
					sCellText = sCellText.trim();
				if( !"".equals(sCellText) && !"-".equals(sCellText))
				{
					
					VariantValue value = new VariantValue(option, tmpCode, codeDesc, VariantValue.VALUE_USE, false);
					option.addValue(value);
					options.put(option.getOptionName(), option);
					
					if(cell.getStringCellValue().equalsIgnoreCase("S")) {
						if( !standardOptions.containsKey(tmpCode)){
							standardOptions.put(optionCategory, tmpCode);
						}
					}
					
				}
    			
    		}
    		
    		variantOptions.put(variantCell.getStringCellValue().toUpperCase(), options);
    		standardVariantOptions.put(variantCell.getStringCellValue().toUpperCase(), standardOptions);
    		
    		//VariantOption의 모든 Option은 Product에 추가되어야 하며,
    		//duplicate 하여 ProductOptio에 추가함.
    		Collection collection = options.values();
    		Iterator<VariantOption> its = collection.iterator();
    		while( its.hasNext()){
    			
    			VariantOption option = its.next().duplicate();
    			VariantOption savedOption = null;
    			if( productOption.containsKey(option.getOptionName())){
    				savedOption = productOption.get(option.getOptionName());
    				
    				if( option.getValues() != null){
    					for( VariantValue value : option.getValues()){
        					VariantValue savedValue = savedOption.getValue(value.getValueName());
        					if( savedValue == null){
        						savedOption.addValue(value);
        					}
            			}	
    				}
    				
    			}else{
    				productOption.put(option.getOptionName(), option);
    			}
    			
    		}
    	}
    	// Excel Load End
	}
	
	/**
	 * Excel에서 로드한 옵션 코드를 Product와 Variant에 적용한다.
	 * 
	 * @throws Exception
	 */
	private void apply() throws Exception{
		
		
		shell.getDisplay().syncExec(new Runnable() {

			public void run() {
				try {
					
					addBtn.setEnabled(false);
					
					for( ManualTreeItem item : itemList){
						treeItemMap.put(item.getItemID(), item);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		
		product_no = target.getItem().getProperty("item_id");
		final TreeItem productItem = treeItemMap.get(product_no);
		shell.getDisplay().syncExec(new Runnable() {

			public void run() {
				try {
					if( !productItem.getChecked()){
						text.append("Please check a Variant.\n");
						BWVariantOptionImpDialog.super.executeButton.setEnabled(true);
						isChecked = false;
					}else{
						isChecked = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		
		//Product가 선택되지 않으면 리턴.
		if( !isChecked ){
			throw new TCException("Please check a Variant.");
		}
		
		ArrayList<VariantOption> corpOptionSet = optionManager.getCorpOptionSet();
		
		Vector<String[]> userDefineErrorList = new Vector();
		Vector<String[]> moduleConstratintsList = new Vector();
		HashMap<String, VariantOption> optionMap = new HashMap();
		
		
		super.syncItemState(treeItemMap.get(product_no), super.STATUS_INPROGRESS, "현재 설정된 옵션 정보를 가져옵니다.");
		super.syncSetItemText(treeItemMap.get(product_no), 4, "현재 설정된 옵션 정보를 가져옵니다.");
		super.syncSetItemTextField("[" + product_no + "]현재 설정된 옵션 정보를 가져옵니다.");
		//target(Product)에 설정된 옵션 셋을 가져온다.
		ArrayList<VariantOption> optionSet = optionManager.getOptionSet(target, optionMap, userDefineErrorList, moduleConstratintsList);
		
		super.syncSetItemText(treeItemMap.get(product_no), 4, "옵션 정보를 설정합니다.");
		//1. Excel에서 Import한 Option이 Product에 존재하는지 확인하고, Product에 Excel에 정의된 옵션을 셋팅한다.
    	Collection collection = productOption.values();
    	Iterator<VariantOption> its =  collection.iterator();
    	int totalOptionCount = collection.size();
    	int curNum = 0;
    	
    	TCVariantService tcvariantservice = null;
    	tcvariantservice = target.getSession().getVariantService();
    	while( its.hasNext()){
    		curNum++;
    		
    		VariantOption option = its.next();
    		List<VariantValue> list = option.getValues();
    		//BOMline에서 가져온 Option에서도 발견되면, 정상적인 케이스
    		if( optionSet.contains(option)){
    			
    			VariantOption curOption = optionMap.get(option.getOptionName());
    			List<VariantValue> curList = curOption.getValues();
    			
    			//현재 설정된 Option code map
    			HashMap<String, VariantValue> curValueMap = curOption.getValueMap();
    			
    			for( VariantValue value : list){
    				
    				VariantValue curVal = curValueMap.get(value.getValueName()); 
    				boolean bFound = curVal == null ? false:true;
    				ArrayList<TCComponentBOMLine> corporateBOMLines = optionManager.getCorporateBOMLines();
    				
    				if( bFound ){
    					super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + value.getValueName() + " 는 이미 추가되어 있습니다.");
    				}else{
    					//옵션은 정의 되어 있지만, 옵션 코드는 정의 되어 있지 않음.
        				//Category에 현재의 옵션코드를 추가해야한다.
        				//Product에 설정된 옵션은 모두  Corporate Option Item에 정의된 옵션이므로
        				//Corporate Option Item에 정의된 옵션을 찾고, 그 옵션에 새로운 옵션 코드를 추가해야한다.
    					System.out.println("" + value.getValueName() + "는 추가되어야 함");
    					
    					if( corpOptionSet.contains(option)){
    						
    						super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + value.getValueName() + " 옵션코드를 추가합니다.");
    						
    						//현재의 BOM Line에서 가져온 옵션 코드들 + 엑셀에서 가져온 코드를 추가.
    						VariantValue curValue = curOption.getValue(value.getValueName());
    						if( curValue == null){
    							curOption.addValue(value);
    						}
    						
    						if( corporateBOMLines != null && corporateBOMLines.size() > 0){
    							
    							//Corporate Option Map에서 해당하는 옵션을 가져온다.
    							HashMap<String, VariantOption> corpOptionMap =  optionManager.getCorpOptionMap();
    							VariantOption corpOption = corpOptionMap.get(curOption.getOptionName());
    							String mvlStr = OptionManager.getCorpOptionString(curOption);
    							
    							TCComponentBOMLine corpDictionaryLine = corporateBOMLines.get(0);
    							corpDictionaryLine.refresh();
    							//Corporate Option Item의 옵션에 옵션 코드를 추가후 변경한다.
    							try{
	    							CustomMVPanel.changeOption(tcvariantservice, corpDictionaryLine, corpOption.getOveOptionId(), mvlStr);
    							}catch(TCException tce){
    								System.out.println(mvlStr);
    								tce.printStackTrace();
    							}finally{
    								target.refresh();
    							}
    							
    						}else{
    							throw new TCException("not found CorporateOption Item");
    						}
    					}else{
    						
    						throw new TCException("not found the option at the Corporate Option Item");
    					}
    				}
    				
    			}
    			
    		}else{
    			//BOMLine에서 발견할 수 없는 옵션이면 Corporate Option에서 검색하고, 거기에 있으면
    			//그 옵션을 Product에 셋팅, 
    			//없으면 새로 생성하여 Corporate Option Item에 추가하고, Product에도 추가한다.
    			System.out.println("optionSet에서 찾을 수 없는 옵션명 : " + option.getOptionName());
    			HashMap<String, VariantOption> corpOptionMap =  optionManager.getCorpOptionMap();
				VariantOption corpOption = corpOptionMap.get(option.getOptionName());
				
				//Corporate Option에서 찾을 수 없으므로
				//옵션을 생성 후 Corporate OPtion Item에 추가하고
				//Product에도 추가해야함.
				if( corpOption == null ){
					throw new TCException("not found the option at the Corporate Option Item");
				}else{
					super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + corpOption.getOptionName() + " 옵션을 추가합니다.");
					String mvlStr = OptionManager.getOptionString(corpOption);
					System.out.println(mvlStr);
					try{
						tcvariantservice.lineDefineOption(target, mvlStr);
					}catch( TCException tce){
						tce.printStackTrace();
						throw tce;
					}finally{
						target.refresh();
					}
				}
    		}
    	}
    	super.syncItemState(treeItemMap.get(product_no), super.STATUS_COMPLETED, "옵션 정보 설정을 완료하였습니다.");
    	super.syncSetItemText(treeItemMap.get(product_no), 4, "옵션 정보 설정을 완료하였습니다.");
    	super.syncSetItemTextField("[" + product_no +  "] 정보 저장 및 새로고침 중....");
    	target.window().save();
//    	target.window().refresh();
    	target.refresh();
    	
    	//기존에 사용중이었던 OptionCode는 비사용으로 변경.
    	Collection<VariantOption> curCollection = optionMap.values();
    	Iterator<VariantOption> curIts =  curCollection.iterator();
    	while(curIts.hasNext()){
    		VariantOption curOption = curIts.next();
    		List<VariantValue> curValues = curOption.getValues();
    		VariantOption option = productOption.get(curOption.getOptionName());
    		VariantValue tmpValue;
    		
    		if( option == null){
    			
    			//현재 Product에 설정된 옵션값들은 변경없이 사용함.
    			VariantOption tmpOption = new VariantOption(null, curOption.getItemId(), curOption.getOptionName(), curOption.getOptionDesc());
    			for( VariantValue curValue : curOption.getValues()){
    				 
//    				if( curValue.getValueStatus() != VariantValue.VALUE_NOT_DEFINE){
//    					tmpValue = new VariantValue(tmpOption, curValue.getValueName(), curValue.getValueDesc(), VariantValue.VALUE_NOT_USE, false);
//    				}else{
//    					tmpValue = new VariantValue(tmpOption, curValue.getValueName(), curValue.getValueDesc(), VariantValue.VALUE_NOT_DEFINE, false);
//    				}
    				tmpValue = new VariantValue(tmpOption, curValue.getValueName(), curValue.getValueDesc(), curValue.getValueStatus(), false);
    				tmpOption.addValue(tmpValue);
    				
    			}
    			productOption.put(tmpOption.getOptionName(), tmpOption);
    			
    		}else{
    			//현재 Product에 사용이 아닌 옵션값은 사용으로 변경. 
    			for( VariantValue curValue : curValues){
        			VariantValue value = option.getValue(curValue.getValueName());
        			
        			//Excel import에서 발견되었을때
        			if( value == null ){
        				//Excel에서 발견되면 무조건 사용으로 셋팅.
        				tmpValue = new VariantValue(option, curValue.getValueName(), curValue.getValueDesc(), VariantValue.VALUE_USE, false);
        				option.addValue(tmpValue);
        			}
        		}
    		}
    		
    	}
    	
    	//1-1. Excel에서 사용하지 않는 옵션코드는 유효성 검사를 추가한다.
    	super.syncSetItemText(treeItemMap.get(product_no), 4, "유효성 검사 항목을 추가합니다.");
    	super.syncSetItemTextField("[" + product_no + "] 유효성 검사 항목을 추가합니다.");
    	
    	HashMap<String, VariantErrorCheck> notUseErrorMap = new HashMap();
		HashMap<String, VariantErrorCheck> notDefineErrorMap = new HashMap();
		
		collection = productOption.values();
    	its =  collection.iterator();
    	totalOptionCount = collection.size();
    	curNum = 0;
//    	tcvariantservice = target.getSession().getVariantService();
    	while( its.hasNext()){
    		curNum++;
    		VariantOption option = its.next();
    		List<VariantValue> values = option.getValues();
    		//BOMline에서 가져온 Option에서도 발견되면, 정상적인 케이스
    		for( VariantValue value : values){
    			if( value.getValueStatus() == VariantValue.VALUE_NOT_DEFINE){
    				
    				VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(option.getOptionName());
					if( notDefineErrorcheck == null){
						notDefineErrorcheck = new VariantErrorCheck();
						notDefineErrorcheck.type = "inform";
						notDefineErrorcheck.message = VariantValue.TC_MESSAGE_NOT_DEFINE;
					}
					
    				ConditionElement condition = new ConditionElement();
					if( notDefineErrorcheck.getConditionSize() == 0 ){
						condition.ifOrAnd = "if";
					}else{
						condition.ifOrAnd = "or";
					}
					condition.item = target.getItem().getProperty("item_id");
					condition.op = "=";
					condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
					condition.value = value.getValueName();
					condition.valueIsString = true;
					condition.fullName = condition.item + ":" + condition.option;
					notDefineErrorcheck.addCondition( condition );
					
					notDefineErrorMap.put(option.getOptionName(), notDefineErrorcheck);
					
					super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + condition.option + ":" + condition.value + "을 유효성 검사 항목으로 추가합니다.");
    			}else if( value.getValueStatus() == VariantValue.VALUE_NOT_USE){
    				
    				VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(option.getOptionName());
					if( notUseErrorcheck == null){
						notUseErrorcheck = new VariantErrorCheck();
						notUseErrorcheck.type = "inform";
						notUseErrorcheck.message = VariantValue.TC_MESSAGE_NOT_USE;
					}
					
    				ConditionElement condition = new ConditionElement();
					if( notUseErrorcheck.getConditionSize() == 0 ){
						condition.ifOrAnd = "if";
					}else{
						condition.ifOrAnd = "or";
					}
					condition.item = target.getItem().getProperty("item_id");
					condition.op = "=";
					condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
					condition.value = value.getValueName();
					condition.valueIsString = true;
					condition.fullName = condition.item + ":" + condition.option;
			        notUseErrorcheck.addCondition( condition );		
			        notUseErrorMap.put(option.getOptionName(), notUseErrorcheck);
			        
			        super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + condition.option + ":" + condition.value + "을 유효성 검사 항목으로 추가합니다.");
    			}
    		}
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	Set<String> set = notUseErrorMap.keySet();
		Iterator<String> itrs = set.iterator();
		while( itrs.hasNext()){
			String key = itrs.next();
			VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(key);
			String msg = VariantValue.TC_MESSAGE_NOT_USE;
			ConditionElement[] elements = notUseErrorcheck.getCondition();
			for( int i = 0; elements != null && i < elements.length; i++){
				if( i == 0 ){
					msg += "[";
				}
				msg += (i > 0 ? ", ":"") + elements[i].value;
				if( i == elements.length-1 ){
					msg += "]";
				}
			}
			notUseErrorcheck.message = msg;		
	        notUseErrorcheck.appendConstraints(sb);
		}
		
		set = notDefineErrorMap.keySet();
		itrs = set.iterator();
		while( itrs.hasNext()){
			String key = itrs.next();
			VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(key);
			String msg = VariantValue.TC_MESSAGE_NOT_DEFINE;
			ConditionElement[] elements = notDefineErrorcheck.getCondition();
			for( int i = 0; elements != null && i < elements.length; i++){
				if( i == 0 ){
					msg += "[";
				}
				msg += (i > 0 ? ", ":"") + elements[i].value;
				if( i == elements.length-1 ){
					msg += "]";
				}
			}
			notDefineErrorcheck.message = msg;	
			notDefineErrorcheck.appendConstraints(sb);
		}
        
        try{
        	
        	tcvariantservice = target.getSession().getVariantService();
	        if( notUseErrorMap.size() > 0 || notDefineErrorMap.size() > 0 ){
	        	tcvariantservice.setLineMvl(target, sb.toString());
	        }else{
	        	tcvariantservice.setLineMvl(target, "");
	        }
        }catch(TCException tce){
        	System.out.println(sb);
        	tce.printStackTrace();
        }finally{
        	optionMap.clear();
    		userDefineErrorList.clear();
    		moduleConstratintsList.clear();
    		super.syncSetItemText(treeItemMap.get(product_no), 4, "모든 옵션 설정을 완료하였습니다.");
    		super.syncSetItemTextField("[" + product_no +  "] 정보 저장 및 새로고침 중....");
        	target.window().save();
//        	target.window().refresh();
        	target.refresh();
        }
    	
    	//2.Variant Item에 해당 옵션을 설정하고 사용하는 옵션코드 외의 코드는 constraint로 추가.
        int operationCount = 0;
    	AIFComponentContext[] contexts = target.getChildren();
		for(int i = 0; contexts != null && i < contexts.length; i++){
			final TCComponentBOMLine childLine = (TCComponentBOMLine)contexts[i].getComponent();
			final String variantId = childLine.getItem().getProperty("item_id");
			
			isChecked = false;
			shell.getDisplay().syncExec(new Runnable()
	        {
	            
	            public void run()
	            {
	            	ManualTreeItem treeNode = treeItemMap.get(variantId);
	            	if( treeNode.getChecked()) 
	            		isChecked = true;
	            }
	            
	        });
			
			if( !isChecked ) continue;
			
			TCSession session = childLine.getSession();
			operationCount++;
			BWVariantOptionImpOperation operation =  new BWVariantOptionImpOperation(childLine);
			operation.addOperationListener(new InterfaceAIFOperationListener() {
				
				@Override
				public void startOperation(String arg0) {
					
				}
				
				@Override
				public void endOperation() {
					finishedCount++;
				}
			});
			session.queueOperation(operation);
	    	
		}
		
		while( finishedCount < operationCount){
			Thread.sleep(1000);
		}
		
		//OSI 정보를 DB저장전 삭제
		//deleteOsiInfo();
		//OSI 정보를 DB에 저장.
		insertOsiInfo();
		
		// [SR없음][20150910][jclee] OSpec Trim 정보를 DB에 저장
		OSpec ospec = OpUtil.getOSpec(exlFile);
		insertOSpecTrim(ospec);
	}

	@Override
	public void load() throws Exception {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					
					shell.setCursor(waitCursor);
					
					// Load 전처리
					loadPre();
					// ManualTreeItem List
					itemList = new ArrayList<ManualTreeItem>();
					// TCComponentItemRevision List
					tcItemRevSet = new HashMap<String, TCComponentItemRevision>();
					
					tree.removeAll();
					// Header 정보 Loading
					loadHeader();
					
					// ManualTreeItem List
					itemList = new ArrayList<ManualTreeItem>();

					// Load BOM
					LoadJob job = new LoadJob(shell.getText());
					job.schedule();

					// loadOption();

					if (validate()) {
						executeButton.setEnabled(true);
						isAllValidFiles &= true;
					}else{
						isAllValidFiles &= false;
					}
				} catch (Exception e) {
					e.printStackTrace();
					isAllValidFiles &= false;
					executeButton.setEnabled(false);
					addBtn.setEnabled(false);
					MessageBox.post(shell, e.getMessage(), "Notification", 2);
				} finally {
					// Load 후처리
					try {
						loadPost();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						currentFilePath = null;
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
		String[] szClass = new String[] { "Item", "Item", "Revision", "Item" };
		String[] szAttr = new String[] { "Level", "item_id",
				"item_revision_id", "object_name" };
		this.headerModel = new BWItemModel();
		// Type 설정 (BOM)
		this.strTargetItemType = "S7_Product";
		for (int i = 0; i < szClass.length; i++) {
			this.headerModel
					.setModelData(szClass[i], szAttr[i], new Integer(i));
		}
	}

	public class LoadJob extends Job {

		// TCComponentItemRevision targetRevision;

		public LoadJob(String jobName) {
			super(jobName);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			try {

				shell.getDisplay().syncExec(new Runnable() {

					public void run() {
						try {
							shell.setCursor(waitCursor);
							addBtn.setEnabled(false);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});
				
				loadBOMData(target, null, 0);

				shell.getDisplay().syncExec(new Runnable() {

					public void run() {
						try {

							if (validate())
								executeButton.setEnabled(true);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});

			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				
				shell.getDisplay().syncExec(new Runnable() {

					public void run() {
						try {
							shell.setCursor(arrowCursor);
							addBtn.setEnabled(true);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});
			}

			return new Status(IStatus.OK, "Loading ", "Job Completed");
		}

	}

	/**
	 * BomLine,Item,ItemRevision,Dataset 속성정보를 ManualTreeItem에 저장
	 * 
	 * @param bomLine
	 *            : 대상 BomLine
	 * @param pTreeItem
	 *            :
	 * @throws Exception
	 */
	private void loadBOMData(final TCComponentBOMLine bomLine,
			final ManualTreeItem pTreeItem, final int nLevel) throws Exception {
		// Thread.sleep(2000);

		if (nLevel > 1)
			return;

		final TCComponentItemRevision itemRevision = bomLine.getItemRevision();

		final String strID = itemRevision.getProperty("item_id");
		final String strRevision = itemRevision.getProperty("item_revision_id");

		final String strMapKey = strID + "/" + strRevision;

		shell.getDisplay().syncExec(new Runnable() {

			public void run() {
				ManualTreeItem mTreeItem = null;

				// Top Line인 경우, Level(Tree구성) 사용하지 않는 경우
				if (pTreeItem == null) {

					mTreeItem = new ManualTreeItem(tree, tree.getItemCount(),
							ManualTreeItem.ITEM_TYPE_TCITEM, strID);
					mTreeItem.setLevel(0);

				} else {
					mTreeItem = new ManualTreeItem(pTreeItem, pTreeItem
							.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM,
							strID);
					mTreeItem.setLevel(nLevel);

				}

				tree.setSelection(mTreeItem);

				itemList.add(mTreeItem);

				try {

					setSTCItemData(mTreeItem, bomLine.getItem(),
							CLASS_TYPE_ITEM);
					setSTCItemData(mTreeItem, itemRevision, CLASS_TYPE_REVISION);

				} catch (Exception e) {
					e.toString();
				}

			}

		});

		ManualTreeItem currentItem = this.itemList
				.get(this.itemList.size() - 1);

		//[20131025] 옵션 import 속도 개선.
		if( bomLine.getItem().getType().equals(TcDefinition.VARIANT_ITEM_TYPE)){
			return;
		}
		TCComponent[] children = bomLine.getRelatedComponents("bl_child_lines");
		
		for (TCComponent child : children) {
			TCComponentBOMLine childBOMLine = (TCComponentBOMLine) child;
			//[SR190920-033][CSH] Obsolete된 Variant는 리스트에서 제외
			String status = childBOMLine.getProperty("s7_MATURITY");
			if(status != null && status.equals("Obsolete")){
				continue;
			}
			this.loadBOMData(childBOMLine, currentItem, nLevel + 1);
		}
	}

	public void setSTCItemData(ManualTreeItem treeItem, TCComponent component,
			String strType) throws TCException {
		HashMap<String, Integer> attrIndexMap = this.headerModel
				.getModelAttrs(strType);
		Object[] attrNames = attrIndexMap.keySet().toArray();

		for (int i = 0; i < attrNames.length; i++) {
			String strAttrName = (String) attrNames[i];

			String strAttrValue = component.getProperty((String) attrNames[i]);
			treeItem.setBWItemAttrValue(strType, strAttrName, strAttrValue);

		}
		this.setTreeItemData(treeItem, strType);
	}

	/**
	 * 추후 Report를 위해, OSI 정보를 DB에 저장함.
	 * 
	 * @throws Exception
	 */
	private void insertOsiInfo() throws Exception{
		if( !osiSpec.isEmpty()){
		    SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.put("PRODUCT_NO", product_no);
			ds.put("OSI_NO", osi_no);
			ds.put("OSI_SPEC", osiSpec);
			// [NON-SR] [20150609] [ymjang] Base Spec IF 용 데이터 생성을 위한 Map 생성
			ds.put("BASE_SPEC", baseSpec);
			
			// [NON-SR] [20160825] [ymjang] 동일한 Vaiant 에 각 옵션별로 업로드시간이 조금씩 틀려서 HBOM에서 최신 O/Spec 정보를 읽을 때 문제 발행.
			DateFormat dtf = new SimpleDateFormat("yyyyMMddHHmmss"); 
	        String today = dtf.format(new Date());
			ds.put("CRE_DATE", today);
			ds.put("UPD_DATE", today);
			
			// [NON-SR] [20150430] ymjang, IF_OSPEC_MASTER_FROM_HBOM 에 insert 시에 Variant Option 일부 누락 발생
			// 생성된 데이터를 학인하기 위하여 로그를 생성함.
			Set osiSpecKeys = osiSpec.keySet();
			Iterator<String> osiSpecs = osiSpecKeys.iterator();
			while(osiSpecs.hasNext()){
				String variantID = osiSpecs.next();
				ArrayList<String> values = osiSpec.get(variantID);
				
				for( String value : values){
					BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantID + ", " + osi_no + "] " + value + " inserted.");
				}
			}
			
			// [NON-SR] [20150609] [ymjang] Base Spec IF 용 데이터 생성을 위한 Map 생성
			// 생성된 데이터를 학인하기 위하여 로그를 생성함.
			Set baseSpecKeys = baseSpec.keySet();
			Iterator<String> baseSpecs = baseSpecKeys.iterator();
			while(baseSpecs.hasNext()){
				String variantID = baseSpecs.next();
				ArrayList<String> values = baseSpec.get(variantID);
				
				for( String value : values){
					BWVariantOptionImpDialog.super.syncSetItemTextField("BASE SPEC [" + variantID + ", " + osi_no + "] " + value + " inserted.");
				}
			}
			
			// [NON-SR] [20150609] ymjang, 오타 긴급 수정
			// com.kgm.serv1ice.VariantService --> com.kgm.service.VariantService
			remote.execute("com.kgm.service.VariantService", "insertOsiInfo", ds);
		}
	}
	
	/**
	 * OSpec Trim 정보를 DB에 저장
	 * @param ospec
	 */
	private void insertOSpecTrim(OSpec ospec) throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		
		String sOSINo = ospec.getOspecNo();
		String sProjectCode = ospec.getProject();
		
		// 기존에 동일한 OSI No로 가 존재할 경우 Delete.
		DataSet dsTemp = new DataSet();
		dsTemp.put("OSI_NO", sOSINo);
		ArrayList<HashMap<String, String>> result = (ArrayList<HashMap<String, String>>)remote.execute("com.kgm.service.VariantService", "selectOSpecTrim", dsTemp);
		
		if (result.size() > 0) {
			boolean isDelete = (Boolean) remote.execute("com.kgm.service.VariantService", "deleteOSpecTrim", dsTemp);
			
			if (!isDelete) {
				MessageBox.post("", "", MessageBox.ERROR);
				throw new Exception("Cannot insert the OSI Trim Information to DB.");
			}
		}
		
		ArrayList<OpTrim> alTrims = ospec.getTrimList();
		for (int inx = 0; inx < alTrims.size(); inx++) {
			DataSet ds = new DataSet();
			OpTrim trim = alTrims.get(inx);
			
			Option transmissionOption = getStandardOption(ospec, trim.getTrim(), "E00");
			Option transferCaseOption = getStandardOption(ospec, trim.getTrim(), "E10");
			
			String sTrimCode = trim.getTrim();
			String sArea = trim.getArea();
			String sPassenger = trim.getPassenger();
			String sEngineCode = trim.getEngine();
			String sGrade = trim.getGrade();
			String sTMOptionCode = transmissionOption == null ? "." : transmissionOption.getOpValue();
			String sTMOptionDesc = transmissionOption == null ? "." : transmissionOption.getOpValueName();
			String sWheelTypeCode = transferCaseOption == null ? "." : transferCaseOption.getOpValue();
			String sWheelTypeDesc = transferCaseOption == null ? "." : transferCaseOption.getOpValueName();
			String sTrimSeq = String.valueOf(inx);
			String sGModelCode = ospec.getgModel();
			
			ds.put("OSI_NO", sOSINo);
			ds.put("PROJECT_NO", sProjectCode);
			ds.put("VARIANT_HEADER", sTrimCode);
			ds.put("OSPEC_SECOND", sArea);
			ds.put("OSPEC_THIRD", sPassenger);
			ds.put("OSPEC_FOURTH", sEngineCode);
			ds.put("OSPEC_SIX", sGrade);
			ds.put("OPT_E00", sTMOptionCode);
			ds.put("OPT_E00_DESC", sTMOptionDesc);
			ds.put("OPT_E10", sWheelTypeCode);
			ds.put("OPT_E10_DESC", sWheelTypeDesc);
			ds.put("TRIM_SEQ", sTrimSeq);
			ds.put("GMODEL_CODE", sGModelCode);
			
			remote.execute("com.kgm.service.VariantService", "insertOSpecTrim", ds);
		}
	}
	
	/**
	 * Trim 내에서 해당 Category가 S를 갖는 Value를 Return
	 * @param ospec
	 * @param trim
	 * @param optionCategory
	 * @return
	 */
	private Option getStandardOption(OSpec ospec, String trim, String optionCategory){
		HashMap<String, ArrayList<Option>> optionMap = ospec.getOptions();
		ArrayList<Option> optionList = optionMap.get(trim);
		
		for( Option option : optionList){
			if( optionCategory.equals( option.getOp()) && option.getValue().equalsIgnoreCase("S")){
				return option;
			}
		}
		
		return null;
	}
	
	/**
	 * Excel에서 옵션 정보를 로딩하여 Variant BOM line에 옵션을 설정한다.
	 * 
	 * @author slobbie
	 *
	 */
	class BWVariantOptionImpOperation extends AbstractAIFOperation{

		TCComponentBOMLine childLine;
		TCVariantService tcvariantservice;
		BWVariantOptionImpOperation(TCComponentBOMLine childLine){
			this.childLine = childLine;
			this.tcvariantservice = childLine.getSession().getVariantService();
		}
		
		public void executeOperation() throws Exception {
			
			childLine.refresh();
			String variantId = childLine.getItem().getProperty("item_id");
			String variantFullId = variantId;
			if( variantId.length() < 7 )
				throw new TCException("Invalid Item ID type!");
			
			variantId = variantId.substring(1, 6).toUpperCase();
			
			BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "] 현재 설정된 옵션 정보를 가져옵니다.");
			BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_INPROGRESS, "현재 설정된 옵션 정보를 가져옵니다.");
			HashMap<String, VariantOption> variantMap = variantOptions.get(variantId.toUpperCase());
			if( variantMap == null) {
				BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantFullId), 4, "Skip");
				BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "]import파일에서 찾을 수 없으므로, Skip합니다.");
				return;
			}
			
			HashMap<String, VariantOption> optionMap = new HashMap();
			Vector<String[]> userDefineErrorList = new Vector();
			Vector<String[]> moduleConstratintsList = new Vector();
			
			AbstractAIFUIApplication aifApp = AIFUtility.getCurrentApplication();
			PSEApplicationService service = (PSEApplicationService)aifApp;
			BOMTreeTable treeTable = (BOMTreeTable)service.getAbstractViewableTreeTable();
			treeTable.clearSelection();
			treeTable.addSelectedBOMLine(childLine);
			OptionManager manager = new OptionManager(childLine,true);
			ArrayList<VariantOption> optionSet = manager.getOptionSet(childLine, optionMap, userDefineErrorList, moduleConstratintsList, true, true);
			
			try{
				ArrayList<String> variantSpecs = osiSpec.get(variantId);
				if( variantSpecs == null ){
					variantSpecs = new ArrayList();
				}
				
				Collection collection = variantMap.values();
		    	Iterator<VariantOption> its =  collection.iterator();
		    	int totalOptionCount = collection.size();
		    	int curNum = 0;
		    	while( its.hasNext()){
		    		curNum++;
		    		
		    		VariantOption option = its.next();
		    		List<VariantValue> list = option.getValues();
		    		//BOMline에서 가져온 Option에서도 발견되면, 정상적인 케이스
		    		if( optionSet.contains(option)){
		    			VariantOption curOption = optionMap.get(option.getOptionName());
		    			
		    			//현재 BOMLIne에 설정된 Option code map
		    			HashMap<String, VariantValue> curValueMap = curOption.getValueMap();
		    			
		    			for( VariantValue value : list){
		    				
		    				VariantValue curVal = curValueMap.get(value.getValueName()); 
		    				boolean bFound = curVal == null ? false:true;

		    				//Osi Spec으로 추가할 옵션코드를 저장.
		    				variantSpecs.add(value.getValueName());
		    				if( bFound ){
		    					BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + value.getValueName() + " 는 이미 추가되어 있습니다.");
		    				}else{
		    					BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + value.getValueName() + " 옵션코드를 추가합니다.");
	    						
	    						//현재의 BOM Line에서 가져온 옵션 코드들 + 엑셀에서 가져온 코드를 추가.
		    					VariantValue curValue = curOption.getValue(value.getValueName());
	    						if( curValue == null){
	    							curOption.addValue(value);
	    						}
	    						
	    						//OSI_NO와 Varaint_NO, Option_VAL을 TC DB에 저장함.
	    						
	    						
			    				//옵션은 정의 되어 있지만, 옵션 코드는 정의 되어 있지 않음.
			    				//Category에 현재의 옵션코드를 추가해야한다.
			    				//Product에 설정된 옵션은 모두  Corporate Option Item에 정의된 옵션이므로
			    				//Corporate Option Item에 정의된 옵션을 찾고, 그 옵션에 새로운 옵션 코드를 추가해야한다.
			    				
			    				//Corporate Option Map에서 해당하는 옵션을 가져온다.
			    				//옵션코드의 유무는 Product에서 이미 체크완료하여, 더이상  확인하지 않음. 
								HashMap<String, VariantOption> corpOptionMap =  optionManager.getCorpOptionMap();
								VariantOption corpOption = corpOptionMap.get(curOption.getOptionName());
								
								String mvlStr = OptionManager.getOptionString(corpOption);
								
								try{
									//Corporate Option Item의 옵션에 옵션 코드를 추가후 변경한다.
									CustomMVPanel.changeOption(tcvariantservice, childLine, corpOption.getOveOptionId(), mvlStr);
								}catch(TCException tce){
									System.out.println(mvlStr);
									tce.printStackTrace();
									throw tce;
								}finally{
									childLine.refresh();
								}
		    				}
		    				
		    			}
		    		}else{
		    			
						// [NON-SR] [20150430] ymjang, IF_OSPEC_MASTER_FROM_HBOM 에 insert 시에 Variant Option 일부 누락 발생
		    			// if_ospec_master_from_hbom table insert 를 위한 variantSpecs 에 해당 옵션값을 넣어줌.
						for( VariantValue value : list){
		    				//OSI Spec으로 추가할 옵션코드를 저장.
		    				variantSpecs.add(value.getValueName());
						}
		    			
		    			//Product에 정의된 옵션은 이미 Corporate Option아이템이 존재한다고 가정함.
		    			//이미 이전에 체크 완료.
		    			BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + option.getOptionName() + " 옵션을 추가합니다.");
		    			HashMap<String, VariantOption> corpOptionMap =  optionManager.getCorpOptionMap();
						VariantOption corpOption = corpOptionMap.get(option.getOptionName());
						
						String mvlStr = OptionManager.getOptionString(corpOption);
						try{
							tcvariantservice.lineDefineOption(childLine, mvlStr);
						}catch(TCException tce){
							System.out.println(mvlStr);
							tce.printStackTrace();
							throw tce;
						}finally{
							childLine.refresh();
						}
		    		}
		    	}
		    	
		    	//osiSpec에 저장함.
		    	osiSpec.put(variantId, variantSpecs);
		    	
		    	// [NON-SR] [20150609] [ymjang] Base Spec IF 용 데이터 생성을 위한 Map 생성
		    	HashMap<String, String> standardvariantMap = standardVariantOptions.get(variantId.toUpperCase());
				if( standardvariantMap != null) {
					
					ArrayList<String> variantBaseSpecs = baseSpec.get(variantId);
					if( variantBaseSpecs == null ){
						variantBaseSpecs = new ArrayList();
					}

					Iterator<String> varKeys = standardvariantMap.keySet().iterator();
					while(varKeys.hasNext()){
						String optionCat = varKeys.next();
						String optionCode = standardvariantMap.get(optionCat);
						
						if (!variantBaseSpecs.contains(optionCode))
							variantBaseSpecs.add(optionCode);
					}
					
					//baseSpec에 저장함.
					baseSpec.put(variantId, variantBaseSpecs);
				}	
				
		    	
		    	BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_INPROGRESS, "옵션 정보 설정을 완료하였습니다.");
		    	BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantFullId), 4, "옵션 정보 설정을 완료하였습니다.");
			}catch( Exception e){
				
				//osi Spec업데이트시에 Osi_no와 Variant단위로 스펙을 제거하므로.
				//에러가 발생하여 osiSpec에 포함되지 않은 경우는 osi spec이 제거되지 않는다. 
				osiSpec.remove(variantId);
				BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_ERROR, "옵션 정보 설정 중 오류가 발생되었습니다.");
			}finally{
				childLine.window().save();
//				childLine.window().refresh();
				childLine.refresh();
			}
			
			
			
			//기존에 사용중이었던 OptionCode는 비사용으로 변경.
	    	Collection curCollection = optionMap.values();
	    	Iterator<VariantOption> curIts =  curCollection.iterator();
	    	while(curIts.hasNext()){
	    		VariantOption curOption = curIts.next();
	    		List<VariantValue> curValues = curOption.getValues();
	    		
//	    		Excel에서 import할 Option
	    		VariantOption option = variantMap.get(curOption.getOptionName());
	    		
	    		if( option == null){
	    			for( VariantValue value : curOption.getValues()){
	    				if( value.getValueStatus() != VariantValue.VALUE_NOT_DEFINE){
	    					value.setValueStatus(VariantValue.VALUE_NOT_USE);
	    				}
	    			}
	    			VariantOption tmpOption = new VariantOption(null, curOption.getItemId(), curOption.getOptionName(), curOption.getOptionDesc());
	    			for(VariantValue tmpVal : curOption.getValues()){
	    				tmpOption.addValue(tmpVal);
	    			}
	    			variantMap.put(tmpOption.getOptionName(), tmpOption);
	    		}
	    	}
	    	
	    	
	    	HashMap<String, VariantErrorCheck> notUseErrorMap = new HashMap();
			HashMap<String, VariantErrorCheck> notDefineErrorMap = new HashMap();
			
			try{
				Collection collection = variantMap.values();
		    	Iterator<VariantOption> its =  collection.iterator();
		    	int totalOptionCount = collection.size();
		    	int curNum = 0;
		    	while( its.hasNext()){
		    		curNum++;
		    		VariantOption option = its.next();
		    		List<VariantValue> values = option.getValues();
		    		
		    		VariantOption pOption = productOption.get(option.getOptionName());
		    		if(pOption == null){
		    			//import 할 Excel에는 없는 옵션이므로 통과
		    			continue;
		    		}
		    		List<VariantValue> pValues = pOption.getValues();
		    		
		    		for( VariantValue val : pValues){
						if( !values.contains(val)){
							VariantValue newValue = new VariantValue(option, val.getValueName(), val.getValueDesc(), VariantValue.VALUE_NOT_DEFINE, false);
							option.addValue(newValue);
						}
					}
		    		
		    		values = option.getValues();
		    		for( VariantValue value : values){
		    			if( value.getValueStatus() == VariantValue.VALUE_NOT_DEFINE){
		    				
		    				VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(option.getOptionName());
							if( notDefineErrorcheck == null){
								notDefineErrorcheck = new VariantErrorCheck();
								notDefineErrorcheck.type = "inform";
								notDefineErrorcheck.message = VariantValue.TC_MESSAGE_NOT_DEFINE;
							}
							
		    				ConditionElement condition = new ConditionElement();
							if( notDefineErrorcheck.getConditionSize() == 0 ){
								condition.ifOrAnd = "if";
							}else{
								condition.ifOrAnd = "or";
							}
							condition.item = target.getItem().getProperty("item_id");
							condition.op = "=";
							condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
							condition.value = value.getValueName();
							condition.valueIsString = true;
							condition.fullName = condition.item + ":" + condition.option;
							notDefineErrorcheck.addCondition( condition );
							notDefineErrorMap.put(option.getOptionName(), notDefineErrorcheck);
							
							BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + condition.option + ":" + condition.value + "을 유효성 검사 항목으로 추가합니다.");
		    			}else if( value.getValueStatus() == VariantValue.VALUE_NOT_USE){
		    				
		    				VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(option.getOptionName());
							if( notUseErrorcheck == null){
								notUseErrorcheck = new VariantErrorCheck();
								notUseErrorcheck.type = "inform";
								notUseErrorcheck.message = VariantValue.TC_MESSAGE_NOT_USE;
							}
							
		    				ConditionElement condition = new ConditionElement();
							if( notUseErrorcheck.getConditionSize() == 0 ){
								condition.ifOrAnd = "if";
							}else{
								condition.ifOrAnd = "or";
							}
							condition.item = target.getItem().getProperty("item_id");
							condition.op = "=";
							condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
							condition.value = value.getValueName();
							condition.valueIsString = true;
							condition.fullName = condition.item + ":" + condition.option;
					        notUseErrorcheck.addCondition( condition );		
					        notUseErrorMap.put(option.getOptionName(), notUseErrorcheck);
					        
					        BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + condition.option + ":" + condition.value + "을 유효성 검사 항목으로 추가합니다.");
		    			}
		    		}
		    		
		    	}
		    	
		    	StringBuilder sb = new StringBuilder();
		    	Set<String> set = notUseErrorMap.keySet();
				Iterator<String> itrs = set.iterator();
				while( itrs.hasNext()){
					String key = itrs.next();
					VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(key);
					String msg = VariantValue.TC_MESSAGE_NOT_USE;
					ConditionElement[] elements = notUseErrorcheck.getCondition();
					for( int i = 0; elements != null && i < elements.length; i++){
						if( i == 0 ){
							msg += "[";
						}
						msg += (i > 0 ? ", ":"") + elements[i].value;
						if( i == elements.length-1 ){
							msg += "]";
						}
					}
					notUseErrorcheck.message = msg;		
			        notUseErrorcheck.appendConstraints(sb);
				}
				
				set = notDefineErrorMap.keySet();
				itrs = set.iterator();
				while( itrs.hasNext()){
					String key = itrs.next();
					VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(key);
					String msg = VariantValue.TC_MESSAGE_NOT_DEFINE;
					ConditionElement[] elements = notDefineErrorcheck.getCondition();
					for( int i = 0; elements != null && i < elements.length; i++){
						if( i == 0 ){
							msg += "[";
						}
						msg += (i > 0 ? ", ":"") + elements[i].value;
						if( i == elements.length-1 ){
							msg += "]";
						}
					}
					notDefineErrorcheck.message = msg;	
					notDefineErrorcheck.appendConstraints(sb);
				}
	            
	            try{
		            if( notUseErrorMap.size() > 0 || notDefineErrorMap.size() > 0 ){
		            	tcvariantservice.setLineMvl(childLine, sb.toString());
		            }else{
		            	tcvariantservice.setLineMvl(childLine, "");
		            }
	            }catch( TCException tce){
	            	int errorCodes = tce.getErrorCode();
	            	
	            	System.out.println(sb.toString());
	            	tce.printStackTrace();
	            	throw tce;
	            	//수행은 되지만 잠재적인 문제로 Exception이 발생하는 경우가 있어서.
	            	//권한이 없는 경우에만 에러 발생.
//	            	if( errorCodes == 51003){
//	            		osiSpec.remove(variantId);
//	            		throw tce;
//	            	}
	            	
	            }finally{
	            	childLine.window().save();
//	            	childLine.window().refresh();
	            	childLine.refresh();
	            }
	            
//	            DB에 O-Spec정보를 입력하기위해 키값을 TC의 Variant 아이템 아이디로 변경한다.
	            ArrayList<String> tmpList = osiSpec.get(variantId);
	            osiSpec.remove(variantId);
	            osiSpec.put(variantFullId, tmpList);
		    	
	            // [NON-SR] [20150609] [ymjang] Base Spec IF 용 데이터 생성을 위한 Map 생성
	            ArrayList<String> tmpBspecList = baseSpec.get(variantId);
	            baseSpec.remove(variantId);
	            baseSpec.put(variantFullId, tmpBspecList);
	            
		    	BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_COMPLETED, "유효성 검사 추가를 완료하였습니다.");
		    	BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantFullId), 4, "유효성 검사 추가를 완료하였습니다.");
		    	
		    	BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_COMPLETED, "Stored Option Set을 생성합니다.수분가량 소요 될 수 있습니다.");
		    	createStoredOptionSet(childLine);
		    	BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantFullId), 4, "완료되었습니다.");
		    	BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_COMPLETED, "완료되었습니다.");
			}catch( Exception e){
				e.printStackTrace();
				BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_ERROR, e.getMessage());
			}finally{
				childLine.clearCache();
				System.gc();
			}
		}
		
	}
	
	/**
	 * Stored Option Set을 생성하고 저장함.
	 * @param line
	 * @throws TCException
	 */
	private void createStoredOptionSet(TCComponentBOMLine line) throws TCException{
//		IMAN_reference
		String variantId = line.getItem().getProperty("item_id").toUpperCase();
		variantId = variantId.substring(1, 6).toUpperCase();
		TCComponentItemRevision tRevision = line.getItemRevision();
		AIFComponentContext[] context =  tRevision.getChildren("IMAN_reference");
		for( int j = 0; context != null && j < context.length; j++){
			TCComponent com =  (TCComponent)context[j].getComponent();
			String comType = com.getType();
			if( comType.equals("StoredOptionSet")){
				String comName = com.getProperty("object_name");
				if( comName.equals(line.getItem().getProperty("item_id") + "_BASE")){
					try{
						tRevision.remove("IMAN_reference", com);
						com.delete();
					}catch( TCException tce){
						BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantId), BWVariantOptionImpDialog.super.STATUS_WARNING, "참조중이므로 삭제 할 수 없습니다. NewStuff로 이동합니다.");
				    	BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantId), 4, "참조중이므로 삭제 할 수 없습니다. NewStuff로 이동합니다.");
						line.getSession().getUser().getNewStuffFolder().add("contents", com);
//						try{
//							com.delete();
//						}catch(TCException e){
//							throw e;
//						}
					}
				}
			}
		}
		
		TCComponentBOMWindow window = null;
		SelectedOptionSetDialog sosDlg = null;
		try{
			TCSession session = line.getSession();
			TCComponentBOMWindowType winType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		    TCComponentRevisionRuleType tccomponentrevisionruletype = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
			window = winType.create(tccomponentrevisionruletype.getDefaultRule());
			TCComponentBOMLine newTopLine = window.setWindowTopLine(null, line.getItemRevision(), null, null);
			sosDlg = new SelectedOptionSetDialog(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication(), newTopLine);
			sosDlg.setValue(line, standardVariantOptions.get(variantId));
			
			TCVariantService variantService = newTopLine.getSession().getVariantService();
			TCComponent sosComponent = variantService.getSos(newTopLine);
			
			//TCComponentVariantRule parentVariantRule = newTopLine.window().askVariantRule();
			TCComponentVariantRule parentVariantRule = null;
			List<TCComponentVariantRule> rules = newTopLine.window().askVariantRules();
			if(rules != null && rules.size() > 0) {
				parentVariantRule = rules.get(0);
			}
			
			if(parentVariantRule != null) {
				TCComponentVariantRule legacyVariantRule = parentVariantRule.copy();
				TCComponent tccomponent = variantService.createVariantConfig(
						legacyVariantRule, new TCComponent[] {  sosComponent });
				TCComponent tccomponent1;
				try {
					tccomponent1 = variantService.writeStoredConfiguration(newTopLine.getItem().getProperty("item_id") + "_BASE",
							tccomponent);
					tccomponent1.setStringProperty("object_desc", "Standard Option");
//				tccomponent1.setProperty("s7_BUILDSPEC", "Y");
					tccomponent1.setProperty("s7_PROJECT_CODE", tRevision.getProperty("s7_PROJECT_CODE"));
					tccomponent1.save();
				} catch (TCException tcexception) {
					variantService.deleteVariantConfig(tccomponent);
					throw tcexception;
				}
				variantService.deleteVariantConfig(tccomponent);
				TCComponentItemRevision tccomponentitemrevision = newTopLine.getItemRevision();
				tccomponentitemrevision.add("IMAN_reference", tccomponent1);				
			}		
		}catch(TCException e){
			e.printStackTrace();
		}finally{
			window.close();
			sosDlg.dispose();
		}
		
	}
}
