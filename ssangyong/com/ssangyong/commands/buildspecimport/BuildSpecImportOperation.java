package com.ssangyong.commands.buildspecimport;

import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.WaitProgressBar;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCComponentVariantRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.sosvi.SelectedOptionSetDialog;

/**
 * @author slobbie
 * Build Spec(SOS)을 생성하기위한 Operation
 */
public class BuildSpecImportOperation extends AbstractAIFOperation {
	
	private TCComponentBOMLine target = null;
	private String projectName = null;
	private HashMap<String,HashMap<String, String>> specMap = null;
	private WaitProgressBar waitProgress = null;
	private String[] selectedSpec = null;
	
	public BuildSpecImportOperation(TCComponentBOMLine target, String projectName, String[] selectedSpec, HashMap<String, HashMap<String, String>> specMap, WaitProgressBar waitProgress){
		this.target = target;
		this.projectName = projectName;
		this.selectedSpec = selectedSpec;
		this.specMap = specMap;
		this.waitProgress = waitProgress;
	}
	
	@Override
	public void executeOperation() throws Exception {
		try{
			createStoredOptionSet(target);
			waitProgress.setShowButton(true);
			waitProgress.setStatus("Build Spec creation complete.");
		}catch(Exception e){
			waitProgress.setShowButton(true);
			waitProgress.setStatus(e.getMessage());
		}
	}

	/**
	 * Stored Option Set을 생성하고 저장함.
	 * @param line
	 * @throws TCException
	 */
	@SuppressWarnings("unused")
	private void createStoredOptionSet(TCComponentBOMLine line) throws TCException{
		String variantId = line.getItem().getProperty("item_id").toUpperCase();

		waitProgress.setStatus("The previous Build Spec is deleted.");
		TCComponentItemRevision tRevision = line.getItemRevision();
		AIFComponentContext[] context =  tRevision.getChildren("IMAN_reference");
		for( int j = 0; context != null && j < context.length; j++){
			TCComponent com =  (TCComponent)context[j].getComponent();
			String comType = com.getType();
			if( comType.equals("StoredOptionSet")){
				String comName = com.getProperty("object_name");
				
				for( String specStr : selectedSpec){
					
					if( specStr.indexOf("|") > -1 ){
						specStr = specStr.substring(0, specStr.indexOf("|")).trim();
					}else{
						specStr = specStr.trim();
					}		
					
					if( comName.equals(specStr)){
						try{
							tRevision.remove("IMAN_reference", com);
							com.delete();
						}catch( TCException tce){
							
							line.getSession().getUser().getNewStuffFolder().add("contents", com);
							throw tce;
						}
					}
					
				}
				
			}
		}
		
		waitProgress.setStatus("Creating a Stored Option Set.");
		TCComponentBOMWindow window = null;
		SelectedOptionSetDialog sosDlg = null;
		try{
			TCSession session = line.getSession();
			TCComponentBOMWindowType winType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		    TCComponentRevisionRuleType tccomponentrevisionruletype = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
			window = winType.create(tccomponentrevisionruletype.getDefaultRule());
			TCComponentBOMLine newTopLine = window.setWindowTopLine(null, line.getItemRevision(), null, null);
			sosDlg = new SelectedOptionSetDialog(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication(), newTopLine);
			for( String specStr : selectedSpec){
				
				if( specStr.indexOf("|") > -1 ){
					specStr = specStr.substring(0, specStr.indexOf("|")).trim();
				}else{
					specStr = specStr.trim();
				}				
				
				waitProgress.setStatus("Creating " + specStr);
				sosDlg.setValue(line, specMap.get(specStr));
				
				TCVariantService variantService = newTopLine.getSession().getVariantService();
				TCComponent sosComponent = variantService.getSos(newTopLine);
//				TCComponentVariantRule parentVariantRule = newTopLine.window().askVariantRule();
				TCComponentVariantRule parentVariantRule = null;
				List<TCComponentVariantRule> parentVariantRules = newTopLine.window().askVariantRules();
				if(parentVariantRules != null && parentVariantRules.size() > 0) {
					parentVariantRule = parentVariantRules.get(0);
				}
				
				TCComponentVariantRule legacyVariantRule = parentVariantRule.copy();
				TCComponent tccomponent = variantService.createVariantConfig(
						legacyVariantRule, new TCComponent[] {  sosComponent });
				TCComponent tccomponent1;
				try {
					tccomponent1 = variantService.writeStoredConfiguration(specStr, tccomponent);
					tccomponent1.setStringProperty("object_desc", "Build Spec Option");
					tccomponent1.setProperty("s7_BUILDSPEC", "Y");
					tccomponent1.setProperty("s7_PROJECT_CODE", projectName);
					tccomponent1.save();
				} catch (TCException tcexception) {
					variantService.deleteVariantConfig(tccomponent);
					throw tcexception;
				}
				variantService.deleteVariantConfig(tccomponent);
				TCComponentItemRevision tccomponentitemrevision = newTopLine.getItemRevision();
				try{
					//리비전에 붙이기 실패시 SOS 삭제.
					tccomponentitemrevision.add("IMAN_reference", tccomponent1);
				}catch(TCException e){
					tccomponent1.delete();
					throw e;
				}
			}
			
		}catch(TCException e){
			e.printStackTrace();
			throw e;
		}finally{
			window.close();
			sosDlg.dispose();
		}
		
	}	
}
