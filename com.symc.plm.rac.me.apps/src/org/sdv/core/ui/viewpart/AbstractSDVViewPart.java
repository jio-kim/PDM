/**
 * 
 */
package org.sdv.core.ui.viewpart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.sdv.core.common.ISDVOperation;
import org.sdv.core.common.ISDVViewListener;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.IViewStubBean;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.SDVRuntimeException;
import org.sdv.core.ui.OperationBeanFactory;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.event.ISDVInitOperationListener;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.dialog.event.ViewInitOperationListener;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.ui.view.ViewDataManager;
import org.sdv.core.ui.view.layout.View;

import com.symc.plm.activator.Activator;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * Class Name : AbstractSDVViewPart
 * Class Description :
 * 
 * @date 2013. 10. 11.
 * 
 */
public abstract class AbstractSDVViewPart extends ViewPart implements IViewPane, ISDVInitOperationListener {

    private static final Logger logger = Logger.getLogger(AbstractSDVViewPart.class);

    protected ISDVViewListener viewListener;

    protected Composite parent;
    protected Composite rootComposite;

    // Definition of member for SDV Framework
    protected IViewStubBean stub;

    private List<IViewPane> views;
    private String id;
    private int configId;
    private Map<String, Object> parameters;

    private View currentViewLayout;

    private List<IAction> actionTools;
    private List<IAction> actionMenus;

    private Map<String, String> toolbarActions;

    private List<IViewPane> nonInitializedViewList;

    private boolean initalized;

    public AbstractSDVViewPart() {
        super();
    }

    /*
     * ######################################################################################################
     * #
     * # Define abstract method
     * #
     * ######################################################################################################
     */
    /**
     * UI Render 종료 후 실행
     */
    protected abstract void afterCreateContents();

    public abstract void setLocalDataMap(IDataMap dataMap);

    public abstract IDataMap getLocalDataMap();

    public abstract IDataMap getLocalSelectDataMap();

    /**
     * Create contents of the view part.
     * 
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        rootComposite = new Composite(parent, SWT.NONE);
        this.parent = parent;

        // 지원하지 않는 구성을 호출하면 오류를 발생한다.
        validateConfig(configId);

        // UI 초기상태(STAT_INIT) 이벤트 Fire
        initUI(rootComposite);

        afterCreateContents();
    }

    protected abstract void initUI(Composite container);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.IShellProvider#getShell()
     */
    @Override
    public Shell getShell() {
        if (this.parent instanceof Shell)
            return (Shell) this.parent;
        return this.parent.getShell();
    }

    /*
     * ######################################################################################################
     * #
     * #
     * #
     * ######################################################################################################
     */

    protected void setViews(List<IViewPane> views) {
        this.views = views;
        if(!initalized){
            this.nonInitializedViewList = new ArrayList<IViewPane>(this.views);
            callViewInitOperations(views);
            initalized = true;
        }
    }

    
    /**
     * 
     * @method callViewInitOperations 
     * @date 2013. 11. 28.
     * @author CS.Park
     * @param
     * @return void
     * @throws
     * @see
     */
    protected void callViewInitOperations(List<IViewPane> views) {
        if(views!= null && views.size() > 0){
            for(IViewPane view : views){
                AbstractSDVInitOperation viewInitOperation = (AbstractSDVInitOperation)view.getInitOperation();
                if(viewInitOperation != null){
                    ViewInitOperationListener initOperationListener = new ViewInitOperationListener(this,  view.getId(), viewInitOperation);
                    initOperationListener.addInitListener(this);
                    viewInitOperation.addOperationListener(initOperationListener);
                    viewInitOperation.getSession().queueOperation(viewInitOperation);                    
                }else{
                    this.nonInitializedViewList.remove(view);
                    if(this.nonInitializedViewList.size() == 0 ){
                        viewInitCompleted();
                    }
                }
            }
        }
    }
    protected void setViewParamter(Map<String, Object> paramters) {
        if (getViewCount() > 0) {
            for (IViewPane view : getViews()) {
                view.setParameters(paramters);
            }
        }
    }

    /**
     * Validation 체크 메소드
     * Implements Method
     * 
     * @Copyright : S-PALM
     * @author : 권오규
     * @since : 2013. 1. 10.
     * @return
     */
    protected boolean validationCheck() {
        return true;
    }

    /*
     * ######################################################################################################
     * #
     * # Implemented Method of IViewPane
     * #
     * ######################################################################################################
     */

    /**
     * @return the parameters
     */
    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * @param paramters
     *            the parameters to set
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the currentViewLayout
     */
    public View getCurrentViewLayout() {
        return currentViewLayout;
    }

    /**
     * @param currentViewLayout
     *            the currentViewLayout to set
     */
    public void setCurrentViewLayout(View currentViewLayout) {
        this.currentViewLayout = currentViewLayout;
    }

    /**
     * @return the stub
     */
    public IViewStubBean getStub() {
        return this.stub;
    }

