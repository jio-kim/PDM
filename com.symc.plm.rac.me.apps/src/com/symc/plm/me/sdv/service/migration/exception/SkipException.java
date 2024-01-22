/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.exception;

/**
 * Class Name : SkipException
 * Class Description : Skip Exception
 * 
 * @date 2013. 11. 26.
 * 
 */
@SuppressWarnings("serial")
public class SkipException extends Exception {
    private int status;
    
    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public SkipException(String massage) {
        super(massage);
    }

    public SkipException(String massage, Throwable throwable) {
        super(massage, throwable);
    }
    
}
