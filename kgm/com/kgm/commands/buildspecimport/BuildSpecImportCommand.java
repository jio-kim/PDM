package com.kgm.commands.buildspecimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

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
 * @author slobbie
 *
 * H-BOM과 인터페이스 하여 허브봄에 존재하는 Spec(O-Spec 아님)을 가져온 후
 * 새로운 SOS를 생성한다.
 *
 */
public class BuildSpecImportCommand extends AbstractAIFCommand {

	BuildSpecImportDialog dialog = null;
	Registry registry = null;
	
	@Override
	protected void executeCommand() throws Exception {
		if( dialog != null ){
			dialog.dispose();
		}
		registry = Registry.getRegistry(this);
		
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		TCComponentBOMLine selectedLine = null;
		
		//하나도 선택되어 있지않으면 리턴.
		if( coms == null || coms.length < 1){
			return;
		}
		if( coms[0] instanceof TCComponentBOMLine){
			selectedLine = (TCComponentBOMLine)coms[0];
			TCComponentBOMLine topLine = selectedLine.window().getTopBOMLine();
			String type = topLine.getItem().getType();
			if( !type.equals(TcDefinition.VARIANT_ITEM_TYPE)){
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.isNotTop"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			
			//선택한 BOM line이 Variant타입이 아니면 리턴.
			type = selectedLine.getItem().getType();
			if( !type.equals(TcDefinition.VARIANT_ITEM_TYPE)){
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.invalidItemType"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			
			OptionManager manager = null;
			HashMap<String, VariantOption> optionMap = new HashMap<String, VariantOption>();
			
			manager = new OptionManager(selectedLine, true);
			Vector<String[]> userDefineErrorList = new Vector<String[]>();
			Vector<String[]> moduleConstratintsList = new Vector<String[]>();
			//Option manager에서 설정된 옵션 정보를 가져온다.
			ArrayList<VariantOption> optionSet = manager.getOptionSet(selectedLine, userDefineErrorList, moduleConstratintsList, true);
			for( int i = 0; optionSet !=null && i < optionSet.size(); i++){
				optionMap.put(optionSet.get(i).getOptionName(), optionSet.get(i));
			}
			
			dialog = new BuildSpecImportDialog(selectedLine, optionMap);
			setRunnable(dialog);
//			dialog.setVisible(true);
		}		
		
		super.executeCommand();
	}

}
