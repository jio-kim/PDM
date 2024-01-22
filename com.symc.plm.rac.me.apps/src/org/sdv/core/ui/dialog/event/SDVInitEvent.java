/**
 * 
 */
package org.sdv.core.ui.dialog.event;

import java.util.EventObject;

import org.sdv.core.common.data.IDataSet;

/**
 * Class Name : SDVInitEvent
 * Class Description : 
 * @date 	2013. 11. 28.
 * @author  CS.Park
 * 
 */
public class SDVInitEvent extends EventObject{

    private static final long serialVersionUID = 8691124285348322288L;
    
    public static final int INIT_SUCCESS = 1;
    public static final int INIT_FAILED = -1;
    
    private String id;
    private IDataSet data;
    private int result;
    private Throwable error;
    
    public SDVInitEvent(Object source, String id, IDataSet data){
        this(source, INIT_SUCCESS, id, data, null);
    }
    
    
    public SDVInitEvent(Object source, int result, String id, IDataSet data){
        this(source, result, id, data, null);
    }
    
    
    public SDVInitEvent(Object source, int result, String id, IDataSet data, Throwable th){
        super(source);
        this.result = result;
        this.id =id;
        this.data = data;
        this.error = th;
    }


    /**
     * @return the result
     */
    public int getResult() {
        return result;
    }


    /**
     * @return the error
     */
    public Throwable getError() {
        return error;
    }


    public String getId() {
        return id;
    }


    public IDataSet getData() {
        return data;
    }
    
}
