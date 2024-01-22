/**
 *
 */
package org.sdv.core.ui.operation;

import java.util.List;

import org.sdv.core.common.ISDVInitOperation;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.event.ISDVInitOperationListener;

import com.teamcenter.rac.aif.AbstractAIFOperation;

/**
 * Class Name : AbstractSDVInitOperation
 * Class Description :
 * @date 	2013. 11. 28.
 * @author  CS.Park
 *
 */
public abstract class AbstractSDVInitOperation extends AbstractAIFOperation implements ISDVInitOperation {


    private List<ISDVInitOperationListener> listeners;

    protected IDataMap paramDataMap;

    /**
     *
     */
    public AbstractSDVInitOperation(){
        super();
    }

    public AbstractSDVInitOperation(IDataMap dataMap) {
        this.paramDataMap = dataMap;
    }

    public void removeListener(ISDVInitOperationListener listener){
        listeners.remove(listener);
    }

    public void addListener(ISDVInitOperationListener listener) {
        this.listeners.add(listener);
    }

    protected void setData(IDataSet resultData){
        this.storeOperationResult(resultData);
    }

    public IDataSet getData(){
        return (IDataSet)this.getOperationResult();
    }

}
