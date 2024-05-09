package com.kgm.commands.variantoptioneditor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.TcDefinition;
import com.kgm.common.utils.variant.OptionManager;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.operations.ExpandBelowOperation;
import com.teamcenter.rac.pse.services.PSEApplicationService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Product, Variant, Function에 옵션을 설정한다..
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"unused", "rawtypes"})
public class OptionSetCommand extends AbstractAIFCommand {

	private ArrayList<TCComponentBOMLine> list ; 
	private HashMap<String, String> optionItemMap = new HashMap<String, String>();
	private ArrayList<VariantOption> globalOptionSet = new ArrayList<VariantOption>();
	private Registry registry =null;
	private HashMap descMap = null;
	private OptionManager manager = null;
	private static OptionSetDialog dialog = null;
	
	@Override
	protected void executeCommand() throws Exception {
		if( dialog != null ){
			dialog.dispose();
		}
		// 모든 Corporate Option Item의 옵션 정보를 가져오고, globalOptionSet에 옵션 정보를 입력한다.
		registry = Registry.getRegistry(this);
		
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		
		if( coms == null || coms.length < 1){
			return;
		}
		if( coms[0] instanceof TCComponentBOMLine){
			final TCComponentBOMLine selectedLine = (TCComponentBOMLine)coms[0];
			TCComponentBOMLine topLine = selectedLine.window().getTopBOMLine();
			String topType = topLine.getItem().getType();
			//최상위가 Product Type인 경우만 가능함.
			if( !topType.equals(TcDefinition.PRODUCT_ITEM_TYPE)){
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.topItemIsNotProduct"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			if( selectedLine.window().isModified() ){
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.windowSaveFirst"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			
			final WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			AbstractAIFUIApplication aifApp = AIFUtility.getCurrentApplication();
			PSEApplicationService service = (PSEApplicationService)aifApp;
			final BOMTreeTable treeTable = (BOMTreeTable)service.getAbstractViewableTreeTable();
			initDialog(treeTable, selectedLine, waitProgress);
			
//			expandAll(treeTable, selectedLine, waitProgress);
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
	private void initDialog(final BOMTreeTable treeTable, final TCComponentBOMLine target, final WaitProgressBar waitProgress) throws TCException
	{
		ArrayList<VariantOption> enableOptionSet = new ArrayList<VariantOption>();
  		ArrayList<VariantOption> selectedLineOptionSet = new ArrayList<VariantOption>();
    	ArrayList<VariantOption> optionSet = null;
    	
    	try{
    		waitProgress.setStatus(registry.getString("variant.findingCorpItem"), true);
			manager = new OptionManager(target, true, waitProgress);
			
			globalOptionSet = manager.getCorpOptionSet();
			String type = target.getItem().getType();
			
			
			//사용자가 팀센터 기본기능을 이용하여 추가한 유효성 검사목록
			Vector<String[]> userDefineErrorList = new Vector<String[]>();
			Vector<String[]> moduleConstratintsList = new Vector<String[]>();
			if( type.equals(TcDefinition.PRODUCT_ITEM_TYPE) ){
				
				optionSet = manager.getOptionSet(target, userDefineErrorList, moduleConstratintsList, true);
				selectedLineOptionSet.addAll(optionSet);
				
				Vector<VariantValue> valueList = new Vector<VariantValue>();
				for( int i = 0; i < optionSet.size(); i++){
					VariantOption option = optionSet.get(i);
					List<VariantValue> list = option.getValues();
					for( int j = 0; j < list.size(); j++){
						list.get(j).setUsing(false);
						if( !valueList.contains(list.get(j)))
							valueList.add(list.get(j));
					}
				}
//				HashMap<TCComponentBOMLine, List<String>> conditionMap = manager.getConditionSetAll(target, null);
				HashMap<TCComponentBOMLine, List<String>> conditionMap = new HashMap<TCComponentBOMLine, List<String>>();
				waitProgress.setStatus("Loading a sub option set...");
//				manager.usingCheck(target, valueList, conditionMap, target.getSession().getVariantService(), 0, 2);
				dialog = new OptionSetDialog(globalOptionSet, globalOptionSet, selectedLineOptionSet, target, userDefineErrorList, moduleConstratintsList, manager);
			
			}else if ( type.equals(TcDefinition.VARIANT_ITEM_TYPE) || type.equals(TcDefinition.FUNCTION_ITEM_TYPE) ){
				
				TCComponentBOMLine topLine = target.window().getTopBOMLine();
				optionSet = manager.getOptionSet(topLine, userDefineErrorList, moduleConstratintsList, false);
				enableOptionSet.addAll(optionSet);
				userDefineErrorList.clear();
				moduleConstratintsList.clear();
				optionSet = manager.getOptionSet(target, userDefineErrorList, moduleConstratintsList, true);
				selectedLineOptionSet.addAll(optionSet);
				
				Vector<VariantValue> valueList = new Vector<VariantValue>();
				for( int i = 0; i < optionSet.size(); i++){
					VariantOption option = optionSet.get(i);
					List<VariantValue> list = option.getValues();
					for( int j = 0; j < list.size(); j++){
						list.get(j).setUsing(false);
						if( !valueList.contains(list.get(j)))
							valueList.add(list.get(j));
					}
				}
				
				waitProgress.setStatus("Loading a sub option set...");
//				SRME:: [][20140812] swyoon  Prouct, Variant, Function에 옵션 설정 속도 개선(하위에서 사용여부 체크 제거).				
//				HashMap<TCComponentBOMLine, List<String>> conditionMap = manager.getConditionSetAll(target, null);
				HashMap<TCComponentBOMLine, List<String>> conditionMap = new HashMap<TCComponentBOMLine, List<String>>();
				if( type.equals(TcDefinition.VARIANT_ITEM_TYPE)){
					//Variant는 하위 Function이랑 상관없으므로 하위에서 사용중인지 아닌지가 의미 없을 수도 있다.
//					manager.usingCheck(target, valueList, conditionMap, target.getSession().getVariantService(),0,1);
				}else if(type.equals(TcDefinition.FUNCTION_ITEM_TYPE)){
					//Conditiond으로 사용되므로 체크 필요.
//					manager.usingCheck(target, valueList, conditionMap, target.getSession().getVariantService());
				}
				
				dialog = new OptionSetDialog(globalOptionSet, enableOptionSet, selectedLineOptionSet, target, userDefineErrorList, moduleConstratintsList, manager);
			
			}else{
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.onlyProductVariantFunction"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			
//			Dialog 닫은 후 생성한 모든 윈도우를 Close함.
			dialog.addWindowListener(new WindowAdapter(){
				
				@Override
				public void windowClosed(WindowEvent windowevent) {
					manager.clear(false);
					super.windowClosed(windowevent);
				}
				
			});
			setRunnable(dialog);
		
			OptionSetCommand.super.executeCommand();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			waitProgress.dispose();
		}
	}
	
	/**
	 * BOM 이 모두 확장되어야 함.(확장을 하지 않아도 진행은 되지만, 속도가 느림)
	 * 
	 * @param treeTable
	 * @param target
	 * @param waitProgress
	 * @throws TCException
	 */
	public void expandAll(final BOMTreeTable treeTable, final TCComponentBOMLine target, final WaitProgressBar waitProgress) throws TCException
	{

		int expendDeption = -1;
		String type = target.getItem().getType();
		if( type.equals(TcDefinition.PRODUCT_ITEM_TYPE)){
			expendDeption = 2;
		}else if( type.equals(TcDefinition.VARIANT_ITEM_TYPE) ){
			expendDeption = 1;
		}else if( type.equals(TcDefinition.FUNCTION_ITEM_TYPE) ){
			expendDeption = 2;
		}
		
		ExpandBelowOperation operation = new ExpandBelowOperation(treeTable, target == null ? treeTable.getRootBOMLineNode() : treeTable.getNode(target), expendDeption, true);
		operation.addOperationListener(new InterfaceAIFOperationListener() {

			@Override
			public void endOperation() {

				AbstractAIFOperation op = new AbstractAIFOperation() {

					@Override
					public void executeOperation() throws Exception {
						try {
							initDialog(treeTable, target, waitProgress);
						} catch (TCException e) {
							e.printStackTrace();
						}
					}

				};
				treeTable.getRootBOMLineNode().getSession().queueOperation(op);
			}

	      @Override
	      public void startOperation(String arg0)
	      {
	    	  waitProgress.setStatus("Expanding BOM", true);
	      }

	    });
	    treeTable.getRootBOMLineNode().getSession().queueOperation(operation);
	 }
}
