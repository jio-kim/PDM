/**
 * 
 */
package org.sdv.core.ui.view.event;

import java.util.EventObject;

import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataSet;

/**
 * Class Name : SDVViewStateEvent
 * Class Description :
 * 
 * @date 2013. 10. 2.
 * 
 */
public class SDVViewStateEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    
    public static final int EVENT_STATE_CHANGED = 0;
    public static final int EVENT_STATE_INIT = 1;
    public static final int EVENT_STATE_COMPLETED = 2;
    public static final int EVENT_STATE_UNKNOWN = -1;
    

    private int state = EVENT_STATE_UNKNOWN;
    private IDataSet data;
    private IViewPane sourceView;
    
    /**
     * @param paramObject
     */
    public SDVViewStateEvent(IViewPane source, int eventType) {
        this(source, eventType, null);
    }
    /**
     * @param paramObject
     */
    public SDVViewStateEvent(IViewPane source, int eventType, IDataSet data) {
        super(source);
        this.sourceView = source;
        this.state = eventType;
        this.data = data;
    }
    
    /**
     * @return the data
     */
    public IDataSet getData() {
        return data;
    }
    
    /**
     * @return the state
     */
    public int getState() {
        return state;
    }
    
    /**
     * @return the sourceView
     */
    public IViewPane getSourceView() {
        return sourceView;
    }


}
