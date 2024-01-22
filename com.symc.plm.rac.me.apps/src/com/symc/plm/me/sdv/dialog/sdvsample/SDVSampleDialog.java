/**
 * 
 */
package com.symc.plm.me.sdv.dialog.sdvsample;

import java.util.List;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.IViewPane;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

/**
 * Class Name : SDVSampleDialog
 * Class Description :
 * 
 * @date 2013. 10. 11.
 * 
 */
public class SDVSampleDialog extends SimpleSDVDialog {
    // ActionButton
    protected Button editButton;
    // CommandButton
    protected Button searchButton;

    /**
     * @param dialogInfo
     */
    public SDVSampleDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        
//        Registry registry = Registry.getRegistry(this);
//        System.out.println("TEST.NAME : " + registry.getString("TEST.NAME"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.IViewPane#getViews()
     */
    @Override
    public List<IViewPane> getViews() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.IViewPane#getRootContext()
     */
    @Override
    public Composite getRootContext() {
        return null;
    }

     /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.IViewPane#refresh()
     */
    @Override
    public void refresh() {

    }
}
