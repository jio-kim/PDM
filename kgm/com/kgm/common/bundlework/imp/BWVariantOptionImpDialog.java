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
 * [NON-SR] [20150430] ymjang, IF_OSPEC_MASTER_FROM_HBOM �� insert �ÿ� Variant Option �Ϻ� ���� �߻�
 * [NON-SR] [20150609] [ymjang] Base Spec IF �� ������ ������ ���� Map ����
 * [SR150408-022] [20150529] [ymjang] Variant/Function EPL ����ǥ�� �������(��� �� ����������Ʈ �ݿ��Ͽ� ����Ҽ� �ֵ���)
 * [NON-SR] [20160825] [ymjang] ������ Vaiant �� �� �ɼǺ��� ���ε�ð��� ���ݾ� Ʋ���� HBOM���� �ֽ� O/Spec ������ ���� �� ���� ����.
 * [20170314][ymjang] ��ȣȭ ���� ��� ���� ����
 */

/**
 * O-SPEC�� Excel�� Import�ϴ� ���.
 * #### ��ȿ�� ���� ######
 * 1. ������ Product BOM Line ������ Excel�� ǥ��� Variant BOM Line�� ��� ���� �ϴ°�?
 *     Y : continue          N : throw ERROR
 * 2. Excel�� Product�� ���ǵ� �ɼ��ڵ尡 Corporate Option Item�� �����ϴ°�?
 *     Y : continue          N : throw ERROR(�ɼ��ڵ带 �켱 ���� �Ͻÿ�).
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
	
	//Excel�� �ε��Ͽ� Product �ɼ� �ڵ带 ��� �Ѱ�.
	private HashMap<String, VariantOption> productOption = new HashMap();
	//Excel�� �ε��Ͽ� Variant���� �ɼ� �ڵ带 ��� �Ѱ�.
	private HashMap<String, HashMap> variantOptions = new HashMap();
	//Excel���� S:Standard�� ǥ��� �ɼǸ� ���� ����.
	private HashMap<String, HashMap> standardVariantOptions = new HashMap();
	
	private HashMap<String, ManualTreeItem> treeItemMap = new HashMap();
	
	private int finishedCount = 0;
	private boolean isChecked = false;
	private String currentFilePath = null;
	private File exlFile = null;
	private HashMap<String, ArrayList<String>> osiSpec = new HashMap();
	
	// [NON-SR] [20150609] [ymjang] Base Spec IF �� ������ ������ ���� Map ����
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
        this.lblAttachFileMsg.setFont(SWTResourceManager.getFont("���� ���", 13, SWT.BOLD));
        this.lblAttachFileMsg.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        this.lblAttachFileMsg.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 8, 1));
        this.lblAttachFileMsg.setText("�� ��ȣȭ�� ������ �ݵ�� ��ȣ�� �����Ͻ� ��, ����ϼž� �մϴ�. ��");
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
                    // Excel ���� ����, ���õ� Excel Loading
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
	               
	               //Variant�� �����Ͽ� üũ�� ���
	               if( parentItem != null){
	            	   if( item.getChecked()){
	            		   parentItem.setChecked(item.getChecked());
	            	   }
	               }else{
	            	   //Product�� ������ ���
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
        // ���� ��ư Disable
        super.executeButton.setEnabled(false);
        // Excel �˻� ��ư Disable
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
	                        // Txt Upload Report ���� //
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
	                        
	                        // Import Log File ����
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
	 * ����ڰ� ������ Excel�� �ε��ϰ�
	 * ���� ���õ� Product BOM Line ������ Excel�� ���ǵ� Variant�� ����. 
	 * 1. Excel�� ���ǵǾ� �ִ� Variant �������� ������ BOM�� ��� �����Ǿ� �־�� �Ѵ�.
	 * 2. Product�� ���ǵ� ��� �ɼ� ī�װ� �� �ɼ� �ڵ�� Corporate Option Item�� �����ؾ��Ѵ�.
	 */
	public void loadPre() throws Exception {
		if(optionManager != null){
			optionManager.clear(false);
		}
		
		// [SR150408-022] [20150529] [ymjang] Variant/Function EPL ����ǥ�� �������(��� �� ����������Ʈ �ݿ��Ͽ� ����Ҽ� �ֵ���)
		// Merge �� O/Spec �� xlsx �μ� POI �� �а�, ������ O/Spec �� xls �μ� jxl �� ����.
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
		
		//1. Excel�� ���ǵǾ� �ִ� Variant �������� ������ BOM�� ��� �����Ǿ� �־�� �Ѵ�.
		Iterator<String> its = set.iterator();
		while(its.hasNext()){
			String itemId = its.next().toUpperCase();
			if( !bomlineSet.contains(itemId)){
				throw new Exception("not found '" + itemId + "' Variant Item");
			}
		}
		
		//2. Product�� ���ǵ� ��� �ɼ� ī�װ� �� �ɼ� �ڵ�� Corporate Option Item�� �����ؾ��Ѵ�.
		//OptionManager ����
		optionManager = new OptionManager(target, true);
		ArrayList<VariantOption> corpOptionSet = optionManager.getCorpOptionSet();
		HashMap<String, VariantOption> corpOptionMap = optionManager.getCorpOptionMap();
		Iterator<String> corpKeyIts = corpOptionMap.keySet().iterator();
		
		Iterator<String> prdOptionIts = productOption.keySet().iterator();
		while(prdOptionIts.hasNext()){
			String optionName = prdOptionIts.next();
			if( corpOptionMap.keySet().contains(optionName)){
				//Product�� �ɼ� ī�װ��� Corporate Option�� �����Ѵ�.
				VariantOption vOption = productOption.get(optionName);
				VariantOption corpOption = corpOptionMap.get(optionName);
				
				List<VariantValue> values = vOption.getValues();
				List<VariantValue> corpValues = corpOption.getValues();
				
				for( VariantValue val : values){
					if( !corpValues.contains(val)){
						throw new TCException(optionName + ":" + val.getValueName() + " is not found.\nplease register option code " + val.getValueName() + " at the Option Dictionary");
					}
				}
				
				//Coroporate Option�� ���ǵ� �߰����� �ɼ� �ڵ带 Excel���� ������ Option�� �߰���. 
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
        
        // Validation ���� �� �����ư Ȱ��ȭ
        if (validate()){
        	
        }
	}

    public void selectTargetFile()
    {
        
        FileDialog fDialog = new FileDialog(this.shell, SWT.SINGLE);
        fDialog.setFilterNames(new String[] { "Excel File" });
        // *.xls, *.xlsx Filter ����
        fDialog.setFilterExtensions(new String[] { "*.xls*" });
        fDialog.open();
        
        String strfileName = fDialog.getFileName();
        if ((strfileName == null) || (strfileName.equals("")))
            return;
        
        this.strImageRoot = fDialog.getFilterPath();
        list.add(this.strImageRoot + File.separatorChar + strfileName);
        
        // ���õ� Excel File Loading
        // this.load();
        currentFilePath = this.strImageRoot + File.separatorChar + strfileName;
    }
	
	@Override
	public void loadPost() throws Exception {
		super.loadPost();
	}

	/**
	 * [SR150408-022] [20150529] [ymjang] Variant/Function EPL ����ǥ�� �������(��� �� ����������Ʈ �ݿ��Ͽ� ����Ҽ� �ֵ���)
	 * ������ O/Spec �� ���, xls Ÿ������ jxl �� �о�� ��.
	 * @param file
	 * @throws Exception
	 */
	private void excelLoad(File file) throws Exception{
		
		// Excel Load Start
    	Workbook workBook = Workbook.getWorkbook(file);
    	Sheet sheet = workBook.getSheet(0);
    	
    	//all �� ã�� �� �� ���� ������ �����ؾ���. �� 7idx���� �����Ͽ� Eff-IN idx������ Variant�� �о� ����.
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
    	
    	//imp.properties���� ���� �����´�.
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
    		
//    				Standard�ɼǿ� �� �߰�
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
    			
    			//�ɼ� �ڵ忡�� �� 3�ڸ��� �ɼ� ī�װ��̴�.
    			if( tmpCode.length() < 3 ) continue;
    			String optionCategory = tmpCode.substring(0, 3);
    			VariantOption option = options.get(optionCategory);
    			if( option == null ){
    				option = new VariantOption(null, itemId, optionCategory, categoryDescCell.getContents());
    			}
    			
    			Cell cell = sheet.getCell(column, row);
    			if( cell instanceof LabelCell){
    				// [20140113] Label Cell�� ���� value�� ""�� �ƴ� " "�� �������� ���� �߸��� Option value�� �߰��Ǵ� ���� �߻�.
    				// �̸� �ذ��ϱ� ���� Cell ���� trim �ؼ� ���ϴ� ������ ����. ���� null string ���� ������ ���� equals ���� �� ����. 
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
    		
    		//VariantOption�� ��� Option�� Product�� �߰��Ǿ�� �ϸ�,
    		//duplicate �Ͽ� ProductOptio�� �߰���.
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
	 * [SR150408-022] [20150529] [ymjang] Variant/Function EPL ����ǥ�� �������(��� �� ����������Ʈ �ݿ��Ͽ� ����Ҽ� �ֵ���)
	 * Merge �� O/Spec �� ���, xlsx Ÿ������ Poi �� �о�� ��.
	 * @param file
	 * @throws Exception
	 */
	private void mergeExcelLoad(File file) throws Exception{
		
		// Excel Load Start
    	FileInputStream fin = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fin);
        XSSFSheet sheet = workbook.getSheetAt(0);
        
    	//all �� ã�� �� �� ���� ������ �����ؾ���. �� 7idx���� �����Ͽ� Eff-IN idx������ Variant�� �о� ����.
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
    	
    	//imp.properties���� ���� �����´�.
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
    		
    		// Standard�ɼǿ� �� �߰�
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
    			
    			//�ɼ� �ڵ忡�� �� 3�ڸ��� �ɼ� ī�װ��̴�.
    			if( tmpCode.length() < 3 ) continue;
    			String optionCategory = tmpCode.substring(0, 3);
    			VariantOption option = options.get(optionCategory);
    			if( option == null ){
    				option = new VariantOption(null, itemId, optionCategory, categoryDescCell.getStringCellValue());
    			}
    			
    			org.apache.poi.ss.usermodel.Cell cell = sheet.getRow(row).getCell(column);
				// [20140113] Label Cell�� ���� value�� ""�� �ƴ� " "�� �������� ���� �߸��� Option value�� �߰��Ǵ� ���� �߻�.
				// �̸� �ذ��ϱ� ���� Cell ���� trim �ؼ� ���ϴ� ������ ����. ���� null string ���� ������ ���� equals ���� �� ����. 
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
    		
    		//VariantOption�� ��� Option�� Product�� �߰��Ǿ�� �ϸ�,
    		//duplicate �Ͽ� ProductOptio�� �߰���.
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
	 * Excel���� �ε��� �ɼ� �ڵ带 Product�� Variant�� �����Ѵ�.
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
		
		//Product�� ���õ��� ������ ����.
		if( !isChecked ){
			throw new TCException("Please check a Variant.");
		}
		
		ArrayList<VariantOption> corpOptionSet = optionManager.getCorpOptionSet();
		
		Vector<String[]> userDefineErrorList = new Vector();
		Vector<String[]> moduleConstratintsList = new Vector();
		HashMap<String, VariantOption> optionMap = new HashMap();
		
		
		super.syncItemState(treeItemMap.get(product_no), super.STATUS_INPROGRESS, "���� ������ �ɼ� ������ �����ɴϴ�.");
		super.syncSetItemText(treeItemMap.get(product_no), 4, "���� ������ �ɼ� ������ �����ɴϴ�.");
		super.syncSetItemTextField("[" + product_no + "]���� ������ �ɼ� ������ �����ɴϴ�.");
		//target(Product)�� ������ �ɼ� ���� �����´�.
		ArrayList<VariantOption> optionSet = optionManager.getOptionSet(target, optionMap, userDefineErrorList, moduleConstratintsList);
		
		super.syncSetItemText(treeItemMap.get(product_no), 4, "�ɼ� ������ �����մϴ�.");
		//1. Excel���� Import�� Option�� Product�� �����ϴ��� Ȯ���ϰ�, Product�� Excel�� ���ǵ� �ɼ��� �����Ѵ�.
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
    		//BOMline���� ������ Option������ �߰ߵǸ�, �������� ���̽�
    		if( optionSet.contains(option)){
    			
    			VariantOption curOption = optionMap.get(option.getOptionName());
    			List<VariantValue> curList = curOption.getValues();
    			
    			//���� ������ Option code map
    			HashMap<String, VariantValue> curValueMap = curOption.getValueMap();
    			
    			for( VariantValue value : list){
    				
    				VariantValue curVal = curValueMap.get(value.getValueName()); 
    				boolean bFound = curVal == null ? false:true;
    				ArrayList<TCComponentBOMLine> corporateBOMLines = optionManager.getCorporateBOMLines();
    				
    				if( bFound ){
    					super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + value.getValueName() + " �� �̹� �߰��Ǿ� �ֽ��ϴ�.");
    				}else{
    					//�ɼ��� ���� �Ǿ� ������, �ɼ� �ڵ�� ���� �Ǿ� ���� ����.
        				//Category�� ������ �ɼ��ڵ带 �߰��ؾ��Ѵ�.
        				//Product�� ������ �ɼ��� ���  Corporate Option Item�� ���ǵ� �ɼ��̹Ƿ�
        				//Corporate Option Item�� ���ǵ� �ɼ��� ã��, �� �ɼǿ� ���ο� �ɼ� �ڵ带 �߰��ؾ��Ѵ�.
    					System.out.println("" + value.getValueName() + "�� �߰��Ǿ�� ��");
    					
    					if( corpOptionSet.contains(option)){
    						
    						super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + value.getValueName() + " �ɼ��ڵ带 �߰��մϴ�.");
    						
    						//������ BOM Line���� ������ �ɼ� �ڵ�� + �������� ������ �ڵ带 �߰�.
    						VariantValue curValue = curOption.getValue(value.getValueName());
    						if( curValue == null){
    							curOption.addValue(value);
    						}
    						
    						if( corporateBOMLines != null && corporateBOMLines.size() > 0){
    							
    							//Corporate Option Map���� �ش��ϴ� �ɼ��� �����´�.
    							HashMap<String, VariantOption> corpOptionMap =  optionManager.getCorpOptionMap();
    							VariantOption corpOption = corpOptionMap.get(curOption.getOptionName());
    							String mvlStr = OptionManager.getCorpOptionString(curOption);
    							
    							TCComponentBOMLine corpDictionaryLine = corporateBOMLines.get(0);
    							corpDictionaryLine.refresh();
    							//Corporate Option Item�� �ɼǿ� �ɼ� �ڵ带 �߰��� �����Ѵ�.
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
    			//BOMLine���� �߰��� �� ���� �ɼ��̸� Corporate Option���� �˻��ϰ�, �ű⿡ ������
    			//�� �ɼ��� Product�� ����, 
    			//������ ���� �����Ͽ� Corporate Option Item�� �߰��ϰ�, Product���� �߰��Ѵ�.
    			System.out.println("optionSet���� ã�� �� ���� �ɼǸ� : " + option.getOptionName());
    			HashMap<String, VariantOption> corpOptionMap =  optionManager.getCorpOptionMap();
				VariantOption corpOption = corpOptionMap.get(option.getOptionName());
				
				//Corporate Option���� ã�� �� �����Ƿ�
				//�ɼ��� ���� �� Corporate OPtion Item�� �߰��ϰ�
				//Product���� �߰��ؾ���.
				if( corpOption == null ){
					throw new TCException("not found the option at the Corporate Option Item");
				}else{
					super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + corpOption.getOptionName() + " �ɼ��� �߰��մϴ�.");
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
    	super.syncItemState(treeItemMap.get(product_no), super.STATUS_COMPLETED, "�ɼ� ���� ������ �Ϸ��Ͽ����ϴ�.");
    	super.syncSetItemText(treeItemMap.get(product_no), 4, "�ɼ� ���� ������ �Ϸ��Ͽ����ϴ�.");
    	super.syncSetItemTextField("[" + product_no +  "] ���� ���� �� ���ΰ�ħ ��....");
    	target.window().save();
//    	target.window().refresh();
    	target.refresh();
    	
    	//������ ������̾��� OptionCode�� �������� ����.
    	Collection<VariantOption> curCollection = optionMap.values();
    	Iterator<VariantOption> curIts =  curCollection.iterator();
    	while(curIts.hasNext()){
    		VariantOption curOption = curIts.next();
    		List<VariantValue> curValues = curOption.getValues();
    		VariantOption option = productOption.get(curOption.getOptionName());
    		VariantValue tmpValue;
    		
    		if( option == null){
    			
    			//���� Product�� ������ �ɼǰ����� ������� �����.
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
    			//���� Product�� ����� �ƴ� �ɼǰ��� ������� ����. 
    			for( VariantValue curValue : curValues){
        			VariantValue value = option.getValue(curValue.getValueName());
        			
        			//Excel import���� �߰ߵǾ�����
        			if( value == null ){
        				//Excel���� �߰ߵǸ� ������ ������� ����.
        				tmpValue = new VariantValue(option, curValue.getValueName(), curValue.getValueDesc(), VariantValue.VALUE_USE, false);
        				option.addValue(tmpValue);
        			}
        		}
    		}
    		
    	}
    	
    	//1-1. Excel���� ������� �ʴ� �ɼ��ڵ�� ��ȿ�� �˻縦 �߰��Ѵ�.
    	super.syncSetItemText(treeItemMap.get(product_no), 4, "��ȿ�� �˻� �׸��� �߰��մϴ�.");
    	super.syncSetItemTextField("[" + product_no + "] ��ȿ�� �˻� �׸��� �߰��մϴ�.");
    	
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
    		//BOMline���� ������ Option������ �߰ߵǸ�, �������� ���̽�
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
					
					super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + condition.option + ":" + condition.value + "�� ��ȿ�� �˻� �׸����� �߰��մϴ�.");
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
			        
			        super.syncSetItemTextField("[" + product_no + "][" + curNum + "/" + totalOptionCount + "] " + condition.option + ":" + condition.value + "�� ��ȿ�� �˻� �׸����� �߰��մϴ�.");
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
    		super.syncSetItemText(treeItemMap.get(product_no), 4, "��� �ɼ� ������ �Ϸ��Ͽ����ϴ�.");
    		super.syncSetItemTextField("[" + product_no +  "] ���� ���� �� ���ΰ�ħ ��....");
        	target.window().save();
//        	target.window().refresh();
        	target.refresh();
        }
    	
    	//2.Variant Item�� �ش� �ɼ��� �����ϰ� ����ϴ� �ɼ��ڵ� ���� �ڵ�� constraint�� �߰�.
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
		
		//OSI ������ DB������ ����
		//deleteOsiInfo();
		//OSI ������ DB�� ����.
		insertOsiInfo();
		
		// [SR����][20150910][jclee] OSpec Trim ������ DB�� ����
		OSpec ospec = OpUtil.getOSpec(exlFile);
		insertOSpecTrim(ospec);
	}

	@Override
	public void load() throws Exception {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					
					shell.setCursor(waitCursor);
					
					// Load ��ó��
					loadPre();
					// ManualTreeItem List
					itemList = new ArrayList<ManualTreeItem>();
					// TCComponentItemRevision List
					tcItemRevSet = new HashMap<String, TCComponentItemRevision>();
					
					tree.removeAll();
					// Header ���� Loading
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
					// Load ��ó��
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
		String[] szClass = new String[] { "Item", "Item", "Revision", "Item" };
		String[] szAttr = new String[] { "Level", "item_id",
				"item_revision_id", "object_name" };
		this.headerModel = new BWItemModel();
		// Type ���� (BOM)
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
	 * BomLine,Item,ItemRevision,Dataset �Ӽ������� ManualTreeItem�� ����
	 * 
	 * @param bomLine
	 *            : ��� BomLine
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

				// Top Line�� ���, Level(Tree����) ������� �ʴ� ���
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

		//[20131025] �ɼ� import �ӵ� ����.
		if( bomLine.getItem().getType().equals(TcDefinition.VARIANT_ITEM_TYPE)){
			return;
		}
		TCComponent[] children = bomLine.getRelatedComponents("bl_child_lines");
		
		for (TCComponent child : children) {
			TCComponentBOMLine childBOMLine = (TCComponentBOMLine) child;
			//[SR190920-033][CSH] Obsolete�� Variant�� ����Ʈ���� ����
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
	 * ���� Report�� ����, OSI ������ DB�� ������.
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
			// [NON-SR] [20150609] [ymjang] Base Spec IF �� ������ ������ ���� Map ����
			ds.put("BASE_SPEC", baseSpec);
			
			// [NON-SR] [20160825] [ymjang] ������ Vaiant �� �� �ɼǺ��� ���ε�ð��� ���ݾ� Ʋ���� HBOM���� �ֽ� O/Spec ������ ���� �� ���� ����.
			DateFormat dtf = new SimpleDateFormat("yyyyMMddHHmmss"); 
	        String today = dtf.format(new Date());
			ds.put("CRE_DATE", today);
			ds.put("UPD_DATE", today);
			
			// [NON-SR] [20150430] ymjang, IF_OSPEC_MASTER_FROM_HBOM �� insert �ÿ� Variant Option �Ϻ� ���� �߻�
			// ������ �����͸� �����ϱ� ���Ͽ� �α׸� ������.
			Set osiSpecKeys = osiSpec.keySet();
			Iterator<String> osiSpecs = osiSpecKeys.iterator();
			while(osiSpecs.hasNext()){
				String variantID = osiSpecs.next();
				ArrayList<String> values = osiSpec.get(variantID);
				
				for( String value : values){
					BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantID + ", " + osi_no + "] " + value + " inserted.");
				}
			}
			
			// [NON-SR] [20150609] [ymjang] Base Spec IF �� ������ ������ ���� Map ����
			// ������ �����͸� �����ϱ� ���Ͽ� �α׸� ������.
			Set baseSpecKeys = baseSpec.keySet();
			Iterator<String> baseSpecs = baseSpecKeys.iterator();
			while(baseSpecs.hasNext()){
				String variantID = baseSpecs.next();
				ArrayList<String> values = baseSpec.get(variantID);
				
				for( String value : values){
					BWVariantOptionImpDialog.super.syncSetItemTextField("BASE SPEC [" + variantID + ", " + osi_no + "] " + value + " inserted.");
				}
			}
			
			// [NON-SR] [20150609] ymjang, ��Ÿ ��� ����
			// com.kgm.serv1ice.VariantService --> com.kgm.service.VariantService
			remote.execute("com.kgm.service.VariantService", "insertOsiInfo", ds);
		}
	}
	
	/**
	 * OSpec Trim ������ DB�� ����
	 * @param ospec
	 */
	private void insertOSpecTrim(OSpec ospec) throws Exception {
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		
		String sOSINo = ospec.getOspecNo();
		String sProjectCode = ospec.getProject();
		
		// ������ ������ OSI No�� �� ������ ��� Delete.
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
	 * Trim ������ �ش� Category�� S�� ���� Value�� Return
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
	 * Excel���� �ɼ� ������ �ε��Ͽ� Variant BOM line�� �ɼ��� �����Ѵ�.
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
			
			BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "] ���� ������ �ɼ� ������ �����ɴϴ�.");
			BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_INPROGRESS, "���� ������ �ɼ� ������ �����ɴϴ�.");
			HashMap<String, VariantOption> variantMap = variantOptions.get(variantId.toUpperCase());
			if( variantMap == null) {
				BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantFullId), 4, "Skip");
				BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "]import���Ͽ��� ã�� �� �����Ƿ�, Skip�մϴ�.");
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
		    		//BOMline���� ������ Option������ �߰ߵǸ�, �������� ���̽�
		    		if( optionSet.contains(option)){
		    			VariantOption curOption = optionMap.get(option.getOptionName());
		    			
		    			//���� BOMLIne�� ������ Option code map
		    			HashMap<String, VariantValue> curValueMap = curOption.getValueMap();
		    			
		    			for( VariantValue value : list){
		    				
		    				VariantValue curVal = curValueMap.get(value.getValueName()); 
		    				boolean bFound = curVal == null ? false:true;

		    				//Osi Spec���� �߰��� �ɼ��ڵ带 ����.
		    				variantSpecs.add(value.getValueName());
		    				if( bFound ){
		    					BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + value.getValueName() + " �� �̹� �߰��Ǿ� �ֽ��ϴ�.");
		    				}else{
		    					BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + value.getValueName() + " �ɼ��ڵ带 �߰��մϴ�.");
	    						
	    						//������ BOM Line���� ������ �ɼ� �ڵ�� + �������� ������ �ڵ带 �߰�.
		    					VariantValue curValue = curOption.getValue(value.getValueName());
	    						if( curValue == null){
	    							curOption.addValue(value);
	    						}
	    						
	    						//OSI_NO�� Varaint_NO, Option_VAL�� TC DB�� ������.
	    						
	    						
			    				//�ɼ��� ���� �Ǿ� ������, �ɼ� �ڵ�� ���� �Ǿ� ���� ����.
			    				//Category�� ������ �ɼ��ڵ带 �߰��ؾ��Ѵ�.
			    				//Product�� ������ �ɼ��� ���  Corporate Option Item�� ���ǵ� �ɼ��̹Ƿ�
			    				//Corporate Option Item�� ���ǵ� �ɼ��� ã��, �� �ɼǿ� ���ο� �ɼ� �ڵ带 �߰��ؾ��Ѵ�.
			    				
			    				//Corporate Option Map���� �ش��ϴ� �ɼ��� �����´�.
			    				//�ɼ��ڵ��� ������ Product���� �̹� üũ�Ϸ��Ͽ�, ���̻�  Ȯ������ ����. 
								HashMap<String, VariantOption> corpOptionMap =  optionManager.getCorpOptionMap();
								VariantOption corpOption = corpOptionMap.get(curOption.getOptionName());
								
								String mvlStr = OptionManager.getOptionString(corpOption);
								
								try{
									//Corporate Option Item�� �ɼǿ� �ɼ� �ڵ带 �߰��� �����Ѵ�.
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
		    			
						// [NON-SR] [20150430] ymjang, IF_OSPEC_MASTER_FROM_HBOM �� insert �ÿ� Variant Option �Ϻ� ���� �߻�
		    			// if_ospec_master_from_hbom table insert �� ���� variantSpecs �� �ش� �ɼǰ��� �־���.
						for( VariantValue value : list){
		    				//OSI Spec���� �߰��� �ɼ��ڵ带 ����.
		    				variantSpecs.add(value.getValueName());
						}
		    			
		    			//Product�� ���ǵ� �ɼ��� �̹� Corporate Option�������� �����Ѵٰ� ������.
		    			//�̹� ������ üũ �Ϸ�.
		    			BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + option.getOptionName() + " �ɼ��� �߰��մϴ�.");
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
		    	
		    	//osiSpec�� ������.
		    	osiSpec.put(variantId, variantSpecs);
		    	
		    	// [NON-SR] [20150609] [ymjang] Base Spec IF �� ������ ������ ���� Map ����
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
					
					//baseSpec�� ������.
					baseSpec.put(variantId, variantBaseSpecs);
				}	
				
		    	
		    	BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_INPROGRESS, "�ɼ� ���� ������ �Ϸ��Ͽ����ϴ�.");
		    	BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantFullId), 4, "�ɼ� ���� ������ �Ϸ��Ͽ����ϴ�.");
			}catch( Exception e){
				
				//osi Spec������Ʈ�ÿ� Osi_no�� Variant������ ������ �����ϹǷ�.
				//������ �߻��Ͽ� osiSpec�� ���Ե��� ���� ���� osi spec�� ���ŵ��� �ʴ´�. 
				osiSpec.remove(variantId);
				BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_ERROR, "�ɼ� ���� ���� �� ������ �߻��Ǿ����ϴ�.");
			}finally{
				childLine.window().save();
//				childLine.window().refresh();
				childLine.refresh();
			}
			
			
			
			//������ ������̾��� OptionCode�� �������� ����.
	    	Collection curCollection = optionMap.values();
	    	Iterator<VariantOption> curIts =  curCollection.iterator();
	    	while(curIts.hasNext()){
	    		VariantOption curOption = curIts.next();
	    		List<VariantValue> curValues = curOption.getValues();
	    		
//	    		Excel���� import�� Option
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
		    			//import �� Excel���� ���� �ɼ��̹Ƿ� ���
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
							
							BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + condition.option + ":" + condition.value + "�� ��ȿ�� �˻� �׸����� �߰��մϴ�.");
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
					        
					        BWVariantOptionImpDialog.super.syncSetItemTextField("[" + variantId + "][" + curNum + "/" + totalOptionCount + "] " + condition.option + ":" + condition.value + "�� ��ȿ�� �˻� �׸����� �߰��մϴ�.");
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
	            	//������ ������ �������� ������ Exception�� �߻��ϴ� ��찡 �־.
	            	//������ ���� ��쿡�� ���� �߻�.
