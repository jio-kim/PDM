/**
 * 
 */
package org.sdv.core.common;

import java.util.Map;

import org.eclipse.swt.SWT;

/**
 * Class Name : IViewStubBean
 * Class Description : 
 * @date 	2013. 11. 21.
 * @author  cspark
 * 
 */
public interface IViewStubBean extends IStubBean {

	public static enum ToolbarStyle{
		TOP,BOTTOM,LEFT,RIGHT,CENTER,FILL
	}
	
	public static final int TOOLBAR_DEFAULT = SWT.DEFAULT;
	public static final int TOOLBAR_ALIGN = 0;
	public static final int TOOLBAR_LOCATION = 1;
    
    public String getTitle();

    public String getDescription();

    public Map<String, IViewStubBean> getViews();

    public String getLayoutXml();
    
    /**
     * 
     * @method getToolbarActions 
     * @date 2013. 11. 22.
     * @author CS.Park
     * @return Map<String,String>
     */
    public Map<String, String> getToolbarActions();

    
    public String getToolbarAlign();
    
    
    public String getToolbarLocation();
    
    
    
    /**
     * 
     * @method getMenuActions 
     * @date 2013. 11. 22.
     * @author CS.Park
     * @return Map<String,String>
     */
    public Map<String, String> getMenuActions();
       
    public int getToolbarStyle(int style);
    
}
