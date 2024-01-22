/**
 * 
 */
package org.sdv.core.ui.view.event;

import org.sdv.core.common.ISDVViewListener;

/**
 * Class Name : SDVViewAdapterListener
 * Class Description : 
 * @date 2013. 10. 2.
 *
 */
public class SDVViewAdapterListener implements ISDVViewListener {

    
    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return : 
     * @see org.sdv.core.common.ISDVViewListener#viewStateChanged(org.sdv.core.ui.view.event.SDVViewStateEvent)
     */
    @Override
    public void viewStateChanged(SDVViewStateEvent evt) {
        System.out.printf("View Event Code ..... " + evt.getState() + "\n");        
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return : 
     * @see org.sdv.core.common.ISDVViewListener#viewUIInitialized(org.sdv.core.ui.view.event.SDVViewStateEvent)
     */
    @Override
    public void viewUIInitialized(SDVViewStateEvent evt) {
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return : 
     * @see org.sdv.core.common.ISDVViewListener#viewUICompleted(org.sdv.core.ui.view.event.SDVViewStateEvent)
     */
    @Override
    public void viewUICompleted(SDVViewStateEvent evt) {
    }
}

