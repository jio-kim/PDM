/**
 * 
 */
package com.symc.plm.me.sdv.dialog.meco;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : MecoChangeNoDialog
 * Class Description : MECO No º¯°æ Dialog
 * 
 * @date 2014. 2. 5.
 * 
 */
public class ChangeMecoNoDialog extends SimpleSDVDialog {

    private Registry registry = null;

    /**
     * @param shell
     * @param dialogStub
     */
    public ChangeMecoNoDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.meco.meco");
    }


    @Override
    protected boolean validationCheck() {
        TCComponentItemRevision mecoRevision = null;
        StringBuffer errorMsg = new StringBuffer();

        IDataSet dataSetAll = this.getSelectDataSetAll();
        Object newMecoObj = dataSetAll.getValue("inform", SDVPropertyConstant.ITEM_REV_MECO_NO);

        if (newMecoObj != null)
            mecoRevision = (TCComponentItemRevision) newMecoObj;

        if (mecoRevision == null)
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", "MECO NO").concat("\n"));

        if (errorMsg.length() > 0) {
            MessageBox.post(getShell(), errorMsg.toString(), registry.getString("Warning.NAME"), MessageBox.WARNING);
            return false;
        }

        return true;
    }

}
