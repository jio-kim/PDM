/**
 * 
 */
package org.sdv.core.ui.view.layout;

import org.eclipse.swt.widgets.Composite;

/**
 * Class Name : SplitLayoutViewPane
 * Class Description : 
 * @date 	2013. 12. 3.
 * @author  CS.Park
 * 
 */
public class SplitLayoutViewPane extends AbstractLayoutPane {

    /**
     * @param parent
     * @param style
     * @param id
     */
    public SplitLayoutViewPane(Composite parent, int style, String id) {
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
        return this;
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
        return LayoutType.SPLIT_LAYOUT;
    }

}
