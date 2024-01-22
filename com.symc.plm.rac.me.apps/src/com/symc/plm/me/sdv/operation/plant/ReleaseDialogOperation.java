package com.symc.plm.me.sdv.operation.plant;

import org.apache.log4j.Logger;
import org.sdv.core.common.IDialogOpertation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.operation.ps.SDVNewProcessCommand;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : ReleaseDialogOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public class ReleaseDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    private static final Logger logger = Logger.getLogger(CreateStationDialogOperation.class);
    private Registry registry = Registry.getRegistry(this);

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            InterfaceAIFComponent[] selectedTargets = AIFUtility.getCurrentApplication().getTargetComponents();
            
            /* 선택 대상 Validation */
            for(InterfaceAIFComponent selectedTarget : selectedTargets) {
                TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) selectedTarget;
                
                //Alternative Plant는 Release하지 않는다.
                boolean altPlant = tcComponentBOMLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);
                if(altPlant) {
                    throw new Exception(selectedTarget + " is Alternative Plant.\nAlternative Plant cannot release.");
                }
                
                //이미 Release된 것은 오류 처리
                String releaseStatusList = tcComponentBOMLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                if(releaseStatusList.length() > 0) {
                    throw new Exception(selectedTarget + " has already been released.");
                }
            }
            
            if (selectedTargets != null && selectedTargets.length > 0) {
                new SDVNewProcessCommand(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication(), selectedTargets, registry.getString("Workflow_Template"));
            }
        } catch (Exception exception) {
            logger.error(exception.getClass().getName(), exception);
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), exception.getMessage(), "ERROR", MessageBox.ERROR);
        }
    }

}
