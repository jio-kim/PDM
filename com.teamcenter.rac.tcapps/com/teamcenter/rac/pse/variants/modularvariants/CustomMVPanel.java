package com.teamcenter.rac.pse.variants.modularvariants;

import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.variants.VariantPanel;
import com.teamcenter.rac.psebase.AbstractBOMLineViewerApplication;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;

@SuppressWarnings("serial")
public class CustomMVPanel extends VariantPanel {

	@SuppressWarnings("unused")
	private AbstractBOMLineViewerApplication abstractbomlineviewerapplication = null;
	
	public CustomMVPanel(AbstractBOMLineViewerApplication abstractbomlineviewerapplication, BOMTreeTable bomtreetable, boolean flag) throws TCException  {
		super(bomtreetable, flag);
		this.abstractbomlineviewerapplication = abstractbomlineviewerapplication;
//		removeAll();
//		setLayout(new BorderLayout());
//		add("Nothing Selected", new NoSelectionPanel());
//        add("Incompatible BOM Line", new IncompatibleBomLinePanel());
//        authoringPanel = new AuthoringPanel();
//        add("MV Authoring", authoringPanel);
	}

	
	@Override
	public void abandonEdits() {
		System.out.println("abandonEdits");
	}

	@Override
	public boolean isModified() {
		System.out.println("isModified");
		return false;
	}

	@Override
	public void save() {
		System.out.println("save");
	}

	@Override
	protected void selectionHasChanged() {
		System.out.println("selectionHasChanged");
	}

	@Override
	public boolean setBomLine(TCComponentBOMLine tccomponentbomline) {
		System.out.println("setBomLine");
		return false;
	}

	@Override
	public void setHidden(boolean flag) {
		System.out.println("setHidden");
	}
	
	public static void deleteOption( TCVariantService varService, TCComponentBOMLine bomLine, OVEOption oveoption) throws TCException{
        varService.lineDeleteOption(bomLine, oveoption.id);
	}
	
	public static void changeOption( TCVariantService varService, TCComponentBOMLine bomLine, OVEOption oveoption, String s) throws TCException{
        varService.lineChangeOption(bomLine, oveoption.id, s);
	}
	
	public static void changeOption( TCVariantService varService, TCComponentBOMLine bomLine, int oveOptionId, String s) throws TCException{
        varService.lineChangeOption(bomLine, oveOptionId, s);
	}
	
	public static OVEOption getOveOption(TCComponentBOMLine line, HashMap<Integer, OVEOption> options, ModularOption modularoption) throws TCException{
		String[] as = null;
		String s = null;
		
		if(line == null)
            return null;
        s = line.getProperty("bl_item_item_id");
        
		TCVariantService varService = line.getSession().getVariantService();
        OVEOption oveoption = new OVEOption();
        boolean flag = !modularoption.defaultValue.isEmpty();
        oveoption.option = varService.createOveOption(s, modularoption.optionName, modularoption.optionDescription, modularoption.optionScope, modularoption.optionType, modularoption.optionValueType, modularoption.basedOnOption.basedOptionId, flag);
        oveoption.defaultValue = modularoption.defaultValue;
        oveoption.id = modularoption.optionId;
        oveoption.isFullyEnumerated = true;
        OVEOption oveoption1 = null;
        if(modularoption.optionType != 0)
        {
            if(options.containsKey(Integer.valueOf(modularoption.basedOnOption.basedOptionId)))
            {
                oveoption.option.basedOn = modularoption.basedOnOption.basedOptionId;
            } else
            {
                oveoption1 = new OVEOption();
                oveoption1.id = modularoption.basedOnOption.basedOptionId;
                oveoption1.option = varService.createOveOption(modularoption.basedOnOption.owningModuleKey, modularoption.basedOnOption.owningOptionName, "", 0, 0, modularoption.basedOnOption.basedOnType, 0, false);
            }
            if(modularoption.optionType == 1)
                oveoption.presentsPath = modularoption.basedOnOption.path;
        }
        
        // String 타입만 사용하므로, 다른 타입입 경우 에러 발생.
        if( oveoption.option.valueType != TCVariantService.OVE_STRING){
        	throw new TCException("Invalid Option Type.");
        }
        
        oveoption.stringVals = varService.createStringsAndDefault(modularoption.allowedValues, modularoption.defaultValue);
        oveoption.hasStringValues = true;
        if(oveoption.stringVals.values.length == 0)
            oveoption.isFullyEnumerated = false;
        else
            as = oveoption.stringVals.values;
        
        oveoption.comboValues = as;		
        synchronized(options)
        {
            if(oveoption1 != null && !options.containsKey(Integer.valueOf(oveoption1.id)))
                options.put(new Integer(oveoption1.id), oveoption1);
            options.put(new Integer(oveoption.id), oveoption);
        }
        
        return oveoption;
	}
}
