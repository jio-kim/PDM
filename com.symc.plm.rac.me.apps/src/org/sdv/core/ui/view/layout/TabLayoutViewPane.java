/**
 * 
 */
package org.sdv.core.ui.view.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

/**
 * Class Name : TabLayoutViewPane
 * Class Description : 
 * @date 	2013. 12. 3.
 * @author  CS.Park
 * 
 */
public class TabLayoutViewPane extends AbstractLayoutPane {

    private TabFolder layoutComposite;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public TabLayoutViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 3.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.layout.AbstractLayoutPane#getLayoutComposite()
     */
    @Override
    public Composite getLayoutComposite() {
        if(this.layoutComposite == null){
            this.layoutComposite = new TabFolder(this, SWT.TOP);
        }
        return this.layoutComposite;
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 3.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.layout.AbstractLayoutPane#getLayoutType()
     */
    @Override
    protected LayoutType getLayoutType() {
        return LayoutType.TAB_LAYOUT;
    }

}
