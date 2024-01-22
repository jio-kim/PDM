/**
 * 
 */
package com.symc.plm.me.sdv.dialog.assembly;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : MoveBOPLineDialog
 * Class Description :
 * 
 * @date 2014. 2. 17.
 * 
 */
public class MoveAssyOPDialog extends SimpleSDVDialog {
    private Registry registry = null;

    public MoveAssyOPDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.assembly.assembly");
    }

    @Override
    protected boolean validationCheck() {

        StringBuffer errorMsg = new StringBuffer();
        IDataSet dataSetAll = this.getSelectDataSetAll();

        Object targetObject = dataSetAll.getValue("inform", "TARGET_BOMLINE");

        if (targetObject == null) {
            errorMsg.append(registry.getString("NotSelectTarget.MSG","You have not selected Target.").concat("\n"));
        }

        if (errorMsg.length() > 0) {
            MessageBox.post(getShell(), errorMsg.toString(), registry.getString("Warning.NAME"), MessageBox.WARNING);
            return false;
        }

        return true;
    }
}
