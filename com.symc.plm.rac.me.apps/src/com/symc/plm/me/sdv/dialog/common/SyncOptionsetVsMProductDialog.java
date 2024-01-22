/**
 * 
 */
package com.symc.plm.me.sdv.dialog.common;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SyncOptionsetVsMProductDialog
 * Class Description : MProduct俊   可记 Sync. Update Dialog
 * @date 2014. 2. 3.
 *
 */
public class SyncOptionsetVsMProductDialog extends SimpleSDVDialog {

    private Registry registry = null;
    private boolean isValidateOK = false; // Validation 己傍蜡公

    /**
     * @param shell
     * @param dialogStub
     */
    public SyncOptionsetVsMProductDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
        registry = Registry.getRegistry("com.symc.plm.me.sdv.view.assembly.assembly");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.dialog.AbstractSDVUIREnderDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {

        if (isValidateOK)
            return true;
        StringBuffer errorMsg = new StringBuffer();

        IDataSet dataSetAll = getSelectDataSetAll();

        String targetBomNo = dataSetAll.getStringValue("updateOption", "TARGET_BOM_NO");
        String productNo = dataSetAll.getStringValue("updateOption", "SRC_PRODUCT_NO");

        TCComponentItemRevision srcProduct = (TCComponentItemRevision) dataSetAll.getValue("updateOption", "SRC_PRODUCT_REV");

        // MECO No眉农
        if (targetBomNo.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("TargetProductNo.NAME")).concat("\n"));
        if (productNo.isEmpty()) {
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("SrcProductNo.NAME")).concat("\n"));
        } else {
            if (srcProduct == null)
                errorMsg.append(registry.getString("WrongInputCheck.MSG").replace("%0", registry.getString("SrcProductNo.NAME")).concat("\n"));
        }

        if (errorMsg.length() > 0) {
            MessageBox.post(getShell(), errorMsg.toString(), registry.getString("Warning.NAME"), MessageBox.WARNING);
            return false;
        }
        isValidateOK = true;
        return true;
    }
}