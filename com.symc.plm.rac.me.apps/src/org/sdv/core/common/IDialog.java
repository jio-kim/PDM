/**
 * 
 */
package org.sdv.core.common;

import org.eclipse.jface.window.WindowManager;
import org.sdv.core.ui.DialogManager;

/**
 * Class Name : ISDVDialog
 * Class Description : 
 * @date 2013. 10. 11.
 *
 */
public interface IDialog extends IViewPane {
    
    int open();
    
    IViewPane getView(String viewId);
    
    DialogManager getDialogManager();
    
    void setWindowManager(WindowManager wm);
}
