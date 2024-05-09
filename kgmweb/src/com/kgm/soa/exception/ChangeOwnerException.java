package com.kgm.soa.exception;

@SuppressWarnings("serial")
public class ChangeOwnerException extends Exception {
    public ChangeOwnerException(Throwable cause) {
        super(cause);
    }
    
    public ChangeOwnerException(String msg) {
        super(msg);
    }
}
