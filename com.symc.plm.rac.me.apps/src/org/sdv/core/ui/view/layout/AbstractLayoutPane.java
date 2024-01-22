/**
 * 
 */
package org.sdv.core.ui.view.layout;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import swing2swt.layout.BorderLayout;

/**
 * Class Name : AbstractLayoutPane
 * Class Description :
 * 
 * @date 2013. 12. 3.
 * @author CS.Park
 * 
 */
public abstract class AbstractLayoutPane extends Composite {

    protected String id;

    /**
     * @param parent
     * @param style
     */
    public AbstractLayoutPane(Composite parent, int style, String id) {
        super(parent, style);
        this.id = id;
        this.setLayout(getLayoutType(), style);
    }

    protected void setLayout(LayoutType type, int style) {
        switch (type) {
        case BORDER_LAYOUT:
            getLayoutComposite().setLayout(new BorderLayout());
            break;
        case GRID_LAYOUT:
            getLayoutComposite().setLayout(new GridLayout());
            break;
        case SPLIT_LAYOUT:
            getLayoutComposite().setLayout(new GridLayout(2, false));
            break;
        case TAB_LAYOUT:
        case FILL_LAYOUT:
        default:
            getLayoutComposite().setLayout(new FillLayout(style));
            break;
        }
    }

    public abstract Composite getLayoutComposite();

    protected abstract LayoutType getLayoutType();

}
