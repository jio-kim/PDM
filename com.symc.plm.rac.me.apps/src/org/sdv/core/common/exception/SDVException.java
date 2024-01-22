/**
 * 
 */
package org.sdv.core.common.exception;

/**
 * Class Name : SDVException
 * Class Description : 
 * @date 2013. 9. 24.
 *
 */
public class SDVException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public SDVException(String error) {
        super(error);
    }
    
    public SDVException(String error, Throwable throwable) {
        super(error, throwable);
    }

}
