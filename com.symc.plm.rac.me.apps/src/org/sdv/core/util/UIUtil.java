/**
 * 
 */
package org.sdv.core.util;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;

import javax.swing.JDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import common.Logger;

/**
 * Class Name : UIUtil
 * Class Description : 
 * @date 2013. 10. 16.
 *
 */
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
    
    public static String getGenerateViewId(String viewName) {
        return viewName + "_" + UUID.randomUUID();
    }

    //==================================================================================================
    //
    //  Static Random Color
    //
    //==================================================================================================
    
    public static Color getRandomColor() {
        return getRandomColor(null, null);
    }
    
    public static Color getRandomColor(Color mix) {
        return getRandomColor(null, mix);
    }
    
    public static Color getRandomColor(Display display, Color mix) {
        if(display == null) display = Display.getDefault();
        
        //색 범위 256에서 밝은 색 위주로 하기 위해 128 이상의 값을 사용
        int red = random.nextInt(128) + 128;
        int green = random.nextInt(128) + 128;
        int blue = random.nextInt(128) + 128;

        // mix the color
        if (mix != null) {
            red = (red + mix.getRed()) / 2;
            green = (green + mix.getGreen()) / 2;
            blue = (blue + mix.getBlue()) / 2;
        }
        Color color = new Color(display, red, green, blue);
        return color;
    }    
    

    //==================================================================================================
    //
    //  Static Color
    //
    //==================================================================================================
    public static Color getColor(int colorId){
        return getColor(Display.getCurrent(), colorId);
    }

    public static Color getColor(int r, int g, int b){
        return getColor(null, r, g, b);
    }
    
    public static Color getColor(Display display, int colorId){
        if(display == null) display = Display.getDefault();
        return display.getSystemColor(colorId);
    }

    public static Color getColor(Display display, int r, int g, int b){
        if(display == null) display = Display.getDefault();
        return new Color(display, r, g, b);
    }
    
    
    public static Color getColor(String colorName, Color defaultColor){
    	
    	Color color = defaultColor;
    	try {
        	String colorId = "COLOR_" + colorName.toUpperCase();
			Field colorField = SWT.class.getDeclaredField(colorId);
			int colorNo = colorField.getInt(null);
			return getColor(colorNo);
		} catch (SecurityException e) {
			logger.warn(e);
		} catch (NoSuchFieldException e) {
			logger.warn(e);
		} catch (IllegalArgumentException e) {
			logger.warn(e);
		} catch (IllegalAccessException e) {
			logger.warn(e);
		} catch( Throwable e){
			logger.warn(e);
		}
        //return getColor(Display.getCurrent(), colorId);
        return color;
    }
    
    //==================================================================================================
    //
    //  Static GridLayout
    //
    //==================================================================================================
    
    public static GridData getGridData(int style){
        return new GridData(style);
    }

    public  static GridLayout getGridLayout(int numColumns){
        return getGridLayout(numColumns, 0, 0, 0, 0, 0, 0,  false);
    }

    public  static GridLayout getGridLayout(int numColumns, boolean makeColumnsEqualWidth){
        return getGridLayout(numColumns, 0, 0, 0, 0, 0, 0,  makeColumnsEqualWidth);
    }

    public  static GridLayout getGridLayout(int numColumns, int marginWidth, int marginHeight){
        return getGridLayout(numColumns, marginWidth, marginHeight, 0, 0, 0, 0, false);
    }

    public  static GridLayout getGridLayout(int numColumns, int marginWidth, int marginHeight, boolean makeColumnsEqualWidth){
        return getGridLayout(numColumns, marginWidth, marginHeight, 0, 0, 0, 0, makeColumnsEqualWidth);
    }

    public  static GridLayout getGridLayout(boolean makeColumnsEqualWidth){
        return getGridLayout(1, 0, 0, 0, 0, 0, 0, makeColumnsEqualWidth);
    }

    public  static GridLayout getGridLayout(int numColumns, int marginWidth, int marginHeight, int marginTop, int marginBottom, int horizontalSpacing, int verticalSpacing,  boolean makeColumnsEqualWidth){
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = numColumns;
        gridLayout.marginWidth = marginWidth;
        gridLayout.marginHeight = marginHeight;
        gridLayout.marginTop = marginTop;
        gridLayout.marginBottom = marginBottom;
        gridLayout.makeColumnsEqualWidth = makeColumnsEqualWidth;
        gridLayout.horizontalSpacing = horizontalSpacing;
        gridLayout.verticalSpacing = verticalSpacing;
        return gridLayout;
    }
    
    //==================================================================================================
    //
    //  Static FillLayout
    //
    //==================================================================================================    

    public  static FillLayout getHorizontalFillLayout(){
        return getFillLayout();
    }    

    public  static FillLayout getHorizontalFillLayout(int marginWidth, int marginHeight){
        return getFillLayout(marginWidth, marginHeight);
    }    
    
    public  static FillLayout getHorizontalFillLayout(int marginWidth, int marginHeight, int spacing){
        return getFillLayout(marginWidth, marginHeight, spacing);
    }    

    public  static FillLayout getVerticalFillLayout(){
        return getFillLayout(SWT.VERTICAL, 0, 0, 0);
    }    

    public  static FillLayout getVerticalFillLayout(int marginWidth, int marginHeight){
        return getFillLayout(SWT.VERTICAL, marginWidth, marginHeight, 0);
    }    

    public  static FillLayout getVerticalFillLayout(int marginWidth, int marginHeight, int spacing){
        return getFillLayout(SWT.VERTICAL, marginWidth, marginHeight, spacing);
    }    

    public  static FillLayout getFillLayout(){
        return getFillLayout(SWT.HORIZONTAL, 0, 0, 0);
    } 
    
    public  static FillLayout getFillLayout(int marginWidth, int marginHeight){
        return getFillLayout(SWT.HORIZONTAL, marginWidth, marginHeight, 0);
    } 
    
    public  static FillLayout getFillLayout(int marginWidth, int marginHeight, int spacing){
        return getFillLayout(SWT.HORIZONTAL, marginWidth, marginHeight, spacing);
    } 
    
    public  static FillLayout getFillLayout(int type, int marginWidth, int marginHeight, int spacing){
        FillLayout fillLayout = new FillLayout();
        if(type == SWT.VERTICAL) fillLayout =  new FillLayout(type);
        fillLayout.marginWidth = marginWidth;
        fillLayout.marginHeight = marginHeight;
        fillLayout.spacing = spacing;
        return fillLayout;
    }    
    
    public static void addColorBorder(Composite comp, Color color){
      if(comp == null) return;
      
      if(color == null) color = DEFAULT_RED_COLOR;
      final  Color borderColor = color; 
      
        
      comp.addPaintListener(new PaintListener() {
      @Override
      public void paintControl(PaintEvent e) {
            GC gc=e.gc;
            Composite source = (Composite)e.getSource();
            gc.setForeground(borderColor);
            Rectangle rect= source.getBounds();
            Rectangle rect1 = new Rectangle(rect.x, rect.y, rect.width-1, rect.height-1);
            gc.drawRectangle(rect1);
          }
      });
    }
    
    
    public static void addUnderline(Control control, Color lineColor){
        addUnderline(control, lineColor, null, 1);
    }
    
    public static void addUnderline(Control control, Color lineColor, Color shdowColor){
        addUnderline(control, lineColor, shdowColor, 1);
    }

    public static void addUnderline(Control control, Color lineColor, Color shdowColor, int lineWidth){
        if(control == null) return;
        control.addPaintListener(new UnderlinePaintListener(lineColor, shdowColor, lineWidth));
    }
    
    public static void addBoxBorder(Control control, Color lineColor){
        addBoxBorder(control, lineColor, 1);
    }
    
    public static void addBoxBorder(Control control, Color lineColor, int lineWidth){
        if(control == null) return;
        control.addPaintListener(new BoxBorderPaintListener(lineColor, lineWidth));
    }
    
    public static Image getSystemImage(int imageId){
    	return getSystemImage(null, imageId);
    }

    public static Image getSystemImage(Display display, int imageId){
    	if(display == null) display = Display.getDefault();
    	return display.getSystemImage(imageId);
    }

	/**
	 * 
	 * @method isUseRandomBackground 
	 * @date 2013. 12. 16.
	 * @author CS.Park
	 * @param
	 * @return boolean
	 * @throws
	 * @see
	 */
	public static boolean isUseRandomBackground() {
		return useRandomBackground;
	}
    
	public static boolean isUseLayoutMargin(){
		return useLayoutMargin;
	}
	
	public static void setUseRandomBackground(boolean value){
		useRandomBackground = value;
	}

	/**
	 * 
	 * @method applyRandomBackground 
	 * @date 2013. 12. 16.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws
	 * @see
	 */
	public static void applyRandomBackground(Composite composite) {
		if(composite != null && isUseRandomBackground()){
			composite.setBackground(getRandomColor());
		}
	}

	public static void centerToParent(Shell parentShell, Shell childShell){
		Rectangle shlParent = parentShell.getBounds();
		Rectangle shlChild = childShell.getBounds();
		int x = parentShell.getLocation().x + (shlParent.width - shlChild.width)/2;
		int y = parentShell.getLocation().y + (shlParent.height - shlChild.height)/2;
		childShell.setLocation(x, y);
	}

	public static void centerToParent(Shell parentShell, JDialog aifCommandDialog){
	    Rectangle shlParent = parentShell.getBounds();
	    java.awt.Rectangle shlChild = aifCommandDialog.getBounds();
    	int x = parentShell.getLocation().x + (shlParent.width - shlChild.width)/2;
    	int y = parentShell.getLocation().y + (shlParent.height - shlChild.height)/2;
    	aifCommandDialog.setLocation(x, y);
	}
}