//	            	if( errorCodes == 51003){
//	            		osiSpec.remove(variantId);
//	            		throw tce;
//	            	}
	            	
	            }finally{
	            	childLine.window().save();
//	            	childLine.window().refresh();
	            	childLine.refresh();
	            }
	            
//	            DB�� O-Spec������ �Է��ϱ����� Ű���� TC�� Variant ������ ���̵�� �����Ѵ�.
	            ArrayList<String> tmpList = osiSpec.get(variantId);
	            osiSpec.remove(variantId);
	            osiSpec.put(variantFullId, tmpList);
		    	
	            // [NON-SR] [20150609] [ymjang] Base Spec IF �� ������ ������ ���� Map ����
	            ArrayList<String> tmpBspecList = baseSpec.get(variantId);
	            baseSpec.remove(variantId);
	            baseSpec.put(variantFullId, tmpBspecList);
	            
		    	BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_COMPLETED, "��ȿ�� �˻� �߰��� �Ϸ��Ͽ����ϴ�.");
		    	BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantFullId), 4, "��ȿ�� �˻� �߰��� �Ϸ��Ͽ����ϴ�.");
		    	
		    	BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_COMPLETED, "Stored Option Set�� �����մϴ�.���а��� �ҿ� �� �� �ֽ��ϴ�.");
		    	createStoredOptionSet(childLine);
		    	BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantFullId), 4, "�Ϸ�Ǿ����ϴ�.");
		    	BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantFullId), BWVariantOptionImpDialog.super.STATUS_COMPLETED, "�Ϸ�Ǿ����ϴ�.");
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
	 * Stored Option Set�� �����ϰ� ������.
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
						BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantId), BWVariantOptionImpDialog.super.STATUS_WARNING, "�������̹Ƿ� ���� �� �� �����ϴ�. NewStuff�� �̵��մϴ�.");
				    	BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantId), 4, "�������̹Ƿ� ���� �� �� �����ϴ�. NewStuff�� �̵��մϴ�.");
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
