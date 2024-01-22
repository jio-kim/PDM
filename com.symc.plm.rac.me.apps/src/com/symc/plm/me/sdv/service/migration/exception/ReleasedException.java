/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.exception;

import org.sdv.core.common.exception.ValidateSDVException;

/**
 * Class Name : ReleasedException
 * Class Description :
 * 릴리즈 상태일때 Exception
 * 
 * @date 2013. 11. 26.
 * 
 */
@SuppressWarnings("serial")
public class ReleasedException extends ValidateSDVException {

    /**
     * @param error
     * @param throwable
     */
    public ReleasedException(String error, Throwable throwable) {
        super(error, throwable);
    }

    /**
     * @param error
     */
    public ReleasedException(String error) {
        super(error);
    }

}
