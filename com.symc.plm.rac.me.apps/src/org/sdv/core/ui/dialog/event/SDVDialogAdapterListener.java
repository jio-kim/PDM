/**
 * 
 */
package org.sdv.core.ui.dialog.event;

import org.sdv.core.common.IDialogListener;

/**
 * Class Name : SDVDialogAdapterListener
 * Class Description : 
 * @date 2013. 10. 2.
 *
 */
public class SDVDialogAdapterListener implements IDialogListener {

    /* (non-Javadoc)
     * @see com.symc.plm.me.sdv.ui.dialog.event.ISimpleDialogListener#dialogStatChanged(com.symc.plm.me.sdv.ui.dialog.event.SimpleDialogEvent)
     */
    @Override
    public void dialogStatChanged(SDVDialogStatEvent t) {
        //Object o = t.getSource();
        System.out.printf("Dialog UI Event Code ..... " + t.getCurrentState() + "\n");        
    }
}

