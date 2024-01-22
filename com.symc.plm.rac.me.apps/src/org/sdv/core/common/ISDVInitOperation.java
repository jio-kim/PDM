/**
 * 
 */
package org.sdv.core.common;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.event.ISDVInitOperationListener;

import com.teamcenter.rac.aif.InterfaceAIFOperationListener;

/**
 * Class Name : ISDVInitOperation
 * Class Description : 
 * @date 	2013. 11. 29.
 * @author  CS.Park
 * 
 */
public interface ISDVInitOperation {

    public void addOperationListener(InterfaceAIFOperationListener paramInterfaceAIFOperationListener);
    
    public void removeOperationListener(InterfaceAIFOperationListener paramInterfaceAIFOperationListener);
    
    public void removeListener(ISDVInitOperationListener listener);
    
    public void addListener(ISDVInitOperationListener listener);
    
    public IDataSet getData();
    
    public abstract void executeOperation() throws Exception;
    
}
