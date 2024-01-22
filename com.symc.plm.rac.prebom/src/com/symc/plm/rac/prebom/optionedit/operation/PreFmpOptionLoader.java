package com.symc.plm.rac.prebom.optionedit.operation;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class PreFmpOptionLoader extends AbstractAIFOperation {

	private OptionManager optionManager = null;
	private TCComponentBOMLine preProductLine = null;
	private TCComponentBOMLine fmpLine = null;
	private TCComponentItemRevision fmpRevision = null;
	private TCSession session = null;
	private WaitProgressBar waitBar = null;
	private ArrayList<VariantOption> enableOptionList = null;
	private ArrayList<VariantOption> fmpOptionList = null;
	private HashMap<String, Object> result = new HashMap();
	
	public static final String ERROR = "ERROR";
	public static final String ENABLE_OPTIONS = "ENABLE_OPTIONS";
	public static final String FMP_OPTIONS = "FMP_OPTIONS";
	
	
	public PreFmpOptionLoader(TCComponentBOMLine preProductLine, TCComponentBOMLine fmpLine, WaitProgressBar waitBar) throws TCException{
		this.preProductLine = preProductLine;
		this.fmpLine = fmpLine;
		this.fmpRevision = fmpLine.getItemRevision();
		this.session = fmpRevision.getSession();
		this.waitBar = waitBar;
	}
	
	private void setStatusMsg(String msg){
		if( waitBar != null){
			waitBar.setStatus(msg);
		}
	}
	
	@Override
	public void executeOperation() throws Exception {
		// TODO Auto-generated method stub
		
//		TCComponentItemRevision preProductRevision = (TCComponentItemRevision)BomUtil.getParent(fmpRevision, TypeConstant.S7_PREPRODUCTREVISIONTYPE);
		
		TCComponentBOMLine productLine = null;
		try{
			
			setStatusMsg("Loading Pre-Product Option...");
//			productLine = CustomUtil.getBomline(preProductRevision, session);
			optionManager = new OptionManager(preProductLine, false);
			enableOptionList = optionManager.getOptionSet(preProductLine,null, null, null, false, false);
			result.put(ENABLE_OPTIONS, enableOptionList);
			
			setStatusMsg("Loading Pre-FMP Option...");
			fmpOptionList = optionManager.getOptionSet(fmpLine,null, null, null, false, false);
			result.put(FMP_OPTIONS, fmpOptionList);
			
		}catch(Exception e){
			result.put(ERROR, e);
		}finally{
			storeOperationResult(result);
//			if( productLine != null){
//				productLine.window().close();
//			}
//			
//			if( fmpLine != null){
//				fmpLine.window().close();
//			}
		}
	}

}
