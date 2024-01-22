/**
 * 
 */
package org.sdv.core.common.exception;

/**
 * Class Name : SDVRuntimeException
 * Class Description : 
 * @date 2013. 11. 21.
 *
 */
public class SDVRuntimeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 710221941501239617L;

    /**
     * 
     */
    public SDVRuntimeException() {
        
    }

    /**
     * @param arg0
     */
    public SDVRuntimeException(String message) {
        super(message);
    }

    /**
     * @param arg0
     */
    public SDVRuntimeException(Throwable th) {
        super(th);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public SDVRuntimeException(String message, Throwable th) {
        super(message, th);
    }

}
