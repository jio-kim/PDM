/**
 * 
 */
package com.symc.plm.me.sdv.operation.swm;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractDialogSDVOperation;

import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : ReviseSWMDocOperation
 * Class Description :
 * 
 * @date 2014. 1. 22.
 * 
 */
public class ReviseSWMDocOperation extends AbstractDialogSDVOperation {
    private Registry registry;

    /**
     * @param jobName
     */
    public ReviseSWMDocOperation() {
        this("");
    }

    /**
     * @param jobName
     */
    public ReviseSWMDocOperation(String jobName) {
        super(jobName);
    }

    @Override
    public void startOperation(String jobName) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        registry = Registry.getRegistry(this);

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();

                    for (InterfaceAIFComponent selectedTarget : selectedTargets) {
                        TCComponentItemRevision selectedComponent = (TCComponentItemRevision) selectedTarget;
                        TCComponentItemRevision LatestItemRev = selectedComponent.getItem().getLatestItemRevision();
                        if (selectedComponent != null && selectedComponent != LatestItemRev) {
                            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("SelectTargetIsNotLatestSWMDoc.MESSAGE"), "Error", MessageBox.ERROR);
                            return;
                        }
                    }

                    Shell shell = Display.getCurrent().getActiveShell();
                    IDialog dialog = UIManager.getDialog(shell, dialogId);

                    dialog.open();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
