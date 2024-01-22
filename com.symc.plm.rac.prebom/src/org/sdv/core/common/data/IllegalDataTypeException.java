/**
 * 
 */
package org.sdv.core.common.data;

/**
 * Class Name : IllegalDataTypeException
 * Class Description : 
 * @date 2013. 10. 24.
 *
 */
public class IllegalDataTypeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 8103520721202251549L;

    public IllegalDataTypeException(){
        
    }
    
    
    public IllegalDataTypeException(String message){
        super(message);
    }
}
