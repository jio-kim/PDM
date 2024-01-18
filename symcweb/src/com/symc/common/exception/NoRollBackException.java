package com.symc.common.exception;

public class NoRollBackException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NoRollBackException(Throwable t) {
        super(t);
    }
}
