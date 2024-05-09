package com.symc.plm.me.sdv.operation.accountabilitycheck;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.WaitProgressBar;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.rac.cme.biw.apa.search.FindConnectedPartUtility;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.accountabilitycheck.Activator;
import com.teamcenter.rac.cme.accountabilitycheck.extensions.IAccountabilityCheckResultModel;
import com.teamcenter.rac.cme.accountabilitycheck.services.IAccountabilityCheckResultService;
import com.teamcenter.rac.cme.accountabilitycheck.services.impl.AccountabilityCheckResultServiceImpl;
import com.teamcenter.rac.cme.accountabilitycheck.util.AccResult;
import com.teamcenter.rac.cme.accountabilitycheck.util.AccUtil;
import com.teamcenter.rac.cme.accountabilitycheck.views.AccountabilityCheckResultHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.exceptions.NotLoadedException;

/**
 *  * Accountability Check ����� Excel�� ��� �� �� �ֵ��� ����� �߰����ּ���.
 * [SR150106-015] 20151028 taeku.jeong	����ȭ ������� �䱸�� �߰��� SR�� ���� ����
 * 
 * @author Taeku
 *
 */
public class AccountabilityCheckResultExport {
	
	private TCSession session;
	
	private AccResult accUtilListForSourceModel[];
	private AccResult accUtilListForTargetModel[];
	private Hashtable<AccResult, RGB> accResulRGBtHash;
	private Hashtable<AccResult, String> accResulNameHash; 
	
	private TCComponentBOMWindow srcWindow = null;
	private String srcTopNodeItemType = null;
	private String srcTopNodeItemId = null;
	private boolean srcIsBOP = false;
	private String bopType = null;
	
	private TCComponentBOMWindow targetWindow = null;
	private String targetTopNodeItemType = null;
	private String targetTopNodeItemId = null;
	private boolean targetIsBOP = false;
	
	private String exportTargetDir = null;
	private String exportTargetFilePath = null;
	
	private String[] keyIndexList = new String[]{
			"Function Id","Function Rev",
			"FMP Id","FMP Rev",
			"Parent Id","Parent Rev","Parent Name",
			"",
			"EndItem Id","EndItem Rev","EndItem Name","EndItem Type",
			"Match Type", "Supply Mode", "Feature Name","EndItem Status",
			"",
			"Operation Id","Operation Rev","Operation Name",
			"Station Id","Station Rev","Station Name",
			"Line Id","Line Rev","Line Name"
		};
	
	private AccountabilityCheckResultServiceImpl localAccountabilityCheckResultServiceImpl;
	
	private AccountabilityDataExcelExporter accountabilityDataExcelExporter = null;
	
			
	public AccountabilityCheckResultExport(TCSession session){
		this.session = session;
	}
	
