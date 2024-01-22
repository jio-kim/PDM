/**
 *
 */
package org.sdv.core.ui.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.ActionButtonInfo;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.ISDVActionOperation;
import org.sdv.core.common.ISDVOperation;
import org.sdv.core.common.ISDVViewListener;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.IViewStubBean;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.SDVRuntimeException;
import org.sdv.core.ui.OperationBeanFactory;
import org.sdv.core.ui.SDVUILayoutManager;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.event.ISDVInitOperationListener;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.dialog.event.ViewInitOperationListener;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.event.SDVViewStateEvent;
import org.sdv.core.ui.view.layout.View;
import org.sdv.core.util.ProgressBar;
import org.sdv.core.util.ReflectUtil;
import org.sdv.core.util.UIUtil;
import org.springframework.util.StringUtils;

import swing2swt.layout.BorderLayout;

import com.symc.plm.activator.Activator;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : AbstractSDVView
 * Class Description :
 *
 * @date 2013. 10. 11.
 *
 */
public abstract class AbstractSDVViewPane extends Composite implements IViewPane, ISDVViewListener, ISDVInitOperationListener {

	private static final Logger		logger					= Logger.getLogger(AbstractSDVViewPane.class);

	public static final int			DEFAULT_BUTTON_WIDTH_HINT = 50;
	public static final int			DEFAULT_ACTION_MARGIN	 = 4;

	// 화면 글자수에 따른 간격처리를 위한 폰트메트릭 정보
	private static FontMetrics		fontMetrics;

	protected String				id;
	protected ISDVViewListener		viewListener;
	private Map<String, Object>		parameters;

	private Composite				rootComposite;

	private List<IViewPane>			views;

	private View					currentViewLayout;
	private IViewStubBean			stub;

	private int						configId;
	private String					order;

	private Map<String, String>		toolbarActionMap;
	private Map<String, String>		menuActionMap;

	private List<IAction>			actionTools;
	private List<IAction>			actionMenus;

	private ActionBar				actionToolBar;
	private ActionBar				actionMenuBar;

	protected Composite				actionBar;
	protected Composite				contentPane;

	protected Registry				parentRegistry;
	protected Registry				registry;

	private LinkedHashMap<String, IButtonInfo> actionToolButtons;

	private boolean					initalized;

	private HashMap<String, Composite>	viewContainers;

	private boolean isShowProgress;
	private ProgressBar progressShell;
	private Shell thisShell;
	// ==================================================================================================
	//
	// Constructor
	//
	// ==================================================================================================

	/**
	 * 기본 생성자
	 * Configuration을 기본구성으로 한다.
	 *
	 * @wbp.parser.constructor
	 * @param parent
	 *            SWT parent Composite
	 * @param style
	 *            SWT display style
	 * @param id
	 *            viewId
	 */
	public AbstractSDVViewPane(Composite parent, int style, String id) {
		this(parent, style, id, DEFAULT_CONFIG_ID, null);
	}

	public AbstractSDVViewPane(Composite parent, int style, String id, int configId) {
		this(parent, style, id, configId, null);
	}

	/**
	 *
	 * @param parent
	 *            SWT parent Composite
	 * @param style
	 *            SWT display style
	 * @param id
	 *            viewId
	 * @param configId
	 *            view configurationId , default is 0
	 */
	public AbstractSDVViewPane(Composite parent, int style, String id, int configId, String order) {
		super(parent, style);
		this.id = id;
		this.configId = configId;
		this.order = order;

		try {
			thisShell = getShell();
			// 지원하지 않는 구성을 호출하면 오류를 발생한다.
			validateConfig(configId);
			setLayout(UIUtil.getGridLayout(false));
			UIUtil.applyRandomBackground(this);
			create(this);
		} catch (Exception ex) {
			logger.debug(ex);
			throw new RuntimeException(ex);
		}
	}

	protected HashMap<String, Composite> addViewContainer(String viewId, Composite composite) {
		if (viewContainers == null) {
			viewContainers = new HashMap<String, Composite>();
		}
		viewContainers.put(viewId, composite);
		return viewContainers;
	}

	public HashMap<String, Composite> getViewContainer(Composite parent, String viewId) {
		return viewContainers;
	}

