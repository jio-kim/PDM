/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.exception;

/**
 * Class Name : ValidateException
 * Class Description : Execute Exception
 * 
 * @date 2013. 11. 26.
 * 
 */
@SuppressWarnings("serial")
public class StopException extends Exception {
    public StopException(String massage) {
        super(massage);
    }

    public StopException(String massage, Throwable throwable) {
        super(massage, throwable);
    }
}
