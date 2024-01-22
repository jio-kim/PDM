/**
 * 
 */
package org.sdv.core.common;

import java.util.EventListener;

import org.sdv.core.ui.view.event.SDVViewStateEvent;

/**
 * Class Name : ISDVViewListener
 * Class Description : 
 * @date 2013. 10. 2.
 *
 */
public interface ISDVViewListener extends EventListener {
    
    /**
     * View의 상태가 변경된 경우(Refresh가 필요한 경우)
     * 다른 오퍼레이션에 의해 데이터가 로드된 경우에도 데이터 값이 넘어온다.
     * @method viewStateChanged 
     * @date 2013. 11. 21.
     * @param  
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void viewStateChanged(SDVViewStateEvent evt);
    
    /**
     * View의 UI 로드후  초기 데이터 처리 오퍼레이션이 완료된후  호출되는 이벤트, 
     * 초기 데이터 값이 Event에 넘어온다.
     * 
     * @method viewUIInitialized 
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void viewUIInitialized(SDVViewStateEvent evt);
    
    /**
     * View가 속한 부모 Contents의 모든 데이터가 로드 된 후에 호출되는 이벤트, 
     * 다른 View의 데이터 로드 후에 처리할 작업을 이 이벤트에서 처리한다.
     * 
     * @method viewUICompleted 
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void viewUICompleted(SDVViewStateEvent evt);
    
    
}
