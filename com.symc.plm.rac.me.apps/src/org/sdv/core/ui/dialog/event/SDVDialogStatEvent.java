/**
 * 
 */
package org.sdv.core.ui.dialog.event;

import java.util.EventObject;

/**
 * Class Name : SDVDialogStatEvent
 * Class Description :
 * 
 * @date 2013. 10. 2.
 * 
 */
public class SDVDialogStatEvent extends EventObject {

    public static final int EVENT_STAT_CREATE = 0;
    public static final int EVTENT_STAT_INIT = 1;
    public static final int EVENT_STAT_COMPLETE = 2;
    public static final int EVENT_STAT_CHILD_COMPLETE = 3;
    public static final int EVENT_STAT_CHILD_ALL_COMPLETE = 4;
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected int eventStat = EVENT_STAT_CREATE;

    /**
     * @param paramObject
     */
    public SDVDialogStatEvent(Object source, int eventType) {
        super(source);
        switch (eventType) {
        case EVENT_STAT_CREATE:
        case EVTENT_STAT_INIT:
        case EVENT_STAT_COMPLETE:
            this.eventStat = eventType;
            break;
        default:
            break;
        }
    }
    
    /**
     * Event ป๓ลย
     * 
     * @method getCurrentState 
     * @date 2013. 10. 2.
     * @param
     * @return Object
     * @exception
     * @throws
     * @see
     */
    public Object getCurrentState() {
        return eventStat;
    }
}
