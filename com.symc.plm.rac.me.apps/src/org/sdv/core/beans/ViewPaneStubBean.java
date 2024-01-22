/**
 *
 */
package org.sdv.core.beans;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.sdv.core.common.IViewStubBean;

/**
 * Class Name : ViewPaneStubBean
 * Class Description :
 *
 * @date 2013. 10. 14.
 *
 */
public class ViewPaneStubBean implements IViewStubBean {

   
	protected String id;
    protected String title;
    protected String description;
    protected int    configId;
    protected String order;
    
    //구현 클래스 명
    protected String implement;
    
    //View  Layout 정의
    protected String layoutXml;
    
    //자식 View Bean 정보
    protected Map<String, IViewStubBean> views;
    
    //ToolBar 명령어 및  Operation Bean ID
    protected Map<String, String> toolbarActions;
    
    //ToolBar의 버튼 Align
    protected String toolbarAlign = "LEFT";
    
    protected String toolbarLocation = "TOP";

    //ToolBar 명령어 및  Operation Bean ID
    protected Map<String, String> menuActions;

    /*
     *
     * @see org.sdv.core.common.IStubBean#getImplement()
     */
    @Override
    public String getImplement() {
        return this.implement;
    }

    /*

     *
     * @see org.sdv.core.common.IStubBean#getId()
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * @return the title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the views
     */
    @Override
    public Map<String, IViewStubBean> getViews() {
        return views;
    }

    /**
     * @param views
     *            the views to set
     */
    public void setViews(Map<String, IViewStubBean> viewStubs) {
        this.views = viewStubs;
    }

    /**
     * @return the layoutXml
     */
    @Override
    public String getLayoutXml() {
        return layoutXml;
    }

    /**
     * @param layoutXml
     *            the layoutXml to set
     */
    public void setLayoutXml(String layoutXml) {
        this.layoutXml = layoutXml;
    }

    /**
     * @return the toolbarActions
     */
    @Override
    public Map<String, String> getToolbarActions() {
        return toolbarActions;
    }

    /**
     * @param toolbarActions
     *            the toolbarActions to set
     */
    public void setToolbarActions(Map<String, String> toolbarActions) {
        this.toolbarActions = toolbarActions;
    }

    
	/**
	 * @return the toolbarAlign
	 */
	public String getToolbarAlign() {
		return toolbarAlign;
	}

	
	/**
	 * @param toolbarAlign the toolbarAlign to set
	 */
	public void setToolbarAlign(String toolbarAlign) {
		this.toolbarAlign = toolbarAlign;
	}

	/**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param implement
     *            the implement to set
     */
    public void setImplement(String implement) {
        this.implement = implement;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.intface.IStubBean#clone()
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * @return the configId
     */
    public int getConfigId() {
        return configId;
    }

    /**
     * @param configId the configId to set
     */
    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 22.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.common.IViewStubBean#getMenuActions()
     */
    @Override
    public Map<String, String> getMenuActions() {
        return null;
    }

	/**
	 * Description :
	 * @method :
	 * @date : 2013. 12. 17.
	 * @author : CS.Park
	 * @param :
	 * @return : 
	 * @see org.sdv.core.common.IViewStubBean#getToolbarLocation()
	 */
	@Override
	public String getToolbarLocation() {
		return this.toolbarLocation;
	}
	

	public void setToolbarLocation(String toolbarLocation) {
		this.toolbarLocation = toolbarLocation;
	}


	/**
	 * Description :
	 * @method :
	 * @date : 2013. 12. 17.
	 * @author : CS.Park
	 * @param :
	 * @return : 
	 * @see org.sdv.core.common.IViewStubBean#getToolbarStyle(int)
	 */
	@Override
	public int getToolbarStyle(int style) {
    	
		ToolbarStyle toolbarStyle = null;
		
    	if(style == TOOLBAR_ALIGN){
    		toolbarStyle = ToolbarStyle.valueOf(getToolbarAlign());
    	}else if(style == TOOLBAR_LOCATION){
    		toolbarStyle = ToolbarStyle.valueOf(getToolbarLocation());
    	}
    	
    	int styleVlaue = TOOLBAR_DEFAULT;
    	
    	switch(toolbarStyle){
        	case TOP 		: styleVlaue = SWT.TOP; break;
        	case BOTTOM 	: styleVlaue = SWT.BOTTOM; break;
        	case LEFT 		: styleVlaue = SWT.LEFT; break;
        	case RIGHT 		: styleVlaue = SWT.RIGHT; break;
        	case CENTER 	: styleVlaue = SWT.CENTER; break;
        	case FILL 		: styleVlaue = SWT.FILL; break;
        		default 	: styleVlaue = SWT.DEFAULT;
    	}
    	
    	return styleVlaue;
    }

}
