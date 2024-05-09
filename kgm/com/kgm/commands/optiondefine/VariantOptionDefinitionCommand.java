package com.kgm.commands.optiondefine;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;

import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.variant.OptionManager;
import com.kgm.common.utils.variant.VariantOption;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Corporate Option�� 
 * ���ǵ� �ɼ� ī�װ��� ���ο� �ɼ��� �߰� �� ���� ������ �� �� �ִ�.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"unused"})
public class VariantOptionDefinitionCommand extends AbstractAIFCommand {

	private OptionManager manager;
	private static VariantOptionDefinitionDialog dialog = null;
	
    @Override
	protected void executeCommand() throws Exception {
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		ArrayList<VariantOption> optionSet = null;
		
		if( coms[0] instanceof TCComponentBOMLine){
			
			final TCComponentBOMLine targetLine = (TCComponentBOMLine)coms[0];
			TCComponentItem item = (TCComponentItem)targetLine.getItem();
			String type = targetLine.getItem().getType();
			
			//Coporate Option Type �� ����� ���� �� �� �ִ�.
			if( type.equals("S7_CorpOption")){
				
				WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
				waitProgress.start();
				waitProgress.setStatus("The defined options is loading.. ");
				manager = new OptionManager(targetLine, true);
				Vector<String[]> userDefineErrorList = new Vector<String[]>();
				Vector<String[]> moduleConstraintList = new Vector<String[]>();
				
				if( dialog != null){
					dialog.dispose();
				}
				optionSet = manager.getOptionSet(targetLine, userDefineErrorList, moduleConstraintList, true);
				dialog = new VariantOptionDefinitionDialog(targetLine, optionSet, manager);
				dialog.addWindowListener(new WindowAdapter(){
					@Override
					public void windowClosed(WindowEvent windowevent) {
						manager.clear(false);
						super.windowClosed(windowevent);
					}
				});
				setRunnable(dialog);
				waitProgress.close();
				
			}else{
				Registry registry = Registry.getRegistry(this);
				MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("OptionDefine.onlyCorpItem"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			
		}
		
		super.executeCommand();
	}

}
