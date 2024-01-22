/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.commands;

import java.util.HashMap;

import com.symc.plm.rac.prebom.prebom.dialog.preproject.PreProjectCreateDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * @author jinil
 *
 */
public class CreatePreProjectCommand extends AbstractAIFCommand {
    @Override
    protected void executeCommand() throws Exception {
        PreProjectCreateDialog dialog = new PreProjectCreateDialog(AIFUtility.getActiveDesktop().getShell(), new HashMap<String, Object>());
        dialog.open();
    }
}
