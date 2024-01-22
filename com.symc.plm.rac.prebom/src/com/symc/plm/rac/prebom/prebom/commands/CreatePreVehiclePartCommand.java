/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.commands;

import java.util.HashMap;

import com.symc.plm.rac.prebom.prebom.dialog.prevehiclepart.PreVehiclePartCreateDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * @author jinil
 *
 */
public class CreatePreVehiclePartCommand extends AbstractAIFCommand {
    @Override
    protected void executeCommand() throws Exception {
        PreVehiclePartCreateDialog dialog = new PreVehiclePartCreateDialog(AIFUtility.getActiveDesktop().getShell(), new HashMap<String, Object>());
        dialog.open();
    }
}
