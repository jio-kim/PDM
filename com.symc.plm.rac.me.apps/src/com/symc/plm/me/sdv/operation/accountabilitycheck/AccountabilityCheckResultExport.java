package com.symc.plm.me.sdv.operation.accountabilitycheck;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.WaitProgressBar;
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
 *  * Accountability Check 결과를 Excel로 출력 할 수 있도록 기능을 추가해주세요.
 * [SR150106-015] 20151028 taeku.jeong	이종화 차장님의 요구로 추가된 SR에 대한 대응
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
		
		// Report의 Source와 관련된 출력을 위한 Match Type을 정의 한다. 
		this.accUtilListForSourceModel = new AccResult[] {
				AccResult.MISSING_TARGET, AccResult.PARTIAL_MATCH,
				AccResult.MULTIPLE_PARTIAL_TARGET, AccResult.FULL_MATCH,
				AccResult.MULTIPLE_TARGET,
		};
		
		// Report의 Target과 관련된 출력을 위한 Match Type을 정의 한다.
		this.accUtilListForTargetModel = new AccResult[] {
				AccResult.MISSING_SOURCE
		};
		 
		// Match Type 정의에 대해 정리한다.
		initAccResultHashs();

		// Accountability Check Result 정보 초기화
		initResultInformation();

		// Result가 List up 되는지 확인 한다.
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
    	
    	
    	// 선택된 Source와 Target BOMTree를 찾아야 한다.
    	initBopType();
    	
    	initExportTargetFilePath();
    	if(this.exportTargetFilePath==null || (this.exportTargetFilePath!=null && this.exportTargetFilePath.trim().length()<1)){
    		this.exportTargetFilePath = "c:\\AccountabilityCheckReport.xlsx";
    	}
		
		try {
			accountabilityDataExcelExporter =new AccountabilityDataExcelExporter();
			accountabilityDataExcelExporter.readyFile(this.exportTargetFilePath, this.session );
			
			System.out.println("Print source model data");
			
			// 헤더에 해당하는 정보를 Excel에 출력할 경우 이쯤에서 해야 될것 같은데..
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
    		// 선택된 소스가 MProduct의 BOMLine인 경우임.
    		accountabilityDataExcelExporter.printSrcAndTargetInfomation(this.srcWindow, this.targetWindow);
    	}else{
    		// 선택된 소스가 BOP의 BOMLine인 경우임
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
     * Accountability Check에서 
     * Missing Target 과 Full Match, Partial Match, Multiple Target, Full Match, Multiple Partial Target를 표현하면 좋겠다.
     * Missing Source를 제외한 Report가 가능하다.
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
     * Accountability Check에서 
     * Missing Source를 표현하면 좋겠다.
     * Missing Target을 제외한 Report가 가능하지만 다른 것들은 Source를 기준으로 전개되는것이 일관성이 있을것으로 보인다.
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
     * Accountability Check  결과를 Match Type별로 구분해서  Report 출력에 반영 하도록 Data를 준비하는 Function
     * Missing Source, Full Match, Missing Target, Multiple Target 등의 Match Type에 해당하는 Data 묶음 단위로
     * 비교된 결과를 엑셀시트에 출력할 Data를 준비하고 출력하도록 한다.
     * 
     * @param sourceResultModel 비교결과 Data 모델
     * @param coloredComponent 비교결과를 Match Type별 색상으로 구분한다.
     * @param accResultMatchType 비교결과 Match Type
     * @param matchTypeDesc 비교결과 Match Type의 이름
     * @param isTargetModel 비교결과 Data Model이 사용자가 선택한 Target Data 인지 Source Data 인지 구분 (true 인경우 Target )
     */
    private void dataCheckUsingTextBaseOutput(
			IAccountabilityCheckResultModel sourceResultModel,
			Map<RGB, List<TCComponent>> coloredComponent,
			AccResult accResultMatchType, String matchTypeDesc, boolean isTargetModel) {

    	// Target Data를 표현할 대상인지 확인 한다.
    	boolean showTarget = true;
    	if(isTargetModel==false && accResultMatchType.equals(AccResult.MISSING_TARGET)==true){
    		showTarget = false;
    	}
    	
    	// Source Data를 표현할 대상인지 확인 한다.
    	boolean showSource = true;
    	if(isTargetModel==true && accResultMatchType.equals(AccResult.MISSING_SOURCE)==true){
    		showSource = false;
    	}
    	
    	// Report를 출력할 Excel 파일의 Cell Color를 결정한다.
    	RGB matchedTypeRGB =this.accResulRGBtHash.get(accResultMatchType);
    	List<TCComponent> component = coloredComponent.get(matchedTypeRGB);
    	
		for (int i = 0;component != null && i < component.size(); i++) {
			
			if(i == 0){
				System.out.println("[" + new Date() + "] " + matchTypeDesc +" ------------");
			}
			
			String colorCode = null;

			TCComponent currentComponent = component.get(i);
			if(currentComponent!=null && currentComponent instanceof TCComponentBOMLine){

				// Report를 Source 또는 Target의 Match Type별로 묶어서 진행 하므로 각 Case별로 한번만 Check 하면 된다.
				if(i == 0){
					try {
						colorCode = ((TCComponentBOMLine)currentComponent).getStringProperty("bl_bg_colour_int_as_str");
					} catch (TCException e) {
						e.printStackTrace();
					}
				}

				// 선택된 BOMLine의 Source와 Target 정보를 표시하기 위한 방법을 준비  한다.
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
	    		// 위의 두가지에 해당 하는 경우 복수개의 할당이 있을 수 있으므로 For Loop에서 Report에 출력할 Data를 만든다.
	    		// 그리고 Target에만 Data가 있는겨우에도 여기 Loop에서 처리가 될 것이므로 Targe 없이 Source만 있는 경우에 대한
	    		// 보완 처리를 해주면 되겠다.
	    		for (int j = 0;currentTargetLines!=null && j < currentTargetLines.size(); j++) {
	    			//progressBar.setStatus("[" + new Date() + "]" + "currentTargetLines["+i+"]["+j+"] = "+currentTargetLines.get(j) +" [T]");
	    			TCComponentBOMLine targetEndItemBOMLine = null;	
	    			targetEndItemBOMLine = (TCComponentBOMLine)currentTargetLines.get(j);
	    			
	    			printOutDataPreparations(sourceEndItemBOMLine, targetEndItemBOMLine, accResultMatchType);
				}
	    		
	    		// Source만 있고 Target이 없는 경우에 대한 보완 처리
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
     * Accountability Check 결과를 Excel에 출력하기위한 Format에 맞는 Data를 문자열 배열에 담아 Return한다.
     * @param srcBOMLine
     * @param targetBOMLine
     */
    private void printOutDataPreparations(TCComponentBOMLine srcBOMLine, TCComponentBOMLine targetBOMLine, AccResult accResultMatchType){
    	
    	// Source & Target의 구체적인 정보를 취합해야 한다.
    	// Excel의 Column의 색상을 칠하기위한 준비도 해야 한다.
    	
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
    	
    	// End Item이 용접점인지 확인 한다.
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
 
    	// BOP와 MProduct의 정보를 구분해서 초기화 한다.
    	if(srcIsBOP == true && srcBOMLine!=null){
    		
    		endItemBOMLine= srcBOMLine;
    		
    		try {
				endItemSupplyMode = srcBOMLine.getProperty(SDVPropertyConstant.S7_SUPPLY_MODE);
			} catch (TCException e) {
				e.printStackTrace();
			}
    		
    		// Report 출력 속도를 조금이라도 향상 하기위해서는 Parent Node를 찾기위해 검색된 Operation을 기반으로 Station, Line을 찾으면
    		// 조금이라도 나은 속도가 될것이다.
        	// Top Node가 Assembly인지 Body, Paint 인지 확인 해야 한다.
    		if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("A")){
    			operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM);
    			if(operationBOPLine!=null){
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(operationBOPLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}else{
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(srcBOMLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}
    		}else if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("B")){
    			// End Item이 용접점 인경우 Weld Operation을 찾아야 한다.
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
    		
    		// Report 출력 속도를 조금이라도 향상 하기위해서는 Parent Node를 찾기위해 검색된 Operation을 기반으로 Station, Line을 찾으면
    		// 조금이라도 나은 속도가 될것이다.
        	// Top Node가 Assembly인지 Body, Paint 인지 확인 해야 한다.
    		if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("A")){
    			operationBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM);
    			if(operationBOPLine!=null){
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(operationBOPLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}else{
    				lineBOPLine = (TCComponentBOPLine) findTypedBOMLine(targetBOMLine,  SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
    			}
    		}else if(this.bopType!=null && this.bopType.trim().equalsIgnoreCase("B")){
    			// End Item이 용접점 인경우 Weld Operation을 찾아야 한다.
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
    	
    	// Report를 구성할 각각의 BOMLine에 대한 Attribute를 Hashtable에 담는다.
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
        		 
        		 // 용접점의 경우 Name을 Occurrence Name으로 표기해줘야 Engineer가 알 수 있다.
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
    	 
    	 
    	 // 이제 Report에 프린트 하는 부분만 찾아서 붙여주면 되겠다.
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
					// Paren가 Part가 아닐때 Current를 Return 한다.
					return partBOMLine;
				}else{
					return findAssemblyTopPart(parentBOMLine);
				}
				
			}else{
				// current를 Return 한다.
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
		
		// Excel Template을 보면서 Index를 정한다.
		// Index는 0부터 시작 한다.
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
