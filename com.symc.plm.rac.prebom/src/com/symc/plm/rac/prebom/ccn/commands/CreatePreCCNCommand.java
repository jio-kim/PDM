/**
 * 
 */
package com.symc.plm.rac.prebom.ccn.commands;

import java.util.HashMap;

import com.symc.plm.rac.prebom.ccn.dialog.PreCCNCreateDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * @author JWLEE
 * 
 */
public class CreatePreCCNCommand extends AbstractAIFCommand {

    @Override
    protected void executeCommand() throws Exception {
        PreCCNCreateDialog dialog = new PreCCNCreateDialog(AIFUtility.getActiveDesktop().getShell(), new HashMap<String, Object>());
        dialog.open();
    }
}
