/**
 * 
 */
package org.sdv.core.common.message;

/**
 * Class Name : ResultMessage
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public class ResultMessage {

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAIL = -1;

    private String ownerId;
    private String message;
    private int resultCode;
    private Throwable throwable;

    /**
     * @param message
     * @param resultCode
     * @param exception
     */
    public ResultMessage(String ownerId, String message, int resultCode, Throwable throwable) {
        this.ownerId = ownerId;
        this.message = message;
        this.resultCode = resultCode;
        this.throwable = throwable;
    }

    /**
     * @param message
     * @param resultCode
     * @param exception
     */
    public ResultMessage(String ownerId, String message, int resultCode) {
        this.ownerId = ownerId;
        this.message = message;
        this.resultCode = resultCode;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the resultCode
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * @return the throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * @return the ownerId
     */
    public String getOwnerId() {
        return ownerId;
    }
    
    public boolean isSuccess() {
        return this.resultCode == RESULT_SUCCESS;
    }

}
