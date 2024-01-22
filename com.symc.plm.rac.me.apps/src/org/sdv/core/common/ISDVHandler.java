package org.sdv.core.common;

import org.eclipse.core.commands.ExecutionEvent;
import org.sdv.core.common.exception.SDVException;



public interface ISDVHandler {

    void beforeExecuteCommand(ExecutionEvent event) throws SDVException;
    
    void executeCommand(ExecutionEvent event) throws SDVException;

    void afterExecuteCommand(ExecutionEvent event) throws SDVException;  
    
    void validateCommand(ExecutionEvent event) throws SDVException;
}
