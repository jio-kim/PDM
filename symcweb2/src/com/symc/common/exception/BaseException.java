package com.symc.common.exception;

public class BaseException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public BaseException(Throwable t) {
        super(t);
    }

    public BaseException(String msg) {
        super(msg);
    }
}