	public void runExportAction() throws Exception{
		
/*
		TCComponentBOMWindow a = null;
		// Given an AbsOccUid, search in the bom window for bom lines associated with this appearance.
		a.findAppearance(java.lang.String AbsOccUid);
		a.findAppearance(java.lang.String AbsOccUid, boolean searchAllContexts, boolean byAbsOccID);
		// Given an abs occ ID, search in the bom window for all bom lines associated with this abs occ ID.
		a.findConfigedBOMLinesForAbsOccID(java.lang.String absOccID, boolean searchAllContexts, TCComponent contextLine);
		// Given an appearance or absocc, search in the bom window for bom lines associated with this appearance.
		a.findConfigedBOMLinesForAbsOccOrAPN(TCComponent appr, boolean byAbsOccID, boolean searchAllContexts, TCComponent contextLine);
*/
		
		// Report�� Source�� ���õ� ����� ���� Match Type�� ���� �Ѵ�. 
		this.accUtilListForSourceModel = new AccResult[] {
				AccResult.MISSING_TARGET, AccResult.PARTIAL_MATCH,
				AccResult.MULTIPLE_PARTIAL_TARGET, AccResult.FULL_MATCH,
				AccResult.MULTIPLE_TARGET,
		};
		
		// Report�� Target�� ���õ� ����� ���� Match Type�� ���� �Ѵ�.
		this.accUtilListForTargetModel = new AccResult[] {
				AccResult.MISSING_SOURCE
		};
		 
		// Match Type ���ǿ� ���� �����Ѵ�.
		initAccResultHashs();

		// Accountability Check Result ���� �ʱ�ȭ
		initResultInformation();

		// Result�� List up �Ǵ��� Ȯ�� �Ѵ�.
		this.accountabilityDataExcelExporter = null;
		try {
			accountabilityCheckResultWrite();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally{
			//progressBar.dispose();
		}

	}
	
    private void initAccResultHashs(){
		
		accResulRGBtHash = new Hashtable<AccResult, RGB>();
		accResulNameHash = new Hashtable<AccResult, String>();
		
		AccResult missingTarget = AccResult.MISSING_TARGET;
		RGB missingTargetRGB = new RGB(missingTarget.getResultColor().getRed(), missingTarget.getResultColor().getGreen(), missingTarget.getResultColor().getBlue());
		accResulRGBtHash.put(missingTarget, missingTargetRGB);
		accResulNameHash.put(missingTarget, "Missing Target");
		
		AccResult partialMatch = AccResult.PARTIAL_MATCH;
		RGB partialMatchRGB = new RGB(partialMatch.getResultColor().getRed(), partialMatch.getResultColor().getGreen(), partialMatch.getResultColor().getBlue());
		accResulRGBtHash.put(partialMatch, partialMatchRGB);
		accResulNameHash.put(partialMatch, "Partial Match");
	
		AccResult missingSource = AccResult.MISSING_SOURCE;
		RGB missingSourceRGB = new RGB(missingSource.getResultColor().getRed(), missingSource.getResultColor().getGreen(), missingSource.getResultColor().getBlue());
		accResulRGBtHash.put(missingSource, missingSourceRGB);
		accResulNameHash.put(missingSource, "Missing Source");
		
		AccResult multipleTarget = AccResult.MULTIPLE_TARGET;
		RGB multipleTargetRGB = new RGB(multipleTarget.getResultColor().getRed(), multipleTarget.getResultColor().getGreen(), multipleTarget.getResultColor().getBlue());
		accResulRGBtHash.put(multipleTarget, multipleTargetRGB);
		accResulNameHash.put(multipleTarget, "Multiple Target");
		
		AccResult fullMatch = AccResult.FULL_MATCH;
		RGB fullMatchRGB = new RGB(fullMatch.getResultColor().getRed(), fullMatch.getResultColor().getGreen(), fullMatch.getResultColor().getBlue());
		accResulRGBtHash.put(fullMatch, fullMatchRGB);
		accResulNameHash.put(fullMatch, "Full Match");
		
		AccResult multiplePartialTarget = AccResult.MULTIPLE_PARTIAL_TARGET;
		RGB multiplePartialTargetRGB = new RGB(multiplePartialTarget.getResultColor().getRed(), multiplePartialTarget.getResultColor().getGreen(), multiplePartialTarget.getResultColor().getBlue());
		accResulRGBtHash.put(multiplePartialTarget, multiplePartialTargetRGB);
		accResulNameHash.put(multiplePartialTarget, "Multiple Partial Target");
		
	}

	private void initResultInformation(){
	
		// Check Result Service
		this.localAccountabilityCheckResultServiceImpl = 
			(AccountabilityCheckResultServiceImpl) OSGIUtil.getService(
				Activator.getDefault(),
				IAccountabilityCheckResultService.class.getName()
			);
	}

	private void accountabilityCheckResultWrite() throws Exception{
    	
    	if(localAccountabilityCheckResultServiceImpl == null){
    		System.out.println("[" + new Date() + "]" + "Result service implement is null.");
    		throw new Exception("Result service implement is null.");
    	}
    	
    	// Check Result Service
    	boolean accountabilityCheckDone = localAccountabilityCheckResultServiceImpl.isAccountabilityCheckDone();
    	
    	if(accountabilityCheckDone == false){
    		System.out.println("[" + new Date() + "]" + "The accountability check must be completed.");
    		throw new Exception("The accountability check must be completed.");
    	}
    	
    	this.srcWindow = null;
    	this.targetWindow = null;
    	
    	// Soruce Model
    	IAccountabilityCheckResultModel sourceResultModel = 
    			localAccountabilityCheckResultServiceImpl.getSourceResultModel();
    	if(sourceResultModel != null){
    		
    		System.out.println("[" + new Date() + "] // Source Model ");
    		
    		List<TCComponent> srcScop = sourceResultModel.getInputModel().getScopeModel().getScopeComponents();
    		for (int i = 0;srcScop!=null && i < srcScop.size(); i++) {
    			System.out.println("srcScop["+i+"] = "+srcScop.get(i));
    			
				if(srcScop.get(i)!=null && srcScop.get(i) instanceof TCComponentBOMLine){
					TCComponentBOMLine tempBOMLine = (TCComponentBOMLine)srcScop.get(i);
					
					if(this.srcWindow==null && tempBOMLine!=null){
						try {
							if(tempBOMLine.window()!=null){
								srcWindow = tempBOMLine.window();
							}
						} catch (TCException e) {
							e.printStackTrace();
						}
					}
				}
				
			}
    	
    	}
    	
    	// Target Model
    	IAccountabilityCheckResultModel targetResultModel = 
    			localAccountabilityCheckResultServiceImpl.getTargetResultModel();
    	if(targetResultModel != null){
    		System.out.println("[" + new Date() + "] // Target Model ");
    		
    		List<TCComponent> targetScop = targetResultModel.getInputModel().getScopeModel().getScopeComponents();
    		for (int i = 0;targetScop!=null && i < targetScop.size(); i++) {
    			System.out.println("targetScop["+i+"] = "+targetScop.get(i));
    			if(targetScop.get(i)!=null && targetScop.get(i) instanceof TCComponentBOMLine){
    				TCComponentBOMLine tempBOMLine = (TCComponentBOMLine)targetScop.get(i);
    				
    				if(this.targetWindow==null && tempBOMLine!=null){
    					try {
    						if(tempBOMLine.window()!=null){
    							targetWindow = tempBOMLine.window();
    						}
    					} catch (TCException e) {
    						e.printStackTrace();
    					}
    				}
    			}
    			
			}
    		
    	}
    	
    	
    	// ���õ� Source�� Target BOMTree�� ã�ƾ� �Ѵ�.
    	initBopType();
    	
    	initExportTargetFilePath();
    	if(this.exportTargetFilePath==null || (this.exportTargetFilePath!=null && this.exportTargetFilePath.trim().length()<1)){
    		this.exportTargetFilePath = "c:\\AccountabilityCheckReport.xlsx";
    	}
		
		try {
			accountabilityDataExcelExporter =new AccountabilityDataExcelExporter();
			accountabilityDataExcelExporter.readyFile(this.exportTargetFilePath, this.session );
			
			System.out.println("Print source model data");
			
			// ����� �ش��ϴ� ������ Excel�� ����� ��� ���뿡�� �ؾ� �ɰ� ������..
			excelTitleOutPut();
			
			
			if(sourceResultModel!=null){
				getPrintableDataFromSourceModel(sourceResultModel);
			}

			System.out.println("Print tareget model data");
			
			if(targetResultModel!=null){
				getPrintableDataFromTargetModel(targetResultModel);
			}
			System.out.println("[" + new Date() + "]" + "The result is saved in a file "+this.exportTargetFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
			
		}finally{
			accountabilityDataExcelExporter.closeWorkSheet();
			System.out.println("Cloase report excel file");
		}
    	 
    }
	
	private void excelTitleOutPut(){
		
		System.out.println("(B)this.srcWindow = "+this.srcWindow);
		System.out.println("(B)this.targetWindow = "+this.targetWindow);
		
    	if(targetIsBOP==true){
    		// ���õ� �ҽ��� MProduct�� BOMLine�� �����.
    		accountabilityDataExcelExporter.printSrcAndTargetInfomation(this.srcWindow, this.targetWindow);
    	}else{
    		// ���õ� �ҽ��� BOP�� BOMLine�� �����
    		accountabilityDataExcelExporter.printSrcAndTargetInfomation(this.targetWindow, this.srcWindow);
    	}
	}
	
	private void initExportTargetFilePath(){
		
		this.exportTargetDir = Utilities.getCookie("BOPCustomCookie", "Accountability.Report.Dir", true);
		
		if(this.exportTargetDir==null || (this.exportTargetDir!=null && this.exportTargetDir.trim().length()<1)){
			this.exportTargetDir = System.getProperty ( "user.home" );
		}
		exportTargetFilePath = null;
		
		final Shell shell = AIFUtility.getActiveDesktop().getShell();
		shell.getDisplay().syncExec(new Runnable() {
			
			public void run()
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setText("Save");
				fd.setFilterPath(exportTargetDir);
				fd.setFileName("AccountabilityCheckReport");
				String[] filterExt = { "*.xlsx", "*.*" };
				fd.setFilterExtensions(filterExt);
				exportTargetFilePath = fd.open();				
			}
		});
		
		if(exportTargetFilePath!=null && exportTargetFilePath.trim().length()>0){
			FilePathPars filepathpars = null;
			try {
				filepathpars = new FilePathPars(exportTargetFilePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String tempExportTargetDir = filepathpars.getStrFileDir();
			if(tempExportTargetDir!=null && tempExportTargetDir.trim().length()>0){
				if(tempExportTargetDir.trim().equalsIgnoreCase(exportTargetDir)==false){

					Utilities.setCookie("BOPCustomCookie", true, "Accountability.Report.Dir", tempExportTargetDir.trim());
					
				}
			}
		}
		
	}
    
    private void initBopType(){
		
		if(targetWindow!=null){
			
			TCComponentBOMLine topBOMLine = null;
			try {
				topBOMLine = targetWindow.getTopBOMLine();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			
			try {
				targetTopNodeItemType = topBOMLine.getItem().getType();
				targetTopNodeItemId = topBOMLine.getProperty("bl_item_item_id");
			} catch (TCException e) {
				e.printStackTrace();
			}
			
			System.out.println("targetTopNodeItemType = "+targetTopNodeItemType);
			
			if(targetTopNodeItemType!=null && targetTopNodeItemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)){
				targetIsBOP = true;
				srcIsBOP = false;
				if(targetTopNodeItemId!=null && targetTopNodeItemId.trim().length()>=5){
					bopType = ""+targetTopNodeItemId.trim().charAt(4);
				}
			}
		}
		
		if(srcWindow!=null){
			
			TCComponentBOMLine topBOMLine = null;
			try {
				topBOMLine = srcWindow.getTopBOMLine();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			
			try {
				srcTopNodeItemType = topBOMLine.getItem().getType();
				srcTopNodeItemId = topBOMLine.getProperty("bl_item_item_id");
			} catch (TCException e) {
				e.printStackTrace();
			}
			
			System.out.println("srcTopNodeItemType = "+srcTopNodeItemType);
			
			if(srcTopNodeItemType!=null && srcTopNodeItemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)){
				srcIsBOP = true;
				targetIsBOP = false;
				if(srcTopNodeItemId!=null && srcTopNodeItemId.trim().length()>=5){
					bopType = ""+srcTopNodeItemId.trim().charAt(4);
				}
			}
		}
		
		
	}

	/**
     * Accountability Check���� 
     * Missing Target �� Full Match, Partial Match, Multiple Target, Full Match, Multiple Partial Target�� ǥ���ϸ� ���ڴ�.
     * Missing Source�� ������ Report�� �����ϴ�.
     * @param resultModel
     */
    private void getPrintableDataFromSourceModel(IAccountabilityCheckResultModel resultModel){
    	
    	AccUtil aAccUtil = new AccUtil();
    	Map<RGB, List<TCComponent>> resultModelHashMap = aAccUtil.getColoredComponents(resultModel);
    	
    	for (int i = 0; accUtilListForSourceModel!=null && i < accUtilListForSourceModel.length; i++) {
    		AccResult currentMatchTypedResult = accUtilListForSourceModel[i];
    		String matchTypeName = this.accResulNameHash.get(currentMatchTypedResult);
    		dataCheckUsingTextBaseOutput(resultModel, resultModelHashMap, currentMatchTypedResult, matchTypeName, false);
		}
    	
    	// ClosureRuleTablePanel
    	// TCComponentClosureRuleType

    }

    /**
     * Accountability Check���� 
     * Missing Source�� ǥ���ϸ� ���ڴ�.
     * Missing Target�� ������ Report�� ���������� �ٸ� �͵��� Source�� �������� �����Ǵ°��� �ϰ����� ���������� ���δ�.
     * @param resultModel
     */
    private void getPrintableDataFromTargetModel(IAccountabilityCheckResultModel resultModel){
    	
    	AccUtil aAccUtil = new AccUtil();
    	Map<RGB, List<TCComponent>> resultModelHashMap = aAccUtil.getColoredComponents(resultModel);
    	
    	for (int i = 0; accUtilListForTargetModel!=null && i < accUtilListForTargetModel.length; i++) {
    		AccResult currentMatchTypedResult = accUtilListForTargetModel[i];
    		String matchTypeName = this.accResulNameHash.get(currentMatchTypedResult);
    		dataCheckUsingTextBaseOutput(resultModel, resultModelHashMap, currentMatchTypedResult, matchTypeName, true);
		}
    }
    
    /**
     * Accountability Check  ����� Match Type���� �����ؼ�  Report ��¿� �ݿ� �ϵ��� Data�� �غ��ϴ� Function
     * Missing Source, Full Match, Missing Target, Multiple Target ���� Match Type�� �ش��ϴ� Data ���� ������
     * �񱳵� ����� ������Ʈ�� ����� Data�� �غ��ϰ� ����ϵ��� �Ѵ�.
     * 
     * @param sourceResultModel �񱳰�� Data ��
     * @param coloredComponent �񱳰���� Match Type�� �������� �����Ѵ�.
     * @param accResultMatchType �񱳰�� Match Type
     * @param matchTypeDesc �񱳰�� Match Type�� �̸�
     * @param isTargetModel �񱳰�� Data Model�� ����ڰ� ������ Target Data ���� Source Data ���� ���� (true �ΰ�� Target )
     */
    private void dataCheckUsingTextBaseOutput(
			IAccountabilityCheckResultModel sourceResultModel,
			Map<RGB, List<TCComponent>> coloredComponent,
			AccResult accResultMatchType, String matchTypeDesc, boolean isTargetModel) {

    	// Target Data�� ǥ���� ������� Ȯ�� �Ѵ�.
    	boolean showTarget = true;
    	if(isTargetModel==false && accResultMatchType.equals(AccResult.MISSING_TARGET)==true){
    		showTarget = false;
    	}
    	
    	// Source Data�� ǥ���� ������� Ȯ�� �Ѵ�.
    	boolean showSource = true;
    	if(isTargetModel==true && accResultMatchType.equals(AccResult.MISSING_SOURCE)==true){
    		showSource = false;
    	}
    	
    	// Report�� ����� Excel ������ Cell Color�� �����Ѵ�.
    	RGB matchedTypeRGB =this.accResulRGBtHash.get(accResultMatchType);
    	List<TCComponent> component = coloredComponent.get(matchedTypeRGB);
    	
		for (int i = 0;component != null && i < component.size(); i++) {
			
			if(i == 0){
				System.out.println("[" + new Date() + "] " + matchTypeDesc +" ------------");
			}
			
			String colorCode = null;

			TCComponent currentComponent = component.get(i);
			if(currentComponent!=null && currentComponent instanceof TCComponentBOMLine){

				// Report�� Source �Ǵ� Target�� Match Type���� ��� ���� �ϹǷ� �� Case���� �ѹ��� Check �ϸ� �ȴ�.
				if(i == 0){
					try {
						colorCode = ((TCComponentBOMLine)currentComponent).getStringProperty("bl_bg_colour_int_as_str");
					} catch (TCException e) {
						e.printStackTrace();
					}
				}

				// ���õ� BOMLine�� Source�� Target ������ ǥ���ϱ� ���� ����� �غ�  �Ѵ�.
				AccountabilityCheckResultHelper.EquivalentLines currentEquivalentLines = sourceResultModel.getEquivalents(currentComponent);
	    		List<TCComponent> currentSourceLines = currentEquivalentLines.sourceLines;
	    		List<TCComponent> currentTargetLines = currentEquivalentLines.targetLines;
	    		
	    		TCComponentBOMLine sourceEndItemBOMLine = null;
	    		for (int j = 0;currentSourceLines!=null && j < currentSourceLines.size(); j++) {
	    			sourceEndItemBOMLine = (TCComponentBOMLine)currentSourceLines.get(j);
	    			//progressBar.setStatus("[" + new Date() + "]" + "currentSourceLines["+i+"]["+j+"] = "+currentSourceLines.get(j) +" [S]");
				}	    		

	    		// accResultMatchType.equals(AccResult.MULTIPLE_TARGET)
	    		// accResultMatchType.equals(AccResult.MULTIPLE_PARTIAL_TARGET)
	    		// ���� �ΰ����� �ش� �ϴ� ��� �������� �Ҵ��� ���� �� �����Ƿ� For Loop���� Report�� ����� Data�� �����.
	    		// �׸��� Target���� Data�� �ִ°ܿ쿡�� ���� Loop���� ó���� �� ���̹Ƿ� Targe ���� Source�� �ִ� ��쿡 ����
	    		// ���� ó���� ���ָ� �ǰڴ�.
	    		for (int j = 0;currentTargetLines!=null && j < currentTargetLines.size(); j++) {
	    			//progressBar.setStatus("[" + new Date() + "]" + "currentTargetLines["+i+"]["+j+"] = "+currentTargetLines.get(j) +" [T]");
	    			TCComponentBOMLine targetEndItemBOMLine = null;	
	    			targetEndItemBOMLine = (TCComponentBOMLine)currentTargetLines.get(j);
	    			
	    			printOutDataPreparations(sourceEndItemBOMLine, targetEndItemBOMLine, accResultMatchType);
				}
	    		
	    		// Source�� �ְ� Target�� ���� ��쿡 ���� ���� ó��
	    		if(currentTargetLines==null || (currentTargetLines!=null && currentTargetLines.size()<1)){
	    			if(sourceEndItemBOMLine!=null){
	    				TCComponentBOPLine targetEndItemBOMLine = null;	
	    				
	    				printOutDataPreparations(sourceEndItemBOMLine, targetEndItemBOMLine, accResultMatchType);	
	    			}
	    		}

	    		System.out.println("");
				
			}
			//((TCComponentBOMLine)currentComponent).getDefaultBackgroundColor();			
			
		}
	}
    
    /**
     * Accountability Check ����� Excel�� ����ϱ����� Format�� �´� Data�� ���ڿ� �迭�� ��� Return�Ѵ�.
     * @param srcBOMLine
     * @param targetBOMLine
     */
    private void printOutDataPreparations(TCComponentBOMLine srcBOMLine, TCComponentBOMLine targetBOMLine, AccResult accResultMatchType){
    	
    	// Source & Target�� ��ü���� ������ �����ؾ� �Ѵ�.
    	// Excel�� Column�� ������ ĥ�ϱ����� �غ� �ؾ� �Ѵ�.
    	
    	// SDVTypeConstant.EBOM_MPRODUCT, SDVTypeConstant.EBOM_FUNCTION, 
    	// SDVTypeConstant.EBOM_FUNCTION_MAST

    	// SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, SDVTypeConstant.BOP_PROCESS_LINE_ITEM
    	// SDVTypeConstant.BOP_PROCESS_STATION_ITEM
    	// SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM, SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM
    	// SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM
    	// SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM,  SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM

    	TCComponentBOMLine functionBOMLine = null;
    	TCComponentBOMLine functionMastBOMLine = null;
    	TCComponentBOMLine parentBOMLine = null;

    	TCComponentBOMLine endItemBOMLine = null;
    	
    	TCComponentBOPLine operationBOPLine = null;
    	TCComponentBOPLine stationBOPLine = null;
    	TCComponentBOPLine lineBOPLine = null;
    	
    	// End Item�� ���������� Ȯ�� �Ѵ�.
    	boolean isWeldPoint = false;
    	if(srcBOMLine!=null){
    		String srcBOMLineItemType = null;
			try {
				srcBOMLineItemType = srcBOMLine.getItem().getType();
			} catch (TCException e) {
				e.printStackTrace();
			}
    		if(srcBOMLineItemType!=null && srcBOMLineItemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM)==true){
    			isWeldPoint = true;
    		}
    	}else if(targetBOMLine!=null){
    		String targetBOMLineItemType = null;
			try {
				targetBOMLineItemType = targetBOMLine.getItem().getType();
			} catch (TCException e) {
				e.printStackTrace();
			}
    		if(targetBOMLineItemType!=null && targetBOMLineItemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM)==true){
    			isWeldPoint = true;
    		}
    	}
    	
    	String endItemSupplyMode = null;
 
    	// BOP�� MProduct�� ������ �����ؼ� �ʱ�ȭ �Ѵ�.
    	if(srcIsBOP == true && srcBOMLine!=null){
    		
    		endItemBOMLine= srcBOMLine;
    		
    		try {
				endItemSupplyMode = srcBOMLine.getProperty(SDVPropertyConstant.S7_SUPPLY_MODE);
			} catch (TCException e) {
				e.printStackTrace();
			}
    		
    		// Report ��� �ӵ��� �����̶� ��� �ϱ����ؼ��� Parent Node�� ã������ �˻��� Operation�� ������� Station, Line�� ã����
    		// �����̶� ���� �ӵ��� �ɰ��̴�.
        	// Top Node�� Assembly���� Body, Paint ���� Ȯ�� �ؾ� �Ѵ�.
    		if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("A")){
    			operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM);
    			if(operationBOPLine!=null){
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(operationBOPLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}else{
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}
    		}else if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("B")){
    			// End Item�� ������ �ΰ�� Weld Operation�� ã�ƾ� �Ѵ�.
    			if(isWeldPoint==true){
    				operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM);
    			}else{
    				operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM);
    			}
    			
