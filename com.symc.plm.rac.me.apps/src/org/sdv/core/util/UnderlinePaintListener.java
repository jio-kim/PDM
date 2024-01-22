/**
 * 
 */
package org.sdv.core.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

/**
 * Class Name : UnderlinePaintListener
 * Class Description : 
 * @date 	2013. 12. 5.
 * @author  CS.Park
 * 
 */
public class UnderlinePaintListener extends AbstractCustomPaintListener {

    private boolean useShadow = false;

    /**
     * 
     */
    public UnderlinePaintListener(){
        this(null);
    }
    
    public UnderlinePaintListener(Color linecolor) {
        this(linecolor, null, 1);
    }
    
    public UnderlinePaintListener(Color linecolor, Color shdowcolor) {
        this(linecolor, shdowcolor, 1);
    }  

    public UnderlinePaintListener(Color linecolor, int linewidth) {
        this(linecolor, null, linewidth);
    }  
    
    public UnderlinePaintListener(Color linecolor, Color shdowcolor, int linewidth) {
        super(linecolor, shdowcolor, linewidth);
        this.useShadow = (shdowcolor != null);
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
    public void paintControl(PaintEvent e){
        
        Control control = (Control)e.widget;
        int x = control.getBounds().x;
        int y = control.getBounds().y;
        int width = control.getBounds().width;
        int height = control.getBounds().height;
        
        e.gc.setLineWidth(drawWidth);
        if(foreground == null){
            foreground = e.display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        }
        
        e.gc.setForeground(foreground);
        e.gc.drawLine(x, y + height - (useShadow?drawWidth*2:drawWidth), x + width, y + height -(useShadow?drawWidth*2:drawWidth)); 
        if(useShadow){
            e.gc.setForeground(background);
            e.gc.drawLine(x, y + height - drawWidth, x + width, y + height - drawWidth);
        }
    }

}