	/**
	 *
	 * @method getRegistry
	 * @date 2013. 12. 2.
	 * @author CS.Park
	 * @param
	 * @return Object
	 * @throws
	 * @see
	 */
	protected Registry getRegistry() {
		try {
			if (this.registry == null) {
				return Registry.getRegistry(this);
			}
		} catch (Exception ex) {
			logger.error(ex);
			try{
    			if(this.parentRegistry == null){
    				this.parentRegistry = Registry.getRegistry(AbstractSDVViewPane.class);
    			}
			}catch(NullPointerException nex){
				if(logger.isDebugEnabled())
					logger.debug(nex);
			}
			return this.parentRegistry;
		}
		return this.registry;
	}

	protected void create(Composite parent) {
		this.rootComposite = new Composite(parent, SWT.NONE);
		//this.rootComposite = new Group(parent, SWT.NONE);


		Layout parentLayout = parent.getLayout();
		if (parentLayout instanceof GridLayout) {
			this.rootComposite.setLayoutData(UIUtil.getGridData(GridData.FILL_BOTH)); // VERTICAL_ALIGN_FILL | GRAB_VERTICAL | VERTICAL_ALIGN_FILL | GRAB_HORIZONTAL
		}
		// this.rootComposite.setLayout(UIUtil.getGridLayout(1, 0, 0, 0, 0, 0, 0, false));
		// 버튼과 컨텐츠를 상, 하 구조로 넣기 위해 BorderLayout을 사용한다.
		this.rootComposite.setLayout(new BorderLayout());

		this.actionBar = createActionBar(this.rootComposite);
		this.contentPane = createContents(this.rootComposite);
	}

	protected Composite createActionBar(Composite paramComposite) {
		Composite actionBarHolder = new Composite(paramComposite, SWT.NONE);
		actionBarHolder.setLayoutData(BorderLayout.NORTH);
		actionBarHolder.setLayout(UIUtil.getGridLayout(false));
		// actionBarHolder.setLayoutData(UIUtil.getGridData(GridData.FILL_HORIZONTAL));

		Composite actionBarPane = new Composite(actionBarHolder, SWT.NONE);

		// 뷰안에 뷰가 생성될 경우 뷰가 넣어질 판넬의 크기가 처음에 작게 잡혀서 버튼이 안보이게 되는 경우가 발생하므로
		// 초기 뷰판넬은 전체 크기를 잡도록 설정하고 버튼을 넣은후 왼쪽 정렬로 변경하여 준다.
		actionBarPane.setLayoutData(UIUtil.getGridData(GridData.FILL_HORIZONTAL));

		actionBarPane.setLayout(UIUtil.getGridLayout(1, 0, 0, 0, 0, convertHorizontalDLUsToPixels(4), convertHorizontalDLUsToPixels(4), false));
		return actionBarPane;
	}

	protected Composite getActionBar() {
		return this.actionBar;
	}

	protected void actionBarDispose(Composite composite) {
		if (composite == null)
			return;
		if (this.actionBar == null || this.actionBar.isDisposed()) {
			composite.dispose();
		} else {
			if (this.actionBar == composite) {
				composite.getParent().dispose();
				this.actionBar = null;
			} else {
				actionBarDispose(composite.getParent());
			}
		}
	}


	public void setToolbarLocation(int location, Composite toolbar){

		switch(location){
		case SWT.LEFT :
			toolbar.setLayoutData(BorderLayout.WEST);
			break;
		case SWT.RIGHT :
			toolbar.setLayoutData(BorderLayout.EAST);
			break;
		case SWT.BOTTOM :
			toolbar.setLayoutData(BorderLayout.SOUTH);
			break;
		case SWT.TOP :
			default :
			toolbar.setLayoutData(BorderLayout.NORTH);
		}
	}



