package com.kgm.commands.conditionmapper;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;

import com.kgm.common.SYMCClass;
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
 * �ɼ� ���ս�, ����� ���� �ش��ϴ� ������ ����
 * 
 * @author slobbie
 *
 */
public class AutoConditionMapperCommand extends AbstractAIFCommand {

	private OptionManager manager;

	@SuppressWarnings("unused")
    @Override
	protected void executeCommand() throws Exception {
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		ArrayList<VariantOption> enableOptionSet = new ArrayList<VariantOption>();
		ArrayList<VariantOption> selectedLineOptionSet = new ArrayList<VariantOption>();
		TCComponentBOMLine selectedLine = null;
		
		if( coms == null || coms.length < 1){
			return;
		}
		
		Registry registry = Registry.getRegistry("com.kgm.commands.variantconditionset.variantconditionset");
		
		if( coms[0] instanceof TCComponentBOMLine){
			
			ArrayList<VariantOption> optionSet = null;
			selectedLine = (TCComponentBOMLine)coms[0];
			TCComponentBOMLine parent = selectedLine.parent();
			if( parent == null){
				return;
			}
			
			manager = new OptionManager(selectedLine, true);
			String type = selectedLine.getItem().getType();
			
			//����ڰ� ������ �⺻����� �̿��Ͽ� �߰��� ��ȿ�� �˻���
			Vector<String[]> userDefineErrorList = new Vector<String[]>();
			if ( type.equals(SYMCClass.S7_VEHPARTTYPE) || type.equals(SYMCClass.S7_STDPARTTYPE)){
				
				TCComponentBOMLine sParent = selectedLine.parent();
				String parentType = sParent.getItem().getType();
				optionSet = manager.getOptionSet(sParent,null, null, null, false, false);
				while( sParent != null && ( optionSet == null || optionSet.isEmpty())){
					sParent = sParent.parent();
					if( sParent == null ) break;
					
					parentType = sParent.getItem().getType();
					optionSet = manager.getOptionSet(sParent,null, null, null, false, false);
					
					if( parentType.equals(TcDefinition.FUNCTION_ITEM_TYPE))
						break;
				}
				
				enableOptionSet.addAll(optionSet);
				AutoConditionMapperDialog dialog = new AutoConditionMapperDialog(enableOptionSet, selectedLine, sParent);
				dialog.addWindowListener(new WindowAdapter(){
					
					@Override
					public void windowClosed(WindowEvent windowevent) {
						manager.clear(false);
						super.windowClosed(windowevent);
					}
					
				});
				setRunnable(dialog);
//				dialog.setVisible(true);
			
			}else{
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.invalidItemType"), "INFORMATION", MessageBox.WARNING);
				return;
				
			}
			
		}else{
			MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.invalidItemType"), "INFORMATION", MessageBox.WARNING);
			return;
		}
		
		super.executeCommand();
	}

}
