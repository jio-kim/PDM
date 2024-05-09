package com.kgm.common.bundlework.bwutil;

import java.util.HashMap;

import com.kgm.commands.optiondefine.excel.exception.ValidationOptionCombinationException;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.ModularOptionModel;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;

public class BWOptionCombinationUtil {
    
    public static HashMap<String, String> getOptionMapList(TCComponentBOMLine functionBomLine) throws Exception {
        HashMap<String, String> optionList = new HashMap<String, String>();
        OVEOption[] options = getSetOptions(functionBomLine);
        if (options == null) {
            return optionList;
        }        
        for (int i = 0; options != null && i < options.length; i++) {
            String[] list = options[i].stringVals.values;
            for (int j = 0; list != null && j < list.length; j++) {
                // key : value / value : option[item:name]
                optionList.put(list[j], options[i].option.item + ":" + MVLLexer.mvlQuoteId(options[i].option.name, false));
            }
        }
        return optionList;
    }
    
    /**
     * Function �� ������ Set Option List�� �����´�. (Optopn Value ���� ��)
     * 
     * @method getSetOptions
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static OVEOption[] getSetOptions(TCComponentBOMLine functionBomLine) throws Exception {
        OVEOption[] setOptions = null;
        try {
            // VehiclePart -> FunctionMaster - Function
            // Function���� Set Option List�� �����´�.
            TCComponentBOMLine line = functionBomLine;
            BOMTreeTable treeTable = new BOMTreeTable(line.getSession());
            treeTable.setBOMWindow(line.window());            
            ModularOptionModel moduleModel = new ModularOptionModel(treeTable, line, true);
            int[] optionNums = moduleModel.getOptionsForModule(treeTable.getNode(line));
            if (optionNums == null) 
            {
            	setOptions = new OVEOption[0];
            	return setOptions; 
            }
            setOptions = new OVEOption[optionNums.length];
            if (optionNums != null) {
                for (int i = 0; i < optionNums.length; i++) {
                    setOptions[i] = moduleModel.getOption(optionNums[i]);
                }
            }
        } catch (ValidationOptionCombinationException ve) {
            ve.printStackTrace();
            throw ve;
        } catch (Exception e) {
            e.printStackTrace();            
            throw new Exception("Function�� ������ �ɼ� ������ Ȯ�ιٶ��ϴ�.");
        }
        return setOptions;
    }

    /**
     * Option ���� ����
     * 
     * @method saveCondition
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void saveCondition(SYMCBOMLine saveBomLine, String strCondition) throws TCException {
        // TCVariantService svc = session.getVariantService();
        // String oldVC =
        // vehiclePart.getTccomponentbomline().getProperty("bl_variant_condition");
        // svc.setLineMvlCondition(vehiclePart.getTccomponentbomline(),
        // vehiclePart.getStrCondition());
        saveBomLine.setMVLCondition(strCondition);
    }
}