    			if(operationBOPLine!=null){
    				stationBOPLine = (TCComponentBOPLine) findTypedBOMLine(operationBOPLine,  SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
    			}else{
    				stationBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
    			}
    			if(stationBOPLine!=null){
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(stationBOPLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}else{
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}
    		}else if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("P")){
    			operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM);
    			if(operationBOPLine!=null){
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(operationBOPLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}else{
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}
    		}
    		
    	}

    	if(srcIsBOP == false && srcBOMLine!=null){
    		
    		TCComponentBOMLine assyTopBOMLine = findAssemblyTopPart(srcBOMLine);
    		if(assyTopBOMLine!=null && (assyTopBOMLine.equals(srcBOMLine)==false)){
    			parentBOMLine = assyTopBOMLine;
    		}
    		
    		if(parentBOMLine!=null){
    			functionMastBOMLine= findTypedBOMLine(parentBOMLine,  SDVTypeConstant.EBOM_FUNCTION_MAST);
    		}else{
    			functionMastBOMLine= findTypedBOMLine(srcBOMLine,  SDVTypeConstant.EBOM_FUNCTION_MAST);
    		}
    		if(functionMastBOMLine!=null){
    			functionBOMLine= findTypedBOMLine(functionMastBOMLine,  SDVTypeConstant.EBOM_FUNCTION);
    		}else{
    			functionBOMLine= findTypedBOMLine(srcBOMLine,  SDVTypeConstant.EBOM_FUNCTION);
    		}
    		endItemBOMLine= srcBOMLine;
    	}
    	
    	if(targetIsBOP == true && targetBOMLine!=null){
    		
    		endItemBOMLine= targetBOMLine;
    		System.out.println("targetBOMLine = "+targetBOMLine);
    		System.out.println("SDVPropertyConstant.S7_SUPPLY_MODE = "+SDVPropertyConstant.S7_SUPPLY_MODE);
    		try {
				endItemSupplyMode = targetBOMLine.getProperty(SDVPropertyConstant.S7_SUPPLY_MODE);
			} catch (TCException e) {
				e.printStackTrace();
			}
    		
    		// Report ��� �ӵ��� �����̶� ��� �ϱ����ؼ��� Parent Node�� ã������ �˻��� Operation�� ������� Station, Line�� ã����
    		// �����̶� ���� �ӵ��� �ɰ��̴�.
        	// Top Node�� Assembly���� Body, Paint ���� Ȯ�� �ؾ� �Ѵ�.
    		if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("A")){
    			operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM);
    			if(operationBOPLine!=null){
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(operationBOPLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}else{
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}
    		}else if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("B")){
    			// End Item�� ������ �ΰ�� Weld Operation�� ã�ƾ� �Ѵ�.
    			if(isWeldPoint==true){
    				operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM);
    			}else{
    				operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM);
    			}
    			
    			if(operationBOPLine!=null){
    				stationBOPLine = (TCComponentBOPLine) findTypedBOMLine(operationBOPLine,  SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
    			}else{
    				stationBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
    			}
    			if(stationBOPLine!=null){
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(stationBOPLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}else{
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}

    		}else if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("P")){
    			operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM);
    			if(operationBOPLine!=null){
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(operationBOPLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}else{
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}
    		}
    		
    	}

    	if(targetIsBOP == false && targetBOMLine!=null){
    			
    			TCComponentBOMLine assyTopBOMLine = findAssemblyTopPart(targetBOMLine);
    			if(assyTopBOMLine!=null && (assyTopBOMLine.equals(targetBOMLine)==false)){
    				parentBOMLine = assyTopBOMLine;
    			}
    			
    			if(assyTopBOMLine!=null){
    				functionMastBOMLine= findTypedBOMLine(assyTopBOMLine,  SDVTypeConstant.EBOM_FUNCTION_MAST);
    			}else{
    				functionMastBOMLine= findTypedBOMLine(targetBOMLine,  SDVTypeConstant.EBOM_FUNCTION_MAST);
    			}
    			if(functionMastBOMLine!=null){
    				functionBOMLine= findTypedBOMLine(functionMastBOMLine,  SDVTypeConstant.EBOM_FUNCTION);
    			}else{
    				functionBOMLine= findTypedBOMLine(targetBOMLine,  SDVTypeConstant.EBOM_FUNCTION);
    			}
    			if(endItemBOMLine==null){
    				endItemBOMLine= targetBOMLine;	
    			}
    	}
    	
    	// Report�� ������ ������ BOMLine�� ���� Attribute�� Hashtable�� ��´�.
    	Hashtable<String, String> reportOutDataHash = new Hashtable<String, String>(); 

    	// "Item Id", "Rev Id", "Name", "Status", "Type", "FEATURE NAME"
    	if(functionBOMLine != null){
    		 Hashtable<String, String> functionHash = null;
    		 functionHash = getTypedItemRevAttribute(functionBOMLine);
    		 String itemId = functionHash.get("Item Id");
    		 String revId = functionHash.get("Rev Id");
    		 if(itemId!=null && itemId.trim().length()>0){
    			 reportOutDataHash.put("Function Id", itemId);
    		 }
    		 if(revId!=null && revId.trim().length()>0){
    			 reportOutDataHash.put("Function Rev", revId);
    		 }
    	 }
    	 if(functionMastBOMLine != null){
    		 Hashtable<String, String> functionMastHash = null;
    		 functionMastHash = getTypedItemRevAttribute(functionMastBOMLine);
    		 String itemId = functionMastHash.get("Item Id");
    		 String revId = functionMastHash.get("Rev Id");
    		 if(itemId!=null && itemId.trim().length()>0){
    			 reportOutDataHash.put("FMP Id", itemId);
    		 }
    		 if(revId!=null && revId.trim().length()>0){
    			 reportOutDataHash.put("FMP Rev", revId);
    		 }
    	 }
    	 if(parentBOMLine != null){
    		 Hashtable<String, String> parentPartHash = null;
    		 parentPartHash = getTypedItemRevAttribute(parentBOMLine);
    		 String itemId = parentPartHash.get("Item Id");
    		 String revId = parentPartHash.get("Rev Id");
    		 String name = parentPartHash.get("Name");
    		 if(itemId!=null && itemId.trim().length()>0){
    			 reportOutDataHash.put("Parent Id", itemId);
    		 }
    		 if(revId!=null && revId.trim().length()>0){
    			 reportOutDataHash.put("Parent Rev", revId);
    		 }
    		 if(name!=null && name.trim().length()>0){
    			 reportOutDataHash.put("Parent Name", name);
    		 }
    	 }
    	 
    	 if(endItemBOMLine != null){
    		 Hashtable<String, String> endItemHash = null;
    		 endItemHash = getTypedItemRevAttribute(endItemBOMLine);
    		 String itemId = endItemHash.get("Item Id");
    		 String revId = endItemHash.get("Rev Id");
    		 String name = endItemHash.get("Name");
    		 String type = endItemHash.get("Type");
    		 String status = endItemHash.get("Status");
    		 String featureName = endItemHash.get("FEATURE NAME");
    		 
    		 if(type!=null && (type.equalsIgnoreCase(SDVTypeConstant.EBOM_VEH_PART)==true ||
    				 type.equalsIgnoreCase(SDVTypeConstant.EBOM_STD_PART)==true ||
    				 type.equalsIgnoreCase(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM)==true
    				 )) {
    			 
        		 if(itemId!=null && itemId.trim().length()>0){
        			 reportOutDataHash.put("EndItem Id", itemId);
        		 }
        		 if(revId!=null && revId.trim().length()>0){
        			 reportOutDataHash.put("EndItem Rev", revId);
        		 }
        		 
        		 // �������� ��� Name�� Occurrence Name���� ǥ������� Engineer�� �� �� �ִ�.
        		 if(isWeldPoint==true){
        			 String occurrenceName = null;
    				try {
    					occurrenceName = endItemBOMLine.getProperty(SDVPropertyConstant.BL_OCCURRENCE_NAME);
    				} catch (TCException e) {
    					e.printStackTrace();
    				}
        			 if(occurrenceName!=null && occurrenceName.trim().length()>0){
        				 reportOutDataHash.put("EndItem Name", occurrenceName);
        			 }
        		 }else{
        			 if(name!=null && name.trim().length()>0){
        				 reportOutDataHash.put("EndItem Name", name);
        			 }
        		 }
        		 
        		 if(type!=null && type.trim().length()>0){
        			 reportOutDataHash.put("EndItem Type", type);
        		 }
        		 if(status!=null && status.trim().length()>0){
        			 reportOutDataHash.put("EndItem Status", status);
        		 }
        		 if(featureName!=null && featureName.trim().length()>0){
        			 reportOutDataHash.put("Feature Name", featureName);
        		 }
    			 
    		 }

    	 }
    	 if(operationBOPLine != null){
    		 Hashtable<String, String> operationHash = null;
    		 operationHash = getTypedItemRevAttribute(operationBOPLine);
    		 String itemId = operationHash.get("Item Id");
    		 String revId = operationHash.get("Rev Id");
    		 String name = operationHash.get("Name");
    		 
    		 if(itemId!=null && itemId.trim().length()>0){
    			 reportOutDataHash.put("Operation Id", itemId);
    		 }
    		 if(revId!=null && revId.trim().length()>0){
    			 reportOutDataHash.put("Operation Rev", revId);
    		 }
    		 if(name!=null && name.trim().length()>0){
    			 reportOutDataHash.put("Operation Name", name);
    		 }
    	 }
    	 if(stationBOPLine != null){
    		 Hashtable<String, String> stationHash = null;
    		 stationHash = getTypedItemRevAttribute(stationBOPLine);
    		 String itemId = stationHash.get("Item Id");
    		 String revId = stationHash.get("Rev Id");
    		 String name = stationHash.get("Name");
    		 
    		 if(itemId!=null && itemId.trim().length()>0){
    			 reportOutDataHash.put("Station Id", itemId);
    		 }
    		 if(revId!=null && revId.trim().length()>0){
    			 reportOutDataHash.put("Station Rev", revId);
    		 }
    		 if(name!=null && name.trim().length()>0){
    			 reportOutDataHash.put("Station Name", name);
    		 }
    	 }
    	 if(lineBOPLine != null){
    		 Hashtable<String, String> lineHash = null;
    		 lineHash = getTypedItemRevAttribute(lineBOPLine);
    		 String itemId = lineHash.get("Item Id");
    		 String revId = lineHash.get("Rev Id");
    		 String name = lineHash.get("Name");
    		 
    		 if(itemId!=null && itemId.trim().length()>0){
    			 reportOutDataHash.put("Line Id", itemId);
    		 }
    		 if(revId!=null && revId.trim().length()>0){
    			 reportOutDataHash.put("Line Rev", revId);
    		 }
    		 if(name!=null && name.trim().length()>0){
    			 reportOutDataHash.put("Line Name", name);
    		 }
    	 }
    	 
    	 
    	 if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("A")){
			if(operationBOPLine!=null){
				String stationId = null;
				try {
					stationId = operationBOPLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
				} catch (TCException e) {
					e.printStackTrace();
				}
				
				if(stationId!=null && stationId.trim().length()>0){
					reportOutDataHash.put("Station Id", stationId);
				}
			}
		}
    	 
		 if(endItemSupplyMode!=null && endItemSupplyMode.trim().length()>0){
			 reportOutDataHash.put("Supply Mode", endItemSupplyMode);
		 }
    	 
    	 
    	 // ���� Report�� ����Ʈ �ϴ� �κи� ã�Ƽ� �ٿ��ָ� �ǰڴ�.
    	 printReportData(reportOutDataHash, accResultMatchType);
    }
    
    private TCComponentBOMLine findTypedBOMLine(TCComponentBOMLine bomLine,  String targetType){
		
		TCComponentBOMLine typedBOMLine = FindConnectedPartUtility.findTypedItemBOMLine(bomLine, targetType);
		if(typedBOMLine!=null){
			return typedBOMLine;
		}
		
		return (TCComponentBOMLine)null;
	}

	/**
	 * 
	 * @param partBOMLine
	 * @return
	 */
	private TCComponentBOMLine findAssemblyTopPart(TCComponentBOMLine partBOMLine){
		
		
		
		String currentItemType = null;
		if(partBOMLine!=null){
			try {
				currentItemType = partBOMLine.getItem().getType();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		if(currentItemType!=null && (
					currentItemType.trim().equalsIgnoreCase(SDVTypeConstant.EBOM_VEH_PART) || 
					currentItemType.trim().equalsIgnoreCase(SDVTypeConstant.EBOM_STD_PART) ||
					currentItemType.trim().equalsIgnoreCase(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM)
				)
				){
			TCComponentBOMLine parentBOMLine = null;
			try {
				parentBOMLine = partBOMLine.parent();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			
			if(parentBOMLine!=null){
				String parentItemType = null;
				try {
					parentItemType = parentBOMLine.getItem().getType();
				} catch (TCException e) {
					e.printStackTrace();
				}
				
				if(parentItemType!=null && 
						(currentItemType.trim().equalsIgnoreCase(SDVTypeConstant.EBOM_VEH_PART) || 
								currentItemType.trim().equalsIgnoreCase(SDVTypeConstant.EBOM_STD_PART))==false
						){
					// Paren�� Part�� �ƴҶ� Current�� Return �Ѵ�.
					return partBOMLine;
				}else{
					return findAssemblyTopPart(parentBOMLine);
				}
				
			}else{
				// current�� Return �Ѵ�.
				return partBOMLine;
			}
			
		}else{
			return null;
		}
		
	}

	private Hashtable<String, String> getTypedItemRevAttribute(TCComponentBOMLine bomLine){
		
		Hashtable<String, String> itemRevisionData = null;
		
		
		String itemId = null;
		String itemRevId = null;
		String itemName = null;
		String itemRevisionStatus = null;
		String itemType = null;
	
		try {
			itemId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
			itemRevId = bomLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
			itemName = bomLine.getProperty(SDVPropertyConstant.BL_REV_OBJECT_NAME);
			itemRevisionStatus = bomLine.getProperty(SDVPropertyConstant.BL_RELEASE_STATUS);
			itemType = bomLine.getItem().getType();
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		
		if(itemId!=null && itemId.trim().length()>0){
			itemRevisionData = new Hashtable<String, String>();
		}else{
			return itemRevisionData;
		}
		
		if(itemId!=null && itemId.trim().length()>0){
			itemRevisionData.put("Item Id", itemId.trim());
		}
		if(itemRevId!=null && itemRevId.trim().length()>0){
			itemRevisionData.put("Rev Id", itemRevId.trim());
		}
		if(itemName!=null && itemName.trim().length()>0){
			itemRevisionData.put("Name", itemName.trim());
		}
		if(itemRevisionStatus!=null && itemRevisionStatus.trim().length()>0){
			itemRevisionData.put("Status", itemRevisionStatus.trim());
		}
		if(itemId!=null && itemId.trim().length()>0){
			itemRevisionData.put("Type", itemType.trim());
		}
	
		String featureName = null;
		if(itemType!=null && itemType.equalsIgnoreCase(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM)==true){
			Property currentFeatureNameProperty = null;
			try {
				currentFeatureNameProperty = bomLine.getPropertyObject("M7_FEATURE_NAME");
			} catch (NotLoadedException e) {
				//e.printStackTrace();
			}
			if(currentFeatureNameProperty!=null){
				featureName = currentFeatureNameProperty.getStringValue();
				itemRevisionData.put("FEATURE NAME", featureName.trim());
			}
		}
		
		return itemRevisionData;
	}

	private void printReportData(Hashtable<String, String> reportOutDataHash, AccResult accResultMatchType){
    	//reportOutDataHash;
    	
		String functionId = null;
		String functionRev = null;
		String fMPId = null;
		String fMPRev = null;
		String parentId = null;
		String parentRev = null;
		String parentName = null;
		String endItemId = null;
		String endItemRev = null;
		String endItemName = null;
		String endItemType = null;
		String endItemStatus = null;
		String featureName = null;
		String operationId = null;
		String operationRev = null;
		String operationName = null;
		String stationId = null;
		String stationRev = null;
		String StationName = null;
		String lineId = null;
		String lineRev = null;
		String lineName = null;
		
		// Excel Template�� ���鼭 Index�� ���Ѵ�.
		// Index�� 0���� ���� �Ѵ�.
		if(reportOutDataHash.get("Function Id")!=null){
			functionId = reportOutDataHash.get("Function Id");
		}
		if(reportOutDataHash.get("Function Rev")!=null){
			functionRev = reportOutDataHash.get("Function Rev");
		}
		if(reportOutDataHash.get("FMP Id")!=null){
			fMPId = reportOutDataHash.get("FMP Id");
		}
		if(reportOutDataHash.get("FMP Rev")!=null){
			fMPRev = reportOutDataHash.get("FMP Rev");
		}
		if(reportOutDataHash.get("Parent Id")!=null){
			parentId = reportOutDataHash.get("Parent Id");
		}
		if(reportOutDataHash.get("Parent Rev")!=null){
			parentRev = reportOutDataHash.get("Parent Rev");
		}
		if(reportOutDataHash.get("Parent Name")!=null){
			parentName = reportOutDataHash.get("Parent Name");
		}
		if(reportOutDataHash.get("EndItem Id")!=null){
			endItemId = reportOutDataHash.get("EndItem Id");
		}
		if(reportOutDataHash.get("EndItem Rev")!=null){
			endItemRev = reportOutDataHash.get("EndItem Rev");
		}
		if(reportOutDataHash.get("EndItem Name")!=null){
			endItemName = reportOutDataHash.get("EndItem Name");
		}
		if(reportOutDataHash.get("EndItem Type")!=null){
			endItemType = reportOutDataHash.get("EndItem Type");
		}
		if(reportOutDataHash.get("Feature Name")!=null){
			featureName = reportOutDataHash.get("Feature Name");
		}
		if(reportOutDataHash.get("EndItem Status")!=null){
			endItemStatus = reportOutDataHash.get("EndItem Status");
		}
		if(reportOutDataHash.get("Operation Id")!=null){
			operationId = reportOutDataHash.get("Operation Id");
		}
		if(reportOutDataHash.get("Operation Rev")!=null){
			operationRev = reportOutDataHash.get("Operation Rev");
		}
		if(reportOutDataHash.get("Operation Name")!=null){
			operationName = reportOutDataHash.get("Operation Name");
		}
		if(reportOutDataHash.get("Station Id")!=null){
			stationId = reportOutDataHash.get("Station Id");
		}
		if(reportOutDataHash.get("Station Rev")!=null){
			stationRev = reportOutDataHash.get("Station Rev");
		}
		if(reportOutDataHash.get("Station Name")!=null){
			StationName = reportOutDataHash.get("Station Name");
		}
		if(reportOutDataHash.get("Line Id")!=null){
			lineId = reportOutDataHash.get("Line Id");
		}
		if(reportOutDataHash.get("Line Rev")!=null){
			lineRev = reportOutDataHash.get("Line Rev");
		}
		if(reportOutDataHash.get("Line Name")!=null){
			lineName = reportOutDataHash.get("Line Name");
		}
		
		System.out.println(
				functionId +"\t"+
				functionRev +"\t"+
				fMPId +"\t"+
				fMPRev +"\t"+
				parentId +"\t"+
				parentRev +"\t"+
				parentName +"\t"+
				endItemId +"\t"+
				endItemRev +"\t"+
				endItemName +"\t"+
				endItemType +"\t"+
				endItemStatus +"\t"+
				featureName +"\t"+
				operationId +"\t"+
				operationRev +"\t"+
				operationName +"\t"+
				stationId +"\t"+
				stationRev +"\t"+
				StationName +"\t"+
				lineId +"\t"+
				lineRev +"\t"+
				lineName
				);
		
		String matchType = accResulNameHash.get(accResultMatchType);
		RGB matchedTypeRGB =this.accResulRGBtHash.get(accResultMatchType);
		
		reportOutDataHash.put("Match Type", matchType);
		accountabilityDataExcelExporter.writeRow(reportOutDataHash, keyIndexList, matchedTypeRGB);
		
    }
 
}
