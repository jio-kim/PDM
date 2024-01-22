/**
 * 
 */
package org.sdv.core.common.exception;

/**
 * Class Name : ExecuteSDVException
 * Class Description : ExecuteSDVException
 * 
 * @date 2013. 11. 26.
 * 
 */
@SuppressWarnings("serial")
public class ExecuteSDVException extends SDVException {
    public ExecuteSDVException(String massage) {
        super(massage);
    }

    public ExecuteSDVException(String massage, Throwable throwable) {
        super(massage, throwable);
    }
}
