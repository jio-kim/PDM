/**
 * 
 */
package com.symc.plm.me.sdv.dialog.body;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : UpdateWeldCondSheetDialog
 * Class Description : [NON-SR][20160217] taeku.jeong, 용접조건표가 개정만 되는 경우 개정 이력이 기록되지 않는 경우가 있어 이를 일괄 Update 하는 기능을위한 Dialog 생성
 * 
 * @date 2016. 02. 17.
 * 
 */
public class UpdateWeldCondSheetDialog extends SimpleSDVDialog {
    Registry registry = Registry.getRegistry(UpdateWeldCondSheetDialog.class);

    /**
     * @param shell
     * @param dialogStub
     */
    public UpdateWeldCondSheetDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }
    
    /**
     * @param shell
     * @param dialogStub
     * @param configId
     */
    public UpdateWeldCondSheetDialog(Shell shell, DialogStubBean dialogStub, int configId) {
        super(shell, dialogStub, configId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.dialog.SimpleSDVDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
        try {
            IDataSet dataSet = getDataSetAll();
            Object mecoID = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);

            if (mecoID == null || mecoID.toString().trim().length() == 0) {
                showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV, SDVPropertyConstant.LINE_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
                return false;
            }

        } catch (Exception ex) {
            showErrorMessage(ex.getMessage(), ex);
            return false;
        }

        return true;
    }
}
