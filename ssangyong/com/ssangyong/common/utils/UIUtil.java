/**
 * 
 */
package com.ssangyong.common.utils;

import java.util.Random;

import javax.swing.JDialog;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import common.Logger;

/**
 * Class Name : UIUtil
 * Class Description : 
 * @date 2013. 10. 16.
 *
 */
@SuppressWarnings("unused")
public class UIUtil {
    
    private static final Color DEFAULT_RED_COLOR = new Color(null, 0,0,0);
    private static Logger logger  = Logger.getLogger(UIUtil.class);
    
    private static Random random = new Random();
    
    public static boolean useLayoutMargin = false;
    public static boolean useRandomBackground = false;
    
    static{
        useLayoutMargin 	= Boolean.parseBoolean(System.getProperty("USE_LAYOUT_MARGIN"));
        useRandomBackground = Boolean.parseBoolean(System.getProperty("USE_RANDOM_BACKGROUND"));
    }    
    
	public static void centerToParent(Shell parentShell, JDialog aifCommandDialog){
	    Rectangle shlParent = parentShell.getBounds();
	    java.awt.Rectangle shlChild = aifCommandDialog.getBounds();
    	int x = parentShell.getLocation().x + (shlParent.width - shlChild.width)/2;
    	int y = parentShell.getLocation().y + (shlParent.height - shlChild.height)/2;
    	aifCommandDialog.setLocation(x, y);
	}


}
