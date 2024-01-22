/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.commands;

import java.util.HashMap;

import com.symc.plm.rac.prebom.prebom.dialog.prefuncmaster.PreFuncMasterCreateDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * @author jinil
 *
 */
public class CreatePreFuncMasterCommand extends AbstractAIFCommand {
    @Override
    protected void executeCommand() throws Exception {
        PreFuncMasterCreateDialog dialog = new PreFuncMasterCreateDialog(AIFUtility.getActiveDesktop().getShell(), new HashMap<String, Object>());
        dialog.open();
    }
}
