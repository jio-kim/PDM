package com.symc.plm.me.sdv.operation.ps;

import java.awt.Frame;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;

import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFApplication;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.workflow.commands.newprocess.NewProcessCommand;

public class SDVNewProcessCommand extends AbstractAIFCommand {

    public TCSession session;
    public final Frame parentFrame;
    public InterfaceAIFComponent[] targetArray;
    private static final Logger logger = Logger.getLogger(NewProcessCommand.class);
    private InterfaceAIFComponent[] targetComps;

    private SDVNewProcessDialog dialog;

    public SDVNewProcessCommand(Frame paramFrame, AbstractAIFApplication paramAbstractAIFApplication, String templatePrefName) {
        this(paramFrame, paramAbstractAIFApplication, null, templatePrefName);
    }

    public SDVNewProcessCommand(Frame paramFrame, AbstractAIFApplication paramAbstractAIFApplication, InterfaceAIFComponent[] paramArrayOfInterfaceAIFComponent, final String templatePrefName) {
        InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = paramArrayOfInterfaceAIFComponent;
        this.parentFrame = paramFrame;
        if (arrayOfInterfaceAIFComponent != null)
            this.targetComps = arrayOfInterfaceAIFComponent;
        else
            this.targetComps = getSelectedTargetComponents();
        try {
            this.session = CustomUtil.getTCSession();
        } catch (Exception localException1) {
            logger.error(localException1.getClass().getName(), localException1);
        }
        try {
            TCComponentProcessType localTCComponentProcessType = (TCComponentProcessType) this.session.getTypeComponent("EPMJob");
            this.targetArray = localTCComponentProcessType.validateProposedAttachments(this.targetComps, 1);
        } catch (Exception localException2) {
            MessageBox.post(paramFrame, localException2);
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog = new SDVNewProcessDialog(parentFrame, SDVNewProcessCommand.this, templatePrefName);
//                AbstractTCCommandDialog localAbstractTCCommandDialog = (AbstractTCCommandDialog) localRegistry.newInstanceFor("newProcessDialog", new Object[] { NewProcessCommand.this });
                if (dialog != null) {
                    SDVNewProcessCommand.this.setRunnable(dialog);
                    try {
                        SDVNewProcessCommand.this.executeModeless();
                    } catch (Exception localException) {
                        localException.printStackTrace();
                    }
                } else {
//                    MessageBox.post(this.val$parent, localRegistry.getString("failFindDialog"), localRegistry.getString("error.TITLE"), 1);
                }
            }
        });
    }

    public static InterfaceAIFComponent[] getSelectedTargetComponents() {
        InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = new InterfaceAIFComponent[0];
        ISelectionService localISelectionService = null;
        IWorkbenchWindow localIWorkbenchWindow = PlatformHelper.getCurrentWorkbenchWindow();
        if (localIWorkbenchWindow != null) {
            localISelectionService = localIWorkbenchWindow.getSelectionService();
            ISelection localISelection = localISelectionService.getSelection();
            arrayOfInterfaceAIFComponent = SelectionHelper.getTargetComponents(localISelection);
        }
        return arrayOfInterfaceAIFComponent;
    }

    public SDVNewProcessDialog getDialog() {
        return this.dialog;
    }

}
