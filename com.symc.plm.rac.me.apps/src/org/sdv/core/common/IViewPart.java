/**
 * 
 */
package org.sdv.core.common;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.ui.UIManager;

/**
 * Class Name : IViewPart
 * Class Description :
 * 
 * @date 2013. 10. 11.
 * 
 */
public interface IViewPart extends IViewPane {
    UIManager getUIManager();

    void setUIManager(UIManager uiManager);

    int open();

    public Shell getShell();

}
