package com.kgm.common.remote;


@SuppressWarnings("serial")
public class DataTypeException extends Exception
{

    public DataTypeException()
    {
        super("Wrong instance type");
    }
}
