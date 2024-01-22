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
 * Class Name : SyncFunctionsetVsMProductDialog
 * Class Description : MProduct에   Function Sync
 * 
 * [SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가를 위한 Class 신규 생성
 *
 */
public class SyncFunctionsetVsMProductDialog extends SimpleSDVDialog {

    private Registry registry = null;
    private boolean isValidateOK = false; // Validation 성공유무

    /**
     * @param shell
     * @param dialogStub
     */
    public SyncFunctionsetVsMProductDialog(Shell shell, DialogStubBean dialogStub) {
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

        String targetBomNo = dataSetAll.getStringValue("syncFunction", "TARGET_BOM_NO");
        String productNo = dataSetAll.getStringValue("syncFunction", "SRC_PRODUCT_NO");

        TCComponentItemRevision srcProduct = (TCComponentItemRevision) dataSetAll.getValue("syncFunction", "SRC_PRODUCT_REV");

        
        if (targetBomNo == null || targetBomNo.isEmpty())
            errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", registry.getString("TargetProductNo.NAME")).concat("\n"));
        if (productNo == null || productNo.isEmpty()) {
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