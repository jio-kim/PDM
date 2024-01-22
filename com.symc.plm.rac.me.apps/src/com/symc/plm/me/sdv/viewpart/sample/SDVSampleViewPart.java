/**
 *
 */
package com.symc.plm.me.sdv.viewpart.sample;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.viewpart.AbstractSDVViewPart;

/**
 * Class Name : SDVSampleViewPart
 * Class Description :
 *
 * @date 2013. 10. 14.
 *
 */
public class SDVSampleViewPart extends AbstractSDVViewPart {

    public static final String ID = "com.symc.plm.me.sdv.dialog.sdvsample.SDVSampleViewPart"; //$NON-NLS-1$
    protected Action action;
    protected Action action_1;
    protected Action action_2;

    public SDVSampleViewPart() {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.ui.viewpart.AbstractSDVViewPart#initUI(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void initUI(Composite container) {
        createActions();
        initializeToolBar();
        initializeMenu();
    }

    @Override
    protected Map<String, String> getToolbarActions(){
        Map<String, String> toolbarActions = super.getToolbarActions();
        if(toolbarActions == null){
            toolbarActions = new HashMap<String,String>();
            setToolbarActions(toolbarActions);
        }
        toolbarActions.put("New Action1", "");
        toolbarActions.put("New Action2", "");
        toolbarActions.put("New Action3", "");

        return toolbarActions;
    }


    @Override
    public void setFocus() {
        // Set the focus
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return :
     * @see org.sdv.core.ui.viewpart.AbstractSDVViewPart#afterCreateContents()
     */
    @Override
    protected void afterCreateContents() {
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return :
     * @see org.sdv.core.ui.viewpart.AbstractSDVViewPart#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return :
     * @see org.sdv.core.ui.viewpart.AbstractSDVViewPart#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 21.
     * @author : cspark
     * @param :
     * @return :
     * @see org.sdv.core.ui.viewpart.AbstractSDVViewPart#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        return null;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

}
