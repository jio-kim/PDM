package com.symc.plm.rac.prebom.optionedit.commands;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.symc.plm.rac.prebom.optionedit.dialog.PreFmpOptionSetDialog;
import com.symc.plm.rac.prebom.optionedit.operation.PreFmpOptionLoader;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Product, Variant, Function에 옵션을 설정한다..
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"unused", "rawtypes"})
public class PreFmpOptionSetCommand extends AbstractAIFCommand {

	private ArrayList<TCComponentBOMLine> list ; 
	private HashMap<String, String> optionItemMap = new HashMap<String, String>();
//	private ArrayList<VariantOption> globalOptionSet = new ArrayList<VariantOption>();
	private Registry registry =null;
	private HashMap descMap = null;
	private OptionManager manager = null;
	private static PreFmpOptionSetDialog dialog = null;
	
	@Override
	protected void executeCommand() throws Exception {
		if( dialog != null ){
			dialog.dispose();
		}
		// 모든 Corporate Option Item의 옵션 정보를 가져오고, globalOptionSet에 옵션 정보를 입력한다.
		registry = Registry.getRegistry("com.symc.plm.rac.prebom.optionedit");
		
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		
		if( coms == null || coms.length < 1){
			return;
		}
		if( coms[0] instanceof TCComponentBOMLine){
			final TCComponentBOMLine selectedLine = (TCComponentBOMLine)coms[0];
			TCComponentBOMLine topLine = selectedLine.window().getTopBOMLine();
			String topType = topLine.getItem().getType();
			
			String selectedType = selectedLine.getItem().getType();
			if( !selectedType.equals(TypeConstant.S7_PREFUNCMASTERTYPE)){
				MessageBox.post(AIFUtility.getActiveDesktop().getFrame(), "Please select Pre-FMP Line.", "INFO", MessageBox.INFORMATION);
				return;
			}
			
			final TCComponentItemRevision preProductRevision = (TCComponentItemRevision)BomUtil.getParent(selectedLine.getItemRevision(), TypeConstant.S7_PREPRODUCTREVISIONTYPE);
			if( preProductRevision == null){
				MessageBox.post(AIFUtility.getActiveDesktop().getFrame(), "Could not find PreProduct.", "INFO", MessageBox.INFORMATION);
				return;
			}
			
			//최상위가 Product Type인 경우만 가능함.
			if( !topType.equals(TypeConstant.S7_PREPRODUCTTYPE)){
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.topItemIsNotProduct"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			if( selectedLine.window().isModified() ){
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.windowSaveFirst"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			
			final WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
			waitProgress.start();
			final PreFmpOptionLoader loader = new PreFmpOptionLoader(topLine, selectedLine, waitProgress);
			loader.addOperationListener(new InterfaceAIFOperationListener() {
				
				@Override
				public void startOperation(String arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void endOperation() {
					// TODO Auto-generated method stub
					waitProgress.dispose();
					HashMap<String, Object> result = (HashMap<String, Object>)loader.getOperationResult();
					if( result.containsKey(PreFmpOptionLoader.ERROR)){
						Exception e = (Exception)result.get(PreFmpOptionLoader.ERROR);
						MessageBox.post(AIFUtility.getActiveDesktop(), e.getMessage(), "ERROR", MessageBox.ERROR);
						return;
					}
					
					ArrayList<VariantOption> enableOptionSet = (ArrayList<VariantOption>)result.get(PreFmpOptionLoader.ENABLE_OPTIONS);
					ArrayList<VariantOption> selectedLineOptionSet = (ArrayList<VariantOption>)result.get(PreFmpOptionLoader.FMP_OPTIONS);
					
					try {
						initDialog(selectedLine, enableOptionSet, selectedLineOptionSet, waitProgress);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						MessageBox.post(AIFUtility.getActiveDesktop(), e.getMessage(), "ERROR", MessageBox.ERROR);
						return;
					}
				}
			});
			selectedLine.getSession().queueOperation(loader);
			
		}
		
	}
	
	/**
	 * dialog 생성시 필요한 데이타(하위 옵션셋)를 수집 및 Dialog Open.
	 * 
	 * @param treeTable
	 * @param target
	 * @param waitProgress
	 * @throws TCException
	 */
	private void initDialog(final TCComponentBOMLine target, ArrayList<VariantOption> enableOptionSet
			, ArrayList<VariantOption> selectedLineOptionSet, final WaitProgressBar waitProgress) throws TCException
	{
//		ArrayList<VariantOption> enableOptionSet = new ArrayList<VariantOption>();
//  		ArrayList<VariantOption> selectedLineOptionSet = new ArrayList<VariantOption>();
    	ArrayList<VariantOption> optionSet = null;
    	
    	try{
    		waitProgress.setStatus(registry.getString("variant.findingCorpItem"), true);
//			manager = new OptionManager(target, false, waitProgress);
			
//			globalOptionSet = manager.getCorpOptionSet();
			String type = target.getItem().getType();
			
			if( !type.equals(TypeConstant.S7_PREFUNCMASTERTYPE)){
				MessageBox.post(AIFUtility.getActiveDesktop(), "Select a FMP Item", "INFORMATION", MessageBox.WARNING);
				return;
			}
			
			//사용자가 팀센터 기본기능을 이용하여 추가한 유효성 검사목록
//			Vector<String[]> userDefineErrorList = new Vector<String[]>();
//			Vector<String[]> moduleConstratintsList = new Vector<String[]>();
//			TCComponentBOMLine topLine = target.window().getTopBOMLine();
//			optionSet = manager.getOptionSet(topLine, userDefineErrorList, moduleConstratintsList, false);
//			enableOptionSet.addAll(optionSet);
//			userDefineErrorList.clear();
//			moduleConstratintsList.clear();
//			optionSet = manager.getOptionSet(target, userDefineErrorList, moduleConstratintsList, true);
//			selectedLineOptionSet.addAll(optionSet);
			
//			Vector<VariantValue> valueList = new Vector<VariantValue>();
//			for( int i = 0; i < optionSet.size(); i++){
//				VariantOption option = optionSet.get(i);
//				List<VariantValue> list = option.getValues();
//				for( int j = 0; j < list.size(); j++){
//					list.get(j).setUsing(false);
//					if( !valueList.contains(list.get(j)))
//						valueList.add(list.get(j));
//				}
//			}
			
			waitProgress.setStatus("Loading a sub option set...");
			
			dialog = new PreFmpOptionSetDialog(enableOptionSet, selectedLineOptionSet, target);
			setRunnable(dialog);
		
			PreFmpOptionSetCommand.super.executeCommand();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			waitProgress.dispose();
		}
	}
	
//	/**
//	 * BOM 이 모두 확장되어야 함.(확장을 하지 않아도 진행은 되지만, 속도가 느림)
//	 * 
//	 * @param treeTable
//	 * @param target
//	 * @param waitProgress
//	 * @throws TCException
//	 */
//	public void expandAll(final BOMTreeTable treeTable, final TCComponentBOMLine target, final WaitProgressBar waitProgress) throws TCException
//	{
//
//		int expendDeption = -1;
//		String type = target.getItem().getType();
//		if( type.equals(TcDefinition.PRODUCT_ITEM_TYPE)){
//			expendDeption = 2;
//		}else if( type.equals(TcDefinition.VARIANT_ITEM_TYPE) ){
//			expendDeption = 1;
//		}else if( type.equals(TcDefinition.FUNCTION_ITEM_TYPE) ){
//			expendDeption = 2;
//		}
//		
//		ExpandBelowOperation operation = new ExpandBelowOperation(treeTable, target == null ? treeTable.getRootBOMLineNode() : treeTable.getNode(target), expendDeption, true);
//		operation.addOperationListener(new InterfaceAIFOperationListener() {
//
//			@Override
//			public void endOperation() {
//
//				AbstractAIFOperation op = new AbstractAIFOperation() {
//
//					@Override
//					public void executeOperation() throws Exception {
//						try {
//							initDialog(target, waitProgress);
//						} catch (TCException e) {
//							e.printStackTrace();
//						}
//					}
//
//				};
//				treeTable.getRootBOMLineNode().getSession().queueOperation(op);
//			}
//
//	      @Override
//	      public void startOperation(String arg0)
//	      {
//	    	  waitProgress.setStatus("Expanding BOM", true);
//	      }
//
//	    });
//	    treeTable.getRootBOMLineNode().getSession().queueOperation(operation);
//	 }
}