    /**
     * @param stub
     *            the stub to set
     */
    public void setStub(IViewStubBean stub) {
        this.stub = stub;
    }

    @Override
    public Composite getRootContext() {
        return this.rootComposite;
    }

    /************************************************************************************
     * Implement method from IViewPane interface
     ************************************************************************************/

    /*
     * @see org.sdv.core.common.IViewPane#getViews()
     */
    @Override
    public List<IViewPane> getViews() {
        return views;
    }

    @Override
    public int getViewCount() {
        if (views == null)
            return 0;
        return views.size();
    }

    /*
     * @see org.sdv.core.common.IViewPane#getView(java.lang.String)
     */
    @Override
    public IViewPane getView(String viewId) {
        if (views != null && viewId != null) {
            for (IViewPane viewPane : views) {
                if (viewId.equals(viewPane.getId()))
                    return viewPane;
            }
        }
        return null;
    }

    /*
     * @see org.sdv.core.common.IViewPane#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /*
     * @see org.sdv.core.common.IViewPane#refresh()
     */
    @Override
    public void refresh() {

    }

    /*---------------------------------------------------------------------------------------------
     * 
     *  Data 처리
     *     
     *---------------------------------------------------------------------------------------------*/

    public void setDataSet(String targetViewId, IDataSet dataSet) {
        setDataSet(targetViewId, targetViewId, dataSet);
    }

    public void setDataSet(String targetViewId, String sourceViewId, IDataSet dataSet) {
        if (dataSet == null || sourceViewId == null || !dataSet.containsMap(sourceViewId))
            return;
        setDataMap(targetViewId, dataSet.getDataMap(sourceViewId));
    }

    public void setDataMap(String targetViewId, IDataMap dataMap) {
        if (targetViewId == null)
            return;
        if (targetViewId.equals(getId())) {
            setLocalDataMap(dataMap);
        } else {
            IViewPane targetView = getView(targetViewId);
            if (targetView != null && targetView instanceof AbstractSDVSWTDialog) {
                ((AbstractSDVSWTDialog) targetView).setLocalDataMap(dataMap);
            } else {
                throw new SDVRuntimeException("IViewPane is not inherited AbstractSDVViewPane!!. ");
            }
        }
    }

    public void setDataSetAll(IDataSet dataSet) {
        IDataMap dataMap = dataSet.getDataMap(getId());
        if (dataMap != null)
            this.setLocalDataMap(dataMap);

        if (getViewCount() > 0) {
            for (IViewPane viewPane : getViews()) {
                if (viewPane instanceof AbstractSDVSWTDialog) {
                    ((AbstractSDVViewPane) viewPane).setDataSetAll(dataSet);
                }
            }
        }
    }