	protected void createButtonsForActionBar(Composite parent) {

		Map<String, String> toolbarActions = stub.getToolbarActions();
		if (toolbarActions == null || toolbarActions.size() == 0) {
			//정의된 버튼이 없다면 버튼판넬을 숨긴다.
			actionBarDispose(parent);
			return;
		}

		int toolbarLocation = stub.getToolbarStyle(IViewStubBean.TOOLBAR_LOCATION);
		setToolbarLocation(toolbarLocation, parent.getParent());

		for(String actionId : toolbarActions.keySet()) {
			String buttonId = actionId;
			String buttonImage = actionId;
			boolean isIconButton = false;

			String operationId = toolbarActions.get(actionId);

			if (actionId.indexOf(";") != -1) {
				String[] actionInfos = actionId.split(";");
				if (actionInfos.length > 1  && !StringUtils.isEmpty(actionInfos[1])) {
					buttonImage = actionInfos[1];
				} else {
					buttonImage = actionInfos[0];
				}

				// 아이콘으로 표시하기로 설정되어 있으면 버튼 텍스트를 툴팁으로 보여준다.
				if (actionInfos.length > 2) {
					isIconButton = Boolean.valueOf(actionInfos[2]);
				}
				buttonId = actionInfos[0];
			}

			if (!getActionToolButtons().containsKey(actionId)) {
				IButtonInfo actionButtonInfo = createActionButton(parent, actionId, buttonId, buttonImage, operationId, isIconButton );
				getActionToolButtons().put(buttonId, actionButtonInfo);
			}
		}

		// 뷰안에 뷰가 생성될 경우 뷰가 넣어질 판넬의 크기가 처음에 작게 잡혀서 버튼이 안보이게 되는 경우가 발생하므로
		// 초기 뷰판넬은 전체 크기를 잡도록 설정하고 버튼을 넣은후 왼쪽 정렬로 변경하여 준다.
		if (parent.getLayoutData() != null || parent.getLayoutData() instanceof GridData) {
			//GridData parentLayoutData = (GridData) parent.getLayoutData();

			int toolbarAlign = this.stub.getToolbarStyle(IViewStubBean.TOOLBAR_ALIGN);
			switch(toolbarAlign){
			case SWT.RIGHT :
				parent.setLayoutData(UIUtil.getGridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
				break;
			case SWT.CENTER :
				parent.setLayoutData(UIUtil.getGridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL));
				break;
			case SWT.FILL :
				parent.setLayoutData(UIUtil.getGridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
				break;
			case SWT.LEFT :
				default :
					parent.setLayoutData(UIUtil.getGridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
			}
		}
	}


	protected IButtonInfo createActionButton(Composite parent, String actionId, String buttonId, String buttonImage, String operationId, boolean isIconButton) {
		((GridLayout) parent.getLayout()).numColumns += 1;

		Button button = new Button(parent, SWT.PUSH);

		String buttonText = getRegistry().getString(buttonId + ".TEXT", buttonId);
		String buttonToolTip = (isIconButton? buttonText:"");

		if (isIconButton) {
			// 툴팁 설정
			button.setToolTipText(buttonToolTip);
		}else{
			// 버튼의 텍스트 설정
    		button.setText(buttonText);
    		button.setFont(JFaceResources.getDialogFont());
		}

		button.setData(buttonId);

		// 버튼의 이미지 설정
		if (buttonImage != null) {
			if(!buttonImage.endsWith(".ICON")){
				buttonImage += ".ICON";
			}

			Image iconImage = getRegistry().getImage(buttonImage);
			if(iconImage != null){
				button.setImage(iconImage);
			}
		}

		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				String buttonId = (String) event.widget.getData();
				actionPressed(buttonId);
			}
		});

