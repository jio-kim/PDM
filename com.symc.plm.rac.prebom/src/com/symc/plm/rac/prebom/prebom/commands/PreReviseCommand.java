/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.commands;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.prebom.dialog.revise.PreReviseDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * @author jinil
 *
 */
public class PreReviseCommand extends AbstractAIFCommand {
    private Registry registry = Registry.getRegistry("com.symc.plm.rac.prebom.prebom.dialog.revise.revise");
    private HashMap<String, Object> targetItemRevs = new HashMap<String, Object>();
    private String project_code;

    @Override
    protected void executeCommand() throws Exception {
        if (! startValidate())
            return;

        PreReviseDialog dialog = new PreReviseDialog(AIFUtility.getActiveDesktop().getShell(), targetItemRevs, project_code);
        dialog.open();
    }

    private boolean startValidate() throws Exception {
    	project_code = "";
        boolean isTargetBOMLine = false;
        boolean isCCNRequired = false;
        ArrayList<String> preTypeList = new ArrayList<String>();
        ArrayList<String> selectedTypeList = new ArrayList<String>();
        ArrayList<TCComponent> targetRevisions = new ArrayList<TCComponent>();

        preTypeList.add(TypeConstant.S7_PREPRODUCTREVISIONTYPE);
        preTypeList.add(TypeConstant.S7_PREFUNCTIONREVISIONTYPE);
        preTypeList.add(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE);
        preTypeList.add(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE);
        preTypeList.add(TypeConstant.S7_PREPROJECTREVISIONTYPE);

        try
        {
            InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();

            if (targetComponents == null || targetComponents.length == 0)
            {
                MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("PreReviseDialog.MESSAGE.NoSelectedItem"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                return false;
            }

            for (InterfaceAIFComponent targetComponent : targetComponents)
            {
                String targetType = null;
                TCComponentItemRevision targetRev = null;

                if (! (targetComponent instanceof TCComponentBOMLine || targetComponent instanceof TCComponentItemRevision))
                {
                    MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("PreReviseDialog.MESSAGE.SelectPreItemType"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                    return false;
                }

                if (targetComponent instanceof TCComponentBOMLine)
                {
                    if (targetRevisions.size() > 0 && ! isTargetBOMLine)
                    {
                        MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("PreReviseDialog.MESSAGE.SelectPreBOMLineType"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                        return false;
                    }

                    targetType = ((TCComponentBOMLine) targetComponent).getItemRevision().getType();
                    targetRev = ((TCComponentBOMLine) targetComponent).getItemRevision();
                    isTargetBOMLine = true;
                }
                else
                {
                    if (isTargetBOMLine)
                    {
                        MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("PreReviseDialog.MESSAGE.SelectPreBOMLineType"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                        return false;
                    }

                    targetType = ((TCComponent) targetComponent).getType();
                    targetRev = (TCComponentItemRevision) targetComponent;
                    isTargetBOMLine = false;
                }

                if (! preTypeList.contains(targetType))
                {
                    MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("PreReviseDialog.MESSAGE.SelectPreItemType"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                    return false;
                }

                if (selectedTypeList.size() > 0)
                {
                    if (((targetType.equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE) || targetType.equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE)) &&
                            ! (selectedTypeList.contains(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE) || selectedTypeList.contains(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE))) ||
                       (! (targetType.equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE) || targetType.equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE)) &&
                               (selectedTypeList.contains(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE) || selectedTypeList.contains(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE))))
                    {
                        MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("PreReviseDialog.MESSAGE.SelectCCNType"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                        return false;
                    }
                }

                if (! (targetRev.getItem().getLatestItemRevision().equals(targetRev)))
                {
                    MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("PreReviseDialog.MESSAGE.IsNotLatestRevision"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                    return false;
                }

                if (! CustomUtil.isReleased(targetRev))
                {
                    MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("PreReviseDialog.MESSAGE.IsNotRelease"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                    return false;
                }

                if (! selectedTypeList.contains(targetType))
                    selectedTypeList.add(targetType);

                if ((targetType.equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE) || targetType.equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE)))
                    isCCNRequired = true;

                targetRevisions.add((TCComponent) targetComponent);
            }

            if (targetRevisions != null && targetRevisions.size() > 0)
            {
                targetItemRevs.put("TargetRevisions", targetRevisions);
                targetItemRevs.put("TargetBOMLine", isTargetBOMLine);
                targetItemRevs.put("TargetCCNRequired", isCCNRequired);
            }
            
            //[CSH 20181204] CCN이 필요한 FMP에 대해 Project Code가 일치하는지 Validation 로직 추가
            //Revise 대상과 CCN Project Code가 일치하여야 함. (기술관리 송대영 책임)
            if(isCCNRequired){
            	if(!checkSameProjectCode()){
            		MessageBox.post(AIFUtility.getActiveDesktop(), "The project code of the target FMPs do not match.", registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
                    return false;
            	}
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }

        return true;
    }
    
    private boolean checkSameProjectCode() throws Exception{
    	ArrayList<TCComponent> targets = (ArrayList)targetItemRevs.get("TargetRevisions");
    	String type = "";
    	String pCode = "";
    	for(TCComponent component : targets){
    		type = component.getType();
    		if(type.equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)){
    			pCode = component.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
    			if(project_code.equals("")){
    				project_code = pCode;
    			} else {
    				if(!project_code.equals(pCode)){
    					return false;
    				}
    			}
    		}
    	}
    	return true;
    }
}
