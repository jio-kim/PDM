/**
 * 
 */
package org.sdv.core.ui.dialog.event;

/**
 * Class Name : ISDVInitOperationListener
 * Class Description : 
 * @date 	2013. 11. 28.
 * @author  CS.Park
 * 
 */
public interface ISDVInitOperationListener {

    /**
     * 
     * @method willInitalize 
     * @date 2013. 11. 29.
     * @author CS.Park
     * @param
     * @return void
     * @throws
     * @see
     */
    public void willInitalize(SDVInitEvent sdvInitEvent);

    /**
     * 
     * @method failInitalize 
     * @date 2013. 11. 29.
     * @author CS.Park
     * @param
     * @return void
     * @throws
     * @see
     */
    public void failInitalize(SDVInitEvent sdvInitEvent);

}
