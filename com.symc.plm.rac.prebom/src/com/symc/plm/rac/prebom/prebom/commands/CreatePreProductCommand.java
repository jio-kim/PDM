/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.commands;

import java.util.HashMap;

import com.symc.plm.rac.prebom.prebom.dialog.preproduct.PreProductCreateDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * @author jinil
 *
 */
public class CreatePreProductCommand extends AbstractAIFCommand {

    @Override
    protected void executeCommand() throws Exception {
//        AbstractAIFUIApplication aif = AIFUtility.getCurrentApplication();
//        if (aif.getClass().getSimpleName().equals("PSEApplicationService"))
//        {
//            if (aif.isSaveOnCloseNeeded())
//            {
//                int ret = aif.promptToSaveOnClose();
//                if (ret == IDialogConstants.YES_ID)
//                {
//                    // 여기에 코딩 필요한 거 할 것.
//                }
//                else if (ret == IDialogConstants.CANCEL_ID)
//                {
//                    return;
//                }
//            }
//        }

        PreProductCreateDialog dialog = new PreProductCreateDialog(AIFUtility.getActiveDesktop().getShell(), new HashMap<String, Object>());
        dialog.open();
    }
}