		setButtonLayoutData(button);
		return new ActionButtonInfo(actionId, buttonId, operationId, buttonImage, button);
	}

	public LinkedHashMap<String, IButtonInfo> getActionToolButtons() {
		if (actionToolButtons == null) {
			actionToolButtons = new LinkedHashMap<String, IButtonInfo>();
		}
		return actionToolButtons;
	}

	protected Composite createContents(Composite parent) {
		// UI를 로드한다.
		Composite contentPane = new Composite(parent, SWT.NONE);
		contentPane.setLayoutData(BorderLayout.CENTER);
		// contentPane.setLayoutData(UIUtil.getGridData(GridData.FILL_BOTH));
		contentPane.setLayout(new FillLayout());
		initUI(contentPane);
		return contentPane;
	}

	/*
	 * ######################################################################################################
	 * #
	 * #
	 * #
	 * ######################################################################################################
	 */

	protected void setViews(List<IViewPane> views) {
		if (!initalized && views != null) {
			this.views = views;
			callViewInitOperations(new ArrayList<IViewPane>(views));
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
		if (views != null) {
			for(IViewPane view : views) {
				AbstractSDVInitOperation viewInitOperation = (AbstractSDVInitOperation) view.getInitOperation();
				if (viewInitOperation != null) {
					ViewInitOperationListener initOperationListener = new ViewInitOperationListener(this, view.getId(), viewInitOperation);
					initOperationListener.addInitListener(this);
					viewInitOperation.addOperationListener(initOperationListener);
					viewInitOperation.getSession().queueOperation(viewInitOperation);
				}
			}
		}
	}

	/**
	 * @deprecated
	 * @method initUI
	 * @date 2013. 11. 22.
	 * @author CS.Park
	 */
	protected void initUI() {

	};

	/**
	 * 화면에 표시될 UI를 정의한다.
	 *
	 * @method initUI
	 * @date 2013. 11. 21.
	 * @return void
	 * @throws
	 * @see
	 */
	protected void initUI(Composite parent) {

	}

	public abstract void setLocalDataMap(IDataMap dataMap);

	public abstract IDataMap getLocalDataMap();

	public abstract IDataMap getLocalSelectDataMap();

	public Composite getRootContext() {
		return rootComposite;
	}

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
			if (targetView == null) {
				for(IViewPane viewPane : getViews()) {
					if (viewPane instanceof AbstractSDVViewPane) {
						((AbstractSDVViewPane) viewPane).setDataMap(targetViewId, dataMap);
					}
				}
			} else if (targetView instanceof AbstractSDVViewPane) {
				((AbstractSDVViewPane) targetView).setLocalDataMap(dataMap);
			}
		}
	}

	public void setDataSetAll(IDataSet dataSet) {
		IDataMap dataMap = dataSet.getDataMap(getId());
		if (dataMap != null)
			this.setLocalDataMap(dataMap);

		if (getViewCount() > 0) {
			for(IViewPane viewPane : getViews()) {
				if (viewPane instanceof AbstractSDVViewPane) {
					((AbstractSDVViewPane) viewPane).setDataSetAll(dataSet);
				}
			}
		}
	}

	protected void setViewParamter(Map<String, Object> paramters) {
		if (getViewCount() > 0) {
			for(IViewPane view : getViews()) {
				view.setParameters(paramters);
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

	public int getConfigId() {
		return this.configId;
	}

	public String getOrder() {
		return this.order;
	}

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
		return stub;
	}

	/**
	 * @param stub
	 *            the stub to set
	 */
	public void setStub(IViewStubBean stub) {
		if (this.stub == null || this.stub != stub) {
			this.stub = stub;
			if (stub != null) {
				try {
					// Composite viewContainer = getViewContainer(contentPane, stub.getId());
					HashMap<String, Composite> viewContainerMap = getViewContainer(contentPane, stub.getId());
					addChildViewPane(stub, viewContainerMap);
				} catch (Exception e) {
					e.printStackTrace();
				}
				createButtonsForActionBar(getActionBar());
			}
		}
	}

	protected void addChildViewPane(IViewStubBean stub, HashMap<String, Composite> viewContainerMap) {
		try {
			if (stub != null) {
				if (viewContainerMap == null || !viewContainerMap.containsKey(DEFAULT_VIEW_CONTAINER_ID)) {
					viewContainerMap = addViewContainer(DEFAULT_VIEW_CONTAINER_ID, contentPane);
				}
				List<IViewPane> views = SDVUILayoutManager.createViewPaneLayout(viewContainerMap, stub, getParameters());
				if (views != null) {
					setViews(views);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			for(IViewPane viewPane : views) {
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

	/*
	 * @see org.sdv.core.common.IViewPane#getDataSet(java.lang.String)
	 */
	@Override
	public IDataSet getDataSet(final String viewId) {
		IDataSet newDataset = new DataSet();
		return ViewDataManager.addDataSet(this, newDataset, viewId);
	}

	/*
	 * @see org.sdv.core.common.IViewPane#getDataSetAll()
	 */
	@Override
	public IDataSet getDataSetAll() {
		IDataSet newDataset = new DataSet();
		return ViewDataManager.addDataSetAll(this, newDataset);
	}

	/*
	 * @see org.sdv.core.common.IViewPane#getSelectDataSet(java.lang.String)
	 */
	@Override
	public IDataSet getSelectDataSet(final String viewId) {
		IDataSet newDataset = new DataSet();
		return ViewDataManager.addSelectDataSet(this, newDataset, viewId);
	}

	/*
	 * @see org.sdv.core.common.IViewPane#getSelectDataSetAll()
	 */
	@Override
	public IDataSet getSelectDataSetAll() {
		IDataSet newDataset = new DataSet();
		return ViewDataManager.addSelectDataSetAll(this, newDataset);
	}

	/************************************************************************************
	 * Implement method from ISDVViewListener interface
	 ************************************************************************************/

	/*
	 * @param evt SDVViewStateEvent
	 *
	 * @see org.sdv.core.common.ISDVViewListener#viewStateChanged(org.sdv.core.ui.view.event.SDVViewStateEvent)
	 */
	@Override
	public void viewStateChanged(SDVViewStateEvent evt) {
		if (evt.getState() == SDVViewStateEvent.EVENT_STATE_CHANGED) {
			setDataSetAll(evt.getData());
		}
		refresh();
	}

	/*
	 * @param evt SDVViewStateEvent
	 *
	 * @see org.sdv.core.common.ISDVViewListener#viewUIInitialized(org.sdv.core.ui.view.event.SDVViewStateEvent)
	 */
	@Override
	public void viewUIInitialized(SDVViewStateEvent evt) {

	}

	/*
	 * @param evt SDVViewStateEvent
	 *
	 * @see org.sdv.core.common.ISDVViewListener#viewUICompleted(org.sdv.core.ui.view.event.SDVViewStateEvent)
	 */
	@Override
	public void viewUICompleted(SDVViewStateEvent evt) {

	}

	protected Map<String, String> getToolbarActionMap() {
		return this.toolbarActionMap;
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
	protected void setToolbarActionMap(Map<String, String> toolbarActionMap) {
		this.toolbarActionMap = toolbarActionMap;
	}

	protected Map<String, String> getMenuActionMap() {
		return this.menuActionMap;
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
	protected void setMenuActionMap(Map<String, String> menuActionMap) {
		this.menuActionMap = menuActionMap;
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
	protected void createActions(Composite actionParent) {

		IViewStubBean stub = getStub();
		if (stub != null) {
			this.actionTools = createToolbarAction(stub.getToolbarActions());
			this.actionMenus = createMenuAction(stub.getMenuActions());
		}
	}

	/**
	 *
	 * @method createToolbarAction
	 * @date 2013. 11. 22.
	 * @author CS.Park
	 * @param
	 * @return Map<String,String>
	 * @throws
	 * @see
	 */
	protected List<IAction> createToolbarAction(Map<String, String> toolbarActions) {
		List<IAction> actions = getActionTools();
		setToolbarActionMap(toolbarActions);
		if (toolbarActions != null) {
			for(final String actionName : toolbarActions.keySet()) {
				IAction action = new Action(actionName, Activator.imageDescriptorFromPlugin(toolbarActions.get(actionName), String.format("icons/%s.png", actionName))) {

					public void run() {
						actionPressed(actionName);
					}
				};
				actions.add(action);
			}
		}
		return actions;
	}

	/**
	 *
	 * @method createToolbarAction
	 * @date 2013. 11. 22.
	 * @author CS.Park
	 * @param
	 * @return Map<String,String>
	 * @throws
	 * @see
	 */
	protected List<IAction> createMenuAction(Map<String, String> menuActions) {
		List<IAction> actions = getActionMenus();
		setMenuActionMap(menuActions);
		if (menuActions != null) {
			for(final String actionName : menuActions.keySet()) {
				IAction action = new Action(actionName, Activator.imageDescriptorFromPlugin(menuActions.get(actionName), String.format("icons/%s.png", actionName))) {

					public void run() {
						actionPressed(actionName);
					}
				};
				actions.add(action);
			}
		}
		return actions;
	}

	/*---------------------------------------------------------------------------------------------
	 *
	 *  Commmand Button 처리
	 *
	 *---------------------------------------------------------------------------------------------*/

	protected void localExecute(String actionId, String operationId) {
		Object[] arguments = new Object[] { actionId, operationId };
		String methodName = operationId.substring(INTERNAL_METHOD_INDICATOR.length());

		try {
			Class<?> current = getClass();
			
			Class<?>[] argumentTypes = new Class<?>[] { String.class, String.class };
			Method method = ReflectUtil.getLocalMethod(current, methodName, argumentTypes);

			if (method != null) {
				method.invoke(this, arguments);
				return;
			}
			method = ReflectUtil.getLocalMethod(current, methodName, null);
			if (method != null) {
				method.invoke(this);
				return;
			}
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (InvocationTargetException e) {
			logger.error(e);
		} finally {
			lockParent(false);
		}
	}

	protected void localChildExecute(String buttonId, String operationId) {
		Object[] arguments = new Object[] { buttonId, operationId };
		String methodInfo = operationId.substring(INTERNAL_VIEW_METHOD_INDICATOR.length());

		try {
			int methodIndex = methodInfo.lastIndexOf(".");
			// 뷰 이름이 없으므로 작업을 중단한다.
			if (methodIndex < 1)
				return;
			
			String viewId = methodInfo.substring(0, methodIndex);
			String methodName = methodInfo.substring(methodIndex + 1);
			
			IViewPane targetView = getView(viewId);
			if (targetView == null)
				return;
			
			Class<?> current = targetView.getClass();
			Class<?>[] argumentTypes = new Class<?>[] { String.class, String.class };
			Method method = ReflectUtil.getLocalMethod(current, methodName, argumentTypes);

			if (method != null) {
				method.invoke(targetView, arguments);
				return;
			}
			method = ReflectUtil.getLocalMethod(current, methodName, null);
			if (method != null) {
				method.invoke(targetView);
				return;
			}
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (InvocationTargetException e) {
			logger.error(e);
		} finally {
			lockParent(false);
		}
	}

	protected void actionPressed(final String buttonId) {
		if (! getShell().isEnabled())
			return;

		if (StringUtils.isEmpty(buttonId) || getActionToolButtons() == null || !getActionToolButtons().containsKey(buttonId)){
			logger.info(String.format("The action[%s] of viewPane[%s] is not implemented!!.\n action name is not contains toolbarACtions.", buttonId, getId()));
			return;
		}

		IButtonInfo buttonInfo = getActionToolButtons().get(buttonId);
		String operationId = buttonInfo.getOperationId();

		if (StringUtils.isEmpty(operationId))
			return;

		Map<String, Object> parameters = null;

		lockParent(true);
		
		if (operationId.startsWith(INTERNAL_METHOD_INDICATOR)) {
			localExecute(buttonId, operationId);
			return;
			// 현재 창에 연결된 뷰의 메서드를 호출하는 오퍼레이션
		} else if (operationId.startsWith(INTERNAL_VIEW_METHOD_INDICATOR)) {
			localChildExecute(buttonId, operationId);
			return;
		}

		if (buttonId.equals("Search"))
			showProgress(true);

		String[] operationParams = operationId.split(";");

		if (operationParams.length > 1) {
			parameters = parseActionParameters(operationParams[1]);
		}
		try {
			final String parmOperationId = operationParams[0];
			final ISDVOperation operation = OperationBeanFactory.getActionOperator(parmOperationId, buttonId, getId(), parameters, getSelectDataSetAll());

			if (operation instanceof ISDVActionOperation) {
				final ISDVActionOperation actionOperation = (ISDVActionOperation) operation;
				actionOperation.addOperationListener(new InterfaceAIFOperationListener() {

					@Override
					public void endOperation() {
						Display display = AIFUtility.getActiveDesktop().getShell().getDisplay();

						display.syncExec(new Runnable() {

							@Override
							public void run() {
								lockParent(false);
								showProgress(false);
								
								IDataSet result = actionOperation.getDataSet();
								String targetId = actionOperation.getTargetId();
								// String ownerId = actionOperation.getOwnerId();
								AbstractSDVSWTDialog ownerDialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
								IDataMap dataMap = result.getDataMap(targetId);
								if (dataMap == null) {
									dataMap = new RawDataMap();
								}
								dataMap.put("actionId", buttonId, IData.STRING_FIELD);
								ownerDialog.setDataSet(targetId, result);
							}
						});

					}

					@Override
					public void startOperation(String arg0) {
					}
				});
				AIFUtility.getDefaultSession().queueOperation((Job) actionOperation);
			} else {
				new Job(getId() + "::" + buttonId + "::" + operationId) {

					@Override
					protected IStatus run(IProgressMonitor arg0) {
						try {
							operation.startOperation(buttonId);
							operation.executeOperation();
							operation.endOperation();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							lockParent(false);
							showProgress(false);
						}
						return null;
					}
				}.schedule();
			}
			return;
		} catch (ClassNotFoundException cnfex) {
			lockParent(false);
			logger.error(cnfex.getMessage());
		} catch (SDVException ex) {
			lockParent(false);
			logger.error(ex);
			return;
		} catch (Exception e) {
			lockParent(false);
			logger.error(e);
			return;
		} finally {
//			lockParent(false);
		}
	}

	protected Map<String, Object> parseActionParameters(String paramStr) {
		Map<String, Object> parameters = null;
		if (!StringUtils.isEmpty(paramStr)) {
			parameters = new HashMap<String, Object>();
			String[] entries = paramStr.split(":");
			for(String entryStr : entries) {
				String[] entry = entryStr.split("=");
				if (entry.length > 1) {
					parameters.put(entry[0], entry[1]);
				} else {
					parameters.put(entry[0], entry[0]);
				}
			}
		}
		return parameters;
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
		IToolBarManager toolbarManager = getActionToolBar().getToolBarManager();
		List<IAction> actions = getActionTools();
		if (actions != null) {
			for(IAction action : actions) {
				toolbarManager.add(action);
			}
		}
	}

	/**
	 * Initialize the menu.
	 */
	protected void initializeMenu() {
		IMenuManager menuManager = getActionMenuBar().getMenuManager();
		List<IAction> actions = getActionMenus();
		if (actions != null) {
			for(IAction action : actions) {
				menuManager.add(action);
			}
		}
	}

	/**
	 *
	 * Description :
	 *
	 * @method :
	 * @date : 2013. 11. 22.
	 * @author : CS.Park
	 * @param :
	 * @return : List<IAction>
	 * @see org.sdv.core.common.IViewPane#getActions()
	 */
	@Override
	public List<IAction> getActionTools() {
		return actionTools;
	}

	@Override
	public List<IAction> getActionMenus() {
		return actionMenus;
	}

	protected ActionBar getActionToolBar() {
		if (actionToolBar == null)
			actionToolBar = new ActionBar();
		return actionToolBar;
	}

	protected ActionBar getActionMenuBar() {
		if (actionMenuBar == null)
			actionMenuBar = new ActionBar();
		return actionMenuBar;
	}

	protected class ActionBar {

		public IToolBarManager getToolBarManager() {
			return null;
		}

		public IMenuManager getMenuManager() {
			return null;
		}
	}

	/**
	 * Description :
	 *
	 * @method :
	 * @date : 2013. 11. 28.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.common.IViewPane#getInitOperation()
	 */
	@Override
	public abstract AbstractSDVInitOperation getInitOperation();

	public abstract void initalizeLocalData(int result, IViewPane owner, IDataSet dataset);

	public abstract void uiLoadCompleted();

	/**
	 * Description :
	 *
	 * @method :
	 * @date : 2013. 11. 28.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.ui.dialog.event.ISDVInitOperationListener#startInitalize(org.sdv.core.ui.dialog.event.SDVInitEvent)
	 */
	@Override
	public void willInitalize(SDVInitEvent sdvInitEvent) {
		// 모든 뷰가 초기화 되었는지 확인하고 초기화 되면 다시 각 뷰에 알려주기 위해 확인한다.
		String viewId = sdvInitEvent.getId();
		viewInitalizeData(getView(viewId), SDVInitEvent.INIT_SUCCESS, sdvInitEvent.getData());
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
	public void failInitalize(SDVInitEvent sdvInitEvent) {
		if (this.views == null)
			return;

		// 모든 뷰가 초기화 되었는지 확인하고 초기화 되면 다시 각 뷰에 알려주기 위해 확인한다.
		String viewId = sdvInitEvent.getId();
		viewInitalizeData(getView(viewId), SDVInitEvent.INIT_FAILED, sdvInitEvent.getData());
	}

	protected void viewInitalizeData(IViewPane view, int result, IDataSet data) {
		if (view == null)
			return;
		try {
			view.initalizeData(result, this, data);
		} catch (Exception ex) {
			logger.error(ex);
		} finally {
		}
	}

	@Override
	public void initalizeData(int result, IViewPane owner, IDataSet dataset) {
		if (result == SDVInitEvent.INIT_SUCCESS) {
			initalizeLocalData(result, owner, dataset);
		}

		if (this.getViews() != null) {
			for(IViewPane view : getViews()) {
				view.initalizeLocalData(result, owner, dataset);
			}
		}
	}

	/*
	 * ############################################################################
	 *
	 *
	 * #############################################################################
	 */
	protected Button createButton(Composite parent, String id, String label, String tooltip) {
		((GridLayout) parent.getLayout()).numColumns += 1;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				AbstractSDVViewPane.this.actionPressed(event.widget.getData().toString());
			}
		});
		if(tooltip != null){
			button.setToolTipText(tooltip);
		}
		setButtonLayoutData(button);
		return button;
	}

	protected void setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.GRAB_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(DEFAULT_BUTTON_WIDTH_HINT);
		Point minSize = button.computeSize(-1, -1, true);
		int imageSize = 0;

		if(button.getImage() != null){
			imageSize =8;
		}

		if(StringUtils.isEmpty(button.getText())){
			data.widthHint = minSize.x + 4;
		}else{
			//아이콘이 포함되어 있을 경우 computeSize로 나온 값이 아이콘 크기를 포함하지 않으므로 이 값으로 처리한다.
			data.widthHint = Math.max(widthHint, minSize.x + imageSize);
		}
		button.setLayoutData(data);
	}

	protected int convertHeightInCharsToPixels(int chars) {
		Control control = getParent();
		if (getFontMetrics(control) == null) {
			return 0;
		}
		return Dialog.convertHeightInCharsToPixels(getFontMetrics(control), chars);
	}

	protected int convertHorizontalDLUsToPixels(int dlus) {
		Control control = getParent();
		if (getFontMetrics(control) == null) {
			return 0;
		}
		return Dialog.convertHorizontalDLUsToPixels(getFontMetrics(control), dlus);
	}

	protected int convertVerticalDLUsToPixels(int dlus) {
		Control control = getParent();
		if (getFontMetrics(control) == null) {
			return 0;
		}
		return Dialog.convertVerticalDLUsToPixels(getFontMetrics(control), dlus);
	}

	protected int convertWidthInCharsToPixels(int chars) {
		Control control = getParent();
		if (getFontMetrics(control) == null) {
			return 0;
		}
		return Dialog.convertWidthInCharsToPixels(getFontMetrics(control), chars);
	}

	protected static FontMetrics initializeFontUnits(Control control) {
		GC gc = new GC(control);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		return fontMetrics;
	}

	protected static FontMetrics getFontMetrics(Control control) {
		try {
			if (AbstractSDVViewPane.fontMetrics == null) {
				AbstractSDVViewPane.fontMetrics = initializeFontUnits(control);
			}
		} catch (Throwable th) {
			logger.error(th);
		}
		return AbstractSDVViewPane.fontMetrics;
	}

	protected void lockParent(boolean lock)
	{
		for (Control childControl : thisShell.getChildren())
		{
			childControl.setEnabled(lock ? false : true);
			childControl.update();
		}
//		getShell().setEnabled(! lock);
//		getShell().update();
	}

	protected void showProgress(boolean show){
		if(this.isShowProgress != show){
			if(show){
				if(progressShell == null)
				{
					try
					{
						thisShell.getDisplay().syncExec(new Runnable() {
							@Override
							public void run() {
								progressShell = new ProgressBar(thisShell);
								progressShell.start();
							}
						});
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}else if(progressShell != null){
				lockParent(false);

				thisShell.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						progressShell.close();
						progressShell = null;
					}
				});
			}

			isShowProgress = show;
		}
	}
}
