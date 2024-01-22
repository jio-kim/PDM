/**
 * 
 */
package org.sdv.core.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IDialogListener;
import org.sdv.core.ui.dialog.event.SDVDialogStatEvent;

/**
 * Event 관련 추가 확장 Dialog (IDialog 구현)
 * 
 * Class Name : AbstractSDVDialog
 * Class Description :
 * 
 * @date 2013. 10. 16.
 * 
 */
public abstract class AbstractSDVEventDialog extends Dialog implements IDialog {
    
    private static final Logger logger = Logger.getLogger(AbstractSDVEventDialog.class);
    
    protected List<IDialogListener> dialogListeners;
    protected List<ShellListener> shellListeners;
    

    /**
     * @param paramShell
     */
    public AbstractSDVEventDialog(Shell paramShell) {
        super(paramShell);
        dialogListeners = new ArrayList<IDialogListener>();
        shellListeners = new ArrayList<ShellListener>();
        
        if(logger.isDebugEnabled())logger.debug("AbstractSDVEventDialog <initilzed>" );
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.IDialog#addDialogListener(org.sdv.core.common.IDialogListener)
     */
    public void addDialogListener(IDialogListener listner) {
        if(!dialogListeners.contains(listner)){
            this.dialogListeners.add(listner);
        }
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.IDialog#removeDialogListener(org.sdv.core.common.IDialogListener)
     */
    public void removeDialogListener(IDialogListener listner) {
        if(dialogListeners.contains(listner)){
            this.dialogListeners.remove(listner);
        }
    }    
    
    
    
    /**
     * UI Init Event Fire..
     * 
     * @method fireUiCompleteEvent
     * @date 2013. 10. 2.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void fireUIInitEvent() {
        final SDVDialogStatEvent e = new SDVDialogStatEvent(this, SDVDialogStatEvent.EVTENT_STAT_INIT);
        for (IDialogListener dialogListener : dialogListeners) {
            dialogListener.dialogStatChanged(e);
        }
    }

    /**
     * UI Complete Event Fire..
     * 
     * @method fireUiCompleteEvent
     * @date 2013. 10. 2.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void fireUICompleteEvent() {
        SDVDialogStatEvent e = new SDVDialogStatEvent(this, SDVDialogStatEvent.EVENT_STAT_COMPLETE);
        for (IDialogListener dialogListener : dialogListeners) {
            dialogListener.dialogStatChanged(e);
        }
    }
    
    

    /* (non-Javadoc)
     * @see org.sdv.core.common.IDialog#addShellListener(org.eclipse.swt.events.ShellListener)
     */
    public void addShellListener(ShellListener listner) {
        if(!shellListeners.contains(listner)){
            this.shellListeners.add(listner);
        }
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.IDialog#removeShellListener(org.eclipse.swt.events.ShellListener)
     */
    public void removeShellListener(ShellListener listner) {
        if(shellListeners.contains(listner)){
            this.shellListeners.remove(listner);
        }
    }

    

}
