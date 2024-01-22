/**
 * 
 */
package org.sdv.core.common;

import java.util.EventListener;

import org.sdv.core.ui.dialog.event.SDVDialogStatEvent;

/**
 * Class Name : ISDVDialogListener
 * Class Description : 
 * @date 2013. 10. 2.
 *
 */
public interface IDialogListener extends EventListener {
    public void dialogStatChanged(SDVDialogStatEvent t);
}
