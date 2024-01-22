/**
 * 
 */
package org.sdv.core.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

/**
 * Class Name : BoxBorderPaintListener
 * Class Description : 
 * @date 	2013. 12. 5.
 * @author  CS.Park
 * 
 */
public class BoxBorderPaintListener extends AbstractCustomPaintListener {

    /**
     * 
     */
    public BoxBorderPaintListener(){
        super(null);
    }
    
    public BoxBorderPaintListener(Color color) {
        super(color);
    }

    public BoxBorderPaintListener(Color color, int width) {
        super(color, null, width);
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
        e.gc.drawRectangle(x, y, x + width - drawWidth , y + height -drawWidth);
        
    }

}
