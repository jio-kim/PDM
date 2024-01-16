/**
 * 
 */
package com.ssangyong.commands.prebommapping;

import org.eclipse.swt.SWT;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * @author jinil
 *
 */
public class VehPartMappingCommand extends AbstractAIFCommand {
    @Override
    protected void executeCommand() throws Exception {
        VehPartMappingDialog dialog = new VehPartMappingDialog(AIFUtility.getActiveDesktop().getShell(), SWT.NONE);
        dialog.open();
    }
}