    /**
     * 요청된 config가 지원되는지 여부를 확인한다. 기본 구성 이외의 구성을 지원하려면 이 메서드를 Override하여 구성한다.
     * 
     * @method validateConfig
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected void validateConfig(int configId) {
        if (configId != DEFAULT_CONFIG_ID)
            throw new SDVRuntimeException("View[" + getId() + " not supported config Id :" + configId);
    }


    /*
     * @see org.sdv.core.common.IViewPane#getDataSet(java.lang.String)
     */
    @Override
    public IDataSet getDataSet(final String viewId) {
    	final IDataSet newDataset = new DataSet();
    	Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
		    	ViewDataManager.addDataSet(AbstractSDVViewPart.this, newDataset,viewId);
			}
		});
        return newDataset;
        
    }

    /*
     * @see org.sdv.core.common.IViewPane#getDataSetAll()
     */
    @Override
    public IDataSet getDataSetAll() {
    	final IDataSet newDataset = new DataSet();
    	Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ViewDataManager.addDataSetAll(AbstractSDVViewPart.this, newDataset);
			}
		});
        return newDataset;
    }

    /*
     * @see org.sdv.core.common.IViewPane#getSelectDataSet(java.lang.String)
     */
    @Override
    public IDataSet getSelectDataSet(final String viewId) {
    	final IDataSet newDataset = new DataSet();
    	Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ViewDataManager.addSelectDataSet(AbstractSDVViewPart.this, newDataset,viewId);
			}
		});
        return newDataset;
    }

    /*
     * @see org.sdv.core.common.IViewPane#getSelectDataSetAll()
     */
    @Override
    public IDataSet getSelectDataSetAll() {
    	final IDataSet newDataset = new DataSet();
    	Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ViewDataManager.addSelectDataSetAll(AbstractSDVViewPart.this, newDataset );
			}
		});
        return newDataset;
    }

    protected Map<String, String> getToolbarActions(){
        return this.toolbarActions;
    }

    /**
     * 
     * @method setToolbarActions 
     * @date 2013. 11. 21.
     * @author cspark
     * @param
     * @return void
     * @throws
     * @see
     */
    protected void setToolbarActions(Map<String, String> toolbarActions){
        this.toolbarActions = toolbarActions;
    }
    
    
    /**
     * 
     * @method createActions
     * @date 2013. 11. 21.
     * @author cspark
     * @param
     * @return void
     * @throws
     * @see
     */
    protected void createActions() {
        
        IViewStubBean stub = getStub();
        if(stub != null){
            setToolbarActions(stub.getToolbarActions());
            if(this.toolbarActions == null) return;
            
            List<IAction> actions = getActionTools();
            
            for(final String actionName : toolbarActions.keySet()){
                IAction action = new Action(actionName, Activator.imageDescriptorFromPlugin(toolbarActions.get(actionName), String.format("icons/%s.png", actionName)))
                {
                    public void run() {
                        actionPressed(actionName);
                    }
                };
                actions.add(action);
            }
        }
    }
    
    public void actionPressed(final String actionName){
        if(this.toolbarActions == null || !toolbarActions.containsKey(actionName)) return;
        
        final String operationId = toolbarActions.get(actionName);

            try {
                final ISDVOperation operation = OperationBeanFactory.getActionOperator(operationId, actionName, getId(), getSelectDataSetAll());

                if (operation instanceof AbstractAIFOperation) {
                    System.out.println("operationId=" + operationId + "[" + actionName + "] ->" + getId());
                    AIFUtility.getDefaultSession().queueOperation((Job) operation);
                } else {
                    new Thread(){
                        public void run(){
                            System.out.println("operationId=" + operationId + "[" + actionName + "] ->" + getId());
                            try {
                                operation.startOperation(operationId);
                                operation.executeOperation();
                                operation.endOperation();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                return;
            } catch (ClassNotFoundException cnfex) {
                logger.error(cnfex.getMessage());
            } catch (SDVException ex) {
                logger.error(ex);
                return;
            } catch (Exception e) {
                logger.error(e);
                return;
            }
    }

    /**
     * 
     * @method initializeToolBar
     * @date 2013. 11. 21.
     * @author cspark
     * @param
     * @return void
     * @throws
     * @see
     */
    protected void initializeToolBar() {
        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
        List<IAction> actions = getActionTools();
        if (actions != null) {
            for (IAction action : actions) {
                toolbarManager.add(action);
            }
        }
    }
    
    /**
     * Initialize the menu.
     */
    protected void initializeMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        List<IAction> actions = getActionMenus();
        if (actions != null) {
            for (IAction action : actions) {
                menuManager.add(action);
            }
        }
    }    

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 22.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.common.IViewPane#getActionTools()
     */
    @Override
    public List<IAction> getActionTools() {
        return actionTools;
    }
    
    @Override
    public List<IAction> getActionMenus() {
        return actionMenus;
    }    

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 28.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.common.IViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    @Override
    public void uiLoadCompleted(){
        
    }
    
    @Override
    public void initalizeData(int result, IViewPane owner, IDataSet dataset){
        
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 28.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.dialog.event.ISDVInitOperationListener#startInitalize(org.sdv.core.ui.dialog.event.SDVInitEvent)
     */
    @Override
    public void willInitalize(SDVInitEvent sdvInitEvent) {
        //모든 뷰가 초기화 되었는지 확인하고 초기화 되면 다시 각 뷰에 알려주기 위해 확인한다.
        String viewId = sdvInitEvent.getId();
        for(IViewPane view : this.nonInitializedViewList){
            if(view.getId().equals(viewId)){
                viewInitalizeData(view,SDVInitEvent.INIT_SUCCESS, sdvInitEvent.getData());
            }
        }
    }
    
    /**
     * 
     * @method failInitalize 
     * @date 2013. 11. 29.
     * @author CS.Park
     * @param
     * @return void
     * @throws
     * @see
     */
    public void failInitalize(SDVInitEvent sdvInitEvent){
        if(nonInitializedViewList == null) return;
        
        //모든 뷰가 초기화 되었는지 확인하고 초기화 되면 다시 각 뷰에 알려주기 위해 확인한다.
        String viewId = sdvInitEvent.getId();
        for(IViewPane view : this.nonInitializedViewList){
            if(view.getId().equals(viewId)){
                viewInitalizeData(view,SDVInitEvent.INIT_FAILED, sdvInitEvent.getData());
            }
        }
    }
    
    
    protected void viewInitalizeData(IViewPane view, int result, IDataSet data){
        try{
        view.initalizeData(result, this, data);
        }catch(Exception ex){
            logger.error(ex);
        }finally{
            this.nonInitializedViewList.remove(view);
            //초기화 되지 않은 뷰목록이 비었다면 모두 초기화 되었다고 판단하고 완료를 시킨다.
            if(this.nonInitializedViewList.size() == 0 ){
                viewInitCompleted();
            }
        }
    }
    
    /**
     * 
     * @method viewInitCompleted 
     * @date 2013. 11. 28.
     * @author CS.Park
     * @param
     * @return void
     * @throws
     * @see
     */
    protected void viewInitCompleted(){
        if(getViews() == null) return;
        for(IViewPane view : getViews()){
            view.uiLoadCompleted();
        }
    }        
}
