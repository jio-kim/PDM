/**
 * 
 */
package com.symc.plm.rac.prebom.ccn.commands;

import com.symc.plm.rac.prebom.ccn.operation.PreCCNReportOperation;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * @author JWLEE
 * 
 */
public class ReportPreCCNCommand extends AbstractAIFCommand {
    private Registry registry;

    @Override
    protected void executeCommand() throws Exception {
        registry = Registry.getRegistry(this);
        InterfaceAIFComponent[] comps = SDVPreBOMUtilities.getTargets();
        if (null != comps && comps.length == 1) {
            if (comps[0].getType().equals(TypeConstant.S7_PRECCNREVISIONTYPE)) {
                PreCCNReportOperation operation = new PreCCNReportOperation((TCComponentItemRevision)comps[0]);
                operation.executeOperation();
            } else {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("ReportPreCCNCommand.CCN.SelectedError"), "Error", MessageBox.ERROR);
            }
        } else {
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("ReportPreCCNCommand.CCN.SelectedError"), "Error", MessageBox.ERROR);
        }
    }
}
