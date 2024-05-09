package com.kgm.commands.variantoptioncompare;

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
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
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
 * Product Ÿ���� ���� �� ����Ǿ�� �ϸ�. Product�� ������ ��� �ɼ��� ���� Variant �ɼǵ��� �����հ� ����.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings("unused")
public class OptionCompareCommand extends AbstractAIFCommand {

	public static OptionCompareDialog dialog = null;
	private OptionManager manager;
	private ArrayList<VariantOption> globalOptionSet;
	
	@Override
	protected void executeCommand() throws Exception {
		if( dialog != null ){
			dialog.dispose();
		}
		// ��� Corporate Option Item�� �ɼ� ������ ��������, globalOptionSet�� �ɼ� ������ �Է��Ѵ�.
		
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		TCComponentBOMLine selectedLine = null;
		
		if( coms == null || coms.length < 1){
			return;
		}
		if( coms[0] instanceof TCComponentBOMLine){
			selectedLine = (TCComponentBOMLine)coms[0];
			if( selectedLine.window().isModified() ){
				Registry reg = Registry.getRegistry("com.kgm.commands.variantoptioneditor.variantoptioneditor");
				MessageBox.post(AIFUtility.getActiveDesktop(), reg.getString("variant.windowSaveFirst"), "INFORMATION", MessageBox.WARNING);
				return;
			}
			final WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			AbstractAIFUIApplication aifApp = AIFUtility.getCurrentApplication();
			PSEApplicationService service = (PSEApplicationService)aifApp;
			BOMTreeTable treeTable = (BOMTreeTable)service.getAbstractViewableTreeTable();
			
			expandAll(treeTable, selectedLine, waitProgress);
			
		}
		
	}
	
	/**
	 * BOM �� ��� Ȯ��Ǿ�� ��.
	 * @param treeTable
	 * @param target
	 * @param waitProgress
	 * @throws TCException
	 */
	public void expandAll(final BOMTreeTable treeTable, final TCComponentBOMLine target, final WaitProgressBar waitProgress) throws TCException
	{

		waitProgress.setStatus("Expanding BOM", true);
		ExpandBelowOperation operation = new ExpandBelowOperation(treeTable, target == null ? treeTable.getRootBOMLineNode() : treeTable.getNode(target), 1, true);
	    operation.addOperationListener(new InterfaceAIFOperationListener(){

			@Override
			public void endOperation()
			{
		    	ArrayList<VariantOption> enableOptionSet = new ArrayList<VariantOption>();
		  		ArrayList<VariantOption> selectedLineOptionSet = new ArrayList<VariantOption>();
		    	ArrayList<VariantOption> optionSet = null;
		    	
		    	try{
					manager = new OptionManager(target, true);
					Registry reg = Registry.getRegistry("com.kgm.commands.variantoptioneditor.variantoptioneditor");
					waitProgress.setStatus(reg.getString("variant.findingCorpItem"), true);
					globalOptionSet = manager.getCorpOptionSet();
					String type = target.getItem().getType();
					
					
					//����ڰ� ������ �⺻����� �̿��Ͽ� �߰��� ��ȿ�� �˻���
					Vector<String[]> userDefineErrorList = new Vector<String[]>();
					if( type.equals(TcDefinition.PRODUCT_ITEM_TYPE) ){
						
						//Product Option Set�� �����´�.
						optionSet = manager.getOptionSet(target, userDefineErrorList, null, true);
						selectedLineOptionSet.addAll(optionSet);
						
						Vector<VariantValue> productValueList = new Vector<VariantValue>();
						for( int i = 0; i < optionSet.size(); i++){
							VariantOption option = optionSet.get(i);
							List<VariantValue> list = option.getValues();
							for( int j = 0; j < list.size(); j++){
								
								if( list.get(j).getValueStatus() != VariantValue.VALUE_USE ) 
									continue;
								
								list.get(j).setUsing(false);
								if( !productValueList.contains(list.get(j)))
									productValueList.add(list.get(j));
							}
						}
						
						//Variant Option Set�� �����´�.
						HashMap<TCComponentBOMLine, List<VariantValue>> map = new HashMap<TCComponentBOMLine, List<VariantValue>>();
						AIFComponentContext[] contexts = target.getChildren();
						for( int k = 0; contexts != null && k < contexts.length; k++){
							TCComponentBOMLine childLine = (TCComponentBOMLine)contexts[k].getComponent();
							
							if( !TcDefinition.VARIANT_ITEM_TYPE.equals(childLine.getItem().getType()))
								continue;
							
							ArrayList<VariantOption> variantOptionSet = manager.getOptionSet(childLine, userDefineErrorList, null, true);
							
							Vector<VariantValue> variantValueList = new Vector<VariantValue>();
							for( int i = 0; i < variantOptionSet.size(); i++){
								VariantOption option = variantOptionSet.get(i);
								List<VariantValue> list = option.getValues();
								for( int j = 0; j < list.size(); j++){
									if( list.get(j).getValueStatus() != VariantValue.VALUE_USE ) 
										continue;
									list.get(j).setUsing(false);
									if( !variantValueList.contains(list.get(j)))
										variantValueList.add(list.get(j));
								}
							}
							map.put(childLine, variantValueList);
						}
						dialog = new OptionCompareDialog(productValueList, map, target, manager);
						//			Dialog ���� �� ������ ��� �����츦 Close��.
						dialog.addWindowListener(new WindowAdapter(){
							
							@Override
							public void windowClosed(WindowEvent windowevent) {
								manager.clear(false);
								super.windowClosed(windowevent);
							}
							
						});
						setRunnable(dialog);
					}else{
						MessageBox.post(AIFUtility.getActiveDesktop(), "Product Type-only feature.", "INFORMATION", MessageBox.WARNING);
						return;
					}
					OptionCompareCommand.super.executeCommand();
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					waitProgress.dispose();
				}
		      }
	
		      @Override
		      public void startOperation(String arg0)
		      {
		      }

	    });
	    treeTable.getRootBOMLineNode().getSession().queueOperation(operation);
	 }
}
