package com.kgm.commands.variantconditionset;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.kgm.common.SYMCClass;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.TcDefinition;
import com.kgm.common.utils.variant.OptionManager;
import com.kgm.common.utils.variant.VariantOption;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Function Master바로 하위 Vehicle Part에만 condition을 셋팅할 수 있다.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"unused", "rawtypes"})
public class ConditionSetCommand extends AbstractAIFCommand {
	private ArrayList<TCComponentBOMLine> list ; 
	private HashMap<String, String> optionItemMap = new HashMap<String, String>();
	private Registry registry =null;
    private HashMap descMap = null;
	private OptionManager manager = null;
	
	@Override
	protected void executeCommand() throws Exception {
		registry = Registry.getRegistry(this);
		WaitProgressBar waitProgress = new WaitProgressBar(null);
		waitProgress.start();
		
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		ArrayList<VariantOption> enableOptionSet = new ArrayList<VariantOption>();
		ArrayList<VariantOption> selectedLineOptionSet = new ArrayList<VariantOption>();
		TCComponentBOMLine selectedLine = null;
		ConditionSetDialog dialog = null;
		
		if( coms == null || coms.length < 1){
			waitProgress.dispose();
			return;
		}
		
		if( coms[0] instanceof TCComponentBOMLine){
			
			ArrayList<VariantOption> optionSet = null;
			selectedLine = (TCComponentBOMLine)coms[0];
			TCComponentBOMLine parent = selectedLine.parent();
			if( parent == null){
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.isTopLine"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			
			String parentType = parent.getItem().getType();
			Registry reg = Registry.getRegistry("com.kgm.commands.variantoptioneditor.variantoptioneditor");
			String type = selectedLine.getItem().getType();
			
			//사용자가 팀센터 기본기능을 이용하여 추가한 유효성 검사목록
			Vector<String[]> userDefineErrorList = new Vector<String[]>();
			if ( parentType.equals(TcDefinition.FUNCTION_MASTER_ITEM_TYPE)){
				waitProgress.setStatus(reg.getString("variant.findingCorpItem"), true);
				manager = new OptionManager(selectedLine, true);
//				TCComponentBOMLine topLine = selectedLine.window().getTopBOMLine();
				
				//Function Master바로 하위 Vehicle Part에만 condition을 셋팅할 수 있다.
				optionSet = manager.getOptionSet(parent,null, null, null, false, false);
				while( parent != null && ( optionSet == null || optionSet.isEmpty())){
					parent = parent.parent();
					if( parent == null ) break;
					parentType = parent.getItem().getType();
					optionSet = manager.getOptionSet(parent,null, null, null, false, false);
					
					if( parentType.equals(TcDefinition.FUNCTION_ITEM_TYPE))
						break;
				}
				
				enableOptionSet.addAll(optionSet);
				List<ConditionVector> conditions = manager.getConditionSet(selectedLine);
				dialog = new ConditionSetDialog( enableOptionSet, conditions, selectedLine, userDefineErrorList, manager);
			
			}else{
				if(parentType.equals(TcDefinition.PRE_FUNCTION_MASTER_ITEM_TYPE) || parentType.equals("S7_PreVehPart")){
					
					waitProgress.setStatus("Loading Pre FMP Options...");
					manager = new OptionManager(selectedLine, false);
					
					//Pre Function Master바로 하위 Pre Vehicle Part에만 condition을 셋팅할 수 있다.
					optionSet = manager.getOptionSet(parent,null, null, null, false, false);
					while( parent != null && ( optionSet == null || optionSet.isEmpty())){
						parent = parent.parent();
						if( parent == null ) break;
						parentType = parent.getItem().getType();
						optionSet = manager.getOptionSet(parent,null, null, null, false, false);
						
						if( parentType.equals(TcDefinition.PRE_FUNCTION_ITEM_TYPE))
							break;
					}
					
					enableOptionSet.addAll(optionSet);
					List<ConditionVector> conditions = manager.getConditionSet(selectedLine);
					dialog = new ConditionSetDialog( enableOptionSet, conditions, selectedLine, userDefineErrorList, manager);
				}else{
					waitProgress.dispose();
					MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.invalidItemType"), "INFORMATION", MessageBox.WARNING);
					return;
					
				}
			}
			
		}else{
			waitProgress.dispose();
			return;
		}
		
		waitProgress.dispose();
		
//		Dialog 닫은 후 생성한 모든 윈도우를 Close함.
		dialog.addWindowListener(new WindowAdapter(){

			
			@Override
			public void windowClosed(WindowEvent windowevent) {
				//Dialog close시에 생성된 Window를 모두 Close함.
				manager.clear(false);
				super.windowClosed(windowevent);
			}
			
		});
//		dialog.setVisible(true);
		setRunnable(dialog);
		
		super.executeCommand();
	}
}
