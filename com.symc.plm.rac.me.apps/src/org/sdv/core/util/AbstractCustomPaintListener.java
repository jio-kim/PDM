/**
 * 
 */
package org.sdv.core.util;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;

/**
 * Class Name : AbstractCustomPaintListener
 * Class Description : 
 * @date 	2013. 12. 5.
 * @author  CS.Park
 * 
 */
public class AbstractCustomPaintListener implements PaintListener {

    
    public static final Color DEFAULT_FOREGROUND_COLOR = new Color(null, 0,0,0);
    public static final Color DEFAULT_BACKGROUND_COLOR = new Color(null, 255,255,255);
    
    protected Color foreground;
    
    protected Color background;
    
    protected int drawWidth =1;
    
    /**
     * 
     */
    public AbstractCustomPaintListener() {
        this(DEFAULT_FOREGROUND_COLOR, DEFAULT_BACKGROUND_COLOR);
    }
    
    public AbstractCustomPaintListener(Color foreground) {
        this(foreground, DEFAULT_BACKGROUND_COLOR);
    }

    public AbstractCustomPaintListener(Color foreground, Color background) {
        this.foreground = foreground;
        this.background = background;
    }
    
    public AbstractCustomPaintListener(Color foreground, Color background, int drawWidth) {
        this.foreground = foreground;
        this.background = background;
        this.drawWidth  = drawWidth;
    }    
    
    public Color getColor(){
        return getForegroundColor();
    }
    
    public void setColor(Color foreground){
        setForegroundColor(foreground);
    }      
    
    public Color getForegroundColor(){
        return foreground;
    }
    
    public void setForegroundColor(Color foreground){
        this.foreground = foreground;
    }    
    
    public Color getBackgroundColor(){
        return background;
    }
    
    public void setBackgroundColor(Color background){
        this.background = background;
    }
    
    public int getDrawWidth(){
        return this.drawWidth;
    }
    
    public void setDrawWidth(int width){
        this.drawWidth = width;
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 5.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    @Override
    public void paintControl(PaintEvent e) {
    }

}
