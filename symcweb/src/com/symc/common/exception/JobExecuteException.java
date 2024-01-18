package com.symc.common.exception;

@SuppressWarnings("serial")
public class JobExecuteException extends Exception {
    public JobExecuteException(Throwable t) {
        super(t);
    }

    public JobExecuteException(String se) {
        super(se);
    }
}
