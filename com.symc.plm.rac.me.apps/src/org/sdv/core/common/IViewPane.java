/**
 *
 */
package org.sdv.core.common;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Composite;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.view.layout.View;

/**
 * Class Name : IViewPane
 * Class Description :
 *
 * @date 2013. 10. 11.
 *
 */
public interface IViewPane extends IShellProvider{

    public static final int DEFAULT_CONFIG_ID = 0;
    public static final String DEFAULT_VIEW_CONTAINER_ID = "__DEFAULT_VIEW__";

    public static final String INTERNAL_METHOD_INDICATOR = "local:";
    public static final String INTERNAL_VIEW_METHOD_INDICATOR = "view:";
    
	public static final String LABEL_TYPE_ACTION_INDICATOR = "[label]";
    
    public static final String DEFAULT_METHOD_INDICATOR = "__DEFAULT_METHOD__";


    
    //-------------------------------------------------------------------------
    // public property access Method
    //-------------------------------------------------------------------------

    public List<IViewPane> getViews();

    public int getViewCount();

    public IViewPane getView(String viewId);

    public void setParameters(Map<String, Object> parameters);

    public Map<String, Object> getParameters();

    public String getId();

    //-------------------------------------------------------------------------
    // layout information Method
    //-------------------------------------------------------------------------

    public View getCurrentViewLayout();

    public void setCurrentViewLayout(View  viewlayout);

    public IViewStubBean getStub();

    public void setStub(IViewStubBean stub);

    //-------------------------------------------------------------------------
    // UI Control Method
    //-------------------------------------------------------------------------

    public Composite getRootContext();

    //public void setLayout(Layout layout);

    public void refresh();

    //-------------------------------------------------------------------------
    // Action Control Method
    //-------------------------------------------------------------------------

    public  List<IAction> getActionTools();

    public  List<IAction> getActionMenus();

    //-------------------------------------------------------------------------
    // Data Control Method
    //-------------------------------------------------------------------------

    public IDataSet getDataSet(String viewId);

    public IDataSet getDataSetAll();

    public IDataSet getSelectDataSet(String viewId);

    public IDataSet getSelectDataSetAll();

    public IDataMap getLocalDataMap();

    public IDataMap getLocalSelectDataMap();

    public void uiLoadCompleted();

    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset);

    public void initalizeData(int result, IViewPane owner, IDataSet dataset);

    /**
     *
     * @method getInitOperation
     * @date 2013. 11. 28.
     * @author CS.Park
     * @param
     * @return AbstractSDVInitOperation
     * @throws
     * @see
     */
    public ISDVInitOperation getInitOperation();


}
