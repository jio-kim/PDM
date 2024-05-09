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
 * H-BOM�� �������̽� �Ͽ� ��꺽�� �����ϴ� Spec(O-Spec �ƴ�)�� ������ ��
 * ���ο� SOS�� �����Ѵ�.
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
		
		//�ϳ��� ���õǾ� ���������� ����.
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
			
			//������ BOM line�� VariantŸ���� �ƴϸ� ����.
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
			//Option manager���� ������ �ɼ� ������ �����´�.
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
