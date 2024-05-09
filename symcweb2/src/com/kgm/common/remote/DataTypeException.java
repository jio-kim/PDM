package com.kgm.common.remote;


public class DataTypeException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DataTypeException()
    {
        super("Wrong instance type");
    }
}
