/**
 * 
 */
package org.sdv.core.common.exception;


/**
 * Class Name : ValidateSDVException
 * Class Description : 
 * @date 2013. 9. 24.
 *
 */
public class ValidateSDVException extends SDVException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public ValidateSDVException(String error) {
        super(error);
    }
    
    public ValidateSDVException(String error, Throwable throwable) {
        super(error, throwable);
    }

}
