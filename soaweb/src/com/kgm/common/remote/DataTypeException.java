package com.kgm.common.remote;

public class DataTypeException extends Exception
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2838584729608279099L;

	public DataTypeException()
    {
        super("Wrong instance type");
    }
}
