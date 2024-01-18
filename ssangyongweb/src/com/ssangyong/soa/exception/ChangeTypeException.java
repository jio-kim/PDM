package com.ssangyong.soa.exception;

@SuppressWarnings("serial")
public class ChangeTypeException extends Exception {
    public ChangeTypeException(Throwable cause) {
        super(cause);
    }
    
    public ChangeTypeException(String msg) {
        super(msg);
    }
}
